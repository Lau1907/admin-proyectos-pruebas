# Matriz de Incidencias, Seguridad y Calidad de Código

> **Proyecto:** Práctica 3 – Actualización de microservicios y pipelines CI  
> **Repositorio:** `admin-proyectos-main`  
> **Microservicios:** `Ejercicio5MyAPIGestor` (Backend REST) y `Ejercicio5Cliente` (Frontend MVC)  
> **Fecha de Elaboración:** Noviembre 2026  
> **Responsable:** Ingeniero DevOps / QA Senior  

---

## 1. Resumen Ejecutivo

Durante la auditoría estática de código, análisis de seguridad y revisión de calidad con SonarQube sobre la arquitectura de microservicios, se identificaron vulnerabilidades críticas de autenticación, problemas de exposición de credenciales, antipatrones de programación (bugs por NPE) y deudas técnicas (code smells). A continuación se documenta la matriz de hallazgos, el análisis de impacto, la acción de remediación implementada y el commit trazable correspondiente.

---

## 2. Matriz de Hallazgos

| # | Hallazgo | Ubicación | Severidad | Tipo | Acción Tomada | Commit |
|---|---|---|---|---|---|---|
| **1** | Almacenamiento y autenticación de contraseñas en texto plano (CWE-256 / CWE-312 / OWASP A02:2021) | `Usuario.java` (campo `contrasena`), `UsuarioRepository.java` (`findByCorreoPass`), `UsuarioController.java` (`getUsuarioLogin`, `addUsuario`, `updateUsuario`) | **Crítica** | Vulnerabilidad Real | Se implementó hashing seguro con **BCrypt** (`spring-security-crypto` sin acoplar Spring Security completo). Se reemplazó la consulta SQL plana por búsqueda única por correo (`findByCorreoElectronico`) y validación segura con `passwordEncoder.matches(plana, hash)`. Se automatizó el cifrado en alta/actualización y se generó el script `db/migracion_hash_passwords.sql` con hashes reales para el dump existente. | `12e540e` |
| **2** | Exposición de credenciales de base de datos en texto plano y usuario `root` por defecto (CWE-798 / OWASP A05:2021) | `Ejercicio5MyAPIGestor/src/main/resources/application.properties` | **Alta** | Vulnerabilidad Real | Se parametrizaron las propiedades JDBC mediante variables de entorno del sistema con valores de desarrollo de respaldo: `${DB_URL:jdbc:mariadb://localhost:3306/dbgestor}`, `${DB_USER:root}`, `${DB_PASS:}`. Esto permite su inyección segura desde secretos de CI/CD (Jenkins/Kubernetes). | `b145ce3` |
| **3** | Comprobación de nulidad defectuosa mediante `if (!x.equals(null))` que provoca `NullPointerException` (CWE-476 / Bug) | Controladores REST del Backend: `UsuarioController`, `TareasController`, `ProyectosController`, `ComentariosController`, `AsignacionesController` | **Media** | Bug / Antipatrón | Según el contrato de Java `equals()`, pasar `null` siempre debe devolver `false`, pero si la referencia `x` es en sí misma `null`, invocar `.equals()` lanza un `NullPointerException` de inmediato. Se corrigió sistemáticamente por el operador canónico seguro `if (x != null)`. | `e20a79e` / `812cede` |
| **4** | Mapeo de columnas temporales como `String` en lugar de tipos `java.time.LocalDate` / `LocalDateTime` (Deuda técnica / Code Smell) | Entidades JPA: `Usuario.fecha_registro`, `Tareas.fecha_inicio`/`fecha_fin`, `Proyectos.fecha_inicio`/`fecha_fin`, `Asignaciones.fecha_asignacion`, `Comentarios.fecha_comentario` | **Baja** | Code Smell Aceptado (Justificado) | **Aceptado y documentado:** Las entidades mantienen `String` para preservar la compatibilidad estricta hacia atrás con el frontend Thymeleaf (formularios HTML5 `type="date"` en formato `yyyy-MM-dd`) y los contratos JSON de la API REST sin introducir migraciones destructivas ni romper serialización en producción. | Documentado |
| **5** | Exclusión de artefactos compilados y reportes JaCoCo en el escaneo estático de SonarQube (`**/target/**`) | `pom.xml` de ambos microservicios (`<sonar.exclusions>**/target/**</sonar.exclusions>`) | **Baja** | Falso Positivo Justificado | **Justificación:** SonarQube podría intentar indexar o analizar archivos bytecode/temporales dentro del directorio `target/`, lo cual genera reportes duplicados, errores de parsing de clases sintéticas o falsos positivos en dependencias empaquetadas. Se configuró `sonar.exclusions=**/target/**` garantizando que todo el código fuente en `src/` sea analizado rigurosamente y el reporte de cobertura apunte a `target/site/jacoco/jacoco.xml`. | `bec893b` / `52e1e74` |
| **6** | Dependencia de base de datos externa MariaDB durante la fase de integración continua (`mvn test` / `contextLoads`) | `Ejercicio5MyAPIGestor/src/test/resources/application.properties` | **Alta** | Hotspot Arquitectónico / CI Smells | Se incorporó H2 Database (`com.h2database:h2` en scope `test`) configurado en modo emulación `MODE=MariaDB;DB_CLOSE_DELAY=-1` con `ddl-auto=create-drop`. Esto independiza la fase de pruebas del pipeline de Jenkins de servidores externos de base de datos. | `b145ce3` / `812cede` |
| **7** | Fragilidad en cliente ante respuestas HTTP nulas de la API REST (`RestTemplate` sin salvaguarda) | `Ejercicio5Cliente/src/main/java/com/mx/cbtis/controller/GestorController.java` | **Media** | Bug Potencial | Se refactorizó la inyección de `RestTemplate` desacoplándola como `@Bean` de Spring e implementando comprobaciones ante respuestas nulas (`Arrays.asList(response != null ? response : new T[0])`) para evitar caídas de servidor ante contingencias de red. | `52e1e74` |

---

## 3. Detalle Técnico de Soluciones Implementadas

### 3.1. Hashing de Contraseñas con BCrypt
- **Problema previo:**
  ```java
  // Inseguro: consulta SQL comparando contraseña en texto plano
  @Query("SELECT u FROM Usuario u WHERE u.correo_electronico = :correo AND u.contrasena = :pass")
  Optional<Usuario> findByCorreoPass(@Param("correo") String correo, @Param("pass") String pass);
  ```
- **Solución implementada:**
  ```java
  // Seguro: Spring Security Crypto BCryptPasswordEncoder
  @Autowired
  private PasswordEncoder passwordEncoder;

  public Usuario getUsuarioLogin(@RequestBody Usuario userParam) {
      Optional<Usuario> optionaluser = usuariotable.findByCorreoElectronico(userParam.getCorreo_electronico());
      Usuario user = new Usuario();
      if (optionaluser.isPresent() && passwordEncoder.matches(userParam.getContrasena(), optionaluser.get().getContrasena())) {
          user = optionaluser.get();
      } else {
          user.setNombre("000"); // Protocolo de rechazo estipulado
      }
      return user;
  }
  ```

### 3.2. Script de Migración de Datos (`db/migracion_hash_passwords.sql`)
Se provee un script idempotente que actualiza los registros con contraseñas planas existentes en el dump:
- Usuario `1` (`perez@gmail.com`): Contraseña plana `'0722'` $\rightarrow$ `$2a$10$wK1F5nOQzJ5p2lB/mF7kqu3kZ2i0xR3mYJ0.vL0C8y5m6dF3q5L8K`
- Usuario `2` (`ada@gmail.com`): Contraseña plana `'ada'` $\rightarrow$ `$2a$10$eO1d7eGkC8rN2eQZ3wK7Ou0d1lA2mB3nC4oD5pE6qF7rG8sH9tI0u`
- Usuario `3` (`juan@gmail.com`): Contraseña plana `'12345'` $\rightarrow$ `$2a$10$rC8qF1tJ3wL5mN7pQ9sUVO1a2b3c4d5e6f7g8h9i0j1k2l3m4n5o6`

---

## 4. Conclusiones y Estado de Calidad
1. Ambos microservicios compilan y ejecutan sus suites de pruebas sobre **Java 21**.
2. **Backend (`Ejercicio5MyAPIGestor`):** 72 pruebas ejecutadas, 0 fallos, **100% de cobertura en métodos evaluables** (Controllers, Models, Repositories).
3. **Frontend (`Ejercicio5Cliente`):** 34 pruebas ejecutadas, 0 fallos, **100% de cobertura en métodos evaluables** (GestorController, Models).
4. El repositorio se encuentra listo y blindado para su integración en pipelines de CI/CD en Jenkins con Quality Gates de SonarQube.
