(function() {
	angular.module('ModuloNormativa').controller('ModalGarantiasCtrl',
	function($scope,$translate,bcsServiciosNormativa,bcsComunes) {
		var mg = this;
		var idVentana = bcsComunes.ventanaWinId($scope);
		var parametros = bcsComunes.ventanaParams(idVentana);
		mg.Garantia = parametros.objGarantia === undefined ? null : $.extend({},parametros.objGarantia[0]);
		mg.GarantiaOriginal = parametros.objGarantia === undefined ? null : $.extend({},parametros.objGarantia[0]);
		var keyCab = $.extend({},parametros.keyCab);
		mg.IdGarantiaRequerido = true;
    	mg.MontoUmbralRequerido = true;
		mg.ValorGarantiaRequerido = true;
		
		mg.cmbbxTipoGarantia = {
			filtros : {
				Esquema : {field: "Esquema", operator: "eq", value: 'DJ1'},
				Version : {field: "Version", operator: "eq", value: parametros.version},
				Tipo : {field: "Tipo", operator: "eq", value: 'S6_TipoGar'}
			}
		};
		
		mg.cmbbxMonedaValorizacion = {
			filtros : {
				Esquema : {field: "Esquema", operator: "eq", value: 'DJ1'},
				Version : {field: "Version", operator: "eq", value: parametros.version},
				Tipo : {field: "Tipo", operator: "eq", value: 'S6_MonVal'}
			}
		}
		
		if (mg.Garantia){
			//modificacion
			mg.IdGarantiaDisabled = true;
			bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_MODIFICAR_GARANTIA'));
			mg.IdGarantiaRequerido = mg.Garantia.KS6_IdGar === null? true : false;
    		mg.MontoUmbralRequerido = mg.Garantia.S6_MontoUmb === null ? true : false;
			mg.ValorGarantiaRequerido = mg.Garantia.S6_ValGtia === null? true : false;
		} else{
			mg.IdGarantiaDisabled =  false;
			bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_AGREGAR_GARANTIA'));
		}
		
		mg.aceptar = function() {
			//modificacion
			if (mg.Garantia.ID){
				var campos = [
							{ nombre: "S6_TipoGar" ,tipo : "combo"},
							{ nombre: "S6_MonVal" ,tipo : "combo"},
							{ nombre: "S6_MontoUmb" ,tipo : "numero"},						
							{ nombre: "S6_ValGtia" ,tipo : "numero"}
						];
					
				var registroCambio = [];
				
				$.each(campos,function(key,value) {
					if (value.tipo === 'combo') {
						if (!(mg.Garantia[value.nombre] === false && mg.Garantia[value.nombre] === null) && (mg.Garantia[value.nombre] != mg.GarantiaOriginal[value.nombre]))
							registroCambio.push({ RutContraparte 	: keyCab.KRutCtraparte1,
												  IdContrato 		: keyCab.KIdContrato, 
												  FechaContrato 	: util.dateSPdate(keyCab.KFechaContrato),
												  IdOperacion 		: keyCab.KIdOpeEstruc,
												  Producto 			: keyCab.kProducto,
												  PeriodoComercial 	: util.dateSPdate(keyCab.kPeriodoComercial),
												  IdGarantia		: mg.Garantia.KS6_IdGar, 
												  IdUsuario			: $scope.SesionIdUsuario,
												  CampoModificado 	: value.nombre, 
												  ValorNuevo 		: (mg.Garantia[value.nombre] ? mg.Garantia[value.nombre]:"")
										});
					}
					if (value.tipo === 'numero') {
						if (mg.Garantia[value.nombre] !== mg.GarantiaOriginal[value.nombre])
							registroCambio.push({ RutContraparte 	: keyCab.KRutCtraparte1,
												  IdContrato 		: keyCab.KIdContrato, 
												  FechaContrato 	: util.dateSPdate(keyCab.KFechaContrato),
												  IdOperacion 		: keyCab.KIdOpeEstruc,
												  Producto 			: keyCab.kProducto,
												  PeriodoComercial 	: util.dateSPdate(keyCab.kPeriodoComercial),
												  IdGarantia		: mg.Garantia.KS6_IdGar, 
												  IdUsuario			: $scope.SesionIdUsuario,
												  CampoModificado 	: value.nombre, 
												  ValorNuevo 		: (mg.Garantia[value.nombre] || 0)
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
					p_IdGarantia		: mg.Garantia.KS6_IdGar,
					p_RegistroCambio	: registroCambio
				};
				bcsComunes.ventanaOverlay(idVentana,true);
				bcsServiciosNormativa.ModificarGarantia(datos).$promise.then(
					function(result) {
						console.log(result);
						if (result.P_OUT_ADVERTENCIASCONTADOR){
							bcsComunes.msg("warning","Advertencia","La operacion tiene advertencias");
						}
						else if (result.P_OUT_ERRORESCONTADOR){
							bcsComunes.msg("error","error","Error al modificar la Garantía");
						}
						else
							bcsComunes.msg("info","info","Garantía modificada");
						
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : "Garantía modificada",
							conflictos : util.parseServerResponse(result)}, idVentana);
						
						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							bcsComunes.ventanaData(idVentana,true);
							bcsComunes.ventanaCerrar(idVentana);
							bcsComunes.ventanaOverlay(idVentana,false);
						});
					},
					function(result){
						console.log(result);
						bcsComunes.msg("error","error","Error al modificar Garantía");
						bcsComunes.ventanaData(idVentana,true);
						bcsComunes.ventanaCerrar(idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);
					});
			}
			//creacion
			else {
				var lstGarantias = []; 
				var garantia = {
					p_RutContraparte		: keyCab.KRutCtraparte1,
					p_IdContrato			: keyCab.KIdContrato,
					p_FechaContrato			: util.dateSPdate(keyCab.KFechaContrato),
					p_IdOperacion			: keyCab.KIdOpeEstruc,
					p_Producto				: keyCab.kProducto,
					p_PeriodoComercial		: util.dateSPdate(keyCab.kPeriodoComercial),
					p_TipoGarantia		    : mg.Garantia.S6_TipoGar,
					p_MonedaValorizacion 	: mg.Garantia.S6_MonVal,
					p_IdGarantia			: mg.Garantia.KS6_IdGar,   
					p_MontoUmbral			: mg.Garantia.S6_MontoUmb,   
					p_ValorGarantia			: mg.Garantia.S6_ValGtia,   
				};
				
				lstGarantias.push(garantia);
				
				var datos = {
					p_IdEmpresa 			: $scope.SesionIdEmpresa,
					p_IdUsuario 			: $scope.SesionIdUsuario,
					p_IdModulo 				: 'NOR',
					p_TipoCarga 			: 'MANUAL',
					p_Garantias				: lstGarantias
				};
				
				bcsComunes.ventanaOverlay(idVentana,true);
				bcsServiciosNormativa.CrearGarantia(datos).$promise.then(
					function(result) {
					if(result.P_OUT_ERRORESCONTADOR > 0){
							var idMensaje = bcsComunes.ventanaMsg({ mensaje : "Error al agregar Garantía",
							conflictos : util.parseServerResponse(result)}, idVentana);
							
							bcsComunes.ventanaPromesa(idMensaje).then(function(){
								bcsComunes.ventanaData(idVentana,true);
								bcsComunes.ventanaOverlay(idVentana,false);								
							});
							
						}else{
							var idMensaje = bcsComunes.ventanaMsg({ mensaje : "Garantía agregada",
							conflictos : util.parseServerResponse(result)}, idVentana);
						
							bcsComunes.ventanaPromesa(idMensaje).then(function(){
								bcsComunes.ventanaData(idVentana,true);
								bcsComunes.ventanaCerrar(idVentana);
								bcsComunes.ventanaOverlay(idVentana,false);								
							});
						}
					},	
					function(result){
						bcsComunes.msg("error","error","Error al agregar garantía");
						bcsComunes.ventanaData(idVentana,false);
						bcsComunes.ventanaCerrar(idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);
					});
			}
		};
		
		mg.cancelar = function() {
			bcsComunes.ventanaData(idVentana,false);
			bcsComunes.ventanaCerrar(idVentana);
		};

		mg.blurRequerido = function(campo){
			if(campo === 'KS6_IdGar') (mg.IdGarantiaRequerido = mg.Garantia.KS6_IdGar === "" || mg.Garantia.KS6_IdGar === undefined) ? true : false;
			if(campo === 'S6_MontoUmb') (mg.MontoUmbralRequerido = mg.Garantia.S6_MontoUmb === "" || mg.Garantia.S6_MontoUmb === undefined) ? true : false;
			if(campo === 'S6_ValGtia') (mg.ValorGarantiaRequerido = mg.Garantia.S6_ValGtia === "" || mg.Garantia.S6_ValGtia === undefined) ? true : false;
		}
	});
}());