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

import static java.util.stream.Collectors.toList;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import com.sigpwned.chardet4j.com.ibm.icu.text.CharsetDetector;
import com.sigpwned.chardet4j.io.BomAwareInputStream;
import com.sigpwned.chardet4j.io.DecodedInputStreamReader;
import com.sigpwned.chardet4j.util.ByteStreams;
import com.sigpwned.chardet4j.util.CharStreams;

/**
 * Simple interface to charset detection.
 */
public final class Chardet {
  private Chardet() {}

  private static final int MIN_CONFIDENCE = 0;
  private static final int MAX_CONFIDENCE = 100;

  private static final int DECLARED_ENCODING_BUMP = Optional
      .ofNullable(System.getProperty("chardet4j.detect.bump")).map(Integer::parseInt).orElse(10);

  /**
   * We have to do this because the ICU detector ignores the declared encoding, but the CharsetMatch
   * values are immutable and the constructor isn't visible.
   */
  private static class ChardetMatch implements Comparable<ChardetMatch> {
    public static ChardetMatch of(String name, int confidence) {
      return new ChardetMatch(name, confidence);
    }

    private final String name;
    private final int confidence;

    public ChardetMatch(String name, int confidence) {
      if (name == null)
        throw new NullPointerException();
      if (confidence < MIN_CONFIDENCE || confidence > MAX_CONFIDENCE)
        throw new IllegalArgumentException("confidence out of range " + confidence);
      this.name = name;
      this.confidence = confidence;
    }

    /**
     * @return the name
     */
    public String getName() {
      return name;
    }

    /**
     * @return the confidence
     */
    public int getConfidence() {
      return confidence;
    }

    @Override
    public int hashCode() {
      return Objects.hash(confidence, name);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      ChardetMatch other = (ChardetMatch) obj;
      return confidence == other.confidence && Objects.equals(name, other.name);
    }

    @Override
    public String toString() {
      return "PossibleMatch [name=" + name + ", confidence=" + confidence + "]";
    }

    @Override
    public int compareTo(ChardetMatch o) {
      return Integer.compare(getConfidence(), o.getConfidence());
    }
  }

  // detectCharset /////////////////////////////////////////////////////////////////////////////////

  /**
   * Detect the charset of the given byte data. Input includes the entire array. If the character
   * encoding is detected, but not supported, then an {@link UnsupportedCharsetException} is thrown.
   * 
   * @throws NullPointerException if data is null
   * @throws UnsupportedOperationException If the charset can be detected, but is not supported.
   */
  public static Optional<Charset> detectCharset(byte[] data) {
    return detectCharset(data, null);
  }

  /**
   * Detect the charset of the given byte data with the given encoding as a hint. Input includes the
   * entire array. If the character encoding is detected, but not supported, then an
   * {@link UnsupportedCharsetException} is thrown.
   * 
   * @param data the byte data
   * @param declaredEncoding the declared encoding, treated as a hint
   * @return the charset, if one can be detected
   * 
   * @throws NullPointerException if data is null
   * @throws UnsupportedOperationException If the charset can be detected, but is not supported.
   */
  public static Optional<Charset> detectCharset(byte[] data, String declaredEncoding) {
    if (data == null)
      throw new NullPointerException();
    return detectCharset(data, data.length, declaredEncoding);
  }

  /**
   * Detect the charset encoding of the given byte data in the first len bytes of the given array.
   * If the character encoding is detected, but not supported, then an
   * {@link UnsupportedCharsetException} is thrown.
   * 
   * @param data the byte data
   * @param len the number of bytes to consider, starting from 0
   * @param declaredEncoding the optional declared encoding, which is treated as a hint
   * @return the charset encoding, if one can be detected
   * 
   * @throws NullPointerException if data is null
   * @throws IllegalArgumentException if len < 0
   * @throws ArrayIndexOutOfBoundsException if len > data.length
   * @throws UnsupportedOperationException If the charset can be detected, but is not supported.
   */
  public static Optional<Charset> detectCharset(byte[] data, int len, String declaredEncoding) {
    return detectCharset(data, 0, len, declaredEncoding);
  }

  /**
   * Detect the charset encoding of the given byte data in the given range of the given array. If
   * the character encoding is detected, but not supported, then an
   * {@link UnsupportedCharsetException} is thrown.
   * 
   * @param data the byte data
   * @param off the offset into the byte data
   * @param len the number of bytes to consider
   * @param declaredEncoding the optional declared encoding, which is treated as a hint
   * @return the charset encoding, if one can be detected
   * 
   * @throws NullPointerException if data is null
   * @throws IllegalArgumentException if len < 0
   * @throws ArrayIndexOutOfBoundsException if off < 0 or off + len > data.length
   * @throws UnsupportedOperationException If the charset can be detected, but is not supported. To
   *         get the charset name whether it is supported or not, use
   *         {@link #detectCharsetName(byte[], int, String)}.
   */
  public static Optional<Charset> detectCharset(byte[] data, int off, int len,
      String declaredEncoding) {
    return detectCharsetName(data, off, len, declaredEncoding).map(Charset::forName);
  }

  // detectCharsetName /////////////////////////////////////////////////////////////////////////////

  /**
   * Detect the charset of the given byte data. Input includes the entire array.
   * 
   * @throws NullPointerException if data is null
   */
  public static Optional<String> detectCharsetName(byte[] data) {
    return detectCharsetName(data, null);
  }

  /**
   * Detect the charset of the given byte data. Input includes the entire array.
   * 
   * @param data the byte data
   * @param declaredEncoding the declared encoding, treated as a hint
   * @return the charset name, if one is detected
   * 
   * @throws NullPointerException if data is null
   */
  public static Optional<String> detectCharsetName(byte[] data, String declaredEncoding) {
    if (data == null)
      throw new NullPointerException();
    return detectCharsetName(data, data.length, declaredEncoding);
  }

  /**
   * Detect the name of the charset encoding of the given byte data in the first len bytes of the
   * given array.
   * 
   * @param data the byte data
   * @param len the number of bytes to consider, starting from 0
   * @param declaredEncoding the optional declared encoding, which is treated as a hint
   * @return the charset encoding, if one can be detected
   * 
   * @throws NullPointerException if data is null
   * @throws IllegalArgumentException if len < 0
   * @throws ArrayIndexOutOfBoundsException if len > data.length
   */
  public static Optional<String> detectCharsetName(byte[] data, int len, String declaredEncoding) {
    return detectCharsetName(data, 0, len, declaredEncoding);
  }

  /**
   * Detect the name of the charset encoding of the given range of the given array.
   * 
   * @param data the byte data
   * @param len the number of bytes to consider, starting from 0
   * @param declaredEncoding the optional declared encoding, which is treated as a hint
   * @return the charset encoding, if one can be detected
   * 
   * @throws NullPointerException if data is null
   * @throws IllegalArgumentException if len < 0
   * @throws ArrayIndexOutOfBoundsException if off < 0 or off + len > data.length
   * @throws UncheckedIOException if an I/O error occurs, which should not happen because all I/O
   *         operations are performed in-memory
   */
  public static Optional<String> detectCharsetName(byte[] data, int off, int len,
      String declaredEncoding) {
    if (data == null)
      throw new NullPointerException();
    if (len < 0)
      throw new IllegalArgumentException("len < 0");
    if (off < 0)
      throw new ArrayIndexOutOfBoundsException(off);
    if (off + len > data.length)
      throw new ArrayIndexOutOfBoundsException(off + len);

    Optional<ByteOrderMark> maybeBom = ByteOrderMark.detect(data, off, len);
    if (maybeBom.isPresent()) {
      return maybeBom.map(ByteOrderMark::getCharsetName);
    }

    CharsetDetector chardet = new CharsetDetector();

    if (off == 0 && len == data.length) {
      // Let's avoid a byte copy if we can
      chardet.setText(data);
    } else {
      try {
        chardet.setText(new ByteArrayInputStream(data, off, len));
      } catch (IOException e) {
        // This should never happen in a ByteArrayInputStream
        throw new UncheckedIOException("unexpected exception when reading from byte array", e);
      }
    }

    // Ideally, we'd just use this methods from the CharsetDetector class, but the declared encoding
    // is ignored. So we have to do it ourselves.
    // if (declaredEncoding != null)
    // chardet.setDeclaredEncoding(declaredEncoding);

    List<ChardetMatch> matches = Arrays.stream(chardet.detectAll()).map(mi -> {
      String name = mi.getName();

      int confidence = mi.getConfidence();
      if (declaredEncoding != null && name.equalsIgnoreCase(declaredEncoding))
        confidence = Math.min(confidence + DECLARED_ENCODING_BUMP, MAX_CONFIDENCE);

      return ChardetMatch.of(name, confidence);
    }).sorted(Comparator.reverseOrder()).collect(toList());

    if (matches.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(matches.get(0).getName());
  }

  // decode ////////////////////////////////////////////////////////////////////////////////////////

  /**
   * The default is chosen based on a reading of the CharsetDetector source code, which sets buffer
   * size for byte frequency analysis at 8000. (Ample) extra space is left for BOMs.
   */
  public static final int DECODE_DETECT_BUFSIZE =
      Optional.ofNullable(System.getProperty("chardet4j.detect.bufsize")).map(Integer::parseInt)
          .orElse(8192);

  /**
   * Returns a character-decoded version of the given byte stream. Any leading BOMs are discarded.
   * If no character set can be detected, then the given default is used.
   * 
   * @param input the input stream
   * @param defaultCharset the default charset to use if no other can be detected
   * 
   * @throws NullPointerException if input is null
   * @throws NullPointerException if defaultCharset is null
   * @throws IOException if an I/O error occurs
   * @throws UnsupportedCharsetException if the detected charset is not supported
   */
  public static DecodedInputStreamReader decode(InputStream input, Charset defaultCharset)
      throws IOException {
    return decode(input, null, defaultCharset);
  }

  /**
   * Returns a character-decoded version of the given byte stream. The declared encoding is treated
   * as a hint. Any leading BOMs are discarded. If no character set can be detected, then the given
   * default is used. If the character set is detected, but not supported, then an
   * {@link UnsupportedCharsetException} is thrown.
   * 
   * @param input the input stream
   * @param declaredEncoding the declared encoding, treated as a hint
   * @param defaultCharset the default charset to use if no other can be detected
   * @return the character-decoded stream
   * 
   * @throws NullPointerException if input is null
   * @throws NullPointerException if defaultCharset is null
   * @throws IOException if an I/O error occurs
   * @throws UnsupportedCharsetException if the detected charset is not supported
   */
  public static DecodedInputStreamReader decode(InputStream input, String declaredEncoding,
      Charset defaultCharset) throws IOException {
    if (input == null)
      throw new NullPointerException();
    if (defaultCharset == null)
      throw new NullPointerException();

    // Detect the BOM, if any. If there is one, then trust it and use the corresponding charset.
    final BomAwareInputStream bomed = BomAwareInputStream.detect(input);
    if (bomed.bom().isPresent())
      return new DecodedInputStreamReader(bomed, bomed.bom().get().getCharset());

    // If there is no BOM, then read some bytes to detect the charset.
    final byte[] buf = ByteStreams.readNBytes(bomed, DECODE_DETECT_BUFSIZE);

    // Note that charset cannot be null, since we check defaultCharset above.
    Charset charset = detectCharset(buf, declaredEncoding).orElse(defaultCharset);

    return new DecodedInputStreamReader(
        new SequenceInputStream(new ByteArrayInputStream(buf), bomed), charset);
  }

  /**
   * Returns a character-decoded String version of the given bytes. The declared encoding is treated
   * as a hint. Any leading BOMs are discarded. If no character set can be detected, then the given
   * default is used. If the character set is detected, but not supported, then an
   * {@link UnsupportedCharsetException} is thrown.
   * 
   * @param data the byte data
   * @param declaredEncoding the declared encoding, treated as a hint
   * @param defaultCharset the default charset to use if no other can be detected
   * @return the character-decoded string
   * 
   * @throws NullPointerException if data is null
   * @throws UnsupportedCharsetException if the detected charset is not supported
   * @throws UncheckedIOException if an I/O error occurs, which should not happen because all I/O
   *         operations are performed in-memory
   */
  public static String decode(byte[] data, Charset defaultCharset) {
    return decode(data, null, defaultCharset);
  }

  /**
   * Returns a character-decoded String version of the given bytes. The declared encoding is treated
   * as a hint. Any leading BOMs are discarded. If no character set can be detected, then the given
   * default is used. If the character set is detected, but not supported, then an
   * {@link UnsupportedCharsetException} is thrown.
   * 
   * @param data the byte data
   * @param declaredEncoding the declared encoding, treated as a hint
   * @param defaultCharset the default charset to use if no other can be detected
   * @return the character-decoded string
   * 
   * @throws NullPointerException if data is null
   * @throws UnsupportedCharsetException if the detected charset is not supported
   * @throws UncheckedIOException if an I/O error occurs, which should not happen because all I/O
   *         operations are performed in-memory
   */
  public static String decode(byte[] data, String declaredEncoding, Charset defaultCharset) {
    if (data == null)
      throw new NullPointerException();
    return decode(data, data.length, declaredEncoding, defaultCharset);
  }

  /**
   * Returns a character-decoded String version of the given bytes. The declared encoding is treated
   * as a hint. Any leading BOMs are discarded. If no character set can be detected, then the given
   * default is used. If the character set is detected, but not supported, then an
   * {@link UnsupportedCharsetException} is thrown.
   * 
   * @param data the byte data
   * @param len the number of bytes to consider, starting from 0
   * @param declaredEncoding the declared encoding, treated as a hint
   * @param defaultCharset the default charset to use if no other can be detected
   * @return the character-decoded string
   * 
   * @throws NullPointerException if data is null
   * @throws IllegalArgumentException if len < 0
   * @throws ArrayIndexOutOfBoundsException if len > data.length
   * @throws UnsupportedCharsetException if the detected charset is detected, but not supported
   * @throws UncheckedIOException if an I/O error occurs, which should not happen because all I/O
   *         operations are performed in-memory
   */
  public static String decode(byte[] data, int len, String declaredEncoding,
      Charset defaultCharset) {
    return decode(data, 0, len, declaredEncoding, defaultCharset);
  }

  /**
   * Returns a character-decoded String version of the given bytes. The declared encoding is treated
   * as a hint. Any leading BOMs are discarded. If no character set can be detected, then the given
   * default is used. If the character set is detected, but not supported, then an
   * {@link UnsupportedCharsetException} is thrown.
   * 
   * @param data the byte data
   * @param off the offset into the byte data
   * @param len the number of bytes to consider, starting at off
   * @param declaredEncoding the declared encoding, treated as a hint
   * @param defaultCharset the default charset to use if no other can be detected
   * @return the character-decoded string
   * 
   * @throws NullPointerException if data is null
   * @throws NullPointerException if defaultCharset is null
   * @throws IllegalArgumentException if len < 0
   * @throws ArrayIndexOutOfBoundsException if off < 0 or off + len > data.length
   * @throws UnsupportedCharsetException if the detected charset is detected, but not supported
   * @throws UncheckedIOException if an I/O error occurs, which should not happen because all I/O
   *         operations are performed in-memory
   */
  public static String decode(byte[] data, int off, int len, String declaredEncoding,
      Charset defaultCharset) {
    if (data == null)
      throw new NullPointerException();
    if (defaultCharset == null)
      throw new NullPointerException();
    if (len < 0)
      throw new IllegalArgumentException("len < 0");
    if (off < 0)
      throw new ArrayIndexOutOfBoundsException(off);
    if (off + len > data.length)
      throw new ArrayIndexOutOfBoundsException(off + len);

    try (InputStream in = new ByteArrayInputStream(data, off, len);
        Reader r = decode(in, declaredEncoding, defaultCharset);
        Writer w = new StringWriter()) {
      CharStreams.transferTo(r, w);
      return w.toString();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
