angular.module('ModuloNormativa').controller('ExportControllerRMO', function ($scope, $translate, bcsComunes, Model, Utils) {
	
	const windowParams = Utils.getWindowParams($scope);
	const datos = windowParams.datos;
	const idVentana = bcsComunes.ventanaWinId($scope);
	
	Utils.setWindowTitle($scope, $translate.instant('UI_EXPORTAR_EXCEL') + ' RMO');
	$scope.separadorListas = ';';
	$scope.simboloDecimal = ',';
	$scope.formatoFechas = 'dd-MM-yyyy';

	$scope.botonExportarHabilitado = true;
	
	$scope.exportarRMO = function() {
		$scope.botonExportarHabilitado = false;

		try {
			submitirExportacion();
		} catch (err) {
			bcsComunes.msg('error', 'Exportacion Documento', err);
		}

		$scope.botonExportarHabilitado = true;
	}

	function submitirExportacion() {
		let params = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario : $scope.SesionIdUsuario,
			p_IdModulo : datos.modulo,

			p_IdAgenda : 0,
			p_IdTipoTarea : 'ExportacionDJ',
			p_Segundos : 0,
			p_CreaParam : 'S',

			p_TablaParametros : [ {
				NombreParametro : 'idDocumento',
				Valor : datos.idDocumento
			}, {
				NombreParametro : 'idUsuario',
				Valor : $scope.SesionIdUsuario
			}, {
				NombreParametro : 'schema',
				Valor : datos.esquemaDatos
			}, {
				NombreParametro : 'separadorListas',
				Valor : $scope.separadorListas
			}, {
				NombreParametro : 'simboloDecimal',
				Valor : $scope.simboloDecimal
			}, {
				NombreParametro : 'formatoFechas',
				Valor : $scope.formatoFechas
			} ]
		};

		bcsComunes.msg('info', 'Exportacion', 'En proceso...');
		Model.agregarPrograma(params, function(reply) {
			bcsComunes.msg('info', 'Agregar Programa', 'OK');
			bcsComunes.ventanaCerrar(idVentana);
		}, function(error) {
			bcsComunes.msg('error', 'Agregar Programa', error.message);
		});
	}

});
