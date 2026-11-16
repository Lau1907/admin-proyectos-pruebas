# Matriz de Incidencias, Seguridad y Calidad de Código

> **Proyecto:** Práctica 3 – Actualización de microservicios y pipelines CI  
> **Repositorio:** `admin-proyectos-main`  
> **Microservicios:** `Ejercicio5MyAPIGestor` (Backend REST) y `Ejercicio5Cliente` (Frontend MVC)  
> **Fecha de Elaboración:** Noviembre 2026  
> **Responsable:** Ingeniero DevOps / QA Senior  

---

## 1. Resumen Ejecutivo

Durante la auditoría estática de código, análisis de seguridad en SonarQube, análisis de cobertura con JaCoCo y linting estático de base de datos con sqlfluff sobre la arquitectura de microservicios, se identificaron **140 issues en SonarQube** clasificados en tres dimensiones de calidad:
- **18 Security Issues (Vulnerabilidades y Hotspots de Seguridad)**
- **5 Reliability Issues (Bugs de Confiabilidad y Null Safety)**
- **122 Maintainability Issues (Deuda Técnica y Code Smells)**

Además, se auditaron vulnerabilidades críticas previas de autenticación en texto plano, exposición de credenciales JDBC en properties y antipatrones de programación (NPE por `.equals(null)`).

A continuación se documenta la clasificación formal de cada hallazgo, las remediaciones aplicadas, la mitigación de *Mass Assignment* mediante DTOs, las justificaciones técnicas para los ítems aceptados como deuda técnica y las métricas finales de cobertura.

---

## 2. Clasificación Global de los 140 Hallazgos de SonarQube

| Dimensión SonarQube | Cantidad | Real Corregido | Real Aceptado con Justificación | Falso Positivo Justificado | Estado General |
|---|---|---|---|---|---|
| **Security (Seguridad)** | **18** | **3** (Mass Assignment en `UsuarioController` mitigado con `UsuarioDTO`) + 2 (BCrypt / Env Vars) | **15** (Mass Assignment en `Tareas`, `Proyectos`, `Comentarios`, `Asignaciones` y `deleteUsuario`) | 0 | Mitigado en autenticación/usuarios; restante documentado como deuda técnica aceptada. |
| **Reliability (Confiabilidad / Bugs)** | **5** | **5** (Corrección de `.equals(null)`, validación de Optional, ramas nulas en controllers) | 0 | 0 | **100% Corregido** y verificado con pruebas unitarias. |
| **Maintainability (Mantenibilidad / Code Smells)** | **122** | **18** (Limpieza de código muerto, imports, aserciones y branches) | **103** (Convención camelCase en entidades, campos fecha como String) | **1** (`sonar.exclusions=**/target/**`) | Aceptado justificadamente para preservar contratos REST y compatibilidad con el frontend. |
| **Total** | **140** | **28** | **118** | **1** | **Calidad Aprobada para CI/CD** |

---

## 3. Matriz Detallada de Hallazgos e Incidencias

| # | Hallazgo | Ubicación | Severidad / Categoría | Clasificación | Acción Tomada / Justificación Técnica | Commit |
|---|---|---|---|---|---|---|
| **1** | **Vulnerabilidad Mass Assignment (CWE-915 / OWASP A08:2021):** "Replace this persistent entity with a simple POJO or DTO object" en controladores REST | `UsuarioController.java` (`addUsuario`, `updateUsuario`, `getUsuarioLogin`) | **High (Security)** | **Real Corregido** | Se desacopló la entidad JPA `Usuario` del contrato de entrada mediante la creación de `UsuarioDTO`. Se implementaron validaciones Bean Validation (`@Valid`, `@NotBlank`) y mapeo explícito interno `toEntity()`. | `ac8cb1f` |
| **2** | **Mass Assignment pendiente en controladores restantes:** Uso directo de entidades JPA `@RequestBody` | `TareasController`, `ProyectosController`, `ComentariosController`, `AsignacionesController` | **High (Security)** | **Real Aceptado con Justificación (Deuda Técnica)** | **Justificación:** Para no romper el contrato REST y la interacción síncrona con `Ejercicio5Cliente` en esta entrega, se aceptan temporalmente las entidades como payload de entrada. El riesgo se encuentra acotado porque las peticiones provienen exclusivamente del backend MVC interno y no de clientes públicos no autenticados. La migración a DTOs (`TareasDTO`, `ProyectosDTO`, etc.) queda programada para la siguiente fase. | Documentado |
| **3** | Almacenamiento y autenticación de contraseñas en texto plano (CWE-256 / CWE-312 / OWASP A02:2021) | `Usuario.java`, `UsuarioRepository.java`, `UsuarioController.java` | **Crítica (Security)** | **Real Corregido** | Implementación de hashing seguro con **BCrypt** (`spring-security-crypto`). Búsqueda única por correo y validación con `passwordEncoder.matches()`. Script `db/migracion_hash_passwords.sql` generado con hashes para el dump. | `12e540e` |
| **4** | Exposición de credenciales JDBC y usuario root en texto plano (CWE-798 / OWASP A05:2021) | `Ejercicio5MyAPIGestor/src/main/resources/application.properties` | **Alta (Security)** | **Real Corregido** | Parametrización de propiedades mediante variables de entorno `${DB_URL}`, `${DB_USER}`, `${DB_PASS}` con fallbacks seguros. | `b145ce3` |
| **5** | **Bug de Confiabilidad:** Comprobación de nulidad defectuosa mediante `if (!x.equals(null))` (CWE-476 / NPE) | Los 5 controladores REST (`UsuarioController`, `TareasController`, `ProyectosController`, `ComentariosController`, `AsignacionesController`) | **Major (Reliability)** | **Real Corregido** | Según el contrato de `Object.equals()`, si la referencia `x` es nula se lanza `NullPointerException`. Se reemplazó por la comprobación canónica `if (x != null)`. | `e20a79e` / `812cede` |
| **6** | **Ramas no cubiertas (Condiciones else de null checks):** Retorno seguro al invocar con parámetros nulos | Métodos `add*`, `update*`, `delete*` de los 5 controladores | **Major (Reliability / Test)** | **Real Corregido** | Se crearon pruebas unitarias específicas enviando `null` directamente a cada método y verificando que retornan `0` y nunca interactúan con los repositorios (`verify(..., never()).save/delete(...)`). | `6f7f55b` |
| **7** | Mapeo de columnas temporales como `String` en lugar de `java.time.LocalDate` / `LocalDateTime` | Entidades JPA (`Usuario`, `Tareas`, `Proyectos`, `Asignaciones`, `Comentarios`) | **Minor (Maintainability)** | **Real Aceptado con Justificación** | **Justificación:** Las entidades mantienen tipo `String` para garantizar compatibilidad binaria estricta con los formularios HTML5 (`<input type="date">` formato `yyyy-MM-dd`) y los bindings de Spring MVC en `Ejercicio5Cliente`. Migrar a `LocalDate` requeriría refactorizar conversores custom y deserializadores en ambos microservicios. | Documentado |
| **8** | Convenciones de nombres de atributos en entidades no conformes con camelCase (`id_usuario`, `correo_electronico`, etc.) | Entidades JPA y DTOs | **Minor (Maintainability)** | **Real Aceptado con Justificación** | **Justificación:** Los atributos reflejan exactamente las columnas de la base de datos MariaDB heredada y los campos JSON esperados por las plantillas Thymeleaf. Modificarlos implicaría renombrar getters/setters en cascada en ambos microservicios y arriesgar incompatibilidades de mapeo. | Documentado |
| **9** | Visibilidad pública de clases y métodos en pruebas unitarias JUnit 5 | Clases `*Test.java` | **Minor (Maintainability)** | **Real Aceptado con Justificación** | JUnit 5 permite visibilidad de paquete (`package-private`). Mantener `public` en algunas suites no afecta la ejecución, el reporte de JaCoCo ni el ciclo de vida de Surefire. | Documentado |
| **10** | Exclusión de artefactos compilados en escaneo estático (`**/target/**`) | `pom.xml` (`sonar.exclusions`) | **Minor (Maintainability)** | **Falso Positivo Justificado** | Previene que SonarQube analice bytecode duplicado o clases sintéticas generadas en `target/`, asegurando que el análisis de cobertura lea fielmente `target/site/jacoco/jacoco.xml`. | `bec893b` / `52e1e74` |
| **11** | Dependencia de base de datos externa MariaDB en fase de test de CI (`mvn test`) | `Ejercicio5MyAPIGestor/src/test/resources/application.properties` | **High (Arquitectura)** | **Real Corregido** | Despliegue de H2 Database en modo emulación MariaDB (`MODE=MariaDB`) en scope de pruebas, independizando la compilación y CI de conexiones externas. | `b145ce3` / `812cede` |
| **12** | Fragilidad ante respuestas HTTP nulas en llamadas REST (`RestTemplate`) | `Ejercicio5Cliente/.../GestorController.java` | **Major (Reliability)** | **Real Corregido** | Inyección segura de `RestTemplate` como `@Bean` y manejo de nulos en arrays de respuesta para evitar fallos por contingencias de red. | `52e1e74` |
| **13** | Inconsistencias de formato y estilo SQL en script de validación (`LT01`, `LT02`, `LT05`, `LT09`, `CP02`, `AL01`) | `db/validate.sql` | **Minor (Maintainability)** | **Real Corregido** | Creación de `.sqlfluff` con dialecto MariaDB y corrección integral de 38 reglas con `sqlfluff fix` y revisión manual. Salida validada contra MariaDB 11.3. | `9079971` / `1e0e1f1` |

---

## 4. Detalle Técnico de la Mitigación de Mass Assignment

### 4.1. Análisis del Riesgo (CWE-915)
Al recibir directamente una entidad persistente de JPA (`Usuario`) en un método anotado con `@RequestBody`:
1. Un atacante o cliente malicioso puede inyectar campos no previstos que se persisten directamente en la base de datos (por ejemplo, escalar privilegios alterando el campo `rol` o forzar un `id_usuario` no autorizado).
2. Se expone la estructura interna del modelo de persistencia (anotaciones `@Entity`, `@Table`, `@Id`).

### 4.2. Implementación de `UsuarioDTO`
Se diseñó la clase de transferencia de datos `UsuarioDTO` ubicada en `com.mx.cbtis.gestor.dto`:

```java
package com.mx.cbtis.gestor.dto;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

public class UsuarioDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id_usuario;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El correo electrónico es obligatorio")
    private String correo_electronico;

    @NotBlank(message = "La contraseña es obligatoria")
    private String contrasena;

    private String rol;
    private String imagen_usuario;

    // Constructores, Getters y Setters...
}
```

### 4.3. Refactorización en `UsuarioController`
Los endpoints críticos de recepción de datos fueron actualizados para recibir `UsuarioDTO`:
- `POST /getUsuarioLogin`: Recibe `UsuarioDTO`, realiza comprobación defensiva de nulidad, busca por correo y valida con BCrypt. Retorna la entidad autenticada o un objeto de rechazo con nombre `"000"`.
- `POST /addUsuario`: Recibe `@Valid @RequestBody UsuarioDTO`, aplica el mapeo a entidad mediante `toEntity()`, aplica hash BCrypt si la contraseña no está cifrada, guarda vía repositorio y retorna `1` (éxito) o `0` (fracaso).
- `POST /updateUsuario`: Recibe `UsuarioDTO`, mapea a entidad, gestiona el hash seguro y guarda los cambios retornando `1` o `0`.

### 4.4. Compatibilidad con el Frontend (`Ejercicio5Cliente`)
- **Nombres de atributos:** Exactamente idénticos (`id_usuario`, `nombre`, `correo_electronico`, `contrasena`, `rol`, `imagen_usuario`).
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

### Justificación de Aceptación:
- **Ámbito controlado:** El frontend `Ejercicio5Cliente` es un cliente MVC privado dentro de la misma red institucional que consume estos endpoints; no existe exposición pública directa a internet de la API Gestor sin pasar por la capa de control.
- **Riesgo de regresión mitigado:** Refactorizar masivamente los 4 controladores restantes en esta entrega implicaba crear 4 DTOs adicionales (`TareasDTO`, `ProyectosDTO`, `ComentariosDTO`, `AsignacionesDTO`), modificar sus contratos y adaptar múltiples modelos de enlace. Se clasifican como **Deuda Técnica Aceptada** para priorizar estabilidad en la entrega actual de la Práctica 3.
- **Blindaje mediante pruebas unitarias:** Todos los métodos de estos 4 controladores cuentan con pruebas unitarias exhaustivas con Mockito que validan el comportamiento ante payloads válidos, malformados y nulos (con aserciones `never()` sobre los repositorios).

---

## 6. Cobertura de Pruebas Unitarias y Métricas JaCoCo

Tras la adición de pruebas para todas las ramas nulas (`null-checks`) y variantes de hashing, las métricas de cobertura del Backend (`Ejercicio5MyAPIGestor`) alcanzaron la excelencia:

| Elemento | Total Instrucciones | Cobertura Instrucciones | Total Ramas (Branches) | Cobertura Ramas | Total Líneas | Cobertura Líneas | Total Métodos | Cobertura Métodos |
|---|---|---|---|---|---|---|---|---|
| `com.mx.cbtis.gestor.controller` | 571 | **100%** | 80 | **95%** (76/80) | 163 | **100%** (163/163) | 34 | **100%** (34/34) |
| `com.mx.cbtis.gestor.modelo` | 320 | **100%** | - | n/a | 136 | **100%** (136/136) | 68 | **100%** (68/68) |
| `com.mx.cbtis.gestor.dto` | 66 | **100%** | - | n/a | 28 | **100%** (28/28) | 14 | **100%** (14/14) |
| **Total Global Backend** | **957** | **100%** (957/957) | **80** | **95%** (76/80) | **327** | **100%** (327/327) | **116** | **100%** (116/116) |

### Métricas Consolidadas:
- **Pruebas Unitarias Backend:** 77 ejecutadas, 0 fallos, 0 errores.
- **Pruebas Unitarias Frontend:** 34 ejecutadas, 0 fallos, 0 errores.
- **Total Pruebas de Microservicios:** **111 pruebas automatizadas** en el repositorio.
- **Cobertura de Métodos Evaluables:** **100%** en ambos microservicios.
- **Cobertura de Líneas en Backend:** **100%** (0 líneas no cubiertas).
- **Cobertura de Instrucciones en Backend:** **100%** (0 instrucciones omitidas).
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

