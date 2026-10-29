(function() {
	angular.module('ModuloMesa').controller('IngresarForwardCtrl',
	function($scope,$translate,$timeout,$resource,bcsServiciosMesa,bcsConsultasMesa,bcsComunes,bcsServicioPlataforma,bcsAlerta,OPCIONES_BCSALERTA)
	{		
		var idVentana = bcsComunes.ventanaWinId($scope);
		$scope.data = {};
		$scope.data.oDesplegar = {};
		
		bcsComunes.ventanaAutoHeight(idVentana);
		var ingFwd = this;
		
		ingFwd.datosAntes = {};
		ingFwd.datosDespues = {};
		
		var datosIndicadores	= [];
		var datosParidadTC		= [];
		var datosPuntasDefault	= [];
		
		var ParametrosForward = [];
		var ReferenciaMercado = [];
		$scope.ObjSeleccionGrilla = {};
		$scope.data.TipoCotizador = "";
		$scope.dataTabAntActivo = false;
		$scope.data.PagaConCtaInv="N";
		
		
		/********************************************************************************************/
		/******************************** INICIO DE VARIABLES GLOBALES ******************************/
		/********************************************************************************************/
		var totalMontoFormaPagoEnt = 0;
		var totalMontoFormaPagoRec = 0;
		var montoPrincipal = 0;
		var montoSecundario = 0;
		var IdOperacion = 0;
		var FolioCartera = 0;
		var modalidadLiquidacionAux = '';
		var calcularFechaEnEjecucion = false;
		var varFechaVencimientoIni = 'S';
		var obtuvoValoresParidadTC = false;
		var varFechaFixingIni = 'S';
		var varAdvAnticipoParcial = '';
		var varOperacionConCalce = false;
		var validaFeriado = 0;
		var verificarDatosPreCargados=0;
		var ingresar = 0;
		
		/*********nueva forma de pago **************/
		$scope.data.IdMonedaMontoDisplay = $scope.SesionIdMonedaSistema;
		$scope.data.formaDePagoEntregamos = {};
		$scope.data.formaDePagoRecibimos = {};
		$scope.data.formaDePagoEntregamosTotal = 0;
		$scope.data.formaDePagoRecibimosTotal = 0;
		$scope.data.valorClienteCargado = false;
		//$scope.data.cargaFormaExistente = false;
		/*********************************************/
		
		$scope.bloqueoIngresoRapido = true;
		$scope.bloqueoAnticipo = false;
		$scope.bloqueoAnticipoEF = false;
		$scope.bloqueoModificar = false;
		$scope.bloqueoAnular = false;
		$scope.bloqueoConsultar = false;
		$scope.data.DeshabilitoFechaPorEntregaFisica = true;
		$scope.data.HabilitadoPorCompensacion = true;
		$scope.data.FocoMonto = false;
		$scope.data.DeshabilitoBancoCentral = true;
		
		//Variables para efectuar los cálculos en el tab Anticipo
		var antTCParCierre = 0;
		var antNemo = '';
		var antTipoOperacion = '';
		var antMultiplicaODivide = '';
		var antPlazoResidual = 0;
		var factorArbitrajeTipoCambioDO = 1;
		
		$scope.parametros = bcsComunes.ventanaParams(idVentana);
		if($scope.parametros.Opcion === "ANTICIPO"){
			$scope.bloqueoAnticipo = true;
			$scope.bloqueoModificar = false;
			$scope.bloqueoAnular = false;	
			$scope.bloqueoConsultar = false;
			$scope.data.UITituloFormulario = "UI_ANTICIPO_FORWARD";
			$scope.dataTabAntActivo = true;
			$scope.data.DeshabilitaFormaPago = true;
		}
		else if ($scope.parametros.Opcion === "MODIFICAR"){
			$scope.bloqueoAnticipo = false;
			$scope.bloqueoModificar = true;
			$scope.bloqueoAnular = false;
			$scope.bloqueoConsultar = false;
			$scope.data.UITituloFormulario = "UI_MODIFICO_FORWARD";
			$scope.dataTabAntActivo = false;
		}
		else if ($scope.parametros.Opcion === "ANULAR"){
			$scope.bloqueoAnticipo = false;
			$scope.bloqueoModificar = false;
			$scope.bloqueoAnular = true;
			$scope.bloqueoConsultar = false;
			$scope.data.UITituloFormulario = "UI_ANULO_FORWARD";
			$scope.dataTabAntActivo = false;
		}
		else if ($scope.parametros.Opcion === "MODIFICARPAGO"){
			$scope.bloqueoAnticipo = false;
			$scope.bloqueoModificar = false;
			$scope.bloqueoAnular = true;
			$scope.bloqueoConsultar = false;
			$scope.data.UITituloFormulario = "UI_MODIFICO_FORWARD";
			$scope.dataTabAntActivo = false;
			$scope.data.datosFormaPagoActive = true;
			$scope.data.datosOperacionalesActive = false;
		}
		else if ($scope.parametros.Opcion === "CONSULTAR"){
			$scope.bloqueoAnticipo = false;
			$scope.bloqueoModificar = false;
			$scope.bloqueoAnular = true;
			$scope.bloqueoConsultar = true;
			$scope.data.UITituloFormulario = "UI_CONSULTO_FORWARD";
			
			$scope.dataTabAntActivo = $scope.parametros.EstaAnticipada ? true : false;
			//$scope.dataTabAntActivo = ($scope.parametros.IdEstado === 'ANT' && $scope.parametros.EstaAnticipada) ? true : false;//En caso que no se quiera el foco en el tab Anticipo
			
		    $scope.data.DeshabilitaFormaPago = true;
			
		}
		else{
			$scope.parametros.Opcion = 'INGRESO';
			$scope.data.UITituloFormulario = "UI_INGRESO_FORWARD"
			$scope.dataTabAntActivo = false;
		}
		
		
		/********************************************************************************************/
		/***************************************   COMBOBOX   ***************************************/
		/********************************************************************************************/
		$scope.data.oClientePortafolio={
			filtros: {
				IdEstado  : {"field": "IdEmpresa","operator": "eq", "value"  :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"  :  'UI_SI'}
				//IdCliente : {"field": "EsCliente","operator": "eq", "value"  :  0},
			},
		};
			
		//Operación
		$scope.data.oConfiguracionOperacion={
			filtros : {
				IdTipoProducto : { "field"   : "IdTipoProducto","operator": "eq", "value" : "MEFW" }
			}
		};
		
		$scope.data.oConfiguracionTipoInstrumento={
				filtros : {
					IdTipoInstrumento 	: { "field"   : "IdTipoInstrumento","operator": "eq", "value" : "FWD" },
					IdEstado 			: { "field"   : "IdEstado","operator": "eq", "value" : "ACT" },
					logic : "and",
			           IdMonedaPrincipal : { "field"   : "IdMonedaPrincipal","operator": "eq", "value" : "" },
			           IdMonedaSecundaria : { "field"   : "IdMonedaSecundaria","operator": "eq", "value" : "" }				
				}
		};
		
		//Moneda Principal
		$scope.data.oConfiguracionMonedaPrincipal={
			filtros : {
				IdTipoInstrumento 	: { "field"   : "IdTipoInstrumento","operator": "eq", "value" : "FWD" },
				IdEstado 			: { "field"   : "IdEstado","operator": "eq", "value" : "ACT" }
			}
		};
		
		//Moneda Secundaria
		$scope.data.oConfiguracionMonedaSecundaria={
			filtros : {
				IdTipoInstrumento 	: { "field"   : "IdTipoInstrumento","operator": "eq", "value" : "FWD" },
				IdEstado 			: { "field"   : "IdEstado","operator": "eq", "value" : "ACT" }
			}
		};
		
		
		//Configuración Liquidación
		$scope.data.oConfiguracionModalidadLiquidacion={
			filtros : {
				IdEstado : { field: "IdEstado", operator: "eq", value: "VIG" },
				//NombreTipoParametro : { "field"   : "NombreTipoParametro","operator": "eq", "value" : "ModalidadLiquidacion" },
				//ValorParametro 		: { "field"   : "ValorParametro" , "operator" : "eq" , "value" : "" }
			}
		};
		
		//Plaza
		$scope.data.oConfiguracionPlaza={
			filtros : {
				IdCiudad : { "field"   : "IdCiudad","operator": "eq", "value" : "" }
			}		
		}
		
		//Fixing
		$scope.data.oConfiguracionFixing={
			filtros : {
				NombreTipoParametro : { "field"   : "NombreTipoParametro","operator": "eq", "value" : "Fixing" }
			}
		};
		
		//Clasificación Cartera
		$scope.data.oConfiguracionClasificacionCartera={
			filtros : {
				NombreTipoParametro : { "field"   : "NombreTipoParametro","operator": "eq", "value" : "ClasificacionCartera" }
			}
		};
		
		//Mesas
		$scope.data.oConfiguracionMesa={
			filtros : {
				IdTipoProducto : { "field"   : "IdTipoProducto","operator": "eq", "value" : "MEFW" }
			}
		};
		
		//Formas de Pagos (Entregamos/Recibimos)
		$scope.data.oConfiguracionFormaPago={
			filtros : {
				NombreTipoParametro : { "field"   : "NombreTipoParametro" , "operator" : "eq" , "value" : "FormaPago" },
				ValorParametro 		: { "field"   : "ValorParametro" , "operator" : "eq" , "value" : "" }
			}		
		};
		
		//Medios de Pagos
		$scope.data.oConfiguracionMedioDePago={
			filtros : {
				IdTipoProducto 		: { "field"   : "IdTipoProducto"	,"operator": "eq", "value" : "MEFW" },
				IdEstado 			: { "field"   : "IdEstado"      	,"operator": "eq", "value" : "ACT" },
				IdInstrumento 		: { "field"   : "IdInstrumento" 	,"operator": "eq", "value" : "" },
				IdMonedaPrincipal 	: { "field"   : "IdMonedaPrincipal" ,"operator": "eq", "value" : "" },
				IdMonedaSecundaria	: { "field"   : "IdMonedaSecundaria","operator": "eq", "value" : "" }
			}		
		}
		
		//contactos
		$scope.data.oConfiguracionContactos={
				filtros : {
					IdCliente : { "field": "IdCliente","operator" : "eq", "value" : "IdCliente"}
				},
				configuracion : {},
				idActual : false,
				idDefecto : false,
				seleccionado : false,
				
			};
		
		//documentos
		$scope.data.oConfiguracionDocumentos = {
				tipo : "combobox-documentos-mesa",
				filtros : {
					Codigo : {"field": "Codigo","operator": "eq", "value"   :  "" },
					IdTipoOperacion : {"field": "IdTipoOperacion","operator": "eq", "value"   :  "" },
					TipoOperacionMesa : {"field": "TipoOperacionMesa","operator": "eq", "value"   :  "FWR" },
				},
				configuracion : {},
				idActual : false,
				idDefecto : false,
				seleccionado : false,
				
		};
		
		$scope.IdMonedaCLP = { "field"   : "IdMoneda","operator": "eq", "value" : "CLP" };
		$scope.IdMonedaUSD = { "field"   : "IdMoneda","operator": "eq", "value" : "USD" };
		
		$scope.data.oConfiguracionMonedaCompensacion={
			filtros : {
				complejo: [{ "field"   : "IdEstado","operator": "eq", "value" : "ACT" }],
				logic : "and",
				filters : [{
					logic : "or", 
					filters : [$scope.IdMonedaCLP,$scope.IdMonedaUSD]
				}]
			}
		};
		
		/********************************************************************************************/
		/*************************************   DATASOURCES   **************************************/
		/********************************************************************************************/
		var monedasPorProductoFeriadoMonPrinc;
		var obtenerMonedasPorProducto=function(){
			bcsServicioPlataforma.Servicios().MonedasPorProductoFeriadoMonPrinc().$promise.then(function(result){
				monedasPorProductoFeriadoMonPrinc=result.d.results;
			},function(result){
				bcsComunes.msg("warning",$translate.instant("UI_ERROR"),$translate.instant('UI_ERROR_AL_OBTENER_MONEDAS_POR_PRODUCTO'));
			});
		}
		obtenerMonedasPorProducto();
		var dsDefFormaDePago = {
			type : "odata",
			definition : {
                serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                pageSize        : 100,
                transport		: {
                	read: {
                		url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=542",
                		dataType:"json"
                	}
                },
                sort: { field: "IdComprobanteFormaDePago", dir: "desc" },
                schema: {
                	model: {
                		fields: {
                			 IdComprobanteFormaDePago	: {type: "number"}
							,FolioOperacion				: {type: "number"}
							,IdTipoProducto				: {type: "number"}
							,IdMedioDePago				: {type: "string"}
							,NombreMedioDePago			: {type: "string"}
							,IdMoneda					: {type: "string"}
							,Monto						: {type: "number", validation : {min : 0}}
							,FechaIngresoRegistro		: {type: "date"}
							,IdModalidadPago			: {type: "string"}
							,IdCartera					: {type: "number"}
                		}
                	}
                }
			}
		};
		
		var dsDefMedioDePago = {
			type : "array",
			definition : {
                serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                pageSize        : 100,
                sort 			: { field: "IdMedioDePago", dir: "asc" },
                schema 			: {
                	model: {
                		fields: {
                			 IdMedioDePago     	: {type: "string"}
							,NombreMedioDePago 	: {type: "string"}
							,IdMoneda		 	: {type: "string"}
							,Monto			   	: {type: "number", validation : {min : 0}}
							,FechaPagoMedio	   	: {type: "date"}
                		}
                	}
                }
			}
		};
		
		var dsDefConsultaContactosFWD = { // TODO ( modificar llamada)
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
		
		// Validación requisito Contrato Condiciones Generales Fwd
		var ConsultaReqOpeFWDCli = new bcs.DataSource({
			type 		: "odata",
			definition 	: {
				serverFiltering	: true,
				transport: {
					read : {
						url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=579",
						dataType : "json",
					}
				},
				schema: {
					model: {
						fields: {
							IdTipoEtapa		: {type: "string"},
							ValorIndicador	: {type: "string"},
							IdCliente		: {type: "number"}
						}
					}
				}
			},
		});
		
		var dsDefConsultaMovimientosFWD = {
			type : "odata",
			definition : {
                serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                pageSize        : 15,
                transport: {
	        		read: {
	        			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=515",
						dataType:"json"
	        		}
                },
                sort: { field: "IdOperacion", dir: "DESC" },
                schema: {
                	model: {
                		fields : {
                			IdOperacion				: {type: "number"},
							FechaOperacion			: {type: "sysdate"},
							FechaLiquidación		: {type: "sysdate"},
							FechaOperacionOPE		: {type: "systime"},
							NombreInstrumento       : {type: "string"},
							Nemotecnico       		: {type: "string"},
							IdTipoOperacion			: {type: "string"},
							TipoOperacion			: {type: "string"},
							RutCliente				: {type: "string"},
							NombreCliente			: {type: "string"},
							SocioNegocio			: {type: "string"},
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
							MontoCalce				: {type: "number", validation : {min : 0}},
							MontoDisponible			: {type: "number"},
							Modalidad				: {type: "string"},
							Plazo					: {type: "number"},
							PlazoResidual			: {type: "number"},
							FechaVencimiento		: {type: "sysdate"},
							FechaEfectiva			: {type: "sysdate"},
							IdAgente				: {type: "number"},
							NombreAgente			: {type: "string"},
							CodigoUsuario			: {type: "string"},
							Estado					: {type: "string"},
							INGMontoMonedaPrincipal	: {type: "number"},
							FechaFixing				: {type: "string"},
							IdMonedaCompensacion		: {type:"string"}
						}
                	}
                }
			}
		};
		
		var dsDefConsultaOperacionesCalce = {
			type : "odata",
			definition : {
                serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                pageSize        : 15,
                transport: {
	        		read: {
	        			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=520",
						dataType:"json"
	        		}
                },
                //sort: { field: "IdOperacion", dir: "DESC" },
                schema: {
                	model: {
                		fields : {
                			IdOperacionOri			: {type: "number"},
							IdOperacion				: {type: "number"},
							FolioCartera			: {type: "number"},
							IdMonedaPrincipal		: {type: "string"},
							IdMonedaSecundaria		: {type: "string"},
							MontoOperacion			: {type: "number"},
							MontoCalce				: {type: "number", validation : {min : 0}},
							MontoDisponible			: {type: "number"},
							PlazoResidual			: {type: "number"},
							IdTipoInstrumento		: {type: "string"},
							IdInstrumento			: {type: "number"},
							IdTipoOperacion			: {type: "string"},
							TipoOperacion			: {type: "string"},
							FechaVencimiento		: {type: "sysdate"},
							Modalidad				: {type: "number"},
							IdCliente				: {type: "number"},
							RutCliente				: {type: "string"},
							NombreCliente			: {type: "string"},
							IdAgente				: {type: "number"},
							NombreAgente			: {type: "string"},
							IdEstado				: {type: "string"}
						}
                	}
                }
			}
		};		
		
		/********************************************************************************************/
		/*************************************     GRILLAS     **************************************/
		/********************************************************************************************/
		
		var dsMedioDePago = new bcs.DataSource(dsDefMedioDePago);
		$scope.confBasicaformapago = {
			dataSource: dsMedioDePago,
			columns: [
			     {field: "IdMedioDePago", title: "UI_ID_MEDIO_PAGO", width: "20px", align: "right", sortable : false, hidden: true}
			    ,{field: "NombreMedioDePago", title: "UI_MEDIO_PAGO", width: "150px", align: "left", sortable : false }
			    ,{field: "IdMoneda", title: "UI_MEDIO_PAGO", width: "150px", align: "left", sortable : false, hidden: true }
			    ,{field: "Monto", title: "UI_MONTO", width: "70px", align: "left", sortable : false,
			    	format: function (value, item) {
			    		return util.bcs_formato(value,'FWD','Monto',item.IdMoneda,'','');
			    	}
			    }
			    ,{field: "FechaPagoMedio", title: "UI_FECHA_PAGO", width: "70px", align: "left", sortable : false } 		
			],
			sortable	: true,
			pageable	: true,
			filterable	: false,
			height		: 120,
			selectable	: "multiple row",
			autoBind	: false,
		};
		
		var dsMedioDePagoRec = new bcs.DataSource(dsDefMedioDePago);
		$scope.confBasicaformapagorecibimos = {
			dataSource: dsMedioDePagoRec,
			columns: [
			    {field: "IdMedioDePago", title: "UI_ID_MEDIO_PAGO", width: "20px", align: "right", sortable : false, hidden: true}
			    ,{field: "NombreMedioDePago", title: "UI_MEDIO_PAGO", width: "150px", align: "left", sortable : false }
			    ,{field: "IdMoneda", title: "UI_MEDIO_PAGO", width: "150px", align: "left", sortable : false, hidden: true }
			    ,{field: "Monto", title: "UI_MONTO", width: "70px", align: "left", sortable : false,
			    	format: function (value, item) {
			    		return util.bcs_formato(value,'FWD','Monto',item.IdMoneda,'','');
			    	}
			    }
			    ,{field: "FechaPagoMedio", title: "UI_FECHA_PAGO", width: "70px", align: "left", sortable : false } 		
			],
			sortable	: true,
			pageable	: true,
			filterable	: false,
			height		: 120,
			selectable	: "multiple row",
			autoBind	: false
		};
		
		var dsCarteraCalce = undefined;
		var columnasCalce = [];
		if ($scope.parametros.Opcion === "INGRESO"){
			dsCarteraCalce = new bcs.DataSource(dsDefConsultaMovimientosFWD);
			columnasCalce = [
			     {field: "IdOperacion",			title: "UI_FOLIO",			width: "60px",	align: "left",	sortable: false }
			    ,{field: "TipoOperacion",		title: "UI_TIPO_OPERACION",	width: "90px",	align: "left",	sortable: false }
			    ,{field: "NombreCliente",		title: "UI_NOMBRE_CLIENTE",	width: "150px",	align: "left",	sortable: false }
			    ,{field: "SocioNegocio",		title: "UI_SOCIO_NEGOCIO",	width: "150px",	align: "left",	sortable: false }
			    ,{field: "IdMonedaPrincipal",	title: "UI_MONEDA",			width: "60px",	align: "center",sortable: false }
			    ,{field: "INGMontoMonedaPrincipal",title: "UI_MONTO_OPERACION",width: "70px",	align: "left",	sortable: false,
			    	format: function (value, item) {
			    		return util.bcs_formato(value,'FWD','Monto',item.IdMonedaPrincipal,'','');
			    	}
			    }
			    ,{field: "MontoDisponible",		title: "UI_MONTO_DISPONIBLE_CALCE", width: "70px", align: "right", sortable: false,
			    	format: function (value, item) {
			    		return util.bcs_formato(value,'FWD','Monto',item.IdMonedaPrincipal,'','');
			    	}
			    }
			    ,{field: "MontoCalce",			title: "UI_MONTO_CALZAR",	width: "70px",	align: "right",	sortable: false, editable: true,
			    	format: "{0:d}", 
			    	template: "<label> #:(util.bcs_formato(MontoCalce,'FWD','Monto',IdMonedaPrincipal,'','')).valor #</label>" }
			];
		}
		else{
			dsCarteraCalce = new bcs.DataSource(dsDefConsultaOperacionesCalce);
			columnasCalce = [
			     {field: "FolioCartera",		title: "UI_FOLIO",			width: "60px",	align: "left",	sortable: false }
			    ,{field: "TipoOperacion",		title: "UI_TIPO_OPERACION",	width: "90px",	align: "left",	sortable: false }
			    ,{field: "NombreCliente",		title: "UI_NOMBRE_CLIENTE",	width: "150px",	align: "left",	sortable: false }
			    ,{field: "IdMonedaPrincipal",	title: "UI_MONEDA",			width: "60px",	align: "center",sortable: false }
			    ,{field: "MontoOperacion",		title: "UI_MONTO_OPERACION",width: "70px",	align: "left",	sortable: false,
			    	format: function (value, item) {
			    		return util.bcs_formato(value,'FWD','Monto',item.IdMonedaPrincipal,'','');
			    	}
			    }
			    ,{field: "MontoDisponible",		title: "UI_MONTO_DISPONIBLE_CALCE", width: "70px", align: "right", sortable: false,
			    	format: function (value, item) {
			    		return util.bcs_formato(value,'FWD','Monto',item.IdMonedaPrincipal,'','');
			    	}
			    }
			];
		}
		
		$scope.confBasicaCalce = {
			dataSource	: dsCarteraCalce,
			columns		: columnasCalce,
			sortable	: true,
			pageable	: true,
			filterable	: false,
			height		: 150,
			selectable	: "row",
			autoBind	: false
		};
		
		
		/********************************************************************************************/
		/*************************************    FUNCIONES    **************************************/
		/********************************************************************************************/
		
		var cargarFechaFixing = function(){
			if($scope.parametros.Opcion !== "MODIFICAR" && $scope.data.FechaFixing != '' && $scope.data.FechaFixing != '0001-01-01'){
				return;
			}
			if($scope.data.kngFechaVencimiento){
				var datos = {
						p_IdEmpresa			: $scope.SesionIdEmpresa,
						p_IdUsuario			: $scope.SesionIdUsuario,
						p_IdModulo			: 'MFW',
						p_UsaReglasNegocio	: 'N',
						p_Fecha				: util.dateSPdate($scope.data.kngFechaVencimiento),
						p_NumeroDias		: 1,
						p_EsHabil			: 'S',
						p_IdPais			: $scope.data.PlazaSeleccionado ? $scope.data.PlazaSeleccionado.IdPais : $scope.SesionIdPaisSistema
					};
				bcsServicioPlataforma.FechaAnterior(datos,function(respuesta) {
					if(respuesta.P_OUT_FECHASIGUIENTE && respuesta.P_OUT_ERRORESCONTADOR === 0){
						$scope.data.kngFechaFixing = util.dateInicioDia(respuesta.P_OUT_FECHASIGUIENTE);
					}
					else{
						console.log("ERROR: " + JSON.stringify(respuesta));
					}
				},function(respuesta) {
					console.log("ERROR: " + JSON.stringify(respuesta));
				});
			}
		};
		
		var fnFormularioInicial = function(){
			verificarDatosPreCargados=0;			
			//Cliente seleccionado
			$scope.data.oClientePortafolio.idDefecto = false;
			$scope.filtros.IdPortafolio = undefined;
			/*Campos necesarios para habilitar o deshabilitar otros*/
			$scope.data.ChkPuntosForward = true;
			$scope.data.DeshabilitoFechaPorEntregaFisica = false;
			$scope.data.HabilitarEliminar = false;
			$scope.HabilitarMontoPago = false;
			varFechaVencimientoIni = 'S';
			varFechaFixingIni = 'S';
			
			/*Valores por Defecto para el par de Monedas*/
			$scope.data.MonedaPrincipalDefecto = $scope.parametros.Opcion==="INGRESO"? 'USD':'';
			$scope.data.MonedaSecundariaDefecto = $scope.parametros.Opcion==="INGRESO"? 'CLP':'';
			
			/*Campos de Textos o números*/
			$scope.data.kngPlazo = Number(fnBuscoValorParametro('PLAZOVENCIMIENTOMINIMO'));
			//$scope.data.kngPlazoAux = Number(fnBuscoValorParametro('PLAZOVENCIMIENTOMINIMO'));
			$scope.data.kngMonto = 0;
			$scope.data.kngMontoInicial = undefined;
			$scope.data.kngMontoFinal = undefined; 
			$scope.data.kngMontoDiferido = undefined; 
			$scope.data.kngMargen = undefined;
			$scope.data.kngMontoCalce = undefined;
			$scope.data.kngMontoCalzado = undefined;
			$scope.data.kngMontoDiferencia = undefined;
			
			if($scope.data.oConfiguracionModalidadLiquidacion.seleccionado !== undefined && $scope.parametros.Opcion === 'INGRESO'){
				$scope.data.ModalidadLiquidacionIdActual = '';
				//$scope.data.oConfiguracionModalidadLiquidacion.filtros.ValorParametro.value = '';			
			}
			
			if($scope.data.tipoInstrumentoSeleccionado){
				if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'ARBITR'){
					$scope.data.TipoCotizador = "UI_PARIDAD_COSTO";
					$scope.data.kngTCCosto = fnBuscoParidadTC($scope.data.MonedaPrincipalIdActual);
				}					
				else if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SECANO'){
					$scope.data.TipoCotizador = "UI_TC_COSTO";
					if($scope.data.MonedaPrincipalIdActual === 'USD'){
						//fnBuscoPuntasDefault($scope.data.MonedaPrincipalIdActual, $scope.parametros.Opcion);
						$scope.data.kngTCCosto = $scope.data.kngMontoDO || fnBuscoReferenciaMercado($scope.data.MonedaPrincipalIdActual) || 0;
					}
					else{
						$scope.data.kngTCCosto = fnBuscoReferenciaMercado($scope.data.MonedaPrincipalIdActual) || 0;
					}
				}
				else if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SEINFL' ||
						$scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SECARE'){
					$scope.data.TipoCotizador = "UI_TC_COSTO";
					$scope.data.kngMontoInicial	= 0;

					var TCPrincipal = fnBuscoTC($scope.data.MonedaPrincipalIdActual);
					var TCSecundaria = fnBuscoTC($scope.data.MonedaSecundariaIdActual);
					if ($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SECARE') {
						if ($scope.data.MonedaPrincipalIdActual === 'USD' && $scope.data.MonedaSecundariaIdActual === 'UF') {
							$scope.data.kngTCCosto = parseFloat(($scope.data.kngMontoDO / $scope.data.kngMontoUf).toFixed($scope.data.DecimalesMonSecInstrumento))
						}else if ($scope.data.MonedaPrincipalIdActual === 'EUR' && $scope.data.MonedaSecundariaIdActual === 'UF'){
							$scope.data.kngTCCosto = parseFloat((TCPrincipal / TCSecundaria).toFixed($scope.data.DecimalesMonSecInstrumento))	
						}
					} else {
						$scope.data.kngTCCosto = fnBuscoReferenciaMercado('UF') || 0;
					}
					
					if(($scope.data.MonedaPrincipalIdActual === 'USD' && $scope.data.MonedaSecundariaIdActual === 'UF') || 
					   ($scope.data.MonedaPrincipalIdActual === 'EUR' && $scope.data.MonedaSecundariaIdActual === 'UF') || 
					   ($scope.data.MonedaPrincipalIdActual === 'UF'  && $scope.data.MonedaSecundariaIdActual === 'CLP')){
						$scope.data.ModalidadLiquidacionIdActual = 'COMPENSADA';
						$scope.data.HabilitadoPorCompensacion = false;
					}
				}
			}
			$scope.data.kngPuntosForward = 0;
			$scope.data.kngTCForward = $scope.data.kngTCCosto;
			$scope.data.kngMontoPago = undefined;
			$scope.data.kngTCCierre = undefined;
			$scope.data.kngParidadCierre = undefined;
			$scope.data.kngMontoMonedaPrincipal = 0;
			$scope.data.kngMontoMonedaSecundaria = 0;
			$scope.data.ngComentario = undefined;
			
			$scope.data.oConfiguracionFormaPago.filtros.ValorParametro.value = '';
			$scope.data.FormaPagoActual = false;
			//$scope.data.oClientePortafolio.idActual = false;
			
			/*DataSource*/
			if(dsMedioDePago	!== undefined) dsMedioDePago.removeAll();
			if(dsMedioDePagoRec	!== undefined) dsMedioDePagoRec.removeAll();
			if(dsCarteraCalce	!== undefined) dsCarteraCalce.removeAll();
			totalMontoFormaPagoEnt = 0;
			totalMontoFormaPagoRec = 0;
			montoPrincipal = 0;
			montoSecundario = 0;
			
			/*Campos de Fechas y horas*/
			$scope.data.kngFechaOperacion = util.dateInicioDia($scope.SesionFechaSistema);
			$scope.data.kngFechaPago = undefined;
			$scope.data.kngFechaPagoFormaPago = undefined;
			$scope.data.kngFechaFixing = undefined;
			$scope.data.kngHoraFixing = undefined;
			$scope.data.HoraFixing = '';

			$scope.data.LimitefechaVencimiento = {};
			$scope.data.LimitefechaVencimiento.min = new Date(util.date($scope.data.kngFechaOperacion).add(1,'days').toDate());
			$scope.data.LimiteFechaPago = {};
			$scope.data.LimiteFechaFixing = {};
			$scope.data.LimiteFechaFormaPago = {};
			
			$scope.data.limpiarFormaPago = true;
			$scope.data.ModalidadLiquidacionIdActual = 'COMPENSADA';
			if($scope.parametros && $scope.parametros.Opcion == 'INGRESO'){
				$scope.data.MonedaCompensacionIdActual = $scope.data.ModalidadLiquidacionIdActual === 'COMPENSADA' ? 'CLP' : '';
				$scope.data.MonedaCompensacionDefecto = $scope.data.ModalidadLiquidacionIdActual === 'COMPENSADA' ? 'CLP' : '';
			}
		};
		
		
		var fnObtenerValores = function(){
			var datos = {};
			datos.p_IndFuente = 'BCS';
			datos.p_Fecha = $scope.SesionFechaSistema;
			if($scope.parametros.Opcion === "CONSULTAR" || $scope.parametros.Opcion === "ANULAR"){
				datos.p_Fecha = $scope.parametros.FechaOperacion;
			}
			
			bcsServiciosMesa.ObtenerValores(datos).$promise.then(function(respuesta){
		   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
								
				//se valido la orden
				if(respuesta.P_OUT_INDICADORES.length > 0){
					$.each(respuesta.P_OUT_INDICADORES,function(key,value){
						var objMontoMonedas = {
							"Moneda" : value.IdIndicador,
							"Monto"  : Number(value.TipoCambio)
						};
						ReferenciaMercado.push(objMontoMonedas);
						
						if(value.IdIndicador === 'UF')
							$scope.data.kngMontoUf = Number(value.TipoCambio);
						
						if(value.IdIndicador === 'DO')
							$scope.data.kngMontoDO = Number(value.TipoCambio);
					});
					datosIndicadores = respuesta.P_OUT_INDICADORES;
				}
				bcsComunes.ventanaOverlay(idVentana,false);
				
				//Anticipo de Operación Forward
				if($scope.parametros.Opcion !== "INGRESO"){
					fnCargoValores();
				}
	   		},function(respuesta){
	   			bcsComunes.msg("error","error",$translate.instant('UI_REFERENCIA_MERCADO_ERROR'));
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				bcsComunes.ventanaOverlay($scope.winID, false);
	   		});	
		};
		fnObtenerValores();
		
		
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
						aux.TipoCambioMercado = value.TipoCambioMercado;
						
						datosParidadTC.push(aux);
					});
					obtuvoValoresParidadTC = true;
				}
				bcsComunes.ventanaOverlay(idVentana,false);
				
	   		},function(respuesta){
	   			bcsComunes.msg("error","error",$translate.instant('UI_REFERENCIA_MERCADO_ERROR'));
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				bcsComunes.ventanaOverlay($scope.winID, false);
	   		});	
		};
		if ($scope.parametros.Opcion !== "CONSULTAR"){
            fnObtenerValoresParidadTC();
			
		}
		
		
		var fnBuscoParidadTC = function(IdMoneda){
			if(IdMoneda === false || IdMoneda === '' || IdMoneda === undefined) return;
			
			var valor = -1;
			for(var i=0; i<datosParidadTC.length; i++){
				if(datosParidadTC[i].IdMoneda === IdMoneda) {
					if($scope.data.OperacionSeleccionada.IdTipoOperacion === 'COMFWD' || ($scope.parametros.Opcion === 'ANTICIPO' && antTipoOperacion === 'COMFWD')){
						if(datosParidadTC[i].ParidadBID === 0 && datosParidadTC[i].ParidadMercado === 0){
							valor = -1;
						}
						else{
							valor = datosParidadTC[i].ParidadBID || datosParidadTC[i].ParidadMercado;
						}
					}
					else if($scope.data.OperacionSeleccionada.IdTipoOperacion === 'VENFWD' || ($scope.parametros.Opcion === 'ANTICIPO' && antTipoOperacion === 'VENFWD')){
						if(datosParidadTC[i].ParidadASK === 0 && datosParidadTC[i].ParidadMercado === 0){
							valor = -1;
						}
						else{
							valor = datosParidadTC[i].ParidadASK || datosParidadTC[i].ParidadMercado;
						}
					}
					break;
				}
			}
			if((valor === undefined || valor === null || valor === -1) || !obtuvoValoresParidadTC){
				if($scope.parametros.Opcion === 'INGRESO' || $scope.parametros.Opcion === 'MODIFICAR'){
					bcsComunes.msg("warning","warning",$translate.instant('UI_SIN_VALOR_MONEDA_PTC')+' '+IdMoneda);
				}
				valor = 0;
			}
			return valor;
		};
		
		var fnBuscoTC = function(IdMoneda){
			if(IdMoneda === false || IdMoneda === '' || IdMoneda === undefined) return;
			
			var valor = -1;
			for(var i=0; i<datosParidadTC.length; i++){
				if(datosParidadTC[i].IdMoneda === IdMoneda) {
					valor = datosParidadTC[i].TipoCambioMercado===0 ? -1 : datosParidadTC[i].TipoCambioMercado;
					break;
				}
			}
			if(valor === undefined || valor === null || valor === -1){
				if($scope.parametros.Opcion === 'INGRESO' || $scope.parametros.Opcion === 'MODIFICAR'){
					bcsComunes.msg("warning","warning",$translate.instant('UI_SIN_VALOR_MONEDA_PTC')+' '+IdMoneda);
				}
				valor = 0;
			}
			return valor;
		};
		
		
		// se obtiene valor de Puntas Default
		var fnBuscoPuntasDefault = function(IdMoneda, opcion){
			if(!$scope.$root.SesionIdEmpresa || !IdMoneda) return;
			
			var valor = 0;
			var parametrosSP = {};
			parametrosSP.IdMoneda	= IdMoneda;
			parametrosSP.IdEmpresa	= $scope.$root.SesionIdEmpresa;
						
			bcsServiciosMesa.ConsultarPuntas(parametrosSP,function (result){
				if(result.P_OUT_PUNTAS.length > 0){
					datosPuntasDefault = result.P_OUT_PUNTAS;
					if($scope.data.OperacionSeleccionada.IdTipoOperacion === 'COMFWD'){
						valor = result.P_OUT_PUNTAS[0].PuntaCompra || 0;
					}
					else if($scope.data.OperacionSeleccionada.IdTipoOperacion === 'VENFWD'){
						valor = result.P_OUT_PUNTAS[0].PuntaVenta || 0;
					}
				}
				bcsComunes.ventanaOverlay(idVentana,false);
				
				if(opcion === 'ANTICIPO'){
					$scope.data.kngTCAnticipo = valor;
				}
				else{
					$scope.data.kngTCCosto = valor;
				}
				
				if(valor === 0 && IdMoneda && IdMoneda !== ''){
					bcsComunes.msg("warning","warning",$translate.instant('UI_SIN_VALOR_MONEDA_PD')+' '+IdMoneda);
				}
				
	   		},function(respuesta){
	   			bcsComunes.msg("error","error",$translate.instant('UI_REFERENCIA_MERCADO_ERROR'));
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				bcsComunes.ventanaOverlay($scope.winID, false);
				
				if(opcion === 'ANTICIPO'){
					$scope.data.kngTCAnticipo = valor;
				}
				else{
					$scope.data.kngTCCosto = valor;
				}
	   		});
		};
		
		
		var fnConsultarConfiguracionMesa = function(){
			var params = {
				p_IdEmpresa 	 	: $scope.SesionIdEmpresa,	
				p_IdTipoProducto 	: 'MEFW',
				p_CodigoParametro 	: ''
			};
			
			bcsServiciosMesa.ConsultarConfiguracionMesa(params).$promise.then(function(respuesta){
				$.each(respuesta.P_OUT_DATOSMESA,function(key,value){
					var ObjParametros = {
			            "CodigoParametro" : value.CodigoParametro,
			            "Valor"           : value.Valor
					};
					ParametrosForward.push(ObjParametros);
				});
				/*Llamo a la función para iniciar controles*/
				fnFormularioInicial();
			});
		};
		
		var fnBuscoValorParametro = function(strParam){
			var valor = undefined;
			for(var i = 0, len = ParametrosForward.length; i < len; i++){
				if(ParametrosForward[i].CodigoParametro === strParam){
					valor = ParametrosForward[i].Valor;
					break;
				}
			}			
			return valor;
		};
		
		var fnBuscoReferenciaMercado = function(strParam){
			var valor = undefined;
			for(var i = 0, len = ReferenciaMercado.length; i < len; i++){
				if(ReferenciaMercado[i].Moneda === strParam){
					valor = ReferenciaMercado[i].Monto || 0;
					break;
				}
			}
			if(valor === 0 && strParam && strParam !== ''){
				bcsComunes.msg("warning","warning",$translate.instant('UI_SIN_VALOR_MONEDA_RM')+' '+strParam);
			}
			return valor;
		};
		
		
		// limpiar campos
		$scope.data.Limpiar = function(){
			fnFormularioInicial();			
		};
		
		/*
		var fnLimiteFechas = function(){
			$scope.data.LimiteFechaPago.max = new Date($scope.data.kngFechaVencimiento);
			$scope.data.LimiteFechaFixing.max = new Date($scope.data.kngFechaVencimiento);
			$scope.data.LimiteFechaFormaPago.max = new Date($scope.data.kngFechaVencimiento);
		};
		*/
		
		fnConsultarConfiguracionMesa();
		
		
		$scope.data.OperacionForward = function(){
			if($scope.parametros.Opcion === "ANULAR") AnulacionForward();
			else if($scope.parametros.Opcion === "MODIFICAR") ModificarForward();
			else if($scope.parametros.Opcion === "MODIFICARPAGO") ModificarForward();
			else if($scope.parametros.Opcion === "ANTICIPO") AnticipoForward();
			else if($scope.parametros.Opcion === "INGRESO") IngresoForward('VIG'); 
		};
		$scope.data.IngresoPendienteForward = function(){ 
			IngresoForward('PEN');
		}
		
		$scope.data.irMonto = function(){
			//$scope.data.FocoMonto = true;
		};
		
		$scope.data.saleMonto = function(){
			//$scope.data.FocoMonto = false;
		};
		
		
		/********************************************************************************************/
		/***************** Consulta, Modificacion o Anticipo de Operación Forward *******************/
		/********************************************************************************************/
		var fnCargoValores = function(){
			var ConsultaTemplate = new bcs.DataSource(dsDefConsultaMovimientosFWD);
			var filterTemplate = [ { field: "IdCartera",operator:"eq", value: $scope.parametros.IdCartera }];
			ConsultaTemplate.query({ filter: filterTemplate }).then(function(){
				if(ConsultaTemplate.view().length > 0){
					/*Datos de la Operación*/
				    

				   $scope.data.PagaConCtaInv = ConsultaTemplate.view()[0].PagaConCtaInv;
                   
                  
					IdOperacion = ConsultaTemplate.view()[0].FolioOperacion;
					FolioCartera = ConsultaTemplate.view()[0].IdOperacion;
			
					modalidadLiquidacionAux = ConsultaTemplate.view()[0].ModalidadLiquidacionValor;					
					bcsComunes.ventanaTitulo(idVentana,$translate.instant($scope.data.UITituloFormulario) + " Folio:" + FolioCartera);
					
					//Variables para efectuar los cálculos en el tab Anticipo
					antTCParCierre = ConsultaTemplate.view()[0].TipoCambioCierre;
					antNemo = ConsultaTemplate.view()[0].Nemotecnico;
					antTipoOperacion = ConsultaTemplate.view()[0].IdTipoOperacion;
					antMultiplicaODivide = ConsultaTemplate.view()[0].MultiplicaODivide;
					antPlazoResidual = ConsultaTemplate.view()[0].PlazoResidual;
					
					$scope.data.oClientePortafolio.seleccionado=ConsultaTemplate.view();
					
					$scope.data.oClientePortafolio.idActual	= ConsultaTemplate.view()[0].IdPortafolio;
					$scope.data.oClientePortafolio.idDefecto= ConsultaTemplate.view()[0].IdPortafolio;
					$scope.data.PortafolioIdActual			= ConsultaTemplate.view()[0].IdPortafolio;
					$scope.filtros.IdPortafolio				= ConsultaTemplate.view()[0].IdPortafolio;//idActual del combobox
					$scope.data.PortafolioIdDefecto			= ConsultaTemplate.view()[0].IdPortafolio;
					$scope.data.MonedaPrincipalIdActual		= ConsultaTemplate.view()[0].IdMonedaPrincipal;
					$scope.data.MonedaPrincipalDefecto		= ConsultaTemplate.view()[0].IdMonedaPrincipal;
					$scope.data.MonedaSecundariaIdActual	= ConsultaTemplate.view()[0].IdMonedaSecundaria;
					$scope.data.MonedaSecundariaDefecto		= ConsultaTemplate.view()[0].IdMonedaSecundaria;
					$scope.data.OperacionIdActual			= ConsultaTemplate.view()[0].IdTipoOperacion;
					$scope.data.ModalidadLiquidacionIdActual= ConsultaTemplate.view()[0].ModalidadLiquidacionValor;
					$scope.data.ModalidadLiquidacionDefecto	= ConsultaTemplate.view()[0].ModalidadLiquidacionValor;
					$scope.data.FixingIdActual				= ConsultaTemplate.view()[0].Fixing;
					$scope.data.FixingDefecto				= ConsultaTemplate.view()[0].Fixing;
					$scope.data.kngFechaOperacion			= ConsultaTemplate.view()[0].FechaOperacion;
					$scope.data.FechaFixing					= ConsultaTemplate.view()[0].FechaFixing;
					
					if(ConsultaTemplate.view()[0].ModalidadLiquidacionValor === 'COMPENSADA'){
						$scope.data.kngFechaFixing = ConsultaTemplate.view()[0].FechaFixing;
						//$scope.data.kngHoraFixing = ConsultaTemplate.view()[0].HoraFixing;
						$scope.data.HoraFixing = ConsultaTemplate.view()[0].HoraFixing;
						$scope.data.MonedaCompensacionIdActual	= ConsultaTemplate.view()[0].IdMonedaCompensacion;
						$scope.data.MonedaCompensacionDefecto	= ConsultaTemplate.view()[0].IdMonedaCompensacion;
					}
					else{
						$scope.data.kngFechaFixing = '';
						$scope.data.kngHoraFixing = '';
						$scope.data.HoraFixing = '';
						$scope.data.MonedaCompensacionIdActual	= false;
						$scope.data.MonedaCompensacionDefecto	= '';
					}
					
					$scope.data.kngMonto		= ConsultaTemplate.view()[0].MontoMonedaPrincipal;
					$scope.data.kngTCCierre		= ConsultaTemplate.view()[0].Nemotecnico !== 'ARBITR' ? ConsultaTemplate.view()[0].TipoCambioCierre : undefined;
					$scope.data.kngParidadCierre= ConsultaTemplate.view()[0].Nemotecnico === 'ARBITR' ? ConsultaTemplate.view()[0].ParidadCierre : undefined;
					$scope.data.kngPlazo		= ConsultaTemplate.view()[0].Plazo;
					$scope.data.kngFechaVencimiento	= ConsultaTemplate.view()[0].FechaVencimiento;
					$scope.data.kngFechaPago	= util.dateInicioDia(ConsultaTemplate.view()[0].FechaPago);
					if(ConsultaTemplate.view()[0].EsPuntos === 'S'){
						$scope.data.ChkPuntosForward = true;
						$scope.data.ChkTasasForward = false;
					}
					else{
						$scope.data.ChkPuntosForward = false;
						$scope.data.ChkTasasForward = true;
					}
					$scope.data.kngPuntosForward= ConsultaTemplate.view()[0].PuntosForward;
					$scope.data.kngTCForward	= ConsultaTemplate.view()[0].TasaForward;
					$scope.data.kngTCCosto		= (ConsultaTemplate.view()[0].Nemotecnico !== 'ARBITR' ? ConsultaTemplate.view()[0].TipoCambioCosto : ConsultaTemplate.view()[0].ParidadCosto) || 0;
					$scope.data.kngMargen		= ConsultaTemplate.view()[0].Margen;
					$scope.data.kngMontoDiferido= ConsultaTemplate.view()[0].MontoDiferido;

					if(ConsultaTemplate.view()[0].Nemotecnico === 'SEINFL' || ConsultaTemplate.view()[0].Nemotecnico === 'SECARE'){
						$scope.data.kngMontoInicial	= $scope.data.kngMonto * $scope.data.kngTCCosto;
						$scope.data.kngMontoFinal	= $scope.data.kngMonto * $scope.data.kngTCCierre;
					}
					else{
						$scope.data.kngMontoInicial	= undefined;
						$scope.data.kngMontoFinal	= undefined;
					}
					
					//FORMAS DE PAGO
					var ConsultaFormaDePago = new bcs.DataSource(dsDefFormaDePago);
					var filterFormaDePago = [{ field: "FolioOperacion",	operator:"eq", value: IdOperacion }
											,{ field: "IdTipoProducto",	operator:"eq", value: "MEFW" }
											,{ field: "IdCartera",		operator:"eq", value: $scope.parametros.IdCartera }];
					ConsultaFormaDePago.query({ filter: filterFormaDePago }).then(function(){
						if(ConsultaFormaDePago.view().length > 0){
							$.each(ConsultaFormaDePago.view(),function(key,value){
								if (value.IdTipoMovimiento === 'MOVEGR'){
				            		dsMedioDePago.add({
				            			 IdMedioDePago		: value.IdMedioDePago
				            			,NombreMedioDePago	: value.NombreMedioDePago
				            			,IdMoneda			: value.IdMoneda
				            			,Monto				: value.Monto
				            			,FechaPagoMedio		: value.FechaCondicionLiquidacion
				            			,IdModalidadDePago	: value.IdModalidadPago
				            		});
				            		totalMontoFormaPagoEnt = totalMontoFormaPagoEnt + value.Monto;
								}
								else{
				            		dsMedioDePagoRec.add({
				            			 IdMedioDePago		: value.IdMedioDePago
				            			,NombreMedioDePago	: value.NombreMedioDePago
				            			,IdMoneda			: value.IdMoneda
				            			,Monto				: value.Monto
				            			,FechaPagoMedio		: value.FechaCondicionLiquidacion
				            			,IdModalidadDePago	: value.IdModalidadPago
				            		});
				            		totalMontoFormaPagoRec = totalMontoFormaPagoRec + value.Monto;
								}
							}); 
						}
					});
					
					//OTROS
					$scope.data.MesaIdActual	= ConsultaTemplate.view()[0].IdMesa;
					$scope.data.kngAsociadaSpot	= !ConsultaTemplate.view()[0].IdSpotAsociado ? '' : ConsultaTemplate.view()[0].IdSpotAsociado;
					$scope.data.ClasificacionCarteraIdActual= ConsultaTemplate.view()[0].ClasificacionCartera;
					$scope.data.ngComentario	= ConsultaTemplate.view()[0].Glosa;
					$scope.data.oConfiguracionContactos.idActual = ConsultaTemplate.view()[0].IdContacto;
					$scope.data.oConfiguracionContactos.idDefecto = ConsultaTemplate.view()[0].IdContacto;
					$scope.data.oConfiguracionDocumentos.idActual = ConsultaTemplate.view()[0].TipoDocumento;
					$scope.data.oConfiguracionDocumentos.idDefecto = ConsultaTemplate.view()[0].TipoDocumento;
					
					//ANTICIPO
					if($scope.parametros.Opcion === 'ANTICIPO' || ($scope.parametros.Opcion === 'CONSULTAR' && $scope.parametros.EstaAnticipada)){
						var tcParCierre = $scope.data.kngTCCierre || 0;
						var varMontoLiquidacion = 0;
						
						if(antNemo === 'ARBITR'){
							tcParCierre = $scope.data.kngParidadCierre || 0;
							antTCParCierre = ConsultaTemplate.view()[0].ParidadCierre;
							factorArbitrajeTipoCambioDO = $scope.data.kngMontoDO;
						}
						else if (antNemo === 'SECARE') {
							factorArbitrajeTipoCambioDO = $scope.data.kngMontoDO;
						}
						
						$scope.data.kngPlazoResidual = ConsultaTemplate.view()[0].PlazoResidual;
						
						if($scope.parametros.Opcion === 'CONSULTAR' && $scope.parametros.EstaAnticipada){
							$scope.data.kngTCObservadoAnt	= ConsultaTemplate.view()[0].TCObservadoTABANT;
							$scope.data.kngMontoPrincipalAnt= ConsultaTemplate.view()[0].MontoMonedaPricipalTABANT;
							$scope.data.kngTasaDescuentoAnt = ConsultaTemplate.view()[0].TasaDescuentoTABANT;
							$scope.data.kngMontoRecibirAnt	= ConsultaTemplate.view()[0].MontoAnticipoTABANT;
							$scope.data.kngTCAnticipo		= ConsultaTemplate.view()[0].TCAnticipoTABANT;
						}
						else{
							$scope.bloqueoAnticipoEF = ConsultaTemplate.view()[0].ModalidadLiquidacionValor === 'ENTREGA_FISICA' ? true : false;
							
							if(antNemo === 'ARBITR'){
								$scope.data.kngTCObservadoAnt = fnBuscoParidadTC(ConsultaTemplate.view()[0].IdMonedaPrincipal) || 0;
							}
							else if(antNemo === 'SECARE'){
								if ($scope.data.MonedaPrincipalIdActual === 'USD' && $scope.data.MonedaSecundariaIdActual === 'UF') {
									$scope.data.kngTCObservadoAnt = $scope.data.kngMontoUf ? ($scope.data.kngMontoDO / $scope.data.kngMontoUf) || 0 : 0;
								}else if ($scope.data.MonedaPrincipalIdActual === 'EUR' && $scope.data.MonedaSecundariaIdActual === 'UF'){
									var TCPrincipal = fnBuscoTC($scope.data.MonedaPrincipalIdActual);
									var TCSecundaria = fnBuscoTC($scope.data.MonedaSecundariaIdActual);
									$scope.data.kngTCObservadoAnt = parseFloat(TCPrincipal / TCSecundaria);
								}
							}
							else if(antNemo === 'SEINFL'){
								$scope.data.kngTCObservadoAnt = fnBuscoReferenciaMercado('UF') || 0;
							}
							else{
								if(ConsultaTemplate.view()[0].IdMonedaPrincipal !== 'USD'){
									$scope.data.kngTCObservadoAnt = fnBuscoReferenciaMercado(ConsultaTemplate.view()[0].IdMonedaPrincipal) || 0;
								}
								else{
									$scope.data.kngTCObservadoAnt = $scope.data.kngMontoDO || 0;
								}
							}
							$scope.data.kngTCObservadoAnt = $scope.data.kngTCObservadoAnt ? $scope.data.kngTCObservadoAnt : 0;
							
							if(ConsultaTemplate.view()[0].ModalidadLiquidacionValor === 'ENTREGA_FISICA'){
								$scope.data.kngTCAnticipo = tcParCierre || 0;
							}
							else{
								/*if(antNemo === 'ARBITR' || antNemo === 'SECARE' || antNemo === 'SEINFL'){
									$scope.data.kngTCAnticipo = $scope.data.kngTCObservadoAnt || 0;
								}
								else{
									$scope.data.kngTCAnticipo = fnBuscoReferenciaMercado(ConsultaTemplate.view()[0].IdMonedaPrincipal) || 0;
								}*/
								$scope.data.kngTCAnticipo = $scope.data.kngTCObservadoAnt || 0;
							}
							$scope.data.kngTCAnticipo = $scope.data.kngTCAnticipo ? $scope.data.kngTCAnticipo : 0;
							
							$scope.data.kngMontoPrincipalAnt= ConsultaTemplate.view()[0].INGMontoMonedaPrincipal;
							$scope.data.kngTasaDescuentoAnt = 0;
						}
						
						if(ConsultaTemplate.view()[0].ModalidadLiquidacionValor === 'COMPENSADA'){
							if(ConsultaTemplate.view()[0].IdTipoOperacion === 'COMFWD'){
								if(antNemo === 'ARBITR' && ConsultaTemplate.view()[0].MultiplicaODivide === 'D'){
									var parte1 = $scope.data.kngTCAnticipo ? ($scope.data.kngMontoPrincipalAnt / $scope.data.kngTCAnticipo) : 0;
									var parte2 = tcParCierre ? ($scope.data.kngMontoPrincipalAnt / tcParCierre) : 0;
									varMontoLiquidacion = (parte1 - parte2) * factorArbitrajeTipoCambioDO;
								}
								else if (antNemo === 'SECARE') {
									varMontoLiquidacion = $scope.data.kngMontoPrincipalAnt * ($scope.data.kngTCAnticipo - tcParCierre) * $scope.data.kngMontoUf;
								}
								else{
									varMontoLiquidacion = $scope.data.kngMontoPrincipalAnt * ($scope.data.kngTCAnticipo - tcParCierre) * factorArbitrajeTipoCambioDO;
								}
							}
							else if(ConsultaTemplate.view()[0].IdTipoOperacion === 'VENFWD'){
								if(antNemo === 'ARBITR' && ConsultaTemplate.view()[0].MultiplicaODivide === 'D'){
									var parte1 = tcParCierre ? ($scope.data.kngMontoPrincipalAnt / tcParCierre) : 0;
									var parte2 = $scope.data.kngTCAnticipo ? ($scope.data.kngMontoPrincipalAnt / $scope.data.kngTCAnticipo) : 0;
									varMontoLiquidacion = (parte1 - parte2) * factorArbitrajeTipoCambioDO;
								}
								else if (antNemo === 'SECARE') {
									varMontoLiquidacion = $scope.data.kngMontoPrincipalAnt * (tcParCierre - $scope.data.kngTCAnticipo) * $scope.data.kngMontoUf;
								}
								else{
									varMontoLiquidacion = $scope.data.kngMontoPrincipalAnt * (tcParCierre - $scope.data.kngTCAnticipo) * factorArbitrajeTipoCambioDO;
								}
							}
						}
						else if(ConsultaTemplate.view()[0].ModalidadLiquidacionValor === 'ENTREGA_FISICA'){
							if(antNemo === 'ARBITR' && ConsultaTemplate.view()[0].MultiplicaODivide === 'D'){
								varMontoLiquidacion	= $scope.data.kngMontoPrincipalAnt / tcParCierre * factorArbitrajeTipoCambioDO;
							}
							else{
								varMontoLiquidacion	= $scope.data.kngMontoPrincipalAnt * tcParCierre * factorArbitrajeTipoCambioDO;
							}
						}
						$scope.data.kngMontoSecundarioAnt = varMontoLiquidacion;
						if($scope.parametros.Opcion === 'ANTICIPO'){
							$scope.data.kngMontoRecibirAnt = varMontoLiquidacion || 0;
						}
						
						if(antNemo === 'ARBITR'){
							if((!$scope.data.kngTCObservadoAnt || $scope.data.kngTCObservadoAnt === '') && (!$scope.data.kngMontoDO || $scope.data.kngMontoDO === '')){
								bcsComunes.msg("warning","Error","Paridad Mercado de la moneda " + ConsultaTemplate.view()[0].IdMonedaPrincipal + " y Tipo Cambio del Dólar Observado son cero en Mantenedor de Paridad y Tipo de Cambio.");
							}
							else if(!$scope.data.kngTCObservadoAnt || $scope.data.kngTCObservadoAnt === ''){
								bcsComunes.msg("warning","Error","Paridad Mercado de la moneda " + ConsultaTemplate.view()[0].IdMonedaPrincipal + " es cero en Mantenedor de Paridad y Tipo de Cambio.");
							}
							else if(!$scope.data.kngMontoDO || $scope.data.kngMontoDO === ''){
								bcsComunes.msg("warning","Error","Tipo de Cambio del Dólar Observado es cero en Mantenedor de Paridad y Tipo de Cambio.");
							}
						}
						else if(antNemo === 'SECARE'){
							if((!$scope.data.kngMontoDO || $scope.data.kngMontoDO === '') && (!$scope.data.kngMontoUf || $scope.data.kngMontoUf === '')){
								bcsComunes.msg("warning","Error","Tipo de Cambio del Dólar Observado y de la UF son cero en Mantenedor de Paridad y Tipo de Cambio.");
							}
							else if(!$scope.data.kngMontoDO || $scope.data.kngMontoDO === ''){
								bcsComunes.msg("warning","Error","Tipo de Cambio del Dólar Observado es cero en Mantenedor de Paridad y Tipo de Cambio.");
							}
							else if(!$scope.data.kngMontoUf || $scope.data.kngMontoUf === ''){
								bcsComunes.msg("warning","Error","Tipo de Cambio de la UF es cero en Mantenedor de Paridad y Tipo de Cambio.");
							}
						}
					}
					// MODIFICAR: parche evita que que Margen y MontoDiferido quede como NaN al ser sobreescrito por los watch
					if($scope.parametros.Opcion === 'MODIFICAR'){
						$scope.data.tipoInstrumentoSeleccionado.Nemotecnico = '';
					}
					
					//CALCE
					$scope.IdEstado				= { field: "IdEstado"			, operator: "eq", value: 'VIG'};
					$scope.IdOperacionOriA		= { field: "IdOperacionOri"		, operator: "eq", value: ConsultaTemplate.view()[0].FolioOperacion};
					$scope.PlazoResidual		= { field: "PlazoResidual"		, operator: "eq", value: ConsultaTemplate.view()[0].PlazoResidual};
					$scope.Modalidad			= { field: "Modalidad"			, operator: "eq", value: ConsultaTemplate.view()[0].Modalidad};
					$scope.IdMonedaPrincipal	= { field: "IdMonedaPrincipal"	, operator: "eq", value: ConsultaTemplate.view()[0].IdMonedaPrincipal};
					$scope.IdMonedaSecundaria	= { field: "IdMonedaSecundaria"	, operator: "eq", value: ConsultaTemplate.view()[0].IdMonedaSecundaria};
					$scope.IdTipoOperacion		= { field: "IdTipoOperacion"	, operator: "eq", value: ConsultaTemplate.view()[0].IdTipoOperacion === 'COMFWD' ? 'VENFWD' : 'COMFWD'};
					var filtrosCalce = {}
					if ($scope.parametros.Opcion === "CONSULTAR" || $scope.parametros.Opcion === "ANTICIPO"){
						filtrosCalce = {
						logic: "and",
						filters : [ 
						     //{logic : "or", filters : [$scope.IdOperacionOriA,$scope.IdOperacionOriB]}
						     $scope.IdOperacionOriA
							]
						};
					}else{
						filtrosCalce = {
						logic: "and",
						filters : [ 
						     //{logic : "or", filters : [$scope.IdOperacionOriA,$scope.IdOperacionOriB]}
						     $scope.IdOperacionOriA
						    ,$scope.IdEstado
					        ,$scope.PlazoResidual
					        ,$scope.IdTipoOperacion
					        ,$scope.Modalidad
					        ,$scope.IdMonedaPrincipal
					        ,$scope.IdMonedaSecundaria
						]
					};
					}
								
					//dsCarteraCalce.filter(filtrosCalce);
					dsCarteraCalce.query({ filter: filtrosCalce }).then(function(){
						if(dsCarteraCalce.view().length > 0){
							varOperacionConCalce = true;
						}
					});
				}
			});
		};
				
		var dsCargoMedioDePago = function(){
			if($scope.data.tipoInstrumentoSeleccionado === undefined){
				bcsComunes.msg("error","error",$translate.instant('UI_INSTRUMENTO_SELECCIONADO_ERROR'));
				return;
			}
			if($scope.data.oConfiguracionModalidadLiquidacion.seleccionado.ValorParametro !== 'COMPENSADA' && 
			   ($scope.data.kngMontoPago === undefined || $scope.data.kngMontoPago <= 0)){
				bcsComunes.msg("error","error",$translate.instant('UI_MONTO_FORMA_PAGO_ERROR'));
				return;
			}
			if($scope.data.kngFechaPagoFormaPago === undefined || $scope.data.kngFechaPagoFormaPago === ''){
				bcsComunes.msg("error","error",$translate.instant('UI_FECHA_FORMA_PAGO_ERROR'));
				return;
			}
			if(Date.parse($scope.data.kngFechaPagoFormaPago) < Date.parse($scope.data.kngFechaVencimiento)){
				bcsComunes.msg("error","error","La Fecha de Pago no debe ser menor a la Fecha de Vencimiento.");
				return;
			}
			if($scope.data.oConfiguracionMedioDePago.seleccionado.IdMedioDePago === undefined || $scope.data.oConfiguracionMedioDePago.seleccionado.IdMedioDePago === ''){
				bcsComunes.msg("error","error",$translate.instant('UI_MEDIO_PAGO_ERROR'));
				return;
			}
			
			var IdMoneda = '';
		
        	if($scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'ENTREGAMOS'){
        		if ((totalMontoFormaPagoEnt + $scope.data.kngMontoPago) > (montoSecundario)
        				&& ($scope.data.OperacionSeleccionada.IdTipoOperacion === 'COMFWD')){
					bcsComunes.msg("warning","warning","No puede ser mayor que monto: "+ ($scope.data.kngMontoMonedaSecundaria));
        			return
        		}
        		if ((totalMontoFormaPagoEnt + $scope.data.kngMontoPago) > (montoPrincipal) 
        				&& ($scope.data.OperacionSeleccionada.IdTipoOperacion === 'VENFWD')){
					bcsComunes.msg("warning","warning","No puede ser mayor que monto: "+ ($scope.data.kngMontoMonedaPrincipal));
        			return
        		}
        		
        		//$scope.data.FechaMedioPagoDisabled = true;
        		
        		if (dsMedioDePago.view().length > 0){
        			if (dsMedioDePago.view()[0].FechaPagoMedio !== $scope.data.kngFechaPagoFormaPago){
						bcsComunes.msg("warning","warning","La fecha de Pago no debe ser distinta a la ya ingresada.");
	        			return
	        		}
        		}	
        		
        		if ($scope.data.OperacionSeleccionada.IdTipoOperacion === 'COMFWD') 
        			IdMoneda = $scope.data.oConfiguracionMonedaSecundaria.seleccionado.IdMonedaSecundaria;
        		if ($scope.data.OperacionSeleccionada.IdTipoOperacion === 'VENFWD')
        			IdMoneda = $scope.data.oConfiguracionMonedaPrincipal.seleccionado.IdMonedaPrincipal;
        		
        		dsMedioDePago.add({
        			 IdMedioDePago		: $scope.data.oConfiguracionMedioDePago.seleccionado.IdMedioDePago
        			,NombreMedioDePago	: $scope.data.oConfiguracionMedioDePago.seleccionado.NombreMedioDePago
        			,IdMoneda			: IdMoneda
        			,Monto				: $scope.data.kngMontoPago
        			,FechaPagoMedio		: $scope.data.kngFechaPagoFormaPago
        			,IdModalidadDePago	: $scope.data.oConfiguracionMedioDePago.seleccionado.IdModalidadDePago
        		});
        		totalMontoFormaPagoEnt = totalMontoFormaPagoEnt + $scope.data.kngMontoPago;
        		
        		//resta el valor agregado a kngMontoPago
        		if ($scope.data.OperacionSeleccionada.IdTipoOperacion === 'COMFWD') 
        			$scope.data.kngMontoMonedaSecundaria = ($scope.data.kngMontoMonedaSecundaria||0) - $scope.data.kngMontoPago;
        		if ($scope.data.OperacionSeleccionada.IdTipoOperacion === 'VENFWD') 
        			$scope.data.kngMontoMonedaPrincipal = ($scope.data.kngMontoMonedaPrincipal||0) - $scope.data.kngMontoPago;
        	}
        	else if($scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'RECIBIMOS'){       		
        		if ((totalMontoFormaPagoRec + $scope.data.kngMontoPago) > (montoSecundario)
        				&& ($scope.data.OperacionSeleccionada.IdTipoOperacion === 'VENFWD')){
					bcsComunes.msg("warning","warning",$translate.instant("MONTO_FORMA_PAGO_MAYOR_A_MOVIMIENTO"));
        			return
        		}
        		if ((totalMontoFormaPagoRec + $scope.data.kngMontoPago) > (montoPrincipal)
        				&& ($scope.data.OperacionSeleccionada.IdTipoOperacion === 'COMFWD')){
					bcsComunes.msg("warning","warning",$translate.instant("MONTO_FORMA_PAGO_MAYOR_A_MOVIMIENTO"));
        			return
        		}
        		
        		if ($scope.data.OperacionSeleccionada.IdTipoOperacion === 'COMFWD') 
        			IdMoneda = $scope.data.oConfiguracionMonedaPrincipal.seleccionado.IdMonedaPrincipal;
        		if ($scope.data.OperacionSeleccionada.IdTipoOperacion === 'VENFWD')
        			IdMoneda = $scope.data.oConfiguracionMonedaSecundaria.seleccionado.IdMonedaSecundaria;
        		
        		dsMedioDePagoRec.add({
        			 IdMedioDePago		: $scope.data.oConfiguracionMedioDePago.seleccionado.IdMedioDePago
        			,NombreMedioDePago	: $scope.data.oConfiguracionMedioDePago.seleccionado.NombreMedioDePago
        			,IdMoneda			: IdMoneda
        			,Monto				: $scope.data.kngMontoPago
        			,FechaPagoMedio		: $scope.data.kngFechaPagoFormaPago
        			,IdModalidadDePago	: $scope.data.oConfiguracionMedioDePago.seleccionado.IdModalidadDePago
        		});
        		totalMontoFormaPagoRec = totalMontoFormaPagoRec + $scope.data.kngMontoPago;
        		
        		//resta el valor agregado a kngMontoPago
        		if ($scope.data.OperacionSeleccionada.IdTipoOperacion === 'COMFWD') 
        			$scope.data.kngMontoMonedaPrincipal = ($scope.data.kngMontoMonedaPrincipal||0) - $scope.data.kngMontoPago;
        		if ($scope.data.OperacionSeleccionada.IdTipoOperacion === 'VENFWD') 
        			$scope.data.kngMontoMonedaSecundaria = ($scope.data.kngMontoMonedaSecundaria||0) - $scope.data.kngMontoPago;
        	}
            $scope.data.kngMontoPago = 0;
		};
		
		
		$scope.data.IngresoMedioDePago = function(){
			dsCargoMedioDePago();
		};
		
		var verificaFeriadosUSA = function(monedasPorProductoFeriadoMonPrinc){//verifica si aplica feriado moneda principal
			var resultado=false;
			for(m=0;m < monedasPorProductoFeriadoMonPrinc.length;m++){
				//validación operaciones forward
				if ((monedasPorProductoFeriadoMonPrinc[m].IdInstrumento==$scope.data.tipoInstrumentoSeleccionado.IdInstrumento)
					&& monedasPorProductoFeriadoMonPrinc[m].IdMonedaPrincipal=='USD'
					&& $scope.data.MonedaPrincipalIdActual=='USD'
					&& monedasPorProductoFeriadoMonPrinc[m].IdMonedaSecundaria=='CLP'
					&& $scope.data.MonedaSecundariaIdActual=='CLP'
					&& monedasPorProductoFeriadoMonPrinc[m].IdEstado=='ACT'
					&& monedasPorProductoFeriadoMonPrinc[m].FeriadoMonedaPrincipal=='SI'
					&& ($scope.data.oConfiguracionModalidadLiquidacion.seleccionado.ValorParametro=='ENTREGA_FISICA')
					){
						resultado=true;//se verifica checkbox "encendido"
				}
			}
			return resultado;
		}
		var calculaFechaVencimiento = function(monedasPorProductoFeriadoMonPrinc){
			var aplicaFeriadoMonedaPrincipal=(typeof verificaFeriadosUSA !== 'undefined' && typeof monedasPorProductoFeriadoMonPrinc !== 'undefined') ? verificaFeriadosUSA(monedasPorProductoFeriadoMonPrinc): false;
			var paises = aplicaFeriadoMonedaPrincipal?[$scope.SesionIdPaisSistema,223]:[$scope.SesionIdPaisSistema];
			var datoDia = {
				p_diaHabil          : 'N',
				p_soloCaeDiaHabil   : 'S',
				p_fechas            : [{	
										fechaInicial:util.dateSPdate($scope.data.kngFechaOperacion),
										numeroDias: Math.round($scope.data.kngPlazo),
										paises
										}]
			};
			bcsServicioPlataforma.Servicios().ObtenerDiaSiguienteMultiplesPaises(datoDia).$promise.then(function(respuesta){//calculo dia habil EEUU
				
				if(respuesta.P_OUT_ERRORESCONTADOR === 0){
					var pOutFecha = respuesta.p_out_fechas[0].fechaCalculada;
					
					if(respuesta.p_out_fechas[0].numeroDiasInicial != respuesta.p_out_fechas[0].numeroDias && validaFeriado==1){
						validaFeriado = 0;
						bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_MENSAJE_PLAZO_DIA_NO_HABIL_O_FERIADO"), conflictos:util.parseServerResponse(respuesta)},idVentana);
						
					}
								
					$scope.data.LimiteFechaPago		= {};
					$scope.data.LimiteFechaFixing	= {};
					$scope.data.LimiteFechaFormaPago= {};

					$scope.data.kngFechaVencimiento		= util.dateInicioDia(pOutFecha);
					$scope.data.kngFechaVencimientoAux	= util.dateInicioDia(pOutFecha);
					$scope.data.kngFechaPago			= util.dateInicioDia(pOutFecha);
					$scope.data.kngPlazoAux				= respuesta.p_out_fechas[0].numeroDias;
					$scope.data.kngPlazo				= respuesta.p_out_fechas[0].numeroDias;
					$scope.data.kngFechaPagoFormaPago	= util.dateInicioDia(pOutFecha);
			
					$scope.data.LimiteFechaPago.min		= new Date(util.dateInicioDia(pOutFecha));
					$scope.data.LimiteFechaFormaPago.max= new Date(util.dateInicioDia(pOutFecha));
			
					//TODO setear el mes y año del campo Fecha Fixing
					if(varFechaVencimientoIni === 'S'){
						$scope.data.kngFechaVencimientoIni = util.dateInicioDia(pOutFecha);
					}
					bcsComunes.ventanaAutoOverlay(idVentana,false,1000);
					$scope.data.LimiteFechaFixing.max = new Date(util.dateInicioDia(pOutFecha));
					if(varFechaVencimientoIni === 'N'){
						if(varFechaFixingIni === 'S' ||
							(varFechaFixingIni === 'N' && util.dateSPdate($scope.data.kngFechaVencimientoIni) !== util.dateSPdate($scope.data.kendoWidgetFixing.dateView._current))){
							$scope.data.kendoWidgetFixing.dateView._current = util.dateInicioDia(pOutFecha);
							varFechaFixingIni = 'N';
						}
						else{
							$scope.data.kngFechaFixing = util.dateInicioDia(pOutFecha);
							$timeout(function(){
								$scope.data.kngFechaFixing = null;
							}, 1);
						}
					}
			
					//se asigna a Fecha Fixing el dia habil anterior a la Fecha Vencimiento cuando es SECANO y MX/CLP y COMPENSADA
					if($scope.data.oConfiguracionModalidadLiquidacion.seleccionado.ValorParametro === 'COMPENSADA' && $scope.data.FixingIdActual === 'BANCOCENTRAL'){
						cargarFechaFixing();
					}
					varFechaVencimientoIni = 'N';
				}else{
					$scope.data.kngFechaVencimiento = util.dateInicioDia('1900-01-01');
						$scope.data.LimiteFechaPago.min		= new Date(util.dateInicioDia(pOutFecha));
						$scope.data.LimiteFechaFixing.max	= new Date(util.dateInicioDia(pOutFecha));
						$scope.data.LimiteFechaFormaPago.max= new Date(util.dateInicioDia(pOutFecha));
						console.log("ERROR: " + JSON.stringify(respuesta));
				}
				
				},function(result){
					bcsComunes.msg("warning",$translate.instant("UI_ERROR"),$translate.instant('UI_ERROR_AL_OBTENER_FECHA_SIGUIENTE_HABIL'));
			});
			
		}


		/********************************************************************************************/
		/************************************* FUNCIONES WATCH **************************************/
		/********************************************************************************************/
		

		$scope.$watch('data.oClientePortafolio.seleccionado',function(newval,oldval) {   
			if (newval === oldval) return;

            if (($scope.data.oClientePortafolio.seleccionado||'undefined') === 'undefined') return;   
            
            //activador para cuando cargan los datos del cliente y ver las formas de pago 
			if($scope.data.oClientePortafolio.seleccionado!== false && $scope.data.oClientePortafolio.seleccionado !== undefined){
				$scope.data.valorClienteCargado = true;
			}
         });
		
		
		$scope.$watch('data.oClientePortafolio.seleccionado.IdCliente',function(newval,oldval) {
			if($scope.parametros.Opcion === 'CONSULTAR' || $scope.parametros.Opcion === 'ANULAR' || $scope.parametros.Opcion === 'ANTICIPO') return;
			
			if(newval && newval !== oldval){
				if($scope.parametros.Opcion === 'INGRESO' || $scope.parametros.Opcion === 'MODIFICAR'){
					var filtros = [ { field: "IdCliente",	operator: "eq", value: $scope.data.oClientePortafolio.seleccionado.IdCliente },
					                { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa } ];
					ConsultaReqOpeFWDCli.query({ filter: filtros }).then(function(){
						if(ConsultaReqOpeFWDCli.view().length > 0 && ConsultaReqOpeFWDCli.view()[0].ValorIndicador === 'N'){
							ConsultaReqOpeFWDCli.view()[0].P_OUT_ERRORESCONTADOR = 1;
							bcsComunes.ventanaMsg({
								mensaje:$translate.instant("UI_MENSAJE_NO_FIRMADO_CCG_FWD"),
								conflictos:util.parseServerResponse(ConsultaReqOpeFWDCli.view()[0])
							},idVentana);
							//$scope.data.oClientePortafolio.idActual = false;
						}
					});
				}
				
				var parametros = {};
    			parametros.p_IdEmpresa	= $scope.SesionIdEmpresa;
    			parametros.p_Clientes	= [{
					 "IdCliente": $scope.data.oClientePortafolio.seleccionado.IdCliente 
 					,"IdAgente"	: $scope.data.oClientePortafolio.seleccionado.IdPersonaEjecutivo
 				}];
            	bcsServiciosMesa.ConsultarConfiguracionCliente(parametros, function(respuesta){
            		$.each(respuesta.P_OUT_CONFIGURACION,function(key,value){
            			if (value.Codigo === "AGENTE" && value.Valor !== null) {
            				$scope.data.Agente = value.Valor;
            				return;
            			}
            		});
            	},function(respuesta){
    				bcsComunes.ventanaOverlay(idVentana,false);
    				bcsComunes.ventanaMsg({
        				//mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"),
        				conflictos:util.parseServerResponse(respuesta)
        			},idVentana);
    	   		});
			}
		});
		
		
		$scope.$watch('data.tipoInstrumentoSeleccionado',function(newval,oldval){//TODO
			if(JSON.stringify(newval) === JSON.stringify(oldval) || newval === undefined || newval.Nemotecnico === undefined){
				return;
			}
			actionTipoInstrumentoSeleccionado();
		});
		
		var actionTipoInstrumentoSeleccionado = function(){
			if($scope.parametros.Opcion === 'INGRESO' || $scope.parametros.Opcion === 'MODIFICAR'){
				/*DataSource*/
				if(dsMedioDePago	!== undefined) dsMedioDePago.removeAll();
				if(dsMedioDePagoRec	!== undefined) dsMedioDePagoRec.removeAll();
				if(dsCarteraCalce	!== undefined) dsCarteraCalce.removeAll();
				totalMontoFormaPagoEnt = 0;
				totalMontoFormaPagoRec = 0;
			}
			
			if(($scope.parametros.Opcion === 'INGRESO') && ($scope.data.NemotecnicoAux === 'SEINFL' || $scope.data.NemotecnicoAux === 'SECARE')){
				$scope.data.ModalidadLiquidacionIdActual = '';
			}
			$scope.data.NemotecnicoAux = $scope.data.tipoInstrumentoSeleccionado.Nemotecnico;
			
			if($scope.parametros.Opcion !== 'INGRESO'){
				if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'ARBITR'){
					$scope.data.TipoCotizador = "UI_PARIDAD_COSTO";
				}
				else if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SECANO'){
					$scope.data.TipoCotizador = "UI_TC_COSTO";
				}
				else if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SEINFL' ||
						$scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SECARE'){
					$scope.data.TipoCotizador = "UI_TC_COSTO";
					$scope.data.HabilitadoPorCompensacion = false;
				}	
				return;
			}
			
			$scope.data.kngPuntosForward = 0;
			$scope.data.DeshabilitoBancoCentral = false;
			$scope.data.HabilitadoPorCompensacion = true;
			
			if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'ARBITR'){
				$scope.data.kngTCCierre = undefined;
				$scope.data.kngParidadCierre = 0;
				$scope.data.kngMontoInicial = undefined;
				$scope.data.kngMontoFinal = undefined;
				$scope.data.kngTCCosto = fnBuscoParidadTC($scope.data.MonedaPrincipalIdActual);
				$scope.data.TipoCotizador = "UI_PARIDAD_COSTO";
				cargarFechaFixing();
				$scope.data.ModalidadLiquidacionIdActual = 'COMPENSADA';
			}
			else if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SECANO'){
				$scope.data.kngTCCierre = 0;
				$scope.data.kngParidadCierre = undefined;
				$scope.data.kngMontoInicial = undefined;
				$scope.data.kngMontoFinal = undefined;
				
				if($scope.data.MonedaPrincipalIdActual === 'USD'){
					//fnBuscoPuntasDefault($scope.data.MonedaPrincipalIdActual, $scope.parametros.Opcion);
					$scope.data.kngTCCosto = $scope.data.kngMontoDO || fnBuscoReferenciaMercado($scope.data.MonedaPrincipalIdActual) || 0;
				}
				else{
					$scope.data.kngTCCosto = fnBuscoReferenciaMercado($scope.data.MonedaPrincipalIdActual) || 0;
				}
				
				$scope.data.TipoCotizador = "UI_TC_COSTO";
				$scope.data.FixingIdActual = 'BANCOCENTRAL';
				$scope.data.ModalidadLiquidacionIdActual = 'COMPENSADA';
				$scope.data.kngFechaFixing = undefined;
				$scope.data.kngHoraFixing = undefined;
				$scope.data.HoraFixing = '';
				if($scope.data.FixingIdActual !== 'BANCOCENTRAL' && $scope.data.MonedaPrincipalIdActual === 'USD' && $scope.data.MonedaSecundariaIdActual === 'CLP'){
					$scope.data.DeshabilitoBancoCentral = true;
				}
				else if($scope.data.oConfiguracionModalidadLiquidacion.seleccionado && $scope.data.oConfiguracionModalidadLiquidacion.seleccionado.ValorParametro === 'COMPENSADA' 
						&& $scope.data.FixingIdActual === 'BANCOCENTRAL'){
					cargarFechaFixing();
					$scope.data.DeshabilitoFechaPorEntregaFisica = false;
				}
			}
			else if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SEINFL' || 
					$scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SECARE'){
				$scope.data.kngTCCierre		= 0;
				$scope.data.kngParidadCierre= undefined;
				$scope.data.TipoCotizador	= "UI_TC_COSTO";
				$scope.data.kngMontoInicial	= 0;
				
				var TCPrincipal = fnBuscoTC($scope.data.MonedaPrincipalIdActual);
				var TCSecundaria = fnBuscoTC($scope.data.MonedaSecundariaIdActual);
				if ($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SECARE') {
					if ($scope.data.MonedaPrincipalIdActual === 'USD' && $scope.data.MonedaSecundariaIdActual === 'UF') {
						$scope.data.kngTCCosto = parseFloat(($scope.data.kngMontoDO / $scope.data.kngMontoUf).toFixed($scope.data.DecimalesMonSecInstrumento))
					}else if ($scope.data.MonedaPrincipalIdActual === 'EUR' && $scope.data.MonedaSecundariaIdActual === 'UF'){
						$scope.data.kngTCCosto = parseFloat((TCPrincipal / TCSecundaria).toFixed($scope.data.DecimalesMonSecInstrumento))	
					}
				} else {
					$scope.data.kngTCCosto = fnBuscoReferenciaMercado('UF') || 0;
				}
				
				if($scope.data.kngTCCosto && $scope.data.kngMonto){
					$scope.data.kngMontoInicial = $scope.data.kngMonto * $scope.data.kngTCCosto;
				}
				$scope.data.kngMontoFinal	= 0;
				
				if(($scope.data.MonedaPrincipalIdActual === 'USD' && $scope.data.MonedaSecundariaIdActual === 'UF') ||
					($scope.data.MonedaPrincipalIdActual === 'EUR' && $scope.data.MonedaSecundariaIdActual === 'UF') || 
				   ($scope.data.MonedaPrincipalIdActual === 'UF'  && $scope.data.MonedaSecundariaIdActual === 'CLP')){
					if($scope.data.kngMonto === 0 || $scope.data.kngMonto === undefined || $scope.data.kngMonto === null || 
					   $scope.data.kngMonto === '' ){
						$scope.data.MontoSinValor = true;
					}
					$scope.data.ModalidadLiquidacionIdActual = 'COMPENSADA';
					$scope.data.HabilitadoPorCompensacion = false;
				}
				
				if((($scope.data.MonedaPrincipalIdActual === 'EUR' && $scope.data.MonedaSecundariaIdActual === 'UF') || ($scope.data.MonedaPrincipalIdActual==='USD' && $scope.data.MonedaSecundariaIdActual==='UF')) &&
				   ($scope.data.kngMontoUf === 0 || $scope.data.kngMontoUf === undefined || $scope.data.kngMontoUf === null || $scope.data.kngMontoUf === '')){
					var respuesta = {};
					respuesta.P_OUT_ADVERTENCIASDETALLE = [];
					respuesta.P_OUT_ADVERTENCIASDETALLE.push({"Descripcion":'SIN_VALOR_TIPO_CAMBIO_UF_SECARE', "RequiereAutorizacion":'N'});
					bcsComunes.ventanaMsg({
						//mensaje:$translate.instant("UI_ADVERTENCIA"),
						conflictos:util.parseServerResponse(respuesta)
					},idVentana);
				}
			}
			
			$scope.data.kngMontoMonedaPrincipal	= $scope.data.kngMonto;
			$scope.data.kngMontoMonedaSecundaria= 0;
			$scope.data.kngMontoPago			= 0;
			if($scope.data.OperacionSeleccionada && $scope.data.OperacionSeleccionada.IdTipoOperacion === 'COMFWD' && 
			   $scope.data.oConfiguracionMonedaSecundaria.seleccionado){ 
				if($scope.data.oConfiguracionFormaPago.seleccionado !== undefined && 
				   $scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'ENTREGAMOS'){
					$scope.data.kngMontoPago			= $scope.data.kngMontoMonedaSecundaria;
					$scope.data.DecimalesMontoFormaPago	= $scope.data.oConfiguracionMonedaSecundaria.seleccionado.MonedaSecundariaCantidadDecimales;
				}
				else if($scope.data.oConfiguracionFormaPago.seleccionado !== undefined && 
						$scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'RECIBIMOS'){
					$scope.data.kngMontoPago = $scope.data.kngMontoMonedaPrincipal;
	    			$scope.data.DecimalesMontoFormaPago = $scope.data.oConfiguracionMonedaPrincipal.seleccionado.MonedaPrincipalCantidadDecimales;
				}
			}
			else if($scope.data.OperacionSeleccionada && $scope.data.OperacionSeleccionada.IdTipoOperacion === 'VENFWD' && 
				    $scope.data.oConfiguracionMonedaPrincipal.seleccionado){
				if($scope.data.oConfiguracionFormaPago.seleccionado !== undefined && 
				   $scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'ENTREGAMOS'){
					$scope.data.kngMontoPago			= $scope.data.kngMontoMonedaPrincipal;
	    			$scope.data.DecimalesMontoFormaPago	= $scope.data.oConfiguracionMonedaPrincipal.seleccionado.MonedaPrincipalCantidadDecimales;
				}
				else if($scope.data.oConfiguracionFormaPago.seleccionado !== undefined && 
						$scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'RECIBIMOS'){
					$scope.data.kngMontoPago = $scope.data.kngMontoMonedaSecundaria;
	    			$scope.data.oConfiguracionMonedaSecundaria.seleccionado.MonedaSecundariaCantidadDecimales;
				}
    			}
		}
		
		$scope.$watch('data.OperacionSeleccionada',function(newval,oldval){
			if(newval === oldval || newval === undefined || !newval) return;
			
			var idMonedaPrincipalSel	= $scope.data.MonedaPrincipalIdActual	? $scope.data.MonedaPrincipalIdActual	: $scope.data.MonedaPrincipalDefecto;
			var idMonedaSecundariaSel	= $scope.data.MonedaSecundariaIdActual	? $scope.data.MonedaSecundariaIdActual	: $scope.data.MonedaSecundariaDefecto;

			if(idMonedaPrincipalSel != '' && idMonedaSecundariaSel != ''){
				bcsServiciosMesa.ConsultarMoneda().$promise.then(function(respuesta){
					
					for(var i=0; i < respuesta.d.results.length; i++){
						if(respuesta.d.results[i].IdMonedaPrincipal == idMonedaPrincipalSel && respuesta.d.results[i].IdMonedaSecundaria == idMonedaSecundariaSel && respuesta.d.results[i].IdTipoInstrumento == 'FWD' && respuesta.d.results[i].IdEstado == 'ACT'){
							$scope.data.tipoInstrumentoSeleccionado = respuesta.d.results[i];
						}
					}
					
					if($scope.parametros.Opcion === 'INGRESO' || $scope.parametros.Opcion === 'MODIFICAR'){
						/*DataSource*/
						if(dsMedioDePago	!== undefined) dsMedioDePago.removeAll();
						if(dsMedioDePagoRec	!== undefined) dsMedioDePagoRec.removeAll();
						if(dsCarteraCalce	!== undefined) dsCarteraCalce.removeAll();
						totalMontoFormaPagoEnt = 0;
						totalMontoFormaPagoRec = 0;
					}
					else{
						if(antTipoOperacion && $scope.data.OperacionSeleccionada.IdTipoOperacion !== antTipoOperacion){
							$scope.data.OperacionIdActual = antTipoOperacion;
							return;
						}
					}
					if($scope.data.tipoInstrumentoSeleccionado){
						$scope.data.kngMontoMonedaPrincipal = $scope.data.kngMonto;
						if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico !== 'ARBITR' && $scope.data.kngTCCierre){
							$scope.data.kngMontoMonedaSecundaria = $scope.data.kngMonto * $scope.data.kngTCCierre;
						}
						else if($scope.data.kngParidadCierre){
							if($scope.data.oConfiguracionMonedaPrincipal.seleccionado && 
							   $scope.data.oConfiguracionMonedaPrincipal.seleccionado.MultiplicaODivide === 'D'){
								$scope.data.kngMontoMonedaSecundaria = $scope.data.kngMonto / $scope.data.kngParidadCierre;
							}
							else{
								$scope.data.kngMontoMonedaSecundaria = $scope.data.kngMonto * $scope.data.kngParidadCierre;
							}
						}
						
						if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'ARBITR'){
							if($scope.data.MonedaPrincipalIdActual && $scope.parametros.Opcion !== "CONSULTAR" && $scope.parametros.Opcion !== "MODIFICAR") {
								$scope.data.kngTCCosto = fnBuscoParidadTC($scope.data.MonedaPrincipalIdActual);
							}
						}
						else if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SECANO') {
							if($scope.data.MonedaPrincipalIdActual === 'USD'){
								//fnBuscoPuntasDefault($scope.data.MonedaPrincipalIdActual, $scope.parametros.Opcion);
								if ($scope.parametros.Opcion !== "CONSULTAR"){
						            $scope.data.kngTCCosto = $scope.data.kngMontoDO || fnBuscoReferenciaMercado($scope.data.MonedaPrincipalIdActual) || 0;
								}
								
							}
							else{
								$scope.data.kngTCCosto = fnBuscoReferenciaMercado($scope.data.MonedaPrincipalIdActual) || 0;
							}
						} else if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SEINFL' ||
							$scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SECARE'){
							$scope.data.TipoCotizador = "UI_TC_COSTO";
					
							if ($scope.parametros.Opcion !== "CONSULTAR"){
								var TCPrincipal = fnBuscoTC($scope.data.MonedaPrincipalIdActual);
								var TCSecundaria = fnBuscoTC($scope.data.MonedaSecundariaIdActual);
								if ($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SECARE') {
									if ($scope.data.MonedaPrincipalIdActual === 'USD' && $scope.data.MonedaSecundariaIdActual === 'UF') {
										$scope.data.kngTCCosto = parseFloat(($scope.data.kngMontoDO / $scope.data.kngMontoUf).toFixed($scope.data.DecimalesMonSecInstrumento))
									}else if ($scope.data.MonedaPrincipalIdActual === 'EUR' && $scope.data.MonedaSecundariaIdActual === 'UF'){
										$scope.data.kngTCCosto = parseFloat((TCPrincipal / TCSecundaria).toFixed($scope.data.DecimalesMonSecInstrumento))	
									}
								} else {
									$scope.data.kngTCCosto = fnBuscoReferenciaMercado('UF') || 0;
								}						
							}
						}
						
						var tcParCierre = $scope.data.tipoInstrumentoSeleccionado.Nemotecnico !== 'ARBITR' ? $scope.data.kngTCCierre : $scope.data.kngParidadCierre;
						calculoMargenMontoDiferido($scope.data.kngTCForward, tcParCierre, $scope.data.kngTCCosto, $scope.data.kngMonto);
						
						if($scope.data.oConfiguracionFormaPago.seleccionado !== undefined){
							if($scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'ENTREGAMOS'){
								if(newval.IdTipoOperacion === 'COMFWD'){ 
									$scope.data.kngMontoPago			= $scope.data.kngMontoMonedaSecundaria;
									$scope.data.DecimalesMontoFormaPago	= $scope.data.oConfiguracionMonedaSecundaria.seleccionado.MonedaSecundariaCantidadDecimales;
								}
								else if(newval.IdTipoOperacion === 'VENFWD'){ 
					    			$scope.data.kngMontoPago			= $scope.data.kngMontoMonedaPrincipal;
					    			$scope.data.DecimalesMontoFormaPago	= $scope.data.oConfiguracionMonedaPrincipal.seleccionado.MonedaPrincipalCantidadDecimales;
					    		}
					    		$scope.HabilitarMontoPago = false;
							}
							else if($scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'RECIBIMOS'){
								if(newval.IdTipoOperacion === 'COMFWD'){ 
					    			$scope.data.kngMontoPago = $scope.data.kngMontoMonedaPrincipal;
					    			$scope.data.DecimalesMontoFormaPago = $scope.data.oConfiguracionMonedaPrincipal.seleccionado.MonedaPrincipalCantidadDecimales;
								}
								else if(newval.IdTipoOperacion === 'VENFWD'){ 
					    			$scope.data.kngMontoPago = $scope.data.kngMontoMonedaSecundaria;
					    			$scope.data.DecimalesMontoFormaPago = $scope.data.oConfiguracionMonedaSecundaria.seleccionado.MonedaSecundariaCantidadDecimales;
					    		}
					    		$scope.HabilitarMontoPago = true;
							}
						}
					}
				},function(respuesta){
		   			bcsComunes.msg("error","error",$translate.instant('UI_CONSULTAR_TIPO_INSTRUMENTO_ERROR'));
					bcsComunes.ventanaOverlay($scope.winID, false);
		   		});
			}
		});

		$scope.$watchGroup(['data.oConfiguracionMonedaPrincipal.seleccionado','data.kngPlazo','data.oConfiguracionMonedaSecundaria.seleccionado','data.oConfiguracionModalidadLiquidacion.seleccionado'],function(newval,oldval) {
			
			if($scope.data.oConfiguracionMonedaPrincipal.seleccionado === undefined || 
				$scope.data.oConfiguracionMonedaSecundaria.seleccionado === undefined || newval === oldval || $scope.parametros.Opcion=="ANULAR" || $scope.parametros.Opcion=="CONSULTAR" ){
				 return;
			}
			if(($scope.data.kngPlazo!=undefined || $scope.data.kngPlazoAux!=$scope.data.kngPlazo) && (($scope.parametros.Opcion=="INGRESO" && ingresar==0) || ($scope.parametros.Opcion=="MODIFICAR" && verificarDatosPreCargados==1) || ($scope.parametros.Opcion=="INGRESO" && ingresar>0 && verificarDatosPreCargados==1)) ){
				validaFeriado = 1;
				calculaFechaVencimiento(monedasPorProductoFeriadoMonPrinc);
			}

			if($scope.parametros.Opcion=="MODIFICAR" && $scope.data.kngPlazo!== undefined && $scope.data.oConfiguracionMonedaPrincipal.seleccionado !== undefined && $scope.data.oConfiguracionMonedaSecundaria.seleccionado !== undefined && $scope.data.oConfiguracionModalidadLiquidacion.seleccionado!==undefined ){
				verificarDatosPreCargados=1;
			}

			if(ingresar>0 && $scope.data.oClientePortafolio.seleccionado!=undefined){
				verificarDatosPreCargados=1;
				ingresar=0;
			}
		});
		
		$scope.$watchGroup(['data.oConfiguracionMonedaPrincipal.seleccionado','data.oConfiguracionMonedaSecundaria.seleccionado'],function(newval,oldval) {   
            if($scope.data.oConfiguracionMonedaPrincipal.seleccionado === undefined || 
               $scope.data.oConfiguracionMonedaSecundaria.seleccionado === undefined || newval === oldval){
            	return;
            }
            
			var idMonedaPrincipalSel	= $scope.data.MonedaPrincipalIdActual	? $scope.data.MonedaPrincipalIdActual	: '';
			var idMonedaSecundariaSel	= $scope.data.MonedaSecundariaIdActual	? $scope.data.MonedaSecundariaIdActual	: '';
			$scope.data.oConfiguracionTipoInstrumento.filtros.IdMonedaPrincipal.value	= idMonedaPrincipalSel;
			$scope.data.oConfiguracionTipoInstrumento.filtros.IdMonedaSecundaria.value	= idMonedaSecundariaSel;
			
			var monPriDecimales	= $scope.data.oConfiguracionMonedaPrincipal.seleccionado.TCPrincipalDecimales;
			var tcSecDecimales	= $scope.data.oConfiguracionMonedaSecundaria.seleccionado.TCSecundariaDecimales;
			$scope.data.DecimalesPorInstrumento = monPriDecimales;
			$scope.data.DecimalesMonSecInstrumento = idMonedaSecundariaSel === 'UF' ? tcSecDecimales : monPriDecimales;
		});
		
		
		$scope.$watch('data.kngMonto',function(newval,oldval){
			if(newval === oldval || newval === undefined) return;
			
			if($scope.parametros.Opcion === 'INGRESO' || $scope.parametros.Opcion === 'MODIFICAR'){
				/*DataSource*/
				if(dsMedioDePago	!== undefined) dsMedioDePago.removeAll();
				if(dsMedioDePagoRec	!== undefined) dsMedioDePagoRec.removeAll();
				if(dsCarteraCalce	!== undefined) dsCarteraCalce.removeAll();
				totalMontoFormaPagoEnt = 0;
				totalMontoFormaPagoRec = 0;
			}
			
			if($scope.data.tipoInstrumentoSeleccionado && ($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SEINFL' || 
			   $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SECARE')){
				var decimalesMontoInicial = $scope.data.oConfiguracionMonedaSecundaria.seleccionado.MonedaSecundariaCantidadDecimales;
				var montoInicial= parseFloat(($scope.data.kngMonto * $scope.data.kngTCCosto).toFixed(decimalesMontoInicial));
				var montoFinal	= parseFloat(($scope.data.kngMonto * $scope.data.kngTCCierre).toFixed(decimalesMontoInicial));				
				// solo si valores son distintos, se asigna nuevo valor. Esto para evitar $watch repetitivos
				if($scope.data.kngMontoInicial !== montoInicial){
					$scope.data.kngMontoInicial = montoInicial;
				}
				if($scope.data.kngMontoFinal !== montoFinal){
					$scope.data.kngMontoFinal = montoFinal;
				}
			}

			$scope.data.kngMontoMonedaPrincipal = $scope.data.kngMonto;
			if($scope.data.tipoInstrumentoSeleccionado && $scope.data.tipoInstrumentoSeleccionado.Nemotecnico !== 'ARBITR'){
				$scope.data.kngMontoMonedaSecundaria = $scope.data.kngMonto * $scope.data.kngTCCierre;
				calculoMargenMontoDiferido($scope.data.kngTCForward, $scope.data.kngTCCierre, $scope.data.kngTCCosto, $scope.data.kngMonto);
			}
			else{
				if($scope.data.oConfiguracionMonedaPrincipal.seleccionado && 
				   $scope.data.oConfiguracionMonedaPrincipal.seleccionado.MultiplicaODivide === 'D'){
					$scope.data.kngMontoMonedaSecundaria = $scope.data.kngMonto / $scope.data.kngParidadCierre;
				}
				else{
					$scope.data.kngMontoMonedaSecundaria = $scope.data.kngMonto * $scope.data.kngParidadCierre;
				}
				calculoMargenMontoDiferido($scope.data.kngTCForward, $scope.data.kngParidadCierre, $scope.data.kngTCCosto, $scope.data.kngMonto);
			}

			$scope.data.kngMontoCalce = $scope.data.kngMonto;
			montoPrincipal = $scope.data.kngMonto;
			montoSecundario = $scope.data.kngMontoMonedaSecundaria;
			
			if($scope.data.oConfiguracionModalidadLiquidacion.seleccionado && $scope.data.oConfiguracionFormaPago.seleccionado){
				if($scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'ENTREGAMOS'){
					if($scope.data.OperacionSeleccionada.IdTipoOperacion === 'COMFWD'){
						$scope.data.kngMontoPago = $scope.data.kngMontoMonedaSecundaria;
						$scope.data.DecimalesMontoFormaPago = $scope.data.oConfiguracionMonedaSecundaria.seleccionado.MonedaSecundariaCantidadDecimales;
					}
					else if($scope.data.OperacionSeleccionada.IdTipoOperacion === 'VENFWD'){ 
		    			$scope.data.kngMontoPago = $scope.data.kngMontoMonedaPrincipal;
		    			$scope.data.DecimalesMontoFormaPago = $scope.data.oConfiguracionMonedaPrincipal.seleccionado.MonedaPrincipalCantidadDecimales;
		    		}
		    		//$scope.data.FechaMedioPagoDisabled = true;
		    		$scope.HabilitarMontoPago = false;
				}
				else if($scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'RECIBIMOS'){
					if($scope.data.OperacionSeleccionada.IdTipoOperacion === 'COMFWD'){ 
		    			$scope.data.kngMontoPago = $scope.data.kngMontoMonedaPrincipal;
		    			$scope.data.DecimalesMontoFormaPago = $scope.data.oConfiguracionMonedaPrincipal.seleccionado.MonedaPrincipalCantidadDecimales;
					}
					else if($scope.data.OperacionSeleccionada.IdTipoOperacion === 'VENFWD'){ 
		    			$scope.data.kngMontoPago = $scope.data.kngMontoMonedaSecundaria;
		    			$scope.data.DecimalesMontoFormaPago = $scope.data.oConfiguracionMonedaSecundaria.seleccionado.MonedaSecundariaDecimales;
		    		}
		    		//$scope.data.FechaMedioPagoDisabled = false;
		    		$scope.HabilitarMontoPago = true;
				}
				
				if(dsMedioDePago.view().length > 0){
					$scope.data.kngFechaPagoFormaPago = dsMedioDePago.view()[0].FechaPagoMedio;
				}
			}
		});
		
		
		$scope.$watch('data.kngMontoInicial',function(newval,oldval){
			if(newval === oldval || newval === undefined || $scope.data.kngMonto === undefined) return;
			if($scope.data.tipoInstrumentoSeleccionado && ($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SEINFL' || 
			   $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SECARE')){
				var tcCosto = $scope.data.kngMonto ? parseFloat(($scope.data.kngMontoInicial / $scope.data.kngMonto).toFixed($scope.data.DecimalesMonSecInstrumento)): 0;
				// solo si valores son distintos, se asigna nuevo valor. Esto para evitar $watch repetitivos
				if($scope.data.kngMonto && $scope.data.kngTCCosto !== tcCosto ){
					$scope.data.kngTCCosto = tcCosto;
				}
			}
		});
		
		
		$scope.$watch('data.kngMontoFinal',function(newval,oldval){
			if(newval === oldval || newval === undefined || $scope.data.kngMonto === undefined) return;
			if($scope.data.tipoInstrumentoSeleccionado && ($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SEINFL' || 
			   $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SECARE')){
				var tcCierre = $scope.data.kngMonto !== 0 ? parseFloat(($scope.data.kngMontoFinal / $scope.data.kngMonto).toFixed($scope.data.DecimalesMonSecInstrumento)): 0;
				// solo si valores son distintos, se asigna nuevo valor. Esto para evitar $watch repetitivos
				if($scope.data.kngTCCierre !== tcCierre){
					$scope.data.kngTCCierre = tcCierre;
				}
			}
		});
		
		
		$scope.$watch('data.kngTCCierre',function(newval,oldval){
			if(newval === oldval || newval === undefined || $scope.data.kngMonto === undefined) return;
			
			if($scope.parametros.Opcion === 'INGRESO' || $scope.parametros.Opcion === 'MODIFICAR'){
				/*DataSource*/
				if(dsMedioDePago	!== undefined) dsMedioDePago.removeAll();
				if(dsMedioDePagoRec	!== undefined) dsMedioDePagoRec.removeAll();
				if(dsCarteraCalce	!== undefined) dsCarteraCalce.removeAll();
				totalMontoFormaPagoEnt = 0;
				totalMontoFormaPagoRec = 0;
			}
			
			$scope.data.kngParidadCierre = undefined;
			
			
			var montoFinal = $scope.data.kngMonto * $scope.data.kngTCCierre;
			
			if($scope.data.tipoInstrumentoSeleccionado && ($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SEINFL' || 
			   $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SECARE')){
				// solo si valores son distintos, se asigna nuevo valor. Esto para evitar $watch repetitivos
				const decimalesMontoFinal = $scope.data.oConfiguracionMonedaSecundaria.seleccionado.MonedaSecundariaCantidadDecimales;
				montoFinal = parseFloat(montoFinal.toFixed(decimalesMontoFinal));
			
				if($scope.data.kngMontoFinal !== montoFinal){
					$scope.data.kngMontoFinal = montoFinal;
				}
			}
			
			$scope.data.kngMontoMonedaPrincipal = $scope.data.kngMonto;
			$scope.data.kngMontoMonedaSecundaria = montoFinal;
			montoSecundario = $scope.data.kngMontoMonedaSecundaria;
			
			calculoMargenMontoDiferido($scope.data.kngTCForward, $scope.data.kngTCCierre, $scope.data.kngTCCosto, $scope.data.kngMonto);
			
			if($scope.data.oConfiguracionModalidadLiquidacion.seleccionado && $scope.data.oConfiguracionFormaPago.seleccionado &&
			   $scope.data.kngMontoMonedaSecundaria !== $scope.data.kngMontoPago){
				if($scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'ENTREGAMOS'){
					if ($scope.data.OperacionSeleccionada.IdTipoOperacion === 'COMFWD'){ 
						$scope.data.kngMontoPago = $scope.data.kngMontoMonedaSecundaria;
						$scope.data.DecimalesMontoFormaPago = $scope.data.oConfiguracionMonedaSecundaria.seleccionado.MonedaSecundariaDecimales;
					}
					else if ($scope.data.OperacionSeleccionada.IdTipoOperacion === 'VENFWD'){ 
		    			$scope.data.kngMontoPago = $scope.data.kngMontoMonedaPrincipal;
		    			$scope.data.DecimalesMontoFormaPago = $scope.data.oConfiguracionMonedaPrincipal.seleccionado.MonedaPrincipalCantidadDecimales;
		    		}
		    		//$scope.data.FechaMedioPagoDisabled = true;
		    		$scope.HabilitarMontoPago = false;
				}
				else if($scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'RECIBIMOS'){
					if($scope.data.OperacionSeleccionada.IdTipoOperacion === 'COMFWD'){ 
		    			$scope.data.kngMontoPago = $scope.data.kngMontoMonedaPrincipal;
		    			$scope.data.DecimalesMontoFormaPago = $scope.data.oConfiguracionMonedaPrincipal.seleccionado.MonedaPrincipalCantidadDecimales;
					}
					else if ($scope.data.OperacionSeleccionada.IdTipoOperacion === 'VENFWD'){ 
		    			$scope.data.kngMontoPago = $scope.data.kngMontoMonedaSecundaria;
		    			$scope.data.DecimalesMontoFormaPago = $scope.data.oConfiguracionMonedaSecundaria.seleccionado.MonedaSecundariaDecimales;
		    		}
		    		//$scope.data.FechaMedioPagoDisabled = false;
		    		$scope.HabilitarMontoPago = true;
				}
				if(dsMedioDePago.view().length > 0){
					$scope.data.kngFechaPagoFormaPago = dsMedioDePago.view()[0].FechaPagoMedio;
				}
			}
		});
		
		
		$scope.$watch('data.kngParidadCierre',function(newval,oldval){
			if(newval === oldval || newval === undefined || $scope.data.kngMonto === undefined || 
			   ($scope.data.tipoInstrumentoSeleccionado && $scope.data.tipoInstrumentoSeleccionado.Nemotecnico !== 'ARBITR')) return;
			
			if($scope.parametros.Opcion === 'INGRESO' || $scope.parametros.Opcion === 'MODIFICAR'){
				/*DataSource*/
				if(dsMedioDePago	!== undefined) dsMedioDePago.removeAll();
				if(dsMedioDePagoRec	!== undefined) dsMedioDePagoRec.removeAll();
				if(dsCarteraCalce	!== undefined) dsCarteraCalce.removeAll();
				totalMontoFormaPagoEnt = 0;
				totalMontoFormaPagoRec = 0;
			}
			
			$scope.data.kngTCCierre = undefined;
			
			$scope.data.kngMontoMonedaPrincipal = $scope.data.kngMonto;
			if($scope.data.oConfiguracionMonedaPrincipal.seleccionado && 
			   $scope.data.oConfiguracionMonedaPrincipal.seleccionado.MultiplicaODivide === 'D'){
				$scope.data.kngMontoMonedaSecundaria = $scope.data.kngParidadCierre ? $scope.data.kngMonto / $scope.data.kngParidadCierre : 0;
			}
			else{
				$scope.data.kngMontoMonedaSecundaria = $scope.data.kngMonto * $scope.data.kngParidadCierre;
			}
			montoSecundario = $scope.data.kngMontoMonedaSecundaria;
			
			calculoMargenMontoDiferido($scope.data.kngTCForward, $scope.data.kngParidadCierre, $scope.data.kngTCCosto, $scope.data.kngMonto);
			
			if($scope.data.oConfiguracionModalidadLiquidacion.seleccionado && $scope.data.oConfiguracionFormaPago.seleccionado &&
			   $scope.data.kngMontoMonedaSecundaria !== $scope.data.kngMontoPago){
				if($scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'ENTREGAMOS'){
					if($scope.data.OperacionSeleccionada.IdTipoOperacion === 'COMFWD'){ 
						$scope.data.kngMontoPago = $scope.data.kngMontoMonedaSecundaria;
						$scope.data.DecimalesMontoFormaPago = $scope.data.oConfiguracionMonedaSecundaria.seleccionado.MonedaSecundariaCantidadDecimales;
					}
					else if($scope.data.OperacionSeleccionada.IdTipoOperacion === 'VENFWD'){ 
		    			$scope.data.kngMontoPago = $scope.data.kngMontoMonedaPrincipal;
		    			$scope.data.DecimalesMontoFormaPago = $scope.data.oConfiguracionMonedaPrincipal.seleccionado.MonedaPrincipalCantidadDecimales;
		    		}
		    		//$scope.data.FechaMedioPagoDisabled = true;
		    		$scope.HabilitarMontoPago = false;
				}
				else if($scope.data.oConfiguracionFormaPago.seleccionado.ValorParametro === 'RECIBIMOS'){
					if($scope.data.OperacionSeleccionada.IdTipoOperacion === 'COMFWD'){ 
		    			$scope.data.kngMontoPago = $scope.data.kngMontoMonedaPrincipal;
		    			$scope.data.DecimalesMontoFormaPago = $scope.data.oConfiguracionMonedaPrincipal.seleccionado.MonedaPrincipalCantidadDecimales;
					}
					else if($scope.data.OperacionSeleccionada.IdTipoOperacion === 'VENFWD'){ 
		    			$scope.data.kngMontoPago = $scope.data.kngMontoMonedaSecundaria;
		    			$scope.data.DecimalesMontoFormaPago = $scope.data.oConfiguracionMonedaSecundaria.seleccionado.MonedaSecundariaCantidadDecimales;
		    		}
		    		//$scope.data.FechaMedioPagoDisabled = false;
		    		$scope.HabilitarMontoPago = true;
				}
				if(dsMedioDePago.view().length > 0){
					$scope.data.kngFechaPagoFormaPago = dsMedioDePago.view()[0].FechaPagoMedio;
				}
			}
		});
		
		
		$scope.$watch('data.kngPlazo',function(newval,oldval){// TODO
			if(newval === oldval || newval== $scope.data.kngPlazoAux ) return;
			if(monedasPorProductoFeriadoMonPrinc){
				let plazoVencimientoMinimo =  Number(fnBuscoValorParametro('PLAZOVENCIMIENTOMINIMO'));
				if(newval === 0) newval = plazoVencimientoMinimo;
				// valida plazo minimo de vencimiento de operacion forward
				if(newval < plazoVencimientoMinimo){
					bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('UI_PLAZO_MINIMO_VENCIMIENTO_FORWARD').replace("::CantidadDias::", plazoVencimientoMinimo));
					newval = plazoVencimientoMinimo;
				}
			
				if($scope.parametros.Opcion !== 'INGRESO' && $scope.parametros.Opcion !== 'MODIFICAR') return;
				
				if(newval !== $scope.data.kngPlazoAux && (($scope.parametros.Opcion=="INGRESO" && ingresar==0) || ($scope.parametros.Opcion=="MODIFICAR" && verificarDatosPreCargados==1) || ($scope.parametros.Opcion=="INGRESO" && ingresar>0 && verificarDatosPreCargados==1))){
					calculaFechaVencimiento(monedasPorProductoFeriadoMonPrinc);
				}
				else{
					$scope.data.kngPlazo = $scope.data.kngPlazoAux;
				}
			}
		});
		
		
		$scope.$watch('data.kngFechaVencimiento', function(newval,oldval){
	   		if(angular.equals(newval,oldval)) return;
	   		if(Date.parse(newval) === Date.parse(oldval)) return;
	   		if($scope.parametros.Opcion !== 'INGRESO' && $scope.parametros.Opcion !== 'MODIFICAR') return;
	   		if(newval === undefined){
	   			$scope.data.kngFechaVencimiento = $scope.data.kngFechaVencimientoAux;
	   			return;
	   		}

			if($scope.data.kngFechaVencimiento < $scope.data.kngFechaOperacion){
				$scope.data.kngFechaVencimiento = undefined;
				bcsComunes.msg("warning","warning",$translate.instant('La fecha vencimiento no puede ser menor a la fecha de operación.'));
			}
			else{
				if(Date.parse(newval) !== Date.parse($scope.data.kngFechaVencimientoAux)){
					var varIdPais = $scope.data.PlazaSeleccionado ? $scope.data.PlazaSeleccionado.IdPais : $scope.SesionIdPaisSistema;
					var varPlazo = Math.round(util.datediff(newval,$scope.data.kngFechaOperacion).asDays());
					varPlazo = varPlazo === 0 ? Number(fnBuscoValorParametro('PLAZOVENCIMIENTOMINIMO')) : varPlazo;
			
					var datos = {
						p_IdEmpresa			: $scope.SesionIdEmpresa,
						p_IdUsuario			: $scope.SesionIdUsuario,
						p_IdModulo			: 'MFW',
						p_UsaReglasNegocio	: 'N',
						p_IniFecha			: varFechaVencimientoIni,
						p_Fecha				: util.dateSPdate($scope.data.kngFechaOperacion),
						p_NumeroDias		: varPlazo || $scope.data.kngPlazo,
						p_EsHabil			: 'S',
						p_IdPais			: varIdPais
					};
					
					bcsServiciosMesa.FechaVencimiento(datos,function(respuesta){
						if (respuesta.P_OUT_ERRORESCONTADOR === 0){
							var pOutFecha = respuesta.P_OUT_FECHA;
							
							if(varFechaVencimientoIni === 'N' && respuesta.P_OUT_ESHABIL === 'N'){
								bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_MENSAJE_PLAZO_DIA_NO_HABIL_O_FERIADO"), conflictos:util.parseServerResponse(respuesta)},idVentana);
							}
							
							$scope.data.LimiteFechaPago		= {};
							$scope.data.LimiteFechaFixing	= {};
							$scope.data.LimiteFechaFormaPago= {};
							
							$scope.data.kngFechaVencimiento		= util.dateInicioDia(pOutFecha);
							$scope.data.kngFechaVencimientoAux	= util.dateInicioDia(pOutFecha);
							$scope.data.kngFechaPago			= util.dateInicioDia(pOutFecha);
							$scope.data.kngPlazoAux				= respuesta.P_OUT_PLAZO;
							$scope.data.kngPlazo				= respuesta.P_OUT_PLAZO;
							$scope.data.kngFechaPagoFormaPago	= util.dateInicioDia(pOutFecha);
							
							$scope.data.LimiteFechaPago.min		= new Date(util.dateInicioDia(pOutFecha));
							$scope.data.LimiteFechaFormaPago.max= new Date(util.dateInicioDia(pOutFecha));
							
							//TODO setear el mes y año del campo Fecha Fixing
							if(varFechaVencimientoIni === 'S'){
								$scope.data.kngFechaVencimientoIni = util.dateInicioDia(pOutFecha);
							}
							bcsComunes.ventanaAutoOverlay(idVentana,false,1000);
							$scope.data.LimiteFechaFixing.max = new Date(util.dateInicioDia(pOutFecha));
							if(varFechaVencimientoIni === 'N'){
								if(varFechaFixingIni === 'S' || 
								  (varFechaFixingIni === 'N' && util.dateSPdate($scope.data.kngFechaVencimientoIni) !== util.dateSPdate($scope.data.kendoWidgetFixing.dateView._current))){
									$scope.data.kendoWidgetFixing.dateView._current = util.dateInicioDia(pOutFecha);
									varFechaFixingIni = 'N';
								}
								else{
									$scope.data.kngFechaFixing = util.dateInicioDia(pOutFecha);
									$timeout(function(){
										$scope.data.kngFechaFixing = null;
									}, 1);
								}
							}
							
							//se asigna a Fecha Fixing el dia habil anterior a la Fecha Vencimiento cuando es SECANO y MX/CLP y COMPENSADA
							if($scope.data.oConfiguracionModalidadLiquidacion.seleccionado && 
								$scope.data.oConfiguracionModalidadLiquidacion.seleccionado.ValorParametro === 'COMPENSADA' && $scope.data.FixingIdActual === 'BANCOCENTRAL'){
								cargarFechaFixing();
							}
							varFechaVencimientoIni = 'N';
						}
						else{
							$scope.data.kngFechaVencimiento = util.dateInicioDia('1900-01-01');
							$scope.data.LimiteFechaPago.min		= new Date($scope.data.kngFechaVencimiento);
							$scope.data.LimiteFechaFixing.max	= new Date($scope.data.kngFechaVencimiento);
							$scope.data.LimiteFechaFormaPago.max= new Date($scope.data.kngFechaVencimiento);
							console.log("ERROR: " + JSON.stringify(respuesta));
					    }
					}
					,function(respuesta){
						bcsComunes.msg("warning","Informacion","Existen problemas al asignar Plazo a Fecha Vencimiento.");
						$scope.data.LimiteFechaPago.min		= new Date($scope.data.kngFechaVencimiento);
						$scope.data.LimiteFechaFixing.max	= new Date($scope.data.kngFechaVencimiento);
						$scope.data.LimiteFechaFormaPago.max= new Date($scope.data.kngFechaVencimiento);
						return;
					});
				}
				else{
					$scope.data.kngFechaVencimiento = $scope.data.kngFechaVencimientoAux;
				}
			}
		});
		
		
		$scope.$watch('data.OperacionIdActual',function(newval,oldval){
			if ($scope.data.OperacionIdActual)
				$scope.data.limpiarFormaPago = true;
		});
		$scope.$watch('data.oConfiguracionModalidadLiquidacion.seleccionado',function(newval,oldval){//TODO
			if(newval === undefined || newval === false){
				//$scope.data.ModalidadLiquidacionIdActual = '';
				//$scope.data.oConfiguracionModalidadLiquidacion.filtros.ValorParametro.value = '';
				return;
			}
			if($scope.parametros.Opcion === 'INGRESO'){
				$scope.data.MonedaCompensacionIdActual	= $scope.data.oConfiguracionModalidadLiquidacion.seleccionado.ValorParametro !== 'COMPENSADA'? false : 'CLP';
				$scope.data.MonedaCompensacionDefecto	= $scope.data.oConfiguracionModalidadLiquidacion.seleccionado.ValorParametro !== 'COMPENSADA'? false : 'CLP';
			} else {
				$scope.data.MonedaCompensacionIdActual	= $scope.data.oConfiguracionModalidadLiquidacion.seleccionado.ValorParametro !== 'COMPENSADA'? false : $scope.data.MonedaCompensacionIdActual;
				$scope.data.MonedaCompensacionDefecto	= $scope.data.oConfiguracionModalidadLiquidacion.seleccionado.ValorParametro !== 'COMPENSADA'? false : $scope.data.MonedaCompensacionDefecto;
			}
			
			if($scope.parametros.Opcion === 'INGRESO' || $scope.parametros.Opcion === 'MODIFICAR'){
				/*DataSource*/
				if(dsMedioDePago	!== undefined) dsMedioDePago.removeAll();
				if(dsMedioDePagoRec	!== undefined) dsMedioDePagoRec.removeAll();
				if(dsCarteraCalce	!== undefined) dsCarteraCalce.removeAll();
				totalMontoFormaPagoEnt = 0;
				totalMontoFormaPagoRec = 0;
			}
			
			$scope.data.habilitoFormasDePagos = true;
			$scope.data.limpiarFormaPago = true;
			if($scope.data.oConfiguracionModalidadLiquidacion.seleccionado.ValorParametro === 'COMPENSADA'){
				$scope.data.oConfiguracionFormaPago.filtros.ValorParametro.value = 'ENTREGAMOS';
				$scope.data.FormaPagoActual = 'ENTREGAMOS';
				$scope.data.bolIndicoFormaPago = true;
				$scope.HabilitarMontoPago = false;
				$scope.data.DeshabilitoFechaPorEntregaFisica = false;
				$scope.data.DeshabilitoBancoCentral = false;
				
				$scope.data.kngFechaPagoFormaPago = $scope.data.kngFechaVencimiento;
				
				if($scope.data.oConfiguracionFixing.seleccionado && $scope.data.oConfiguracionFixing.seleccionado.NombreParametro === 'BANCO CENTRAL' && 
				   $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SECANO'){
					$scope.data.kngFechaFixing = undefined;
					$scope.data.kngHoraFixing = undefined;
					$scope.data.HoraFixing = '';
					//$scope.data.kngFechaPagoFormaPago = undefined;
					
					if($scope.data.FixingIdActual !== 'BANCOCENTRAL' && $scope.data.oConfiguracionMonedaPrincipal.seleccionado.IdMonedaPrincipal === 'USD' && 
					   $scope.data.oConfiguracionMonedaSecundaria.seleccionado.IdMonedaSecundaria === 'CLP'){
						$scope.data.DeshabilitoBancoCentral = true;
					}
					else if($scope.data.FixingIdActual === 'BANCOCENTRAL'){
						cargarFechaFixing();
					}
				}
				//$scope.data.LimiteFechaPago.max		= new Date($scope.data.kngFechaVencimiento);
				$scope.data.LimiteFechaFixing.max	= new Date($scope.data.kngFechaVencimiento);
				//$scope.data.LimiteFechaFormaPago.max= new Date($scope.data.kngFechaVencimiento);
			}
			else if($scope.data.oConfiguracionModalidadLiquidacion.seleccionado.ValorParametro === 'ENTREGA_FISICA'){
				$scope.data.oConfiguracionFormaPago.filtros.ValorParametro.value = '';
				$scope.data.FormaPagoActual = 'ENTREGAMOS';
				$scope.data.bolIndicoFormaPago = false;
				$scope.HabilitarMontoPago = false;
				$scope.data.DeshabilitoFechaPorEntregaFisica = true;
				
				//$scope.data.kngFechaPago = undefined;
				$scope.data.kngFechaFixing = undefined;
				$scope.data.kngHoraFixing = undefined;
				$scope.data.HoraFixing = '';
				$scope.data.kngFechaPagoFormaPago = $scope.data.kngFechaVencimiento;
				
				//se calcula campo Monto del tab Forma de Pago
				$scope.data.kngMontoMonedaPrincipal = $scope.data.kngMonto;
				if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico !== 'ARBITR' && $scope.data.kngTCCierre){
					$scope.data.kngMontoMonedaSecundaria = $scope.data.kngMonto * $scope.data.kngTCCierre;
				}
				else if($scope.data.kngParidadCierre){
					if($scope.data.oConfiguracionMonedaPrincipal.seleccionado && 
					   $scope.data.oConfiguracionMonedaPrincipal.seleccionado.MultiplicaODivide === 'D'){
						$scope.data.kngMontoMonedaSecundaria = $scope.data.kngMonto / $scope.data.kngParidadCierre;
					}
					else{
						$scope.data.kngMontoMonedaSecundaria = $scope.data.kngMonto * $scope.data.kngParidadCierre;
					}
				}
				if($scope.data.OperacionSeleccionada.IdTipoOperacion === 'COMFWD'){ 
					$scope.data.kngMontoPago			= $scope.data.kngMontoMonedaSecundaria;
					$scope.data.DecimalesMontoFormaPago	= $scope.data.oConfiguracionMonedaSecundaria.seleccionado.MonedaSecundariaCantidadDecimales;
				}
				else if($scope.data.OperacionSeleccionada.IdTipoOperacion === 'VENFWD'){ 
	    			$scope.data.kngMontoPago			= $scope.data.kngMontoMonedaPrincipal;
	    			$scope.data.DecimalesMontoFormaPago	= $scope.data.oConfiguracionMonedaPrincipal.seleccionado.MonedaPrincipalCantidadDecimales;
	    		}
			}
		});
		
		
		$scope.$watch('data.kngTCCosto',function(newval,oldval){
			if(newval === oldval || newval === undefined || $scope.data.kngMonto === undefined ||
			  ($scope.data.kngTCForward === undefined && $scope.data.kngPuntosForward === undefined) || 
			  $scope.data.tipoInstrumentoSeleccionado === undefined){
				return;
			}
			
			if($scope.data.kngMonto){
				$scope.data.kngMontoInicial	= $scope.data.kngMonto * $scope.data.kngTCCosto;
				if ($scope.data.tipoInstrumentoSeleccionado && ($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SEINFL' || 
				    $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SECARE')){
					const decimalesMontoInicial = $scope.data.oConfiguracionMonedaSecundaria.seleccionado.MonedaSecundariaCantidadDecimales;
					const montoInicial = parseFloat($scope.data.kngMontoInicial.toFixed(decimalesMontoInicial));
			
					if($scope.data.kngMontoInicial !== montoInicial){
						$scope.data.kngMontoInicial = montoInicial;
					}
				}		
			}
			
			$scope.data.kngTCForward	= $scope.data.kngPuntosForward + $scope.data.kngTCCosto;
			var tcParCierre = $scope.data.tipoInstrumentoSeleccionado.Nemotecnico !== 'ARBITR' ? $scope.data.kngTCCierre : $scope.data.kngParidadCierre;
			calculoMargenMontoDiferido($scope.data.kngTCForward, tcParCierre, $scope.data.kngTCCosto, $scope.data.kngMonto);
		});
		
		
		$scope.$watch('data.ChkPuntosForward',function(newval,oldval){
			if($scope.data.ChkPuntosForward){
				$scope.data.ChkTasasForward = false;
				$scope.data.kngTCForward = 0;
				$scope.data.kngPuntosForward = 0;
				$scope.data.kngUsd = undefined;
				$scope.data.kngClp = undefined; 
			}
			else{
				$scope.data.kngMontoDiferido = 0;
				$scope.data.kngMargen = 0;
				$scope.data.ChkTasasForward = true;
			}
		});
		
		$scope.$watch('data.ChkTasasForward',function(newval,oldval){
			if($scope.data.ChkTasasForward){
				$scope.data.ChkPuntosForward = false;
				$scope.data.kngUsd = 0;
				$scope.data.kngClp = 0; 
				$scope.data.kngTCForward = undefined;
				$scope.data.kngPuntosForward = undefined;
			}
			else{
				$scope.data.kngMontoDiferido = 0;
				$scope.data.kngMargen = 0;
				$scope.data.ChkPuntosForward = true;
			}
		});
		
		
		$scope.$watch('data.kngPuntosForward',function(newval,oldval){
			if(newval === oldval || $scope.data.kngTCCosto === undefined || $scope.data.kngMonto === undefined ||
			  ($scope.data.kngTCForward === undefined && $scope.data.kngPuntosForward === undefined) || 
			  $scope.data.tipoInstrumentoSeleccionado === undefined){
				return;
			}
			$scope.data.kngTCForward = $scope.data.kngTCCosto + newval;
			var tcParCierre = $scope.data.tipoInstrumentoSeleccionado.Nemotecnico !== 'ARBITR' ? $scope.data.kngTCCierre : $scope.data.kngParidadCierre;
			calculoMargenMontoDiferido($scope.data.kngTCForward, tcParCierre, $scope.data.kngTCCosto, $scope.data.kngMonto);
		});
		
		
		$scope.$watch('data.kngTCForward',function(newval,oldval){
			if(newval === oldval || $scope.data.kngTCCosto === undefined || $scope.data.kngMonto === undefined ||
			  ($scope.data.kngTCForward === undefined && $scope.data.kngPuntosForward === undefined) || 
			  $scope.data.tipoInstrumentoSeleccionado === undefined){
				return;
			}
			$scope.data.kngPuntosForward = newval - $scope.data.kngTCCosto;
			var tcParCierre = $scope.data.tipoInstrumentoSeleccionado.Nemotecnico !== 'ARBITR' ? $scope.data.kngTCCierre : $scope.data.kngParidadCierre;
			calculoMargenMontoDiferido($scope.data.kngTCForward, tcParCierre, $scope.data.kngTCCosto, $scope.data.kngMonto);
		});
		
		$scope.$watch('data.MonedaCompensacionIdActual',function(newval,oldval){
			if(newval === oldval || newval === undefined || $scope.data.MonedaCompensacionIdActual === undefined) return;
			if($scope.data.ModalidadLiquidacionIdActual === 'COMPENSADA' && (!$scope.data.MonedaCompensacionIdActual || $scope.data.MonedaCompensacionIdActual === '')){
				$scope.data.MonedaCompensacionIdActual	= 'CLP';
				$scope.data.MonedaCompensacionDefecto	= 'CLP';
			}
		});
		
		var calculoMargenMontoDiferido = function(tcForward, tcParCierre, tcCosto, montoOperacion){
			if($scope.parametros.Opcion === "INGRESO" || $scope.parametros.Opcion === "MODIFICAR"){
				var dolarObservado = $scope.data.MonedaSecundariaIdActual === 'USD' ? $scope.data.kngMontoDO : 1;
				if($scope.data.OperacionSeleccionada && $scope.data.OperacionSeleccionada.IdTipoOperacion === 'COMFWD'){
					if($scope.data.tipoInstrumentoSeleccionado && $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'ARBITR' &&
					   $scope.data.oConfiguracionMonedaPrincipal.seleccionado &&
					   $scope.data.oConfiguracionMonedaPrincipal.seleccionado.MultiplicaODivide === 'D'){
						var parte1 = tcParCierre ? (montoOperacion / tcParCierre) : 0;
						var parte2 = tcCosto ? (montoOperacion / tcCosto) : 0;
						$scope.data.kngMontoDiferido = parte1 - parte2;
						
						parte2 = tcForward ? (montoOperacion / tcForward) : 0;
						$scope.data.kngMargen = (parte2 - parte1) * dolarObservado;
					}
					else{
						$scope.data.kngMontoDiferido = montoOperacion * (tcParCierre - tcCosto);
						$scope.data.kngMargen = montoOperacion * (tcForward - tcParCierre) * dolarObservado;
					}
					if($scope.data.tipoInstrumentoSeleccionado && 
							$scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SECARE' && 
							($scope.data.tipoInstrumentoSeleccionado.IdMonedaPrincipal === 'USD' || $scope.data.tipoInstrumentoSeleccionado.IdMonedaPrincipal === 'EUR') && 
							$scope.data.tipoInstrumentoSeleccionado.IdMonedaSecundaria === 'UF' && 
							$scope.data.oConfiguracionMonedaPrincipal.seleccionado && 
							$scope.data.oConfiguracionMonedaPrincipal.seleccionado.MultiplicaODivide === 'M'){
						$scope.data.kngMargen = $scope.data.kngMargen * $scope.data.kngMontoUf;
					}
				}
				else if($scope.data.OperacionSeleccionada && $scope.data.OperacionSeleccionada.IdTipoOperacion === 'VENFWD'){
					if($scope.data.tipoInstrumentoSeleccionado && $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'ARBITR' && 
					   $scope.data.oConfiguracionMonedaPrincipal.seleccionado &&
					   $scope.data.oConfiguracionMonedaPrincipal.seleccionado.MultiplicaODivide === 'D'){
						var parte1 = tcCosto ? (montoOperacion / tcCosto) : 0;
						var parte2 = tcParCierre ? (montoOperacion / tcParCierre) : 0;
						$scope.data.kngMontoDiferido = parte1 - parte2;
						
						parte1 = tcForward ? (montoOperacion / tcForward) : 0;
						$scope.data.kngMargen = (parte2 - parte1) * dolarObservado;
					}
					else{
						$scope.data.kngMontoDiferido = montoOperacion * (tcCosto - tcParCierre);
						$scope.data.kngMargen = montoOperacion * (tcParCierre - tcForward) * dolarObservado;
					}
					if($scope.data.tipoInstrumentoSeleccionado && 
							$scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SECARE' && 
							($scope.data.tipoInstrumentoSeleccionado.IdMonedaPrincipal === 'USD' || $scope.data.tipoInstrumentoSeleccionado.IdMonedaPrincipal === 'EUR') && 
							$scope.data.tipoInstrumentoSeleccionado.IdMonedaSecundaria === 'UF' && 
							$scope.data.oConfiguracionMonedaPrincipal.seleccionado && 
							$scope.data.oConfiguracionMonedaPrincipal.seleccionado.MultiplicaODivide === 'M'){
						$scope.data.kngMargen = $scope.data.kngMargen * $scope.data.kngMontoUf;
					}
				}
			}
		};
		
		
		$scope.$watch('data.oConfiguracionFixing.seleccionado',function(newval,oldval){
			if(newval === undefined || newval === false) return;
			if(newval === oldval) return;
			
			if(newval.NombreParametro !== 'BANCO CENTRAL' && 
			   $scope.data.oConfiguracionModalidadLiquidacion.seleccionado.ValorParametro === 'COMPENSADA' && 
			   $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SECANO' && 
			   $scope.data.oConfiguracionMonedaPrincipal.seleccionado.IdMonedaPrincipal === 'USD' && 
			   $scope.data.oConfiguracionMonedaSecundaria.seleccionado.IdMonedaSecundaria === 'CLP'){
				$scope.data.kngFechaFixing = undefined;
				$scope.data.kngHoraFixing = undefined;
				$scope.data.HoraFixing = '';
				$scope.data.DeshabilitoBancoCentral = false;
			}else if(newval.NombreParametro === 'BANCO CENTRAL' && $scope.data.oConfiguracionModalidadLiquidacion.seleccionado.ValorParametro === 'COMPENSADA'){
					cargarFechaFixing();
			}
		});
		
		
		$scope.$watch('data.kngFechaFixing',function(newval,oldval){
			if(newval === oldval) return;
			if($scope.data.oConfiguracionModalidadLiquidacion.seleccionado === undefined) return;
			
			if($scope.data.oConfiguracionModalidadLiquidacion.seleccionado.ValorParametro === 'COMPENSADA'){	
				//$scope.data.kngFechaPagoFormaPago = $scope.data.kngFechaFixing;	//ESTO ESTA MAL
			}			
		});
		
		
		/***** Tab Forma de Pago *****/
		$scope.$watch('data.oConfiguracionFormaPago.seleccionado', function(newValue,oldValue){//TODO
			if(newValue === oldValue)
				return;
			
			if($scope.data.oConfiguracionFormaPago.seleccionado !== undefined){
				$scope.data.kngMontoMonedaPrincipal = $scope.data.kngMonto;
				if($scope.data.tipoInstrumentoSeleccionado && $scope.data.tipoInstrumentoSeleccionado.Nemotecnico !== 'ARBITR' && 
				   $scope.data.kngTCCierre){
					$scope.data.kngMontoMonedaSecundaria = $scope.data.kngMonto * $scope.data.kngTCCierre;
				}
				else if($scope.data.kngParidadCierre){
					if($scope.data.oConfiguracionMonedaPrincipal.seleccionado && 
					   $scope.data.oConfiguracionMonedaPrincipal.seleccionado.MultiplicaODivide === 'D'){
						$scope.data.kngMontoMonedaSecundaria = $scope.data.kngMonto / $scope.data.kngParidadCierre;
					}
					else{
						$scope.data.kngMontoMonedaSecundaria = $scope.data.kngMonto * $scope.data.kngParidadCierre;
					}
				}
				
				if(newValue.ValorParametro === 'ENTREGAMOS'){
					if($scope.data.OperacionSeleccionada.IdTipoOperacion === 'COMFWD'){ 
						$scope.data.kngMontoPago			= $scope.data.kngMontoMonedaSecundaria;
						$scope.data.DecimalesMontoFormaPago	= $scope.data.oConfiguracionMonedaSecundaria.seleccionado.MonedaSecundariaCantidadDecimales;
					}
					else if($scope.data.OperacionSeleccionada.IdTipoOperacion === 'VENFWD'){ 
		    			$scope.data.kngMontoPago			= $scope.data.kngMontoMonedaPrincipal;
		    			$scope.data.DecimalesMontoFormaPago	= $scope.data.oConfiguracionMonedaPrincipal.seleccionado.MonedaPrincipalCantidadDecimales;
		    		}
		    		//$scope.data.FechaMedioPagoDisabled = true;
		    		$scope.HabilitarMontoPago = false;
				}
				else if(newValue.ValorParametro === 'RECIBIMOS'){
					if($scope.data.OperacionSeleccionada.IdTipoOperacion === 'COMFWD'){
		    			$scope.data.kngMontoPago = $scope.data.kngMontoMonedaPrincipal;
		    			$scope.data.DecimalesMontoFormaPago = $scope.data.oConfiguracionMonedaPrincipal.seleccionado.MonedaPrincipalCantidadDecimales;
					}
					else if($scope.data.OperacionSeleccionada.IdTipoOperacion === 'VENFWD'){ 
		    			$scope.data.kngMontoPago = $scope.data.kngMontoMonedaSecundaria;
		    			$scope.data.DecimalesMontoFormaPago = $scope.data.oConfiguracionMonedaSecundaria.seleccionado.MonedaSecundariaCantidadDecimales;
		    		}
		    		//$scope.data.FechaMedioPagoDisabled = false;
		    		$scope.HabilitarMontoPago = true;
				}
				
				if (dsMedioDePago.view().length > 0){
					$scope.data.kngFechaPagoFormaPago = dsMedioDePago.view()[0].FechaPagoMedio;
				}
			}
		});
		
		
		$scope.$watch('ObjSeleccionGrilla.seleccionFormaPagoEnt',function(newval,oldval){
			if(newval === oldval) return;
			
			if($scope.ObjSeleccionGrilla.seleccionFormaPagoEnt !== undefined){
				$scope.data.HabilitarEliminarEnt = true;	
			}	
		});
		
		
		$scope.$watch('ObjSeleccionGrilla.seleccionFormaPagoRec',function(newval,oldval){
			if(newval === oldval) return;
			
			if($scope.ObjSeleccionGrilla.seleccionFormaPagoRec !== undefined){
				$scope.data.HabilitarEliminarRec = true;
			}
		});
		
		
		$scope.data.EliminarMedioDePagoEnt = function(){
			if($scope.ObjSeleccionGrilla.seleccionFormaPagoEnt !== undefined){
				var montosFP = $scope.ObjSeleccionGrilla.seleccionFormaPagoEnt;
				$.each(montosFP,function(key,value){	
					if($scope.data.OperacionSeleccionada.IdTipoOperacion === 'VENFWD')
						$scope.data.kngMontoMonedaPrincipal = ($scope.data.kngMontoMonedaPrincipal||0) + value.Monto;
					else if($scope.data.OperacionSeleccionada.IdTipoOperacion === 'COMFWD')
						$scope.data.kngMontoMonedaSecundaria = ($scope.data.kngMontoMonedaSecundaria||0) + value.Monto;
					
					totalMontoFormaPagoEnt = totalMontoFormaPagoEnt - value.Monto;
				});

				dsMedioDePago.remove($scope.ObjSeleccionGrilla.seleccionFormaPagoEnt);
				$scope.ObjSeleccionGrilla.seleccionFormaPagoEnt = undefined;
			}
			$scope.data.HabilitarEliminarEnt = false;	
		};
		
		
		$scope.data.EliminarMedioDePagoRec = function(){
			if($scope.ObjSeleccionGrilla.seleccionFormaPagoRec !== undefined){
				var montosFP = $scope.ObjSeleccionGrilla.seleccionFormaPagoRec;
				$.each(montosFP,function(key,value){	
					if($scope.data.OperacionSeleccionada.IdTipoOperacion === 'COMFWD')
						$scope.data.kngMontoMonedaPrincipal = ($scope.data.kngMontoMonedaPrincipal||0) + value.Monto;
					else if($scope.data.OperacionSeleccionada.IdTipoOperacion === 'VENFWD')
						$scope.data.kngMontoMonedaSecundaria = ($scope.data.kngMontoMonedaSecundaria||0) + value.Monto;

					totalMontoFormaPagoRec = totalMontoFormaPagoRec - value.Monto;
				});
				
				//totalMontoFormaPagoRec = totalMontoFormaPagoRec - $scope.ObjSeleccionGrilla.seleccionFormaPagoRec.Monto;
				dsMedioDePagoRec.remove($scope.ObjSeleccionGrilla.seleccionFormaPagoRec);
				$scope.ObjSeleccionGrilla.seleccionFormaPagoRec = undefined;
			}		
			$scope.data.HabilitarEliminarRec = false;	
		};
		
		
		/***** Tab Calce *****/
		$scope.data.PresionaEnterGrillaCalce = function(){
			//document.activeElement.blur();
		}
		
		
		$scope.data.BuscoOperacionesCalce = function(){
			if($scope.data.OperacionSeleccionada === undefined) return;
			if($scope.data.oConfiguracionMonedaPrincipal.seleccionado === undefined) return;
			if($scope.data.oConfiguracionMonedaSecundaria.seleccionado === undefined) return;
			if($scope.data.kngMonto === undefined || $scope.data.kngMonto === 0) return;
			if($scope.data.kngPlazo === undefined || $scope.data.kngPlazo === 0) return;
			
			/*REINICIO CAMPOS PARA MANEJAR EL CALCE DE OPERACIONES*/
			totalCalce = 0;
			$scope.data.kngMontoCalzado = undefined;
			$scope.data.kngMontoDiferencia = undefined;
			
			var filter = Array();
			//filter.push({ field: "IdEmpresa"		, operator: "eq", value: $scope.SesionIdEmpresa });
			//filter.push({ field: "IdTipoProducto"	, operator: "eq", value: $scope.data.OperacionSeleccionada.IdTipoProducto });
			filter.push({ field: "Estado",				operator: "eq", value: 'VIG'});
			filter.push({ field: "PlazoResidual",		operator: "eq", value: $scope.data.kngPlazo});
			filter.push({ field: "IdMonedaPrincipal",	operator: "eq", value: $scope.data.oConfiguracionMonedaPrincipal.seleccionado.IdMonedaPrincipal});
			filter.push({ field: "IdMonedaSecundaria",	operator: "eq", value: $scope.data.oConfiguracionMonedaSecundaria.seleccionado.IdMonedaSecundaria});
			filter.push({ field: "ModalidadLiquidacionValor", operator: "eq", value: $scope.data.oConfiguracionModalidadLiquidacion.seleccionado.ValorParametro});
			filter.push({ field: "MontoDisponible",		operator: "gt", value: 0});
			
			if($scope.data.OperacionSeleccionada.IdTipoOperacion === 'COMFWD'){
				filter.push({ field: "IdTipoOperacion"	, operator: "eq", value: 'VENFWD' });	
			}
			else{
				filter.push({ field: "IdTipoOperacion"	, operator: "eq", value: 'COMFWD' });
			}

			dsCarteraCalce.filter(filter);
		};
		
		
		var valMontosCalces = $scope.confBasicaCalce;
		valMontosCalces.save = function(e){
			var dataCalces = dsCarteraCalce.data();
			var FolioCartera = e.model.IdOperacion;
			var montoCalce = 0;
			for(var i=0; i < dataCalces.length; i++){
				if(dataCalces[i].IdOperacion === FolioCartera){
					if(dataCalces[i].MontoDisponible >= e.values.MontoCalce){
						if(dataCalces[i].MontoDisponible > 0){
							montoCalce += e.values.MontoCalce;
							e.model.MontoCalce = e.values.MontoCalce;
						}
						else{
							e.values.MontoCalce = 0;
							e.model.MontoCalce = 0;
							e.preventDefault();
							bcsComunes.msg("error","Error","No se puede asignar Monto Calzar si no hay Monto Disponible.");
							break;
						}
					}
					else{
						e.values.MontoCalce = 0;
						e.model.MontoCalce = 0;
						e.preventDefault();
						bcsComunes.msg("error","Error","No se puede asignar un Monto Calzar mayor al Monto Disponible Calce.");
						break;
					}
				}
				else{
					montoCalce += (dataCalces[i].MontoCalce || 0);
				}
			}
			if(montoCalce > $scope.data.kngMontoCalce){
				montoCalce -= e.values.MontoCalce;
				e.values.MontoCalce = 0;
				e.model.MontoCalce = 0;
				e.preventDefault();
				bcsComunes.msg("error","Error","El monto total de operaciones a calzar no debe supererar el monto de la operación " + ($scope.data.kngMontoCalce) + ".");
			}
			$scope.data.kngMontoCalzado = montoCalce;
			$scope.data.kngMontoDiferencia = (($scope.data.kngMontoCalce || 0) - ($scope.data.kngMontoCalzado || 0));
		};
		
		
		//Tab Parametros Financieros
		$scope.$watch('data.oConfiguracionReferenciaMercado.seleccionado',function(newval,oldval) {
			if(newval === oldval) return;
			
			if($scope.data.oConfiguracionReferenciaMercado.seleccionado){
				$scope.data.kngMontoReferenciaMercado = 0;
				$.each (datosIndicadores,function(index,value){
					if (newval.IdMoneda === value.IdIndicador){
						$scope.data.kngMontoReferenciaMercado = Number(value.TipoCambio);
					}
				});
            }
		}
		,function(result){
			console.log(result);
		});
		
		
		/***** Tab Anticipo *****/
		$scope.$watchGroup(['data.kngTasaDescuentoAnt','data.kngTCAnticipo','data.kngMontoPrincipalAnt'],function(newval,oldval){
			if(oldval[0] === undefined && oldval[1] === undefined && oldval[2] === undefined) return;
			if(newval[0] === undefined || newval[1] === undefined || newval[2] === undefined) return;
			if(newval[0] === oldval[0] || newval[1] === oldval[1] || newval[2] === oldval[2]) return;
			if($scope.data.tipoInstrumentoSeleccionado === undefined || $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === undefined) return;
			
			var varMontoLiquidacion = 0;
			if(modalidadLiquidacionAux === 'COMPENSADA'){
				if(antTipoOperacion === 'COMFWD'){
					if(antNemo === 'ARBITR' && antMultiplicaODivide === 'D'){
						var parte1 = $scope.data.kngTCAnticipo ? ($scope.data.kngMontoPrincipalAnt / $scope.data.kngTCAnticipo) : 0;
						var parte2 = antTCParCierre ? ($scope.data.kngMontoPrincipalAnt / antTCParCierre) : 0;
						varMontoLiquidacion = (parte1 - parte2) * factorArbitrajeTipoCambioDO;
					}
					else if (antNemo === 'SECARE') {
						varMontoLiquidacion = $scope.data.kngMontoPrincipalAnt * ($scope.data.kngTCAnticipo - antTCParCierre) * $scope.data.kngMontoUf;
					}
					else{
						varMontoLiquidacion = $scope.data.kngMontoPrincipalAnt * ($scope.data.kngTCAnticipo - antTCParCierre) * factorArbitrajeTipoCambioDO;
					}
				}
				else if(antTipoOperacion === 'VENFWD'){
					if(antNemo === 'ARBITR' && antMultiplicaODivide === 'D'){
						var parte1 = antTCParCierre ? ($scope.data.kngMontoPrincipalAnt / antTCParCierre) : 0;
						var parte2 = $scope.data.kngTCAnticipo ? ($scope.data.kngMontoPrincipalAnt / $scope.data.kngTCAnticipo) : 0;
						varMontoLiquidacion = (parte1 - parte2) * factorArbitrajeTipoCambioDO;
					}
					else if (antNemo === 'SECARE') {
						varMontoLiquidacion = $scope.data.kngMontoPrincipalAnt * (antTCParCierre - $scope.data.kngTCAnticipo) * $scope.data.kngMontoUf;
					}
					else{
						varMontoLiquidacion = $scope.data.kngMontoPrincipalAnt * (antTCParCierre - $scope.data.kngTCAnticipo) * factorArbitrajeTipoCambioDO;
					}
				}
			}
			else if(modalidadLiquidacionAux === 'ENTREGA_FISICA'){
				if(antNemo === 'ARBITR' && antMultiplicaODivide === 'D'){
					varMontoLiquidacion	= $scope.data.kngMontoPrincipalAnt / antTCParCierre * factorArbitrajeTipoCambioDO;
				}
				else{
					varMontoLiquidacion	= $scope.data.kngMontoPrincipalAnt * antTCParCierre * factorArbitrajeTipoCambioDO;
				}
			}
			var varCalculoAnticipo = 1 + (($scope.data.kngTasaDescuentoAnt/100) * (antPlazoResidual/360));
			
			$scope.data.kngMontoSecundarioAnt	= varMontoLiquidacion;
			$scope.data.kngMontoRecibirAnt		= varMontoLiquidacion / varCalculoAnticipo;
		});
		
		
		/********************************************************************************************/
		/********************************* INGRESO DE OPERACION FORWARD******************************/
		/********************************************************************************************/
		var IngresoForward = function(varEstadoOperacion){ 
			if(!$scope.data.tipoInstrumentoSeleccionado){
				bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('UI_DEBE_SELECCIONAR_TIPO_INSTRUMENTO'));
				return;
			}
			if($scope.data.oConfiguracionModalidadLiquidacion.seleccionado === undefined || $scope.data.oConfiguracionModalidadLiquidacion.seleccionado === false){
				bcsComunes.msg("warning","Informacion","Debe seleccionar una Modalidad de Liquidación.");
				return;
			}
			if($scope.data.oClientePortafolio.seleccionado.IdCliente === undefined){
				bcsComunes.msg("warning","Informacion","Debe seleccionar un Cliente.");
				return;
			}
			if($scope.data.oClientePortafolio.seleccionado.IdPortafolio === undefined){
				bcsComunes.msg("warning","Informacion","Debe seleccionar un Portafolio.");
				return;
			}

			var msjErrorFormaPago = '';
			var entraError = false;
			if($scope.data.oConfiguracionModalidadLiquidacion.seleccionado.ValorParametro === 'ENTREGA_FISICA'){
				if($scope.data.formaDePagoEntregamosTotal===0/* && !$scope.data.operaCuentaInversion*/){
					bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('FORMA_DE_PAGO_ENTREGAMOS_INCOMPLETO'));
					return;
				}
				if($scope.data.formaDePagoRecibimosTotal===0/* && !$scope.data.operaCuentaInversion*/){
					bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('FORMA_DE_PAGO_RECIBIMOS_INCOMPLETO'));
					return;
				}
				
				if ($scope.data.OperacionSeleccionada.IdTipoOperacion === 'COMFWD'/* && !$scope.data.operaCuentaInversion*/){
					if($scope.data.formaDePagoEntregamosTotal < ($scope.data.kngMontoMonedaSecundaria||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_ENTREGAMOS_INCOMPLETO'; entraError = true;}
	    			if($scope.data.formaDePagoEntregamosTotal > ($scope.data.kngMontoMonedaSecundaria||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_ENTREGAMOS_EXCEDE'; entraError = true;}
	    			if($scope.data.formaDePagoRecibimosTotal < ($scope.data.kngMontoMonedaPrincipal||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_RECIBIMOS_INCOMPLETO'; entraError = true;}
	    			if($scope.data.formaDePagoRecibimosTotal > ($scope.data.kngMontoMonedaPrincipal||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_RECIBIMOS_EXCEDE'; entraError = true;}
	    			
	    			if(entraError){
	    				bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant(msjErrorFormaPago));
	    				return;
	    			}
	    		}
	    		else if($scope.data.OperacionSeleccionada.IdTipoOperacion === 'VENFWD'/* && !$scope.data.operaCuentaInversion*/){
	    			if($scope.data.formaDePagoEntregamosTotal < ($scope.data.kngMontoMonedaPrincipal||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_ENTREGAMOS_INCOMPLETO'; entraError = true;}
	    			if($scope.data.formaDePagoEntregamosTotal > ($scope.data.kngMontoMonedaPrincipal||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_ENTREGAMOS_EXCEDE'; entraError = true;}
	    			if($scope.data.formaDePagoRecibimosTotal < ($scope.data.kngMontoMonedaSecundaria||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_RECIBIMOS_INCOMPLETO'; entraError = true;}
	    			if($scope.data.formaDePagoRecibimosTotal > ($scope.data.kngMontoMonedaSecundaria||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_RECIBIMOS_EXCEDE'; entraError = true;}
	    			
	    			if(entraError){
	    				bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant(msjErrorFormaPago));
	    				return;
	    			}
	    		}
			}
			else if($scope.data.oConfiguracionModalidadLiquidacion.seleccionado.ValorParametro === 'COMPENSADA'){
				if($scope.data.oConfiguracionFixing.seleccionado === undefined){
					bcsComunes.msg("warning","Informacion","Falta Seleccionar el Fixing de la Operación.");
					return;
				}
				if($scope.data.kngFechaFixing === undefined && $scope.data.oConfiguracionFixing.seleccionado.NombreParametro !== 'BANCO CENTRAL'){
					bcsComunes.msg("warning","Informacion","Si la Modalidad de Liquidación es Compensada debe agregar Fecha Fixing.");
					return;
				}
				//if($scope.data.kngHoraFixing === undefined && $scope.data.oConfiguracionFixing.seleccionado.NombreParametro !== 'BANCO CENTRAL'){
				if(!$scope.data.HoraFixing && $scope.data.oConfiguracionFixing.seleccionado.NombreParametro !== 'BANCO CENTRAL'){
					bcsComunes.msg("warning","Informacion","Si la Modalidad de Liquidación es Compensada debe agregar Hora Fixing.");
					return;
				}

				if($scope.data.OperacionSeleccionada.IdTipoOperacion === 'VENFWD' && $scope.data.formaDePagoEntregamosTotal===0/* && !$scope.data.operaCuentaInversion*/){
					bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('FORMA_DE_PAGO_ENTREGAMOS_INCOMPLETO'));
					return;
				}
				if($scope.data.OperacionSeleccionada.IdTipoOperacion === 'COMFWD' && $scope.data.formaDePagoRecibimosTotal===0/* && !$scope.data.operaCuentaInversion*/){
					bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('FORMA_DE_PAGO_RECIBIMOS_INCOMPLETO'));
					return;
				}
				
				if ($scope.data.OperacionSeleccionada.IdTipoOperacion === 'COMFWD'/* && !$scope.data.operaCuentaInversion*/){
					if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SEINFL'){
						if($scope.data.formaDePagoRecibimosTotal < ($scope.data.kngMontoMonedaSecundaria||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_RECIBIMOS_INCOMPLETO'; entraError = true;}
		    			if($scope.data.formaDePagoRecibimosTotal > ($scope.data.kngMontoMonedaSecundaria||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_RECIBIMOS_EXCEDE'; entraError = true;}
					}
					else{
						if($scope.data.formaDePagoRecibimosTotal < ($scope.data.kngMontoMonedaPrincipal||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_RECIBIMOS_INCOMPLETO'; entraError = true;}
		    			if($scope.data.formaDePagoRecibimosTotal > ($scope.data.kngMontoMonedaPrincipal||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_RECIBIMOS_EXCEDE'; entraError = true;}
					}
					
	    			if(entraError){
	    				bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant(msjErrorFormaPago));
	    				return;
	    			}
	    		}
	    		else if($scope.data.OperacionSeleccionada.IdTipoOperacion === 'VENFWD'/* && !$scope.data.operaCuentaInversion*/){
	    			if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SEINFL'){
						if($scope.data.formaDePagoEntregamosTotal < ($scope.data.kngMontoMonedaSecundaria||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_RECIBIMOS_INCOMPLETO'; entraError = true;}
		    			if($scope.data.formaDePagoEntregamosTotal > ($scope.data.kngMontoMonedaSecundaria||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_RECIBIMOS_EXCEDE'; entraError = true;}
					}
					else{
						if($scope.data.formaDePagoEntregamosTotal < ($scope.data.kngMontoMonedaPrincipal||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_ENTREGAMOS_INCOMPLETO'; entraError = true;}
		    			if($scope.data.formaDePagoEntregamosTotal > ($scope.data.kngMontoMonedaPrincipal||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_ENTREGAMOS_EXCEDE'; entraError = true;}
					}
	    			
	    			if(entraError){
	    				bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant(msjErrorFormaPago));
	    				return;
	    			}
	    		}
			}

			if(!$scope.data.kngTCCosto){
				bcsComunes.msg("warning","Informacion","Debe Ingresar un Tipo Cambio Costo.");
				return;
			}
			if(!$scope.data.kngTCCierre && $scope.data.tipoInstrumentoSeleccionado.Nemotecnico !== 'ARBITR'){
				bcsComunes.msg("warning","Informacion",$translate.instant('TC_CIERRE_MAYOR_CERO'));
				return;
			}				
			if($scope.data.ChkPuntosForward){
				if(!$scope.data.kngTCForward){
					bcsComunes.msg("warning","Informacion","Debe Ingresar un Tipo Cambio Forward.");
					return;
				}
	    	}
			else{
	    		if(!$scope.data.kngTasaMonedaPrincipal){
					bcsComunes.msg("warning","Informacion","Debe Ingresar una Tasa para la Moneda Principal.");
					return;
				}
				if(!$scope.data.kngTasaMonedaSecundaria){
					bcsComunes.msg("warning","Informacion","Debe Ingresar una Tasa para la Moneda Secundaria.");
					return;
				}
	    	}

			if(!$scope.data.kngMontoDiferido){
				bcsComunes.msg("warning","Informacion","Falta calcular el Monto Diferido.");
				//return;
			}
			if(!$scope.data.kngMargen){
				bcsComunes.msg("warning","Informacion","Falta calcular el Monto Margen.");
				//return;
			}
			
			var tablaDatosFormaPago	= [];
			var i=0;
			$.each($scope.data.formaDePagoEntregamos,function(key,value) {
				if (value > 0){
					var idComprobantePagoOperacion = 0;
					$.each($scope.data.formasDePago,function(keyF,valueF) {
						if (valueF.RecibePagaPortafolio === 'P' && valueF.IdFormaDePago === key)
							idComprobantePagoOperacion = valueF.IdComprobantePagoOperacion
					});
					tablaDatosFormaPago.push({
						 "IdInternoPagoOperacion"		: ++i
						,"IdComprobantePagoOperacion"	: idComprobantePagoOperacion
						,"IdTipoMovimiento"				: $scope.data.OperacionIdActual==='COMFWD'?'MOVING':'MOVEGR'
						,"IdFormaDePago"				: key
						,"IdOperacion"					: Number(1)
						,"RecibePagaPortafolio"			: 'P'
						,"IdMoneda"						: $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SEINFL' || $scope.data.OperacionIdActual==='COMFWD' ? $scope.data.MonedaSecundariaIdActual : $scope.data.MonedaPrincipalIdActual
						,"IdMonedaLiquidacion"			: $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SEINFL' || $scope.data.OperacionIdActual==='COMFWD' ? $scope.data.MonedaSecundariaIdActual : $scope.data.MonedaPrincipalIdActual
						,"MontoFormaDePago"				: value
						,"AfectarTesoreria"				: key.includes("_CIN_")?'N':'S'
					});
				}
			});
			$.each($scope.data.formaDePagoRecibimos,function(key,value) {
				if (value > 0){
					var idComprobantePagoOperacion = 0;
					$.each($scope.data.formasDePago,function(keyF,valueF) {
						if (valueF.RecibePagaPortafolio === 'R' && valueF.IdFormaDePago === key)
							idComprobantePagoOperacion = valueF.IdComprobantePagoOperacion
					});
					tablaDatosFormaPago.push({
						 "IdInternoPagoOperacion"		: ++i
						,"IdComprobantePagoOperacion"	: idComprobantePagoOperacion
						,"IdTipoMovimiento"				: $scope.data.OperacionIdActual==='COMFWD'?'MOVEGR':'MOVING'
						,"IdFormaDePago"				: key
						,"IdOperacion"					: Number(1)
						,"RecibePagaPortafolio"			: 'R'
						,"IdMoneda"						: $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SEINFL' || $scope.data.OperacionIdActual==='VENFWD' ? $scope.data.MonedaSecundariaIdActual : $scope.data.MonedaPrincipalIdActual
						,"IdMonedaLiquidacion"			: $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SEINFL' || $scope.data.OperacionIdActual==='VENFWD' ? $scope.data.MonedaSecundariaIdActual : $scope.data.MonedaPrincipalIdActual
						,"MontoFormaDePago"				: value
						,"AfectarTesoreria"				: key.includes("_CIN_")?'N':'S'
					});
				}
			});
			
			var tablaOperacionesCalce	= [];
			var CalceOperacionesForward = dsCarteraCalce.data() || [];
			var montoTotalCalce = 0;
			$.each(CalceOperacionesForward,function(key,value){	
				if (value.MontoCalce > 0){
					var ObjCalce = {
					 	"FolioInterno" 			: 1,
					 	"IdOperacion"			: 0,
					 	"FolioCartera"			: value.IdOperacion,
       				    "IdTipoOperacion" 		: value.IdTipoOperacion,
       				    "IdMonedaPrincipal"		: value.IdMonedaPrincipal,
       				    "MontoOperacion"		: value.INGMontoMonedaPrincipal,
						"MontoDisponible"		: value.MontoDisponible,
						"MontoCalce"			: value.MontoCalce
					};
					tablaOperacionesCalce.push(ObjCalce);
					montoTotalCalce = montoTotalCalce + value.MontoCalce;
				}
			});	
			
			if(montoTotalCalce > $scope.data.kngMonto){
				bcsComunes.msg("error","Error","No se puede asignar un Monto Calzar mayor al Monto Disponible Calce.");
				return;
			}
			
			var datosIngresoForward = {
				p_IdEmpresa 		: $scope.SesionIdEmpresa, //saca de variables de entorno
				p_IdPais			: $scope.SesionIdPaisSistema,
				p_IdUsuario 		: $scope.SesionIdUsuario, //saca de variables de entorno
				p_IdBolsa			: $scope.SesionBolsaDefectoSistema,
				p_IdSucursal		: 0,
				p_IdModulo 			: 'MFW',
				p_UsaReglasNegocio 	: 'S',
				PagaConCtaInv			: $scope.data.operaCuentaInversion?'S':'N',
				p_tablaForward : [{
					FolioInterno			: 1,
					IdTipoOperacion       	: $scope.data.OperacionSeleccionada.IdTipoOperacion,
					IdTipoInstrumento       : $scope.data.tipoInstrumentoSeleccionado.IdTipoInstrumento,
					IdTipoProducto			: $scope.data.tipoInstrumentoSeleccionado.IdTipoProducto,
					IdInstrumento       	: $scope.data.tipoInstrumentoSeleccionado.IdInstrumento,
					Nemotecnico       		: $scope.data.tipoInstrumentoSeleccionado.Nemotecnico,
					IdCliente               : $scope.data.oClientePortafolio.seleccionado.IdCliente,
					IdPortafolio            : $scope.data.oClientePortafolio.seleccionado.IdPortafolio,
					IdPersonaEjecutivo		: $scope.data.oClientePortafolio.seleccionado.IdPersonaEjecutivo,
					IdTipoCanalIngreso		: 'MES',
					IdMonedaPrincipal       : $scope.data.oConfiguracionMonedaPrincipal.seleccionado.IdMonedaPrincipal,
					IdMonedaSecundaria      : $scope.data.oConfiguracionMonedaSecundaria.seleccionado.IdMonedaSecundaria,
					MontoOperacion          : $scope.data.kngMonto || 0,
					MontoInicial            : $scope.data.kngMontoInicial || 0,
					MontoFinal              : $scope.data.kngMontoFinal || 0,
					FechaOperacion          : util.dateSPdate($scope.data.kngFechaOperacion),
					TCCierre                : $scope.data.kngTCCierre || 0,
					ParidadCierre           : $scope.data.kngParidadCierre || 0,
					Plazo                   : $scope.data.kngPlazo || 0,
					FechaVencimiento        : ($scope.data.kngFechaVencimiento !== undefined) ? util.dateSPdate($scope.data.kngFechaVencimiento) : '0001-01-01',
					FechaPago               : ($scope.data.kngFechaPago !== undefined) ? util.dateSPdate($scope.data.kngFechaPago) : '0001-01-01',
					IdModalidadLiquidacion  : $scope.data.oConfiguracionModalidadLiquidacion.seleccionado.IdParametro || 0,
					IdReferenciaMercado     : 0,
					IdPlaza                 : $scope.data.PlazaSeleccionado.IdCiudad,
					TCCosto                 : $scope.data.kngTCCosto || 0,
					PuntosTasasForward		: ($scope.data.ChkPuntosForward === true ? 'S':'N'),
					PuntosForward           : $scope.data.kngPuntosForward || 0,
					TCForward               : $scope.data.kngTCForward || 0,
					TasasMonedaPrincipal    : $scope.data.kngTasaMonedaPrincipal || 0,
					TasasMonedaSecundaria	: $scope.data.kngTasaMonedaSecundaria || 0,
					MontoDiferido           : $scope.data.kngMontoDiferido || 0,
					MontoMargen             : $scope.data.kngMargen || 0,
					IdFixing                : $scope.data.oConfiguracionFixing.seleccionado.IdParametro || 0,
					FechaFixing             : ($scope.data.kngFechaFixing !== undefined && $scope.data.kngFechaFixing !== null) ? util.dateSPdate($scope.data.kngFechaFixing) : '0001-01-01',
					HoraFixing              : $scope.data.HoraFixing ? util.formatoTime(new Date($scope.SesionFechaSistema + " " + $scope.data.HoraFixing)) : '00:00:00',
					IdMesa                  : $scope.data.oConfiguracionMesa.seleccionado.IdMesa || 0,
					IdSpotAsociado          : $scope.data.kngAsociadaSpot || 0,
					IdClasificacionCartera  : $scope.data.oConfiguracionClasificacionCartera.seleccionado.IdParametro || 0,
					Observaciones           : $scope.data.ngComentario || '',
					IdEstado				: varEstadoOperacion, 
					IdContacto				: $scope.data.oConfiguracionContactos.seleccionado.IdPersona || 0 ,
					TipoDocumento			: $scope.data.oConfiguracionDocumentos.idActual || "",
					IdMonedaCompensacion	: $scope.data.oConfiguracionModalidadLiquidacion.seleccionado.ValorParametro === 'COMPENSADA' ? $scope.data.MonedaCompensacionIdActual : ''
				}],
				p_DatosFormaPago : tablaDatosFormaPago,
				p_tablaCalce	 : tablaOperacionesCalce
			};
			
			bcsComunes.ventanaOverlay(idVentana,true);
			bcsServiciosMesa.IngresoForward(datosIngresoForward).$promise.then(function(respuesta){
		   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		   		
		   		var mensaje = '';
				if (respuesta.P_OUT_TBLIDFORWARD[0] !== undefined && respuesta.P_OUT_ERRORESCONTADOR === 0){
					fnConsultarConfiguracionMesa();
					if(respuesta.P_OUT_TBLIDFORWARD[0].IdEstado === 'VIG'){
						mensaje = "Operación ingresada: " + respuesta.P_OUT_TBLIDFORWARD[0].FolioCartera;
						ingresar++;
					}
					else if(respuesta.P_OUT_TBLIDFORWARD[0].IdEstado === 'PDA'){
						mensaje = "Operación ingresada Pendiente de límites: " + respuesta.P_OUT_TBLIDFORWARD[0].FolioCartera;
					}
					else{
						mensaje = "Operación ingresada Pendiente: " + respuesta.P_OUT_TBLIDFORWARD[0].FolioCartera;
					}
					bcsComunes.ventanaMsg({ mensaje:$translate.instant(mensaje), conflictos:util.parseServerResponse(respuesta)},idVentana);
				}
				else{
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("Existen problemas al ingresar la operación"),
						conflictos:util.parseServerResponse(respuesta)
					},idVentana);
			    }
				bcsComunes.ventanaOverlay(idVentana,false);
	   			
	   		},function(respuesta){
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("Existen problemas al ingresar la operación"),
					conflictos:util.parseServerResponse(respuesta)
				},idVentana);
	   		});	
	   	};
	   	
	   	
	   	/********************************************************************************************/
		/******************************* MODIFICAR OPERACION FORWARD ********************************/
		/********************************************************************************************/
		var ModificarForward = function(){
			if($scope.parametros.Opcion !== "MODIFICAR" && $scope.parametros.Opcion !== "MODIFICARPAGO") return;
			
			//IMPCREDOPT-2801 se deja mensaje bloqueante al intentar modificar operacion forward con calce
			if($scope.confBasicaCalce.dataSource.view().length > 0 || varOperacionConCalce){
				var msg = "No es posible Modificar una Operación calzada. Para esto, en el blotter Calce de Operaciones se debe Descalzar.";
				var respuesta = {};
				respuesta.P_OUT_ERRORESCONTADOR = 1;
				var idPopup = bcsComunes.ventanaMsg({mensaje:msg,conflictos:util.parseServerResponse(respuesta)},idVentana);
				if(idPopup){
					var promesa = bcsComunes.ventanaPromesa(idPopup);
					$.when(promesa).then(function(data){
						bcsComunes.ventanaOverlay(idVentana,false);
						bcsComunes.ventanaCerrar(idVentana);
					});
				}
				else{
					bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.ventanaCerrar(idVentana);
				}
			}
			else{
				if(!$scope.data.tipoInstrumentoSeleccionado){
					bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('UI_DEBE_SELECCIONAR_TIPO_INSTRUMENTO')); return;
				}
				if($scope.data.oConfiguracionModalidadLiquidacion.seleccionado === undefined || $scope.data.oConfiguracionModalidadLiquidacion.seleccionado === false){
					bcsComunes.msg("warning","Informacion","Debe seleccionar una Modalidad de Liquidación."); return;
				}
				if($scope.data.oClientePortafolio.seleccionado.IdCliente === undefined){
					bcsComunes.msg("warning","Informacion","Debe seleccionar un Cliente."); return;
				}
				if($scope.data.oClientePortafolio.seleccionado.IdPortafolio === undefined){
					bcsComunes.msg("warning","Informacion","Debe seleccionar un Portafolio."); return;
				}
				
				var msjErrorFormaPago = '';
				var entraError = false;
				if($scope.data.oConfiguracionModalidadLiquidacion.seleccionado.ValorParametro === 'ENTREGA_FISICA'){
					if($scope.data.formaDePagoEntregamosTotal === 0){
						bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('FORMA_DE_PAGO_ENTREGAMOS_INCOMPLETO'));
						return;
					}
					if($scope.data.formaDePagoRecibimosTotal === 0){
						bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('FORMA_DE_PAGO_RECIBIMOS_INCOMPLETO'));
						return;
					}
					
					if ($scope.data.OperacionSeleccionada.IdTipoOperacion === 'COMFWD'){
						if($scope.data.formaDePagoEntregamosTotal < ($scope.data.kngMontoMonedaSecundaria||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_ENTREGAMOS_INCOMPLETO'; entraError = true;}
		    			if($scope.data.formaDePagoEntregamosTotal > ($scope.data.kngMontoMonedaSecundaria||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_ENTREGAMOS_EXCEDE'; entraError = true;}
		    			if($scope.data.formaDePagoRecibimosTotal < ($scope.data.kngMontoMonedaPrincipal||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_RECIBIMOS_INCOMPLETO'; entraError = true;}
		    			if($scope.data.formaDePagoRecibimosTotal > ($scope.data.kngMontoMonedaPrincipal||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_RECIBIMOS_EXCEDE'; entraError = true;}
		    			
		    			if(entraError){
		    				bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant(msjErrorFormaPago));
		    				return;
		    			}
		    		}
		    		else if($scope.data.OperacionSeleccionada.IdTipoOperacion === 'VENFWD'){
		    			if($scope.data.formaDePagoEntregamosTotal < ($scope.data.kngMontoMonedaPrincipal||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_ENTREGAMOS_INCOMPLETO'; entraError = true;}
		    			if($scope.data.formaDePagoEntregamosTotal > ($scope.data.kngMontoMonedaPrincipal||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_ENTREGAMOS_EXCEDE'; entraError = true;}
		    			if($scope.data.formaDePagoRecibimosTotal < ($scope.data.kngMontoMonedaSecundaria||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_RECIBIMOS_INCOMPLETO'; entraError = true;}
		    			if($scope.data.formaDePagoRecibimosTotal > ($scope.data.kngMontoMonedaSecundaria||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_RECIBIMOS_EXCEDE'; entraError = true;}
		    			
		    			if(entraError){
		    				bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant(msjErrorFormaPago));
		    				return;
		    			}
		    		}
				}
				else if($scope.data.oConfiguracionModalidadLiquidacion.seleccionado.ValorParametro === 'COMPENSADA'){
					if($scope.data.oConfiguracionFixing.seleccionado === undefined){
						bcsComunes.msg("warning","Informacion","Falta Seleccionar el Fixing de la Operación."); return;
					}
					if($scope.data.kngFechaFixing === undefined && $scope.data.oConfiguracionFixing.seleccionado.NombreParametro !== 'BANCO CENTRAL'){
						bcsComunes.msg("warning","Informacion","Si la Modalidad de Liquidación es Compensada debe agregar Fecha Fixing."); return;
					}
					if(!$scope.data.HoraFixing && $scope.data.oConfiguracionFixing.seleccionado.NombreParametro !== 'BANCO CENTRAL'){
						bcsComunes.msg("warning","Informacion","Si la Modalidad de Liquidación es Compensada debe agregar Hora Fixing."); return;
					}
					
					if($scope.data.OperacionSeleccionada.IdTipoOperacion === 'VENFWD' && $scope.data.formaDePagoEntregamosTotal === 0){
						bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('FORMA_DE_PAGO_ENTREGAMOS_INCOMPLETO'));
						return;
					}
					if($scope.data.OperacionSeleccionada.IdTipoOperacion === 'COMFWD' && $scope.data.formaDePagoRecibimosTotal === 0){
						bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('FORMA_DE_PAGO_RECIBIMOS_INCOMPLETO'));
						return;
					}
					
					if ($scope.data.OperacionSeleccionada.IdTipoOperacion === 'COMFWD'){
						if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SEINFL'){
							if($scope.data.formaDePagoRecibimosTotal < ($scope.data.kngMontoMonedaSecundaria||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_RECIBIMOS_INCOMPLETO'; entraError = true;}
			    			if($scope.data.formaDePagoRecibimosTotal > ($scope.data.kngMontoMonedaSecundaria||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_RECIBIMOS_EXCEDE'; entraError = true;}
						}
						else{
							if($scope.data.formaDePagoRecibimosTotal < ($scope.data.kngMontoMonedaPrincipal||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_RECIBIMOS_INCOMPLETO'; entraError = true;}
			    			if($scope.data.formaDePagoRecibimosTotal > ($scope.data.kngMontoMonedaPrincipal||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_RECIBIMOS_EXCEDE'; entraError = true;}
						}
						
		    			if(entraError){
		    				bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant(msjErrorFormaPago));
		    				return;
		    			}
		    		}
		    		else if($scope.data.OperacionSeleccionada.IdTipoOperacion === 'VENFWD'){
		    			if($scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SEINFL'){
							if($scope.data.formaDePagoEntregamosTotal < ($scope.data.kngMontoMonedaSecundaria||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_RECIBIMOS_INCOMPLETO'; entraError = true;}
			    			if($scope.data.formaDePagoEntregamosTotal > ($scope.data.kngMontoMonedaSecundaria||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_RECIBIMOS_EXCEDE'; entraError = true;}
						}
						else{
							if($scope.data.formaDePagoEntregamosTotal < ($scope.data.kngMontoMonedaPrincipal||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_ENTREGAMOS_INCOMPLETO'; entraError = true;}
			    			if($scope.data.formaDePagoEntregamosTotal > ($scope.data.kngMontoMonedaPrincipal||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_ENTREGAMOS_EXCEDE'; entraError = true;}
						}
		    			
		    			if(entraError){
		    				bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant(msjErrorFormaPago));
		    				return;
		    			}
		    		}
				}
				
				if(!$scope.data.kngTCCosto){
					bcsComunes.msg("warning","Informacion","Debe Ingresar un Tipo Cambio Costo."); return;
				}
				if($scope.data.ChkPuntosForward){
					if(!$scope.data.kngTCForward){
						bcsComunes.msg("warning","Informacion","Debe Ingresar un Tipo Cambio Forward."); return;
					}
		    	}
				else{
		    		if(!$scope.data.kngTasaMonedaPrincipal){
						bcsComunes.msg("warning","Informacion","Debe Ingresar una Tasa para la Moneda Principal."); return;
					}
					if(!$scope.data.kngTasaMonedaSecundaria){
						bcsComunes.msg("warning","Informacion","Debe Ingresar una Tasa para la Moneda Secundaria."); return;
					}
		    	}
				
				if(!$scope.data.kngMontoDiferido){
					bcsComunes.msg("warning","Informacion","Falta calcular el Monto Diferido.");
				}
				if(!$scope.data.kngMargen){
					bcsComunes.msg("warning","Informacion","Falta calcular el Monto Margen.");
				}
				
				var tablaDatosFormaPago	= [];
				var i=0;
				$.each($scope.data.formaDePagoEntregamos,function(key,value) {
					if (value > 0){
						var idComprobantePagoOperacion = 0;
						$.each($scope.data.formasDePago,function(keyF,valueF) {
							if (valueF.RecibePagaPortafolio === 'P' && valueF.IdFormaDePago === key)
								idComprobantePagoOperacion = valueF.IdComprobantePagoOperacion
						});
						tablaDatosFormaPago.push({
							 "IdInternoPagoOperacion"		: ++i
							,"IdComprobantePagoOperacion"	: idComprobantePagoOperacion
							,"IdTipoMovimiento"          	: $scope.data.OperacionIdActual==='COMFWD'?'MOVING':'MOVEGR'
							,"IdFormaDePago"				: key
							,"IdOperacion"					: $scope.parametros.IdOperacion
							,"RecibePagaPortafolio"			: 'P'
							,"IdMoneda"						: $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SEINFL' || $scope.data.OperacionIdActual==='COMFWD' ? $scope.data.MonedaSecundariaIdActual : $scope.data.MonedaPrincipalIdActual
							,"IdMonedaLiquidacion"			: $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SEINFL' || $scope.data.OperacionIdActual==='COMFWD' ? $scope.data.MonedaSecundariaIdActual : $scope.data.MonedaPrincipalIdActual
							,"MontoFormaDePago"				: value
							,"AfectarTesoreria"				: key.includes("_CIN_")?'N':'S'
						});
					}
				});
				$.each($scope.data.formaDePagoRecibimos,function(key,value) {
					if (value > 0){
						var idComprobantePagoOperacion = 0;
						$.each($scope.data.formasDePago,function(keyF,valueF) {
							if (valueF.RecibePagaPortafolio === 'R' && valueF.IdFormaDePago === key)
								idComprobantePagoOperacion = valueF.IdComprobantePagoOperacion
						});
						tablaDatosFormaPago.push({
							 "IdInternoPagoOperacion"		: ++i
							,"IdComprobantePagoOperacion"	: idComprobantePagoOperacion
							,"IdTipoMovimiento"				: $scope.data.OperacionIdActual==='COMFWD'?'MOVEGR':'MOVING'
							,"IdFormaDePago"				: key
							,"IdOperacion"					: $scope.parametros.IdOperacion
							,"RecibePagaPortafolio"			: 'R'
							,"IdMoneda"						: $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SEINFL' || $scope.data.OperacionIdActual==='VENFWD' ? $scope.data.MonedaSecundariaIdActual : $scope.data.MonedaPrincipalIdActual
							,"IdMonedaLiquidacion"			: $scope.data.tipoInstrumentoSeleccionado.Nemotecnico === 'SEINFL' || $scope.data.OperacionIdActual==='VENFWD' ? $scope.data.MonedaSecundariaIdActual : $scope.data.MonedaPrincipalIdActual
							,"MontoFormaDePago"				: value
							,"AfectarTesoreria"				: key.includes("_CIN_")?'N':'S'
						});
					}
				});
				
				var datosModificacionForward = {
					p_IdEmpresa 		: $scope.SesionIdEmpresa, //saca de variables de entorno
					p_IdPais			: $scope.SesionIdPaisSistema,
					p_IdUsuario 		: $scope.SesionIdUsuario, //saca de variables de entorno
					p_IdBolsa			: $scope.SesionBolsaDefectoSistema,
					p_IdSucursal		: 0,
					p_IdModulo 			: 'MFW',
					p_UsaReglasNegocio 	: 'S',
					PagaConCtaInv			: $scope.data.operaCuentaInversion?'S':'N',
					p_tablaForward    	: [{
					  	IdOperacion	 			: IdOperacion,
					  	IdTipoProducto			: $scope.data.tipoInstrumentoSeleccionado.IdTipoProducto,
					  	FolioCartera 			: $scope.parametros.IdCartera,
					  	IdCliente               : $scope.data.oClientePortafolio.seleccionado.IdCliente,
						IdPortafolio            : $scope.data.oClientePortafolio.seleccionado.IdPortafolio,
						IdPersonaEjecutivo		: $scope.data.oClientePortafolio.seleccionado.IdPersonaEjecutivo,
						MontoOperacion 			: $scope.data.kngMonto || 0,
						MontoOperacionInicial	: $scope.data.kngMontoInicial || 0,
						MontoOperacionFinal		: $scope.data.kngMontoFinal || 0,
						TCCierre				: $scope.data.kngTCCierre || 0,
						ParidadCierre			: $scope.data.kngParidadCierre || 0,
						Plazo                   : $scope.data.kngPlazo || 0,
						FechaVencimiento        : $scope.data.kngFechaVencimiento ? util.dateSPdate($scope.data.kngFechaVencimiento) : '0001-01-01',
						FechaPago               : $scope.data.kngFechaPago ? util.dateSPdate($scope.data.kngFechaPago) : $scope.data.oConfiguracionModalidadLiquidacion.seleccionado.ValorParametro === 'ENTREGA_FISICA' ? $scope.data.kngFechaVencimiento ? util.dateSPdate($scope.data.kngFechaVencimiento) : '0001-01-01' : '0001-01-01',
						IdModalidadLiquidacion  : $scope.data.oConfiguracionModalidadLiquidacion.seleccionado.IdParametro || 0,
						TCCosto					: $scope.data.kngTCCosto || 0,
						PuntosTasasForward		: ($scope.data.ChkPuntosForward === true ? 'S':'N'),
						PuntosForward			: $scope.data.kngPuntosForward || 0,
						TCForward				: $scope.data.kngTCForward || 0,
						TasasMonedaPrincipal	: $scope.data.kngTasaMonedaPrincipal || 0,
						TasasMonedaSecundaria	: $scope.data.kngTasaMonedaSecundaria || 0,
						MontoDiferido			: $scope.data.kngMontoDiferido || 0,
						MontoMargen				: $scope.data.kngMargen || 0,
						IdFixing                : $scope.data.oConfiguracionFixing.seleccionado.IdParametro || 0,
						FechaFixing             : $scope.data.kngFechaFixing ? typeof $scope.data.kngFechaFixing !== 'string' ? util.dateSPdate($scope.data.kngFechaFixing) : $scope.data.kngFechaFixing : '0001-01-01',
						HoraFixing              : $scope.data.HoraFixing ? util.formatoTime(new Date($scope.SesionFechaSistema + " " + $scope.data.HoraFixing)) : '00:00:00',
						IdContacto				: $scope.data.oConfiguracionContactos.seleccionado.IdPersona || 0 ,
						TipoDocumento			: $scope.data.oConfiguracionDocumentos.idActual || "",
						IdMonedaCompensacion	: $scope.data.oConfiguracionModalidadLiquidacion.seleccionado.ValorParametro === 'COMPENSADA' ? $scope.data.MonedaCompensacionIdActual == "" ? "CLP" : $scope.data.MonedaCompensacionIdActual : ''
					}],
					p_DatosFormaPago : tablaDatosFormaPago,
				};
				
				bcsComunes.ventanaOverlay(idVentana,true);
				bcsServiciosMesa.ModificacionForward(datosModificacionForward).$promise.then(function(respuesta){
			   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
					if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
						bcsComunes.msg("info","Información","Operación modificada: " + FolioCartera);
					}
					else{
						bcsComunes.setConflictos(util.parseServerResponse(respuesta));
						bcsComunes.ventanaMsg({mensaje:msg,conflictos:util.parseServerResponse(respuesta)},idVentana);
				    }
					bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.ventanaCerrar(idVentana);
		   			
		   		},function(respuesta){
					bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.setConflictos(util.parseServerResponse(respuesta));
					bcsComunes.msg("error","Error","Existen problemas al modificar la operación");
					bcsComunes.ventanaCerrar(idVentana);
		   		});
			}
	    };
		
		/********************************************************************************************/
		/********************************* ANULAR OPERACION FORWARD *********************************/
		/********************************************************************************************/
		var AnulacionForward = function(){
			if($scope.parametros.Opcion !== "ANULAR") return;
			
			var datosAnulacionForward = {
				p_IdEmpresa 		: $scope.SesionIdEmpresa, //saca de variables de entorno
				p_IdPais			: $scope.SesionIdPaisSistema,
				p_IdUsuario 		: $scope.SesionIdUsuario, //saca de variables de entorno
				p_IdBolsa			: $scope.SesionBolsaDefectoSistema,
				p_IdSucursal		: 0,
				p_IdModulo 			: 'MFW',
				p_UsaReglasNegocio 	: 'N',
				p_tablaAnulacion    : [{ IdOperacion: IdOperacion, FolioCartera: FolioCartera }]
			};
			
			if($scope.confBasicaCalce.dataSource.view().length > 0 || varOperacionConCalce){
				//IMPCREDOPT-2801 se deja mensaje bloqueante al intentar anular operacion forward con calce
				/*var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_ANULA_OPERACION_CON_CALCE")+' '+FolioCartera+'?');
				t.result.then(function(result){
					if(result === OPCIONES_BCSALERTA.SI){
						llamadaSPAnulacion(datosAnulacionForward);
					}
					else{
						bcsComunes.ventanaCerrar(idVentana);
					}
				});*/
				var msg = "No es posible Anular una Operación calzada. Para esto, en el blotter Calce de Operaciones se debe Descalzar.";
				var respuesta = {};
				respuesta.P_OUT_ERRORESCONTADOR = 1;
				var idPopup = bcsComunes.ventanaMsg({mensaje:msg,conflictos:util.parseServerResponse(respuesta)},idVentana);
				if(idPopup){
					var promesa = bcsComunes.ventanaPromesa(idPopup);
					$.when(promesa).then(function(data){
						bcsComunes.ventanaOverlay(idVentana,false);
						bcsComunes.ventanaCerrar(idVentana);
					});
				}
				else{
					bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.ventanaCerrar(idVentana);
				}
			}
			else{
				var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_ANULA_OPERACION_CONFIRMACION")+' '+FolioCartera+'?');
				t.result.then(function(result){
					if(result === OPCIONES_BCSALERTA.SI){
						llamadaSPAnulacion(datosAnulacionForward);
					}
					else{
						bcsComunes.ventanaCerrar(idVentana);
					}
				});
			}
	    };
	    
	    var llamadaSPAnulacion = function(datosAnulacionForward){
	    	bcsComunes.ventanaOverlay(idVentana,true);
			bcsServiciosMesa.AnulacionForward(datosAnulacionForward).$promise.then(function(respuesta){
		   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				if (respuesta.P_OUT_ERRORESCONTADOR === 0){
					var idPopup = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_MENSAJE_OPERACION_ANULADA")+' '+FolioCartera,conflictos:util.parseServerResponse(respuesta)},idVentana);
					if(idPopup){
						var promesa = bcsComunes.ventanaPromesa(idPopup);
						$.when(promesa).then(function(data){
							bcsComunes.ventanaOverlay(idVentana,false);
							bcsComunes.ventanaCerrar(idVentana);
						});
					}
					else{
						bcsComunes.ventanaOverlay(idVentana,false);
						bcsComunes.ventanaCerrar(idVentana);
					}
				}
				else{
					var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
					$.each(ErrorDetalle,function(key,value){	
						bcsComunes.msg("error","Error","Existen problemas al anular la operación " + value.Codigo + " - " + value.Descripcion);
					});
					bcsComunes.ventanaOverlay(idVentana,false);
			    }
	   		},function(respuesta){
	   			var idPopup = bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas al Anular la operación"),conflictos:util.parseServerResponse(respuesta)},idVentana);
	   			if(idPopup){
					var promesa = bcsComunes.ventanaPromesa(idPopup);
					$.when(promesa).then(function(data){
						bcsComunes.ventanaOverlay(idVentana,false);
						bcsComunes.ventanaCerrar(idVentana);
					});
				}
				else{
					bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.ventanaCerrar(idVentana);
				}
	   		});
	    };
	    
	    
	    /********************************************************************************************/
		/******************************* ANTICIPO DE OPERACION FORWARD ******************************/
		/********************************************************************************************/
	    var AnticipoForward = function(){
	    	if($scope.parametros.Opcion === "ANTICIPO"){
	    		if($scope.data.oConfiguracionModalidadLiquidacion.seleccionado === undefined){
					bcsComunes.msg("warning","Error","Debe seleccionar una Modalidad de Liquidación.");
					return;
				}
				if($scope.data.oClientePortafolio.seleccionado.IdCliente === undefined){
					bcsComunes.msg("warning","Error","Debe seleccionar un Cliente.");
					return;
				}
				if($scope.data.oClientePortafolio.seleccionado.IdPortafolio === undefined){
					bcsComunes.msg("warning","Error","Debe seleccionar un Portafolio.");
					return;
				}
				/*if($scope.data.kngTCAnticipo === undefined || $scope.data.kngTCAnticipo === 0){
					bcsComunes.msg("warning","Error","Debe ingresar un Tipo de Cambio para el Anticipo.");
					return;
				}*/
				if(antNemo === 'ARBITR'){
					if(!$scope.data.kngTCObservadoAnt || $scope.data.kngTCObservadoAnt === ''){
						bcsComunes.msg("error","Error","Paridad Mercado de la moneda " + $scope.data.oConfiguracionMonedaPrincipal.seleccionado.IdMonedaPrincipal + " es cero en Mantenedor de Paridad y Tipo de Cambio.");
						return;
					}
					if(!$scope.data.kngTCAnticipo || $scope.data.kngTCAnticipo === ''){
						bcsComunes.msg("error","Error","Tipo de Cambio del Anticipo es cero.");
						return;
					}
					if(!$scope.data.kngMontoDO || $scope.data.kngMontoDO === ''){
						bcsComunes.msg("error","Error","Tipo de Cambio del Dólar Observado es cero en Mantenedor de Paridad y Tipo de Cambio.");
						return;
					}
				}
				else if(antNemo === 'SECARE'){
					if(!$scope.data.kngTCAnticipo || $scope.data.kngTCAnticipo === ''){
						bcsComunes.msg("error","Error","Tipo de Cambio del Anticipo es cero.");
						return;
					}
					if(!$scope.data.kngMontoDO || $scope.data.kngMontoDO === ''){
						bcsComunes.msg("error","Error","Tipo de Cambio del Dólar Observado es cero en Mantenedor de Paridad y Tipo de Cambio.");
						return;
					}
					if(!$scope.data.kngMontoUf || $scope.data.kngMontoUf === ''){
						bcsComunes.msg("error","Error","Tipo de Cambio de la UF es cero en Mantenedor de Paridad y Tipo de Cambio.");
						return;
					}
				}
				if($scope.data.kngMontoRecibirAnt === undefined || $scope.data.kngMontoRecibirAnt === 0){
					bcsComunes.msg("warning","Información","Monto a Recibir para el Anticipo es 0.");
				}
				/*if($scope.data.kngTCObservadoAnt === undefined || $scope.data.kngTCObservadoAnt === 0){
					bcsComunes.msg("warning","Error","Debe ingresar un Tipo de Cambio Observado.");
					return;
				}*/
				if($scope.data.kngPlazoResidual === undefined || $scope.data.kngPlazoResidual === 0 ){
					bcsComunes.msg("warning","Error","El Plazo del Anticipo no debe estar en 0.");
					return;
				}
				if($scope.data.kngPlazoResidual > $scope.data.kngPlazo){
					bcsComunes.msg("warning","Error","Para Anticipar una Operación el Plazo del Anticipo debe ser menor al Plazo de la Operación.");
					return;
				}
				if($scope.data.kngMontoPrincipalAnt > $scope.data.kngMonto){
					bcsComunes.msg("warning","Error","El Monto Anticipo no puede ser mayor al Monto de la Operación.");
					return;
				}
				
				var montoDiferidoANT = 0;
				var margenANT = 0;
				var dolarObservado = $scope.data.oConfiguracionMonedaSecundaria.seleccionado.IdMonedaSecundaria === 'USD' ? $scope.data.kngMontoDO : 1;
				var tipCamCOParCierre = $scope.data.tipoInstrumentoSeleccionado.Nemotecnico !== 'ARBITR' ? $scope.data.kngTCCierre||0 : $scope.data.kngParidadCierre||0; 
				if($scope.data.OperacionSeleccionada && $scope.data.OperacionSeleccionada.IdTipoOperacion === 'COMFWD'){
					montoDiferidoANT = (tipCamCOParCierre - $scope.data.kngTCCosto||0) * $scope.data.kngMontoPrincipalAnt;
					margenANT = ($scope.data.kngTCForward||0 - tipCamCOParCierre) * $scope.data.kngMontoPrincipalAnt * dolarObservado;
				}
				else if($scope.data.OperacionSeleccionada && $scope.data.OperacionSeleccionada.IdTipoOperacion === 'VENFWD'){
					montoDiferidoANT = ($scope.data.kngTCCosto||0 - tipCamCOParCierre) * $scope.data.kngMontoPrincipalAnt;
					margenANT = (tipCamCOParCierre - $scope.data.kngTCForward||0) * $scope.data.kngMontoPrincipalAnt * dolarObservado;
				}
				
				var datosAnticipoForward = {
					p_IdEmpresa 		: $scope.SesionIdEmpresa, //saca de variables de entorno
					p_IdPais			: $scope.SesionIdPaisSistema,
					p_IdUsuario 		: $scope.SesionIdUsuario, //saca de variables de entorno
					p_IdBolsa			: $scope.SesionBolsaDefectoSistema,
					p_IdSucursal		: 0,
					p_IdModulo 			: 'MFW',
					p_UsaReglasNegocio 	: 'N',
					PagaConCtaInv			: $scope.data.operaCuentaInversion?'S':'N',
					p_tablaForward : [{
						FolioInterno			: IdOperacion,
						FolioCartera			: FolioCartera,
						IdTipoOperacion       	: 'ANTFWD',
						IdTipoInstrumento       : $scope.data.tipoInstrumentoSeleccionado.IdTipoInstrumento,
						IdTipoProducto			: $scope.data.tipoInstrumentoSeleccionado.IdTipoProducto,
						IdInstrumento       	: $scope.data.tipoInstrumentoSeleccionado.IdInstrumento,
						Nemotecnico       		: $scope.data.tipoInstrumentoSeleccionado.Nemotecnico,
						IdCliente               : $scope.data.oClientePortafolio.seleccionado.IdCliente,
						IdPortafolio            : $scope.data.oClientePortafolio.seleccionado.IdPortafolio,
						IdPersonaEjecutivo		: $scope.data.oClientePortafolio.seleccionado.IdPersonaEjecutivo,
						IdTipoCanalIngreso		: 'MES',
						IdMonedaPrincipal       : $scope.data.oConfiguracionMonedaPrincipal.seleccionado.IdMonedaPrincipal,
						IdMonedaSecundaria      : $scope.data.oConfiguracionMonedaSecundaria.seleccionado.IdMonedaSecundaria,
						MontoOperacion          : $scope.data.kngMontoPrincipalAnt || 0,
						MontoInicial            : $scope.data.kngMontoInicial || 0,
						MontoFinal              : $scope.data.kngMontoFinal || 0,
						FechaOperacion          : $scope.SesionFechaSistema, // valor anterior util.dateSPdate($scope.data.kngFechaOperacion)
						TCCierre                : $scope.data.kngTCCierre || 0,
						ParidadCierre           : $scope.data.kngParidadCierre || 0,
						Plazo                   : $scope.data.kngPlazo || 0,
						FechaVencimiento        : $scope.SesionFechaSistema, //$scope.data.kngFechaVencimiento ? util.dateSPdate($scope.data.kngFechaVencimiento) : '0001-01-01',
						FechaPago               : $scope.data.kngFechaPago ? util.dateSPdate($scope.data.kngFechaPago) : $scope.data.oConfiguracionModalidadLiquidacion.seleccionado.ValorParametro === 'ENTREGA_FISICA' ? $scope.data.kngFechaVencimiento ? util.dateSPdate($scope.data.kngFechaVencimiento) : '0001-01-01' : '0001-01-01',
						IdModalidadLiquidacion  : $scope.data.oConfiguracionModalidadLiquidacion.seleccionado.IdParametro || 0,
						IdReferenciaMercado     : 0,
						IdPlaza                 : $scope.data.PlazaSeleccionado.IdCiudad,
						TCCosto                 : $scope.data.kngTCCosto || 0,
						PuntosTasasForward		: ($scope.data.ChkPuntosForward === true ? 'S':'N'),
						PuntosForward           : $scope.data.kngPuntosForward || 0,
						TCForward               : $scope.data.kngTCForward || 0,
						TasasMonedaPrincipal    : $scope.data.kngTasaMonedaPrincipal || 0,
						TasasMonedaSecundaria	: $scope.data.kngTasaMonedaSecundaria || 0,
						MontoDiferido           : montoDiferidoANT || 0,//$scope.data.kngMontoDiferido || 0,
						MontoMargen             : margenANT || 0,//$scope.data.kngMargen || 0
						IdFixing                : $scope.data.oConfiguracionFixing.seleccionado.IdParametro || 0,
						FechaFixing             : $scope.data.kngFechaFixing ? $scope.data.kngFechaFixing : '0001-01-01',
						HoraFixing              : $scope.data.HoraFixing ? util.formatoTime(new Date($scope.SesionFechaSistema + " " + $scope.data.HoraFixing)) : '00:00:00',
						IdMesa                  : $scope.data.oConfiguracionMesa.seleccionado.IdMesa || 0,
						IdSpotAsociado          : $scope.data.kngAsociadaSpot || 0,
						IdClasificacionCartera  : $scope.data.oConfiguracionClasificacionCartera.seleccionado.IdParametro || 0,
						Observaciones           : $scope.data.ngComentario || '',
						IdEstado				: 'ANT',
						TasaDescuento			: $scope.data.kngTasaDescuentoAnt,
						TipoCambioAnticipo		: $scope.data.kngTCAnticipo,
						MontoARecibirAnticipo   : $scope.data.kngMontoRecibirAnt,
						TipoCambioObservado     : $scope.data.kngTCObservadoAnt,
						MontoPrincipalAnticipo	: $scope.data.kngMontoPrincipalAnt
					}],
				};
				
				if($scope.data.kngMontoPrincipalAnt !== $scope.data.kngMonto){
					//Ahora se pueden Reversar Anticipos Parciales asi que se comenta la siguiente linea
					//varAdvAnticipoParcial = '. Los Anticipos Parciales no se pueden Reversar. ';
					varAdvAnticipoParcial = '';
				}
				
				if($scope.confBasicaCalce.dataSource.view().length){
					var arrayFoliosCalce = [];
					$.each($scope.confBasicaCalce.dataSource.view(),function(key,value){
						arrayFoliosCalce.push(value.FolioCartera);
					});
					
					var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_ANTICIPA_OPERACION_CON_CALCE").replace("::IdOperacion::",FolioCartera.toString()).replace("::FoliosOperacion::",arrayFoliosCalce.toString()));
					t.result.then(function(result){
						if(result === OPCIONES_BCSALERTA.SI){
							llamadaSPAnticipo(datosAnticipoForward);
						}
						else{
							bcsComunes.ventanaCerrar(idVentana);
						}
					});
				}
				else{
					llamadaSPAnticipo(datosAnticipoForward);
				}
			}
	    };
	    
	    var llamadaSPAnticipo = function(datosAnticipoForward){
	    	bcsComunes.ventanaOverlay(idVentana,true);
	    	bcsServiciosMesa.AnticipoForward(datosAnticipoForward).$promise.then(function(respuesta){
		   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				if (respuesta.P_OUT_TBLIDFORWARD[0] !== undefined && respuesta.P_OUT_ERRORESCONTADOR === 0){
					fnConsultarConfiguracionMesa();
					if(respuesta.P_OUT_TBLIDFORWARD[0].IdEstado === 'VIG' || respuesta.P_OUT_TBLIDFORWARD[0].IdEstado === 'ANT' || respuesta.P_OUT_TBLIDFORWARD[0].IdEstado === 'LIQ'){
						var idPopup = bcsComunes.ventanaMsg({mensaje:$translate.instant("Operación anticipada correctamente: " + respuesta.P_OUT_TBLIDFORWARD[0].FolioCartera)+varAdvAnticipoParcial,conflictos:util.parseServerResponse(respuesta)},idVentana);
						if(idPopup){
							var promesa = bcsComunes.ventanaPromesa(idPopup);
							$.when(promesa).then(function(data){
								bcsComunes.ventanaOverlay(idVentana,false);
								bcsComunes.ventanaCerrar(idVentana);
							});
						}
						else{
							bcsComunes.ventanaOverlay(idVentana,false);
							bcsComunes.ventanaCerrar(idVentana);
						}
					}
				}
				else{
	   			var idPopup = bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas al Anticipar la operación"),conflictos:util.parseServerResponse(respuesta)},idVentana);
	   			if(idPopup){
	   				var promesa = bcsComunes.ventanaPromesa(idPopup);
					$.when(promesa).then(function(data){
						bcsComunes.ventanaOverlay(idVentana,false);
						bcsComunes.ventanaCerrar(idVentana);
					});
	   			}
	   			else{
	   				bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.ventanaCerrar(idVentana);
	   			}	
			    }
	   		},function(respuesta){
	   			var idPopup = bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas al Anticipar la operación"),conflictos:util.parseServerResponse(respuesta)},idVentana);
	   			if(idPopup){
	   				var promesa = bcsComunes.ventanaPromesa(idPopup);
					$.when(promesa).then(function(data){
						bcsComunes.ventanaOverlay(idVentana,false);
						bcsComunes.ventanaCerrar(idVentana);
					});
	   			}
	   			else{
	   				bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.ventanaCerrar(idVentana);
	   			}
	   		});
	    };
	    
	    $scope.msgWebsocket = function(message) {
			if (message.Evento === 'ACTPTA') {
				actionTipoInstrumentoSeleccionado();
			}else if(message.Evento === 'ACTPTC'){
				var parametrosSP = {};
				parametrosSP.p_IdEmpresa= $scope.$root.SesionIdEmpresa;
				parametrosSP.p_IdUsuario= $scope.$root.SesionIdUsuario;
				parametrosSP.p_Fecha	= $scope.SesionFechaSistema;
				datosParidadTC = [];	
				bcsServiciosMesa.ConsultarParidadYTipoCambio(parametrosSP,function (result){
					if(result.P_OUT_PARIDADESYTIPOCAMBIO.length > 0 && result.P_OUT_ERRORESCONTADOR === 0){
						//datosParidadTC = result.P_OUT_PARIDADESYTIPOCAMBIO;
						$.each(result.P_OUT_PARIDADESYTIPOCAMBIO,function(key,value){
							var aux = {};
							aux.IdMoneda		= value.IdMoneda;
							aux.ParidadMercado	= value.ParidadMercado;
							aux.ParidadBID		= value.ParidadBID;
							aux.ParidadASK		= value.ParidadASK;
							aux.TipoCambioMercado = value.TipoCambioMercado;
							
							datosParidadTC.push(aux);
						});
						obtuvoValoresParidadTC = true;
					}
					bcsComunes.ventanaOverlay(idVentana,false);
					actionTipoInstrumentoSeleccionado();
					
		   		},function(respuesta){
		   			bcsComunes.msg("error","error",$translate.instant('UI_REFERENCIA_MERCADO_ERROR'));
					bcsComunes.setConflictos(util.parseServerResponse(respuesta));
					bcsComunes.ventanaOverlay($scope.winID, false);
		   		});	
				
			}
				
		}
	    
	});
}());