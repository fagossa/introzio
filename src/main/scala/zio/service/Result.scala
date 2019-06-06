package zio.service

case class Result(value: Map[String, Option[Throwable]]) { self =>
  final def |+|(that: Result): Result =
    Result(self.value ++ that.value)

  final def succeeded: Int = value.values.count(_.isEmpty)
  final def failures: List[Throwable] = value.values.collect { case Some(t) => t}.toList

}
object Result {
  def empty: Result = Result(Map.empty)
  def success(fileName: String): Result = Result(Map(fileName -> None))
  def failure(fileName: String, t: Throwable): Result = Result(Map(fileName -> Some(t)))
}
