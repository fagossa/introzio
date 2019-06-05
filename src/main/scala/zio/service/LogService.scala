package zio.service

import java.io.File

import zio.domain.Log
import scalaz.zio.{Task, ZIO}

case class FileContent(fileName: String, contents: String)

case class LogWithName(fileName: String, log: Log)

object LogService extends IOResources {

  import zio.domain._

  def readFileContents(file: File): Task[FileContent] = {
    import scala.io.Source
    // We use brackets to close resources
    Task.effect(Source.fromFile(file)(scala.io.Codec.UTF8)).bracket {
      close
    } { source =>
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
      xml <- Task.effect(XML.loadString(contents.contents))
      // handle event that DOES NOT launch exceptions
      maybeLog <- Task.effectTotal(XmlReader.of[Log].read(xml).toOption)
      response <- Task.effectTotal(maybeLog.map(LogWithName(contents.fileName, _)))
    } yield response
  }

  def writeLogToFile(dest: String)(log: LogWithName): Task[Unit] = {
    import java.io._
    val file = new File(s"$dest/${log.fileName.replaceAll(".xml", "")}.csv")
    for {
      _ <- Task.effect(new PrintWriter(file)).bracket {
        close
      } { writer =>
        for {
          _ <- Task.effect(writer.write("from, to, text\n"))
          // Same as Traverse[T]
          _ <- ZIO.foreach(log.log.messages) { message => Task.effect(writer.write(s"${Message.toLine(message)}\n")) }
        } yield ()
      }
    } yield ()
  }

}
