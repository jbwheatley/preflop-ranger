package preflop.ranger

import scalafx.application.JFXApp3
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.layout.{Background, BorderPane, StackPane}
import scalafx.scene.paint._
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.Text

import java.nio.file.Path

object PreflopRanger extends JFXApp3 {
  val chartSquareSize   = 40.0
  val borderInset       = 25
  val widthWithoutInset = (chartSquareSize + 0.5) * 13
  val widthWithInset    = widthWithoutInset + (2 * borderInset)

  val allCharts = ChartReader.readAll(Path.of(System.getenv("PREFLOP_RANGER_PATH")))

  override def start(): Unit =
    stage = new JFXApp3.PrimaryStage {
      //    initStyle(StageStyle.Unified)
      title = "Preflop Ranger"

      scene = new Scene {
        fill = Color.rgb(38, 38, 38)
        maxWidth = widthWithInset
        minWidth = widthWithInset
        maxHeight = 667
        minHeight = 667
        root = new BorderPane {
          background = Background.Empty
          padding = Insets(borderInset)
          top = PreflopMenu.menu(
            allCharts,
            (name, hands) => {
              this.center = Chart.draw(hands, chartSquareSize)
              this.bottom = new StackPane() {
                children = List(
                  {
                    val r = Rectangle(widthWithoutInset, 25.0, Color.OrangeRed)
                    r.arcHeight = 5.0
                    r.arcWidth = 5.0
                    r
                  },
                  new Text(name) {
                    style = "-fx-font: normal bold 11pt sans-serif"
                  }
                )
              }
            }
          )
          center = Chart.default(chartSquareSize)
        }
      }
    }
}
