angular.module('ModuloCustodia').controller('IngresarGarantiaCtrl',
				function($scope,$translate,$resource,$timeout,bcsComunes,$rootScope,
						bcsServiciosGarantia,bcsServicioPlataforma)
{		
	
	$scope.ventanaId = bcsComunes.ventanaWinId($scope); // bcsComunes.ventanaOverlay($scope.ventanaId,true);
    var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	 
	$scope.data = {};
	$scope.btnActivo = true;	
	$scope.data.kngTotalMontoGarantizado = 0;
	
	$scope.data.instrumentoBusqueda = { IdInstrumento : 0 };
 
	//FECHA SISTEMA
	$scope.data.kngFechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
	
	$scope.oConfiguracionInstrumento = {
			buscar : "-1"    
		};
	
	
	var monedaDatasource = new kendo.data.DataSource({
		type: "odata",
		serverFiltering:true,
		transport: {
			read: {
				url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1013",
				dataType: "json"
				}
		},
		filter: { field: "IdMoneda", operator: "eq", value: $scope.SesionIdMonedaSistema},
	});		
	monedaDatasource.query().then(function(){
		$scope.$apply(function() {
			$scope.data.monedaSistema = monedaDatasource.data()[0].NombreLargo;
		});
	});
 
	//LLAMADA A SERVICIOS SP
	/*ObtenerParidadTipoCambio = function(Input){
		var urlObtenerParidadTipoCambio = "/BSCL/Plataforma/GWJAVA/PLA/call?t=201";
		var r = $resource("", {},{'ObtenerParidadTipoCambio': { method: "POST", url: urlObtenerParidadTipoCambio}});
		var post = r.ObtenerParidadTipoCambio(Input);
		return post.$promise;
	};*/


	//COMBOBOX	
	$scope.data.oClientePortafolio={
		tipo : "combobox-cliente-portafolio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdEstado : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},
	};
	
	$scope.data.oConfiguracionConceptoGarantia={
			tipo : "combobox-conceptos-garantia",
			configuracion : {},
			filtros : {
				IdEstado			: { "field"  : "IdEstado","operator": "eq", "value"   : "VIG" },
				ConfiguradoEnModulo	: { "field"  : "ConfiguradoEnModulo","operator": "eq", "value"   : "S" },
				VisibleOptimus		: { "field"  : "VisibleOptimus","operator": "eq", "value" : "S" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	
	$scope.data.oConfiguracionTipoInstrumento={
			tipo 			: "combobox-tipo-instrumento-subyacente",
			configuracion 	: {},
			filtros 		: {
						IdEstado 							: { "field" : "IdEstadoSubyacente",		"operator": "eq", "value"   : "ACT" },
						ExigeGarantia 						: { "field" : "ExigeGarantia",	"operator": "eq", "value"   : "S" },
						IdClasificacionTipoInstrumento 		: { "field" : "IdClasificacionTipoInstrumento",  "operator": "eq",  "value" : "" },
						IdTipoInstrumento 					: { "field" : "IdTipoInstrumento",  "operator": "eq",  "value" : "" },
			},
			idActual 		: false,
			seleccionado 	: false,
			idDefecto 		: ''
	};
		
	$scope.data.oConfiguracionInstrumento={
			tipo 			: "combobox-instrumento",
			configuracion 	: {},
			filtros 		: {
				IdEstado 			: { "field" : "IdEstado",		   "operator": "eq", "value" : "ACT" },
				IdTipoInstrumento 	: { "field" : "IdTipoInstrumento", "operator": "eq", "value" : "" },
			//	FechaVencimiento    : { "field" : "FechaVencimiento",  "operator": "gt",  "value" :  util.date($scope.SesionFechaSistema).toDate() }
				
			},
			idActual 		: false,
			seleccionado 	: false,
			idDefecto 		: '',
			idPostFiltro  	: false
	};
	
	$scope.data.oConfiguracionEntidadDestinoGarantia={
			tipo 			: "combobox-entidad-destino-garantia",
			configuracion 	: {},
			filtros 		: {
				IdEstadoEntidadDestinoGarantia 	: { "field"   : "IdEstadoEntidadDestinoGarantia", "operator": "eq", "value"   : "ACT" },
				IdEntidadDestinoGarantia 	    : { "field"   : "IdEntidadDestinoGarantia",       "operator": "eq", "value"   : 0 }
			},
			idActual 		: false,
			seleccionado 	: false,
			idDefecto 		: '',
			idPostFiltro  	: false
	};	
	
	$scope.data.oConfiguracionTipoInstrumentoExigeGarantia={
		tipo 			: "combobox-tipo-instrumento",
		configuracion 	: {},
		filtros 		: {
				IdEstado 				: { "field"   : "IdEstado",					"operator": "eq", "value"   : "VIG" },
				ExigeGarantia 			: { "field"   : "ExigeGarantia",			"operator": "eq", "value"   : "S" },
				IdEntidadDestinoGarantia: { "field"   : "IdEntidadDestinoGarantia",	"operator": "eq", "value"   : 0 },
				IdTipoInstrumento: 		  { "field"   : "IdTipoInstrumento",		"operator": "eq", "value"   : "" },			
		},
		idActual 		: false,
		seleccionado 	: false,
		idDefecto 		: ''
	};
	

	$scope.data.oConfiguracionInstrumentoExigeGarantia={
		tipo 			: "combobox-instrumento-bolsa",
		configuracion 	: {},
		filtros 		: {
			IdEstado 		  		: { "field"   : "IdEstado",         		"operator": "eq", "value"   : "ACT" },
			ExigeGarantia 	  		: { "field"   : "ExigeGarantia",    		"operator": "eq", "value"   : "S" },
			IdTipoInstrumento 		: { "field"   : "IdTipoInstrumento",		"operator": "eq", "value"   : "" },
			IdEntidadDestinoGarantia: { "field"   : "IdEntidadDestinoGarantia",	"operator": "eq", "value"   : 0 },
		},
		idActual 		: false,
		seleccionado 	: false,
		idDefecto 		: ''
	};

	
	$scope.data.oConfiguracionBoveda={
			tipo : "combobox-boveda",
			configuracion : {},
			filtros : {
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "VIG" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	$scope.data.oConfiguracionEspecie={
			tipo : "combobox-especie-en-garantia",
			configuracion : {},
			filtros : {
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.data.oConfiguracionTipoInstrumentoB={
			tipo : "combobox-tipo-instrumento",
			configuracion : {},
			filtros : {
				IdEstado : { "field": "IdEstado","operator": "eq", "value"   : "VIG" },
				excl1 	 : { "field": "IdClasificacionTipoInstrumento", "operator": "neq", "value" : "DV" },
				excl2 	 : { "field": "IdTipoInstrumento", "operator": "neq", "value" : "PAC" },
				excl3 	 : { "field": "IdTipoInstrumento", "operator": "neq", "value" : "SPT" },
				excl4 	 : { "field": "IdTipoInstrumento", "operator": "neq", "value" : "PRT" },
				excl5 	 : { "field": "IdTipoInstrumento", "operator": "neq", "value" : "EMP" },
				excl6 	 : { "field": "IdTipoInstrumento", "operator": "neq", "value" : "SIM" },
				excl61 	 : { "field": "IdTipoInstrumento", "operator": "neq", "value" : "RRV" },
				excl62	 : { "field": "IdTipoInstrumento", "operator": "neq", "value" : "RRF" },
				excl7 	 : { "field": "IdTipoInstrumento", "operator": "neq", "value" : "OSA" },
				excl8 	 : { "field": "IdTipoInstrumento", "operator": "neq", "value" : "MON" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	//PARAMETROS SERVICIOS 
	var parametrosServicios = function(serv){
		var param = {};	

		param.p_IdEmpresa = $scope.SesionIdEmpresa;  
		param.p_IdUsuario = $scope.SesionIdUsuario; 
		param.p_IdModulo  = "CUS"; 

		if (serv === "CTAINV"){
			var datosPortafolio = {};
			var TablaPortafolioAConsultar = [];
			
			//datosPortafolio.Identificador = 1;
			datosPortafolio.IdPortafolio = $scope.data.oClientePortafolio.seleccionado.IdPortafolio;
			datosPortafolio.IdMoneda = null;
			TablaPortafolioAConsultar.push(datosPortafolio);
							  			                                                                                                                                                              
			param.p_FechaDesde 						= $scope.SesionFechaSistema;
			param.p_FechaHasta 						= $scope.SesionFechaSistema;
            param.p_PortafolioAConsultar            = TablaPortafolioAConsultar;
            param.p_UsaReglasNegocio 				= "N";
            param.p_Uso 							= "CUENTA_INVERSION";

		}

		if (serv === "CUSTODIA"){
			param.p_UsaReglasNegocio 				= "S";
			param.p_IdPais                          = $scope.SesionIdPaisSistema;
			param.p_PlazoDias						= 0;
			param.p_IdCliente  						= $scope.data.oClientePortafolio.seleccionado.IdCliente;         
			param.p_IdPortafolio 					= $scope.data.oClientePortafolio.seleccionado.IdPortafolio;
//			param.p_IdTipoOperacion					= $scope.data.oConfiguracionConceptoGarantia.seleccionado.IdTipoOperacion;
			param.p_IdTipoOperacion					= $scope.data.Prendaria ? 'INGGPR': 'INGGTA';

			if ($scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento == 'IOA'){
				param.p_IdTipoInstrumentoAGarantizar	= $scope.data.oConfiguracionInstrumento.seleccionado.CodigoTipoInstrumento || null;  //null para garantias prendarias
			}	
			else
			{	
				param.p_IdTipoInstrumentoAGarantizar	= $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento || null;  //null para garantias prendarias
			}
			
			
			param.p_IdInstrumentoAGarantizar		= $scope.data.oConfiguracionInstrumento.seleccionado.IdInstrumento || null;          //null para garantias prendarias
			param.p_IdEntidadCobertura				= $scope.data.oConfiguracionEntidadDestinoGarantia.seleccionado.IdEntidadDestinoGarantia || null;           //BCS  para garantias prendarias			
			param.p_ValorizarFlujo					= 'S';
			param.p_IdBolsa							= $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdBolsa;

			if ($scope.data.oConfiguracionTipoInstrumentoB.seleccionado.IdTipoInstrumento !== '' && $scope.data.oConfiguracionTipoInstrumentoB.seleccionado.IdTipoInstrumento !== 0){
				param.p_IdTipoInstrumentoBusqueda = $scope.data.oConfiguracionTipoInstrumentoB.seleccionado.IdTipoInstrumento;
				
				if (typeof $scope.data.instrumentoBusqueda !== 'undefined') {
					if ($scope.data.instrumentoBusqueda.IdInstrumento > 0){ 
						param.p_IdInstrumentoBusqueda = $scope.data.instrumentoBusqueda.IdInstrumento;
					}
				}
			} 
			
		}				
		return param;
	};


	//GRILLA	
	var dsGrillaGtaCustodia = new bcs.DataSource({
		type 		: "call", //Los tipos posibles son: odata, call, array y object
					definition 	: { serverFiltering 	: false, // Filtrado de datos en el servidor. 
									serverPaging 		: true, // Paginado de los datos en el servidor.
									serverSorting 		: true, // Ordenamiento de los datos en el servidor.							
									//sort 				: { field: "IdMoneda", dir: "desc" },
									pageSize 			: 25,
									callParameters      : parametrosServicios('CUSTODIA'),
									callTransaction 	: "265",
									callResultSet 		: "p_out_CustodiaGarantia",
									schema 				: {model: {
																fields : {
																	IdClasificacionTipoInstrumento  	: 	{type: "string"},																
																	IdTipoInstrumento 					: 	{type: "string"},
																	NombreTipoInstrumento 				: 	{type: "string"},
																	Instrumento							:	{type: "string"},
																	IdInstrumento						:	{type: "number"},
																	IdEmisor							:	{type: "string"},
																	CantidadDisponibleGarantia			:	{type: "number"},
																	ValorRescateDisponibleGarantia		:	{type: "number"},
																	MontoDisponibleGarantia				:	{type: "number"},
																	MontoDisponibleMonedaLocalGarantia  :	{type: "number"},
																	IdMonedaInstrumento					:	{type: "string"},
																	IdMonedaEmisionInstrumento			:	{type: "string"},
																	EsFungible 							:	{type: "string"},
																	IdEmisor							:	{type: "string"},
																	FechaEmisionInstrumento				:	{type: "sysdate"},
																	FechaVencimientoInstrumento			:	{type: "sysdate"},
																	ValorClasificacionRiesgo			:	{type: "string"},
																	PrecioMercado						:	{type: "number"},
																	PrecioVaR							:	{type: "number"},
																	TasaMercado							:	{type: "number"},
																	MontoAMercado						:	{type: "number"},
																	PrecioVector						:	{type: "number"},
																	MontoVector							:	{type: "number"},
																	PorcentajeCoberturaEntidad			:	{type: "number"},
																	MontoCoberturaEntidad				:	{type: "number"},
																	PorcentajeCoberturaCorredora		:	{type: "number"},
																	MontoCoberturaCorredora				:	{type: "number"},
																	MontoCobertura						:	{type: "number"},
																	CantidadAIngresar					:	{type: "number"},//, validation : { required: true, min: 0 }},
																	MontoAGarantizar					:	{type: "number"},																	
																	MontoAGarantizarMonSist 			: 	{type: "number"},
																	Precio 								:   {type: "number"},
																	CorteMinimo							:   {type: "number"},
																	ValorNominal						:   {type: "number"},
																	ISIN								:	{type: "string"},
																	IdTipoReajuste						:	{type: "string"},
																	NemoSVS								:	{type: "string"},
																}
															}
														},

								  }
	});
	
	var dsGrillaGtaCtaInv = new bcs.DataSource({
		type 		: "call", //Los tipos posibles son: odata, call, array y object
							definition 	: {
											serverFiltering 	: false, // Filtrado de datos en el servidor.
											serverPaging 		: true, // Paginado de los datos en el servidor.
											serverSorting 		: true, // Ordenamiento de los datos en el servidor.							
											//sort 				: { field: "IdMoneda", dir: "desc" },
											pageSize 			: 25,
											callParameters      : parametrosServicios('CTAINV'),
											callTransaction 	: "1673",
											callResultSet 		: "P_OUT_FLUJOCAJA",							
											schema 				: {model: {
																		fields : {
																			IdMoneda				: { type: "string"},
																			FechaMovimiento			: { type: "date"},
																			Saldo 					: { type: "number"},
																			MontoAGarantizar 		: { type: "number"},
																			CantidadAIngresar 		: { type: "number"},
																			MontoAGarantizarMonSist : { type: "number"}
																		}
																	}
																}
										  }
	});
	
	 
	var grillaCustodia = null;
	$scope.data.confBasicaCustodia = {
		dataSource			: dsGrillaGtaCustodia,
		columns				:  [
								 {field: "IdClasificacionTipoInstrumento", 		title: "UI_MERCADO",				    width: "50px"}			
								,{field: "NombreTipoInstrumento",				title: "UI_TIPO_INSTRUMENTO", 			width: "100px", 	attributes:{style:"white-space:nowrap;"}}
								,{field: "Instrumento", 						title: "UI_INSTRUMENTO",				width: "90px"}
								,{field: "ISIN", 								title: 'UI_ISIN', 						width: "70px",		hidden: true,	attributes:{style:"white-space:nowrap;"}}	    		
								,{field: "IdEmisor", 							title: "UI_EMISOR", 					width: "80px", 		attributes:{style:"white-space:nowrap;"}}
								,{field: "EsFungible", 							title: "UI_FUNGIBLE", 					width: "50px"}
								,{field: "CorteMinimo", 						title: "UI_CORTE_MINIMO", 				width: "80px",  	template : " <label> #:  (util.bcs_formato(CorteMinimo||0,'CUS','Cantidad',(IdClasificacionTipoInstrumento == 'RF' || IdClasificacionTipoInstrumento == 'IF')? IdTipoReajuste:IdMonedaInstrumento,IdTipoInstrumento,'')).valor #</label>"}
								,{field: "CantidadDisponibleGarantia", 			title: "UI_CANTIDAD",					width: "70px",  	template : " <label> #:  (util.bcs_formato(CantidadDisponibleGarantia||0,'CUS','Cantidad',(IdMonedaInstrumento!=''?IdMonedaInstrumento:IdTipoReajuste),IdTipoInstrumento,'')).valor #</label>"}	
								,{field: "ValorRescateDisponibleGarantia", 		title: "UI_VALOR_RESCATE",				width: "70px",  	hidden: true, 	template : " <label> #:  (util.bcs_formato(ValorRescateDisponibleGarantia||0,'CUS','Cantidad',(IdMonedaInstrumento!=''?IdMonedaInstrumento:IdTipoReajuste),IdTipoInstrumento,'')).valor #</label>"}	
								,{field: "ValorNominal", 						title: "UI_VALOR_NOMINAL", 				width: "100px",  	hidden: true,	template : " <label> #:  (util.bcs_formato(ValorNominal||0,'CUS','Cantidad',(IdMonedaInstrumento!=''?IdMonedaInstrumento:IdTipoReajuste),IdTipoInstrumento,'')).valor #</label>"}
								,{field: "IdMonedaInstrumento", 				title: "UI_MONEDA", 					width: "50px", 		template: kendo.template("#if ( IdClasificacionTipoInstrumento == 'RF' || IdClasificacionTipoInstrumento == 'IF') {##=IdTipoReajuste##} else {##=IdMonedaInstrumento##}#")}
								,{field: "PrecioMercado", 						title: "UI_PRECIO_MERCADO", 			width: "100px", 	spinners:false, template : " <label> #:  (util.bcs_formato(PrecioMercado,'GEN','Precio',(IdMonedaInstrumento!==''?IdMonedaInstrumento:IdTipoReajuste),IdTipoInstrumento,'')).valor #</label>"}
								,{field: "PrecioVaR", 							title: "UI_PRECIO_VAR", 				width: "100px", 	spinners:false, template : " <label> #:  (util.bcs_formato(PrecioVaR,'GEN','Precio',(IdMonedaInstrumento!==''?IdMonedaInstrumento:IdTipoReajuste),IdTipoInstrumento,'')).valor #</label>"}
								,{field: "TasaMercado", 						title: "UI_TASA_MERCADO", 				width: "100px", 	spinners:false, template : " <label> #:  (util.bcs_formato(TasaMercado,'GEN','Tasa',(IdMonedaInstrumento!==''?IdMonedaInstrumento:IdTipoReajuste),IdTipoInstrumento,'')).valor #</label>"}
								,{field: "MontoDisponibleMonedaLocalGarantia",	title: "UI_MONTO_MERCADO", 				width: "100px", 	spinners:false, template : " <label> #:  (util.bcs_formato(MontoDisponibleMonedaLocalGarantia,'CUS','Monto',(IdMonedaInstrumento!==''?IdMonedaInstrumento:IdTipoReajuste),IdTipoInstrumento,'')).valor #</label>"}
								,{field: "MontoDisponibleGarantia", 			title: "UI_VALOR_MERCADO", 				width: "100px",  	template : " <label> #:  (util.bcs_formato(MontoDisponibleGarantia||0,'CUS','Monto',(IdMonedaInstrumento!=''?IdMonedaInstrumento:IdTipoReajuste),IdTipoInstrumento,'')).valor #</label>"}
								
								//Varía dependiendo de IdClasificacionTipoInstrumento
								,{field: "CantidadAIngresar", 					title: "UI_CANTIDAD_A_GARANTIZAR", 		width: "100px"			,spinners:false, editable: true, template : " <label> #:  (util.bcs_formato(CantidadAIngresar,'CUS','Cantidad',(IdClasificacionTipoInstrumento == 'RF' || IdClasificacionTipoInstrumento == 'IF')? IdTipoReajuste:IdMonedaInstrumento,IdTipoInstrumento,'')).valor #</label>" }
								,{field: "MontoAGarantizarMonSist", 			title: "UI_MONTO_A_GARANTIZAR_MONEDA_SISTEMA", width: "100px"	,spinners:false, editable: true, template : " <label> #:  (util.bcs_formato(MontoAGarantizarMonSist,'CUS','Monto','" + $scope.SesionIdMonedaSistema + "','','')).valor #</label>"}  //Formato debe ir moneda de sistema
								,{field: "MontoAGarantizar", 					title: "UI_VALOR_A_GARANTIZAR", 		width: "100px"			,spinners:false, editable: false, template : " <label> #:  (util.bcs_formato(MontoAGarantizar,'CUS','Monto',(IdMonedaInstrumento!==''?IdMonedaInstrumento:IdTipoReajuste),IdTipoInstrumento,'')).valor #</label>"}
								
								//,{field: "PrecioVector", 						title: "UI_PRECIO_VECTOR", 				width: "100px", 	hidden:true, template : " <label> #:  (util.bcs_formato(PrecioVector,'CUS','Monto',(IdMonedaInstrumento!==''?IdMonedaInstrumento:IdTipoReajuste),IdTipoInstrumento,'')).valor #</label>"} 
								//,{field: "MontoVector", 						title: "UI_MONTO_VECTOR", 				width: "100px", 	hidden:true, template : " <label> #:  (util.bcs_formato(MontoVector,'CUS','Monto',(IdMonedaInstrumento!==''?IdMonedaInstrumento:IdTipoReajuste),IdTipoInstrumento,'')).valor #</label>"}
								,{field: "PorcentajeCoberturaEntidad", 			title: "UI_PORC_COBERTURA_ENT_ABR", 	width: "100px", 	spinners:false, template : " <label> #:  (util.bcs_formato(PorcentajeCoberturaEntidad,'CUS','Monto',(IdMonedaInstrumento!==''?IdMonedaInstrumento:IdTipoReajuste),IdTipoInstrumento,'')).valor #</label>"}
								,{field: "MontoCoberturaEntidad", 				title: "UI_VALOR_COBERTURA_ENT_ABR", 	width: "100px", 	spinners:false, template : " <label> #:  (util.bcs_formato(MontoCoberturaEntidad,'CUS','Monto',(IdMonedaInstrumento!==''?IdMonedaInstrumento:IdTipoReajuste),IdTipoInstrumento,'')).valor #</label>"}
								,{field: "PorcentajeCoberturaCorredora",		title: "UI_PORC_COBERTURA_CORR_ABR", 	width: "100px", 	spinners:false, template : " <label> #:  (util.bcs_formato(PorcentajeCoberturaCorredora,'CUS','Monto',(IdMonedaInstrumento!==''?IdMonedaInstrumento:IdTipoReajuste),IdTipoInstrumento,'')).valor #</label>"}
								,{field: "MontoCoberturaCorredora", 			title: "UI_VALOR_COBERTURA_CORR_ABR", 	width: "100px", 	spinners:false, template : " <label> #:  (util.bcs_formato(MontoCoberturaCorredora,'CUS','Monto',(IdMonedaInstrumento!==''?IdMonedaInstrumento:IdTipoReajuste),IdTipoInstrumento,'')).valor #</label>"}
								,{field: "FechaEmisionInstrumento", 			title: "UI_FECHA_EMISION", 				width: "85px"}
								,{field: "FechaVencimientoInstrumento", 		title: "UI_FECHA_VENCIMIENTO", 			width: "85px"}
								,{field: "ValorClasificacionRiesgo", 			title: "UI_CLASIFICACION_RIESGO", 		width: "100px",		attributes:{style:"white-space:nowrap;"}}								
							  ],
		sortable			: true,
		userSettings 		: true,
		allowCopy			: { delimeter: "\t" },
		pageable			: false,
		autoBind			: false,  //comentar		
		filterable			: true,
		editable			: true,
		navigatable			: true,
		height				: 385, 
		selectable 			: "row",
		save				: function(e) 	{ calculoMontoAGarantizar('CUS', e.values, e.model);},
		gridCreated 		: function(grid) { 
									grillaCustodia = grid;
									if ( $scope.SesionLocale === 'PE' ){
										grid.showColumn("ISIN");
										//grid.showColumn("ValorNominal");
										//grid.showColumn("PrecioVector");
										//grid.showColumn("MontoVector");	
									} else {
										grid.showColumn("ValorRescateDisponibleGarantia");
									}									
							  },
		// se comenta ya que presenta dificultades con los template,		
		notEditableColumns : function(fila) {
								if ( fila.IdClasificacionTipoInstrumento !='RV'){
									return ['MontoAGarantizarMonSist'];
								}
	                          }	    
	    };
	 
	var grillaCtaInv  = null;
	$scope.data.confBasicaCuentaInversion = {
		dataSource			: dsGrillaGtaCtaInv,
		columns				: [  {field: "IdMoneda", 				title: "UI_MONEDA", width: "150px"}
								,{field: "Saldo", 					title: "UI_SALDO", width: "120px", template : " <label> #:  (util.bcs_formato(Saldo,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
								,{field: "MontoAGarantizar", 		title: "UI_MONTO_A_GARANTIZAR", width: "120px", editable: true, validation : { required: true, min: 0 }, template : " <label> #:  (util.bcs_formato(MontoAGarantizar,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
								,{field: "MontoAGarantizarMonSist", title: "UI_MONTO_A_GARANTIZAR_MONEDA_SISTEMA",  template : " <label> #:  (util.bcs_formato(MontoAGarantizarMonSist,'GEN','Monto','" + $scope.SesionIdMonedaSistema + "','','')).valor #</label>" }
							  ],
		sortable			: true,
		pageable			: false,
		userSettings 		: true,
		allowCopy			: { delimeter: "\t" },
		autoBind			: false,  //comentar
		filterable			: true,
		editable			: true,
		height				: 385,
		selectable 			: "row",
		save: function(e) 	{calculoMontoAGarantizar('CTA', e.values, e.model);},
		gridCreated : function(grid) {
	    	grillaCtaInv = grid;
	    },	    
		notEditableColumns : function(fila)
		{
			if (fila.Saldo == 0)  //Sólo se puede ingresar monto a garantizar, cuando tenga saldo en cta inversión
			{
				fila.MontoAGarantizar = 0;
				return ['MontoAGarantizar'];
			}
		}
	};

	//CALCULO DE MONTO A GARANTIZAR
	var TotalMontoGarantizado = 0;
	var calculoMontoAGarantizar = function(grid, values, model){		
		var MontoAGarantizar      = 0;
		var IdMonedaOrigen        = '';
		var data = Object();
		MontoAGarantizarOriginal = Number(model.MontoAGarantizar) || 0;
		TotalMontoGarantizado = $scope.data.kngTotalMontoGarantizado || 0;
		TotalMontoGarantizado = TotalMontoGarantizado - MontoAGarantizarOriginal;
		let maxErrFlag;
		
		switch (grid) 
		{
			case "CUS":
				IdMonedaOrigen        = (model.IdClasificacionTipoInstrumento === 'RF' || model.IdClasificacionTipoInstrumento === 'IF') ? model.IdTipoReajuste: model.IdMonedaInstrumento;
						
				if (values.CantidadAIngresar < 1)
				{	
					bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant("CANTIDAD_ASIGNAR_INVALIDA"));
					model.CantidadAIngresar = 0;					
					return;
				}
				if (values.MontoAGarantizarMonSist < 1)
				{	
					bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant("MONTO_ASIGNAR_INVALIDO"));
					model.CantidadAIngresar = 0;					
					return;
				}
				/*
				if (model.IdClasificacionTipoInstrumento === 'IF')// && model.EsFungible === 'S') 
				{   //La cantidad siempre es 1   se muestra  el valor de rescate
					model.CantidadAIngresar = model.ValorRescateDisponibleGarantia;
					
					if (values.CantidadAIngresar <= model.ValorRescateDisponibleGarantia)
					{						
						if (model.MontoDisponibleGarantia > 0)
						{
							ValorRescatedAGarantizar = (Number(model.ValorRescateDisponibleGarantia) / model.MontoDisponibleGarantia) * values.MontoAGarantizarMonSist;
						} 
						else 
						{
							ValorRescatedAGarantizar = model.ValorRescateDisponibleGarantia;
						}

						model.CantidadAIngresar = Math.trunc(ValorRescatedAGarantizar);
					}
					else 
					{
						bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant("UI_ERROR_MONTOGAR_EXCEDE_DISPONIBLE"));						
						return;
					}
				} 
				else
				{*/ 

					if ((values.CantidadAIngresar > model.CantidadDisponibleGarantia && model.IdClasificacionTipoInstrumento !== 'IF') || (values.CantidadAIngresar > model.ValorRescateDisponibleGarantia && model.IdClasificacionTipoInstrumento === 'IF'))
					{	
						var dataCustodia = dsGrillaGtaCustodia.data() || [];
						for(var i=0; i<dataCustodia.length; i++){
							if(model.uid == dataCustodia[i].uid){
								dataCustodia[i].CantidadAIngresar = dataCustodia[i].CantidadDisponibleGarantia;
								values.CantidadAIngresar = dataCustodia[i].CantidadDisponibleGarantia;
							}
						}
						
						bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant("UI_ERROR_EXCEDE_CANTIDAD"));
						maxErrFlag = true;
					}
					if (values.MontoAGarantizarMonSist > model.MontoDisponibleGarantia)
					{	
						var dataCustodia = dsGrillaGtaCustodia.data() || [];
						for(var i=0; i<dataCustodia.length; i++){
							if(model.uid == dataCustodia[i].uid){
								dataCustodia[i].MontoAGarantizarMonSist = dataCustodia[i].MontoDisponibleGarantia;
								values.MontoAGarantizarMonSist = dataCustodia[i].MontoDisponibleGarantia;
							}
						}
						bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant("UI_ERROR_EXCEDE_MONTO"));
						maxErrFlag = true;
					}
					if (values.MontoAGarantizarMonSist){
						var cant = new Big(Number(values.MontoAGarantizarMonSist) / Number(model.PrecioVaR));
						model.CantidadAIngresar = Math.trunc(Number(cant.toString()));
						values.CantidadAIngresar = model.CantidadAIngresar;
					}
					/*
					if ((model.IdClasificacionTipoInstrumento === 'RF' || model.IdClasificacionTipoInstrumento === 'IF') && model.CorteMinimo > 0)
					{
						if (values.CantidadAIngresar % model.CorteMinimo!==0 && model.CantidadDisponibleGarantia>=model.CorteMinimo)
						{
							bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant("CORTE_MINIMO_INVALIDO"));							
							return;
						}						
					}*/
					var disp = model.CantidadDisponibleGarantia;
					if ( model.IdClasificacionTipoInstrumento === "IF" || model.IdClasificacionTipoInstrumento === "RF")	{
						var cantidad = new Big(values.CantidadAIngresar);
						var corte = new Big(model.CorteMinimo);
						var nominal = cantidad.mod(corte);
						var rescate = new Big(values.CantidadAIngresar);
						nominal = rescate.mod(corte);
						if (nominal > 0){
							bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant("CORTE_MINIMO_INVALIDO"));
							values.MontoAGarantizarMonSist = 0;
							model.CantidadAIngresar = 0;							
							return;
						}
						if (model.IdClasificacionTipoInstrumento === "IF"){
							disp = model.ValorRescateDisponibleGarantia;
						}
					}					
					
					if (model.MontoDisponibleGarantia > 0)
					{
						MontoAGarantizar = values.CantidadAIngresar?(Number(model.PrecioVaR) * values.CantidadAIngresar):values.MontoAGarantizarMonSist;
					} 
					else 
					{
						MontoAGarantizar = values.CantidadAIngresar?(Number(model.PrecioVaR) * values.CantidadAIngresar):values.MontoAGarantizarMonSistr;
					}
					
					model.MontoAGarantizar = MontoAGarantizar;

				//}
					data.p_IdEmpresa =  $scope.SesionIdEmpresa;
					data.p_IdTrabajo =  1366;
					data.p_CalculosEntrada = [{
						"IdTrabajo"			: data.p_IdTrabajo
						,"IdInterno"		: 1
						,"Fecha"			: util.dateSPdate($scope.data.kngFechaSistema)
						,"IdMonedaOrigen" 	: model.IdMonedaInstrumento
						,"IdMonedaDestino" 	: $scope.SesionIdMonedaSistema
						,"ValorCambioLocalModificado" : 0
						,"MontoOrigen"		: MontoAGarantizar || 0
						,"IdUso"			: 'CUS'
						//,"TipoCambio" 		: 0
						//,"MonedaCambio"		: $scope.data.IdMonedaMontoDisplay
						//,"OrigenTipoCambio" : 'OBSERVADO'
					}];
					bcsServiciosGarantia.ObtenerParidadTipoCambio(data,function(value){
						if (value.p_out_CalculosSalida.length > 0){
							if (maxErrFlag){
								model.CantidadAIngresar = values.CantidadAIngresar;
							}
							
							var montoLoc = value.p_out_CalculosSalida[0].MontoLocal;
							var montoDes = value.p_out_CalculosSalida[0].MontoDestino <= 0? value.p_out_CalculosSalida[0].MontoOrigen:value.p_out_CalculosSalida[0].MontoDestino;
							var montoOri = value.p_out_CalculosSalida[0].MontoOrigen;
							
							//if (Number(montoOri) === Number(montoLoc)){
							if (value.p_out_CalculosSalida[0].IdMonedaOrigen === value.p_out_CalculosSalida[0].IdMonedaDestino){
								//model.MontoAGarantizar = montoLoc;
								MontoAGarantizar = montoLoc;
							}
							else {					
								//model.MontoAGarantizar = montoDes;
								MontoAGarantizar = montoDes;
							}
							//CALCULO DE COBERTURA 	
							var datoSimulacion = [{
								IdReferencial   : 1, 
								IdInstrumento	: model.IdInstrumento,
								Cantidad		: values.CantidadAIngresar, 
								PrecioMercado	: $scope.SesionLocale === 'PE' ? model.PrecioVector : model.PrecioMercado,
								Monto 			: MontoAGarantizar,
								MontoCoberturaSimular : 0
							}];
							
							let idTipoIstrumento = $scope.data.oConfiguracionConceptoGarantia.seleccionado.IdClasificacionTipoInstrumento == 'DV' ? $scope.data.oConfiguracionTipoInstrumentoExigeGarantia.seleccionado.IdTipoInstrumento : $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento;
							let idIstrumento = $scope.data.oConfiguracionConceptoGarantia.seleccionado.IdClasificacionTipoInstrumento == 'DV' ? $scope.data.oConfiguracionInstrumentoExigeGarantia.seleccionado.IdInstrumento : $scope.data.oConfiguracionInstrumento.seleccionado.IdInstrumento;
							
							var datosCobertura = {
								p_IdEmpresa: $scope.SesionIdEmpresa,
								p_IdUsuario: $scope.SesionIdUsuario,
								p_IdModulo: 'CUS',
								p_UsaReglasNegocio: 'S',
								p_IdPortafolio: $scope.data.oClientePortafolio.seleccionado.IdPortafolio,
								p_IdEntidadDestinoGarantia: $scope.data.oConfiguracionEntidadDestinoGarantia.seleccionado.IdEntidadDestinoGarantia,
								p_IdTipoInstrumentoExigeGarantia: idTipoIstrumento,
								p_IdInstrumentoExigeGarantia: idIstrumento,
								p_Simulacion: datoSimulacion,
							};
							bcsServicioPlataforma.Servicios().CalcularCobertura(datosCobertura).$promise.then(function(respuesta){

								if (respuesta.P_OUT_TABLECOBERTURASIMULACION.length > 0){
									model.MontoCoberturaCorredora		= respuesta.P_OUT_TABLECOBERTURASIMULACION[0].MontoCoberturaCorredora;
									model.MontoCoberturaEntidad			= respuesta.P_OUT_TABLECOBERTURASIMULACION[0].MontoCoberturaEntidad;
									model.PorcentajeCoberturaCorredora	= respuesta.P_OUT_TABLECOBERTURASIMULACION[0].PorcentajeCoberturaCorredora;
									model.PorcentajeCoberturaEntidad	= respuesta.P_OUT_TABLECOBERTURASIMULACION[0].PorcentajeCoberturaEntidad;
								}
								/*if ($scope.SesionIdMonedaSistema === IdMonedaOrigen)
								{*/
									if (model.IdClasificacionTipoInstrumento == 'IF') 
									{
										model.MontoAGarantizarMonSist = (values.CantidadAIngresar * model.MontoDisponibleMonedaLocalGarantia)/model.ValorRescateDisponibleGarantia;
									}
									else
									{
										model.MontoAGarantizarMonSist = MontoAGarantizar;
									}
								/*}
								else
								{	
									if (model.IdClasificacionTipoInstrumento == 'IF') 
									{
										model.MontoAGarantizarMonSist = (values.CantidadAIngresar * model.MontoDisponibleMonedaLocalGarantia)/model.MontoDisponibleGarantia;
									}
									else
									{
										model.MontoAGarantizarMonSist = (model.MontoDisponibleMonedaLocalGarantia * values.CantidadAIngresar) / model.CantidadDisponibleGarantia;
									}
								}*/					

								$scope.data.kngTotalMontoGarantizado = TotalMontoGarantizado + Number(model.MontoAGarantizarMonSist||0);
								grillaCustodia.refresh();
							},
							function(respuesta){
								bcsComunes.ventanaOverlay($scope.winID,false);
							});		
							
										
						}
					});
				
				
				break;
				
			case "CTA":
				IdMonedaOrigen  = model.IdMoneda;
				if (values.MontoAGarantizar <= model.Saldo) 
				{	
					MontoAGarantizar = values.MontoAGarantizar;
				} 
				else 
				{
 					bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant("UI_ERROR_EXCEDE_CANTIDAD"));
 					return;
				}
				
			
				//CALCULO DE MONTO GARANTIA A MONEDA LOCAL
				if ($scope.SesionIdMonedaSistema === IdMonedaOrigen)
				{
					model.MontoAGarantizarMonSist = MontoAGarantizar;
					$scope.data.kngTotalMontoGarantizado = TotalMontoGarantizado + Number(model.MontoAGarantizarMonSist||0);
				}
				else
				{		
					/*var Input = [];
					Input.push({"Fecha"				: $scope.SesionFechaSistema,
								"MonedaOrigen"		: IdMonedaOrigen,
								"MontoOrigen"		: values.MontoAGarantizar,
								"MonedaDestino"		: $scope.SesionIdMonedaSistema,
								"TipoCambio"		: 0, 
								"MonedaCambio"		: 'USD', 
								"OrigenTipoCambio"	: 'OBSERVADO'}); //validar*/
					if (values.MontoAGarantizar > 0)
					{
						data.p_IdEmpresa =  $scope.SesionIdEmpresa;
						data.p_IdTrabajo =  1366;
						data.p_CalculosEntrada = [{
							"IdTrabajo"			: data.p_IdTrabajo
							,"IdInterno"		: 1
							,"Fecha"			: $scope.SesionFechaSistema
							,"IdMonedaOrigen" 	: IdMonedaOrigen
							,"IdMonedaDestino" 	: $scope.SesionIdMonedaSistema
							,"ValorCambioLocalModificado" : 0
							,"MontoOrigen"		: values.MontoAGarantizar || 0
							,"IdUso"			: 'CUS'
							//,"TipoCambio" 		: 0
							//,"MonedaCambio"		: $scope.data.IdMonedaMontoDisplay
							//,"OrigenTipoCambio" : 'OBSERVADO'
						}];
						bcsServiciosGarantia.ObtenerParidadTipoCambio(data,function(value){
							if (value.p_out_CalculosSalida.length > 0){
								
								var montoLoc = value.p_out_CalculosSalida[0].MontoLocal;
								var montoDes = value.p_out_CalculosSalida[0].MontoDestino <= 0? value.p_out_CalculosSalida[0].MontoOrigen:value.p_out_CalculosSalida[0].MontoDestino;
								var montoOri = value.p_out_CalculosSalida[0].MontoOrigen;
								
								//if (Number(montoOri) === Number(montoLoc)){
								if (value.p_out_CalculosSalida[0].IdMonedaOrigen === value.p_out_CalculosSalida[0].IdMonedaDestino){
									//model.MontoAGarantizar = montoLoc;
									model.MontoAGarantizarMonSist = Number(montoLoc);
								}
								else {					
									//model.MontoAGarantizar = montoDes;
									model.MontoAGarantizarMonSist = Number(montoDes);
								}
								bcsComunes.ventanaOverlay($scope.ventanaId,true);
								TotalMontoGarantizado = TotalMontoGarantizado + Number(model.MontoAGarantizarMonSist);			
								$scope.data.kngTotalMontoGarantizado = TotalMontoGarantizado;
								bcsComunes.ventanaOverlay($scope.ventanaId,false);
								grillaCtaInv.refresh();	
							}
						});
						
					}	
					
					/*if (Input[0].MontoOrigen > 0)
					{
						var params = {};
						params.p_in_Indicadores = Input;
						ObtenerParidadTipoCambio(params).then(function(Output){
							bcsComunes.ventanaOverlay($scope.ventanaId,true);
							model.MontoAGarantizarMonSist = Number(Output.P_OUT_INDICADORES[0].MontoDestino);
							TotalMontoGarantizado = TotalMontoGarantizado + Number(model.MontoAGarantizarMonSist);			
							$scope.data.kngTotalMontoGarantizado = TotalMontoGarantizado;
							bcsComunes.ventanaOverlay($scope.ventanaId,false);
							grillaCtaInv.refresh();	
						});
					};*/
					
				}	
								
				break;
		}
		
		grillaCustodia.refresh();
		grillaCtaInv.refresh();		
		
		return true;
	};

	$scope.$watch('data.oConfiguracionConceptoGarantia.seleccionado',function(newval,oldval){	
		if ($scope.data.oConfiguracionConceptoGarantia.seleccionado.IdConcepto !== undefined && $scope.data.oConfiguracionConceptoGarantia.seleccionado.IdConcepto !== ''){	
			
			$scope.data.Prendaria = $scope.data.oConfiguracionConceptoGarantia.seleccionado.EsGarantiaPrendaria === 'S' ? true : false;
			
			$scope.data.oConfiguracionTipoInstrumento.idActual 		    	= 0;
			$scope.data.oConfiguracionEntidadDestinoGarantia.idActual   	= 0;			
			$scope.data.oConfiguracionEntidadDestinoGarantia.idPostFiltro   = false;
			$scope.data.oConfiguracionInstrumento.idPostFiltro              = false;
			$scope.oConfiguracionInstrumento = {buscar : "-1"}; 
			$scope.data.oConfiguracionTipoInstrumentoB.idActual 			= 0;
			$scope.ccc.idActual = 0;
 
			
			//Si el concepto tiene configurado la entidad destino garantía, se precarga			
			if ($scope.data.oConfiguracionConceptoGarantia.seleccionado.IdEntidadDestinoGarantia)
			{
				$scope.data.oConfiguracionEntidadDestinoGarantia.idDefecto 			= $scope.data.oConfiguracionConceptoGarantia.seleccionado.IdEntidadDestinoGarantia;
				$scope.data.oConfiguracionEntidadDestinoGarantia.idActual 			= $scope.data.oConfiguracionConceptoGarantia.seleccionado.IdEntidadDestinoGarantia;
				$scope.data.oConfiguracionEntidadDestinoGarantia.idPostFiltro 		= $scope.data.oConfiguracionConceptoGarantia.seleccionado.IdEntidadDestinoGarantia;
			}			
						
			if	($scope.data.oConfiguracionConceptoGarantia.seleccionado.EsGarantiaPrendaria === 'N')
			{ //Ingreso Garantia Prendarias									
				$scope.data.oConfiguracionTipoInstrumento.idDefecto = $scope.data.oConfiguracionConceptoGarantia.seleccionado.IdTipoInstrumento.trim() || '';
				$scope.data.oConfiguracionTipoInstrumento.idActual = $scope.data.oConfiguracionConceptoGarantia.seleccionado.IdTipoInstrumento.trim() || '';
			}
			
			
			//Si el concepto tiene configurado el instrumento, se precarga
			if ($scope.data.oConfiguracionConceptoGarantia.seleccionado.IdInstrumento > 0 )
			{	
				$scope.data.oConfiguracionTipoInstrumento.idDefecto = false;
				$scope.data.oConfiguracionTipoInstrumento.idActual = false;
				
				$scope.data.oConfiguracionInstrumento.idDefecto = false;
				$scope.data.oConfiguracionInstrumento.idActual = false;
				
				$scope.data.oConfiguracionInstrumento.idDefecto    				  = $scope.data.oConfiguracionConceptoGarantia.seleccionado.IdInstrumento;
				$scope.data.oConfiguracionInstrumento.idActual    		          = $scope.data.oConfiguracionConceptoGarantia.seleccionado.IdInstrumento;
				$scope.data.oConfiguracionInstrumento.idPostFiltro 				  = $scope.data.oConfiguracionConceptoGarantia.seleccionado.IdInstrumento;
			}
			else
			{
				$scope.data.oConfiguracionTipoInstrumento.idDefecto = false;
				$scope.data.oConfiguracionTipoInstrumento.idActual = false;
				
				$scope.data.oConfiguracionInstrumento.idDefecto = false;
				$scope.data.oConfiguracionInstrumento.idActual = false;
			}
			
			//Si el Concepto es OPCIONES SOBRE ACCIONES se asigna ACCIONES NACIONALES A COMBO TIPO INSTRUMENTO
			if ($scope.data.oConfiguracionConceptoGarantia.idActual === 'GARDVA'){
				$scope.data.oConfiguracionTipoInstrumentoB.idActual = "ACN";
			}			
			
			dsGrillaGtaCustodia.removeAll();
			dsGrillaGtaCtaInv.removeAll();
		}
	});	

	//Si el Concepto es OPCIONES SOBRE ACCIONES se selecciona un instrumento, asigna EMISOR a la buaqueda
	$scope.$watch('data.oConfiguracionInstrumento.seleccionado',function(newval,oldval){	
		if ($scope.data.oConfiguracionConceptoGarantia.idActual === 'GARDVA'){
			if ($scope.data.oConfiguracionInstrumento.seleccionado !== undefined && $scope.data.oConfiguracionInstrumento.seleccionado !== ''){
				$scope.oConfiguracionInstrumento = {
						buscar : $scope.data.oConfiguracionInstrumento.seleccionado.RazonSocial
					};

				}
			}			
		
		});
	
	
	$scope.$watchGroup(['data.oConfiguracionInstrumentoExigeGarantia.idActual', 
		'data.oConfiguracionEntidadDestinoGarantia.seleccionado'], function(newval, oldval) {
		if (newval !== oldval) {
			$scope.limpiarEdicion();
		}
	});
		
	$scope.limpiarEdicion = function() {
		grillaCustodia.dataSource.data().forEach(item => {
			item.CantidadAIngresar = 0;
			item.MontoAGarantizar = 0;
			item.MontoAGarantizarMonSist = 0;
			item.MontoCoberturaCorredora = 0;
			item.MontoCoberturaEntidad = 0;
			item.PorcentajeCoberturaCorredora = 0;
			item.PorcentajeCoberturaEntidad = 0;
		});
		grillaCustodia.refresh();
	};
    
	//BOTONES 
	$scope.data.buscar = function(){	
		bcsComunes.ventanaOverlay($scope.ventanaId,true);//overlay.encendido = true;

		$scope.data.kngTotalMontoGarantizado = 0;
		var datosCus = parametrosServicios("CUSTODIA");
		var datosCta = parametrosServicios("CTAINV");

		dsGrillaGtaCustodia.setCallParameters(datosCus);
		dsGrillaGtaCtaInv.setCallParameters(datosCta);

		bcsComunes.ventanaOverlay($scope.ventanaId,false);//overlay.encendido = false;
	};

	$scope.data.limpiar = function()
	{						
		$scope.data.oConfiguracionConceptoGarantia.idActual = 0;
		$scope.data.oConfiguracionTipoInstrumento.idActual = 0;
		$scope.data.oConfiguracionInstrumento.idActual = 0;
		$scope.data.oConfiguracionEntidadDestinoGarantia.idDefecto = 0;		
		$scope.data.oConfiguracionEntidadDestinoGarantia.idActual = 0;
		$scope.data.oConfiguracionEntidadDestinoGarantia.idPostFiltro   = false;
		$scope.data.oClientePortafolio.idActual = 0;
		$scope.data.oConfiguracionInstrumento.idActual = false;
		$scope.data.oConfiguracionTipoInstrumentoB.idActual = 0;


		$scope.data.ngFolioOperacion				= 0;
		$scope.data.kngmontoOperacion				= false;
		$scope.data.kngmontoExigibilidadCorredora	= false;				
		$scope.data.kngmontoExigibilidadEntidad		= false;
						

		$scope.data.kngTotalMontoGarantizado = 0;
		$scope.data.kngMonto = 0;
		$scope.data.descripcion = '';

		dsGrillaGtaCustodia.removeAll();
		dsGrillaGtaCtaInv.removeAll();
	};
	
	$scope.data.cancelar = function(refrescar)
	{					
		bcsComunes.ventanaData(idVentana,refrescar);
		bcsComunes.ventanaCerrar(idVentana);
	};
	
	$scope.fnBusquedaFolio = function(registro){

		if (typeof $scope.data.oConfiguracionInstrumento.seleccionado === undefined ||  !$scope.data.oConfiguracionInstrumento.seleccionado){
			bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant("UI_DEBE_SELECCIONAR_OPERACION"));
			return;
		}

		var objInfoOperacion = {};
		objInfoOperacion.IdEmpresa			= $scope.SesionIdEmpresa;
		objInfoOperacion.IdTipoInstrumento	= $scope.data.oConfiguracionInstrumento.seleccionado.IdTipoInstrumento;
		objInfoOperacion.IdTipoOperacion	= $scope.data.oConfiguracionInstrumento.seleccionado ? $scope.data.oConfiguracionInstrumento.seleccionado.IdTipoOperacion : null;
		objInfoOperacion.IdTipoPrestamo		= $scope.data.oConfiguracionInstrumento.seleccionado ? $scope.data.oConfiguracionInstrumento.seleccionado.IdTipoPrestamo : null;
		objInfoOperacion.IdCliente			= $scope.data.oClientePortafolio.seleccionado ? $scope.data.oClientePortafolio.seleccionado.IdCliente : null;	
		objInfoOperacion.IdPortafolio		= $scope.data.oClientePortafolio.seleccionado ? $scope.data.oClientePortafolio.seleccionado.IdPortafolio : null;


		var folioOperacion = bcsComunes.ventanaAbrir("BusquedaOperacionEspecifica", objInfoOperacion, false, false, $scope.winID);
		var promesa = bcsComunes.ventanaPromesa(folioOperacion);
		promesa.then(function(data){
			if (typeof data.Cartera !== 'undefined'){
				//$scope.data.ngFolioOperacion 		= data.Cartera.IdOperacion;
				$scope.data.ngFolioOperacion 		= data.Cartera.FolioCartera;
				$scope.data.kngmontoOperacion 		= data.Cartera.INGMontoATasaCompraActual;
				$scope.data.MontoOperacionSimbolo	= data.Cartera.IdMonedaSistema;
				$scope.data.MontoOperacionDecimales	= (util.bcs_formato(0,'GEN','Monto',$scope.data.MontoOperacionSimbolo,'','')).cantidadDecimales;

				var montoCorredora = 0;
				var montoEntidad = 0;
				for (i=0;i<data.Subyacente.length;i++){
					montoCorredora = montoCorredora + data.Subyacente[i].MontoExigibilidadCorredora;
					montoEntidad = montoEntidad + data.Subyacente[i].MontoExigibilidadEntidad;
				}

				$scope.data.kngmontoExigibilidadCorredora		= montoCorredora;
				$scope.data.MontoExigibilidadCorredoraSimbolo	= data.Cartera.IdMonedaSistema;
				$scope.data.MontoExigibilidadCorredoraDecimales	= (util.bcs_formato(0,'GEN','Monto',$scope.data.MontoExigibilidadCorredoraSimbolo,'','')).cantidadDecimales;

				$scope.data.kngmontoExigibilidadEntidad			= montoEntidad;
				$scope.data.MontoExigibilidadEntidadSimbolo		= data.Cartera.IdMonedaSistema;
				$scope.data.MontoExigibilidadEntidadDecimales	= (util.bcs_formato(0,'GEN','Monto',$scope.data.MontoExigibilidadEntidadSimbolo,'','')).cantidadDecimales;
			}
		});
	}

	
	$scope.data.validarIngresarGarantia = function()
    {
        var RegistrosCta = dsGrillaGtaCtaInv.dataColumns(["Saldo","MontoAGarantizar"],dsGrillaGtaCtaInv.itemsEdited()) || [];
        var validacion = true;
        $.each(RegistrosCta,function(key,value){
            
            if (value.Saldo < value.MontoAGarantizar)
            {
                bcsComunes.ventanaMsgErr( $translate.instant("UI_ERROR_EXCEDE_MONTO") , idVentana );
                validacion = false;
            }
            
            if (value.CantidadAIngresar < 1)
            {   
                bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant("CANTIDAD_ASIGNAR_INVALIDA"));
                validacion = false;
            }   
            
        });
        return(validacion);
    };
	
    
	//LLAMAR SERVICIO DE INGRESO GARANTIA
	$scope.data.ingresarGarantia = function(param)
    {
		validacionCorteOK = true; 
		if ($scope.data.validarIngresarGarantia())
		{		
				bcsComunes.ventanaOverlay($scope.ventanaId,true);//overlay.encendido = true;
				$scope.btnActivo = false;

		 
				//Crear tablas de datos de las grillas				
				var tablaCustodia 		= [];
				var tablaCTA 			= [];		
				var tablaEspecie 		= [];
				
				
				var RegistrosCus = dsGrillaGtaCustodia.dataColumns(
										["IdInstrumento","IdMonedaInstrumento","CantidadAIngresar","CantidadDisponibleGarantia","ValorRescateDisponibleGarantia",
										 "MontoAGarantizar","MontoAGarantizarMonSist","IdClasificacionTipoInstrumento","IdTipoReajuste","NemoSVS","FechaEmisionInstrumento",
										 "FechaVencimientoInstrumento","IdEmisor","PlazoEnDias","EsFungible","IdMonedaInstrumento", "PrecioMercado", "PrecioVaR","CorteMinimo","Instrumento", "IdMonedaEmisionInstrumento"
										],
										dsGrillaGtaCustodia.itemsEdited()
								 	) || [];
				$.each(RegistrosCus,function(key,value){			
					if (value.CantidadAIngresar > 0){
						
						if ( value.IdClasificacionTipoInstrumento === "IF" || value.IdClasificacionTipoInstrumento === "RF")	{
							var cantidad = new Big(value.CantidadAIngresar);
							var corte = new Big(value.CorteMinimo);
							var nominal = cantidad.mod(corte);
							var rescate = new Big(value.CantidadAIngresar);
							nominal = rescate.mod(corte);

							if (nominal > 0){
								bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant("CORTE_MINIMO_INVALIDO"));
								validacionCorteOK = false;							
								return;
							}
							if ((value.IdClasificacionTipoInstrumento === 'IF' && value.CantidadAIngresar > value.ValorRescateDisponibleGarantia) || 
								(value.IdClasificacionTipoInstrumento === 'RF' && value.CantidadAIngresar > value.CantidadDisponibleGarantia) ) 
							{	
								bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant("UI_ERROR_EXCEDE_CANTIDAD"));
								validacionCorteOK = false;
								return;
							}
							
						}
						
						if ($scope.data.oConfiguracionConceptoGarantia.seleccionado.FolioObligatorio=='S' && ($scope.data.ngFolioOperacion == 0 || $scope.data.ngFolioOperacion === undefined)){
							bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant("UI_CONCEPTO_EXIGE_FOLIO_DE_OPERACION"));
							validacionCorteOK = false;
							return;
						}


						var ObjCus = {
							"IdInstrumento"						: value.IdInstrumento,
							"IdMoneda"							: value.IdMonedaInstrumento || '',
							"IdMonedaEmisionInstrumento"		: value.IdMonedaEmisionInstrumento || '',
							"Cantidad"							: (value.IdClasificacionTipoInstrumento === 'IF')? value.EsFungible === 'S'?1:(Number(value.CantidadAIngresar)/Number(value.CorteMinimo)):value.CantidadAIngresar || 0,
							"ValorRescate"						: value.CantidadAIngresar || 0,
			               	"MontoMonedaInstrumento"			: value.MontoAGarantizar || 0,
			               	"MontoMonedaSistema"				: value.MontoAGarantizarMonSist || 0,
							"IdClasificacionTipoInstrumento" 	: value.IdClasificacionTipoInstrumento || '',               	
							"IdTipoReajuste" 					: value.IdTipoReajuste || '',
							"NemoSVS" 		 					: (value.IdClasificacionTipoInstrumento==='IF' ? value.Instrumento : ''),
							"FechaEmisionInstrumento" 			: util.dateSPdate(value.FechaEmisionInstrumento) || '0001-01-01',
							"FechaVencimientoInstrumento" 		: util.dateSPdate(value.FechaVencimientoInstrumento)|| '0001-01-01',
							"IdEmisor" 							: value.IdEmisor || '',
							"PlazoEnDias" 						: value.PlazoEnDias || 0,
							"EsFungible"						: value.EsFungible || 'N',
							"IdCliente"							: $scope.data.oClientePortafolio.seleccionado.IdCliente, 
							"IdPortafolio"						: $scope.data.oClientePortafolio.seleccionado.IdPortafolio,
							"IdAgente" 							: $scope.data.oClientePortafolio.seleccionado.IdEmpleadoEjecutivo || 0,
							"IdTipoInstrumento"					: $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento || '',
							"IdBolsa"							: $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdBolsa || 'BCS',           //BCS  para garantias prendarias
							"IdInstrumentoGarantizado"			: $scope.data.oConfiguracionInstrumento.seleccionado.IdInstrumento || 0,
							"IdConcepto"						: $scope.data.oConfiguracionConceptoGarantia.seleccionado.IdConcepto || '',
							"IdContraparteGarantia"				: $scope.data.oConfiguracionEntidadDestinoGarantia.seleccionado.IdContraparte || 0,
							"Precio"							: value.PrecioMercado || 0,
							"IdTipoOperacion"					: 'INGGTA',
							"IdOperacion"						: $scope.data.ngFolioOperacion || 0,
							"CorteMinimo"						: value.CorteMinimo,
							//"FolioCarteraGarantizada"			: $scope.data.ngFolioOperacion,

							/*
							public Long					IdInstrumento; 
							public String				IdMoneda; 
							public String				IdMonedaEmisionInstrumento;
							public BigDecimal			Cantidad;
							public BigDecimal			ValorRescate;
							public BigDecimal			MontoMonedaInstrumento;
							public BigDecimal			MontoMonedaSistema; 
							public String				IdClasificacionTipoInstrumento; 
							public String				IdTipoReajuste; 
							public String				NemoSVS; 
							public String				FechaEmisionInstrumento; 
							public String				FechaVencimientoInstrumento; 
							public String				IdEmisor; 
							public Long					PlazoEnDias; 
							public String				EsFungible; 
							public Long					IdCliente; 
							public Long					IdPortafolio;
							public Long 				IdAgente;
							public String				IdTipoInstrumento; 
							public String				IdBolsa; 
							public Long					IdInstrumentoGarantizado; 
							public String				IdConcepto; 
							public Long					IdContraparteGarantia;
							public BigDecimal			Precio; 
							public BigDecimal			Tasa;
							public Long					FolioCarteraGarantizada;
							public BigDecimal			CorteMinimo;

							//Si viene del negocio
							public String				IdTipoOperacion; 
							public Long					IdOperacion; 		
							//Si viene de facturación
							public String				IdTipoComprobante; 
							public Long					FolioComprobante; 
							public java.sql.Date		FechaComprobante; */
						};
						tablaCustodia.push(ObjCus);
					}			
				});		
				
				var RegistrosCta = dsGrillaGtaCtaInv.dataColumns(["IdMoneda","MontoAGarantizar","MontoAGarantizarMonSist"],dsGrillaGtaCtaInv.itemsEdited()) || [];
				$.each(RegistrosCta,function(key,value){
					if (value.MontoAGarantizar > 0){
						var ObjCta = {
							"IdMoneda"							: value.IdMoneda || '',
							"MontoMonedaInstrumento"			: value.MontoAGarantizar || 0,
							"MontoMonedaSistema"				: value.MontoAGarantizarMonSist || 0,
							"IdCliente"							: $scope.data.oClientePortafolio.seleccionado.IdCliente, 
							"IdPortafolio"						: $scope.data.oClientePortafolio.seleccionado.IdPortafolio,
							"IdAgente" 							: $scope.data.oClientePortafolio.seleccionado.IdEmpleadoEjecutivo || 0,
							"IdTipoInstrumento"					: $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento || '',
							"IdBolsa"							: $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdBolsa || 'BCS',           //BCS  para garantias prendarias
							"IdInstrumentoGarantizado"			: $scope.data.oConfiguracionInstrumento.seleccionado.IdInstrumento || 0,
							"IdConcepto"						: $scope.data.oConfiguracionConceptoGarantia.seleccionado.IdConcepto || '',
							"IdContraparteGarantia"				: $scope.data.oConfiguracionEntidadDestinoGarantia.seleccionado.IdContraparte || 0,
							"IdTipoOperacion"					: 'INGGTA',
							"IdOperacion"						: $scope.data.ngFolioOperacion || 0
						};
						tablaCTA.push(ObjCta);
					}
				});
				
				
				var ObjEspecie = {
					"IdEspecie"							: $scope.data.oConfiguracionEspecie.seleccionado.IdEspecieGarantia || 0,
					"IdMoneda"							: $scope.SesionIdMonedaSistema || '',
					"MontoMonedaSistema"				: $scope.data.kngMonto || 0,   
					"Monto"								: $scope.data.kngMonto || 0,
					"Glosa"								: $scope.data.descripcion || '',
					"IdCliente"							: $scope.data.oClientePortafolio.seleccionado.IdCliente, 
					"IdPortafolio"						: $scope.data.oClientePortafolio.seleccionado.IdPortafolio,
					"IdAgente" 							: $scope.data.oClientePortafolio.seleccionado.IdEmpleadoEjecutivo || 0,
					"IdTipoInstrumento"					: $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento || '',			
					"IdBolsa"							: $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdBolsa || 'BCS',           //BCS  para garantias prendarias			
					"IdInstrumentoGarantizado"			: $scope.data.oConfiguracionInstrumento.seleccionado.IdInstrumento || 0,
					"IdConcepto"						: $scope.data.oConfiguracionConceptoGarantia.seleccionado.IdConcepto || '',			
					"IdContraparteGarantia"				: $scope.data.oConfiguracionEntidadDestinoGarantia.seleccionado.IdContraparte || 0,
					"IdTipoOperacion"					: 'INGGTA',
					"IdOperacion"						: $scope.data.ngFolioOperacion || 0
				};
				if (ObjEspecie.MontoMonedaSistema > 0 && ObjEspecie.IdEspecie > 0) {
					tablaEspecie.push(ObjEspecie);
				}
				
				
				
				if ((tablaCustodia.length > 0 || tablaCTA.length > 0 || tablaEspecie.length > 0) && validacionCorteOK ) {
					//DATOS SERVICIO
					var datos = {};
					datos.p_IdEmpresa               = $scope.SesionIdEmpresa; 
					datos.p_IdUsuario               = $scope.SesionIdUsuario;
					datos.p_IdModulo                = 'CUS';
					datos.p_IdEmpleado 				= $scope.SesionIdEmpleado;
					datos.p_UsaReglasNegocio        = 'S';
					datos.p_SimulaOperacion			= 'N';
					datos.p_Certifica				= 'N'; //Valor fijo
					datos.p_FechaMovimiento			= util.dateSPdate($scope.data.kngFechaSistema);
					datos.p_GarantiaCustodia		= tablaCustodia;
					datos.p_GarantiaCuentaInversion	= tablaCTA;
					datos.p_GarantiaEspecies		= tablaEspecie;
			
			       	bcsServiciosGarantia.IngresarGarantia(
			       		datos,
			       		function(respuesta){	  		
							bcsComunes.ventanaMsg({
								conflictos:util.parseServerResponse(respuesta)
							},idVentana);
		
							$scope.data.buscar();
							$scope.data.kngTotalMontoGarantizado = 0;
							bcsComunes.ventanaOverlay($scope.ventanaId, false);
							$scope.btnActivo = true;
						},
						function(respuesta){
							bcsComunes.ventanaMsg({
								mensaje:$translate.instant("UI_ERROR_INGRESO_GARANTIA"),
								conflictos:util.parseServerResponse(respuesta)
							},idVentana);
							bcsComunes.ventanaOverlay($scope.ventanaId, false);
							$scope.btnActivo = true;
						}
			       	);
		
				} else{
					bcsComunes.ventanaOverlay($scope.ventanaId,false);
					$scope.btnActivo = true;
					if (validacionCorteOK) {
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_NO_HAY_DATOS_PARA_GUARDAR")				
						},idVentana);
					}

				}		
		}; 

    };

   $timeout(function(){
        //Cuando llegan los datos desde otra ventana(Ej. Cuenta Inversion Cliente)
    	if (typeof parametros.IdCliente !== 'undefined')
    	{
    		$scope.data.oClientePortafolio.idDefecto	= parametros.IdPortafolio;
			$scope.data.oClientePortafolio.idActual		= parametros.IdPortafolio;

    	}
    },1000);

});
