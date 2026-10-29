angular.module('ModuloNormativa').controller('FondoAccionarioCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionMaestroDeclaraciones,bcsAlerta,bcsServiciosNormativa,OPCIONES_BCSALERTA,$timeout){
	$scope.formData = {};
	$scope.filtros = {};
	var esquema = $scope.complementosMain.confGlobal.esquema;
	$scope.filtros.esquema = esquema;
	var version = $scope.complementosMain.confGlobal.version;
	$scope.datos = {};
	var conf = bcsConfiguracionMaestroDeclaraciones.getConsultaFondoAccionario();
	$scope.defaultOptions = conf.options;
	$scope.buscarFondoAccionarios = conf.buscar;
	$scope.winID=bcsComunes.ventanaWinId($scope);
	
  	$scope.defaultOptions.gridCreated = function(grid){
		$scope.ajustaVentana();
	}
  	
  	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,40);
    }
  	
  	$scope.ingresarFondoAccionario = function(){
		 bcsComunes.ventanaOverlay($scope.winID,true);
		 var datosArchivo = {codigoArchivo: 'ARCHIVO_FONDOS_ACCIONARIOS'};
		 var idVentanaCargaArchivo = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,true,true);
		 var promesa=bcsComunes.ventanaPromesa(idVentanaCargaArchivo);                               
		 promesa.then(function(data){
			 if(!data || !data.fileData){
				 bcsComunes.ventanaOverlay($scope.winID,false);
				 return;
			 }
			 $scope.datosCarga = data.fileData;
			 cargarFondosAccionarios();
		 });
	}
  	
  	var cargarFondosAccionarios = function(){
		bcsComunes.ventanaOverlay($scope.winID,true);
		var parametrosSP = {};
        parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
        parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
        parametrosSP.p_IdModulo = 'NOR';
        parametrosSP.p_Version = version;
        parametrosSP.p_Fondos = $scope.datosCarga;
   	    
        if($scope.datosCarga.length)
    	{ 	
        	if (esquema === 'DJ1894'){
	        	bcsServiciosNormativa.CrearFondoAccionario1894(parametrosSP).$promise.then(function(respuesta){
		    		$scope.defaultOptions.dataSource.data(respuesta.P_OUT_RESULTADO); 
		   	     	if (respuesta.P_OUT_ERRORESCONTADOR === 0){
			     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_CARGAR_FONDOS"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			     		conf.options.dataSource.read();
		   	     	}else{
			     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_CARGAR_FONDOS"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			     	 }
			    },function(respuesta){
				  bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_CARGAR_FONDOS"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			    });
        	} else if (esquema === 'DJ1922'){
        		bcsServiciosNormativa.CrearFondoAccionario1922(parametrosSP).$promise.then(function(respuesta){
		    		$scope.defaultOptions.dataSource.data(respuesta.P_OUT_RESULTADO); 
		   	     	if (respuesta.P_OUT_ERRORESCONTADOR === 0){
			     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_CARGAR_FONDOS"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			     		conf.options.dataSource.read();
		   	     	}else{
			     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_CARGAR_FONDOS"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			     	 }
			    },function(respuesta){
				  bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_CARGAR_FONDOS"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			    });
        	} else {
            	console.log('No existe esquema ' + esquema + ' configurado');
            	bcsComunes.ventanaOverlay($scope.winID,false);
            }
       	}
	}
  	
  	$scope.cargarFondosAccionarios = function(){
		bcsComunes.ventanaOverlay($scope.winID,true);
		var parametrosSP = {};
        parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
        parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
        parametrosSP.p_IdModulo = 'NOR';
        parametrosSP.p_Version = version;
   	    
        if (esquema === 'DJ1894'){
	    	bcsServiciosNormativa.CargarFondos1894(parametrosSP).$promise.then(function(respuesta){
	    		$scope.defaultOptions.dataSource.data(respuesta.P_OUT_RESULTADO); 
	   	     	if (respuesta.P_OUT_ERRORESCONTADOR === 0){
		     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ACTUALIZAR_FONDOS"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		     		conf.options.dataSource.read();
	   	     	}else{
		     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ACTUALIZAR_FONDOS"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		     	 }
		    },function(respuesta){
			  bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ACTUALIZAR_FONDOS"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		    });
        } else if (esquema === 'DJ1922'){
	    	bcsServiciosNormativa.CargarFondos1922(parametrosSP).$promise.then(function(respuesta){
	    		$scope.defaultOptions.dataSource.data(respuesta.P_OUT_RESULTADO); 
	   	     	if (respuesta.P_OUT_ERRORESCONTADOR === 0){
		     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ACTUALIZAR_FONDOS"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		     		conf.options.dataSource.read();
	   	     	}else{
		     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ACTUALIZAR_FONDOS"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		     	 }
		    },function(respuesta){
			  bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ACTUALIZAR_FONDOS"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		    });
        } else {
        	console.log('No existe esquema ' + esquema + ' configurado');
        	bcsComunes.ventanaOverlay($scope.winID,false);
        }
	}
  	
  	$scope.activarDesactivarFondosAccionarios = function(datos,estado){
		bcsComunes.ventanaOverlay($scope.winID,true);
		var parametrosSP = {
	        p_IdEmpresa : $scope.$root.SesionIdEmpresa,
	        p_IdUsuario :  $scope.$root.SesionIdUsuario,
	        p_IdModulo  : 'NOR',
	        p_Version   : version,
	        p_Fondos    : []
		};
		$.each(datos,function(key,sol){
			parametrosSP.p_Fondos.push({IdFondo : sol.IdFondo, IdEstado : estado});
  		});
		if (esquema === 'DJ1894'){
	    	bcsServiciosNormativa.ActivarDesactivarFondos1894(parametrosSP).$promise.then(function(respuesta){
	    		$scope.defaultOptions.dataSource.data(respuesta.P_OUT_RESULTADO); 
	   	     	if (respuesta.P_OUT_ERRORESCONTADOR === 0 && respuesta.P_OUT_ADVERTENCIASCONTADOR === 0){
		     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ACTIVAR_DESACTIVAR_FONDOS"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		     		conf.options.dataSource.read();
	   	     	}else{
		     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ACTIVAR_DESACTIVAR_FONDOS"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		     	}
		    },function(respuesta){
			  bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ACTIVAR_DESACTIVAR_FONDOS"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		    });
		} else if (esquema === 'DJ1922'){
	    	bcsServiciosNormativa.ActivarDesactivarFondos1922(parametrosSP).$promise.then(function(respuesta){
	    		$scope.defaultOptions.dataSource.data(respuesta.P_OUT_RESULTADO); 
	   	     	if (respuesta.P_OUT_ERRORESCONTADOR === 0 && respuesta.P_OUT_ADVERTENCIASCONTADOR === 0){
		     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ACTIVAR_DESACTIVAR_FONDOS"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		     		conf.options.dataSource.read();
	   	     	}else{
		     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ACTIVAR_DESACTIVAR_FONDOS"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		     	}
		    },function(respuesta){
			  bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ACTIVAR_DESACTIVAR_FONDOS"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		    });
		} else {
			console.log('No existe esquema' + esquema + ' configurado');
			bcsComunes.ventanaOverlay($scope.winID,false);
		}
	}
  	
});