(function() {
	angular.module('ModuloPlataforma').controller('ModalAdministrarGrupoActivosDetalleCtrl',
		function ($scope,bcsComunes,servicioGrupoActivos) {
			/*********************************************
			 * VARIABLES Y CONTEXTOS
			 ********************************************/
			var magad 			= this;
			magad.idVentana 	= bcsComunes.ventanaWinId($scope);
			magad.parametros 	= bcsComunes.ventanaParams(magad.idVentana);
			magad.cmbbx			= {};
			magad.datosDespues	= {};
			//bcsComunes.ventanaTamanoAutomatico(magad.idVentana,20);
			
			/*********************************************
			 * FUNCIONES
			 *********************************************/
			magad.cargaDatosPantalla = function(){
				magad.datosDespues.idGrupoActivos =  magad.parametros.data.idGrupoActivos;

				if(magad.parametros.mod) {
					magad.datosDespues.IdDetalleGrupoActivos = magad.parametros.data.IdDetalleGrupoActivos;
					magad.datosDespues.instrumento =  magad.parametros.data.instrumento.Nemotecnico;
					magad.EstadoDetalle = magad.parametros.data.instrumento.IdEstado;
					magad.datosAntes = _.clone(magad.datosDespues);
				}
			};		
	
			magad.cancelar = function () {
				bcsComunes.ventanaData(magad.idVentana, magad.respuesta);
				bcsComunes.ventanaCerrar(magad.idVentana);
			};

			magad.guardar = function () {
				bcsComunes.ventanaOverlay(magad.idVentana,true);
				if(magad.parametros.mod) {//editar
					var grupoActivoDetalle = magad.preparaDatosModificar();
					servicioGrupoActivos.editarGrupoActivosDetalle(grupoActivoDetalle,function(result){
						var titulo = (result.P_OUT_ERRORESCONTADOR==0)?"UI_DETALLE_GRUPO_ACTIVOS_MODIFICADO" : "";

						var idMensajeModal = bcsComunes.ventanaMsg({ mensaje : titulo,
							conflictos : util.parseServerResponse(result)}, magad.idVentana);
							
						bcsComunes.ventanaPromesa(idMensajeModal).then(function(){
							bcsComunes.ventanaOverlay(magad.idVentana,false);
							bcsComunes.ventanaData(magad.idVentana,{res:'OK', result: result});
							bcsComunes.ventanaCerrar(magad.idVentana);
						});
					},function(){
						bcsComunes.ventanaOverlay(magad.idVentana,false);
						bcsComunes.msg("error","Error","La operacion tiene errores");
					});
				}else {//crear
					var grupoActivoDetalle = magad.preparaDatosCrear();
					servicioGrupoActivos.saveGrupoActivosDetalle(grupoActivoDetalle,function(result){
						var titulo = (result.P_OUT_ERRORESCONTADOR==0)?"UI_DETALLE_GRUPO_ACTIVOS_CREADO" : "";

						var idMensajeModal = bcsComunes.ventanaMsg({ mensaje : titulo,
							conflictos : util.parseServerResponse(result)}, magad.idVentana);
							
						bcsComunes.ventanaPromesa(idMensajeModal).then(function(){
							bcsComunes.ventanaOverlay(magad.idVentana,false);
							bcsComunes.ventanaData(magad.idVentana,{res:'OK', result: result});
							bcsComunes.ventanaCerrar(magad.idVentana);
						});
					},function(){
						bcsComunes.ventanaOverlay(magad.idVentana,false);
						bcsComunes.msg("error","Error","La operacion tiene errores");
					});
				}
			};
			
			
			magad.preparaDatosCrear = function(){
				var datosGuardar = {};
				datosGuardar.p_IdEmpresa 				= $scope.$root.SesionIdEmpresa; 
				datosGuardar.p_IdUsuario 				= $scope.$root.SesionIdUsuario;
				datosGuardar.p_IdModulo 				= "PLA";
				datosGuardar.p_UsaReglasNegocio 		= "N";
				
				datosGuardar.p_IdGrupoActivos 			= magad.datosDespues.idGrupoActivos;
				datosGuardar.p_IdInstrumento 			= magad.datosDespues.instrumentoObj.IdInstrumento;
				datosGuardar.p_IdEstado 				= magad.datosDespues.EstadoDetalle;
				return datosGuardar;
			};
			
			
			magad.preparaDatosModificar = function(){
				var datosGuardar = {};
				datosGuardar.p_IdEmpresa 				= $scope.$root.SesionIdEmpresa; 
				datosGuardar.p_IdUsuario 				= $scope.$root.SesionIdUsuario;
				datosGuardar.p_IdModulo 				= "PLA";
				datosGuardar.p_UsaReglasNegocio 		= "N";
				datosGuardar.p_IdDetalleGrupoActivos	= magad.datosDespues.IdDetalleGrupoActivos;
				datosGuardar.p_RegistroCambio			= [];

				if(magad.parametros.data.instrumento.IdEstado !== magad.datosDespues.EstadoDetalle){
					datosGuardar.p_RegistroCambio.push({"CampoModificado" : "IdEstado", "ValorNuevo": magad.datosDespues.EstadoDetalle});
				}
				return datosGuardar;
			};


			/*********************************************
			 * COMBOBOX
			 *********************************************/
			magad.cmbbx.GrupoActivos={
				filtros : {
				},
			};
			

			/*********************************************
			 * GO!!!
			 *********************************************/
			magad.cargaDatosPantalla();
			
	});
}());