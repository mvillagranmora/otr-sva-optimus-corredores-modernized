(function() {
	angular.module('ModuloContabilidad').controller('ModalAdministrarCentroCostoCtrl',
		function ($scope,bcsComunes,servicioCentroCosto) {
			/*********************************************
			 * VARIABLES Y CONTEXTOS
			 ********************************************/
			var mAdmCentroCosto 			= this;
			mAdmCentroCosto.idVentana 		= bcsComunes.ventanaWinId($scope);
			mAdmCentroCosto.parametros 		= bcsComunes.ventanaParams(mAdmCentroCosto.idVentana);
			mAdmCentroCosto.datosAntes		= {};
			mAdmCentroCosto.datosDespues	= {};
			mAdmCentroCosto.cmbbx			= {};
			mAdmCentroCosto.respuesta 		= {};
			mAdmCentroCosto.modoEditar 		= false;

			/*********************************************
			 * FUNCIONES
			 *********************************************/
			mAdmCentroCosto.cargaDatosPantalla = function(){
				bcsComunes.ventanaAutoOverlay(mAdmCentroCosto.idVentana,'CentroCostoComboBolsa input',3000);
				if(mAdmCentroCosto.parametros && mAdmCentroCosto.parametros.CentroCosto){
					mAdmCentroCosto.modoEditar 			= true;
					mAdmCentroCosto.datosAntes			= _.clone(mAdmCentroCosto.parametros.CentroCosto);
//					mAdmCentroCosto.datosAntes.IdEstado = (mAdmCentroCosto.parametros.CentroCosto.IdEstado==='VIG')?true:false;
				}
				else {
					mAdmCentroCosto.datosAntes.IdEstado = true;
				}
				mAdmCentroCosto.datosDespues	= _.clone(mAdmCentroCosto.datosAntes);
			};		
	
			mAdmCentroCosto.cancelar = function () {
				mAdmCentroCosto.respuesta.aceptar = false;
				bcsComunes.ventanaData(mAdmCentroCosto.idVentana, mAdmCentroCosto.respuesta);
				bcsComunes.ventanaCerrar(mAdmCentroCosto.idVentana);
			};
			
			mAdmCentroCosto.preparaDatosGuardar = function(){
				var datosGuardar = {};
				datosGuardar.p_IdEmpresa 			= $scope.$root.SesionIdEmpresa; 
				datosGuardar.p_IdUsuario 			= $scope.$root.SesionIdUsuario;
				datosGuardar.p_IdModulo 			= "CON";
				datosGuardar.p_UsaReglasNegocio		= "N";
				datosGuardar.p_ProductoConcepto 	= mAdmCentroCosto.datosDespues.IdProductoConcepto;
				datosGuardar.p_Relacionado 		    = mAdmCentroCosto.datosDespues.IdRelacionado;
				datosGuardar.p_IdBolsa 			    = mAdmCentroCosto.datosDespues.IdBolsa;
				datosGuardar.p_TipoCentro 		    = mAdmCentroCosto.datosDespues.TipoCentro;
				datosGuardar.p_Business 		    = mAdmCentroCosto.datosDespues.Business;
				datosGuardar.p_Vehiculo 		    = mAdmCentroCosto.datosDespues.Vehiculo;
				datosGuardar.p_CentroBeneficio 	    = mAdmCentroCosto.datosDespues.CentroBeneficio;
				datosGuardar.p_Tipo 			    = mAdmCentroCosto.datosDespues.Tipo;
				return datosGuardar;
			};

			mAdmCentroCosto.aceptar = function () {
				bcsComunes.ventanaOverlay(mAdmCentroCosto.idVentana,true);
				mAdmCentroCosto.respuesta.aceptar = true;
				var datosGuardar = mAdmCentroCosto.preparaDatosGuardar();
				if(mAdmCentroCosto.modoEditar) { //EDITAR
					servicioCentroCosto.updateCentroCosto(datosGuardar,function (result){
						if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID  > 0 && result.P_OUT_ERRORESCONTADOR===0){
							bcsComunes.msg("info","CentroCosto",'OK');
						} else {
							bcsComunes.msg("error","CentroCosto",result.CODIGO + ' : ' + result.PILA);
							console.log(result);
						}
						bcsComunes.ventanaData(mAdmCentroCosto.idVentana, mAdmCentroCosto.respuesta);
						bcsComunes.ventanaCerrar(mAdmCentroCosto.idVentana);
					}, function(resError){
						bcsComunes.msg("error","CentroCosto",'Error');
						console.log(resError);
						bcsComunes.ventanaData(mAdmCentroCosto.idVentana, mAdmCentroCosto.respuesta);
						bcsComunes.ventanaCerrar(mAdmCentroCosto.idVentana);
					});
				}
				else { // GUARDAR
					servicioCentroCosto.saveCentroCosto(datosGuardar,function (result){
						if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID  > 0 && result.P_OUT_ERRORESCONTADOR===0){
							bcsComunes.msg("info","CentroCosto",'OK');
						} else {
							bcsComunes.msg("error","CentroCosto",result.P_OUT_ERRORESDETALLE[0].Codigo );
							console.log(result);
						}
						bcsComunes.ventanaData(mAdmCentroCosto.idVentana, mAdmCentroCosto.respuesta);
						bcsComunes.ventanaCerrar(mAdmCentroCosto.idVentana);
					}, function(resError){
						bcsComunes.msg("error","CentroCosto",'Error');
						console.log(resError);
						bcsComunes.ventanaData(mAdmCentroCosto.idVentana, mAdmCentroCosto.respuesta);
						bcsComunes.ventanaCerrar(mAdmCentroCosto.idVentana);
					});
				}
			};

			/*********************************************
			 * COMBOBOX
			 *********************************************/


			/*********************************************
			 * GO!!!
			 *********************************************/
			mAdmCentroCosto.cargaDatosPantalla();
			
	});
}());