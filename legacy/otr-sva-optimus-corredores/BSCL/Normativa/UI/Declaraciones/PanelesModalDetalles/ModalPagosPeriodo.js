(function() {
	angular.module('ModuloNormativa').controller('ModalPagosPeriodoCtrl',
	function($scope,$translate,bcsServiciosNormativa,bcsComunes) {
		var mpp = this;
		var idVentana = bcsComunes.ventanaWinId($scope);
		var parametros = bcsComunes.ventanaParams(idVentana);
		mpp.PagoPeriodo = parametros.objPagoPeriodo === undefined ? null : $.extend({},parametros.objPagoPeriodo[0]);
		mpp.PagoPeriodoOriginal = parametros.objPagoPeriodo === undefined ? null : $.extend({},parametros.objPagoPeriodo[0]);
		var keyCab = $.extend({},parametros.keyCab);
		mpp.OptFechaPago = {};

		mpp.CorrelativoPagoRequerido = true;
    	mpp.FechaPagoRequerido = true;
		mpp.MontoPagoRequerido = true;
		
		mpp.cmbbxIdPago = {
			filtros : {
				Esquema : {field: "Esquema", operator: "eq", value: 'DJ1'},
				Version : {field: "Version", operator: "eq", value: parametros.version},
				Tipo : {field: "Tipo", operator: "eq", value: 'KS5_IdPago'}
			}
		};
		
		mpp.cmbbxTipoPago = {
			filtros : {
				Esquema : {field: "Esquema", operator: "eq", value: 'DJ1'},
				Version : {field: "Version", operator: "eq", value: parametros.version},
				Tipo : {field: "Tipo", operator: "eq", value: 'S5_TipoPago'}
			}
		}
		
		mpp.cmbbxCodMoneda = {
			filtros : {
				Esquema : {field: "Esquema", operator: "eq", value: 'DJ1'},
				Version : {field: "Version", operator: "eq", value: parametros.version},
				Tipo : {field: "Tipo", operator: "eq", value: 'S5_CodMoneda'}
			}
		}
		
		if (mpp.PagoPeriodo){
			//modificacion
			mpp.CorrelativoPagoDisabled = true;
			mpp.IdPagoActivado = false;
			bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_MODIFICAR_PERIODO_PAGO'));
			mpp.CorrelativoPagoRequerido = mpp.PagoPeriodo.KS5_CorrPago === null? true : false;
    		mpp.FechaPagoRequerido = mpp.PagoPeriodo.S5_FecPago === null ? true : false;
			mpp.MontoPagoRequerido = mpp.PagoPeriodo.S5_MontoPago === null? true : false;
		} else{
			mpp.CorrelativoPagoDisabled = false;
			mpp.IdPagoActivado = true;
			bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_AGREGAR_PERIODO_PAGO'));
			mpp.OptFechaPago.min = $scope.$root.SesionFechaSistema;
			mpp.S5_FecPago = $scope.$root.SesionFechaSistema;
		}
		
		mpp.aceptar = function() {
			//modificacion
			if (mpp.PagoPeriodo.ID){
				var campos = [
							{ nombre: "S5_FecPago" ,tipo : "fecha"},
							{ nombre: "S5_MontoPago" ,tipo : "numero"},						
							{ nombre: "S5_TipoPago" ,tipo : "combo"},
							{ nombre: "S5_CodMoneda" ,tipo : "combo"}
						];
					
				var registroCambio = [];
				
				$.each(campos,function(key,value) {
					if (value.tipo === 'combo') {
						if (!(mpp.PagoPeriodo[value.nombre] === false && mpp.PagoPeriodo[value.nombre] === null) && (mpp.PagoPeriodo[value.nombre] != mpp.PagoPeriodoOriginal[value.nombre]))
							registroCambio.push({ RutContraparte 	: keyCab.KRutCtraparte1,
												  IdContrato 		: keyCab.KIdContrato, 
												  FechaContrato 	: util.dateSPdate(keyCab.KFechaContrato),
												  IdOperacion 		: keyCab.KIdOpeEstruc,
												  Producto 			: keyCab.kProducto,
												  PeriodoComercial 	: util.dateSPdate(keyCab.kPeriodoComercial),
												  CorrelativoPago	: mpp.PagoPeriodo.KS5_CorrPago, 
												  IdPago			: mpp.PagoPeriodo.KS5_IdPago, 
												  IdUsuario			: $scope.SesionIdUsuario,
												  CampoModificado 	: value.nombre,
												  ValorNuevo 		: (mpp.PagoPeriodo[value.nombre] ? mpp.PagoPeriodo[value.nombre]:"")
										});
					}
					if (value.tipo === 'fecha') {
						if (mpp.PagoPeriodo[value.nombre] !== mpp.PagoPeriodoOriginal[value.nombre])
							registroCambio.push({ RutContraparte 	: keyCab.KRutCtraparte1,
												  IdContrato 		: keyCab.KIdContrato, 
												  FechaContrato 	: util.dateSPdate(keyCab.KFechaContrato),
												  IdOperacion 		: keyCab.KIdOpeEstruc,
												  Producto 			: keyCab.kProducto,
												  PeriodoComercial 	: util.dateSPdate(keyCab.kPeriodoComercial),
												  CorrelativoPago	: mpp.PagoPeriodo.KS5_CorrPago, 
												  IdPago			: mpp.PagoPeriodo.KS5_IdPago, 
												  IdUsuario			: $scope.SesionIdUsuario,
												  CampoModificado 	: value.nombre, 
												  ValorNuevo 		: (mpp.PagoPeriodo[value.nombre] ? util.dateSPdate(mpp.PagoPeriodo[value.nombre]) :"")
										});
					}
					if (value.tipo === 'numero') {
						if (mpp.PagoPeriodo[value.nombre] !== mpp.PagoPeriodoOriginal[value.nombre])
							registroCambio.push({ RutContraparte 	: keyCab.KRutCtraparte1,
												  IdContrato 		: keyCab.KIdContrato, 
												  FechaContrato 	: util.dateSPdate(keyCab.KFechaContrato),
												  IdOperacion 		: keyCab.KIdOpeEstruc,
												  Producto 			: keyCab.kProducto,
												  PeriodoComercial 	: util.dateSPdate(keyCab.kPeriodoComercial),
												  CorrelativoPago	: mpp.PagoPeriodo.KS5_CorrPago, 
												  IdPago			: mpp.PagoPeriodo.KS5_IdPago, 
												  IdUsuario			: $scope.SesionIdUsuario,
											      CampoModificado 	: value.nombre, 
												  ValorNuevo 		: (mpp.PagoPeriodo[value.nombre] || 0)
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
					p_CorrelativoPago	: mpp.PagoPeriodo.KS5_CorrPago,
					p_RegistroCambio	: registroCambio
				};
				bcsComunes.ventanaOverlay(idVentana,true);
				bcsServiciosNormativa.ModificarPagosPeriodo(datos).$promise.then(
					function(result) {
						if (result.P_OUT_ADVERTENCIASCONTADOR){
							bcsComunes.msg("warning","Advertencia","La operacion tiene advertencias");
						}
						else if (result.P_OUT_ERRORESCONTADOR){
							bcsComunes.msg("error","error","Error al modificar el Periodo de Pago");
						}
						else
							bcsComunes.msg("info","info","Periodo de Pago modificado");
						
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : "Periodo de Pago modificado",
							conflictos : util.parseServerResponse(result)}, idVentana);
						
						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							bcsComunes.ventanaData(idVentana,true);
							bcsComunes.ventanaCerrar(idVentana);
							bcsComunes.ventanaOverlay(idVentana,false);
						});
					},
					function(result){
						bcsComunes.msg("error","error","Error al modificar Periodo de Pago");
						bcsComunes.ventanaData(idVentana,true);
						bcsComunes.ventanaCerrar(idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);
					});
			}
			//creacion
			else {
				var lstPagosPeriodo = [];
				var pagoPeriodo = {				
					p_RutContraparte		: keyCab.KRutCtraparte1,
					p_IdContrato			: keyCab.KIdContrato,
					p_FechaContrato			: util.dateSPdate(keyCab.KFechaContrato),
					p_IdOperacion			: keyCab.KIdOpeEstruc,
					p_Producto				: keyCab.kProducto,
					p_PeriodoComercial		: util.dateSPdate(keyCab.kPeriodoComercial),		
					p_CorrelativoPago		: mpp.PagoPeriodo.KS5_CorrPago,
					p_IdPago				: mpp.PagoPeriodo.KS5_IdPago,
					p_FechaPago				: util.dateSPdate(mpp.PagoPeriodo.S5_FecPago),   
					p_MontoPago				: mpp.PagoPeriodo.S5_MontoPago,     
					p_TipoPago				: mpp.PagoPeriodo.S5_TipoPago,
					p_CodMoneda				: mpp.PagoPeriodo.S5_CodMoneda 
				};
				
				lstPagosPeriodo.push(pagoPeriodo); 
				
				var datos = {
					p_IdEmpresa 			: $scope.SesionIdEmpresa,
					p_IdUsuario 			: $scope.SesionIdUsuario,
					p_IdModulo 				: 'NOR',
					p_TipoCarga				: 'MANUAL',
					p_PagosPeriodo			: lstPagosPeriodo
				};
				
				bcsComunes.ventanaOverlay(idVentana,true);
				bcsServiciosNormativa.CrearPagosPeriodo(datos).$promise.then(
					function(result) {
						if(result.P_OUT_ERRORESCONTADOR > 0){	
							var idMensaje = bcsComunes.ventanaMsg({ mensaje : "Error al agregar Periodo de Pago",
							conflictos : util.parseServerResponse(result)}, idVentana);
							
							bcsComunes.ventanaPromesa(idMensaje).then(function(){
								bcsComunes.ventanaData(idVentana,true);
								bcsComunes.ventanaOverlay(idVentana,false);	
							});
						}else{
							var idMensaje = bcsComunes.ventanaMsg({ mensaje : "Periodo de Pago agregado",
							conflictos : util.parseServerResponse(result)}, idVentana);
							
							bcsComunes.ventanaPromesa(idMensaje).then(function(){
								bcsComunes.ventanaData(idVentana,true);
								bcsComunes.ventanaCerrar(idVentana);
								bcsComunes.ventanaOverlay(idVentana,false);	
							});
						}
					},
					function(result){
						bcsComunes.msg("error","error","Error al agregar Periodo de Pago");
						bcsComunes.ventanaData(idVentana,false);
						bcsComunes.ventanaCerrar(idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);
					});
			}
		};
		
		mpp.cancelar = function() {
			bcsComunes.ventanaData(idVentana,false);
			bcsComunes.ventanaCerrar(idVentana);
		};

		mpp.blurRequerido = function(campo){
			if(campo === 'KS5_CorrPago') (mpp.CorrelativoPagoRequerido = mpp.PagoPeriodo.KS5_CorrPago === "" || mpp.PagoPeriodo.KS5_CorrPago === undefined) ? true : false;
			if(campo === 'S5_FecPago') (mpp.FechaPagoRequerido = mpp.PagoPeriodo.S5_FecPago === "" || mpp.PagoPeriodo.S5_FecPago === undefined) ? true : false;
			if(campo === 'S5_MontoPago') (mpp.MontoPagoRequerido = mpp.PagoPeriodo.S5_MontoPago === "" || mpp.PagoPeriodo.S5_MontoPago === undefined) ? true : false;
		}
	});
}());
//------------------------- FIN CONTROLLER --------------------------