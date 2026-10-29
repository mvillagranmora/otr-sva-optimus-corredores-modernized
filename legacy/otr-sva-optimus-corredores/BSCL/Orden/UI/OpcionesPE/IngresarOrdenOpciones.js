angular.module('ModuloOrdenes').controller('IngresarOrdenOpcionesCtrl', function($scope,OPCIONES_BCSALERTA,
																				 bcsServicioReportes,
																				 bcsServiciosPlataformaMercado,
																				 $translate,
																				 bcsComunes,
																				 bcsConfiguracionOrdenesV2,
																				 bcsServicioOrden,
																				 bcsServicioPlataforma,
																				 bcsAlerta,
																				 $timeout,
																				 bcsConfiguracionOrdenes,
																				 bcsConsultasOrdenes){

	var ordenOpciones = this;
	var controllerName = 'IngresarOrdenOpcionesCtrl';
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	ordenOpciones.datosOrden = {};
	
	ordenOpciones.datosResumen = {};
	
	$scope.$watchGroup(['ordenOpciones.datosOrden.portafolioSeleccionado.IdPortafolio','ordenOpciones.datosOrden.operacionSeleccionada.IdTipoOperacion'],function(newval,oldval){
		
		if (ordenOpciones.datosOrden.portafolioSeleccionado && ordenOpciones.datosOrden.portafolioSeleccionado.IdPortafolio
				&& ordenOpciones.datosOrden.operacionSeleccionada && ordenOpciones.datosOrden.operacionSeleccionada.IdTipoOperacion) {
			
			var datos = {
					p_IdEmpresa 		: $scope.SesionIdEmpresa,
					p_IdUsuario			: $scope.SesionIdUsuario,
					p_IdModulo			: 'ORD',
					p_IdEmpleado		: $scope.SesionIdEmpleado,
					p_IdTipoOperacion	: ordenOpciones.datosOrden.operacionSeleccionada.IdTipoOperacion,
					P_IdPortafolio		: ordenOpciones.datosOrden.portafolioSeleccionado.IdPortafolio
			}
			
			bcsServicioOrden.ConsultarRequisitosBasicosCrearOrdenes(datos).$promise.then(function(result){
				if (result.P_OUT_ERRORESCONTADOR > 0) {
					bcsComunes.ventanaMsg({
						conflictos : util.parseServerResponse(result)
					},idVentana);
				}
			});
			
		}
		
		
	});
	
	$scope.$watchGroup(['ordenOpciones.datosOrden.portafolioSeleccionado.IdPortafolio',
		'ordenOpciones.datosForm.AccionOrden',
		'ordenOpciones.datosOrden.formaCustodiaSeleccionada.IdFormaCustodiaOrden'],function(){
		if (ordenOpciones.datosOrden.portafolioSeleccionado && ordenOpciones.datosOrden.portafolioSeleccionado.IdPortafolio &&
				ordenOpciones.datosForm.AccionOrden === 'VENTA' &&
				ordenOpciones.datosOrden.formaCustodiaSeleccionada && ordenOpciones.datosOrden.formaCustodiaSeleccionada.IdFormaCustodiaOrden === 'CUS'
				) {
			ordenOpciones.datosForm.validaFlujoCustodia = 'S';
		}
		else {
			ordenOpciones.datosForm.validaFlujoCustodia = 'N';
		}
	});
	
	ordenOpciones.datosForm = {
			fechaInicial :util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL"),
			decimalesCantidad : 0,
			pensando 			: false,
			calculandoAlgo		: false,
			decimalesPrecio : 0,
			estilo		: "EUROPEA",
			modalidadLiquidacion : "FINANCIERA",
			decimalesMonto : 0,
			decimalesTipoCambio : 0,
			valorAllin : undefined,		
			montoAllin : undefined,
			idVentana : idVentana,
			modo : 'INGRESO',
			muestraCobros : false,
			configuracionSocioNegocio : {
				filtros: {
					IdEmpresa 		: {"field" : "IdEmpresa", "operator" : "eq", "value" : $scope.SesionIdEmpresa},
					EsCliente 		: {"field"   : "EsCliente","operator": "eq", "value"   :  'UI_SI' }
				},
			},
			configuracionCompraVenta : {
				defecto : "COMPRA"
			},
			configuracionPortafolio : {
				filtros: {
					IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  0 }
				},
			},
			configuracionAutorizadoDarOrdenes : {
				filtros: {
					IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  -1 },
					IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   :  "ACT" }
				},
			},
			configuracionBoveda : {
				filtros : {
					IdBoveda : { "field"   : "IdBoveda","operator": "eq", "value"   :  "" }
				}
			},
			configuracionMecanismoCaptacion : {
				filtros: {
					CodigoTipoIndicador : { "field"   : "CodigoTipoIndicador","operator": "eq", "value"   :  "ORDMODALIDAD" },
					IdEntidad : {"field":"IdEntidad","operator":"eq","value": -1 },
				},
			},
			configuracionCorredorCorresponsal : {
				filtros : {
					IdPais		: { "field"   : "IdPais","operator": "eq", "value"   : "" },
					IdEmpresa	: { "field"   : "IdEmpresa","operator": "eq", "value"   : $scope.SesionIdEmpresa },
				}
			},
			configuracionCuentaCustodio : {
				filtros : {
					IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   : "" },
					IdEstadoAutorizacion : { "field"   : "IdEstadoAutorizacion","operator": "eq", "value"   : "AUT" },
					IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
					IdModeloCustodia : { "field"   : "IdModeloCustodia","operator": "eq", "value"   : "" }
				},
			},
			configuracionPreferenciaPagoPortafolio : {
				filtros : {
					IdMedioDePago : { "field"   : "IdMedioDePago",operator: "neq", "value"   :  "CIN" },
					IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  "" },
					IdMoneda : { "field"   : "IdMoneda","operator": "eq", "value"   :  "" },
					IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   :  "" }
				}
			},
			
			configuracionTipoOpcion : {
				defecto : bcsConfiguracionOrdenes.CONSTANTES.TipoOpcion,
				filtros : {
					IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
				},
			},
			configuracionTipoVigencia : {
				defecto : bcsConfiguracionOrdenes.CONSTANTES.IdTipoVigenciaSoloHoy,
				filtros : {
					IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
				},
			},
			configuracionPorcentajeVisibilidad : {
				filtros : {
					IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
				},
			},
			configuracionCondicionLiquidacion : { 
				filtros : {
					IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   : "" },
					EstadoTipoOperacionYCondicionDeLiquidacion : { "field"   : "EstadoTipoOperacionYCondicionDeLiquidacion","operator": "eq", "value"   : "ACT" }
				}
			},
			comboMonedaLiquidacionOrden : { 
				filtros : {
					IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value"   : "" },
					IdMonedaInstrumento : { "field"   : "IdMoneda","operator": "eq", "value"   : "" }
				}
			},
			configuracionCanalDistribucion : {
				filtros : {
					IdTipoInstrumento : { field : "IdTipoInstrumento", operator : "eq", value : ""},
					IdBolsa : { field : "IdBolsa", operator : "eq", value : ""},
					IdTipoCanalIngreso : { field : "IdTipoCanalIngreso", operator : "eq", value : $scope.SesionTipoCanalIngreso},
					IdEmpresa : { field : "IdEmpresa", operator : "eq", value : $scope.SesionIdEmpresa},
				}
			},
			configuracionBolsa : {
				defecto : 'OUT',
				filtros : {
					IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
					IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value"   : "OPC" },
					EstadoBolsaTipoInstrumento : { "field"   : "EstadoBolsaTipoInstrumento","operator": "eq", "value"   : "ACT" }//,
				}
			},
			verComprobante : function(){
				bcsConfiguracionOrdenes.FUNCIONES.verComprobante($scope.SesionIdEmpresa,
						$scope.ordenOpciones.datosResumen.documentoIdentidad,
						$scope.ordenOpciones.datosResumen.fechaOperacionOrden,
						$scope.ordenOpciones.datosResumen.secuenciaOrden,
						$scope.ordenOpciones.datosResumen.montoOperacion,
						
						
						idVentana);
			},
			verDetalle : function() {
				bcsComunes.ventanaAbrir("DetalleOrden", {IdOrden : ordenOpciones.datosResumen.secuenciaOrden} );
			}
    
	};
	
	$scope.ordenOpciones.datosForm.cambioPorcentaje = function() {
		var operacion = [{
			IdInstrumento  				: ordenOpciones.datosOrden.instrumento.IdInstrumento,
			FechaOperacion 				: $scope.SesionFechaSistema,
			CantidadOrden   			: ordenOpciones.datosOrden.cantidadInicial || 0,
			PrecioOrden   				: ordenOpciones.datosOrden.precio || 0,
			MontoTotal					: ordenOpciones.datosOrden.cantidadInicial * ordenOpciones.datosOrden.precio * ordenOpciones.datosForm.tamanioContrato || 0,
			IdMonedaLiquidacionOrden	: ordenOpciones.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion,

			IdPortafolio 				: ordenOpciones.datosOrden.portafolioSeleccionado.IdPortafolio,
			IdTipoProducto 				: ordenOpciones.datosOrden.operacionSeleccionada.IdTipoProducto,
			IdTipoOperacion 			: ordenOpciones.datosOrden.operacionSeleccionada.IdTipoOperacion,
			IdCanalIngreso 				: $scope.SesionTipoCanalIngreso,
			IdBolsa 					: ordenOpciones.datosOrden.bolsaSeleccionada.IdBolsa,
			IdCorredorCorresponsal		: ordenOpciones.datosOrden.idCorredorCorresponsal,
			ValorAllIn					: ordenOpciones.datosForm.valorAllin,
			MontoAllIn					: undefined

		}];
		
		ordenOpciones.datosForm.apiCobros.calcularCobros(operacion);
	}
	
	$scope.ordenOpciones.datosForm.cambioMonto = function() {
		var operacion = [{
			IdInstrumento  				: ordenOpciones.datosOrden.instrumento.IdInstrumento,
			FechaOperacion 				: $scope.SesionFechaSistema,
			CantidadOrden   			: ordenOpciones.datosOrden.cantidadInicial || 0,
			PrecioOrden   				: ordenOpciones.datosOrden.precio || 0,
			MontoTotal					: ordenOpciones.datosOrden.cantidadInicial * ordenOpciones.datosOrden.precio * ordenOpciones.datosForm.tamanioContrato || 0,
			IdMonedaLiquidacionOrden	: ordenOpciones.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion,

			IdPortafolio 				: ordenOpciones.datosOrden.portafolioSeleccionado.IdPortafolio,
			IdTipoProducto 				: ordenOpciones.datosOrden.operacionSeleccionada.IdTipoProducto,
			IdTipoOperacion 			: ordenOpciones.datosOrden.operacionSeleccionada.IdTipoOperacion,
			IdCanalIngreso 				: $scope.SesionTipoCanalIngreso,
			IdBolsa 					: ordenOpciones.datosOrden.bolsaSeleccionada.IdBolsa,
			IdCorredorCorresponsal		: ordenOpciones.datosOrden.idCorredorCorresponsal,
			ValorAllIn					: undefined,
			MontoAllIn					: ordenOpciones.datosForm.montoAllin

		}];
		
		ordenOpciones.datosForm.apiCobros.calcularCobros(operacion);
	}
	
	/*OPTIMUSPE-1215*/
	var defCorresponsalBoveda = new bcs.DataSource({
		type: "odata",
		definition 	: {
			serverFiltering : true,
			serverPaging 	: true,
			serverSorting 	: true,
			schema 	: {
				model	: {
                    fields	: {
                    	IdCorredorCorresponsal	: { type: "number"},
                    	IdTipoProducto			: { type: "string"},
                    	NombreTipoProducto		: { type: "string"},
                    	IdBoveda				: { type: "string"}
                    }
           		}
			},
			transport : {
				read : {
					url :bcsConsultasOrdenes('ConsultarProductoBovedaCorredorCorresponsal'),
					dataType : "json"
				}
			}
		}	
	});
	
	//watch operacion cobros
	$scope.$watchGroup(['ordenOpciones.datosOrden.tipoCambio',
		'ordenOpciones.datosOrden.precio',
		'ordenOpciones.datosOrden.portafolioSeleccionado.IdPortafolio',
		'ordenOpciones.datosOrden.operacionSeleccionada.IdTipoOperacion',
		'ordenOpciones.datosOrden.bolsaSeleccionada.IdBolsa',
		'ordenOpciones.datosOrden.cantidadInicial',
		'ordenOpciones.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion',
		'ordenOpciones.datosOrden.idCorredorCorresponsal',
		'ordenOpciones.datosOrden.corredorCorresponsalSeleccionada'
		],function(newval,oldval){
	
		
		if (ordenOpciones.datosOrden.monedaLiquidacionOrdenSeleccionada && ordenOpciones.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion !== undefined &&
				ordenOpciones.datosOrden.instrumento && ordenOpciones.datosOrden.instrumento.IdInstrumento !== undefined &&
				ordenOpciones.datosOrden.portafolioSeleccionado && ordenOpciones.datosOrden.portafolioSeleccionado.IdPortafolio !== undefined &&
				ordenOpciones.datosOrden.operacionSeleccionada && ordenOpciones.datosOrden.operacionSeleccionada.IdTipoOperacion !== undefined &&
				ordenOpciones.datosOrden.bolsaSeleccionada && ordenOpciones.datosOrden.bolsaSeleccionada.IdBolsa !== undefined &&
				ordenOpciones.datosOrden.precio &&
				(ordenOpciones.datosOrden.cantidadInicial)
		) {
			
			var operacion = [{
				IdInstrumento  				: ordenOpciones.datosOrden.instrumento.IdInstrumento,
				FechaOperacion 				: $scope.SesionFechaSistema,
				CantidadOrden   			: ordenOpciones.datosOrden.cantidadInicial || 0,
				PrecioOrden   				: ordenOpciones.datosOrden.precio || 0,
				MontoTotal					: ordenOpciones.datosOrden.cantidadInicial * ordenOpciones.datosOrden.precio * ordenOpciones.datosForm.tamanioContrato || 0,
				IdMonedaLiquidacionOrden	: ordenOpciones.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion,

				IdPortafolio 				: ordenOpciones.datosOrden.portafolioSeleccionado.IdPortafolio,
				IdTipoProducto 				: ordenOpciones.datosOrden.operacionSeleccionada.IdTipoProducto,
				IdTipoOperacion 			: ordenOpciones.datosOrden.operacionSeleccionada.IdTipoOperacion,
				IdCanalIngreso 				: $scope.SesionTipoCanalIngreso,
				IdBolsa 					: ordenOpciones.datosOrden.bolsaSeleccionada.IdBolsa,
				IdCorredorCorresponsal		: ordenOpciones.datosOrden.idCorredorCorresponsal,
				ValorAllIn					: ordenOpciones.datosForm.valorAllin,
				MontoAllIn					: ordenOpciones.datosForm.montoAllin

			}];
			
			ordenOpciones.datosForm.apiCobros.calcularCobros(operacion);
		}
		/* OPTIMUSPE-1215 - Boveda por defecto cuando es Bolsa Extranjera*/	
		if($scope.SesionLocale === 'PE'){
			if(ordenOpciones.datosOrden.bolsaSeleccionada != undefined && ordenOpciones.datosOrden.bolsaSeleccionada.EsExtranjera === 'S' &&
					ordenOpciones.datosOrden.operacionSeleccionada !== undefined && ordenOpciones.datosOrden.idCorredorCorresponsal !== undefined &&
					ordenOpciones.datosOrden.corredorCorresponsalSeleccionada !== undefined){
				var ConsultarCorresponsalBoveda = new bcs.DataSource(defCorresponsalBoveda);
				var filtro = Array();
				filtro.push({ field : "IdCorredorCorresponsal", operator : "eq", value : ordenOpciones.datosOrden.idCorredorCorresponsal == false ? null : ordenOpciones.datosOrden.idCorredorCorresponsal});
				filtro.push({ field : "IdTipoProducto", operator : "eq", value : ordenOpciones.datosOrden.operacionSeleccionada.IdTipoProducto});
				ConsultarCorresponsalBoveda.query({filter: filtro}).then(function(){
					if(ConsultarCorresponsalBoveda.data() != undefined && ConsultarCorresponsalBoveda.data().length > 0 && ConsultarCorresponsalBoveda.data()[0] != undefined){					
						ordenOpciones.IdBovedaCorresponsal = ConsultarCorresponsalBoveda.data()[0].IdBoveda  == null ? "" : ConsultarCorresponsalBoveda.data()[0].IdBoveda;
						ordenOpciones.datosForm.configuracionCuentaCustodio.filtros.IdBoveda = { "field"   : "IdBoveda","operator": "eq", "value"   : ordenOpciones.IdBovedaCorresponsal };
					}else{
						//ordenOpciones.datosForm.configuracionCuentaCustodio.filtros.IdBoveda = { "field"   : "IdBoveda","operator": "eq", "value"   : ""};
						ordenRF.IdBovedaCorresponsal = "";
						ordenOpciones.datosForm.configuracionCuentaCustodio.filtros = [];						
						ordenOpciones.datosForm.configuracionCuentaCustodio  = {
							filtros : {
								IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   : "" },
								IdEstadoAutorizacion : { "field"   : "IdEstadoAutorizacion","operator": "eq", "value"   : "AUT" },
								IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
								IdModeloCustodia : { "field"   : "IdModeloCustodia","operator": "eq", "value"   : "" },
								IdBoveda : { "field"   : "IdBoveda","operator": "eq", "value" : "-1" }
							}
						};
					}
				});	
			}	
		}
		/*OPTIMUSPE-1215*/
	});	
	
	//watch operacion cobros cotizador
	$scope.$watchGroup([
		'ordenOpciones.datosOrden.precio',
		'ordenOpciones.datosOrden.precioObjetivo',
		'ordenOpciones.datosOrden.portafolioSeleccionado.IdPortafolio',
		'ordenOpciones.datosOrden.operacionSeleccionada.IdTipoOperacion',
		'ordenOpciones.datosOrden.bolsaSeleccionada.IdBolsa',
		'ordenOpciones.datosOrden.cantidadInicial',
		'ordenOpciones.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion',
		'ordenOpciones.datosOrden.instrumento.IdInstrumento',
		'ordenOpciones.datosForm.montoAllin'
		],function(newval,oldval){
	
		
		if (
				ordenOpciones.datosOrden.precio &&
				ordenOpciones.datosOrden.precioObjetivo &&
				ordenOpciones.datosOrden.portafolioSeleccionado && ordenOpciones.datosOrden.portafolioSeleccionado.IdPortafolio !== undefined &&
				ordenOpciones.datosOrden.operacionSeleccionada && ordenOpciones.datosOrden.operacionSeleccionada.IdTipoOperacion !== undefined &&
				ordenOpciones.datosOrden.bolsaSeleccionada && ordenOpciones.datosOrden.bolsaSeleccionada.IdBolsa !== undefined &&
				ordenOpciones.datosOrden.cantidadInicial &&
				ordenOpciones.datosOrden.monedaLiquidacionOrdenSeleccionada && ordenOpciones.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion !== undefined &&
				ordenOpciones.datosOrden.instrumento && ordenOpciones.datosOrden.instrumento.IdInstrumentoRelacionado !== undefined &&
				ordenOpciones.datosForm.montoAllin !== undefined
				
		) {
			
			var datos = {	
					p_IdEmpresa: $scope.SesionIdEmpresa,
					p_IdUsuario: $scope.SesionIdUsuario,
					p_IdModulo: 'ORD',
										
					p_Ordenes : [],
					p_CobrosModificados : []
				};
    			
    		var operacion1 = undefined;
    		var operacion2 = undefined;
    		var producto = "INRV";
			if (ordenOpciones.datosOrden.instrumento.EsPutEsCall === 'CALL' && ordenOpciones.datosForm.AccionOrden === 'COMPRA') {
				operacion1 = "COMACN";
				operacion2 = "VENACN";
			}
			else if (ordenOpciones.datosOrden.instrumento.EsPutEsCall === 'CALL' && ordenOpciones.datosForm.AccionOrden === 'VENTA') {
				operacion1 = "VENACN";
				operacion2 = "COMACN";
			}
			else if (ordenOpciones.datosOrden.instrumento.EsPutEsCall === 'PUT' && ordenOpciones.datosForm.AccionOrden === 'COMPRA') {
				operacion1 = "VENACN";
				operacion2 = "COMACN";
			}
			else if (ordenOpciones.datosOrden.instrumento.EsPutEsCall === 'PUT' && ordenOpciones.datosForm.AccionOrden === 'VENTA') {
				operacion1 = "COMACN";
				operacion2 = "VENACN";
			}
			else {
				return;
			}

			//ORDEN A PRECIO STRIKE
			var orden1 = {
				CantidadOrden   				: ordenOpciones.datosOrden.cantidadInicial*ordenOpciones.datosForm.tamanioContrato,
				DiasPlazo 						: 0,
				EsFungible  					: 'N',
				IdBolsa							: ordenOpciones.datosOrden.bolsaSeleccionada.IdBolsa,
				IdCorredorCorresponsal			: ordenOpciones.datosOrden.idCorredorCorresponsal?ordenOpciones.datosOrden.idCorredorCorresponsal:null,
				IdInterno  						: 0,
				IdInstrumento  					: ordenOpciones.datosOrden.instrumento.IdInstrumentoRelacionado,
				IdMonedaLiquidacionOrden        : ordenOpciones.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion,
				IdPortafolio 					: ordenOpciones.datosOrden.portafolioSeleccionado.IdPortafolio,
				IdTipoProducto  				: producto,
				IdTipoOperacion					: operacion1,
				IdTipoCanalIngreso				: $scope.SesionTipoCanalIngreso,
				FechaOperacion					: $scope.SesionFechaSistema,
				MontoOrden						: 0,
				PrecioOrden 					: ordenOpciones.datosOrden.instrumento.PrecioStrike,
				TasaSimultanea  				: 0,
				TasaRFIF  						: 0,
				TasaVentaCorta  				: 0,
				TipoCambio 						: 0,
				ValorRescateOrden				: 0,

				MontoTotal						: 0,
				EsPorMonto 						: 'N',
				TasaReporte						: 0,
				TasaReportado					: 0,
				PlazoReporte					: 0,
				BaseTasa						: 0,
				TipoInteres						: '',
			};
			datos.p_Ordenes.push(orden1);
			
			//ORDEN A MERCADO
			var orden2 = {
					CantidadOrden   				: ordenOpciones.datosOrden.cantidadInicial*ordenOpciones.datosForm.tamanioContrato,
					DiasPlazo 						: 0,
					EsFungible  					: 'N',
					IdBolsa							: ordenOpciones.datosOrden.bolsaSeleccionada.IdBolsa,
					IdCorredorCorresponsal			: ordenOpciones.datosOrden.idCorredorCorresponsal?ordenOpciones.datosOrden.idCorredorCorresponsal:null,
					IdInterno  						: 1,
					IdInstrumento  					: ordenOpciones.datosOrden.instrumento.IdInstrumentoRelacionado,
					IdMonedaLiquidacionOrden        : ordenOpciones.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion,
					IdPortafolio 					: ordenOpciones.datosOrden.portafolioSeleccionado.IdPortafolio,
					IdTipoProducto  				: producto,
					IdTipoOperacion					: operacion2,
					IdTipoCanalIngreso				: $scope.SesionTipoCanalIngreso,
					FechaOperacion					: $scope.SesionFechaSistema,
					MontoOrden						: 0,
					PrecioOrden 					: ordenOpciones.datosOrden.precioObjetivo,
					TasaSimultanea  				: 0,
					TasaRFIF  						: 0,
					TasaVentaCorta  				: 0,
					TipoCambio 						: 0,
					ValorRescateOrden				: 0,

					MontoTotal						: 0,
					EsPorMonto 						: 'N',
					TasaReporte						: 0,
					TasaReportado					: 0,
					PlazoReporte					: 0,
					BaseTasa						: 0,
					TipoInteres						: '',
				};
			datos.p_Ordenes.push(orden2);
			
			//ORDEN OPCIONES
			var orden3 = {
					CantidadOrden   				: ordenOpciones.datosOrden.cantidadInicial,
					DiasPlazo 						: 0,
					EsFungible  					: 'N',
					IdBolsa							: ordenOpciones.datosOrden.bolsaSeleccionada.IdBolsa,
					IdCorredorCorresponsal			: ordenOpciones.datosOrden.idCorredorCorresponsal?ordenOpciones.datosOrden.idCorredorCorresponsal:null,
					IdInterno  						: 2,
					IdInstrumento  					: ordenOpciones.datosOrden.instrumento.IdInstrumento,
					IdMonedaLiquidacionOrden        : ordenOpciones.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion,
					IdPortafolio 					: ordenOpciones.datosOrden.portafolioSeleccionado.IdPortafolio,
					IdTipoProducto  				: ordenOpciones.datosOrden.operacionSeleccionada.IdTipoProducto,
					IdTipoOperacion					: ordenOpciones.datosOrden.operacionSeleccionada.IdTipoOperacion,
					IdTipoCanalIngreso				: $scope.SesionTipoCanalIngreso,
					FechaOperacion					: $scope.SesionFechaSistema,
					MontoOrden						: 0,
					PrecioOrden 					: ordenOpciones.datosOrden.precio,
					TasaSimultanea  				: 0,
					TasaRFIF  						: 0,
					TasaVentaCorta  				: 0,
					TipoCambio 						: 0,
					ValorRescateOrden				: 0,

					MontoTotal						: 0,
					EsPorMonto 						: 'N',
					TasaReporte						: 0,
					TasaReportado					: 0,
					PlazoReporte					: 0,
					BaseTasa						: 0,
					MontoAllIn						: ordenOpciones.datosForm.montoAllin,
					TipoInteres						: '',
				};
			datos.p_Ordenes.push(orden3);
			
			var p = bcsServicioOrden.CalcularMontoYCobrosOrden(datos);
			p.$promise.then(function(result) {
				
				var cobrosOrden1 = result.p_out_Ordenes[0].TotalCobrosLiquidacion;
				var cobrosOrden2 = result.p_out_Ordenes[1].TotalCobrosLiquidacion;
				var variacionPrecio = new Big(ordenOpciones.datosOrden.precioObjetivo).minus(new Big(ordenOpciones.datosOrden.instrumento.PrecioStrike)).minus(new Big(ordenOpciones.datosOrden.precio));
				var cantidadAcciones = ordenOpciones.datosOrden.cantidadInicial*ordenOpciones.datosForm.tamanioContrato;

				console.log("cobros orden 1:" + cobrosOrden1);
				console.log("cobros orden 2:" + cobrosOrden2);
				console.log("variacionPrecio:" + variacionPrecio.round(3).toString());
				console.log("cantidadAcciones:" + cantidadAcciones);
				
				ordenOpciones.datosOrden.montoDiferido = cobrosOrden1 + cobrosOrden2;
				ordenOpciones.datosOrden.margenOSpread = (new Big(variacionPrecio).times(new Big(cantidadAcciones)).minus(new Big(ordenOpciones.datosOrden.montoDiferido))).round(3).toString();
				ordenOpciones.datosOrden.montoOrden1 = result.p_out_Ordenes[0].MontoTotalLiquidacion;
				ordenOpciones.datosOrden.montoOrden2 = result.p_out_Ordenes[1].MontoTotalLiquidacion;
				ordenOpciones.datosOrden.montoOrden3 = result.p_out_Ordenes[2].MontoTotalLiquidacion;
			});
			
			
			
		}
	});	
	
	
	
	ordenOpciones.datosForm.cleanData = function() {
        ordenOpciones.datosOrden.idPortafolio = false;
		ordenOpciones.datosForm.ingresoRapido = undefined;
		ordenOpciones.datosOrden.mecanismoDeCaptacion = undefined;
		ordenOpciones.datosForm.operacionSalida = [];
		ordenOpciones.datosForm.operacionSalidaCotizador = [];
		ordenOpciones.datosForm.AccionOrdenDefecto = "COMPRA";
		ordenOpciones.datosForm.setInstrumento = "-1";
		
		ordenOpciones.datosResumen = {
				fechaOperacionOrden : '',
				secuenciaOrden : '',
				tipoInstrumento : '',
				operacion : '',
				cantidad : '',
				instrumento : '',
				precio : '',
				montoOperacion : '',
				totalCobros : '',
				montoTotal :'',
				documentoIdentidad : '',
				nombreEjecutivoPortafolio : ''
		};
		
		ordenOpciones.datosOrden.precioStrike				= undefined;
		ordenOpciones.datosOrden.vencimientoOpcion			= undefined;
		ordenOpciones.datosForm.setInstrumentoSubyacente	 = "-1";
		
		ordenOpciones.datosOrden.precio 					= undefined;
		ordenOpciones.datosOrden.tipoCambio 				= undefined;
		ordenOpciones.datosOrden.fechaRecepcion 			= undefined;
		ordenOpciones.datosOrden.cantidadInicial 			= undefined;
		ordenOpciones.datosOrden.fechaVencimiento 		= undefined;
		ordenOpciones.datosOrden.montoPlazo 				= undefined;
		ordenOpciones.datosOrden.montoOperacionCalculado 	= 0;
		ordenOpciones.datosOrden.cantidadCalculada 		= 0;
		ordenOpciones.datosOrden.totalCobros 				= 0;
		ordenOpciones.datosOrden.totalOrden 				= 0;
		ordenOpciones.datosOrden.precioCierre 				= undefined;
		ordenOpciones.datosOrden.montoDiferido 				= undefined;
		
		
		ordenOpciones.datosForm.configuracionCompraVenta.activado = true;
		ordenOpciones.datosForm.instrumentoDeshabilitado = false;
		
		

		
	};
	
	ordenOpciones.datosForm.volver = function() {
		ordenOpciones.datosForm.modo = 'INGRESO';
		ordenOpciones.datosForm.cleanData();
	};
	
	ordenOpciones.datosForm.ingresarOrden = function(a) {
		
		var cobrosModificados = [];
		
		$.each(ordenOpciones.datosOrden.cobrosModificados,function(key,value) {
			cobrosModificados.push({ IdInterno: 0 ,IdCobro : key, ValorCobroAplicado : value  });
		});
		
		var datosIngresoOrden = {
				p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
				p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
				p_IdModulo : 'ORD',
				p_simularOperacion : 'N',
				p_UsaReglasNegocio : 'S',
				
				p_Ordenes : [{
					IdInterno :0,
				    FechaRecepcion : (ordenOpciones.datosOrden.fechaRecepcion)? util.dateSPseconddate(ordenOpciones.datosOrden.fechaRecepcion) : '0001-01-01 00:00:00',
					FechaVigencia  : (ordenOpciones.datosOrden.fechaVigencia)? util.dateSPdate(ordenOpciones.datosOrden.fechaVigencia) :  '0001-01-01',
					FechaEmision : '0001-01-01',
					FechaOperacion : '0001-01-01', //se calcula en el server
					IdCondicionDeLiquidacion : ordenOpciones.datosOrden.condicionLiquidacionSeleccionada.IdCondicionDeLiquidacion,
					IdTipoOperacion : ordenOpciones.datosOrden.operacionSeleccionada.IdTipoOperacion,
					IdTipoVigencia : ordenOpciones.datosOrden.tipoVigenciaSeleccionada.IdTipoVigenciaOrden,
					Glosa : ordenOpciones.datosOrden.comentario || '',
					Cantidad : ordenOpciones.datosForm.operacionSalida[0].cantidadCalculada,
					MontoNeto : 0,
					ValorRescate : 0,
					IdBoveda 		: ordenOpciones.datosOrden.cuentaCustodioSeleccionada.IdBoveda,
					IdFormaCustodia : ordenOpciones.datosOrden.formaCustodiaSeleccionada.IdFormaCustodiaOrden,
					IdPorcentajeVisibilidad : ordenOpciones.datosOrden.porcentajeVisibilidadSeleccionada.IdPorcentajeVisibilidad,
					PrecioLimite : Number(ordenOpciones.datosOrden.precio),
					PrecioMercado : 0,
					EsFacturaInmediata : 'N',
					IdTipoPrecio : "L",
					IdPortafolio : ordenOpciones.datosOrden.portafolioSeleccionado.IdPortafolio,
					IdTipoCanalIngreso : $scope.SesionTipoCanalIngreso,
					IdTipoReajuste : '',
					IdEmisor: ordenOpciones.datosOrden.instrumento.IdEmisor,
					IdBolsa : ordenOpciones.datosOrden.bolsaSeleccionada.IdBolsa,
					EspecificaBolsa : 'S',
					IdMonedaLiquidacionOrden : ordenOpciones.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion,
					IdMonedaLiquidacionCobros : ordenOpciones.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion,
					TipoCambio : ordenOpciones.datosOrden.tipoCambio || null,
					IdInstrumento : ordenOpciones.datosOrden.instrumento.IdInstrumento,
					IdInstrumentoSubyacente : ordenOpciones.datosOrden.instrumentoSubyacente.IdInstrumento || 0,
					IdCondicionDeLiquidacionPlazo : '',
					TasaVentaCorta : 0,
					PlazoVentaCorta : 0,
					EsVentaCorta : 'N',
					TasaSimultanea : 0,
					PlazoSimultanea : 0,
					PlazoIF : 0,
					TasaRFIF : 0,
					EsOD: 'N',
					EsFungible : 'N',
					MontoPlazo : 0,
					IdTipoLibroOrden : 'NORMAL',
					IdLibroOrdenes : null,
					IdCuentaCustodioPortafolio : ordenOpciones.datosOrden.cuentaCustodioSeleccionada.IdCuentaCustodioPortafolio,
					EsPorMonto : "N",
					IdCorredorCorresponsal : ordenOpciones.datosOrden.idCorredorCorresponsal || null,
					IdPersonaAutorizadoDarOrdenes : ordenOpciones.datosOrden.autorizadoDarOrdenes.IdPersona || null,
					IdTipoCanalDistribucion : 	ordenOpciones.datosOrden.idTipoCanalDistribucion || null,
					EsOrdenEspecial : ordenOpciones.datosOrden.esOrdenEspecial === true?"S":"N",
					PagoCobroCuentaInversion : ordenOpciones.datosOrden.pagoCobroCuentaInversion === true?"S":"N",
					IdPreferenciaPagoPortafolio : ordenOpciones.datosOrden.formaDePagoSeleccionada.IdPreferenciaPagoPortafolio,
					TipoInteres : "",
					EsClienteFinal : ordenOpciones.datosOrden.esParaClienteFinal === true?"S":"N",
					IdMecanismoCaptacion : ordenOpciones.datosOrden.mecanismoDeCaptacion?ordenOpciones.datosOrden.mecanismoDeCaptacion:''
				}],
				p_CobrosModificados : cobrosModificados
				
		};
		

		bcsComunes.ventanaOverlay(idVentana,true);
		var promise = bcsServicioOrden.CrearOrdenes(datosIngresoOrden).$promise;
		promise.then(function(respuesta){
   			
   			bcsComunes.ventanaOverlay(idVentana,false);
	   		
			//se valido la orden
			if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_RESULTADO[0].IdOrden > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0) 
			{
				ordenOpciones.datosForm.modo = 'RESUMEN';
				ordenOpciones.datosResumen = {
						fechaOperacionOrden :	util.dateDS(respuesta.P_OUT_RESULTADO[0].FechaOperacionOrden,"FechaHoraSistema","CALL"),
						secuenciaOrden		:	respuesta.P_OUT_RESULTADO[0].IdOrden,
						tipoInstrumento 	: 	respuesta.P_OUT_RESULTADO[0].NombreTipoInstrumento,
						operacion 			:	respuesta.P_OUT_RESULTADO[0].AccionOrden,
						tipoInstrumento_operacion  : respuesta.P_OUT_RESULTADO[0].AccionOrden + ' ' + respuesta.P_OUT_RESULTADO[0].NombreTipoInstrumento,
						cantidad 			:	respuesta.P_OUT_RESULTADO[0].Cantidad,
						instrumento 		: 	respuesta.P_OUT_RESULTADO[0].Instrumento,
						precio 				: 	Number(respuesta.P_OUT_RESULTADO[0].PrecioOrden),
						tipoPrecio 			: 	respuesta.P_OUT_RESULTADO[0].NombreTipoPrecio,
						precio_tipoPrecio	: 	Number(respuesta.P_OUT_RESULTADO[0].PrecioOrden) + ' ' + respuesta.P_OUT_RESULTADO[0].NombreTipoPrecio,
						montoOperacion 		: 	respuesta.P_OUT_RESULTADO[0].MontoLiquidacionOperacion,
						totalCobros 		: 	Number(respuesta.P_OUT_RESULTADO[0].TotalCobros),
						montoTotal 			:	Number(respuesta.P_OUT_RESULTADO[0].MontoTotal),
						idMoneda 			:	respuesta.P_OUT_RESULTADO[0].IdMonedaLiquidacionOrden,
						idMonedaOrden		:	respuesta.P_OUT_RESULTADO[0].IdMonedaOrden,
						documentoIdentidad	:	respuesta.P_OUT_RESULTADO[0].DocumentoIdentidad,
						nombreEjecutivoPortafolio : respuesta.P_OUT_RESULTADO[0].NombreEjecutivoPortafolio
				};
				
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ORDEN_INGRESADA")+": " + respuesta.P_OUT_RESULTADO[0].IdOrden,
					conflictos:util.parseServerResponse(respuesta)
				},idVentana);
				
				ordenOpciones.datosResumen.decimales = util.bcs_formato(0,'ORD','Monto',respuesta.P_OUT_RESULTADO[0].IdMonedaLiquidacionOrden,respuesta.P_OUT_RESULTADO[0].IdTipoInstrumento,'').cantidadDecimales;
				ordenOpciones.datosResumen.decimalesCantidad = util.bcs_formato(0,'ORD','Cantidad',respuesta.P_OUT_RESULTADO[0].IdMonedaLiquidacionOrden,respuesta.P_OUT_RESULTADO[0].IdTipoInstrumento,'').cantidadDecimales;
				ordenOpciones.datosResumen.decimalesPrecio = util.bcs_formato(0,'ORD','Precio',respuesta.P_OUT_RESULTADO[0].IdMonedaLiquidacionOrden,respuesta.P_OUT_RESULTADO[0].IdTipoInstrumento,'').cantidadDecimales;
			}
			else
		    {
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_NO_SE_PUDO_INGRESAR_ORDEN"),
					conflictos:util.parseServerResponse(respuesta)
				},idVentana);
		    }
			
   		},function(respuesta){
   			bcsComunes.ventanaOverlay(idVentana,false);
			bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_EXISTEN_PROBLEMAS_PARA_INGRESAR_ORDEN"),
					conflictos:util.parseServerResponse(respuesta)
				},idVentana);
   		});
   	};
	
   	$scope.$watch('ordenOpciones.datosOrden.pagoCobroCuentaInversion',function(newval,oldva){
   		if (newval === true)
   			ordenOpciones.datosForm.IdPreferenciaPagoPortafolio = false;
   	})
   	
   	
   	$scope.$watch('ordenOpciones.datosOrden.idBolsaSeleccionada',function(newval,oldval) {
   		if (ordenOpciones.datosOrden.idBolsaSeleccionada) {
   			if (ordenOpciones.datosOrden.bolsaSeleccionada.EsExtranjera === 'N') {
   				ordenOpciones.datosForm.configuracionCuentaCustodio.filtros.IdModeloCustodia.value = "LOCAL";
   			}
   			else if (ordenOpciones.datosOrden.bolsaSeleccionada.EsExtranjera === 'S') {
   				ordenOpciones.datosForm.configuracionCuentaCustodio.filtros.IdModeloCustodia.value = "EXTERIOR";
   				/*OPTIMUSPE-1215*/
   				if($scope.SesionLocale === 'PE' && ordenOpciones.IdBovedaCorresponsal !== undefined)
   					ordenOpciones.datosForm.configuracionCuentaCustodio.filtros.IdBoveda.value = ordenOpciones.IdBovedaCorresponsal;
   				/*OPTIMUSPE-1215*/
   			}
   			
   		}
   	});
   	
	$scope.$watch('ordenOpciones.datosOrden.instrumento.IdTipoInstrumento',function(newval,oldval) {
   		if (!newval) {
			ordenOpciones.datosForm.comboMonedaLiquidacionOrden.filtros.IdTipoInstrumento.value = undefined;
			ordenOpciones.datosForm.configuracionBolsa.filtros.IdTipoInstrumento.value = undefined;
			ordenOpciones.datosOrden.idBolsaSeleccionada = undefined;
			ordenOpciones.datosForm.configuracionCanalDistribucion.filtros.IdTipoInstrumento.value = undefined;
   		}
   		else {
			ordenOpciones.datosForm.comboMonedaLiquidacionOrden.filtros.IdTipoInstrumento.value = newval;
			ordenOpciones.datosForm.configuracionBolsa.filtros.IdTipoInstrumento.value = newval;
			ordenOpciones.datosForm.configuracionCanalDistribucion.filtros.IdTipoInstrumento.value = newval;
   		}
	});
	
	$scope.$watch('ordenOpciones.datosForm.ingresoRapido',function(newval,oldval) {
		//NUEVO FORMATO {C/V}{ESPACIO}{CANTIDAD}{NEMO}{SIGNO PESO}{PRECIO}
		//C 5000PLANVITAL$1000
		if (!newval) return;
		var entrada=newval;
		var parms=entrada.split(" ");
		if (parms.length != 2) return;
		if (parms[0].toUpperCase() !== 'C' && parms[0].toUpperCase() !== 'V') return;
		
		var indexNemo = (parms[1].match(/[a-zA-Z]/) || []).index;
		if (indexNemo === undefined) return;
		var indexPrecio = parms[1].indexOf('$');
		if (indexPrecio === undefined) return;
		
		if (parms[0] == "C") { ordenOpciones.datosForm.AccionOrden = 'COMPRA';} 
		if (parms[0] == "V") { ordenOpciones.datosForm.AccionOrden = 'VENTA';} 
		
		ordenOpciones.datosForm.setInstrumento = parms[1].substring(indexNemo,indexPrecio!=-1?indexPrecio:parms[1].length);
		ordenOpciones.datosOrden.cantidadInicial=Number(parms[1].substring(0,indexNemo));
		ordenOpciones.datosOrden.precio = Number(parms[1].substring(indexPrecio +1));
		
	});
	
	$scope.$watch('ordenOpciones.datosForm.calculandoAlgo',function(newval,oldval) {
		ordenOpciones.datosForm.pensando = newval;
	});
	
	$scope.$watch('ordenOpciones.datosOrden.instrumento.IdInstrumento',function(newval,oldval) {
		if (ordenOpciones.datosOrden.instrumento && ordenOpciones.datosOrden.instrumento.IdInstrumento) {
			ordenOpciones.datosForm.decimalesCantidad = util.bcs_formato(0,'ORD','Cantidad',ordenOpciones.datosOrden.instrumento.IdMoneda,'','').cantidadDecimales;
			ordenOpciones.datosForm.decimalesPrecio = util.bcs_formato(0,'ORD','Precio',ordenOpciones.datosOrden.instrumento.IdMoneda,'','').cantidadDecimales;
			ordenOpciones.datosForm.decimalesMonto = util.bcs_formato(0,'ORD','Monto',ordenOpciones.datosOrden.instrumento.IdMoneda,'','').cantidadDecimales;
			ordenOpciones.datosForm.decimalesTipoCambio = util.bcs_formato(0,'ORD','TipoCambio',ordenOpciones.datosOrden.instrumento.IdMoneda,'','').cantidadDecimales;
			
			ordenOpciones.datosForm.comboMonedaLiquidacionOrden.filtros.IdMonedaInstrumento.value = ordenOpciones.datosOrden.instrumento.IdMoneda;
			ordenOpciones.datosForm.comboMonedaLiquidacionOrden.filtros.IdTipoInstrumento.value = ordenOpciones.datosOrden.instrumento.IdTipoInstrumento;
			ordenOpciones.datosForm.tamanioContrato = ordenOpciones.datosOrden.instrumento.Tamano;

			if (ordenOpciones.datosOrden.vencimientoOpcion === undefined || ordenOpciones.datosOrden.vencimientoOpcion.getTime() !== ordenOpciones.datosOrden.instrumento.FechaVencimiento.getTime()) {
				//ordenOpciones.datosOrden.vencimientoOpcion = util.dateDS(ordenOpciones.datosOrden.instrumento.FechaVencimiento,"FechaSistema","CALL");
				ordenOpciones.datosOrden.vencimientoOpcion = ordenOpciones.datosOrden.instrumento.FechaVencimiento;
			}
			
			if (ordenOpciones.datosOrden.instrumentoSubyacente === undefined || ordenOpciones.datosOrden.instrumentoSubyacente.IdInstrumento !== ordenOpciones.datosOrden.instrumento.IdInstrumentoRelacionado) {
				ordenOpciones.datosForm.setIdInstrumentoSubyacente = ordenOpciones.datosOrden.instrumento.IdInstrumentoRelacionado;
			}
			
			if (ordenOpciones.datosOrden.precioStrike === undefined || ordenOpciones.datosOrden.precioStrike !== ordenOpciones.datosOrden.instrumento.PrecioStrike) {
				ordenOpciones.datosOrden.precioStrike = ordenOpciones.datosOrden.instrumento.PrecioStrike;
			}
		}
	});
	
	$scope.$watchGroup(['ordenOpciones.datosForm.AccionOrden','ordenOpciones.datosOrden.instrumento.IdInstrumento'],function(newval,oldval) {
		console.log('entre');
		if (!newval) {
			return;
		}
		
		if (!ordenOpciones.datosOrden.instrumento || !ordenOpciones.datosOrden.instrumento.IdTipoInstrumento) {
			ordenOpciones.datosOrden.operacionSeleccionada = undefined;
			return;
		}
		console.log(idTipoInstrumento);
		var idTipoInstrumento = ordenOpciones.datosOrden.instrumento.IdTipoInstrumento;
		if (!idTipoInstrumento)
			return;
		
		var tipoOperacion = undefined;
		
		var tipoOpciones = ordenOpciones.datosOrden.tipoOpcionesSeleccionado.ValorParametro;
		
		$.each(BCS_OPERACION_INSTRUMENTO,function(key,value){
			if (value.AccionOrden === ordenOpciones.datosForm.AccionOrden && value.IdTipoInstrumento === idTipoInstrumento) {
				if (value.IdTipoInstrumento === 'OPC') {
					if (ordenOpciones.datosOrden.instrumento.EsPutEsCall === value.EsPutEsCall) {
						tipoOperacion = value;	
						return false;
					}
				}
				else {
					tipoOperacion = value;	
					return false;
				}
			}
		});
		console.log('tipoOperacion ' + tipoOperacion)
		ordenOpciones.datosOrden.operacionSeleccionada = tipoOperacion;
		
		

	});
	
	//watch pago cobro cuenta inversion
	$scope.$watch('ordenOpciones.datosOrden.portafolioSeleccionado.IdPortafolio',function(newval,oldval){
		if (newval){
			bcsConfiguracionOrdenes.FUNCIONES.getInformacionPortafolios([ordenOpciones.datosOrden.portafolioSeleccionado.IdPortafolio]).then(function(result){
				
				if (result[0].OperaCuentaInversion === "S") {
					ordenOpciones.datosOrden.pagoCobroCuentaInversion = true;
					ordenOpciones.datosForm.pagoCobroCuentaInversionDisabled = false;
				} else {
					ordenOpciones.datosOrden.pagoCobroCuentaInversion = false;
					ordenOpciones.datosForm.pagoCobroCuentaInversionDisabled = true;
				}
				
				if (result[0].IdPersonaBrokerExtranjero !== undefined && result[0].IdPersonaBrokerExtranjero > 0 ) {
					ordenOpciones.datosForm.paraClienteFinalDisabled = false;
					ordenOpciones.datosOrden.esParaClienteFinal = true;
				} 
				else if (result[0].EsClienteFinal === 'S' ) {
					ordenOpciones.datosForm.paraClienteFinalDisabled = true;
					ordenOpciones.datosOrden.esParaClienteFinal = true;
				}
				else {
					ordenOpciones.datosForm.paraClienteFinalDisabled = true;
					ordenOpciones.datosOrden.esParaClienteFinal = false;
				}
				
			});
			
			ordenOpciones.datosForm.configuracionMecanismoCaptacion = {
				filtros: {
					CodigoTipoIndicador : { "field"   : "CodigoTipoIndicador","operator": "eq", "value"   :  "ORDMODALIDAD" },
					IdEntidad : {"field":"IdEntidad","operator":"eq","value": newval },
				}
			};
			/*OPTIMUSPE-1215*/
			if($scope.SesionLocale === 'PE' && ordenOpciones.IdBovedaCorresponsal !== undefined)
				ordenOpciones.datosForm.configuracionCuentaCustodio.filtros.IdBoveda.value = ordenOpciones.IdBovedaCorresponsal;
			/*OPTIMUSPE-1215*/
		}
	});
	
	//watch forma de pago ultima orden
	$scope.$watchGroup([
		'ordenOpciones.datosOrden.portafolioSeleccionado.IdPortafolio',
		'ordenOpciones.datosOrden.operacionSeleccionada.IdTipoOperacion',
		'ordenOpciones.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion'],function(newval,oldval){
		if (ordenOpciones.datosOrden.portafolioSeleccionado && ordenOpciones.datosOrden.portafolioSeleccionado.IdPortafolio && 
			ordenOpciones.datosOrden.operacionSeleccionada && ordenOpciones.datosOrden.operacionSeleccionada.IdTipoOperacion && 
			ordenOpciones.datosOrden.monedaLiquidacionOrdenSeleccionada && ordenOpciones.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion) {
			
			var datos = {
					p_IdPortafolio				:	ordenOpciones.datosOrden.portafolioSeleccionado.IdPortafolio,
					p_IdTipoOperacion			:	ordenOpciones.datosOrden.operacionSeleccionada.IdTipoOperacion,
					p_IdMonedaLiquidacionOrden	:	ordenOpciones.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion
			}
			
			bcsServicioOrden.ConsultarFormaDePagoUltimaOrden(datos).$promise.then(function(result){
				if (!result.p_out_FormaDePagoOrden[0])
					return;
				
				if (result.p_out_FormaDePagoOrden[0].IdMedioDePago === 'CIN') {
					ordenOpciones.datosForm.IdPreferenciaPagoPortafolio = undefined;
					if (ordenOpciones.datosForm.pagoCobroCuentaInversionDisabled === false) {
						ordenOpciones.datosOrden.pagoCobroCuentaInversion = true;
					}
				}
				else {
					ordenOpciones.datosOrden.pagoCobroCuentaInversion = false;
					ordenOpciones.datosForm.configuracionPreferenciaPagoPortafolio.idPostFiltro = result.p_out_FormaDePagoOrden[0].IdPreferenciaPagoPortafolio;
				}
				
			});
			/*OPTIMUSPE-1215*/
			if($scope.SesionLocale === 'PE' && ordenOpciones.IdBovedaCorresponsal !== undefined)
				ordenOpciones.datosForm.configuracionCuentaCustodio.filtros.IdBoveda.value = ordenOpciones.IdBovedaCorresponsal;
			/*OPTIMUSPE-1215*/
			
		}
	});
	
	$scope.$watch('ordenOpciones.datosOrden.operacionSeleccionada',function(newval,oldval) {
		if (!newval)
			return;
		ordenOpciones.datosForm.configuracionCondicionLiquidacion.filtros.IdTipoOperacion.value = ordenOpciones.datosOrden.operacionSeleccionada.IdTipoOperacion;
		ordenOpciones.datosForm.configuracionPreferenciaPagoPortafolio.filtros.IdTipoOperacion.value = ordenOpciones.datosOrden.operacionSeleccionada.IdTipoOperacion;
		/*OPTIMUSPE-1215*/
		if($scope.SesionLocale === 'PE' && ordenOpciones.IdBovedaCorresponsal !== undefined)
			ordenOpciones.datosForm.configuracionCuentaCustodio.filtros.IdBoveda.value = ordenOpciones.IdBovedaCorresponsal;
		/*OPTIMUSPE-1215*/
	});

	$scope.$watch('ordenOpciones.datosForm.muestraCobros',function(newval,oldval) {
		$timeout(function() {bcsComunes.ventanaTamanoAutomatico(ordenOpciones.datosForm.idVentana);},10);
	});
	
	
	//watch paridades
	$scope.$watchGroup(["ordenOpciones.datosOrden.instrumento.IdInstrumento","ordenOpciones.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion"],function(newval,oldval){
		if (!ordenOpciones.datosOrden.instrumento || !ordenOpciones.datosOrden.instrumento.IdMoneda || !ordenOpciones.datosOrden.monedaLiquidacionOrdenSeleccionada 
			|| !ordenOpciones.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion)
			return;
		var idMonedaOrigen = ordenOpciones.datosOrden.instrumento.IdMoneda;
		var idMonedaDestino = ordenOpciones.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion;
		var datos = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdTrabajo : 0,
			p_CalculosEntrada : [{
				IdTrabajo : 0,
				IdInterno : 0,
				Fecha : $scope.SesionFechaSistema,
				IdMonedaOrigen : idMonedaOrigen,
				IdMonedaDestino : idMonedaDestino,
				ValorCambioLocalModificado : 0,
				MontoOrigen : 1,
				IdUso : 'ORDENES'
			}]
		};
		


		var promise = bcsServiciosPlataformaMercado.CalcularParidadYTipoCambio(datos);
		promise.then(function(result){
			if (result.p_out_CalculosSalida[0].Resultado === 'RECHAZO') {
				ordenOpciones.datosForm.paridadReferencia = $translate.instant("UI_NO_SE_ENCONTRO_PARIDAD") + ":" + idMonedaOrigen + " - " + idMonedaDestino;
			}
			else {
				ordenOpciones.datosForm.paridadReferencia = idMonedaOrigen + " - " + idMonedaDestino + ":" + result.p_out_CalculosSalida[0].FactorOrigenDestino;
				if (idMonedaDestino != $scope.SesionIdMonedaSistema) {
					ordenOpciones.datosForm.paridadReferencia+= "/" + idMonedaOrigen + " - " + $scope.SesionIdMonedaSistema + ":" + result.p_out_CalculosSalida[0].FactorOrigenLocal;
				}
			}
		});
		
	});
	
	$scope.$watchGroup(['ordenOpciones.datosOrden.tipoVigenciaSeleccionada.IdTipoVigenciaOrden'], function(newval,oldval){
		if (ordenOpciones.datosOrden.tipoVigenciaSeleccionada && ordenOpciones.datosOrden.tipoVigenciaSeleccionada.IdTipoVigenciaOrden){
			if (ordenOpciones.datosOrden.tipoVigenciaSeleccionada.IdTipoVigenciaOrden ===  bcsConfiguracionOrdenes.CONSTANTES.IdTipoVigenciaSoloHoy){
				ordenOpciones.datosOrden.fechaVigencia = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
			}
			else if (ordenOpciones.datosOrden.tipoVigenciaSeleccionada.IdTipoVigenciaOrden === bcsConfiguracionOrdenes.CONSTANTES.IdTipoVigenciaPermanente) {
				var datos = {
						p_fechas : [
							{ fechaInicial :$scope.SesionFechaSistema, numeroDias : 30 }
							],
						p_diaHabil : 'N',
						p_soloCaeDiaHabil : 'S',
						p_idPais : $scope.SesionIdPaisSistema
					};
					bcsServicioPlataforma.Servicios().ObtenerDiaSiguiente(datos,function(result) {
						ordenOpciones.datosOrden.fechaVigencia = util.dateDS(result.p_out_fechas[0].fechaCalculada,"FechaSistema","CALL");
						}
					,function(result){
						console.log(result);
						}
					);
			}
			else if (ordenOpciones.datosOrden.tipoVigenciaSeleccionada.IdTipoVigenciaOrden === bcsConfiguracionOrdenes.CONSTANTES.IdTipoVigenciaFechaFija) {
				ordenOpciones.datosOrden.fechaVigencia = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
			}
			else if (ordenOpciones.datosOrden.tipoVigenciaSeleccionada.IdTipoVigenciaOrden === bcsConfiguracionOrdenes.CONSTANTES.IdTipoVigenciaSemanal) {
				var datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo : 'ORD',
					p_UsaReglasNegocio : 'S',
					p_Fecha : $scope.SesionFechaSistema,
					p_NumeroDias : 5,
					p_EsHabil : 'S',
					p_IdPais : $scope.SesionIdPaisSistema
				};
				bcsServicioPlataforma.FechaSiguiente(datos,function(result) {
					ordenOpciones.datosOrden.fechaVigencia = util.dateDS(result.P_OUT_FECHASIGUIENTE,"FechaSistema","CALL");
					}
				,function(result){
					console.log(result);
					}
				);
			}
			else {
				ordenOpciones.datosOrden.fechaVigencia = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
			}
		}
	});

	$scope.$watch('ordenOpciones.datosOrden.instrumentoSubyacente.IdInstrumento',function(newval,oldval){
		if ($scope.ordenOpciones.datosOrden.instrumentoSubyacente) {
			ordenOpciones.datosOrden.precioCierre = ordenOpciones.datosOrden.instrumentoSubyacente.PrecioMercado;
		}
	});
	
	$scope.$watchGroup(['ordenOpciones.datosOrden.precioCierre','ordenOpciones.datosOrden.tipoOpcionesSeleccionado.ValorParametro'],function(newval,oldval){
		if (ordenOpciones.datosOrden.precioCierre && ordenOpciones.datosOrden.tipoOpcionesSeleccionado
			&& ordenOpciones.datosOrden.tipoOpcionesSeleccionado.ValorParametro) {
			
			if (ordenOpciones.datosOrden.tipoOpcionesSeleccionado.ValorParametro === 'PUT') {
				ordenOpciones.datosOrden.precioObjetivo = ordenOpciones.datosOrden.precioCierre * (0.95);	
			}
			if (ordenOpciones.datosOrden.tipoOpcionesSeleccionado.ValorParametro === 'CALL') {
				ordenOpciones.datosOrden.precioObjetivo = ordenOpciones.datosOrden.precioCierre * (1.05);;
			}
			
		}
		
	});
	
	
	$scope.$watchGroup(['ordenOpciones.datosOrden.precioObjetivo','ordenOpciones.datosOrden.precioCierre'],function(newval,oldval){
		if (ordenOpciones.datosOrden.precioObjetivo !== undefined && ordenOpciones.datosOrden.precioCierre !== undefined) {
			var porcentaje = 100*(ordenOpciones.datosOrden.precioObjetivo - ordenOpciones.datosOrden.precioCierre) / ordenOpciones.datosOrden.precioCierre;
			ordenOpciones.datosForm.porcentajeDiferencia = util.bcs_formato(porcentaje,'ORD','Monto','USD','','').valorNumerico + " %";
		}
		
	});
	
	$scope.$watchGroup(['ordenOpciones.datosOrden.tipoOpcionesSeleccionado.ValorParametro',
		'ordenOpciones.datosOrden.instrumentoSubyacente.IdInstrumento',
		'ordenOpciones.datosOrden.vencimientoOpcion',
		'ordenOpciones.datosOrden.precioStrike'],function(newval,oldval) {
		if (!newval) {
			return;
		}
		
		if ($scope.ordenOpciones.datosOrden.tipoOpcionesSeleccionado && $scope.ordenOpciones.datosOrden.instrumentoSubyacente &&
		$scope.ordenOpciones.datosOrden.vencimientoOpcion && $scope.ordenOpciones.datosOrden.precioStrike){
			var day = ("00" + new Date($scope.ordenOpciones.datosOrden.vencimientoOpcion).getDate()).slice(-2);
			var month = ("00" + (new Date($scope.ordenOpciones.datosOrden.vencimientoOpcion).getMonth() + 1)).slice(-2);
			var year = $scope.ordenOpciones.datosOrden.vencimientoOpcion.getYear() - 100;
			var putcall = $scope.ordenOpciones.datosOrden.tipoOpcionesSeleccionado.ValorParametro.substring(0,1);
			var precioEntero = Math.trunc($scope.ordenOpciones.datosOrden.precioStrike);
			var precioDecimal = Math.ceil(((new Big($scope.ordenOpciones.datosOrden.precioStrike) < 1.0) ? new Big($scope.ordenOpciones.datosOrden.precioStrike) : (new Big($scope.ordenOpciones.datosOrden.precioStrike).mod(Math.floor(new Big($scope.ordenOpciones.datosOrden.precioStrike)))) * 1000))
			var nemoInstrumento = $scope.ordenOpciones.datosOrden.instrumentoSubyacente.Nemotecnico.substring(0,4) +
						   year + month + day + putcall + ("00000" + precioEntero).slice(-5) + 
						  ("000" + precioDecimal).slice(-3);
			$scope.ordenOpciones.datosForm.setInstrumento = nemoInstrumento;
		}
	});
	
	ordenOpciones.datosForm.cleanData();
	
	if (parametros.orden) {
		ordenOpciones.datosForm.setInstrumento = parametros.orden.Instrumento;
		if (parametros.orden.AccionOrden === 'COMPRA') {
			ordenOpciones.datosForm.configuracionCompraVenta.defecto = 'VENTA';
		}
		else {
			ordenOpciones.datosForm.configuracionCompraVenta.defecto = 'COMPRA';
		}
		ordenOpciones.datosOrden.cantidadInicial = parametros.orden.CantidadOrden;
		ordenOpciones.datosForm.IdCondicionDeLiquidacion = parametros.orden.IdCondicionDeLiquidacion;
		ordenOpciones.datosForm.configuracionTipoVigencia.defecto = parametros.orden.IdTipoVigenciaOrden;
		if (ordenOpciones.datosForm.configuracionTipoVigencia.defecto === 'FF') {
			ordenOpciones.datosOrden.fechaVigencia = parametros.orden.FechaVigencia; 
		}
		
		ordenOpciones.datosForm.configuracionBolsa.defecto = parametros.orden.IdBolsa;
		console.log(parametros.orden);
		ordenOpciones.datosOrden.vencimientoOpcion = parametros.orden.FechaVencimiento;
		ordenOpciones.datosForm.setInstrumentoSubyacente = parametros.orden.InstrumentoSubyacente;
		ordenOpciones.datosOrden.precioStrike = parametros.orden.PrecioStrike;	
		ordenOpciones.datosForm.configuracionTipoOpcion.defecto = parametros.orden.EsPutEsCall;
	} 
	
	//otros defaults
	else {
		ordenOpciones.datosForm.IdCondicionDeLiquidacion = 'T1';
	}

});
