(function() {
	angular.module('ModuloNormativa').controller('ModalTipoActivoGarantiasCtrl',
	function($scope,$translate,bcsServiciosNormativa,bcsComunes) {
		var mdg = this;
		var idVentana = bcsComunes.ventanaWinId($scope);
		var parametros = bcsComunes.ventanaParams(idVentana);
		mdg.TipoActivoGarantia = parametros.objTipoActivoGarantia === undefined ? null : $.extend({},parametros.objTipoActivoGarantia[0]);
		mdg.TipoActivoGarantiaOriginal = parametros.objTipoActivoGarantia === undefined ? null : $.extend({},parametros.objTipoActivoGarantia[0]);
		var keyCab = $.extend({},parametros.keyCab);
		mdg.IdGarantiaRequerido = true;
    	mdg.PorcentajeGarantiaRequerido = true;
		
		mdg.cmbbxTipoGarantia = {
			filtros : {
				Esquema : {field: "Esquema", operator: "eq", value: 'DJ1'},
				Version : {field: "Version", operator: "eq", value: parametros.version},
				Tipo : {field: "Tipo", operator: "eq", value: 'S7_TipoGar'}
			}
		};
		
		mdg.cmbbxTipoActivoGarantia = {
			filtros : {
				Esquema : {field: "Esquema", operator: "eq", value: 'DJ1'},
				Version : {field: "Version", operator: "eq", value: parametros.version},
				Tipo : {field: "Tipo", operator: "eq", value: 'S7_TipoActGar'}
			}
		}
		
		if (mdg.TipoActivoGarantia){
			//modificacion
			mdg.IdGarantiaDisabled = true;
			bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_MODIFICAR_TIPO_ACTIVO_GARANTIA'));
			mdg.IdGarantiaRequerido = mdg.TipoActivoGarantia.KS7_IdGar === null? true : false;
    		mdg.PorcentajeGarantiaRequerido = mdg.TipoActivoGarantia.S7_PorcGar === null ? true : false;
		} else{
			mdg.IdGarantiaDisabled = false;
			bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_AGREGAR_TIPO_ACTIVO_GARANTIA'));
		}
		
		mdg.aceptar = function() {
			//modificacion
			if (mdg.TipoActivoGarantia.ID){
				var campos = [
							{ nombre: "S7_TipoGar" ,tipo : "combo"},
							{ nombre: "S7_TipoActGar" ,tipo : "combo"},
							{ nombre: "S7_PorcGar" ,tipo : "numero"}				
						];
					
				var registroCambio = [];
				
				$.each(campos,function(key,value) {
					if (value.tipo === 'combo') {
						if (!(mdg.TipoActivoGarantia[value.nombre] === false && mdg.TipoActivoGarantia[value.nombre] === null) && (mdg.TipoActivoGarantia[value.nombre] != mdg.TipoActivoGarantiaOriginal[value.nombre]))
							registroCambio.push({ RutContraparte 		: keyCab.KRutCtraparte1,
												  IdContrato 			: keyCab.KIdContrato, 
												  FechaContrato 		: util.dateSPdate(keyCab.KFechaContrato),
												  IdOperacion 			: keyCab.KIdOpeEstruc,
												  Producto 				: keyCab.kProducto,
												  PeriodoComercial 		: util.dateSPdate(keyCab.kPeriodoComercial),
												  IdGarantia			: mdg.TipoActivoGarantia.KS7_IdGar, 
												  IdUsuario				: $scope.SesionIdUsuario,
												  CampoModificado 		: value.nombre, 
												  ValorNuevo 			: (mdg.TipoActivoGarantia[value.nombre] ? mdg.TipoActivoGarantia[value.nombre]:"")
										});
								
					}
					if (value.tipo === 'numero') {
						if (mdg.TipoActivoGarantia[value.nombre] !== mdg.TipoActivoGarantiaOriginal[value.nombre])
							registroCambio.push({ RutContraparte 		: keyCab.KRutCtraparte1,
												  IdContrato 			: keyCab.KIdContrato, 
												  FechaContrato 		: util.dateSPdate(keyCab.KFechaContrato),
												  IdOperacion 			: keyCab.KIdOpeEstruc,
												  Producto 				: keyCab.kProducto,
												  PeriodoComercial 		: util.dateSPdate(keyCab.kPeriodoComercial),
												  IdGarantia			: mdg.TipoActivoGarantia.KS7_IdGar, 
												  IdUsuario				: $scope.SesionIdUsuario,
												  CampoModificado 		: value.nombre, 
												  ValorNuevo 			: (mdg.TipoActivoGarantia[value.nombre] || 0)
										});
					}			
				});
				
				if (registroCambio.length === 0){
					bcsComunes.ventanaData(idVentana,true);
					bcsComunes.ventanaCerrar(idVentana);
					return;
				}
				
				var datos = {
					p_IdEmpresa 			: $scope.SesionIdEmpresa,
					p_IdUsuario 			: $scope.SesionIdUsuario,
					p_IdModulo 				:'NOR',
					p_IdGarantia			: mdg.TipoActivoGarantia.KS7_IdGar,
					p_RegistroCambio		: registroCambio
				};
				
				bcsComunes.ventanaOverlay(idVentana,true);
				bcsServiciosNormativa.ModificarTipoActivoGarantia(datos).$promise.then(
					function(result) {
						console.log(result);
						if (result.P_OUT_ADVERTENCIASCONTADOR){
							bcsComunes.msg("warning","Advertencia","La operacion tiene advertencias");
						}
						else if (result.P_OUT_ERRORESCONTADOR){
							bcsComunes.msg("error","error","Error al modificar el Tipo Activo de Garantía");
						}
						else
							bcsComunes.msg("info","info","Tipo Activo de Garantía modificada");
						
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : "Tipo Activo de Garantía modificada",
							conflictos : util.parseServerResponse(result)}, idVentana);
						
						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							bcsComunes.ventanaData(idVentana,true);
							bcsComunes.ventanaCerrar(idVentana);
							bcsComunes.ventanaOverlay(idVentana,false);
						});
					},
					function(result){
						console.log(result);
						bcsComunes.msg("error","error","Error al modificar Tipo Activo de Garantía");
						bcsComunes.ventanaData(idVentana,true);
						bcsComunes.ventanaCerrar(idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);
					});
			}
			//creacion
			else {
				var lstTipoActivoGarantias = [];
				var tipoActivoGarantia = {		
					p_RutContraparte		: keyCab.KRutCtraparte1,
					p_IdContrato			: keyCab.KIdContrato,
					p_FechaContrato			: util.dateSPdate(keyCab.KFechaContrato),
					p_IdOperacion			: keyCab.KIdOpeEstruc,
					p_Producto				: keyCab.kProducto,
					p_PeriodoComercial		: util.dateSPdate(keyCab.kPeriodoComercial),		
					p_TipoGarantia			: mdg.TipoActivoGarantia.S7_TipoGar,
					p_IdGarantia 			: mdg.TipoActivoGarantia.KS7_IdGar,
					p_TipoActivoGarantia	: mdg.TipoActivoGarantia.S7_TipoActGar,   
					p_PorcentajeGarantia	: mdg.TipoActivoGarantia.S7_PorcGar  
				};
				
				lstTipoActivoGarantias.push(tipoActivoGarantia);
				
				var datos = {
					p_IdEmpresa 				: $scope.SesionIdEmpresa,
					p_IdUsuario 				: $scope.SesionIdUsuario,
					p_IdModulo 					: 'NOR',
					p_TipoCarga 				: 'MANUAL',
					p_TipoActivoGarantias		: lstTipoActivoGarantias
				};
				
				bcsComunes.ventanaOverlay(idVentana,true);
				bcsServiciosNormativa.CrearTipoActivoGarantia(datos).$promise.then(
					function(result) {
						if(result.P_OUT_ERRORESCONTADOR > 0){
							var idMensaje = bcsComunes.ventanaMsg({ mensaje : "Error al agregar Tipo Activo Garantía",
							conflictos : util.parseServerResponse(result)}, idVentana);
							
							bcsComunes.ventanaPromesa(idMensaje).then(function(){
								bcsComunes.ventanaData(idVentana,true);
								bcsComunes.ventanaOverlay(idVentana,false);								
							});
							
						}else{
							var idMensaje = bcsComunes.ventanaMsg({ mensaje : "Tipo Activo Garantía agregado",
							conflictos : util.parseServerResponse(result)}, idVentana);
						
							bcsComunes.ventanaPromesa(idMensaje).then(function(){
								bcsComunes.ventanaData(idVentana,true);
								bcsComunes.ventanaCerrar(idVentana);
								bcsComunes.ventanaOverlay(idVentana,false);								
							});
						}
					},
					function(result){

						bcsComunes.msg("error","error","Error al agregar Tipo Activo de Garantía");
						bcsComunes.ventanaData(idVentana,false);
						bcsComunes.ventanaCerrar(idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);
					});
			}
		};
		
		mdg.cancelar = function() {
			bcsComunes.ventanaData(idVentana,false);
			bcsComunes.ventanaCerrar(idVentana);
		};

		mdg.blurRequerido = function(campo){
			if(campo === 'KS7_IdGar') (mdg.IdGarantiaRequerido = mdg.TipoActivoGarantia.KS7_IdGar === "" || mdg.TipoActivoGarantia.KS7_IdGar === undefined) ? true : false;
			if(campo === 'S7_PorcGar') (mdg.PorcentajeGarantiaRequerido = mdg.TipoActivoGarantia.S7_PorcGar === "" || mdg.TipoActivoGarantia.S7_PorcGar === undefined) ? true : false;
		}
	});
}());