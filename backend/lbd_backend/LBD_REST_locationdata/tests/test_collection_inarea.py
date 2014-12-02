# -*- coding: UTF-8 -*-
from functools import partial, update_wrapper
import httplib
import json
from unittest import TestCase
import math
import itertools
import unittest
from lbd_backend.LBD_REST_locationdata.tests.test_settings import lbdheader, port, url
import nose

__author__ = 'Aki Mäkinen'

list = ["latitude=foo", "latitude=bar", "latitude=", "latitude", "longitude=1", "longitude=s", "longitude=-1", "longitude=", "longitude=", "longitude", "mini=", "mini=true", "mini=false"]
f = itertools.combinations(list, 3)

def check(a, b, c):
    if a == "longitude=1":
        pass
    elif c == "mini=true":
        pass
    else:
        assert a == c

#class Test(object):
def test_generator():
    for i, k, v in f:
        func = partial(check)
        update_wrapper(func, check)
        func.description = "nice test name %s" % i
        test_generator.__name__ = "nice test name %s" % i
        yield check, i, k, v
