package uk.co.telegraph.utils.client.http.serialization

import java.lang.Exception
import java.lang.reflect.InvocationTargetException

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.util.ByteString
import com.fasterxml.jackson.core.io.JsonEOFException
import org.json4s.{Formats, MappingException, Serialization, jackson}

/**
  * Automatic to and from JSON marshalling/unmarshalling using an in-scope *Json4s* protocol.
  */
trait Json4sSupport {

  protected val serialization:Serialization

  private val jsonStringUnmarshaller =
    Unmarshaller
      .byteStringUnmarshaller
      .forContentTypes(`application/json`)
      .mapWithCharset {
        case (ByteString.empty, _) => throw Unmarshaller.NoContentException
        case (data, charset)       => data.decodeString(charset.nioCharset.name)
      }

  private val jsonStringMarshaller =
    Marshaller
      .stringMarshaller(`application/json`)

  /**
    * HTTP entity => `A`
    *
    * @tparam A type to decode
    * @return unmarshaller for `A`
    */
  implicit def unmarshaller[A: Manifest](implicit formats: Formats): FromEntityUnmarshaller[A] =
    jsonStringUnmarshaller.map( serialization.read[A] )
      .recover { _ => _ => {
        case ex:JsonEOFException => throw MappingException("Fail to parse JsonPayload", ex)
      }}

  /**
    * `A` => HTTP entity
    *
    * @tparam A type to encode, must be upper bounded by `AnyRef`
    * @return marshaller for any `A` value
    */
  implicit def marshaller[A <: AnyRef](implicit formats: Formats): ToEntityMarshaller[A] =
    jsonStringMarshaller.compose( serialization.write[A] )

}

object Json4sSupport extends Json4sSupport{
  protected val serialization = jackson.Serialization
}
