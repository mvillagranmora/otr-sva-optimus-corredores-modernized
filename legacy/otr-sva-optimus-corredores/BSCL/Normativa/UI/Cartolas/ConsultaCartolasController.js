angular.module('ModuloNormativa').controller(
		'ConsultaCartolasController',
		[ '$scope', '$http', '$translate', '$timeout', 'bcsComunes',
				'bcsAlerta', 'OPCIONES_BCSALERTA', 'Utils', 'bcsCartolas','bcsServiciosCartola',
				'Model', ConsultaCartolasController ]);

function ConsultaCartolasController($scope, $http, $translate, $timeout,
		bcsComunes, bcsAlerta, OPCIONES_BCSALERTA, Utils, bcsCartolas, bcsServiciosCartola, Model) {

	$scope.FechaHasta = util.dateInicioDia($scope.SesionFechaSistema);
	
	
	var dataSourceDescargaCartolas = bcsCartolas
			.getDataSourceDescargaCartolas($scope.SesionIdEmpresa);

	$scope.configSocioNegocio = {
		filtros : {
			IdEmpresa : {
				'field' : 'IdEmpresa',
				'operator' : 'eq',
				'value' : $scope.SesionIdEmpresa
			},
			
			EsCliente : {
				'field' : 'EsCliente',
				'operator' : 'eq',
				'value' : 'UI_SI'
			}
		},
	};

	$scope.configPortafolio = {
		filtros : {
			IdCliente : {
				'field' : 'IdCliente',
				'operator' : 'eq',
				'value' : 0
			},
			IdPortafolio : {
				'field' : 'IdPortafolio',
				'operator' : 'eq',
				'value' : 0
			}
		},
	};

   	$scope.filtroMoneda1 = { "field"   : "IdMoneda","operator": "eq", "value" : "CLP" };
	$scope.filtroMoneda2 = { "field"   : "IdMoneda","operator": "eq", "value" : "USD" };
	$scope.filtroMoneda3 = { "field"   : "IdMoneda","operator": "eq", "value" : "UF" };
    
	$scope.MonedaCartola = {};

	$scope.MonedaCartola.filtroMoneda = {
			complejo : [$scope.filMonCLP],
			logic : "or",
			filters : [
				$scope.filtroMoneda1, $scope.filtroMoneda2, $scope.filtroMoneda3
			]
	};
    
	$scope.configuracionGrillaDescargaCartolas = {
		dataSource : dataSourceDescargaCartolas,
		columns : [ 
			{field : 'Periodo',title : 'Periodo',width : '100px'}, 
			{field : 'IdEmpresa',title : 'IdEmpresa',width : '100px', hidden:true}, 
			{field : 'IdCartola',title : 'DESCARGAR',width : '100px',template : kendo.template("#if (IdCartola === '0' || IdCartola === '') {#Sin Errores#}else{#<a class='downloadOffline'>Descargar</a> #}#") },
			{field : 'IdNomina',title : 'IdNomina',width : '100px', hidden:true}, 
			{field : 'IdCliente',title : 'IdCliente',width : '100px', hidden:true}, 
			{field : 'IdPortafolio',title : 'IdPortafolio',width : '100px', hidden:true}, 
			{field : 'CodigoPortafolio',title : 'CodigoPortafolio',width : '100px', hidden:true}, 
			{field : 'Reprocesar',title : 'Reprocesar',width : '100px', hidden:true}, 
			{field : 'Notificar',title : 'Notificar',width : '100px', hidden:true}, 
			{field : 'EstadoProceso',title : 'EstadoProceso',width : '100px', hidden:true}, 
			{field : 'MensajeProceso',title : 'MensajeProceso',width : '100px', hidden:true}, 
			{field : 'DocumentoIdentidad',title : 'DocumentoIdentidad',width : '100px', hidden:true}, 
			{field : 'NombreCliente',title : 'NombreCliente',width : '100px', hidden:true}, 
			{field : 'DireccionElectronica',title : 'DireccionElectronica',width : '100px', hidden:true}
			],
		allowCopy : false,
		autoBind : false,
		userSettings : true,
		excel : {
			allPages : true
		},
		filterable : false,
		height : 260,
		pageable : true,
		reorderable : true,
		resizable : true,
		scrollable : true,
		dataBound			: function(e) { // Este evento se ejecuta cada vez que la grilla se vincula al dataSource
															$scope.armaRutaDescargaArchivo();},
		sortable : true
	};

	var pct = this;
	pct.FecOptions = {format: 'yyyy', start: 'decade',depth: 'decade',buttomView: 'decade',activeView:'decade',topView:'decade'};
	pct.FecInicio 	 = util.dateInicioDia($scope.SesionFechaSistema);
	pct.FecFin 	 = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.pct=pct;
	

	$scope.limpiarFiltros = function() {
		$scope.IdCliente = null;
		$scope.IdPortafolio = null;
		$scope.AnoDesde = null;
		$scope.AnoHasta = null;
		$scope.pct.FecInicio = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.pct.FecFin = util.dateInicioDia($scope.SesionFechaSistema);
	}

	$scope.buscarCartolas = function() {
		var errors = [];
		$scope.AnoDesde = $scope.pct.FecInicio.getFullYear().toString();
		$scope.AnoHasta = $scope.pct.FecFin.getFullYear().toString();
		if ($scope.IdPortafolio === null
				|| typeof $scope.IdPortafolio !== 'number') {
			errors.push('Debe seleccionar un portafolio');
		}

		if ($scope.AnoDesde !== null && typeof $scope.AnoDesde === 'string'
				&& $scope.AnoHasta !== null
				&& typeof $scope.AnoHasta === 'string') {
			if ($scope.AnoDesde > $scope.AnoHasta)
				errors.push('Rango de años inválido');
		}

		if (errors.length > 0) {
			Utils.showErrors(errors, 'Consulta de Cartolas', bcsComunes
					.ventanaWinId($scope));
			return;
		}

		var filters = new Array();

		filters.push({
			field : 'IdEmpresa',
			operator : 'eq',
			value : $scope.SesionIdEmpresa
		});

		if ($scope.IdCliente !== null && typeof $scope.IdCliente === 'number') {
			filters.push({
				field : 'IdCliente',
				operator : 'eq',
				value : $scope.ClienteSeleccionado.IdCliente
			});
		}

		if ($scope.IdPortafolio !== null
				&& typeof $scope.IdPortafolio === 'number') {
			filters.push({
				field : 'IdPortafolio',
				operator : 'eq',
				value : $scope.IdPortafolio
			});
		}

		if ($scope.AnoDesde !== null && typeof $scope.AnoDesde === 'string') {
			filters.push({
				field : 'Periodo',
				operator : 'gte',
				value : $scope.AnoDesde + '-01'
			});
		}

		if ($scope.AnoHasta !== null && typeof $scope.AnoHasta === 'string') {
			filters.push({
				field : 'Periodo',
				operator : 'lte',
				value : $scope.AnoHasta + '-12'
			});
		}
		
		filters.push({
			field : 'TipoProceso',
			operator : 'neq',
			value : 'ONLINE'
		});

		dataSourceDescargaCartolas.query({ filter: filters}).then(function(){
		});
		
		var grilla = $("#gridDescargaCartolas").find("[kendo-grid]");
		grilla.height(grilla.height());
	}

	$scope.armaRutaDescargaArchivo = function()
	{
			$("a.downloadOffline").on("click",function(e){
				var data = $("#gridDescargaCartolas").find("[kendo-grid]").data("kendoGrid").dataItem($(this).parent().parent());
				$scope.obtenerOffline(data);
			});	

	}

	$scope.obtenerOffline = function(dataItem) {
		var params = {};
		bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),true);
		params.p_IdEmpresa = $scope.SesionIdEmpresa;
		params.p_IdUsuario = $scope.SesionIdUsuario;
		params.p_IdCartola = dataItem.IdCartola;
		params.p_AccionNomina	= 'OFFLINE';
		bcsServiciosCartola.GenerarCartola(params, function(response) {
			bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),false);
			if (response.P_OUT_CARTOLAS != null && response.P_OUT_CARTOLAS[0]){
				var leBlob = util.b64toBlob(response.P_OUT_CARTOLAS[0].CartolaEncoded,'application/pdf',512);
				saveAs(leBlob, response.P_OUT_CARTOLAS[0].NombreArchivo);
			}
		}, function(response) {
			bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),false);
			Utils.showError(response, 'Consulta de Cartolas', bcsComunes
					.ventanaWinId($scope));
		});
	}

	$scope.verCartolaHabilitado = true;
	
	$scope.obtenerOnline = function(dataItem) {
		var errors = [];

		if ($scope.IdPortafolio === null
				|| typeof $scope.IdPortafolio !== 'number') {
			errors.push('Debe seleccionar un portafolio');
		}

		if ($scope.FechaHasta === null || typeof $scope.FechaHasta !== 'object') {
			errors.push('Debe seleccionar la fecha de termino');
		}

		var fh = Utils.formatDate($scope.FechaHasta);
		var fd = fh.substring(0, 8) + '01';

//		if (fd > $scope.SesionFechaSistema || fh < $scope.SesionFechaSistema) {
//			errors.push('Fecha de termino debe ser del mes en curso');
//		}

		if (errors.length > 0) {
			Utils.showErrors(errors, 'Consulta de Cartolas', bcsComunes
					.ventanaWinId($scope));
			return;
		}

		$scope.verCartolaHabilitado = false;
		bcsComunes.ventanaOverlay(bcsComunes
				.ventanaWinId($scope),true);
		
		var aux = [{
			"IdCliente"		: $scope.ClienteSeleccionado.IdCliente,
			"IdPortafolio" 	: $scope.IdPortafolio
		}];
		
		var params = {};
		params.p_IdEmpresa 		= $scope.SesionIdEmpresa;
		params.p_IdModulo 		= 'NOR';
		params.p_IdUsuario 		= $scope.SesionIdUsuario;
		params.p_FechaDesde 	= fd;
		params.p_FechaHasta 	= fh;
		params.p_IdCliente		= $scope.ClienteSeleccionado.IdCliente;
		params.p_IdPortafolio	= $scope.IdPortafolio;
		
		bcsServiciosCartola.InformacionCliente(params,
			function(response){
				var generarCartolaOnline = function(){
					var params = {};
					params.p_IdEmpresa 		= $scope.SesionIdEmpresa;
					params.p_IdModulo 		= 'NOR';
					params.p_IdUsuario 		= $scope.SesionIdUsuario;
					params.p_AccionNomina	= 'ONLINE';
					params.p_TipoProceso	= 'ONLINE';
					params.p_FechaDesde 	= fd;
					params.p_FechaHasta 	= fh;
					params.p_Clientes		= aux;

					bcsServiciosCartola.GenerarCartola(params,
						function(response){		
							bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),false);
							if (response.P_OUT_CARTOLAS != null && response.P_OUT_CARTOLAS[0]){
								var leBlob = util.b64toBlob(response.P_OUT_CARTOLAS[0].CartolaEncoded,'application/pdf',512);
								saveAs(leBlob, response.P_OUT_CARTOLAS[0].NombreArchivo);
							}
							$scope.verCartolaHabilitado = true;
						},
						function(response){
							bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),false);
							bcsComunes.ventanaMsg({
								conflictos : util.parseServerResponse(response)
							},bcsComunes.ventanaWinId($scope));
							$scope.verCartolaHabilitado = true;
						}
					);
				};
				
				if(response && response.clienteTieneInformacion) {
					generarCartolaOnline();
				} else {
					bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope), false);
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("CLIENTE_NO_GENERA_CARTOLA")}, 
						bcsComunes.ventanaWinId($scope));			
						generarCartolaOnline();
				}
			},
			function(response) {
				bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),false);
				bcsComunes.ventanaMsg({
					conflictos : util.parseServerResponse(response)
				},bcsComunes.ventanaWinId($scope));
				$scope.verCartolaHabilitado = true;
			}
		);
	}

}
