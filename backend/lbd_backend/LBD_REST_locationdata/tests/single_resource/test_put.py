# -*- coding: UTF-8 -*-
from functools import partial, update_wrapper
import httplib
import itertools

from nose import with_setup

from lbd_backend.LBD_REST_locationdata.tests.single_resource.checkers import _status_tester, _content_tester
from lbd_backend.LBD_REST_locationdata.tests.test_settings import url, port, lbdheader


__author__ = 'Aki Mäkinen'

_con = httplib.HTTPConnection(url, port)
_base = "/locationdata/api/"
_method = None

_initialcontent = '{"geometry": {"type": "Point", "coordinates": [23.643239226767022, 61.519112683582854]}, "id": ' \
           '"WFS_KATUVALO.405172", "type": "Feature", "properties": {"NIMI": "XPWR_6769212", ' \
           '"LAMPPU_TYYPPI_KOODI": "100340", "TYYPPI_KOODI": "105007", "KATUVALO_ID": 405172, ' \
           '"LAMPPU_TYYPPI": "ST 100 (SIEMENS)", "metadata": {"status": "teststatus", "modifier": ' \
           '"HeliHumanisti", "modified": 1415705418}}, "geometry_name": "GEOLOC"}'

_content = '{"geometry": {"type": "Point", "coordinates": [23.643239226767022, 61.519112683582854]}, "id": ' \
           '"WFS_KATUVALO.405172", "type": "Feature", "properties": {"NIMI": "XPWR_6769212", ' \
           '"LAMPPU_TYYPPI_KOODI": "100340", "TYYPPI_KOODI": "105007", "KATUVALO_ID": 405172, ' \
           '"LAMPPU_TYYPPI": "ST 100 (SIEMENS)", "metadata": {"status": "CHANGED", "modifier": ' \
           '"HeliHumanisti", "modified": 1415705418}}, "geometry_name": "GEOLOC"}'


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

_collectionvalues = [("Streetlights",False),
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


def empty_metadata_and_post():
    print "Clearing metadata"
    global _con
    _con = httplib.HTTPConnection(url, port)
    _con.request("GET", "/locationdata/api/SL/testing/dropmeta", headers={"LBD_LOGIN_HEADER": "VakaVanhaVainamoinen"})
    setupresp = _con.getresponse()
    setupresp.read()
    print "Clear meta status: "+str(setupresp.status)
    assert setupresp.status == 200, "Clearing metadata went wrong!"
    _con = httplib.HTTPConnection(url, port)
    _con.request("GET", "/locationdata/api/SL/testing/dropmeta", headers={"LBD_LOGIN_HEADER": "VakaVanhaVainamoinen"})


@with_setup(empty_metadata_and_post)
def test_generator_post_status():
    i = 1
    for col, res in _statuscombinations:
        method = "POST"
        call = partial(_status_tester)
        update_wrapper(call, _status_tester)
        call.description = "Testing response statuses with different collection and resource values"
        test_generator_post_status.__name__ = "DeleteStatusTest#"+str(i)
        yield _status_tester, col, res, _con, method
        i += 1


@with_setup(empty_metadata)
def test_generator_post_content():
    i = 1
    for col, res in _valuecombinations:
        method = "POST"
        call = partial(_content_tester)
        update_wrapper(call, _content_tester)
        call.description = "Testing response statuses with different collection and resource values"
        test_generator_post_content.__name__ = "DeleteContentTest#"+str(i)
        yield _content_tester, col, res, _con, method, _content
        i += 1
