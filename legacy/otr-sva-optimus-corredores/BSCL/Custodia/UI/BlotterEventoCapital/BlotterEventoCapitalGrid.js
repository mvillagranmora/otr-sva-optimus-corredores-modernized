angular.module('ModuloCustodia').factory('bcsBlotterEventoCapitalGrid',['bcsComunes','$rootScope',
		function(bcsComunes,$rootScope)  
{
	var obj = {};
	
	
	
	//Evento Capital
	modeloEventoCapital = function() {
		return {
    		model: {
    			fields: {					
					IdEventoCapital					: { type: "number"},
					IdTipoEventoCapital				: { type: "string"},
					NumeroDividendo					: { type: "number"},
					Secuencia						: { type: "number"},
					Descripcion						: { type: "string"},
					IdClasificacionTipoInstrumento	: { type: "string"},
					IdTipoInstrumento				: { type: "string"},
					Nemo							: { type: "string"},
					ISIN							: { type: "string"},
					Factor							: { type: "number"},
					Impuesto						: { type: "number"},
					FechaActualizacion				: { type: "date"},
					FechaLimite						: { type: "date"},
					FechaPago						: { type: "date"},
					IdBoveda					    : { type: "string"},
					IdMoneda						: { type: "string"},
					Paridad							: { type: "number"},
					NombreMoneda					: { type: "string"},
					IdInstrumentoNuevo				: { type: "number"},
					NemoNuevo						: { type: "string"},
					FactorNuevo						: { type: "number"},
					NumeroDividendo					: { type: "number"},
					IdTipoDividendo					: { type: "string"},										
					EsInscrito						: { type: "string"},
					IdEstado						: { type: "string"},
					Origen							: { type: "string"},
					CuentaDepositanteSorteoLetras	: { type: "number"},
					IdEmisor						: { type: "string"},
					EstadoNotificacion				: { type: "string"},
					Indice							: { type: "number"},
					IndiceRue						: { type: "number"},
					OperacionesVigentes				: { type: "string"},
					MontoRepartido					: { type: "number"},
					ISFUT							: { type: "string"},
					TasaEfectiva					: { type: "number"},
					SecuenciaRelacionada 			: { type: "string"},
					RETART104						: { type: "string"},
					PaisEmisorInstrumento			: { type: "string"},
					GastoTransferencia				: { type: "number"}
        		} 
        	}
		};
	};
	//Evento Capital Cliente//	
	modeloEventoCapitalCliente = function() {
		return {
    		model: {
    			fields: {
    				IdEventoCapitalCliente			: { type: "number"},
    				IdTipoInstrumento				: { type: "string"},
    				IdTipoEventoCapital				: { type: "string"},
    				NumeroDividendo					: { type: "number"},
    				Secuencia						: { type: "number"},
    				IdPortafolio					: { type: "number"},
    				CodigoPortafolio 				: { type: "string"},
    				RUTCavali						: { type: "string"},
    				IdClasificacionTipoInstrumento	: { type: "string"},
    				TipoSocioNegocio				: { type: "string"},
    				Nemo							: { type: "string"},
    				ISIN							: { type: "string"},
    				NemoNuevo						: { type: "string"},
    				IdConceptoGarantia				: { type: "string"},
    				TipoCartera						: { type: "string"},
    				DocumentoIdentidad				: { type: "string"},
    				NombreCliente					: { type: "string"},
    				ConfiguracionNotificacion		: { type: "string"},
    				TienePreferenciaDePago			: { type: "string"},
    				IdEmpleadoEjecutivoPortafolio	: { type: "number"},
    				CodigoUsuarioAgente				: { type: "string"},
    				NombreAgente					: { type: "string"},
    				CodigoSucursal					: { type: "string"},
    				MontoCapital					: { type: "number"},
    				Factor							: { type: "number"},
    				FactorNuevo						: { type: "number"},
    				Precio							: { type: "number"},
    				PrecioNuevo						: { type: "number"},
    				CantidadNueva					: { type: "number"},
    				MontoInteres					: { type: "number"},
    				MontoReajuste					: { type: "number"},
    				FechaPago						: { type: "date"},
					HoraIngresoFechaPago			: { type: "string"},
    				IdMoneda						: { type: "string"},
    				CantidadDerecho					: { type: "number"},
    				PrecioAnterior					: { type: "number"},
    				Total							: { type: "number"},
    				MontoRetencion					: { type: "number"},
    				CantidadDerechoArchivo			: { type: "number"},
    				AccionesDisponiblesArchivo		: { type: "number"},
    				DiferenciaCantidadDerecho		: { type: "number"},
    				DiferenciaAccionesCantidadNueva	: { type: "number"},
    				MontoBeneficioArchivo			: { type: "number"},
    				DiferenciaMontoBeneficio    	: { type: "number"},
    				MontoInteresArchivo         	: { type: "number"},
    				MontoCapitalArchivo         	: { type: "number"},
    				DiferenciaMontoInteres      	: { type: "number"},
    				DiferenciaMontoCapital      	: { type: "number"},
    				MontoRetencionArchivo	      	: { type: "number"},
    				DiferenciaMontoRetencion      	: { type: "number"},
    				IdBoveda						: { type: "string"},
    				Origen							: { type: "string"},
    				IdEstado						: { type: "string"},
    				EstadoNotificacion				: { type: "string"},
    				NombreMedioDePago				: { type: "string"},
    				TipoCuentaBoveda				: { type: "string"},
    				NumeroCuentaBoveda				: { type: "string"},
    				PagaClienteCorredor				: { type: "string"},
    				IdTipoReajuste					: { type: "string"},
    				INGMontoInteresCorteCupon		: { type: "number"},
    				Amortizacion					: { type: "number"},
    				AfectoaClearing					: { type: "string"},
    				ProcesadoClearing				: { type: "string"},
    				IdRegimenFondoMutuo				: { type: "string"},
    				OperacionesVigentes				: { type: "string"},
    				ISFUT							: { type: "string"},
    				RetencionISFUT					: { type: "string"},
    				ISFUTEventoCapital				: { type: "string"},
    				TasaEfectiva					: { type: "number"},
    				Modalidad						: { type: "string"},
    				MedioDePago						: { type: "string"},
					ImpuestoDivExtranjero			: { type: "number"},
					MontoImpuestoExtranjero			: { type: "number"},
					GastoTransferencia				: { type: "number"}
        		} 
        	}
		};
	};
	
	


	modeloEventosFaltantes = function() {
		return {
    		model: {
    			fields: {
			
				TipoEventoCapital			: { type: "string"},
				Nemotecnico					: { type: "string"},
				Secuencia					: { type: "number"},
				Descripcion					: { type: "string"},
				FechaLimite					: { type: "string"},
				FechaPago					: { type: "string"},

        		} 
        	}
		};
	};
	
	
	obj.fnColorBk = function(row) {
        var color = "";
		
		if ( row.IdEstado == "PEN"){
			color = "yellow";
			return color;
		}
		
		if ( row.IdEstado == "MAN"){
			color = "yellow";
			return color;
		}
		
		if (row.EsInscrito == "S") {
			if (( row.Indice !== row.IndiceRue  || row.Indice == 0)& row.IdTipoEventoCapital!== 'CC'){
				color = "red";
				return color;
				
			}
		}
  	};
	
	
	//Evento Capital Cliente Forma de Pago	
	obj.modeloEventoCapitalClienteFormaDePago = function() {
		return {
    		model: { 
    			fields: {
    				IdEventoCapitalClienteFormaDePago		: { type: "number"},
					IdEventoCapitalCliente    				: { type: "number"},
					IdPreferenciaPagoPortafolio				: { type: "number"},
					IdTipoMovimiento                 		: { type: "string"},
					IdModalidadPago                  		: { type: "string"},
					IdMedioDePago 							: { type: "string"},
					NombreMedioDePago 						: { type: "string"},
					IdBanco 								: { type: "number"},
					NombreBanco 							: { type: "string"},
					IdTipoCuentaBancaria  	        		: { type: "string"},
					NombreTipoCuentaBancaria         		: { type: "string"},
					IdCuentaBancaria 						: { type: "number"},
					NombreCuentaBancaria 					: { type: "string"},
					NumeroCuentaBancaria  	 	        	: { type: "string"},
					IdTipoTarjetaBancaria 					: { type: "string"},
					NumeroTarjetaBancaria 					: { type: "string"},
					NumeroMedioDePago 						: { type: "string"},
					Sucursal 								: { type: "string"},
					FechaVctoDocumento	 					: { type: "date"},
					IdMoneda								: { type: "string"},
					Monto	 								: { type: "number"},
					FechaHoraIngresoRegistro				: { type: "date"},
					IdUsuarioIngreso						: { type: "number"},
					IdEstado								: { type: "string"},
					IdPortafolio							: { type: "number"},
					CodigoPortafolio						: { type: "string"},
					DocumentoIdentidad						: { type: "string"},
					NombreCliente							: { type: "string"}
        		} 
        	}
		};
	};
	
	//data source eventos de capital
	obj.dataSourceEvento = function() {
		return new bcs.DataSource ({
			type 		: "odata", //Los tipos posibles son: odata, call, array y object
			definition 	: {
							serverFiltering 	: 	true, // Filtrado de datos en el servidor.
							serverPaging 		: 	true, // Paginado de los datos en el servidor.
							serverSorting 		: 	true, // Ordenamiento de los datos en el servidor.
							sort 				: 	{ field: "IdTipoEventoCapital", dir: "desc" },
							filter 				: 	{ field: "IdEstado", operator: "contains", value: 'VIG' },
							pageSize 			: 	25,
							transport 			:	{
	                            	   					read : {
															url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=364",
															dataType : "json"
														}
	                               					 },
							schema 				: modeloEventoCapital()
						  }
		});
	};
	
/*
//Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles (Custodia - Evento Capital)		 
	//data source eventos de capital cliente
	obj.dataSourceEventoCliente = function() {
		return new bcs.DataSource ( {
			type 		: "odata", //Los tipos posibles son: odata, call, array y object
			definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: true, // Ordenamiento de los datos en el servidor.
							sort 				: { field: "IdTipoEventoCapital", dir: "desc" },
							filter 				: { field: "IdBoveda", operator: "contains", value: 'DCV' },
							pageSize 			: 25,
							transport 			: {
	                            	   					read : {
															url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=366",
															dataType : "json"
														}
	                               					 },
							schema 				: modeloEventoCapitalCliente(),
							aggregate			: [ //Para resumen de totales de cobro
													{ field: "MontoCapital", 					aggregate: "sum" },
													{ field: "CantidadNueva", 					aggregate: "sum" },			    	        
													{ field: "MontoInteres", 					aggregate: "sum" },
													{ field: "MontoReajuste", 					aggregate: "sum" },
													{ field: "CantidadDerecho",					aggregate: "sum" },
													{ field: "Total", 							aggregate: "sum" },
													{ field: "MontoRetencion", 					aggregate: "sum" },
													{ field: "CantidadDerechoArchivo",			aggregate: "sum" },
													{ field: "AccionesDisponiblesArchivo",		aggregate: "sum" },
													{ field: "DiferenciaCantidadDerecho",		aggregate: "sum" },
													{ field: "DiferenciaAccionesCantidadNueva",	aggregate: "sum" },
													{ field: "MontoBeneficioArchivo",			aggregate: "sum" },
													{ field: "DiferenciaMontoBeneficio",		aggregate: "sum" },
													{ field: "MontoInteresArchivo",				aggregate: "sum" },
													{ field: "MontoCapitalArchivo",				aggregate: "sum" },
													{ field: "DiferenciaMontoInteres",			aggregate: "sum" },
													{ field: "DiferenciaMontoCapital",			aggregate: "sum" },
													{ field: "MontoRetencionArchivo",			aggregate: "sum" },
													{ field: "DiferenciaMontoRetencion",		aggregate: "sum" }			
													]
						  }
		});
	};
*/
	var filtro = {};
	
	//if($rootScope.SesionLocale === 'CL')
	//	filtro = { field: "IdBoveda", operator: "contains", value: 'DCV' };
	
	//data source eventos de capital cliente filtrados
	obj.dataSourceEventoCliente = function() {
		return new bcs.DataSource ( {
			type 		: "call", //Los tipos posibles son: odata, call, array y object
			definition 	: {
							callTransaction	: "13063",
							serverFiltering 	: false, // Filtrado de datos en el servidor.
							serverPaging 		: false, // Paginado de los datos en el servidor.
							serverSorting 		: false, // Ordenamiento de los datos en el servidor.
							callResultSet   	: "P_OUT_CUSTODIA_EVENTO_CAPITAL",
							sort 				: { field: "IdTipoEventoCapital", dir: "desc" },
							filter 				: filtro,
							pageSize 			: 25,
							schema 				: modeloEventoCapitalCliente(),
							group 				: [{
													field: "IdMoneda", aggregates: [
														{ field: "MontoCapital", 					aggregate: "sum" },
														{ field: "CantidadNueva", 					aggregate: "sum" },			    	        
														{ field: "MontoInteres", 					aggregate: "sum" },
														{ field: "MontoReajuste", 					aggregate: "sum" },
														{ field: "CantidadDerecho",					aggregate: "sum" },
														{ field: "Total", 							aggregate: "sum" },
														{ field: "MontoRetencion", 					aggregate: "sum" },
														{ field: "CantidadDerechoArchivo",			aggregate: "sum" },
														{ field: "AccionesDisponiblesArchivo",		aggregate: "sum" },
														{ field: "DiferenciaCantidadDerecho",		aggregate: "sum" },
														{ field: "DiferenciaAccionesCantidadNueva",	aggregate: "sum" },
														{ field: "MontoBeneficioArchivo",			aggregate: "sum" },
														{ field: "DiferenciaMontoBeneficio",		aggregate: "sum" },
														{ field: "MontoInteresArchivo",				aggregate: "sum" },
														{ field: "MontoCapitalArchivo",				aggregate: "sum" },
														{ field: "DiferenciaMontoInteres",			aggregate: "sum" },
														{ field: "DiferenciaMontoCapital",			aggregate: "sum" },
														{ field: "MontoRetencionArchivo",			aggregate: "sum" },
														{ field: "DiferenciaMontoRetencion",		aggregate: "sum" }
													]
												}]
						  }
		});
	};
//Fin GCMBIPAPAS-1	


	

	//Data source eventos de capital cargados en rueda y que falta por cargar en optimus
	obj.dataSourceEventosFaltantes = function() {
		return new bcs.DataSource ( {
			type 		: "call", //Los tipos posibles son: odata, call, array y object
			definition 	: {
							callTransaction		: "2903",
							serverFiltering 	: false, // Filtrado de datos en el servidor.
							serverPaging 		: false, // Paginado de los datos en el servidor.
							serverSorting 		: false, // Ordenamiento de los datos en el servidor.
							callResultSet   	: "P_OUT_EVENTOSFALTANTES",
							sort 				: { field: "Nemotecnico", dir: "asc" },
							filter 				: filtro,
							pageSize 			: 25,
							schema 				: modeloEventosFaltantes()
						  }
		});
	};
	
	
	
	//columnas grilla evento de capital
	obj.columnasEvento = function() {
		return  [
		 {field: "IdTipoEventoCapital", 			title: 'UI_TIPO_EVENTO', 								width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
		,{field: "NumeroDividendo",					title: 'UI_NUMERO_DIVIDENDO',							width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
		,{field: "Secuencia", 						title: 'UI_SECUENCIA', 									width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
		,{field: "Descripcion", 					title: 'UI_DESCRIPCION', 								width: "240px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
		,{field: "IdClasificacionTipoInstrumento", 	title: 'UI_ID_CLASIFICACION_TIPO_INSTRUMENTO', 			width: "40px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
		,{field: "IdTipoInstrumento", 				title: 'UI_TIPO_INSTRUMENTO', 							width: "40px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
		,{field: "Nemo", 							title: 'UI_NEMO_ABR', 									width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
		,{field: "ISIN", 							title: 'UI_ISIN',	 									width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
		,{field: "FechaActualizacion", 				title: 'UI_FECHA_ACTUALIZACION', 						template : " <label> #:  util.formatoDate(FechaActualizacion) #</label>" , 	width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }
		,{field: "FechaLimite", 					title: 'UI_FECHA_LIMITE', 								template : " <label> #:  util.formatoDate(FechaLimite) #</label>" , 			width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }
		,{field: "FechaPago", 						title: 'UI_EVENTO_FECHA_PAGO', 								template : " <label> #:  util.formatoDate(FechaPago) #</label>" ,			 	width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }
		,{field: "IdBoveda", 						title: 'UI_BOVEDA', 									width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
		,{field: "IdMoneda", 						title: 'UI_MONEDA', 									width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "NombreMoneda", 					title: 'UI_MONEDA', 									width: "120px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
		,{field: "MontoRepartido",					title: 'UI_MONTO_REPARTIDO', 							width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"},
		 
			format : function (value, item) {
				return util.bcs_formato(value,'CUS','Monto',item.IdMoneda,item.IdTipoInstrumento,'');
			} }
		//EVENTO CAPITAL
		,{field: "ISFUT", 							title: 'UI_ISFUT', 										width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}		
		,{field: "TasaEfectiva", 					title: 'UI_TASA_EFECTIVA', 								width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
		,{field: "RETART104", 						title: 'UI_RET_ART_104', 								width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
		,{field: "Factor", 							title: 'UI_ACCIONES_ANTIGUAS',							width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "Paridad", 						title: 'UI_PARIDAD', 									width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }		
		,{field: "NemoNuevo", 						title: 'UI_INSTRUMENTO_NUEVO', 							width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
		,{field: "FactorNuevo", 					title: 'UI_ACCIONES_NUEVAS',							width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "NumeroDividendo", 				title: 'UI_NUMERO_DIVIDENDO', 							width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 		, groupable: true}
		,{field: "IdTipoDividendo", 				title: 'UI_TIPO_DIVIDENDO', 							width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "EsInscrito", 						title: 'UI_ES_INSCRITO', 								width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 		, groupable: true}
//		,{field: "PrecioAnterior", 					title: 'UI_PRECIO', 									width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "IdEstado", 						title: 'UI_ESTADO', 									width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
		,{field: "Origen", 							title: 'UI_ORIGEN', 									width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
		,{field: "IdEmisor", 						title: 'UI_EMISOR',	 									width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
		,{field: "EstadoNotificacion",				title: 'UI_NOTIFICACION', 								width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
		,{field: "Indice", 							title: 'UI_INDICE_ACT', 								width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "IndiceRue",						title: 'UI_INDICE_NUE', 								width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "SecuenciaRelacionada",			title: 'Secuencia evento relacionado', 					width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "PaisEmisorInstrumento",			title: 'UI_COL_PAIS_EMISOR_INSTRUMENTO',				width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "GastoTransferencia",				title: 'UI_COL_EC_GASTO_TRANSFERENCIA', 				width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} ,template : " <label> #:  (util.bcs_formato(GastoTransferencia,'CUS','Monto',(IdMoneda != '' ? IdMoneda : 'CLP'),'EVT','')).valor #</label>"}
		
		
		         ];
	};

  
	
	//columnas grilla evento de capital cliente
	obj.columnasEventoCliente = function() {
		 
		
		return  [
		 {field: "IdEventoCapitalCliente",			title: 'UI_TIPO_EVENTO_CLIENTE',								width: "40px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "IdTipoEventoCapital", 			title: 'UI_TIPO_EVENTO', 										width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
		,{field: "NumeroDividendo",					title: 'UI_NUMERO_DIVIDENDO',									width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}		
		,{field: "Secuencia", 						title: 'UI_SECUENCIA', 											width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
		,{field: "TipoSocioNegocio", 				title: 'UI_TIPO_SOCIO_NEGOCIO', 								width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
		,{field: "IdTipoInstrumento", 				title: 'UI_TIPO_INSTRUMENTO', 									width: "40px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
//		,{field: "IdPortafolio", 					title: 'UI_PORTAFOLIO', 										width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
		,{field: "CodigoPortafolio", 				title: 'UI_CODIGOPORTAFOLIO', 									width: "70px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
		,{field: "IdClasificacionTipoInstrumento", 	title: 'UI_CODIGO_CLASIFICACION_TIPO_INSTRUMENTO', 				width: "40px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
		,{field: "Nemo", 							title: 'UI_NEMO_ABR', 											width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
		,{field: "ISIN", 							title: 'UI_ISIN',	 											width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}		
		,{field: "NemoNuevo", 						title: 'UI_NEMO_NUEVO_ABR',										width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
		,{field: "IdConceptoGarantia",				title: 'UI_CONCEPTO_GARANTIA',									width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
		,{field: "TipoCartera", 					title: 'UI_TIPO_CARTERA', 										width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}		
		,{field: "DocumentoIdentidad", 				title: 'UI_RUT', 												width: "70px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
		,{field: "NombreCliente", 					title: 'UI_NOMBRE_CLIENTE', 									width: "230px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
		,{field: "ConfiguracionNotificacion", 		title: 'UI_PREFERENCIAS_NOTIFICACIONES', 						width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
		,{field: "TienePreferenciaDePago", 			title: 'UI_PREFERENCIA_PAGO',									width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
		,{field: "MontoCapital", 					title: 'UI_MONTO_CAPITAL', 										width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , template : " <label> #:  (util.bcs_formato(MontoCapital,'GEN','Monto',(IdMoneda != '' ? IdMoneda : 'CLP'),'','')).valor #</label>", groupFooterTemplate:"#: (util.bcs_formato(sum,'GEN','Monto','"+  $rootScope.SesionIdMonedaSistema +"','','')).valor #"}
		,{field: "Precio", 							title: 'UI_PRECIO',												width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , template : " <label> #:  (util.bcs_formato(Precio,'GEN','Precio',IdMoneda,IdTipoInstrumento,'')).valor #</label>" }
		,{field: "Factor", 							title: 'UI_ACCIONES_ANTIGUAS',									width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "FactorNuevo", 					title: 'UI_ACCIONES_NUEVAS',									width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "PrecioNuevo", 					title: 'UI_PRECIO_NUEVO', 										width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , template : " <label> #:  (util.bcs_formato(Precio,'GEN','Precio',IdMoneda,IdTipoInstrumento,'')).valor #</label>" }
		,{field: "CantidadNueva", 					title: 'UI_CANTIDAD_NUEVA', 			format: "{0:n0}",		width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , template : " <label> #:  (util.bcs_formato(CantidadNueva,'GEN','Monto','CLP','','')).valor #</label>", groupFooterTemplate:"#: (util.bcs_formato(sum,'GEN','Monto','"+  $rootScope.SesionIdMonedaSistema +"','','')).valor #"}
		,{field: "MontoInteres", 					title: 'UI_INTERES', 										width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , template : " <label> #:  (util.bcs_formato(MontoInteres,'GEN','Monto',(IdMoneda != '' ? IdMoneda : 'CLP'),'','')).valor #</label>", groupFooterTemplate:"#: (util.bcs_formato(sum,'GEN','Monto','"+  $rootScope.SesionIdMonedaSistema +"','','')).valor #"}				

		,{field: "MontoReajuste", 					title: 'UI_MONTO_REAJUSTE',				format: "{0:n0}",		width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , template : " <label> #:  (util.bcs_formato(MontoReajuste,'GEN','Monto','CLP','','')).valor #</label>", groupFooterTemplate:"#: (util.bcs_formato(sum,'GEN','Monto','"+  $rootScope.SesionIdMonedaSistema +"','','')).valor #"}
		,{field: "CantidadDerecho", 				title: 'UI_CANTIDAD_DERECHO', 			format: "{0:n0}",		width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , template : " <label> #:  (util.bcs_formato(CantidadDerecho,'CUS','Monto',(IdMoneda != '' ? IdMoneda : 'CLP'),'EVT','')).valor #</label>", groupFooterTemplate:"#: (util.bcs_formato(sum,'CUS','Monto',(angular.element(document.body).scope().$root.SesionIdMonedaSistema != '' ? angular.element(document.body).scope().$root.SesionIdMonedaSistema : 'CLP'),'EVT','')).valor #"}
		,{field: "Total", 							title: 'UI_TOTAL', 												width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , template : " <label> #:  (util.bcs_formato(Total,'CUS','Monto',(IdMoneda != '' ? IdMoneda : 'CLP'),'EVT','')).valor #</label>", groupFooterTemplate:"#: (util.bcs_formato(sum,'GEN','Monto','USD','','')).valor #"}
		,{field: "MontoRetencion",					title: 'UI_MONTO_RETENIDO',										width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , template : " <label> #:  (util.bcs_formato(MontoRetencion,'GEN','Monto',(IdMoneda != '' ? IdMoneda : 'CLP'),'','')).valor #</label>", groupFooterTemplate:"#: (util.bcs_formato(sum,'GEN','Monto','"+  $rootScope.SesionIdMonedaSistema +"','','')).valor #"}

		,{field: "CantidadDerechoArchivo", 			title: 'UI_CANTIDAD_DERECHO_ARCHIVO',	format: "{0:n0}",		width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , template : " <label> #:  (util.bcs_formato(CantidadDerechoArchivo,'GEN','Monto','CLP','','')).valor #</label>", groupFooterTemplate:"#: (util.bcs_formato(sum,'GEN','Monto','"+  $rootScope.SesionIdMonedaSistema +"','','')).valor #"}
		,{field: "AccionesDisponiblesArchivo", 		title: 'UI_CANTIDAD_NUEVA_ARCHIVO',		format: "{0:n0}",		width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , template : " <label> #:  (util.bcs_formato(AccionesDisponiblesArchivo,'GEN','Monto','CLP','','')).valor #</label>", groupFooterTemplate:"#: (util.bcs_formato(sum,'GEN','Monto','"+  $rootScope.SesionIdMonedaSistema +"','','')).valor #"}
		,{field: "DiferenciaCantidadDerecho", 		title: 'UI_DIFERENCIA_CANTIDAD_DERECHO',format: "{0:n0}",		width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , template : " <label> #:  (util.bcs_formato(DiferenciaCantidadDerecho,'GEN','Monto','CLP','','')).valor #</label>", groupFooterTemplate:"#: (util.bcs_formato(sum,'GEN','Monto','"+  $rootScope.SesionIdMonedaSistema +"','','')).valor #"}
		,{field: "DiferenciaAccionesCantidadNueva",	title: 'UI_DIFERENCIA_CANTIDAD_NUEVA',	format: "{0:n0}",		width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , template : " <label> #:  (util.bcs_formato(DiferenciaAccionesCantidadNueva,'GEN','Monto','CLP','','')).valor #</label>", groupFooterTemplate:"#: (util.bcs_formato(sum,'GEN','Monto','"+  $rootScope.SesionIdMonedaSistema +"','','')).valor #"}
		,{field: "MontoBeneficioArchivo", 			title: 'UI_MONTO_BENEFICIO_ARCHIVO',	format: "{0:n0}",		width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , template : " <label> #:  (util.bcs_formato(MontoBeneficioArchivo,'GEN','Monto',(IdMoneda != '' ? IdMoneda : 'CLP'),'','')).valor #</label>", groupFooterTemplate:"#: (util.bcs_formato(sum,'GEN','Monto','"+  $rootScope.SesionIdMonedaSistema +"','','')).valor #"}
		,{field: "DiferenciaMontoBeneficio", 		title: 'UI_DIFERENCIA_MONTO_BENEFICIO', format: "{0:n0}",		width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , template : " <label> #:  (util.bcs_formato(DiferenciaMontoBeneficio,'GEN','Monto',(IdMoneda != '' ? IdMoneda : 'CLP'),'','')).valor #</label>", groupFooterTemplate:"#: (util.bcs_formato(sum,'GEN','Monto','"+  $rootScope.SesionIdMonedaSistema +"','','')).valor #"}
		,{field: "MontoInteresArchivo", 			title: 'UI_MONTO_INTERES_ARCHIVO', 		format: "{0:n0}",		width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , template : " <label> #:  (util.bcs_formato(MontoInteresArchivo,'GEN','Monto',(IdMoneda != '' ? IdMoneda : 'CLP'),'','')).valor #</label>", groupFooterTemplate:"#: (util.bcs_formato(sum,'GEN','Monto','"+  $rootScope.SesionIdMonedaSistema +"','','')).valor #"}
		,{field: "MontoCapitalArchivo", 			title: 'UI_MONTO_CAPITAL_ARCHIVO',		format: "{0:n0}",		width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , template : " <label> #:  (util.bcs_formato(MontoCapitalArchivo,'GEN','Monto',(IdMoneda != '' ? IdMoneda : 'CLP'),'','')).valor #</label>", groupFooterTemplate:"#: (util.bcs_formato(sum,'GEN','Monto','"+  $rootScope.SesionIdMonedaSistema +"','','')).valor #"}
		,{field: "DiferenciaMontoInteres", 			title: 'UI_DIFERENCIA_MONTO_INTERES',	format: "{0:n0}",		width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , template : " <label> #:  (util.bcs_formato(DiferenciaMontoInteres,'GEN','Monto',(IdMoneda != '' ? IdMoneda : 'CLP'),'','')).valor #</label>", groupFooterTemplate:"#: (util.bcs_formato(sum,'GEN','Monto','"+  $rootScope.SesionIdMonedaSistema +"','','')).valor #"}
		,{field: "DiferenciaMontoCapital", 			title: 'UI_DIFERENCIA_MONTO_CAPITAL', 	format: "{0:n0}",		width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , template : " <label> #:  (util.bcs_formato(DiferenciaMontoCapital,'GEN','Monto',(IdMoneda != '' ? IdMoneda : 'CLP'),'','')).valor #</label>", groupFooterTemplate:"#: (util.bcs_formato(sum,'GEN','Monto','"+  $rootScope.SesionIdMonedaSistema +"','','')).valor #"}
		,{field: "MontoRetencionArchivo", 			title: 'UI_MONTO_RETENIDO_ARCHIVO',		format: "{0:n0}",		width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , template : " <label> #:  (util.bcs_formato(MontoRetencionArchivo,'GEN','Monto',(IdMoneda != '' ? IdMoneda : 'CLP'),'','')).valor #</label>", groupFooterTemplate:"#: (util.bcs_formato(sum,'GEN','Monto','"+  $rootScope.SesionIdMonedaSistema +"','','')).valor #"}
		,{field: "DiferenciaMontoRetencion",		title: 'UI_DIFERENCIA_MONTO_RETENIDO',	format: "{0:n0}",		width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , template : " <label> #:  (util.bcs_formato(DiferenciaMontoRetencion,'GEN','Monto',(IdMoneda != '' ? IdMoneda : 'CLP'),'','')).valor #</label>", groupFooterTemplate:"#: (util.bcs_formato(sum,'GEN','Monto','"+  $rootScope.SesionIdMonedaSistema +"','','')).valor #"}

		,{field: "IdMoneda", 						title: 'UI_MONEDA', 											width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
		,{field: "FechaPago", 						title: 'UI_EVENTO_FECHA_PAGO', 									template : " <label> #:  util.formatoDate(FechaPago) #</label>" ,			 	width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }
		,{field: "HoraIngresoFechaPago", 			title: 'UI_EVENTO_HORA_INGRESO_FECHA_PAGO',				 		template : " <label> #:  util.formatoTime(util.dateDS(HoraIngresoFechaPago,'FechaHoraSistema','CALL')) #</label>" ,	width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "CodigoUsuarioAgente", 			title: 'UI_AGENTE', 											width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "NombreAgente", 					title: 'UI_NOMBRE', 											width: "180px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "IdBoveda", 						title: 'UI_BOVEDA', 											width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}		
		,{field: "IdEstado", 						title: 'UI_ESTADO', 											width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
		,{field: "Origen", 							title: 'UI_ORIGEN', 											width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
		,{field: "EstadoNotificacion",				title: 'UI_NOTIFICACION', 										width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
		,{field: "NombreMedioDePago",				title: 'UI_MEDIO_PAGO',											width: "120px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "TipoCuentaBoveda",				title: 'UI_TIPO_CUENTA_BOVEDA',									width: "120px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }				
		,{field: "NumeroCuentaBoveda",				title: 'UI_NUMERO_CUENTA_BOVEDA',								width: "120px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }			
    	,{field: "PagaClienteCorredor",				title: 'UI_CLIENTE_CORREDOR',									width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }			
    	,{field: "IdTipoReajuste",					title: 'UI_TIPO_REAJUSTE',										width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "AfectoaClearing",					title: 'UI_AFECTOACLEARING',									width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
		,{field: "ProcesadoClearing",				title: 'UI_PROCESADOCLEARING',									width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
		,{field: "IdRegimenFondoMutuo",				title: 'UI_REGIMEN',											width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
		,{field: "ISFUT", 							title: 'UI_ISFUT', 												width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
		,{field: "RetencionISFUT", 					title: 'UI_RETENCION_ISFUT', 									width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
		,{field: "Modalidad", 						title: 'UI_MODALIDAD', 									        width: "120px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
		,{field: "MedioDePago", 					title: 'UI_MEDIO_DE_PAGO', 										width: "120px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
		,{field: "ImpuestoDivExtranjero", 			title: 'UI_COL_ECC_IMPUESTO_DIV_EXTRANJERO', format: "{0:n4}",  width: "150px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "MontoImpuestoExtranjero",			title: 'UI_COL_MONTO_IMPUESTO_EXTRANJERO', 						width: "150px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , template : " <label> #:  (util.bcs_formato(MontoImpuestoExtranjero,'CUS','Monto',(IdMoneda != '' ? IdMoneda : 'CLP'),'EVT','')).valor #</label>"}
		,{field: "GastoTransferencia",				title: 'UI_COL_ECC_GASTO_TRANSFERENCIA', 						width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , template : " <label> #:  (util.bcs_formato(GastoTransferencia,'CUS','Monto',(IdMoneda != '' ? IdMoneda : 'CLP'),'EVT','')).valor #</label>"}
		];
	};

	

	

	//columnas grilla eventos capital faltantes (cargados en ruebas y no en optimus)
	obj.columnasEventosFaltantes = function() {
		return  [

		 {field: "TipoEventoCapital", 			title: 'UI_TIPO_EVENTO', 			width: "70px", attributes:{style:"text-align:left;"},  groupable: true}
		,{field: "Nemotecnico", 				title: 'UI_NEMOTECNICO', 			width: "90px", attributes:{style:"text-align:left;"},  groupable: true}		
		,{field: "Secuencia", 					title: 'UI_SECUENCIA', 				width: "60px", attributes:{style:"text-align:left;"},  groupable: true}
		,{field: "Descripcion", 				title: 'UI_DESCRIPCION', 			width: "200px", attributes:{style:"text-align:left;"},  groupable: true}
		,{field: "FechaLimite", 				title: 'UI_FECHA_LIMITE', 			width: "80px",  template : " <label> #:  util.formatoDate(FechaLimite) #</label>",  groupable: true}
		,{field: "FechaPago", 					title: 'UI_FECHA_PAGO', 			width: "80px",  template : " <label> #:  util.formatoDate(FechaPago) #</label>",  groupable: true}

		];
	};
	
	
	
	
	//columnas grilla Formas de Pago evento de capital cliente

	obj.columnasEventoClienteFormaDePago = function() {
		return  [
		 {field: "IdEventoCapitalClienteFormaDePago",	title: 'UI_ID_EVENTO_CAPITAL_CLIENTE_FORMA_PAGO',		width: "40px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "IdTipoMovimiento",					title: 'UI_TIPO_MOVIMIENTO', 							width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "IdModalidadPago",						title: 'UI_MODALIDAD_PAGO',								width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "NombreMedioDePago", 					title: 'UI_MEDIO_DE_PAGO', 								width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "NombreBanco", 						title: 'UI_BANCO',	 									width: "70px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }		
		,{field: "NombreTipoCuentaBancaria",			title: 'UI_TIPO_CUENTA_BANCARIA',				 		format: "{0:n0}",		width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "IdCuentaBancaria",					title: 'UI_CUENTA_BANCARIA', 		format: "{0:n0}",	width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "NombreCuentaBancaria",				title: 'UI_NOMBRE_CUENTA_BANCARIA',	format: "{0:n0}",	width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "NumeroCuentaBancaria",	 			title: 'UI_CUENTA_BANCARIA', 							width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "NumeroTarjetaBancaria",				title: 'UI_TARJETA_BANCARIA', 							width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "NumeroMedioDePago",					title: 'UI_MEDIO_DE_PAGO', 								width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "Sucursal",							title: 'UI_SUCURSAL', 				format: "{0:n0}",	width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "FechaVctoDocumento",	 				title: 'UI_FECHA_VENCIMIENTO', 							template : " <label> #:  util.formatoDate(FechaVctoDocumento) #</label>" ,			 	width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }
		,{field: "Monto",								title: 'UI_MONTO', 					format: "{0:n0}",	width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "FechaHoraIngresoRegistro",			title: 'UI_FECHA_INGRESO', 								template : " <label> #:  util.formatoDate(FechaHoraIngresoRegistro) #</label>" ,			 	width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }
		,{field: "IdUsuarioIngreso",					title: 'UI_USUARIO', 				format: "{0:n0}",	width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "IdEstado",							title: 'UI_ESTADO', 				format: "{0:n0}",	width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }

		
	         
		];
	};
	return obj;
}]);	








