// Dibuja los bolsines en el mapa (Leaflet + OpenStreetMap) a partir de las filas de la grilla.
document.addEventListener('DOMContentLoaded', function () {
    var contenedor = document.getElementById('mapa');
    if (!contenedor || typeof L === 'undefined') {
        return;
    }
    var datos = contenedor.dataset;
    var mapa = L.map(contenedor).setView([Number(datos.lat), Number(datos.lon)], Number(datos.zoom));
    L.tileLayer(datos.url, {maxZoom: 18, attribution: datos.atribucion}).addTo(mapa);

    var seleccionHabilitada = datos.seleccion === 'true';
    var formSeleccion = document.getElementById('form-seleccion');
    var marcadores = [];

    document.querySelectorAll('tr.fila-bolsin').forEach(function (fila) {
        var b = fila.dataset;
        var marcador = L.marker([Number(b.lat), Number(b.lon)], {title: 'Bolsín ' + b.nro}).addTo(mapa);

        var popup = document.createElement('div');
        popup.className = 'popup-bolsin';
        var titulo = document.createElement('strong');
        titulo.textContent = 'Bolsín N° ' + b.nro;
        popup.appendChild(titulo);
        [['Precinto', b.precinto], ['CM destino', b.cm], ['Actualizado', b.fecha]].forEach(function (par) {
            var linea = document.createElement('div');
            linea.textContent = par[0] + ': ' + par[1];
            popup.appendChild(linea);
        });
        if (seleccionHabilitada) {
            var boton = document.createElement('button');
            boton.type = 'button';
            boton.className = 'btn chico';
            boton.textContent = 'Seleccionar este bolsín';
            boton.addEventListener('click', function () {
                formSeleccion.querySelector('input[name="numeroBolsin"]').value = b.nro;
                formSeleccion.submit();
            });
            popup.appendChild(boton);
        }
        marcador.bindPopup(popup);
        marcadores.push(marcador);

        fila.addEventListener('mouseenter', function () {
            marcador.openPopup();
        });
        if (datos.seleccionado && datos.seleccionado === b.nro) {
            marcador.openPopup();
        }
    });

    if (marcadores.length > 1) {
        mapa.fitBounds(L.featureGroup(marcadores).getBounds().pad(0.4));
    }
});
