-- ==============================================================================
-- Script de Migración: Hashing de Contraseñas con BCrypt
-- Base de Datos: dbgestor
-- Práctica 3 - Tarea 3a: Corrección de contraseñas en texto plano
-- ==============================================================================
-- Contexto:
-- El dump original (dump-dbgestor-202406261453.sql) almacenaba contraseñas en texto plano.
-- Este script actualiza los registros existentes para reemplazar las contraseñas planas
-- por su correspondiente hash BCrypt (factor de costo 10), compatible con
-- org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.
-- ==============================================================================

USE `dbgestor`;

-- 1. Actualizar usuario ID 131 (Ana Paola Ramírez Vázquez - contraseña original: '0722')
UPDATE `usuario`
SET `contrasena` = '$2a$10$fFP6kKLSKQpMTALHcHkfm.N8WRtP8TfpSxhFyC.1dUsbHW.Vxy4uy'
WHERE `id_usuario` = 131 AND `contrasena` = '0722';

-- 2. Actualizar usuario ID 172 (Liliana Gómez Martínez - contraseña original: 'ada')
UPDATE `usuario`
SET `contrasena` = '$2a$10$YU59M0f6l.LjPhrFXeoFj.6n3zr.FKRmwAFazjK7tw1i6V6T2ytWa'
WHERE `id_usuario` = 172 AND `contrasena` = 'ada';

-- 3. Actualizar usuario ID 196 (Perla Joceline Martínez Meza - contraseña original: '12345')
UPDATE `usuario`
SET `contrasena` = '$2a$10$LESq7.JheaOle7DQJHCA8uUTxTsyVz3DRfhLroNATMrm6Ku.XPYYy'
WHERE `id_usuario` = 196 AND `contrasena` = '12345';

-- 4. Verificación de seguridad: confirmar que no quedan contraseñas en texto plano (longitud < 60)
SELECT 
    `id_usuario`,
    `nombre`,
    `correo_electronico`,
    LENGTH(`contrasena`) AS `longitud_hash`,
    CASE 
        WHEN `contrasena` LIKE '$2a$%' AND LENGTH(`contrasena`) = 60 THEN 'HASH_VALIDO_BCRYPT'
        ELSE 'ALERTA_TEXTO_PLANO'
    END AS `estado_seguridad`
FROM `usuario`;
