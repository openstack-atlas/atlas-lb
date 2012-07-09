### What is Atlas
-------------
Atlas is Openstack LoadBalancers API that is actively being developed.

### Wiki
--------
http://wiki.openstack.org/Atlas-LB

### Requirements
----------------
1. Java >= 1.5  (note: Java 1.7 seems to have a compatibility issue with JAXB, so until we fix this, better to be avoided).

2. Apache Maven == 2.2.1

3. Apache ActiveMQ == 5.5.0

    Run ActiveMQ on default port.

    `java -jar activemq/bin/run.jar start`

4. MySQL >= 5.x

    Create a MySQL database named 'openstack_atlas'

5. Glassfish == 3.0.1 (Optional as Atlas comes with an embedded jetty server)


### Getting Started for development
-----------------------------------
1. Build

    Grab the atlas-lb/core-api/core-public-web/src/deb/contrib/maven/settings.xml and put it inside your
    ~/.m2 directory:

    `cd atlas-lb`

    `./bin/build.sh`

2. Configure

    Copy all of the configuration files from atlas-lb/core-api/core-public-web/src/deb/contrib/config
    and put it under your /etc/openstack/atlas directory. 
    
    Make sure you update /etc/openstack/atlas/public-api.conf with:
    
      * your database username/password.
      * If you are not using null (fake) adapter which is the default one configured, choose another adapter.
    

3. Start atlas:

    `./bin/run.sh`

    To run the app into debug mode on port 8080 with embedded jetty:

    `./bin/debug.sh`

4. If atlas is properly started, it should have created the necessary database tables for you. Seed the 'openstack_atlas'
    database with some fake data (cluster, hosts, virtual ips, etc.). A sample for testing is here: atlas-lb/core-api/core-public-web/src/deb/contrib/db/ directory

Now you can access the Atlas REST API eg. do a GET on [http://localhost:8080/v1.1/1000/loadbalancers](http://localhost:8080/v1.1/1000/loadbalancers)
where 1000 is a tenant_id. What does it return? May be its time to do a POST. More more operations, [http://wiki.openstack.org/Atlas-LB](http://wiki.openstack.org/Atlas-LB)


### Deploy under Glassfish Application Server
---------------------------------------------

Alternative to the step 3 above, if you want to use an Application Server like Glassfish instead of the embedded jetty, you could:

    /glassfish/glassfish/bin/asadmin start-domain

    cp atlas-lb/core-api/core-public-web/target/core-public-web-1.1.0-SNAPSHOT.war /glassfish/glassfish/domains/domain1/autodeploy



For more information about Openstack, go to [Openstack.org](http://openstack.org)

