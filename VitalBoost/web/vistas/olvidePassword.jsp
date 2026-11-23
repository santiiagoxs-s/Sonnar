<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Recuperar contraseña - VitalBoost</title>
    <link rel="stylesheet" href="${ctx}/css/registroExitoso.css">
</head>

<body>

<%@ include file="/includes/loader.jspf" %>

    <div class="exito-blob exito-blob--1"></div>
    <div class="exito-blob exito-blob--2"></div>
    <div class="exito-blob exito-blob--3"></div>

    <div class="exito-card">

        <c:choose>

            <%-- ================= ENLACE ENVIADO ================= --%>
            <c:when test="${enlaceEnviado}">
                <div class="exito-icono">
                    <svg viewBox="0 0 24 24" fill="none" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M20 6 9 17l-5-5"/>
                    </svg>
                </div>
                <h1>Revisa tu correo</h1>
                <p class="exito-sub">
                    Si el correo que ingresaste está registrado en VitalBoost, te enviamos un enlace
                    para restablecer tu contraseña. Revisa tu bandeja de entrada (y la carpeta de spam);
                    el enlace vence en 1 hora.
                </p>
                <div class="exito-acciones">
                    <a href="${ctx}/ServletLogin" class="exito-btn exito-btn--secundario">Volver al inicio de sesión</a>
                </div>
            </c:when>

            <%-- ================= FORMULARIO ================= --%>
            <c:otherwise>
                <div class="exito-icono exito-icono--advertencia">
                    <svg viewBox="0 0 24 24" fill="none" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                        <rect x="3" y="11" width="18" height="10" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                    </svg>
                </div>
                <h1>¿Olvidaste tu contraseña?</h1>
                <p class="exito-sub">
                    Ingresa el correo con el que te registraste y te enviaremos un enlace para
                    restablecerla.
                </p>

                <form action="${ctx}/RecuperarPassword" method="POST" class="exito-form-reenviar">
                    <input type="hidden" name="accion" value="EnviarEnlace">
                    <input type="email" name="txtEmail" placeholder="nombre@ejemplo.com"
                           value="${txtEmailOld}" maxlength="100" required>

                    <c:if test="${not empty errorEmail}">
                        <small class="msg error">${errorEmail}</small>
                    </c:if>

                    <button type="submit" class="exito-btn exito-btn--primary">Enviar enlace</button>
                </form>

                <div class="exito-acciones" style="margin-top: 18px;">
                    <a href="${ctx}/ServletLogin" class="exito-btn exito-btn--secundario">Volver al inicio de sesión</a>
                </div>
            </c:otherwise>

        </c:choose>

    </div>

</body>
</html>
