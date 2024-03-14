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
      if (bomlength <= data.length && equals(data, 0, bomlength, bom, 0, bomlength)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }

  public static Optional<ByteOrderMark> detect(byte[] data, int datalen) {
    for (ByteOrderMark value : values()) {
      byte[] bom = value.getBytes();
      int bomlen = value.getBytes().length;
      if (bomlen <= datalen && equals(data, 0, bomlen, bom, 0, bomlen)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }

  /**
   * Checks that {@code fromIndex} and {@code toIndex} are in the range and throws an exception if
   * they aren't.
   */
  private static void rangeCheck(int arrayLength, int fromIndex, int toIndex) {
    if (fromIndex > toIndex) {
      throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
    }
    if (fromIndex < 0) {
      throw new ArrayIndexOutOfBoundsException(fromIndex);
    }
    if (toIndex > arrayLength) {
      throw new ArrayIndexOutOfBoundsException(toIndex);
    }
  }

  /**
   * Returns true if the two specified arrays of bytes, over the specified ranges, are <i>equal</i>
   * to one another.
   *
   * <p>
   * Two arrays are considered equal if the number of elements covered by each range is the same,
   * and all corresponding pairs of elements over the specified ranges in the two arrays are equal.
   * In other words, two arrays are equal if they contain, over the specified ranges, the same
   * elements in the same order.
   *
   * @param a the first array to be tested for equality
   * @param aFromIndex the index (inclusive) of the first element in the first array to be tested
   * @param aToIndex the index (exclusive) of the last element in the first array to be tested
   * @param b the second array to be tested for equality
   * @param bFromIndex the index (inclusive) of the first element in the second array to be tested
   * @param bToIndex the index (exclusive) of the last element in the second array to be tested
   * @return {@code true} if the two arrays, over the specified ranges, are equal
   * @throws IllegalArgumentException if {@code aFromIndex > aToIndex} or if
   *         {@code bFromIndex > bToIndex}
   * @throws ArrayIndexOutOfBoundsException if {@code aFromIndex < 0 or aToIndex > a.length} or if
   *         {@code bFromIndex < 0 or bToIndex > b.length}
   * @throws NullPointerException if either array is {@code null}
   */
  private static boolean equals(byte[] a, int aFromIndex, int aToIndex, byte[] b, int bFromIndex,
      int bToIndex) {
    rangeCheck(a.length, aFromIndex, aToIndex);
    rangeCheck(b.length, bFromIndex, bToIndex);

    int aLength = aToIndex - aFromIndex;
    int bLength = bToIndex - bFromIndex;
    if (aLength != bLength)
      return false;
    int length = aLength;

    for (int i = 0; i < length; i++) {
      if (a[aFromIndex + i] != b[bFromIndex + i]) {
        return false;
      }
    }

    return true;
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
