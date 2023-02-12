package tailcall.gateway.internal

import zio.schema.{DynamicValue, StandardType}

object DynamicValueExtension {
  implicit final class DynamicValueExtension(dv: DynamicValue) {
    def asString: Option[String] =
      dv match {
        case DynamicValue.Primitive(value, tag) if tag == StandardType.StringType =>
          Some(value.asInstanceOf[String])
        case _                                                                    => None
      }

    def asPrimitive: Option[DynamicValue.Primitive[_]] =
      dv match {
        case primitive: DynamicValue.Primitive[_] => Some(primitive)
        case _                                    => None
      }

    def getPath(path: List[String], withIndex: Boolean = false): Option[DynamicValue] = {
      path match {
        case Nil          => Some(dv)
        case head :: tail => dv match {
            case DynamicValue.Record(_, record) => record.get(head).flatMap(_.getPath(tail))
            case DynamicValue.Sequence(array)   =>
              if (withIndex) head.toIntOption.flatMap(array.lift).flatMap(_.getPath(tail))
              else Option(DynamicValue(array.flatMap(_.getPath(tail))))
            case DynamicValue.Dictionary(chunk) => chunk
                .find(_._1.asString.map(_ == head).getOrElse(false))
                .map(_._2)
                .flatMap(_.getPath(tail))
            case _                              => None
          }
      }
    }
  }
}