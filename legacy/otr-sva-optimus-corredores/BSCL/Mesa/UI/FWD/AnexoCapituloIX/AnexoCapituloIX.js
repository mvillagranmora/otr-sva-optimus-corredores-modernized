(function() {
	angular.module('ModuloMesa').controller('AnexoCapituloIX',
	function($scope,$translate,$resource,$timeout,bcsConsultasMesa,bcsServiciosMesa,bcsComunes,bcsServicioPlataforma,$http)
	{		
		var idVentana = bcsComunes.ventanaWinId($scope);
		$scope.data = {};
		$scope.data.LimiteFecha = {};
		$scope.data.ChkTXT		= true;
		
		
		$scope.data.LimiteFecha.max = new Date(util.date($scope.data.kngFechaDesde).toDate());
		$scope.data.kngFechaDesde = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.kngFechaHasta = util.dateInicioDia($scope.SesionFechaSistema); 
		

		/*********************************************
		 * WATCHES
		 *********************************************/

		$scope.$watch('data.ChkTXT',function(newval,oldval){
			if($scope.data.ChkTXT)
				$scope.data.ChkCSV = false;
			else
				$scope.data.ChkCSV = true;
				
		});
		
		$scope.$watch('data.ChkCSV',function(newval,oldval){
			if($scope.data.ChkCSV)
				$scope.data.ChkTXT = false;
			else
				$scope.data.ChkTXT = true;
		});
		/*********************************************
		 * FUNCIONES
		 *********************************************/

		$scope.data.generarArchivo = function(){
			var parametrosSP = {};
			parametrosSP.p_IdEmpresa		= $scope.$root.SesionIdEmpresa;
			parametrosSP.p_IdUsuario		= $scope.$root.SesionIdUsuario;
	        parametrosSP.p_IdModulo			= 'FWD';
			parametrosSP.p_UsaReglasNegocio	= 'N';

	        parametrosSP.p_FechaDesde	    = util.dateSPdate($scope.data.kngFechaDesde);
	        parametrosSP.p_FechaHasta	    = util.dateSPdate($scope.data.kngFechaHasta);
	        parametrosSP.p_CodigoComercio	= 0
	        
			if($scope.data.ChkCSV)
				parametrosSP.p_Formato		= 'CSV';
			else
				parametrosSP.p_Formato  	= 'TXT';
			
	        //if(!parametrosSP.p_Fecha || !parametrosSP.p_CodigoComercio) return;
	        
        	bcsComunes.ventanaOverlay(idVentana,true);
        	bcsServiciosMesa.GenerarArchivoAnexoIX(parametrosSP).$promise.then(function(result) {
        		if (result.P_OUT_ADVERTENCIASCONTADOR){
        			bcsComunes.msg("warning",$translate.instant("UI_SOLICITUD_ANEXO_IX"),result.P_OUT_ADVERTENCIASDETALLE[0].Codigo);
        			bcsComunes.ventanaOverlay(idVentana,false);
        		}
        		else if (result.P_OUT_ERRORESCONTADOR){
        			bcsComunes.msg("error",$translate.instant("UI_SOLICITUD_ANEXO_IX"),result.P_OUT_ERRORESDETALLE[0].Codigo);
        			bcsComunes.ventanaOverlay(idVentana,false);
        		}
        		else{
        			var ruta = result.P_OUT_RESULTADOPROCESO[7].Valor;
        			var nombre = result.P_OUT_RESULTADOPROCESO[4].Valor;
        			
        			$timeout(function(){
        				var url = "/BSCL/Plataforma/GWJAVA/PLA/download?";
        			    var datos = { file: ruta + nombre };
        			    $http({
        			    	method: 'POST',
        			    	url: url,
        			    	params: datos
        			    }).success(function(datos){
        			    	var file = new File([datos], nombre);
        			    	saveAs(file);
        			    	bcsComunes.msg("info",$translate.instant("UI_SOLICITUD_ANEXO_IX"),$translate.instant("UI_ARCHIVO_ANEXO_IX_OK"));
        			    	bcsComunes.ventanaOverlay(idVentana,false);
        			    }).error(function (err){
        			    	bcsComunes.msg("error",$translate.instant("UI_SOLICITUD_ANEXO_IX"),$translate.instant('UI_ARCHIVO_ANEXO_IX_ERROR'));
        			    	bcsComunes.ventanaOverlay(idVentana,false);
        			    });
        			}, 8000);
        		}
        	},
        	function(result){
        		bcsComunes.msg("error",$translate.instant("UI_SOLICITUD_ANEXO_IX"),$translate.instant("UI_ARCHIVO_ANEXO_IX_ERROR"));
        		bcsComunes.ventanaOverlay(idVentana,false);
        	});
		};
		
	});
}());



