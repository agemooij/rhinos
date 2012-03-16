package com.scalapeno.rhinos

import org.mozilla.javascript._
import cc.spray.json._

import scala.collection.immutable.ListMap
import scala.collection.JavaConversions._
import scala.util.control.Exception._


trait RhinosJsonSupport {
  def toScala[T : JsonReader](input: Any): Option[T] = {
    toJsValueOption(input).flatMap { jsValue =>
      catching(classOf[DeserializationException]) opt {
        jsValue.convertTo[T]
      }
    }
  }

  private def toJsValueOption(input: Any): Option[JsValue] = toJsValue(input) match {
    case value if value == JsNull => None
    case jsValue @ _ => Some(jsValue)
  }

  private def toJsValue(input: Any): JsValue = input match {
    case b:Boolean => JsBoolean(b)
    case i:Int => JsNumber(i)
    case l:Long => JsNumber(l)
    case f:Float => JsNumber(f)
    case d:Double => JsNumber(d)
    case s:String => JsString(s)

    case o:NativeObject => toJsObject(o)
    case a:NativeArray => toJsArray(a)

    case u:Undefined => JsNull
    case null => JsNull
    case other @ _ => {
      log.warn("Cannot convert '%s' to a JsValue. Returning None.".format(other))

      JsNull
    }
  }

  private def toJsObject(nativeObject: NativeObject): JsObject = {
    val tuples = nativeObject.entrySet.toList.map(entry => (entry.getKey.toString, toJsValue(entry.getValue)))

    new JsObject(ListMap(tuples: _*))
  }

  private def toJsArray(nativeArray: NativeArray): JsArray = {
    new JsArray(nativeArray.iterator().map(item => toJsValue(item)).toList)
  }
}