angular.module('ModuloOrdenes').controller('OrdenesPorNegociarReportesCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionOrdenes,bcsConfiguracionOrdenesV2,bcsServicioOrden){
	
	var controllerName = 'OrdenesPorNegociarReportesCtrl';
	var idVentana = bcsComunes.ventanaWinId($scope)
	$scope.defaultOptionsNew = {
		allowCopy: true,
		autoBind: false,
		excel : {
			allPages: true,
		},
  		filterable: true,
		groupable: true,
		height:300,
		pageable: true,
		reorderable: true,
		resizable: true,
  		rowColorBackground : function(row) {
	  		switch(row.get("AccionOrden")) {
	            case 'VENTA':
	                return "red";
	                break;
	            case 'COMPRA':
	                return "green";
	                break;
	        }
	  	},
	  	scrollable: true,
  		selectable: "multiple, row",
		sortable: true,
		userSettings:true,
		navigatable: true,
		
		dataSource: new bcs.DataSource(bcsConfiguracionOrdenesV2.getDataSourceConsultarOrdenesPorNegociar()),
		columns: [
			bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdOrden")
			,bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreTipoInstrumento")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("AccionOrden")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("Instrumento")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("InstrumentoSubyacente")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaOperacionOrden")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaVencimiento")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("PrecioOrden")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("TasaPromedioReporte")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("CantidadOrden")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("PlazoReporte")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreCondicionDeLiquidacion")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("MontoOrden")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdMonedaOrden")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdMonedaLiquidacionOrden")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("DocumentoIdentidad")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreCompleto")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoPortafolio")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("EstadoOrden")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaVigencia")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreFormaCustodia")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoEmpleadoPortafolio")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreEmpleadoPortafolio")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreEmpleadoDigitador")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("TipoPrecio")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreCanal")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdBolsa")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreLibroOrdenes")
	 		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("EstadoOrdenAsignacion")
		],
	};
	
	$scope.confGarantia  = function(filtros) {
		var config = {};
		var defDataSource = bcsConfiguracionOrdenesV2.getDataSourceConsultarGarantias();
		defDataSource.definition.filter = [{ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa}
									,{ field: "IdPortafolio", operator: "eq", value: filtros.IdPortafolio}
									//,{field : "IdInstrumento", operator : "eq", value: filtros.IdInstrumentoSubyacente}
									,{field : "IdOperacionOrigenGarantia", operator : "eq", value: filtros.IdOrden}];//falta ver el id operacion para ver las garantias especificas
		
		config = {
			
			options : {
				sortable: false,
	      		reorderable: false,
	      		filterable: false,
				groupable: false,
				pageable: true,
				selectable: "multiple, row",
				reorderable: false,
				allowCopy: true,
				resizable: true,
	      		autoBind: true,
	      		scrollable: true,
	      		height:150,

				dataSource: new bcs.DataSource(defDataSource),
	  			columns: [
	  					 //bcsConfiguracionOrdenesV2.getConfiguracionColumna("FolioGarantia")
						bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaIngresoRegistro")
						,bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreEstado")
						,bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreConcepto")
						,bcsConfiguracionOrdenesV2.getConfiguracionColumna("Nemotecnico")
						,bcsConfiguracionOrdenesV2.getConfiguracionColumna("CantidadGarantia")
						,bcsConfiguracionOrdenesV2.getConfiguracionColumna("MontoValorizado")
						
				],
				
			}
		};
		return config.options;
	};

	$scope.buscarOrdenes = function(apiGrid,filtros){
		$scope.apiGridOrdenes.dataSource.filter(bcsConfiguracionOrdenesV2.validarFiltrosV2($scope.filtros,'ORDEN'));
		$scope.ordenesSeleccionadas = undefined;
		$scope.sumaCompras = 0; 
  		$scope.sumaVentas = 0; 
  		$scope.precioMenor = 0;
  		$scope.precioMayor = 0;
	}
		
	$scope.filtros = {};
	$scope.filtros.esConfirmada = 'S';
	$scope.filtros.AccionOrden = 'VENTA';
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	
	$scope.limpiarFiltros = function()
	{
		$scope.filtros.buscarInstrumento = "-1";
		$scope.filtros.AccionOrden = 'VENTA';
		$scope.filtros.IdTipoInstrumento = 0;
		$scope.filtros.IdClasificacionTipoInstrumento = 'RE';
		$scope.filtros.fechaOperacion = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtros.esConfirmada = 'S';
	};

	$scope.abrirDetalle = function (items) {
		bcsComunes.ventanaAbrir("DetalleOrden", {IdOrden : items[0].IdOrden} );
	};
  
	$scope.$watch('ordenesSeleccionadas',function(newval,oldval) {
		if (!newval) return;
		$scope.sumaCompras = 0;
		$scope.sumaVentas = 0;
		$scope.precioMayor = 0;
		$scope.precioMenor = 0;
		var mayor = -1;
		var menor = 999999999999999;
		$.each(newval,function(key,value){
			if (value.AccionOrden === 'COMPRA') {
				$scope.sumaCompras = $scope.sumaCompras + value.CantidadOrden;
			}
			if (value.AccionOrden === 'VENTA') {
				$scope.sumaVentas = $scope.sumaVentas + value.CantidadOrden;
			}
			
			menor = Number(value.PrecioOrden)<menor?Number(value.PrecioOrden):menor;
			mayor = Number(value.PrecioOrden)>mayor?Number(value.PrecioOrden):mayor;
		})
		$scope.precioMenor = menor != 999999999999999?menor:0;
		$scope.precioMayor = mayor != -1?mayor:0;
		bcsComunes.ventanaAutoHeight(idVentana);
	});

	$scope.cantarOrdenes = function(ordenesSeleccionadas) {
		
		bcsComunes.ventanaOverlay($scope.idVentana,true);		
		var datos = {};
		var ordenes = [];
  		$.each(ordenesSeleccionadas,function(key,orden){
  			ordenes.push({IdInterno: orden.IdOrden, IdOrden : orden.IdOrden , IdBolsa : ''});
  			
  		});
  		datos.p_IdEmpresa = $scope.SesionIdEmpresa;
  		datos.p_IdUsuario = $scope.SesionIdUsuario;
  		datos.p_IdModulo = 'ORD';
  		datos.p_Ordenes = ordenes;
  		
  		$scope.sumaCompras = 0; 
  		$scope.sumaVentas = 0; 
  		$scope.precioMenor = 0;
  		$scope.precioMayor = 0;
  		
  		bcsServicioOrden.CantarOrdenes(datos).$promise.then(function(result){
  			$scope.ordenesSeleccionadas = undefined;
  			$scope.buscarOrdenes($scope.apiGridOrdenes,$scope.filtros);
  			bcsComunes.msg("info",$translate.instant('UI_INFORMACION'),'Operacion Exitosa');
  			bcsComunes.ventanaOverlay($scope.idVentana,false);	
  			
  		},function(result){
  			bcsComunes.ventanaOverlay($scope.idVentana,false);	
  			$scope.buscarOrdenes($scope.apiGridOrdenes,$scope.filtros);
  		});
	};
	
	$scope.rutearOrdenes = function(ordenesSeleccionadas,operador,bolsaDistribucion) {
		
		if (!ordenesSeleccionadas || ordenesSeleccionadas.length === 0) return;

		bcsComunes.ventanaOverlay($scope.idVentana,true);		
		var datos = {};
		var ordenes = [];
  		$.each(ordenesSeleccionadas,function(key,orden){
  			ordenes.push(
  			{
  				IdOrden : orden.IdOrden , 
  				IdBolsaDistribucion : bolsaDistribucion,
  				CodigoOperador : operador,
  				IdTipoCanalDistribucion : bcsConfiguracionOrdenes.CONSTANTES.IdTipoCanalDistribucionAgente,
  				});
  			
  		});
  		datos.p_IdEmpresa = $scope.SesionIdEmpresa;
  		datos.p_IdUsuario = $scope.SesionIdUsuario;
  		datos.p_IdModulo = 'ORD';
  		datos.p_Ordenes = ordenes;
  		
  		bcsServicioOrden.RutearOrdenes(datos).$promise.then(function(result){
  			$scope.buscarOrdenes($scope.apiGridOrdenes,$scope.filtros);
  			bcsComunes.msg("info",$translate.instant('UI_INFORMACION'),'Operacion Exitosa');
  			bcsComunes.ventanaOverlay($scope.idVentana,false);	
  			
  		},function(result){
  			bcsComunes.ventanaOverlay($scope.idVentana,false);	
  			$scope.buscarOrdenes($scope.apiGridOrdenes,$scope.filtros);
  		});
	};
	
	$scope.oConfiguracionClasificacion = {
			filtros : {
				IdClasificacionTipoInstrumento : {
					field:"IdClasificacionTipoInstrumento",
					operator:"eq",
					value:"RE"
				}
			}
	}
	
	$scope.oConfiguracionTipoInstrumento={
			filtros : { 	
				IdClasificacionTipoInstrumento : {
			        "field"   : "IdClasificacionTipoInstrumento",
			        "operator": "eq",
			        "value"   : "RE"
				}				
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.limpiarFiltros();
	bcsComunes.ventanaAutoHeight(idVentana);
	
});
