angular.module('ModuloNormativa').controller('Calculo4PorcientoCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionMaestroDeclaraciones,bcsAlerta,bcsServicioPlataforma,bcsServiciosNormativa,OPCIONES_BCSALERTA,$timeout){
	$scope.formData = {};
	$scope.filtros = {};
	var esquema = $scope.complementosMain.confGlobal.esquema;
	$scope.filtros.esquema = esquema;
	var version = $scope.complementosMain.confGlobal.version;
	$scope.filtros.version = version;
	$scope.datos = {};
	var pse = this;
	pse.idVentana 	= bcsComunes.ventanaWinId($scope);
	var conf = bcsConfiguracionMaestroDeclaraciones.getConsultaCalculo4Porciento();
	$scope.defaultOptions = conf.options;
	//$scope.buscar = conf.buscar;
	$scope.winID=bcsComunes.ventanaWinId($scope);
	
  	$scope.defaultOptions.gridCreated = function(grid){
		$scope.ajustaVentana();
	}
  	
  	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,40);
    }
  	
  	
	$scope.buscar = function(apiGrid,filtros) {
  		
		console.log(filtros.tipoNemo);
		console.log(filtros.Cliente.IdCliente);
		if( filtros.Cliente.IdCliente == 0 && filtros.tipoNemo.Nemotecnico == ' TODOS'){
			conf.buscar(apiGrid,filtros);
		}
		else if(filtros.Cliente.IdCliente == 0 && filtros.tipoNemo.Nemotecnico != ' TODOS'){
			var filters = new Array();
			if (filtros.tipoNemo && filtros.tipoNemo.Nemotecnico && filtros.tipoNemo.Nemotecnico != ' TODOS')
				filters.push({ field: "Nemotecnico", operator: "eq", value: filtros.tipoNemo.Nemotecnico });
			$scope.apiGrid.dataSource.filter(filters);
		}else if(filtros.Cliente.IdCliente != 0 && filtros.tipoNemo.Nemotecnico == ' TODOS'){
		  var filters = new Array();
		  if (filtros.Cliente && filtros.Cliente.IdCliente)
			  filters.push({ field: "IdCliente", operator: "eq", value: filtros.Cliente.IdCliente });
		  $scope.apiGrid.dataSource.filter(filters);
	  }
		else{
			var filters = new Array();
			if (filtros.Cliente && filtros.Cliente.IdCliente)
				filters.push({ field: "IdCliente", operator: "eq", value: filtros.Cliente.IdCliente });
			if (filtros.tipoNemo && filtros.tipoNemo.Nemotecnico && filtros.tipoNemo.Nemotecnico != ' TODOS')
			  filters.push({ field: "Nemotecnico", operator: "eq", value: filtros.tipoNemo.Nemotecnico });
			$scope.apiGrid.dataSource.filter(filters);
		}
  };



  var ActualizarEstadoInstrumento = function(Nemotecnico, IdEstado,filtros){
	bcsComunes.ventanaOverlay($scope.winID,true);
	var parametrosSP = {};
	parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
	parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
	parametrosSP.p_IdModulo = 'NOR';
	parametrosSP.p_Version = version;
	parametrosSP.p_IdCliente = filtros.Cliente.IdCliente;
	parametrosSP.p_NemotecnicoSVS = Nemotecnico;
	parametrosSP.p_IdEstadoInstrumento = IdEstado;
	 	
		if (esquema === 'DJ1874'){
			bcsServiciosNormativa.ActualizarEstadoInstrumento1874(parametrosSP).$promise.then(function(respuesta){
				$scope.defaultOptions.dataSource.data(respuesta.P_OUT_RESULTADO); 
					if (respuesta.P_OUT_ERRORESCONTADOR === 0){
					 bcsComunes.ventanaMsg({mensaje:"ACTUALIZADO: Estado de Instrumentos Actualizados",conflictos:util.parseServerResponse(respuesta)},$scope.winID);
					 conf.options.dataSource.read();
					}else{
					 bcsComunes.ventanaMsg({mensaje:"ERROR: Error al Actualizar Estados",conflictos:util.parseServerResponse(respuesta)},$scope.winID);
				  }
			},function(respuesta){
			  bcsComunes.ventanaMsg({mensaje:"ERROR: Error al Actualizar Estados",conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			});
		} 
	
	}
  	
  	
  	
  	$scope.ActivarProcesar = function(datos,estado,filtros){
		bcsComunes.ventanaOverlay($scope.winID,true);
		
		$.each(datos,function(key,sol){
			if(sol.IdEstado=="NO PROCESAR" || sol.IdEstado=="PROCESADO")
				sol.IdEstado="PROCESAR";
				ActualizarEstadoInstrumento(sol.Nemotecnico, sol.IdEstado,filtros);
				
				
  		});
		bcsComunes.ventanaOverlay($scope.winID,false);
	}
  	
  	$scope.DesactivarProcesar = function(datos,estado,filtros){
		bcsComunes.ventanaOverlay($scope.winID,true);
		
		$.each(datos,function(key,sol){
			if(sol.IdEstado=="PROCESAR" || sol.IdEstado=="PROCESADO"){
				sol.IdEstado="NO PROCESAR";
				ActualizarEstadoInstrumento(sol.Nemotecnico, sol.IdEstado,filtros);
				
			}
  		});
		bcsComunes.ventanaOverlay($scope.winID,false);
	}
  	
  	$scope.formData.oConfiguracionTipoNemo = {
			filtros : { 	
				IdCliente : {
			        "field"   : "IdCliente",
			        "operator": "eq",
			        "value"   : ""
				}				
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
  	
  	$scope.ProcesarNemos=function(){
		bcsComunes.ventanaOverlay(pse.idVentana,true);
		var parametrosSP = {};
		parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
		parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
		parametrosSP.p_IdModulo = 'NOR';
		parametrosSP.p_Version = version;

		var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_CONFIRMA" ) + " "  + "Proceso Reajuste 4%" +" ?",pse.idVentana);
    		t.then(function(result)
    		      {		
    				 if (result===OPCIONES_BCSALERTA.SI)
    			       {
						if (esquema === 'DJ1874'){

							bcsServiciosNormativa.ConsultarValidacionDJ1874(parametrosSP).$promise.then(function(respuesta){
								$scope.defaultOptions.dataSource.data(respuesta.P_OUT_RESULTADO); 
									if (respuesta.P_OUT_VALRESULTADO === 1){
										bcsServiciosNormativa.Reajuste4porciento1874(parametrosSP).$promise.then(function(respuesta){
											$scope.defaultOptions.dataSource.data(respuesta.P_OUT_RESULTADO); 
												if (respuesta.P_OUT_ERRORESCONTADOR === 0){
												 bcsComunes.ventanaMsg({mensaje:"REAJUSTE: Proceso finalizado",conflictos:util.parseServerResponse(respuesta)},$scope.winID);
												 conf.options.dataSource.read();
												}else{
												 bcsComunes.ventanaMsg({mensaje:"ERROR: Error al Actualizar Reajuste",conflictos:util.parseServerResponse(respuesta)},$scope.winID);
											  }
										},function(respuesta){
										  bcsComunes.ventanaMsg({mensaje:"ERROR: Error al Actualizar Reajuste",conflictos:util.parseServerResponse(respuesta)},$scope.winID);
										});
									}else{
									 bcsComunes.ventanaMsg({mensaje:"INFORMACIÓN: Se debe realizar la Valorización de la DJ",conflictos:util.parseServerResponse(respuesta)},$scope.winID);
								  }
							},function(respuesta){
							  bcsComunes.ventanaMsg({mensaje:"ERROR: Error Consultar Valorización",conflictos:util.parseServerResponse(respuesta)},$scope.winID);
							});


							
						}
    					
    					
    			       };
    			});
		
		
		
				//bcsComunes.ventanaOverlay($scope.winID,true);
		 
			 
			   
			   
  	}
  	
});