import time
import mongoengine
from RESThandlers.HandlerInterface.Exceptions import GenericDBError

__author__ = 'xc-'

import urllib
import json

from RESThandlers.HandlerInterface.HandlerBaseClass import HandlerBase
from RESThandlers.Streetlight.models import Streetlights, Geometry, Properties


class StreetlightHandler(HandlerBase):
    @property
    @staticmethod
    def handler_id(self):
        return json.dumps({
            "identifier_field_name": "feature_id"
        })

    _doc_structure = {
        "_id": 0,
        "type": 1,
        "id": "$feature_id",
        "geometry": {
            "type": 1,
            "coordinates": 1
        },
        "geometry_name": 1,
        "properties": {
            "KATUVALO_ID": 1,
            "NIMI": 1,
            "TYYPPI_KOODI": 1,
            "LAMPPU_TYYPPI_KOODI": 1,
            "LAMPPU_TYYPPI": 1
        }
    }

    _doc_structure_mini = {
        "_id": 0,
        "type": 1,
        "id": "$feature_id",
        "geometry": {
            "type": 1,
            "coordinates": 1
        },
    }

    _featurecollection = {
        "type": "Feature",
        "totalFeatures": None,
        "features": list()
    }

    def __init__(self):
        self.modelobject = Streetlights


    def update_db(self):
        req = urllib.urlopen('http://tampere.navici.com/tampere_wfs_geoserver/opendata/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=opendata:WFS_KATUVALO&outputFormat=json&srsName=EPSG:4326', proxies={})
        jsonItem = json.loads(req.read())
        itemsInserted = 0
        itemlist = list()
        self.modelobject.drop_collection()
        for item in jsonItem["features"]:
            fid = item.pop("id")
            temp = self.modelobject.from_json(json.dumps(item))
            temp.feature_id = fid
            itemlist.append(temp)
            itemsInserted += 1
        self.modelobject.objects().insert(itemlist)

        return itemsInserted


    def get_by_id(self, iid):
        return self.modelobject.objects(id=iid).to_json()


    def get_by_handler_id(self, iid):
        return self.modelobject.objects().get(feature_id=iid).to_json()


    def get_by_handler_json(self, jsonitem):
        return self.modelobject.objects().get(feature_id=jsonitem["feature_id"]).to_json()


    def get_near(self, latitude, longitude, range=0.001):
        return self.modelobject.objects(geometry__geo_within_center=[(float(latitude), float(longitude)), range]).to_json()


    def get_within_rectangle(self,xtop_right, ytop_right, xbottom_left, ybottom_left, mini=False):

        if mini:
            doc_structure = self._doc_structure_mini
        else:
            doc_structure = self._doc_structure

        raw = self.modelobject._get_collection().aggregate([{'$match':
                                                                 {'geometry':
                                                                      {'$geoWithin':
                                                                           {'$box': [[xbottom_left, ybottom_left ],
                                                                                     [xtop_right, ytop_right]]}
                                                                      }
                                                                 }
                                                            },
                                                            {'$project':
                                                                    doc_structure
                                                            }])
        if int(raw["ok"]) != 1:
            raise GenericDBError("Database query failed. Status: "+str(raw["ok"]))
        else:
            f_count = self.modelobject.objects(geometry__geo_within_box=
                                        [(xbottom_left,ybottom_left), (xtop_right,ytop_right)]).count()
            featurecollection = self._featurecollection
            featurecollection["totalFeatures"] = f_count
            featurecollection["features"] = raw["result"]

            return featurecollection


    def get_all(self):
        raw = self.modelobject._get_collection().aggregate([{'$project':self._doc_structure}])
        if int(raw["ok"]) != 1:
            raise GenericDBError("Database query failed. Status: "+str(raw["ok"]))
        else:
            featurecollection = self._featurecollection
            featurecollection["totalFeatures"] = self.modelobject.objects().count()
            featurecollection["features"] = raw["result"]

            return featurecollection

    def get_all_mini(self):
        raw = self.modelobject._get_collection().aggregate([{'$project':self._doc_structure_mini}])
        if int(raw["ok"]) != 1:
            raise GenericDBError("Database query failed. Status: "+str(raw["ok"]))
        else:
            featurecollection = self._featurecollection
            featurecollection["totalFeatures"] = self.modelobject.objects().count()
            featurecollection["features"] = raw["result"]

            return featurecollection


    # Return values:
    #       Boolean: True if all were deleted, False if objects remain in db after deletion
    def delete_all(self):
        obj = self.modelobject.objects()
        items = obj.count()
        deleted = obj.delete()
        if items == deleted:
            return True
        else:
            return False

    def delete_near(self, latitude, longitude, range):
        itemcount = self.modelobject.objects(geometry__geo_within_center=
                                             [(float(latitude), float(longitude)), range]).count()

        delcount = self.modelobject.objects(geometry__geo_within_center=
                                            [(float(latitude), float(longitude)), range]).delete()

        if itemcount == delcount:
            return True
        else:
            return False
    # Function delete_item
    # Parameters:
    #       jsonitem: a Python json object (returned by json.loads())
    def delete_item_by_handler_json(self, jsonitem):
        self.modelobject.objects().get(feature_id=jsonitem["feature_id"]).delete()


    def delete_item_by_handler_id(self, iid):
        self.modelobject.objects().get(feature_id=iid).delete()


    # Function delete_item_by_id
    # Parameters:
    #       iid: a MongoDB id
    def delete_item_by_id(self, iid):
        result = self.modelobject.objects().get(id=iid).delete()


    # Function get_item_count
    # Return values:
    #       Integer: number of items in table
    def get_item_count(self):
        return self.modelobject.objects().count()




