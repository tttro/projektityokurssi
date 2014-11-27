# -*- coding: utf-8 -*-
__author__ = 'Aki Mäkinen'

import json
import urllib
from lbd_backend.LBD_REST_locationdata.models import *
from RESThandlers.Streetlight import models as SL
import time
import mongoengine

req = urllib.urlopen(
            'http://tampere.navici.com/tampere_wfs_geoserver/opendata/ows?service=WFS&version=1.0.0&request=GetFeature'
            '&typeName=opendata:WFS_KATUVALO&outputFormat=json&srsName=EPSG:4326', proxies={})
jsonitem = json.loads(req.read())
SL.Streetlights.drop_collection()

start = time.time()*1000
for item in jsonitem["features"]:
    item["feature_id"] = item.pop("id")
SL.Streetlights._get_collection().insert(jsonitem["features"])
end = time.time()*1000
print end-start

def populate_meta():
    o = SL.Streetlights.objects()
    MetaDocument.drop_collection()
    i = 1
    for item in o:
        try:
            temp = MetaDocument(
                feature_id = item.feature_id,
                collection = "Streetlights",
                meta_data = MetaData(status="SNAFU", modified=int(time.time()), modifier="Seppo Sähkäri")
            )
            temp.save()
        except mongoengine.NotUniqueError:
            pass
        if i % 1000 == 0:
            print i
        i += 1

#populate_meta()
