<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Casi listo - VitalBoost</title>
    <link rel="stylesheet" href="${ctx}/css/registroExitoso.css">
</head>

<body>

<%@ include file="/includes/loader.jspf" %>

    <div class="exito-blob exito-blob--1"></div>
    <div class="exito-blob exito-blob--2"></div>
    <div class="exito-blob exito-blob--3"></div>

    <div class="exito-card">

        <div class="exito-icono">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M4 4h16v16H4z"/><path d="m22 6-10 7L2 6"/>
            </svg>
        </div>

        <h1>¡Ya casi! Falta confirmar tu correo</h1>
        <p class="exito-sub">
            Gracias, <strong><c:out value="${nombrePaciente}" /></strong>.
            Tu cuenta y tu código QR de emergencia todavía no se han creado.
        </p>

        <div class="exito-nota exito-nota--correo">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M4 4h16v16H4z"/><path d="m22 6-10 7L2 6"/>
            </svg>
            <span>
                Te estamos enviando un correo de verificación a
                <strong><c:out value="${emailPaciente}" /></strong>; puede tardar unos segundos en llegar.
                Ábrelo y haz clic en el enlace para activar tu cuenta y generar tu código QR de emergencia.
                El enlace vence en 24 horas. Si no te llega, puedes reenviarlo más tarde desde la pantalla
                de inicio de sesión.
            </span>
        </div>

        <div class="exito-acciones">
            <a href="${ctx}/ServletLogin" class="exito-btn exito-btn--primary">
                Ir al inicio de sesión
            </a>
            <a href="${ctx}/index.jsp" class="exito-btn exito-btn--secundario">
                Volver al Inicio
            </a>
        </div>

    </div>

</body>
</html>
