(function() {
	angular.module('ModuloNormativa').controller('ModalCalendarioPagosCtrl',
	function($scope,$translate,bcsServiciosNormativa,bcsComunes) {
		var mcp = this;
		var idVentana = bcsComunes.ventanaWinId($scope);
		var parametros = bcsComunes.ventanaParams(idVentana);
		mcp.CalendarioPago = parametros.objCalPag === undefined ? null : $.extend({},parametros.objCalPag[0]);
		mcp.CalendarioPagoOriginal = parametros.objCalPag === undefined ? null : $.extend({},parametros.objCalPag[0]);
		var keyCab = $.extend({},parametros.keyCab);
		mcp.OptFechaFixTasa = {};
		mcp.OptFechaInicio = {};
		mcp.OptFechaTermino = {};
		
		mcp.NumeroFlujoRequerido = true;
    	mcp.TipoFlujoRequerido = true;
		mcp.FactorTasaRequerido = true;
		mcp.SpreadTasaRequerido = true;
		mcp.TasaFijaRequerido = true;
		mcp.OptFechaFixTasaRequerido = true;
		mcp.OptFechaInicioRequerido = true;
		mcp.OptFechaTerminoRequerido = true;
		mcp.MonedaMontoRequerido = true;
		mcp.MontoNocionalRequerido = true;
		mcp.MontoIntercambioRequerido = true;
		
		mcp.cmbbxTipoFlujo = {
			filtros : {
				Esquema : {field: "Esquema", operator: "eq", value: 'DJ1'},
				Version : {field: "Version", operator: "eq", value: parametros.version},
				Tipo : {field: "Tipo", operator: "eq", value: 'S4_XR_TipoFlujo'}
			}
		};
		
		mcp.cmbbxMonedaMonto = {
			filtros : {
				Esquema : {field: "Esquema", operator: "eq", value: 'DJ1'},
				Version : {field: "Version", operator: "eq", value: parametros.version},
				Tipo : {field: "Tipo", operator: "eq", value: 'S4_X_MonMonto'}
			}
		}
		
		if (mcp.CalendarioPago){
			//modificacion
			mcp.NumeroFlujoDisabled = true;
			bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_MODIFICAR_CALENDARIO_PAGOS'));
			mcp.NumeroFlujoRequerido = mcp.CalendarioPago.KS4_XR_NroFlu === null? true : false;
    		mcp.TipoFlujoRequerido = mcp.CalendarioPago.S4_XR_TipoFlujo === null ? true : false;
			mcp.FactorTasaRequerido = mcp.CalendarioPago.S4_XR_FacTasa === null? true : false;
			mcp.SpreadTasaRequerido = mcp.CalendarioPago.S4_XR_SpreadTasa === null? true : false;
			mcp.TasaFijaRequerido = mcp.CalendarioPago.S4_XR_TasaFija === null? true : false;
			mcp.FechaFixTasaRequerido = mcp.CalendarioPago.S4_XR_FecFixTasa === null? false : false;
			mcp.FechaInicioRequerido = mcp.CalendarioPago.S4_XR_FecIni === null? true : false;
			mcp.FechaTerminoRequerido = mcp.CalendarioPago.S4_XR_FecTer === null? true : false;
			mcp.MonedaMontoRequerido = mcp.CalendarioPago.S4_X_MonMonto === null? true : false;
			mcp.MontoNocionalRequerido = mcp.CalendarioPago.S4_XR_MontoNoc === null? true : false;
			mcp.MontoIntercambioRequerido = mcp.CalendarioPago.S4_XR_MontoInt === null? true : false;
		} else{
			mcp.NumeroFlujoDisabled = false;
			bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_AGREGAR_CALENDARIO_PAGOS'));
			mcp.OptFechaFixTasa.min = $scope.$root.SesionFechaSistema;
			mcp.FechaFixTasa = $scope.$root.SesionFechaSistema;
			mcp.OptFechaInicio.min = $scope.$root.SesionFechaSistema;
			mcp.FechaInicio = $scope.$root.SesionFechaSistema;
			mcp.OptFechaTermino.min = $scope.$root.SesionFechaSistema;
			mcp.FechaTermino = $scope.$root.SesionFechaSistema;
		}
		
		mcp.aceptar = function() {
			//modificacion
			if (mcp.CalendarioPago.ID){
				var campos = [
							{ nombre: "S4_XR_TipoFlujo" ,tipo : "combo"},
							{ nombre: "S4_XR_FacTasa" ,tipo : "numero"},
							{ nombre: "S4_XR_SpreadTasa" ,tipo : "numero"},						
							{ nombre: "S4_XR_TasaFija" ,tipo : "numero"},
							{ nombre: "S4_XR_FecFixTasa" ,tipo : "fecha"},
							{ nombre: "S4_XR_FecIni" ,tipo : "fecha"},
							{ nombre: "S4_XR_FecTer" ,tipo : "fecha"},
							{ nombre: "S4_XR_MontoNoc" ,tipo : "numero"},
							{ nombre: "S4_XR_MontoInt" ,tipo : "numero"},
							{ nombre: "S4_X_MonMonto" ,tipo : "combo"}
						];
					
				var registroCambio = [];
				
				$.each(campos,function(key,value) {
					if (value.tipo === 'combo') {
						if (!(mcp.CalendarioPago[value.nombre] === false && mcp.CalendarioPago[value.nombre] === null) && (mcp.CalendarioPago[value.nombre] != mcp.CalendarioPagoOriginal[value.nombre]))
							registroCambio.push({ RutContraparte 	: keyCab.KRutCtraparte1,
												  IdContrato 		: keyCab.KIdContrato, 
												  FechaContrato 	: util.dateSPdate(keyCab.KFechaContrato),
												  IdOperacion 		: keyCab.KIdOpeEstruc,
												  Producto 			: keyCab.kProducto,
												  PeriodoComercial 	: util.dateSPdate(keyCab.kPeriodoComercial),
												  NumeroFlujo		: mcp.CalendarioPago.KS4_XR_NroFlu, 
												  IdUsuario			: $scope.SesionIdUsuario,
												  CampoModificado 	: value.nombre, 
												  ValorNuevo 		: (mcp.CalendarioPago[value.nombre] ? mcp.CalendarioPago[value.nombre]:"")
										});
					}
					if (value.tipo === 'fecha') {
						if (mcp.CalendarioPago[value.nombre] !== mcp.CalendarioPagoOriginal[value.nombre])
							registroCambio.push({ RutContraparte 	: keyCab.KRutCtraparte1,
												  IdContrato 		: keyCab.KIdContrato, 
												  FechaContrato 	: util.dateSPdate(keyCab.KFechaContrato),
												  IdOperacion 		: keyCab.KIdOpeEstruc,
												  Producto 			: keyCab.kProducto,
												  PeriodoComercial 	: util.dateSPdate(keyCab.kPeriodoComercial),
												  NumeroFlujo		: mcp.CalendarioPago.KS4_XR_NroFlu, 
												  IdUsuario			: $scope.SesionIdUsuario,
												  CampoModificado 	: value.nombre, 
												  ValorNuevo 		: (mcp.CalendarioPago[value.nombre] ? util.dateSPdate(mcp.CalendarioPago[value.nombre]) :"")
										});
					}
					if (value.tipo === 'numero') {
						if (mcp.CalendarioPago[value.nombre] !== mcp.CalendarioPagoOriginal[value.nombre])
							registroCambio.push({ RutContraparte 	: keyCab.KRutCtraparte1,
												  IdContrato 		: keyCab.KIdContrato, 
												  FechaContrato 	: util.dateSPdate(keyCab.KFechaContrato),
												  IdOperacion 		: keyCab.KIdOpeEstruc,
												  Producto 			: keyCab.kProducto,
												  PeriodoComercial 	: util.dateSPdate(keyCab.kPeriodoComercial),
												  NumeroFlujo 		: mcp.CalendarioPago.KS4_XR_NroFlu,
												  IdUsuario 		: $scope.SesionIdUsuario, 
											 	  CampoModificado 	: value.nombre, 
												  ValorNuevo 		: (mcp.CalendarioPago[value.nombre] || 0)
										});
					}			
				});
				
				if (registroCambio.length === 0){
					bcsComunes.ventanaData(idVentana,true);
					bcsComunes.ventanaCerrar(idVentana);
					return;
				}
				
				var datos = {
					p_IdEmpresa 		: $scope.SesionIdEmpresa,
					p_IdUsuario 		: $scope.SesionIdUsuario,
					p_IdModulo 			:'NOR',
					p_NumeroFlujo		: mcp.CalendarioPago.KS4_XR_NroFlu,
					p_RegistroCambio	: registroCambio
				};
				bcsComunes.ventanaOverlay(idVentana,true);
				bcsServiciosNormativa.ModificarCalendarioPago(datos).$promise.then(
					function(result) {
						if (result.P_OUT_ADVERTENCIASCONTADOR){
							bcsComunes.msg("warning","Advertencia","La operacion tiene advertencias");
						}
						else if (result.P_OUT_ERRORESCONTADOR){
							bcsComunes.msg("error","error","Error al modificar calendario de pago");
						}
						else
							bcsComunes.msg("info","info","Calendario de pago modificado");
						
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : "Calendario de pago modificado",
							conflictos : util.parseServerResponse(result)}, idVentana);
						
						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							bcsComunes.ventanaData(idVentana,true);
							bcsComunes.ventanaCerrar(idVentana);
							bcsComunes.ventanaOverlay(idVentana,false);
						});
					},
					function(result){
						bcsComunes.msg("error","error","Error al modificar calendario de pago");
						bcsComunes.ventanaData(idVentana,true);
						bcsComunes.ventanaCerrar(idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);
					});
			}
			//creacion
			else {
				var lstCalendarioPago = [];
				var calendarioPago = {	
					p_RutContraparte		: keyCab.KRutCtraparte1,
					p_IdContrato			: keyCab.KIdContrato,
					p_FechaContrato			: util.dateSPdate(keyCab.KFechaContrato),
					p_IdOperacion			: keyCab.KIdOpeEstruc,
					p_Producto				: keyCab.kProducto,
					p_PeriodoComercial		: util.dateSPdate(keyCab.kPeriodoComercial),	
					p_NumeroFlujo			: mcp.CalendarioPago.KS4_XR_NroFlu,
					p_TipoFlujo				: mcp.CalendarioPago.S4_XR_TipoFlujo,
					p_FactorTasa			: mcp.CalendarioPago.S4_XR_FacTasa,   
					p_SpreadTasa			: mcp.CalendarioPago.S4_XR_SpreadTasa,   
					p_TasaFija				: mcp.CalendarioPago.S4_XR_TasaFija,   
					p_FechaFixTasa			: util.dateSPdate(mcp.CalendarioPago.S4_XR_FecFixTasa),   
					p_FechaInicio			: util.dateSPdate(mcp.CalendarioPago.S4_XR_FecIni),   
					p_FechaTermino			: util.dateSPdate(mcp.CalendarioPago.S4_XR_FecTer),   
					p_MontoNocional			: mcp.CalendarioPago.S4_XR_MontoNoc,   
					p_MontoIntercambio		: mcp.CalendarioPago.S4_XR_MontoInt,   
					p_MonedaMonto			: mcp.CalendarioPago.S4_X_MonMonto,   
	
				};
				
				lstCalendarioPago.push(calendarioPago);
				
				var datos = {
					p_IdEmpresa 			: $scope.SesionIdEmpresa,
					p_IdUsuario 			: $scope.SesionIdUsuario,
					p_IdModulo 				: 'NOR',
					p_TipoCarga				: 'MANUAL',
					p_CalendarioPagos		: lstCalendarioPago		
				};
				
				bcsComunes.ventanaOverlay(idVentana,true);
				bcsServiciosNormativa.CrearCalendarioPago(datos).$promise.then(
					function(result) {
						if(result.P_OUT_ERRORESCONTADOR > 0){
							var idMensaje = bcsComunes.ventanaMsg({ mensaje : "Error al agregar Calendario de pago",
							conflictos : util.parseServerResponse(result)}, idVentana);
							
							bcsComunes.ventanaPromesa(idMensaje).then(function(){
								bcsComunes.ventanaData(idVentana,true);
								bcsComunes.ventanaOverlay(idVentana,false);								
							});
							
						}else{
							var idMensaje = bcsComunes.ventanaMsg({ mensaje : "Calendario de pago agregado",
							conflictos : util.parseServerResponse(result)}, idVentana);
						
							bcsComunes.ventanaPromesa(idMensaje).then(function(){
								bcsComunes.ventanaData(idVentana,true);
								bcsComunes.ventanaCerrar(idVentana);
								bcsComunes.ventanaOverlay(idVentana,false);								
							});
						}	
					},
					function(result){
						bcsComunes.msg("error","error","Error al agregar calendario de pago");
						bcsComunes.ventanaData(idVentana,false);
						bcsComunes.ventanaCerrar(idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);
					});
			}
		};
		
		mcp.cancelar = function() {
			bcsComunes.ventanaData(idVentana,false);
			bcsComunes.ventanaCerrar(idVentana);
		};
		
		mcp.blurRequerido = function(campo){
			if(campo === 'KS4_XR_NroFlu') (mcp.NumeroFlujoRequerido = mcp.CalendarioPago.KS4_XR_NroFlu === "" || mcp.CalendarioPago.KS4_XR_NroFlu === undefined) ? true : false;
			if(campo === 'S4_XR_TipoFlujo') (mcp.TipoFlujoRequerido = mcp.CalendarioPago.S4_XR_TipoFlujo === "" || mcp.CalendarioPago.S4_XR_TipoFlujo === undefined) ? true : false;
			if(campo === 'S4_XR_FacTasa') (mcp.FactorTasaRequerido = mcp.CalendarioPago.S4_XR_FacTasa === "" || mcp.CalendarioPago.S4_XR_FacTasa === undefined) ? true : false;
			if(campo === 'S4_XR_SpreadTasa') (mcp.SpreadTasaRequerido = mcp.CalendarioPago.S4_XR_SpreadTasa === "" || mcp.CalendarioPago.S4_XR_SpreadTasa === undefined) ? true : false;
			if(campo === 'S4_XR_TasaFija') (mcp.TasaFijaRequerido = mcp.CalendarioPago.S4_XR_TasaFija === "" || mcp.CalendarioPago.S4_XR_TasaFija === undefined) ? true : false;
			if(campo === 'S4_XR_FecFixTasa') (mcp.FechaFixTasaRequerido = mcp.CalendarioPago.S4_XR_FecFixTasa === "" || mcp.CalendarioPago.S4_XR_FecFixTasa === undefined) ? true : false;
			if(campo === 'S4_XR_FecIni') (mcp.FechaInicioRequerido = mcp.CalendarioPago.S4_XR_FecIni === "" || mcp.CalendarioPago.S4_XR_FecIni === undefined) ? true : false;
			if(campo === 'S4_XR_FecTer') (mcp.FechaTerminoRequerido = mcp.CalendarioPago.S4_XR_FecTer === "" || mcp.CalendarioPago.S4_XR_FecTer === undefined) ? true : false;
			if(campo === 'S4_X_MonMonto') (mcp.MonedaMontoRequerido = mcp.CalendarioPago.S4_X_MonMonto === "" || mcp.CalendarioPago.S4_X_MonMonto === undefined) ? true : false;
			if(campo === 'S4_XR_MontoNoc') (mcp.MontoNocionalRequerido = mcp.CalendarioPago.S4_XR_MontoNoc === "" || mcp.CalendarioPago.S4_XR_MontoNoc === undefined) ? true : false;
			if(campo === 'S4_XR_MontoInt') (mcp.MontoIntercambioRequerido = mcp.CalendarioPago.S4_XR_MontoInt === "" || mcp.CalendarioPago.S4_XR_MontoInt === undefined) ? true : false;
		}
	});
}());
//------------------------- FIN CONTROLLER --------------------------