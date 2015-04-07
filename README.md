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

Copy the built artifacts into `$NUXEO_HOME/templates/custom/bundles/` and activate the "custom" template.

#### Configure the Datasource

Nuxeo jBPM relies on a Datasource `nxjbpm` which is already defined in a default Nuxeo 5.6 distribution.

## QA results

[![Build Status](https://qa.nuxeo.org/jenkins/buildStatus/icon?job=addons_nuxeo-platform-jbpm-master)](https://qa.nuxeo.org/jenkins/job/addons_nuxeo-platform-jbpm-master/)

# About Nuxeo

Nuxeo dramatically improves how content-based applications are built, managed and deployed, making customers more agile, innovative and successful. Nuxeo provides a next generation, enterprise ready platform for building traditional and cutting-edge content oriented applications. Combining a powerful application development environment with SaaS-based tools and a modular architecture, the Nuxeo Platform and Products provide clear business value to some of the most recognizable brands including Verizon, Electronic Arts, Netflix, Sharp, FICO, the U.S. Navy, and Boeing. Nuxeo is headquartered in New York and Paris. More information is available at www.nuxeo.com.
