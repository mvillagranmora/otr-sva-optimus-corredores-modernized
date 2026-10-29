angular.module('ModuloSocioNegocio').
	controller('ModalModificarSupervisorAEjecutivosCtrl',
			function($scope,$translate, bcsComunes, servicioSociodeNegocio){
	
	
	$scope.data = {};
	var ventanaId = bcsComunes.ventanaWinId($scope);
	parametros = bcsComunes.ventanaParams (ventanaId);
	var listadoEjecutivos = [];
	
	if(parametros){

		listadoEjecutivos = parametros.Ejecutivos;
	}

	$scope.data.Supervisor = {

		filtros: {
			
			IdEmpresa 	: {"field" : "IdEmpresa", "operator" : "eq", "value" : $scope.SesionIdEmpresa},
		},
	}

	var callService = function(v_Dato){

		servicioSociodeNegocio.ActualizarSupervisorAEjecutivo(v_Dato).$promise.then(
			
			function(result) {
				var titulo = "";

				if (result.P_OUT_ERRORESCONTADOR === 0){

					var complemento = (listadoEjecutivos.length > 1) ? "s" : "";
					titulo = "Supervisor Actualizado para Ejecutivo"+complemento;
				}
				else{

					titulo = "Error al Actualizar Supervisor";
				}

				var idMensajeModal = bcsComunes.ventanaMsg({ mensaje : titulo,
					conflictos : util.parseServerResponse(result)}, ventanaId);

				bcsComunes.ventanaPromesa(idMensajeModal).then(function(){

					bcsComunes.ventanaCerrar(ventanaId);
				})
			}
		);
		
	}

	$scope.Modificar = function(){

		var p_IdNuevoSupervisor = $scope.data.IdEmpleadoSupervisor;
		var p_listadoEjecutivos = listadoEjecutivos;
		var parametros = {

			p_IdEmpresa			   			: $scope.$root.SesionIdEmpresa, 
			p_IdUsuario			   			: $scope.$root.SesionIdUsuario,
			p_IdModulo			   			: 'SOC',
			p_UsaReglasNegocio	   			: 'N',
			p_EmpleadoEjecutivos			: {
				
												p_IdNuevoSupervisor,
												p_listadoEjecutivos
											}							
		};

		callService(parametros);
	}
});