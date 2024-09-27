/*-
 * =================================LICENSE_START==================================
 * chardet4j
 * ====================================SECTION=====================================
 * Copyright (C) 2022 - 2024 Andy Boothe
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
package com.sigpwned.chardet4j.util;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public final class CharStreams {
  private CharStreams() {}

  /**
   * Copy all characters from the given {@link Reader} to the given {@link Writer} and return the
   * total number of characters copied. Equivalent to the Java 9+ {@code Reader} method of the same
   * name.
   *
   * @param in the input reader
   * @param out the output writer
   * @return the total number of characters copied
   * @throws NullPointerException if in or out is null
   * @throws IOException if an I/O error occurs
   */
  public static long transferTo(Reader in, Writer out) throws IOException {
    if (in == null)
      throw new NullPointerException();
    if (out == null)
      throw new NullPointerException();

    long total = 0;

    final char[] buf = new char[8192];
    for (int nread = in.read(buf); nread != -1; nread = in.read(buf)) {
      out.write(buf, 0, nread);
      total = total + nread;
    }

    return total;
  }
}
