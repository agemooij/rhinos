package com.scalapeno.rhinos

import scala.util.control.Exception._

import java.io._

import org.slf4j.LoggerFactory
import org.mozilla.javascript._
import cc.spray.json._


trait RhinosEvaluationSupport { self: RhinosJsonSupport =>
  val scope: RhinosScope
  
  def eval[T : JsonReader](javascriptCode: String): Option[T] = {
    withContext[Any] { context =>
      context.evaluateString(scope, javascriptCode, "RhinoContext.eval()", 1, null)
    }.flatMap(value => toScala[T](value))
  }
  
  def evalFile[T : JsonReader](path: String): Option[T] = evalFile(new File(path))
  def evalFile[T : JsonReader](file: File): Option[T] = {
    if (file != null && file.exists) {
      evalReader(new FileReader(file))
    } else {
      log.warn("Could not evaluate Javascript file %s because it does not exist.".format(file))

      None
    }
  }

  def evalFileOnClasspath[T : JsonReader](path: String): Option[T] = {
    val in = this.getClass.getClassLoader.getResourceAsStream(path)

    if (in != null) {
      evalReader(new BufferedReader(new InputStreamReader(in)))
    } else {
      log.warn("Could not evaluate Javascript file %s because it does not exist on the classpath.".format(path))

      None
    }
  }

  // ==========================================================================
  // Implementation Details
  // ==========================================================================

  private[rhinos] def evalReader[T : JsonReader](reader: Reader): Option[T] = {
    using(reader) { r =>
      withContext[Any] { context =>
        context.evaluateReader(scope, r, "RhinoContext.eval(Reader)", 1, null)
      }
    }.flatMap(value => toScala[T](value))
  }
  
  private[rhinos] def using[X <: {def close()}, A](resource : X)(f : X => A) = {
     try {
       f(resource)
     } finally {
       ignoring(classOf[Exception]) {
         resource.close()
       }
     }
  }
}