package benchmark

import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.InvocationType
import software.amazon.awssdk.services.lambda.model.LambdaException
import software.amazon.awssdk.services.lambda.model.LogType
import software.amazon.awssdk.services.lambda.model.PublishVersionResponse
import software.amazon.awssdk.services.lambda.model.ResourceConflictException
import software.amazon.awssdk.services.lambda.model.TooManyRequestsException
import java.lang.Thread.sleep
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.UUID
import java.util.regex.Pattern

val reportPattern: Pattern = Pattern.compile("REPORT RequestId: [a-z0-9-]+\\s+Duration:\\s(?<duration>[0-9.]+) ms(.*?Init Duration:\\s(?<init>[0-9.]+) ms)?")

fun parseLogToColdStartTime(tail: String?, requireInit: Boolean): Double? {
    if (tail == null) {
        throw RuntimeException("Did not get any log back");
    }
    val log = String(Base64.getDecoder().decode(tail))
    val match = reportPattern.matcher(log)
    if (match.find()) {
        val duration = match.group("duration").toDouble()
        if (requireInit && match.group("init") == null) {
            return null
        }
        val init = (match.group("init") ?: "0").toDouble()
        return init + duration
    } else {
        throw RuntimeException("No timing found in log")
    }
}

data class PublishingLambdaFunction(
    val lambda: LambdaClient,
    val name: String
) {
    private fun benchmarkColdStart(): Double? {
        val currentConfiguration = lambda.getFunctionConfiguration {
            it.functionName(name)
        }
        val variables = HashMap(currentConfiguration.environment().variables())
        variables.put("random", UUID.randomUUID().toString())
        for (i in (0..5)) {
            try {
                lambda.updateFunctionConfiguration {
                    it.functionName(name)
                        .environment { env ->
                            env.variables(variables)
                        }
                }
                break
            } catch (e: ResourceConflictException) {
                // Old update still running, wait
                sleep(5000)
            }
        }
        lambda.waiter().waitUntilFunctionUpdated {
            it.functionName(name)
        }
        val publishedVersion: PublishVersionResponse = (0..5).firstNotNullOf {
            try {
                return@firstNotNullOf lambda.publishVersion { it.functionName(name) }
            } catch (e: LambdaException) {
                when (e) {
                    is ResourceConflictException,
                    is TooManyRequestsException -> {
                        // Cool down a bit
                        println("%45s    : Too many snapshots; sleeping for 30 s".format(name))
                        sleep(30000)
                        return@firstNotNullOf null
                    }
                    else -> throw e
                }
            }
        }
        val version = publishedVersion.version()
        lambda.waiter().waitUntilFunctionActiveV2 {
            it.functionName(name)
                .qualifier(version)
        }
        val result = lambda.invoke {
            it.functionName(name)
                .qualifier(version)
                .payload(SdkBytes.fromString("{ \"name\": \"John Doe\"}", StandardCharsets.UTF_8))
                .invocationType(InvocationType.REQUEST_RESPONSE)
                .logType(LogType.TAIL)
        }
        if (result.statusCode() >= 300) {
            throw RuntimeException("Lambda invocation failed for ${name}:$version")
        }
        val coldStartTime = parseLogToColdStartTime(result.logResult(), !name.contains("SnapStart"))
        lambda.deleteFunction {
            it.functionName(name)
                .qualifier(version)
        }
        return coldStartTime
    }

    fun collectResults(n: Int): List<Double> {
        val results = ArrayList<Double>()
        while (results.size < n) {
            val coldStartTime = benchmarkColdStart()
            if (coldStartTime != null) {
                println("%45s (%03d): %5.3f ms".format(name, results.size + 1, coldStartTime))
                results.add(coldStartTime)
            } else {
                println("%45s (%03d): Warm :(".format(name, results.size + 1))
            }
        }
        return results.toList()
    }

}
