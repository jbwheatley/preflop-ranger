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

package preflop.ranger.model

import javafx.beans.binding.Bindings
import javafx.scene.input.{MouseButton, MouseEvent}
import preflop.ranger.PreflopRanger
import preflop.ranger.PreflopRanger.borderInset
import preflop.ranger.edit.UndoRedo.Change
import preflop.ranger.edit.{EditRegistry, UndoRedo}
import preflop.ranger.model.Chart.{ChartSquare, df}
import preflop.ranger.popups.{ChartEditPopup, ChartSquareEditPopup}
import scalafx.beans.property.{BooleanProperty, ObjectProperty, StringProperty}
import scalafx.geometry.Pos.Center
import scalafx.scene.control.{ContextMenu, MenuItem, Tooltip}
import scalafx.scene.layout._
import scalafx.scene.text.Text

import java.math.RoundingMode
import java.text.DecimalFormat
import scala.annotation.tailrec
import scala.util.Random

case class Chart(
    name: StringProperty,
    actions: Array[Array[HandAction]],
    subset: Boolean,
    isPlaceholder: Boolean,
    noOfPlayers: Int
) {
  private var initName: String = name.value

  def updateName(newName: String): Unit = {
    val current = name.value
    if (newName != current) {
      val update = () => {
        if (newName != initName) EditRegistry.register(this) else EditRegistry.deregister(this)
        name.value = newName
      }
      val inverse = () => {
        if (current != initName) EditRegistry.register(this) else EditRegistry.deregister(this)
        name.value = current
      }
      UndoRedo.add(Change(update, inverse, "Rename Chart"))
      update()
    }
  }

  val squares: Array[Array[ChartSquare]] = actions.zipWithIndex
    .zip(Hands.hands)
    .map { case ((actions, i), hands) =>
      actions.zipWithIndex.zip(hands).map { case ((action, j), hand) =>
        new ChartSquare(this, hand, i, j) {
          override val handActionProperty: ObjectProperty[HandAction] = ObjectProperty(action)
          init = action
        }
      }
    }

  def draw(container: BorderPane): GridPane =
    new GridPane() { grid =>
      val availableHeight = container.height.subtract(
        (PreflopRanger.borderInset * 2) + PreflopRanger.chartMenuHeight + PreflopRanger.chartTitleHeight + PreflopRanger.randomiserHeight + (3 * PreflopRanger.boxSpacing)
      )
      val availableWidth = container.width.subtract((2 * borderInset) + (2 * PreflopRanger.boxSpacing))
      val gridDim        = Bindings.min(availableWidth, availableHeight).subtract(6)
      grid.minWidth.bind(gridDim)
      grid.maxWidth.bind(gridDim)
      hgap = 0.5
      vgap = 0.5
      snapToPixel = false
      alignment = Center
      squares.zipWithIndex.foreach { case (ss, idx) => addRow(idx, ss.map(_.draw(grid)).toList.map(_.delegate): _*) }
    }

  def save(): Unit = {
    initName = name.value
    squares.foreach(_.foreach(_.save()))
  }

  private def percentages: String = {
    val allCombos = HandAction.combineAll(for {
      x <- 0 to 12
      y <- 0 to 12
    } yield {
      val h = actions(x)(y)
      if (x > y) h * 12
      else if (x == y) h * 6
      else h * 4
    })
    val total: Double = if (!subset) 1326.0 else 1326.0 - (allCombos.n / 100)
    val r =
      if (allCombos.r == 0 && allCombos.j == 0 && allCombos.variableRaiseSizes.values.forall(_ == 0)) None
      else Some(s"R ${df.format((allCombos.r + allCombos.j + allCombos.variableRaiseSizes.values.sum) / total)}%")
    val c =
      if (allCombos.c == 0 && allCombos.l == 0) None else Some(s"C ${df.format((allCombos.c + allCombos.l) / total)}%")
    val f = if (allCombos.f == 0) None else Some(s"F ${df.format(allCombos.f / total)}%")
    List(r, c, f).flatten.mkString(", ")
  }

  val percentagesProperty: StringProperty = StringProperty(percentages)

  def recalcPercentages(): Unit = percentagesProperty.value = percentages

}

object Chart {
  lazy val df: DecimalFormat = {
    val d = new DecimalFormat("#.#")
    d.setRoundingMode(RoundingMode.UP)
    d
  }

  def apply(
      name: String,
      actions: Array[Array[HandAction]],
      subset: Boolean,
      numberOfPlayers: Int,
      isPlaceholder: Boolean = false
  ): Chart = Chart(
    StringProperty(name),
    actions,
    subset,
    isPlaceholder,
    numberOfPlayers
  )

  abstract case class ChartSquare(chart: Chart, hand: String, x: Int, y: Int) { self =>
    var init: HandAction = _
    def handActionProperty: ObjectProperty[HandAction]

    def draw(container: GridPane): StackPane = {
      def handActionView: StackPane = handActionProperty.get().draw(container)
      val p: StackPane = new StackPane { sp =>
        onMouseClicked = (e: MouseEvent) =>
          if (!chart.isPlaceholder && !PreflopRanger.popupOpen.value) {
            if (e.getButton == MouseButton.SECONDARY) {
              new ContextMenu(
                new MenuItem("Edit") {
                  onAction = _ => {
                    PreflopRanger.popupOpen.value = true
                    new ChartSquareEditPopup(chart, self).show()
                  }
                },
                new MenuItem("Edit All") {
                  onAction = _ => {
                    PreflopRanger.popupOpen.value = true
                    new ChartEditPopup(chart).show()
                  }
                }
              ) {
                self =>
                onMouseExited = _ => self.hide()
              }.show(sp, e.getScreenX, e.getScreenY)
            } else if (e.getButton == MouseButton.PRIMARY) {
              val randomAction = randomise()
              if (randomAction.nonEmpty)
                PreflopRanger.randomiserText.text = s"$hand - $randomAction"
            }
          }
        centerShape = true
        children = Array(
          handActionView,
          new Text(hand)
        )
      }

      def tooltipText(ha: HandAction): String = {
        val ratio: Double = if (!chart.subset) 100.0 else (100.0 - ha.n) / 100
        val r             = if (ha.r > 0) Some(s"Raise: ${df.format(ha.r.toDouble / ratio)}%") else None
        val vari = ha.variableRaiseSizes.map { case (s, i) =>
          if (i > 0) Some(s"Raise (${s}x): ${df.format(i.toDouble / ratio)}%") else None
        }.toList
        val j = if (ha.j > 0) Some(s"Jam: ${df.format(ha.j.toDouble / ratio)}%") else None
        val c = if (ha.c > 0) Some(s"Call: ${df.format(ha.c.toDouble / ratio)}%") else None
        val l = if (ha.l > 0) Some(s"Limp: ${df.format(ha.l.toDouble / ratio)}%") else None
        val f = if (ha.f > 0) Some(s"Fold: ${df.format(ha.f.toDouble / ratio)}%") else None
        (r :: vari ::: List(j, c, l, f)).flatten.mkString("\n")
      }

      var tooltipInstalled: Boolean = false
      val tooltip                   = new Tooltip()

      def installTooltip(ha: HandAction, disabled: Boolean): Unit = if (ha.n != 100 && !tooltipInstalled && !disabled) {
        tooltip.text = tooltipText(ha)
        Tooltip.install(p, tooltip)
        tooltipInstalled = true
      } else if ((ha.n == 100 && tooltipInstalled) || disabled) {
        Tooltip.uninstall(p, tooltip)
        tooltipInstalled = false
      }

      val tooltipDisabled: BooleanProperty = {
        val b = BooleanProperty(true)
        b.bind(PreflopRanger.popupOpen)
        b.onChange { (_, _, disabled) =>
          installTooltip(handActionProperty.get(), disabled)
        }
        b
      }

      installTooltip(handActionProperty.get(), tooltipDisabled.get())

      handActionProperty.onChange { (_, _, ha) =>
        p.children(0) = handActionView
        installTooltip(ha, tooltipDisabled.get())
      }
      p
    }

    private def randomise(): String = {
      @tailrec
      def loop(acc: Int, rem: List[(String, Int)], target: Int): String =
        rem match {
          case (name, pc) :: rest => if (acc + pc >= target) name else loop(acc + pc, rest, target)
          case Nil                => ""
        }
      val ha = handActionProperty.get()
      import ha._
      val all: List[(String, Int)] = List("Raise" -> r) ++ variableRaiseSizes
        .map { case (k, v) => s"Raise (${k}x)" -> v }
        .map { case (k, v) => k -> v } ++ List("Jam" -> j, "Call" -> c, "Limp" -> l, "Fold" -> f, "Fold" -> n)
      val countedInSum = if (!chart.subset) all else all.init // exclude no action
      val random       = Random.nextInt(countedInSum.map(_._2).sum) + 1
      loop(0, all, random)
    }

    def save(): Unit =
      init = handActionProperty.value

    def massUpdate(change: HandAction => HandAction): Unit = {
      val current   = handActionProperty.get()
      val newAction = change(current)
      if (newAction != current) {
        if (newAction != init) EditRegistry.register(self) else EditRegistry.deregister(self)
        handActionProperty.value = newAction
        chart.actions(x)(y) = newAction
      }
    }

    def update(newAction: HandAction): Unit = {
      val current = handActionProperty.get()
      if (newAction != current) {
        val update = () => {
          if (newAction != init) EditRegistry.register(self) else EditRegistry.deregister(self)
          handActionProperty.value = newAction
          chart.actions(x)(y) = newAction
          chart.recalcPercentages()
        }
        val inverse = () => {
          if (current != init) EditRegistry.register(self) else EditRegistry.deregister(self)
          handActionProperty.value = current
          chart.actions(x)(y) = current
          chart.recalcPercentages()
        }
        UndoRedo.add(Change(update, inverse, s"Update $hand"))
        update()
      }
    }
  }

  def default(container: BorderPane): GridPane =
    Chart(
      "",
      Array.fill(13)(
        Array.fill(13)(HandAction(0, 0, 0, 0, 0, 100, Map.empty))
      ),
      subset = false,
      numberOfPlayers = 0,
      isPlaceholder = true
    )
      .draw(container)
}
