## Spring Cloud Consul

Preview of Spring Cloud Consul implementation

### Short consul overview

consul does

* distributed configuration
* service registration and discovery
* messaging
* distributed locking and sessions
* supports multiple data centers
* has a slick ui

See the [intro](https://consul.io/intro/index.html) for more information.

### Running the sample

1. [Install consul](https://consul.io/downloads.html)
2. Run `./run_consul.sh` found in the root of this project
3. verify consul is running by visiting [http://localhost:8500](http://localhost:8500)
4. run `mvn --settings .settings.xml package` this will bring in the required spring cloud maven repositories and build
5. run `java -jar spring-cloud-consul-sample/target/spring-cloud-consul-sample-1.0.0.BUILD-SNAPSHOT.jar`
6. visit [http://localhost:8080](http://localhost:8080), verify that `{"serviceId":"<yourhost>:8080","address":"<yourhost>","port":8080}` results
7. run `java -jar spring-cloud-consul-sample/target/spring-cloud-consul-sample-1.0.0.BUILD-SNAPSHOT.jar --server.port=8081`
8. visit [http://localhost:8080](http://localhost:8080) again, verify that `{"serviceId":"<yourhost>:8081","address":"<yourhost>","port":8081}` eventually shows up in the results in a round robbin fashion (may take a minute or so).
