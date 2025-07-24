# ministore
Ministore project for development different functionality

```shell
docker run --name ministore-keycloak -p 8082:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin -v ./config/keycloak/import:/opt/keycloak/data/import quay.io/keycloak/keycloak:23.0.4 start-dev --import-realm
```

## Victoria metrics
```shell
docker run --name ministore-metrics -p 8428:8428 -v ./config/victoria-metrics/promscrape.yml:/promscrape.yml victoriametrics/victoria-metrics:v1.93.12 -promscrape.config=/promscrape.yml
```

## Grafana
```shell
docker run --name ministore-grafana -p 3000:3000 -v ./data/grafana:/var/lib/grafana -u "$(id -u)" grafana/grafana:10.2.4
```

## Grafana loki
```shell
docker run --name ministore-loki -p 3100:3100 grafana/loki:2.9.4
```

## Grafana tempo
```shell
docker run --name ministore-tracing -p 3200:3200 -p 9095:9095 -p 4317:4317 -p 4318:4318 -p 9411:9411 -p 14268:14268 -v ./config/tempo/tempo.yml:/etc/tempo.yml grafana/tempo:2.3.1 -config.file=/etc/tempo.yml
```
