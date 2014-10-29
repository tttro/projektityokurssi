import json
import urllib

__author__ = 'xc-'

from lbd_backend.LBD_REST_locationdata.models import *
from RESThandlers.Streetlight import models as SL
import mongoengine

req = urllib.urlopen(
            'http://tampere.navici.com/tampere_wfs_geoserver/opendata/ows?service=WFS&version=1.0.0&request=GetFeature'
            '&typeName=opendata:WFS_KATUVALO&outputFormat=json&srsName=EPSG:4326',
            proxies={})
jsonitem = json.loads(req.read())
itemsinserted = 0
itemlist = list()
SL.Streetlights.drop_collection()
for item in jsonitem["features"]:
    fid = item.pop("id")
    temp = SL.Streetlights.from_json(json.dumps(item))
    temp.feature_id = fid
    itemlist.append(temp)
    itemsinserted += 1
SL.Streetlights.objects().insert(itemlist)

o = SL.Streetlights.objects()

for item in o:
    try:
        temp = MetaDocument.objects().get(feature_id=item.feature_id)
        temp.meta_data.status = "SNAFU"
        temp.save()
    except mongoengine.DoesNotExist:
        try:
            temp = MetaDocument(
                feature_id = item.feature_id,
                collection = "Streetlights",
                meta_data = MetaData(status="SNAFU")
            )
            temp.save()
        except mongoengine.NotUniqueError:
            pass
    except mongoengine.MultipleObjectsReturned:
        pass

