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

    try (Reader chars=Chardet.decode(bytes, StandardCharsets.UTF_8)) {
        // Process chars here
    }

Note that the `UTF-8` encoding is used by default if the character set
cannot be detected automatically.

Charset detection is important when dealing with content of unknown
provenance, like content downloaded from the internet. In such cases,
users often have a declared encoding, typically from a content
type. The name of the declared encoding can be provided as a hint to
charset detection:

    try (Reader chars=Chardet.decode(bytes, declaredEncoding, StandardCharsets.UTF_8)) {
        // Process chars here
    }

Byte arrays can be converted directly to Strings as well:

    String chars=Chardet.toString(bytes, declaredEncoding, StandardCharsets.UTF_8);

Users can simply detect a character set, too:

    Optional<Charset> maybeCharset=Chardet.detectCharset(bytes, declaredEncoding);
    
## Supported Character Encodings

The chardet4j library and Java in general supports the following character
encodings at the following levels:

|     Name     | Standard | ICU4J | BOM | Laptop |
|:------------:|:--------:|:-----:|:---:|:------:|
| Big5         |          |   ✔   |     |    ✔   |
| BOCU-1       |          |       |  ✔  |        |
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
| SCSU         |          |       |  ✔  |        |
| Shift_JIS    |          |   ✔   |     |    ✔   |
| US-ASCII     |     ✔    |   ✔*  |     |    ✔   |
| UTF-1        |          |       |  ✔  |        |
| UTF-16BE     |     ✔    |   ✔   |  ✔  |    ✔   |
| UTF-16LE     |     ✔    |   ✔   |  ✔  |    ✔   |
| UTF-32BE     |          |   ✔   |  ✔  |    ✔   |
| UTF-32LE     |          |   ✔   |  ✔  |    ✔   |
| UTF-7        |          |       |  ✔  |        |
| UTF-8        |     ✔    |   ✔   |  ✔  |    ✔   |
| UTF-EBCDIC   |          |       |  ✔  |        |
| windows-1250 |          |   ✔   |     |    ✔   |
| windows-1251 |          |   ✔   |     |    ✔   |
| windows-1252 |          |   ✔   |     |    ✔   |
| windows-1253 |          |   ✔   |     |    ✔   |
| windows-1254 |          |   ✔   |     |    ✔   |
| windows-1255 |          |   ✔   |     |    ✔   |
| windows-1256 |          |   ✔   |     |    ✔   |
|:------------:|:--------:|:-----:|:---:|:------:|
|     Name     | Standard | ICU4J | BOM | Laptop |

Notes:
`*`: ICU4J detects US-ASCII as ISO-8859-1, a superset of US-ASCII

The support levels have the following meanings:

* `Standard` -- The Java Standard requires that all JVMs support this character encoding
* `ICU4J` -- The ICU4J project has a bespoke charset recognizer for this character encoding
* `BOM` -- The character encoding can be detected by Byte Order Mark
* `Laptop` -- The character sets supported by `java version "1.8.0_321"` on my laptop (Obviously, this test is completely unscientific. If you have a better suggestion, please open an issue!)

## Licensing

The icu library is released under the ICU license. The chardet4j
library is released under the Apache license.
