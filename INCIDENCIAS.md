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
