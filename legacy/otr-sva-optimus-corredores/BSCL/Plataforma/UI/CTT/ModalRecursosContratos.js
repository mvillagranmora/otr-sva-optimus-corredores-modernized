angular.module('ModuloPlataforma').controller('ModalRecursosContratosCtl', function ($scope,bcsServicioOrden,bcsComunes,$translate,$resource,$sce,$http) {
		
		var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
		var parametros = bcsComunes.ventanaParams(idVentana);
		bcsComunes.ventanaTitulo(idVentana, $translate.instant(parametros.title) );
		
		$scope.start = function (){
			bcsComunes.ventanaOverlay(idVentana,true);
			var file = bcsComunes.ventanaParams(idVentana).file;
			var type = bcsComunes.ventanaParams(idVentana).type;
			
			
			$("#binaryFile").append('<embed src="'+file+'" type="'+type+'"></embed>');
			bcsComunes.ventanaOverlay(idVentana,false);
		};
		
		$scope.start();
				
	  	$scope.cerrar = function () {
			bcsComunes.ventanaCerrar(idVentana);
		};
});
