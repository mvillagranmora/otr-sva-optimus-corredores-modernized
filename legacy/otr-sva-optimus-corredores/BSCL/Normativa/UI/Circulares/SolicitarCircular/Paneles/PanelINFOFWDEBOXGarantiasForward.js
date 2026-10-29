(function() {
	angular.module('ModuloNormativa').controller('PanelINFOFWDEBOXGarantiasForwardCtrl', function($scope, Model, $translate, bcsComunes, bcsServiciosNormativa, $rootScope) {
		
		const gfEBox = this;
		gfEBox.idVentana = bcsComunes.ventanaWinId($scope);
		gfEBox.domVentana = $("[ventanaid='" + gfEBox.idVentana + "']");
		gfEBox.IdVersionEsquema = '';
		gfEBox.IdTipoDocumento = 'GARFWDEBOX';
		$scope.dataTemplate = {};
		gfEBox.fechaSistema = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
		const idEmpresa = $scope.SesionIdEmpresa;
		gfEBox.IdCorredora = $scope.SesionIdCorredoraSistema;

		gfEBox.opcionFiltro = 'AM';
		gfEBox.DocumentoIdentidadCorredora = 0;

		/*********************************************
		 * VARIABLES PARAMETROS
		 ********************************************/

		let params = {
			p_TablaParametrosAux: [
				{
					Tipo: 'BATCH',
					NombreParametro: 'idUsuario',
					Valor: $scope.SesionIdUsuario
				}, {
					Tipo: 'BATCH',
					NombreParametro: 'schema',
					Valor: Model.getSchema()
				}, {
					Tipo: 'BATCH',
					NombreParametro: 'grabar',
					Valor: $scope.solCir.grabar
				}, {
					Tipo: 'BATCH',
					NombreParametro: 'reemplazar',
					Valor: $scope.solCir.reemplazar
				}, {
					Tipo: 'BATCH',
					NombreParametro: 'separadorListas',
					Valor: $scope.solCir.separadorListas,
				}, {
					Tipo: 'BATCH',
					NombreParametro: 'simboloDecimal',
					Valor: $scope.solCir.simboloDecimal
				}, {
					Tipo: 'BATCH',
					NombreParametro: 'simboloFecha',
					Valor: $scope.solCir.simboloFecha
				}, {
					Tipo: 'BATCH',
					NombreParametro: 'aceptarFechasNull',
					Valor: $scope.solCir.aceptarFechasNull
				},]
		};
		
		let parametrosSP = {};

		/*********************************************
		 * DataSource
		 ********************************************/

		const ModeloSolicitar = function() {
			return {
				model: {
					fields: {
						Fecha: { type: "date" },
					}
				}
			};
		};

		const ModeloDocumentoIdentidadCorredora = function() {
			return {
				model: {
					fields: {
						DocumentoIdentidad: { type: "string" },
					}
				}
			};
		};

		const dataSourceFechasSolicitar = function() {
			return new bcs.DataSource({
				type: "odata",
				definition: {
					serverFiltering: true,
					serverPaging: true,
					serverSorting: true,
					pageSize: 50,
					transport: {
						read: { url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=2470", dataType: "json" }
					},
					filter: [ { field: "FechaOperacion", operator: "lte", value: idEmpresa } ],
					schema: ModeloSolicitar(),
				}
			});
		};

		const dataSourceDocumentoIdentidadCorredora = function() {
			return new bcs.DataSource({
				type: "odata",
				definition: {
					serverFiltering: true,
					serverPaging: true,
					serverSorting: true,
					pageSize: 50,
					transport: {
						read: { url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3129", dataType: "json" }
					},
					filter: [ { field: "IdCorredora", operator: "eq", value: gfEBox.IdCorredora } ],
					schema: ModeloDocumentoIdentidadCorredora()
				}
			});
		};

		/*********************************************
		 * FUNCIONES
		 ********************************************/

		$scope.gfEBox.clear = function() {
			gfEBox.FecInicio = util.dateInicioDia($scope.SesionFechaSistema);
			gfEBox.FecTermino = util.dateInicioDia($scope.SesionFechaSistema);
		};
		
		const successCrearSolicitud = function(respuesta) {
			if (respuesta.P_OUT_ERRORESCONTADOR == 0) {
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				bcsComunes.msg("info", "info", 'Solicitud creada con éxito');
				IdSolicitudCircular = respuesta.P_OUT_RESULTADO;
			}
			else {
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				bcsComunes.msg("error", "error", $translate.instant('Solicitud no fue creada'));

				var i = 0;
				for (i; i < respuesta.P_OUT_ERRORESCONTADOR; i++) {
					bcsComunes.log().info("Error: " + respuesta.P_OUT_ERRORESDETALLE[i].Descripcion);
				}
			}
			bcsComunes.ventanaOverlay(gfEBox.idVentana, false);
			bcsComunes.ventanaData(gfEBox.idVentana, respuesta);
			bcsComunes.ventanaCerrar(gfEBox.idVentana);

		};

		const errorCrearSolicitud = function(respuesta) {
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.msg("error", "error", $translate.instant('INGRESO ERROR'));
			bcsComunes.ventanaOverlay(gfEBox.idVentana, false);
		};
		
		$scope.imprimirCircular = function() {
			var filterTemplate = [{ field: "IdTipoDocumento", operator: "eq", value: gfEBox.IdTipoDocumento },
			{ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa },
			{ field: "IdEstado", operator: "eq", value: 'ACT' }];
			$scope.solCir.ConsultaArchivoNormativa.query({ filter: filterTemplate }).then(function() {
				if ($scope.solCir.ConsultaArchivoNormativa.view().length > 0) {
					$scope.dataTemplate.TipoReporte = $scope.solCir.ConsultaArchivoNormativa.view()[0].TipoArchivo;
					$scope.dataTemplate.IdReporte = $scope.solCir.ConsultaArchivoNormativa.view()[0].IdArchivo;
					$scope.dataTemplate.NombreReporte = $scope.solCir.ConsultaArchivoNormativa.view()[0].NombreArchivo;
					$scope.dataTemplate.ServicioReporte = $scope.solCir.ConsultaArchivoNormativa.view()[0].ServicioArchivo;
					$scope.dataTemplate.dataSource = $scope.dataTemplate.ServicioReporte + "(p_IdEmpresa =>" + $scope.$root.SesionIdEmpresa + ",p_FechaOperacionDesde =>'" + util.dateSPdate(gfEBox.FecInicio) + "',p_FechaOperacionHasta =>'" + util.dateSPdate(gfEBox.FecTermino);
				}
			});
		};

		gfEBox.solicitar = function() {
			let fechaInicio = util.dateOdata(util.date(gfEBox.FecInicio).hours(0).minutes(0).seconds(0).toDate());

			if ($scope.existeFecha(gfEBox.FecInicio)) {
				let parametrosConsulta = { FechaDesde: fechaInicio };

				$scope.DocumentoIdentidadCorredora = [];
				$scope.DocumentoIdentidadCorredoraDataSource = dataSourceDocumentoIdentidadCorredora();

				$scope.DocumentoIdentidadCorredoraDataSource.fetch(function() {
					let data = this.data();
					$scope.$apply(() => { gfEBox.DocumentoIdentidadCorredora = data; });
				});

				dataSourceFechasSolicitar().query({ data: parametrosConsulta }).then(function() {
					parametrosSP.p_IdEmpresa = $scope.SesionIdEmpresa;
					parametrosSP.p_IdUsuario = $scope.SesionIdUsuario;
					parametrosSP.p_IdModulo = $scope.solCir.modulo;
					parametrosSP.p_FechaInicio = util.dateSPdate(gfEBox.FecInicio);
					parametrosSP.p_FechaTermino = util.dateSPdate(gfEBox.FecInicio);
					
					let rutCorredor = gfEBox.DocumentoIdentidadCorredora[0].DocumentoIdentidad;
					let fh = util.dateSPdate(gfEBox.FecInicio);
					let nomarch = rutCorredor + '_garantiasforwards';
			
					parametrosSP.p_IdTipoDocumento = gfEBox.IdTipoDocumento;
					parametrosSP.p_TablaParametros = params.p_TablaParametrosAux;
					parametrosSP.p_TablaParametros.push({Tipo: 'BATCH',NombreParametro : 'FechaSistema',Valor : gfEBox.fechaSistema});
					parametrosSP.p_TablaParametros.push({Tipo: 'BATCH',NombreParametro : 'IdEmpresa',Valor : $scope.SesionIdEmpresa});
					parametrosSP.p_TablaParametros.push({ Tipo: 'BATCH', NombreParametro: 'FINI', Valor: fh });
					parametrosSP.p_TablaParametros.push({ Tipo: 'BATCH', NombreParametro: 'FTER', Valor: fh });
					parametrosSP.p_TablaParametros.push({ Tipo: 'SOLICITUD', NombreParametro: 'NOMSAL', Valor: nomarch });

					bcsComunes.msg('info', 'Carga', 'En proceso...');
					bcsServiciosNormativa.CircularCrearSolicitud(parametrosSP, successCrearSolicitud, errorCrearSolicitud);
				});
			}
		};

		const ModeloFechas = function() {
			return {
				model: {
					fields: { Fecha: { type: "date" } }
				}
			};
		};

		const dataSourceFechas = function() {
			return new bcs.DataSource({
				type: "odata",
				definition: {
					serverFiltering: true,
					serverPaging: true,
					serverSorting: true,
					transport: {
						read: { url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=2470", dataType: "json" }
					},
					schema: ModeloFechas(),
				}
			});
		};

		$scope.fechas = [];

		$scope.existeFecha = function(fechaBuscada) {
			return new Promise(function(resolve) {
				let fechasDataSource = dataSourceFechas();

				fechasDataSource.fetch(function() {
					let data = this.data();
					$scope.$apply(() => {
						$scope.fechas = data;
						let fechaBuscadaStr = util.dateSPdate(fechaBuscada);

						let existeFecha = $scope.fechas.some(function(fecha) {
							let fechaStr = fecha.FechaOperacion;
							return fechaStr <= fechaBuscadaStr;
						});

						resolve(existeFecha);
					});
				});
			});
		};

		$scope.operacionesConError = [];

		$scope.operacionCompleta = function() {
			$scope.operacionesConError = [];

			$scope.fechas.forEach(function(fecha) {
				let faltanDatos = false;

				for (let propiedad in fecha) {
					if (propiedad !== "Fecha") {
						if (fecha[propiedad] === null || fecha[propiedad] === '' || fecha[propiedad] === undefined) {
							faltanDatos = true;
							break;
						}
					}
				}

				if (faltanDatos && new Date(fecha.FechaOperacion) < gfEBox.FecInicio) {
					$scope.operacionesConError.push(fecha.NumeroOperacion);
				}
			});

			return $scope.operacionesConError.length === 0;
		};

		$scope.gfEBox.clear();
	});
}());
