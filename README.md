# yagson: yet another fork of google-gson
Gson is a Java library that can be used to convert Java Objects into their JSON representation and back. 
However, it is shipped with a bunch of restrictions which does not allow to use it for really arbitrary Java objects.
For example, it cannot automatically deserialize pure inner classes, collections of mixed types, circular references etc.
See Gson [user guide](http://sites.google.com/site/gson/gson-user-guide) for the description of the limitations.

Yagson is here for a help! It is devoted to overcome all these limitations, and automatically serialize and deserialize
arbitrary Java object to/from JSON.

** Current status **
Just forked, no advantages over Gson yet.