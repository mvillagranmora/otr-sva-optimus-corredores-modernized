angular.module('ModuloOrdenes').controller('ConsultarOrdenesCtrl',
function($scope,$translate,bcsComunes,bcsConfiguracionOrdenesV2,bcsConsultasOrdenes,bcsConfiguracionOrdenes,bcsServicioOrden,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioReportes,bcsServicioPlataforma)
{
/******************************************************/
/*** INICIO DE VARIABLES Y PRECARGA					***/
/******************************************************/
	
	var controllerName = 'ConsultarOrdenesCtrl';
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	
	$scope.datos = {};
	$scope.filtros = {};
	$scope.defecto = {};
	bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise('CUS.PrestamosRentaFija', $scope.SesionIdEmpresa, 'PLATAFORMA')
	.then(function (parametroRentaFijaVentaCorta) 
	{
		$scope.PermiteVentaCortaRF = parametroRentaFijaVentaCorta.Valor == 'S';
	});
	bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise('ORD.OrdenVentaCortaRentaFija', $scope.SesionIdEmpresa, 'PLATAFORMA')
	.then(function (parametroOrdenVentaCortaRentaFija) 
	{
		$scope.PermiteIngresoOrdenVentaCortaRentaFija = parametroOrdenVentaCortaRentaFija.Valor === 'S';
	});
	var parametros = bcsComunes.ventanaParams($scope.idVentana);
	
	if (parametros.IdLibroOrdenes)
		$scope.defecto.IdLibroOrdenes = parametros.IdLibroOrdenes;
	
	if (parametros.OrdenesVigentes)
		$scope.filtros.OrdenesVigentes = true;
	
	$scope.MostrarProducto = BCS_CONFIGURACION.PLATAFORMA["APV.Habilitado"] === 'S' ? true : false;

	$scope.sumaCantidad = 0;
	$scope.sumaMonto = 0;
	
	var filters = {};
/******************************************************/
/*** FILTROS										***/
/******************************************************/
	
	$scope.datos.filtrosClasificacion = {
		logic : "or",
		RV : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "RV"},
		DV : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "DV"},
		RF : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "RF"},
		IF : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "IF"},
 	};
	
 	$scope.datos.filtrosTipoInstrumento = {
 		IdClasificacionTipoInstrumento : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": ""},
 		IdTipoInstrumento : {"field": "IdTipoInstrumento","operator": "neq","value": "FWD"}
 	};
 	
	$scope.datos.filtrosOperacion = {
 		EsOrden : {"field": "EsOrden","operator": "eq","value": "S"}    	
 	};
	
	$scope.datos.filtrosCondicionDeLiquidacion = {
 		AccionTipoOperacion : {"field": "AccionTipoOperacion","operator": "eq","value": ""}    
 	};
	
	 $scope.datos.filtrosMonedaNegociacion = {
		IdMoneda : {"field": "IdMoneda","operator": "eq","value": ""}    
	};

 	$scope.datos.filtrosLibroDeOrdenes = {
 		IdEmpresa : { field : "IdEmpresa", operator : "eq", value: $scope.SesionIdEmpresa }
 	};
 	
	$scope.CanalCliente	= { "field" : "IdTipoCanalIngreso","operator": "eq", "value"   : "CLI" };
	$scope.CanalTrader	= { "field" : "IdTipoCanalIngreso","operator": "eq", "value"   : "TRA" };
	$scope.CanalAgente	= { "field" : "IdTipoCanalIngreso","operator": "eq", "value"   : "AGE" };
	$scope.CanalOWP		= { "field" : "IdTipoCanalIngreso","operator": "eq", "value"   : "OWP" };
	
	$scope.datos.filtrosCanalIngreso = {
		complejo : [$scope.CanalCliente,$scope.CanalTrader,$scope.CanalAgente,$scope.CanalOWP],
		logic : "or",
		filters : [
			$scope.CanalCliente,$scope.CanalTrader,$scope.CanalAgente, $scope.CanalOWP
		]
	};
	
	$scope.oSocioNegocio = {
		filtros: {
			IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},
	};
	
	//Con Producto
	$scope.oProducto = {
		idActual : false,
		seleccionado : false,			
	};
	
	$scope.oPortafolio = {
		filtros: {
			IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 },
			IdProductoAgrupador : { "field": "IdProductoAgrupador","operator": "eq", "value"   :  0 }
		},
	};

	$scope.datos.filtrosInstrumentos = {
		IdClasificacionTipoInstrumento : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": ""},
		IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value"   : "" }
	};
	
	//Sin Producto
	$scope.datos.oClientePortafolio={
		tipo : "combobox-cliente-portafolio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
    idPostFiltro  	: false,	
		itemPostFiltro	: false,
		filtros: {
			IdEstado : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},
	};
	
/******************************************************/
/*** DATASOURCES									***/
/******************************************************/
	
	
	
/******************************************************/
/*** FUNCIONES DE CARGA DE TABLAS					***/
/******************************************************/
	
	var columnasOrdenes = [
		bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdOrden")
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaOperacionOrden",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdClasificacionTipoInstrumento",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("AccionTipoOperacion",{ groupable: true})					
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreTipoOperacion",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("InstrumentoUI",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("PrecioOrden",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("PrecioStrike",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("TipoPrecio",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("PrecioPromedioAsignacion",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("CantidadOrden",{ groupable: true,aggregates: ["sum"], groupFooterTemplate: "#= util.formateaNumero(sum,0) #", footerAttributes:{style:"text-align: right; "}})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("CantidadDisponibleOrden",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoCondicionDeLiquidacion",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("MontoOrden",{ groupable: true, aggregates: ["sum"], groupFooterTemplate: "#= util.formateaNumero(sum,0) #", footerAttributes:{style:"text-align: right; "}})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdMonedaOrden",{ groupable: true})		
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdMonedaLiquidacionOrden",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("EsVentaCortaUI",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("DocumentoIdentidad",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreCompleto",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoPortafolio",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("RazonSocialPortafolio",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("EstadoOrden",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaVigencia",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaVencimiento",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("Tasa",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreTipoVigenciaOrden",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("OrdenVigenteUI",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaRecepcion",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreFormaCustodia",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoEmpleadoPortafolio",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreEmpleadoPortafolio",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreSucursalEmpleadoPortafolio",{ groupable: true})
		
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoEmpleadoDigitador",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreEmpleadoDigitador",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreSucursalEmpleadoDigitador",{ groupable: true})
		
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoProcedenciaOrden",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreClaseOrden",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreCanal",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreCanalDistribucion",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdBolsa",{ groupable: true, template: "#= (IdBolsa == 0 ? 'TODAS' : IdBolsa) #"})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("EsPorMontoUI",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdTipoLibroOrden",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreLibroOrdenes",{ groupable: true})
 		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("EstadoOrdenAsignacion",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdTipoInstrumento",{ groupable: true})
		
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("CantidadAsignadaOrden",{ groupable: true, aggregates: ["sum"], groupFooterTemplate: "#= util.formateaNumero(sum,0) #", footerAttributes:{style:"text-align: right; "}})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("AsignadoDia",{ groupable: true , aggregates: ["sum"], groupFooterTemplate: "#= util.formateaNumero(sum,0) #", footerAttributes:{style:"text-align: right; "}})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("FolioOrden",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaOferta",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoCorredorCorresponsal",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("GlosaOrden",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreAutorizadorOrden",{ groupable: true})
		
		
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("MercadoSMV",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("ModalidadSMV",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("EsClienteFinal",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoBrokerExtranjero",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreBoveda",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoBoveda",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("IndicadorBancoCustodio",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoBancoCustodio",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoBancoLiquidador",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreMedioDePago",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("ModalidadLiquidacion",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("ModalidadEjercicio",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("TipoOpcion",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("EstiloOpcion",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("Tamano",{ groupable: true})

		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdSistemaBursatil",{ groupable: true})		
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("Producto",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaAceptacion",{ groupable: true})		
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("MontoOrdenPlazo",{ groupable: true, aggregates: ["sum"], groupFooterTemplate: "#= util.formateaNumero(sum,0) #", footerAttributes:{style:"text-align: right; "}})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("Interes",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("EnAnulacion",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("ModoFacturacion",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("TipoLiquidacion",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("PorcentajeVisibilidad",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("InstrumentoNegociacion",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("MontoLiquidacion",{ groupable: true})		
	];
	
	var dsOrdenes = new bcs.DataSource({
		type : "call",
		definition : {
			callTransaction : "1688",
			serverFiltering : false, // Filtrado de datos en el servidor.
			serverPaging 	: false, // Paginado de los datos en el servidor.
			serverSorting 	: false, // Ordenamiento de los datos en el servidor.
			callParameters  : filters,
			callResultSet 	: "P_OUT_ORDENES",
			sort			: { field: "IdOrden", dir: "desc"},
			pageSize 		: 200,	
			schema : bcsConfiguracionOrdenesV2.getSchemaOrden(),
		},
	});	
	
	var conf = {
		userSettings:true,
		sortable: true,
		reorderable: true,
		filterable: true,
		groupable: true,
		pageable: true,
		selectable: "multiple, row",
		allowCopy: true,
		resizable: true,
		autoBind: false,
		scrollable: true,
		navigatable: true,
		height:400,
		dataSource: dsOrdenes,
		columns: columnasOrdenes,
		excel : {
			allPages: true,
		},
		rowCellColor : function(row) {
			switch(row["AccionOrden"].toUpperCase()) {
				case 'VENTA':
					return {column: "NombreTipoOperacion", color: "red"};
					break;
				case 'COMPRA':
					return {column: "NombreTipoOperacion", color: "green"};
					break;
			}
		},	
		rowColorBackground : function(row) {
			console.log(row);
			switch(row["IdEstadoOrden"].toUpperCase()) {
				case "ANU":
					return "red";
					break;
				case "RZD":
					return "red";
					break;	
				case "VCD":
					return "red";
					break;
				case "EJP":
					return "blue";
					break;	
				case "EJE":
					return "blue";
					break;												
				case "ANP":
					return "blue";
					break;
				case "ENR":
					return "blue";
					break;
				case "ING":
					return "black";
					break;
				case "OFE":
					return "green";
					break;					
				case "PDA":
					return "orange";
					break;					
			}
		},
		boldRow: function() {
			return "bold";
		},			
	};
	
	if ($scope.SesionLocale === "PE") {
		conf.columns.splice(22,0,
			bcsConfiguracionOrdenesV2.getConfiguracionColumna("OrdenEspecialUI",{ groupable: true})
		);
	}
	
/******************************************************/
/*** FUNCIONES										***/
/******************************************************/
	
	var dataSource = conf.dataSource;
	$scope.defaultOptionsNew = conf;
	
	$scope.buscarOrdenes = function(filtros)
	{
		var resp = util.validarRangoFecha(filtros.fechaDesde, filtros.fechaHasta,365);
		
		filters.p_IdCliente 		= "0";
		filters.p_IdPortafolio		= "0";
		filters.p_IdProducto 		= "0";
		
		if(!resp.estado)
		{	
			
			//Datos grillas	
			filters.p_IdEmpresa 			= $scope.SesionIdEmpresa;		
			filters.p_IdUsuario         	= $scope.SesionIdUsuario;
			filters.p_IdModulo				= 'ORD';
			filters.p_IdEmpleadoConsulta	= $scope.SesionIdEmpleado;
			filters.p_Mercado				= filtros.IdClasificacionTipoInstrumento || "0";			
			filters.p_TipoInstrumento 		= filtros.IdTipoInstrumento || "0";
			filters.p_Operacion 			= filtros.AccionOrden || "0";
			filters.p_Instrumento 			= filtros.buscarInstrumento || "0";
			filters.p_FechaOperacionOrdenI 	= util.dateInicioDia(filtros.fechaDesde);
			filters.p_FechaOperacionOrdenF 	= util.dateTerminoDia(filtros.fechaHasta);
			filters.p_LibroOrdenes 			= filtros.IdLibroOrdenes || "0";
			filters.p_CondicionLiquidacion 	= filtros.IdCondicionDeLiquidacion || "0";
			filters.p_Moneda = filtros.IdMoneda === " TODOS" ? "0" : filtros.IdMoneda || "0";
			filters.p_CondicionLiquidacion 	= filtros.IdCondicionDeLiquidacion || "0";	
			filters.p_IdTipoCanalIngreso 	= filtros.IdTipoCanalIngreso;
			filters.p_IdPortafolio			= filtros.IdPortafolio || "0";
			filters.p_EsVentaCorta 			= filtros.EsVentaCorta ? "S" : "N";
			filters.p_IncluyeOrdenesVigentes = filtros.OrdenesVigentes ? "S" : "N";
			filters.p_OrdenesPorAsignar      = filtros.OrdenPorAsignar || false;
			
			//Para AVP
			if($scope.MostrarProducto)
			{	
				
				
				if($scope.oSocioNegocio.seleccionado.IdCliente !== undefined)
					filters.p_IdCliente    = Number($scope.oSocioNegocio.seleccionado.IdCliente);
				
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
							filters.p_IdProducto 			= "0"; 
						}
						else
						{
							filters.p_IdProducto 			= Number($scope.oProducto.seleccionado.IdProductoAgrupador);; 
						}
					}
				}
			}
			else
			{
				filters.p_IdCliente 		= "0";
				filters.p_IdProducto 		= "0";
			}
			
			dsOrdenes.setCallParameters(filters);
			
			
		}	
		else{
			bcsComunes.msg("warning", "INFO", resp.mensaje);
		}
		$scope.sumaCantidad = 0;
		$scope.sumaMonto = 0;
	};
	
	
	
	$scope.crearOrden = function()
	{
		bcsComunes.ventanaAbrir("IngresarOrdenMain", {} );
	};
	
	$scope.crearOrdenRF = function()
	{
		bcsComunes.ventanaAbrir("IngresarOrdenRF", {} );
	};
	
	$scope.crearOrdenOpciones = function()
	{
		bcsComunes.ventanaAbrir("IngresarOrdenOpcionesPE", {} );
	};
	
	$scope.contraorden = function(ordenes)
	{
		if (!ordenes || !ordenes.length)
			return;
		if(ordenes[0].IdClasificacionTipoInstrumento==='RF'){
			console.log("Generar Contraorden RF");
			bcsComunes.ventanaAbrir("IngresarOrdenRF",{orden:ordenes[0]});
			
		}else if (ordenes[0].IdClasificacionTipoInstrumento==='RV'){
			bcsComunes.ventanaAbrir("IngresarOrdenMain",{orden : ordenes[0]} );
		} else {
			bcsComunes.ventanaAbrir("IngresarOrdenOpcionesPE",{orden : ordenes[0]} );
		}
	};
	
	$scope.seleccionarTodo = function() {	
		$scope.apiGridOrdenes.select($scope.apiGridOrdenes.tbody.find(">tr"));		
	};
	
	$scope.verBitacora = function(items)
	{
		if (!items || !items[0]) return;
		bcsComunes.ventanaAbrir("BitacoraOrden", {IdOrden : items[0].IdOrden},false,false,$scope.idVentana );
	};

	$scope.exportarLibro = function() {
		var f = $scope.filtros.fechaDesde;
		console.log(util.dateInicioDia($scope.filtros.fechaDesde));
		bcsComunes.ventanaAbrir("ExportarLibro", { tipoLibro: "ORDENES", libroDefecto : $scope.filtros.IdLibroOrdenes, fecha : f, columnas : columnasOrdenes} ,false,false,$scope.idVentana); 
	};
	
	$scope.modificar = function(ordenes) {
		if (!ordenes || ordenes.length == 0 || ordenes.length !== 1) return;
		if (ordenes[0].IdClasificacionTipoInstrumento === 'RV' || ordenes[0].IdClasificacionTipoInstrumento === 'RF') {
			if (ordenes[0].IdEstadoOrden === 'ING') {	
				var ventanamod = "";
				if(ordenes[0].IdClasificacionTipoInstrumento === 'RV') ventanamod = "ModificarOrdenRV";
				else if(ordenes[0].IdClasificacionTipoInstrumento === 'RF') ventanamod = "ModificarOrdenRF";
				var id = bcsComunes.ventanaAbrir(ventanamod, {orden:ordenes[0]},false,false,$scope.idVentana);
				var promesa = bcsComunes.ventanaPromesa(id);
				promesa.then(function(result) {
					if(result)$scope.buscarOrdenes($scope.apiGridOrdenes,$scope.filtros);
				});	
			}else bcsComunes.msg("error","UI_ERROR","UI_ORDEN_ESTADO_INGRESADO_PARA_MODIFICAR");			
		}else bcsComunes.msg("error","UI_ERROR","UI_CLASIFICACION_ORDEN_NO_PERMITE_MODIFICACION");
	};
	
	
	$scope.ValidarSolicitudAnulacion = function(ordenesTemp){
		var esValida = true;
		$.each(ordenesTemp,function(key,value){
			if(value.EnAnulacion === 'S'){
				esValida = false;
		}
		
		});
		return esValida;
	}
	
	
	$scope.anularOrdenes = function(ordenesTemp)
	{
		if($scope.ValidarSolicitudAnulacion(ordenesTemp)){
			
			if (!ordenesTemp || ordenesTemp.length == 0) return;
			
			if ($scope.SesionLocale === 'PE') {
				var id = bcsComunes.ventanaAbrir("IngresarMotivoAnularOrdenes", {} ,false,false,$scope.idVentana);
				var promesa = bcsComunes.ventanaPromesa(id);
				promesa.then(function(motivo) {
					if (motivo && angular.isString(motivo)) {
						var datos = {
			    			p_IdEmpresa : $scope.SesionIdEmpresa,
				  			p_IdUsuario : $scope.SesionIdUsuario,
				  			p_IdModulo : 'ORD',
				  			p_ordenes : [],
			    		};
				    	$.each(ordenesTemp,function(key,ord){
				  			datos.p_ordenes.push({IdOrden : ord.IdOrden,MotivoAnulacion : motivo});
				  		});
				    	bcsComunes.ventanaOverlay($scope.idVentana,true);
				    	bcsServicioOrden.AnularOrdenMasiva(datos).$promise.then(function(result){
				    		bcsComunes.ventanaOverlay($scope.idVentana,false);
							var id = bcsComunes.ventanaMsg({
								mensaje: '',
								conflictos: util.parseServerResponse(result)
							},$scope.idVentana)
	
				    		$scope.buscarOrdenes($scope.apiGridOrdenes,$scope.filtros);
				    		
				    	},function(result){
				    		bcsComunes.ventanaOverlay($scope.idVentana,false);
				    		$scope.buscarOrdenes($scope.apiGridOrdenes,$scope.filtros);
				    		
				    	});
					}
				});
			}
			else {
				
				var t = bcsAlerta.crear('PREGUNTA',$translate.instant('UI_ADVERTENCIA_ANULACION_ORDENES'),$scope.idVentana);
				t.then(function(result){
					if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
						
						var datos = {
			    			p_IdEmpresa : $scope.SesionIdEmpresa,
				  			p_IdUsuario : $scope.SesionIdUsuario,
				  			p_IdModulo : 'ORD',
				  			p_ordenes : [],
			    		};
				    	$.each(ordenesTemp,function(key,ord){
				  			datos.p_ordenes.push({IdOrden : ord.IdOrden,MotivoAnulacion : ""});
				  		});
				    	bcsComunes.ventanaOverlay($scope.idVentana,true);
				    	bcsServicioOrden.AnularOrdenMasiva(datos).$promise.then(function(result){
				    		bcsComunes.ventanaOverlay($scope.idVentana,false);
							var id = bcsComunes.ventanaMsg({
								mensaje: '',
								conflictos: util.parseServerResponse(result)
							},$scope.idVentana)
							
							$scope.buscarOrdenes($scope.filtros);
				    	
				    	},function(result){
				    		bcsComunes.ventanaOverlay($scope.idVentana,false);
				    		$scope.buscarOrdenes($scope.apiGridOrdenes,$scope.filtros);
				    		
				    	});
					}
				});
			}
		}else{
			bcsComunes.ventanaOverlay($scope.idVentana,true);
			var idModal = bcsComunes.ventanaMsgErr($translate.instant('UI_ORDEN_CON_SOLICITUD_ANULACION_PENDIENTE'), $scope.idVentana);
			bcsComunes.ventanaOverlay($scope.idVentana,false);
		}
	};
	
	$scope.abrirDetalle = function (items) {
		bcsComunes.ventanaAbrir("DetalleOrden", {IdOrden : items[0].IdOrden},false,false);
	};
	
	$scope.verMapaRelaciones = function (items) {
		bcsComunes.ventanaAbrir("MapaRelaciones", {TipoEntidad : "ORDEN",IdEntidad : items[0].IdOrden},false,false );
	};
	
	$scope.limpiarFiltros = function()
	{
		$scope.filtros.buscarInstrumento				= "-1";
		$scope.filtros.IdTipoOperacion					= 0;
		$scope.filtros.IdTipoInstrumento				= 0;
		$scope.filtros.IdMoneda							= 0;
		$scope.filtros.IdCondicionDeLiquidacion			= 0;
		$scope.filtros.IdLibroOrdenes					= 0;
		$scope.filtros.AccionOrden						= 0;
		$scope.filtros.IdClasificacionTipoInstrumento	= 'RV';
		$scope.filtros.documentoIdentidad				= undefined;
		$scope.filtros.nombreCompleto					= undefined;
		$scope.filtros.EsVentaCorta						= false;
		$scope.filtros.OrdenPorAsignar					= false;
		$scope.filtros.IdPortafolio						= false;
		$scope.filtros.fechaDesde						= util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtros.fechaHasta						= util.dateTerminoDia($scope.SesionFechaSistema);
		
		$scope.oSocioNegocio.idActual					= false;
		$scope.oProducto.idActual						= false;
		$scope.oPortafolio.idActual						= false;
	};
	
  	$scope.limpiarFiltros();
  	
  	$scope.verComprobante = function(){
  		var winID=bcsComunes.ventanaWinId($scope);//necesario para el overlay de espera.
  		if($scope.datos.kNgModel && $scope.datos.kNgModel.length>0){
  			var data={idEmpresa:$scope.SesionIdEmpresa, modulo:'ORD',idOrden:$scope.datos.kNgModel[0].IdOrden};
  			bcsServicioReportes.abrirReporteSolicitudCompraOrdenes(data,winID);
  		}
  	};
  	
  	$scope.limpiarTotalizador = function(){
  		$scope.nemo = '';
  		$scope.sumaCompras = "-";
		$scope.sumaVentas = "-";
		$scope.precioMayorCompras = "-";
		$scope.precioMenorCompras = "-";
		$scope.precioMayorVentas = "-";
		$scope.precioMenorVentas = "-";  		
  	};
  	
  	$scope.$watch('datos.kNgModel',function(newval,oldval) {
		if (!newval) {
			return;
		}
		
		var nemo = undefined;
		var exit = false;
		$.each(newval,function(key,value){
			if (nemo === undefined) {
				nemo = value.IdInstrumento;
			}
			
			if (value.IdInstrumento !== nemo) {
				exit = true;
				return false;
			}
		});
		
		
		$scope.nemo = '';
		$scope.sumaCompras = "-";
		$scope.sumaVentas = "-";
		$scope.precioMayorCompras = "-";
		$scope.precioMenorCompras = "-";
		$scope.precioMayorVentas = "-";
		$scope.precioMenorVentas = "-";

		if (exit) {
			return;
		}
		if (newval[0] === undefined) {
			return;
		}
		$scope.nemo = newval[0].InstrumentoUI;
		var sumaCompras = 0;
		var sumaVentas = 0;
		var mayorCompras = -1;
		var mayorVentas = -1;
		var menorCompras = 999999999999999;
		var menorVentas = 999999999999999;
		//variables para formatear resumen
		var monedaSeleccion = [];
		var idMoneda = "";
		var idTipoInstrumento = "";
		var idTipoOperacion = "";

		$.each(newval,function(key,value){
			if (value.AccionOrden === 'COMPRA') {
				sumaCompras = sumaCompras + value.CantidadOrden;
				menorCompras = Number(value.PrecioOrden)<menorCompras?Number(value.PrecioOrden):menorCompras;
				mayorCompras = Number(value.PrecioOrden)>mayorCompras?Number(value.PrecioOrden):mayorCompras;
			}
			if (value.AccionOrden === 'VENTA') {
				sumaVentas = sumaVentas + value.CantidadOrden;
				menorVentas = Number(value.PrecioOrden)<menorVentas?Number(value.PrecioOrden):menorVentas;
				mayorVentas = Number(value.PrecioOrden)>mayorVentas?Number(value.PrecioOrden):mayorVentas;
			}
			
			if(!_.contains(monedaSeleccion,value.IdMonedaOrden)){
				monedaSeleccion.push(value.IdMonedaOrden);
				idMoneda = value.IdMonedaOrden;
				idTipoOperacion = value.IdTipoOperacion;
				IdTipoInstrumento = value.IdTipoInstrumento;
				IdClasificacionTipoInstrumento = value.IdClasificacionTipoInstrumento;
			}
		});
		
		if (sumaCompras !== 0) {
			$scope.sumaCompras = " " + util.formateaNumero(sumaCompras,0);
		}
		if (sumaVentas !== 0) {
			$scope.sumaVentas = " " + util.formateaNumero(sumaVentas,0);
		}
		if (IdClasificacionTipoInstrumento == 'RF' || IdClasificacionTipoInstrumento == 'IF'){
			idTipoInstrumento = IdTipoInstrumento;
			idTipoOperacion = "";
		}
		
		if (menorCompras !== 999999999999999) {
			$scope.precioMenorCompras = monedaSeleccion[0] + " " + util.bcs_formato((menorCompras != 999999999999999 && monedaSeleccion.length==1?menorCompras:0),"GEN","precio",idMoneda,idTipoInstrumento,idTipoOperacion).valor;
		}
		if (menorVentas !== 999999999999999) {
			$scope.precioMenorVentas = monedaSeleccion[0] + " " + util.bcs_formato((menorVentas != 999999999999999 && monedaSeleccion.length==1?menorVentas:0),"GEN","precio",idMoneda,idTipoInstrumento,idTipoOperacion).valor;
		}
		if (mayorCompras !== -1) {
			$scope.precioMayorCompras = monedaSeleccion[0] + " " + util.bcs_formato((mayorCompras != -1 && monedaSeleccion.length==1 ?mayorCompras:0),"GEN","precio",idMoneda,idTipoInstrumento,idTipoOperacion).valor;
		}
		
		if (mayorVentas !== -1) {
			$scope.precioMayorVentas = monedaSeleccion[0] + " " + util.bcs_formato((mayorVentas != -1 && monedaSeleccion.length==1 ?mayorVentas:0),"GEN","precio",idMoneda,idTipoInstrumento,idTipoOperacion).valor;	
		}
		
	});
	$scope.$watch('datos.kNgModel',function(newval,oldval) {
		if (!newval) return;
		$scope.sumaCantidad = 0;
		$scope.sumaMonto = 0;
		var moneda = "USD";
		$.each(newval,function(key,value){
			if(isNaN(Number(value.CantidadOrden)) || value.CantidadOrden === ""){
				bcsComunes.msg("warning", "UI_ADVERTENCIAS", "UI_NO_POSIBLE_SUMAR_CANTIDAD");
			}else{
				$scope.sumaCantidad = $scope.sumaCantidad + value.CantidadOrden;
			}
			if(isNaN(Number(value.MontoOrden)) || value.MontoOrden === ""){
				bcsComunes.msg("warning", "UI_ADVERTENCIAS", "UI_NO_POSIBLE_SUMAR_MONTOo");
			}else{
				$scope.sumaMonto = $scope.sumaMonto + value.MontoOrden;
			}
		})
		if($scope.sumaCantidad != 0){
			$scope.sumaCantidad = util.bcs_formato($scope.sumaCantidad,'GEN','CANTIDAD',moneda,'','').valor;
		}
		if($scope.sumaMonto != 0 && util.bcs_formato($scope.sumaMonto,'GEN','Monto',moneda,'','').valor != 'Error'){
			$scope.sumaMonto = util.bcs_formato($scope.sumaMonto,'GEN','Monto',moneda,'','').valor;
		}else{
			$scope.sumaMonto = 0;
		}
	});
var array = [
		{ Estado: 'Anulado', Color: 'red' },
	    { Estado: 'Rechazado', Color: 'red' },	    
	    { Estado: 'Vencido', Color: 'red' },
	    { Estado: 'Ejecutado Parcialmente', Color: 'blue' },
	    { Estado: 'Ejecutado', Color: 'blue' },
	    { Estado: 'Anulado Parcial', Color: 'blue' },
	    { Estado: 'En Ruteo', Color: 'blue' },
	    { Estado: 'Ingresado', Color: 'black' },
	    { Estado: 'Ofertado', Color: 'green' },
	    { Estado: 'Pendiente de Autorización', Color: 'orange' }

	];
	
		bcsComunes.tootipGlosa('toolConsultarOrdenes', array)	
  	
/******************************************************/
/*** FINAL											***/
/******************************************************/
});