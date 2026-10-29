angular.module('ModuloPlataforma').directive('bcsNotificacion', function ($translate, bcsComunes, bcsServicioPlataforma, $resource) {
	return {
		restrict: 'E',
		replace: false,
		scope: {
			kOptions: '=?',
			kNgModel: '=?',
			bcsHabilitado: '=?',
			idEntidad: "=?",
			tipoEntidad: "=?",
			direccionEvento: "=?"
				/* activa/desactiva  el boton  */
				// AGREGAR VARIABLES SUELTAS, COMO FILTRO, ETC
		},
		templateUrl: 'directives/bcs-notificacion/bcs-notificacion.html',
		link: function (scope, elem, attrs) {
			scope.postData = {};
			scope.camposAtr = [];
			scope.valores = {};
			scope.suspensionTemporal = false;
			var idVentana = bcsComunes.ventanaWinId(scope);
			var tabSeleccionado = "";
			if (typeof scope.bcsHabilitado === 'undefined') {
				scope.bcsHabilitado = false;
			}
			var dataSourceConfiguracionEventoPortafolio = new bcs.DataSource({
				type: "odata",
				definition: {
					serverFiltering: true,
					serverPaging: true,
					serverSorting: true,
					sort: {
						field: "IdConfiguracionEvento",
						dir: "desc"
					},
					pageSize: 50,
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1464",
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								IdConfiguracionEvento: {
									type: "number"
								},
								FechaCreacion: {
									type: "date"
								},
								Descripcion: {
									type: "code"
								},
								IdEstado: {
									type: "string"
								},
								EstadoConfiguracionPortafolio: {
									type: "string"
								},
								IdEntidad: {
									type: "number"
								},
								TipoEntidad:{
									type: "entidad"
								},
								FechaInicioSuspension: {
									type: "date"
								},
								FechaTerminoSuspension: {
									type: "date"
								}
							}
						}
					},
					filter: [
						{field: "IdEntidad", operator: "eq", value: scope.idEntidad},
						{field: "TipoEntidad", operator: "eq", value: scope.tipoEntidad},
					],
				}
			});
			var dataSourceConfiguracionEvento = new bcs.DataSource({
				type: "odata",
				definition: {
					serverFiltering: true,
					serverPaging: true,
					serverSorting: true,
					sort: {
						field: "IdConfiguracionEvento",
						dir: "desc"
					},
					pageSize: 50,
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1376",
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								IdConfiguracionEvento: {
									type: "number"
								},
								FechaCreacion: {
									type: "date"
								},
								Descripcion: {
									type: "code"
								},
								IdEstado: {
									type: "string"
								}
							}
						}
					},
					filter: [
						{field: "DirEvt", operator: "eq", value: scope.direccionEvento},
					],
				}
			});
			scope.getDataPortafolio = function () {
				dataSourceConfiguracionEventoPortafolio.fetch().then(function () {
					var campo = {};
					$.each(dataSourceConfiguracionEventoPortafolio.data(), function (llave, code) {
						if(scope.campos[code.IdConfiguracionEvento]){
							scope.campos[code.IdConfiguracionEvento].valor = (code.EstadoConfiguracionPortafolio==="ACT")? true: false;
						}						
						if(dataSourceConfiguracionEventoPortafolio.data().length-1 == llave){
							if (code.SuspensionTemporal == 'N') {
								scope.suspensionTemporal = false;
							} else {
								scope.suspensionTemporal = true;
							}
							scope.suspensionTemporalOriginal = scope.suspensionTemporal;
							scope.FechaDesdeSuspension = code.FechaInicioSuspension;
							scope.FechaDesdeSuspensionOriginal = code.FechaInicioSuspension;
							scope.FechaHastaSuspension = code.FechaTerminoSuspension;
							scope.FechaHastaSuspensionOriginal = code.FechaTerminoSuspension;
							//respaldar configuracion inicial
							scope.camposOriginal = angular.copy(scope.campos);
						}
					});
					bcsComunes.ventanaOverlay(idVentana, false);
				});
			}
			scope.getData = function () {
				bcsComunes.ventanaOverlay(idVentana, true);
				dataSourceConfiguracionEvento.fetch().then(function () {
					if(dataSourceConfiguracionEvento.data().length>0){
						$.each(dataSourceConfiguracionEvento.data(), function (key, codigos) {
							var campo = {};
							campo.IdConfiguracionEvento = codigos.IdConfiguracionEvento;
							campo.Descripcion = codigos.Descripcion;
							campo.valor = true;
							if (codigos.PermitePersonalizar == 'UI_NO') {
								campo.PermitePersonalizar = false;
							} else {
								campo.PermitePersonalizar = true;
							}
							scope.campos[codigos.IdConfiguracionEvento] = campo;
							if(dataSourceConfiguracionEvento.data().length-1 == key){
								//respaldar configuracion por defecto
								scope.camposOriginal = angular.copy(scope.campos);
								scope.getDataPortafolio();
							}
						});
					}else{
						bcsComunes.ventanaOverlay(idVentana, false);
					}
				});
			}
			scope.campos = {};
			scope.camposOriginal = {};
			scope.getData();
			scope.modificarDatos = function () {
				//if (scope.verificarCambios(scope.campos, scope.camposOriginal)===true) {
					bcsComunes.ventanaOverlay(idVentana, true);
					var camposMod = [];
					var obj = {};
					$.each(scope.campos, function (key, codigos) {
						obj.IdConfiguracionEvento = codigos.IdConfiguracionEvento;
						obj.IdEstado = (codigos.valor) ? 'ACT' : 'INA';
						obj.FechaVencimiento = new Date('0001-01-01');
						camposMod.push(obj);
						obj = {};
					});
					// llamar servicio modificar
					llamarServicioModificarPreferenciasNotificacion(camposMod);
//				}
//				else{
//					bcsComunes.ventanaMsg({ mensaje : "No hay datos para guardar",
//						conflictos : null}, idVentana);
//				}
					;
			};
			scope.clicEditar = function () {
				scope.bcsHabilitado = true;
			};
			scope.verificarCambios = function(original, nuevo){
				var i =0;
				var length = Object.keys(original).length;
				for (key in original){
					if(original[key].valor != nuevo[original[key].IdConfiguracionEvento].valor){
						return true;
					}
				}
				return false;
			};
			scope.clicCancelar = function () {
				scope.bcsHabilitado = false;
				$.each(scope.campos, function (key, codigos) {
					codigos.valor = scope.camposOriginal[codigos.IdConfiguracionEvento].valor;
				});
				scope.suspensionTemporal = scope.suspensionTemporalOriginal;
				scope.FechaDesdeSuspension = scope.FechaDesdeSuspensionOriginal;
				scope.FechaHastaSuspension = scope.FechaHastaSuspensionOriginal;
				scope.$emit('CambiosOk', { source: "Notificaciones" });
			};
			scope.onTabSelect = function (tabName) {
				tabSeleccionado = tabName;
			}
			llamarServicioModificarPreferenciasNotificacion = function (camposMod) {
				var EventoModificar = [];
				//Parámetros de entrada al servicio
				var datosModificar = {};
				var fec = [];
				fec = scope.FechaDesde.split("-");
				var fecDesde = fec[1] + "-" + fec[0] + "-" + fec[2];
				var fecHasta = [];
				fecHasta = scope.FechaHasta.split("-");
				var fecHasta = fecHasta[1] + "-" + fecHasta[0] + "-" + fecHasta[2];
				var fechaInicioSus;
				var fechaTerminoSus;
				if (scope.suspensionTemporal) {
					if (scope.FechaDesde.trim().length > 0 && scope.FechaHasta.trim().length > 0) {
						fechaInicioSus = util.dateSPseconddate(new Date(fecDesde));
						fechaTerminoSus = util.dateSPseconddate(new Date(fecHasta));
					} else {
						bcsComunes.msg("error", "Error", 'Debe seleccionar fecha inicio y termino');
						fechaInicioSus = util.dateSPseconddate(new Date('01-01-0001'));
						fechaTerminoSus = util.dateSPseconddate(new Date('01-01-0001'));
						bcsComunes.ventanaOverlay(idVentana, false);
						scope.clicCancelar();
						return;
					}
				} else {
					fechaInicioSus = util.dateSPseconddate(new Date('01-01-0001'));
					fechaTerminoSus = util.dateSPseconddate(new Date('01-01-0001'));
				}
				datosModificar.p_IdEmpresa = scope.$root.SesionIdEmpresa;
				datosModificar.p_IdUsuario = scope.$root.SesionIdUsuario;
				datosModificar.p_IdModulo = 'PLA';
				datosModificar.p_UsaReglasNegocio = 'N';
				datosModificar.p_IdEntidad = scope.idEntidad;
				datosModificar.p_TipoEntidad = scope.tipoEntidad;
				datosModificar.p_DireccionEvento = scope.direccionEvento;
				datosModificar.SuspensionTemporal = scope.suspensionTemporal ? 'S' : 'N';
				datosModificar.FechaInicioSuspension = fechaInicioSus;
				datosModificar.FechaTerminoSuspension = fechaTerminoSus;
				datosModificar.p_RegistroAtributo = camposMod;
				
				var successModificar = function (respuesta) {
					bcsComunes.ventanaMsg({
						mensaje: respuesta.P_SALIDA,
						conflictos: util.parseServerResponse(respuesta)
					}, idVentana);
					scope.camposOriginal = angular.copy(scope.campos);	
					scope.bcsHabilitado = false;
					scope.$emit('CambiosOk', { source: "Notificaciones" });
					bcsComunes.ventanaOverlay(idVentana, false);
				};
				var errorModificar = function (respuesta) {
					bcsComunes.ventanaMsg({
						mensaje: "La operacion tiene errores",
						conflictos: util.parseServerResponse(respuesta)
					}, idVentana);
					bcsComunes.ventanaOverlay(idVentana, false);
					scope.clicCancelar();
				};
				bcsServicioPlataforma.ModificarPreferenciasNotificacion(datosModificar, successModificar, errorModificar);
			};

			scope.avisarCambioPanel = function (data,dataOriginal) {
				const sonIguales = _.isEqual(data, dataOriginal);
				if (sonIguales) {
					scope.$emit('CambiosOk', { source: "Notificaciones" });
				} else {
					scope.$emit('CambiosPendientes', { source: "Notificaciones" });
				}
			};

			scope.$watch('scope.campos', function () {
				bcsComunes.ventanaAutoHeight(idVentana)
				if( scope.bcsHabilitado) scope.$emit('CambiosPendientes', { source: "Notificaciones" });
			}, true);
			scope.$watchGroup(['suspensionTemporal','FechaDesdeSuspension','FechaHastaSuspension'], function () {
				if (scope.bcsHabilitado) {
					scope.$emit('CambiosPendientes', { source: "Notificaciones" });
				}
			}, true);

		}
	};
});