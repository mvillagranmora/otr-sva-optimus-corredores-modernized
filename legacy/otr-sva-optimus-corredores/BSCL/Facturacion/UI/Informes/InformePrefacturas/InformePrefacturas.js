angular.module('ModuloFacturacion').controller('InformePrefacturas',
	function($scope, $translate, $rootScope, bcsServiciosFacturacion, bcsComunes)	{
		var obj = this;
		$scope.data = {};
		$scope.grid = {};	
		var idVentana = bcsComunes.ventanaWinId ($scope);
		bcsComunes.ventanaAutoOverlay(idVentana,false,3000);

		var fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);

		var fechaMinima = new Date(fechaSistema);
		fechaMinima.setMonth(fechaMinima.getMonth() - 6);
		$scope.esFechaActual = false;
		$scope.data.kOpFechaConsulta = {
			min: fechaMinima,
			max: fechaSistema
		};
		$scope.data.kngFechaConsulta = fechaSistema;
		//------------------------------------------------
		// CONFIGURACION DE COMBOX
		//------------------------------------------------	

		$scope.oConfiguracionCliente = {
			tipo : "combobox-cliente-portafolio",
			configuracion :{},
			filtros: {
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},			
			idActual : undefined,
			seleccionado : -1,
			idDefecto : -1
		};
		
		$scope.oConfiguracionFormato = {
			tipo: "combobox-formato-archivo",
			configuracion: {},
			filtros: {},
			idActual: false,
			seleccionado: false,
			idDefecto: ' TODOS'
		};

		//------------------------------------------------
		// CONFIGURACION DE GRILLA
		//------------------------------------------------	

		$scope.prefacturaOptions = new bcs.DataSource({
		type: "odata",
			definition: {
				serverFiltering: true,
				serverPaging: true,
				serverSorting: true,
				pageSize: 100,
				sort: [
					{ field: "FechaComprobante", dir: "desc" }
				],
				requestStart: function(e) {
					if (!$scope.data.kngFechaConsulta) {
						e.preventDefault();
						return;
					}
					// Forzar filtro de fecha siempre
					var filtrosActuales = $scope.prefacturaOptions.filter();
					var tieneFecha = filtrosActuales && filtrosActuales.filters && 
						filtrosActuales.filters.some(function(f) { 
							return f.field === "FechaComprobante"; 
						});
					if (!tieneFecha) {
						e.preventDefault();
					}
				},
				transport: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010283",
						dataType: "json"
					}
				},
				schema: {
					model: {
						fields: {
							IdCliente: { type: "number" },
							DocumentoIdentidad: { type: "string" },
							NombreLargoCliente: { type: "string" },
							IdPortafolio: { type: "number" },
							CodigoPortafolio: { type: "string" },
							FechaComprobante: { type: "date" },
							Cantidad: { type: "number" },
							CantidadFAC: { type: "number" },
            				CantidadENV: { type: "number" }
						}
					}
				}
			}
		});

		$scope.grid.confGridInforme = {
			dataSource: $scope.prefacturaOptions,
			columns: [
				{ field: "DocumentoIdentidad", title: "UI_RUT", width: "150px" },
				{ field: "NombreLargoCliente", title: "UI_NOMBRE_CLIENTE", width: "250px" },
				{ field: "CodigoPortafolio", title: "UI_PORTAFOLIO", width: "100px" },
				{ 
					field: "Cantidad",
					title: "UI_CANTIDAD_REGISTROS", 
					width: "100px",
					template: function(dataItem) {
						return $scope.esFechaActual ? dataItem.CantidadFAC : dataItem.Cantidad;
					}
				}
			],
			filterable: true,
			sortable: true,
			pageable: true,
			height: 300,
			selectable: "multiple, row",
			autoBind: false,
		};

	    // Buscar
		$scope.buscarPrefacturas = function() {
			if (!$scope.data.kngFechaConsulta) return;

			var fechaBusqueda = util.dateInicioDia($scope.data.kngFechaConsulta);

			if (fechaBusqueda < fechaMinima) {
				bcsComunes.ventanaMsg({
					mensaje: $translate.instant('UI_FECHA_FUERA_DE_RANGO')
				}, idVentana);
				return;
			}

			$scope.seleccionInformePrefacturas = [];
			$scope.esFechaActual = fechaBusqueda.getTime() === fechaSistema.getTime();

			var filtros = [];

			filtros.push({ 
				field: "FechaComprobante", 
				operator: "eq", 
				value: kendo.toString($scope.data.kngFechaConsulta, "yyyy-MM-dd HH:mm:ss") 
			});

			if ($scope.esFechaActual) {
				filtros.push({ field: "CantidadFAC", operator: "gt", value: 0 });
			}

			var sel = $scope.data.oClientePortafolio?.seleccionado;
			if (sel && sel.IdPortafolio) {
				filtros.push({ field: "IdPortafolio", operator: "eq", value: sel.IdPortafolio });
			}

			$scope.prefacturaOptions.filter(filtros);
		};


		$scope.exportarTodo = function() {
			if (!$scope.data.kngFechaConsulta) {
				bcsComunes.msg("error", "ERROR", $translate.instant('UI_FECHA_CONSULTA_REQUERIDA'))
				return;
			}

			bcsComunes.ventanaOverlay(idVentana, true);

			_obtenerPortafoliosFiltrados().then(function(listaPortafolios) {
				if (listaPortafolios.length === 0) {
					bcsComunes.ventanaOverlay(idVentana, false);
					bcsComunes.msg("info", "info", $translate.instant('UI_NO_HAY_INFORMACION_PARA_EXPORTAR'));
					return;
				}
				_ejecutarJob(listaPortafolios, true);
			}, function() {
				bcsComunes.ventanaOverlay(idVentana, false);
				bcsComunes.msg("error", "ERROR", $translate.instant('UI_ERROR_INGRESO'));
			});
		};

		var _obtenerPortafoliosFiltrados = function() {
			var dsFiltrado = new bcs.DataSource({
				type: "odata",
				definition: {
					serverFiltering: true,
					serverPaging: false,
					serverSorting: false,
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010283",
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								IdPortafolio: { type: "number" }
							}
						}
					}
				}
			});

			dsFiltrado.filter($scope.prefacturaOptions.filter());

			return dsFiltrado.read().then(function() {
				var listaPortafolios = [];
				angular.forEach(dsFiltrado.data(), function(item) {
					if (item.IdPortafolio && listaPortafolios.indexOf(item.IdPortafolio) === -1) {
						listaPortafolios.push(item.IdPortafolio);
					}
				});
				return listaPortafolios;
			});
		};

		$scope.exportarSeleccionado = function() {
			if (!$scope.data.kngFechaConsulta) {
				bcsComunes.msg("error", "ERROR", $translate.instant('UI_FECHA_CONSULTA_REQUERIDA'))
				return;
			}
			var listaPortafolios = [];
			angular.forEach($scope.seleccionInformePrefacturas, function(item) {
				if (item.IdPortafolio && listaPortafolios.indexOf(item.IdPortafolio) === -1) {
					listaPortafolios.push(item.IdPortafolio);
				}
			});

			_ejecutarJob(listaPortafolios, false);
		};

		var _armarParametros = function(listaPortafolios, exportarTodo) {
			var formato = $scope.oConfiguracionFormato.idActual === ' TODOS' 
				? 'TODOS' 
				: $scope.oConfiguracionFormato.idActual.trim() || null;

			var params = {
				IdEmpresa       : $rootScope.SesionIdEmpresa,
				IdUsuario       : $rootScope.SesionIdUsuario,
				FechaComprobante: kendo.toString($scope.data.kngFechaConsulta, "yyyy-MM-dd"),
				FormatoSalida   : formato,
				ExportarTodo    : exportarTodo
			};

			if (listaPortafolios && listaPortafolios.length > 0) {
				params.IdPortafolio = listaPortafolios.join(',');
			}

			return params;
		};

		var _ejecutarJob = function(listaPortafolios, exportarTodo) {
			var params = _armarParametros(listaPortafolios, exportarTodo);

			var parametros = [{
				IdInterno      : 0,
				NombreParametro: 'IdUsuario',
				Valor          : String($rootScope.SesionIdUsuario)
			},{
				IdInterno      : 0,
				NombreParametro: 'FechaComprobante',
				Valor          : params.FechaComprobante
			},{
				IdInterno      : 0,
				NombreParametro: 'FormatoSalida',
				Valor          : params.FormatoSalida
			},{
				IdInterno      : 0,
				NombreParametro: 'ExportarTodo',
				Valor          : String(params.ExportarTodo)
			}];

			if (params.IdPortafolio) {
				parametros.push({
					IdInterno      : 0,
					NombreParametro: 'IdPortafolio',
					ValorBlob      : params.IdPortafolio
				});
			}

			var datosJob = {
				p_IdEmpresa  : $rootScope.SesionIdEmpresa,
				p_IdUsuario  : $rootScope.SesionIdUsuario,
				p_IdModulo   : 'FAC',
				p_TipoPrograma: [{
					IdInterno      : 0,
					IdTipoPrograma : 'GenerarInformePrefacturas',
					SegundosDesfase: 0,
					Comentario     : 'Generar Informe Prefacturas'
				}],
				p_Parametro: parametros
			};

			bcsComunes.ventanaOverlay(idVentana, true);

			var success = function(respuesta) {
				bcsComunes.ventanaOverlay(idVentana, false);
				if (respuesta.P_OUT_ERRORESCONTADOR == "0") {
					var fechaLocal = util.dateDS(
						respuesta.P_OUT_PROGRAMA[0].FechaEjecucion, "FechaHora", "CALL"
					);
					bcsComunes.ventanaMsg({
						mensaje   : $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO')
										.replace("::IdPrograma",     respuesta.P_OUT_PROGRAMA[0].IdPrograma)
										.replace("::FechaEjecucion", util.formatoSecondate(fechaLocal)),
						conflictos: util.parseServerResponse(respuesta)
					}, $scope.winID);
				} else {
					bcsComunes.ventanaMsg({
						mensaje   : $translate.instant(""),
						conflictos: util.parseServerResponse(respuesta)
					}, idVentana);
				}
			};

			var error = function(respuesta) {
				bcsComunes.ventanaOverlay(idVentana, false);
				bcsComunes.ventanaMsg({
					mensaje   : $translate.instant('UI_ERROR_INGRESO'),
					conflictos: util.parseServerResponse(respuesta)
				}, idVentana);
			};

			bcsServiciosFacturacion.InscribirProcesoBatch(datosJob, success, error);
		};
					
    }	
);