Poseidon Project
================

This project is a performance monitor for Http/2


Compilation
===========
```bash
./gradlew fatJar 
```
The artifact is located in ./build/libs


Run
===
This application uses jdk's ALPN library, so bootclasspath should be configured. But several versions of jdk have problem when loading the library, jdk1.8.0_51 works well for me
