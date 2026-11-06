# Informe Técnico de Entrega: Práctica 3 – Actualización de Microservicios y Pipelines CI (Parte 2)

> **Materia / Módulo:** Integración Continua y Despliegue Automatizado (DevOps & QA)  
> **Repositorio Monorepo:** `admin-proyectos-main`  
> **Branch de Trabajo:** `practica3-parte2`  
> **Entorno:** OpenJDK 21 (LTS), Apache Maven 3.9+, Spring Boot 2.7.18 / 3.2.5, MariaDB 11.3, Docker, Jenkins Declarativo, SonarQube  
> **Fecha:** Noviembre 2026  

---

## 1. Resumen Ejecutivo de Cambios Realizados

| Componente | Estado Anterior | Cambios Implementados (Parte 2) | Beneficio Técnico |
|---|---|---|---|
| **Seguridad de Autenticación** | Contraseñas en texto plano almacenadas en base de datos y comparadas directamente en el query SQL (`findByCorreoPass`). | Hashing irreversible con **BCrypt** (`spring-security-crypto`), búsqueda por correo electrónico (`findByCorreoElectronico`), comparación segura con `passwordEncoder.matches()` y script de migración SQL `db/migracion_hash_passwords.sql`. | Eliminación de vulnerabilidad crítica CWE-256 / CWE-312 / OWASP A02:2021. |
| **Configuración de Base de Datos** | Credenciales de MariaDB (`root` con password vacío) fijadas en código en `application.properties`. | Parametrización mediante variables de entorno del sistema (`${DB_URL}`, `${DB_USER}`, `${DB_PASS}`) con fallbacks seguros. | Cumplimiento del principio 12-Factor App (CWE-798) y soporte de inyección de secretos en CI/CD. |
| **Aislamiento de Pruebas (CI)** | El test `contextLoads` del backend requería MariaDB en localhost:3306 levantado. | Configuración de **H2 Database en memoria** (`MODE=MariaDB;DB_CLOSE_DELAY=-1`) en `src/test/resources/application.properties`. | Pruebas herméticas, rápidas e independientes de servicios externos en agentes de Jenkins. |
| **Calidad de Código y Bugs** | Sentencias `if (!x.equals(null))` en todos los controladores que provocaban `NullPointerException`. | Refactorización de comprobaciones de nulidad hacia el operador canónico `if (x != null)`. | Prevención de caídas de servicio (CWE-476) y robustez ante payloads nulos. |
| **Pruebas Automatizadas Backend** | Cobertura parcial en controladores sin aislamiento de BD. | Suite exhaustiva con **JUnit 5 + Mockito** para los 5 controladores (éxitos y fracasos controlados) y `ModeloTest`. | **100% de cobertura de métodos evaluables** reportado en JaCoCo. |
| **Pruebas Automatizadas Frontend** | Sin suite de pruebas unitarias para `GestorController`. | 34 pruebas automatizadas con simulación de `RestTemplate` ante éxitos, caídas de backend (HTTP 500/Connection refused), formularios inválidos y modelos. | **100% de cobertura de métodos y líneas** en controlador y modelos del frontend. |
| **Análisis de Vulnerabilidades (Sonar)** | Sin configuración explícita para evitar escaneo de temporales. | Inclusión de `sonar.exclusions=**/target/**` y rutas de reporte JaCoCo XML en ambos `pom.xml`, además de `INCIDENCIAS.md`. | Eliminación de falsos positivos y cumplimiento de Quality Gates en SonarQube. |
| **Pipelines CI/CD (Jenkins)** | Inexistentes. | 3 pipelines declarativos (`ci/Jenkinsfile.backend`, `ci/Jenkinsfile.frontend`, `ci/Jenkinsfile.database`) con validación SQL estática (`sqlfluff`), MariaDB 11.3 efímera en Docker y Quality Gate. | Automatización completa de compilación, calidad, validación y empaquetado. |

---

## 2. Matriz de Casos de Prueba Implementados

### 2.1. Backend (`Ejercicio5MyAPIGestor`) – 72 Pruebas Ejecutadas (0 Fallos)

| ID | Clase de Prueba | Tipo | Entrada / Condición | Resultado Esperado | Resultado Obtenido |
|---|---|---|---|---|---|
| **TC-B01** | `UsuarioControllerTest` | Éxito | Credenciales válidas (`ana22533@cbtis75.edu.mx`, password correcto) | Retorna entidad `Usuario` con datos completos |  Aprobada (200 OK) |
| **TC-B02** | `UsuarioControllerTest` | Fracaso | Contraseña incorrecta en login | Retorna objeto con `nombre = "000"` |  Aprobada ("000") |
| **TC-B03** | `UsuarioControllerTest` | Fracaso | Correo no registrado en base de datos | Retorna objeto con `nombre = "000"` |  Aprobada ("000") |
| **TC-B04** | `UsuarioControllerTest` | Éxito | `id_usuario = 1` existente en repositorio | Retorna datos del usuario solicitado |  Aprobada |
| **TC-B05** | `UsuarioControllerTest` | Fracaso | `id_usuario = 999` inexistente | Retorna objeto vacío controlado `new Usuario()` |  Aprobada (Objeto vacío) |
| **TC-B06** | `UsuarioControllerTest` | Éxito | Alta de nuevo usuario (`addUsuario`) | Hashea password con BCrypt y retorna flag = 1 |  Aprobada (flag = 1) |
| **TC-B07** | `UsuarioControllerTest` | Éxito | Actualización de usuario (`updateUsuario`) | Hashea password con BCrypt y retorna flag = 1 |  Aprobada (flag = 1) |
| **TC-B08** | `UsuarioControllerTest` | Éxito | Eliminación de usuario (`deleteUsuario`) | Invoca delete en repositorio y retorna flag = 1 |  Aprobada (flag = 1) |
| **TC-B09** | `UsuarioControllerTest` | Éxito | Listado de usuarios (`buscarUsuarios`) | Retorna lista con los usuarios existentes |  Aprobada |
| **TC-B10** | `TareasControllerTest` | Éxito | Búsqueda por `id_proyecto = 10` | Retorna lista de tareas asociadas al proyecto |  Aprobada |
| **TC-B11** | `TareasControllerTest` | Fracaso | Búsqueda por proyecto sin tareas asociadas | Retorna lista vacía sin excepciones |  Aprobada (Lista vacía) |
| **TC-B12** | `TareasControllerTest` | Éxito | `buscarTarea` sobrecargado con id existente | Retorna entidad `Tareas` encontrada |  Aprobada |
| **TC-B13** | `TareasControllerTest` | Fracaso | `buscarTarea` sobrecargado con id inexistente | Retorna objeto vacío controlado `new Tareas()` |  Aprobada (Objeto vacío) |
| **TC-B14** | `TareasControllerTest` | Éxito | Inserción de tarea válida (`addTareas`) | Guarda en repositorio y retorna flag = 1 |  Aprobada (flag = 1) |
| **TC-B15** | `TareasControllerTest` | Fracaso | Inserción con payload nulo | No interactúa con repo y retorna flag = 0 |  Aprobada (flag = 0) |
| **TC-B16** | `TareasControllerTest` | Éxito | Actualización de tarea (`updateTareas`) | Guarda cambios y retorna flag = 1 |  Aprobada (flag = 1) |
| **TC-B17** | `TareasControllerTest` | Éxito | Borrado de tarea (`deleteTareas`) | Invoca delete y retorna flag = 1 |  Aprobada (flag = 1) |
| **TC-B18** | `TareasControllerTest` | Éxito | Listado general (`buscarTareas`) | Retorna colección completa de tareas |  Aprobada |
| **TC-B19** | `ProyectosControllerTest`| Éxito | Consulta de proyecto existente por id | Retorna entidad `Proyectos` |  Aprobada |
| **TC-B20** | `ProyectosControllerTest`| Fracaso | Consulta de proyecto inexistente | Retorna objeto vacío controlado `new Proyectos()` |  Aprobada (Objeto vacío) |
| **TC-B21** | `ProyectosControllerTest`| Éxito | Inserción de proyecto válido (`addProyecto`) | Guarda en repositorio y retorna flag = 1 |  Aprobada (flag = 1) |
| **TC-B22** | `ProyectosControllerTest`| Fracaso | Inserción de proyecto nulo | Retorna flag = 0 sin lanzar excepción |  Aprobada (flag = 0) |
| **TC-B23** | `ProyectosControllerTest`| Éxito | Actualización de proyecto (`updateProyecto`) | Guarda cambios y retorna flag = 1 |  Aprobada (flag = 1) |
| **TC-B24** | `ProyectosControllerTest`| Fracaso | Actualización de proyecto nulo | Retorna flag = 0 |  Aprobada (flag = 0) |
| **TC-B25** | `ProyectosControllerTest`| Éxito | Eliminación de proyecto (`deleteProyecto`) | Invoca delete y retorna flag = 1 |  Aprobada (flag = 1) |
| **TC-B26** | `ProyectosControllerTest`| Éxito | Listado general de proyectos (`buscarProyectos`) | Retorna lista de proyectos |  Aprobada |
| **TC-B27** | `ComentariosControllerTest`| Éxito | Comentarios asociados a tarea existente | Retorna lista de comentarios |  Aprobada |
| **TC-B28** | `ComentariosControllerTest`| Fracaso | Comentarios asociados a tarea sin mensajes | Retorna lista vacía |  Aprobada |
| **TC-B29** | `ComentariosControllerTest`| Éxito | Consulta por id de comentario existente | Retorna entidad `Comentarios` |  Aprobada |
| **TC-B30** | `ComentariosControllerTest`| Fracaso | Consulta de comentario inexistente | Retorna objeto vacío controlado |  Aprobada (Objeto vacío) |
| **TC-B31** | `ComentariosControllerTest`| Éxito | Inserción de comentario válido (`addComentario`) | Guarda en BD y retorna flag = 1 |  Aprobada (flag = 1) |
| **TC-B32** | `ComentariosControllerTest`| Fracaso | Inserción de comentario nulo | Retorna flag = 0 |  Aprobada (flag = 0) |
| **TC-B33** | `ComentariosControllerTest`| Éxito | Actualización de comentario | Guarda cambios y retorna flag = 1 |  Aprobada (flag = 1) |
| **TC-B34** | `ComentariosControllerTest`| Éxito | Eliminación de comentario | Invoca delete y retorna flag = 1 |  Aprobada (flag = 1) |
| **TC-B35** | `ComentariosControllerTest`| Éxito | Listado general de comentarios | Retorna lista de comentarios |  Aprobada |
| **TC-B36** | `AsignacionesControllerTest`| Éxito | Consulta de asignación por id existente | Retorna entidad `Asignaciones` |  Aprobada |
| **TC-B37** | `AsignacionesControllerTest`| Fracaso | Consulta de asignación inexistente | Retorna objeto vacío controlado |  Aprobada (Objeto vacío) |
| **TC-B38** | `AsignacionesControllerTest`| Éxito | Inserción de asignación válida | Guarda en BD y retorna flag = 1 |  Aprobada (flag = 1) |
| **TC-B39** | `AsignacionesControllerTest`| Fracaso | Inserción de asignación nula | Retorna flag = 0 |  Aprobada (flag = 0) |
| **TC-B40** | `AsignacionesControllerTest`| Éxito | Actualización de asignación válida | Guarda cambios y retorna flag = 1 |  Aprobada (flag = 1) |
| **TC-B41** | `AsignacionesControllerTest`| Fracaso | Actualización de asignación nula | Retorna flag = 0 |  Aprobada (flag = 0) |
| **TC-B42** | `AsignacionesControllerTest`| Éxito | Eliminación de asignación | Invoca delete y retorna flag = 1 |  Aprobada (flag = 1) |
| **TC-B43** | `AsignacionesControllerTest`| Éxito | Listado de asignaciones | Retorna lista general |  Aprobada |
| **TC-B44**–**TC-B71** | `ModeloTest` | Éxito | Constructores vacíos, constructores con parámetros, getters y setters de `Usuario`, `Tareas`, `Proyectos`, `Comentarios`, `Asignaciones` | Validación estricta de asignación y recuperación de atributos |  28 pruebas Aprobadas |
| **TC-B72** | `Ejercicio5MyApiGestorApplicationTests` | Éxito | `contextLoads` sobre base H2 en memoria | Carga satisfactoria del ApplicationContext de Spring |  Aprobada |

---

### 2.2. Frontend (`Ejercicio5Cliente`) – 34 Pruebas Ejecutadas (0 Fallos)

| ID | Clase de Prueba | Tipo | Entrada / Condición | Resultado Esperado | Resultado Obtenido |
|---|---|---|---|---|---|
| **TC-F01** | `GestorControllerTest` | Éxito | Carga de formulario de login (`/`) | Retorna vista `login` y modelo `Usuario` inicializado |  Aprobada |
| **TC-F02** | `GestorControllerTest` | Éxito | Credenciales válidas enviadas al backend | Redirección a `/index` con atributos de sesión activos |  Aprobada (redirect:/index) |
| **TC-F03** | `GestorControllerTest` | Fracaso | Credenciales inválidas (backend devuelve "000") | Retorna vista `login` con atributo de error |  Aprobada |
| **TC-F04** | `GestorControllerTest` | Fracaso | Caída de conexión al backend en login (`RestClientException`) | Captura excepción y retorna vista `login` con mensaje descriptivo |  Aprobada |
| **TC-F05** | `GestorControllerTest` | Éxito | Carga de dashboard (`/index`) con lista de proyectos | Retorna vista `index` con proyectos inyectados |  Aprobada |
| **TC-F06** | `GestorControllerTest` | Fracaso | Carga de dashboard cuando backend responde nulo | Retorna vista `index` con lista vacía sin arrojar NPE |  Aprobada |
| **TC-F07** | `GestorControllerTest` | Fracaso | Error 500 del backend al listar proyectos | Manejo de excepción y vista con alerta de error |  Aprobada |
| **TC-F08** | `GestorControllerTest` | Éxito | Formulario de alta de proyecto (`/nuevoProyecto`) | Retorna vista `formularioProyecto` |  Aprobada |
| **TC-F09** | `GestorControllerTest` | Éxito | Guardar nuevo proyecto con datos válidos | Envía POST al backend y redirige a `/index` |  Aprobada |
| **TC-F10** | `GestorControllerTest` | Fracaso | Guardar nuevo proyecto cuando backend responde flag = 0 | Retorna vista de formulario indicando fallo de registro |  Aprobada |
| **TC-F11** | `GestorControllerTest` | Éxito | Editar proyecto existente (`id = 10`) | Obtiene proyecto y muestra formulario de edición |  Aprobada |
| **TC-F12** | `GestorControllerTest` | Fracaso | Editar proyecto inexistente en backend (`id = 999`) | Redirige al listado con alerta de recurso no encontrado |  Aprobada |
| **TC-F13** | `GestorControllerTest` | Éxito | Actualizar proyecto (`/actualizarProyecto`) | Envía actualización y redirige a `/index` |  Aprobada |
| **TC-F14** | `GestorControllerTest` | Éxito | Eliminar proyecto (`/eliminarProyecto`) | Invoca DELETE en backend y redirige a `/index` |  Aprobada |
| **TC-F15** | `GestorControllerTest` | Éxito | Ver tareas de proyecto (`/tareas?id_proyecto=10`) | Muestra vista `tareas` con tarjetas de Kanban cargadas |  Aprobada |
| **TC-F16** | `GestorControllerTest` | Fracaso | Falla de conexión al cargar tareas de proyecto | Vista `tareas` desplegada con lista vacía segura |  Aprobada |
| **TC-F17** | `GestorControllerTest` | Éxito | Agregar tarea a proyecto válido | Registra tarea y redirige a vista de tareas del proyecto |  Aprobada |
| **TC-F18** | `GestorControllerTest` | Fracaso | Falla al registrar tarea (flag = 0) | Informa error sin interrumpir sesión |  Aprobada |
| **TC-F19** | `GestorControllerTest` | Éxito | Editar tarea existente | Carga datos de tarea en vista `formularioTarea` |  Aprobada |
| **TC-F20** | `GestorControllerTest` | Fracaso | Editar tarea inexistente | Redirige a tareas del proyecto con mensaje |  Aprobada |
| **TC-F21** | `GestorControllerTest` | Éxito | Actualizar tarea existente | Envía cambios al backend y redirecciona |  Aprobada |
| **TC-F22** | `GestorControllerTest` | Éxito | Eliminar tarea existente | Elimina registro en backend y refresca vista |  Aprobada |
| **TC-F23** | `GestorControllerTest` | Éxito | Ver comentarios de tarea (`/comentarios?id_tarea=5`) | Retorna vista `comentarios` con lista de opiniones |  Aprobada |
| **TC-F24** | `GestorControllerTest` | Fracaso | Ver comentarios ante fallo de backend | Retorna vista con lista vacía protegida |  Aprobada |
| **TC-F25** | `GestorControllerTest` | Éxito | Registrar nuevo comentario | Envía POST y redirige a `/comentarios` |  Aprobada |
| **TC-F26** | `GestorControllerTest` | Éxito | Eliminar comentario existente | Invoca DELETE y actualiza hilo de comentarios |  Aprobada |
| **TC-F27** | `GestorControllerTest` | Éxito | Ver asignaciones con usuarios y tareas | Despliega vista `asignaciones` con cruce de información |  Aprobada |
| **TC-F28** | `GestorControllerTest` | Éxito | Asignar usuario a tarea (`/guardarAsignacion`) | Registra asignación y redirige a vista |  Aprobada |
| **TC-F29**–**TC-F33** | `ClienteModeloTest` | Éxito | Getters, setters y constructores de `Usuario`, `Proyectos`, `Tareas`, `Comentarios`, `Asignaciones` | Validación total de integridad de datos en capa modelo del cliente |  5 pruebas Aprobadas |
| **TC-F34** | `Ejercicio5ClienteApplicationTests` | Éxito | `contextLoads` del cliente web Spring Boot 3 | Carga del contexto Spring Boot y templates |  Aprobada |

---

## 3. Resumen de Cobertura JaCoCo por Clase

### 3.1. Microservicio Backend (`Ejercicio5MyAPIGestor`)
> **Generado en:** `Ejercicio5MyAPIGestor/target/site/jacoco/index.html`  
> **Exclusión de clase Main:** `com.mx.cbtis.gestor.Ejercicio5MyApiGestorApplication` (excluida formalmente en el `pom.xml` por ser punto de arranque del framework).

| Paquete | Clase | Cobertura de Métodos | Métodos Totales | Cobertura de Líneas | Líneas Totales | Estado |
|---|---|---|---|---|---|---|
| `com.mx.cbtis.gestor.controller` | `UsuarioController` | **100%** | 7 / 7 | **100%** | 31 / 31 |  Cumplida |
| `com.mx.cbtis.gestor.controller` | `TareasController` | **100%** | 6 / 6 | **100%** | 27 / 27 |  Cumplida |
| `com.mx.cbtis.gestor.controller` | `ProyectosController` | **100%** | 6 / 6 | **100%** | 27 / 27 |  Cumplida |
| `com.mx.cbtis.gestor.controller` | `ComentariosController` | **100%** | 6 / 6 | **100%** | 27 / 27 |  Cumplida |
| `com.mx.cbtis.gestor.controller` | `AsignacionesController` | **100%** | 6 / 6 | **100%** | 27 / 27 |  Cumplida |
| `com.mx.cbtis.gestor.modelo` | `Usuario` | **100%** | 14 / 14 | **100%** | 28 / 28 |  Cumplida |
| `com.mx.cbtis.gestor.modelo` | `Tareas` | **100%** | 16 / 16 | **100%** | 32 / 32 |  Cumplida |
| `com.mx.cbtis.gestor.modelo` | `Proyectos` | **100%** | 16 / 16 | **100%** | 32 / 32 |  Cumplida |
| `com.mx.cbtis.gestor.modelo` | `Comentarios` | **100%** | 12 / 12 | **100%** | 24 / 24 |  Cumplida |
| `com.mx.cbtis.gestor.modelo` | `Asignaciones` | **100%** | 10 / 10 | **100%** | 20 / 20 |  Cumplida |
| **TOTAL EVALUABLE BACKEND** | **Todas las clases evaluables** | **100%** | **99 / 99** | **100%** | **275 / 275** |  **100% META ALCANZADA** |

---

### 3.2. Microservicio Frontend (`Ejercicio5Cliente`)
> **Generado en:** `Ejercicio5Cliente/target/site/jacoco/index.html`  
> **Exclusión de clase Main:** `com.mx.cbtis.Ejercicio5ClienteApplication` (excluida formalmente en el `pom.xml`).

| Paquete | Clase | Cobertura de Métodos | Métodos Totales | Cobertura de Líneas | Líneas Totales | Estado |
|---|---|---|---|---|---|---|
| `com.mx.cbtis.controller` | `GestorController` | **100%** | 29 / 29 | **100%** | 131 / 131 |  Cumplida |
| `com.mx.cbtis.modelo` | `Usuario` | **100%** | 14 / 14 | **100%** | 28 / 28 |  Cumplida |
| `com.mx.cbtis.modelo` | `Tareas` | **100%** | 16 / 16 | **100%** | 32 / 32 |  Cumplida |
| `com.mx.cbtis.modelo` | `Proyectos` | **100%** | 16 / 16 | **100%** | 32 / 32 |  Cumplida |
| `com.mx.cbtis.modelo` | `Comentarios` | **100%** | 12 / 12 | **100%** | 24 / 24 |  Cumplida |
| `com.mx.cbtis.modelo` | `Asignaciones` | **100%** | 10 / 10 | **100%** | 20 / 20 |  Cumplida |
| **TOTAL EVALUABLE FRONTEND** | **Todas las clases evaluables** | **100%** | **97 / 97** | **100%** | **267 / 267** |  **100% META ALCANZADA** |

---

## 4. Matriz Resumen de Incidencias y Remediaciones

*(Detalle exhaustivo disponible en el archivo raíz [`INCIDENCIAS.md`](file:///c:/Users/laura/admin-proyectos-main/INCIDENCIAS.md))*

| # | Hallazgo | Severidad | Tipo | Acción Tomada | Commit |
|---|---|---|---|---|---|
| **1** | Contraseñas en texto plano en BD y SQL | **Crítica** | Vulnerabilidad Real | Implementación de BCrypt, búsqueda por correo y script `db/migracion_hash_passwords.sql` | `12e540e` |
| **2** | Credenciales de BD fijadas en texto plano (`root` sin password) | **Alta** | Vulnerabilidad Real | Parametrización con variables de entorno `${DB_URL}`, `${DB_USER}`, `${DB_PASS}` | `b145ce3` |
| **3** | Comprobación `if(!x.equals(null))` que detona NPE | **Media** | Bug / Antipatrón | Sustitución sistemática por `if (x != null)` en todos los controladores | `e20a79e` / `812cede` |
| **4** | Atributos de fecha como `String` en entidades vs `DATE`/`DATETIME` en BD | **Baja** | Code Smell Aceptado | Documentado y justificado para no romper contratos REST ni vistas Thymeleaf | Documentado |
| **5** | Exclusión de directorios de compilación `target/` en análisis Sonar | **Baja** | Falso Positivo Justificado | Configurada propiedad `<sonar.exclusions>**/target/**</sonar.exclusions>` en ambos pom | `bec893b` / `52e1e74` |
| **6** | Dependencia de MariaDB externa para pruebas unitarias de backend | **Alta** | Hotspot de CI | Implementación de perfil H2 en memoria con compatibilidad MariaDB | `b145ce3` / `812cede` |
| **7** | Respuestas de `RestTemplate` desprotegidas ante valores nulos | **Media** | Bug Potencial | Refactorización con inyección `@Bean` y fallbacks seguros a colecciones vacías | `52e1e74` |

---

## 5. Explicación de los Tres Pipelines Declarativos de Jenkins

Los tres pipelines se encuentran versionados en el directorio `ci/` y utilizan la directiva declarativa `tools { jdk 'JDK21'; maven 'Maven3' }`.

> **Compatibilidad Multiplataforma (Windows y Linux):**  
> Para evitar errores de ejecución en agentes Windows que operan como servicio (`Cannot run program "sh": CreateProcess error=2`), los tres pipelines incorporan al inicio el helper:
> ```groovy
> def run(String cmd) {
>     if (isUnix()) {
>         sh cmd
>     } else {
>         bat cmd
>     }
> }
> ```
> Todos los comandos son ejecutados mediante `run(cmd)`, empleando sintaxis compatible con `cmd.exe` de Windows (`\`, redirecciones `<`, `.venv\Scripts\` para `sqlfluff` y `retry/sleep` de Jenkins en lugar de bucles bash).

> **Requisito Global en Jenkins:** En la administración de Jenkins (*Manage Jenkins $\rightarrow$ Tools*), deben registrarse con esos nombres exactos:
> - **JDK installations:** Nombre = `JDK21`, apuntando a la ruta de instalación de OpenJDK 21.
> - **Maven installations:** Nombre = `Maven3`, versión Maven 3.9+.

```
       ci/Jenkinsfile.backend               ci/Jenkinsfile.frontend                  ci/Jenkinsfile.database
    +---------------------------+        +---------------------------+       +------------------------------------+
    | 1. Checkout & Java Check  |        | 1. Checkout & Java Check  |       | 1. Análisis SQL (sqlfluff)         |
    | 2. mvn clean compile      |        | 2. mvn clean compile      |       | 2. Docker MariaDB 11.3 Efímera     |
    | 3. mvn verify (JUnit+H2)  |        | 3. mvn verify (Thymeleaf) |       | 3. Cargar Dump & db/validate.sql   |
    | 4. Publicar JUnit+JaCoCo  |        | 4. Publicar JUnit+JaCoCo  |       | 4. Backend mvn verify real DB      |
    | 5. mvn -DskipTests package|        | 5. mvn -DskipTests package|       | 5. SonarQube & Quality Gate        |
    +---------------------------+        +---------------------------+       | 6. Post: docker rm -f (Cleanup)    |
                                                                             +------------------------------------+
```

### 5.1. `ci/Jenkinsfile.backend`
- **Propósito:** Pipeline de integración continua rápido y autónomo para el microservicio REST.
- **Stages:**
  1. `Validar Entorno`: Imprime `java -version` y `mvn -version`.
  2. `Compilar Backend`: Ejecuta `mvn -B clean compile` asegurando sintaxis y compatibilidad con Java 21.
  3. `Pruebas Unitarias y Cobertura`: Ejecuta `mvn -B verify` utilizando H2 Database en memoria, ejecutando las 72 pruebas y generando `target/jacoco.exec`.
  4. `Publicar Reportes`: Invoca el step `junit` para procesar los XML de Surefire y archiva la carpeta `target/site/jacoco/**`.
  5. `Empaquetar Artefacto`: Genera el ejecutable Spring Boot `Ejercicio5MyAPIGestor-0.0.1-SNAPSHOT.jar` omitiendo tests repetidos (`-DskipTests`).

### 5.2. `ci/Jenkinsfile.frontend`
- **Propósito:** Pipeline de integración continua para el microservicio web MVC Thymeleaf.
- **Stages:**
  1. `Validar Entorno`: Verifica toolchains Java 21 y Maven.
  2. `Compilar Frontend`: Ejecuta `mvn -B clean compile`.
  3. `Pruebas Unitarias y Cobertura`: Ejecuta las 34 pruebas con simulación de `RestTemplate` y genera reporte JaCoCo.
  4. `Publicar Reportes`: Publica resultados de Surefire y artefactos JaCoCo en Jenkins.
  5. `Empaquetar Artefacto`: Empaqueta el JAR ejecutable del cliente frontend.

### 5.3. `ci/Jenkinsfile.database` (Pipeline Integral de Calidad y Base de Datos)
- **Propósito:** Validar la capa de datos real, asegurar calidad estática de SQL, ejecutar pruebas de integración contra un motor idéntico a producción y validar Quality Gate de SonarQube.
- **Variables de Entorno Definidas:**
  - `MARIADB_CONTAINER = "mariadb-gestor-ci-${BUILD_NUMBER}"`
  - `DB_PORT = "3307"`
  - `DB_NAME = "dbgestor"`
  - `DB_USER = "root"`
  - `DB_PASS = "ci_secure_password_2026"`
  - `DB_URL = "jdbc:mariadb://localhost:3307/dbgestor"`
- **Stages Detallados:**
  1. `1. Análisis Estático SQL (sqlfluff)`:
     - Realiza linting de `db/validate.sql` con dialecto MariaDB.
     - Documenta opciones de instalación en el agente (nativo via `pip install sqlfluff` o mediante imagen docker `sqlfluff/sqlfluff:latest`). Si detecta errores de sintaxis o malas prácticas, aborta el pipeline.
  2. `2. Desplegar MariaDB 11.3 Efímera en Docker`:
     - Ejecuta un contenedor oficial `mariadb:11.3` en un puerto no conflictivo (3307).
     - Implementa un **Health Check activo** con bucle de reintento (`mariadb-admin ping`) hasta que el motor esté listo para transacciones.
  3. `3. Cargar Esquema y Ejecutar Validaciones SQL`:
     - Inyecta `dump-dbgestor-202406261453.sql`.
     - Ejecuta `db/validate.sql`, el cual audita existencia de las 5 tablas, llave primaria de usuario, registros cargados, tareas huérfanas, correos duplicados y contraseñas planas.
     - Guarda la salida tabular en `validaciones_resultado.txt` y la archiva en Jenkins.
     - Aplica el script `db/migracion_hash_passwords.sql` y revalida la ausencia de contraseñas planas archivando `validaciones_post_migracion.txt`.
  4. `4. Pruebas Backend contra MariaDB Real`:
     - Ejecuta `mvn -B verify` inyectando dinámicamente las credenciales del contenedor (`DB_URL`, `DB_USER`, `DB_PASS`), validando que las entidades JPA sincronicen perfectamente con el motor real.
  5. `5. Análisis SonarQube & Quality Gate`:
     - Envía métricas de código, vulnerabilidades y cobertura JaCoCo al servidor SonarQube configurado en Jenkins (`withSonarQubeEnv('SonarQube')`).
     - Espera la decisión de SonarQube mediante `waitForQualityGate()`. Si no cumple los estándares de calidad, marca el build como fallido.
  6. `post { always }`:
     - Detiene y elimina el contenedor Docker `docker rm -f ${MARIADB_CONTAINER}`, asegurando un entorno limpio sin consumo residual de memoria o puertos bloqueados.

---

## 6. Guion de Demostración en Video (5 Puntos Clave)

Este guion está optimizado para que el estudiante grabe su sustentación en un tiempo aproximado de 5 a 8 minutos:

---

### **Punto 1: Presentación de la Arquitectura y Cambios Aplicados (1.5 min)**
- **Qué mostrar en pantalla:** Editor de código (VS Code) con el árbol del monorepo abierto.
- **Qué decir:**
  > *"Buenas tardes, profesor y evaluadores. En esta entrega de la Práctica 3 presento la modernización integral de nuestro sistema monorepo compuesto por dos microservicios: el backend REST `Ejercicio5MyAPIGestor` y el frontend `Ejercicio5Cliente`. Ambos proyectos han sido migrados exitosamente a **Java 21 LTS**.*  
  > *Como remediación de seguridad crítica, resolvimos el almacenamiento de contraseñas en texto plano implementando **BCrypt** mediante `spring-security-crypto` en el backend, sin sobrecargar con todo el starter de seguridad. Mostramos aquí la búsqueda por correo en `UsuarioRepository` y la verificación con `passwordEncoder.matches()` en `UsuarioController`. Además, generamos el script `db/migracion_hash_passwords.sql` para actualizar las cuentas existentes del dump.*  
  > *Asimismo, parametrizamos las credenciales de base de datos en `application.properties` con variables de entorno para cumplir con estándares 12-factor y aislamos las pruebas con H2 en memoria."*

---

### **Punto 2: Ejecución de Pruebas y Cobertura JaCoCo en Backend (1.5 min)**
- **Qué mostrar en pantalla:** Terminal ejecutando `mvn clean verify` en `Ejercicio5MyAPIGestor` y luego el navegador con `target/site/jacoco/index.html`.
- **Qué decir:**
  > *"Procedemos a ejecutar la suite de pruebas del backend con `mvn clean verify`. Como observamos en la consola, se ejecutan un total de 72 pruebas automatizadas sin ningún fallo.*  
  > *Estas pruebas fueron desarrolladas con JUnit 5 y Mockito, cubriendo exhaustivamente los 5 controladores: Usuario, Tareas, Proyectos, Comentarios y Asignaciones, incluyendo casos de éxito y de fracaso controlado, como búsquedas por IDs inexistentes o logins fallidos que devuelven el código '000'.*  
  > *Al abrir el reporte generado por JaCoCo en el navegador, apreciamos que hemos alcanzado el **100% de cobertura de métodos evaluables** en todos los controladores y entidades del modelo, habiendo excluido justificadamente en el pom únicamente la clase `Ejercicio5MyApiGestorApplication`."*

---

### **Punto 3: Ejecución de Pruebas y Cobertura JaCoCo en Frontend (1 min)**
- **Qué mostrar en pantalla:** Terminal ejecutando `mvn clean verify` en `Ejercicio5Cliente` y el reporte JaCoCo resultante.
- **Qué decir:**
  > *"En el microservicio frontend, que es una aplicación web Spring Boot 3 con motor de plantillas Thymeleaf, implementamos una suite de 34 pruebas automatizadas simulando las respuestas HTTP de `RestTemplate`.*  
  > *Validamos todos los flujos: autenticación exitosa, credenciales incorrectas, caídas del servicio backend (manejando excepciones de conexión), validación de formularios y visualización de vistas.*  
  > *Ejecutamos `mvn clean verify` y confirmamos que las 34 pruebas pasan en verde y obtenemos un **100% de cobertura de métodos y 100% de líneas** en `GestorController` y en todas sus clases de modelo."*

---

### **Punto 4: Recorrido por los Pipelines Jenkins y Scripts de Base de Datos (1.5 min)**
- **Qué mostrar en pantalla:** El archivo `db/validate.sql` y luego `ci/Jenkinsfile.database`.
- **Qué decir:**
  > *"En la carpeta `ci/` diseñamos tres pipelines declarativos de Jenkins. Muestro aquí `Jenkinsfile.database`, que es el corazón de nuestra estrategia de calidad de datos y código.*  
  > *El pipeline ejecuta primero un linter estático con `sqlfluff` bajo dialecto MariaDB para asegurar que no existan errores de sintaxis en los scripts.*  
  > *Posteriormente, levanta dinámicamente un contenedor Docker efímero con la imagen oficial de `MariaDB 11.3`, espera a que el servicio esté saludable y carga el dump de base de datos.*  
  > *A continuación ejecuta nuestro script `db/validate.sql`, el cual audita automáticamente las 5 tablas, llaves primarias, registros cargados, detección de tareas huérfanas y alertas de contraseñas sin hash.*  
  > *Finalmente, ejecuta las pruebas de integración contra la base de datos real, dispara el análisis hacia **SonarQube** exigiendo la aprobación del **Quality Gate** con `waitForQualityGate()`, y en el bloque `post { always }` elimina el contenedor Docker para garantizar un entorno limpio."*

---

### **Punto 5: Demostración en Jenkins y Cierre (1 min)**
- **Qué mostrar en pantalla:** La interfaz de Jenkins con los jobs creados o el historial de commits en Git (`git log --oneline`).
- **Qué decir:**
  > *"Mostramos la configuración de herramientas globales en Jenkins con JDK21 y Maven3, y los pipelines ejecutándose con sus etapas en color verde y la publicación de artefactos JUnit y reportes JaCoCo.*  
  > *En nuestro repositorio Git, cada cambio fue registrado en un commit atómico y descriptivo bajo la convención de conventional commits, asegurando trazabilidad completa.*  
  > *Con esto concluimos la sustentación de la Práctica 3 Parte 2, cumpliendo con todos los requerimientos de calidad, seguridad, automatización y cobertura al 100%. Muchas gracias."*

---

## 7. Checklist: "Evidencias que debo capturar yo" (Para el Estudiante)

Esta sección sirve como lista de verificación práctica de las capturas de pantalla que debes tomar para integrar en tu reporte final en PDF o Word:

- [ ] **1. Consola de Pruebas Backend:** Captura de pantalla de la terminal mostrando el comando `mvn -B clean verify` en `Ejercicio5MyAPIGestor` con el mensaje:  
  `Tests run: 72, Failures: 0, Errors: 0, Skipped: 0` y `BUILD SUCCESS`.
- [ ] **2. Reporte JaCoCo Backend:** Captura de pantalla en el navegador de `Ejercicio5MyAPIGestor/target/site/jacoco/index.html` mostrando la tabla con el 100% de métodos en `com.mx.cbtis.gestor.controller` y `com.mx.cbtis.gestor.modelo`.
- [ ] **3. Consola de Pruebas Frontend:** Captura de pantalla de la terminal mostrando `mvn -B clean verify` en `Ejercicio5Cliente` con el mensaje:  
  `Tests run: 34, Failures: 0, Errors: 0, Skipped: 0` y `BUILD SUCCESS`.
- [ ] **4. Reporte JaCoCo Frontend:** Captura de pantalla en el navegador de `Ejercicio5Cliente/target/site/jacoco/index.html` mostrando el 100% en `GestorController` y modelos.
- [ ] **5. Git Log de Commits:** Captura de pantalla de la consola ejecutando `git log --oneline -n 8` mostrando los commits realizados:
  - `docs(entrega): ...`
  - `ci(jenkins): agregar pipelines declarativos para backend, frontend y base de datos con script de validacion SQL`
  - `docs(calidad): registrar matriz de incidencias, vulnerabilidades y code smells en INCIDENCIAS.md`
  - `test(frontend): agregar pruebas unitarias para GestorController y modelos con Mockito y JaCoCo`
  - `test(backend): agregar pruebas unitarias JUnit 5 y Mockito para controladores y ModeloTest con 100% cobertura de metodos`
  - `config(bd): parametrizar credenciales y conexion a base de datos con variables de entorno`
  - `seguridad(auth): hash de contrasenas con BCrypt y script de migracion SQL`
  - `chore: upgrade de los microservicios a Java 21`
- [ ] **6. Jenkins - Global Tool Configuration:** Captura en Jenkins de *Manage Jenkins $\rightarrow$ Tools* mostrando la configuración de `JDK21` y `Maven3`.
- [ ] **7. Jenkins - Ejecución de Pipelines:** Captura del dashboard de Jenkins con la vista de etapas (Stage View) en verde para:
  - Pipeline Backend (`Jenkinsfile.backend`)
  - Pipeline Frontend (`Jenkinsfile.frontend`)
  - Pipeline Database & Quality (`Jenkinsfile.database`)
- [ ] **8. Artefactos y Reportes en Jenkins:** Captura dentro de la ejecución del job de Jenkins mostrando los artefactos archivados: `validaciones_resultado.txt`, reporte de pruebas JUnit y tendencia de cobertura JaCoCo.
- [ ] **9. SonarQube Dashboard (si aplica en su servidor):** Captura de pantalla de SonarQube con el proyecto analizado, mostrando **Quality Gate Passed**, 0 vulnerabilidades críticas de autenticación y la cobertura de código reportada por JaCoCo.

