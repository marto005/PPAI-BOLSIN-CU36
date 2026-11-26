# PPAI DSI 2026 · Bolsines · CU 36 Consultar seguimiento de bolsines

Implementación del CU 36 rediseñado con el patrón **Adapter** (GoF), para la 2ª entrega del PPAI (grupo 9, 3K6).

| | |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.3.6 (Spring MVC + Spring Data JPA) |
| Tecnología | Web (Thymeleaf; mapa con Leaflet + OpenStreetMap) |
| Base de datos | H2 embebida en archivo (`./data/bolsines.mv.db`), con JPA/Hibernate |
| API externa | API mock del GPS Tracker de la cátedra (`dsi-api-bolsines`) |

## Cómo levantarlo

1. **API mock del GPS Tracker** (puerto 8080). En la carpeta `dsi-api-bolsines-main`:
   ```
   mvnw.cmd spring-boot:run        (Windows)
   ./mvnw spring-boot:run          (Linux/Mac)
   ```
2. **Esta aplicación** (puerto 8081). En esta carpeta:
   ```
   mvnw.cmd spring-boot:run        (Windows)
   ./mvnw spring-boot:run          (Linux/Mac)
   ```
   También se puede abrir con IntelliJ y ejecutar `BolsinesApplication`.
3. Abrir **http://localhost:8081** e iniciar sesión.

### Si aparece "no se ha encontrado o cargado la clase principal"

Es un bug de `spring-boot:run` en Windows (Spring Boot 3.3.0 a 3.3.5) cuando la ruta del proyecto tiene "ñ" o tildes, por ejemplo "3er año" o "Documentación". Este proyecto ya usa la 3.3.6, que lo corrige. Si igual pasa, hay dos alternativas:

- Generar el .jar y ejecutarlo desde esta carpeta:
  ```
  mvnw.cmd -DskipTests package
  java -jar target\ppai-bolsines-cu36-1.0.0.jar
  ```
- Copiar el proyecto a una ruta sin espacios, tildes ni "ñ" (por ejemplo `C:\dev\ppai-bolsines-cu36`).

La API mock de la cátedra usa Spring Boot 3.3.5, así que puede fallar igual si está en una ruta así. Se resuelve de la misma manera: con `java -jar` sobre el .jar que queda en su carpeta `target`, o moviéndola a una ruta simple.

La primera vez que se levanta se crea la base y se cargan los datos de prueba (`CargaDatosIniciales`).
Para volver a los datos originales: cerrar la app y borrar la carpeta `data/`.

Consola de la base: **http://localhost:8081/h2-console**. JDBC URL `jdbc:h2:file:./data/bolsines`, usuario `sa`, sin contraseña.

## Usuarios de prueba (contraseña `1234`)

| Usuario | CM | Para mostrar |
|---|---|---|
| `eb.cordoba` | CM Córdoba Capital (CM-CBA-01) | flujo principal, A2, A3, A4, A5 |
| `eb.rosario` | CM Rosario (CM-ROS-02) | un bolsín que se ubica con los 3 modelos |
| `eb.jujuy` | CM Jujuy (CM-JUJ-01) | A1: sin bolsines enviados |

## Cambiar el modelo de GPS Tracker (para mostrar los 3 adaptadores)

El sistema usa **un modelo de GPS Tracker para todo el sistema**, igual que en los diagramas.
Se elige en `src/main/resources/application.properties`:

```
gps.tracker.modelo=XTR-4500L          # o NavTrack QX-7A, o GeoPulse MTR-900
```

Después de cambiarlo hay que reiniciar la app. También se puede elegir al arrancar, sin editar el archivo:

```
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--gps.tracker.modelo=GeoPulse-MTR-900"
```

En PowerShell todo el `-D...` va entre comillas, y el modelo se escribe sin espacios (`XTR-4500L`, `NavTrack-QX-7A`, `GeoPulse-MTR-900`): el sistema ignora guiones, espacios y mayúsculas al comparar el nombre.

El modelo activo aparece arriba a la derecha de la pantalla del CU.

## Qué se ve con cada modelo (usuario `eb.cordoba`)

La CM Córdoba Capital tiene 3 bolsines en estado *Enviado*: 101, 110 y 111.

| Bolsín | XTR-4500L (valida CM origen) | NavTrack QX-7A (valida CM destino) | GeoPulse MTR-900 (solo número) |
|---|---|---|---|
| 101 | ✅ en el mapa | ✅ en el mapa | ✅ en el mapa |
| 110 | ❌ A3: la API lo tiene con CM origen CM-CBA-02 | ✅ en el mapa | ✅ en el mapa |
| 111 | ❌ A3: no existe en la API | ❌ A3 | ❌ A3 |

El bolsín 110 se cargó a propósito con CM origen CM-CBA-01. Así se ve que cada adaptador traduce la consulta según las reglas de su modelo.

## Guion de prueba de los flujos

- **Flujo principal:** `eb.cordoba` → *Consultar seguimiento de bolsines*. Se ve la CM, el mapa con los bolsines y la grilla.
  Hacé clic en un marcador → *Seleccionar este bolsín* → *Sí, enviar correo*. El correo (simulado) se imprime en la consola de la app.
- **A1:** entrar con `eb.jujuy`.
- **A2:** filtrar por un precinto que no existe, por ejemplo `PR-9999`.
- **A3:** con `eb.cordoba` aparecen los avisos de los bolsines que el GPS Tracker no pudo ubicar.
  Si la API mock está apagada, ninguno se puede ubicar.
- **A4:** botón *Cancelar*.
- **A5:** seleccionar un bolsín → *No enviar*.

## Dónde está el patrón Adapter

Paquete `ar.edu.utn.frc.dsi.bolsines.gps`, más el gestor y la boundary con el GPS Tracker. El método de enganche es `solicitarDatosUbicacionBolsines()` del gestor.

| Rol en el patrón | Clase |
|---|---|
| Cliente | `control.GestorConsultarSeguimientoBolsines` |
| Boundary con el sistema externo (crea y usa el adaptador) | `boundary.InterfazGPSTracker` |
| Objetivo (Target) | `IAdaptadorGPSTracker` |
| Adaptador (Adapter) | `AdaptadorXTR4500L`, `AdaptadorNavTrackQX7A`, `AdaptadorGeoPulseMTR900` |
| Adaptable (Adaptee) | `ServicioGPSTrackerXTR4500L`, `ServicioGPSTrackerNavTrackQX7A`, `ServicioGPSTrackerGeoPulseMTR900` |
| Dato adaptado (formato común) | `String[]` → `[0]` número de bolsín, `[1]` latitud, `[2]` longitud, `[3]` fecha y hora de la última actualización (`dd/MM/yyyy HH:mm:ss`) |

## Estructura

```
src/main/java/ar/edu/utn/frc/dsi/bolsines
├── boundary/     PantallaConsultaSeguimientoBolsines, InterfazGPSTracker, InterfazServidorMapas, InterfazEmail, PantallaInicio
├── control/      GestorConsultarSeguimientoBolsines, GestorNotificarUbicacionBolsin (CU 31)
├── entidad/      Bolsin, CambioEstadoBolsin, Estado, ComisionMedica, Empleado, Cargo, Usuario, Sesion
├── gps/          patrón Adapter (ver arriba)
├── repositorio/  repositorios Spring Data JPA
└── config/       ConfiguracionGPSTracker, CargaDatosIniciales
```

Decisiones de implementación:
- El servidor de mapas es OpenStreetMap con Leaflet, en lugar de Google Maps, porque no requiere API key. Leaflet está incluido en el proyecto. Las teselas del mapa necesitan internet.
- El envío de correo del CU 31 está simulado: se escribe en la consola.
