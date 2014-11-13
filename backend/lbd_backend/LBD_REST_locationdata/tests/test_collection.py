# -*- coding: UTF-8 -*-
__author__ = 'Aki Mäkinen'


import httplib
import json

from unittest import TestCase
from lbd_backend.utils import geo_json_scheme_validation

from lbd_backend.LBD_REST_locationdata.views import collection

httpconn = httplib.HTTPConnection("localhost", 8000)

class TestCollection(TestCase):
    def test_collection(self):
        self.fail()