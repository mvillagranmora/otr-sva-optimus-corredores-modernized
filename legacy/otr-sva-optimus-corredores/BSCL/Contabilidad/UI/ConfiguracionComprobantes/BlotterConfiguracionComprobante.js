angular.module('ModuloContabilidad').controller('BlotterConfiguracionComprobantesCtrl',function($scope,bcsComunes,$timeout){
	
	$scope.select = function(grid)
	{
		$timeout(function() {
			if ($("#" + grid).find("[kendo-grid]").data("kendo-grid"))
				$("#" + grid).find("[kendo-grid]").data("kendo-grid").refresh()
		},50);
	};
 });