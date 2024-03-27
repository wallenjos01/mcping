plugins {
    id("mcping-build")
    id("application")
    alias(libs.plugins.shadow)
}

configurations.create("shade").setExtendsFrom(listOf(configurations.getByName("implementation")))
application.mainClass.set("org.wallentines.mcping.Main")

dependencies {

    implementation(project(":api"))

    implementation(libs.midnight.cfg)
    implementation(libs.netty.buffer)
    implementation(libs.netty.codec)
    implementation(libs.netty.codec.dns)
    implementation(libs.netty.codec.haproxy)
    implementation(libs.netty.common)
    implementation(libs.netty.handler)
    implementation(libs.slf4j.api)
    implementation(libs.slf4j.simple)
    compileOnly(libs.jetbrains.annotations)
}


java {
    manifest {
        attributes(Pair("Main-Class", application.mainClass))
    }
}


tasks.withType<JavaExec> {

    workingDir = File("run")
}
