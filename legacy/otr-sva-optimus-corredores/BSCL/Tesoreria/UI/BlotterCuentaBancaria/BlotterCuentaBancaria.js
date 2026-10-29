angular.module('ModuloTesoreria').controller('BlotterCuentaBancariaCtrl',function($scope,$timeout,bcsComunes){
	
	$scope.select = function(grid)
	{
		$timeout(function() {
			if ($("#" + grid).find("[kendo-grid]").data("kendo-grid"))
				$("#" + grid).find("[kendo-grid]").data("kendo-grid").refresh()
		},50);
	};
	
	
	//Obtiene datos de la Empresa
	if ($scope.SesionIdEmpresa)
	{
			var empresaDatasource = new kendo.data.DataSource({
				type: "odata",
				serverFiltering:true,
				transport  : {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1234",dataType : "json"}},
				filter: { field: "IdEmpresa", operator: "eq", value: Number($scope.SesionIdEmpresa) },
			});
	
	        empresaDatasource.fetch().then(function(){
				 if (empresaDatasource.data().length != 1 ) 
				 {
					 console.log("Empresa no existe!");
					 return false;
				 }
				 else 
				 {
					 $scope.$apply(function() {						
						$scope.Empresa = empresaDatasource.data()[0].DocumentoIdentidad +' - '+ $scope.SesionEmpresa;
						return true;
					 });
				 }
		    });		
	};

	var firstHeight = $('#Win_BancosTesoreria');
        firstHeight.css('height', '625px');
	
	
});