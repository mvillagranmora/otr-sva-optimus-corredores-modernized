angular.module('ModuloCustodia').controller('BlotterGarantiaCtrl',function($scope,bcsComunes,$timeout,$modal){
	
/**********
 * Andrés Céspedes
 * El 20-10-2017 el contenido de este archivo (el js y el html) se movió 
 * a los archivos js y html respectivos creados como "ConsultaGarantia"
 * con el fin de transformar en un blotter con pestañas las garantías
 */
	
	$scope.select = function(grid)
	{
		
		$timeout(function() {
			if ($("#" + grid).find("[kendo-grid]").data("kendo-grid"))
				$("#" + grid).find("[kendo-grid]").data("kendo-grid").refresh()
		},50);

	};
	
});