package example

import org.scalatest._
import domain._
import com.lucidchart.open.xtract.XmlReader
import scala.xml.XML

class XmlMappingSpec extends FlatSpec with Matchers {
  "The Log" should "be retrevaible from XML" in {
    val xmlData = 
    """
      |<Log>
      |  <Message>
      |      <From><User FriendlyName="Juliana" /></From>
      |      <To><User FriendlyName="Maria" /></To>
      |      <Text>hello, how are you?</Text>
      |  </Message>
      |</Log>
    """.stripMargin

    val expected = Log(
      messages = Seq(
        Message(
          From(User("Juliana")), 
          To(User("Maria")), 
          Text("hello, how are you?")
        )
      )
    )
    XmlReader.of[Log].read(XML.loadString(xmlData)).toOption.get shouldEqual expected
  }
}
