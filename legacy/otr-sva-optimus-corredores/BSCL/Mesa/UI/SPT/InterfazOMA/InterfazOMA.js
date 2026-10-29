(function() {
	angular.module('ModuloPlataforma').controller('InterfazOMACtrl',
	function($scope,$translate,bcsComunes,bcsServiciosMesa,$http,$timeout){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var InterfazOma         = this;
		InterfazOma.idVentana	= bcsComunes.ventanaWinId($scope);
		InterfazOma.cmbbx		= {};
		InterfazOma.filtro		= {};
		InterfazOma.fn			= {};
		InterfazOma.ChkTXT		= true;
		InterfazOma.ngHora      = true;
		InterfazOma.haBoton 	= true;
		$scope.ngHoraDesde		= '00:00:00';
		$scope.ngHoraHasta		= '16:00:00';
		InterfazOma.filtro.Fecha = util.dateDS($scope.$root.SesionFechaSistema,"FechaSistema","CALL");
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		InterfazOma.cmbbx.Fuente={
			filtros: {
				IdEmpresa	: {field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdEstado	: {field: "IdEstado",	operator: "eq", value: 'ACT'}
			}
		};
		
		
		/*********************************************
		 * WATCHES
		 *********************************************/

		$scope.$watch('InterfazOma.ChkTXT',function(newval,oldval){
			if(InterfazOma.ChkTXT)
				InterfazOma.ChkCSV = false;
			else
				InterfazOma.ChkCSV = true;
				
		});
		
		$scope.$watch('InterfazOma.ChkCSV',function(newval,oldval){
			if(InterfazOma.ChkCSV)
				InterfazOma.ChkTXT = false;
			else
				InterfazOma.ChkTXT = true;
		});
		
		$scope.$watch('InterfazOma.ChkHora',function(newval,oldval){
			var desde = new Date('1/1/1990 '+$scope.ngHoraDesde);
			var hasta = new Date('1/1/1990 '+$scope.ngHoraHasta);
			
			if(InterfazOma.ChkHora){
				InterfazOma.ngHora 	= false;
				InterfazOma.haBoton = true
				if(hasta >= desde){
					InterfazOma.haBoton = true;
					
				}
				else{
					InterfazOma.haBoton = false;
					bcsComunes.msg("error","Error","Hora desde debe ser mayor o igual a la Hora hasta.");
				}

			}
			else if($scope.ngHoraDesde !== undefined && $scope.ngHoraHasta !== undefined  && InterfazOma.ChkHora){
				InterfazOma.ngHora = true;
			}
			else if($scope.ngHoraDesde == undefined && $scope.ngHoraHasta == undefined){
				InterfazOma.ngHora = true;
			}
			else if(InterfazOma.ChkHora == false ){
				InterfazOma.haBoton = true;
				InterfazOma.ngHora 	= true;
			}
		});
		
		$scope.$watch('ngHoraDesde',function(newval,oldval){
			
			var desde = new Date('1/1/1990 '+$scope.ngHoraDesde);
			var hasta = new Date('1/1/1990 '+$scope.ngHoraHasta);
			
			if($scope.ngHoraDesde !== undefined && $scope.ngHoraHasta === undefined){
				InterfazOma.ngHoraDes = false;
				$scope.ngHoraHasta = $scope.ngHoraDesde;
			}
			else if($scope.ngHoraDesde === ""){
				$scope.ngHoraHasta = $scope.ngHoraDesde;
			}
			else if(desde > hasta && desde != hasta){				
				InterfazOma.haBoton = false;
				bcsComunes.msg("error","Error","Hora desde debe ser mayor o igual a la Hora hasta.");
			}
			else if(hasta >= desde){
				InterfazOma.haBoton = true;
			}
			
		});
		
		$scope.$watch('ngHoraHasta',function(newval,oldval){
			
			var desde = new Date('1/1/1990 '+$scope.ngHoraDesde);
			var hasta = new Date('1/1/1990 '+$scope.ngHoraHasta);
			
			if(desde > hasta && desde != hasta){
				InterfazOma.haBoton 	= false;
				InterfazOma.haBoton 	= false;
				bcsComunes.msg("error","Error","Hora desde debe ser mayor o igual a la Hora hasta.");
			}
			else if(hasta >= desde){
				InterfazOma.haBoton 	= true;
			}
		}); 
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		
		InterfazOma.fn.generarArchivo = function(){
			var parametrosSP = {};
			parametrosSP.p_IdEmpresa		= $scope.$root.SesionIdEmpresa;
			parametrosSP.p_IdUsuario		= $scope.$root.SesionIdUsuario;
	        parametrosSP.p_IdModulo			= 'MSP';
			parametrosSP.p_UsaReglasNegocio	= 'N';
	        
	        parametrosSP.p_Fecha		    = util.dateSPdate(InterfazOma.filtro.Fecha);
	        parametrosSP.p_CodigoComercio	= 0;

			if(InterfazOma.ngHora == false && $scope.ngHoraDesde !== undefined){				
				if($scope.ngHoraDesde == null || $scope.ngHoraHasta == null || $scope.ngHoraDesde.trim() == '' || $scope.ngHoraHasta.trim() == ''){
					parametrosSP.p_FechaHoraDesde = '';
					parametrosSP.p_FechaHoraHasta = '';
				}else{
					parametrosSP.p_FechaHoraDesde = parametrosSP.p_Fecha+ " " +$scope.ngHoraDesde;
					parametrosSP.p_FechaHoraHasta = parametrosSP.p_Fecha+ " " +$scope.ngHoraHasta;
				}
			}else{
				parametrosSP.p_FechaHoraDesde = '';
				parametrosSP.p_FechaHoraHasta = '';
			}
			
			
	        
			if(InterfazOma.ChkCSV)
				parametrosSP.p_Formato		= 'CSV';
			else
				parametrosSP.p_Formato  	= 'TXT';
			
	        //if(!parametrosSP.p_Fecha || !parametrosSP.p_CodigoComercio) return;
	        
        	bcsComunes.ventanaOverlay(InterfazOma.idVentana,true);
        	bcsServiciosMesa.GenerarArchivoOMA(parametrosSP).$promise.then(function(result) {
        		if (result.P_OUT_ADVERTENCIASCONTADOR){
        			bcsComunes.msg("warning",$translate.instant("UI_SOLICITUD_OMA"),result.P_OUT_ADVERTENCIASDETALLE[0].Codigo);
        			bcsComunes.ventanaOverlay(InterfazOma.idVentana,false);
        		}
        		else if (result.P_OUT_ERRORESCONTADOR){
        			bcsComunes.msg("error",$translate.instant("UI_SOLICITUD_OMA"),result.P_OUT_ERRORESDETALLE[0].Codigo);
        			bcsComunes.ventanaOverlay(InterfazOma.idVentana,false);
        		}
        		else{
					var ruta = result.P_OUT_RESULTADOPROCESO[9].Valor;
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
        			    	bcsComunes.msg("info",$translate.instant("UI_SOLICITUD_OMA"),$translate.instant("UI_ARCHIVO_OMA_OK"));
        			    	bcsComunes.ventanaOverlay(InterfazOma.idVentana,false);
        			    }).error(function (err){
        			    	bcsComunes.msg("error",$translate.instant("UI_SOLICITUD_OMA"),$translate.instant('UI_ARCHIVO_OMA_ERROR'));
        			    	bcsComunes.ventanaOverlay(InterfazOma.idVentana,false);
        			    });
        			}, 2000);
        		}
        	},
        	function(result){
        		bcsComunes.msg("error",$translate.instant("UI_SOLICITUD_OMA"),$translate.instant("UI_ARCHIVO_OMA_ERROR"));
        		bcsComunes.ventanaOverlay(InterfazOma.idVentana,false);
        	});
		};

	});
}());