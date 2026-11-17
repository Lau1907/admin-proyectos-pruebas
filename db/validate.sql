-- =============================================================================
-- Script de Validación de Calidad, Integridad y Seguridad de Base de Datos
-- Dialecto: MariaDB 11.3 / MySQL
-- Ubicación: db/validate.sql
-- Propósito: Auditoría automática en pipelines de CI/CD (ci/Jenkinsfile.database)
-- Columnas devueltas: prueba, encontradas, resultado (OK / FALLO / HALLAZGO)
-- =============================================================================

-- 1. Existencia de las 5 tablas del esquema
SELECT
    '1. Existencia de las 5 tablas principales (usuario, proyectos, tareas, comentarios, asignaciones)' AS prueba,
    COUNT(*) AS encontradas,
    CASE
        WHEN COUNT(*) = 5 THEN 'OK'
        ELSE 'FALLO'
    END AS resultado
FROM information_schema.tables
WHERE
    table_schema = DATABASE()
    AND table_name IN ('usuario', 'proyectos', 'tareas', 'comentarios', 'asignaciones')

UNION ALL

-- 2. Llave primaria de la tabla usuario
SELECT
    '2. Llave primaria definida en tabla usuario (PRIMARY KEY)' AS prueba,
    COUNT(*) AS encontradas,
    CASE
        WHEN COUNT(*) >= 1 THEN 'OK'
        ELSE 'FALLO'
    END AS resultado
FROM information_schema.table_constraints
WHERE
    table_schema = DATABASE()
    AND table_name = 'usuario'
    AND constraint_type = 'PRIMARY KEY'

UNION ALL

-- 3. Datos cargados en la tabla usuario
SELECT
    '3. Registros de datos cargados en tabla usuario' AS prueba,
    COUNT(*) AS encontradas,
    CASE
        WHEN COUNT(*) > 0 THEN 'OK'
        ELSE 'FALLO'
    END AS resultado
FROM usuario

UNION ALL

-- 4. Datos cargados en la tabla proyectos
SELECT
    '4. Registros de datos cargados en tabla proyectos' AS prueba,
    COUNT(*) AS encontradas,
    CASE
        WHEN COUNT(*) > 0 THEN 'OK'
        ELSE 'FALLO'
    END AS resultado
FROM proyectos

UNION ALL

-- 5. Detección de tareas huérfanas (sin proyecto padre correspondiente)
SELECT
    '5. Detección de tareas huérfanas (id_proyecto inexistente en proyectos)' AS prueba,
    COUNT(*) AS encontradas,
    CASE
        WHEN COUNT(*) = 0 THEN 'OK'
        ELSE 'FALLO'
    END AS resultado
FROM tareas AS t
LEFT JOIN proyectos AS p ON t.id_proyecto = p.id_proyecto
WHERE t.id_proyecto IS NOT NULL AND p.id_proyecto IS NULL

UNION ALL

-- 6. Detección de correos electrónicos duplicados en tabla usuario
SELECT
    '6. Detección de correos electrónicos duplicados en usuario' AS prueba,
    COUNT(*) AS encontradas,
    CASE
        WHEN COUNT(*) = 0 THEN 'OK'
        ELSE 'HALLAZGO'
    END AS resultado
FROM (
    SELECT
        correo_electronico,
        COUNT(*) AS cant
    FROM usuario
    WHERE correo_electronico IS NOT NULL
    GROUP BY correo_electronico
    HAVING COUNT(*) > 1
) AS dupes

UNION ALL

-- 7. Detección de contraseñas en texto plano (sin hash seguro BCrypt, longitud < 60)
SELECT
    '7. Detección de contraseñas sin hash seguro BCrypt (longitud < 60)' AS prueba,
    COUNT(*) AS encontradas,
    CASE
        WHEN COUNT(*) = 0 THEN 'OK'
        ELSE 'HALLAZGO'
    END AS resultado
FROM usuario
WHERE contrasena IS NULL OR LENGTH(contrasena) < 60;
