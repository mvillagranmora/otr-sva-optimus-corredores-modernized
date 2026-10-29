/***********************************************
 * Purpose	: Modificar Orden Renta Fija (PERU)
 * Author	: Jgarces
 * Date		: 27/08/2017
 ***********************************************/
(function(){
	angular.module('ModuloOrdenes').controller('ModificarOrdenRFCtrl',
	function($scope,bcsComunes,bcsAlerta,bcsServicioOrden,$translate,OPCIONES_BCSALERTA)
	{
		/*********************
		 * DEFINICIONES
		 *********************/
		var idVentana = bcsComunes.ventanaWinId($scope);
		var parametros = bcsComunes.ventanaParams(idVentana);
		$scope.IdOrden = parametros.orden.IdOrden;
		$scope.cantidad = parametros.orden.CantidadOrden;
		$scope.idSistemmaBursatil = parametros.orden.IdSistemmaBursatil;

		/*********************
		 * FILTROS
		 *********************/
		$scope.filtroSistemaBursatil = {
			IdEmpresa:{"field":"IdEmpresa","operator":"eq","value":$scope.$root.SesionIdEmpresa},
			IdClasificacionTipoInstrumento:{"field":"IdClasificacionTipoInstrumento","operator":"eq","value":'RF'},
		}; 
		$scope.idSistemaBursatilDef = $scope.idSistemaBursatil;
		/*********************
		 * FUNCIONES
		 *********************/
		$scope.aceptar = function() {
			var modificaCantidad = ($scope.cantidad === parametros.orden.CantidadOrden)?"N":"S";
			var modificaSistemBursatil = ($scope.idSistemaBursatil === parametros.orden.IdSistemaBursatil)?"N":"S";
			if (modificaCantidad==="N"&&modificaSistemBursatil==="N") {
				bcsComunes.msg("warning","UI_ADVERTENCIA","UI_DATOS_NO_MODIFICADOS");
				bcsComunes.ventanaData(idVentana,false);
				bcsComunes.ventanaCerrar(idVentana);
				return;
			}
			if($scope.cantidad > parametros.orden.CantidadOrden){
				bcsComunes.msg("warning","UI_ADVERTENCIA","UI_CANTIDAD_NO_PUEDE_SER_MAYOR");
				bcsComunes.ventanaData(idVentana,false);
				bcsComunes.ventanaCerrar(idVentana);
				return;
			}
			var t1 = bcsAlerta.crear('PREGUNTA',$translate.instant('UI_ADVERTENCIA_MODIFICAR_ORDEN'),idVentana);
			t1.then(function(respuesta){
				if (respuesta===OPCIONES_BCSALERTA.ACEPTAR)  {
					var orden = {
						IdOrden						: $scope.IdOrden,
						ModificaCantidad			: modificaCantidad,
						Cantidad					: $scope.cantidad,
						modificaSistemBursatil		: modificaSistemBursatil,
						IdSistemBursatil			: $scope.idSistemaBursatil,
						ModificaCobros				: "N",
						IdAsociacion				: 0,
						TasaPromedioReporte 		: 0,
						ModificaTasaPromedioReporte : "N"	
					};
					var datos = {
						p_IdEmpresa 		: $scope.SesionIdEmpresa,
						p_IdUsuario			: $scope.SesionIdUsuario,
						p_IdModulo			: "ORD",
						p_Ordenes			: [orden],
						p_CobrosModificados	: []
					};
					//console.log(datos);return;
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
		};
		
		$scope.cancelar = function() {
			bcsComunes.ventanaData(idVentana,undefined);
			bcsComunes.ventanaCerrar(idVentana);
		};
	
	});
}());
//------------------------- FIN CONTROLLER --------------------------	
	
