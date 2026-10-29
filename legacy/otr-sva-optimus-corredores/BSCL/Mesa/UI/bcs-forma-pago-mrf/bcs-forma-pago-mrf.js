angular.module('ModuloMesa').directive('bcsFormaPagoMrf', function (bcsComunes, $resource, $timeout, $rootScope, $q, bcsServiciosGrillasMesaRF,bcsServiciosMesa, $translate) {
	return {
		restrict: 'E',
		replace: false,
		scope: {
			idPortafolio: '=?',
			idPersona: '=?',
			idTipoProducto: '=?',
			idTipoOperacionPrincipal: '=?',
			idTipoOperacionSecundaria: '=?',
			montoOperacionMonedaPrincipal: '=?',
			monedaPrincipal: '=?',
			montoOperacionMonedaSecundaria: '=?',
			monedaSecundaria: '=?',
			fechaOperacion: '=?',
			limpiarDatos: '=?',
			activado: '=?',
			idOperacion: '=?',
			operaCuentaInversion: '=?',
			formaDePagoEntregamos: '=?',
			formaDePagoRecibimos: '=?',
			montoAsignadoRecibimos: '=?',
			montoAsignadoEntregamos: '=?',
			recarga: '=?',
			formaDePago: '=?',
			fechaVencimiento: '=?',
			fechaLiquidacion: '=?',
			idPortafolioCorredora: '=?',
			accionOperacion: '=?',
			idEstado: '=?'
		
		},
		templateUrl: '../../Mesa/UI/bcs-forma-pago-mrf/bcs-forma-pago-mrf.html',
		link: function (scope, element, attrs, fn) {

		
			/*******************************************************************
			 * DEFINICION DE VARIABLES
			 ******************************************************************/
			scope.numeroIdInternoOperacion = 0;
			scope.formaDePago = new Map();
			scope.formaDePagoArray = [];
			scope.formaDePagoPorDefecto = {}
			scope.formaDePagoPorDefectoEnt = {}
			scope.formaDePagoPorDefectoRec = {}
			scope.formaDePagoArrayEntregamos = [];
			scope.formaDePagoArrayRecibimos = [];
			scope.cantidadFormasDePago;
			scope.activaFPClienteEnt = true;
			scope.activaFPCorredoraEnt = false;
			scope.activaFPClienteRec = true;
			scope.activaFPCorredoraRec = false;
			scope.maxMediosPagoRec = false;
			scope.maxMediosPagoEnt = false;
			scope.noPermiteCuentaInversion = false;
			scope.corredoraPermiteCuentaInversion = false;
			scope.datosModificados=false;
			scope.datos={};
			
			var consultaFormasdePagoCliEnt = bcsServiciosGrillasMesaRF.getDataSourceFormasPagoPortaCliente();
			var consultaFormasdePagoCliRec = bcsServiciosGrillasMesaRF.getDataSourceFormasPagoPortaCliente();
			
			var consultaFormasdePagoCorCliEnt = bcsServiciosGrillasMesaRF.getDataSourceFormasPagoPortaCliente();
			var consultaFormasdePagoCorCliRec = bcsServiciosGrillasMesaRF.getDataSourceFormasPagoPortaCliente();
			
			var consultaFormasdePagoCorEnt = bcsServiciosGrillasMesaRF.getDataSourceFormasPagoMesa();
			var consultaFormasdePagoCorRec = bcsServiciosGrillasMesaRF.getDataSourceFormasPagoMesa();
			
			var consultaPreferenciaPortafolio = bcsServiciosGrillasMesaRF.getDataSourcePreferenciaPortafolio();
			var consultarComprobantePagoOperacion = bcsServiciosGrillasMesaRF.getDataSourceComprobantePagoOperacionMesaRF();

			/*******************************************************************
			 * DEFINICION DE PARAMETROS
			 ******************************************************************/
			var defConfiguracionMesa =
			{
				type: "odata",
				definition:
				{
					serverFiltering: true, // Filtrado de datos en el servid or.
					serverSorting: true, // Ordenamiento de los datos en el
											// servidor.
					sort: { field: "", dir: "DESC" },
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=432",
							dataType: "json"
						}
					},
					schema: {
						model:
						{
							fields:
							{
								IdEmpresa: { type: "int" },
								IdTipoProducto: { type: "string" },
								CodigoParametro: { type: "string" },
								Descripcion: { type: "string" },
								Valor: { type: "string" },
							}
						}
					}

				}
			};
			var dsConfiguracionMesa = new bcs.DataSource(defConfiguracionMesa);

			var filtroConfiguracionMesa = new Array();
			filtroConfiguracionMesa.push({ field: "CodigoParametro", operator: "eq", value: 'MAXIMO_INTRUCCIONES_FORMAS_PAGO' });
			dsConfiguracionMesa.query({ filter: filtroConfiguracionMesa }).then(function () {
				let resultado = dsConfiguracionMesa.data();
				scope.cantidadFormasDePago = resultado[0].Valor
			});


			/*******************************************************************
			 * CONFIGURACION COMBOS
			 ******************************************************************/

			scope.oConfPreferenciaPagoClienteEnt = {
				tipo: "combobox-forma-pago-cliente",
				configuracion: {
					placeholder: $translate.instant("UI_FORMA_DE_PAGO"),
					template: '<span >#: Descripcion # </span>',
					
				},
				configuracion: {},
				idActual: false,
				idDefecto: false,
				seleccionado: false,
				itemPostFiltro: false,
				filtros: {
					IdPortafolio: { "field": "IdPortafolio", "operator": "eq", "value": scope.idPortafolio || 0 },
					IdMoneda: { "field": "IdMoneda", "operator": "eq", "value": scope.monedaPrincipal || "CLP" },
					RecibePagaPortafolio: { "field": "RecibePagaPortafolio", "operator": "eq", "value": "P" },
					IdTipoOperacion: { "field": "IdTipoOperacion", "operator": "eq", "value": scope.idTipoOperacionPrincipal || "" },
					IdEstado: { "field": "IdEstado", "operator": "eq", "value": 'ACT' },
					IdTipoProducto: { "field": "IdTipoProducto", "operator": "eq", "value": 'MERF' }
				},
			};

			scope.oConfPreferenciaPagoClienteRec = {
				tipo: "combobox-forma-pago-cliente",
				configuracion: {
					placeholder: $translate.instant("UI_FORMA_DE_PAGO"),
					template: '<span >#: Descripcion # </span>',
					
				},
				configuracion: {},
				idActual: false,
				idDefecto: false,
				seleccionado: false,
				itemPostFiltro: false,
				
				filtros: {
					IdPortafolio: { "field": "IdPortafolio", "operator": "eq", "value": scope.idPortafolio || 0 },
					IdMoneda: { "field": "IdMoneda", "operator": "eq", "value": scope.monedaPrincipal || "CLP" },
					RecibePagaPortafolio: { "field": "RecibePagaPortafolio", "operator": "eq", "value": "R" },
					IdTipoOperacion: { "field": "IdTipoOperacion", "operator": "eq", "value": scope.idTipoOperacionPrincipal || "" },
					IdEstado: { "field": "IdEstado", "operator": "eq", "value": 'ACT' },
					IdTipoProducto: { "field": "IdTipoProducto", "operator": "eq", "value": 'MERF' }
				},
			};



			scope.oConfPreferenciaPagoCorredoraEnt = {
				tipo: "combobox-forma-pago-cliente",
				configuracion: {
					placeholder: $translate.instant("UI_FORMA_DE_PAGO"),
					template: '<span >#: Descripcion # </span>'
				},
				configuracion: {},
				idActual: false,
				idDefecto: false,
				seleccionado: false,
				itemPostFiltro: false,
				filtros: {
					IdPortafolio: { "field": "IdPortafolio", "operator": "eq", "value": scope.idPortafolioCorredora || 0 },
					IdMoneda: { "field": "IdMoneda", "operator": "eq", "value": scope.monedaPrincipal || "CLP" },
					RecibePagaPortafolio: { "field": "RecibePagaPortafolio", "operator": "eq", "value": "P" },
					IdTipoOperacion: { "field": "IdTipoOperacion", "operator": "eq", "value": scope.idTipoOperacionPrincipal || "" },
					IdEstado: { "field": "IdEstado", "operator": "eq", "value": 'ACT' },
					IdTipoProducto: { "field": "IdTipoProducto", "operator": "eq", "value": 'MERF' }
				}
			}

			scope.oConfPreferenciaPagoCorredoraRec = {
				tipo: "combobox-forma-pago-cliente",
				configuracion: {
					placeholder: $translate.instant("UI_FORMA_DE_PAGO"),
					template: '<span >#: Descripcion # </span>'
				},
				configuracion: {},
				idActual: false,
				idDefecto: false,
				seleccionado: false,
				itemPostFiltro: false,
				filtros: {
					IdPortafolio: { "field": "IdPortafolio", "operator": "eq", "value": scope.idPortafolioCorredora || 0 },
					IdMoneda: { "field": "IdMoneda", "operator": "eq", "value": scope.monedaPrincipal || "CLP" },
					RecibePagaPortafolio: { "field": "RecibePagaPortafolio", "operator": "eq", "value": "R" },
					IdTipoOperacion: { "field": "IdTipoOperacion", "operator": "eq", "value": scope.idTipoOperacionPrincipal || "" },
					IdEstado: { "field": "IdEstado", "operator": "eq", "value": 'ACT' },
					IdTipoProducto: { "field": "IdTipoProducto", "operator": "eq", "value": 'MERF' }
				}
			}

			/*******************************************************************
			 * CONFIGURACION GRILLAS
			 ******************************************************************/

			scope.getSchemaFormasPago = function() {
				return { 
					model: {
						fields : {
							IdFormaDePago				: { type : "string"},
							NombreFormaDePago			: { type : "string"},
							Dias						: { type : "number"},	
							PorDefecto					: { type : "string"},
							RecibePagaPortafolioDesc	: { type : "string"},
							IdPreferenciaPago			: { type : "string"},
							IdTipoOperacion				: { type : "string"},
							IdMedioDePago				: { type : "string"},
							IdMoneda					: { type : "string"},
							UsaPersonaRelacionada		: { type : "string"},
							FechaCreacion				: { type : "sysdate"},
							FechaModificacion			: { type : "sysdate"},
							IdUsuarioCreador			: { type : "number"},
							IdEstado					: { type : "string"},
							IdEmpresa					: { type : "number"},
							NombreTipoOperacion			: { type : "string"},
							IdTipoProducto				: { type : "string"},
							NombreTipoProducto			: { type : "string"},
							RecibePagaPortafolio		: { type : "string"},
							NombreMedioDePago			: { type : "string"},
							NombreMoneda				: { type : "string"},
							UsaCuentaBancaria			: { type : "string"},
							UsaTarjetaBancaria			: { type : "string"},
							NombreBanco					: { type : "string"},
							FechaLiquidacion			: {	type : "sysdate"},
							NombreEstado				: { type : "string"},
							Monto						: { type : "number"},
							CuentaBancaria				: {	type :"string"},
							IdComprobantePagoOperacion	: { type  : "numbrer"}
						}
					}
				}
			};

			scope.columnasFormasDePagoEntregamos = function() {
	
				return  [
					{field: "NombreFormaDePago"					, title: $translate.instant('UI_FORMA_PAGO_ENTREGAMOS'), width: "80px" ,align: "left"},
					{field: "NombreMoneda"					, title: $translate.instant('UI_MONEDA'), width: "80px" ,align: "left"},
					{field: "NombreBanco"					, title: $translate.instant('UI_BANCO'), width: "70px" ,align: "left"},
					{field: "CuentaBancaria"					, title: $translate.instant('UI_CUENTA_BANCARIA'), width: "70px" ,align: "left"},
					{field: "FechaLiquidacion"					, title: $translate.instant('UI_FECHA_LIQUIDACION'), width: "70px" ,align: "left"},	
					{field: "Monto"					, title: $translate.instant('UI_MONTO'), width: "70px" ,align: "left",
					template_ : " <label> #:  (util.bcs_formato(MontoCompra,'GEN','Monto',scope.monedaPrincipal,'')).valor #</label>",
					format : function(value, dataItem) {
						return util.bcs_formato(value,'GEN','Monto',scope.monedaPrincipal,scope.monedaPrincipal,'');
					}	},	
					
				];
			};

			scope.columnasFormasDePagoRecibimos = function() {
	
				return  [
					{field: "NombreFormaDePago"					, title: $translate.instant('UI_FORMA_PAGO_RECIBIMOS'), width: "80px" ,align: "left"},
					{field: "NombreMoneda"					, title: $translate.instant('UI_MONEDA'), width: "80px" ,align: "left"},
					{field: "NombreBanco"					, title: $translate.instant('UI_BANCO'), width: "70px" ,align: "left"},
					{field: "CuentaBancaria"					, title: $translate.instant('UI_CUENTA_BANCARIA'), width: "70px" ,align: "left"},
					{field: "FechaLiquidacion"					, title: $translate.instant('UI_FECHA_LIQUIDACION'), width: "70px" ,align: "left"},	
					{field: "Monto"					, title: $translate.instant('UI_MONTO'), width: "70px" ,align: "left",
					template_ : " <label> #:  (util.bcs_formato(MontoCompra,'GEN','Monto',scope.monedaPrincipal,'')).valor #</label>",
					format : function(value, dataItem) {
						return util.bcs_formato(value,'GEN','Monto',scope.monedaPrincipal,scope.monedaPrincipal,'');
					}	},	
					
				];
			};

			var dsFormasPagoRecibimos = new bcs.DataSource({
				type		: "call",
				definition	: {
										
					pageSize		: 10,
					schema : scope.getSchemaFormasPago()
				}
			});

			var dsFormasPagoEntregamos = new bcs.DataSource({
				type		: "call",
				definition	: {
										
					pageSize		: 10,
					schema : scope.getSchemaFormasPago()
				}
			});

			scope.confBasicaFormaDePagoRecibimos = {
				dataSource	: dsFormasPagoRecibimos,
				columns		: scope.columnasFormasDePagoRecibimos(),
				sortable	: true,
				pageable	: true,   //pagina localmente
				filterable 	: true,
				selectable 	: "row",
				reorderable	: true,
				userSettings : true,
				groupable	: true,
				height		: 150,
				allowCopy	: { delimeter: "\t" },
				resizable	: true,
				autoBind	: false,
				excel : {
					allPages: true,
					},
				gridCreated : function(grid) {
					grillaFormaPagoRecibimos = grid;
					
				},
				scrollable	: true
			};

			scope.confBasicaFormaDePagoEntregamos = {
				dataSource	: dsFormasPagoEntregamos,
				columns		: scope.columnasFormasDePagoEntregamos(),
				sortable	: true,
				pageable	: true,   //pagina localmente
				filterable 	: true,
				selectable 	: "row",
				reorderable	: true,
				userSettings : true,
				groupable	: true,
				height		: 150,
				
				resizable	: true,
				autoBind	: false,
				
				gridCreated : function(grid) {
					grillaFormaPagoEntregamos = grid;
					
				},
				scrollable	: true
			};

			scope.cargarFormasDePago = function (idOperacion) {
				let filtroPP = [];
				if (idOperacion != undefined && idOperacion != "" && idOperacion !=0) {
					filtroPP.push({ field: "IdOperacion", operator: "eq", value: idOperacion })
					consultarComprobantePagoOperacion.query({ filter: filtroPP }).then(function () {
						if (consultarComprobantePagoOperacion.data().length > 0) {

							$.each(consultarComprobantePagoOperacion.data(), function (key, value) {
								let formaPago = [{
									"IdComprobantePagoOperacion": value.IdComprobantePagoOperacion
									, "NombreFormaDePago": value.NombreFormaDePago
									, "NombreMoneda": value.IdMoneda
									, "NombreBanco":value.NombreBancoCuentaBancaria
									, "CuentaBancaria":value.NumeroCuentaBancaria
									, "FechaLiquidacion": util.dateSPdate(value.FechaLiquidacionComprobantePagoOperacion || '0001-01-01') 
									, "Monto": value.Monto
									
								}];
								


								
								if((scope.idTipoOperacionPrincipal==='VENPAC' || scope.idTipoOperacionPrincipal==='VENTER') && value.RecibePaga ==='P'){
									if(!(scope.confBasicaFormaDePagoEntregamos.dataSource.data().find( fmp => fmp.IdComprobantePagoOperacion == formaPago[0].IdComprobantePagoOperacion))){
										scope.confBasicaFormaDePagoEntregamos.dataSource.add(formaPago[0]);
										scope.numeroIdInternoOperacion +=1;
										
									}

									if(scope.confBasicaFormaDePagoEntregamos.dataSource.data().length>1){
										scope.formaDePagoEntregamos.push(value);			
			
									}else{
										scope.formaDePagoEntregamos=value;			
			
									}
									
								
								}else if((scope.idTipoOperacionPrincipal==='VENPAC' || scope.idTipoOperacionPrincipal==='VENTER') && value.RecibePaga ==='R'){
									
									if(!(scope.confBasicaFormaDePagoRecibimos.dataSource.data().find( fmp => fmp.IdComprobantePagoOperacion == formaPago[0].IdComprobantePagoOperacion))){
										scope.confBasicaFormaDePagoRecibimos.dataSource.add(formaPago[0]);	
										scope.numeroIdInternoOperacion +=1;
									}
									if(scope.confBasicaFormaDePagoRecibimos.dataSource.data().length>1){
										scope.formaDePagoRecibimos.push(value);				
									}else{
										scope.formaDePagoRecibimos=value;			
									}
								}
	
								if((scope.idTipoOperacionPrincipal==='COMPAC' || scope.idTipoOperacionPrincipal==='COMTER') && value.RecibePaga ==='P'){

									if(!(scope.confBasicaFormaDePagoRecibimos.dataSource.data().find( fmp => fmp.IdComprobantePagoOperacion == formaPago[0].IdComprobantePagoOperacion))){
										scope.confBasicaFormaDePagoRecibimos.dataSource.add(formaPago[0]);	
										scope.numeroIdInternoOperacion +=1;
									}
								
									if(scope.confBasicaFormaDePagoRecibimos.dataSource.data().length>1){
										scope.formaDePagoRecibimos.push(value);				
									}else{
										scope.formaDePagoRecibimos=value;			
									}

								}else if((scope.idTipoOperacionPrincipal==='COMPAC' || scope.idTipoOperacionPrincipal==='COMTER')  && value.RecibePaga ==='R'){
									if(!(scope.confBasicaFormaDePagoEntregamos.dataSource.data().find( fmp => fmp.IdComprobantePagoOperacion == formaPago[0].IdComprobantePagoOperacion))){
										scope.confBasicaFormaDePagoEntregamos.dataSource.add(formaPago[0]);
										scope.numeroIdInternoOperacion +=1;
										
									}
									if(scope.confBasicaFormaDePagoEntregamos.dataSource.data().length>1){
										scope.formaDePagoEntregamos.push(value);			
			
									}else{
										scope.formaDePagoEntregamos=value;			
			
									}

								}
								
							});
						}
					})
				}
			};
			scope.agregarFormaDePagoRecibimos = function (item){
				var datosParam = new Object();
				
				datosParam.p_IdEmpresa = $rootScope.SesionIdEmpresa;
				datosParam.p_IdUsuario = $rootScope.SesionIdUsuario;
				datosParam.p_IdModulo = 'MRF';
				datosParam.p_UsaReglasNegocio = "N";
				datosParam.p_FormaPagoNueva = 'S'
				let IdModalidadPago = '';
				if(item.IdMedioDePago==='CHE'||item.IdMedioDePago==='VVI'||item.IdMedioDePago==='EFE'||item.IdMedioDePago==='EBA'){
					IdModalidadPago = 'MODCAJ'
				}else{
					IdModalidadPago = 'MODDEP'
				}
				let montoAsignado=0;
				let fechaLiquidacion;
				if(scope.idTipoOperacionPrincipal === 'VENPAC' ||scope.idTipoOperacionPrincipal ==='VENTER'){
					montoAsignado = scope.montoOperacionMonedaPrincipal;
					fechaLiquidacion=util.dateSPdate(scope.fechaLiquidacion || '0001-01-01')
				}else{
					montoAsignado = scope.montoOperacionMonedaSecundaria;
					fechaLiquidacion=util.dateSPdate(scope.fechaVencimiento || '0001-01-01')

				}
				

				datosParam.p_ComprobantePagoOperacion = [{
					"AfectarTesoreria": item.IdMedioDePago==='CIN'?'N':'S'
					, "FechaIngresoComprobantePagoOperacion": util.dateSPdate(scope.fechaOperacion)
					, "FechaLiquidacionComprobantePagoOperacion": fechaLiquidacion
					, "IdAbono":0
					, "IdAbonoAnticipo": 0
					, "IdCargo": 0
					, "IdCargoAnticipo": 0
					, "IdComprobantePagoOperacion": 0
					, "IdCuentaBanco": item.IdCuentaBancaria
					, "IdEmpresa": item.IdEmpresa
					, "IdEstado": scope.idEstado
					, "IdFormaDePago": item.IdPreferenciaPago
					, "IdIngresoEgreso": 0
					, "IdIngresoEgresoAnticipo": 0
					, "IdInternoPagoOperacion": scope.numeroIdInternoOperacion + 1
					, "IdModalidadPago": IdModalidadPago
					, "IdMoneda": item.IdMoneda
					, "IdOperacion": scope.idOperacion
					, "IdTipoProducto": scope.idTipoProducto
					, "Monto": montoAsignado
					, "RecibePagaPortafolio": item.RecibePagaPortafolio
				}];
				bcsServiciosMesa.CrearFormaDePagoOperacionMesaRF(datosParam,function(respuesta){
					if (respuesta.P_OUT_ERRORESCONTADOR > 0){
						console.error('Error al agregar formas de pago',$translate.instant('UI_ERROR_AL_GRABAR'));
						var error = '';
						$.each(respuesta.P_OUT_MENSAJES,function(key,value) {
							if (value.Tipo === 'ERROR'){
								error = value.descripcion;
							}
						});
						bcsComunes.msg("warning",'Valorizador',$translate.instant('UI_ERROR_RUTINA_VALORIZACION') + ': ' + error);
						
					}else{
						var formaPago = {};
						$.each(respuesta.P_OUT_FORMADEPAGO,function(key,value) {
							scope.numeroIdInternoOperacion = scope.numeroIdInternoOperacion+1;
							formaPago.IdComprobantePagoOperacion = value.IdComprobantePagoOperacion;
							formaPago.NombreFormaDePago = value.NombreFormaDePago;
							formaPago.NombreMoneda = value.IdMoneda;
							formaPago.NombreBanco = value.NombreBancoCuentaBancaria
							formaPago.CuentaBancaria = value.NumeroCuentaBancaria;
							formaPago.FechaLiquidacion = value.FechaLiquidacionComprobantePagoOperacion;
							formaPago.Monto = value.Monto;
							if(scope.idTipoOperacionPrincipal === 'VENPAC' || scope.idTipoOperacionPrincipal === 'VENTER'){
								if(value.RecibePaga ==='R'){
									scope.confBasicaFormaDePagoRecibimos.dataSource.add(formaPago);
								}else{
									scope.confBasicaFormaDePagoEntregamos.dataSource.add(formaPago);
								}
							}

							if(scope.idTipoOperacionPrincipal === 'COMPAC' || scope.idTipoOperacionPrincipal === 'COMTER'){
								if(value.RecibePaga ==='P'){
									scope.confBasicaFormaDePagoEntregamos.dataSource.add(formaPago);
								}else{
									scope.confBasicaFormaDePagoRecibimos.dataSource.add(formaPago);
								}
							}
							
						});
						
						

						datosParam.p_ComprobantePagoOperacion[0].NombreFormaDePago = formaPago.NombreFormaDePago;
						datosParam.p_ComprobantePagoOperacion[0].CuentaBancaria = formaPago.CuentaBancaria;
						datosParam.p_ComprobantePagoOperacion[0].IdComprobantePagoOperacion = formaPago.IdComprobantePagoOperacion;
						scope.formaDePagoRecibimos = datosParam.p_ComprobantePagoOperacion;
							
						if(scope.confBasicaFormaDePagoRecibimos.dataSource.data().length>1){
							scope.formaDePagoRecibimos.push(datosParam.p_ComprobantePagoOperacion);		

						}else{
							scope.formaDePagoRecibimos = datosParam.p_ComprobantePagoOperacion;		

						}
						bcsComunes.msg("success",'Forma de Pago','Forma de pago ingresada correctamente.');

					}
					
				});
			}

			scope.agregarFormaDePagoEntregamos = function (item){
				var datosParam = new Object();
				
				datosParam.p_IdEmpresa = $rootScope.SesionIdEmpresa;
				datosParam.p_IdUsuario = $rootScope.SesionIdUsuario;
				datosParam.p_IdModulo = 'MRF';
				datosParam.p_UsaReglasNegocio = "N";
				datosParam.p_FormaPagoNueva = 'S'
				let IdModalidadPago = '';
				if(item.IdMedioDePago==='CHE'||item.IdMedioDePago==='VVI'||item.IdMedioDePago==='EFE'||item.IdMedioDePago==='EBA'){
					IdModalidadPago = 'MODCAJ'
				}else{
					IdModalidadPago = 'MODDEP'
				}
				let montoAsignado = 0;
				let fechaLiquidacion;
				if(scope.idTipoOperacionPrincipal === 'VENPAC' || scope.idTipoOperacionPrincipal ==='VENTER'){
					montoAsignado = scope.montoOperacionMonedaSecundaria;
					fechaLiquidacion=util.dateSPdate(scope.fechaVencimiento || '0001-01-01')
				}else{
					montoAsignado = scope.montoOperacionMonedaPrincipal;
					fechaLiquidacion=util.dateSPdate(scope.fechaLiquidacion || '0001-01-01')
				}
		
				datosParam.p_ComprobantePagoOperacion = [{
					"AfectarTesoreria": item.IdMedioDePago==='CIN'?'N':'S'
					, "FechaIngresoComprobantePagoOperacion": util.dateSPdate(scope.fechaOperacion)
					, "FechaLiquidacionComprobantePagoOperacion": fechaLiquidacion
					, "IdAbono":0
					, "IdAbonoAnticipo": 0
					, "IdCargo": 0
					, "IdCargoAnticipo": 0
					, "IdComprobantePagoOperacion": 0
					, "IdCuentaBanco": item.IdCuentaBancaria
					, "IdEmpresa": item.IdEmpresa
					, "IdEstado": scope.idEstado
					, "IdFormaDePago": item.IdPreferenciaPago
					, "IdIngresoEgreso": 0
					, "IdIngresoEgresoAnticipo": 0
					, "IdInternoPagoOperacion": scope.numeroIdInternoOperacion + 1
					, "IdModalidadPago": IdModalidadPago
					, "IdMoneda": item.IdMoneda
					, "IdOperacion": scope.idOperacion
					, "IdTipoProducto": scope.idTipoProducto
					, "Monto": montoAsignado
					, "RecibePagaPortafolio": item.RecibePagaPortafolio
				}];
				bcsServiciosMesa.CrearFormaDePagoOperacionMesaRF(datosParam,function(respuesta){
					if (respuesta.P_OUT_ERRORESCONTADOR > 0){
						console.error('Error al agregar formas de pago',$translate.instant('UI_ERROR_AL_GRABAR'));
						var error = '';
						$.each(respuesta.P_OUT_MENSAJES,function(key,value) {
							if (value.Tipo === 'ERROR'){
								error = value.descripcion;
							}
						});
						
					}else{
						var formaPago = {};
						$.each(respuesta.P_OUT_FORMADEPAGO,function(key,value) {
							scope.numeroIdInternoOperacion = scope.numeroIdInternoOperacion+1;
							formaPago.IdComprobantePagoOperacion = value.IdComprobantePagoOperacion;
							formaPago.NombreFormaDePago = value.NombreFormaDePago;
							formaPago.NombreMoneda = value.IdMoneda;
							formaPago.NombreBanco = value.NombreBancoCuentaBancaria
							formaPago.CuentaBancaria = value.NumeroCuentaBancaria;
							formaPago.FechaLiquidacion = value.FechaLiquidacionComprobantePagoOperacion;
							formaPago.Monto = value.Monto;
							if(scope.idTipoOperacionPrincipal === 'VENPAC' || scope.idTipoOperacionPrincipal === 'VENTER'){
								if(value.RecibePaga ==='R'){
									scope.confBasicaFormaDePagoRecibimos.dataSource.add(formaPago);
								}else{
									scope.confBasicaFormaDePagoEntregamos.dataSource.add(formaPago);
								}
							}

							if(scope.idTipoOperacionPrincipal === 'COMPAC' || scope.idTipoOperacionPrincipal === 'COMTER'){
								if(value.RecibePaga ==='P'){
									scope.confBasicaFormaDePagoEntregamos.dataSource.add(formaPago);
								}else{
									scope.confBasicaFormaDePagoRecibimos.dataSource.add(formaPago);
								}
							}

						});
						
						datosParam.p_ComprobantePagoOperacion[0].NombreFormaDePago = formaPago.NombreFormaDePago;
						datosParam.p_ComprobantePagoOperacion[0].CuentaBancaria = formaPago.CuentaBancaria;
						datosParam.p_ComprobantePagoOperacion[0].IdComprobantePagoOperacion = formaPago.IdComprobantePagoOperacion;
						if(scope.confBasicaFormaDePagoEntregamos.dataSource.data().length>1){
							scope.formaDePagoEntregamos.push(datosParam.p_ComprobantePagoOperacion);		

						}else{
							scope.formaDePagoEntregamos = datosParam.p_ComprobantePagoOperacion;		

						}
						bcsComunes.msg("success",'Forma de Pago','Forma de pago ingresada correctamente.');

					}
					
				});
			}

			scope.eliminarFormaDePagoEntregamos = function (formaDePagoEntregamos){

				var datosParam = new Object();
				datosParam.p_IdEmpresa = $rootScope.SesionIdEmpresa;
				datosParam.p_IdUsuario = $rootScope.SesionIdUsuario;
				datosParam.p_IdComprobantePagoOperacion = formaDePagoEntregamos.IdComprobantePagoOperacion;
				datosParam.p_NombreFormaDePago = formaDePagoEntregamos.NombreFormaDePago;
				bcsServiciosMesa.EliminarFormaDePagoOperacionMesaRF(datosParam,function(respuesta){
					if (respuesta.P_OUT_ERRORESCONTADOR > 0){
						console.error('Error al Eliminar formas de pago',$translate.instant('UI_ERROR_AL_GRABAR'));
						var error = '';
						$.each(respuesta.P_OUT_MENSAJES,function(key,value) {
							if (value.Tipo === 'ERROR'){
								error = value.descripcion;
							}
						});
						bcsComunes.msg("warning",'Valorizador',$translate.instant('UI_ERROR_RUTINA_VALORIZACION') + ': ' + error);
						
					}else{
											
						$.each(respuesta.P_OUT_COMPRABANTEPAGO,function(key,value) {
							scope.confBasicaFormaDePagoEntregamos.dataSource.data().forEach(fmp=>{
								if(fmp.IdComprobantePagoOperacion == value.IdComprobantePagoOperacion){
									scope.confBasicaFormaDePagoEntregamos.dataSource.data().remove(fmp);
									return;
								}
							});
							scope.formaDePagoEntregamos = scope.formaDePagoEntregamos.filter(function(item){
								return item.IdComprobantePagoOperacion !== value.IdComprobantePagoOperacion;
							});
						});	
						bcsComunes.msg("warning",'Forma de Pago','Forma de pago eliminada correctamente.');

					}
				});
			}

			scope.eliminarFormaDePagoRecibimos = function (formaDePagoRecibimos){

				var datosParam = new Object();

				datosParam.p_IdEmpresa = $rootScope.SesionIdEmpresa;
				datosParam.p_IdUsuario = $rootScope.SesionIdUsuario;
				datosParam.p_IdComprobantePagoOperacion = formaDePagoRecibimos.IdComprobantePagoOperacion;
				datosParam.p_NombreFormaDePago = formaDePagoRecibimos.NombreFormaDePago;
				bcsServiciosMesa.EliminarFormaDePagoOperacionMesaRF(datosParam,function(respuesta){
					if (respuesta.P_OUT_ERRORESCONTADOR > 0){
						console.error('Error al Eliminar formas de pago',$translate.instant('UI_ERROR_AL_GRABAR'));
						var error = '';
						$.each(respuesta.P_OUT_MENSAJES,function(key,value) {
							if (value.Tipo === 'ERROR'){
								error = value.descripcion;
							}
						});
						
					}else{
						
						
						$.each(respuesta.P_OUT_COMPRABANTEPAGO,function(key,value) {
							scope.confBasicaFormaDePagoRecibimos.dataSource.data().forEach(fmp=>{
								if(fmp.IdComprobantePagoOperacion == value.IdComprobantePagoOperacion){
									scope.confBasicaFormaDePagoRecibimos.dataSource.data().remove(fmp);
									return;
								}
							});
							scope.formaDePagoRecibimos = scope.formaDePagoRecibimos.filter(function(item){
								return item.IdComprobantePagoOperacion !== value.IdComprobantePagoOperacion;
							});
						});	
						bcsComunes.msg("warning",'Forma de Pago','Forma de pago eliminada correctamente.');
					}
				});
			}

			
			scope.$watch('idPortafolio', function (newval, oldval) {
				
				if (newval !== oldval && newval != 0 && newval) {
					scope.oConfPreferenciaPagoClienteEnt= {
						filtros: {
							IdPortafolio: { "field": "IdPortafolio", "operator": "eq", "value": scope.idPortafolio || 0 },
							IdMoneda: { "field": "IdMoneda", "operator": "eq", "value": scope.monedaPrincipal || "CLP" },
							RecibePagaPortafolio: { "field": "RecibePagaPortafolio", "operator": "eq", "value": "P" },
							IdTipoOperacion: { "field": "IdTipoOperacion", "operator": "eq", "value": scope.idTipoOperacionPrincipal || "" },
							IdEstado: { "field": "IdEstado", "operator": "eq", "value": 'ACT' },
							IdTipoProducto: { "field": "IdTipoProducto", "operator": "eq", "value": 'MERF' }
						}
					}
					
					scope.oConfPreferenciaPagoClienteRec= {
						filtros: {
							IdPortafolio: { "field": "IdPortafolio", "operator": "eq", "value": scope.idPortafolio || 0 },
							IdMoneda: { "field": "IdMoneda", "operator": "eq", "value": scope.monedaPrincipal || "CLP" },
							RecibePagaPortafolio: { "field": "RecibePagaPortafolio", "operator": "eq", "value": "R" },
							IdTipoOperacion: { "field": "IdTipoOperacion", "operator": "eq", "value": scope.idTipoOperacionPrincipal || "" },
							IdEstado: { "field": "IdEstado", "operator": "eq", "value": 'ACT' },
							IdTipoProducto: { "field": "IdTipoProducto", "operator": "eq", "value": 'MERF' }
						}
					}
					return;
				}
			});

			scope.$watchGroup(['idOperacion','idTipoOperacionPrincipal'], function (newval, oldval) {
				
				if (newval !== oldval && newval.length==2 && newval[1]!=false & newval[0]!=0) {
					scope.cargarFormasDePago(scope.idOperacion);
					return;
				}
			});

			scope.$watch('idPortafolioCorredora', function (newval, oldval) {
				
				if (newval !== oldval && newval != 0 && newval) {
					
					return;
				}
			});
		


			
		}
	}
});