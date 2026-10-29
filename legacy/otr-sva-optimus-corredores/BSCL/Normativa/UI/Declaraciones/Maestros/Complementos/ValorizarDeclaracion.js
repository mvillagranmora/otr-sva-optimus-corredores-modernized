angular.module('ModuloNormativa').controller('ValorizarDeclaracionCtrl',function($scope,Utils,$translate,Model,$compile,$modal,bcsComunes,bcsConfiguracionMaestroDeclaraciones,bcsAlerta,bcsServicioPlataforma,bcsServiciosNormativa,OPCIONES_BCSALERTA,$timeout){
	$scope.formData = {};
	$scope.filtros = {};
	$scope.datos = {};
	var version = $scope.complementosMain.confGlobal.version;
	var esquema = $scope.complementosMain.confGlobal.esquema;
	$scope.filtros.version = version;
	var pse = this;
	pse.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
  	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico(pse.idVentana,40);
    }
  	
  	$scope.ValorizarDeclaracionDirecta = function(){
		bcsComunes.ventanaOverlay(pse.idVentana,true);
		var FuenteDatos ='';
		if(pse.CargaSistema ===true){FuenteDatos ='INICIAL';};
		if(pse.CargaExterna ===true){FuenteDatos ='EXTERNA';};
		if (pse.CargaSistema ===true && pse.CargaExterna ===true){FuenteDatos ='AMBAS';};
		
		var parametrosSP = {};
        parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
        parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
        parametrosSP.p_IdModulo = 'NOR';
        parametrosSP.p_Version = version;
        parametrosSP.p_FuenteDatos = FuenteDatos;
        
        if (pse.CargaSistema ===true || pse.CargaExterna ===true){
        	var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_CONFIRMA" ) + " "  + $translate.instant("UI_VALORIZAR_DECLARACION" ) +" ?",pse.idVentana);
    		t.then(function(result)
    		      {		
    				 if (result===OPCIONES_BCSALERTA.SI)
    			       {	
    					 
    					 if (esquema === 'DJ1874' || esquema === 'DJ1874'  ){
    					        if (esquema === 'DJ1874'){
    						    	bcsServiciosNormativa.ValorizarDeclaracion1874(parametrosSP).$promise.then(function(respuesta){
    						   	     	if (respuesta.P_OUT_ERRORESCONTADOR === 0){
    							     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_VALORIZAR_DECLARACION"),conflictos:util.parseServerResponse(respuesta)},pse.idVentana);
    						   	     	}else{
    							     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ACTUALIZAR_MONTOS"),conflictos:util.parseServerResponse(respuesta)},pse.idVentana);
    							     	 }
    						   	     	bcsComunes.ventanaOverlay(pse.idVentana,false);
    							    },function(respuesta){
    								  bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ACTUALIZAR_MONTOS"),conflictos:util.parseServerResponse(respuesta)},pse.idVentana);
    								  bcsComunes.ventanaOverlay(pse.idVentana,false);
    							    });
    					        }

    					        } else {
    					        	console.log('No existe esquema ' + esquema + ' configurado');
    					        	bcsComunes.ventanaOverlay(pse.idVentana,false);
    					        }
    					
    					 

    					
    					 
    			       };
    			});	

        }else{
        	bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_SELECCIONAR"));
        	bcsComunes.ventanaOverlay(pse.idVentana,false);
        }
    	        
     
        
	}
  	
 	
  	$scope.ValorizarDeclaracion = function(){
		bcsComunes.ventanaOverlay(pse.idVentana,true);
		var FuenteDatos ='';
		if(pse.CargaSistema ===true){FuenteDatos ='INICIAL';};
		if(pse.CargaExterna ===true){FuenteDatos ='EXTERNA';};
		if (pse.CargaSistema ===true && pse.CargaExterna ===true){FuenteDatos ='AMBAS';};
		
		var parametrosSP = {};
        parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
        parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
        parametrosSP.p_IdModulo = 'NOR';
        parametrosSP.p_Version = version;
        parametrosSP.p_FuenteDatos = FuenteDatos;
        
        if (pse.CargaSistema ===true || pse.CargaExterna ===true){
        	var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_CONFIRMA" ) + " "  + $translate.instant("UI_VALORIZAR_DECLARACION" ) +" ?",pse.idVentana);
    		t.then(function(result)
    		      {		
    				 if (result===OPCIONES_BCSALERTA.SI)
    			       {	
    					 
    					 if (esquema === 'DJ1874' || esquema === 'DJ1874'  ){
    						 
    						 
    						var entradaPrograma 		= {};					
							entradaPrograma.p_IdEmpresa			= $scope.SesionIdEmpresa;
							entradaPrograma.p_IdUsuario 		= $scope.SesionIdUsuario;
							entradaPrograma.p_IdModulo    		= 'NOR';
							entradaPrograma.p_IdAgenda    		= 0;    
							entradaPrograma.p_IdTipoTarea   	= 'VALORIZAR_DECLARACION';
							entradaPrograma.p_Segundos      	= 0;			
							entradaPrograma.p_CreaParam     	= 'S';
							entradaPrograma.p_TablaParametros =
							[
							   {NombreParametro	: 'IdEmpresa', 	  		Valor: $scope.SesionIdEmpresa},
							   {NombreParametro	: 'IdUsuario', 	  		Valor: $scope.SesionIdUsuario},
							   {NombreParametro	: 'IdModulo', 	  		Valor: 'NOR'},
							   {NombreParametro	: 'Esquema', 	  		Valor: esquema},
							   {NombreParametro	: 'Version', 	  		Valor: version},
							   {NombreParametro	: 'FuenteDatos', 	  	Valor: FuenteDatos}
							];

							console.log("ENTRADA BATCH> " + JSON.stringify(entradaPrograma));
										
							bcsServicioPlataforma.AgregarPrograma(entradaPrograma,
								function(respuesta){
									if (respuesta.P_OUT_ERRORESCONTADOR === 0){						
										Programa = respuesta.P_OUT_IDPROGRAMA;					
										if (Programa.length > 0)
										    idPrograma = Programa[0].IdPrograma;								
												
										bcsComunes.ventanaMsg({
											mensaje: "Programa solicitado correctamente (#" + idPrograma +"). Vea su ejecucion en Consultar Programas"							
										},pse.idVentana);						
									}
									else
									{						
										bcsComunes.ventanaMsg({
											mensaje:$translate.instant("ERROR")							
										},pse.idVentana);
									}
								},
								function(respuesta){
									bcsComunes.ventanaMsg({
										mensaje:$translate.instant("ERROR"),
										conflictos:util.parseServerResponse(respuesta)
									},pse.idVentana);					
								}
							);	
    					        
    					        
    					        

    					        } else {
    					        	console.log('No existe esquema ' + esquema + ' configurado');
    					        	bcsComunes.ventanaOverlay(pse.idVentana,false);
    					        }
    					
    					 

    					
    					 
    			       };
    			});	

        }else{
        	bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_SELECCIONAR"));
        	bcsComunes.ventanaOverlay(pse.idVentana,false);
        }
    	        
     
        
	}
  	
  	$scope.CargaGrilla = function(){
  		if (esquema === 'DJ1899'){
  		var conf = bcsConfiguracionMaestroDeclaraciones.getConsultaMontosDJ1899();
	  		$scope.defaultOptions = conf.options;
	  		$scope.buscarMontos = conf.buscar;
	  		pse.idVentana=bcsComunes.ventanaWinId($scope);
	
	  	  	$scope.defaultOptions.gridCreated = function(grid){
	  			$scope.ajustaVentana();
	  		}
  		}
  		
  	}
  	
  	$scope.CargaGrilla();
});