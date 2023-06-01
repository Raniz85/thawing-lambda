package benchmark;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvocationType;
import software.amazon.awssdk.services.lambda.model.LogType;
import software.amazon.awssdk.services.lambda.model.ResourceConflictException;
import software.amazon.awssdk.services.lambda.model.TooManyRequestsException;

public class LambdaFunction {

    static final Pattern durationPattern = Pattern.compile("Duration:\\s(?<duration>[0-9.]+) ms");
    static final Pattern initPattern = Pattern.compile("Init Duration:\\s(?<init>[0-9.]+) ms");
    static final Pattern restorePattern = Pattern.compile("Restore Duration:\\s(?<restore>[0-9.]+) ms");
    private final LambdaClient lambda;
    private final String name;

    private static Double parseLogToColdStartTime(String tail, Boolean requireInit) {
        if (tail == null) {
            throw new RuntimeException("Did not get any log back");
        }
        var log = new String(Base64.getDecoder().decode(tail));
        var match = durationPattern.matcher(log);
        if (match.find()) {
            var duration = Double.parseDouble(match.group("duration"));
            var initMatch = initPattern.matcher(log);
            var restoreMatch = restorePattern.matcher(log);
            Double init = null;
            if (initMatch.find()) {
                init = Double.parseDouble(initMatch.group("init"));
            } else if (restoreMatch.find()) {
                init = Double.parseDouble(restoreMatch.group("restore"));
            } else {
                return null;
            }
            return init + duration;
        } else {
            throw new RuntimeException("No timing found in log");
        }
    }

    public LambdaFunction(LambdaClient lambda, String name) {
        this.lambda = lambda;
        this.name = name;
    }

    private String publish() {
        System.out.println("Getting current configuration");
        var currentConfiguration = lambda.getFunctionConfiguration((it) -> {
            it.functionName(name);
        });
        var variables = new HashMap<>(currentConfiguration.environment().variables());
        variables.put("random", UUID.randomUUID().toString());
        for (int i = 0; i < 5; i++) {
            try {
                System.out.println("Updating function");
                lambda.updateFunctionConfiguration(it -> {
                    it.functionName(name)
                            .environment(env -> env.variables(variables)
                            );
                });
                break;
            } catch (ResourceConflictException e) {
                // Old update still running, wait
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                }
            }
        }
        System.out.println("Waiting for function to update");
        lambda.waiter().waitUntilFunctionUpdated(it -> {
            it.functionName(name);
        });
        System.out.println("Publishing version");
        var publishedVersion = lambda.publishVersion(it -> it.functionName(name));
        var version = publishedVersion.version();
        System.out.printf("Waiting for published version %s to be active%n", publishedVersion.version());
        lambda.waiter().waitUntilFunctionActiveV2(it ->
                it.functionName(name)
                        .qualifier(version)
        );
        // Prime it
        try {
            takeBenchMark(version);
        } catch (Exception e) {
        }
        return version;
    }

    public ArrayList<Double> benchmark(ExecutorService executor, int concurrency, int n) {
        var allResults = new ArrayList<Double>();
        while (allResults.size() < n) {
            var results = Collections.synchronizedList(new ArrayList<Double>());
            var qualifier = publish();
            System.out.printf("Collecting results for %s:%s%n", name, qualifier);
            var futures = IntStream.range(0, concurrency).mapToObj(__ -> CompletableFuture.runAsync(() -> {
                var coldStartTime = takeBenchMark(qualifier);
                if (coldStartTime != null) {
                    System.out.printf("%45s : %5.3f ms%n", name, coldStartTime);
                    results.add(coldStartTime);
                } else {
                    System.out.printf("%45s : warm%n", name);
                }
            }, executor))
            .toList();
            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).exceptionally(__ -> null).join();
            allResults.addAll(results);
            destroy(qualifier);
        }
        return allResults;
    }

    private void destroy(String qualifier) {
        lambda.deleteFunction(it ->
                it.functionName(name)
                        .qualifier(qualifier)
        );
    }

    private Double takeBenchMark(String qualifier) {
        try {
            var result = lambda.invoke(it ->
                    it.functionName(name)
                            .qualifier(qualifier)
                            .payload(SdkBytes.fromString("{ \"name\": \"John Doe\"}", StandardCharsets.UTF_8))
                            .invocationType(InvocationType.REQUEST_RESPONSE)
                            .logType(LogType.TAIL)
            );
            if (result.statusCode() >= 300) {
                throw new RuntimeException("Lambda invocation failed for ${name}:$qualifier");
            }
            var coldStartTime = parseLogToColdStartTime(result.logResult(), !name.contains("SnapStart"));
            return coldStartTime;
        } catch (TooManyRequestsException | SdkClientException e) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
            return null;
        }
    }
}
