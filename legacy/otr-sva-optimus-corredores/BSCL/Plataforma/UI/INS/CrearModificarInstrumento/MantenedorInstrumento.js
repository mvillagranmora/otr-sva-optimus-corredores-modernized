angular.module('ModuloPlataforma').controller('MantenedorInstrumentoCtrl',function($scope, bcsComunes, bcsServicioPlataforma, bcsAlerta, OPCIONES_BCSALERTA, $timeout,$translate) {

	var mantenedorInstrumento = this;
	$scope.IdVentana = bcsComunes.ventanaWinId($scope);
	$scope.modo = 'INGRESAR';
	var parametros = bcsComunes.ventanaParams($scope.IdVentana);
	$scope.ActivarCheckEsAutorizar = true;
	$scope.ActivarIntegracion = false;

	$scope.tiposDeBonos = null;
	bcsServicioPlataforma.TiposDeBonos(function(arr) {
		$scope.tiposDeBonos = arr;
	});

	$scope.formData = {};
	$scope.formData.filtros = {
		tipoInstrumento : {
			IdClasificacionTipoInstrumento : {
				field : "IdClasificacionTipoInstrumento",
				operator : "eq",
				value : ""
			},
			IdBolsa : {
				field : "IdBolsa",
				operator : "eq",
				value : ""
			}
		},
		tipoInstrumentoSubyacente : {
			IdClasificacionTipoInstrumentoSubyacente : {
				field : "IdClasificacionTipoInstrumentoSubyacente",
				operator : "eq",
				value : ""
			},
			IdTipoInstrumento : {
					field : "IdTipoInstrumento",
					operator : "ne",				
					value : "IOA"

			},			
			IdBolsa : {
				field : "IdBolsa",
				operator : "eq",
				value : ""
			}
		},
		valorClasificacionRiesgo : {
			IdClasificacionTipoInstrumento : {
				field : "IdClasificacionTipoInstrumento",
				operator : "eq",
				value : ""
			},
			IdEntidadClasificadoraRiesgo : {
				field : "IdEntidadClasificadoraRiesgo",
				operator : "eq",
				value : ""
			}
		},
		entidadClasificadoraRiesgo : {
			IdClasificacionTipoInstrumento : {
				field : "IdClasificacionTipoInstrumento",
				operator : "eq",
				value : ""
			}
		},
		emisores : {
			IdEstado : {
				field : "IdEstado",
				operator : "eq",
				value : "ACT"
			}
		}
	};
	
	var dataSourceBonos = new bcs.DataSource({
		type : 'odata',
		definition : {
			transport : {
				read : {
					url : '/BSCL/Plataforma/GWJAVA/PLA/call?t=4146',
					dataType : 'json'
				}
			},
			serverFiltering : true,
			serverPaging : true,
			schema : {
				model : {
					fields : {
						IdInstrumento 					: { type : 'number'},
						FechaDevengo 					: {type : 'date' },
						AjusteFinDeMes 					: {type : 'string'},
						CallConfirmado 					: {type : 'string'},
						ConvencionDias 					: {type : 'string'},
						CuponeraVariable 				: {type : 'string'},
						TasaEmision 					: {type : 'number'},
						TenorInflacion 					: {type : 'string'},
						TipoLibor 						: {type : 'string'},
						TipoCalculoInteresesCupon 		: {type : 'number'},
						TipoCalculoInteresesDevengados 	: {type : 'number'},
						IdCalendario 					: {type : 'string'},
						IdTipoInflacion 				: {type : 'string'},
						FechaPrepago 					: {type : 'date'},
						CUSIP 							: {type : 'string'},
						COMMON 							: {type : 'string'},
						SEDOL 							: {type : 'string'},
						TickerBloomberg 				: {type : 'string'},
						MercadoLocal 					: {type : 'string'},
						MercadoExtrabursatil 			: {type : 'string'},
						MercadoExtranjero 				: {type : 'string'},
						PresenciaBursatil 				: {type : 'string'},
						IdPaisTransaccion 				: {type : 'number'},
						ValorBolsa 						: {type : 'number'},
						ValorBloomberg 					: {type : 'number'},
						DurationBolsa 					: {type : 'number'},
						DurationBloomberg 				: {type : 'number'},
						FechaValorizacion 				: {type : 'date'},
						ValorNominal 					: {type : 'number'},
						PrecioValorizacion 				: {type : 'number'}
					}
				}
			}
		}
	});

	$scope.formData.agregarValorRiesgo = function() {
		var data = $scope.formData.apiValoresRiesgo.dataSource
				.data();
		var a = {};
		a.ValorClasificacionRiesgo = $scope.formData.valorClasificacionRiesgoSeleccionada.ClasificacionRiesgo;
		a.IdEntidadClasificadoraRiesgo = $scope.formData.entidadClasificadoraRiesgoSeleccionada.IdEntidadClasificadoraRiesgo;

		var found = false;
		$.each(data,function(key, value) {
			if (a.IdEntidadClasificadoraRiesgo === value.IdEntidadClasificadoraRiesgo) {
				found = true;
			}
		});

		if (found) {
			bcsComunes.msg("error", "UI_ERROR", $translate
					.instant("UI_VALOR_YA_EXISTE"));
		} else {
			$scope.formData.apiValoresRiesgo.dataSource.add(a);
			$scope.formData.apiValoresRiesgo.refresh();
		}
	};

	$scope.formData.eliminarValorRiesgo = function() {
		$scope.formData.apiValoresRiesgo.dataSource.remove($scope.formData.valorSeleccionado);
		$scope.formData.apiValoresRiesgo.refresh();
		$scope.formData.valorSeleccionado = false;
	};
	
	$scope.muestraCheckEsInstrumentoCalificado = function() {
   		return $scope.instrumento.IdClasificacionInstrumento === 'RV';
	};

	$scope.bonoDef = {};
	$scope.bono = {};
	$scope.bono.IdInstrumento = 0;

	$scope.instrumento = {};
	$scope.instrumento.valoresRiesgo = [];
	$scope.instrumentoDef = {};
	
	var schemaCupones = {
		model : {
			fields : {
				IdCupon : {
					type : 'number'
				},
				Nemotecnico : {
					type : 'string'
				},
				Numero : {
					type : 'number'
				},
				Fecha : {
					type : 'date'
				},
				Call : {
					type : 'string'
				},
				Intereses : {
					type : 'number'
				},
				Amortizacion : {
					type : 'number'
				}
			}
		}
	};
	
	var dataSourceCupones = new bcs.DataSource({
			type : 'odata',
			definition : {
				transport : {
					read : {
						url : '/BSCL/Plataforma/GWJAVA/PLA/call?t=4144',
						dataType : 'json'
					}
				},
				serverFiltering : true,
				serverPaging : true,
				serverSorting : true,
				sort : {
					field : "Fecha",
					dir : "asc"
				},
				pageSize : 200,
				schema : schemaCupones
			}
		});

	$scope.oConfiguracionCondicionLiquidacion = {
 			filtros:{ 				   
 				IdTipoOperacion		: {"field": "IdTipoOperacion","operator":"eq","value": "COMACN"}
 			}
 	}

	//se utiliza onBlur para identificar la cantidad de cupones cuando se digita Nemotecnico
	$scope.obtenerCuponesPorNemotecnico = function() {
		if ($scope.formData.instrumentoSubyacente === 'RF') {
			dataSourceCupones.filter({field : 'Nemotecnico', operator : 'eq', value : $scope.instrumento.Nemotecnico});
			dataSourceCupones.read().then(function() {
					var cupones = bcs.cleanData(dataSourceCupones.data());
					$scope.instrumento.NumeroCupones = cupones.length;
			});
		}
	};
		
	//Combo manual ya que periodo no varia 		
	$scope.FrecuenciaCuponOptions = {
		     placeholder	: "PERIODO",
		     dataTextField	: "text",
		     dataValueField	: "value",
		     dataSource		: [
				         { text: "MENSUAL", 	value: 1 },
				         { text: "TRIMESTRAL", 	value: 3 },
				         { text: "SEMESTRAL", 	value: 6 },
				         { text: "ANUAL", 		value: 12}
		     ],
		     filter			: "contains",
		     autoBind		: true,
		     readonly       : true,
		     minLength		: 2,
		     index			: 2,
		     clearButton    : "false"
		};
	
	
	if (parametros.instrumento) {
		bcsComunes.ventanaTitulo($scope.IdVentana, $translate.instant("UI_MODIFICAR_INSTRUMENTO"));
		$scope.modo = 'MODIFICAR';
		$scope.instrumentoDef = $.extend({},parametros.instrumento);
		$scope.instrumento = $.extend({},parametros.instrumento);
		$scope.instrumento.EstaInscrito = $scope.instrumento.EstaInscrito === 'S';
		$scope.instrumento.InstrumentoAcreditado = $scope.instrumento.InstrumentoAcreditado === 'S';
		$scope.instrumento.EsAutorizado = $scope.instrumento.EsAutorizado === 'S';

//		$scope.instrumento.RelacionadoGrupo = $scope.instrumento.RelacionadoGrupo === 'S';
//		$scope.instrumento.FormadorMercado = $scope.instrumento.FormadorMercado === 'S';
//		$scope.instrumento.PactoAndino = $scope.instrumento.PactoAndino === 'S';

		$scope.instrumento.LargoPlazo = $scope.instrumento.LargoPlazo === 'S';
		$scope.instrumento.AfectoArt107 = $scope.instrumento.AfectoArt107 === 'S';
		$scope.instrumento.AfectoArt104 = $scope.instrumento.AfectoArt104 === 'S';
		$scope.instrumento.DividendoOtraMoneda = $scope.instrumento.DividendoOtraMoneda === 'S';
		$scope.instrumento.CobroComisionesCustodia = $scope.instrumento.CobroComisionesCustodia === 'S';
		$scope.instrumento.EsInstrumentoCalificado = $scope.instrumento.EsInstrumentoCalificado === 'S';
		
		if ($scope.instrumento.IdTipoInstrumento === 'OPC') {
			$scope.formData.setInstrumentoSubyacente = parametros.instrumento.NemotecnicoRelacionado;
		}
		dataSourceBonos.query({
							filter : [ {
								field : 'IdInstrumento',
								operator : 'eq',
								value : $scope.instrumento.IdInstrumento
							} ]
						})
				.then(function() {
					if (dataSourceBonos.data().length > 0) {
						$scope.bonoDef = $
								.extend(
										{},
										dataSourceBonos
												.data()[0]);
						$scope.bono = $.extend({},
								$scope.bonoDef);
						$scope.bono.CuponeraVariable = $scope.bono.CuponeraVariable === 'S';
						$scope.bono.CallConfirmado = $scope.bono.CallConfirmado === 'S';
						$scope.bono.AjusteFinDeMes = $scope.bono.AjusteFinDeMes === 'S';
						$scope.bono.MercadoLocal = $scope.bono.MercadoLocal === 'S';
						$scope.bono.MercadoExtrabursatil = $scope.bono.MercadoExtrabursatil === 'S';
						$scope.bono.MercadoExtranjero = $scope.bono.MercadoExtranjero === 'S';
						$scope.bono.PresenciaBursatil = $scope.bono.PresenciaBursatil === 'S';
					}
					
				});
	}

	$scope.cancelar = function() {
		bcsComunes.ventanaCerrar($scope.IdVentana);
	}

	$scope.aceptar = function() {
		if ($scope.modo === 'INGRESAR') {
			var valores = $scope.formData.apiValoresRiesgo.dataSource.data();
			var p_valores = [];
			$.each(valores,function(key, value) {
				var t = {
						IdInterno : 0,
						IdEntidadClasificadoraRiesgo : value.IdEntidadClasificadoraRiesgo,
						ValorClasificacionRiesgo : value.ValorClasificacionRiesgo,
				}
				p_valores.push(t);
			});

			var p_bolsas = [];
			var bolsas = $scope.formData.apiBolsas.dataSource.data();
			$.each(bolsas,function(key, value) {
				var t = {
					IdInterno 	: 0,
					IdBolsa 	: value.IdBolsa,
					IdInstrumento : value.IdInstrumento,
					IdInstrumentoSubyacente : value.IdInstrumentoSubyacente,
					IdTipoInstrumento : value.IdTipoInstrumento,
					IdTipoInstrumentoSubyacente : $scope.instrumento.IdTipoInstrumento,
					EsGarantizable : value.EsGarantizable
				}
				p_bolsas.push(t);
			});
			
			var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo : 'PLA',
				p_simularOperacion : 'N',
				p_UsaReglasNegocio : 'N',
				p_Autorizar : 'S',
				p_Instrumentos : [ {
					IdInterno : 0,
					Nemotecnico : $scope.instrumento.Nemotecnico || null,
					ISIN : $scope.instrumento.ISIN || null,
					CorteMinimo : $scope.instrumento.CorteMinimo || null,
					EstaInscrito : $scope.instrumento.EstaInscrito ? 'S' : 'N',
					InstrumentoAcreditado : $scope.instrumento.InstrumentoAcreditado ? 'S' : 'N',
					RazonSocial : $scope.instrumento.RazonSocial || null,
					FechaEmision : $scope.instrumento.FechaEmision ? util.dateSPdate($scope.instrumento.FechaEmision) : null,
					FechaVencimiento : $scope.instrumento.FechaVencimiento ? util.dateSPdate($scope.instrumento.FechaVencimiento) : null,
					IdEmisor : $scope.instrumento.IdEmisor || null,
					IdTipoReajuste : $scope.instrumento.IdTipoReajuste || null,
					IdMoneda : $scope.instrumento.IdMoneda || null,
					IdTipoInstrumento : $scope.instrumento.IdTipoInstrumento || null,
					IdEstado : $scope.instrumento.IdEstado || null,
					Tamano : $scope.instrumento.Tamano || null,
					IdEntidadClasificadoraRiesgo : $scope.instrumento.IdEntidadClasificadoraRiesgo || null,
					ValorClasificacionRiesgo : $scope.instrumento.ValorClasificacionRiesgo || null,
					PrecioBase : $scope.instrumento.Precio || null,
					PrecioReferencia : 0,					//deprecated
					FechaReferencia : "0001-01-01",			//deprecated
					FrecuenciaCupon : $scope.instrumento.FrecuenciaCupon || null,
					NumeroCupones : $scope.instrumento.NumeroCupones || null,
					TipoBonoIRF : $scope.instrumento.TipoBonoIRF || null,
					OrigenInstrumento : $scope.instrumento.OrigenInstrumento || null,
					TipoRenta : $scope.instrumento.TipoRenta || null,
					RelacionadoGrupo : $scope.instrumento.RelacionadoGrupo || null,
//					RelacionadoGrupo : $scope.instrumento.RelacionadoGrupo ? 'S' : 'N',
					FormadorMercado: $scope.instrumento.FormadorMercado || null,
//					FormadorMercado : $scope.instrumento.FormadorMercado ? 'S' : 'N',
					PactoAndino: $scope.instrumento.PactoAndino || null,
//					PactoAndino : $scope.instrumento.PactoAndino ? 'S' : 'N',
					FactorExposicion : $scope.instrumento.FactorExposicion || null,
					LargoPlazo : $scope.instrumento.LargoPlazo ? 'S' : 'N',
					IdInstrumentoRelacionado : $scope.formData.instrumentoSubyacente ? $scope.formData.instrumentoSubyacente.IdInstrumento : 0, 		
					EsPutEsCall : $scope.instrumento.TipoOpcion.ValorParametro || null,
					PrecioStrike : $scope.instrumento.PrecioStrike || null,
					PorcentajeCastigo : $scope.instrumento.PorcentajeCastigo || null,
					FechaHastaAcreditado : $scope.instrumento.FechaHastaAcreditado ? util.dateSPdate($scope.instrumento.FechaHastaAcreditado) : null,
					NemotecnicoCambio : $scope.instrumento.NemotecnicoCambio || null,
					Tasa : $scope.instrumento.Tasa || null,
					AfectoArt107 : $scope.instrumento.AfectoArt107 ? 'S' : 'N',
					AfectoArt104 : $scope.instrumento.AfectoArt104 ? 'S' : 'N',
					DividendoOtraMoneda : $scope.instrumento.DividendoOtraMoneda ? 'S' : 'N',
					EsAutorizado : $scope.instrumento.EsAutorizado ? 'S' : 'N',
					CobroComisionesCustodia: $scope.instrumento.CobroComisionesCustodia ? 'S' : 'N',
					IdInstrumentoRaiz: $scope.formData.instrumento == undefined  ? 0 : $scope.formData.instrumento.IdInstrumento,
					IdCondicionLiquidacion: $scope.instrumento.oConfiguracionCondicionLiquidacion || '',
					EsInstrumentoCalificado: $scope.instrumento.EsInstrumentoCalificado && $scope.instrumento.IdClasificacionInstrumento == 'RV'? 'S' : 'N'
				} ],
				p_ValoresRiesgo : p_valores,
				p_Bolsas : p_bolsas
			};
			bcsComunes.ventanaOverlay($scope.IdVentana, true);
			bcsServicioPlataforma.Servicios().CrearInstrumento(datos).$promise.then(function(result) {
				bcsComunes.ventanaOverlay($scope.IdVentana,false);
				if (result.P_OUT_ERRORESCONTADOR === 0) {
					if ($scope.esBono($scope.instrumento)) {
						grabarBono($scope.bono);
					} else {
						ventanaResultadoOK("UI_INSTRUMENTO_INGRESADO",result);
					}
				} else {
					ventanaResultadoError("UI_ERROR_INGRESAR_INSTRUMENTO",result);
				}
			}, function(result) {
				bcsComunes.ventanaOverlay($scope.IdVentana,false);
				bcsComunes.msg("error", "UI_ERROR","UI_ERROR_INGRESAR_INSTRUMENTO");
			});
		} else if ($scope.modo === 'MODIFICAR') {
			var ISINAnterior = parametros.instrumento.ISIN
			
			if (ISINAnterior !== $scope.instrumento.ISIN) {
				var y = bcsAlerta.crear("PREGUNTA_SI_NO","El codigo ISIN sera modificado. ¿Desea continuar?" ,$scope.IdVentana)
				y .then(function(result)
				{
					bcsComunes.ventanaOverlay($scope.IdVentana, true);

					if (result === OPCIONES_BCSALERTA.NO)
					{
						bcsComunes.ventanaOverlay($scope.IdVentana, false);
						return;
					}
					if (result === OPCIONES_BCSALERTA.SI){
						ventanaModificar()
					}
				});
			}
			else{
				ventanaModificar()
			}
		}
	}

	// riesgo
	$scope.formData.apiValoresRiesgo = undefined;

	$scope.formData.configuracionValoresRiesgo = {
		gridCreated : function(e) {
			$scope.formData.apiValoresRiesgo = e;
			if ($scope.modo === 'MODIFICAR') {
				var datasss = [];
				var ds = new bcs.DataSource(
						{
							type : "odata",
							definition : {
								serverFiltering : true,
								transport : {
									read : {
										url : "/BSCL/Plataforma/GWJAVA/PLA/call?c=1&t=3232",
										dataType : "json"
									}
								},
								filter : {
									field : "IdInstrumento",
									operator : "eq",
									value : Number($scope.instrumento.IdInstrumento)
								},
								schema : {
									model : {
										fields : {
											IdInstrumento 					: { type : "number" },
											ValorClasificacionRiesgo 		: { type : "string"},
											IdEntidadClasificadoraRiesgo 	: { type : "string"}
										}
									}
								}
							}
						});
				ds.fetch().then(function() {
					datasss = ds.data();
					e.dataSource.data(datasss);
				});
			}
		},
		autoBind : true,
		height : 150,
		selectable : "row",
		dataSource : new bcs.DataSource({
			type : "array",
			definition : {
				data : [],
				schema : {
					model : {
						fields : {
							ValorClasificacionRiesgo 		: { type : "string"},
							IdEntidadClasificadoraRiesgo 	: { type : "string"}
						}
					}
				}
			}
		}),
		columns : [
				{ field : "IdEntidadClasificadoraRiesgo",	title : $translate.instant('UI_ENTIDAD_CLASIFICADORA'), width : "100px" },
				{ field : "ValorClasificacionRiesgo",		title : $translate.instant('UI_VALOR'),	width : "100px"} 
			]
	};
	
	$scope.formData.configuracionBolsas = {
		gridCreated : function(e) {
			$scope.formData.apiBolsas = e;
			if ($scope.modo === 'MODIFICAR') {
				var datasss = [];
				var ds = new bcs.DataSource({
					type : "odata",
					definition : {
						serverFiltering : true,
						transport : {
							read : {
								url : "/BSCL/Plataforma/GWJAVA/PLA/call?c=1&t=20999",
								dataType : "json"
							}
						},
						filter : {
							field : "IdInstrumento",
							operator : "eq",
							value : Number($scope.instrumento.IdInstrumento)
						},
						schema : {
							model : {
								fields : {
									IdInstrumento	: { type : "number" },
									IdBolsa			: { type : "string" },
									Nombre		: { type : "string" },
									IdInstrumentoSubyacente : {type: "number"},
									IdTipoInstrumento : {type: "string"},
									EsGarantizable : {type:"string"},
									IdTipoInstrumentoSubyacente : {type:"string"}
								}
							}
						}
					}
				});
				ds.fetch().then(function() {
					datasss = ds.data();
					e.dataSource.data(datasss);
				});
			}
		},
		autoBind : true,
		height : 150,
		selectable : "row",
		dataSource : new bcs.DataSource({
			type : "array",
			definition : {
				data : [],
				schema : {
					model : {
						fields : {
							IdBolsa : { type : "string"},
							Nombre : { type : "string"},
							IdInstrumento : {type: "number"},
							IdInstrumentoSubyacente : {type: "number"},
							IdTipoInstrumento : {type: "string"},
							EsGarantizable : {type:"string"},
							IdTipoInstrumentoSubyacente : {type:"string"}
						}
					}
				},
				group: [
					//{field: "IdBolsa"},
					{field: "Nombre"}
				]
			}
		}),
		columns : [
				{ field : "IdTipoInstrumento",	title : $translate.instant('UI_TIPO_OPERACION'), width : "100px"},
				{ field : "EsGarantizable",	title : $translate.instant('UI_ES_GARANTIZABLE'), width : "100px"},
			]
	};
	
	
	$scope.formData.agregarBolsa = function() {
		var data = $scope.formData.apiBolsas.dataSource.data();
		var a = {};
		a.IdBolsa = $scope.formData.bolsaSeleccionada.IdBolsa;
		a.Nombre = $scope.formData.bolsaSeleccionada.Nombre;
		a.IdInstrumento = $scope.instrumento.IdInstrumento;
		a.IdInstrumentoSubyacente = null;
		a.IdTipoInstrumento = $scope.formData.tipoInstrumentoSeleccionado.IdTipoInstrumento === undefined ? '' : $scope.formData.tipoInstrumentoSeleccionado.IdTipoInstrumento;
		a.IdTipoInstrumentoSubyacente = $scope.instrumento.IdTipoInstrumento;
		a.EsGarantizable = $scope.formData.tipoInstrumentoSeleccionado.IdTipoInstrumento === undefined ? '' : $scope.formData.esGarantizable === true ? 'S' : 'N';

		var found = false;
		var agrega = true;
		$.each(data,function(key, value) {
			if (a.IdBolsa === value.IdBolsa && a.IdTipoInstrumento === value.IdTipoInstrumento) {
				value.EsGarantizable = value.EsGarantizable == null ? 'N' : value.EsGarantizable;
				if (a.EsGarantizable === value.EsGarantizable) {
					found = true;	
				} else {
					agrega = false;
					value.EsGarantizable = a.EsGarantizable;
				}
			}
		});

		if (found) {
			bcsComunes.msg("error", "UI_ERROR", $translate.instant("UI_VALOR_YA_EXISTE"));
		} else {
			if (agrega) {
				$scope.formData.apiBolsas.dataSource.add(a);
			}
			$scope.formData.apiBolsas.refresh();
		}
	};

	$scope.formData.eliminarBolsa = function() {
		$scope.formData.apiBolsas.dataSource.remove($scope.formData.bolsaGridSeleccionada);
		$scope.formData.apiBolsas.refresh();
		$scope.formData.idBolsaSeleccionada = false;
		$scope.formData.bolsaGridSeleccionada = false;
	};

	function grabarBono(bono) {
		var datos = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario : $scope.SesionIdUsuario,
			p_IdModulo : 'PLA',
			p_Nemotecnico : $scope.instrumento.Nemotecnico,
			p_FechaDevengo : bono.FechaDevengo || null,
			p_AjusteFinDeMes : bono.AjusteFinDeMes ? "S" : "N",
			p_CallConfirmado : bono.CallConfirmado ? "S" : "N",
			p_ConvencionDias : bono.ConvencionDias,
			p_CuponeraVariable : bono.CuponeraVariable ? "S" : "N",
			p_TasaEmision : bono.TasaEmision,
			p_TenorInflacion : bono.TenorInflacion || null,
			p_TipoLibor : bono.TipoLibor || null,
			p_TipoCalculoInteresesCupon : bono.TipoCalculoInteresesCupon,
			p_TipoCalculoInteresesDevengados : bono.TipoCalculoInteresesDevengados,
			p_IdCalendario : bono.IdCalendario || null,
			p_IdTipoInflacion : bono.IdTipoInflacion || null,
			p_FechaPrepago : bono.FechaPrepago || null,
			p_CUSIP : bono.CUSIP || null,
			p_COMMON : bono.COMMON || null,
			p_SEDOL : bono.SEDOL || null,
			p_TickerBloomberg : bono.TickerBloomberg || null,
			p_MercadoLocal : bono.MercadoLocal ? "S" : "N",
			p_MercadoExtrabursatil : bono.MercadoExtrabursatil ? "S" : "N",
			p_MercadoExtranjero : bono.MercadoExtranjero ? "S" : "N",
			p_PresenciaBursatil : bono.PresenciaBursatil ? "S" : "N",
			p_IdPaisTransaccion : bono.IdPaisTransaccion || null,
			p_ValorBolsa : bono.ValorBolsa || null,
			p_ValorBloomberg : bono.ValorBloomberg || null,
			p_DurationBolsa : bono.DurationBolsa || null,
			p_DurationBloomberg : bono.DurationBloomberg || null,
			p_FechaValorizacion : bono.FechaValorizacion || null,
			p_ValorNominal : bono.ValorNominal || null,
			p_PrecioValorizacion : bono.PrecioValorizacion || null
		};
		bcsComunes.ventanaOverlay($scope.IdVentana, true);
		bcsServicioPlataforma.Servicios().GuardarBono(datos).$promise.then(
				function(result) {
					bcsComunes.ventanaOverlay(
							$scope.IdVentana, false);
					if (result.P_OUT_ERRORESCONTADOR === 0) {
						ventanaResultadoOK("UI_BONO_GUARDADO",result);
					} else {
						ventanaResultadoError("UI_ERROR_GUARDAR_BONO",result);
					}
				}, function(result) {
					bcsComunes.ventanaOverlay($scope.IdVentana, false);
					bcsComunes.msg("error", "UI_ERROR","UI_ERROR_GUARDAR_BONO");
				});
	}

	function ventanaResultadoOK(codigoMensaje, result) {
		var p = bcsComunes.ventanaMsg({
			mensaje : $translate.instant(codigoMensaje),
			conflictos : util.parseServerResponse(result)
		}, $scope.IdVentana);
		bcsComunes.ventanaPromesa(p).then(function() {
			bcsComunes.ventanaData($scope.IdVentana, {
				guardar : true
			});
			bcsComunes.ventanaCerrar($scope.IdVentana);
		});
	}

	function ventanaResultadoError(codigoMensaje, result) {
		bcsComunes.ventanaOverlay($scope.IdVentana, false);
		bcsComunes.ventanaMsg({
			mensaje : codigoMensaje,
			conflictos : util.parseServerResponse(result)
		}, $scope.IdVentana);
	}

	function ventanaModificar(){
		var valores = $scope.formData.apiValoresRiesgo.dataSource.data();
		var p_valores = [];
		$.each(valores,function(key, value) {
			var t = {
				IdInstrumento : $scope.instrumento.IdInstrumento,
				IdEntidadClasificadoraRiesgo : value.IdEntidadClasificadoraRiesgo,
				ValorClasificacionRiesgo : value.ValorClasificacionRiesgo,
			}
			p_valores.push(t);
		});

		var p_bolsas = [];
		var bolsas = $scope.formData.apiBolsas.dataSource.data();
		$.each(bolsas,function(key, value) {
			var t = {
				IdInstrumento 		: $scope.instrumento.IdInstrumento,
				IdBolsa 			: value.IdBolsa,
				IdInstrumento : value.IdInstrumento,
				IdInstrumentoSubyacente : value.IdInstrumentoSubyacente,
				IdTipoInstrumento : value.IdTipoInstrumento,
				IdTipoInstrumentoSubyacente : $scope.instrumento.IdTipoInstrumento,
				EsGarantizable : value.EsGarantizable
			}
			p_bolsas.push(t);
		});

		var datos = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario : $scope.SesionIdUsuario,
			p_IdModulo : 'PLA',
			p_simularOperacion : 'N',
			p_UsaReglasNegocio : 'N',
			p_Autorizar : 'S',
			p_Instrumentos : [ {
				IdInstrumento : $scope.instrumento.IdInstrumento,
				ISIN : $scope.instrumento.ISIN || null,
				CorteMinimo : $scope.instrumento.CorteMinimo || null,
				EstaInscrito : $scope.instrumento.EstaInscrito ? 'S' : 'N',
				InstrumentoAcreditado : $scope.instrumento.InstrumentoAcreditado ? 'S' : 'N',
				RazonSocial : $scope.instrumento.RazonSocial || null,
				FechaEmision : $scope.instrumento.FechaEmision ? util.dateSPdate($scope.instrumento.FechaEmision): null,
				FechaVencimiento : $scope.instrumento.FechaVencimiento ? util.dateSPdate($scope.instrumento.FechaVencimiento): null,
				IdEmisor : $scope.instrumento.IdEmisor || null,
				IdTipoReajuste : $scope.instrumento.IdTipoReajuste || null,
				IdMoneda : $scope.instrumento.IdMoneda || null,
				IdEstado : $scope.instrumento.IdEstado || null,
				Tamano : $scope.instrumento.Tamano || null,
				PrecioBase : ($scope.instrumento.Precio!==undefined) ? $scope.instrumento.Precio : null,
				PrecioReferencia : 0,				//deprecated
				FechaReferencia : "0001-01-01",		//deprecated
				FrecuenciaCupon : $scope.instrumento.FrecuenciaCupon || null,
				NumeroCupones : ($scope.instrumento.NumeroCupones !==undefined && $scope.instrumento.NumeroCupones >=0) ? $scope.instrumento.NumeroCupones : null,
				TipoBonoIRF : $scope.instrumento.TipoBonoIRF || null,
				OrigenInstrumento : $scope.instrumento.OrigenInstrumento || null,
				TipoRenta : $scope.instrumento.TipoRenta || null,
				//RelacionadoGrupo : $scope.instrumento.RelacionadoGrupo ? 'S' : 'N',
				RelacionadoGrupo : $scope.instrumento.RelacionadoGrupo || null,
				//FormadorMercado : $scope.instrumento.FormadorMercado ? 'S' : 'N',
				FormadorMercado : $scope.instrumento.FormadorMercado || null,
				//PactoAndino : $scope.instrumento.PactoAndino ? 'S' : 'N',
				PactoAndino: $scope.instrumento.PactoAndino || null,
				FactorExposicion : $scope.instrumento.FactorExposicion || null,
				LargoPlazo : $scope.instrumento.LargoPlazo ? 'S' : 'N',
				PorcentajeCastigo : $scope.instrumento.PorcentajeCastigo || null,
				FechaHastaAcreditado : $scope.instrumento.FechaHastaAcreditado || null,
				NemotecnicoCambio : $scope.instrumento.NemotecnicoCambio || null,
				EsPutEsCall : $scope.instrumento.TipoOpcion.ValorParametro || null,
				PrecioStrike : $scope.instrumento.PrecioStrike || null,
				IdTipoInstrumento : $scope.instrumento.IdTipoInstrumento || null,
				Tasa : $scope.instrumento.Tasa || null,
				AfectoArt107 : $scope.instrumento.AfectoArt107 ? 'S' : 'N',
				AfectoArt104 : $scope.instrumento.AfectoArt104 ? 'S' : 'N',
				DividendoOtraMoneda : $scope.instrumento.DividendoOtraMoneda ? 'S' : 'N',
				EsAutorizado : $scope.instrumento.EsAutorizado ? 'S' : 'N',
				CobroComisionesCustodia: $scope.instrumento.CobroComisionesCustodia ? 'S' : 'N',
				IdInstrumentoRaiz: $scope.instrumento.IdInstrumentoRaiz || 0,
				IdCondicionLiquidacion: $scope.instrumento.oConfiguracionCondicionLiquidacion || '',
				EsInstrumentoCalificado : $scope.instrumento.EsInstrumentoCalificado && $scope.instrumento.IdClasificacionInstrumento == 'RV'? 'S' : 'N'
			} ],
			p_ValoresRiesgo : p_valores,
			p_Bolsas : p_bolsas
		};
		bcsComunes.ventanaOverlay($scope.IdVentana, true);
		bcsServicioPlataforma.Servicios().ModificarInstrumento(datos).$promise.then(
			function(result) {
				bcsComunes.ventanaOverlay($scope.IdVentana,false);
				if (result.P_OUT_ERRORESCONTADOR === 0) {
					if ($scope.esBono($scope.instrumento)) {
						grabarBono($scope.bono);
					} else {
						ventanaResultadoOK("UI_INSTRUMENTO_MODIFICADO",result);
					}
				}
				else {
					ventanaResultadoError("UI_ERROR_MODIFICAR_INSTRUMENTO",result);
				}
			},
			function(result) {
				bcsComunes.ventanaOverlay($scope.IdVentana,false);
				bcsComunes.msg("error","UI_ERROR","UI_ERROR_MODIFICAR_INSTRUMENTO");
			}
		);	
	}

	$scope.cupones = function() {
		bcsComunes.ventanaAbrir('MantenedorCupones', {
			modo : $scope.modo,
			instrumento : $scope.instrumento,
			bono : $scope.bono
		}, true, false, bcsComunes.ventanaWinId($scope));
	}

	$scope.valorizar = function() {
		bcsComunes
				.ventanaAbrir(
						'MantenedorInstrumentoValorizador', {
							instrumento : $scope.instrumento,
							bono : $scope.bono
						}, true, false, bcsComunes
								.ventanaWinId($scope));
	}

	$scope.esBono = function(instrumento) {
		if ($scope.instrumento.IdClasificacionInstrumento === null) {
			return false;
		}

		//if ($scope.SesionLocale !== 'PE') {
			//return false;
		//}
		
		if ($scope.instrumento.IdClasificacionInstrumento !== 'RF') {
			return false;
		}

		var tipos = $scope.tiposDeBonos;
		if (tipos === null || tipos.length == 0) {
			return true;
		}

		for (var i = 0; i < tipos.length; i++) {
			if (instrumento.IdTipoInstrumento === tipos[i]) {
				return true;
			}
		}

		return false;
	}
	
	var after = function(n) {
	  var fraction = n.toString().split('.')[1];
	  return parseInt(fraction, 10);
	};
	
	

	$scope.$watch('formData.entidadClasificadoraRiesgoSeleccionada.IdEntidadClasificadoraRiesgo',function(newval,oldval){
		
		if (oldval !== undefined)
		{		
			$scope.formData.filtros.valorClasificacionRiesgo.IdClasificacionTipoInstrumento.value = $scope.instrumento.IdClasificacionInstrumento;
			$scope.formData.filtros.valorClasificacionRiesgo.IdEntidadClasificadoraRiesgo.value = $scope.formData.entidadClasificadoraRiesgoSeleccionada.IdEntidadClasificadoraRiesgo;
			$scope.formData.deshabilitarGarantizable = $scope.instrumento.IdClasificacionInstrumento === 'RF' ? true : false;
		}
		
	});	
	
	$scope.$watch('instrumento.IdClasificacionInstrumento',function(newval,oldval){
		if (newval !== undefined & newval !== false){
			if (newval == 'RV'){
				$scope.ActivarIntegracion = true;
			}else{				
				$scope.ActivarIntegracion = false;
			}
			bcsComunes.ventanaAutoHeight($scope.IdVentana);	
		}
	});

	$scope.$watch('instrumento.IdTipoInstrumento',function(newval,oldval){
		if (newval !== undefined & newval !== false){
			if (newval == 'CFI' || newval == 'CFX' ){
				$scope.ActivarCheckEsAutorizar = false;
			}else{				
				$scope.ActivarCheckEsAutorizar = true;
				$scope.instrumento.EsAutorizado = true;				
			}			
		}					
	});	
	$scope.$watchGroup(['formData.instrumentoSubyacente',
						'instrumento.FechaVencimiento',
						'instrumento.TipoOpcion',
						'instrumento.Precio'],function(newval,oldval) {
		if (!newval) {
			return;
		}
		if ($scope.formData.instrumentoSubyacente && $scope.instrumento.FechaVencimiento &&
				$scope.instrumento.TipoOpcion && $scope.instrumento.PrecioStrike){
			var day = ("00" + new Date($scope.instrumento.FechaVencimiento).getDate()).slice(-2);
			var month = ("00" + (new Date($scope.instrumento.FechaVencimiento).getMonth() + 1)).slice(-2);
			var year = $scope.instrumento.FechaVencimiento.getYear() - 100;
			var putcall = $scope.instrumento.TipoOpcion.ValorParametro.substring(0,1);
			var precioEntero = Math.trunc($scope.instrumento.PrecioStrike);
			var precioDecimal = Math.ceil(((new Big($scope.instrumento.PrecioStrike) < 1.0) ? new Big($scope.instrumento.PrecioStrike) : (new Big($scope.instrumento.PrecioStrike).mod(Math.floor(new Big($scope.instrumento.PrecioStrike)))) * 1000))
			var nemoInstrumento = $scope.formData.instrumentoSubyacente.Nemotecnico.substring(0,4) +
								  year + month + day + putcall + ("000000" + precioEntero).slice(-6) + 
								  ("000" + precioDecimal).slice(-3);
		    $scope.instrumento.Nemotecnico = nemoInstrumento;
		}
		
		$timeout(function() {bcsComunes.ventanaAutoHeight($scope.IdVentana);})
	}); 
	
	$scope.instrumento.InstrumentoAcreditado = false;

	$scope.$watch('formData.bolsaGridSeleccionada', function(newval,oldval){
		if (newval !== undefined) {
			$scope.formData.idBolsaSeleccionada = newval.IdBolsa;
			$scope.formData.esGarantizable = newval.EsGarantizable === 'S' ? true : false;
			$scope.formData.tipoInstrumentoSeleccionadoGrilla = newval.IdTipoInstrumento;
		}
	});

});
