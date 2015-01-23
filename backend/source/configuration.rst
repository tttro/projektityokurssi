.. _config:

Configuration
=============

Now to get the back-end properly configured. First thing is to check Django's settings.::

    BASE_DIR = os.path.dirname(os.path.dirname(__file__))

    # SECURITY WARNING: keep the secret key used in production secret!
    SECRET_KEY = ''

    TESTING_URLS = True

    DEBUG = True

    TEMPLATE_DEBUG = DEBUG

    ALLOWED_HOSTS = ["*"]


*BASE_DIR* defines the installation directory. The value in the sample settings file should be left alone, unless you are
certain on what you are doing.

*SECRET_KEY* should always be kept as a secret and changed for each production server. In order to generate a new secret key,
run *generate_skey.py* located in *backend/lbd_backend* folder and paste the string to SECRET_KEY.

*TESTING_URLS* is used to enable and disable some URL configurations. In the current stage of development, this means
the unofficial "registeration" URLs (only used to add a new user to user database). In production this should be set
False and only enabled temporarily if/when needed and if you are absolutely sure about it.

*DEBUG* enables some Django debug printing such as stack trace when an exception is left uncaught. In production this should
always be False.

*TEMPLATE_DEBUG* should have same value as DEBUG.

The code below connects to the mongoengine database. The first argument is the name of the database and the second the address
and port of the database. Change these according to your MongoDB configuration.::

    mongoengine.connect("lbd_backend", host="127.0.0.1:27017")

The following set some settings needed for the mongoengine and MongoDB. Change these at your own risk.::

    AUTHENTICATION_BACKENDS = (
                                'mongoengine.django.auth.MongoEngineBackend',
                                )
    SESSION_ENGINE = 'mongoengine.django.sessions'

    TEST_RUNNER = 'lbd_backend.LBD_REST_locationdata.testrunner.NoSQLTestRunner'


    SESSION_COOKIE_SECURE = True
    CSRF_COOKIE_SECURE = True
    SESSION_EXPIRE_AT_BROWSER_CLOSE = True

    SECURE_PROXY_SSL_HEADER = ('HTTP_X_FORWARDED_PROTOCOL', 'https')


Also the following should be modified only if you are certain of what you are doing.::

    TEMPLATE_LOADERS = (
        'django.template.loaders.filesystem.Loader',
        'django.template.loaders.app_directories.Loader',
        'django.template.loaders.eggs.Loader',
    )

    root_path = os.path.abspath(os.path.dirname(__file__))

    TEMPLATE_DIRS = (
        os.path.normpath(root_path+"/templates"),
    )


For the rest of the settings, please see https://docs.djangoproject.com/en/1.7/ref/settings/

Google APIs
+++++++++++

The user "registeration" requires a google client-id and requires that a Google project has Google plus API enabled, so that
user information can be accessed in order to add it to local database.
