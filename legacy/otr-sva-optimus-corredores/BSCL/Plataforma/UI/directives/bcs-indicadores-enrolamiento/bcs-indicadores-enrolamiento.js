angular
	.module('ModuloPlataforma')
	.directive(
		'bcsIndicadoresEnrolamiento',
		function($translate, bcsComunes, bcsServicioPlataforma,
			$resource) {

			return {
				restrict: 'E',
				replace: false,
				scope: {
					kOptions: '=?',
					kNgModel: '=?',
					idEntidad: '=?',
					idTipoEntidad: '=?',
					idTipoPersona: '=?',
					modeloCliente: '=?',
					idUso: '=?',
					bcsHabilitado: '=?', /* activa/desactiva  el boton  */
					codigoFormulario: '=?',
					idIndicador: '=?'
					// AGREGAR VARIABLES SUELTAS, COMO FILTRO, ETC
				},
				templateUrl: 'directives/bcs-indicadores-enrolamiento/bcs-indicadores-enrolamiento.html',
				link: function(scope, elem, attrs) {

					scope.postData = {};
					scope.camposAtr = [];
					scope.filtroCombobox = {};
					scope.tipoPersona = [];
					scope.grupoAtr = [];
					scope.camposRequeridos = {};
					scope.forms = {};

					var ventanaId = bcsComunes.ventanaWinId(scope);
					var tabSeleccionado = "";

					if (typeof scope.bcsHabilitado === 'undefined') { scope.bcsHabilitado = false; }
					if (typeof scope.idUso === 'undefined') { scope.idUso = 'IND'; }
					if (typeof scope.idTipoPersona === 'undefined') {
						scope.tipoPersona.push('N');
						scope.tipoPersona.push('J');
					}
					else {
						scope.tipoPersona.push(scope.idTipoPersona);
					}

					var consultaCamposAtributos = function(v_Dato) {
						var urlConsulta = '';
						if (scope.idUso === 'IND')
							urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=274";
						else if (scope.idUso === 'REQ')
							urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=277";
						else if (scope.idUso === 'PRE')
							urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=275";
						var r = $resource
							("", {}, {
								'urlConsulta': { method: "POST", url: urlConsulta }
							}
							);
						var post = r.urlConsulta(v_Dato);
						return post.$promise;
					};

					/////////////////////////////////////


					var consultaRelacionadosPEP =
					{
						type: "odata",
						definition: {
							serverFiltering: true, // Filtrado de datos en el servidor.
							transport: {
								read: {
									url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=601194",
									dataType: "json"
								}
							},
							schema: {
								model: {
									fields: {
										"DocumentoIdentidad": { type: "string" },
										"CodigoTipoIndicador": { type: "string" },
										"ValorIndicador": { type: "string" },
										"IdPersonaOrigen": { type: "integer" }
									}
								}
							}
						}
					};

					var consultaIndicadoresBase =
					{
						type: "odata",
						definition: {
							serverFiltering: true,
							transport: {
								read: {
									url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700255",
									dataType: "json"
								}
							},
							schema: {
								model: {
									fields: {
										"IdEnrolamiento": { type: "integer" },
										"IdTipoCliente": { type: "string" },
										"IdTipoEntidad": { type: "string" },
										"NombreTipoIndicador": { type: "string" },
										"IdUso": { type: "string" },
										"IdTipoIndicador": { type: "integer" },
										"CodigoTipoIndicador": { type: "string" },
										"ClaseDato": { type: "string" },
										"CantidadDecimales": { type: "integer" },
										"LongitudMinima": { type: "integer" },
										"LongitudMaxima": { type: "integer" },
										"IdTipoPersona": { type: "string" },
										"ModeloCliente": { type: "string" },
										"ValorIndicador": { type: "string" },
										"CodigoValorIndicador": { type: "string" },
										"NombreValorIndicador": { type: "string" },
										"IdIndicador": { type: "integer" },
										"IdEmpresa": { type: "integer" }
									}
								}
							}
						}
					};

					var filtros = [{ field: "IdPersonaOrigen", operator: "eq", value: scope.idEntidad }];


					var IndicadorPep = "";
					var consultarPEP = new bcs.DataSource(consultaRelacionadosPEP);
					consultarPEP.query({ filter: filtros }).then(function() {
						if (consultarPEP.view().length > 0) {
							IndicadorPep = "S";

						}
						else {
							IndicadorPep = "N";
						}
					});
					scope.IndicadorCheckPep = 'N';
					///////////////////////////////////
					scope.grupos = {};
					
					getAtributos = function() {
						var filtros = [{ field: "IdUso", operator: "eq", value: scope.idUso },
						{ field: "IdTipoEntidad", operator: "eq", value: scope.idTipoEntidad },
						{ field: "IdEnrolamiento", operator: "eq", value: scope.idEntidad }];
						var consultarIndicadores = new bcs.DataSource(consultaIndicadoresBase);
						scope.grupoAtr = [];
						consultarIndicadores.query({ filter: filtros }).then(function() {
							if (consultarIndicadores.view().length > 0) {
								scope.camposAtrOriginal = [];
								scope.camposAtr = [];
								scope.ubicacionIndicadores = { "PEP": [] };
								for (var i = 0; i < consultarIndicadores.view().length; i++) {
									var campo = {};
									campo.IdIndicador = consultarIndicadores.view()[i].IdIndicador;
									campo.IdTipoIndicador = consultarIndicadores.view()[i].IdTipoIndicador;
									campo.NombreTipoIndicador = consultarIndicadores.view()[i].NombreTipoIndicador;
									campo.ClaseDato = consultarIndicadores.view()[i].ClaseDato;
									campo.IdEstadoAutorizacion = consultarIndicadores.view()[i].IdEstadoAutorizacion;
									campo.GrupoIndicador = consultarIndicadores.view()[i].GrupoIndicador;
									campo.IdTipoPersona = consultarIndicadores.view()[i].IdTipoPersona;
									campo.RequiereFechaVencimiento = consultarIndicadores.view()[i].RequiereFechaVencimiento;
									campo.FechaVencimiento = consultarIndicadores.view()[i].FechaVencimiento;
									campo.RelacionDocumento = consultarIndicadores.view()[i].RelacionDocumento || '';
									campo.CodigoTipoIndicador = consultarIndicadores.view()[i].CodigoTipoIndicador;
									campo.IdClase = consultarIndicadores.view()[i].IdClase;
									campo.Tipo = consultarIndicadores.view()[i].Tipo;

									if (consultarIndicadores.view()[i].IdClase === 2 || consultarIndicadores.view()[i].IdClase === 3) {
										campo.Habilitado = false;
									} else {
										campo.Habilitado = true;//Usado para des/habilitar campos con dependencias	
									}

									var tipoPersonaIndicador = consultarIndicadores.view()[i].IdTipoPersona.split('');
									var inters = _.intersection(scope.tipoPersona, tipoPersonaIndicador);
									if (inters.length > 0 || consultarIndicadores.view()[i].IdTipoPersona === "" || consultarIndicadores.view()[i].IdTipoPersona === "A") {
										campo.IndicadorVisible = true;
									} else {
										campo.IndicadorVisible = false;
									}

									if (campo.ClaseDato.toUpperCase() == 'BIGINT')
										campo.ValorIndicador = parseInt(consultarIndicadores.view()[i].ValorIndicador);
									else if (campo.ClaseDato.toUpperCase() == 'CHECKBOX') {
										if (consultarIndicadores.view()[i].ValorIndicador == "S") {
											consultarIndicadores.view()[i].ValorIndicador = "S";
											campo.ValorIndicador = consultarIndicadores.view()[i].ValorIndicador ? true : true;
										}

									} else {
										campo.ValorIndicador = consultarIndicadores.view()[i].ValorIndicador;
									}
									if (campo.ClaseDato == 'COMBO') {
										scope.filtroCombobox[campo.IdTipoIndicador] = [];
										scope.filtroCombobox[campo.IdTipoIndicador].push({ field: "IdTipoIndicador", operator: "eq", value: parseInt(campo.IdTipoIndicador) });
										campo.ValorIndicadorDef = consultarIndicadores.view()[i].ValorIndicador;
									}
									if (campo.GrupoIndicador === "PEP" && campo.CodigoTipoIndicador != "PEP") {
										if (scope.ubicacionIndicadores[campo.GrupoIndicador]) {
											scope.ubicacionIndicadores[campo.GrupoIndicador].push(i);
										}
									}
									scope.camposAtr.push(campo);
								}
								scope.camposAtrOriginal = angular.copy(scope.camposAtr);
								for (var i = 0; i < consultarIndicadores.view().length; i++) {
									scope.grupos[consultarIndicadores.view()[i].GrupoIndicador] = consultarIndicadores.view()[i].GrupoIndicador;
								}
								$.each(scope.grupos, function(key, val) {
									var campo = {};
									campo.GrupoIndicador = val;
									scope.grupoAtr.push(campo);
								});	
							}
						});
					};

					scope.$watch('modeloCliente',
						function handleAutorizado(newValue, oldValue) {
							if (newValue !== false) {
								getAtributos();
							}
						}
					);
					scope.$watch('camposAtr',
						function handleChange(newValue, oldValue) {
							for (var i = 0; i < newValue.length; i++) {
								if (newValue[i].CodigoTipoIndicador === "PEP") {
									$.each(scope.ubicacionIndicadores[newValue[i].CodigoTipoIndicador], function(key, val) {
										scope.camposAtr[val].Habilitado = newValue[i].ValorIndicador;
									});
								}
							}
						}, true
					);

					scope.modificarDatos = function() {
						if (scope.camposAtr !== scope.camposAtrOriginal) {
							bcsComunes.ventanaOverlay(ventanaId, true);
							var camposMod = [];
							for (var i = 0; i < scope.camposAtr.length; i++) {
								if (scope.camposAtr[i].GrupoIndicador === tabSeleccionado) {
									var campoSel = {};
									//if(scope.idUso === 'IND')
									//campoSel.FechaVencimiento = new Date('0001-01-01');
									campoSel.IdEnrolamiento = scope.idEntidad;
									campoSel.IdIndicador = scope.camposAtr[i].IdIndicador;
									//campoSel.IdTipoIndicador = parseInt(scope.camposAtr[i].IdTipoIndicador);

									//scope.camposAtr[i].ValorIndicador = scope.camposAtr[i].ValorIndicador || '';

									if (scope.camposAtr[i].ClaseDato == 'BIGINT' || scope.camposAtr[i].ClaseDato == 'COMBO')
										campoSel.ValorIndicador = scope.camposAtr[i].ValorIndicador || '';
									else if (scope.camposAtr[i].ClaseDato == 'CHECKBOX') {
										campoSel.ValorIndicador = scope.camposAtr[i].ValorIndicador === true ? 'S' : 'N';
										if (scope.camposAtr[i].RequiereFechaVencimiento === 'S') {
											//if(scope.idUso === 'IND')
											//campoSel.FechaVencimiento = scope.camposAtr[i].FechaVencimiento; 
										}
									}
									else {
										campoSel.ValorIndicador = scope.camposAtr[i].ValorIndicador;
									}
									if (scope.camposAtr[i].ValorIndicador !== scope.camposAtrOriginal[i].ValorIndicador)
										camposMod.push(campoSel);

								}
							}
							console.log(camposMod);
							// llamar servicio modificar
							llamarServicioModificarAtributo(camposMod);
						}
						;
					};

					scope.clicEditar = function() {
						scope.bcsHabilitado = true;
					};
					scope.clicCancelar = function() {
						scope.bcsHabilitado = false;
						getAtributos();
					};
					scope.onTabSelect = function(tabName) {
						tabSeleccionado = tabName;
						scope.nombreFormActual = `grupo_${scope.grupoAtr.findIndex(u => u.GrupoIndicador === tabName)}`;
					}
					llamarServicioModificarAtributo = function(camposMod) {
						var EventoModificar = [];

						//Parámetros de entrada al servicio
						var datosModificar = {};

						datosModificar.p_IdEmpresa = scope.$root.SesionIdEmpresa;
						datosModificar.p_IdUsuario = scope.$root.SesionIdUsuario;
						datosModificar.p_IdModulo = 'PLA';
						datosModificar.p_UsaReglasNegocio = 'N';
						datosModificar.p_CodigoFormulario = scope.codigoFormulario;
						datosModificar.p_IdTipoCanal = 'OPT';
						//datosModificar.p_IdTipoEntidad		= scope.idTipoEntidad;
						//datosModificar.p_IdEntidad			= scope.idEntidad || '0';
						//datosModificar.p_ModeloCliente		= scope.modeloCliente;
						datosModificar.p_Indicadores = camposMod;


						console.log(datosModificar);

						var successModificar = function(respuesta) {
							getAtributos();
							scope.bcsHabilitado = false;
							bcsComunes.ventanaMsg({
								mensaje: "Atributos Modificados",
								conflictos: util.parseServerResponse(respuesta)
							}, ventanaId);
							bcsComunes.ventanaOverlay(ventanaId, true);
						};
						var errorModificar = function(respuesta) {
							console.log("Error:" + JSON.stringify(respuesta));
							bcsComunes.msg("error", "Error Interno", JSON.stringify(respuesta));
							bcsComunes.ventanaOverlay(ventanaId, false);
							scope.clicCancelar();
						};
						//
						bcsServicioPlataforma.ModificarAtributoEnrolamiento(datosModificar, successModificar, errorModificar);
					};

					var parseServerResponse = function(response) {
						var result = { errores: [], advertencias: [] };
						$.each(response.P_OUT_ADVERTENCIASDETALLE, function(index, value) {
							;
							result.adv.push(value);
						});
						$.each(response.P_OUT_ERRORESDETALLE, function(index, value) {
							;
							result.errores.push(value);
						});
						return result;
					}

					scope.$watch('scope.codigoFormulario', function() {
						if (scope.codigoFormulario) {
							bcsServicioPlataforma.CamposRequeridosFormulario(scope.codigoFormulario, function(respuesta) {
								scope.camposRequeridos = respuesta;
							});
						}
					});

					scope.$watch('bcsHabilitado', function () {
						if (!scope.bcsHabilitado) {
							scope.$emit('CambiosOk', { source: scope.idIndicador });
						} else {
							scope.$emit('CambiosPendientes', { source: scope.idIndicador });
						}
					});
				}
			};
		});
