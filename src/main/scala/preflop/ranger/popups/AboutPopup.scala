package preflop.ranger.popups

import preflop.ranger.PreflopRanger
import preflop.ranger.custom.{ButtonHBox, LeftClickButton, Popup}
import scalafx.geometry.Insets
import scalafx.geometry.Pos.Center
import scalafx.scene.Scene
import scalafx.scene.control.Hyperlink
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{Background, Border, BorderPane, GridPane}
import scalafx.scene.paint.Color
import scalafx.scene.text.{Text, TextFlow}

class AboutPopup extends Popup {
  title = "About Preflop Ranger"

  val grid = new GridPane(4, 4) {
    background = Background.fill(Color.White)
  }

  grid.add(
    new TextFlow(
      new Text("Preflop Ranger") { style = "-fx-font-weight: bold" },
      new Text("  is free open-source software\ndeveloped by jbwheatley.")
    ),
    0,
    0,
    2,
    1
  )
  grid.addRow(
    1,
    new Text("Version:"),
    new Text(new String(PreflopRanger.getClass.getResource("/version").openStream().readAllBytes()).trim)
  )
  grid.addRow(
    2,
    new Text("License:"),
    new Hyperlink("GNU GPL-3.0") {
      border = Border.Empty
      onAction = _ => PreflopRanger.hostServices.showDocument("https://www.gnu.org/licenses/gpl-3.0")
    }
  )
  grid.addRow(
    3,
    new Text("Java Runtime:"),
    new Text(System.getProperty("java.vendor") + " " + System.getProperty("java.version"))
  )
  grid.addRow(
    4,
    new Text("Source Code:"),
    new Hyperlink("github/preflop-ranger") {
      border = Border.Empty
      onAction = _ => PreflopRanger.hostServices.showDocument("https://github.com/jbwheatley/preflop-ranger")
    }
  )
  grid.addRow(
    5,
    new Text("Icons:"),
    new Hyperlink("icons8") {
      border = Border.Empty
      onAction = _ => PreflopRanger.hostServices.showDocument("https://icons8.com")
    }
  )
  scene = new Scene {
    root = new BorderPane() {
      background = Background.fill(Color.White)
      padding = Insets(20)
      top = new ImageView(
        new Image(
          "/ranger.png",
          requestedWidth = 50,
          requestedHeight = 50,
          preserveRatio = true,
          smooth = true
        )
      ) {
        alignmentInParent = Center
      }
      center = grid
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
