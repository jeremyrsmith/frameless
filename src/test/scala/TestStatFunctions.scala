package typedframe

import org.apache.spark.sql.SpecWithContext
import shapeless.test.illTyped

class TestStatFunctions extends SpecWithContext {
  import testImplicits._
  
  def fooTF: TypedFrame[Foo] = TypedFrame(Seq((1, "id1"), (4, "id3"), (5, "id2")).toDF)
  
  case class Ns(i: Int, j: Int, d: Double, s: String)
  val nsSeq = Seq((1, 2, 3.0, "s"), (2, 2, 8.0, "S"), (4, 4, 6.0, "c"))
  def ns: TypedFrame[Ns] = TypedFrame(nsSeq.toDF)
  
  test("cov") {
    ns.stat.cov('i, 'j)
    ns.stat.cov('i, 'd)
    illTyped("ns.stat.cov('i, 's)")
  }
  
  test("corr") {
    ns.stat.corr('i, 'j)
    ns.stat.corr('i, 'd)
    illTyped("ns.stat.corr('i, 's)")
  }
  
  test("crosstab") {
    ns.stat.crosstab('i, 'j)
    ns.stat.crosstab('i, 'd)
    ns.stat.crosstab('i, 's)
    illTyped("ns.stat.corr('a, 's)")
    illTyped("ns.stat.corr('i, 'a)")
  }
  
  test("freqItems") {
    import eu.timepit.refined.auto._
    
    fooTF.stat.freqItems()('a)
    fooTF.stat.freqItems(1.0)('b)
    fooTF.stat.freqItems(0.5)('a, 'b)
    illTyped("fooTF.stat.freqItems(0.5)()")
    illTyped("fooTF.stat.freqItems(-0.1)('a)")
    illTyped("fooTF.stat.freqItems(1.1)('a)")
  }
  
  test("sampleBy") {
    import eu.timepit.refined.auto._
    
    fooTF.stat.sampleBy('a, Map(1 -> 0.5, -1 -> 1.0), 10l)
    fooTF.stat.sampleBy('b, Map("s" -> 0.0, "c" -> 0.5, "S" -> 0.1), 10l)
    illTyped("fooTF.stat.sampleBy('b, Map(1 -> 0.5), 10l)")
    illTyped("fooTF.stat.sampleBy('a, Map(1 -> 1.1), 10l)")
  }
}
