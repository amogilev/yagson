Change Log
==========

## Version 0.5.1

_2019-07-02_

New features & fixes:
* Support serialization of non-iterable collections and maps

## Version 0.5

_2019-04-01_

New features & fixes:
* Support for JsonSerializer/JsonDeserializer
* New ReferencePolicy: `DETECT_CIRCULAR_AND_THROW`
* Improvements and bugfixes
* google-gson codebase is updated to release 2.8.5

## Version 0.4.1

_2018-10-06_

New features & fixes:
* Limited support for Android 4

## Version 0.4

_2017-12-09_

New features:
* Java 9 compatibility

Updates:

* google-gson codebase is updated to release 2.8.2

## Version 0.3.1

_2017-08-27_

Bugfixes


## Version 0.3

_2017-07-31_

New features:
* Ability to specify class loaders to use for de-serialization, see `YaGsonBuilder.setPreferredClassLoaders()`;
* Ability to limit the output JSON length, see `YaGson.toJson(Object src, long charsLimit)`

Updates:

* google-gson codebase is updated to release 2.8.1;
* bugfixes


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
