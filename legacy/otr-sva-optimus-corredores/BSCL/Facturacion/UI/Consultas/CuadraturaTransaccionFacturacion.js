angular.module('ModuloFacturacion').controller('CuadraturaTransaccionFacturacionCtrl',
		function($scope,$translate,$modal,bcsComunes,bcsServiciosFacturacion,
				 bcsServicioPlataforma, bcsCuadraturaTransaccionGrid, $timeout)
{
	
	$scope.data	= {};
	
	var idVentana = bcsComunes.ventanaWinId ($scope);
	bcsComunes.ventanaAutoOverlay(idVentana,false,3000);
		
	//Seteo de variables de sistema
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdModulo	= 'FAC';
	$scope.data.btnActivos  = true;   //Activa botones
	
	$scope.data.oFechaTransaccion = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
	
	//GRILLA PRINCIPAL DE MOVIMIENTOS A FACTURAR
	$scope.confBasicaCuadraturaTransaccion = { 
		dataSource			: bcsCuadraturaTransaccionGrid.dataSourceCuadraturaTransaccion,
		columns				: bcsCuadraturaTransaccionGrid.columnasCuadraturaTransaccion(),
		sortable			: true,
		pageable			: true,
		filterable			: true,
		userSettings        : true,
		////autoBind			: true,
		height				: 450,
	    selectable 			: "multiple row",
	    navigatable         : true,
		allowCopy 			: true
		
	};
		
	$scope.confBasicaCuadraturaTransaccion.rowColorBackground = function(row) {
		switch(row["TienePendientes"].toUpperCase()) {
			case "PENDIENTE":
				return "red";
				break;
		}
	};
	
	//SUB-GRILLA DE DETALLE 
	$scope.confBasicaCuadraturaTransaccionDetalle = function(dataItem)
	  {
		
		var filtrosCuadraturaTransaccionDetalle = [{field: "IdTransaccion", operator: "eq", value: Number(dataItem.IdTransaccion)}];
		return {
			  dataSource		: bcsCuadraturaTransaccionGrid.dataSourceCuadraturaTransaccionDetalle(filtrosCuadraturaTransaccionDetalle),
			  columns			: bcsCuadraturaTransaccionGrid.columnasCuadraturaTransaccionDetalle(),		
			  sortable			: true,
			  pageable			: true,		
			  height			: 120,
			  width				: 300,
			  autoBind          : true,
			  selectable 		: "multiple row",
			  navigatable       : true,
			  allowCopy 		: true
	       };
	  };
	  
	  
	  
	//COMBOBOX
	$scope.data.oClasificacionTipoInstrumento={
        tipo : "combobox-clasificacion-tipo-instrumento",
	    configuracion : {},
	    filtros : { 	
		   		    IdEstado :	{field : "IdEstado", operator : "eq", value : "ACT"}
			      },
	    idActual : false,
	    seleccionado : false,
	    idDefecto : ''
	};  
	
	
	$scope.data.oTipoInstrumento={
		tipo : "combobox-tipo-instrumento",
		configuracion : {},
	    filtros : {
		 		    IdClasificacionTipoInstrumento : { "field"   : "IdClasificacionTipoInstrumento", "operator": "eq", "value"   : ""} 	
			      },
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	  
	
	
	  
	//OPCIONES	
	$scope.buscar = function(){
		var filters = new Array();	
		
		if ($scope.data.oFolioTransaccion)
		    filters.push({ "field": "FolioTransaccion","operator": "eq", "value" : $scope.data.oFolioTransaccion});
		if ($scope.data.oFechaTransaccion)
			filters.push({ "field": "FechaTransaccion", "operator": "eq", "value": $scope.data.oFechaTransaccion});
		if ($scope.data.oSoloDescuadratura)
			filters.push({ "field": "CantidadPorFacturar", "operator": "neq", "value": 0});
		if ($scope.data.oClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento)
			filters.push({ "field": "IdClasificacionTipoInstrumento", "operator": "eq", "value": $scope.data.oClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento});
		if ($scope.data.oTipoInstrumento.seleccionado.IdTipoInstrumento)
			filters.push({ "field": "IdTipoInstrumento", "operator": "eq", "value": $scope.data.oTipoInstrumento.seleccionado.IdTipoInstrumento});
		
				
		$scope.gridApiCuadraturaTransaccion.dataSource.filters(filters);       
	};
	
		
	$scope.limpiarFiltros = function()
	  {			
		$scope.data.oFechaTransaccion = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
        $scope.data.oClasificacionTipoInstrumento.idActual = 0;
        $scope.data.oTipoInstrumento.idActual = 0;
		$scope.gridApiCuadraturaTransaccion.dataSource.removeAll();	
 	  };
});