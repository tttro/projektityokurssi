# -*- coding: utf-8 -*-
"""
.. module:: Backend.utils
    :platform: Unix, Windows
.. moduleauthor:: Aki Mäkinen <aki.makinen@outlook.com>

"""

__author__ = 'Aki Mäkinen'

s_codes = {
    "OK": 200,
    "FORBIDDEN": 403,
    "NOTFOUND": 404,
    "METHODNOTALLOWED": 405,
    "TEAPOT": 418,
    "INTERNALERROR": 500
}

_geojson_feature_fields = {
    "type": {
        "_self_": (unicode(), True),
        "_values_": ["Feature"]
    },
    "geometry": {
        "_self_": (dict(), True),
        "type": {
            "_self_": (unicode(), True),
            "_values_": ["Point"]
        },
        "coordinates": {
            "_self_": (list(), True),
            "_elements_": float(),
            "_elementcount_": 2
        }
    },
    "properties": {
        "_self_": (dict(), True),
        "metadata": {
            "_self_": (dict(), False),
            "status": (unicode(), True)
        }
    },
    "id": (unicode(), True)
}

_geojson_featurecollection_fields = {
    "type": (unicode(), True), # Field key hard coded in validation
    "totalFeatures": (int(), False),
    "features": {
        "_self_": (list(), True),
        "_elements_": _geojson_feature_fields
    } # Field key hard coded in validation
}

def geo_json_scheme_validation(jsondict):
    """
    GeoJSON validator
    :param jsondict:
    :return:
    """
    if not isinstance(jsondict, dict):
        return False

    if "type" in jsondict:
        # Check that the given itemdict follows the given format.
        # Stops at the first error returning False
        def check_items(itemdict, itemformat):
            for key, value in itemformat.iteritems():
                if isinstance(value, tuple):
                    if value[1] == True and key not in itemdict:
                        return False
                    elif key in itemdict:
                        if not isinstance(itemdict[key], type(value[0])):
                            return False
                    elif key.lower() in [k.lower() for k in itemdict]:
                        return False
                    else:
                        pass
                elif isinstance(value, dict):
                    if value["_self_"][1] == True and key not in itemdict:
                        return False
                    elif key in itemdict:
                        if isinstance(value["_self_"][0], list):
                            if "_elementcount_" in value:
                                if not len(itemdict[key]) == value["_elementcount_"]:
                                    return False
                            if isinstance(value["_elements_"], dict):
                                itemlist = itemdict[key]
                                newitemformat = dict(value["_elements_"])
                                for item in itemlist:
                                    result = check_items(item, newitemformat)
                                    if not result:
                                        return False
                            else:
                                for listitem in itemdict[key]:
                                    if not isinstance(listitem, type(value["_elements_"])):
                                        return False
                        elif isinstance(value["_self_"][0], dict):
                            newitemdict = itemdict[key]
                            newitemformat = dict(value)
                            del newitemformat["_self_"]
                            result = check_items(newitemdict, newitemformat)
                            if not result:
                                return False
                        else:
                            if isinstance(itemdict[key], type(value["_self_"][0])):
                                if "_values_" in value:
                                    try:
                                        if itemdict[key].lower() not in [v.lower() for v in value["_values_"]]:
                                            return False
                                    except AttributeError:
                                        if itemdict[key] not in value["_values_"]:
                                            return False
                            else:
                                return False
                    elif key in [k.lower() for k in itemdict]:
                        return False
                    else:
                        pass
                else:
                    return False
            return True
        if jsondict["type"].lower() == "featurecollection":
            result = check_items(jsondict, _geojson_featurecollection_fields)
        elif jsondict["type"].lower() == "feature":
            result = check_items(jsondict, _geojson_feature_fields)
        else:
            return False
    else:
        result = False

    return result
