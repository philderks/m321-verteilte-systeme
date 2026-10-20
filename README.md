# M321 – Verteilte Systeme

Umsetzung der [Eureka-, Feign- und Gateway-Aufgaben](https://bbw-it.github.io/321_main_rupe/03_Drehbuch/Drehbuch_Modul_321_FS26_23f_PR/) für 5IA23f. Die Übungen zu Microservice-Patterns vom 21.10. sind ein separates Unterrichtsthema.

## Aufbau

| Projekt | Zweck | Adresse |
| --- | --- | --- |
| `eureka/` | Service Discovery | http://localhost:8761/ |
| `service01/` | Beispiel-API und Bücher (In-Memory) | http://localhost:8081/api, http://localhost:8081/books |
| `service02/` | `/call` über Eureka/RestTemplate, `/books` über Eureka/Feign | http://localhost:8082/call, http://localhost:8082/books |
| `gateway/` | API-Gateway mit Eureka-Routing und Browser-CORS | http://localhost:8080/ |
| `react/` | Zeigt beide Antworten über das Gateway | http://localhost:5173/ |

Java 21 und Node.js 20.19+ oder 22.12+ mit npm werden benötigt. Maven wird über die Wrapper (`./mvnw` bzw. `mvnw.cmd`) gestartet. Das Gateway verwendet wie die vorhandenen Projekte Spring Boot 4.0.3 / Spring Cloud 2025.1.3 und den passenden WebFlux-Gateway-Starter. Die Kursseite nennt für ein neu erzeugtes Gateway-Projekt Spring Boot 3.5.12; diese Implementierung ist stattdessen mit der bereits verwendeten Boot-4-Kombination getestet.

## Starten

Vom Repo-Verzeichnis aus in **fünf separaten Terminals**, der Reihe nach:

```bash
(cd eureka && ./mvnw spring-boot:run)
(cd service01 && ./mvnw spring-boot:run)
(cd service02 && ./mvnw spring-boot:run)
(cd gateway && ./mvnw spring-boot:run)
(cd react && npm ci && npm run dev)
```

Unter Windows in den Java-Projekten `mvnw.cmd` statt `./mvnw` verwenden. Vor dem nächsten Dienst jeweils warten, bis der vorherige bereit ist. Eureka und die Clients aktualisieren ihre Registries periodisch: direkt nach dem Start kann das Gateway kurzzeitig 503 liefern; im Eureka-Dashboard sollten **SERVICE-01**, **SERVICE-02** und **GATEWAY** als `UP` erscheinen.

**Datenfluss:** React ruft `GET http://localhost:8080/api/call` und `GET http://localhost:8080/api/books` auf. Die Gateway-Route `Path=/api/**` entfernt mit `StripPrefix=1` das Segment `/api` und leitet über `lb://service-02` an dessen `/call` bzw. `/books` weiter. Service-02 erreicht Service-01 für Bücher mit `@FeignClient("service-01")`. Eine zweite Gateway-Route `Path=/service-01/**` leitet nach Entfernen des Präfixes direkt an `lb://service-01` weiter, z. B. `/service-01/books`. Nur der Origin `http://localhost:5173` ist am Gateway für Browser-CORS erlaubt. Alle Dienste müssen auf demselben Rechner laufen, wenn das Frontend `localhost` verwendet.

**Zusatzaufgabe:** `POST /books` auf Service-01 fügt ein Buch zur In-Memory-Liste hinzu; Service-02 reicht `POST /books` via Feign weiter. Über das Gateway: `POST http://localhost:8080/api/books` mit JSON `{"id":5,"title":"Test Book","author":"Test Author"}`. Die Daten gehen beim Neustart von Service-01 verloren. Die React-Oberfläche zeigt die Bücher an, bietet aber kein Eingabeformular; ein POST kann z. B. mit curl oder einer HTTP-Datei erfolgen.

## Testen

```bash
(cd eureka && ./mvnw verify)
(cd service01 && ./mvnw verify)
(cd service02 && ./mvnw verify)
(cd gateway && ./mvnw verify)
(cd react && npm ci && npm test && npm run build)
```

Bei laufenden Diensten:

```bash
curl http://localhost:8080/api/call
curl http://localhost:8080/api/books
curl http://localhost:8080/service-01/books
curl -i -H 'Origin: http://localhost:5173' http://localhost:8080/api/books
curl -i -X POST http://localhost:8080/api/books -H 'Content-Type: application/json' -d '{"id":5,"title":"Test Book","author":"Test Author"}'
```
