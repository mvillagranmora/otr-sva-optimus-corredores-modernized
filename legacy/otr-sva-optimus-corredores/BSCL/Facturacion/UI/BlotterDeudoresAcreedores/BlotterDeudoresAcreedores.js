angular.module('ModuloFacturacion').controller('BlotterDeudoresAcreedoresCtrl',function($scope,bcsComunes,$timeout){
	
	
	$scope.select = function(grid)
	{
		$timeout(function() {
			if($('#' + grid).length > 0 && !!$("#" + grid).find("[kendo-grid]").data("kendo-grid"))
				$("#" + grid).find("[kendo-grid]").data("kendo-grid").refresh();
		},10);
	};
	
});