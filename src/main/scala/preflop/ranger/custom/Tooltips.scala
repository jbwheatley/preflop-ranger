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

import scalafx.animation.PauseTransition
import scalafx.scene.control.Tooltip
import scalafx.stage.Stage
import scalafx.util.Duration

object Tooltips {
  private var tooltipShowing: Boolean = false

  def showTooltip(stage: Stage)(text: String): Unit = if (!tooltipShowing) {
    val tt = new Tooltip(text) {
      autoFix = true
    }
    // TODO show over where the stage currently is
    tooltipShowing = true
    tt.show(stage)
    new PauseTransition(Duration(2000)) {
      onFinished = _ => {
        tt.hide()
        tooltipShowing = false
      }
    }.play()
  }

}
