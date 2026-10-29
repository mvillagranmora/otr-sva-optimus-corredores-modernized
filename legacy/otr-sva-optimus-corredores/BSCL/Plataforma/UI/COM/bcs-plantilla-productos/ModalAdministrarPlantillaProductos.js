(function() {
	angular.module('ModuloPlataforma').controller('ModalAdministrarPlantillaProductosCtrl',
		function ($scope,bcsComunes,servicioPerfilProductos) {
			/*********************************************
			 * VARIABLES Y CONTEXTOS
			 ********************************************/
			var modAdmPprod 			= this;
			modAdmPprod.idVentana 	= bcsComunes.ventanaWinId($scope);
			modAdmPprod.parametros 	= bcsComunes.ventanaParams(modAdmPprod.idVentana);
			modAdmPprod.domVentana	= $("[ventanaid='"+modAdmPprod.idVentana+"']");
			
			modAdmPprod.cmbbx		= {};
			modAdmPprod.win			= {};
			modAdmPprod.respuesta 	= {};
			
			modAdmPprod.datosAntes 	= {};
			modAdmPprod.datosDespues= {};
			
			/*********************************************
			 * FUNCIONES
			 *********************************************/
			modAdmPprod.cargaDatosPantalla = function(){
				if(modAdmPprod.parametros.perfil) {
					modAdmPprod.datosAntes = _.clone(modAdmPprod.parametros.perfil);
					//modAdmPprod.datosAntes.Habilitado = (modAdmPprod.parametros.perfil.Habilitado==='S')?true:false;
					//modAdmPprod.datosAntes.PorDefecto = (modAdmPprod.parametros.perfil.PorDefecto==='S')?true:false;
					modAdmPprod.datosDespues = _.clone(modAdmPprod.datosAntes);
				}
				else {
					modAdmPprod.datosDespues.Habilitado = true;
				}
			};		
	
			modAdmPprod.cancelar = function () {
				modAdmPprod.respuesta.aceptar = false;
				bcsComunes.ventanaData(modAdmPprod.idVentana, modAdmPprod.respuesta);
				bcsComunes.ventanaCerrar(modAdmPprod.idVentana);
			};

			modAdmPprod.guardar = function () {
				bcsComunes.ventanaOverlay(modAdmPprod.idVentana,true);	
				modAdmPprod.respuesta.IdPerfilProductos = modAdmPprod.datosDespues.IdPerfilProductos;
				if(modAdmPprod.datosAntes.IdPerfilProductos) { // EDITAR
					var datosGuardar = modAdmPprod.preparaDatosModificar();
					if(datosGuardar) {
						servicioPerfilProductos.updatePerfilProductos(datosGuardar,function (result){
							var titulo = "";
							if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID  > 0){
								titulo = "Perfil de Productos Actualizado";
								bcsComunes.ventanaData(modAdmPprod.idVentana, modAdmPprod.respuesta);
							}
							else {
								titulo = "PERFIL DE PRODUCTOS "+ result.CODIGO + ' : ' + result.PILA;
								console.log(result);
							}
							
							var idModal = bcsComunes.ventanaMsg({
								mensaje: titulo,
								conflictos:util.parseServerResponse(result)
							},modAdmPprod.idVentana);
							
							bcsComunes.ventanaPromesa(idModal).then(function(){
								bcsComunes.ventanaOverlay(modAdmPprod.idVentana,false);
								bcsComunes.ventanaCerrar(modAdmPprod.idVentana);	
							});
							
						}, function(resError){
							bcsComunes.ventanaOverlay(modAdmPprod.idVentana,false);	
							bcsComunes.msg("error","PERFIL DE PRODUCTOS",'Revise Log');
							console.log(resError);
						});
					}
					else {
						bcsComunes.ventanaOverlay(modAdmPprod.idVentana,false);	
						bcsComunes.msg("warning","PERFIL DE PRODUCTOS",'No hay datos para guardar');
					}
				}
				else { // CREAR
					var datosGuardar = modAdmPprod.preparaDatosCrear();
					if(datosGuardar) {	
						servicioPerfilProductos.savePerfilProductos(datosGuardar,function (result){
							var titulo = "";

							if(result.P_OUT_ERRORESCONTADOR>0){
								
								modAdmPprod.respuesta.aceptar = false;
								bcsComunes.ventanaOverlay(modAdmPprod.idVentana, modAdmPprod.respuesta);
								
							}else{
								if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID  > 0){
									titulo = "Perfil de Productos Creado";
									modAdmPprod.respuesta.aceptar = true;
									bcsComunes.ventanaData(modAdmPprod.idVentana, modAdmPprod.respuesta);
								}
								else {
									titulo = "PERFIL DE PRODUCTOS "+ result.CODIGO + ' : ' + result.PILA;
									console.log(result);
								}
							}
							var idModal = bcsComunes.ventanaMsg({
								mensaje: titulo,
								conflictos:util.parseServerResponse(result)
							},modAdmPprod.idVentana);
							
							bcsComunes.ventanaPromesa(idModal).then(function(){
								bcsComunes.ventanaOverlay(modAdmPprod.idVentana,false);
								bcsComunes.ventanaCerrar(modAdmPprod.idVentana);	
							});
							
						}, function(resError){
							bcsComunes.ventanaOverlay(modAdmPprod.idVentana,false);	
							bcsComunes.msg("error","PERFIL DE PRODUCTOS",'Revise Log');
							console.log(resError);
						});
					}
					else{
						bcsComunes.ventanaOverlay(modAdmPprod.idVentana,false);	
						bcsComunes.msg("warning","PERFIL DE PRODUCTOS",'No hay datos para guardar');
					}
				}
			};
			
			
			modAdmPprod.preparaDatosCrear = function(){
				var perfilProductos = {};
				perfilProductos.p_IdEmpresa 				= $scope.$root.SesionIdEmpresa; 
				perfilProductos.p_IdUsuario 				= $scope.$root.SesionIdUsuario;
				perfilProductos.p_IdModulo 				  	= "PLA";
				perfilProductos.p_UsaReglasNegocio 			= "S";
				perfilProductos.p_IdPerfilProductos 			= modAdmPprod.datosDespues.IdPerfilProductos;
				perfilProductos.p_DescripcionPerfilProductos 	= modAdmPprod.datosDespues.DescripcionPerfilProductos;
				perfilProductos.p_IdCategoriaInversionista 	= modAdmPprod.datosDespues.IdCategoriaInversionista;
				perfilProductos.p_EsPerfilPersonalizado	= 'N';
				perfilProductos.p_IdEstado  				= (modAdmPprod.datosDespues.Habilitado === true) ? 'ACT' : 'INA';
				perfilProductos.p_PorDefecto 				= (modAdmPprod.datosDespues.PorDefecto === true) ? 'S'   : 'N';
				return perfilProductos;
			};
			
			modAdmPprod.preparaDatosModificar = function(){
				var perfilProductos = {};
				perfilProductos.p_IdEmpresa 				= $scope.$root.SesionIdEmpresa; 
				perfilProductos.p_IdUsuario 				= $scope.$root.SesionIdUsuario;
				perfilProductos.p_IdModulo 				= "PLA";
				perfilProductos.p_UsaReglasNegocio 		= "N";
				perfilProductos.p_IdPerfilProductos 			= modAdmPprod.datosDespues.IdPerfilProductos;
				
				perfilProductos.p_RegistroCambio = [];
				
				if(modAdmPprod.datosAntes.DescripcionPerfilProductos!==modAdmPprod.datosDespues.DescripcionPerfilProductos)
					perfilProductos.p_RegistroCambio.push({CampoModificado:'DescripcionPerfilProductos',ValorNuevo:String(modAdmPprod.datosDespues.DescripcionPerfilProductos)});
				if(modAdmPprod.datosAntes.IdCategoriaInversionista!==modAdmPprod.datosDespues.IdCategoriaInversionista)
					perfilProductos.p_RegistroCambio.push({CampoModificado:'IdCategoriaInversionista',ValorNuevo:modAdmPprod.datosDespues.IdCategoriaInversionista});
				if(modAdmPprod.datosAntes.PorDefecto!==modAdmPprod.datosDespues.PorDefecto)
					perfilProductos.p_RegistroCambio.push({CampoModificado:'PorDefecto',ValorNuevo:(modAdmPprod.datosDespues.PorDefecto)?'S':'N'});
				if(modAdmPprod.datosAntes.Habilitado!==modAdmPprod.datosDespues.Habilitado)
					perfilProductos.p_RegistroCambio.push({CampoModificado:'IdEstado',ValorNuevo:(modAdmPprod.datosDespues.Habilitado)?'ACT' : 'INA'});
				if(perfilProductos.p_RegistroCambio.length>0) return perfilProductos;
				else return undefined;
			};
			

			/*********************************************
			 * COMBOBOX
			 *********************************************/
			
			

			
			/*********************************************
			 * GO!!!
			 *********************************************/
			modAdmPprod.cargaDatosPantalla();
			
	});
}());