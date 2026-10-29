angular.module('ModuloPlataforma').controller(
		'MantenedorCuponesCtrl',
		[ '$resource', '$scope', '$translate', 'bcsComunes', 'bcsAlerta',
				'OPCIONES_BCSALERTA', 'bcsServicioPlataforma',
				MantenedorCuponesCtrl ]);

function MantenedorCuponesCtrl($resource, $scope, $translate, bcsComunes,
		bcsAlerta, OPCIONES_BCSALERTA, bcsServicioPlataforma) {

	$scope.IdVentana = bcsComunes.ventanaWinId($scope);
	var parametros = bcsComunes.ventanaParams($scope.IdVentana);
	$scope.modo = parametros.modo;
	$scope.instrumento = parametros.instrumento;
	$scope.bono = parametros.bono;

	var schemaCupones = {
		model : {
			fields : {
				IdCupon : {
					type : 'number'
				},
				Nemotecnico : {
					type : 'string'
				},
				Numero : {
					type : 'number'
				},
				Fecha : {
					type : 'date'
				},
				Call : {
					type : 'string'
				},
				Intereses : {
					type : 'number'
				},
				Amortizacion : {
					type : 'number'
				}
			}
		}
	};

	var dataSourceCupones = new bcs.DataSource({
		type : 'odata',
		definition : {
			transport : {
				read : {
					url : '/BSCL/Plataforma/GWJAVA/PLA/call?t=4144',
					dataType : 'json'
				}
			},
			serverFiltering : true,
			serverPaging : true,
			serverSorting : true,
			filter : {
				field : 'Nemotecnico',
				operator : 'eq',
				value : $scope.instrumento.Nemotecnico
			},
			sort : {
				field : "Fecha",
				dir : "asc"
			},
			pageSize : 200,
			schema : schemaCupones
		}
	});

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
			field : 'Numero',
			title : 'Numero',
			width : '50px',
			editable : false
		}, {
			field : 'Fecha',
			title : 'Fecha',
			width : '100px',
			editable : true
		}, {
			field : 'Call',
			title : 'Call',
			width : '50px',
			editable : true
		}, {
			field : 'Intereses',
			title : 'Intereses',
			format : '{0:##0.0000000000}',
			width : '100px',
			editable : true
		}, {
			field : 'Amortizacion',
			title : 'Amortizacion',
			format : '{0:##0.0000000000}',
			editable : true
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
		sortable : true,
		gridCreated : function(g) {
			dataSourceCupones.read().then(function() {
				var cupones = bcs.cleanData(dataSourceCupones.data());
				if (cupones.length === 0) {
					cupones = generarBullet($scope.instrumento, $scope.bono);
				}

				g.dataSource.data(cupones);
			});
		}
	};

	$scope.aceptar = function() {
		arrayDataSourceCupones.sort({
			field : "Fecha",
			dir : "asc"
		});

		var cupones = [];
		var saldo = 100;
		var data = arrayDataSourceCupones.view();
		for (var i = 0; i < data.length; i++) {
			var cupon = data[i];
			saldo -= cupon.Amortizacion;
			cupon.SaldoPorAmortizar = saldo;
			cupon.Numero = i + 1;
			cupones.push(cupon);
		}

		var datos = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario : $scope.SesionIdUsuario,
			p_IdModulo : 'PLA',
			p_Nemotecnico : $scope.instrumento.Nemotecnico,
			p_Cupones : cupones
		};

		bcsComunes.ventanaOverlay($scope.IdVentana, true);

		bcsServicioPlataforma.Servicios().GuardarCupones(datos).$promise.then(
				function(result) {
					bcsComunes.ventanaOverlay($scope.IdVentana, false);
					if (result.P_OUT_ERRORESCONTADOR === 0) {
						var p = bcsComunes.ventanaMsg({
							mensaje : $translate
									.instant("UI_CUPONES_GUARDADOS")
									+ " : " + $scope.instrumento.Nemotecnico,
							conflictos : util.parseServerResponse(result)
						}, $scope.IdVentana);
						bcsComunes.ventanaPromesa(p).then(function() {
							bcsComunes.ventanaData($scope.IdVentana, {
								guardar : true
							});
							bcsComunes.ventanaCerrar($scope.IdVentana);
						});
					} else {
						bcsComunes.ventanaOverlay($scope.IdVentana, false);
						bcsComunes.ventanaMsg({
							mensaje : "UI_ERROR_GUARDAR_CUPONES",
							conflictos : util.parseServerResponse(result)
						}, $scope.IdVentana);
					}
				}, function(result) {
					bcsComunes.ventanaOverlay($scope.IdVentana, false);
					bcsComunes.msg("error", "UI_ERROR",
							"UI_ERROR_GUARDAR_CUPONES");
				});
	}

	$scope.cancelar = function() {
		bcsComunes.ventanaCerrar($scope.IdVentana);
	}

	$scope.validar = function() {
		var suma = 0;
		var data = arrayDataSourceCupones.data();
		for (var i = 0; i < data.length; i++) {
			var cupon = data[i];
			suma += cupon.Amortizacion;
		}

		bcsComunes.ventanaOverlay($scope.IdVentana, false);
		if (suma < 100 || suma > 100) {
			bcsComunes.msg("error", "UI_ERROR",
					"Total de amortizaciones debe ser 100");
		} else {
			bcsComunes.msg("info", "UI_INFORMACION",
					"Total de amortizaciones es 100");
		}
	}

	$scope.VentaDetalleCarga = function() {
		var datos = [];
		var datosArchivo = {
			codigoArchivo : 'CARGA_CUPONES',
			formatoArchivoDefecto : '.csv'
		};
		var IdVentanamodal = bcsComunes.ventanaAbrir("CargaArchivosGeneral",
				datosArchivo, true, false, $scope.idVentana);
		var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
		promesa.then(function(data) {

			if (!data || !data.fileData) {
				return;
			}
			$.each(data.fileData, function(key, value) {
				////////////////////////
				if(isNaN(value.UI_INTERESES)){
					value.UI_INTERESES = "0";
				}
				if(isNaN(value.UI_AMORTIZACION)){
					value.UI_AMORTIZACION = "0";
				}
				if(value.UI_CALL==="n" || value.UI_CALL==="s"){
					value.UI_CALL = ((fieldConfig.ForzarMayuscula && fieldConfig.ForzarMayuscula=='S')? String(value.UI_CALL).toUpperCase() : String(value.UI_CALL)).replace("\r","");
				}else if(value.UI_CALL==="" || value.UI_CALL==="0" || isNaN(value.UI_CALL)){
					value.UI_CALL = "N";
				} 
				
				
				////////////////////////
				datos.push({
					"Numero" : value.UI_NUMERO_CUPNES,
					"Fecha" : util.date( value.UI_FECHA),
					"Call" : value.UI_CALL.toString(),
					"Intereses" : value.UI_INTERESES,
					"Amortizacion" : value.UI_AMORTIZACION
				});
			});
			 
			$scope.configuracionGrillaCupones.dataSource.data(datos);
		});
	};

	function isLeapYear(y) {
		return (0 === y % 4 && 0 !== y % 100) || (0 === y % 400);
	}

	function daysInMonth(y, m) {
		switch (m) {
		case 0:
			return 31;
		case 1:
			return isLeapYear(y) ? 29 : 28;
		case 2:
			return 31;
		case 3:
			return 30;
		case 4:
			return 31;
		case 5:
			return 30;
		case 6:
			return 31;
		case 7:
			return 31;
		case 8:
			return 30;
		case 9:
			return 31;
		case 10:
			return 30;
		case 11:
			return 31;
		default:
			return -1;
		}
	}

	function calcularFecha(instrumento, bono, fecha, numero) {
		if (numero == 1) {
			return bono.FechaDevengo;
		}

		var y = fecha.getFullYear();
		var m = fecha.getMonth();
		var d = fecha.getDate();

		for (var i = 0; i < instrumento.FrecuenciaCupon; i++) {
			m--;
			if (m < 0) {
				m = 11;
				y--;
			}
		}

		var dim = daysInMonth(y, m);
		if (d > dim) {
			d = dim;
		}

		return new Date(y, m, d, 0, 0, 0, 0);
	}

	function generarBullet(instrumento, bono) {
		var cupones = [];
		var fecha = instrumento.FechaVencimiento;
		var intereses = bono.TasaEmision * instrumento.FrecuenciaCupon / 12.0;

		for (var n = instrumento.NumeroCupones; n > 0; n--) {
			var cupon = {};
			cupon.Numero = n;
			cupon.Fecha = fecha;
			cupon.Call = 'N';
			cupon.Intereses = intereses;
			cupon.Amortizacion = n == instrumento.NumeroCupones ? 100 : 0;
			cupon.SaldoPorAmortizar = n == instrumento.NumeroCupones ? 0 : 100;
			cupones.push(cupon);

			fecha = calcularFecha(instrumento, bono, fecha, n);
		}

		return cupones.reverse();
	}

}
