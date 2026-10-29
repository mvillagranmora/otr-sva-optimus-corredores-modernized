(function() {
	angular.module('ModuloMesa').controller('ModalMetodoPosicionCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosMesa){
		
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var mMetPos 		= this;
		mMetPos.idVentana	= bcsComunes.ventanaWinId($scope);
		mMetPos.parametros	= bcsComunes.ventanaParams(mMetPos.idVentana);
		mMetPos.domVentana	= $("[ventanaid='"+mMetPos.idVentana+"']");
		
		var metodoSel = _.clone(mMetPos.parametros.MetodoPosicionSel);
		
		mMetPos.win		= {};
		mMetPos.cmbbx	= {};
		mMetPos.fn		= {};
		
		mMetPos.datosMetodo	= {};
		mMetPos.datosMetodo.IdEstadoDef = 'VIG';
		
		mMetPos.IdBotonAceptar = 'ModalMetodoPosicion_aceptar' + mMetPos.parametros.AccionBoton;
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		if(metodoSel){
			bcsComunes.ventanaTitulo(mMetPos.idVentana, $translate.instant('UI_MODIFICAR_METODO_POSICION'));
			mMetPos.datosMetodo.NombreMetodo		= metodoSel.NombreMetodo;
			mMetPos.datosMetodo.Glosa				= metodoSel.Glosa;
			mMetPos.datosMetodo.IdTipoReajuste		= metodoSel.IdTipoReajuste;
			mMetPos.datosMetodo.IdTipoReajusteDef	= metodoSel.IdTipoReajuste;
			mMetPos.datosMetodo.IdMesa				= metodoSel.IdMesa;
			mMetPos.datosMetodo.IdMesaDef			= metodoSel.IdMesa;
			mMetPos.datosMetodo.IdEstado			= metodoSel.IdEstado || 'VIG';
			mMetPos.datosMetodo.IdEstadoDef			= metodoSel.IdEstado || 'VIG';
			mMetPos.datosMetodo.esPosicionInicial	= (metodoSel.IdTipoPosicion == 'INICIAL' ? true : false);
			
			mMetPos.win.modoEditar = true;
		}
		else{
			bcsComunes.ventanaTitulo(mMetPos.idVentana, $translate.instant('UI_AGREGAR_METODO_POSICION'));
			mMetPos.win.modoEditar = false;
		}
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		mMetPos.cmbbx.TipoReajuste={
			filtros : {
				IdEstado : { field: "IdEstado", operator: "eq", value: "ACT" }
			}
		};
		
		mMetPos.cmbbx.Mesa = {
			filtros: {
				IdEmpresa		: { field: "IdEmpresa",			operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdEstado		: { field: "IdEstadoRegistro",	operator: "eq", value: "ACT" },
				IdTipoProducto	: { field: "IdTipoProducto",	operator: "eq", value: "MERF" }  // TODO revisar si es siempre MERF
			}
		};
		
		mMetPos.cmbbx.Estado={
			filtros : {
				NombreEntidad : { field: "toupper(NombreEntidad)", operator: "eq", value: "POSICIONMRF" }
			}
		};
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		mMetPos.fn.cancelar = function () {
			bcsComunes.ventanaData(mMetPos.idVentana, false);
			bcsComunes.ventanaCerrar(mMetPos.idVentana);
		};
		
		
		mMetPos.fn.aceptar = function () {
			var datosGuardar = {};
			datosGuardar.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa; 
			datosGuardar.p_IdUsuario 		= $scope.$root.SesionIdUsuario;
			datosGuardar.p_IdModulo 		= "MES";
			datosGuardar.p_UsaReglasNegocio	= "N";
			
			if(metodoSel && metodoSel.IdMetodo) { // EDITAR
				if(mMetPos.datosMetodo.NombreMetodo		=== metodoSel.NombreMetodo && 
				   mMetPos.datosMetodo.Glosa			=== metodoSel.Glosa && 
				   mMetPos.datosMetodo.IdTipoReajuste	=== metodoSel.IdTipoReajuste && 
				   mMetPos.datosMetodo.IdMesa			=== metodoSel.IdMesa && 
				   mMetPos.datosMetodo.IdEstado			=== metodoSel.IdEstado &&
				   mMetPos.datosMetodo.esPosicionInicial === (metodoSel.IdTipoPosicion == 'INICIAL' ? true : false)){
					bcsComunes.msg("warning","UI_METODO_POSICION",$translate.instant("UI_SIN_MODIFICACIONES"));
					bcsComunes.ventanaData(mMetPos.idVentana, false);
					bcsComunes.ventanaCerrar(mMetPos.idVentana);
					return
				}
				
				datosGuardar.p_IdMetodo			= metodoSel.IdMetodo;
				datosGuardar.p_NombreMetodo		= mMetPos.datosMetodo.NombreMetodo;
				datosGuardar.p_Glosa			= mMetPos.datosMetodo.Glosa;
				datosGuardar.p_IdTipoReajuste	= mMetPos.datosMetodo.IdTipoReajuste;
				datosGuardar.p_IdMesa			= mMetPos.datosMetodo.IdMesa;
				datosGuardar.p_IdEstado			= mMetPos.datosMetodo.IdEstado;
				datosGuardar.p_IdTipoPosicion	= (mMetPos.datosMetodo.esPosicionInicial ? "INICIAL" : "DIARIA");
				
				datosGuardar.p_DetalleLog = [];
				if(mMetPos.datosMetodo.NombreMetodo !== metodoSel.NombreMetodo)
					datosGuardar.p_DetalleLog.push({SubRegistro:'METODO '+metodoSel.IdMetodo, CampoRegistro:'NombreMetodo', ValorAntiguo:metodoSel.NombreMetodo, ValorNuevo:mMetPos.datosMetodo.NombreMetodo });
				if(mMetPos.datosMetodo.Glosa !== metodoSel.Glosa)
					datosGuardar.p_DetalleLog.push({SubRegistro:'METODO '+metodoSel.IdMetodo, CampoRegistro:'Glosa', ValorAntiguo:metodoSel.Glosa, ValorNuevo:mMetPos.datosMetodo.Glosa });
				if(mMetPos.datosMetodo.IdTipoReajuste !== metodoSel.IdTipoReajuste)
					datosGuardar.p_DetalleLog.push({SubRegistro:'METODO '+metodoSel.IdMetodo, CampoRegistro:'IdTipoReajuste', ValorAntiguo:metodoSel.IdTipoReajuste, ValorNuevo:mMetPos.datosMetodo.IdTipoReajuste });
				if(mMetPos.datosMetodo.IdMesa !== metodoSel.IdMesa)
					datosGuardar.p_DetalleLog.push({SubRegistro:'METODO '+metodoSel.IdMetodo, CampoRegistro:'IdMesa', ValorAntiguo:metodoSel.IdMesa, ValorNuevo:mMetPos.datosMetodo.IdMesa });
				if(mMetPos.datosMetodo.IdEstado !== metodoSel.IdEstado)
					datosGuardar.p_DetalleLog.push({SubRegistro:'METODO '+metodoSel.IdMetodo, CampoRegistro:'IdEstado', ValorAntiguo:metodoSel.IdEstado, ValorNuevo:mMetPos.datosMetodo.IdEstado });
				if(mMetPos.datosMetodo.esPosicionInicial !== (metodoSel.IdTipoPosicion == 'INICIAL' ? true : false))
					datosGuardar.p_DetalleLog.push({SubRegistro:'METODO '+metodoSel.IdMetodo, CampoRegistro:'esPosicionInicial', ValorAntiguo:metodoSel.IdTipoPosicion == 'INICIAL' ? true : false, ValorNuevo:mMetPos.datosMetodo.esPosicionInicial });
				
				bcsComunes.ventanaOverlay(mMetPos.idVentana,true);
				bcsServiciosMesa.ModificarMetodoPosicionMRF(datosGuardar,function (result){
					mMetPos.fn.evaluaRespuestaSP(result, 'UI_METODO_POSICION_MODIFICADO');
				}, function(resError){
					bcsComunes.msg("error","UI_METODO_POSICION",$translate.instant("UI_REVISE_LOG"));
					bcsComunes.ventanaOverlay(mMetPos.idVentana,false);
				});
			}
			else { // CREAR
				datosGuardar.p_NombreMetodo		= mMetPos.datosMetodo.NombreMetodo;
				datosGuardar.p_Glosa			= mMetPos.datosMetodo.Glosa;
				datosGuardar.p_IdTipoReajuste	= mMetPos.datosMetodo.IdTipoReajuste;
				datosGuardar.p_IdMesa			= mMetPos.datosMetodo.IdMesa;
				datosGuardar.p_IdEstado			= mMetPos.datosMetodo.IdEstado;
				datosGuardar.p_IdTipoPosicion	= mMetPos.datosMetodo.esPosicionInicial ? "INICIAL" : "DIARIA";
				
				bcsComunes.ventanaOverlay(mMetPos.idVentana,true);
				bcsServiciosMesa.CrearMetodoPosicionMRF(datosGuardar,function (result){
					mMetPos.fn.evaluaRespuestaSP(result, 'UI_METODO_POSICION_CREADO');
				}, function(resError){
					bcsComunes.msg("error","UI_METODO_POSICION",$translate.instant("UI_REVISE_LOG"));
					bcsComunes.ventanaOverlay(mMetPos.idVentana,false);
				});
			}
		};
		
		
		mMetPos.fn.evaluaRespuestaSP = function(result, msj){
			if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID > 0 && result.P_OUT_ERRORESCONTADOR == 0 && result.P_OUT_ADVERTENCIASCONTADOR == 0){
				bcsComunes.msg("info","UI_METODO_POSICION",$translate.instant(msj));
			}
			else if(result.P_OUT_ADVERTENCIASCONTADOR !== undefined && result.P_OUT_ADVERTENCIASCONTADOR > 0){
				bcsComunes.msg("warning","UI_METODO_POSICION",result.P_OUT_ADVERTENCIASDETALLE[0].Codigo);
			}
			else if(result.P_OUT_ERRORESCONTADOR !== undefined && result.P_OUT_ERRORESCONTADOR > 0){
				bcsComunes.msg("error","UI_METODO_POSICION",result.P_OUT_ERRORESDETALLE[0].Codigo);
			}
			else bcsComunes.msg("error","UI_METODO_POSICION",$translate.instant("UI_REVISE_LOG"));
			console.log(result);
			
			bcsComunes.ventanaOverlay(mMetPos.idVentana,false);
			bcsComunes.ventanaData(mMetPos.idVentana, true);
			bcsComunes.ventanaCerrar(mMetPos.idVentana);
		};
	});
}());