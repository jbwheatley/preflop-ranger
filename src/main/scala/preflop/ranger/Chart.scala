package preflop.ranger

import scalafx.geometry.Insets
import scalafx.scene.layout.{HBox, StackPane, VBox}
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.Text

object Chart {

  def default(squareSize: Double): VBox = {
    val squares = Array
      .fill(13, 13)(())
      .zip(Hands.hands)
      .map { case (actions, hands) =>
        actions.zip(hands).map { case (_, hand) =>
          new StackPane() {
            children = List(Rectangle(squareSize, squareSize, HandAction.N_100.color), new Text(hand))
          }
        }
      }

    new VBox(
      children = squares.map { recs =>
        new HBox(children = recs: _*) { spacing = 0.5 }
      }: _*
    ) {
      padding = Insets(2)
      spacing = 0.5
    }
  }

  def draw(values: Array[Array[HandAction]], squareSize: Double): VBox = {
    val squares: Array[Array[StackPane]] = values
      .zip(Hands.hands)
      .map { case (actions, hands) =>
        actions.zip(hands).map {
          case (action: _100, hand) =>
            new StackPane() {
              children = List(Rectangle(squareSize, squareSize, action.color), new Text(hand))
            }
          case (action: Mixed, hand) =>
            new StackPane() {
              children = List(
                new HBox(
                  children = List(
                    Rectangle(squareSize * action.ratio1, squareSize, action.color1),
                    Rectangle(squareSize * action.ratio2, squareSize, action.color2)
                  ): _*
                ) {
                  spacing = 0
                },
                new Text(hand)
              )
            }

        }
      }

    new VBox(
      children = squares.map { recs =>
        new HBox(children = recs: _*) { spacing = 0.5 }
      }: _*
    ) {
      padding = Insets(2)
      spacing = 0.5
    }
  }

}
