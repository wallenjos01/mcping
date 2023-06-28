import java.net.URI

plugins {
    id("maven-publish")
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            groupId = (project.parent as Project).group as String
            artifactId = (project.parent as Project).name
            version = (project.parent as Project).version as String
            artifact(sourcesJar.get())
        }
    }
    repositories {
        maven {
            if(project.hasProperty("pubUrl")) {
                url = URI.create(project.properties["pubUrl"] as String)
                credentials {
                    username = project.properties["pubUser"] as String
                    username = project.properties["pubPass"] as String
                }
            }
        }
    }
}