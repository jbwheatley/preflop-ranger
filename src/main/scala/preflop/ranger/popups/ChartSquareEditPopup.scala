/*
 * Copyright (C) 2025  io.github.jbwheatley
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package preflop.ranger.popups

import javafx.scene.input.{KeyCode, KeyEvent}
import preflop.ranger.custom.Bindings.trueBinding
import preflop.ranger.custom.Tooltips.showTooltip
import preflop.ranger.custom._
import preflop.ranger.model.Chart.ChartSquare
import preflop.ranger.model.{Chart, HandAction, SettingsMenu}
import preflop.ranger.popups.ChartSquareEditPopup.PopupScene
import scalafx.beans.binding.BooleanBinding
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.layout._
import scalafx.scene.paint.Color
import scalafx.scene.text.Text
import scalafx.stage.Stage

class ChartSquareEditPopup(chart: Chart, square: ChartSquare) extends Popup { stage =>
  title = s"Edit - ${chart.name.value} - ${square.hand}"
  scene = new PopupScene(chart, square, stage)
}

object ChartSquareEditPopup {
  class PopupScene(chart: Chart, square: ChartSquare, popupStage: Stage) extends Scene {
    private lazy val next: ChartSquare =
      if (square.j < 12) chart.squares(square.i)(square.j + 1)
      else if (square.i < 12) chart.squares(square.i + 1)(0)
      else chart.squares(0)(0)
    private lazy val previous: ChartSquare =
      if (square.j > 0) chart.squares(square.i)(square.j - 1)
      else if (square.i > 0) chart.squares(square.i - 1)(12)
      else chart.squares(12)(12)

    val grid = new GridPane(4, 4)

    private def mkField(action: HandAction => Int, rowIdx: Int): HandActionTextField = new HandActionTextField(
      action(square.handActionProperty.get()),
      (self, replacement) => {
        grid.children.remove(self)
        grid.add(replacement, 1, rowIdx)
      }
    ) {

      onKeyPressed = (e: javafx.scene.input.KeyEvent) =>
        if (e.getEventType == KeyEvent.KEY_PRESSED && e.isShortcutDown)
          if (e.getCode == KeyCode.RIGHT) {
            popupStage.scene = new PopupScene(chart, next, popupStage)
          } else if (e.getCode == KeyCode.LEFT) {
            popupStage.scene = new PopupScene(chart, previous, popupStage)
          }
    }

    private val raiseBox = mkField(_.r, 0)
    private val callBox  = mkField(_.c, 1)
    private val foldBox  = mkField(_.f, 2)
    private val jamBox   = mkField(_.j, 3)
    private val limpBox  = mkField(_.l, 4)

    grid.addRow(0, new Text("Raise:"), raiseBox)
    grid.addRow(1, new Text("Call:"), callBox)
    grid.addRow(2, new Text("Fold:"), foldBox)
    grid.addRow(3, new Text("Jam:"), jamBox)
    grid.addRow(4, new Text("Limp:"), limpBox)

    private val variableRaises: Array[(String, HandActionTextField)] =
      SettingsMenu.variableRaiseSizes.zipWithIndex.map { case (s, idx) =>
        val field = mkField(_.variableRaiseSizes.getOrElse(s, 0), idx + 5)
        val text  = new Text(s"Raise (${s}x):")
        grid.addRow(idx + 5, text, field)
        s -> field
      }

    private val noActionBox = mkField(_.n, variableRaises.length + 5)
    grid.addRow(variableRaises.length + 5, new Text("None:"), noActionBox)

    private val allBoxes: List[HandActionTextField] =
      List(raiseBox, callBox, foldBox, jamBox, limpBox) ++ List(noActionBox)

    private val invalid: BooleanBinding = !allBoxes.map(_.valid).foldLeft(trueBinding)(_ && _)

    private val backButton = new LeftClickButton("←") {
      override def onLeftClick(): Unit = {
        update
        if (update) popupStage.scene = new PopupScene(chart, previous, popupStage)
      }
      disable.bind(invalid)
    }

    private val forwardButton = new LeftClickButton("→") {
      override def onLeftClick(): Unit = {
        update
        if (update) popupStage.scene = new PopupScene(chart, next, popupStage)
      }
      disable.bind(invalid)
    }

    private val applyButton = new LeftClickButton("Apply") {
      override def onLeftClick(): Unit = {
        update
        ()
      }
      disable.bind(invalid)
    }

    private val okButton = new LeftClickButton("OK") {
      override def onLeftClick(): Unit = {
        update
        if (update) popupStage.close()
      }
      disable.bind(invalid)
    }

    def update: Boolean = {
      val _r = raiseBox.validate
      val _c = callBox.validate
      val _f = foldBox.validate
      val _j = jamBox.validate
      val _n = noActionBox.validate
      val _l = limpBox.validate
      val variables = variableRaises.map { case (str, box) =>
        box.validate.map(str -> _)
      }

      if ((List(_r, _c, _f, _j, _n, _l) ++ variables).forall(_.isDefined)) {
        val r  = _r.get
        val c  = _c.get
        val f  = _f.get
        val j  = _j.get
        val n  = _n.get
        val l  = _l.get
        val vs = variables.map(_.get)
        if (r + c + f + j + n + l + vs.map(_._2).sum == 100) {
          square.update(
            HandAction(r, j, c, l, f, n, vs.toMap)
          )
          true
        } else {
          showTooltip(popupStage)("Values must add to 100")
          false
        }
      } else false
    }

    root = new BorderPane() {
      background = Background.fill(Color.White)
      padding = Insets(20)
      top = new VBox() {
        children = List(
          new RectangleStack(
            90,
            25,
            new Text(chart.name.value) {
              style = "-fx-font: normal bold 11pt sans-serif"
            }
          ),
          new RectangleStack(
            90,
            25,
            new Text(square.hand) {
              style = "-fx-font: normal bold 11pt sans-serif"
            }
          )
        )
      }
      center = new HBox() {
        spacing = 25
        children = List(
          new StackPane {
            children = backButton
          },
          grid,
          new StackPane {
            children = forwardButton
          }
        )
      }
      bottom = new ButtonHBox(
        List(
          new LeftClickButton("Cancel") {
            override def onLeftClick(): Unit = popupStage.close()
          },
          applyButton,
          okButton
        )
      )
    }
  }
}
