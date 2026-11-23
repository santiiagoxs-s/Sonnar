<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Restablecer contraseña - VitalBoost</title>
    <link rel="stylesheet" href="${ctx}/css/registroExitoso.css">
    <link rel="stylesheet" href="${ctx}/css/global.css">
    <link rel="stylesheet" href="${ctx}/css/recuperarPassword.css">
</head>

<body>

<%@ include file="/includes/loader.jspf" %>

    <div class="exito-blob exito-blob--1"></div>
    <div class="exito-blob exito-blob--2"></div>
    <div class="exito-blob exito-blob--3"></div>

    <div class="exito-card">

        <c:choose>

            <%-- ================= FORMULARIO: ELEGIR CONTRASEÑA NUEVA ================= --%>
            <c:when test="${estado == 'formulario'}">
                <div class="exito-icono exito-icono--advertencia">
                    <svg viewBox="0 0 24 24" fill="none" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                        <rect x="3" y="11" width="18" height="10" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                    </svg>
                </div>
                <h1>Elige tu contraseña nueva</h1>
                <p class="exito-sub">
                    Debe tener entre 8 y 20 caracteres, con mayúscula, minúscula, número y carácter especial.
                </p>

                <form action="${ctx}/RecuperarPassword" method="POST" class="exito-form-reenviar">
                    <input type="hidden" name="accion" value="Restablecer">
                    <input type="hidden" name="token" value="${token}">

                    <label for="txtPassword">Contraseña nueva</label>
                    <div class="password-wrapper">
                        <input type="password" id="txtPassword" name="txtPassword"
                               placeholder="••••••••" maxlength="20" required>
                        <button type="button" class="toggle-password" data-target="txtPassword" aria-label="Mostrar contraseña">
                            <svg class="icon-eye" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                            <svg class="icon-eye-off" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.94 10.94 0 0 1 12 20c-7 0-11-8-11-8a20.6 20.6 0 0 1 5.06-6.06M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a20.6 20.6 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
                        </button>
                    </div>
                    <c:if test="${not empty errorPassword}">
                        <small class="msg error">${errorPassword}</small>
                    </c:if>

                    <label for="txtConfirmarPassword">Confirmar contraseña</label>
                    <div class="password-wrapper">
                        <input type="password" id="txtConfirmarPassword" name="txtConfirmarPassword"
                               placeholder="••••••••" maxlength="20" required>
                        <button type="button" class="toggle-password" data-target="txtConfirmarPassword" aria-label="Mostrar contraseña">
                            <svg class="icon-eye" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                            <svg class="icon-eye-off" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.94 10.94 0 0 1 12 20c-7 0-11-8-11-8a20.6 20.6 0 0 1 5.06-6.06M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a20.6 20.6 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
                        </button>
                    </div>
                    <c:if test="${not empty errorConfirmar}">
                        <small class="msg error">${errorConfirmar}</small>
                    </c:if>

                    <button type="submit" class="exito-btn exito-btn--primary">Restablecer contraseña</button>
                </form>
            </c:when>

            <%-- ================= ENLACE VENCIDO ================= --%>
            <c:when test="${estado == 'expirado'}">
                <div class="exito-icono exito-icono--advertencia">
                    <svg viewBox="0 0 24 24" fill="none" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="12" cy="12" r="10"/><path d="M12 8v4M12 16h.01"/>
                    </svg>
                </div>
                <h1>El enlace ya venció</h1>
                <p class="exito-sub">
                    Los enlaces para restablecer la contraseña duran 1 hora. Pide uno nuevo:
                </p>
                <div class="exito-acciones">
                    <a href="${ctx}/RecuperarPassword" class="exito-btn exito-btn--primary">Pedir un enlace nuevo</a>
                </div>
            </c:when>

            <%-- ================= TOKEN INVÁLIDO / DEFAULT ================= --%>
            <c:otherwise>
                <div class="exito-icono exito-icono--advertencia">
                    <svg viewBox="0 0 24 24" fill="none" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="12" cy="12" r="10"/><path d="M12 8v4M12 16h.01"/>
                    </svg>
                </div>
                <h1>Enlace no válido</h1>
                <p class="exito-sub">
                    <c:choose>
                        <c:when test="${not empty errorReenvio}">
                            <c:out value="${errorReenvio}" />
                        </c:when>
                        <c:otherwise>
                            Este enlace para restablecer tu contraseña no es correcto o ya fue usado antes.
                        </c:otherwise>
                    </c:choose>
                </p>
                <div class="exito-acciones">
                    <a href="${ctx}/RecuperarPassword" class="exito-btn exito-btn--primary">Pedir un enlace nuevo</a>
                    <a href="${ctx}/ServletLogin" class="exito-btn exito-btn--secundario">Ir al inicio de sesión</a>
                </div>
            </c:otherwise>

        </c:choose>

    </div>

<script src="${ctx}/js/password-toggle.js"></script>

</body>
</html>
