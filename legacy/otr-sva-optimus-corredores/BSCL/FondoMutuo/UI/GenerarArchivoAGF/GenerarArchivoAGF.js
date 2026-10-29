angular.module('ModuloFFM').controller('GenerarArchivoAGFFFMCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioReportes,$timeout){
	$scope.formData = {};
	$scope.winID=bcsComunes.ventanaWinId($scope);
	var overlay = bcsComunes.overlay;
	
	$scope.limpiarFiltros = function()
	{
		$scope.formData.fechaDesde = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.formData.fechaHasta = util.dateTerminoDia($scope.SesionFechaSistema);
		
	};
	
	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,40);
    }
	
	$scope.generarArchivo = function(){
		bcsComunes.ventanaOverlay($scope.winID,true);
		
		var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo  : 'FMM',
				p_TipoPrograma : [{
					IdInterno	:	0,
					IdTipoPrograma : 'GENERARARCHIVOOPERACIONESAGF',
					SegundosDesfase	:	0
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
					NombreParametro : 'FechaDesde',
					Valor : util.dateSPseconddate($scope.formData.fechaDesde)
				},{
					IdInterno : 0,
					NombreParametro : 'FechaHasta',
					Valor : util.dateSPseconddate($scope.formData.fechaHasta)
				},{
					IdInterno : 0,
					NombreParametro : 'IdAmbiente',
					Valor : $scope.formData.oIndicador.seleccionado.CodigoTipoIndicador
				}]
		}
		
		bcsServicioFondosMutuos.InscribirProcesoBatch(datos).$promise.then(
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
	}
	
	$scope.limpiarFiltros();
});