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
package com.sigpwned.chardet4j.io;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Optional;
import com.sigpwned.chardet4j.ByteOrderMark;
import com.sigpwned.chardet4j.util.ByteStreams;

/**
 * A wrapper {@link InputStream} that remembers the {@link ByteOrderMark} that was detected at the
 * beginning of the stream.
 */
public final class BomAwareInputStream extends FilterInputStream {
  /**
   * Detect the {@link ByteOrderMark} at the beginning of the stream, if any, and return a
   * {@link BomAwareInputStream} that wraps the stream.
   *
   * @param in the input stream
   * @return the {@link BomAwareInputStream}
   * @throws IOException if an I/O error occurs
   */
  public static BomAwareInputStream detect(InputStream in) throws IOException {
    final byte[] buf = ByteStreams.readNBytes(in, ByteOrderMark.MAX_BYTE_LENGTH);

    ByteOrderMark bom = ByteOrderMark.detect(buf).orElse(null);

    // If there is no BOM, then return all the bytes read so far, followed by the rest of the stream
    if (bom == null)
      return new BomAwareInputStream(new SequenceInputStream(new ByteArrayInputStream(buf), in),
          null);

    final int bomlen = bom.length();

    // If there is a BOM and it is the same length as the bytes read so far, then return the rest of
    // the stream
    if (bomlen == buf.length)
      return new BomAwareInputStream(in, bom);

    // If there is a BOM and it is shorter than the bytes read so far, then return the BOM followed
    // by the rest of the bytes read so far, followed by the rest of the stream
    return new BomAwareInputStream(
        new SequenceInputStream(new ByteArrayInputStream(buf, bomlen, buf.length - bomlen), in),
        bom);
  }

  private final ByteOrderMark bom;

  private BomAwareInputStream(InputStream delegate, ByteOrderMark bom) {
    super(delegate);
    this.bom = bom;
  }

  /**
   * The {@link ByteOrderMark} that was detected at the beginning of the stream, if any, or else
   * empty.
   *
   * @return the {@link ByteOrderMark}
   */
  public Optional<ByteOrderMark> bom() {
    return Optional.ofNullable(bom);
  }
}
