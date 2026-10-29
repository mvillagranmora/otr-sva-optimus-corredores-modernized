(function() {
	angular.module('ModuloPlataforma').controller('ReporteMensualOperacionesCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosMesa){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		let RptMenOpe        	= this;
		RptMenOpe.idVentana		= bcsComunes.ventanaWinId($scope);
		RptMenOpe.fn			= {};
		RptMenOpe.data 			= {};
		RptMenOpe.config 		= {};
		RptMenOpe.ChkTXT		= true;
		RptMenOpe.config.agnoMes = {format: 'MMMM yyyy', start: 'year',depth: 'year'};
		RptMenOpe.data.agnoMes = util.dateInicioDia($scope.SesionFechaSistema);
		
		/*********************************************
		 * WATCHES
		 *********************************************/

		$scope.$watch('RptMenOpe.ChkTXT',function(){
			RptMenOpe.ChkCSV = RptMenOpe.ChkTXT ? false: true;	
		});
		
		$scope.$watch('RptMenOpe.ChkCSV',function(){
			RptMenOpe.ChkTXT = RptMenOpe.ChkCSV ? false: true;	
		});
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		RptMenOpe.fn.generarArchivo = function(){
			let periodo = util.dateSPdate(RptMenOpe.data.agnoMes);
			let formatoArchivo = RptMenOpe.ChkCSV ? 'CSV': 'TXT';
		
			bcsComunes.ventanaOverlay($scope.winID,true);
			let datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo  : 'MSP',
					p_TipoPrograma : [{
						IdInterno : 0,
						IdTipoPrograma : 'GENERAR_ARCHIVO_OPE_MEN', 
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
						Valor : 'MSP'
					},{
						IdInterno : 0,
						NombreParametro : 'Periodo',
						Valor : periodo
					},{
						IdInterno : 0,
						NombreParametro : 'FormatoArchivo',
						Valor : formatoArchivo
					}]
			}
			
			bcsServiciosMesa.GenerarProcesoBatch(datos).$promise.then(function(result) {
				if(result.P_OUT_ERRORESCONTADOR>0){
					bcsComunes.ventanaOverlay($scope.winID,false);
					bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
				}else{
					bcsComunes.ventanaOverlay($scope.winID,false);
					var fechaLocal=util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion,"FechaHora","CALL");
					bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO')
											.replace("::IdPrograma",result.P_OUT_PROGRAMA[0].IdPrograma)
											.replace("::FechaEjecucion",util.formatoSecondate(fechaLocal)),
											conflictos : util.parseServerResponse(result)}, $scope.winID);
				}
			},
			function(){
				bcsComunes.ventanaOverlay($scope.winID,false);
				bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
			});
		};
	});
}());