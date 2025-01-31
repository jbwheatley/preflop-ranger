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

package preflop.ranger.custom

import scalafx.beans.property.BooleanProperty
import scalafx.geometry.Insets
import scalafx.scene.control.{TextField, TextFormatter, Tooltip}
import scalafx.scene.layout.{Background, BackgroundFill, CornerRadii}
import scalafx.scene.paint.Color
import scalafx.util.Duration

class HandActionTextField(private var initialValue: Int, replaceInParent: (TextField, TextField) => Unit) { self =>
  val valid: BooleanProperty   = BooleanProperty(true)
  val updated: BooleanProperty = BooleanProperty(false)

  def reset(): Unit = {
    field.getText.toIntOption.foreach {
      initialValue = _
    }
    updated.value = false
  }

  def validate: Option[Int] = field.getText.toIntOption match {
    case Some(i) if i >= 0 && i <= 100 =>
      valid.value = true
      Some(i)
    case _ =>
      valid.value = false
      None
  }

  def mkField(_text: String): TextField = new TextField() { tf =>
    text = _text

    prefColumnCount = 2
    focused.onChange { (_, _, n) =>
      if (!n) {
        validate
        ()
      }
    }

    text.onChange { (_, _, t) =>
      updated.value = t.toIntOption.exists(_ != initialValue)
      if (!valid.get()) {
        validate
        ()
      }
    }

    textFormatter = new TextFormatter[String]((change: TextFormatter.Change) => {
      change.text = change.text.filter(_.isDigit)
      change
    })

    valid.onChange { (_, _, valid) =>
      if (!valid) {
        tooltip = new Tooltip("Must be number between 0 and 100") {
          showDelay = Duration(200)
        }
        val bg = tf.backgroundProperty().get()
        if (bg != null) {
          val fill1 = bg.getFills.get(0)
          val fill2 = bg.getFills.get(1)
          val newBg = new Background(
            fills = Array(
              new BackgroundFill(
                Color.DarkRed,
                new CornerRadii(fill1.getRadii),
                new Insets(fill1.getInsets)
              ),
              new BackgroundFill(
                Color.DarkSalmon,
                new CornerRadii(fill2.getRadii),
                new Insets(fill2.getInsets)
              )
            )
          )
          field.deselect()
          field.backgroundProperty().setValue(newBg)
        }
      } else {
        val t = mkField(field.getText)
        replaceInParent(field, t)
        field = t
        field.requestFocus()
        field.end()
      }
    }
  }

  var field: TextField = mkField(initialValue.toString)
}
