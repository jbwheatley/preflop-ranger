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
import preflop.ranger.custom.LeftClickButton
import preflop.ranger.edit.EditRegistry
import preflop.ranger.model._
import preflop.ranger.popups._
import scalafx.application.JFXApp3
import scalafx.beans.property.BooleanProperty
import scalafx.geometry.Pos.{Center, TopCenter}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{ContextMenu, MenuBar, MenuItem, RadioMenuItem}
import scalafx.scene.layout.Priority.Always
import scalafx.scene.layout._
import scalafx.scene.paint._
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.Text
import scalafx.stage.Stage

import java.io.File
import java.nio.file.{Files, Path, StandardCopyOption, StandardOpenOption}
import scala.util.Random

object PreflopRanger extends JFXApp3 {

  private val debug: Boolean = false

  val borderInset: Double        = 25.0
  val boxArc: Double             = 8.0
  private val boxSpacing: Double = 3.0

  val basePath: Path            = Path.of(System.getProperty("user.home") + File.separator + ".preflop-ranger")
  private val profilePath: Path = basePath.resolve("profiles.json")

  var allProfiles: Array[Profile] = _
  var selectedProfile: Data       = _

  private def load(): Unit =
    if (Files.exists(profilePath)) {
      val profiles = upickle.default.read[Array[Profile]](Files.readString(profilePath))
      allProfiles = profiles.sortBy(_.name)
      val name = profiles.find(_.selected).getOrElse(profiles.head).name
      loadProfile(name)
    } else {
      firstOpenLoad()
    }

  private def firstOpenLoad(): Unit = {
    Files.createDirectories(basePath.resolve("profiles"))
    val profiles = Array(Profile("empty", selected = false), Profile("sample", selected = true))
    Files.writeString(
      profilePath,
      upickle.default.write[Array[Profile]](profiles, indent = 2),
      StandardOpenOption.CREATE,
      StandardOpenOption.WRITE
    )
    Files.copy(
      this.getClass.getResource("/default.json").openStream(),
      basePath.resolve("profiles/empty.json"),
      StandardCopyOption.REPLACE_EXISTING
    )
    Files.copy(
      this.getClass.getResource("/sample.json").openStream(),
      basePath.resolve("profiles/sample.json"),
      StandardCopyOption.REPLACE_EXISTING
    )
    allProfiles = profiles
    loadProfile("sample")
  }

  def loadProfile(name: String): Unit =
    if (Files.exists(basePath.resolve(s"profiles/${name.replaceAll(" ", "_")}.json"))) {
      val data = upickle.default.read[FileData](
        Files.readString(basePath.resolve(s"profiles/${name.replaceAll(" ", "_")}.json"))
      )
      putDataInMemory(name, Data.fromFileData(name, data))
    }

  private def putDataInMemory(name: String, data: Data): Unit = {
    selectedProfile = data
    allProfiles.foreach(_.selected = false)
    allProfiles.find(_.name == name).get.selected = true
    SettingsMenu.showPercentagesInit = data.settings.showPercentages
    SettingsMenu.showPercentages.unbind()
    SettingsMenu.showPercentages.value = data.settings.showPercentages

    SettingsMenu.noOfPlayersInit = data.settings.defaultPlayers
    SettingsMenu.noOfPlayers.value = data.settings.defaultPlayers

    SettingsMenu.actionsInit = data.settings.actions
    SettingsMenu.actions.value = data.settings.actions
  }

  def saveProfiles(): Unit = {
    Files.writeString(
      profilePath,
      upickle.default.write[Array[Profile]](allProfiles, indent = 2),
      StandardOpenOption.CREATE,
      StandardOpenOption.WRITE,
      StandardOpenOption.TRUNCATE_EXISTING
    )
    ()
  }

  def saveData(): Unit = {
    Files.writeString(
      basePath.resolve(s"profiles/${selectedProfile.profile.replaceAll(" ", "_")}.json"),
      upickle.default.write[FileData](selectedProfile.toFileData, indent = 2),
      StandardOpenOption.CREATE,
      StandardOpenOption.WRITE,
      StandardOpenOption.TRUNCATE_EXISTING
    )
    selectedProfile.saveInMemoryModel()
  }

  val popupOpen: BooleanProperty = BooleanProperty(false)

  def resetStage(): Unit = stage.scene.value.rootProperty().setValue(defaultScene(stage, new Scene(stage.scene.value)))

  private def defaultScene(selfStage: Stage, selfScene: Scene) = new BorderPane {
    top = SettingsMenu.draw()
    background = Background.Empty
    center = new BorderPane {
      borderP =>
      prefWidth = selfStage.minWidth
      prefHeight = selfStage.minHeight - 27.5
      padding = Insets(borderInset)
      center = Chart.default(selfScene)
      top = chartMenuBox(borderP, selfScene)
      vgrow = Always
      hgrow = Always
    }
  }

  override def main(args: Array[String]): Unit = try super.main(args)
  catch {
    case e: Throwable =>
      Files.writeString(
        basePath.resolve(s"${System.currentTimeMillis() / 1000}.log"),
        e.toString + "\n  " + e.getStackTrace.map(_.toString).mkString("\n  "),
        StandardOpenOption.CREATE,
        StandardOpenOption.WRITE
      )
      throw e
  }

  override def start(): Unit = {
    load()
    stage = new JFXApp3.PrimaryStage { selfStage =>
      title = "Preflop Ranger"
      resizable = true
      scene = new Scene {
        selfScene =>
        minWidth = 505
        minHeight = 650
        stylesheets = List(this.getClass.getResource("/style.css").toExternalForm)
        fill = Color.rgb(38, 38, 38)
        root = defaultScene(selfStage, selfScene)
      }
      onCloseRequest = mainCloseRequest => {
        saveProfiles()
        if (EditRegistry.hasEdits.value) {
          new OnCloseUnsavedPopup(mainCloseRequest).showAndWait()
        }
      }
    }
    stage.maxHeightProperty().bind(stage.widthProperty().add(145 + 27.5))
    stage.minHeightProperty().bind(stage.widthProperty().add(145 + 27.5))
    stage.sizeToScene()

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

  private def menuCallback(border: BorderPane, _scene: Scene): Chart => Unit = { chart =>
    border.center = new VBox() {
      children = Array(
        chart.draw(_scene),
        chartTitleBox(chart, _scene),
        randomiserBox(_scene)
      )
      alignment = TopCenter
      spacing = boxSpacing
    }
  }

  private def chartMenuBox(borderP: BorderPane, _scene: Scene): StackPane = new StackPane() {
    val menu: MenuBar = selectedProfile.menu.draw(
      menuCallback(borderP, _scene)
    )
    children = List(
      new Rectangle() {
        this.width.bind(_scene.width.subtract(2.0 * borderInset))
        this.height.bind(menu.height)
        fill = Color.LightGreen
        arcWidth = boxArc
        arcHeight = boxArc
        hgrow = Always
      },
      menu
    )
    alignment = Center
    padding = Insets(bottom = boxSpacing, top = 0, left = 0, right = 0)
  }

  private def chartTitleBox(chart: Chart, _scene: Scene): StackPane = {
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
        new Rectangle() {
          this.width.bind(_scene.width.subtract(2 * borderInset))
          fill = Color.LightGreen
          height = 25.0
          arcHeight = boxArc
          arcWidth = boxArc
        },
        textItem
      )
    }
  }

  private def randomiserBox(_scene: Scene): StackPane = new StackPane {
    children = List(
      new Rectangle() {
        fill = Color.SkyBlue
        this.width.bind(_scene.width.subtract(2 * borderInset))
        height = 50.0
        arcHeight = boxArc
        arcWidth = boxArc
      }, {
        val rand = new Text("0") {
          style = "-fx-font: normal bold 20pt sans-serif"
        }
        new HBox() {
          children = List(
            new LeftClickButton("Randomise") {
              override def onLeftClick(): Unit = rand.text = (Random.nextInt(100) + 1).toString
              style = "-fx-font: normal bold 10pt sans-serif"
            },
            rand
          )
          spacing = 10.0
          alignment = Pos.Center
        }
      }
    )
  }
}
