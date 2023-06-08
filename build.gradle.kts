plugins {
  id("java")
  id("org.jetbrains.intellij") version "1.8.0"
}

group = "com.wh"
version = "1.0"

// 顶层结构
tasks.jar.configure {
  duplicatesStrategy = org.gradle.api.file.DuplicatesStrategy.INCLUDE
  from(configurations.runtimeClasspath.get().filter { it.name.endsWith("jar")}.map { zipTree(it) })
}

// 顶层结构
repositories {
  mavenLocal()
  maven("https://maven.aliyun.com/nexus/content/repositories/central/")
  mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
  version.set("2022.2.3")
  type.set("IC") // Target IDE Platform

  plugins.set(listOf(/* Plugin Dependencies */))
}

tasks {
  // Set the JVM compatibility versions
  withType<JavaCompile> {
    sourceCompatibility = "11"
    targetCompatibility = "11"
  }

  patchPluginXml {
    sinceBuild.set("213")
    untilBuild.set("233.*")
  }

  signPlugin {
    certificateChainFile.set(file("certificate/chain.crt"))
    privateKeyFile.set(file("certificate/private.pem"))
    password.set("112233wh")
  }

  publishPlugin {
    token.set("perm:1")
  }

}
