# sconfig
A partial implementation of the [HOCON config API](https://github.com/lightbend/config) with extended features, written in pure Scala.

### Deviations from HOCON
#### Paths
- spaces in unquoted paths are (currently) not allowed, i.e. `a.path with. whitespace` is not valid.

