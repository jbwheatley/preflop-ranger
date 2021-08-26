package preflop.ranger

import java.nio.file.{Files, Path}
import scala.collection.IterableOnce
import scala.jdk.CollectionConverters._

object ChartReader {
  def readAll(base: Path): Map[String, Array[Array[HandAction]]] =
    ChartNames.all.map { name =>
      name -> read(Path.of(base.toString, name.toLowerCase.replaceAll(" ", "")))
    }.toMap

  def read(path: Path): Array[Array[HandAction]] = {
    val array = Array.ofDim[HandAction](13, 13)
    if (!path.toFile.exists()) Files.createFile(path)
    zipWithDefault(0 to 12, Files.readAllLines(path).asScala.toList).foreach { case (x, str) =>
      zipWithDefault(0 to 12, str.split(",").map(_.trim)).foreach {
        case (y, "r100")   => array(x)(y) = HandAction.R_100
        case (y, "c100")   => array(x)(y) = HandAction.C_100
        case (y, "f100")   => array(x)(y) = HandAction.F_100
        case (y, "n100")   => array(x)(y) = HandAction.N_100
        case (y, "r50f50") => array(x)(y) = HandAction.R_50_F_50
        case (y, "r50n50") => array(x)(y) = HandAction.R_50_N_50
        case (y, "r50c50") => array(x)(y) = HandAction.R_50_C_50
        case (y, "c50f50") => array(x)(y) = HandAction.C_50_F_50
        case (y, "c50n50") => array(x)(y) = HandAction.C_50_N_50
        case (y, "f50n50") => array(x)(y) = HandAction.F_50_N_50
        case (y, "r75f25") => array(x)(y) = HandAction.R_75_F_25
        case (y, "r75c25") => array(x)(y) = HandAction.R_75_C_25
        case (y, "c75f25") => array(x)(y) = HandAction.C_75_F_25
        case (y, "r25f75") => array(x)(y) = HandAction.R_25_F_75
        case (y, "r25c75") => array(x)(y) = HandAction.R_25_C_75
        case (y, "c25f75") => array(x)(y) = HandAction.C_25_F_75
        case (y, _)        => array(x)(y) = HandAction.N_100
      }
    }
    array
  }

  private def zipWithDefault(left: Range, right: IterableOnce[String]): IndexedSeq[(Int, String)] = {
    val b   = left.iterableFactory.newBuilder[(Int, String)]
    val it1 = left.iterator
    val it2 = right.iterator
    while (it1.hasNext)
      if (it2.hasNext)
        b += ((it1.next(), it2.next()))
      else b += ((it1.next(), ""))
    b.result()
  }
}
