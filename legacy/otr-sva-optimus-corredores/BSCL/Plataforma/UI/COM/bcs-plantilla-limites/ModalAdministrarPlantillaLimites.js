(function() {
	angular.module('ModuloPlataforma').controller('ModalAdministrarPlantillaLimitesCtrl',
		function ($scope,bcsComunes,servicioPlantillaLimites,$translate) {
			/*********************************************
			 * VARIABLES Y CONTEXTOS
			 ********************************************/
			var maPLim 			= this;
			maPLim.idVentana 	= bcsComunes.ventanaWinId($scope);
			maPLim.parametros 	= bcsComunes.ventanaParams(maPLim.idVentana);
			maPLim.domVentana	= $("[ventanaid='"+maPLim.idVentana+"']");
			
			maPLim.cmbbx		= {};
			maPLim.win			= {};
			maPLim.respuesta 	= {};
			
			maPLim.datosAntes 	= {};
			maPLim.datosDespues	= {};
			
			/*********************************************
			 * FUNCIONES
			 *********************************************/
			maPLim.cargaDatosPantalla = function(){
				if(maPLim.parametros.plantilla) {
					maPLim.datosAntes = _.clone(maPLim.parametros.plantilla);
					maPLim.datosDespues = _.clone(maPLim.datosAntes);
					maPLim.win.modoEditar = true;
				}
				else {
					maPLim.datosDespues.IdEstado = true;
					maPLim.win.modoEditar = false;
				}
			};		
	
			maPLim.cancelar = function () {
				maPLim.respuesta.aceptar = false;
				bcsComunes.ventanaData(maPLim.idVentana, maPLim.respuesta);
				bcsComunes.ventanaCerrar(maPLim.idVentana);
			};

			maPLim.guardar = function () {
				maPLim.respuesta.IdPlantillaLimite = maPLim.datosDespues.IdPlantillaLimite;
				if(maPLim.datosAntes.IdPlantillaLimite) { // EDITAR
					var datosGuardar = maPLim.preparaDatosModificar();
					if(datosGuardar) {
						bcsComunes.ventanaOverlay(maPLim.idVentana,true);
						servicioPlantillaLimites.updatePlantillaLimites(datosGuardar,function (result){
							maPLim.evaluaRespuestaSP(result, 'UI_PLANTILLA_LIMITES_MODIFICADA');
						}, function(resError){
							bcsComunes.msg("error","PLANTILLA LIMITES",$translate.instant("UI_REVISE_LOG"));
							console.log(resError);
							bcsComunes.ventanaOverlay(maPLim.idVentana,false);
						});
					}
					else bcsComunes.msg("warning","PLANTILLA LIMITES",$translate.instant("UI_NO_HAY_DATOS_PARA_GUARDAR"));
				}
				else { // CREAR
					var datosGuardar = maPLim.preparaDatosCrear();
					if(datosGuardar) {
						bcsComunes.ventanaOverlay(maPLim.idVentana,true);
						servicioPlantillaLimites.savePlantillaLimites(datosGuardar,function (result){
							maPLim.evaluaRespuestaSP(result, 'UI_PLANTILLA_LIMITES_CREADA');
						}, function(resError){
							bcsComunes.msg("error","PLANTILLA LIMITES",$translate.instant("UI_REVISE_LOG"));
							console.log(resError);
							bcsComunes.ventanaOverlay(maPLim.idVentana,false);
						});
					}
					else bcsComunes.msg("warning","PLANTILLA LIMITES",$translate.instant("UI_NO_HAY_DATOS_PARA_GUARDAR"));
				}
			};
			
			maPLim.evaluaRespuestaSP = function(result, msj){
				
				var titulo = ""
				if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID > 0 && result.P_OUT_ERRORESCONTADOR == 0){
					titulo = $translate.instant(msj);
				}
				else if(result.P_OUT_ADVERTENCIASCONTADOR !== undefined && result.P_OUT_ADVERTENCIASCONTADOR > 0 && result.P_OUT_REFID == 0){
					titulo = result.P_OUT_ADVERTENCIASDETALLE[0].Codigo + ' : ' + result.P_OUT_ADVERTENCIASDETALLE[0].Descripcion;
				}
				else if(result.P_OUT_ERRORESCONTADOR !== undefined && result.P_OUT_ERRORESCONTADOR > 0){
					titulo = result.P_OUT_ERRORESDETALLE[0].Codigo + ' : ' + result.P_OUT_ERRORESDETALLE[0].Descripcion;
				}
				else {
					titulo = $translate.instant("UI_REVISE_LOG");
				}

				console.log(result);

				var idModal = bcsComunes.ventanaMsg({
					mensaje: titulo,
					conflictos:util.parseServerResponse(result)
				},maPLim.idVentana);

				bcsComunes.ventanaPromesa(idModal).then(function(){
					maPLim.respuesta.aceptar = true;
					bcsComunes.ventanaOverlay(maPLim.idVentana,false);
					bcsComunes.ventanaData(maPLim.idVentana, maPLim.respuesta);
					bcsComunes.ventanaCerrar(maPLim.idVentana);
				});
			};
			
			
			maPLim.preparaDatosCrear = function(){
				var plantillaLimites = {};
				plantillaLimites.p_IdEmpresa 				= $scope.$root.SesionIdEmpresa; 
				plantillaLimites.p_IdUsuario 				= $scope.$root.SesionIdUsuario;
				plantillaLimites.p_IdModulo 				= "LIM";
				plantillaLimites.p_UsaReglasNegocio 		= "N";
				
				plantillaLimites.p_IdPlantillaLimite			= maPLim.datosDespues.IdPlantillaLimite;
				plantillaLimites.p_DescripcionPlantillaLimite	= maPLim.datosDespues.DescripcionPlantillaLimite;
				plantillaLimites.p_IdEstado						= maPLim.datosDespues.IdEstado === true ? 'ACT' : 'INA';
				plantillaLimites.p_PorDefecto					= maPLim.datosDespues.PorDefecto === true ? 'S'   : 'N';
				return plantillaLimites;
			};
			
			maPLim.preparaDatosModificar = function(){
				var plantillaLimites = {};
				plantillaLimites.p_IdEmpresa 			= $scope.$root.SesionIdEmpresa; 
				plantillaLimites.p_IdUsuario 			= $scope.$root.SesionIdUsuario;
				plantillaLimites.p_IdModulo 			= "LIM";
				plantillaLimites.p_UsaReglasNegocio 	= "N";
				plantillaLimites.p_IdPlantillaLimite	= maPLim.datosDespues.IdPlantillaLimite;
				
				plantillaLimites.p_RegistroCambio = [];
				
				if(maPLim.datosAntes.DescripcionPlantillaLimite !== maPLim.datosDespues.DescripcionPlantillaLimite)
					plantillaLimites.p_RegistroCambio.push({CampoModificado:'DescripcionPlantillaLimite',ValorNuevo:String(maPLim.datosDespues.DescripcionPlantillaLimite)});
				if(maPLim.datosAntes.PorDefecto!==maPLim.datosDespues.PorDefecto)
					plantillaLimites.p_RegistroCambio.push({CampoModificado:'PorDefecto',ValorNuevo:(maPLim.datosDespues.PorDefecto)?'S':'N'});
				if(maPLim.datosAntes.IdEstado!==maPLim.datosDespues.IdEstado)
					plantillaLimites.p_RegistroCambio.push({CampoModificado:'IdEstado',ValorNuevo:(maPLim.datosDespues.IdEstado)?'ACT':'INA'});
				if(plantillaLimites.p_RegistroCambio.length>0) return plantillaLimites;
				else return undefined;
			};
			

			/*********************************************
			 * COMBOBOX
			 *********************************************/
			
			
			/*********************************************
			 * GO!!!
			 *********************************************/
			maPLim.cargaDatosPantalla();
	});
}());