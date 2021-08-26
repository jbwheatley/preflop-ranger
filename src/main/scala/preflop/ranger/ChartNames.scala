package preflop.ranger

object ChartNames {
  object Pos {
    val BB  = "BIG BLIND"
    val SB  = "SMALL BLIND"
    val BTN = "BUTTON"
    val CO  = "CUT OFF"
    val HJ  = "HIJACK"
    val LJ  = "LOJACK"
  }

  val all: List[String] = List(
    RFI.SB,
    RFI.BTN,
    RFI.CO,
    RFI.HJ,
    RFI.LJ,
    VS3BET.SB.vsBB,
    VS3BET.BTN.vsSB,
    VS3BET.BTN.vsBB,
    VS3BET.CO.vsBTN,
    VS3BET.CO.vsSB,
    VS3BET.CO.vsBB,
    VS3BET.HJ.vsCO,
    VS3BET.HJ.vsBTN,
    VS3BET.HJ.vsSB,
    VS3BET.HJ.vsBB,
    VS3BET.LJ.vsHJ,
    VS3BET.LJ.vsCO,
    VS3BET.LJ.vsBTN,
    VS3BET.LJ.vsSB,
    VS3BET.LJ.vsBB,
    VSRFI.BB.vsSB,
    VSRFI.BB.vsBTN,
    VSRFI.BB.vsCO,
    VSRFI.BB.vsHJ,
    VSRFI.BB.vsLJ,
    VSRFI.SB.vsBTN,
    VSRFI.SB.vsCO,
    VSRFI.SB.vsHJ,
    VSRFI.SB.vsLJ,
    VSRFI.BTN.vsCO,
    VSRFI.BTN.vsHJ,
    VSRFI.BTN.vsLJ,
    VSRFI.CO.vsHJ,
    VSRFI.CO.vsLJ,
    VSRFI.HJ.vsLJ,
    COLD4BET.BBvsSB3Bet.andBTN,
    COLD4BET.BBvsSB3Bet.andCO,
    COLD4BET.BBvsSB3Bet.andEarly,
    COLD4BET.BB.vsBTN3Bet,
    COLD4BET.BB.vsCO3Bet,
    COLD4BET.BB.vsHJ3Bet,
    COLD4BET.SB.vsBTN3Bet,
    COLD4BET.SB.vsCO3Bet,
    COLD4BET.SB.vsHJ3Bet,
    COLD4BET.BTN.vsCO3Bet,
    COLD4BET.BTN.vsHJ3Bet,
    COLD4BET.CO.vsHJ3Bet,
    VS4BET.BB.vsSB4bet,
    VS4BET.BB.vsBTN4bet,
    VS4BET.BB.vsCO4bet,
    VS4BET.BB.vsHJ4Bet,
    VS4BET.BB.vsLJ4Bet,
    VS4BET.SB.vsBTN4bet,
    VS4BET.SB.vsCO4bet,
    VS4BET.SB.vsHJ4Bet, //105 22 31
    VS4BET.SB.vsLJ4Bet,
    VS4BET.BTN.vsCO4bet,
    VS4BET.BTN.vsHJ4Bet,
    VS4BET.BTN.vsLJ4Bet,
    VS4BET.CO.vsHJ4Bet,
    VS4BET.CO.vsLJ4Bet,
    VS4BET.HJ.vsLJ4Bet
  )

  object RFI {
    val SB  = s"RFI ${Pos.SB}"
    val BTN = s"RFI ${Pos.BTN}"
    val CO  = s"RFI ${Pos.CO}"
    val HJ  = s"RFI ${Pos.HJ}"
    val LJ  = s"RFI ${Pos.LJ}"
  }

  object VS3BET {
    private def make(open: String, raiser: String): String = s"$open VS $raiser 3-BET"

    object SB {
      val vsBB = make(Pos.SB, Pos.BB)
    }
    object BTN {
      val vsSB = make(Pos.BTN, Pos.SB)
      val vsBB = make(Pos.BTN, Pos.BB)
    }
    object CO {
      val vsSB  = make(Pos.CO, Pos.SB)
      val vsBB  = make(Pos.CO, Pos.BB)
      val vsBTN = make(Pos.CO, Pos.BTN)
    }
    object HJ {
      val vsSB  = make(Pos.HJ, Pos.SB)
      val vsBB  = make(Pos.HJ, Pos.BB)
      val vsBTN = make(Pos.HJ, Pos.BTN)
      val vsCO  = make(Pos.HJ, Pos.CO)
    }
    object LJ {
      val vsSB  = make(Pos.LJ, Pos.SB)
      val vsBB  = make(Pos.LJ, Pos.BB)
      val vsBTN = make(Pos.LJ, Pos.BTN)
      val vsCO  = make(Pos.LJ, Pos.CO)
      val vsHJ  = make(Pos.LJ, Pos.HJ)
    }
  }

  object VSRFI {
    def make(hero: String, open: String): String = s"$hero VS $open RFI"
    object BB {
      val vsSB  = make(Pos.BB, Pos.SB)
      val vsBTN = make(Pos.BB, Pos.BTN)
      val vsCO  = make(Pos.BB, Pos.CO)
      val vsHJ  = make(Pos.BB, Pos.HJ)
      val vsLJ  = make(Pos.BB, Pos.LJ)
    }

    object SB {
      val vsBTN = make(Pos.SB, Pos.BTN)
      val vsCO  = make(Pos.SB, Pos.CO)
      val vsHJ  = make(Pos.SB, Pos.HJ)
      val vsLJ  = make(Pos.SB, Pos.LJ)
    }

    object BTN {
      val vsCO = make(Pos.BTN, Pos.CO)
      val vsHJ = make(Pos.BTN, Pos.HJ)
      val vsLJ = make(Pos.BTN, Pos.LJ)
    }

    object CO {
      val vsHJ = make(Pos.CO, Pos.HJ)
      val vsLJ = make(Pos.CO, Pos.LJ)
    }

    object HJ {
      val vsLJ = make(Pos.HJ, Pos.LJ)
    }
  }

  object COLD4BET {
    def make(hero: String, _3bet: String) = s"$hero 4-BET VS ${_3bet} 3-BET"
    object BBvsSB3Bet {
      val andBTN   = s"${Pos.BB} VS ${Pos.SB} 3-BET AND ${Pos.BTN} OPEN"
      val andCO    = s"${Pos.BB} VS ${Pos.SB} 3-BET AND ${Pos.CO} OPEN"
      val andEarly = s"${Pos.BB} VS ${Pos.SB} 3-BET AND EARLY OPEN"
    }

    object BB {
      val vsBTN3Bet = make(Pos.BB, Pos.BTN)
      val vsCO3Bet  = make(Pos.BB, Pos.CO)
      val vsHJ3Bet  = make(Pos.BB, Pos.HJ)
    }

    object SB {
      val vsBTN3Bet = make(Pos.SB, Pos.BTN)
      val vsCO3Bet  = make(Pos.SB, Pos.CO)
      val vsHJ3Bet  = make(Pos.SB, Pos.HJ)
    }

    object BTN {
      val vsCO3Bet = make(Pos.BTN, Pos.CO)
      val vsHJ3Bet = make(Pos.BTN, Pos.HJ)
    }

    object CO {
      val vsHJ3Bet = make(Pos.CO, Pos.HJ)
    }
  }

  object VS4BET {
    def make(hero: String, villain: String) = s"$hero VS $villain 4-BET"

    object BB {
      val vsSB4bet  = make(Pos.BB, Pos.SB)
      val vsBTN4bet = make(Pos.BB, Pos.BTN)
      val vsCO4bet  = make(Pos.BB, Pos.CO)
      val vsHJ4Bet  = make(Pos.BB, Pos.HJ)
      val vsLJ4Bet  = make(Pos.BB, Pos.LJ)
    }

    object SB {
      val vsBTN4bet = make(Pos.SB, Pos.BTN)
      val vsCO4bet  = make(Pos.SB, Pos.CO)
      val vsHJ4Bet  = make(Pos.SB, Pos.HJ)
      val vsLJ4Bet  = make(Pos.SB, Pos.LJ)
    }

    object BTN {
      val vsCO4bet = make(Pos.BTN, Pos.CO)
      val vsHJ4Bet = make(Pos.BTN, Pos.HJ)
      val vsLJ4Bet = make(Pos.BTN, Pos.LJ)
    }

    object CO {
      val vsHJ4Bet = make(Pos.CO, Pos.HJ)
      val vsLJ4Bet = make(Pos.CO, Pos.LJ)
    }

    object HJ {
      val vsLJ4Bet = make(Pos.HJ, Pos.LJ)
    }
  }
}
