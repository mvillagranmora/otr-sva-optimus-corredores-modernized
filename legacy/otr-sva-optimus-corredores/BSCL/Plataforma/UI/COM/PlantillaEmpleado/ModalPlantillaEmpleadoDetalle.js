(function() {
	angular.module('ModuloPlataforma').controller('PopUpNuevaLineaPlantillaEmpleadoCtrl',
		function ($scope,bcsComunes,servicioPlantillaEmpleado,$translate) {
			$scope.conf = {};
			$scope.data = {};
			$scope.dataModal = {};
			var respuesta = {};
			var ventanaId = bcsComunes.ventanaWinId($scope);
			parametros = bcsComunes.ventanaParams (ventanaId);
			
			$scope.conf = {};
			
			$scope.conf.oConfiguracionMoneda={
					tipo : "combobox-moneda",
					configuracion : {},
					filtros : {
						
					},
					idActual : false,
					seleccionado : false,
					idDefecto : false
				};
			
			$scope.conf.TipoProducto={
					filtros : {},
				};

			$scope.conf.TipoOperacion={
				filtros : {
					IdTipoProducto : { "field" : "IdTipoProducto", "operator": "eq","value" : '-1' }
				},
			};
			
			$scope.conf.oConfiguracionEstado={
					tipo : "combobox-estado-perfil",
					configuracion : {},
					filtros : {
						
					},
					idActual : false,
					seleccionado : false,
					idDefecto : 'ACT'
				};
			
			if(parametros.perfil)
			{
				//$scope.datosCabecera.bloqueado = true;
				bcsComunes.ventanaTitulo(ventanaId, $translate.instant('UI_MODIFICAR_DETALLE_PLANTILLA_EMPLEADO'));
				$scope.conf.modalidad = 'ModoEditar';
				$scope.conf.TipoProducto.IdTipoProductoDef = parametros.perfil.IdTipoProducto;
				$scope.conf.TipoOperacion.IdTipoOperacionDef = parametros.perfil.IdTipoOperacion;
				
				$scope.data.descuento = parametros.perfil.PorcentajeDescuentoComision;
				$scope.data.montoMaximoOperacion = parametros.perfil.MontoMaximoOperacion;
				$scope.data.PermiteRuteo = parametros.perfil.PermiteRuteo === 'S'? true : false;
				$scope.conf.oConfiguracionEstado.idDefecto = parametros.perfil.IdEstadoDetalle;
			}
			else
			{
				//$scope.datosCabecera.bloqueado = false;
				bcsComunes.ventanaTitulo(ventanaId, $translate.instant('UI_AGREGAR_DETALLE_PLANTILLA_EMPLEADO'));
				$scope.conf.modalidad = 'ModoCrear';
			}
	
			$scope.cancelar = function () {
				respuesta.aceptar = false;
				bcsComunes.ventanaData(ventanaId,respuesta)
				bcsComunes.ventanaCerrar(ventanaId); 
			};
			
			
			
			
			
			$scope.preparaDatosCrear = function(){
				var perfilEmpleadoDetalle = {};
				
				
				perfilEmpleadoDetalle.p_IdEmpresa 				= $scope.$root.SesionIdEmpresa; 
				perfilEmpleadoDetalle.p_IdUsuario 				= $scope.$root.SesionIdUsuario;
				perfilEmpleadoDetalle.p_IdModulo 				= "PLA";
				perfilEmpleadoDetalle.p_UsaReglasNegocio 		= "N";
				perfilEmpleadoDetalle.p_IdPlantillaEmpleado 	= parametros.IdPlantillaEmpleado;
				perfilEmpleadoDetalle.p_IdTipoProducto  		= ($scope.conf.TipoProducto.IdTipoProducto === 'TODOS' || $scope.conf.TipoProducto.IdTipoProducto === undefined) ? '0' : $scope.conf.TipoProducto.IdTipoProducto;
				perfilEmpleadoDetalle.p_IdTipoOperacion  		= ($scope.conf.TipoOperacion.IdTipoOperacion === 'TODOS' || $scope.conf.TipoOperacion.IdTipoOperacion  === undefined) ? '0' : $scope.conf.TipoOperacion.IdTipoOperacion ;
				perfilEmpleadoDetalle.p_PorcentajeDescuentoComision  = $scope.data.descuento;
				perfilEmpleadoDetalle.p_PorcentajeMargenEmpleado = 0;
				perfilEmpleadoDetalle.p_PermiteRuteo 			= $scope.data.PermiteRuteo ? 'S' : 'N';
				perfilEmpleadoDetalle.p_MontoMaximoRuteo 		= 0;
				perfilEmpleadoDetalle.p_MontoMaximoOperacion	= $scope.data.montoMaximoOperacion;
				perfilEmpleadoDetalle.p_IdEstado 				= $scope.conf.oConfiguracionEstado.IdEstado;
				
				
				return perfilEmpleadoDetalle;
			};
			
			$scope.preparaDatosModificar = function(){
				var perfilEmpleadoDetalle = {};
				
				
				perfilEmpleadoDetalle.p_IdEmpresa 				= $scope.$root.SesionIdEmpresa; 
				perfilEmpleadoDetalle.p_IdUsuario 				= $scope.$root.SesionIdUsuario;
				perfilEmpleadoDetalle.p_IdModulo 				= "PLA";
				perfilEmpleadoDetalle.p_UsaReglasNegocio 		= "N";
				perfilEmpleadoDetalle.p_IdPlantillaEmpleado 	= parametros.IdPlantillaEmpleado;
				perfilEmpleadoDetalle.p_IdPlantillaEmpleadoDetalle = parametros.perfil.IdDetallePlantillaEmpleado;
				perfilEmpleadoDetalle.p_IdTipoProducto  		= ($scope.conf.TipoProducto.IdTipoProducto === 'TODOS' || $scope.conf.TipoProducto.IdTipoProducto === undefined) ? '0' : $scope.conf.TipoProducto.IdTipoProducto;
				perfilEmpleadoDetalle.p_IdTipoOperacion  		= ($scope.conf.TipoOperacion.IdTipoOperacion === 'TODOS' || $scope.conf.TipoOperacion.IdTipoOperacion  === undefined) ? '0' : $scope.conf.TipoOperacion.IdTipoOperacion ;
				perfilEmpleadoDetalle.p_Descuento  				= $scope.data.descuento;
				perfilEmpleadoDetalle.p_IdEstado 				= $scope.conf.oConfiguracionEstado.IdEstado;
				perfilEmpleadoDetalle.p_PermiteRuteo 			= $scope.data.PermiteRuteo ? 'S' : 'N';
				perfilEmpleadoDetalle.p_MontoMaximoOperacion	= $scope.data.montoMaximoOperacion;
				
				return perfilEmpleadoDetalle;
			};
			
			$scope.guardar = function() {
				if($scope.conf.modalidad === 'ModoCrear') {
					var plantillaEmpleados = $scope.preparaDatosCrear();
					if(plantillaEmpleados !== undefined) {
						servicioPlantillaEmpleado.savePerfilEmpleadoDetalle(plantillaEmpleados,function (result){
//							var datos = {};
//							if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID  > 0){
//								datos.REF_ID = result.P_OUT_REFID;
//								datos.message = 'Plantilla Empleados Ingresado';
//								bcsComunes.msg("info","GUARDAR PLANTILLA EMPLEADOS",datos.message);
//								console.log(result);
//								
//							}
//							else {
//								datos.message = result.CODIGO + ' : ' + result.PILA;
//								bcsComunes.msg("error","GUARDAR PLANTILLA EMPLEADOS",datos.message);
//								console.log(result);
//							}
							
							
							
							var p1 = bcsComunes.ventanaMsg({ mensaje : "Plantilla Empleados Ingresado",
								conflictos : util.parseServerResponse(result)}, ventanaId);
							bcsComunes.ventanaPromesa(p1).then(function(result2){
								if (result.P_OUT_ERRORESDETALLE.length == 0) {
									bcsComunes.ventanaData(ventanaId, true);
									bcsComunes.ventanaCerrar(ventanaId);  
								}
							})
							
							},function (result){
								var datos = {};
								datos.message = 'Error en el Ingreso '+result.data.CODIGO + ' : ' + result.data.PILA;
								
								bcsComunes.msg("error","GUARDAR PLANTILLA EMPLEADOS",datos.message);
								console.log(JSON.stringify(result));
								
								
								
								
						});
					}
					else {
						console.log("No hay datos :(");
					}
				}
				else if($scope.conf.modalidad === 'ModoEditar') {
					var plantillaEmpleados = $scope.preparaDatosModificar();
					if(plantillaEmpleados !== undefined) {
						servicioPlantillaEmpleado.modifyPerfilEmpleadoDetalle(plantillaEmpleados,function (result){
							
							
							bcsComunes.ventanaMsg({ mensaje : "Detalle Plantilla Empleados Modificado",
								conflictos : util.parseServerResponse(result)}, ventanaId);
							bcsComunes.ventanaData(ventanaId, true);
							bcsComunes.ventanaCerrar(ventanaId);
						
						},function (result){
								var datos = {};
								datos.message = 'Error en el Ingreso '+result.data.CODIGO + ' : ' + result.data.PILA;
								
								bcsComunes.msg("error","GUARDAR PLANTILLA EMPLEADOS",datos.message);
								console.log(JSON.stringify(result));
								
								
								
								
						});
					}
				}
			};
	
			
	});
}());