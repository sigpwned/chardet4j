# CHARDET4J [![tests](https://github.com/sigpwned/chardet4j/actions/workflows/tests.yml/badge.svg)](https://github.com/sigpwned/chardet4j/actions/workflows/tests.yml) ![Maven Central](https://img.shields.io/maven-central/v/com.sigpwned/chardet4j)

## Introduction

The state-of-the-art character set detection library for Java is
[icu4j](https://github.com/unicode-org/icu). However, the icu4j JAR
file is about 13MB. This is a hefty price to pay for programs that
only require charset detection! There should be a smaller option of
the same quality.

The chardet4j library pulls the `CharsetDetector` feature from icu4j
and repackages it as this standalone library. This allows programs to
make good use of this important feature without bloating their
JARs. At the time of this writing, the chardet4j JAR comes in around
75KB.

This library also implements some other important components of
character set detection and decoding, namely byte order mark handling.

## Getting Started

To decode an `InputStream` to a `Reader` by detecting its character set:

    // Easy-to-use DecodedInputStreamReader implementation is AutoCloseable and makes
    // the detected charset available.
    try (DecodedInputStreamReader chars=
            Chardet.decode(bytes, StandardCharsets.UTF_8)) {
        // One of the following has happened:
        // - The charset was detected and is supported by current JVM
        // - The charset could not be detected and given default (UTF-8) being used
        
        // This is the charset being used to decode characters
        Charset detectedCharset = chars.charset();
        
        // Process chars here
    } catch(UnsupportedCharsetException e) {
        // The charset was detected, but is not supported by current JVM.
    }

Charset detection is important when dealing with content of unknown
provenance, like content downloaded from the internet. In such cases,
users often have a declared encoding, typically from a content
type. The name of the declared encoding can be provided as a hint to
charset detection:

    try (Reader chars=Chardet.decode(bytes, declaredEncoding, StandardCharsets.UTF_8)) {
        // Process chars here
    } catch(UnsupportedCharsetException e) {
        // Process unsupported charset here
    }

Byte arrays can be converted directly to Strings as well:

    String chars;
    try {
        chars = Chardet.decode(bytes, declaredEncoding, StandardCharsets.UTF_8);
    } catch(UnsupportedCharsetException e) {
        // Could not decode chars because detected charset not supported by current JVM
    }

Users can simply detect a character set, too:

    // We can detect the charset by name. Never throws an exception.
    Optional<String> maybeCharsetName = Chardet.detectCharsetName(bytes, declaredEncoding);
    if(maybeCharsetName.isPresent()) {
        // The charset was detected successfully.
        String detectedCharsetName = maybeCharsetName.orElseThrow();
        
        // We can look up the charset by name at any point.
        Charset detectedCharset;
        try {
            detectedCharset = Charset.forName(detectedCharsetName);
            // The charset is supported by the current JVM.
        } catch(UnsupportedCharsetException e) {
            // The charset is not supported by the current JVM.
        }
    } else {
        // The charset could not be detected. maybeCharsetName is empty.
    }
    
    // We can detect the charset directly. Throws an UnsupportedCharsetException if
    // the detected charset is not supported by the current JVM.
    Optional<Charset> maybeCharset;
    try {
        maybeCharset = Chardet.detectCharset(bytes, declaredEncoding);
        if(maybeCharset.isPresent()) {
            // The charset was detected and is supported by current JVM.
            Charset detectedCharset = maybeCharset.orElseThrow();
        } else {
            // The charset could not be detected. maybeCharset is empty.
        }
    } catch(UnsupportedCharsetException e) {
        // The charset was detected, but is not supported by current JVM.
    }
    
    
## Supported Character Encodings

The chardet4j library and Java in general supports the following character
encodings at the following levels:

|     Name     | Standard | ICU4J | BOM | Laptop |
|:------------:|:--------:|:-----:|:---:|:------:|
| Big5         |          |   ✔   |     |    ✔   |
| EUC-JP       |          |   ✔   |     |    ✔   |
| EUC-KR       |          |   ✔   |     |    ✔   |
| GB18030      |          |   ✔   |  ✔  |    ✔   |
| ISO-2022-CN  |          |   ✔   |     |    ✔   |
| ISO-2022-JP  |          |   ✔   |     |    ✔   |
| ISO-2022-KR  |          |   ✔   |     |    ✔   |
| ISO-8859-1   |          |   ✔   |     |    ✔   |
| ISO-8859-2   |          |   ✔   |     |    ✔   |
| ISO-8859-5   |          |   ✔   |     |    ✔   |
| ISO-8859-6   |          |   ✔   |     |    ✔   |
| ISO-8859-7   |          |   ✔   |     |    ✔   |
| ISO-8859-8   |          |   ✔   |     |    ✔   |
| ISO-8859-8-I |          |   ✔   |     |        |
| ISO-8859-9   |          |   ✔   |     |    ✔   |
| KOI8-R       |          |   ✔   |     |    ✔   |
| Shift_JIS    |          |   ✔   |     |    ✔   |
| US-ASCII     |     ✔    |   ✔*  |     |    ✔   |
| UTF-1        |          |       |  ✔  |        |
| UTF-16BE     |     ✔    |   ✔   |  ✔  |    ✔   |
| UTF-16LE     |     ✔    |   ✔   |  ✔  |    ✔   |
| UTF-32BE     |          |   ✔   |  ✔  |    ✔   |
| UTF-32LE     |          |   ✔   |  ✔  |    ✔   |
| UTF-8        |     ✔    |   ✔   |  ✔  |    ✔   |
| UTF-EBCDIC   |          |       |  ✔  |        |
| windows-1250 |          |   ✔   |     |    ✔   |
| windows-1251 |          |   ✔   |     |    ✔   |
| windows-1252 |          |   ✔   |     |    ✔   |
| windows-1253 |          |   ✔   |     |    ✔   |
| windows-1254 |          |   ✔   |     |    ✔   |
| windows-1255 |          |   ✔   |     |    ✔   |
| windows-1256 |          |   ✔   |     |    ✔   |

Notes:  
`*`: ICU4J detects US-ASCII as ISO-8859-1, a superset of US-ASCII

The support levels have the following meanings:

* `Standard` -- The Java Standard requires that all JVMs support this
   character encoding
* `ICU4J` -- The ICU4J project has a bespoke charset recognizer for this
  character encoding
* `BOM` -- The character encoding can be detected by Byte Order Mark
* `Laptop` -- The character sets supported by `java version "1.8.0_321"` on my
   laptop (Obviously, this test is completely unscientific. If you have a
   better suggestion, please open an issue!)

## Licensing

The icu library is released under the ICU license. The chardet4j library is
released under the Apache license.
