package com.snapswap.siftscience.utils

import spray.json._

object JsonHelper {

  implicit class JsValueImporter(js: JsValue) {
    def /(name: String): Option[JsValue] =
      js.asJsObject.fields.get(name)
  }

  implicit class JsOptValueImporter(js: Option[JsValue]) {
    def /(name: String): Option[JsValue] =
      js.flatMap(_./(name))

    def convertTo[T :JsonReader]: Option[T] =
      js.map(jsonReader[T].read(_))
  }

}
