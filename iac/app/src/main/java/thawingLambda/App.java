package thawingLambda;

import java.util.List;
import java.util.Map;

import com.pulumi.Config;
import com.pulumi.Context;
import com.pulumi.Pulumi;
import com.pulumi.asset.FileArchive;
import com.pulumi.aws.dynamodb.Table;
import com.pulumi.aws.dynamodb.TableArgs;
import com.pulumi.aws.dynamodb.inputs.TableAttributeArgs;
import com.pulumi.aws.ec2.Instance;
import com.pulumi.aws.ec2.InstanceArgs;
import com.pulumi.aws.ec2.SecurityGroup;
import com.pulumi.aws.ec2.SecurityGroupArgs;
import com.pulumi.aws.ec2.enums.InstanceType;
import com.pulumi.aws.ec2.inputs.SecurityGroupEgressArgs;
import com.pulumi.aws.ec2.inputs.SecurityGroupIngressArgs;
import com.pulumi.aws.iam.IamFunctions;
import com.pulumi.aws.iam.InstanceProfile;
import com.pulumi.aws.iam.InstanceProfileArgs;
import com.pulumi.aws.iam.Role;
import com.pulumi.aws.iam.RoleArgs;
import com.pulumi.aws.iam.inputs.GetPolicyDocumentArgs;
import com.pulumi.aws.iam.inputs.GetPolicyDocumentStatementArgs;
import com.pulumi.aws.iam.inputs.GetPolicyDocumentStatementPrincipalArgs;
import com.pulumi.aws.iam.outputs.GetPolicyDocumentResult;
import com.pulumi.aws.lambda.Function;
import com.pulumi.aws.lambda.FunctionArgs;
import com.pulumi.aws.lambda.inputs.FunctionEnvironmentArgs;
import com.pulumi.aws.lambda.inputs.FunctionSnapStartArgs;

public class App {

    private static int MEMORY_SIZE = 256;

    private final Context context;
    private final Config config;

    public static void main(String[] args) {
        Pulumi.run(ctx -> {
            var app = new App(ctx);
            app.synth();
        });
    }

    private App(Context context) {
        this.context = context;
        this.config = context.config();
    }

    public void synth() {

        var database = new Table("UsersTable",
                TableArgs.builder()
                        .attributes(
                                TableAttributeArgs.builder()
                                        .name("id")
                                        .type("S")
                                        .build()
                        )
                        .hashKey("id")
                        .billingMode("PAY_PER_REQUEST")
                        .build()
        );

        var lambdaRole = new Role("LambdaRole", RoleArgs.builder()
                .assumeRolePolicy("""
                        {
                            "Version": "2012-10-17",
                            "Statement": [{
                                "Effect": "Allow",
                                "Principal": {
                                        "Service": "lambda.amazonaws.com"
                                },
                                "Action": "sts:AssumeRole"
                            }]
                        }
                    """)
                .managedPolicyArns(
                        "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole",
                        "arn:aws:iam::aws:policy/AmazonDynamoDBFullAccess"
                )
                .build());

        var lambdaEnvironment = FunctionEnvironmentArgs.builder()
                .variables(database.name().applyValue(name -> Map.of(
                        "USER_TABLE_NAME", name
                )))
                .build();
        var springCfLambda = new Function("SpringCloudFunctionLambda", FunctionArgs
                .builder()
                .code(new FileArchive("../lambdas/spring-cf/build/libs/spring-cf-0.1-aws.jar"))
                .role(lambdaRole.arn())
                .runtime("java17")
                .handler("org.springframework.cloud.function.adapter.aws.FunctionInvoker")
                .environment(lambdaEnvironment)
                .timeout(60)
                .memorySize(MEMORY_SIZE)
                .build());
        context.export("SpringCfLambda", springCfLambda.name());

        var micronautLambda = new Function("MicronautLambda", FunctionArgs
                .builder()
                .code(new FileArchive("../lambdas/micronaut/build/libs/micronaut-0.1-all.jar"))
                .role(lambdaRole.arn())
                .runtime("java17")
                .handler("com.x10.lambda.UserRequestHandler")
                .environment(lambdaEnvironment)
                .timeout(60)
                .memorySize(MEMORY_SIZE)
                .build());
        context.export("MicronautLambda", micronautLambda.name());

        var micronautSnapStartLambda = new Function("MicronautSnapStartLambda", FunctionArgs
                .builder()
                .code(new FileArchive("../lambdas/micronaut/build/libs/micronaut-0.1-all.jar"))
                .role(lambdaRole.arn())
                .runtime("java17")
                .handler("com.x10.lambda.UserRequestHandler")
                .environment(lambdaEnvironment)
                .timeout(60)
                .memorySize(MEMORY_SIZE)
                .snapStart(FunctionSnapStartArgs.builder()
                        .applyOn("PublishedVersions")
                        .build())
                .build());
        context.export("MicronautSnapStartLambda", micronautSnapStartLambda.name());

        var springCfSnapStartLambda = new Function("SpringCloudFunctionSnapStartLambda", FunctionArgs
                .builder()
                .code(new FileArchive("../lambdas/spring-cf/build/libs/spring-cf-0.1-aws.jar"))
                .role(lambdaRole.arn())
                .runtime("java17")
                .handler("org.springframework.cloud.function.adapter.aws.FunctionInvoker")
                .environment(lambdaEnvironment)
                .timeout(60)
                .memorySize(MEMORY_SIZE)
                .snapStart(FunctionSnapStartArgs.builder()
                        .applyOn("PublishedVersions")
                        .build())
                .build());
        context.export("SpringCfSnapStartLambda", springCfSnapStartLambda.name());

        var micronautNativeImageLambda = new Function("MicronautNativeImageLambda", FunctionArgs
                .builder()
                .code(new FileArchive("../lambdas/micronaut/build/libs/micronaut-0.1-lambda.zip"))
                .role(lambdaRole.arn())
                .runtime("provided.al2")
                .handler("com.x10.lambda.UserRequestHandler")
                .environment(lambdaEnvironment)
                .timeout(60)
                .memorySize(MEMORY_SIZE)
                //.memorySize(128)
                .build());
        context.export("MicronautNativeLambda", micronautNativeImageLambda.name());

        var rustLambda = new Function("RustLambda", FunctionArgs
                .builder()
                .code(new FileArchive("../lambdas/rust/bootstrap.zip"))
                .role(lambdaRole.arn())
                .runtime("provided.al2")
                .handler("unused")
                .environment(lambdaEnvironment)
                .timeout(60)
                .memorySize(MEMORY_SIZE)
                //.memorySize(128)
                .build());
        context.export("RustLambda", rustLambda.name());

        var sg = new SecurityGroup("BenchmarkSecurityGroup", SecurityGroupArgs.builder()
                .vpcId("vpc-0636e418250603a90")
                .ingress(SecurityGroupIngressArgs.builder()
                        .cidrBlocks(context.config().require("sshSource"))
                        .fromPort(22)
                        .toPort(22)
                        .protocol("tcp")
                        .build())
                .egress(SecurityGroupEgressArgs.builder()
                        .cidrBlocks("0.0.0.0/0")
                        .protocol("-1")
                        .fromPort(0)
                        .toPort(0)
                        .build())
                .build());


        final var assumeRole = IamFunctions.getPolicyDocument(GetPolicyDocumentArgs.builder()
                .statements(GetPolicyDocumentStatementArgs.builder()
                        .effect("Allow")
                        .principals(GetPolicyDocumentStatementPrincipalArgs.builder()
                                .type("Service")
                                .identifiers("ec2.amazonaws.com")
                                .build())
                        .actions("sts:AssumeRole")
                        .build())
                .build());

        var instanceRole = new Role("InstanceRole", RoleArgs.builder()
                .assumeRolePolicy(assumeRole.applyValue(GetPolicyDocumentResult::json))
                .managedPolicyArns(
                        "arn:aws:iam::aws:policy/AWSLambda_FullAccess"
                )
                .build());
        var instanceProfile = new InstanceProfile("InstanceProfile", InstanceProfileArgs.builder()
                .role(instanceRole.name())
                .build());

        var executor = new Instance("BenchmarkInstance", InstanceArgs.builder()
                .ami("ami-016b30666f212275a")
                .associatePublicIpAddress(true)
                .vpcSecurityGroupIds(sg.id().applyValue(List::of))
                .instanceType(InstanceType.M6i_Large)
                .subnetId("subnet-0d4700323e13be5ec")
                .iamInstanceProfile(instanceProfile.name())
                .keyName("raniz@raniz-x10-1")
                .build());
    }

}
