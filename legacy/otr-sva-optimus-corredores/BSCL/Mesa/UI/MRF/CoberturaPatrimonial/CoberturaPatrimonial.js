(function () {
	angular.module('ModuloMesa').controller('CoberturaPatrimonial',
		function ($scope, $translate, $resource, $timeout, bcsAlerta, OPCIONES_BCSALERTA, bcsServiciosMesa, bcsComunes, bcsServicioPlataforma, $http) {
			const idVentana = bcsComunes.ventanaWinId($scope);
			$scope.data = {};
			$scope.data.informes = {};
			$scope.data.informes.fechaSistema = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");

			let datos = {
				p_fechas: [
					{ fechaInicial: $scope.SesionFechaSistema, numeroDias: 1, pais: $scope.SesionIdPaisSistema }
				],
				p_diaHabil: 'S',
				p_soloCaeDiaHabil: 'S',
				p_idPais: $scope.SesionIdPaisSistema
			};

			$scope.data.SesionIdEmpresa = $scope.SesionIdEmpresa;
			$scope.data.kngFechaHasta 	= util.dateInicioDia($scope.SesionFechaSistema);
			$scope.data.fechaValorizacionMin = {
				min: util.dateInicioDia($scope.SesionFechaSistema)
			};

			bcsServicioPlataforma.Servicios().ObtenerDiaSiguiente(datos, function (result) {
				if (result.P_OUT_ERRORESCONTADOR === 0) {
					$scope.data.kngValorizaAl = util.dateDS(result.p_out_fechas[0].fechaCalculada, "FechaSistema", "CALL");
				}
			}
				, function () {
					bcsComunes.msg("error", 'Error', $translate.instant('UI_ERROR_OBTENER_DIA_SIGUIENTE'));
					bcsComunes.ventanaOverlay($scope.winID, false);
				}
			);

			$scope.data.oConfiguracionMesaActiva = {
				filtros: {
					IdEstado: { "field": "IdEstadoRegistro", "operator": "eq", "value": "ACT" },
					IdTipoProducto: { "field": "IdTipoProducto", "operator": "eq", "value": "MERF" }
				}
			};

			const ConsultaTemplate = new bcs.DataSource({
				type: "odata",
				definition: {
					serverFiltering: true,
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3015",
							dataType: "json",
						}
					},
					schema: {
						model: {
							fields: {
								CompanyId: { type: "string" },
								Id: { type: "string" },
								ReportName: { type: "string" }
							}
						}
					}
				},
			});


			$scope.data.validarFechas = function(){ 
				if (util.datediff($scope.data.kngValorizaAl, $scope.data.kngFechaHasta)._data.days < 0) {
					bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('FECHA_CONSULTA_MAYOR_FECHA_VALORACIÓN'));
					return false;
				}
				return true
			}
		
			$scope.data.cambiarFechaMin = function(){ 
				let datos = {
					p_fechas  : [
									{ fechaInicial : util.dateSPdate($scope.data.kngFechaHasta), numeroDias : 1, pais : $scope.SesionIdPaisSistema }
								],
					p_diaHabil 			: 'S',
					p_soloCaeDiaHabil 	: 'S',
					p_idPais 			: $scope.SesionIdPaisSistema
				};
				bcsServicioPlataforma.Servicios().ObtenerDiaSiguiente(datos,function(result) {
						if (result.P_OUT_ERRORESCONTADOR === 0)
						{
							$scope.data.kngValorizaAl = util.dateDS(result.p_out_fechas[0].fechaCalculada,"FechaSistema","CALL");
						}				
					}
				,function(){
						bcsComunes.msg("error", 'Error', $translate.instant('UI_ERROR_OBTENER_DIA_SIGUIENTE'));
						bcsComunes.ventanaOverlay($scope.winID, false);
					}
				);
				$scope.data.fechaValorizacionMin = {
					min: util.dateInicioDia($scope.data.kngFechaHasta)
				};
		
			}

			$scope.obtenerReporte = function (dataTemplate) {

				let parameters = {
					"exportFormat": dataTemplate.TipoReporte,
					"reportId": dataTemplate.Id,
					"parameters": "",
					"configuration": {
						"mainReport": {
							"reportName": dataTemplate.ReportName,
							"data": dataTemplate.data,
							"parameters": {}
						},
						"subReports": []
					}
				};


				$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, { "responseType": "arraybuffer" })
					.success(function (response) {
						let file = new Blob([response], { type: 'application/pdf' });
						let nombreArchivo = dataTemplate.ReportName + '_' + $scope.SesionFechaSistema.replace(/-/g, '');
						saveAs(file, nombreArchivo + "." + dataTemplate.TipoReporte);
						excelReport();
						bcsComunes.ventanaOverlay(idVentana, false);
						RegistrarLog();
					})
					.error(function (response) {
						bcsComunes.msg("error", 'Error', $translate.instant('UI_ERROR_AL_GENERAR_REPORTE'));
						bcsComunes.ventanaOverlay(idVentana);
					});


				const excelReport = function () {
					let parameters = {
						"exportFormat": 'excel',
						"reportId": dataTemplate.Id,
						"parameters": "",
						"configuration": {
							"mainReport": {
								"reportName": dataTemplate.ReportName,
								"data": dataTemplate.data,
								"parameters": {}
							},
							"subReports": []
						}
					};

					$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, { "responseType": "arraybuffer" })
						.success(function (response) {
							let file = new Blob([response], { type: 'application/pdf' });
							let nombreArchivo = dataTemplate.ReportName + '_' + $scope.SesionFechaSistema.replace(/-/g, '');
							saveAs(file, nombreArchivo + "." + 'xls');
						})
						.error(function (response) {
							bcsComunes.msg("error", 'Error', $translate.instant('UI_ERROR_AL_GENERAR_REPORTE'));
							bcsComunes.ventanaOverlay(idVentana);
						});

				}
			};

			$scope.data.obtenerDataInforme = function (IdInforme) {
				const filterTemplate = [{ field: "ReportName", operator: "eq", value: 'COBERTURAPATRIMONIALCONTRATOPERSONA' }];
				ConsultaTemplate.query({ filter: filterTemplate }).then(function () {
					if (ConsultaTemplate.view().length > 0) {
						$scope.dataTemplate.TipoReporte = 'pdf';
						$scope.dataTemplate.Id = ConsultaTemplate.view()[0].Id;
						$scope.dataTemplate.ReportName = ConsultaTemplate.view()[0].ReportName;
						let EsHistorico = 'N';
						let fechaSistema = new Date($scope.SesionFechaSistema + "T00:00:00");
						if($scope.data.kngFechaHasta +"T00:00:00" < fechaSistema) {
							EsHistorico = 'S';
						}
						let dataSourceInforme = new bcs.DataSource({
							type: "call",
							definition: {
								callParameters: {
									p_keyInforme: IdInforme,
									p_IdCliente: $scope.data.oConfiguracionMesaActiva.seleccionado.IdCliente || 0,
									p_opcion: $scope.data.OpcionCoberturaSeleccionado.IdOpcionCobertura,
									p_TipoValor: $scope.data.ValorCoberturaSeleccionado.IdValorCobertura,
									p_NombreMesa: $scope.data.oConfiguracionMesaActiva.seleccionado.Nombre || 'TODOS',
									p_plazoDias: $scope.data.plazoCoberturaSeleccionado.IdDiasCobertura || 3,
									p_IdEmpresa: $scope.SesionIdEmpresa,
									p_FechaValorizacion: $scope.data.kngValorizaAl,
								},
								callTransaction: "101010154",
								callResultSet: "p_out_CoberturaPatrimonioDetalle",
								schema: {
									model: {
										fields: {
											
												CantidadDecimales: { type: "number" },
												FolioComprobante: { type: "number" },
												FolioCartera: { type: "number"},
												FolioOperacion: {type: "number"},
												IdTipoOperacion: { type: "string" },
												IdInstrumento: { type: "number" },
												IdTipoInstrumento: { type: "string" },
												TipoInstrumento: { type: "string" },
												ValorMercado: { type: "number" },
												TIR: { type: "number" },
												Cartera: { type: "string" },
												FechaVcto: { type: "string" },
												Cantidad: { type: "number" },
												Emisor: { type: "string" },
												Nemotecnico: { type: "string" },
												Corredor: { type: "string" },
												IdMonedaInstrumento: { type: "string" },
												CLA_RIE: { type: "string" },
												FechaVencimientoInstrumento: { type: "string" },
												NombreIndicador: { type: "string" },
												Plazo: { type: "number" },
												PlazoPacto: { type: "number" },
												TramoCobertura: { type: "number" },
												PorcentajeCobertura: { type: "number" },
												MontoCobertura: { type: "number" },
												Clasificacion: { type: "string" },
												CatRie: { type: "string" },
												Tipo_Amo: { type: "number" },
												TieneCupon: { type: "string" },
												Fecha: { type: "string" },
												OpcionSeleccionada: { type: "string" },
												TipoValorizacion: { type: "string" },
												OpcPlazoDias: { type: "string" },
												IdEmpresa: { type: "number" },
												TipoEmisor: { type: "string" },
												TipoCartera: { type: "string" },
												NombreMesa: { type: "string"},
												Duration: {type: "number"}
											
										}
									}
								}
							}
						});

						dataSourceInforme.fetch().then(function (result) {
							let datosFinales = [];
							let objeto;
							$.each(dataSourceInforme.data(), function (key, value) {
								objeto = new Object();
								objeto.CantidadDecimales = value.CantidadDecimales;
								objeto.TasaDecimales = value.TasaDecimales;
								objeto.IdCoberturaPatrimonial = value.IdCoberturaPatrimonial;
								objeto.FolioComprobante = value.FolioComprobante;
								objeto.FolioCartera = value.FolioCartera;
								objeto.FolioOperacion = value.FolioOperacion;
								objeto.IdTipoOperacion = value.IdTipoOperacion;
								objeto.IdInstrumento = value.IdInstrumento;
								objeto.IdTipoInstrumento = value.IdTipoInstrumento;
								objeto.TipoInstrumento = value.TipoInstrumento;
								objeto.ValorMercado = value.ValorMercado;
								objeto.TIR = value.TIR;
								objeto.Cartera = value.Cartera;
								objeto.FechaVcto = value.FechaVcto;
								objeto.Cantidad = value.Cantidad;
								objeto.Emisor = value.Emisor;
								objeto.Nemotecnico = value.Nemotecnico;
								objeto.Corredor = value.Corredor;
								objeto.Variable_Cobertura = value.Variable_Cobertura;
								objeto.IdMonedaInstrumento = value.IdMonedaInstrumento;
								objeto.CLA_RIE = value.CLA_RIE;
								objeto.FechaVencimientoInstrumento = value.FechaVencimientoInstrumento;
								objeto.IdIndicador = value.IdIndicador;
								objeto.NombreIndicador = value.NombreIndicador;
								objeto.Plazo = value.Plazo;
								objeto.PlazoPacto = value.PlazoPacto;
								objeto.TramoCobertura = value.TramoCobertura;
								objeto.PorcentajeCobertura = value.PorcentajeCobertura;
								objeto.MontoCobertura = value.MontoCobertura;
								objeto.Clasificacion = value.Clasificacion;
								objeto.CatRie = value.CatRie;
								objeto.CatRiePactos = value.CatRiePactos;
								objeto.Tipo_Amo = value.Tipo_Amo;
								objeto.TieneCupon = value.TieneCupon;
								objeto.Fecha = value.Fecha;
								objeto.IdOpcionSeleccionada = value.IdOpcionSeleccionada;
								objeto.OpcionSeleccionada = value.OpcionSeleccionada;
								objeto.IdTipoValorizacion = $scope.data.ValorCoberturaSeleccionado.IdValorCobertura;
								objeto.TipoValorizacion = value.TipoValorizacion;
								objeto.IdOpcPlazoDias = value.IdOpcPlazoDias;
								objeto.OpcPlazoDias = value.OpcPlazoDias;
								objeto.Observaciones = value.Observaciones;
								objeto.IdEmpresa = value.IdEmpresa;
								objeto.TipoEmisor = value.TipoEmisor;
								objeto.TipoCartera = value.TipoCartera;
								objeto.NombreMesa = value.NombreMesa;
								objeto.Duration = value.Duration;

								datosFinales.push(objeto);

							});
							$scope.dataTemplate.data = datosFinales;
							$scope.obtenerReporte($scope.dataTemplate);
						},
							function (result) {
								bcsComunes.msg("error", "UI_ERROR_AL_OBTENER_DATOS", "UI_ERROR_AL_OBTENER_DATOS");
							});
					}
				});
			}

			$scope.dataTemplate = {};

            $scope.data.validarValorizacionAnterior = function() {		
	
                if ($scope.data.kngFechaHasta > $scope.data.kngValorizaAl ) {
                    bcsComunes.msg("error", "COBERTURA PATRIMONIAL", $translate.instant('UI_ERROR_FECHA_VALORIZACION'));
                    bcsComunes.ventanaOverlay(idVentana, false);
                    return;
                }
                $scope.data.generarReporte();
                return;
            }

			$scope.data.generarReporte = function () {
				bcsComunes.ventanaOverlay(idVentana, true);
				let keyInforme = 'COBERTURA.PATRIMONIAL'
					+ (($scope.data.ValorCoberturaSeleccionado.IdValorCobertura === 1) ? "COMP" : "MERC")
					+ util.dateSPdate($scope.data.kngValorizaAl)
					+ util.dateSPdate($scope.data.kngValorizaAl)
				let buscarInformeAnterior = new bcs.DataSource({
					type: "odata",
					definition: {
						serverFiltering: true,
						transport: {
							read: {
								url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010137",
								dataType: "json"
							}
						},
						schema: {
							model: {
								fields: {
									IdInforme: { type: "string" },
									Key: { type: "string" },
									FechaHora: { type: "string" },
									Rank: { type: "number" }
								}
							}
						}
					}
				});

				let filtroInforme = [{ field: "Key", operator: "eq", value: keyInforme }, {field: "IdSocioDeNegocio", operator: "eq", value: $scope.SesionCorredoraIdSocioDeNegocio}];

				buscarInformeAnterior.query({filter : filtroInforme}).then(function(){
					const informe = buscarInformeAnterior.data();
					const noTieneIdInforme = !informe[0]?.IdInforme;

					if (noTieneIdInforme) {
						$scope.data.generarValorizacion();
						return
					}
					
					const fechaUltimaValorizacion = informe[0]?.FechaHora;
					const modalConfirmacion = bcsAlerta.crear("PREGUNTA_SI_NO",
						`La ultima valorización fue realizada el ${fechaUltimaValorizacion} ¿Desea realizar una nueva valorización?`,
						idVentana);
						
					modalConfirmacion.then(function(result){
						if(result === OPCIONES_BCSALERTA.SI) {
							$scope.data.generarValorizacion();
						} else {
							$scope.data.obtenerDataInforme(informe[0].IdInforme);
						}
					})
				})
			}

			$scope.data.generarValorizacion = function () {
				bcsComunes.ventanaOverlay(idVentana, true);
				let datos = {
					p_IdEmpresa: $scope.SesionIdEmpresa,
					p_FechaOperacion: util.dateSPdate($scope.data.kngFechaHasta),
					p_plazoDias: $scope.data.plazoCoberturaSeleccionado.IdDiasCobertura || 3,
					p_opcion: $scope.data.OpcionCoberturaSeleccionado.IdOpcionCobertura,
					p_TipoValor: $scope.data.ValorCoberturaSeleccionado.IdValorCobertura,
					p_IdCliente: $scope.data.oConfiguracionMesaActiva.seleccionado.IdCliente || 0,
					p_IdPortafolio: $scope.data.oConfiguracionMesaActiva.seleccionado.IdPortafolio || 0,
					p_NombreMesa: $scope.data.oConfiguracionMesaActiva.seleccionado.Nombre || 'TODOS',
					p_ValorizaAl: util.dateSPdate($scope.data.kngValorizaAl),
					p_FechaHoraSistema: $scope.SesionFechaSistema,
					p_Informe: $scope.data.informe,
					p_IdUsuario			: $scope.SesionIdUsuario,
					p_IdModulo			: "MRF",
					p_IdPais 			: $scope.$root.SesionIdPaisSistema, 
				}
				bcsServiciosMesa.ConsultarValorizacionPatrimonioMRF(datos).$promise.then(function (respuesta) {
					if (respuesta.LlaveReporte !== undefined && respuesta.P_OUT_ERRORESCONTADOR === 0) {
						$scope.data.obtenerDataInforme(respuesta.LlaveReporte);
					}
					else {
						bcsComunes.ventanaMsg({
							conflictos: util.parseServerResponse(respuesta)
						}, idVentana);
					}
				});
			}

			const RegistrarLog = function () {
				let datos = new Object()
				let datosLog = [{
					IdModulo: "MRF",
					IdUsuario: $scope.data.SesionIdEmpresa,
					TipoMensaje: "INFORMACION",
					Glosa: "Cobertura patrimonial actualizada",
					Valor1: util.dateSPdate($scope.data.kngFechaHasta),
					Valor2: "CoberturaPatrimonial"
				}];
				datos.RegistroLog = datosLog;

				bcsServicioPlataforma.RegistrarLog(datos, function (resp) {
					
				})

			}

		});
}());


