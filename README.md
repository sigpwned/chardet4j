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

## Features

The library assists the user with detecting character set encodings for byte
streams and decoding them into character streams. It offers specific
abstractions for byte order marks (BOMs) and specific methods for identifying
and decoding character encodings for byte arrays and input streams.

The library uses the following algorithm to determine character encoding of
binary data:

1. Check for a BOM. If one is present, then trust it, and use the corresponding
   charset to decode the data.
2. Use a battery of bespoke character set detectors to guess which charset is
   most likely. Users may provide a declared encoding, which provides a boost
   to the given charset in this estimation process. If a charset is identified
   with sufficient confidence, then use it to decode the data.
3. The default charset is used to decode the data, if one is given.

## Installation

The library can be found in Maven Central with the following coordinates:

    <dependency>
        <groupId>com.sigpwned</groupId>
        <artifactId>chardet4j</artifactId>
        <version>75.1.0</version>
    </dependency>

It is compatible with Java versions 8 and later.

## Getting Started

To decode an `InputStream` to a `Reader` by detecting its character set:

    try (Reader chars=Chardet.decode(bytes, StandardCharsets.UTF_8)) {
        // Process chars here
    }

Charset detection is important when dealing with content of unknown provenance,
like content downloaded from the internet or text files uploaded by users. In
such cases, users often have a declared encoding, typically from a content type.
The name of the declared encoding can be provided as a hint to charset
detection:

    try (Reader chars=Chardet.decode(bytes, declaredEncoding, StandardCharsets.UTF_8)) {
        // Process chars here
    }

Byte arrays can be converted directly to Strings as well:

    String chars=Chardet.decode(bytes, declaredEncoding, StandardCharsets.UTF_8);

## Advanced Usage

The following are more sophisticated use cases and edge cases that most users
will not need to worry about.

### Working with BOMs Directly

The easiest way to work with byte order marks directly is with the
`BomAwareInputStream` class:

    try (BomAwareInputStream bomed=BomAwareInputStream.detect(in)) {
        if(bomed.bom().isPresent()) {
            // A BOM was detected in this byte stream, and can be accessed using
            // bomed.bom()
        } else {
            // No BOM was detected in this byte stream.
        }
    }

It is not typically required to work with BOMs directly, but it can be useful
when creating a custom decode pipeline.

### Accessing Character Encoding

The easiest way to determine which character encoding is in use is with the
`DecodedInputStreamReader` class:

    try (DecodedInputStreamReader chars=Chardet.decode(bytes, StandardCharsets.UTF_8)) {
        // The charset that was detected and is being used to decode the given byte
        // stream can be accessed using chars.charset()
        Charset charset = chars.charset();
    }

### Handling Unsupported Charsets

The Java Standard only requires that distributions support the
[standard charsets](https://docs.oracle.com/javase/8/docs/api/index.html?java/nio/charset/StandardCharsets.html)
ISO-8859-1, US-ASCII, UTF-8, UTF-16BE, and UTF-16LE. This library detects those
charsets and many more besides, so there is a possibility that the detected
charset is not supported by the current JVM.

Users are unlikely to hit this situation in the wild, since (a) Java generally
supports almost all of the charsets this library detects, and (b) the
unsupported charsets are scarce in the wild, and getting more scarce every year.

Regardless, there are a couple ways to manage this situation.

#### Catch UnsupportedCharsetException

The library throws a `UnsupportedCharsetException` when the detected charset is not
supported by the current JVM. Users are free to catch this exception and handle
as desired.

    try (Reader chars=Chardet.decode(bytes, StandardCharsets.UTF_8)) {
        // Process chars here
    } catch(UnsupportedCharsetException e) {
        // The charset was detected, but is not supported by current JVM. There are a
        // few ways this is typically handled:
        // 
        // - Propagate as an IOException, since the content cannot be decoded properly
        // - Ignore the error and use a default charset
    }

#### Detect Charset Names

Rather than working with charsets, work with charset names instead. This will
never throw an exception.

    Optional<String> maybeCharsetName = Chardet.detectCharsetName(bytes);
    if(maybeCharsetName.isPresent()) {
        // The charset was detected successfully, and the name can be accessed using
        // maybeCharsetName.get()
    } else {
        // The charset could not be detected
    }

### Using Custom Charsets

Users who wish to add new charsets to the JVM should follow the instructions
on the
[CharsetProvider](https://docs.oracle.com/javase/8/docs/api/java/nio/charset/spi/CharsetProvider.html)
class. The library will automatically pick up any such new charsets.
    
## Configuration

The following configuration variables are available to customize the working of
the library.

### System Property chardet4j.detect.bufsize

One way the library detects character encodings is by analyzing the leading
bytes of a binary file. The more data the library analyzes, the more accurate
the estimates will be, but the longer it will take. By default, this value is
8192 bytes, or 8KiB. Users can change this value by setting the
`chardet4j.detect.bufsize` system property. For example, to set this value to 
16KiB, use:

    java -Dchardet.detect.bufsize=16384 ...

Adjusting the buffer size can be useful when dealing with particularly large
files where detection accuracy or performance might be a concern.

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
released under the Apache license. For more details, see the LICENSE file.
