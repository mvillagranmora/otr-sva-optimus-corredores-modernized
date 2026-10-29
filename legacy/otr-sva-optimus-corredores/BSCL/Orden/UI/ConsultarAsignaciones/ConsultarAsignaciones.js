angular.module('ModuloOrdenes').controller('ConsultarAsignacionesCtrl',
function($scope,bcsComunes,bcsConsultasOrdenes,bcsServicioOrden,$translate,bcsAlerta,OPCIONES_BCSALERTA,bcsConfiguracionOrdenes,bcsConfiguracionOrdenesV2)
{
/******************************************************/
/*** INICIO DE VARIABLES Y PRECARGA					***/
/******************************************************/
	
	let controllerName = 'ConsultarAsignacionesCtrl';
	
	$scope.data = {};
	$scope.filtros = {};
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	$scope.data.ActivaBotonEnvAFac = true;
	$scope.data.ActivaBotonReasignar = true;
	
	$scope.MostrarProducto = BCS_CONFIGURACION.PLATAFORMA["APV.Habilitado"] === 'S' ? true : false;
	$scope.data.AjustarDolarAsignacion = bcsConfiguracionOrdenes.CONFIGURACION["AjustarDolarAsignacion"] === 'S';

	$scope.sumaCantidad = 0;
	$scope.sumaMonto = 0;
	
	let filters = {};
/******************************************************/
/*** FILTROS										***/
/******************************************************/
	
	$scope.data.filtrosTipoInstrumento = bcsConfiguracionOrdenes.UI.getFiltrosCombo(controllerName,'combobox-tipo-instrumento');
	$scope.data.filtrosOperacion = bcsConfiguracionOrdenes.UI.getFiltrosCombo(controllerName,'combobox-operacion');
	$scope.data.filtrosCondicionDeLiquidacion = bcsConfiguracionOrdenes.UI.getFiltrosCombo(controllerName,'combobox-condicion-liquidacion');
	$scope.data.filtrosInstrumentos = {
		IdClasificacionTipoInstrumento : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": ""},
		IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value"   : "" }
	};
	$scope.data.filtrosMonedaNegociacion = {
		IdMoneda : {"field": "IdMoneda","operator": "eq","value": ""}    
	};
	
	$scope.oSocioNegocio = {
		filtros: {
			IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},
	};
	
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
	
    let columnasAsignaciones = [
    	 bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaAsignacion",{ groupable: true})
		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdOrden",{ locked: true,groupable: true})
		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("FolioTransaccion",{ groupable: true})
		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaOperacionTransaccion",{ groupable: true})
		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdClasificacionTipoInstrumento",{ groupable: true})
		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreTipoOperacion",{ groupable: true})
		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoOperador",{ groupable: true})
		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("InstrumentoUI",{ groupable: true})
		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("PrecioAsignacion",{ groupable: true})
	 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("CantidadAsignada",{ groupable: true})
	 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("MontoAsignado",{ groupable: true})			 	
	 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("Tasa",{ groupable: true})
	 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("Plazo",{ groupable: true})
	 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdBolsa",{ groupable: true})
	 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaLiquidacion",{ groupable: true})
	 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("ModoFacturacion",{ groupable: true})
	 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("TipoLiquidacion",{ groupable: true})
	 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaVencimiento",{ groupable: true})
	 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdEmisor",{ groupable: true})
	 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoCondicionDeLiquidacion",{ groupable: true})
	 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("ValorRescateAsignado",{ groupable: true})
	 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("EstadoAsignacion",{ groupable: true})
	 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreEstadoFacturacion",{ groupable: true})
	 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdTipoReajuste",{ groupable: true})
	 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdMonedaOperacion",{ groupable: true})				 	
	 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdTipoInstrumento",{ groupable: true})
	 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("ValorClasificacionRiesgo",{ groupable: true})
	 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreLibroOrdenes",{ groupable: true})
	 	
	 	, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreBoveda",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoBoveda",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("IndicadorBancoCustodio",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoBancoCustodio",{ groupable: true})
		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoBancoLiquidador",{ groupable: true})
	 	
	 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreFormaCustodia",{ groupable: true})
	 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoEmpleadoPortafolio",{ groupable: true})
	 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoEmpleadoDigitador",{ groupable: true})
	 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("MontoAsignadoPlazo",{ groupable: true})
	 	/*OPTIMUSPE-1239*/
	 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("RutCavali",{ groupable: true})
	 	/*OPTIMUSPE-1239*/
	 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("DocumentoIdentidad",{ groupable: true})
	 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreCompleto",{ groupable: true})
	 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoPortafolio",{ groupable: true})
	 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("RazonSocialPortafolio",{ groupable: true})
	 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreTipoCanalIngreso",{ groupable: true})
		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("IndicadorOrdenDirecta",{ groupable: true})
		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("EsAPlazoUI",{ groupable: true})
		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("EsVentaCortaUI",{ groupable: true})
		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("TasaVentaCorta",{ groupable: true, title: $translate.instant('UI_TASA_VENTA_CORTA')})
		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("FolioOrden",{ groupable: true})
		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaOperacionOrden",{ groupable: true})
		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdAsignacion",{ groupable: true})
		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoIFRS",{ groupable: true})
		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreMedioDePago",{ groupable: true})
		
		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("ModalidadLiquidacion",{ groupable: true})
		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("ModalidadEjercicio",{ groupable: true})
		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("TipoOpcion",{ groupable: true})
		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("EstiloOpcion",{ groupable: true})
		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("Tamano",{ groupable: true})
		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("Producto",{ groupable: true})
		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("FolioSpot",{ groupable: true})
		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("EstadoSpot",{ groupable: true})
		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("InstrumentoNegociacion",{ groupable: true})		
		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdMonedaInstrumento",{ groupable: true})
		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdAsignacionMasiva",{ groupable: true, hidden: true})
		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreTipoAsignacion",{ groupable: true})
		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("AplicaComisionAsignacionMasiva",{ groupable: true})
		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("MontoCLP",{ groupable: true})
		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("TipoCambio",{ groupable: true})

		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("MarcaAsignacion",{ groupable: true})
	];
    
    
    let conf = {};
	let dsAsignaciones = new bcs.DataSource({
    	type : "call",
    	definition : {
    		callTransaction : "1825",
    		serverFiltering : false, // Filtrado de datos en el servidor.
			serverPaging 	: false, // Paginado de los datos en el servidor.
			serverSorting 	: false, // Ordenamiento de los datos en el servidor.
			callParameters  : filters,
			callResultSet 	: "P_OUT_ASIGNACIONES",
			sort			: { field: "IdAsignacion", dir: "desc"},
			pageSize 		: 200,	
			schema 			: bcsConfiguracionOrdenesV2.getSchemaAsignacion(),
    	}
    });
    

    conf.options = {
    		allowCopy	: true,
    		autoBind	: false,
    		excel 		: {allPages: true,},
      		filterable	: true,
    		groupable	: true,
    		height		: 400,
    		pageable	: true,
    		reorderable	: true,
    		resizable	: true,
      		selectable	: "multiple",
    		sortable	: true,
    		userSettings: true,
    		navigatable	: true,
    		dataSource: dsAsignaciones,
    		columns: columnasAsignaciones,
    		gridCreated : function(grid) {
    			$scope.apiGridAsignaciones = grid;
    			if ($scope.SesionLocale === 'PE'){
    				grid.showColumn("RutCavali");
    			}
    		}  		
    		
    	};    
    
    
    
    conf.options.rowCellColor = function(row) {
    	switch(row["AccionOrden"].toUpperCase()) {
			case 'VENTA':
				return {column: "NombreTipoOperacion", color: "red"};
				break;
			case 'COMPRA':
				return {column: "NombreTipoOperacion", color: "green"};
				break;
		}
	};
	
	conf.options.rowColorBackground = function(row) {
		
		if (row["IdEstadoAsignacion"].toUpperCase()== 'NEF' & row["IdEstadoFacturacion"].toUpperCase()== 'NEF'){
			return "black";
		}
			
		else if (row["IdEstadoAsignacion"].toUpperCase()== 'EAF' & row["IdEstadoFacturacion"].toUpperCase()== 'FAC'){
			return "blue";
		}
										
		else if (row["IdEstadoAsignacion"].toUpperCase()== 'EAF' & row["IdEstadoFacturacion"].toUpperCase()== 'ING'){
			return "green";
		}
		
		else if (row["IdEstadoAsignacion"].toUpperCase()== 'NEF' & row["IdEstadoFacturacion"].toUpperCase()== 'ANU'){
			return "red";
		}		
					
		else if (row["IdEstadoAsignacion"].toUpperCase()== 'EAF' & (row["IdEstadoFacturacion"].toUpperCase()== 'REF' || row["IdEstadoFacturacion"].toUpperCase()== 'FAC')){
			return "yellow";
		}
	},
		conf.options.boldRow = function() {
			return "bold";
		};
	
	
    
/******************************************************/
/*** FUNCIONES										***/
/******************************************************/
    
	$scope.abrirDetalle = function () {
		bcsComunes.ventanaAbrir("DetalleAsignacion", {IdAsignacion : $scope.data.kNgModel[0].IdAsignacion } ,false,false,$scope.idVentana);
  	};

  	$scope.verMapaRelaciones = function (items) {
		bcsComunes.ventanaAbrir("MapaRelaciones", {TipoEntidad : "ASIGNACION",IdEntidad : items[0].IdAsignacion},false,false );
	};
	
    $scope.data.enviarFacturar = function (asignacionesTemp) {
    	
    	if (!asignacionesTemp || asignacionesTemp.length == 0) return;
		let t = bcsAlerta.crear('PREGUNTA',$translate.instant('UI_ESTA_SEGURO_ENVIAR_FACTURAR_MOVIMIENTOS_SELECCIONADOS'),$scope.idVentana);
		t.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
				let datos = {
					p_IdEmpresa 	: $scope.SesionIdEmpresa,
					p_IdUsuario 	: $scope.SesionIdUsuario,
					p_IdModulo		:'ORD',
					p_Asignaciones	: []

				};

				$.each(asignacionesTemp,function(key,asignacion){
					datos.p_Asignaciones.push({IdAsignacion : asignacion.IdAsignacion,GeneraComprobante : 'N'});
				});

				bcsComunes.ventanaOverlay($scope.idVentana,true);
				bcsServicioOrden.EnviarAsignacionesFacturar(datos,function(result){
					bcsComunes.ventanaOverlay($scope.idVentana,false);

					let id = bcsComunes.ventanaMsg({
						mensaje: '',
						conflictos: util.parseServerResponse(result)
					},$scope.idVentana)
					
					$scope.buscarAsignaciones($scope.filtros);
				},function(result){
					bcsComunes.ventanaOverlay($scope.idVentana,false);

					let id = bcsComunes.ventanaMsg({
						mensaje: '',
						conflictos: util.parseServerResponse(result)
					},$scope.idVentana)
				});
			}
		});
    };
    
    
    $scope.data.anularAsignaciones = function (asignacionesTemp) {
    	
		if (!asignacionesTemp || asignacionesTemp.length == 0) return;
		let t = bcsAlerta.crear('PREGUNTA',$translate.instant('UI_ADVERTENCIA_ANULACION_ASIGNACIONES'),$scope.idVentana);
		t.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
				
				let datos = {
		    			p_IdEmpresa : $scope.SesionIdEmpresa,
			  			p_IdUsuario : $scope.SesionIdUsuario,
			  			p_IdModulo : 'ORD',
			  			p_simularOperacion : "N",
			  			asignacionesParaAnular : [],
		    	};
		    	$.each(asignacionesTemp,function(key,asignacion){
		    		
		    		let unico = datos.asignacionesParaAnular.filter(valor=> valor.IdAsignacion==asignacion.IdAsignacion);
		    		
		    		if(unico!=null && unico.length==0){
		    			datos.asignacionesParaAnular.push({IdAsignacion : asignacion.IdAsignacion});		    			
		    		}
			  	});
			  	
		    	bcsComunes.ventanaOverlay($scope.idVentana,true);
		    	bcsServicioOrden.AnularAsignacionMasiva(datos).$promise.then(function(result){
		    		bcsComunes.ventanaOverlay($scope.idVentana,false);
		    		let id = bcsComunes.ventanaMsg({
						mensaje: '',
						conflictos: util.parseServerResponse(result)
					},$scope.idVentana)
		    		$scope.buscarAsignaciones($scope.filtros);
		    		$scope.data.kNgModel = [];
		    		if ($scope.apiGridAsignaciones) {
		    			$scope.apiGridAsignaciones.clearSelection();
		    		}

		    	},function(result){
		    		bcsComunes.ventanaOverlay($scope.idVentana,false);
		    		$scope.buscarAsignaciones($scope.filtros);
		    	});
			}
		});
    };

	$scope.data.desasignacionSeleccion = function () {
		if (!$scope.data.kNgModel || $scope.data.kNgModel.length === 0) {
			bcsComunes.msg("warning", "UI_ADVERTENCIA", "UI_DEBE_SELECCIONAR_ASIGNACION");
			return;
		}
		$scope.data.anularAsignaciones($scope.data.kNgModel);
	};

	$scope.data.desasignacionMasiva = function () {
		let idModal = bcsComunes.ventanaAbrir("EjecutarDesasignacion", null, false, false, $scope.idVentana);
		let promesa = bcsComunes.ventanaPromesa(idModal);
		promesa.then(function () {
			$scope.buscarAsignaciones($scope.filtros);
		});
	};

	$scope.data.opcionesDesasignacion = [
		{ titulo : $translate.instant("UI_DESASIGNACION"),        funcion : $scope.data.desasignacionSeleccion },
		{ titulo : $translate.instant("UI_DESASIGNACION_MASIVA"), funcion : $scope.data.desasignacionMasiva }
	];

	//Reasignar
	$scope.data.reasignarAsignaciones = function(items) {
		let datos = [];
		if (!items)
			return;
		for (let i = 0 ; i< items.length ; i++){
			datos.push({"IdMovimiento" : items[i].IdAsignacion });
		};
		
		bcsComunes.ventanaAbrir("InterfazRefacturacion", {FormaAsignacion: 'REASIGNACION', Origen : 'ASIG', movimientos : datos}, false, false, $scope.idVentana);

	}
	
	$scope.data.ajustarTipoCambio = function() {

		if (!$scope.data.AjustarDolarAsignacion) {
			return
		}

		bcsComunes.ventanaOverlay($scope.idVentana,true);
		var id1 	= bcsComunes.ventanaAbrir("AjustarTipoCambio", {IdAsignacion : $scope.data.kNgModel[0].IdAsignacion, AjustaTipoCambio: $scope.data.AjustarDolarAsignacion && $scope.data.ActivaBotonAjustarDolarAsignacion} , false, false, $scope.idVentana);
		var promesa = bcsComunes.ventanaPromesa(id1);                               
		promesa.then(function(data){
			bcsComunes.ventanaOverlay($scope.idVentana, false);
			dsAsignaciones.setCallParameters(filters);
		}); 
	}
	
	$scope.defaultOptionsNew = conf.options;
	
	$scope.buscarAsignaciones = function(filtros)
	{
		let resp = util.validarRangoFecha(filtros.fechaDesde, filtros.fechaHasta,365);
		
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
			filters.p_FechaAsignacionI 		= util.dateInicioDia(filtros.fechaDesde);
			filters.p_FechaAsignacionF 		= util.dateTerminoDia(filtros.fechaHasta);
			filters.p_CondicionLiquidacion 	= filtros.IdCondicionDeLiquidacion || "0";						
			filters.p_LibroOrdenes 			= filtros.IdLibroOrdenes || "0";
			filters.p_Bolsa 				= filtros.IdBolsa || "0";			
			filters.p_Estado 				= filtros.IdEstadoAsignacion || "0";
			filters.p_IdPortafolio			= filtros.IdPortafolio || "0";
			filters.p_IdMonedaOperacion 	= filtros.IdMoneda === " TODOS" ? "0" : filtros.IdMoneda || "0";
			filters.p_MarcaAsignacion		= filtros.MarcaAsignacion || "";
			filters.p_CodigoOperador 		= filtros.codigoOperador || "0";

			//Para AVP
			if($scope.MostrarProducto)
			{	
				filters.p_IdCliente 		= "0";
				filters.p_IdPortafolio		= "0";
				filters.p_Producto 			= "0";
				
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
							filters.p_Producto 			= "0"; 
						}
						else
						{
							filters.p_Producto 			= Number($scope.oProducto.seleccionado.IdProductoAgrupador);; 
						}
					}
				}
			}
			else
			{
				filters.p_IdCliente 		= "0";
				filters.p_Producto 			= "0";
			}
			
			dsAsignaciones.setCallParameters(filters);
			
			
		}	
		else{
			bcsComunes.msg("warning", "INFO", resp.mensaje);
		}
			
		$scope.sumaCantidad = 0;
		$scope.sumaMonto = 0
	};
	
	
	
    
	$scope.seleccionarTodo = function()
	{	
		$scope.apiGridAsignaciones.select($scope.apiGridAsignaciones.tbody.find(">tr"));		
	};
	
	$scope.exportarLibro = function()
	{
		let f = $scope.filtros.fechaDesde;
		bcsComunes.ventanaAbrir("ExportarLibro", { tipoLibro: "ASIGNACIONES", libroDefecto : $scope.filtros.IdLibroOrdenes, fecha : f, columnas : columnasAsignaciones} ,false,false,$scope.idVentana);
	};
	
	$scope.limpiarFiltros = function()
	{
		$scope.filtros.buscarInstrumento				= "-1";
		$scope.filtros.AccionOrden						= 0;
		$scope.filtros.IdTipoInstrumento				= 0;
		$scope.filtros.IdLibroOrdenes					= 0;
		$scope.filtros.IdBolsa							= 0;
		$scope.filtros.IdEstadoAsignacion				= 0;
		$scope.filtros.IdClasificacionTipoInstrumento	= 0;
		$scope.filtros.IdCondicionDeLiquidacion			= 0;
		$scope.filtros.fechaDesde						= util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtros.fechaHasta						= util.dateTerminoDia($scope.SesionFechaSistema);
		$scope.filtros.IdPortafolio						= false;
		$scope.filtros.IdMoneda							= 0;
		$scope.filtros.MarcaAsignacion					= '';
		$scope.filtros.codigoOperador 					= "";
		
		$scope.oSocioNegocio.idActual					= false;
		$scope.oProducto.idActual						= false;
		$scope.oPortafolio.idActual						= false;
		$scope.sumaCantidad = 0;
		$scope.sumaMonto = 0;

		dsAsignaciones.removeAll();
	};
	
	$scope.limpiarFiltros();
	
/******************************************************/
/*** WATCH											***/
/******************************************************/
	
	$scope.$watch('data.kNgModel',function(newval,oldval)
	{	
		let f = false;
		$.each($scope.data.kNgModel,function(key,value)
		{
			if (value.IdEstadoAsignacion !== 'EAF' && value.IdEstadoAsignacion !== 'PDA')
			{
				$scope.data.ActivaBotonEnvAFac = true;
				$scope.data.ActivaBotonReasignar = true;
				f = true;
			}
			
			$scope.data.ActivaBotonAjustarDolarAsignacion = value.IdMonedaOperacion === 'USD' 															
															&& value.IdEstadoAsignacion === 'NEF'
															&& value.IdClasificacionTipoInstrumento === 'RV';
		})
		
		if (!f)
		{
			$scope.data.ActivaBotonEnvAFac = false;
			$scope.data.ActivaBotonReasignar = false;
		}
	});
	$scope.$watch('data.kNgModel',function(newval,oldval) {
		if (!newval) return;
		$scope.sumaCantidad = 0;
		$scope.sumaMonto = 0;
		let moneda = "USD";
		$.each(newval,function(key,value){
			if(isNaN(Number(value.CantidadAsignada)) || value.CantidadAsignada === ""){
				bcsComunes.msg("warning", "UI_ADVERTENCIAS", "UI_NO_POSIBLE_SUMAR_CANTIDAD");
			}else{
				$scope.sumaCantidad = $scope.sumaCantidad + value.CantidadAsignada;
			}
			if(isNaN(Number(value.MontoAsignado)) || value.MontoAsignado === ""){
				bcsComunes.msg("warning", "UI_ADVERTENCIAS", "UI_NO_POSIBLE_SUMAR_MONTO");
			}else{
				$scope.sumaMonto = $scope.sumaMonto + value.MontoAsignado;
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
	
	let array = [
		{ Estado: 'No enviada a Facturar - No enviada a Facturar', Color: 'black' },		
		{ Estado: 'Enviado a Facturar - Facturado', Color: 'blue' },	    
		{ Estado: 'Enviado a Facturar - Ingresado', Color: 'green' },
		{ Estado: 'No enviada a Facturar - Anulado', Color: 'red' },
	];
	
	bcsComunes.tootipGlosa('toolConsultarAsignaciones', array)
	
/******************************************************/
/*** FINAL											***/
/******************************************************/
});