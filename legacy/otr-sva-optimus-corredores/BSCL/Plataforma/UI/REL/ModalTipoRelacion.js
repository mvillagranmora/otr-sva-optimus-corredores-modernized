/********************************************************************
 * Purpose	: Modal Crear/Modificar Tipo Relación
 * Author	: TEAM SOCIO NEGOCIO
 * Date		: 2016
 * 
 * Purpose	: Se incorporan definiciones Optimus Perú
 * Author	: JGarces - BCS
 * Date		: 14/03/2017
 *******************************************************************/
(function() {
	angular.module('ModuloPlataforma').controller('ModalTipoRelacionCtrl',
	function($scope,$translate,bcsComunes,$uibModal,$resource) {	
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var modtr = this;	
		var idVentana = bcsComunes.ventanaWinId($scope); 
		var parametros = bcsComunes.ventanaParams(idVentana);
		modtr.TipoRelacion			= $.extend({},parametros.TipoRelSel);
		modtr.TipoRelacionOriginal	= $.extend({},parametros.TipoRelSel); 
		modtr.IdBotonAceptar = 'ModalTipoRelacion_aceptar' + parametros.AccionBoton;
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		if(parametros.TipoRelSel){ //modificar
			bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_MODIFICAR_TIPO_RELACION'));
			modtr.TipoRelacion = {};
			modtr.TipoRelacion.IdTipoRelacion				= parametros.TipoRelSel.IdTipoRelacion;
			modtr.TipoRelacion.NombreTipoRelacion			= parametros.TipoRelSel.NombreTipoRelacion;
			modtr.TipoRelacion.EsPersonaNatural				= parametros.TipoRelSel.EsPersonaNatural==='S'?true:false;
			modtr.TipoRelacion.EsIndefinida					= parametros.TipoRelSel.EsIndefinida==='S'?true:false;
			modtr.TipoRelacion.IdEstado						= parametros.TipoRelSel.IdEstado==='ACT'?true:false;
			modtr.TipoRelacion.UsaPorcentajeParticipacion 	= parametros.TipoRelSel.UsaPorcentajeParticipacion==='S'?true:false;
			modtr.TipoRelacion.RetiraDocumentos 			= parametros.TipoRelSel.RetiraDocumentos==='S'?true:false;
			modtr.TipoRelacion.UsaFinalidadRelacion 		= parametros.TipoRelSel.UsaFinalidadRelacion==='S'?true:false;
			modtr.TipoRelacion.UsaCondicionesRelacion 		= parametros.TipoRelSel.UsaCondicionesRelacion==='S'?true:false;
			modtr.TipoRelacion.UsaFirmaDocumento 			= parametros.TipoRelSel.UsaFirmaDocumento==='S'?true:false;
			modtr.TipoRelacion.AccesoOptimusCliente 		= parametros.TipoRelSel.AccesoOptimusCliente==='S'?true:false;
			modtr.TipoRelacion.UsaGradoVinculacionParental 	= parametros.TipoRelSel.UsaGradoVinculacionParental==='S'?true:false;
			
			modtr.TipoRelacionOriginal	= $.extend({},modtr.TipoRelacion);
			modtr.TipoRelacion.modoCrear= false;
		} else{ //crear
			bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_AGREGAR_TIPO_RELACION'));
			modtr.TipoRelacion = {};
			modtr.TipoRelacion.modoCrear = true;
		}
		
		modtr.aceptar = function(){
			if(!modtr.TipoRelacion.modoCrear){ //modificar
				var datos = {
					p_IdEmpresa						: $scope.$root.SesionIdEmpresa,
					p_IdUsuario						: $scope.$root.SesionIdUsuario,
					p_IdModulo						: 'REL',
					p_UsaReglasNegocio				: 'N',
					p_IdTipoRelacion 				: modtr.TipoRelacionOriginal.IdTipoRelacion,
					p_EsPersonaNatural 				: modtr.TipoRelacion.EsPersonaNatural?'S':'N',
					p_NombreTipoRelacion 			: modtr.TipoRelacionOriginal.NombreTipoRelacion,
					p_EsIndefinida 					: modtr.TipoRelacion.EsIndefinida?'S':'N',
					p_IdEstado 						: modtr.TipoRelacion.IdEstado?'ACT':'INA',
					p_UsaPorcentajeParticipacion 	: modtr.TipoRelacion.UsaPorcentajeParticipacion?'S':'N',
					p_RetiraDocumentos 				: modtr.TipoRelacion.RetiraDocumentos?'S':'N',
					p_UsaFinalidadRelacion 			: modtr.TipoRelacion.UsaFinalidadRelacion?'S':'N',
					p_UsaCondicionesRelacion 		: modtr.TipoRelacion.UsaCondicionesRelacion?'S':'N',
					p_UsaFirmaDocumento		 		: modtr.TipoRelacion.UsaFirmaDocumento?'S':'N',
					p_AccesoOptimusCliente	 		: modtr.TipoRelacion.AccesoOptimusCliente?'S':'N',
					p_UsaGradoVinculacionParental 	: modtr.TipoRelacion.UsaGradoVinculacionParental?'S':'N'
				};
				bcsComunes.ventanaOverlay(idVentana,true);
				modificarTipoRelacion(datos).then(
					function(result) {
						console.log(result);
						var msg = "";
						if (result.P_OUT_ADVERTENCIASCONTADOR)
							msg = $translate.instant('UI_OPERACION_TIENE_ADVERTENCIAS');
						else if (result.P_OUT_ERRORESCONTADOR)
							msg = $translate.instant('UI_ERROR_MODIFICAR_TIPO_RELACION');
						else bcsComunes.msg("info","info",$translate.instant('UI_TIPO_RELACION_MODIFICADA'));
						if(msg!="")
							bcsComunes.ventanaMsg({
								mensaje: msg,
								conflictos:util.parseServerResponse(result)
							},idVentana);
						bcsComunes.ventanaData(idVentana,true);
						bcsComunes.ventanaCerrar(idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);
					},
					function(result){
						console.log(result);
						bcsComunes.msg("error","error",$translate.instant('UI_ERROR_MODIFICAR_TIPO_RELACION'));
						bcsComunes.ventanaData(idVentana,true);
						bcsComunes.ventanaCerrar(idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);
					});
			}
			else{ //crear
				var datos = {
					p_IdEmpresa						: $scope.$root.SesionIdEmpresa,
					p_IdUsuario						: $scope.$root.SesionIdUsuario,
					p_IdModulo						: 'REL',
					p_UsaReglasNegocio				: 'N',
					p_IdTipoRelacion 				: modtr.TipoRelacion.IdTipoRelacion,
					p_EsPersonaNatural 				: modtr.TipoRelacion.EsPersonaNatural?'S':'N',
					p_NombreTipoRelacion 			: modtr.TipoRelacion.IdTipoRelacionObj.DescripcionTipoRelacion,
					p_EsIndefinida 					: modtr.TipoRelacion.EsIndefinida?'S':'N',
					p_IdEstado 						: modtr.TipoRelacion.IdEstado?'ACT':'INA',
					p_Admnistracion					: 'RELACIONADOS',
					p_UsaPorcentajeParticipacion 	: modtr.TipoRelacion.UsaPorcentajeParticipacion?'S':'N',
					p_RetiraDocumentos				: modtr.TipoRelacion.RetiraDocumentos?'S':'N',
					p_UsaFinalidadRelacion 			: modtr.TipoRelacion.UsaFinalidadRelacion?'S':'N',
					p_UsaCondicionesRelacion 		: modtr.TipoRelacion.UsaCondicionesRelacion?'S':'N',
					p_UsaFirmaDocumento		 		: modtr.TipoRelacion.UsaFirmaDocumento?'S':'N',
					p_AccesoOptimusCliente	 		: modtr.TipoRelacion.AccesoOptimusCliente?'S':'N',
					p_UsaGradoVinculacionParental 	: modtr.TipoRelacion.UsaGradoVinculacionParental?'S':'N'
				};
				bcsComunes.ventanaOverlay(idVentana,true);
				crearTipoRelacion(datos).then(
					function(result) {
						console.log(result);
						var msg = "";
						if (result.P_OUT_ADVERTENCIASCONTADOR)
							msg = $translate.instant('UI_OPERACION_TIENE_ADVERTENCIAS');
						else if (result.P_OUT_ERRORESCONTADOR)
							msg = $translate.instant('UI_ERROR_AGREGAR_TIPO_RELACION');
						else bcsComunes.msg("info","info",$translate.instant('UI_TIPO_RELACION_AGREGADA'));
						if(msg!="")
							bcsComunes.ventanaMsg({
								mensaje: msg,
								conflictos:util.parseServerResponse(result)
							},idVentana);
						bcsComunes.ventanaData(idVentana,true);
						bcsComunes.ventanaCerrar(idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);
					},
					function(result){
						console.log(result);
						bcsComunes.msg("error","error",$translate.instant('UI_ERROR_AGREGAR_TIPO_RELACION'));
						bcsComunes.ventanaData(idVentana,false);
						bcsComunes.ventanaCerrar(idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);
					}
				);
			}
		};
		modtr.cancelar = function() {
			bcsComunes.ventanaData(idVentana,false);
			bcsComunes.ventanaCerrar(idVentana);
		};
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		modtr.conf = {};
		modtr.conf.oConfiguracionTipoRelacionDestino={
			tipo : "combobox-tipo-relacion",
			configuracion : {},
			filtros : {
				Administracion : {"field" : "Administracion", "operator" : "eq", "value" : 'RELACIONADOS'},
				IdEmpresa : {"field" : "IdEmpresa", "operator" : "eq", "value" : $scope.$root.SesionIdEmpresa}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};	
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		var crearTipoRelacion = function(p_datos){
			var urlCrearTipoRelacion = "/BSCL/Plataforma/GWJAVA/PLA/call?t=180";
			var r = $resource("", {}, {'urlCrearTipoRelacion': {method:"POST", url:urlCrearTipoRelacion}});
			var post = r.urlCrearTipoRelacion(p_datos);
			return post.$promise;
	    };
		var modificarTipoRelacion = function(p_datos){
			var urlModificarTipoRelacion = "/BSCL/Plataforma/GWJAVA/PLA/call?t=181";
			var r = $resource("", {}, {'urlModificarTipoRelacion': {method:"POST", url:urlModificarTipoRelacion}});
			var post = r.urlModificarTipoRelacion(p_datos);
			return post.$promise;
	    };
	//---------------------------------- FIN CONTROLLER PRINCIPAL ----------------------------
	});
}());