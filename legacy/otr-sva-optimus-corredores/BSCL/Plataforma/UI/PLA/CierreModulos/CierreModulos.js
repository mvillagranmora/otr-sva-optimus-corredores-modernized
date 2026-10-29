angular.module('ModuloPlataforma').controller('CierreModulosCtrl',function($scope,bcsComunes, $translate, $resource){
	
	
	if ($scope.SesionIdEmpresa)
	{
			var empresaDatasource = new kendo.data.DataSource({
				type: "odata",
				serverFiltering:true,
	    		transport: {
	    			read: {
	    				url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1557",
	    				dataType: "json"
			    	}
				},
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
	
	$scope.Requisitos = function(){
		var id1 = bcsComunes.ventanaAbrir("RequisitosCierre", {} );
		
		var promesa = bcsComunes.ventanaPromesa(id1);
		promesa.then(function(data){ 
			//Refrescar grilla para visualizar cambios.
			//$scope.gridApi.dataSource.read();
		});
	};
	var firstHeight = $('#Win_CierreModulos');
        firstHeight.css('height', '607px');

	
	var firstWidth = $('#Win_CierreModulos');
		firstWidth.css('width', '1042px');
});