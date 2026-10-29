(function() {
	angular.module('ModuloMesa').controller('ModalEventoPosicionCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosMesa){
		
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var mEvPos 			= this;
		mEvPos.idVentana	= bcsComunes.ventanaWinId($scope);
		mEvPos.parametros	= bcsComunes.ventanaParams(mEvPos.idVentana);
		mEvPos.domVentana	= $("[ventanaid='"+mEvPos.idVentana+"']");
		
		var eventoSel = _.clone(mEvPos.parametros.EventoPosicionSel);
		
		mEvPos.win		= {};
		mEvPos.cmbbx	= {};
		mEvPos.fn		= {};
		
		mEvPos.datosEvento = {};
		mEvPos.datosEvento.IdEstadoDef = 'VIG';
		
		mEvPos.IdBotonAceptar = 'ModalEventoPosicion_aceptar' + mEvPos.parametros.AccionBoton;
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		if(eventoSel){
			bcsComunes.ventanaTitulo(mEvPos.idVentana, $translate.instant('UI_MODIFICAR_EVENTO_POSICION'));
			mEvPos.datosEvento.IdOperacion		= eventoSel.IdOperacion;
			mEvPos.datosEvento.IdOperacionDef	= eventoSel.IdOperacion;
			mEvPos.datosEvento.IdTipoEvento		= eventoSel.IdTipoEvento;
			mEvPos.datosEvento.IdTipoEventoDef	= eventoSel.IdTipoEvento;
			mEvPos.datosEvento.IndAccion		= eventoSel.IndAccion;
			mEvPos.datosEvento.IndAccionDef		= eventoSel.IndAccion;
			mEvPos.datosEvento.IdEstado			= eventoSel.IdEstado || 'VIG';
			mEvPos.datosEvento.IdEstadoDef		= eventoSel.IdEstado || 'VIG';
			
			mEvPos.win.modoEditar = true;
		}
		else{
			bcsComunes.ventanaTitulo(mEvPos.idVentana, $translate.instant('UI_CREAR_EVENTO_POSICION'));
			mEvPos.win.modoEditar = false;
		}
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		mEvPos.cmbbx.Operacion={
			filtros : {
				IdEstado : { field: "IdEstado",	operator: "eq", value: "VIG" },
			}
		};
		
		mEvPos.cmbbx.TipoEvento={
			filtros : {
				NombreTipoParametro : { field: "NombreTipoParametro",	operator: "eq", value: "TIPOEVENTO" },
				IdEmpresa			: { field: "IdEmpresa",				operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdEstado			: { field: "IdEstado",				operator: "eq", value: "VIG" },
			},
			configuracion : {
				placeholder: $translate.instant('UI_TIPO_EVENTO_POSICION'),
			},
		};
		
		mEvPos.cmbbx.IndicadorAccion={
			filtros : {
				NombreTipoParametro : { field: "NombreTipoParametro",	operator: "eq", value: "INDICADORACCION" },
				IdEmpresa			: { field: "IdEmpresa",				operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdEstado			: { field: "IdEstado",				operator: "eq", value: "VIG" },
			},
			configuracion : {
				placeholder: $translate.instant('UI_INDICADOR_ACCION'),
			},
		};
		
		mEvPos.cmbbx.Estado={
			filtros : {
				NombreEntidad : { field: "toupper(NombreEntidad)", operator: "eq", value: "POSICIONMRF" }
			}
		};
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		mEvPos.fn.cancelar = function () {
			bcsComunes.ventanaData(mEvPos.idVentana, false);
			bcsComunes.ventanaCerrar(mEvPos.idVentana);
		};
		
		
		mEvPos.fn.aceptar = function () {
			var datosGuardar = {};
			datosGuardar.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa; 
			datosGuardar.p_IdUsuario 		= $scope.$root.SesionIdUsuario;
			datosGuardar.p_IdModulo 		= "MES";
			datosGuardar.p_UsaReglasNegocio	= "N";
			
			if(eventoSel && eventoSel.IdEvento) { // EDITAR
				if(mEvPos.datosEvento.IdOperacion	=== eventoSel.IdOperacion && 
				   mEvPos.datosEvento.IdTipoEvento	=== eventoSel.IdTipoEvento && 
				   mEvPos.datosEvento.IndAccion		=== eventoSel.IndAccion && 
				   mEvPos.datosEvento.IdEstado		=== eventoSel.IdEstado){
					bcsComunes.msg("warning","UI_EVENTO_POSICION",$translate.instant("UI_SIN_MODIFICACIONES"));
					bcsComunes.ventanaData(mEvPos.idVentana, false);
					bcsComunes.ventanaCerrar(mEvPos.idVentana);
					return
				}
				
				datosGuardar.p_IdEvento		= eventoSel.IdEvento;
				datosGuardar.p_IdOperacion	= mEvPos.datosEvento.IdOperacion;
				datosGuardar.p_IdTipoEvento	= mEvPos.datosEvento.IdTipoEvento;
				datosGuardar.p_IndAccion	= mEvPos.datosEvento.IndAccion;
				datosGuardar.p_IdEstado		= mEvPos.datosEvento.IdEstado;
				
				datosGuardar.p_DetalleLog = [];
				if(mEvPos.datosEvento.IdOperacion !== eventoSel.IdOperacion)
					datosGuardar.p_DetalleLog.push({SubRegistro:'EVENTO '+eventoSel.IdEvento, CampoRegistro:'IdOperacion', ValorAntiguo:eventoSel.IdOperacion, ValorNuevo:mEvPos.datosEvento.IdOperacion });
				if(mEvPos.datosEvento.IdTipoEvento !== eventoSel.IdTipoEvento)
					datosGuardar.p_DetalleLog.push({SubRegistro:'EVENTO '+eventoSel.IdEvento, CampoRegistro:'IdTipoEvento', ValorAntiguo:eventoSel.IdTipoEvento, ValorNuevo:mEvPos.datosEvento.IdTipoEvento });
				if(mEvPos.datosEvento.IndAccion !== eventoSel.IndAccion)
					datosGuardar.p_DetalleLog.push({SubRegistro:'EVENTO '+eventoSel.IdEvento, CampoRegistro:'IndAccion', ValorAntiguo:eventoSel.IndAccion, ValorNuevo:mEvPos.datosEvento.IndAccion });
				if(mEvPos.datosEvento.IdEstado !== eventoSel.IdEstado)
					datosGuardar.p_DetalleLog.push({SubRegistro:'EVENTO '+eventoSel.IdEvento, CampoRegistro:'IdEstado', ValorAntiguo:eventoSel.IdEstado, ValorNuevo:mEvPos.datosEvento.IdEstado });
				
				bcsComunes.ventanaOverlay(mEvPos.idVentana,true);
				bcsServiciosMesa.ModificarEventoPosicion(datosGuardar,function (result){
					mEvPos.fn.evaluaRespuestaSP(result, 'UI_EVENTO_POSICION_MODIFICADO');
				}, function(resError){
					bcsComunes.msg("error","UI_EVENTO_POSICION",$translate.instant("UI_REVISE_LOG"));
					bcsComunes.ventanaOverlay(mEvPos.idVentana,false);
				});
			}
			else { // CREAR
				datosGuardar.p_IdOperacion	= mEvPos.datosEvento.IdOperacion;
				datosGuardar.p_IdTipoEvento	= mEvPos.datosEvento.IdTipoEvento;
				datosGuardar.p_IndAccion	= mEvPos.datosEvento.IndAccion;
				datosGuardar.p_IdEstado		= mEvPos.datosEvento.IdEstado;
				
				bcsComunes.ventanaOverlay(mEvPos.idVentana,true);
				bcsServiciosMesa.CrearEventoPosicion(datosGuardar,function (result){
					mEvPos.fn.evaluaRespuestaSP(result, 'UI_EVENTO_POSICION_CREADO');
				}, function(resError){
					bcsComunes.msg("error","UI_EVENTO_POSICION",$translate.instant("UI_REVISE_LOG"));
					bcsComunes.ventanaOverlay(mEvPos.idVentana,false);
				});
			}
		};
		
		
		mEvPos.fn.evaluaRespuestaSP = function(result, msj){
			if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID > 0 && result.P_OUT_ERRORESCONTADOR == 0 && result.P_OUT_ADVERTENCIASCONTADOR == 0){
				bcsComunes.msg("info","UI_EVENTO_POSICION",$translate.instant(msj));
			}
			else if(result.P_OUT_ADVERTENCIASCONTADOR !== undefined && result.P_OUT_ADVERTENCIASCONTADOR > 0){
				bcsComunes.msg("warning","UI_EVENTO_POSICION",result.P_OUT_ADVERTENCIASDETALLE[0].Codigo);
			}
			else if(result.P_OUT_ERRORESCONTADOR !== undefined && result.P_OUT_ERRORESCONTADOR > 0){
				bcsComunes.msg("error","UI_EVENTO_POSICION",result.P_OUT_ERRORESDETALLE[0].Codigo);
			}
			else bcsComunes.msg("error","UI_EVENTO_POSICION",$translate.instant("UI_REVISE_LOG"));
			console.log(result);
			
			bcsComunes.ventanaOverlay(mEvPos.idVentana,false);
			bcsComunes.ventanaData(mEvPos.idVentana, true);
			bcsComunes.ventanaCerrar(mEvPos.idVentana);
		};
	});
}());