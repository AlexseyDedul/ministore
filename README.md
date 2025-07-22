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
