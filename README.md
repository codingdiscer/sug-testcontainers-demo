# Spring User Group - testcontainers demo
This repo contains code that will be presented at the Spring User Group meeting : Reliable and Portable Integration Testing with Spring

Features a simple service, and several different ways to build integration test cases for that service.  [Testcontainers](https://www.testcontainers.org/) is used as the wrapper for postgres docker images that are the external system to perform integration tests against.  

The integration test classes are found [here](src/test/java/sug/testcontainers/demo/service).  

To run these test cases, you must have [Docker](https://www.docker.com/get-docker) installed and running.  To run 3 of the 5 test cases, you need to prepare two images locally.  Instructions for preparing the images are found below (only the _docker build..._ commands are necessary).  



## Postgres via Docker
The [docker](docker) folder contains the definition for the postgres images.  

From the root of the project, run the following commands to...

__Build the postgres image with just the schema...__

```
$ docker build -f docker/Dockerfile-base -t sug-testcontainers-demo-db:base ./docker
```

__...and run that image on the default postgres port of 5432__ (this step is not necessary to run the test cases)

```
$ docker-compose -f docker/docker-compose-base.yml up
```

---

__Build the postgres image with the schema and some test data...__

```
$ docker build -f docker/Dockerfile-testdata -t sug-testcontainers-demo-db:testdata ./docker
```

__...and run that image on the default postgres port of 5432__ (this step is not necessary to run the test cases)

```
$ docker-compose -f docker/docker-compose-testdata.yml up
```

