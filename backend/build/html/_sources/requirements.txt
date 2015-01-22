Requirements
============

The back-end is written using Python 2.7. While it is considered to be legacy and (if we are mean) old, it is quite
widely spread and common version of Python. The good thing is that it is stable and does not change anymore, while
Python 3 is still in active development and has not yet seen the final form. Maybe at some point, if this software
is developed further, we will move from 2.7 to 3.x :) .

As database we decided to try out MongoDB, a NoSQL database that stores information as JSON documents. The reason for
this was to try out something other than SQL and because MongoDB has support for geospatial data builtin, which made
the use of GeoJSON easier. In addition we could also form geospatial indexes and searches without any additional
plugins. To use the MongoDB database from Python code, mongoengine and pymongo libraries are used. These can be installed
with Pip.


Software requirements:

* **MongoDB 2.6.6** or greater (designed with 2.6.6)
* **Python 2.7.x** (designed with 2.7.8)

    * **Django 1.7** (*web-framework*)
    * **pymongo 2.7.2** or greater (designed with 2.7.2) (*Python library*)
    * **mongoengine 0.8.7** or greater (designed with 0.8.7) (*Python library*)
    * **httplib2 0.9** or greater (designed with 0.9) (*Python library*)

