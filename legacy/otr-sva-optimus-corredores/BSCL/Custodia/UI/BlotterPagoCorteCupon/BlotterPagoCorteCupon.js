
angular.module('ModuloCustodia').controller('BlotterPagoCorteCuponCtrl',function($scope,bcsComunes,$timeout,$modal,$translate){
	
	$scope.select = function(grid)
	{
		$timeout(function() {
			$("#" + grid).find("[kendo-grid]").data("kendo-grid").refresh()
		},10);
	};

});