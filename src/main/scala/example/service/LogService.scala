package example.service

import scala.io.BufferedSource
import java.io.{File, PrintWriter}

import domain.Log
import scalaz.zio.{IO, Task, ZIO}

case class FileContent(fileName: String, contents: String)
case class LogWithName(fileName: String, log: Log)

object LogService {
  import domain._

  def close(s: BufferedSource): ZIO[Any, Nothing, Unit] =
    Task.effect(s.close()).catchAll(_ => IO.unit)

  def close(s: PrintWriter): ZIO[Any, Nothing, Unit] =
    Task.effect(s.close()).catchAll(_ => IO.unit)

  def readFileContents(file: File): Task[FileContent] = {
    import scala.io.Source
    // We use brackets to close resources
    Task.effect(Source.fromFile(file)(scala.io.Codec.UTF8)).bracket { close } { source =>
      Task.effect(source.getLines().mkString("\n")).map {
        content => FileContent(file.getName, content)
      }
    }
  }

  // TODO: replace this with an either?
  def transformToMessage(contents: FileContent): Task[Option[LogWithName]] = {
    import com.lucidchart.open.xtract.XmlReader
    import scala.xml.XML
    for {
      // handle event that can launch exceptions
      xml      <- Task.effect(XML.loadString(contents.contents))
      // handle event that DOES NOT launch exceptions
      maybeLog <- Task.effectTotal(XmlReader.of[Log].read(xml).toOption)
      response <- Task.effectTotal(maybeLog.map(LogWithName(contents.fileName, _)))
    } yield response
  }

  def writeLogToFile(dest: String)(log: LogWithName): Task[Unit] = {
    import java.io._
    val toLine: Message => String = message =>
      s"${message.from.user.friendlyName.replaceAll(",", ".")},${message.to.user.friendlyName.replaceAll(",", ".")},${message.text.value.replaceAll(",", ".")}"
    for {
      _  <- Task.effect(new PrintWriter(new File(s"$dest/${log.fileName.replaceAll(".xml", "")}.csv" ))).bracket { close } { writer =>
        for {
          _ <- Task.effect(writer.write("from, to, text\n"))
          _ <- ZIO.foreach(log.log.messages) { message => Task.effect(writer.write(s"${toLine(message)}\n")) }
        } yield()
      }
    } yield ()
  }

  def getListOfFiles(dir: String, extension: String): Task[List[File]] = {
    (for {
      d     <- Task.effect(new File(dir))
      files <- Task.effect(
                d.listFiles //TODO: can we handle this filter -> if d.exists && d.isDirectory
                .filter(_.isFile)
                .filter(_.getName.endsWith(extension))
              )
    } yield files.toList)
      .orElse(ZIO.succeed(List[File]()))
  }
}
