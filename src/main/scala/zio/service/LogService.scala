package zio.service

import java.io.File

import scalaz.zio.{Task, ZIO}

/**
  * For blocking events see:
  *  {{{ val t: Task[Unit] = blocking.effectBlocking(???) }}}
  */
object LogService extends IOResources {

  import zio.domain._

  def readFileContents(file: File): Task[FileContent] = {
    import scala.io.Source
    // We use brackets to close resources
    Task(Source.fromFile(file)(scala.io.Codec.UTF8)).bracket(closeSource){ source =>
      Task(source.getLines().mkString("\n"))
        .map { content => FileContent(file.getName, content)
      }
    }
  }

  def transformToMessage(contents: FileContent): Task[Option[LogWithName]] = {
    import com.lucidchart.open.xtract.XmlReader
    import scala.xml.XML
    for {
      // effect === apply := handle event that can launch exceptions
      xml <- Task(XML.loadString(contents.contents))
      // handle an already ready event
      maybeLog <- Task.succeed(XmlReader.of[Log].read(xml).toOption)
      // handle event that DOES NOT launch exceptions
      response <- Task.effectTotal(maybeLog.map(LogWithName(contents.fileName, _)))
    } yield response
  }

  def writeLogToFile(dest: String)(log: LogWithName): Task[Result] = {
    import java.io._
    for {
      file      <- Task(new File(s"$dest/${log.fileName.replaceAll(".xml", "")}.csv"))
      receipts  <- Task(new PrintWriter(file)).bracket(closeWriter){ writer =>
        for {
          _        <- Task(writer.write("from, to, text\n"))
          // Same as Traverse[T]
          // Max 10 threads: ZIO.foreachParN(10)(log.log.messages)
          receipts <- ZIO.foreach(log.log.messages) { message =>
                        // Transform each individual message into a Result()
                        Task(writer.write(s"${Message.toLine(message)}\n"))
                          .fold(t => Result.failure(log.fileName, t), _ => Result.success(log.fileName))
                     }
        } yield receipts
      }
    } yield receipts.foldLeft(Result.empty){ _ |+| _ }
  }

}

