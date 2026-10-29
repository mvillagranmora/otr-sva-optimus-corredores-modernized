(function() {
	angular.module('ModuloPlataforma').controller('ModalMantenedorMotivosCtrl',
		function ($scope,bcsComunes,servicioMotivo) {
			/*********************************************
			 * VARIABLES Y CONTEXTOS
			 ********************************************/
			var mManMot 			= this;
			mManMot.idVentana 		= bcsComunes.ventanaWinId($scope);
			mManMot.parametros 		= bcsComunes.ventanaParams(mManMot.idVentana);
			mManMot.datosAntes		= {};
			mManMot.datosDespues	= {};
			mManMot.cmbbx			= {};
			mManMot.respuesta 		= {};
			mManMot.modoEditar 		= false;
			mManMot.filtrosEstado = {
					Entidad : {"field" : "NombreEntidad", "operator" : "eq", "value" : 'INSTRUCCION'}
				}

			/*********************************************
			 * FUNCIONES
			 *********************************************/
			mManMot.cargaDatosPantalla = function(){
				bcsComunes.ventanaAutoOverlay(mManMot.idVentana,'cargaModalMotivo input',1500);
				if(mManMot.parametros && mManMot.parametros.motivo){
					mManMot.modoEditar 				= true;
					mManMot.datosAntes				= _.clone(mManMot.parametros.motivo);
					mManMot.datosAntes.Motivo		= mManMot.parametros.motivo.Motivo;
					mManMot.datosAntes.IdEstado	 	= mManMot.parametros.motivo.IdEstado;
					mManMot.datosAntes.Estado	 	= mManMot.parametros.motivo.Estado;
					mManMot.datosAntes.Activo		= (mManMot.parametros.motivo.Activo==='S')?true:false;
					mManMot.datosAntes.tipoMotivo	= mManMot.parametros.motivo.Tipo;
				}
				else {
					mManMot.datosAntes.tipoMotivo	= 'M';
					mManMot.datosAntes.Activo		= true;
				}
				mManMot.datosDespues	= _.clone(mManMot.datosAntes);
			};		
	
			mManMot.cancelar = function () {
				mManMot.respuesta.aceptar = false;
				bcsComunes.ventanaData(mManMot.idVentana, mManMot.respuesta);
				bcsComunes.ventanaCerrar(mManMot.idVentana);
			};
			
			mManMot.preparaDatosGuardar = function(){
				var datosGuardar = {};
				datosGuardar.p_IdEmpresa 					= $scope.$root.SesionIdEmpresa; 
				datosGuardar.p_IdUsuario 					= $scope.$root.SesionIdUsuario;
				datosGuardar.p_IdModulo 					= "PLA";
				datosGuardar.p_Motivos						= []
				var motivoIN 								= {};
				if(mManMot.modoEditar){
					datosGuardar.p_Accion					= 'UPDATE';
					
					motivoIN.p_IdMotivo						= mManMot.datosAntes.IdMotivo;
					motivoIN.p_IdEstado						= mManMot.datosDespues.IdEstado;
					motivoIN.p_Motivo						= mManMot.datosDespues.Motivo;
					motivoIN.p_IdTipoCanalIngreso			= 'SIS';
					motivoIN.p_Activo	 					= (mManMot.datosDespues.Activo===true)?'S':'N';
				}
				else{
					motivoIN.p_IdEstado						= mManMot.datosDespues.IdEstado;
					motivoIN.p_Motivo						= mManMot.datosDespues.Motivo;
					motivoIN.p_IdTipoCanalIngreso			= 'SIS';
					motivoIN.p_Activo	 					= (mManMot.datosDespues.Activo===true)?'S':'N';
				}
				datosGuardar.p_Motivos.push(motivoIN);
				//
				return datosGuardar;
			};

			mManMot.aceptar = function () {
				bcsComunes.ventanaOverlay(mManMot.idVentana,true);
				mManMot.respuesta.aceptar = true;
				var datosGuardar = mManMot.preparaDatosGuardar();
				if(mManMot.modoEditar) { //EDITAR
					servicioMotivo.updateMotivo(datosGuardar,function (result){
						if (result.P_OUT_ERRORESCONTADOR===0){
							bcsComunes.msg("info","MOTIVO",'Motivo modificado correctamente');
						} else {
							bcsComunes.msg("error","MOTIVO",result.P_OUT_MENSAJES[0].descripcion);
							console.log(result);
						}
						bcsComunes.ventanaData(mManMot.idVentana, mManMot.respuesta);
						bcsComunes.ventanaCerrar(mManMot.idVentana);
					}, function(resError){
						bcsComunes.msg("error","MOTIVO",'Error');
						console.log(resError);
						bcsComunes.ventanaData(mManMot.idVentana, mManMot.respuesta);
						bcsComunes.ventanaCerrar(mManMot.idVentana);
					});
				}
				else { // CREAR
					servicioMotivo.saveMotivo(datosGuardar,function (result){
						if (result.P_OUT_MOTIVOS !== undefined && result.P_OUT_MOTIVOS.length  > 0 && result.P_OUT_ERRORESCONTADOR===0){
							bcsComunes.msg("info","MOTIVO",'Motivo ingresado correctamente');
						} else {
							bcsComunes.msg("error","MOTIVO",result.P_OUT_MENSAJES[0].descripcion);
							console.log(result);
						}
						bcsComunes.ventanaData(mManMot.idVentana, mManMot.respuesta);
						bcsComunes.ventanaCerrar(mManMot.idVentana);
					}, function(resError){
						bcsComunes.msg("error","MOTIVO",'Error');
						console.log(resError);
						bcsComunes.ventanaData(mManMot.idVentana, mManMot.respuesta);
						bcsComunes.ventanaCerrar(mManMot.idVentana);
					});
				}
			};

			/*********************************************
			 * COMBOBOX
			 *********************************************/

			/*********************************************
			 * GO
			 *********************************************/
			mManMot.cargaDatosPantalla();
			
	});
}());