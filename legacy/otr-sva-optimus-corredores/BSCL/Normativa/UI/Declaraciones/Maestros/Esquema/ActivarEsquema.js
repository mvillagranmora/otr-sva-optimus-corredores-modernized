angular.module('ModuloNormativa').controller('ActivarEsquemaDeclaracionCtrl',function($scope,$translate,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,bcsConfiguracionMaestroDeclaraciones,bcsServicioMaestroDeclaraciones,$timeout){
	
	$scope.formData = {};
	$scope.datos = {};
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.parametros = bcsComunes.ventanaParams($scope.winID);
	
	$scope.datos.oConfiguracionVersion = {
		filtros:{
			Esquema : { "field"   : "Esquema", "operator": "eq", "value"   :  $scope.parametros.esquema}
		}	
	};
	
	$scope.activarEsquema = function(){
		bcsComunes.ventanaOverlay($scope.winID,true);
		var datosActivarVersion = {
				 p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
				 p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
				 p_IdModulo : 'NOR',
				 p_simularOperacion : 'N',
				 p_UsaReglasNegocio : 'S',
				 
				 p_Versiones : [{
					    IdInterno		      		:	0,
					    Esquema						:   $scope.parametros.esquema,
					    Version 	          		:	$scope.formData.version,
					    FechaPresentacionLimite     :	util.dateSPdate($scope.formData.fechaLimitePresentacion)
				 }]
		}
		bcsServicioMaestroDeclaraciones.ActivarVersion(datosActivarVersion).$promise.then(function(respuesta){
			if (respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		var id = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ACTIVAR_ESQUEMA"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     		bcsConfiguracionMaestroDeclaraciones.ingresarCerrarVentana(id, $scope.winID);
	     	 }else{
	     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ACTIVAR_ESQUEMA"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     	 }
		},function(respuesta){
   			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ACTIVAR_ESQUEMA"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
  		 });
	}
	
	$scope.limpiar = function()
	{
		$scope.formData.version = undefined;
		$scope.formData.fechaLimitePresentacion = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.formData.observacion	= undefined;
	};
	
	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,40);
    }
	
	$scope.$watch('formData.versionSeleccionado',function(newval,oldval){
		if(newval && newval != oldval){
			$scope.formData.fechaLimitePresentacion = util.dateInicioDia(newval.FechaPresentacionLimite);
		}
	})
	
	$scope.limpiar();
	
	$scope.ajustaVentana();
	
})