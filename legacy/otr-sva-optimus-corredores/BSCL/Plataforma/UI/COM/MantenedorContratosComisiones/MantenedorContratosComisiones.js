angular.module('ModuloPlataforma').controller('MantenedorContratosComisionesCtrl',function($scope,$timeout){
	
	mContratosComisiones = this;
	mContratosComisiones.select = function(grid){

			util.refreshGridSize(grid,100);
		
	};
	
});