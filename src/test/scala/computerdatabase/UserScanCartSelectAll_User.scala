import io.gatling.core.Predef._
import io.gatling.http.Predef.{StringBody, _}
import io.gatling.jdbc.Predef._

class UserScanCartSelectAll_User extends Simulation {
  val feeder = jdbcFeeder("jdbc:mysql://172.16.0.52:5306/posdb", "postest", "DJdg@903902901", "SELECT token as token_w FROM user_profile WHERE DATEDIFF(update_time,NOW())=0 And token is not NULL and wx_open_id = \"ob8b70BNwE3bkVyFemg0gH_grAP0\"").random
//  println(feeder)
//  println("Data Source:" + feeder.records)
//  println("Data Source Length:" + feeder.records.length)

  //  val feeder = jdbcFeeder("jdbc:mysql://172.16.0.52:5306/posdb", "postest", "DJdg@903902901", "SELECT id FROM `store` WHERE store_category = 0 and store_type = 1 ").random
  val feeder2 = jdbcFeeder("jdbc:mysql://172.16.0.52:5306/product", "postest", "DJdg@903902901", "SELECT  product.id as pid, product_spec.id as specId FROM product, product_spec WHERE product.user_id = 10 And product_spec.product_id = product.id ").circular
//  println(feeder2)
//  println("Data Source:" + feeder2.records)
//  println("Data Source Length:" + feeder2.records.length)
  val httpConf = http.baseURL("http://devtest-pos.thy360.com/")

  object selectAllTest {
    val test = scenario("user_selectAll") // A scenario is a chain of requests and pauses
      .feed(feeder)
      .feed(feeder2)
//      .exec(http("login").post("ja/user/v1/user/login")
//          .headers(Map("token"->"${token_w}"))
//          .body(StringBody("""{
//                          "code":  "1234",
//                          "phone": "13712345678"
//                      }""")).asJSON.check(status.is(200),jsonPath("$.data.token").saveAs("token"))
//      )
      .doIf(session => session.contains("token_w")) {
          exec(http("clear").post("ja/user/v1/cart/10/clear")
            .headers(Map("token" -> "${token_w}"))
            .check(status.is(200))
          )
//            .exec { session =>
//              println(session)
//              session
//            }
            .doIf(session => session.contains("pid") && session.contains("specId")) {
              exec(http("add_cart1").post("ja/user/v1/cart/10/${pid}/${specId}/add")
                .headers(Map("token" -> "${token_w}"))
                .body(StringBody("""{"tableNo":"5"}"""))
                .asJSON.check(status.is(200) ,jsonPath("$.statusCode").is(0.toString()))
              ).
                exec(http("add_cart2").post("ja/user/v1/cart/10/${pid}/${specId}/add")
                  .headers(Map("token" -> "${token_w}"))
                  .body(StringBody("""{"tableNo":"5"}"""))
                  .asJSON.check(status.is(200) ,jsonPath("$.statusCode").is(0.toString()))
                )
            exec(http("selectAll").post("ja/user/v1/cart/10/selectAll/0")
              .headers(Map("token" -> "${token_w}"))
              .body(StringBody("""{"tableNo":"5"}"""))
              .asJSON.check(status.is(200) ,jsonPath("$.statusCode").is(0.toString()))
            )
            }
        }
      }
  val user_selectAll = scenario("user_selectAll").exec(selectAllTest.test)
  setUp(
    //    notice.inject(constantUsersPerSec(1) during(10)),
    user_selectAll.inject(constantUsersPerSec(30) during (10))
  ).protocols(httpConf)
}
