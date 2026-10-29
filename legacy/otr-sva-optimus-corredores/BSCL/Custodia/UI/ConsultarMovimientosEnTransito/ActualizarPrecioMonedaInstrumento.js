(function() {
	angular.module('ModuloCustodia').controller('ActualizarPrecioMonedaIntrumentoCtrl',
		function($scope, $translate, bcsComunes, bcsServiciosMovTransito) {

			/*********************************************
			 * VARIABLES Y CONTEXTOS
			 ********************************************/

			var pMoneda = this;
			pMoneda.idVentana = bcsComunes.ventanaWinId($scope);
			pMoneda.parametros = bcsComunes.ventanaParams(pMoneda.idVentana);
			pMoneda.domVentana = $("[ventanaid='" + pMoneda.idVentana + "']");

			var movimientos = _.clone(pMoneda.parametros.Movimientos);

			pMoneda.win = {};
			pMoneda.fn = {};
			pMoneda.datos = {};


			/*********************************************
			 * ACCIONES DE PANTALLA
			 ********************************************/

			if (movimientos) {

				bcsComunes.ventanaTitulo(pMoneda.idVentana, $translate.instant('UI_ACTUALIZAR_PRECIO_MONEDA'));
				pMoneda.datos.tipoComprobante = movimientos.IdTipoComprobante || '';
				pMoneda.datos.numeroDocumento = movimientos.FolioComprobante || '';
				pMoneda.datos.nemotecnico = movimientos.Nemotecnico || '';
				pMoneda.datos.precio = movimientos.PrecioCompra || '';
				pMoneda.datos.cantidad = movimientos.Cantidad || '';

				$scope.$watch('pMoneda.datos.precio', function() {

					pMoneda.datos.monto = pMoneda.datos.precio * pMoneda.datos.cantidad;
				})

			}


			/*********************************************
			 * FUNCIONES
			 *********************************************/

			pMoneda.fn.cancelar = function() {
				bcsComunes.ventanaData(pMoneda.idVentana, false);
				bcsComunes.ventanaCerrar(pMoneda.idVentana);
			};


			pMoneda.fn.aceptar = function() {

				    var actualizarDatos = {
				p_IdEmpresa : movimientos.IdEmpresa,
				p_IdUsuario : movimientos.IdUsuario,
				p_IdModulo : movimientos.IdModulo,
				p_MovimientoCarteraEnTransito: [{
				    		CampoRegistro : "Precio",
				            PrecioCompraAntiguo : movimientos.PrecioCompra,
				            PrecioCompraNuevo : pMoneda.datos.precio,
				            Cantidad : movimientos.Cantidad,
				            IdMovimientoCarteraEnTransito :  movimientos.IdMovimientoCarteraEnTransito,
							IdMonedaInstrumento: movimientos.IdMonedaInstrumento
				}]

    };	
				
				bcsComunes.ventanaOverlay(pMoneda.idVentana, true);

				bcsServiciosMovTransito.ActualizarPrecioMonedaInstrumento
				(actualizarDatos,
				function(respuesta){			
					bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(respuesta)
					},$scope.pMoneda.idVentana);
					bcsComunes.ventanaOverlay(pMoneda.idVentana,false);
					bcsComunes.ventanaCerrar(pMoneda.idVentana);
					
				},
				function(respuesta){
					bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(respuesta)
					},$scope.pMoneda.idVentana);
					bcsComunes.ventanaOverlay(pMoneda.idVentana,false);
				});

}
												
	});
}());



