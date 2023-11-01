![Build](https://github.com/manami-project/kommand/actions/workflows/build.yml/badge.svg) [![Coverage Status](https://coveralls.io/repos/github/manami-project/kommand/badge.svg?branch=master)](https://coveralls.io/github/manami-project/kommand?branch=master) ![jdk21](https://img.shields.io/badge/jdk-21-informational)
# kommand

This library allows you to easily create kotlin-style wrappers for command line functions.
It encapsulates `java.lang.ProcessBuilder` and also handles the usage of `sudo` for a command.

## Example

Lets create an example for the `echo` cli command.

Create a configuration for the various parameters that you want to support.

```kotlin
data class EchoConfig(
    var commandExecutor: CommandExecutor = JavaProcessBuilder(),
    var suppressTrailingNewlineCharacters: Boolean = false,
)
```

Create a wrapper for the function

```kotlin
fun echo(message: String, config: EchoConfig.() -> Unit = { }): String {
    val currentConfig = EchoConfig().apply(config)

    require(message.isNotBlank()) { "Message must not be blank." }

    val cmdBuilder = mutableListOf("echo")

    if (currentConfig.suppressTrailingNewlineCharacters) {
        cmdBuilder.add("-n")
    }

    cmdBuilder.add(message)

    return currentConfig.commandExecutor.executeCmd(cmdBuilder)
}
```

Now you can call the function using defaults:

```kotlin
fun main() {
    val result = echo("hello world")
    println(result)
}
```

Or alternatively call it with parameters:

```kotlin
fun main() {
    val result = echo("hello world") {
        suppressTrailingNewlineCharacters = true
    }
    println(result)
}
```