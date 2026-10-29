angular.module('ModuloMesa').controller('BlotterSolicitudPuntasCtrl',function($scope,bcsComunes,$timeout,$modal){
	
	$scope.select = function(grid)
	{
		
		$timeout(function() {
			if ($("#" + grid).find(".k-grid").data("kendo-grid"))
				$("#" + grid).find(".k-grid").data("kendo-grid").refresh()
		},50);

	};
	
});