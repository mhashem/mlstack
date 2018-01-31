# README #

This README would normally document whatever steps are necessary to get your application up and running.

## Quick summary ##

```mlstack``` is a backend service for machine learning basically face recognition services with multiple integrations 
with Microsoft Cognitive Services, Amazon AWS Rekognition, and Intel OpenCV.

* [Learn Markdown](https://bitbucket.org/tutorials/markdowndemo)

## Restful API ##

run training POST ```http://localhost:8082/mlstack/api/v1/notification/push?notificationType=1002```

run recognition POST ```http://localhost:8082/mlstack/api/v1/faces/recognition``` + targetImage as file

run index faces POST ```http://localhost:8082/mlstack/api/v1/faces/12/index?personName=Mahmoud``` + faceImage as file

## Setup ##

```mlstack``` makes use of the following: 

Dependency  | Usage
------------- | -------------
Java 8 | Programming Language
Spring / Spring Boot  | Web Framework
Mavne  | Dependency Management

## Configuration ##


## Dependencies ##

how to obtain a key for aws and cognitive services ...

## Database configuration ##

* Create a Postgresql database for example ( ```sql CREATE DATABASE "mlstack";```
* Create a user with password ```sql CREATE USER vagrant WITH PASSWORD '***';```
* Grant user all privileges to database GRANT ALL PRIVILEGES ON DATABASE "mlstack" to vagrant;
* Supply database config to mlstack configurations and it will handle table creation

## How to run tests ##

```test -P integration-tests```

## Deployment instructions ##

* Clone 
* ...

### Contribution guidelines ###

* Writing tests
* Code review

### Who do I talk to? ###

* Owner mhachem can be reached at mahmoudhashim.k@gmail.com