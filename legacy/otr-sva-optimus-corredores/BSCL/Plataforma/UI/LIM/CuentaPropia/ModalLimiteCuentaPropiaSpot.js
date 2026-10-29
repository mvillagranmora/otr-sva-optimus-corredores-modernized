(function () {
	angular.module('ModuloPlataforma').controller(
			'ModalLimiteCuentaPropiaSpotCtrl',
			function ($scope, bcsComunes, servicioLimites, bcsConfiguracionLimite, $translate) {

				/*********************************************
				 * VARIABLES Y CONTEXTOS
				 ********************************************/
				const self = this;
				self.idVentana = bcsComunes.ventanaWinId($scope);
				self.parametros = bcsComunes.ventanaParams(self.idVentana);
				self.domVentana = $("[ventanaid='" + self.idVentana + "']");

				const limiteOriginal = _.clone(self.parametros.LimiteSeleccionado);
				self.dataDirectiva = _.clone(self.parametros.DataDirectiva);

				self.limite = {};
				self.limite.IdEstadoDef = $scope.$root.SesionValoresDefectoSistema.IdEstadoLimiteCuentaPropia;
				
				self.win = {};
				self.win.MontoDecimal = (util.bcs_formato(0, 'ORD', 'MONTO', $scope.$root.SesionIdMonedaSistema, '', '')).cantidadDecimales;
				
				self.cmbbx = {};

				self.respuesta = {};
				
				bcsConfiguracionLimite.obtenerTipoValidacionPorDefectoPromise($scope.SesionIdEmpresa)
						.then(function (idTipoValidacionPorDefecto) {
							if (!limiteOriginal) {
								self.limite.IdTipoComportamiento = idTipoValidacionPorDefecto;
								self.limite.IdTipoComportamientoDef = idTipoValidacionPorDefecto;
							}
						});

				if (limiteOriginal) {
					bcsComunes.ventanaTitulo(self.idVentana, $translate.instant('UI_MODIFICAR_LIMITE_SPOT'));
					
					self.limite.IdTipoInstrumento = limiteOriginal.IdTipoInstrumento || 0;
					self.limite.IdTipoInstrumentoDef = limiteOriginal.IdTipoInstrumento || 0;
					self.limite.IdInstrumento = limiteOriginal.TipoInstrumento || 0;
					self.limite.IdInstrumentoDef = limiteOriginal.TipoInstrumento || 0;
					self.limite.IdCanalIngreso = limiteOriginal.IdCanalIngreso || 0;
					self.limite.IdCanalIngresoDef = limiteOriginal.IdCanalIngreso || 0;
					self.limite.CodigoOperador = limiteOriginal.CodigoOperador === 'TODOS'? undefined :  limiteOriginal.CodigoOperador;
					self.limite.CodigoOperadorDef =  limiteOriginal.CodigoOperador === 'TODOS'? undefined :  limiteOriginal.CodigoOperador;
					self.limite.IdMonedaPrincipal = limiteOriginal.IdMoneda;
					self.limite.IdMonedaPrincipalDef = limiteOriginal.IdMoneda;
					self.limite.MontoIntraday = limiteOriginal.MontoIntraday;
					self.limite.MontoOvernight = limiteOriginal.MontoOvernight;
					self.limite.IdEstado = limiteOriginal.IdEstadoConfigurable || $scope.$root.SesionValoresDefectoSistema.IdEstadoLimiteCuentaPropia;
					self.limite.IdEstadoDef = limiteOriginal.IdEstadoConfigurable;
					self.limite.IdTipoComportamiento = limiteOriginal.IdTipoComportamiento;
					self.limite.IdTipoComportamientoDef = limiteOriginal.IdTipoComportamiento;
					self.win.modoEditar = true;
				} else {
					bcsComunes.ventanaTitulo(self.idVentana, $translate.instant('UI_AGREGAR_LIMITE_SPOT'));
					self.limite.IdInstrumento = 0;
					self.limite.IdInstrumentoDef = 0;
					self.limite.IdTipoOperacion = 0;
					self.limite.IdTipoOperacionDef = 0;
					self.limite.IdTipoInstrumento = 'SPT';
					self.limite.IdTipoInstrumentoDef = 'SPT';
					self.win.modoEditar = false;
				}

				/*********************************************
				 * ACCIONES DE PANTALLA
				 ********************************************/
				self.cancelar = function cancelar() {
					bcsComunes.ventanaData(self.idVentana, self.respuesta);
					bcsComunes.ventanaCerrar(self.idVentana);
				};


				self.aceptar = function aceptar() {
					const datosGuardar = {};
					datosGuardar.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
					datosGuardar.p_IdUsuario = $scope.$root.SesionIdUsuario;
					datosGuardar.p_IdModulo = "PLA";
					datosGuardar.p_UsaReglasNegocio = "N";
					const codigoTodos = ' TODOS';
					const idInstrumentoStr = self.limite.InstrumentoObj.IdInstrumento || 0;
					const idCanalIngreso = bcsConfiguracionLimite.mapIf(self.limite.IdCanalIngreso, codigoTodos, '0').trim();
					
					if (limiteOriginal && limiteOriginal.IdLimite) { // EDITAR
						datosGuardar.p_IdLimite = limiteOriginal.IdLimite;
						datosGuardar.p_RegistroCambio = [];
						datosGuardar.p_RegistroCambio.push({ CampoModificado: 'IdInstrumento', ValorNuevo: idInstrumentoStr });
						datosGuardar.p_RegistroCambio.push({ CampoModificado: 'IdCanalIngreso', ValorNuevo: idCanalIngreso });
						datosGuardar.p_RegistroCambio.push({ CampoModificado: 'CodigoOperador', ValorNuevo: (self.limite.CodigoOperador || '').trim() });
						datosGuardar.p_RegistroCambio.push({ CampoModificado: 'IdMoneda', ValorNuevo: (self.limite.IdMonedaPrincipal || '0').trim() });
						datosGuardar.p_RegistroCambio.push({ CampoModificado: 'MontoIntraday', ValorNuevo: String(self.limite.MontoIntraday) || '0' });
						datosGuardar.p_RegistroCambio.push({ CampoModificado: 'MontoOvernight', ValorNuevo: String(self.limite.MontoOvernight) || '0' });
						datosGuardar.p_RegistroCambio.push({ CampoModificado: 'IdEstado', ValorNuevo: self.limite.IdEstado || '' });
						datosGuardar.p_RegistroCambio.push({ CampoModificado: 'IdTipoComportamiento', ValorNuevo: self.limite.IdTipoComportamiento || '' });

						if (datosGuardar.p_RegistroCambio.length === 0) {
							self.respuesta.aceptar = true;
							bcsComunes.ventanaData(self.idVentana, self.respuesta);
							bcsComunes.ventanaCerrar(self.idVentana);
							return;
						}

						bcsComunes.ventanaOverlay(self.idVentana, true);
						servicioLimites.modificarLimiteCtaPropia(datosGuardar, function (result) {
							self.evaluaRespuestaSP(result, 'UI_LIMITE_SPOT_MODIFICADO');
						}, function (resError) {
							bcsComunes.msg("error", "LIMITES CUENTA PROPIA", $translate.instant("UI_REVISE_LOG"));
							console.log(resError);
							bcsComunes.ventanaOverlay(self.idVentana, false);
						});
					} else { // CREAR
						
						datosGuardar.p_IdTipoLimite = self.dataDirectiva.TipoLimite;
						datosGuardar.p_IdClasificacionTipoInstrumento = self.dataDirectiva.Clasificacion;
						
						datosGuardar.p_IdInstrumento = idInstrumentoStr || 0;
						datosGuardar.p_IdTipoInstrumento = self.limite.IdTipoInstrumento;
						datosGuardar.p_IdCanalIngreso = idCanalIngreso;
						datosGuardar.p_CodigoOperador = (self.limite.CodigoOperador || '').trim();
						datosGuardar.p_IdMoneda = (self.limite.IdMonedaPrincipal || '0').trim();
						datosGuardar.p_MontoIntraday = Number(self.limite.MontoIntraday);
						datosGuardar.p_MontoOvernight = Number(self.limite.MontoOvernight);
						datosGuardar.p_IdEstado = self.limite.IdEstado;
						datosGuardar.p_IdTipoComportamiento = self.limite.IdTipoComportamiento;


						bcsComunes.ventanaOverlay(self.idVentana, true);
						servicioLimites.crearLimiteCtaPropia(datosGuardar, function (result) {
							self.evaluaRespuestaSP(result, 'UI_LIMITE_SPOT_CREADO');
						}, function (resError) {
							bcsComunes.msg("error", "LIMITES CUENTA PROPIA", $translate.instant("UI_REVISE_LOG"));
							console.log(resError);
							bcsComunes.ventanaOverlay(self.idVentana, false);
						});
					}
				};


				/*********************************************
				 * DATASOURCES
				 *********************************************/
				
				
				/*********************************************
				 * COMBOBOX
				 *********************************************/
				self.cmbbx.TipoInstrumento = {
					filtros: {
						IdEstado: { field: "IdEstado", operator: "eq", value: "VIG" },
						IdBolsa: { field: "IdBolsa", operator: "eq", value: $scope.$root.SesionBolsaDefectoSistema },
						IdClasificacionTipoInstrumento: { field: "IdClasificacionTipoInstrumento", operator: 'eq', value: self.dataDirectiva.Clasificacion }
					}
				};

				self.cmbbx.Instrumento = {
					staticItems: [{ value: bcsConfiguracionLimite.CodigoNoAplicaNumber, text: 'UI_NO_APLICA' }],
					filtros: {
						IdEstado: { field: "IdEstado", operator: "eq", value: "ACT" },
						IdTipoInstrumento: { field: "IdTipoInstrumento", operator: "eq", value: '-1' }
					}
				};

				self.cmbbx.TipoCanalIngreso = {
					staticItems: [{ value: bcsConfiguracionLimite.CodigoNoAplicaString, text: 'UI_NO_APLICA' }],
					filtros: {
						IdEstado: { field: "IdEstado", operator: "eq", value: "ACT" }
					}
				};

				self.cmbbx.Estado = {
					filtros: {
						NombreEntidad: { field: "toupper(NombreEntidad)", operator: "eq", value: "LIMITES" }
					}
				};

				self.cmbbx.Moneda = {
					filtros: {
						IdEstado: { field: "IdEstado", operator: "eq", value: "ACT" },
						IdTipoInstrumento: { field: "IdTipoInstrumento", operator: "eq", value: '-1' }
					}
				};

				self.cmbbx.Operador = {
					staticItems: [{ value: bcsConfiguracionLimite.CodigoNoAplicaString, text: 'UI_NO_APLICA' }],
					filtros: {
						IdEstado: { field: "IdEstado", operator: "eq", value: "ACT" },
						IdEmpresa: { field: 'IdEmpresa', operator: 'eq', value: $scope.SesionIdEmpresa },
						EsEnrolado: { field: 'EsEnrolado', operator: 'eq', value: 'S' }
					}
				};
				
				/*********************************************
				 * FUNCIONES
				 *********************************************/
				self.evaluaRespuestaSP = function evaluaRespuestaSP(result, msj) {
					if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID > 0 && result.P_OUT_ERRORESCONTADOR === 0 && result.P_OUT_ADVERTENCIASCONTADOR === 0) {
						bcsComunes.msg("info", "LIMITES CUENTA PROPIA", $translate.instant(msj));
					} else if (result.P_OUT_ADVERTENCIASCONTADOR !== undefined && result.P_OUT_ADVERTENCIASCONTADOR > 0) {
						bcsComunes.msg("warning", "LIMITES CUENTA PROPIA", result.P_OUT_ADVERTENCIASDETALLE[0].Codigo + ' : ' + result.P_OUT_ADVERTENCIASDETALLE[0].Descripcion);
					} else if (result.P_OUT_ERRORESCONTADOR !== undefined && result.P_OUT_ERRORESCONTADOR > 0) {
						bcsComunes.msg("error", "LIMITES CUENTA PROPIA", result.P_OUT_ERRORESDETALLE[0].Codigo + ' : ' + result.P_OUT_ERRORESDETALLE[0].Descripcion);
					} else {
						bcsComunes.msg("error", "LIMITES CUENTA PROPIA", $translate.instant("UI_REVISE_LOG"));
					}
					console.log(result);

					self.respuesta.aceptar = true;
					bcsComunes.ventanaOverlay(self.idVentana, false);
					bcsComunes.ventanaData(self.idVentana, self.respuesta);
					bcsComunes.ventanaCerrar(self.idVentana);
				};


				$scope.$watch('self.limite.IdMoneda', function (newValue, oldValue) {
					if (newValue !== oldValue) {
						self.win.MontoDecimal = util.bcs_formato(0, 'ORD', 'MONTO', self.limite.IdMoneda, '', '').cantidadDecimales;
					}
				});
			});
}());