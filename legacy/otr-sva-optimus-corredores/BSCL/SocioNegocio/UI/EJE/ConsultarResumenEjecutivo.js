angular.module('ModuloSocioNegocio').controller('ConsultaResumenEjecutivoCtrl',
		function($scope,$translate, bcsComunes, $resource){
	
		$scope.data	= {};
		$scope.config = {};
		var idVentana = bcsComunes.ventanaWinId($scope);
		
		//Seteo de variables de sistema
		$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
		$scope.data.IdUsuario	= $scope.SesionIdUsuario;
		$scope.data.IdModulo	= 'PLA';
		
		$scope.init = function(){
			bcsComunes.ventanaOverlay(idVentana,true);
			//consultar usuario completo 384
			var urlConsultarResumenEjecutivo = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3018";
			var r = $resource("", {},
		    {
				'ConsultarResumenEjecutivo': { method: "POST", url: urlConsultarResumenEjecutivo}
			});
			
			var datosConsulta= {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: 'PLA'
			};
			
			var post = r.ConsultarResumenEjecutivo(datosConsulta);
			post.$promise.then(function(result){
				if(result.P_OUT_DATOS_EJECUTIVO!=null && result.P_OUT_DATOS_EJECUTIVO.length>0){
					
					$scope.data.ejecutivo = result.P_OUT_DATOS_EJECUTIVO[0];
					$scope.data.resumen = result.P_OUT_RESUMEN_EJECUTIVO[0];
					console.log($scope.data.ejecutivo);
					console.log($scope.data.resumen);
				}else{
					
					bcsComunes.ventanaMsg({
						mensaje: $translate.instant("UI_ERROR"),
						conflictos:util.parseServerResponse(result)
					},idVentana);
					bcsComunes.ventanaCerrar(idVentana);

				}
				bcsComunes.ventanaOverlay(idVentana,false);
				
			},function(error){
				bcsComunes.ventanaOverlay(idVentana,false);
				bcsComunes.ventanaCerrar(idVentana);
				bcsComunes.msg("error","Error","Ocurrio un error, intentelo nuevamente mas tarde");
			});			
		};
		
		$scope.abrirOrdenesVigentes = function () {
			bcsComunes.ventanaAbrir("ConsultarOrdenes",{ OrdenesVigentes : true },false,false,idVentana);
		}
		
		$scope.openDetalle = function(opcion){
			var id = bcsComunes.ventanaAbrir(opcion, {},true,false);			
		}
		
		$scope.init();
});