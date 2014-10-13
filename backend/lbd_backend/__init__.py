import threading
from time import sleep, time

class Singleton(object):
    _instance = None
    def __new__(cls, *args, **kwargs):
        if not isinstance(cls._instance, cls):
            cls._instance = object.__new__(cls, *args, **kwargs)
        return cls._instance

class SingleThread(Singleton, threading.Thread):

    def __init__(self):
        self.created = True
        threading.Thread.__init__(self)

    def run(self):
        self.started = True
        while(1):
            print("FOOBAR")
            sleep(5)

# th = SingleThread()
# th.daemon = True
# th.start()