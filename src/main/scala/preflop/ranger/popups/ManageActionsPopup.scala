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

import javafx.scene.control.{ColorPicker, TextFormatter}
import javafx.scene.input.MouseButton
import preflop.ranger.PreflopRanger
import preflop.ranger.custom.Tooltips.showTooltip
import preflop.ranger.custom.{ButtonHBox, LeftClickButton, Popup, RectangleStack}
import preflop.ranger.edit.UndoRedo.Change
import preflop.ranger.edit.{EditRegistry, UndoRedo}
import preflop.ranger.model.FileData.ActionData
import preflop.ranger.model.{Chart, HandAction, SettingsMenu}
import scalafx.geometry.Insets
import scalafx.geometry.Pos.Center
import scalafx.scene.Scene
import scalafx.scene.control.{Spinner, Tooltip}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{Background, BorderPane, GridPane}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.Text

import java.math.RoundingMode
import java.text.DecimalFormat
import scala.collection.mutable

class ManageActionsPopup extends Popup { popup =>
  alwaysOnTop = true
  title = "Manage Actions"

  private lazy val df: DecimalFormat = {
    val d = new DecimalFormat("#.##")
    d.setRoundingMode(RoundingMode.UP)
    d
  }

  private var actionData: Map[String, ActionData] = SettingsMenu.actions.value

  private val standardPickers = Map(
    "r" -> new ColorPicker(actionData("r").colour),
    "c" -> new ColorPicker(actionData("c").colour),
    "f" -> new ColorPicker(actionData("f").colour),
    "l" -> new ColorPicker(actionData("l").colour),
    "j" -> new ColorPicker(actionData("j").colour),
    "n" -> new ColorPicker(actionData("n").colour)
  )

  private val variablePickers: mutable.Map[String, (Spinner[Double], ColorPicker)] = mutable.Map.empty

  // the spinner and delete button don't render properly if this stuff is done when the class is created,
  // it has to be when the scene is constructed.
  private def populateGrid(grid: GridPane): Unit = {
    def deleteButton(
        init: String,
        text: Text,
        spinner: Spinner[Double],
        picker: ColorPicker,
        idx: Int
    ): RectangleStack =
      new RectangleStack(
        deleteWidth,
        deleteWidth,
        new ImageView(new Image("/delete-icon.png")) {
          fitHeight = deleteWidth.toDouble
          fitWidth = deleteWidth.toDouble
        }
      ) { self =>
        onMouseClicked = o =>
          if (o.getButton == MouseButton.PRIMARY) {
            if (actionData.contains(init)) {
              val newText = new RectangleStack(
                0,
                spinner.height.value.toInt,
                new Text(s"Raise ${init}x (Deleted)")
              ) {
                alignmentInParent = Center
              }
              variablePickers.remove(init)
              grid.children.removeAll(self, spinner, picker, text)
              grid.add(
                newText,
                0,
                idx,
                3,
                1
              )
              grid.add(
                new RectangleStack(
                  deleteWidth,
                  deleteWidth,
                  new ImageView(new Image("/undo-icon.png")) {
                    fitHeight = deleteWidth.toDouble
                    fitWidth = deleteWidth.toDouble
                  }
                ) {
                  self =>
                  onMouseClicked = o =>
                    if (o.getButton == MouseButton.PRIMARY) {
                      variablePickers.addOne(init -> (spinner -> picker))
                      grid.children.remove(newText)
                      grid.children.remove(self)
                      grid.addRow(idx, text, spinner, picker, deleteButton(init, text, spinner, picker, idx))
                      popup.sizeToScene()
                    }
                },
                3,
                idx
              )

            } else {
              variablePickers.remove(init)
              val idx = GridPane.getRowIndex(self)
              grid.children.removeAll(self, spinner, picker, text)
              grid.children.foreach { n =>
                val nodeIdx = javafx.scene.layout.GridPane.getRowIndex(n).toInt
                if (nodeIdx > idx) {
                  javafx.scene.layout.GridPane.setRowIndex(n, nodeIdx - 1)
                }
              }
            }
            popup.sizeToScene()
          }
      }

    grid.addRow(0, new Text("Raise:"), fillRectLeft, standardPickers("r"), fillRectRight)
    grid.addRow(1, new Text("Call:"), fillRectLeft, standardPickers("c"), fillRectRight)
    grid.addRow(2, new Text("Fold:"), fillRectLeft, standardPickers("f"), fillRectRight)
    grid.addRow(3, new Text("Limp:"), fillRectLeft, standardPickers("l"), fillRectRight)
    grid.addRow(4, new Text("Jam:"), fillRectLeft, standardPickers("j"), fillRectRight)
    grid.addRow(5, new Text("None:"), fillRectLeft, standardPickers("n"), fillRectRight)

    actionData.view.filterKeys(_.toDoubleOption.isDefined).toList.sortBy(_._1).zipWithIndex.foreach {
      case ((d, c), idx) =>
        val spinner = mkSpinner(d.toDouble)
        val picker  = new ColorPicker(c.colour)
        val text    = new Text(s"Raise (${d}x):")
        variablePickers.addOne(d -> (mkSpinner(d.toDouble) -> new ColorPicker(c.colour)))
        grid.addRow(idx + 6, text, spinner, picker, deleteButton(d, text, spinner, picker, idx + 6))
    }

    val addRowButton = new LeftClickButton("+") {
      alignmentInParent = Center
      tooltip = new Tooltip("Add a new variable sized raise.")
      override def onLeftClick(): Unit = {
        val init: Double =
          variablePickers.values.maxByOption(_._1.value.value).map(_._1.value.value + 0.1).getOrElse(2.0)
        val picker                   = new ColorPicker()
        val spinner: Spinner[Double] = mkSpinner(init)
        val text                     = new Text("Raise (new):")
        grid.add(text, 0, grid.getRowCount - 1)
        grid.add(spinner, 1, grid.getRowCount - 1)
        grid.add(picker, 2, grid.getRowCount - 1)
        grid.add(deleteButton(df.format(init), text, spinner, picker, grid.getRowCount - 1), 3, grid.getRowCount - 1)
        GridPane.setRowIndex(this, grid.getRowCount)
        popup.sizeToScene()
        variablePickers.addOne(df.format(init) -> (spinner -> picker))
      }
    }

    grid.add(addRowButton, 0, actionData.size, 3, 1)
  }

  private val spinnerWidth: Double = 65.0
  private val deleteWidth: Int     = 18
  private def fillRectLeft         = Rectangle(spinnerWidth, 0, Color.White)
  private def fillRectRight        = Rectangle(deleteWidth.toDouble, 0, Color.White)

  private def mkSpinner(d: Double): Spinner[Double] = new Spinner[Double](
    min = 1,
    max = Double.MaxValue,
    initialValue = d,
    amountToStepBy = 0.1
  ) { self =>
    tooltip = new Tooltip("Choose a variable raise size.")
    prefWidth = spinnerWidth
    editable = true
    editor.value.setTextFormatter(
      new TextFormatter[Double]((change: TextFormatter.Change) =>
        if (
          change.getControlNewText
            .matches("""[1-9]\d*(\.\d?5?)?""")
        ) {
          change
        } else null
      )
    )
  }

  private def validate: Boolean = {
    val values = variablePickers.values.map(_._1.value.value)
    values.size == values.toSet.size
  }

  private def updateHandAction(remapping: Map[String, String])(ha: HandAction): HandAction = {
    val vr = ha.variableRaiseSizes
    // its been deleted if it doesn't exist in the rename set
    val renamed: Map[String, Int] = remapping.view
      .filterKeys(vr.keySet.contains)
      .map { case (k, v) => v -> vr.getOrElse(k, 0) }
      .toMap
    val newSizes: collection.Set[String] = variablePickers.keySet.diff(vr.keySet)
    val renameSet: Set[String]           = variablePickers.values.map(p => df.format(p._1.getValue)).toSet
    val deletedSum: Int                  = vr.view.filterKeys(!renameSet.contains(_)).values.sum
    // TODO option to either make no action or regular raise??
    val newNoAction: Int = ha.n + deletedSum
    ha.copy(n = newNoAction, variableRaiseSizes = renamed ++ newSizes.map(_ -> 0))
  }

  private def update(): Unit = if (validate) {
    val current = actionData
    val remappedVariableActions: Map[String, String] =
      variablePickers.toMap.view.mapValues(p => df.format(p._1.getValue)).toMap
    val updatedActionData: Map[String, ActionData] = {
      standardPickers.view.mapValues { picker =>
        new Color(picker.getValue)
      }.toMap ++ variablePickers.map { case (_, (spinner, picker)) =>
        df.format(spinner.getValue) -> new Color(picker.getValue)
      }
    }.view.mapValues(ActionData(_)).toMap
    if (updatedActionData != current) {
      val copyOfCurrentHandActions: Map[Chart, Array[Array[HandAction]]] =
        PreflopRanger.selectedProfile.charts.map(c => c -> c.actions.map(_.map(identity))).toMap
      val change = () => {
        if (updatedActionData != SettingsMenu.actionsInit) EditRegistry.register("actions")
        else EditRegistry.deregister("actions")
        SettingsMenu.actions.update(updatedActionData)
        PreflopRanger.selectedProfile.charts.foreach(
          _.squares.foreach(_.foreach(_.massUpdate(updateHandAction(remappedVariableActions))))
        )
        actionData = updatedActionData
      }
      val revert = () => {
        if (current != SettingsMenu.actionsInit) EditRegistry.register("actions")
        else EditRegistry.deregister("actions")
        SettingsMenu.actions.update(current)
        PreflopRanger.selectedProfile.charts.foreach { c =>
          c.squares.foreach(_.foreach { sq =>
            val oldAction = copyOfCurrentHandActions(c)(sq.i)(sq.j)
            sq.massUpdate(_ => oldAction)
          })
        }
        actionData = current
      }
      UndoRedo.add(Change(change, revert, "Update Actions"))
      change()
    }
  } else {
    showTooltip(popup)("Raise sizes must all be distinct.")
  }

  scene = new Scene {
    root = new BorderPane() {
      background = Background.fill(Color.White)
      padding = Insets(20)
      center = {
        val grid: GridPane = new GridPane(4, 4)
        populateGrid(grid)
        grid
      }
      bottom = new ButtonHBox(
        List(
          new LeftClickButton("Cancel") {
            override def onLeftClick(): Unit = popup.close()
          },
          new LeftClickButton("Apply") {
            override def onLeftClick(): Unit = update()
          },
          new LeftClickButton("OK") {
            override def onLeftClick(): Unit = {
              update()
              popup.close()
            }
          }
        )
      )
    }
  }
}
