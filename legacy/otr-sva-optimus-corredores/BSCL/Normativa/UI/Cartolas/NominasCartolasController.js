angular.module('ModuloNormativa').controller(
		'NominaCartolasController',
		[ '$scope', '$http', '$translate', '$timeout', 'bcsComunes',
				'bcsAlerta', 'OPCIONES_BCSALERTA', 'Utils', 'bcsCartolas','bcsServiciosCartola',
				NominaCartolasController ]);

function NominaCartolasController($scope, $http, $translate, $timeout,
		bcsComunes, bcsAlerta, OPCIONES_BCSALERTA, Utils, bcsCartolas, bcsServiciosCartola) {

	var windowParams = Utils.getWindowParams($scope);
	var idVentana = bcsComunes.ventanaWinId($scope);
	var dataSourceCartolas = bcsCartolas
			.getDataSourceCartolas(windowParams.NominaSeleccionada.IdNomina);
	$scope.configuracionGrillaCartolas = bcsCartolas
			.getConfiguracionGrillaCartolas(dataSourceCartolas);
	
	$scope.Nomina = windowParams.NominaSeleccionada;
	$scope.verCartolaHabilitado = true;
	$scope.verCartola = function() {
		if (typeof $scope.CartolasSeleccionadas === 'undefined' || !$scope.CartolasSeleccionadas[0]) {
			Utils.showWarning('Debe seleccionar una fila de la grilla',
					'Consulta de Cartola', bcsComunes.ventanaWinId($scope));
			return;
		}

		$scope.verCartolaHabilitado = false;
		bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),true);
		var params = {};
		bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),true);
		params.p_IdEmpresa = $scope.SesionIdEmpresa;
		params.p_IdUsuario = $scope.SesionIdUsuario;
		params.p_IdCartola = $scope.CartolasSeleccionadas[0].IdCartola;
		params.p_AccionNomina	= 'OFFLINE';
		bcsServiciosCartola.GenerarCartola(params, function(response) {
			bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),false);
			if (response.P_OUT_CARTOLAS != null && response.P_OUT_CARTOLAS[0]){
				var leBlob = util.b64toBlob(response.P_OUT_CARTOLAS[0].CartolaEncoded,'application/pdf',512);
				saveAs(leBlob, response.P_OUT_CARTOLAS[0].NombreArchivo);
			}
			$scope.verCartolaHabilitado = true;
		}, function(response) {
			bcsComunes.msg('error', 'Obtener Cartola', response.statusText);
			$scope.verCartolaHabilitado = true;
			bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),false);
		});
	}

	$scope.marcarReproceso = function(valor) {
		if (windowParams.NominaSeleccionada.Certificada === 'S') {
			Utils.showWarning('Proceso ya certificado',
					'Marcar/Quitar Reproceso', bcsComunes.ventanaWinId($scope));
			return;
		}

		if (typeof $scope.CartolasSeleccionadas === 'undefined') {
			Utils.showWarning('Debe seleccionar una fila de la grilla',
					'Reproceso de Nomina', bcsComunes.ventanaWinId($scope));
			return;
		}

		var cartolas = [];
		for (i = 0; i < $scope.CartolasSeleccionadas.length; i++) {
			var cartola = $scope.CartolasSeleccionadas[i];
			cartolas.push({IdCartola: cartola.IdCartola, Valor: valor});
		}

		if (cartolas.length > 0) {
			actualizarReproceso(cartolas);
		}
	}

	$scope.verFiltroNomina = function() {
		bcsComunes.ventanaAbrir('DetalleProcesoCartolas', {
			NominaSeleccionada : windowParams.NominaSeleccionada,
			ReadOnly : true
		}, true, false, bcsComunes.ventanaWinId($scope));
	}

	function actualizarReproceso(cartolas) {
		bcsComunes.ventanaOverlay(idVentana,true);
		bcsCartolas.actualizarReproceso($scope.SesionIdEmpresa,
				$scope.SesionIdUsuario, cartolas, function(reply) {
					var id = bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(reply)
					},idVentana);
					if(id){
						var promesa=bcsComunes.ventanaPromesa(id);                               
						$.when(promesa).then(function(data){
					    	 $timeout(function() {
					    		 bcsComunes.ventanaOverlay(idVentana,false);
					    		 buscarCartolas();
					    	 },20);
					    });
					}else{
						bcsComunes.ventanaOverlay(idVentana,false);
						buscarCartolas();
					}
					
				}, function(error) {
					Utils.showError(error, 'ERROR_ACTUALIZAR_REPROCESO',
							idVentana);
				});
	}
	
	function buscarCartolas(){
		var filters = [ {
			field : 'IdNomina',
			operator : 'eq',
			value : windowParams.NominaSeleccionada.IdNomina
		} ];
		dataSourceCartolas.filter(filters);
	}
}