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

import javafx.beans.value.ObservableValue
import javafx.scene.control
import preflop.ranger.PreflopRanger.{allProfiles, resetStage}
import preflop.ranger.edit.RangerFiles.{loadProfile, saveSelectedProfile}
import preflop.ranger.custom.{DisappearingMenuItem, SelectMenuItem}
import preflop.ranger.edit.{EditRegistry, UndoRedo}
import preflop.ranger.model.FileData.ActionData
import preflop.ranger.popups._
import scalafx.beans.property._
import scalafx.scene.control._
import scalafx.scene.image.{Image, ImageView}

import java.lang

object SettingsMenu {

  var actionsInit: Map[String, ActionData] = _

  var showPercentages: BooleanProperty                 = _
  var noOfPlayers: IntegerProperty                     = _
  var actions: ObjectProperty[Map[String, ActionData]] = _

  def variableRaiseSizes: Array[String] = actions.getValue.view
    .filterKeys(_.toDoubleOption.isDefined)
    .keys
    .toArray
    .sorted

  def save(): Unit = {
    showPercentagesMenuItem.save()
    noOfPlayersMenu.save()
    actionsInit = actions.value
  }

  def switchProfile(selected: String): Unit = {
    UndoRedo.reset()
    loadProfile(selected, startup = false)
    resetStage()
  }

  private def selectProfile(
      toggles: javafx.scene.control.ToggleGroup,
      old: javafx.scene.control.Toggle,
      selected: String
  ): Unit =
    if (EditRegistry.hasEdits.value) {
      new SwitchProfileConfirmationPopup(toggles, old, selected).showAndWait()
    } else {
      switchProfile(selected)
    }

  private def profilesMenu(parent: Menu): Menu = new Menu("Profiles") { self =>
    private val radioItems: List[RadioMenuItem] = allProfiles.map { p =>
      new RadioMenuItem(p.name)
    }.toList
    private val toggles: ToggleGroup = new ToggleGroup() {
      this.toggles = radioItems
    }
    toggles.selectToggle(radioItems(allProfiles.indexWhere(_.selected, 0)))
    toggles.selectedToggle.onChange((_, old, selected) =>
      selectProfile(toggles, old, selected.asInstanceOf[javafx.scene.control.RadioMenuItem].getText)
    )

    items = radioItems
      .appendedAll(
        List(
          new SeparatorMenuItem(),
          new MenuItem("New profile...") {
            onAction = _ =>
              new CreateProfilePopup(() => parent.items(parent.items.indexOf(self.delegate)) = profilesMenu(parent))
                .showAndWait()
          },
          new MenuItem("Import profile...") {
            onAction = _ =>
              new ImportProfilePopup(() => parent.items(parent.items.indexOf(self.delegate)) = profilesMenu(parent))
                .showAndWait()
          },
          new MenuItem("Export profile...") {
            onAction = _ => new ExportProfilePopup().showAndWait()
          },
          new MenuItem("Manage profiles...") {
            onAction = _ =>
              new ManageProfilesPopup(() => parent.items(parent.items.indexOf(self.delegate)) = profilesMenu(parent))
                .showAndWait()
          }
        )
      )
  }

  private def redoItem(parent: Menu) = System.getProperty("os.name") match {
    case n if n != null && (n.startsWith("Windows") || n.startsWith("Linux")) =>
      new DisappearingMenuItem(
        "Redo",
        UndoRedo.hasRedos,
        'y',
        shift = false,
        parent,
        1,
        UndoRedo.redo(),
        self => UndoRedo.nextRedoDescription.onChange((_, _, c) => self.text = s"Redo $c")
      )
    case _ =>
      new DisappearingMenuItem(
        "Redo",
        UndoRedo.hasRedos,
        'z',
        shift = true,
        parent,
        1,
        UndoRedo.redo(),
        self => UndoRedo.nextRedoDescription.onChange((_, _, c) => self.text = s"Redo $c")
      )
  }

  lazy val showPercentagesMenuItem
      : CheckMenuItem with SelectMenuItem[CheckMenuItem, Boolean, lang.Boolean, Boolean, lang.Boolean] =
    new CheckMenuItem("Show percentages")
      with SelectMenuItem[CheckMenuItem, Boolean, lang.Boolean, Boolean, lang.Boolean] {
      override def memoryValue: Property[Boolean, lang.Boolean] = showPercentages

      override def changeValue: lang.Boolean => Unit = this.selected = _

      override def selectionProperty: CheckMenuItem => Property[Boolean, lang.Boolean] = _.selected

      override def changeActionName: lang.Boolean => String = if (_) "Show %" else "Hide %"

      override def editRegistryName: String = "showPercentages"

      override def initialiseProperty: Boolean => Unit = show => this.selected = show

      override def bindingFunction: ReadOnlyProperty[Boolean, lang.Boolean] => ObservableValue[lang.Boolean] = x => x
    }

  lazy val noOfPlayersMenu: Menu with SelectMenuItem[Menu, Int, Number, control.Toggle, control.Toggle] = {
    val radioItems: Array[RadioMenuItem] = Array(
      new RadioMenuItem("2"),
      new RadioMenuItem("3"),
      new RadioMenuItem("4"),
      new RadioMenuItem("5"),
      new RadioMenuItem("6"),
      new RadioMenuItem("7"),
      new RadioMenuItem("8"),
      new RadioMenuItem("9")
    )
    val toggles: ToggleGroup = new ToggleGroup() {
      this.toggles = radioItems
    }
    new Menu("No. of players") with SelectMenuItem[Menu, Int, Number, control.Toggle, control.Toggle] {

      override def memoryValue: Property[Int, Number] = noOfPlayers

      override def initialise: () => Unit = () => items = radioItems

      override def changeValue: control.Toggle => Unit = tog => toggles.selectToggle(tog)

      override def selectionProperty: Menu => ReadOnlyProperty[control.Toggle, control.Toggle] =
        _ => toggles.selectedToggle

      override def bindingFunction: ReadOnlyProperty[control.Toggle, control.Toggle] => ObservableValue[Number] =
        _.map(selected =>
          radioItems.indexWhere(_.delegate == selected) + radioItems.minBy(_.getText.toInt).getText.toInt
        )

      override def changeActionName: control.Toggle => String = _ => "Players"

      override val editRegistryName: String = "noOfPlayers"

      override def initialiseProperty: Int => Unit =
        noOfPlayers => toggles.selectToggle(radioItems(noOfPlayers - radioItems.minBy(_.getText.toInt).getText.toInt))
    }
  }

  lazy val draw: MenuBar = new MenuBar {
    menus = List(
      new Menu(
        "",
        new ImageView(
          new Image(
            "/ranger.png",
            requestedWidth = 20,
            requestedHeight = 20,
            preserveRatio = true,
            smooth = true
          )
        )
      ) {
        style = "-fx-padding: 4 4 3 3;"
        items = List(
          new MenuItem("About") {
            onAction = _ => new AboutPopup().showAndWait()
          },
          new MenuItem("Buy Me A Coffee") {
            onAction = _ => new BuyMeACoffeePopup().showAndWait()
          }
        )
      },
      new Menu("File") { self =>
        items = List(
          new DisappearingMenuItem("Save", show = EditRegistry.hasEdits, 's', false, self, 0, saveSelectedProfile()),
          profilesMenu(self)
        )
      },
      new Menu("Edit") { self =>
        items = Array(
          new DisappearingMenuItem(
            "Undo",
            UndoRedo.hasChanges,
            'z',
            shift = false,
            self,
            0,
            UndoRedo.undo(),
            self => UndoRedo.latestChangeDescription.onChange((_, _, c) => self.text = s"Undo $c")
          ),
          redoItem(self),
          new MenuItem("Manage Actions") {
            onAction = _ => new ManageActionsPopup().showAndWait()
          }
        )
      },
      new Menu("View") {
        items = List(
          showPercentagesMenuItem,
          noOfPlayersMenu
        )
      }
    )
  }
}
