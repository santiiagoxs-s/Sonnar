-- ==========================================================================
-- VitalBoost · Cuenta solo se crea al verificar el correo
-- Ejecutar UNA sola vez contra la base de Railway (MySQL) antes de
-- desplegar el código nuevo. No reemplaza a verificacion_email.sql, es un
-- paso adicional.
--
-- Motivo: antes el paciente quedaba insertado en Usuarios (con
-- email_verificado = 0) desde el momento del registro, así que su QR de
-- emergencia y sus datos ya existían en el sistema aunque nunca confirmara
-- el correo. Ahora el registro público NO toca la tabla Usuarios: los datos
-- quedan "en espera" aquí, y solo se crean en Usuarios (y su contacto de
-- emergencia) cuando el paciente hace clic en el enlace de verificación.
-- ==========================================================================

CREATE TABLE Registros_pendientes (
    id_pendiente INT AUTO_INCREMENT PRIMARY KEY,

    id_rol INT NOT NULL,
    id_tipo_documento INT NOT NULL,
    id_tipo_sangre INT NOT NULL,

    numero_documento VARCHAR(20) NOT NULL,
    nombre_completo VARCHAR(150) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    fecha_nacimiento VARCHAR(20) NOT NULL,
    alergias_conocidas VARCHAR(255),
    eps VARCHAR(150),
    medicamentos_actuales VARCHAR(255),

    nombre_contacto VARCHAR(150) NOT NULL,
    parentesco_contacto VARCHAR(100),
    telefono_contacto VARCHAR(20) NOT NULL,

    token_verificacion VARCHAR(64) NOT NULL,
    token_expira DATETIME NOT NULL,
    creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY uk_pendiente_token (token_verificacion),
    UNIQUE KEY uk_pendiente_email (email),
    UNIQUE KEY uk_pendiente_documento (numero_documento)
);
