# -*- coding: utf-8 -*-
"""
.. module:: Backend.utils
    :platform: Unix, Windows
.. moduleauthor:: Aki Mäkinen <aki.makinen@outlook.com>

"""

__author__ = 'Aki Mäkinen'

s_codes = {
    "OK": 200,
    "BAD": 400,
    "UNAUTH": 401,
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
            "status": (unicode(), True),
            "info": (unicode(), True)
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
    A simple GeoJSON validator.

    Uses the GeoJSON definitions described in LBD JSON Formats document.
    JSON format is described as python dictionary, where the key specifies the name of a JSON field and
    value describes if the field/value is required and what is the type of the value. There are some special
    key values: _self_ (if the value is list or embedded document), _elements_ (if the value is a list, this describes
    the element type) and _elementcount_ (restricts how many elements list can have).

    .. note::

        This function is a if-else hell... and the JSON format document is outdated.

    :param jsondict: GeoJSON formatted Python dictionary containing either GeoJSON Feature or FeatureCollection.
    :return Boolean: True or False depending on the result of the validation
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


def flattener(dicti, parent):
    """
    Dictionary flattener

    Flattens a dictionary and... Ok I don't remember what this is for.
    Creates once iterable list.

    :param dicti: Dictionary to be flattened
    :param parent: Parent element of the dictionary
    """
    for k, v in dicti.iteritems():
        if isinstance(v, dict):
            if parent is None:
                father = k
            else:
                father = parent + "." + k
            for item in flattener(v, father):
                yield item
        else:
            if parent is not None:
                yield parent + "." + k
            else:
                yield k