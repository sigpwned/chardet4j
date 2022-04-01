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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

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
}
