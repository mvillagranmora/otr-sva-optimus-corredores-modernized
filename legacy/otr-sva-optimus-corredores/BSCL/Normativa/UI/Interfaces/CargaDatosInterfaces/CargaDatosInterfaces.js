angular.module('ModuloNormativa').directive('fileReaderInterfaz', function() {
	return {
		//restrict: 'A',
		scope: {
			ngAccept:"=",
			callback:"=?"
		},
		link: function(scope, element) {
			$(element).on('change', function(changeEvent) {
				var files = changeEvent.target.files;
				if (files.length) {
					var r = new FileReader();
					r.onload = function(e) {
						var contents = e.target.result;
						var archivo = {
								contenido : contents,
								nombre : files[0].name
						}
						scope.callback(archivo);
					};
					r.readAsText(files[0],'utf-8');
				}
			});
			
			if(scope.ngAccept) $(element).attr('accept',scope.ngAccept);
			
		}
	};
})

angular.module('ModuloNormativa').controller('CargaDatosInterfacesCtrl',function($scope,$translate,bcsComunes,bcsServicioInterfaces,$timeout){
	
	$scope.formData = {
			archivo: undefined
	};
	
	
	$scope.configArchivo = {
			formatoArchivoDefecto : "txt"
	}
	
	$scope.winID=bcsComunes.ventanaWinId($scope);
	
	$scope.formInfo = {
			fechaSolicitud : util.dateInicioDia($scope.SesionFechaSistema),
			usuario : $scope.SesionUsuario
	};
	
	$scope.oConfiguracionInterfaz = {
		filtros:{
			TipoInterfaz: {field:"TipoInterfaz", operator:"eq",value:"CARGA"},
			IdEstado: {field:"IdEstado", operator:"eq",value:"ACT"}
		}
	};
	
	$scope.formInfo.cargarInterfaz = function(){
		bcsComunes.ventanaOverlay($scope.winID,true);
		var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo  : 'NOR',
				p_TipoPrograma : [{
					IdInterno : 0,
					IdTipoPrograma : $scope.formData.Interfazseleccionado.IdTipoPrograma,
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
				}]
		}
		bcsServicioInterfaces.InscribirProcesoBatch(datos).$promise.then(
		function(result) {
			bcsComunes.ventanaOverlay($scope.winID.ventanaId,false);
			var fechaLocal=util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion,"FechaHora","CALL");
			bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO').replace("::IdPrograma",result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion",util.formatoSecondate(fechaLocal)),
				conflictos : util.parseServerResponse(result)}, $scope.winID);

		},
		function(result){
			bcsComunes.ventanaOverlay($scope.winID,false);
			bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
		});
	};
	
	$scope.formInfo.cargarArchivo = function(archivo){
		bcsComunes.ventanaOverlay($scope.winID,true);
		$scope.formData.archivo = archivo;
		var fileData = $scope.formData.archivo.contenido;
		$scope.formData.fileDataEncoded = btoa(fileData);
		bcsComunes.ventanaOverlay($scope.winID,false);
	}
	
	$scope.formInfo.limpiarFiltros = function()
	{
		$scope.formData.idInterfaz = false;
	};
	
})