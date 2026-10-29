angular.module('ModuloOrdenes').controller('ConsultarHistorialTransaccionesCtrl',function($scope,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,$translate,bcsConfiguracionOrdenesV2,bcsServicioOrden)
		                                                                                
{
	//var controllerName = 'ConsultarTransaccionesCtrl';
	
	$scope.datosForm = {};	
	var idVentana = bcsComunes.ventanaWinId($scope);
	$scope.datosBusqueda = {};
	
	//COMBOBOX
	$scope.datosBusqueda.oConfiguracionEstadoTransAnuMod={
		tipo : "combobox-estado-transaccionbolsa",
		configuracion : {},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	$scope.datosForm.defaultOptions = {
			autoBind: false,
      		allowCopy: true,
      		excel : {
				allPages: true,
			},
			
			filterable: true,
			groupable: true,
			height:400,
			pageable: true,
      		reorderable: true,
			resizable: true,
      		selectable: "multiple, row",
      		scrollable: true,
      		sortable: true,
      		userSettings:true,
			navigatable: true,
      		 
			dataSource: new bcs.DataSource(bcsConfiguracionOrdenesV2.getDataSourceConsultarTransaccionesHistorico()),
  			columns: [
  				bcsConfiguracionOrdenesV2.getConfiguracionColumna("FolioTransaccion",{ groupable: true })
  				,bcsConfiguracionOrdenesV2.getConfiguracionColumna("TipoRegistroUI",{ groupable: true })
  				,bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdEstadoRegistroUI",{ groupable: true })
  				,bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaIngresoRegistroHistorico")
  				,bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaEjecucionRegistro")
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaOperacionTransaccion",{ groupable: true })
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdClasificacionTipoInstrumento",{ groupable: true})
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreTipoOperacion",{ groupable: true})
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("InstrumentoUI",{ groupable: true})
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("CantidadTransaccion",{ groupable: true})
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("PrecioTransaccion",{ groupable: true})
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("PrecioStrike",{ groupable: true})
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("Tasa",{ groupable: true})
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("CantidadDisponibleTransaccion",{ groupable: true})
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoCondicionDeLiquidacion",{ groupable: true})
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdEmisor",{ groupable: true})
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("Plazo",{ groupable: true})
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaVencimiento",{ groupable: true})
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("IndicadorOrdenDirecta",{ groupable: true})		    		
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("EsBilateralUI",{ groupable: true})		    		
	    		
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreCorredorContraparte",{ groupable: true})
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdTipoInstrumento",{ groupable: true})
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("NemotecnicoSVS",{ groupable: true})
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("EsFungibleUI",{ groupable: true})
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdBolsa",{ groupable: true})
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdBoveda",{ groupable: true})
				,bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaLiquidacion",{ groupable: true})
				,bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdTipoReajuste",{ groupable: true})
				,bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdMonedaTransaccion",{ groupable: true})
				,bcsConfiguracionOrdenesV2.getConfiguracionColumna("MontoTransaccion",{ groupable: true})
				,bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoOperadorComprador",{ groupable: true })
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoOperadorVendedor",{ groupable: true })
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("MarcaNegociacion",{ groupable: true})
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("EsAPlazoUI",{ groupable: true})
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("EsVentaCortaUI",{ groupable: true})
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoEmpleadoReserva",{ groupable: true})
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaModificacionOperacion",{ groupable: true})
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("CantidadAsignadaTransaccion",{ groupable: true})
				
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreLibroOrdenes",{ groupable: true})
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreSistemaBursatil",{ groupable: true})
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreTipoInstrumento",{ groupable: true})
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("CorteMinimo",{ template : " <label> #:  (util.bcs_formato(CorteMinimo,'GEN','Cantidad',IdMonedaTransaccion,'','')).valor #</label>", groupable: true})
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("EstadoTransaccion",{ groupable: true})
	    		
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("ModalidadLiquidacion",{ groupable: true})
				,bcsConfiguracionOrdenesV2.getConfiguracionColumna("ModalidadEjercicio",{ groupable: true})
				,bcsConfiguracionOrdenesV2.getConfiguracionColumna("TipoOpcion",{ groupable: true})
				,bcsConfiguracionOrdenesV2.getConfiguracionColumna("EstiloOpcion",{ groupable: true})
				,bcsConfiguracionOrdenesV2.getConfiguracionColumna("Tamano",{ groupable: true})
			],
		};
	
	$scope.datosForm.buscarTransacciones = function() {
		var filters = new Array();
		
		if ($scope.datosBusqueda.IdClasificacionTipoInstrumento)
	        filters.push( { "field": "IdClasificacionTipoInstrumento","operator": "eq", "value" : $scope.datosBusqueda.IdClasificacionTipoInstrumento});
		
		if ($scope.datosBusqueda.IdTipoInstrumento)
	        filters.push( { "field": "IdTipoInstrumento","operator": "eq", "value" : $scope.datosBusqueda.IdTipoInstrumento});
		
		if ($scope.datosBusqueda.AccionOrden)
	        filters.push( { "field": "AccionOrden","operator": "eq", "value" : $scope.datosBusqueda.AccionOrden});
		
		
		if ($scope.datosBusqueda.instrumento !== undefined){    		
	    	if ($scope.datosBusqueda.instrumento.IdInstrumento != "" && $scope.datosBusqueda.instrumento.IdInstrumento !== undefined)	    		
	    		filters.push({ field: "IdInstrumento", operator: "eq", value: Number($scope.datosBusqueda.instrumento.IdInstrumento) });
    	}
				
		if ($scope.datosBusqueda.TipoRegistro)
			filters.push( {field: "TipoRegistro", operator : "eq", value : $scope.datosBusqueda.TipoRegistro});
		
		if ($scope.datosBusqueda.fechaDesde)
			filters.push( { "field": "FechaOperacionTransaccion","operator": "gte", "value" :util.dateInicioDia($scope.datosBusqueda.fechaDesde)});
						
		if ($scope.datosBusqueda.fechaHasta)
  		    filters.push( { "field": "FechaOperacionTransaccion","operator": "lte", "value" :util.dateTerminoDia($scope.datosBusqueda.fechaHasta)});
		
		if ($scope.datosBusqueda.IdCondicionDeLiquidacion)
			filters.push( {field: "IdCondicionDeLiquidacion", operator : "eq", value : $scope.datosBusqueda.IdCondicionDeLiquidacion});
		
		if ($scope.datosBusqueda.IdLibroOrdenes)
			filters.push( {field: "IdLibroOrdenes", operator : "eq", value : $scope.datosBusqueda.IdLibroOrdenes});
		
		if ($scope.datosBusqueda.MarcaNegociacion)
			filters.push( {field: "MarcaNegociacion", operator : "eq", value : $scope.datosBusqueda.MarcaNegociacion});
		
		if ($scope.datosBusqueda.EsVentaCorta)
			filters.push( {field: "EsVentaCorta", operator : "eq", value : 'S'});
		
		if ($scope.datosBusqueda.EsPendiente)
			filters.push( {field: "IdEstadoRegistro", operator : "eq", value : 'PEN'});
		
		$scope.datosForm.apiGridTransaccionesHistorico.dataSource.filter(filters);
		
	};
	
	$scope.datosForm.limpiarFiltros = function()
	{
		$scope.datosBusqueda.setInstrumento = "-1";
		$scope.datosBusqueda.IdClasificacionTipoInstrumento = 0;
		$scope.datosBusqueda.IdTipoInstrumento = 0;
		$scope.datosBusqueda.AccionOrden = 0;
		$scope.datosBusqueda.TipoRegistro = 0;
		$scope.datosBusqueda.fechaDesde = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.datosBusqueda.fechaHasta = util.dateTerminoDia($scope.SesionFechaSistema);
		$scope.datosBusqueda.IdCondicionDeLiquidacion = 0;
		$scope.datosBusqueda.IdLibroOrdenes = 0;
		$scope.datosBusqueda.MarcaNegociacion = "";
		$scope.datosBusqueda.EsVentaCorta = false;
		$scope.datosBusqueda.EsPendiente = false;
	};
  	
  	
  	$scope.datosForm.seleccionarTodo = function() {	
  		$scope.datosForm.apiGridTransaccionesAnuMod.select($scope.datosForm.apiGridTransaccionesAnuMod.tbody.find(">tr"));		
	};
	
	$scope.datosForm.procesarTransaccion = function(transaccion){
		if(!transaccion) return;
		bcsComunes.ventanaOverlay(idVentana,true);
		var datos = {
    			p_IdEmpresa   : $scope.SesionIdEmpresa,
	  			p_IdUsuario   : $scope.SesionIdUsuario,
	  			p_IdModulo 	  : 'ORD',
	  			p_UsaReglasNegocio :'S',
	  			p_Transacciones : [transaccion.IdTransaccionHistorico]
			}
		bcsServicioOrden.ReprocesarTransaccion(datos).$promise.then(function(respuesta)
		{
			bcsComunes.ventanaOverlay(idVentana,false);
			bcsComunes.ventanaMsg({
				conflictos:util.parseServerResponse(respuesta)
			},$scope.idVentana);
		},function(respuesta)
		{
			bcsComunes.ventanaOverlay(idVentana,false);
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant('UI_ERROR'),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.idVentana);
		});
		
	}
	
	$scope.datosForm.limpiarFiltros();
});
