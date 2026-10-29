(function() {
	angular.module('ModuloPlataforma').controller('ModalAgregarValorProductoDetalleCtrl',
		function ($scope,bcsComunes,servicioPerfilProductos) {
			/*********************************************
			 * VARIABLES Y CONTEXTOS
			 ********************************************/
			var mavpd 			= this;
			mavpd.idVentana 	= bcsComunes.ventanaWinId($scope);
			mavpd.parametros 	= bcsComunes.ventanaParams(mavpd.idVentana);
			mavpd.domVentana	= $("[ventanaid='"+mavpd.idVentana+"']");
			var lineaDetalle	= _.clone(mavpd.parametros.lineaDetalle);
			var IdPerfilProductos	= mavpd.parametros.IdPerfilProductos;
			mavpd.cmbbx			= {};
			mavpd.respuesta 	= {};
			mavpd.esProductoPermiteRuteo = false;
			mavpd.listaProductoPermiteRuteo = ['CUST','INAC','INAX','INCI','INCX','INEN','INEX','INMI','INMO','INOS'];
			//bcsComunes.ventanaTamanoAutomatico(mavpd.idVentana,20);
			
			/*********************************************
			 * FUNCIONES
			 *********************************************/
			mavpd.cargaDatosPantalla = function(){
				lineaDetalle = lineaDetalle || {};
				mavpd.respuesta.IdTipoProducto 				= lineaDetalle.IdTipoProducto || 0;
				mavpd.respuesta.IdTipoProductoDef			= lineaDetalle.IdTipoProducto || 0;
				mavpd.respuesta.CodigoProductoClasificacion 	= lineaDetalle.CodigoProductoClasificacion || 0;
				mavpd.respuesta.CodigoProductoClasificacionDef	= lineaDetalle.CodigoProductoClasificacion || 0;
				mavpd.respuesta.IdValorProducto 				= lineaDetalle.IdValorProducto || 0;
				mavpd.respuesta.IdValorProductoDef				= lineaDetalle.IdValorProducto || 0;
				mavpd.respuesta.EsLineaNueva				= (lineaDetalle.esEdicionLinea === true) ? 'N': 'S';
				mavpd.respuesta.IdBolsaDef 					= lineaDetalle.IdBolsa || 'BCS';
				mavpd.respuesta.IdTipoOperacion 			= lineaDetalle.IdTipoOperacion || "0";
				mavpd.respuesta.IdTipoOperacionDef 			= lineaDetalle.IdTipoOperacion || "0";
				mavpd.respuesta.IdCanalIngreso				= lineaDetalle.IdCanalIngreso || "0";
				mavpd.respuesta.IdCanalIngresoDef 			= lineaDetalle.IdCanalIngreso || "0";
				mavpd.respuesta.DiasPlazo 					= lineaDetalle.DiasPlazo || 0;
				mavpd.respuesta.EstadoDetalleDef 			= lineaDetalle.EstadoProducto || 'ACT';
				mavpd.respuesta.Prioridad					= lineaDetalle.Prioridad || 0;
				mavpd.respuesta.PermiteRuteo 				= (lineaDetalle.PermiteRuteo=='S') ? true : false;
				mavpd.respuesta.IdGrupoActivosDef 			= lineaDetalle.IdGrupoActivos || 0;
				
				if (!mavpd.listaProductoPermiteRuteo.includes(lineaDetalle.IdTipoProducto)) {
					mavpd.ProductoPermiteRuteo = true;
				}
			};		
	
			mavpd.cancelar = function () {
				mavpd.respuesta.aceptar = false;
				bcsComunes.ventanaData(mavpd.idVentana, mavpd.respuesta);
				bcsComunes.ventanaCerrar(mavpd.idVentana);
			};

			mavpd.aceptar = function () {
				if(lineaDetalle.IdDetallePerfilProductos) { // EDITAR
					var datosGuardar = mavpd.preparaDatosModificar();
					if(datosGuardar) {
						bcsComunes.ventanaOverlay(mavpd.idVentana,true);	
						servicioPerfilProductos.updateDetallePerfilProductos(datosGuardar,function (result){
							var titulo ="";
							if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID  > 0 && result.P_OUT_ERRORESCONTADOR===0){
								titulo = 'Perfil de Productos Actualizado';
								mavpd.respuesta.aceptar = true;
								bcsComunes.ventanaData(mavpd.idVentana, mavpd.respuesta);
							}
							else {
								titulo = "PERFIL DE PRODUCTOS";
								console.log(result);
							}

							var idModal = bcsComunes.ventanaMsg({
								mensaje: titulo,
								conflictos:util.parseServerResponse(result)
							},mavpd.idVentana);

							bcsComunes.ventanaPromesa(idModal).then(function(){
								bcsComunes.ventanaOverlay(mavpd.idVentana,false);
								bcsComunes.ventanaCerrar(mavpd.idVentana);	
							});
							

						}, function(resError){
							bcsComunes.ventanaOverlay(mavpd.idVentana,false);
							bcsComunes.msg("error","PERFIL DE PRODUCTOS",'Revise Log');
							console.log(resError);
						});
					}
					else {
						bcsComunes.ventanaOverlay(mavpd.idVentana,false);
						bcsComunes.msg("warning","PERFIL DE PRODUCTOS",'No hay datos para guardar');
					}
					
				}
				else { // CREAR
					var datosGuardar = mavpd.preparaDatosCrear();
					var detalleExiste = mavpd.detalleExiste();
					if(!datosGuardar) { bcsComunes.msg("warning","PERFIL DE PRODUCTOS",'No hay datos para guardar'); return; }
					if(detalleExiste) { bcsComunes.msg("warning","PERFIL DE PRODUCTOS",'Detalle existe'); return; }	
					bcsComunes.ventanaOverlay(mavpd.idVentana,true);	
					servicioPerfilProductos.saveDetallePerfilProductos(datosGuardar,function (result){
						var titulo = "";
						if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID  > 0 && result.P_OUT_ERRORESCONTADOR===0){
							titulo = 'Detalle creado';
							mavpd.respuesta.aceptar = true;
							bcsComunes.ventanaData(mavpd.idVentana, mavpd.respuesta);
						}
						else {
							titulo = "PERFIL DE PRODUCTOS",result.CODIGO + ' : ' + result.PILA;
						}
						
						var idModal = bcsComunes.ventanaMsg({
								mensaje: titulo,
								conflictos:util.parseServerResponse(result)
							},mavpd.idVentana);
							
							bcsComunes.ventanaPromesa(idModal).then(function(){
								bcsComunes.ventanaOverlay(mavpd.idVentana,false);
								bcsComunes.ventanaCerrar(mavpd.idVentana);	
							});
							
					}, function(resError){
						bcsComunes.ventanaOverlay(mavpd.idVentana,false);
						bcsComunes.msg("error","PERFIL DE PRODUCTOS",'Revise Log');
						console.log(resError);
					});
				}
			};
			
			
			mavpd.detalleExiste = function(){
				var existe = false;
				if(mavpd.parametros && mavpd.parametros.detallePerfil) {
					$.each(mavpd.parametros.detallePerfil,function(key,value){
						if(	   value.IdTipoProducto == mavpd.respuesta.IdTipoProducto
							&& value.IdTipoOperacion == mavpd.respuesta.IdTipoOperacion
							&& value.IdCanalIngreso == mavpd.respuesta.IdCanalIngreso
						) {
							existe = true;
							return false;
						}
					});
				}
				return existe;
			};
			
			mavpd.preparaDatosCrear = function(){
				var datosGuardar = {};
				datosGuardar.p_IdEmpresa 				= $scope.$root.SesionIdEmpresa; 
				datosGuardar.p_IdUsuario 				= $scope.$root.SesionIdUsuario;
				datosGuardar.p_IdModulo 				= "PLA";
				datosGuardar.p_UsaReglasNegocio 		= "S";
				
				datosGuardar.p_IdPerfilProductos 			= IdPerfilProductos;
				
				datosGuardar.p_IdTipoProducto			= mavpd.respuesta.IdTipoProducto; 
				datosGuardar.p_IdTipoOperacion			= mavpd.respuesta.IdTipoOperacion;
				datosGuardar.p_IdCanalIngreso			= mavpd.respuesta.IdCanalIngreso;
				datosGuardar.p_IdMoneda					= 0;
				datosGuardar.p_IdGrupoActivos			= (mavpd.respuesta.IdGrupoActivos)?mavpd.respuesta.IdGrupoActivos:'';
				datosGuardar.p_IdEstado					= mavpd.respuesta.EstadoDetalle;
				datosGuardar.p_PermiteRuteo				= (mavpd.respuesta.PermiteRuteo)?"S":"N";
				return datosGuardar;
			};
			
			
			mavpd.preparaDatosModificar = function(){
				var datosGuardar = {};
				datosGuardar.p_IdEmpresa 				= $scope.$root.SesionIdEmpresa; 
				datosGuardar.p_IdUsuario 				= $scope.$root.SesionIdUsuario;
				datosGuardar.p_IdModulo 				= "PLA";
				datosGuardar.p_UsaReglasNegocio 		= "N";
				
				datosGuardar.p_IdDetallePerfilProductos = lineaDetalle.IdDetallePerfilProductos;
				
				datosGuardar.p_RegistroCambio	= [];	
				
				if(mavpd.respuesta.IdTipoOperacion !== lineaDetalle.IdTipoOperacion)
					datosGuardar.p_RegistroCambio.push({CampoModificado:'IdTipoOperacion',ValorNuevo:mavpd.respuesta.IdTipoOperacion});
				if(mavpd.respuesta.IdCanalIngreso !== lineaDetalle.IdCanalIngreso)
					datosGuardar.p_RegistroCambio.push({CampoModificado:'IdCanalIngreso',ValorNuevo:mavpd.respuesta.IdCanalIngreso});
				if(mavpd.respuesta.IdGrupoActivos !== lineaDetalle.IdGrupoActivos)
					datosGuardar.p_RegistroCambio.push({CampoModificado:'IdGrupoActivos',ValorNuevo:(mavpd.respuesta.IdGrupoActivos)?mavpd.respuesta.IdGrupoActivos:''});
				if(mavpd.respuesta.EstadoDetalle !== lineaDetalle.EstadoDetalle)
					datosGuardar.p_RegistroCambio.push({CampoModificado:'IdEstado',ValorNuevo:mavpd.respuesta.EstadoDetalle});
				if(mavpd.respuesta.EstadoDetalle !== lineaDetalle.EstadoDetalle)
					datosGuardar.p_RegistroCambio.push({CampoModificado:'PermiteRuteo',ValorNuevo:((mavpd.respuesta.PermiteRuteo)?"S":"N")});
				if(datosGuardar.p_RegistroCambio.length>0) return datosGuardar;
				else return undefined;
			};


			/*********************************************
			 * COMBOBOX
			 *********************************************/
			mavpd.cmbbx.Bolsa={
				filtros : {},
			};
			
			mavpd.cmbbx.TipoProducto={
					filtros : {},
				};

			mavpd.cmbbx.TipoOperacion={
				filtros : {
					IdTipoProducto : { "field" : "IdTipoProducto", "operator": "eq","value" : '-1' }
				},
			};
			
			mavpd.cmbbx.TipoCanalIngreso={
				filtros : {
					IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
				},
			};
			
			mavpd.cmbbx.GrupoActivos={
				filtros : {
					IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
					IdEmpresa : { "field"   : "IdEmpresa","operator": "eq", "value"   : $scope.SesionIdEmpresa },
				},
			};

			mavpd.cmbbx.EstadoPerfilComercial={
				filtros : {},
			};

			
			
			/*********************************************
			 * GO!!!
			 *********************************************/
			mavpd.cargaDatosPantalla();
			$scope.$watch('mavpd.respuesta.IdTipoProducto',function(newval,oldval)
			{
				if (newval!=0) {
					if (!mavpd.listaProductoPermiteRuteo.includes(mavpd.respuesta.IdTipoProducto)) {
						mavpd.ProductoPermiteRuteo = true;
						mavpd.respuesta.PermiteRuteo = false;
					}
					else{
						mavpd.ProductoPermiteRuteo = false;
					}
				}
			});
			
	});
}());