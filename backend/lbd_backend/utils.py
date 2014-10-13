__author__ = 'xc-'
import json

def combine_data_meta(djo, mjo):


    temp = {
            "OD_info": {
                "identifier_field_name": "feature_id"
            },
            "OD_data": djo,
            "MetaData": mjo
    }

    return temp
