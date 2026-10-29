angular.module('ModuloOrdenes').controller('ConsultarTransaccionesCtrl',function($scope,bcsConfiguracionOrdenes,bcsConsultasOrdenes,bcsComunes,bcsConfiguracionOrdenesV2,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioOrdenV2,bcsServicioOrden,$translate,bcsServicioPlataforma){
	var controllerName = 'ConsultarTransaccionesCtrl';
	
	$scope.datosForm = {};
	$scope.datosBusqueda = {};
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	$scope.sumaCantidad = 0;
	$scope.sumaMonto = 0;
	
	$scope.datosForm.filtrosTipoInstrumento = bcsConfiguracionOrdenes.UI.getFiltrosCombo(controllerName,'combobox-tipo-instrumento');
	$scope.datosForm.filtrosOperacion = bcsConfiguracionOrdenes.UI.getFiltrosCombo(controllerName,'combobox-operacion');
	$scope.datosForm.filtrosCondicionDeLiquidacion = bcsConfiguracionOrdenes.UI.getFiltrosCombo(controllerName,'combobox-condicion-liquidacion');
	$scope.datosForm.filtrosInstrumentos = {
		IdClasificacionTipoInstrumento : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": ""},
		IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value"   : "" }
	};
	
	$scope.datosForm.defaultOptionsNew = {
			autoBind: false,
      		allowCopy: true,
      		excel : {
				allPages: true,
			},
			rowCellColor : function(row) {
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
					case "ING":
						if(row["Reservado"].toUpperCase() === 'S')
						return "green";
						break;	
				}
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
      		 
			dataSource: new bcs.DataSource(bcsConfiguracionOrdenesV2.getDataSourceConsultarTransacciones()),
  			columns: [
  				bcsConfiguracionOrdenesV2.getConfiguracionColumna("FolioTransaccion")
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
				
				,bcsConfiguracionOrdenesV2.getConfiguracionColumna("PrecioRaiz",{ groupable: true})
				,bcsConfiguracionOrdenesV2.getConfiguracionColumna("MontoRaiz",{ groupable: true})
				,bcsConfiguracionOrdenesV2.getConfiguracionColumna("MonedaRaiz",{ groupable: true})
				,bcsConfiguracionOrdenesV2.getConfiguracionColumna("InstrumentoNegociacionTran",{ groupable: true})				
				,bcsConfiguracionOrdenesV2.getConfiguracionColumna("MotivoReserva",{ groupable: true})
				,bcsConfiguracionOrdenesV2.getConfiguracionColumna("Reservado",{ groupable: true})	
			],
		};
	
	bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise('CUS.UsaFechaLiquidacionEnVencimiento', $scope.SesionIdEmpresa, 'PLATAFORMA')
	.then(function (parametroUsaFechaLiquidacion) {
		$scope.datosForm.UsaFechaLiquidacionEnVencimiento = parametroUsaFechaLiquidacion.Valor === 'S';
	});

	$scope.datosForm.buscarTransacciones = function() {
		$scope.datosForm.kNgModel = undefined;
		$scope.datosForm.apiGridTransacciones.dataSource.filter(bcsConfiguracionOrdenesV2.validarFiltrosV2($scope.datosBusqueda,'TRANSACCION'));
		$scope.sumaCantidad = 0;
		$scope.sumaMonto = 0;
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
		$scope.datosBusqueda.TransaccionesConSaldo = true;
		$scope.datosBusqueda.TransaccionesReservadas = false;
		$scope.datosBusqueda.IdClasificacionTipoInstrumento = 0;
		$scope.datosBusqueda.codigoOperador = "";
		$scope.datosBusqueda.fechaDesde = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.datosBusqueda.fechaHasta = util.dateTerminoDia($scope.SesionFechaSistema);
		
	};

	$scope.datosForm.asignacionRapida = function(transacciones) {
		
		if (!transacciones || transacciones.length === 0)
			return;

		var accionOrden = _.groupBy(transacciones, function(num){ return num.AccionOrden; });
		if (Object.keys(accionOrden).length > 1) {
			bcsComunes.msg("error","UI_ERROR","UI_TRANSACCIONES_DIFERENTE_ACCION");
			return;
		}

		var fechaOperacion = _.groupBy(transacciones, function(num){ return util.dateSPdate(num.FechaOperacionTransaccion); });
		if (Object.keys(fechaOperacion).length > 1) {
			bcsComunes.msg("error","UI_ERROR","UI_TRANSACCIONES_DIFERENTE_FECHA");
			return;
		}
		var libroOrdenes = _.groupBy(transacciones, function(num){ return num.IdLibroOrdenes; });
		
		var ventaCorta = _.groupBy(transacciones, function(num){ return num.EsVentaCorta; });
		if (Object.keys(ventaCorta).length > 1) {
			bcsComunes.msg("error","UI_ERROR","UI_TRANSACCIONES_DIFERENTE_ATRIBUTO_VENTA_CORTA");
			return;
		}

		var idMonedaInstrumento = _.groupBy(transacciones, function(num) { return num.IdMonedaInstrumento; });
		if (Object.keys(idMonedaInstrumento).length > 1) {
			bcsComunes.msg("error","UI_ERROR","UI_TRANSACCIONES_CON_MONEDA_INSTRUMENTO_DISTINTAS");
			return;
		}

		if ($scope.datosForm.UsaFechaLiquidacionEnVencimiento) {
			var condLiq = _.groupBy(transacciones, function(num){ return num.IdCondicionDeLiquidacion; });
			if (Object.keys(condLiq).length > 1) {
				bcsComunes.msg("error","UI_ERROR","UI_TRANSACCIONES_DIFERENTE_CONDICION_LIQUIDACION");
				return true;
			}
		}

		for (i=0;i<transacciones.length;i++) {
			if(transacciones[i].CantidadDisponibleTransaccion === 0) {
				bcsComunes.msg("error","error","UI_TRANSACCION_SIN_SALDO_DISPONIBLE");
				return;
			}
		}
		
		
		var idPromesa = bcsComunes.ventanaAbrir("AsignacionCliente", { transacciones : transacciones } ,false,false,$scope.idVentana);
		bcsComunes.ventanaPromesa(idPromesa).then(function(result){
			$scope.datosForm.apiGridTransacciones.dataSource.read();
			$scope.datosForm.kNgModel = undefined;
		})
	}
	
	$scope.datosForm.asignacionGrupo = function(transacciones) {
		if (!transacciones || transacciones.length === 0)
			return;
		
		//NO PERMITE COMBINAR MARCAS
		/*var marcas = _.groupBy(transacciones, function(tran){ 
			if (tran.MarcaNegociacion.trim() === "") return "$VACIO";
			return tran.MarcaNegociacion; 
		});
		if (marcas.$VACIO)
			delete marcas.$VACIO;
		if (Object.keys(marcas).length > 1) {
			bcsComunes.msg("error","UI_ERROR","UI_TRANSACCIONES_DIFERENTE_MARCA");
			return;
		}*/
		//NO PERMITE COMBINAR CLASIFICACION
		var clasificacion = _.groupBy(transacciones, function(tran){ return tran.IdClasificacionTipoInstrumento; });
		if (Object.keys(clasificacion).length > 1) {
			bcsComunes.msg("error","UI_ERROR","UI_TRANSACCIONES_DIFERENTE_CLASIFICACION");
			return;
		}
		
		//NO PERMITE COMBINAR VENTA CORTA CON VENTA NORMAL
		var ventacorta = _.groupBy(transacciones, function(tran){ return tran.EsVentaCorta; });
		if (Object.keys(ventacorta).length > 1) {
			bcsComunes.msg("error","UI_ERROR","UI_TRANSACCIONES_DIFERENTE_ATRIBUTO_VENTA_CORTA");
			return;
		}
		
		var fechaOperacion = _.groupBy(transacciones, function(tran){ return util.dateSPdate(tran.FechaSistemaCreacion); });
		if (Object.keys(fechaOperacion).length > 1) {
			bcsComunes.msg("error","UI_ERROR","UI_TRANSACCIONES_DIFERENTE_FECHA");
			return true;
		}
		
		//NO PERMITE COMBINAR LIBROS
		var libros = _.groupBy(transacciones, function(tran){ return tran.IdLibroOrdenes; });
		if (Object.keys(libros).length > 1) {
			bcsComunes.msg("error","UI_ERROR","UI_TRANSACCIONES_DIFERENTE_LIBRO_ORDENES");
			return;
		}
		
		//NO PERMITE COMBINAR BOLSAS
		var bolsas = _.groupBy(transacciones, function(tran){ return tran.IdBolsa; });
		if (Object.keys(bolsas).length > 1) {
			bcsComunes.msg("error","UI_ERROR","UI_TRANSACCIONES_DIFERENTE_BOLSA");
			return;
		}
		
		//NO PERMITE COMBINAR Bovedas
		var bovedas = _.groupBy(transacciones, function(tran){ return tran.IdBoveda; });
		if (Object.keys(bovedas).length > 1) {
			bcsComunes.msg("error","UI_ERROR","UI_TRANSACCIONES_DIFERENTE_BOVEDA");
			return;
		}

		if ($scope.datosForm.UsaFechaLiquidacionEnVencimiento) {
			var condLiq = _.groupBy(transacciones, function(num){ return num.IdCondicionDeLiquidacion; });
			if (Object.keys(condLiq).length > 1) {
				bcsComunes.msg("error","UI_ERROR","UI_TRANSACCIONES_DIFERENTE_CONDICION_LIQUIDACION");
				return true;
			}
		}
		
		var idPromesa = bcsComunes.ventanaAbrir("AsignacionGrupo", {transacciones : transacciones, bolsa: transacciones[0].IdBolsa, boveda: transacciones[0].IdBoveda, modeloCustodia: transacciones[0].IdModeloCustodia, mercado: transacciones[0].IdClasificacionTipoInstrumento } ,false,false,$scope.idVentana);
		bcsComunes.ventanaPromesa(idPromesa).then(function(result){
			$scope.datosForm.apiGridTransacciones.dataSource.read();
			$scope.datosForm.kNgModel = undefined;
		})
	}
	
	$scope.datosForm.asignar = function(transacciones) {
		
		if (!transacciones || transacciones.length === 0)
			return;
		
		if (bcsServicioOrdenV2.transaccionesEstanCombinadas(transacciones)) {
			return;
		}

		for (i=0;i<transacciones.length;i++) {
			if(transacciones[i].CantidadDisponibleTransaccion === 0) {
				bcsComunes.msg("error","error","UI_TRANSACCION_SIN_SALDO_DISPONIBLE");
				return;
			}
		}
		
		var idPromesa = bcsComunes.ventanaAbrir("InterfazAsignacion", { transacciones : transacciones } ,false,false,$scope.idVentana);
		bcsComunes.ventanaPromesa(idPromesa).then(function(result){
			$scope.datosForm.apiGridTransacciones.dataSource.read();
			$scope.datosForm.kNgModel = undefined;
		})
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
			p_Transacciones	:transacciones,
		};
		
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		bcsServicioOrden.ReservarLiberarTransacciones(datos).$promise.then(function(result){
			
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			
			var id = bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(result)
				},$scope.idVentana);
			bcsComunes.ventanaPromesa(id).then(function() {
				$scope.datosForm.buscarTransacciones();
			});
					
		},function(result){
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			bcsComunes.ventanaMsg({
				conflictos:util.parseServerResponse(result)
			},$scope.idVentana);
		});
	};
	
	$scope.datosForm.liberar = function(transaccionTemp) {
		if (transaccionTemp && transaccionTemp.length>0) {
			ejecutarReservarLiberar('L',transaccionTemp,'');
		}
		else {
			bcsComunes.msg("warning","UI_ADVERTENCIA", $translate.instant("UI_DEBE_SELECCIONAR_TRANSACCIONES"));
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

	$scope.datosForm.liberacionSeleccion = function () {
		$scope.datosForm.liberar($scope.datosForm.kNgModel);
	};

	$scope.datosForm.liberacionMasiva = function () {
		var id = bcsComunes.ventanaAbrir("LiberacionMasiva", null, false, false, $scope.idVentana);
		bcsComunes.ventanaPromesa(id).then(function () {
			$scope.datosForm.buscarTransacciones();
		});
	};

	$scope.datosForm.opcionesLiberacion = [
		{ titulo : $translate.instant("UI_LIBERACION"),        funcion : $scope.datosForm.liberacionSeleccion },
		{ titulo : $translate.instant("UI_LIBERACION_MASIVA"), funcion : $scope.datosForm.liberacionMasiva }
	];

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
			  		datos.p_transacciones.push({IdTransaccion : tran.IdTransaccion, Motivo : '', Observaciones : '', FechaAnulacion : null});
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
		    		bcsComunes.ventanaMsg({
						conflictos: util.parseServerResponse(result)
					},$scope.idVentana)
		    		bcsComunes.ventanaOverlay($scope.idVentana,false);
		    	});				
			}
		});
		
	};
	$scope.datosForm.abrirDetalle = function (transaccionTemp) {
		if (!transaccionTemp) return;
		bcsComunes.ventanaAbrir("DetalleTransaccion", {IdTransaccion : transaccionTemp[0].IdTransaccion} ,false,false);
	};
	
	$scope.datosForm.verMapaRelaciones = function (transaccionTemp) {
		bcsComunes.ventanaAbrir("MapaRelaciones", {TipoEntidad : "TRANSACCION",IdEntidad : transaccionTemp[0].IdTransaccion},false,false );
	};
	
	
	$scope.datosForm.seleccionarTodo = function() {	
		$scope.datosForm.apiGridTransacciones.select($scope.datosForm.apiGridTransacciones.tbody.find(">tr"));		
	};
	//GO
	$scope.$watch('datosForm.kNgModel',function(newval,oldval) {
		if (!newval) return;
		$scope.sumaCantidad = 0;
		$scope.sumaMonto = 0;
		var moneda = "USD";
		$.each(newval,function(key,value){
			if(isNaN(Number(value.CantidadTransaccion)) || value.CantidadTransaccion === ""){
				bcsComunes.msg("warning", "UI_ADVERTENCIAS", "UI_NO_POSIBLE_SUMAR_CANTIDAD");
			}else{
				$scope.sumaCantidad = $scope.sumaCantidad + value.CantidadTransaccion;
			}
			if(isNaN(Number(value.MontoTransaccion)) || value.MontoTransaccion === ""){
				bcsComunes.msg("warning", "UI_ADVERTENCIAS", "UI_NO_POSIBLE_SUMAR_MONTO");
			}else{
				$scope.sumaMonto = $scope.sumaMonto + value.MontoTransaccion;
			}
		})
		if($scope.sumaCantidad != 0){
			$scope.sumaCantidad = util.bcs_formato($scope.sumaCantidad,'GEN','CANTIDAD',moneda,'','').valor;
		}
		if($scope.sumaMonto != 0 && util.bcs_formato($scope.sumaMonto,'GEN','Monto',moneda,'','').valor != 'Error'){
			$scope.sumaMonto = util.bcs_formato($scope.sumaMonto,'GEN','Monto',moneda,'','').valor;
		}else{
			$scope.sumaMonto = 0;
		}
	});
	
  	
  	$scope.datosForm.limpiarFiltros();
var array = [
		{ Estado: 'Ingresado', Color: 'black' },
	    { Estado: 'Anulado', Color: 'red' },
		{ Estado: 'Reservado', Color: 'green' }
	];
	
	bcsComunes.tootipGlosa('toolConsultarTransacciones', array)
  	

});
