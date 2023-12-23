import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.cec.brick.api.Brick
import org.cec.brick.app.App
import org.cec.brick.engine.BrickEngine
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun main() {
    startKoin {
        modules(module {
            singleOf(::BrickEngine) bind Brick::class
        })
    }
    application {
        Window(onCloseRequest = ::exitApplication, title = "cec-brick") {
            App()
        }
    }
}
