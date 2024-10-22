plugins {
    id("build.application")
    id("build.library")
    id("build.shadow")
}

application.mainClass.set("org.wallentines.mcping.Main")

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
    implementation(libs.slf4j.api)
    implementation(libs.slf4j.simple)

    shadow(project(":api")) { isTransitive = false }

    shadow(libs.midnight.cfg) { isTransitive = false }
    shadow(libs.midnight.cfg.json) { isTransitive = false }
    shadow(libs.netty.buffer)
    shadow(libs.netty.codec)
    shadow(libs.netty.codec.dns)
    shadow(libs.netty.codec.haproxy)
    shadow(libs.netty.common)
    shadow(libs.netty.handler)
    shadow(libs.slf4j.api)
    shadow(libs.slf4j.simple)

}
