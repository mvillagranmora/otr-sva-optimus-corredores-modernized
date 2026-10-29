(function() {
angular.module('ModuloFFM').controller('GenerarCertificado31Ctrl',function($rootScope,$scope,$translate,bcsComunes,bcsServicioInterfaces,$timeout){
		
	
	$scope.configData = {};
	$scope.formData = {};
	$scope.formData.valorDefecto = 2931;
	var idVentana = bcsComunes.ventanaWinId($scope);
	$scope.parametros = bcsComunes.ventanaParams(idVentana);

	$scope.configData.habilitarSolicitar = false;

	
	$scope.configData.filtroCiudad = {
		IdPais : {field: "IdPais", operator: "eq", value: 44}//CIUDADES DE CHILE
	};
	
	$scope.cancelar = function(){
		bcsComunes.ventanaCerrar(idVentana);
	}
	
	
	$scope.cargarCiudadFecha = function(){
		
		const ciudadNombre = $scope.formData.ciudadSeleccionado.NombreCiudad;
		const fecha = new Date($scope.formData.fecha);
		
		console.log(ciudadNombre);
		console.log(fecha);
		
		if(fecha instanceof Date && !isNaN(fecha) && typeof(ciudadNombre) != "undefined"){
			
			const fechaFormato = fecha.getDate()+"-"+(fecha.getMonth()+1)+"-"+fecha.getFullYear();
		
			let listaTraspaso = $scope.parametros.datoLista;
			const datos = {
						p_IdEmpresa: $scope.SesionIdEmpresa,
						p_IdUsuario: $scope.SesionIdUsuario,
						p_IdModulo: 'FFM',
						p_TipoPrograma: [{
							IdInterno: 0,
							IdTipoPrograma: "GenerarCertificado31FFMM",
							SegundosDesfase: 0
						}],
						p_Parametro: [{
								IdInterno: 0,
								NombreParametro: 'IdEmpresa',
								Valor: $scope.SesionIdEmpresa
							}, {
								IdInterno: 0,
								NombreParametro: 'IdUsuario',
								Valor: $scope.SesionIdUsuario
							}, {
								IdInterno: 0,
								NombreParametro: 'IdModulo',
								Valor: 'FMM'
							},{
								IdInterno: 0,
								NombreParametro: 'EsMasivo',
								Valor: 'S'
							},{
								IdInterno: 0,
								NombreParametro: 'FechaCertificado',
								Valor: fechaFormato
							},{
								IdInterno: 0,
								NombreParametro: 'CiudadCertificado',
								Valor: ciudadNombre
							},{
								IdInterno: 0,
								NombreParametro: 'ListaTraspasos',
								Valor: listaTraspaso
							}]
						}
					bcsServicioInterfaces.InscribirProcesoBatch(datos).$promise.then(
					function (result) {
						bcsComunes.ventanaOverlay(idVentana, false);
						var fechaLocal = util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion, "FechaHora", "CALL");
						bcsComunes.ventanaMsg({
							mensaje: $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO').replace("::IdPrograma", result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion", util.formatoSecondate(fechaLocal)),
							conflictos: util.parseServerResponse(result)
						}, $scope.winID);
	
					},
					function (result) {
						bcsComunes.ventanaOverlay(pct.idVentana, false);
						bcsComunes.msg("error", "Error", $translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
				});
				bcsComunes.ventanaCerrar(idVentana);
		}
		
	};

	});
	
}());
		
