angular.module('ModuloOrdenes').controller('AperturaOrdenesCtrl',
		function($scope,$translate, bcsComunes, $resource){
	
		$scope.data	= {};
		$scope.data.estado = {};
		$scope.config = {};
		var idVentana = bcsComunes.ventanaWinId($scope);
		
		//Seteo de variables de sistema
		$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
		$scope.data.IdUsuario	= $scope.SesionIdUsuario;
		$scope.data.IdModulo	= 'ORD';
		
		$scope.data.estado.CambioFecha = "UI_NO_EJECUTADO";
		$scope.data.estado.CargarPalosAutomaticos = "UI_NO_EJECUTADO";
		
		$scope.apertura = function(){
			bcsComunes.ventanaOverlay(idVentana,true);
			//consultar usuario completo 384
			var urlAperturaOrdenes = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3051";
			var r = $resource("", {},
		    {
				'AperturaOrdenes': { method: "POST", url: urlAperturaOrdenes}
			});
			
			var datosApertura= {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: 'PLA'
			};
			
			var post = r.AperturaOrdenes(datosApertura);
			post.$promise.then(function(result){
				
				if(result.P_OUT_ERRORESCONTADOR==0 && result.P_OUT_ADVERTENCIASCONTADOR==0){
					$scope.data.estado.CargarPalosAutomaticos = $translate.instant(result.P_OUT_CARGATRANSACCIONES);
					$scope.data.estado.CambioFecha = $translate.instant(result.P_OUT_CAMBIOFECHA);
				}else{
					
					bcsComunes.ventanaMsg({
						mensaje: $translate.instant("UI_ERROR"),
						conflictos:util.parseServerResponse(result)
					},idVentana);

				}
				bcsComunes.ventanaOverlay(idVentana,false);
				
			},function(error){
				bcsComunes.ventanaOverlay(idVentana,false);
				bcsComunes.msg("error","Error","Ocurrio un error, intentelo nuevamente mas tarde");
			});			
		};
		
});