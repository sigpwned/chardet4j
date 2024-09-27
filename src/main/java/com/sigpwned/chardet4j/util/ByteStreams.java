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
import java.io.InputStream;
import java.util.Arrays;

/**
 * Utility methods for working with {@link InputStream byte streams}.
 */
public final class ByteStreams {
  private ByteStreams() {}

  /**
   * Read as many bytes as possible from the the given {@link InputStream}, up to count, and return
   * them as a byte array. If the stream ends before count bytes can be read, then the returned
   * array will be shorter than count. Equivalent to the Java 9+ {@code InputStream} method of the
   * same name.
   * 
   * @param in the input stream
   * @param count the maximum number of bytes to read
   * @return the bytes read
   * @throws NullPointerException if in is null
   * @throws IllegalArgumentException if count is negative
   * @throws IOException if an I/O error occurs
   */
  public static byte[] readNBytes(InputStream in, int count) throws IOException {
    if (in == null)
      throw new NullPointerException();
    if (count < 0)
      throw new IllegalArgumentException("count must not be negative");

    final byte[] buf = new byte[count];
    if (count == 0)
      return buf;

    int len = 0;
    for (int nread = in.read(buf); nread != -1; nread = in.read(buf, len, buf.length - len)) {
      len = len + nread;
      if (len == buf.length)
        break;
    }

    if (len == buf.length)
      return buf;

    return Arrays.copyOf(buf, len);
  }
}
