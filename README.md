# Spek IntelliJ IDEA Plugin
This is the official IntelliJ IDEA plugin for [Spek](https://github.com/JetBrains/spek).

## Features
- Run specs directly from IDEA.
- Choose a specific group/test to run within a spec.

## Requirements
- Make sure you have compatible versions of `org.junit.platform:junit-platform-launcher` and `org.jetbrains.spek:spek-junit-platform-engine` in the test runtime classpath.


## What's missing?
- Navigate to source via the test tree.

## Known Limitations
- If your class is annotated with `@RunWith(...)` the junit plugin will take over and this plugin will not work.
- For Android Studio, you need to add a `Before launch` gradle step to compile your test classes. If you have the default
setup, adding `assembleDebugUnitTest` should suffice.

# Development
Import the project to any IDE that supports gradle, do note that if you're using IntelliJ IDEA the imported project 
is not marked as IDEA plugin project.

## Versions
The project uses `com.zoltu.git-versioning`, which means versions are maintained via git tags.


## Testing
`./gradlew plugin:runIde -Pprofile={profile}`. 

List of profiles can be found at `plugin.gradle`.

## Building
`./gradlew clean plugin:builPlugin`
