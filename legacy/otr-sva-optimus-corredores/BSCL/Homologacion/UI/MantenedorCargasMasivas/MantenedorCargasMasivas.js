angular.module('ModuloHomologacion').controller('MantenedorCargasMasivasCtrl',
		function($scope
				,$timeout
				,bcsComunes
				,bcsServicioPlataforma
				)	
{
    
	//Configuracion de Ventana Inicial
	$scope.data = {};
	
	//TipoCarga
	$scope.data.oConfiguracionTipoCargaConfiguracion={
		tipo : "combobox-tipo-carga-masiva",
		configuracion : {},
		filtros : {},
		idActual : false,
		seleccionado : false,
		idDefecto : '',
		autoCargar: true 
	}; 
	
	//ModoCarga
	$scope.data.oConfiguracionModoCargaConfiguracion={
		tipo : "combobox-modo-carga-masiva",
		configuracion : {},
		filtros : {},
		idActual : false,
		seleccionado : false,
		idDefecto : '',
		autoCargar: true
	}; 
	 
	//limpiar filtros y grilla
	$scope.limpiarFiltros = function() {
		$scope.data.oConfiguracionTipoCargaIdActual = 0;		
		//$scope.gridApi.dataSource.removeAll();
	};
	
	//RESUMEN COBERTURA
	/*$scope.ResumenOperacionesCobertura = {
		dataSource			: bcsConfiguracionRevisionCoberturasGarantias.dataSourceResumenOperacion(),
		columns				: bcsConfiguracionRevisionCoberturasGarantias.columnasResumenOperaciones(),		
		sortable			: true,
		filterable			: true,
		pageable			: true,
		autoBind			: false,  //comentar
		height				: 470,
	    selectable 			: "row"
	};
	*/

	/*$scope.BuscarCoberturas = function(){
		var filters = new Array();	
		if ($scope.data.oConfiguracionBolsa.seleccionado.IdBolsa)
			filters.push({ field: "IdBolsa", operator: "eq", value: $scope.data.oConfiguracionBolsa.seleccionado.IdBolsa });
		if ($scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento)
			filters.push({ field: "IdTipoInstrumento", operator: "eq", value: $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento });		
		if ($scope.data.oSocioNegocioConfiguracion.seleccionado.IdCliente)
			filters.push({ field: "IdCliente", operator: "eq", value: $scope.data.oSocioNegocioConfiguracion.seleccionado.IdCliente});				
		if ($scope.data.oPortafolioConfiguracion.seleccionado.IdPortafolio)
			filters.push({ field: "IdPortafolio", operator: "eq", value: $scope.data.oPortafolioConfiguracion.seleccionado.IdPortafolio});		
		$scope.gridApi.dataSource.filters(filters);		
	};*/

    //Primer evento - filtro inicial
	$timeout(function(){
	},1000);
});