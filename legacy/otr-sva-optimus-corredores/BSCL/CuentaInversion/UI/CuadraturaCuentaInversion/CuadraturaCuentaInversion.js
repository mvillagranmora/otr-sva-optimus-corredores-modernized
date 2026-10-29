(function() {
	angular.module('ModuloCuentaInversion').controller('CuadraturaCuentaInversionCtrl',
	function($scope,$translate,$resource,bcsComunes,$http,$timeout){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		
		var idVentana = bcsComunes.ventanaWinId($scope);
		$scope.filtro = {};
		$scope.fn = {};
		
		$scope.filtro.FechaProceso = util.dateDS($scope.$root.SesionFechaSistema,"FechaSistema","CALL");
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		$scope.fn.ejecutar = function(){
			if(!$scope.filtro.FechaProceso) return;
			
			var parametrosSP = {};
			parametrosSP.p_IdEmpresa		= $scope.$root.SesionIdEmpresa;
			parametrosSP.p_IdUsuario		= $scope.$root.SesionIdUsuario;
	        parametrosSP.p_IdModulo			= 'CTA';
	        parametrosSP.p_UsaReglasNegocio	= 'N';
	        parametrosSP.p_FechaProceso		= util.dateSPdate($scope.filtro.FechaProceso);
	        
        	bcsComunes.ventanaOverlay(idVentana,true);
        	$scope.fn.call("1394", parametrosSP);
		};
		
		$scope.fn.call = function(callTransaction,parameters) {
			var resultSet = [];
			var urlFiltros = "/BSCL/Plataforma/GWJAVA/PLA/call?t="+callTransaction;
			var r = $resource("", {},{
				'Filtros': { method: "POST", url: urlFiltros, async: false,}
			});
			var post = r.Filtros(parameters);
			post.$promise.then($scope.fn.success_fn);
		};
		
		$scope.fn.success_fn = function(respuesta){
			if(respuesta.P_OUT_ERRORESCONTADOR === 0) {
				bcsComunes.msg('info',$translate.instant("UI_INFORMACION"),$translate.instant("UI_PROCESO_EJECUTADO_EXITO"));
				bcsComunes.ventanaOverlay(idVentana,false);
			}
			else{
				bcsComunes.msg('error',$translate.instant("UI_ERROR"),respuesta.P_OUT_ERRORESDETALLE[0].Codigo);
			}
		};

		var firstHeight = $('#Win_CuadraturaCuentaInversion');
		firstHeight.css("height", "145px");
		
	});
}());