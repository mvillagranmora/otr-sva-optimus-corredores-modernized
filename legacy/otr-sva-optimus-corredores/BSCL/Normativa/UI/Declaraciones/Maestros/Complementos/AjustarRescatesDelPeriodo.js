angular.module('ModuloNormativa').controller('AjustarRescatesDelPeriodoCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionMaestroDeclaraciones,bcsAlerta,bcsServiciosNormativa,OPCIONES_BCSALERTA,$timeout)
{
	$scope.formData = {};
	$scope.filtros = {};
	$scope.datos = {};
	
	var version = $scope.complementosMain.confGlobal.version;
	var esquema = $scope.complementosMain.confGlobal.esquema;
	
	$scope.filtros.agnotributario = version;
	
	//var conf = bcsConfiguracionMaestroDeclaraciones.getConsultaIPC();
	
	//$scope.defaultOptions = conf.options;
	//$scope.buscarIPC = conf.buscar;
	$scope.winID=bcsComunes.ventanaWinId($scope);
	
	/*
  	$scope.defaultOptions.gridCreated = function(grid)
  	{
		$scope.ajustaVentana();
	}
	*/
  	
  	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,40);
    }
  	
  	$scope.actualizarAjustarRescatesDelPeriodo = function()
  	{
		bcsComunes.ventanaOverlay($scope.winID,true);
		
		var parametrosSP = {};
        parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
        parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
        parametrosSP.p_IdModulo = 'NOR';
        parametrosSP.p_Version = version;
        
        if (esquema === 'DJ1871'){
	    	bcsServiciosNormativa.AjustarRescatesDelPeriodo(parametrosSP).$promise.then(function(respuesta){
	    		if (respuesta.P_OUT_ERRORESCONTADOR === 0){
		     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ACTUALIZAR_AJURESDELPER"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	   	     	} else {
		     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ACTUALIZAR_AJURESDELPER"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	   	     	}
	    		bcsComunes.ventanaOverlay($scope.winID,false);
		    },function(respuesta){
		    	bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ACTUALIZAR_AJURESDELPER"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		    	bcsComunes.ventanaOverlay($scope.winID,false);
		    });
        } else {
        	console.log('No existe esquema ' + esquema + ' configurado');
        	bcsComunes.ventanaOverlay($scope.winID,false);
        }
  	}
});