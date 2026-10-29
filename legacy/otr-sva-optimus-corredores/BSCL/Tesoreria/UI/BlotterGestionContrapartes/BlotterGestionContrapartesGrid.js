
angular.module('ModuloTesoreria').factory('bcsBlotterGestionContrapartesGrid',function($translate,$rootScope) {
	var obj = {};



	//---------------------------------------------
	// TRANSACCIONES
	//---------------------------------------------		
	
	
	obj.getSchemaTransaccion = function(){
		return {
			model: {
				fields : {
					AccionOrden								: { type: "string"},					
					CantidadDisponibleTransaccion			: { type: "number"},
					CantidadAsignadaTransaccion				: { type: "number"},
					CantidadTransaccion						: { type: "number"},
					CantidadSimulacionTransaccion			: { type: "number"},
					CodigoCorredorComprador					: { type: "string"},
					CodigoCorredorVendedor					: { type: "string"},
					CodigoClasificacionTipoInstrumento		: { type: "string"},
					CodigoCondicionDeLiquidacion			: { type: "string"},
					CodigoEmpleadoReserva					: { type: "string"},
					CodigoOperadorCorredor					: { type: "string"},
					CodigoOperadorContraparte				: { type: "string"},
					CodigoOperadorComprador					: { type: "string"},
					CodigoOperadorVendedor					: { type: "string"},
					CorteMinimo								: { type: "number"},
					EsCuentaPropia							: { type: "string"},
					EsAPlazo								: { type: "string"},
					EsAPlazoUI								: { type: "string"},
					EsFungible								: { type: "string"},
					EsFungibleUI							: { type: "string"},
					EsBilateral				    			: { type: "string"},
					EsBilateralUI							: { type: "string"},
					EsVentaCorta							: { type: "string"},
					EsVentaCortaUI							: { type: "string"},
					EstadoTransaccion						: { type: "string"},
					FolioTransaccion 						: { type: "string"},
					FechaSistemaCreacion					: { type: "sysdate"},
					FechaOperacionTransaccion				: { type: "sysdatetime"},
					FechaLiquidacion						: { type: "sysdate"},
					FechaPago								: { type: "sysdate"},   //Fecha de liquidación original ya que en tesorería(Gestión de Contrapartes) se puede editar la fecha liquidación					
					FechaLogCambio							: { type: "sysdatetime"},
					FechaModificacionOperacion				: { type: "sysdatetime"},
					FechaEmision							: { type: "sysdate"},
					FechaTransaccion						: { type: "sysdate"},
					FechaVencimiento						: { type: "sysdate"},
					HoraSistemaCreacion						: { type: "date"},
					IdBolsa									: { type: "string"},
					IdCondicionDeLiquidacion				: { type: "string"},
					IdClasificacionTipoInstrumento			: { type: "string"},
					IdCorredorContraparte					: { type: "number"},
					IdEmisor 								: { type: "string"},
					IdEmpleadoReserva						: { type: "number"},
					IdInstrumento							: { type: "number"}, 
					IdLibroOrdenes							: { type: "number"}, 
					IdMovimiento							: { type: "number"}, 
					IdMonedaInstrumento 					: { type: "string"},
					IdMonedaTransaccion 					: { type: "string"},
					IdTipoInstrumento						: { type: "string"},
					IdTipoOperacion							: { type: "string"},
					IdTipoReajuste							: { type: "string"},
					IdTransaccion							: { type: "number"},
					IdUsuarioReserva						: { type: "number"},
					IndicadorOrdenDirecta					: { type: "string"},
					Instrumento								: { type: "string"},
					InstrumentoUI							: { type: "string"},
					InstrumentoSubyacente					: { type: "string"},
					MarcaNegociacion						: { type: "string"},
					MontoTransaccion						: { type: "number",excelFormat : "#,##0.00"},
					MontoSimulacionTransaccion 				: { type: "number"},
					MontoDisponibleTransaccion				: { type: "number"},
					MontoAsignadoTransaccion				: { type: "number"},
					Motivo									: { type: "string"},					
					Nemotecnico								: { type: "string"},
					NemotecnicoSVS							: { type: "string"},
					NombreTipoOperacion						: { type: "string"},
					NombreTipoInstrumento					: { type: "string"},
					NombreTipoInstrumentoSubyacente			: { type: "string"},
					NombreClasificacionTipoInstrumento		: { type: "string"},					
					NombreLibroOrdenes						: { type: "string"},
					NombreCondicionDeLiquidacion			: { type: "string"},
					NombreCorredorContraparte				: { type: "string"},
					NombreSistemaBursatil					: { type: "string"},					
					Observaciones							: { type: "string"},
					Plazo									: { type: "number"},
					PrecioTransaccion						: { type: "number"},					
					Reservado								: { type : "string"},					
					Tasa									: { type: "number"},
					TasaTransaccion							: { type: "number"},
					TipoLogCambio							: { type: "string"},					
					ValorRescateDisponibleTransaccion		: { type: "number"},
					ValorRescateTransaccion					: { type: "number"},
					ValorRescateSimulacionTransaccion 		: { type: "number"},					
					Intereses								: { type: "number"},    //PERU para RF
					Reajustes								: { type: "number"},    //PERU para RF
					BloqueoLiquidacion						: { type: "string"},    //Indica si la transacción está bloqueada para la liquidación a contraparte
					BloqueoLiquidacionUI					: { type: "string"},
					TipoCambioCorresponsal					: { type: "number"},
					IdCorredorCorresponsal					: { type: "number"},
					CorredorCorresponsal					: { type: "string"},
					MontoLiquidacionCorresponsal			: { type: "number"},
					IdMonedaLiquidacionCorresponsal			: { type: "string"},
					GastosCorresponsal						: { type: "number"},
					NombreClasificacionTipoInstrumentoSubyacente	: { type: "string"},
					MontoRetencion 							: { type: "number"},    //PERU monto retenido por CAVALI
					MarcaIGKUI 								: { type: "string"},					
					IdCamara								: {type : "string"},    // Para clearing financiero (actualizado a traves de WS Cloper CCLV)
				}
			}
		};
	};


	

	obj.getColumnTransaccion = function(){
		return [
				{field: "FolioTransaccion",					title: 'UI_FOLIO_TRANSACCION',			width: "70px"},  				
				{field: "FechaOperacionTransaccion",		title: 'UI_FECHA_TRANSACCION',			width: "100px", groupable: true},
				{field: "FechaLiquidacion",					title: 'UI_FECHA_LIQUIDACION',			width: "100px", groupable: true},
				{field: "FechaPago",		                title: 'UI_FECHA_PAGO',					width: "100px", groupable: true},   //Fecha de liquidación original ya que en tesorería(Gestión de Contrapartes) se puede editar la fecha liquidación
				{field: "IdClasificacionTipoInstrumento",	title: 'UI_CLASIFICACION',				width: "50px",  groupable: true},
				{field: "NombreTipoOperacion",				title: 'UI_TIPO_OPERACION',				width: "200px", groupable: true},
				{field: "InstrumentoUI",					title: 'UI_INSTRUMENTO',				width: "125px", groupable: true},
				
				{field: "CantidadTransaccion",				title: 'UI_CANTIDAD',					width: "75px", template : " <label> #:  (util.bcs_formato(CantidadTransaccion,'GEN','Cantidad',IdMonedaInstrumento,'','')).valor #</label>"},
				{field: "PrecioTransaccion",				title: 'UI_PRECIO',						width: "75px", template : " <label> #:  (util.bcs_formato(PrecioTransaccion,'GEN','Precio',IdMonedaInstrumento,'','')).valor #</label>"},
				{field: "Tasa",								title: 'UI_TASA',						width: "45px",  groupable: true},
				{field: "CantidadDisponibleTransaccion",	title: 'UI_SALDO',						width: "100px", template : " <label> #:  (util.bcs_formato(CantidadDisponibleTransaccion,'ORD','Cantidad',IdMonedaInstrumento,'','')).valor #</label>"},
				{field: "CodigoCondicionDeLiquidacion", 	title: 'UI_CONDICION_LIQUIDACION_ABR',	width: "50px",  groupable: true},
				{field: "IdEmisor",							title: 'UI_EMISOR',						width: "100px", groupable: true},
				{field: "Plazo",							title: 'UI_PLAZO',						width: "50px",  groupable: true},
				{field: "FechaVencimiento",					title: 'UI_FECHA_VENCIMIENTO',			width: "100px", groupable: true},
				{field: "IndicadorOrdenDirecta",			title: 'UI_INDICADOR_ORDEN_DIRECTA',	width: "75px",  groupable: true},
				{field: "EsBilateralUI",					title: 'UI_BILATERAL',					width: "70px",  groupable: true},				
				{field: "IdCamara",					        title: 'UI_CAMARA',					    width: "70px",  groupable: true},
				{field: "NombreCorredorContraparte",		title: 'UI_CORREDOR_CONTRAPARTE',		width: "220px",  groupable: true},
				{field: "IdTipoInstrumento",				title: 'UI_TIPO_INSTRUMENTO',			width: "75px",  groupable: true},
				{field: "NemotecnicoSVS",					title: "UI_NEMOTECNICO_SVS",			width: "75px",  groupable: true},
				{field: "EsFungibleUI",						title: 'UI_FUNGIBLE',					width: "85px",  groupable: true},
				{field: "IdBolsa",							title: 'UI_BOLSA',						width: "75px",  groupable: true},
				
				{field: "IdTipoReajuste",					title: 'UI_TIPO_REAJUSTE',				width: "75px",  groupable: true},
				{field: "IdMonedaInstrumento",				title: 'UI_MONEDA_INSTRUMENTO',			width: "50px",  groupable: true},
				{field: "MontoTransaccion",					title: 'UI_MONTO',						width: "100px", template : " <label> #:  (util.bcs_formato(MontoTransaccion,'GEN','Monto',IdMonedaInstrumento, '','')).valor #</label>"},  					
				{field: "CodigoOperadorComprador",			title: 'UI_OPERADOR_COMPRADOR',			width: "100px", groupable: true},
				{field: "CodigoOperadorVendedor",			title: 'UI_OPERADOR_VENDEDOR',			width: "100px", groupable: true},
				{field: "MarcaNegociacion",					title: 'UI_MARCA_NEGOCIACION',			width: "100px", groupable: true},
				
				{field: "EsAPlazoUI",						title: 'UI_A_PLAZO',					width: "75px",  groupable: true},
				{field: "EsVentaCortaUI",					title: 'UI_VENTA_CORTA',				width: "50px",  groupable: true},
				{field: "CodigoEmpleadoReserva",			title: 'UI_RESERVADO_POR',				width: "70px",  groupable: true},
				{field: "FechaModificacionOperacion",		title: 'UI_FECHA_MODIFICACION',			width: "100px", groupable: true},
				{field: "CantidadAsignadaTransaccion",		title: 'UI_CANTIDAD_ASIGNADA',			width: "100px",template : " <label> #:  (util.bcs_formato(CantidadAsignadaTransaccion,'ORD','Cantidad',IdMonedaInstrumento,'','')).valor #</label>"},
				
				{field: "NombreLibroOrdenes",				title: 'UI_LIBRO_ORDENES',				width: "125px",  groupable: true},
				{field: "NombreSistemaBursatil",			title: 'UI_FORMA_NEGOCIACION',			width: "150px",  groupable: true},
				{field: "NombreTipoInstrumento", 			title: 'UI_TIPO_INSTRUMENTO',			width: "215px",  groupable: true},
				{field: "CorteMinimo",						title: 'UI_CORTE',						width: "100px",  groupable: true},

				//Para RF PERU	
				{field: "Intereses",						title: 'UI_INTERES',					width: "100px", hidden: true, template : " <label> #:  (util.bcs_formato(Intereses,'GEN','Monto',IdMonedaInstrumento, '','')).valor #</label>"},
				{field: "Reajustes",						title: 'UI_REAJUSTE',					width: "100px", hidden: true, template : " <label> #:  (util.bcs_formato(Reajustes,'GEN','Monto',IdMonedaInstrumento, '','')).valor #</label>"},
				{field: "MontoRetencion",					title: 'UI_MONTO_RETENCION',			width: "100px", hidden: true, template : " <label> #:  (util.bcs_formato(MontoRetencion,'GEN','Monto',IdMonedaInstrumento, '','')).valor #</label>"},
				
				{field: "BloqueoLiquidacionUI",				title: 'UI_BLOQUEADO',					width: "100px",  groupable: true},
				{field: "MarcaIGKUI",						title: 'UI_MARCA_IGK',					width: "100px",  groupable: true, hidden: true},
				
			    {title   : "UI_INFORMACION_CORRESPONSAL", align: "center",
					   columns : [
						    {field: "CorredorCorresponsal",				title: 'UI_CORREDOR',				width: "150px", attributes:{style:"white-space:nowrap;"},  groupable: true},
						    {field: "IdMonedaLiquidacionCorresponsal",	title: 'UI_MONEDA_LIQUIDACION',		width: "80px",  groupable: true},
		  					{field: "TipoCambioCorresponsal",			title: 'UI_TIPO_CAMBIO',			width: "80px"},
		  					{field: "GastosCorresponsal",				title: 'UI_GASTOS',					width: "100px"},
							{field: "MontoLiquidacionCorresponsal",		title: 'UI_MONTO_LIQUIDACION',		width: "100px"}
		  		]},
				{field: "EstadoTransaccion",				title: 'UI_ESTADO',						width: "100px",template : "#: (EstadoTransaccion || '').toUpperCase()  #"}
			];
	};




    //--------------------------------------------
	// CLEARING FINANCIERO RESUMEN
    //--------------------------------------------
	obj.dsClearingFinanciero = new bcs.DataSource({
			type : "call",
			definition : {		
				sort 				: { field: "IdBolsa", dir: "asc" },
				pageSize 			: 50,
				callParameters      : '',
				callTransaction 	: $rootScope.SesionLocale === 'PE' ? 1810 : 193,  //193 : BSCL.Tesoreria.BD.Servicios.LIQ::ConsultarClearingFinanciero
																					  //1810: BSCL.Tesoreria.BD.Servicios.LIQ::ConsultarClearingFinancieroPE
				callResultSet 		: "P_OUT_CLEARINGFINANCIERORESUMEN",			
				schema : {
					model : {
						fields : {
							 IdBolsa    		      		: {type : "string"}
							,CodigoGrupoCamara				: {type : "number"}
							,NombreGrupoCamara        		: {type : "string"}
			                ,IdClasificacionTipoInstrumento : {type : "string"}
			                ,IdCorredorContraparte			: {type : "number"}
			                ,IdMonedaTransaccion            : {type : "string"}
			                ,RutContraparte 		  		: {type : "string"}
			                ,RazonSocialContraparte   		: {type : "string"}		                
			                ,IdComprobanteTesoreria   		: {type : "number"}
			                ,MontoRetencion					: {type : "number"}
			                ,TotalVenta     		  		: {type : "number"}
			                ,TotalCompra    		  		: {type : "number"}
			                ,SaldoAcreedor  		  		: {type : "number"}
			                ,SaldoDeudor    	      		: {type : "number"}
						}
					}
				},
                aggregate			: [ //Para resumen de totales                 	  		               
				                        { field: "TotalVenta", aggregate: "sum" },	
				                        { field: "TotalCompra", aggregate: "sum" },	
				                        { field: "SaldoAcreedor", aggregate: "sum" },	
				                        { field: "SaldoDeudor", aggregate: "sum" }						                       
                                      ]
			}
	});
			
	obj.columnasClearingFinanciero = function() {
			return  [
				 {field: "NombreGrupoCamara",        		title: "UI_CAMARA",                width: "48px", groupable: true } 
				,{field: "IdBolsa",                  		title: "UI_BOLSA",                 width: "38px", groupable: true }							
				,{field: "IdClasificacionTipoInstrumento", 	title: "UI_MERCADO",               width: "48px", groupable: true }
				,{field: "RazonSocialContraparte",   		title: "UI_CONTRAPARTE",           width: "185px", attributes:{style:"white-space:nowrap;"}, groupable: true }
				,{field: "RutContraparte",           		title: "UI_RUT",                   width: "65px", align: "right", groupable: true }
				,{field: "IdMonedaTransaccion",           	title: "UI_MONEDA",                width: "65px", groupable: true }
				
				,{field: "TotalVenta",               		title: "UI_TOTAL_VENTAS",          width: "80px", template : " <label> #:  (util.bcs_formato(TotalVenta,'GEN','Monto',IdMonedaTransaccion, '','')).valor #</label>",    groupable: false, footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','"+  $rootScope.SesionIdMonedaSistema +"', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"} } //A cobrar Total Ventas
				,{field: "TotalCompra",              		title: "UI_TOTAL_COMPRAS",         width: "80px", template : " <label> #:  (util.bcs_formato(TotalCompra,'GEN','Monto',IdMonedaTransaccion, '','')).valor #</label>" ,  groupable: false, footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','"+  $rootScope.SesionIdMonedaSistema +"', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"} } //A pagar Total Compras			
				,{field: "SaldoAcreedor",            		title: "UI_SALDO_ACREEDOR",        width: "80px", template : " <label> #:  (util.bcs_formato(SaldoAcreedor,'GEN','Monto',IdMonedaTransaccion, '','')).valor #</label>" ,groupable: false, footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','"+  $rootScope.SesionIdMonedaSistema +"', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"} } //Saldo Neto Acreedor
				,{field: "SaldoDeudor",              		title: "UI_SALDO_DEUDOR",          width: "80px", template : " <label> #:  (util.bcs_formato(SaldoDeudor,'GEN','Monto',IdMonedaTransaccion, '','')).valor #</label>" ,  groupable: false, footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','"+  $rootScope.SesionIdMonedaSistema +"', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"} } //Saldo Neto Deudor
				
				,{field: "IdComprobanteTesoreria",   		title: "UI_SECUENCIA_COMPROBANTE", width: "65px"}
				//PERU: monto retención cavali(informado por archivo IGK)
				,{field: "MontoRetencion",          		title: "UI_MONTO_RETENCION",       width: "70px", hidden : $rootScope.SesionLocale === 'PE' ? false : true ,template : " <label> #:  (util.bcs_formato(MontoRetencion,'GEN','Monto',IdMonedaTransaccion, '','')).valor #</label>"}
			];
	};

			
		
	//--------------------------------------------------------
	// CLEARING FINANCIERO DETALLE TRANSACCIONES
	//---------------------------------------------------------	
		
	obj.dsClearingFinancieroDetalleConf = function(){
			return {
				type : "call",
				definition : {
					sort 				: { field: "IdBolsa", dir: "asc" },
					callParameters      : '',
					callTransaction 	: $rootScope.SesionLocale === 'PE' ? 1810 : 193,  //193 : BSCL.Tesoreria.BD.Servicios.LIQ::ConsultarClearingFinanciero
							  															  //1810: BSCL.Tesoreria.BD.Servicios.LIQ::ConsultarClearingFinancieroPE
					callResultSet 		: "P_OUT_CLEARINGFINANCIERODETALLE",
					schema : {
						model : {
							fields : {			     
				            	 FolioTransaccion 	  			: {type : "string"}
				            	,FechaSistemaCreacion 			: {type : "sysdate"}
				            	,IdCondicionDeLiquidacion 		: {type : "string"}
				            	,CodigoCondicionDeLiquidacion 	: {type : "string"}
				            	,IdMonedaTransaccion 			: {type : "string"}
				            	,Instrumento 	      			: {type : "string"}			            	
					  			,CantidadTransaccion  			: {type : "number"}
								,PrecioTransaccion    			: {type : "number"}
								,Tasa                 			: {type : "number"}						  
				            	,AccionTipoOperacion  			: {type : "string"}
				            	,TotalCompra		  			: {type : "number"}
				            	,TotalVenta           			: {type : "number"}
				            	,MontoRetencion       			: {type : "number"}  //PERU: monto retención cavali(informado por archivo IGK)
							}
						}
					}
					
					
										
				}
			};
	};
			  
		  
		
	obj.columnasClearingFinancieroDetalle = function() {
			return  [		 
				 {field: "FolioTransaccion",  			title: "UI_FOLIO_TRANSACCION",     width: "65px"}			
				,{field: "FechaSistemaCreacion",		title: "UI_FECHA_TRANSACCION",     width: "70px"}
				,{field: "CodigoCondicionDeLiquidacion",title: "UI_CONDICION_LIQUIDACION", width: "55px"}
				,{field: "IdMonedaTransaccion",         title: "UI_MONEDA",                width: "65px"}				
				,{field: "Instrumento",  	    		title: "UI_INSTRUMENTO",           width: "70px"}
				,{field: "CantidadTransaccion", 		title: "UI_CANTIDAD",          	   width: "65px", template : " <label> #:  (util.bcs_formato(CantidadTransaccion,'GEN','Cantidad',IdMonedaTransaccion, '','')).valor #</label>"}
				,{field: "PrecioTransaccion",  			title: "UI_PRECIO",            	   width: "65px", template : " <label> #:  (util.bcs_formato(PrecioTransaccion,'GEN','Precio',IdMonedaTransaccion, '','')).valor #</label>"}								
				,{field: "Tasa",  	    				title: "UI_TASA",              	   width: "50px", template : " <label> #:  (util.bcs_formato(Tasa,'GEN','Tasa',IdMonedaTransaccion, '','')).valor #</label>"}
				,{field: "AccionTipoOperacion", 		title: "UI_OPERACION",             width: "70px"}			
				,{field: "TotalCompra",         		title: "UI_MONTO_COMPRA",          width: "70px", template : " <label> #:  (util.bcs_formato(TotalCompra,'GEN','Monto',IdMonedaTransaccion, '','')).valor #</label>"}
				,{field: "TotalVenta",          		title: "UI_MONTO_VENTA",           width: "70px", template : " <label> #:  (util.bcs_formato(TotalVenta,'GEN','Monto',IdMonedaTransaccion, '','')).valor #</label>"}
				//PERU: monto retención cavali(informado por archivo IGK)
				,{field: "MontoRetencion",          	title: "UI_MONTO_RETENCION",       width: "70px", hidden : $rootScope.SesionLocale === 'PE' ? false : true ,template : " <label> #:  (util.bcs_formato(MontoRetencion,'GEN','Monto',IdMonedaTransaccion, '','')).valor #</label>"}
			];
	};




	//---------------------------------------------
	// CLEARING FINANCIERO DIVIDENDOS
	//---------------------------------------------	
	obj.dsClearingFinancieroDividendosConf = function(){
			return {
				type : "call",
				definition : {
					sort 				: { field: "IdBolsa", dir: "asc" },
					//pageSize 			: 25,
					callParameters      : '',
					callTransaction 	: "193",
					callResultSet 		: "P_OUT_CLEARINGFINANCIERODIVIDENDOS",
					schema : {
						model : {
							fields : {
			   					 CodigoGrupoCamara				: {type : "number"}
			   					,NombreGrupoCamara				: {type : "string"}
			   					,IdEventoCapitalCliente			: {type : "number"}
			   					,IdEventoCapital				: {type : "number"}
			   					,IdTipoEventoCapital			: {type : "string"}
			   					,TipoEvento						: {type : "string"}
			   					,NumeroDividendo				: {type : "number"}
			   					,Secuencia						: {type : "number"}
			   					,IdTipoInstrumento				: {type : "string"}   
			   					,Nemo							: {type : "string"}
			   					,IdMoneda						: {type : "string"}
			   					,DocumentoIdentidad				: {type : "string"} 
			   					,CantidadDerecho				: {type : "number"}
			   					,Total							: {type : "number"}
			   					,TotalVenta						: {type : "number"}
			   					,TotalCompra					: {type : "number"}
			   					,IdBoveda						: {type : "string"}
			   					,IdComprobanteTesoreria			: {type : "number"}
			   					,IdBolsa						: {type : "string"}
			   					,RutContraparte					: {type : "string"}			
			   					,RazonSocialContraparte			: {type : "string"}			
			   					,IdCorredorContraparte			: {type : "number"}
			   					,TipoOperacion					: {type : "string"}
							}
						}
					}
				}
			};
	};

	obj.columnasClearingFinancieroDividendos = function() {
		return  [
			 {field: "TipoEvento",			title: "UI_TIPO_EVENTO",     	 width: "150px",attributes:{style:"white-space:nowrap;"} }			
			,{field: "NumeroDividendo",		title: "UI_NUMERO_DIVIDENDO",    width: "70px"}			
			,{field: "Secuencia",			title: "UI_SECUENCIA",     		 width: "65px"}
			,{field: "IdTipoInstrumento",	title: "UI_TIPO_INSTRUMENTO",    width: "70px"}			
			,{field: "Nemo",				title: "UI_INSTRUMENTO",     	 width: "90px"}			
			,{field: "IdMoneda",			title: "UI_MONEDA",     		 width: "60px"}	
			,{field: "CantidadDerecho",		title: "UI_CANTIDAD_DERECHO",    width: "85px", template: kendo.template("#if ( CantidadDerecho == 0)  {##=CantidadDerecho##} else {##=util.formateaNumero(CantidadDerecho,0)##} #")}
			,{field: "Total",				title: "UI_TOTAL",     			 width: "90px", template: kendo.template("#if ( Total == 0)  {##=Total##} else {##=util.formateaNumero(Total,0)##} #")}
			,{field: "IdBoveda",			title: "UI_BOVEDA",     	     width: "80px"}
			,{field: "TipoOperacion",		title: "UI_TIPO_OPERACION",      width: "80px"}
		];
	};	



	//---------------------------------------------
	// CLEARING FINANCIERO LIQUIDACIONES
	//---------------------------------------------	
	obj.dsClearingFinancieroLiquidacionesConf = function(){
		return {
			type : "call",
			definition : {
				sort 				: { field: "IdMovimiento", dir: "asc" },
				//pageSize 			: 25,
				callParameters      : '',
				callTransaction 	: "193",
				callResultSet 		: "P_OUT_CLEARINGFINANCIEROLIQUIDACIONES",
				schema : {
					model : {
						fields : {
								IdMovimiento					: {type : "number"}
							   ,Cliente							: {type : "string"}
							   ,IdPortafolio					: {type : "number"}
							   ,FechaMovimiento					: {type : "date"}
							   ,Abono							: {type : "number"}
							   ,Cargo							: {type : "number"}
							   ,TipoMovimiento					: {type : "string"}
							   ,DescripcionMovimiento			: {type : "string"}
						}
					}
				}
			}
		};
	};
	
	obj.columnasClearingFinancieroLiquidaciones = function() {
		return  [
			 {field: "IdMovimiento",			title: "UI_ID_MOVIMIENTO", 		width: "60px",attributes:{style:"white-space:nowrap;"} }			
			,{field: "Cliente",		title: "UI_CLIENTE",		width: "150px"}			
			,{field: "IdPortafolio",			title: "UI_ID_PORTAFOLIO",		width: "65px"}
			,{field: "FechaMovimiento",	title: "UI_FECHA_MOVIMIENTO",		width: "70px"}			
			,{field: "Abono",				title: "UI_ABONO",		width: "90px"}			
			,{field: "Cargo",			title: "UI_CARGO",		width: "60px"}	
			,{field: "TipoMovimiento",		title: "UI_TIPO_MOVIMIENTO",		width: "85px"}
			,{field: "DescripcionMovimiento",				title: "UI_DESCRIPCION_MOVIMIENTO",		width: "90px"}
		];
	};	

	//---------------------------------------------
	// CLEARING FINANCIERO ANTICIPO SIMULTANEAS
	//---------------------------------------------	
	obj.dsClearingFinancieroSimultaneasConf = function(){
			return {
				type : "call",
				definition : {
					sort 				: { field: "IdBolsa", dir: "asc" },
					//pageSize 			: 25,
					callParameters      : '',
					callTransaction 	: "193",
					callResultSet 		: "P_OUT_CLEARINGFINANCIEROSIMULTANEAS",
					schema : {
						model : {
							fields : {
			   					 TipoOperacion					: {type : "string"}
								,FechaLiquidacion			    : {type : "date"}
								,Nemotecnico					: {type : "string"}
								,Cantidad						: {type : "number"}
								,MontoCompra					: {type : "number"}
								,MontoVenta						: {type : "number"}
								,NombreGrupoCamara				: {type : "string"}
								,RutContraparte					: {type : "string"}
								,IdClasificacionTipoInstrumento	: {type : "string"}
								,IdComprobanteTesoreria			: {type : "number"}
								,IdClasificacionTipoInstrumento	: {type : "string"}

								
							}
						}
					}
				}
			};
	};

	obj.columnasClearingFinancieroSimultaneas = function() {
		return  [
			{field: "TipoOperacion",		title: "UI_TIPO_OPERACION",      width: "80px"}			
			,{field: "FechaLiquidacion",		title: "UI_FECHA_LIQUIDACION",    width: "70px"}			
			,{field: "Nemotecnico",			title: "UI_NEMOTECNICO",     		 width: "65px"}
			,{field: "Cantidad",		title: "UI_CANTIDAD",    width: "85px", template: kendo.template("#if ( Cantidad == 0)  {##=Cantidad##} else {##=util.formateaNumero(Cantidad,0)##} #")}
			,{field: "MontoCompra",				title: "UI_TOTAL_COMPRA",     			 width: "90px", template: kendo.template("#if ( MontoCompra == 0)  {##=MontoCompra##} else {##=util.formateaNumero(MontoCompra,0)##} #")}
			,{field: "MontoVenta",				title: "UI_TOTAL_VENTA",     			 width: "90px", template: kendo.template("#if ( MontoVenta == 0)  {##=MontoVenta##} else {##=util.formateaNumero(MontoVenta,0)##} #")}
			
		];
	};	

	obj.columnasClearingFinancieroDividendos = function() {
		return  [
			 {field: "TipoEvento",			title: "UI_TIPO_EVENTO",     	 width: "150px",attributes:{style:"white-space:nowrap;"} }			
			,{field: "NumeroDividendo",		title: "UI_NUMERO_DIVIDENDO",    width: "70px"}			
			,{field: "Secuencia",			title: "UI_SECUENCIA",     		 width: "65px"}
			,{field: "IdTipoInstrumento",	title: "UI_TIPO_INSTRUMENTO",    width: "70px"}			
			,{field: "Nemo",				title: "UI_INSTRUMENTO",     	 width: "90px"}			
			,{field: "IdMoneda",			title: "UI_MONEDA",     		 width: "60px"}	
			,{field: "CantidadDerecho",		title: "UI_CANTIDAD_DERECHO",    width: "85px", template: kendo.template("#if ( CantidadDerecho == 0)  {##=CantidadDerecho##} else {##=util.formateaNumero(CantidadDerecho,0)##} #")}
			,{field: "Total",				title: "UI_TOTAL",     			 width: "90px", template: kendo.template("#if ( Total == 0)  {##=Total##} else {##=util.formateaNumero(Total,0)##} #")}
			,{field: "IdBoveda",			title: "UI_BOVEDA",     	     width: "80px"}
			,{field: "TipoOperacion",		title: "UI_TIPO_OPERACION",      width: "80px"}
		];
	};	
		


	//---------------------------------------
	// CLEARING FISICO
	//---------------------------------------
	obj.dsClearingFisico = new bcs.DataSource({
		type : "call",
		definition : {		
			sort 				: { field: "IdBolsa", dir: "asc" },
			pageSize 			: 50,
			callParameters      : '',
			callTransaction 	: "3591",   //BSCL.Tesoreria.BD.Servicios.LIQ::ConsultarClearingFisico
			callResultSet 		: "P_OUT_CLEARINGFISICORESUMEN",			
			schema : {
				model : {
					fields : {
						 IdBolsa    		      		: {type : "string"}	
						,IdCamara    		      		: {type : "string"}
						,IdClasificacionTipoInstrumento	: {type : "string"}
						,IdTipoInstrumento 				: {type : "string"}
						,NombreTipoInstrumento 			: {type : "string"}
			            ,IdInstrumento			  		: {type : "number"}
			            ,Instrumento			  		: {type : "string"}
		                ,CantidadVenta     		  		: {type : "number"}
		                ,CantidadCompra    		  		: {type : "number"}
		                ,SaldoAcreedor  		  		: {type : "number"}
		                ,SaldoDeudor    	      		: {type : "number"}
					}
				}
			},
        	aggregate		: [
      								{ field: "CantidadVenta", 	aggregate: "sum" },
      								{ field: "CantidadCompra", 	aggregate: "sum" },
      								{ field: "SaldoAcreedor", 	aggregate: "sum" },
      								{ field: "SaldoDeudor", 	aggregate: "sum" }
      						  ]
		}
	});
	
		
	obj.columnasClearingFisico = function() {
		return  [
			 {field: "IdBolsa",                  		title: "UI_BOLSA",                 width: "40px",  groupable: true }
			,{field: "IdCamara",                  		title: "UI_CAMARA",                width: "40px",  groupable: true }
			,{field: "IdClasificacionTipoInstrumento",	title: "UI_MERCADO",               width: "40px",  groupable: true }
			,{field: "NombreTipoInstrumento",           title: "UI_TIPO_INSTRUMENTO",      width: "100px", groupable: true }
			,{field: "Instrumento",              		title: "UI_INSTRUMENTO",           width: "60px",  groupable: true }
			,{field: "CantidadVenta",            		title: "UI_CANTIDAD_VENTAS",       width: "80px",  template: kendo.template("#if ( CantidadVenta == 0)  {##=CantidadVenta##} else {##=util.formateaNumero(CantidadVenta,0)##} #"),groupable: false, aggregates: ["sum"],groupFooterTemplate:"#=util.formateaNumero(sum,0)#"}      
			,{field: "CantidadCompra",           		title: "UI_CANTIDAD_COMPRAS",      width: "80px",  template: kendo.template("#if ( CantidadCompra == 0)  {##=CantidadCompra##} else {##=util.formateaNumero(CantidadCompra,0)##} #"),groupable: false, aggregates: ["sum"],groupFooterTemplate:"#=util.formateaNumero(sum,0)#"}
			,{field: "SaldoAcreedor",            		title: "UI_SALDO_ACREEDOR",        width: "80px",  template: kendo.template("#if ( SaldoAcreedor == 0)  {##=SaldoAcreedor##} else {##=util.formateaNumero(SaldoAcreedor,0)##} #"),groupable: false, aggregates: ["sum"],groupFooterTemplate:"#=util.formateaNumero(sum,0)#"}
			,{field: "SaldoDeudor",              		title: "UI_SALDO_DEUDOR",          width: "80px",  template: kendo.template("#if ( SaldoDeudor == 0)  {##=SaldoDeudor##} else {##=util.formateaNumero(SaldoDeudor,0)##} #"),groupable: false, aggregates: ["sum"],groupFooterTemplate:"#=util.formateaNumero(sum,0)#"}
		];
	};	
	
	    
	    
	//---------------------------------------
	// CLEARING FISICO DETALLE
	//---------------------------------------	
	obj.dsClearingFisicoDetalleConf = function(){
		return {
			type : "call",
			definition : {
				sort 				: { field: "IdBolsa", dir: "asc" },
				//pageSize 			: 25,
				callParameters      : '',
				callTransaction 	: "3591",   //BSCL.Tesoreria.BD.Servicios.LIQ::ConsultarClearingFisico
				callResultSet 		: "P_OUT_CLEARINGFISICODETALLE",
				schema : {
					model : {
						fields : {
							 IdTransaccion        	  		: {type : "number"}
			            	,FolioTransaccion 	  	  		: {type : "string"}
			            	,IndicadorMercado	  	  		: {type : "string"}
			            	,FechaSistemaCreacion 	  		: {type : "sysdate"}
			            	,CodigoCondicionDeLiquidacion 	: {type : "string"}			            	
			            	,AccionTipoOperacion  	  		: {type : "string"}
			            	,Instrumento 	      	  		: {type : "string"}			            	
			            	,IdInstrumento 	      	  		: {type : "number"}
				  			,CantidadTransaccion  	  		: {type : "number"}
							,PrecioTransaccion    	  		: {type : "number"}
							,Tasa                 	  		: {type : "number"}
			            	,CantidadCompra		  	  		: {type : "number"}
			            	,CantidadVenta        	  		: {type : "number"}
			            	,MontoTransaccion		  		: {type : "number"}	
			            	,IdClasificacionTipoInstrumento	: {type : "string"}
			            	,IdCamara 	  	  				: {type : "string"}
						}
					}
				}
			}
		};
	};


	obj.columnasClearingFisicoDetalle = function() {
		return  [
			 {field: "FolioTransaccion",  		 		title: "UI_FOLIO_TRANSACCION",     	width: "65px"}
			,{field: "FechaSistemaCreacion",	 		title: "UI_FECHA_TRANSACCION",     	width: "70px"}			
			,{field: "CodigoCondicionDeLiquidacion", 	title: "UI_CONDICION_LIQUIDACION", 	width: "60px"}			
			,{field: "AccionTipoOperacion", 	 		title: "UI_OPERACION",             	width: "65px"}			
			,{field: "Instrumento",  	    	 		title: "UI_INSTRUMENTO",           	width: "80px"}
			,{field: "CantidadTransaccion", 	 		title: "UI_CANTIDAD",          		width: "65px", template: kendo.template("#if ( CantidadTransaccion == 0)  {##=CantidadTransaccion##} else {##=util.formateaNumero(CantidadTransaccion,0)##} #")}
			,{field: "PrecioTransaccion",  				title: "UI_PRECIO",            		width: "65px", template: kendo.template("#if ( PrecioTransaccion == 0)  {##=PrecioTransaccion##} else {##=util.formateaNumero(PrecioTransaccion,0)##} #")}
			,{field: "Tasa",  	    			 		title: "UI_TASA",              		width: "50px", template: kendo.template("#if ( Tasa == 0)  {##=Tasa##} else {##=util.formateaNumero(Tasa,0)##} #")}			
			,{field: "MontoTransaccion",      	 		title: "UI_MONTO",                  width: "70px", template: kendo.template("#if ( MontoTransaccion == 0)  {##=MontoTransaccion##} else {##=util.formateaNumero(MontoTransaccion,0)##} #")}
		];
	};		
	
	
	return obj;
});