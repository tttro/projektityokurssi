# -*- coding: UTF-8 -*-
__author__ = 'Aki Mäkinen'

_urllist = {"local": ("localhost", 8000),
           "amazon": ("ec2-54-93-164-116.eu-central-1.compute.amazonaws.com", 80)}

url, port = _urllist["local"]

lbdheader = {"LBD_LOGIN_HEADER": "TiinaTeekkari"}