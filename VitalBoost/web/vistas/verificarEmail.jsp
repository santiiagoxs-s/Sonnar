<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Verificación de correo - VitalBoost</title>
    <link rel="stylesheet" href="${ctx}/css/registroExitoso.css">
</head>

<body>

<%@ include file="/includes/loader.jspf" %>

    <div class="exito-blob exito-blob--1"></div>
    <div class="exito-blob exito-blob--2"></div>
    <div class="exito-blob exito-blob--3"></div>

    <div class="exito-card">

        <c:choose>

            <%-- ================= ÉXITO ================= --%>
            <c:when test="${estado == 'exito'}">
                <div class="exito-icono">
                    <svg viewBox="0 0 24 24" fill="none" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M20 6 9 17l-5-5"/>
                    </svg>
                </div>
                <h1>¡Cuenta creada!</h1>
                <p class="exito-sub">
                    Gracias, <strong><c:out value="${nombrePaciente}" /></strong>. Tu cuenta de VitalBoost
                    ya quedó confirmada.
                    <c:if test="${not empty qrImageUrl}"> Este es tu código QR de emergencia.</c:if>
                </p>

                <c:if test="${not empty qrImageUrl}">
                    <div class="exito-nota">
                        <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <circle cx="12" cy="12" r="10"/><path d="M12 16v-4M12 8h.01"/>
                        </svg>
                        <span>
                            Guarda o imprime este código: no necesitas iniciar sesión para usarlo.
                            Cualquier personal médico podrá escanearlo en caso de emergencia
                            para ver tu hoja de vida (tipo de sangre, EPS, alergias y medicamentos).
                        </span>
                    </div>

                    <div class="exito-qr-box">
                        <img src="${qrImageUrl}" alt="Código QR de emergencia">
                    </div>

                    <p class="exito-url">
                        <c:out value="${urlHojaVida}" />
                    </p>
                </c:if>

                <div class="exito-acciones">
                    <c:if test="${not empty urlHojaVida}">
                        <a href="${urlHojaVida}" target="_blank" class="exito-btn exito-btn--primary">
                            Ver mi Hoja de Vida
                        </a>
                    </c:if>
                    <a href="${ctx}/ServletLogin" class="exito-btn exito-btn--secundario">Iniciar sesión</a>
                </div>
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
                    Los enlaces de verificación duran 24 horas. Pide uno nuevo:
                </p>

                <form action="${ctx}/VerificarEmail" method="POST" class="exito-form-reenviar">
                    <input type="hidden" name="accion" value="Reenviar">
                    <input type="email" name="txtEmail" placeholder="Tu correo electrónico"
                           value="${emailExpirado}" maxlength="100" required>
                    <button type="submit" class="exito-btn exito-btn--primary">Reenviar verificación</button>
                </form>
            </c:when>

            <%-- ================= REENVÍO OK ================= --%>
            <c:when test="${estado == 'reenvioExito'}">
                <div class="exito-icono">
                    <svg viewBox="0 0 24 24" fill="none" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M20 6 9 17l-5-5"/>
                    </svg>
                </div>
                <h1>¡Correo reenviado!</h1>
                <p class="exito-sub">
                    Revisa tu bandeja de entrada (y la carpeta de spam) para confirmar tu cuenta.
                </p>
                <div class="exito-acciones">
                    <a href="${ctx}/ServletLogin" class="exito-btn exito-btn--primary">Volver al inicio de sesión</a>
                </div>
            </c:when>

            <%-- ================= ERROR AL REENVIAR ================= --%>
            <c:when test="${estado == 'reenvioError'}">
                <div class="exito-icono exito-icono--advertencia">
                    <svg viewBox="0 0 24 24" fill="none" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="12" cy="12" r="10"/><path d="M12 8v4M12 16h.01"/>
                    </svg>
                </div>
                <h1>No se pudo reenviar</h1>
                <p class="exito-sub"><c:out value="${errorReenvio}" /></p>

                <form action="${ctx}/VerificarEmail" method="POST" class="exito-form-reenviar">
                    <input type="hidden" name="accion" value="Reenviar">
                    <input type="email" name="txtEmail" placeholder="Tu correo electrónico" maxlength="100" required>
                    <button type="submit" class="exito-btn exito-btn--primary">Intentar de nuevo</button>
                </form>
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
                            Este enlace de verificación no es correcto o ya fue usado antes.
                        </c:otherwise>
                    </c:choose>
                </p>
                <div class="exito-acciones">
                    <a href="${ctx}/ServletLogin" class="exito-btn exito-btn--secundario">Ir al inicio de sesión</a>
                </div>
            </c:otherwise>

        </c:choose>

    </div>

</body>
</html>
