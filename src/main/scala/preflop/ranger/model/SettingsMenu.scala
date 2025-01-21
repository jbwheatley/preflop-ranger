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

import preflop.ranger.PreflopRanger
import preflop.ranger.PreflopRanger.allProfiles
import preflop.ranger.edit.UndoRedo.Change
import preflop.ranger.edit.{EditRegistry, UndoRedo}
import preflop.ranger.model.FileData.ActionData
import preflop.ranger.popups._
import scalafx.beans.property.{BooleanProperty, IntegerProperty, ObjectProperty}
import scalafx.event.subscriptions.Subscription
import scalafx.scene.control._
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.input.{KeyCharacterCombination, KeyCombination}

object SettingsMenu {

  var showPercentagesInit: Boolean         = _
  var noOfPlayersInit: Int                 = _
  var actionsInit: Map[String, ActionData] = _

  val showPercentages: BooleanProperty                 = BooleanProperty(true)
  val noOfPlayers: IntegerProperty                     = IntegerProperty(0)
  val actions: ObjectProperty[Map[String, ActionData]] = ObjectProperty(Map.empty[String, ActionData])

  def variableRaiseSizes: Array[String] = actions.getValue.view
    .filterKeys(_.toDoubleOption.isDefined)
    .keys
    .toArray
    .sorted

  def save(): Unit = {
    showPercentagesInit = showPercentages.value
    noOfPlayersInit = noOfPlayers.value
    actionsInit = actions.value
  }

  private class DisappearingMenuItem(
      name: String,
      show: BooleanProperty,
      shortcutKey: Char,
      shift: Boolean,
      parent: Menu,
      idxInParent: Int,
      action: => Unit,
      conditionalNameUpdate: MenuItem => Subscription = _ => () => ()
  ) extends MenuItem { self =>
    conditionalNameUpdate(self)
    text = name
    show.onChange {
      parent.items(idxInParent) = new DisappearingMenuItem(
        self.text.get(),
        show,
        shortcutKey,
        shift,
        parent,
        idxInParent,
        action,
        conditionalNameUpdate
      )
    }
    if (!show.get()) styleClass.addOne("nohover")
    onAction = _ => action
    style = if (show.get()) "-fx-text-color: black;" else "-fx-text-fill: lightgray;"
    private val modifiers: List[KeyCombination.Modifier] =
      if (shift) List(KeyCombination.ShortcutDown, KeyCombination.ShiftDown) else List(KeyCombination.ShortcutDown)
    accelerator = new KeyCharacterCombination(shortcutKey.toString, modifiers.map(_.delegate): _*)
  }

  private var switching: Boolean = false

  def switchProfile(selected: String): Unit = {
    UndoRedo.reset()
    PreflopRanger.loadProfile(selected)
    PreflopRanger.resetStage()
  }

  private def switchProfile(
      toggles: javafx.scene.control.ToggleGroup,
      old: javafx.scene.control.Toggle,
      selected: String
  ): Unit = if (!switching) {
    switching = true
    if (EditRegistry.hasEdits.value) {
      new SwitchProfileConfirmationPopup(toggles, old, selected).showAndWait()
    } else {
      switchProfile(selected)
    }
    switching = false
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
      switchProfile(toggles, old, selected.asInstanceOf[javafx.scene.control.RadioMenuItem].getText)
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

  def draw(): MenuBar = new MenuBar {
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
          new DisappearingMenuItem("Save", show = EditRegistry.hasEdits, 's', false, self, 0, PreflopRanger.saveData()),
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
          new CheckMenuItem("Show percentages") {
            private var undoRedoAction = false

            selected = showPercentages.value
            showPercentages.bind(selected)
            selected.onChange { (_, _, select) =>
              val change = () => {
                undoRedoAction = true
                selected = select
                undoRedoAction = false
              }
              val undo = () => {
                undoRedoAction = true
                selected = !select
                undoRedoAction = false
              }
              if (!undoRedoAction) {
                val desc = if (select) "Show %" else "Hide %"
                UndoRedo.add(Change(change, undo, desc))
              }
              if (showPercentages.value != showPercentagesInit) EditRegistry.register("showPercentages")
              else EditRegistry.deregister("showPercentages")
            }
          },
          new Menu("No. of players") {
            private var undoRedoAction = false

            private val radioItems: Array[RadioMenuItem] = Array(
//              new RadioMenuItem("2"), //TODO support heads up
              new RadioMenuItem("3"),
              new RadioMenuItem("4"),
              new RadioMenuItem("5"),
              new RadioMenuItem("6"),
              new RadioMenuItem("7"),
              new RadioMenuItem("8"),
              new RadioMenuItem("9")
            )
            private val toggles: ToggleGroup = new ToggleGroup() {
              this.toggles = radioItems
            }
            toggles.selectToggle(radioItems(noOfPlayers.value - 3))
            toggles.selectedToggle.onChange { (_, old, selected) =>
              val change = () => {
                undoRedoAction = true
                toggles.selectToggle(selected)
                undoRedoAction = false
              }
              val undo = () => {
                undoRedoAction = true
                toggles.selectToggle(old)
                undoRedoAction = false
              }
              // don't add change to the redo stack if we're acting inside the undo/redo action
              if (!undoRedoAction) UndoRedo.add(Change(change, undo, "Players"))
              noOfPlayers.value = radioItems.indexWhere(_.delegate == selected) + 3
              if (noOfPlayers.value != noOfPlayersInit) EditRegistry.register("noOfPlayers")
              else EditRegistry.deregister("noOfPlayers")
            }
            items = radioItems
          }
        )
      }
    )
  }
}
