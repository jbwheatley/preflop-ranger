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

import javafx.beans.InvalidationListener
import javafx.scene.input.MouseButton
import preflop.ranger.custom.Bindings.{falseBinding, trueBinding}
import preflop.ranger.custom.Tooltips.showTooltip
import preflop.ranger.custom._
import preflop.ranger.edit.UndoRedo
import preflop.ranger.edit.UndoRedo.Change
import preflop.ranger.model.{Chart, HandAction, Hands, SettingsMenu}
import preflop.ranger.popups.ChartEditPopup.PopupScene
import scalafx.beans.binding.BooleanBinding
import scalafx.geometry.Insets
import scalafx.geometry.Pos.Center
import scalafx.scene.{Node, Scene}
import scalafx.scene.control.{ScrollPane, TextField}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.GridPane.getRowIndex
import scalafx.scene.layout._
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.Text
import scalafx.stage.Stage

class ChartEditPopup(chart: Chart) extends Popup { stage =>
  title = s"Edit - ${chart.name.value}"
  resizable = true
  height = 600.0
  width = 450.0
  scene = new PopupScene(chart, stage)
  x = stage.getX
  y = stage.getY
}

object ChartEditPopup {
  private val variableRaiseSizes: Array[String] = SettingsMenu.variableRaiseSizes

  private class HandActionRow(
      hand: String,
      val i: Int,
      val j: Int,
      protected var initHand: HandAction,
      grid: GridPane
  ) {
    lazy val invalid: BooleanBinding = !fields.map(_.valid).foldLeft(trueBinding)(_ && _)

    lazy val updated: BooleanBinding = fields.map(_.updated).foldLeft(falseBinding)(_ || _)

    def validate(): Option[(Boolean, HandAction)] = {
      val _r = raiseField.validate
      val _c = callField.validate
      val _f = foldField.validate
      val _j = jamField.validate
      val _l = limpField.validate
      val _n = noActionField.validate
      val variables: List[Option[(String, Int)]] = other.toList.map { case (str, box) =>
        box.validate.map(str -> _)
      }

      if ((List(_r, _c, _f, _j, _l, _n) ::: variables).forall(_.isDefined)) {
        val r  = _r.get
        val c  = _c.get
        val f  = _f.get
        val j  = _j.get
        val n  = _n.get
        val l  = _l.get
        val vs = variables.map(_.get)
        if (r + c + f + j + n + l + vs.map(_._2).sum == 100) {
          val ha = HandAction(r, j, c, l, f, n, vs.toMap)
          Some(
            (ha != initHand) -> ha
          )
        } else None
      } else None
    }

    private val raiseField: HandActionTextField = new HandActionTextField(
      initHand.r,
      (self, tf) => {
        grid.children.remove(self)
        grid.add(tf, 1, (i * 13) + j + 1)
      }
    )
    private val callField: HandActionTextField = new HandActionTextField(
      initHand.c,
      (self, tf) => {
        grid.children.remove(self)
        grid.add(tf, 2, (i * 13) + j + 1)
      }
    )
    private val foldField: HandActionTextField = new HandActionTextField(
      initHand.f,
      (self, tf) => {
        grid.children.remove(self)
        grid.add(tf, 3, (i * 13) + j + 1)
      }
    )
    private val jamField: HandActionTextField = new HandActionTextField(
      initHand.j,
      (self, tf) => {
        grid.children.remove(self)
        grid.add(tf, 4, (i * 13) + j + 1)
      }
    )

    private val limpField: HandActionTextField = new HandActionTextField(
      initHand.l,
      (self, tf) => {
        grid.children.remove(self)
        grid.add(tf, 5, (i * 13) + j + 1)
      }
    )

    private val other: Array[(String, HandActionTextField)] = variableRaiseSizes.zipWithIndex
      .map { case (size, idx) =>
        size -> new HandActionTextField(
          initHand.variableRaiseSizes.getOrElse(size, 0),
          (self, tf) => {
            grid.children.remove(self)
            grid.add(tf, idx + 6, (i * 13) + j + 1)
          }
        )
      }

    private val noActionField: HandActionTextField = new HandActionTextField(
      initHand.n,
      (self, tf) => {
        grid.children.remove(self)
        grid.add(tf, variableRaiseSizes.length + 6, (i * 13) + j + 1)
      }
    )

    private val label: RectangleStack = new RectangleStack(
      54,
      24,
      new Text(hand) {
        style = "-fx-font: normal bold 11pt sans-serif"
      }
    )

    private val fields: Array[HandActionTextField] = Array(
      raiseField,
      callField,
      foldField,
      jamField,
      limpField
    ) ++ other.map(_._2) :+ noActionField

    def toNodes: List[Node] = label +: fields.map(_.field).toList

    def highlight(): Unit   = label.highlight()
    def unhighlight(): Unit = label.unhighlight()
    def reset(update: HandAction): Unit = {
      initHand = update
      fields.foreach(_.reset())
    }
  }

  class PopupScene(chart: Chart, popupStage: Stage) extends Scene {
    private val labels: List[String] =
      List("Raise", "Call", "Fold", "Jam", "Limp") ++ variableRaiseSizes.map(s => s"R (${s}x)") :+ "None"

    private val headers = new Rectangle() {
      height = 24
      width = 54
      fill = Color.White
    } :: labels.map { l =>
      new RectangleStack(
        labels.maxBy(_.length).length * 10,
        24,
        new Text(l) {
          style = "-fx-font: normal bold 11pt sans-serif"
        }
      )
    }

    private val grid = new GridPane(4, 4) {
      background = Background.fill(Color.White)
    }

    private val updates: Array[HandActionRow] = Hands.hands.zipWithIndex
      .flatMap { case (row, i) =>
        row.zipWithIndex.map { case (hand, j) => (hand, i, j) }
      }
      .map { case (hand, i, j) =>
        new HandActionRow(hand, i, j, chart.actions(i)(j), grid)
      }

    updates.zipWithIndex.foreach { case (u, idx) =>
      grid.addRow(rowIndex = idx + 1, children = u.toNodes.map(_.delegate): _*)
    }

    private lazy val anyInvalid: BooleanBinding = updates.map(_.invalid).reduce(_ || _)
    private lazy val hasUpdates: BooleanBinding = updates.map(_.updated).reduce(_ || _)

    grid.addRow(
      rowIndex = 0,
      children = headers.map(_.delegate): _*
    )
    private val scrollPane = new ScrollPane() {
      fitToWidth = true
      content = grid
    }

    private val headerLock: InvalidationListener = _ => {
      val ty = (grid.getHeight - scrollPane.getViewportBounds.getHeight) * scrollPane.getVvalue
      headers.foreach(_.setTranslateY(ty))
    }

    grid.heightProperty().addListener(headerLock)
    scrollPane.viewportBoundsProperty().addListener(headerLock)
    scrollPane.vvalueProperty().addListener(headerLock)

    def update(): Boolean = {
      val rows = updates.map { row =>
        row -> row.validate()
      }
      val (valid, invalid) = rows.partitionMap {
        case (r, Some(action)) => Left(r -> action)
        case (r, None)         => Right(r)
      }
      valid.foreach(_._1.unhighlight())
      if (invalid.nonEmpty) {
        scrollPane.vvalue = (getRowIndex(invalid.head.toNodes.head) - 1).toDouble / grid.getRowCount
        showTooltip(popupStage)("Values must add to 100")
        invalid.foreach(_.highlight())
        false
      } else {
        val updatedRows: Array[(HandActionRow, HandAction)] = valid.collect { case (row, (true, action)) =>
          row -> action
        }
        val currentValuesOfUpdatedRows = updatedRows.map { case (r, _) =>
          (r.i, r.j, chart.squares(r.i)(r.j).handActionProperty.value)
        }
        val change = () => {
          updatedRows.foreach { case (r, action) =>
            chart.squares(r.i)(r.j).massUpdate(_ => action)
          }
          chart.recalcPercentages()
        }
        val revert = () => {
          currentValuesOfUpdatedRows.foreach { case (i, j, ha) => chart.squares(i)(j).massUpdate(_ => ha) }
          chart.recalcPercentages()
        }
        UndoRedo.add(Change(change, revert, "Update Chart"))
        updatedRows.foreach { case (row, newAction) => row.reset(newAction) }
        change()
        true
      }
    }

    private val topRectangle: RectangleStack = new RectangleStack(
      0,
      30,
      chartName(chart.name.value)
    )

    private def nameEditIcon(h: HBox) =
      new RectangleStack(18, 18, new ImageView(new Image("/edit-icon.png"))) {
        override def map: Rectangle => Rectangle = r => {
          r.setStroke(Color.LightGray)
          r.setArcHeight(2)
          r.setArcWidth(2)
          r
        }
        onMouseClicked = o =>
          if (o.getButton == MouseButton.PRIMARY) {
            val tf = new TextField() {
              text = chart.name.value
            }
            h.spacing = 4
            h.children = List(
              tf,
              new LeftClickButton("Cancel") {
                override def onLeftClick(): Unit = topRectangle.replaceNode(chartName(chart.name.value))
              },
              new LeftClickButton("OK") {
                override def onLeftClick(): Unit = chart.updateName(tf.getText)
              }
            )
            tf.requestFocus()
            tf.end()
          }
      }

    private def chartName(name: String): HBox = new HBox() { h =>
      alignment = Center
      spacing = 10
      children = List(
        Rectangle(18, 18, Color.White),
        new Text(name) {
          style = "-fx-font: normal bold 11pt sans-serif"
        },
        nameEditIcon(h)
      )
    }

    root = new BorderPane() {
      background = Background.fill(Color.White)
      padding = Insets(20)
      top = topRectangle
      center = scrollPane
      bottom = new ButtonHBox(
        List(
          new LeftClickButton("Cancel") {
            override def onLeftClick(): Unit = popupStage.close()
          },
          new LeftClickButton("Apply") {
            disable.bind(anyInvalid || !hasUpdates)
            override def onLeftClick(): Unit = {
              update()
              ()
            }
          },
          new LeftClickButton("OK") {
            disable.bind(anyInvalid || !hasUpdates)
            override def onLeftClick(): Unit = {
              val valid = update()
              if (valid) popupStage.close()
            }
          }
        )
      )
    }
  }
}
