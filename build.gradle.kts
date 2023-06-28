plugins {
    id("java")
}

group = "org.wallentines"
version = "1.0-SNAPSHOT"

subprojects.forEach { sp ->

    sp.apply(plugin = "java")

    sp.java.sourceCompatibility = JavaVersion.VERSION_17
    sp.java.targetCompatibility = JavaVersion.VERSION_17

    sp.repositories {
        mavenCentral()
        maven("https://maven.wallentines.org/")
        mavenLocal()
    }

    sp.dependencies {

        compileOnly("org.wallentines:midnightcfg:1.0-SNAPSHOT")

        compileOnly("io.netty:netty-all:4.1.86.Final")

        testImplementation("org.wallentines:midnightlib:1.0-SNAPSHOT")
        testImplementation("org.wallentines:midnightcfg:1.0-SNAPSHOT")
        testImplementation(platform("org.junit:junit-bom:5.9.2"))
        testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    }

    sp.tasks.test {
        useJUnitPlatform()
    }

}

