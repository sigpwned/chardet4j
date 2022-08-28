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
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import com.sigpwned.chardet4j.com.ibm.icu.text.CharsetDetector;
import com.sigpwned.chardet4j.com.ibm.icu.text.CharsetMatch;

/**
 * Simple interface to charset detection.
 */
public final class Chardet {
  private Chardet() {}

  /**
   * Detect the charset of the given byte data. Input includes the entire array.
   */
  public static Optional<Charset> detectCharset(byte[] data) {
    return detectCharset(data, null);
  }

  private static final int MIN_CONFIDENCE = 0;
  private static final int MAX_CONFIDENCE = 100;
  private static final int DECLARED_ENCODING_BUMP = 10;

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
      return getConfidence() - o.getConfidence();
    }
  }

  /**
   * Detect the charset of the given byte data with the given encoding as a hint. Input includes the
   * entire array.
   */
  public static Optional<Charset> detectCharset(byte[] data, String declaredEncoding) {
    Optional<ByteOrderMark> maybeBom = ByteOrderMark.detect(data);
    if (maybeBom.isPresent()) {
      return maybeBom.map(ByteOrderMark::getCharset);
    }

    CharsetDetector chardet = new CharsetDetector();

    chardet.setText(data);

    List<ChardetMatch> matches = Arrays.stream(chardet.detectAll()).map(mi -> {
      String name = mi.getName();
      int confidence = name.equalsIgnoreCase(declaredEncoding)
          ? Math.min(mi.getConfidence() + DECLARED_ENCODING_BUMP, MAX_CONFIDENCE)
          : mi.getConfidence();
      return ChardetMatch.of(name, confidence);
    }).sorted(Comparator.reverseOrder()).collect(toList());

    return matches.isEmpty() ? Optional.empty()
        : Optional.of(matches.get(0).getName()).map(Charset::forName);
  }

  /**
   * Detect the charset of the given byte data in the first datalen bytes with the given encoding as
   * a hint.
   */
  public static Optional<Charset> detectCharset(byte[] data, int datalen, String declaredEncoding) {
    Optional<ByteOrderMark> maybeBom = ByteOrderMark.detect(data, datalen);
    if (maybeBom.isPresent()) {
      return maybeBom.map(ByteOrderMark::getCharset);
    }

    CharsetDetector chardet = new CharsetDetector();

    if (datalen == data.length) {
      // Let's avoid a byte copy if we can
      chardet.setText(data);
    } else {
      try {
        chardet.setText(new ByteArrayInputStream(data, 0, datalen));
      } catch (IOException e) {
        // This should never happen in a ByteArrayInputStream
        throw new UncheckedIOException("unexpected exception when reading from byte array", e);
      }
    }

    if (declaredEncoding != null)
      chardet.setDeclaredEncoding(declaredEncoding);

    CharsetMatch match = chardet.detect();

    return Optional.ofNullable(match).map(m -> Charset.forName(m.getName()));
  }

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
   */
  public static Reader decode(InputStream input, Charset defaultCharset) throws IOException {
    return decode(input, null, defaultCharset);
  }

  /**
   * Returns a character-decoded version of the given byte stream. The declared encoding is treated
   * as a hint. Any leading BOMs are discarded. If no character set can be detected, then the given
   * default is used.
   */
  public static Reader decode(InputStream input, String declaredEncoding, Charset defaultCharset)
      throws IOException {
    int buflen = 0;
    byte[] buf = new byte[DECODE_DETECT_BUFSIZE];
    for (int nread = input.read(buf, buflen, buf.length - buflen); nread != -1; nread =
        input.read(buf, buflen, buf.length - buflen)) {
      buflen = buflen + nread;
      if (buflen == buf.length)
        break;
    }

    Charset charset = detectCharset(buf, buflen, declaredEncoding).orElse(defaultCharset);

    int offset;
    Optional<ByteOrderMark> maybeBom = ByteOrderMark.detect(buf, buflen);
    if (maybeBom.isPresent()) {
      offset = maybeBom.map(bom -> bom.getBytes().length).get();
    } else {
      offset = 0;
    }

    return new InputStreamReader(
        new SequenceInputStream(new ByteArrayInputStream(buf, offset, buflen - offset), input),
        charset);
  }

  /**
   * Returns a character-decoded String version of the given bytes. The declared encoding is treated
   * as a hint. Any leading BOMs are discarded. If no character set can be detected, then the given
   * default is used.
   */
  public static String decode(byte[] data, String declaredEncoding, Charset defaultCharset) {
    return decode(data, data.length, declaredEncoding, defaultCharset);
  }

  /**
   * Returns a character-decoded String version of the given bytes. The declared encoding is treated
   * as a hint. Any leading BOMs are discarded. If no character set can be detected, then the given
   * default is used.
   */
  public static String decode(byte[] data, int datalen, String declaredEncoding,
      Charset defaultCharset) {
    // We work directly with the byte array here as opposed to wrapping in an InputStream to avoid
    // extra copies. Performance matters.
    Charset charset = detectCharset(data, datalen, declaredEncoding).orElse(defaultCharset);

    int offset;
    Optional<ByteOrderMark> maybeBom = ByteOrderMark.detect(data, datalen);
    if (maybeBom.isPresent()) {
      offset = maybeBom.map(bom -> bom.getBytes().length).get().intValue();
    } else {
      offset = 0;
    }

    return new String(data, offset, datalen - offset, charset);
  }
}
