angular.module('ModuloNormativa').controller('PanelSeleccionRegistrosCtrl',function($scope,
		$translate,bcsComunes,$resource,$compile,bcsAlerta,OPCIONES_BCSALERTA,bcsServiciosNormativa){
	
	var overlay = bcsComunes.overlay;
	var psr = this;
	$scope.psr.oSeleccionRegistros.registroCliente = false;
	
	
	$scope.$watch('psr.oSeleccionRegistros.registroCliente',function(newval,oldval) {
		$scope.data.Nemo = 0;
		
	});
	
}); 