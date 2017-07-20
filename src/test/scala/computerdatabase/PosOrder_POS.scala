import java.sql.{DriverManager, Connection}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import scala.concurrent.duration._
import scala.util.Random


class PosOrder_POS extends Simulation {
  val start = 10000000;
  val end = 99999000;
  val rnd = new Random

  println("170" + Random.nextInt(end - start + 1 ).toString())
  val feeder3 = Array(Map("posno" -> (Random.nextInt(end - start + 1 ).toString()))).random
//  println(feeder)
//  val driver = "com.mysql.jdbc.Driver"
//  并发个数
//  val number = 200
//  println("Data Source Length："+feeder.records.length)
//  val url = "jdbc:mysql://172.16.0.52:5306/posdb"
////  val driver = "com.mysql.jdbc.Driver"
//  val username = "postest"
//  val password = "DJdg@903902901"
//  var connection: Connection = _
//  Class.forName(driver)
//  connection = DriverManager.getConnection(url, username, password)
//  val statement = connection.createStatement
//  val feeder0 = jdbcFeeder("jdbc:mysql://172.16.0.52:5306/posdb", "postest", "DJdg@903902901", "SELECT store_id FROM store_employee").random

//  val feeder = jdbcFeeder("jdbc:mysql://172.16.0.52:5306/posdb", "postest", "DJdg@903902901", "SELECT token, store_id as id FROM store_employee where token is not NULL and last_login is not NULL limit 10").random
  val feeder = jdbcFeeder("jdbc:mysql://172.16.0.52:5306/posdb", "postest", "DJdg@903902901", "SELECT token, store_id as id FROM `store_employee` WHERE last_login >= date_format(date_sub(date_sub(now(), INTERVAL WEEKDAY(NOW()) DAY), INTERVAL 1 WEEK), '%Y-%m-%d')").random
  println(feeder)
  println("Data Source:" + feeder.records)
  println("Data Source Length:" + feeder.records.length)
  val feeder2 = jdbcFeeder("jdbc:mysql://172.16.0.52:5306/posdb", "postest", "DJdg@903902901", "SELECT login_tel as tel, store_id as id FROM `store_employee`, store WHERE password = \"e10adc3949ba59abbe56e057f20f883e\" and store.store_category = 0 and store.id = store_employee.store_id").circular
  println(feeder2)
  println("Data2 Source:" + feeder2.records)
  println("Data2 Source Length:" + feeder2.records.length)

  val httpConf = http.baseURL("http://devtest-pos.thy360.com/")

  object noticeTest {
    val test = scenario("notice") // A scenario is a chain of requests and pauses
      .feed(feeder)
      .exec(http("notice").get("ja/v1/boss/setting/notice")
        .headers(Map("token" -> "${token}", "storeNo" -> "${id}"))
        .body(StringBody("""{"notice":"1234567890123456789"}"""))
        .asJSON.check(status.is(200))
      )
      .exec { session =>
        println(session)
        session }
  }
  object addCartTest {
    val cartTest = scenario("addCart") // A scenario is a chain of requests and pauses
      .feed(feeder2)
        .feed(feeder3)
//      .doIf(session => !session.contains("token"))
        .tryMax(1) {
          exec(http("posLogin").post("ja/pos/v1/login")
            .body(StringBody(
              """{
                          "username":  "${tel}",
                          "pos_info": {
                              "pos_version": "1.0",
                              "pos_type": "Google Nexus 9 - 5.0.0 - API 21 - 1536x2048",
                              "rom_version": "3.10.0-genymotion-g08e528d",
                              "device_type": 1,
                              "pos_no": "${posno}",
                              "system_version": "5.0"
                          },
                          "pwd": "123456",
                          "shop_no": ${id}
                      }""")).asJSON
          .check(status.is(200), jsonPath("$.statusCode").is(0.toString())))
           }
        .pause(2)
//      .exec { session =>
//        println(session)
//        session.remove("data")
//        session }
        .doIf(session => session.contains("token")) {
              exec(http("addCart").post("ja/pos/v1/synccart")
                .headers(Map("token" -> "${token}", "storeNo" -> "${id}"))
                .body(StringBody("""{
                                       "pay_type": 0,
                                       "loose_change_amount": "0.00",
                                       "order_source": 0,
                                       "order_no": "37022600710304224116",
                                       "order_time": "2017-06-26 22:15:12",
                                       "cashier_name": "杨文军",
                                       "order_status": 0,
                                       "pay_way": 0,
                                       "product_amount": "5.00",
                                       "total_discount_amount": "0.00",
                                       "order_amount": "5.00",
                                       "customer_time": "2017-06-26 22:15:12",
                                       "store_type": 0,
                                       "current_order_item": [
                                           {
                                               "is_weighing": 0,
                                               "orderNum": 0,
                                               "product_id": 0,
                                               "spec_id": -1,
                                               "barcode": "-1",
                                               "special_price": "5",
                                               "product_name": "无码商品",
                                               "isManySpecs": false,
                                               "item_type": -1,
                                               "special_price_type": 0,
                                               "sale_cnt": "1",
                                               "seari_num": 0,
                                               "new_product": 0,
                                               "sale_price": "5",
                                               "category_id": -1,
                                               "img_url": "",
                                               "purchase_price": "5",
                                               "order_item_id": 0
                                           }
                                       ],
                                       "cashier_id": 10,
                                       "order_item": [
                                           {
                                               "is_weighing": 0,
                                               "orderNum": 0,
                                               "product_id": 0,
                                               "spec_id": -1,
                                               "barcode": "-1",
                                               "special_price": "5",
                                               "product_name": "无码商品",
                                               "isManySpecs": false,
                                               "item_type": -1,
                                               "special_price_type": 0,
                                               "sale_cnt": "1",
                                               "seari_num": 0,
                                               "new_product": 0,
                                               "sale_price": "5",
                                               "category_id": -1,
                                              "img_url": "",
                                              "purchase_price": "5",
                                               "order_item_id": 0
                                           }
                                       ],
                                       "member_first_order": false,
                                       "delivery_status": 0,
                                       "orderLocal": 1,
                                      "order_type": 0

                                   }"""))
                .asJSON.check(status.is(200) ,jsonPath("$.statusCode").is(0.toString()))
              )
        }
      }


  val notice = scenario("noticet").exec(noticeTest.test)
  val cart = scenario("cart").exec(addCartTest.cartTest)
	
	setUp(
//    notice.inject(constantUsersPerSec(1) during(10)),
    cart.inject(constantUsersPerSec(70) during(3600))
  ).protocols(httpConf)
}
