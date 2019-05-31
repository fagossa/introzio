package example

import scalaz.zio._
import scalaz.zio.console._

object Main extends App {

  import example.service.LogService._

  def run(args: List[String]): ZIO[Console, Nothing, Int] =
    myAppLogic.fold(_ => 1, _ => 0)

  private val originName = "/Users/myuser/Documents/messages/xml"
  private val destinationName = "/Users/myuser/Documents/messages/csv"

  val myAppLogic =
    for {
      files       <- getListOfFiles(originName, ".xml")
      _           <- putStrLn(s"Got these raw files: $files")
      contents    <- ZIO.foreach(files.map(readFileContents)) { identity }
      allLogs     <- ZIO.foreach(contents.map(transformToMessage)) { identity }
      correctLogs = allLogs.collect { case Some(i) => i }
      _           <- ZIO.foreach(correctLogs)(writeLogToFile(destinationName))
    } yield ()

}
