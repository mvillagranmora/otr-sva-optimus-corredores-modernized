angular.module('ModuloCustodia').controller('ConsultarCustodiaVencimientosCtrl',
function($scope,$translate,bcsComunes,bcsServicioPlataforma)
{	
/******************************************************/
/*** INICIO DE VARIABLES Y PRECARGA					***/
/******************************************************/
	
	$scope.filtros = {};
	$scope.grid = {};

	var parametrosConsulta = {};
	var idVentana  = bcsComunes.ventanaWinId($scope);
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	$scope.filtros.kngFechaVencimientoDesde	= util.dateInicioDia($scope.SesionFechaSistema);
	$scope.filtros.kngFechaVencimientoHasta = util.dateInicioDia($scope.SesionFechaSistema);

	$scope.MostrarProducto = BCS_CONFIGURACION.PLATAFORMA["APV.Habilitado"] === 'S' ? true : false;
	
/******************************************************/
/*** FILTROS										***/
/******************************************************/
	
	$scope.filtros.oConfiguracionCliente = {
		configuracion :{},
		filtros: {
			IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},		
		idActual : undefined,
		seleccionado : -1,
		idDefecto : -1
	};
	
	$scope.filtros.oConfiguracionPortafolio = {
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  -1 }
		},
	};
	
	$scope.filtros.oConfiguracionEjecutivo = {
		configuracion : {},
		filtros : {},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdEstado : { "field": "IdEstado","operator": "eq", "value"   :  'ACT' }
		},
	};
	
	$scope.filtros.oConfiguracionClasificacion = {		
		configuracion : {},
		idActual : false,
		seleccionado : false,
		idDefecto : '',
		idPostFiltro : false
	};
	
	$scope.filtros.oConfiguracionTipoInstrumento = {		
		configuracion : {},
		filtros : {
			IdClasificacionTipoInstrumento 	: { "field" : "IdClasificacionTipoInstrumento", "operator": "eq", "value" : ''},
			IdEstado 						: { "field" : "IdEstado",                       "operator": "eq", "value" : 'VIG' },
			NoDerivados						: { "field" : "CodigoTipoInstrumento",          "operator": "neq", "value" : "DER" }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : '',
		idPostFiltro : false
	};
	
	$scope.filtros.oConfiguracionInstrumento = {
		buscar : "-1"
	};
	
	$scope.oSocioNegocio = {
		filtros: {
			IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		}
	};
	
	$scope.oProducto = {
		idActual : false,
		seleccionado : false,			
	};
	
	$scope.oPortafolio = {
		filtros: {
			IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  -1 },
			IdProductoAgrupador : { "field": "IdProductoAgrupador","operator": "eq", "value"   :  undefined }
		},
	};
	
/******************************************************/
/*** FUNCIONES DE CARGA DE TABLAS					***/
/******************************************************/
	
	var filters = {};
	$scope.grid.confCustodiaVencimientos = {
		dataSource  : new bcs.DataSource({ 
						type : "call",
						definition : {
							serverFiltering 	: false, // Filtrado de datos en el servidor.
							serverPaging 		: false, // Paginado de los datos en el servidor.
							serverSorting 		: false, // Ordenamiento de los datos en el servidor.															
							callParameters      : filters,
							pageSize            : 200,
							callTransaction 	: "1780",
							callResultSet 		: "P_OUT_CUSTODIAVENCIMIENTOS",
							schema : {
								model : {
									fields : {
										FechaVencimientoInstrumento			: {type : "sysdate"},
										IdCliente							: {type : "number"},
										DocumentoIdentidad                  : {type : "string"},
										CodigoP                             : {type : "string"},
										NombreCliente						: {type : "string"},
										IdPortafolio						: {type : "number"},
									   	Portafolio							: {type : "string"},
									   	RutCAVALI							: {type : "string"},
										IdTipoComprobante					: {type : "string"},
									   	FolioComprobante					: {type : "number"},
									   	TipoVencimientoTraduccion			: {type : "string"},
									   	
										IdClasificacionTipoInstrumento		: {type : "string"},
										CodigoTipoInstrumento				: {type : "string"},
										Nemotecnico							: {type : "string"},
									   	ISIN								: {type : "string"},
									   	IdEmisor							: {type : "string"},
									   	EsFungible							: {type : "string"},
									   	IdBoveda							: {type : "string"},
									   	NumeroCuentaBoveda					: { type: "string"},
									   	Cantidad							: {type : "number"},
									   	VALORRESCATE						: {type : "number"},										
									   	PrecioCompra						: {type : "number"},
									   	TasaCompra							: {type : "number"},									   	
									   	IdMonedaSistema						: {type : "string"},
									   	IdMonedaMontos						: {type : "string"},
									    MontoInteresCorridoCompraActual		: {type : "number"},
									    MontoReajusteCorridoCompraActual	: {type : "number"},
									   	MontoATasaCompraActual				: {type : "number"},
									   	MontoCompraActualMonedaInstrumento	: {type : "number"},									   	
									   	Folio								: {type : "number"},
									   	NombreTipoCustodia					: {type : "string"},
										NombreProductoAgrupador				: {type : "string"},
										AdminCartera						: {type : "string"},										
										Modalidad    						: {type : "string"},
										MonedaPrincipal						: {type : "string"},
										MonedaSecundaria					: {type : "string"},
										MontoMonedaPrincipal				: {type : "number"},
										MontoMonedaSecundaria				: {type : "number"},
										TCCierre							: {type : "number"},
										ParidadCierre						: {type : "number"},
										MontoEquivalente					: {type : "number"},
										EsPrestamista						: {type : "string"},
										EstadoVencimiento					: {type : "string"},   
									}
								}
							}
						}
					  }),
		columns 	: [	 {field: "FechaVencimientoInstrumento",			title: 'UI_FECHA_VENCIMIENTO',			width: "80px"}	
						,{field: "DocumentoIdentidad",					title: 'UI_DOCUMENTO_IDENTIDAD',		width: "130px"}	
						,{field: "CodigoP",								title: 'UI_CODIGO_PORTAFOLIO_ABR',		width: "130px"}	
						,{field: "NombreCliente",						title: 'UI_CLIENTE',					width: "130px",	groupable: true}
						,{field: "RutCAVALI",							title: 'UI_RUT_CAVALI',					width: "100px",	groupable: true	,	hidden: true}
						,{field: "Portafolio",							title: 'UI_PORTAFOLIO',					width: "100px",	groupable: true}
						,{field: "IdTipoComprobante",					title: 'UI_TIPO_COMPROBANTE',			width: "100px",	groupable: true}
						,{field: "FolioComprobante",					title: 'UI_FOLIO_COMPROBANTE',			width: "100px",	groupable: true}
						,{field: "IdClasificacionTipoInstrumento",		title: 'UI_MERCADO',					width: "40px",	groupable: true}
						,{field: "CodigoTipoInstrumento",				title: 'UI_TIPO_INSTRUMENTO',			width: "50px",	groupable: true}						
						,{field: "Nemotecnico",							title: 'UI_INSTRUMENTO',				width: "100px",	groupable: true}
						,{field: "ISIN", 								title: 'UI_ISIN', 						width: "100px",	groupable: true,	hidden: true}
						,{field: "IdEmisor",							title: 'UI_EMISOR',						width: "80px",	groupable: true}
						,{field: "EsFungible",							title: 'UI_FUNGIBLE',					width: "40px",	groupable: true}
						,{field: "IdBoveda",							title: 'UI_BOVEDA',						width: "40px",	groupable: true}
						,{field: "NumeroCuentaBoveda", 					title: 'UI_NUMERO_CUENTA', 				width: "55px", 	groupable: true,	hidden: true,}
						,{field: "Cantidad",							title: 'UI_CANTIDAD',					width: "65px",	template : " <label> #:  (util.bcs_formato(Cantidad,'GEN','Cantidad',IdMonedaMontos,'','')).valor #</label>"}
						,{field: "VALORRESCATE",						title: 'UI_VALOR_RESCATE',				width: "85px",	template : " <label> #:  (util.bcs_formato(VALORRESCATE,'GEN','Precio',IdMonedaMontos,'','')).valor #</label>",aggregates: ["sum"],groupFooterTemplate: "#= util.formateaNumero(sum,2) #"}
						,{field: "IdMonedaMontos",						title: 'UI_MONEDA_TIPO_REAJUSTE',		width: "40px",	groupable: true}
						,{field: "TasaCompra",							title: 'UI_TASA_COMPRA',				width: "100px",	template : " <label> #:  (util.bcs_formato(TasaCompra,'GEN','Tasa',IdMonedaMontos,IdTipoInstrumento,'')).valor #</label>"}
						,{field: "MontoATasaCompraActual",				title: 'UI_MONTO_PAGO',					width: "100px",	template : " <label> #:  (util.bcs_formato(MontoATasaCompraActual,'GEN','Monto',IdMonedaSistema,'','')).valor #</label>",aggregates: ["sum"],groupFooterTemplate: "#= util.formateaNumero(sum,2) #"}
						,{field: "MontoInteresCorridoCompraActual",		title: 'UI_INTERESES_CORRIDOS', 		width: "105px",	hidden: true,		template : " <label> #:  (util.bcs_formato(MontoInteresCorridoCompraActual,'GEN','Monto',IdMonedaMontos,'','')).valor #</label>",aggregates: ["sum"],groupFooterTemplate: "#= util.formateaNumero(sum,2) #"}
						,{field: "MontoReajusteCorridoCompraActual",	title: 'UI_REAJUSTES_VAC_CORRIDO', 		width: "105px", hidden: true,		template : " <label> #:  (util.bcs_formato(MontoReajusteCorridoCompraActual,'GEN','Monto',IdMonedaMontos,'','')).valor #</label>",aggregates: ["sum"],groupFooterTemplate: "#= util.formateaNumero(sum,2) #"}
						,{field: "TipoVencimientoTraduccion",			title: 'UI_TIPO_VENCIMIENTO',			width: "100px",	groupable: true}
						,{field: "NombreTipoCustodia",					title: 'UI_TIPO_CUSTODIA',				width: "60px",	groupable: true,	hidden: true}
						,{field: "Folio",								title: 'UI_FOLIO_CARTERA',				width: "60px",	template: "#if (( Folio == 0  || (Folio == null)))  {##} else {##=Folio##}#"}
						,{field: "NombreProductoAgrupador", 			title: 'UI_PRODUCTO',		        	width: "80px",  hidden: true}
						,{field: "AdminCartera", 						title: 'UI_ADMIN_CARTERA',		        width: "80px",  groupable: true}
						,{field: "Modalidad", 						    title: 'UI_MODALIDAD',		        	width: "80px"}
						,{field: "MonedaPrincipal", 					title: 'UI_MONEDA_PRINCIPAL',		    width: "80px"} 
						,{field: "MonedaSecundaria", 					title: 'UI_MONEDA_SECUNDARIA',		    width: "80px"}
						,{field: "MontoMonedaPrincipal", 				title: 'UI_MONTO_MONEDA_PRINCIPAL',		width: "80px",aggregates: ["sum"],groupFooterTemplate: "#= util.formateaNumero(sum,2) #"}
						,{field: "MontoMonedaSecundaria", 				title: 'UI_MONTO_MONEDA_SECUNDARIA',	width: "80px",aggregates: ["sum"],groupFooterTemplate: "#= util.formateaNumero(sum,2) #"}
						,{field: "TCCierre", 							title: 'UI_TC_CIERRE',		            width: "80px"  ,template : " <label> #:  (util.bcs_formato(TCCierre,'GEN','Precio',IdMonedaSistema,'','')).valor #</label>"}
						,{field: "ParidadCierre", 						title: 'UI_PARIDAD_CIERRE',		        width: "80px"}
						,{field: "MontoEquivalente", 					title: 'UI_MONTO_EQUIVALENTE',		        width: "80px",  groupable: true ,aggregates: ["sum"],groupFooterTemplate: "#= util.formateaNumero(sum,2) #"}
						,{field: "EsPrestamista", 						title: 'UI_ES_PRESTAMISTA',		        width: "80px",  groupable: true}
						,{field: "EstadoVencimiento",					title: "UI_ESTADO_VENCIMIENTO", 		width: "100px", sortable : true }				       				    
						],	
		gridCreated : function(g) { 
						$scope.grid.ApiCustodiaVencimientos = g;

				    	if($scope.MostrarProducto === true){
					    	g.showColumn("NombreProductoAgrupador");
				    	}

						if ($scope.SesionLocale === 'PE'){
							g.showColumn("RutCAVALI");
							g.showColumn("ISIN");
							g.showColumn("NumeroCuentaBoveda");
							g.showColumn("NombreTipoCustodia");
							g.showColumn("MontoInteresCorridoCompraActual");
							g.showColumn("MontoReajusteCorridoCompraActual");
						}					
					  },
		contextMenu : true,
		filterable	: true,
		sortable 	: true,
		height 		: 477,
		selectable 	: "multiple row",
		autoBind 	: false,
		userSettings: true,
		pageable	: true,
		allowCopy	: { delimeter: "\t" },
		excel 		: { allPages: true },
		rowColorBackground	: function(row) { 
								 if (row.Origen == 'UI_GARANTIA')								 	
								 	return "yellow";
								 return "";
							  }

	};
	
/******************************************************/
/*** FUNCIONES										***/
/******************************************************/
	
	$scope.grid.detalleSubyacentes = function(SelectGrid){
		if (SelectGrid[0].PoseeInstrumentoSubyacente === 'S'){
			bcsComunes.ventanaAbrir("CustodiaSubyacenteModal", SelectGrid[0], false, false, $scope.idVentana);
		} else {
			bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_CARTERA_NO_POSEE_SUBYACENTE"));
			return false;
		}
	};
	
	
	$scope.buscar = function(){
		var filters = {};
		
		//Datos grillas	
		filters.p_IdEmpresa 			= $scope.SesionIdEmpresa;
		filters.p_IdEjecutivo			= (typeof $scope.filtros.oConfiguracionEjecutivo.idActual !== 'undefined' && Number($scope.filtros.oConfiguracionEjecutivo.idActual) !==0) ? Number($scope.filtros.oConfiguracionEjecutivo.seleccionado.IdEmpleadoEjecutivo) : null;
		filters.p_FechaVencimientoDesde	= util.dateSPdate($scope.filtros.kngFechaVencimientoDesde) || '0001-01-01';
		filters.p_FechaVencimientoHasta	= util.dateSPdate($scope.filtros.kngFechaVencimientoHasta) || '0001-01-01';
		filters.p_IdClasificacion		= (typeof $scope.filtros.oConfiguracionClasificacion !== 'undefined' && Number($scope.filtros.oConfiguracionClasificacion.idActual) !==0) ? $scope.filtros.oConfiguracionClasificacion.seleccionado.IdClasificacionTipoInstrumento : null;
		filters.p_IdTipoInstrumento		= (typeof $scope.filtros.oConfiguracionTipoInstrumento !== 'undefined' && Number($scope.filtros.oConfiguracionTipoInstrumento.idActual) !==0) ? $scope.filtros.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento : null;
		filters.p_IdInstrumento			= (typeof $scope.filtros.instrumentoDatos !== 'undefined' && Number($scope.filtros.instrumentoDatos.IdInstrumento) !==0) ? ( ($scope.filtros.instrumentoDatos.ConteoNemotecnico === undefined || $scope.filtros.instrumentoDatos.ConteoNemotecnico === 1) ? $scope.filtros.instrumentoDatos.IdInstrumento : null ) : null;
		filters.p_Nemotecnico			= (typeof $scope.filtros.instrumentoDatos !== 'undefined' && $scope.filtros.instrumentoDatos.Nemotecnico !== '') ? ( ($scope.filtros.instrumentoDatos.ConteoNemotecnico > 1) ? $scope.filtros.instrumentoDatos.Nemotecnico : null) : null;
		
		filters.p_IdCliente				= null;
		filters.p_IdPortafolio			= null;
		filters.p_IdProductoAgrupador	= null;
		filters.p_IdTipoInstrumentoSubyacente   = null;

		if(filters.p_IdTipoInstrumento !== null && (filters.p_IdTipoInstrumento=='SIM' || filters.p_IdTipoInstrumento=='PRT' || filters.p_IdTipoInstrumento=='PAC' || filters.p_IdTipoInstrumento=='EMP') ){
			filters.p_IdTipoInstrumentoSubyacente = filters.p_IdTipoInstrumento;
			filters.p_IdTipoInstrumento = null;
		}
		
		if($scope.MostrarProducto)
		{
			if($scope.oSocioNegocio.seleccionado.IdCliente !== undefined)
				filters.p_IdCliente = Number($scope.oSocioNegocio.seleccionado.IdCliente);
			
			if($scope.oProducto.seleccionado.IdProductoAgrupador !== undefined && $scope.oPortafolio.seleccionado.IdPortafolio !== undefined)
			{
				filters.p_IdPortafolio = Number($scope.oPortafolio.seleccionado.IdPortafolio);
			}
			else if($scope.oProducto.seleccionado.IdProductoAgrupador === undefined && $scope.oPortafolio.seleccionado.IdPortafolio !== undefined)
			{
				filters.p_IdPortafolio = Number($scope.oPortafolio.seleccionado.IdPortafolio);
			}
			else if($scope.oProducto.seleccionado.IdProductoAgrupador !== undefined && $scope.oPortafolio.seleccionado.IdPortafolio === undefined)
			{
				if(Number($scope.oProducto.seleccionado.IdProductoAgrupador) !== 0)
				{
					if(Number($scope.oProducto.seleccionado.IdProductoAgrupador) === 9999)
					{
						filters.p_IdProductoAgrupador = 0;
					}
					else
					{
						filters.p_IdProductoAgrupador = Number($scope.oProducto.seleccionado.IdProductoAgrupador);
					}
				}
			}
		}
		else
		{
			filters.p_IdCliente = (typeof $scope.filtros.oConfiguracionPortafolio !== 'undefined' ) ? Number($scope.filtros.oConfiguracionPortafolio.seleccionado.IdCliente) : null;
			filters.p_IdPortafolio = (typeof $scope.filtros.oConfiguracionPortafolio !== 'undefined' ) ? Number($scope.filtros.oConfiguracionPortafolio.seleccionado.IdPortafolio) : null;
		}
		
		$scope.grid.ApiCustodiaVencimientos.dataSource.setCallParameters(filters);
	};
	
	
	$scope.limpiar = function(){
		$scope.filtros.kngFechaVencimientoDesde	= util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtros.kngFechaVencimientoHasta = util.dateInicioDia($scope.SesionFechaSistema);
		
		$scope.filtros.oConfiguracionCliente.idActual			= false;
		$scope.filtros.oConfiguracionPortafolio.idActual		= 0;
		$scope.filtros.oConfiguracionEjecutivo.idActual			= 0;
		$scope.filtros.oConfiguracionClasificacion.idActual		= 0;
		$scope.filtros.oConfiguracionTipoInstrumento.idActual	= 0;
		$scope.filtros.oConfiguracionInstrumento.buscar			= "-1";
		
		$scope.oSocioNegocio.idActual							= false;
		$scope.oProducto.idActual								= 0;
		$scope.oPortafolio.idActual								= false;
		
		$scope.grid.ApiCustodiaVencimientos.dataSource.removeAll();
	};
	
/******************************************************/
/*** WATCH											***/
/******************************************************/
	
	$scope.$watch('filtros.oConfiguracionCliente.seleccionado',function(newval,oldval){
		if (!$scope.filtros.oConfiguracionCliente.seleccionado) return;
		
		try{
			$scope.filtros.socioGral.idActual = $scope.filtros.oConfiguracionCliente.seleccionado.IdSocioDeNegocio;
		}catch(e){}
	});	
	
	$scope.$watch('oSocioNegocio.seleccionado',function(newval,oldval){
		if (!$scope.oSocioNegocio.seleccionado) return;
		
		try{
			$scope.filtros.socioGral.idActual = $scope.oSocioNegocio.seleccionado.IdSocioDeNegocio;
		}catch(e){}
	});	
	
/******************************************************/
/*** FINAL											***/
/******************************************************/
});