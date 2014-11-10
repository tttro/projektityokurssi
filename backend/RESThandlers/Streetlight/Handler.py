# -*- coding: utf-8 -*-
"""
.. module:: Handlers.Streetlight.handler
    :platform: Unix, Windows
.. moduleauthor:: Aki Mäkinen <aki.makinen@outlook.com>

"""
__author__ = 'Aki Mäkinen'

from RESThandlers.HandlerInterface.Exceptions import GenericDBError

import urllib
import json

from RESThandlers.HandlerInterface.HandlerBaseClass import HandlerBase
from RESThandlers.Streetlight.models import Streetlights


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
        "type": "FeatureCollection",
        "totalFeatures": None,
        "features": list()
    }

    def __init__(self):
        self.modelobject = Streetlights

    def update_db(self):
        req = urllib.urlopen(
            'http://tampere.navici.com/tampere_wfs_geoserver/opendata/ows?service=WFS&version=1.0.0&request=GetFeature'
            '&typeName=opendata:WFS_KATUVALO&outputFormat=json&srsName=EPSG:4326',
            proxies={})
        jsonitem = json.loads(req.read())
        itemsinserted = 0
        itemlist = list()
        self.modelobject.drop_collection()
        for item in jsonitem["features"]:
            fid = item.pop("id")
            temp = self.modelobject.from_json(json.dumps(item))
            temp.feature_id = fid
            itemlist.append(temp)
            itemsinserted += 1
        self.modelobject.objects().insert(itemlist)

        return itemsinserted

    def get_by_id(self, iid):
        result = self.modelobject._get_collection().aggregate([
            {"$match": {"feature_id": iid}},
            {"$project": self._doc_structure}
        ])
        if int(result["ok"]) == 1 and len(result["result"]) > 0:
            return result["result"][0]
        else:
            return None

    def get_near(self, longitude, latitude, nrange=0.001, mini=False):
        if mini:
            doc_structure = self._doc_structure_mini
        else:
            doc_structure = self._doc_structure

        result = self.modelobject._get_collection().aggregate([
            {"$match":
                 {"geometry":
                      {"$geoWithin":
                           {"$center": [[float(longitude), float(latitude)], nrange]}
                      }
                 }
            },
            {"$project": doc_structure}
        ])
        itemcount = len(result["result"])
        if int(result["ok"]) and itemcount > 0:
            fc = self._featurecollection
            fc["totalFeatures"] = itemcount
            fc["features"] = result["result"]

            return fc
        else:
            return None

    def get_within_rectangle(self, xtop_right, ytop_right, xbottom_left, ybottom_left, mini=False):

        if mini:
            doc_structure = self._doc_structure_mini
        else:
            doc_structure = self._doc_structure

        raw = self.modelobject._get_collection().aggregate([{'$match':
                                                                 {'geometry':
                                                                      {'$geoWithin':
                                                                           {'$box': [[xbottom_left, ybottom_left],
                                                                                     [xtop_right, ytop_right]]}
                                                                      }
                                                                 }
                                                            },
                                                            {'$project': doc_structure}
        ])
        if int(raw["ok"]) != 1:
            raise GenericDBError("Database query failed. Status: " + str(raw["ok"]))
        else:
            f_count = self.modelobject.objects(geometry__geo_within_box=
                                               [(xbottom_left, ybottom_left), (xtop_right, ytop_right)]).count()
            if f_count > 0:
                featurecollection = self._featurecollection
                featurecollection["totalFeatures"] = f_count
                featurecollection["features"] = raw["result"]

                return featurecollection
            else:
                return None

    def get_all(self, mini=True):
        if mini:
            doc_structure = self._doc_structure_mini
        else:
            doc_structure = self._doc_structure

        raw = self.modelobject._get_collection().aggregate([{'$project': doc_structure}])
        if int(raw["ok"]) != 1:
            raise GenericDBError("Database query failed. Status: " + str(raw["ok"]))
        else:
            res_count = len(raw["result"])
            if res_count > 0:
                featurecollection = self._featurecollection
                featurecollection["totalFeatures"] = res_count
                featurecollection["features"] = raw["result"]

                return featurecollection
            else:
                return None

    # Return values:
    # Boolean: True if all were deleted, False if objects remain in db after deletion
    def delete_all(self):
        obj = self.modelobject.objects()
        items = obj.count()
        deleted = obj.delete()
        if items == deleted:
            return True
        else:
            return False

    def delete_near(self, latitude, longitude, nrange):
        itemcount = self.modelobject.objects(geometry__geo_within_center=
                                             [(float(latitude), float(longitude)), nrange]).count()

        delcount = self.modelobject.objects(geometry__geo_within_center=
                                            [(float(latitude), float(longitude)), nrange]).delete()

        if itemcount == delcount:
            return True
        else:
            return False

    # Function get_item_count
    # Return values:
    #       Integer: number of items in table
    def get_item_count(self):
        return self.modelobject.objects().count()
