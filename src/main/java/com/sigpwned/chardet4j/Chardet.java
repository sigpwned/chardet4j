package com.sigpwned.chardet4j;

import java.nio.charset.Charset;
import java.util.Optional;
import com.sigpwned.chardet4j.com.ibm.icu.text.CharsetDetector;
import com.sigpwned.chardet4j.com.ibm.icu.text.CharsetMatch;

public class Chardet {
  public static Optional<Charset> detectCharset(byte[] data, String declaredEncoding) {
    CharsetDetector chardet = new CharsetDetector();

    chardet.setText(data);
    chardet.setDeclaredEncoding(declaredEncoding);

    CharsetMatch match = chardet.detect();

    return Optional.ofNullable(match).map(m -> Charset.forName(m.getName()));
  }
}
