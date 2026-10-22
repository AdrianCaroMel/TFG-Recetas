pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TFG-Recetas"

// :core -> lógica de negocio en Kotlin puro (modelos, catálogo, planificador). Testeable sin Android.
// :app  -> aplicación Android (Jetpack Compose) que usa :core.
include(":core")
include(":app")
