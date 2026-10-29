angular.module('ModuloMesa').controller('CrearIngresoOperacionMesaRFCtrl',
	function ($scope, $translate, $timeout, $rootScope, bcsComunes, bcsAlerta, OPCIONES_BCSALERTA, bcsServiciosMesa, bcsServiciosGrillasMesaRF, bcsServicioPlataforma) {
		var IngresoOperacion = this;
		IngresoOperacion.mostrarCamposTasaCostoFondo = "N";
		IngresoOperacion.valorParametroCostoFondo = 0;
		$scope.data = {};
		$scope.dataNew = {};
		$scope.data.IdMonedaMontoDisplay = $scope.SesionIdMonedaSistema;
		$scope.data.IdDecimalMontoDisplay = util.bcs_formato(0, 'GEN', 'Monto', $scope.data.IdMonedaMontoDisplay, '', '').cantidadDecimales;
		$scope.data.IdDecimalTasaDisplay = 0;
		$scope.winID = bcsComunes.ventanaWinId($scope);
		bcsComunes.ventanaAutoOverlay($scope.winID, false, 3000);
		$scope.TipoOperacionSeleccionado = false;
		$scope.ClienteSeleccionado = false;
		$scope.data.recarga = false;
		$scope.data.formasDePagoDefecto = new Array();
		var ident = '>> ';
		var ident = '>> ';
		var parametrosVentana = bcsComunes.ventanaParams($scope.winID);
		var confGridCartera = bcsServiciosGrillasMesaRF.getConfiguracionCarteraEnIngreso();

		$scope.ocultarcamposcostofondo = true;
		$scope.ocultartasaanualizada = true;
		$scope.ocultartasaanualizadainput = true;

		$scope.data.bloqueaCombos = true;
		// VER PARAMETRO MESA
		$scope.formasDePago={}
		$scope.formasDePago.IdPortafolioCliente=0;
		$scope.data.operacionEnModificacion = null;
		$scope.data.muestraNotificacion = true;
		$scope.data.AfectaCustodia = null;
		$scope.data.bloqueaComboClienteOpera = false;
		$scope.data.tabFormasDePagoActive = false;


		$scope.data.validoValorInicialBoton= true;
		$scope.data.validoVencimiento= true;




		

		var defConfiguracionMesa =
		{
			type: "odata",
			definition:
			{
				serverFiltering: true, // Filtrado de datos en el servidor.
				serverSorting: true, // Ordenamiento de los datos en el servidor.
				sort: { field: "", dir: "DESC" },
				transport: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=432",
						dataType: "json"
					}
				},
				schema: {
					model:
					{
						fields:
						{
							IdEmpresa: { type: "int" },
							IdTipoProducto: { type: "string" },
							CodigoParametro: { type: "string" },
							Descripcion: { type: "string" },
							Valor: { type: "string" },
						}
					}
				}

			}
		};

		var dsConfiguracionMesa = new bcs.DataSource(defConfiguracionMesa);

		var filtroConfiguracionMesa = new Array();
		
		filtroConfiguracionMesa.push( {
			logic: "or",
		    filters: [{logic: "and",filters: [{ field: "CodigoParametro", operator: "eq", value: "COSTOFONDORF" }]}, 
		    		{logic: "and",filters: [{ field: "CodigoParametro", operator: "eq", value: "VALORCFRF" }]}, 
	              	{logic: "and",filters: [{ field: "CodigoParametro", operator: "eq", value: "MAXIMO_INTRUCCIONES_FORMAS_PAGO" }]}
		             ]
		  }
		);
		dsConfiguracionMesa.query({ filter: filtroConfiguracionMesa }).then(function () {
			$.each(dsConfiguracionMesa.data(), function (key, value) {
				if("COSTOFONDORF" == value.CodigoParametro){

					IngresoOperacion.mostrarCamposTasaCostoFondo = (value && value.Valor) ? value.Valor : "N";
					if (IngresoOperacion.mostrarCamposTasaCostoFondo == "N" && $scope.data.oConfiguracionTipoOperacion.idActual != "COMPAC" || $scope.data.oConfiguracionTipoOperacion.idActual != "VENPAC") {
						$scope.ocultarcamposcostofondo = true;
					}
				}

				if("VALORCFRF" == value.CodigoParametro){
					IngresoOperacion.valorParametroCostoFondo = (value && value.Valor) ? parseFloat(value.Valor) : 0;
				}
				

				if("MAXIMO_INTRUCCIONES_FORMAS_PAGO" == value.CodigoParametro){
					$scope.cantidadFormasDePago = value.Valor
				}
				

			});
			
		});


		var dataSourceCarteraOperacion = bcsServiciosGrillasMesaRF.getDataSourceCarteraOperacion();
		var dataSourceDetalleOperacion = bcsServiciosGrillasMesaRF.getDataSourceDetalleOperacionMesaRF();
		var dataSourceMargenes = bcsServiciosGrillasMesaRF.getDataSourceConsultarMargenes();
		var dataSourcePreferenciaPortafolio = bcsServiciosGrillasMesaRF.getDataSourcePreferenciaPortafolio();
		var dataSourceClienteCuentaPropia = bcsServiciosGrillasMesaRF.getDataSourceConsultarClienteCuentaPropia();

		var dataSourceBoveda
		var buscaParidad = true;
		var ParametrosMesa = {};
		var schemaGridCarteraNueva = null;
		var params = {
			p_IdEmpresa: $scope.SesionIdEmpresa,
			p_IdTipoProducto: 'MERF',
			p_CodigoParametro: ''
		};
		$scope.data.IdPortafolioCorredora = 0;
		$scope.data.BovedaDefaultPortafolio = "";
		$scope.data.OperacionExiste = false;
		$scope.data.CargandoOperacion = false;
		$scope.data.CargandoDatosOperacionExistente = false;
		$scope.data.marcaEdicionUsuario = function () {
			var eraCargaDatos = $scope.data.CargandoDatosOperacionExistente;
			$scope.data.CargandoDatosOperacionExistente = false;
			if (eraCargaDatos) {
				calcularMontoOperacionMoneda();
				calcularMontoOperacion();
			}
		};
		$scope.data.habilitaVentaNominal = false;
		$scope.data.habilitaMonedaLiquidacion = false;
		$scope.data.habilitaCabecera = true;
		$scope.data.cierraVentana = false;
		$scope.data.formaDePagoEntregamos =[];
		$scope.data.formaDePagoRecibimos =[];
		$scope.data.formaDePagoEntregamosTotal = 0;
		$scope.data.formaDePagoRecibimosTotal = 0;
		$scope.data.EnProcesoGrabar = false;
		$scope.data.PuedeGrabarFormasPago = false;
		$scope.data.MontoMON_Org = 0;
		$scope.data.MontoCLP_Org = 0;
		$scope.data.MontoMaximoParaAjuste = 10000;
		$scope.data.PuedeAjustar = true;
		$scope.data.MontoAjustado = 0;
		$scope.data.habilitaCerrar = true;
		$scope.data.formaDePago = new Map();
		$scope.data.modificacionFormaDePago = parametrosVentana.ModificacionFormasDePago ? parametrosVentana.ModificacionFormasDePago : false;
		$scope.data.cerrando = false;
		$scope.data.accionOperacion = '';
		$scope.data.agregaPapel= false;
		$scope.data.errorGrabaDetalle = false;
		// No se puede evitar por el momento que no grabe al principio


		// --------------------------------------------------
		// CONFIGURACION DE COMBOS
		// --------------------------------------------------
		$scope.data.oConfiguracionMesas = {
			tipo: "combobox-mesas",
			configuracion: {},
			idActual: false,
			idDefecto: false,
			seleccionado: false,
			filtros: {
				IdEstado: { "field": "IdEstadoRegistro", "operator": "eq", "value": "ACT" },
				IdTipoProducto: { "field": "IdTipoProducto", "operator": "eq", "value": "MERF" }
			},
		};

		$scope.data.oConfiguracionContactos = {
			tipo: "combobox-contactos",
			filtros: {
				IdCliente: { "field": "IdCliente", "operator": "eq", "value": "" }
			},
			idActual: false,
			idDefecto: false,
			seleccionado: false,
		};

		$scope.data.oConfiguracionDocumentos = {
			tipo: "combobox-documentos-mesa",
			filtros: {
				Codigo: { "field": "Codigo", "operator": "eq", "value": "" },
				IdTipoOperacion: { "field": "IdTipoOperacion", "operator": "eq", "value": "" },
				TipoOperacionMesa: { "field": "TipoOperacionMesa", "operator": "eq", "value": "RF" },
			},
			configuracion: {},
			idActual: false,
			idDefecto: false,
			seleccionado: false,

		};

		$scope.data.oClientePortafolio = {
			idActual: false,
			seleccionado: false,
			filtros: {
				IdEstado: { "field": "IdEmpresa", "operator": "eq", "value": $scope.SesionIdEmpresa },
				EsCliente: { "field": "EsCliente", "operator": "eq", "value": 'UI_SI' }
			}
		};


		$scope.data.oConfiguracionSocioNegocio = {
			tipo: "combobox-socio-negocio",
			configuracion: {},
			idActual: false,
			seleccionado: false,
			filtros: {
				IdEmpresa: { "field": "IdEmpresa", "operator": "eq", "value": $scope.SesionIdEmpresa },
				EsCliente: { "field": "EsCliente", "operator": "eq", "value": 'UI_SI' }
			},
			idDefecto: ''
		};

		$scope.data.oConfiguracionPortafolio = {
			tipo: "combobox-portafolio",
			configuracion: {},
			idActual: false,
			seleccionado: false,
			filtros: {
				IdCliente: { "field": "IdCliente", "operator": "eq", "value": 0 }
			},
		};

		$scope.IdTipoProducto = { "field": "IdTipoProducto", "operator": "eq", "value": 'MERF' };
		$scope.data.EsAnticipo = { "field": "EsAnticipo", "operator": "eq", "value": 'N' };
		$scope.data.EsAnulacion = { "field": "EsAnulacion", "operator": "eq", "value": 'N' };
		$scope.data.IdTipoOperacionRelacionada = [{ "field": "IdTipoOperacionRelacionada", "operator": "eq", "value": 'VENPAC' },
		{ "field": "IdTipoOperacionRelacionada", "operator": "eq", "value": 'VENTER' },
		{ "field": "IdTipoOperacionRelacionada", "operator": "eq", "value": 'COMPAC' },
		{ "field": "IdTipoOperacionRelacionada", "operator": "eq", "value": 'COMTER' }
		];

		$scope.data.oConfiguracionTipoOperacion = {
			tipo: "combobox-tipo-operacion",
			configuracion: {},
			idActual: false,
			seleccionado: false,
			filtros: {
				complejo: [$scope.data.IdTipoOperacionRelacionada],
				logic: "and",
				filters: [{
					logic: "or",
					filters: $scope.data.IdTipoOperacionRelacionada
				},
				$scope.IdTipoProducto,
				$scope.data.EsAnticipo,
				$scope.data.EsAnulacion
				]
			},
			idDefecto: ''
		};


		$scope.data.oConfiguracionTipoReajusteOperacion = {
			tipo: "combobox-tipo-reajuste",
			configuracion: {
				template: '<span >#: IdTipoReajuste # - #: Nombre #</span>'
			},
			idActual: false,
			seleccionado: false,
			filtros: {
				IdEstado: { "field": "IdEstado", "operator": "eq", "value": "ACT" }
			},
			idDefecto: ''
		};

		$scope.data.oConfiguracionMoneda = {
			tipo: "combobox-tipo-instrumento-moneda-liquidacion",
			configuracion: {},
			filtros: {
				IdTipoInstrumento: { "field": "IdTipoInstrumento", "operator": "eq", "value": "" },
				IdMoneda: { "field": "IdMoneda", "operator": "eq", "value": "" }
			}
		};

		$scope.data.oConfiguracionCondicionLiquidacionOperacion = {
			tipo: "combobox-condicion-liquidacion",
			configuracion: {
				dataTextField: "Codigo",
				placeholder: "",
				template: '<span >#: Codigo #</span>'
			},
			filtros: {
				IdTipoOperacion: { "field": "IdTipoOperacion", "operator": "eq", "value": "" },
				EstadoTipoOperacionYCondicionDeLiquidacion: { "field": "EstadoTipoOperacionYCondicionDeLiquidacion", "operator": "eq", "value": "ACT" }
			}
		};

		$scope.data.oConfiguracionMonedaLiquidacionOperacion = {

			tipo: "combobox-tipo-instrumento-moneda-liquidacion",

			seleccionado: false,
			configuracion: {
				template: '<span >#: IdMonedaLiquidacion # - #: NombreMonedaLiquidacion #</span>'
			},
			filtros: {
				IdTipoInstrumento: { "field": "IdTipoInstrumento", "operator": "eq", "value": "" },
				IdMoneda: { "field": "IdMoneda", "operator": "eq", "value": "" }
			},
			idActual: false,
			idDefecto: ''
		};
		$scope.data.oConfiguracionMontoOperacionMoneda = {
			// En UAT no simpre se actualiza este dato (WATCH no funcionan igual
			// que desarrollo)
			// Se establece al incio maximo considerando tipo de reajuste CLP
			// max: 0
			max: 999999999999999
		};
		$scope.data.costoFondoOperacionOptions = {
			min: -999999999999999
		};

		// --------------------------------------------------
		// CONFIGURACION DE GRILLAS
		// --------------------------------------------------
		$scope.data.confBasicaCarteraInstrumento = confGridCartera.options;
		$scope.data.gridApiCarteraInstrumento = confGridCartera.options;


		// --------------------------------------------------
		// CONFIGURACION DE WATCHES
		// --------------------------------------------------

		var enableCabeceraOperacion = function () {

			deshabilitaIntermesa();
			if ($scope.data.oConfiguracionTipoOperacion.idActual === 'COMPAC' || $scope.data.oConfiguracionTipoOperacion.idActual === 'VENPAC') {
				$scope.data.EnabledTipoOperacion = true;
				$scope.data.RequeridoTipoOperacion = true;
				if (!$scope.data.OperacionExiste) {
					// Si esta modificando la operacion no se actualiza plazo
					$scope.data.plazoOperacion = '';
				}
			} else {
				$scope.data.EnabledTipoOperacion = false;
				$scope.data.RequeridoTipoOperacion = false;
				$scope.data.kngTasaOperacion = '';
				$scope.data.plazoOperacion = '';
			}

		};

		$scope.data.marcarOperacionModificada = function () {
			if (parametrosVentana.Accion !== 'MODIFICAR' || !parametrosVentana.IdOperacion) return;
			var datosMarcarOperacion = {
				p_IdEmpresa: $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: 'MRF',
				p_Operaciones: [{ Accion: 'M', IdOperacion: parametrosVentana.IdOperacion }]
			};
			bcsServiciosMesa.MarcarOperacion(datosMarcarOperacion, function () {}, function () {});
		};

		var success = function (respuesta) {
			bcsComunes.ventanaOverlay($scope.winID, false);
			$scope.data.agregaPapel  = false;
			if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
				$scope.data.marcarOperacionModificada();
				bcsComunes.ventanaData($scope.winID, [{ IdOperacion: respuesta.P_OUT_OPERACION[0].IdOperacion }]);
				if ($scope.data.cierraVentana) {
					if (respuesta.P_OUT_OPERACION[0].Accion === 'CERRAR') {
						var id = bcsComunes.ventanaMsg({
							mensaje: $translate.instant("UI_OPERACION_INGRESO_EXITO") + ": " + $translate.instant("UI_FOLIO_OPERACION") + " " + respuesta.P_OUT_OPERACION[0].FolioOperacion,
							conflictos: util.parseServerResponse(respuesta)
						}, $scope.winID);
						bcsComunes.ventanaPromesa(id).then(function (result) {
							if ($scope.WinCartera != null) bcsComunes.ventanaCerrar($scope.WinCartera.winID);
							bcsComunes.ventanaCerrar($scope.winID);
						});
						return;
					} else if (respuesta.P_OUT_OPERACION[0].Accion === 'MODIFICAR' && respuesta.P_OUT_OPERACION[0].FolioOperacion === 0 && $scope.data.muestraNotificacion) {
						var id = bcsComunes.ventanaMsg({
							mensaje: $translate.instant("UI_OPERACION_INGRESO_EXITO") + ": " + $translate.instant("UI_SECUENCIA") + " " + respuesta.P_OUT_OPERACION[0].IdOperacion,
							conflictos: util.parseServerResponse(respuesta)
						}, $scope.winID);
						bcsComunes.ventanaPromesa(id).then(function (result) {
							if ($scope.WinCartera != null) bcsComunes.ventanaCerrar($scope.WinCartera.winID);
							bcsComunes.ventanaCerrar($scope.winID);
						});
						return;
					}

					reset();
					$scope.data.habilitaCabecera = true;
					$scope.data.EnProcesoGrabar = false;
					bcsComunes.ventanaOverlay($scope.winID, false);
					if ($scope.WinCartera != null) bcsComunes.ventanaCerrar($scope.WinCartera.winID);
					bcsComunes.ventanaCerrar($scope.winID);
					return;
				}

				$scope.data.gridApiCarteraInstrumento.dataSource.sync();
				$scope.data.kngSecuenciaOperacion = respuesta.P_OUT_OPERACION[0].IdOperacion;
				$scope.data.kngFolioOperacion = respuesta.P_OUT_OPERACION[0].FolioOperacion;
				$scope.data.IdEstadoOperacion = respuesta.P_OUT_OPERACION[0].IdEstado;
				$scope.data.EstadoOperacion = respuesta.P_OUT_OPERACION[0].NombreEstado;

				$.each(respuesta.P_OUT_DETALLEOPERACION, function (key, value) {
					var data = $scope.data.gridApiCarteraInstrumento.dataSource.data();
					if (data.length > 0 && data[value.IdInternoDetalle])
						data[value.IdInternoDetalle].IdReserva = value.IdReserva;

				});
				//formas de pago por defecto cuando data.formasDePago =[]
				//$scope.data.formasDePagoDefecto = respuesta.P_OUT_FORMASDEPAGO;
				if ($scope.confBasicaFormaDePagoEntregamos.dataSource.data().length == 0 && $scope.confBasicaFormaDePagoRecibimos.dataSource.data().length == 0) {
					respuesta.P_OUT_FORMASDEPAGO.forEach(fmp => {
						let fmpUI = []
						fmp.Monto = fmp.MontoFormaDePago
						if (fmp.RecibePagaPortafolio == 'R' && ($scope.data.oConfiguracionTipoOperacion.idActual === 'VENPAC')) {
							fmp.FechaLiquidacion = util.dateSPdate($scope.data.fechaVencimientoOperacion || '0001-01-01')
							$scope.confBasicaFormaDePagoRecibimos.dataSource.add(fmp);
							fmpUI.push(fmp)
							$scope.data.formaDePagoRecibimos = fmpUI;
							
						}
						if (fmp.RecibePagaPortafolio == 'P' && ($scope.data.oConfiguracionTipoOperacion.idActual === 'VENPAC')) {
							fmp.FechaLiquidacion = util.dateSPdate($scope.data.fechaLiquidacionOperacion || '0001-01-01')
							$scope.confBasicaFormaDePagoEntregamos.dataSource.add(fmp);
							fmpUI.push(fmp)
							$scope.data.formaDePagoEntregamos = fmpUI;
						}
						if (fmp.RecibePagaPortafolio == 'P' && ($scope.data.oConfiguracionTipoOperacion.idActual === 'COMPAC')) {
							fmp.FechaLiquidacion = util.dateSPdate($scope.data.fechaLiquidacionOperacion || '0001-01-01')
							$scope.confBasicaFormaDePagoEntregamos.dataSource.add(fmp);
							fmpUI.push(fmp)
							$scope.data.formaDePagoEntregamos = fmpUI;
						}
						if (fmp.RecibePagaPortafolio == 'R' && ($scope.data.oConfiguracionTipoOperacion.idActual === 'COMPAC')) {
							fmp.FechaLiquidacion = util.dateSPdate($scope.data.fechaVencimientoOperacion || '0001-01-01')
							$scope.confBasicaFormaDePagoRecibimos.dataSource.add(fmp);
							fmpUI.push(fmp)
							$scope.data.formaDePagoRecibimos = fmpUI;
						}
						if (fmp.RecibePagaPortafolio == 'P' && ($scope.data.oConfiguracionTipoOperacion.idActual === 'COMTER'  || $scope.data.oConfiguracionTipoOperacion.idActual === 'VENTER')) {
							fmp.FechaLiquidacion = util.dateSPdate($scope.data.fechaLiquidacionOperacion || '0001-01-01')
							$scope.confBasicaFormaDePagoEntregamos.dataSource.add(fmp);
							fmpUI.push(fmp)
							$scope.data.formaDePagoEntregamos = fmpUI;
						}

					})
				}

				$scope.confBasicaFormaDePagoRecibimos.dataSource.sync();
				$scope.confBasicaFormaDePagoEntregamos.dataSource.sync();
				$scope.data.EnProcesoGrabar = false;
			}
			else {
				
				var data = $scope.data.gridApiCarteraInstrumento.dataSource.data();
				
				data.forEach(function(item){
					if(item.IdReserva === null || item.IdReserva === undefined){
						$scope.data.gridApiCarteraInstrumento.dataSource.data().remove(item);
					}
				});
					
				calcularMontoSimulacion();
				
				var id = bcsComunes.ventanaMsg({
					conflictos: util.parseServerResponse(respuesta)
				}, $scope.winID);

				bcsComunes.ventanaPromesa(id).then(function (result) {					
				});
				$scope.data.EnProcesoGrabar = false;
				$scope.data.cierraVentana = false;
			}

			$scope.data.cerrando = false;
		};
		var error = function (respuesta) {
			var id = bcsComunes.ventanaMsg({
				conflictos: util.parseServerResponse(respuesta)
			}, $scope.winID);
			bcsComunes.ventanaPromesa(id).then(function (result) {
				bcsComunes.ventanaOverlay($scope.winID, false);
			});
		};

		var ErrGrabaDetalle = function (respuesta) {
			var id = bcsComunes.ventanaMsg({

				conflictos: util.parseServerResponse(respuesta)
			}, $scope.winID);
			
 			bcsComunes.ventanaPromesa(id).then(function (result) {
				bcsComunes.ventanaOverlay($scope.winID, false);
			});
			
			if ($scope.data.agregaPapel) {
				if (schemaGridCarteraNueva !== null) {
			        $scope.data.gridApiCarteraInstrumento.dataSource.data(schemaGridCarteraNueva);
			        $scope.data.gridApiCarteraInstrumento.dataSource.sync();
			        schemaGridCarteraNueva = null;
			    } else {
			        $scope.data.gridApiCarteraInstrumento.dataSource.removeAll();
			        $scope.data.gridApiCarteraInstrumento.dataSource.sync();
			    }
			    
			    $scope.data.errorGrabaDetalle = true;
			    $scope.data.EnProcesoGrabar = false;
			    calcularMontoSimulacion();
			    $scope.data.agregaPapel = false;
			    $scope.data.EnProcesoGrabar = false;
			    $scope.data.errorGrabaDetalle = false;
			} 
		}

		var resetCabecera = function () {

			$scope.data.oClientePortafolio.idActual = false;
			$scope.data.oConfiguracionTipoReajusteOperacion.idActual = false;
			$scope.data.oConfiguracionMonedaLiquidacionOperacion.idActual = false;
			$scope.data.SesionIdEmpresa = $scope.SesionIdEmpresa;
			$scope.data.SesionIdUsuario = $scope.SesionIdUsuario;
			$scope.data.kngFechaOperacion = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
			$scope.data.kngMontoOperacion = '';
			$scope.data.kngTasaOperacion = '';
			$scope.data.kngMontoOperacionMoneda = '';
			$scope.data.kngSecuenciaOperacion = '';
			$scope.data.kngFolioOperacion = '';
			$scope.data.plazoOperacion = '';
			$scope.data.IdCondicionDeLiquidacion = '';
			$scope.data.EstadoOperacion = '';
			$scope.data.oConfiguracionTipoReajusteOperacion.idDefecto = $scope.data.IdMonedaMontoDisplay;
			$scope.data.oConfiguracionTipoReajusteOperacion.idActual = $scope.data.IdMonedaMontoDisplay;
			$scope.data.oConfiguracionMonedaLiquidacionOperacion.idDefecto = $scope.data.IdMonedaMontoDisplay;
			$scope.data.oConfiguracionMonedaLiquidacionOperacion.idActual = $scope.data.IdMonedaMontoDisplay;
			$scope.data.MontoMON_Org = 0;
			$scope.data.MontoCLP_Org = 0;
			$scope.data.EsVencimientoValido = false;


		};
		var resetPie = function () {
			$scope.data.NombreAgente = '';
			$scope.data.NombreOperador = $scope.SesionUsuario;
			$scope.data.kngTasaAgente = '';
		};


		var reset = function () {
			resetCabecera();
			enableCabeceraOperacion();
			resetPie();
			if ($scope.data.gridApiCarteraInstrumento) {
				$scope.data.gridApiCarteraInstrumento.dataSource.removeAll();
				$scope.data.gridApiCarteraInstrumento.dataSource.sync();
			}
			bcsServiciosMesa.ConsultarConfiguracionMesa(params).$promise.then(function (respuesta) {
				$.each(respuesta.P_OUT_DATOSMESA, function (key, value) {
					ParametrosMesa[value.CodigoParametro] = value.Valor;
				});

				$scope.data.habilitaVentaNominal = ParametrosMesa.ACEPTA_VENTA_NOMINAL_CORREDOR === 'S';
				$scope.data.MontoMaximoParaAjuste = ParametrosMesa.MONTO_MAXIMO_PARA_AJUSTE_SENCILLO;
				$scope.data.ConfirmaMediosPago = ParametrosMesa.CONFIRMA_OPERACIONES_MESARF;
				$scope.data.MontoMaximoParaAjuste = $scope.data.MontoMaximoParaAjuste === undefined ? 10000 : $scope.data.MontoMaximoParaAjuste;
			});
			$scope.data.limpiarFormaPago = true;

			// Para que tome el cliente que viene por default desde la
			// configuracion
			// Con la propiedad del HTML
			if (parametrosVentana.IdOperacion === false || parametrosVentana.IdOperacion === 0) {
				tmpId = sesion.getPreferencias("IdClienteRecordado");
				if (Number(tmpId) > 0) {
					// $scope.data.oConfiguracionSocioNegocio.idActual =
					// Number(tmpId);
					$scope.data.oClientePortafolio.idActual = Number(tmpId);
					tmpId = sesion.getPreferencias("portafolioRecordado");
					if (Number(tmpId) > 0) {
						$scope.data.oConfiguracionPortafolio.idActual = Number(tmpId);
					}
				}
			}
		};

		// * Carga datos de la operación
		// * -------------------------------
		var cargarOperacion = function () {
			var filtros = [];
			// para la modificacion

			if (parametrosVentana.IdOperacion && parametrosVentana.IdOperacion > 0) {

				bcsComunes.ventanaOverlay($scope.winID, true);
				$scope.data.habilitaCabecera = false;
				$scope.data.OperacionExiste = true;
				$scope.data.CargandoOperacion = true;
				$scope.data.CargandoDatosOperacionExistente = true;
				buscaParidad = false;
				$scope.data.IdPortafolioCorredora = parametrosVentana.IdPortafolioCorredor;
				filtros = bcsServiciosGrillasMesaRF.validarFiltros({
					"SesionIdEmpresa": $scope.SesionIdEmpresa
					, "IdOperacion": parametrosVentana.IdOperacion
				}
					, 'OPERACION');

				dataSourceDetalleOperacion.query({ filter: filtros }).then(function () {
					var datos = [];
					var data = dataSourceDetalleOperacion.data();
					$scope.data.kngSecuenciaOperacion;

					if (data.length > 0) {
						console.log("data");
						console.log(data[0]);
            //actualizamos filtros de FP
            $scope.renovarFiltrosModificacion(data[0]);
            	
						$scope.data.AfectaCustodia = data[0].AfectaCustodia == "S" ? true : false;
						
						// cargo datos de la cabecera
						// cargo la mesa en la que estoy realizando la operacion
						$scope.data.oConfiguracionMesas.idActual = Number(data[0].IdMesaDestino);
						$scope.data.oConfiguracionMesas.idDefecto = Number(data[0].IdMesaDestino);
						$scope.data.esIntermesa = data[0].Intermesa === 'S';
						// datos cliente
						$scope.data.IdCliente = Number(data[0].IdCliente);

						$scope.data.NombreCompleto = data[0].NombreCompleto;
						$scope.data.codigoPortafolioCliente = data[0].CodigoPortafolio;
						$scope.data.NombreCuentaPropia = data[0].NombreCuentaPropia;
						$scope.data.portafolioCuentaPropiaCodigo = data[0].portafolioCodigo;
						$scope.data.portafolioCuentaPropiaProposito = data[0].portafolioProposito;

						$scope.data.oClientePortafolio.idActual = Number(data[0].IdPortafolio);
						$scope.data.oClientePortafolio.idDefecto = Number(data[0].IdPortafolio);
						$scope.data.kngSecuenciaOperacion = Number(data[0].IdOperacion);
						$scope.data.kngFolioOperacion = Number(data[0].FolioOperacion);

						// tipoOperacion
						$scope.data.oConfiguracionTipoOperacion.idActual = data[0].IdTipoOperacion;
						$scope.data.oConfiguracionTipoOperacion.idDefecto = data[0].IdTipoOperacion;
						$scope.data.oConfiguracionTipoOperacion.seleccionado = { 'Accion': (data[0].IdTipoOperacion === 'COMPAC' || data[0].IdTipoOperacion === 'COMTER') ? 'COMPRA' : 'VENTA' };

						$scope.data.kngFechaOperacion = data[0].FechaOperacion;
						$scope.data.EstadoOperacion = data[0].NombreEstado;
						$scope.data.oConfiguracionTipoReajusteOperacion.idActual = data[0].IdTipoReajuste;
						$scope.data.oConfiguracionTipoReajusteOperacion.idDefecto = data[0].IdTipoReajuste;
						$scope.data.kngTasaOperacion = Number(data[0].Tasa);

						$scope.data.plazoOperacion = Number(data[0].PlazoEnDias);
						$scope.data.fechaVencimientoOperacion = data[0].FechaVencimiento;

						$scope.data.MontoMON_Org = Number(data[0].MontoOperacionInicialMoneda);;
						$scope.data.MontoCLP_Org = Number(data[0].MontoOperacionInicial);

						$scope.data.kngMontoOperacionMoneda = Number(data[0].MontoOperacionInicialMoneda);
						$scope.data.kngMontoValorFinalOperacionMoneda = Number(data[0].MontoOperacionFinalMoneda);

						$scope.data.kngMontoOperacion = Number(data[0].MontoOperacionInicial);
						$scope.data.kngMontoValorFinalOperacion = Number(data[0].MontoOperacionFinal);

						$scope.data.oConfiguracionMonedaLiquidacionOperacion.idActual = data[0].IdMonedaLiquidacion;
						$scope.data.oConfiguracionMonedaLiquidacionOperacion.idDefecto = data[0].IdMonedaLiquidacion;

						$scope.data.oConfiguracionDocumentos.idActual = data[0].TipoDocumento;
						$scope.data.oConfiguracionDocumentos.idDefecto = data[0].TipoDocumento;

						$scope.data.oConfiguracionContactos.idActual = data[0].IdContacto;
						$scope.data.oConfiguracionContactos.idDefecto = data[0].IdContacto;

						$scope.data.IdCondicionDeLiquidacionModif = data[0].IdCondicionLiquidacion;
						$scope.data.IdCondicionDeLiquidacion = data[0].IdCondicionLiquidacion;
						$scope.data.fechaLiquidacionOperacion = util.dateDS(util.dateSPdate(data[0].FechaCondicionLiquidacion), "FechaSistema", "CALL");

						$scope.data.NombreAgente = data[0].NombreEjecutivo;
						$scope.data.kngTasaAgente = Number(data[0].TasaAgente);
						$scope.data.NombreOperador = data[0].CodigoUsuario;
						$scope.data.IdEstadoOperacion = data[0].IdEstado;

						$scope.data.Comentarios = data[0].Glosa;

						$scope.data.kngTasaPactoAnualizada = data[0].TasaAnualizada;
						$scope.data.kngUtilidadPerdida = data[0].UtilidadPerdida
						$scope.data.kngCostoFondo = data[0].CostoFondo
						$scope.data.kngUtilidadFinal = data[0].UtilidadFinal

						// > Carga los papeles de la operacion que tiene
						if (data[0].IdReservaDetalle > 0) {
							// Si tiene papeles asociados prende flag para que
							// no grabe al inicio de la operacion nuevamente.
							$scope.data.CargarPapeles(data);
						}
						// > Solo para la primera vez
						$scope.data.IdTipoOperacionCartera = data[0].IdTipoOperacion;
						
						if($scope.data.AfectaCustodia || (!$scope.data.AfectaCustodia && $scope.data.oConfiguracionTipoOperacion.idActual.includes("VEN"))){
							$scope.data.buscarCartera();
							$timeout(function () {
								var wrapperCartera = $(
									"[ventanaid='" + $scope.ventanaCartera + "']"
								);

								wrapperCartera.one("focusin", function () {
									$timeout(function () {
										bcsComunes.toggleAllWatchers(
											$("[ventanaid='" + $scope.winID + "']"),
											false
										);
									}, 0);
								});
							}, 0);
						
						}
						
						
						
					}
					else {
						// no existe operacion
						bcsComunes.msg("error", $translate.instant('UI_ERROR'), $translate.instant("UI_OPERACION_INVALIDA"));
						bcsComunes.ventanaOverlay($scope.winID, false);
					}
				});
			}
			else {
				// cargo la mesa en la que estoy realizando la operacion
				$scope.data.oConfiguracionMesas.idActual = parametrosVentana.IdMesa;
				$scope.data.oConfiguracionMesas.idDefecto = parametrosVentana.IdMesa;
				$scope.data.IdPortafolioCorredora = parametrosVentana.IdPortafolioCorredor;
			}
		};

		var calcularParidadMonto = function (montoLocal, callback) {
			var IdMonedaOrigen = "CLP";
			var IdMonedaDestino = "CLP";
			var data = Object();
			var montoOrigen = 0;
			if (!$scope.data.oConfiguracionTipoReajusteOperacion.idActual) {// } ||
				// Number($scope.data.kngMontoOperacionMoneda)
				// <=
				// 0){
				$scope.data.MontoCLP_Org = 0;
				$scope.data.MontoMON_Org = 0;
				$scope.data.kngMontoOperacion = 0;
				$scope.data.kngMontoOperacionMoneda = 0;
				if (callback) callback();
			}
			else {

				IdMonedaOrigen = $scope.data.oConfiguracionTipoReajusteOperacion.idActual;
				IdMonedaDestino = $scope.data.IdMonedaMontoDisplay;
				montoOrigen = $scope.data.kngMontoOperacionMoneda
				if ($scope.data.oConfiguracionTipoReajusteOperacion.idActual === 'UF' && montoLocal) {
					IdMonedaOrigen = !montoLocal ? $scope.data.oConfiguracionTipoReajusteOperacion.idActual || $scope.data.IdMonedaMontoDisplay : $scope.data.IdMonedaMontoDisplay;
					IdMonedaDestino = !montoLocal ? $scope.data.IdMonedaMontoDisplay : $scope.data.oConfiguracionTipoReajusteOperacion.idActual || $scope.data.IdMonedaMontoDisplay;
					montoOrigen = !montoLocal ? $scope.data.kngMontoOperacionMoneda || 0 : $scope.data.kngMontoOperacion || 0;
				}

				data.p_IdEmpresa = $scope.SesionIdEmpresa;
				data.p_IdTrabajo = 1315;
				data.p_CalculosEntrada = [{
					"IdTrabajo": data.p_IdTrabajo
					, "IdInterno": 1
					, "Fecha": util.dateSPdate($scope.data.kngFechaOperacion)
					, "IdMonedaOrigen": IdMonedaOrigen
					, "IdMonedaDestino": IdMonedaDestino
					, "ValorCambioLocalModificado": 0
					, "MontoOrigen": montoOrigen
					, "IdUso": 'MESA'
				}];

				bcsServiciosMesa.ObtenerParidadTipoCambio(data, function (value) {
					if (value.p_out_CalculosSalida.length > 0) {

						var montoLoc = value.p_out_CalculosSalida[0].MontoLocal;
						var montoDes = value.p_out_CalculosSalida[0].MontoDestino <= 0 ? value.p_out_CalculosSalida[0].MontoOrigen : value.p_out_CalculosSalida[0].MontoDestino;
						var montoOri = value.p_out_CalculosSalida[0].MontoOrigen;


						if (value.p_out_CalculosSalida[0].IdMonedaOrigen === value.p_out_CalculosSalida[0].IdMonedaDestino) {
							$scope.data.kngMontoOperacion = value.p_out_CalculosSalida[0].MontoOrigen;
							$scope.data.kngMontoOperacionMoneda = value.p_out_CalculosSalida[0].MontoLocal;
							$scope.data.MontoMON_Org = $scope.data.kngMontoOperacionMoneda;
							$scope.data.MontoCLP_Org = $scope.data.kngMontoOperacion;
						}
						else {
							if (Number($scope.data.kngMontoOperacionMoneda) !== Number($scope.data.MontoMON_Org) || Number($scope.data.kngMontoOperacionMoneda) != Number(montoLoc)
								|| (Number($scope.data.kngMontoOperacionMoneda) === Number(montoLoc) && Number($scope.data.kngMontoOperacion) != Number(montoDes))) {

								if ($scope.data.IdMonedaMontoDisplay != IdMonedaOrigen) {
									$scope.data.kngMontoOperacion = value.p_out_CalculosSalida[0].MontoLocal;
									$scope.data.MontoMON_Org = $scope.data.kngMontoOperacionMoneda;
									$scope.data.MontoCLP_Org = $scope.data.kngMontoOperacion;
								} else {
									$scope.data.kngMontoOperacion = value.p_out_CalculosSalida[0].MontoOrigen;
									$scope.data.kngMontoOperacionMoneda = value.p_out_CalculosSalida[0].MontoLocal;
									$scope.data.MontoMON_Org = $scope.data.kngMontoOperacionMoneda;
									$scope.data.MontoCLP_Org = $scope.data.kngMontoOperacion;
								}
							}

							if (Number($scope.data.kngMontoOperacion) !== Number($scope.data.MontoCLP_Org) || Number($scope.data.kngMontoOperacion) != Number(montoDes)) {
								$scope.data.kngMontoOperacionMoneda = value.p_out_CalculosSalida[0].MontoDestino <= 0 ? value.p_out_CalculosSalida[0].MontoOrigen : value.p_out_CalculosSalida[0].MontoDestino;
								$scope.data.MontoMON_Org = $scope.data.kngMontoOperacionMoneda;
								$scope.data.MontoCLP_Org = $scope.data.kngMontoOperacion;
							}
						}
					}
					else {
						$scope.data.kngMontoOperacion = 0;
						$scope.data.MontoCLP_Org = 0;
					}
					if (callback) callback();
				});
			}
		};

		var actualizarWidgetMonetarioPie = function (idComponente, valor) {
			var elemento = document.getElementById(idComponente);

			if (!elemento) {
				return;
			}

			var componente = angular.element(elemento);
			var scopeComponente = componente.isolateScope();
			var widget = scopeComponente && scopeComponente.kendoWidget;

			if (!widget) {
				componente.find('input').each(function () {
					var widgetActual = angular.element(this).data('kendoNumericTextBox');

					if (widgetActual) {
						widget = widgetActual;
						return false;
					}
				});
			}

			if (!widget) {
				return;
			}

			var valorNumerico = Number(valor);

			if (isNaN(valorNumerico)) {
				valorNumerico = 0;
			}

			widget.value(valorNumerico);
		};

		var refrescarMontosIngresoPie = function () {
			$timeout(function () {
				actualizarWidgetMonetarioPie('frmIngresoPie_MontoAsignado',	$scope.data.kngMontoAsignado);
				actualizarWidgetMonetarioPie('frmIngresoPie_MontoDiferencia', $scope.data.kngMontoDiferencia);
				actualizarWidgetMonetarioPie('frmIngresoPie_MontoFinal', $scope.data.kngMontoFinal);
				actualizarWidgetMonetarioPie('frmIngresoPie_MontoLiquidacion', $scope.data.kngMontoAsignadoMonedaLiquidacion);
			}, 0);
		};

		var calcularMontoSimulacion = function () {
			var val = 0;
			var val2 = 0;
			var d = $scope.data.gridApiCarteraInstrumento ? $scope.data.gridApiCarteraInstrumento.dataSource.data() : [];
			$.each(d, function (key, value) {
				val += value.Monto;// || 0;
				val2 += value.MontoLiquidacionOperacion;// || 0;
			});
			$scope.data.kngMontoAsignadoAnterior = $scope.data.kngMontoAsignado
			$scope.data.kngMontoAsignado = $scope.data.oConfiguracionTipoReajusteOperacion.idActual === "UF" ? val : val2;
			$scope.data.kngMontoAsignadoMonedaLiquidacion = val2;
			$scope.data.IngresarDetalleOperacion($scope.data.kngMontoAsignado,$scope.data.kngMontoAsignadoAnterior);
			$scope.data.kngMontoDiferencia = $scope.data.oConfiguracionTipoReajusteOperacion.idActual === "UF" ? Number($scope.data.kngMontoOperacion) - Number(val) : Number($scope.data.kngMontoOperacionMoneda) - Number(val2);
			$scope.data.kngMontoFinal = Number($scope.data.kngMontoAsignado) + Number($scope.data.kngMontoDiferencia)
			refrescarMontosIngresoPie();
		};

		var calcularMontoOperacionMoneda = function () {
			var monto = 0;
			if ($scope.data.kngMontoOperacionMoneda && $scope.data.plazoOperacion && $scope.data.kngTasaOperacion) {
				if ($scope.data.oConfiguracionTipoReajusteOperacion.seleccionado.IdTipoReajuste != $scope.data.IdMonedaMontoDisplay) {
					monto = Number(calcularMontoaPlazo(Number($scope.data.kngMontoOperacionMoneda), Number($scope.data.plazoOperacion), Number($scope.data.kngTasaOperacion), 360)).toFixed($scope.data.IdDecimalTipoReajusteDisplay);
				}
				else {
					monto = Number(bcsComunes.calcularMontoPlazo(Number($scope.data.kngMontoOperacionMoneda), Number($scope.data.plazoOperacion), Number($scope.data.kngTasaOperacion))).toFixed($scope.data.IdDecimalTipoReajusteDisplay);
				}

				if (!$scope.data.CargandoDatosOperacionExistente) {
					$scope.data.kngMontoValorFinalOperacionMoneda = Number(monto);
				}

				var totalFormaDePagoRecibimos = 0;
				var porcentajeRecibimos = 1;
				if ($scope.data.formaDePagoRecibimos.size > 0) {


					$scope.data.formaDePagoRecibimos.forEach(element => {
						totalFormaDePagoRecibimos = totalFormaDePagoRecibimos + element.MontoFormaDePago;
					});

					$scope.data.formaDePagoRecibimos.forEach(element => {

						if ($scope.data.formaDePagoRecibimos.size > 1)
							porcentajeRecibimos = (element.MontoFormaDePago) / totalFormaDePagoRecibimos;
						else
							porcentajeRecibimos = 1;
						//kngMontoValorFinalOperacion
						// Para VENPAC o VENTER  la Forma de pago Recibimos toma el Valor Inicial
						if ($scope.data.oConfiguracionTipoOperacion.idActual === "VENPAC" || $scope.data.oConfiguracionTipoOperacion.idActual === "VENTER") {
							element.MontoFormaDePago = $scope.data.oConfiguracionTipoReajusteOperacion.seleccionado.IdTipoReajuste === "UF" ? $scope.data.kngMontoOperacion * porcentajeRecibimos : $scope.data.kngMontoOperacionMoneda * porcentajeRecibimos;
						}
						else {  // Para  COMPAC o COMTER la Forma de pago Recibimos toma el Valor Final
							element.MontoFormaDePago = $scope.data.oConfiguracionTipoReajusteOperacion.seleccionado.IdTipoReajuste === "UF" ? $scope.data.kngMontoValorFinalOperacion * porcentajeRecibimos : $scope.data.kngMontoValorFinalOperacionMoneda * porcentajeRecibimos;
						}
					});
				}

				var totalFormaDePagoEntregamos = 0;
				var porcentajeEntregamos = 1;
				if ($scope.data.formaDePagoEntregamos.size > 0) {

					$scope.data.formaDePagoEntregamos.forEach(element => {
						totalFormaDePagoEntregamos = totalFormaDePagoEntregamos + element.MontoFormaDePago;
					});

					$scope.data.formaDePagoEntregamos.forEach(element => {

						if ($scope.data.formaDePagoEntregamos.size > 1)
							porcentajeEntregamos = (element.MontoFormaDePago) / totalFormaDePagoEntregamos;
						else
							porcentajeEntregamos = 1;

						// Para VENPAC o VENTER  la Forma de pago Entregamos toma el Valor Final
						if ($scope.data.oConfiguracionTipoOperacion.idActual === "VENPAC" || $scope.data.oConfiguracionTipoOperacion.idActual === "VENTER") {
							element.MontoFormaDePago = $scope.data.oConfiguracionTipoReajusteOperacion.seleccionado.IdTipoReajuste === "UF" ? $scope.data.kngMontoValorFinalOperacion * porcentajeRecibimos : $scope.data.kngMontoValorFinalOperacionMoneda * porcentajeEntregamos;
						}
						else {  // Para  COMPAC o COMTER la Forma de pago Entregamos toma el Valor Inicial	
							element.MontoFormaDePago = $scope.data.oConfiguracionTipoReajusteOperacion.seleccionado.IdTipoReajuste === "UF" ? $scope.data.kngMontoOperacion * porcentajeRecibimos : $scope.data.kngMontoOperacionMoneda * porcentajeEntregamos;
						}

					});
				}





			}
			else if (!$scope.data.CargandoDatosOperacionExistente) {
				$scope.data.kngMontoValorFinalOperacionMoneda = $scope.data.kngMontoOperacionMoneda;
			}
		};

		var calcularMontoOperacion = function () {
			var monto = 0;
			if ($scope.data.kngMontoOperacion && $scope.data.plazoOperacion && $scope.data.kngTasaOperacion) {
				if ($scope.data.oConfiguracionTipoReajusteOperacion.seleccionado.IdTipoReajuste != $scope.data.IdMonedaMontoDisplay) {
					if ($scope.data.oConfiguracionTipoReajusteOperacion.idActual == 'CLP') {
						monto = Number(bcsComunes.calcularMontoPlazo(Number($scope.data.kngMontoOperacion), Number($scope.data.plazoOperacion), Number($scope.data.kngTasaOperacion))).toFixed($scope.data.IdDecimalMontoDisplay);
					}
					else {
						monto = Number(calcularMontoaPlazo(Number($scope.data.kngMontoOperacion), Number($scope.data.plazoOperacion), Number($scope.data.kngTasaOperacion), 360)).toFixed($scope.data.IdDecimalMontoDisplay);
					}
				}
				else {
					monto = Number(bcsComunes.calcularMontoPlazo(Number($scope.data.kngMontoOperacion), Number($scope.data.plazoOperacion), Number($scope.data.kngTasaOperacion))).toFixed($scope.data.IdDecimalMontoDisplay);

				}
				if (!$scope.data.CargandoDatosOperacionExistente) {
					$scope.data.kngMontoValorFinalOperacion = Number(monto);
				}
			}
			else if (!$scope.data.CargandoDatosOperacionExistente) {
				$scope.data.kngMontoValorFinalOperacion = $scope.data.kngMontoOperacion;
			}
			calcularMontoSimulacion();

		};

		var deshabilitaIntermesa = function () {
			Push('deshabilitaIntermesa');
			$scope.data.deshabilitaIntermesa = true;
			Pop();
		};

		var setData = function (opcion) {
			Push('setData');
			var datosParam = new Object();
			var carteras = [];
			var formaDePagoUI = [];
			// var formaDePagoRecibimos = [];
			var fechaVen = '';
			fechaVen = new Date($scope.data.fechaVencimientoOperacion || $scope.SesionFechaSistema || '0001-01-01');

			datosParam.p_IdEmpresa = $scope.SesionIdEmpresa;
			datosParam.p_IdUsuario = $scope.SesionIdUsuario;
			datosParam.p_IdModulo = 'MRF';
			datosParam.p_UsaReglasNegocio = (opcion === 'CERRAR' ? 'S' : 'N');
			datosParam.p_IdEstadoConfirmacionPago = 'NCF';
			if ($scope.data.oConfiguracionTipoOperacion.idActual != "COMPAC" && $scope.data.oConfiguracionTipoOperacion.idActual != "VENPAC") {

				$scope.data.kngTasaPactoAnualizada = 0;
				$scope.data.kngCostoFondo = 0;
				$scope.data.kngUtilidadPerdida = 0;
				$scope.data.kngUtilidadFinal = 0;
			}
			datosParam.p_Operacion = [{
				"Accion": opcion
				, "IdInternoOperacion": 1
				, "IdOperacion": Number($scope.data.kngSecuenciaOperacion || 0)
				, "FolioOperacion": Number($scope.data.kngFolioOperacion || 0)
				, "IdCliente": Number($scope.data.oClientePortafolio.seleccionado.IdCliente)// Number(opcion==='PENDIENTE'?$scope.data.IdCliente:$scope.data.oConfiguracionPortafolio.seleccionado.IdCliente)
				, "IdPortafolio": Number($scope.data.oClientePortafolio.seleccionado.IdPortafolio)// Number(opcion==='PENDIENTE'?$scope.data.IdPortafolio:$scope.data.oConfiguracionPortafolio.seleccionado.IdPortafolio)
				, "IdClienteCorredor": Number(parametrosVentana.IdClienteCorredor)
				, "IdPortafolioCorredor": Number(parametrosVentana.IdPortafolioCorredor)
				, "Intermesa": $scope.data.esIntermesa ? 'S' : 'N'
				, "IdMesaOrigen": Number(parametrosVentana.IdMesa)
				, "IdMesaDestino": Number($scope.data.oConfiguracionMesas.seleccionado.IdMesa)

				, "AfectaCustodia": $scope.data.formaCustodiaSeleccionada.IdFormaCustodiaOrden == "CUS" ? 'S' : 'N'



				, "IdTipoProducto": $scope.data.oConfiguracionTipoOperacion.seleccionado.IdTipoProducto
				, "IdTipoOperacion": $scope.data.oConfiguracionTipoOperacion.seleccionado.IdTipoOperacion
				, "IdTipoReajuste": $scope.data.oConfiguracionTipoReajusteOperacion.seleccionado.IdTipoReajuste // Tipo reajuste
				, "Plazo": Number($scope.data.plazoOperacion || 0)
				, "Monto": Number($scope.data.kngMontoOperacion || 0)
				, "MontoMoneda": Number($scope.data.kngMontoOperacionMoneda || 0) // cambiar al monto en moneda local 
				, "MontoLiquidacion": Number($scope.data.kngMontoAsignadoMonedaLiquidacion || 0)
				, "IdMoneda": $scope.data.oConfiguracionMonedaLiquidacionOperacion.seleccionado.IdMonedaLiquidacion // $scope.data.IdMonedaMontoDisplay moneda local
				, "IdMonedaLiquidacion": $scope.data.oConfiguracionMonedaLiquidacionOperacion.seleccionado.IdMonedaLiquidacion // moneda de liquidacion de la operacion
				, "Tasa": Number($scope.data.kngTasaOperacion || 0)
				, "ValorFinal": Number($scope.data.kngMontoValorFinalOperacion || 0)
				, "ValorFinalMoneda": Number($scope.data.kngMontoValorFinalOperacionMoneda || 0)
				, "TasaAgente": Number($scope.data.kngTasaAgente || 0)
				, "FechaOperacion": util.dateSPdate($scope.data.kngFechaOperacion)
				, "FechaVencimiento": util.dateSPdate(fechaVen)
				, "IdCondicionLiquidacion": $scope.data.IdCondicionDeLiquidacion
				, "FechaLiquidacion": util.dateSPdate($scope.data.fechaLiquidacionOperacion || '0001-01-01')
				, "IdAgente": Number($scope.data.oClientePortafolio.seleccionado.IdPersonaEjecutivo || 0)
				, "IdOperador": $scope.SesionIdUsuario
				, "IdEstado": $scope.data.IdEstadoOperacion || ''// $scope.data.oConfiguracionEstado.seleccionado
				, "Glosa": $scope.data.Comentarios || ''
				, "IdTipoCanalIngreso": $scope.SesionTipoCanalIngreso
				, "TipoDocumento": $scope.data.oConfiguracionDocumentos.idActual || ""
				, "IdContacto": $scope.data.oConfiguracionContactos.idActual || 0
				, "TasaAnualizada": $scope.data.kngTasaPactoAnualizada || 0
				, "CostoFondo": $scope.data.kngCostoFondo || 0
				, "UtilidadPerdida": $scope.data.kngUtilidadPerdida || 0
				, "UtilidadFinal": $scope.data.kngUtilidadFinal || 0
				, "GuardandoCabecera" : ((opcion === 'MODIFICAR' && $scope.data.operacionEnModificacion !== null) || opcion === 'CREAR') ? "S" : "N"
				, "ValorizacionManual" :$scope.data.ValorizacionManual



			}];

			if(opcion === 'MODIFICAR' && $scope.data.operacionEnModificacion === null) $scope.data.operacionEnModificacion = datosParam.p_Operacion[0];
			$.each($scope.data.gridApiCarteraInstrumento.dataSource.data(), function (key, value) {
				// Ahora todo lo que esta en la grilla se graba...
				// if (value.MontoSimulacion > 0 || value.IdReserva > 0){
				if ($scope.data.oConfiguracionTipoReajusteOperacion.idActual == 'CLP') {
					value.INGMontoAPlazoDetalle = Number(bcsComunes.calcularMontoPlazo(Number(value.Monto), Number($scope.data.plazoOperacion), Number($scope.data.kngTasaOperacion)));
					value.INGMontoAPlazoMonedaInstrumentoDetalle = Number(bcsComunes.calcularMontoPlazo(Number(value.MontoLiquidacionOperacion), Number($scope.data.plazoOperacion), Number($scope.data.kngTasaOperacion)));
				}
				else {
					value.INGMontoAPlazoDetalle = Number(calcularMontoaPlazo(Number(value.Monto), Number($scope.data.plazoOperacion), Number($scope.data.kngTasaOperacion),360));
					value.INGMontoAPlazoMonedaInstrumentoDetalle = Number(calcularMontoaPlazo(Number(value.MontoLiquidacionOperacion), Number($scope.data.plazoOperacion), Number($scope.data.kngTasaOperacion),360));
				}
				carteras.push({
					"IdInternoDetalle": key
					, "IdInternoOperacion": 1
					, "IdReserva": Number(value.IdReserva) || 0
					, "IdCliente": Number($scope.data.oClientePortafolio.seleccionado.IdCliente)
					, "IdPortafolio": Number($scope.data.oClientePortafolio.seleccionado.IdPortafolio)
					, "IdClienteCorredor": Number(parametrosVentana.IdClienteCorredor)
					, "IdPortafolioCorredor": Number(parametrosVentana.IdPortafolioCorredor)
					, "IdMesa": Number(parametrosVentana.IdMesa)// Number(value.IdMesa)
					, "IdTipoProducto": value.IdTipoProducto
					, "IdTipoOperacion": value.IdTipoOperacion
					, "IdClasificacionTipoInstrumento": value.IdClasificacionTipoInstrumento
					// value.IdClasificacionTipoInstrumento
					, "IdInstrumento": value.IdInstrumento
					, "Nemotecnico": value.Nemotecnico
					, "EsFungible": value.EsFungible
					, "IdBolsa": value.IdBolsa || "BSC"
					, "IdBoveda": value.IdBoveda
					, "FolioCartera": value.FolioCartera
					, "IdCartera": value.IdCartera
					, "CodigoIFRS": value.CodigoIFRS ? value.CodigoIFRS : parametrosVentana.CodigoIFRS
					, "IdEmisor": value.IdEmisor
					, "IdMoneda": $scope.data.IdMonedaMontoDisplay // moneda
					// Local
					, "IdTipoReajuste": value.IdTipoReajuste
					, "IdMonedaEmisionInstrumento": value.IdTipoReajuste 
					, "IdMonedaLiquidacionInstrumento": value.MonedaInstrumento
					, "IdMonedaLiquidacionOperacion": $scope.data.oConfiguracionMonedaLiquidacionOperacion.seleccionado.IdMonedaLiquidacion
					, "CorteMinimo": Number(value.CorteMinimo || 1)
					, "Cantidad": Number(value.INGCantidad || 0)
					, "Precio": Number(value.INGPrecioCompraActual || 0)
					, "PrecioMercado": Number(value.INGPrecioMercadoActual || 0)
					, "Tasa": Number(value.INGTasaCompra || 0)
					, "TasaMercado": Number(value.INGTasaMercadoActual || 0)
					// ,"Monto" :
					// Number(value.IdClasificacionTipoInstrumento==="RF"?0:value.ValorRescateSimulacion
					// || 0)
					, "Monto": Number(value.Monto || 0)
					, "MontoLiquidacion": Number(value.MontoLiquidacion || 0)
					, "MontoLiquidacionOperacion": Number(value.MontoLiquidacionOperacion || 0)
					, "ValorRescate": Number(value.INGValorRescate || 0)
					, "FechaVencimiento": util.dateSPdate(value.FechaVencimientoInstrumento || '0001-01-01')
					, "MontoValorizadoReserva": Number(value.MontoValorizadoReserva || 0)
					, "TipoCarteraCorredor": value.TipoCarteraCorredor || ''
					, "INGMontoAPlazoDetalle" : value.INGMontoAPlazoDetalle || 0
					, "INGMontoAPlazoMonedaInstrumentoDetalle" : value.INGMontoAPlazoMonedaInstrumentoDetalle || 0
				});
			});


			datosParam.p_DetalleOperacion = carteras;

			if (opcion === 'CERRAR') {		
				var IdInternoPagoOperacionEntregamos	= 0;
				var IdInternoPagoOperacionRecibimos	= 0;
				$scope.data.formaDePagoEntregamos.forEach(function (item) {
					IdInternoPagoOperacionEntregamos++;
					let entradaFormaPago = {};
					entradaFormaPago.AfectarTesoreria = item.AfectarTesoreria;
					entradaFormaPago.FechaLiquidacion = item.FechaLiquidacionComprobantePagoOperacion;
					entradaFormaPago.FechaVencimiento = item.FechaLiquidacionComprobantePagoOperacion;
					entradaFormaPago.IdComprobantePagoOperacion = item.IdComprobantePagoOperacion;
					entradaFormaPago.IdCuentaBancaria = item.IdCuentaBanco||item.IdCuentaBancaria ||0;
					entradaFormaPago.IdFormaDePago = item.IdFormaDePago;
					entradaFormaPago.IdInternoOperacion = 1;
					entradaFormaPago.IdInternoPagoOperacion = item.IdInternoPagoOperacion || IdInternoPagoOperacionEntregamos;

					entradaFormaPago.IdMoneda = item.IdMoneda;
					entradaFormaPago.IdOperacion = item.IdOperacion;
					entradaFormaPago.IdTipoCuentaBancaria = "";
					entradaFormaPago.MontoFormaDePago = item.Monto;
					entradaFormaPago.NombreCuentaBancaria = "";
					entradaFormaPago.RecibePagaPortafolio = item.RecibePagaPortafolio;

					entradaFormaPago.NombreFormaDePago = item.NombreFormaDePago;
					entradaFormaPago.NumeroCuentaBancaria = item.CuentaBancaria;
					entradaFormaPago.IdModalidadDePago = item.IdModalidadPago;
					entradaFormaPago.IdMedioDePago = item.IdMedioDePago;

					formaDePagoUI.push(entradaFormaPago);
				});
				$scope.data.formaDePagoRecibimos.forEach(function (item) {
					IdInternoPagoOperacionRecibimos++;
					let entradaFormaPago = {};
					entradaFormaPago.AfectarTesoreria = item.AfectarTesoreria;
					entradaFormaPago.FechaLiquidacion = item.FechaLiquidacionComprobantePagoOperacion;
					entradaFormaPago.FechaVencimiento = item.FechaLiquidacionComprobantePagoOperacion;
					entradaFormaPago.IdComprobantePagoOperacion = item.IdComprobantePagoOperacion;
					entradaFormaPago.IdCuentaBancaria = item.IdCuentaBanco||item.IdCuentaBancaria ||0;
					entradaFormaPago.IdFormaDePago = item.IdFormaDePago;
					entradaFormaPago.IdInternoOperacion = 1;
					entradaFormaPago.IdInternoPagoOperacion = item.IdInternoPagoOperacion || IdInternoPagoOperacionRecibimos;

					entradaFormaPago.IdMoneda = item.IdMoneda;
					entradaFormaPago.IdOperacion = item.IdOperacion;
					entradaFormaPago.IdTipoCuentaBancaria = "";
					entradaFormaPago.MontoFormaDePago = item.Monto;
					entradaFormaPago.NombreCuentaBancaria = "";
					entradaFormaPago.RecibePagaPortafolio = item.RecibePagaPortafolio;

					entradaFormaPago.NombreFormaDePago = item.NombreFormaDePago;
					entradaFormaPago.NumeroCuentaBancaria = item.CuentaBancaria;
					entradaFormaPago.IdModalidadDePago = item.IdModalidadPago;
					entradaFormaPago.IdMedioDePago = item.IdMedioDePago;
					formaDePagoUI.push(entradaFormaPago);
				});
				datosParam.p_FormasDePago = formaDePagoUI;

			}
      
      	if (opcion === 'MODIFICAR' && formaDePagoUI.length ==0  && $scope.operacionModificadaGuardaFP(datosParam.p_Operacion[0])) {		
				var IdInternoPagoOperacionEntregamos	= 0;
				var IdInternoPagoOperacionRecibimos	= 0;
				$scope.data.formaDePagoEntregamos.forEach(function (item) {
					IdInternoPagoOperacionRecibimos++;
					let entradaFormaPago = {};
					entradaFormaPago.AfectarTesoreria = item.AfectarTesoreria;
					entradaFormaPago.FechaLiquidacion = item.FechaLiquidacionComprobantePagoOperacion;
					entradaFormaPago.FechaVencimiento = item.FechaLiquidacionComprobantePagoOperacion;
					entradaFormaPago.IdComprobantePagoOperacion = item.IdComprobantePagoOperacion;
					entradaFormaPago.IdCuentaBancaria = item.IdCuentaBanco||item.IdCuentaBancaria ||0;
					entradaFormaPago.IdFormaDePago = item.IdFormaDePago;
					entradaFormaPago.IdInternoOperacion = 1;
					entradaFormaPago.IdInternoPagoOperacion = item.IdInternoPagoOperacion || IdInternoPagoOperacionEntregamos;

					entradaFormaPago.IdMoneda = item.IdMoneda;
					entradaFormaPago.IdOperacion = item.IdOperacion;
					entradaFormaPago.IdTipoCuentaBancaria = "";
					entradaFormaPago.MontoFormaDePago = item.Monto;
					entradaFormaPago.NombreCuentaBancaria = "";
					entradaFormaPago.RecibePagaPortafolio = item.RecibePagaPortafolio;

					entradaFormaPago.NombreFormaDePago = item.NombreFormaDePago;
					entradaFormaPago.NumeroCuentaBancaria = item.CuentaBancaria;
					entradaFormaPago.IdModalidadDePago = item.IdModalidadPago;
					entradaFormaPago.IdMedioDePago = item.IdMedioDePago;

					formaDePagoUI.push(entradaFormaPago);
				});
				$scope.data.formaDePagoRecibimos.forEach(function (item) {
					IdInternoPagoOperacionRecibimos++;
					let entradaFormaPago = {};
					entradaFormaPago.AfectarTesoreria = item.AfectarTesoreria;
					entradaFormaPago.FechaLiquidacion = item.FechaLiquidacionComprobantePagoOperacion;
					entradaFormaPago.FechaVencimiento = item.FechaLiquidacionComprobantePagoOperacion;
					entradaFormaPago.IdComprobantePagoOperacion = item.IdComprobantePagoOperacion;
					entradaFormaPago.IdCuentaBancaria = item.IdCuentaBanco||item.IdCuentaBancaria ||0;
					entradaFormaPago.IdFormaDePago = item.IdFormaDePago;
					entradaFormaPago.IdInternoOperacion = 1;
					entradaFormaPago.IdInternoPagoOperacion = item.IdInternoPagoOperacion || IdInternoPagoOperacionEntregamos;

					entradaFormaPago.IdMoneda = item.IdMoneda;
					entradaFormaPago.IdOperacion = item.IdOperacion;
					entradaFormaPago.IdTipoCuentaBancaria = "";
					entradaFormaPago.MontoFormaDePago = item.Monto;
					entradaFormaPago.NombreCuentaBancaria = "";
					entradaFormaPago.RecibePagaPortafolio = item.RecibePagaPortafolio;

					entradaFormaPago.NombreFormaDePago = item.NombreFormaDePago;
					entradaFormaPago.NumeroCuentaBancaria = item.CuentaBancaria;
					entradaFormaPago.IdModalidadDePago = item.IdModalidadPago;
					entradaFormaPago.IdMedioDePago = item.IdMedioDePago;
					formaDePagoUI.push(entradaFormaPago);
				});
				datosParam.p_FormasDePago = formaDePagoUI;

			}
			
			datosParam.p_FormasDePago = formaDePagoUI;

			Pop();
			return datosParam;
		};

		var setData2 = function () {
			
			var datosParam = new Object();
			var carteras = [];
			var formaDePagoUI = [];

			var fechaVen = '';
			fechaVen = new Date($scope.data.fechaVencimientoOperacion || $scope.SesionFechaSistema || '0001-01-01');

			datosParam.p_IdEmpresa = $scope.SesionIdEmpresa;
			datosParam.p_IdUsuario = $scope.SesionIdUsuario;
			datosParam.p_IdModulo = 'MRF';
			datosParam.p_UsaReglasNegocio = 'N';
			datosParam.p_IdEstadoConfirmacionPago = 'NCF';
			datosParam.p_OrigenProceso = 'MAN'
			if ($scope.data.oConfiguracionTipoOperacion.idActual != "COMPAC" && $scope.data.oConfiguracionTipoOperacion.idActual != "VENPAC") {
				$scope.data.kngTasaPactoAnualizada = 0;
				$scope.data.kngCostoFondo = 0;
				$scope.data.kngUtilidadPerdida = 0;
				$scope.data.kngUtilidadFinal = 0;
			}

			datosParam.p_Operacion = [{
				"Accion": 'MODIFICAR'
				, "IdInternoOperacion": 1
				, "IdOperacion": Number($scope.data.kngSecuenciaOperacion || 0)
				, "FolioOperacion": Number($scope.data.kngFolioOperacion || 0)
				, "FechaOperacion": util.dateSPdate($scope.data.kngFechaOperacion)
				, "FechaLiquidacion": util.dateSPdate($scope.data.fechaLiquidacionOperacion || '0001-01-01')
				, "IdCliente": Number($scope.data.oClientePortafolio.seleccionado.IdCliente)// Number(opcion==='PENDIENTE'?$scope.data.IdCliente:$scope.data.oConfiguracionPortafolio.seleccionado.IdCliente)
				, "IdPortafolio": Number($scope.data.oClientePortafolio.seleccionado.IdPortafolio)// Number(opcion==='PENDIENTE'?$scope.data.IdPortafolio:$scope.data.oConfiguracionPortafolio.seleccionado.IdPortafolio)
				, "IdClienteCorredor": Number(parametrosVentana.IdClienteCorredor)
				, "IdPortafolioCorredor": Number(parametrosVentana.IdPortafolioCorredor)
				, "Intermesa": $scope.data.esIntermesa ? 'S' : 'N'
				, "IdMesaOrigen": Number(parametrosVentana.IdMesa)
				, "IdMesaDestino": Number($scope.data.oConfiguracionMesas.seleccionado.IdMesa)
				, "AfectaCustodia": $scope.data.formaCustodiaSeleccionada.IdFormaCustodiaOrden == "CUS" ? 'S' : 'N'
				, "IdTipoProducto": $scope.data.oConfiguracionTipoOperacion.seleccionado.IdTipoProducto
				, "IdTipoOperacion": $scope.data.oConfiguracionTipoOperacion.seleccionado.IdTipoOperacion
				, "IdTipoReajuste": $scope.data.oConfiguracionTipoReajusteOperacion.seleccionado.IdTipoReajuste // Tipo reajuste
				, "Plazo": Number($scope.data.plazoOperacion || 0)
				, "Monto": Number($scope.data.kngMontoOperacion || 0)
				, "MontoMoneda": Number($scope.data.kngMontoOperacionMoneda || 0) // cambiar al monto en moneda local 
				, "MontoLiquidacion": Number($scope.data.kngMontoAsignadoMonedaLiquidacion || 0)
				, "IdMoneda": $scope.data.oConfiguracionMonedaLiquidacionOperacion.seleccionado.IdMonedaLiquidacion // $scope.data.IdMonedaMontoDisplay moneda local
				, "IdMonedaLiquidacion": $scope.data.oConfiguracionMonedaLiquidacionOperacion.seleccionado.IdMonedaLiquidacion // moneda de liquidacion de la operacion
				, "Tasa": Number($scope.data.kngTasaOperacion || 0)
				, "ValorFinal": Number($scope.data.kngMontoValorFinalOperacion || 0)
				, "ValorFinalMoneda": Number($scope.data.kngMontoValorFinalOperacionMoneda || 0)
				, "TasaAgente": Number($scope.data.kngTasaAgente || 0)
				, "FechaVencimiento": util.dateSPdate(fechaVen)
				, "IdCondicionLiquidacion": $scope.data.IdCondicionDeLiquidacion
				, "IdAgente": Number($scope.data.oClientePortafolio.seleccionado.IdPersonaEjecutivo || 0)
				, "IdOperador": $scope.SesionIdUsuario
				, "IdEstado": $scope.data.IdEstadoOperacion || ''// $scope.data.oConfiguracionEstado.seleccionado
				, "Glosa": $scope.data.Comentarios || ''
				, "IdTipoCanalIngreso": $scope.SesionTipoCanalIngreso
				, "TipoDocumento": $scope.data.oConfiguracionDocumentos.idActual || ""
				, "IdContacto": $scope.data.oConfiguracionContactos.idActual || 0
				, "TasaAnualizada": $scope.data.kngTasaPactoAnualizada || 0
				, "CostoFondo": $scope.data.kngCostoFondo || 0
				, "UtilidadPerdida": $scope.data.kngUtilidadPerdida || 0
				, "UtilidadFinal": $scope.data.kngUtilidadFinal || 0
				, "ValorizacionManual" :$scope.data.ValorizacionManual
			}];

			$.each($scope.data.gridApiCarteraInstrumento.dataSource.data(), function (key, value) {
				if ((value.IdReserva == null && value.INGCantidad > 0 && value.Monto > 0) || (value.IdReserva > 0 && value.Cantidad == null && value.Monto == 0)) {
					carteras.push({
						"IdInternoDetalle": key
						, "IdInternoOperacion": 1
						, "IdReserva": Number(value.IdReserva) || 0
						, "IdCliente": Number($scope.data.oClientePortafolio.seleccionado.IdCliente)
						, "IdPortafolio": Number($scope.data.oClientePortafolio.seleccionado.IdPortafolio)
						, "IdClienteCorredor": Number(parametrosVentana.IdClienteCorredor)
						, "IdPortafolioCorredor": Number(parametrosVentana.IdPortafolioCorredor)
						, "IdMesa": Number(parametrosVentana.IdMesa)
						, "IdTipoProducto": value.IdTipoProducto
						, "IdTipoOperacion": value.IdTipoOperacion
						, "IdClasificacionTipoInstrumento": value.IdClasificacionTipoInstrumento
						, "IdInstrumento": value.IdInstrumento
						, "Nemotecnico": value.Nemotecnico
						, "EsFungible": value.EsFungible
						, "IdBolsa": value.IdBolsa || "BSC"
						, "IdBoveda": value.IdBoveda
						, "FolioCartera": value.FolioCartera
						, "IdCartera": value.IdCartera
						, "CodigoIFRS": value.CodigoIFRS ? value.CodigoIFRS : parametrosVentana.CodigoIFRS
						, "IdEmisor": value.IdEmisor
						, "IdMoneda": $scope.data.IdMonedaMontoDisplay
						, "IdTipoReajuste": value.IdTipoReajuste
						, "IdMonedaEmisionInstrumento": value.IdTipoReajuste
						, "IdMonedaLiquidacionInstrumento": value.MonedaInstrumento
						, "IdMonedaLiquidacionOperacion": $scope.data.oConfiguracionMonedaLiquidacionOperacion.seleccionado.IdMonedaLiquidacion
						, "CorteMinimo": Number(value.CorteMinimo || 1)
						, "Cantidad": Number(value.INGCantidad || 0)
						, "Precio": Number(value.INGPrecioCompraActual || 0)
						, "PrecioMercado": Number(value.INGPrecioMercadoActual || 0)
						, "Tasa": Number(value.INGTasaCompra || 0)
						, "TasaMercado": Number(value.INGTasaMercadoActual || 0)
						, "Monto": Number(value.Monto || 0)
						, "MontoLiquidacion": Number(value.MontoLiquidacion || 0)
						, "MontoLiquidacionOperacion": Number(value.MontoLiquidacionOperacion || 0)
						, "ValorRescate": Number(value.INGValorRescate || 0)
						, "FechaVencimiento": util.dateSPdate(value.FechaVencimientoInstrumento || '0001-01-01')
						, "MontoValorizadoReserva": Number(value.MontoValorizadoReserva || 0)
						, "TipoCarteraCorredor": value.TipoCarteraCorredor || ''
						, "INGMontoAPlazoDetalle" : value.INGMontoAPlazoDetalle || 0
						, "INGMontoAPlazoMonedaInstrumentoDetalle" : value.INGMontoAPlazoMonedaInstrumentoDetalle || 0
					});
				}
			});

			datosParam.p_DetalleOperacion = carteras;
			
			$scope.data.formaDePagoEntregamos.forEach(function (item) {

				let entradaFormaPago = {};
				entradaFormaPago.AfectarTesoreria = item.AfectarTesoreria;
				entradaFormaPago.FechaLiquidacion = item.FechaLiquidacionComprobantePagoOperacion;
				entradaFormaPago.FechaVencimiento = item.FechaLiquidacionComprobantePagoOperacion;
				entradaFormaPago.IdComprobantePagoOperacion = item.IdComprobantePagoOperacion;
				entradaFormaPago.IdCuentaBancaria = item.IdCuentaBancaria||0;
				entradaFormaPago.IdFormaDePago = item.IdFormaDePago;
				entradaFormaPago.IdInternoOperacion = 1;
				entradaFormaPago.IdInternoPagoOperacion = item.IdInternoPagoOperacion;

				entradaFormaPago.IdMoneda = item.IdMoneda;
				entradaFormaPago.IdOperacion = item.IdOperacion;
				entradaFormaPago.IdTipoCuentaBancaria = "";
				entradaFormaPago.MontoFormaDePago = item.Monto;
				entradaFormaPago.NombreCuentaBancaria = "";
				entradaFormaPago.RecibePagaPortafolio = item.RecibePagaPortafolio;

				entradaFormaPago.NombreFormaDePago = item.NombreFormaDePago;
				entradaFormaPago.NumeroCuentaBancaria = item.CuentaBancaria;

				formaDePagoUI.push(entradaFormaPago);
			});
			$scope.data.formaDePagoRecibimos.forEach(function (item) {
				let entradaFormaPago = {};
				entradaFormaPago.AfectarTesoreria = item.AfectarTesoreria;
				entradaFormaPago.FechaLiquidacion = item.FechaLiquidacionComprobantePagoOperacion;
				entradaFormaPago.FechaVencimiento = item.FechaLiquidacionComprobantePagoOperacion;
				entradaFormaPago.IdComprobantePagoOperacion = item.IdComprobantePagoOperacion;
				entradaFormaPago.IdCuentaBancaria = item.IdCuentaBancaria||0;
				entradaFormaPago.IdFormaDePago = item.IdFormaDePago;
				entradaFormaPago.IdInternoOperacion = 1;
				entradaFormaPago.IdInternoPagoOperacion = item.IdInternoPagoOperacion;

				entradaFormaPago.IdMoneda = item.IdMoneda;
				entradaFormaPago.IdOperacion = item.IdOperacion;
				entradaFormaPago.IdTipoCuentaBancaria = "";
				entradaFormaPago.MontoFormaDePago = item.Monto;
				entradaFormaPago.NombreCuentaBancaria = "";
				entradaFormaPago.RecibePagaPortafolio = item.RecibePagaPortafolio;

				entradaFormaPago.NombreFormaDePago = item.NombreFormaDePago;
				entradaFormaPago.NumeroCuentaBancaria = item.CuentaBancaria;
				formaDePagoUI.push(entradaFormaPago);
			});
		
			datosParam.p_FormasDePago = formaDePagoUI;
			
			return datosParam;
		};

		var obtenerTasaAgente = function () {
			Push('obtenerTasaAgente');
			var filtros = new Array();

			$scope.data.kngTasaAgente = 0;
			if ($scope.data.oConfiguracionTipoOperacion.idActual && $scope.data.kngMontoOperacionMoneda && $scope.data.plazoOperacion && $scope.data.kngTasaOperacion) {
				if ($scope.data.oConfiguracionTipoOperacion.idActual === 'COMPAC' || $scope.data.oConfiguracionTipoOperacion.idActual === 'VENPAC') {
					filtros.push({ field: "TipoEntidad", operator: "eq", value: 'AGENTE' });
					filtros.push({ field: "IdMoneda", operator: "eq", value: $scope.data.oConfiguracionTipoReajusteOperacion.idActual });
					filtros.push({ field: "IdEstado", operator: "eq", value: 'ACT' });
					filtros.push({ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa });
					filtros.push({ field: "MontoHasta", operator: "gte", value: $scope.data.kngMontoOperacionMoneda });
					filtros.push({ field: "MontoDesde", operator: "lt", value: $scope.data.kngMontoOperacionMoneda });
					filtros.push({ field: "PlazoHasta", operator: "gte", value: $scope.data.plazoOperacion });
					filtros.push({ field: "PlazoDesde", operator: "lt", value: $scope.data.plazoOperacion });
					filtros.push({ field: "Borrado", operator: "eq", value: 'N' });

					dataSourceMargenes.query({ filter: filtros }).then(function () {
						var data = dataSourceMargenes.data();
						if (data.length > 0)
							$scope.data.kngTasaAgente = data[0].Tasa;

					});
				}
			}
			Pop();
		};

		var validarOperacion = function (callback) {

			var datosEnvio = {};
			if ($scope.data.oClientePortafolio.idActual && $scope.data.oConfiguracionTipoOperacion.idActual) {
				datosEnvio.p_IdEmpresa = $scope.SesionIdEmpresa;
				datosEnvio.p_IdUsuario = $scope.SesionIdUsuario;
				datosEnvio.p_IdModulo = 'MRF';
				datosEnvio.p_UsaReglasNegocio = 'S';
				datosEnvio.P_DATOS = [{
					"IdPortafolio": $scope.data.oClientePortafolio.idActual
					, "IdTipoOperacion": $scope.data.oConfiguracionTipoOperacion.idActual || ''
					, "IdMoneda": $scope.data.oConfiguracionMonedaLiquidacionOperacion.idActual || ''
					, "IdCondicionDeLiquidacion": $scope.data.IdCondicionDeLiquidacion || ''
					, "FechaOperacion": util.dateSPdate($scope.data.kngFechaOperacion)
					, "FechaLiquidacion": $scope.data.fechaLiquidacionOperacion ? util.dateSPdate($scope.data.fechaLiquidacionOperacion) : util.dateSPdate($scope.data.kngFechaOperacion)
					, "MontoOperacion": $scope.data.kngMontoOperacionMoneda
				}];
				bcsServiciosMesa.ValidarDatosOperacion(datosEnvio, function (resp) {
					if (resp.P_OUT_ERRORESCONTADOR != 0 || resp.P_OUT_ADVERTENCIASCONTADOR != 0) {
						bcsComunes.ventanaMsg({
							conflictos: util.parseServerResponse(resp)
						}, $scope.winID);
					}
					if (callback) callback(resp.P_OUT_ERRORESCONTADOR <= 0);
				}, function (resp) {
					bcsComunes.ventanaMsg({
						conflictos: util.parseServerResponse(resp)
					}, $scope.winID);
					if (callback) callback(false);
				});
			}
		};

		$scope.dataNew.agregarCartera = function (opcion, item) {

			$scope.data.limpiarFormaPago = true;
			// agrego data a la grilla
			var schemaGridCartera = _.extend({}, confGridCartera.options.dataSource.definition.schema.model.fields);
			var dataOld = [];
			var dataNew = [];
			var datos = new Object();
			var datosValorizados = new Object();

			$.each($scope.data.gridApiCarteraInstrumento.dataSource.data(), function (key, value) {
				dataOld.push(value);
			});

			// Cargo los datos nuevos o actualizados
			// -----------------------------------------------------------------------------------

			schemaGridCartera.FolioCartera = (opcion === 'update' ? item.FolioCartera : 0);
			schemaGridCartera.IdCartera = (opcion === 'update' ? item.IdCartera : 0);
			schemaGridCartera.IdTipoInstrumento = (opcion === 'update' ? item.IdTipoInstrumento : item.instrumento.IdTipoInstrumento) || '';// familia
			schemaGridCartera.IdClasificacionTipoInstrumento = (opcion === 'update' ? item.IdClasificacionTipoInstrumento : item.oConfiguracionClasificacion.seleccionado.IdClasificacionTipoInstrumento);
			schemaGridCartera.Nemotecnico = (opcion === 'update' ? item.Nemotecnico : item.instrumento.Nemotecnico);// instrumento
			schemaGridCartera.IdEmisor = (opcion === 'update' ? item.IdEmisor : item.oConfiguracionEmisor.seleccionado.IdEmisor);

			schemaGridCartera.INGCantidad = Number(item.kngCantidad);
			schemaGridCartera.INGValorRescate = Number(item.kngValorRescate);
			schemaGridCartera.INGTasaCompra = Number(item.kngTasa);
			schemaGridCartera.INGTasaMercadoActual = Number(item.kngTasa);
			schemaGridCartera.INGPrecioCompraActual = Number(item.kngPrecio);
			schemaGridCartera.INGMontoATasaCompraActual = Number(item.kngMonto);
			schemaGridCartera.Monto = Number(item.kngMonto);
			schemaGridCartera.MontoLiquidacion = Number(item.kngMontoLiquidacion);
			schemaGridCartera.MontoLiquidacionOperacion = Number(item.kngMontoLiquidacionOperacion);
			schemaGridCartera.Nominal = Number(item.kngCantidad);
			schemaGridCartera.INGPrecioMercadoActual = Number(item.kngPrecioMercado);
			schemaGridCartera.INGTasaMercadoActual = Number(item.kngTasaMercado);

			schemaGridCartera.MontoValorizadoReserva = Number(item.MontoValorizadoTipoReajuste) || 0;


			if (schemaGridCartera.IdClasificacionTipoInstrumento === 'IF') {
				schemaGridCartera.Nominal = Number(item.kngValorRescate);
			}

			schemaGridCartera.IdInstrumento = (opcion === 'update' ? item.IdInstrumento : item.instrumento.IdInstrumento);
			schemaGridCartera.IdBoveda = (opcion === 'update' ? item.IdBoveda : item.oConfiguracionBoveda.seleccionado.Codigo);
			schemaGridCartera.EsFungible = (opcion === 'update' ? item.EsFungible : (item.switchFungible ? 'S' : 'N'));
			schemaGridCartera.FechaVencimientoInstrumento = (opcion === 'update' ? item.FechaVencimientoInstrumento : item.kngFechaVencimiento);
			schemaGridCartera.INGMontoATasaMercadoProximo = Number((opcion === 'update' ? item.INGMontoATasaMercadoProximo : 0));
			schemaGridCartera.CorteMinimo = Number((opcion === 'update' ? item.CorteMinimo : item.kngCorteMinimo) || 0);
			schemaGridCartera.IdMesa = (opcion === 'update' ? item.IdMesa : $scope.data.IdMesa);
			schemaGridCartera.IdTipoProducto = $scope.data.oConfiguracionTipoOperacion.seleccionado.IdTipoProducto;
			schemaGridCartera.IdTipoOperacion = $scope.data.oConfiguracionTipoOperacion.seleccionado.IdTipoOperacion;
			schemaGridCartera.IdBolsa = (opcion === 'update' ? item.IdBolsa : (item.instrumento.IdBolsa || 'BCS'));
			schemaGridCartera.CodigoIFRS = (opcion === 'update' ? item.CodigoIFRS : item.oConfiguracionClasificacionIfrs.seleccionado.CodigoCarteraIFRS);
			schemaGridCartera.CodigoDescIFRS = (opcion === 'update' ? item.CodigoDescIFRS : item.oConfiguracionClasificacionIfrs.seleccionado.CarteraIFRS);
			schemaGridCartera.ValorClasificacionRiesgo = (opcion === 'update' ? item.ValorClasificacionRiesgo : item.instrumento.ValorClasificacionRiesgo);
			schemaGridCartera.CarteraPacto = (opcion === 'update' ? item.CarteraPacto : 'N');
			// MonedaReajuste
			schemaGridCartera.IdTipoReajuste = (opcion === 'update' ? item.IdTipoReajuste : item.oConfiguracionInstrumentoTipoReajuste.seleccionado.IdTipoReajuste);
			// MonedaEmision
			schemaGridCartera.Moneda = (opcion === 'update' ? (item.IdTipoReajuste || item.IdMonedaInstrumento) : item.oConfiguracionInstrumentoTipoReajuste.idActual);
			// MonedaSistema
			schemaGridCartera.IdMonedaSistema = (opcion === 'update' ? item.IdMonedaSistema : item.IdMonedaMontoDisplay);
			// MonedaLiquidacion item.IdMonedaLiquidacionOperacion);
			schemaGridCartera.MonedaInstrumento = (opcion === 'update' ? item.Moneda :  item.IdMonedaEmisionInstrumento );
			schemaGridCartera.MonedaLiquidacionOperacion = (opcion === 'update' ? item.MonedaLiquidacionOperacion : item.IdMonedaLiquidacionOperacion);
			schemaGridCartera.TipoCarteraCorredor = (item.TipoCarteraCorredor != null ? item.TipoCarteraCorredor : '');
			
			//montos a plazo 
			
			if ($scope.data.oConfiguracionTipoReajusteOperacion.idActual == 'CLP') {
				schemaGridCartera.INGMontoAPlazoDetalle = Number(bcsComunes.calcularMontoPlazo(Number(item.kngMonto), Number($scope.data.plazoOperacion), Number($scope.data.kngTasaOperacion)));
				schemaGridCartera.INGMontoAPlazoMonedaInstrumentoDetalle = Number(bcsComunes.calcularMontoPlazo(Number(item.kngMontoLiquidacionOperacion), Number($scope.data.plazoOperacion), Number($scope.data.kngTasaOperacion)));
			}
			else {
				schemaGridCartera.INGMontoAPlazoDetalle = Number(calcularMontoaPlazo(Number(item.kngMonto), Number($scope.data.plazoOperacion), Number($scope.data.kngTasaOperacion),360));
				schemaGridCartera.INGMontoAPlazoMonedaInstrumentoDetalle = Number(calcularMontoaPlazo(Number(item.kngMontoLiquidacionOperacion), Number($scope.data.plazoOperacion), Number($scope.data.kngTasaOperacion),360));
			}
			// Solo cuando es Nominal la 1ra vez...
			if (opcion === 'new') {
				schemaGridCartera.ClasificacionTipoInstrumento = item.oConfiguracionClasificacion.seleccionado.Nombre;
				schemaGridCartera.NombreBoveda = item.oConfiguracionBoveda.seleccionado.Nombre;
			} else {
				schemaGridCartera.ClasificacionTipoInstrumento = item.ClasificacionTipoInstrumento;
				schemaGridCartera.NombreBoveda = item.NombreBoveda;
			}

			/** ********************* */

			// Agrega el registro a la Grilla
			schemaGridCarteraNueva = dataOld;
			$scope.data.agregaPapel= true;
			dataNew.push(schemaGridCartera);
			$scope.data.gridApiCarteraInstrumento.dataSource.data(_.union(dataOld, dataNew));
			$scope.data.gridApiCarteraInstrumento.dataSource.sync();
			calcularMontoSimulacion();
			$scope.data.seleccionCarteraInstrumento = false;
			$scope.data.ValorizacionManual=$scope.data.ValorizacionManual=='S'?'S':item.ValorizacionManual
		}


		$scope.data.CargarPapeles = function (items) {
			Push('$scope.data.CargarPapeles');
			var dataNew = [];

			$.each(items, function (key, value) {
				// Limpia Objeto
				schemaGridCartera = _.extend({}, confGridCartera.options.dataSource.definition.schema.model.fields);

				schemaGridCartera.FolioCartera = value.FolioCarteraDetalle;
				schemaGridCartera.IdCartera = value.IdCarteraDetalle;
				schemaGridCartera.IdTipoInstrumento = value.IdTipoInstrumentoDetalle;
				schemaGridCartera.IdClasificacionTipoInstrumento = value.IdClasificacionTipoInstrumentoDetalle;
				schemaGridCartera.ClasificacionTipoInstrumento = value.ClasificacionTipoInstrumentoDetalle;

				schemaGridCartera.Nemotecnico = value.NemotecnicoDetalle;
				schemaGridCartera.IdEmisor = value.IdEmisorDetalle;
				schemaGridCartera.IdTipoReajuste = value.IdTipoReajusteDetalle;
				schemaGridCartera.MonedaInstrumento = value.IdMonedaLiquidacionInstrumentoDetalle;// value.IdMonedaLiquidacion;
				schemaGridCartera.MonedaLiquidacionOperacion = value.IdMonedaLiquidacionOperacionDetalle;

				schemaGridCartera.Nominal = Number(value.CantidadDetalle);
				schemaGridCartera.Cantidad = Number(value.CantidadDetalle);
				schemaGridCartera.INGCantidad = Number(value.CantidadDetalle);
				if (value.IdClasificacionTipoInstrumentoDetalle === 'IF') {
					schemaGridCartera.Nominal = Number(value.ValorRescateDetalle);
				}

				// Para los Edits del formulario de edición de instrumento
				schemaGridCartera.INGTasaMercadoActual = Number(value.TasaCompraDetalle);
				schemaGridCartera.INGTasaCompra = Number(value.TasaCompraDetalle);
				schemaGridCartera.INGPrecioMercadoActual = Number(value.PrecioMercadoDetalle);
				schemaGridCartera.INGPrecioCompraActual = Number(value.PrecioCompraDetalle);

				schemaGridCartera.Monto = Number(value.MontoCompraDetalle);
				schemaGridCartera.MontoLiquidacion = Number(value.MontoLiquidacionDetalle);
				schemaGridCartera.MontoLiquidacionOperacion = Number(value.MontoLiquidacionOperacionDetalle);

				schemaGridCartera.INGMontoATasaMercadoActual = Number(value.ValorRescateDetalle);
				schemaGridCartera.INGMontoATasaCompraActual = Number(value.ValorRescateDetalle);
				schemaGridCartera.MontoATasaMercado = Number(value.ValorRescateDetalle);
				schemaGridCartera.INGValorRescate = Number(value.ValorRescateDetalle);
				schemaGridCartera.ValorRescate = Number(value.ValorRescateDetalle);

				schemaGridCartera.IdInstrumento = value.IdInstrumentoDetalle;
				schemaGridCartera.IdBoveda = value.IdBovedaDetalle;
				schemaGridCartera.NombreBoveda = value.NombreBovedaDetalle;

				schemaGridCartera.IdReserva = value.IdReservaDetalle;
				schemaGridCartera.EsFungible = value.EsFungibleDetalle;
				schemaGridCartera.INGPrecioMercadoActual = Number(value.PrecioMercadoDetalle);
				schemaGridCartera.FechaVencimientoInstrumento = value.FechaVencimientoInstrumentoDetalle;
				schemaGridCartera.INGMontoATasaMercadoProximo = Number(value.INGMontoATasaMercadoProximo);
				schemaGridCartera.CorteMinimo = Number(value.CorteMinimoDetalle);
				schemaGridCartera.Moneda = value.IdTipoReajuste;
				schemaGridCartera.IdMonedaSistema = value.IdMonedaSistema;
				schemaGridCartera.IdMesa = value.IdMesa;
				schemaGridCartera.IdTipoProducto = value.IdTipoProducto;
				schemaGridCartera.IdTipoOperacion = value.IdTipoOperacion;
				schemaGridCartera.IdBolsa = value.IdBolsa || "BCS";
				schemaGridCartera.CodigoIFRS = value.CodigoIFRSDetalle;
				schemaGridCartera.CodigoDescIFRS = value.CodigoDescIFRSDetalle;
				schemaGridCartera.ValorClasificacionRiesgo = value.ValorClasificacionRiesgo;
				schemaGridCartera.CarteraPacto = value.CarteraPacto;
				schemaGridCartera.TipoValorizacion = value.TipoValorizacion;

				schemaGridCartera.MontoValorizadoReserva = value.MontoValorizadoReserva;
				schemaGridCartera.TipoCarteraCorredor = (value.TipoCarteraCorredor = ! null ? value.TipoCarteraCorredor : '');
				schemaGridCartera.INGMontoAPlazoDetalle = value.INGMontoAPlazoDetalle;
				schemaGridCartera.INGMontoAPlazoMonedaInstrumentoDetalle = value.INGMontoAPlazoMonedaInstrumentoDetalle;
				
				
				dataNew.push(schemaGridCartera);
			})


			if (items && items.length > 0) {
				$timeout(function () {
					$scope.data.gridApiCarteraInstrumento.dataSource.data(dataNew);
					$scope.data.gridApiCarteraInstrumento.dataSource.sync();
					calcularMontoSimulacion();
					Pop();
				}, 2000);
			}
		};

		$scope.$watchGroup(['data.kngTasaOperacion', 'data.kngTasaPactoAnualizada', 'data.oConfiguracionTipoReajusteOperacion.idActual', 'data.oConfiguracionTipoOperacion.idActual'], function (newval, oldval) {

			if ($scope.data.oConfiguracionTipoReajusteOperacion.idActual && $scope.data.oConfiguracionTipoOperacion.idActual) {
				if ($scope.data.oConfiguracionTipoReajusteOperacion.idActual != "CLP" && ($scope.data.oConfiguracionTipoOperacion.idActual == "COMPAC" || $scope.data.oConfiguracionTipoOperacion.idActual == "VENPAC")) {
					if ($scope.data.kngTasaOperacion && $scope.data.kngTasaPactoAnualizada) {
						$scope.data.kngCostoFondo = ($scope.data.oConfiguracionTipoOperacion.idActual == "COMPAC") ? $scope.data.kngTasaOperacion - $scope.data.kngTasaPactoAnualizada : $scope.data.kngTasaPactoAnualizada - $scope.data.kngTasaOperacion;
						//$scope.data.kngCostoFondo= $scope.data.kngCostoFondo<0 ?0:$scope.data.kngCostoFondo;
					}
				}
			}

		});


		$scope.$watchGroup(['data.oConfiguracionTipoReajusteOperacion.idActual', 'data.oConfiguracionTipoOperacion.idActual'], function (newval, oldval) {

			var IdMoneda = "";
			IdMoneda = $scope.data.oConfiguracionTipoReajusteOperacion.idActual;
			$scope.ocultartasaanualizada = false;
			$scope.ocultartasaanualizadainput = false;

			if (newval !== oldval && newval.length > 0) {
				$scope.data.IdDecimalTipoReajusteDisplay = (util.bcs_formato(0, 'GEN', 'Monto', $scope.data.oConfiguracionTipoReajusteOperacion.seleccionado.IdTipoReajuste ? $scope.data.oConfiguracionTipoReajusteOperacion.seleccionado.IdTipoReajuste : $scope.data.IdMonedaMontoDisplay, '', '')).cantidadDecimales;

				if (parametrosVentana.Accion !== 'MODIFICAR') {
					$scope.data.MontoMON_Org = 0;
					$scope.data.kngMontoOperacionMoneda = 0;
				}

				// Bloquea la moneda de liquidacion

				if (IdMoneda === "UF" || IdMoneda === "ICP" || IdMoneda === "IVP") {
					IdMoneda = "CLP";
				}
				$scope.data.oConfiguracionMonedaLiquidacionOperacion.idActual = IdMoneda;
				$scope.data.oConfiguracionMonedaLiquidacionOperacion.idDefecto = IdMoneda;

			}

			if (IdMoneda != "CLP" && ($scope.data.oConfiguracionTipoOperacion.idActual == "COMPAC" || $scope.data.oConfiguracionTipoOperacion.idActual == "VENPAC")) {

				if (parametrosVentana.IdOperacion == 0) {
					$scope.data.kngCostoFondo = 0;
				}

				if ($scope.data.kngTasaOperacion && $scope.data.kngTasaPactoAnualizada) {
					$scope.data.kngCostoFondo = ($scope.data.oConfiguracionTipoOperacion.idActual == "COMPAC") ? $scope.data.kngTasaOperacion - $scope.data.kngTasaPactoAnualizada : $scope.data.kngTasaPactoAnualizada - $scope.data.kngTasaOperacion;
					$scope.data.kngCostoFondo = $scope.data.kngCostoFondo < 0 ? 0 : $scope.data.kngCostoFondo;
				}

			} else if (IdMoneda != "CLP" && ($scope.data.oConfiguracionTipoOperacion.idActual == false || $scope.data.oConfiguracionTipoOperacion.idActual == false)) {
				if (parametrosVentana.IdOperacion == 0) {
					$scope.data.kngCostoFondo = 0;
				}
			} else {

				$scope.data.kngCostoFondo = IngresoOperacion.valorParametroCostoFondo;
			}
		});

		$scope.$watch('data.oConfiguracionMonedaLiquidacionOperacion.idActual', function (newval, oldval) {
			if (newval !== oldval && newval.length > 0 && $scope.data.oConfiguracionMonedaLiquidacionOperacion.seleccionado) {
				$scope.data.IdDecimalMonedaLiquidacionDisplay = (util.bcs_formato(0, 'MES', 'Monto', $scope.data.oConfiguracionMonedaLiquidacionOperacion.seleccionado.IdMonedaLiquidacion ? $scope.data.oConfiguracionMonedaLiquidacionOperacion.seleccionado.IdMonedaLiquidacion : $scope.data.IdMonedaMontoDisplay, '', '')).cantidadDecimales;
			}
		});

		$scope.$watchGroup(['data.kngMontoOperacionMoneda', 'data.plazoOperacion', 'data.kngTasaOperacion', 'data.oConfiguracionTipoReajusteOperacion.idActual'], function (newval, oldval) {
			Push('$scope.$watchGroup(data.kngMontoOperacionMoneda', 'data.plazoOperacion', 'data.kngTasaOperacion', 'data.oConfiguracionTipoReajusteOperacion.idActual)');
			if ($scope.data.kngMontoOperacionMoneda != 0 && $scope.data.kngMontoOperacionMoneda != null) {
				calcularMontoOperacionMoneda();
				if (buscaParidad) {
					calcularParidadMonto(false);
					buscaParidad = true;
				}
        
        obtenerTasaAgente();
				Pop();
			}
		});

		$scope.$watchGroup(['data.kngMontoOperacion', 'data.plazoOperacion', 'data.kngTasaOperacion'], function (newval, oldval) {
			Push('$scope.$watchGroup(data.kngMontoOperacion', 'data.plazoOperacion', 'data.kngTasaOperacion)');
			calcularMontoOperacion();
			calcularMontoSimulacion();
			Pop();
		});

		$scope.$watchGroup(['data.oConfiguracionTipoOperacion.idActual', 'data.oConfiguracionTipoReajusteOperacion.idActual'], function (newval, oldval) {
			Push('$scope.$watch(data.oConfiguracionTipoOperacion.idActual)');
			if (newval === oldval) {
				Pop();
				return;
			}

			if (($scope.data.oConfiguracionTipoOperacion.idActual == "COMPAC" || $scope.data.oConfiguracionTipoOperacion.idActual == "VENPAC") && ($scope.data.oConfiguracionTipoReajusteOperacion.idActual == "CLP" && IngresoOperacion.mostrarCamposTasaCostoFondo == "S")) {
				$scope.ocultarcamposcostofondo = false;
				$scope.ocultartasaanualizada = true;
				$scope.ocultartasaanualizadainput = true;
				$scope.data.kngTasaPactoAnualizada = '';
			} else if (($scope.data.oConfiguracionTipoOperacion.idActual == "COMPAC" || $scope.data.oConfiguracionTipoOperacion.idActual == "VENPAC") && ($scope.data.oConfiguracionTipoReajusteOperacion.idActual != "CLP" && IngresoOperacion.mostrarCamposTasaCostoFondo == "S")) {
				$scope.ocultarcamposcostofondo = false;
				$scope.ocultartasaanualizada = false;
				$scope.ocultartasaanualizadainput = false;
			} else {
				$scope.ocultarcamposcostofondo = true;
				$scope.ocultartasaanualizada = true;
				$scope.ocultartasaanualizadainput = true;
			}


			if ($scope.data.oConfiguracionTipoOperacion.idActual) {
				enableCabeceraOperacion();
				$scope.TipoOperacionSeleccionado = true;
			} else {
				$scope.TipoOperacionSeleccionado = false;
			}

			if ($scope.data.oConfiguracionTipoOperacion.idActual == "COMPAC" || $scope.data.oConfiguracionTipoOperacion.idActual == "VENPAC") {
				$scope.data.IdDecimalTasaDisplay = util.bcs_formato(0, 'GEN', 'Tasa', $scope.data.IdMonedaMontoDisplay, 'PAC', '').cantidadDecimales;
			}

			obtenerTasaAgente();
			validarOperacion();

			Pop();
		});

		$scope.$watch('data.habilitaCabecera', function (newval, oldval) {
			Push('$scope.$watch(data.habilitaCabecera)');
			if (newval !== oldval && newval > '') {
				// console.log("ahora!");
			}
			Pop();
		});


		$scope.$watch('data.oClientePortafolio.seleccionado', function (newval, oldval) {

			if (newval === oldval) {
				return;
			}

			if (($scope.data.oClientePortafolio.seleccionado || 'undefined') === 'undefined') {
				return;
			}

			if (typeof $scope.data.oClientePortafolio.seleccionado !== 'undefined') {
				$scope.ClienteSeleccionado = true;
				var tablaDatosCliente = [];
				var ObjCliente = {
					"IdCliente": $scope.data.oClientePortafolio.seleccionado.IdCliente
					, "IdAgente": $scope.data.oClientePortafolio.seleccionado.IdPersonaEjecutivo
				};
				tablaDatosCliente.push(ObjCliente);
				var parametros = {};
				parametros.p_IdEmpresa = $scope.SesionIdEmpresa;
				parametros.p_Clientes = tablaDatosCliente;


				bcsServiciosMesa.ConsultarConfiguracionCliente(parametros, function (respuesta) {
					$.each(respuesta.P_OUT_CONFIGURACION, function (key, value) {
						if (value.Codigo === "AGENTE" && value.Valor !== null) {
							$scope.data.NombreAgente = value.Descripcion === "" ? value.Valor : value.Descripcion;
						}
					});
				});


				var filter = [{ "field": "IdPortafolio", "operator": "eq", "value": $scope.data.oClientePortafolio.idActual }];
				dataSourcePreferenciaPortafolio.query({ filter: filter }).then(function () {
					if (dataSourcePreferenciaPortafolio.data().length < 1) {
						$scope.data.idFormaCustodiaPortafolio = 'NOM';   // Opera Nominal
					}
					else {
						var datos = dataSourcePreferenciaPortafolio.data();
						$.each(datos, function (key, value) {
							$scope.data.BovedaDefaultPortafolio = value.IdBoveda;
							if($scope.data.CargandoOperacion){
								$scope.data.idFormaCustodiaPortafolio = $scope.data.AfectaCustodia ? 'CUS' : 'NOM'
							}
							else{
								if (value.OperaCustodia === 'S') {
									$scope.data.idFormaCustodiaPortafolio = 'CUS';   // Opera con Custodia								
								}
								else {
									$scope.data.idFormaCustodiaPortafolio = 'NOM';   // Opera Nominal

								}
							}
						});
					}
				});
			} else {
				$scope.ClienteSeleccionado = false;
			}
		});


		$scope.$watch('data.oConfiguracionPortafolio.idActual', function (newval, oldval) {


			$scope.data.idFormaCustodiaPortafolio = 'NOM';   // Opera nomninal


			if ($scope.data.oConfiguracionPortafolio.idActual) {
				var filter = [
					{ "field": "IdPortafolio", "operator": "eq", "value": $scope.data.oConfiguracionPortafolio.idActual }
				];
				dataSourcePreferenciaPortafolio.query({ filter: filter }).then(function () {
					if (dataSourcePreferenciaPortafolio.data().length < 1) {
						$scope.data.idFormaCustodiaPortafolio = 'NOM';   // Opera nomninal
					}
					else {
						var datos = dataSourcePreferenciaPortafolio.data();
						$.each(datos, function (key, value) {
							$scope.data.BovedaDefaultPortafolio = value.IdBoveda;
							if (value.OperaCustodia === 'S') {
								$scope.data.idFormaCustodiaPortafolio = 'CUS';   // Opera con Custodia								

							}
							else {
								$scope.data.idFormaCustodiaPortafolio = 'NOM';   // Opera Nominal

							}
						});
					}
				});
				validarOperacion();
			}
		});



		$scope.$watch('data.oConfiguracionMesas.idActual', function (newval, oldval) {
			var dataSourceSocioNegocio;
			Push('$scope.$watch(data.oConfiguracionMesas.idActual)');
			if (newval === oldval) {
				Pop();
				return;
			}

			if ($scope.data.oConfiguracionMesas.idActual && ($scope.data.oConfiguracionMesas.idActual !== parametrosVentana.IdMesa || ($scope.data.IdCliente && $scope.data.IdPortafolio))) {
				// cargo solo venta para el tipo de eoperacion


				// cargo socio asociado a la mesa
				dataSourceSocioNegocio = bcsServiciosGrillasMesaRF.getDataSourceSocioDeNegocioRelacionado($scope.data.IdCliente || $scope.data.oConfiguracionMesas.seleccionado.IdCliente);
				dataSourceSocioNegocio.query().then(function (results) {	
					if (dataSourceSocioNegocio.data().length > 0) {
						var Socio = $.extend({}, dataSourceSocioNegocio.data()[0]);
						var validadorPortafolioIntermesa = false
						$scope.data.esIntermesa = ($scope.data.oConfiguracionMesas.idActual !== parametrosVentana.IdMesa);
						$scope.data.oConfiguracionSocioNegocio.idActual = Socio.IdSocioDeNegocio;
						if($scope.data.esIntermesa ===true){
							parametrosVentana.PortafoliosMesas.forEach(function(item){
								if(item.IdPortafolio !=$scope.data.oConfiguracionMesas.seleccionado.IdPortafolio && validadorPortafolioIntermesa === false){
									$scope.data.oClientePortafolio.idActual = $scope.data.IdPortafolio || item.IdPortafolio ;
									$scope.data.oClientePortafolio.idDefecto = $scope.data.IdPortafolio ||item.IdPortafolio ;
									validadorPortafolioIntermesa = true;
								}
							})
						}else{
						$scope.data.oClientePortafolio.idActual = $scope.data.IdPortafolio || $scope.data.oConfiguracionMesas.seleccionado.IdPortafolio;
						$scope.data.oClientePortafolio.idDefecto = $scope.data.IdPortafolio || $scope.data.oConfiguracionMesas.seleccionado.IdPortafolio;
						}
					}
					else {
						$scope.data.esIntermesa = false;
						if ($scope.data.oConfiguracionSocioNegocio.idActual !== false)
							$scope.data.oConfiguracionSocioNegocio.filtros.read = true;
						$scope.data.oConfiguracionTipoOperacion.filtros.Accion.value = '';
						$scope.data.oConfiguracionTipoOperacion.filtros.read = true;
						$scope.data.oConfiguracionSocioNegocio.idActual = false;
					}
					$scope.data.oConfiguracionTipoOperacion.filtros.Accion.value = $scope.data.esIntermesa ? 'VENTA' : '';
					$scope.data.oConfiguracionTipoOperacion.filtros.read = true;
				});
			}
			else {
				$scope.data.esIntermesa = false;
				if ($scope.data.oConfiguracionSocioNegocio.idActual !== false)
					$scope.data.oConfiguracionSocioNegocio.filtros.read = true;

				$scope.data.oConfiguracionTipoOperacion.filtros.read = true;
				$scope.data.oConfiguracionSocioNegocio.idActual = false;

			}
			sapoCartera;
			deshabilitaIntermesa();
			Pop();
		});



		// *
		// *
		// *
		// *
		// ---------------------------------------------------------------------------------------------------------------------
		// * MSH
		// * 1.- CUANDO CARGA LA OPERACION PARA MODIFICAR PASA 2 VECES POR ACA
		// EN EL INICIO, INICIALIZACION Y LUEGO CARGA DE DATOS
		// * 2.- CUANDO GRABA SE DEBE FIJAR EN VAR. QUE INDICA SI TODAVÍA ESTA
		// EN EL PROCESO PARA QUE NO ENVIE 2 VECES LA INFORMACIÓN Y DUPLIQUE LOS
		// PAPELES ADJUNTOS
		// *
		// ---------------------------------------------------------------------------------------------------------------------


		/***********************************************************************
		 * CAMBIOS EN LAS FORMAS DE PAGO
		 **********************************************************************/
		$scope.data.IngresarDetalleOperacion = function (newval,oldval) {
			var datos = new Object();
			if ($scope.data.agregaPapel) {
				if (newval !== undefined && oldval !== undefined) {
					if (newval !== oldval && !$scope.data.EnProcesoGrabar) {
						if ($scope.data.CargandoOperacion) {
							$scope.data.CargandoOperacion = false;
						}

						bcsComunes.ventanaOverlay($scope.winID, true);
						$scope.data.EnProcesoGrabar = true;
						$scope.actualizarMontosFormasDePago();
						
						if ($scope.data.errorGrabaDetalle)
							return;

						$timeout(function () {
							datos = setData2();
							bcsServiciosMesa.IngresarDetalleOperacionMesaRF(datos, success, ErrGrabaDetalle);
							
						}, 2000);
					}
				}
			}
		}




		$scope.$watchGroup(['data.kngCostoFondo', 'data.fechaVencimientoOperacion', 'data.kngMontoOperacion', 'data.kngTasaOperacion', 'data.kngTasaPactoAnualizada'], function (newval, oldval) {
			Push('$scope.$watchGroup(data.kngCostoFondo', 'data.fechaVencimientoOperacion', 'data.kngTasaOperacion)');
			calcularUtilidadPerdidaCostoFondo();
			Pop();
		});

		$scope.$watch('data.kngUtilidadPerdida', function (newval, oldval) {
			if (newval) {
				if ($scope.data.kngUtilidadPerdida <= 0) {
					$scope.data.kngUtilidadFinal = 0;
				} else {
					$scope.data.kngUtilidadFinal = $scope.data.kngUtilidadPerdida;
				}
			}
		})




		$scope.data.buscarCartera = function () {
			Push('$scope.data.buscarCartera');
			// Abre la ventana de la cartera al lado
			// --------------------------------------
			var info = {};
			info.Registro = $scope.seleccionMovimiento;
			$scope.data.ConfirmaMediosPago = ParametrosMesa.CONFIRMA_OPERACIONES_MESARF;
			
			if($scope.data.IdTipoOperacionCartera != $scope.data.oConfiguracionTipoOperacion.idActual){
				$scope.data.IdTipoOperacionCartera = $scope.data.oConfiguracionTipoOperacion.idActual;
			}else{
				$scope.data.IdTipoOperacionCartera = $scope.data.IdTipoOperacionCartera;
			}
			
			if ($scope.data.ConfirmaMediosPago == 'S') {
				$scope.data.habilitaCerrar = false;
			}
		
			if (  $scope.data.IdTipoOperacionCartera === 'VENTER' || $scope.data.IdTipoOperacionCartera === 'VENPAC' ) {
				$scope.data.IdPortafolioFiltro = parametrosVentana.IdPortafolioCorredor;// ir
				// con
				// el
				// portafolio
				// del
				// corredor
				// de
				// la
				// configuracion
				// de
				// la
				// mesa
				$scope.data.IdClienteFiltro = parametrosVentana.IdClienteCorredor;
				$scope.data.IdMesa = parametrosVentana.IdMesa;
				$scope.data.CarteraPacto = 'N';
				if ($scope.data.oConfiguracionTipoOperacion.idActual === 'VENPAC')
					$scope.data.CarteraPacto = 'S';
			}
			else {
				$scope.data.IdPortafolioFiltro = ($scope.data.oClientePortafolio.idActual || $scope.data.IdPortafolio);
				$scope.data.IdClienteFiltro = ($scope.data.oClientePortafolio.seleccionado.IdCliente || $scope.data.IdCliente);
				$scope.data.IdMesa = undefined;
			}

			if ( $scope.data.IdTipoOperacionCartera === 'COMTER' || $scope.data.IdTipoOperacionCartera === 'VENTER') {
				$scope.data.IdMonedaLiquidacionOperacion = $scope.data.oConfiguracionMonedaLiquidacionOperacion.seleccionado.IdMonedaLiquidacion || $scope.data.oConfiguracionMonedaLiquidacionOperacion.idActual;
			}
			$scope.data.IdMonedaLiquidacionOperacionDetalle = $scope.data.oConfiguracionMonedaLiquidacionOperacion.seleccionado.IdMonedaLiquidacion || $scope.data.oConfiguracionMonedaLiquidacionOperacion.idActual;
			$scope.data.habilitaCabecera = false;

			let montoInicialResp = $scope.data.kngMontoOperacion;
			$scope.data.kngMontoOperacion = 0;
			$scope.data.WinIngOperacion = $scope;

			$scope.ventanaCartera = bcsComunes.ventanaAbrir("CarteraMesaRF", $scope.data, false, false);

			$scope.data.kngMontoOperacion = montoInicialResp;

			Pop();
		};

		$scope.data.cancelar = function () {
			$scope.data.gridApiCarteraInstrumento.dataSource.removeAll();
			$scope.data.gridApiCarteraInstrumento.dataSource.sync();
			$scope.data.buscarCartera();
			calcularMontoSimulacion();
			Pop();
		};

		$scope.validaVentana = function (idVentana) {
			if ($("[ventanaid='" + idVentana + "']").length > 0) return true;
			return false;
		}

		$scope.habilitaBtnCartera = function () {
			if($scope.data.AfectaCustodia || (!$scope.data.AfectaCustodia && ($scope.data.oConfiguracionTipoOperacion.idActual === 'VENTER' || $scope.data.oConfiguracionTipoOperacion.idActual === 'VENPAC'))){
				return true;
			}
			else{
				return false;
			}
		}

		$scope.habilitaBtnNominal = function () {
			if(!$scope.data.AfectaCustodia || ($scope.data.AfectaCustodia && ($scope.data.oConfiguracionTipoOperacion.idActual === 'VENTER' || $scope.data.oConfiguracionTipoOperacion.idActual === 'VENPAC'))){
				return true;
			}
			else{
				return false;
			}
		}

		$scope.data.guardarOperacionMesa = function (estModifica) {

			var existeDetalle = false;
			buscaParidad = true;
			
			$scope.renovarfiltros();

			validarOperacion(function (resp) {
				if (resp) {
					if (estModifica) {
						if ($scope.WinCartera != null && $scope.WinCartera != undefined) bcsComunes.ventanaCerrar($scope.WinCartera.winID);
						var data = $scope.data.gridApiCarteraInstrumento.dataSource.data();
						$.each(data, function (key, value) {
							if (value.Monto > 0)
								existeDetalle = true;
						});
						if (existeDetalle) {

							$scope.data.limpiarFormaPago = true;
							$scope.data.habilitaCabecera = true;
							$scope.data.bloqueaCombos = false;
							$scope.data.bloqueaComboClienteOpera = true;
							deshabilitaIntermesa();

							$scope.data.IdCliente = undefined;
							$scope.data.IdPortafolio = undefined;
							$scope.data.guardarOperacion(Number($scope.data.kngSecuenciaOperacion) == 0 ? 'INICIO' : 'MODIFICAR',false);

							calcularMontoSimulacion();

						} else {
							$scope.data.habilitaCabecera = true;
							$scope.data.bloqueaCombos = true;
							deshabilitaIntermesa();
							$scope.data.IdCliente = undefined;
							$scope.data.IdPortafolio = undefined;
							$scope.data.bloqueaComboClienteOpera = false;

							$.each($scope.data.gridApiCarteraInstrumento.dataSource.data(), function (key, value) {
								if (value.Nominal > 0)
									value.Nominal = 0;
							});

							$scope.data.guardarOperacion('MODIFICAR',false);
							$scope.data.gridApiCarteraInstrumento.dataSource.removeAll();
							$scope.data.gridApiCarteraInstrumento.dataSource.sync();
							calcularMontoSimulacion();
						}
						dataSourceDetalleOperacion.removeAll();
						$scope.data.OperacionExiste = false;
					} else {

						if (($scope.data.oConfiguracionTipoOperacion.idActual === 'COMPAC' || $scope.data.oConfiguracionTipoOperacion.idActual === 'VENPAC') &&
							($scope.data.kngFechaOperacion === $scope.data.fechaVencimientoOperacion || $scope.data.plazoOperacion === 0)) {
							bcsComunes.msg("warning", $translate.instant('UI_ADVERTENCIA'), $translate.instant('PLAZO_MAYOR_CERO'));
							Pop;
							return;
						}
						var data = $scope.data.gridApiCarteraInstrumento.dataSource.data();
						if(data.length > 0 && ($scope.data.oConfiguracionTipoOperacion.idActual === 'COMPAC' || $scope.data.oConfiguracionTipoOperacion.idActual === 'VENPAC')){
							
								var mensaje = $scope.validaCarterasConFechaVencimientoInvalida(data);
								if(mensaje.P_OUT_MENSAJES.length > 0)
								{
									var id= bcsComunes.ventanaMsg({
										conflictos:util.parseServerResponse(mensaje)
									},$scope.winID);
									
									bcsComunes.ventanaPromesa(id).then(function (result) {
										$scope.data.guardarOperacion(Number($scope.data.kngSecuenciaOperacion) == 0 ? 'INICIO' : 'MODIFICAR',true);
									});
								}
								else{
									$scope.data.guardarOperacion(Number($scope.data.kngSecuenciaOperacion) == 0 ? 'INICIO' : 'MODIFICAR',true);
								}
							
							
							
						}
						else{
							$scope.data.guardarOperacion(Number($scope.data.kngSecuenciaOperacion) == 0 ? 'INICIO' : 'MODIFICAR',true);
						}
						
						
							
					}
				}
			});



		};
		$scope.validaCarterasConFechaVencimientoInvalida = function(data){
			var respuesta = {};
			var mensajes = [];
			var mensaje = {};
			carterasNominalesNoValidas = 0;
			$.each(data, function (key, value) {
				if($scope.data.fechaVencimientoOperacion > value.FechaVencimientoInstrumento){
					if(value.FolioCartera === 0){
						carterasNominalesNoValidas++;
					}
					else{
						mensaje = {};
						mensaje.codigo = "CARTERA_CON_FECHA_VENCIMIENTO_INVALIDA";
						mensaje.descripcion = "Cartera Folio: "+value.FolioCartera+" con Fecha Vencimiento inferior a la fecha pactada.";
						mensaje.tipo = "ADVERTENCIA";
						mensaje.id=value.FolioCartera;
						mensajes.push(mensaje);
					}
						
				}
			});
			if(carterasNominalesNoValidas > 0){
				mensaje = {};
				mensaje.codigo = "CARTERA_CON_FECHA_VENCIMIENTO_INVALIDA";
				mensaje.descripcion = carterasNominalesNoValidas > 1 ? "Operacion con "+carterasNominalesNoValidas+" Carteras Nominales con Fecha Vencimiento inferior a la fecha pactada." : "Operacion con "+carterasNominalesNoValidas+" Cartera Nominal con Fecha Vencimiento inferior a la fecha pactada.";
				mensaje.tipo = "ADVERTENCIA";
				mensaje.id=0;
				mensajes.push(mensaje);
			}
			respuesta.P_OUT_MENSAJES= mensajes;
			return respuesta;
		}

		$scope.mensajeIngresaDetalleOperacion = function () {
			//$scope.data.guardarOperacion(Number($scope.data.kngSecuenciaOperacion) == 0 ? 'INICIO' : 'MODIFICAR');
			var msg = bcsAlerta.crear("PREGUNTA_SI_NO", $translate.instant('UI_MENSAJE_INGRESAR_DETALLE_OPERACION'));// +' .
			msg.result.then(function (result) {
				if (result === OPCIONES_BCSALERTA.SI) {
					$scope.data.habilitaCabecera = false;
					$scope.data.buscarCartera();
					$scope.data.operacionEnModificacion=null;
				} else {
										// dijo no se cierra y cargo el blotter
					$scope.data.habilitaCabecera = true;
					$scope.data.cierraVentana = true;
					$scope.data.operacionEnModificacion=null;
					var id = bcsComunes.ventanaMsg({
						mensaje: $translate.instant("UI_OPERACION_INGRESO_EXITO") + ": " + $translate.instant("UI_SECUENCIA") + " " + $scope.data.kngSecuenciaOperacion,
					}, $scope.winID);
					bcsComunes.ventanaPromesa(id).then(function (result) {
						if ($scope.WinCartera != null) bcsComunes.ventanaCerrar($scope.WinCartera.winID);
						bcsComunes.ventanaCerrar($scope.winID);
					});
				}
			});
		}
		$scope.mensajeIngresaDetalleOperacionNominal = function () {
			//$scope.data.guardarOperacion(Number($scope.data.kngSecuenciaOperacion) == 0 ? 'INICIO' : 'MODIFICAR');
			var msg = bcsAlerta.crear("PREGUNTA_SI_NO", $translate.instant('UI_MENSAJE_INGRESAR_DETALLE_OPERACION_NOMINAL'));// +' .
			msg.result.then(function (result) {
				if (result === OPCIONES_BCSALERTA.SI) {
					$scope.data.habilitaCabecera = false;
					$scope.data.cargarNominal();
					$scope.data.operacionEnModificacion=null;
				} else {
										// dijo no se cierra y cargo el blotter
					$scope.data.habilitaCabecera = true;
					$scope.data.cierraVentana = true;
					$scope.data.operacionEnModificacion=null;
					var id = bcsComunes.ventanaMsg({
						mensaje: $translate.instant("UI_OPERACION_INGRESO_EXITO") + ": " + $translate.instant("UI_SECUENCIA") + " " + $scope.data.kngSecuenciaOperacion,
					}, $scope.winID);
					bcsComunes.ventanaPromesa(id).then(function (result) {
						if ($scope.WinCartera != null) bcsComunes.ventanaCerrar($scope.WinCartera.winID);
						bcsComunes.ventanaCerrar($scope.winID);
					});
				}
			});
		}
		 $scope.data.guardarOperacion = function (opcion,muestraAlerta) {
			if(muestraAlerta === undefined || muestraAlerta === null)
			{
				muestraAlerta = false;
			}
			Push('$scope.data.guardarOperacion');
			var datos = new Object();
			bcsComunes.ventanaOverlay($scope.winID, true);
			$scope.data.muestraNotificacion = true;
			opcion = opcion === 'INICIO' ? 'CREAR' : opcion;
			$scope.data.EnProcesoGrabar = true;

			
			$scope.data.IdTipoOperacionCartera = $scope.data.CondicionDeLiquidacion.IdTipoOperacion;
			var esVenta = $scope.data.IdTipoOperacionCartera == "VENPAC" || $scope.data.IdTipoOperacionCartera == "VENTER";

			var filtros = [{ "field": "IdCliente", "operator": "eq", "value": esVenta ? $scope.data.oConfiguracionMesas.seleccionado.IdCliente : $scope.data.oClientePortafolio.seleccionado.IdCliente }]

			dataSourceClienteCuentaPropia.query({ filter: filtros }).then(function () {
				var data = dataSourceClienteCuentaPropia.data();
				$scope.data.NombreCompleto = $scope.data.oClientePortafolio.seleccionado.NombreCompleto;
				$scope.data.codigoPortafolioCliente = $scope.data.oClientePortafolio.seleccionado.CodigoPortafolio;
				$scope.data.NombreCuentaPropia = data[0].NombreCompleto;
				$scope.data.portafolioCuentaPropiaCodigo = $scope.data.oConfiguracionMesas.seleccionado.CodigoPortafolio.charAt(0);
			});

			datos = setData(opcion);
      let FPSINDUPLI = new Set(datos.p_FormasDePago);
      datos.p_FormasDePago = [];
			FPSINDUPLI.forEach(element =>{
				datos.p_FormasDePago.push(element);
			});
			if(muestraAlerta){
				$scope.data.muestraNotificacion = false; //se valida en la funcion success()
				var promise = bcsServiciosMesa.CrearOperacionMesaRF(datos, success, ErrGrabaDetalle).$promise;
				promise.then(function () {
						Pop();
						$scope.data.ValidaOperaCustodia();
						if( (($scope.data.oConfiguracionTipoOperacion.idActual).includes("PAC") && ($scope.confBasicaFormaDePagoEntregamos.dataSource.data().length == 0 || $scope.confBasicaFormaDePagoRecibimos.dataSource.data().length == 0))
						|| ($scope.data.oConfiguracionTipoOperacion.idActual).includes("TER") && ($scope.confBasicaFormaDePagoEntregamos.dataSource.data().length == 0) ){
							var msg = bcsAlerta.crear("PREGUNTA_SI_NO", $translate.instant('UI_MENSAJE_DESEA_INGRESAR_FORMA_DE_PAGO'));
							msg.result.then(function (result) {
								if (result === OPCIONES_BCSALERTA.SI) {
									$scope.data.habilitaCabecera = false;
									$scope.data.operacionEnModificacion=null;
									$scope.data.tabFormasDePagoActive = true;
								} else {
									if($scope.data.AfectaCustodia || (!$scope.data.AfectaCustodia && $scope.data.oConfiguracionTipoOperacion.idActual.includes("VEN"))){
										$scope.mensajeIngresaDetalleOperacion();
									}
									else{
										$scope.mensajeIngresaDetalleOperacionNominal();
									}
									
								}
							});
						}
						else{
							if($scope.data.AfectaCustodia || (!$scope.data.AfectaCustodia && $scope.data.oConfiguracionTipoOperacion.idActual.includes("VEN"))){
								$scope.mensajeIngresaDetalleOperacion();
							}
							else{
								$scope.mensajeIngresaDetalleOperacionNominal();
							}
						}
						
						
						$scope.data.recarga = true;
					});
			}
			else{
				bcsServiciosMesa.CrearOperacionMesaRF(datos, success, ErrGrabaDetalle);
				Pop();
			}
			
			
			
		};
		$scope.data.ValidaOperaCustodia = function(){
			if($scope.data.formaCustodiaSeleccionada.IdFormaCustodiaOrden == 'CUS'){
				$scope.data.AfectaCustodia = true;
			}else{
				$scope.data.AfectaCustodia = false;
			}

		}

		$scope.data.enviarOperacion = function () {
			$scope.data.ConfirmaMediosPago = ParametrosMesa.CONFIRMA_OPERACIONES_MESARF;

			var datos = new Object();

			var opcion = $scope.data.ConfirmaMediosPago == 'N' && !$scope.data.modificacionFormaDePago ? 'CERRAR' : 'MODIFICAR';
			$scope.data.accionOperacion = opcion;
			var montoOperacion = 0;
			var IdEstadoConfirmacionPago = 'NCF'
			montoOperacion = $scope.data.kngMontoAsignado;// bcsComunes.calcularMontoInicial(Number($scope.data.kngMontoAsignado),Number($scope.data.plazoOperacion),Number($scope.data.kngTasaOperacion));//,$scope.data.IdDecimalMontoDisplay);
			
			let montoCeroFP = $scope.existeFormasDePagoCero();			
			
			if(!montoCeroFP){
				$scope.actualizarMontosFormasDePago();
				
				var reajusteEnMonedaExtranjera = $scope.data.oConfiguracionTipoReajusteOperacion.idActual !== 'UF'
					&& $scope.data.oConfiguracionTipoReajusteOperacion.seleccionado.IdTipoReajuste != $scope.data.IdMonedaMontoDisplay;

				var hayDescuadreMonto;
				if (reajusteEnMonedaExtranjera) {
					var decimalesMonedaOperacion = Number($scope.data.IdDecimalTipoReajusteDisplay) || 0;
					var montoOperacionRedondeado = Number(Number($scope.data.kngMontoOperacionMoneda).toFixed(decimalesMonedaOperacion));
					var montoAsignadoRedondeado = Number(Number($scope.data.kngMontoAsignado).toFixed(decimalesMonedaOperacion));
					hayDescuadreMonto = montoOperacionRedondeado !== montoAsignadoRedondeado;
				} else {
					hayDescuadreMonto = $scope.data.kngMontoOperacion !== $scope.data.kngMontoAsignado;
				}

				if (hayDescuadreMonto){	
					var msg = bcsAlerta.crear("PREGUNTA", $translate.instant('UI_MENSAJE_MONTO_OPERACION'));// +' .
					// '+$translate.instant('UI_CONTINUAR'));
					msg.result.then(function (result) {
						if (result === OPCIONES_BCSALERTA.ACEPTAR) {
							bcsComunes.ventanaOverlay($scope.winID, true);
							if ($scope.data.oConfiguracionTipoReajusteOperacion.idActual !== 'UF') {
								$scope.data.kngMontoOperacionMoneda = montoOperacion;
							}
							else {
								$scope.data.kngMontoOperacion = montoOperacion;
							}
							$scope.data.MontoCLP_Org = 0;
							calcularParidadMonto(true, function () {
								buscaParidad = false;
								calcularMontoOperacion();
								calcularMontoOperacionMoneda();
								datos = setData(opcion);
								if (datos.p_FormasDePago.length !== 0) {
									// Para que cuando inicialice los datos para
									// una nueva operación no grabe nada en
									// blanco
									// $scope.data.NroVecesGrabaPagos = 0;
									$scope.data.cierraVentana = true;
									$scope.data.cerrando = true;
									bcsServiciosMesa.CrearOperacionMesaRF(datos, success, error);
								} else {
									bcsComunes.msg("warning", $translate.instant("UI_ADVERTENCIA"), $translate.instant('FORMA_PAGO_SIN_ASIGNAR'));
									bcsComunes.ventanaOverlay($scope.winID, false);
									Pop();
									return;
								}
							});
						}
	
					});
				} else {
					bcsComunes.ventanaOverlay($scope.winID, true);
					$scope.data.kngMontoOperacion = montoOperacion;
					$scope.data.MontoMON_Org = 0;
					calcularParidadMonto(true, function () {
						buscaParidad = false;
						datos = setData(opcion);
						if (datos.p_FormasDePago.length !== 0) {
							$scope.data.cierraVentana = true;
							$scope.data.cerrando = true;
							bcsServiciosMesa.CrearOperacionMesaRF(datos, success, error);
						} else {
							bcsComunes.msg("warning", $translate.instant("UI_ADVERTENCIA"), $translate.instant('FORMA_PAGO_SIN_ASIGNAR'));
							bcsComunes.ventanaOverlay($scope.winID, false);
							Pop();
							return;
						}
	
					});
				}
			}
			
			

		};


		$scope.data.cargarNominal = function () {
			$scope.data.HayCambios = true;
			$scope.data.habilitaCerrar = true;
			$scope.data.ConfirmaMediosPago = 'N';
			var id1 = bcsComunes.ventanaAbrir(
				"IngresoNominalMesaRF",
				{
					"FechaLiquidacionOperacion": $scope.data.fechaLiquidacionOperacion,
					"FechaVencimientoOperacion": $scope.data.fechaVencimientoOperacion,
					"BovedaDefault": $scope.data.BovedaDefaultPortafolio,
					"IdMonedaLiquidacion": $scope.data.oConfiguracionMonedaLiquidacionOperacion.seleccionado.IdMonedaLiquidacion,
					"IdCondicionDeLiquidacion": $scope.data.IdCondicionDeLiquidacion,
					"IdTipoOperacion": $scope.data.oConfiguracionTipoOperacion.seleccionado.IdTipoOperacion,
					"CodigoIFRS": parametrosVentana.CodigoIFRS,
				}, false, false, $scope.winID);

			var promesa = bcsComunes.ventanaPromesa(id1);


			$.when(promesa).then(function (data) {
				if (data.boton === 'ACEPTAR') {
					$scope.data.PuedeGrabarFormasPago = true;
					$scope.dataNew.agregarCartera('new', data.data);
				}

				// bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_DETALLE")
				// + ' :'+ data);
			});
			Pop();
		};


		// Eliminar papel de la operacion
		// (Si esta en la grilla, ya tiene un nro de reserva tomado)
		$scope.data.eliminarNominal = function (nominal) {			
			var totCar = 0;
			$scope.data.HayCambios = true;

			if (nominal) {
				$scope.data.PuedeGrabarFormasPago = true;
				nominal.Nominal = Number(0);
				nominal.INGCantidad = Number(0);
				nominal.INGTasaCompra = Number(0);
				nominal.INGPrecioCompraActual = Number(0);
				nominal.INGMontoATasaCompraActual = Number(0);
				nominal.INGValorRescate = Number(0);
				nominal.Monto = Number(0);

				$scope.data.gridApiCarteraInstrumento.dataSource.sync();
				$.each($scope.data.gridApiCarteraInstrumento.dataSource.data(), function (key, value) {
					totCar = totCar + value.MontoSimulacion;
				});

				// IMPMBIOPT-520 al eliminar un papel debe actualizar monto y
				// por lo tanto, modificar las formas de pago
				var gridAux = _.extend({}, $scope.data.gridApiCarteraInstrumento.dataSource.data());
				gridAux.remove(nominal);
				var val = 0;
				var val2 = 0;
				var d = gridAux ? gridAux : [];
				$.each(d, function (key, value) {
					val += value.Monto;// || 0;
					val2 += value.MontoLiquidacionOperacion;
				});
				$scope.data.kngMontoAsignadoAnterior = $scope.data.kngMontoAsignado;
				$scope.data.kngMontoAsignado = val;
				$scope.data.kngMontoAsignadoMonedaLiquidacion = val2;
				$scope.data.IngresarDetalleOperacion($scope.data.kngMontoAsignado,$scope.data.kngMontoAsignadoAnterior);
				$scope.data.kngMontoDiferencia = Number($scope.data.kngMontoOperacion) - Number(val);
				$scope.data.kngMontoFinal = Number($scope.data.kngMontoAsignado) + Number($scope.data.kngMontoDiferencia);
				var datos = new Object();
				datos.p_IdEmpresa = $scope.SesionIdEmpresa;
				datos.p_IdUsuario = $scope.SesionIdUsuario;
				datos.p_IdModulo = 'MES';
				datos.p_IdPais = $scope.$root.SesionIdPaisSistema;
				datos.p_TblCarteraReserva = [{IdReserva: nominal.IdReserva}];
				bcsServiciosMesa.EliminaReserva(datos, function (respuesta) {
					bcsComunes.ventanaOverlay($scope.IdVentana, false);
	
					if (respuesta.P_OUT_ERRORESCONTADOR > 0) {
						var error = '';
						$.each(respuesta.P_OUT_MENSAJES, function (key, value) {
							if (value.tipo === 'ERROR') {
								error = value.descripcion;
							}
						});
						bcsComunes.msg("warning", $translate.instant("UI_ADVERTENCIA"), error);
					}else {
						$scope.data.marcarOperacionModificada();
						$scope.data.seleccionCarteraInstrumento = false;
						$scope.data.gridApiCarteraInstrumento.dataSource.remove(nominal);
						$scope.data.gridApiCarteraInstrumento.dataSource.sync();
					}
		
		
				});
				
			}
			
			$scope.actualizarMontosFormasDePago();
			$timeout(function () {
				if ($scope.WinCartera != null)
					$scope.WinCartera.data.buscarCartera();
			}, 3000);
		};
		$scope.data.cargarFormaPago = function (opcion) {
			Push('$scope.data.cargarFormaPago');
			var id1 = bcsComunes.ventanaAbrir("IngresoFormaDePago", {}, false, false, $scope.winID);
			var promesa = bcsComunes.ventanaPromesa(id1);
			$.when(promesa).then(function (data) {
				if (data.boton === 'ACEPTAR')
					$scope.dataNew.agregarFormaDePago(data.data);
			});
		};

		$scope.data.modificarOperacion = function () {
			var msg = bcsAlerta.crear("PREGUNTA", $translate.instant('UI_MENSAJE_MODIFICAR_OPERACION'));// +' .
			// '+$translate.instant('UI_CONTINUAR'));
			msg.result.then(function (result) {
				if (result === OPCIONES_BCSALERTA.ACEPTAR) {
					$scope.data.habilitaCabecera = true;
					deshabilitaIntermesa();
					$.each($scope.data.gridApiCarteraInstrumento.dataSource.data(), function (key, value) {
						if (value.Nominal > 0)
							value.Nominal = 0;
					});

					$scope.data.guardarOperacion('MODIFICAR',false);
					$scope.data.gridApiCarteraInstrumento.dataSource.removeAll();
					$scope.data.gridApiCarteraInstrumento.dataSource.sync();
				}

			});
		};


		$scope.data.CargarDetalle = function () {

			Push('$scope.data.CargarDetalle');
			var Papel = $scope.data.seleccionCarteraInstrumento;
			var id1 = bcsComunes.ventanaAbrir(
				"IngresoCarteraMesaRF",
				{
					"FechaLiquidacionOperacion": $scope.data.fechaLiquidacionOperacion,
					"FolioCartera": Papel.FolioCartera,
					"IdCartera": Papel.IdCartera,
					"IdClasificacionTipoInstrumento": Papel.IdClasificacionTipoInstrumento,
					"ClasificacionTipoInstrumento": Papel.ClasificacionTipoInstrumento,
					"IdInstrumento": Papel.IdInstrumento,
					"IdTipoInstrumento": Papel.IdTipoInstrumento,
					"Nemotecnico": Papel.Nemotecnico,
					"IdMonedaInstrumento": Papel.IdMonedaInstrumento,
					"IdEmisor": Papel.IdEmisor,
					"IdBoveda": Papel.IdBoveda,
					"NombreBoveda": Papel.NombreBoveda,
					"IdBolsa": Papel.IdBolsa,
					"CodigoIFRS": Papel.CodigoIFRS,
					"CodigoDescIFRS": Papel.CodigoDescIFRS,
					"IdTipoReajuste": Papel.IdTipoReajuste,
					"MonedaEmisionInstrumento": Papel.Moneda,
					"MonedaLiquidacionInstrumento": Papel.MonedaInstrumento,
					"MonedaLiquidacionOperacion": Papel.MonedaLiquidacionOperacion,
					"EsFungible": Papel.EsFungible,
					"CorteMinimo": Papel.CorteMinimo,
					"FechaVencimientoInstrumento": Papel.FechaVencimientoInstrumento,
					"Cantidad": Papel.INGCantidad,
					"ValorRescate": Papel.INGValorRescate,
					"INGPrecioMercadoActual": Papel.INGPrecioMercadoActual,
					"INGTasaMercadoActual": Papel.INGTasaMercadoActual,
					"INGMontoATasaMercadoActual": Papel.Monto,
					"INGPrecioCompra": Papel.INGPrecioCompraActual,
					"INGTasaCompra": Papel.INGTasaCompra,
					"INGMontoATasaCompraActual": Papel.Monto,
					"TipoValorizacion": Papel.TipoValorizacion,
					"MontoLiquidacion": Papel.MontoLiquidacion,
					"MontoLiquidacionOperacion": Papel.MontoLiquidacionOperacion,
					"IdMonedaLiquidacion": $scope.data.oConfiguracionMonedaLiquidacionOperacion.seleccionado.IdMonedaLiquidacion,
				},
				false, false, $scope.winID);

			var promesa = bcsComunes.ventanaPromesa(id1);
			$.when(promesa).then(function (dataCartera) {
				if (dataCartera.boton === 'ACEPTAR') {

					// Actualizo los datos de la grilla antes de enviar a
					// grabar...
					$scope.data.seleccionCarteraInstrumento.INGCantidad = dataCartera.data.kngCantidad;
					$scope.data.seleccionCarteraInstrumento.INGValorRescate = dataCartera.data.kngValorRescate;
					$scope.data.seleccionCarteraInstrumento.INGPrecioCompraActual = dataCartera.data.kngPrecio;
					$scope.data.seleccionCarteraInstrumento.INGTasaCompra = dataCartera.data.kngTasa;
					$scope.data.seleccionCarteraInstrumento.INGMontoATasaCompraActual = dataCartera.data.kngMonto;
					$scope.data.seleccionCarteraInstrumento.Monto = dataCartera.data.kngMonto;

					$scope.data.seleccionCarteraInstrumento.INGPrecioMercadoActual = Number(dataCartera.data.kngPrecioMercado);
					$scope.data.seleccionCarteraInstrumento.INGTasaMercadoActual = Number(dataCartera.data.kngTasaMercado);
					$scope.data.seleccionCarteraInstrumento.MontoLiquidacion = Number(dataCartera.data.kngMontoLiquidacionOperacion);
					$scope.data.seleccionCarteraInstrumento.MontoLiquidacionOperacion = Number(dataCartera.data.kngMontoLiquidacionOperacion);

					$scope.data.seleccionCarteraInstrumento.MontoValorizadoReserva = Number(dataCartera.data.MontoValorizadoTipoReajuste) || 0;


					$scope.data.seleccionCarteraInstrumento.Nominal = dataCartera.data.kngCantidad;
					if (dataCartera.data.IdClasificacionTipoInstrumento === 'IF') {
						$scope.data.seleccionCarteraInstrumento.Nominal = Number(dataCartera.data.kngValorRescate);
					}

					$scope.data.gridApiCarteraInstrumento.dataSource.sync();

					// Ahora a grabar la información
					calcularMontoSimulacion();
					$scope.data.guardarOperacion('MODIFICAR',false);
					$scope.data.seleccionCarteraInstrumento = false;
				}
				bcsComunes.ventanaCerrar($scope.id1);
				$timeout(function () {
					if($scope.WinCartera != null)
						$scope.WinCartera.data.buscarCartera();
				}, 3000);
			});

			Pop();
		};


		// -------------------------------------------
		// AJUSTE SENCILLO
		// -------------------------------------------

		var ValorizarAjuste = function () {

			var datos = new Object();
			Papel = $scope.data.gridApiCarteraInstrumento.dataSource.data()[0];

			// Precio y/o Tasa son las variables a calcular en la valorizacion
			datos.p_IdEmpresa = $scope.SesionIdEmpresa;
			datos.p_IdUsuario = $scope.SesionIdUsuario;
			datos.p_IdModulo = 'MES';
			datos.p_IdPais = $scope.$root.SesionIdPaisSistema;
			datos.p_OrigenProcesamiento = 'MESA_UI'
			datos.p_IdMonedaAdicional = $scope.data.oConfiguracionMonedaLiquidacionOperacion.seleccionado.IdMonedaLiquidacion;
			datos.p_TicketProcesamiento = 5604,


				datos.p_Instrumentos = [{
					IdValorizacion: 1,
					FechaOperacion: util.dateSPdate($scope.data.kngFechaOperacion, "FechaSistema", "CALL"),
					FechaValorizacion: util.dateSPdate($scope.data.FechaLiquidacionOperacion || $scope.data.kngFechaOperacion, "FechaSistema", "CALL"),// $scope.SesionFechaSistema
					IdClasificacionTipoInstrumento: Papel.IdClasificacionTipoInstrumento,
					IdTipoInstrumento: Papel.IdTipoInstrumento,
					IdInstrumento: Papel.IdInstrumento,
					FechaVencimiento: util.dateSPdate(Papel.FechaVencimientoInstrumento, "FechaSistema", "CALL"),
					FechaEmision: util.dateSPdate(Papel.FechaEmision || $scope.data.kngFechaOperacion, "FechaSistema", "CALL"),
					IdMonedaInstrumento: Papel.MonedaInstrumento,
					IdTipoReajusteInstrumento: Papel.IdTipoReajuste,
					Cantidad: Papel.Cantidad || Papel.INGCantidad,
					ValorRescate: Papel.ValorRescate || Papel.INGValorRescate,
					PrecioCompra: null,
					TasaCompra: null,
					MontoCompra: $scope.data.MontoAjustado,
					Emisor: Papel.IdEmisor,
					PrecioSubyacente: null,
					TasaSubyacente: null,
					PrecioGarantia: null,
					TasaGarantia: null,
					TasaTIF: null,
					TieneCorteDeCupon: null,
					IdCliente: null,
					IdPortafolio: null,
					TablaOrigen: null,
					IdUso: null,
					TipoCartera: null,
					IdTipoCriterioTasa: null,
					Nemotecnico: Papel.Nemotecnico,
					IdCondicionDeLiquidacion: $scope.data.IdCondicionDeLiquidacion,
					IdMonedaEmisionInstrumento: Papel.IdTipoReajuste,
				}];



			bcsServicioPlataforma.ValorizarInstrumento(datos, function (respuesta) {
				bcsComunes.ventanaOverlay($scope.IdVentana, false);

				if (respuesta.P_OUT_ERRORESCONTADOR > 0) {
					var error = '';
					$.each(respuesta.P_OUT_MENSAJES, function (key, value) {
						if (value.tipo === 'ERROR') {
							error = value.descripcion;
						}
					});
					bcsComunes.msg("warning", 'Valorizador', $translate.instant('UI_ERROR_RUTINA_VALORIZACION') + ': ' + error);
				}
				else if (respuesta.P_OUT_INSTRUMENTOSVALORIZADOS.length == 0) {
					console.error('Sin datos valorizados');
					var p = bcsComunes.ventanaMsgErr($translate.instant('UI_SIN_DATOS_VALORIZADOS'), idVentana);
					bcsComunes.ventanaPromesa(p).then(function () {
						console.timeEnd(vtime);
						console.groupEnd(vgroup);
					});
				} else {
					$.each(respuesta.P_OUT_INSTRUMENTOSVALORIZADOS, function (key, value) {
						if (value.DimensionTasa === 'COMP' && value.DimensionTiempo === 'AC' && value.IdValorizacion === 1) {	// A
							// Tasa
							// Compra
							console.info('Valorizacion sin Error');

							PapelAjustado = $scope.data.gridApiCarteraInstrumento.dataSource.data()[0];

							$scope.data.gridApiCarteraInstrumento.dataSource.data()[0].INGPrecioCompraActual = Number(value.Precio || 0);
							$scope.data.gridApiCarteraInstrumento.dataSource.data()[0].PrecioCompra = Number(value.Precio || 0);
							$scope.data.gridApiCarteraInstrumento.dataSource.data()[0].TasaCompra = Number(value.Tasa || 0);
							$scope.data.gridApiCarteraInstrumento.dataSource.data()[0].INGTasaCompra = Number(value.Tasa || 0);

							$scope.data.gridApiCarteraInstrumento.dataSource.data()[0].INGMontoATasaCompraActual = value.MontoMonedaSistema;
							$scope.data.gridApiCarteraInstrumento.dataSource.data()[0].Monto = value.MontoMonedaSistema;
							$scope.data.gridApiCarteraInstrumento.dataSource.data()[0].MontoLiquidacion = Number(value.MontoMonedaInstrumento || 0);
							$scope.data.gridApiCarteraInstrumento.dataSource.data()[0].MontoLiquidacionOperacion = Number(value.MontoMonedaAdicional || 0);

							$scope.data.gridApiCarteraInstrumento.dataSource.sync();

							// Ahora a grabar la información
							calcularMontoSimulacion();
						}
					});
				}


			}, function (respuesta) {
				// $scope.dataNominal.kngValorRescate = 0;
				bcsComunes.ventanaOverlay($scope.IdVentana, false);
			});
		};


		var ParidadAjuste = function () {
			var decimales = 1;
			var data = Object();
			var Diferencia = $scope.data.kngMontoDiferencia;
			if (Diferencia < 0) {
				Diferencia = Diferencia * -1;
			}


			Papel = $scope.data.gridApiCarteraInstrumento.dataSource.data()[0];
			// Horrrorsss
			dec_x_1 = 1;
			if (Papel.CorteMinimo === 0.1) {
				dec_x_1 = 10;
			} else if (Papel.CorteMinimo === 0.01) {
				dec_x_1 = 100;
			} else if (Papel.CorteMinimo === 0.001) {
				dec_x_1 = 1000;
			} else if (Papel.CorteMinimo === 0.0001) {
				dec_x_1 = 10000;
			}

			data.p_IdEmpresa = $scope.SesionIdEmpresa;
			data.p_CalculosEntrada = [{
				"IdTrabajo": 2147
				, "IdInterno": 1
				, "Fecha": $scope.SesionFechaSistema
				, "IdMonedaOrigen": "CLP"
				, "IdMonedaDestino": Papel.IdTipoReajuste !== "CLP" ?
					Papel.IdTipoReajuste :
					$scope.data.oConfiguracionMonedaLiquidacionOperacion.seleccionado.IdMonedaLiquidacion
				, "ValorCambioLocalModificado": 0
				, "MontoOrigen": Diferencia
				, "IdUso": 'MESA'
			}];

			bcsServiciosMesa.ObtenerParidadTipoCambio(data).$promise.then(function (respuesta) {
				if (respuesta.p_out_CalculosSalida.length > 0) {

					// Ahora envia a valorizar...
					$scope.data.MontoAjustado = respuesta.p_out_CalculosSalida[0].MontoDestino + Papel.MontoValorizadoReserva;
					ValorizarAjuste();
				}
			});
		};


		$scope.data.AjusteSencillo = function () {

			var Diferencia = Math.round($scope.data.kngMontoDiferencia * 1) / 1;
			if (Diferencia < 0) {
				Diferencia = Diferencia * -1;
			}


			if (Number($scope.data.kngMontoValorFinalOperacion) === 0) {
				bcsComunes.msg("error", "error", $translate.instant('UI_MRF_AJUSTE_FALTA_MONTO_OPERACION'));
				return;
			}

			if ($scope.data.gridApiCarteraInstrumento.dataSource.total() === 0) {
				bcsComunes.msg("error", "error", $translate.instant('UI_MRF_AJUSTE_SIN_PAPELES'));
				return;
			}

			if (Diferencia === 0) {
				bcsComunes.msg("error", "error", $translate.instant('UI_MRF_AJUSTE_SIN_DIFERENCIA'));
				return;
			}

			if (Diferencia > $scope.data.MontoMaximoParaAjuste) {
				bcsComunes.msg("error", "error", $translate.instant('UI_MRF_AJUSTE_DIFERENCIA_MAYOR'));
				return;
			}

			$scope.data.MontoAjustado = 0;
			Papel = $scope.data.gridApiCarteraInstrumento.dataSource.data()[0];
			if (("CLP" !== Papel.IdTipoReajuste && "UF" !== Papel.IdTipoReajuste) && Papel.IdClasificacionTipoInstrumento === "IF") {
				// Viene desde calculo de paridades

				ParidadAjuste();
			} else {
				$scope.data.MontoAjustado = Papel.Monto + (Math.round($scope.data.kngMontoDiferencia * 1) / 1);
				ValorizarAjuste();
			}
		};

		calcularMontoaPlazo = function (monto, plazo, tasa, base) {
			return Number(monto * (1 + (tasa / 100) * (plazo / base)));
		};

		$scope.data.limpiarFiltros = function () {
			Push('$scope.data.limpiarFiltros');
			reset();
			Pop();
		};

		var Push = function (xProc) {
			ident = ident + '   ';
		}

		var Pop = function () {
			sapoCartera();
			ident = ident.substring(0, ident.length - 3);
		}

		var sapoCartera = function () {
			if ($scope.data.gridApiCarteraInstrumento.dataSource.ds._data.length > 0) {
				// console.log(ident + 'NEMO : ' +
				// $scope.data.gridApiCarteraInstrumento.dataSource.ds._data[0].Nemotecnico
				// || '' );
			}
		}
		calcularUtilidadPerdidaCostoFondo = function () {
			var FechaDesde = kendo.date.firstDayOfMonth(new Date($scope.SesionFechaSistema));

			var p_FechaFinal = moment($scope.SesionFechaSistema).endOf('day');
			var FechaHasta = kendo.date.lastDayOfMonth(new Date(p_FechaFinal));

			var FechaInicio = $scope.data.kngFechaOperacion;
			var FechaLiquidacion = $scope.data.fechaVencimientoOperacion;
			var IdMoneda = $scope.data.oConfiguracionTipoReajusteOperacion.idActual;
			if ($scope.data.oConfiguracionTipoOperacion.idActual == "COMPAC") { //compra pacto
				if (IdMoneda === "CLP") {
					if (FechaLiquidacion < FechaDesde || FechaLiquidacion == undefined) {
						$scope.data.kngUtilidadPerdida = 0;
						$scope.data.kngUtilidadFinal = 0;
					} else {
						if (FechaInicio >= FechaDesde) {
							if (FechaLiquidacion <= FechaHasta) {
								$scope.data.kngUtilidadPerdida = $scope.data.kngMontoOperacion * util.datediff(FechaLiquidacion, FechaInicio).days() * ($scope.data.kngTasaOperacion - $scope.data.kngCostoFondo) / 3000
							} else {
								$scope.data.kngUtilidadPerdida = $scope.data.kngMontoOperacion * util.datediff(FechaHasta, FechaInicio).days() * ($scope.data.kngTasaOperacion - $scope.data.kngCostoFondo) / 3000
							}
						} else {
							if (FechaLiquidacion > FechaHasta) {
								$scope.data.kngUtilidadPerdida = $scope.data.kngMontoOperacion * (util.datediff(FechaHasta, FechaDesde).days() + 1) * ($scope.data.kngTasaOperacion - $scope.data.kngCostoFondo) / 3000
							} else {
								$scope.data.kngUtilidadPerdida = $scope.data.kngMontoOperacion * (util.datediff(FechaLiquidacion, FechaDesde).days() + 1) * ($scope.data.kngTasaOperacion - $scope.data.kngCostoFondo) / 3000
							}
						}
					}
				} else {
					if (FechaLiquidacion < FechaDesde || FechaLiquidacion == undefined) {
						$scope.data.kngUtilidadPerdida = 0;
						$scope.data.kngUtilidadFinal = 0;
					} else {
						if (FechaInicio >= FechaDesde) {
							if (FechaLiquidacion <= FechaHasta) {
								$scope.data.kngUtilidadPerdida = $scope.data.kngMontoOperacion * (util.datediff(FechaLiquidacion, FechaInicio).days()) * ($scope.data.kngTasaPactoAnualizada - $scope.data.kngCostoFondo) / 36000
							} else {
								$scope.data.kngUtilidadPerdida = $scope.data.kngMontoOperacion * util.datediff(FechaHasta, FechaInicio).days() * ($scope.data.kngTasaPactoAnualizada - $scope.data.kngCostoFondo) / 36000
							}
						} else {
							if (FechaLiquidacion > FechaHasta) {
								$scope.data.kngUtilidadPerdida = $scope.data.kngMontoOperacion * (util.datediff(FechaHasta, FechaDesde).days() + 1) * ($scope.data.kngTasaPactoAnualizada - $scope.data.kngCostoFondo) / 36000
							} else {
								$scope.data.kngUtilidadPerdida = $scope.data.kngMontoOperacion * (util.datediff(FechaLiquidacion - FechaDesde).days() + 1) * ($scope.data.kngTasaPactoAnualizada - $scope.data.kngCostoFondo) / 36000
							}
						}
					}
				}
			}
			if ($scope.data.oConfiguracionTipoOperacion.idActual == "VENPAC") {//venta pacto
				if (IdMoneda === "CLP") {
					if (FechaLiquidacion < FechaDesde || FechaLiquidacion == undefined) {
						$scope.data.kngUtilidadPerdida = 0;
						$scope.data.kngUtilidadFinal = 0;
					} else {
						if (FechaInicio >= FechaDesde) {
							if (FechaLiquidacion <= FechaHasta) {
								$scope.data.kngUtilidadPerdida = $scope.data.kngMontoOperacion * util.datediff(FechaLiquidacion, FechaInicio).days() * ($scope.data.kngCostoFondo - $scope.data.kngTasaOperacion) / 3000
							} else {
								$scope.data.kngUtilidadPerdida = $scope.data.kngMontoOperacion * util.datediff(FechaHasta, FechaInicio).days() * ($scope.data.kngCostoFondo - $scope.data.kngTasaOperacion) / 3000
							}
						} else {
							if (FechaLiquidacion > FechaHasta) {
								$scope.data.kngUtilidadPerdida = $scope.data.kngMontoOperacion * (util.datediff(FechaHasta, FechaDesde).days() + 1) * ($scope.data.kngCostoFondo - $scope.data.kngTasaOperacion) / 3000
							} else {
								$scope.data.kngUtilidadPerdida = $scope.data.kngMontoOperacion * (util.datediff(FechaLiquidacion, FechaDesde).days() + 1) * ($scope.data.kngCostoFondo - $scope.data.kngTasaOperacion) / 3000
							}
						}
					}
				} else {
					if (FechaLiquidacion < FechaDesde || FechaLiquidacion == undefined) {
						$scope.data.kngUtilidadPerdida = 0;
						$scope.data.kngUtilidadFinal = 0;
					} else {
						if (FechaInicio >= FechaDesde) {
							if (FechaLiquidacion <= FechaHasta) {
								$scope.data.kngUtilidadPerdida = $scope.data.kngMontoOperacion * (util.datediff(FechaLiquidacion, FechaInicio).days()) * ($scope.data.kngCostoFondo - $scope.data.kngTasaPactoAnualizada) / 36000
							} else {
								$scope.data.kngUtilidadPerdida = $scope.data.kngMontoOperacion * util.datediff(FechaHasta, FechaInicio).days() * ($scope.data.kngCostoFondo - $scope.data.kngTasaPactoAnualizada) / 36000
							}
						} else {
							if (FechaLiquidacion > FechaHasta) {
								$scope.data.kngUtilidadPerdida = $scope.data.kngMontoOperacion * (util.datediff(FechaHasta, FechaDesde).days() + 1) * ($scope.data.kngCostoFondo - $scope.data.kngTasaPactoAnualizada) / 36000
							} else {
								$scope.data.kngUtilidadPerdida = $scope.data.kngMontoOperacion * (util.datediff(FechaLiquidacion - FechaDesde).days() + 1) * ($scope.data.kngCostoFondo - $scope.data.kngTasaPactoAnualizada) / 36000
							}
						}
					}
				}
			}
		}


		reset();
		cargarOperacion();

		/*******************************************************************
			 * CONTROL FORMAS DE PAGO
			 ******************************************************************/

		/*******************************************************************
			 * DEFINICION DE VARIABLES
			 ******************************************************************/
		$scope.numeroIdInternoOperacion = 0;
		$scope.formaDePago = new Map();
		$scope.formaDePagoArray = [];
		$scope.formaDePagoPorDefecto = {}
		$scope.formaDePagoPorDefectoEnt = {}
		$scope.formaDePagoPorDefectoRec = {}
		$scope.formaDePagoArrayEntregamos = [];
		$scope.formaDePagoArrayRecibimos = [];
		$scope.cantidadFormasDePago;
		$scope.activaFPClienteEnt = true;
		$scope.activaFPCorredoraEnt = false;
		$scope.activaFPClienteRec = true;
		$scope.activaFPCorredoraRec = false;
		$scope.maxMediosPagoRec = false;
		$scope.maxMediosPagoEnt = false;
		$scope.noPermiteCuentaInversion = false;
		$scope.corredoraPermiteCuentaInversion = false;
		$scope.datosModificados = false;
		$scope.datos = {};

		consultaFormasdePagoCliEnt = bcsServiciosGrillasMesaRF.getDataSourceFormasPagoPortaCliente();
		var consultaFormasdePagoCliRec = bcsServiciosGrillasMesaRF.getDataSourceFormasPagoPortaCliente();

		var consultaFormasdePagoCorCliEnt = bcsServiciosGrillasMesaRF.getDataSourceFormasPagoPortaCliente();
		var consultaFormasdePagoCorCliRec = bcsServiciosGrillasMesaRF.getDataSourceFormasPagoPortaCliente();

		var consultaFormasdePagoCorEnt = bcsServiciosGrillasMesaRF.getDataSourceFormasPagoMesa();
		var consultaFormasdePagoCorRec = bcsServiciosGrillasMesaRF.getDataSourceFormasPagoMesa();

		var consultaPreferenciaPortafolio = bcsServiciosGrillasMesaRF.getDataSourcePreferenciaPortafolio();
		var consultarComprobantePagoOperacion = bcsServiciosGrillasMesaRF.getDataFormaPagoGuardadasOperacionMesaRF();


		/*******************************************************************
		 * CONFIGURACION COMBOS
		 ******************************************************************/
		$scope.tipoOperacionCliente = function(){
			if($scope.data.oConfiguracionTipoOperacion.idActual != null){
				switch($scope.data.oConfiguracionTipoOperacion.idActual){
					case "COMPAC": return "VENPAC";
					case "VENPAC": return "COMPAC";
					case "COMTER": return "VENTER";
					case "VENTER": return "COMTER";					 
				}
			}
		};
	
		$scope.renovarfiltros = function (){
			
			let tipoOperacionCliente = $scope.tipoOperacionCliente ();
			
			let filtrosEntregamosCompra=new Array();
			let filtrosRecibimosVenta  =new Array();
			let filtrosRecibimosCompra=new Array();
			let filtrosEntregamosVenta=new Array();
			
			filtrosEntregamosCompra.push({ field: "IdPortafolio",   operator: "eq", value: $scope.data.oClientePortafolio.seleccionado.IdPortafolio });
			filtrosEntregamosCompra.push({ field: "IdTipoOperacion", operator: "eq", value : ($scope.data.oConfiguracionTipoOperacion.idActual).includes("PAC")?"VENPAC":"VENTER" || "" });			
			filtrosEntregamosCompra.push({ field: "IdMoneda", operator: "eq", value :$scope.data.oConfiguracionMonedaLiquidacionOperacion.seleccionado.IdMonedaLiquidacion || "CLP" });
			filtrosEntregamosCompra.push({ field: "IdEstado", operator: "eq", value :"ACT" });
			filtrosEntregamosCompra.push({ field: "IdTipoProducto", operator: "eq", value: "MERF" });
						
			$scope.confPreferenciaPagoEntregamosCompra.filtros=filtrosEntregamosCompra;
			
			filtrosRecibimosVenta.push({ field: "IdPortafolio", operator:   "eq", value: $scope.data.oClientePortafolio.seleccionado.IdPortafolio });
			filtrosRecibimosVenta.push({ field: "IdTipoOperacion", operator: "eq", value:   ($scope.data.oConfiguracionTipoOperacion.idActual).includes("PAC")?"VENPAC":"VENTER" || "" });
			filtrosRecibimosVenta.push({ field: "IdMoneda", operator: "eq", value :$scope.data.oConfiguracionMonedaLiquidacionOperacion.seleccionado.IdMonedaLiquidacion || "CLP" });
			filtrosRecibimosVenta.push({ field: "IdEstado", operator: "eq", value :"ACT" });
			filtrosRecibimosVenta.push({ field: "IdTipoProducto", operator: "eq", value: "MERF" });
			
			$scope.confPreferenciaPagoRecibimosVenta.filtros=filtrosRecibimosVenta;
			
			filtrosRecibimosCompra.push({ field: "IdPortafolio", operator: "eq", value:  $scope.data.IdPortafolioCorredora });
			filtrosRecibimosCompra.push({ field: "IdTipoOperacion", operator: "eq",value:   ($scope.data.oConfiguracionTipoOperacion.idActual).includes("PAC")?"VENPAC":"VENTER" || "" });
			filtrosRecibimosCompra.push({ field: "IdMoneda", operator: "eq", value :$scope.data.oConfiguracionMonedaLiquidacionOperacion.seleccionado.IdMonedaLiquidacion || "CLP" });
			filtrosRecibimosCompra.push({ field: "IdEstado", operator: "eq", value :"ACT" });
			filtrosRecibimosCompra.push({ field: "IdTipoProducto", operator: "eq", value: "MERF" });
						
			$scope.confPreferenciaPagoRecibimosCompra.filtros =filtrosRecibimosCompra;
			
			filtrosEntregamosVenta.push({ field: "IdPortafolio", operator: "eq", value:  $scope.data.IdPortafolioCorredora });
			filtrosEntregamosVenta.push({ field: "IdTipoOperacion", operator: "eq",value:   ($scope.data.oConfiguracionTipoOperacion.idActual).includes("PAC")?"VENPAC":"VENTER" || "" });
			filtrosEntregamosVenta.push({ field: "IdMoneda", operator: "eq", value :$scope.data.oConfiguracionMonedaLiquidacionOperacion.seleccionado.IdMonedaLiquidacion || "CLP" });
			filtrosEntregamosVenta.push({ field: "IdEstado", operator: "eq", value :"ACT" });
			filtrosEntregamosVenta.push({ field: "IdTipoProducto", operator: "eq", value: "MERF" });
			
			$scope.confPreferenciaPagoEntregamosVenta.filtros=filtrosEntregamosVenta;
						
		};
		
			$scope.renovarFiltrosModificacion = function (data){
			
			let tipoOperacionCliente = $scope.tipoOperacionCliente ();
			
			let filtrosEntregamosCompra=new Array();
			let filtrosRecibimosVenta  =new Array();
			let filtrosRecibimosCompra=new Array();
			let filtrosEntregamosVenta=new Array();
		
      let OperacionFiltro="";
      switch (data.IdTipoOperacion) {
  case 'VENTER':
    OperacionFiltro = 'VENTER';
    break;
  case 'COMTER':
     OperacionFiltro = 'VENTER';
    break;
  case 'COMPAC':
     OperacionFiltro = 'VENPAC';
    break;
  case 'VENPAC':
     OperacionFiltro = 'VENPAC';
    break;
  default:
     OperacionFiltro = '';
}
		filtrosEntregamosCompra.push({ field: "IdPortafolio",   operator: "eq", value: data.IdPortafolio});
		filtrosEntregamosCompra.push({ field: "IdTipoOperacion", operator: "eq", value : OperacionFiltro});			
		filtrosEntregamosCompra.push({ field: "IdMoneda", operator: "eq", value : data.IdMonedaLiquidacion });
		filtrosEntregamosCompra.push({ field: "IdEstado", operator: "eq", value :"ACT" });
		filtrosEntregamosCompra.push({ field: "IdTipoProducto", operator: "eq", value: "MERF" });
							  
          	
			$scope.confPreferenciaPagoEntregamosCompra.filtros=filtrosEntregamosCompra;
			
      
    filtrosRecibimosVenta.push({ field: "IdPortafolio",   operator: "eq", value: data.IdPortafolio});
		filtrosRecibimosVenta.push({ field: "IdTipoOperacion", operator: "eq", value : OperacionFiltro});			
		filtrosRecibimosVenta.push({ field: "IdMoneda", operator: "eq", value : data.IdMonedaLiquidacion });
		filtrosRecibimosVenta.push({ field: "IdEstado", operator: "eq", value :"ACT" });
		filtrosRecibimosVenta.push({ field: "IdTipoProducto", operator: "eq", value: "MERF" });
      
      
			$scope.confPreferenciaPagoRecibimosVenta.filtros=filtrosRecibimosVenta;
			
      
      filtrosRecibimosCompra.push({ field: "IdPortafolio",   operator: "eq", value:  $scope.data.IdPortafolioCorredora});
  		filtrosRecibimosCompra.push({ field: "IdTipoOperacion", operator: "eq", value :OperacionFiltro});			
  		filtrosRecibimosCompra.push({ field: "IdMoneda", operator: "eq", value : data.IdMonedaLiquidacion });
  		filtrosRecibimosCompra.push({ field: "IdEstado", operator: "eq", value :"ACT" });
  		filtrosRecibimosCompra.push({ field: "IdTipoProducto", operator: "eq", value: "MERF" });
						
			$scope.confPreferenciaPagoRecibimosCompra.filtros =filtrosRecibimosCompra;
			
      
      filtrosEntregamosVenta.push({ field: "IdPortafolio",   operator: "eq", value:  $scope.data.IdPortafolioCorredora});
  		filtrosEntregamosVenta.push({ field: "IdTipoOperacion", operator: "eq", value : OperacionFiltro});			
  		filtrosEntregamosVenta.push({ field: "IdMoneda", operator: "eq", value : data.IdMonedaLiquidacion });
  		filtrosEntregamosVenta.push({ field: "IdEstado", operator: "eq", value :"ACT" });
  		filtrosEntregamosVenta.push({ field: "IdTipoProducto", operator: "eq", value: "MERF" });
						
      
			$scope.confPreferenciaPagoEntregamosVenta.filtros=filtrosEntregamosVenta;
						
		};
		
		//Inicial compra
		$scope.confPreferenciaPagoEntregamosCompra = {
			tipo: "combobox-forma-pago-cliente",
			configuracion: {
				placeholder: $translate.instant("UI_FORMA_DE_PAGO"),
				template: '<span >#: Descripcion # </span>',

			},
			idActual: false,
			idDefecto: false,
			seleccionado: false,
			itemPostFiltro: false,
			filtros: {
				IdPortafolio: { "field": "IdPortafolio", "operator": "eq", "value": -1 },
				IdMoneda: { "field": "IdMoneda", "operator": "eq", "value": $scope.data.oConfiguracionMonedaLiquidacionOperacion.seleccionado.IdMonedaLiquidacion || "CLP" },				
				IdEstado: { "field": "IdEstado", "operator": "eq", "value": 'ACT' },
				IdTipoProducto: { "field": "IdTipoProducto", "operator": "eq", "value": 'MERF' }				
			},
		};
		
		//Final Compra
		$scope.confPreferenciaPagoRecibimosCompra = {
			tipo: "combobox-forma-pago-corredora",
			configuracion: {
				placeholder: $translate.instant("UI_FORMA_DE_PAGO"),
				template: '<span >#: Descripcion # </span>',

			},
			idActual: false,
			idDefecto: false,
			seleccionado: false,
			itemPostFiltro: false,
			filtros: {
				IdPortafolio: { "field": "IdPortafolio", "operator": "eq", "value": $scope.data.IdPortafolioCorredora || 0 },
				IdMoneda: { "field": "IdMoneda", "operator": "eq", "value": $scope.data.oConfiguracionMonedaLiquidacionOperacion.seleccionado.IdMonedaLiquidacion || "CLP" },
				IdEstado: { "field": "IdEstado", "operator": "eq", "value": 'ACT' },
				IdTipoProducto: { "field": "IdTipoProducto", "operator": "eq", "value": 'MERF' }
			},
		};
		
		//Inicial Venta
		$scope.confPreferenciaPagoEntregamosVenta = {
			tipo: "combobox-forma-pago-corredora",
			configuracion: {
				placeholder: $translate.instant("UI_FORMA_DE_PAGO"),
				template: '<span >#: Descripcion # </span>',

			},
			idActual: false,
			idDefecto: false,
			seleccionado: false,
			itemPostFiltro: false,
			filtros: {
				IdPortafolio: { "field": "IdPortafolio", "operator": "eq", "value": $scope.data.IdPortafolioCorredora || 0 },
				IdMoneda: { "field": "IdMoneda", "operator": "eq", "value": $scope.data.oConfiguracionMonedaLiquidacionOperacion.seleccionado.IdMonedaLiquidacion || "CLP" },
				IdEstado: { "field": "IdEstado", "operator": "eq", "value": 'ACT' },
				IdTipoProducto: { "field": "IdTipoProducto", "operator": "eq", "value": 'MERF' }
			},
		};
		
		//Final Venta
		$scope.confPreferenciaPagoRecibimosVenta= {
			tipo: "combobox-forma-pago-cliente",
			configuracion: {
				placeholder: $translate.instant("UI_FORMA_DE_PAGO"),
				template: '<span >#: Descripcion # </span>',

			},
			idActual: false,
			idDefecto: false,
			seleccionado: false,
			itemPostFiltro: false,
			filtros: {
				IdPortafolio: { "field": "IdPortafolio", "operator": "eq", "value":  -1 },
				IdMoneda: { "field": "IdMoneda", "operator": "eq", "value": $scope.data.oConfiguracionMonedaLiquidacionOperacion.seleccionado.IdMonedaLiquidacion || "CLP" },				
				IdEstado: { "field": "IdEstado", "operator": "eq", "value": 'ACT' },
				IdTipoProducto: { "field": "IdTipoProducto", "operator": "eq", "value": 'MERF' }
			},
		};
		

		/*******************************************************************
		 * CONFIGURACION GRILLAS
		 ******************************************************************/
		
		$scope.getSchemaFormasPago = function () {
			return {
				model: {
					fields: {
						IdFormaDePago: { type: "string" },
						NombreFormaDePago: { type: "string" },
						Dias: { type: "number" },
						PorDefecto: { type: "string" },
						RecibePagaPortafolioDesc: { type: "string" },
						IdPreferenciaPago: { type: "string" },
						IdTipoOperacion: { type: "string" },
						IdMedioDePago: { type: "string" },
						IdMoneda: { type: "string" },
						UsaPersonaRelacionada: { type: "string" },
						FechaCreacion: { type: "sysdate" },
						FechaModificacion: { type: "sysdate" },
						IdUsuarioCreador: { type: "number" },
						IdEstado: { type: "string" },
						IdEmpresa: { type: "number" },
						NombreTipoOperacion: { type: "string" },
						IdTipoProducto: { type: "string" },
						NombreTipoProducto: { type: "string" },
						RecibePagaPortafolio: { type: "string" },
						NombreMedioDePago: { type: "string" },
						NombreMoneda: { type: "string" },
						UsaCuentaBancaria: { type: "string" },
						UsaTarjetaBancaria: { type: "string" },
						NombreBanco: { type: "string" },
						FechaLiquidacion: { type: "sysdate" },
						NombreEstado: { type: "string" },
						Monto: { type: "number" },
						CuentaBancaria: { type: "string" },
						IdComprobantePagoOperacion: { type: "numbrer" }
					}
				}
			}
		};


		$scope.columnasFormasDePago = function () {

			return [
				{ field: "NombreFormaDePago", title: $translate.instant('UI_FORMA_PAGO'), width: "80px", align: "left" },
				{ field: "NombreMoneda", title: $translate.instant('UI_MONEDA'), width: "80px", align: "left" },
				{ field: "NombreBanco", title: $translate.instant('UI_BANCO'), width: "70px", align: "left" },
				{ field: "CuentaBancaria", title: $translate.instant('UI_CUENTA_BANCARIA'), width: "70px", align: "left" },
				{ field: "FechaLiquidacion", title: $translate.instant('UI_FECHA_LIQUIDACION'), width: "70px", align: "left" },
				{
					field: "Monto", title: $translate.instant('UI_MONTO'), width: "70px", align: "left",
					editable:true,
					template_: " <label> #:  (util.bcs_formato(MontoCompra,'GEN','Monto',$scope.data.oConfiguracionMonedaLiquidacionOperacion.seleccionado.IdMonedaLiquidacion,'')).valor #</label>",
					format: function (value, dataItem) {
						return util.bcs_formato(value, 'GEN', 'Monto', dataItem.NombreMoneda, dataItem.NombreMoneda, '');
					}
				},

			];
		};
				
		
		$scope.confBasicaFormaDePagoRecibimos = {			
			
			sortable: true,
			filterable: false,
			selectable: "row",
			reorderable: true,
			userSettings: true,
			groupable: false,
			height: 120,
			resizable: true,
			autoBind: true,		
			scrollable: false,
			dataSource: new bcs.DataSource({ 
				type : "array",
				definition : {
					data : [],
					pageSize: 10,
					schema: $scope.getSchemaFormasPago()
				},
				
			}),
			columns: $scope.columnasFormasDePago()
		};

		$scope.confBasicaFormaDePagoEntregamos = {
						
			sortable: true,
			filterable: false,
			selectable: "row",
			reorderable: true,
			userSettings: true,
			groupable: false,
			height: 120,
			resizable: true,
			autoBind: true,		
			scrollable: false,
			dataSource: new bcs.DataSource({ 
				type : "array",
				definition : {
					data : [],
					pageSize: 10,
					schema: $scope.getSchemaFormasPago()
				},
				
			}),
			columns: $scope.columnasFormasDePago()
		};
		

		

		/*******************************************************************
		 * FUNCIONES
		 ******************************************************************/
	
		$scope.montoFinalformaPago = function (){
			if($scope.validarMontoAsignado()){
				if ($scope.data.oConfiguracionTipoReajusteOperacion.idActual == 'CLP') {
					return Number(bcsComunes.calcularMontoPlazo(Number($scope.data.kngMontoAsignadoMonedaLiquidacion), Number($scope.data.plazoOperacion), Number($scope.data.kngTasaOperacion))).toFixed($scope.data.IdDecimalTipoReajusteDisplay);
				}
				else {
					return Number(calcularMontoaPlazo(Number($scope.data.kngMontoAsignadoMonedaLiquidacion), Number($scope.data.plazoOperacion), Number($scope.data.kngTasaOperacion),360)).toFixed($scope.data.IdDecimalTipoReajusteDisplay);
				}
			}
			else{
				if ($scope.data.oConfiguracionTipoReajusteOperacion.idActual == 'UF'){
					return $scope.data.kngMontoValorFinalOperacion || 0;
				}else{
					return $scope.data.kngMontoValorFinalOperacionMoneda || 0;
				}
				
			}
		}
		
	
		$scope.tipoOperacionActual = function(){
			if($scope.data.oConfiguracionTipoOperacion.idActual === 'VENPAC' || $scope.data.oConfiguracionTipoOperacion.idActual === 'VENTER'){
				return 'VEN';
			}else if($scope.data.oConfiguracionTipoOperacion.idActual === 'COMPAC' || $scope.data.oConfiguracionTipoOperacion.idActual === 'COMTER'){
				return 'COM';
			}
		}
		
		$scope.entradaModificarFormaDePago = function(ArrayFormaDePago){
			
			let entraFP = {}
			
			entraFP.p_IdEmpresa = $rootScope.SesionIdEmpresa;
			entraFP.p_IdUsuario = $rootScope.SesionIdUsuario;
			entraFP.p_IdModulo = 'MRF';
			entraFP.p_UsaReglasNegocio = "N";
			entraFP.p_FormaPagoNueva = 'N';
			entraFP.p_ActualizaFormaPago = "S";
			
			entraFP.p_ComprobantePagoOperacion =  ArrayFormaDePago.map(function (formaPago){
				
				return{
					  "AfectarTesoreria": formaPago.AfectarTesoreria
					, "FechaIngresoComprobantePagoOperacion": formaPago.FechaIngresoComprobantePagoOperacion
					, "FechaLiquidacionComprobantePagoOperacion": formaPago.FechaLiquidacionComprobantePagoOperacion
					, "IdAbono": formaPago.IdAbono
					, "IdAbonoAnticipo": formaPago.IdAbonoAnticipo
					, "IdCargo": formaPago.IdCargo
					, "IdCargoAnticipo":formaPago.IdCargoAnticipo
					, "IdComprobantePagoOperacion": formaPago.IdComprobantePagoOperacion
					, "IdCuentaBanco": formaPago.IdCuentaBancaria
					, "IdEmpresa": formaPago.IdEmpresa
					, "IdEstado": formaPago.IdEstado
					, "IdFormaDePago": formaPago.IdPreferenciaPago
					, "IdIngresoEgreso": formaPago.IdIngresoEgreso
					, "IdIngresoEgresoAnticipo": formaPago.IdIngresoEgresoAnticipo
					, "IdInternoPagoOperacion": formaPago.IdInternoPagoOperacion
					, "IdModalidadPago": formaPago.IdModalidadPago
					, "IdMoneda": formaPago.IdMoneda
					, "IdOperacion": formaPago.IdOperacion
					, "IdTipoProducto":formaPago.IdTipoProducto
					, "Monto": formaPago.Monto
					, "RecibePagaPortafolio": formaPago.RecibePagaPortafolio				
				}
				
			});
			
			return entraFP;
			
		}
		
		
		$scope.ModificarFormaDePago = function (formaDePago){
			
			let datosParam = $scope.entradaModificarFormaDePago(formaDePago);

			bcsServiciosMesa.CrearFormaDePagoOperacionMesaRF(datosParam, function (respuesta) {
				if (respuesta.P_OUT_ERRORESCONTADOR > 0) {
					console.error('Error al agregar formas de pago', $translate.instant('UI_ERROR_AL_GRABAR'));
					var error = '';
					$.each(respuesta.P_OUT_MENSAJES, function (key, value) {
						if (value.Tipo === 'ERROR') {
							error = value.descripcion;
						}
					});					

				}else {					
					$scope.data.marcarOperacionModificada();
					bcsComunes.msg("success", 'Forma de Pago', 'Forma de pago modificada correctamente.');
				}

			});
		}

		$scope.actualizarMontosFormasDePago = function (cerramosVentanaOperacion) {	
			if(cerramosVentanaOperacion === undefined || cerramosVentanaOperacion === null){
				cerramosVentanaOperacion = false;//solo para cuando hay mas de una forma de pago
			}
			let cantidadRecibimos = $scope.data.formaDePagoRecibimos.length;
			let cantidadEntregamos = $scope.data.formaDePagoEntregamos.length;	
										
			if(cantidadRecibimos === 1 && !cerramosVentanaOperacion){
				$scope.confBasicaFormaDePagoRecibimos.dataSource.data().forEach(fmp => {
										
					if($scope.tipoOperacionActual() ==='COM'){
						if($scope.data.oConfiguracionTipoOperacion.seleccionado.IdTipoOperacion === 'COMTER'){
							$scope.data.formaDePagoRecibimos[0].Monto = $scope.validarMontoAsignado() ? $scope.data.kngMontoAsignadoMonedaLiquidacion : $scope.data.kngMontoOperacionMoneda;
							fmp.Monto = $scope.validarMontoAsignado() ? $scope.data.kngMontoAsignadoMonedaLiquidacion : $scope.data.kngMontoOperacionMoneda;
								
						}
						else
						{
							fmp.Monto = Number($scope.montoFinalformaPago());
							$scope.data.formaDePagoRecibimos[0].Monto = Number($scope.montoFinalformaPago());
						}
					}
					else
					{
						if($scope.data.oConfiguracionTipoOperacion.seleccionado.IdTipoOperacion === 'VENTER'){
							fmp.Monto = $scope.validarMontoAsignado() ? $scope.data.kngMontoAsignadoMonedaLiquidacion : $scope.data.kngMontoOperacionMoneda;
							$scope.data.formaDePagoRecibimos[0].Monto = $scope.validarMontoAsignado() ? $scope.data.kngMontoAsignadoMonedaLiquidacion : $scope.data.kngMontoOperacionMoneda;
								
			         	      
			            }
						else
						{
							fmp.Monto = Number($scope.montoFinalformaPago());
							$scope.data.formaDePagoRecibimos[0].Monto = Number($scope.montoFinalformaPago());
								
			            }
            		}					
				});
												
				$scope.confBasicaFormaDePagoRecibimos.dataSource.sync()		
				$scope.ModificarFormaDePago($scope.data.formaDePagoRecibimos);	
			}
			else if (cantidadRecibimos > 1){
				//recorro data source
				$scope.confBasicaFormaDePagoRecibimos.dataSource.data().forEach(fmp => {					
					console.log("monto data source Recibimos: "+fmp.Monto);
					$scope.data.formaDePagoRecibimos.forEach(function (item) {
						if(item.IdComprobantePagoOperacion === fmp.IdComprobantePagoOperacion){
							item.Monto = fmp.Monto;
						}
						
					});
				});
				$scope.ModificarFormaDePago($scope.data.formaDePagoRecibimos);	
			}
			
			if(cantidadEntregamos === 1 && !cerramosVentanaOperacion){
				
				$scope.confBasicaFormaDePagoEntregamos.dataSource.data().forEach(fmp => {					
					if($scope.tipoOperacionActual() ==='COM'){
							if($scope.data.oConfiguracionTipoOperacion.seleccionado.IdTipoOperacion === 'COMTER'){
								fmp.Monto = $scope.validarMontoAsignado() ? $scope.data.kngMontoAsignadoMonedaLiquidacion : $scope.data.kngMontoOperacionMoneda;
								$scope.data.formaDePagoEntregamos[0].Monto = $scope.validarMontoAsignado() ? $scope.data.kngMontoAsignadoMonedaLiquidacion : $scope.data.kngMontoOperacionMoneda;
							}
							else
							{
								fmp.Monto = $scope.validarMontoAsignado() ? $scope.data.kngMontoAsignadoMonedaLiquidacion : $scope.data.kngMontoOperacion;
								$scope.data.formaDePagoEntregamos[0].Monto = $scope.validarMontoAsignado() ? $scope.data.kngMontoAsignadoMonedaLiquidacion : $scope.data.kngMontoOperacion;
							}
						
					}else{
			          
			          	  	if($scope.data.oConfiguracionTipoOperacion.seleccionado.IdTipoOperacion === 'VENTER'){
								fmp.Monto = $scope.validarMontoAsignado() ? $scope.data.kngMontoAsignadoMonedaLiquidacion : $scope.data.kngMontoOperacionMoneda;
								$scope.data.formaDePagoEntregamos[0].Monto = $scope.validarMontoAsignado() ? $scope.data.kngMontoAsignadoMonedaLiquidacion : $scope.data.kngMontoOperacionMoneda;
			         		}
							else
							{
			              		fmp.Monto = $scope.validarMontoAsignado() ? $scope.data.kngMontoAsignadoMonedaLiquidacion : $scope.data.kngMontoOperacion;
								$scope.data.formaDePagoEntregamos[0].Monto = $scope.validarMontoAsignado() ? $scope.data.kngMontoAsignadoMonedaLiquidacion : $scope.data.kngMontoOperacion;
							}
						
					}
				});
				
				$scope.confBasicaFormaDePagoEntregamos.dataSource.sync()
				$scope.ModificarFormaDePago($scope.data.formaDePagoEntregamos);	
			}
			else if (cantidadEntregamos > 1){
				//recorro data source
				$scope.confBasicaFormaDePagoEntregamos.dataSource.data().forEach(fmp => {					
					console.log("monto data source entregamos: "+fmp.Monto);
					$scope.data.formaDePagoEntregamos.forEach(function (item) {
						if(item.IdComprobantePagoOperacion === fmp.IdComprobantePagoOperacion){
							item.Monto = fmp.Monto;
						}
						
					});
				});
				$scope.ModificarFormaDePago($scope.data.formaDePagoEntregamos);	
			}

		}
		
		$scope.validarMontoAsignado = function(){
			if($scope.data.kngMontoAsignadoMonedaLiquidacion === 0 || $scope.data.kngMontoAsignadoMonedaLiquidacion == undefined
				|| $scope.data.kngMontoAsignadoMonedaLiquidacion == null || $scope.data.kngMontoAsignadoMonedaLiquidacion == false){
					return false;
			}
			else
			{
				return true;
			}
		}		

		$scope.operacionModificadaGuardaFP = function(p_Operacion){
			if($scope.data.operacionEnModificacion.IdOperacion === p_Operacion.IdOperacion
				 && ($scope.confBasicaFormaDePagoEntregamos.dataSource.data().length > 0 || $scope.confBasicaFormaDePagoRecibimos.dataSource.data().length > 0)){
				if($scope.data.operacionEnModificacion.IdPortafolio !== p_Operacion.IdPortafolio){
					$scope.eliminaFormasPagoUI();
					return false;
				} 
				if($scope.data.operacionEnModificacion.IdPortafolioCorredor !== p_Operacion.IdPortafolioCorredor){
					$scope.eliminaFormasPagoUI();
					return false;
				} 
				if($scope.data.operacionEnModificacion.IdTipoOperacion !== p_Operacion.IdTipoOperacion){
					$scope.eliminaFormasPagoUI();
					return false;
				} 
				if($scope.data.operacionEnModificacion.IdTipoReajuste !== p_Operacion.IdTipoReajuste){
					$scope.eliminaFormasPagoUI();
					return false;
				} 
				if($scope.data.operacionEnModificacion.Plazo !== p_Operacion.Plazo){
					$scope.eliminaFormasPagoUI();
					return false;
				} 
				if($scope.data.operacionEnModificacion.IdCondicionDeLiquidacion !== p_Operacion.IdCondicionDeLiquidacion){
					$scope.eliminaFormasPagoUI();
					return false;
				} 
				if($scope.data.operacionEnModificacion.Tasa !== p_Operacion.Tasa){
					//actualizar formas de pago del monto final
					$scope.actualizarMontosFormasDePago()
				}
				if($scope.data.operacionEnModificacion.Monto !== p_Operacion.Monto){
					//actualizar forma de pago inicial
					$scope.actualizarMontosFormasDePago()
				}
				return true;
			}else{
				return false
			}
		}
		$scope.eliminaFormasPagoUI = function(){
			$scope.confBasicaFormaDePagoEntregamos.dataSource.removeAll();
			$scope.confBasicaFormaDePagoRecibimos.dataSource.removeAll();
			$scope.data.formaDePagoEntregamos =[];
			$scope.data.formaDePagoRecibimos =[];
			$scope.confBasicaFormaDePagoRecibimos.dataSource.sync();
			$scope.confBasicaFormaDePagoEntregamos.dataSource.sync();
			bcsComunes.msg("warning", $translate.instant("UI_ADVERTENCIA"), $translate.instant('FORMAS_DE_PAGO_UI_ELIMINADAS'));
		}
		
		$scope.cargarFormasDePago = function (idOperacion) {
			let filtroPP = [];
			if (idOperacion != undefined && idOperacion != "" && idOperacion != 0) {
				filtroPP.push({ field: "IdOperacion", operator: "eq", value: idOperacion })
				consultarComprobantePagoOperacion.query({ filter: filtroPP }).then(function () {
					if (consultarComprobantePagoOperacion.data().length > 0) {

						$.each(consultarComprobantePagoOperacion.data(), function (key, value) {
							let formaPago = [{
								"IdComprobantePagoOperacion": value.IdComprobantePagoOperacion
								, "NombreFormaDePago": value.NombreFormaDePago
								, "NombreMoneda": value.IdMoneda
								, "NombreBanco": value.NombreBancoCuentaBancaria
								, "CuentaBancaria": value.NumeroCuentaBancaria
								, "FechaLiquidacion": util.dateSPdate(value.FechaLiquidacionComprobantePagoOperacion || '0001-01-01')
								, "Monto": value.Monto

							}];

							if ((value.IdTipoOperacion === 'VENPAC' || value.IdTipoOperacion === 'VENTER') && value.RecibePagaPortafolio === 'P') {
								if (!($scope.confBasicaFormaDePagoEntregamos.dataSource.data().find(fmp => fmp.IdComprobantePagoOperacion == formaPago[0].IdComprobantePagoOperacion))) {
									$scope.confBasicaFormaDePagoEntregamos.dataSource.add(formaPago[0]);
									$scope.numeroIdInternoOperacion += 1;

								}
								if (!($scope.data.formaDePagoEntregamos.find(fmp => fmp.IdComprobantePagoOperacion == value.IdComprobantePagoOperacion))) {
									$scope.data.formaDePagoEntregamos.push(value);

								}


							} else if ((value.IdTipoOperacion === 'VENPAC' || value.IdTipoOperacion === 'VENTER') && value.RecibePagaPortafolio === 'R') {

								if (!($scope.confBasicaFormaDePagoRecibimos.dataSource.data().find(fmp => fmp.IdComprobantePagoOperacion == formaPago[0].IdComprobantePagoOperacion))) {
									$scope.confBasicaFormaDePagoRecibimos.dataSource.add(formaPago[0]);
									$scope.numeroIdInternoOperacion += 1;
								}
								if (!($scope.data.formaDePagoRecibimos.find(fmp => fmp.IdComprobantePagoOperacion == value.IdComprobantePagoOperacion))) {
									$scope.data.formaDePagoRecibimos.push(value);

								}
							}

							if ((value.IdTipoOperacion === 'COMPAC' || value.IdTipoOperacion === 'COMTER') && value.RecibePagaPortafolio === 'P') {
								
								if (!($scope.confBasicaFormaDePagoEntregamos.dataSource.data().find(fmp => fmp.IdComprobantePagoOperacion == formaPago[0].IdComprobantePagoOperacion))) {
									$scope.confBasicaFormaDePagoEntregamos.dataSource.add(formaPago[0]);
									$scope.numeroIdInternoOperacion += 1;

								}
								if (!($scope.data.formaDePagoEntregamos.find(fmp => fmp.IdComprobantePagoOperacion == value.IdComprobantePagoOperacion))) {
									$scope.data.formaDePagoEntregamos.push(value);

								}

							} else if ((value.IdTipoOperacion === 'COMPAC' || value.IdTipoOperacion === 'COMTER') && value.RecibePagaPortafolio === 'R') {
								
								if (!($scope.confBasicaFormaDePagoRecibimos.dataSource.data().find(fmp => fmp.IdComprobantePagoOperacion == formaPago[0].IdComprobantePagoOperacion))) {
									$scope.confBasicaFormaDePagoRecibimos.dataSource.add(formaPago[0]);
									$scope.numeroIdInternoOperacion += 1;
								}
								if (!($scope.data.formaDePagoRecibimos.find(fmp => fmp.IdComprobantePagoOperacion == value.IdComprobantePagoOperacion))) {
									$scope.data.formaDePagoRecibimos.push(value);

								}

							}

						});
					}
				})
				$scope.confBasicaFormaDePagoRecibimos.dataSource.sync();
				$scope.confBasicaFormaDePagoEntregamos.dataSource.sync();
			}
		};

		$scope.agregarFormaDePagoRecibimos = function () {
			var datosParam = new Object();
			let cantidadFormasDePagoActual = $scope.confBasicaFormaDePagoRecibimos.dataSource.data().length;			
			
			if(($scope.data.oConfiguracionTipoOperacion.idActual).includes("COM")){
				item = $scope.confPreferenciaPagoRecibimosCompra.seleccionado;
			}else{
				item = $scope.confPreferenciaPagoRecibimosVenta.seleccionado;
			}
			
			if (($scope.cantidadFormasDePago != 0 && cantidadFormasDePagoActual  >= $scope.cantidadFormasDePago) || $scope.cantidadFormasDePago === 0 || $scope.cantidadFormasDePago === undefined || $scope.cantidadFormasDePago == null) {
				bcsComunes.msg("warning", $translate.instant("UI_ADVERTENCIA"), $translate.instant('MAXIMO_FORMAS_DE_PAGO_SUPERADA') + $scope.cantidadFormasDePago);
				return;
			}	
			datosParam.p_IdEmpresa = $rootScope.SesionIdEmpresa;
			datosParam.p_IdUsuario = $rootScope.SesionIdUsuario;
			datosParam.p_IdModulo = 'MRF';
			datosParam.p_UsaReglasNegocio = "N";
			datosParam.p_FormaPagoNueva = 'S'
			let IdModalidadPago = '';
			if (item.IdMedioDePago === 'CHE' || item.IdMedioDePago === 'VVI' || item.IdMedioDePago === 'EFE' || item.IdMedioDePago === 'EBA') {
				IdModalidadPago = 'MODCAJ'
			} else {
				IdModalidadPago = 'MODDEP'
			}
			let montoAsignado = 0;
			let fechaLiquidacion;
			
			if ($scope.data.gridApiCarteraInstrumento.dataSource.data().length == 0 && $scope.data.formaDePagoRecibimos.length == 0) {
				if ($scope.data.oConfiguracionTipoOperacion.idActual === 'VENPAC' || $scope.data.oConfiguracionTipoOperacion.idActual === 'VENTER') {
					montoAsignado = $scope.data.montoValorFinalOperacionMoneda;
					fechaLiquidacion = util.dateSPdate($scope.data.fechaVencimientoOperacion || '0001-01-01')
				}
				else {
					montoAsignado = $scope.data.montoValorFinalOperacionMoneda;
					fechaLiquidacion = util.dateSPdate($scope.data.fechaVencimientoOperacion || '0001-01-01')
				}
			}
			else {
				if ($scope.data.oConfiguracionTipoOperacion.idActual === 'VENPAC' || $scope.data.oConfiguracionTipoOperacion.idActual === 'VENTER') {
					montoAsignado = $scope.montoFinalformaPago();
					fechaLiquidacion = util.dateSPdate($scope.data.fechaVencimientoOperacion || '0001-01-01')
				} else {
					montoAsignado = $scope.montoFinalformaPago();
					fechaLiquidacion = util.dateSPdate($scope.data.fechaVencimientoOperacion || '0001-01-01')
				}
			}

			//AQUI comprobar que no esta repetida 			
			if($scope.formaDePagoRepetida($scope.confBasicaFormaDePagoRecibimos.dataSource.data(),item))return;

			datosParam.p_ComprobantePagoOperacion = [{
				"AfectarTesoreria": item.IdMedioDePago === 'CIN' ? 'N' : 'S'
				, "FechaIngresoComprobantePagoOperacion": util.dateSPdate($scope.data.fechaLiquidacionOperacion)
				, "FechaLiquidacionComprobantePagoOperacion": util.dateSPdate($scope.data.fechaVencimientoOperacion)
				, "IdAbono": 0
				, "IdAbonoAnticipo": 0
				, "IdCargo": 0
				, "IdCargoAnticipo": 0
				, "IdComprobantePagoOperacion": 0
				, "IdCuentaBanco": item.IdCuentaBancaria
				, "IdEmpresa": item.IdEmpresa
				, "IdEstado": "PEN"
				, "IdFormaDePago": item.IdPreferenciaPago
				, "IdIngresoEgreso": 0
				, "IdIngresoEgresoAnticipo": 0
				, "IdInternoPagoOperacion": $scope.numeroIdInternoOperacion + 1
				, "IdModalidadPago": IdModalidadPago
				, "IdMoneda": item.IdMoneda
				, "IdOperacion": $scope.data.kngSecuenciaOperacion
				, "IdTipoProducto": $scope.data.oConfiguracionTipoOperacion.seleccionado.IdTipoProducto
				, "Monto": cantidadFormasDePagoActual==0?montoAsignado:0
				, "RecibePagaPortafolio": "R"
				,"IdMedioDePago" : item.IdMedioDePago
			}];
			bcsServiciosMesa.CrearFormaDePagoOperacionMesaRF(datosParam, function (respuesta) {
				if (respuesta.P_OUT_ERRORESCONTADOR > 0) {
					console.error('Error al agregar formas de pago', $translate.instant('UI_ERROR_AL_GRABAR'));
					var error = '';
					$.each(respuesta.P_OUT_MENSAJES, function (key, value) {
						if (value.Tipo === 'ERROR') {
							error = value.descripcion;
						}
					});
					bcsComunes.msg("warning", 'Valorizador', $translate.instant('UI_ERROR_RUTINA_VALORIZACION') + ': ' + error);

				} else {
					var formaPago = {};
					$.each(respuesta.P_OUT_FORMADEPAGO, function (key, value) {
						$scope.numeroIdInternoOperacion = $scope.numeroIdInternoOperacion + 1;
						formaPago.IdComprobantePagoOperacion = value.IdComprobantePagoOperacion;
						formaPago.NombreFormaDePago = value.NombreFormaDePago;
						formaPago.NombreMoneda = value.IdMoneda;
						formaPago.NombreBanco = value.NombreBancoCuentaBancaria
						formaPago.CuentaBancaria = value.NumeroCuentaBancaria;
						formaPago.FechaLiquidacion = value.FechaLiquidacionComprobantePagoOperacion;
						formaPago.Monto = value.Monto;
						$scope.data.marcarOperacionModificada();					
						$scope.confBasicaFormaDePagoRecibimos.dataSource.add(formaPago);

					});



					datosParam.p_ComprobantePagoOperacion[0].NombreFormaDePago = formaPago.NombreFormaDePago;
					datosParam.p_ComprobantePagoOperacion[0].CuentaBancaria = formaPago.CuentaBancaria;
					datosParam.p_ComprobantePagoOperacion[0].IdComprobantePagoOperacion = formaPago.IdComprobantePagoOperacion;

					if ($scope.confBasicaFormaDePagoRecibimos.dataSource.data().length > 1) {
						$scope.data.formaDePagoRecibimos.push(datosParam.p_ComprobantePagoOperacion[0]);

					} else {
						$scope.data.formaDePagoRecibimos = datosParam.p_ComprobantePagoOperacion;

					}

					bcsComunes.msg("success", 'Forma de Pago', 'Forma de pago ingresada correctamente.');

				}

			});
		}

		$scope.agregarFormaDePagoEntregamos= function () {
			var datosParam = new Object();
			let cantidadFormasDePagoActual= $scope.confBasicaFormaDePagoEntregamos.dataSource.data().length;
			let item ="";
			
			if(($scope.data.oConfiguracionTipoOperacion.idActual).includes("COM")){
				item = $scope.confPreferenciaPagoEntregamosCompra.seleccionado;
			}else{
				item = $scope.confPreferenciaPagoEntregamosVenta.seleccionado;
			}
			
			if (($scope.cantidadFormasDePago != 0 && cantidadFormasDePagoActual >= $scope.cantidadFormasDePago) || $scope.cantidadFormasDePago === 0 || $scope.cantidadFormasDePago === undefined || $scope.cantidadFormasDePago == null) {
				bcsComunes.msg("warning", $translate.instant("UI_ADVERTENCIA"), $translate.instant('MAXIMO_FORMAS_DE_PAGO_SUPERADA') + $scope.cantidadFormasDePago);
				return;
			}	
			datosParam.p_IdEmpresa = $rootScope.SesionIdEmpresa;
			datosParam.p_IdUsuario = $rootScope.SesionIdUsuario;
			datosParam.p_IdModulo = 'MRF';
			datosParam.p_UsaReglasNegocio = "N";
			datosParam.p_FormaPagoNueva = 'S'
			let IdModalidadPago = '';
			if (item.IdMedioDePago === 'CHE' || item.IdMedioDePago === 'VVI' || item.IdMedioDePago === 'EFE' || item.IdMedioDePago === 'EBA') {
				IdModalidadPago = 'MODCAJ'
			} else {
				IdModalidadPago = 'MODDEP'
			}
			let montoAsignado = 0;
			let fechaLiquidacion;
			
			if ($scope.data.gridApiCarteraInstrumento.dataSource.data().length == 0 && $scope.data.formaDePagoEntregamos.length == 0) {
				if ($scope.data.oConfiguracionTipoOperacion.idActual === 'VENPAC' || $scope.data.oConfiguracionTipoOperacion.idActual === 'VENTER') {
					montoAsignado = $scope.data.montoOperacionMoneda;
					fechaLiquidacion = util.dateSPdate($scope.data.fechaLiquidacionOperacion || '0001-01-01')
				}
				else {
					montoAsignado = $scope.data.montoOperacionMoneda;
					fechaLiquidacion = util.dateSPdate($scope.data.fechaLiquidacionOperacion || '0001-01-01')
				}
			}
			else {
				if ($scope.data.oConfiguracionTipoOperacion.idActual === 'VENPAC' || $scope.data.oConfiguracionTipoOperacion.idActual === 'VENTER') {
					montoAsignado = $scope.data.kngMontoAsignadoMonedaLiquidacion;
					fechaLiquidacion = util.dateSPdate($scope.data.fechaLiquidacionOperacion || '0001-01-01')
				} else {
					montoAsignado = $scope.data.kngMontoAsignadoMonedaLiquidacion;
					fechaLiquidacion = util.dateSPdate($scope.data.fechaLiquidacionOperacion || '0001-01-01')
				}
			}
			//AQUI comprobar que no esta repetida 			
			if($scope.formaDePagoRepetida($scope.confBasicaFormaDePagoEntregamos.dataSource.data(),item))return;

			datosParam.p_ComprobantePagoOperacion = [{
				"AfectarTesoreria": item.IdMedioDePago === 'CIN' ? 'N' : 'S'
				, "FechaIngresoComprobantePagoOperacion": util.dateSPdate($scope.data.fechaLiquidacionOperacion)
				, "FechaLiquidacionComprobantePagoOperacion": fechaLiquidacion
				, "IdAbono": 0
				, "IdAbonoAnticipo": 0
				, "IdCargo": 0
				, "IdCargoAnticipo": 0
				, "IdComprobantePagoOperacion": 0
				, "IdCuentaBanco": item.IdCuentaBancaria
				, "IdEmpresa": item.IdEmpresa
				, "IdEstado": "PEN"
				, "IdFormaDePago": item.IdPreferenciaPago
				, "IdIngresoEgreso": 0
				, "IdIngresoEgresoAnticipo": 0
				, "IdInternoPagoOperacion": $scope.numeroIdInternoOperacion + 1
				, "IdModalidadPago": IdModalidadPago
				, "IdMoneda": item.IdMoneda
				, "IdOperacion": $scope.data.kngSecuenciaOperacion
				, "IdTipoProducto": $scope.data.oConfiguracionTipoOperacion.seleccionado.IdTipoProducto
				, "Monto": cantidadFormasDePagoActual ==0 ?montoAsignado:0
				, "RecibePagaPortafolio":"P"
				, "IdMedioDePago" : item.IdMedioDePago
			}];
			bcsServiciosMesa.CrearFormaDePagoOperacionMesaRF(datosParam, function (respuesta) {
				if (respuesta.P_OUT_ERRORESCONTADOR > 0) {
					console.error('Error al agregar formas de pago', $translate.instant('UI_ERROR_AL_GRABAR'));
					var error = '';
					$.each(respuesta.P_OUT_MENSAJES, function (key, value) {
						if (value.Tipo === 'ERROR') {
							error = value.descripcion;
						}
					});

				} else {
					var formaPago = {};
					$.each(respuesta.P_OUT_FORMADEPAGO, function (key, value) {
						$scope.numeroIdInternoOperacion = $scope.numeroIdInternoOperacion + 1;
						formaPago.IdComprobantePagoOperacion = value.IdComprobantePagoOperacion;
						formaPago.NombreFormaDePago = value.NombreFormaDePago;
						formaPago.NombreMoneda = value.IdMoneda;
						formaPago.NombreBanco = value.NombreBancoCuentaBancaria
						formaPago.CuentaBancaria = value.NumeroCuentaBancaria;
						formaPago.FechaLiquidacion = value.FechaLiquidacionComprobantePagoOperacion;
						formaPago.Monto = value.Monto;
						$scope.data.marcarOperacionModificada();
						$scope.confBasicaFormaDePagoEntregamos.dataSource.add(formaPago);
						
						datosParam.p_ComprobantePagoOperacion[0].NombreFormaDePago = formaPago.NombreFormaDePago;
						datosParam.p_ComprobantePagoOperacion[0].CuentaBancaria = formaPago.CuentaBancaria;
						datosParam.p_ComprobantePagoOperacion[0].IdComprobantePagoOperacion = formaPago.IdComprobantePagoOperacion;
						
						if ($scope.confBasicaFormaDePagoEntregamos.dataSource.data().length > 1) {
							$scope.data.formaDePagoEntregamos.push(datosParam.p_ComprobantePagoOperacion[0]);
	
						} else {
							$scope.data.formaDePagoEntregamos = datosParam.p_ComprobantePagoOperacion;
	
						}
						bcsComunes.msg("success", 'Forma de Pago', 'Forma de pago ingresada correctamente.');						

					});
				}

			});
		}
		
		//Verifica que las ninguna forma de pago vaya en monto 0		
		$scope.existeFormasDePagoCero = function (){
			let existeMontoCero = false
			
			$scope.confBasicaFormaDePagoRecibimos.dataSource.data().forEach(function (formaDePago){
				if((formaDePago.Monto ===0 || formaDePago.Monto == null) && $scope.data.oConfiguracionTipoOperacion.idActual != "COMTER"){
					existeMontoCero= true;
					bcsComunes.msg("error", $translate.instant("UI_ERROR"), $translate.instant('UI_FORMA_DE_PAGO_EN_CERO') +" Recibimos" );	
				}				
			});
			
			$scope.confBasicaFormaDePagoEntregamos.dataSource.data().forEach(function (formaDePago){
				if((formaDePago.Monto ===0 || formaDePago.Monto == null) && $scope.data.oConfiguracionTipoOperacion.idActual != "VENTER"){
					existeMontoCero= true;
					bcsComunes.msg("error", $translate.instant("UI_ERROR"), $translate.instant('UI_FORMA_DE_PAGO_EN_CERO') +" Entregamos");
				}			
			});
			
			return existeMontoCero;
		}
		
		
		
		// Parametros entrada lista de data en el grill , forma de pago entrante
		$scope.formaDePagoRepetida = function (listaFormaDePago, formaDePagoEntrada){
				
			let estaRepetida = false;
			let existeFormDePago =[];
			
			if(listaFormaDePago.length >0){
				existeFormDePago =  listaFormaDePago.filter(function (formaDePagoEnGrilla){		
							
					if(formaDePagoEnGrilla.NombreFormaDePago ===formaDePagoEntrada.MedioDePago && formaDePagoEnGrilla.CuentaBancaria===formaDePagoEntrada.NumeroCuentaBancaria && formaDePagoEnGrilla.NombreBanco===formaDePagoEntrada.NombreBancoCuentaBancaria){
						return formaDePagoEnGrilla;
					}
				})
			}		
			
			if(existeFormDePago.length>0){
				bcsComunes.msg("error", $translate.instant("UI_ERROR"), $translate.instant('UI_FORMA_DE_PAGO_REPETIDA') +" "+formaDePagoEntrada.MedioDePago );
				estaRepetida = true
			}
			
			return estaRepetida;
		}

		$scope.eliminarFormaDePagoEntregamos = function () {

			var datosParam = new Object();
			let formaDePagoEntregamos =$scope.datos.formaDePagoEntregamosSelecionada;
			
			datosParam.p_IdEmpresa = $rootScope.SesionIdEmpresa;
			datosParam.p_IdUsuario = $rootScope.SesionIdUsuario;
			datosParam.p_IdComprobantePagoOperacion = formaDePagoEntregamos.IdComprobantePagoOperacion;
			datosParam.p_NombreFormaDePago = formaDePagoEntregamos.NombreFormaDePago;
			bcsServiciosMesa.EliminarFormaDePagoOperacionMesaRF(datosParam, function (respuesta) {
				if (respuesta.P_OUT_ERRORESCONTADOR > 0) {
					console.error('Error al Eliminar formas de pago', $translate.instant('UI_ERROR_AL_GRABAR'));
					var error = '';
					$.each(respuesta.P_OUT_MENSAJES, function (key, value) {
						if (value.Tipo === 'ERROR') {
							error = value.descripcion;
						}
					});
					bcsComunes.msg("warning", 'Valorizador', $translate.instant('UI_ERROR_RUTINA_VALORIZACION') + ': ' + error);

				} else {
					$scope.data.marcarOperacionModificada();
					$.each(respuesta.P_OUT_COMPRABANTEPAGO, function (key, value) {
						$scope.confBasicaFormaDePagoEntregamos.dataSource.data().forEach(fmp => {
							if(fmp){
								if (fmp.IdComprobantePagoOperacion == value.IdComprobantePagoOperacion) {
									$scope.confBasicaFormaDePagoEntregamos.dataSource.data().remove(fmp);
									$scope.datos.formaDePagoEntregamosSelecionada = null;
									return;
								}
							}
						});
						$scope.data.formaDePagoEntregamos = $scope.data.formaDePagoEntregamos.filter(function (item) {
							return item.IdComprobantePagoOperacion !== value.IdComprobantePagoOperacion;
						});
					});
					bcsComunes.msg("warning", 'Forma de Pago', 'Forma de pago eliminada correctamente.');

				}
			});
		}

		$scope.eliminarFormaDePagoRecibimos = function () {

			var datosParam = new Object();
			let formaDePagoRecibimos =$scope.datos.formaDePagoRecibimosSelecionada;
			
			datosParam.p_IdEmpresa = $rootScope.SesionIdEmpresa;
			datosParam.p_IdUsuario = $rootScope.SesionIdUsuario;
			datosParam.p_IdComprobantePagoOperacion = formaDePagoRecibimos.IdComprobantePagoOperacion;
			datosParam.p_NombreFormaDePago = formaDePagoRecibimos.NombreFormaDePago;
			bcsServiciosMesa.EliminarFormaDePagoOperacionMesaRF(datosParam, function (respuesta) {
				if (respuesta.P_OUT_ERRORESCONTADOR > 0) {
					console.error('Error al Eliminar formas de pago', $translate.instant('UI_ERROR_AL_GRABAR'));
					var error = '';
					$.each(respuesta.P_OUT_MENSAJES, function (key, value) {
						if (value.Tipo === 'ERROR') {
							error = value.descripcion;
						}
					});

				} else {
					$scope.data.marcarOperacionModificada();
					$.each(respuesta.P_OUT_COMPRABANTEPAGO, function (key, value) {
						$scope.confBasicaFormaDePagoRecibimos.dataSource.data().forEach(fmp => {
							if(fmp){
								if (fmp.IdComprobantePagoOperacion == value.IdComprobantePagoOperacion) {
									$scope.confBasicaFormaDePagoRecibimos.dataSource.data().remove(fmp);
									$scope.datos.formaDePagoRecibimosSelecionada = null;
									return;
								}
							}
						});
						$scope.data.formaDePagoRecibimos = $scope.data.formaDePagoRecibimos.filter(function (item) {
							return item.IdComprobantePagoOperacion !== value.IdComprobantePagoOperacion;
						});
					});
					bcsComunes.msg("warning", 'Forma de Pago', 'Forma de pago eliminada correctamente.');
				}
			});
		}

		/*******************************************************************
		 * WATCHS, EN MEDIDA DE LO POSIBLE NO USAR MAS WATCHS
		 ******************************************************************/


		$scope.$watchGroup(['data.kngSecuenciaOperacion'], function (newval, oldval) {

			if (newval !== oldval && newval.length == 1 && newval[0] != false & newval[0] != 0) {
				$scope.cargarFormasDePago($scope.data.kngSecuenciaOperacion);
				return;
			}
		});

		//Cierra ventana adicional al presionar la cruz de la ventana principal
		$("[ventanaid='" + $scope.winID + "']").data("kendoWindow").bind("close", 
				function w(e) {
					$scope.actualizarMontosFormasDePago(true);
					if ($scope.WinCartera != null) bcsComunes.ventanaCerrar($scope.WinCartera.winID);
				}
		);

		// deshabilitar boton guardar y modificar cuando el monto es inferior a MONTO_MINIMO_PACTO_EN_UF solo cuando sea pacto

		$scope.data.validarValorIncial = function () {
			if ($scope.data.oConfiguracionTipoOperacion.idActual === 'COMPAC' || $scope.data.oConfiguracionTipoOperacion.idActual === 'VENPAC') {
				var filtroConfiguracionMesa = new Array();
				filtroConfiguracionMesa.push({ field: "CodigoParametro", operator: "eq", value: 'MONTO_MINIMO_PACTO_EN_UF' });
				filtroConfiguracionMesa.push({ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa });
				
				dsConfiguracionMesa.query({ filter: filtroConfiguracionMesa }).then(function () {
					let resultado = dsConfiguracionMesa.data();
					var montoMinimoPactoUf = resultado[0].Valor;
					if (montoMinimoPactoUf > 0) {
						var data = Object();

						data.p_IdEmpresa = $scope.SesionIdEmpresa;
						data.p_CalculosEntrada = [{
							"IdTrabajo": Math.floor(Math.random() * 10000)
							, "IdInterno": 1
							, "Fecha": $scope.SesionFechaSistema
							, "IdMonedaOrigen": "UF"
							, "IdMonedaDestino": "CLP"
							, "ValorCambioLocalModificado": 0
							, "MontoOrigen": montoMinimoPactoUf
							, "IdUso": 'VALORIZADOR'
						}];

						bcsServiciosMesa.ObtenerParidadTipoCambio(data).$promise.then(function (respuesta) {
							var montoDestino = respuesta.p_out_CalculosSalida[0].MontoDestino;
							var montoInput = $scope.data.kngMontoOperacion;
							if (montoDestino > montoInput) {
								$scope.data.validoValorInicialBoton = false;
								bcsComunes.msg("error", $translate.instant('UI_ERROR'), $translate.instant('SRV_MRF_OPERACION_PACTO_INFERIOR_MONTO_UF').replace(/::MontoMinimoUF::/g, montoMinimoPactoUf));
							}else {
								$scope.data.validoValorInicialBoton = true;
							}
						});
					} else {
						$scope.data.validoValorInicialBoton = false;
						bcsComunes.msg("error", $translate.instant('UI_ERROR'), $translate.instant('SRV_MRF_OPERACION_PACTO_INFERIOR_MONTO_UF_OBTENER'));
					}
				});
			} else {
				$scope.data.validoValorInicialBoton = true;
			}
		}
		
		// deshabilitar boton guardar y modificar cuando la fecha no sea habil
		$scope.$watch('data.fechaVencimientoOperacion',function(){
			if ($scope.data.oConfiguracionTipoOperacion.idActual === 'COMPAC' || $scope.data.oConfiguracionTipoOperacion.idActual === 'VENPAC'){
				var fechaVencimientoOperacion = $scope.data.fechaVencimientoOperacion;
				var idPais = $scope.SesionIdPaisSistema;

				if (fechaVencimientoOperacion !== undefined) {
					if(util.esFechaHabil(idPais,fechaVencimientoOperacion)){
						$scope.data.validoVencimiento = true;
					} else {
						$scope.data.validoVencimiento = false;
					}
				} else {
					$scope.data.validoVencimiento = false;
				}
			} else {
				$scope.data.validoVencimiento = true;
			} 
		});

	});