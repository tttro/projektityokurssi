# -*- coding: utf-8 -*-
"""
.. module:: Handlers.Sources
    :platform: Unix, Windows
.. moduleauthor:: Aki Mäkinen <aki.makinen@outlook.com>

"""
__author__ = 'Aki Mäkinen'

from RESThandlers.Streetlight import Handler

_installedSources = {
    "Streetlights": {
        "handler": Handler.StreetlightHandler
    }
}