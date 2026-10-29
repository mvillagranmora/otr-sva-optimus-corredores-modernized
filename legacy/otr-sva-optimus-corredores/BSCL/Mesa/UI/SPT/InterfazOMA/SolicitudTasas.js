(function() {
	angular.module('ModuloPlataforma').controller('InterfazOMACtrl',
	function($scope,$translate,bcsComunes,bcsServiciosTasas,$http,$timeout){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var sTas 		= this;
		sTas.idVentana	= bcsComunes.ventanaWinId($scope);
		sTas.cmbbx		= {};
		sTas.filtro		= {};
		sTas.fn			= {};
		
		sTas.filtro.Fecha = util.dateDS($scope.$root.SesionFechaSistema,"FechaSistema","CALL");
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		sTas.cmbbx.Fuente={
			filtros: {
				IdEmpresa	: {field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdEstado	: {field: "IdEstado",	operator: "eq", value: 'ACT'}
			}
		};
		
		
		/*********************************************
		 * GRILLAS
		 *********************************************/
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		
		sTas.fn.generarArchivo = function(){
			var parametrosSP = {};
			parametrosSP.p_IdEmpresa		= $scope.$root.SesionIdEmpresa;
			parametrosSP.p_IdUsuario		= $scope.$root.SesionIdUsuario;
	        parametrosSP.p_IdModulo			= 'PLA';
	        parametrosSP.p_UsaReglasNegocio	= 'N';
	        
	        parametrosSP.p_Fecha		= util.dateSPdate(sTas.filtro.Fecha);
	        parametrosSP.p_CodigoFuente	= sTas.filtro.Codigo;
	        
	        if(!parametrosSP.p_Fecha || !parametrosSP.p_CodigoFuente) return;
	        
        	bcsComunes.ventanaOverlay(sTas.idVentana,true);
        	bcsServiciosTasas.generarArchivoTasas(parametrosSP).$promise.then(function(result) {
        		if (result.P_OUT_ADVERTENCIASCONTADOR){
        			bcsComunes.msg("warning",$translate.instant("UI_SOLICITUD_TASAS"),result.P_OUT_ADVERTENCIASDETALLE[0].Codigo);
        			bcsComunes.ventanaOverlay(sTas.idVentana,false);
        		}
        		else if (result.P_OUT_ERRORESCONTADOR){
        			bcsComunes.msg("error",$translate.instant("UI_SOLICITUD_TASAS"),result.P_OUT_ERRORESDETALLE[0].Codigo);
        			bcsComunes.ventanaOverlay(sTas.idVentana,false);
        		}
        		else{
        			var ruta = result.P_OUT_RESULTADOPROCESO[6].Valor;
        			var nombre = result.P_OUT_RESULTADOPROCESO[4].Valor;
        			
        			$timeout(function(){
        				var url = "/BSCL/Plataforma/GWJAVA/PLA/download?";
        			    var data = { file: ruta + nombre };
        			    $http({
        			    	method: 'POST',
        			    	url: url,
        			    	params: data
        			    }).success(function(data){
        			    	var file = new File([data], nombre);
        			    	saveAs(file);
        			    	bcsComunes.msg("info",$translate.instant("UI_SOLICITUD_TASAS"),$translate.instant("UI_ARCHIVO_TASAS_OK"));
        			    	bcsComunes.ventanaOverlay(sTas.idVentana,false);
        			    }).error(function (err){
        			    	bcsComunes.msg("error",$translate.instant("UI_SOLICITUD_TASAS"),$translate.instant('UI_ARCHIVO_TASAS_ERROR'));
        			    	bcsComunes.ventanaOverlay(sTas.idVentana,false);
        			    });
        			}, 2000);
        		}
        	},
        	function(result){
        		bcsComunes.msg("error",$translate.instant("UI_SOLICITUD_TASAS"),$translate.instant("UI_ARCHIVO_TASAS_ERROR"));
        		bcsComunes.ventanaOverlay(sTas.idVentana,false);
        	});
		};
		
	});
}());