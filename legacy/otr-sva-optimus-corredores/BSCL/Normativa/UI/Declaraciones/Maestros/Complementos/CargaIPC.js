angular.module('ModuloNormativa').controller('CargaIpcCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionMaestroDeclaraciones,bcsAlerta,bcsServiciosNormativa,OPCIONES_BCSALERTA,$timeout){
	$scope.formData = {};
	$scope.filtros = {};
	$scope.datos = {};
	var version = $scope.complementosMain.confGlobal.version;
	var esquema = $scope.complementosMain.confGlobal.esquema;
	$scope.filtros.agnotributario = version; 
	var conf = bcsConfiguracionMaestroDeclaraciones.getConsultaIPC();
	$scope.defaultOptions = conf.options;
	$scope.buscarIPC = conf.buscar;
	$scope.winID=bcsComunes.ventanaWinId($scope);

  	$scope.defaultOptions.gridCreated = function(grid){
		$scope.ajustaVentana();
	}
  	
  	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,40);
    }
  	
  	$scope.cargarIPC = function(){
		bcsComunes.ventanaOverlay($scope.winID,true);
		var parametrosSP = {};
        parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
        parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
        parametrosSP.p_IdModulo = 'NOR';
        parametrosSP.p_SimularOperacion = 'N';
        parametrosSP.p_UsaReglasNegocio = 'S';
   	    
    	bcsServiciosNormativa.CrearProgramaCargaIPC(parametrosSP).$promise.then(function(respuesta){
   	     	if (respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_CARGAR_IPC"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
   	     	}else{
	     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_CARGAR_IPC"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     	 }
   	     	bcsComunes.ventanaOverlay($scope.winID,false);
	    },function(respuesta){
		  bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_CARGAR_IPC"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		  bcsComunes.ventanaOverlay($scope.winID,false);
	    });
	}
  	
  	$scope.actualizarIPC = function(){
		bcsComunes.ventanaOverlay($scope.winID,true);
		var parametrosSP = {};
        parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
        parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
        parametrosSP.p_IdModulo = 'NOR';
        parametrosSP.p_Version = version;
        if (esquema === 'DJ1894'){
	    	bcsServiciosNormativa.ActualizarValorFactor1894(parametrosSP).$promise.then(function(respuesta){
	   	     	if (respuesta.P_OUT_ERRORESCONTADOR === 0){
		     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ACTUALIZAR_IPC"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	   	     	}else{
		     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ACTUALIZAR_IPC"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		     	 }
	   	     	bcsComunes.ventanaOverlay($scope.winID,false);
		    },function(respuesta){
			  bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ACTUALIZAR_IPC"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			  bcsComunes.ventanaOverlay($scope.winID,false);
		    });
        } else if (esquema === 'DJ1922'){
	    	bcsServiciosNormativa.ActualizarValorFactor1922(parametrosSP).$promise.then(function(respuesta){
	   	     	if (respuesta.P_OUT_ERRORESCONTADOR === 0){
		     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ACTUALIZAR_IPC"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	   	     	}else{
		     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ACTUALIZAR_IPC"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		     	 }
	   	     	bcsComunes.ventanaOverlay($scope.winID,false);
		    },function(respuesta){
			  bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ACTUALIZAR_IPC"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			  bcsComunes.ventanaOverlay($scope.winID,false);
		    });
        } else if (esquema === 'DJ1871'){
	    	bcsServiciosNormativa.ActualizarValorFactor1871(parametrosSP).$promise.then(function(respuesta){
	   	     	if (respuesta.P_OUT_ERRORESCONTADOR === 0){
		     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ACTUALIZAR_IPC"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	   	     	}else{
		     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ACTUALIZAR_IPC"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		     	 }
	   	     	bcsComunes.ventanaOverlay($scope.winID,false);
		    },function(respuesta){
			  bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ACTUALIZAR_IPC"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			  bcsComunes.ventanaOverlay($scope.winID,false);
		    });
        } else if (esquema === 'DJ1889'){
	    	bcsServiciosNormativa.ActualizarValorFactor1889(parametrosSP).$promise.then(function(respuesta){
	   	     	if (respuesta.P_OUT_ERRORESCONTADOR === 0){
		     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ACTUALIZAR_IPC"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	   	     	}else{
		     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ACTUALIZAR_IPC"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		     	 }
	   	     	bcsComunes.ventanaOverlay($scope.winID,false);
		    },function(respuesta){
			  bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ACTUALIZAR_IPC"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			  bcsComunes.ventanaOverlay($scope.winID,false);
		    });
        } else if (esquema === 'DJ1899'){
	    	bcsServiciosNormativa.ActualizarValorFactor1899(parametrosSP).$promise.then(function(respuesta){
	   	     	if (respuesta.P_OUT_ERRORESCONTADOR === 0){
		     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ACTUALIZAR_IPC"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	   	     	}else{
		     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ACTUALIZAR_IPC"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		     	 }
	   	     	bcsComunes.ventanaOverlay($scope.winID,false);
		    },function(respuesta){
			  bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ACTUALIZAR_IPC"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			  bcsComunes.ventanaOverlay($scope.winID,false);
		    });
        } else {
        	console.log('No existe esquema ' + esquema + ' configurado');
        	bcsComunes.ventanaOverlay($scope.winID,false);
        }
  	}
});