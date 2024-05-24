plugins {
    id("mcping-build")
    id("application")
    alias(libs.plugins.shadow)
}

configurations.shadow {
    extendsFrom(configurations.implementation.get())
}
application.mainClass.set("org.wallentines.mcping.Main")

tasks.jar {
    archiveClassifier.set("partial")
}

tasks.shadowJar {
    archiveClassifier.set("")
}

dependencies {

    implementation(project(":api")) { isTransitive = false }

    implementation(libs.midnight.cfg) { isTransitive = false }
    implementation(libs.midnight.cfg.json) { isTransitive = false }
    implementation(libs.netty.buffer)
    implementation(libs.netty.codec)
    implementation(libs.netty.codec.dns)
    implementation(libs.netty.codec.haproxy)
    implementation(libs.netty.common)
    implementation(libs.netty.handler)
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

tasks.register<Copy>("copyFinalJar") {

    dependsOn(tasks.shadowJar)
    from(tasks.shadowJar.get().archiveFile)

    var output = "build/output"
    if(project.hasProperty("outputDir")) {
        output = project.properties["outputDir"] as String
    }

    into(output)
    rename("(.*)\\.jar", "${rootProject.name}.jar")
}
