angular.module('ModuloPlataforma').directive('bcsPreferencias',
	function($translate, bcsComunes, $resource, bcsServicioPlataforma) {
		return {
			restrict: 'E',
			replace: false,
			scope: {
				modeloCliente: '=?',
				tipoEntidad: '=?',
				idEntidad: '=?',
				idPortafolio: '=?',
				idPersona: '=?',
				codigoFormulario: '=?'
			},
			templateUrl: '../../Plataforma/UI/directives/bcs-preferencias/bcs-preferencias.html',
			link: function(scope, element, attrs, fn) {

				/*********************************************
				 * VARIABLES Y CONTEXTOS
				 ********************************************/
				scope.currentRootScope = scope.$root;
				scope.idVentana = bcsComunes.ventanaWinId(scope); // bcsComunes.ventanaOverlay(scope.idVentana,true);
				scope.win = scope.win || {};
				scope.win.modoLectura = true;
				scope.win.uiPortafolio = scope.idEntidad && scope.idPersona ? true : false;

				scope.cmbbx = scope.cmbbx || {};
				scope.ds = scope.ds || {};
				scope.grilla = scope.grilla || {};
				scope.fn = scope.fn || {};

				scope.datosAntes = {};
				scope.datosDespues = {};
				scope.definicionIndicadores = {};
				scope.IdDireccionPredeterminada;

				scope.camposRequeridos = {};

				/*********************************************
				 * CHECK INICIAL DE VARIABLES
				 ********************************************/

				if (scope.modeloCliente === undefined) scope.modeloCliente = 0;
				if (scope.idEntidad === undefined) scope.idEntidad = 0;
				if (scope.tipoEntidad === undefined) scope.tipoEntidad = 'PORTAFOLIO';


				/*********************************************
				 * ACCIONES DE PANTALLA
				 ********************************************/

				scope.win.editar = function() {
					scope.win.modoLectura = false;
				};
				var validarCuentaCustodia = function() {
					if (scope.datosDespues['GRUPOGENERAL//GEN_OCCINV'] || scope.datosDespues['GRUPOORDENES//ORDOCUSTRF'] || scope.datosDespues['GRUPOORDENES//ORDOCUSTRF'] || scope.datosDespues['GRUPOORDENES//ORDOCUSTRF'] || scope.datosDespues['GRUPOORDENES//ORDOCUSTRF'] || scope.datosDespues['GRUPOORDENES//ORDCUENTAPROPIA'] || scope.datosDespues['GRUPOORDENES//ORDCUENTABANCO']) {
						return true;
					} else {
						return false;
					}
				}

				var validarSobregirosCustodia = function() {
					if (!scope.datosDespues['GRUPOORDENES//ORDOCUSTRV'] && scope.datosDespues['GRUPOORDENES//ORDSOBREGIRORV']) {
						return { error : true, message: $translate.instant("UI_ERROR_SOBREGIRO_CUSTODIA_RV")}
					}
					if (!scope.datosDespues['GRUPOORDENES//ORDOCUSTRF'] && (scope.datosDespues['GRUPOORDENES//ORDSOBREGIROIF'] || scope.datosDespues['GRUPOORDENES//ORDSOBREGIRORF'])) {
						return { error : true, message: $translate.instant("UI_ERROR_SOBREGIRO_CUSTODIA_RF_IF") }
					}
					return { error : false}
				}
				
				var validarIndicadorFacturacionPrecioPromedio = function() {
					if (scope.datosDespues['GRUPOFACTURACION//FAC_FFPPRV'] && !scope.datosDespues['GRUPOFACTURACION//FAC_SFPINS']) {
						return { error : true, message: $translate.instant("UI_ERROR_DEPENDENCIA_INDICADOR_PRECIO_PROMEDIO_RV")}
					}
					return { error : false}
				}

				scope.$watch(function() {
					return scope.datosDespues['GRUPOORDENES//ORDOCUSTRV'];
				}, function(newValue, oldValue) {
					if (newValue !== oldValue && !newValue) {
						scope.datosDespues['GRUPOORDENES//ORDSOBREGIRORV'] = false;
					}
				});

				
				scope.$watch(function() {
					return scope.datosDespues['GRUPOORDENES//ORDOCUSTRF'];
				}, function(newValue, oldValue) {
					if (newValue !== oldValue && !newValue) {
						scope.datosDespues['GRUPOORDENES//ORDSOBREGIRORF'] = false;
						scope.datosDespues['GRUPOORDENES//ORDSOBREGIROIF'] = false;
					}
				});

				scope.win.guardar = function() {
					var datosGuardar = scope.fn.preparaDatosGuardar();
					if (datosGuardar !== undefined) {
						if (scope.$root.SesionLocale === "PE" && !validarCuentaCustodia()) {
							bcsComunes.ventanaMsg({
								mensaje: 'Error',
								conflictos: util.parseServerResponse({ "P_OUT_ERRORESDETALLE": [{ "Codigo": "UI_ERROR_CUENTA_CUSTODIO", "Descripcion": 'Si no Opera con Custodia debe seleccionar "Tipo Custodia Propia" o "Banco Custodio"' }] })
							}, scope.idVentana);
							return false;
						}

						const resultadoValidacion = validarSobregirosCustodia()
						if (resultadoValidacion.error) {
							bcsComunes.ventanaMsg({
								mensaje: 'Error',
								conflictos: util.parseServerResponse({ "P_OUT_ERRORESDETALLE": [{ "Codigo": "UI_ERROR_OPERA_CUSTODIA_SOBREGIRO", "Descripcion": resultadoValidacion.message }] })
							}, scope.idVentana);
							return false;
						}
						
						const resultadoValidacionDependenciaIndicador = validarIndicadorFacturacionPrecioPromedio();
						if (resultadoValidacionDependenciaIndicador.error) {
							bcsComunes.ventanaMsg({
								mensaje: 'Error',
								conflictos: util.parseServerResponse({ "P_OUT_ERRORESDETALLE": [{ "Codigo": "UI_ERROR_DEPENDENCIA_INDICADOR_PRECIO_PROMEDIO_RV", "Descripcion": resultadoValidacionDependenciaIndicador.message }] })
							}, scope.idVentana);
							return false;
						}

						bcsComunes.ventanaOverlay(scope.idVentana, true);
						modificarPreferenciaSP(datosGuardar).then(function(result) {
							bcsComunes.ventanaOverlay(scope.idVentana, false);
							var titulo = "";
							if (result.P_OUT_ERRORESCONTADOR != undefined && result.P_OUT_ERRORESCONTADOR == 0) {
								titulo = $translate.instant("UI_MODIFICAR_PREFERENCIAS");
								scope.$emit('CambiosOk', { source: "Preferencias" });
							}
							else {
								titulo = "ERROR" + $translate.instant("UI_ERROR_MODIFICAR_PREFERENCIAS");
							}

							bcsComunes.ventanaMsg({
								mensaje: titulo,
								conflictos: util.parseServerResponse(result)
							}, scope.idVentana);

							scope.win.cancelar();
							scope.fn.cargaDatosPantalla();
						}, function(error) {
							bcsComunes.msg("error", "ERROR", $translate.instant("UI_ERROR_MODIFICAR_PREFERENCIAS"));
							console.log(error);
							bcsComunes.ventanaOverlay(scope.idVentana, false);
						});
					}
					else {
						console.log("No hay datos");
					}
				};

				scope.win.cancelar = function() {
					scope.win.modoLectura = true;
					scope.datosDespues = _.clone(scope.datosAntes);
					scope.$emit('CambiosOk', { source: "Preferencias" });
				};


				/*********************************************
				 * DATASOURCES
				 *********************************************/


				/*********************************************
				 * COMBOBOX
				 *********************************************/

				scope.dsModalidadOrdenes = new bcs.DataSource({
					type: "odata",
					definition: {
						serverFiltering: true,
						transport: {
							read: {
								url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1549",
								dataType: "json"
							}
						},
						filter: { field: "CodigoTipoIndicador", operator: "eq", value: 'ORDMODALIDAD' },
						schema: {
							model: {
								fields: {

								}
							}
						}
					}
				});

				scope.cmbbx.oConfiguracionModoFacturacionventacorta = {
					filtros: {
						CodigoTipoIndicador: { field: "CodigoTipoIndicador", operator: "eq", value: 'FAC_FFVC' }
					}
				};

				scope.cmbbx.oConfiguracionOrdLibroOrdenes = {
					filtros: {
						CodigoTipoIndicador: { field: "CodigoTipoIndicador", operator: "eq", value: 'GEN_CODIFRS' }
					}
				};


				scope.cmbbx.oConfiguracionCobroCustodia = {
					filtros: {
						CodigoTipoIndicador: { field: "CodigoTipoIndicador", operator: "eq", value: 'COBROCUST' }
					}
				};
				scope.cmbbx.oConfiguracionModoFacturacion = {
					filtros: {
						CodigoTipoIndicador: { field: "CodigoTipoIndicador", operator: "eq", value: 'FAC_MODFAC' }
					}
				};

				scope.cmbbx.oConfiguracionTipoOrdenDefecto = {
					filtros: {
						CodigoTipoIndicador: { field: "CodigoTipoIndicador", operator: "eq", value: 'ORDDEFECTOESPORD' }
					}
				};

				scope.cmbbx.oConfiguracionPeriodicidad = {
					filtros: {
						CodigoTipoIndicador: { field: "CodigoTipoIndicador", operator: "eq", value: 'CAR_PERIOD' }
					}
				};

				scope.cmbbx.oConfiguracionEmail = {
					filtros: {
						IdPersona: { field: "IdPersona", operator: "eq", value: scope.idPersona || -1 }
					}
				};

				scope.cmbbx.oConfiguracionOrdEmailNotificacion = {
					filtros: {
						IdPersona: { field: "IdPersona", operator: "eq", value: scope.idPersona || -1 }
					}
				};

				scope.cmbbx.oConfiguracionOrdLibroOrdenes = {
					filtros: {
						IdEmpresa: { field: "IdEmpresa", operator: "eq", value: scope.$root.SesionIdEmpresa }
					}
				};


				scope.cmbbx.oConfiguracionSectorEconomico = {
					filtros: {
						CodigoTipoIndicador: { field: "CodigoTipoIndicador", operator: "eq", value: 'MES_SECECO' }
					}
				};
				scope.cmbbx.oConfiguracionApoderadoCliente = {
					filtros: {
						IdEmpresa: { "field": "IdEmpresa", "operator": "eq", "value": scope.$root.SesionIdEmpresa },
						IdPersonaOrigen: { "field": "IdPersonaOrigen", "operator": "eq", "value": scope.idPersona || -1 },
						Administracion: { "field": "IdTipoRelacion", "operator": "eq", "value": "APOFIR" },
						IdEstado: { "field": "IdEstado", "operator": "eq", "value": "ACT" }
					},
				};

				scope.cmbbx.oConfiguracionDireccionContrato = {
					filtros: {
						IdPersona: { field: "IdPersona", operator: "eq", value: scope.idPersona || -1 }
					}
				};

				scope.cmbbx.oConfiguracionDividendoOtraMoneda = {
					filtros: {
						CodigoTipoIndicador: { field: "CodigoTipoIndicador", operator: "eq", value: "DIV_DOM" }
					}
				};

				scope.getDireccionPredeterminada = function() {
					var DireccionFisicaPrederminado = new bcs.DataSource({
						type: "odata",
						definition: {

							transport: {
								read: {
									url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1650",
									dataType: "json"
								}
							},
							filter: { field: "IdPersona", operator: "eq", value: scope.idPersona || -1 },
							schema: {
								model: {
									fields: {
										IdDireccionFisica: { type: "number" },
										Predeterminado: { type: "string" }
									}
								}
							}
						}
					});
					return DireccionFisicaPrederminado;
				}
				/*********************************************
				 * GRILLAS
	
				/*********************************************
				 * FUNCIONES
				 *********************************************/


				scope.fn.cargaDatosPantalla = function() {
					if (scope.modeloCliente || (scope.tipoEntidad && scope.idEntidad)) {
						var parametrosSP = {
							p_IdEmpresa: scope.$root.SesionIdEmpresa,
							p_IdEntidad: scope.idEntidad || 0,
							p_IdTipoEntidad: scope.tipoEntidad || '',
							p_CodigoTipoIndicador: '',
							p_GrupoIndicador: '',
							p_ModeloCliente: scope.modeloCliente || '',
							p_IdIndicador: 0
						};
						bcsComunes.ventanaOverlay(scope.idVentana, true);
						consultaIndicadoresSP(parametrosSP).then(function(result) {
							bcsComunes.ventanaOverlay(scope.idVentana, false);
							if (result && result.P_OUT_INDICADORES) {
								$.each(result.P_OUT_INDICADORES, function(key, value) {
									scope.definicionIndicadores[value.GrupoIndicador + "//" + value.CodigoTipoIndicador] = value;

								});
								//sobregiro cuenta inversion
								scope.datosAntes['GRUPOGENERAL//GEN_SOBCTA'] = (scope.definicionIndicadores['GRUPOGENERAL//GEN_SOBCTA'].ValorIndicador === 'S') ? true : false;
								//sobregiro tesoreria
								if(scope.definicionIndicadores['GRUPOGENERAL//GEN_SOBTES']){
									scope.datosAntes['GRUPOGENERAL//GEN_SOBTES'] = (scope.definicionIndicadores['GRUPOGENERAL//GEN_SOBTES'].ValorIndicador === 'S') ? true : false;
								}
								//opera cuenta inversion
								scope.datosAntes['GRUPOGENERAL//GEN_OCCINV'] = (scope.definicionIndicadores['GRUPOGENERAL//GEN_OCCINV'].ValorIndicador === 'S') ? true : false;
								//opera con saldo negativo
								scope.datosAntes['GRUPOGENERAL//GEN_OCSNEG'] = (scope.definicionIndicadores['GRUPOGENERAL//GEN_OCSNEG'].ValorIndicador === 'S') ? true : false;
								//codigo ifrs
								scope.datosAntes['GRUPOGENERAL//GEN_CODIFRS'] = scope.definicionIndicadores['GRUPOGENERAL//GEN_CODIFRS'].ValorIndicador;
								//Usa Direccion Propia
								scope.datosAntes['GRUPOGENERAL//GEN_DIR_PROPIA'] = (scope.definicionIndicadores['GRUPOGENERAL//GEN_DIR_PROPIA'].ValorIndicador === 'S') ? true : false;
								//Cobro Custodia
								scope.datosAntes['GENERALES//COBROCUST'] = scope.definicionIndicadores['GENERALES//COBROCUST'].ValorIndicador;

								//Direccion Fisica Contratos Propio
								scope.datosAntes['GRUPOGENERAL//GEN_CODDIR'] = (scope.definicionIndicadores['GRUPOGENERAL//GEN_CODDIR'].ValorIndicador === '0') ? scope.IdDireccionPredeterminada : scope.definicionIndicadores['GRUPOGENERAL//GEN_CODDIR'].ValorIndicador;
								//Direccion Fisica Contratos Relacionado
								scope.datosAntes['GRUPOGENERAL//GEN_CODDIR_REL'] = (scope.definicionIndicadores['GRUPOGENERAL//GEN_CODDIR_REL'].ValorIndicador === '0') ? '1' : scope.definicionIndicadores['GRUPOGENERAL//GEN_CODDIR_REL'].ValorIndicador;
								//separa linea de factura por transaccion
								scope.datosAntes['GRUPOFACTURACION//FAC_SLFPTR'] = (scope.definicionIndicadores['GRUPOFACTURACION//FAC_SLFPTR'].ValorIndicador === 'S' && scope.definicionIndicadores['GRUPOFACTURACION//FAC_FLFPPR'].ValorIndicador === 'N') ? true : false;
								//separa linea por instrumento
								scope.datosAntes['GRUPOFACTURACION//FAC_SFPINS'] = (scope.definicionIndicadores['GRUPOFACTURACION//FAC_SFPINS'].ValorIndicador === 'S') ? true : false;
								//fusiona linea factura por precio
								scope.datosAntes['GRUPOFACTURACION//FAC_FLFPPR'] = (scope.definicionIndicadores['GRUPOFACTURACION//FAC_SLFPTR'].ValorIndicador === 'N' && scope.definicionIndicadores['GRUPOFACTURACION//FAC_FLFPPR'].ValorIndicador === 'S') ? true : false;
								//fusiona factura a precio promedio RV
								scope.datosAntes['GRUPOFACTURACION//FAC_FFPPRV'] =  (scope.definicionIndicadores['GRUPOFACTURACION//FAC_FFPPRV'].ValorIndicador === 'S') ? true : false;
								//modo facturacion
								scope.datosAntes['GRUPOFACTURACION//FAC_MODFAC'] = scope.definicionIndicadores['GRUPOFACTURACION//FAC_MODFAC'].ValorIndicador;
								//convenio derechos bcs
								scope.datosAntes['GRUPOFACTURACION//FAC_CDRBCS'] = (scope.definicionIndicadores['GRUPOFACTURACION//FAC_CDRBCS'].ValorIndicador === 'S') ? true : false;
								//convenio derechos bec
								scope.datosAntes['GRUPOFACTURACION//FAC_CDRBEC'] = (scope.definicionIndicadores['GRUPOFACTURACION//FAC_CDRBEC'].ValorIndicador === 'S') ? true : false;
								//codigo operador afp
								scope.datosAntes['GRUPOFACTURACION//FAC_COIAFP'] = scope.definicionIndicadores['GRUPOFACTURACION//FAC_COIAFP'].ValorIndicador;
								//envia factura por mail
								scope.datosAntes['GRUPOFACTURACION//FAC_ENVFEM'] = (scope.definicionIndicadores['GRUPOFACTURACION//FAC_ENVFEM'].ValorIndicador === 'S') ? true : false;
								//direccion mail factura
								scope.datosAntes['GRUPOFACTURACION//FAC_DIRELE'] = scope.definicionIndicadores['GRUPOFACTURACION//FAC_DIRELE'].ValorIndicador;
								//direccion fisica
								scope.datosAntes['GRUPOFACTURACION//FAC_DIRFIS'] = scope.definicionIndicadores['GRUPOFACTURACION//FAC_DIRFIS'].ValorIndicador;
								//Fusionar factura venta corta
								scope.datosAntes['GRUPOFACTURACION//FAC_FFVC'] = scope.definicionIndicadores['GRUPOFACTURACION//FAC_FFVC'].ValorIndicador;
								//netea ingresos y egresos
								scope.datosAntes['GRUPOTESORERIA//TES_NIE'] = (scope.definicionIndicadores['GRUPOTESORERIA//TES_NIE'].ValorIndicador === 'S') ? true : false;
								//opera custodia rv
								scope.datosAntes['GRUPOORDENES//ORDSOBREGIRORV'] = (scope.definicionIndicadores['GRUPOORDENES//ORDSOBREGIRORV'].ValorIndicador === 'S') ? true : false;
								//opera custodia rv
								scope.datosAntes['GRUPOORDENES//ORDSOBREGIRORF'] = (scope.definicionIndicadores['GRUPOORDENES//ORDSOBREGIRORF'].ValorIndicador === 'S') ? true : false;
								//opera sobregiro custodia if
								scope.datosAntes['GRUPOORDENES//ORDSOBREGIROIF'] = (scope.definicionIndicadores['GRUPOORDENES//ORDSOBREGIROIF'].ValorIndicador === 'S') ? true : false;
								//opera custodia rv
								scope.datosAntes['GRUPOORDENES//ORDOCUSTRV'] = (scope.definicionIndicadores['GRUPOORDENES//ORDOCUSTRV'].ValorIndicador === 'S') ? true : false;
								//opera custodia rf
								scope.datosAntes['GRUPOORDENES//ORDOCUSTRF'] = (scope.definicionIndicadores['GRUPOORDENES//ORDOCUSTRF'].ValorIndicador === 'S') ? true : false;
								//permite venta nominal rv
								scope.datosAntes['GRUPOORDENES//ORDVNNOMRV'] = (scope.definicionIndicadores['GRUPOORDENES//ORDVNNOMRV'].ValorIndicador === 'S') ? true : false;
								//permite venta descubierta
								scope.datosAntes['GRUPOORDENES//ORDVENDES'] = (scope.definicionIndicadores['GRUPOORDENES//ORDVENDES'].ValorIndicador === 'S') ? true : false;
								//permite venta nominal rf
								scope.datosAntes['GRUPOORDENES//ORDVNNOMRF'] = (scope.definicionIndicadores['GRUPOORDENES//ORDVNNOMRF'].ValorIndicador === 'S') ? true : false;
								//permite rutear en optimus cliente
								scope.datosAntes['GRUPOORDENES//ORDCLIRUR'] = (scope.definicionIndicadores['GRUPOORDENES//ORDCLIRUR'].ValorIndicador === 'S') ? true : false;
								//notificacion ordenes
								scope.datosAntes['GRUPOORDENES//ORDMAILNTF'] = scope.definicionIndicadores['GRUPOORDENES//ORDMAILNTF'].ValorIndicador;
								//libro de ordenes
								scope.datosAntes['GRUPOORDENES//ORDLIBRO'] = scope.definicionIndicadores['GRUPOORDENES//ORDLIBRO'].ValorIndicador;
								//libro de ordenes ruteo agente
								scope.datosAntes['GRUPOORDENES//ORDLIBROAGENTE'] = scope.definicionIndicadores['GRUPOORDENES//ORDLIBROAGENTE'].ValorIndicador;
								//libro de ordenes ruteo retail
								scope.datosAntes['GRUPOORDENES//ORDLIBRORETAIL'] = scope.definicionIndicadores['GRUPOORDENES//ORDLIBRORETAIL'].ValorIndicador;

								//PERUPERUPERU
								if (scope.$root.SesionLocale === 'PE') {
									scope.datosAntes['GRUPOORDENES//ORDDEFECTOESPORD'] = scope.definicionIndicadores['GRUPOORDENES//ORDDEFECTOESPORD'].ValorIndicador;
									scope.datosAntes['GRUPOORDENES//ORDCUENTAPROPIA'] = (scope.definicionIndicadores['GRUPOORDENES//ORDCUENTAPROPIA'].ValorIndicador === 'S') ? true : false;
									scope.datosAntes['GRUPOORDENES//ORDCUENTABANCO'] = (scope.definicionIndicadores['GRUPOORDENES//ORDCUENTABANCO'].ValorIndicador === 'S') ? true : false;
									scope.datosAntes['GRUPOFACTURACION//FAC_EMPOEN'] = (scope.definicionIndicadores['GRUPOFACTURACION//FAC_EMPOEN'].ValorIndicador === 'S') ? true : false;
								}

								var obj = {};
								if (scope.definicionIndicadores['GRUPOORDENES//ORDMODALIDAD'].ValorIndicador) {
									obj = JSON.parse(scope.definicionIndicadores['GRUPOORDENES//ORDMODALIDAD'].ValorIndicador);
									//obj = {};
								}
								scope.datosAntes['GRUPOORDENES//ORDMODALIDAD'] = obj;
								scope.dsModalidadOrdenes.fetch().then(function() {
									var d = scope.dsModalidadOrdenes.data();
									scope.valoresModalidadOrdenes = {};
									$.each(d, function(key, value) {
										scope.valoresModalidadOrdenes[value.Codigo] = value.DetalleIndicador;
									})
								});
								//shortname
								scope.datosAntes['GRUPOORDENES//GEN_SHORTNAME'] = scope.definicionIndicadores['GRUPOORDENES//GEN_SHORTNAME'].ValorIndicador;
								//permite asignar od a mismo rut
								scope.datosAntes['GRUPOORDENES//PER_ASIGNAR_MISMORUT_OEOD'] = (scope.definicionIndicadores['GRUPOORDENES//PER_ASIGNAR_MISMORUT_OEOD'].ValorIndicador === 'S') ? true : false;
								//operacion requiere autorizacion
								scope.datosAntes['GRUPOFORWARD//FRWDINGORA'] = (scope.definicionIndicadores['GRUPOFORWARD//FRWDINGORA'].ValorIndicador === 'S') ? true : false;
								//Primer Apoderado Firma Contrato
								scope.datosAntes['GRUPOFORWARD//FWDAPO1'] = scope.definicionIndicadores['GRUPOFORWARD//FWDAPO1'].ValorIndicador;
								//Segundo Apoderado Firma Contrato
								scope.datosAntes['GRUPOFORWARD//FWDAPO2'] = scope.definicionIndicadores['GRUPOFORWARD//FWDAPO2'].ValorIndicador;
								//envia resumen operaciones diarias
								scope.datosAntes['GRUPONOTIFICACIONES//NOT_ENVROP'] = (scope.definicionIndicadores['GRUPONOTIFICACIONES//NOT_ENVROP'].ValorIndicador === 'S') ? true : false;
								//mail default notificaciones
								scope.datosAntes['GRUPONOTIFICACIONES//NOT_EMAIL'] = scope.definicionIndicadores['GRUPONOTIFICACIONES//NOT_EMAIL'].ValorIndicador;

								//periodicidad
								scope.datosAntes['GRUPOCARTOLA//CAR_PERIOD'] = scope.definicionIndicadores['GRUPOCARTOLA//CAR_PERIOD'].ValorIndicador;
								//envia cartola por mail
								scope.datosAntes['GRUPOCARTOLA//CAR_ENVCEM'] = (scope.definicionIndicadores['GRUPOCARTOLA//CAR_ENVCEM'].ValorIndicador === 'S') ? true : false;
								//direccion electronica cartola
								scope.datosAntes['GRUPOCARTOLA//CAR_DIRELE'] = scope.definicionIndicadores['GRUPOCARTOLA//CAR_DIRELE'].ValorIndicador;
								//envia cartola por dirección física
								scope.datosAntes['GRUPOCARTOLA//CAR_ENVCDF'] = (scope.definicionIndicadores['GRUPOCARTOLA//CAR_ENVCDF'].ValorIndicador === 'S') ? true : false;
								//direccion fisica cartola
								scope.datosAntes['GRUPOCARTOLA//CAR_DIRFIS'] = scope.definicionIndicadores['GRUPOCARTOLA//CAR_DIRFIS'].ValorIndicador;

								//money market automatico
								scope.datosAntes['GRUPOFFMM//FFMM_MMAUT'] = (scope.definicionIndicadores['GRUPOFFMM//FFMM_MMAUT'].ValorIndicador === 'S') ? true : false;

								//opera custodia rf if
								scope.datosAntes['GRUPOMESA//MES_CUSTRF'] = (scope.definicionIndicadores['GRUPOMESA//MES_CUSTRF'].ValorIndicador === 'S') ? true : false;
								//codigo sector economico
								scope.datosAntes['GRUPOMESA//MES_SECECO'] = scope.definicionIndicadores['GRUPOMESA//MES_SECECO'].ValorIndicador;
								//codigo otc
								scope.datosAntes['GRUPOMESA//MES_CODOTC'] = scope.definicionIndicadores['GRUPOMESA//MES_CODOTC'].ValorIndicador;
								//codigo datatec
								scope.datosAntes['GRUPOMESA//MES_DATTEC'] = scope.definicionIndicadores['GRUPOMESA//MES_DATTEC'].ValorIndicador;
								//Primer Apoderado Firma Adendum
								scope.datosAntes['GRUPOMESA//MES_APO1'] = scope.definicionIndicadores['GRUPOMESA//MES_APO1'].ValorIndicador;
								//Primer Apoderado Firma Adendum
								scope.datosAntes['GRUPOMESA//MES_APO2'] = scope.definicionIndicadores['GRUPOMESA//MES_APO2'].ValorIndicador;
								//Firma Conjunta Adendum
								scope.datosAntes['GRUPOMESA//MES_FIRMA_CONJUNTA'] = (scope.definicionIndicadores['GRUPOMESA//MES_FIRMA_CONJUNTA'].ValorIndicador === 'S') ? true : false;
								// Dividendos otras monedas
								scope.datosAntes['GRUPODIVIDENDOS//DIV_DOM'] = scope.definicionIndicadores['GRUPODIVIDENDOS//DIV_DOM'].ValorIndicador;
							}
							scope.datosDespues = _.clone(scope.datosAntes);
							//scope.win.directivaCargada = true;
						}, function(result) {
							bcsComunes.ventanaMsg({
								mensaje: '',
								conflictos: util.parseServerResponse(respuesta)
							}, scope.idVentana);
							bcsComunes.ventanaOverlay(scope.idVentana, false);
						});

					}
				};

				scope.fn.direcionPredeterminada = function() {
					var direccionPredeterminada = new bcs.DataSource({
						type: "odata",
						definition: {
							serverFiltering: true,
							transport: {
								read: {
									url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1650",
									dataType: "json"
								}
							},
							filter: { field: "IdPersona", operator: "eq", value: scope.idPersona || -1 },
							schema: {
								model: {
									fields: {
										IdDireccionFisica: { type: "string" },
										Predeterminado: { type: "string" }
									}
								}
							}
						}
					});
					direccionPredeterminada.fetch().then(function() {
						$.each(direccionPredeterminada.data(), function(key, value) {
							if (value.IdPersona === scope.idPersona && value.Predeterminado === 'UI_SI') {
								scope.IdDireccionPredeterminada = value.IdDireccionFisica;
							}
						});
					});
				}

				scope.fn.consultarPropositoPortafolio = function() {
					var propositoPortafolio = new bcs.DataSource({
						type: "odata",
						definition: {
							serverFiltering: true,
							transport: {
								read: {
									url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3518",
									dataType: "json"
								}
							},
							filter: { field: "IdPortafolio", operator: "eq", value: scope.idEntidad },
							schema: {
								model: {
									fields: {
										IdDireccionFisica: { type: "string" },
										Predeterminado: { type: "string" }
									}
								}
							}
						}
					});
					propositoPortafolio.fetch().then(function() {
						$.each(propositoPortafolio.data(), function(key, value) {
							scope.propositoPortafolio = value.Proposito;
						});
					});
				}

				var consultaIndicadoresSP = function(p_datos) {
					var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=275";
					var r = $resource("", {}, { 'urlConsulta': { method: "POST", url: urlConsulta } });
					var post = r.urlConsulta(p_datos);

					return post.$promise;
				};


				scope.fn.preparaDatosGuardar = function() {

					var datosGuardar = {};
					datosGuardar.p_IdEmpresa = scope.$root.SesionIdEmpresa;
					datosGuardar.p_IdUsuario = scope.$root.SesionIdUsuario;
					datosGuardar.p_IdModulo = 'PLA';
					datosGuardar.p_UsaReglasNegocio = 'N';
					datosGuardar.p_IdTipoEntidad = scope.tipoEntidad || '';
					datosGuardar.p_IdEntidad = scope.idEntidad || 0;

					datosGuardar.p_RegistroAtributo = [];

					$.each(scope.datosDespues, function(key) {
						var indicador = scope.definicionIndicadores[key];
						if (indicador) {
							IdIndicadorzz = indicador.IdIndicador;
							IdTipoIndicadorzz = indicador.IdTipoIndicador;

							if (indicador.ClaseDato === "CHECKBOX") {
								ValorIndicadorzz = (scope.datosDespues[indicador.GrupoIndicador + "//" + indicador.CodigoTipoIndicador]) ? 'S' : 'N';
							}
							else if (indicador.ClaseDato === "COMBO") {
								ValorIndicadorzz = (scope.datosDespues[indicador.GrupoIndicador + "//" + indicador.CodigoTipoIndicador]) ? scope.datosDespues[indicador.GrupoIndicador + "//" + indicador.CodigoTipoIndicador] : null;
							}
							else if (indicador.ClaseDato === "MULTI") {
								ValorIndicadorzz = (scope.datosDespues[indicador.GrupoIndicador + "//" + indicador.CodigoTipoIndicador]) ? JSON.stringify(scope.datosDespues[indicador.GrupoIndicador + "//" + indicador.CodigoTipoIndicador]) : null;
							}
							else {
								ValorIndicadorzz = scope.datosDespues[indicador.GrupoIndicador + "//" + indicador.CodigoTipoIndicador];
							}

							datosGuardar.p_RegistroAtributo.push({ IdIndicador: IdIndicadorzz, IdTipoIndicador: IdTipoIndicadorzz, ValorIndicador: ValorIndicadorzz });
						}
					});

					datosGuardar.p_ModeloCliente = scope.modeloCliente;

					return datosGuardar;
				};

				var modificarPreferenciaSP = function(p_datos) {
					var urlModificarPreferencia = "/BSCL/Plataforma/GWJAVA/PLA/call?t=280";
					var r = $resource("", {}, { 'urlModificarPreferencia': { method: "POST", url: urlModificarPreferencia } });
					var post = r.urlModificarPreferencia(p_datos);

					return post.$promise;
				};


				scope.win.focusOutEmail = function() {
					if ((scope.datosDespues['GRUPOFACTURACION//FAC_DIRELE'] !== undefined && scope.datosDespues['GRUPOFACTURACION//FAC_DIRELE'] !== '' && region.ValidaFormatoEmail(scope.datosDespues['GRUPOFACTURACION//FAC_DIRELE']) == false) ||
						(scope.datosDespues['GRUPOCARTOLA//CAR_DIRELE'] !== undefined && scope.datosDespues['GRUPOCARTOLA//CAR_DIRELE'] !== '' && region.ValidaFormatoEmail(scope.datosDespues['GRUPOCARTOLA//CAR_DIRELE']) == false)
					) {
						bcsComunes.msg("warning", "warning", $translate.instant("UI_FORMATO_EMAIL_INCORRECTO"));
						scope.win.validForm = false;
						return;
					}
					else scope.win.validForm = true;
				}

				/*********************************************
				 * FUNCIONES WATCH
				 *********************************************/
				scope.$watch('modeloCliente', function(newval, oldval) {
					if (newval !== undefined && newval !== oldval) {
						scope.fn.cargaDatosPantalla();
						scope.win.modoLectura = true;
					}
					else {
						//scope.win.directivaCargada = false;
					}
				});

				// El campo Separa linea factura por trans y campo Fusiona linea factura por precio NO pueden ir juntos
				scope.$watch("datosDespues['GRUPOFACTURACION//FAC_SLFPTR']", function(newValue, oldValue) {
					if (scope.datosDespues['GRUPOFACTURACION//FAC_SLFPTR']) {
						scope.datosDespues['GRUPOFACTURACION//FAC_FLFPPR'] = false;
					}
				});
				scope.$watch("datosDespues['GRUPOFACTURACION//FAC_FLFPPR']", function(newValue, oldValue) {
					if (scope.datosDespues['GRUPOFACTURACION//FAC_FLFPPR']) {
						scope.datosDespues['GRUPOFACTURACION//FAC_SLFPTR'] = false;
					}
				});

				//El campo Fusiona factura a precio promedio RV debe ir siempre junto a separa factura por instrumento
				scope.$watch("datosDespues['GRUPOFACTURACION//FAC_FFPPRV']", function(newValue, oldValue) {
					if (scope.datosDespues['GRUPOFACTURACION//FAC_FFPPRV']) {
						scope.datosDespues['GRUPOFACTURACION//FAC_SFPINS'] = true;
						scope.win.disableSFPINS = true;
					}else{
						scope.win.disableSFPINS = false;
					}
				});
				
				// El campo Neteo ingresos y egresos en tesorería SOLO puede ser seleccionado si Opera contra cta inversion NO es marcado
				scope.$watchGroup(["datosDespues['GRUPOGENERAL//GEN_OCCINV']", "datosDespues['GRUPOTESORERIA//TES_NIE']"], function(newValue, oldValue) {
					if (scope.datosDespues['GRUPOGENERAL//GEN_OCCINV']) {
						scope.datosDespues['GRUPOTESORERIA//TES_NIE'] = false;
						scope.win.disableNeteaIngEgrTes = true;
					}
					else { scope.win.disableNeteaIngEgrTes = false; }
				});

				// El campo "Opera con saldo Negativo" solo puede habilitarse si "Opera con Cta Inversion" esta Marcado.
				scope.$watch("datosDespues['GRUPOGENERAL//GEN_OCCINV']", function(newValue, oldValue) {
					if (scope.datosDespues['GRUPOGENERAL//GEN_OCCINV']) {
						scope.win.disableOpCSN = false;
					}
					else { scope.win.disableOpCSN = true; }
				});

				if (scope.$root.SesionLocale === "PE") {
					//solo para perú, no permitir activar sobregiro cuenta inversion, si no tiene configurado opera contra cuenta inversion.
					scope.$watch("datosDespues['GRUPOGENERAL//GEN_SOBCTA']", function(newValue, oldValue) {
						if (newValue && !scope.datosDespues['GRUPOGENERAL//GEN_OCCINV']) {
							scope.datosDespues['GRUPOGENERAL//GEN_SOBCTA'] = false;
						}
						console.log(newValue);
						console.log(scope.datosDespues['GRUPOGENERAL//GEN_OCCINV']);
						//falta evaluar el caso que opera contra cuenta inversion se desactive.
					});

					scope.$watch("datosDespues['GRUPOGENERAL//GEN_OCCINV']", function(newValue, oldValue) {
						if (!newValue) {
							scope.datosDespues['GRUPOGENERAL//GEN_SOBCTA'] = false;
						}
					});

					scope.$watch("datosDespues['GRUPOORDENES//ORDOCUSTRV']", function(nval, oval) {
						if (nval) {
							scope.datosDespues['GRUPOORDENES//ORDOCUSTRF'] = true;
							scope.datosDespues['GRUPOORDENES//ORDCUENTAPROPIA'] = false;
							scope.datosDespues['GRUPOORDENES//ORDCUENTABANCO'] = false;
						} else {
							scope.datosDespues['GRUPOORDENES//ORDOCUSTRF'] = false;
						}
					});
					scope.$watch("datosDespues['GRUPOORDENES//ORDOCUSTRF']", function(nval, oval) {
						if (nval) {
							scope.datosDespues['GRUPOORDENES//ORDOCUSTRV'] = true;
							scope.datosDespues['GRUPOORDENES//ORDCUENTAPROPIA'] = false;
							scope.datosDespues['GRUPOORDENES//ORDCUENTABANCO'] = false;
						} else {
							scope.datosDespues['GRUPOORDENES//ORDOCUSTRV'] = false;
						}
					});

					scope.$watch("datosDespues['GRUPOORDENES//ORDCUENTAPROPIA']", function(nval, oval) {
						if (nval) {
							scope.datosDespues['GRUPOORDENES//ORDCUENTABANCO'] = false;
							scope.datosDespues['GRUPOORDENES//ORDOCUSTRV'] = false;
							scope.datosDespues['GRUPOORDENES//ORDOCUSTRF'] = false;
						}
					});
					scope.$watch("datosDespues['GRUPOORDENES//ORDCUENTABANCO']", function(nval, oval) {
						if (nval) {
							scope.datosDespues['GRUPOORDENES//ORDCUENTAPROPIA'] = false;
							scope.datosDespues['GRUPOORDENES//ORDOCUSTRV'] = false;
							scope.datosDespues['GRUPOORDENES//ORDOCUSTRF'] = false;
						}
					});

				}

				scope.$watch('scope.codigoFormulario', function() {
					if (scope.codigoFormulario) {
						bcsServicioPlataforma.CamposRequeridosFormulario(scope.codigoFormulario, function(respuesta) {
							scope.camposRequeridos = respuesta;
						});
					}
				});

				
				scope.$watchCollection('datosDespues', function (nval,oval) {
					const modoEdicion = !scope.win.modoLectura;
					if (modoEdicion) scope.$emit('CambiosPendientes', { source: "Preferencias" });
				});
				


				/*********************************************
				 * GO!!!
				 **********************************************/
				if ((scope.modeloCliente > 0 && scope.idEntidad > 0) || (scope.modeloCliente === 0 && scope.idEntidad === 0)) {
					throw "directiva mal cargada";
				}
				else {
					scope.fn.consultarPropositoPortafolio();
					scope.fn.direcionPredeterminada();
					scope.win.validForm = true;
					scope.fn.cargaDatosPantalla();
				}
			}
		}
	});