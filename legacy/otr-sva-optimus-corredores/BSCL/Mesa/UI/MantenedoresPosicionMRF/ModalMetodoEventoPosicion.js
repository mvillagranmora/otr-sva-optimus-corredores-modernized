(function() {
	angular.module('ModuloMesa').controller('ModalMetodoEventoPosicionCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosMesa){
		
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var mMEP 		= this;
		mMEP.idVentana	= bcsComunes.ventanaWinId($scope);
		mMEP.parametros	= bcsComunes.ventanaParams(mMEP.idVentana);
		mMEP.domVentana	= $("[ventanaid='"+mMEP.idVentana+"']");
		
		var metEveSel = _.clone(mMEP.parametros.MetodoEventoPosicionSel);
		
		mMEP.win	= {};
		mMEP.cmbbx	= {};
		mMEP.fn		= {};
		
		mMEP.datosMetodoEvento = {};
		mMEP.datosMetodoEvento.IdEstadoDef = 'VIG';
		
		mMEP.IdBotonAceptar = 'ModalMetodoEventoPosicion_aceptar' + mMEP.parametros.AccionBoton;
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		if(metEveSel){
			bcsComunes.ventanaTitulo(mMEP.idVentana, $translate.instant('UI_MODIFICAR_METODO_EVENTO_POSICION'));
			mMEP.datosMetodoEvento.IdMetodo			= metEveSel.IdMetodo;
			mMEP.datosMetodoEvento.IdMetodoDef		= metEveSel.IdMetodo;
			mMEP.datosMetodoEvento.IdEvento			= metEveSel.IdEvento;
			mMEP.datosMetodoEvento.IdEventoDef		= metEveSel.IdEvento;
			mMEP.datosMetodoEvento.IdAgrupacion		= metEveSel.IdAgrupacion;
			mMEP.datosMetodoEvento.IdAgrupacionDef	= metEveSel.IdAgrupacion;
			mMEP.datosMetodoEvento.IdEstado			= metEveSel.IdEstado || 'VIG';
			mMEP.datosMetodoEvento.IdEstadoDef		= metEveSel.IdEstado || 'VIG';
			mMEP.datosMetodoEvento.OrdenItem		= metEveSel.OrdenItem;
			
			mMEP.win.modoEditar = true;
		}
		else{
			bcsComunes.ventanaTitulo(mMEP.idVentana, $translate.instant('UI_CREAR_METODO_EVENTO_POSICION'));
			mMEP.win.modoEditar = false;
		}
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		mMEP.cmbbx.PosicionMRF={
			filtros: {
				IdEstado : {field: "IdEstado", operator: "eq", value: 'VIG'}
			},
			configuracion : {
				placeholder: $translate.instant('UI_METODO_POSICION'),
			},
		};
		
		mMEP.cmbbx.Evento={
			filtros: {
				IdEstado : {field: "IdEstado", operator: "eq", value: 'VIG'}
			}
		};
		
		mMEP.cmbbx.Agrupacion={
			filtros : {
				NombreTipoParametro : { field: "NombreTipoParametro",	operator: "eq", value: "Grupo_PosMRF" },
				IdEmpresa			: { field: "IdEmpresa",				operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdEstado			: { field: "IdEstado",				operator: "eq", value: "VIG" },
			},
			configuracion : {
				placeholder: $translate.instant('UI_AGRUPACION'),
			},
		};
		
		mMEP.cmbbx.Estado={
			filtros : {
				NombreEntidad : { field: "toupper(NombreEntidad)", operator: "eq", value: "POSICIONMRF" }
			}
		};
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		mMEP.fn.cancelar = function () {
			bcsComunes.ventanaData(mMEP.idVentana, false);
			bcsComunes.ventanaCerrar(mMEP.idVentana);
		};
		
		
		mMEP.fn.aceptar = function () {
			var datosGuardar = {};
			datosGuardar.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa; 
			datosGuardar.p_IdUsuario 		= $scope.$root.SesionIdUsuario;
			datosGuardar.p_IdModulo 		= "MES";
			datosGuardar.p_UsaReglasNegocio	= "N";
			
			if(metEveSel && metEveSel.IdMatriz) { // EDITAR
				if(mMEP.datosMetodoEvento.IdMetodo		=== metEveSel.IdMetodo && 
				   mMEP.datosMetodoEvento.IdEvento		=== metEveSel.IdEvento && 
				   mMEP.datosMetodoEvento.IdAgrupacion	=== metEveSel.IdAgrupacion && 
				   mMEP.datosMetodoEvento.IdEstado		=== metEveSel.IdEstado && 
				   mMEP.datosMetodoEvento.OrdenItem		=== metEveSel.OrdenItem){
					bcsComunes.msg("warning","UI_METODO_EVENTO_POSICION",$translate.instant("UI_SIN_MODIFICACIONES"));
					bcsComunes.ventanaData(mMEP.idVentana, false);
					bcsComunes.ventanaCerrar(mMEP.idVentana);
					return
				}
				
				datosGuardar.p_IdMatriz		= metEveSel.IdMatriz;
				datosGuardar.p_IdMetodo		= mMEP.datosMetodoEvento.IdMetodo;
				datosGuardar.p_IdEvento		= mMEP.datosMetodoEvento.IdEvento;
				datosGuardar.p_IdAgrupacion	= mMEP.datosMetodoEvento.IdAgrupacion;
				datosGuardar.p_IdEstado		= mMEP.datosMetodoEvento.IdEstado;
				datosGuardar.p_OrdenItem	= mMEP.datosMetodoEvento.OrdenItem;
				
				datosGuardar.p_DetalleLog = [];
				if(mMEP.datosMetodoEvento.IdMetodo !== metEveSel.IdMetodo)
					datosGuardar.p_DetalleLog.push({SubRegistro:'METODOEVENTO '+metEveSel.IdMatriz, CampoRegistro:'IdMetodo', ValorAntiguo:metEveSel.IdMetodo, ValorNuevo:mMEP.datosMetodoEvento.IdMetodo });
				if(mMEP.datosMetodoEvento.IdEvento !== metEveSel.IdEvento)
					datosGuardar.p_DetalleLog.push({SubRegistro:'METODOEVENTO '+metEveSel.IdMatriz, CampoRegistro:'IdEvento', ValorAntiguo:metEveSel.IdEvento, ValorNuevo:mMEP.datosMetodoEvento.IdEvento });
				if(mMEP.datosMetodoEvento.IdAgrupacion !== metEveSel.IdAgrupacion)
					datosGuardar.p_DetalleLog.push({SubRegistro:'METODOEVENTO '+metEveSel.IdMatriz, CampoRegistro:'IdAgrupacion', ValorAntiguo:metEveSel.IdAgrupacion, ValorNuevo:mMEP.datosMetodoEvento.IdAgrupacion });
				if(mMEP.datosMetodoEvento.IdEstado !== metEveSel.IdEstado)
					datosGuardar.p_DetalleLog.push({SubRegistro:'METODOEVENTO '+metEveSel.IdMatriz, CampoRegistro:'IdEstado', ValorAntiguo:metEveSel.IdEstado, ValorNuevo:mMEP.datosMetodoEvento.IdEstado });
				if(mMEP.datosMetodoEvento.OrdenItem !== metEveSel.OrdenItem)
					datosGuardar.p_DetalleLog.push({SubRegistro:'METODOEVENTO '+metEveSel.IdMatriz, CampoRegistro:'OrdenItem', ValorAntiguo:metEveSel.OrdenItem, ValorNuevo:mMEP.datosMetodoEvento.OrdenItem });
				
				bcsComunes.ventanaOverlay(mMEP.idVentana,true);
				bcsServiciosMesa.ModificarMetodoEventoPosicion(datosGuardar,function (result){
					mMEP.fn.evaluaRespuestaSP(result, 'UI_METODO_EVENTO_POSICION_MODIFICADO');
				}, function(resError){
					bcsComunes.msg("error","UI_METODO_EVENTO_POSICION",$translate.instant("UI_REVISE_LOG"));
					bcsComunes.ventanaOverlay(mMEP.idVentana,false);
				});
			}
			else { // CREAR
				datosGuardar.p_IdMetodo		= mMEP.datosMetodoEvento.IdMetodo;
				datosGuardar.p_IdEvento		= mMEP.datosMetodoEvento.IdEvento;
				datosGuardar.p_IdAgrupacion	= mMEP.datosMetodoEvento.IdAgrupacion;
				datosGuardar.p_IdEstado		= mMEP.datosMetodoEvento.IdEstado;
				datosGuardar.p_OrdenItem	= mMEP.datosMetodoEvento.OrdenItem;
				
				bcsComunes.ventanaOverlay(mMEP.idVentana,true);
				bcsServiciosMesa.CrearMetodoEventoPosicion(datosGuardar,function (result){
					mMEP.fn.evaluaRespuestaSP(result, 'UI_METODO_EVENTO_POSICION_CREADO');
				}, function(resError){
					bcsComunes.msg("error","UI_METODO_EVENTO_POSICION",$translate.instant("UI_REVISE_LOG"));
					bcsComunes.ventanaOverlay(mMEP.idVentana,false);
				});
			}
		};
		
		
		mMEP.fn.evaluaRespuestaSP = function(result, msj){
			if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID > 0 && result.P_OUT_ERRORESCONTADOR == 0 && result.P_OUT_ADVERTENCIASCONTADOR == 0){
				bcsComunes.msg("info","UI_METODO_EVENTO_POSICION",$translate.instant(msj));
			}
			else if(result.P_OUT_ADVERTENCIASCONTADOR !== undefined && result.P_OUT_ADVERTENCIASCONTADOR > 0){
				bcsComunes.msg("warning","UI_METODO_EVENTO_POSICION",result.P_OUT_ADVERTENCIASDETALLE[0].Codigo);
			}
			else if(result.P_OUT_ERRORESCONTADOR !== undefined && result.P_OUT_ERRORESCONTADOR > 0){
				bcsComunes.msg("error","UI_METODO_EVENTO_POSICION",result.P_OUT_ERRORESDETALLE[0].Codigo);
			}
			else bcsComunes.msg("error","UI_METODO_EVENTO_POSICION",$translate.instant("UI_REVISE_LOG"));
			console.log(result);
			
			bcsComunes.ventanaOverlay(mMEP.idVentana,false);
			bcsComunes.ventanaData(mMEP.idVentana, true);
			bcsComunes.ventanaCerrar(mMEP.idVentana);
		};
	});
}());