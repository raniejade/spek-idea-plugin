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
