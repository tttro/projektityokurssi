.. _locationjson:

Location data
-------------

Location data uses GeoJSON (http://geojson.org/) specification, so all objects retrieved from the back-end are compatible
with GeoJSON readers. Thanks to GeoJSON's flexibility, new elements can be added to the objects. Back-end utilizes this
by adding "metadata" element inside "properties" element. This new field is completely optional and may contain information
such as status of the location data object, who has modified the metadata and so on. In this chapter this new element is
described in detail.

JSON Format
^^^^^^^^^^^

Like stated above, the JSON used by the software follows the GeoJSON specification.

An example from http://gejson.org/geojson-spec.html (referenced 11.1.2015)::

    {
        "type": "FeatureCollection",
        "features": [
            {
                "type": "Feature",
                "geometry": {
                    "type": "Point",
                    "coordinates": [102.0, 0.5]
                },
                "properties": {
                    "prop0": "value0"
                }
            },
            {
                "type": "Feature",
                "geometry": {
                    "type": "LineString",
                    "coordinates": [
                        [102.0, 0.0], [103.0, 1.0], [104.0, 0.0], [105.0, 1.0]
                    ]
                },
                "properties": {
                    "prop0": "value0",
                    "prop1": 0.0
                }
            },
            {
                "type": "Feature",
                "geometry": {
                    "type": "Polygon",
                    "coordinates": [
                         [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0],
                           [100.0, 1.0], [100.0, 0.0] ]
                     ]
                },
                "properties": {
                    "prop0": "value0",
                    "prop1": {
                        "this": "that"
                    }
                }
            }
        ]
    }

.. note::
   It should be noted that the current system does not support other feature types than "Point".

Back-end supports GeoJSON Feature when relaying information on single object and FeatureCollection when sending multiple
objects.

The format for the additional information is:

============ ======== ========== =================================================================
Field's name Required Value type Notes
============ ======== ========== =================================================================
status       True     String
modified     True     String     Unix timestamp (in seconds). Tells when the metadata was modified
modifier     True     String     Tells who has modified the metadata
info         True     String
============ ======== ========== =================================================================

Which translates to::

    "metadata": {
        "status": **String**,
        "modified": **Integer**,
        "modifier": **String**,
        "info": **String**
    }


And a "real" example using open data service of Tampere (http://www.tampere.fi/tampereinfo/avoindata.html) to provide
the Streetlight information::

    {
        "geometry": {
            "type": "Point",
            "coordinates": [23.643239226767022, 61.519112683582854]
        },
        "id": "WFS_KATUVALO.405172",
        "type": "Feature",
        "properties": {
            "NIMI": "XPWR_6769212",
            "LAMPPU_TYYPPI_KOODI": "100340",
            "TYYPPI_KOODI": "105007",
            "KATUVALO_ID": 405172,
            "LAMPPU_TYYPPI": "ST 100 (SIEMENS)",
            "metadata": {
                "status": "foobar",
                "note": "FOOBAR",
                "modifier": "tiina@teekkari.fi",
                "modified": 1420741774
            }
        },
        "geometry_name": "GEOLOC"
    }

