(function () {
	angular.module('ModuloPlataforma').controller(
			'ModalLimiteCuentaPropiaRFIFPlazoVencimientoCtrl',
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
					bcsComunes.ventanaTitulo(self.idVentana, $translate.instant('UI_MODIFICAR_LIMITE_PLAZO_VENCIMIENTO'));
					console.log({limiteOriginal})
					self.limite.IdTipoInstrumento = limiteOriginal.IdTipoInstrumento || 0;
					self.limite.IdTipoInstrumentoDef = limiteOriginal.IdTipoInstrumento || 0;
					self.limite.IdCanalIngreso = limiteOriginal.IdCanalIngreso || 0;
					self.limite.IdCanalIngresoDef = limiteOriginal.IdCanalIngreso || 0;
					self.limite.IdMoneda = limiteOriginal.IdMoneda;
					self.limite.IdMonedaDef = limiteOriginal.IdMoneda;
					self.limite.PlazoMinimoDias = limiteOriginal.Plazo;
					self.limite.IdEstado = limiteOriginal.IdEstadoConfigurable || $scope.$root.SesionValoresDefectoSistema.IdEstadoLimiteCuentaPropia;
					self.limite.IdEstadoDef = limiteOriginal.IdEstadoConfigurable;
					self.limite.IdTipoComportamiento = limiteOriginal.IdTipoComportamiento;
					self.limite.IdTipoComportamientoDef = limiteOriginal.IdTipoComportamiento;
					self.win.modoEditar = true;
				} else {
					bcsComunes.ventanaTitulo(self.idVentana, $translate.instant('UI_AGREGAR_LIMITE_PLAZO_VENCIMIENTO'));
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

					if (limiteOriginal && limiteOriginal.IdLimite) { // EDITAR
						datosGuardar.p_IdLimite = limiteOriginal.IdLimite;
						datosGuardar.p_RegistroCambio = [];
						datosGuardar.p_RegistroCambio.push({ CampoModificado: 'IdTipoInstrumento', ValorNuevo: String(self.limite.TipoInstrumentoObj.IdTipoInstrumento || '0') });
						datosGuardar.p_RegistroCambio.push({ CampoModificado: 'IdCanalIngreso', ValorNuevo: (self.limite.IdCanalIngreso || '0').trim() });
						datosGuardar.p_RegistroCambio.push({ CampoModificado: 'CodigoOperador', ValorNuevo: (self.limite.CodigoOperador || '').trim() });
						datosGuardar.p_RegistroCambio.push({ CampoModificado: 'IdMoneda', ValorNuevo: (self.limite.IdMoneda || '0').trim() });
						datosGuardar.p_RegistroCambio.push({ CampoModificado: 'UnidadPlazo', ValorNuevo: 'D' });
						datosGuardar.p_RegistroCambio.push({ CampoModificado: 'Plazo', ValorNuevo: String(self.limite.PlazoMinimoDias) || '0' });
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
							self.evaluaRespuestaSP(result, 'UI_LIMITE_PLAZO_VENCIMIENTO_MODIFICADO');
						}, function (resError) {
							bcsComunes.msg("error", "LIMITES CUENTA PROPIA", $translate.instant("UI_REVISE_LOG"));
							console.log(resError);
							bcsComunes.ventanaOverlay(self.idVentana, false);
						});
					} else { // CREAR
						datosGuardar.p_IdTipoLimite = self.dataDirectiva.TipoLimite;
						datosGuardar.p_IdClasificacionTipoInstrumento = self.dataDirectiva.Clasificacion;

						datosGuardar.p_IdTipoInstrumento = self.limite.IdTipoInstrumento;
						datosGuardar.p_IdCanalIngreso = (self.limite.IdCanalIngreso || '0').trim();
						datosGuardar.p_IdMoneda = (self.limite.IdMoneda || '0').trim();
						datosGuardar.p_IdEstado = self.limite.IdEstado;
						datosGuardar.p_IdTipoComportamiento = self.limite.IdTipoComportamiento;

						datosGuardar.p_UnidadPlazo = 'D';
						datosGuardar.p_Plazo = Number(self.limite.PlazoMinimoDias);

						bcsComunes.ventanaOverlay(self.idVentana, true);
						servicioLimites.crearLimiteCtaPropia(datosGuardar, function (result) {
							self.evaluaRespuestaSP(result, 'UI_LIMITE_PLAZO_VENCIMIENTO_CREADO');
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

				// self.cmbbx.TipoOperacion = {
				// 	filtros: {
				// 		EstadoTipoOperacionTipoInstrumento: { field: "EstadoTipoOperacionTipoInstrumento", operator: 'eq', value: 'ACT' },
				// 		IdClasificacionTipoInstrumento: { field: "IdClasificacionTipoInstrumento", operator: 'eq', value: '-1' },
				// 		IdTipoInstrumento: { field: "IdTipoInstrumento", operator: 'eq', value: '-1' },
				// 	}
				// }

				self.cmbbx.TipoCanalIngreso = {
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
						IdTipoInstrumento: { field: 'IdTipoInstrumento', operator: 'eq', value: '-1' },
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