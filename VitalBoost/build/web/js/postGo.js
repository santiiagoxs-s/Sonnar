/**
 * postGo(url, extraParams)
 * ------------------------------------------------------------------
 * Envía una navegación como POST en vez de GET.
 *
 * Recibe una URL que puede traer querystring (por ejemplo:
 * "ctx/Servlet?accion=eliminar&id=5") y la transforma en un
 * formulario oculto que se envía por POST, para no exponer acciones,
 * identificadores ni datos sensibles como parámetros en la URL.
 *
 * Uso:
 *   postGo('${ctx}/ServletX?accion=eliminar&id=5');
 *   postGo('${ctx}/ServletX', { accion: 'eliminar', id: 5 });
 */
function postGo(url, extraParams) {
    var partes = String(url).split('?');
    var accion = partes[0];

    var form = document.createElement('form');
    form.method = 'POST';
    form.action = accion;
    form.style.display = 'none';

    function agregarCampo(nombre, valor) {
        var input = document.createElement('input');
        input.type = 'hidden';
        input.name = nombre;
        input.value = valor;
        form.appendChild(input);
    }

    if (partes.length > 1 && partes[1].length > 0) {
        var params = new URLSearchParams(partes[1]);
        params.forEach(function (valor, nombre) {
            agregarCampo(nombre, valor);
        });
    }

    if (extraParams) {
        Object.keys(extraParams).forEach(function (nombre) {
            agregarCampo(nombre, extraParams[nombre]);
        });
    }

    document.body.appendChild(form);
    form.submit();
}
