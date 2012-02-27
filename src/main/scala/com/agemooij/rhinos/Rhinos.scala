package com.agemooij.rhinos

import scala.collection.immutable.ListMap
import scala.collection.JavaConversions._

import org.mozilla.javascript._
import cc.spray.json._




object Rhinos {

  def rhino(block: RhinoContext => Option[JsValue]): Option[JsValue] = {
    val rhinoContext = new RhinoContext()
    val result = try {
      block(rhinoContext)
    } catch {
      case e: Exception => {
        e.printStackTrace
        None
      }
    } finally {
      rhinoContext.close
    }
    
    result
  }
}

class RhinoContext() {
  val context = Context.enter()
  val scope = context.initStandardObjects()
  
  def close {
    Context.exit()
  }
  
  def eval(javascriptCode: String): Option[JsValue] = {
    val result = context.evaluateString(scope, javascriptCode, "<js>", 1, null)
    
    result match {
      case u: Undefined => None
      case value @ _ => Some(toJsValue(value))
    }
  }
  
  
  // ==========================================================================
  // Conversions
  // ==========================================================================
  
  private[rhinos] def toJsValue(input: Any): JsValue = input match {
    case b:Boolean => JsBoolean(b)
    case i:Int => JsNumber(i)
    case l:Long => JsNumber(l)
    case f:Float => JsNumber(f)
    case d:Double => JsNumber(d)
    case s:String => JsString(s)
    case null => JsNull

    case o:NativeObject => toJsObject(o)
    case a:NativeArray => toJsArray(a)

    case other @ _ => JsString("unknown: " + other.toString)
  }

  private[rhinos] def toJsObject(nativeObject: NativeObject): JsObject = {
    val tuples = nativeObject.entrySet.toList.map(entry => (entry.getKey.toString, toJsValue(entry.getValue)))

    new JsObject(ListMap(tuples: _*))
  }

  private[rhinos] def toJsArray(nativeArray: NativeArray): JsArray = {
    new JsArray(nativeArray.iterator().map(item => toJsValue(item)).toList)
  }
}