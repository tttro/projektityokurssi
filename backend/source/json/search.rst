.. _searchjson:

Search
------

Search from the back-end is done by posting a JSON document to the back-end (see:
:ref:`REST documentation <locationrest>`).

============ ============ ========== =====================================
Field's name Required     Value type Notes
============ ============ ========== =====================================
from         True         String     Only allowed value currently is "ALL"
search       True         String     Search phrase
limit        True         Integer    Maximum number of results returned.
============ ============ ========== =====================================

Result format:

============ ============ ========== =================================================
Field's name Required     Value type Notes
============ ============ ========== =================================================
totalresults True         Integer    The total amount of results.
limit        True         Integer    The defined limit of the results.
results      True         JSON       GeoJSON FeatureCollection containing the results.
============ ============ ========== =================================================


Example::

    SEARCH:
    {
        "from": "ALL",
        "search": "42",
        "limit": 21
    }

    RESULT:
    {
        "totalresults": 1138,
        "limit": 42,
        "results": { ...GeoJSON FeatureCollection... }
    }

