# Matriz de Incidencias, Seguridad y Calidad de Código

> **Proyecto:** Práctica 3 – Actualización de microservicios y pipelines CI  
> **Repositorio:** `admin-proyectos-main`  
> **Microservicios:** `Ejercicio5MyAPIGestor` (Backend REST) y `Ejercicio5Cliente` (Frontend MVC)  
> **Fecha de Elaboración:** Noviembre 2026  
> **Responsable:** Laura Berenice Tapia Cid  

---

## 1. Resumen Ejecutivo

Durante la auditoría estática de código, análisis de seguridad en SonarQube, análisis de cobertura con JaCoCo y linting de base de datos con sqlfluff sobre la arquitectura de microservicios, se verificaron los siguientes resultados finales en SonarQube (`localhost:9000`, proyecto `com.mx.cbtis:Ejercicio5MyAPIGestor`):
- **Quality Gate:** **PASSED (Aprobado)** en código nuevo y global.
- **Issues Abiertos Actuales:** **128 issues** clasificados por dimensiones de calidad de software:
  - **Security (Seguridad):** **15** abiertos (Mass Assignment `java:S4684` aceptados como deuda técnica documentada).
  - **Reliability (Confiabilidad):** **0** abiertos (100% libre de bugs de confiabilidad tras migrar la inyección de dependencias a constructores en el commit `6c8e852`).
  - **Maintainability (Mantenibilidad):** **113** abiertos (102 convenciones de nombres para compatibilidad con MariaDB, 4 métodos idénticos, 3 imports, 2 variables locales, 1 aserción y 1 código comentado).
- **Líneas Duplicadas:** **0.0%** en código nuevo y en el proyecto.
- **Cobertura de Pruebas:** **99.0%** de cobertura global en SonarQube y **98.2%** en código nuevo (JaCoCo reporta **100%** de métodos cubiertos: 119/119 en backend y 34/34 en frontend).

En comparación con el **escaneo inicial de referencia** (140 issues: 18 Security, 5 Reliability, 122 Maintainability), se resolvieron satisfactoriamente los 5 issues de confiabilidad (`java:S6813`), 3 issues de seguridad mediante el desacoplamiento con `UsuarioDTO`, y la deuda técnica de mantenibilidad asociada. Asimismo, se corrigieron vulnerabilidades y defectos arquitectónicos detectados por el equipo que no eran señalados por las reglas por defecto de SonarQube (contraseñas en texto plano, credenciales JDBC expuestas, antipatrones `.equals(null)` y dependencia de bases de datos externas en tests).

---

## 2. Clasificación Global de Issues en SonarQube (Referencia vs Actual)

| Dimensión de Calidad (Software Quality) | Escaneo Inicial (Referencia) | Estado Actual (Verificado en SonarQube) | Variación | Estado General / Remediación |
|---|---|---|---|---|
| **Security (Seguridad)** | **18** | **15** | **-3** | 3 endpoints de `UsuarioController` mitigados con `UsuarioDTO` (commits `ac8cb1f` y `f243c06`). 15 abiertos (`java:S4684`) aceptados como deuda técnica documentada. |
| **Reliability (Confiabilidad)** | **5** | **0** | **-5** | **100% Corregido.** Los 5 issues correspondían a inyección por campo (`java:S6813`), corregidos mediante inyección por constructor en los 5 controladores (commit `6c8e852`). |
| **Maintainability (Mantenibilidad)** | **122** | **113** | **-9** | Reducción neta tras eliminar `java:S6813`, reducir complejidad cognitiva (`java:S3776`) y unificar métodos duplicados (`java:S4144`). 113 abiertos aceptados justificadamente. |
| **Total Issues Abiertos** | **140** | **128** | **-12** | **Quality Gate APROBADO** (0 issues en código nuevo, 0.0% duplicación, 99.0% cobertura global, 98.2% código nuevo). |

> **Nota sobre las Dimensiones de Calidad:**  
> Las dimensiones de calidad de software en SonarQube clasifican el impacto en la salud del código. Los 5 issues iniciales de Reliability eran violaciones a la regla `java:S6813` (inyección por campo en controladores), los cuales afectaban simultáneamente Confiabilidad y Mantenibilidad. Al refactorizar los 5 controladores a inyección por constructor (commit `6c8e852`), la dimensión de Reliability se redujo a 0 y Maintainability descendió a 113.

---

## 3. Matriz Detallada de Hallazgos e Incidencias

### 3.1. Hallazgos Reportados por SonarQube

#### Desglose de los 128 Issues Abiertos Actuales en SonarQube:
- **Por Severidad:**
  - **Critical:** 15 issues (`java:S4684` - Mass Assignment en entidades persistentes).
  - **Major:** 5 issues (4 `java:S4144` por métodos con lógica similar en controladores restantes, 1 `java:S125` por bloque de comentario histórico).
  - **Minor:** 108 issues (45 `java:S117`, 38 `java:S100`, 19 `java:S116` por convenciones de nombres snake_case heredadas de MariaDB; 3 `java:S1128` imports; 2 `java:S1488` retornos inmediatos; 1 `java:S5838` aserción AssertJ).
- **Por Calidad de Software (Impact Software Qualities):**
  - **Security:** 15
  - **Reliability:** 0
  - **Maintainability:** 113

| # | Regla SonarQube | Descripción y Ubicación | Severidad / Dimensión | Estado en Sonar | Acción Tomada / Justificación Técnica | Commit |
|---|---|---|---|---|---|---|
| **S-01** | `java:S4684` | **Mass Assignment en UsuarioController:** Uso de entidad JPA `Usuario` en `@RequestBody` (`addUsuario`, `updateUsuario`, `getUsuarioLogin`) | Critical / Security | **Corregido** | Desacoplamiento mediante `UsuarioDTO`, validación con Bean Validation y mapeo defensivo interno `toEntity()`. | `ac8cb1f` / `f243c06` |
| **S-02** | `java:S4684` | **Mass Assignment en controladores restantes (15 issues):** Recepción de entidades JPA en `@RequestBody` en `TareasController` (3), `ProyectosController` (3), `ComentariosController` (3), `AsignacionesController` (3), `UsuarioController.deleteUsuario` (1) y entidades vinculadas | Critical / Security | **Abierto (Deuda Aceptada)** | **Riesgo real aceptado:** Los endpoints carecen de autenticación y autorización perimetral. Se acepta temporalmente para no romper contratos REST síncronos con `Ejercicio5Cliente`. Mitigación pendiente: implementar DTOs específicos y autenticación. | Documentado |
| **S-03** | `java:S6813` | **Inyección por campo (5 issues):** "Remove this field injection and use constructor injection instead" en `AsignacionesController`, `ComentariosController`, `ProyectosController`, `TareasController` y `UsuarioController` | Major / Reliability & Maintainability | **Corregido** | Se eliminó `@Autowired` sobre campos y se implementó inyección por constructor con campos `private final` en los 5 controladores. Confiabilidad pasa a 0 issues abiertos. | `6c8e852` |
| **S-04** | `java:S3776` | **Complejidad cognitiva alta:** Complejidad 16 en `UsuarioController.getUsuarioLogin` (límite permitido: 15) | Major / Maintainability | **Corregido** | Reducción de complejidad a 5 extrayendo los helpers privados `verificarPassword(...)` y `hashContrasena(...)`. | `f243c06` |
| **S-05** | `java:S4144` | **Métodos idénticos:** Implementación idéntica entre `addUsuario` y `updateUsuario` | Major / Maintainability | **Corregido** | Unificación en método privado común `guardarUsuario(UsuarioDTO)`. | `f243c06` |
| **S-06** | `java:S4144` | **Métodos con implementación idéntica en controladores restantes (4 issues):** Métodos de persistencia en `TareasController`, `ProyectosController`, etc. | Major / Maintainability | **Abierto (Deuda Aceptada)** | Se preserva la separación de métodos por verbo/ruta REST hasta la migración integral a DTOs de las entidades correspondientes. | Documentado |
| **S-07** | `java:S116`, `java:S117`, `java:S100` | **Convenciones de nombres en UsuarioDTO (15 issues):** Nombres en snake_case en atributos, parámetros y métodos accesores | Minor / Maintainability | **Corregido** | Refactorización a camelCase estándar en `UsuarioDTO` agregando anotaciones `@JsonProperty` para preservar la estructura JSON requerida por el frontend. | `54e02c6` |
| **S-08** | `java:S117`, `java:S100`, `java:S116` | **Convenciones de nombres en entidades JPA (102 issues):** 45 `java:S117`, 38 `java:S100`, 19 `java:S116` en entidades `Usuario`, `Tareas`, `Proyectos`, `Asignaciones`, `Comentarios` | Minor / Maintainability | **Abierto (Deuda Aceptada)** | **Justificación técnica:** Los atributos reflejan estrictamente las columnas snake_case de MariaDB (`id_usuario`, `correo_electronico`) y los bindings de vistas Thymeleaf en `Ejercicio5Cliente`. Renombrarlos en las entidades rompería el mapeo ORM y la compatibilidad frontend. | Documentado |
| **S-09** | `java:S1128` | **Imports no utilizados (3 issues):** Sentencias `import` redundantes en clases backend | Minor / Maintainability | **Abierto (Deuda Aceptada)** | Imports residuales inocuos sin impacto en runtime ni seguridad. | Documentado |
| **S-10** | `java:S1488` | **Variables locales redundantes (2 issues):** Variables declaradas e inmediatamente retornadas | Minor / Maintainability | **Abierto (Deuda Aceptada)** | Mantenidas para facilitar inspección con depurador en desarrollo. | Documentado |
| **S-11** | `java:S5838` | **Optimización de aserción AssertJ (1 issue):** Sugerencia de simplificación en suite de test | Minor / Maintainability | **Abierto (Deuda Aceptada)** | Aserción válida en pruebas unitarias; no afecta ejecución ni reporte. | Documentado |
| **S-12** | `java:S125` | **Código comentado (1 issue):** Fragmento de código comentado en backend | Major / Maintainability | **Abierto (Deuda Aceptada)** | Bloque conservado como documentación histórica de integración. | Documentado |

> **Nota de Configuración en pom.xml:**  
> Las propiedades `<sonar.exclusions>**/target/**</sonar.exclusions>` y `<sonar.cpd.exclusions>**/src/test/**</sonar.cpd.exclusions>` corresponden a configuraciones de escaneo estático y análisis de duplicación (CPD), respectivamente. Su objetivo es evitar falsos positivos en bytecode generado y aislar las aserciones repetitivas de las suites de prueba unitaria sin relajar la inspección ni excluir código fuente productivo de `src/main` (Commits `bec893b` y `23a722a`).

---

### 3.2. Hallazgos Encontrados por el Equipo (Auditoría Interna no Marcada por SonarQube)

Durante la revisión manual de arquitectura y seguridad del código, el equipo detectó defectos críticos adicionales no señalados por el perfil predeterminado de SonarQube:

| # | Hallazgo de Auditoría Interna | Ubicación | Severidad / Categoría | Clasificación | Acción Tomada / Mitigación Técnica | Commit |
|---|---|---|---|---|---|---|
| **E-01** | **Contraseñas almacenadas y autenticadas en texto plano** (CWE-256 / CWE-312 / OWASP A02:2021) | `Usuario.java`, `UsuarioRepository.java`, `UsuarioController.java` | Crítica / Seguridad | **Real Corregido** | Adopción de **BCrypt** (`spring-security-crypto`). Autenticación mediante búsqueda por correo y comparación segura con `passwordEncoder.matches()`. Generación del script `db/migracion_hash_passwords.sql` con hashes para los registros existentes. | `12e540e` |
| **E-02** | **Exposición de credenciales JDBC y usuario root en texto plano** (CWE-798 / OWASP A05:2021) | `Ejercicio5MyAPIGestor/.../application.properties` | Alta / Seguridad | **Real Corregido** | Parametrización mediante variables de entorno `${DB_URL}`, `${DB_USER}`, `${DB_PASS}` con fallbacks controlados. | `b145ce3` |
| **E-03** | **Bug de Confiabilidad por comprobación de nulidad con `.equals(null)`** (CWE-476 / NPE) | Los 5 controladores REST (`UsuarioController`, `TareasController`, `ProyectosController`, `ComentariosController`, `AsignacionesController`) | Major / Confiabilidad | **Real Corregido** | `x.equals(null)` arroja `NullPointerException` si `x` es nulo según el contrato de Java. Se reemplazó por la comprobación canónica `x != null`. | `e20a79e` / `812cede` |
| **E-04** | **Dependencia de base de datos MariaDB externa en fase de test (`mvn test`)** | `Ejercicio5MyAPIGestor/src/test/resources/application.properties` | Alta / Arquitectura CI | **Real Corregido** | Configuración de base de datos H2 en memoria con modo de compatibilidad MariaDB (`MODE=MariaDB`) en el entorno de pruebas, independizando la ejecución de pruebas unitarias y pipelines de servicios externos. | `b145ce3` / `812cede` |
| **E-05** | **Fragilidad ante respuestas HTTP nulas en llamadas REST (`RestTemplate`)** | `Ejercicio5Cliente/.../GestorController.java` | Major / Resiliencia | **Real Corregido** | Inyección de `RestTemplate` como `@Bean` singleton y manejo defensivo de respuestas nulas para evitar fallos por contingencias de red. | `52e1e74` |
| **E-06** | **Ausencia de cobertura en ramas defensivas (null checks en controladores)** | Métodos `add*`, `update*`, `delete*` de los 5 controladores | Major / Cobertura | **Real Corregido** | Creación de casos de prueba unitaria enviando `null` directamente a cada controlador, validando el retorno defensivo `0` y asegurando con Mockito que nunca se interactúa con el repositorio (`verify(..., never()).save/delete(...)`). | `6f7f55b` |
| **E-07** | **Inconsistencias de formato y estilo SQL en script de validación** (`LT01`, `LT02`, `LT05`, `LT09`, `CP02`, `AL01`) | `db/validate.sql` | Minor / Calidad SQL | **Real Corregido** | Creación del archivo de configuración `.sqlfluff` con dialecto MariaDB y formateo integral de 38 reglas con `sqlfluff fix` y validación manual contra MariaDB 11.3. | `9079971` / `1e0e1f1` |

---

## 4. Detalle Técnico de la Mitigación de Mass Assignment

### 4.1. Análisis del Riesgo (CWE-915)
Al recibir directamente una entidad persistente de JPA (`Usuario`) en un método anotado con `@RequestBody`:
1. Un atacante o cliente malicioso puede inyectar campos no previstos que se persisten directamente en la base de datos (por ejemplo, escalar privilegios alterando el campo `rol` o forzar un `id_usuario` no autorizado).
2. Se expone la estructura interna del modelo de persistencia (anotaciones `@Entity`, `@Table`, `@Id`).

### 4.2. Implementación de `UsuarioDTO`
Se diseñó la clase de transferencia de datos `UsuarioDTO` ubicada en `com.mx.cbtis.gestor.dto` utilizando la convención estándar `camelCase` para el cumplimiento de reglas SonarQube (`java:S116`, `java:S117`, `java:S100`), junto con `@JsonProperty` para conservar compatibilidad con el JSON en snake_case y Bean Validation (`javax.validation.constraints`):

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
- **Persistencia del riesgo (CWE-915 / Security High):** El riesgo de Mass Assignment (`java:S4684`, 15 issues abiertos) es un **riesgo real persistente**. Mientras estos controladores vinculen directamente entidades JPA al cuerpo de las solicitudes HTTP, un cliente malicioso puede enviar propiedades no deseadas que alteren el estado de persistencia.
- **Ausencia de autenticación en endpoints:** Actualmente los endpoints REST no cuentan con autenticación ni autorización perimetral (no existe Spring Security perimetral ni validación de tokens/JWT en las rutas). En consecuencia, cualquier cliente con acceso a la red puede invocar directamente los métodos de modificación y borrado.
- **Mitigación técnica pendiente:** La mitigación definitiva requiere:
  1. Diseñar e implementar los DTOs restantes (`TareasDTO`, `ProyectosDTO`, `ComentariosDTO`, `AsignacionesDTO` y DTOs de borrado seguro).
  2. Implementar un esquema formal de autenticación y autorización en la API (Spring Security con tokens JWT / OAuth2) para restringir el acceso a los endpoints.
- **Aceptación temporal:** Se acepta este riesgo como deuda técnica justificada de forma transitoria para no romper los contratos REST síncronos con `Ejercicio5Cliente` en el cierre de la Práctica 3, respaldando la integridad del sistema mediante validaciones defensivas de nulos y pruebas unitarias exhaustivas con Mockito (`verify(..., never())`).

---

## 6. Cobertura de Pruebas Unitarias y Métricas JaCoCo

Tras la adición de pruebas para todas las ramas nulas (`null-checks`), variantes de hashing, refactorización de controladores e inyección por constructor, las métricas de cobertura del Backend (`Ejercicio5MyAPIGestor`) verificadas contra `target/site/jacoco/jacoco.csv` son:

| Elemento | Total Instrucciones | Cobertura Instrucciones | Total Ramas (Branches) | Cobertura Ramas | Total Líneas | Cobertura Líneas | Total Métodos | Cobertura Métodos |
|---|---|---|---|---|---|---|---|---|
| `com.mx.cbtis.gestor.controller` | 527 | **100%** (527/527) | 70 | **97.1%** (68/70) | 150 | **100%** (150/150) | 37 | **100%** (37/37) |
| `com.mx.cbtis.gestor.modelo` | 320 | **100%** (320/320) | 0 | n/a | 136 | **100%** (136/136) | 68 | **100%** (68/68) |
| `com.mx.cbtis.gestor.dto` | 66 | **100%** (66/66) | 0 | n/a | 28 | **100%** (28/28) | 14 | **100%** (14/14) |
| **Total Global Backend** | **913** | **100%** (913/913) | **70** | **97.1%** (68/70) | **314** | **100%** (314/314) | **119** | **100%** (119/119) |

### Métricas Consolidadas:
- **Pruebas Unitarias Backend:** 77 ejecutadas, 0 fallos, 0 errores.
- **Pruebas Unitarias Frontend:** 34 ejecutadas, 0 fallos, 0 errores.
- **Total Pruebas Automatizadas:** **111 pruebas unitarias** ejecutadas exitosamente.
- **Cobertura de Métodos Evaluables:** **100%** (119/119 en backend, 34/34 en frontend).
- **Cobertura de Líneas en Backend:** **100%** (314/314 líneas cubiertas, 0 omitidas).
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
| **Cobertura en Código Nuevo** | 98.2% | **98.2%** | >= 80.0% | **Aprobado (OK)** |
| **Estado General Quality Gate** | **ERROR** | **OK** | **OK** | **APROBADO** |

---

## 8. Evidencia de Ejecución en Pipelines de CI/CD (Jenkins)

Las correcciones de código, pruebas unitarias y configuración de calidad fueron validadas mediante los pipelines declarativos automatizados en Jenkins. La siguiente tabla presenta la evidencia de las ejecuciones clave:

| Pipeline | Build # | Resultado Jenkins | Quality Gate SonarQube | Detalle y Observaciones |
|---|---|---|---|---|
| `database-quality-pipeline` | **#11** | **FAILURE** | **ERROR (Fallido)** | **Fallo inicial de Quality Gate:** Detección de 17 issues nuevos de mantenibilidad y 22.63% de líneas duplicadas en código nuevo. Detonó las refactorizaciones de la Práctica 3 Parte 2. |
| `database-quality-pipeline` | **#13** | **SUCCESS** | **OK (Aprobado)** | **Quality Gate superado exitosamente.** 0 issues nuevos, 0.0% duplicación de código, MariaDB efímera en Docker validada y script SQL 100% verificado con sqlfluff. |
| `backend-pipeline` | **#7** | **SUCCESS** | **Cumplido** | Ejecución limpia de 77 pruebas unitarias JUnit 5/Mockito, cobertura JaCoCo (100% métodos, 97.1% ramas, 100% líneas) y empaquetado del artefacto JAR de `Ejercicio5MyAPIGestor`. |
| `frontend-pipeline` | **#2** | **SUCCESS** | **Cumplido** | Ejecución limpia de 34 pruebas unitarias JUnit 5/Mockito, validación de integración REST síncrona y empaquetado del microservicio web `Ejercicio5Cliente`. |
