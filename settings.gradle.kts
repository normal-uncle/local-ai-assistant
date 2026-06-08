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

rootProject.name = "Assistant"
include(":app")
include(":ui_component")
include(":local")
include(":repository")
include(":usecase")
include(":feature_capture")
include(":feature_memo")
include(":remote")
include(":feature_onboarding")
include(":feature_chat")
include(":feature_settings")
include(":ai")
