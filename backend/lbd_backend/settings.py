"""
Django settings for mysite project.

For more information on this file, see
https://docs.djangoproject.com/en/1.6/topics/settings/

For the full list of settings and their values, see
https://docs.djangoproject.com/en/1.6/ref/settings/
"""

# Build paths inside the project like this: os.path.join(BASE_DIR, ...)
import os
import socket
import mongoengine

BASE_DIR = os.path.dirname(os.path.dirname(__file__))


# openshift is our PAAS for now.
ON_PAAS = 'OPENSHIFT_REPO_DIR' in os.environ


# Quick-start development settings - unsuitable for production
# See https://docs.djangoproject.com/en/1.6/howto/deployment/checklist/

if ON_PAAS:
    SECRET_KEY = os.environ['OPENSHIFT_SECRET_TOKEN']
else:
    # SECURITY WARNING: keep the secret key used in production secret!
    SECRET_KEY = ')_7av^!cy(wfx=k#3*7x+(=j^fzv+ot^1@sh9s9t=8$bu@r(z$'

# SECURITY WARNING: don't run with debug turned on in production!
# adjust to turn off when on Openshift, but allow an environment variable to override on PAAS
DEBUG = not ON_PAAS
DEBUG = DEBUG or 'DEBUG' in os.environ
if ON_PAAS and DEBUG:
    print("*** Warning - Debug mode is on ***")

TEMPLATE_DEBUG = True

if ON_PAAS:
    ALLOWED_HOSTS = [os.environ['OPENSHIFT_APP_DNS'], socket.gethostname()]
else:
    ALLOWED_HOSTS = []


# Application definition

INSTALLED_APPS = (
    'django.contrib.admin',
    'django.contrib.auth',
    'django.contrib.contenttypes',
    'django.contrib.sessions',
    'django.contrib.messages',
#    'django.contrib.staticfiles',
    'lbd_backend.LBD_REST_locationdata',
)

MIDDLEWARE_CLASSES = (
    'django.contrib.sessions.middleware.SessionMiddleware',
    'django.middleware.common.CommonMiddleware',
#    'django.middleware.csrf.CsrfViewMiddleware',
    'django.contrib.auth.middleware.AuthenticationMiddleware',
    'django.contrib.messages.middleware.MessageMiddleware',
    'django.middleware.clickjacking.XFrameOptionsMiddleware',
)

ROOT_URLCONF = 'lbd_backend.urls'

WSGI_APPLICATION = 'lbd_backend.wsgi.application'


# Database
# https://docs.djangoproject.com/en/1.6/ref/settings/#databases

if ON_PAAS:
    DATABASES = {
        'default': {
            'ENGINE': '',                   
        },
    }
    mongoengine.connect(os.environ['OPENSHIFT_APP_NAME'],
        username=os.environ['OPENSHIFT_MONGODB_DB_USERNAME'],
        password=os.environ['OPENSHIFT_MONGODB_DB_PASSWORD'],
        host=os.environ['OPENSHIFT_MONGODB_DB_HOST'],
        port=int(os.environ['OPENSHIFT_MONGODB_DB_PORT']))

else:
    # stock django
    DATABASES = {
        'default': {
            'ENGINE': '',
        }
    }
    mongoengine.connect("lbd_backend", host="127.0.0.1:27017")

# Internationalization
# https://docs.djangoproject.com/en/1.6/topics/i18n/

LANGUAGE_CODE = 'en-us'

TIME_ZONE = 'UTC'

USE_I18N = True

USE_L10N = True

USE_TZ = True


# Static files (CSS, JavaScript, Images)
# https://docs.djangoproject.com/en/1.6/howto/static-files/
#STATIC_URL = '/static/'
#STATIC_ROOT = os.path.join(BASE_DIR, 'wsgi','static')
#STATICFILES_FINDERS = (
#    'django.contrib.staticfiles.finders.FileSystemFinder',
#    'django.contrib.staticfiles.finders.AppDirectoriesFinder',
#
#)

AUTHENTICATION_BACKENDS = (
                            'mongoengine.django.auth.MongoEngineBackend',
                            )
SESSION_ENGINE = 'mongoengine.django.sessions'

