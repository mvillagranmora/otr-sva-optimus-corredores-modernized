(function() {
	angular.module('ModuloSocioNegocio').controller('ModalEliminarPortafolioCtrl', 
	function($scope,servicioSociodeNegocio,bcsComunes,$translate){
		
		$scope.data = {};
		var ventanaId = bcsComunes.ventanaWinId($scope);
		parametros = bcsComunes.ventanaParams (ventanaId);
		$scope.IdBoton = 'ModalEliminarPortafolio' + parametros.Entidad + '_AceptarEliminar';
		$scope.tituloModal = "Portafolio eliminado";

		//seteo inicial.
		$scope.data.Fecha = new Date();
		$scope.Url = '';
        bcsComunes.ventanaTitulo(ventanaId, $translate.instant('UI_ELIMINAR_PORTAFOLIO'));
		
		$scope.CerrarModal = function () {
			bcsComunes.ventanaCerrar(ventanaId); 
		};
		
	    $scope.EliminarPortafolio = function() {
	    	bcsComunes.ventanaOverlay(ventanaId,true);
            $scope.Url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=12661";
            var datos = {
                p_IdEmpresa : $scope.$root.SesionIdEmpresa,
                p_IdUsuario : $scope.$root.SesionIdUsuario,
                p_IdModulo : 'SOC',
                p_UsaReglasNegocio: 'S',
                p_IdTipo: $scope.data.IdTipo,
                p_IdPortafolio: parametros.IdEntidad,
                p_IdCliente: parametros.IdCliente,
                p_Comentario: $scope.data.Comentario,
            };
            servicioSociodeNegocio.eliminarPortafolioCliente(datos).$promise.then(
                function(result) {
					bcsComunes.ventanaOverlay(ventanaId,false);
					if (result.P_OUT_ERRORESCONTADOR > 0){
						var mensajes = util.parseServerResponse(result); 
						for (var i = 0; i < mensajes.errores.length; i++) {
							mensajes.errores[i].Descripcion = $translate.instant(mensajes.errores[i].Descripcion);
						}
						bcsComunes.ventanaMsg({ 
							mensaje:$translate.instant("UI_PROBLEMA_ELIMINAR_PORTAFOLIO"),
							conflictos : mensajes
						}, ventanaId);
						
					}else{
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : $scope.tituloModal}, ventanaId);
							
						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							bcsComunes.ventanaData(ventanaId,{res:'OK', result: result});
							bcsComunes.ventanaCerrar(ventanaId);
						});
					}
				},
				function(result){
					bcsComunes.ventanaOverlay(ventanaId,false);
					bcsComunes.msg("error","Error","La operacion tiene errores");
				}
            );
		};
	});
}());
//------------------------- FIN CONTROLLER --------------------------	