package dev.koenv.roadassist.app

import dev.koenv.roadassist.core.sayHello

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return sayHello(platform.name)
    }
}
