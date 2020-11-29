# deployservice
A deployments repository to measure team performance

### Run app as a Docker container

*See https://github.com/docker-library/openjdk/issues/135 as to why spring.boot.mongodb.. env vars don't work*

```
docker stop deployservice
docker rm deployservice
docker pull awconstable/deployservice
docker run --name deployservice -d -p 8080:8080 --network <mongo network> -e spring_data_mongodb_host=<mongo host> -e spring_data_mongodb_port=<mongo port> -e spring_data_mongodb_database=<mondo db> -e server_ssl_key-store-type=<keystore type - PKCS12> -e server_ssl_key-store=/deployservice.p12 -e server_ssl_key-store-password=<password> -e server_ssl_key-alias=<alias> -e spring_cloud_consul_host=<consul host> -e spring_cloud_consul_port=<consul port> -v <cert path>:/deployservice.p12 awconstable/deployservice:latest
```

[Spring Boot Initilizr Config](https://start.spring.io/#!type=maven-project&language=java&platformVersion=2.4.0.RELEASE&packaging=jar&jvmVersion=11&groupId=team&artifactId=deployservice&name=deployservice&description=A%20deployments%20repository%20to%20measure%20team%20performance&packageName=team.deployservice&dependencies=devtools,lombok,web,data-mongodb,testcontainers,security,actuator,prometheus,cloud-starter-consul-discovery,cloud-starter-consul-config)