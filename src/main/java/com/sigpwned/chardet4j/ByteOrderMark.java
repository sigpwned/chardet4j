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

import static java.util.Objects.requireNonNull;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.charset.spi.CharsetProvider;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

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
  UTF_8(new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}, StandardCharsets.UTF_8, "UTF-8"),

  /**
   * The BOM for a UTF-16 big endian stream
   */
  UTF_16BE(new byte[] {(byte) 0xFE, (byte) 0xFF}, StandardCharsets.UTF_16BE, "UTF-16BE"),

  /**
   * The BOM for a UTF-16 little endian stream
   */
  UTF_16LE(new byte[] {(byte) 0xFF, (byte) 0xFE}, StandardCharsets.UTF_16LE, "UTF-16LE"),

  /**
   * The BOM for a UTF-32 big endian stream
   */
  UTF_32BE(new byte[] {(byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF}, null, "UTF-32BE"),

  /**
   * The BOM for a UTF-32 little endian stream
   */
  UTF_32LE(new byte[] {(byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x00}, null, "UTF-32LE"),

  /**
   * The BOM for a UTF-7 stream
   */
  UTF_7(new byte[] {(byte) 0x2B, (byte) 0x2F, (byte) 0x76, (byte) 0x38}, null, "UTF-7"),

  /**
   * The BOM for a UTF-1 stream
   */
  UTF_1(new byte[] {(byte) 0xF7, (byte) 0x64, (byte) 0x4C}, null, "UTF-1"),

  /**
   * The BOM for a UTF-EBCDIC
   */
  UTF_EBCDIC(new byte[] {(byte) 0xDD, (byte) 0x73, (byte) 0x66, (byte) 0x73}, null, "UTF-EBCDIC"),

  /**
   * The BOM for a SCSU stream
   */
  SCSU(new byte[] {(byte) 0x0E, (byte) 0xFE, (byte) 0xFF}, null, "SCSU"),

  /**
   * The BOM for a BOCU-1 stream
   */
  BOCU_1(new byte[] {(byte) 0xFB, (byte) 0xEE, (byte) 0x28}, null, "BOCU-1"),

  /**
   * The BOM for a GB-18030 stream
   */
  GB_18030(new byte[] {(byte) 0x84, (byte) 0x31, (byte) 0x95, (byte) 0x33}, null, "GB-18030");

  public static final int MAX_BYTE_LENGTH =
      Arrays.stream(values()).mapToInt(bom -> bom.getBytes().length).max().getAsInt();

  /**
   * The values of the enum, sorted by the length of the BOM bytes, with the longest BOMs first.
   */
  private static final ByteOrderMark[] VALUES = Arrays.copyOf(values(), values().length);
  static {
    Arrays.sort(VALUES, Comparator.<ByteOrderMark>comparingInt(bom -> bom.getBytes().length)
        .reversed().thenComparing(ByteOrderMark::getCharsetName));
  }

  /**
   * Returns the BOM for the given data, if it is supported. Searches the whole array.
   * 
   * @param data the data to check
   * @return the BOM, if found, otherwise empty
   * 
   * @throws NullPointerException if {@code data} is {@code null}
   * 
   * @see #detect(byte[], int)
   */
  public static Optional<ByteOrderMark> detect(byte[] data) {
    if(data == null)
      throw new NullPointerException();
    return detect(data, data.length);
  }

  /**
   * Detects the BOM in the given data, starting at 0, up to the given length.
   * 
   * @param data the data to check
   * @param len the length of the data to check
   * @return the BOM, if found, otherwise empty
   * 
   * @throws NullPointerException if {@code data} is {@code null}
   * @throws IllegalArgumentException if {@code len < 0}
   * 
   * @see #detect(byte[], int, int)
   */
  public static Optional<ByteOrderMark> detect(byte[] data, int len) {
    return detect(data, 0, len);
  }

  /**
   * Detects the BOM in the given data, starting at the given offset and continuing for the given
   * length.
   * 
   * @param data the data to check
   * @param off the offset in the data to start checking
   * @param len the length of the data to check
   * @return the BOM, if found, otherwise empty
   *
   * @throws NullPointerException if {@code data} is {@code null}
   * @throws IllegalArgumentException if {@code len < 0}
   * @throws ArrayIndexOutOfBoundsException if {@code off < 0} or {@code off + len > data.length}
   */
  public static Optional<ByteOrderMark> detect(byte[] data, int off, int len) {
    if (data == null)
      throw new NullPointerException();
    if (len < 0)
      throw new IllegalArgumentException("len < 0");
    if (off < 0)
      throw new ArrayIndexOutOfBoundsException(off);
    if (off + len > data.length)
      throw new ArrayIndexOutOfBoundsException(off + len);

    for (ByteOrderMark value : VALUES) {
      byte[] bom = value.getBytes();
      int bomlen = value.getBytes().length;
      if (off + bomlen <= len && equals(data, off, off + bomlen, bom, 0, bomlen)) {
        return Optional.of(value);
      }
    }

    return Optional.empty();
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

  private final byte[] bytes;
  private final Charset standardCharset;
  private final String charsetName;
  private volatile AtomicReference<Charset> charset;

  private ByteOrderMark(byte[] bytes, Charset standardCharset, String charsetName) {
    this.bytes = requireNonNull(bytes);
    this.standardCharset = standardCharset;
    this.charsetName = requireNonNull(charsetName);
    if (standardCharset != null)
      this.charset = new AtomicReference<>(standardCharset);
  }

  /**
   * @return the bytes
   */
  public byte[] getBytes() {
    return bytes;
  }

  /**
   * Returns the charset for this BOM. Checks for standard charsets first, then attempts to load the
   * charset using {@link Charset#forName(String)}. If the charset is not supported, then throws an
   * {@link UnsupportedCharsetException}.
   * 
   * @return the charset
   * @throws UnsupportedCharsetException if the charset is not supported, e.g., UTF-32BE
   * 
   * @see #getCharsetIfSupported()
   * @see CharsetProvider
   */
  public Charset getCharset() {
    return getCharsetIfSupported().orElseThrow(() -> new UnsupportedCharsetException(charsetName));
  }

  /**
   * Returns the charset for this BOM. Checks for standard charsets first, then attempts to load the
   * charset using {@link Charset#forName}. If the charset is not supported, then returns empty.
   * 
   * @return the charset, if supported, otherwise empty
   * @see #getCharset()
   */
  public Optional<Charset> getCharsetIfSupported() {
    // If it's a standard charset, return it
    if (standardCharset != null)
      return Optional.of(standardCharset);

    // If it's not a standard charset, then attempt to load it and cache the result.
    if (charset == null) {
      Charset c;
      try {
        c = Charset.forName(charsetName);
      } catch (IllegalCharsetNameException e) {
        // Odd. None of these charset names should be invalid. Just treat it like it's not supported
        // and set the cached charset to null.
        c = null;
      } catch (UnsupportedCharsetException e) {
        // If the charset is not supported, then set the cached charset to null.
        c = null;
      }
      charset = new AtomicReference<>(c);
    }

    // If the cached charset is null, then return empty. Otherwise, return.
    return Optional.ofNullable(charset.get());
  }

  /**
   * @return the charset name
   */
  public String getCharsetName() {
    return charsetName;
  }
}
