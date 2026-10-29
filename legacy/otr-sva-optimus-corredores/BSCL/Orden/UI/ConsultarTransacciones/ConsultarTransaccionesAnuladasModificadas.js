angular.module('ModuloOrdenes').controller('ConsultarTransaccionesAnuLModCtrl',function($scope,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,$translate,bcsConfiguracionOrdenesV2)
		                                                                                
{
	//var controllerName = 'ConsultarTransaccionesCtrl';
	
	$scope.datosForm = {};	
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	$scope.datosBusqueda = {};
	
	//COMBOBOX
	$scope.datosBusqueda.oConfiguracionEstadoTransAnuMod={
		tipo : "combobox-estado-transaccionbolsa",
		configuracion : {},
		filtros : {
			        NombreEntidad : {field: "NombreEntidad", operator: "eq", value: ("TRANSACCIONBOLSA")}
		          },
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	$scope.datosForm.defaultOptionsNew = {
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
      		 
			dataSource: new bcs.DataSource(bcsConfiguracionOrdenesV2.getDataSourceConsultarTransaccionesAnuMod()),
  			columns: [
  				 bcsConfiguracionOrdenesV2.getConfiguracionColumna("FolioTransaccion")
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaOperacionTransaccion",{ groupable: true })
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreClasificacionTipoInstrumento",{ groupable: true})
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("InstrumentoUI",{ groupable: true})
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("CantidadTransaccion",{ groupable: true})
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("PrecioTransaccion",{ groupable: true})
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("MontoTransaccion",{ groupable: true})
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaLiquidacion",{ groupable: true})
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaVencimiento",{ groupable: true})			
	    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("EstadoTransaccion",{ groupable: true})
			],
		};
	
	$scope.datosForm.buscarTransacciones = function() {
		var filters = new Array();
		
		if ($scope.datosBusqueda.IdClasificacionTipoInstrumento)
	        filters.push( { "field": "IdClasificacionTipoInstrumento","operator": "eq", "value" : $scope.datosBusqueda.IdClasificacionTipoInstrumento});
		
		
		if ($scope.datosBusqueda.instrumento !== undefined){    		
	    	if ($scope.datosBusqueda.instrumento.IdInstrumento != "" && $scope.datosBusqueda.instrumento.IdInstrumento !== undefined)	    		
	    		filters.push({ field: "IdInstrumento", operator: "eq", value: Number($scope.datosBusqueda.instrumento.IdInstrumento) });
    	}
		
		if ($scope.datosBusqueda.instrumento !== undefined){    		
	    	if ($scope.datosBusqueda.instrumento.IdInstrumento != "" && $scope.datosBusqueda.instrumento.IdInstrumento !== undefined)	    		
	    		filters.push({ field: "IdInstrumento", operator: "eq", value: Number($scope.datosBusqueda.instrumento.IdInstrumento) });
    	} 
		
		
		if ($scope.datosBusqueda.oConfiguracionEstadoTransAnuMod.seleccionado.IdEstado)
			filters.push( {field: "IdEstado", operator : "eq", value : $scope.datosBusqueda.oConfiguracionEstadoTransAnuMod.seleccionado.IdEstado});
				
		
		if ($scope.datosBusqueda.fechaDesde)
			filters.push( { "field": "FechaOperacionTransaccion","operator": "gte", "value" :util.dateInicioDia($scope.datosBusqueda.fechaDesde)});
						
		if ($scope.datosBusqueda.fechaHasta)
  		    filters.push( { "field": "FechaOperacionTransaccion","operator": "lte", "value" :util.dateTerminoDia($scope.datosBusqueda.fechaHasta)});
		
		$scope.datosForm.apiGridTransaccionesAnuMod.dataSource.filter(filters);
		
	};
	
	$scope.datosForm.limpiarFiltros = function()
	{
		$scope.datosBusqueda.setInstrumento = "-1";
		$scope.datosBusqueda.IdClasificacionTipoInstrumento = 0;
		$scope.datosBusqueda.fechaDesde = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.datosBusqueda.fechaHasta = util.dateTerminoDia($scope.SesionFechaSistema);
		
	};
  	
  	
  	$scope.datosForm.seleccionarTodo = function() {	
  		$scope.datosForm.apiGridTransaccionesAnuMod.select($scope.datosForm.apiGridTransaccionesAnuMod.tbody.find(">tr"));		
	};
	
	$scope.datosForm.limpiarFiltros();
});
