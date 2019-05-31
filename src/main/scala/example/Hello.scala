package example

import scalaz.zio._
import scalaz.zio.console._

import scalaz.zio._
import scalaz.zio.console._

object Hello extends App {

  import FileRepository._

  def run(args: List[String]) =
    myAppLogic.fold(_ => 1, _ => 0)

  val myAppLogic = 
    for {
      files <- getListOfFiles("/Users/fagossa/Documents/fabian/__delpilartorres-files/xml", ".xml")
      _     <- putStrLn(s"Got ${files}")
    } yield ()
}

object FileRepository {
  import java.io.File
  import scalaz.zio.blocking._

  def getListOfFiles(dir: String, extension: String): Task[List[File]] = {
    (for {
      d     <- Task.effect(new File(dir))
      files <- Task.effect(
                d.listFiles
                .filter(_.isFile)
                .filter(_.getName.endsWith(extension))) //if d.exists && d.isDirectory
    } yield (files.toList)).orElse(ZIO.succeed(List[File]()))
}

  // import scala.xml.XML
  // val xml = XML.loadFile("/Users/fagossa/Documents/fabian/__delpilartorres2136277875")
}
