# -*- coding: UTF-8 -*-
from functools import partial, update_wrapper
import httplib
import itertools
import json

from nose import with_setup

from lbd_backend.LBD_REST_locationdata.tests.single_resource.checkers import Checker
from lbd_backend.LBD_REST_locationdata.tests.test_settings import url, port, lbdheader


__author__ = 'Aki Mäkinen'

_con = httplib.HTTPConnection(url, port)
_base = "/locationdata/api/"
_method = None
_content = '{"geometry": {"type": "Point", "coordinates": [23.643239226767022, 61.519112683582854]}, "id": ' \
           '"WFS_KATUVALO.405172", "type": "Feature", "properties": {"NIMI": "XPWR_6769212", ' \
           '"LAMPPU_TYYPPI_KOODI": "100340", "TYYPPI_KOODI": "105007", "KATUVALO_ID": 405172, ' \
           '"LAMPPU_TYYPPI": "ST 100 (SIEMENS)", "metadata": {"status": "teststatus", "modifier": ' \
           '"HeliHumanisti", "modified": 1415705418}}, "geometry_name": "GEOLOC"}'
_jsontest = {
    "geometry": {
        "type": ["Point", "LineString", "Circle", "Line", "ÖÖÄÄÅÅÅ", "()/)(/><>><»”»", "", " ", 123, 12.22],
        "coordinates": list(itertools.product(
            [23.643239226767022, "23.64323922676sss7022", " ", "", 23, "ÄÖÅÅÅÅÅÅ", "][{][{@$£@"],
            [61.519112683582854, "61.519112ssss683582854", " ", "", 61, "ÄÖÅÅÅÅÅÅ", "][{][{@$£@"]
        )),
    },
    "id": ["WFS_KATUVALO.405172", "WFS_KATUVALO.999999", "ÄÖÄÅÅLÖÄÖLÄ", " ", "", 234.234, 234],
    "type": ["featureCollection", "vuohi", "lammas", "SKÅNDINÅÅVI", "ÖÖÖÄÄÄÅÅÅ", ")(/#)(¤@£$@$", 123.123, 123],
    "properties": {
        "NIMI": ["XPWR_6769212","AÖSLDK", "ÖÄÅÅ", 234.234, 234, "$£]$[{£$‚[£"],
        "LAMPPU_TYYPPI_KOODI": [1415705418, 234.234, "234", "ALKJFLFKJ", "${‚$‚$£‚"],
        "TYYPPI_KOODI": [1415705418, 234.234, "234", "ALKJFLFKJ", "${‚$‚$£‚"],
        "KATUVALO_ID": [1415705418, 234.234, "234", "ALKJFLFKJ", "${‚$‚$£‚"],
        "LAMPPU_TYYPPI": ["ST 100 (SIEMENS)", " ", "", "Foobar", "ÄÖÅ", "]@£${@", 234.2342, 234],
        "metadata": {
            "status": ["HeliHumanisti", 234.234, 234, " ", "", "ÖÄÅ", "]£[${£"],
             "modifier": ["HeliHumanisti", 234.234, 234, " ", "", "ÖÄÅ", "]£[${£"],
             "modified": [1415705418, 234.234, "234", "ALKJFLFKJ", "${‚$‚$£‚"]
        }
    },
    "geometry_name": ["FOoBAR", "ÄÖÅÅ", ")(/)/(()/¤&#„»„>»", 123.12, 123]
}
_collectionstatuses = [("Streetlights",200),
                     ("Streetlighstfa",404),
                     (" ",404),
                     ("", 404),
                     ("äöääöäö", 404),
                     ("///\\\///",404),
                     ("Streetlights{;};", 404)]

_resourcestatuses = [("WFS_KATUVALO.405171",405),
                   ("WFS_KATUVALO.999171",405),
                   ("ÄÖÄÖÄÅÅÅ",405),
                   ("/foobar",405),
                   ("/))#(/¤)(#/¤",405),
                   ("WFS_KATUVALO.405171},{",405)
                   ]
_statuscombinations = list(itertools.product(_collectionstatuses, _resourcestatuses))

# For validating the possible content

_collectionvalues = [("Streetlights",True),
                     ("Streetlighstfa",False),
                     (" ",False),
                     ("", False),
                     ("äöääöäö", False),
                     ("///\\\///",False),
                     ("Streetlights{;};", False)]

_resourcevalues = [("WFS_KATUVALO.405171",False),
                   ("WFS_KATUVALO.999171",False),
                   ("ÄÖÄÖÄÅÅÅ",False),
                   ("/foobar",False),
                   ("/))#(/¤)(#/¤",False),
                   ("WFS_KATUVALO.405171},{",False)]

_valuecombinations = list(itertools.product(_collectionvalues, _resourcevalues))


def empty_metadata():
    print "Clearing metadata"
    global con
    _con = httplib.HTTPConnection(url, port)
    _con.request("GET", "/locationdata/api/SL/testing/dropmeta", headers={"LBD_LOGIN_HEADER": "VakaVanhaVainamoinen"})
    setupresp = _con.getresponse()
    setupresp.read()
    print "Clear meta status: "+str(setupresp.status)
    assert setupresp.status == 200, "Clearing metadata went wrong!"
    _con = httplib.HTTPConnection(url, port)


@with_setup(empty_metadata)
def test_generator_post_status():
    i = 1
    c = Checker()
    c.httpmethod = "POST"
    for col, res in _statuscombinations:
        call = partial(c._status_tester)
        update_wrapper(call, c._status_tester)
        call.description = "Testing response statuses with different collection and resource values"
        test_generator_post_status.__name__ = "PostStatusTest#"+str(i)
        c.con = _con
        yield c._status_tester, col, res
        i += 1


@with_setup(empty_metadata)
def test_generator_post_content():
    i = 1
    c = Checker()
    c.httpmethod = "POST"
    for col, res in _valuecombinations:
        call = partial(c._content_tester)
        update_wrapper(call, c._content_tester)
        call.description = "Testing response statuses with different collection and resource values"
        test_generator_post_content.__name__ = "PostContentTest#"+str(i)
        c.con = _con
        c.content = _content
        yield c._content_tester, col, res
        i += 1

# Manual checkup. These tests are run automatically, but success must be checked manually.
@with_setup(empty_metadata)
def test_generator_post_json_format():
    loaded = json.loads(_content)
    for k, v in _jsontest.iteritems():
        content_as_json = loaded
        if isinstance(v, list):
            for item in v:
                content_as_json[k] = item
                print content_as_json
        elif k == "geometry":
            for g, w in _jsontest["geometry"].iteritems():
                for item in w:
                    content_as_json["geometry"][g] = item
                    print content_as_json
        elif k == "properties":
            for g, w in _jsontest["properties"].iteritems():
                if g == "metadata":
                    for l, u in _jsontest["properties"]["metadata"].iteritems():
                        for item in u:
                            content_as_json["properties"]["metadata"][l] = item
                            print content_as_json
                else:
                    for item in w:
                        content_as_json["properties"][g] = item
                        print content_as_json




