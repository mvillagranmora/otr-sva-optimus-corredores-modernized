angular.module('ModuloFacturacion').controller('GenerarInformeComprobanteComisionesDiariasRVCtrl',function($scope,bcsComunes,$translate,$resource){
	const fecha =new Date(util.dateInicioDia($scope.SesionFechaSistema))
	$scope.data = {};
	$scope.data.Fecha = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
	$scope.data.oClientePortafolio = {
		tipo : "combobox-cliente-portafolio",
		configuracion : {},
		idActual : false,
		seleccionado : false,
		filtros : {
			IdEstado : {
				"field" : "IdEmpresa",
				"operator" : "eq",
				"value" : $scope.SesionIdEmpresa
			},
			EsCliente : {
				"field" : "EsCliente",
				"operator" : "eq",
				"value" : 'UI_SI'
			}
		},
	};

	$scope.data.oConfiguracionClasificacionTipoInstrumento={
		tipo : "combobox-clasificacion-tipo-instrumento",
		configuracion : {},
		filtros : { 	
			IdEstado :	{field : "IdEstado", operator : "eq", value : "ACT"}
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
};
	
	/******************************************************/
	/*** BOTON GENERAR									***/
	/******************************************************/
				
	$scope.generar = function() {
		let fecha = new Date($scope.data.Fecha).toISOString().split('T')[0]; 
		
		let programa = 'GENERAR_INFORME_COMISIONES_DIARIAS';
		const datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo  : 'FAC',
				p_TipoPrograma : [{
					IdInterno : 0,
					IdTipoPrograma : programa,
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
					Valor : 'FAC'
				},{
					IdInterno : 0,
					NombreParametro : 'Archivo',
					ValorBlob : 'PruebaGenerarInformeCobroComisionesDiariasRV'
				},
				{
					IdInterno : 0,
					NombreParametro : 'IdCliente',
					ValorBlob :	$scope.data.oClientePortafolio.seleccionado.IdCliente || 0
				},
				{
					IdInterno : 0,
					NombreParametro : 'IdPortafolio',
					ValorBlob :	$scope.data.oClientePortafolio.seleccionado.IdPortafolio || 0
				},
				{
					IdInterno : 0,
					NombreParametro : 'Fecha',
					ValorBlob :	fecha
				}]
		}
    		
		const r = $resource("",{} , {
				'Consultar' : { method : "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=20043"}
			});
			
		r.Consultar(datos).$promise.then(
		function(result) {
			if(result.P_OUT_ERRORESCONTADOR>0){
				bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
			}else{
				const fechaLocal = util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion,"FechaHora","CALL");
				bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO').replace("::IdPrograma",result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion",util.formatoSecondate(fechaLocal)),
					conflictos : util.parseServerResponse(result)}, $scope.winID);
			}
		},
		function(result){
			alert(result);
		});
		bcsComunes.msg("info", "info", 'Archivo generado correctamente');
		
	};
	
	$scope.limpiar = function() {
		$scope.data.IdCliente = 0;
		$scope.data.IdPortafolio = 0;
		$scope.filtros.IdPortafolio = false;
		$scope.data.Fecha = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
	
	}
	
});