angular.module('ModuloNormativa').controller('MantenedorReporteAPVCtrl', function($scope, bcsComunes, $translate, bcsServiciosNormativa, bcsServicioPlataforma, bcsAlerta, OPCIONES_BCSALERTA)
{
	$scope.filtros = {};
	const idVentana = bcsComunes.ventanaWinId($scope);
	const idMonedaSistema = $scope.SesionIdMonedaSistema;
	$scope.filtros.kngFechaSistema 	= util.dateInicioDia($scope.SesionFechaSistema);
	bcsComunes.ventanaAutoHeight(idVentana);
	
	let dsReporteAPV = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: true, // Ordenamiento de los datos en el servidor.						
			sort: { field: "DocumentoIdentidad", dir: "desc" },
			pageSize : 100,
			transport: {
				read: {
					url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010280",
					dataType: "json"
				}
			},
			schema : {
				model : {
					fields : {
						IdReporteAPV					: { type: "number"},
						DocumentoIdentidad				: { type: "string"},
						NombreCliente					: { type: "string"},
						CodigoRegimenTributario			: { type: "string"},
						NombreRegimenTributario			: { type: "string"},
						TipoProducto					: { type: "string"},
						IdPlanTipoInversion				: { type: "number"},
						NombrePlanTipoInversion			: { type: "string"},
						FechaCierre						: { type: "sysdate"},
						SaldoAcumulado					: { type: "number"},
						SaldoRegimenTributarioA			: { type: "number"},
						SaldoRegimenTributarioB			: { type: "number"},
						SaldoBonificacionEstatal		: { type: "number"},
						SaldoAPVCEmpleador				: { type: "number"},
						SaldoAPVCTrabajadorA			: { type: "number"},
						SaldoAPVCTrabajadorB			: { type: "number"},
						SaldoAPVCBonificacionEstatal	: { type: "number"}
					}
				}
			}
		}
	});

	$scope.columnasReporteAPV = function() {
		return  [
		        {field: "IdReporteAPV", 				title: 'UI_ID_REPORTE_APV',					width: "70px",  hidden: true},
		        {field: "FechaCierre", 					title: 'UI_FECHA_CIERRE',					width: "80px",  hidden: true},
		        {field: "DocumentoIdentidad", 			title: 'UI_RUT',							width: "70px",  groupable: true},
		        {field: "NombreCliente", 				title: 'UI_CLIENTE',						width: "200px", groupable: true},
		        {field: "CodigoRegimenTributario", 		title: 'UI_ID_TIPO_APV',					width: "80px",  hidden: true},
		        {field: "NombreRegimenTributario", 		title: 'UI_TIPO_APV', 						width: "140px", groupable: true},
		        {field: "TipoProducto", 				title: 'UI_TIPO_PRODUCTO',					width: "220px", groupable: true},
		        {field: "IdPlanTipoInversion", 			title: 'UI_ID_PLAN_AHORRO',					width: "80px",  hidden: true},
		        {field: "NombrePlanTipoInversion", 		title: 'UI_PLAN_AHORRO',					width: "80px",  groupable: true},
		        {field: "SaldoAcumulado", 				title: 'UI_SALDO_ACUMULADO',				width: "200px", template : " <label> #: util.bcs_formato(SaldoAcumulado,'GEN','Monto','"+idMonedaSistema+"','','').valor #</label>"},
		        {field: "SaldoRegimenTributarioA",		title: 'UI_SALDO_REGIMEN_TRIBUTARIO_A',		width: "200px", template : " <label> #: util.bcs_formato(SaldoRegimenTributarioA,'GEN','Monto','"+idMonedaSistema+"','','').valor #</label>"},
		        {field: "SaldoRegimenTributarioB",		title: 'UI_SALDO_REGIMEN_TRIBUTARIO_B', 	width: "200px", template : " <label> #: util.bcs_formato(SaldoRegimenTributarioB,'GEN','Monto','"+idMonedaSistema+"','','').valor #</label>"},
		        {field: "SaldoBonificacionEstatal",		title: 'UI_SALDO_BONIFICACION_ESTATAL',		width: "200px", template : " <label> #: util.bcs_formato(SaldoBonificacionEstatal,'GEN','Monto','"+idMonedaSistema+"','','').valor #</label>"},
		        {field: "SaldoAPVCEmpleador",			title: 'UI_SALDO_APVC_EMPLEADOR',			width: "200px", template : " <label> #: util.bcs_formato(SaldoAPVCEmpleador,'GEN','Monto','"+idMonedaSistema+"','','').valor #</label>"},
		        {field: "SaldoAPVCTrabajadorA",			title: 'UI_SALDO_APVC_TRABAJADOR_A',		width: "200px", template : " <label> #: util.bcs_formato(SaldoAPVCTrabajadorA,'GEN','Monto','"+idMonedaSistema+"','','').valor #</label>"},
		        {field: "SaldoAPVCTrabajadorB",			title: 'UI_SALDO_APVC_TRABAJADOR_B',		width: "200px", template : " <label> #: util.bcs_formato(SaldoAPVCTrabajadorB,'GEN','Monto','"+idMonedaSistema+"','','').valor #</label>"},
		        {field: "SaldoAPVCBonificacionEstatal",	title: 'UI_SALDO_APVC_BONIFICACION_ESTATAL',width: "200px", template : " <label> #: util.bcs_formato(SaldoAPVCBonificacionEstatal,'GEN','Monto','"+idMonedaSistema+"','','').valor #</label>"}
		];
	};
	
	$scope.confBasicaReporteAPV = {
		dataSource  : dsReporteAPV,
		columns 	: $scope.columnasReporteAPV(),
		sortable 	: true,
		pageable 	: true,
		filterable	: true,
		selectable 	: "multiple row",
		groupable	: true,
		height 		: 520,
		autoBind 	: false,
		allowCopy	: true,
		excel : {
			allPages: true,
		},
		userSettings: true,
		scrollable	: true
	};

	$scope.buscar = function(){
		let filters = new Array();
		if($scope.filtros.kngFechaSistema)
			filters.push({field: "FechaCierre", operator: "eq", value: util.dateSPdate($scope.filtros.kngFechaSistema)});
						
		dsReporteAPV.filter(filters);
	};

	$scope.modificarReporteAPV = function () {
		let id = bcsComunes.ventanaAbrir("ModificarReporteAPV", { seleccionReporteAPV: $scope.seleccionReporteAPV }, false, false, idVentana);
		bcsComunes.ventanaPromesa(id).then(function() {
			$scope.buscar();
		});
	};

	$scope.eliminarRegistroAPV = function () {
		let pregunta = bcsAlerta.crear('PREGUNTA', $translate.instant('UI_SE_ELIMINARAN_REPORTES_APV'), idVentana);

		pregunta.then(function (result) {
			if (result === OPCIONES_BCSALERTA.CANCELAR) {
				return;
			}

			const datos = {
				p_IdEmpresa: $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: 'NOR',
				p_ReporteAPV: $scope.seleccionReporteAPV.map(item => ({
					IdReporteAPV: item.IdReporteAPV,
					DocumentoIdentidad: item.DocumentoIdentidad,
					NombreRegimenTributario: item.NombreRegimenTributario,
					NombrePlanTipoInversion: item.NombrePlanTipoInversion
				}))
			};

			bcsServiciosNormativa.EliminarReporteAPV(datos).$promise.then(function (result) {
				let mensajeTotal = new Array();

				bcsComunes.ventanaOverlay(idVentana, false);
				if (result.P_OUT_ERRORESCONTADOR === 0) {
					$.each(result.P_OUT_MENSAJES, function (key, value) {
						const objMensaje = {
							id: value.id,
							tipo: value.tipo,
							codigo: value.codigo,
							descripcion: value.descripcion
						};
						mensajeTotal.push(objMensaje);
					});

					result.P_OUT_MENSAJES = mensajeTotal;
					let id = bcsComunes.ventanaMsg({ mensaje: $translate.instant("UI_PROCESO_COMPLETADO"), conflictos: util.parseServerResponse(result) }, idVentana);
					bcsComunes.ventanaPromesa(id).then(function () {
						$scope.buscar();
					});
				}
			});
		});
	};

	$scope.generarReporteAPV = function () {
		bcsComunes.ventanaOverlay(idVentana,true);
		
		const datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo  : 'NOR',
				p_IdAgenda: 0,
				p_IdTipoTarea: 'GENERAR_REPORTE_APV',
				p_Segundos: 0,
				p_CreaParam: 'S',
				p_TablaParametros: [
					{ NombreParametro: 'IdEmpresa', Valor: $scope.SesionIdEmpresa },
					{ NombreParametro: 'IdUsuario', Valor: $scope.SesionIdUsuario },
					{ NombreParametro: 'IdModulo', Valor: 'NOR' },
					{ NombreParametro: 'FechaCierre', Valor: util.dateSPdate($scope.filtros.kngFechaSistema) }
				]
		}
		
		bcsServicioPlataforma.AgregarPrograma(datos, function(respuestaJob) {
			let Programa = respuestaJob.P_OUT_IDPROGRAMA;
            let idPrograma = 0;
            
            if (Programa.length > 0)
                idPrograma = Programa[0].IdPrograma;
                
			bcsComunes.ventanaOverlay(idVentana.ventanaId,false);
			
			bcsComunes.ventanaMsg({ mensaje : "Programa solicitado correctamente (#" + idPrograma +"). Vea la salida en Consultar Programas",
				conflictos : util.parseServerResponse(respuestaJob)}, idVentana);

		},
		function(respuestaJob){
			bcsComunes.ventanaOverlay(idVentana,false);
			bcsComunes.ventanaMsg({ mensaje:$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'), conflictos:util.parseServerResponse(respuestaJob) },idVentana);
		});
	}

	$scope.limpiar = function(){
		$scope.filtros.kngFechaSistema 	= util.dateInicioDia($scope.SesionFechaSistema);
		dsReporteAPV.removeAll();
		$scope.seleccionReporteAPV = [];
	};
	
	$scope.cargar = function() {
		if ($scope.filtros.kngFechaSistema == undefined || $scope.filtros.kngFechaSistema == "" || $scope.filtros.kngFechaSistema == null) {
			bcsComunes.msg('error', $translate.instant("UI_ERROR"), $translate.instant("DEBE_ESPECIFICAR_FECHA_CIERRE"));
			return;
		}

		let pregunta = bcsAlerta.crear("PREGUNTA_SI_NO", $translate.instant('UI_SE_SOBRESCRIBIRAN_DATOS_REPORTE_APV'), OPCIONES_BCSALERTA.SI_NO);
		
		pregunta.result.then(function(result) {
			bcsComunes.ventanaOverlay(idVentana, true);
			if (result === OPCIONES_BCSALERTA.SI) {
				dsReporteAPV.removeAll();
				const datos = {
					p_IdEmpresa: $scope.SesionIdEmpresa,
					p_IdUsuario: $scope.SesionIdUsuario,
					p_IdModulo: 'NOR',
					p_IdAgenda: 0,
					p_IdTipoTarea: 'CARGA_REPORTE_APV',
					p_Segundos: 0,
					p_CreaParam: 'S',
					p_TablaParametros: [
						{ NombreParametro: 'IdEmpresa', Valor: $scope.SesionIdEmpresa },
						{ NombreParametro: 'IdUsuario', Valor: $scope.SesionIdUsuario },
						{ NombreParametro: 'FechaCierre', Valor: util.dateSPdate($scope.filtros.kngFechaSistema) }
					]
				};

				bcsServicioPlataforma.AgregarPrograma(datos, function(respuestaJob) {
					if (respuestaJob.P_OUT_ERRORESCONTADOR > 0) {
						bcsComunes.ventanaMsg({ mensaje:$translate.instant("UI_ERROR_CARGA_REPORTE_APV") },idVentana);
					} else {
						let Programa = respuestaJob.P_OUT_IDPROGRAMA;
						let idPrograma = 0;
						
						if (Programa.length > 0)
						    idPrograma = Programa[0].IdPrograma;		
						
						bcsComunes.ventanaMsg({ mensaje: "Programa solicitado correctamente (#" + idPrograma +"). Vea la salida en Consultar Programas",
							conflictos : util.parseServerResponse(respuestaJob)}, idVentana);
					}
					bcsComunes.ventanaOverlay(idVentana, false);
				}, function(respuestaJob) {
					bcsComunes.ventanaOverlay(idVentana, false);
					bcsComunes.ventanaMsg({ mensaje:$translate.instant("UI_ERROR_CARGA_REPORTE_APV"), conflictos:util.parseServerResponse(respuestaJob) },idVentana);
				});

			}
			bcsComunes.ventanaOverlay(idVentana, false);
		});
	};
})