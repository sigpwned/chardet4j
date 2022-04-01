CHARDET4J [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.sigpwned/chardet4j/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.sigpwned/chardet4j)

# Introduction

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

# Getting Started

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

Users can simply detect a character set, too:

    Optional<Charset> maybeCharset=Chardet.detectCharset(bytes, declaredEncoding);

# Licensing

The icu library is released under the ICU license. The chardet4j
library is released under the Apache license.