(function() {
	angular.module('ModuloMesa').controller('ModalTipoOperacionPosicionCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosMesa){
		
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var mTOP 		= this;
		mTOP.idVentana	= bcsComunes.ventanaWinId($scope);
		mTOP.parametros	= bcsComunes.ventanaParams(mTOP.idVentana);
		mTOP.domVentana	= $("[ventanaid='"+mTOP.idVentana+"']");
		
		var tipOpePosSel = _.clone(mTOP.parametros.TipoOperacionPosicionSel);
		
		mTOP.win	= {};
		mTOP.cmbbx	= {};
		mTOP.fn		= {};
		
		mTOP.datosTipoOpePos = {};
		mTOP.datosTipoOpePos.IdEstadoDef = 'VIG';
		
		mTOP.IdBotonAceptar = 'ModalTipoOperacionPosicion_aceptar' + mTOP.parametros.AccionBoton;
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		if(tipOpePosSel){
			bcsComunes.ventanaTitulo(mTOP.idVentana, $translate.instant('UI_MODIFICAR_TIPO_OPERACION_POSICION'));
			mTOP.datosTipoOpePos.IdTipoProducto		= tipOpePosSel.IdTipoProducto;
			mTOP.datosTipoOpePos.IdTipoProductoDef	= tipOpePosSel.IdTipoProducto;
			mTOP.datosTipoOpePos.IdTipoOperacion	= tipOpePosSel.IdTipoOperacion;
			mTOP.datosTipoOpePos.IdTipoOperacionDef	= tipOpePosSel.IdTipoOperacion;
			mTOP.datosTipoOpePos.IdTipoComprobante	 =tipOpePosSel.IdTipoComprobante;
			mTOP.datosTipoOpePos.IdTipoComprobanteDef=tipOpePosSel.IdTipoComprobante;
			mTOP.datosTipoOpePos.NombreEvento		= tipOpePosSel.NombreEvento;
			mTOP.datosTipoOpePos.IdTipoEstado		= tipOpePosSel.IdTipoEstado;
			mTOP.datosTipoOpePos.IdTipoEstadoDef	= tipOpePosSel.IdTipoEstado;
			mTOP.datosTipoOpePos.IdEstado			= tipOpePosSel.IdEstado || 'VIG';
			mTOP.datosTipoOpePos.IdEstadoDef		= tipOpePosSel.IdEstado || 'VIG';
			
			mTOP.win.modoEditar = true;
		}
		else{
			bcsComunes.ventanaTitulo(mTOP.idVentana, $translate.instant('UI_CREAR_TIPO_OPERACION_POSICION'));
			mTOP.win.modoEditar = false;
		}
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		mTOP.cmbbx.TipoProducto={
			filtros : {
				IdEstado : { field: "IdEstado",	operator: "eq" , value: "VIG" }
			}
		};
		
		mTOP.cmbbx.TipoOperacion = {
			filtros: {
				IdTipoProducto : { field: "IdTipoProducto",	operator: "eq", value: "-1" }
			}
		};
		
		mTOP.cmbbx.TipoComprobante={
			filtros : {
				IdEstado : { field: "IdEstado",	operator: "eq" , value: "VIG" }
			}
		};
		
		mTOP.cmbbx.TipoEstado={
			filtros : {
				IdEstado : { field: "IdEstado",	operator: "eq" , value: "VIG" }
			}
		};
		
		mTOP.cmbbx.Estado={
			filtros : {
				NombreEntidad : { field: "toupper(NombreEntidad)", operator: "eq", value: "POSICIONMRF" }
			}
		};
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		mTOP.fn.cancelar = function () {
			bcsComunes.ventanaData(mTOP.idVentana, false);
			bcsComunes.ventanaCerrar(mTOP.idVentana);
		};
		
		
		mTOP.fn.aceptar = function () {
			var datosGuardar = {};
			datosGuardar.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa; 
			datosGuardar.p_IdUsuario 		= $scope.$root.SesionIdUsuario;
			datosGuardar.p_IdModulo 		= "MES";
			datosGuardar.p_UsaReglasNegocio	= "N";
			
			if(tipOpePosSel && tipOpePosSel.IdOperacion) { // EDITAR
				if(mTOP.datosTipoOpePos.IdTipoProducto		=== tipOpePosSel.IdTipoProducto && 
				   mTOP.datosTipoOpePos.IdTipoOperacion		=== tipOpePosSel.IdTipoOperacion && 
				   mTOP.datosTipoOpePos.IdTipoComprobante	=== tipOpePosSel.IdTipoComprobante && 
				   mTOP.datosTipoOpePos.NombreEvento		=== tipOpePosSel.NombreEvento && 
				   mTOP.datosTipoOpePos.IdTipoEstado		=== tipOpePosSel.IdTipoEstado && 
				   mTOP.datosTipoOpePos.IdEstado			=== tipOpePosSel.IdEstado){
					bcsComunes.msg("warning","UI_TIPO_OPERACION_POSICION",$translate.instant("UI_SIN_MODIFICACIONES"));
					bcsComunes.ventanaData(mTOP.idVentana, false);
					bcsComunes.ventanaCerrar(mTOP.idVentana);
					return
				}
				
				datosGuardar.p_IdOperacion		= tipOpePosSel.IdOperacion;
				datosGuardar.p_IdTipoProducto	= mTOP.datosTipoOpePos.IdTipoProducto;
				datosGuardar.p_IdTipoOperacion	= mTOP.datosTipoOpePos.IdTipoOperacion;
				datosGuardar.p_IdTipoComprobante= mTOP.datosTipoOpePos.IdTipoComprobante;
				datosGuardar.p_NombreEvento		= mTOP.datosTipoOpePos.NombreEvento;
				datosGuardar.p_IdTipoEstado		= mTOP.datosTipoOpePos.IdTipoEstado;
				datosGuardar.p_IdEstado			= mTOP.datosTipoOpePos.IdEstado;
				
				datosGuardar.p_DetalleLog = [];
				if(mTOP.datosTipoOpePos.IdTipoProducto !== tipOpePosSel.IdTipoProducto)
					datosGuardar.p_DetalleLog.push({SubRegistro:'TIPOOPERACION '+tipOpePosSel.IdOperacion, CampoRegistro:'IdTipoProducto', ValorAntiguo:tipOpePosSel.IdTipoProducto, ValorNuevo:mTOP.datosTipoOpePos.IdTipoProducto });
				if(mTOP.datosTipoOpePos.IdTipoOperacion !== tipOpePosSel.IdTipoOperacion)
					datosGuardar.p_DetalleLog.push({SubRegistro:'TIPOOPERACION '+tipOpePosSel.IdOperacion, CampoRegistro:'IdTipoOperacion', ValorAntiguo:tipOpePosSel.IdTipoOperacion, ValorNuevo:mTOP.datosTipoOpePos.IdTipoOperacion });
				if(mTOP.datosTipoOpePos.IdTipoComprobante !== tipOpePosSel.IdTipoComprobante)
					datosGuardar.p_DetalleLog.push({SubRegistro:'TIPOOPERACION '+tipOpePosSel.IdOperacion, CampoRegistro:'IdTipoComprobante', ValorAntiguo:tipOpePosSel.IdTipoComprobante, ValorNuevo:mTOP.datosTipoOpePos.IdTipoComprobante });
				if(mTOP.datosTipoOpePos.NombreEvento !== tipOpePosSel.NombreEvento)
					datosGuardar.p_DetalleLog.push({SubRegistro:'TIPOOPERACION '+tipOpePosSel.IdOperacion, CampoRegistro:'NombreEvento', ValorAntiguo:tipOpePosSel.NombreEvento, ValorNuevo:mTOP.datosTipoOpePos.NombreEvento });
				if(mTOP.datosTipoOpePos.IdTipoEstado !== tipOpePosSel.IdTipoEstado)
					datosGuardar.p_DetalleLog.push({SubRegistro:'TIPOOPERACION '+tipOpePosSel.IdOperacion, CampoRegistro:'IdTipoEstado', ValorAntiguo:tipOpePosSel.IdTipoEstado, ValorNuevo:mTOP.datosTipoOpePos.IdTipoEstado });
				if(mTOP.datosTipoOpePos.IdEstado !== tipOpePosSel.IdEstado)
					datosGuardar.p_DetalleLog.push({SubRegistro:'TIPOOPERACION '+tipOpePosSel.IdOperacion, CampoRegistro:'IdEstado', ValorAntiguo:tipOpePosSel.IdEstado, ValorNuevo:mTOP.datosTipoOpePos.IdEstado });
				
				bcsComunes.ventanaOverlay(mTOP.idVentana,true);
				bcsServiciosMesa.ModificarTipoOperacionPosicion(datosGuardar,function (result){
					mTOP.fn.evaluaRespuestaSP(result, 'UI_TIPO_OPERACION_POSICION_MODIFICADO');
				}, function(resError){
					bcsComunes.msg("error","UI_TIPO_OPERACION_POSICION",$translate.instant("UI_REVISE_LOG"));
					bcsComunes.ventanaOverlay(mTOP.idVentana,false);
				});
			}
			else { // CREAR
				datosGuardar.p_IdTipoProducto	= mTOP.datosTipoOpePos.IdTipoProducto;
				datosGuardar.p_IdTipoOperacion	= mTOP.datosTipoOpePos.IdTipoOperacion;
				datosGuardar.p_IdTipoComprobante= mTOP.datosTipoOpePos.IdTipoComprobante;
				datosGuardar.p_NombreEvento		= mTOP.datosTipoOpePos.NombreEvento;
				datosGuardar.p_IdTipoEstado		= mTOP.datosTipoOpePos.IdTipoEstado;
				datosGuardar.p_IdEstado			= mTOP.datosTipoOpePos.IdEstado;
				
				bcsComunes.ventanaOverlay(mTOP.idVentana,true);
				bcsServiciosMesa.CrearTipoOperacionPosicion(datosGuardar,function (result){
					mTOP.fn.evaluaRespuestaSP(result, 'UI_TIPO_OPERACION_POSICION_CREADO');
				}, function(resError){
					bcsComunes.msg("error","UI_TIPO_OPERACION_POSICION",$translate.instant("UI_REVISE_LOG"));
					bcsComunes.ventanaOverlay(mTOP.idVentana,false);
				});
			}
		};
		
		
		mTOP.fn.evaluaRespuestaSP = function(result, msj){
			if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID > 0 && result.P_OUT_ERRORESCONTADOR == 0 && result.P_OUT_ADVERTENCIASCONTADOR == 0){
				bcsComunes.msg("info","UI_TIPO_OPERACION_POSICION",$translate.instant(msj));
			}
			else if(result.P_OUT_ADVERTENCIASCONTADOR !== undefined && result.P_OUT_ADVERTENCIASCONTADOR > 0){
				bcsComunes.msg("warning","UI_TIPO_OPERACION_POSICION",result.P_OUT_ADVERTENCIASDETALLE[0].Codigo);
			}
			else if(result.P_OUT_ERRORESCONTADOR !== undefined && result.P_OUT_ERRORESCONTADOR > 0){
				bcsComunes.msg("error","UI_TIPO_OPERACION_POSICION",result.P_OUT_ERRORESDETALLE[0].Codigo);
			}
			else bcsComunes.msg("error","UI_TIPO_OPERACION_POSICION",$translate.instant("UI_REVISE_LOG"));
			console.log(result);
			
			bcsComunes.ventanaOverlay(mTOP.idVentana,false);
			bcsComunes.ventanaData(mTOP.idVentana, true);
			bcsComunes.ventanaCerrar(mTOP.idVentana);
		};
	});
}());