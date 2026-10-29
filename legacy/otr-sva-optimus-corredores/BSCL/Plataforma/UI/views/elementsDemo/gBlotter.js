angular.module('ModuloPlataforma').controller('gBlotterCtrl',function($scope,$translate,bcsComunes,$timeout,$http, $element){

	var refrescar = [];
	//bcsComunes.configurarVentana(900,780,false,false,'UI_TITULO_VENTANA');
	$scope.select = function(grid)
	{
		if ( refrescar[grid] === undefined || !refrescar[grid] ) {
			//$timeout(function(){$("#" + grid).find("[kendo-grid]").data("kendo-grid").refresh(),200});
			refrescar[grid] = true;
		}
	};

	$scope.tabs = new bcs.uibTab($timeout,1,$scope,$element);
	//$scope.tabs = new bcs.uibTab($timeout,1000);

	$scope.winId = bcsComunes.ventanaWinId($scope);



});
