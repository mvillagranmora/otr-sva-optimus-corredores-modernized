angular.module('ModuloOrdenes').controller('ConsultarTransaccionesSimpleCtrl',function($scope,bcsConfiguracionOrdenes,bcsConsultasOrdenes,bcsComunes,bcsConfiguracionOrdenesV2,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioOrdenV2,bcsServicioOrden,$translate){
	var controllerName = 'ConsultarTransaccionesCtrl';
	
	$scope.datosForm = {};
	$scope.datosBusqueda = {};
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	
	$scope.datosForm.filtrosTipoInstrumento = bcsConfiguracionOrdenes.UI.getFiltrosCombo(controllerName,'combobox-tipo-instrumento');
	$scope.datosForm.filtrosOperacion = bcsConfiguracionOrdenes.UI.getFiltrosCombo(controllerName,'combobox-operacion');
	$scope.datosForm.filtrosCondicionDeLiquidacion = bcsConfiguracionOrdenes.UI.getFiltrosCombo(controllerName,'combobox-condicion-liquidacion');
	
	$scope.hola = function(hofasfa) {
		console.log('asdfasd');
	}
	
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
      		
			dataSource: new kendo.data.DataSource({
	  			type : "odata",
	  			
					serverFiltering : true, 
					serverPaging : true, 
					serverSorting : true,
					pageSize: 200,
					sort: { field: "IdTransaccion", dir: "desc" },
					transport: {
						read: {
							url:bcsConsultasOrdenes('ConsultarTransaccionesUI'),
							dataType: "json"
						}
					},
					schema : bcsConfiguracionOrdenesV2.getSchemaTransaccion()
				
	  		}),
  			columns: [
  			        bcsConfiguracionOrdenesV2.getConfiguracionColumna("FolioTransaccion")
		    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaOperacionTransaccion",{ groupable: true })
		    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdClasificacionTipoInstrumento",{ groupable: true})
		    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreTipoOperacion",{ groupable: true})
		    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("InstrumentoUI",{ groupable: true})
		    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("CantidadTransaccion",{ groupable: true})
		    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("PrecioTransaccion",{ groupable: true})
		    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("Tasa",{ groupable: true})
		    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("CantidadDisponibleTransaccion",{ groupable: true})
		    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoCondicionDeLiquidacion",{ groupable: true})
		    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdEmisor",{ groupable: true})
		    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaVencimiento",{ groupable: true})
		    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("IndicadorOrdenDirecta",{ groupable: true})
		    		
		    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdTipoInstrumento",{ groupable: true})
		    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("NemotecnicoSVS",{ groupable: true})
		    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("EsFungibleUI",{ groupable: true})
		    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("ValorRescateDisponibleTransaccion",{ groupable: true})
					,bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdBolsa",{ groupable: true})
					,bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaLiquidacion",{ groupable: true})
					,bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdTipoReajuste",{ groupable: true})
					,bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdMonedaLiquidacion",{ groupable: true})
					,bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdMonedaInstrumento",{ groupable: true})
					,bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoOperadorCorredor",{ groupable: true})
					,bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoOperadorContraparte",{ groupable: true})
		    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaEmision",{ groupable: true})
		    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("EsCuentaPropia",{ groupable: true})
		    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("EsAPlazoUI",{ groupable: true})
		    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("EsVentaCortaUI",{ groupable: true})
		    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdEmpleadoReserva",{ groupable: true})
		    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaModificacionOperacion",{ groupable: true})
		    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("CantidadAsignadaTransaccion",{ groupable: true})
					,bcsConfiguracionOrdenesV2.getConfiguracionColumna("MontoTransaccion",{ groupable: true})
		    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreLibroOrdenes",{ groupable: true})
		    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreTipoInstrumento",{ groupable: true})
		    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("ValorRescateTransaccion",{ groupable: true})
		    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("EstadoTransaccion",{ groupable: true})
		    		,bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreCorredorContraparte",{ groupable: true})
			],
			
		};
	
	$scope.datosForm.buscarTransacciones = function() {
		$scope.datosForm.apiGridTransacciones.dataSource.filter(bcsConfiguracionOrdenesV2.validarFiltrosV2($scope.datosBusqueda,'TRANSACCION'));
	};
	
	$scope.datosForm.limpiarFiltros = function()
	{
		$scope.datosBusqueda.setInstrumento = "-1";
		$scope.datosBusqueda.EsVentaCorta = false;
		$scope.datosBusqueda.AccionOrden = 0;
		$scope.datosBusqueda.IdTipoInstrumento = 0;
		$scope.datosBusqueda.IdCondicionDeLiquidacion = 0;
		$scope.datosBusqueda.IdLibroOrdenes = 0;
		$scope.datosBusqueda.IdBolsa = 0;
		$scope.datosBusqueda.IdClasificacionTipoInstrumento = 0;
		$scope.datosBusqueda.fechaDesde = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.datosBusqueda.fechaHasta = util.dateTerminoDia($scope.SesionFechaSistema);
		
	};

	$scope.datosForm.asignar = function(transacciones) {
		
		if (bcsServicioOrdenV2.transaccionesEstanCombinadas(transacciones)) {
			return;
		}

		bcsComunes.ventanaAbrir("InterfazAsignacion", { transacciones : transacciones } ,false,false);
	};
	$scope.datosForm.reAsignar = function(transacciones,tipo) {
		//VALIDAR SI SON TRANSACCIONES DEL DIA O DE OTRO
		var parametros = {Tipo:tipo,fechaSistema:$scope.SesionFechaSistema};
		if (bcsServicioOrdenV2.transaccionesEstanCombinadas(transacciones)) {
			return;
		}

		var datos = [];
		if (!transacciones)
			return;
		for (var i = 0 ; i< transacciones.length ; i++){
			datos.push({"IdMovimiento" : transacciones[i].IdTransaccion});
		};
		bcsComunes.ventanaAbrir("InterfazAsignacion", { Origen : 'TRAN', Tipo : tipo, movimientos : datos} );
	};
	$scope.datosForm.crearTransaccion = function()
	{
		bcsComunes.ventanaAbrir("CrearTransaccion", {} );
	};
	
	$scope.datosForm.modificarTransaccion = function(transaccionTemp)
	{
		if (transaccionTemp[0] && transaccionTemp[0].IdTransaccion) {
			var id = bcsComunes.ventanaAbrir("ModificarTransaccion", {IdTransaccion : transaccionTemp[0].IdTransaccion} ,false,false,$scope.idVentana);
			bcsComunes.ventanaPromesa(id).then(function() {
				$scope.datosForm.buscarTransacciones()
			});
		}
	};
	
	/* BLOQUE RESERVAR LIBERAR */
	var ejecutarReservarLiberar = function(modo,transaccionesIn,motivo) {
		var transacciones = [];
		$.each(transaccionesIn,function(key,value) { 
			transacciones.push({IdTransaccion: value.IdTransaccion});
		});	
		
		var datos = {
			p_IdEmpresa		:$scope.SesionIdEmpresa,
		    p_IdUsuario		:$scope.SesionIdUsuario,
		    p_IdModulo		:'ORD',
		    p_Accion		: modo,
			p_Motivo		: motivo,
			p_transacciones	:transacciones,
		};
		
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		bcsServicioOrden.ReservarLiberarTransacciones(datos).$promise.then(function(result){
			
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			var operaciones = [];
			$.each(result.P_OUT_TRANSACCIONES,function(key,value) {
				operaciones.push({id: value.IdTransaccion , resultado: value.Resultado, mensaje: value.Codigo});
			});
			
			var id = bcsComunes.ventanaAbrir("ResumenOperacionesMasivas", { 
				tituloVentana : $translate.instant("UI_LIBERAR_TRANSACCIONES"), 
				nombreEntidad : $translate.instant("UI_TRANSACCION"), 
				operaciones : operaciones   
			});
			bcsComunes.ventanaPromesa(id).then(function() {
				$scope.datosForm.buscarTransacciones();
			});
					
			
			
		},function(result){
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			console.log("ERROR" + JSON.stringify(result));
		});
	};
	
	$scope.datosForm.liberar = function(transaccionTemp) {
		if (transaccionTemp && transaccionTemp.length>0) {
			ejecutarReservarLiberar('L',transaccionTemp,'');
		}
		else {
			bcsComunes.msg("error",$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_SELECCIONAR_TRANSACCIONES"));
		}
	};
	
	$scope.datosForm.reservar = function(transaccionTemp) {
		if (transaccionTemp && transaccionTemp.length>0) {
			var id = bcsComunes.ventanaAbrir("IngresarMotivoReservarLiberar", {} ,false,false,$scope.idVentana);
			var promesa = bcsComunes.ventanaPromesa(id);
			promesa.then(function(motivo) {
				if (angular.isString(motivo))
					ejecutarReservarLiberar('R',transaccionTemp,motivo);
			});
		}
		else {
			bcsComunes.msg("error",$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_SELECCIONAR_TRANSACCIONES"));
		}
	};
		
	$scope.datosForm.anularTransacciones = function(transaccionTemp)
	{
		
		if (!transaccionTemp || transaccionTemp.length == 0) return;
		var t = bcsAlerta.crear('PREGUNTA',$translate.instant('UI_ADVERTENCIA_ANULACION_TRANSACCIONES'),$scope.idVentana);
		t.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
			
				var datos = {
		    			p_IdEmpresa : $scope.SesionIdEmpresa,
			  			p_IdUsuario : $scope.SesionIdUsuario,
			  			p_IdModulo : 'ORD',
			  			p_transacciones : [],
		    	};
		    	$.each(transaccionTemp,function(key,tran){
			  		datos.p_transacciones.push({IdTransaccion : tran.IdTransaccion});
			  	});
		    	bcsComunes.ventanaOverlay($scope.idVentana,true);
		    	bcsServicioOrden.AnularTransaccionMasiva(datos).$promise.then(function(result){
		    		
		    		var id = bcsComunes.ventanaMsg({
						mensaje: '',
						conflictos: util.parseServerResponse(result)
					},$scope.idVentana)
					
		    		$scope.datosForm.buscarTransacciones();
		    		bcsComunes.ventanaOverlay($scope.idVentana,false);
		    	},function(result){
		    		$scope.datosForm.buscarTransacciones();
		    		bcsComunes.ventanaOverlay($scope.idVentana,false);
		    	});				
			}
		});
		
	};
	$scope.datosForm.abrirDetalle = function (transaccionTemp) {
		if (!transaccionTemp) return;
		bcsComunes.ventanaAbrir("DetalleTransaccion", {IdTransaccion : transaccionTemp[0].IdTransaccion} );
	};
	
	//GO
	
	
  	
  	$scope.datosForm.limpiarFiltros();
  	

});
