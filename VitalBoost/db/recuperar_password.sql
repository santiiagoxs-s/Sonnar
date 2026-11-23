-- ==========================================================================
-- VitalBoost · Recuperación de contraseña
-- Ejecutar UNA sola vez contra la base de Railway (MySQL) antes de
-- desplegar el código nuevo. No reemplaza a verificacion_email.sql, es un
-- paso adicional.
--
-- Se usan columnas propias (token_recuperacion / token_recuperacion_expira)
-- separadas de token_verificacion / token_expira, para que pedir un cambio
-- de contraseña nunca invalide un enlace de verificación de correo
-- pendiente (y viceversa).
-- ==========================================================================

ALTER TABLE Usuarios
    ADD COLUMN token_recuperacion VARCHAR(64) NULL,
    ADD COLUMN token_recuperacion_expira DATETIME NULL;
