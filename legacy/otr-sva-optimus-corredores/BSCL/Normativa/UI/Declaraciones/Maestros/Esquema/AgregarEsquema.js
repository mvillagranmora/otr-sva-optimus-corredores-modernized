angular.module('ModuloNormativa').controller('AgregarEsquemaDeclaracionCtrl',function($scope,$translate,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,bcsConfiguracionMaestroDeclaraciones,bcsServicioMaestroDeclaraciones,$timeout){
	
	$scope.formData = {};
	$scope.datos = {};
	$scope.winID=bcsComunes.ventanaWinId($scope);
	
	$scope.ingresarEsquema = function(){
		bcsComunes.ventanaOverlay($scope.winID,true);
		var datosIngresarEsquema = {
				 p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
				 p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
				 p_IdModulo : 'NOR',
				 p_simularOperacion : 'N',
				 p_UsaReglasNegocio : 'S',
				 
				 p_Versiones : [{
					    IdInterno		      		:	0,
					    Version 	          		:	$scope.formData.periodo,
					    FechaPresentacionLimite     :	util.dateSPdate($scope.formData.fechaLimitePresentacion)
				 }]
		}
		bcsServicioMaestroDeclaraciones.ConfigurarVersion(datosIngresarEsquema).$promise.then(function(respuesta){
			if (respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		var id = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_AGREGAR_ESQUEMA"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     		bcsConfiguracionMaestroDeclaraciones.ingresarCerrarVentana(id, $scope.winID);
	     	 }else{
	     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_AGREGAR_ESQUEMA"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     	 }
		},function(respuesta){
   			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_AGREGAR_ESQUEMA"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
  		 });
	}
	
	$scope.limpiar = function()
	{
		$scope.formData.periodo = undefined;
		$scope.formData.fechaLimitePresentacion = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.formData.observacion	= undefined;
	};
	
	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,40);
    }
	
	$scope.limpiar();
	
	$scope.ajustaVentana();
	
})