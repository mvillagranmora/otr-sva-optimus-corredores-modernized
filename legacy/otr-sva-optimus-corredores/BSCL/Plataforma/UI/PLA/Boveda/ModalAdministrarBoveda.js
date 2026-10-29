(function() {
	angular.module('ModuloPlataforma').controller('ModalAdministrarBovedaCtrl',
		function ($scope,bcsComunes,servicioBoveda) {
			/*********************************************
			 * VARIABLES Y CONTEXTOS
			 ********************************************/
			var mAdmBvd 			= this;
			mAdmBvd.idVentana 		= bcsComunes.ventanaWinId($scope);
			mAdmBvd.parametros 		= bcsComunes.ventanaParams(mAdmBvd.idVentana);
			mAdmBvd.datosAntes		= {};
			mAdmBvd.datosDespues	= {};
			mAdmBvd.cmbbx			= {};
			mAdmBvd.respuesta 		= {};
			mAdmBvd.modoEditar 		= false;

			/*********************************************
			 * FUNCIONES
			 *********************************************/
			mAdmBvd.cargaDatosPantalla = function(){
				bcsComunes.ventanaAutoOverlay(mAdmBvd.idVentana,'bovedaComboPersona input',3000);
				if(mAdmBvd.parametros && mAdmBvd.parametros.boveda){
					mAdmBvd.modoEditar 			= true;
					mAdmBvd.datosAntes			= _.clone(mAdmBvd.parametros.boveda);
					mAdmBvd.datosAntes.IdEstado = (mAdmBvd.parametros.boveda.IdEstado==='VIG')?true:false;
				}
				else {
					mAdmBvd.datosAntes.IdEstado = true;
				}
				mAdmBvd.datosDespues	= _.clone(mAdmBvd.datosAntes);
			};		
	
			mAdmBvd.cancelar = function () {
				mAdmBvd.respuesta.aceptar = false;
				bcsComunes.ventanaData(mAdmBvd.idVentana, mAdmBvd.respuesta);
				bcsComunes.ventanaCerrar(mAdmBvd.idVentana);
			};
			
			mAdmBvd.preparaDatosGuardar = function(){
				var datosGuardar = {};
				datosGuardar.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa; 
				datosGuardar.p_IdUsuario 		= $scope.$root.SesionIdUsuario;
				datosGuardar.p_IdModulo 		= "PLA";
				datosGuardar.p_UsaReglasNegocio = "N";
				datosGuardar.p_IdBoveda			= mAdmBvd.datosDespues.IdBoveda;
				datosGuardar.p_Codigo	 		= mAdmBvd.datosDespues.Codigo;
				datosGuardar.p_Nombre	 		= mAdmBvd.datosDespues.Nombre;
				datosGuardar.p_IdEstado	 		= (mAdmBvd.datosDespues.IdEstado===true)?'VIG':'BLO';
				datosGuardar.p_IdSocioDeNegocio	= mAdmBvd.datosDespues.IdSocioDeNegocio || 0;
				return datosGuardar;
			};

			mAdmBvd.aceptar = function () {
				bcsComunes.ventanaOverlay(mAdmBvd.idVentana,true);
				mAdmBvd.respuesta.aceptar = true;
				var datosGuardar = mAdmBvd.preparaDatosGuardar();
				if(mAdmBvd.modoEditar) { //EDITAR
					servicioBoveda.updateBoveda(datosGuardar,function (result){
						if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID  > 0 && result.P_OUT_ERRORESCONTADOR===0){
							bcsComunes.msg("info","BOVEDA",'OK');
						} else {
							bcsComunes.msg("error","BOVEDA",result.CODIGO + ' : ' + result.PILA);
							console.log(result);
						}
						bcsComunes.ventanaData(mAdmBvd.idVentana, mAdmBvd.respuesta);
						bcsComunes.ventanaCerrar(mAdmBvd.idVentana);
					}, function(resError){
						bcsComunes.msg("error","BOVEDA",'Error');
						console.log(resError);
						bcsComunes.ventanaData(mAdmBvd.idVentana, mAdmBvd.respuesta);
						bcsComunes.ventanaCerrar(mAdmBvd.idVentana);
					});
				}
				else { // GUARDAR
					servicioBoveda.saveBoveda(datosGuardar,function (result){
						if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID  > 0 && result.P_OUT_ERRORESCONTADOR===0){
							bcsComunes.msg("info","BOVEDA",'OK');
						} else {
							bcsComunes.msg("error","BOVEDA",result.CODIGO + ' : ' + result.PILA);
							console.log(result);
						}
						bcsComunes.ventanaData(mAdmBvd.idVentana, mAdmBvd.respuesta);
						bcsComunes.ventanaCerrar(mAdmBvd.idVentana);
					}, function(resError){
						bcsComunes.msg("error","BOVEDA",'Error');
						console.log(resError);
						bcsComunes.ventanaData(mAdmBvd.idVentana, mAdmBvd.respuesta);
						bcsComunes.ventanaCerrar(mAdmBvd.idVentana);
					});
				}
			};

			/*********************************************
			 * COMBOBOX
			 *********************************************/

			mAdmBvd.cmbbx.SocioDeNegocio={
				filtros : {
					TipoPersona	: {field : "CodigoTipoPersona", operator : "eq", value : "J"}
				}
			};

			/*********************************************
			 * GO!!!
			 *********************************************/
			mAdmBvd.cargaDatosPantalla();
			
	});
}());