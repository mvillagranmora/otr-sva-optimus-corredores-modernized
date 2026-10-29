angular.module('ModuloPlataforma')
		.controller(
				'ValorizadorCarteraController',
				[ '$http', '$resource', '$scope', '$translate', 'bcsComunes',
						'bcsAlerta', 'OPCIONES_BCSALERTA', 'bcsServicioPlataforma',
						ValorizadorCarteraController ]);

function ValorizadorCarteraController($http, $resource, $scope, $translate,
		bcsComunes, bcsAlerta, OPCIONES_BCSALERTA, bcsServicioPlataforma) {

	$scope.IdVentana = bcsComunes.ventanaWinId($scope);
	$scope.fecha = new Date();

	$scope.tiposDeBonos = null;
	bcsServicioPlataforma.TiposDeBonos(function(arr) {
		$scope.tiposDeBonos = arr;
	});

	var schemaCartera = {
		model : {
			fields : {
				nemo : {
					type : 'string'
				},
				fecha : {
					type : 'date'
				},
				valorNominal : {
					type : 'number'
				},
				precio : {
					type : 'number'
				},
				tasa : {
					type : 'number'
				},
				monto : {
					type : 'number'
				},
				numero : {
					type : 'number'
				}
			}
		}
	};

	var data = [];

	var arrayDataSourceCartera = new bcs.DataSource({
		type : "array",
		definition : {
			data : data,
			sort : {
				field : "Fecha",
				dir : "asc"
			},
			pageSize : 200,
			schema : schemaCartera
		}
	});

	$scope.configuracionGrillaCartera = {
		dataSource : arrayDataSourceCartera,
		columns : [ {
			field : 'nemo',
			title : 'Nemo',
			width : '150px',
			editable : false
		}, {
			field : 'fecha',
			title : 'Fecha',
			width : '100px',
			editable : true
		}, {
			field : 'valorNominal',
			title : 'Valor Nominal',
			width : '120px',
			format : '{0:###,###,###,##0}',
			editable : true
		}, {
			field : 'precio',
			title : 'Precio',
			width : '80px',
			format : '{0:##0.00}',
			editable : true
		}, {
			field : 'tasa',
			title : 'Tasa',
			width : '80px',
			format : '{0:##0.00}',
			editable : false
		}, {
			field : 'monto',
			title : 'Monto',
			format : '{0:###,###,###,##0.00}',
			editable : false
		} ],
		autoBind : true,
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
		sortable : true,
		gridCreated : function(g) {
			$scope.grilla = g;
		}
	};

	$scope.cancelar = function() {
		bcsComunes.ventanaCerrar($scope.IdVentana);
	}

	$scope.agregar = function() {
		var params = null;
		if ($scope.params.Precio != null) {
			params = {
				nemo : $scope.form.instrumento.Nemotecnico,
				fecha : $scope.params.Fecha,
				valorNominal : $scope.params.ValorNominal,
				precio : $scope.params.Precio,
				tasa : null
			}
		} else if ($scope.params.Tasa != null) {
			params = {
				nemo : $scope.form.instrumento.Nemotecnico,
				fecha : $scope.params.Fecha,
				valorNominal : $scope.params.ValorNominal,
				precio : null,
				tasa : $scope.params.Tasa
			}
		}

		if (params != null) {
			data.push(params);
			for (var i = 0; i < data.length; i++) {
				data[i].numero = i;
			}

			$scope.grilla.dataSource.data(data);
		}
	}

	$scope.eliminar = function() {
		if ($scope.filaSeleccionada != null) {
			var d = [];
			for (var i = 0; i < data.length; i++) {
				if (i != $scope.filaSeleccionada.numero) {
					d.push(data[i]);
				}
			}

			data = d;
			$scope.grilla.dataSource.data(data);
		}
	}

	$scope.valorizar = function() {
		for (var i = 0; i < data.length; i++) {
			valorizar(data[i]);
		}
	}

	function valorizar(params) {
		var fechaValorizacion = params.fecha.getFullYear() + "-"
				+ (params.fecha.getMonth() + 1) + "-" + params.fecha.getDate();
		var p = {
			nemo : params.nemo,
			fechaValorizacion : fechaValorizacion,
			valorNominal : params.valorNominal,
			precio : params.precio,
			tasa : params.tasa
		}

		llamarValorizador(p, function(dat) {
			if (dat.codigoRetorno && dat.codigoRetorno !== 0) {
				bcsComunes.ventanaOverlay($scope.IdVentana, false);
				bcsComunes.msg("error", "UI_ERROR", dat.mensaje);
			} else {
				params.precio = dat.precio;
				params.tasa = dat.tasa;
				params.monto = dat.montoTotal;

				$scope.grilla.dataSource.data(data);
			}
		}, function(result) {
			bcsComunes.ventanaOverlay($scope.IdVentana, false);
			bcsComunes.msg("error", "UI_ERROR",
					"UI_ERROR_AL_VALORIZAR_INSTRUMENTO");
		});
	}

	function llamarValorizador(params, fnSuccess, fnError) {
		var url = "/BSCL/Plataforma/GWJAVA/SERVLETS/valo2/valorizar";
		$http({
			method : 'GET',
			url : url,
			params : params
		}).success(fnSuccess).error(fnError);
	}
}
