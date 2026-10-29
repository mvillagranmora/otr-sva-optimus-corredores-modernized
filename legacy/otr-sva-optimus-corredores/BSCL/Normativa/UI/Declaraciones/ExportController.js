angular.module('ModuloNormativa').controller(
		'ExportController',
		[ '$scope', '$translate', 'bcsComunes', 'Model', 'Utils',
				ExportController ]);

function ExportController($scope, $translate, bcsComunes, Model, Utils) {
	
	var windowParams = Utils.getWindowParams($scope);
	
	if(windowParams.Model){
		Model = windowParams.Model
	}

	Utils.setWindowTitle($scope, $translate.instant('UI_EXPORTAR_EXCEL') + ' '
			+ Model.getEsquema() + '-' + Model.getVersion());

	$scope.separadorListas = ';';
	$scope.simboloDecimal = ',';
	$scope.formatoFechas = 'dd-MM-yyyy';

	$scope.botonExportarHabilitado = true;
	$scope.exportar = function() {
		$scope.botonExportarHabilitado = false;

		try {
			submitirExportacion();
		} catch (err) {
			bcsComunes.msg('error', 'Exportacion Documento', err);
		}

		$scope.botonExportarHabilitado = true;
	}

	function submitirExportacion() {
		var params = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario : $scope.SesionIdUsuario,
			p_IdModulo : Model.getModulo(),

			p_IdAgenda : 0,
			p_IdTipoTarea : 'ExportacionDJ',
			p_Segundos : 0,
			p_CreaParam : 'S',

			p_TablaParametros : [ {
				NombreParametro : 'idDocumento',
				Valor : Model.getDocumento().IdDocumento
			}, {
				NombreParametro : 'idUsuario',
				Valor : $scope.SesionIdUsuario
			}, {
				NombreParametro : 'schema',
				Valor : Model.getSchema()
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
		}, function(error) {
			bcsComunes.msg('error', 'Agregar Programa', error.message);
		});
	}

}
