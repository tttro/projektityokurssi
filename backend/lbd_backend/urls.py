# -*- coding: utf-8 -*-
"""
.. module:: lbd_backend.urls
    :platform: Unix, Windows
    :synopsis: This module handles http requests related to location data.
"""

__author__ = 'Aki Mäkinen'

from django.conf.urls import patterns, url
from django.contrib import admin
from django.conf import settings

from lbd_backend.LBD_REST_locationdata.views import single_resource, collection, \
    collection_near, collection_inarea, testing_view_popmeta, testing_view_dropmeta, search_from_rest, api
from lbd_backend.LBD_REST_messagedata.views import msg_general, msg_send
from lbd_backend.LBD_REST_users.views import list_users, user_exists, add_user, index


admin.autodiscover()

urlpatterns = patterns('',
    # REST API
    url(r'^locationdata/api/$', api),
    url(r'^locationdata/api/(?P<collection>\w+)/$', collection),
    url(r'^locationdata/api/(?P<collection>\w+)/(?P<resource>(\w|\.)+)$', single_resource),
    url(r'^locationdata/api/(?P<collection>\w+)/near/$', collection_near),
    url(r'^locationdata/api/(?P<collection>\w+)/inarea/$', collection_inarea),
    url(r'^locationdata/api/(?P<collection>\w+)/search/$', search_from_rest),

    url(r'^messagedata/api/send/$', msg_send),
    url(r'^messagedata/api/users/list/$', list_users),
    url(r'^messagedata/api/messages/$', msg_general),
    url(r'^messagedata/api/messages/(?P<message>\w+)$', msg_general),
    url(r'^messagedata/api/messages/(?P<category>\w+)/$', msg_general),
    url(r'^messagedata/api/messages/(?P<category>\w+)/(?P<message>(\w|\.)+)$', msg_general),
)

if settings.TESTING_URLS:
    testpatterns = patterns('',
        url(r'^locationdata/api/SL/testing/popmeta$', testing_view_popmeta),
        url(r'^locationdata/api/SL/testing/dropmeta$', testing_view_dropmeta),
        url(r'^$', index),
        url(r'^add_user/$', add_user),
        url(r'^user_exists/$', user_exists),
    )

    urlpatterns += testpatterns

