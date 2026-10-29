angular.module('ModuloOrdenes').controller('DetalleAsignacionCtrl',
		function ($scope,bcsComunes,bcsConfiguracionOrdenesV2,bcsConsultasOrdenes,$translate) {
	
	var idVentana = bcsComunes.ventanaWinId($scope);
	$scope.parametros = bcsComunes.ventanaParams(bcsComunes.ventanaWinId($scope))

	if (!$scope.parametros.IdAsignacion)
		console.log("Asignacion no existe!");
	else {
		bcsComunes.ventanaTitulo(idVentana,$translate.instant("UI_INFORMACION_ASIGNACION") + ' ' + $scope.parametros.IdAsignacion);
		var asignacionDatasource = new bcs.DataSource({
			type : "odata",
  			definition : {
				serverFiltering:true,
					transport: {
						read: {
							url:  $scope.parametros.EsReporte === 'S'?bcsConsultasOrdenes('ConsultarAsignacionesReporteUI'): bcsConsultasOrdenes('ConsultarAsignacionesUI'),
							dataType: "json"
							}
					},
					filter: { field: "IdAsignacion", operator: "eq", value: Number($scope.parametros.IdAsignacion) },
					schema: bcsConfiguracionOrdenesV2.getSchemaAsignacion(),
			}
		});

		var movFacDatasource = new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering:true,
					transport: {
						read: {
							url: bcsConsultasOrdenes('ConsultarMovimientosFacturar'),
							dataType: "json"
							}
					},
					schema: bcsConfiguracionOrdenesV2.getSchemaMovFacturar(),
			}
		});

		asignacionDatasource.fetch().then(function(){
			if (asignacionDatasource.data().length!=1)
			{
				console.log("Asignacion no existe!");
				return false;
			}
			else
			{
				$scope.$apply(function() {$scope.asignacion = asignacionDatasource.data()[0];});
				return true;
			}
		}).then(function(){
			
			var filterMovs = [{ field: "IdAsignacion", operator: "eq", value: asignacionDatasource.data()[0]===undefined? Number($scope.parametros.IdAsignacion):asignacionDatasource.data()[0].IdAsignacion }];
			movFacDatasource.query({filter : filterMovs}).then(function(){
				$scope.$apply(function() {$scope.movimientos = movFacDatasource.data();});
			});

		});
	}
	
	$scope.verMapaRelaciones = function()
	{
		bcsComunes.ventanaAbrir("MapaRelaciones", {TipoEntidad : "ASIGNACION",IdEntidad :$scope.parametros.IdAsignacion},false,false );
	};

	var firstHeight = $('#Win_DetalleAsignacion');
	firstHeight.css("height", "533px");
});
