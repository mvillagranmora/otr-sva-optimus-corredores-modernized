(function() {
	angular.module('ModuloPlataforma').controller('ModalRelacionInstrumentoCtrl',
		function ($scope,$translate,bcsComunes,servicioRelacionInstrumento,bcsServicioPlataforma) {
			/*********************************************
			 * VARIABLES Y CONTEXTOS
			 ********************************************/
			var mRelInst 			= this;
			mRelInst.idVentana 		= bcsComunes.ventanaWinId($scope);
			mRelInst.parametros 		= bcsComunes.ventanaParams(mRelInst.idVentana);
			mRelInst.datosAntes		= {};
			mRelInst.datosDespues	= {};
			mRelInst.cmbbx			= {};
			mRelInst.respuesta 		= {};
			mRelInst.modoEditar 		= false;
			mRelInst.filtroTipoInstrumento_instrumentoRelacionado = [];

			/*********************************************
			 * FUNCIONES
			 *********************************************/
			mRelInst.cargaDatosPantalla = function(){
				bcsComunes.ventanaAutoOverlay(mRelInst.idVentana,'bovedaComboPersona input',3000);
				if(mRelInst.parametros && mRelInst.parametros.RegRelacionInstrumento){
					mRelInst.modoEditar 			= true;
					mRelInst.datosAntes			= _.clone(mRelInst.parametros.RegRelacionInstrumento);
//					mRelInst.cmbbx.TipoRelacionInstrumento.idactual = mRelInst.parametros.RegRelacionInstrumento.IdTipoRelacionInstrumento;
					mRelInst.datosAntes.IdEstado = (mRelInst.parametros.RegRelacionInstrumento.IdEstado==='VIG')?true:false;
					mRelInst.filtroTipoInstrumento_instrumentoRelacionado = [mRelInst.parametros.RegRelacionInstrumento.IdTipoInstrumento, mRelInst.parametros.RegRelacionInstrumento.IdTipoInstrumentoRelacionado];
				}
				else {
					mRelInst.datosAntes.IdEstado = true;
				}
				mRelInst.datosDespues	= _.clone(mRelInst.datosAntes);
			};		
	
			mRelInst.cancelar = function () {
				mRelInst.respuesta.aceptar = false;
				bcsComunes.ventanaData(mRelInst.idVentana, mRelInst.respuesta);
				bcsComunes.ventanaCerrar(mRelInst.idVentana);
			};
			
			mRelInst.preparaDatosGuardar = function(){
				var datosGuardar = {};
				datosGuardar.p_IdEmpresa 									= $scope.$root.SesionIdEmpresa; 
				datosGuardar.p_IdUsuario 									= $scope.$root.SesionIdUsuario;
				datosGuardar.p_IdModulo 									= "PLA";
				datosGuardar.p_UsaReglasNegocio 							= "N";
				
				datosGuardar.p_IdRelacionInstrumento						= mRelInst.datosAntes.IdRelacionInstrumento;
				datosGuardar.p_IdTipoRelacionInstrumento					= mRelInst.cmbbx.TipoRelacionInstrumento.idactual;
				datosGuardar.p_IdClasificacionTipoInstrumento				= mRelInst.instrumentoObjeto.IdClasificacionTipoInstrumento;
				datosGuardar.p_IdTipoInstrumento							= mRelInst.instrumentoObjeto.IdTipoInstrumento;
				datosGuardar.p_IdInstrumento								= mRelInst.instrumentoObjeto.IdInstrumento;
				datosGuardar.p_Nemo											= mRelInst.instrumentoObjeto.Nemotecnico;
				
				datosGuardar.p_IdClasificacionTipoInstrumentoRelacionado	= mRelInst.instrumentoRelacionadoObjeto.IdClasificacionTipoInstrumento;
				datosGuardar.p_IdTipoInstrumentoRelacionado					= mRelInst.instrumentoRelacionadoObjeto.IdTipoInstrumento;
				datosGuardar.p_IdInstrumentoRelacionado						= mRelInst.instrumentoRelacionadoObjeto.IdInstrumento;
				datosGuardar.p_NemoRelacionado								= mRelInst.instrumentoRelacionadoObjeto.Nemotecnico;
				datosGuardar.p_FechaInicioRelacion							= util.dateSPdate(mRelInst.datosDespues.FechaInicioRelacion|| util.datedefault());
				datosGuardar.p_FechaFinRelacion								= util.dateSPdate(mRelInst.datosDespues.FechaFinRelacion || util.datedefault());
				datosGuardar.p_Descripcion									= mRelInst.datosDespues.Descripcion;
				
				datosGuardar.p_IdEstado	 									= (mRelInst.datosDespues.IdEstado===true)?'VIG':'BLO';
				return datosGuardar;
			};

			mRelInst.aceptar = function () {
				bcsComunes.ventanaOverlay(mRelInst.idVentana,true);
				mRelInst.respuesta.aceptar = true;
				var datosGuardar = mRelInst.preparaDatosGuardar();
				if(mRelInst.modoEditar) { //EDITAR
					servicioRelacionInstrumento.updateRelacionInstrumento(datosGuardar,function (result){
						var titulo = "";
						if (result.P_OUT_ERRORESCONTADOR !== undefined && result.P_OUT_ERRORESCONTADOR==0){
							
						} else {
							titulo = $translate.instant("UI_ERROR_MODIFICAR_RELACION_INSTRUMENTO");
						}
						var idMensaje = bcsComunes.ventanaMsg({
							mensaje: titulo,
							conflictos:util.parseServerResponse(result)
						},mRelInst.idVentana);

						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							bcsComunes.ventanaData(mRelInst.idVentana, mRelInst.respuesta);
							bcsComunes.ventanaCerrar(mRelInst.idVentana);
						});
						
					}, function(resError){
						bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_ERROR_MODIFICAR_RELACION_INSTRUMENTO"));
						bcsComunes.ventanaOverlay(mRelInst.idVentana,false);
					});
				}
				else { // GUARDAR NUEVO REGISTRO
					servicioRelacionInstrumento.saveRelacionInstrumento(datosGuardar,function (result){
						var titulo = "";
						if (result.P_OUT_ERRORESCONTADOR !== undefined && result.P_OUT_ERRORESCONTADOR==0){
							titulo = $translate.instant("UI_RELACION_INSTRUMENTO_CREADA");
						} else {
							titulo = $translate.instant("UI_ERROR_CREAR_RELACION_INSTRUMENTO");
						}
						var idMensaje = bcsComunes.ventanaMsg({
							mensaje: titulo,
							conflictos:util.parseServerResponse(result)
						},mRelInst.idVentana);
						
						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							bcsComunes.ventanaData(mRelInst.idVentana, mRelInst.respuesta);
							bcsComunes.ventanaCerrar(mRelInst.idVentana);
						});


					}, function(resError){ // ERROR
						bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_ERROR_CREAR_RELACION_INSTRUMENTO"));
						bcsComunes.ventanaOverlay(mRelInst.idVentana,false);
					});
				}
			};
			
			$scope.$watch('mRelInst.cmbbx.TipoRelacionInstrumento.Seleccionado',function(newval,oldval) {
				if (newval && newval.IdTipoRelacionInstrumento){
					if (newval.IdTipoRelacionInstrumento === 'OPSUAC'){
						mRelInst.filtroTipoInstrumento_instrumentoRelacionado[0] = '';
						mRelInst.filtroTipoInstrumento_instrumentoRelacionado[1] = 'OSA';
						if(mRelInst.instrumentoRelacionadoObjeto && mRelInst.instrumentoRelacionadoObjeto.IdTipoInstrumento !== 'OSA'){
							mRelInst.datosDespues.NemoRelacionado = "-1";
						}
					} else {
						mRelInst.filtroTipoInstrumento_instrumentoRelacionado[0] = mRelInst.cmbbx.TipoInstrumento.idactual;
						mRelInst.filtroTipoInstrumento_instrumentoRelacionado[1] = '';
					}
				}
			}, true);
			
			$scope.$watch('mRelInst.cmbbx.TipoInstrumento.Seleccionado',function(newval,oldval) {
				if (newval && newval.IdTipoInstrumento){
					if(mRelInst.cmbbx.TipoRelacionInstrumento.idactual === 'OPSUAC'){
						mRelInst.filtroTipoInstrumento_instrumentoRelacionado[0] = '';
					} else {
						mRelInst.filtroTipoInstrumento_instrumentoRelacionado[0] = mRelInst.cmbbx.TipoInstrumento.idactual;
					}
				}
			}, true);
			
			/*********************************************
			 * COMBOBOX
			 *********************************************/

			mRelInst.cmbbx.TipoRelacionInstrumento={
				tipo : "combobox-tipo-relacion-instrumento",
				configuracion : {},
				idactual : false,
				seleccionado : false,
				idDefecto : '',
				filtros : {
					TipoRelacionInstrumento : {field : "IdEstado", operator : "eq", value : "ACT"}
				}
			};
			
			mRelInst.cmbbx.TipoInstrumento={
				tipo : "combobox-tipo-instrumento",
				configuracion : {},
				idactual : false,
				seleccionado : false,
				idDefecto : '',
				filtros : {
					TipoRelacionInstrumento : {field : "IdClasificacionTipoInstrumento", operator : "eq", value : "RV"},
					IdEstado 				: {"field" : "IdEstado","operator": "eq", "value" : "VIG" }
				}
			};

			/*********************************************
			 * GO!!!
			 *********************************************/
			mRelInst.cargaDatosPantalla();
			bcsComunes.ventanaTamanoAutomatico(mRelInst.idVentana,15);
			
	});
}());