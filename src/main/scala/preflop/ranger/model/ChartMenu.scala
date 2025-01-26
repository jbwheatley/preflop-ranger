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

import javafx.event.ActionEvent
import javafx.geometry.Pos
import javafx.scene.control
import preflop.ranger.PreflopRanger
import preflop.ranger.model.ChartMenu.{ChartMenuNode0, Node}
import scalafx.application.Platform
import scalafx.geometry.Insets
import scalafx.scene.control.{Menu, MenuBar, MenuItem, Skin}
import scalafx.scene.layout.{Background, BackgroundFill, BorderPane, CornerRadii}
import scalafx.scene.paint.Color

import scala.collection.mutable.ArrayBuffer

case class ChartMenu(children: ArrayBuffer[ChartMenuNode0]) extends Node {
  val depth                   = -1
  val chartOpt: Option[Chart] = None
  val label                   = "Chart Menu"
  def draw(callback: Chart => Unit, container: BorderPane): MenuBar = new MenuBar() {
    style = "-fx-font: normal bold 11pt sans-serif"
    background = new Background(
      Array(
        new BackgroundFill(
          fill = Color.LightGreen,
          radii = new CornerRadii(PreflopRanger.boxArc / 2),
          insets = Insets.Empty
        )
      )
    )

    Platform.runLater {
      skin = {
        val s: javafx.scene.control.skin.MenuBarSkin = skin.get() match {
          case s: javafx.scene.control.skin.MenuBarSkin =>
            s.setContainerAlignment(Pos.CENTER)
            s
          case _ => ???
        }
        new Skin[javafx.scene.control.MenuBar] {
          override def delegate: control.Skin[javafx.scene.control.MenuBar] = s
        }
      }
    }
    prefWidth.bind(container.widthProperty())
    menus = children.map(_.draw(callback))
  }

  children.foreach(_.menuParent = Some(this))
}

object ChartMenu {
  sealed trait Node {
    def label: String
    def chartOpt: Option[Chart]
    def isLeaf: Boolean = chartOpt.isDefined
    def depth: Int
    var menuParent: Option[ChartMenu.Node] = None
  }

  case class ChartMenuNode0(label: String, children: ArrayBuffer[ChartMenuNode1]) extends Node {
    val chartOpt: Option[Chart] = None
    val depth                   = 0
    def draw(callback: Chart => Unit): Menu = new Menu(label = label) {
      items = children.map(_.draw(callback, this))
      visible = !(label == "COLD 4-BET" && SettingsMenu.noOfPlayers.value == 2)
      SettingsMenu.noOfPlayers.onChange {
        visible = !(label == "COLD 4-BET" && SettingsMenu.noOfPlayers.value == 2)
      }
    }
    children.foreach(_.menuParent = Some(this))
  }

  case class ChartMenuNode1(label: String, chart: Option[Chart], children: ArrayBuffer[ChartMenuLeaf]) extends Node {
    val depth                             = 1
    val chartOpt: Option[Chart]           = chart
    private var dynamicMenuItem: MenuItem = _
    private def makeDynamicMenuItem(callback: Chart => Unit): MenuItem = {
      val visibleChildren = children.filter(_.chart.noOfPlayers <= SettingsMenu.noOfPlayers.value)
      if (visibleChildren.isEmpty) {
        new MenuItem() {
          visible = false
        }
      } else if (visibleChildren.length == 1) {
        new MenuItem(text = label) {
          onAction = (_: ActionEvent) => callback(visibleChildren.head.chart)
        }
      } else {
        new Menu(label = label) {
          items = children.map(_.draw(callback))
        }
      }
    }
    def draw(callback: Chart => Unit, parentMenu: Menu): MenuItem = chart match {
      case Some(chart) =>
        new MenuItem(text = label) {
          onAction = (_: ActionEvent) => callback(chart)
          visible = chart.noOfPlayers <= SettingsMenu.noOfPlayers.value
          SettingsMenu.noOfPlayers.onChange {
            visible = chart.noOfPlayers <= SettingsMenu.noOfPlayers.value
          }
        }
      case None =>
        val item = makeDynamicMenuItem(callback)
        dynamicMenuItem = item
        SettingsMenu.noOfPlayers.onChange {
          val newItem = makeDynamicMenuItem(callback)
          val idx     = parentMenu.getItems.indexOf(dynamicMenuItem)
          parentMenu.getItems.set(idx, newItem)
          dynamicMenuItem = newItem
        }
        item
    }
    children.foreach(_.menuParent = Some(this))
  }

  case class ChartMenuLeaf(label: String, chart: Chart) extends Node {
    val depth                   = 2
    val chartOpt: Option[Chart] = Some(chart)
    def draw(callback: Chart => Unit): MenuItem = new MenuItem(text = label) {
      onAction = (_: ActionEvent) => callback(chart)
      visible = chart.noOfPlayers <= SettingsMenu.noOfPlayers.value
      SettingsMenu.noOfPlayers.onChange {
        visible = chart.noOfPlayers <= SettingsMenu.noOfPlayers.value
      }
    }
  }
}
