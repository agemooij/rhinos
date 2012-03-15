package com.scalapeno

import scala.collection.immutable.ListMap
import scala.collection.JavaConversions._
import scala.util.control.Exception._

import java.io._

import org.slf4j.LoggerFactory
import org.mozilla.javascript._
import cc.spray.json._


package object rhinos {
  val log = LoggerFactory.getLogger(this.getClass)
  
  def rhino[T : JsonReader](block: RhinoContext[T] => Option[T]): Option[T] = {
    val rhinoContext = new RhinoContext[T]()
    
    try {
      block(rhinoContext)
    } catch {
      case jse: EvaluatorException => {
        log.error("Could not evaluate Javascript code: " + jse.getMessage)
        None
      }
      case e: Exception => {
        log.error("Rhinos ran into a problem while evaluating Javascript.", e)
        None
      }
    } finally {
      rhinoContext.close
    }
  }
  
  class RhinoContext[T : JsonReader] {
    val context = Context.enter()
    val scope = context.initStandardObjects()

    def eval(javascriptCode: String): Option[T] = {
      val result = context.evaluateString(scope, javascriptCode, "RhinoContext.eval()", 1, null)

      toScala(result)
    }

    def evalFile(path: String): Option[T] = evalFile(new File(path))
    def evalFile(file: File): Option[T] = {
      if (file != null && file.exists) {
        eval(new FileReader(file))
      } else {
        log.warn("Could not evaluate Javascript file %s because it does not exist.".format(file))

        None
      }
    }

    def evalFileOnClasspath(path: String): Option[T] = {
      val in = this.getClass.getClassLoader.getResourceAsStream(path)

      if (in != null) {
        eval(new BufferedReader(new InputStreamReader(in)))
      } else {
        log.warn("Could not evaluate Javascript file %s because it does not exist on the classpath.".format(path))

        None
      }
    }

    def close {
      Context.exit()
    }

    // ==========================================================================
    // Implementation Details
    // ==========================================================================

    private def eval(reader: Reader): Option[T] = {
      val result = using(reader) { r =>
        context.evaluateReader(scope, r, "RhinoContext.eval(Reader)", 1, null)
      }

      toScala(result)
    }

    private def using[X <: {def close()}, A](resource : X)(f : X => A) = {
       try {
         f(resource)
       } finally {
         resource.close()
       }
    }

    private def toScala(input: Any): Option[T] = {
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
  
  implicit object JsObjectReader extends JsonReader[JsObject] {
    def read(value: JsValue) = value match {
      case o: JsObject => o
      case x => deserializationError("Expected JsObject, but got " + x)
    }
  }
  
  implicit object JsArrayReader extends JsonReader[JsArray] {
    def read(value: JsValue) = value match {
      case o: JsArray => o
      case x => deserializationError("Expected JsArray, but got " + x)
    }
  }
}
