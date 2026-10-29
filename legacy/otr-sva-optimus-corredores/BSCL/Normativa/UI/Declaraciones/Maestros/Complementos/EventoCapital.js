angular.module('ModuloNormativa').controller('EventoCapitalCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionMaestroDeclaraciones,bcsAlerta,bcsServiciosNormativa,OPCIONES_BCSALERTA,$timeout){
	$scope.formData = {};
	$scope.filtros = {};
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.parametros = bcsComunes.ventanaParams($scope.winID);
	var esquema = $scope.parametros.esquema;
	var version = $scope.parametros.version;
	$scope.fechaDesde = null;
	$scope.fechaHasta = null;
	$scope.selectOne = null;
	$scope.datos = {};
	
	if (esquema == 'DJ1949') {
		var conf = bcsConfiguracionMaestroDeclaraciones.getConsultaEventosCapitalPendientesDJ1949();
	}else if (esquema == 'DJ1922') {
		var conf = bcsConfiguracionMaestroDeclaraciones.getConsultaEventosCapitalPendientesDJ1922();
	}else if (esquema == 'DJ1885') {
		var conf = bcsConfiguracionMaestroDeclaraciones.getConsultaEventosCapitalPendientes();
	};
	
	$scope.defaultOptions = conf.options;

	var dataSourceEventoCapital = conf.options.dataSource;

	$scope.configuracionGrillaEventoCapital = {
		dataSource : dataSourceEventoCapital,
		columns : [ 
			{field : 'RutSociedad',title : 'Rut Sociedad',width : '100px'}, 
			{field : 'Nemotecnico',title : 'Nemotecnico',width : '100px'}, 
			{field : 'SecuenciaEvento',title : 'Secuencia Evento',width : '100px'}, 
			{field : 'RazonSocial',title : 'Razón Social',width : '100px'},
			{field : 'FechaPago',title : 'Fecha Pago',width : '100px', filterable:{cell:{enabled:false}}}
			],
		allowCopy : false,
		autoBind : false,
		userSettings : true,
		excel : {
			allPages : true
		},
		filterable : {mode: "row"},
		height : 260,
		pageable : true,
		reorderable : true,
		selectable: "row",
		resizable : true,
		scrollable : true,
		sortable : true
	};

	$scope.buscarEventoCapital = function(apiGridEventoCapital,filtros) {
		//conf.buscar(apiGridEventoCapital, filtros);
		var year = version.substring(0,4);
		var month= 0;
		var startDate = new Date(year, month, 1);
		var endDate = new Date(year, month + 11, 31);
		$scope.fechaDesde = util.dateSPdate(startDate);
		$scope.fechaHasta = util.dateSPdate(endDate);
		var filters = new Array();
		if ($scope.fechaDesde != null) {
			filters.push({
				field : "FechaPago",
				operator : 'gte',
				value : $scope.fechaDesde
			});
		}
		if ($scope.fechaHasta != null) {
			filters.push({
				field : "FechaPago",
				operator : 'lte',
				value : $scope.fechaHasta
			});
		}
		$scope.apiGridEventoCapital.dataSource.filter(filters);
  	};
	
	$scope.winID=bcsComunes.ventanaWinId($scope);
	
  	$scope.defaultOptions.gridCreated = function(grid){
		$scope.ajustaVentana();
	}
  	
  	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,40);
    }
  	
  	$scope.asociarEvento = function(datos) {
  		console.log(datos);
  		bcsComunes.ventanaAbrir("AsociarEventoCapitalTributario", {
			SecuenciaEvento : datos.SecuenciaEvento,
			FechaPago : datos.FechaPago, 
			Nemotecnico: datos.Nemotecnico,
			Esquema: esquema,
			Version: version 
			});
	}
});