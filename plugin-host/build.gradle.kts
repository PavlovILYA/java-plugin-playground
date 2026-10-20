plugins {
    application
}

dependencies {
    implementation(project(":plugin-api"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("playground.host.Main")
}

tasks.test {
    useJUnitPlatform()
    dependsOn(":plugin-matrix:jar", ":plugin-aquarium:jar")
    systemProperty("matrixJar", rootProject.layout.projectDirectory.file("plugin-matrix/build/libs/plugin-matrix.jar").asFile.absolutePath)
    systemProperty("aquariumJar", rootProject.layout.projectDirectory.file("plugin-aquarium/build/libs/plugin-aquarium.jar").asFile.absolutePath)
}

tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
    standardInput = System.`in`
}
