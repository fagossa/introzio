package zio.service

import java.io.PrintWriter

import scalaz.zio.{IO, Task, ZIO}

import scala.io.BufferedSource

trait IOResources {

  def close(s: BufferedSource): ZIO[Any, Nothing, Unit] =
    Task.effect(s.close()).catchAll(_ => IO.unit)

  def close(s: PrintWriter): ZIO[Any, Nothing, Unit] =
    Task.effect(s.close()).catchAll(_ => IO.unit)

}
