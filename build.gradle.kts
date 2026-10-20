subprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
    }

    extensions.configure<org.gradle.api.plugins.JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    tasks.withType<org.gradle.api.tasks.compile.JavaCompile>().configureEach {
        options.release.set(21)
    }
}

tasks.register<Copy>("preparePlugins") {
    dependsOn(":plugin-matrix:jar", ":plugin-aquarium:jar")
    from("plugin-matrix/build/libs/plugin-matrix.jar")
    from("plugin-aquarium/build/libs/plugin-aquarium.jar")
    into("plugins")
}
