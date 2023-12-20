import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.cec.brick.app.App

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "cec-brick") {
        App()
    }
}
