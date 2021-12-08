package com.ayushworks.shorturl

import java.nio.{ByteBuffer, CharBuffer}
import java.nio.charset.{CharacterCodingException, Charset, StandardCharsets}
import java.security.MessageDigest
import java.util.Base64

package object util {
  
  def urlSafeEncode64(input: String): String =
    Base64.getUrlEncoder.encodeToString(input.getBytes(StandardCharsets.UTF_8))

  def isValidUTF8(input: Array[Byte]): Option[CharBuffer] = {
    val cs = Charset.forName("UTF-8").newDecoder
    try {
      Some(cs.decode(ByteBuffer.wrap(input)))
    } catch {
      case _: CharacterCodingException => None
    }
  }

  def md5(str: String): Array[Byte] = MessageDigest.getInstance("MD5").digest(str.getBytes)

  def base64(str: String): String = Base64.getEncoder.encodeToString(str.getBytes(StandardCharsets.UTF_8))
}
