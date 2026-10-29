angular.module('ModuloFacturacion').controller('ModificarMovimientoCobroModalCtrl',
		function($scope,$translate,$modal,bcsComunes,bcsServiciosFacturacion, $rootScope,
				 bcsServicioPlataforma, bcsModificarMovimientoCobroGridModal,$timeout)
{
	$scope.data	= {};
	var idVentana = bcsComunes.ventanaWinId ($scope);
	
    var datosEntrada = bcsComunes.ventanaParams(idVentana);
	$scope.datosGrillaMvto = datosEntrada;
	
	//GRILLA DE COBROS
	var gridCobros = null;
	
	$scope.confBasicaModificarMovimientoCobros = {
		dataSource: new bcs.DataSource({
			type: "array",
			definition : {
				data : [],
				schema : bcsModificarMovimientoCobroGridModal.modeloMovimientoCobros(),
				sort : { field: "NombreCobro", dir: "asc" },
				pageSize : 10
			}
		}),			
		columns : bcsModificarMovimientoCobroGridModal.columnasMovimientoCobros(),
		save: function(e) {
			var seleccionado = e.model;
			seleccionado.HayDatosModificados = 'N';

			if(e.values.MontoCobro != undefined && e.values.MontoCobro !== seleccionado.MontoCobro){
				e.preventDefault(); //Para redefinir el comportamiento del evento 'save'
				
				seleccionado.MontoCobro = e.values.MontoCobro;
				var porcentajeTemp = e.values.MontoCobro / $scope.datosGrillaMvto.MontoMovimientoComprobante * 100;
				if(porcentajeTemp !== seleccionado.Porcentaje && seleccionado.NombreFormaCobro.toUpperCase() == 'VARIABLE'){
					seleccionado.Porcentaje = porcentajeTemp;
				}
				seleccionado.HayDatosModificados = 'S';
				
				gridCobros.refresh();
			}
			else if(e.values.Porcentaje != undefined && e.values.Porcentaje !== seleccionado.Porcentaje){
				e.preventDefault();
				
				seleccionado.Porcentaje = e.values.Porcentaje;
				var montoTemp = $scope.datosGrillaMvto.Monto * e.values.Porcentaje / 100;
				if(montoTemp != seleccionado.MontoCobro && montoTemp != 0){
					seleccionado.MontoCobro = montoTemp;
					seleccionado.HayDatosModificados = 'S';
				}
				
				gridCobros.refresh();
			}
			
			if (seleccionado.HayDatosModificados === 'S') {
				var datosActualizar = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
	                p_IdTrabajo : 0,
	                p_CalculosEntrada : [{
	                	IdTrabajo : 0,
	                	IdInterno : 0,
	                	Fecha :  $scope.SesionFechaSistema,
	                	IdMonedaOrigen : seleccionado.IdMonedaLiquidacionCobro,
	                	IdMonedaDestino : seleccionado.IdMonedaLiquidacionCobro,
	                	ValorCambioLocalModificado : 0,
	                	MontoOrigen : Number(seleccionado.MontoCobro || 0),
	                	IdUso : 'FACTURACION'
	                }]
				};
				
				bcsComunes.ventanaOverlay(idVentana,true);
				bcsServiciosFacturacion.ActualizarMontosCobro(datosActualizar).$promise.then(function(respuesta){
			   		if (respuesta.p_out_CalculosSalida[0]) {
			   			seleccionado.MontoMovimientoComprobanteCobro = respuesta.p_out_CalculosSalida[0].MontoLocal ? respuesta.p_out_CalculosSalida[0].MontoLocal : 0;
			   			seleccionado.IdMonedaLiquidacionCobro = respuesta.p_out_CalculosSalida[0].IdMonedaDestino;
			   			seleccionado.MontoLiquidacionCobro = respuesta.p_out_CalculosSalida[0].MontoDestino;
			   		}
			   		else {
			   			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			   			bcsComunes.msg("error","error",$translate.instant('UI_ERROR_CALCULO'));
			   			
			   			seleccionado.IdMonedaMovimientoComprobanteCobro = "";
			   			seleccionado.MontoMovimientoComprobanteCobro = 0;
			   			seleccionado.IdMonedaLiquidacionCobro = "";
			   			seleccionado.MontoLiquidacionCobro = 0;
			   		}
			   		bcsComunes.ventanaOverlay(idVentana, false);
			   		
			   		gridCobros.refresh();
		   			
		   		},function(respuesta){
		   			bcsComunes.msg("error","error",$translate.instant('UI_ERROR_INGRESO'));
		   			bcsComunes.ventanaOverlay(idVentana, false);
		   		});
			}
		},
		sortable			: true,	
		pageable			: true,
		height				: 350,
		autoBind            : true,
		selectable 			: "row",
		navigatable         : true,
		allowCopy 			: true,
		gridCreated	: function(grid) {
			gridCobros = grid;
		},
		notEditableColumns	:
			function(row) {
				if(row.EsRecalculable === 'S'){
					return ['MontoCobro','Porcentaje'];
				}
				else if (row.NombreFormaCobro.toUpperCase() == 'FIJO') {
					return ['Porcentaje'];
				}
			}
	};
	
	
	//Almacena los datos para mostrar en la grilla
	//MOVIMIENTOS COBROS
	var transporteMovimientoCobros = function() {
		return {
			read : {
				url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1236",				
				dataType : "json"
			}
		};
	};
	
	//MOVIMIENTOS COBROS PERU
	var transporteMovimientoCobrosPeru = function() {
		return {
			read : {
				url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=20062",				
				dataType : "json"
			}
		};
	};
	
	
	var definicionOMovimientoCobros = function() {
		if ($rootScope.SesionLocale === 'CL') {
			return {
				serverFiltering : true,
				filter : [
					{field : "IdMovimientoComprobante", operator : "eq", value : $scope.bModifMvtoMod.oMovimientoComprobante.IdMovimientoComprobante},
					{field : "IdEstado", operator : "eq", value : "VIG"}
				],
				transport : {
					read : {
						url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1236",
						dataType: "json"
					}
				},
				schema: bcsModificarMovimientoCobroGridModal.modeloMovimientoCobros()
			};
		}
		else if ($rootScope.SesionLocale === 'PE') {
			return {
				serverFiltering : true, // Filtrado de datos en el servidor.
				filter : [
					{field : "IdMovimientoComprobante", operator : "eq", value : $scope.bModifMvtoMod.oMovimientoComprobante.IdMovimientoComprobante},
					{field : "IdEstado", operator : "eq", value : "VIG"}
				],
				transport : {
					read : {
						url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=20062",
						dataType: "json"
					}
				},
				schema: bcsModificarMovimientoCobroGridModal.modeloMovimientoCobros()
			};
		}
	};
	
	
	var ModifMvtoCobrosDatasource = new bcs.DataSource({
		type : "odata",
		definition : definicionOMovimientoCobros()
	});
	
	
	//Carga Grilla con datasource
	function ModifMvtoCobros () {
		//Limpia seleccionado de grilla
		$scope.seleccionModificarMovimientoCobros = undefined;
		 
	   	//Lee datos de grilla
		ModifMvtoCobrosDatasource.fetch().then(function() {
			var data = ModifMvtoCobrosDatasource.data();
			
			//Formatea Porcentaje para que sea number
			$.each(data,function(key,value) {
				value.Porcentaje = Number(value.Porcentaje);
			});
			
			if ($scope.gridApiModificarMovimientoCobros) {
				$scope.gridApiModificarMovimientoCobros.dataSource.data(data);
			}
			else {
				$scope.confBasicaModificarMovimientoCobros.dataSource.definition.data = data;
			}
			
			//Carga Datos en Grilla
			$timeout(function() {
				LimpiarDatosMvtoCobro();
			}, 250);
		});
	};
	  
	//Limpiar Datos Modificables
	function LimpiarDatosMvtoCobro () {
		$scope.data.oTipoCobro = "";
		$scope.data.activaroTipoCobro = true;		
		$scope.data.oPorcentajeCobro = 0;
		$scope.data.activaroPorcentajeCobro = true;
		$scope.data.oMonedaOperacionMovimientoComprobanteCobro = "";
		$scope.data.activaroMonedaOperacionMovimientoComprobanteCobro = true;
		$scope.data.oMontoOperacionCobro = 0;
		$scope.data.activaroMontoOperacionCobro = true;
		$scope.data.oMonedaLocalMovimientoComprobanteCobro = "";
		$scope.data.activaroMonedaLocalMovimientoComprobanteCobro = true;
		$scope.data.oMontoLocalCobro = 0;
		$scope.data.activaroMontoLocalCobro = true;
		$scope.data.oMonedaLiquidacionMovimientoComprobanteCobro = "";
		$scope.data.activaroMonedaLiquidacionMovimientoComprobanteCobro = true;
		$scope.data.oMontoLiquidacionCobro = 0;
        $scope.data.activaroMontoLiquidacionCobro = true;
	};
	
	//Ejecuta function de carga grilla
	ModifMvtoCobros();
	
	// Actualiza el registro seleccionado
    $scope.data.modificarMovimientoComprobanteCobro = function() {
		// Envio a actualizar cobro
		bcsComunes.setConflictos([]);
	
		// Acumula tabla de entrada de movimiento comprobante - general
		var MovimientoFacturarCobros = [];
		$.each($scope.confBasicaModificarMovimientoCobros.dataSource.data(),function(key,value) {
			if (value.HayDatosModificados === 'S') {
				MovimientoFacturarCobros.push({
					IdMovimientoComprobanteCobro : value.IdMovimientoComprobanteCobro,
					IdMovimientoComprobante : value.IdMovimientoComprobante,
					IdCobro : value.IdCobro,
					IdEstado : 'VIG',
					Porcentaje : value.Porcentaje,
					IdMonedaCobro : value.IdMonedaCobro,
					MontoCobro : value.MontoCobro,
			
					IdMonedaMovimientoComprobanteCobro : value.IdMonedaMovimientoComprobanteCobro,
					MontoMovimientoComprobanteCobro : value.MontoCobro,
			
					IdMonedaLiquidacionCobro : value.IdMonedaLiquidacionCobro,
					MontoLiquidacionCobro : value.MontoCobro,
					MontoMovimientoComprobante : $scope.bModifMvtoMod.oMovimientoComprobante.MontoMovimientoComprobante
				});
			}
		});
		
		// Valida que se hayan seleccionado movimientos forma de pago
		if (MovimientoFacturarCobros === undefined || MovimientoFacturarCobros.length === 0) {
			$scope.data.btnActivos = true; // Activa botones
			bcsComunes.msg("warning", "warning", $translate.instant('UI_SIN_CAMBIOS'));
			return;
		}

		// parametros de servicio
		var datos = {};
		datos.p_IdEmpresa = $scope.SesionIdEmpresa;
		datos.p_IdUsuario = $scope.SesionIdUsuario;
		datos.p_IdModulo = 'FAC';
		datos.p_simularOperacion = 'N';
		datos.p_UsaReglasNegocio = 'S';
		datos.p_EsIngresoAutomatico = 'N';
		datos.P_MOVIMIENTOFACTURARCOBROS = MovimientoFacturarCobros;

		bcsComunes.ventanaOverlay(idVentana, true);
		bcsServiciosFacturacion.ModificarMovimientoComprobante(datos).$promise.then(function(respuesta) {
			bcsComunes.ventanaOverlay(idVentana, false);

			if (respuesta.P_OUT_ERRORESCONTADOR == "0" && respuesta.P_OUT_ADVERTENCIASCONTADOR == "0") {
				bcsComunes.ventanaMsg({conflictos : util.parseServerResponse(respuesta)}, idVentana);

				// Ejecuta function de carga grilla
				ModifMvtoCobros();

				// Limpia datos panel
				LimpiarDatosMvtoCobro();
			}
			else if (respuesta.P_OUT_ERRORESCONTADOR > 0) {
				bcsComunes.ventanaMsg({
					mensaje : $translate.instant(""),
					conflictos : util.parseServerResponse(respuesta)
				},idVentana);
			}
			else if (respuesta.P_OUT_ADVERTENCIASCONTADOR > 0) {
				bcsComunes.ventanaMsg({
					mensaje : $translate.instant(""),
					conflictos : util.parseServerResponse(respuesta)
				},idVentana);
			}
		},
		function(respuesta) {
			bcsComunes.ventanaOverlay(idVentana, false);
			bcsComunes.msg("error", "Error", 'UI_ERROR_MODIFICACION');
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		});
	};
	
	
	// Cancela Operación
    $scope.data.cancelar = function() {
    	bcsComunes.ventanaCerrar(idVentana);
    };
    
});
