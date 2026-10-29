(function() {
	angular.module('ModuloPlataforma').controller('ModalAdministrarGrupoActivosCtrl',
		function ($scope,bcsComunes,servicioGrupoActivos) {
			/*********************************************
			 * VARIABLES Y CONTEXTOS
			 ********************************************/
			var modAdmGAct 			= this;
			modAdmGAct.idVentana 	= bcsComunes.ventanaWinId($scope);
			modAdmGAct.parametros 	= bcsComunes.ventanaParams(modAdmGAct.idVentana);
			modAdmGAct.domVentana	= $("[ventanaid='"+modAdmGAct.idVentana+"']");
			
			modAdmGAct.cmbbx		= {};
			modAdmGAct.win			= {};
			modAdmGAct.respuesta 	= {};
			
			modAdmGAct.datosAntes 	= {};
			modAdmGAct.datosDespues= {};
			
			/*********************************************
			 * FUNCIONES
			 *********************************************/
			modAdmGAct.cargaDatosPantalla = function(){
				if(modAdmGAct.parametros.mod) {
					console.log(modAdmGAct.parametros.data);
					modAdmGAct.datosDespues.IdGrupoActivos = modAdmGAct.parametros.data.IdGrupoActivos;
					modAdmGAct.datosDespues.DescripcionGrupoActivos =  modAdmGAct.parametros.data.DescripcionGrupoActivos;
					modAdmGAct.oConfiguracionTipoGrupoActivos.idDefecto = modAdmGAct.parametros.data.TipoGrupoActivosObj.IdTipoGrupoActivos;
					modAdmGAct.datosDespues.Habilitado = modAdmGAct.parametros.data.Habilitado;
					modAdmGAct.datosDespues.PorDefecto = modAdmGAct.parametros.data.PorDefecto;
					modAdmGAct.datosAntes = _.clone(modAdmGAct.datosDespues);
				}
				else {
					modAdmGAct.datosDespues.Habilitado = true;
				}
			};		
	
			modAdmGAct.cancelar = function () {
				modAdmGAct.respuesta.aceptar = false;
				bcsComunes.ventanaData(modAdmGAct.idVentana, modAdmGAct.respuesta);
				bcsComunes.ventanaCerrar(modAdmGAct.idVentana);
			};

			modAdmGAct.guardar = function () {
				bcsComunes.ventanaOverlay(modAdmGAct.idVentana,true);
				var grupoActivo = modAdmGAct.preparaDatos();
				if(!modAdmGAct.parametros.mod){
					servicioGrupoActivos.saveGrupoActivos(grupoActivo,function (result){
						var titulo = (result.P_OUT_ERRORESCONTADOR==0)?"UI_GRUPO_ACTIVOS_CREADO" : "";
						
						var idMensajeModal = bcsComunes.ventanaMsg({ mensaje : titulo,
							conflictos : util.parseServerResponse(result)}, modAdmGAct.idVentana);
							
						bcsComunes.ventanaPromesa(idMensajeModal).then(function(){
							bcsComunes.ventanaOverlay(modAdmGAct.idVentana,false);
							bcsComunes.ventanaData(modAdmGAct.idVentana,{res:'OK', result: result});
							bcsComunes.ventanaCerrar(modAdmGAct.idVentana);
						});
					},function(){
						bcsComunes.ventanaOverlay(modAdmGAct.idVentana,false);
						bcsComunes.msg("error","Error","La operacion tiene errores");
					});
				}else{
					var grupoActivo = modAdmGAct.preparaDatosModificacion();
					servicioGrupoActivos.editarGrupoActivos(grupoActivo, function(result){
						var titulo = (result.P_OUT_ERRORESCONTADOR==0)?"UI_GRUPO_ACTIVOS_MODIFICADO" : "";

						var idMensajeModal = bcsComunes.ventanaMsg({ mensaje : titulo,
							conflictos : util.parseServerResponse(result)}, modAdmGAct.idVentana);
							
						bcsComunes.ventanaPromesa(idMensajeModal).then(function(){
							bcsComunes.ventanaOverlay(modAdmGAct.idVentana,false);
							bcsComunes.ventanaData(modAdmGAct.idVentana,{res:'OK', result: result});
							bcsComunes.ventanaCerrar(modAdmGAct.idVentana);
						});
					},function(){
						bcsComunes.ventanaOverlay(modAdmGAct.idVentana,false);
						bcsComunes.msg("error","Error","La operacion tiene errores");
					});
				}	
			};
			
			
			modAdmGAct.preparaDatos = function(){
				var grupoActivo = {};
				grupoActivo.p_IdEmpresa 				= $scope.$root.SesionIdEmpresa; 
				grupoActivo.p_IdUsuario 				= $scope.$root.SesionIdUsuario;
				grupoActivo.p_IdModulo 				  	= "PLA";
				grupoActivo.p_simularOperacion 			= "N";
				grupoActivo.p_UsaReglasNegocio 			= "N";
				grupoActivo.p_IdGrupoActivos 			= modAdmGAct.datosDespues.IdGrupoActivos;
				grupoActivo.p_DescripcionGrupoActivos  	= modAdmGAct.datosDespues.DescripcionGrupoActivos;
				grupoActivo.p_IdTipoGrupoActivos 		= modAdmGAct.oConfiguracionTipoGrupoActivos.idActual;
				grupoActivo.p_EsPerfilPersonalizado		= 'N';
				grupoActivo.p_IdEstado  				= (modAdmGAct.datosDespues.Habilitado === true) ? 'ACT' : 'INA';
				grupoActivo.p_PorDefecto 				= (modAdmGAct.datosDespues.PorDefecto === true) ? 'S'   : 'N';
				return grupoActivo;
			};
			modAdmGAct.preparaDatosModificacion = function(){
				var grupoActivo = {};
				grupoActivo.p_IdEmpresa 				= $scope.$root.SesionIdEmpresa; 
				grupoActivo.p_IdUsuario 				= $scope.$root.SesionIdUsuario;
				grupoActivo.p_IdModulo 				  	= "PLA";
				grupoActivo.P_SIMULAROPERACION 			= "N";
				grupoActivo.p_UsaReglasNegocio 			= "N";
				grupoActivo.p_IdGrupoActivos			= modAdmGAct.datosDespues.IdGrupoActivos;
				grupoActivo.p_RegistroCambio			= [];

				$.each(modAdmGAct.datosAntes, function(key){
					if(modAdmGAct.datosAntes[key] !== modAdmGAct.datosDespues[key]){
						if(key==="Habilitado"){
							grupoActivo.p_RegistroCambio.push({"CampoModificado" : "IdEstado", "ValorNuevo": (modAdmGAct.datosDespues[key])?"ACT":"INA"});	
						}else if(key==="PorDefecto"){
							grupoActivo.p_RegistroCambio.push({"CampoModificado" : key, "ValorNuevo": (modAdmGAct.datosDespues[key])?"S":"N"});	
						}else{
							grupoActivo.p_RegistroCambio.push({"CampoModificado" : key, "ValorNuevo": modAdmGAct.datosDespues[key]});	
						}
					}
				});
				if(modAdmGAct.oConfiguracionTipoGrupoActivos.idActual !== modAdmGAct.parametros.data.TipoGrupoActivosObj.IdTipoGrupoActivos){
					grupoActivo.p_RegistroCambio.push({"CampoModificado" : "IdTipoGrupoActivos", "ValorNuevo": modAdmGAct.oConfiguracionTipoGrupoActivos.idActual});
				}

				if(grupoActivo.p_RegistroCambio.length>0){
					console.log(grupoActivo.p_RegistroCambio);	
				}
				return grupoActivo;
			};	

			/*********************************************
			 * COMBOBOX
			 *********************************************/
			modAdmGAct.oConfiguracionTipoGrupoActivos={
				tipo : "combobox-tipo-grupo-activos",
				configuracion : {},
				filtros : {},
				idActual : false,
				seleccionado : false,
				idDefecto : ''
			};
			

			
			/*********************************************
			 * GO!!!
			 *********************************************/
			modAdmGAct.cargaDatosPantalla();
			
	});
}());