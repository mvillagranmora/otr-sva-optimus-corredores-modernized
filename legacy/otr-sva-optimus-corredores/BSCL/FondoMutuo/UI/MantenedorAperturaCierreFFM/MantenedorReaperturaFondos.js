angular.module('ModuloFFM').controller('MantenedorReaperturaFondosCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioReportes,$timeout){
	$scope.formData = {};
	$scope.winID=bcsComunes.ventanaWinId($scope);
	var idVentana = bcsComunes.ventanaWinId($scope);
	var parametros = bcsComunes.ventanaParams(idVentana);
	$scope.formData.IdFondo = parametros.IdFondo; 
	$scope.formData.fechaFondo = parametros.FechaFondo;
	$scope.formData.reabrirFondo = function(){
		bcsComunes.ventanaOverlay($scope.winID,true);
		var dataReabrirFondo = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo : 'FMM',
				p_Fondo : [
				                {
				                	IdInterno	 : 0,
				                	IdFondo : $scope.formData.IdFondo,
				                	FechaFondo	 : util.dateSPdate($scope.formData.fechaFondo)
				                }
				               ]
		}
		bcsServicioFondosMutuos.ReaperturaFondos(dataReabrirFondo).$promise.then(function(respuesta){
			if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		var id = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_REABRIR_FONDO_EXITOSO"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     		bcsConfiguracionFFM.ingresarCerrarVentana(id, $scope.winID);
	     	 }else{
	     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_REABRIR_FONDO"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     	 }
		},function(respuesta){
  			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_REABRIR_FONDO"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
  			console.log('me cai jeje ' + respuesta);
  		})
	}
	
});