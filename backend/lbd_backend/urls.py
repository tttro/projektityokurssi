# -*- coding: utf-8 -*-
"""
.. module:: lbd_backend.urls
    :platform: Unix, Windows
    :synopsis: This module handles http requests related to location data.
"""
from lbd_backend.LBD_REST_messagedata.views import msg_collection, msg_send

__author__ = 'Aki Mäkinen'


from django.conf.urls import patterns, url
from lbd_backend.LBD_REST_locationdata.views import single_resource, collection, \
    collection_near, collection_inarea, testing_view_popmeta, testing_view_dropmeta, search_from_rest, api, add_user

from django.contrib import admin
from django.conf import settings

admin.autodiscover()

urlpatterns = patterns('',
    # REST API
    url(r'^locationdata/api/$', api),
    url(r'^locationdata/api/(?P<collection>\w+)/$', collection),
    url(r'^locationdata/api/(?P<collection>\w+)/(?P<resource>(\w|\.)+)$', single_resource),
    url(r'^locationdata/api/(?P<collection>\w+)/near/$', collection_near),
    url(r'^locationdata/api/(?P<collection>\w+)/inarea/$', collection_inarea),
    url(r'^locationdata/api/(?P<collection>\w+)/search/$', search_from_rest),

    # url(r'^messages/api/(?P<collection>\w+)$', stub),
    # url(r'^messages/api/(?P<collection>\w+)/(?P<message>\w+)$', stub),
    url(r'^add_user/$', add_user),


    url(r'^messagedata/api/messages/$', msg_collection),
    url(r'^messagedata/api/send/$', msg_send),
    # url(r'^messagedata/api/(?P<collection>\w+)/(?P<message>\w+)$', stub),
    # url(r'^messagedata/api/(?P<collection>\w+)/(?P<category>\w+)$', stub),
)

if settings.TESTING_URLS:
    testpatterns = patterns('',
        url(r'^locationdata/api/SL/testing/popmeta$', testing_view_popmeta),
        url(r'^locationdata/api/SL/testing/dropmeta$', testing_view_dropmeta),
    )

    urlpatterns += testpatterns

