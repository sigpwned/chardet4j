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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import com.google.common.io.CharStreams;

public class ChardetTest {
  @Test
  public void iso8859Test() {
    Charset charset =
        Chardet.detectCharset("Hello, world!".getBytes(StandardCharsets.ISO_8859_1)).get();

    assertThat(charset, is(StandardCharsets.ISO_8859_1));
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
}
