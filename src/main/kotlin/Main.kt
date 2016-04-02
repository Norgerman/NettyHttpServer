@file:JvmName("Startup")

import com.norgerman.server.WebApplication

/**
 * Created by Norgerman on 4/2/2016.
 * Main.kt
 */

fun main(args: Array<String>) {
    WebApplication(8080).run();
}