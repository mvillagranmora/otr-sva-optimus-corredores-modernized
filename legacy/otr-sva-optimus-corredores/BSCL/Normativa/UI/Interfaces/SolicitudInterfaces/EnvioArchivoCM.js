angular.module('ModuloNormativa').controller('EnvioArchivoCMCtrl',function($scope,$translate,bcsComunes,bcsServicioInterfaces,$timeout,$http){
		
	$scope.winID=bcsComunes.ventanaWinId($scope);
	
	$scope.filtros = {
			fechaDesde : util.dateInicioDia($scope.SesionFechaSistema)
	};
	
	$scope.data ={};	
		
	var parametrosConsulta = {};	
	var conParametros = false;
	
	$scope.data.oConfiguracionTipoOperacion={
			tipo : "combobox-parametro-mesa",
			configuracion : {},
			filtros : { 
						IdEstado : { "field"   : "IdEstado", "operator": "eq", "value" : "VIG" },
						TipoParametro : { "field": "NombreTipoParametro", "operator": "eq", "value" : "CUS_OPPEND_Operacion" }
					  },
			idActual : false,
			seleccionado : false,
			idDefecto : '' 
	};

	$scope.data.oConfiguracionTipoComprobante={
			tipo : "combobox-parametro-mesa",
			configuracion : {},
			filtros : { 
						IdEstado : { "field"   : "IdEstado", "operator": "eq", "value" : "VIG" },
						TipoParametro : { "field": "NombreTipoParametro", "operator": "eq", "value" : "CUS_OPPEND_Comprobante" }
					  },
			idActual : false,
			seleccionado : false,
			idDefecto : '',
			autoCargar: true
	};

	$scope.data.oConfiguracionTipoCuenta={
			tipo : "combobox-parametro-mesa",
			configuracion : {},
			filtros : { 
						IdEstado : { "field"   : "IdEstado", "operator": "eq", "value" : "VIG" },
						TipoParametro : { "field": "NombreTipoParametro", "operator": "eq", "value" : "CUS_OPPEND_Cuenta" }
					  },
			idActual : false,
			seleccionado : false,
			idDefecto : '' 
	};	

	$scope.data.oConfiguracionMercado={
			tipo : "combobox-parametro-mesa",
			configuracion : {},
			filtros : { 
						IdEstado : { "field"   : "IdEstado", "operator": "eq", "value" : "VIG" },
						TipoParametro : { "field": "NombreTipoParametro", "operator": "eq", "value" : "CUS_OPPEND_Mercado" }
					  },
			idActual : false,
			seleccionado : false,
			idDefecto : '' 
	};		

	
	$scope.data.oConfiguracionEstadoMovimiento={
		tipo : "combobox-parametro-mesa",
		configuracion : {},
		filtros : { 
			IdEstado : { "field"   : "IdEstado", "operator": "eq", "value" : "VIG" },
			TipoParametro : { "field": "NombreTipoParametro", "operator": "eq", "value" : "CUS_OPPEND_Estado" }
		  },
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	var validarFiltros = function(filtros)
	{
		var filters = new Array();
		//filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa});
		if (filtros.fechaDesde)
			filters.push({ field: "FechaMovimientoComprobante", operator: "gte", value: util.dateSPdate(filtros.fechaDesde)});
		if (filtros.fechaHasta)
			filters.push({ field: "FechaMovimientoComprobante", operator: "lte", value: util.dateSPdate(util.dateTerminoDia(filtros.fechaDesde))});
		return filters;
	}
	
	var configuracionColumnas = function() {
		return { 
			FechaMovimientoComprobante:{field: "FechaMovimientoComprobante",title: $translate.instant('UI_FECHA'),width: "100px",	align: "left"},
			NombreProducto:{field: "NombreProducto",title: $translate.instant('UI_NOMBRE_PRODUCTO'),width: "100px",	align: "left"},
			NombreConcepto:{field: "NombreConcepto",title: $translate.instant('UI_NOMBRE_CONCEPTO'),width: "100px",	align: "left"},
			ModalidadOperacion:{field: "ModalidadOperacion",title: $translate.instant('UI_MODALIDAD'),width: "100px",	align: "left"},
			NumeroOperacion:{field: "NumeroOperacion",title: $translate.instant('UI_FOLIO'),width: "100px",	align: "left"},
			CorrelativoBVL:{field: "CorrelativoBVL",title: $translate.instant('UI_FOLIO_CAVALI'),width: "100px",	align: "left"},
			TipoDetalle:{field: "TipoDetalle",title: $translate.instant('UI_TIPO_DETALLE'),width: "100px",	align: "left"},
			CodigoTitular:{field: "CodigoTitular",title: $translate.instant('UI_CODIGO_TITULAR'),width: "100px",	align: "left"},
			CantidadCavali:{field: "CantidadCavali",title: $translate.instant('UI_CANTIDAD_CAVALI'),width: "100px",	align: "left"},
			CantidadFisica:{field: "CantidadFisica",title: $translate.instant('UI_CANTIDAD_FISICA'),width: "100px",	align: "left"},
			Giro:{field: "Giro",title: $translate.instant('UI_GIRO'),width: "100px",	align: "left"},
			FlagTrabajador:{field: "FlagTrabajador",title: $translate.instant('UI_FLAG_TRABAJADOR'),width: "100px",	align: "left"},
			ComisionContado:{field: "ComisionContado",title: $translate.instant('UI_COMISION_CONTADO'),width: "100px",	align: "left"},
			ComisionPlazo:{field: "ComisionPlazo",title: $translate.instant('UI_COMISION_PLAZO'),width: "100px",	align: "left"},
			FlagPoliza:{field: "FlagPoliza",title: $translate.instant('UI_FLAG_POLIZA'),width: "100px",	align: "left"},
			FlagContado:{field: "FlagContado",title: $translate.instant('UI_FLAG_CONTADO'),width: "100px",	align: "left"},
			FlagPlazo:{field: "FlagPlazo",title: $translate.instant('UI_FLAG_PLAZO'),width: "100px",	align: "left"},
			CodigoCustodioValores:{field: "CodigoCustodioValores",title: $translate.instant('UI_CODIGO_CUSTODIO_VALORES'),width: "100px",	align: "left"},
			CodigoCustodioFondo:{field: "CodigoCustodioFondo",title: $translate.instant('UI_CODIGO_CUSTODIO_FONDO'),width: "100px",	align: "left"},
			InteresContado:{field: "InteresContado",title: $translate.instant('UI_INTERES_CONTADO'),width: "100px",	align: "left"},
			InteresPlazo:{field: "InteresPlazo",title: $translate.instant('UI_INTERES_PLAZO'),width: "100px",	align: "left"},
			LiquidacionDTC:{field: "LiquidacionDTC",title: $translate.instant('UI_LIQUIDACION_DTC'),width: "100px",	align: "left"},
			FlagDayTrade:{field: "FlagDayTrade",title: $translate.instant('UI_FLAG_DAY_TRADE'),width: "100px",	align: "left"},
			IndicadorCartaFianza:{field: "IndicadorCartaFianza",title: $translate.instant('UI_INDICADOR_CARTA_FIANZA'),width: "100px",	align: "left"},
			MontoCartaFianza:{field: "MontoCartaFianza",title: $translate.instant('UI_MONTO_CARTA_FIANZA'),width: "100px",	align: "left"},
			CodigoBanco:{field: "CodigoBanco",title: $translate.instant('UI_CODIGO_BANCO'),width: "100px",	align: "left"}
			
		}
	}
	
	getConfiguracionColumna = function(campo,extras) {
		if (!configuracionColumnas()[campo]) console.log("FALTA COLUMNA " + campo);
		return _.extend( {}, configuracionColumnas()[campo], extras);
	};
	
	var getSchemaConsultaCM = function(){
		return {
			model : {
				fields : {
					FechaMovimientoComprobante		: { type : "sysdate"},
					ModalidadOperacion				: { type : "number"},
					NumeroOperacion					: {	type : "number"},
					CorrelativoBVL					: {	type : "number"},
					CodigoParticipante 				: { type : "number"},
					TipoDetalle 					: { type : "number"},
					CodigoTitular 					: { type : "number"},
					CantidadCavali 					: { type : "number"},
					CantidadFisica 					: { type : "number"},
					Giro 							: { type : "number"},
					FlagTrabajador 					: { type : "number"},
					ComisionContado 				: { type : "number"},
					ComisionPlazo 					: { type : "number"},
					FlagPoliza 						: { type : "string"},
					FlagContado 					: { type : "string"},
					FlagPlazo 						: { type : "string"},
					CodigoCustodioValores 			: { type : "number"},
					CodigoCustodioFondo 			: { type : "number"},
					InteresContado 					: { type : "number"},
					InteresPlazo 					: { type : "number"},
					LiquidacionDTC 					: { type : "string"},
					FlagDayTrade 					: { type : "number"},
					FechaVencimientoDocumento 		: { type : "string"},
					IndicadorCartaFianza 			: { type : "number"},
					MontoCartaFianza 				: { type : "number"},
					CodigoBanco 					: { type : "number"},
					NombreProducto 					: { type : "string"},
					NombreConcepto 					: { type : "string"},
					FechaOperacion 					: { type : "string"}
				}
			}
		};
	}
	
	getDataSourceConsultaCM = function(){
		return new bcs.DataSource({ 
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				sort: { field: "NumeroOperacion", dir: "desc" },
				pageSize : 200,
				transport: {
					read: {
						url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=4215",
						dataType: "json"
					}
				},
				schema : getSchemaConsultaCM()
			},
			
		});
	}
	
	var getConsultaCM = function() {
		var config = {};
		config = {
				buscar : function(apiGrid,filtros) {
					apiGrid.dataSource.filter(validarFiltros(filtros));
				},
				options : {
					userSettings:true,
					sortable: true,
		      		reorderable: true,
		      		filterable: true,
					groupable: true,
					pageable: true,
					selectable: "multiple, row",
					reorderable: true,
					allowCopy: true,
					resizable: true,
		      		autoBind: false,
		      		scrollable: true,
		      		height:450,
			
					dataSource: getDataSourceConsultaCM(),
				
					columns: [
					           getConfiguracionColumna("FechaMovimientoComprobante",{ groupable: true})
					          ,getConfiguracionColumna("NombreConcepto",{ groupable: true})
					          ,getConfiguracionColumna("NombreProducto",{ groupable: true})
					          ,getConfiguracionColumna("ModalidadOperacion",{ groupable: true})
					          ,getConfiguracionColumna("NumeroOperacion",{ groupable: true})
					          ,getConfiguracionColumna("CorrelativoBVL",{ groupable: true})
					          ,getConfiguracionColumna("TipoDetalle",{ groupable: true})
					          ,getConfiguracionColumna("CodigoTitular",{ groupable: true}) 
					          ,getConfiguracionColumna("CantidadCavali",{ groupable: true})
					          ,getConfiguracionColumna("CantidadFisica",{ groupable: true})
					          ,getConfiguracionColumna("Giro",{ groupable: true})
					          ,getConfiguracionColumna("FlagTrabajador",{ groupable: true})
					          ,getConfiguracionColumna("ComisionContado",{ groupable: true})
					          ,getConfiguracionColumna("ComisionPlazo",{ groupable: true})
					          ,getConfiguracionColumna("FlagPoliza",{ groupable: true})
					          ,getConfiguracionColumna("FlagContado",{ groupable: true})
					          ,getConfiguracionColumna("CodigoCustodioValores",{ groupable: true})
					          ,getConfiguracionColumna("CodigoCustodioFondo",{ groupable: true}) 
					          ,getConfiguracionColumna("InteresContado",{ groupable: true})
					          ,getConfiguracionColumna("InteresPlazo",{ groupable: true})
					          ,getConfiguracionColumna("LiquidacionDTC",{ groupable: true})
					          ,getConfiguracionColumna("FlagDayTrade",{ groupable: true})
					          ,getConfiguracionColumna("IndicadorCartaFianza",{ groupable: true})
					          ,getConfiguracionColumna("MontoCartaFianza",{ groupable: true})
					          ,getConfiguracionColumna("CodigoBanco",{ groupable: true})
					]
				}
		};
		return config;
	};
	
	var conf = getConsultaCM();
	
	$scope.defaultOptions = conf.options;
	$scope.buscar = conf.buscar;
	$scope.winID=bcsComunes.ventanaWinId($scope);
			  	
  	$scope.generarCM = function(){
		bcsComunes.ventanaOverlay($scope.winID,true);
		
		var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo  : 'NOR',
				p_TipoPrograma : [{
					IdInterno	:	0,
					IdTipoPrograma : 'GenerarInterfazArchivoCM',
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
					Valor : util.dateSPseconddate($scope.filtros.fechaDesde)
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
	}
  	
  	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,40);
    }
	
	$scope.defaultOptions.gridCreated = function(grid){
		$scope.ajustaVentana();
	}
	
	
  	
	$scope.limpiarFiltros = function()
	{
		$scope.filtros = {
				fechaDesde : util.dateInicioDia($scope.SesionFechaSistema),
				fechaHasta : util.dateTerminoDia($scope.SesionFechaSistema),
				idInterfaz : undefined
		};
	};
		
	$scope.limpiarFiltros();
});