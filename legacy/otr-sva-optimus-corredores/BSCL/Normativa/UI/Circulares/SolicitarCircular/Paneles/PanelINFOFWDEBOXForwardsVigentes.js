(function() {
	angular.module('ModuloNormativa').controller('PanelINFOFWDEBOXForwardsVigentesCtrl', function($scope, Model, $translate, bcsComunes, bcsServiciosNormativa, $rootScope) {

		const fvEBox = this;
		fvEBox.idVentana = bcsComunes.ventanaWinId($scope);
		fvEBox.domVentana = $("[ventanaid='" + fvEBox.idVentana + "']");
		fvEBox.IdVersionEsquema = '';
		fvEBox.IdTipoDocumento = 'FWDVIGEBOX';
		$scope.dataTemplate = {};
		fvEBox.fechaSistema = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
		const idEmpresa = $scope.SesionIdEmpresa;
		fvEBox.IdCorredora = $scope.SesionIdCorredoraSistema;

		fvEBox.opcionFiltro = 'AM';
		fvEBox.DocumentoIdentidadCorredora = 0;

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
					filter: [ { field: "IdCorredora", operator: "eq", value: fvEBox.IdCorredora } ],
					schema: ModeloDocumentoIdentidadCorredora()
				}
			});
		};

		/*********************************************
		 * FUNCIONES
		 ********************************************/

		$scope.fvEBox.clear = function() {
			fvEBox.FecInicio = util.dateInicioDia($scope.SesionFechaSistema);
			fvEBox.FecTermino = util.dateInicioDia($scope.SesionFechaSistema);
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
			bcsComunes.ventanaOverlay(fvEBox.idVentana, false);
			bcsComunes.ventanaData(fvEBox.idVentana, respuesta);
			bcsComunes.ventanaCerrar(fvEBox.idVentana);

		};

		const errorCrearSolicitud = function(respuesta) {
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.msg("error", "error", $translate.instant('INGRESO ERROR'));
			bcsComunes.ventanaOverlay(fvEBox.idVentana, false);
		};
		
		$scope.imprimirCircular = function() {
			var filterTemplate = [{ field: "IdTipoDocumento", operator: "eq", value: fvEBox.IdTipoDocumento },
			{ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa },
			{ field: "IdEstado", operator: "eq", value: 'ACT' }];
			$scope.solCir.ConsultaArchivoNormativa.query({ filter: filterTemplate }).then(function() {
				if ($scope.solCir.ConsultaArchivoNormativa.view().length > 0) {
					$scope.dataTemplate.TipoReporte = $scope.solCir.ConsultaArchivoNormativa.view()[0].TipoArchivo;
					$scope.dataTemplate.IdReporte = $scope.solCir.ConsultaArchivoNormativa.view()[0].IdArchivo;
					$scope.dataTemplate.NombreReporte = $scope.solCir.ConsultaArchivoNormativa.view()[0].NombreArchivo;
					$scope.dataTemplate.ServicioReporte = $scope.solCir.ConsultaArchivoNormativa.view()[0].ServicioArchivo;
					$scope.dataTemplate.dataSource = $scope.dataTemplate.ServicioReporte + "(p_IdEmpresa =>" + $scope.$root.SesionIdEmpresa + ",p_FechaOperacionDesde =>'" + util.dateSPdate(fvEBox.FecInicio) + "',p_FechaOperacionHasta =>'" + util.dateSPdate(fvEBox.FecTermino);
				}
			});
		};

		fvEBox.solicitar = function() {
			let fechaInicio = util.dateOdata(util.date(fvEBox.FecInicio).hours(0).minutes(0).seconds(0).toDate());

			if ($scope.existeFecha(fvEBox.FecInicio)) {
				let parametrosConsulta = { FechaDesde: fechaInicio };

				$scope.DocumentoIdentidadCorredora = [];
				$scope.DocumentoIdentidadCorredoraDataSource = dataSourceDocumentoIdentidadCorredora();

				$scope.DocumentoIdentidadCorredoraDataSource.fetch(function() {
					let data = this.data();
					$scope.$apply(() => { fvEBox.DocumentoIdentidadCorredora = data; });
				});

				dataSourceFechasSolicitar().query({ data: parametrosConsulta }).then(function() {
					parametrosSP.p_IdEmpresa = $scope.SesionIdEmpresa;
					parametrosSP.p_IdUsuario = $scope.SesionIdUsuario;
					parametrosSP.p_IdModulo = $scope.solCir.modulo;
					parametrosSP.p_FechaInicio = util.dateSPdate(fvEBox.FecInicio);
					parametrosSP.p_FechaTermino = util.dateSPdate(fvEBox.FecInicio);
					
					let rutCorredor = fvEBox.DocumentoIdentidadCorredora[0].DocumentoIdentidad;
					let fh = util.dateSPdate(fvEBox.FecInicio);
					let nomarch = rutCorredor + '_forwards';
					
					parametrosSP.p_IdTipoDocumento = fvEBox.IdTipoDocumento;
					parametrosSP.p_TablaParametros = params.p_TablaParametrosAux;

					parametrosSP.p_TablaParametros.push({Tipo: 'BATCH',NombreParametro : 'FechaSistema',Valor : fvEBox.fechaSistema});
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

				if (faltanDatos && new Date(fecha.FechaOperacion) < fvEBox.FecInicio) {
					$scope.operacionesConError.push(fecha.NumeroOperacion);
				}
			});

			return $scope.operacionesConError.length === 0;
		};

		$scope.fvEBox.clear();
	});
}());
