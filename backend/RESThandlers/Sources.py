from RESThandlers.Streetlight import Handler

__author__ = 'xc-'


_installedSources = {
    "Streetlights": {
        "handler": Handler.StreetlightHandler,
        "id_field": "feature_id"
    }
}