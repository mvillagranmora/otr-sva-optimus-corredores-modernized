angular
	.module('ModuloPlataforma')
	.directive(
		'bcsIndicadores',
		function($translate, bcsComunes, bcsServicioPlataforma,
			$resource, $rootScope) {

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
					bcsAccionEditar: '=?'
					// AGREGAR VARIABLES SUELTAS, COMO FILTRO, ETC
				},
				templateUrl: 'directives/bcs-indicadores/bcs-indicadores.html',
				link: function(scope, elem, attrs) {
					
					
					scope.postData = {};
					scope.camposAtr = [];
					scope.filtroCombobox = {};
					scope.tipoPersona = [];
					scope.grupoAtr = [];
					scope.camposRequeridos = {};
					scope.forms = {};
					scope.grupoAtrActual = {}

					var ventanaId = bcsComunes.ventanaWinId(scope);
					var tabSeleccionado = "";
					
					let perfilValido = new bcs.DataSource({
						type: "odata",
						definition : {
							serverFiltering : true, // Filtrado de datos en el servidor.
							serverPaging : true, // Paginado de los datos en el servidor.
							serverSorting : true, // Ordenamiento de los datos en el servidor.
							sort : [{ field: "IdEstadoPerfilAcceso", dir: "DESC"}], // Criterios de Ordenamiento.
							pageSize : 5, // Número de registros por página.
							transport: {
								read: {
									url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=4290670",
									dataType: "json"
							    }
							},
							schema : {
								model: {
									fields : {
										IdUsuario: {type: "number"},
										CodigoUsuario: {type: "string"},
										IdPerfil: {type: "number"},
										IdElementoHtml: {type: "string"},
										IdEstadoPerfilAcceso: {type: "string"}
									}
								}
							},
							filter : 
									  [ { field: "IdUsuario", operator: "eq", value: $rootScope.SesionIdUsuario},
										{ field: "IdElementoHtml", operator: "eq", value: "TabIndicadores" }
										]
							}
						});
					
					perfilValido.query().then(function(){
						if (perfilValido.data().length >= 1){
							scope.bcsAccionEditar = true;
						} else {
							scope.bcsAccionEditar = false;
						}
					});
					
					if (typeof scope.bcsHabilitado === 'undefined') {
						scope.bcsHabilitado = false;
					}
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





					getAtributos = function() {
						var parametrosSP = {};
						parametrosSP.p_IdEmpresa = scope.$root.SesionIdEmpresa;
						parametrosSP.p_IdEntidad = scope.idEntidad || 0;
						parametrosSP.p_IdTipoEntidad = scope.idTipoEntidad;
						parametrosSP.p_ModeloCliente = scope.modeloCliente || '';
						scope.grupoAtr = [];


						consultaCamposAtributos(parametrosSP).then(function(result) {
							scope.camposAtrOriginal = [];
							scope.camposAtr = [];
							
							if (result.P_OUT_ERRORESDETALLE != undefined && result.P_OUT_ERRORESDETALLE.length > 0) {
								bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_INDICADORES_ERROR"),conflictos:util.parseServerResponse(result)},scope.winID);
							}
							
							scope.ubicacionIndicadores = { "PEP": [] };//usado para identificar dependencias
							//var SumadorPEP = 0;
							for (var i = 0; i < result.P_OUT_INDICADORESCONTADOR; i++) {
								var campo = {};
								campo.IdIndicador = result.P_OUT_INDICADORES[i].IdIndicador;
								campo.IdTipoIndicador = result.P_OUT_INDICADORES[i].IdTipoIndicador;
								campo.NombreTipoIndicador = result.P_OUT_INDICADORES[i].NombreTipoIndicador;
								campo.ClaseDato = result.P_OUT_INDICADORES[i].ClaseDato;
								campo.IdEstadoAutorizacion = result.P_OUT_INDICADORES[i].IdEstadoAutorizacion;
								campo.GrupoIndicador = result.P_OUT_INDICADORES[i].GrupoIndicador;
								campo.IdTipoPersona = result.P_OUT_INDICADORES[i].IdTipoPersona;
								campo.RequiereFechaVencimiento = result.P_OUT_INDICADORES[i].RequiereFechaVencimiento;
								campo.FechaVencimiento = result.P_OUT_INDICADORES[i].FechaVencimiento;
								campo.RelacionDocumento = result.P_OUT_INDICADORES[i].RelacionDocumento || '';
								campo.CodigoTipoIndicador = result.P_OUT_INDICADORES[i].CodigoTipoIndicador;
								campo.IdClase = result.P_OUT_INDICADORES[i].IdClase;
								campo.Tipo = result.P_OUT_INDICADORES[i].Tipo;
								campo.CantidadDecimales = result.P_OUT_INDICADORES[i].CantidadDecimales;

								if (result.P_OUT_INDICADORES[i].IdClase === 2 || result.P_OUT_INDICADORES[i].IdClase === 3) {
									campo.Habilitado = false;
								} else {
									campo.Habilitado = true;//Usado para des/habilitar campos con dependencias	
								}

								var tipoPersonaIndicador = result.P_OUT_INDICADORES[i].IdTipoPersona.split('');
								var inters = _.intersection(scope.tipoPersona, tipoPersonaIndicador);
								if (inters.length > 0 || result.P_OUT_INDICADORES[i].IdTipoPersona === "" || result.P_OUT_INDICADORES[i].IdTipoPersona === "A") {
									campo.IndicadorVisible = true;
								} else {
									campo.IndicadorVisible = false;
								}

								if (campo.ClaseDato.toUpperCase() == 'BIGINT'){
									campo.ValorIndicador = parseInt(result.P_OUT_INDICADORES[i].ValorIndicador);
								}
								else if(campo.ClaseDato.toUpperCase() == 'PORCENTAJE'){
									campo.ValorIndicador = parseFloat(result.P_OUT_INDICADORES[i].ValorIndicador);
								}
								else if (campo.ClaseDato.toUpperCase() == 'CHECKBOX') {									
										campo.ValorIndicador = result.P_OUT_INDICADORES[i].ValorIndicador === "S" ;
								}
								else
									campo.ValorIndicador = result.P_OUT_INDICADORES[i].ValorIndicador;

								if (campo.ClaseDato == 'COMBO') {
									scope.filtroCombobox[campo.IdTipoIndicador] = [];
									scope.filtroCombobox[campo.IdTipoIndicador].push({ field: "IdTipoIndicador", operator: "eq", value: parseInt(campo.IdTipoIndicador) });
									campo.ValorIndicadorDef = result.P_OUT_INDICADORES[i].ValorIndicador;
								}
								if (campo.GrupoIndicador === "PEP" && campo.CodigoTipoIndicador != "PEP") {
									if (scope.ubicacionIndicadores[campo.GrupoIndicador]) {
										scope.ubicacionIndicadores[campo.GrupoIndicador].push(i);
									}
								}
								scope.camposAtr.push(campo);
							}

							for (var i = 0; i < result.P_OUT_GRUPOINDICADORESCONTADOR; i++) {
								var campo = {};
								campo.GrupoIndicador = result.P_OUT_GRUPOINDICADORES[i].GrupoIndicador;
								scope.grupoAtr.push(campo);
							}
							scope.camposAtrOriginal = angular.copy(scope.camposAtr);
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
									if (scope.idUso === 'IND')
										campoSel.FechaVencimiento = new Date('0001-01-01');

									campoSel.IdIndicador = scope.camposAtr[i].IdIndicador;
									campoSel.IdTipoIndicador = parseInt(scope.camposAtr[i].IdTipoIndicador);

									//scope.camposAtr[i].ValorIndicador = scope.camposAtr[i].ValorIndicador || '';

									if (scope.camposAtr[i].ClaseDato == 'BIGINT' || scope.camposAtr[i].ClaseDato == 'COMBO' || scope.camposAtr[i].ClaseDato == 'PORCENTAJE')
										campoSel.ValorIndicador = scope.camposAtr[i].ValorIndicador || '';
									else if (scope.camposAtr[i].ClaseDato == 'CHECKBOX') {
										campoSel.ValorIndicador = scope.camposAtr[i].ValorIndicador === true ? 'S' : 'N';
										if (scope.camposAtr[i].RequiereFechaVencimiento === 'S') {
											if (scope.idUso === 'IND')
												campoSel.FechaVencimiento = scope.camposAtr[i].FechaVencimiento;
										}
									}
									else
										campoSel.ValorIndicador = scope.camposAtr[i].ValorIndicador;
									//											
									if (scope.camposAtr[i].ValorIndicador !== scope.camposAtrOriginal[i].ValorIndicador)
										camposMod.push(campoSel);
								}
							}
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
						scope.avisarCambios('CambiosOk')
						getAtributos();
					};
					scope.onTabSelect = function(tabName) {
						tabSeleccionado = tabName;
						let index = scope.grupoAtr.findIndex(u => u.GrupoIndicador === tabName);
						scope.grupoAtrActual = scope.grupoAtr[index];
						scope.nombreFormActual = `grupo_${index}`;
					}
					llamarServicioModificarAtributo = function(camposMod) {
						var EventoModificar = [];

						//Parámetros de entrada al servicio
						var datosModificar = {};

						datosModificar.p_IdEmpresa = scope.$root.SesionIdEmpresa;
						datosModificar.p_IdUsuario = scope.$root.SesionIdUsuario;
						datosModificar.p_IdModulo = 'PLA';
						datosModificar.p_UsaReglasNegocio = 'N';
						datosModificar.p_IdTipoEntidad = scope.idTipoEntidad;
						datosModificar.p_IdEntidad = scope.idEntidad || '0';
						datosModificar.p_ModeloCliente = scope.modeloCliente;
						datosModificar.p_RegistroAtributo = camposMod;

						var successModificar = function(respuesta) {
							getAtributos();
							scope.bcsHabilitado = false;
							bcsComunes.ventanaMsg({
								mensaje: "Atributos Modificados",
								conflictos: util.parseServerResponse(respuesta)
							}, ventanaId);
							scope.avisarCambios('CambiosOk');
							bcsComunes.ventanaOverlay(ventanaId, false);
						};
						var errorModificar = function(respuesta) {
							console.log("Error:" + JSON.stringify(respuesta));
							bcsComunes.msg("error", "Error Interno", JSON.stringify(respuesta));
							bcsComunes.ventanaOverlay(ventanaId, false);
							scope.clicCancelar();
						};
						//Llamada a servicio para crear comprobante de facturación()
						if (scope.idUso === 'IND')
							bcsServicioPlataforma.ModificarAtributo(datosModificar, successModificar, errorModificar);
						else if (scope.idUso === 'REQ')
							bcsServicioPlataforma.ModificarRequisito(datosModificar, successModificar, errorModificar);
						else bcsComunes.ventanaOverlay(ventanaId, true);

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

					scope.avisarCambios = function (evento) {
						switch (scope.idUso) {
								case 'REQ':
									scope.$emit(evento, { source: "Requisitos" });
									break;
								case 'IND':
									scope.$emit(evento, { source: "Indicadores" });
									break;
								case 'PRE':
									scope.$emit(evento, { source: "Preferencias" });
									break;
							}
					}

					scope.$watch('camposAtr', function (nval, oval) {
						if (scope.bcsHabilitado) {
							scope.avisarCambios('CambiosPendientes');
						}
					},true);
				}
			};
		});
