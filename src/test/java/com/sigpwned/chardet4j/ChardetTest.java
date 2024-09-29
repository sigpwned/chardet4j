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

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import com.google.common.io.CharStreams;
import com.google.common.io.Resources;
import com.sigpwned.chardet4j.io.DecodedInputStreamReader;

public class ChardetTest {
  @Test
  public void iso8859Test() {
    Charset charset =
        Chardet.detectCharset("Hello, world!".getBytes(StandardCharsets.ISO_8859_1)).get();

    assertThat(charset, is(StandardCharsets.ISO_8859_1));
  }

  @Test
  public void iso8859Utf8Test() {
    Charset charset =
        Chardet.detectCharset("Hello, world!".getBytes(StandardCharsets.UTF_8), "utf-8").get();

    assertThat(charset, is(StandardCharsets.UTF_8));
  }

  @Test
  public void utf8Test() {
    Charset charset = Chardet.detectCharset("Hellö, world!".getBytes(StandardCharsets.UTF_8)).get();

    assertThat(charset, is(StandardCharsets.UTF_8));
  }

  @Test
  public void utf16BeTest() {
    Charset charset =
        Chardet.detectCharset("Hellö, world!".getBytes(StandardCharsets.UTF_16BE)).get();

    assertThat(charset, is(StandardCharsets.UTF_16BE));
  }

  @Test
  public void utf16LeTest() {
    Charset charset =
        Chardet.detectCharset("Hellö, world!".getBytes(StandardCharsets.UTF_16LE)).get();

    assertThat(charset, is(StandardCharsets.UTF_16LE));
  }

  @Test
  public void utf8BomTest() throws IOException {
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    buf.write(ByteOrderMark.UTF_8.getBytes());
    buf.write("Hello, world!".getBytes(StandardCharsets.UTF_8));

    Charset charset = Chardet.detectCharset(buf.toByteArray()).get();

    assertThat(charset, is(StandardCharsets.UTF_8));
  }

  @Test
  public void utf16BeBomTest() throws IOException {
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    buf.write(ByteOrderMark.UTF_16BE.getBytes());
    buf.write("Hello, world!".getBytes(StandardCharsets.UTF_16BE));

    Charset charset = Chardet.detectCharset(buf.toByteArray()).get();

    assertThat(charset, is(StandardCharsets.UTF_16BE));
  }

  @Test
  public void utf16LeBomTest() throws IOException {
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    buf.write(ByteOrderMark.UTF_16LE.getBytes());
    buf.write("Hello, world!".getBytes(StandardCharsets.UTF_16LE));

    Charset charset = Chardet.detectCharset(buf.toByteArray()).get();

    assertThat(charset, is(StandardCharsets.UTF_16LE));
  }

  /**
   * We should detect the correct charset if the declared hint is wrong
   */
  @Test
  public void mismatchedDeclaredEncodingTest() {
    Charset charset =
        Chardet.detectCharset("Hellö, world!".getBytes(StandardCharsets.UTF_8), "UTF-16").get();

    assertThat(charset, is(StandardCharsets.UTF_8));
  }

  /**
   * We should detect the correct charset if the declared hint is not a valid charset
   */
  @Test
  public void invalidDeclaredEncodingTest() {
    Charset charset =
        Chardet.detectCharset("Hellö, world!".getBytes(StandardCharsets.UTF_8), "FOOBAR").get();

    assertThat(charset, is(StandardCharsets.UTF_8));
  }

  /**
   * We should ignore the BOM
   */
  @Test
  public void decodeStreamTest() throws IOException {
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    buf.write(ByteOrderMark.UTF_8.getBytes());
    buf.write("Hello, world!".getBytes(StandardCharsets.UTF_8));

    String decoded;
    try (Reader r = Chardet.decode(new ByteArrayInputStream(buf.toByteArray()), "utf-8",
        StandardCharsets.UTF_8)) {
      decoded = CharStreams.toString(r);
    }

    assertThat(decoded, is("Hello, world!"));
  }

  /**
   * We should ignore the BOM
   */
  @Test
  public void decodeArrayTest() throws IOException {
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    buf.write(ByteOrderMark.UTF_8.getBytes());
    buf.write("Hello, world!".getBytes(StandardCharsets.UTF_8));

    String decoded = Chardet.decode(buf.toByteArray(), "utf-8", StandardCharsets.UTF_8);

    assertThat(decoded, is("Hello, world!"));
  }

  /**
   * We should ignore the BOM
   */
  @Test
  public void longTest() throws IOException {
    byte[] data = Resources.toByteArray(Resources.getResource("webpage.html"));

    Charset charset = Chardet.detectCharset(data, "utf-8").get();

    assertThat(charset, is(StandardCharsets.UTF_8));
  }

  public static class TestableCharset {
    public final boolean standard;
    public final String charsetName;
    public final ByteOrderMark bom;

    public TestableCharset(boolean standard, String charsetName, ByteOrderMark bom) {
      this.standard = standard;
      this.charsetName = requireNonNull(charsetName);
      this.bom = requireNonNull(bom);
    }

    public Optional<Charset> getCharset() {
      try {
        return Optional.of(Charset.forName(charsetName));
      } catch (UnsupportedCharsetException e) {
        return Optional.empty();
      }
    }
  }

  public static byte[] concat(byte[] xs, byte[] ys) {
    byte[] zs = new byte[xs.length + ys.length];
    System.arraycopy(xs, 0, zs, 0, xs.length);
    System.arraycopy(ys, 0, zs, xs.length, ys.length);
    return zs;
  }

  /**
   * These are the charsets we'll test decoding with. We'll test decoding with/out a BOM.
   */
  public static final List<TestableCharset> DETECT_CHARSET_TEST_CHARSETS =
      asList(new TestableCharset(true, "UTF-16BE", ByteOrderMark.UTF_16BE),
          new TestableCharset(true, "UTF-16LE", ByteOrderMark.UTF_16LE),
          new TestableCharset(true, "UTF-8", ByteOrderMark.UTF_8),
          new TestableCharset(false, "UTF-32BE", ByteOrderMark.UTF_32BE),
          new TestableCharset(false, "UTF-32LE", ByteOrderMark.UTF_32LE),
          new TestableCharset(false, "UTF-1", ByteOrderMark.UTF_1),
          new TestableCharset(false, "UTF-EBCDIC", ByteOrderMark.UTF_EBCDIC));

  /**
   * Test a variety of charsets using a known text and detect them
   */
  @Test
  public void detectCharsetTest() throws IOException {
    // Stopping by Woods on a Snowy Evening, by Robert Frost
    // We'll encode this in various charsets and decode them
    // We use a text without diacritics to avoid any issues with encoding. We're not here to test
    // the correctness of charset implementations, only correct application of same.
    // Note: The poem is public domain.
    final String originalText = "Whose woods these are I think I know.   \n"
        + "His house is in the village though;   \n" + "He will not see me stopping here   \n"
        + "To watch his woods fill up with snow.   \n" + "\n"
        + "My little horse must think it queer   \n" + "To stop without a farmhouse near   \n"
        + "Between the woods and frozen lake   \n" + "The darkest evening of the year.   \n" + "\n"
        + "He gives his harness bells a shake   \n" + "To ask if there is some mistake.   \n"
        + "The only other sound’s the sweep   \n" + "Of easy wind and downy flake.   \n" + "\n"
        + "The woods are lovely, dark and deep,   \n" + "But I have promises to keep,   \n"
        + "And miles to go before I sleep,   \n" + "And miles to go before I sleep.";

    // These are all the charsets that Java is required to support
    for (TestableCharset testableCharset : DETECT_CHARSET_TEST_CHARSETS) {
      if (!testableCharset.getCharset().isPresent()) {
        if (testableCharset.standard)
          throw new AssertionError(
              "JVM does not support standard charset " + testableCharset.charsetName);
        continue;
      }

      final Charset charset = testableCharset.getCharset().get();


      // Make sure we get the right charset when we decode WITHOUT a BOM
      final byte[] plainEncodedText = originalText.getBytes(charset);
      final Charset plainDetectedCharset = Chardet.detectCharset(plainEncodedText).get();
      if (testableCharset.charsetName.equals("UTF-8")) {
        // Over the wire, UTF-8 is indistinguishable from ISO-8859-1 for this text.
        assertThat(plainDetectedCharset,
            anyOf(is(StandardCharsets.ISO_8859_1), is(StandardCharsets.UTF_8)));
      } else {
        assertThat(plainDetectedCharset, is(charset));
      }

      // Make sure we get the right charset when we decode WITHOUT a BOM
      final byte[] bomEncodedText =
          concat(testableCharset.bom.getBytes(), originalText.getBytes(charset));
      final Charset bomDetectedCharset = Chardet.detectCharset(bomEncodedText).get();
      if (testableCharset.charsetName.equals("UTF-8")) {
        // Over the wire, UTF-8 is indistinguishable from ISO-8859-1 for this text.
        assertThat(bomDetectedCharset,
            anyOf(is(StandardCharsets.ISO_8859_1), is(StandardCharsets.UTF_8)));
      } else {
        assertThat(bomDetectedCharset, is(charset));
      }
    }
  }

  /**
   * These are the charsets we'll test decoding with. We'll test decoding with/out a BOM.
   */
  public static final List<TestableCharset> DECODE_TEST_CHARSETS =
      asList(new TestableCharset(true, "UTF-16BE", ByteOrderMark.UTF_16BE),
          new TestableCharset(true, "UTF-16LE", ByteOrderMark.UTF_16LE),
          new TestableCharset(true, "UTF-8", ByteOrderMark.UTF_8),
          new TestableCharset(false, "UTF-32BE", ByteOrderMark.UTF_32BE),
          new TestableCharset(false, "UTF-32LE", ByteOrderMark.UTF_32LE),
          new TestableCharset(false, "UTF-1", ByteOrderMark.UTF_1),
          new TestableCharset(false, "UTF-EBCDIC", ByteOrderMark.UTF_EBCDIC));

  /**
   * Test the ability to decode an InputStream
   * 
   * @see Chardet#decode(byte[], Charset)
   */
  @Test
  public void decodeTest() throws IOException {
    // Stopping by Woods on a Snowy Evening, by Robert Frost
    // We'll encode this in various charsets and decode them
    // We use a text without diacritics to avoid any issues with encoding. We're not here to test
    // the correctness of charset implementations, only correct application of same.
    // Note: The poem is public domain.
    final String originalText = "Whose woods these are I think I know.   \n"
        + "His house is in the village though;   \n" + "He will not see me stopping here   \n"
        + "To watch his woods fill up with snow.   \n" + "\n"
        + "My little horse must think it queer   \n" + "To stop without a farmhouse near   \n"
        + "Between the woods and frozen lake   \n" + "The darkest evening of the year.   \n" + "\n"
        + "He gives his harness bells a shake   \n" + "To ask if there is some mistake.   \n"
        + "The only other sound’s the sweep   \n" + "Of easy wind and downy flake.   \n" + "\n"
        + "The woods are lovely, dark and deep,   \n" + "But I have promises to keep,   \n"
        + "And miles to go before I sleep,   \n" + "And miles to go before I sleep.";

    for (TestableCharset testableCharset : DECODE_TEST_CHARSETS) {
      if (!testableCharset.getCharset().isPresent()) {
        if (testableCharset.standard)
          throw new AssertionError(
              "JVM does not support standard charset " + testableCharset.charsetName);
        continue;
      }

      final Charset charset = testableCharset.getCharset().get();

      final byte[] encodedText = originalText.getBytes(charset);

      // Make sure we get the right charset when we decode WITHOUT a BOM
      final StringWriter plainWriter = new StringWriter();
      try (DecodedInputStreamReader plainReader =
          Chardet.decode(new ByteArrayInputStream(encodedText), charset)) {
        final Charset detectedCharset = plainReader.charset();
        if (testableCharset.charsetName.equals("UTF-8")) {
          // Over the wire, UTF-8 is indistinguishable from ISO-8859-1 for this text.
          assertThat(detectedCharset,
              anyOf(is(StandardCharsets.ISO_8859_1), is(StandardCharsets.UTF_8)));
        } else {
          assertThat(detectedCharset, is(charset));
        }
        CharStreams.copy(plainReader, plainWriter);
      }
      assertThat(plainWriter.toString(), is(originalText));

      // Make sure we get the right charset when we decode WITH a BOM
      final StringWriter bomWriter = new StringWriter();
      try (DecodedInputStreamReader bomReader = Chardet
          .decode(new SequenceInputStream(new ByteArrayInputStream(testableCharset.bom.getBytes()),
              new ByteArrayInputStream(encodedText)), charset)) {
        final Charset detectedCharset = bomReader.charset();
        if (testableCharset.charsetName.equals("UTF-8")) {
          // Over the wire, UTF-8 is indistinguishable from ISO-8859-1 for this text.
          assertThat(detectedCharset,
              anyOf(is(StandardCharsets.ISO_8859_1), is(StandardCharsets.UTF_8)));
        } else {
          assertThat(detectedCharset, is(charset));
        }
        CharStreams.copy(bomReader, bomWriter);
      }
      assertThat(bomWriter.toString(), is(originalText));
    }
  }
}
