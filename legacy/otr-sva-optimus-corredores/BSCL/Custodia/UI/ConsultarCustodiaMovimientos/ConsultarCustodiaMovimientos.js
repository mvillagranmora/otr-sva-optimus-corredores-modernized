angular.module('ModuloCustodia').controller('ConsultarCustodiaMovimientosCtrl',
function($rootScope, $scope,$state,$translate,$resource,$timeout,bcsComunes,bcsServicioPlataforma,bcsServiciosCustodia)
{	
/******************************************************/
/*** INICIO DE VARIABLES Y PRECARGA					***/
/******************************************************/	
	
	$scope.filtros = {};
	$scope.data ={};
	$scope.data.btnHabilitado = false;
	
	var parametrosConsulta = {};
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	
	$scope.MostrarProducto = BCS_CONFIGURACION.PLATAFORMA["APV.Habilitado"] === 'S' ? true : false;
	
	$scope.data.kngFechaIni = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.data.kngFechaFin = util.dateInicioDia($scope.SesionFechaSistema);
	
	var datos = {};
	datos.p_IdEmpresa = $scope.$root.SesionIdEmpresa; //se obtiene de variables de sistema
	datos.p_IdUsuario = $scope.$root.SesionIdUsuario; //se obtiene de variables de sistema
	datos.p_IdModulo = 'CUS';
	datos.p_UsaReglasNegocio = 'S';
	
/******************************************************/
/*** FILTROS										***/
/******************************************************/
	
	$scope.data.oConfiguracionCliente = {
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
	
	$scope.data.oConfiguracionPortafolio = {
		tipo : "combobox-portafolio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  -1 }
		},
	};
	
	$scope.data.oConfiguracionClasificacion = {
		tipo : "combobox-clasificacion-tipo-instrumento",
		configuracion : {},
		idActual : false,
		seleccionado : false,
		idDefecto : '',
		idPostFiltro : false
	};
	
	$scope.data.oConfiguracionTipoInstrumento = {
		tipo : "combobox-tipo-instrumento",
		configuracion : {},
		filtros : {
			IdClasificacionTipoInstrumento 	: { "field" : "IdClasificacionTipoInstrumento", "operator": "eq", "value" : "" },
			IdEstado 						: { "field" : "IdEstado", 	"operator": "eq", "value" : "VIG" },
			NoDerivados						: { "field" : "IdClasificacionTipoInstrumento", "operator": "neq", "value" : "DV" }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : '',
		idPostFiltro : false
	};
	
	$scope.data.oSocioNegocio = {
		filtros: {
			IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},
	};
	
	$scope.data.oProducto = {
		idActual : false,
		seleccionado : false			
	};
	
	$scope.data.oPortafolio = {
		filtros: {
			IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  -1 },
			IdProductoAgrupador : { "field": "IdProductoAgrupador","operator": "eq", "value"   :  undefined }
		},
	};
	
/******************************************************/
/*** DATASOURCES									***/
/******************************************************/
	
	var dsCustMovimientos = new bcs.DataSource({
		type : "call",
		definition : {
			pageSize		: 100,
			callTransaction 	: "1724",
			callResultSet    : "P_OUT_TABLA",
			schema : {
				model : {
					fields : {
						UsuarioIngresoOperacion			: {type: "string" },
						UsuarioCertificaOperacion  	 	: {type: "string" },
						FechaCertificacion				: {type: "sysdate"},
						IdCartera 						: {type: "number" },
						FechaRegistro 					: {type: "sysdate"},
						FechaOperacion 					: {type: "sysdate"},
						FechaLiquidacion 				: {type: "sysdate"},
						Accion 							: {type: "string" },
						TipoOperacion 					: {type: "string" },
						IdCliente 						: {type: "number" },
						IdPortafolio 					: {type: "number" },
						CodigoPortafolio 				: {type: "string" },
						NombreCliente 					: {type: "string" },
						DocumentoIdentidad 				: {type: "string" },
						IdConcepto 						: {type: "string" },
						NombreConcepto 					: {type: "string" },
						IdClasificacionTipoInstrumento 	: {type: "string" },
						IdTipoInstrumento 				: {type: "string" },
						IdInstrumento 					: {type: "number" },
						Nemotecnico 					: {type: "string" },																	
						IdEmisor 						: {type: "string" },
						FechaVencimientoInstr 			: {type: "sysdate"},
						IdTipoReajuste 					: {type: "string" },
						NemoSVS 						: {type: "string" },
						IdMonedaInstr 					: {type: "string" },
						IdBoveda 						: {type: "string" },
						Precio	 						: {type: "number" },
						Tasa		 					: {type: "number" },
						Monto	 						: {type: "number" },
						ValorRescate					: {type: "number" },
						Saldo							: {type: "number" },
						Cantidad 						: {type: "number" },
						CantidadNeg 					: {type: "number" },
						PrecioTir	 					: {type: "number" },
						FolioCartera					: {type: "number" },
						IdComprobante 					: {type: "number" },
						FolioComprobante				: {type: "number" },
						IdTipoComprobante				: {type: "string" },
						FechaComprobante 				: {type: "sysdate"},
						TipoRegistro 					: {type: "string" },
						NroFila 						: {type: "number" },
	
						ISIN							: {type: "string" },
						IdCuentaCustodioPortafolio		: {type: "string" },
						NumeroCuentaBoveda				: {type: "string" },
						IdTipoCustodia					: {type: "string" },
						NombreTipoCustodia				: {type: "string" },
						Producto						: {type: "string" },
						MontoMercadoMonedaInstrumento	: { type: "number"}
					}
				}
			},
		    group: 
		    [
				{field: "DocumentoIdentidad", dir: "asc"},
				{field: "CodigoPortafolio", dir: "asc"},
				{field: "Nemotecnico", dir: "asc"}
		    ] ,
		    sort:
		    [
		    	{ field: "NroFila", dir: "desc" }
			]
		}
	});
	
	
	$scope.columnasCustMovimientos = function() {
		return  [
	         {field: "DocumentoIdentidad",				title: 'UI_CLIENTE',			width: "40px", 	attributes:{style:"white-space:nowrap;"}}
	        ,{field: "CodigoPortafolio",				title: 'UI_PORTAFOLIO',			width: "40px", 	attributes:{style:"white-space:nowrap;"}}
	        ,{field: "FechaOperacion",					title: 'UI_FECHA',				width: "40px",	attributes:{style:"white-space:nowrap;"}}
	        ,{field: "FechaLiquidacion",				title: 'UI_FECHA_LIQUIDACION',	width: "40px",	attributes:{style:"white-space:nowrap;"}}
	        ,{field: "Accion",							title: 'UI_ACCION_CARTERA',		width: "50px"}
	        ,{field: "TipoOperacion",					title: 'UI_TIPO_OPERACION',		width: "50px",  attributes:{style:"white-space:nowrap;"}}
	        ,{field: "NombreConcepto",					title: 'UI_CONCEPTO',			width: "100px", attributes:{style:"white-space:nowrap;"},	groupable: true}
			,{field: "CantidadNeg",						title: 'UI_CANTIDAD_RESCATE',	width: "70px", 	filterable:false, 	template : " <label> #: util.bcs_formato(CantidadNeg,'GEN','Cantidad','" + $rootScope.SesionIdMonedaSistema + "',IdTipoInstrumento,'').valor #</label>"}
			,{field: "PrecioTir",						title: 'UI_PRECIO_TIR',			width: "70px", 	filterable:false, 	template : " <label> #: util.bcs_formato(PrecioTir,'GEN','Precio','" + $rootScope.SesionIdMonedaSistema + "',IdTipoInstrumento,'').valor #</label>"}			
			,{field: "MontoMercadoMonedaInstrumento",	title: 'UI_MONTO_MONEDA_ORIGEN',width: "90px",						template : " <label> #: util.bcs_formato(MontoMercadoMonedaInstrumento,'GEN','Monto',IdMonedaInstr,'','').valor #</label>"}
            ,{field: "IdMonedaInstr",					title: 'UI_MONEDA', 			width: "55px"}
			,{field: "Nemotecnico",						title: 'UI_INSTRUMENTO',		width: "80px",	attributes:{style:"white-space:nowrap;"},	groupable: true}
			,{field: "ISIN", 							title: 'UI_ISIN', 				width: "100px",	attributes:{style:"white-space:nowrap;"},	groupable: true,	hidden: true }
			,{field: "IdEmisor",						title: 'UI_EMISOR',				width: "80px",	attributes:{style:"white-space:nowrap;"},	groupable: true}
			,{field: "IdClasificacionTipoInstrumento",	title: 'UI_MERCADO',			width: "40px"}
			,{field: "IdTipoInstrumento",				title: 'UI_TIPO_INSTRUMENTO_ABR',width: "40px"}
			,{field: "IdBoveda",						title: 'UI_BOVEDA',				width: "30px"}
			,{field: "NumeroCuentaBoveda",				title: 'UI_NUMERO_CUENTA',		width: "40px",	attributes:{style:"white-space:nowrap;"}}
			,{field: "NombreTipoCustodia",				title: 'UI_TIPO_CUSTODIA',		width: "60px",	attributes:{style:"white-space:nowrap;"}}
			,{field: "FechaComprobante",				title: 'UI_FECHA_COMPROBANTE',	width: "40px",	attributes:{style:"white-space:nowrap;"}}
			,{field: "IdTipoComprobante",				title: 'UI_TIPO',				width: "30px"}
			,{field: "FolioComprobante",				title: 'UI_COMPROBANTE',		width: "40px"}
			,{field: "FolioCartera",					title: 'UI_FOLIO_CARTERA',		width: "40px"}
			,{field: "Producto",						title: 'UI_PRODUCTO',	        width: "100px",	attributes:{style:"white-space:nowrap;"}}
			,{field: "UsuarioIngresoOperacion",			title: 'UI_USUARIO_INGRESO_OPERACION',width: "100px"}
			,{field: "UsuarioCertificaOperacion",		title: 'UI_USUARIO_CERTIFICA_OPERACION', width: "100px"}
			,{field: "FechaCertificacion",				title: 'UI_FECHA_CERTIFICACION',	width: "100px"}
		];
	};
	
/******************************************************/
/*** FUNCIONES DE CARGA DE TABLAS					***/
/******************************************************/
	
   	//GRILLA PRINCIPAL
	$scope.confGrillaCustMovimientos = {
		dataSource	: dsCustMovimientos,
		columns		: $scope.columnasCustMovimientos(),		
		sortable	: true,
		pageable	: { pageSize : 25 },   //pagina localmente
		filterable 	: true,
		selectable 	: "row",
		reorderable	: false,
		userSettings : true,
		allowCopy	: { delimeter: "\t" },
		groupable	: true,
		height		: 526,
		allowCopy	: true,
		resizable	: true,
	    autoBind	: false,
	    hideGroupingHeader: true,
	    gridCreated : function(grid) {

	    	grillaCustMovimientos = grid;

	    	if($scope.MostrarProducto === true){
		    	grid.showColumn("NombreProductoAgrupador");
	    		dsCustMovimientos.read();
	    	}
	    	
	    	if ($scope.SesionLocale === 'PE'){
				grid.showColumn("ISIN");
			}
	    },
	    scrollable	: true
	};
	
/******************************************************/
/*** FUNCIONES										***/
/******************************************************/
	
	$scope.preparaParametros = function(filtros){
		param = {};
		param.p_IdCliente = null;
		param.p_IdPortafolio = null;
		param.p_IdClasificacionTipoInstrumento = "";
		param.p_IdTipoInstrumento = "";
		param.p_IdInstrumento = 0;
		param.p_Nemotecnico = "";
		param.p_IdProductoAgrupador = null;
			
		try{
			if($scope.MostrarProducto)
			{
				if($scope.data.oSocioNegocio.seleccionado.IdCliente !== undefined)
					param.p_IdCliente = Number($scope.data.oSocioNegocio.seleccionado.IdCliente);
				
				if($scope.data.oProducto.seleccionado.IdProductoAgrupador !== undefined && $scope.data.oPortafolio.seleccionado.IdPortafolio !== undefined)
				{
					param.p_IdPortafolio  = Number($scope.data.oPortafolio.seleccionado.IdPortafolio);
				}
				else if($scope.data.oProducto.seleccionado.IdProductoAgrupador === undefined && $scope.data.oPortafolio.seleccionado.IdPortafolio !== undefined)
				{
					param.p_IdPortafolio  = Number($scope.data.oPortafolio.seleccionado.IdPortafolio);
				}
				else if($scope.data.oProducto.seleccionado.IdProductoAgrupador !== undefined && $scope.data.oPortafolio.seleccionado.IdPortafolio === undefined)
				{
					if(Number($scope.data.oProducto.seleccionado.IdProductoAgrupador) !== 0)
					{
						if(Number($scope.data.oProducto.seleccionado.IdProductoAgrupador) === 9999)
						{
							param.p_IdProductoAgrupador = 0;
						}
						else
						{
							param.p_IdProductoAgrupador = Number($scope.data.oProducto.seleccionado.IdProductoAgrupador);
						}
					}
				}
			}
			else
			{
				if ($scope.data.oConfiguracionCliente.seleccionado.IdCliente > 0 && $scope.data.oConfiguracionPortafolio.seleccionado.IdPortafolio > 0)
				{
					param.p_IdCliente = $scope.data.oConfiguracionCliente.seleccionado.IdCliente;
					param.p_IdPortafolio = $scope.data.oConfiguracionPortafolio.seleccionado.IdPortafolio;
				}
				else if ($scope.data.oConfiguracionCliente.seleccionado.IdCliente > 0)
				{
					param.p_IdCliente = $scope.data.oConfiguracionCliente.seleccionado.IdCliente;
					param.p_IdPortafolio = 0;
				}
				else
				{
					param.p_IdCliente = 0;
					param.p_IdPortafolio = 0;
				}
			}
		}catch(e){
			param.p_IdCliente = 0;
			param.p_IdPortafolio = 0;
		}
		
		try{
			if($scope.data.oConfiguracionClasificacion.seleccionado.Codigo !== undefined)
				param.p_IdClasificacionTipoInstrumento = $scope.data.oConfiguracionClasificacion.seleccionado.IdClasificacionTipoInstrumento;	
		}catch(e){}
		
		try{
			if($scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento !== undefined && $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento !== 0)
				param.p_IdTipoInstrumento = $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento;
		}catch (e){}
		
		try {
			if ($scope.data.instrumentoDatos.IdInstrumento > 0 && ($scope.data.instrumentoDatos.ConteoNemotecnico === undefined || $scope.data.instrumentoDatos.ConteoNemotecnico === 1) )
				param.p_IdInstrumento = $scope.data.instrumentoDatos.IdInstrumento;
			else
				param.p_IdInstrumento = 0;
				
			if ($scope.data.instrumentoDatos.Nemotecnico != "" && $scope.data.instrumentoDatos.ConteoNemotecnico > 1)
				param.p_Nemotecnico = $scope.data.instrumentoDatos.Nemotecnico;
			else
				param.p_Nemotecnico = "";
		}catch(e){}
		
		var filtros = [{
			"IdCliente"							: param.p_IdCliente,
			"IdPortafolio"						: param.p_IdPortafolio,
			"FolioCartera"						: 0,
			"IdClasificacionTipoInstrumento" 	: param.p_IdClasificacionTipoInstrumento,
			"IdTipoInstrumento"					: param.p_IdTipoInstrumento,
			"IdInstrumento"						: param.p_IdInstrumento,
			"Nemotecnico"						: param.p_Nemotecnico
		}];
		
		parametrosConsulta = {};
		parametrosConsulta.p_FechaInicio			= util.dateSPdate($scope.data.kngFechaIni);
		parametrosConsulta.p_FechaTermino			= util.dateSPdate($scope.data.kngFechaFin);
		parametrosConsulta.p_IdEmpresa				= $scope.SesionIdEmpresa; // falta sacar de variables de sistema
		parametrosConsulta.p_IdUsuario				= $scope.SesionIdUsuario;
		parametrosConsulta.p_IdModulo				= "CUS";
		parametrosConsulta.p_VariablesProceso		= filtros;
		parametrosConsulta.p_IdProductoAgrupador	= param.p_IdProductoAgrupador;
		parametrosConsulta.p_UIConsulta				= "UI_MOV_CUS";
		
		return parametrosConsulta;
	};
	
	$scope.buscar = function (){
		dsCustMovimientos.setCallParameters($scope.preparaParametros($scope.data));
	};

	$scope.limpiarFiltros = function() {
		$scope.data.oConfiguracionCliente.idActual = false;
		$scope.data.oConfiguracionCliente.seleccionado = false;
		
		$scope.data.oConfiguracionPortafolio.filtros = { IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  -547548 } };
		$scope.data.oConfiguracionPortafolio.idActual = false;
		$scope.data.oConfiguracionPortafolio.seleccionado = false;
		
		$scope.data.oConfiguracionClasificacion.idPostFiltro = false;
		$scope.data.oConfiguracionClasificacion.idActual = false;
		$scope.data.oConfiguracionClasificacion.seleccionado = false;
		
    	$scope.data.oConfiguracionTipoInstrumento.idPostFiltro = false;
    	$scope.data.oConfiguracionTipoInstrumento.idActual = false;
    	$scope.data.oConfiguracionTipoInstrumento.seleccionado = false;

    	$scope.oConfiguracionClasificacionProducto.seleccionado = false;
		$scope.oConfiguracionClasificacionProducto.idActual = 0;
		
    	$scope.data.oConfiguracionInstrumento.buscar = "-1";
    	
		$scope.data.oSocioNegocio.idActual						= false;
		$scope.data.oProducto.idActual							= false;
		$scope.data.oPortafolio.idActual						= false;
	}
	
	var fnHabilita = function(newValue, oldValue) {
		try {
			if($scope.data.instrumentoDatos.IdInstrumento > 0)
				$scope.data.btnHabilitado = true;
		}catch(e){}
	};
	
/******************************************************/
/*** WATCH											***/
/******************************************************/
	
	$scope.$watchGroup(['data.instrumentoDatos.IdInstrumento','data.oConfiguracionCliente.seleccionado'], fnHabilita );

	$scope.$watch('data.oConfiguracionCliente.seleccionado',function(newval,oldval){
		if (!$scope.data.oConfiguracionCliente.seleccionado){
			$scope.data.btnHabilitado = false;
			return;
		}
		
		if($scope.data.oConfiguracionCliente.seleccionado.IdCliente > 0){
			$scope.data.btnHabilitado = true;
		}else{
			$scope.data.btnHabilitado = false;
		}
		
	});
	
	$scope.$watch('data.oSocioNegocio.seleccionado',function(newval,oldval)
	{
		if (!$scope.data.oSocioNegocio.seleccionado){
			$scope.data.btnHabilitado = false;
			return;
		}
		
		try{
			$scope.socioGral.idActual = $scope.data.oSocioNegocio.seleccionado.IdSocioDeNegocio;
			$scope.socioGral.idDefecto = $scope.data.oSocioNegocio.seleccionado.IdSocioDeNegocio;
		}catch(e){}
		
		if($scope.data.oSocioNegocio.seleccionado.IdCliente > 0){
			$scope.data.btnHabilitado = true;
		}else{
			$scope.data.btnHabilitado = false;
		}
	});	
	
/******************************************************/
/*** FINAL											***/
/******************************************************/
});