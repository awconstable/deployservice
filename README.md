# deployservice
A deployments repository to measure team performance

[![CircleCI](https://circleci.com/gh/awconstable/deployservice.svg?style=shield)](https://circleci.com/gh/awconstable/deployservice)
![CodeQL](https://github.com/awconstable/deployservice/workflows/CodeQL/badge.svg)
[![codecov](https://codecov.io/gh/awconstable/deployservice/branch/main/graph/badge.svg)](https://codecov.io/gh/awconstable/deployservice)
[![Libraries.io dependency status for GitHub repo](https://img.shields.io/librariesio/github/awconstable/deployservice.svg)](https://libraries.io/github/awconstable/deployservice)
[![dockerhub](https://img.shields.io/docker/pulls/awconstable/deployservice.svg)](https://cloud.docker.com/repository/docker/awconstable/deployservice)

### Run app as a Docker container

*See https://github.com/docker-library/openjdk/issues/135 as to why spring.boot.mongodb.. env vars don't work*

```
docker stop deployservice
docker rm deployservice
docker pull awconstable/deployservice
docker run --name deployservice -d -p 8080:8080 --network <mongo network> -e spring_data_mongodb_host=<mongo host> -e spring_data_mongodb_port=<mongo port> -e spring_data_mongodb_database=<mondo db> -e server_ssl_key-store-type=<keystore type - PKCS12> -e server_ssl_key-store=/deployservice.p12 -e server_ssl_key-store-password=<password> -e server_ssl_key-alias=<alias> -e spring_cloud_consul_host=<consul host> -e spring_cloud_consul_port=<consul port> -v <cert path>:/deployservice.p12 awconstable/deployservice:latest
```

### Example deployment

```
  POST http://localhost:8088/api/v1/deployment
  Accept: application/json
  Cache-Control: no-cache
  Content-Type: application/json
  
  {
    "deploymentId": "d1",  
    "deploymentDesc": "deployment d1",
    "applicationId": "a1",  
    "rfcId": "rfc1",
    "created": "2020-11-30T13:00:00.000+00:00",
    "source": "test",
    "changes": [
      {
        "id": "c123",
        "created": "2020-11-20T12:00:00.000+00:00",
        "source": "test",
        "eventType": "test"
      },
      {
        "id": "c234",
        "created": "2020-11-20T12:00:00.000+00:00",
        "source": "test",
        "eventType": "test"
      }
    ]
  }
```

[Spring Boot Initilizr Config](https://start.spring.io/#!type=maven-project&language=java&platformVersion=2.4.0.RELEASE&packaging=jar&jvmVersion=11&groupId=team&artifactId=deployservice&name=deployservice&description=A%20deployments%20repository%20to%20measure%20team%20performance&packageName=team.deployservice&dependencies=devtools,lombok,web,data-mongodb,testcontainers,security,actuator,prometheus,cloud-starter-consul-discovery,cloud-starter-consul-config)