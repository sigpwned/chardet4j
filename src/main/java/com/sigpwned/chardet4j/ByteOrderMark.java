/*-
 * =================================LICENSE_START==================================
 * chardet4j
 * ====================================SECTION=====================================
 * Copyright (C) 2022 Andy Boothe
 * ====================================SECTION=====================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ==================================LICENSE_END===================================
 */
package com.sigpwned.chardet4j;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

/**
 * A byte order mark (BOM) that hard-codes charset into an input stream. At this time, this
 * implementation only supports BOMs for the character sets the JVM supports, namely UTF-8,
 * UTF-16LE, and UTF-16BE.
 * 
 * @see <a href=
 *      "https://en.wikipedia.org/wiki/Byte_order_mark">https://en.wikipedia.org/wiki/Byte_order_mark</a>
 */
public enum ByteOrderMark {
  /**
   * The BOM for a UTF-8 stream
   */
  UTF_8(new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}, StandardCharsets.UTF_8),

  /**
   * The BOM for a UTF-16 big endian stream
   */
  UTF_16BE(new byte[] {(byte) 0xFE, (byte) 0xFF}, StandardCharsets.UTF_16BE),

  /**
   * The BOM for a UTF-16 little endian stream
   */
  UTF_16LE(new byte[] {(byte) 0xFF, (byte) 0xFE}, StandardCharsets.UTF_16LE);

  public static final int MAX_BYTE_LENGTH =
      Arrays.stream(values()).mapToInt(bom -> bom.getBytes().length).max().getAsInt();

  /**
   * Returns
   */
  public static Optional<ByteOrderMark> detect(byte[] data) {
    for (ByteOrderMark value : values()) {
      byte[] bom = value.getBytes();
      int bomlength = value.getBytes().length;
      if (bomlength <= data.length && Arrays.equals(data, 0, bomlength, bom, 0, bomlength)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }

  public static Optional<ByteOrderMark> detect(byte[] data, int datalen) {
    for (ByteOrderMark value : values()) {
      byte[] bom = value.getBytes();
      int bomlen = value.getBytes().length;
      if (bomlen <= datalen && Arrays.equals(data, 0, bomlen, bom, 0, bomlen)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }

  private final byte[] bytes;
  private final Charset charset;

  private ByteOrderMark(byte[] bytes, Charset charset) {
    this.bytes = bytes;
    this.charset = charset;
  }

  /**
   * @return the bytes
   */
  public byte[] getBytes() {
    return bytes;
  }

  /**
   * @return the charset
   */
  public Charset getCharset() {
    return charset;
  }
}
