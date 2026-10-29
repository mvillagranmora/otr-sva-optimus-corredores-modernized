(function() {
	angular.module('ModuloPlataforma').controller('PopUpNuevaLineaPlantillaEmpleadoCtrl',
		function ($scope,bcsComunes) {
			$scope.conf = {};
			$scope.dataModal = {};
			var respuesta = {};
			var ventanaId = bcsComunes.ventanaWinId($scope);
			parametros = bcsComunes.ventanaParams (ventanaId);
			
	
			$scope.cancelar = function () {
				respuesta.aceptar = false;
				bcsComunes.ventanaData(ventanaId,respuesta)
				bcsComunes.ventanaCerrar(ventanaId); 
			};
	
			$scope.aceptar = function () {
				respuesta.aceptar = true;
				respuesta.montoMinimo = $scope.data.montoMinimo;
				respuesta.montoMaximo = $scope.data.montoMaximo;
				respuesta.descuento   = $scope.data.descuento;
				respuesta.IdEstado    = $scope.conf.oConfiguracionEstado.seleccionado.IdEstado;
				respuesta.IdMoneda 	  = $scope.conf.oConfiguracionMoneda.seleccionado.IdMoneda;
				respuesta.Moneda 	  = $scope.conf.oConfiguracionMoneda.seleccionado.Nombre;
				
				//respuesta.EsLineaNueva	= (dato1.esEdicionLinea === true) ? 'N': 'S'; // 18/12/15
//				if(respuesta.IdTipoOperacion !== undefined
//				&& respuesta.IdTipoCanalIngreso !== undefined )
//				{
//					//$modalInstance.close(respuesta);
//				}
//				else {
//					bcsComunes.msg("warning","ACEPTAR","No ha seleccionado un dato.");
//				}
				bcsComunes.ventanaData(ventanaId,respuesta)
				bcsComunes.ventanaCerrar(ventanaId); 
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