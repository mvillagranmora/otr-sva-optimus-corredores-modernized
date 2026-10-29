angular.module('ModuloOrdenes').controller('OrdenesEnNegociacionReportesCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionOrdenesV2,bcsServicioOrden){

	var conf = bcsConfiguracionOrdenesV2.getConfiguracionConsultarOrdenesEnNegociacion();
	var idVentana = bcsComunes.ventanaWinId($scope)
	//$scope.defaultOptionsNew = conf.options;
	$scope.filtros = {};
	$scope.filtros.esConfirmada = 'S';
	$scope.filtros.AccionOrden = 'VENTA';
	$scope.idVentana = bcsComunes.ventanaWinId($scope);

	$scope.buscarOrdenes = function(apiGrid,filtros){
		
		conf.buscar(apiGrid,filtros);
		$scope.ordenesSeleccionadas = undefined;
	}
	
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
			
			dataSource: new bcs.DataSource(bcsConfiguracionOrdenesV2.getDataSourceConsultarOrdenesEnNegociacion()),
			columns: [
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdOrden")
				,bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreTipoInstrumento")
				, bcsConfiguracionOrdenesV2.getConfiguracionColumna("AccionOrden")
				//, bcsConfiguracionOrdenesV2.getConfiguracionColumna("InstrumentoUI")
				, bcsConfiguracionOrdenesV2.getConfiguracionColumna("Instrumento")
				, bcsConfiguracionOrdenesV2.getConfiguracionColumna("InstrumentoSubyacente")
				, bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaOperacionOrden")
				, bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaVencimiento")
				, bcsConfiguracionOrdenesV2.getConfiguracionColumna("PlazoReporte")
				
				, bcsConfiguracionOrdenesV2.getConfiguracionColumna("PrecioOrden")
				, bcsConfiguracionOrdenesV2.getConfiguracionColumna("CantidadOrden")								
				, bcsConfiguracionOrdenesV2.getConfiguracionColumna("CantidadDisponibleOrden")
				, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreCondicionDeLiquidacion")
				, bcsConfiguracionOrdenesV2.getConfiguracionColumna("MontoOrden")
				, bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdMonedaOrden")
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
				, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreClasificacionTipoInstrumento")
				, bcsConfiguracionOrdenesV2.getConfiguracionColumna("CantidadAsignadaOrden")
			],
		};

	
	
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
  
	$scope.retirarOfertas = function(ordenesSeleccionadas) {
		
		bcsComunes.ventanaOverlay($scope.idVentana,true);	
		
		var datos = {};
		var ordenes = [];
  		$.each(ordenesSeleccionadas,function(key,orden){
  			ordenes.push({IdOrden : orden.IdOrden});
  			
  		});
  		datos.p_IdEmpresa = $scope.SesionIdEmpresa;
  		datos.p_IdUsuario = $scope.SesionIdUsuario;
  		datos.p_IdModulo = 'ORD';
  		datos.p_Ordenes = ordenes;
  		
  		bcsServicioOrden.RetirarOfertasOrdenes(datos).$promise.then(function(result){
  			$scope.ordenesSeleccionadas = undefined;
  			$scope.buscarOrdenes($scope.apiGridOrdenes,$scope.filtros);
  			bcsComunes.msg("info",$translate.instant('UI_INFORMACION'),'Operacion Exitosa');
  			bcsComunes.ventanaOverlay($scope.idVentana,false);	
//  			 bcsComunes.ventanaMsg({ mensaje : $translate.instant("UI_RETIRAR_OFERTA"), 
//		        	conflictos : util.parseServerResponse(result)}, $scope.idVentana);
  			
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
