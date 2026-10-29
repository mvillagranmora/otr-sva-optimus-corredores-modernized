(function() {
	angular.module('ModuloContabilidad').controller('ModalAdministrarClaseEmisorCtrl',
		function ($scope,bcsComunes,servicioClaseEmisor) {
			/*********************************************
			 * VARIABLES Y CONTEXTOS
			 ********************************************/
			var mAdmClaseEmisor 			= this;
			mAdmClaseEmisor.idVentana 		= bcsComunes.ventanaWinId($scope);
			mAdmClaseEmisor.parametros 		= bcsComunes.ventanaParams(mAdmClaseEmisor.idVentana);
			mAdmClaseEmisor.datosAntes		= {};
			mAdmClaseEmisor.datosDespues	= {};
			mAdmClaseEmisor.cmbbx			= {};
			mAdmClaseEmisor.respuesta 		= {};
			mAdmClaseEmisor.modoEditar 		= false;
			
			mAdmClaseEmisor.cmbbx.TipoOperacion = {};
			mAdmClaseEmisor.cmbbx.IdClasificacionInstrumento = {};
			mAdmClaseEmisor.cmbbx.IdTipoBonoIRF = {};
			mAdmClaseEmisor.cmbbx.IdOrigenInstrumento = {};
			mAdmClaseEmisor.cmbbx.IdPlaza = {};
			mAdmClaseEmisor.cmbbx.IdCodigoIFRS = {};
			mAdmClaseEmisor.cmbbx.IdRelacionadoGrupo = {};
			mAdmClaseEmisor.cmbbx.IdPresenciaBursatil = {};
			mAdmClaseEmisor.cmbbx.IdFormadorMercado = {};
			mAdmClaseEmisor.cmbbx.IdPactoAndino = {};

			/*********************************************
			 * FUNCIONES
			 *********************************************/
			mAdmClaseEmisor.cargaDatosPantalla = function(){
				bcsComunes.ventanaAutoOverlay(mAdmClaseEmisor.idVentana,'claseEmisorComboOperacion input',3000);
				if(mAdmClaseEmisor.parametros && mAdmClaseEmisor.parametros.ClaseEmisor){
					mAdmClaseEmisor.modoEditar 			= true;
					mAdmClaseEmisor.datosAntes			= _.clone(mAdmClaseEmisor.parametros.ClaseEmisor);
//					mAdmClaseEmisor.datosAntes.IdEstado = (mAdmClaseEmisor.parametros.ClaseEmisor.IdEstado==='VIG')?true:false;
				}
				else {
					mAdmClaseEmisor.datosAntes.IdEstado = true;
				}
				mAdmClaseEmisor.datosDespues	= _.clone(mAdmClaseEmisor.datosAntes);
			};
			
			mAdmClaseEmisor.habilitarCombobox = function() {
				if (mAdmClaseEmisor.modoEditar) {
					mAdmClaseEmisor.cmbbx.TipoOperacion.activado = false;
					mAdmClaseEmisor.cmbbx.IdClasificacionInstrumento.activado = false;
					mAdmClaseEmisor.cmbbx.IdTipoBonoIRF.activado = false;
					mAdmClaseEmisor.cmbbx.IdOrigenInstrumento.activado = false;
					mAdmClaseEmisor.cmbbx.IdPlaza.activado = false;
					mAdmClaseEmisor.cmbbx.IdCodigoIFRS.activado = false;
					mAdmClaseEmisor.cmbbx.IdRelacionadoGrupo.activado = false;
					mAdmClaseEmisor.cmbbx.IdPresenciaBursatil.activado = false;
					mAdmClaseEmisor.cmbbx.IdFormadorMercado.activado = false;
					mAdmClaseEmisor.cmbbx.IdPactoAndino.activado = false;
				} else {
					mAdmClaseEmisor.cmbbx.TipoOperacion.activado = true;
					mAdmClaseEmisor.cmbbx.IdClasificacionInstrumento.activado = true;
					mAdmClaseEmisor.cmbbx.IdTipoBonoIRF.activado = true;
					mAdmClaseEmisor.cmbbx.IdOrigenInstrumento.activado = true;
					mAdmClaseEmisor.cmbbx.IdPlaza.activado = true;
					mAdmClaseEmisor.cmbbx.IdCodigoIFRS.activado = true;
					mAdmClaseEmisor.cmbbx.IdRelacionadoGrupo.activado = true;
					mAdmClaseEmisor.cmbbx.IdPresenciaBursatil.activado = true;
					mAdmClaseEmisor.cmbbx.IdFormadorMercado.activado = true;
					mAdmClaseEmisor.cmbbx.IdPactoAndino.activado = true;
				}
			};

			mAdmClaseEmisor.cancelar = function () {
				mAdmClaseEmisor.respuesta.aceptar = false;
				bcsComunes.ventanaData(mAdmClaseEmisor.idVentana, mAdmClaseEmisor.respuesta);
				bcsComunes.ventanaCerrar(mAdmClaseEmisor.idVentana);
			};
			
			mAdmClaseEmisor.preparaDatosGuardar = function(){
				var datosGuardar = {};
				datosGuardar.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa; 
				datosGuardar.p_IdUsuario 		= $scope.$root.SesionIdUsuario;
				datosGuardar.p_IdModulo 		= "CON";
				datosGuardar.p_UsaReglasNegocio = "N";
				datosGuardar.p_TipoOperacion	= mAdmClaseEmisor.datosDespues.TipoOperacion;
				datosGuardar.p_ClaseInstrumento	= mAdmClaseEmisor.datosDespues.IdClasificacionInstrumento;
				datosGuardar.p_TipoBonoIRF		= mAdmClaseEmisor.datosDespues.IdTipoBonoIRF || '';
				datosGuardar.p_OrigenInstrumento= mAdmClaseEmisor.datosDespues.IdOrigenInstrumento || '';
				datosGuardar.p_Plaza 			= mAdmClaseEmisor.datosDespues.IdPlaza || '';
				datosGuardar.p_CodigoIFRS 		= mAdmClaseEmisor.datosDespues.IdCodigoIFRS || '';
				datosGuardar.p_RelacionadoGrupo = mAdmClaseEmisor.datosDespues.IdRelacionadoGrupo || '';
				datosGuardar.p_PresenciaBursatil= mAdmClaseEmisor.datosDespues.IdPresenciaBursatil || '';
				datosGuardar.p_FormadorMercado 	= mAdmClaseEmisor.datosDespues.IdFormadorMercado || '';
				datosGuardar.p_PactoAndino 		= mAdmClaseEmisor.datosDespues.IdPactoAndino || '';
				datosGuardar.p_ClaseEmisor 		= mAdmClaseEmisor.datosDespues.ClaseEmisor;
				datosGuardar.p_LargoPlazo 		= mAdmClaseEmisor.datosDespues.LargoPlazo|| '';				
				return datosGuardar;
			};

			mAdmClaseEmisor.aceptar = function () {
				bcsComunes.ventanaOverlay(mAdmClaseEmisor.idVentana,true);
				mAdmClaseEmisor.respuesta.aceptar = true;
				var datosGuardar = mAdmClaseEmisor.preparaDatosGuardar();
				if(mAdmClaseEmisor.modoEditar) { //EDITAR
					servicioClaseEmisor.updateClaseEmisor(datosGuardar,function (result){
						if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID  > 0 && result.P_OUT_ERRORESCONTADOR===0){
							bcsComunes.msg("info","ClaseEmisor",'OK');
						} else {
							bcsComunes.msg("error","ClaseEmisor",result.CODIGO + ' : ' + result.PILA);
							console.log(result);
						}
						bcsComunes.ventanaData(mAdmClaseEmisor.idVentana, mAdmClaseEmisor.respuesta);
						bcsComunes.ventanaCerrar(mAdmClaseEmisor.idVentana);
					}, function(resError){
						bcsComunes.msg("error","ClaseEmisor",'Error');
						console.log(resError);
						bcsComunes.ventanaData(mAdmClaseEmisor.idVentana, mAdmClaseEmisor.respuesta);
						bcsComunes.ventanaCerrar(mAdmClaseEmisor.idVentana);
					});
				}
				else { // GUARDAR
					servicioClaseEmisor.saveClaseEmisor(datosGuardar,function (result){
						if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID  > 0 && result.P_OUT_ERRORESCONTADOR===0){
							bcsComunes.msg("info","ClaseEmisor",'OK');
						} else {
							bcsComunes.msg("error","CentroCosto",result.P_OUT_ERRORESDETALLE[0].Codigo );
							console.log(result);
						}
						bcsComunes.ventanaData(mAdmClaseEmisor.idVentana, mAdmClaseEmisor.respuesta);
						bcsComunes.ventanaCerrar(mAdmClaseEmisor.idVentana);
					}, function(resError){
						bcsComunes.msg("error","ClaseEmisor",'Error');
						console.log(resError);
						bcsComunes.ventanaData(mAdmClaseEmisor.idVentana, mAdmClaseEmisor.respuesta);
						bcsComunes.ventanaCerrar(mAdmClaseEmisor.idVentana);
					});
				}
			};

			/*********************************************
			 * COMBOBOX
			 *********************************************/


			/*********************************************
			 * GO!!!
			 *********************************************/
			mAdmClaseEmisor.cargaDatosPantalla();
			mAdmClaseEmisor.habilitarCombobox();

	});
}());