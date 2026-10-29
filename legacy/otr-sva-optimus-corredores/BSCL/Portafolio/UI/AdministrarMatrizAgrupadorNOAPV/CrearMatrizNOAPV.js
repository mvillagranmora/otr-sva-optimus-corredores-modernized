angular.module('ModuloPortafolio').controller('CrearMatrizNOAPVCtrl',
		function ($rootScope,$scope,$translate,$timeout,bcsComunes,
				OPCIONES_BCSALERTA,$resource,servicioPortafolio) {
	
	$scope.data = {};
	var idVentana = bcsComunes.ventanaWinId($scope);
	
	$scope.guardar = function(){
		bcsComunes.ventanaOverlay(idVentana,true);
		var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo  : 'SOC',
				p_SimularOperacion : 'N',
				p_UsaReglasNegocio : 'S',
				p_MatrizAgrupadorNOAPV: [{
					CodigoPortafolio: $scope.data.CodigoPortafolio,
					EsADC : $scope.data.EsADC ? 'S' : 'N',
					IdEstado: 'VIG'
				}]
		}
		servicioPortafolio.CrearMatrizAgrupadorNOAPV(datos).$promise.then(
				function(result) {
					bcsComunes.ventanaOverlay(idVentana,false);
					if(result ){
						var id = bcsComunes.ventanaMsg({ mensaje : $translate.instant(result.P_OUT_MENSAJE),
							conflictos : util.parseServerResponse(result)}, idVentana);
						if(result.P_OUT_ERRORESCONTADOR === 0) {
							if(id){
								var promesa=bcsComunes.ventanaPromesa(id);                               
							    $.when(promesa).then(function(data){
							    	 $timeout(function() {
							    		 bcsComunes.ventanaCerrar(idVentana);
							    	 },20);
							     }); 
							}else{
								 bcsComunes.ventanaCerrar(idVentana); 
							 }
						}
					}

				},
				function(result){
					bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.msg("error","Error",$translate.instant('UI_ERROR'));
				});
	}
	
	 //bcsComunes.ventanaTamanoAutomatico(idVentana,40);
	
})