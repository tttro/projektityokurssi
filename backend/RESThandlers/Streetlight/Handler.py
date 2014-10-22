import time
import mongoengine

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


    def get_within_rectangle(self, xtop_right, ytop_right, xbottom_left, ybottom_left):
        return self.modelobject.objects(geometry__geo_within_box=
                                        [(xbottom_left,ybottom_left), (xtop_right,ytop_right)]).to_json()


    def get_within_rectangle_mini(self,xtop_right, ytop_right, xbottom_left, ybottom_left):
        obs = self.modelobject.objects(geometry__geo_within_box=
                                       [(xbottom_left,ybottom_left), (xtop_right,ytop_right)])
        print(obs)
        itemlist = list()
        for item in obs:
            temp = {
                "id": item.feature_id,
                "coordinates": item.geometry.coordinates
            }
            itemlist.append(temp)

        return json.dumps(itemlist)


    def get_all(self):
        return self.modelobject.objects().to_json()

    def get_all_mini(self):
        obs = self.modelobject.objects()
        itemlist = list()
        for item in obs:
            temp = {
                "id": item.feature_id,
                "coordinates": item.geometry.coordinates
            }
            itemlist.append(temp)

        return json.dumps(itemlist)


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




