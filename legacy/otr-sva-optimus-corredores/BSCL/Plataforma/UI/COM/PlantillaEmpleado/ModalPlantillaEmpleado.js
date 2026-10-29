(function() {
	angular.module('ModuloPlataforma').controller('ModalAdministrarPlantillaEmpleadosCtrl',
		function ($scope,bcsComunes,servicioPlantillaEmpleado,$translate) {
			$scope.conf = {};
			$scope.dataModal = {};
			$scope.datosCabecera = {};
			var respuesta = {};
			var ventanaId = bcsComunes.ventanaWinId($scope);
			
			parametros = bcsComunes.ventanaParams (ventanaId);
			
			if(parametros.perfil)
			{
				bcsComunes.ventanaTitulo(ventanaId, $translate.instant('UI_MODIFICAR_PLANTILLA_EMPLEADO'));
				$scope.datosCabecera.bloqueado = true;
				$scope.conf.modalidad = 'ModoEditar';
				$scope.datosCabecera.IdPerfilEmpleado = parametros.perfil.IdPlantillaEmpleado;
				$scope.datosCabecera.DescripcionPerfilEmpleado = parametros.perfil.Descripcion;
				$scope.datosCabecera.Habilitado = parametros.perfil.Habilitado;
			}
			else
			{
				bcsComunes.ventanaTitulo(ventanaId, $translate.instant('UI_AGREGAR_PLANTILLA_EMPLEADO'));
				$scope.datosCabecera.bloqueado = false;
				$scope.conf.modalidad = 'ModoCrear';
			}
			
			
	
			$scope.cancelar = function () {
				respuesta.aceptar = false;
				bcsComunes.ventanaData(ventanaId,respuesta)
				bcsComunes.ventanaCerrar(ventanaId); 
			};
	
			
			$scope.preparaDatosCrear = function(){
				var perfilEmpleado = {};
				perfilEmpleado.p_IdEmpresa 				= $scope.$root.SesionIdEmpresa; 
				perfilEmpleado.p_IdUsuario 				= $scope.$root.SesionIdUsuario;
				perfilEmpleado.p_IdModulo 				= "PLA";
				perfilEmpleado.p_UsaReglasNegocio 		= "N";
				perfilEmpleado.p_IdPlantillaEmpleado 	= $scope.datosCabecera.IdPerfilEmpleado;
				perfilEmpleado.p_Descripcion 			= $scope.datosCabecera.DescripcionPerfilEmpleado;
				perfilEmpleado.p_IdEstado 				= $scope.datosCabecera.Habilitado ? 'ACT':'INA';
				
				
				return perfilEmpleado;
			};
			
			$scope.preparaDatosModificar = function(){
				var perfilEmpleado = {};
				perfilEmpleado.p_IdEmpresa 				= $scope.$root.SesionIdEmpresa; 
				perfilEmpleado.p_IdUsuario 				= $scope.$root.SesionIdUsuario;
				perfilEmpleado.p_IdModulo 				= "PLA";
				perfilEmpleado.p_UsaReglasNegocio 		= "N";
				perfilEmpleado.p_IdPlantillaEmpleado 	= $scope.datosCabecera.IdPerfilEmpleado;
				perfilEmpleado.p_Descripcion 			= $scope.datosCabecera.DescripcionPerfilEmpleado;
				perfilEmpleado.p_IdEstado 				= $scope.datosCabecera.Habilitado ? 'ACT':'INA';
				
				
				return perfilEmpleado;
			};
			
			$scope.guardar = function() {
				if($scope.conf.modalidad === 'ModoCrear') {
					var plantillaEmpleados = $scope.preparaDatosCrear();
					if(plantillaEmpleados !== undefined) {
						servicioPlantillaEmpleado.savePerfilEmpleado(plantillaEmpleados,function (result){
							
							bcsComunes.ventanaMsg({ mensaje : "Plantilla Empleados Ingresado",
								conflictos : util.parseServerResponse(result)}, ventanaId);
							 bcsComunes.ventanaData(ventanaId, {res:true, IdPlantillaEmpleado:plantillaEmpleados});
							bcsComunes.ventanaCerrar(ventanaId); 
							
							},function (result){
								var datos = {};
								datos.message = 'Error en el Ingreso '+result.data.CODIGO + ' : ' + result.data.PILA;
								bcsComunes.ventanaMsg({ mensaje : "Plantilla Empleados Ingresado",
									conflictos : util.parseServerResponse(result)}, ventanaId);
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
						servicioPlantillaEmpleado.modifyPerfilEmpleado(plantillaEmpleados,function (result){

														
							bcsComunes.ventanaMsg({ mensaje : "Plantilla Empleados Modificado",
								conflictos : util.parseServerResponse(result)}, ventanaId);
							
							bcsComunes.ventanaData(ventanaId, {res:true, IdPlantillaEmpleado: parametros.perfil.IdPlantillaEmpleado});
							bcsComunes.ventanaCerrar(ventanaId); 
							
							},function (result){
								bcsComunes.ventanaMsg({ mensaje : "Error en el Ingreso",
									conflictos : util.parseServerResponse(result)}, ventanaId);
								console.log(JSON.stringify(result));
						});
					}
					
				}
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
			
			$scope.conf.oConfiguracionMoneda={
					tipo : "combobox-moneda",
					configuracion : {},
					filtros : {
						
					},
					idActual : false,
					seleccionado : false,
					idDefecto : false
				};
	
			
	});
}());