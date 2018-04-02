# Spring User Group - testcontainers demo
This repo contains code that will be presented at the Spring User Group meeting : Reliable and Portable Integration Testing with Spring

Features a simple service, and several different ways to build integration test cases for that service.  Testcontainers is used as the wrapper for postgres docker images that are the external system to perform integration tests against.  


## Postgres via Docker
The docker folder contains the definition for the postgres images.  

From the root of the project, run the following commands to...

__Build the postgres image with just the schema__

```
$ docker build -f docker/Dockerfile-base -t sug-testcontainers-demo-db:base ./docker
```

__and run that image on the default postgres port (5432)...__

```
$ docker-compose -f docker/docker-compose-base.yml up
```

---

__Build the postgres image with the schema and some test data__

```
$ docker build -f docker/Dockerfile-testdata -t sug-testcontainers-demo-db:testdata ./docker
```

__and run that image on the default postgres port (5432)...__

```
$ docker-compose -f docker/docker-compose-testdata.yml up
```

