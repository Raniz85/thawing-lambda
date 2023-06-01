plugins {
    id("application")
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("com.pulumi:pulumi:(,1.0]")
    implementation("com.pulumi:aws:[5.0,6.0)")
}

application {
    mainClass.set("thawingLambda.App")
}
