angular.module('ModuloOrdenes').controller('OficializacionModalCtrl',
		function ($scope,bcsServicioOrden,bcsComunes,$translate,$resource) {

		$scope.data = {};
	
		var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
		var parametros = bcsComunes.ventanaParams(idVentana);
		var oficializacionConfirmado = false;

	  	$scope.cerrar = function () {
			bcsComunes.ventanaData(idVentana,oficializacionConfirmado);
			bcsComunes.ventanaCerrar(idVentana);
		};

		$scope.confirmarOperacion = function()
	  	{
			var urlVencimiento = "/BSCL/Plataforma/GWJAVA/PLA/call?t=276";
			
			var r = $resource("", {},
		    {
				'operarData': { method: "POST", url: urlVencimiento}
			});
			
			var dataVencimiento = {
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdModulo: 'ORD',
				p_FechaVencimiento: $scope.data.fechaVencimiento
			}
						
			var post = r.operarData(dataVencimiento);
			post.$promise.then(function(result){
				oficializacionConfirmado = true;
				var mensaje = $translate.instant("UI_ERROR_OFICIALIZAR_ORDENES");
				if(result.P_ESTADO_OFICIALIZACION && result.P_ESTADO_OFICIALIZACION === "OK+++"){
					mensaje = ($translate.instant("UI_ORDENES_OFICIALIZADAS")).replace("XXX", result.P_ORDENES_OFICIALIZADAS);
				}
				
				bcsComunes.ventanaMsg({
				       mensaje: mensaje,
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
