angular.module('ModuloNormativa').controller('ModificarReporteAPVCtrl', function($scope, $translate, bcsComunes, bcsServiciosNormativa) {

	const idVentana = bcsComunes.ventanaWinId($scope);
	const parametrosVentana = bcsComunes.ventanaParams(idVentana);
	let seleccionReporteAPV = parametrosVentana.seleccionReporteAPV;

	$scope.puedeGrabar = false;
	$scope.inicializado = false;

	$scope.data = {
		IdReporteAPV: seleccionReporteAPV[0].IdReporteAPV || 0,
		MontoSimbolo: $scope.SesionIdMonedaSistema
	};

	$scope.data.MontoDecimales = util.bcs_formato(0, 'GEN', 'Monto', $scope.data.MontoSimbolo, '', '').cantidadDecimales;

	$scope.dataForm = {};

	$scope.saldoAcumuladoBD = 0;

	const dataSource = new bcs.DataSource({
		type: "odata",
		definition: {
			serverFiltering: true,
			transport: {
				read: {
					url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010280",
					dataType: "json"
				}
			},
			filter: {
				field: "IdReporteAPV",
				operator: "eq",
				value: Number($scope.data.IdReporteAPV)
			},
			schema: {
				model: {
					fields: {
						IdReporteAPV: 					{ type: "number" },
						DocumentoIdentidad: 			{ type: "string" },
						NombreCliente: 					{ type: "string" },
						CodigoRegimenTributario:		{ type: "string" },
						NombreRegimenTributario: 		{ type: "string" },
						TipoProducto: 					{ type: "string" },
						IdPlanTipoInversion: 			{ type: "number" },
						NombrePlanTipoInversion: 		{ type: "string" },
						FechaCierre: 					{ type: "sysdate" },
						SaldoAcumulado:	 				{ type: "number" },
						SaldoRegimenTributarioA: 		{ type: "number" },
						SaldoRegimenTributarioB: 		{ type: "number" },
						SaldoBonificacionEstatal: 		{ type: "number" },
						SaldoAPVCEmpleador: 			{ type: "number" },
						SaldoAPVCTrabajadorA: 			{ type: "number" },
						SaldoAPVCTrabajadorB: 			{ type: "number" },
						SaldoAPVCBonificacionEstatal: 	{ type: "number" }
					}
				}
			}
		}
	});


	dataSource.fetch().then(function() {
		if (dataSource.data().length !== 1) {
			console.error("Reporte APV no existe");
			return;
		}

		$scope.$apply(function() {

			const r = dataSource.data()[0];
			angular.extend($scope.data, r);

			$scope.saldoAcumuladoBD = formatear(r.SaldoAcumulado);

			$scope.dataForm = {
				IdReporteAPV: r.IdReporteAPV,
				DocumentoIdentidad: r.DocumentoIdentidad,
				NombreCliente: r.NombreCliente,
				CodigoRegimenTributario: r.CodigoRegimenTributario,
				NombreRegimenTributario: r.NombreRegimenTributario,
				TipoProducto: r.TipoProducto,
				IdPlanTipoInversion: r.IdPlanTipoInversion,
				NombrePlanTipoInversion: r.NombrePlanTipoInversion,
				FechaCierre: r.FechaCierre,
				SaldoAcumulado: formatear($scope.saldoAcumuladoBD),
				SaldoRegimenTributarioA: formatear(r.SaldoRegimenTributarioA),
				SaldoRegimenTributarioB: formatear(r.SaldoRegimenTributarioB),
				SaldoBonificacionEstatal: formatear(r.SaldoBonificacionEstatal),
				SaldoAPVCEmpleador: formatear(r.SaldoAPVCEmpleador),
				SaldoAPVCTrabajadorA: formatear(r.SaldoAPVCTrabajadorA),
				SaldoAPVCTrabajadorB: formatear(r.SaldoAPVCTrabajadorB),
				SaldoAPVCBonificacionEstatal: formatear(r.SaldoAPVCBonificacionEstatal)
			};

			$scope.puedeGrabar = false;
			$scope.inicializado = true;
		});
	});

	$scope.modificarReporteAPV = function() {

		const datos = {
			p_IdEmpresa: $scope.SesionIdEmpresa,
			p_IdUsuario: $scope.SesionIdUsuario,
			p_IdModulo: 'NOR',
			p_ReporteAPV: [{
				IdReporteAPV: $scope.dataForm.IdReporteAPV,
				DocumentoIdentidad: $scope.dataForm.DocumentoIdentidad,
				NombreCliente: $scope.dataForm.NombreCliente,
				CodigoRegimenTributario: $scope.dataForm.CodigoRegimenTributario,
				NombreRegimenTributario: $scope.dataForm.NombreRegimenTributario,
				TipoProducto: $scope.dataForm.TipoProducto,
				IdPlanTipoInversion: $scope.dataForm.IdPlanTipoInversion,
				NombrePlanTipoInversion: $scope.dataForm.NombrePlanTipoInversion,
				FechaCierre: $scope.dataForm.FechaCierre,
				SaldoRegimenTributarioA: formatearNumero($scope.dataForm.SaldoRegimenTributarioA),
				SaldoRegimenTributarioB: formatearNumero($scope.dataForm.SaldoRegimenTributarioB),
				SaldoBonificacionEstatal: formatearNumero($scope.dataForm.SaldoBonificacionEstatal),
				SaldoAPVCEmpleador: formatearNumero($scope.dataForm.SaldoAPVCEmpleador),
				SaldoAPVCTrabajadorA: formatearNumero($scope.dataForm.SaldoAPVCTrabajadorA),
				SaldoAPVCTrabajadorB: formatearNumero($scope.dataForm.SaldoAPVCTrabajadorB),
				SaldoAPVCBonificacionEstatal: formatearNumero($scope.dataForm.SaldoAPVCBonificacionEstatal),
				SaldoAcumulado: Number($scope.data.SaldoAcumulado),
				IdUsuarioModificacion: $scope.SesionIdUsuario,
				FechaModificacion: util.dateSPdate(new Date())
			}]
		};

		bcsComunes.ventanaOverlay(idVentana, true);

		bcsServiciosNormativa.ModificarReporteAPV(datos).$promise.then(
			function() {
				bcsComunes.ventanaMsg({
					mensaje: $translate.instant("UI_REPORTE_APV_MODIFICADO")
				}, idVentana);
				bcsComunes.ventanaOverlay(idVentana, false);
				bcsComunes.ventanaCerrar(idVentana);
			},
			function(err) {
				bcsComunes.ventanaMsg({
					conflictos: util.parseServerResponse(err)
				}, idVentana);
				bcsComunes.ventanaOverlay(idVentana, false);
			}
		);
	};

	$scope.$watchGroup([
		'dataForm.SaldoRegimenTributarioA', 'dataForm.SaldoRegimenTributarioB', 'dataForm.SaldoBonificacionEstatal',
		'dataForm.SaldoAPVCEmpleador', 'dataForm.SaldoAPVCTrabajadorA', 'dataForm.SaldoAPVCTrabajadorB', 'dataForm.SaldoAPVCBonificacionEstatal'
	], function(valores) {
		if (!$scope.inicializado) return;
		let total = 0;
		valores.forEach(valor => total += formatearNumero(valor) || 0);

		let totalFormateado = formatear(total);

		$scope.data.SaldoAcumulado = total;
		$scope.dataForm.SaldoAcumulado = totalFormateado;
		$scope.puedeGrabar = (totalFormateado !== $scope.saldoAcumuladoBD);
	});

	function formatear(valor) {
		return util.bcs_formato(Number(valor) || 0, 'GEN', 'Monto', $scope.data.MontoSimbolo, '', '').valor;
	}

	function formatearNumero(valor) {
		return Number((valor || '0').toString().replace(/\./g, '').replace(',', '.')) || 0;
	}
});
