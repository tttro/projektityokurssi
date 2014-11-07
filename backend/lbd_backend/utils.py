__author__ = 'xc-'
import json

s_codes = {
    "OK": 200,
    "FORBIDDEN": 403,
    "NOTFOUND": 404,
    "METHODNOTALLOWED": 405,
    "TEAPOT": 418,
    "INTERNALERROR": 500
}

_geojson_feature_fields = {
    "type": (unicode(), True),
    "geometry": {
        "_self_": (dict(), True),
        "type": (unicode(), True),
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

def geo_json_scheme_validation(jsondict):
    if "type" in jsondict:
        # Check that the given itemdict follows the given format.
        # Stops at the first error returning False
        def check_items(itemdict, itemformat):
            #print itemdict
            #print itemformat
            for key, value in itemformat.iteritems():

                if isinstance(value, tuple):
                    #print "TUPLE"
                    #print value
                    if value[1] == True and key not in itemdict:
                        #print "1"
                        return False
                    elif key in itemdict:
                        if not isinstance(itemdict[key], type(value[0])):
                            ##print type(itemdict[key])
                            #print type(value[0])
                            #print "2"
                            return False
                    else:
                        pass
                elif isinstance(value, dict):
                    if value["_self_"][1] == True and key not in itemdict:
                        #print "FIRST"
                        return False
                    elif key in itemdict:
                        if isinstance(value["_self_"][0], list):
                            if not len(itemdict[key]) == value["_elementcount_"]:
                                #print "SECOND"
                                return False
                            else:
                                if isinstance(value["_elements_"], dict):
                                    newitemdict = itemdict[key]
                                    newitemformat = dict(value["_elements_"])
                                    result = check_items(newitemdict, newitemformat)
                                    if not result:
                                        #print "THIRD"
                                        return False
                                else:
                                    for listitem in itemdict[key]:
                                        #print value["_elements_"]
                                        if not isinstance(listitem, type(value["_elements_"])):
                                            #print "FOURTH"
                                            return False
                        elif isinstance(value["_self_"][0], dict):
                            newitemdict = itemdict[key]
                            newitemformat = dict(value)
                            del newitemformat["_self_"]
                            result = check_items(newitemdict, newitemformat)
                            if not result:
                                #print "FIFTH"
                                return False
                    else:
                        pass
                else:
                    #print "SIXTH"
                    return False
            return True

        result = check_items(jsondict, _geojson_feature_fields)
    else:
        result = False

    return result




    #
    #     if jsondict["type"].lower() == "featurecollection" and "features" in jsondict:
    #         for item in jsondict["features"]:
    #             for key, subitem in item:
    #
    #
    #     elif jsondict["type"].lower() == "feature":
    #
    #     else:
    #         return False
    # else:
    #     return False