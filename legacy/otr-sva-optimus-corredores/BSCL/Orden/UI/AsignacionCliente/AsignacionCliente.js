angular.module('ModuloOrdenes').controller('AsignacionClienteCtrl',function ($scope,bcsComunes,bcsServicioOrden,bcsConfiguracionOrdenes,bcsConsultasOrdenes,bcsServicioPlataforma, $translate, bcsAlerta, OPCIONES_BCSALERTA) {
	
	if ($scope.SesionLocale === 'CL') {
		bcsConfiguracionOrdenes.CONFIGURACION.promise.then(function(result){
			if (bcsConfiguracionOrdenes.CONFIGURACION.IdBovedaDefecto) {
				$scope.idBovedaDefecto = bcsConfiguracionOrdenes.CONFIGURACION.IdBovedaDefecto;
			}
		});
	}
	
	
	$scope.formData = {};
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	var params = bcsComunes.ventanaParams($scope.idVentana);
	
	$scope.formData.tipoCambio=0;
	$scope.formData.IdTipoOperacion = params.transacciones[0].IdTipoOperacion;
	$scope.formData.IdTipoInstrumento = params.transacciones[0].IdTipoInstrumento;
	$scope.formData.fechaInicial = params.transacciones[0].FechaOperacionTransaccion;
	$scope.formData.idMoneda = params.transacciones[0].IdMonedaTransaccion;
	$scope.formData.idMonedaLiquidacionDefecto = params.transacciones[0].IdMonedaTransaccion;
	$scope.formData.idCondicionDeLiquidacion = params.transacciones[0].IdCondicionDeLiquidacion;
	$scope.formData.glosa = '';
	var OperacionSobregirable = 'N'; 
	var EsSobregiro = 'N';	
	var tipoOperacionSobregirable = [];
	bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise('CUS.OperacionesNegativas', $scope.SesionIdEmpresa, 'CUSTODIA')
	 .then(function (result) {
		tipoOperacionSobregirable = result.Valor.split(",").map(item => item.trim())
	 });

	var ventaCorta = _.groupBy(params.transacciones, function(num){ return num.EsVentaCorta; });
	if (Object.keys(ventaCorta).length === 1 && ventaCorta["S"] !== undefined) {
		$scope.formData.esVentaCorta = true;
		$scope.idFormaCustodiaPortafolio = 'NOM';
	}
	else {
		$scope.formData.esVentaCorta = false;
	}

	if ($scope.formData.esVentaCorta) {
		bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise('CUS.UsaFechaLiquidacionEnVencimiento', $scope.SesionIdEmpresa, 'PLATAFORMA')
				.then(function (parametroUsaFechaLiquidacion) {
					if (parametroUsaFechaLiquidacion.Valor === 'S') {
						$scope.formData.fechaInicial = params.transacciones[0].FechaLiquidacion;
					}
				});
	}
	
	var c1 = _.groupBy(params.transacciones, function(num){ return num.IdClasificacionTipoInstrumento; });
	clasificacion = Object.keys(c1)[0];
	$scope.formData.IdClasificacionTipoInstrumento = clasificacion;
	$scope.formData.EsCuentaPropia = false;
	
	$scope.$watch('formData.portafolioSeleccionado.IdPortafolio',function(newval,oldval){
		
		if (!$scope.formData.portafolioSeleccionado || !$scope.formData.portafolioSeleccionado.IdPortafolio) {
			return;
		}
		
			
		bcsConfiguracionOrdenes.FUNCIONES.getInformacionPortafolios([$scope.formData.portafolioSeleccionado.IdPortafolio]).then(function(result){
			
			if (!$scope.formData.esVentaCorta){
				if (clasificacion === 'RV' || clasificacion === 'DV' && result[0]) {
					$scope.idFormaCustodiaPortafolio = result[0].OperaCustodiaRV === 'S'?'CUS':'NOM';
				}
				else if ((clasificacion === 'RF' || clasificacion === 'IF' )&& result[0]) {
					$scope.idFormaCustodiaPortafolio = result[0].OperaCustodiaRFIF === 'S'?'CUS':'NOM';
				}
				else {
					$scope.idFormaCustodiaPortafolio = result[0].OperaCustodiaRV === 'S'?'CUS':'NOM';
				}				
			} else {
				$scope.idFormaCustodiaPortafolio = 'NOM';
			}
			

			
			$scope.formData.filtrosPreferenciaPago.IdPortafolio.value = $scope.formData.portafolioSeleccionado.IdPortafolio;
			if (result[0].OperaCuentaInversion === "S") {
				$scope.formData.pagoCobroCuentaInversion = true;
				$scope.formData.pagoCobroCuentaInversionDisabled = false;
			} else {
				$scope.formData.pagoCobroCuentaInversion = false;
				$scope.formData.pagoCobroCuentaInversionDisabled = true;
			}
			
			if(result[0].CodigoIfrs && $scope.formData.portafolioSeleccionado.EsCuentaPropia==="S"){
				$scope.formData.codigoIfrs = result[0].CodigoIfrs; 
			}
		});
	});
	
	$scope.formData.filtrosMonedaLiquidacion = {
			IdMoneda 			: { "field"   : "IdMoneda","operator": "eq", "value"   : $scope.formData.idMoneda },
			IdTipoInstrumento	: { "field"   : "IdTipoInstrumento","operator": "eq", "value"   : $scope.formData.IdTipoInstrumento },
	}

	$scope.$watch('formData.pagoCobroCuentaInversion',function(newval){
		if (newval)
			$scope.formData.IdPreferenciaPagoPortafolio =  false;
	});
	
	$scope.formData.filtrosBoveda = {
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "VIG" },
			IdBoveda : { "field"   : "IdBoveda","operator": "eq", "value"   : "" }
	}
	
	$scope.formData.filtrosCuentaCustodio = {
			IdPortafolio 			: { "field"   : "IdPortafolio","operator": "eq", "value"   : "" },
			IdEstadoAutorizacion 	: { "field"   : "IdEstadoAutorizacion","operator": "neq", "value"   : "RZD" },
			IdEstado 				: { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
	}
	
	$scope.formData.filtrosPreferenciaPago = {
			IdMedioDePago 	: { "field"   : "IdMedioDePago",operator: "neq", "value"   :  "CIN" },
			IdPortafolio 	: { "field"   : "IdPortafolio","operator": "eq", "value"   :  "" },
			IdMoneda 		: { "field"   : "IdMoneda","operator": "eq", "value"   :  "" },
			IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   : $scope.formData.IdTipoOperacion }
	};
	
	
	$scope.formData.aceptarAsignacion = function() {

		bcsComunes.ventanaOverlay($scope.idVentana,true);

		if ( $scope.formData.glosa.length > 100 ) {
				bcsComunes.ventanaOverlay($scope.idVentana,false);
				bcsComunes.ventanaMsgErr($translate.instant('UI_GLOSA_MAX_CARACTERES'),$scope.idVentana);
			return;
		}

		var asignaciones =   [];
		var i = 0;
		$.each(params.transacciones,function(key,value){
			var asig11 = {
				IdInterno					: i++,      
				FechaRecepcion				: util.dateSPseconddate(util.date($scope.SesionFechaSistema).hour(util.date(new Date()).hour()).minute(util.date(new Date()).minute()).second(util.date(new Date()).second()).toDate()),
				Glosa						: $scope.formData.glosa, 
				IdBoveda					: $scope.formData.bovedaSeleccionada.IdBoveda,
				IdFormaCustodia				: $scope.formData.formaCustodiaSeleccionada.IdFormaCustodiaOrden,
				EsFacturaInmediata			: 'N',
				IdTransaccion				: value.IdTransaccion,
				FolioTransaccion			: value.FolioTransaccion,
				IdPortafolio				: $scope.formData.portafolioSeleccionado.IdPortafolio,
				Cantidad					: value.CantidadDisponibleTransaccion,
				ValorRescate				: value.ValorRescateDisponibleTransaccion,
				FolioCartera				: 0,
				TipoCambio					: $scope.formData.tipoCambio ||0,
				IdMonedaLiquidacionOrden 	: $scope.formData.monedaLiquidacionSeleccionada.IdMonedaLiquidacion,
				TasaVentaCorta				: $scope.formData.tasaVentaCorta || 0,
				PlazoVentaCorta				: $scope.formData.plazoVentaCorta || 0,
				PagoCobroCuentaInversion 	: $scope.formData.pagoCobroCuentaInversion? "S":"N",
				IdPreferenciaPagoPortafolio : $scope.formData.formaDePagoSeleccionada.IdPreferenciaPagoPortafolio,
				IdCuentaCustodioPortafolio	: $scope.formData.cuentaCustodioSeleccionada? $scope.formData.cuentaCustodioSeleccionada.IdCuentaCustodioPortafolio : null,
				PorcentajeAllIn				: $scope.formData.porcentajeAllIn,
				CodigoIFRS					: $scope.formData.codigoIFRSSeleccionado?$scope.formData.codigoIFRSSeleccionado.Codigo : null,
				IngresoDesde				: "COR"
			};
			
			asignaciones.push(asig11);
			
		})
		for(var i = 0; i < tipoOperacionSobregirable.length; i++){
			if(tipoOperacionSobregirable[i] === $scope.formData.IdTipoOperacion){
				OperacionSobregirable = 'S';
			}
		}
		if(OperacionSobregirable === 'S'){
			var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo : 'ORD',
				p_Ordenes : asignaciones,
				p_CobrosModificados : [],
				p_simularOperacion : 'S'
			};
			var promiseSimulacion= {};
			promiseSimulacion = bcsServicioOrden.CrearOrdenesPostreras(datos).$promise;
			promiseSimulacion.then(function(result) {
				if(result.P_OUT_MENSAJES.length === 0 || result.P_OUT_ERRORESCONTADOR > 0){
					bcsComunes.ventanaOverlay($scope.idVentana,true);
					confirmarOrden(asignaciones);
				}else{
					bcsComunes.ventanaOverlay($scope.idVentana,true);
					var ClientesSobregirados = [];
					for(var i = 0; i < result.P_OUT_MENSAJES.length; i++){
						if(result.P_OUT_MENSAJES[i].codigo === "UI_ASIGNACION_SOBREGIRO"){
							ClientesSobregirados.push(result.P_OUT_MENSAJES[i].descripcion);
						}	
					}
					if(ClientesSobregirados.length >0){
						var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant(ClientesSobregirados.join(", ")+" "+$translate.instant('UI_CONTINUAR')), $scope.idVentana);
						t.then(function(result2){
							if (result2 === OPCIONES_BCSALERTA.SI) {
								confirmarOrden(asignaciones);
							}else{
								return;
							}
						});	
					}else{
						confirmarOrden(asignaciones);
					}
				}	
			},function(result){
				bcsComunes.ventanaOverlay($scope.idVentana,false);
				bcsComunes.ventanaMsg({
						mensaje:'UI_ERROR_INTERNO_SISTEMA',
						conflictos:util.parseServerResponse(result)
					},$scope.idVentana);	
			});
		}else{
			confirmarOrden(asignaciones);
		}
	};
	
	function confirmarOrden(asignaciones){
		var datos = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario : $scope.SesionIdUsuario,
			p_IdModulo : 'ORD',
			p_Ordenes : asignaciones,
			p_CobrosModificados : [],
		};

	bcsComunes.ventanaOverlay($scope.idVentana,true);
	promise = bcsServicioOrden.CrearOrdenesPostreras(datos).$promise; 
	
	promise.then(function(result) {
		bcsComunes.ventanaOverlay($scope.idVentana,false);
		bcsComunes.ventanaOverlay($scope.idVentana,false);
		if (result.P_OUT_ERRORESCONTADOR === 0) {
			//bcsComunes.msg("info","UI_INFORMACION","UI_ASIGNACIONES_CREADAS");
			bcsComunes.ventanaMsg({
				mensaje:'UI_ASIGNACIONES_CREADAS',
				conflictos:util.parseServerResponse(result)
			},$scope.idVentana);
			bcsComunes.ventanaCerrar($scope.idVentana);
		}
		else
			bcsComunes.ventanaMsg({
				mensaje:'UI_ERROR_AL_CREAR_ASIGNACIONES',
				conflictos:util.parseServerResponse(result)
			},$scope.idVentana);
		
	},function(result){
		bcsComunes.ventanaOverlay($scope.idVentana,false);
		bcsComunes.ventanaMsg({
				mensaje:'UI_ERROR_INTERNO_SISTEMA',
				conflictos:util.parseServerResponse(result)
			},$scope.idVentana);

		
	});
	}
});
