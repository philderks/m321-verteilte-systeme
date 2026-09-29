# M321 – Verteilte Systeme

Umsetzung der [Eureka-Aufgaben vom 23.09.2026](https://bbw-it.github.io/321_main_rupe/03_Drehbuch/Drehbuch_Modul_321_FS26_23f_PR/#modul-321-23092026-woche-38) und der dazugehörigen Hausaufgaben. **Feign und Gateway (30.09.) sind noch nicht umgesetzt.**

## Projekte

| Ordner | Zweck | Adresse |
| --- | --- | --- |
| `eureka/` | Eureka Discovery Server | http://localhost:8761/ |
| `service01/` | Beispiel-API | http://localhost:8081/api |
| `service02/` | Ruft Service-01 über Eureka auf | http://localhost:8082/call |
| `react/` | Ruft Service-02 direkt aus dem Browser auf | http://localhost:5173/ |

Voraussetzungen: Java 21, Node.js 20.19+ oder 22.12+ und npm. Maven wird über die mitgelieferten Wrapper (`mvnw` bzw. `mvnw.cmd`) geladen.

## Starten

In **vier separaten Terminals** im Repo-Verzeichnis nacheinander starten:

```bash
cd eureka && ./mvnw spring-boot:run
cd service01 && ./mvnw spring-boot:run
cd service02 && ./mvnw spring-boot:run
cd react && npm ci && npm run dev
```

Unter Windows in den drei Java-Projekten `mvnw.cmd` statt `./mvnw` verwenden. Vor dem Start des nächsten Services jeweils warten, bis der vorherige bereit ist. Eureka registriert die Services und verteilt seine Registry periodisch; direkt nach dem Start kann `/call` kurzzeitig noch keine Instanz finden. Im Eureka-Dashboard sollten **SERVICE-01** und **SERVICE-02** als `UP` erscheinen.

React verwendet bewusst `http://localhost:8082/call` statt Eureka – wie in der Aufgabe gefordert. Service-02 erlaubt dafür CORS vom Vite-Dev-Server `http://localhost:5173`. Der Frontend-Aufruf setzt voraus, dass Browser und Backend auf demselben Rechner laufen.

## Testen

```bash
(cd eureka && ./mvnw verify)
(cd service01 && ./mvnw verify)
(cd service02 && ./mvnw verify)
(cd react && npm ci && npm test && npm run build)
```

Manuell prüfen: `/api` liefert `{"name":"Hello World from Service 01"}`; `/call` liefert `originalData`, `message` und `timestamp`. Die React-Seite zeigt die Antwort von Service-02 an.
