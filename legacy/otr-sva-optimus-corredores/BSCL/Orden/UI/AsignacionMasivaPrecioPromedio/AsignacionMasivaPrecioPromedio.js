angular.module('ModuloOrdenes').controller('AsignacionMasivaPrecioPromedioCtrl',function ($scope,$rootScope,$q,bcsComunes,bcsServicioOrden,
$translate,bcsServicioPlataforma, $translate, bcsAlerta, OPCIONES_BCSALERTA,$timeout) {
	
	//TIENEN QUE LLEGAR TRANSACCIONES DE UN LIBRO, UNA CLASIFICACION, Y UNA MARCA (O MARCA VACIA)
	let defer = $q.defer();
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	let parametros = bcsComunes.ventanaParams($scope.idVentana);
	$scope.transacciones = parametros.transacciones;
	$scope.IdBolsa = parametros.bolsa;
	$scope.IdBoveda = parametros.boveda;
	$scope.IdModeloCustodia = parametros.modeloCustodia;
	$scope.IdClasificacionTipoInstrumento = parametros.mercado;
	$scope.formData = {};
	$scope.cliente = {};
	
	$scope.asignaciones = {};
	
	$scope.datosForm = {};
	$scope.datosBusqueda = {};
	$scope.transaccionesAgrupadas = [];
	
	let fechaMaxima = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.datosBusqueda.fechaDesde = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.datosBusqueda.fechaHasta = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.datosBusqueda.horaDesde = new Date('1/1/1990 00:00:00');
	$scope.datosBusqueda.horaHasta = new Date('1/1/1990 23:59:00');
	
	$scope.datosForm.habilitaBotonBusqueda = false;
	
	$scope.datosForm.oConfiguracionFechaDesde = { max: fechaMaxima };
	
	$scope.datosForm.oConfiguracionFechaHasta = { max: fechaMaxima };
	
	$scope.datosForm.oConfiguracionHora = { min: "00:00",max: "23:59" };
	
	$scope.datosForm.filtrosClasificacionTipoInstrumento = {
		IdClasificacionTipoInstrumento : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "RV"}
	};
	
	$scope.datosForm.filtrosInstrumentos = {
		IdClasificacionTipoInstrumento : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": $scope.datosBusqueda.IdClasificacionTipoInstrumento},
		FechaSistemaCreacionDesde: {"field": "FechaSistemaCreacion", "operator": "eq","value": util.dateSPdate($scope.datosBusqueda.fechaDesde)}
	};
	
	if (parametros?.transaccionesDesasignadas?.length) {

		let listaTransacciones = [
			...new Set(
				parametros.transaccionesDesasignadas.map(t => t.FolioTransaccion)
			)
		];

		$scope.datosForm.filtrosInstrumentos = {
			complejo: true,
			logic: "or",
			filters: listaTransacciones.map(trans => ({
				field: "FolioTransaccion",
				operator: "eq",
				value: trans
			}))
		};
	}

	$scope.datosForm.filtrosLibroOrdenes = {
		EstadoLibroOrdenes 	: {field:"EstadoLibroOrdenes",operator:"eq",value:"ABT"},
		IdEmpresa: {field:"IdEmpresa",operator:"eq",value:$scope.SesionIdEmpresa},
		IdLibroOrdenes: {field:"IdLibroOrdenes",operator:"eq",value: ""}

	};
	
	let configGrilla = {
		columns : [
		 	{field: "InstrumentoUI",				title: $translate.instant('UI_INSTRUMENTO'),	width: "15px"},
			{field: "CantidadDisponibleTotal",		title: $translate.instant('UI_CANTIDAD_DISPONIBLE'),	width: "20px", template : " <label> #:  (util.bcs_formato(CantidadDisponibleTotal,'GEN','Cantidad',IdMonedaInstrumento,IdTipoInstrumento,'')).valor #</label>"},
			{field: "MontoDisponibleTotal",			title: $translate.instant('UI_MONTO_TOTAL_DISPONIBLE'),	width: "20px", template : " <label> #:  (util.bcs_formato(MontoDisponibleTotal,'GEN','Monto',IdMonedaInstrumento,IdTipoInstrumento,'')).valor #</label>"},
			{field: "CantidadTransacciones",		title: $translate.instant('UI_CANTIDAD_TRANSACCIONES'),	width: "15px"},
			{field: "IdMonedaInstrumento",			title: $translate.instant('UI_MONEDA'),	width: "30px", hidden : "true"},
		],
		editable: false,
		selectable: "row",
		autoBind: false,
		groupable : false,
  		scrollable: true,
 		height:125,
 		userSettings:false,
		navigatable: true,
 		dataSource : new bcs.DataSource({
 			type : "array",
 			definition : {
 				data : [],
				schema: {
					model: {
	                    fields: {
	                    	InstrumentoUI				: { type: "string"},
	                        CantidadDisponibleTotal		: { type: "number"},
	                        MontoDisponibleTotal		: { type: "number"},
	                        IdInstrumentoUI				: { type: "number"},
	                        IdMonedaInstrumento			: { type: "string"},
	                        IdTipoInstrumento			: { type: "string"},
	                        CantidadTransacciones		: { type: "number"},
                        }
                   }
				},
		            
	 			}
 		})
	};
	
	$scope.formData.optionsGrillaTransacciones = configGrilla;
	
	$scope.armarEntradaBusqueda = function (esDetalle) {
		let datos = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
  			p_IdUsuario : $scope.SesionIdUsuario,
  			p_IdModulo : 'ORD',
  			p_FiltrosBusqueda : {
				IdClasificacionTipoInstrumento: $scope.datosBusqueda.IdClasificacionTipoInstrumento,
				IdBolsa: $scope.datosBusqueda.IdBolsa === 0 ? "" : $scope.datosBusqueda.IdBolsa,
				MarcasNegociacion: $scope.datosBusqueda.marcasNegociacionSeleccionadas ? $scope.datosBusqueda.marcasNegociacionSeleccionadas : [],
				IdInstrumentos: esDetalle === 'N' ? $scope.datosBusqueda.instrumentosSeleccionados : [$scope.formData.registroAgrupadoSeleccionado.IdInstrumentoUI],
				IdCondicionDeLiquidacion: $scope.datosBusqueda.IdCondicionDeLiquidacion,
				TipoOperacion: $scope.datosBusqueda.AccionOrden,
				FechaDesde: util.dateSPdate($scope.datosBusqueda.fechaDesde),
				FechaHasta: util.dateSPdate($scope.datosBusqueda.fechaHasta),
				HoraDesde: $scope.datosBusqueda.horaDesde ? moment(util.fixUtcDate($scope.datosBusqueda.horaDesde)).format('HH:mm:00') : '00:00:00',
				HoraHasta: $scope.datosBusqueda.horaHasta ? moment(util.fixUtcDate($scope.datosBusqueda.horaHasta)).format('HH:mm:59') : '23:59:59',
				PrecioDesde: $scope.datosBusqueda.precioDesde > 0 ? $scope.datosBusqueda.precioDesde : 0,
				PrecioHasta: $scope.datosBusqueda.precioHasta > 0 ? $scope.datosBusqueda.precioHasta : 0,
				EsDetalle: esDetalle,
				IdLibroOrdenes: $scope.datosBusqueda.IdLibroOrdenes ? $scope.datosBusqueda.IdLibroOrdenes : 0,
				IdBoveda: $scope.datosBusqueda.IdBoveda ? $scope.datosBusqueda.IdBoveda : ''
			}
		};
		
		if (parametros?.transaccionesDesasignadas?.length) {
			datos.p_FiltrosBusqueda.FolioTransacciones = parametros.transaccionesDesasignadas.map(trans => trans.FolioTransaccion)
		}

		return datos;
	};
	
	$scope.buscarTransacciones = function (){
		console.log(util.fixUtcDate($scope.datosBusqueda.horaHasta));
		
		let datos = $scope.armarEntradaBusqueda('N');
		
		console.log(datos);
		
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		bcsServicioOrden.ConsultarTransaccionesAsignacionPrecioPromedio(datos).$promise.then(function(respuesta) {
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			if (respuesta.P_OUT_TRANSACCIONES_AGRUPADAS !== undefined && respuesta.P_OUT_TRANSACCIONES_AGRUPADAS.length > 0) {
				bcsComunes.msg("ok", "INFO", "Transacciones encontradas con exito");
				
				$scope.transaccionesAgrupadas = respuesta.P_OUT_TRANSACCIONES_AGRUPADAS;
				
				$scope.formData.optionsGrillaTransacciones.dataSource.data($scope.transaccionesAgrupadas);
			} else {
				$scope.formData.optionsGrillaTransacciones.dataSource.data([]);
				bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(respuesta)
				},$scope.idVentana);
			}
		},function(respuesta)
		{
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant('UI_OPERACION_INGRESO_ERROR'),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.idVentana);
		});
	};
	
	//TODO: funcion boton ver detalle
	$scope.detalleTransacciones = function () {
		console.log($scope.formData.registroAgrupadoSeleccionado);
		
		let datos = $scope.armarEntradaBusqueda('S');
		
		console.log(datos);
		
		bcsServicioOrden.ConsultarTransaccionesAsignacionPrecioPromedio(datos).$promise.then(function(respuesta) {
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			if (respuesta.P_OUT_TRANSACCIONES_DETALLE !== undefined && respuesta.P_OUT_TRANSACCIONES_DETALLE.length > 0) {
				bcsComunes.msg("ok", "INFO", "Transacciones encontradas con exito");
				
				$scope.transaccionesDetalle = respuesta.P_OUT_TRANSACCIONES_DETALLE;
				console.log($scope.transaccionesDetalle);
				
				$scope.abrirVentanaDetalle($scope.transaccionesDetalle);
				//$scope.formData.optionsGrillaTransacciones.dataSource.data($scope.transaccionesAgrupadas);
			} else {
				//$scope.formData.optionsGrillaTransacciones.dataSource.data([]);
				bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(respuesta)
				},$scope.idVentana);
			}
		},function(respuesta)
		{
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant('UI_OPERACION_INGRESO_ERROR'),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.idVentana);
		});
	};
	
	$scope.abrirVentanaDetalle = function(transaccionesDetalle) {
		let id1 = bcsComunes.ventanaAbrir("AsignacionMasivaPrecioPromedioDetalle",
			{ TransaccionesDetalle: transaccionesDetalle },
				false,false,$scope.idVentana); 
	        let promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){ 
	    	});
	};
	
	//TODO: funcion botón asignacion por grupo
	$scope.asignarPorGrupo = function () {
		
		let idsInstrumentos = [];
		
		$.each($scope.transaccionesAgrupadas, function(index, value){
			idsInstrumentos.push(value.IdInstrumentoUI);
		});
		
		let datos = $scope.armarEntradaBusqueda('S');
		
		console.log(datos);
		
		bcsServicioOrden.ConsultarTransaccionesAsignacionPrecioPromedio(datos).$promise.then(function(respuesta) {
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			if (respuesta.P_OUT_TRANSACCIONES_DETALLE !== undefined && respuesta.P_OUT_TRANSACCIONES_DETALLE.length > 0) {
				bcsComunes.msg("ok", "INFO", "Transacciones encontradas con exito");
				
				$scope.transaccionesDetalle = respuesta.P_OUT_TRANSACCIONES_DETALLE;
				console.log($scope.transaccionesDetalle);
				
				$.each($scope.transaccionesDetalle, function(index, value){
					value.FechaOperacionTransaccion = new Date(value.FechaOperacionTransaccion);
				});
				
				$scope.abrirVentanaAsignarGrupo($scope.transaccionesDetalle);
				//$scope.formData.optionsGrillaTransacciones.dataSource.data($scope.transaccionesAgrupadas);
			} else {
				//$scope.formData.optionsGrillaTransacciones.dataSource.data([]);
				bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(respuesta)
				},$scope.idVentana);
			}
		},function(respuesta)
		{
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant('UI_OPERACION_INGRESO_ERROR'),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.idVentana);
		});
	};
	
	$scope.abrirVentanaAsignarGrupo = function(transacciones) {
		let id1 = bcsComunes.ventanaAbrir("AsignacionGrupo",
			{ transacciones: transacciones, esAsignacionGrupoPrecioPromedio: true, bolsa: transacciones[0].IdBolsa, boveda: transacciones[0].IdBoveda, modeloCustodia: transacciones[0].IdModeloCustodia, mercado: transacciones[0].IdClasificacionTipoInstrumento },
				false,false,$scope.idVentana); 
	        let promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){ 
	    	});
	};
	
	$scope.$watchGroup([
	    'datosBusqueda.IdClasificacionTipoInstrumento',
	    'datosBusqueda.IdBolsa',
	    'datosBusqueda.AccionOrden',
	    'datosBusqueda.instrumentosSeleccionados.length'
	], function() {
	
	    $scope.datosForm.habilitaBotonBusqueda =
	        !!$scope.datosBusqueda.IdClasificacionTipoInstrumento &&
	        !!$scope.datosBusqueda.IdBolsa &&
	        !!$scope.datosBusqueda.AccionOrden &&
	        $scope.datosBusqueda.instrumentosSeleccionados.length > 0;
	});

	$scope.$watch('datosBusqueda.fechaHasta', function (newVal, oldVal) {
	    if (newVal === undefined || newVal === null) return;
	    if (!$scope.datosBusqueda.fechaDesde) return;
	    if (newVal < $scope.datosBusqueda.fechaDesde) {
	        bcsComunes.msg("error", "Error", "La Fecha Hasta no puede ser menor a la Fecha Desde");
	        $timeout(function () { $scope.datosBusqueda.fechaHasta = oldVal; }, 0);
	    }else if(newVal != $scope.datosBusqueda.fechaDesde){
			$scope.datosForm.filtrosInstrumentos.FechaSistemaCreacionDesde = {"field": "FechaSistemaCreacion", "operator": "gte","value": util.dateSPdate($scope.datosBusqueda.fechaDesde)};
			$scope.datosForm.filtrosInstrumentos.FechaSistemaCreacionHasta = {"field": "FechaSistemaCreacion", "operator": "lte","value": util.dateSPdate($scope.datosBusqueda.fechaHasta)};
		}
	});
	
	$scope.$watch('datosBusqueda.fechaDesde', function (newVal, oldVal) {
	    if (newVal === undefined || newVal === null) {
	        bcsComunes.msg("error", "Error", "Debe establecer una Fecha Desde");
	        $timeout(function () { $scope.datosBusqueda.fechaDesde = oldVal; }, 0);
	    } else {
			$scope.datosBusqueda.instrumentosSeleccionados = [];
			if(newVal > $scope.datosBusqueda.fechaHasta){
				$scope.datosBusqueda.fechaHasta = newVal;
				$scope.datosForm.filtrosInstrumentos.FechaSistemaCreacionDesde = {"field": "FechaSistemaCreacion", "operator": "eq","value": util.dateSPdate($scope.datosBusqueda.fechaDesde)};
				$scope.datosForm.filtrosInstrumentos.FechaSistemaCreacionHasta = {};
			}else if(newVal != $scope.datosBusqueda.fechaHasta){
				$scope.datosForm.filtrosInstrumentos.FechaSistemaCreacionDesde = {"field": "FechaSistemaCreacion", "operator": "gte","value": util.dateSPdate($scope.datosBusqueda.fechaDesde)};
				$scope.datosForm.filtrosInstrumentos.FechaSistemaCreacionHasta = {"field": "FechaSistemaCreacion", "operator": "lte","value": util.dateSPdate($scope.datosBusqueda.fechaHasta)};
			} else {
				$scope.datosForm.filtrosInstrumentos.FechaSistemaCreacionDesde = {"field": "FechaSistemaCreacion", "operator": "gte","value": util.dateSPdate($scope.datosBusqueda.fechaDesde)};				
			}
			
		}
	});
	
});
