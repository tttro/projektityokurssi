# -*- coding: UTF-8 -*-
from functools import partial, update_wrapper
import httplib
import itertools
from nose import with_setup
from lbd_backend.LBD_REST_locationdata.tests.test_settings import url, port
from lbd_backend.LBD_REST_locationdata.tests.single_resource.checkers import Checker
__author__ = 'Aki Mäkinen'

_con = httplib.HTTPConnection(url, port)

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

def setup_get():
    global _con
    _con = httplib.HTTPConnection(url, port)


@with_setup(setup_get)
def test_generator_get_status():
    i = 1
    c = Checker()
    c.httpmethod = "GET"
    for col, res in _statuscombinations:
        call = partial(c._status_tester)
        update_wrapper(call, Checker._status_tester)
        call.description = "Testing response statuses with different collection and resource values"
        test_generator_get_status.__name__ = "GetStatusTest#"+str(i)
        c.con = _con
        yield c._status_tester, col, res
        i += 1


@with_setup(setup_get)
def test_generator_get_content():
    i = 1
    c = Checker()
    c.httpmethod = "GET"
    for col, res in _valuecombinations:
        call = partial(c._content_tester)
        update_wrapper(call, c._content_tester)
        call.description = "Testing response statuses with different collection and resource values"
        test_generator_get_content.__name__ = "GetContentTest#"+str(i)
        c.con = _con
        yield c._content_tester, col, res
        i += 1
