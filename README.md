CHARDET4J [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.sigpwned/chardet4j/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.sigpwned/chardet4j)

The state of the art character set detection library for Java is
[icu4j](https://github.com/unicode-org/icu). However, the icu4j JAR
file is about 13MB. This is a hefty price to pay for programs that
only require charset detection!

The chardet4j library simply pulls the `CharsetDetector` feature from
icu4j and repackages it as a standalone library. This allows programs
to make good use of this important feature without bloating their
JARs. At the time of this writing, the chardet4j JAR comes in around
75KB.

The icu library is released under the ICU license. The chardet4j
library is released under the Apache license.