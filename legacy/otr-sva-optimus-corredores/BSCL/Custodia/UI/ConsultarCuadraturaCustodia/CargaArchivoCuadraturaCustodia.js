angular.module('ModuloCustodia').directive('fileReaderCargaArchivoCustodia', function() {
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

angular.module('ModuloCustodia').controller('CargaArchivoCuadraturaCustodiaCtrl',function($scope,$translate,bcsComunes,bcsServicioPlataforma,$timeout){
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	$scope.idVentanaCargaArchivo = idVentana;
	
	bcsComunes.ventanaTitulo(idVentana,  (parametros.titulo!=undefined) ?parametros.titulo:bcsComunes.ventanaGetTitulo(idVentana));

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
	
	$scope.formInfo.cargarArchivoCuadraturaCustodia = function(){
		bcsComunes.ventanaOverlay($scope.winID,true);
		var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo  : 'CUS',
				p_TipoPrograma : [{
					IdInterno : 0,
					IdTipoPrograma : 'CARGAR_ARCHIVO_CUSTODIA_CUADRATURA',
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
					Valor : 'CUS'
				},{
					IdInterno : 0,
					NombreParametro : 'TipoArchivo',
					Valor : parametros.codigoArchivo
				},{
					IdInterno : 0,
					NombreParametro : 'Archivo',
					ValorBlob : $scope.formData.fileDataEncoded
				}]
		}
		bcsServicioPlataforma.Servicios().InsertarProcesoBatch(datos).$promise.then(
		function(result) {
			bcsComunes.ventanaOverlay(idVentana,false);
			if(result.P_OUT_ERRORESCONTADOR === 0){
				var fechaLocal=util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion,"FechaHora","CALL");
				var id = bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO').replace("::IdPrograma",result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion",util.formatoSecondate(fechaLocal)),
					conflictos : util.parseServerResponse(result)}, $scope.winID);
				var promesa=bcsComunes.ventanaPromesa(id);                               
			     $.when(promesa).then(function(data){
			    	 $timeout(function() {
			    		 bcsComunes.ventanaCerrar(idVentana);
			    	 },20);
			     });
			}else{
				bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
			}
			

		},
		function(result){
			bcsComunes.ventanaOverlay(idVentana,false);
			bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
		});
	};
	
	$scope.formInfo.cargarArchivo = function(archivo){
		bcsComunes.ventanaOverlay(idVentana,true);
		$scope.formData.archivo = archivo;
		var fileData = $scope.formData.archivo.contenido;
		$scope.formData.fileDataEncoded = btoa(unescape(encodeURIComponent(fileData)));
		bcsComunes.ventanaOverlay(idVentana,false);
	} 
	
	
})