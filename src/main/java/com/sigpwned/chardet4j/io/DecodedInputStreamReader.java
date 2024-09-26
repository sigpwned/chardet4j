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

import static java.util.Objects.requireNonNull;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * A simple wrapper around an InputStreamReader that remembers the charset that was used to decode
 * the input stream.
 */
public class DecodedInputStreamReader extends InputStreamReader {
  private final Charset charset;

  public DecodedInputStreamReader(InputStream in, Charset charset) {
    super(in, charset);
    this.charset = requireNonNull(charset);
  }

  /**
   * The charset that was used to decode the input stream.
   *
   * @return the charset
   */
  public Charset charset() {
    return charset;
  }
}
