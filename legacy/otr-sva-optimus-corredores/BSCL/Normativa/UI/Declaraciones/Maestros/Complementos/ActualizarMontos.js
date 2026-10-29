angular.module('ModuloNormativa').controller('ActualizarMontosCtrl',function($scope,Utils,$translate,Model,$compile,$modal,bcsComunes,bcsConfiguracionMaestroDeclaraciones,bcsAlerta,bcsServiciosNormativa,OPCIONES_BCSALERTA,$timeout){
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
  	
  	$scope.actualizarMontos = function(){
		bcsComunes.ventanaOverlay(pse.idVentana,true);
		var Afecto100 =0
		if(pse.Afecto100Sel ===true){Afecto100 =1;};
		
		
		var parametrosSP = {};
        parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
        parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
        parametrosSP.p_IdModulo = 'NOR';
        parametrosSP.p_Version = version;
        
        
    	var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_CONFIRMA" ) + " "  + $translate.instant("UI_PROCESAR_MONTOS" ) +" ?",pse.idVentana);
		t.then(function(result)
		      {		
				 if (result===OPCIONES_BCSALERTA.SI)
			       {	
					 
					 if (esquema === 'DJ1899' || esquema === 'DJ1922' || esquema === 'DJ1949' ){
					        if (esquema === 'DJ1899'){
						    	bcsServiciosNormativa.ActualizarMontos1899(parametrosSP).$promise.then(function(respuesta){
						   	     	if (respuesta.P_OUT_ERRORESCONTADOR === 0){
							     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_PROCESAR_MONTOS"),conflictos:util.parseServerResponse(respuesta)},pse.idVentana);
						   	     	}else{
							     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ACTUALIZAR_MONTOS"),conflictos:util.parseServerResponse(respuesta)},pse.idVentana);
							     	 }
						   	     	bcsComunes.ventanaOverlay(pse.idVentana,false);
							    },function(respuesta){
								  bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ACTUALIZAR_MONTOS"),conflictos:util.parseServerResponse(respuesta)},pse.idVentana);
								  bcsComunes.ventanaOverlay(pse.idVentana,false);
							    });
					        }
					        if (esquema === 'DJ1922'){
					        	parametrosSP.p_Afecto100 = Afecto100;
					        	
						    	bcsServiciosNormativa.ActualizarMontos1922(parametrosSP).$promise.then(function(respuesta){
						   	     	if (respuesta.P_OUT_ERRORESCONTADOR === 0){
							     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_PROCESAR_MONTOS"),conflictos:util.parseServerResponse(respuesta)},pse.idVentana);
						   	     	}else{
							     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ACTUALIZAR_MONTOS"),conflictos:util.parseServerResponse(respuesta)},pse.idVentana);
							     	 }
						   	     	bcsComunes.ventanaOverlay(pse.idVentana,false);
							    },function(respuesta){
								  bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ACTUALIZAR_MONTOS"),conflictos:util.parseServerResponse(respuesta)},pse.idVentana);
								  bcsComunes.ventanaOverlay(pse.idVentana,false);
							    });
					        }
					        if (esquema === 'DJ1949'){
					        	parametrosSP.p_Afecto100 = Afecto100;
						    	bcsServiciosNormativa.ActualizarMontos1949(parametrosSP).$promise.then(function(respuesta){
						   	     	if (respuesta.P_OUT_ERRORESCONTADOR === 0){
							     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_PROCESAR_MONTOS"),conflictos:util.parseServerResponse(respuesta)},pse.idVentana);
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