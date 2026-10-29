angular.module('ModuloFFM').controller('IngresarHorarioCierreFondoCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioReportes,$timeout){
	$scope.formData = {};
	$scope.datosFondo = {};
	$scope.formData.Horarios = {};
	$scope.filtros = {};
	$scope.winID=bcsComunes.ventanaWinId($scope);
	var idVentana = bcsComunes.ventanaWinId($scope);
	var parametros = bcsComunes.ventanaParams(idVentana);
	$scope.formData.EsEdicion = parametros.EsEdicion;
	
	if($scope.formData.EsEdicion){
		$scope.IdFondoDefault = angular.copy(parametros.Fondo.IdFondo);
		$scope.IdFondo = parametros.Fondo.IdFondo;
		$scope.formData.Horarios = parametros.Horarios;
		$scope.formData.Horarios.HoraCierreComercial
		$scope.formData.Horarios.EsHabilitado = parametros.Horarios.EsHabilitado.toUpperCase() === 'UI_SI' ? true : false;
	}
	
	$scope.formData.filtrosEmisor = {
			IndicadorAGF : { "field"   : "IndicadorAGF","operator": "eq", "value"   :  "S"}
	};
	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,7);
    }
	$scope.ajustaVentana();
	
	$scope.formData.crearHorariosInstrumento = function()
    {	 
		 bcsComunes.ventanaOverlay($scope.winID,true);
		 var datosModificarHorarioCierreInstrumento = {
				 p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
				 p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
				 p_IdModulo : 'FMM',
				 p_UsaReglasNegocio : 'N',
				 
				 p_Fondo : [{
					    IdInterno		     :	0,
					    IdFondo 	     	 :	$scope.IdFondo,
					    HoraCierreComercial  :	$scope.formData.Horarios.HoraCierreComercial ? $scope.formData.Horarios.HoraCierreComercial : '',
					    HoraCierreBackOffice :	$scope.formData.Horarios.HoraCierreBackOffice ? $scope.formData.Horarios.HoraCierreBackOffice : '',
					    UsaHorarioDefault    :	$scope.formData.EsEdicion && !$scope.formData.Horarios.EsHabilitado ? 'S' : 'N',
					    TipoSolicitud        :  parametros.TipoMovimiento,
				 }]
		 }
	     bcsServicioFondosMutuos.ModificarHorarioCierreInstrumento(datosModificarHorarioCierreInstrumento).$promise.then(function(respuesta){
	     	 console.log(respuesta);
	     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		$scope.ajustaVentana();
	     		var id = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_MODIFICAR_HORARIO_CIERRE"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     		bcsConfiguracionFFM.ingresarCerrarVentana(id, $scope.winID);
	     	 }else{
	     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_MODIFICAR_HORARIO_CIERRE"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     	 }
  		 },function(respuesta){
  			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_MODIFICAR_HORARIO_CIERRE"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
  			console.log('me cai jeje ' + respuesta);
  		 });
    }
	
	 $scope.$watch('datosFondo',function(newval,oldval){
		 if (newval && newval.IdFondo) {
			 $scope.IdSerieFondo = newval.IdSerieFondo;
		 }
	 });
	
});