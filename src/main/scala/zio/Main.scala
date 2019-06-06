package zio

import scalaz.zio._
import scalaz.zio.console._
import zio.service.{FileService, LogService}

object Main extends App {

  def run(args: List[String]): ZIO[Console, Nothing, Int] =
    myAppLogic.fold(_ => 1, _ => 0)

  private val originFolder = "/Users/myuser/Documents/messages/xml"
  private val destinationFolder = "/Users/myuser/Documents/messages/csv"

  val myAppLogic =
    for {
      files       <- FileService.listOfFiles(originFolder, _.getName.endsWith(".xml"))
      _           <- putStrLn(s"Got <${files.size}> raw files")
      contents    <- ZIO.foreach(files.map(LogService.readFileContents)) { identity }
      allLogs     <- ZIO.foreach(contents.map(LogService.transformToMessage)) { identity }
      correctLogs = allLogs.collect { case Some(i) => i }
      results     <- ZIO.foreach(correctLogs)(LogService.writeLogToFile(destinationFolder))
      _           <- ZIO.foreach(results){result => putStrLn(s"Result <$result>")}
    } yield ()

}
