angular.module('ModuloPortafolio').controller('ModificarMatrizNOAPVCtrl',
		function ($rootScope,$scope,$translate,$timeout,bcsComunes,
				OPCIONES_BCSALERTA,$resource,servicioPortafolio) {
	
	$scope.data = {};
	var idVentana = bcsComunes.ventanaWinId($scope);
	$scope.parametros = bcsComunes.ventanaParams(idVentana);
	$scope.data.IdMatrizAgrupadorNOAPV = $scope.parametros.Matriz.IdMatrizAgrupadorNOAPV;
	$scope.data.EsADC = $scope.parametros.Matriz.EsADC === 'S' ? true : false;
	$scope.data.CodigoPortafolio = $scope.parametros.Matriz.CodigoPortafolio;
	
	$scope.guardar = function(){
		bcsComunes.ventanaOverlay(idVentana,true);
		var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo  : 'SOC',
				p_SimularOperacion : 'N',
				p_UsaReglasNegocio : 'S',
				p_MatrizAgrupadorNOAPV: [{
					IdMatrizAgrupadorNOAPV: $scope.data.IdMatrizAgrupadorNOAPV,
					EsADC : $scope.data.EsADC ? 'S' : 'N'
				}]
		}
		servicioPortafolio.ModificarMatrizAgrupadorNOAPV(datos).$promise.then(
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
	
	bcsComunes.ventanaAutoHeight(idVentana);
	
})