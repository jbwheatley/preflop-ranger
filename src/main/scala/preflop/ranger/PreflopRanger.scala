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

package preflop.ranger

import javafx.scene.control.ToggleGroup
import javafx.scene.input.{MouseButton, MouseEvent}
import javafx.stage.WindowEvent
import preflop.ranger.edit.RangerFiles.{load, saveProfileList, writeErrorLog}
import preflop.ranger.custom.LeftClickButton
import preflop.ranger.edit.EditRegistry
import preflop.ranger.model._
import preflop.ranger.popups._
import scalafx.application.{JFXApp3, Platform}
import scalafx.beans.property.BooleanProperty
import scalafx.geometry.Pos.{BottomCenter, Center, TopCenter}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{ContextMenu, MenuItem, RadioMenuItem}
import scalafx.scene.layout.Priority.Always
import scalafx.scene.layout._
import scalafx.scene.paint._
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.Text

import scala.util.Random

object PreflopRanger extends JFXApp3 {

  private val debug: Boolean = false

  private val minChartSquareWidth: Double = 40.0
  val borderInset: Double                 = 25.0
  val chartMenuHeight: Double             = 32.0
  val chartTitleHeight: Double            = 25.0
  val randomiserHeight: Double            = 50.0
  val boxArc: Double                      = 8.0
  val boxSpacing: Double                  = 3.0

  var allProfiles: Array[Profile] = _
  var selectedProfile: Data       = _

  val popupOpen: BooleanProperty = BooleanProperty(false)

  override def main(args: Array[String]): Unit = try super.main(args)
  catch {
    case e: Throwable =>
      writeErrorLog(e)
      throw e
  }

  override def start(): Unit = {
    load(startup = true)
    stage = new JFXApp3.PrimaryStage { selfStage =>
      title = "Preflop Ranger"
      minWidth = ((minChartSquareWidth + 0.5) * 13.0) + (borderInset * 2.0)
      scene = new Scene {
        stylesheets = List(this.getClass.getResource("/style.css").toExternalForm)
        fill = Color.rgb(38, 38, 38)
        root = makeScene()
      }
      onCloseRequest = mainCloseRequest => {
        saveProfileList()
        if (EditRegistry.hasEdits.value) {
          new OnCloseUnsavedPopup(mainCloseRequest).showAndWait()
        }
      }
    }

    Platform.runLater {
      stage.minHeight = stage.height.value
    }

    if (debug) {
      val chart = selectedProfile.menu.children.head.children.head.chart.get
      new ChartEditPopup(chart).show()
      new ChartSquareEditPopup(chart, chart.squares.head.head).show()
      new ImportProfilePopup(() => ()).show()
      new ManageProfilesPopup(() => ()).show()
      new CreateProfilePopup(() => ()).show()
      new OnCloseUnsavedPopup(new WindowEvent(stage, WindowEvent.ANY)).show()
      new SwitchProfileConfirmationPopup(new ToggleGroup(), new RadioMenuItem(""), "").show()
      new ManageActionsPopup().show()
      new ExportProfilePopup().show()
      new AboutPopup().show()
      new BuyMeACoffeePopup().show()
    }
  }

  def resetStage(): Unit = stage.scene.value.rootProperty().setValue(makeScene())

  private var stageScene: StackPane = _
  popupOpen.onChange { (_, _, open) =>
    if (open)
      stageScene.children.append(
        new Rectangle() {
          width.bind(stageScene.width)
          height.bind(stageScene.height)
          fill = Color.LightGray
          opacity = 0.4
        }
      )
    else stageScene.children.remove(1)
    ()
  }

  private def makeScene() = {
    val s = new StackPane() {
      background = Background.Empty
      children = new BorderPane() {
        top = SettingsMenu.draw
        background = Background.Empty
        padding = Insets.Empty
        center = new BorderPane() {
          borderP =>
          minWidth = ((minChartSquareWidth + 0.5) * 13.0) + (borderInset * 2.0)
          minHeight = minWidth.value + chartMenuHeight + chartTitleHeight + randomiserHeight + (3 * boxSpacing)
          padding = Insets(borderInset)
          top = chartMenuBar(borderP)
          center = Chart.default(borderP)
          bottom = new VBox() { v =>
            children = List(emptyChartTitleBox(borderP), emptyRandomiserBox(borderP))
            spacing = boxSpacing
            padding = Insets(top = boxSpacing, right = 0, bottom = 0, left = 0)
            alignment = TopCenter
            alignmentInParent = TopCenter
          }
          alignmentInParent = Center
        }
      }
    }
    stageScene = s
    s
  }

  private def menuCallback(borderP: BorderPane): Chart => Unit = { chart =>
    randomiserText.text = ""
    borderP.center = chart.draw(borderP)
    borderP.bottom = new VBox() {
      children = List(chartTitleBox(chart, borderP), randomiserBox(borderP))
      spacing = boxSpacing
      padding = Insets(top = boxSpacing, right = 0, bottom = 0, left = 0)
      alignment = TopCenter
    }
  }

  private def chartMenuBar(container: BorderPane): StackPane = new StackPane() {
    children = List(
      selectedProfile.menu.draw(
        menuCallback(container),
        container
      )
    )
    alignment = BottomCenter
    padding = Insets(bottom = boxSpacing, top = 0, left = 0, right = 0)
  }

  private def emptyChartTitleBox(container: BorderPane): Rectangle =
    new Rectangle() {
      width.bind(container.widthProperty().subtract(2 * borderInset))
      fill = Color.LightGreen
      height = chartTitleHeight
      arcHeight = boxArc
      arcWidth = boxArc
      hgrow = Always
    }

  private def chartTitleBox(chart: Chart, container: BorderPane): StackPane = {
    def text: String =
      if (SettingsMenu.showPercentages.get())
        s"${chart.name.value} - ${chart.percentagesProperty.get()}"
      else chart.name.value
    val textItem = new Text(text) {
      style = "-fx-font: normal bold 11pt sans-serif"
    }
    chart.name.onChange {
      textItem.text = text
    }
    SettingsMenu.showPercentages.onChange {
      textItem.text = text
    }
    chart.percentagesProperty.onChange {
      textItem.text = text
    }
    new StackPane { sp =>
      onMouseClicked = (e: MouseEvent) =>
        if (!chart.isPlaceholder && e.getButton == MouseButton.SECONDARY && !PreflopRanger.popupOpen.get()) {
          new ContextMenu(
            new MenuItem("Edit All") {
              onAction = _ => {
                PreflopRanger.popupOpen.value = true
                new ChartEditPopup(chart).show()
              }
            }
          ) { self =>
            onMouseExited = _ => self.hide()
          }.show(sp, e.getScreenX, e.getScreenY)
        }
      children = List(
        new Rectangle() { r =>
          width.bind(container.widthProperty().subtract(2 * borderInset))
          hgrow = Always
          fill = Color.LightGreen
          height = chartTitleHeight
          arcHeight = boxArc
          arcWidth = boxArc
        },
        textItem
      )
    }
  }

  private def emptyRandomiserBox(container: BorderPane): Rectangle =
    new Rectangle() {
      fill = Color.SkyBlue
      hgrow = Always
      width.bind(container.widthProperty().subtract(2 * borderInset))
      height = randomiserHeight
      arcHeight = boxArc
      arcWidth = boxArc
    }

  val randomiserText: Text = new Text("") {
    style = "-fx-font: normal bold 20pt sans-serif"
  }

  private def randomiserBox(container: BorderPane): StackPane = new StackPane {
    children = List(
      new Rectangle() {
        fill = Color.SkyBlue
        hgrow = Always
        width.bind(container.widthProperty().subtract(2 * borderInset))
        height = randomiserHeight
        arcHeight = boxArc
        arcWidth = boxArc
      },
      new HBox() {
        children = List(
          new LeftClickButton("Randomise") {
            override def onLeftClick(): Unit = randomiserText.text = (Random.nextInt(100) + 1).toString
            style = "-fx-font: normal bold 10pt sans-serif"
          },
          randomiserText
        )
        spacing = 10.0
        alignment = Pos.Center
      }
    )
  }
}
