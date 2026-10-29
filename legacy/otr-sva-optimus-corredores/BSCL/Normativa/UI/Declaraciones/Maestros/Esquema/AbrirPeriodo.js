angular.module('ModuloNormativa').controller('AbrirPeriodoEsquemaDeclaracionCtrl',function($scope,$translate,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,bcsConfiguracionMaestroDeclaraciones,bcsServicioMaestroDeclaraciones,$timeout){
	
	$scope.formData = {};
	
	$scope.winID=bcsComunes.ventanaWinId($scope);
	
	$scope.ingresarPeriodo = function(){
		bcsComunes.ventanaOverlay($scope.winID,true);
		var datosPeriodo = {
				 p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
				 p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
				 p_IdModulo : 'NOR',
				 p_simularOperacion : 'N',
				 p_UsaReglasNegocio : 'S',
				 
				 p_Version : $scope.formData.periodo
		}
		bcsServicioMaestroDeclaraciones.ActivarPeriodo(datosPeriodo).$promise.then(function(respuesta){
			if (respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		var id = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_INGRESAR_PERIODO"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     		bcsConfiguracionMaestroDeclaraciones.ingresarCerrarVentana(id, $scope.winID);
	     	 }else{
	     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_INGRESAR_PERIODO"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     	 }
		},function(respuesta){
   			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_INGRESAR_PERIODO"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
  		 });
	}
	
	$scope.limpiar = function()
	{
		$scope.formData.periodo = undefined;
	};
	
	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,40);
    }
	
	$scope.limpiar();
	
	$scope.ajustaVentana();
	
})