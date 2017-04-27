Change Log
==========

## Version 0.2

_2017-04-27_

New features:
* Java 8 support;
* full serialization and de-serialization of serializable lambdas;
* skipping non-serializable lambdas;

Updates:

* google-gson codebase is updated to release 2.8.0



## Version 0.1

_2016-09-25_

New features:
* (almost) arbitrary objects serialization, with no need for custom adapters, annotations or any changes of the classes;
* preserving exact types during mapping;
* preserving Collections/Maps behavior, including custom Comparators;
* serializing self-referenced objects, including collections, maps and arrays;
* serializing inner, local and anonymous classes;
* support for mixed-type collections, maps and arrays;
* support for non-unique field names, when a field is "overridden" in sub-classes;

## Known issues:
* incorrect serialization of some Iterators
