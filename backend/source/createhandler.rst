Creating a new handler module
=============================

Creating a new handler requires one to implement the handler interface specified in HandlerBaseClass.py
(RESThandlers/HandlerInterface). For the documentation, see :ref:`Handler Interface documentation <hanint>`

When returning location data from a method, GeoJSON format is used: GeoJSON Feature when a method always returns only one
object and GeoJSON Feature Collection when it is possible to get multiple objects in return.

If data duplication is needed, a model should be created. This is not required as it is possible to use MongoDB through
PyMongo and because the MongoDB documents do not have set form (like SQL tables have), but it can make inserting documents
much easier.

.. note::
    It is not possible to have a user specified field called "id" because of MongoDB. It should be renamed to something internally.

An example model (Tampere Streetlight): ::

    class Geometry(EmbeddedDocument):
        type = StringField()
        coordinates = ListField()

    class Properties(EmbeddedDocument):
        KATUVALO_ID = IntField()
        NIMI = StringField()
        TYYPPI_KOODI = StringField()
        TYYPPI = StringField()
        LAMPPU_TYYPPI_KOODI = StringField()
        LAMPPU_TYYPPI = StringField()

    class Streetlights(Document):
        type = StringField()
        feature_id = StringField(unique=True)
        geometry =  EmbeddedDocumentField(Geometry)
        geometry_name = StringField()
        properties = EmbeddedDocumentField(Properties)

The above model matches the following json: ::

    {
        "geometry":
        {
            "type": "Point",
            "coordinates": [23.643239226767022, 61.519112683582854]
        },
        "id": "WFS_KATUVALO.405172",
        "type": "Feature",
        "properties":
        {
            "NIMI": "XPWR_6769212",
            "TYYPPI_KOODI": "105007",
            "KATUVALO_ID": 405172,
            "LAMPPU_TYYPPI": "ST 100 (SIEMENS)",
            "LAMPPU_TYYPPI_KOODI": "100340"
        },
        "geometry_name": "GEOLOC"
    }

.. note::
    Notice that the id field in the JSON equates to feature_id in the model.