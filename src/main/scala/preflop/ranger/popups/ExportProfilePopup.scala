package preflop.ranger.popups

import preflop.ranger.PreflopRanger
import preflop.ranger.PreflopRanger.basePath
import preflop.ranger.custom.{ButtonHBox, LeftClickButton, Popup, Tooltips}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.ChoiceBox
import scalafx.scene.input.{Clipboard, ClipboardContent}
import scalafx.scene.layout.{Background, BorderPane, GridPane}
import scalafx.scene.paint.Color
import scalafx.scene.text.Text
import scalafx.stage.FileChooser

import java.nio.file.{Files, StandardCopyOption}
import java.time.LocalDateTime

class ExportProfilePopup extends Popup { self =>
  title = "Export Profile"

  private val dropdown: ChoiceBox[String] = new ChoiceBox[String]() {
    items = new ObservableBuffer[String]().addAll(PreflopRanger.allProfiles.map(_.name))
    value = items.value.get(0)
    prefWidth = PreflopRanger.allProfiles.map(_.name).maxBy(_.length).length.toDouble * 10 + 30
  }

  private val copyToClipboard: LeftClickButton = new LeftClickButton("Copy to Clipboard") {
    override def onLeftClick(): Unit = {
      val clipboard = Clipboard.systemClipboard
      val content   = new ClipboardContent()
      val read      = Files.readString(basePath.resolve(s"profiles/${dropdown.value.value.replaceAll(" ", "_")}.json"))
      content.putString(read)
      clipboard.setContent(content)
      Tooltips.showTooltip(self)("Copied!")
    }
  }

  private val writeToFile: LeftClickButton = new LeftClickButton("Write to File...") {
    override def onLeftClick(): Unit = {
      val file = new FileChooser() {
        initialFileName = dropdown.value.value
          .replaceAll(" ", "_") + "_" + LocalDateTime
          .now()
          .withSecond(0)
          .withNano(0)
          .toString
          .replaceAll("[:\\-]", "_") + ".json"
      }.showSaveDialog(self)
      if (file != null) {
        Files.copy(
          basePath.resolve(s"profiles/${dropdown.value.value.replaceAll(" ", "_")}.json"),
          file.toPath,
          StandardCopyOption.REPLACE_EXISTING
        )
        Tooltips.showTooltip(self)("Saved!")
      }
      ()
    }
  }

  private val grid = new GridPane(4, 4) {
    background = Background.fill(Color.White)
  }

  grid.addRow(0, new Text("Profile:"))
  grid.addRow(1, dropdown, copyToClipboard, writeToFile)

  scene = new Scene {
    root = new BorderPane() {
      background = Background.fill(Color.White)
      padding = Insets(20)
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
