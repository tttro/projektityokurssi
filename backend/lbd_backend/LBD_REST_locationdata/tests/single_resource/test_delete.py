# -*- coding: UTF-8 -*-
from functools import partial, update_wrapper
import httplib
import itertools

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

_collectionstatuses = [("Streetlights",200),
                     ("Streetlighstfa",404),
                     (" ",404),
                     ("", 404),
                     ("äöääöäö", 404),
                     ("///\\\///",404),
                     ("Streetlights{;};", 404)]

_resourcestatuses = [("WFS_KATUVALO.405171",200),
                   ("WFS_KATUVALO.999171",404),
                   ("ÄÖÄÖÄÅÅÅ",404),
                   ("/foobar",404),
                   ("/))#(/¤)(#/¤",404),
                   ("WFS_KATUVALO.405171},{",404)
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

_resourcevalues = [("WFS_KATUVALO.405171",True),
                   ("WFS_KATUVALO.999171",False),
                   ("ÄÖÄÖÄÅÅÅ",False),
                   ("/foobar",False),
                   ("/))#(/¤)(#/¤",False),
                   ("WFS_KATUVALO.405171},{",False)]

_valuecombinations = list(itertools.product(_collectionvalues, _resourcevalues))


def setup_delete_single():
    print "Setup delete (single)"
    global _con
    _con = httplib.HTTPConnection(url, port)
    _con.request("POST", "locationdata/api/Streetligths/WFS_KATUVALO.405171", _content, headers=lbdheader)
    setupresp = _con.getresponse()
    setupresp.read()
    print "Metadata post status: "+str(setupresp.status)
    assert setupresp.status == 200, "Setting up delete (single) went wrong!"
    _con = httplib.HTTPConnection(url, port)


@with_setup(setup_delete_single)
def test_generator_delete_status():
    i = 1
    c = Checker()
    c.httpmethod = "DELETE"
    for col, res in _statuscombinations:
        call = partial(c._status_tester)
        update_wrapper(call, c._status_tester)
        call.description = "Testing response statuses with different collection and resource values"
        test_generator_delete_status.__name__ = "DeleteStatusTest#"+str(i)
        c.con = _con
        yield c._status_tester, col, res
        i += 1


@with_setup(setup_delete_single)
def test_generator_delete_content():
    i = 1
    c = Checker()
    c.httpmethod = "DELETE"
    for col, res in _valuecombinations:
        call = partial(c._content_tester)
        update_wrapper(call, c._content_tester)
        call.description = "Testing response statuses with different collection and resource values"
        test_generator_delete_content.__name__ = "DeleteContentTest#"+str(i)
        c.con = _con
        c.content = _content
        yield c._content_tester, col, res
        i += 1
