angular.module('ModuloCustodia').controller('CoberturaGarantiaCtrl',
		function($scope
				,$timeout
				,bcsComunes
				,bcsCoberturaGarantia
				,bcsServicioPlataforma
				)	
{
    
	//Configuracion de Ventana Inicial
	$scope.data = {};
	
	
	//--------------------------------------------
	// COMBOBOX
	//--------------------------------------------
	
	$scope.data.oClientePortafolio = {
			tipo 			: "combobox-cliente-portafolio",
			configuracion 	: {},
			filtros			: {
					IdEmpresa : {"field" : "IdEmpresa", "operator" : "eq", "value" : $scope.SesionIdEmpresa},
					EsCliente : {"field" : "EsCliente", "operator" : "eq", "value" : "UI_SI" }
			},
			idActual 		: false,
			seleccionado 	: false
	}; 
	
	$scope.data.oConfiguracionTipoInstrumento={
		tipo 			: "combobox-tipo-instrumento",
		configuracion 	: {},
		filtros 		: {
							IdEstado        : { "field"   : "IdEstado",      "operator": "eq", "value"   : "VIG" },
							ExigeGarantia 	: { "field"   : "ExigeGarantia", "operator": "eq", "value"   : "S" },
		},
		idActual 		: false,
		seleccionado 	: false,
		idDefecto 		: '',		
	};
	

	$scope.data.oConfiguracionInstrumento={
			tipo 			: "combobox-instrumento",
			configuracion 	: {},
			filtros : {
				IdTipoInstrumento : { "field" : "IdTipoInstrumento",  "operator": "eq",  "value" : "" },
			},
			idActual 		: false,
			seleccionado 	: false,
			idDefecto 		: ''
	};
	
	
	$scope.data.oConfiguracionEntidadDestino = {
			tipo 			: "combobox-entidad-destino-garantia",
			configuracion 	: {},
			filtros 		: {IdEstadoEntidadDestinoGarantia : { "field"   : "IdEstadoEntidadDestinoGarantia","operator": "eq", "value"   : "ACT" }},
			idActual 		: false,
			seleccionado 	: false,
			idDefecto 		: ''
	};

	
	//--------------------------------------------
	// GRILLA COBERTURA GARANTIA RESUMEN
	//---------------------------------------------	
	
	$scope.configCoberturaGarantiaResumen = {
			dataSource			: bcsCoberturaGarantia.dsCoberturaGarantiaResumen,
			columns				: bcsCoberturaGarantia.columnasCoberturaGarantiaResumen(),		
			sortable			: true,
			filterable			: true,
			pageable			: true,
			autoBind			: false,
			height				: 470,
		    selectable 			: "row",
		    userSettings 	    : true,
		    excel : {
				allPages: true,
			},
		    
		    rowColorBackground	: function(row) { 
				var color = "";
				if (row.EstadoCoberturaCorredora !== 'DESCUBIERTO')
				{    
   			 	  color = "green";					 	
				}
				else
				{
				 color = "red";	
				}
				return color;
		    }
		};
	

	//--------------------------------------------
	// GRILLA DETALLE COBERTURA GARANTIA
	//---------------------------------------------	
	
	$scope.data.configCoberturaGarantiaDetalle = function (dataItem){	
		var parametros = {};		
		parametros.p_IdEmpresa = $scope.SesionIdEmpresa;
		parametros.p_IdUsuario = $scope.SesionIdUsuario;
		parametros.p_IdModulo = "CUS";
		parametros.p_IdCliente = dataItem.IdCliente;
		parametros.p_IdPortafolio = dataItem.IdPortafolio;
		parametros.p_IdTipoInstrumentoGarantizado = dataItem.IdTipoInstrumentoGarantizado;
		
		var dsCoberturaGarantiaDetalle =  bcsCoberturaGarantia.dsCoberturaGarantiaDetalle();
		dsCoberturaGarantiaDetalle.definition.callParameters = parametros;
		
		return {
			dataSource			: new bcs.DataSource(dsCoberturaGarantiaDetalle),
			columns				: bcsCoberturaGarantia.columnasCoberturaGarantiaDetalle(),		
			sortable			: false,
			pageable			: true,
			filterable			: false,
			autoBind			: true,
			height				: 150,
			selectable 			: "row"
		}
	};
		
	//--------------------------------------------
	// GRILLA COBERTURA GARANTIA OPERACIONES
	//---------------------------------------------
	
	$scope.data.configCoberturaGarantiaOperaciones = function (dataItem){
	    var dsCoberturaGarantiaOperaciones =  bcsCoberturaGarantia.dsCoberturaGarantiaOperaciones;
		var filter = [];
		var filtros = new Array();
		var filtroVentaCorta = new Array();
		var fechaActual = util.dateInicioDia($scope.SesionFechaSistema);
		
		filtroVentaCorta.push({ field: "IdCliente", 	   	operator: "eq",  value: dataItem.IdCliente });
		filtroVentaCorta.push({ field: "IdPortafolio",      operator: "eq",  value: Number(dataItem.IdPortafolio) });
		filtroVentaCorta.push({ field: "IdTipoInstrumentoGarantizado", operator: "eq",  value: dataItem.IdTipoInstrumentoGarantizado });  // PRT - VENTA CORTA
		filtroVentaCorta.push({ field: "IdInstrumentoGarantizado",     operator: "eq",  value: Number(dataItem.IdInstrumentoCarteraGarantizada) }); // Ej.: 2249 (VENSIM)
		filtroVentaCorta.push({ field: "FechaVencimiento",      operator: "gte",  value: fechaActual });
		filtroVentaCorta.push({ field: "EsVentaCorta",      operator: "eq",  value: "S" });
		if (dataItem.IdOperacionOrigenGarantia > 0 ) {
			filtroVentaCorta.push({ field: "FolioCartera",  operator: "eq",  value: dataItem.IdOperacionOrigenGarantia  });
		}
		
		filter.push({ field: "IdCliente", 	                 operator: "eq",  value: dataItem.IdCliente });
		filter.push({ field: "IdPortafolio",                 operator: "eq",  value: Number(dataItem.IdPortafolio) });		
		filter.push({ field: "IdTipoInstrumentoGarantizado", operator: "eq",  value: dataItem.IdTipoInstrumentoGarantizado });            // Ej.: SIM				
		filter.push({ field: "IdInstrumentoGarantizado",     operator: "eq",  value: Number(dataItem.IdInstrumentoCarteraGarantizada) }); // Ej.: 2249 (VENSIM)
		filter.push({ field: "IdContraparteGarantia",        operator: "eq",  value: Number(dataItem.IdContraparteGarantia) });           // Entidad Destino
		//filter.push({ field: "EsEspecifica",               operator: "eq",  value: dataItem.EsEspecifica });

//		if (dataItem.EsEspecifica === 'S') // Si es específica (reporte/vta. corta) solo muestra garantías de la operacion(orden)
//		{
			filter.push({ field: "IdOperacionOrigenGarantia", operator: "eq", value: dataItem.IdOperacionOrigenGarantia });
//		}
			
		filtros = {
				logic: "or",
				filters: [
					{
						logic: "and",
						filters: filter
					},
					{
						logic: "and",
						filters: filtroVentaCorta
					},				
				]
			}
			
		dsCoberturaGarantiaOperaciones.definition.filter = filtros;	    

		return {
			dataSource          : new bcs.DataSource(dsCoberturaGarantiaOperaciones),
			columns				: bcsCoberturaGarantia.columnasCoberturaGarantiaOperaciones(),		
			sortable			: true,
			groupable           : false,
			pageable			: true,
			autoBind			: true,
			height				: 150,
			selectable 			: "row"
		}
	};	
	
	
	
	
	//--------------------------------------------
	// FUNCIONES
	//---------------------------------------------
		
	$scope.buscarGarantiasResumen = function()
	{
			var parametrosConsulta = {};		
			parametrosConsulta.p_IdEmpresa 		= $scope.SesionIdEmpresa;
			parametrosConsulta.p_IdUsuario 		= $scope.SesionIdUsuario;
			parametrosConsulta.p_IdModulo  		= "CUS";			
			parametrosConsulta.p_IdCliente    	= 0;
			parametrosConsulta.p_IdPortafolio 	= 0			
			parametrosConsulta.p_IdTipoInstrumentoGarantizado 		= '';
			parametrosConsulta.p_IdInstrumentoCarteraGarantizada 	= 0			
			parametrosConsulta.p_IdContraparteGarantia 				= 0;
			                                                          
				
			if ($scope.data.oClientePortafolio.seleccionado.IdCliente)
				parametrosConsulta.p_IdCliente = $scope.data.oClientePortafolio.seleccionado.IdCliente;
				
			if ($scope.data.oClientePortafolio.seleccionado.IdPortafolio)
				parametrosConsulta.p_IdPortafolio = $scope.data.oClientePortafolio.seleccionado.IdPortafolio;		
			
			if ($scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento)				
				parametrosConsulta.p_IdTipoInstrumentoGarantizado = $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento;
			
			try {
				if ($scope.data.oConfiguracionInstrumento.seleccionado.IdInstrumento > 0 ) {
					parametrosConsulta.p_IdInstrumentoCarteraGarantizada = $scope.data.oConfiguracionInstrumento.seleccionado.IdInstrumento;
				} else {
					parametrosConsulta.p_IdInstrumentoCarteraGarantizada = 0;
				}
			}catch(e){
				parametrosConsulta.p_IdInstrumentoCarteraGarantizada = 0;
			}
			
			if ($scope.data.oConfiguracionEntidadDestino.seleccionado.IdContraparte)
				parametrosConsulta.p_IdContraparteGarantia = $scope.data.oConfiguracionEntidadDestino.seleccionado.IdContraparte;
			
			bcsCoberturaGarantia.dsCoberturaGarantiaResumen.setCallParameters(parametrosConsulta);
	};
	
	$scope.limpiarFiltros = function() 
	{	
		$scope.data.oConfiguracionTipoInstrumento.idActual	= false;
		$scope.data.oConfiguracionInstrumento.idActual	    = false;		
		$scope.filtros.IdPortafolio							= false;
		$scope.data.oConfiguracionEntidadDestino.idActual   = false;
		
		bcsCoberturaGarantia.dsCoberturaGarantiaResumen.removeAll();
	};
		
	 
	$scope.select = function(grid)
	{
		$timeout(function() {			
			var t = $("."+grid).find("[kendo-grid]");
			for (var i = 0; i < t.length; i++ ) {
				if ($(t[i]).data("kendo-grid")) $(t[i]).data("kendo-grid").refresh()
			}
		},0);
	};
	
	
	$timeout(function(){
		$scope.data.oConfiguracionTipoInstrumento.filtros.read  = true;
	},100);
	
	
});