angular.module('ModuloOrdenes').controller('IngresarordenFUCtrl', function($scope,OPCIONES_BCSALERTA,bcsServicioReportes,bcsServiciosPlataformaMercado,$translate,bcsComunes,bcsConfiguracionOrdenesV2,bcsServicioOrden,bcsServicioPlataforma,bcsAlerta,$timeout,bcsConfiguracionOrdenes,bcsConsultasOrdenes){
	
	var ordenFU = this;
	var controllerName = 'IngresarordenFUCtrl';
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	
	//PARA CONTROLAR INGRESO DE ORDENES NORMALES Y OFERTAS PUBLICas
	ordenFU.modoVentana = 'NORMAL';	//NORMAL, OFERTAPUBLICA
	ordenFU.SW = 'N';
	ordenFU.MonedaSistema = $scope.SesionIdMonedaSistema;
	ordenFU.datosOrden = {};
	
	
	ordenFU.datosResumen = {};
	ordenFU.habilitarBoton = true;
	
	$scope.$watchGroup(['ordenFU.datosOrden.portafolioSeleccionado.IdPortafolio','ordenFU.datosOrden.operacionSeleccionada.IdTipoOperacion'],function(newval,oldval){
		
		if (ordenFU.datosOrden.portafolioSeleccionado && ordenFU.datosOrden.portafolioSeleccionado.IdPortafolio
				&& ordenFU.datosOrden.operacionSeleccionada && ordenFU.datosOrden.operacionSeleccionada.IdTipoOperacion) {
			
			var datos = {
					p_IdEmpresa 		: $scope.SesionIdEmpresa,
					p_IdUsuario			: $scope.SesionIdUsuario,
					p_IdModulo			: 'ORD',
					p_IdEmpleado		: $scope.SesionIdEmpleado,
					p_IdTipoOperacion	: ordenFU.datosOrden.operacionSeleccionada.IdTipoOperacion,
					P_IdPortafolio		: ordenFU.datosOrden.portafolioSeleccionado.IdPortafolio
			}
			
			bcsServicioOrden.ConsultarRequisitosBasicosCrearOrdenes(datos).$promise.then(function(result){
				if (result.P_OUT_ERRORESCONTADOR > 0) {
					bcsComunes.ventanaMsg({
						conflictos : util.parseServerResponse(result)
					},idVentana);
				}
			});
			/*OPTIMUSPE-1215*/
			if($scope.SesionLocale === 'PE' && ordenFU.datosOrden.bolsaSeleccionada !== undefined 
					&& ordenFU.datosOrden.idBolsaSeleccionada === 'OUT' 
						&& ordenFU.IdBovedaCorresponsal !== undefined 
							&& ordenFU.datosForm.configuracionCuentaCustodio.filtros.IdBoveda !== undefined)
				ordenFU.datosForm.configuracionCuentaCustodio.filtros.IdBoveda.value = ordenFU.IdBovedaCorresponsal;
			/*OPTIMUSPE-1215*/
			
			ordenFU.datosForm.configuracionPreferenciaPagoPortafolio.filtros.IdTipoOperacion.value = ordenFU.datosOrden.operacionSeleccionada.IdTipoOperacion;
		}
	});
	
	$scope.$watchGroup(['ordenFU.datosOrden.portafolioSeleccionado.IdPortafolio',
		'ordenFU.datosForm.AccionOrden',
		'ordenFU.datosOrden.formaCustodiaSeleccionada.IdFormaCustodiaOrden',
		'ordenFU.datosOrden.EsVentaCorta'],function(){
		
		if (ordenFU.datosForm.AccionOrden==='COMPRA') ordenFU.datosOrden.EsVentaCorta=false;	
		if (ordenFU.datosOrden.portafolioSeleccionado && ordenFU.datosOrden.portafolioSeleccionado.IdPortafolio &&
				ordenFU.datosForm.AccionOrden === 'VENTA' &&
				ordenFU.datosOrden.formaCustodiaSeleccionada && ordenFU.datosOrden.formaCustodiaSeleccionada.IdFormaCustodiaOrden === 'CUS' &&
				ordenFU.datosOrden.EsVentaCorta === false
				) {
			ordenFU.datosForm.validaFlujoCustodia = 'S';
		}
		else { //ordenFU.datosForm.AccionOrden
			ordenFU.datosForm.validaFlujoCustodia = 'N';
		}
		/*OPTIMUSPE-1215*/
		if($scope.SesionLocale === 'PE' && ordenFU.datosOrden.bolsaSeleccionada !== undefined 
				&& ordenFU.datosOrden.idBolsaSeleccionada === 'OUT' 
					&& ordenFU.IdBovedaCorresponsal !== undefined 
						&& ordenFU.datosForm.configuracionCuentaCustodio.filtros.IdBoveda !== undefined)
			ordenFU.datosForm.configuracionCuentaCustodio.filtros.IdBoveda.value = ordenFU.IdBovedaCorresponsal;
		/*OPTIMUSPE-1215*/
	});
	
	/*watch cobertura
	$scope.$watchGroup([
						'ordenFU.datosOrden.instrumento.IdInstrumento',
	            		'ordenFU.datosOrden.portafolioSeleccionado.IdPortafolio',
	            		'ordenFU.datosOrden.EsVentaCorta',
	            		'ordenFU.datosOrden.bolsaSeleccionada.IdBolsa',
	            		'ordenFU.datosOrden.cantidadInicial',
	            		'ordenFU.datosOrden.montoInicial'

	            	],function(newval,oldval){
	            		
	            		var operaciones = [];
	            		if (ordenFU.datosOrden.portafolioSeleccionado && ordenFU.datosOrden.portafolioSeleccionado.IdPortafolio
	            				&& ordenFU.datosOrden.bolsaSeleccionada && ordenFU.datosOrden.bolsaSeleccionada.IdBolsa
	            				&& ordenFU.datosOrden.instrumento && ordenFU.datosOrden.instrumento.IdInstrumento) {
	            			
	            			var i = ordenFU.datosOrden.instrumento || {};
	            			var f= ordenFU.datosResumen;
	            			

	            			operaciones.push({
	            				EsVentaCorta		: ordenFU.datosOrden.EsVentaCorta?ordenFU.datosOrden.EsVentaCorta:false,
	            				IdBolsa				: ordenFU.datosOrden.bolsaSeleccionada.IdBolsa,
	            				IdPortafolio		: ordenFU.datosOrden.portafolioSeleccionado.IdPortafolio,
	            				IdInstrumento		: 0,
	            				IdInstrumentoExige	: i.IdInstrumento || 0,
	            				IdTipoInstrumentoExige	: ordenFU.datosOrden.instrumento.IdTipoInstrumento, 
	            				Cantidad			:  0,
	            				Precio				: i.PrecioMercado || 0,
	            				Monto 				: ordenFU.datosResumen.montoTotal || 0,	            				
	            			});
	            			
	            		}
	            		if (operaciones.length > 0) 
	            			ordenFU.datosForm.operacionesCobertura = operaciones;
	            		
	            	});*/
	
	//watch exigibilidad y cobertura (futuros y venta corta)
	$scope.$watchGroup([
		'ordenFU.datosOrden.portafolioSeleccionado.IdPortafolio',
		'ordenFU.datosOrden.instrumento.IdInstrumento',
		'ordenFU.datosOrden.bolsaSeleccionada.IdBolsa',
	  'ordenFU.datosForm.operacionSalida[0].montoOperacionCalculado',
	//	'ordenFU.datosOrden.montoLiquidacionCalculado'
	],function(newval,oldval){
		
		var operacionesExigibilidad = [];
		var operacionesCobertura    = [];
		
		if (ordenFU.datosOrden.instrumento && ordenFU.datosOrden.portafolioSeleccionado && ordenFU.datosOrden.portafolioSeleccionado.IdPortafolio
				&& ordenFU.datosOrden.bolsaSeleccionada && ordenFU.datosOrden.bolsaSeleccionada.IdBolsa && ordenFU.datosForm.operacionSalida && ordenFU.datosForm.operacionSalida.length>0) {
			
			operacionesExigibilidad.push({
				EsVentaCorta		: false,
				EsDerivado      : true,
				IdTipoInstrumento	: ordenFU.datosOrden.instrumento.IdTipoInstrumento || 0,
				IdBolsa					: ordenFU.datosOrden.bolsaSeleccionada.IdBolsa,
				IdPortafolio		: ordenFU.datosOrden.portafolioSeleccionado.IdPortafolio,
				IdInstrumento		: ordenFU.datosOrden.instrumento.IdInstrumento || 0,
				IdInstrumentoExige	: ordenFU.datosOrden.instrumento.IdInstrumento || 0,
				Cantidad			: 0,
				Precio				: 0,
				Monto 				: ordenFU.datosForm.operacionSalida[0].montoOperacionCalculado || 0,	            				
			});
			
		}
		
		
		if (ordenFU.datosOrden.portafolioSeleccionado && ordenFU.datosOrden.portafolioSeleccionado.IdPortafolio
				&& ordenFU.datosOrden.bolsaSeleccionada && ordenFU.datosOrden.bolsaSeleccionada.IdBolsa
				&& ordenFU.datosOrden.instrumento && ordenFU.datosOrden.instrumento.IdInstrumento) {
			
			var i = ordenFU.datosOrden.instrumento || {};           				
				

			operacionesCobertura.push({
			EsVentaCorta		: false,
			IdBolsa					: ordenFU.datosOrden.bolsaSeleccionada.IdBolsa,
			IdPortafolio		: ordenFU.datosOrden.portafolioSeleccionado.IdPortafolio,
			IdInstrumento		: 0,
			IdInstrumentoExige	: i.IdInstrumento,
			IdTipoInstrumentoExige	: i.IdTipoInstrumento, 
			Cantidad				: 0,
			Precio					: ordenFU.datosOrden.precio || 0,
			Monto 					: 0,
			MonedaDestino		: ordenFU.datosForm.operacionSalida.length!==0? ordenFU.datosForm.operacionSalida[0].idMonedaLiquidacionOrden :""
		});
		
	}
		
		/*OPTIMUSPE-1215*/
		if($scope.SesionLocale === 'PE' && ordenFU.datosOrden.bolsaSeleccionada !== undefined 
				&& ordenFU.datosOrden.idBolsaSeleccionada === 'OUT' 
					&& ordenFU.IdBovedaCorresponsal !== undefined 
						&& ordenFU.datosForm.configuracionCuentaCustodio.filtros.IdBoveda !== undefined)
			ordenFU.datosForm.configuracionCuentaCustodio.filtros.IdBoveda.value = ordenFU.IdBovedaCorresponsal;
		/*OPTIMUSPE-1215*/
			
		if (operacionesExigibilidad.length > 0) 
			ordenFU.datosForm.operacionesExigibilidad = operacionesExigibilidad;	    
		
		if (operacionesCobertura.length > 0) 
			ordenFU.datosForm.operacionesCobertura = operacionesCobertura;
		
	});
	
	//Watch Rut Interno
	$scope.$watchGroup(['ordenFU.datosOrden.portafolioSeleccionado.IdPortafolio','ordenFU.datosOrden.bolsaSeleccionada.IdBolsa'], function(newval,oldval) {
		ordenFU.habilitarBoton = true;
		if($scope.SesionLocale === 'PE') {
			if (newval[0] !== undefined) {
				if (newval[1] !== undefined && newval[1] !== 'FRD') {
					var esRutInterno = ordenFU.datosOrden.portafolioSeleccionado.EsRutInterno;
					if (esRutInterno === 'S') {
						ordenFU.habilitarBoton = false;
						bcsComunes.ventanaMsg({
							conflictos : {
								erroresFatales: [],
			    				errores: [{
			   						Codigo : "",
			   						Descripcion : "Cliente con Rut Interno, seleccione un Mercado OTC."
			   					}],
			   					advertencias: [],
			    				autorizaciones: [],
			    				informaciones: [],
								erroresFatalesContador: 0,
								erroresContador: 1,
								autorizacionesContador: 0,
								advertenciasContador: 0,
								informacionesContador: 0
							}
						},idVentana);
					}
				}
			}
			/*OPTIMUSPE-1215*/
			if($scope.SesionLocale === 'PE' && ordenFU.datosOrden.bolsaSeleccionada !== undefined 
					&& ordenFU.datosOrden.idBolsaSeleccionada === 'OUT'
						&& ordenFU.IdBovedaCorresponsal !== undefined 
							&& ordenFU.datosForm.configuracionCuentaCustodio.filtros.IdBoveda !== undefined)
				ordenFU.datosForm.configuracionCuentaCustodio.filtros.IdBoveda.value = ordenFU.IdBovedaCorresponsal;
			/*OPTIMUSPE-1215*/
		}
	});

	//watch linea de riesgo
	$scope.$watchGroup([
		'ordenFU.datosOrden.portafolioSeleccionado.IdPortafolio',
		'ordenFU.datosOrden.operacionSeleccionada.IdTipoOperacion',
		'ordenFU.datosOrden.EsVentaCorta',
		'ordenFU.datosOrden.instrumento.IdInstrumento',
		'ordenFU.datosOrden.bolsaSeleccionada.IdBolsa',
		'ordenFU.datosOrden.tasaVentaCorta',
		'ordenFU.datosOrden.montoPlazo',
		'ordenFU.datosOrden.plazoVentaCorta',
		'ordenFU.datosOrden.montoLiquidacionCalculado'
	],function(newval,oldval){
		
		var operaciones = [];
		if (ordenFU.datosOrden.instrumento && ordenFU.datosOrden.instrumento.IdInstrumento 
				&& ordenFU.datosOrden.portafolioSeleccionado && ordenFU.datosOrden.portafolioSeleccionado.IdPortafolio
				&& ordenFU.datosOrden.operacionSeleccionada && ordenFU.datosOrden.operacionSeleccionada.IdTipoOperacion
				&& ordenFU.datosOrden.bolsaSeleccionada && ordenFU.datosOrden.bolsaSeleccionada.IdBolsa) {
			
			operaciones.push({
				IdPortafolio		: ordenFU.datosOrden.portafolioSeleccionado.IdPortafolio,
				IdTipoOperacion		: ordenFU.datosOrden.operacionSeleccionada.IdTipoOperacion,
				EsVentaCorta		: ordenFU.datosOrden.EsVentaCorta?ordenFU.datosOrden.EsVentaCorta:false,
				EsODCuentaPropia	: false,
				IdInstrumento		: ordenFU.datosOrden.instrumento.IdInstrumento || 0,
				Monto 				: ordenFU.datosOrden.EsVentaCorta?ordenFU.datosOrden.montoPlazo || 0:ordenFU.datosOrden.montoLiquidacionCalculado || 0,
				Tasa				: ordenFU.datosOrden.EsVentaCorta?ordenFU.datosOrden.tasaVentaCorta || 0:0,
				PlazoEnDias			: ordenFU.datosOrden.EsVentaCorta?ordenFU.datosOrden.plazoVentaCorta || 0:0,
				IdBolsa				: ordenFU.datosOrden.bolsaSeleccionada.IdBolsa,
				
			});
			
			ordenFU.datosForm.configuracionPreferenciaPagoPortafolio.filtros.IdTipoOperacion.value = ordenFU.datosOrden.operacionSeleccionada.IdTipoOperacion;
		}
		if (operaciones.length > 0) 
			ordenFU.datosForm.operacionesLineaRiesgo = operaciones;
		
		/*OPTIMUSPE-1215*/
		if($scope.SesionLocale === 'PE' && ordenFU.datosOrden.bolsaSeleccionada !== undefined 
				&& ordenFU.datosOrden.idBolsaSeleccionada === 'OUT' 
					&& ordenFU.IdBovedaCorresponsal !== undefined 
						&& ordenFU.datosForm.configuracionCuentaCustodio.filtros.IdBoveda !== undefined)
			ordenFU.datosForm.configuracionCuentaCustodio.filtros.IdBoveda.value = ordenFU.IdBovedaCorresponsal;
		/*OPTIMUSPE-1215*/
	});
	
	
	ordenFU.datosForm = {
		fechaInicial :util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL"),
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
			}
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
				CodigoTipoIndicador : { "field" : "CodigoTipoIndicador","operator": "eq", "value" : "ORDMODALIDAD" },
				IdEntidad : { "field" : "IdEntidad", "operator" : "eq", "value": "" }
			},
			itemPostFiltro : '1'
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
			filtros : ($scope.SesionLocale === 'PE')?{
				IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   : "" },
				IdEstadoAutorizacion : { "field"   : "IdEstadoAutorizacion","operator": "neq", "value"   : "RZD" },
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
				IdModeloCustodia : { "field"   : "IdModeloCustodia","operator": "eq", "value"   : "" }
			}: 	
			{
				IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   : "" },
				IdEstadoAutorizacion : { "field"   : "IdEstadoAutorizacion","operator": "neq", "value"   : "RZD" },
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
				//IdModeloCustodia : { "field"   : "IdModeloCustodia","operator": "eq", "value"   : "" }
			}
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
			},
			itemPostFiltro : '1'				
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
			defecto : $scope.SesionBolsaDefectoSistema,
			filtros : {
				IdInstrumento : { "field"   : "IdInstrumento", "operator": "eq", "value"   : 0 },
				
				
			}
		},
		verComprobante : function(){
			bcsConfiguracionOrdenes.FUNCIONES.verComprobante($scope.SesionIdEmpresa,
					$scope.ordenFU.datosResumen.documentoIdentidad,
					$scope.ordenFU.datosResumen.fechaOperacionOrden,
					$scope.ordenFU.datosResumen.secuenciaOrden,
					$scope.ordenFU.datosResumen.montoOperacion,
					idVentana);
		},
		verDetalle : function() {
			bcsComunes.ventanaAbrir("DetalleOrden", {IdOrden : ordenFU.datosResumen.secuenciaOrden} );
		}
	};
	
	if ($scope.SesionLocale === 'PE') {
		ordenFU.datosForm.configuracionBoveda.filtros.IdBoveda = { "field"   : "IdBoveda","operator": "eq", "value"   :  "" };
	}
	
	bcsConfiguracionOrdenes.CONFIGURACION.promise.then(function(result){
		if ($scope.SesionLocale === 'CL'){
			if (bcsConfiguracionOrdenes.CONFIGURACION.IdBovedaDefecto) {
				ordenFU.datosForm.configuracionBoveda.defecto = bcsConfiguracionOrdenes.CONFIGURACION.IdBovedaDefecto;
			}
		};
		
		if (bcsConfiguracionOrdenes.CONFIGURACION.VigenciaDefectoordenFU) {
			ordenFU.datosForm.configuracionTipoVigencia.defecto = bcsConfiguracionOrdenes.CONFIGURACION.VigenciaDefectoordenFU;
		}
	});
	
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
	$scope.$watchGroup(['ordenFU.datosOrden.tipoCambio',
		'ordenFU.datosOrden.precio',
		'ordenFU.datosOrden.portafolioSeleccionado.IdPortafolio',
		'ordenFU.datosOrden.operacionSeleccionada.IdTipoOperacion',
		'ordenFU.datosOrden.bolsaSeleccionada.IdBolsa',
		'ordenFU.datosOrden.montoInicial',
		'ordenFU.datosOrden.cantidadInicial',
		'ordenFU.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion',
		'ordenFU.datosOrden.idCorredorCorresponsal',
		'ordenFU.datosOrden.corredorCorresponsalSeleccionada'
		],function(newval,oldval){
	
		
		if (ordenFU.datosOrden.monedaLiquidacionOrdenSeleccionada && ordenFU.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion !== undefined &&
				ordenFU.datosOrden.instrumento && ordenFU.datosOrden.instrumento.IdInstrumento !== undefined &&
				ordenFU.datosOrden.portafolioSeleccionado && ordenFU.datosOrden.portafolioSeleccionado.IdPortafolio !== undefined &&
				ordenFU.datosOrden.operacionSeleccionada && ordenFU.datosOrden.operacionSeleccionada.IdTipoOperacion !== undefined &&
				ordenFU.datosOrden.bolsaSeleccionada && ordenFU.datosOrden.bolsaSeleccionada.IdBolsa !== undefined &&
				ordenFU.datosOrden.precio &&
				(ordenFU.datosOrden.cantidadInicial || ordenFU.datosOrden.montoInicial)
		) {
			
			var operacion = [{
				IdInstrumento  				: ordenFU.datosOrden.instrumento.IdInstrumento,
				FechaOperacion 				: $scope.SesionFechaSistema,
				CantidadOrden   			: ordenFU.datosOrden.cantidadInicial || 0,
				PrecioOrden   				: ordenFU.datosOrden.precio || 0,
				MontoTotal					: ordenFU.datosOrden.montoInicial || 0,
				IdMonedaLiquidacionOrden	: ordenFU.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion,

				IdPortafolio 				: ordenFU.datosOrden.portafolioSeleccionado.IdPortafolio,
				IdTipoProducto 				: ordenFU.datosOrden.operacionSeleccionada.IdTipoProducto,
				IdTipoOperacion 			: ordenFU.datosOrden.operacionSeleccionada.IdTipoOperacion,
				IdCanalIngreso 				: $scope.SesionTipoCanalIngreso,
				IdBolsa 					: ordenFU.datosOrden.bolsaSeleccionada.IdBolsa,
				IdCorredorCorresponsal		: ordenFU.datosOrden.idCorredorCorresponsal,
				TipoCambio					: ordenFU.datosOrden.tipoCambio

			}];
			ordenFU.datosForm.operaciones = operacion;
			//console.log(ordenFU.datosOrden.nocional);
			//ordenFU.datosOrden.precio = ordenFU.datosOrden.instrumento.Precio;
			//ordenFU.datosOrden.tamano = ordenFU.datosOrden.instrumento.Tamano;
			//ordenFU.datosOrden.nocional = ordenFU.datosOrden.tamano * ordenFU.datosOrden.cantidadInicial
			/*ordenFU.datosOrden.precio = ordenFU.datosOrden.instrumento.Precio;
			ordenFU.datosOrden.tamano = ordenFU.datosOrden.instrumento.Tamano;*/
			ordenFU.datosForm.configuracionPreferenciaPagoPortafolio.filtros.IdTipoOperacion.value = ordenFU.datosOrden.operacionSeleccionada.IdTipoOperacion;
		}
		/* OPTIMUSPE-1215 - Boveda por defecto cuando es Bolsa Extranjera*/	
		if($scope.SesionLocale === 'PE'){
			if(ordenFU.datosOrden.bolsaSeleccionada != undefined && ordenFU.datosOrden.idBolsaSeleccionada === 'OUT' &&
					ordenFU.datosOrden.operacionSeleccionada !== undefined && ordenFU.datosOrden.idCorredorCorresponsal !== undefined &&
						ordenFU.datosOrden.corredorCorresponsalSeleccionada !== undefined){
				var ConsultarCorresponsalBoveda = new bcs.DataSource(defCorresponsalBoveda);
				var filtro = Array();
				filtro.push({ field : "IdCorredorCorresponsal", operator : "eq", value : ordenFU.datosOrden.idCorredorCorresponsal == false ? null : ordenFU.datosOrden.idCorredorCorresponsal});
				filtro.push({ field : "IdTipoProducto", operator : "eq", value : ordenFU.datosOrden.operacionSeleccionada.IdTipoProducto});
				ConsultarCorresponsalBoveda.query({filter: filtro}).then(function(){
					if(ConsultarCorresponsalBoveda.data() != undefined && ConsultarCorresponsalBoveda.data().length > 0 && ConsultarCorresponsalBoveda.data()[0] != undefined){					
						ordenFU.IdBovedaCorresponsal = ConsultarCorresponsalBoveda.data()[0].IdBoveda == null ? "" : ConsultarCorresponsalBoveda.data()[0].IdBoveda;
						ordenFU.datosForm.configuracionCuentaCustodio.filtros.IdBoveda = { "field"   : "IdBoveda","operator": "eq", "value" : ordenFU.IdBovedaCorresponsal };
					}else{
						ordenFU.IdBovedaCorresponsal = "";
					}
				});	
			}	
		}
		/*OPTIMUSPE-1215*/
	});	
	//},true);
	
	
	ordenFU.datosForm.cleanData = function() {
		ordenFU.SW = 'N';		
		ordenFU.datosForm.ingresoRapido = undefined;
		ordenFU.datosForm.operacionSalida = [];
		ordenFU.datosForm.operaciones = [];
		ordenFU.datosForm.AccionOrdenDefecto = "COMPRA";
		ordenFU.datosForm.idTipoPrecio = bcsConfiguracionOrdenes.CONSTANTES.IdTipoPrecioMercado;
		ordenFU.datosForm.setInstrumento = "-1";
		ordenFU.datosForm.configuracionPortafolio.idActual = false;		
		
		ordenFU.datosResumen = {
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
		
		ordenFU.datosOrden.tasaVentaCorta 			= undefined;
		ordenFU.datosOrden.precio 					= undefined;
		ordenFU.datosOrden.montoInicial 			= undefined;
		ordenFU.datosOrden.tipoCambio 				= undefined;
		ordenFU.datosOrden.fechaRecepcion 			= undefined;
		ordenFU.datosOrden.cantidadInicial 			= undefined;
		ordenFU.datosOrden.plazoVentaCorta 			= undefined;
		ordenFU.datosOrden.fechaVencimiento 		= undefined;
		ordenFU.datosOrden.montoPlazo 				= undefined;
		ordenFU.datosOrden.EsVentaCorta 			= false;
		ordenFU.datosOrden.montoOperacionCalculado 	= 0;
		ordenFU.datosOrden.cantidadCalculada 		= 0;
		ordenFU.datosOrden.totalCobros 				= 0;
		ordenFU.datosOrden.totalOrden 				= 0;
		ordenFU.datosOrden.EsVentaCorta 			= false;
		
		if (ordenFU.modoVentana != 'NORMAL') {
			ordenFU.modoVentana = 'NORMAL';
			ordenFU.datosForm.configuracionLibroOrdenes.filtros.IdInstrumento.value = "";
			ordenFU.datosForm.configuracionLibroOrdenes.idPostFiltro = "";
			ordenFU.datosForm.configuracionCompraVenta.activado = true;
			ordenFU.datosForm.instrumentoDeshabilitado = false;
		}
		
		//limpia campos faltantes
		ordenFU.datosForm.IdCondicionDeLiquidacion = "T2";
		ordenFU.datosForm.configuracionTipoVigencia.IdActual = ordenFU.datosForm.configuracionTipoVigencia.defecto;
		ordenFU.datosOrden.fechaLiquidacion = undefined;
		ordenFU.datosForm.AccionOrden = "COMPRA";
		ordenFU.datosForm.ingresoRapidoTemp = undefined;
	};
	
	ordenFU.datosForm.volver = function() {
		ordenFU.datosForm.modo = 'INGRESO';
		ordenFU.datosForm.cleanData();
	};
	


	$scope.$watch('ordenFU.datosOrden.idBolsaSeleccionada',function(newval,oldval) {
   		if (ordenFU.datosOrden.idBolsaSeleccionada) {
   			if ((ordenFU.datosOrden.bolsaSeleccionada.EsExtranjera === 'N' || ordenFU.datosOrden.bolsaSeleccionada.UsaBovedaLocal ==='S') &&  ($scope.SesionLocale === 'PE')) {
   				ordenFU.datosForm.configuracionCuentaCustodio.filtros.IdModeloCustodia.value = "LOCAL";
   			}
   			else if (ordenFU.datosOrden.bolsaSeleccionada.EsExtranjera === 'S'  &&  ($scope.SesionLocale === 'PE')) {
   				ordenFU.datosForm.configuracionCuentaCustodio.filtros.IdModeloCustodia.value = "EXTERIOR";
   				/*OPTIMUSPE-1215*/
   				if($scope.SesionLocale === 'PE' && ordenFU.datosOrden.bolsaSeleccionada !== undefined 
   						&& ordenFU.datosOrden.idBolsaSeleccionada === 'OUT' 
   							&& ordenFU.IdBovedaCorresponsal !== undefined 
   								&& ordenFU.datosForm.configuracionCuentaCustodio.filtros.IdBoveda !== undefined)
   					ordenFU.datosForm.configuracionCuentaCustodio.filtros.IdBoveda.value = ordenFU.IdBovedaCorresponsal;
   				/*OPTIMUSPE-1215*/
   			}
   			
   		}
   	});
   	


	ordenFU.datosForm.ingresarOrden = function(a) {
		
		var cobrosModificados = [];
		
		$.each(ordenFU.datosOrden.cobrosModificados,function(key,value) {
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
				    FechaRecepcion : (ordenFU.datosOrden.fechaRecepcion)? util.dateSPseconddate(ordenFU.datosOrden.fechaRecepcion) : '0001-01-01 00:00:00',
					FechaVigencia  : (ordenFU.datosOrden.fechaVigencia)? util.dateSPdate(ordenFU.datosOrden.fechaVigencia) :  '0001-01-01',
					FechaEmision : '0001-01-01',
					FechaOperacion : '0001-01-01', //se calcula en el server
					IdCondicionDeLiquidacion : ordenFU.datosOrden.condicionLiquidacionSeleccionada.IdCondicionDeLiquidacion,
					IdTipoOperacion : ordenFU.datosOrden.operacionSeleccionada.IdTipoOperacion,
					IdTipoVigencia : ordenFU.datosOrden.tipoVigenciaSeleccionada.IdTipoVigenciaOrden,
					Glosa : ordenFU.datosOrden.comentario || '',
					Cantidad : ordenFU.datosForm.operacionSalida[0].cantidadCalculada,
					MontoNeto : 0,
					ValorRescate : 0,
					IdBoveda 		: ordenFU.datosOrden.bovedaSeleccionada.IdBoveda,
					IdFormaCustodia : ordenFU.datosOrden.formaCustodiaSeleccionada.IdFormaCustodiaOrden,
					IdPorcentajeVisibilidad : ordenFU.datosOrden.porcentajeVisibilidadSeleccionada.IdPorcentajeVisibilidad,
					PrecioLimite : Number(ordenFU.datosOrden.precio),
					PrecioMercado : Number(ordenFU.datosOrden.instrumento.PrecioMercado),
					EsFacturaInmediata : 'N',
					IdTipoPrecio : ordenFU.datosOrden.tipoPrecioSeleccionado.IdTipoPrecioOrden,
					IdPortafolio : ordenFU.datosOrden.portafolioSeleccionado.IdPortafolio,
					IdTipoCanalIngreso : $scope.SesionTipoCanalIngreso,
					IdTipoReajuste : '',
					IdEmisor: ordenFU.datosOrden.instrumento.IdEmisor,
					IdBolsa : ordenFU.datosOrden.bolsaSeleccionada.IdBolsa,
					EspecificaBolsa : ordenFU.datosOrden.bolsaSeleccionada.IdBolsa === 0? 'N':'S',
					IdMonedaLiquidacionOrden : ordenFU.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion,
					IdMonedaLiquidacionCobros : ordenFU.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion,
					TipoCambio : ordenFU.datosOrden.tipoCambio || null,
					IdInstrumento : ordenFU.datosOrden.instrumento.IdInstrumento,
					IdInstrumentoSubyacente : 0,
					IdCondicionDeLiquidacionPlazo : '',
					TasaVentaCorta : (ordenFU.datosOrden.EsVentaCorta)? ordenFU.datosOrden.tasaVentaCorta:0,
					PlazoVentaCorta : (ordenFU.datosOrden.EsVentaCorta)? ordenFU.datosOrden.plazoVentaCorta:0,
					EsVentaCorta : ordenFU.datosOrden.EsVentaCorta? 'S': 'N',
					TasaSimultanea : 0,
					PlazoSimultanea : 0,
					PlazoIF : 0,
					TasaRFIF : 0,
					EsOD: 'N',
					EsFungible : 'N',
					MontoPlazo : 0,
					IdTipoLibroOrden : $scope.SesionLocale === 'PE' && ordenFU.modoVentana === 'OFERTAPUBLICA' ? ordenFU.datosForm.configuracionLibroOrdenes.seleccionado.IdTipoLibroOrden : 'NORMAL',
					IdLibroOrdenes : $scope.SesionLocale === 'PE' && ordenFU.modoVentana === 'OFERTAPUBLICA' ? ordenFU.datosForm.configuracionLibroOrdenes.seleccionado.IdLibroOrdenes : null,
					IdCuentaCustodioPortafolio : ordenFU.datosOrden.cuentaCustodioSeleccionada? ordenFU.datosOrden.cuentaCustodioSeleccionada.IdCuentaCustodioPortafolio || null:null,
					EsPorMonto : ordenFU.datosOrden.montoInicial !== undefined && ordenFU.datosOrden.montoInicial > 0?"S":"N",
					IdCorredorCorresponsal : ordenFU.datosOrden.idCorredorCorresponsal || null,
					IdPersonaAutorizadoDarOrdenes : ordenFU.datosOrden.autorizadoDarOrdenes.IdPersona || null,
					IdTipoCanalDistribucion : 	ordenFU.datosOrden.idTipoCanalDistribucion || null,
					EsOrdenEspecial : ordenFU.datosOrden.esOrdenEspecial === true?"S":"N",
					PagoCobroCuentaInversion : ordenFU.datosOrden.pagoCobroCuentaInversion === true?"S":"N",
					IdPreferenciaPagoPortafolio : ordenFU.datosOrden.formaDePagoSeleccionada.IdPreferenciaPagoPortafolio,
					BaseTasa : 0,
					TipoInteres : "",
					EsClienteFinal : ordenFU.datosOrden.esParaClienteFinal === true?"S":"N",
					IdMecanismoCaptacion : ordenFU.datosOrden.mecanismoDeCaptacion?ordenFU.datosOrden.mecanismoDeCaptacion.ValorIndicador:'',
					FechaConfirmacionPlazo : '0001-01-01', //se calcula en el server
					IngresoDesde : "COR"
				}],
				p_CobrosModificados : cobrosModificados
				
		};
		
		if ($scope.SesionLocale === 'PE' && ordenFU.datosOrden.cuentaCustodioSeleccionada) {
			datosIngresoOrden.p_Ordenes[0].IdCuentaCustodioPortafolio = ordenFU.datosOrden.cuentaCustodioSeleccionada.IdCuentaCustodioPortafolio || null;
		}

		bcsComunes.ventanaOverlay(idVentana,true);
		var promise = bcsServicioOrden.CrearOrdenes(datosIngresoOrden).$promise;
		promise.then(function(respuesta){
   			
   			bcsComunes.ventanaOverlay(idVentana,false);
	   		
			//se valido la orden
			if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_RESULTADO[0].IdOrden > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0) 
			{
				ordenFU.datosForm.modo = 'RESUMEN';
				ordenFU.datosResumen = {
						fechaOperacionOrden :	util.dateDS(respuesta.P_OUT_RESULTADO[0].FechaOperacionOrden,"FechaHoraSistema","CALL"),
						secuenciaOrden		:	respuesta.P_OUT_RESULTADO[0].IdOrden,
						tipoInstrumento 	: 	respuesta.P_OUT_RESULTADO[0].NombreTipoInstrumento,
						operacion 			:	respuesta.P_OUT_RESULTADO[0].AccionOrden,
						tipoInstrumento_operacion  : respuesta.P_OUT_RESULTADO[0].AccionOrden + ' ' + respuesta.P_OUT_RESULTADO[0].NombreTipoInstrumento,
						cantidad 			:	respuesta.P_OUT_RESULTADO[0].Cantidad,
						instrumento 		: 	respuesta.P_OUT_RESULTADO[0].Instrumento,
						precio 				: 	Number(respuesta.P_OUT_RESULTADO[0].PrecioOrden),
						esVentaCorta		: 	respuesta.P_OUT_RESULTADO[0].EsVentaCorta === 'S'?true:false,
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
				
				ordenFU.datosResumen.decimales = util.bcs_formato(0,'GEN','Monto',respuesta.P_OUT_RESULTADO[0].IdMonedaLiquidacionOrden,respuesta.P_OUT_RESULTADO[0].IdTipoInstrumento,'').cantidadDecimales;
				ordenFU.datosResumen.decimalesCantidad = util.bcs_formato(0,'GEN','Cantidad',respuesta.P_OUT_RESULTADO[0].IdMonedaLiquidacionOrden,respuesta.P_OUT_RESULTADO[0].IdTipoInstrumento,'').cantidadDecimales;
				ordenFU.datosResumen.decimalesPrecio = util.bcs_formato(0,'GEN','Precio',respuesta.P_OUT_RESULTADO[0].IdMonedaLiquidacionOrden,respuesta.P_OUT_RESULTADO[0].IdTipoInstrumento,'').cantidadDecimales;
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
	
   	$scope.$watch('ordenFU.datosOrden.pagoCobroCuentaInversion',function(newval,oldva){
   		if (newval === true)
   			ordenFU.datosForm.IdPreferenciaPagoPortafolio = false;
   	});

	$scope.$watch('ordenFU.datosOrden.instrumento.IdTipoInstrumento',function(newval,oldval) {
   		if (!newval) 
   		{
			
			ordenFU.datosForm.comboMonedaLiquidacionOrden.filtros.IdMonedaInstrumento.value = undefined;
			ordenFU.datosForm.comboMonedaLiquidacionOrden.filtros.IdTipoInstrumento.value = undefined;
			ordenFU.datosForm.configuracionCanalDistribucion.filtros.IdTipoInstrumento.value = undefined;
   		}
   		else 
   		{	
			//console.log(ordenFU.datosForm.configuracionBolsa);
			ordenFU.datosOrden.precio = ordenFU.datosOrden.instrumento.Precio;
			ordenFU.datosOrden.cantidadInicial = 1;			
			ordenFU.datosOrden.tamano = ordenFU.datosOrden.instrumento.Tamano;
			ordenFU.datosOrden.nocional = ordenFU.datosOrden.tamano * 10;

			ordenFU.datosForm.comboMonedaLiquidacionOrden.filtros.IdMonedaInstrumento.value = ordenFU.datosOrden.instrumento.IdMoneda;
			ordenFU.datosForm.comboMonedaLiquidacionOrden.filtros.IdTipoInstrumento.value = newval;
			ordenFU.datosForm.configuracionCanalDistribucion.filtros.IdTipoInstrumento.value = newval;
			//ordenFU.datosForm.configuracionBolsa.defecto =  'BCS';
   		}
	});
	
	$scope.$watch('ordenFU.datosForm.ingresoRapido',function(newval,oldval) {
		//NUEVO FORMATO {C/V}{ESPACIO}{CANTIDAD}{NEMO}{SIGNO PESO}{PRECIO}
		//C 5000PLANVITAL$1000
		if (!newval) {
			ordenFU.SW = 'N';
			return;
		}

		var entrada=newval;
		var parms=entrada.split(" ");
		if (parms.length != 2) return;
		if (parms[0].toUpperCase() !== 'C' && parms[0].toUpperCase() !== 'V') return;
		
		var indexNemo = (parms[1].match(/[a-zA-Z]/) || []).index;
		if (indexNemo === undefined) return;
		var indexPrecio = parms[1].indexOf('$');
		
		if (parms[0] == "C") { ordenFU.datosForm.AccionOrden = 'COMPRA';} 
		if (parms[0] == "V") { ordenFU.datosForm.AccionOrden = 'VENTA';} 
		
		ordenFU.datosForm.setInstrumento = parms[1].substring(indexNemo,indexPrecio!=-1?indexPrecio:parms[1].length);
		ordenFU.datosOrden.cantidadInicial=Number(parms[1].substring(0,indexNemo));
		if (indexPrecio!=-1) {
			ordenFU.datosForm.idTipoPrecio = bcsConfiguracionOrdenes.CONSTANTES.IdTipoPrecioLimite;
			ordenFU.datosOrden.precio = Number(parms[1].substring(indexPrecio +1));
		}
		else {
			ordenFU.datosForm.idTipoPrecio = bcsConfiguracionOrdenes.CONSTANTES.IdTipoPrecioMercado;
			
		}	
		
		});
	
	$scope.$watchGroup(['ordenFU.datosOrden.tipoPrecioSeleccionado.IdTipoPrecioOrden','ordenFU.datosOrden.instrumento.PrecioMercado'],function(newval,oldval) {
		
		if (ordenFU.datosOrden.instrumento && ordenFU.datosOrden.tipoPrecioSeleccionado) {
			
			if (ordenFU.datosOrden.tipoPrecioSeleccionado.IdTipoPrecioOrden === bcsConfiguracionOrdenes.CONSTANTES.IdTipoPrecioMercado) {
				ordenFU.datosOrden.precio = Number(ordenFU.datosOrden.instrumento.PrecioMercado);
			}
		}
	});
	
	$scope.$watch('ordenFU.datosForm.calculandoAlgo',function(newval,oldval) {
		ordenFU.datosForm.pensando = newval;
	});
	
	$scope.$watchGroup(['ordenFU.datosOrden.tasaVentaCorta','ordenFU.datosOrden.plazoVentaCorta','ordenFU.datosForm.operacionSalida[0].montoOperacionCalculado'],function(newval,oldval){
		if (ordenFU.datosOrden.tasaVentaCorta && ordenFU.datosOrden.plazoVentaCorta && ordenFU.datosForm.operacionSalida[0] && ordenFU.datosForm.operacionSalida[0].montoOperacionCalculado)
			ordenFU.datosOrden.montoPlazo = bcsComunes.calcularMontoPlazo(Number(ordenFU.datosForm.operacionSalida[0].montoOperacionCalculado),Number(ordenFU.datosOrden.plazoVentaCorta),Number(ordenFU.datosOrden.tasaVentaCorta));
		else ordenFU.datosOrden.montoPlazo = undefined;
	});
	
	/*
	$scope.$watchGroup(['ordenFU.datosOrden.instrumento.IdInstrumento','ordenFU.datosOrden.portafolioSeleccionado.IdPortafolio','ordenFU.datosOrden.formaCustodiaSeleccionada.IdFormaCustodiaOrden'],function(newval,oldval) {
		if (ordenFU.datosOrden.portafolioSeleccionado && ordenFU.datosOrden.portafolioSeleccionado.IdPortafolio
				&& ordenFU.datosOrden.instrumento && ordenFU.datosOrden.instrumento.IdInstrumento) {
			
			bcsConfiguracionOrdenes.FUNCIONES.getInformacionPortafolios([ordenFU.datosOrden.portafolioSeleccionado.IdPortafolio]).then(function(result){
				var indicadores = result[0];
				var mercado = ordenFU.datosOrden.instrumento.IdClasificacionTipoInstrumento;
				if (ordenFU.datosOrden.formaCustodiaSeleccionada.IdFormaCustodiaOrden==="CUS") {
					if(mercado === "RV" && indicadores.OperaCustodiaRV === "S" ){
						ordenFU.OperaFormaCustodia =true;
					}
					else{
						ordenFU.OperaFormaCustodia=false;
						bcsComunes.msg("error",'Error',$translate.instant('UI_PORTAFOLIO_NO_OPERA_CUSTODIA'));
					}
					//ordenFU.datosForm.configuracionFormaCustodia.idActual = "CUS";
				}
				else if (ordenFU.datosOrden.formaCustodiaSeleccionada.IdFormaCustodiaOrden==="NOM") {
					 if(mercado === "RV" && indicadores.PermiteVenderNominalRV==="S"){
						 ordenFU.OperaFormaCustodia=true;
					 }
					 else{
						 ordenFU.OperaFormaCustodia=false;
						 bcsComunes.msg("error",'Error',$translate.instant('UI_PORTAFOLIO_NO_OPERA_CUSTODIA_NOMINAL'));						 
					 }
					//ordenFU.datosForm.configuracionFormaCustodia.idActual = "NOM";
						 
				}
								
			});
			
		}
			
	});*/
	
	
	//Validad indicador Opera custodia RV
	$scope.$watch('ordenFU.datosOrden.portafolioSeleccionado.IdPortafolio',function(newval,oldval) {
		
		if(ordenFU.datosOrden.portafolioSeleccionado){
			bcsConfiguracionOrdenes.FUNCIONES.getInformacionPortafolios([ordenFU.datosOrden.portafolioSeleccionado.IdPortafolio]).then(function(result){
				var indicadores = result[0];
				if(indicadores.OperaCustodiaRV === "S"){
					ordenFU.datosForm.configuracionFormaCustodia.idActual = "CUS";
				}else{
					ordenFU.datosForm.configuracionFormaCustodia.idActual = "NOM";
				}
			});
		}
	});
	
	
	
	$scope.$watch('ordenFU.datosOrden.instrumento.IdInstrumento',function(newval,oldval) {
		if (ordenFU.datosOrden.instrumento && ordenFU.datosOrden.instrumento.IdInstrumento) {
			ordenFU.datosForm.decimalesCantidad = util.bcs_formato(0,'GEN','Cantidad',ordenFU.datosOrden.instrumento.IdMoneda,'','').cantidadDecimales;
			ordenFU.datosForm.decimalesPrecio = util.bcs_formato(0,'GEN','Precio',ordenFU.datosOrden.instrumento.IdMoneda,'','').cantidadDecimales;
			ordenFU.datosForm.decimalesTasaVentaCorta = util.bcs_formato(0,'GEN','Tasa',ordenFU.datosOrden.instrumento.IdMoneda,'','').cantidadDecimales;
			ordenFU.datosForm.decimalesMonto = util.bcs_formato(0,'GEN','Monto',ordenFU.datosOrden.instrumento.IdMoneda,'','').cantidadDecimales;
			ordenFU.datosForm.decimalesTipoCambio = util.bcs_formato(0,'GEN','TipoCambio',ordenFU.datosOrden.instrumento.IdMoneda,'','').cantidadDecimales;			
			ordenFU.datosForm.configuracionBolsa.filtros.IdInstrumento.value = ordenFU.datosOrden.instrumento.IdInstrumento;
			
			if ($scope.SesionLocale === 'PE') {
				var ds = new bcs.DataSource(bcsConfiguracionOrdenesV2.getDataSourceConsultarLibroOrdenes());
				ds.fetch().then(function(){
					var libros = ds.data();
					var libroEncontrado = undefined;
					$.each(libros,function(key,value){
						if (value.EsLibroOfertaPublica === "S" && value.IdEstado === "ABT" && value.IdInstrumento === ordenFU.datosOrden.instrumento.IdInstrumento) {
							libroEncontrado = value;
							return false;
						}
					});
					if (libroEncontrado) {
						var t = bcsAlerta.crear('PREGUNTA',$translate.instant('UI_EXISTE_OFERTA_PUBLICA_ASOCIADA_INSTRUMENTO'),idVentana);
						//CARGAR FORMULARIO OFERTA PUBLICA
						t.then(function(result){
							if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
								ordenFU.modoVentana = 'OFERTAPUBLICA';
								ordenFU.datosForm.configuracionLibroOrdenes.filtros.IdInstrumento.value = libroEncontrado.IdInstrumento;
								ordenFU.datosForm.configuracionLibroOrdenes.idPostFiltro = libroEncontrado.IdLibroOrdenes;
								ordenFU.datosForm.AccionOrden = libroEncontrado.AccionOrden;
								ordenFU.datosForm.configuracionCompraVenta.activado = false;
								ordenFU.datosOrden.EsVentaCorta = false;
								ordenFU.datosForm.instrumentoDeshabilitado = true;

								$.each(BCS_OPERACION_INSTRUMENTO.LISTA,function(key,value){
									if (value.IdTipoOperacion === libroEncontrado.IdTipoOperacion) {
										ordenFU.datosOrden.operacionSeleccionada = value;
										return false;
									}

								})
							} 
						})//FIN PROMESA
					}
					
				},function(result){
					
				});
		  		
			}
			
			if($scope.ordenFU.datosOrden.instrumento.IdTipoInstrumento ==='MIL' || $scope.ordenFU.datosOrden.instrumento.IdTipoInstrumento ==='ACX'){
				$scope.ordenFU.datosForm.IdCondicionDeLiquidacion = "T3";	
			}else{
				$scope.ordenFU.datosForm.IdCondicionDeLiquidacion = "T2";
			}
			
		}
	});
	
	$scope.$watchGroup(['ordenFU.datosOrden.cantidadInicial', 'ordenFU.datosOrden.precio'],function(newval,oldval){
		ordenFU.datosOrden.montoInicial = ordenFU.datosOrden.precio * ordenFU.datosOrden.cantidadInicial * ordenFU.datosOrden.tamano;
		ordenFU.datosOrden.nocional = ordenFU.datosOrden.cantidadInicial * ordenFU.datosOrden.tamano;
		

	});
	$scope.$watch('ordenFU.datosOrden.montoInicial',function(newval,oldval){
	
		/*if (newval && !oldval) {
			ordenFU.datosOrden.cantidadInicial = undefined;
		}*/

	});
	
	$scope.$watchGroup(['ordenFU.datosForm.AccionOrden','ordenFU.datosOrden.instrumento.IdInstrumento'],function(newval,oldval) {
		if (!newval) {
			return;
		}
		if (ordenFU.modoVentana === 'OFERTAPUBLICA') {
			return;
		}
		if ((!ordenFU.datosOrden.instrumento || !ordenFU.datosOrden.instrumento.IdTipoInstrumento) && ordenFU.SW === 'N') {
			ordenFU.datosOrden.operacionSeleccionada = undefined;
			return;
		}
		if(ordenFU.datosOrden.instrumento === undefined) {
			return;
		}
		var idTipoInstrumento = ordenFU.datosOrden.instrumento.IdTipoInstrumento;
		if (!idTipoInstrumento)
			return;
		
		var tipoOperacion = undefined;
		$.each(BCS_OPERACION_INSTRUMENTO,function(key,value){
			
			if (value.IdTipoInstrumento === idTipoInstrumento && value.AccionOrden === ordenFU.datosForm.AccionOrden) {
				tipoOperacion = value;
				return false;
			}

		})
		
		if (tipoOperacion === undefined) {
			console.error('Tipo operacion no encontrada');
		}
		
		if (ordenFU.SW === 'N') {
			ordenFU.datosOrden.operacionSeleccionada = tipoOperacion;
		} else {
			ordenFU.datosOrden.operacionSeleccionada = tipoOperacion;
			calcularFechaCondicionLiquidacion(tipoOperacion);
		}
		
		if (ordenFU.datosForm.AccionOrden === 'COMPRA')
			ordenFU.datosOrden.EsVentaCorta = false;

	});
	
	//watch condicion de liquidacion y switch ingreso rapido activado
	$scope.$watch('ordenFU.datosOrden.condicionLiquidacionSeleccionada', function(newval, oldval) {
		if (newval && ordenFU.SW === 'S') {
			var tipoOperacion = undefined;
			if(ordenFU.datosOrden.instrumento === undefined) {
				return;
			}
			var idTipoInstrumento = ordenFU.datosOrden.instrumento.IdTipoInstrumento;
			$.each(BCS_OPERACION_INSTRUMENTO,function(key,value) {
			
				if (value.IdTipoInstrumento === idTipoInstrumento && value.AccionOrden === ordenFU.datosForm.AccionOrden) {
					tipoOperacion = value;
					return false;
				}

			});
			calcularFechaCondicionLiquidacion(tipoOperacion);
		}
	});

	//watch pago cobro cuenta inversion
	$scope.$watch('ordenFU.datosOrden.portafolioSeleccionado.IdPortafolio',function(newval,oldval){
		if (newval){
			bcsConfiguracionOrdenes.FUNCIONES.getInformacionPortafolios([ordenFU.datosOrden.portafolioSeleccionado.IdPortafolio]).then(function(result){
				
				if (result[0].OperaCuentaInversion === "S") {
					ordenFU.datosOrden.pagoCobroCuentaInversion = true;
					ordenFU.datosForm.pagoCobroCuentaInversionDisabled = false;
				} else {
					ordenFU.datosOrden.pagoCobroCuentaInversion = false;
					ordenFU.datosForm.pagoCobroCuentaInversionDisabled = true;
				}
				

				if (result[0].IdPersonaBrokerExtranjero !== undefined && result[0].IdPersonaBrokerExtranjero > 0 ) {
					ordenFU.datosForm.paraClienteFinalDisabled = false;
					ordenFU.datosOrden.esParaClienteFinal = true;
				} 
				else if (result[0].EsClienteFinal === 'S' ) {
					ordenFU.datosForm.paraClienteFinalDisabled = true;
					ordenFU.datosOrden.esParaClienteFinal = true;
				}
				else {
					ordenFU.datosForm.paraClienteFinalDisabled = true;
					ordenFU.datosOrden.esParaClienteFinal = false;
				}
			});
			/*OPTIMUSPE-1215*/
			if($scope.SesionLocale === 'PE' && ordenFU.datosOrden.bolsaSeleccionada !== undefined 
					&& ordenFU.datosOrden.idBolsaSeleccionada === 'OUT'
						&& ordenFU.IdBovedaCorresponsal !== undefined 
							&& ordenFU.datosForm.configuracionCuentaCustodio.filtros.IdBoveda !== undefined)
				ordenFU.datosForm.configuracionCuentaCustodio.filtros.IdBoveda.value = ordenFU.IdBovedaCorresponsal;
			/*OPTIMUSPE-1215*/
			ordenFU.datosForm.configuracionMecanismoCaptacion.filtros.IdEntidad.value = newval;
		} else {
			ordenFU.datosForm.configuracionMecanismoCaptacion.filtros.IdEntidad.value = -1;
		}
	});
	

	//watch forma de pago ultima orden
	$scope.$watchGroup([
		'ordenFU.datosOrden.portafolioSeleccionado.IdPortafolio',
		'ordenFU.datosOrden.operacionSeleccionada.IdTipoOperacion',
		'ordenFU.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion'],function(newval,oldval){
		if (ordenFU.datosOrden.portafolioSeleccionado && ordenFU.datosOrden.portafolioSeleccionado.IdPortafolio && 
			ordenFU.datosOrden.operacionSeleccionada && ordenFU.datosOrden.operacionSeleccionada.IdTipoOperacion && 
			ordenFU.datosOrden.monedaLiquidacionOrdenSeleccionada && ordenFU.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion) {
			
			var datos = {
				p_IdPortafolio				:	ordenFU.datosOrden.portafolioSeleccionado.IdPortafolio,
				p_IdTipoOperacion			:	ordenFU.datosOrden.operacionSeleccionada.IdTipoOperacion,
				p_IdMonedaLiquidacionOrden	:	ordenFU.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion
			}
			
			bcsServicioOrden.ConsultarFormaDePagoUltimaOrden(datos).$promise.then(function(result){
				if (!result.p_out_FormaDePagoOrden[0])
					return;
				
				if (result.p_out_FormaDePagoOrden[0].IdMedioDePago === 'CIN') {
					ordenFU.datosForm.IdPreferenciaPagoPortafolio = undefined;
					if (ordenFU.datosForm.pagoCobroCuentaInversionDisabled === false) {
						ordenFU.datosOrden.pagoCobroCuentaInversion = true;
					}
				}
				else {
					ordenFU.datosOrden.pagoCobroCuentaInversion = false;
					ordenFU.datosForm.configuracionPreferenciaPagoPortafolio.idPostFiltro = result.p_out_FormaDePagoOrden[0].IdPreferenciaPagoPortafolio;
				}
				
			});
			/*OPTIMUSPE-1215*/
			if($scope.SesionLocale === 'PE' && ordenFU.datosOrden.bolsaSeleccionada !== undefined 
					&& ordenFU.datosOrden.idBolsaSeleccionada === 'OUT'
						&& ordenFU.IdBovedaCorresponsal !== undefined 
							&& ordenFU.datosForm.configuracionCuentaCustodio.filtros.IdBoveda !== undefined)
				ordenFU.datosForm.configuracionCuentaCustodio.filtros.IdBoveda.value = ordenFU.IdBovedaCorresponsal;
			/*OPTIMUSPE-1215*/
			
			ordenFU.datosForm.configuracionPreferenciaPagoPortafolio.filtros.IdTipoOperacion.value = ordenFU.datosOrden.operacionSeleccionada.IdTipoOperacion;
		}
	});
	
	$scope.$watch('ordenFU.datosOrden.operacionSeleccionada',function(newval,oldval) {
		if (!newval)
			return;
		
		if (ordenFU.SW === 'N'){
			ordenFU.datosForm.configuracionCondicionLiquidacion.filtros.IdTipoOperacion.value = ordenFU.datosOrden.operacionSeleccionada.IdTipoOperacion;
		}
		
		ordenFU.datosForm.configuracionPreferenciaPagoPortafolio.filtros.IdTipoOperacion.value = ordenFU.datosOrden.operacionSeleccionada.IdTipoOperacion;
	});

	$scope.$watchGroup(['ordenFU.datosOrden.EsVentaCorta','ordenFU.datosForm.muestraCobros'],function(newval,oldval) {
		$timeout(function() {bcsComunes.ventanaTamanoAutomatico(ordenFU.datosForm.idVentana);},10);
	});
	
	
	//watch paridades
	$scope.$watchGroup(["ordenFU.datosOrden.instrumento.IdInstrumento","ordenFU.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion"
						,"ordenFU.datosOrden.bolsaSeleccionada.IdBolsa","ordenFU.datosForm.AccionOrden"],function(newval,oldval){
		if (!ordenFU.datosOrden.instrumento || !ordenFU.datosOrden.instrumento.IdMoneda || !ordenFU.datosOrden.monedaLiquidacionOrdenSeleccionada 
			|| !ordenFU.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion)
			return;
		var idMonedaOrigen = ordenFU.datosOrden.instrumento.IdMoneda;
		var idMonedaDestino = ordenFU.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion;
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
				IdUso : obtenerIdUsoPorIdTipoOperacion(ordenFU.datosForm.AccionOrden, ordenFU.datosOrden.instrumento.IdTipoInstrumento)
			}]
		};

		var promise = bcsServiciosPlataformaMercado.CalcularParidadYTipoCambio(datos);
		promise.then(function(result){
			if (result.p_out_CalculosSalida.length == 0 || result.p_out_CalculosSalida[0].Resultado === 'RECHAZO') {
				ordenFU.datosForm.paridadReferencia = $translate.instant("UI_NO_SE_ENCONTRO_PARIDAD") + ":" + idMonedaOrigen + " - " + idMonedaDestino;
			}
			else {
				ordenFU.datosForm.paridadReferencia = idMonedaOrigen + " - " + idMonedaDestino + ":" + result.p_out_CalculosSalida[0].FactorOrigenDestino;
				if (idMonedaDestino != $scope.SesionIdMonedaSistema) {
					ordenFU.datosForm.paridadReferencia+= "/" + idMonedaOrigen + " - " + $scope.SesionIdMonedaSistema + ":" + result.p_out_CalculosSalida[0].FactorOrigenLocal;
				}
			}
		});
		
	});
	
	var obtenerIdUsoPorIdTipoOperacion = function (accionOrden, idTipoOperacion) {
		var idUso = '';
		if($scope.SesionLocale === 'CL'){
			if ('COMPRA' === accionOrden && 'MIL' === idTipoOperacion)
				idUso = 'COMPRA MILA';
			else if ('VENTA' === accionOrden && 'MIL' === idTipoOperacion)
				idUso = 'VENTA MILA';
			else if ('COMPRA' === accionOrden && ('ETX' === idTipoOperacion || 'CFX' === idTipoOperacion || 'ACX' === idTipoOperacion))
				idUso = 'COMPRA EXTRANJEROS'; //COMETX;COMCFX;COMACX
			else if ('VENTA' === accionOrden && ('ETX' === idTipoOperacion || 'CFX' === idTipoOperacion || 'ACX' === idTipoOperacion))
				idUso = 'VENTA EXTRANJEROS'; //VENETX;VENCFX;VENACX
			else
				idUso = 'ORDEN';
		}else if($scope.SesionLocale === 'PE' && ordenFU.datosOrden.idBolsaSeleccionada !== undefined){
			if ('COMPRA' === accionOrden && 'MIL' === ordenFU.datosOrden.idBolsaSeleccionada)
				idUso = 'COMPRA MILA';
			else if ('VENTA' === accionOrden && 'MIL' === ordenFU.datosOrden.idBolsaSeleccionada)
				idUso = 'VENTA MILA';
			else if ('COMPRA' === accionOrden && 'OUT' === ordenFU.datosOrden.idBolsaSeleccionada)
				idUso = 'COMPRA EXTRANJEROS'; //COMETX;COMCFX;COMACX
			else if ('VENTA' === accionOrden && 'OUT' === ordenFU.datosOrden.idBolsaSeleccionada)
				idUso = 'VENTA EXTRANJEROS'; //VENETX;VENCFX;VENACX
			else
				idUso = 'ORDEN';
		}
		return idUso;
	};

	if ($scope.SesionLocale === 'PE') {
		$scope.$watch('ordenFU.datosForm.valorAllin',function(key,value){
			if (ordenFU.datosForm.valorAllin === undefined)
				return;
			if (ordenFU.datosForm.valorAllin < 0)
				return;
			
			ordenFU.datosForm.apiCobros.calcularAllIn($scope.ordenFU.datosForm.valorAllin);
		});
	}
	
	$scope.$watchGroup(['ordenFU.datosOrden.tipoVigenciaSeleccionada.IdTipoVigenciaOrden'], function(newval,oldval){
		if (ordenFU.datosOrden.tipoVigenciaSeleccionada && ordenFU.datosOrden.tipoVigenciaSeleccionada.IdTipoVigenciaOrden){
			if (ordenFU.datosOrden.tipoVigenciaSeleccionada.IdTipoVigenciaOrden ===  bcsConfiguracionOrdenes.CONSTANTES.IdTipoVigenciaSoloHoy){
				ordenFU.datosOrden.fechaVigencia = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
			}
			else if ($scope.SesionLocale === 'CL' && ordenFU.datosOrden.tipoVigenciaSeleccionada.IdTipoVigenciaOrden === bcsConfiguracionOrdenes.CONSTANTES.IdTipoVigenciaPermanente) {
				ordenFU.datosOrden.fechaVigencia = util.dateDS("0001-01-01","FechaSistema","CALL");
			}
			else if ($scope.SesionLocale === 'PE' && ordenFU.datosOrden.tipoVigenciaSeleccionada.IdTipoVigenciaOrden === bcsConfiguracionOrdenes.CONSTANTES.IdTipoVigenciaPermanente) {
				var datos = {
						p_fechas : [
							{ fechaInicial :$scope.SesionFechaSistema, numeroDias : 30 }
							],
						p_diaHabil : 'N',
						p_soloCaeDiaHabil : 'S',
						p_idPais : $scope.SesionIdPaisSistema
					};
				bcsServicioPlataforma.Servicios().ObtenerDiaSiguiente(datos,function(result) {
						ordenFU.datosOrden.fechaVigencia = util.dateDS(result.p_out_fechas[0].fechaCalculada,"FechaSistema","CALL");
						}
					,function(result){
						console.log(result);
						}
					);
			}
			else if (ordenFU.datosOrden.tipoVigenciaSeleccionada.IdTipoVigenciaOrden === bcsConfiguracionOrdenes.CONSTANTES.IdTipoVigenciaFechaFija) {
				ordenFU.datosOrden.fechaVigencia = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
			}
			else if (ordenFU.datosOrden.tipoVigenciaSeleccionada.IdTipoVigenciaOrden === bcsConfiguracionOrdenes.CONSTANTES.IdTipoVigenciaSemanal) {
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
					ordenFU.datosOrden.fechaVigencia = util.dateDS(result.P_OUT_FECHASIGUIENTE,"FechaSistema","CALL");
					}
				,function(result){
					console.log(result);
					}
				);
			}
			else {
				ordenFU.datosOrden.fechaVigencia = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
			}
		}
	});
	
	//watch orden especial
	if ($scope.SesionLocale === 'PE') {
		$scope.$watch("ordenFU.datosForm.operacionSalida[0].totalOrden",function(newval,oldval){
			
			if (!newval) {
				ordenFU.datosForm.ordenEspecialDisabled = false;
				return;
			}
			
			var idMonedaOrigen = ordenFU.datosOrden.instrumento.IdMoneda;
			var idMonedaDestino = "USD";
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
					MontoOrigen : ordenFU.datosForm.operacionSalida[0].totalOrden,
					IdUso : obtenerIdUsoPorIdTipoOperacion(ordenFU.datosForm.AccionOrden, ordenFU.datosOrden.instrumento.IdTipoInstrumento)
				}]
			};
			
			var promise = bcsServiciosPlataformaMercado.CalcularParidadYTipoCambio(datos);
			promise.then(function(result){
				if (result.p_out_CalculosSalida.length > 0 && result.p_out_CalculosSalida[0].Resultado === 'OK') {
				
					if (result.p_out_CalculosSalida[0].MontoDestino > 50000) {
						ordenFU.datosForm.ordenEspecialDisabled = true;
						ordenFU.datosOrden.esOrdenEspecial = true;
					}
					else {
						ordenFU.datosForm.ordenEspecialDisabled = false;
					}
				}
			});
			
		});
	}
	
	//ordenFU.datosForm.cleanData();
	
	if (parametros.orden) {
		ordenFU.datosForm.setInstrumento = parametros.orden.Instrumento;
		if (parametros.orden.AccionOrden === 'COMPRA') {
			ordenFU.datosForm.configuracionCompraVenta.defecto = 'VENTA';
		}
		else {
			ordenFU.datosForm.configuracionCompraVenta.defecto = 'COMPRA';
		}
		ordenFU.datosOrden.cantidadInicial = parametros.orden.CantidadOrden;
		ordenFU.datosForm.IdCondicionDeLiquidacion = parametros.orden.IdCondicionDeLiquidacion;
		ordenFU.datosForm.configuracionTipoVigencia.defecto = parametros.orden.IdTipoVigenciaOrden;
		if (ordenFU.datosForm.configuracionTipoVigencia.defecto === 'FF') {
			ordenFU.datosOrden.fechaVigencia = parametros.orden.FechaVigencia; 
		}
		ordenFU.datosForm.configuracionTipoPrecio.defecto = parametros.orden.IdTipoPrecio;
		if (ordenFU.datosForm.configuracionTipoPrecio.defecto === "L") {
			ordenFU.datosOrden.precio = parametros.orden.PrecioOrden;
		}
		ordenFU.datosForm.configuracionBolsa.defecto = parametros.orden.IdBolsa;
		ordenFU.datosOrden.EsVentaCorta = parametros.orden.EsVentaCorta === "S"?true: false;
		ordenFU.datosOrden.plazoVentaCorta = parametros.orden.PlazoVentaCorta;
		ordenFU.datosOrden.tasaVentaCorta = parametros.orden.TasaVentaCorta;
		
		
	} 
	
	//otros defaults
	else {
		if ($scope.SesionLocale === 'PE') {
			ordenFU.datosForm.IdCondicionDeLiquidacion = 'T2';
		} else {
			ordenFU.datosForm.IdCondicionDeLiquidacion = 'T2';
		}
	}
	
	var calcularFechaCondicionLiquidacion = function(tipoOperacion) {
		var datos = {};
			datos.p_IdEmpresa = $scope.SesionIdEmpresa;
			datos.p_IdUsuario = $scope.SesionIdUsuario; 
			datos.p_IdModulo = 'PLA'; 
			datos.p_UsaReglasNegocio = 'S';
			datos.p_FechaIngreso = angular.isDate(ordenFU.datosForm.fechaInicial)?util.dateSPdate(ordenFU.datosForm.fechaInicial):ordenFU.datosForm.fechaInicial;//util.dateSPdate(scope.fechaInicial);
			datos.p_IdTipoOperacion = tipoOperacion.IdTipoOperacion || null;
			datos.P_IdCondicionDeLiquidacion = ordenFU.datosOrden.condicionLiquidacionSeleccionada.IdCondicionDeLiquidacion;
			datos.p_IdPais = Number($scope.SesionIdPaisSistema);


			bcsServicioPlataforma.FechaLiquidacion(datos,function(result) {

				if (result.P_OUT_ERRORESCONTADOR === 0){
					ordenFU.datosOrden.fechaLiquidacion = util.dateDS(result.P_OUT_FECHALIQUIDACION,"FechaSistema","CALL");
				}
				else {
					ordenFU.datosOrden.fechaLiquidacion = util.dateDS("0001-01-01","FechaSistema","CALL");
					console.log("Error." + result.P_OUT_ERRORESCONTADOR);
				}
			},function(result) {

				console.error(result);
			});
	}
	$scope.tabClick = function (){
		bcsComunes.ventanaAutoHeight(idVentana);
	}	
});
