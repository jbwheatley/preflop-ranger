package preflop.ranger.popups

import preflop.ranger.PreflopRanger
import preflop.ranger.custom.{ButtonHBox, LeftClickButton, Popup}
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.Hyperlink
import scalafx.scene.layout.{Background, Border, BorderPane}
import scalafx.scene.paint.Color
import scalafx.scene.text.Text
import scalafx.scene.text.TextAlignment.Center

class BuyMeACoffeePopup extends Popup {
  title = "Buy Me A Coffee"

  scene = new Scene {
    root = new BorderPane() {
      background = Background.fill(Color.White)
      padding = Insets(20)
      top = new Text(
        "I don't and never will charge for Preflop Ranger.\n" +
          "However, if you would like to say thanks you can get me a long black."
      ) {
        textAlignment = Center
      }
      center = new Hyperlink("buymeacoffee.com/jbwheatley") {
        border = Border.Empty
        onAction = _ => PreflopRanger.hostServices.showDocument("https://buymeacoffee.com/jbwheatley")
      }
      bottom = new ButtonHBox(
        List(
          new LeftClickButton("Close") {
            override def onLeftClick(): Unit = close()
          }
        )
      )
    }
  }
}
