(function() {
	angular.module('ModuloMesa').controller('IngresarSpotCtrl',
	function($scope,
	$translate,
	$resource,
	$timeout,
	bcsComunes,
	bcsAlerta,
	OPCIONES_BCSALERTA,
	bcsServiciosMesa,
	bcsDinamicosMesa,
			bcsServiciosGrillasMesaRF){
		$scope.accesoNoFacturable = BCS_ACCESOS['IngresarSpot_OperacionNoFacturable']?.Existe === 'S'
		var overlay = bcsComunes.overlay;
		var idVentana = bcsComunes.ventanaWinId($scope);
		$scope.data = {};
		$scope.tabs = new bcs.uibTab($timeout,1);
		
		var ingSpot = this;

		var datosIndicadores= [];
		var datosParidadTC	= [];
		var datosTramos		= [];
		var puntasPorTramo = 'NO';
		var puntaCompraUtilizada = 0;
		var puntaVentaUtilizada = 0;
		$scope.data.bloqueoTimeOut = true;
		
		var monedaUsadaObtenerPunta = '';
		
		var tcCostoGuardado = 0;
		
		var cambioMonedaSecundaria = false;

		$scope.IdSolicitudPuntas = 0;
		$scope.data.bloqueoSolicitudPuntas = false;
		$scope.data.bloqueoSolicitudPuntasAceptar = false;
		var solicitudPuntasObligatorio = false;
		
		/*********nueva forma de pago **************/
		//var dataSourceDetalleComprobantePagoOperacion = bcsServiciosGrillasMesaRF.getDataSourceComprobantePagoOperacionMesaRF();
		$scope.data.IdMonedaMontoDisplay = $scope.SesionIdMonedaSistema;
		$scope.data.formaDePagoEntregamos = {};
		$scope.data.formaDePagoRecibimos = {};
		$scope.data.formaDePagoEntregamosTotal = 0;
		$scope.data.formaDePagoRecibimosTotal = 0;
		$scope.data.matrizFormaPagoEntregamos = {};
		$scope.data.matrizFormaPagoRecibimos = {};
		$scope.data.valorClienteCargado = false;
		
		//$scope.data.cargaFormaExistente = false;
		/*********************************************/
		
		//Puntas.UpdatePuntas = true;
		$scope.data.Puntas = bcsDinamicosMesa.DynPuntas;
		$scope.data.oClientePortafolioIdActual = 0;

		$scope.data.MonedaPrincipalIdActual = 'USD';
		$scope.data.MonedaSecundariaIdActual = 'CLP';
		
		
	    $scope.data.tipoCambioCostoCorrespondiente = 0;
	    $scope.data.utilizaParidadBidAsk = 'N';
	    $scope.data.EstadoMesa = 'C';
	    $scope.data.MargenBidAsk = 0;
	    $scope.data.MargenPrincipal = 0;

	    $scope.data.idOperacionAModificar = 0;
	    $scope.data.idSolicitudAporteRescate = 0;
	    $scope.data.cargaInicialDatos = 'NO';
	    $scope.data.cargaInicialFormaPagos = 'NO';
	    $scope.data.bloqueoModificacion = false;
	    $scope.data.bloqueoAnulacion = false;
	    $scope.data.MensajeSinPuntasPorTramo = false;
	    $scope.data.FocoTipoCambioCierre = false;
	    $scope.data.FocoParidadCierre = false;
	    $scope.data.siempreFEE = false;
	    // Inicializa variables para identificar si solo Edita Forma de Pago
	    $scope.data.EsEditarFormaDePago = false;
	    $scope.data.tabFormasDePagoActive = false;
	    
	    //Inicializa variable para identificar si el canal de ingreso es 'CLI'
	 	$scope.data.EsIdCliente = false; 

	    $scope.data.tipoInstrumento = 'X';
	    $scope.data.tipoInstrumentoTO = ''; 
	    $scope.data.tipoInstrumentoMP = '';
	    $scope.data.tipoInstrumentoAUX = '';
		
	    $scope.data.permiteDist = 'X';
		$scope.data.permiteInterb = 'X';    

		$scope.parametrosEntrada = bcsComunes.ventanaParams(idVentana);
		if ($scope.parametrosEntrada.Opcion !== undefined){
			bcsComunes.ventanaTitulo(idVentana,bcsComunes.ventanaGetTitulo(idVentana)+" Folio: "+$scope.parametrosEntrada.IdOperacionUI)
		}
		$scope.data.idPostFiltroMonedaPrincipal = 'USD';
		$scope.data.idPostFiltroMonedaSecundaria = 'CLP';

		if ($scope.parametrosEntrada.EsEditarFormaDePago !== undefined) {
			$scope.data.EsEditarFormaDePago = $scope.parametrosEntrada.EsEditarFormaDePago;
		}
		
		//Totales de montos FormaPago entregamos y recibimos
		var totalMontoFormaPagoEnt = 0;
		var totalMontoFormaPagoRec = 0;

		//Perfil que permite distribucion e interbancario
		var usuarioPermiteDistribucion = 'X';
		var usuarioPermiteInterbancario = 'X';
		
		var totalMontoFormaPagoRec = 0;
		ingSpot.datosAntes = {};
		ingSpot.datosDespues = {};
		
		$scope.data.FechaPagoDisabled = 'false';
		ingSpot.datosAntes = angular.copy(ingSpot.datosDespues);
		
		ingSpot.data = {};
		
		$scope.data.kngFechaOperacion = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.kngFechaPagoFormaPago = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.showOperacionNoFacturableAccess = false;
		
		if($scope.data.kngFechaVencimiento < $scope.data.kngFechaOperacion){
			bcsComunes.msg("warning","warning",$translate.instant('La fecha vencimiento no puede ser menor a la fecha de operación.'));
			return;
		}
		
		
		/********************************************************************************************/
		/***************************************   COMBOBOX   ***************************************/
		/********************************************************************************************/	
		$scope.data.oClientePortafolio={
			filtros: {
				IdEstado : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			}

		};
		

		//Combobox Tipo Instrumento
		$scope.data.oConfiguracionTipoInstrumento={
			filtros : {
				Nemotecnico :{ "field"   : "Nemotecnico","operator": "eq", "value" : "" },
				IdMonedaPrincipal :{ "field"   : "IdMonedaPrincipal","operator": "eq", "value" : "" },
				IdMonedaSecundaria :{ "field"   : "IdMonedaSecundaria","operator": "eq", "value" : "" },
				IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value" : "SPT" },
				IdEstado 			: { "field"   : "IdEstado","operator": "eq", "value" : "ACT" }
			}
		};
		
		//Moneda Principal
		$scope.MonedaPrincipalIdTipoInstrumento = { "field"   : "IdTipoInstrumento","operator": "eq", "value" : "SPT" };
		$scope.MonedaPrincipalNemotecnico1 = { "field"   : "Nemotecnico","operator": "eq", "value" : "" };
		$scope.MonedaPrincipalNemotecnico2 = { "field"   : "Nemotecnico","operator": "eq", "value" : "" };
		var cargaComboboxMonedaPrincipalComplejo = function(){
			$scope.data.oConfiguracionMonedaPrincipal={
				filtros : {
					complejo : [$scope.MonedaPrincipalIdTipoInstrumento, $scope.MonedaPrincipalNemotecnico1, $scope.MonedaPrincipalNemotecnico2],
					logic: "and",
					filters: [{
						logic : "or",
						filters: [$scope.MonedaPrincipalNemotecnico1, $scope.MonedaPrincipalNemotecnico2]
		        		},
		        		$scope.MonedaPrincipalIdTipoInstrumento
		        	]
				}
			};
		};
		
		var cargaComboboxMonedaPrincipalSimple = function(){
			$scope.data.oConfiguracionMonedaPrincipal={
				filtros : {
					IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value" : "SPT" },
					IdEstado 		  : { "field"   : "IdEstado","operator": "eq", "value" : "ACT" },
				}
			};
		};

		//Moneda Secundaria
		$scope.MonedaSecundariaIdTipoInstrumento = { "field"   : "IdTipoInstrumento","operator": "eq", "value" : "SPT" };
		$scope.MonedaSecundariaNemotecnico1 = { "field"   : "Nemotecnico","operator": "eq", "value" : "" };
		$scope.MonedaSecundariaNemotecnico2 = { "field"   : "Nemotecnico","operator": "eq", "value" : "" };
		var cargaComboboxMonedaSecundariaComplejo = function(){
			$scope.data.oConfiguracionMonedaSecundaria={
				filtros : {
					complejo : [$scope.MonedaSecundariaIdTipoInstrumento, $scope.MonedaSecundariaNemotecnico1, $scope.MonedaSecundariaNemotecnico2],
					logic: "and",
					filters: [{
						logic : "or",
						filters: [$scope.MonedaSecundariaNemotecnico1, $scope.MonedaSecundariaNemotecnico2]
		        		},
		        		$scope.MonedaSecundariaIdTipoInstrumento
		        	]
				}
			};
		};
		
		var cargaComboboxMonedaSecundariaSimple = function(){
			$scope.data.oConfiguracionMonedaSecundaria={
					filtros : {
						IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value" : "SPT" },
						IdEstado 		  : { "field"   : "IdEstado","operator": "eq", "value" : "ACT" },
				}
			};
		}
		
		//Combobox Operación
		$scope.data.oConfiguracionOperacion={
			filtros : {
				IdTipoProducto : { "field"   : "IdTipoProducto","operator": "eq", "value" : "MEME" },
				IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "ne", "value" : "ANCSPT" },
				IdTipoOperacion2 : { "field"   : "IdTipoOperacion","operator": "ne", "value" : "ANVSPT" }
			}
		};
		//Combobox Documentos
		$scope.data.oConfiguracionDocumentos = {
			tipo : "combobox-documentos-mesa",
			filtros : {
				Codigo : {"field": "Codigo","operator": "eq", "value"   :  "" },
				IdTipoOperacion : {"field": "IdTipoOperacion","operator": "eq", "value"   :  "" },
				TipoOperacionMesa : {"field": "TipoOperacionMesa","operator": "eq", "value"   :  "SPOT" },
			},
			configuracion : {},
			idActual : false,
			idDefecto : false,
			seleccionado : false,
			
	};
		//Combobox Contactos
		
		$scope.data.oConfiguracionContactos={
			filtros : {
				IdCliente : { "field": "IdCliente","operator" : "eq", "value" : "IdCliente"}
			},
			configuracion : {},
			idActual : false,
			idDefecto : false,
			seleccionado : false,
		};
		//Combobox Referencia Mercado
		$scope.data.oConfiguracionReferenciaMercado={
			filtros : {
				NombreTipoParametro : { "field"   : "NombreTipoParametro","operator": "eq", "value" : "ReferenciaMercado" }
			}
		};
		
		//Combobox Tipo Dolar
		$scope.data.oConfiguracionTipoDolar={
			filtros : {
				NombreTipoParametro : { "field"   : "NombreTipoParametro","operator": "eq", "value" : "TipoDolar" }
			}
		};
		
		//Combobox Mesa
		$scope.data.oConfiguracionMesa={
			filtros : {
				IdTipoProducto : { "field"   : "IdTipoProducto","operator": "eq", "value" : "MEME" }
			}
		};

		//Formas de Pagos (Entregamos/Recibimos)
		$scope.data.oConfiguracionFormaPago={
			filtros : {
				NombreTipoParametro : { "field"   : "NombreTipoParametro","operator": "eq", "value" : "FormaPago" }
			}		
		};
		
		/*Se agregó columna UsarEnSpot para filtrar los TipoComprobante que se pueden utilizar en Spot
		$scope.TipoComprobanteEstadoFacturacion = { field: "IdEstadoFacturacion", operator: "eq", value: "ACT" };
		$scope.TipoComprobanteCodigo1			= { field: "Codigo", operator: "eq", value: "FAE" };
		$scope.TipoComprobanteCodigo2			= { field: "Codigo", operator: "eq", value: "FAC" };
		$scope.TipoComprobanteCodigo3			= { field: "Codigo", operator: "eq", value: "BOE" };
		$scope.data.oConfiguracionTipoComprobante = {
			filtros : {
				complejo : [$scope.TipoComprobanteEstadoFacturacion ,$scope.TipoComprobanteCodigo1,$scope.TipoComprobanteCodigo2],
				logic: "and",
				filters: [{
					logic : "or",
					filters: [$scope.TipoComprobanteCodigo1, $scope.TipoComprobanteCodigo2, $scope.TipoComprobanteCodigo3]
	        		},
	        		$scope.TipoComprobanteEstadoFacturacion
	        	]
			}
		};*/
		$scope.data.oConfiguracionTipoComprobante={
			filtros : {
				IdEstadoFacturacion	: { field: "IdEstadoFacturacion", operator: "eq", value: "ACT" },
				UsarEnSpot			: { field: "UsarEnSpot", operator: "eq", value: "S" },
			}
		};
		
		
		/********************************************************************************************/
		/*************************************   DATASOURCES   **************************************/
		/********************************************************************************************/
		var dsDefPuntasPorTramo = {
			type : "odata",
			definition : {
                serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                sort 			: { field: "IdPuntasPorTramo", dir: "DESC" },
                pageSize        : 100,
                transport: {
                	read: {
                		url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=406",
                		dataType:"json"
                	}
                },
                schema: {
                	model: {
                		fields: {
							IdPuntasPorTramo		: {type: "number"},
							IdEmpresa				: {type: "number"},
							IdPuntas				: {type: "number"},
							MontoPorTramo			: {type: "number", editable: true ,validation: { min: 0, required: true }},
							PorcentajeTramo       	: {type: "number", editable: true ,validation: { min: 0, required: true }},
							MontoCentavos			: {type: "number"},
							ValorCompra				: {type: "number"},
							ValorVenta				: {type: "number"},
							Estado					: {typer:"string"},
							IdUsuario				: {type: "number"},
							Usuario				    : {type: "string", editable: true },
							FechaModificacion		: {type: "sysdate", editable: true },
							Canal					: {type: "string"},
							IdMonedaPunta			: {type: "string"}
						}
                	}
                }
			}
		};
		
		var dsDefFormaDePago = {
			type : "odata",
			definition : {
                serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                sort 			: { field: "IdComprobanteFormaDepago", dir: "DESC" },
                pageSize        : 100,
                transport: {
                	read: {
                		url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=542",
                		dataType:"json"
                	}
                },
                schema: {
                	model: {
                		fields: {
							 IdComprobanteFormaDepago     : {type: "number"}
							,FolioOperacion               : {type: "number"}
							,IdTipoProducto               : {type: "number"}
							,IdMedioDePago                : {type: "string"}
							,NombreMedioDePago            : {type: "string"}
							,IdMoneda                     : {type: "string"}
							,Monto                        : {type: "number"}
							,FechaIngresoRegistro         : {type: "date"}
                		}
                	}
                }
			}
		};
		
		var dsDefMedioDePago = {
			type : "array",
			definition : {
                serverFiltering : false, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                sort 			: { field: "IdMedioDePago", dir: "asc" },
                pageSize        : 100,
                schema: {
                	model: {
                		fields: {
                			 IdMedioDePago		: {type: "string"}
                			,NombreMedioDePago	: {type: "string"}
							,Monto				: {type: "number"}
							,Moneda				: {type: "string"}
							,FechaPagoMedio		: {type: "date"}
							,IdModalidadDePago	: {type: "string"}
						}
					}
                }
			}
		};

		var dsDefConsultaContactosSPT = { // TODO ( modificar llamada)
			type: "odata",
			definition: {
				serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
				serverSorting   : true, // Ordenamiento de los datos en el servidor.
				filter		: [{ "field": "IdCliente",	"operator": "eq", "value" : 21}], //reparar
                sort 			: { field: "IdCLiente", dir: "DESC" , "value" : 32},
                pageSize        : 15,
                transport: {
                	read: {
                		url: "/BSCL/Plataforma/GWJAVA/PLA/call?t499",
                		dataType:"json"
                	}
			},
			schema: {
				model: {
					fields: {
						Nombre		: {type: "String"},
						Numero				: {type: "String"},
						Calle				: {type: "String"},
						NumeroCalle			: {type: "String", editable: true ,validation: { min: 0, required: true }},
						NombreCIudad      	: {type: "String", editable: true ,validation: { min: 0, required: true }},
						NombrePais			: {type: "String"},
						}
					}
				}
			}
		};
		
		var dsDefConsultaMovimientosSPT = {
			type : "odata",
			definition : {
                serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                sort 			: { field: "IdOperacion", dir: "DESC" },
                pageSize        : 15,
                transport: {
                	read: {
                		url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=439",
                		dataType:"json"
                	}
                },
                schema: {
                	model: {
                		fields: {
							IdOperacion				: {type: "number"},
							FechaOperacion			: {type: "sysdate"},
							FechaLiquidacion		: {type: "sysdate"},
							HoraOperacion			: {type: "string"},
							NombreInstrumento       : {type: "string"},
							Nemotecnico       		: {type: "string"},
							IdTipoOperacion			: {type: "string"},
							TipoOperacion			: {type: "string"},
							RutCliente				: {type: "string"},
							NombreCliente			: {type: "string"},
							IdMonedaPrincipal		: {typer:"string"},
							IdMonedaSecundaria		: {type: "string"},
							MontoMonedaPrincipal	: {type: "number"},
							MontoMonedaSecundaria	: {type: "number"},
							TipoCambioCierre		: {type: "number"},
							TipoCambioCosto			: {type: "number"},
							ParidadCierre			: {type: "number"},
							ParidadCosto			: {type: "number"},
							MontoEquivalente		: {type: "number"},
							Margen					: {type: "number"},
							Utilidad				: {type: "number"},
							MontoDiferido			: {type: "number"},
							MontoCalce				: {type: "number"},
							MontoDisponible			: {type: "number"},
							Modalidad				: {type: "string"},
							Plazo					: {type: "number"},
							PlazoResidual			: {type: "number"},
							FechaVencimiento		: {type: "sysdate"},
							FechaEfectiva			: {type: "sysdate"},
							Agente					: {type: "number"},
							NombreAgente			: {type: "string"},
							CodigoUsuario			: {type: "string"},
							Estado					: {type: "string"},
							IdSolicitudAporteRescate : {type: "number"}
                		}
                	}
                }
			}
		};
		
		var dsSolicitudPuntas = {
			type: "odata",
			definition : {
				serverFiltering:true,
				transport	: {read:  {url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=44290884",dataType: "json"}},
				schema 		: {model: {fields : {TipoCambioCostoAprobado	: { type: "number"}, IdEstadoOperacion : { type: "string"} }}},
				filter 		: []
			}
		};
		
		/********************************************************************************************/
		/*************************************     GRILLAS     **************************************/
		/********************************************************************************************/
		var dsPuntas = new bcs.DataSource(dsDefPuntasPorTramo);
		$scope.confBasicaPunta = {
		    dataSource: dsPuntas,
			columns: [
			     {field: "MontoPorTramo", title: "UI_MONTO_EN_USD", width: "70px", align: "right", sortable : false}
			    ,{field: "MontoCentavos", title: "UI_APERTURA_PESOS", width: "70px", align: "right", sortable : false}
			    ,{field: "ValorCompra", title: "UI_COMPRA", width: "70px", align: "right", sortable : false }
			    ,{field: "ValorVenta", title: "UI_VENTA", width: "70px", align: "right", sortable : false }
			],		
			sortable	: true,
			pageable	: true,
			filterable	: false,
			navigatable: true,
			height		: 200,
			selectable 	: "row",
		    autoBind 	 : false,
		    gridCreated: function(grid){
				var filtroPunta = new Array();
	           	filtroPunta.push({ field: "Canal", operator: "eq", value: "Punta" });
	           	//filtroPunta.push({ field: "IdMonedaPunta", operator: "eq", value:$scope.data.oConfiguracionMoneda.seleccionado.IdMoneda});
	           	filtroPunta.push({ field: "IdMonedaPunta", operator: "eq", value: "USD"});
	           	dsPuntas.filter(filtroPunta); 
	    		$scope.confBasicaPunta.dataSource = dsPuntas;
		    }
       	};
		
		var dsMedioDePago = new bcs.DataSource(dsDefMedioDePago)
		$scope.confBasicaformapago = {
			dataSource	: dsMedioDePago,
			columns	: [ 
			   	 {field: "IdMedioDePago", title: "UI_ID_MEDIO_PAGO", width: "20px", align: "right", sortable : false, hidden: true}
			    ,{field: "NombreMedioDePago", title: "UI_MEDIO_PAGO", width: "150px", align: "left", sortable : false }
				,{field: "Moneda", title: "UI_MONEDA", width: "70px", align: "left", sortable : false, hidden: true }
				,{field: "Monto", title: "UI_MONTO", width: "70px", align: "left", sortable : false,
					format: function (value, item) {
			    		return util.bcs_formato(value,'SPT','Monto',item.Moneda,'','');
			    	}
					//template : " <label> #: (util.bcs_formato(Monto,'SPT','Monto',Moneda,'','')).valor #</label>" }
			    }
				,{field: "FechaPagoMedio", title: "UI_FECHA_PAGO", width: "70px", align: "left", sortable : false }
			],		
			sortable	: true,
			pageable	: true,
			filterable	: false,
			height		: 120,
			selectable	: "row",
			autoBind	: true
		};

		var dsMedioDePagoRec = new bcs.DataSource(dsDefMedioDePago)
		$scope.confBasicaformapagorecibimos = {
			dataSource	: dsMedioDePagoRec,
			columns		: [ 
			   	 {field: "IdMedioDePago", title: "UI_ID_MEDIO_PAGO", width: "20px", align: "right", sortable : false, hidden: true}
			    ,{field: "NombreMedioDePago", title: "UI_MEDIO_PAGO", width: "150px", align: "left", sortable : false }
				,{field: "Moneda", title: "UI_MONEDA", width: "70px", align: "left", sortable : false, hidden: true }
				,{field: "Monto", title: "UI_MONTO", width: "70px", align: "left", sortable : false,
					format: function (value, item) {
			    		return util.bcs_formato(value,'SPT','Monto',item.Moneda,'','');
			    	}
					//template : " <label> #: (util.bcs_formato(Monto,'SPT','Monto',Moneda,'','')).valor #</label>" }
			    }
				,{field: "FechaPagoMedio", title: "UI_FECHA_PAGO", width: "70px", align: "left", sortable : false }
			],		
			sortable	: true,
			pageable	: true,
			filterable	: false,
			height		: 120,
			selectable	: "row",
			autoBind	: false
		};
		
		var dataSourceConsultarParametrosSistema = new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering	: true, 
				serverPaging 	: true, 
				serverSorting	: true,
				transport: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=30000",
						dataType: "json"
					}		
				},
				schema: {
			     	model: {
			     		fields: { Valor : { type: "string"}	}
			     	}
				},
				filter: [{
							logic: 'or', 
							filters: [{ field: "CodigoParametro", operator: "eq", value: 'TIPODOLAR' }, { field: "CodigoParametro", operator: "eq", value: "SOLICITUDPUNTASOBLIGATORIO" }]		
						},		
						{ field: "IdEmpresa"		, operator: "eq", value: $scope.SesionIdEmpresa }]
			}
		});
		
		dataSourceConsultarParametrosSistema.fetch(function(){
			console.log($scope.parametrosEntrada.Opcion);
			if(dataSourceConsultarParametrosSistema.data().length > 0){
				$.each(dataSourceConsultarParametrosSistema.data(), function(index, value){
					if(value.CodigoParametro == "TIPODOLAR") $scope.IdTipoDolarDefecto = value.Valor;
					
					if(value.CodigoParametro == "SOLICITUDPUNTASOBLIGATORIO" && value.Valor == "S" && $scope.parametrosEntrada.Opcion === undefined ) solicitudPuntasObligatorio = true;						
					
				});
									
			}
	 	});


		/***********************************
			CARGA METODOS DE PAGO
		*************************************/

		var dsCargoMedioDePago = function(){
			if (($scope.data.kngMontoPago||'undefined') === 'undefined') return;
			
			if (typeof $scope.data.tipoInstrumentoSeleccionado !== 'undefined'){
				var MonedaFormaPago;
            	if($scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'ENTREGAMOS'){
            		if($scope.data.oConfiguracionMedioDePago.seleccionado.IdMedioDePago === undefined || $scope.data.oConfiguracionMedioDePago.seleccionado.IdMedioDePago === ''){
            			bcsComunes.msg("error","error",$translate.instant('UI_MEDIO_PAGO_ERROR'));
             			return;
             		}
            		if((totalMontoFormaPagoEnt + $scope.data.kngMontoPago) > ($scope.data.kngMontoPrincipal || 0) && 
            		   $scope.data.OperacionSeleccionado.IdTipoOperacion === 'VENSPT'){
						bcsComunes.msg("warning","warning","No puede ser mayor que monto: "+ ($scope.data.kngMontoPrincipal||0));
            			return;
            		}
            		if((totalMontoFormaPagoEnt + $scope.data.kngMontoPago) > ($scope.data.kngMontoSecundario || 0) && 
            		   $scope.data.OperacionSeleccionado.IdTipoOperacion === 'COMSPT'){
						bcsComunes.msg("warning","warning","No puede ser mayor que monto: "+ ($scope.data.kngMontoSecundario||0));
            			return;
            		}
            		
            		//asigna la moneda para la forma de pago
            		if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'VENSPT')
            			MonedaFormaPago = $scope.data.MonedaPrincipalIdActual;
            		else if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'COMSPT')
            			MonedaFormaPago = $scope.data.MonedaSecundariaIdActual;
	            		
            		//dsMedioDePago.add(filtros);
            		dsMedioDePago.add({
            			 IdMedioDePago		: $scope.data.oConfiguracionMedioDePago.seleccionado.IdMedioDePago
            			,NombreMedioDePago	: $scope.data.oConfiguracionMedioDePago.seleccionado.NombreMedioDePago
            			,Moneda				: MonedaFormaPago
            			,Monto				: $scope.data.kngMontoPago
            			,FechaPagoMedio		: $scope.data.kngFechaPagoFormaPago
            			,IdModalidadDePago	: $scope.data.oConfiguracionMedioDePago.seleccionado.IdModalidadDePago
            		});	
            		totalMontoFormaPagoEnt = totalMontoFormaPagoEnt + $scope.data.kngMontoPago;
            		
            		//resta el valor agregado a kngMontoPago
            		if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'COMSPT') 
            			$scope.data.kngMontoPago = ($scope.data.kngMontoSecundario||0)-totalMontoFormaPagoEnt;
            		else if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'VENSPT') 
            			$scope.data.kngMontoPago = ($scope.data.kngMontoPrincipal||0)-totalMontoFormaPagoEnt;
            	}
            	else{
            		if($scope.data.oConfiguracionMedioDePago.seleccionado.IdMedioDePago === undefined || $scope.data.oConfiguracionMedioDePago.seleccionado.IdMedioDePago === ''){
            			bcsComunes.msg("error","error",$translate.instant('UI_MEDIO_PAGO_ERROR'));
             			return;
             		}
            		if((totalMontoFormaPagoRec + $scope.data.kngMontoPago) > ($scope.data.kngMontoSecundario || 0) && 
            		$scope.data.OperacionSeleccionado.IdTipoOperacion === 'VENSPT'){
						bcsComunes.msg("warning","warning",$translate.instant("MONTO_FORMA_PAGO_MAYOR_A_MOVIMIENTO"));
            			return;
            		}
            		if((totalMontoFormaPagoRec + $scope.data.kngMontoPago) > ($scope.data.kngMontoPrincipal || 0) && 
            		   $scope.data.OperacionSeleccionado.IdTipoOperacion === 'COMSPT'){
						bcsComunes.msg("warning","warning",$translate.instant("MONTO_FORMA_PAGO_MAYOR_A_MOVIMIENTO"));
            			return;
            		}
            		
            		//asigna la moneda para la forma de pago
            		if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'VENSPT')
            			MonedaFormaPago = $scope.data.MonedaSecundariaIdActual;
            		else if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'COMSPT')
            			MonedaFormaPago = $scope.data.MonedaPrincipalIdActual;
            		
            		//dsMedioDePagoRec.add(filtros);
            		dsMedioDePagoRec.add({
            			 IdMedioDePago		: $scope.data.oConfiguracionMedioDePago.seleccionado.IdMedioDePago
            			,NombreMedioDePago	: $scope.data.oConfiguracionMedioDePago.seleccionado.NombreMedioDePago
            			,Moneda				: MonedaFormaPago
            			,Monto				: $scope.data.kngMontoPago
            			,FechaPagoMedio		: $scope.data.kngFechaPagoFormaPago
            			,IdModalidadDePago	: $scope.data.oConfiguracionMedioDePago.seleccionado.IdModalidadDePago
            		});	
            		totalMontoFormaPagoRec = totalMontoFormaPagoRec+$scope.data.kngMontoPago;
            		//resta el valor agregado a kngMontoPago
            		if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'COMSPT') 
            			$scope.data.kngMontoPago = ($scope.data.kngMontoPrincipal||0)-totalMontoFormaPagoRec;
            		else if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'VENSPT') 
            			$scope.data.kngMontoPago = ($scope.data.kngMontoSecundario||0)-totalMontoFormaPagoRec;
            	}
/*
            	//Fechas de pago
    			if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'COMSPT'){
    				if ($scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'ENTREGAMOS'){
                		if ($scope.data.kngFechaPagoFormaPago >= ($scope.data.kngFechaPagoSecundaria||$scope.data.kngFechaPagoFormaPago))
                			$scope.data.kngFechaPagoSecundaria = $scope.data.kngFechaPagoFormaPago;
    				}
    				if ($scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'RECIBIMOS'){
                		$scope.data.kngFechaPagoPrincipal = $scope.data.kngFechaPagoFormaPago;
    				}
    			}

    			if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'VENSPT'){
    				if ($scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'ENTREGAMOS'){
                		$scope.data.kngFechaPagoPrincipal = $scope.data.kngFechaPagoFormaPago;
    				}
    				else if ($scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'RECIBIMOS'){
                		$scope.data.kngFechaPagoSecundaria = $scope.data.kngFechaPagoFormaPago;
    				}
    			}
*/
    	   		if(dsMedioDePagoRec.total()>0 && $scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'RECIBIMOS'){
    	   			$scope.data.FechaPagoDisabled = 'true';
    	   		}
    	   		else{
    	   			$scope.data.FechaPagoDisabled = 'false';
    	   		}
            }
		};
		
		//**************
		//*  WATCHES   *
		//**************
		
		$scope.$watch('data.Puntas.UpdatePuntas', function(newval,oldval){
			if (newval === oldval) return;
			// Solo actualizar puntas desde WS si el formulario aún no tiene datos ingresados por el usuario
			var formularioConDatos = ($scope.data.kngMontoPrincipal && $scope.data.kngMontoPrincipal !== 0) ||
			                         ($scope.data.kngTipoCambioCosto && $scope.data.kngTipoCambioCosto !== 0);
			if(!formularioConDatos){
				obtienePuntas();
			}
		});
		
		$scope.$watch('data.tipoInstrumentoSeleccionado',function(newval,oldval) {
            if (newval === false || newval === oldval) return;
            actionTipoInstrumentoSeleccionado();
		}
        ,function(result){
        	console.log(result);
		});
		

		$scope.$watch('data.oClientePortafolio.idActual',function(newval,oldval){
			if (newval === oldval){		
				return;
			}
			if ($scope.data.oClientePortafolio.idActual){
				$scope.clienteSeleccionado=true;
			}else{
				$scope.clienteSeleccionado=false;
			}			
		});
		
		//Extraigo la función para poder ocuparla con el websocket
		actionTipoInstrumentoSeleccionado = function(){
			if($scope.data.tipoInstrumentoMP){
            	$scope.data.oConfiguracionTipoInstrumento.idActual = $scope.data.tipoInstrumentoMP;
            }

            if (typeof $scope.data.tipoInstrumentoSeleccionado !== 'undefined'){
       			if($scope.parametrosEntrada.Opcion === undefined || 
       			   ($scope.parametrosEntrada.Opcion === "MODIFICAR" && $scope.data.cargaInicialDatos === 'OK')){
       				dsMedioDePago.removeAll();
					dsMedioDePagoRec.removeAll();
	            	totalMontoFormaPagoEnt = 0;
	            	totalMontoFormaPagoRec = 0;
       			}
       			$scope.data.bloqueoSolicitudPuntasAceptar = false;
       			//vacía campos de costo y margen
            	if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'INTERB'){
            		$scope.data.tipoInstrumentoTO = $scope.data.tipoInstrumentoSeleccionado.Instrumento;
            		$scope.data.tipoInstrumentoMP = '';

            		$scope.data.kngTipoCambioCosto = '';
            		$scope.data.kngParidadCosto = fnBuscoParidadTC($scope.data.MonedaPrincipalIdActual);;
            		$scope.data.kngMargen = '';
                    if($scope.data.tabPuntasActive === true)	
                        $scope.data.tabDatosOperacionesActive = true;
            	}
            	else if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'ARBINT'){
            		$scope.data.tipoInstrumentoTO = $scope.data.tipoInstrumentoSeleccionado.Instrumento;
            		$scope.data.tipoInstrumentoMP = '';
            		
            		$scope.data.kngTipoCambioCosto = '';
            		$scope.data.kngParidadCosto = fnBuscoParidadTC($scope.data.MonedaPrincipalIdActual);
            		$scope.data.kngMargen = '';
                    if($scope.data.tabPuntasActive === true)	
                        $scope.data.tabDatosOperacionesActive = true;
                    

				}
            	else if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'DISTRI'){
            		$scope.data.tipoInstrumentoTO = $scope.data.tipoInstrumentoSeleccionado.Instrumento;
            		$scope.data.tipoInstrumentoMP = '';
            		if(solicitudPuntasObligatorio) $scope.data.bloqueoSolicitudPuntasAceptar = true;
				}
            	else if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'ARBDIS'){
            		$scope.data.tipoInstrumentoTO = $scope.data.tipoInstrumentoSeleccionado.Instrumento;
            		$scope.data.tipoInstrumentoMP = '';
            		$scope.data.kngParidadCosto = fnBuscoParidadTC($scope.data.MonedaPrincipalIdActual);
            	}
            }
			obtieneTCCosto();
			obtienePuntas();		
		}
		
		$scope.$watch('data.oClientePortafolio.seleccionado',function(newval,oldval) {   
			if (newval === oldval) return;

            if (($scope.data.oClientePortafolio.seleccionado||'undefined') === 'undefined') return;      

			$scope.data.showOperacionNoFacturable = ($scope.data.oClientePortafolio.seleccionado.IdContraparte !== undefined && !isNaN($scope.data.oClientePortafolio.seleccionado.IdContraparte) && $scope.data.oClientePortafolio.seleccionado.IdContraparte > 0);

            if (typeof $scope.data.oClientePortafolio.seleccionado !== 'undefined'){
    			var tablaDatosCliente = [];	
    			var ObjCliente = {
					 "IdCliente": $scope.data.oClientePortafolio.seleccionado.IdCliente 
					,"IdAgente"	: $scope.data.oClientePortafolio.seleccionado.IdPersonaEjecutivo
				};
    			tablaDatosCliente.push(ObjCliente);

    			var parametros = {};
    			parametros.p_IdEmpresa = $scope.SesionIdEmpresa;
    			parametros.p_Clientes = tablaDatosCliente;

            	bcsServiciosMesa.ConsultarConfiguracionCliente(parametros, function(respuesta){
					$.each(respuesta.P_OUT_CONFIGURACION,function(key,value){
						if (value.Codigo === 'ESHABITUALIDAD' && value.Descripcion === "CODFACTURAHABITUALIDAD" && value.Valor !== null) {
							if(!(($scope.data.MonedaPrincipalSeleccionado && $scope.data.MonedaPrincipalSeleccionado.IdMonedaPrincipal === 'USD' && $scope.data.kngMontoPrincipal >= 10000) || 
								 ($scope.data.oClientePortafolio.seleccionado.TipoPersona === 'J'))){
								$scope.data.oConfiguracionTipoComprobante.idActual = value.Valor;
							}
						}
						
						if (value.Codigo === "AGENTE" && value.Valor !== null) {
		        			$scope.data.Agente = value.Valor;
						}
					});

					//TipoComprobante pestaña Otros
					if($scope.data.MonedaPrincipalSeleccionado && $scope.data.MonedaPrincipalSeleccionado.IdMonedaPrincipal === 'USD' && 
					   $scope.data.kngMontoPrincipal >= 10000){
						$scope.data.oConfiguracionTipoComprobante.idActual = 'FEE';
						$scope.data.siempreFEE = true;
					}
					else if($scope.data.oClientePortafolio.seleccionado.TipoPersona === 'J'){
						$scope.data.oConfiguracionTipoComprobante.idActual = 'FEE';
						$scope.data.siempreFEE = true;
					}
					else{
						$scope.data.siempreFEE = false;
					}
	        	},error);
            }
            
            if(($scope.data.cargaInicialDatos === 'OK' && $scope.parametrosEntrada.Opcion === "MODIFICAR") || 
          		   ($scope.parametrosEntrada.Opcion === undefined)){
                 	var parametros = {};
         			parametros.p_IdEmpresa		= $scope.SesionIdEmpresa;
         			parametros.p_IdUsuario		= $scope.SesionIdUsuario;
         			parametros.p_IdModulo		= 'MSP';
         			parametros.p_IdPortafolio	= $scope.data.oClientePortafolio.seleccionado.IdPortafolio;
         			parametros.p_IdTipoOperacion= $scope.data.OperacionSeleccionado.Codigo;
         			
         			if(parametros.p_IdPortafolio && parametros.p_IdTipoOperacion){
         				bcsServiciosMesa.ConsultarProductoPortafolioMesa(parametros, function(respuesta){
                     		if(respuesta.P_OUT_SALIDA[0].Codigo !== 'PRODUCTO_OK' && respuesta.P_OUT_SALIDA[0].IdResultado === 4){
                     			respuesta.P_OUT_SALIDA[0].P_OUT_ERRORESCONTADOR = 1;
             					bcsComunes.ventanaMsg({
             						mensaje:$translate.instant(respuesta.P_OUT_SALIDA[0].Codigo),
             						conflictos:util.parseServerResponse(respuesta.P_OUT_SALIDA[0])
             					},idVentana);
             				}
                     	}
                     	,function(respuesta){
                     		bcsComunes.ventanaMsg({
                 				mensaje:$translate.instant("PRODUCTO_PORTAFOLIO_ERROR"),
                 				conflictos:util.parseServerResponse(respuesta)
                 			},idVentana);
             			});
         			}
                 }
			//activador para cuando cargan los datos del cliente y ver las formas de pago 
			if($scope.data.oClientePortafolio.seleccionado!== false && $scope.data.oClientePortafolio.seleccionado !== undefined){
				$scope.data.valorClienteCargado = true;
			}
		}
        ,function(result){
        	console.log(result);
        });

		$scope.$watchGroup(['data.showOperacionNoFacturable'], function (newValue, oldValue){
			if (!newValue[0] && !oldValue[0]) return;
			if ($scope.data.showOperacionNoFacturableAccess === undefined) {
				$scope.data.showOperacionNoFacturableAccess = newValue[0];
			}
		});
		
			
		$scope.$watch('data.OperacionSeleccionado', function(newval,oldval){
			if(newval === oldval) return;
			if($scope.data.kngTipoCambioCosto === undefined) return;
			if($scope.data.OperacionSeleccionado === undefined) return;
			
			if(($scope.data.MonedaPrincipalSeleccionado.IdMonedaPrincipal||'undefined') === 'undefined') return;
			if(($scope.data.MonedaSecundariaSeleccionado.IdMonedaSecundaria||'undefined') === 'undefined') return;
			
			if(($scope.data.cargaInicialDatos === 'OK' && $scope.parametrosEntrada.Opcion === "MODIFICAR") || 
			   ($scope.parametrosEntrada.Opcion === undefined)){
				dsMedioDePago.removeAll(); 
				dsMedioDePagoRec.removeAll();
				totalMontoFormaPagoEnt = 0;
				totalMontoFormaPagoRec = 0;
				
				if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'ARBINT' || 
				   $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'ARBDIS'){
            		$scope.data.kngParidadCosto = fnBuscoParidadTC($scope.data.MonedaPrincipalIdActual);
            	}

				//MONTO EQUIVALENTE
				if ($scope.data.MonedaSecundariaSeleccionado.IdMonedaSecundaria === 'CLP'){
					$scope.data.kngMontoSecundario = Number((($scope.data.kngMontoPrincipal || 0) * ($scope.data.kngTipoCambioCierre || 0)).toFixed(2));
				}
				else{
					if($scope.data.MonedaPrincipalSeleccionado.MultiplicaODivide === 'D'){
						$scope.data.kngMontoSecundario = $scope.data.kngParidadCierre ? ($scope.data.kngMontoPrincipal||0)/$scope.data.kngParidadCierre : 0;
					}
					else{
						$scope.data.kngMontoSecundario = ($scope.data.kngMontoPrincipal||0)*($scope.data.kngParidadCierre||0);
					}
				}
				$scope.data.kngMontoEquivalente = $scope.data.kngMontoSecundario;
				$scope.data.bloqueoTimeOut = false;
				$timeout(function(){
						$scope.data.bloqueoTimeOut = true;
				},3000);
				if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'INTERB'){
					$scope.data.kngMargen = '';
					return;
				}
				else if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'ARBINT'){
					$scope.data.kngMargen = '';
					return;
				}
				calculoMontos();
				
				
				//Al cambiar el TipoOperacion, debe validar si el Producto del Portafolio está Bloqueado
				var parametros = {};
    			parametros.p_IdEmpresa		= $scope.SesionIdEmpresa;
    			parametros.p_IdUsuario		= $scope.SesionIdUsuario;
    			parametros.p_IdModulo		= 'MSP';
    			parametros.p_IdPortafolio	= $scope.data.oClientePortafolio.seleccionado.IdPortafolio;
    			parametros.p_IdTipoOperacion= $scope.data.OperacionSeleccionado.Codigo;
    			
    			if(parametros.p_IdPortafolio && parametros.p_IdTipoOperacion){
    				bcsServiciosMesa.ConsultarProductoPortafolioMesa(parametros, function(respuesta){
                		if(respuesta.P_OUT_SALIDA[0].Codigo !== 'PRODUCTO_OK' && respuesta.P_OUT_SALIDA[0].IdResultado === 4){
                			respuesta.P_OUT_SALIDA[0].P_OUT_ERRORESCONTADOR = 1;
        					bcsComunes.ventanaMsg({
        						mensaje:$translate.instant(respuesta.P_OUT_SALIDA[0].Codigo),
        						conflictos:util.parseServerResponse(respuesta.P_OUT_SALIDA[0])
        					},idVentana);
        				}
                	}
                	,function(respuesta){
                		bcsComunes.ventanaMsg({
            				mensaje:$translate.instant("PRODUCTO_PORTAFOLIO_ERROR"),
            				conflictos:util.parseServerResponse(respuesta)
            			},idVentana);
        			});
				}
			}
			
			//Si es interbancario o la monedaSecundaria es distinto a Monedalocal, calcula inmediatamente
			//De lo contrario obtiene las puntas antes de realizar el calculo
			if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'INTERB' /*||
			   $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'ARBINT' ||
			   $scope.data.MonedaSecundariaSeleccionado.IdMonedaSecundaria !== 'CLP'*/){
				calculoMontos();
			}
			else{
				obtieneTCCosto();
				obtienePuntas();
			}
		});
		
		
		$scope.$watch('data.kngMontoPrincipal',function(newval,oldval) {// TODO
			if(newval === oldval) return;
			if(newval === Infinity) return;
			if($scope.data.kngTipoCambioCosto === undefined) return;
			if($scope.data.OperacionSeleccionado === undefined) return;
			if($scope.data.MonedaPrincipalSeleccionado === undefined || $scope.data.MonedaPrincipalSeleccionado.IdMonedaPrincipal === undefined) return;
			if($scope.data.MonedaSecundariaSeleccionado === undefined || $scope.data.MonedaSecundariaSeleccionado.IdMonedaSecundaria === undefined) return;
			
			if(newval === undefined || newval === null || newval === '' || newval === 0 || isNaN(newval)){
				$scope.data.kngMontoPrincipal = 0;
			}
			
			if(($scope.data.cargaInicialDatos === 'OK' && $scope.parametrosEntrada.Opcion === "MODIFICAR") || 
			   ($scope.parametrosEntrada.Opcion === undefined)){
				//TipoComprobante pestaña Otros
				var params = {
					p_IdEmpresa 	 	: $scope.SesionIdEmpresa,	
					p_IdTipoProducto 	: 'MEME',
					p_CodigoParametro 	: 'CODFACTURAHABITUALIDAD'
				};
				bcsServiciosMesa.ConsultarConfiguracionMesa(params).$promise.then(function(respuesta){
					$.each(respuesta.P_OUT_DATOSMESA,function(key,value){
						if(!(($scope.data.MonedaPrincipalSeleccionado && $scope.data.MonedaPrincipalSeleccionado.IdMonedaPrincipal === 'USD' && $scope.data.kngMontoPrincipal >= 10000) || 
							 ($scope.data.oClientePortafolio.seleccionado.TipoPersona === 'J'))){
							$scope.data.oConfiguracionTipoComprobante.idActual = value.Valor;
						}
					});
				});
				if($scope.data.MonedaPrincipalSeleccionado && $scope.data.MonedaPrincipalSeleccionado.IdMonedaPrincipal === 'USD' && 
				   $scope.data.kngMontoPrincipal >= 10000){
					$scope.data.oConfiguracionTipoComprobante.idActual = 'FEE';
					$scope.data.siempreFEE = true;
				}
				else if($scope.data.oClientePortafolio.seleccionado && $scope.data.oClientePortafolio.seleccionado.TipoPersona === 'J'){
					$scope.data.oConfiguracionTipoComprobante.idActual = 'FEE';
					$scope.data.siempreFEE = true;
				}
				else{
					$scope.data.siempreFEE = false;
				}

				//MONTO EQUIVALENTE
				if ($scope.data.MonedaSecundariaSeleccionado.IdMonedaSecundaria === 'CLP'){
					$scope.data.kngMontoSecundario = Number((($scope.data.kngMontoPrincipal || 0) * ($scope.data.kngTipoCambioCierre || 0)).toFixed(2));
				}
				else{
					if($scope.data.MonedaPrincipalSeleccionado.MultiplicaODivide === 'D'){
						$scope.data.kngMontoSecundario = $scope.data.kngParidadCierre ? ($scope.data.kngMontoPrincipal||0)/$scope.data.kngParidadCierre : 0;
					}
					else{
						$scope.data.kngMontoSecundario = ($scope.data.kngMontoPrincipal||0)*($scope.data.kngParidadCierre||0);
					}
				}
				$scope.data.kngMontoEquivalente = $scope.data.kngMontoSecundario;

				//Monto pestaña forma pago
				if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'COMSPT' && dsMedioDePagoRec.total()===1){
					totalMontoFormaPagoRec = $scope.data.kngMontoPrincipal;
					dsMedioDePagoRec.data()[0].Monto = $scope.data.kngMontoPrincipal;
				}
				else if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'VENSPT' && dsMedioDePago.total()===1){
					totalMontoFormaPagoEnt = $scope.data.kngMontoPrincipal;
					dsMedioDePago.data()[0].Monto = $scope.data.kngMontoPrincipal;
				}
				else if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'COMSPT' && dsMedioDePagoRec.total()>1){
					dsMedioDePagoRec.removeAll();
					totalMontoFormaPagoRec = 0;
				}
				else if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'VENSPT' && dsMedioDePago.total()>1){
					dsMedioDePago.removeAll();
					totalMontoFormaPagoEnt = 0;
				}
				
				//actualizaMontoFormaPago();
				
				if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'ARBINT'){
					$scope.data.kngMargen = '';
					return;
				}
				else if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'INTERB'){
					$scope.data.kngMargen = '';
					calculoMontos();
				}
				else{
					// Si los tramos ya están cargados en caché, calcular TC Costo sincrónicamente
					// sin hacer una nueva llamada HTTP (elimina el delay por round-trip de red)
					if(puntasPorTramo === 'SI' && datosTramos.length > 0){
						obtieneCostoPorTramo($scope.data.kngMontoPrincipal || 0);
						calculoMontos();
					} else {
						obtienePuntas();
					}
				}
			}
			
		});
		
		$scope.$watch('data.kngMontoSecundario',function(newval,oldval) {
			if(newval === oldval) return;
			if($scope.data.kngTipoCambioCosto === undefined) return;
			if($scope.data.OperacionSeleccionado === undefined) return;
			if($scope.data.MonedaSecundariaSeleccionado === undefined || $scope.data.MonedaSecundariaSeleccionado.IdMonedaSecundaria === undefined) return;	
			
			if(($scope.data.cargaInicialDatos === 'OK' && $scope.parametrosEntrada.Opcion === "MODIFICAR") || 
			   ($scope.parametrosEntrada.Opcion === undefined)){
				$scope.data.kngMontoEquivalente = $scope.data.kngMontoSecundario;

				//Monto pestaña forma pago
				if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'COMSPT' && dsMedioDePago.total()===1){
					totalMontoFormaPagoEnt = $scope.data.kngMontoSecundario;
					dsMedioDePago.data()[0].Monto = $scope.data.kngMontoSecundario;
				}
				else if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'VENSPT' && dsMedioDePagoRec.total()===1){
					totalMontoFormaPagoRec = $scope.data.kngMontoSecundario;
					dsMedioDePagoRec.data()[0].Monto = $scope.data.kngMontoSecundario;
				}
				else if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'COMSPT' && dsMedioDePago.total()>1){
					dsMedioDePago.removeAll();
					totalMontoFormaPagoEnt = 0;
				}
				else if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'VENSPT' && dsMedioDePagoRec.total()>1){
					dsMedioDePagoRec.removeAll();
					totalMontoFormaPagoRec = 0;
				}
				
				//actualizaMontoFormaPago();
				
				if((((!$scope.data.kngTipoCambioCierre || isNaN($scope.data.kngTipoCambioCierre)) && $scope.data.MonedaSecundariaSeleccionado.IdMonedaSecundaria === 'CLP') ||
					((!$scope.data.kngParidadCierre || isNaN($scope.data.kngParidadCierre)) && $scope.data.MonedaSecundariaSeleccionado.IdMonedaSecundaria !== 'CLP')) && 
					(!$scope.data.kngMontoPrincipal || isNaN($scope.data.kngMontoPrincipal))){
						return;
				}
					
					
				//MONTO EQUIVALENTE
				if($scope.data.MonedaSecundariaSeleccionado.IdMonedaSecundaria === 'CLP'){
					if($scope.data.kngMontoPrincipal === undefined || $scope.data.kngMontoPrincipal === null ||
					   $scope.data.kngMontoPrincipal === '' || $scope.data.kngMontoPrincipal === 0 || isNaN($scope.data.kngMontoPrincipal)){
						$scope.data.kngMontoPrincipal = ($scope.data.kngMontoSecundario||0)/($scope.data.kngTipoCambioCierre||$scope.data.kngTipoCambioCosto||0);
					}
					else if(!$scope.data.kngTCCierreEditadoManualmente){
						// solo si TipoCambioCierre es distinto a la division, setea el nuevo valor (para evitar watch repetidos)
						if($scope.data.kngTipoCambioCierre !== Number(new Big($scope.data.kngMontoSecundario||0).div(new Big($scope.data.kngMontoPrincipal||0)))){
							$scope.data.kngTipoCambioCierre = Number(new Big($scope.data.kngMontoSecundario||0).div(new Big($scope.data.kngMontoPrincipal||0)));
							$scope.data.kngTipoCambioCierre = util.bcs_formato($scope.data.kngTipoCambioCierre,'SPT','MONTO', 'USD', '','').valorNumerico;
						}
					}
				}
				else{
					if($scope.data.kngMontoPrincipal === undefined || $scope.data.kngMontoPrincipal === null || 
					   $scope.data.kngMontoPrincipal === '' || $scope.data.kngMontoPrincipal === 0 || isNaN($scope.data.kngMontoPrincipal)){
						if($scope.data.MonedaPrincipalSeleccionado.MultiplicaODivide === 'D'){
							$scope.data.kngMontoPrincipal = ($scope.data.kngMontoSecundario||0)*($scope.data.kngParidadCierre||0);
						}
						else{
							$scope.data.kngMontoPrincipal = $scope.data.kngParidadCierre ? ($scope.data.kngMontoSecundario||0)/$scope.data.kngParidadCierre : 0;
						}
					}
					else{
						if($scope.data.MonedaPrincipalSeleccionado.MultiplicaODivide === 'D'){
							$scope.data.kngParidadCierre = $scope.data.kngMontoSecundario ? ($scope.data.kngMontoPrincipal||0)/$scope.data.kngMontoSecundario : 0;
						}
						else{
							$scope.data.kngParidadCierre = $scope.data.kngMontoPrincipal ? ($scope.data.kngMontoSecundario||0)/$scope.data.kngMontoPrincipal : 0;
						}
					}
				}
				
				if($scope.data.tipoInstrumentoSeleccionado && $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'INTERB'){
					$scope.data.kngMargen = '';
					return;
				}
				else if($scope.data.tipoInstrumentoSeleccionado && $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'ARBINT'){
					$scope.data.kngMargen = '';
					return;
				}
				
				calculoMontos();
			}
		});
		

		$scope.$watch('data.kngTipoCambioCierre',function(newval,oldval) {
			if(newval === oldval) return;
			if($scope.data.kngTipoCambioCosto === undefined) return;
			if($scope.data.kngTipoCambioCierre === undefined) return;
			if($scope.data.OperacionSeleccionado === undefined) return;
			if($scope.data.MonedaSecundariaSeleccionado === undefined || $scope.data.MonedaSecundariaSeleccionado.IdMonedaSecundaria === undefined) return;		

			//if(($scope.data.kngMontoPrincipal||'undefined') === 'undefined'){}
			//	return
			
			if(($scope.data.cargaInicialDatos === 'OK' && $scope.parametrosEntrada.Opcion === "MODIFICAR") || 
			   ($scope.parametrosEntrada.Opcion === undefined)){
				if($scope.data.kngMontoPrincipal === undefined){
					if($scope.data.MonedaSecundariaSeleccionado.IdMonedaSecundaria === 'CLP'){
						$scope.data.kngMontoPrincipal = ($scope.data.kngMontoSecundario||0)/($scope.data.kngTipoCambioCierre||0);
					}
					else{
						$scope.data.kngMontoPrincipal = ($scope.data.kngMontoSecundario||0)/($scope.data.kngParidadCierre||0);
					}
				}
				
				//MONTO EQUIVALENTE
				if ($scope.data.MonedaSecundariaSeleccionado.IdMonedaSecundaria === 'CLP'){
					$scope.data.kngMontoSecundario = Number((($scope.data.kngMontoPrincipal || 0) * ($scope.data.kngTipoCambioCierre || 0)).toFixed(2));
				}
				else{
					if($scope.data.MonedaPrincipalSeleccionado.MultiplicaODivide === 'D'){
						$scope.data.kngMontoSecundario = $scope.data.kngParidadCierre ? ($scope.data.kngMontoPrincipal||0)/$scope.data.kngParidadCierre : 0;
					}
					else{
						$scope.data.kngMontoSecundario = ($scope.data.kngMontoPrincipal||0)*($scope.data.kngParidadCierre||0);
					}
				}
				$scope.data.kngMontoEquivalente = $scope.data.kngMontoSecundario;
				
				if($scope.data.tipoInstrumentoSeleccionado && $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'INTERB'){
					$scope.data.kngMargen = '';
					return;
				}
				else if($scope.data.tipoInstrumentoSeleccionado && $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'ARBINT'){
					$scope.data.kngMargen = '';
					return;
				}
				calculoMontos();
			}
		});
		

		$scope.$watch('data.kngTipoCambioCosto',function(newval,oldval) {
			if(newval === oldval) return;
			if($scope.data.kngTipoCambioCosto === undefined) return;

			if($scope.data.tipoInstrumentoSeleccionado && $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'INTERB'){
				$scope.data.kngMargen = '';
				return;
			}
			if($scope.data.tipoInstrumentoSeleccionado && $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'ARBINT'){
				$scope.data.kngMargen = '';
				return;
			}

			// Condición de carrera: kngMontoPrincipal fue ingresado antes de que llegara la respuesta HTTP inicial
			// oldval === undefined indica que este es el disparo de la carga inicial, no un cambio del usuario
			if(oldval === undefined && $scope.data.kngMontoPrincipal && !isNaN($scope.data.kngMontoPrincipal) && $scope.data.kngMontoPrincipal !== 0){
				if(puntasPorTramo === 'SI' && datosTramos.length > 0){
					// datosTramos ya está cargado — recalcular TC Costo con el monto real de forma síncrona
					// (sin nueva llamada HTTP). Esto disparará el watch nuevamente con el valor correcto.
					obtieneCostoPorTramo($scope.data.kngMontoPrincipal);
					return;
				} else {
					// puntasPorTramo='NO': kngTipoCambioCierre ya fue seteado por obtieneTCCosto en el mismo callback
					var montoSec = Number((($scope.data.kngMontoPrincipal || 0) * ($scope.data.kngTipoCambioCierre || 0)).toFixed(2));
					if($scope.data.kngMontoSecundario !== montoSec){
						$scope.data.kngMontoSecundario = montoSec;
					}
				}
			}

			calculoMontos();
			//TipoCambioCostoCorrespondiente = oldval;

			if(solicitudPuntasObligatorio && $scope.data.oClientePortafolio.seleccionado && $scope.data.MontoPrincipal > 0 && $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'DISTRI' && !$scope.data.bloqueoSolicitudPuntas) $scope.IngresarSolicitudPuntas();
		});

		$scope.$watch('data.kngParidadCierre',function(newval,oldval) {
			if(!$scope.data.kngParidadCierre) return;
			if($scope.data.OperacionSeleccionado === undefined) return;
			if($scope.data.MonedaSecundariaSeleccionado === undefined && $scope.data.MonedaSecundariaSeleccionado.IdMonedaSecundaria === undefined) return;		
			
			if(($scope.data.cargaInicialDatos === 'OK' && $scope.parametrosEntrada.Opcion === "MODIFICAR") || 
			   ($scope.parametrosEntrada.Opcion === undefined)){
			   	if((!$scope.data.kngMontoPrincipal || isNaN($scope.data.kngMontoPrincipal)) && (!$scope.data.kngMontoSecundario || isNaN($scope.data.kngMontoSecundario))){
					return;
				}

				if($scope.data.kngMontoPrincipal && !isNaN($scope.data.kngMontoPrincipal)){
					var montoSecundarioAux = 0;
					if($scope.data.MonedaPrincipalSeleccionado.MultiplicaODivide === 'D'){
						montoSecundarioAux = $scope.data.kngMontoPrincipal / $scope.data.kngParidadCierre;
					}
					else{
						montoSecundarioAux = $scope.data.kngMontoPrincipal * $scope.data.kngParidadCierre;
					}

					//setea el nuevo valor solo si son distintos (para evitar watch repetidos)
					if($scope.data.kngMontoSecundario !== montoSecundarioAux){
						$scope.data.kngMontoSecundario = montoSecundarioAux;
					}
				}
				else if($scope.data.kngMontoSecundario && !isNaN($scope.data.kngMontoSecundario)){
					var montoPrincipalAux = 0;
					if($scope.data.MonedaPrincipalSeleccionado.MultiplicaODivide === 'D'){
						montoPrincipalAux = $scope.data.kngMontoSecundario * $scope.data.kngParidadCierre;
					}
					else{
						montoPrincipalAux = $scope.data.kngMontoSecundario / $scope.data.kngParidadCierre;
					}

					//setea el nuevo valor solo si son distintos (para evitar watch repetidos)
					if($scope.data.kngMontoPrincipal !== montoPrincipalAux){
						$scope.data.kngMontoPrincipal = montoPrincipalAux;
					}
				}
				
				$scope.data.kngMontoEquivalente = $scope.data.kngMontoSecundario;
				
				if($scope.data.tipoInstrumentoSeleccionado && $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'INTERB'){
					$scope.data.kngMargen = '';
					return;
				}
				else if($scope.data.tipoInstrumentoSeleccionado && $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'ARBINT'){
					$scope.data.kngMargen = '';
					return;
				}
				calculoMontos();
			}
		});
		
		
		$scope.$watch('data.kngParidadCosto',function(newval,oldval) {
			if(newval === oldval) return;
			if($scope.data.kngTipoCambioCosto === undefined) return;

			if($scope.data.tipoInstrumentoSeleccionado && $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'INTERB'){
				$scope.data.kngMargen = '';
				return;
			}
			if($scope.data.tipoInstrumentoSeleccionado && $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'ARBINT'){
				$scope.data.kngMargen = '';
				return;
			}
			calculoMontos();
		});
		
			
		$scope.$watchGroup(['data.permiteDist','data.permiteInterb'],function(newval,oldval){
			//if(newval === oldval) return;+
			
			//tiene ver si tiene ambos permisos
			//sino revisamos permisos individuales
			//tiene ver si tiene permiso distribucion
			//sino revisamos permiso restante
			//tiene ver si tiene permiso interbancario
			
		if($scope.data.permiteDist != 'X' && $scope.data.permiteInterb != 'X'){
			//tiene ambos permisos
			//asignamos distribucion como predeterminado
			$scope.data.oConfiguracionTipoInstrumento.idActual = 'DISTRIBUCION'
			$scope.data.permiteDist = 'X';
				$scope.data.permiteInterb = 'X';
		}else{
			if($scope.data.permiteDist != 'X' && $scope.data.permiteInterb == 'X'){
				//solo permiso dist
				$scope.data.oConfiguracionTipoInstrumento.idActual = 'DISTRIBUCION'
				$scope.data.permiteDist = 'X';
				$scope.data.permiteInterb = 'X';
			}else{
				if($scope.data.permiteDist == 'X' && $scope.data.permiteInterb != 'X'){
					//solo permiso interbancario
					$scope.data.oConfiguracionTipoInstrumento.idActual = 'INTERBANCARIO'
					$scope.data.permiteDist = 'X';
				$scope.data.permiteInterb = 'X';
				}

			}
		}
		});
		
		$scope.$watchGroup(['data.MonedaPrincipalSeleccionado','data.OperacionSeleccionado'],function(newval,oldval){
			//if(newval === oldval) return;
			if(JSON.stringify(newval) === JSON.stringify(oldval)) return
			actionMonedaPrincipal();
		});
		
		var actionMonedaPrincipal = function(){
			if($scope.data.MonedaPrincipalSeleccionado === undefined || $scope.data.MonedaPrincipalSeleccionado.IdMonedaPrincipal === undefined) return;
			if($scope.data.MonedaSecundariaSeleccionado === undefined || $scope.data.MonedaSecundariaSeleccionado.IdMonedaSecundaria === undefined) return;
			if($scope.data.OperacionSeleccionado.IdTipoOperacion === undefined) return;
			
			
			if($scope.data.cargaInicialDatos === 'NO'){
				if($scope.parametrosEntrada.Opcion === 'MODIFICAR'){
					fnCargoValoresSpot();
				}
				else if($scope.parametrosEntrada.Opcion === 'ANULAR'){
					fnCargoValoresSpot();
				    $scope.data.bloqueoAnulacion = 'true';
				}
				else if($scope.parametrosEntrada.Opcion === 'CONSULTAR'){
					fnCargoValoresSpot();
				    $scope.data.bloqueoAnulacion = 'true';
				}else{
					$scope.data.kngParidadCosto = 0;
					$scope.data.kngTipoCambioCierre = 0;
					
				}
			}

			$scope.data.MensajeSinPuntasPorTramo = false;

			$scope.data.oConfiguracionTipoInstrumento.filtros.IdMonedaPrincipal.value = $scope.data.MonedaPrincipalSeleccionado.IdMonedaPrincipal;
			$scope.data.oConfiguracionTipoInstrumento.filtros.IdMonedaSecundaria.value = $scope.data.MonedaSecundariaSeleccionado.IdMonedaSecundaria;
			
			if(usuarioPermiteDistribucion === 'N' && usuarioPermiteInterbancario === 'N'){
				$scope.data.oConfiguracionTipoInstrumento.filtros.Nemotecnico.value = $scope.data.tipoInstrumento
			}
			else if(usuarioPermiteDistribucion === 'N' || usuarioPermiteInterbancario === 'N'){
				$scope.data.oConfiguracionTipoInstrumento.filtros.Nemotecnico.value = $scope.data.MonedaSecundariaSeleccionado.Nemotecnico;
	       	}
	       	else{
	       		$scope.data.oConfiguracionTipoInstrumento.filtros.Nemotecnico.value = '';
	       		if($scope.data.tipoInstrumentoTO){
	       			$scope.data.tipoInstrumentoMP = $scope.data.tipoInstrumentoTO;
	       		}
	       	}

			if($scope.parametrosEntrada.Opcion === "CONSULTAR" || $scope.parametrosEntrada.Opcion === "MODIFICAR" ||
			   $scope.parametrosEntrada.Opcion === "ANULAR" ){
				$scope.data.oConfiguracionTipoInstrumento.filtros.Nemotecnico.value = $scope.data.tipoInstrumento
			}
			
			if ($scope.data.MonedaPrincipalSeleccionado.IdMonedaPrincipal !== 'CLP'){
				//-- Grilla Puntas --
				var filtroPunta = new Array();
	           	filtroPunta.push({ field: "Canal", operator: "eq", value: "Punta" });
	           	filtroPunta.push({ field: "IdMonedaPunta", operator: "eq", value:$scope.data.MonedaPrincipalSeleccionado.IdMonedaPrincipal});
	           	dsPuntas.filter(filtroPunta); 
			}

			//TipoComprobante pestaña Otros
			var params = {
				p_IdEmpresa 	 	: $scope.SesionIdEmpresa,	
				p_IdTipoProducto 	: 'MEME',
				p_CodigoParametro 	: 'CODFACTURAHABITUALIDAD'
			};
			bcsServiciosMesa.ConsultarConfiguracionMesa(params).$promise.then(function(respuesta){
				$.each(respuesta.P_OUT_DATOSMESA,function(key,value){
					if(!(($scope.data.MonedaPrincipalSeleccionado && $scope.data.MonedaPrincipalSeleccionado.IdMonedaPrincipal === 'USD' && $scope.data.kngMontoPrincipal >= 10000) || 
						 ($scope.data.oClientePortafolio.seleccionado.TipoPersona === 'J'))){
						$scope.data.oConfiguracionTipoComprobante.idActual = value.Valor;
					}
				});
			});
			if($scope.data.MonedaPrincipalSeleccionado && $scope.data.MonedaPrincipalSeleccionado.IdMonedaPrincipal === 'USD' && 
			   $scope.data.kngMontoPrincipal >= 10000){
				$scope.data.oConfiguracionTipoComprobante.idActual = 'FEE';
				$scope.data.siempreFEE = true;
			}
			else if($scope.data.oClientePortafolio.seleccionado && $scope.data.oClientePortafolio.seleccionado.TipoPersona === 'J'){
				$scope.data.oConfiguracionTipoComprobante.idActual = 'FEE';
				$scope.data.siempreFEE = true;
			}
			else{
				actionTipoInstrumentoSeleccionado();
				$scope.data.siempreFEE = false;
			}
			
			/*
			if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'COMSPT'){
				if ($scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'ENTREGAMOS'){
					$scope.data.MontoPagoDecimales = $scope.data.MonedaSecundariaSeleccionado.MonedaSecundariaDecimales;
				}
				else if ($scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'RECIBIMOS'){
					$scope.data.MontoPagoDecimales = $scope.data.MonedaPrincipalSeleccionado.MonedaPrincipalDecimales;
				}
			}
			else if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'VENSPT'){
				if ($scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'ENTREGAMOS'){
					$scope.data.MontoPagoDecimales = $scope.data.MonedaPrincipalSeleccionado.MonedaPrincipalDecimales;
				}
				else if ($scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'RECIBIMOS'){
					$scope.data.MontoPagoDecimales = $scope.data.MonedaSecundariaSeleccionado.MonedaSecundariaDecimales;
				}
			}*/
			
			//validaciones para saber si es necesario obtener puntas
			if($scope.data.tipoInstrumentoSeleccionado && $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'INTERB') return;
			if($scope.data.tipoInstrumentoSeleccionado && $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'ARBINT') return;
			if($scope.data.MonedaSecundariaSeleccionado && $scope.data.MonedaSecundariaSeleccionado.IdMonedaSecundaria !== 'CLP') return;
			obtienePuntas();			
		}
		
		$scope.$watch('data.MonedaSecundariaSeleccionado',function(newval,oldval){
			if(newval === oldval) return;
			if($scope.data.MonedaSecundariaSeleccionado.IdMonedaSecundaria === undefined) return;
			if($scope.data.MonedaPrincipalSeleccionado.IdMonedaPrincipal === undefined) return;
			
			if($scope.data.cargaInicialDatos === 'NO'){
				if($scope.parametrosEntrada.Opcion === 'MODIFICAR'){
					fnCargoValoresSpot();
				}
				else if($scope.parametrosEntrada.Opcion === 'ANULAR'){
					fnCargoValoresSpot();
				    $scope.data.bloqueoAnulacion = 'true';
				}
				else if($scope.parametrosEntrada.Opcion === 'CONSULTAR'){
					fnCargoValoresSpot();
				    $scope.data.bloqueoAnulacion = 'true';
				}
			}
			
			$scope.data.MensajeSinPuntasPorTramo = false;
			
			$scope.data.oConfiguracionTipoInstrumento.filtros.IdMonedaPrincipal.value = $scope.data.MonedaPrincipalSeleccionado.IdMonedaPrincipal;
			$scope.data.oConfiguracionTipoInstrumento.filtros.IdMonedaSecundaria.value = $scope.data.MonedaSecundariaSeleccionado.IdMonedaSecundaria;
			
			if(usuarioPermiteDistribucion === 'N' && usuarioPermiteInterbancario === 'N'){
				$scope.data.oConfiguracionTipoInstrumento.filtros.Nemotecnico.value = $scope.data.tipoInstrumento
			}
			else if(usuarioPermiteDistribucion === 'N' || usuarioPermiteInterbancario === 'N'){
				$scope.data.oConfiguracionTipoInstrumento.filtros.Nemotecnico.value = $scope.data.MonedaSecundariaSeleccionado.Nemotecnico;
	       	}

			if($scope.parametrosEntrada.Opcion === "CONSULTAR" || $scope.parametrosEntrada.Opcion === "MODIFICAR" ||
			   $scope.parametrosEntrada.Opcion === "ANULAR" ){
				$scope.data.oConfiguracionTipoInstrumento.filtros.Nemotecnico.value = $scope.data.tipoInstrumento
			}
			
			if ($scope.data.MonedaSecundariaSeleccionado.IdMonedaSecundaria === 'CLP'){
				$scope.data.kngParidadCierre = '';
				$scope.data.kngParidadCosto = '';
			}
			else{
				$scope.data.kngTipoCambioCierre = '';
				$scope.data.kngTipoCambioCosto = '';
				$scope.data.kngTCCierreEditadoManualmente = false;
				cambioMonedaSecundaria = true;
				actionTipoInstrumentoSeleccionado();			
				$scope.data.kngParidadCosto = fnBuscoParidadTC($scope.data.MonedaPrincipalIdActual);
			}
			
			if($scope.data.tipoInstrumentoSeleccionado &&
			  ($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'INTERB' ||
			   $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'ARBINT') ||
			   $scope.data.MonedaSecundariaSeleccionado.IdMonedaSecundaria !== 'CLP'){
				return;
			}else{
				obtieneTCCosto();
				obtienePuntas();			
			}

			/*
			if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'COMSPT'){
				if ($scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'ENTREGAMOS'){
					$scope.data.MontoPagoDecimales = $scope.data.MonedaSecundariaSeleccionado.MonedaSecundariaDecimales;
				}
				if ($scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'RECIBIMOS'){
					$scope.data.MontoPagoDecimales = $scope.data.MonedaPrincipalSeleccionado.MonedaPrincipalDecimales;
				}
			}
			else if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'VENSPT'){
				if ($scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'ENTREGAMOS'){
					$scope.data.MontoPagoDecimales = $scope.data.MonedaPrincipalSeleccionado.MonedaPrincipalDecimales;
				}
				if ($scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'RECIBIMOS'){
					$scope.data.MontoPagoDecimales = $scope.data.MonedaSecundariaSeleccionado.MonedaSecundariaDecimales;
				}
			}*/
		});

	   	$scope.$watch('data.kngFechaPagoFormaPago', function(newValue, oldValue) {
	   		if (newValue === oldValue) return;
	   		if($scope.data.kngFechaPagoFormaPago  === undefined) return;
	   		
			if($scope.data.kngFechaPagoFormaPago < $scope.data.kngFechaOperacion){
				bcsComunes.msg("warning","warning",$translate.instant('La fecha pago no puede ser menor a la fecha de operación.'));
				return;
			}
/*
        	//Fechas de pago
			if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'COMSPT'){
				if ($scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'ENTREGAMOS'){
            		$scope.data.kngFechaPagoSecundaria = $scope.data.kngFechaPagoFormaPago;
				}
				else if ($scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'RECIBIMOS'){
            		$scope.data.kngFechaPagoPrincipal = $scope.data.kngFechaPagoFormaPago;
				}
			}
			else if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'VENSPT'){
				if ($scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'ENTREGAMOS'){
            		$scope.data.kngFechaPagoPrincipal = $scope.data.kngFechaPagoFormaPago;
				}
				else if ($scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'RECIBIMOS'){
            		$scope.data.kngFechaPagoSecundaria = $scope.data.kngFechaPagoFormaPago;
				}
			}*/
		});
	   	
		$scope.$watchGroup(['data.oConfiguracionFormaPago.seleccionado'],function(newval,oldval){
			if($scope.data.oConfiguracionFormaPago.seleccionado === undefined) return;
			if($scope.data.OperacionSeleccionado.IdTipoOperacion === undefined) return;
	   		
	   		if(dsMedioDePagoRec.total()>0 && $scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'RECIBIMOS'){
	   			$scope.data.FechaPagoDisabled = 'true'; //data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'ENTREGAMOS
	   		}
	   		else{
	   			$scope.data.FechaPagoDisabled = 'false';
	   		}
			
			if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'COMSPT'){
				if ($scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'ENTREGAMOS'){
					$scope.data.MontoPagoDecimales = $scope.data.MonedaSecundariaSeleccionado.MonedaSecundariaDecimales;
					$scope.data.kngMontoPago = ($scope.data.kngMontoSecundario||0)-totalMontoFormaPagoEnt;
				}
				else if ($scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'RECIBIMOS'){
					$scope.data.MontoPagoDecimales = $scope.data.MonedaPrincipalSeleccionado.MonedaPrincipalDecimales;
					$scope.data.kngMontoPago = ($scope.data.kngMontoPrincipal||0)-totalMontoFormaPagoRec;
				}
			}
			else if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'VENSPT'){
				if ($scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'ENTREGAMOS'){
					$scope.data.MontoPagoDecimales = $scope.data.MonedaPrincipalSeleccionado.MonedaPrincipalDecimales;
					$scope.data.kngMontoPago = ($scope.data.kngMontoPrincipal||0)-totalMontoFormaPagoEnt;
				}
				else if ($scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'RECIBIMOS'){
					$scope.data.MontoPagoDecimales = $scope.data.MonedaSecundariaSeleccionado.MonedaSecundariaDecimales;
					$scope.data.kngMontoPago = ($scope.data.kngMontoSecundario||0)-totalMontoFormaPagoRec;
				}
			}
		});
		/*
		$scope.$watchCollection('data.formaDePagoEntregamos',function(newval, oldval){
			$scope.data.formaDePagoEntregamosTotal = 0;
			$.each($scope.data.formaDePagoEntregamos,function(key,value) {
				if (value > 0){
					$scope.data.formaDePagoEntregamosTotal= $scope.data.formaDePagoEntregamosTotal + value;
				}
			});
			
		});
		$scope.$watchCollection('data.formaDePagoRecibimos',function(newval, oldval){
			$scope.data.formaDePagoRecibimosTotal = 0;
			$.each($scope.data.formaDePagoRecibimos,function(key,value) {
				if (value > 0){
					$scope.data.formaDePagoRecibimosTotal = $scope.data.formaDePagoRecibimosTotal + value;	
				}
			});
		});*/
		
		var varFicticia;
		$scope.$watch('data.Ficticia',function(newval,oldval){
			if (typeof $scope.data.Ficticia === 'string') {
				varFicticia = $scope.data.Ficticia;
				return;
			}

			if($scope.data.Ficticia){
				varFicticia = 'S';
			}
			else{
				varFicticia = 'N';
			}	
		});
		
		
		var obtieneCostoPorTramo = function(monto){
			//puntasPorTramo = 'SI';
			var montoTramoAnterior = 999999999999;
			var continuar = true; // 999999999999;
			var montoPunta = '';
			
			if (monto > (datosTramos[datosTramos.length-1].MontoPorTramo)){
				if ($scope.data.OperacionSeleccionado.IdTipoOperacion === 'COMSPT')
					montoPunta = datosTramos[datosTramos.length-1].PuntaCompra||'';
				else if ($scope.data.OperacionSeleccionado.IdTipoOperacion === 'VENSPT')
					montoPunta = datosTramos[datosTramos.length-1].PuntaVenta||'';

				if ($scope.parametrosEntrada.Opcion === undefined){
					$scope.data.kngPuntaCompra = datosTramos[0].PuntaCompraDefault||'';
					$scope.data.kngPuntaVenta = datosTramos[0].PuntaVentaDefault||'';
				}
			}
			else if(monto < (datosTramos[0].MontoPorTramo)){
				if ($scope.data.OperacionSeleccionado.IdTipoOperacion === 'COMSPT')
					montoPunta = datosTramos[0].PuntaCompra||'';
				else if ($scope.data.OperacionSeleccionado.IdTipoOperacion === 'VENSPT')
					montoPunta = datosTramos[0].PuntaVenta||'';
				
				if ($scope.parametrosEntrada.Opcion === undefined){
					$scope.data.kngPuntaCompra = datosTramos[0].PuntaCompraDefault||'';
					$scope.data.kngPuntaVenta = datosTramos[0].PuntaVentaDefault||'';
				}
			}
			else{
				$.each (datosTramos,function(index,value){
					//(continuar === true)
	        		if ((monto >= value.MontoPorTramo)){
						if ($scope.data.OperacionSeleccionado.IdTipoOperacion === 'COMSPT')
							montoPunta = value.PuntaCompra||'';
						else if ($scope.data.OperacionSeleccionado.IdTipoOperacion === 'VENSPT')
							montoPunta = value.PuntaVenta||'';

						if ($scope.parametrosEntrada.Opcion === undefined){
							$scope.data.kngPuntaCompra = value.PuntaCompraDefault||'';
							$scope.data.kngPuntaVenta = value.PuntaVentaDefault||'';
						}
						montoTramoAnterior = value.MontoPorTramo
						continuar = false
	        		}
	        		else{
	        			montoTramoAnterior = value.MontoPorTramo;
	        		}
	        	});
			}
			
			// SAP-6525 (MSH 11-05-2017) 
			// Acuerdo GC + JO 
			//		+ TCCierre SIEMPRE debe ir en cero (Solo cuando es ingreso)
			//		+ TCCosto SOLO debe setear cuando el primer tramo es > 0 			

			if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico !== 'ARBINT'){ 
				if(monto === 0 && datosTramos[0].MontoPorTramo > 0){
					montoPunta = 0;
				}
				if(($scope.data.cargaInicialDatos === 'OK' && $scope.parametrosEntrada.Opcion === "MODIFICAR") || 
				   ($scope.parametrosEntrada.Opcion === undefined)){
					//$scope.data.kngTipoCambioCosto = montoPunta || '0';
					if($scope.data.kngTipoCambioCosto !== tcCostoGuardado && !tcCostoGuardado){
						$scope.data.kngTipoCambioCosto = montoPunta || '0';
					}
				}
			}
			
			// if (isNaN($scope.data.kngTipoCambioCierre) || $scope.data.kngTipoCambioCierre === undefined )
			//	$scope.data.kngTipoCambioCierre = montoPunta||'';
			
			if($scope.parametrosEntrada.Opcion !== 'MODIFICAR' && $scope.parametrosEntrada.Opcion !== 'ANULAR' && 
			   $scope.parametrosEntrada.Opcion !== 'CONSULTAR'){
				//$scope.data.kngTipoCambioCierre = 0;
			}			
		}
		
		
		obtienePuntas = function(){
			if(!$scope.data.tipoInstrumentoSeleccionado) return;
			var parametrosPuntas = {};
			parametrosPuntas.p_IdEmpresa = $scope.SesionIdEmpresa;
			if ($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'ARBINT' || $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'ARBDIS'){
				parametrosPuntas.p_IdMoneda = $scope.data.MonedaSecundariaSeleccionado.IdMonedaSecundaria || 'USD';
			}	
			else{
				parametrosPuntas.p_IdMoneda = $scope.data.MonedaPrincipalSeleccionado.IdMonedaPrincipal || 'USD';
			}
			parametrosPuntas.p_MontoPorTramo = 0;
			
			
			bcsComunes.log().info(JSON.stringify(parametrosPuntas));	
	       	bcsServiciosMesa.ConsultarPuntasPorTramo(parametrosPuntas,function(respuesta){
				if (respuesta.P_OUT_ERRORESCONTADOR == 0 && respuesta.P_OUT_ADVERTENCIASCONTADOR == 0){
					bcsComunes.setConflictos(util.parseServerResponse(respuesta));
					
					if ((respuesta.P_OUT_PUNTAS[0].CheckPrecioPorTramo||'NO') === 'NO'){
						puntasPorTramo = 'NO';

						if(($scope.data.cargaInicialDatos === 'OK' && $scope.parametrosEntrada.Opcion === "MODIFICAR") || 
						   ($scope.parametrosEntrada.Opcion === undefined)){
						   	if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'COMSPT' && 
							   $scope.data.tipoInstrumentoSeleccionado.Nemotecnico !== 'ARBINT' &&
							   $scope.data.kngTipoCambioCosto !== respuesta.P_OUT_PUNTAS[0].PuntaCompra) {								
								
								if (!$scope.data.kngTCCierreEditadoManualmente && !$scope.data.kngTipoCambioCierre && $scope.data.kngTipoCambioCosto)
									$scope.data.kngTipoCambioCierre = $scope.data.kngTipoCambioCosto || '';
								if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'INTERB')
									$scope.data.kngTipoCambioCosto = Number(respuesta.P_OUT_PUNTAS[0].PuntaCompraDefault);								
							}
							if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'VENSPT' && 
							   $scope.data.tipoInstrumentoSeleccionado.Nemotecnico !== 'ARBINT' &&
							   $scope.data.kngTipoCambioCosto !== respuesta.P_OUT_PUNTAS[0].PuntaVenta ) {
																
								if (!$scope.data.kngTCCierreEditadoManualmente && !$scope.data.kngTipoCambioCierre && $scope.data.kngTipoCambioCosto)
									$scope.data.kngTipoCambioCierre = $scope.data.kngTipoCambioCosto || '';

								if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'INTERB')
									$scope.data.kngTipoCambioCosto = Number(respuesta.P_OUT_PUNTAS[0].PuntaVentaDefault);
							}
				   		}												
					}
					else{
						datosTramos = respuesta.P_OUT_PUNTAS;
						puntasPorTramo = 'SI';
						if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'INTERB'){
							
                            $scope.data.kngPuntaVenta = Number(respuesta.P_OUT_PUNTAS[0].PuntaVentaDefault)||'';
                            $scope.data.kngPuntaCompra = Number(respuesta.P_OUT_PUNTAS[0].PuntaCompraDefault)||'';
                            
							if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'VENSPT'){
								$scope.data.kngTipoCambioCosto = Number(respuesta.P_OUT_PUNTAS[0].PuntaVentaDefault);
							}else{
								$scope.data.kngTipoCambioCosto = Number(respuesta.P_OUT_PUNTAS[0].PuntaCompraDefault);
							}
						}else{
							obtieneCostoPorTramo(($scope.data.kngMontoPrincipal||0));
						}
							
							
						
					}
					calculoMontos();
					$scope.data.tipoCambioCostoCorrespondiente = $scope.data.kngTipoCambioCosto;
					$scope.data.MensajeSinPuntasPorTramo = false;
				}
				else{			
					bcsComunes.setConflictos(util.parseServerResponse(respuesta));
					$scope.data.kngPuntaCompra = '';
					$scope.data.kngPuntaVenta = '';
					$.each(respuesta.P_OUT_ADVERTENCIASDETALLE,function(key,value){
						if(!$scope.data.MensajeSinPuntasPorTramo){
							bcsComunes.msg("warning","warning",$translate.instant(value.Descripcion));
							$scope.data.MensajeSinPuntasPorTramo = true;
						}
					});
					$.each(respuesta.P_OUT_ERRORESDETALLE,function(key,value){
						bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant(value.Descripcion));
					});
					$scope.data.kngTipoCambioCosto = '';
				}
				if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'INTERB')
					$scope.data.kngMargen = '';
				bcsComunes.ventanaOverlay(idVentana, false);
	       	}		
	       	,error);

		}
		
		obtieneTCCosto = function(){
			if(!$scope.data.tipoInstrumentoSeleccionado) return;
			var parametrosPuntas = {};
			parametrosPuntas.p_IdEmpresa = $scope.SesionIdEmpresa;
			if ($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'ARBINT' || $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'ARBDIS'){
				parametrosPuntas.p_IdMoneda = $scope.data.MonedaSecundariaSeleccionado.IdMonedaSecundaria || 'USD';
			}	
			else{
				parametrosPuntas.p_IdMoneda = $scope.data.MonedaPrincipalSeleccionado.IdMonedaPrincipal || 'USD';
			}
			parametrosPuntas.p_MontoPorTramo = 0;
						
			bcsComunes.log().info(JSON.stringify(parametrosPuntas));	
	       	bcsServiciosMesa.ConsultarPuntasPorTramo(parametrosPuntas,function(respuesta){
				if (respuesta.P_OUT_ERRORESCONTADOR == 0 && respuesta.P_OUT_ADVERTENCIASCONTADOR == 0){
					bcsComunes.setConflictos(util.parseServerResponse(respuesta));
					
					if ((respuesta.P_OUT_PUNTAS[0].CheckPrecioPorTramo||'NO') === 'NO'){
						puntasPorTramo = 'NO';

						if(($scope.data.cargaInicialDatos === 'OK' && $scope.parametrosEntrada.Opcion === "MODIFICAR") || 
						   ($scope.parametrosEntrada.Opcion === undefined)){
						   	if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'COMSPT' && 
							   $scope.data.tipoInstrumentoSeleccionado.Nemotecnico !== 'ARBINT' &&
							   $scope.data.kngTipoCambioCosto !== respuesta.P_OUT_PUNTAS[0].PuntaCompra) {								

								if($scope.data.kngTipoCambioCosto !== tcCostoGuardado && !tcCostoGuardado){
									$scope.data.kngTipoCambioCosto = respuesta.P_OUT_PUNTAS[0].PuntaCompra||'';
								}
								if (!$scope.data.kngTCCierreEditadoManualmente && !$scope.data.kngTipoCambioCierre && $scope.data.kngTipoCambioCosto)
									$scope.data.kngTipoCambioCierre = $scope.data.kngTipoCambioCosto || '';
								if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'INTERB')
									$scope.data.kngTipoCambioCosto = Number(respuesta.P_OUT_PUNTAS[0].PuntaCompraDefault);								
							}
							if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'VENSPT' && 
							   $scope.data.tipoInstrumentoSeleccionado.Nemotecnico !== 'ARBINT' &&
							   $scope.data.kngTipoCambioCosto !== respuesta.P_OUT_PUNTAS[0].PuntaVenta ) {
								
								if($scope.data.kngTipoCambioCosto !== tcCostoGuardado && !tcCostoGuardado){
									$scope.data.kngTipoCambioCosto = respuesta.P_OUT_PUNTAS[0].PuntaVenta||'';
								}
								if (!$scope.data.kngTCCierreEditadoManualmente && !$scope.data.kngTipoCambioCierre && $scope.data.kngTipoCambioCosto)
									$scope.data.kngTipoCambioCierre = $scope.data.kngTipoCambioCosto || '';

								if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'INTERB')
									$scope.data.kngTipoCambioCosto = Number(respuesta.P_OUT_PUNTAS[0].PuntaVentaDefault);
							}
				   		}
						
						if ($scope.parametrosEntrada.Opcion === undefined){
							$scope.data.kngPuntaCompra = Number(respuesta.P_OUT_PUNTAS[0].PuntaCompraDefault)||'';
							$scope.data.kngPuntaVenta = Number(respuesta.P_OUT_PUNTAS[0].PuntaVentaDefault)||'';
						}
					}
					else{
						datosTramos = respuesta.P_OUT_PUNTAS;
						puntasPorTramo = 'SI';
						if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'INTERB'){
							
                            $scope.data.kngPuntaVenta = Number(respuesta.P_OUT_PUNTAS[0].PuntaVentaDefault)||'';
                            $scope.data.kngPuntaCompra = Number(respuesta.P_OUT_PUNTAS[0].PuntaCompraDefault)||'';
                            
							if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'VENSPT'){
								$scope.data.kngTipoCambioCosto = Number(respuesta.P_OUT_PUNTAS[0].PuntaVentaDefault);
							}else{
								$scope.data.kngTipoCambioCosto = Number(respuesta.P_OUT_PUNTAS[0].PuntaCompraDefault);
							}
						}else{
							obtieneCostoPorTramo(($scope.data.kngMontoPrincipal||0));
						}																				
					}
					calculoMontos();
					$scope.data.tipoCambioCostoCorrespondiente = $scope.data.kngTipoCambioCosto;
					$scope.data.MensajeSinPuntasPorTramo = false;
				}
				else{			
					bcsComunes.setConflictos(util.parseServerResponse(respuesta));
					$scope.data.kngPuntaCompra = '';
					$scope.data.kngPuntaVenta = '';
					$.each(respuesta.P_OUT_ADVERTENCIASDETALLE,function(key,value){
						if(!$scope.data.MensajeSinPuntasPorTramo){
							bcsComunes.msg("warning","warning",$translate.instant(value.Descripcion));
							$scope.data.MensajeSinPuntasPorTramo = true;
						}
					});
					$.each(respuesta.P_OUT_ERRORESDETALLE,function(key,value){
						bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant(value.Descripcion));
					});
					$scope.data.kngTipoCambioCosto = '';
				}
				if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'INTERB')
					$scope.data.kngMargen = '';
				bcsComunes.ventanaOverlay(idVentana, false);
	       	}		
	       	,error);

		}

		var calculoMontos = function(){
			if($scope.parametrosEntrada.Opcion === undefined || $scope.parametrosEntrada.Opcion === "MODIFICAR"){
				//Compra
				if($scope.data.OperacionSeleccionado && $scope.data.OperacionSeleccionado.IdTipoOperacion === 'COMSPT'){
					if($scope.data.MonedaSecundariaSeleccionado && $scope.data.MonedaSecundariaSeleccionado.IdMonedaSecundaria === 'CLP'){
						$scope.data.MargenPrincipal = ($scope.data.kngTipoCambioCosto - $scope.data.kngTipoCambioCierre) * $scope.data.kngMontoPrincipal;
					}
					else if($scope.data.MonedaSecundariaSeleccionado && $scope.data.MonedaSecundariaSeleccionado.IdMonedaSecundaria !== 'CLP'){
						if($scope.data.MonedaPrincipalSeleccionado.MultiplicaODivide === 'D'){
							var parte1 = $scope.data.kngParidadCosto  ? ($scope.data.kngMontoPrincipal / $scope.data.kngParidadCosto) : 0;
							var parte2 = $scope.data.kngParidadCierre ? ($scope.data.kngMontoPrincipal / $scope.data.kngParidadCierre) : 0;
							$scope.data.MargenPrincipal = parte1 - parte2;
						}
						else{
							$scope.data.MargenPrincipal = ($scope.data.kngParidadCosto - $scope.data.kngParidadCierre) * $scope.data.kngMontoPrincipal;
						}
						$scope.data.MargenBidAsk = ($scope.data.kngParidadCosto - $scope.data.kngParidadCierre) * $scope.data.kngMontoPrincipal;
					}
				}
				//Venta
				else if($scope.data.OperacionSeleccionado && $scope.data.OperacionSeleccionado.IdTipoOperacion === 'VENSPT'){
					if($scope.data.MonedaSecundariaSeleccionado && $scope.data.MonedaSecundariaSeleccionado.IdMonedaSecundaria === 'CLP'){
						$scope.data.MargenPrincipal = ($scope.data.kngTipoCambioCierre - $scope.data.kngTipoCambioCosto) * $scope.data.kngMontoPrincipal;
					}
					else if($scope.data.MonedaSecundariaSeleccionado && $scope.data.MonedaSecundariaSeleccionado.IdMonedaSecundaria !== 'CLP'){
						if($scope.data.MonedaPrincipalSeleccionado.MultiplicaODivide === 'D'){
							var parte1 = $scope.data.kngParidadCosto  ? ($scope.data.kngMontoPrincipal / $scope.data.kngParidadCosto) : 0;
							var parte2 = $scope.data.kngParidadCierre ? ($scope.data.kngMontoPrincipal / $scope.data.kngParidadCierre) : 0;
							$scope.data.MargenPrincipal = parte2 - parte1;
						}
						else{
							$scope.data.MargenPrincipal = ($scope.data.kngParidadCierre - $scope.data.kngParidadCosto) * $scope.data.kngMontoPrincipal;
						}
						$scope.data.MargenBidAsk = ($scope.data.kngParidadCierre - $scope.data.kngParidadCosto) * $scope.data.kngMontoPrincipal;
					}
				}
				
				$scope.data.kngMargen = $scope.data.MargenPrincipal;
			}
		};		

		var IdTipoCanalIngreso;
		var fnCargoValoresSpot = function(){ //TODO
			console.log("Modificacion Spot");

		    if ($scope.data.cargaInicialDatos === 'OK') return;
			if(($scope.data.MonedaPrincipalSeleccionado.IdMonedaPrincipal||'undefined') === 'undefined') return;
			if(($scope.data.MonedaSecundariaSeleccionado.IdMonedaSecundaria||'undefined') === 'undefined') return;
			
			/* Parámetro que habilita la pestaña de Anticipo cuando el Formulario se llama del Blotter 
			 * Consulta de Operaciones -> Anticipo */
			//$scope.HabilitoTabAnticipo = true;
			var ConsultaTemplate = new bcs.DataSource(dsDefConsultaMovimientosSPT);
			var filterTemplate = [{ field: "IdOperacion"   ,operator:"eq", value: $scope.parametrosEntrada.IdOperacion }];
			var filtros = bcsServiciosGrillasMesaRF.validarFiltros({"SesionIdEmpresa" : $scope.SesionIdEmpresa,"IdOperacion":$scope.parametrosEntrada.IdOperacion,"IdTipoProducto":$scope.parametrosEntrada.IdTipoProducto},'OPERACION');
			
			ConsultaTemplate.query({ filter: filterTemplate }).then(function(){
				if(ConsultaTemplate.view().length > 0){
					/*Datos de la Operación*/
				    $scope.data.idOperacionAModificar	= ConsultaTemplate.view()[0].IdOperacion;
					//$scope.data.oClientePortafolio.IdActual	= ConsultaTemplate.view()[0].IdSocioDeNegocio;
					$scope.data.oClientePortafolio.idActual	= ConsultaTemplate.view()[0].IdPortafolio;
					$scope.data.oClientePortafolio.idDefecto = ConsultaTemplate.view()[0].IdPortafolio;
					$scope.data.kngFechaOperacion		= ConsultaTemplate.view()[0].FechaOperacion;
					$scope.data.MonedaPrincipalIdActual	= ConsultaTemplate.view()[0].IdMonedaPrincipal;
					$scope.data.MonedaSecundariaIdActual= ConsultaTemplate.view()[0].IdMonedaSecundaria;
					$scope.data.idPostFiltroMonedaPrincipal = ConsultaTemplate.view()[0].IdMonedaPrincipal;
					$scope.data.idPostFiltroMonedaSecundaria = ConsultaTemplate.view()[0].IdMonedaSecundaria;
					$scope.data.TipoOperacionIdActual	= ConsultaTemplate.view()[0].IdTipoOperacion;
					$scope.data.CodigoComercio = ConsultaTemplate.view()[0].CodigoComercio;
					$scope.data.oConfiguracionTipoInstrumento.idActual = ConsultaTemplate.view()[0].NombreInstrumento;
					$scope.data.tipoInstrumentoAUX = ConsultaTemplate.view()[0].NombreInstrumento;
					$scope.data.tipoInstrumento = ConsultaTemplate.view()[0].Nemotecnico;
					$scope.data.oConfiguracionTipoInstrumento.filtros.Nemotecnico.value = ConsultaTemplate.view()[0].Nemotecnico;
					$scope.data.kngMontoPrincipal = ConsultaTemplate.view()[0].MontoMonedaPrincipal;
					$scope.data.kngMontoSecundario = ConsultaTemplate.view()[0].MontoMonedaSecundaria;
					$scope.data.kngTipoCambioCierre = ConsultaTemplate.view()[0].TipoCambioCierre;
					$scope.data.kngParidadCierre = ConsultaTemplate.view()[0].ParidadCierre;
					$scope.data.kngTipoCambioCosto = ConsultaTemplate.view()[0].TipoCambioCosto;
					$scope.data.kngParidadCosto = ConsultaTemplate.view()[0].ParidadCosto;
					$scope.data.kngMontoEquivalente = ConsultaTemplate.view()[0].MontoMonedaSecundaria;
					$scope.data.kngMargen = ConsultaTemplate.view()[0].Margen;
					$scope.data.oConfiguracionMesaIdActual = ConsultaTemplate.view()[0].IdMesa;
					$scope.data.kngPuntaCompra = ConsultaTemplate.view()[0].PuntaCompra;
					$scope.data.kngPuntaVenta = ConsultaTemplate.view()[0].PuntaVenta;
					$scope.data.oConfiguracionTipoComprobante.idActual = ConsultaTemplate.view()[0].TipoComprobanteFacturacion;
					$scope.data.TipoCambioCierre = ConsultaTemplate.view()[0].TipoCambioCierre;
					$scope.data.oConfiguracionContactos.idActual = ConsultaTemplate.view()[0].IdContacto;
					$scope.data.oConfiguracionContactos.idDefecto= ConsultaTemplate.view()[0].IdContacto;
					$scope.data.oConfiguracionDocumentos.idActual = ConsultaTemplate.view()[0].TipoDocumento;
					$scope.data.oConfiguracionDocumentos.idDefecto = ConsultaTemplate.view()[0].TipoDocumento;
					//$scope.data.oClientePortafolio.seleccionado.IdPersonaEjecutivo = 1;
					//$scope.data.kngEquivalenteGasto = 1;
					//$scope.data.kngTipoCambioCierreGasto = 1;
					$scope.data.Comentario = ConsultaTemplate.view()[0].Observacion;
				    $scope.data.bloqueoModificacion = 'true';
				    $scope.data.idSolicitudAporteRescate	= ConsultaTemplate.view()[0].IdSolicitudAporteRescate || 0;
				    
				    tcCostoGuardado = ConsultaTemplate.view()[0].TipoCambioCosto;
					IdTipoCanalIngreso = ConsultaTemplate.view()[0].IdTipoCanalIngreso;
					
					//Valida si el tipo canal de ingreso es 'CLI' 
					if(IdTipoCanalIngreso==='CLI'){ 
						$scope.data.EsIdCliente = true; 
					} 
					
				    //FORMAS DE PAGO
				    //usa la directiva para cargar con el IdOperacion que esta en los parametros
				    $scope.data.DeshabilitaFormaPago = $scope.parametrosEntrada.Opcion === 'CONSULTAR'?true:false;
				    $scope.data.cargaInicialDatos = 'OK';
					$scope.data.Ficticia = ConsultaTemplate.view()[0].OperacionFicticia === 'S' || 'N';
				}
			},error);
		};
		
		
		var successReferenciaMercado = function(respuesta){
			$.each(respuesta.P_OUT_INDICADORES,function(key,value){
				if (value.IdIndicador === 'UF'){
					$scope.data.kngMontoUf = Number(value.TipoCambio);
				}
				else if (value.IdIndicador === 'DO'){
					$scope.data.kngMontoDolar = Number(value.TipoCambio);
				}
			}); 
			datosIndicadores = respuesta.P_OUT_INDICADORES;
		};
		
		var error = function(respuesta){
			bcsComunes.ventanaMsg({
				//mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"),
				conflictos:util.parseServerResponse(respuesta)
			},idVentana);
			bcsComunes.ventanaOverlay(idVentana, false);
			bcsComunes.ventanaCerrar(idVentana);
		};

		var errorTerminal = function(respuesta){
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_INGRESO_SPOT"),
				conflictos:util.parseServerResponse(respuesta)
			},idVentana);
			bcsComunes.ventanaOverlay(idVentana, false);
			bcsComunes.ventanaCerrar(idVentana);
		};
		
		var successIngreso = function(respuesta){
			if(respuesta.P_OUT_ERRORESCONTADOR == 0 && respuesta.P_OUT_ADVERTENCIASCONTADOR == 0){
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_OPERACION_INGRESO_EXITO")+": " +$translate.instant("UI_FOLIO_OPERACION")+" " + respuesta.P_OUT_IDOPERACIONSPOT[0].IdOperacionSpot,
					conflictos:util.parseServerResponse(respuesta)
				},idVentana);
				var _savedTipoOperacion = $scope.data.TipoOperacionIdActual;
				$scope.data.limpiar();
				$scope.data.TipoOperacionIdActual = _savedTipoOperacion;
			}
			else if(respuesta.P_OUT_ADVERTENCIASCONTADOR > 0 && respuesta.P_OUT_ERRORESCONTADOR == 0){
				var msgFolioOperacion = respuesta.P_OUT_IDOPERACIONSPOT[0] ? ': ' +$translate.instant("UI_FOLIO_OPERACION")+' '+respuesta.P_OUT_IDOPERACIONSPOT[0].IdOperacionSpot : '';
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_OPERACION_INGRESO_ADVERTENCIA")+msgFolioOperacion,
					conflictos:util.parseServerResponse(respuesta)
				},idVentana);
				var _savedTipoOperacion = $scope.data.TipoOperacionIdActual;
				$scope.data.limpiar();
				$scope.data.TipoOperacionIdActual = _savedTipoOperacion;
			}
			else{
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"),
					conflictos:util.parseServerResponse(respuesta)
				},idVentana);
			}
			bcsComunes.ventanaOverlay(idVentana, false);
		};
		
		var errorIngreso = function(respuesta){
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"),
				conflictos:util.parseServerResponse(respuesta)
			},idVentana);
			bcsComunes.ventanaOverlay(idVentana, false);
		};

		var successModifica = function(respuesta){
			var idPopUp = undefined;
			if(respuesta.P_OUT_ERRORESCONTADOR == 0 && respuesta.P_OUT_ADVERTENCIASCONTADOR == 0){
				idPopUp = bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_MODIFICACION_INGRESO_EXITO"),
							conflictos:util.parseServerResponse(respuesta)
						  },idVentana);
				var _savedTipoOperacion = $scope.data.TipoOperacionIdActual;
				$scope.data.limpiar();
				$scope.data.TipoOperacionIdActual = _savedTipoOperacion;
			}
			else if(respuesta.P_OUT_ADVERTENCIASCONTADOR > 0 && respuesta.P_OUT_ERRORESCONTADOR == 0){
				var msgFolioOperacion = respuesta.P_OUT_IDOPERACIONSPOT[0] ? ': ' +$translate.instant("UI_FOLIO_OPERACION")+' '+respuesta.P_OUT_IDOPERACIONSPOT[0].IdOperacionSpot : '';
				idPopUp = bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_OPERACION_MODIFICA_ADVERTENCIA")+msgFolioOperacion,
							conflictos:util.parseServerResponse(respuesta)
						  },idVentana);
				var _savedTipoOperacion = $scope.data.TipoOperacionIdActual;
				$scope.data.limpiar();
				$scope.data.TipoOperacionIdActual = _savedTipoOperacion;
			}
			else{
				idPopUp = bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_OPERACION_MODIFICA_ERROR"),
							conflictos:util.parseServerResponse(respuesta)
						  },idVentana);
			}
			if(idPopUp){
				var promesa = bcsComunes.ventanaPromesa(idPopUp);
				$.when(promesa).then(function(data){
					bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.ventanaCerrar(idVentana);
				});
			}
			else{
				bcsComunes.ventanaOverlay(idVentana,false);
				bcsComunes.ventanaCerrar(idVentana);
			}
		};

		var successAnulacion = function(respuesta){
			var idPopUp = undefined;
			if(respuesta.P_OUT_ERRORESCONTADOR == 0 && respuesta.P_OUT_ADVERTENCIASCONTADOR == 0){
				idPopUp = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_OPERACION_ANULADA_EXITO"),conflictos:util.parseServerResponse(respuesta)
						  						},idVentana);
			}
			else if(respuesta.P_OUT_ADVERTENCIASCONTADOR){
				idPopUp = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_OPERACION_ANULA_ADVERTENCIA"),conflictos:util.parseServerResponse(respuesta)
												},idVentana);
			}
			else{
				idPopUp = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_OPERACION_ANULA_ERROR"),conflictos:util.parseServerResponse(respuesta)
												},idVentana);
			}
			if(idPopUp){
				var promesa = bcsComunes.ventanaPromesa(idPopUp);
				$.when(promesa).then(function(data){
					bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.ventanaCerrar(idVentana);
				});
			}
			else{
				bcsComunes.ventanaOverlay(idVentana,false);
				bcsComunes.ventanaCerrar(idVentana);
			}
		};
		
		
		//LLAMADA A SERVICIOS SP
		
		var cargaConfiguracionUsuario = function(){
			//Se carga la configuración del usuario	
			var tablaDatosUsuario = [];	
			var ObjUsuario = { "IdUsuario" : $scope.SesionIdUsuario };
			tablaDatosUsuario.push(ObjUsuario);

			var parametros = {};
			parametros.p_IdEmpresa		= $scope.SesionIdEmpresa;
			parametros.p_in_Usuarios	= tablaDatosUsuario;

			bcsComunes.log().info(JSON.stringify(parametros));	
	       	bcsServiciosMesa.ConsultarConfiguracionUsuario(parametros,function(respuesta){
	       		$.each(respuesta.P_OUT_CONFIGURACION,function(key,value){
					if (value.Codigo === "PERMITEDISTRI") usuarioPermiteDistribucion = value.Valor;
					if (value.Codigo === "PERMITEINTERB") usuarioPermiteInterbancario = value.Valor;
					if (value.Codigo === "MARGENBIDASK") $scope.data.utilizaParidadBidAsk = value.Valor;
					if (value.Codigo === "ESTADOMESA") $scope.data.EstadoMesa = value.Valor;
				});
				bcsComunes.ventanaOverlay(idVentana, false);
				
		       	if(usuarioPermiteDistribucion === 'S' && usuarioPermiteInterbancario === 'S' && $scope.data.EstadoMesa === 'A'){
		    		cargaComboboxMonedaPrincipalSimple();
		    		cargaComboboxMonedaSecundariaSimple();
		       	}
		       	else if((usuarioPermiteDistribucion === 'N' && usuarioPermiteInterbancario === 'N') || $scope.data.EstadoMesa === 'C'){
		       		$scope.MonedaPrincipalNemotecnico1.value = 'X';
		       		$scope.MonedaPrincipalNemotecnico2.value = 'X';
		       		$scope.MonedaSecundariaNemotecnico1.value = 'X';
		       		$scope.MonedaSecundariaNemotecnico2.value = 'X';
		    		cargaComboboxMonedaPrincipalComplejo();
		    		cargaComboboxMonedaSecundariaComplejo();
		    		
		    		errorTerminal(respuesta);
		       	}
		       	else{
		       		if (usuarioPermiteDistribucion === 'S'){
			       		$scope.MonedaPrincipalNemotecnico1.value = 'DISTRI';
			       		$scope.MonedaPrincipalNemotecnico2.value = 'ARBDIS';
			       		$scope.MonedaSecundariaNemotecnico1.value = 'DISTRI';
			       		$scope.MonedaSecundariaNemotecnico2.value = 'ARBDIS';
		       		};
		       		if (usuarioPermiteInterbancario === 'S'){
			       		$scope.MonedaPrincipalNemotecnico1.value = 'INTERB';
			       		$scope.MonedaPrincipalNemotecnico2.value = 'ARBINT';
			       		$scope.MonedaSecundariaNemotecnico1.value = 'INTERB';
			       		$scope.MonedaSecundariaNemotecnico2.value = 'ARBINT';
		       		};
		    		cargaComboboxMonedaPrincipalComplejo();
		    		cargaComboboxMonedaSecundariaComplejo();
		       	}
	       	}
   			,error);
	    };
	    
	    
	    var actualizaMontoFormaPago = function(){
	    	if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'COMSPT'){
				if ($scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'ENTREGAMOS'){
					$scope.data.MontoPagoDecimales = $scope.data.MonedaSecundariaSeleccionado.MonedaSecundariaDecimales;
					$scope.data.kngMontoPago = ($scope.data.kngMontoSecundario||0)-totalMontoFormaPagoEnt;
				}
				else if ($scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'RECIBIMOS'){
					$scope.data.MontoPagoDecimales = $scope.data.MonedaPrincipalSeleccionado.MonedaPrincipalDecimales;
					$scope.data.kngMontoPago = ($scope.data.kngMontoPrincipal||0)-totalMontoFormaPagoRec;
				}
			}
	    	else if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'VENSPT'){
				if ($scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'ENTREGAMOS'){
					$scope.data.MontoPagoDecimales = $scope.data.MonedaPrincipalSeleccionado.MonedaPrincipalDecimales;
					$scope.data.kngMontoPago = ($scope.data.kngMontoPrincipal||0)-totalMontoFormaPagoEnt;
				}
				else if ($scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'RECIBIMOS'){
					$scope.data.MontoPagoDecimales = $scope.data.MonedaSecundariaSeleccionado.MonedaSecundariaDecimales;
					$scope.data.kngMontoPago = ($scope.data.kngMontoSecundario||0)-totalMontoFormaPagoRec;
				}
			}
	    };

		$scope.data.IngresoMedioDePago = function(){
			dsCargoMedioDePago();
		};
		
	    $scope.data.EliminaMedioDePago = function(FormaPago,fila){
	    	if (fila){
				if(FormaPago === 'ENTREGAMOS'){
					totalMontoFormaPagoEnt = totalMontoFormaPagoEnt - fila.Monto;
					dsMedioDePago.remove(fila);
					//actualizaMontoFormaPago();
				}
				else if(FormaPago === 'RECIBIMOS'){
					totalMontoFormaPagoRec = totalMontoFormaPagoRec - fila.Monto;
					dsMedioDePagoRec.remove(fila);
					//actualizaMontoFormaPago();
				}
	    	}
			
			//var tramosPunta = dsAux.remove(fila);
	    };
	    
	    
		$scope.data.limpiar = function(){
			//Limpiar grillas medios de pago
        	dsMedioDePago.removeAll();
			dsMedioDePagoRec.removeAll();
			totalMontoFormaPagoEnt = 0;
			totalMontoFormaPagoRec = 0;
			$scope.data.FechaPagoDisabled = 'true';
			

			//Limpia id Cliente / Portafolio
			$scope.data.oClientePortafolio = { };
			$scope.data.oClientePortafolio.seleccionado = false;
			//Fin Limpia id Cliente / Portafolio
			
			$scope.data.bloqueoSolicitudPuntas = false;
			$scope.data.bloqueoSolicitudPuntasAceptar = false;
			$scope.IdSolicitudPuntas = 0;

			$scope.data.oConfiguracionTipoInstrumento.idActual = false;
			$scope.data.TipoOperacionIdActual = false;
			
			$scope.data.kngMontoPrincipal = '';
			$scope.data.kngMontoSecundario = '';
			$scope.data.kngTipoCambioCierre = '';
			$scope.data.kngTCCierreEditadoManualmente = false;
			$scope.data.kngParidadCierre = '';
			$scope.data.kngTipoCambioCosto = '';
			$scope.data.kngParidadCosto = '';
			$scope.data.kngMontoEquivalente = '';
			$scope.data.kngMargen = '';
			$scope.data.kngEquivalenteGasto = '';
			$scope.data.kngTipoCambioCierreGasto = '';
			$scope.data.kngMontoPago = '';
			$scope.data.Comentario = '';
			$scope.data.limpiarFormaPago = true;
			varFicticia = undefined;
			$scope.data.Ficticia = false;
			tcCostoGuardado = 0;
			fnVolverTipoInstrumentoPredeterminado();

		};
		
		var fnVolverTipoInstrumentoPredeterminado = function(){
			let tablaDatosUsuario1 = [];	
			let  ObjUsuario = { "IdUsuario" : $scope.SesionIdUsuario };
			tablaDatosUsuario1.push(ObjUsuario);

			let parametros1 = {};
			parametros1.p_IdEmpresa		= $scope.SesionIdEmpresa;
			parametros1.p_in_Usuarios	= tablaDatosUsuario1;
			
	       	bcsServiciosMesa.ConsultarConfiguracionUsuario(parametros1,function(respuesta){
	       		$.each(respuesta.P_OUT_CONFIGURACION,function(key,value){
					if (value.Codigo === "PERMITEDISTRI") {
						if(value.Valor == 'S') $scope.data.permiteDist = 'DISTIBUCION'
					}
					if (value.Codigo === "PERMITEINTERB") {
						if(value.Valor == 'S') $scope.data.permiteInterb = 'INTERBANCARIO'
					}
					 
				});
				
	       	}
   			,error);
		}
		
		$scope.IngresarSolicitudPuntas = function() {		
				
			bcsComunes.ventanaOverlay(idVentana, true);
			var ventanaModalId = bcsComunes.ventanaAbrir("IngresarSolicitudPuntas", 
					{TipoOperacion: $scope.data.OperacionSeleccionado, Monto: $scope.data.MontoPrincipal, Cliente: $scope.data.oClientePortafolio.seleccionado,
					 Moneda: $scope.data.MonedaPrincipalSeleccionado, TipoCambioCierre: $scope.data.TipoCambioCierre, TipoCambioCosto: $scope.data.TipoCambioCosto}, false,false,idVentana);	
			
			var promesa=bcsComunes.ventanaPromesa(ventanaModalId);	        
	        promesa.then(function(data){ 
	        	if( typeof data.P_OUT_MENSAJES?.[0].id !== 'undefined' ) {
	        		$scope.IdSolicitudPuntas = data.P_OUT_MENSAJES[0].id;
	        		$scope.data.bloqueoSolicitudPuntas = true;
	        		$scope.data.bloqueoSolicitudPuntasAceptar = true;
	        	}
	        	else{
	        		$scope.data.bloqueoSolicitudPuntas = false;
	        	}
	        		bcsComunes.ventanaOverlay(idVentana, false);
	    	});
		};
		
		
		$scope.data.aceptar = function(varEstadoOperacion){
			if ($scope.parametrosEntrada.Opcion === "MODIFICAR"){
				$scope.data.modificaSpot(varEstadoOperacion);
			}
			else{
				$scope.data.ingresarSpot(varEstadoOperacion);
			}
		};
		
		$scope.data.ingresarSpot = function(varEstadoOperacion){
			var varSolicitudTipoCambio = 'N';
			
			if(!$scope.data.tipoInstrumentoSeleccionado){
				bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('UI_DEBE_SELECCIONAR_TIPO_INSTRUMENTO'));
				return;
			}
			
			if(varEstadoOperacion === 'VIG'){
				if(($scope.data.MonedaSecundariaIdActual !== 'CLP') && (($scope.data.kngParidadCierre === undefined) || $scope.data.kngParidadCierre === 0 || $scope.data.kngParidadCierre === '')){
					bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('PARIDAD_CIERRE_IGUAL_CERO'));
					return;
				}
				if(($scope.data.MonedaSecundariaIdActual !== 'CLP') && ($scope.data.tipoInstrumentoSeleccionado.Nemotecnico !== 'ARBINT') && (($scope.data.kngParidadCosto === undefined) || $scope.data.kngParidadCosto === 0 || $scope.data.kngParidadCosto === '')){
					bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('PARIDAD_COSTO_IGUAL_CERO'));
					return;
				}

				if($scope.data.formaDePagoEntregamosTotal===0/* && !$scope.data.operaCuentaInversion*/){
					bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('FORMA_DE_PAGO_ENTREGAMOS_INCOMPLETO'));
					return;
				}
				if($scope.data.formaDePagoRecibimosTotal===0/* && !$scope.data.operaCuentaInversion*/){
					bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('FORMA_DE_PAGO_RECIBIMOS_INCOMPLETO'));
					return;
				}
				if(Object.keys($scope.data.matrizFormaPagoEntregamos).length>1){
					var keys = Object.keys($scope.data.matrizFormaPagoEntregamos);
					var fechasCoinciden =
						keys.every(k =>
							new Date($scope.data.matrizFormaPagoEntregamos[k].FechaLiquidacion).getTime() ===
							new Date($scope.data.matrizFormaPagoEntregamos[keys[0]].FechaLiquidacion).getTime()
						);
	
					if (!fechasCoinciden) {
						bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('FORMA_DE_PAGO_ENTREGAMOS_MULTIPLES_VALUTA'));
						return;
					}
				}
				if(Object.keys($scope.data.formaDePagoRecibimos).length>1){
					var keys = Object.keys($scope.data.matrizFormaPagoRecibimos);
					var fechasCoinciden =
						keys.every(k =>
							new Date($scope.data.matrizFormaPagoRecibimos[k].FechaLiquidacion).getTime() ===
							new Date($scope.data.matrizFormaPagoRecibimos[keys[0]].FechaLiquidacion).getTime()
						);
	
					if (!fechasCoinciden) {
						bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('FORMA_DE_PAGO_RECIBIMOS_MULTIPLES_VALUTA'));
						return;
					}
				}
				if ($scope.data.OperacionSeleccionado.IdTipoOperacion === 'COMSPT'/* && !$scope.data.operaCuentaInversion*/){
					var msjErrorFormaPago = '';
					var entraError = false;
	    			if($scope.data.formaDePagoEntregamosTotal < ($scope.data.kngMontoSecundario||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_ENTREGAMOS_INCOMPLETO'; entraError = true;}
	    			if($scope.data.formaDePagoEntregamosTotal > ($scope.data.kngMontoSecundario||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_ENTREGAMOS_EXCEDE'; entraError = true;}
	    			if($scope.data.formaDePagoRecibimosTotal < ($scope.data.kngMontoPrincipal||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_RECIBIMOS_INCOMPLETO'; entraError = true;}
	    			if($scope.data.formaDePagoRecibimosTotal > ($scope.data.kngMontoPrincipal||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_RECIBIMOS_EXCEDE'; entraError = true;}
	    			
	    			if(entraError){
	    				bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant(msjErrorFormaPago));
	    				return;
	    			}
	    		}
	    		else if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'VENSPT'/* && !$scope.data.operaCuentaInversion*/){
	    			var msjErrorFormaPago = '';
					var entraError = false;
	    			if($scope.data.formaDePagoEntregamosTotal < ($scope.data.kngMontoPrincipal||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_ENTREGAMOS_INCOMPLETO'; entraError = true;}
	    			if($scope.data.formaDePagoEntregamosTotal > ($scope.data.kngMontoPrincipal||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_ENTREGAMOS_EXCEDE'; entraError = true;}
	    			if($scope.data.formaDePagoRecibimosTotal < ($scope.data.kngMontoSecundario||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_RECIBIMOS_INCOMPLETO'; entraError = true;}
	    			if($scope.data.formaDePagoRecibimosTotal > ($scope.data.kngMontoSecundario||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_RECIBIMOS_EXCEDE'; entraError = true;}
	    			
	    			if(entraError){
	    				bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant(msjErrorFormaPago));
	    				return;
	    			}
	    		}
			}
			else if(varEstadoOperacion === 'PEN'){
				if(!$scope.data.oClientePortafolio.seleccionado.IdCliente){
					bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('UI_DEBE_SELECCIONAR_CLIENTE'));
					return;
				}
				if(!$scope.data.oClientePortafolio.seleccionado.IdPortafolio){
					bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('UI_DEBE_SELECCIONAR_PORTAFOLIO'));
					return;
				}
			}
			
    		
    		if ((($scope.data.MonedaSecundariaIdActual === 'CLP')&&
	                ($scope.data.tipoInstrumentoSeleccionado.Nemotecnico !== 'DISTRI') &&
	                ($scope.data.tipoInstrumentoSeleccionado.Nemotecnico !== 'ARBDIS')) && 
	                $scope.data.tipoCambioCostoCorrespondiente !== $scope.data.kngTipoCambioCosto){
    			varSolicitudTipoCambio = 'S';
    		}
	 
    		
    		
			//Crear tablas de datos de las grillas				
			var tablaDatosSpot 		= [];		
			var tablaDatosFormaPago	= [];
			
			//valida que valor de margen utilizar
			var varMargen = 0;
			if ($scope.data.utilizaParidadBidAsk === 'S')
				 varMargen = $scope.data.MargenBidAsk; 
			//else if ($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'ARBINT' || $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'ARBDIS' )
				//varMargen = $scope.data.MargenPrincipal;
			else 
				varMargen = $scope.data.kngMargen;
			
			var ObjFormaPago = {};
			var ObjSpot = {
				  "NumeroOperacion"             : 1
				, "IdInstrumento"               : $scope.data.tipoInstrumentoSeleccionado.IdInstrumento || 0
				, "IdTipoOperacion"             : $scope.data.OperacionSeleccionado.Codigo || '' // prueba cambiar en SP a NVARCHAR          
				, "IdTipoProducto"              : $scope.data.OperacionSeleccionado.IdTipoProducto || ''
				, "IdCliente"                   : $scope.data.oClientePortafolio.seleccionado.IdCliente
				, "IdPortafolio"                : $scope.data.oClientePortafolio.seleccionado.IdPortafolio
				, "FechaOperacion"              : $scope.SesionFechaSistema      
				, "IdMonedaPrincipal"           : $scope.data.MonedaPrincipalIdActual || '' // 'USD'    prueba
				, "IdMonedaSecundaria"          : $scope.data.MonedaSecundariaIdActual || '' //'USD' prueba           
				, "MontoMonedaPrincipal"        : $scope.data.kngMontoPrincipal || 0//!!     
				, "MontoMonedaSecundaria"       : $scope.data.kngMontoSecundario || 0  //Monto referencia   
				, "TipoCambioCierre"            : $scope.data.kngTipoCambioCierre || 0     
				, "TipoCambioCierreForward"     : 0     
				, "ParidadCierre"               : $scope.data.kngParidadCierre || 0     
				, "ParidadCierreForward"        : 0     
				, "TipoCambioCosto"             : $scope.data.kngTipoCambioCosto || 0     
				, "ParidadCosto"                : $scope.data.kngParidadCosto || 0     
				, "MontoEquivalente"            : $scope.data.kngMontoEquivalente || 0  //equivale a moneda secundaria
				, "MontoRentabilidad"           : 0     
				, "MontoRentabilidadCanal"      : 0     
				, "Margen"                      : varMargen || 0  
				, "Utilidad"                    : varMargen || 0  
				, "PuntaCompra"                 : $scope.data.kngPuntaCompra ||0     
				, "PuntaVenta"                  : $scope.data.kngPuntaVenta ||0     
				, "SolicitudTipoCambio"         : varSolicitudTipoCambio || 'N'  
				, "IdMesa"                      : $scope.data.oConfiguracionMesaIdActual || ''
				, "IdModulo"                    : 'SPT'       
				, "IdTipoDolar"                 : 1 //$scope.data.oConfiguracionTipoDolar.idActual           
				, "TipoComprobanteFacturacion"  : $scope.data.oConfiguracionTipoComprobante.idActual 
				, "IdAgente"                    : $scope.data.oClientePortafolio.seleccionado.IdPersonaEjecutivo || 0
				, "BenOur"                      : 'BEN'     
				, "EquivalenteConGasto"         : $scope.data.kngEquivalenteGasto || 0     
				, "TipoCambioCierreConGasto"    : $scope.data.kngTipoCambioCierreGasto || 0     
				, "Observacion"                 : $scope.data.Comentario || ''    
				, "CodigoComercio"              : $scope.data.CodigoComercioSeleccionado.IdPlanillaBancoCentral || ''
				, "FolioPlanilla"               : 0            
				, "FolioInterfaz"               : ''        
				, "OperacionFicticia"           : varFicticia||'N'      
				, "IdTipoCanalIngreso"          : 'MES'  
				, "IdDatatec"                   : 0  
				, "Estado"						: varEstadoOperacion
				, "IdSolicitudAporteRescate"	: 0
				, "IdContacto"					:$scope.data.oConfiguracionContactos.seleccionado.IdPersona || 0 
				, "TipoDocumento"				: $scope.data.oConfiguracionDocumentos.idActual || ""
				//, "PagaConCtaInv"				: $scope.data.operaCuentaInversion?'S':'N'
				
			};
			tablaDatosSpot.push(ObjSpot);

			//var RegistrosFormaPago = dsMedioDePago.dataColumns(["IdMedioDePago","NombreMedioDePago"],dsMedioDePago.itemsEdited()) || [];
			var i=0;
			$.each($scope.data.formaDePagoEntregamos,function(key,value) {
				var idFormaDePago = $scope.data.matrizFormaPagoEntregamos[key].IdFormaDePago;
				if (value > 0){
					var idComprobantePagoOperacion = 0;
					$.each($scope.data.formasDePago,function(keyF,valueF) {
						if (valueF.RecibePagaPortafolio === 'P' && valueF.IdFormaDePago === key)
							idComprobantePagoOperacion = valueF.IdComprobantePagoOperacion
					});
					tablaDatosFormaPago.push({
										 "IdInternoPagoOperacion" : ++i
										,"IdComprobantePagoOperacion" : idComprobantePagoOperacion
										,"IdTipoMovimiento"          : $scope.data.TipoOperacionIdActual==='COMSPT'?'MOVING':'MOVEGR'
										,"IdFormaDePago":idFormaDePago
										,"IdOperacion":Number(1)
										,"RecibePagaPortafolio":'P'
										,"IdMoneda":$scope.data.TipoOperacionIdActual==='COMSPT'?$scope.data.MonedaSecundariaIdActual:$scope.data.MonedaPrincipalIdActual
										,"IdMonedaLiquidacion":$scope.data.TipoOperacionIdActual==='COMSPT'?$scope.data.MonedaSecundariaIdActual:$scope.data.MonedaPrincipalIdActual
										,"MontoFormaDePago":value
										,"AfectarTesoreria" :idFormaDePago.includes("_CIN_")?'N':'S'
										,"IdCuentaBancaria" :$scope.data.matrizFormaPagoEntregamos[key].IdCuentaBancaria});
				}
			});
			$.each($scope.data.formaDePagoRecibimos,function(key,value) {
				var idFormaDePago = $scope.data.matrizFormaPagoRecibimos[key].IdFormaDePago;
				if (value > 0){
					var idComprobantePagoOperacion = 0;
					$.each($scope.data.formasDePago,function(keyF,valueF) {
						if (valueF.RecibePagaPortafolio === 'R' && valueF.IdFormaDePago === key)
							idComprobantePagoOperacion = valueF.IdComprobantePagoOperacion
					});
					tablaDatosFormaPago.push({
									 "IdInternoPagoOperacion" : ++i
									,"IdComprobantePagoOperacion" : idComprobantePagoOperacion
									,"IdTipoMovimiento"          : $scope.data.TipoOperacionIdActual==='COMSPT'?'MOVEGR':'MOVING'
									,"IdFormaDePago":idFormaDePago
									,"IdOperacion":Number(1)
									,"RecibePagaPortafolio":'R'
									,"IdMoneda":$scope.data.TipoOperacionIdActual==='COMSPT'?$scope.data.MonedaPrincipalIdActual:$scope.data.MonedaSecundariaIdActual
									,"IdMonedaLiquidacion":$scope.data.TipoOperacionIdActual==='COMSPT'?$scope.data.MonedaPrincipalIdActual:$scope.data.MonedaSecundariaIdActual
									,"MontoFormaDePago":value
									,"AfectarTesoreria" :idFormaDePago.includes("_CIN_")?'N':'S'
									,"IdCuentaBancaria" :$scope.data.matrizFormaPagoRecibimos[key].IdCuentaBancaria});
				}
			});
			
			
			var parametros = {};
			parametros.p_IdEmpresa           = $scope.SesionIdEmpresa;
			parametros.p_IdUsuario           = $scope.SesionIdUsuario;
			parametros.p_IdModulo 			 = 'MSP';
			parametros.p_CalculaMargen       = 'N'
			parametros.p_UsaReglasNegocio    = varEstadoOperacion === 'VIG' ? 'S' : 'N';
			parametros.p_DatosOperacion      = tablaDatosSpot;
			parametros.p_DatosFormaPago      = tablaDatosFormaPago;
			
			bcsComunes.ventanaOverlay(idVentana, true);
			
			if(varEstadoOperacion === 'VIG'){
				if(varMargen < 0){
					var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_INGRESAR_OPERACION_CON_PERDIDA"));
					t.result.then(function(result){
						if(result === OPCIONES_BCSALERTA.SI){
							var p_ValidarPC = {
								p_IdEmpresa			: $scope.SesionIdEmpresa,
								p_IdUsuario			: $scope.SesionIdUsuario,
								p_IdModulo			: 'MSP',
								p_FechaOperacion	: $scope.SesionFechaSistema,
								p_Nemotecnico		: $scope.data.tipoInstrumentoSeleccionado.Nemotecnico,
								p_IdMonedaPrincipal	: $scope.data.MonedaPrincipalIdActual,
								p_TipoCambioCierre	: $scope.data.kngTipoCambioCierre || 0,
								p_ParidadCierre		: $scope.data.kngParidadCierre || 0,
								p_TipoCambioCosto	: $scope.data.kngTipoCambioCosto || 0,
								p_ParidadCosto		: $scope.data.kngParidadCosto || 0
							};
							var idMonPri = $scope.data.MonedaPrincipalIdActual;
					   		
					   		bcsServiciosMesa.ValidarPrecioCierre(p_ValidarPC,function(respuesta){
					   			var limInf = util.bcs_formato(respuesta.P_OUT_SALIDA[0].LimiteInferior,'SPT','Monto',idMonPri,'','').valor;
								var limSup = util.bcs_formato(respuesta.P_OUT_SALIDA[0].LimiteSuperior,'SPT','Monto',idMonPri,'','').valor;
								var msj = $translate.instant(respuesta.P_OUT_SALIDA[0].CodigoTCCierre)+' : '+limInf+'  -  '+limSup+'.  ';
								
								if(respuesta.P_OUT_SALIDA[0].CodigoTCCierre !== 'OK'){
									t = bcsAlerta.crear("PREGUNTA_SI_NO",msj + $translate.instant("UI_CONTINUAR"));
									t.result.then(function(result){
										if(result === OPCIONES_BCSALERTA.SI){
											if(respuesta.P_OUT_SALIDA[0].CodigoTCCosto !== 'OK'){
												msj = $translate.instant(respuesta.P_OUT_SALIDA[0].CodigoTCCosto)+' : '+limInf+'  -  '+limSup+'.  ';
												t = bcsAlerta.crear("PREGUNTA_SI_NO",msj + $translate.instant("UI_CONTINUAR"));
												t.result.then(function(result){
													if(result === OPCIONES_BCSALERTA.SI){
														bcsServiciosMesa.IngresoSpot(parametros,successIngreso,errorIngreso);
													}
													else{
														bcsComunes.ventanaOverlay(idVentana, false);
														return;
													}
												});
											}
											else{
												bcsServiciosMesa.IngresoSpot(parametros,successIngreso,errorIngreso);
											}
										}
										else{
											bcsComunes.ventanaOverlay(idVentana, false);
											return;
										}
									});
								}
								else if(respuesta.P_OUT_SALIDA[0].CodigoTCCosto !== 'OK'){
									msj = $translate.instant(respuesta.P_OUT_SALIDA[0].CodigoTCCosto)+' : '+limInf+'  -  '+limSup+'.  ';
									t = bcsAlerta.crear("PREGUNTA_SI_NO",msj + $translate.instant("UI_CONTINUAR"));
									t.result.then(function(result){
										if(result === OPCIONES_BCSALERTA.SI){
											bcsServiciosMesa.IngresoSpot(parametros,successIngreso,errorIngreso);
										}
										else{
											bcsComunes.ventanaOverlay(idVentana, false);
											return;
										}
									});
								}
								else{
									bcsServiciosMesa.IngresoSpot(parametros,successIngreso,errorIngreso);
							    }
							}
							,function(respuesta){
								bcsServiciosMesa.IngresoSpot(parametros,successIngreso,errorIngreso);
							});
						}
						else{
							bcsComunes.ventanaOverlay(idVentana, false);
							return;
						}
					});
				}
				else{
					var p_ValidarPC = {
						p_IdEmpresa			: $scope.SesionIdEmpresa,
						p_IdUsuario			: $scope.SesionIdUsuario,
						p_IdModulo			: 'MSP',
						p_FechaOperacion	: $scope.SesionFechaSistema,
						p_Nemotecnico		: $scope.data.tipoInstrumentoSeleccionado.Nemotecnico,
						p_IdMonedaPrincipal	: $scope.data.MonedaPrincipalIdActual,
						p_TipoCambioCierre	: $scope.data.kngTipoCambioCierre || 0,
						p_ParidadCierre		: $scope.data.kngParidadCierre || 0,
						p_TipoCambioCosto	: $scope.data.kngTipoCambioCosto || 0,
						p_ParidadCosto		: $scope.data.kngParidadCosto || 0
					};
					var idMonPri = $scope.data.MonedaPrincipalIdActual;

					bcsServiciosMesa.ValidarPrecioCierre(p_ValidarPC,function(respuesta){
						var limInf = util.bcs_formato(respuesta.P_OUT_SALIDA[0].LimiteInferior,'SPT','Monto',idMonPri,'','').valor;
						var limSup = util.bcs_formato(respuesta.P_OUT_SALIDA[0].LimiteSuperior,'SPT','Monto',idMonPri,'','').valor;
						var msj = $translate.instant(respuesta.P_OUT_SALIDA[0].CodigoTCCierre)+' : '+limInf+'  -  '+limSup+'.  ';
						
						if(respuesta.P_OUT_SALIDA[0].CodigoTCCierre !== 'OK'){
							t = bcsAlerta.crear("PREGUNTA_SI_NO",msj + $translate.instant("UI_CONTINUAR"));
							t.result.then(function(result){
								if(result === OPCIONES_BCSALERTA.SI){
									if(respuesta.P_OUT_SALIDA[0].CodigoTCCosto !== 'OK'){
										msj = $translate.instant(respuesta.P_OUT_SALIDA[0].CodigoTCCosto)+' : '+limInf+'  -  '+limSup+'.  ';
										t = bcsAlerta.crear("PREGUNTA_SI_NO",msj + $translate.instant("UI_CONTINUAR"));
										t.result.then(function(result){
											if(result === OPCIONES_BCSALERTA.SI){
												bcsServiciosMesa.IngresoSpot(parametros,successIngreso,errorIngreso);
											}
											else{
												bcsComunes.ventanaOverlay(idVentana, false);
												return;
											}
										});
									}
									else{
										bcsServiciosMesa.IngresoSpot(parametros,successIngreso,errorIngreso);
									}
								}
								else{
									bcsComunes.ventanaOverlay(idVentana, false);
									return;
								}
							});
						}
						else if(respuesta.P_OUT_SALIDA[0].CodigoTCCosto !== 'OK'){
							msj = $translate.instant(respuesta.P_OUT_SALIDA[0].CodigoTCCosto)+' : '+limInf+'  -  '+limSup+'.  ';
							t = bcsAlerta.crear("PREGUNTA_SI_NO",msj + $translate.instant("UI_CONTINUAR"));
							t.result.then(function(result){
								if(result === OPCIONES_BCSALERTA.SI){
									bcsServiciosMesa.IngresoSpot(parametros,successIngreso,errorIngreso);
								}
								else{
									bcsComunes.ventanaOverlay(idVentana, false);
									return;
								}
							});
						}
						else{
							bcsServiciosMesa.IngresoSpot(parametros,successIngreso,errorIngreso);
					    }
					}
					,function(respuesta){
						bcsServiciosMesa.IngresoSpot(parametros,successIngreso,errorIngreso);
					});
				}
			}
			else if(varEstadoOperacion === 'PEN'){
				bcsServiciosMesa.IngresoSpot(parametros,successIngreso,errorIngreso);
			}
		};

		
		$scope.data.modificaSpot = function(varEstadoOperacion){
			var varSolicitudTipoCambio = 'N';
			var MtoSecundario = 0
			
			if(!$scope.data.tipoInstrumentoSeleccionado){
				bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('UI_DEBE_SELECCIONAR_TIPO_INSTRUMENTO'));
				return;
			}
			
			//VALIDA Monedas
			if(($scope.data.MonedaSecundariaIdActual !== 'CLP') && (($scope.data.kngParidadCierre === undefined) || $scope.data.kngParidadCierre === 0 || $scope.data.kngParidadCierre === '')){
				bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('PARIDAD_CIERRE_IGUAL_CERO'));
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				return;
			}
			if(($scope.data.MonedaSecundariaIdActual !== 'CLP') && ($scope.data.tipoInstrumentoSeleccionado.Nemotecnico !== 'ARBINT') && (($scope.data.kngParidadCosto === undefined) || $scope.data.kngParidadCosto === 0 || $scope.data.kngParidadCosto === '')){
				bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('PARIDAD_COSTO_IGUAL_CERO'));
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				return;
			}

			if($scope.data.formaDePagoEntregamosTotal ===0 && !$scope.data.operaCuentaInversion){
				bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('FORMA_DE_PAGO_ENTREGAMOS_INCOMPLETO'));
				return;
			}
			if($scope.data.formaDePagoRecibimosTotal===0 && !$scope.data.operaCuentaInversion){
				bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('FORMA_DE_PAGO_RECIBIMOS_INCOMPLETO'));
				return;
			}
			if(Object.keys($scope.data.matrizFormaPagoEntregamos).length>1){
				var keys = Object.keys($scope.data.matrizFormaPagoEntregamos);
				var fechasCoinciden =
					keys.every(k =>
						new Date($scope.data.matrizFormaPagoEntregamos[k].FechaLiquidacion).getTime() ===
						new Date($scope.data.matrizFormaPagoEntregamos[keys[0]].FechaLiquidacion).getTime()
					);

				if (!fechasCoinciden) {
					bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('FORMA_DE_PAGO_ENTREGAMOS_MULTIPLES_VALUTA'));
					return;
				}
			}
			if(Object.keys($scope.data.formaDePagoRecibimos).length>1){
				var keys = Object.keys($scope.data.matrizFormaPagoRecibimos);
				var fechasCoinciden =
					keys.every(k =>
						new Date($scope.data.matrizFormaPagoRecibimos[k].FechaLiquidacion).getTime() ===
						new Date($scope.data.matrizFormaPagoRecibimos[keys[0]].FechaLiquidacion).getTime()
					);

				if (!fechasCoinciden) {
					bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('FORMA_DE_PAGO_RECIBIMOS_MULTIPLES_VALUTA'));
					return;
				}
			}
			
			MtoSecundario = $scope.data.kngMontoSecundario || 0;
			totalMontoFormaPagoEnt = 0;
			var RegistrosFormaPago = dsMedioDePago.data()|| [];
			$.each(RegistrosFormaPago,function(key,value){
				totalMontoFormaPagoEnt = totalMontoFormaPagoEnt + value.Monto;
			});

			// Por si da decimales Monto*TCCierre
			if ($scope.data.MonedaSecundariaSeleccionado.IdMonedaSecundaria === 'CLP'){
				totalMontoFormaPagoEnt = Math.round(totalMontoFormaPagoEnt);
				MtoSecundario = Math.round($scope.data.kngMontoSecundario||0);
			}			

			totalMontoFormaPagoRec = 0;	
			var RegistrosFormaPago = dsMedioDePagoRec.data()|| [];
			$.each(RegistrosFormaPago,function(key,value){	
				totalMontoFormaPagoRec = totalMontoFormaPagoRec + value.Monto;
			});			
			
    		if (($scope.data.formaDePagoEntregamosTotal !== ($scope.data.kngMontoPrincipal||0)) && ($scope.data.OperacionSeleccionado.IdTipoOperacion === 'VENSPT')){
				bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('FORMA_DE_PAGO_ENTREGAMOS_INCOMPLETO'));
    			return;
    		}
    		if (($scope.data.formaDePagoEntregamosTotal !== ($scope.data.kngMontoSecundario||0)) && ($scope.data.OperacionSeleccionado.IdTipoOperacion === 'COMSPT')){
				bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('FORMA_DE_PAGO_ENTREGAMOS_INCOMPLETO'));
    			return;
    		}
    		if (($scope.data.formaDePagoRecibimosTotal !== ($scope.data.kngMontoSecundario||0)) && ($scope.data.OperacionSeleccionado.IdTipoOperacion === 'VENSPT')){
				bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('FORMA_DE_PAGO_RECIBIMOS_INCOMPLETO'));
    			return;
    		}
    		if (($scope.data.formaDePagoRecibimosTotal !== ($scope.data.kngMontoPrincipal||0)) && ($scope.data.OperacionSeleccionado.IdTipoOperacion === 'COMSPT')){
				bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('FORMA_DE_PAGO_RECIBIMOS_INCOMPLETO'));
    			return;
    		}
			
    		if ((($scope.data.MonedaSecundariaIdActual === 'CLP')&&
	                ($scope.data.tipoInstrumentoSeleccionado.Nemotecnico !== 'DISTRI') &&
	                ($scope.data.tipoInstrumentoSeleccionado.Nemotecnico !== 'ARBDIS')) && 
	                $scope.data.tipoCambioCostoCorrespondiente !== $scope.data.kngTipoCambioCosto){
    			varSolicitudTipoCambio = 'S';
    		}
	 
			//Crear tablas de datos de las grillas				
			var tablaDatosSpot 		= [];		
			var tablaDatosFormaPago	= [];
			
			//valida que valor de margen utilizar
			var varMargen = 0;
			if ($scope.data.utilizaParidadBidAsk === 'S')
				 varMargen = $scope.data.MargenBidAsk; 
			//else if ($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'ARBINT' || $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'ARBDIS' )
				//varMargen = $scope.data.MargenPrincipal;
			else 
				varMargen = $scope.data.kngMargen;
			
			var ObjFormaPago = {};
			var ObjSpot = {
				  "NumeroOperacion"             : $scope.data.idOperacionAModificar
				, "IdInstrumento"               : $scope.data.tipoInstrumentoSeleccionado.IdInstrumento   
				, "IdTipoOperacion"             : $scope.data.OperacionSeleccionado.Codigo // prueba cambiar en SP a NVARCHAR 
				, "IdTipoProducto"              : $scope.data.OperacionSeleccionado.IdTipoProducto
				, "IdCliente"                   : $scope.data.oClientePortafolio.seleccionado.IdCliente
				, "IdPortafolio"                : $scope.data.oClientePortafolio.seleccionado.IdPortafolio
				, "FechaOperacion"              : $scope.SesionFechaSistema      
				, "IdMonedaPrincipal"           : $scope.data.MonedaPrincipalIdActual // 'USD'    prueba
				, "IdMonedaSecundaria"          : $scope.data.MonedaSecundariaIdActual  //'USD' prueba           
				, "MontoMonedaPrincipal"        : $scope.data.kngMontoPrincipal || 0//!!     
				, "MontoMonedaSecundaria"       : $scope.data.kngMontoSecundario || 0  //Monto referencia   
				, "TipoCambioCierre"            : $scope.data.kngTipoCambioCierre || 0     
				, "TipoCambioCierreForward"     : 0     
				, "ParidadCierre"               : $scope.data.kngParidadCierre || 0     
				, "ParidadCierreForward"        : 0      
				, "TipoCambioCosto"             : $scope.data.kngTipoCambioCosto || 0     
				, "ParidadCosto"                : $scope.data.kngParidadCosto || 0     
				, "MontoEquivalente"            : $scope.data.kngMontoEquivalente || 0  //equivale a moneda secundaria
				, "MontoRentabilidad"           : 0     
				, "MontoRentabilidadCanal"      : 0     
				, "Margen"                      : varMargen || 0  
				, "Utilidad"                    : varMargen || 0  
				, "PuntaCompra"                 : $scope.data.kngPuntaCompra ||0     
				, "PuntaVenta"                  : $scope.data.kngPuntaVenta ||0     
				, "SolicitudTipoCambio"         : varSolicitudTipoCambio || 'N'  
				, "IdMesa"                      : $scope.data.oConfiguracionMesaIdActual           
				, "IdModulo"                    : 'SPT'       
				, "IdTipoDolar"                 : 1 //$scope.data.oConfiguracionTipoDolar.idActual           
				, "TipoComprobanteFacturacion"  : $scope.data.oConfiguracionTipoComprobante.idActual 
				, "IdAgente"                    : $scope.data.oClientePortafolio.seleccionado.IdPersonaEjecutivo
				, "BenOur"                      : 'BEN'     
				, "EquivalenteConGasto"         : $scope.data.kngEquivalenteGasto || 0     
				, "TipoCambioCierreConGasto"    : $scope.data.kngTipoCambioCierreGasto || 0     
				, "Observacion"                 : $scope.data.Comentario || ''    
				, "CodigoComercio"              : $scope.data.CodigoComercioSeleccionado.IdPlanillaBancoCentral            
				, "FolioPlanilla"               : 0            
				, "FolioInterfaz"               : ''        
				, "OperacionFicticia"           : varFicticia||'N'  
				, "IdTipoCanalIngreso"          : IdTipoCanalIngreso  || 'MES'
				, "IdDatatec"                   : 0  
				, "Estado"						: varEstadoOperacion
				, "IdSolicitudAporteRescate"	: $scope.data.idSolicitudAporteRescate
				, "IdContacto"					:$scope.data.oConfiguracionContactos.seleccionado.IdPersona || 0 
				, "TipoDocumento"				: $scope.data.oConfiguracionDocumentos.idActual || ""
				//, "PagaConCtaInv"				: $scope.data.operaCuentaInversion?'S':'N'
			};
			tablaDatosSpot.push(ObjSpot);

			//var RegistrosFormaPago = dsMedioDePago.dataColumns(["IdMedioDePago","NombreMedioDePago"],dsMedioDePago.itemsEdited()) || [];
			var i=0;
			$.each($scope.data.formaDePagoEntregamos,function(key,value) {
				var idFormaDePago = $scope.data.matrizFormaPagoEntregamos[key].IdFormaDePago;
				if (value > 0){
					var idComprobantePagoOperacion = 0;
					$.each($scope.data.formasDePago,function(keyF,valueF) {
						if (valueF.RecibePagaPortafolio === 'P' && valueF.IdFormaDePago === key)
							idComprobantePagoOperacion = valueF.IdComprobantePagoOperacion
					});
					tablaDatosFormaPago.push({
										 "IdInternoPagoOperacion" : ++i
										,"IdComprobantePagoOperacion" : idComprobantePagoOperacion
										,"IdTipoMovimiento"          : $scope.data.TipoOperacionIdActual==='COMSPT'?'MOVING':'MOVEGR'
										,"IdFormaDePago":idFormaDePago
										,"IdOperacion":$scope.data.idOperacionAModificar
										,"RecibePagaPortafolio":'P'
										,"IdMoneda":$scope.data.TipoOperacionIdActual==='COMSPT'?$scope.data.MonedaSecundariaIdActual:$scope.data.MonedaPrincipalIdActual
										,"IdMonedaLiquidacion":$scope.data.TipoOperacionIdActual==='COMSPT'?$scope.data.MonedaSecundariaIdActual:$scope.data.MonedaPrincipalIdActual
										,"MontoFormaDePago":value
										,"AfectarTesoreria" :idFormaDePago.includes("_CIN_")?'N':'S'
										,"IdCuentaBancaria" :$scope.data.matrizFormaPagoEntregamos[key].IdCuentaBancaria});
				}
			});
			$.each($scope.data.formaDePagoRecibimos,function(key,value) {
				var idFormaDePago = $scope.data.matrizFormaPagoRecibimos[key].IdFormaDePago;
				if (value > 0){
					var idComprobantePagoOperacion = 0;
					$.each($scope.data.formasDePago,function(keyF,valueF) {
						if (valueF.RecibePagaPortafolio === 'R' && valueF.IdFormaDePago === key)
							idComprobantePagoOperacion = valueF.IdComprobantePagoOperacion
					});
					tablaDatosFormaPago.push({
									 "IdInternoPagoOperacion" : ++i
									,"IdComprobantePagoOperacion" : idComprobantePagoOperacion
									,"IdTipoMovimiento"          : $scope.data.TipoOperacionIdActual==='COMSPT'?'MOVEGR':'MOVING'
									,"IdFormaDePago":idFormaDePago
									,"IdOperacion":$scope.data.idOperacionAModificar
									,"RecibePagaPortafolio":'R'
									,"IdMoneda":$scope.data.TipoOperacionIdActual==='COMSPT'?$scope.data.MonedaPrincipalIdActual:$scope.data.MonedaSecundariaIdActual
									,"IdMonedaLiquidacion":$scope.data.TipoOperacionIdActual==='COMSPT'?$scope.data.MonedaPrincipalIdActual:$scope.data.MonedaSecundariaIdActual
									,"MontoFormaDePago":value
									,"AfectarTesoreria" :idFormaDePago.includes("_CIN_")?'N':'S'
									,"IdCuentaBancaria" :$scope.data.matrizFormaPagoRecibimos[key].IdCuentaBancaria});
				}
			});
			/*
			var RegistrosFormaPago = dsMedioDePago.data()|| [];
			$.each(RegistrosFormaPago,function(key,value){	
				var ObjFormaPago = {
					"NumeroOperacion"			: $scope.data.idOperacionAModificar,
		            "IdTipoProducto"            : 'MEME',
		            "IdTipoMovimiento"          : 'MOVEGR',
					"IdMedioDePago"				: value.IdMedioDePago,
					"NombreMedioDePago"			: value.NombreMedioDePago,
					"IdModalidadPago"			: value.IdModalidadDePago,
					"NombreModalidadDePago"		: '',
					"IdMoneda"					: value.Moneda,
					"Monto"						: value.Monto,
					"FechaIngresoRegistro"		: $scope.SesionFechaSistema,
					"FechaCondicionLiquidacion"	: value.FechaPagoMedio
				};
				tablaDatosFormaPago.push(ObjFormaPago);
			});
			
			var RegistrosFormaPago = dsMedioDePagoRec.data()|| [];
			$.each(RegistrosFormaPago,function(key,value){	
				var ObjFormaPago = {
					"NumeroOperacion"			: $scope.data.idOperacionAModificar,
		            "IdTipoProducto"            : 'MEME',
		            "IdTipoMovimiento"          : 'MOVING',
					"IdMedioDePago"				: value.IdMedioDePago,
					"NombreMedioDePago"			: value.NombreMedioDePago,
					"IdModalidadPago"			: value.IdModalidadDePago,
					"NombreModalidadDePago"		: '',
					"IdMoneda"					: value.Moneda,
					"Monto"						: value.Monto,
					"FechaIngresoRegistro"		: $scope.SesionFechaSistema,
					"FechaCondicionLiquidacion"	: value.FechaPagoMedio
				};
				tablaDatosFormaPago.push(ObjFormaPago);
			});*/
			
			var parametros = {};
			parametros.p_IdEmpresa           = $scope.SesionIdEmpresa;
			parametros.p_IdUsuario           = $scope.SesionIdUsuario;
			parametros.p_IdModulo 			 = 'MSP';
			parametros.p_CalculaMargen		 = 'S' ;
			parametros.p_UsaReglasNegocio    = varEstadoOperacion === 'VIG' ? 'S' : 'N';
			parametros.p_DatosFormaPago      = tablaDatosFormaPago;
			parametros.p_DatosOperacion      = tablaDatosSpot;
			parametros.p_DatosFormaPago      = tablaDatosFormaPago;
			
			bcsComunes.ventanaOverlay(idVentana, true);
	       	
	       	if(varMargen < 0){
				var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_MODIFIAR_OPERACION_CON_PERDIDA"));
				t.result.then(function(result){
					if(result === OPCIONES_BCSALERTA.SI){
						var p_ValidarPC = {
							p_IdEmpresa			: $scope.SesionIdEmpresa,
							p_IdUsuario			: $scope.SesionIdUsuario,
							p_IdModulo			: 'MSP',
							p_FechaOperacion	: $scope.SesionFechaSistema,
							p_Nemotecnico		: $scope.data.tipoInstrumentoSeleccionado.Nemotecnico,
							p_IdMonedaPrincipal	: $scope.data.MonedaPrincipalIdActual,
							p_TipoCambioCierre	: $scope.data.kngTipoCambioCierre || 0,
							p_ParidadCierre		: $scope.data.kngParidadCierre || 0,
							p_TipoCambioCosto	: $scope.data.kngTipoCambioCosto || 0,
							p_ParidadCosto		: $scope.data.kngParidadCosto || 0
						};
						var idMonPri = $scope.data.MonedaPrincipalIdActual;
				   		
				   		bcsServiciosMesa.ValidarPrecioCierre(p_ValidarPC,function(respuesta){
				   			var limInf = util.bcs_formato(respuesta.P_OUT_SALIDA[0].LimiteInferior,'SPT','Monto',idMonPri,'','').valor;
							var limSup = util.bcs_formato(respuesta.P_OUT_SALIDA[0].LimiteSuperior,'SPT','Monto',idMonPri,'','').valor;
							var msj = $translate.instant(respuesta.P_OUT_SALIDA[0].CodigoTCCierre)+' : '+limInf+'  -  '+limSup+'.  ';
							
							if(respuesta.P_OUT_SALIDA[0].CodigoTCCierre !== 'OK'){
								t = bcsAlerta.crear("PREGUNTA_SI_NO",msj + $translate.instant("UI_CONTINUAR"));
								t.result.then(function(result){
									if(result === OPCIONES_BCSALERTA.SI){
										if(respuesta.P_OUT_SALIDA[0].CodigoTCCosto !== 'OK'){
											msj = $translate.instant(respuesta.P_OUT_SALIDA[0].CodigoTCCosto)+' : '+limInf+'  -  '+limSup+'.  ';
											t = bcsAlerta.crear("PREGUNTA_SI_NO",msj + $translate.instant("UI_CONTINUAR"));
											t.result.then(function(result){
												if(result === OPCIONES_BCSALERTA.SI){
													bcsServiciosMesa.ModificarSpot(parametros,successModifica,error);
												}
												else{
													bcsComunes.ventanaOverlay(idVentana, false);
													return;
												}
											});
										}
										else{
											bcsServiciosMesa.ModificarSpot(parametros,successModifica,error);
										}
									}
									else{
										bcsComunes.ventanaOverlay(idVentana, false);
										return;
									}
								});
							}
							else if(respuesta.P_OUT_SALIDA[0].CodigoTCCosto !== 'OK'){
								msj = $translate.instant(respuesta.P_OUT_SALIDA[0].CodigoTCCosto)+' : '+limInf+'  -  '+limSup+'.  ';
								t = bcsAlerta.crear("PREGUNTA_SI_NO",msj + $translate.instant("UI_CONTINUAR"));
								t.result.then(function(result){
									if(result === OPCIONES_BCSALERTA.SI){
										bcsServiciosMesa.ModificarSpot(parametros,successModifica,error);
									}
									else{
										bcsComunes.ventanaOverlay(idVentana, false);
										return;
									}
								});
							}
							else{
								bcsServiciosMesa.ModificarSpot(parametros,successModifica,error);
						    }
						}
						,function(respuesta){
							bcsServiciosMesa.ModificarSpot(parametros,successModifica,error);
						});
					}
					else{
						bcsComunes.ventanaOverlay(idVentana, false);
						return;
					}
				});
			}
			else{
				var p_ValidarPC = {
					p_IdEmpresa			: $scope.SesionIdEmpresa,
					p_IdUsuario			: $scope.SesionIdUsuario,
					p_IdModulo			: 'MSP',
					p_FechaOperacion	: $scope.SesionFechaSistema,
					p_Nemotecnico		: $scope.data.tipoInstrumentoSeleccionado.Nemotecnico,
					p_IdMonedaPrincipal	: $scope.data.MonedaPrincipalIdActual,
					p_TipoCambioCierre	: $scope.data.kngTipoCambioCierre || 0,
					p_ParidadCierre		: $scope.data.kngParidadCierre || 0,
					p_TipoCambioCosto	: $scope.data.kngTipoCambioCosto || 0,
					p_ParidadCosto		: $scope.data.kngParidadCosto || 0
				};
				var idMonPri = $scope.data.MonedaPrincipalIdActual;
				
				bcsServiciosMesa.ValidarPrecioCierre(p_ValidarPC,function(respuesta){
					var limInf = util.bcs_formato(respuesta.P_OUT_SALIDA[0].LimiteInferior,'SPT','Monto',idMonPri,'','').valor;
					var limSup = util.bcs_formato(respuesta.P_OUT_SALIDA[0].LimiteSuperior,'SPT','Monto',idMonPri,'','').valor;
					var msj = $translate.instant(respuesta.P_OUT_SALIDA[0].CodigoTCCierre)+' : '+limInf+'  -  '+limSup+'.  ';
					
					if(respuesta.P_OUT_SALIDA[0].CodigoTCCierre !== 'OK'){
						t = bcsAlerta.crear("PREGUNTA_SI_NO",msj + $translate.instant("UI_CONTINUAR"));
						t.result.then(function(result){
							if(result === OPCIONES_BCSALERTA.SI){
								if(respuesta.P_OUT_SALIDA[0].CodigoTCCosto !== 'OK'){
									msj = $translate.instant(respuesta.P_OUT_SALIDA[0].CodigoTCCosto)+' : '+limInf+'  -  '+limSup+'.  ';
									t = bcsAlerta.crear("PREGUNTA_SI_NO",msj + $translate.instant("UI_CONTINUAR"));
									t.result.then(function(result){
										if(result === OPCIONES_BCSALERTA.SI){
											bcsServiciosMesa.ModificarSpot(parametros,successModifica,error);
										}
										else{
											bcsComunes.ventanaOverlay(idVentana, false);
											return;
										}
									});
								}
								else{
									bcsServiciosMesa.ModificarSpot(parametros,successModifica,error);
								}
							}
							else{
								bcsComunes.ventanaOverlay(idVentana, false);
								return;
							}
						});
					}
					else if(respuesta.P_OUT_SALIDA[0].CodigoTCCosto !== 'OK'){
						msj = $translate.instant(respuesta.P_OUT_SALIDA[0].CodigoTCCosto)+' : '+limInf+'  -  '+limSup+'.  ';
						t = bcsAlerta.crear("PREGUNTA_SI_NO",msj + $translate.instant("UI_CONTINUAR"));
						t.result.then(function(result){
							if(result === OPCIONES_BCSALERTA.SI){
								bcsServiciosMesa.ModificarSpot(parametros,successModifica,error);
							}
							else{
								bcsComunes.ventanaOverlay(idVentana, false);
								return;
							}
						});
					}
					else{
						bcsServiciosMesa.ModificarSpot(parametros,successModifica,error);
				    }
				}
				,function(respuesta){
					bcsServiciosMesa.ModificarSpot(parametros,successModifica,error);
				});
			}
		};
		
		
		$scope.data.anularOperacion = function() {
			var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_ANULARA_LA_OPERACION')+' '+$scope.parametrosEntrada.IdOperacionUI);
			t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.ACEPTAR){
					bcsComunes.ventanaOverlay(idVentana, true);
					
					var tablaDatosSpot = [];		
					var ObjSpot = {
						"NumeroOperacion": $scope.parametrosEntrada.IdOperacionUI
					};
					tablaDatosSpot.push(ObjSpot);
					
					var parametros = {};
					parametros.p_IdEmpresa			= $scope.SesionIdEmpresa;
					parametros.p_IdUsuario			= $scope.SesionIdUsuario;
					parametros.p_IdModulo			= 'MSP';
					parametros.p_UsaReglasNegocio	= 'S';
					parametros.p_DatosOperacion		= tablaDatosSpot;

					bcsComunes.log().info(JSON.stringify(parametros));	
			       	bcsServiciosMesa.AnularSpot(parametros,successAnulacion,error);
				}
				else return;
			});
		};


		//Cargas iniciales:
		cargaConfiguracionUsuario();
		
		var datos = {};
		datos.p_IndFuente = 'BCS'; 
		datos.p_Fecha = $scope.SesionFechaSistema;
		if($scope.parametrosEntrada.Opcion === "CONSULTAR" || $scope.parametrosEntrada.Opcion === "ANULAR"){
			datos.p_Fecha = $scope.parametrosEntrada.FechaOperacion;
		}
		
       	bcsServiciosMesa.ObtenerValores(datos,successReferenciaMercado,error);
       	
	       
       	// se obtienen valores de Paridad y Tipo Cambio
		var fnObtenerValoresParidadTC = function(){
			var parametrosSP = {};
			parametrosSP.p_IdEmpresa= $scope.$root.SesionIdEmpresa;
			parametrosSP.p_IdUsuario= $scope.$root.SesionIdUsuario;
			parametrosSP.p_Fecha	= $scope.SesionFechaSistema;

			bcsServiciosMesa.ConsultarParidadYTipoCambio(parametrosSP,function (result){
				if(result.P_OUT_PARIDADESYTIPOCAMBIO.length > 0 && result.P_OUT_ERRORESCONTADOR === 0){
					//datosParidadTC = result.P_OUT_PARIDADESYTIPOCAMBIO;
					$.each(result.P_OUT_PARIDADESYTIPOCAMBIO,function(key,value){
						var aux = {};
						aux.IdMoneda		= value.IdMoneda;
						aux.ParidadMercado	= value.ParidadMercado;
						aux.ParidadBID		= value.ParidadBID;
						aux.ParidadASK		= value.ParidadASK;
						
						datosParidadTC.push(aux);
					});
				}

				bcsComunes.ventanaOverlay(idVentana,false);
				
	   		},function(respuesta){
	   			bcsComunes.msg("error","error",$translate.instant('UI_REFERENCIA_MERCADO_ERROR'));
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				bcsComunes.ventanaOverlay(idVentana, false);
	   		});	
		};
		
		fnObtenerValoresParidadTC();
		
		var fnBuscoParidadTC = function(IdMoneda){
			var valor = 0;
			for(var i=0; i<datosParidadTC.length; i++){
				if(datosParidadTC[i].IdMoneda === IdMoneda) {
					if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'COMSPT'){
						valor = datosParidadTC[i].ParidadBID || datosParidadTC[i].ParidadMercado || 0;
					}
					else if($scope.data.OperacionSeleccionado.IdTipoOperacion === 'VENSPT'){
						valor = datosParidadTC[i].ParidadASK || datosParidadTC[i].ParidadMercado || 0;
					}
					break;
				}
			}			
			return valor;
		};
       	
       	
		$scope.$watch('data.oConfiguracionReferenciaMercado.seleccionado',function(newval,oldval){   
            if (newval === oldval) return;
            
            if (typeof $scope.data.oConfiguracionReferenciaMercado.seleccionado !== 'undefined'){
        		$scope.data.kngMontoReferenciaMercado = 0;
            	//filtros.push({ field: "IdTipoOperacion", operator: "eq", value: $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoOperacion });
            	$.each (datosIndicadores,function(index,value){
            		if (newval.IdMoneda === value.IdIndicador){
            			$scope.data.kngMontoReferenciaMercado  = Number(value.TipoCambio);
            		}
            	});
            }
		},function(result){
			console.log(result);
		});

		
		$scope.data.irTipoCambioOParidadCierre = function(){
			var nemo = $scope.data.tipoInstrumentoSeleccionado ? $scope.data.tipoInstrumentoSeleccionado.Nemotecnico : undefined;
			if(nemo && (nemo === 'INTERB' || nemo === 'DISTRI')){
				// Diferir el foco para que el browser procese el blur actual antes de intentar el focus.
				// Si el documento no tiene foco (clic fuera de la ventana), el .focus() sería rechazado
				// y FocoTipoCambioCierre quedaría true indefinidamente bloqueando el widget Kendo.
				$timeout(function(){
					if(document.hasFocus()){
						$scope.data.FocoTipoCambioCierre = true;
					}
				}, 0);
			}
			else if(nemo && (nemo === 'ARBINT' || nemo === 'ARBDIS')){
				$timeout(function(){
					if(document.hasFocus()){
						$scope.data.FocoParidadCierre = true;
					}
				}, 0);
			}
		};

		$scope.data.saleTipoCambioOParidadCierre = function(){
			$scope.data.FocoTipoCambioCierre = false;
			$scope.data.FocoParidadCierre = false;
		};
		
		$scope.data.focoIngresoRapido = function(){
			//$scope.data.EjemploIngresoRapido = 'UI_EJEMPLO_INGRESO';
			$scope.data.EjemploIngresoRapido = 'C|V 994/999 13265876-5 1 100000 10 685.25 F';
		};

		$scope.data.noFocoIngresoRapido = function(){
			//$scope.data.EjemploIngresoRapido = 'UI_EJEMPLO_INGRESO';
			$scope.data.EjemploIngresoRapido = '';
		};
		
		$scope.msgWebsocket = function(message) {
			if (message.Evento === 'ACTPTA') {
				actionTipoInstrumentoSeleccionado();
				dsPuntas.read();
			}else if (message.Evento === 'ACTPTC') {
				var parametrosSP = {};
				parametrosSP.p_IdEmpresa= $scope.$root.SesionIdEmpresa;
				parametrosSP.p_IdUsuario= $scope.$root.SesionIdUsuario;
				parametrosSP.p_Fecha	= $scope.SesionFechaSistema;

				datosParidadTC = [];
				bcsServiciosMesa.ConsultarParidadYTipoCambio(parametrosSP,function (result){
					if(result.P_OUT_PARIDADESYTIPOCAMBIO.length > 0 && result.P_OUT_ERRORESCONTADOR === 0){
						$.each(result.P_OUT_PARIDADESYTIPOCAMBIO,function(key,value){
							var aux = {};
							aux.IdMoneda		= value.IdMoneda;
							aux.ParidadMercado	= value.ParidadMercado;
							aux.ParidadBID		= value.ParidadBID;
							aux.ParidadASK		= value.ParidadASK;
							
							datosParidadTC.push(aux);
						});
					}

					bcsComunes.ventanaOverlay(idVentana,false);
					actionTipoInstrumentoSeleccionado();
					$scope.data.tipoInstrumentoTO = $scope.data.tipoInstrumentoSeleccionado.Instrumento;
            				$scope.data.tipoInstrumentoMP = '';
            				$scope.data.kngParidadCosto = fnBuscoParidadTC($scope.data.MonedaPrincipalIdActual);
					
		   		},function(respuesta){
		   			bcsComunes.msg("error","error",$translate.instant('UI_REFERENCIA_MERCADO_ERROR'));
					bcsComunes.setConflictos(util.parseServerResponse(respuesta));
					bcsComunes.ventanaOverlay(idVentana, false);
		   		});		
			}
			else if(message.Evento=='ENVIOPUNTA_' + $scope.IdSolicitudPuntas ){	        	
				bcsComunes.ventanaMsg({mensaje: message.DATA},$scope.IdVentana);					
				dsSolicitudPuntas.definition.filter = [
	                { field: "IdSolicitudPuntas", operator: "eq", value: $scope.IdSolicitudPuntas }
				 ];				
				var solicitudPuntasData = new bcs.DataSource(dsSolicitudPuntas);
				solicitudPuntasData.query().then(function(){
					if (typeof solicitudPuntasData.data()[0] !== 'undefined'){
						if(solicitudPuntasData.data()[0].IdEstadoOperacion == 'APD'){
							$scope.data.kngTipoCambioCosto = solicitudPuntasData.data()[0].TipoCambioCostoAprobado;  
							$scope.data.bloqueoSolicitudPuntasAceptar = false;
						}
						else if(solicitudPuntasData.data()[0].IdEstadoOperacion == 'RZD'){
			        		$scope.data.bloqueoSolicitudPuntas = false;
			        		$scope.data.bloqueoSolicitudPuntasAceptar = false;
						}							
					}
					else
					{
							$scope.data.bloqueoSolicitudPuntas = false;
							$scope.data.bloqueoSolicitudPuntasAceptar = false;
					}
					var winpadre=$("[ventanaid='"+idVentana+"']");
					winpadre.focus();
				});
				

			}
		}
		// Inicializa el tab Forma de Pago si el parametro esta habilitado
		if ($scope.data.EsEditarFormaDePago === true) {
			$scope.data.tabFormasDePagoActive = true;
		}

		$scope.$on('$destroy', function(){
			if($scope.IdSolicitudPuntas > 0)
			{
				var datos = {};	
				
				datos.p_IdEmpresa 				= $scope.SesionIdEmpresa; 
				datos.p_IdUsuarioAutorizador	= $scope.SesionIdUsuario;
				datos.p_IdSolicitudPuntas		= $scope.IdSolicitudPuntas;						
				datos.p_IdEstado 				= 'RZD';
				datos.p_TipoCambioCostoAprobado	= 0;	
				datos.p_ObservacionAutorizador 	= 'Rechazado al cerrar el ingreso spot.';		

				bcsServiciosMesa.AprobarRechazarSolicitudPuntas(datos,function (result){				
						console.log(result);
					}, function(resError){
						console.log(resError);						
		 		});
			}
		});
		
	});		
}());