angular.module('ModuloPlataforma').controller(
		'ConsultaCuponesCtrl',
		[ '$http', '$resource', '$scope', '$translate', 'bcsComunes',
				'bcsAlerta', 'OPCIONES_BCSALERTA', 'bcsServicioPlataforma',
				ConsultaCuponesCtrl ]);

function ConsultaCuponesCtrl($http, $resource, $scope, $translate, bcsComunes,
		bcsAlerta, OPCIONES_BCSALERTA, bcsServicioPlataforma) {

	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	$scope.fecha = new Date();

	$scope.tiposDeBonos = null;
	bcsServicioPlataforma.TiposDeBonos(function(arr) {
		$scope.tiposDeBonos = arr;
	});

	var schemaCupones = {
		model : {
			fields : {
				nemo : {
					type : 'string'
				},
				numero : {
					type : 'number'
				},
				fecha : {
					type : 'string'
				},
				call : {
					type : 'string'
				},
				intereses : {
					type : 'number'
				},
				amortizacion : {
					type : 'number'
				}
			}
		}
	};

	var arrayDataSourceCupones = new bcs.DataSource({
		type : "array",
		definition : {
			data : [],
			sort : {
				field : "Fecha",
				dir : "asc"
			},
			pageSize : 200,
			schema : schemaCupones
		}
	});

	$scope.configuracionGrillaCupones = {
		dataSource : arrayDataSourceCupones,
		columns : [ {
			field : 'numero',
			title : 'Numero',
			width : '50px',
			editable : false
		}, {
			field : 'fecha',
			title : 'Fecha',
			width : '100px',
			editable : false
		}, {
			field : 'call',
			title : 'Call',
			width : '50px',
			editable : false
		}, {
			field : 'intereses',
			title : 'Intereses',
			width : '100px',
			format : '{0:###,###,###,##0.0000000000}',
			editable : false
		}, {
			field : 'amortizacion',
			title : 'Amortizacion',
			format : '{0:###,###,###,##0.0000000000}',
			editable : false
		} ],
		allowCopy : true,
		userSettings : true,
		navigatable : true,
		excel : {
			allPages : true
		},
		filterable : false,
		height : 250,
		pageable : true,
		reorderable : true,
		resizable : true,
		scrollable : true,
		selectable : "row",
		sortable : true
	};

	$scope.buscar = function() {
		var fecha = $scope.fecha.getFullYear() + "-"
				+ ($scope.fecha.getMonth() + 1) + "-" + $scope.fecha.getDate();

		var params = {
			nemo : $scope.instrumento.Nemotecnico,
			fecha : fecha,
			valorNominal : $scope.valorNominal
		}

		consultarCupones(params, function(data) {
			if (data.codigoRetorno && data.codigoRetorno !== 0) {
				bcsComunes.ventanaOverlay($scope.idVentana, false);
				bcsComunes.msg("error", "UI_ERROR", data.mensaje);
			} else {
				for (var i = 0; i < data.length; i++) {
					data[i].call = data[i].call ? "S" : "N";
				}

				arrayDataSourceCupones.data(data);
			}
		}, function(result) {
			bcsComunes.ventanaOverlay($scope.idVentana, false);
			bcsComunes.msg("error", "UI_ERROR", "UI_ERROR_CONSULTA_CUPONES");
		});
	}

	function consultarCupones(params, fnSuccess, fnError) {

		
		var url = "/BSCL/Plataforma/GWJAVA/SERVLETS/valo2/cupones";
		$http({
			method : 'GET',
			url : url,
			params : params
		}).success(fnSuccess).error(fnError);
	}
}
