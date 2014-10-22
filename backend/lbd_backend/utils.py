__author__ = 'xc-'
import json

s_codes = {
    "OK": 200,
    "FORBIDDEN": 403,
    "NOTFOUND": 404,
    "TEAPOT": 418,
    "INTERNALERROR": 500
}


def combine_data_meta(djo, mjo):
    temp = {
            "OD_info": {
                "identifier_field_name": "feature_id"
            },
            "OD_data": djo,
            "MetaData": mjo
    }

    return temp
