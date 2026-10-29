(function() {
	angular.module('ModuloPlataforma').controller('InformeSaldosCuentaCambioCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosMesa,$http,$timeout){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		let InfSaldosCuenta        	= this;
		InfSaldosCuenta.idVentana		= bcsComunes.ventanaWinId($scope);
		InfSaldosCuenta.cmbbx			= {};
		InfSaldosCuenta.filtro		= {};
		InfSaldosCuenta.fn			= {};
		InfSaldosCuenta.data 			= {};
		InfSaldosCuenta.config 		= {};
		InfSaldosCuenta.ChkTXT		= true;
		InfSaldosCuenta.haBoton 		= true;
		InfSaldosCuenta.config.agnoMes = {format: 'MMMM yyyy', start: 'year',depth: 'year'};
		InfSaldosCuenta.data.agnoMes = util.dateInicioDia($scope.SesionFechaSistema);
		
		/*********************************************
		 * WATCHES
		 *********************************************/

		$scope.$watch('InfSaldosCuenta.ChkTXT',function(newval,oldval){
			if(InfSaldosCuenta.ChkTXT)
				InfSaldosCuenta.ChkCSV = false;
			else
				InfSaldosCuenta.ChkCSV = true;
				
		});
		
		$scope.$watch('InfSaldosCuenta.ChkCSV',function(newval,oldval){
			if(InfSaldosCuenta.ChkCSV)
				InfSaldosCuenta.ChkTXT = false;
			else
				InfSaldosCuenta.ChkTXT = true;
		});
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		 InfSaldosCuenta.fn.generarArchivo = function(){
			let periodo = util.dateSPdate(InfSaldosCuenta.data.agnoMes);
			let formatoArchivo = InfSaldosCuenta.ChkCSV ? 'CSV': 'TXT';
		
			bcsComunes.ventanaOverlay($scope.winID,true);
			let datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo  : 'MSP',
					p_TipoPrograma : [{
						IdInterno : 0,
						IdTipoPrograma : 'GENERAR_ARCHIVO_INF_SALD', 
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