# Android Demo App

This app demonstrates what's necessary to build the LLEAP Java code in
Android Studio.

The app does not do anything useful, it just does one set and get. The
point of the app is to demonstrate how the Android Studio build system
needs to be configured.

This was tested with Android Studio 3.1.

## Note in particular

1. Android-specific Java code is in directory "app".

2. There is a Gradle module called "lib", which consists of only a
symlink from lib/src/main/java/ch to the Java source code in
external/java. This way, even if you make changes via Android Studio,
they are made on the one true copy of the Java library source code.

3. The lib module's build.gradle includes all of the dependencies
necessary to build the Java library. These same dependencies are also
listed in the Maven build configs used when compiling a JAR file for
use in other systems.  Because Android does not use Maven, this cannot
be shared.

## Gotchas

Adding in the Google protobufs causes build errors about platform-specific
files. The exclusions in app/build.gradle are the workaround for this.

