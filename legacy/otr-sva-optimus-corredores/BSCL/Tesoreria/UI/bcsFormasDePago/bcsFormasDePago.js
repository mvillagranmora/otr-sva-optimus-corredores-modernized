angular.module('ModuloTesoreria').directive('bcsFormasDePago', function(bcsComunes,$resource,$rootScope,$translate) {
	
	return {
		restrict: 'E',
    	replace: false,
		scope : {
			idVentana 					: '=?',
			objFormaDePago				: '=?',
			saldoPorAsignar				: '=?',
			editFormaDePago				: '=?',
			deleteFormaDePago			: '=?',
			resultDirectiva				: '=?',
			configuracionFormaPago		: '=?',
			limpiarFormaPago			: '=?',
			agregarFormaPagoAutomatica	: '=?',  // S, N  (Considerar preferencias de pago preconfiguradas)
			showBloqueDatosOperacion	: '=?',
			showBloqueDatosSocio		: '=?',
				
		},
		
		
		//negocio
		templateUrl: '../../Tesoreria/UI/bcsFormasDePago/bcsFormasDePago.html',
		link: function (scope, element, attrs, fn) {
			scope.data = {};		
			var mapChequesCorredora = new Object();
			var objFilterFormaDePago = {};
			var PreferenciaPagoSocio = [];
			
			//Configuración inicial de página
			scope.data.showModalidadPago 						= true;  		
			scope.data.showDatosOrigen 							= false;   	
			scope.data.showDatosDestino 						= false;
			scope.data.showConfiguracionPartida					= false;		
			scope.data.showDatosCaja 							= false;
			scope.data.showDatosCuentaBancariaCorredoraOrigen 	= false;
			scope.data.showDatosCuentaBancariaCorredoraDestino 	= false; 
			scope.data.showDatosCuentaBancariaSocioDestino 		= false; 
			scope.data.showDatosCuentaBancariaSocioOrigen 		= false;
			scope.data.showUsaTalonarioCheque					= false;		
			scope.data.ShowFolioDocumentoCorredora				= false;					
			scope.data.showSerieDocumentoCorredora				= false;
			scope.data.showFolioDocumentoCorredora				= false;
			scope.data.showMontoSaldo							= scope.objFormaDePago[0].TipoMovimiento === 'PAREGR' ? false : true;			
			scope.data.showCertificadoAutomatico				= scope.objFormaDePago[0].IdTipoMovimiento === 'MOVING' ? true : false;	
			
			scope.data.permiteCuentaBancaria 					= '';
						
			scope.data.montoFormaPagoSimbolo 	= scope.objFormaDePago[0].IdMoneda;
			scope.data.saldoPorAsignarSimbolo 	= scope.objFormaDePago[0].IdMoneda;
			scope.data.montoFormaPagoDecimal 	= (util.bcs_formato(0,'GEN','Monto',scope.objFormaDePago[0].IdMoneda,'','')).cantidadDecimales;	
			scope.data.saldoPorAsignarDecimal 	= (util.bcs_formato(0,'GEN','Monto',scope.objFormaDePago[0].IdMoneda,'','')).cantidadDecimales;
			
			scope.data.IdPersona				  = scope.objFormaDePago[0].IdPersona ? scope.objFormaDePago[0].IdPersona : 0;
			scope.data.kngFechaDocumentoSocio	  = util.dateInicioDia($rootScope.SesionFechaSistema);
			scope.data.IdComprobanteFormaDePago	  = 0;  // Para Comprobantes
			scope.data.IdIngresoEgresoFormaDePago = 0;  // Para Movimientos
			scope.data.CantidadPreferenciaSocio	  = 0;

			//------------------------------------------------
			// TRADUCCION PARA TITULOS
			//------------------------------------------------			
			var translate_array = ['UI_RECIBIMOS','UI_PAGAMOS','UI_SOCIO_NEGOCIO','UI_CORREDORA','UI_ORIGEN','UI_DESTINO'];
			
			$translate(translate_array).then(function(TRADUCCION)
			{
				scope.tituloPagamos   				= TRADUCCION.UI_PAGAMOS;    //Para MOVEGR
				scope.tituloRecibimos 				= TRADUCCION.UI_RECIBIMOS;  //Para MOVING
		 		scope.tituloSocioNegocio  			= TRADUCCION.UI_SOCIO_NEGOCIO;
		 		scope.tituloCorredora  				= TRADUCCION.UI_CORREDORA; 			
		 		scope.tituloOrigen  				= TRADUCCION.UI_ORIGEN;
		 		scope.tituloDestino  				= TRADUCCION.UI_DESTINO;
		 		scope.tituloConfiguracionPartida	= TRADUCCION.UI_CONFIGURACION;
		 		
				if (scope.objFormaDePago[0].IdTipoMovimiento === 'MOVING')				
		 			scope.tituloFormaPago = scope.tituloRecibimos;
				else
					scope.tituloFormaPago = scope.tituloPagamos;
				
			});
			
			
			var defConfiguracionTes = {
				type : "odata",
				definition : 
				{
					serverFiltering : true, // Filtrado de datos en el servidor.
		            serverSorting   : true, // Ordenamiento de los datos en el servidor.
		            sort 			: { field: "", dir: "DESC" },
		            transport		: {
				        	            read: {
				        	            		url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1037",
				        						dataType:"json"
				        	            	}
				        	          },
		            schema 			: { 
		                    			model: 
		                    				{
												fields : 
												{
													IdEmpresa			: {type: "int"},												
													CodigoParametro		: {type: "string"},
													Descripcion			: {type: "string"},
													Valor				: {type: "string"},
												}
											}
										}
		
		    	}
			};
			
			var dsConfiguracionTes = new bcs.DataSource(defConfiguracionTes);
			var filtroConfiguracionTes = new Array();
			filtroConfiguracionTes.push({ field: "CodigoParametro", operator: "eq", value: 'PERMITECUENTABANCARSOCIONEGOCIO' });
			dsConfiguracionTes.query({ filter: filtroConfiguracionTes }).then(function(){
				scope.data.configuracionTesParametroCuentaBancaria= dsConfiguracionTes.data();
				scope.data.permiteCuentaBancaria = scope.data.configuracionTesParametroCuentaBancaria[0].Valor;
			});
			
			
			
			// --------------------------------------------------
			// EDICION CAJA
			// --------------------------------------------------
			if (typeof scope.objFormaDePago[0].IdCaja === 'undefined' || Number(scope.objFormaDePago[0].IdCaja) === 0){
				var cajaDatasource = new bcs.DataSource({
					type: "odata",
					definition : {
						serverFiltering:true,
						transport: {read: {url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1046",dataType: "json"}},
						schema : {
							model : {
								fields : {								
									NombreCaja		: { type: "string"},
									NombreLargoCaja	: { type: "string"}
								}
							}
						},		
						filter: [{ "field": "IdEmpresa",       		"operator": "eq", "value" : $rootScope.SesionIdEmpresa },
								 { "field": "IdUsuario",       		"operator": "eq", "value" : $rootScope.SesionIdUsuario },
								 { "field": "IdEstadoCaja", 		"operator": "eq", "value" : "ABT" },
								 { "field": "IdEstadoCajaMoneda", 	"operator": "eq", "value" : "ACT" },
								 { "field": "IdEstadoCajaUsuario", 	"operator": "eq", "value" : "ACT" },				
							 	 { "field": "FechaInicio",     		"operator": "lte","value" : $rootScope.SesionFechaSistema },
							 	 { "field": "FechaTermino",     	"operator": "gte","value" : $rootScope.SesionFechaSistema },
								 { "field": "IdMoneda",     		"operator": "eq", "value" : scope.objFormaDePago[0].IdMoneda || ''}
								]
					}
				});
				cajaDatasource.query().then(function(){
					$rootScope.$apply(function() {
						if (typeof cajaDatasource.data()[0] !== 'undefined'){
							scope.objFormaDePago[0].IdCaja	   = cajaDatasource.data()[0].IdCaja;
							scope.objFormaDePago[0].CodigoCaja = cajaDatasource.data()[0].CodigoCaja;
							scope.objFormaDePago[0].NombreCaja = cajaDatasource.data()[0].NombreCaja;
						}
					});
				});
			};

			
			// --------------------------------------------------
			// GENERACION DE NOMINAS Y TRANSFERENCIAS  
			// --------------------------------------------------			
			scope.data.localizacion = $rootScope.SesionLocale;   // CL | PE
			
			scope.$watch('data.GeneraNomina',function(newval,oldval){
				if ($rootScope.SesionLocale === "PE")
				{	//Para PERU se puede escoger "Nomina"(Carta) o "Transferencia"(Archivo), pero no ambas.
					if (newval === true)
						scope.data.GeneraTransferencia = false;
				}
			});	

			scope.$watch('data.GeneraTransferencia',function(newval,oldval){
				if ($rootScope.SesionLocale === "PE")
				{	//Para PERU se puede escoger "Nomina"(Carta) o "Transferencia"(Archivo), pero no ambas.
					if (newval === true)
						scope.data.GeneraNomina = false;
				}
			});
			
			
			// --------------------------------------------------
			// CONFIGURACION DE COMBOS 
			// --------------------------------------------------
			
			scope.data.oConfiguracionMedioDePago={
					tipo : "combobox-medio-pago-tesoreria",
					configuracion : {},
					filtros : {},
					idActual : false,
					seleccionado : false,
					idDefecto 		: '',
					idPostFiltro  	: false	
			};	//Cambiar a combo complejo

			scope.data.oConfiguracionModalidadPago={
					tipo : "combobox-modalidad-pago",
					configuracion : {},
					filtros : {
						IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
						IdMedioDePago : { "field"   : "IdMedioDePago","operator": "eq", "value"   : "" }
					},
					idActual : false,
					seleccionado : false,
					idDefecto : '',					
			};
			scope.data.oConfiguracionCuentaBancoCorredora={
					tipo : "combobox-cuentas-bancarias-corredora",
					configuracion : {},
					filtros : {
						IdEstado  : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },				
						IdMoneda  : { "field"   : "IdMoneda", "operator": "eq", "value"   : scope.objFormaDePago[0].IdMoneda  }, // Cuentas de la misma moneda del comprobante
					},
					idActual : false,
					seleccionado : false,
					idDefecto : ''
			};
			//Cuenta bancaria del socio de negocio
			scope.data.oConfiguracionCuentaBancoSocioNegocio={
					tipo : "combobox-persona-cuentas-bancarias",
					configuracion : {},
					filtros : {
						IdEstado  : { "field"   : "IdEstado", "operator": "eq", "value"   : "ACT" },
						IdPersona : { "field"   : "IdPersona","operator": "eq", "value"   : Number(scope.data.IdPersona) }, // Cuentas de la Persona
						IdMoneda  : { "field"   : "IdMoneda", "operator": "eq", "value"   : scope.objFormaDePago[0].IdMoneda  }, // Cuentas de la misma moneda del comprobante
					},
					idActual : false,
					seleccionado : false,
					idDefecto 		: '',
					idPostFiltro  	: false	
			};
			
			//Banco del socio(puede ser cualquiera para los ingresos)
			scope.data.oConfiguracionBancoCuentaSocio={
					tipo          : "combobox-banco",
					configuracion : {},
					filtros       : {
						
					}, 
					idActual      : false,
					seleccionado  : false,
					idDefecto     : '',		
					idPostFiltro  :	false
			};
			scope.data.oConfiguracionChequerasSeries={
					tipo : "combobox-chequeras-series",
					configuracion : {},
					filtros : {
						IdEstado      : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
						IdCuentaBanco : { "field"   : "IdCuentaBanco","operator": "eq", "value"   :"" }
					},
					idActual : false,
					seleccionado : false,
					idDefecto : false
			};

			
			//---------------------------------------------------------	
			// OBTIENE PREFERENCIAS DE PAGO	
			//---------------------------------------------------------
			MediosDePagoPorDefecto = function(){
				if (typeof scope.objFormaDePago[0].IdTipoMovimiento !== 'undefined'){
					scope.data.oConfiguracionMedioDePago.filtros = {
						IdEstado 		 	: { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
						IdMoneda 		 	: { "field"   : "IdMoneda","operator": "eq", "value"   : scope.objFormaDePago[0].IdMoneda },
						IdTipoMovimiento 	: { "field"   : "IdTipoMovimiento","operator": "eq", "value"   : scope.objFormaDePago[0].IdTipoMovimiento },
						DesplegarEnCombobox : { "field"   : "DesplegarEnCombobox","operator": "eq", "value"   : "S" },
					};
				};
			};

			 
			var obtenerPreferenciasPagoCorredor = function(ConsiderarTipoEmpleado)
			{	 
				
				if ((ConsiderarTipoEmpleado) && typeof scope.objFormaDePago[0].TipoEmpleado !== 'undefined' && (scope.objFormaDePago[0].TipoEmpleado === 'FRONTOFFICE' || scope.objFormaDePago[0].TipoEmpleado === 'SUPERVISOR')) return;
			
				scope.data.oConfiguracionMedioDePago.filtros = {};
				if ($rootScope.SesionIdEmpresa && typeof scope.objFormaDePago[0].IdConcepto !== 'undefined' && scope.objFormaDePago[0].TipoMovimiento !== 'PAREGR')				
				{
					var preferenciasDatasource = new bcs.DataSource({
						type: "odata",
						definition : {
							serverFiltering:true,
							transport: {read: {url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1023",dataType: "json"}},
							schema : {
								model : {
									fields : {
										IdEmpresa			: { type: "string"},
										IdTipoMovimiento	: { type: "string"},
										IdEstado 	        : { type: "string"},
										IdConcepto			: { type: "string"},
										IdMedioDePago		: { type: "string"},
										IdCuentaBanco		: { type: "number"},
									}
								}
							},
							filter: [{ field: "IdEmpresa", operator: "eq", value: Number($rootScope.SesionIdEmpresa) },
									 { field: "IdTipoMovimiento",  operator: "eq", value: scope.objFormaDePago[0].IdTipoMovimiento },
									 { field: "IdEstado",  operator: "eq", value: 'ACT' },
									 { field: "IdConcepto", operator: "eq", value: scope.objFormaDePago[0].IdConcepto }
									]
						}
					});
					preferenciasDatasource.fetch().then(function(){
						 if (preferenciasDatasource.data().length >= 1 )
						 {
							 $rootScope.$apply(function() {
								//Si UnicaOpcionDePago = 'S' no se muestran otros medios de pago 	
								if (preferenciasDatasource.data()[0].UnicaOpcionDePago === 'S')
								{
									scope.data.oConfiguracionMedioDePago.filtros = {
											IdEstado 		 	: { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
											IdMoneda 		 	: { "field"   : "IdMoneda","operator": "eq", "value"   : scope.objFormaDePago[0].IdMoneda },
											IdTipoMovimiento 	: { "field"   : "IdTipoMovimiento","operator": "eq", "value"   : scope.objFormaDePago[0].IdTipoMovimiento },											
											IdMedioDePago 	    : { "field"   : "IdMedioDePago","operator": "eq", "value"   :  preferenciasDatasource.data()[0].IdMedioDePago},											
											DesplegarEnCombobox : { "field"   : "DesplegarEnCombobox","operator": "eq", "value"   : "S" },
										};									
								}
								
								scope.data.oConfiguracionMedioDePago.idActual = preferenciasDatasource.data()[0].IdMedioDePago;
								scope.data.oConfiguracionMedioDePago.itemPostFiltro = preferenciasDatasource.data()[0].IdMedioDePago;

								scope.data.oConfiguracionCuentaBancoCorredora.idActual = preferenciasDatasource.data()[0].IdCuentaBanco;		
								scope.data.oConfiguracionCuentaBancoCorredora.itemPostFiltro = preferenciasDatasource.data()[0].IdCuentaBanco;

								return true;
							 });
						 } 
						 else 
						 {
							MediosDePagoPorDefecto();
						 }
					});
				};

				if (scope.objFormaDePago[0].TipoMovimiento === 'PAREGR' && typeof scope.objFormaDePago[0].mediosDePagoAGirar !== 'undefined'){
					$.each(scope.objFormaDePago[0].mediosDePagoAGirar, function(key,value){
						scope.data.oConfiguracionMedioDePago.filtros["IdMedioDePago_" + value.IdMedioDePago] = { field: "IdMedioDePago", operator: "eq", value: value.IdMedioDePago };
					});
					scope.data.oConfiguracionMedioDePago.filtros.logic = 'or';
				} 
				else 
				{
					MediosDePagoPorDefecto();
				};
			};


			
			var obtenerPreferenciasPagoSocioDeNegocio = function(objFormaDePago){
				if (typeof objFormaDePago[0].IdPortafolio     !== 'undefined' && objFormaDePago[0].IdPortafolio !== false &&
					typeof objFormaDePago[0].IdTipoMovimiento !== 'undefined' && objFormaDePago[0].IdTipoMovimiento !== false &&
					typeof objFormaDePago[0].IdMoneda         !== 'undefined' && objFormaDePago[0].IdMoneda != false){
					
					var PreferenciaPagoSocioDeNegocioDatasource = new bcs.DataSource({
						type: "odata",
						definition : {
							serverFiltering	:true,
							transport		: {read:  {url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1266",dataType: "json"}},
							schema 			: {model: {fields : {TipoEmpleado : { type: "string"}}}},		
							filter			: [
												{ field: "IdPortafolio",    operator: "eq", value: objFormaDePago[0].IdPortafolio },
												{ field: "IdTipoOperacion", operator: "eq", value: objFormaDePago[0].IdTipoMovimiento === 'MOVING' ? 'INGTES' : 'EGRTES'},
												{ field: "PorDefecto",      operator: "eq", value: 'S'},
												{ field: "IdMoneda",        operator: "eq", value: objFormaDePago[0].IdMoneda},
												{ field: "IdEstado",        operator: "eq", value: 'ACT'}
											  ]
						}
					});
					
					PreferenciaPagoSocioDeNegocioDatasource.fetch().then(function(){
						if (PreferenciaPagoSocioDeNegocioDatasource.data().length >= 1 )
						{
							$rootScope.$apply(function() {									
								scope.data.oConfiguracionMedioDePago.idActual				= PreferenciaPagoSocioDeNegocioDatasource.data()[0].IdMedioDePago;									
								scope.data.oConfiguracionMedioDePago.idPostFiltro       	= PreferenciaPagoSocioDeNegocioDatasource.data()[0].IdMedioDePago;

								//Socio
								scope.data.oConfiguracionCuentaBancoSocioNegocio.idActual     = PreferenciaPagoSocioDeNegocioDatasource.data()[0].IdCuentaBancaria;
								scope.data.oConfiguracionCuentaBancoSocioNegocio.idPostFiltro = PreferenciaPagoSocioDeNegocioDatasource.data()[0].IdCuentaBancaria;									
								scope.data.oConfiguracionBancoCuentaSocio.idActual 		      = PreferenciaPagoSocioDeNegocioDatasource.data()[0].IdBancoCuentaBancaria;
								scope.data.NumeroCuentaSocio							      = PreferenciaPagoSocioDeNegocioDatasource.data()[0].NumeroCuentaBancaria;
									
								return true;
							});
						}
					});
					
				};
			};		
			
			

			

			// ----------------------------------------------
			// BLOQUE WATCH (INICIO)
			// ----------------------------------------------    
			//Al cambiar la "Cuenta Bancaria de la corredora" se actualizan los datos de visualización.	
			scope.$watch('data.oConfiguracionCuentaBancoCorredora.seleccionado',function(newval,oldval){
				if (typeof scope.data.oConfiguracionCuentaBancoCorredora.seleccionado !== 'undefined' && scope.data.oConfiguracionCuentaBancoCorredora.seleccionado !== false)
				{
					scope.data.TipoCuentaCorredora = scope.data.oConfiguracionCuentaBancoCorredora.seleccionado.NombreTipoCuentaBancaria;
					scope.data.BancoCuentaCorredora = scope.data.oConfiguracionCuentaBancoCorredora.seleccionado.NombreBanco;			
					scope.data.NumeroCuentaCorredora = scope.data.oConfiguracionCuentaBancoCorredora.seleccionado.NumeroCuentaBancaria;
					scope.data.MonedaCuentaCorredora = scope.data.oConfiguracionCuentaBancoCorredora.seleccionado.NombreMoneda;
                    scope.data.configuracionFormaPago
                    if ((scope.objFormaDePago[0].TipoMovimiento == 'PAREGR')) {
						scope.data.GeneraNomina         = (scope.data.oConfiguracionCuentaBancoCorredora.seleccionado.GeneraNomina === 'S') ? true : false;
						scope.data.GeneraTransferencia   = (scope.data.oConfiguracionCuentaBancoCorredora.seleccionado.GeneraTransferencia === 'S') ? true : false;
                    }
				}
			});
						
			//Al cambiar la "Cuenta Bancaria Socio de Negocio" se actualizan los datos de visualización.
			scope.$watch('data.oConfiguracionCuentaBancoSocioNegocio.seleccionado',function(newval,oldval){
				
				if (typeof scope.data.oConfiguracionCuentaBancoSocioNegocio.seleccionado !== 'undefined' && scope.data.oConfiguracionCuentaBancoSocioNegocio.seleccionado !== false)
				{	
					scope.data.TipoCuentaSocio = scope.data.oConfiguracionCuentaBancoSocioNegocio.seleccionado.NombreTipoCuentaBancaria;												
					scope.data.BancoCuentaSocio = scope.data.oConfiguracionCuentaBancoSocioNegocio.seleccionado.NombreBanco;			
					scope.data.NumeroCuentaSocio = scope.data.oConfiguracionCuentaBancoSocioNegocio.seleccionado.NumeroCuentaBancaria;
					scope.data.MonedaCuentaSocio = scope.data.oConfiguracionCuentaBancoSocioNegocio.seleccionado.NombreMoneda;
				}
			});	
		
			//scope.$watch('data.oConfiguracionChequerasSeries.seleccionado',function(newval,oldval){		
			scope.$watchGroup(['data.oConfiguracionCuentaBancoCorredora.seleccionado','data.oConfiguracionChequerasSeries.seleccionado'],function(newval,oldval){
				if (typeof scope.data.oConfiguracionCuentaBancoCorredora.seleccionado !== 'undefined' && scope.data.oConfiguracionCuentaBancoCorredora.seleccionado !== false &&
					typeof scope.data.oConfiguracionChequerasSeries.seleccionado !== 'undefined' && scope.data.oConfiguracionChequerasSeries.seleccionado !== false){
		
					
					var keys = scope.data.oConfiguracionChequerasSeries.seleccionado.IdCuentaBanco +"-"+ scope.data.oConfiguracionChequerasSeries.seleccionado.Serie;
			
					if (typeof mapChequesCorredora[keys] !== 'undefined'){				
						if ( (Number(mapChequesCorredora[keys]) + 1) <= scope.data.oConfiguracionChequerasSeries.seleccionado.NumeroChequeHasta)
							mapChequesCorredora[keys] = mapChequesCorredora[keys] + 1;
					}else{
						mapChequesCorredora[keys] = scope.data.oConfiguracionChequerasSeries.seleccionado.NumeroChequeActual;
					}
					
					scope.data.FolioDocumentoCorredora = mapChequesCorredora[keys];
				}
				else
					scope.data.FolioDocumentoCorredora = '';
			});	
		
					
			scope.$watch('data.oConfiguracionMedioDePago.seleccionado',function(newval,oldval){				
				if (typeof scope.data.oConfiguracionMedioDePago.seleccionado !== 'undefined' && scope.data.oConfiguracionMedioDePago.seleccionado !== false)
				{	
					//Al cambiar "Medio de Pago" activa o desactiva combo "Modalidad de Pago"
					switch (scope.data.oConfiguracionMedioDePago.seleccionado.TipoMedioDePago)
					{		
						case "CIN":   //Cuenta Inversión
							scope.data.showModalidadPago 	= false;   //combo modalidad de pago
							scope.data.showDatosOrigen 		= false;   //Oculta sección "Datos Origen"
							scope.data.showDatosDestino 	= false;   //Oculta sección "Datos Destino"
							break;		
						case "CHE":
							if (scope.objFormaDePago[0].TipoMovimiento !== 'PAREGR'){
								scope.data.ShowFolioDocumentoCorredora = true;	
							} else {
								scope.data.showUsaTalonarioCheque = true;
							}
							scope.data.usaTalonarioCheque = false;
							break;
							
						default:  //Otros Medios de Pago
							scope.data.ShowFolioDocumentoCorredora = false;
							scope.data.showModalidadPago = true;      //disponibiliza modalidad de pago
					}
					//scope.data.oConfiguracionModalidadPago.filtros.read = true;


					if (scope.objFormaDePago[0].TipoMovimiento === 'PAREGR' || scope.objFormaDePago[0].TipoMovimiento === 'PARING'){
						
						if (typeof scope.objFormaDePago[0].mediosDePagoAGirar !== 'undefined')
						{
							$.each(scope.objFormaDePago[0].mediosDePagoAGirar, function(key,value){
								scope.data.oConfiguracionMedioDePago.filtros["IdMedioDePago_" + value.IdMedioDePago] = { field: "IdMedioDePago", operator: "eq", value: value.IdMedioDePago };
							});
							scope.data.oConfiguracionMedioDePago.filtros.logic = 'or';
						};						
						
						$.each(scope.objFormaDePago[0].mediosDePagoAGirar, function(key,value){			
							if (value.IdMedioDePago ===  newval.IdMedioDePago){
								scope.data.kngMontoFormaPago 	= value.MontoPorMedioDePago;
								scope.objFormaDePago[0].Monto	= value.MontoPorMedioDePago;
							}
						});
					} 

					//Actualiza medio de pago del socio de negocio
					if (PreferenciaPagoSocio.length > 0){
						for (i=0; i< PreferenciaPagoSocio.length; i++){
							if (PreferenciaPagoSocio[i].IdMedioDePago === scope.data.oConfiguracionMedioDePago.seleccionado.IdMedioDePago){
								scope.data.oConfiguracionCuentaBancoSocioNegocio.idActual = PreferenciaPagoSocio[i].IdCuentaBancaria;		
								scope.data.oConfiguracionCuentaBancoSocioNegocio.itemPostFiltro = PreferenciaPagoSocio[i].IdCuentaBancaria;
								break;	
							};
						};						
					};
				}
			});
		
			//Al cambiar selección del combo Medio De Pago - Modalidad de pago,  modifica la visualización del formulario de formas de pago.
			scope.$watchGroup(['data.oConfiguracionMedioDePago.seleccionado','data.oConfiguracionModalidadPago.seleccionado','objFormaDePago[0].IdTipoMovimiento'], 
			function(newval,oldval){
				if (newval[0] === oldval[0]  && newval[1] === oldval[1]) return;
				
				if (typeof scope.data.oConfiguracionMedioDePago.seleccionado !== 'undefined' &&
					scope.data.oConfiguracionMedioDePago.seleccionado !== false &&
					typeof scope.data.oConfiguracionModalidadPago.seleccionado !== 'undefined' &&
					scope.data.oConfiguracionModalidadPago.seleccionado !== false)
				{			
					scope.limpiarVariablesShow();
		
					var idTipoMovimiento 	=  scope.objFormaDePago[0].IdTipoMovimiento !== undefined ? scope.objFormaDePago[0].IdTipoMovimiento : 'MOVING' ;  // MOVING:ingreso   MOVEGR:egreso						
					var tipoMedioDePago  	=  scope.data.oConfiguracionMedioDePago.seleccionado.TipoMedioDePago;
					var idModalidadDePago  	=  scope.data.oConfiguracionModalidadPago.seleccionado.IdModalidadDePago;      // MODCAJ:caja   MODDEP:deposito
					
					
					//INGRESO
					if (idTipoMovimiento === "MOVING") 
					{							
						scope.data.showDatosOrigen  = true;
						scope.data.showDatosDestino = true;
						
						//Define títulos según tipo de movimiento
						scope.tituloOrigenPago = scope.tituloOrigen + " ("+  scope.tituloSocioNegocio + ")"
						scope.tituloDestinoPago = scope.tituloDestino + " ("+ scope.tituloCorredora  + ")"				
						scope.data.showDatosCuentaBancariaSocioOrigen  = true;
						
						switch (tipoMedioDePago)
						{
							case "CHE":  //Cheque      -- SOLO CAJA
							case "VVI":  //Vale Vista  -- SOLO CAJA
								////if (idModalidadDePago === "MODCAJ")
								////{
									scope.data.showDatosCaja  = true;  //Muestra sección "Datos Caja"

									//Refresca los datos de la Caja
									scope.data.NombreCaja = scope.objFormaDePago[0].NombreCaja;
									scope.data.CodigoCaja = scope.objFormaDePago[0].CodigoCaja;
									scope.data.MonedaCaja = scope.objFormaDePago[0].IdMoneda;
								////}
								////else 
								////{	//Modalidad "Depósito"
								////	scope.data.showDatosCuentaBancariaCorredoraDestino  = true;  //Activa sección "Datos cuenta bancaria corredora"
									////////scope.data.oConfiguracionCuentaBancoCorredora.requerido	= scope.objFormaDePago[0].TipoEmpleado === 'BACKOFFICE' ? true : false;   /// falta borrar									
								////	scope.data.oConfiguracionCuentaBancoCorredora.requerido	= false;
								////}
								
								scope.data.showBancoCuentaSocio    = true;
								scope.data.showNumeroCuentaSocio   = true;
								scope.data.showFechaDocumentoSocio = true;
								scope.data.showFolioDocumentoSocio = true;
								scope.data.showSerieDocumentoSocio = true;
								
								break;	
								
							case "EFE":  //Efectivo -- SOLO CAJA						
								scope.data.showDatosCaja     = true;  //Activa sección "Datos Caja"
								//Refresca los datos de la Caja
								scope.data.NombreCaja = scope.objFormaDePago[0].NombreCaja; 		
								scope.data.CodigoCaja = scope.objFormaDePago[0].CodigoCaja;
								scope.data.MonedaCaja = scope.objFormaDePago[0].IdMoneda;
								break;
								
							case "TRA":  //Transferencia (agrupa todos los tipos de transferencias)	 -- SOLO DEPOSITO
								scope.data.showDatosCuentaBancariaCorredoraDestino  = true;  //Activa sección "Datos cuenta bancaria corredora"
								///////////scope.data.oConfiguracionCuentaBancoCorredora.requerido	= scope.objFormaDePago[0].TipoEmpleado === 'BACKOFFICE' ? true : false;   //// falta borrar ////////
								scope.data.oConfiguracionCuentaBancoCorredora.requerido	= false;
		
								scope.data.showBancoCuentaSocio    = true;
								scope.data.showNumeroCuentaSocio   = true;
								scope.data.showFechaDocumentoSocio = true;
								scope.data.showFolioDocumentoSocio = true;
								scope.data.showSerieDocumentoSocio = false;								
								break;	
								
							case "BDE":  //Boleta de Depósito -- SOLO DEPOSITO
								scope.data.showDatosCuentaBancariaCorredoraDestino  = true;  //Activa sección "Datos cuenta bancaria corredora"
								/////////////scope.data.oConfiguracionCuentaBancoCorredora.requerido	= scope.objFormaDePago[0].TipoEmpleado === 'BACKOFFICE' ? true : false;  //// falta borrar ////////
								scope.data.oConfiguracionCuentaBancoCorredora.requerido	= false;
								
								scope.data.showBancoCuentaSocio    = false;
								scope.data.showNumeroCuentaSocio   = false;
								scope.data.showFechaDocumentoSocio = true;
								scope.data.showFolioDocumentoSocio = true;
								scope.data.showSerieDocumentoSocio = false;						
								break;
								
							case "EBA":  //Extra Banco       -- SOLO DEPOSITO
								scope.data.showDatosCuentaBancariaCorredoraDestino  = true;  //Activa sección "Datos cuenta bancaria corredora"
								///////scope.data.oConfiguracionCuentaBancoCorredora.requerido	= scope.objFormaDePago[0].TipoEmpleado === 'BACKOFFICE' ? true : false; //// falta borrar /////////
								scope.data.oConfiguracionCuentaBancoCorredora.requerido	= false;

								scope.data.showBancoCuentaSocio    = false;
								scope.data.showNumeroCuentaSocio   = false;
								scope.data.showFechaDocumentoSocio = true;
								scope.data.showFolioDocumentoSocio = true;
								scope.data.showSerieDocumentoSocio = true;						
								break; 
								
								
						}
					}
					
					//EGRESO
					if (idTipoMovimiento === "MOVEGR")
					{	
						
						scope.data.showDatosOrigen  = true;
						scope.data.showDatosCuentaBancariaSocioOrigen  = true;
						scope.data.showDatosDestino = scope.objFormaDePago[0].TipoMovimiento === 'PAREGR' ? false : true;
						scope.data.showConfiguracionPartida	= scope.objFormaDePago[0].TipoMovimiento === 'PAREGR' ? true : false;
						
						//Define títulos según tipo de movimiento
						scope.tituloOrigenPago = scope.tituloOrigen + " ("+  scope.tituloCorredora + ")"
						scope.tituloDestinoPago = scope.tituloDestino + " ("+  scope.tituloSocioNegocio + ")"
						
						
			 			
						switch (tipoMedioDePago)
						{
							case "CIN":  //Cuenta inversion
								break 
							default:     //Otros medios de pago   						
									scope.data.showDatosCuentaBancariaCorredoraOrigen = true;  //Muestra sección "Datos Cuenta Bancaria Corredora" origen
									
									//////scope.data.oConfiguracionCuentaBancoCorredora.requerido	= (scope.objFormaDePago[0].TipoEmpleado === 'BACKOFFICE' || scope.objFormaDePago[0].TipoMovimiento === 'PAREGR')  ? true : false;  //// falta borrar ///////
									scope.data.oConfiguracionCuentaBancoCorredora.requerido	= (scope.objFormaDePago[0].TipoMovimiento === 'PAREGR') ? true : false;
									
								
								if ((tipoMedioDePago === 'VVI' || tipoMedioDePago === 'CHE' || tipoMedioDePago === 'BDE' || tipoMedioDePago === 'EBA') 
									&& scope.objFormaDePago[0].TipoMovimiento !== 'PAREGR')
								{
									scope.data.showFolioDocumentoCorredora = true;
									scope.data.showSerieDocumentoCorredora = true;
								}
								
								if (idModalidadDePago === "MODCAJ")
								{
									if (scope.objFormaDePago[0].TipoMovimiento !== 'PAREGR'){				
										scope.data.showDatosCaja  = true;  //Muestra sección "Datos Caja"
										scope.data.NombreCaja = scope.objFormaDePago[0].NombreCaja; 		
										scope.data.CodigoCaja = scope.objFormaDePago[0].CodigoCaja;
										scope.data.MonedaCaja = scope.objFormaDePago[0].IdMoneda;
									}
								}
								else 
								{	
									scope.data.showDatosCuentaBancariaSocioDestino  = scope.objFormaDePago[0].TipoMovimiento === 'PAREGR' ? false : true;  //Muestra "Datos Cuenta Bancaria Socio Negocio"
								}
								break;
						}
					}

				}
			});
			
			scope.$watch('editFormaDePago',function(newval,oldval){	
				if (typeof scope.editFormaDePago !== 'undefined'){
					scope.data.IdComprobanteFormaDePago						= scope.editFormaDePago.IdComprobanteFormaDePago;   // Para Comprobantes					
					scope.data.IdIngresoEgresoFormaDePago					= scope.editFormaDePago.IdIngresoEgresoFormaDePago; // Para Movimientos
					scope.data.kngMontoFormaPago	 						= scope.editFormaDePago.Monto;
					scope.data.oConfiguracionMedioDePago.idActual			= scope.editFormaDePago.IdMedioDePago;

					//Corredora
					scope.data.oConfiguracionCuentaBancoCorredora.idActual 	= scope.editFormaDePago.IdCuentaBancariaCorredora;
					scope.data.oConfiguracionChequerasSeries.idActual 		= scope.editFormaDePago.NumeroSerieCorredora;
					scope.data.FolioDocumentoCorredora 						= scope.editFormaDePago.NumeroDocumentoCorredora;

					//Socio
					scope.data.oConfiguracionCuentaBancoSocioNegocio.idActual = scope.editFormaDePago.IdCuentaBancariaSocio;
					scope.data.oConfiguracionBancoCuentaSocio.idActual 		= scope.editFormaDePago.IdBancoSocio;
					scope.data.NumeroCuentaSocio							= scope.editFormaDePago.NumeroCuentaBancariaSocio;
					scope.data.SerieDocumentoSocio							= scope.editFormaDePago.NumeroSerieSocio;
					scope.data.FolioDocumentoSocio							= scope.editFormaDePago.NumeroDocumentoSocio;
					scope.data.kngFechaDocumentoSocio						= scope.editFormaDePago.FechaDocumentoPagoSocio;
					
				}
			});
			
			scope.$watch('saldoPorAsignar',function(newval,oldval){				
				scope.data.kngMontoFormaPago 		= scope.saldoPorAsignar;
				scope.data.montoFormaPagoSimbolo	= scope.objFormaDePago[0].IdMoneda;
				scope.data.saldoPorAsignarSimbolo	= scope.objFormaDePago[0].IdMoneda;
				
			},true);

			scope.$watch('deleteFormaDePago',function(newval,oldval){
				if (typeof scope.deleteFormaDePago !== 'undefined'){
					var keys = scope.deleteFormaDePago.IdCuentaBancariaCorredora +"-"+ scope.deleteFormaDePago.NumeroSerieCorredora;			
					mapChequesCorredora[keys] = scope.deleteFormaDePago.NumeroDocumentoCorredora;
				}								
			},true);

			scope.$watch('objFormaDePago',function(newval,oldval){	
				if (newval === oldval) return;
					
						
				//Actualiza combo Cuentas Bancarias del Socio de negocio 
				if (newval[0].IdPersona !== oldval[0].IdPersona || newval[0].IdMoneda !== oldval[0].IdMoneda)
				{
					scope.data.oConfiguracionCuentaBancoSocioNegocio.filtros.IdPersona.value = scope.objFormaDePago[0].IdPersona ? scope.objFormaDePago[0].IdPersona : 0;				
					scope.data.oConfiguracionCuentaBancoSocioNegocio.filtros.IdMoneda.value  = scope.objFormaDePago[0].IdMoneda;
				}

				//Actualiza combo Cuentas Bancarias de la corredora 
				if (newval[0].IdMoneda !== oldval[0].IdMoneda)
				{				
					scope.data.montoFormaPagoSimbolo	= scope.objFormaDePago[0].IdMoneda;
					scope.data.saldoPorAsignarSimbolo	= scope.objFormaDePago[0].IdMoneda;	
					scope.data.oConfiguracionCuentaBancoCorredora.filtros.IdMoneda.value  = scope.objFormaDePago[0].IdMoneda;					
				}
				
				//Actualiza título de forma de pago
				if (newval[0].IdTipoMovimiento !== oldval[0].IdTipoMovimiento)
				{
					if (newval[0].IdTipoMovimiento === 'MOVING')
			 			scope.tituloFormaPago = scope.tituloRecibimos;
					else
						scope.tituloFormaPago = scope.tituloPagamos;
				}

				
				if (scope.agregarFormaPagoAutomatica === 'S')
				{
					obtenerPreferenciasPagoSocioDeNegocio(newval);
					obtenerPreferenciasPagoCorredor(true);
				}
				if (scope.objFormaDePago[0].TipoMovimiento === 'PAREGR'){
					scope.limpiarFormaPago();
				}

				
			},true);
			// ----------------------------------------------
			// BLOQUE WATCH (FIN)
			// ----------------------------------------------
			



			// --------------------------------------------------
			// RESULTADO DIRECTIVA 
			// --------------------------------------------------
			scope.resultDirective = function(){
				var objInfoFormaPago = {};

				objInfoFormaPago.IdCaja								= scope.objFormaDePago[0].IdCaja;
				objInfoFormaPago.CodigoCaja							= scope.objFormaDePago[0].CodigoCaja;
				objInfoFormaPago.NombreCaja							= scope.objFormaDePago[0].NombreCaja;

				objInfoFormaPago.IdMedioDePago						= scope.data.oConfiguracionMedioDePago.seleccionado.IdMedioDePago || 0;
				objInfoFormaPago.NombreMedioDePago					= scope.data.oConfiguracionMedioDePago.seleccionado.Nombre || '';
				objInfoFormaPago.TipoMedioDePago  					= scope.data.oConfiguracionMedioDePago.seleccionado.TipoMedioDePago || '';
				objInfoFormaPago.IdModalidadDePago					= scope.data.oConfiguracionModalidadPago.seleccionado.IdModalidadDePago || '';			
				objInfoFormaPago.NombreModalidadDePago				= scope.data.oConfiguracionModalidadPago.seleccionado.Nombre;				
				objInfoFormaPago.IdMoneda							= scope.objFormaDePago[0].IdMoneda || '';
				objInfoFormaPago.NombreMoneda						= scope.objFormaDePago[0].NombreMoneda || '';
				objInfoFormaPago.Monto								= scope.data.kngMontoFormaPago || 0;
				objInfoFormaPago.IdComprobante						= scope.objFormaDePago[0].IdComprobante || '';
				objInfoFormaPago.IdTipoMovimiento					= scope.objFormaDePago[0].IdTipoMovimiento || '';
				objInfoFormaPago.IdComprobantePartida               = 0;
				objInfoFormaPago.IdComprobanteFormaDePago			= scope.data.IdComprobanteFormaDePago;    // Para comprobantes 
				objInfoFormaPago.IdIngresoEgresoFormaDePago			= scope.data.IdIngresoEgresoFormaDePago;  // Para movimientos
				
				//CORREDORA
				objInfoFormaPago.IdCuentaBancariaCorredora 			= Number(scope.data.oConfiguracionCuentaBancoCorredora.seleccionado.IdCuentaBanco || 0);				
				objInfoFormaPago.IdTipoCuentaBancariaCorredora 		= scope.data.oConfiguracionCuentaBancoCorredora.seleccionado.IdTipoCuentaBancaria || '';				
				objInfoFormaPago.NombreTipoCuentaBancariaCorredora 	= scope.data.oConfiguracionCuentaBancoCorredora.seleccionado.NombreTipoCuentaBancaria || '';						
				objInfoFormaPago.NombreBancoCorredora				= scope.data.oConfiguracionCuentaBancoCorredora.seleccionado.NombreBanco || '';
				objInfoFormaPago.NombreCuentaBancariaCorredora		= scope.data.oConfiguracionCuentaBancoCorredora.seleccionado.NombreCuentaBancaria || '';
				objInfoFormaPago.NumeroCuentaBancariaCorredora		= scope.data.oConfiguracionCuentaBancoCorredora.seleccionado.NumeroCuentaBancaria || '';
				objInfoFormaPago.IdChequera 			            = scope.data.oConfiguracionChequerasSeries.seleccionado.IdChequera || 0;				
				objInfoFormaPago.NumeroDocumentoCorredora			= scope.data.FolioDocumentoCorredora || 0;
				objInfoFormaPago.NumeroSerieCorredora				= scope.data.oConfiguracionChequerasSeries.seleccionado.Serie || '';
				objInfoFormaPago.IdTarjetaBancariaCorredora 		= 0;
				objInfoFormaPago.FechaDocumentoPagoCorredora 		= '0001-01-01'

					

				//SOCIO NEGOCIO
				objInfoFormaPago.NumeroCuentaBancariaSocio			= scope.data.NumeroCuentaSocio || '';
				objInfoFormaPago.IdCuentaBancariaSocio				= scope.data.oConfiguracionCuentaBancoSocioNegocio.seleccionado.IdBanco || 0;
				
				if (objInfoFormaPago.IdTipoMovimiento === "MOVING")
				{
					objInfoFormaPago.IdBancoSocio			    		= scope.data.oConfiguracionBancoCuentaSocio.seleccionado.IdBanco || 0;
					objInfoFormaPago.NombreBancoSocio 					= scope.data.oConfiguracionBancoCuentaSocio.seleccionado.NombreBanco || '';
				}
				else
				{
					objInfoFormaPago.IdBancoSocio			    		= scope.data.oConfiguracionCuentaBancoSocioNegocio.seleccionado.IdBanco || 0;
					objInfoFormaPago.NombreBancoSocio 					= scope.data.oConfiguracionCuentaBancoSocioNegocio.seleccionado.NombreBanco || '';	
				}
				
				objInfoFormaPago.FechaDocumentoPagoSocio 			= (scope.data.kngFechaDocumentoSocio)? scope.data.kngFechaDocumentoSocio :  '0001-01-01';
				objInfoFormaPago.NumeroDocumentoSocio 				= scope.data.FolioDocumentoSocio || 0;
				objInfoFormaPago.NumeroSerieSocio 					= scope.data.SerieDocumentoSocio || '';
				objInfoFormaPago.IdCuentaBancariaSocio 				= Number(scope.data.oConfiguracionCuentaBancoSocioNegocio.seleccionado.IdCuentaBancaria || 0);			
				objInfoFormaPago.IdTipoCuentaBancariaSocio 			= scope.data.oConfiguracionCuentaBancoSocioNegocio.seleccionado.IdTipoCuentaBancaria || '';
				objInfoFormaPago.NombreTipoCuentaBancariaSocio 		= scope.data.oConfiguracionCuentaBancoSocioNegocio.seleccionado.NombreTipoCuentaBancaria || '';
				objInfoFormaPago.NombreCuentaBancariaSocio			= scope.data.oConfiguracionCuentaBancoSocioNegocio.seleccionado.NombreCuentaBancariaFull || '';
				objInfoFormaPago.IdTarjetaBancariaSocio 		    = 0;
				objInfoFormaPago.IdTipoTarjetaBancariaSocio 		= '';
				objInfoFormaPago.NombreTipoTarjetaBancariaSocio 	= '';
				objInfoFormaPago.NumeroTarjetaBancariaSocio 		= '';

				//INDICADORES
				objInfoFormaPago.GeneraNomina						= scope.data.GeneraNomina;
				objInfoFormaPago.GeneraTransferencia				= scope.data.GeneraTransferencia;
				objInfoFormaPago.UsaTalonarioCheque					= scope.data.UsaTalonarioCheque;

				//scope.limpiarFormaPago();
				return objInfoFormaPago;
			};

			//Que se debe registrar según el medio de pago y tipo movimiento
			scope.configuracionFormaPago = function()
			{
				scope.resultDirectiva = scope.resultDirective();
				if (scope.validarFormaPago()){
					var objFormaPago = {};

					//Seteo inicial
					objFormaPago.IdMedioDePago						= scope.resultDirectiva.IdMedioDePago;
					objFormaPago.NombreMedioDePago					= scope.resultDirectiva.NombreMedioDePago;
					objFormaPago.TipoMedioDePago  					= scope.resultDirectiva.TipoMedioDePago;
					objFormaPago.IdModalidadDePago					= scope.resultDirectiva.IdModalidadDePago;			
					objFormaPago.NombreModalidadDePago				= scope.resultDirectiva.NombreModalidadDePago;				
					objFormaPago.IdMoneda							= scope.resultDirectiva.IdMoneda;
					objFormaPago.NombreMoneda						= scope.resultDirectiva.NombreMoneda;
					objFormaPago.Monto								= scope.resultDirectiva.Monto;
					objFormaPago.IdComprobante						= scope.resultDirectiva.IdComprobante;
					objFormaPago.IdTipoMovimiento					= scope.resultDirectiva.IdTipoMovimiento;
					objFormaPago.IdCaja								= scope.resultDirectiva.IdCaja;
					objFormaPago.CodigoCaja							= scope.resultDirectiva.CodigoCaja;
					objFormaPago.NombreCaja							= scope.resultDirectiva.NombreCaja;
					objFormaPago.IdComprobanteFormaDePago			= scope.resultDirectiva.IdComprobanteFormaDePago;   // Para Comprobantes					
					objFormaPago.IdIngresoEgresoFormaDePago			= scope.resultDirectiva.IdIngresoEgresoFormaDePago; // Para Movimientos					
					objFormaPago.IdCuentaBancariaCorredora 			= 0;
					objFormaPago.IdTipoCuentaBancariaCorredora 		= '';
					objFormaPago.NombreTipoCuentaBancariaCorredora 	= '';  
					objFormaPago.NombreCuentaBancariaCorredora		= '';
					objFormaPago.NumeroCuentaBancariaCorredora		= '';				
					objFormaPago.IdChequera 			            = 0;				
					objFormaPago.NumeroDocumentoCorredora			= 0;
					objFormaPago.NumeroSerieCorredora				= '';
					objFormaPago.IdCuentaBancariaSocio 				= 0;
					objFormaPago.IdTipoCuentaBancariaSocio 			= '';
					objFormaPago.NombreTipoCuentaBancariaSocio 		= '';  
					objFormaPago.NombreCuentaBancariaSocio			= '';
					objFormaPago.NumeroCuentaBancariaSocio			= '';	
					objFormaPago.FechaDocumentoPagoSocio    	    = '0001-01-01';
					objFormaPago.IdBancoSocio						= 0;				
					objFormaPago.NombreBancoSocio                   = '';
					objFormaPago.NumeroDocumentoSocio				= 0;
					objFormaPago.NumeroSerieSocio					= '';				
					objFormaPago.IdComprobantePartida               = 0;
					objFormaPago.IdTarjetaBancariaCorredora 		= 0;				
					objFormaPago.IdTarjetaBancariaSocio 		    = 0;
					objFormaPago.IdTipoTarjetaBancariaSocio 		= '';
					objFormaPago.NombreTipoTarjetaBancariaSocio 	= '';
					objFormaPago.NumeroTarjetaBancariaSocio 		= '';
					objFormaPago.FechaDocumentoPagoCorredora 		= '0001-01-01'
					objFormaPago.NombreBancoCorredora				= '';
					objFormaPago.GeneraNomina 						= scope.resultDirectiva.GeneraNomina;			
					objFormaPago.GeneraTransferencia 				= scope.resultDirectiva.GeneraTransferencia; 
					

					//Ingreso
					if (scope.resultDirectiva.IdTipoMovimiento === "MOVING"){
						//Si la modalidad es "Depósito" se asignan los datos de la cuenta bancaria de la corredora
						if (scope.resultDirectiva.IdModalidadDePago === "MODDEP"){	

							objFormaPago.IdCuentaBancariaCorredora 			= scope.resultDirectiva.IdCuentaBancariaCorredora;
							objFormaPago.IdTipoCuentaBancariaCorredora 		= scope.resultDirectiva.IdTipoCuentaBancariaCorredora;
							objFormaPago.NombreTipoCuentaBancariaCorredora 	= scope.resultDirectiva.NombreTipoCuentaBancariaCorredora;
							objFormaPago.NombreBancoCorredora				= scope.resultDirectiva.NombreBancoCorredora;
							objFormaPago.NombreCuentaBancariaCorredora		= scope.resultDirectiva.NombreCuentaBancariaCorredora;
							objFormaPago.NumeroCuentaBancariaCorredora		= scope.resultDirectiva.NumeroCuentaBancariaCorredora;
						}

						objFormaPago.NumeroCuentaBancariaSocio		= scope.resultDirectiva.NumeroCuentaBancariaSocio;					
						objFormaPago.IdBancoSocio			    	= scope.resultDirectiva.IdBancoSocio;
						objFormaPago.NombreBancoSocio 				= scope.resultDirectiva.NombreBancoSocio;
						objFormaPago.FechaDocumentoPagoSocio 		= scope.resultDirectiva.FechaDocumentoPagoSocio;
						objFormaPago.NumeroDocumentoSocio 			= scope.resultDirectiva.NumeroDocumentoSocio;
						objFormaPago.NumeroSerieSocio 				= scope.resultDirectiva.NumeroSerieSocio;
					}  //Fin Ingreso


					//Egreso
					if (scope.resultDirectiva.IdTipoMovimiento === "MOVEGR"){
						switch (scope.resultDirectiva.TipoMedioDePago)
						{
							case "CIN":  //Cuenta Inversión (No guarda datos de caja ni de cta. bancaria)
								//ds.add(objFormaPago);
								break;
							default:     // Otros medios de pago					

								//Datos de la cuenta bancaria de la corredora					
								objFormaPago.IdCuentaBancariaCorredora 			= scope.resultDirectiva.IdCuentaBancariaCorredora;
								objFormaPago.IdTipoCuentaBancariaCorredora 		= scope.resultDirectiva.IdTipoCuentaBancariaCorredora;
								objFormaPago.NombreTipoCuentaBancariaCorredora 	= scope.resultDirectiva.NombreTipoCuentaBancariaCorredora;
								objFormaPago.NombreBancoCorredora				= scope.resultDirectiva.NombreBancoCorredora;
								objFormaPago.NombreCuentaBancariaCorredora		= scope.resultDirectiva.NombreCuentaBancariaCorredora;
								objFormaPago.NumeroCuentaBancariaCorredora		= scope.resultDirectiva.NumeroCuentaBancariaCorredora;
								objFormaPago.IdChequera 			            = scope.resultDirectiva.IdChequera;
								objFormaPago.NumeroDocumentoCorredora			= scope.resultDirectiva.NumeroDocumentoCorredora;
								objFormaPago.NumeroSerieCorredora				= scope.resultDirectiva.NumeroSerieCorredora;


								if (scope.resultDirectiva.IdModalidadDePago === "MODDEP"){	
									//Datos de la cuenta bancaria del socio de negocio								
									objFormaPago.IdCuentaBancariaSocio 			= scope.resultDirectiva.IdCuentaBancariaSocio;
									objFormaPago.IdBancoSocio					= scope.resultDirectiva.IdBancoSocio;
									objFormaPago.IdTipoCuentaBancariaSocio 		= scope.resultDirectiva.IdTipoCuentaBancariaSocio;
									objFormaPago.NombreTipoCuentaBancariaSocio 	= scope.resultDirectiva.NombreTipoCuentaBancariaSocio;
									objFormaPago.NumeroCuentaBancariaSocio		= scope.resultDirectiva.NumeroCuentaBancariaSocio;
									objFormaPago.NombreBancoSocio 				= scope.resultDirectiva.NombreBancoSocio;
								}
								break;
						}
					}   //Fin Egreso

					scope.limpiarFormaPago();
					return objFormaPago;
				}
			};

			//Valida que se ingresen los datos necesarios a la forma de pago, según cada medio de pago,en base a los datos retornados por la directiva. 
			scope.validarFormaPago = function()
			{	
				//Movimiento debe estar asociado a una caja
				if (Number(scope.objFormaDePago[0].IdCaja) === 0 && scope.objFormaDePago[0].TipoEmpleado === 'BACKOFFICE')
				{	
					bcsComunes.msg("error","error",$translate.instant('UI_USUARIO_SIN_REALIZAR_APERTURA_CAJA'));
					return(false);
				}
			
				//Valida ingreso del medio de pago
				if (typeof scope.data.oConfiguracionMedioDePago.seleccionado.IdMedioDePago === 'undefined')
				{	
					bcsComunes.msg("warning","warning",$translate.instant('UI_DEBE_SELECCIONAR_MEDIO_PAGO'));
					return(false);
				}

				//Si no se paga con "Cta. Inversión", se valida la "Modalidad del pago"
				if (scope.data.oConfiguracionMedioDePago.seleccionado.TipoMedioDePago !== "CIN")
				{
					//Valida que se ingrese modalidad del Pago(Caja / Depósito)(No aplica para Cuenta Inversión)
					if (typeof scope.data.oConfiguracionModalidadPago.seleccionado.IdModalidadDePago === 'undefined')
					{
						bcsComunes.msg("warning","warning",$translate.instant('UI_DEBE_SELECCIONAR_MODALIDAD_PAGO'));				
						return(false);
					}
				}
				
				if (scope.objFormaDePago[0].TipoMovimiento !== 'PAREGR'){
				
					//Valida ingreso del monto
					if (typeof scope.data.kngMontoFormaPago === 'undefined' || scope.data.kngMontoFormaPago === 0 || scope.data.kngMontoFormaPago === ''|| scope.data.kngMontoFormaPago === null)
					{	
						bcsComunes.msg("warning","warning",$translate.instant('UI_DEBE_INGRESAR_MONTO'));
						return(false);
					}

					//Calcula el total de las formas de pago
					/*sumMontoFormaDePago = Number(scope.saldoPorAsignar) - Number(scope.data.kngMontoFormaPago);		
					if (sumMontoFormaDePago < 0)
					{
						bcsComunes.msg("warning","warning",$translate.instant('UI_MONTO_FORMA_PAGO_EXCEDE_MONTO_MOVIMIENTO'));
						return(false);
					}*/
				}

				/*** (IMPCREDOPT-3212) No debe ser obligatorio el ingreso se los datos de la cuenta bancaria ***
				if (scope.data.oConfiguracionModalidadPago.seleccionado.IdModalidadDePago === "MODDEP" &&
					scope.objFormaDePago[0].TipoMovimiento !== "PAREGR" &&
					scope.data.oConfiguracionCuentaBancoCorredora.seleccionado.IdCuentaBanco === undefined &&
					scope.objFormaDePago[0].TipoEmpleado === 'BACKOFFICE'){

					bcsComunes.msg("warning","warning",$translate.instant('UI_DEBE_SELECCIONAR_CUENTA_BANCARIA_CORREDORA'));
					return(false);
				}
				**********************/

				/***  (IMPCREDOPT-3212) No debe ser obligatorio el ingreso se los datos de la cuenta bancaria ***
				if (scope.objFormaDePago[0].IdTipoMovimiento === "MOVEGR" &&
					scope.objFormaDePago[0].TipoMovimiento !== "PAREGR" &&
					scope.data.oConfiguracionModalidadPago.seleccionado.IdModalidadDePago === "MODDEP" &&
					scope.data.oConfiguracionCuentaBancoSocioNegocio.seleccionado.IdCuentaBancaria === undefined){

					bcsComunes.msg("warning","warning",$translate.instant('UI_DEBE_SELECCIONAR_CUENTA_BANCARIA_SOCIO_NEGOCIO'));
					return(false);
				}
				**********************/

				
				if (scope.objFormaDePago[0].IdTipoMovimiento === "MOVEGR" &&
					scope.objFormaDePago[0].TipoMovimiento === "PAREGR" &&
					scope.data.oConfiguracionModalidadPago.seleccionado.IdModalidadDePago === "MODDEP" &&
					scope.data.oConfiguracionCuentaBancoCorredora.seleccionado.IdCuentaBanco === undefined)
				{

						bcsComunes.msg("warning","warning",$translate.instant('UI_DEBE_SELECCIONAR_CUENTA_BANCARIA_CORREDORA'));
						return(false);
				}
				if(scope.data.permiteCuentaBancaria === 'S'){
					if (scope.objFormaDePago[0].TipoMovimiento !== "PAREGR" && 
							scope.data.oConfiguracionMedioDePago.seleccionado.TipoMedioDePago === "TRA" && 
							(scope.resultDirectiva.NumeroCuentaBancariaSocio === "" ) ) {
						bcsComunes.msg("warning", "warning", $translate.instant('UI_DEBE_SELECCIONAR_CUENTA_BANCARIA_SOCIO_NEGOCIO'));
						return(false);
					}
				}
			

				
				
				return(true);
			};

			//Limpia los datos disabled de la forma de pago
			scope.limpiarFormaPago = function()
			{
				scope.data.oConfiguracionMedioDePago.idActual = 0;
				scope.data.oConfiguracionModalidadPago.idActual = 0;
				scope.data.kngMontoFormaPago          = 0;				
				scope.data.IdComprobanteFormaDePago   = 0;  // Para Comprobantes				
				scope.data.IdIngresoEgresoFormaDePago = 0;  // Para Movimientos
				
				scope.limpiarDatosDisabled();
			};

			//Limpia las variables show del formulario
			scope.limpiarVariablesShow = function()
			{
				scope.data.showDatosCuentaBancariaCorredoraOrigen 	= false;
				scope.data.showDatosCuentaBancariaCorredoraDestino  = false;
				scope.data.showDatosCuentaBancariaSocioOrigen	= false;
				scope.data.showDatosCuentaBancariaSocioDestino  = false;

				scope.data.showDatosCaja  = false;

				scope.data.showDatosOrigen = false;
				scope.data.showDatosDestino = false;
				scope.data.showConfiguracionPartida = false;
				scope.data.showFolioDocumentoCorredora = false;
				scope.data.showSerieDocumentoCorredora = false;

				scope.data.showBancoCuentaSocio		= false;
				scope.data.showNumeroCuentaSocio	= false;
				scope.data.showFechaDocumentoSocio	= false;
				scope.data.showSerieDocumentoSocio	= false;
				scope.data.showFolioDocumentoSocio	= false;
			};

			scope.limpiarDatosDisabled = function()
			{
				//Limpia datos de la "Caja"
				scope.data.NombreCaja = '';	
				scope.data.CodigoCaja = '';
				scope.data.MonedaCaja = '';

				//Limpia datos de la cuenta bancaria de la corredora
				scope.data.oConfiguracionCuentaBancoCorredora.idActual = undefined;
				scope.data.oConfiguracionChequerasSeries.idActual = undefined;
				scope.data.TipoCuentaCorredora = '';
				scope.data.BancoCuentaCorredora = '';
				scope.data.NumeroCuentaCorredora = '';
				scope.data.MonedaCuentaCorredora = '';			
				scope.data.FolioDocumentoCorredora = 0;
				scope.data.SerieDocumentoCorredora = '';			
				scope.data.IdChequera = 0;			

				//Limpia datos de la cuenta bancaria del socio de negocio
				scope.data.oConfiguracionCuentaBancoSocioNegocio.idActual = 0;
				scope.data.TipoCuentaSocio = '';
				scope.data.BancoCuentaSocio = '';
				scope.data.NumeroCuentaSocio = '';
				scope.data.MonedaCuentaSocio = '';

				//Limpia datos de la cuenta corriente socio origen
				scope.data.oConfiguracionBancoCuentaSocio.idActual = 0;
				scope.data.oConfiguracionBancoCuentaSocio.seleccionado = false;
				scope.data.NumeroCuentaSocio = '';			
				scope.data.kngFechaDocumentoSocio = util.dateInicioDia($rootScope.SesionFechaSistema);
				scope.data.FolioDocumentoSocio = 0;
				scope.data.SerieDocumentoSocio = '';

				//Limpia configuracion
				scope.data.GeneraNomina		= false;
				scope.data.GeneraTransferencia	= false;
				scope.data.UsaTalonarioCheque	= false;

				//Oculta paneles de forma de pago		
				scope.limpiarVariablesShow();
			};

		}
	}
});