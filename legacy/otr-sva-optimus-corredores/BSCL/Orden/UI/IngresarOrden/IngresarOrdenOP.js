angular.module('ModuloOrdenes').controller('IngresarOrdenOPCtrl', 
		function($scope,
				OPCIONES_BCSALERTA,
				bcsServicioReportes,
				bcsServiciosPlataformaMercado,
				$translate,
				bcsComunes,
				bcsConfiguracionOrdenesV2,
				bcsServicioOrden,
				bcsServicioPlataforma,
				bcsAlerta,
				$timeout,
				bcsConfiguracionOrdenes){
	
	var ordenOP = this;
	var controllerName = 'IngresarOrdenOPCtrl';
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	var ident = '>> ';
	
	//PARA CONTROLAR INGRESO DE ORDENES NORMALES Y OFERTAS PUBLICas
	ordenOP.modoVentana = 'NORMAL';	//NORMAL, OFERTAPUBLICA
	ordenOP.Familia_Ins = '';
	ordenOP.datosOrden = {};
	ordenOP.datosOrden.DiaVencimiento = "";
	
	ordenOP.datosResumen = {};
	
	
	$scope.$watchGroup(['ordenOP.datosOrden.portafolioSeleccionado.IdPortafolio','ordenOP.datosOrden.operacionSeleccionada.IdTipoOperacion'],function(newval,oldval){
		
		Push('$watchGroup([ordenOP.datosOrden.portafolioSeleccionado.IdPortafolio,ordenOP.datosOrden.operacionSeleccionada.IdTipoOperacion])');
		
		if (ordenOP.datosOrden.portafolioSeleccionado && ordenOP.datosOrden.portafolioSeleccionado.IdPortafolio
				&& ordenOP.datosOrden.operacionSeleccionada && ordenOP.datosOrden.operacionSeleccionada.IdTipoOperacion) {
			
			
			ordenOP.datosOrden.IdTipoOperacion = ordenOP.datosOrden.operacionSeleccionada.IdTipoOperacion;
			
			var datos = {
					p_IdEmpresa 		: $scope.SesionIdEmpresa,
					p_IdUsuario			: $scope.SesionIdUsuario,
					p_IdModulo			: 'ORD',
					p_IdEmpleado		: $scope.SesionIdEmpleado,
					p_IdTipoOperacion	: ordenOP.datosOrden.operacionSeleccionada.IdTipoOperacion,
					P_IdPortafolio		: ordenOP.datosOrden.portafolioSeleccionado.IdPortafolio
			}
			
			bcsServicioOrden.ConsultarRequisitosBasicosCrearOrdenes(datos).$promise.then(function(result){
				if (result.P_OUT_ERRORESCONTADOR > 0) {
					bcsComunes.ventanaMsg({
						conflictos : util.parseServerResponse(result)
					},idVentana);
				}
			});
			
		}
		
		Pop();
	});

	$scope.$watchGroup([
		'ordenOP.datosOrden.portafolioSeleccionado.IdPortafolio',
		'ordenOP.datosOrden.instrumento.IdInstrumento',
		'ordenOP.datosOrden.bolsaSeleccionada.IdBolsa',
	  'ordenOP.datosForm.operacionSalida[0].montoOperacionCalculado',
	//	'ordenOP.datosOrden.montoLiquidacionCalculado'
	],function(newval,oldval){
		
		var operacionesExigibilidad = [];
		var operacionesCobertura    = [];
		
		if (ordenOP.datosOrden.instrumento && ordenOP.datosOrden.portafolioSeleccionado && ordenOP.datosOrden.portafolioSeleccionado.IdPortafolio
				&& ordenOP.datosOrden.bolsaSeleccionada && ordenOP.datosOrden.bolsaSeleccionada.IdBolsa && ordenOP.datosForm.operacionSalida && ordenOP.datosForm.operacionSalida.length>0) {
			
			operacionesExigibilidad.push({
				EsVentaCorta		: false,
				EsDerivado      : true,
				IdTipoInstrumento	: ordenOP.datosOrden.instrumento.IdTipoInstrumento || 0,
				IdBolsa					: ordenOP.datosOrden.bolsaSeleccionada.IdBolsa,
				IdPortafolio		: ordenOP.datosOrden.portafolioSeleccionado.IdPortafolio,
				IdInstrumento		: ordenOP.datosOrden.instrumento.IdInstrumento || 0,
				IdInstrumentoExige	: ordenOP.datosOrden.instrumento.IdInstrumento || 0,
				Cantidad			: 0,
				Precio				: 0,
				Monto 				: ordenOP.datosForm.operacionSalida[0].montoOperacionCalculado || 0,	            				
			});
			
		}
		
		
		if (ordenOP.datosOrden.portafolioSeleccionado && ordenOP.datosOrden.portafolioSeleccionado.IdPortafolio
				&& ordenOP.datosOrden.bolsaSeleccionada && ordenOP.datosOrden.bolsaSeleccionada.IdBolsa
				&& ordenOP.datosOrden.instrumento && ordenOP.datosOrden.instrumento.IdInstrumento) {
			
			var i = ordenOP.datosOrden.instrumento || {};           				
				

			operacionesCobertura.push({
			EsVentaCorta		: false,
			IdBolsa					: ordenOP.datosOrden.bolsaSeleccionada.IdBolsa,
			IdPortafolio		: ordenOP.datosOrden.portafolioSeleccionado.IdPortafolio,
			IdInstrumento		: 0,
			IdInstrumentoExige	: i.IdInstrumento,
			IdTipoInstrumentoExige	: i.IdTipoInstrumento, 
			Cantidad				: 0,
			Precio					: ordenOP.datosOrden.precio || 0,
			Monto 					: 0,
			MonedaDestino		: ordenOP.datosForm.operacionSalida.length!==0? ordenOP.datosForm.operacionSalida[0].idMonedaLiquidacionOrden :""
		});
		
	}
		
		/*OPTIMUSPE-1215*/
		if($scope.SesionLocale === 'PE' && ordenOP.datosOrden.bolsaSeleccionada !== undefined 
				&& ordenOP.datosOrden.idBolsaSeleccionada === 'OUT' 
					&& ordenOP.IdBovedaCorresponsal !== undefined 
						&& ordenOP.datosForm.configuracionCuentaCustodio.filtros.IdBoveda !== undefined)
			ordenOP.datosForm.configuracionCuentaCustodio.filtros.IdBoveda.value = ordenOP.IdBovedaCorresponsal;
		/*OPTIMUSPE-1215*/
			
		if (operacionesExigibilidad.length > 0) 
			ordenOP.datosForm.operacionesExigibilidad = operacionesExigibilidad;	    
		
		if (operacionesCobertura.length > 0) 
			ordenOP.datosForm.operacionesCobertura = operacionesCobertura;
		
	});


	
	$scope.$watchGroup(['ordenOP.datosOrden.portafolioSeleccionado.IdPortafolio',
		'ordenOP.datosOrden.operacionSeleccionada.IdTipoOperacion',
		'ordenOP.datosOrden.formaCustodiaSeleccionada.IdFormaCustodiaOrden'],function(){
		
		Push('$watchGroup([portafolioSeleccionado.IdPortafolio, operacionSeleccionada.IdTipoOperacion, formaCustodiaSeleccionada.IdFormaCustodiaOrden])');
		
		if (ordenOP.datosOrden.portafolioSeleccionado && ordenOP.datosOrden.portafolioSeleccionado.IdPortafolio &&
				ordenOP.datosOrden.operacionSeleccionada && ordenOP.datosOrden.operacionSeleccionada.AccionOrden === 'VENTA' &&
				ordenOP.datosOrden.formaCustodiaSeleccionada && ordenOP.datosOrden.formaCustodiaSeleccionada.IdFormaCustodiaOrden === 'CUS' 
				) {
			ordenOP.datosForm.validaFlujoCustodia = 'S';
		}
		else { //ordenOP.datosForm.AccionOrden
			ordenOP.datosForm.validaFlujoCustodia = 'N';
		}
		
		if (ordenOP.datosOrden.operacionSeleccionada !== undefined ) {
			ordenOP.datosOrden.IdTipoOperacion = ordenOP.datosOrden.operacionSeleccionada.IdTipoOperacion;
		}
		Pop();
	});
	
	//watch cobertura
	$scope.$watchGroup([
						'ordenOP.datosOrden.instrumento.IdInstrumento',
	            		'ordenOP.datosOrden.portafolioSeleccionado.IdPortafolio',
	            		'ordenOP.datosOrden.bolsaSeleccionada.IdBolsa'

	            	],function(newval,oldval){

		
		Push('$watchGroup([instrumento.IdInstrumento, portafolioSeleccionado.IdPortafolio, bolsaSeleccionada.IdBolsa');
		
	            		var operaciones = [];
	            		if (ordenOP.datosOrden.portafolioSeleccionado && ordenOP.datosOrden.portafolioSeleccionado.IdPortafolio
	            				&& ordenOP.datosOrden.bolsaSeleccionada && ordenOP.datosOrden.bolsaSeleccionada.IdBolsa
	            				&& ordenOP.datosOrden.instrumento && ordenOP.datosOrden.instrumento.IdInstrumento) {
	            			
	            			var i = ordenOP.datosOrden.instrumento || {};

	            			operaciones.push({
	            				EsVentaCorta		: false,
	            				IdBolsa				: ordenOP.datosOrden.bolsaSeleccionada.IdBolsa,
	            				IdPortafolio		: ordenOP.datosOrden.portafolioSeleccionado.IdPortafolio,
	            				IdInstrumento		: 0,
	            				IdInstrumentoExige	: i.IdInstrumento || 0,
	            				IdTipoInstrumentoExige	: ordenOP.datosOrden.instrumento.IdTipoInstrumento, 
	            				Cantidad			: 0,
	            				Precio				: 0,
	            				Monto 				: 0,	            				
	            			});
	            			
	            		}
	            		if (operaciones.length > 0) 
	            			ordenOP.datosForm.operacionesCobertura = operaciones;
	    Pop();        		
	            	});


	// Watch para el filtro del objeto instrumento
	$scope.$watchGroup(['ordenOP.datosForm.MercadoSeleccionado.CodigoClasificacion',
					   	'ordenOP.datosForm.FamiliaOp.IdTipoInstrumento',
					   	'ordenOP.datosForm.SubyacenteOp.Nemo'],function(newval,oldval){

		// Tiene Familia seleccionada y esta borrando mercado
		if ( newval[0] === undefined && newval[1] !== undefined && newval[1] > "" ) {
			ordenOP.datosForm.configuracionFamilia.filtros.CodigoClasificacion = "";
			ordenOP.datosForm.configuracionFamilia.defecto = undefined;
			ordenOP.datosForm.configuracionSubyacente.filtros.CodigoClasificacion = "";
			ordenOP.datosForm.configuracionSubyacente.defecto = undefined;
			ordenOP.datosForm.FamiliaOp.IdTipoInstrumento = undefined;
			ordenOP.datosForm.SubyacenteOp.Nemo = undefined;		
			ordenOP.datosForm.cleanInstrumento();
		}

		// Tiene subyacente seleccionado y esta borrando familia
		// Se debe descartar este parrafo cuando se esta ejecutando por asincrono desde la seleccion del instrumento
		// if( newval[1] === undefined && newval[2] !== undefined && newval[2] > "" ) {
		if ( ordenOP.datosForm.FamiliaOp !== undefined 	&& !ordenOP.datosForm.FamiliaOp.IdTipoInstrumento 
			 && ordenOP.datosForm.SubyacenteOp !== undefined && ordenOP.datosForm.SubyacenteOp.Nemo > "" 
			 && newval[2] !== undefined) {
			ordenOP.datosForm.configuracionSubyacente.defecto = undefined;
			ordenOP.datosForm.SubyacenteOp.Nemo = undefined;		
			//ordenOP.datosForm.cleanInstrumento();
		}
	}); 
	
	
	//watch exigibilidad (futuros y venta corta)
	$scope.$watchGroup([
	            		'ordenOP.datosForm.MercadoOp.IdPortafolio',
	            		'ordenOP.datosOrden.instrumento.IdInstrumento',
	            		'ordenOP.datosOrden.bolsaSeleccionada.IdBolsa',
	            		'ordenOP.datosOrden.montoPlazo',
	            		'ordenOP.datosOrden.montoLiquidacionCalculado'
	            	],function(newval,oldval){

		Push('$watchGroup([MercadoOp.IdPortafolio, instrumento.IdInstrumento, bolsaSeleccionada.IdBolsa, montoPlazo, montoLiquidacionCalculado])');
	            		var operaciones = [];
	            		if (ordenOP.datosOrden.instrumento && ordenOP.datosOrden.instrumento.IdTipoInstrumento
	            			&& ordenOP.datosOrden.portafolioSeleccionado && ordenOP.datosOrden.portafolioSeleccionado.IdPortafolio
	            				&& ordenOP.datosOrden.bolsaSeleccionada && ordenOP.datosOrden.bolsaSeleccionada.IdBolsa) {
	            			
	            			operaciones.push({
	            				EsVentaCorta		: false,
	            				IdTipoInstrumento	: ordenOP.datosOrden.instrumento.IdTipoInstrumento || 0,
	            				IdBolsa				: ordenOP.datosOrden.bolsaSeleccionada.IdBolsa,
	            				IdPortafolio		: ordenOP.datosOrden.portafolioSeleccionado.IdPortafolio,
	            				IdInstrumento		: ordenOP.datosOrden.instrumento.IdInstrumento || 0,
	            				IdInstrumentoExige	: ordenOP.datosOrden.instrumento.IdInstrumento || 0,
	            				Cantidad			: 0,
	            				Precio				: 0,
	            				Monto 				: ordenOP.datosOrden.montoLiquidacionCalculado || 0,	            				
	            			});
	            			
	            		}
	            		if (operaciones.length > 0) 
	            			ordenOP.datosForm.operacionesExigibilidad = operaciones;
	    Pop();
	            	});
	
	//watch linea de riesgo
	$scope.$watchGroup([
		'ordenOP.datosOrden.portafolioSeleccionado.IdPortafolio',
		'ordenOP.datosOrden.operacionSeleccionada.IdTipoOperacion',
		'ordenOP.datosOrden.instrumento.IdInstrumento',
		'ordenOP.datosOrden.bolsaSeleccionada.IdBolsa',
		'ordenOP.datosOrden.montoLiquidacionCalculado'
	],function(newval,oldval){

		Push('$watchGroup([portafolioSeleccionado.IdPortafolio, operacionSeleccionada.IdTipoOperacion, instrumento.IdInstrumento, bolsaSeleccionada.IdBolsa, montoLiquidacionCalculado');
		var operaciones = [];
		if (ordenOP.datosOrden.instrumento && ordenOP.datosOrden.instrumento.IdInstrumento 
				&& ordenOP.datosOrden.portafolioSeleccionado && ordenOP.datosOrden.portafolioSeleccionado.IdPortafolio
				&& ordenOP.datosOrden.operacionSeleccionada && ordenOP.datosOrden.operacionSeleccionada.IdTipoOperacion
				&& ordenOP.datosOrden.bolsaSeleccionada && ordenOP.datosOrden.bolsaSeleccionada.IdBolsa) {
			
			ordenOP.datosOrden.IdTipoOperacion = ordenOP.datosOrden.operacionSeleccionada.IdTipoOperacion;
			operaciones.push({
				IdPortafolio		: ordenOP.datosOrden.portafolioSeleccionado.IdPortafolio,
				IdTipoOperacion		: ordenOP.datosOrden.operacionSeleccionada.IdTipoOperacion,
				EsVentaCorta		: false,
				EsODCuentaPropia	: false,
				IdInstrumento		: ordenOP.datosOrden.instrumento.IdInstrumento || 0,
				Monto 				: ordenOP.datosOrden.montoLiquidacionCalculado || 0,
				Tasa				: 0,
				PlazoEnDias			: 0,
				IdBolsa				: ordenOP.datosOrden.bolsaSeleccionada.IdBolsa,
				
			});
			
		}
		if (operaciones.length > 0) 
			ordenOP.datosForm.operacionesLineaRiesgo = operaciones;
		Pop();
	});
	
	
	ordenOP.datosForm = {
			fechaInicial :util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL"),
			fechaLiquidacion : new Date(util.date($scope.SesionFechaSistema).add(1,'days').toDate()),
			decimalesCantidad : 0,
			pensando 			: false,
			calculandoAlgo		: false,
			decimalesPrecio : 0,
			decimalesTasaVentaCorta : 0,
			decimalesMonto : 0,
			decimalesTipoCambio : 0,
			valorAllin : undefined,			
			idVentana : idVentana,
			
			modo : 'INGRESO',
			muestraCobros : false,
			idUso: "ORDEN",
			configuracionSocioNegocio : {
				filtros: {
					IdEmpresa 		: {"field" : "IdEmpresa", "operator" : "eq", "value" : $scope.SesionIdEmpresa},
					EsCliente 		: {"field"   : "EsCliente","operator": "eq", "value"   :  'UI_SI' },
				},
			},
			configuracionCompraVenta : {
				defecto : "COMPRA"
			},
			configuracionPortafolio : {
				filtros: {
					IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  0 },
					//PortafolioDerivados : {"field"   : "PortafolioDerivados","operator": "eq", "value"   :  'S' },
				},
			},
			configuracionLibroOrdenes : { 
				filtros : {
					IdEmpresa			: { field:"IdEmpresa", operator:"eq", value: $scope.SesionIdEmpresa },
					EsOfertaPublica		: { field:"EsLibroOfertaPublica", operator:"eq", value: "S" },
					IdInstrumento		: { field:"IdInstrumento", operator:"eq", value: "" },
				}
			},
			configuracionAutorizadoDarOrdenes : {
				filtros: {
					IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  -1 },
					IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   :  "ACT" }
				},
			},
			configuracionMecanismoCaptacion : {
				filtros: {
					IdPortafolio : { "field"   : "CodigoTipoIndicador","operator": "eq", "value"   :  "ORDMODALIDAD" }
				},
			},
			configuracionCorredorCorresponsal : {
				filtros : {
					IdPais		: { "field"   : "IdPais","operator": "eq", "value"   : "" },
					IdEmpresa	: { "field"   : "IdEmpresa","operator": "eq", "value"   : $scope.SesionIdEmpresa },
				}
			},configuracionBoveda : {
				filtros : {
					IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "VIG" }
				}
			},
			configuracionFormaCustodia : {
				filtros : {
					IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
				}
			},
			configuracionCuentaCustodio : {
				filtros : {
					IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   : "" },
					IdEstadoAutorizacion : { "field"   : "IdEstadoAutorizacion","operator": "eq", "value"   : "AUT" },
					IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
					IdBoveda : { "field"   : "IdBoveda","operator": "eq", "value"   : "" }
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
			configuracionTipoPrecio : {
				defecto : bcsConfiguracionOrdenes.CONSTANTES.IdTipoPrecioMercado,
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
				defecto : "",
				filtros : {
					IdInstrumento : { "field"   : "IdInstrumento","operator": "eq", "value"   : 0 },
					IdPais : { "field"   : "IdPais", "operator": "eq", "value" : $scope.SesionIdPaisSistema },
				}
			},
			configuracionMercado : {
					configuracion : {},
			},
			configuracionFamilia : {
				filtros : {
					CodigoClasificacion : { field : "CodigoClasificacion", operator : "eq", value : ""},
				}
			},
			configuracionTipoOpcion : {
				filtros : {
					IdEstado : { field : "IdEstado", operator : "eq", value : "VIG"},
				}
			},

			configuracionFechaVencimiento : {
                // defines the start view
                start: "year",
                depth: "year",
                format: "MMMM yyyy"
			},			
			configuracionSubyacente : {
				filtros : {
					CodigoClasificacion : { field : "CodigoClasificacion", operator : "eq", value : ""},
					IdTipoInstrumento : { field : "IdTipoInstrumento", operator : "eq", value : ""},
				}
			},
			
			verComprobante : function(){
				bcsConfiguracionOrdenes.FUNCIONES.verComprobante($scope.SesionIdEmpresa,
						$scope.ordenOP.datosResumen.documentoIdentidad,
						$scope.ordenOP.datosResumen.fechaOperacionOrden,
						$scope.ordenOP.datosResumen.secuenciaOrden,
						$scope.ordenOP.datosResumen.montoOperacion,
						
						
						idVentana);
			},
			verDetalle : function() {
				bcsComunes.ventanaAbrir("DetalleOrden", {IdOrden : ordenOP.datosResumen.secuenciaOrden} );
			},
			
    
	};
	
	bcsConfiguracionOrdenes.CONFIGURACION.promise.then(function(result){
		Push('bcsConfiguracionOrdenes.CONFIGURACION.promise.then(function(result)');
		if (bcsConfiguracionOrdenes.CONFIGURACION.IdBovedaDefecto) {
			ordenOP.datosForm.configuracionBoveda.defecto = bcsConfiguracionOrdenes.CONFIGURACION.IdBovedaDefecto;
		}
		Pop();
	});

	
	
	// Filtros Opciones
	$scope.$watchGroup(['ordenOP.datosForm.MercadoSeleccionado',
			'ordenOP.datosForm.FamiliaOp.IdTipoInstrumento',
			'ordenOP.datosForm.SubyacenteOp.Nemo',
			'ordenOP.datosForm.ModoOp.IdMododalidad',
			'ordenOP.datosForm.Estilo.IdEstilo',
			'ordenOP.datosForm.Tipo.IdTipo',
			'ordenOP.datosOrden.fechaVencimiento'],function(newval,oldval){
		
			if ( ordenOP.datosOrden.instrumento !== undefined &&
			     ordenOP.datosForm.MercadoOp !== undefined && ordenOP.datosForm.MercadoOp.CodigoClasificacion &&
			     ordenOP.datosForm.FamiliaOp !== undefined && ordenOP.datosForm.FamiliaOp.IdTipoInstrumento	&& 
			     ordenOP.datosForm.SubyacenteOp !== undefined && ordenOP.datosForm.SubyacenteOp.Nemo && 
			     ordenOP.datosForm.ModoOp !== undefined && ordenOP.datosForm.ModoOp.IdMododalidad &&
			     ordenOP.datosForm.Estilo !== undefined  && ordenOP.datosForm.Estilo.IdEstilo &&
			     ordenOP.datosForm.ModalidadEjercicio !== undefined  && ordenOP.datosForm.ModalidadEjercicio ) { 

			 	 if ( ordenOP.datosForm.MercadoOp.CodigoClasificacion !== ordenOP.datosOrden.instrumento.CodigoClasificacionDV ||
					 ordenOP.datosForm.FamiliaOp.IdTipoInstrumento !== ordenOP.datosOrden.instrumento.IdTipoInstrumento ||
					 ordenOP.datosForm.SubyacenteOp.Nemo !== ordenOP.datosOrden.instrumento.RazonSocial ||
					 ordenOP.datosForm.ModoOp.IdMododalidad !== ordenOP.datosOrden.instrumento.IdModoOpcion ||
					 ordenOP.datosForm.Tipo.IdTipo !== ordenOP.datosOrden.instrumento.IdTipoOpcion ||
					 ordenOP.datosForm.Estilo.IdEstilo !== ordenOP.datosOrden.instrumento.IdEstilo ||
					 ordenOP.datosOrden.fechaVencimiento !== ordenOP.datosOrden.instrumento.FechaVencimiento ||
					 ordenOP.datosForm.ModalidadEjercicio !==  ordenOP.datosOrden.instrumento.ModalidadEjercicio ) {

					ordenOP.datosForm.cleanInstrumento();
					
				};
			};
		
	});
	
	
	//watch operacion cobros
	$scope.$watchGroup(['ordenOP.datosOrden.tipoCambio',
		'ordenOP.datosOrden.precio',
		'ordenOP.datosOrden.portafolioSeleccionado.IdPortafolio',
		'ordenOP.datosOrden.operacionSeleccionada.IdTipoOperacion',
		'ordenOP.datosOrden.bolsaSeleccionada.IdBolsa',
		'ordenOP.datosOrden.cantidadInicial',
		'ordenOP.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion',
		'ordenOP.datosOrden.idCorredorCorresponsal'
		],function(newval,oldval){
	
		
		if (ordenOP.datosOrden.monedaLiquidacionOrdenSeleccionada && ordenOP.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion !== undefined &&
				ordenOP.datosOrden.instrumento && ordenOP.datosOrden.instrumento.IdInstrumento !== undefined &&
				ordenOP.datosOrden.portafolioSeleccionado && ordenOP.datosOrden.portafolioSeleccionado.IdPortafolio !== undefined &&
				ordenOP.datosOrden.operacionSeleccionada && ordenOP.datosOrden.operacionSeleccionada.IdTipoOperacion !== undefined &&
				ordenOP.datosOrden.bolsaSeleccionada && ordenOP.datosOrden.bolsaSeleccionada.IdBolsa !== undefined &&
				ordenOP.datosOrden.precio &&
				ordenOP.datosOrden.cantidadInicial
		) {
			
			ordenOP.datosOrden.IdTipoOperacion = ordenOP.datosOrden.operacionSeleccionada.IdTipoOperacion;
			var operacion = [{
				IdInstrumento  				: ordenOP.datosOrden.instrumento.IdInstrumento,
				FechaOperacion 				: $scope.SesionFechaSistema,
				CantidadOrden   			: ordenOP.datosOrden.cantidadInicial || 0,
				PrecioOrden   				: ordenOP.datosOrden.precio || 0,
				MontoTotal					: ordenOP.datosOrden.montoInicial || 0,
				IdMonedaLiquidacionOrden	: ordenOP.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion,

				IdPortafolio 				: ordenOP.datosOrden.portafolioSeleccionado.IdPortafolio,
				IdTipoProducto 				: ordenOP.datosOrden.operacionSeleccionada.IdTipoProducto,
				IdTipoOperacion 			: ordenOP.datosOrden.operacionSeleccionada.IdTipoOperacion,
				IdCanalIngreso 				: $scope.SesionTipoCanalIngreso,
				IdBolsa 					: ordenOP.datosOrden.bolsaSeleccionada.IdBolsa,
				IdCorredorCorresponsal		: ordenOP.datosOrden.idCorredorCorresponsal,

			}];
			ordenOP.datosForm.operaciones = operacion;
		}
	});	
	//},true);
	

	ordenOP.datosForm.cleanInstrumento = function() {	
		ordenOP.datosForm.setInstrumento = "-1";		
		ordenOP.datosOrden.LotePadron = 0;
		ordenOP.datosForm.ModalidadEjercicio = '';
		ordenOP.datosOrden.Strike = 0;
	};

	
	ordenOP.datosForm.cleanFiltro = function() {
		ordenOP.datosForm.MercadoOp = undefined;

		// Al cargar la 1ra vez la ventana no estan definidas estas variables	
		if (ordenOP.datosForm.FamiliaOp !== undefined ) {
			ordenOP.datosForm.FamiliaOp.IdTipoInstrumento = undefined;
			ordenOP.datosForm.SubyacenteOp.Nemo = undefined;

			ordenOP.datosForm.configuracionFamilia.defecto = undefined;
			//ordenOP.datosForm.configuracionFamilia.filtros.CodigoClasificacion = "";
			ordenOP.datosForm.configuracionSubyacente.defecto = undefined;
			//ordenOP.datosForm.configuracionSubyacente.filtros.CodigoClasificacion = "";
		}

		ordenOP.datosForm.ModoOp = undefined;
		ordenOP.datosForm.Tipo = undefined;
		ordenOP.datosForm.Estilo = undefined;
		ordenOP.datosOrden.fechaVencimiento = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
		ordenOP.datosOrden.LotePadron =  0;
		ordenOP.datosForm.ModalidadEjercicio =  "";
		ordenOP.datosOrden.Strike = 0;
		ordenOP.datosForm.setInstrumento = "-1";		
	};
	
	ordenOP.datosForm.cleanData = function() {
		ordenOP.datosForm.ingresoRapido = undefined;
		ordenOP.datosForm.operacionSalida = [];
		ordenOP.datosForm.operaciones = [];
		ordenOP.datosForm.AccionOrdenDefecto = "COMPRA";
		ordenOP.datosForm.idTipoPrecio = bcsConfiguracionOrdenes.CONSTANTES.IdTipoPrecioMercado;
		ordenOP.datosForm.setInstrumento = "-1";
		ordenOP.datosForm.configuracionPortafolio.idActual = false;
		ordenOP.datosForm.configuracionMecanismoCaptacion.idActual  = false;
		
		ordenOP.datosResumen = {
				fechaOperacionOrden : '',
				secuenciaOrden : '',
				tipoInstrumento : '',
				operacion : '',
				cantidad : '',
				instrumento : '',
				precio : '',
				tipoPrecio : '',
				montoOperacion : '',
				totalCobros : '',
				montoTotal :'',
				documentoIdentidad : '',
				nombreEjecutivoPortafolio : ''
		};

		ordenOP.datosForm.cleanFiltro();
		
		
		ordenOP.datosOrden.tasaVentaCorta 			= undefined;
		ordenOP.datosOrden.precio 					= undefined;
		ordenOP.datosOrden.montoInicial 			= undefined;
		ordenOP.datosOrden.tipoCambio 				= undefined;
		ordenOP.datosOrden.fechaRecepcion 			= undefined;
		ordenOP.datosOrden.cantidadInicial 			= undefined;
		ordenOP.datosOrden.plazoVentaCorta 			= undefined;
		//ordenOP.datosOrden.fechaVencimiento 		= undefined;
		ordenOP.datosOrden.montoPlazo 				= undefined;
		ordenOP.datosOrden.EsVentaCorta 			= false;
		ordenOP.datosOrden.montoOperacionCalculado 	= 0;
		ordenOP.datosOrden.cantidadCalculada 		= 0;
		ordenOP.datosOrden.totalCobros 				= 0;
		ordenOP.datosOrden.totalOrden 				= 0;
		ordenOP.datosOrden.EsVentaCorta 			= false;
		
		if (ordenOP.modoVentana != 'NORMAL') {
			ordenOP.modoVentana = 'NORMAL';
			ordenOP.datosForm.configuracionLibroOrdenes.filtros.IdInstrumento.value = "";
			ordenOP.datosForm.configuracionLibroOrdenes.idPostFiltro = "";
			ordenOP.datosForm.configuracionCompraVenta.activado = true;
			ordenOP.datosForm.instrumentoDeshabilitado = false;
		}
		

		
	};
	
	ordenOP.datosForm.volver = function() {
		ordenOP.datosForm.modo = 'INGRESO';
		ordenOP.datosForm.cleanData();
	};
	
	//cascada-a="ordenOP.datosForm.configuracionCorredorCorresponsal.filtros.IdPais"
	//	cascada-valor="'IdPais'"
	//cascada a corredor corresponsal
	$scope.$watch('ordenOP.datosOrden.idBolsaSeleccionada',function(newval,oldval){
		if ($scope.ordenOP.datosOrden.bolsaSeleccionada 
				&& $scope.ordenOP.datosOrden.idBolsaSeleccionada 
				&& $scope.ordenOP.datosOrden.idBolsaSeleccionada !== 0 
				&& $scope.ordenOP.datosOrden.bolsaSeleccionada.IdPais !== $scope.SesionIdPaisSistema) {
			ordenOP.datosForm.configuracionCorredorCorresponsal.filtros.IdPais.value = $scope.ordenOP.datosOrden.bolsaSeleccionada.IdPais;
			ordenOP.datosForm.configuracionCorredorCorresponsal.requerido = true;
		} else {
			ordenOP.datosForm.configuracionCorredorCorresponsal.filtros.IdPais.value = -1;
			ordenOP.datosForm.configuracionCorredorCorresponsal.requerido = false;
			ordenOP.datosOrden.idCorredorCorresponsal = undefined;
		}
			
	})
	
	ordenOP.datosForm.ingresarOrden = function(a) {
		
		var cobrosModificados = [];
		
		$.each(ordenOP.datosOrden.cobrosModificados,function(key,value) {
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
				    FechaRecepcion : (ordenOP.datosOrden.fechaRecepcion)? util.dateSPseconddate(ordenOP.datosOrden.fechaRecepcion) : '0001-01-01 00:00:00',
					FechaVigencia  : (ordenOP.datosOrden.fechaVigencia)? util.dateSPdate(ordenOP.datosOrden.fechaVigencia) :  '0001-01-01',
					FechaEmision : '0001-01-01',
					FechaOperacion : '0001-01-01', //se calcula en el server
					IdCondicionDeLiquidacion : ordenOP.datosOrden.condicionLiquidacionSeleccionada.IdCondicionDeLiquidacion,
					IdTipoOperacion : ordenOP.datosOrden.operacionSeleccionada.IdTipoOperacion,
					IdTipoVigencia : ordenOP.datosOrden.tipoVigenciaSeleccionada.IdTipoVigenciaOrden,
					Glosa : ordenOP.datosOrden.comentario || '',
					Cantidad : ordenOP.datosForm.operacionSalida[0].cantidadCalculada,
					MontoNeto : 0,
					ValorRescate : 0,
					IdBoveda 		: ordenOP.datosOrden.bovedaSeleccionada.IdBoveda === 0?null:ordenOP.datosOrden.bovedaSeleccionada.IdBoveda,
					IdFormaCustodia : ordenOP.datosOrden.formaCustodiaSeleccionada.IdFormaCustodiaOrden,
					IdPorcentajeVisibilidad : ordenOP.datosOrden.porcentajeVisibilidadSeleccionada.IdPorcentajeVisibilidad,
					PrecioLimite : Number(ordenOP.datosOrden.precio),
					PrecioMercado : Number(ordenOP.datosOrden.instrumento.PrecioMercado),
					EsFacturaInmediata : 'N',
					IdTipoPrecio : ordenOP.datosOrden.tipoPrecioSeleccionado.IdTipoPrecioOrden,
					IdPortafolio : ordenOP.datosOrden.portafolioSeleccionado.IdPortafolio,
					IdTipoCanalIngreso : $scope.SesionTipoCanalIngreso,
					IdTipoReajuste : '',
					IdEmisor: ordenOP.datosOrden.instrumento.IdEmisor,
					IdBolsa : ordenOP.datosOrden.bolsaSeleccionada.IdBolsa,
					EspecificaBolsa : ordenOP.datosOrden.bolsaSeleccionada.IdBolsa === 0? 'N':'S',
					IdMonedaLiquidacionOrden : ordenOP.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion,
					IdMonedaLiquidacionCobros : ordenOP.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion,
					TipoCambio : ordenOP.datosOrden.tipoCambio || null,
					IdInstrumento : ordenOP.datosOrden.instrumento.IdInstrumento,
					IdInstrumentoSubyacente : 0,
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
					IdCuentaCustodioPortafolio : null,
					EsPorMonto : ordenOP.datosOrden.montoInicial !== undefined && ordenOP.datosOrden.montoInicial > 0?"S":"N",
					IdCorredorCorresponsal : ordenOP.datosOrden.idCorredorCorresponsal || null,
					IdPersonaAutorizadoDarOrdenes : ordenOP.datosOrden.autorizadoDarOrdenes.IdPersona || null,
					IdTipoCanalDistribucion : 	ordenOP.datosOrden.idTipoCanalDistribucion || null,
					EsOrdenEspecial : ordenOP.datosOrden.esOrdenEspecial === true?"S":"N",
					PagoCobroCuentaInversion : ordenOP.datosOrden.pagoCobroCuentaInversion === true?"S":"N",
					IdPreferenciaPagoPortafolio : ordenOP.datosOrden.formaDePagoSeleccionada.IdPreferenciaPagoPortafolio,
					BaseTasa : 0,
					TipoInteres : "",
					IdMecanismoCaptacion : ordenOP.datosOrden.mecanismoDeCaptacion?ordenOP.datosOrden.mecanismoDeCaptacion.Codigo:''
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
				ordenOP.datosForm.modo = 'RESUMEN';
				ordenOP.datosResumen = {
						fechaOperacionOrden :	util.dateDS(respuesta.P_OUT_RESULTADO[0].FechaOperacionOrden,"FechaHoraSistema","CALL"),
						secuenciaOrden		:	respuesta.P_OUT_RESULTADO[0].IdOrden,
						tipoInstrumento 	: 	respuesta.P_OUT_RESULTADO[0].NombreTipoInstrumento,
						operacion 			:	respuesta.P_OUT_RESULTADO[0].AccionOrden,
						tipoInstrumento_operacion  : respuesta.P_OUT_RESULTADO[0].AccionOrden + ' ' + respuesta.P_OUT_RESULTADO[0].NombreTipoInstrumento,
						cantidad 			:	respuesta.P_OUT_RESULTADO[0].Cantidad,
						instrumento 		: 	respuesta.P_OUT_RESULTADO[0].Instrumento,
						precio 				: 	Number(respuesta.P_OUT_RESULTADO[0].PrecioOrden),
						esVentaCorta		: 	false,
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
				
				ordenOP.datosResumen.decimales = util.bcs_formato(0,'ORD','Monto',respuesta.P_OUT_RESULTADO[0].IdMonedaLiquidacionOrden,respuesta.P_OUT_RESULTADO[0].IdTipoInstrumento,'').cantidadDecimales;
				ordenOP.datosResumen.decimalesCantidad = util.bcs_formato(0,'ORD','Cantidad',respuesta.P_OUT_RESULTADO[0].IdMonedaLiquidacionOrden,respuesta.P_OUT_RESULTADO[0].IdTipoInstrumento,'').cantidadDecimales;
				ordenOP.datosResumen.decimalesPrecio = util.bcs_formato(0,'ORD','Precio',respuesta.P_OUT_RESULTADO[0].IdMonedaLiquidacionOrden,respuesta.P_OUT_RESULTADO[0].IdTipoInstrumento,'').cantidadDecimales;
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
	
   	$scope.$watch('ordenOP.datosOrden.pagoCobroCuentaInversion',function(newval,oldva){
   		if (newval === true)
   			ordenOP.datosForm.IdPreferenciaPagoPortafolio = false;
   	})-
   	
	$scope.$watch('ordenOP.datosOrden.instrumento.IdTipoInstrumento',function(newval,oldval) {
   		if (!newval) {
			ordenOP.datosForm.comboMonedaLiquidacionOrden.filtros.IdTipoInstrumento.value = undefined;
			ordenOP.datosOrden.idBolsaSeleccionada = undefined;
			ordenOP.datosForm.configuracionCanalDistribucion.filtros.IdTipoInstrumento.value = undefined;
   		}
   		else {
			ordenOP.datosForm.comboMonedaLiquidacionOrden.filtros.IdTipoInstrumento.value = newval;
			ordenOP.datosForm.configuracionCanalDistribucion.filtros.IdTipoInstrumento.value = newval;
   		}
	});
	
	$scope.$watch('ordenOP.datosForm.ingresoRapido',function(newval,oldval) {
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
		
		if (parms[0] == "C") { ordenOP.datosForm.AccionOrden = 'COMPRA';} 
		if (parms[0] == "V") { ordenOP.datosForm.AccionOrden = 'VENTA';} 
				
		if ( ordenOP.datosOrden.instrumento == undefined ||
			 ordenOP.datosOrden.instrumento.Nemotecnico !== parms[1].substring(indexNemo,indexPrecio!=-1?indexPrecio:parms[1].length) ){
				ordenOP.datosForm.cleanFiltro();
				ordenOP.datosForm.setInstrumento = parms[1].substring(indexNemo,indexPrecio!=-1?indexPrecio:parms[1].length);
			 
		}

		ordenOP.datosOrden.cantidadInicial=Number(parms[1].substring(0,indexNemo));
		if (indexPrecio!=-1) {
			ordenOP.datosForm.idTipoPrecio = bcsConfiguracionOrdenes.CONSTANTES.IdTipoPrecioLimite;
			ordenOP.datosOrden.precio = Number(parms[1].substring(indexPrecio +1));
		}
		else {
			ordenOP.datosForm.idTipoPrecio = bcsConfiguracionOrdenes.CONSTANTES.IdTipoPrecioMercado;
			
		}
	});
	
	$scope.$watchGroup(['ordenOP.datosOrden.tipoPrecioSeleccionado.IdTipoPrecioOrden','ordenOP.datosOrden.instrumento.PrecioMercado'],function(newval,oldval) {
		if (ordenOP.datosOrden.instrumento && ordenOP.datosOrden.tipoPrecioSeleccionado) {
			if (ordenOP.datosOrden.tipoPrecioSeleccionado.IdTipoPrecioOrden === bcsConfiguracionOrdenes.CONSTANTES.IdTipoPrecioMercado && ordenOP.datosOrden.instrumento.PrecioMercado) {
				ordenOP.datosOrden.precio = Number(ordenOP.datosOrden.instrumento.PrecioMercado);
			}
		}
	});
	
	$scope.$watch('ordenOP.datosForm.calculandoAlgo',function(newval,oldval) {
		ordenOP.datosForm.pensando = newval;
	});
	
	$scope.$watchGroup(['ordenOP.datosForm.operacionSalida[0].montoOperacionCalculado'],function(newval,oldval){
		if (ordenOP.datosForm.operacionSalida[0] && ordenOP.datosForm.operacionSalida[0].montoOperacionCalculado)
			ordenOP.datosOrden.montoPlazo = bcsComunes.calcularMontoPlazo(Number(ordenOP.datosForm.operacionSalida[0].montoOperacionCalculado),Number(ordenOP.datosOrden.plazoVentaCorta),Number(ordenOP.datosOrden.tasaVentaCorta));
		else ordenOP.datosOrden.montoPlazo = undefined;
	});

	$scope.$watch('ordenOP.datosOrden.instrumento.IdInstrumento',function(newval,oldval) {
		if (ordenOP.datosOrden.instrumento && ordenOP.datosOrden.instrumento.IdInstrumento) {
			ordenOP.datosForm.decimalesCantidad = util.bcs_formato(0,'ORD','Cantidad',ordenOP.datosOrden.instrumento.IdMoneda,'','').cantidadDecimales;
			ordenOP.datosForm.decimalesPrecio = util.bcs_formato(0,'ORD','Precio',ordenOP.datosOrden.instrumento.IdMoneda,'','').cantidadDecimales;
			ordenOP.datosForm.decimalesTasaVentaCorta = util.bcs_formato(0,'ORD','Tasa',ordenOP.datosOrden.instrumento.IdMoneda,'','').cantidadDecimales;
			ordenOP.datosForm.decimalesMonto = util.bcs_formato(0,'ORD','Monto',ordenOP.datosOrden.instrumento.IdMoneda,'','').cantidadDecimales;
			ordenOP.datosForm.decimalesTipoCambio = util.bcs_formato(0,'ORD','TipoCambio',ordenOP.datosOrden.instrumento.IdMoneda,'','').cantidadDecimales;
			
			ordenOP.datosForm.comboMonedaLiquidacionOrden.filtros.IdMonedaInstrumento.value = ordenOP.datosOrden.instrumento.IdMoneda;
			ordenOP.datosForm.comboMonedaLiquidacionOrden.filtros.IdTipoInstrumento.value = ordenOP.datosOrden.instrumento.IdTipoInstrumento;
			ordenOP.datosForm.configuracionBolsa.filtros.IdInstrumento.value = ordenOP.datosOrden.instrumento.IdInstrumento;
			ordenOP.datosForm.configuracionBolsa.defecto = "BCS";

			// $HILE....
			ordenOP.datosForm.MercadoOp.CodigoClasificacion = ordenOP.datosOrden.instrumento.CodigoClasificacionDV;
			//ordenOP.datosForm.configuracionFamilia.filtros.CodigoClasificacion = ordenOP.datosOrden.instrumento.CodigoClasificacionDV;
			ordenOP.datosForm.configuracionFamilia.defecto = ordenOP.datosOrden.instrumento.IdTipoInstrumento;
			//ordenOP.datosForm.configuracionSubyacente.filtros.CodigoClasificacion = ordenOP.datosOrden.instrumento.RazonSocial;
			//ordenOP.datosForm.configuracionSubyacente.defecto = ordenOP.datosOrden.instrumento.RazonSocial;
			ordenOP.datosForm.SubyacenteOp.Nemo = ordenOP.datosOrden.instrumento.RazonSocial;
			ordenOP.datosForm.ModoOp.IdMododalidad = ordenOP.datosOrden.instrumento.IdModoOpcion;
			ordenOP.datosForm.Tipo.IdTipo = ordenOP.datosOrden.instrumento.IdTipoOpcion;
			ordenOP.datosForm.Estilo.IdEstilo = ordenOP.datosOrden.instrumento.IdEstilo;
			ordenOP.datosOrden.LotePadron =  ordenOP.datosOrden.instrumento.LotePadron;
			ordenOP.datosForm.ModalidadEjercicio =  ordenOP.datosOrden.instrumento.ModalidadEjercicio;
			ordenOP.datosOrden.Strike = ordenOP.datosOrden.instrumento.PrecioStrike;
			
			// Por Defecto
			// ordenOP.datosOrden.bolsaSeleccionada.IdBolsa = "BCS";
			// ordenOP.datosForm.configuracionBolsa.defecto = "BCS";
			// ordenOP.datosOrden.bolsaSeleccionada = "BCS";
			// ordenOP.datosOrden.idBolsaSeleccionada = "BCS"
			
			// Configura la fecha de vencimiento
			ordenOP.datosOrden.fechaVencimiento = ordenOP.datosOrden.instrumento.FechaVencimiento;
			switch (ordenOP.datosOrden.instrumento.FechaVencimiento.getDay())
			{
			case 1 : ordenOP.datosForm.DiaVencimiento = " Domingo "; break;
			case 2 : ordenOP.datosForm.DiaVencimiento = " Lunes "; break;
			case 3 : ordenOP.datosForm.DiaVencimiento = " Martes "; break;
			case 4 : ordenOP.datosForm.DiaVencimiento = " Miercoles "; break;
			case 5 : ordenOP.datosForm.DiaVencimiento = " Jueves "; break;
			case 6 : ordenOP.datosForm.DiaVencimiento = " Viernes "; break;
			case 7 : ordenOP.datosForm.DiaVencimiento = " Sabado "; break;
			}
			ordenOP.datosForm.DiaVencimiento += ordenOP.datosOrden.instrumento.FechaVencimiento.getDate();
						
		} 
	});
	
	
	// Opciones
	$scope.$watchGroup(['ordenOP.datosOrden.cantidadInicial','ordenOP.datosOrden.precio','ordenOP.datosOrden.instrumento.IdInstrumento'],function(newval,oldval) {
		if (!newval) {
			return;
		}
		if( ordenOP.datosOrden.precio && ordenOP.datosOrden.cantidadInicial && ordenOP.datosOrden.instrumento !== undefined  ) {
			// ordenOP.datosOrden.instrumento.LotePadron 
			ordenOP.datosOrden.montoInicial = ordenOP.datosOrden.precio * ordenOP.datosOrden.cantidadInicial * ordenOP.datosOrden.instrumento.LotePadron;
		}
	});
	
	
	$scope.$watchGroup(['ordenOP.datosForm.AccionOrden','ordenOP.datosOrden.instrumento.IdInstrumento'],function(newval,oldval) {
		if (!newval) {
			return;
		}
		if (ordenOP.modoVentana === 'OFERTAPUBLICA') {
			return;
		}
		if (!ordenOP.datosOrden.instrumento || !ordenOP.datosOrden.instrumento.IdTipoInstrumento) {
			// ordenOP.datosOrden.operacionSeleccionada = undefined;
			ordenOP.datosOrden.operacionSeleccionada = 'COMOPA';
			return;
		}
		
		var idTipoInstrumento = ordenOP.datosOrden.instrumento.IdTipoInstrumento;
		if (!idTipoInstrumento)
			return;
		
		var tipoOperacion = undefined;
		$.each(BCS_OPERACION_INSTRUMENTO,function(key,value){
			
			if (value.IdTipoInstrumento === idTipoInstrumento && value.AccionOrden === ordenOP.datosForm.AccionOrden) {
				tipoOperacion = value;
				return false;
			}

		})
		
		ordenOP.datosOrden.operacionSeleccionada = tipoOperacion;
		
		if (ordenOP.datosForm.AccionOrden === 'COMPRA')
			ordenOP.datosOrden.EsVentaCorta = false;

	});
	
	//watch pago cobro cuenta inversion
	$scope.$watch('ordenOP.datosOrden.portafolioSeleccionado.IdPortafolio',function(newval,oldval){
		if (newval)
			bcsConfiguracionOrdenes.FUNCIONES.getInformacionPortafolios([ordenOP.datosOrden.portafolioSeleccionado.IdPortafolio]).then(function(result){
				
				if (result[0].OperaCuentaInversion === "S") {
					ordenOP.datosOrden.pagoCobroCuentaInversion = true;
					ordenOP.datosForm.pagoCobroCuentaInversionDisabled = false;
				} else {
					ordenOP.datosOrden.pagoCobroCuentaInversion = false;
					ordenOP.datosForm.pagoCobroCuentaInversionDisabled = true;
				}
			});
	});
	
	//watch forma de pago ultima orden
	$scope.$watchGroup([
		'ordenOP.datosOrden.portafolioSeleccionado.IdPortafolio',
		'ordenOP.datosOrden.operacionSeleccionada.IdTipoOperacion',
		'ordenOP.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion'],function(newval,oldval){
		if (ordenOP.datosOrden.portafolioSeleccionado && ordenOP.datosOrden.portafolioSeleccionado.IdPortafolio && 
			ordenOP.datosOrden.operacionSeleccionada && ordenOP.datosOrden.operacionSeleccionada.IdTipoOperacion && 
			ordenOP.datosOrden.monedaLiquidacionOrdenSeleccionada && ordenOP.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion) {
			
			ordenOP.datosOrden.IdTipoOperacion = ordenOP.datosOrden.operacionSeleccionada.IdTipoOperacion;
			var datos = {
					p_IdPortafolio				:	ordenOP.datosOrden.portafolioSeleccionado.IdPortafolio,
					p_IdTipoOperacion			:	ordenOP.datosOrden.operacionSeleccionada.IdTipoOperacion,
					p_IdMonedaLiquidacionOrden	:	ordenOP.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion
			}
			
			bcsServicioOrden.ConsultarFormaDePagoUltimaOrden(datos).$promise.then(function(result){
				if (!result.p_out_FormaDePagoOrden[0])
					return;
				
				if (result.p_out_FormaDePagoOrden[0].IdMedioDePago === 'CIN') {
					ordenOP.datosForm.IdPreferenciaPagoPortafolio = undefined;
					if (ordenOP.datosForm.pagoCobroCuentaInversionDisabled === false) {
						ordenOP.datosOrden.pagoCobroCuentaInversion = true;
					}
				}
				else {
					ordenOP.datosOrden.pagoCobroCuentaInversion = false;
					ordenOP.datosForm.configuracionPreferenciaPagoPortafolio.idPostFiltro = result.p_out_FormaDePagoOrden[0].IdPreferenciaPagoPortafolio;
				}
				
			});
			
			
		}
	});
	
	$scope.$watch('ordenOP.datosOrden.operacionSeleccionada',function(newval,oldval) {
		if (!newval)
			return;
		ordenOP.datosForm.configuracionCondicionLiquidacion.filtros.IdTipoOperacion.value = ordenOP.datosOrden.operacionSeleccionada.IdTipoOperacion;
		ordenOP.datosForm.configuracionPreferenciaPagoPortafolio.filtros.IdTipoOperacion.value = ordenOP.datosOrden.operacionSeleccionada.IdTipoOperacion;
		
	});

	
	//watch paridades
	$scope.$watchGroup(["ordenOP.datosOrden.instrumento.IdInstrumento","ordenOP.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion"],function(newval,oldval){
		if (!ordenOP.datosOrden.instrumento || !ordenOP.datosOrden.instrumento.IdMoneda || !ordenOP.datosOrden.monedaLiquidacionOrdenSeleccionada 
			|| !ordenOP.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion)
			return;
		var idMonedaOrigen = ordenOP.datosOrden.instrumento.IdMoneda;
		var idMonedaDestino = ordenOP.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion;
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
				ordenOP.datosForm.paridadReferencia = $translate.instant("UI_NO_SE_ENCONTRO_PARIDAD") + ":" + idMonedaOrigen + " - " + idMonedaDestino;
			}
			else {
				ordenOP.datosForm.paridadReferencia = idMonedaOrigen + " - " + idMonedaDestino + ":" + result.p_out_CalculosSalida[0].FactorOrigenDestino;
				if (idMonedaDestino != $scope.SesionIdMonedaSistema) {
					ordenOP.datosForm.paridadReferencia+= "/" + idMonedaOrigen + " - " + $scope.SesionIdMonedaSistema + ":" + result.p_out_CalculosSalida[0].FactorOrigenLocal;
				}
			}
		});
		
	});
	
	
	
	$scope.$watchGroup(['ordenOP.datosOrden.tipoVigenciaSeleccionada.IdTipoVigenciaOrden'], function(newval,oldval){
		if (ordenOP.datosOrden.tipoVigenciaSeleccionada && ordenOP.datosOrden.tipoVigenciaSeleccionada.IdTipoVigenciaOrden){
			if (ordenOP.datosOrden.tipoVigenciaSeleccionada.IdTipoVigenciaOrden ===  bcsConfiguracionOrdenes.CONSTANTES.IdTipoVigenciaSoloHoy){
				ordenOP.datosOrden.fechaVigencia = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
			}
			else if (ordenOP.datosOrden.tipoVigenciaSeleccionada.IdTipoVigenciaOrden === bcsConfiguracionOrdenes.CONSTANTES.IdTipoVigenciaPermanente) {
				ordenOP.datosOrden.fechaVigencia = util.dateDS("0001-01-01","FechaSistema","CALL");
			}
			else if (ordenOP.datosOrden.tipoVigenciaSeleccionada.IdTipoVigenciaOrden === bcsConfiguracionOrdenes.CONSTANTES.IdTipoVigenciaFechaFija) {
				ordenOP.datosOrden.fechaVigencia = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
			}
			else if (ordenOP.datosOrden.tipoVigenciaSeleccionada.IdTipoVigenciaOrden === bcsConfiguracionOrdenes.CONSTANTES.IdTipoVigenciaSemanal) {
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
					ordenOP.datosOrden.fechaVigencia = util.dateDS(result.P_OUT_FECHASIGUIENTE,"FechaSistema","CALL");
					}
				,function(result){
					console.log(result);
					}
				);
			}
			else {
				ordenOP.datosOrden.fechaVigencia = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
			}
		}
	});
	


	var Push = function(xProc){
		ident = ident + '   ';
		console.log(ident + xProc);
	}

	var Pop = function(){
		console.log(ident + 'Filtro.Boveda : ' + ordenOP.datosForm.configuracionBolsa.filtros.IdInstrumento.value || '' );
		console.log(ident + '<RET>');
		ident = ident.substring(0, ident.length-3) ;
	}

	/*var sapoCartera = function() {
	 	if ( $scope.data.gridApiCarteraInstrumento.dataSource.ds._data.length > 0 ) {
	 		console.log(ident + 'Filtro.Boveda : ' + ordenOP.datosForm.configuracionBolsa.filtros.IdInstrumento || '' );
	 	}
	}*/

	
	ordenOP.datosForm.cleanData();
	// Para combo de Liquidacion
	if ( ordenOP.datosOrden.operacionSeleccionada === undefined ) {
		ordenOP.datosOrden.operacionSeleccionada = {}
		ordenOP.datosOrden.operacionSeleccionada.IdTipoOperacion = 'COMOPA';
	};

	if ( ordenOP.datosOrden.condicionLiquidacionSeleccionada === undefined ) {
		ordenOP.datosOrden.condicionLiquidacionSeleccionada = {}
		ordenOP.datosOrden.condicionLiquidacionSeleccionada.IdCondicionDeLiquidacion = 'PM'
	};

	if (parametros.orden) {
		ordenOP.datosForm.setInstrumento = parametros.orden.Instrumento;
		if (parametros.orden.AccionOrden === 'COMPRA') {
			ordenOP.datosForm.configuracionCompraVenta.defecto = 'VENTA';
		}
		else {
			ordenOP.datosForm.configuracionCompraVenta.defecto = 'COMPRA';
		}
		ordenOP.datosOrden.cantidadInicial = parametros.orden.CantidadOrden;
		ordenOP.datosForm.IdCondicionDeLiquidacion = parametros.orden.IdCondicionDeLiquidacion;
		ordenOP.datosForm.configuracionTipoVigencia.defecto = parametros.orden.IdTipoVigenciaOrden;
		if (ordenOP.datosForm.configuracionTipoVigencia.defecto === 'FF') {
			ordenOP.datosOrden.fechaVigencia = parametros.orden.FechaVigencia; 
		}
		ordenOP.datosForm.configuracionTipoPrecio.defecto = parametros.orden.IdTipoPrecio;
		if (ordenOP.datosForm.configuracionTipoPrecio.defecto === "L") {
			ordenOP.datosOrden.precio = parametros.orden.PrecioOrden;
		}
		ordenOP.datosForm.configuracionBolsa.defecto = parametros.orden.IdBolsa;
		ordenOP.datosOrden.EsVentaCorta = parametros.orden.EsVentaCorta === "S"?true: false;
		ordenOP.datosOrden.plazoVentaCorta = parametros.orden.PlazoVentaCorta;
		ordenOP.datosOrden.tasaVentaCorta = parametros.orden.TasaVentaCorta;
		
		
	} 

	$('div#Win_IngresarOrdenOP').css('height','724px')

	$scope.tabClick = function (){
		bcsComunes.ventanaAutoHeight(idVentana);
	}
	
});
