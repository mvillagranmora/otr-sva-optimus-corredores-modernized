angular.module('ModuloNormativa').controller('GenerarInterfacesCtrl',function($scope,$translate,bcsComunes,bcsServicioInterfaces,$timeout){
	
	$scope.formData = {};
	$scope.data = {};
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.HabilitoCombo = true;
	
	$scope.data.oConfiguracionInterfaz={
			tipo 			: "combobox-generarInterfaz",
			configuracion : {},
			idActual 		: false,
			seleccionado 	: false,
			idDefecto 		: '',
			idPostFiltro 	: false
		};
	$scope.data.oConfiguracionSubInterfaz={
			tipo 			: "combobox-generarTipoInterfaz",			
			configuracion 	: {},
			filtros 		: {
				IdInterfaz 	: { "field" : "Padre", "operator": "eq", "value": -1}			
				},
			idActual 		: false,
			seleccionado 	: false,
			idDefecto 		: '',
			idPostFiltro 	: false
		};
	
	$scope.fecha = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
	
	
	
	$scope.cargarInterfazPrueba = function(){
		
		let interfaz;
		if($scope.formData.subInterfaz!=""){
		interfaz = $scope.formData.subInterfaz
		}else
			{interfaz = $scope.formData.interfaz}
		
		bcsComunes.ventanaOverlay($scope.winID,true);
		var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo  : 'NOR',
				p_TipoPrograma : [{
					IdInterno : 0,
					IdTipoPrograma : interfaz,
					SegundosDesfase : 0
				}],
				p_Parametro : [{
					IdInterno : 0,
					NombreParametro : 'IdEmpresa',
					Valor : $scope.SesionIdEmpresa
				},{
					IdInterno : 0,
					NombreParametro : 'IdUsuario',
					Valor : $scope.SesionIdUsuario
				},{
					IdInterno : 0,
					NombreParametro : 'IdModulo',
					Valor : 'NOR'
				},{
					IdInterno : 0,
					NombreParametro : 'Archivo',
					ValorBlob : $scope.formData.fileDataEncoded
				},{
					IdInterno : 0,
					NombreParametro : 'FechaUI',
					Valor : $scope.fecha
				}]
		}
		bcsServicioInterfaces.InscribirProcesoBatch(datos).$promise.then(
		function(result) {
			if(result.P_OUT_ERRORESCONTADOR>0){
				bcsComunes.ventanaOverlay($scope.winID,false);
				bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
			}else{
				bcsComunes.ventanaOverlay($scope.winID.ventanaId,false);
				var fechaLocal=util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion,"FechaHora","CALL");
				bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO').replace("::IdPrograma",result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion",util.formatoSecondate(fechaLocal)),
					conflictos : util.parseServerResponse(result)}, $scope.winID);
			}
		},
		function(result){
			
		});
	};
	

})