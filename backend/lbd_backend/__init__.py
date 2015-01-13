# -*- coding: utf-8 -*-
"""
.. module:: lbd_backend.urls
    :platform: Unix, Windows
    :synopsis: This module handles http requests related to location data.
"""
#
# import threading
# from time import sleep
# from RESThandlers.Streetlight.Handler import StreetlightHandler
#
#
# class SingleThread(threading.Thread):
#     _instance = None
#     _newlock = False
#     def __new__(cls, *args, **kwargs):
#         if not cls._newlock:
#             cls._newlock = True
#             if not isinstance(cls._instance, cls):
#                 cls._instance = object.__new__(cls, *args, **kwargs)
#             return cls._instance
#         else:
#             return None
#
#     def __init__(self):
#         self.created = True
#         threading.Thread.__init__(self)
#
#     def run(self):
#         self.started = True
#         sh = StreetlightHandler()
#         while(1):
#             print("Updating")
#             sh.update_db()
#             sleep(7200)

# th = SingleThread()
# th.daemon = True
# th.start()