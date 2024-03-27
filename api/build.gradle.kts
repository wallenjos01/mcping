plugins {
    id("mcping-build")
    id("mcping-publish")
}

dependencies {

    api(libs.midnight.cfg)
    api(libs.midnight.cfg.json)
    api(libs.netty.buffer)
    api(libs.netty.codec)
    api(libs.netty.codec.haproxy)
    api(libs.slf4j.api)
    api(libs.jetbrains.annotations)

}
