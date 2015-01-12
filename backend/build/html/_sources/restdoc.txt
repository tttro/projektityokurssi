REST documentation
==================

This chapter describes the REST APIs.

List of status codes used by the REST:

====== ===================== ========================
Status Meaning               Notes
====== ===================== ========================
200    OK                    Request successful
401    Unauthorized          "Login" failed.
403    Forbidden             You shall not pass!
404    Not Found             Resource not found.
405    Method not allowed    HTTP method not allowed.
418    I'm a teapot          Short and stout!
500    Internal Server Error Server snafu'd
====== ===================== ========================

.. _locationrest:

Location data REST
------------------

Location data can be accessed from **/locationdata/api/** (e.g. *www.example.com/locationdata/api/*). This URL does not yet require
authentication and return the installed location data services and what data (element names) they contain.

.. note::
   When creating or updating resources, only metadata is updated or created currently. It is not possible to create actual
   location data objects... yet.

In urls **<collection name>** and **<resource>** are to be replaced with appropriate values. Both are strings.

/locationdata/api/
^^^^^^^^^^^^^^^^^^

Returns the installed location data services that can be accessed by appending the name of the service to the base url of
the location data api.

**Allowed methods:**

* GET

**URL parameters**

* None

Example result::

   [
       {
           "fields": [
               "geometry_name",
               "geometry.type",
               "geometry.coordinates",
               "id",
               "type",
               "properties.URAKKA_ALUE",
               "properties.OSA_ALUE_NIMI",
               "properties.PINTA_ALA",
               "properties.KAYTTOLK",
               "properties.ALUE_NIMI",
               "properties.TILAAJA",
               "properties.VIHERALUEEN_OSAN_ID",
               "properties.KAUPUNGINOSA",
               "properties.TOIMLK",
               "properties.ALUE_SIJ",
               "properties.HOITOLK"
           ],
           "name": "Playgrounds",
           "description": "Ring around the rosie"
       },
       {
           "fields": [
               "geometry_name",
               "geometry.type",
               "geometry.coordinates",
               "id",
               "type",
               "properties.NIMI",
               "properties.LAMPPU_TYYPPI_KOODI",
               "properties.TYYPPI_KOODI",
               "properties.KATUVALO_ID",
               "properties.LAMPPU_TYYPPI",
               "properties.TYYPPI"
           ],
           "name": "Streetlights",
           "description": "Tampere Streetlights"
       }
   ]

.. note::
   The name element is the one to be added to the url.


/locationdata/api/<collection name>/
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Allowed methods:**

* GET

   * Returns the whole collection.

* DELETE

   * Deletes the whole collection.

* PUT

   * Replaces the collection.

* POST

   * Adds a new element to the collection.

**URL parameters**

* mini (*Optional*)

   * **Boolean** Returns minimum amount of data. Valid values: true or false


/locationdata/api/<collection name>/<resource>
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Allowed methods:**

* GET

   * Returns the resource.

* DELETE

   * Deletes the resource.

* PUT

   * Update or create a resource.

/locationdata/api/<collection name>/near/
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Searches objects from circular area.

**Allowed methods:**

* GET

   * Returns the resources near the location.

* DELETE

   * Deletes the resources near the location.

**URL parameters**

* mini (*Optional*)

   * **Boolean** Returns minimum amount of data. Valid values: true or false

* latitude (*Required*)

   * **Float** The latitude of the circle's center

* longitude (*Required*)

   * **Float** The longitude of the circle's center

* range (*Optional*)

   * **Float** The radius of the circle

/locationdata/api/<collection name>/inarea/
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Searches objects inside a rectangular area.

**Allowed methods:**

* GET

   * Returns the resources inside the area.

* DELETE

   * Deletes the resource inside the area.

**URL parameters**

* mini (*Optional*)

   * **Boolean** Returns minimum amount of data. Valid values: true or false

* xbottomleft (*Required*)

   * **Float** The longitude of the bottom left corner of the area.

* ybottomleft (*Required*)

   * **Float** The latitude of the bottom left corner of the area.

* xtopright (*Required*)

   * **Float** The longitude of the top right corner of the area.

* ytopright (*Required*)

   * **Float** The latitude of the top right corner of the area.


/locationdata/api/<collection name>/search/
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Searches from the location data REST. Search is currently limited to the id.

**Allowed methods:**

* POST

   * Send the search JSON.

**URL parameters**

* None

.. _messagerest:

Message data REST
-----------------

The REST for sending messages in the system. For JSON formats, see :ref:`Message formats <messagejson>`

In URLs **<message id>** and **<category>** are to be replaced with appropriate values. Message id is an integer and
category is a string.

/messagedata/api/send/
^^^^^^^^^^^^^^^^^^^^^^

**Allowed methods:**

* POST

   * Send a message.

**URL parameters**

* None

/messagedata/api/users/list/
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Lists all users.

**Allowed methods:**

* GET

   * Returns name and email of users.

**URL parameters**

* None

/messagedata/api/markasread/<message id>
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Allowed methods:**

* GET

   * Mark message read.

**URL parameters**

* None

/messagedata/api/messages/
^^^^^^^^^^^^^^^^^^^^^^^^^^

**Allowed methods:**

* GET

   * Get user's all messages.

* DELETE

   * Delete user's all messages

**URL parameters**

* None

/messagedata/api/messages/<message id>
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Allowed methods:**

* GET

   * Get a single message.

* DELETE

   * Delete the message.

**URL parameters**

* None

/messagedata/api/messages/<category>/
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Allowed methods:**

* GET

   * Get user's all messages in certain category.

* DELETE

   * Delete user's all messages in certain category.

**URL parameters**

* None

/messagedata/api/messages/<category>/<message id>
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

**Allowed methods:**

* GET

   * Get a single message in a certain category.

* DELETE

   * Delete a single message in a certain category.

**URL parameters**

* None
