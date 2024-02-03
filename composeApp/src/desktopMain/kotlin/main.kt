import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.cec.brick.app.App
import org.cec.brick.config.Configuration
import org.cec.brick.engine.BrickEngine
import org.cec.brick.engine.api.Brick
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.io.path.Path

fun main() {
    startKoin {
        modules(module {
            single {
                val pth = System.getenv("BRICK_CONFIG") ?: "config.json"
                Configuration(Path(pth).toFile())
            }
            single {
                BrickEngine(
                    listOf(
                        "org.cec.brick.plugins.StatisticsPlugin",
                        "org.cec.brick.plugins.JournalBuffer",
                        "org.cec.triumvirate.TriumvirateShim"
                    )
                )
            } bind Brick::class
        })
    }
    application {
        Window(onCloseRequest = { quit() }, title = "cec-brick") {
            App()
        }
    }
}

fun ApplicationScope.quit() {
    GlobalContext.get().get<Configuration>().save()
    exitApplication()
}