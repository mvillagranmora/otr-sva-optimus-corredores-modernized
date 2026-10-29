angular.module('ModuloOrdenes').controller('VencimientoModalCtrl',
		function ($scope,bcsServicioOrden,bcsComunes,$translate,$resource) {

		$scope.data = {};
	
		var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
		var parametros = bcsComunes.ventanaParams(idVentana);
		var vencimientoConfirmado = false;

	  	$scope.cerrar = function () {
			bcsComunes.ventanaData(idVentana,vencimientoConfirmado);
			bcsComunes.ventanaCerrar(idVentana);
		};

		$scope.confirmarOperacion = function()
	  	{
			var urlVencimiento = "/BSCL/Plataforma/GWJAVA/PLA/call?t=263";
			
			var r = $resource("", {},
		    {
				'operarData': { method: "POST", url: urlVencimiento}
			});
			
			var dataVencimiento = {
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdModulo: 'ORD',
				p_FechaVencimiento: $scope.data.fechaVencimiento.substring(0, 10)
			}
						
			var post = r.operarData(dataVencimiento);
			post.$promise.then(function(result){
				vencimientoConfirmado = true;
				var mensaje = $translate.instant("UI_ERROR_VENCER_ORDENES");
				if(result.P_RESULTADO_CIERRE_DIARIO && result.P_RESULTADO_CIERRE_DIARIO === "OK+"){
					mensaje = ($translate.instant("UI_ORDENES_VENCIDAS")).replace("XXX", result.P_CANTIDAD_ORDENES_VENCIDAS);
				}
				
				bcsComunes.ventanaMsg({
				       //mensaje:mensaje,
				conflictos:util.parseServerResponse(result)
				},idVentana);

				$scope.cerrar();
			}, function(e){//en caso de falla del servicio.
				if(e.data!=undefined){
					bcsComunes.msg("error","Error", ($translate.instant("UI_ERROR_CODIGO")).replace("XXX", e.data.CODIGO));
				}else{
					bcsComunes.msg("error","Error", e.status +" "+e.statusText);
				}
				$scope.cerrar();
			});
	  	};
	
		$scope.data = parametros;
	  	
		$scope.confirmarHabilitado = true;
});
