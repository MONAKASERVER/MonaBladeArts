plugins { java }

group = "net.monacraft"
version = "1.0.0-alpha.1"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.74-stable")
    compileOnly(files("../SlimefunCore4-2834.jar"))
    compileOnly(files("../.codex-work/craft-engine-paper-plugin-26.8.jar"))
    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java { toolchain.languageVersion.set(JavaLanguageVersion.of(25)) }

tasks {
    compileJava { options.encoding = "UTF-8"; options.release.set(25) }
    processResources {
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") { expand("version" to project.version) }
    }
    test { useJUnitPlatform() }
    jar { archiveFileName.set("MonaBladeArts-${project.version}.jar") }
}
