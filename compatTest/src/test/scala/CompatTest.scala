import utest._

object CompatTest extends TestSuite {
  val tests = Tests {
    'default-{
      Lib.int ==> 42
    }
  }
}
