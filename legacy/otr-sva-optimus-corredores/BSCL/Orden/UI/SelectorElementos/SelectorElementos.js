angular.module('ModuloOrdenes').controller('SelectorElementosCtrl',function($scope,bcsServicioOrdenV2,bcsComunes,bcsConfiguracionOrdenesV2){
			
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	var conf = {
			buscar : function(apiGrid,filtros) {
				apiGrid.dataSource.filter(bcsConfiguracionOrdenesV2.validarFiltrosV2(filtros,'ORDEN','PORASIGNAR'));
			},
			dataSource : new bcs.DataSource(bcsConfiguracionOrdenesV2.getDataSourceConsultarOrdenesPorAsignar()),
			options : {
				allowCopy: true,
				autoBind: true,
	      		filterable: false,
				groupable: false,
				height:300,
				pageable: false,
				reorderable: true,
				resizable: true,
	      		selectable: "multiple, row",
				scrollable: true,
	      		sortable: true,
	      		userSettings:true,
				navigatable: true,
				
				columns: [
					bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdOrden")
						, bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaOperacionOrden")
						, bcsConfiguracionOrdenesV2.getConfiguracionColumna("PrecioOrden")
						, bcsConfiguracionOrdenesV2.getConfiguracionColumna("CantidadDisponibleOrden")
						, bcsConfiguracionOrdenesV2.getConfiguracionColumna("CantidadOrden")
						, bcsConfiguracionOrdenesV2.getConfiguracionColumna("InstrumentoUI")
						, bcsConfiguracionOrdenesV2.getConfiguracionColumna("EsVentaCortaUI")
						, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreTipoOperacion")
						, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreCondicionDeLiquidacion")
						, bcsConfiguracionOrdenesV2.getConfiguracionColumna("MontoOrden")
						, bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdMonedaOrden")
						, bcsConfiguracionOrdenesV2.getConfiguracionColumna("DocumentoIdentidad")
						, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreCompleto")
						, bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoPortafolio")
						, bcsConfiguracionOrdenesV2.getConfiguracionColumna("EstadoOrden")
						, bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaVigencia")
						, bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaRecepcion")
						, bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoEmpleadoPortafolio")
						, bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoEmpleadoDigitador")
						, bcsConfiguracionOrdenesV2.getConfiguracionColumna("TipoPrecio")
						, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreCanal")
						, bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdBolsa")
						, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreLibroOrdenes")
				 		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("EstadoOrdenAsignacion")
						, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreTipoInstrumento")
						, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreClasificacionTipoInstrumento")
						, bcsConfiguracionOrdenesV2.getConfiguracionColumna("CantidadAsignadaOrden")
						, bcsConfiguracionOrdenesV2.getConfiguracionColumna("InstrumentoNegociacionOrden")
				]
				
			}
	};
	
	if ($scope.SesionLocale === "PE") {
		conf.options.columns.splice(22,0,
			bcsConfiguracionOrdenesV2.getConfiguracionColumna("OrdenEspecialUI",{ groupable: true})
		);
	}
	
	var f = bcsConfiguracionOrdenesV2.validarFiltrosV2(parametros,"ORDEN","PORASIGNAR");
	conf.dataSource.definition.filter = f;
	
	$scope.configuracion = conf.options;
	$scope.configuracion.dataSource = new bcs.DataSource(conf.dataSource);
	$scope.filtros = {};  

	$scope.formData = {
			filtrosTipoInstrumento : {
    				IdClasificacionTipoInstrumento : {field   : "IdClasificacionTipoInstrumento",operator: "eq",value   : ""}
			},
			filtrosCondicionDeLiquidacion : {
				IdTipoOperacion : {field   : "IdTipoOperacion",operator: "eq",value   : ""}
			},
			filtrosOperacion : {
				IdTipoInstrumento : {field   : "IdTipoInstrumento",operator: "eq",value   : ""}
			}
			
	};
	
	$scope.defecto = {
			AccionOrden : parametros.AccionOrden,
			IdClasificacionTipoInstrumento : parametros.IdClasificacionTipoInstrumento,
			IdTipoInstrumento : parametros.IdTipoInstrumento,
			IdLibroOrdenes : parametros.IdLibroOrdenes,
			IdBolsa : parametros.IdBolsa,
			instrumento: (!!parametros.instrumento && parametros.instrumento.IdTipoOperacion!=="COMSIM") ? parametros.instrumento.Instrumento : undefined,
			IdCondicionDeLiquidacion : parametros.IdCondicionDeLiquidacion,
			EsVentaCorta : parametros.EsVentaCorta
	};
	
	$scope.filtros.EsVentaCorta = parametros.EsVentaCorta;
	
	$scope.buscar = function() {
		conf.buscar($scope.apiGrid,$scope.filtros);
	};
	
	$scope.aceptar = function() {
		bcsComunes.ventanaData(idVentana, $scope.seleccionado);
		bcsComunes.ventanaCerrar(idVentana);
	};
	
	$scope.cancelar = function() {
		bcsComunes.ventanaData(idVentana, undefined);
		bcsComunes.ventanaCerrar(idVentana);
	};




});
