angular.module('ModuloPlataforma').controller('RegistroMensajesCtrl',function($scope,bcsComunes){
	

	$scope.datars = bcsComunes.logMensajes.data;
	
	$scope.test = function() {
		bcsComunes.msg('3','444','55555');
	};
});