plugins {
    id("java")
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

configurations.create("shade").setExtendsFrom(listOf(configurations.getByName("implementation")))
application.mainClass.set("org.wallentines.mcping.Main")

dependencies {

    implementation(project(":api"))
    implementation("org.wallentines:midnightcfg:1.0-SNAPSHOT")
    implementation("io.netty:netty-all:4.1.86.Final")

}

java {
    manifest {
        attributes(Pair("Main-Class", application.mainClass))
    }
}

tasks.withType<JavaExec> {

    workingDir = File("run")
}
