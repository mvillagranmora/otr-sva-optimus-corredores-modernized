angular.module('ModuloMesa').controller('CarteraConsolidadaCtl',
	function($scope,$state,$timeout,$modal,$translate,bcsComunes,bcsServiciosMesa,bcsConsultasMesa,bcsAlerta) {
	
	$scope.data = {};
	$scope.grid = {};
	var idVentana = bcsComunes.ventanaWinId($scope);
	$scope.data.oConf_ClientePortafolio={
			tipo : "combobox-cliente-portafolio",
			configuracion :{},
			idActual : "filtros.IdPortafolio",
			seleccionado : false
	};
	
	$scope.data.oConf_TipoCartera={
			tipo : "combobox-parametro-mesa",
			configuracion : {dataTextField: 'ValorNombreParametro'},
			filtros : { 
						IdEstado : { "field"   : "IdEstado", "operator": "eq", "value" : "VIG" },
						TipoParametro : { "field": "NombreTipoParametro", "operator": "eq", "value" : "CartCons_TipoCartera" }
					  },
			idActual : false,
			seleccionado : false,
			idDefecto : '',
			autoCargar: true 
	};	


	$scope.data.oConf_Cartera={
			tipo : "combobox-parametro-mesa",
			configuracion : {},
			filtros : { 
						IdEstado : { "field"   : "IdEstado", "operator": "eq", "value" : "VIG" },
						TipoParametro : { "field": "NombreTipoParametro", "operator": "eq", "value" : "CartCons_ClsfCartera" }
					  },
			idActual : false,
			seleccionado : false,
			idDefecto : '',
			autoCargar: true 
	};	

	
	$scope.$watch('data.oConf_Cartera.idActual',function(newval,oldval){
		if ($scope.data.oConf_Cartera.idActual === 'TERC'){
			$scope.data.EnabledClientePortafolio = true;
		} 
		else {
			$scope.data.EnabledClientePortafolio = false;
			$scope.data.oConf_ClientePortafolio.idActual = false; 
		}
	});
	
	

	var dsCarteraConsolidada = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering		: true, 
			serverPaging		: true, 
			serverSorting		: true, 
			pageSize 			: 200,
			
			sort: { field: "FechaOperacion", dir: "asc" },
			transport: {
				read: {
					url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1044",
					dataType: "json"
				},
			},
			
			schema : {
				model : {
					fields : {
								FolioOperacion: {type : "number"},
								TipoCartera: {type : "string"},
								FechaOperacion: {type : "sysdate"},
								RUTCliente: {type : "string"},
								RazonSocial: {type : "string"},
								RelacionCorredora: {type : "string"},
								FamiliaInstrumento: {type : "string"},
								Nemotecnico: {type : "string"},
								IdEmisor: {type : "string"},
								RutEmisor: {type : "string"},
								FechaVencimiento: {type : "sysdate"},
								FechaVencimientoPacto: {type : "sysdate"},
								DiasAlVencimiento: {type : "number"},
								TasaPacto: {type : "number"},
								MonedaPacto: {type : "string"},
								TipoTasa: {type : "string"},
								Nominal: {type : "number"},
								MonedaPapel: {type : "string"},
								TasaCompraOriginal: {type : "number"},
								ValorCompraEnPesos: {type : "number"},
								ValorCompraEnMoneda: {type : "number"},
								InteresAcumulado: {type : "number"},
								ReajusteAcumulado: {type : "number"},
								ValorPresente: {type : "number"},
								Interes: {type : "number"},
								Reajuste: {type : "number"},		
								ValorPresente_t1: {type : "number"},
								ValorAlVencimientoEnPesos: {type : "number"},
								ValorAlVencimientoEnMoneda: {type : "number"},	
								ClasificacionRiesgo: {type : "string"},
								TasaMercado: {type : "number"},
								ValorMercado: {type : "number"},
								MenorValorAlVencimiento: {type : "number"},
								MayorValorAlVencimiento: {type : "number"},	
								FechaTasaBCS: {type : "sysdate"},
								TasaMercadoBCS: {type : "number"},
								ValorMercadoBCS: {type : "number"},
								MenorValor: {type : "number"},
								MayorValor: {type : "number"},
								TipoPacto: {type : "string"},
								FechaProximoCupon: {type : "sysdate"},
								ProximoCuponNominal: {type : "string"}, 
								AmortizacionCupon: {type : "number"},
								InteresCupon: {type : "number"},
								NumeroCupon: {type : "number"},
								FrecuenciaCupon: {type : "string"},
								TieneCustodia_v1: {type : "string"},
								TipoBoveda: {type : "string"},
								Fungibilidad: {type : "string"},
								ClasificacionTablaIndice: {type : "string"},
								TieneGarantia: {type : "string"},
								TipoGarantia: {type : "string"},
								NemoSVS: {type : "string"},
								TasaTera: {type : "number"},
								InteresDevengado: {type : "number"},
								FolioCartera: {type : "number"},
								SecCliente: {type : "string"},
								TipoOperacion: {type : "string"},
								ValorLibro: {type : "number"},
								CodigoIFRS: {type : "string"},
								NombreMesa: {type : "string"}								
								
					}
				}
			}			
		}
	});  
	


    $scope.grid.confBasicaCarteraConsolidada = {
		dataSource  : dsCarteraConsolidada,
		columns     : [  {field : "FolioOperacion",  title:$translate.instant("UI_FOLIO_OPERACION"), width: "100px", align: "right"}
						,{field : "NombreMesa", title:$translate.instant("UI_NOMBRE_MESA"), width: "100px", align: "left"}
				        ,{field : "TipoCartera", title:$translate.instant("UI_TIPO_CARTERA"), width: "100px"}             
				        ,{field : "FechaOperacion", title: $translate.instant("UI_FECHA_OPERACION"), width: "100px",align: "left"}
				        ,{field : "RUTCliente", title: $translate.instant("UI_RUT_CLIENTE"), width: "100px", align: "right"}
				        ,{field : "RazonSocial", title: $translate.instant("UI_RAZON_SOCIAL_O_NOMBRE"), width: "110px"}
				        ,{field : "SecCliente", title:$translate.instant("UI_PORTAFOLIO"), width: "100px", align: "left"}
				        ,{field : "RelacionCorredora",  title:"UI_RELACION_CORREDORA",  width: "100px"}
				        ,{field : "FamiliaInstrumento", title:$translate.instant("UI_FAMILIA"), width: "100px"}
				        ,{field : "Nemotecnico", title: $translate.instant('UI_NEMOTECNICO'), width:"100px", align:"left"}      
				        ,{field : "IdEmisor", title:$translate.instant("UI_EMISOR"), width: "100px"}
				        ,{field : "RutEmisor", title:$translate.instant("UI_RUT_EMISOR"), width: "100px", align: "right"}
				        ,{field : "FechaVencimiento", title: $translate.instant('UI_FECHA_VENCIMIENTO_INSTRUMENTO'), width: "100px", align: "left"}
				        ,{field : "FechaVencimientoPacto", title: $translate.instant('UI_FECHA_VENCIMIENTO_PACTO'), width: "100px", align: "left"}
				        ,{field : "DiasAlVencimiento",title: $translate.instant('UI_DIAS_AL_VENCIMIENTO'), width: "60px",align: "right"}
				        ,{field : "TasaPacto", title: $translate.instant('UI_TASA_PACTO'), width: "100px" ,template : " <label> #:  (util.bcs_formato(TasaPacto,'GEN','Tasa',MonedaPapel,FamiliaInstrumento,'')).valor #</label>"}
				        ,{field : "MonedaPacto", title:$translate.instant("UI_TIPO_REAJUSTE"), width: "100px"}
				        ,{field : "TipoTasa", title: $translate.instant('UI_TIPO_TASA'), width: "100px" }
				        ,{field : "Nominal", width: "100px" ,template : " <label> #:  (util.bcs_formato(Nominal,'MES','Cantidad',MonedaPapel,FamiliaInstrumento,'')).valor #</label>"}
				        ,{field : "MonedaPapel", title:$translate.instant("UI_TIPO_REAJUSTE_INSTRUMENTO"), width: "100px"}
				        //,{field : "TasaCompraOriginal", title: $translate.instant('UI_TASA_COMPRA_ORIGINAL'), width: "100px" ,template : " <label> #: (util.bcs_formato(TasaCompraOriginal,'MES','Tasa',MonedaPapel,FamiliaInstrumento,'')).valor #</label>"}
				        ,{field : "TasaCompraOriginal", title: $translate.instant('UI_TASA_COMPRA_ORIGINAL'), width: "100px" ,template : " <label> #: (util.bcs_formato(TasaCompraOriginal,'GEN','Tasa',MonedaPapel,FamiliaInstrumento,'')).valor #</label>"}
				        ,{field : "ValorCompraEnPesos", title: $translate.instant('UI_MONTO_COMPRA_EN_PESOS'), width: "100px",template : " <label> #: (util.bcs_formato(ValorCompraEnPesos,'MES','Monto','CLP',FamiliaInstrumento,'')).valor #</label>"}
				        ,{field : "ValorCompraEnMoneda", title: $translate.instant('UI_MONTO_COMPRA_EN_MONEDA'), width: "100px",template : " <label> #: MonedaPacto ==''?(util.bcs_formato(ValorCompraEnMoneda,'MES','Monto',MonedaPapel,'','')).valor:(util.bcs_formato(ValorCompraEnMoneda,'MES','Monto',MonedaPacto,'','')).valor #</label>"}
				        ,{field : "InteresAcumulado", title: $translate.instant('UI_INTERES_ACUMULADO'), width: "100px",template : " <label> #: (util.bcs_formato(InteresAcumulado,'MES','Monto',MonedaPapel,FamiliaInstrumento,'')).valor #</label>"}
				        ,{field : "ReajusteAcumulado", title: $translate.instant('UI_REAJUSTE_ACUMULADO'), width: "100px",template : " <label> #: (util.bcs_formato(ReajusteAcumulado,'MES','Monto',MonedaPapel,FamiliaInstrumento,'')).valor #</label>"}
				        //,{field : "ValorPresente", title: $translate.instant('UI_VALOR_PRESENTE'), width: "100px",template : " <label> #: (util.bcs_formato(ValorPresente,'MES','Monto','CLP',FamiliaInstrumento,'')).valor #</label>"}
				        ,{field : "Interes", title: $translate.instant('UI_INTERES'), width: "100px",template : " <label> #: (util.bcs_formato(Interes,'MES','Monto','CLP',FamiliaInstrumento,'')).valor #</label>"}
				        ,{field : "Reajuste", title: $translate.instant('UI_REAJUSTE'), width: "100px",template : " <label> #: (util.bcs_formato(Reajuste,'MES','Monto','CLP',FamiliaInstrumento,'')).valor #</label>"}
				        ,{field : "ValorPresente_t1", title: $translate.instant('UI_MONTO_MERCADO_MANANA'), width: "100px",template : " <label> #: (util.bcs_formato(ValorPresente_t1,'MES','Monto',MonedaPapel,FamiliaInstrumento,'')).valor #</label>"}
				        ,{field : "ValorAlVencimientoEnPesos", title: $translate.instant('UI_VALOR_AL_VENCIMIENTO_PESOS'), width: "100px",template : " <label> #: (util.bcs_formato(ValorAlVencimientoEnPesos,'MES','Monto','CLP',FamiliaInstrumento,'')).valor #</label>"}
				        ,{field : "ValorAlVencimientoEnMoneda", title: $translate.instant('UI_VALOR_AL_VENCIMIENTO_MONEDA'), width: "100px",template : " <label> #: MonedaPacto ==''?(util.bcs_formato(ValorAlVencimientoEnMoneda,'MES','Monto',MonedaPapel,'','')).valor:(util.bcs_formato(ValorAlVencimientoEnMoneda,'MES','Monto',MonedaPacto,'','')).valor #</label>"}
				        ,{field : "ClasificacionRiesgo", title:$translate.instant("UI_CLASIFICACION_RIESGO"), width: "100px"}
				        ,{field : "TasaMercado", title: $translate.instant('UI_COMPRA_ORIGINAL'), width: "100px" ,template : " <label> #:  (util.bcs_formato(TasaMercado,'GEN','Tasa',MonedaPapel,FamiliaInstrumento,'')).valor #</label>"}
				        ,{field : "ValorMercado", title: $translate.instant('UI_VALOR_MERCADO_HOY'), width: "100px",template : " <label> #: (util.bcs_formato(ValorMercado,'MES','Monto',MonedaPapel,FamiliaInstrumento,'')).valor #</label>"} 
				       // ,{field : "MenorValorAlVencimiento", title: $translate.instant('UI_MENOR_VALOR_AL_VENCIMIENTO'), width: "100px",template : " <label> #: (util.bcs_formato(MenorValorAlVencimiento,'MES','Monto','CLP',FamiliaInstrumento,'')).valor #</label>"}
				       // ,{field : "MayorValorAlVencimiento", title: $translate.instant('UI_MAYOR_VALOR_AL_VENCIMIENTO'), width: "100px",template : " <label> #: (util.bcs_formato(MayorValorAlVencimiento,'MES','Monto','CLP',FamiliaInstrumento,'')).valor #</label>"}
				        ,{field : "FechaTasaBCS", title: $translate.instant('UI_FECHA_TASA_BCS'), width: "100px", align: "left"}
				        //,{field : "TasaMercadoBCS", title: $translate.instant('UI_COMPRA_ORIGINAL_BCS'), width: "100px" ,template : " <label> #:  (util.bcs_formato(TasaMercadoBCS,'GEN','Tasa',MonedaPapel,'','')).valor #</label>"}
				        //,{field : "ValorMercadoBCS", title: $translate.instant('UI_VALOR_MERCADO_BCS'), width: "100px",template : " <label> #: (util.bcs_formato(ValorMercadoBCS,'MES','Monto','CLP',FamiliaInstrumento,'')).valor #</label>"}
				        ,{field : "MenorValor", title: $translate.instant('UI_MENOR_VALOR'), width: "100px",template : " <label> #: (util.bcs_formato(MenorValor,'MES','Monto','CLP',FamiliaInstrumento,'')).valor #</label>"}
				        ,{field : "MayorValor", title: $translate.instant('UI_MAYOR_VALOR'), width: "100px",template : " <label> #: (util.bcs_formato(MayorValor,'MES','Monto','CLP',FamiliaInstrumento,'')).valor #</label>"}
				        ,{field : "TipoPacto", title:$translate.instant("UI_TIPO_PACTO"), width: "100px"}
				        ,{field : "FechaProximoCupon", title: $translate.instant('UI_FECHA_PROXIMO_CUPON'), width: "100px", align: "left"}
				        ,{field : "ProximoCuponNominal", title:$translate.instant("UI_PROXIMO_CUPON_NOMINAL"), width: "100px"}
				        ,{field : "AmortizacionCupon", title: $translate.instant('UI_AMORTIZACION_CUPON'), width: "100px",template : " <label> #: (util.bcs_formato(AmortizacionCupon,'MES','Monto','CLP',FamiliaInstrumento,'')).valor #</label>"}
				        ,{field : "InteresCupon", title: $translate.instant('UI_INTERES_CUPON'), width: "100px",template : " <label> #: (util.bcs_formato(InteresCupon,'MES','Monto','CLP',FamiliaInstrumento,'')).valor #</label>"}
				        ,{field : "NumeroCupon", title:$translate.instant("UI_NUMERO_CUPON"), width: "100px", align: "right"}
				        ,{field : "FrecuenciaCupon", title:$translate.instant("UI_FRECUENCIA_CUPON"), width: "100px", align: "left"}
				        ,{field : "TieneCustodia_v1", title:$translate.instant("UI_TIENE_CUSTODIA"), width: "100px", align: "left"}
				        ,{field : "TipoBoveda", title:$translate.instant("UI_TIENE_BOVEDA"), width: "100px", align: "left"}
				        ,{field : "Fungibilidad", title:$translate.instant("UI_FUNGIBILIDAD"), width: "100px", align: "left"}
				        ,{field : "ClasificacionTablaIndice", title:$translate.instant("UI_CLASIFICACION_X_TABLA"), width: "100px", align: "left"}
				        ,{field : "TieneGarantia", title:$translate.instant("UI_TIENE_GARANTIA"), width: "100px", align: "left"}
				        ,{field : "TipoGarantia", title:$translate.instant("UI_TIPO_GARANTIA"), width: "100px", align: "left"}
				        ,{field : "NemoSVS", title: $translate.instant('UI_NEMOTECNICO_SVS'), width:"110px", align:"left"}
				        ,{field : "TasaTera", title: $translate.instant('UI_COMPRA_ORIGINAL_TERA'), width: "100px" ,template : " <label> #:  (util.bcs_formato(TasaTera,'GEN','Tasa',MonedaPapel,FamiliaInstrumento,'')).valor #</label>"}
				        ,{field : "InteresDevengado", title: $translate.instant('UI_INTERES_DEVENGO'), width: "100px",template : " <label> #: (util.bcs_formato(InteresDevengado,'MES','Monto','CLP',FamiliaInstrumento,'')).valor #</label>"}
				        ,{field : "FolioCartera", title:$translate.instant("UI_FOLIO_CARTERA"), width: "100px", align: "right"}
				        ,{field : "TipoOperacion", title:$translate.instant("UI_TIPO_OPERACION"), width: "100px", align: "left"}
				        //,{field : "ValorLibro", title: $translate.instant('UI_VALOR_LIBRO'), width: "100px",template : " <label> #: (util.bcs_formato(ValorLibro,'MES','Monto','CLP',FamiliaInstrumento,'')).valor #</label>"}
				        ,{field : "CodigoIFRS", title:$translate.instant("UI_CODIGO_IFRS"), width: "100px", align: "left"}
        ],
        
		allowCopy: true,
		autoBind: false,
		excel : {
			allPages: true,
		},
		
		filterable: true,
		groupable: true,
		height:400,
		pageable: true,
		reorderable: true,
		resizable: true,
		scrollable: true,
		selectable: "multiple, row",
		sortable: true,
		userSettings:true,
		navigatable: true,
		
    }; 		


    
    
	$scope.buscarCarteraConsolidada = function(filtros) {
		
		var idCliente = -1;
		var idPortafolio = -1;
		var idTipoCartera = $scope.data.oConf_TipoCartera.idActual;
		
		if($scope.data.oConf_Cartera.idActual ==='TOD'){
			 idCliente = 0;
			 idPortafolio = 0;
		}

		if ($scope.data.oConf_Cartera.idActual === 'TERC') {
			idCliente = $scope.data.oConf_ClientePortafolio.seleccionado.IdCliente || 0;
			idPortafolio = $scope.data.oConf_ClientePortafolio.seleccionado.IdPortafolio  || 0;
		} 
		else if ($scope.data.oConf_Cartera.idActual === 'PROP') {
			idCliente = -1;
			idPortafolio = -1;			
		};		
		
		if ($scope.data.oConf_TipoCartera.idActual) {
			idTipoCartera = $scope.data.oConf_TipoCartera.idActual;
		}
		
		var handlerOriginal = {
				p_Fecha			: util.dateSPdate($scope.data.kngFecha),
				p_IdEmpresa 	: $scope.SesionIdEmpresa,
				p_IdUsuario 	: $scope.SesionIdUsuario,
				p_IdModulo 		: 'MRF',
				p_IdPais 		: $scope.SesionIdPaisSistema,
				p_TipoCartera 	: idTipoCartera, //$scope.data.oConf_Cartera.IdActual
				p_IdCliente 	: idCliente,
				p_IdPortafolio 	: idPortafolio
			}
		
		handler = handlerOriginal;
		
		var filtersV2 = new Array();
		
		dsCarteraConsolidada.setHandler(handler);
		dsCarteraConsolidada.filter(filtersV2);
		
		
	};    

	
	
	$scope.data.kngFecha = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
	$scope.data.SesionIdEmpresa = $scope.SesionIdEmpresa;
	$scope.data.oConf_ClientePortafolio.idActual = false;
});


