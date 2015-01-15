Installation
============

This chapter describes where one can download the software and libraries required by the back-end and how they can be
installed.

MongoDB
-------

MongoDB can be downloaded from http://www.mongodb.com/downloads for different operating systems. For Linux distributions
it might be available from package management system (e.g. Portage on Gentoo) depending on the architecture running the operating
system.

MongoDB installation guides for different operating systems can be found from http://docs.mongodb.org/manual/installation/ .


Python
------

Python can be downloaded from https://www.python.org/downloads/ for multiple operating systems. As with MongoDB, on Linux
it can also be installed with package manager.


Pip
^^^

PIP is recommended for easy installation of Python libraries. Now for those using the latest versions of Python, there
are some good news, as PIP comes with the installation and on Linux of course it might be installed by the package manager.
However, if you are not lucky enough to have **Python 2.7.9** or running Linux system, you need to do this yourself.

So... if you have Python <= 2.7.8 or otherwise do not have PIP yet, follow the instructions at https://pip.pypa.io/en/latest/installing.html .

After the installation, the latest versions of libraries can be installed by running::

    pip install <package name>

or specific version::

    pip install <package name>==x.y.z


Django
------

The recommended way to install django in all operating systems is through PIP::

    pip install django

Of course nothing prohibits one to install it with Linux package manager or otherwise. More on django installation at
https://docs.djangoproject.com/en/1.7/topics/install/ .