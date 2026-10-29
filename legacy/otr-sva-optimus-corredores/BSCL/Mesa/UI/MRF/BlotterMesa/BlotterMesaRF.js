angular.module('ModuloMesa').controller('BlotterOperacionesMesaRFCtrl',function($scope,bcsComunes,$timeout){
	
	$scope.winID = bcsComunes.ventanaWinId($scope); //obtengo el ambito
	$scope.data = {};
	
	$scope.abrirResumenPosicion = function() {
		$scope.data.WinIngOperacion = $scope;
		$scope.data.childPositionX = "RIGHT";
		$scope.data.childPositionY = "TOP";
		var id1=bcsComunes.ventanaAbrir("ResumenMRF", $scope.data , false, false, $scope.winID); // le paso los datos al hijo
		$scope.ventanaResumen = id1;
		bcsComunes.ventanaOverlay($scope.winID,false);
	}
	
	$scope.validaVentana = function (idVentana) {
		if ($("[ventanaid='" + idVentana + "']").length > 0) return true;
		return false;
	}
	
	var blotterOpMesa = this;		
	$scope.select = function(grid)
	{
		$timeout(function() {
			if ($("#" + grid).find(".k-grid").data("kendo-grid"))
				$("#" + grid).find(".k-grid").data("kendo-grid").refresh()
		},50);
	};
	//blotterOpMesa.buscar =[];
	$scope.abrirResumenPosicion();
	
	//Cierra ventana adicional al presionar la cruz de la ventana principal
	$("[ventanaid='" + $scope.winID + "']").data("kendoWindow").bind("close", 
				function w(e) {
					if ($scope.WinResumenMRF != null) bcsComunes.ventanaCerrar($scope.WinResumenMRF.winID);
				}
		);
});