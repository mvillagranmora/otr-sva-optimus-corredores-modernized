angular.module('ModuloOrdenes').controller('CargaArchivosTransaccionesOrdenesCtrl',function($scope,$sanitize,$translate,bcsComunes,bcsServicioOrden,bcsConfiguracionOrdenes,bcsConsultasOrdenes)
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
			var contenidoArchivo = {p_contenido : result.fileData};
			var datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo  : 'ORD',
					p_TipoPrograma : [{
						IdInterno : 0,
						IdTipoPrograma : "CARGAR_ARCHIVO_TRANS_ORD",
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
						Valor : 'ORD'
					},{
						IdInterno : 0,
						NombreParametro : 'TipoArchivo',
						Valor : $scope.tipoCarga.seleccionado.Codigo
					},{
						IdInterno : 0,
						NombreParametro : 'ContenidoArchivo',
						ValorBlob : btoa(JSON.stringify(contenidoArchivo))
					},{
						IdInterno : 0,
						NombreParametro : 'NombreArchivo',
						Valor : result.fileName
					}]
			}
			bcsServicioOrden.InscribirProcesoBatch(datos).$promise.then(
			function(result) {
				bcsComunes.ventanaOverlay(idVentana,false);
				var fechaLocal=util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion,"FechaHora","CALL");
				bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO').replace("::IdPrograma",result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion",util.formatoSecondate(fechaLocal)),
					conflictos : util.parseServerResponse(result)}, idVentana);

			},
			function(result){
				bcsComunes.ventanaOverlay(idVentana,false);
				bcsComunes.msg("error","Error",$translate.instant('UI_CARGAR_ARCHIVO_ERROR'));
			});
			
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
		 bcsComunes.ventanaTamanoAutomatico(idVentana,15);
    }
});


