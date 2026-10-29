angular.module('ModuloOrdenes').controller('CargaConfiguracionContableCtrl',function($scope,$sanitize,$translate,bcsComunes,bcsServicioContabilidad)
{
	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	$scope.tipoCarga = {};
	$scope.formatoCarga = "";
	$scope.ejemploCarga = "";
	$scope.cargarArchivo = function() {
		var datosArchivo = {codigoArchivo: $scope.tipoCarga.seleccionado.Codigo, titulo: $scope.tipoCarga.seleccionado.Descripcion};
		var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,false,false,idVentana);
		bcsComunes.ventanaPromesa(id1).then(function(result){
			if(!result || !result.fileData){
				 bcsComunes.ventanaOverlay(idVentana,false);
				 return;
			}
			var datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo  : 'CON'
			}
			if($scope.tipoCarga.seleccionado.Codigo === 'CARGA_CABECERA_CMBTE_CONT'){
				datos.p_Comprobantes = result.fileData;
				bcsComunes.ventanaOverlay(idVentana,true);
				bcsServicioContabilidad.CargarCabeceraComprobante(datos).$promise.then(
				function(result) {
					bcsComunes.ventanaOverlay(idVentana,false);
					if(result){
						var id = bcsComunes.ventanaMsg({ mensaje : $translate.instant(result.P_OUT_MENSAJE),
							conflictos : util.parseServerResponse(result)}, idVentana);
					}

				},
				function(result){
					bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.ventanaMsg({ mensaje : $translate.instant("UI_ERROR"),
						conflictos : util.parseServerResponse(result)}, idVentana);
				});
			}
			if($scope.tipoCarga.seleccionado.Codigo === 'CARGA_DETERMINANTE_CONT'){
				datos.p_Determinantes = result.fileData;
				bcsComunes.ventanaOverlay(idVentana,true);
				bcsServicioContabilidad.CargarDeterminanteComprobante(datos).$promise.then(
				function(result) {
					bcsComunes.ventanaOverlay(idVentana,false);
					if(result){
						var id = bcsComunes.ventanaMsg({ mensaje : $translate.instant(result.P_OUT_MENSAJE),
							conflictos : util.parseServerResponse(result)}, idVentana);
					}

				},
				function(result){
					bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.ventanaMsg({ mensaje : $translate.instant("UI_ERROR"),
						conflictos : util.parseServerResponse(result)}, idVentana);
				});
			}
			if($scope.tipoCarga.seleccionado.Codigo === 'CARGA_ASIENTOS_CONTABLES'){
				datos.p_Asientos = result.fileData;
				bcsComunes.ventanaOverlay(idVentana,true);
				bcsServicioContabilidad.CargarAsientoComprobante(datos).$promise.then(
				function(result) {
					bcsComunes.ventanaOverlay(idVentana,false);
					if(result){
						var id = bcsComunes.ventanaMsg({ mensaje : $translate.instant(result.P_OUT_MENSAJE),
							conflictos : util.parseServerResponse(result)}, idVentana);
					}

				},
				function(result){
					bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.ventanaMsg({ mensaje : $translate.instant("UI_ERROR"),
						conflictos : util.parseServerResponse(result)}, idVentana);
				});
			}
			
		},function(result){
			bcsComunes.ventanaOverlay(idVentana,false);
			console.log(result);
		})
	}
	
	$scope.$watch('tipoCarga.seleccionado',function(newval,oldval){
		 if (newval) {
			 $scope.formatoCarga = $scope.tipoCarga.seleccionado.Formato;
			 $scope.ejemploCarga = $scope.tipoCarga.seleccionado.EjemploLinea;
			 $scope.ajustaVentana();
		 }
	 });
	
	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaAutoHeight(idVentana);
    }
});


