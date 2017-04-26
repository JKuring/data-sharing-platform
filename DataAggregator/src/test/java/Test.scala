import com.eastcom.common.utils.parser.MqHeadParser
import com.rabbitmq.client.AMQP.BasicProperties

/**
  * Created by linghang.kong on 2017/4/26.
  */
object Test {

  def main(args: Array[String]): Unit = {
    val a = Array("1", "2", "3", "4")
    val tmp = MqHeadParser.getHeadProperties(a)
    println(tmp.size())
    //    tmp.put(endTime, TimeTransform.getDate(System.currentTimeMillis))
    //    tmp.put(status, String.valueOf(result))
    new BasicProperties.Builder().headers(tmp).build
  }
}
