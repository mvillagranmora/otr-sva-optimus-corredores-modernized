angular.module('ModuloOrdenes').controller('ConsultarAsignacionesReporteCtrl',function($scope,bcsComunes,bcsConsultasOrdenes,bcsServicioOrden,$translate,bcsAlerta,OPCIONES_BCSALERTA,bcsConfiguracionOrdenes,bcsConfiguracionOrdenesV2){

	var controllerName = 'ConsultarAsignacionesReporteCtrl';

	$scope.data = {};
	$scope.filtros = {};
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	
	$scope.filtros.IdClasificacionTipoInstrumento = 'RE';
	
	$scope.oConfiguracionTipoOperacion={
			filtros : {
				IdClasificacionTipoInstrumento : { "field"   : "IdClasificacionTipoInstrumento","operator": "eq", "value"   : 'RE' }
			
			}
	};
	
	$scope.oConfiguracionOperacion={
			filtros:{
				IdClasificacionTipoInstrumento : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": 'RE'}
			}
	}
	
	$scope.verMapaRelaciones = function (items) {
		bcsComunes.ventanaAbrir("MapaRelaciones", {TipoEntidad : "ASIGNACION",IdEntidad : items[0].IdAsignacion},false,false );
	};
  	
	$scope.oConfiguracionCondicionLiquidacion = {
 			filtros:{
 				AccionTipoOperacion : {"field": "AccionTipoOperacion","operator": "eq","value": undefined},   
 				IdTipoOperacion		: {"field": "IdTipoOperacion","operator":"eq","value": undefined}
 			}
 	}
	
	$scope.limpiarFiltros = function()
	{
		$scope.filtros.buscarInstrumento = "-1";
		$scope.filtros.tipoOperacion = 0;
		$scope.filtros.IdBolsa = 0;
		$scope.filtros.IdEstadoAsignacion = 0;
		$scope.filtros.IdCondicionDeLiquidacion = 0;
		$scope.filtros.fechaDesde = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtros.fechaHasta = util.dateTerminoDia($scope.SesionFechaSistema);
	};

	$scope.exportarLibro = function() {
		var f = $scope.filtros.fechaDesde;
		bcsComunes.ventanaAbrir("ExportarLibro", { tipoLibro: "ASIGNACIONES", libroDefecto : $scope.filtros.IdLibroOrdenes, fecha : f} ,false,false,$scope.idVentana); 
	};
	
	$scope.abrirDetalle = function () {
		bcsComunes.ventanaAbrir("DetalleAsignacion", {EsReporte : 'S',IdAsignacion : $scope.data.kNgModel[0].IdAsignacion} ,false,false,$scope.idVentana);
  	};

  	$scope.data.enviarFacturar = function (asignacionesTemp) {
    	
    	if (!asignacionesTemp || asignacionesTemp.length == 0) return;
		var t = bcsAlerta.crear('PREGUNTA',$translate.instant('UI_ESTA_SEGURO_ENVIAR_FACTURAR_MOVIMIENTOS_SELECCIONADOS'),$scope.idVentana);
		t.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
				var datos = {
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

					var id = bcsComunes.ventanaMsg({
						mensaje: '',
						conflictos: util.parseServerResponse(result)
					},$scope.idVentana)
					$scope.buscarAsignacionesReportes($scope.apiGridAsignaciones,$scope.filtros);
				},function(result){
					bcsComunes.ventanaOverlay($scope.idVentana,false);

					var id = bcsComunes.ventanaMsg({
						mensaje: '',
						conflictos: util.parseServerResponse(result)
					},$scope.idVentana)
				});
			}
		});
    };

    $scope.data.anularAsignaciones = function (asignacionesTemp) {
    	
		if (!asignacionesTemp || asignacionesTemp.length == 0) return;
		var t = bcsAlerta.crear('PREGUNTA',$translate.instant('UI_ADVERTENCIA_ANULACION_ASIGNACIONES'),$scope.idVentana);
		t.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
				
				var datos = {
		    			p_IdEmpresa : $scope.SesionIdEmpresa,
			  			p_IdUsuario : $scope.SesionIdUsuario,
			  			p_IdModulo : 'ORD',
			  			asignacionesParaAnular : [],
		    	};
		    	$.each(asignacionesTemp,function(key,asignacion){
		    		datos.asignacionesParaAnular.push({IdAsignacion : asignacion.IdAsignacion});
			  	});
		    	bcsComunes.ventanaOverlay($scope.idVentana,true);
		    	bcsServicioOrden.AnularAsignacionMasiva(datos).$promise.then(function(result){
		    		bcsComunes.ventanaOverlay($scope.idVentana,false);
		    		var id = bcsComunes.ventanaMsg({
						mensaje: '',
						conflictos: util.parseServerResponse(result)
					},$scope.idVentana)

		    		$scope.buscarAsignacionesReportes($scope.apiGridAsignacionesReportes,$scope.filtros);
		    		
			    	
		    	},function(result){
		    		bcsComunes.ventanaOverlay($scope.idVentana,false);
		    		$scope.buscarAsignacionesReportes($scope.apiGridAsignacionesReportes,$scope.filtros);
		    		
		    	});				
				
			}
		});
    };

    var conf = {};
    conf.options = {
			allowCopy: true,
			autoBind: false,
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
			sortable: true,
			userSettings:true,
			navigatable: true,
			gridCreated : function(grid) {
				$scope.apiGridAsignaciones = grid;
			},
			dataSource: new bcs.DataSource({
	  			type : "odata",
	  			definition : {
					serverFiltering : true, 
					serverPaging : true, 
					serverSorting : true, 
					pageSize : 200,
					sort: { field: "IdAsignacion", dir: "desc" },
					transport: {
						read: {
							url:bcsConsultasOrdenes('ConsultarAsignacionesReporteUI'),
							dataType: "json"
						}
					},
					schema : bcsConfiguracionOrdenesV2.getSchemaAsignacion()
				},
	  		}),
  			columns: [
  				bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaAsignacion",{ groupable: true})
				,bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdOrden",{ groupable: true})
				,bcsConfiguracionOrdenesV2.getConfiguracionColumna("FolioTransaccion",{ groupable: true})
				,bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaOperacionTransaccion",{ groupable: true})
				,bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreTipoInstrumento",{ groupable: true})
				,bcsConfiguracionOrdenesV2.getConfiguracionColumna("AccionOrden",{ groupable: true})
				,bcsConfiguracionOrdenesV2.getConfiguracionColumna("InstrumentoUI",{ groupable: true})
				,bcsConfiguracionOrdenesV2.getConfiguracionColumna("PrecioAsignacion",{ groupable: true})
			 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("CantidadAsignada",{ groupable: true})
			 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("MontoAsignado",{ groupable: true})
			 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("Tasa",{ groupable: true})
			 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("Plazo",{ groupable: true})
			 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdBolsa",{ groupable: true})
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
			 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreFormaCustodia",{ groupable: true})
			 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoEmpleadoPortafolio",{ groupable: true})
			 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoEmpleadoDigitador",{ groupable: true})
			 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("MontoAsignadoPlazo",{ groupable: true})
			 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("DocumentoIdentidad",{ groupable: true})
			 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreCompleto",{ groupable: true})
			 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoPortafolio",{ groupable: true})
			 	,bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreTipoCanalIngreso",{ groupable: true})
				,bcsConfiguracionOrdenesV2.getConfiguracionColumna("IndicadorOrdenDirecta",{ groupable: true})
				,bcsConfiguracionOrdenesV2.getConfiguracionColumna("EsAPlazoUI",{ groupable: true})
				,bcsConfiguracionOrdenesV2.getConfiguracionColumna("EsVentaCortaUI",{ groupable: true})
				,bcsConfiguracionOrdenesV2.getConfiguracionColumna("FolioOrden",{ groupable: true})
				,bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaOperacionOrden",{ groupable: true})
				,bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdAsignacion",{ groupable: true})
			]
			
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
		switch(row["IdEstadoAsignacion"].toUpperCase()) {
			case "ANU":
				return "red";
				break;	
			case "EAF":
				return "green";
				break;
			case "REF":
				return "yellow";
				break;

		}
	};
	
	conf.buscar = function(apiGrid,filtros) {
		$scope.apiGridAsignaciones.dataSource.filter(bcsConfiguracionOrdenesV2.validarFiltrosV2(filtros,'ASIGNACION'));
	}
	
	$scope.defaultOptionsNew = conf.options;
	
	$scope.buscarAsignacionesReportes = function(grid, filters){
		var resp = util.validarRangoFecha(filters.fechaDesde, filters.fechaHasta,365);
		if(!resp.estado){
			conf.buscar(grid,filters);	
		}else{
			bcsComunes.msg("warning", "INFO", resp.mensaje);
		}
		
	};
    
	$scope.seleccionarTodo = function() {	
		$scope.apiGridAsignacionesReportes.select($scope.apiGridAsignacionesReportes.tbody.find(">tr"));		
	};
	
	$scope.limpiarFiltros();

	$scope.data.reAsignacion = function (asignaciones,tipo) {
    	
		var datos = [];
		if (!asignaciones)
			return;
		for (var i = 0 ; i< asignaciones.length ; i++){
			datos.push({"IdMovimiento" : asignaciones[i].IdAsignacion});
		};
		bcsComunes.ventanaAbrir("InterfazAsignacion", { Origen : 'ASIG', Tipo : tipo, movimientos : datos} );		
    };
});
