-- ==========================================================================
-- VitalBoost · Verificación de correo
-- Ejecutar UNA sola vez contra la base de Railway (MySQL) antes de
-- desplegar el código nuevo.
-- ==========================================================================

ALTER TABLE Usuarios
    ADD COLUMN email_verificado TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN token_verificacion VARCHAR(64) NULL,
    ADD COLUMN token_expira DATETIME NULL;

-- Los usuarios que ya existen (admins, médicos, pacientes creados antes de
-- este cambio) quedan verificados automáticamente para que nadie se quede
-- bloqueado del login por una regla que no existía cuando se registraron.
UPDATE Usuarios SET email_verificado = 1;
