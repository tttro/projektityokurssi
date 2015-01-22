# -*- coding: utf-8 -*-
"""
.. module:: Handlers.Sources
    :platform: Unix, Windows
.. moduleauthor:: Aki Mäkinen <aki.makinen@outlook.com>

"""
__author__ = 'Aki Mäkinen'

from RESThandlers.Streetlight import Handler as SL_Handler
from RESThandlers.Playgrounds import Handler as P_Handler


_installedSources = {
    "Streetlights": {
        "handler": SL_Handler.StreetlightHandler,
        "name": "Tampere Streetlights"
    },
    "Playgrounds": {
        "handler": P_Handler.PlaygroundHandler,
        "name": "Ring around the rosie"
    }
}