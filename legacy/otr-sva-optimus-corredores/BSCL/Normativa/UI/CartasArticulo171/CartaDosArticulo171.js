angular.module('ModuloNormativa').controller('ConsultarCartaDosCtrl', 
		function($scope,$timeout,$modal,$translate,bcsComunes,bcsConsultarCartaDos)
{	
	
	//*** configuracion de objetos ***//
	
	$scope.filtro = {};
	$scope.data ={};
	var overlay = bcsComunes.overlay;
	var Accion = ''
		

   	//GRILLA PRINCIPAL
	$scope.confBasicaCartaDos= {
		dataSource			: bcsConsultarCartaDos.dataSourceCartaDos(),
		columns				: bcsConsultarCartaDos.columnasCartaDos(),		
		sortable			: true,
		pageable			: true,
		height				: 580, 
		selectable 			: "row",
		filterable			: true	
	};

	$scope.buscarCartaDos = function(){
		var filters = new Array();
		var auxMes;
		var auxAnio;
		var auxFechaIniConcat;
		var auxFechaFinConcat;
		
		
		//Filtro Fecha
		try{
			if ($scope.data.oCartaDos.Mes !== undefined && $scope.data.oCartaDos.Mes!== "" && $scope.data.oCartaDos.Mes !== null)
			{
			//auxDate1 =  util.dateOdata(util.date($scope.data.oCartaDos.fecha).hours(0).minutes(0).seconds(0).toDate());
				auxMes =  $scope.data.oCartaDos.Mes;
				auxAnio = $scope.data.oCartaDos.anio;
				
				if(auxMes == '12')
				{
					auxFechaIniConcat = '01-'+auxMes+'-'+auxAnio;
					auxFechaFinConcat = '01-01-'+(parseInt(auxAnio,10)+1).toString();
				}
				else
				{
				
					auxFechaIniConcat = '01-'+auxMes+'-'+auxAnio;
					//auxFechaIniConcat = date.parse(auxFechaIniConcat);
					auxFechaIniConcat = moment(auxFechaIniConcat).format('DD-MM-YYYY');
					auxFechaFinConcat = '01-'+(parseInt(auxMes,10)+1).toString()+'-'+auxAnio;
					auxFechaFinConcat = moment(auxFechaFinConcat).format('DD-MM-YYYY');
				
				}
					
				
				
			var fechaInicio = new Date(auxFechaIniConcat);
			var fechaFin = new Date(auxFechaFinConcat);
			filters.push({ field: "Fecha", operator: "gte", value: fechaInicio });
			filters.push({ field: "Fecha", operator: "lte", value: fechaFin });
			}	
		}
		catch(e)
		{
		}
		
		
		
		
		$scope.gridApi.dataSource.filters(filters);		
		
	};
	 
	$scope.limpiarFiltros = function() {
		$scope.data.oCartaDos.fecha= "";
		$scope.gridApi.dataSource.removeAll();
		
	 
	
	}

	
	

});


