angular.module('ModuloNormativa').controller(
		'ProcesosCartolasController',
		[ '$scope', '$http', '$translate', '$timeout', 'bcsComunes',
				'bcsAlerta', 'OPCIONES_BCSALERTA', 'Utils', 'bcsCartolas',
				ProcesosCartolasController ]);

function ProcesosCartolasController($scope, $http, $translate, $timeout,
		bcsComunes, bcsAlerta, OPCIONES_BCSALERTA, Utils, bcsCartolas) {

	var dataSourceNominas = bcsCartolas
			.getDataSourceNominas($scope.SesionIdEmpresa);
	$scope.configuracionGrillaNominas = bcsCartolas
			.getConfiguracionGrillaNominas(dataSourceNominas);
	var idVentana = bcsComunes.ventanaWinId($scope);
	$scope.limpiarFiltros = function() {
		$scope.anoProceso = '';
		$scope.tipoProceso = '';
		$scope.estadoProceso = '';
		$scope.pct.FecInicio = util.dateInicioDia($scope.SesionFechaSistema);
	}
	var pct = this;
	pct.FecInicioOptions = {format: 'yyyy', start: 'decade',depth: 'decade',buttomView: 'decade',activeView:'decade',topView:'decade'};
	pct.FecInicio 	 = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.pct=pct;
	

	$scope.buscarNominas = function() {
		var filters = new Array();
		$scope.anoProceso = $scope.pct.FecInicio.getFullYear().toString();
		filters.length = 0;
		filters.push({
			field : 'IdEmpresa',
			operator : 'eq',
			value : $scope.SesionIdEmpresa
		});

		if ($scope.anoProceso && $scope.anoProceso !== '') {
			filters.push({
				field : 'Ano',
				operator : 'eq',
				value : $scope.anoProceso
			});
		}
		
		if ($scope.tipoProceso !== '') {
			filters.push({
				field : 'TipoProceso',
				operator : 'eq',
				value : $scope.tipoProceso
			});
		}
		filters.push({
			field : 'CartolaConsolidada',
			operator : 'eq',
			value : 'n'
		});	

		filters.push({
			field : 'TipoProceso',
			operator : 'neq',
			value : 'PROGRAMADO'
		});
		
		filters.push({
			field : 'TipoProceso',
			operator : 'neq',
			value : 'ONLINE'
		});
		
		filters.push({
			field : 'TipoProceso',
			operator : 'neq',
			value : 'REEMPAQUETA'
		});
		
		if ($scope.estadoProceso !== '') {
			filters.push({
				field : 'EstadoProceso',
				operator : 'eq',
				value : $scope.estadoProceso
			});
		}

		dataSourceNominas.filter(filters);
	}

	$scope.agregarNomina = function() {
		var id = bcsComunes.ventanaAbrir('DetalleProcesoCartolas', {}, true, false, bcsComunes.ventanaWinId($scope));
		var promesa=bcsComunes.ventanaPromesa(id);
		$.when(promesa).then(function(data){
	    	 $timeout(function() {
	    		 $scope.buscarNominas();
	    	 },20);
	    });
		
	}

	$scope.mostrarDetalleNomina = function() {
		if ($scope.NominaSeleccionada === null
				|| typeof $scope.NominaSeleccionada === 'undefined') {
			Utils.showWarning('Debe seleccionar una fila de la grilla',
					'Ver Detalle Nomina', bcsComunes.ventanaWinId($scope));
			return;
		}

		bcsComunes.ventanaAbrir('DetalleProcesoCartolas', {
			ParentScope : $scope,
			ReadOnly : true,
			NominaSeleccionada : $scope.NominaSeleccionada
		}, true, false, bcsComunes.ventanaWinId($scope));
	}

	$scope.mostrarPreferencias = function() {
		bcsComunes.ventanaAbrir('PreferenciasCartolas', {}, true, false);
	}

	$scope.mostrarProgramados = function() {
		bcsComunes.ventanaAbrir('DetalleProcesoCartolas', {
			ParentScope : $scope,
			Programada : true,
			NominaSeleccionada : $scope.NominaSeleccionada
		}, true, false, bcsComunes.ventanaWinId($scope));
	}

	$scope.cancelarProceso = function() {
		if ($scope.NominaSeleccionada === null
				|| typeof $scope.NominaSeleccionada === 'undefined') {
			Utils.showWarning('Debe seleccionar una fila de la grilla',
					'Cancelación de Proceso', bcsComunes.ventanaWinId($scope));
			return;
		}

		if ($scope.NominaSeleccionada.EstadoProceso !== 'ING') {
			Utils.showWarning('Estado invalido de proceso',
					'Cancelación de Proceso', bcsComunes.ventanaWinId($scope));
			return;
		}

		cancelarProceso($scope.NominaSeleccionada);
	}

	$scope.certificarProceso = function() {
		if ($scope.NominaSeleccionada === null
				|| typeof $scope.NominaSeleccionada === 'undefined') {
			Utils
					.showWarning('Debe seleccionar una fila de la grilla',
							'Certificación de Proceso', bcsComunes
									.ventanaWinId($scope));
			return;
		}

		if ($scope.NominaSeleccionada.Certificada === 'S') {
			Utils
					.showWarning('Proceso ya certificado',
							'Certificación de Proceso', bcsComunes
									.ventanaWinId($scope));
			return;
		}

		var id = bcsComunes.ventanaAbrir('CertificarProceso', {
			NominaSeleccionada : $scope.NominaSeleccionada
		}, true, false, bcsComunes.ventanaWinId($scope));
		
		var promesa=bcsComunes.ventanaPromesa(id);
		$.when(promesa).then(function(data){
	    	 $timeout(function() {
	    		 $scope.buscarNominas();
	    	 },20);
	    });
	}

	$scope.reempaquetarProceso = function() {
		if ($scope.NominaSeleccionada === null
				|| typeof $scope.NominaSeleccionada === 'undefined') {
			Utils.showWarning('Debe seleccionar una fila de la grilla',
					'Reempaquetado de Nomina', bcsComunes.ventanaWinId($scope));
			return;
		}

		bcsComunes.ventanaAbrir('ReprocesarCartolas', {
			NominaSeleccionada : $scope.NominaSeleccionada,
			Title : 'UI_REEMPAQUETAR',
			TipoProceso : 'REEMPAQUETA',
			CallBackFunction : $scope.buscarNominas,
		}, true, false, bcsComunes.ventanaWinId($scope));
	}

	$scope.reprocesarProceso = function() {
		if ($scope.NominaSeleccionada === null
				|| typeof $scope.NominaSeleccionada === 'undefined') {
			Utils.showWarning('Debe seleccionar una fila de la grilla',
					'Reproceso de Nomina', bcsComunes.ventanaWinId($scope));
			return;
		}

		var id = bcsComunes.ventanaAbrir('ReprocesarCartolas', {
			NominaSeleccionada : $scope.NominaSeleccionada,
			Title : 'UI_REPROCESAR',
			TipoProceso : 'REPROCESO',
		}, true, false, bcsComunes.ventanaWinId($scope));
		var promesa=bcsComunes.ventanaPromesa(id);
		$.when(promesa).then(function(data){
	    	 $timeout(function() {
	    		 $scope.buscarNominas();
	    	 },20);
	    });
	}

	$scope.mostrarCartolas = function() {
		if ($scope.NominaSeleccionada === null
				|| typeof $scope.NominaSeleccionada === 'undefined') {
			Utils.showWarning('Debe seleccionar una fila de la grilla',
					'Obtención de Cartola', bcsComunes.ventanaWinId($scope));
			return;
		}

		bcsComunes.ventanaAbrir('NominasCartolas', {
			NominaSeleccionada : $scope.NominaSeleccionada
		}, true, false, bcsComunes.ventanaWinId($scope));
	}

	$scope.limpiarFiltros();

	function cancelarProceso(nominaSeleccionada) {
		bcsComunes.ventanaOverlay(idVentana,true);
		var nomina = angular.copy(nominaSeleccionada);
		nomina.IdEmpresa = $scope.SesionIdEmpresa;
		nomina.IdUsuario = $scope.SesionIdUsuario;
		nomina.AccionNomina	= "CANCELAR";
		
		bcsCartolas.gestionarNomina(nomina,
		function(reply) {
			var id = bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(reply)
				},idVentana);
			if(id){
				var promesa=bcsComunes.ventanaPromesa(id);                               
				$.when(promesa).then(function(data){
			    	 $timeout(function() {
			    		 bcsComunes.ventanaOverlay(idVentana,false);
			    		 $scope.buscarNominas();
			    	 },20);
			    });
			}else{
				bcsComunes.ventanaOverlay(idVentana,false);
	    		$scope.buscarNominas();
			}
		}, function(error) {
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_ERROR_CANCELAR_NOMINA"),
				conflictos:util.parseServerResponse(error)
			},idVentana);
			bcsComunes.ventanaOverlay(idVentana,false);
		 }
		);
	}

}