(function() {
	angular.module('ModuloPlataforma').controller('ModalAdministrarPlantillaCobrosCtrl',
		function ($scope,bcsComunes,servicioPerfilCobros) {
			/*********************************************
			 * VARIABLES Y CONTEXTOS
			 ********************************************/
			var modAdmPcob 			= this;
			modAdmPcob.idVentana 	= bcsComunes.ventanaWinId($scope);
			modAdmPcob.parametros 	= bcsComunes.ventanaParams(modAdmPcob.idVentana);
			modAdmPcob.domVentana	= $("[ventanaid='"+modAdmPcob.idVentana+"']");
			
			modAdmPcob.cmbbx		= {};
			modAdmPcob.win			= {};
			modAdmPcob.respuesta 	= {};
			
			modAdmPcob.datosAntes 	= {};
			modAdmPcob.datosDespues	= {};
			
			/*********************************************
			 * FUNCIONES
			 *********************************************/
			modAdmPcob.cargaDatosPantalla = function(){
				if(modAdmPcob.parametros.perfil) {
					modAdmPcob.datosAntes = _.clone(modAdmPcob.parametros.perfil);
					//modAdmPcob.datosAntes.Habilitado = (modAdmPcob.parametros.perfil.Habilitado==='S')?true:false;
					//modAdmPcob.datosAntes.PorDefecto = (modAdmPcob.parametros.perfil.PorDefecto==='S')?true:false;
					modAdmPcob.datosDespues = _.clone(modAdmPcob.datosAntes);
				}
				else {
					modAdmPcob.datosDespues.Habilitado = true;
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
					var datosGuardar = modAdmPcob.preparaDatosModificar();
					if(datosGuardar) {
						servicioPerfilCobros.updatePerfilCobros(datosGuardar,function (result){
							var titulo = "";
							if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID  > 0){
								titulo = 'Perfil de Cobros Actualizado';
								modAdmPcob.respuesta.aceptar = true;
								bcsComunes.ventanaData(modAdmPcob.idVentana, modAdmPcob.respuesta);
							}
							else {
								titulo = "PERFIL DE CORBROS "+ result.CODIGO + ' : ' + result.PILA;
								console.log(result);
							}
							
							var idModal = bcsComunes.ventanaMsg({
								mensaje: titulo,
								conflictos:util.parseServerResponse(result)
							},modAdmPcob.idVentana);
							
							bcsComunes.ventanaPromesa(idModal).then(function(){
								bcsComunes.ventanaOverlay(modAdmPcob.idVentana,false);
								bcsComunes.ventanaCerrar(modAdmPcob.idVentana);	
							});
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
				else { // CREAR
					var datosGuardar = modAdmPcob.preparaDatosCrear();
					if(datosGuardar) {	
						servicioPerfilCobros.savePerfilCobros(datosGuardar,function (result){
							var titulo = "";
							if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID  > 0){
								titulo = 'Perfil de Cobros Creado';
								modAdmPcob.respuesta.aceptar = true;
								bcsComunes.ventanaData(modAdmPcob.idVentana, modAdmPcob.respuesta);
							}
							else {
								titulo = "PERFIL DE CORBROS "+ result.CODIGO + ' : ' + result.PILA;
								console.log(result);
							}
							
							var idModal = bcsComunes.ventanaMsg({
								mensaje: titulo,
								conflictos:util.parseServerResponse(result)
							},modAdmPcob.idVentana);
							
							bcsComunes.ventanaPromesa(idModal).then(function(){
								bcsComunes.ventanaOverlay(modAdmPcob.idVentana,false);
								bcsComunes.ventanaCerrar(modAdmPcob.idVentana);	
							});
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
				perfilCobros.p_PorDefecto 				= (modAdmPcob.datosDespues.PorDefecto === true) ? 'S'   : 'N';
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
				if(modAdmPcob.datosAntes.PorDefecto!==modAdmPcob.datosDespues.PorDefecto)
					perfilCobros.p_RegistroCambio.push({CampoModificado:'PorDefecto',ValorNuevo:(modAdmPcob.datosDespues.PorDefecto)?'S':'N'});
				if(modAdmPcob.datosAntes.Habilitado!==modAdmPcob.datosDespues.Habilitado)
					perfilCobros.p_RegistroCambio.push({CampoModificado:'IdEstado',ValorNuevo:(modAdmPcob.datosDespues.Habilitado)?'S':'N'});
				if(perfilCobros.p_RegistroCambio.length>0) return perfilCobros;
				else return undefined;
			};
			

			/*********************************************
			 * COMBOBOX
			 *********************************************/
			
			
			/*$scope.$watch('modAdmPcob.datosDespues.IdPerfilCobros',function(newVal){
				if (newVal.indexOf(' ') >= 0)  
					modAdmPcob.datosDespues.IdPerfilCobros = modAdmPcob.datosDespues.IdPerfilCobros.replace(' ','');
			});*/
			
			
			/*********************************************
			 * GO!!!
			 *********************************************/
			modAdmPcob.cargaDatosPantalla();
			
	});
}());