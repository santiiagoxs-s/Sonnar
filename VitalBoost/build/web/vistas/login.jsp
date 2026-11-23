<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Iniciar Sesión - VitalBoost</title>

    <link rel="stylesheet" href="${ctx}/css/login.css">
    <link rel="stylesheet" href="${ctx}/css/global.css">
</head>

<body>

<%@ include file="/includes/loader.jspf" %>

<main class="main-container">

    <section class="left-panel">

        <a href="${ctx}/index.jsp" class="left-logo">
            <div class="logo-icono">
                <svg viewBox="0 0 24 24" fill="none" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.8 6.6c-1.8-3.4-6.2-4.2-8.8-1.6l-.9.9-.9-.9C7.6 2.4 3.2 3.2 1.4 6.6c-1.5 2.9-.7 6.4 1.9 9l7.7 7.6 7.7-7.6c2.6-2.6 3.4-6.1 1.9-9z"/><path d="M6 12h3l1.5-3 2 6 1.5-3H18"/></svg>
            </div>
            <span>Vital<b>Boost</b></span>
        </a>

        <div class="left-content">

            <div class="left-badge">
                <span class="punto"></span>
                Disponible las 24 horas, todos los días
            </div>

            <h1>Bienvenido de nuevo a <span>tu historial médico</span></h1>

            <p>Inicia sesión para consultar y gestionar tu información médica de forma rápida, segura y confiable.</p>

            <div class="left-features">

                <div class="left-feature">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="10" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                    <span>Datos cifrados y protegidos</span>
                </div>

                <div class="left-feature">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/><path d="M9 12l2 2 4-4"/></svg>
                    <span>Acceso solo a usuarios autorizados</span>
                </div>

                <div class="left-feature">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                    <span>Disponible las 24 horas</span>
                </div>

            </div>

        </div>

    </section>

    <section class="right-panel">

        <div class="login-card">

            <a href="${ctx}/index.jsp" class="back-link back-link-top">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
                Volver al inicio
            </a>

            <div class="login-header">
                <h2>Iniciar Sesión</h2>
                <p>Ingresa tus credenciales para continuar</p>
            </div>

            <c:if test="${param.restablecida == '1'}">
                <div class="msg error" style="display:block; margin-bottom:16px; padding:12px 14px; border-radius:10px; background:#ecfdf5; border:1px solid #bbf1db; color:#0a6b4b;">
                    Tu contraseña se actualizó correctamente. Ya puedes iniciar sesión.
                </div>
            </c:if>

            <c:if test="${not empty correoNoVerificado}">
                <div class="msg error" style="display:block; margin-bottom:16px; padding:12px 14px; border-radius:10px; background:#fff7ed; border:1px solid #fde3c7;">
                    Aún no verificas tu correo.
                    <form action="${ctx}/VerificarEmail" method="POST" style="display:inline;">
                        <input type="hidden" name="accion" value="Reenviar">
                        <input type="hidden" name="txtEmail" value="${correoNoVerificado}">
                        <button type="submit" style="background:none; border:none; padding:0; color:var(--vb-azul-600, #1d4ed8); text-decoration:underline; cursor:pointer; font:inherit;">
                            Reenviar verificación
                        </button>
                    </form>
                </div>
            </c:if>

            <form action="${ctx}/ServletLogin" method="POST">

                <div class="form-group">
                    <label for="txtEmail">Correo Electrónico</label>

                    <input type="email"
                           id="txtEmail"
                           name="txtEmail"
                           placeholder="nombre@ejemplo.com"
                           maxlength="100"
                           value="${txtEmailOld}">

                    <c:if test="${not empty errorEmail}">
                        <small class="msg error">${errorEmail}</small>
                    </c:if>
                </div>

                <div class="form-group">
                    <label for="txtPassword">Contraseña</label>

                    <div class="password-wrapper">
                        <input type="password"
                               id="txtPassword"
                               name="txtPassword"
                               placeholder="••••••••"
                               maxlength="100">

                        <button type="button" class="toggle-password" data-target="txtPassword" aria-label="Mostrar contraseña">
                            <svg class="icon-eye" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                            <svg class="icon-eye-off" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.94 10.94 0 0 1 12 20c-7 0-11-8-11-8a20.6 20.6 0 0 1 5.06-6.06M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a20.6 20.6 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
                        </button>
                    </div>

                    <c:if test="${not empty errorPassword}">
                        <small class="msg error">${errorPassword}</small>
                    </c:if>

                    <a href="${ctx}/RecuperarPassword" style="display:inline-block; margin-top:8px; font-size:13px; font-weight:700; color:var(--ib-azul-600, #1d4ed8); text-decoration:none;">
                        ¿Olvidaste tu contraseña?
                    </a>
                </div>

                <div class="form-group">
                    <label for="txtRol">Ingresar como</label>

                    <select id="txtRol" name="txtRol">
                        <option value="">Seleccione un rol</option>
                        <c:forEach var="rol" items="${listaRoles}">
                            <option value="${rol.id_rol}">${rol.nombre_rol}</option>
                        </c:forEach>
                    </select>

                    <c:if test="${not empty errorRol}">
                        <small class="msg error">${errorRol}</small>
                    </c:if>
                </div>

                <div class="form-group form-check">
                    <label class="check-label">
                        <input type="checkbox"
                               id="chkTerminos"
                               name="chkTerminos"
                               value="1"
                               ${not empty chkTerminosOld ? 'checked' : ''}>
                        Acepto los
                        <a href="javascript:void(0);" onclick="abrirTerminos(event)">términos y condiciones</a>
                        de VitalBoost
                    </label>

                    <c:if test="${not empty errorTerminos}">
                        <small class="msg error">${errorTerminos}</small>
                    </c:if>
                </div>

                <button type="submit" name="accion" value="Ingresar" class="btn-ingresar">
                    Iniciar Sesión
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/><polyline points="10 17 15 12 10 7"/><line x1="15" y1="12" x2="3" y2="12"/></svg>
                </button>

            </form>

            <div class="footer-link" style="margin-top: 20px;">
                <p>¿Eres paciente?</p>
                <a href="${ctx}/ServletRegistro">Regístrate y obtén tu código QR de emergencia</a>
            </div>

        </div>

    </section>

</main>

<!-- =====================================
     MODAL TÉRMINOS Y CONDICIONES
====================================== -->

<div id="modalTerminos" class="modal-terminos-overlay" style="display:none;" onclick="cerrarTerminosFuera(event)">

    <div class="modal-terminos-caja">

        <div class="modal-terminos-header">
            <div class="modal-terminos-icono">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="9" y1="13" x2="15" y2="13"/><line x1="9" y1="17" x2="15" y2="17"/></svg>
            </div>
            <div>
                <h2>Términos y Condiciones</h2>
                <span>VitalBoost · Última actualización 2026</span>
            </div>
            <button type="button" class="modal-terminos-cerrar" onclick="cerrarTerminos()" aria-label="Cerrar">&times;</button>
        </div>

        <div class="modal-terminos-cuerpo">

            <p>
                Al registrarte o iniciar sesión en VitalBoost aceptas los siguientes
                términos, los cuales rigen el uso de la plataforma y el tratamiento
                de tu información médica.
            </p>

            <div class="modal-terminos-item">
                <h3><span class="modal-terminos-num">1</span>Naturaleza del servicio</h3>
                <p>
                    VitalBoost es una plataforma que centraliza información médica básica
                    (tipo de sangre, alergias y datos de contacto de emergencia) para
                    facilitar la atención en situaciones críticas. No reemplaza el
                    criterio ni el diagnóstico de un profesional de la salud.
                </p>
            </div>

            <div class="modal-terminos-item">
                <h3><span class="modal-terminos-num">2</span>Tratamiento de datos personales</h3>
                <p>
                    Tus datos médicos son sensibles y se almacenan de forma segura. Solo
                    se muestran a personal médico autorizado o a través de tu código QR
                    de emergencia, con el fin exclusivo de brindarte atención oportuna.
                </p>
            </div>

            <div class="modal-terminos-item">
                <h3><span class="modal-terminos-num">3</span>Responsabilidad del usuario</h3>
                <p>
                    Eres responsable de mantener actualizada tu información médica.
                    VitalBoost no se hace responsable por decisiones clínicas tomadas
                    a partir de datos desactualizados o incompletos.
                </p>
            </div>

            <div class="modal-terminos-item">
                <h3><span class="modal-terminos-num">4</span>Acceso de emergencia</h3>
                <p>
                    La información básica de tu hoja de vida médica puede ser consultada
                    sin necesidad de contraseña mediante tu código QR, pensado para
                    situaciones donde no puedas comunicarte por ti mismo.
                </p>
            </div>

            <div class="modal-terminos-item">
                <h3><span class="modal-terminos-num">5</span>Aceptación</h3>
                <p>
                    El uso continuo de la plataforma implica la aceptación de estos
                    términos y de las actualizaciones que se realicen sobre ellos.
                </p>
            </div>

        </div>

        <div class="modal-terminos-footer">
            <button type="button" class="btn-terminos-cerrar" onclick="cerrarTerminos()">Cerrar</button>
            <button type="button" class="btn-terminos-aceptar" onclick="aceptarTerminos()">
                Aceptar términos
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
            </button>
        </div>

    </div>

</div>

<script src="${ctx}/js/password-toggle.js"></script>

<script>
    function abrirTerminos(e) {
        if (e) e.preventDefault();
        document.getElementById('modalTerminos').style.display = 'flex';
        document.body.style.overflow = 'hidden';
    }

    function cerrarTerminos() {
        document.getElementById('modalTerminos').style.display = 'none';
        document.body.style.overflow = '';
    }

    function cerrarTerminosFuera(e) {
        if (e.target.id === 'modalTerminos') {
            cerrarTerminos();
        }
    }

    function aceptarTerminos() {
        document.getElementById('chkTerminos').checked = true;
        cerrarTerminos();
    }
</script>

</body>
</html>
