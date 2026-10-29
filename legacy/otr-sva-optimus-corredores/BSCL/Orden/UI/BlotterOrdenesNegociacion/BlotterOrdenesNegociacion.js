angular.module('ModuloOrdenes').controller('BlotterOrdenesNegociacionCtrl',function($scope,$timeout){

	$scope.select = function(grid)
	{
		$timeout(function() {
			if ($("#" + grid).find(".k-grid").data("kendo-grid"))
				$("#" + grid).find(".k-grid").data("kendo-grid").refresh()
		},50);
	};
	
});
