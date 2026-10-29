angular.module('ModuloTesoreria').factory('bcsBlotterComprobantesGrid',['bcsComunes',
		function(bcsComunes)  
{
	var obj = {};	
	

	//MOVIMIENTO DE INGRESOS Y EGRESOS.
	obj.dsDefMovimientoIngresoEgreso = {
		type : "odata",
		definition : {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: true, // Ordenamiento de los datos en el servidor.
			pageSize 			: 100,
			sort 				: { field: "IdIngresoEgreso", dir: "desc" },						
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1031",dataType : "json"}},
			schema : {
				model : {
					fields : {								
						IdIngresoEgreso					: { type: "number"},
						NombreTipoMovimiento			: { type: "string"},
						IdConcepto						: { type: "string"},
						NombreConcepto					: { type: "string"},
						IdMoneda						: { type: "string"},
						Monto							: { type: "number"},
						FechaLiquidacion				: { type: "sysdate"},
						FechaIngresoRegistro			: { type: "sysdate"},
						NombrePersona					: { type: "string"},  
						NombreTipoSocioDeNegocio		: { type: "string"},
						DocumentoIdentidad				: { type: "string"},
						NombrePersonaTitularPago		: { type: "string"},
						NombrePortafolioTitularPago		: { type: "string"},
						NombrePortafolio 				: { type: "string"},
						NombreEstado					: { type: "string"},						
						NombreCaja						: { type: "string"},
						IdTipoComprobanteOrigen			: { type: "string"},
						IdComprobanteOrigen				: { type: "number"},
						EsIngresoAutomatico             : { type: "string"},
						NeteaComprobante             	: { type: "string"},
						AgrupaComprobante             	: { type: "string"},
						MueveCuentaInversion			: { type: "string"},						
						FechaHoraAnulacion				: { type: "datetime"},
						IdComprobante					: { type: "number"},
						IdTipoMovimiento				: { type: "string"},
						SolicitudAutorizacion			: { type: "string"},
						Autorizacion					: { type: "string"},
						NombreEstadoAutorizacion		: { type: "string"},
						SecuenciaRelacionada			: { type: "number"},
						FolioComprobanteDTE				: { type: "number"},
						CondicionDeLiquidacion			: { type: "string"},
						NombreEmpleadoDigitador		    : { type: "string"},

						//DATOS OPERACION
						IdComprobanteOrigen				  : { type: "number"},
						FechaComprobanteOrigen			  : { type: "sysdate"},
						FechaLiquidacionComprobanteOrigen : { type: "sysdate"},						
						NombreTipoProducto				  : { type: "string"},
						NombreTipoOperacion				  : { type: "string"},
						FechaOperacion					  : { type: "sysdate"},
						FechaVencimientoOperacion		  : { type: "sysdate"},
						Nemotecnico						  : { type: "string"},
						NombreUsuarioEmision			  : { type: "string"},
						IdClasificacionTipoInstrumento	  : { type: "string"},
						IdTipoInstrumento  				  : { type: "string"},
						
						//Datos de la forma de pago(solo cuando hay 1 medio de pago por comprobante) 
						IdMedioDePago						: { type: "string"},
						NombreMedioDePago					: { type: "string"},
						IdModalidadDePago					: { type: "string"},
						NombreModalidadDePago				: { type: "string"},
						NombreBancoCorredora				: { type: "string"},
						NumeroCuentaBancariaCorredora		: { type: "string"},
						NombreTipoCuentaBancariaCorredora	: { type: "string"},	   
						NombreBancoSocio					: { type: "string"},
						NumeroCuentaBancariaSocio			: { type: "string"},
						NombreTipoCuentaBancariaSocio		: { type: "string"},
						CuentaDCVOrigen                     : { type: "string"},
						CuentaDCVDestino                    : { type: "string"},
						AliasBanco                          : { type: "string"},
						IdTipoCanalIngreso                  : { type: "string"},
						Glosa                  				: { type: "string"},
						EstadoCaja                          : { type: "string"}
					}
				}
			},
							
		}
	};
	obj.columnasIngresosEgresos = function() {
		return  [
			 {field: "IdIngresoEgreso", 			title: "UI_SECUENCIA_MOVIMIENTO", 			width: "60px"}
			,{field: "NombreTipoMovimiento", 		title: "UI_TIPO", 							width: "50px", 	groupable: true }
			,{field: "IdConcepto", 				    title: "UI_ID_CONCEPTO", 					width: "80px",	groupable: true }
			,{field: "NombreConcepto", 				title: "UI_CONCEPTO", 						width: "250px",	groupable: true }
			,{field: "IdTipoComprobanteOrigen",		title: "UI_TIPO_COMPROBANTE_ORIGEN", 		width: "60px",	groupable: true }
			,{field: "IdComprobanteOrigen", 		title: "UI_SECUENCIA_COMPROBANTE_ORIGEN", 	width: "60px",	groupable: true }
			,{field: "SecuenciaRelacionada", 		title: "UI_SECUENCIA_RELACIONADA", 			width: "60px",	groupable: true }
			,{field: "FolioComprobanteDTE", 		title: "UI_FOLIO_DTE",						width: "60px"}
			,{field: "FechaComprobanteOrigen", 		title: "UI_FECHA_COMPROBANTE_ORIGEN", 		width: "75px"}
			,{field: "FechaLiquidacion", 			title: "UI_FECHA_LIQUIDACION", 				width: "75px",	groupable: true }			
			,{field: "Monto", 						title: "UI_MONTO", 							width: "90px",	template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#= util.formateaNumero(sum,0) #", footerAttributes:{style:"text-align: right; "} }
			,{field: "NombreCaja", 					title: "UI_CAJA", 							width: "80px",	groupable: true }
			,{field: "IdMoneda", 					title: "UI_MONEDA", 						width: "65px",	groupable: true }
			,{field: "NombreTipoProducto", 			title: "UI_TIPO_PRODUCTO", 					width: "150px",	groupable: true }
			,{field: "NombreTipoOperacion", 		title: "UI_TIPO_OPERACION", 				width: "200px",	groupable: true }
			,{field: "Nemotecnico", 				title: "UI_INSTRUMENTO", 					width: "200px",	groupable: true }
			,{field: "NombrePersona",	 			title: "UI_SOCIO_NEGOCIO", 					width: "200px",	groupable: true }					
			
			,{field: "NombreTipoSocioDeNegocio",	title: "UI_TIPO_SOCIO_NEGOCIO", 			width: "80px",	groupable: true }
			,{field: "DocumentoIdentidad", 			title: "UI_DOCUMENTO_IDENTIDAD", 			width: "90px",	groupable: true }
			,{field: "NombrePortafolio", 			title: "UI_PORTAFOLIO", 			        width: "90px",	groupable: true }
					
			,{field: "NombrePersonaTitularPago", 	title: "UI_PERSONA_PAGO_COBRO", 			width: "200px",	groupable: true }			
			,{field: "NombrePortafolioTitularPago", title: "UI_PORTAFOLIO_PAGO_COBRO", 			width: "130px",	groupable: true }	
			
			
			,{field: "MueveCuentaInversion", 		title: "UI_MUEVE_CTA", 						width: "50px",	groupable: true, align: "center"}
			,{field: "AgrupaComprobante", 			title: "UI_AGRUPA_COMPROBANTE", 			width: "70px",	groupable: true, align: "center"}
			,{field: "NeteaComprobante", 			title: "UI_NETEA_COMPROBANTE", 				width: "70px",	groupable: true, align: "center"}
			,{field: "SolicitudAutorizacion", 		title: "UI_SOLICITUD_AUTORIZACION", 		width: "200px",	groupable: true }
			,{field: "NombreEstadoAutorizacion", 	title: "UI_ESTADO_AUTORIZACION", 			width: "80px",	groupable: true }
			,{field: "FechaIngresoRegistro", 		title: "UI_FECHA_INGRESO",	 			    width: "90px",	groupable: true, attributes:{style:"white-space:nowrap;"} }
			,{field: "EsIngresoAutomatico", 		title: "UI_INGRESO_AUTOMATICO", 			width: "70px",	groupable: true, align: "center"}			
			,{field: "IdComprobante", 				title: "UI_SECUENCIA_COMPROBANTE", 			width: "70px",	groupable: true }
			,{field: "NombreUsuarioEmision", 		title: "UI_EMITIDO_POR", 					width: "150px",	groupable: true }			
			,{field: "FechaHoraAnulacion", 		    title: "UI_FECHA_ANULACION",	 			width: "90px",	groupable: true , attributes:{style:"white-space:nowrap;"}}
			
			//Datos de la forma de pago (Corredora/Socio)(Solo será visible cuando haya 1 medio de pago x movimiento)
			,{field: "NombreMedioDePago", 			title: "UI_MEDIO_DE_PAGO", 					width: "150px",	groupable: true}			
		    ,{title   : "UI_CORREDORA", align: "center",
			   columns : [
			              	 {field : "NombreBancoCorredora",  	           title:'UI_BANCO',             width : "160px",	groupable: true, attributes:{style:"white-space:nowrap;"} }
			              	,{field : "AliasBanco",  	                   title:'UI_ALIAS_CUENTA_BANCO', width : "160px",	groupable: true, attributes:{style:"white-space:nowrap;"} }
				  			,{field : "NombreTipoCuentaBancariaCorredora", title:'UI_TIPO_CUENTA_BANCO', width : "110px",	groupable: true, attributes:{style:"white-space:nowrap;"} }
							,{field : "NumeroCuentaBancariaCorredora",     title:'UI_NUMERO_CUENTA',     width : "90px",	groupable: true	}
							,{field : "CuentaDCVOrigen",     		       title:'UI_CUENTA_DCV_ORIGEN',  width : "90px",	groupable: true	}	
			]}
		    ,{title   : "UI_SOCIO_NEGOCIO", align: "center",
			  columns : [
			             	 {field : "NombreBancoSocio",     		   title:'UI_BANCO',             width : "160px",	groupable: true, attributes:{style:"white-space:nowrap;"} }
							,{field : "NombreTipoCuentaBancariaSocio", title:'UI_TIPO_CUENTA_BANCO', width : "110px",	groupable: true, attributes:{style:"white-space:nowrap;"} }
							,{field : "NumeroCuentaBancariaSocio",     title:'UI_NUMERO_CUENTA',     width : "90px",	groupable: true, attributes:{style:"white-space:nowrap;"} }
							,{field : "CuentaDCVDestino",     		   title:'UI_CUENTA_DCV_DESTINO',     width : "90px",groupable: true, attributes:{style:"white-space:nowrap;"} }
			]}
		    ,{field: "NombreEstado", 				title: "UI_ESTADO", 						width: "110px",	groupable: true, attributes:{style:"white-space:nowrap;"} }
		    ,{field: "IdTipoCanalIngreso", 			title: "UI_TIPO_CANAL_INGRESO", 			width: "110px",	groupable: true, attributes:{style:"white-space:nowrap;"} }
		    ,{field: "CondicionDeLiquidacion",		title: "UI_COND_DE_LIQUIDACION",			width: "60px",	groupable: true }
			,{field: "Glosa",		title: "UI_COMENTARIO",			width: "100px",	groupable: true }

			,{field: "IdClasificacionTipoInstrumento", 	title: "UI_MERCADO", 						width: "120px",	groupable: true }
			,{field: "IdTipoInstrumento", 				title: "UI_TIPO_INSTRUMENTO", 				width: "120px",	groupable: true }
			,{field: "NombreEmpleadoDigitador", 		title: "UI_EMPLEADO_DIGITADOR", 			width: "250px",	groupable: true }
			,{field: "EstadoCaja", title: "UI_ESTADO_CAJA", width: "100px", sortable : false, hidden: true}
		];
	};

	
	
	
	//FORMA DE PAGO DE INGRESO Y EGRESO
	obj.dsDefMovimientoIngresoEgresoFormaDePago = {
		type : "odata",
		definition : {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.
			sort 				: { field: "IdIngresoEgresoFormaDePago", dir: "asc" }, 
			pageSize 			: 5,								
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1032",dataType : "json"}},
			schema : {
				model : {
					fields : {								
						IdIngresoEgreso              	   	: { type: "number"},
						NombreMedioDePago           		: { type: "string"},   
						NombreModalidadDePago       		: { type: "string"},					 
						IdTipoCuentaBancariaCorredora		: { type: "string"},	 
						NombreTipoCuentaBancariaCorredora	: { type: "string"},	 
						NombreCuentaBancariaCorredora		: { type: "string"},
						NumeroCuentaBancariaCorredora		: { type: "string"},
						IdTipoCuentaBancariaSocio			: { type: "string"},
						NombreTipoCuentaBancariaSocio		: { type: "string"},					 
						NombreCuentaBancariaSocio			: { type: "string"},
						NumeroCuentaBancariaSocio			: { type: "string"},
						IdMoneda							: { type: "string"},					 
						NombreBancoSocio					: { type: "string"},					 
						Monto              		    		: { type: "number"},
						FechaDocumentoPagoSocio				: { type: "sysdate"},
						NumeroDocumentoSocio				: { type: "number"},
						NumeroSerieSocio					: { type: "string"},
						IdComprobante						: { type: "number"},
						NombreBancoCorredora				: { type: "string"},			
						NumeroDocumentoCorredora			: { type: "number"},
						NumeroSerieCorredora				: { type: "string"},
						NombreEstado						: { type: "string"},						
						

						//Columnas ocultas (Se usan para editar forma de pago en forma desconectada)
						IdMedioDePago						: { type: "string"},
						IdModalidadDePago					: { type: "string"},
						IdCuentaBancariaCorredora			: { type: "number"},
						IdCuentaBancariaSocio				: { type: "number"},
						IdTarjetaBancariaCorredora			: { type: "number"},
						IdTarjetaBancariaSocio				: { type: "number"},
						IdBancoSocio						: { type: "number"},
						IdChequera							: { type: "number"},						
						FechaDocumentoPagoCorredora         : { type: "sysdate"},
						
					}
				}
			},
						
		}
	};
	
	
	
	obj.getDsMovimientoIngresoEgresoFormaDePago = function(datachange) {
		return new bcs.DataSource({
			type : "odata",
			definition : {
				dataChange			: datachange,
				serverFiltering 	: true, // Filtrado de datos en el servidor.
				serverPaging 		: true, // Paginado de los datos en el servidor.
				serverSorting 		: false, // Ordenamiento de los datos en el servidor.
				sort 				: { field: "IdIngresoEgresoFormaDePago", dir: "asc" },
				pageSize 			: 5,
				transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1032",dataType : "json"}},
				schema : {
					model : {
						fields : {								
							IdIngresoEgreso              	   	: { type: "number"},
							NombreMedioDePago           		: { type: "string"},   
							NombreModalidadDePago       		: { type: "string"},					 
							IdTipoCuentaBancariaCorredora		: { type: "string"},	 
							NombreTipoCuentaBancariaCorredora	: { type: "string"},	 
							NombreCuentaBancariaCorredora		: { type: "string"},
							NumeroCuentaBancariaCorredora		: { type: "string"},
							IdTipoCuentaBancariaSocio			: { type: "string"},
							NombreTipoCuentaBancariaSocio		: { type: "string"},					 
							NombreCuentaBancariaSocio			: { type: "string"},
							NumeroCuentaBancariaSocio			: { type: "string"},
							IdMoneda							: { type: "string"},					 
							NombreBancoSocio					: { type: "string"},					 
							Monto              		    		: { type: "number"},
							FechaDocumentoPagoSocio				: { type: "sysdate"},
							NumeroDocumentoSocio				: { type: "number"},
							NumeroSerieSocio					: { type: "string"},							
							NombreBancoCorredora				: { type: "string"},			
							NumeroDocumentoCorredora			: { type: "number"},
							NumeroSerieCorredora				: { type: "string"},
							NombreEstado						: { type: "string"},							
							NombreBancoCorredora				: { type: "string"},			
							NumeroDocumentoCorredora			: { type: "number"},
							NumeroSerieCorredora				: { type: "string"},
							NombreEstado						: { type: "string"},
							
							//Columnas ocultas (Se usan para editar forma de pago en forma desconectada)
							IdMedioDePago						: { type: "string"},
							IdModalidadDePago					: { type: "string"},
							IdCuentaBancariaCorredora			: { type: "number"},
							IdCuentaBancariaSocio				: { type: "number"},
							IdTarjetaBancariaCorredora			: { type: "number"},
							IdTarjetaBancariaSocio				: { type: "number"},
							IdBancoSocio						: { type: "number"},
							IdChequera							: { type: "number"},	
							FechaDocumentoPagoCorredora         : { type: "sysdate"},
							

						}
					}
				},
				aggregate			: ([ //Para resumen de totales de cobro
										{ field: "Monto", aggregate: "sum" },	
										])
			}	
		});
	}	
	
	
	
	obj.columnasIngresoEgresoFormaDePago = function() {
		return  [
			  {field : "NombreMedioDePago",     title:'UI_MEDIO_DE_PAGO', 	width :"150px"}
			 ,{field : "NombreModalidadDePago", title:'UI_MODALIDAD', 		width :"80px"}
			 ,{field : "IdMoneda", 				title:"UI_MONEDA", 			width: "60px"}
			 ,{field : "Monto", 				title:'UI_MONTO', 			width :"50px",	template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>" }

			 //Datos de la Corredora
			 ,{title   : "UI_CORREDORA", align: "center",
			   columns : [
							 {field : "NombreBancoCorredora",  		       title:'UI_BANCO',           		width : "100px"}
							,{field : "NombreTipoCuentaBancariaCorredora", title:'UI_TIPO_CUENTA_BANCARIA', width : "100px"}
							,{field : "NombreCuentaBancariaCorredora",     title:'UI_CUENTA_BANCARIA',      width : "100px"}
							,{field : "NumeroCuentaBancariaCorredora",     title:'UI_NUMERO_CUENTA',        width : "80px"}
							,{field : "NumeroSerieCorredora",     		   title:'UI_SERIE_CHEQUERA', 		width : "80px"}
							,{field : "FechaDocumentoPagoCorredora",	   title:'UI_FECHA_DOCUMENTO', 		width : "80px"}							
							,{field : "NumeroDocumentoCorredora",   	   title:'UI_FOLIO_DOCUMENTO', 		width : "80px"}
						]} 

			 //Datos del Socio 
			 ,{title   : "UI_SOCIO_NEGOCIO", align: "center",
			  columns : [				             
							 {field : "NombreBancoSocio",     		   title:'UI_BANCO',           		width : "100px"}
							,{field : "NombreTipoCuentaBancariaSocio", title:'UI_TIPO_CUENTA_BANCO', 	width : "100px"}
							,{field : "NombreCuentaBancariaSocio",     title:'UI_CUENTA_BANCARIA', 		width : "100px"}
							,{field : "NumeroCuentaBancariaSocio",     title:'UI_NUMERO_CUENTA',   		width : "80px"}
							,{field : "NumeroSerieSocio",     		   title:'UI_SERIE_CHEQUERA', 		width : "80px"}
							,{field : "FechaDocumentoPagoSocio",	   title:'UI_FECHA_DOCUMENTO', 		width : "80px"}
							,{field : "NumeroDocumentoSocio",     	   title:'UI_FOLIO_DOCUMENTO', 		width : "80px"}

			  ]}			     
			 ,{field : "NombreEstado",	title:'UI_ESTADO',	width : "80px",	sortable : false}
			 
				
		     //Columnas ocultas (Se usan para editar forma de pago en forma desconectada)
		     ,{field : "IdMedioDePago",                title:'UI_MEDIO_DE_PAGO',     hidden: true  }				
		     ,{field : "IdModalidadDePago",            title:'UI_MODALIDAD',         hidden: true  }		     
		     ,{field : "IdCuentaBancariaCorredora",    title:'UI_TIPO_CUENTA_BANCO', hidden: true  }		     
			 ,{field : "IdCuentaBancariaSocio",        title:'UI_TIPO_CUENTA_BANCO', hidden: true  }			 
			 ,{field : "IdTarjetaBancariaCorredora",   title:'UI_TARJETA_BANCARIA',  hidden: true  }			 
			 ,{field : "IdTarjetaBancariaSocio",       title:'UI_TARJETA_BANCARIA',  hidden: true  }
			 ,{field : "IdBancoSocio",                 title:'UI_BANCO',             hidden: true  }			 
			 ,{field : "IdChequera",                   title:'UI_CHEQUERA',          hidden: true  }
			 
		];
	};

	
	//COMPROBANTES
	obj.dsDefComprobantes = {
		type : "odata",
		definition : {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: true, // Ordenamiento de los datos en el servidor.
			pageSize 			: 100,
			sort 				: { field: "IdComprobante", dir: "desc" }, 									
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1020",dataType : "json"}},
			schema 				: {
				model : {
					fields : {								
						IdComprobante 					: { type: "number"},
						CuentaDCVOrigen                 : { type: "string"},
						CuentaDCVDestino                : { type: "string"},
						NombreTipoMovimiento 			: { type: "string"},					
						IdMoneda 						: { type: "string"},
						IdComprobanteOrigen				: { type: "number"},
						FolioComprobanteDTE				: { type: "number"},
						Monto 							: { type: "number"},
						FechaLiquidacion 				: { type: "sysdate"},
						NombrePersona 					: { type: "string"},
						DocumentoIdentidad 				: { type: "string"},
						NombreTipoSocioDeNegocio		: { type: "string"},
						NombrePersonaTitularPago		: { type: "string"},
						NombrePortafolioTitularPago		: { type: "string"},
						NombreCaja 						: { type: "string"},
						NombreEstado 					: { type: "string"},
						IdConcepto					    : { type: "string"},
						NombreConcepto					: { type: "string"},
						SolicitudAutorizacion			: { type: "string"},
						Autorizacion					: { type: "string"},
						NombreEstadoAutorizacion		: { type: "string"},
						ComprobanteNeteado				: { type: "string"},
						IdClasificacionTipoInstrumento  : { type: "string"},
						IdTipoInstrumento			    : { type: "string"},
						MueveCuentaInversion			: { type: "string"},
						NombreUsuarioLiquidacion		: { type: "string"},
						FechaIngresoRegistro			: { type: "datetime"},
						FechaHoraAnulacion				: { type: "datetime"},
						CuentaDCVOrigen				    : { type: "string"},
						CuentaDCVDestino				: { type: "string"},
						// Datos solo para el primer origen(ya que puede tener n)
						IdTipoComprobanteOrigen			: { type: "string"},   
						FechaComprobanteOrigen			: { type: "sysdate"},

						//Datos de la forma de pago(solo cuando hay 1 medio de pago por comprobante) 
						IdMedioDePago						: { type: "string"},
						NombreMedioDePago					: { type: "string"},
						IdModalidadDePago					: { type: "string"},
						NombreModalidadDePago				: { type: "string"},
						NombreBancoCorredora				: { type: "string"},
						AliasBanco				            : { type: "string"},
						NumeroCuentaBancariaCorredora		: { type: "string"},
						NombreTipoCuentaBancariaCorredora	: { type: "string"},	   
						NombreBancoSocio					: { type: "string"},
						NumeroCuentaBancariaSocio			: { type: "string"},
						NombreTipoCuentaBancariaSocio		: { type: "string"},

						//Relacion comprobante y partida de egreso
						IdComprobantePartidaEgreso			: { type: "string"},
						NombreEstadoNomina					: { type: "string"}

										
						,IdPortafolio						: { type: "number"} // portafolio Socio de negocio
						,NombrePortafolio					: { type: "string"}
						,IdTipoCanalIngreso		            : { type: "string"}
						,Alias		            			: { type: "string"}
						,Nemotecnico		            	: { type: "string"}
						,CondicionDeLiquidacion				: { type: "string"}
						,Glosa								: { type: "string"}
						,EstadoCaja                         : { type: "string"}
					}
				}
			},
			
		}
	};
	obj.columnasComprobantes = function() {
		return  [
    		{field: "IdComprobante", 				title: "UI_SECUENCIA_COMPROBANTE", 	width: "70px",}
    		,{field: "NombreTipoMovimiento", 		title: "UI_TIPO_COMPROBANTE", 		width: "100px",	groupable: true }
			,{field: "Nemotecnico", 				title: "UI_INSTRUMENTO", 					width: "200px",	groupable: true }
    		,{field: "IdConcepto", 				    title: "UI_ID_CONCEPTO", 			width: "80px",	groupable: true, sortable : false }
			,{field: "NombreConcepto", 				title: "UI_CONCEPTO", 				width: "250px",	groupable: true, sortable : false }			
    		,{field: "IdMoneda", 					title: "UI_MONEDA", 				width: "80px",	groupable: true }
    		,{field: "IdComprobanteOrigen", 		title: "UI_SECUENCIA_COMPROBANTE_ORIGEN",	width: "80px" }
    		,{field: "FolioComprobanteDTE",			title: "UI_FOLIO_DTE",				width: "80px"}
    		,{field: "Monto", 						title: "UI_MONTO", 					width: "80px",	template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#= util.formateaNumero(sum,0) #", footerAttributes:{style:"text-align: right; "} }
			,{field: "FechaLiquidacion", 			title: "UI_FECHA_LIQUIDACION", 		width: "70px",	groupable: true}
			,{field: "NombrePersona", 				title: "UI_SOCIO_NEGOCIO", 			width: "200px",	groupable: true }			
			,{field: "DocumentoIdentidad", 			title: "UI_DOCUMENTO_IDENTIDAD",	width: "70px",	groupable: true }
			,{field: "NombrePortafolio", 			title: "UI_PORTAFOLIO", 			width: "90px",	groupable: true }
			

			,{field: "NombreTipoSocioDeNegocio", 	title: "UI_TIPO_SOCIO_NEGOCIO", 	width: "80px",	groupable: true }
			
			,{field: "NombrePersonaTitularPago", 	title: "UI_PERSONA_PAGO_COBRO", 	width: "200px",	groupable: true }			
		
			,{field: "NombrePortafolioTitularPago", title: "UI_PORTAFOLIO_PAGO_COBRO", 	width: "140px",	groupable: true }

			
		
			,{field: "MueveCuentaInversion", 		title: "UI_MUEVE_CTA", 				width: "50px",	groupable: true, align: "center"}			
			,{field: "NombreCaja", 					title: "UI_CAJA", 					width: "80px",	groupable: true }
			,{field: "ComprobanteNeteado", 			title: "UI_NETEADO", 				width: "70px",	groupable: true, align: "center"}
			,{field: "SolicitudAutorizacion", 		title: "UI_SOLICITUD_AUTORIZACION", width: "200px",	groupable: true }
			,{field: "NombreEstadoAutorizacion", 	title: "UI_ESTADO_AUTORIZACION", 	width: "80px",	groupable: true }
			,{field: "NombreUsuarioLiquidacion", 	title: "UI_LIQUIDADO_POR", 			width: "150px",	groupable: true }			
			,{field: "FechaIngresoRegistro",		title: "UI_FECHA_INGRESO", 			width: "90px",	sortable : false, attributes:{style:"white-space:nowrap;"}}
			,{field: "FechaHoraAnulacion", 			title: "UI_FECHA_ANULACION", 		width: "90px",	sortable : false, attributes:{style:"white-space:nowrap;"}}

			//Datos de la forma de pago (Corredora/Socio)(Solo será visible cuando haya 1 medio de pago x comprobante)
			,{field: "NombreMedioDePago", 			title: "UI_MEDIO_DE_PAGO", 			width: "150px",	groupable: true}
		    ,{field: "IdComprobantePartidaEgreso", 	title: "UI_SECUENCIA_PARTIDA", 			width: "90px",	groupable: true }
			,{field: "NombreEstadoNomina", 			title: "UI_ESTADO_NOMINA", 			width: "90px",	groupable: true }
			,{title   : "UI_CORREDORA", align: "center",
			   columns : [
			              	 {field : "NombreBancoCorredora",  	           title:'UI_BANCO',             width : "160px",	groupable: true, attributes:{style:"white-space:nowrap;"} }
			              	,{field : "AliasBanco",                        title:'UI_ALIAS_CUENTA_BANCO',       width : "110px",	groupable: true, attributes:{style:"white-space:nowrap;"} }
				  			,{field : "NombreTipoCuentaBancariaCorredora", title:'UI_TIPO_CUENTA_BANCO', width : "110px",	groupable: true, attributes:{style:"white-space:nowrap;"} }
							,{field : "NumeroCuentaBancariaCorredora",     title:'UI_NUMERO_CUENTA',     width : "90px",	groupable: true	}
							,{field : "CuentaDCVOrigen",                   title:'UI_CUENTA_DCV_ORIGEN', width : "90px",	groupable: true	}
			]}
		    ,{title   : "UI_SOCIO_NEGOCIO", align: "center",
			  columns : [
			             	 {field : "NombreBancoSocio",     		   title:'UI_BANCO',             width : "160px",	groupable: true, attributes:{style:"white-space:nowrap;"} }
							,{field : "NombreTipoCuentaBancariaSocio", title:'UI_TIPO_CUENTA_BANCO', width : "110px",	groupable: true, attributes:{style:"white-space:nowrap;"} }
							,{field : "NumeroCuentaBancariaSocio",     title:'UI_NUMERO_CUENTA',     width : "90px",	groupable: true, attributes:{style:"white-space:nowrap;"} }
							,{field : "CuentaDCVDestino",              title:'UI_CUENTA_DCV_DESTINO', width : "90px",	groupable: true, attributes:{style:"white-space:nowrap;"} }
			]}
		    
		    ,{field: "NombreEstado", 				title: "UI_ESTADO", 				width: "100px",	groupable: true }
		    ,{field: "IdTipoCanalIngreso", 		title: "UI_TIPO_CANAL_INGRESO", 				width: "100px",	groupable: true }		    
			,{field: "Alias", 						title: "UI_ALIAS_RAZON_SOCIAL", 	width: "100px",	groupable: true }
			,{field: "CondicionDeLiquidacion", 		title: "UI_COND_DE_LIQUIDACION", 	width: "80px",	groupable: true}
			,{field: "Glosa",						title: "UI_COMENTARIO",				width: "100px",	groupable: true }

		    ,{field: "IdClasificacionTipoInstrumento", 	title: "UI_MERCADO", 			width: "150px",	groupable: true }
			,{field: "IdTipoInstrumento", 				title: "UI_TIPO_INSTRUMENTO", 	width: "150px",	groupable: true }
			,{field: "EstadoCaja", title: "UI_ESTADO_CAJA", width: "100px", sortable : false, hidden: true}
		];
	};
	
	//COMPROBANTE DETALLE
	obj.dsDefComprobanteDetalle = {
		type : "odata",
		definition : {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.
			sort 				: { field: "IdComprobanteDetalle", dir: "desc" },
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1022",dataType : "json"}},
			schema : {
				model : {
					fields : {								
						NroLinea						: { type: "number"},
						NombreTipoMovimiento 			: { type: "string"},
						NombreConcepto 					: { type: "string"},
						IdMoneda 						: { type: "string"},
						Monto 							: { type: "number"},
						NombreTipoProducto				: { type: "string"},
						NombreTipoOperacion				: { type: "string"},
						Nemotecnico						: { type: "string"},
						IdTipoComprobanteOrigen			: { type: "string"},	
						IdComprobanteOrigen				: { type: "number"},					
						FechaComprobanteOrigen			: { type: "sysdate"}
					}
				}
			}			
		}
	};
	obj.columnasComprobanteDetalle = function() {
		return  [
			 {field: "NroLinea", 						title: "UI_NUMERO_LINEA", 					width: "40px",	sortable : false }
			,{field: "NombreTipoMovimiento", 			title: "UI_TIPO_COMPROBANTE", 				width: "100px" }
			,{field: "NombreConcepto", 					title: "UI_CONCEPTO", 						width: "280px",	sortable : false }
			,{field: "IdMoneda", 						title: "UI_MONEDA", 						width: "60px" }
			,{field: "Monto", 							title: "UI_MONTO", 							width: "80px",	template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>" }			
			,{field: "NombreTipoProducto", 				title: "UI_TIPO_PRODUCTO", 					width: "150px" }
			,{field: "NombreTipoOperacion", 			title: "UI_TIPO_OPERACION", 				width: "200px" }
			,{field: "Nemotecnico", 					title: "UI_INSTRUMENTO", 					width: "200px" }
			,{field: "IdTipoComprobanteOrigen", 		title: "UI_TIPO_COMPROBANTE_ORIGEN", 		width: "70px"}
			,{field: "IdComprobanteOrigen", 			title: "UI_SECUENCIA_COMPROBANTE_ORIGEN",	width: "80px"}
			,{field: "FechaComprobanteOrigen", 			title: "UI_FECHA_COMPROBANTE_ORIGEN", 		width: "75px"}

		];
	};
	
	 
	//COMPROBANTE FORMA DE PAGO
	obj.dsComprobanteFormaDePago = new bcs.DataSource({
		type : "odata",
			definition : {
				serverFiltering 	: true, // Filtrado de datos en el servidor.
				serverPaging 		: true, // Paginado de los datos en el servidor.
				serverSorting 		: false, // Ordenamiento de los datos en el servidor.
				sort 				: { field: "IdComprobanteFormaDePago", dir: "desc" },
				transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1021",dataType : "json"}},
				schema : {
					model : {
						fields : {								
							 NombreMedioDePago           		: { type: "string"},   
							 NombreModalidadDePago       		: { type: "string"},
							 ClaveDCV				       		: { type: "string"},
							 IdTipoCuentaBancariaCorredora		: { type: "string"},	 
							 NombreTipoCuentaBancariaCorredora	: { type: "string"},	 
							 NombreCuentaBancariaCorredora		: { type: "string"},
							 NumeroCuentaBancariaCorredora		: { type: "string"},
							 IdTipoCuentaBancariaSocio			: { type: "string"},
							 NombreTipoCuentaBancariaSocio		: { type: "string"},					 
							 NombreCuentaBancariaSocio			: { type: "string"},
							 NumeroCuentaBancariaSocio			: { type: "string"},
							 IdMoneda							: { type: "string"},					 
							 NombreBanco						: { type: "string"},					 
							 Monto              		    	: { type: "number"},
							 FechaDocumentoPagoSocio			: { type: "sysdate"},
							 NumeroDocumentoSocio				: { type: "number"},
							 NumeroSerieSocio					: { type: "string"},
							 NumeroDocumentoCorredora			: { type: "number"},
							 NumeroSerieCorredora				: { type: "string"},
							 NombreEstado						: { type: "string"},
							 NombreBancoSocio					: { type: "string"},
							 NombreBancoCorredora				: { type: "string"},
							 FechaDocumentoPagoCorredora		: { type: "sysdate"},
							 IdComprobantePartida				: { type: "number"},
							 IdBancoSocio						: { type: "number"},
							 IdChequera							: { type: "number"},
							 
							 
							 //Columnas ocultas (Se usan para editar forma de pago en forma desconectada)
							 IdBanco							: { type: "number"},					 
							 IdMedioDePago               		: { type: "string"},
							 IdModalidadDePago             		: { type: "string"},
							 IdCuentaBancariaCorredora			: { type: "number"},
							 IdCuentaBancariaSocio				: { type: "number"},
							 IdTarjetaBancariaCorredora			: { type: "number"},		     
							 IdTarjetaBancariaSocio				: { type: "number"},		     
							 IdMoneda						    : { type: "string"},
						}
					}
				},
				aggregate			: ([ //Para resumen de totales de cobro
										{ field: "Monto", aggregate: "sum" },	
										])
			}
	});

	obj.dsDefComprobanteFormaDePago = {
		type : "odata",
			definition : {
				serverFiltering 	: true, // Filtrado de datos en el servidor.
				serverPaging 		: true, // Paginado de los datos en el servidor.
				serverSorting 		: false, // Ordenamiento de los datos en el servidor.
				sort 				: { field: "IdComprobanteFormaDePago", dir: "desc" },
				transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1021",dataType : "json"}},
				schema : {
					model : {
						fields : {								
							 NombreMedioDePago           		: { type: "string"},   
							 NombreModalidadDePago       		: { type: "string"},
							 ClaveDCV				       		: { type: "string"},
							 IdTipoCuentaBancariaCorredora		: { type: "string"},	 
							 NombreTipoCuentaBancariaCorredora	: { type: "string"},	 
							 NombreCuentaBancariaCorredora		: { type: "string"},
							 NumeroCuentaBancariaCorredora		: { type: "string"},
							 IdTipoCuentaBancariaSocio			: { type: "string"},
							 NombreTipoCuentaBancariaSocio		: { type: "string"},					 
							 NombreCuentaBancariaSocio			: { type: "string"},
							 NumeroCuentaBancariaSocio			: { type: "string"},
							 IdMoneda							: { type: "string"},					 
							 NombreBanco						: { type: "string"},					 
							 Monto              		    	: { type: "number"},
							 FechaDocumentoPagoSocio			: { type: "sysdate"},
							 NumeroDocumentoSocio				: { type: "number"},
							 NumeroSerieSocio					: { type: "string"},
							 NumeroDocumentoCorredora			: { type: "number"},
							 NumeroSerieCorredora				: { type: "string"},
							 NombreEstado						: { type: "string"},
							 NombreBancoSocio					: { type: "string"},
							 NombreBancoCorredora				: { type: "string"},
							 FechaDocumentoPagoCorredora		: { type: "sysdate"},
							 NombreUsuarioGiro					: { type: "string"},
							 NombreUsuarioLiquidacion			: { type: "string"},
							 NombreUsuarioCertificacion			: { type: "string"},
							 IdComprobantePartida				: { type: "number"},
							 IdBancoSocio						: { type: "number"},
							 IdChequera							: { type: "number"},							 

							 //Columnas ocultas (Se usan para editar forma de pago en forma desconectada)
							 IdBanco							: { type: "number"},					 
							 IdMedioDePago               		: { type: "string"},
							 IdModalidadDePago             		: { type: "string"},
							 IdCuentaBancariaCorredora			: { type: "number"},
							 IdCuentaBancariaSocio				: { type: "number"},
							 IdTarjetaBancariaCorredora			: { type: "number"},		     
							 IdTarjetaBancariaSocio				: { type: "number"},		     
							 IdMoneda						    : { type: "string"},
						}
					}
				},
				aggregate			: ([ //Para resumen de totales de cobro
										{ field: "Monto", aggregate: "sum" },	
										])
			}
	};
	 
	
	obj.getDsComprobanteFormaDePago = function(datachange) {
		return new bcs.DataSource({
			type : "odata",
			definition : {
				dataChange			: datachange,
				serverFiltering 	: true, // Filtrado de datos en el servidor.
				serverPaging 		: true, // Paginado de los datos en el servidor.
				serverSorting 		: false, // Ordenamiento de los datos en el servidor.
				sort 				: { field: "IdComprobanteFormaDePago", dir: "desc" },
				transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1021",dataType : "json"}},
				schema : {
					model : {
						fields : {								
							 NombreMedioDePago           		: { type: "string"},   
							 NombreModalidadDePago       		: { type: "string"},
							 ClaveDCV				       		: { type: "string"},
							 IdTipoCuentaBancariaCorredora		: { type: "string"},	 
							 NombreTipoCuentaBancariaCorredora	: { type: "string"},	 
							 NombreCuentaBancariaCorredora		: { type: "string"},
							 NumeroCuentaBancariaCorredora		: { type: "string"},
							 IdTipoCuentaBancariaSocio			: { type: "string"},
							 NombreTipoCuentaBancariaSocio		: { type: "string"},					 
							 NombreCuentaBancariaSocio			: { type: "string"},
							 NumeroCuentaBancariaSocio			: { type: "string"},
							 IdMoneda							: { type: "string"},					 
							 NombreBanco						: { type: "string"},					 
							 Monto              		    	: { type: "number"},
							 FechaDocumentoPagoSocio			: { type: "sysdate"},
							 NumeroDocumentoSocio				: { type: "number"},
							 NumeroSerieSocio					: { type: "string"},
							 NumeroDocumentoCorredora			: { type: "number"},
							 NumeroSerieCorredora				: { type: "string"},
							 NombreEstado						: { type: "string"},
							 NombreBancoSocio					: { type: "string"},
							 NombreBancoCorredora				: { type: "string"},
							 FechaDocumentoPagoCorredora		: { type: "sysdate"},
							 IdComprobantePartida				: { type: "number"},			
							 IdBancoSocio						: { type: "number"},
							 IdChequera							: { type: "number"},

							 NombreUsuarioCertificacion			: {type: "string"},
							 NombreUsuarioGiro					: {type: "string"},
							 NombreUsuarioLiquidacion			: {type: "string"},

							 //Columnas ocultas (Se usan para editar forma de pago en forma desconectada)
							 IdBanco							: { type: "number"},					 
							 IdMedioDePago               		: { type: "string"},
							 IdModalidadDePago             		: { type: "string"},
							 IdCuentaBancariaCorredora			: { type: "number"},
							 IdCuentaBancariaSocio				: { type: "number"},
							 IdTarjetaBancariaCorredora			: { type: "number"},		     
							 IdTarjetaBancariaSocio				: { type: "number"},		     
							 IdMoneda						    : { type: "string"},
							 IdComprobante						: { type: "number"},
							 IdComprobanteFormaDePago			: { type: "number"},
							 CertificadoAutomatico				: { type: "string"},
						}
					}
				},
				
			}	
		});
	}

	
	obj.columnasComprobanteFormaDePago = function() {
		return  [
			  {field : "NombreMedioDePago",     title:'UI_MEDIO_DE_PAGO', width : "120px",	sortable : false }
			 ,{field : "NombreModalidadDePago", title:'UI_MODALIDAD',     width : "80px", 	sortable : false}
			 ,{field : "ClaveDCV", 				title:'UI_CLAVE_DCV',     width : "80px", 	sortable : false}
			 ,{field : "IdMoneda",              title:'UI_MONEDA',        width : "60px",	sortable : false}
			 ,{field : "Monto",                 title:'UI_MONTO',         width : "50px",	sortable : false, aggregates: ["sum"], template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>" }			 
			 
			  //Datos de la Corredora
		     ,{title   : "UI_CORREDORA", align: "center",
			   columns : [
			              	 {field : "NombreBancoCorredora",  	           title:'UI_BANCO',             width : "100px",	sortable : false }
				  			,{field : "NombreTipoCuentaBancariaCorredora", title:'UI_TIPO_CUENTA_BANCO', width : "100px",	sortable : false }							
							,{field : "NumeroCuentaBancariaCorredora",     title:'UI_NUMERO_CUENTA',     width : "60px",	sortable : false }							
							,{field : "NumeroSerieCorredora",     		   title:'UI_SERIE_CHEQUERA',   width : "100px",	sortable : false }
							,{field : "FechaDocumentoPagoCorredora",   	   title:'UI_FECHA_DOCUMENTO',   width : "100px",	sortable : false }
							,{field : "NumeroDocumentoCorredora",     	   title:'UI_FOLIO_DOCUMENTO',   width : "100px",	sortable : false, 	template: kendo.template("#if ( NumeroDocumentoCorredora == 0  || NumeroDocumentoCorredora == null)  {##} else {##=NumeroDocumentoCorredora##}#") }														

			 ]}
		     //Datos del Socio de Negocio
		     ,{title   : "UI_SOCIO_NEGOCIO", align: "center",
			  columns : [
			             
			             	 {field : "NombreBancoSocio",     		   title:'UI_BANCO',             width : "100px",	sortable : false }
							,{field : "NombreTipoCuentaBancariaSocio", title:'UI_TIPO_CUENTA_BANCO', width : "100px",	sortable : false }
							,{field : "NumeroCuentaBancariaSocio",     title:'UI_NUMERO_CUENTA',     width : "60px",	sortable : false }
							,{field : "NumeroSerieSocio",     		   title:'UI_SERIE_CHEQUERA',   width : "100px",	sortable : false }
							,{field : "FechaDocumentoPagoSocio",       title:'UI_FECHA_DOCUMENTO',   width : "100px",	sortable : false }
							,{field : "NumeroDocumentoSocio",     	   title:'UI_FOLIO_DOCUMENTO',   width : "100px",	sortable : false, 	template: kendo.template("#if ( NumeroDocumentoSocio == 0  || NumeroDocumentoSocio == null)  {##} else {##=NumeroDocumentoSocio##}#") }
							
			 ]}		      
		     ,{field : "IdComprobantePartida",         	title:'UI_SECUENCIA_PARTIDA',	width : "80px",	sortable : false, template: kendo.template("#if ( IdComprobantePartida == 0  || IdComprobantePartida == null)  {##} else {##=IdComprobantePartida##}#")}
		     ,{field : "NombreEstado",                 	title:'UI_ESTADO',     			width : "80px",	sortable : false}
		     ,{field : "NombreUsuarioCertificacion",	title: "UI_CERTIFICADO_POR", 	width: "150px",	sortable : false }
		     ,{field : "NombreUsuarioGiro",				title: "UI_GIRADO_POR", 		width : "150px",sortable : false }
			 ,{field : "NombreUsuarioLiquidacion",		title: "UI_LIQUIDADO_POR", 		width : "150px",sortable : false }
		     
		     //Columnas ocultas (Se usan para editar forma de pago en forma desconectada)
		     ,{field : "IdMedioDePago",                title:'UI_MEDIO_DE_PAGO',     hidden: true  }
		     ,{field : "IdModalidadDePago",            title:'UI_MODALIDAD',         hidden: true  }		     
		     ,{field : "IdCuentaBancariaCorredora",    title:'UI_TIPO_CUENTA_BANCO', hidden: true  }		     
			 ,{field : "IdCuentaBancariaSocio",        title:'UI_TIPO_CUENTA_BANCO', hidden: true  }			 
			 ,{field : "IdTarjetaBancariaCorredora",   title:'UI_TARJETA_BANCARIA',  hidden: true  }
			 ,{field : "IdTarjetaBancariaSocio",       title:'UI_TARJETA_BANCARIA',  hidden: true  }			 
			 ,{field : "IdMoneda",                     title:'UI_MONEDA',            hidden: true  }			 
			 ,{field : "IdBancoSocio",                 title:'UI_BANCO',             hidden: true  }			 
			 ,{field : "IdChequera",                   title:'UI_CHEQUERA',          hidden: true  }			 
		];
	};
	
	
	//COMPROBANTES A GIRAR
	obj.columnasComprobantesAGirar = function() {
		return  [
    		 {field: "IdComprobante", 		title: "UI_SECUENCIA_COMPROBANTE", 	width: "70px" }
			,{field: "Nemotecnico", 		title: "UI_NEMOTECNICO", 			width: "100px"}
    		,{field: "CuentaDCVOrigen", 	title: "UI_CUENTA_DCV_ORIGEN", 		width: "100px"}
    		,{field: "CuentaDCVDestino", 	title: "UI_CUENTA_DCV_DESTINO", 	width: "100px"}
    		,{field: "IdMoneda", 			title: "UI_MONEDA", 				width: "50px",	groupable: true }
			,{field: "Monto", 				title: "UI_MONTO", 					width: "80px",	groupable: true, template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
			,{field: "FechaLiquidacion", 	title: "UI_FECHA_LIQUIDACION",		width: "70px"}
			,{field: "NombrePersona", 		title: "UI_SOCIO_NEGOCIO", 			width: "200px",	groupable: true }
			,{field: "DocumentoIdentidad",	title: "UI_DOCUMENTO_IDENTIDAD", 	width: "70px",	groupable: true }			
			,{field: "NombreConcepto", 		title: "UI_CONCEPTO", 				width: "150px"}
			,{field: "NombreEstado", 		title: "UI_ESTADO", 				width: "60px" }
		];
	};
	
	
	//------------------------------------------------
	// DOCUMENTOS
	//------------------------------------------------
	obj.dsDefDocumentos = {
		type : "odata",
		definition : {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.
			pageSize            : 100,
			sort 				: { field: "IdComprobanteFormaDePago", dir: "desc" },
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1021",dataType : "json"}},
			schema : {
				model : {
					fields : {								
						IdComprobanteFormaDePago			: { type: "number"},
						NombreTipoSocioDeNegocio			: { type: "string"},
						NombrePersona 						: { type: "string"},
						NombrePersonaTitularPago			: { type: "string"},
						NombrePortafolioTitularPago			: { type: "string"},
						NombreMedioDePago           		: { type: "string"},
						NombreModalidadDePago         		: { type: "string"},
						IdMoneda							: { type: "string"},
						Monto              		    		: { type: "number"},
						NombreBancoCorredora				: { type: "string"},
						NombreTipoCuentaBancariaCorredora	: { type: "string"},
						NombreCuentaBancariaCorredora		: { type: "string"},
						NumeroCuentaBancariaCorredora		: { type: "string"},
						NumeroSerieCorredora				: { type: "string"},
						NumeroDocumentoCorredora			: { type: "string"},
						FechaDocumentoPagoCorredora			: { type: "sysdate"},
						NombreBancoSocio					: { type: "string"},
						IdCuentaBancariaSocio				: { type: "number"},
						NombreTipoCuentaBancariaSocio		: { type: "string"},
						NombreCuentaBancariaSocio			: { type: "string"},
						NumeroCuentaBancariaSocio			: { type: "string"},
						IdComprobante 						: { type: "number"},
						NombreTipoMovimiento				: { type: "string"},
						NombreEstado 			   	 		: { type: "string"},
						NumeroDocumentoSocio				: { type: "string"},
						FechaDocumentoPagoSocio				: { type: "sysdate"},
						FechaLiquidacion					: { type: "sysdate"},
						FechaLiquidacionComprobante			: { type: "sysdate"},
						NombreCaja							: { type: "string"},
						IdNominaTransferencia 				: { type: "number"},
						IdDeposito 							: { type: "number"},
						DepositadoTraduccion 				: { type: "string"},
						DiasRetencion						: { type: "number"},
						IdComprobantePartida				: { type: "number"},
						IdBancoSocio						: { type: "number"},
						IdChequera							: { type: "number"},
						NombreUsuarioGiro					: { type: "string"},
						NombreUsuarioLiquidacion			: { type: "string"},
						NombreUsuarioCertificacion			: { type: "string"},
						FechaHoraCertificacion				: { type: "datetime"},
						IdTipoMovimiento                	: { type: "string" },
						IdMedioDePago                   	: { type: "string" },
						TipoMedioDePago                 	: { type: "string" },
						IdModalidadDePago               	: { type: "string" },
						IdCaja                          	: { type: "number" },
						CodigoCaja                      	: { type: "string" },
						IdCuentaBancariaCorredora       	: { type: "number" },
						IdTipoCuentaBancariaCorredora   	: { type: "string" },
						IdBancoCorredora                	: { type: "number" },
						IdTarjetaBancariaCorredora      	: { type: "number" },
						IdTipoTarjetaBancariaCorredora  	: { type: "string" },
						NombreTipoTarjetaBancariaCorredora 	: { type: "string" },
						NumeroTarjetaBancariaCorredora 		: { type: "string" },
						NumeroSerieSocio               		: { type: "string" },
						TipoComprobante                		: { type: "string" },
						IdEstadoComprobante            		: { type: "string" },
						IdTipoCuentaBancariaSocio      		: { type: "string" },
						NombreMoneda                   		: { type: "string" },
						IdTarjetaBancariaSocio         		: { type: "number" },
						IdTipoTarjetaBancariaSocio     		: { type: "string" },
						NombreTipoTarjetaBancariaSocio 		: { type: "string" },
						NumeroTarjetaBancariaSocio     		: { type: "string" },
						IdEstado                       		: { type: "string" },
						IdUsuarioGiro                  		: { type: "number" },
						IdTipoPersona                  		: { type: "string" },
						IdPersona                      		: { type: "number" },
						IdTipoDocumentoIdentidad       		: { type: "string" },
						DocumentoIdentidad             		: { type: "string" },
						IdTipoPersonaTitularPago       		: { type: "string" },
						IdPersonaTitularPago           		: { type: "number" },
						IdPortafolioTitularPago        		: { type: "number" },
						CodigoPortafolioTitularPago    		: { type: "string" },
						IdConcepto                     		: { type: "string" },
						NombreConcepto                 		: { type: "string" },
						Depositado                     		: { type: "string" },
						IdEmpresa                      		: { type: "number" },
						IdUsuarioCertificacion         		: { type: "number" },
						IdUsuarioLiquidacion           		: { type: "number" },
						AfectoSaldo                    		: { type: "string" },
						DocumentoEnviaADeposito        		: { type: "string" },
						MontoEgreso                    		: { type: "number" },
						MontoIngreso                   		: { type: "number" },
						MontoConSigno                  		: { type: "number" },
						SaldoDisponible                		: { type: "number" },
						SaldoContable                  		: { type: "number" },
						ClaveDCV                       		: { type: "string" },
						FechaIngresoRegistro           		: { type: "datetime", bcsType: "datetime" },
						FechaGiro                      		: { type: "datetime", bcsType: "datetime" },
						FechaMovimiento                		: { type: "sysdate",  bcsType: "sysdate"  },
						FechaHoraMovimiento            		: { type: "datetime", bcsType: "datetime" },
						FechaAfectaSaldo               		: { type: "datetime", bcsType: "datetime" },
						FechaHoraAfectaSaldo           		: { type: "datetime", bcsType: "datetime" },
						FechaHoraLiquidacion           		: { type: "datetime", bcsType: "datetime" },					 
					}
				}
			},
			//aggregate	: ([{ field: "Monto", aggregate: "sum" }])
		}
	};


	 
	 
	obj.columnasDocumentos = function() {
		return  [
			 {field : "IdComprobanteFormaDePago",		title: "UI_ID_COMPROBANTE_FORMA_DE_PAGO",	width: "80",	hidden: true}
			,{field : "NombreTipoSocioDeNegocio",		title: "UI_TIPO_SOCIO_NEGOCIO", 			width: "80px",	groupable: true }
			,{field : "NombrePersona", 					title: "UI_SOCIO_NEGOCIO", 					width: "200px",	groupable: true }
			,{field : "NombrePersonaTitularPago", 		title: "UI_PAGUESE_A_LA_ORDEN", 			width: "200px",	groupable: true }			
			,{field : "NombrePortafolioTitularPago", 	title: "UI_PORTAFOLIO", 					width: "100px",	groupable: true }
			
			,{field : "NombreTipoMovimiento", 			title: "UI_TIPO", 							width: "60px",	groupable: true }								
			,{field : "NombreMedioDePago", 				title: 'UI_MEDIO_DE_PAGO', 					width: "150px",	groupable: true }
			,{field : "NombreCaja", 					title: 'UI_CAJA', 							width: "60px",	groupable: true }			
			,{field : "IdMoneda", 						title: "UI_MONEDA", 						width: "60px",	groupable: true }
			,{field : "Monto", 							title: 'UI_MONTO', 							width: "100px", template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
			,{field : "FechaLiquidacion", 				title: 'UI_FECHA_LIQUIDACION', 				width: "100px" }
			,{title   : "UI_CORREDORA", align: "center",
			  columns : [ {field : "NombreBancoCorredora", 				title:'UI_BANCO', 					width: "200px",	sortable : false, groupable: true }
			  			 ,{field : "NombreCuentaBancariaCorredora", 	title:'UI_CUENTA_BANCARIA', 		width: "150px", sortable : false, groupable: true }
						 ,{field : "NombreTipoCuentaBancariaCorredora", title:'UI_TIPO_CUENTA_BANCO', 		width: "100px",	sortable : false }						 
						 ,{field : "NumeroCuentaBancariaCorredora", 	title:'UI_NUMERO_CUENTA', 			width: "100px",	sortable : false }
						 ,{field : "NumeroSerieCorredora", 				title:"UI_SERIE_CHEQUERA", 			width: "90px"}
						 ,{field : "NumeroDocumentoCorredora", 			title:"UI_NUMERO_CHEQUE", 			width: "120px",	template: kendo.template("#if ( NumeroDocumentoCorredora == 0  || NumeroDocumentoCorredora == null)  {##} else {##=NumeroDocumentoCorredora##}#")}
						 ,{field : "FechaDocumentoPagoCorredora", 		title:"UI_FECHA_EMISION", 			width: "120px"}
			 ]}
			 ,{title   : "UI_SOCIO_NEGOCIO", align: "right",
			   columns : [{field : "NombreBancoSocio", 					title:'UI_BANCO', 				width : "200px",	sortable : false, groupable: true }
			   			 ,{field : "NombreCuentaBancariaSocio", 		title:'UI_CUENTA_BANCARIA', 	width : "150px",	sortable : false, groupable: true }
			  			 ,{field : "NombreTipoCuentaBancariaSocio", 	title:'UI_TIPO_CUENTA_BANCO', 	width : "100px",	sortable : false }
						 ,{field : "NumeroCuentaBancariaSocio", 		title:'UI_NUMERO_CUENTA', 		width : "100px",	sortable : false }
						 ,{field : "NumeroSerieSocio", 				    title:"UI_SERIE_CHEQUERA", 		width: "90px"}						 
						 ,{field : "NumeroDocumentoSocio", 				title:"UI_NUMERO_CHEQUE", 		width: "120px",	template: kendo.template("#if ( NumeroDocumentoSocio == 0  || NumeroDocumentoSocio == null)  {##} else {##=NumeroDocumentoSocio##}#")}
						 ,{field : "FechaDocumentoPagoSocio", 			title:"UI_FECHA_EMISION", 		width: "120px"}
			 ]}
			 
			,{field : "IdComprobante", 				title: "UI_SECUENCIA_COMPROBANTE",			width: "60px",	groupable: true }
			,{field : "IdNominaTransferencia", 		title: "UI_SECUENCIA_NOMINA_TRANSFERENCIA",	width: "60px",	groupable: true }
			,{field : "IdDeposito", 				title: "UI_SECUENCIA_DEPOSITO",				width: "60px",	groupable: true }			
			,{field : "NombreEstado", 				title: "UI_ESTADO", 						width: "150px",	sortable : false }	
			,{field : "NombreUsuarioCertificacion",	title: "UI_CERTIFICADO_POR", 				width: "150px",	sortable : false }			
			,{field : "FechaHoraCertificacion",		title: "UI_FECHA_CERTIFICACION", 			width: "90px",	sortable : false,  attributes:{style:"white-space:nowrap;"} }
			,{field : "NombreUsuarioGiro",			title: "UI_GIRADO_POR", 					width: "150px",	sortable : false }
			,{field : "NombreUsuarioLiquidacion",	title: "UI_LIQUIDADO_POR", 					width: "150px",	sortable : false }
		];
	};

	//DOCUMENTOS ARQUEO
	obj.columnasDocumentosArqueo = function() {
		return  [
			 {field : "NombrePersona", 					title: "UI_SOCIO_NEGOCIO", 		width: "200px",	groupable: true }
			,{field : "NombrePersonaTitularPago", 		title: "UI_PAGUESE_A_LA_ORDEN", width: "200px",	groupable: true }			
			,{field : "NombrePortafolioTitularPago", 	title: "UI_PORTAFOLIO", 		width: "100px",	groupable: true }
			
			,{field : "NombreTipoMovimiento", 			title: "UI_TIPO", 				width: "60px",	groupable: true }								
			,{field : "Monto", 							title: 'UI_MONTO', 				width: "100px", template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
			,{field : "FechaLiquidacion", 				title: 'UI_FECHA_LIQUIDACION', 	width: "100px" }
			,{title   : "UI_CORREDORA", align: "center",
			  columns : [ {field : "NombreBancoCorredora", 				title:'UI_BANCO', 					width: "200px",	sortable : false, groupable: true }
			  			 ,{field : "NombreCuentaBancariaCorredora", 	title:'UI_CUENTA_BANCARIA', 		width: "150px", sortable : false, groupable: true }
						 ,{field : "NombreTipoCuentaBancariaCorredora", title:'UI_TIPO_CUENTA_BANCO', 		width: "100px",	sortable : false }						 
						 ,{field : "NumeroCuentaBancariaCorredora", 	title:'UI_NUMERO_CUENTA', 			width: "100px",	sortable : false }
						 ,{field : "NumeroSerieCorredora", 				title:"UI_SERIE_CHEQUERA",			width: "90px"}
						 ,{field : "NumeroDocumentoCorredora", 			title:"UI_NUMERO_CHEQUE", 			width: "120px",	template: kendo.template("#if ( NumeroDocumentoCorredora == 0  || NumeroDocumentoCorredora == null)  {##} else {##=NumeroDocumentoCorredora##}#")}
						 ,{field : "FechaDocumentoPagoCorredora", 		title:"UI_FECHA_EMISION", 			width: "120px"}
			 ]}
			 ,{title   : "UI_SOCIO_NEGOCIO", align: "right",
			   columns : [{field : "NombreBancoSocio", 					title:'UI_BANCO', 				width: "200px",	sortable : false, groupable: true }
			   			 ,{field : "NombreCuentaBancariaSocio", 		title:'UI_CUENTA_BANCARIA', 	width: "150px",	sortable : false, groupable: true }
			  			 ,{field : "NombreTipoCuentaBancariaSocio", 	title:'UI_TIPO_CUENTA_BANCO', 	width: "100px",	sortable : false }
						 ,{field : "NumeroCuentaBancariaSocio", 		title:'UI_NUMERO_CUENTA', 		width: "100px",	sortable : false }
						 ,{field : "NumeroSerieSocio", 				    title:"UI_SERIE_CHEQUERA",		width: "90px"}
						 ,{field : "NumeroDocumentoSocio", 				title:"UI_NUMERO_CHEQUE", 		width: "120px",	template: kendo.template("#if ( NumeroDocumentoSocio == 0  || NumeroDocumentoSocio == null)  {##} else {##=NumeroDocumentoSocio##}#")}
						 ,{field : "FechaDocumentoPagoSocio", 			title:"UI_FECHA_EMISION", 		width: "120px"}
			 ]}
			,{field : "IdComprobante", 			title: "UI_SECUENCIA_COMPROBANTE",			width: "60px",	groupable: true, template: '<label> #:  util.formateaNumero(IdComprobante,0) #</label>' }
			,{field : "IdNominaTransferencia", 	title: "UI_SECUENCIA_NOMINA_TRANSFERENCIA",	width: "60px",	groupable: true, template: '<label> #:  util.formateaNumero(IdNominaTransferencia,0) #</label>' }
			,{field : "IdDeposito", 			title: "UI_SECUENCIA_DEPOSITO",				width: "60px",	groupable: true, template: '<label> #:  util.formateaNumero(IdDeposito,0) #</label>' }
			,{field : "NombreEstado", 			title: "UI_ESTADO", 						width: "150px",	sortable : false }	
			,{field : "DepositadoTraduccion", 	title: "UI_DEPOSITADO", 					width: "50px",	sortable : false }	
		];
	};
	
	
	//DOCUMENTOS
	obj.dsDefDocumentosDeposito = {
		type : "odata",
		definition : {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.
			sort 				: { field: "IdComprobanteFormaDePago", dir: "desc" },
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1754",dataType : "json"}},
			schema : {
				model : {
					fields : {
						NombreMedioDePago           		: { type: "string"},
						FechaLiquidacion					: { type: "sysdate"},
						DiasRetencion						: { type: "number"},
						IdMoneda							: { type: "string"},
						Monto              		    		: { type: "number"},
						NombreBancoSocio					: { type: "string"},
						NombreTipoCuentaBancariaSocio		: { type: "string"},
						NombreCuentaBancariaSocio			: { type: "string"},
						NombrePersona 						: { type: "string"},
						NombrePersonaTitularPago			: { type: "string"},
						NombreTipoMovimiento				: { type: "string"},
						IdComprobante 						: { type: "number"},
						NombreEstado 			   	 		: { type: "string"},
						NumeroCuentaBancariaSocio			: { type: "string"},
					}
				}
			},
		}
	};


	//DOCUMENTOS INGRESO DEPOSITO
	obj.columnasIngresoDeposito = function() {
		return  [
			 {field : "NombreMedioDePago", 				title: 'UI_MEDIO_DE_PAGO', 			width : "150px"}
			,{field : "FechaLiquidacion", 				title: 'UI_FECHA_LIQUIDACION', 		width : "85px"}
			,{field : "DiasRetencion", 					title: 'UI_DIAS_RETENCION', 		width : "50px"}	
			,{field : "IdMoneda", 						title: "UI_MONEDA", 				width : "50px"}
			,{field : "Monto", 							title: 'UI_MONTO', 					width : "100px", 	template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>" } //, footerTemplate:"#=kendo.toString(sum, 'n2')# ", footerAttributes:{style:"text-align: right; padding-right:0;"}			
			,{field : "NombreBancoSocio", 				title: 'UI_BANCO', 					width : "200px",	groupable: true }			
			,{field : "NombreTipoCuentaBancariaSocio",	title: 'UI_TIPO_CUENTA_BANCO', 		width : "100px"}
			,{field : "NumeroCuentaBancariaSocio", 		title: 'UI_NUMERO_CUENTA', 			width : "100px"}
			,{field : "NombrePersona", 					title: "UI_SOCIO_NEGOCIO", 			width : "200px",	groupable: true }
			,{field : "NombrePersonaTitularPago", 		title: "UI_PERSONA_PAGO_COBRO", 	width : "200px",	groupable: true }
			,{field : "NombreTipoMovimiento", 			title: "UI_TIPO", 					width : "70px",		groupable: true }
			,{field : "IdComprobante", 					title: "UI_SECUENCIA_COMPROBANTE", 	width : "60px",		template: '<label> #:  util.formateaNumero(IdComprobante,0) #</label>' }			
		];
	};
	

	
	//-------------------------------
	// ENVIO DE DOCUMENTOS
	//-------------------------------
	obj.dsDefEnvioDocumentos = {
		type : "odata",
		definition : {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.
			sort 				: { field: "IdEnvioDocumento", dir: "desc" }, 									
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1776",dataType : "json"}},  //BSCL.Tesoreria.BD.Vistas::DOC.ConsultarEnvioDocumento
			schema : {
				model : {
					fields : {
						IdEnvioDocumento				: { type: "number"},
						IdCaja							: { type: "number"},
						IdMedioDePago					: { type: "string"},						
						NombreMedioDePago				: { type: "string"},						
						IdMoneda						: { type: "string"},
						CodigoCaja						: { type: "string"},	
						FechaHoraEnvio 					: { type: "sysdate"},
						FechaEnvio 						: { type: "sysdate"},
						IdEmpresa						: { type: "number"},
						IdUsuario						: { type: "number"},
						CodigoUsuario					: { type: "string"},
						IdEstado 						: { type: "string"},
						NombreEstado					: { type: "string"},
						CantidadDocumentos				: { type: "number"},  //Cantidad total de documentos por envio
						MontoEnvio				        : { type: "number"},  //Monto total de documentos por envio
					}
				}
			}
		}
	}; 
	
	obj.columnasEnvioDocumentos = function() {
		return  [
		      {field: "IdEnvioDocumento", 		   	title: "UI_SECUENCIA_ENVIO", 	 width: "70px",	 sortable : false}
		     ,{field: "FechaEnvio", 				title: "UI_FECHA_ENVIO", 		 width: "70px",	 sortable : false}
		     ,{field: "CodigoCaja", 				title: "UI_CAJA", 				 width: "80px",	 groupable: true }		     
		     ,{field: "NombreMedioDePago", 			title: "UI_MEDIO_DE_PAGO", 		 width: "80px",	 groupable: true }
		     ,{field: "IdMoneda", 					title: "UI_MONEDA", 			 width: "80px",	 groupable: true }
		     ,{field: "CantidadDocumentos", 		title: "UI_CANTIDAD_DOCUMENTOS", width: "70px",	 groupable: true}		     
		     ,{field: "MontoEnvio", 		        title: "UI_MONTO_TOTAL",         width: "70px",	 groupable: true, 	template : " <label> #:  (util.bcs_formato(MontoEnvio,'GEN','Monto',IdMoneda,'','')).valor #</label>" }		     
		     ,{field: "CodigoUsuario", 				title: "UI_USUARIO_ENVIO", 		 width: "70px",	 sortable : false}
		     ,{field: "NombreEstado", 				title: "UI_ESTADO", 			 width: "100px", groupable: true}
		];
	};
	
	//-------------------------------
	// ENVIO DE DOCUMENTOS DETALLE
	//-------------------------------
	obj.dsDefEnvioDocumentosDetalle = {
			type : "odata",
			definition : {
				serverFiltering 	: true, // Filtrado de datos en el servidor.
				serverPaging 		: true, // Paginado de los datos en el servidor.
				serverSorting 		: false, // Ordenamiento de los datos en el servidor.
				sort 				: { field: "IdEnvioDocumento", dir: "desc" }, 									
				transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1777",dataType : "json"}},  //BSCL.Tesoreria.BD.Vistas::DOC.ConsultarEnvioDocumentoDetalle
				schema : {
					model : {
						fields : {
							IdCaja							: { type: "number"},
							CodigoCaja						: { type: "string"},
							FechaHoraEnvio					: { type: "sysdate"},
							FechaEnvio						: { type: "sysdate"},
							IdEmpresa						: { type: "number"},
							IdUsuarioEnvioDocumento			: { type: "number"},
							IdEstadoEnvioDocumento			: { type: "string"},
							IdEnvioDocumentoDetalle			: { type: "number"},
							IdEnvioDocumento				: { type: "number"},
							IdComprobante					: { type: "number"},
							IdComprobanteFormaDePago		: { type: "number"},
							FechaHoraRevision				: { type: "sysdate"},
							FechaRevision					: { type: "sysdate"},
							IdUsuarioRevision				: { type: "number"},
							CodigoUsuarioRevision			: { type: "string"},
							IdEstadoEnvioDocumentoDetalle	: { type: "string"},
							NombreEstado					: { type: "string"},
							IdMonedaComprobanteFormaDePago	: { type: "string"},
							NombreMedioDePago				: { type: "string"},													
							IdBancoSocio					: { type: "number"},
							NombreBancoSocio				: { type: "string"},
							NumeroCuentaBancariaSocio		: { type: "string"},
							NumeroSerieSocio				: { type: "string"},
							NumeroDocumentoSocio			: { type: "number"},							
							Monto							: { type: "number"},
						}
					}
				}
			}
		};
		
		obj.columnasEnvioDocumentosDetalle = function() {
			return  [
					 {field: "IdComprobante", 		            title: "UI_SECUENCIA_COMPROBANTE",	 width: "75px",	sortable : true}
					,{field: "NombreMedioDePago", 				title: "UI_MEDIO_DE_PAGO", 			 width: "80px",	sortable : true }					
					,{field: "NombreBancoSocio", 	 	        title: "UI_BANCO", 		             width: "120px",sortable : true }					
					,{field: "NumeroDocumentoSocio", 	 	    title: "UI_FOLIO_DOCUMENTO", 		 width: "75px",	sortable : true }
					,{field: "NumeroSerieSocio", 	            title: "UI_SERIE_DOCUMENTO", 		 width: "75px",	sortable : true }					
					,{field: "IdMonedaComprobanteFormaDePago", 	title: "UI_MONEDA", 				 width: "70px",	sortable : true }
					,{field: "NumeroCuentaBancariaSocio", 	    title: "UI_NUMERO_CUENTA", 			 width: "80px",	sortable : true }
					,{field: "Monto", 							title: "UI_MONTO", 				 	 width: "80px",	sortable : true, template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMonedaComprobanteFormaDePago,'','')).valor #</label>" }					
					,{field: "FechaRevision", 					title: "UI_FECHA_REVISION", 		 width: "78px",	sortable : true }
					,{field: "CodigoUsuarioRevision", 			title: "UI_USUARIO_REVISION", 		 width: "78px",	sortable : true }
					,{field: "NombreEstado", 					title: "UI_ESTADO_REVISION", 		 width: "78px",	sortable : true }			
										
			];
		};		

	
	return obj;
}]);	

