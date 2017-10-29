# sconfig
A configuration library for Scala JVM, JS, and Native that is (mostly) compatible to Typesafe Config / HOCON.

### Deviations from HOCON
#### Paths
- spaces in unquoted paths are (currently) not allowed, i.e. `a.path with. whitespace` is not valid.
