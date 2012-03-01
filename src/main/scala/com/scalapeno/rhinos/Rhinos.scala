package com.scalapeno.rhinos

import scala.collection.immutable.ListMap
import scala.collection.JavaConversions._
import scala.util.control.Exception._

import java.io._

import org.mozilla.javascript._
import cc.spray.json._


object Rhinos {
  def rhino[T : JsonReader](block: RhinoContext[T] => Option[T]): Option[T] = {
    val rhinoContext = new RhinoContext[T]()
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

class RhinoContext[T : JsonReader] {
  val context = Context.enter()
  val scope = context.initStandardObjects()
  
  def eval(javascriptCode: String): Option[T] = {
    val result = context.evaluateString(scope, javascriptCode, "RhinoContext.eval()", 1, null)
    val convertor = jsonReader[T]
    
    toJsValueOption(result).flatMap { jsValue =>
      catching(classOf[DeserializationException]) opt {
        jsValue.convertTo[T]
      }
    }
  }
  
  def loadFromClasspath(path: String): Option[T] = {
    val in = this.getClass.getClassLoader.getResourceAsStream(path)
    
    if (in != null) {
      load(new BufferedReader(new InputStreamReader(in)))
    } else {
      // TODO: log a warning
      
      None
    }
  }
  
  def loadFromFile(path: String): Option[T] = loadFromFile(new File(path))
  def loadFromFile(file: File): Option[T] = {
    if (file != null && file.exists) {
      load(new FileReader(file))
    } else {
      // TODO: log a warning

      None
    }
  }
  
  def close {
    Context.exit()
  }
  
  
  // ==========================================================================
  // Implementation Details
  // ==========================================================================

  private def load(reader: Reader): Option[T] = {
    using(reader) { r =>
      context.evaluateReader(scope, r, "RhinoContext.loadFromClasspath()", 1, null)
    }
    
    None
  }
  
  private def using[X <: {def close()}, A](resource : X)(f : X => A) = {
     try {
       f(resource)
     } finally {
       resource.close()
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
      // TODO: replace with one of the many Scala Logger abstractions!
      println("Error: cannot convert '" + other + "' to a JsValue. Returning None.")
      
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