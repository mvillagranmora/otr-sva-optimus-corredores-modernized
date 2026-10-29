angular.module('ModuloFacturacion').controller('ModificarMasivoComisionesModalCtrl', function($scope, $translate, bcsComunes, bcsMovimientoFacturarGrid, bcsServiciosFacturacion, $timeout) 
{
	
	let idVentana = bcsComunes.ventanaWinId ($scope);
	let idsMovimientoComprobante = [];
	let cobros = {};
	$scope.data	= {}; 
	$scope.modoConsulta = true;
	$scope.data.oIndSeleccionarTodasOperaciones = true;
	$scope.data.tabs = {
		  TabSeleccion: 	{active:	true}	
		, TabCobros: 		{active:	false}	
	};
	
	$scope.data.oCliente = {
		tipo: "combobox-socio-negocio",
		configuracion: {},
		idActual: false,
		seleccionado: false,
		filtros: {
			IdEmpresa: { field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa },
			EstadoCliente: { field: "EstadoCliente", operator: "eq", value: 'ACT' },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},
		idDefecto : ''
	};
	
	
	$scope.data.oPortafolio={
 		tipo : "combobox-portafolio",
 	   	configuracion :{},
 	   	idActual : false,
 	   	seleccionado : false,
 	   	filtros: {
 		  	      IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  $scope.data.oCliente.idActual },
 	   			  IdEstado  : { "field": "IdEstado","operator": "eq", "value"   :  "ACT"}
 			    },
		idDefecto : ''
    };

	$scope.data.oConfiguracionClasificacionTipoInstrumento={
			tipo : "combobox-clasificacion-tipo-instrumento",
			configuracion : {},
			filtros : { 	
				IdEstado :	{field : "IdEstado", operator : "eq", value : "ACT"}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.data.oConfiguracionOperacion={
			tipo : "combobox-operacion",
			configuracion : {},
			filtros : { 	
				IdEstado :	{field : "IdEstado", operator : "eq", value : "ACT"},
				Comprobante : {"field"   : "Comprobante","operator": "eq","value"   : "S"}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.data.oConfiguracionComboTipoCobro={
			tipo : "combobox-cobro",
			configuracion : {},
			filtros : { 
				logic: "or",	
				V :	{field : "IdCobro", operator : "eq", value : "COVA"},
				F :	{field : "IdCobro", operator : "eq", value : "COFI"}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : 'COVA'
	};
	
	$scope.confBasicaGrillaSeleccion = { 
		dataSource			: bcsMovimientoFacturarGrid.dataSourceMovimientosFacturarComisionVariableYFija,
		columns				: bcsMovimientoFacturarGrid.columnasModificarMovimiento(),
		sortable			: true,
		pageable			: true,
		filterable			: true,
		userSettings        : true,
		gridCreated			: function(g) {
			$scope.data.gridApiModificarMovimiento = g;
		},
		height				: 450,
		selectable 			: "multiple row",
		navigatable         : true,
		allowCopy 			: true
		
	};
	
	$scope.confBasicaGrillaCobros = { 
		dataSource			: bcsMovimientoFacturarGrid.dataSourceGrillaCobrosMovimiento,
		columns				: bcsMovimientoFacturarGrid.columnasGrillaCobrosVariableMovimiento(),
		sortable			: true,
		pageable			: true,
		userSettings        : true,
		save :function(e) {
			$.each(e.model,function(key,value){
				if(e.model.IdTipoCobro == 'COVA') {
					if(e.values.Porcentaje === undefined) {
						e.model.Porcentaje = (e.values.MontoCobro / e.model.MontoOperacion) * 100;
					}
					if(e.values.MontoCobro === undefined){
						e.model.MontoCobro = e.model.MontoOperacion * (e.values.Porcentaje/100);
					}
				}
				cobros[obtenerLlave(obtenerLlave)] = e.model;
				
			})
			$scope.data.gridApiModificarMovimientoCobros.refresh();
		},
		gridCreated			: function(g) {
			$scope.data.gridApiModificarMovimientoCobros = g;
		},
		excel 				: { allPages : true },
		height				: 450,
		selectable 			: "multiple row",
		navigatable         : true,
		allowCopy 			: true
		
	};
	
	$scope.buscar = function(){
		var filters = new Array();
		
		if(!$scope.data.oCliente.seleccionado.IdCliente || !($scope.data.oCliente.seleccionado.IdCliente > 0)){
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_SELECCIONAR_CLIENTE"));
			return;
		}
		
		if(!$scope.data.oConfiguracionComboTipoCobro.idActual){
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_SELECCIONAR_TIPO_COBRO"));
			return;
		}
		
		filters.push({ field: "IdCliente", operator: "eq", value: Number($scope.data.oCliente.seleccionado.IdCliente)});			
		filters.push({ field: "EstadoFacturacion", operator: "eq", value: "ING" });			
			
		if ($scope.data.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento)
	        filters.push( { "field": "IdClasificacionTipoInstrumento","operator": "eq", "value" : $scope.data.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento});			
		
		if ($scope.data.oConfiguracionOperacion.seleccionado.AccionOperacion)
			filters.push( { "field": "AccionTipoOperacion","operator": "eq", "value" : $scope.data.oConfiguracionOperacion.seleccionado.AccionOperacion});

		if (($scope.data.oPortafolio.seleccionado.IdPortafolio))	
			filters.push({ field: "IdPortafolio", operator: "eq", value: Number($scope.data.oPortafolio.seleccionado.IdPortafolio)});
		
		if (($scope.data.oConfiguracionComboTipoCobro.seleccionado.IdCobro))	
			filters.push({ field: "IdCobro", operator: "eq", value: $scope.data.oConfiguracionComboTipoCobro.seleccionado.IdCobro});

		$scope.confBasicaGrillaSeleccion.dataSource.filter(filters);
		
		$scope.confBasicaGrillaSeleccion.dataSource.data();
		
		setTimeout(function(){
			fnSeleccionarDeseleccionarTodo();
		},500);
	};
	
	$scope.limpiarFiltros = function()
	{	
		$scope.data.oCliente.idActual = null;
		$scope.data.oPortafolio.idActual = null;
		$scope.data.oConfiguracionClasificacionTipoInstrumento.idActual = 0;
		$scope.data.oConfiguracionOperacion.idActual = 0;
		$scope.data.oConfiguracionComboTipoCobro.idActual = 'COVA';
		
				
		$scope.confBasicaGrillaSeleccion.dataSource.removeAll();	
	};
	
	$scope.$watch('data.oCliente.idActual',function(newval,oldval){
		$scope.confBasicaGrillaSeleccion.dataSource.removeAll();
	});
	
	$scope.$watch('data.oConfiguracionComboTipoCobro.idActual',function(newval,oldval){
		$scope.confBasicaGrillaSeleccion.dataSource.removeAll();
	});
	
	$scope.$watch('data.oIndSeleccionarTodasOperaciones',function(newval,oldval){
		fnSeleccionarDeseleccionarTodo();
	});
	
	$scope.validarSeleccion = function (movimientosSeleccionados){
		
		if(!$scope.data.oCliente.idActual){
			mostrarErrorSeleccion("UI_DEBE_SELECCIONAR_CLIENTE");
			return;
		}
		
		if(!$scope.data.oConfiguracionComboTipoCobro.idActual){
			mostrarErrorSeleccion("UI_DEBE_SELECCIONAR_TIPO_COBRO");
			return;
		}
		
		if((!movimientosSeleccionados || movimientosSeleccionados.length === 0) && (!$scope.data.oIndSeleccionarTodasOperaciones || $scope.confBasicaGrillaSeleccion.dataSource.data().length === 0)){
			mostrarErrorSeleccion("UI_SELECCIONAR_MOVIMENTOS_PARA_CONTINUAR");
			return;
		}
		
		$scope.data.modoConsulta = false;
		idsMovimientoComprobante = [];
		
		if($scope.data.oConfiguracionComboTipoCobro.idActual === 'COVA'){
			$scope.data.gridApiModificarMovimientoCobros.setOptions({
				columns : bcsMovimientoFacturarGrid.columnasGrillaCobrosVariableMovimiento()
			});
		}
		
		if($scope.data.oConfiguracionComboTipoCobro.idActual === 'COFI'){
			$scope.data.gridApiModificarMovimientoCobros.setOptions({
				columns : bcsMovimientoFacturarGrid.columnasGrillaCobrosFijoMovimiento()
			});
		}
		
		idsMovimientoComprobante = obtenerIdsMovimientoComprobante(movimientosSeleccionados);
		
		let consulta = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario : $scope.SesionIdUsuario,
			p_IdModulo : 'FAC',
			p_UsaReglasNegocio : 'S',
			p_IdCobro : $scope.data.oConfiguracionComboTipoCobro.idActual,
			p_MovimientosComprobante : idsMovimientoComprobante
		}
		
		bcsComunes.ventanaOverlay(idVentana, true);
		bcsServiciosFacturacion.ConsultarMovimientoComprobanteCobrosModificar(consulta, successConsulta, errorConsulta);
		
	};
	
	let mostrarErrorSeleccion = function(mensaje){
		let mensajesError = [];
			
		let P_OUT_MENSAJES =  {
			descripcion: $translate.instant(mensaje),
			tipo: "ERROR"
		};			
		mensajesError.push(P_OUT_MENSAJES);
		
		let detalleError = {
			"P_OUT_MENSAJES": mensajesError, 
			"P_OUT_ERRORESCONTADOR": 1
		};
		
		bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_OPERACION_CON_ERRORES"),
				conflictos: util.parseServerResponse(detalleError)
		},$scope.winID);
		
		
			
		$scope.fnSeleccionarTabSeleccion();
	}
	
	let obtenerIdsMovimientoComprobante = function (movimientosSeleccionados) {
		let ids = [];
		if($scope.data.oIndSeleccionarTodasOperaciones){
			let ds = $scope.confBasicaGrillaSeleccion.dataSource.data();
			$.each(ds, function (key,value){
				ids.push({"IdMovimientoComprobante" : value.IdMovimientoComprobante});
			});
			return ids;
		}
		
		$.each(movimientosSeleccionados, function (key,value){
			ids.push({"IdMovimientoComprobante" : value.IdMovimientoComprobante});
		});
		return ids;
	}
	
	$scope.fnSeleccionarTabSeleccion = function () {
		$scope.data.tabs['TabSeleccion'].active = true;
		$scope.activarModoConsulta();
		$timeout(function(e) {
			$.each($scope.data.tabs,function(key,value) { value.active=false; });
			$scope.data.tabs['TabSeleccion'].active=true; 
		}
		,0);
		$scope.data.tabs['TabCobros'].active = false;
	};
	
	let successConsulta = function(respuesta) {
		$scope.confBasicaGrillaCobros.dataSource.data(respuesta.P_OUT_COBROS);
		bcsComunes.ventanaOverlay(idVentana, false);
	}
	
	let errorConsulta = function(){
		bcsComunes.msg("error","error",$translate.instant('UI_ERROR_CONSULTA_COBROS_MODIFICACION'));
		bcsComunes.ventanaOverlay(idVentana, false); 
	}
	
	$scope.activarModoConsulta = function() {
		$scope.data.modoConsulta = true;
		idsMovimientoComprobante = [];
		cobros = {};
	}
	
	$scope.data.cancelar = function(){
		$scope.fnSeleccionarTabSeleccion();
	}
	
	let obtenerLlave = function(cobro){
		return cobro.IdTipoCobro + "_" + cobro.IdMonedaCobro + "_" + cobro.IdMonedaCobroLiquidacion + "_" + cobro.IdMonedaCobroSistema;
	}
	
	$scope.data.grabarModificarMasivoComisiones = function(){
		let entradaModificar = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario : $scope.SesionIdUsuario,
			p_IdModulo : 'FAC',
			p_UsaReglasNegocio : 'S',
			p_IdCobro : $scope.data.oConfiguracionComboTipoCobro.idActual,
			p_MovimientosComprobante : idsMovimientoComprobante,
			p_Cobros : []
		}
		let index = 1;
		$.each(cobros,function(key,value){
			entradaModificar.p_Cobros.push({
				IdInterno : index,
				IdCobro: value.IdTipoCobro,
				IdMonedaCobro: value.IdMonedaCobro,
				IdMonedaCobroLiquidacion: value.IdMonedaCobroLiquidacion,
				IdMonedaMovimientoComprobanteCobro: value.IdMonedaCobroSistema,
				MontoCobro: value.MontoCobro,
				MontoCobroAnterior: value.MontoCobroOriginal,
				Porcentaje : value.Porcentaje
			});
			index++;
		});
		if(entradaModificar.p_Cobros.length === 0){
			bcsComunes.msg("error","error",$translate.instant('UI_ERROR_NO_EXISTEN_CAMBIOS_EN_COBROS'));
			return;
		}
		bcsComunes.ventanaOverlay(idVentana, true);
		bcsServiciosFacturacion.ModificarMovimientoComprobanteCobrosMasivo(entradaModificar, successModificar, errorModificar);
	}
	
	let successModificar = function(respuesta) {
		bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_OPERACION_EXITOSA"),
				conflictos: util.parseServerResponse(respuesta)
		},$scope.winID);
		bcsComunes.ventanaOverlay(idVentana, false);
		$scope.fnSeleccionarTabSeleccion();
	}
	
	let errorModificar = function(){
		bcsComunes.msg("error","error",$translate.instant('UI_ERROR_CAMBIOS_EN_COBROS'));
		bcsComunes.ventanaOverlay(idVentana, false); 
	}
	
	let fnSeleccionarDeseleccionarTodo = function (){
		if($scope.data.oIndSeleccionarTodasOperaciones && $scope.confBasicaGrillaSeleccion.dataSource.data().length > 0){
			$scope.data.gridApiModificarMovimiento.select($scope.data.gridApiModificarMovimiento.tbody.find(">tr"));
		}else if(!$scope.data.oIndSeleccionarTodasOperaciones && $scope.confBasicaGrillaSeleccion.dataSource.data().length > 0){
			$scope.data.gridApiModificarMovimiento.clearSelection();
		}
	};
	
	$scope.$watch('data.gridApiModificarMovimiento.dataSource',function(newval,oldval){
		fnSeleccionarDeseleccionarTodo();
	});
	
});