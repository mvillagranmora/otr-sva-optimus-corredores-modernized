(function() {
	angular.module('ModuloPlataforma').controller('ModalAdministrarPlantillaCobrosPeruCtrl',
		function ($scope,bcsComunes,bcsServiciosPlataformaComercialPeru) {
			/*********************************************
			 * VARIABLES Y CONTEXTOS
			 ********************************************/
			var modAdmPcob 			= this;
			modAdmPcob.idVentana 	= bcsComunes.ventanaWinId($scope);
			modAdmPcob.parametros 	= bcsComunes.ventanaParams(modAdmPcob.idVentana);
			modAdmPcob.domVentana	= $("[ventanaid='"+modAdmPcob.idVentana+"']");
			modAdmPcob.modcop		= modAdmPcob.parametros.copiar;
			
			modAdmPcob.cmbbx		= {};
			modAdmPcob.win			= {};
			modAdmPcob.respuesta 	= {};
			
			modAdmPcob.datosAntes 	= {};
			modAdmPcob.datosDespues	= {};
			
			/*********************************************
			 * FUNCIONES
			 *********************************************/
			modAdmPcob.cargaDatosPantalla = function(){
				if(modAdmPcob.parametros.copiar){
					modAdmPcob.datosAntes = _.clone(modAdmPcob.parametros.perfil);
					modAdmPcob.datosDespues = _.clone(modAdmPcob.datosAntes);
					modAdmPcob.datosDespues.IdPerfilCobrosNuevo= '';
					modAdmPcob.modcop=false;
				}
				
				else {
					if(modAdmPcob.parametros.perfil){
						modAdmPcob.datosAntes = _.clone(modAdmPcob.parametros.perfil);
						modAdmPcob.datosDespues = _.clone(modAdmPcob.datosAntes);
						modAdmPcob.modcop=true;
					}
					else{
						modAdmPcob.datosDespues.Habilitado = true;
						modAdmPcob.modcop=true;
					}
				}
			};		
	
			modAdmPcob.cancelar = function () {
				modAdmPcob.respuesta.aceptar = false;
				bcsComunes.ventanaData(modAdmPcob.idVentana, modAdmPcob.respuesta);
				bcsComunes.ventanaCerrar(modAdmPcob.idVentana);
			};

			modAdmPcob.guardar = function () {
				bcsComunes.ventanaOverlay(modAdmPcob.idVentana,true);	
				modAdmPcob.respuesta.IdPerfilCobros = modAdmPcob.datosDespues.IdPerfilCobros;
				if(modAdmPcob.datosAntes.IdPerfilCobros) { // EDITAR
					if(modAdmPcob.modcop===false){//copiar
						//cosole.log(modAdmPcob.modcop);
						var datosGuardar = modAdmPcob.preparaDatosCopiar();
						if(datosGuardar) {	
							bcsServiciosPlataformaComercialPeru.copiarPerfilCobros(datosGuardar,function (result){
								if (result.P_OUT_REFID !== null && result.P_OUT_REFID  > 0){
									bcsComunes.ventanaOverlay(modAdmPcob.idVentana,false);
									bcsComunes.msg("error","PERFIL DE COBROS",result.CODIGO + ' : ' + result.PILA);
									console.log(result);
								}
								else {
									
									bcsComunes.msg("info","PERFIL DE COBROS",'Perfil de Cobros Creado');
									modAdmPcob.respuesta.aceptar = true;
									bcsComunes.ventanaOverlay(modAdmPcob.idVentana,false);
									bcsComunes.ventanaData(modAdmPcob.idVentana, modAdmPcob.respuesta);
									bcsComunes.ventanaCerrar(modAdmPcob.idVentana);	
								}
							}, function(resError){
								bcsComunes.ventanaOverlay(modAdmPcob.idVentana,false);
								bcsComunes.msg("error","PERFIL DE COBROS",'Revise Log');
								console.log(resError);
							});
						}
						else {
							bcsComunes.ventanaOverlay(modAdmPcob.idVentana,false);
							bcsComunes.msg("warning","PERFIL DE COBROS",'No hay datos para guardar');
						}
						
					}else{//editar
					var datosGuardar = modAdmPcob.preparaDatosModificar();
					if(datosGuardar) {
						bcsServiciosPlataformaComercialPeru.modificarPerfilCobros(datosGuardar,function (result){
							if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID  > 0){
								bcsComunes.msg("info","PERFIL DE COBROS",'Perfil de Cobros Actualizado');
								bcsComunes.ventanaOverlay(modAdmPcob.idVentana,false);
								bcsComunes.ventanaData(modAdmPcob.idVentana, modAdmPcob.respuesta);
								bcsComunes.ventanaCerrar(modAdmPcob.idVentana);
							}
							else {
								bcsComunes.ventanaOverlay(modAdmPcob.idVentana,false);
								bcsComunes.msg("error","PERFIL DE COBROS",result.CODIGO + ' : ' + result.PILA);
							}
						}, function(resError){
							bcsComunes.ventanaOverlay(modAdmPcob.idVentana,false);
							bcsComunes.msg("error","PERFIL DE COBROS",'Revise Log');
						});
					}
					else {
						bcsComunes.ventanaOverlay(modAdmPcob.idVentana,false);
						bcsComunes.msg("warning","PERFIL DE COBROS",'No hay datos para guardar');
					}
				}
				}
				else { // CREAR
					var datosGuardar = modAdmPcob.preparaDatosCrear();
					if(datosGuardar) {	
						bcsServiciosPlataformaComercialPeru.crearPerfilCobros(datosGuardar,function (result){
							if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID  > 0){
								bcsComunes.msg("info","PERFIL DE COBROS",'Perfil de Cobros Creado');
								modAdmPcob.respuesta.aceptar = true;
								bcsComunes.ventanaOverlay(modAdmPcob.idVentana,false);
								bcsComunes.ventanaData(modAdmPcob.idVentana, modAdmPcob.respuesta);
								bcsComunes.ventanaCerrar(modAdmPcob.idVentana);	
							}
							else {
								bcsComunes.ventanaOverlay(modAdmPcob.idVentana,false);
								bcsComunes.msg("error","PERFIL DE COBROS",result.CODIGO + ' : ' + result.PILA);
								console.log(result);
							}
						}, function(resError){
							bcsComunes.ventanaOverlay(modAdmPcob.idVentana,false);
							bcsComunes.msg("error","PERFIL DE COBROS",'Revise Log');
							console.log(resError);
						});
					}
					else {
						bcsComunes.ventanaOverlay(modAdmPcob.idVentana,false);
						bcsComunes.msg("warning","PERFIL DE COBROS",'No hay datos para guardar');
					}
				}
			};
			
			
			modAdmPcob.preparaDatosCrear = function(){
				var perfilCobros = {};
				perfilCobros.p_IdEmpresa 				= $scope.$root.SesionIdEmpresa; 
				perfilCobros.p_IdUsuario 				= $scope.$root.SesionIdUsuario;
				perfilCobros.p_IdModulo 				= "PLA";
				perfilCobros.p_UsaReglasNegocio 		= "N";
				perfilCobros.p_IdPerfilCobros 			= modAdmPcob.datosDespues.IdPerfilCobros;
				perfilCobros.p_DescripcionPerfilCobros 	= modAdmPcob.datosDespues.DescripcionPerfilCobros;
				perfilCobros.p_EsPerfilPersonalizado	= 'N';
				perfilCobros.p_IdEstado  				= (modAdmPcob.datosDespues.Habilitado === true) ? 'ACT' : 'INA';
				return perfilCobros;
			};
			
			modAdmPcob.preparaDatosModificar = function(){
				var perfilCobros = {};
				perfilCobros.p_IdEmpresa 				= $scope.$root.SesionIdEmpresa; 
				perfilCobros.p_IdUsuario 				= $scope.$root.SesionIdUsuario;
				perfilCobros.p_IdModulo 				= "PLA";
				perfilCobros.p_UsaReglasNegocio 		= "N";
				perfilCobros.p_IdPerfilCobros 			= modAdmPcob.datosDespues.IdPerfilCobros;
				
				perfilCobros.p_RegistroCambio = [];
				
				if(modAdmPcob.datosAntes.DescripcionPerfilCobros!==modAdmPcob.datosDespues.DescripcionPerfilCobros)
					perfilCobros.p_RegistroCambio.push({CampoModificado:'DescripcionPerfilCobros',ValorNuevo:String(modAdmPcob.datosDespues.DescripcionPerfilCobros)});
				if(modAdmPcob.datosAntes.Habilitado!==modAdmPcob.datosDespues.Habilitado)
					perfilCobros.p_RegistroCambio.push({CampoModificado:'IdEstado',ValorNuevo:(modAdmPcob.datosDespues.Habilitado)?'S':'N'});
				if(perfilCobros.p_RegistroCambio.length>0) return perfilCobros;
				else return undefined;
			};
			
			
			modAdmPcob.preparaDatosCopiar = function(){
				var perfilCobros = {};
				perfilCobros.p_IdEmpresa 				= $scope.$root.SesionIdEmpresa; 
				perfilCobros.p_IdUsuario 				= $scope.$root.SesionIdUsuario;
				perfilCobros.p_IdModulo 				= "PLA";
				perfilCobros.p_UsaReglasNegocio 		= "N";
				perfilCobros.p_IdPerfilCobros 			= modAdmPcob.datosDespues.IdPerfilCobros;
				perfilCobros.p_DescripcionPerfilCobros 	= modAdmPcob.datosDespues.DescripcionPerfilCobros;
				perfilCobros.p_EsPerfilPersonalizado	= 'N';
				perfilCobros.p_IdEstado  				= (modAdmPcob.datosDespues.Habilitado === true) ? 'ACT' : 'INA';
				
				
				perfilCobros.p_RegistroCambio = [];
				if(modAdmPcob.datosAntes.IdPerfilCobros!==modAdmPcob.datosDespues.IdPerfilCobrosNuevo)
					perfilCobros.p_RegistroCambio.push({CampoModificado:'IdPerfilCobrosNuevo',ValorNuevo:String(modAdmPcob.datosDespues.IdPerfilCobrosNuevo)});
				if(perfilCobros.p_RegistroCambio.length>0) return perfilCobros;
				else return undefined;
			};

			modAdmPcob.cargaDatosPantalla();
			
	});
}());