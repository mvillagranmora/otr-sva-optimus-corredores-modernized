angular.module('ModuloOrdenes').controller('ModificarOrdenRVCtrl',function($scope,bcsComunes,bcsAlerta,bcsServicioOrden,$translate,OPCIONES_BCSALERTA )
{
	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	$scope.IdOrden = parametros.orden.IdOrden;
	$scope.cantidad = parametros.orden.CantidadOrden;
	$scope.IdMonedaLiquidacionOrden = parametros.orden.IdMonedaLiquidacionOrden;
	$scope.aceptar = function() {
		
		if ($scope.cantidad === parametros.orden.CantidadOrden) {
			bcsComunes.msg("warning","UI_ADVERTENCIA","UI_CANTIDAD_NO_MODIFICADA");
			bcsComunes.ventanaData(idVentana,false);
			bcsComunes.ventanaCerrar(idVentana);
			return;
		}

		var t1 = bcsAlerta.crear('PREGUNTA',$translate.instant('UI_ADVERTENCIA_MODIFICAR_ORDEN'),idVentana);
		t1.then(function(respuesta){
			if (respuesta===OPCIONES_BCSALERTA.ACEPTAR)  {
				
				var orden = {
						IdOrden				: $scope.IdOrden,
						ModificaCantidad	: "S",
						Cantidad			: $scope.cantidad,
						ModificaCobros		: "N",
						IdAsociacion		: 0,
						TasaPromedioReporte : 0,
						ModificaTasaPromedioReporte : "N",
						IdMonedaLiquidacionOrden: $scope.IdMonedaLiquidacionOrden	
				};
				
				var datos = {
						p_IdEmpresa 		: $scope.SesionIdEmpresa,
						p_IdUsuario			: $scope.SesionIdUsuario,
						p_IdModulo			: "ORD",
						p_Ordenes			: [orden],
						p_CobrosModificados	: []
				};
				
				bcsComunes.ventanaOverlay(idVentana,true);
				bcsServicioOrden.ModificarOrdenes(datos).$promise.then(function(result){
					bcsComunes.ventanaOverlay(idVentana,false);
					var p1 = bcsComunes.ventanaMsg({
						conflictos : util.parseServerResponse(result)
					});
					bcsComunes.ventanaPromesa(p1).then(function(r){
						if (result.P_OUT_ERRORESCONTADOR === 0) {
							bcsComunes.ventanaData(idVentana,true);
							bcsComunes.ventanaCerrar(idVentana);
						}
						
					})
					

				},function(result){
					bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.ventanaMsg({
						conflictos : util.parseServerResponse(result)
					});
				})
				
				
				
			}
		})
		
		
		
	}
	
	$scope.cancelar = function() {
		bcsComunes.ventanaData(idVentana,undefined);
		bcsComunes.ventanaCerrar(idVentana);
	}

});


