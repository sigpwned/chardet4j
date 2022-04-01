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

import java.nio.charset.Charset;
import java.util.Optional;
import com.sigpwned.chardet4j.com.ibm.icu.text.CharsetDetector;
import com.sigpwned.chardet4j.com.ibm.icu.text.CharsetMatch;

/**
 * Simple interface to charset detection.
 */
public final class Chardet {
  private Chardet() {}

  /**
   * Detect the charset of the given byte data.
   */
  public static Optional<Charset> detectCharset(byte[] data) {
    return detectCharset(data, null);
  }

  /**
   * Detect the charset of the given byte data with the given encoding as a hint.
   */
  public static Optional<Charset> detectCharset(byte[] data, String declaredEncoding) {
    CharsetDetector chardet = new CharsetDetector();

    chardet.setText(data);

    if (declaredEncoding != null)
      chardet.setDeclaredEncoding(declaredEncoding);

    CharsetMatch match = chardet.detect();

    return Optional.ofNullable(match).map(m -> Charset.forName(m.getName()));
  }
}
