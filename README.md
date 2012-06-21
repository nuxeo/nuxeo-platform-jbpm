# Nuxeo jBPM

This addon provides an interface to manage workflows through the jBPM engine.

It was included by default up to Nuxeo 5.5, but since Nuxeo 5.6 it has been deprecated and moved
to a separate addon, and replaced by the new Document Routing module.


## Building and deploying

### How to build

You can build this module with:

    $ mvn clean install

### How to deploy

#### Deploy the module

Copy the `nuxeo-platform-jbpm-*.jar` JARs into your Nuxeo instance in `nxserver/bundles` and restart.

#### Configure the Datasource

Nuxeo jBPM relies on a Datasource `nxjbpm` which is not defined in a default distribution.
The easiest way to add it is to use the provided `jbpm` template. You need to copy the `templates/jbpm` folder into your Nuxeo instance:

    $ cp -r templates/jbpm $NUXEO_HOME/templates/

Edit the `bin/nuxeo.conf` file to deploy the `jbpm` template after the default configuration:

    nuxeo.templates=default,jbpm

Restart the Nuxeo instance.


## About Nuxeo

Nuxeo provides a modular, extensible Java-based [open source software platform for enterprise content management] [1] and packaged applications for [document management] [2], [digital asset management] [3] and [case management] [4]. Designed by developers for developers, the Nuxeo platform offers a modern architecture, a powerful plug-in model and extensive packaging capabilities for building content applications.

[1]: http://www.nuxeo.com/en/products/ep
[2]: http://www.nuxeo.com/en/products/document-management
[3]: http://www.nuxeo.com/en/products/dam
[4]: http://www.nuxeo.com/en/products/case-management

More information on: <http://www.nuxeo.com/>


