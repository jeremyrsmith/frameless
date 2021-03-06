package frameless

import org.apache.spark.sql.DataFrameNaFunctions

import shapeless._
import shapeless.ops.hlist.{Length, IsHCons, Selector, Fill}

final class TypedDataFrameNaFunctions[Schema <: Product] private[frameless]
  (dfn: DataFrameNaFunctions)
  (implicit val fields: Fields[Schema])
    extends Serializable {

  def dropAny = new DropHowCurried("any")

  def dropAll = new DropHowCurried("all")

  class DropHowCurried(how: String) extends SingletonProductArgs {
    def applyProduct[C <: HList](columns: C)
      (implicit f: FieldsExtractor[Schema, C]): TypedDataFrame[Schema] =
        new TypedDataFrame(columns match {
          case HNil => dfn.drop(how)
          case _ => dfn.drop(how, f(columns))
        })
  }

  def drop(minNonNulls: Int) = new DropMinNNCurried(minNonNulls)

  class DropMinNNCurried(minNonNulls: Int) extends SingletonProductArgs {
    def applyProduct[C <: HList](columns: C)
      (implicit h: IsHCons[C], f: FieldsExtractor[Schema, C]): TypedDataFrame[Schema] =
        new TypedDataFrame(dfn.drop(minNonNulls, f(columns)))
  }
  def fillAll(value: Double): TypedDataFrame[Schema] = new TypedDataFrame(dfn.fill(value))

  def fillAll(value: String): TypedDataFrame[Schema] = new TypedDataFrame(dfn.fill(value))

  def fill[T](value: T) = new FillCurried[T](value)

  type CanFill = Int :: Long :: Float :: Double :: String :: HNil

  class FillCurried[T](value: T) extends SingletonProductArgs {
    def applyProduct[C <: HList, G <: HList, S <: HList, NC <: Nat]
      (columns: C)
      (implicit
        h: IsHCons[C],
        s: Selector[CanFill, T],
        f: FieldsExtractor.Aux[Schema, C, G, S],
        l: Length.Aux[S, NC],
        i: Fill.Aux[NC, T, S]
      ): TypedDataFrame[Schema] =
        new TypedDataFrame(dfn.fill(f(columns).map(_ -> value).toMap))
  }

  type CanReplace = Double :: String :: HNil

  def replaceAll[T, G <: HList]
    (replacement: Map[T, T])
    (implicit
      r: Selector[CanReplace, T],
      g: Generic.Aux[Schema, G],
      s: Selector[G, T]
    ): TypedDataFrame[Schema] =
      new TypedDataFrame(dfn.replace("*", replacement))

  def replace[T](replacement: Map[T, T]) = new ReplaceCurried[T](replacement)

  class ReplaceCurried[T](replacement: Map[T, T]) extends SingletonProductArgs {
    def applyProduct[C <: HList, G <: HList, S <: HList, NC <: Nat]
      (columns: C)
      (implicit
        h: IsHCons[C],
        s: Selector[CanReplace, T],
        f: FieldsExtractor.Aux[Schema, C, G, S],
        l: Length.Aux[S, NC],
        i: Fill.Aux[NC, T, S]
      ): TypedDataFrame[Schema] =
        new TypedDataFrame(dfn.replace(f(columns), replacement))
  }

}
