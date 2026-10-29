angular.module('ModuloPlataforma').controller('DashboardPowerBICtrl',function($scope,$translate,$sce,bcsComunes){
	$scope.data = {};
	$scope.data.url = undefined;
	$scope.ventanaId = bcsComunes.ventanaWinId($scope);

	var dataSourceConfiguracion = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering	: true, 
			serverPaging 	: true, 
			serverSorting	: true,
			transport: {
				read: {
					url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1556",
					dataType: "json"
				}		
			},
			schema: {
				model: {
					fields: { Valor : { type: "string"}	}
				}
			},
			filter: [{ field: "CodigoParametro"	, operator: "eq", value: 'PLA.UrlDashboardPowerBI' },
					 { field: "IdEmpresa"		, operator: "eq", value: $scope.SesionIdEmpresa }]
		}
	});

	dataSourceConfiguracion.fetch(function(){
		if(dataSourceConfiguracion.data().length > 0){
			$scope.data.url = $sce.trustAsResourceUrl(dataSourceConfiguracion.data()[0].Valor);
		}else{
			var promesa = bcsComunes.ventanaMsgErr("Dashboard no configurado", $scope.ventanaId);
			bcsComunes.ventanaPromesa(promesa).then(function(){
				bcsComunes.ventanaCerrar($scope.ventanaId);
			});

		}
	});

});