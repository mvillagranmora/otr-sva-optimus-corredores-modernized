angular.module('ModuloPlataforma').controller('CrearModificarEmisorCtrl',
function($scope, bcsComunes, bcsServicioPlataforma, $translate) {

	$scope.IdVentana = bcsComunes.ventanaWinId($scope);
	$scope.modo = 'INGRESAR';
	var parametros = bcsComunes.ventanaParams($scope.IdVentana);
	
	
	$scope.formData = {};
	$scope.emisor = {};
	$scope.emisorDef = {};
	$scope.persona = {};
	
	$scope.oConfiguracionEntidad={
			tipo : "combobox-contraparte",
			configuracion : {},
			filtros : {
				IdEstado      : { "field"   : "IdEstado",     "operator": "eq", "value" : "ACT" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : '0'
	};
	
	if (parametros.emisor) {
		bcsComunes.ventanaTitulo($scope.IdVentana, $translate.instant("UI_MODIFICAR_EMISOR"));
		$scope.modo = 'MODIFICAR';
		$scope.emisorDef = $.extend({}, parametros.emisor);
		$scope.emisor = $.extend({}, parametros.emisor);
		$scope.emisor.EmiteIRF = ($scope.emisor.EmiteIRF == "S" ? true : false);
		$scope.emisor.EmiteIIF = ($scope.emisor.EmiteIIF == "S" ? true : false);
		$scope.emisor.EmiteFungible = ($scope.emisor.EmiteFungible == "S" ? true : false);
		$scope.emisor.IndicadorAGF = ($scope.emisor.IndicadorAGF == "S" ? true : false); 
	}

	$scope.cancelar = function() {
		bcsComunes.ventanaCerrar($scope.IdVentana);
	}

	$scope.aceptar = function() {
			var datos = {
				p_Modo 							: $scope.modo,
				p_IdEmpresa 					: $scope.SesionIdEmpresa,
				p_IdUsuario 					: $scope.SesionIdUsuario,
				p_IdModulo 						: 'PLA',
				p_simularOperacion 				: 'N',
				p_UsaReglasNegocio 				: 'N',
				p_IdPersona 					: ($scope.modo == 'INGRESAR' ? $scope.persona.seleccionado.IdPersona : $scope.emisor.IdPersona) ,
				p_IdEmisor 						: $scope.emisor.IdEmisor,
				p_CodigoEmisor 					: $scope.emisor.CodigoEmisor || '',
				p_CodigoRegulador 				: $scope.emisor.CodigoRegulador || '',
				p_Regulador 					: $scope.emisor.Regulador || '',
				p_EmiteIIF 						: ($scope.emisor.EmiteIIF ? 'S':'N'),
				p_EmiteIRF 						: ($scope.emisor.EmiteIRF ? 'S':'N'),
				p_EmiteFungible					: ($scope.emisor.EmiteFungible ? 'S':'N'),
				p_ValorClasificacionRiesgoIIF	: $scope.emisor.ValorClasificacionRiesgoIIF || '',
				p_IndicadorAGF					: ($scope.emisor.IndicadorAGF ? 'S':'N'),
				p_IdEstado						: $scope.emisor.IdEstado || 'INA',
				p_IdEntidadClasificadoraRiesgo	: $scope.emisor.IdEntidadClasificadoraRiesgo || '',
				p_TipoEntidad					: $scope.emisor.TipoEntidad || '',
			};
			bcsComunes.ventanaOverlay($scope.IdVentana, true);
			bcsServicioPlataforma.Servicios().CrearModificarEmisor(datos).$promise.then(function(result) {
				bcsComunes.ventanaOverlay($scope.IdVentana,false);
				if (result.P_OUT_ERRORESCONTADOR === 0) {
						ventanaResultadoOK("UI_OPERACION_INGRESO_EXITO",result);
				} else {
					ventanaResultadoError("UI_ERROR_INGRESAR_INSTRUMENTO",result);
				}
			}, function(result) {
				bcsComunes.ventanaOverlay($scope.IdVentana,false);
				bcsComunes.msg("error", "UI_ERROR","UI_ERROR_INGRESAR_INSTRUMENTO");
			});
 
	}


	function ventanaResultadoOK(codigoMensaje, result) {
		var p = bcsComunes.ventanaMsg({
			mensaje : $translate.instant(codigoMensaje),
			conflictos : util.parseServerResponse(result)
		}, $scope.IdVentana);
		bcsComunes.ventanaPromesa(p).then(function() {
			bcsComunes.ventanaData($scope.IdVentana, {
				guardar : true
			});
			bcsComunes.ventanaCerrar($scope.IdVentana);
		});
	}

	function ventanaResultadoError(codigoMensaje, result) {
		bcsComunes.ventanaOverlay($scope.IdVentana, false);
		bcsComunes.ventanaMsg({
			mensaje : codigoMensaje,
			conflictos : util.parseServerResponse(result)
		}, $scope.IdVentana);
	}


});
