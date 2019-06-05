package zio.service

import java.io.File

import scalaz.zio.{Task, ZIO}

object FileService {

  def listOfFiles(dir: String, f: File => Boolean): Task[List[File]] = {
    (for {
      d <- Task.effect(new File(dir))
      files <- Task.effect(
        d.listFiles //TODO: can we handle this filter -> if d.exists && d.isDirectory
          .filter(_.isFile)
          .filter(f(_))
      )
    } yield files.toList)
      .orElse(ZIO.succeed(List[File]())) // Specify a 'pure' value
  }

}
