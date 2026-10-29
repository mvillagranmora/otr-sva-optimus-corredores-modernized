angular.module('ModuloOrdenes').controller('VencimientoOrdenesCtrl',
		function($scope,$translate, bcsComunes, $resource){
	
		$scope.data	= {};
		$scope.data.estado = {};
		$scope.config = {};
		var idVentana = bcsComunes.ventanaWinId($scope);
		
		//Seteo de variables de sistema
		$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
		$scope.data.IdUsuario	= $scope.SesionIdUsuario;
		$scope.data.IdModulo	= 'ORD';
		vencimientoConfirmado = false;
		$scope.data.estado.CambioFecha = "UI_NO_EJECUTADO";
		$scope.data.estado.CargarPalosAutomaticos = "UI_NO_EJECUTADO";
		$scope.data.kngFechaVencimientoOrdenes = util.dateInicioDia($scope.SesionFechaSistema);

	  	$scope.cerrar = function () {
			bcsComunes.ventanaData(idVentana,vencimientoConfirmado);
			bcsComunes.ventanaCerrar(idVentana);
		};
		
		$scope.VencimientoOrdenes = function(){
			bcsComunes.ventanaOverlay(idVentana,true);
			//consultar usuario completo 384
			var urlVencimiento = "/BSCL/Plataforma/GWJAVA/PLA/call?t=263";
			
			var r = $resource("", {},
		    {
				'operarData': { method: "POST", url: urlVencimiento}
			});
			
			var dataVencimiento= {
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdModulo: 'ORD',
				p_FechaVencimiento: util.dateSPdate($scope.data.kngFechaVencimientoOrdenes)
				};
			
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
				vencimientoConfirmado = false;
				$scope.cerrar();
			});
	  	};
		
});