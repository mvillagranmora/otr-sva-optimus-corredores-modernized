angular.module('ModuloTesoreria').controller('DetalleIngresoEgresoCtrl',
		function($scope,$uibModal,$translate,bcsComunes,bcsBlotterComprobantesGrid){
	
	var InfoIngresoEgreso = this;
	
	//Recupera parametros desde la ventana recibida
    $scope.IdVentana 	= bcsComunes.ventanaWinId ($scope);
    $scope.parametros 	= bcsComunes.ventanaParams($scope.IdVentana);

    InfoIngresoEgreso.IdIngresoEgreso = $scope.parametros.IdIngresoEgreso;

	
	var definicionDsIngresoEgreso = bcsBlotterComprobantesGrid.dsDefMovimientoIngresoEgreso;
	definicionDsIngresoEgreso.definition.filter = { field: "IdIngresoEgreso", operator: "eq", value: InfoIngresoEgreso.IdIngresoEgreso }
	$scope.dataSourceIngresoEgreso = new bcs.DataSource(definicionDsIngresoEgreso);
	
	$scope.dataSourceIngresoEgreso.query().then(function(){
		$scope.$apply(function() {
			InfoIngresoEgreso.oIngresoEgreso = $scope.dataSourceIngresoEgreso.data();
		});
	});
	
		
	var definicionDsIngresoEgresoFormaDePago = bcsBlotterComprobantesGrid.dsDefMovimientoIngresoEgresoFormaDePago;	
	
	definicionDsIngresoEgresoFormaDePago.definition.filter = [
	                                                           { field: "IdIngresoEgreso", operator: "eq", value: InfoIngresoEgreso.IdIngresoEgreso },
	                                                           { field: "IdEstado", operator: "neq", value: "ANU" },
	                                                           { field: "IdEstado", operator: "neq", value: "MOD" }
	                                                         ];
	
	$scope.dataSourceIngresoEgresoFormaDePago = new bcs.DataSource(definicionDsIngresoEgresoFormaDePago);
	
	$scope.dataSourceIngresoEgresoFormaDePago.query().then(function(){
		$scope.$apply(function() {
			InfoIngresoEgreso.oIngresoEgresoFormaDePago = $scope.dataSourceIngresoEgresoFormaDePago.data();
		});
	});


});
