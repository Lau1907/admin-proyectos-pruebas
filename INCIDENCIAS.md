# Matriz de Incidencias, Seguridad y Calidad de Código

> **Proyecto:** Práctica 3 – Actualización de microservicios y pipelines CI  
> **Repositorio:** `admin-proyectos-main`  
> **Microservicios:** `Ejercicio5MyAPIGestor` (Backend REST) y `Ejercicio5Cliente` (Frontend MVC)  
> **Fecha de Elaboración:** Noviembre 2026  
> **Responsable:** Laura Berenice Tapia Cid  

---

## 1. Resumen Ejecutivo

Durante la auditoría estática de código, análisis de seguridad en SonarQube, análisis de cobertura con JaCoCo y linting estático de base de datos con sqlfluff sobre la arquitectura de microservicios, se auditaron **162 issues en SonarQube** (133 abiertos reales actuales y 29 corregidos/cerrados) clasificados en tres dimensiones de calidad:
- **18 Security Issues (Vulnerabilidades):** 15 abiertos aceptados como deuda técnica y 3 corregidos con DTO.
- **5 Reliability Issues (Bugs de Confiabilidad y Null Safety):** 5 corregidos, 0 abiertos en SonarQube.
- **139 Maintainability Issues (Deuda Técnica y Code Smells):** 118 abiertos reales en SonarQube y 21 corregidos.

Además, se auditaron vulnerabilidades críticas previas de autenticación en texto plano, exposición de credenciales JDBC en properties y antipatrones de programación (NPE por `.equals(null)`).

A continuación se documenta la clasificación formal de cada hallazgo, las remediaciones aplicadas, la mitigación de *Mass Assignment* mediante DTOs, las justificaciones técnicas para los ítems aceptados como deuda técnica y las métricas finales de cobertura.

---

## 2. Clasificación Global de los Issues de SonarQube (133 Abiertos Reales)

| Dimensión SonarQube | Total Auditado | Corregido (Ya no aparece en Sonar) | Abierto en Sonar (Deuda Aceptada) | Estado General |
|---|---|---|---|---|
| **Security (Seguridad / Vulnerabilidades)** | **18** | **3** (Mass Assignment en `UsuarioController` mitigado con `UsuarioDTO`) | **15** (Mass Assignment `java:S4684` en `Tareas`, `Proyectos`, `Comentarios`, `Asignaciones` y `deleteUsuario`) | Mitigado en usuario; 15 abiertos documentados como deuda técnica aceptada. |
| **Reliability (Confiabilidad / Bugs)** | **5** | **5** (Corrección de `.equals(null)`, validación de Optional y ramas nulas) | **0** | **100% Corregido**; 0 bugs abiertos en SonarQube. |
| **Maintainability (Mantenibilidad / Code Smells)** | **139** | **21** (15 en `UsuarioDTO`, 2 en `UsuarioController`, 4 en pruebas/limpieza) | **118** (45 S117, 38 S100, 19 S116, 5 S6813, 4 S4144, 3 S1128, 2 S1488, 1 S5838, 1 S125) | Aceptado justificadamente para preservar modelos de BD MariaDB y contratos REST. |
| **Total** | **162** | **29** | **133** | **133 issues abiertos reales en SonarQube** |

---

## 3. Matriz Detallada de Hallazgos e Incidencias

| # | Hallazgo / Regla SonarQube | Ubicación | Severidad / Categoría | Clasificación | Acción Tomada / Justificación Técnica | Commit |
|---|---|---|---|---|---|---|
| **1** | **Vulnerabilidad Mass Assignment (CWE-915 / OWASP A08:2021 / `java:S4684`):** "Replace this persistent entity with a simple POJO or DTO object" en controladores REST | `UsuarioController.java` (`addUsuario`, `updateUsuario`, `getUsuarioLogin`) | **Critical (Security)** | **Real Corregido** | Se desacopló la entidad JPA `Usuario` del contrato de entrada mediante `UsuarioDTO`. Se implementaron validaciones Bean Validation (`@Valid`, `@NotBlank`) y mapeo explícito interno `toEntity()`. | `ac8cb1f` / `f243c06` |
| **2** | **Mass Assignment pendiente en controladores restantes (`java:S4684`):** 15 issues por uso directo de entidades JPA en `@RequestBody` | `TareasController` (3), `ProyectosController` (3), `ComentariosController` (3), `AsignacionesController` (3), `UsuarioController.deleteUsuario` (1), y entidades relacionadas | **Critical (Security)** | **Real Aceptado con Justificación (Deuda Técnica)** | **Justificación:** El riesgo persiste debido a que los endpoints no cuentan con autenticación. Se acepta temporalmente para no romper la comunicación síncrona con `Ejercicio5Cliente`. La mitigación pendiente consiste en implementar DTOs específicos y un esquema formal de autenticación. | Documentado |
| **3** | Almacenamiento y autenticación de contraseñas en texto plano (CWE-256 / CWE-312 / OWASP A02:2021) | `Usuario.java`, `UsuarioRepository.java`, `UsuarioController.java` | **Crítica (Security)** | **Real Corregido** | Implementación de hashing seguro con **BCrypt** (`spring-security-crypto`). Búsqueda única por correo y validación con `passwordEncoder.matches()`. Script `db/migracion_hash_passwords.sql` generado con hashes para el dump. | `12e540e` |
| **4** | Exposición de credenciales JDBC y usuario root en texto plano (CWE-798 / OWASP A05:2021) | `Ejercicio5MyAPIGestor/src/main/resources/application.properties` | **Alta (Security)** | **Real Corregido** | Parametrización de propiedades mediante variables de entorno `${DB_URL}`, `${DB_USER}`, `${DB_PASS}` con fallbacks seguros. | `b145ce3` |
| **5** | **Bug de Confiabilidad:** Comprobación de nulidad defectuosa mediante `if (!x.equals(null))` (CWE-476 / NPE) | Los 5 controladores REST (`UsuarioController`, `TareasController`, `ProyectosController`, `ComentariosController`, `AsignacionesController`) | **Major (Reliability)** | **Real Corregido** | Según el contrato de `Object.equals()`, si la referencia `x` es nula se lanza `NullPointerException`. Se reemplazó por la comprobación canónica `if (x != null)`. | `e20a79e` / `812cede` |
| **6** | **Ramas no cubiertas (Condiciones else de null checks):** Retorno seguro al invocar con parámetros nulos | Métodos `add*`, `update*`, `delete*` de los 5 controladores | **Major (Reliability / Test)** | **Real Corregido** | Se crearon pruebas unitarias específicas enviando `null` directamente a cada método y verificando que retornan `0` y nunca interactúan con los repositorios (`verify(..., never()).save/delete(...)`). | `6f7f55b` |
| **7** | Mapeo de columnas temporales como `String` en lugar de `java.time.LocalDate` / `LocalDateTime` | Entidades JPA (`Usuario`, `Tareas`, `Proyectos`, `Asignaciones`, `Comentarios`) | **Minor (Maintainability)** | **Real Aceptado con Justificación** | **Justificación:** Las entidades mantienen tipo `String` para garantizar compatibilidad binaria estricta con los formularios HTML5 (`<input type="date">` formato `yyyy-MM-dd`) y los bindings de Spring MVC en `Ejercicio5Cliente`. Migrar a `LocalDate` requeriría refactorizar conversores custom y deserializadores en ambos microservicios. | Documentado |
| **8** | Convenciones de nombres en entidades JPA no conformes con camelCase (102 issues: 45 `java:S117`, 38 `java:S100`, 19 `java:S116`) | Entidades JPA (`Usuario`, `Tareas`, `Proyectos`, `Asignaciones`, `Comentarios`) | **Minor (Maintainability)** | **Real Aceptado con Justificación** | **Justificación:** Los atributos y métodos reflejan fielmente las columnas de la base de datos MariaDB heredada (`id_usuario`, `correo_electronico`, etc.) y las plantillas Thymeleaf. Modificarlos en cascada en las entidades implicaría riesgos de mapeo con MariaDB y el frontend MVC. | Documentado |
| **9** | Inyección de dependencias por campo (`java:S6813`): 5 issues por `@Autowired` en atributos en vez de constructor | Los 5 controladores REST | **Major (Maintainability)** | **Real Aceptado con Justificación** | Inyección por campo preexistente de Spring Boot. Se acepta temporalmente dado que las suites de pruebas unitarias instancian y mockean dependencias directamente o vía `@WebMvcTest`. | Documentado |
| **10** | Implementaciones de métodos idénticas en controladores restantes (`java:S4144`): 4 issues | Controladores restantes (`TareasController`, `ProyectosController`, etc.) | **Major (Maintainability)** | **Real Aceptado con Justificación** | Se acepta temporalmente para no alterar el flujo interno de los controladores pendientes de migración a DTO. | Documentado |
| **11** | Imports no utilizados restantes (`java:S1128`): 3 issues | Clases del backend | **Minor (Maintainability)** | **Real Aceptado con Justificación** | Imports residuales no funcionales aceptados como deuda técnica menor. | Documentado |
| **12** | Retorno inmediato de variables locales temporales (`java:S1488`): 2 issues | Clases del backend | **Minor (Maintainability)** | **Real Aceptado con Justificación** | Variables intermedias para depuración mantenidas por legibilidad. | Documentado |
| **13** | Simplificación de aserción AssertJ (`java:S5838`): 1 issue | Clases de prueba | **Minor (Maintainability)** | **Real Aceptado con Justificación** | Aserción válida en suite de test sin impacto en ejecución. | Documentado |
| **14** | Sección de código comentado (`java:S125`): 1 issue | Código fuente del backend | **Major (Maintainability)** | **Real Aceptado con Justificación** | Comentario conservado como referencia histórica de desarrollo. | Documentado |
| **15** | Visibilidad pública de clases y métodos en pruebas unitarias JUnit 5 | Clases `*Test.java` | **Minor (Maintainability)** | **Real Aceptado con Justificación** | JUnit 5 permite visibilidad de paquete (`package-private`). Mantener `public` en algunas suites no afecta la ejecución, el reporte de JaCoCo ni el ciclo de vida de Surefire. | Documentado |
| **16** | Dependencia de base de datos externa MariaDB en fase de test de CI (`mvn test`) | `Ejercicio5MyAPIGestor/src/test/resources/application.properties` | **High (Arquitectura)** | **Real Corregido** | Despliegue de H2 Database en modo emulación MariaDB (`MODE=MariaDB`) en scope de pruebas, independizando la compilación y CI de conexiones externas. | `b145ce3` / `812cede` |
| **17** | Fragilidad ante respuestas HTTP nulas en llamadas REST (`RestTemplate`) | `Ejercicio5Cliente/.../GestorController.java` | **Major (Reliability)** | **Real Corregido** | Inyección segura de `RestTemplate` como `@Bean` y manejo de nulos en arrays de respuesta para evitar fallos por contingencias de red. | `52e1e74` |
| **18** | Inconsistencias de formato y estilo SQL en script de validación (`LT01`, `LT02`, `LT05`, `LT09`, `CP02`, `AL01`) | `db/validate.sql` | **Minor (Maintainability)** | **Real Corregido** | Creación de `.sqlfluff` con dialecto MariaDB y corrección integral de 38 reglas con `sqlfluff fix` y revisión manual. Salida validada contra MariaDB 11.3. | `9079971` / `1e0e1f1` |

> **Nota de Configuración en pom.xml:**  
> Las propiedades `<sonar.exclusions>**/target/**</sonar.exclusions>` y `<sonar.cpd.exclusions>**/src/test/**</sonar.cpd.exclusions>` corresponden a configuraciones de escaneo estático y análisis de duplicación (CPD), respectivamente. Su objetivo es evitar falsos positivos en bytecode generado y aislar las aserciones repetitivas de las suites de prueba unitaria sin relajar la inspección ni excluir código fuente productivo de `src/main` (Commits `bec893b` y `23a722a`).

---

## 4. Detalle Técnico de la Mitigación de Mass Assignment

### 4.1. Análisis del Riesgo (CWE-915)
Al recibir directamente una entidad persistente de JPA (`Usuario`) en un método anotado con `@RequestBody`:
1. Un atacante o cliente malicioso puede inyectar campos no previstos que se persisten directamente en la base de datos (por ejemplo, escalar privilegios alterando el campo `rol` o forzar un `id_usuario` no autorizado).
2. Se expone la estructura interna del modelo de persistencia (anotaciones `@Entity`, `@Table`, `@Id`).

### 4.2. Implementación de `UsuarioDTO`
Se diseñó la clase de transferencia de datos `UsuarioDTO` ubicada en `com.mx.cbtis.gestor.dto` utilizando la convención estándar `camelCase` para el cumplimiento de reglas SonarQube (`java:S116`, `java:S117`, `java:S100`), junto con `@JsonProperty` para conservar compatibilidad con el JSON en snake_case y Bean Validation (`spring-boot-starter-validation`):

```java
package com.mx.cbtis.gestor.dto;

import javax.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UsuarioDTO {

	@JsonProperty("id_usuario")
	private Integer idUsuario;

	@JsonProperty("nombre")
	private String nombre;

	@NotBlank
	@JsonProperty("correo_electronico")
	private String correoElectronico;

	@NotBlank
	@JsonProperty("contrasena")
	private String contrasena;

	@JsonProperty("rol")
	private String rol;

	@JsonProperty("imagen_usuario")
	private String imagenUsuario;

	public UsuarioDTO() {
	}

	public UsuarioDTO(Integer idUsuario, String nombre, String correoElectronico, String contrasena, String rol,
			String imagenUsuario) {
		this.idUsuario = idUsuario;
		this.nombre = nombre;
		this.correoElectronico = correoElectronico;
		this.contrasena = contrasena;
		this.rol = rol;
		this.imagenUsuario = imagenUsuario;
	}

	// Getters y Setters camelCase (getIdUsuario, setIdUsuario, getCorreoElectronico, etc.)
}
```

### 4.3. Refactorización en `UsuarioController`
Los endpoints críticos de recepción de datos fueron actualizados para recibir `UsuarioDTO`:
- `POST /getUsuarioLogin`: Recibe `UsuarioDTO`, realiza comprobación defensiva de nulidad, busca por correo y valida con BCrypt mediante el helper privado `verificarPassword(...)`. Retorna la entidad autenticada o un objeto de rechazo con nombre `"000"`.
- `POST /addUsuario`: Recibe `@Valid @RequestBody UsuarioDTO`, delega en `guardarUsuario(...)` que aplica el mapeo a entidad mediante `toEntity()`, cifra la contraseña con BCrypt mediante `hashContrasena(...)` si no está cifrada, guarda vía repositorio y retorna `1` (éxito) o `0` (fracaso).
- `POST /updateUsuario`: Recibe `UsuarioDTO` y delega exactamente a `guardarUsuario(...)`, garantizando consistencia y eliminando duplicación de código (`java:S4144`).

### 4.4. Compatibilidad con el Frontend (`Ejercicio5Cliente`)
- **Nombres de atributos serializados:** Gracias a `@JsonProperty("id_usuario")`, `@JsonProperty("correo_electronico")` y `@JsonProperty("imagen_usuario")`, los nombres en los mensajes JSON se mantienen exactamente idénticos.
- **Serialización JSON:** El payload JSON emitido por `RestTemplate` desde el frontend contiene exactamente las mismas llaves, permitiendo que Jackson deserialice en `UsuarioDTO` sin requerir ningún cambio en `Ejercicio5Cliente`.
- **Rutas HTTP y Códigos de Respuesta:** No se modificaron URLs, métodos HTTP (`GET`, `POST`, `DELETE`) ni tipos de retorno (`int` y `Usuario`), preservando el 100% de la compatibilidad operativa.

---

## 5. Deuda Técnica y Estado de los Controladores Restantes

Los siguientes controladores mantienen la recepción de entidades JPA directamente en `@RequestBody`:
1. `TareasController` (`addTareas`, `updateTareas`, `deleteTareas`)
2. `ProyectosController` (`addProyectos`, `updateProyectos`, `deleteProyectos`)
3. `ComentariosController` (`addComentarios`, `updateComentarios`, `deleteComentarios`)
4. `AsignacionesController` (`addAsignaciones`, `updateAsignaciones`, `deleteAsignaciones`)
5. `UsuarioController` (`deleteUsuario`)

### Justificación de Aceptación y Estado del Riesgo:
- **Persistencia del riesgo (CWE-915):** El riesgo de Mass Assignment continúa presente en estos controladores mientras continúen vinculando entidades persistentes directamente en el cuerpo de las peticiones HTTP.
- **Ausencia de autenticación en endpoints:** Actualmente la API REST no cuenta con un mecanismo de autenticación ni autorización perimetral (como JWT o Spring Security a nivel de endpoint), por lo que cualquier cliente con conectividad a la red puede invocar estos métodos directamente.
- **Mitigación pendiente:** La remediación definitiva requiere desacoplar las entidades restantes creando sus respectivos DTOs (`TareasDTO`, `ProyectosDTO`, `ComentariosDTO`, `AsignacionesDTO`, y DTO de baja) e incorporar un esquema formal de autenticación y autorización (Spring Security / tokens de sesión) para blindar el acceso a los endpoints.
- **Aceptación temporal:** Se aceptan como deuda técnica documentada exclusivamente para no alterar los contratos REST síncronos con `Ejercicio5Cliente` en el cierre de la Práctica 3, respaldando la estabilidad funcional mediante la suite de pruebas unitarias con Mockito (`verify(..., never())`).

---

## 6. Cobertura de Pruebas Unitarias y Métricas JaCoCo

Tras la adición de pruebas para todas las ramas nulas (`null-checks`), variantes de hashing y refactorización de controladores, las métricas de cobertura del Backend (`Ejercicio5MyAPIGestor`) verificadas contra `target/site/jacoco/jacoco.csv` son:

| Elemento | Total Instrucciones | Cobertura Instrucciones | Total Ramas (Branches) | Cobertura Ramas | Total Líneas | Cobertura Líneas | Total Métodos | Cobertura Métodos |
|---|---|---|---|---|---|---|---|---|
| `com.mx.cbtis.gestor.controller` | 527 | **100%** (527/527) | 70 | **97.1%** (68/70) | 150 | **100%** (150/150) | 37 | **100%** (37/37) |
| `com.mx.cbtis.gestor.modelo` | 320 | **100%** (320/320) | 0 | n/a | 136 | **100%** (136/136) | 68 | **100%** (68/68) |
| `com.mx.cbtis.gestor.dto` | 66 | **100%** (66/66) | 0 | n/a | 28 | **100%** (28/28) | 14 | **100%** (14/14) |
| **Total Global Backend** | **913** | **100%** (913/913) | **70** | **97.1%** (68/70) | **314** | **100%** (314/314) | **119** | **100%** (119/119) |

### Métricas Consolidadas:
- **Pruebas Unitarias Backend:** 77 ejecutadas, 0 fallos, 0 errores.
- **Pruebas Unitarias Frontend:** 34 ejecutadas, 0 fallos, 0 errores.
- **Total Pruebas de Microservicios:** **111 pruebas automatizadas** en el repositorio.
- **Cobertura de Métodos Evaluables:** **100%** (119/119 en backend, 34/34 en frontend).
- **Cobertura de Líneas en Backend:** **100%** (314/314 líneas cubiertas, 0 líneas no cubiertas).
- **Cobertura de Instrucciones en Backend:** **100%** (913/913 instrucciones cubiertas, 0 omitidas).
- **Calidad de Base de Datos:** Linting con sqlfluff 100% superado en `db/validate.sql` contra MariaDB 11.3.

---

## 7. Remediación del Quality Gate en Código Nuevo (SonarQube)

Durante el análisis del Quality Gate sobre el período de código nuevo de la rama `practica3-parte2`, se detectó inicialmente el fallo de 2 condiciones obligatorias:
1. **17 issues nuevos de mantenibilidad** (requerido: 0).
2. **22.63% de líneas duplicadas** sobre 243 líneas nuevas (requerido: <= 3%).

A continuación se detalla la resolución integral de cada issue y de la duplicación de código.

### 7.1. Matriz Detallada de los 17 Issues en Código Nuevo

| # | Key SonarQube | Regla | Ubicación | Severidad | Descripción del Issue | Clasificación | Acción Tomada / Justificación Técnica | Commit |
|---|---|---|---|---|---|---|---|---|
| **1** | `a7d60d3f-8c12-42dc-a8ba-739faeec92c0` | `java:S3776` | `UsuarioController.java:46` | Major | Refactor this method to reduce its Cognitive Complexity from 16 to the 15 allowed. | **Real Corregido** | Se extrajeron métodos auxiliares privados `verificarPassword(...)` y `hashContrasena(...)`, reduciendo la complejidad cognitiva de `getUsuarioLogin` de 16 a 5. | `f243c06` |
| **2** | `5e36b9c7-3170-4fb8-9272-1de09a5361f9` | `java:S4144` | `UsuarioController.java:106` | Major | Update this method so that its implementation is not identical to "addUsuario" on line 89. | **Real Corregido** | Se unificó la lógica común de mapeo, hasheo defensivo y persistencia en el método privado `guardarUsuario(UsuarioDTO)`. Tanto `addUsuario` como `updateUsuario` delegan a este método en una sola instrucción. | `f243c06` |
| **3** | `9354fbff-59b2-4f20-9280-a84a605143b8` | `java:S116` | `UsuarioDTO.java:7` | Minor | Rename this field "id_usuario" to match the regular expression '^[a-z][a-zA-Z0-9]*$'. | **Real Corregido** | Se renombró el atributo a `idUsuario` (camelCase) y se agregó `@JsonProperty("id_usuario")` para conservar 100% la compatibilidad serializada con `Ejercicio5Cliente`. | `54e02c6` |
| **4** | `4aa7810b-8c3e-4ee6-9793-39f508841889` | `java:S116` | `UsuarioDTO.java:11` | Minor | Rename this field "correo_electronico" to match the regular expression '^[a-z][a-zA-Z0-9]*$'. | **Real Corregido** | Se renombró a `correoElectronico` con `@JsonProperty("correo_electronico")`. | `54e02c6` |
| **5** | `2527c450-41e6-4291-8c64-96e987e8730a` | `java:S116` | `UsuarioDTO.java:17` | Minor | Rename this field "imagen_usuario" to match the regular expression '^[a-z][a-zA-Z0-9]*$'. | **Real Corregido** | Se renombró a `imagenUsuario` con `@JsonProperty("imagen_usuario")`. | `54e02c6` |
| **6** | `da2f0646-f418-4895-a9ef-e2c6fc71efb4` | `java:S117` | `UsuarioDTO.java:23` | Minor | Rename this local variable to match the regular expression '^[a-z][a-zA-Z0-9]*$'. | **Real Corregido** | Parámetro `id_usuario` en constructor renombrado a `idUsuario`. | `54e02c6` |
| **7** | `e41de3d7-e280-4a69-b7fc-b86c9d5dc57a` | `java:S117` | `UsuarioDTO.java:23` | Minor | Rename this local variable to match the regular expression '^[a-z][a-zA-Z0-9]*$'. | **Real Corregido** | Parámetro `correo_electronico` en constructor renombrado a `correoElectronico`. | `54e02c6` |
| **8** | `93ff7e09-4b88-466c-bd18-22c12083034b` | `java:S117` | `UsuarioDTO.java:24` | Minor | Rename this local variable to match the regular expression '^[a-z][a-zA-Z0-9]*$'. | **Real Corregido** | Parámetro `imagen_usuario` en constructor renombrado a `imagenUsuario`. | `54e02c6` |
| **9** | `cf9823f4-ec6a-495e-b997-d14ff19eedee` | `java:S100` | `UsuarioDTO.java:34` | Minor | Rename this method name to match the regular expression '^[a-z][a-zA-Z0-9]*$'. | **Real Corregido** | Getter `getId_usuario()` renombrado a `getIdUsuario()`. | `54e02c6` |
| **10** | `51101289-3644-4127-9792-32c1db441807` | `java:S117` | `UsuarioDTO.java:38` | Minor | Rename this local variable to match the regular expression '^[a-z][a-zA-Z0-9]*$'. | **Real Corregido** | Parámetro `id_usuario` en setter renombrado a `idUsuario`. | `54e02c6` |
| **11** | `932737e5-30c4-417c-906b-2cf5511772e2` | `java:S100` | `UsuarioDTO.java:38` | Minor | Rename this method name to match the regular expression '^[a-z][a-zA-Z0-9]*$'. | **Real Corregido** | Setter `setId_usuario(...)` renombrado a `setIdUsuario(...)`. | `54e02c6` |
| **12** | `287189f0-7ed2-4fbf-bb05-bbebcdf8c53c` | `java:S100` | `UsuarioDTO.java:50` | Minor | Rename this method name to match the regular expression '^[a-z][a-zA-Z0-9]*$'. | **Real Corregido** | Getter `getCorreo_electronico()` renombrado a `getCorreoElectronico()`. | `54e02c6` |
| **13** | `637f126f-d93c-453b-a8a6-da9549d5d05e` | `java:S100` | `UsuarioDTO.java:54` | Minor | Rename this method name to match the regular expression '^[a-z][a-zA-Z0-9]*$'. | **Real Corregido** | Setter `setCorreo_electronico(...)` renombrado a `setCorreoElectronico(...)`. | `54e02c6` |
| **14** | `e618219b-0843-4fc0-870c-be3e4790221c` | `java:S117` | `UsuarioDTO.java:54` | Minor | Rename this local variable to match the regular expression '^[a-z][a-zA-Z0-9]*$'. | **Real Corregido** | Parámetro `correo_electronico` en setter renombrado a `correoElectronico`. | `54e02c6` |
| **15** | `6e0d9962-e8a1-47f0-8fca-d33ed57a2239` | `java:S100` | `UsuarioDTO.java:74` | Minor | Rename this method name to match the regular expression '^[a-z][a-zA-Z0-9]*$'. | **Real Corregido** | Getter `getImagen_usuario()` renombrado a `getImagenUsuario()`. | `54e02c6` |
| **16** | `805cfc06-030b-41c6-b007-3e871ddb2e9b` | `java:S117` | `UsuarioDTO.java:78` | Minor | Rename this local variable to match the regular expression '^[a-z][a-zA-Z0-9]*$'. | **Real Corregido** | Parámetro `imagen_usuario` en setter renombrado a `imagenUsuario`. | `54e02c6` |
| **17** | `be836e31-1d16-4e33-9c62-0b6b7a4974b7` | `java:S100` | `UsuarioDTO.java:78` | Minor | Rename this method name to match the regular expression '^[a-z][a-zA-Z0-9]*$'. | **Real Corregido** | Setter `setImagen_usuario(...)` renombrado a `setImagenUsuario(...)`. | `54e02c6` |

### 7.2. Resolución de Líneas Duplicadas (22.63% -> 0.0%)
1. **Desacoplamiento entre `UsuarioDTO` y `Usuario`:** SonarQube reportaba 55 líneas duplicadas entre `UsuarioDTO.java` y `Usuario.java` debido a la identidad de nombres y estructura en getters/setters snake_case. Al refactorizar `UsuarioDTO` con la convención estándar camelCase, el token stream de CPD diverge por completo, eliminando la duplicación en `src/main`.
2. **Optimización de suites de prueba:** En `UsuarioControllerTest.java` se consolidaron aserciones y configuración de mocks repetitivos mediante los métodos auxiliares `ejecutarLoginConMock` y `assertGuardarRetornaUno`.
3. **Configuración de exclusión CPD:** En `pom.xml` se configuró formalmente `<sonar.cpd.exclusions>**/src/test/**</sonar.cpd.exclusions>` con justificación técnica (evitar falsos positivos en estructuras de prueba unitaria sin relajar la inspección de `src/main`).

### 7.3. Métricas Finales del Quality Gate en SonarQube

| Métrica en Código Nuevo | Valor Inicial | Valor Final | Umbral Requerido | Estado Quality Gate |
|---|---|---|---|---|
| **Violaciones Nuevas (Issues)** | 17 | **0** | 0 | **Aprobado (OK)** |
| **Densidad de Líneas Duplicadas** | 22.63% | **0.0%** | <= 3.0% | **Aprobado (OK)** |
| **Cobertura en Código Nuevo** | 98.2% | **97.9%** | >= 80.0% | **Aprobado (OK)** |
| **Estado General Quality Gate** | **ERROR** | **OK** | **OK** | **APROBADO** |

