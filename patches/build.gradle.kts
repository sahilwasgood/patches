group = "app.sahilwasgood"

patches {
    // TODO: Update this section with your project details.
    about {
        name = "sahilwasgood's patches"
        description = "Patches for apps I use: branding presets, button styles and more"
        source = "https://github.com/sahilwasgood/patches"
        author = "sahilwasgood"
        contact = "https://github.com/sahilwasgood"
        website = "https://github.com/sahilwasgood/patches"
        license = "GPLv3"
    }
}

// Separate configuration so gson is available at runtime for the
// generatePatchesList task but never bundled into the APK.
val patchListGeneratorClasspath = configurations.create("patchListGeneratorClasspath")

dependencies {
    compileOnly(libs.gson)
    patchListGeneratorClasspath(libs.gson)
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"

        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
        mainClass.set("util.PatchListGeneratorKt")
    }

    // Used by gradle-semantic-release-plugin.
    publish {
        dependsOn("generatePatchesList")
    }
}
