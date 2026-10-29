angular.module('ModuloTesoreria').controller('ConsultarTransaccionesTesoreriaCtrl', function($scope,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,$translate,bcsBlotterGestionContrapartesGrid)
{
	
	$scope.datosForm = {};
	$scope.datosBusqueda = {};
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	
	$scope.datosForm.filtrosTipoInstrumento = {
			IdClasificacionTipoInstrumento : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": ""}
	};
	
	$scope.datosForm.filtrosOperacion = {
			EsTransaccion : {"field": "EsTransaccion","operator": "eq","value": "S"}
	};
	
	$scope.datosForm.filtrosCondicionDeLiquidacion = {
			AccionTipoOperacion : {"field": "AccionTipoOperacion","operator": "eq","value": ""}
	};
	
	
	var getDataSourceConsultarTransacciones = function() {
		return {
  			type : "odata",
  			definition : {
				serverFiltering : true,
				serverPaging 	: true,
				serverSorting 	: true,
				pageSize		: 200,
				sort			: { field: "FechaLiquidacion", dir: "desc" },
				transport: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1201",
						dataType: "json"
					}
				},
				schema 			: bcsBlotterGestionContrapartesGrid.getSchemaTransaccion()
			},
  		}
	}
	
	
	
	
	
	$scope.datosForm.defaultOptionsNew = {
			dataSource		: new bcs.DataSource(getDataSourceConsultarTransacciones()),			
			columns			: bcsBlotterGestionContrapartesGrid.getColumnTransaccion(),
			autoBind		: false,
      		allowCopy		: true,      		
      		excel 			: {allPages: true},
			rowCellColor 	: function(row) {
				if (!row["AccionOrden"]) return;
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
				switch(row["IdEstadoTransaccion"].toUpperCase()) {
						case "ANU":
							return "red";
							break;	
						case "ASP":
							return "yellow";
							break;
						case "ASC":
							return "green";
							break;
						case "SAS":
							return "green";
							break;
					}
			},
			filterable		: true,
			groupable		: true,
			height			: 430,
			pageable		: true,
      		reorderable		: true,
			resizable		: true,
      		selectable		: "row",
      		scrollable		: true,
      		sortable		: true,
      		userSettings	: true,
			navigatable		: true,			
			gridCreated     : function(g){
				$scope.ApiTransacciones = g;
				
				if ($scope.SesionLocale === 'PE')
				{
					$scope.ApiTransacciones.showColumn('Intereses'); 		// Para RF
					$scope.ApiTransacciones.showColumn('Reajustes'); 		// Para RF					
					$scope.ApiTransacciones.showColumn('MontoRetencion'); 	// Monto retenido por cavali para liquidacion
					$scope.ApiTransacciones.showColumn('MarcaIGKUI');     	// Confirmación vavali para liquidación(a través de archivo IGK) 
					
				}
			},
		};



		var validarFiltros = function(filtros)
		{
			var filters = new Array();
			
			filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa});
			if (filtros.IdClasificacionTipoInstrumento)
				filters.push({ field: "IdClasificacionTipoInstrumento", operator: "eq", value: filtros.IdClasificacionTipoInstrumento });
			if (filtros.IdTipoInstrumento)
				filters.push({ field: "IdTipoInstrumento", operator: "eq", value: filtros.IdTipoInstrumento });
			if (filtros.instrumento && filtros.instrumento.IdInstrumento)
				filters.push({ field: "IdInstrumentoUI", operator: "eq", value: Number(filtros.instrumento.IdInstrumento) });
			if (filtros.IdTipoOperacion)
				filters.push({ field: "IdTipoOperacion", operator: "eq", value: filtros.IdTipoOperacion });
			if (filtros.AccionOrden === 'COMPRA' || filtros.AccionOrden === 'VENTA')
				filters.push({ field: "AccionOrden", operator: "eq", value: filtros.AccionOrden });
			if (filtros.IdCondicionDeLiquidacion)
				filters.push({ field: "IdCondicionDeLiquidacion", operator: "eq", value: filtros.IdCondicionDeLiquidacion });
			if (filtros.cantidadDisponible)
				filters.push({ field: "CantidadDisponible", operator: "gt", value: 0});
			if (filtros.esAPlazo)
				filters.push({ field: "EsAPlazo", operator: "eq", value: filtros.esAPlazo});
			if (filtros.OrdenPorAsignar)
			{
				filters.push({ field: "CantidadDisponibleOrden", operator: "gt", value: 0});
				filters.push({ field: "IdEstadoOrden", operator: "eq", value: "OFE"});
			}
			if (filtros.instrumentoSubyacente && filtros.instrumentoSubyacente.IdInstrumento)
				filters.push({ field: "IdInstrumentoSubyacente", operator:"eq", value: filtros.instrumentoSubyacente.IdInstrumento})
				
			if (filtros.BloqueoLiquidacion)
				filters.push({ field: "BloqueoLiquidacion", operator: "eq", value: 'S'});
			else
				filters.push({ field: "BloqueoLiquidacion", operator: "eq", value: 'N'});
			
			if (filtros.IdBolsa)
				filters.push({ field: "IdBolsa", operator: "eq", value: filtros.IdBolsa });
			if (filtros.IdClasificacionTipoInstrumento==='IF' && (filtros.EsFungible === true))
		        filters.push({ field: "EsFungible", operator: "eq", value: 'S'});
			if (filtros.fechaDesde)
		        filters.push({ field: "FechaLiquidacion", operator: "gte", value: util.dateSPdate(filtros.fechaDesde)});
			if (filtros.fechaHasta)
				filters.push({ field: "FechaLiquidacion", operator: "lte", value: util.dateSPdate(filtros.fechaHasta)});

			filters.push({ field: "IdEstadoTransaccion", operator: "neq", value: 'ANU'});
			
			return filters;
		}

	
		$scope.datosForm.buscarTransacciones = function() {
			$scope.datosForm.kNgModel = undefined;
			$scope.datosForm.apiGridTransacciones.dataSource.filter(validarFiltros($scope.datosBusqueda));
		};
	
	
		$scope.datosForm.limpiarFiltros = function()
		{
			$scope.datosBusqueda.setInstrumento = "-1";
			$scope.datosBusqueda.AccionOrden = 0;
			$scope.datosBusqueda.IdTipoInstrumento = 0;
			$scope.datosBusqueda.IdCondicionDeLiquidacion = 0;
			$scope.datosBusqueda.IdBolsa = 0;
			$scope.datosBusqueda.BloqueoLiquidacion = false;
			$scope.datosBusqueda.IdClasificacionTipoInstrumento = 0;
			$scope.datosBusqueda.fechaDesde = util.dateInicioDia($scope.SesionFechaSistema);
			$scope.datosBusqueda.fechaHasta = util.dateTerminoDia($scope.SesionFechaSistema);
		};

		
		$scope.datosForm.modificarTransaccion = function(transaccionTemp)
		{
		
			if (transaccionTemp && transaccionTemp.IdTransaccion)
			{
				datosEnvio = {IdTransaccion : transaccionTemp.IdTransaccion, OrigenLlamadaModificar: "TES.GestionContrapartes"}
				var id = bcsComunes.ventanaAbrir("ModificarTransaccion", datosEnvio ,false,false,$scope.idVentana);
				
				bcsComunes.ventanaPromesa(id).then(function() {
					$scope.datosForm.buscarTransacciones()
				});
			}
		};


		//PERU: Abre ventana de interfaz de carga de archivo IGK(Interes de ganancioa de Capital)
		$scope.datosForm.cargarArchivoIGK = function()
		{
			var id = bcsComunes.ventanaAbrir("CargaArchivosTransaccionesOrdenes", {} ,false,false,$scope.idVentana);
	
			bcsComunes.ventanaPromesa(id).then(function() {
				$scope.datosForm.buscarTransacciones()
			});
		};
		
		
		
		$scope.datosForm.abrirDetalle = function (transaccionTemp) {
			if (!transaccionTemp) return;
			bcsComunes.ventanaAbrir("DetalleTransaccion", {IdTransaccion : transaccionTemp[0].IdTransaccion} ,false,false);
		};
		
		
		$scope.datosForm.seleccionarTodo = function() {	
			$scope.datosForm.apiGridTransacciones.select($scope.datosForm.apiGridTransacciones.tbody.find(">tr"));		
		};
		
	  	
	  	$scope.datosForm.limpiarFiltros();
  	

});
