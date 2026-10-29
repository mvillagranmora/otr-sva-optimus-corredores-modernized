angular.module('ModuloSocioNegocio').controller('ConsultaSocioMovimientosCtrl',function($scope,bcsComunes,$timeout,$compile){
	
	var tabCustodiaLoaded 		= false;
	var tabGarabtiasLoaded 		= false;
	var tabPrestamosLoaded 		= false;
	var tabSimultaneasLoaded 	= false;
	var idVentana 				= bcsComunes.ventanaWinId($scope);;
	var domVentana 				= $("[ventanaid='"+idVentana+"']");
	
	$scope.select = function(grid)
	{
		if (!tabCustodiaLoaded && grid === 'divCustodia') {		
			
		}
		else if (!tabGarabtiasLoaded && grid === 'divGarantias') {
			//domVentana.find('#'+grid).append($compile(MovimientosGarantias)($scope));
			//tabGarabtiasLoaded  = true;
		}
		else if (!tabPrestamosLoaded && grid === 'divPrestamos') {
			//domVentana.find('#'+grid).append($compile(MovimientoPrestamos)($scope));
			//tabPrestamosLoaded  = true;
		}
		else if (!tabSimultaneasLoaded && grid === 'divSimultaneas') {
			//domVentana.find('#'+grid).append($compile(MovimientosSimultaneas)($scope));
			//tabSimultaneasLoaded  = true;
		}
		
		$timeout(function() {
			if ($("#" + grid).find("[kendo-grid]").data("kendo-grid"))
				$("#" + grid).find("[kendo-grid]").data("kendo-grid").refresh()
		},50);
	};
	
	/*****************************************************
	 * 						FILTROS						 *
	 ***************************************************** */
	
	$scope.oConfiguracionCliente = {
		tipo : "combobox-socio-negocio",
		configuracion :{},
		filtros: {
			IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},			
		idActual : undefined,
		seleccionado : -1,
		idDefecto : -1
	};
	
	$scope.oConfiguracionPortafolio = {
		tipo : "combobox-portafolio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  -1 }
		},
	};
	
	$scope.oConfiguracionClasificacionTipoInstrumento = {
		tipo : "combobox-clasificacion-tipo-instrumento",
		configuracion : {},
		filtros : { 	
			NoDerivados 		: { "field"   : "IdClasificacionTipoInstrumento",	"operator": "neq", "value"   : "DV"},
			NoFondosMutuos		: { "field"   : "IdClasificacionTipoInstrumento",	"operator": "neq", "value"   : "FM"},
			NoSpot				: { "field"   : "IdClasificacionTipoInstrumento",	"operator": "neq", "value"   : "SP"},
			NoReportes		 	: { "field"   : "IdClasificacionTipoInstrumento",	"operator": "neq", "value"   : "RE"}
			
		},
		idActual : false,
		seleccionado : false,
		idDefecto : '',
		idPostFiltro : false
	};
	
	$scope.oConfiguracionTipoInstrumento = {
		tipo : "combobox-tipo-instrumento",
		configuracion : {},
		filtros : {
			IdClasificacionTipoInstrumento 	: { "field" : "IdClasificacionTipoInstrumento", "operator": "eq", "value" : "" },
			IdEstado 						: { "field" : "IdEstado",                       "operator": "eq", "value" : "VIG" },
			NoPactos 						: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "PAC" },
			NoPrestamos						: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "PRT" },
			NoEmprestitos					: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "EMP" },
			NoSimultaneas					: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "SIM" },
			NoReportesRV					: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "RRV" },
			NoReportesRF					: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "RRF" },
			NoSpot 							: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "SPT" }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : '',
		idPostFiltro : false
	};
	
	$scope.oConfiguracionInstrumento = {
			buscar : "-1"    
		};
	
	$scope.oSocioNegocio = {
		filtros: {
			IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		}
	};
	
	$scope.oPortafolio = {
		filtros: {
			IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  -1 },
		}
	};
	
	
	$scope.limpiarFiltros = function(){
		
		

	};
	
	
	/*****************************************************
	 * 					PESTAÑA CUSTODIAS				 *
	 ***************************************************** */
	
	$scope.confBasicaCustodias = {
			
		dataSource: new bcs.DataSource({
			type : "odata", 
			definition : {
						transport: {
							read: {
								url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=700031",
								dataType: "json"
							}
						},
						serverFiltering : true, // Filtrado de datos en el servidor.
						pageSize: 10, 			// Número de registros por página.
						schema : {
							model: {
								fields : {
									Secuencia 		 : {type: "string"}, 
									Estado 			 : {type: "string"},
									Instrumento		 : {type: "string"},
									Tipo_Reajuste	 : {type: "string"},
									Fecha_Liquidacion: {type: "sysdate"},
									Fecha_Vencimiento: {type: "sysdate"},
									Cantidad		 : {type: "number"},
									Rescate			 : {type: "number"},
									Tasa 			 : {type: "number"},
									Precio			 : {type: "number"},
									Monto 			 : {type: "number"},
									Concepto		 : {type: "string"},
									Desc_Movimiento	 : {type: "string"},
									Boveda	 		 : {type: "string"},
									Cod_Mov			 : {type: "string"},
									Cliente			 : {type: "string"},
									Mercado			 : {type: "string"},
									TipoInstumento	 : {type: "string"}
								}
							}
						},
						/*filter: 
							logic 	: "or",
							filters : [
								//
								 { field: "Cod_Mov", operator: "eq", value: 'ANCACN' },
								 { field: "Cod_Mov", operator: "eq", value: 'ANCRF'  },
								 { field: "Cod_Mov", operator: "eq", value: 'ANUPAC' },
								 { field: "Cod_Mov", operator: "eq", value: 'ANVACN' },
								 { field: "Cod_Mov", operator: "eq", value: 'COMACN' },
								 { field: "Cod_Mov", operator: "eq", value: 'COMACX' },
								 { field: "Cod_Mov", operator: "eq", value: 'COMIIF' },
								 { field: "Cod_Mov", operator: "eq", value: 'COMPAC' },
								 { field: "Cod_Mov", operator: "eq", value: 'COMRF'  },
								 { field: "Cod_Mov", operator: "eq", value: 'COMTER' },
								 { field: "Cod_Mov", operator: "eq", value: 'INGTIT' },
								 { field: "Cod_Mov", operator: "eq", value: 'RETTIT' },
								 { field: "Cod_Mov", operator: "eq", value: 'SORLET' },
								 { field: "Cod_Mov", operator: "eq", value: 'VCDTIT' },
								 { field: "Cod_Mov", operator: "eq", value: 'VENACN' },
								 { field: "Cod_Mov", operator: "eq", value: 'VENACX' },
								 { field: "Cod_Mov", operator: "eq", value: 'VENIIF' },
								 { field: "Cod_Mov", operator: "eq", value: 'VENPAC' },
								 { field: "Cod_Mov", operator: "eq", value: 'VENRF'  },
								 { field: "Cod_Mov", operator: "eq", value: 'VENTER' }
							]					    	
						}*/
					}
		}),
		gridCreated: function(grid) {
			$scope.gridCustodia = grid;
		},
		columns: [
					{field : "Secuencia",			title : "Secuencia", 			width : "90px"	},
					{field : "Estado",				title : "Estado", 				width : "90px"	},
					{field : "Instrumento",			title : "Instrumento", 			width : "90px"	},
		          	{field : "Tipo_Reajuste",		title : "Tipo reajuste", 		width : "90px"	},
		          	{field : "Fecha_Liquidacion",	title : "Fecha Liquidacion",	width : "90px" 	},
					{field : "Fecha_Vencimiento",	title : "Fecha Vencimiento",	width : "90px"	},
					{field : "Cantidad",			title : "Cantidad",				width : "90px"	,align: "rigth" ,template : " <label> #:  (util.bcs_formato(Cantidad,'ORD','Monto','CLP','','')).valor #</label>"},
					{field : "Rescate",				title : "Rescate",				width : "90px"	,align: "rigth" },
					{field : "Tasa", 				title : "Tasa",					width : "90px"	,align: "rigth" },
					{field : "Precio", 				title : "Precio",				width : "90px"	,align: "rigth" ,template : " <label> #:  (util.bcs_formato(Precio,'ORD','Monto','CLP','','')).valor #</label>"},
					{field : "Monto", 				title : "Monto",				width : "90px"	,align: "rigth" ,template : " <label> #:  (util.bcs_formato(Monto,'ORD','Monto','CLP','','')).valor #</label>"},
					{field : "Concepto", 			title : "Concepto",				width : "90px"	},
					{field : "Desc_Movimiento", 	title : "Desc. Movimiento",		width : "350px"	},
					{field : "Boveda", 				title : "Boveda",				width : "90px"	},
					{field : "Cod_Mov", 			title : "Mov",					width : "90px"	}
				],
		sortable: true,
		pageable: true,
		filterable: false,
		scrollable: true,
		height: 400,
		userSettings:true,
		navigatable: true,
  		autoBind : false
	};
	
	/*****************************************************
	 * 					PESTAÑA GARANTIAS				 *
	 ***************************************************** */
	
	$scope.confBasicaGarantias = {
		
		dataSource: new bcs.DataSource({
			type : "odata", 
			definition : {
						transport: {
							read: {
								url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=700031",
								dataType: "json"
							}
						},
						serverFiltering : true, // Filtrado de datos en el servidor.
						pageSize: 200, // Número de registros por página.
						schema : {
							model: {
								fields : {
									Secuencia 		 : {type: "string"}, 
									Estado 			 : {type: "string"},
									Instrumento		 : {type: "string"},
									Tipo_Reajuste	 : {type: "string"},
									Fecha_Liquidacion: {type: "sysdate"},
									Fecha_Vencimiento: {type: "sysdate"},
									Cantidad		 : {type: "number"},
									Rescate			 : {type: "number"},
									Tasa 			 : {type: "number"},
									Precio			 : {type: "number"},
									Monto 			 : {type: "number"},
									Concepto		 : {type: "string"},
									Desc_Movimiento	 : {type: "string"},
									Boveda	 		 : {type: "string"},
									Cod_Mov			 : {type: "string"},
									Cliente			 : {type: "string"},
									Mercado			 : {type: "string"},
									TipoInstumento	 : {type: "string"}
								}
							}
						}
						/*,filter: {
							logic 	: "or",
							filters : [
								//
								 { field: "Cod_Mov", operator: "eq", value: 'INGGTA'  },
								 { field: "Cod_Mov", operator: "eq", value: 'INGGTA'  }
							]					    	
						}*/
					}
		}),
		gridCreated: function(grid) {
			$scope.gridGarantias = grid;
		},
		columns: [
			{field : "Secuencia",			title : "Secuencia", 			width : "90px"	},
			{field : "Estado",				title : "Estado", 				width : "90px"	},
			{field : "Instrumento",			title : "Instrumento", 			width : "90px"	},
          	{field : "Tipo_Reajuste",		title : "Tipo reajuste", 		width : "90px"	},
          	{field : "Fecha_Liquidacion",	title : "Fecha Liquidacion",	width : "90px" 	},
			{field : "Fecha_Vencimiento",	title : "Fecha Vencimiento",	width : "90px"	},
			{field : "Cantidad",			title : "Cantidad",				width : "90px"	,align: "rigth" ,template : " <label> #:  (util.bcs_formato(Cantidad,'ORD','Monto','CLP','','')).valor #</label>"},
			{field : "Rescate",				title : "Rescate",				width : "90px"	,align: "rigth" },
			{field : "Tasa", 				title : "Tasa",					width : "90px"	,align: "rigth" },
			{field : "Precio", 				title : "Precio",				width : "90px"	,align: "rigth" ,template : " <label> #:  (util.bcs_formato(Precio,'ORD','Monto','CLP','','')).valor #</label>"},
			{field : "Monto", 				title : "Monto",				width : "90px"	,align: "rigth" ,template : " <label> #:  (util.bcs_formato(Monto,'ORD','Monto','CLP','','')).valor #</label>"},
			{field : "Concepto", 			title : "Concepto",				width : "90px"	},
			{field : "Desc_Movimiento", 	title : "Desc. Movimiento",		width : "350px"	},
			{field : "Boveda", 				title : "Boveda",				width : "90px"	},
			{field : "Cod_Mov", 			title : "Mov",					width : "90px"	}
		],
		sortable: true,
		pageable: true,
		filterable: false,
		scrollable: true,
		height: 400,
		selectable 			: "multiple row",
		userSettings:true,
		navigatable: true,
  		autoBind : false
	};
	
	/*****************************************************
	 * 					PESTAÑA PRESTAMOS				 *
	 ***************************************************** */
		
	$scope.confBasicaPrestamos = {
			
		dataSource: new bcs.DataSource({
			type : "odata", 
			definition : {
						transport: {
							read: {
								url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=700031",
								dataType: "json"
							}
						},
						serverFiltering : true, // Filtrado de datos en el servidor.
						pageSize: 200, // Número de registros por página.
						schema : {
							model: {
								fields : {
									Secuencia 		 : {type: "string"}, 
									Estado 			 : {type: "string"},
									Instrumento		 : {type: "string"},
									Tipo_Reajuste	 : {type: "string"},
									Fecha_Liquidacion: {type: "sysdate"},
									Fecha_Vencimiento: {type: "sysdate"},
									Cantidad		 : {type: "number"},
									Rescate			 : {type: "number"},
									Tasa 			 : {type: "number"},
									Precio			 : {type: "number"},
									Monto 			 : {type: "number"},
									Concepto		 : {type: "string"},
									Desc_Movimiento	 : {type: "string"},
									Boveda	 		 : {type: "string"},
									Cod_Mov			 : {type: "string"},
									Cliente			 : {type: "string"},
									Mercado			 : {type: "string"},
									TipoInstumento	 : {type: "string"}
								}
							}
						}
						/*,filter: {
							logic 	: "or",
							filters : [
							 { field: "Cod_Mov", operator: "eq", value: 'ANTEMP' },
							 { field: "Cod_Mov", operator: "eq", value: 'ANUEMP' },
							 { field: "Cod_Mov", operator: "eq", value: 'EMPRES' },
							 { field: "Cod_Mov", operator: "eq", value: 'PRESTA' },
							 { field: "Cod_Mov", operator: "eq", value: 'VENEMP' },
							 { field: "Cod_Mov", operator: "eq", value: 'VENPRT' }
							 ]
						}*/
					}
		}),
		gridCreated: function(grid) {
			$scope.gridPrestamos = grid;
		},
		columns: [
			{field : "Secuencia",			title : "Secuencia", 			width : "90px"	},
			{field : "Estado",				title : "Estado", 				width : "90px"	},
			{field : "Instrumento",			title : "Instrumento", 			width : "90px"	},
          	{field : "Tipo_Reajuste",		title : "Tipo reajuste", 		width : "90px"	},
          	{field : "Fecha_Liquidacion",	title : "Fecha Liquidacion",	width : "90px" 	},
			{field : "Fecha_Vencimiento",	title : "Fecha Vencimiento",	width : "90px"	},
			{field : "Cantidad",			title : "Cantidad",				width : "90px"	,align: "rigth" ,template : " <label> #:  (util.bcs_formato(Cantidad,'ORD','Monto','CLP','','')).valor #</label>"},
			{field : "Rescate",				title : "Rescate",				width : "90px"	,align: "rigth" },
			{field : "Tasa", 				title : "Tasa",					width : "90px"	,align: "rigth" },
			{field : "Precio", 				title : "Precio",				width : "90px"	,align: "rigth" ,template : " <label> #:  (util.bcs_formato(Precio,'ORD','Monto','CLP','','')).valor #</label>"},
			{field : "Monto", 				title : "Monto",				width : "90px"	,align: "rigth" ,template : " <label> #:  (util.bcs_formato(Monto,'ORD','Monto','CLP','','')).valor #</label>"},
			{field : "Concepto", 			title : "Concepto",				width : "90px"	},
			{field : "Desc_Movimiento", 	title : "Desc. Movimiento",		width : "350px"	},
			{field : "Boveda", 				title : "Boveda",				width : "90px"	},
			{field : "Cod_Mov", 			title : "Mov",					width : "90px"	}
		],
		sortable: true,
		pageable: true,
		filterable: false,
		scrollable: true,
		height: 400,
		selectable 			: "multiple row",
		userSettings:true,
		navigatable: true,
  		autoBind : false
	};		
		
	/*****************************************************
	 * 					PESTAÑA SIMULTANEAS				 *
	 ***************************************************** */
	$scope.confBasicaSimultaneas = {
		
		dataSource: new bcs.DataSource({
			type : "odata", 
			definition : {
						transport: {
							read: {
								url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=700031",
								dataType: "json"
							}
						},
						serverFiltering : true, // Filtrado de datos en el servidor.
						pageSize: 200, // Número de registros por página.
						schema : {
							model: {
								fields : {
									Secuencia 		 : {type: "string"}, 
									Estado 			 : {type: "string"},
									Instrumento		 : {type: "string"},
									Tipo_Reajuste	 : {type: "string"},
									Fecha_Liquidacion: {type: "sysdate"},
									Fecha_Vencimiento: {type: "sysdate"},
									Cantidad		 : {type: "number"},
									Rescate			 : {type: "number"},
									Tasa 			 : {type: "number"},
									Precio			 : {type: "number"},
									Monto 			 : {type: "number"},
									Concepto		 : {type: "string"},
									Desc_Movimiento	 : {type: "string"},
									Boveda	 		 : {type: "string"},
									Cod_Mov			 : {type: "string"},
									Cliente			 : {type: "string"},
									Mercado			 : {type: "string"},
									TipoInstumento	 : {type: "string"}
								}
							}
						}
						/*,filter: {
							logic 	: "or",
							filters : [
							 { field: "Cod_Mov", operator: "eq", value: 'ANSIMC' },
							 { field: "Cod_Mov", operator: "eq", value: 'COMSIM' },
							 { field: "Cod_Mov", operator: "eq", value: 'VENSIM' }
							 ]
						}*/
					}
		}),
		gridCreated: function(grid) {
			$scope.gridSimultaneas = grid;
		},
		columns: [
			{field : "Secuencia",			title : "Secuencia", 			width : "90px"	},
			{field : "Estado",				title : "Estado", 				width : "90px"	},
			{field : "Instrumento",			title : "Instrumento", 			width : "90px"	},
          	{field : "Tipo_Reajuste",		title : "Tipo reajuste", 		width : "90px"	},
          	{field : "Fecha_Liquidacion",	title : "Fecha Liquidacion",	width : "90px" 	},
			{field : "Fecha_Vencimiento",	title : "Fecha Vencimiento",	width : "90px"	},
			{field : "Cantidad",			title : "Cantidad",				width : "90px"	,align: "rigth" ,template : " <label> #:  (util.bcs_formato(Cantidad,'ORD','Monto','CLP','','')).valor #</label>"},
			{field : "Rescate",				title : "Rescate",				width : "90px"	,align: "rigth" },
			{field : "Tasa", 				title : "Tasa",					width : "90px"	,align: "rigth" },
			{field : "Precio", 				title : "Precio",				width : "90px"	,align: "rigth" ,template : " <label> #:  (util.bcs_formato(Precio,'ORD','Monto','CLP','','')).valor #</label>"},
			{field : "Monto", 				title : "Monto",				width : "90px"	,align: "rigth" ,template : " <label> #:  (util.bcs_formato(Monto,'ORD','Monto','CLP','','')).valor #</label>"},
			{field : "Concepto", 			title : "Concepto",				width : "90px"	},
			{field : "Desc_Movimiento", 	title : "Desc. Movimiento",		width : "350px"	},
			{field : "Boveda", 				title : "Boveda",				width : "90px"	},
			{field : "Cod_Mov", 			title : "Mov",					width : "90px"	}
		],
		sortable: true,
		pageable: true,
		filterable: false,
		scrollable: true,
		height: 400,
		selectable 			: "multiple row",
		userSettings:true,
		navigatable: true,
  		autoBind : false
	};

	 
	/*****************************************************
	 * 					BOTON BUSCAR					 *
	 *****************************************************/

	$scope.Buscar = function(){			
		var ObjFiltrosC = $scope.getFiltroCustodia();
		var ObjFiltrosG = $scope.getFiltroGarantias();
		var ObjFiltrosP = $scope.getFiltroPrestamos();
		var ObjFiltrosS = $scope.getFiltroSimultaneas();
		
		$scope.gridCustodia.dataSource.filter(ObjFiltrosC);
		$scope.gridGarantias.dataSource.filter(ObjFiltrosG);
		$scope.gridPrestamos.dataSource.filter(ObjFiltrosP);
		$scope.gridSimultaneas.dataSource.filter(ObjFiltrosS);
	};


	$scope.getFiltros = function(){
		var ObjFiltro = { };
		var Filtros 	= new Array();
		
		ObjFiltro.logic 	= 'and';
		ObjFiltro.filters 	= Filtros;
		
		if($scope.oSocioNegocio.seleccionado.IdCliente					!== undefined)	{Filtros.push({ field: "Cliente"			,operator: "eq",  value: $scope.oSocioNegocio.seleccionado.DocumentoIdentidadDefecto });}
		if($scope.instrumentoDatos 										!== undefined)	{Filtros.push({ field: "Instrumento"		,operator: "eq",  value: $scope.instrumentoDatos.Nemotecnico });}
		if($scope.oConfiguracionClasificacionTipoInstrumento.idActual 	!== 0) 			{Filtros.push({ field: "Mercado"			,operator: "eq",  value: $scope.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento });}
		if($scope.oConfiguracionTipoInstrumento.idActual				!== 0)			{Filtros.push({ field: "TipoInstumento"		,operator: "eq",  value: $scope.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento });}
		if($scope.filtro.fecIniciong									!== "")			{Filtros.push({ field: "Fecha_Liquidacion"	,operator: "gte", value: $scope.filtro.fecInicio });}
		if($scope.filtro.fecTerminong									!== "")			{Filtros.push({ field: "Fecha_Liquidacion"	,operator: "lte", value: $scope.filtro.fecTermino });}
		
		return ObjFiltro;
	}
	
	/*****************************************************
	 * 					FILTROS BASICOS					 *
	 *****************************************************/
	
	$scope.getFiltroCustodia = function(){
		
		var ObjFiltroC 	= { };
		ObjFiltroC		= $scope.getFiltros();

		
		var ObjFiltro 	= { };				
		var Filtros 	= new Array();
		
		ObjFiltro.logic 	= 'or';
		ObjFiltro.filters 	= Filtros;
		
		Filtros.push({ field: "Cod_Mov", operator: "eq", value: 'ANCACN' });
		Filtros.push({ field: "Cod_Mov", operator: "eq", value: 'ANCRF'  });
		Filtros.push({ field: "Cod_Mov", operator: "eq", value: 'ANUCPA' });
		Filtros.push({ field: "Cod_Mov", operator: "eq", value: 'ANUVPA' });
		Filtros.push({ field: "Cod_Mov", operator: "eq", value: 'ANVACN' });
		Filtros.push({ field: "Cod_Mov", operator: "eq", value: 'COMACN' });
		Filtros.push({ field: "Cod_Mov", operator: "eq", value: 'COMIIF' });
		Filtros.push({ field: "Cod_Mov", operator: "eq", value: 'COMPAC' });
		Filtros.push({ field: "Cod_Mov", operator: "eq", value: 'COMRF'  });
		Filtros.push({ field: "Cod_Mov", operator: "eq", value: 'COMTER' });
		Filtros.push({ field: "Cod_Mov", operator: "eq", value: 'INGTIT' });
		Filtros.push({ field: "Cod_Mov", operator: "eq", value: 'RETTIT' });
		Filtros.push({ field: "Cod_Mov", operator: "eq", value: 'SORLET' });
		Filtros.push({ field: "Cod_Mov", operator: "eq", value: 'VCDTIT' });
		Filtros.push({ field: "Cod_Mov", operator: "eq", value: 'VENACN' });
		Filtros.push({ field: "Cod_Mov", operator: "eq", value: 'VENACX' });
		Filtros.push({ field: "Cod_Mov", operator: "eq", value: 'VENIIF' });
		Filtros.push({ field: "Cod_Mov", operator: "eq", value: 'VENPAC' });
		Filtros.push({ field: "Cod_Mov", operator: "eq", value: 'VENRF'  });
		Filtros.push({ field: "Cod_Mov", operator: "eq", value: 'VENTER' });
		
		ObjFiltroC.filters.push(ObjFiltro);
		
		return ObjFiltroC;
	}
	
	$scope.getFiltroGarantias = function(){
		
		var ObjFiltroG 	= { };
		ObjFiltroG		= $scope.getFiltros();
		
		var ObjFiltro  = { };
		var Filtros 	= new Array();
		
		ObjFiltro.logic 	= 'or';
		ObjFiltro.filters 	= Filtros;
		
		Filtros.push({ field: "Cod_Mov", operator: "eq", value: 'INGGTA'  });
		Filtros.push({ field: "Cod_Mov", operator: "eq", value: 'INGGTA'  });
		
		ObjFiltroG.filters.push(ObjFiltro);
		
		return ObjFiltroG;
	}
	
	$scope.getFiltroPrestamos = function(){
		
		var ObjFiltroP 	= { };
		ObjFiltroP		= $scope.getFiltros();
		
		var ObjFiltro = { };
		var Filtros 	= new Array();
		
		ObjFiltro.logic 	= 'or';
		ObjFiltro.filters 	= Filtros;
		
		Filtros.push({ field: "Cod_Mov", operator: "eq", value: 'ANTEMP' });
		Filtros.push({ field: "Cod_Mov", operator: "eq", value: 'ANUEMP' });
		Filtros.push({ field: "Cod_Mov", operator: "eq", value: 'EMPRES' });
		Filtros.push({ field: "Cod_Mov", operator: "eq", value: 'PRESTA' });
		Filtros.push({ field: "Cod_Mov", operator: "eq", value: 'VENEMP' });
		Filtros.push({ field: "Cod_Mov", operator: "eq", value: 'VENPRT' });
		
		ObjFiltroP.filters.push(ObjFiltro);
		
		return ObjFiltroP;
	}
	
	$scope.getFiltroSimultaneas = function(){
		
		var ObjFiltroS 	= { };
		ObjFiltroS		= $scope.getFiltros();
		
		var ObjFiltro 	= { };
		var Filtros 	= new Array();
		
		ObjFiltro.logic 	= 'or';
		ObjFiltro.filters 	= Filtros;
		
		Filtros.push({ field: "Cod_Mov", operator: "eq", value: 'ANSIMC' });
		Filtros.push({ field: "Cod_Mov", operator: "eq", value: 'COMSIM' });
		Filtros.push({ field: "Cod_Mov", operator: "eq", value: 'VENSIM' });
		
		ObjFiltroS.filters.push(ObjFiltro);

		return ObjFiltroS;
	}
	
	
});
	

	
	
	
	
		
	
	
	

