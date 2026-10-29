angular.module('ModuloMesa').factory('bcsServiciosGrillasMesaRF',function($resource,$translate,$rootScope,bcsConsultasMesa,bcsConfiguracionSociodeNegocio,bcsServiciosMesa) {
	var obj = {};
	var modelFiltros={};
	var configuracionColumnas = function() {
		return { 
			 FolioCartera					:{field: "FolioCartera"						, title: $translate.instant('UI_FOLIO')					, width: "60px" ,align: "left"}
		    ,IdUsuarioReserva			    :{field: "IdUsuarioReserva"					, title: $translate.instant('UI_USUARIO')				, width: "60px" ,align: "left", hidden: true}
			,IdTipoInstrumento				:{field: "IdTipoInstrumento"				, title: $translate.instant('UI_FAMILIA')				, width: "50px",align: "left"}
			,Nemotecnico					:{field: "Nemotecnico"						, title: $translate.instant('UI_NEMOTECNICO')			, width: "150px",align: "left"}
			,NemoSVS						:{field: "NemoSVS"							, title: $translate.instant('UI_NEMO_SVS')				, width: "150px",align: "left"}
			,IdClasificacionTipoInstrumento	:{field: "IdClasificacionTipoInstrumento"	, title: $translate.instant('UI_MERCADO')				, width: "40px" ,align: "left"}
			,ClasificacionTipoInstrumento	:{field: "ClasificacionTipoInstrumento"		, title: $translate.instant('UI_MERCADO')				, width: "150px",align: "left"}			
			,IdTipoOperacion				:{field: "IdTipoOperacion"					, title: $translate.instant('UI_TIPO_OPERACION')		, width: "40px" ,align: "left"}
			,IdEmisor						:{field: "IdEmisor"							, title: $translate.instant('UI_EMISOR')				, width: "100px",align: "left"}
			,EsFungible						:{field: "EsFungible"						, title: $translate.instant('UI_FUNGIBLE')				, width: "40px" ,align: "left"}
			,IdTipoReajuste					:{field: "IdTipoReajuste"					, title: $translate.instant('UI_TIPO_REAJUSTE')			, width: "40px" ,align: "left"}
			,INGPrecioMercadoActual			:{field: "INGPrecioMercadoActual"			, title: $translate.instant('UI_PRECIO')				, width: "100px",
				template_ : " <label> #:  (util.bcs_formato(INGPrecioMercadoActual,'GEN','Precio',IdTipoReajuste,IdTipoInstrumento,'')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Precio',dataItem.IdTipoReajuste,dataItem.IdTipoInstrumento,'');
				}
			}
			,INGPrecioCompraActual			:{field: "INGPrecioCompraActual"			, title: $translate.instant('UI_PRECIO_COMPRA')				, width: "100px",
				template_ : " <label> #:  (util.bcs_formato(INGPrecioPrecioActual,'GEN','Precio',IdTipoReajuste,IdTipoInstrumento,'')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Precio',dataItem.IdTipoReajuste,dataItem.IdTipoInstrumento,'');
				}
			}			
			,INGTasaCompraActual			:{field: "INGTasaCompraActual"				, title: $translate.instant('UI_TASA')					, width: "50px" ,
				template_ : " <label> #:  (util.bcs_formato(INGTasaCompraActual,'GEN','Tasa',IdTipoReajuste,IdTipoInstrumento,'')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Tasa',dataItem.IdTipoReajuste,dataItem.IdTipoInstrumento,'');
				}
			}
			,INGTasaCompra					:{field: "INGTasaCompra"					, title: $translate.instant('UI_TASA_COMPRA')			, width: "50px" ,
				template_ : " <label> #:  (util.bcs_formato(INGTasaCompra,'GEN','Tasa',IdTipoReajuste,IdTipoInstrumento,'')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Tasa',dataItem.IdTipoReajuste,dataItem.IdTipoInstrumento,'');
				}
			}
			,Tasa							:{field: "Tasa"								, title: $translate.instant('UI_TASA')					, width: "70px" ,
				template_ : " <label> #:  (util.bcs_formato(Tasa,'GEN','Tasa',IdTipoReajuste,IdTipoInstrumento,'')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Tasa',dataItem.IdTipoReajuste,dataItem.IdTipoInstrumento,'');
				}
			}
			,Precio							:{field: "Precio"						, title: $translate.instant('UI_PRECIO')				, width: "100px",
				template_ : " <label> #:  (util.bcs_formato(Precio,'GEN','Precio',IdTipoReajuste,IdTipoInstrumento,'')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Precio',dataItem.IdTipoReajuste,dataItem.IdTipoInstrumento,'');
				}
			}
			,INGCantidad					:{field: "INGCantidad"						, title: $translate.instant('UI_CANTIDAD')				, width: "100px" ,
				template_ : " <label> #:  (util.bcs_formato(INGCantidad,'GEN','Cantidad',IdTipoReajuste,IdTipoInstrumento,'')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Cantidad',dataItem.IdTipoReajuste,dataItem.IdTipoInstrumento,'');
				}
			}
			,INGCantidad_UI					:{field: "INGCantidad_UI", title: $translate.instant('UI_CANTIDAD')				, width: "100px" ,
				template_ : " <label> #:  (util.bcs_formato(INGCantidad_UI,'GEN','Cantidad',IdTipoReajuste,IdTipoInstrumento,'')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Cantidad',dataItem.IdTipoReajuste,dataItem.IdTipoInstrumento,'');
				}
			}
			
			,INGMontoATasaCompraActual		:{field: "INGMontoATasaCompraActual"		, title: $translate.instant('UI_MONTO_COMPRA')	, width: "100px",
				template_ : " <label> #:  (util.bcs_formato(INGMontoATasaCompraActual,'GEN','Monto',IdTipoReajuste,IdTipoInstrumento,'')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Monto',$rootScope.SesionIdMonedaSistema||dataItem.IdTipoReajuste,dataItem.IdTipoInstrumento,'');
				}
			}

			
			,INGMontoCompraActualMonedaInstrumento		:{field: "INGMontoCompraActualMonedaInstrumento", title: $translate.instant('UI_VALOR_COMPRA'), width: "100px",
				template_ : " <label> #:  (util.bcs_formato(INGMontoCompraActualMonedaInstrumento,'MES','Cantidad',IdTipoReajuste,IdTipoInstrumento,'')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'MES','Cantidad',dataItem.IdTipoReajuste||$rootScope.SesionIdMonedaSistema,dataItem.IdTipoInstrumento,'');
				}
			}
			

			,INGValorRescate		:{field: "INGValorRescate", title: $translate.instant('UI_VALOR_RESCATE'), width: "100px",
				template_ : " <label> #:  (util.bcs_formato(INGValorRescate,'MES','Precio',IdMonedaInstrumento,IdTipoInstrumento,'')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'MES','Cantidad',dataItem.IdTipoReajuste||$rootScope.SesionIdMonedaSistema,dataItem.IdTipoInstrumento,'');
				}
			}
			
			,PrecioSimulacion				:{field: "PrecioSimulacion"					, title: $translate.instant('UI_PRECIO_SIMULACION')					, width: "100px",
				template_ : " <label> #:  (util.bcs_formato(PrecioSimulacion,'GEN','Precio',IdTipoReajuste,IdTipoInstrumento,'')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Precio',dataItem.IdTipoReajuste,dataItem.IdTipoInstrumento,'');
				}
			}
			,TasaSimulacion					:{field: "TasaSimulacion"					, title: $translate.instant('UI_TASA_SIMULACION')					, width: "50px" ,
				template_ : " <label> #:  (util.bcs_formato(TasaSimulacion,'GEN','Tasa',IdTipoReajuste,IdTipoInstrumento,'')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Tasa',dataItem.IdTipoReajuste,dataItem.IdTipoInstrumento,'');
				}
			}
			,CantidadSimulacion				:{field: "CantidadSimulacion"				, title: $translate.instant('UI_CANTIDAD_SIMULACION')				, width: "50px" ,
				template_ : " <label> #:  (util.bcs_formato(CantidadSimulacion,'GEN','Cantidad',IdTipoReajuste,IdTipoInstrumento,'')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Cantidad',dataItem.IdTipoReajuste,dataItem.IdTipoInstrumento,'');
				}
			}
			,MontoSimulacion				:{field: "MontoSimulacion"					, title: $translate.instant('UI_MONTO_SIMULACION')					, width: "100px",
				template_ : " <label> #:  (util.bcs_formato(MontoSimulacion,'GEN','Monto',IdTipoReajuste,IdTipoInstrumento,'')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Monto',$rootScope.SesionIdMonedaSistema,dataItem.IdTipoInstrumento,'');
				}
			}
			,ValorRescateSimulacion			:{field: "ValorRescateSimulacion"			, title: $translate.instant('UI_VALOR_RESCATE_SIMULACION')			, width: "100px",
				template_ : " <label> #:  (util.bcs_formato(ValorRescateSimulacion,'GEN','Monto',IdTipoReajuste,IdTipoInstrumento,'')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Monto',dataItem.IdTipoReajuste,dataItem.IdTipoInstrumento,'');
				}
			}
			
			,CorteMinimo					:{field: "CorteMinimo"						, title: $translate.instant('UI_CORTE_MINIMO')			, width: "80px",
				template_ : " <label> #:  (util.bcs_formato(CorteMinimo,'GEN','Cantidad',IdTipoReajuste,IdTipoInstrumento,'')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Cantidad',dataItem.IdTipoReajuste,dataItem.IdTipoInstrumento,'');
				}
			}
			,RazonSocial					:{field: "RazonSocial"						, title: $translate.instant('UI_RAZON_SOCIAL_O_NOMBRE')	, width: "150px",align: "left"}
			,PlazoEnDiasOperacion			:{field: "PlazoEnDiasOperacion"				, title: $translate.instant('UI_PLAZO')					, width: "35px",align: "left"}
			,MontoCompra					:{field: "MontoCompra"						, title: $translate.instant('UI_MONTO_COMPRA')					, width: "100px",
				template_ : " <label> #:  (util.bcs_formato(MontoCompra,'GEN','Monto',IdTipoReajuste,IdTipoInstrumento,'')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Monto',dataItem.IdTipoReajuste,dataItem.IdTipoInstrumento,'');
				}	
			}
			,IdAgente						:{field: "IdAgente"							, title: $translate.instant('UI_AGENTE')				, width: "100px",align: "left"}  	
			,Estado							:{field: "Estado"							, title: $translate.instant('UI_ESTADO')				, width: "50px",align: "left"}
			,FechaVencimientoInstrumento	:{field: "FechaVencimientoInstrumento"		, title: $translate.instant('UI_FECHA_VENCIMIENTO_INSTRUMENTO')				, width: "70px",align: "left"}
	  		,FechaVencimientoSimulacion	    :{field: "FechaVencimientoSimulacion"		, title: $translate.instant('UI_FECHA_VENCIMIENTO_SIMULACION')	, width: "70px",align: "left"}    
			
	  		,IdOperacion					:{field: "IdOperacion"						, title: $translate.instant('UI_SECUENCIA')			, width: "40px" ,align: "right"}
	  		,FolioOperacion					:{field: "FolioOperacion"					, title: $translate.instant('UI_FOLIO')			, width: "60px" ,align: "right"}
	  		,NombreCliente					:{field: "RazonSocial"						, title: $translate.instant('UI_CLIENTE')		, width: "150px",align: "left"}	  		
			,PlazoEnDias					:{field: "PlazoEnDias"						, title: $translate.instant('UI_PLAZO')			, width: "35px",align: "left"}
			
			,IdMoneda						:{field: "IdMoneda"							, title: $translate.instant('UI_MONEDA')			, width: "40px" ,align: "left"}
			,Moneda							:{field: "Moneda"							, title: $translate.instant('UI_MONEDA_EMISION')			, width: "40px" ,align: "left"}
			,MonedaInstrumento				:{field: "MonedaInstrumento" 				, title: $translate.instant('UI_MONEDA_INSTRUMENTO'), width: "40px",align: "left"}
			
			,MontoOperacion					:{field: "MontoOperacion"					, title: $translate.instant('UI_MONTO_EQUIVALENTE')					, width: "100px",
				template_ : " <label> #:  (util.bcs_formato(MontoOperacion,'GEN','Monto',IdMoneda,'','')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Monto',dataItem.IdMoneda,'','');
				}
			}
			,MontoOperacionInicial 			:{field: "MontoOperacionInicial"			, title: $translate.instant('UI_MONTO_EQUIVALENTE')					, width: "100px",
				template_ : " <label> #:  (util.bcs_formato(MontoOperacionInicial,'GEN','Monto',IdMoneda,'','')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Monto',dataItem.IdMoneda,'','');
				}
			}
			,MontoOperacionFinal 			:{field: "MontoOperacionFinal"				, title: $translate.instant('UI_MONTO_OPERACION_FINAL')					, width: "100px",
				template_ : " <label> #:  (util.bcs_formato(MontoOperacionFinal,'GEN','Monto',IdMoneda,'','')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Monto',dataItem.IdMoneda,'','');
				}
			}
			,MontoOperacionFinalAnticipo 			:{field: "MontoOperacionFinalAnticipo"				, title: $translate.instant('UI_MONTO_ANTICIPO')					, width: "100px",
				template_ : " <label> #:  (util.bcs_formato(MontoOperacionFinalAnticipo,'GEN','Monto',IdMoneda,'','')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Monto',dataItem.IdMoneda,'','');
				}
			}
			,MontoOperacionMoneda			:{field: "MontoOperacionMoneda"				, title: $translate.instant('UI_MONTO_OPERACION')					, width: "100px",
				template_ : " <label> #:  (util.bcs_formato(MontoOperacionMoneda,'GEN','Monto',IdTipoReajuste,'','')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Monto',dataItem.IdTipoReajuste,'','');
				}
			}
			,MontoOperacionInicialMoneda 	:{field: "MontoOperacionInicialMoneda"		, title: $translate.instant('UI_MONTO_OPERACION')					, width: "100px",
				template_ : " <label> #:  (util.bcs_formato(MontoOperacionInicialMoneda,'GEN','Monto',IdTipoReajuste,'','')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Monto',dataItem.IdTipoReajuste,'','');
				}
			}
			,MontoOperacionFinalMoneda		:{field: "MontoOperacionFinalMoneda"		, title: $translate.instant('UI_MONTO_OPERACION')					, width: "100px",
				template_ : " <label> #:  (util.bcs_formato(MontoOperacionFinalMoneda,'GEN','Monto',IdTipoReajuste,'','')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Monto',dataItem.IdTipoReajuste,'','');
				}
			}
			
			
			,IdEstado						:{field: "IdEstado"							, title: $translate.instant('UI_ESTADO')		, width: "50px",align: "left"}
			,Glosa							:{field: "Glosa"							, title: $translate.instant('UI_COMENTARIO')			, width: "150px",align: "left"}
			,IdOperacionOrigenReasignacion 	:{field: "IdOperacionOrigenReasignacion" 	, title: $translate.instant('UI_ID_ASOCIADO')	, width: "110px",align: "left"}		        
			,DocumentoIdentidad			  	:{field: "DocumentoIdentidad"				, title: $translate.instant('UI_DOCUMENTO_IDENTIDAD'), width: "90px",align: "left"}
			,CodigoPortafolio				:{field: "CodigoPortafolio" 				, title: $translate.instant('UI_CODIGO_PORTAFOLIO'), width: "60px",align: "left"}
			,FechaOperacion					:{field: "FechaOperacion"					, title: $translate.instant('UI_FECHA_OPERACION')			, width: "80px",align: "left"}
			,FechaLiquidacion				:{field: "FechaLiquidacion"			        , title: $translate.instant('UI_FECHA_LIQUIDACION')		, width: "80px",align: "left"}
			,FechaLiquidacionString         :{field: "FechaLiquidacionString"			, title: $translate.instant('UI_FECHA_LIQUIDACION'), width: "80px",align: "left"}
			,FechaCondicionLiquidacion		:{field: "FechaCondicionLiquidacion"		, title: $translate.instant('UI_FECHA_LIQUIDACION')		, width: "80px",align: "left"}
			,FechaVencimientoOperacion		:{field: "FechaVencimientoOperacion"		, title: $translate.instant('UI_FECHA_VENCIMIENTO_OPERACION'), width: "80px",align: "left"}
			,FechaVencimiento				:{field: "FechaVencimiento"					, title: $translate.instant('UI_FECHA_VENCIMIENTO'), width: "80px",align: "left"}
			,IdBoveda						:{field: "IdBoveda"							, title: $translate.instant('UI_BOVEDA'), width: "40px",align: "left"}
			,NombreBoveda					:{field: "NombreBoveda"						, title: $translate.instant('UI_BOVEDA'), width: "100px",align: "left"}			
			,INGTasaMercadoActual			:{field: "INGTasaMercadoActual"				, title: $translate.instant('UI_TASA_MERCADO')	, width: "50px",
				template_ : " <label> #:  (util.bcs_formato(INGTasaMercadoActual,'GEN','Tasa',IdTipoReajuste,IdTipoInstrumento,'')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Tasa',dataItem.IdTipoReajuste,dataItem.IdTipoInstrumento,'');
				}
			}
			
			,INGMontoATasaMercadoActual		:{field: "INGMontoATasaMercadoActual"		, title: $translate.instant('UI_MONTO_MERCADO')	, width: "100px",
				template_ : " <label> #:  (util.bcs_formato(INGMontoATasaMercadoActual,'GEN','Monto',IdTipoReajuste,IdTipoInstrumento,'')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Monto',$rootScope.SesionIdMonedaSistema||dataItem.IdTipoReajuste,dataItem.IdTipoInstrumento,'');
				}
			}
			
			,INGMontoMercadoActualMonedaInstrumento		:{field: "INGMontoMercadoActualMonedaInstrumento", title: $translate.instant('UI_VALOR_MERCADO'), width: "100px",
				template_ : " <label> #:  (util.bcs_formato(INGMontoMercadoActualMonedaInstrumento,'MES','Cantidad',IdTipoReajuste,IdTipoInstrumento,'')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'MES','Cantidad',dataItem.IdTipoReajuste||$rootScope.SesionIdMonedaSistema,dataItem.IdTipoInstrumento,'');
				}
			}
			
			,INGDurationCompraProximo		:{field: "INGDurationCompraProximo"			, title: $translate.instant('UI_DURATION'), width: "100px", hidden: true,
				template_ : " <label> #:  (util.bcs_formato(INGDurationCompraProximo,'GEN','Monto',IdTipoReajuste,IdTipoInstrumento,'')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Monto',$rootScope.SesionIdMonedaSistema||dataItem.IdTipoReajuste,dataItem.IdTipoInstrumento,'');
				}
			}
			
			,INGDurationCompraActual		:{field: "INGDurationCompraActual"			, title: $translate.instant('UI_DURATION'), width: "100px", hidden: true,
				template_ : " <label> #:  (util.bcs_formato(INGDurationCompraActual,'GEN','Monto',IdTipoReajuste,IdTipoInstrumento,'')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Monto',$rootScope.SesionIdMonedaSistema||dataItem.IdTipoReajuste,dataItem.IdTipoInstrumento,'');
				}
			}
			,ValorClasificacionRiesgo		:{field: "ValorClasificacionRiesgo"			, title: $translate.instant('UI_CLASIFICACION_RIESGO'), width: "100px",align: "left"}
			
			,INGMontoInteresCompraProximo	:{field: "INGMontoInteresCompraProximo"		, title: $translate.instant('UI_INTERES_DIARIO'), width: "100px",
				template_ : " <label> #:  (util.bcs_formato(INGMontoInteresCompraProximo,'GEN','Monto',IdTipoReajuste,IdTipoInstrumento,'')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Cantidad',dataItem.IdTipoReajuste||$rootScope.SesionIdMonedaSistema,dataItem.IdTipoInstrumento,'');
				}
			}
			
			//template : " <label> #:  (util.bcs_formato(INGMontoInteresCompraActual,'GEN','Tasa',IdMonedaInstrReajuste,'','')).valor #</label>",
			//aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }
			
			,INGMontoReajusteCompraProximo	:{field: "INGMontoReajusteCompraProximo"	, title: $translate.instant('UI_REAJUSTE_DIARIO'), width: "100px",
				template_ : " <label> #:  (util.bcs_formato(INGMontoReajusteCompraProximo,'MES','Monto',IdMonedaSistema,IdTipoInstrumento,'')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Monto',$rootScope.SesionIdMonedaSistema||dataItem.IdTipoReajuste,dataItem.IdTipoInstrumento,'');
				}
			}
			,TasaCompra						:{field: "TasaCompra"						, title: $translate.instant('UI_TASA_COMPRA'), width: "50px",
				template_ : " <label> #:  (util.bcs_formato(Tasa,'GEN','Tasa',IdTipoReajuste,IdTipoInstrumento,'')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Tasa',dataItem.IdTipoReajuste,dataItem.IdTipoInstrumento,'');
				}
			}
			,CodigoIFRS						:{field: "CodigoIFRS"						, title: $translate.instant('UI_CATEGORIA_IFRS'), width: "100px",align: "left"}
			,CodigoDescIFRS 				:{field: "CodigoDescIFRS"						, title: $translate.instant('UI_CATEGORIA_IFRS'), width: "100px",align: "left"}

			,NombreOperacion				:{field: "NombreOperacion"					, title: $translate.instant('UI_TIPO_OPERACION'), width: "110px" ,align: "left"}			
			,NombreEstado					:{field: "NombreEstado"						, title: $translate.instant('UI_ESTADO'), width: "80px" ,align: "left"}
			,Intermesa						:{field: "Intermesa"						, title: $translate.instant('UI_INTERMESA')				, width: "40px" ,align: "left"}
			,CategoriaIFRS					:{field: "CodigoIFRS"						, title: $translate.instant('UI_CATEGORIA_IFRS'), width: "100px",align: "left"}
			,FechaIngreso					:{field: "INGFechaOperacion"				, title: $translate.instant('UI_FECHA_INGRESO')			, width: "80px",align: "left"}
			,CodigoUsuarioReserva			:{field: "CodUsrReserva" 					, title: $translate.instant('UI_USUARIO'), width: "100px",align: "left"}
			,NombreMesaOrigen				:{field: "NombreMesaOrigen"					, title: $translate.instant('UI_MESA_ORIGEN')		, width: "80px",align: "left"}
			,NombreMesaDestino				:{field: "NombreMesaDestino"				, title: $translate.instant('UI_MESA_DESTINO')		, width: "80px",align: "left"}
			,Cantidad						:{field: "Cantidad"							, title: $translate.instant('UI_CANTIDAD')			, width: "100px",
				template_ : " <label> #:  (util.bcs_formato(Cantidad,'GEN','Cantidad',IdTipoReajuste,IdTipoInstrumento,'')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Cantidad',dataItem.IdTipoReajuste,dataItem.IdTipoInstrumento,'');
				}
			}
			,CantidadTotalReservada				:{field: "CantidadTotalReservada"				, title: $translate.instant('UI_CANTIDAD')			, width: "50px",
				template_ : " <label> #:  (util.bcs_formato(CantidadTotalReservada,'GEN','Cantidad',IdTipoReajuste,IdTipoInstrumento,'')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Cantidad',dataItem.IdTipoReajuste,dataItem.IdTipoInstrumento,'');
				}
			}
			
			,FormaDePago					:{field: "FormaDePago"						, title: $translate.instant('UI_FORMA_DE_PAGO')		, width: "70px", align: "left"}
			,NombreMedioDePago				:{field: "NombreMedioDePago"				, title: $translate.instant('UI_MEDIO_PAGO')		, width: "70px", align: "left"}
			,Monto							:{field: "Monto"							, title: $translate.instant('UI_MONTO')				, width: "100px", align: "left"	,
				template_ : " <label> #:(util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Monto',dataItem.IdMoneda||dataItem.MonedaInstrumento,'','');
				}
			}
			,MontoLiquidacion				:{field: "MontoLiquidacion"							, title: $translate.instant('UI_MONTO_LIQUIDACION')				, width: "100px", align: "left"	,
				template_ : " <label> #:(util.bcs_formato(Monto,'GEN','Monto',MonedaInstrumento,'','')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Monto',dataItem.MonedaInstrumento,'','');
				}
			}
			,MontoLiquidacionOperacion				:{field: "MontoLiquidacionOperacion"							, title: $translate.instant('UI_MONTO_LIQUIDACION')				, width: "100px", align: "left"	,
				template_ : " <label> #:(util.bcs_formato(Monto,'GEN','Monto',MonedaLiquidacionOperacion,'','')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Monto',dataItem.MonedaLiquidacionOperacion,'','');
				}
			}
			,FechaPagoMedio					:{field: "FechaPagoMedio"					, title: $translate.instant('UI_FECHA_PAGO')		, width: "80px", align: "left"}
			,CodigoUsuario					:{field: "CodigoUsuario" 					, title: $translate.instant('UI_USUARIO')			, width: "80px",align: "left"}
			,TipoValorizacion				:{field: "TipoValorizacion" 				, title: $translate.instant('UI_TIPO_VALORIZACION')	, width: "60px",align: "left"}			
			,SaldoCaja						:{field: "SaldoCaja"						, title: $translate.instant('UI_SALDO_CAJA')		, width: "100px",
				template_ : " <label> #:  (util.bcs_formato(SaldoCaja,'GEN','Monto',IdTipoReajuste,'','')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Monto',dataItem.IdTipoReajuste,'','');
				}
			}
			,Nominal						:{field: "Nominal"						, title: $translate.instant('UI_Nominal')		, width: "100px",
				template_ : " <label> #:  (util.bcs_formato(Nominal,'GEN','Monto',IdTipoReajuste,'','')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Cantidad',dataItem.IdTipoReajuste||dataItem.MonedaInstrumento,'','');
				}
			}
			
			,CarteraPacto						:{field: "CarteraPacto"						, title: $translate.instant('UI_RETRO_COMPRA')				, width: "40px" ,align: "left"}
			,NombreBanco				:{field: "NombreBanco"					, title: $translate.instant('UI_MEDIO_DE_PAGO'), width: "70px" ,align: "left"}
			,CuentaBancaria				:{field: "CuentaBancaria"					, title: $translate.instant('UI_MEDIO_DE_PAGO'), width: "70px" ,align: "left"}

			,NombreMedioDePago				:{field: "NombreMedioDePago"					, title: $translate.instant('UI_MEDIO_DE_PAGO'), width: "70px" ,align: "left"}
			,NombreFormaDePago				:{field: "NombreFormaDePago"					, title: $translate.instant('UI_FORMA_DE_PAGO'), width: "80px" ,align: "left"}
			,NombreTipoOperacion			:{field: "NombreTipoOperacion"					, title: $translate.instant('UI_TIPO_OPERACION'), width: "80px" ,align: "left"}
			,Dias							:{field: "Dias"									, title: $translate.instant('UI_CANTIDAD_DIAS')			, width: "35px",align: "left"}
			,NombreMoneda					:{field: "NombreMoneda"							, title: $translate.instant('UI_MONEDA'), width: "80px",align: "left"}
			,PorDefecto						:{field: "PorDefecto"							, title: $translate.instant('UI_POR_DEFECTO'), width: "30px",align: "left"}
			,IdMonedaInstrumento			:{field: "IdMonedaInstrumento" 					, title: $translate.instant('UI_MONEDA_LIQUIDACION'), width: "50px",align: "left"}
			,MontoValorizadoReserva			:{field: "MontoValorizadoReserva" 				, title: $translate.instant('UI_MONTO_VALORIZADO_RESERVA'), width: "50px",align: "left"}
			,TipoDocumento					:{field: "TipoDocumento" 						, title: $translate.instant('UI_TIPO_DOCUMENTO_OPERACION'), width: "50px",align: "left"}
			,ConfirmaAdemdum				:{field: "ConfirmaAdemdum"						, title: $translate.instant('UI_CONFIRMA_PACTO'), width: "40px" ,align: "left"}
			,EstadoConfirmacion				:{field: "EstadoConfirmacion"						, title: $translate.instant('UI_CONFIRMA_MEDIOS_PAGO'), width: "40px" ,align: "left"}
			,IdUsuarioConfirma				:{field: "IdUsuarioConfirma" 					, title: $translate.instant('UI_USUARIO_CONFIRMA'), width: "150px",align: "left"}
			,CodigoUsuarioConfirma			:{field: "CodigoUsuarioConfirma" 				, title: $translate.instant('UI_USUARIO_CONFIRMA'), width: "150px",align: "left"}			
			,TipoCarteraCorredor			:{field: "TipoCarteraCorredor"					, title: $translate.instant('UI_TIPO_CARTERA_CORREDOR'), width: "70px" ,align: "left", hidden: true}
			
			,INGMontoAPlazoDetalle			:{field: "INGMontoAPlazoDetalle"					, title: $translate.instant('UI_MONTO_PLAZO_DETALLE')				, width: "100px", align: "left"	,
				template_ : " <label> #:(util.bcs_formato(INGMontoAPlazoDetalle,'GEN','Monto',IdMoneda,'','')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Monto',$rootScope.SesionIdMonedaSistema,'','');
				}
			}
			,INGMontoAPlazoMonedaInstrumentoDetalle				:{field: "INGMontoAPlazoMonedaInstrumentoDetalle"					, title: $translate.instant('UI_MONTO_PLAZO_INSTRUMENTO_DETALLE')				, width: "100px", align: "left"	,
				template_ : " <label> #:(util.bcs_formato(INGMontoAPlazoMonedaInstrumentoDetalle,'GEN','Monto',IdMoneda,'','')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Monto',dataItem.MonedaLiquidacionOperacion,'','');
				}
			}
			,FormaDePago					:{field: "TipoCarteraCorredor"					, title: $translate.instant('UI_TIPO_CARTERA_CORREDOR'), width: "70px" ,align: "left", hidden: true}
			,TipoCarteraCorredor			:{field: "TipoCarteraCorredor"					, title: $translate.instant('UI_TIPO_CARTERA_CORREDOR'), width: "70px" ,align: "left", hidden: true}
			,TipoCarteraCorredor			:{field: "TipoCarteraCorredor"					, title: $translate.instant('UI_TIPO_CARTERA_CORREDOR'), width: "70px" ,align: "left", hidden: true}

			,TipoCarteraCorredor			:{field: "TipoCarteraCorredor"					, title: $translate.instant('UI_TIPO_CARTERA_CORREDOR'), width: "70px" ,align: "left", hidden: true}
			,ValorizacionManual				:{field: "ValorizacionManual"					, title: $translate.instant('UI_VALORIZACION_MANUAL'), width: "70px" ,align: "left", }
			,Spread 						:{field: "Spread"								, title: $translate.instant('UI_SPREAD'), width: "70px" ,align: "left",
			template_ : " <label> #:(util.bcs_formato(Spread,'GEN','Monto',IdMoneda,'','')).valor #</label>",
				format : function(value, dataItem) {
					return util.bcs_formato(value,'GEN','Monto',dataItem.IdMoneda||dataItem.MonedaInstrumento,'','');
				}
			}

		}
	};
	var getConfiguracionColumna = function(campo,extras) {
		if (!configuracionColumnas()[campo]) console.log("FALTA COLUMNA " + campo);
		return _.extend( {}, configuracionColumnas()[campo], extras);
	};
	obj.getSchemaCarteraMesa = function(){
		return {
			model: {
				fields : {
					"Cantidad"										: { type: "number"}
					,"ValorClasificacionRiesgo"						: { type: "string"}
					,"ClasificacionTipoInstrumento"					: { type: "string"}
					,"CobraPrimaAlInicio"							: { type: "string"}
					,"CodigoIFRS"									: { type: "string"}
					,"CodigoDescIFRS"								: { type: "string"}
					//,"CodigoIFRSDetalle"									: { type: "string"}
					//,"CodigoDescIFRSDetalle"								: { type: "string"}
					,"CodigoPortafolio"								: { type: "string"}
					,"CorteMinimo"									: { type: "number"}
					,"DocumentoIdentidad"							: { type: "string"}
					,"EGRCantidad"									: { type: "number"}
					,"EGRFechaLiquidacion"							: { type: "sysdate"}
					,"EGRFechaOperacion"							: { type: "sysdate"}
					,"EGRFolioExternoReferencia"					: { type: "string"}
					//,"EGRGlosa"										: { type: "string"}
					//,"EGRIdAgente"									: { type: "number"}
					//,"EGRIdComprobanteFactReferencia"				: { type: "number"}
					,"MontoValorizadoReserva"						: { type: "number"}					
					,"EGRIdConcepto"								: { type: "string"}
					,"EGRIdCondicionLiquidacion"					: { type: "string"}
					,"EGRIdModulo"									: { type: "string"}
					,"EGRIdMovimientoCarteraTransito"				: { type: "number"}
					,"EGRIdOperacion"								: { type: "number"}
					,"EGRIdSucursal"								: { type: "number"}
					,"EGRIdTipoCanalIngreso"						: { type: "string"}
					,"EGRIdTipoOperacion"							: { type: "string"}
					,"EGRIdUsuario"									: { type: "number"}
					,"EGRMonto"										: { type: "number"}
					,"EGRMontoAPlazo"								: { type: "number"}
					,"EGRMontoEnMonedaInstrumento"					: { type: "number"}
					,"Precio"										: { type: "number"}
					,"EGRPrecio"									: { type: "number"}
					,"EGRPrecioAPlazo"								: { type: "number"}
					,"EGRSpreadCompra"								: { type: "number"}
					,"EGRSpreadMercado"								: { type: "number"}
					,"EGRTasa"										: { type: "number"}
					,"EGRValorRescate"								: { type: "number"}
					,"EsDiaHabilParaCalculoInteres"					: { type: "string"}
					,"EsDiaHabilPlazoPrestamo"						: { type: "string"}
					,"EsFungible"									: { type: "string"}
					,"EsIntraday"									: { type: "string"}
					,"EsNominal"									: { type: "string"}
					,"Estado"										: { type: "string"}
					,"FechaEmisionInstrumento"						: { type: "sysdate"}
					,"FechaIngresoRegistro"							: { type: "sysdate"}
					,"FechaLiquidacion"								: { type: "sysdate"}
					,"FechaLiquidacionStr"							: { type: "string"}
					,"FechaModificacionRegistro"					: { type: "sysdate"}
					,"FechaOperacion"								: { type: "sysdate"}
					,"FechaReserva"									: { type: "sysdate"}
					,"FechaVencimientoInstrumento"					: { type: "sysdate"}
					,"FechaVencimientoOperacion"					: { type: "sysdate"}
					,"FolioCartera"									: { type: "number"}
					,"FolioCarteraRelacionada"						: { type: "number"}
					,"FolioEntidad"									: { type: "string"}
					,"INGCantidad"									: { type: "number"}
					,"INGConvexidadCompraAnterior"					: { type: "number"}
					,"INGConvexidadCompraProximo"					: { type: "number"}
					,"INGConvexidadDescuentoAnterior"				: { type: "number"}
					,"INGConvexidadDescuentoProximo"				: { type: "number"}
					,"INGConvexidadMercadoAnterior"					: { type: "number"}
					,"INGConvexidadMercadoProximo"					: { type: "number"}
					,"INGConvexidadPactoAnterior"					: { type: "number"}
					,"INGConvexidadPactoProximo"					: { type: "number"}
					,"INGDurationCompraAnterior"					: { type: "number"}
					,"INGDurationCompraProximo"						: { type: "number"}
					,"INGDurationCompraActual"						: { type: "number"}
					,"INGDurationDescuentoAnterior"					: { type: "number"}
					,"INGDurationDescuentoProximo"					: { type: "number"}
					,"INGDurationMercadoAnterior"					: { type: "number"}
					,"INGDurationMercadoProximo"					: { type: "number"}
					,"INGDurationPactoAnterior"						: { type: "number"}
					,"INGDurationPactoProximo"						: { type: "number"}
					,"INGFechaCorteCupon"							: { type: "sysdate"}
					,"INGFechaOperacion"							: { type: "sysdate"}
					,"INGFechaPrecioTasaCompraActual"				: { type: "sysdate"}
					,"INGFechaPrecioTasaCompraAnterior"				: { type: "sysdate"}
					,"INGFechaPrecioTasaCompraProximo"				: { type: "sysdate"}
					,"INGFechaPrecioTasaDescuentoActual"			: { type: "sysdate"}
					,"INGFechaPrecioTasaDescuentoAnterior"			: { type: "sysdate"}
					,"INGFechaPrecioTasaDescuentoProximo"			: { type: "sysdate"}
					,"INGFechaPrecioTasaMercadoActual"				: { type: "sysdate"}
					,"INGFechaPrecioTasaMercadoAnterior"			: { type: "sysdate"}
					,"INGFechaPrecioTasaMercadoProximo"				: { type: "sysdate"}
					,"INGFechaPrecioTasaPactoActual"				: { type: "sysdate"}
					,"INGFechaTasaCorredora"						: { type: "sysdate"}
					,"INGGlosa"										: { type: "string"}
					,"INGIdOrigenTasaPrecioMercadoActual"			: { type: "number"}
					,"INGIdOrigenTasaPrecioMercadoAnterior"			: { type: "number"}
					,"INGIdOrigenTasaPrecioMercadoProximo"			: { type: "number"}
					,"INGMontoAPlazo"								: { type: "number"}
					,"INGMontoAPlazoDetalle"						: { type: "number"}
					,"INGMontoAPlazoMonedaInstrumentoDetalle"		: { type: "number"}
					,"INGMontoATasaCompraActual"					: { type: "number"}
					,"INGMontoATasaCompraAnterior"					: { type: "number"}
					,"INGMontoATasaCompraProximo"					: { type: "number"}
					,"INGMontoATasaCorredora"						: { type: "number"}
					,"INGMontoATasaDescuentoActual"					: { type: "number"}
					,"INGMontoATasaDescuentoAnterior"				: { type: "number"}
					,"INGMontoATasaDescuentoProximo"				: { type: "number"}
					,"INGMontoATasaMercadoActual"					: { type: "number"}
					,"INGMontoATasaMercadoAnterior"					: { type: "number"}
					//,"INGValorRescate"					: { type: "number"}
					,"INGMontoATasaPactoActual"						: { type: "number"}
					,"INGMontoATasaPactoAnterior"					: { type: "number"}
					,"INGMontoATasaPactoProximo"					: { type: "number"}
					,"INGMontoCorteCupon"							: { type: "number"}
					,"INGMontoCorteCuponProximo"					: { type: "number"}
					,"INGMontoInteresCompraAnterior"				: { type: "number"}
					,"INGMontoInteresCompraProximo"					: { type: "number"}
					,"INGMontoInteresDescuentoAnterior"				: { type: "number"}
					,"INGMontoInteresDescuentoProximo"				: { type: "number"}
					,"INGMontoInteresMercadoAnterior"				: { type: "number"}
					,"INGMontoInteresMercadoProximo"				: { type: "number"}
					,"INGMontoInteresPactoAnterior"					: { type: "number"}
					,"INGMontoInteresPactoProximo"					: { type: "number"}
					,"INGMontoReajusteCompraAnterior"				: { type: "number"}
					,"INGMontoReajusteCompraProximo"				: { type: "number"}
					,"INGMontoReajusteDescuentoAnterior"			: { type: "number"}
					,"INGMontoReajusteDescuentoProximo"				: { type: "number"}
					,"INGMontoReajusteMercadoAnterior"				: { type: "number"}
					,"INGMontoReajusteMercadoProximo"				: { type: "number"}
					,"INGMontoReajustePactoAnterior"				: { type: "number"}
					,"INGMontoReajustePactoProximo"					: { type: "number"}
					,"INGPrecioAPlazo"								: { type: "number"}
					,"INGPrecioCompraActual"						: { type: "number"}
					,"INGPrecioCompraAnterior"						: { type: "number"}
					,"INGPrecioCompraProximo"						: { type: "number"}
					,"INGPrecioDescuentoActual"						: { type: "number"}
					,"INGPrecioDescuentoAnterior"					: { type: "number"}
					,"INGPrecioDescuentoProximo"					: { type: "number"}
					,"INGPrecioMercadoActual"						: { type: "number"}
					,"INGPrecioMercadoAnterior"						: { type: "number"}
					,"INGPrecioMercadoProximo"						: { type: "number"}
					,"INGPrecioPactoActual"							: { type: "number"}
					,"INGPrecioPactoAnterior"						: { type: "number"}
					,"INGPrecioPactoProximo"						: { type: "number"}
					,"INGTasaAgente"								: { type: "number"}
					,"INGTasaCompra"								: { type: "number"}
					,"INGTasaCompraActual"							: { type: "number"}
					,"INGTasaCompraAnterior"						: { type: "number"}
					,"INGTasaCompraProximo"							: { type: "number"}
					,"INGTasaCorredora"								: { type: "number"}
					,"INGTasaDescuentoActual"						: { type: "number"}
					,"INGTasaDescuentoAnterior"						: { type: "number"}
					,"INGTasaDescuentoProximo"						: { type: "number"}
					,"INGTasaMercadoActual"							: { type: "number"}
					,"INGTasaMercadoAnterior"						: { type: "number"}
					,"INGTasaMercadoProximo"						: { type: "number"}
					,"INGTasaPactoActual"							: { type: "number"}
					,"INGTasaPactoAnterior"							: { type: "number"}
					,"INGTasaPactoProximo"							: { type: "number"}
					,"INGValorRescate"								: { type: "number"}
					,"IdAgente"										: { type: "number"}
					,"IdBolsa"										: { type: "string"}
					,"IdBoveda"										: { type: "string"}
					,"IdCartera"									: { type: "number"}
					,"IdClasificacionTipoInstrumento"				: { type: "string"}
					,"IdCliente"									: { type: "number"}
					,"IdComprobanteFactReferencia"					: { type: "number"}
					,"IdConcepto"									: { type: "string"}
					,"IdCondicionLiquidacion"						: { type: "string"}
					,"IdEmisor"										: { type: "string"}
					,"IdEmpresa"									: { type: "number"}
					,"IdEstado"										: { type: "string"}
					,"IdInstrumento"								: { type: "number"}
					,"IdMesa"										: { type: "number"}
					,"IdModulo"										: { type: "string"}
					,"IdMonedaInstrumento"							: { type: "string"}
					,"IdMonedaSistema"								: { type: "string"}
					,"IdMovimientoCarteraTransito"					: { type: "number"}
					,"IdOperacion"									: { type: "number"}
					,"IdReserva"									: { type: "number"}
					,"IdPortafolio"									: { type: "number"}
					,"IdSucursal"									: { type: "number"}
					,"IdTipoCanalIngreso"							: { type: "string"}
					,"IdTipoInstrumento"							: { type: "string"}
					,"IdTipoOperacion"								: { type: "string"}
					,"IdTipoPrestamo"								: { type: "string"}
					,"IdTipoProducto"								: { type: "string"}
					,"IdTipoReajuste"								: { type: "string"}
					,"IdUsuario"									: { type: "number"}
					,"IdUsuarioReserva"								: { type: "number"}
					,"MaximoDiasPrestamo"							: { type: "number"}
					,"MinimoDiasAnticipo"							: { type: "number"}
					,"Moneda"										: { type: "string"}
					,"MonedaInstrumento"							: { type: "string"}
					,"MonedaLiquidacionOperacion"					: { type: "string"}
					,"Monto"										: { type: "number"}
					,"MontoLiquidacion"								: { type: "number"}
					,"MontoLiquidacionOperacion"								: { type: "number"}
					,"MontoAPlazo"									: { type: "number"}
					,"MontoCompra"									: { type: "number"}
					,"MontoCompraEnMonedaInstrumento"				: { type: "number"}
					,"NemoSVS"										: { type: "string"}
					,"Nemotecnico"									: { type: "string"}
					,"NombreBolsa"									: { type: "string"}
					,"NombreBoveda"									: { type: "string"}
					,"NombreConcepto"								: { type: "string"}
					,"NombreCondicionLiquidacion"					: { type: "string"}
					,"NombreEmisor"									: { type: "string"}
					,"NombreEmpresa"								: { type: "string"}
					,"NombreEstado"									: { type: "string"}
					,"NombreLargoTipoReajuste"						: { type: "string"}
					,"NombreModulo"									: { type: "string"}
					,"NombreTipoOperacion"							: { type: "string"}
					,"NombreTipoReajuste"							: { type: "string"}
					,"NombreUsuario"								: { type: "string"}
					,"Nominal"										: { type: "number"}
					,"PlazoEnDiasOperacion"							: { type: "number"}
					,"PrecioAPlazo"									: { type: "number"}
					,"PrecioCompra"									: { type: "number"}
					,"Reajustable"									: { type: "string"}
					,"RazonSocial"									: { type: "string"}
					,"Tasa"											: { type: "number"}
					,"TasaAgente"									: { type: "number"}
					,"TasaCompra"									: { type: "number"}
					,"TipoInstrumento"								: { type: "string"}
					,"TipoValorizacion"								: { type: "number"}
					,"UsaGarantiaEspecifica"						: { type: "string"}
					,"ValorRescate"									: { type: "number"}
					,"INGMontoCompraActualMonedaInstrumento"		: { type: "number"}
					,"INGCantidad_UI"								: { type: "number"}
					,"INGMontoMercadoActualMonedaInstrumento"		: { type: "number"}
					//columnas de simulacion
					,"MontoDetalle"									: { type: "number"}
					,"CantidadSimulacion"							: { type: "number", validation : { min: 0 }}
					,"PrecioSimulacion"								: { type: "number", validation : { min: 0 }}
					,"TasaSimulacion"								: { type: "number", validation : { min: -9999999 }}
					,"MontoSimulacion"								: { type: "number", validation : { min: 0 }} 
					,"ValorRescateSimulacion"						: { type: "number", validation : { min: 0 }}
					,"FechaVencimientoSimulacion"					: { type: "sysdate"}
					,"CarteraPacto"									: { type: "string"}
					,"TipoCarteraCorredor"                          : { type: "string"}						
				}
			}
		};
	};
	obj.getSchemaCarteraReservadaMesa = function(){
		return {
			model: {
				fields : {
					 "IdMesa"							: { type: "number"}
					,"FolioCartera"						: { type: "number"}
					,"Nemotecnico"						: { type: "string"}
					,"IdEmisor"							: { type: "string"}
					,"IdTipoReajuste"					: { type: "string"}
					,"Cantidad"							: { type: "number"}
					,"IdEmpresa"						: { type: "number"}
					,"IdPortafolio"						: { type: "number"}
					,"CantidadTotalReservada"			: { type: "number"}
					,"CodUsrReserva"					: { type: "string"}
					,"INGFechaOperacion"				: { type: "sysdate"}
					,"FechaLiquidacion"					: { type: "sysdate"}
					,"IdInstrumento"					: { type: "number"}
					,"IdClasificacionTipoInstrumento"	: { type: "string"}
					,"IdTipoInstrumento"				: { type: "string"}
					,"TipoInstrumento"					: { type: "string"}
					,"CorteMinimo"						: { type: "number"}
					,"PrecioCompra"						: { type: "number"}
					,"TasaCompra"						: { type: "number"}
					,"MontoCompra"						: { type: "number"}
					,"INGPrecioMercadoActual"			: { type: "number"}
					,"INGTasaMercadoActual"				: { type: "number"}
					,"INGMontoATasaMercadoActual"		: { type: "number"}
					,"FechaVencimientoInstrumento"		: { type: "sysdate"}
					,"FechaVencimientoOperacion"		: { type: "sysdate"}
					,"EsFungible"						: { type: "string"}
					,"CodigoIFRS"						: { type: "string"}
					,"MontoValorizadoReserva"			: { type: "number"}
					
				}
			}
		};
	};
	obj.getSchemaDetalleOperacionMesaRF = function(){
		return {
			model: {
				fields : {
					 "IdOperacion"								: { type: "number"}
					,"FolioOperacion"							: { type: "number"}
					,"FechaOperacion"							: { type: "sysdate"}
					,"IdTipoProducto"							: { type: "string"}
					,"IdTipoOperacion"							: { type: "string"}
					,"IdMesa"									: { type: "number"}
					,"IdCliente"								: { type: "number"}
					,"IdPortafolio"								: { type: "number"}
					,"CodigoPortafolio"							: { type: "string"}
					,"RazonSocial"								: { type: "string"}
				
					
					,"MontoOperacionInicial"					: { type: "number"}
					,"MontoOperacionFinal"						: { type: "number"}
					,"IdTipoReajuste"							: { type: "string"}
					,"Tasa"										: { type: "number"}
					,"PlazoEnDias"								: { type: "number"}
					,"FechaVencimiento"							: { type: "sysdate"}
					,"FechaLiquidacion"							: { type: "sysdate"}
					,"IdCondicionLiquidacion"					: { type: "string"}
					,"FechaCondicionLiquidacion"				: { type: "sysdate"}
					,"IdAgente"									: { type: "number"}
					,"IdEstado"									: { type: "string"}
					,"FechaCreacion"							: { type: "sysdate"}
					,"IdUsuarioCreacion"						: { type: "number"}
					,"Glosa"									: { type: "string"}
					,"IdOperacionOrigenReasignacion"			: { type: "number"}
					,"IdMesaDetalle"							: { type: "number"}
					,"FolioCarteraDetalle"						: { type: "number"}
					,"NemotecnicoDetalle"						: { type: "string"}
					,"IdEmisorDetalle"							: { type: "string"}
					,"IdTipoReajusteDetalle"					: { type: "string"}
					,"CantidadDetalle"							: { type: "number"}
					,"IdEmpresaDetalle"							: { type: "number"}
					,"IdPortafolioDetalle"						: { type: "number"}
					,"INGFechaOperacionDetalle"					: { type: "sysdate"}
					,"FechaLiquidacionDetalle"					: { type: "sysdate"}
					,"IdInstrumentoDetalle"						: { type: "number"}
					,"IdClasificacionTipoInstrumentoDetalle"	: { type: "string"}
					,"IdTipoInstrumentoDetalle"					: { type: "string"}
					,"TipoInstrumentoDetalle"					: { type: "string"}
					,"CorteMinimoDetalle"						: { type: "number"}
					,"PrecioCompraDetalle"						: { type: "number"}
					,"TasaCompraDetalle"						: { type: "number"}
					,"MontoCompraDetalle"						: { type: "number"}
					,"INGPrecioMercadoActualDetalle"			: { type: "number"}
					,"INGTasaMercadoActualDetalle"				: { type: "number"}
					,"INGMontoATasaMercadoActualDetalle"		: { type: "number"}
					,"FechaVencimientoInstrumentoDetalle"		: { type: "sysdate"}
					,"FechaVencimientoOperacionDetalle"			: { type: "sysdate"}
					,"EsFungibleDetalle"						: { type: "string"}
					,"CodigoIFRSDetalle"						: { type: "string"}
					,"CodigoDescIFRSDetalle"					: { type: "string"}
					,"IdMonedaInstrumentoDetalle"				: { type: "string"}
					,"CantidadReservadaDetalle"					: { type: "number"}
					,"IdUsrReservaDetalle"						: { type: "number"}
					,"CodUsrReservaDetalle"						: { type: "string"}
					,"CantidadTotalReservadaDetalle"			: { type: "number"}
					,"IdOperacionDetalle"						: { type: "number"}
					,"INGMontoCompraActualMonedaInstrumento"	: { type: "number"}
					,"MontoValorizadoReserva"					: { type: "number"}
					,"TasaAnualizada"							: { type: "number"}
					,"CostoFondo"								: { type: "number"}
					,"UtilidadPerdida"							: { type: "number"}
					,"UtilidadFinal"							: { type: "number"}
					,"INGMontoAPlazo"							: { type: "number"}
					,"INGMontoAPlazoMonedaInstrumento"			: { type: "number"}
					// Se agregan nuevos cambios en el schema JCA
					,"CarteraNominal"							: { type: "string"}
					,"ValorRescateDetalle"						: { type: "number"}
					,"ValorClasificacionRiesgoDetalle"			: { type: "number"}
					,"INGMontoAPlazoMonedaInstrumentoDetalle"	: { type: "number"}
					,"INGMontoAPlazoDetalle"					: { type: "number"}
					,"MontoLiquidacionOperacionDetalle"			: { type: "number"}
					,"INGMontoAPlazoMonedaInstrumentoDetalle"	: { type: "number"}
					,"IdBovedaDetalle"							: { type: "string"}
					,"Spread"									: { type: "number"}
					,"ValorPresente"							: { type: "number"}
				}
			}
		};
	};

	obj.getSchemaConsultarClienteCuentaPropia = function(){
		return {
			model: {
				fields : {
					"NombreCompleto" : {type: "string"},

				}
			}
		};
	};

	obj.getSchemaComprobantePagoOperacionMesaRF = function(){
		return {
			model: {
				fields : {
					  "IdComprobantePagoOperacion"	: { type: "number"}
					, "IdEmpresa"					: { type: "number"}
					, "IdOperacion"					: { type: "number"}
					, "IdIngresoEgreso"				: { type: "number"}
					, "IdAbono"						: { type: "number"}
					, "IdCargo"						: { type: "number"}
					, "IdTipoProducto"				: { type: "string"}
					, "IdFormaPago"					: { type: "string"}
					, "NombreFormaPago"				: { type: "string"}
					, "IdPreferenciaPago"			: { type: "string"}
					, "IdMedioPago"					: { type: "string"}
					, "NombreMedioPago"				: { type: "string"}
					, "RecibePaga"					: { type: "string"}
					, "RecibePagaDesc"				: { type: "string"}
					, "IdModalidadPago"				: { type: "string"}
					, "IdMoneda"					: { type: "string"}
					, "Monto"						: { type: "number"}
					, "FechaIngresoComprobantePagoOperacion"		: { type: "sysdate"}
					, "FechaLiquidacionComprobantePagoOperacion"	: { type: "sysdate"}
					, "IdEstado"					: { type: "string"}
					// Se agregan nuevos cambios en el schema JCA
					,"NombreFormaDePago"			: { type: "string"}
					,"NombreBancoCuentaBancaria"	: { type: "string"}
					,"NumeroCuentaBancaria"			: { type: "string"}
					
					
				}
			}
		};
	};
	
	obj.getSchemaMesaRF = function(){
		return {
			model: {
				fields : {
				 	"IdMesa"						: { type: "number"}
					,"IdEmpresa"					: { type: "number"}
					,"IdCliente"					: { type: "number"}
					,"IdPortafolio"					: { type: "number"}
					,"Codigo"						: { type: "string"}
					,"NombreMesa"					: { type: "string"}
					,"IdTipoProducto"				: { type: "string"}
					,"NombreTipoProducto"			: { type: "string"}
					,"IdEstadoMesa"					: { type: "string"}
					,"EstadoMesa"					: { type: "string"}
					,"IdEstadoRegistro"				: { type: "string"}
					,"IdUsuarioModificacion"		: { type: "number"}
					,"CodigoUsuario"				: { type: "string"}
					,"FechaModificacion"			: { type: "sysdate"}
					,"FechaModificacionFrmt"		: { type: "sysdate"}
					,"PorDefecto"					: { type: "string"}
					,"IdTipoProductoMesaModulo"		: { type: "number"}
					,"IdTipoProductoMesa"			: { type: "string"}
					,"IdTipoProductoModulo"			: { type: "string"}
					,"NombreModulo"					: { type: "string"}
					,"IdEstadoModulo"				: { type: "string"}
					,"TipoEstadoModulo"				: { type: "string"}
					,"MsjEstadoModulo"				: { type: "string"}
					
				}
			}
		};
	};
	obj.getSchemaMargenesMesaRF = function(){
		return {
			model: {
				fields : {
				 	 "IdMargenTasa"			: { type: "number"}
					,"TipoEntidad"			: { type: "string"}
					,"IdMoneda"				: { type: "string"}
					,"MontoDesde"			: { type: "number"}
					,"MontoHasta"			: { type: "number"}
					,"PlazoDesde"			: { type: "number"}
				    ,"PlazoHasta"			: { type: "number"}
					,"Tasa"					: { type: "number"}
				    ,"IdEstado"				: { type: "string"}
				    ,"Estado"				: { type: "string"}
					,"IdEmpresa"			: { type: "number"}
				    ,"FechaIngreso"			: { type: "sysdate"}
				    ,"FechaCreacion"		: { type: "sysdate"}
				    ,"FechaModificacion"	: { type: "sysdate"}
					,"IdUsuarioCreador"		: { type: "number"}
				    ,"Borrado"				: { type: "string"}
				}
			}
		};
	};/*
	obj.getSchemaFormaDePago = function(){
		return {
			model: {
				fields : {
				 	 "FormaDePago"			: { type: "string"}
   				   	,"NombreMedioDePago"	: { type: "string"}
   				   	,"IdMoneda"				: { type: "string"}
					,"Monto"				: { type: "number"}
					,"FechaPagoMedio"		: { type: "sysdate"}
				}
			}
		};
	};*/
	obj.getSchemaFormasPago = function() {
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
					NombreEstado				: { type : "string"},
				}
			}
		}
	};
	obj.getSchemaFormasPagoPortafolio = function() {
		return { 
			model: {
				fields : {
					IdFormaDePago				: { type : "string"},
					NombreFormaDePago			: { type : "string"},
					Dias						: { type : "number"},
					IdPreferenciaPagoPortafolio	: { type : "number"},
					IdPreferenciaPago			: { type : "number"},
					IdMoneda 	  	  		    : { type : "string"},
					NombreBancoCuentaBancaria	: { type : "string"},
					NumeroCuentaBancaria	    : { type : "string"},
					MedioDePago   	  		    : { type : "string"},
					IdPortafolio	            : { type : "number"},
					IdTipoOperacion   	  		: { type : "string"},
					UsaCuentaBancaria			: { type : "string"},
				}
			}
		}
	};
	
	obj.getSchemaFormasPagoPortafolioCliente = function() {
		return { 
			model: {
				fields : {
					IdPreferenciaPago			: { type : "string"},
					MedioDePago					: { type : "string"},
					Dias						: { type : "number"},
					IdPreferenciaPagoPortafolio	: { type : "number"},
					IdTipoOperacion				: { type : "number"},
					IdMoneda 	  	  		    : { type : "string"},
					NombreBancoCuentaBancaria	: { type : "string"},
					NumeroCuentaBancaria	    : { type : "string"},
					MedioDePago   	  		    : { type : "string"},
					IdPortafolio	            : { type : "number"},
					IdTipoOperacion   	  		: { type : "string"},
					UsaCuentaBancaria			: { type : "string"},
				}
			}
		}
	};
	obj.getSchemaPreferenciaPagoPortafolio= function() {
		return { 
			model: {
				fields : {
					IdPreferenciaPagoPortafolio	: { type : "number"},
					IdPortafolio	            : { type : "number"},
					IdPreferenciaPago			: { type : "string"},
					IdCuentaBancaria			: { type : "number"},
					PorDefecto					: { type : "string"}
				}
			}
		}
	};
	obj.getSchemaPreferenciaPortafolio = function() {
		return { 
			model: {
				fields : {
					IdPortafolio 				: { type : "number"},
					OperaCustodia				: { type : "string"},
					SectorEconomico				: { type : "string"},
					CodigoOTC					: { type : "string"},
					DataTec						: { type : "string"},
					IdBovedaDefault				: { type : "string"},
				}
			}
		}
	};
	obj.getSchemaPreferenciaPago = function() {
		return { 
			model: {
				fields : {
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
					NombreEstado				: { type : "string"},
				}
			}
		}
	};
	obj.defDataSourceCarteraReservada = function(){
		return {
		  			type : "odata",
		  			definition : {
						serverFiltering : true, 
						serverPaging : true, 
						serverSorting : true, 
						sort: [{ field: "FolioCartera", dir: "desc" },
						       { field: "INGFechaOperacion", dir: "desc" }],
						pageSize : 5,
					    transport: {
							read: {
								url:bcsConsultasMesa('CarteraReservada'),
								dataType: "json"
							}
						},
						schema : obj.getSchemaCarteraReservadaMesa()
					},
					 
		  		}
	};
	obj.getDataSourceCarteraOperacion = function(){
		return new bcs.DataSource({
	  			type : "odata",
	  			definition : {
					serverFiltering : true, 
					serverPaging : true, 
					serverSorting : true, 
					sort: [{ field: "FolioCartera", dir: "desc" },
					       { field: "FechaOperacion", dir: "desc" }],
					pageSize : 200,
				    transport: {
						read: {
							url:bcsConsultasMesa('CarteraDisponibleInstrumento'),
							dataType: "json"
						}
					},
					schema : obj.getSchemaCarteraMesa()
				},
		})
	};
	obj.getDataSourceCarteraOperacionJava = function(){
		return new bcs.DataSource({
		type		: "call",
		definition	: {
			
			
			pageSize		: 200,
			callTransaction	: "4300004",
			callResultSet	: "P_OUT_SALIDA",
			schema : obj.getSchemaCarteraMesa()
			
		}
	})
	};
	obj.getDataSourceClientePortafolio = function(){
		return new bcs.DataSource({
	  			type : "odata",
	  			definition : {
					serverFiltering : true, 
					serverPaging : true, 
					serverSorting : true, 
					sort: [],
					pageSize : 200,
				    transport: {
						read: {
							url:bcsConsultasMesa('ConsultarClientePortafolio'),
							dataType: "json"
						}
					},
					schema : obj.getSchemaCarteraMesa()
				},
		})
	};
	obj.getDataSourceDetalleOperacionMesaRF = function(){
		return new bcs.DataSource({
	  			type : "odata",
	  			definition : {
					serverFiltering : true, 
					serverPaging : true, 
					serverSorting : true, 
					sort: [{ field: "IdOperacion", dir: "desc" }],
					pageSize : 200,
				    transport: {
						read: {
							url:bcsConsultasMesa('ConsultarDetalleOperacionMesaRF'),
							dataType: "json"
						}
					},
					schema : obj.getSchemaDetalleOperacionMesaRF()
				},
		})
	};
	obj.getDataSourceConsultarClienteCuentaPropia = function(){
		return new bcs.DataSource({
	  			type : "odata",
	  			definition : {
					serverFiltering : true, 
					serverPaging : true, 
					serverSorting : true, 
					pageSize : 200,
				    transport: {
						read: {
							url:bcsConsultasMesa('ConsultarClienteCuentaPropia'),
							dataType: "json"
						}
					},
					schema : obj.getSchemaConsultarClienteCuentaPropia()
				},
		})
	};
	obj.getDataFormaPagoGuardadasOperacionMesaRF = function(){
		return new bcs.DataSource({
	  			type : "odata",
	  			definition : {
					serverFiltering : true, 
					serverPaging : true, 
					serverSorting : true, 
					sort: [{ field: "IdOperacion", dir: "desc" }],
					pageSize : 200,
				    transport: {
						read: {
							url:bcsConsultasMesa('ConsultarFormasPagoOperacion'),
							dataType: "json"
						}
					},
					schema : obj.getSchemaComprobantePagoOperacionMesaRF()
				},
		})
	};
	
	obj.getDataSourceComprobantePagoOperacionMesaRF = function(){
		return new bcs.DataSource({
	  			type : "odata",
	  			definition : {
					serverFiltering : true, 
					serverPaging : true, 
					serverSorting : true, 
					sort: [{ field: "IdOperacion", dir: "desc" }],
					pageSize : 200,
				    transport: {
						read: {
							url:bcsConsultasMesa('ConsultarComprobantePagoOperacionMesaRF'),
							dataType: "json"
						}
					},
					schema : obj.getSchemaComprobantePagoOperacionMesaRF()
				},
		})
	};
	obj.getDataSourceComprobantePagoOperacionMesaSpot = function(){
		return new bcs.DataSource({
	  			type : "odata",
	  			definition : {
					serverFiltering : true, 
					serverPaging : true, 
					serverSorting : true, 
					sort: [{ field: "IdOperacion", dir: "desc" }],
					pageSize : 200,
				    transport: {
						read: {
							url:bcsConsultasMesa('ConsultarComprobantePagoOperacionMesaSpot'),
							dataType: "json"
						}
					},
					schema : obj.getSchemaComprobantePagoOperacionMesaRF()
				},
		})
	};

	obj.getDataSourceSocioDeNegocioRelacionado = function(IdCliente) {
		return new bcs.DataSource({
				type : "odata",
				definition : {
					serverFiltering:true,
					transport: {
						read: {
							url :bcsConsultasMesa('SocioNegocio'),
							dataType: "json"
					    }
					},
					filter: { field: "IdCliente", operator: "eq", value: Number(IdCliente) },
					schema: bcsConfiguracionSociodeNegocio.getSchemaSocioDeNegocioRelacionado()
				}
			})
	};
	obj.getDataSourceConsultarEstadoMesa = function() {
		return new bcs.newDataSource({
				type : "odata",
				definition : {
					serverFiltering:true,
					transport: {
						read: {
							url :bcsConsultasMesa('ConsultarCierreMesa'),
							dataType: "json"
					    }
					},
					filter:	{},//{ field: "IdMesa", operator: "eq", value: Number(IdMesaOrigen) },
					schema: obj.getSchemaMesaRF()
				}
			})
	};
	obj.getDataSourceConsultarMargenes = function() {
		return new bcs.DataSource({
				type : "odata",
				definition : {
					serverFiltering:true,
					transport: {
						read: {
							url :bcsConsultasMesa('ConsultarMargenes'),
							dataType: "json"
					    }
					},
					filter:	{},
					schema: obj.getSchemaMargenesMesaRF()
				}
			})
	};
	obj.getDataSourceFormasPagoPortafolioMesa = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverSorting : true, 
				sort: [{ field: "MedioDePago", dir: "asc" },
					{ field: "Dias", dir: "asc" }], //campo y criterio de ordenamiento
				transport: {
		    		read: {
		    			url: bcsConsultasMesa('ConsultarFormaDePagoPortafolio'),//"/BSCL/Plataforma/GWJAVA/PLA/call?t=1266",
						dataType:"json"
		    		}
		    	},
		    	
				schema : obj.getSchemaFormasPagoPortafolio()			
			}
		})
	};
	obj.getDataSourcePreferenciaPagoPortafolioCliente = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverSorting : true, 
				sort: [{ field: "IdPreferenciaPagoPortafolio", dir: "asc" }], 
				transport: {
		    		read: {
		    			url: bcsConsultasMesa('ConsultarPreferenciaPagoPortafolioCliente'),
						dataType:"json"
		    		}
		    	},
		    	
				schema : obj.getSchemaPreferenciaPagoPortafolio()			
			}
		})
	};

	obj.getDataSourcePreferenciaPagoPortafolioCorredor = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverSorting : true, 
				sort: [{ field: "IdPreferenciaPagoPortafolio", dir: "asc" }], 
				transport: {
		    		read: {
		    			url: bcsConsultasMesa('ConsultarPreferenciaPagoPortafolioCorredor'),
						dataType:"json"
		    		}
		    	},
		    	
				schema : obj.getSchemaPreferenciaPagoPortafolio()			
			}
		})
	};
	obj.getDataSourceFormasPagoPortaCliente = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverSorting : true, 
				sort: [{ field: "IdPreferenciaPagoPortafolio", dir: "asc" }], 
				transport: {
		    		read: {
		    			url: bcsConsultasMesa('ConsultarFormaDePagoCliente'),
						dataType:"json"
		    		}
		    	},
		    	
				schema : obj.getSchemaFormasPagoPortafolioCliente()			
			}
		})
	};
	
	obj.getDataSourceFormasPagoMesa = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverSorting : true, 
				sort: { field: "MedioDePago", dir: "asc" }, //campo y criterio de ordenamiento
				transport: {
		    		read: {
		    			url: bcsConsultasMesa('ConsultarFormaDePago'),//"/BSCL/Plataforma/GWJAVA/PLA/call?t=1769",
						dataType:"json"
		    		}
		    	},
				schema : obj.getSchemaFormasPago()			
			}
		})
	};
	obj.getDataSourcePreferenciaPortafolio = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverSorting : true, 
				sort: { field: "IdPortafolio", dir: "asc" }, //campo y criterio de ordenamiento
				transport: {
		    		read: {
		    			url: bcsConsultasMesa('ConsultarPreferenciaPortafolio'),
						dataType:"json"
		    		}
		    	},
				schema : obj.getSchemaPreferenciaPortafolio()			
			}
		})
	};
	obj.getDataSourcePreferenciaPago = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverSorting : true, 
				sort: { field: "IdPreferenciaPago", dir: "asc" }, //campo y criterio de ordenamiento
				transport: {
		    		read: {
		    			url: bcsConsultasMesa('ConsultarPreferenciaPago'),
						dataType:"json"
		    		}
		    	},
				schema : obj.getSchemaPreferenciaPago()			
			}
		})
	};
	
	/* SVARESGIN-7712 GDIAZ */
	obj.conservarFiltrosGrid = function(apiGrid, nuevosFiltros, camposConservar) {
	    var filtrosActuales = apiGrid.dataSource.filter();
	    if (!filtrosActuales || !filtrosActuales.filters) {
	        return nuevosFiltros;
	    }
	    filtrosActuales.filters.forEach(function(filtroActual) {
	        if (camposConservar.indexOf(filtroActual.field) === -1) {
	            return;
	        }
	        var yaExiste = nuevosFiltros.some(function(filtroNuevo) {
	            return filtroNuevo.field === filtroActual.field;
	        });
	        if (!yaExiste) {
	            nuevosFiltros.push(filtroActual);
	        }
	    });	
	    return nuevosFiltros;
	};
	/* SVARESGIN-7712 Fin */
	
	obj.validarFiltros = function(filtros,modo)
	{
		var filters = new Array();
		if(filtros.SesionIdEmpresa)
			filters.push({ field: "IdEmpresa", operator: "eq", value: Number(filtros.SesionIdEmpresa)});
		
		if (modo === 'CARTERA'){
			/*if(filtros.oConfiguracionPortafolio.idActual)
				filters.push({ field: "IdPortafolio", operator: "eq", value: Number(filtros.oConfiguracionPortafolio.idActual)});
			*/
			if(filtros.kngFecha){
				//filters.push({ field: "FechaVencimientoOperacion", operator: "gte", value: util.dateOdata(filtros.kngFecha)});
				//filters.push({ field: "FechaOperacion", operator: "gte", value: util.dateOdata(filtros.kngFecha)});
				//filters.push({ field: "FechaOperacion", operator: "lte", value: util.dateOdata(util.date(filtros.kngFecha).startOf('day').add(1,'d').toDate()) });
				//filters.push({ field: "FechaLiquidacion", operator: "lte", value: util.dateOdata(filtros.kngFecha)});
				filters.push({ field: "FechaLiquidacion", operator: "lte", value: util.dateTerminoDia(filtros.kngFecha)});
			}
			
			if(filtros.oConfiguracionMesaActiva.idActual){
				filters.push({ field: "IdMesa", operator: "eq", value: Number(filtros.oConfiguracionMesaActiva.idActual)});
				//Por jira IMPTANNOPT-271, se quita filtro IdPortafolio y solo busca por IdMesa
				//Se descomenta el cambio realizado. Se debe volver a revisar como abordar lo pedido en jira IMPTANNOPT-271
				filters.push({ field: "IdPortafolio", operator: "eq", value: Number(filtros.oConfiguracionMesaActiva.seleccionado.IdPortafolio)});
			}
			else {
				filters.push({ field: "IdCliente", operator: "eq", value: Number(filtros.oConfiguracionSocioNegocio.seleccionado.IdCliente)});
			}
		}
		
		if (modo === 'RESERVADA'){
			if(filtros.FolioCartera)
				filters.push({ field: "FolioCartera", operator: "eq", value: filtros.FolioCartera});
			else 
				filters.push({ field: "FolioCartera", operator: "eq", value: 0});
				
			if (filtros.CantidadReservada)
				filters.push({ field: "CantidadTotalReservada", operator: "gt", value: Number(0)});
		}
		
		if (modo === 'OPERACION' || modo === 'CARTERASOP'){
			if(modo ==='CARTERASOP'){
				if(filtros.oConfiguracionMesas.seleccionado.CodigoIFRS)//Filtro Codigo IFRS
					//filters.push({field: "CodigoIFRS", operator: "eq", value: filtros.oConfiguracionMesas.seleccionado.CodigoIFRS});
					//Ahora se traen las Carteras con  Codigos IFRS identicos a la mesa y los que no tienen
					filters.push({
		              logic: "or",
		              filters: [
		                { field: "CodigoIFRS", operator: "eq", value: filtros.oConfiguracionMesas.seleccionado.CodigoIFRS },
		                { field: "CodigoIFRS", operator: "eq", value: "" }
		              ]
		            });
				if(filtros.kngMontoOperacion){//Filtro para mostrar carteras con valor less Equals
					if(filtros.kngMontoOperacion!=0)
						filters.push({field: "INGMontoATasaCompraActual", operator: "lte", value: Number(filtros.kngMontoOperacion)});
				}
				
				//IMPMBIOPT-617 fix al agregar cartera se incorpora filtro FechaLiquidacionOperacion menor o igual a FechaLiquidacion del papel
				if((filtros.IdTipoOperacionCartera === 'COMTER' || filtros.IdTipoOperacionCartera === 'COMPAC' || filtros.oConfiguracionTipoOperacion.seleccionado.Codigo === 'COMTER' || filtros.oConfiguracionTipoOperacion.seleccionado.Codigo === 'COMPAC') && filtros.fechaLiquidacionOperacion){
					filters.push({ field: "FechaLiquidacion", operator: "lte", value: filtros.fechaLiquidacionOperacion });
				}
			}
			if(filtros.IdPortafolioFiltro)
				filters.push({ field: "IdPortafolio", operator: "eq", value: Number(filtros.IdPortafolioFiltro)});
			if(filtros.IdMesaOrigen)
				filters.push({ field: "IdMesaOrigen", operator: "eq", value: Number(filtros.IdMesaOrigen)});
			if(filtros.IdMesa)
				filters.push({ field: "IdMesa", operator: "eq", value: Number(filtros.IdMesa)});
			if(filtros.IdTipoProducto)
				filters.push({ field: "IdTipoProducto", operator: "eq", value: filtros.IdTipoProducto});
			if (filtros.IdClienteFiltro)
				filters.push({ field: "IdCliente", operator: "eq", value: Number(filtros.IdClienteFiltro)});
			if (filtros.IdTipoOperacion)
				filters.push({ field: "IdTipoOperacion", operator: "eq", value: filtros.IdTipoOperacion});
			if (filtros.IdTipoReajuste)
				filters.push({ field: "IdTipoReajuste", operator: "eq", value: filtros.IdTipoReajuste});
			if (filtros.MontoCompra)
				filters.push({ field: "MontoCompra", operator: "gte", value: Number(filtros.MontoCompra)});
			if (filtros.IdOperacion)
				filters.push({ field: "IdOperacion", operator: "eq", value: filtros.IdOperacion});
			if (filtros.FechaOperacion){
				filters.push({ field: "FechaOperacion", operator: "gte", value: util.dateInicioDia(filtros.FechaOperacion)});
				filters.push({ field: "FechaOperacion", operator: "lte", value: util.dateTerminoDia(filtros.FechaOperacionHasta)});				
				//filters.push({ field: "FechaOperacion", operator: "lte", value: util.dateOdata(util.date(filtros.FechaOperacion).startOf('day').add(1,'d').toDate()) });
			}
			if (filtros.FechaVencimiento){
				filters.push({ field: "FechaVencimiento", operator: "gte", value: filtros.FechaVencimiento});
				filters.push({ field: "FechaVencimiento", operator: "lte", value: filtros.FechaVencimientoHasta});
				
				/*if (filtros.Modo === 'ANT') {
					// MSH - Por UI ya viene la fecha de sistema bloqueada en este campo
					filters.push({ field: "FechaOperacion", operator: "lt", value: filtros.FechaVencimiento});
				}*/
			}
			if((filtros.fechaVencimientoOperacion && filtros.oConfiguracionTipoOperacion.seleccionado.Codigo != "COMTER") && (filtros.fechaVencimientoOperacion && filtros.oConfiguracionTipoOperacion.seleccionado.Codigo != "VENTER")){
				filters.push({ field: "FechaVencimientoInstrumento", operator: "gte", value: filtros.fechaVencimientoOperacion});				
			}
			//Fecha ingreso anticipo 
			if(filtros.FechaIngreso){
				//fecha de ingreso sea distinta a la fecha sistema
				filters.push({ field: "FechaOperacion", operator: "neq", value: filtros.FechaIngreso});
			}
			if (filtros.IdEstado){
				filters.push({ field: "IdEstado", operator: "eq", value: filtros.IdEstado});
			}
			if (filtros.FechaLiquidacion){
				filters.push({ field: "FechaCondicionLiquidacion", operator: "gte", value: filtros.FechaLiquidacion});
				filters.push({ field: "FechaCondicionLiquidacion", operator: "lte", value: filtros.FechaLiquidacionHasta});
				//filters.push({ field: "FechaLiquidacion", operator: "gte", value: filtros.FechaLiquidacion});
				//filters.push({ field: "FechaOperacion", operator: "lte", value: util.dateOdata(util.date(filtros.FechaLiquidacion).startOf('day').add(1,'d').toDate()) });				
			}
			if (filtros.IdMonedaLiquidacionOperacion){
				filters.push({ field: "MonedaInstrumento", operator: "eq", value: filtros.IdMonedaLiquidacionOperacion});
			}
			if (filtros.TipoInstrumento && (filtros.TipoInstrumento === 'PAC' || filtros.TipoInstrumento === 'DEP'|| filtros.TipoInstrumento === 'AUT')){
				filters.push({
		              logic: "or",
		              filters: [
		                { field: "IdTipoOperacion", operator: "eq", value: "COMPAC" },
		                { field: "IdTipoOperacion", operator: "eq", value: "VENPAC" },
		                { field: "IdTipoOperacion", operator: "eq", value: "TDEPLA" },
		                { field: "IdTipoOperacion", operator: "eq", value: "TAUTDI" },
		              ]
		            });
				if (filtros.Modo === 'VEN'){
					filters.push({
		              logic: "or",
		              filters: [
		                { field: "IdEstado", operator: "eq", value: "LIQ" },//validar si son las vigentes
		                { field: "IdEstado", operator: "eq", value: "VIG" },
		                { field: "IdEstado", operator: "eq", value: "ANT" }
		              ]
		            });
				}
				// Agregar Anticipos en pestaña anticipos FG/AB
				if (filtros.Modo === 'ANT'){
			          filters.push({
			                  logic: "or",
			                  filters: [
			                    { field: "IdEstado", operator: "eq", value: "VIG" },
			                    { field: "IdEstado", operator: "eq", value: "CNF" },
			                    { field: "IdEstado", operator: "eq", value: "ANT" }
			                  ]
			                });
			     }
			}
			if (filtros.CarteraPacto === 'S'){
				// ES una VP => Considera carteras de CP
				filters.push( {
					logic: "or",
				    filters: [
				      {	logic: "and",
					    filters: [
					      { field: "CarteraPacto", operator: "eq", value: "N" },
					      { field: "FechaLiquidacion", operator: "lte", value: filtros.fechaLiquidacionOperacion }
					    ]
					  }, 
				      {	logic: "and",
					    filters: [
					      { field: "CarteraPacto", operator: "eq", value: "S" },
					      { field: "FechaLiquidacion", operator: "gte", value: filtros.fechaVencimientoOperacion }
					    ]
				      } 
				    ]
				  }
				);
				
			}
			
			if (filtros.CarteraPacto === 'N'){
				filters.push({ field: "CarteraPacto", operator: "eq", value: 'N'});
				filters.push({ field: "FechaLiquidacion", operator: "lte", value: filtros.fechaLiquidacionOperacion});
			}
			if(filtros.FechaOperacionPorEstadoFecha && filtros.FechaOperacionHastaPorEstadoFecha && (filtros.IdEstadoPorEstadoFecha || filtros.IdEstadoPorEstadoFecha === 0)){
				if (filtros.IdEstadoPorEstadoFecha == 0){
					var casoTodos = {
							logic: "and",
							filters: [
								{ field: "FechaOperacion", operator: "gte", value: util.dateInicioDia(filtros.FechaOperacionPorEstadoFecha) },
								{ field: "FechaOperacion", operator: "lte", value: util.dateTerminoDia(filtros.FechaOperacionHastaPorEstadoFecha) }
							]
						};
						filters.push({
							logic: "and",
							filters: [casoTodos]
						});
				} else {
					var casoEstado = {
							logic: "and",
							filters: [
								{ field: "FechaOperacion", operator: "gte", value: util.dateInicioDia(filtros.FechaOperacionPorEstadoFecha) },
								{ field: "FechaOperacion", operator: "lte", value: util.dateTerminoDia(filtros.FechaOperacionHastaPorEstadoFecha) },
								{ field: "IdEstado", operator: "eq", value: filtros.IdEstadoPorEstadoFecha }
							]
						};
						filters.push({
							logic: "and",
							filters: [casoEstado]
						});
				}
			}
			if (filtros.ConfirmaAdemdum === ''){
				var casoPacto = {
					logic: "or",
					filters: [
						{ field: "ConfirmaAdemdum", operator: "eq", value: 'N'},
						{ field: "ConfirmaAdemdum", operator: "eq", value: 'S'},
						{ field: "ConfirmaAdemdum", operator: "eq", value: null},
						{ field: "ConfirmaAdemdum", operator: "eq", value: ''}
					]
				};
				filters.push({
					logic: "and",
					filters: [casoPacto]
				});
			}
		if (filtros.ConfirmaAdemdum === 'S'){
				filters.push({
					logic: "and",
					filters: [
						{ field: "ConfirmaAdemdum", operator: "eq", value: 'S'}
					]
				});
			}
		}

		
		
		
		return filters;
		
	};
	/***Configuracion grid******/
	obj.getConfiguracionCarteraReservadaMesa  = function(filtros) {
		var config = {};
		var defDataSource = obj.defDataSourceCarteraReservada();
		defDataSource.definition.filter = obj.validarFiltros(filtros,'RESERVADA');
		config = {
			
			options : {
				sortable: false,
	      		reorderable: false,
	      		filterable: false,
				groupable: false,
				pageable: true,
				selectable: "multiple, row",
				reorderable: false,
				allowCopy: true,
				resizable: true,
	      		autoBind: true,
	      		scrollable: true,
	      		height:100,
	
				dataSource: new bcs.DataSource(defDataSource),
	  			columns: [
	  			         getConfiguracionColumna("FolioCartera")
	  			         ,getConfiguracionColumna("FechaIngreso")
	  			        //,getConfiguracionColumna("Mercado")
						//,getConfiguracionColumna("FechaCompra")
						//,getConfiguracionColumna("FechaLiquidacion")
						//,getConfiguracionColumna("Instrumento")
						//,getConfiguracionColumna("Emisor")
						//,getConfiguracionColumna("TipoReajuste")
						,getConfiguracionColumna("CantidadTotalReservada")
						,getConfiguracionColumna("CodigoUsuarioReserva")
						//,getConfiguracionColumna("NumCortes")
						//,getConfiguracionColumna("TasaMercado")
						//,getConfiguracionColumna("ValorMercado")
						//,getConfiguracionColumna("Fungible")
						

				],
				
			}
		};
		return config;
	};

	obj.getColumnasFormasPagoPortafolio  = function() {
		var config = {};
		var defDataSource = obj.getDataSourceFormasPagoPortaCliente();
		config = {
			
			options : {
				sortable: false,
	      		reorderable: false,
	      		filterable: false,
				groupable: false,
				pageable: true,
				selectable: "multiple, row",
				reorderable: false,
				allowCopy: true,
				resizable: true,
	      		autoBind: true,
	      		scrollable: true,
	      		height:100,
	
				dataSource: new bcs.DataSource(defDataSource),
	  			columns: [
	  			       	 getConfiguracionColumna("NombreFormaDePago")
	  			    	,getConfiguracionColumna("Moneda")
	  			        
						,getConfiguracionColumna("FechaLiquidacion")
						,getConfiguracionColumna("NombreBanco")
						,getConfiguracionColumna("CuentaBancaria")

				],
				
			}
		};
		return config;
	};
	/*Cartera Renta Fija Menu*/
	obj.getConfiguracionCarteraMesa = function() {
		var config = {};
		config = {
			buscar : function(apiGrid,filtros) {
				apiGrid.dataSource.filter(obj.validarFiltros(filtros,'CARTERA'));
				apiGrid.dataSource.filter({
					logic: "and",
					filters: [{field: "DocumentoIdentidad",operator: "eq",value: filtros.oConfiguracionSocioNegocio.seleccionado.DocumentoIdentidadDefecto}]
				});
			},
			
			options : {
				sortable: true,
	      		reorderable: true,
	      		filterable: true,
				groupable: true,
				pageable: true,
				selectable: "multiple, row",
				reorderable: true,
				allowCopy: true,
				resizable: true,
	      		autoBind: false,
	      		scrollable: true,
	      		height:527,
				userSettings: true,
				dataSource: new bcs.DataSource({
		  			type : "odata",
		  			definition : {
						serverFiltering : true, 
						serverPaging : true, 
						serverSorting : true, 
						sort: [{ field: "FolioCartera", dir: "desc" },
						       { field: "FechaOperacion", dir: "desc" }],
						pageSize : 200,
					    transport: {
							read: {
								url:bcsConsultasMesa('CarteraInstrumento'),
								dataType: "json"
							}
						},
						schema : obj.getSchemaCarteraMesa()
					},
		  		}),
	  			columns: [
	  			         getConfiguracionColumna("Estado",{ groupable: true})
	  			        ,getConfiguracionColumna("DocumentoIdentidad",{ groupable: true})
	  			        ,getConfiguracionColumna("RazonSocial",{ groupable: true})
	  			        ,getConfiguracionColumna("CodigoPortafolio",{ groupable: true})
						,getConfiguracionColumna("FolioCartera",{ groupable: true})
						,getConfiguracionColumna("FechaOperacion",{ groupable: true})
						,getConfiguracionColumna("FechaLiquidacion",{ groupable: true})
						,getConfiguracionColumna("Nemotecnico",{ groupable: true})
						,getConfiguracionColumna("NemoSVS",{ groupable: true})
						,getConfiguracionColumna("IdTipoInstrumento",{ groupable: true})
						,getConfiguracionColumna("IdEmisor",{ groupable: true})
						,getConfiguracionColumna("IdTipoReajuste",{ groupable: true})
						,getConfiguracionColumna("INGCantidad",{ groupable: true})
						,getConfiguracionColumna("CorteMinimo",{ groupable: true})
						//,getConfiguracionColumna("Precio",{ groupable: true})
						,getConfiguracionColumna("TasaCompra",{ groupable: true})//VALIDAR						
						,getConfiguracionColumna("INGTasaMercadoActual",{ groupable: true})
						,getConfiguracionColumna("INGValorRescate",{ groupable: true})
						,getConfiguracionColumna("INGMontoCompraActualMonedaInstrumento",{ groupable: true})
						,getConfiguracionColumna("INGMontoATasaCompraActual",{ groupable: true,aggregates: ["sum"],groupFooterTemplate: "#= util.formateaNumero(sum,0) #"})
						,getConfiguracionColumna("FechaVencimientoInstrumento",{ groupable: true})
						,getConfiguracionColumna("FechaVencimientoOperacion",{ groupable: true})
						,getConfiguracionColumna("IdBoveda",{ groupable: true})
						
						,getConfiguracionColumna("INGMontoMercadoActualMonedaInstrumento",{ groupable: true})
						,getConfiguracionColumna("INGMontoATasaMercadoActual",{ groupable: true,aggregates: ["sum"],groupFooterTemplate: "#= util.formateaNumero(sum,0) #"})
						
						,getConfiguracionColumna("EsFungible",{ groupable: true})
						,getConfiguracionColumna("IdClasificacionTipoInstrumento",{ groupable: true})
						,getConfiguracionColumna("INGDurationCompraProximo",{ groupable: true})
						,getConfiguracionColumna("INGDurationCompraActual",{ groupable: true})
						,getConfiguracionColumna("ValorClasificacionRiesgo",{ groupable: true})
						,getConfiguracionColumna("INGMontoInteresCompraProximo",{ groupable: true})
						,getConfiguracionColumna("INGMontoReajusteCompraProximo",{ groupable: true})
						//,getConfiguracionColumna("InteresAcumulado",{ groupable: true})
						//,getConfiguracionColumna("ReajusteAcumulado",{ groupable: true})
						
						//,getConfiguracionColumna("MontoCompra",{ groupable: true})// VALIDAR

						,getConfiguracionColumna("CodigoDescIFRS",{ groupable: true})
						//,getConfiguracionColumna("ClasificacionCartera",{ groupable: true})
						//,getConfiguracionColumna("TipoCartera",{ groupable: true})
						
						
				],
				gridCreated	    : function(grid)
				{
					if ($rootScope.SesionLocale === 'PE'){
			    		grid.showColumn("INGDurationCompraActual");
			    	}else{
			    		grid.showColumn("INGDurationCompraProximo");
			    	}
				}
				
			}
		};
		return config;
	};
	
	obj.getConfiguracionOperacionesMesaRF = function() {
		var modo = '0';
		var config = {};
		config = {
			buscar : function(apiGrid,filtros) {
				var filter = {};
				var fechaSistema = util.dateDS($rootScope.SesionFechaSistema,"FechaSistema","CALL");			
				
				filter.Modo = filtros.Modo;
				switch(filtros.Modo) {
					case 'ANT':
				        filter.IdMesaOrigen				= filtros.oConfiguracionMesas.idActual;
						filter.IdTipoProducto 			= filtros.oConfiguracionMesas.seleccionado.IdTipoProducto;
						if(filtros.kngFechaOperacion && filtros.kngFechaOperacionFin){
							filter.FechaVencimiento			= util.dateSPdate(filtros.kngFechaOperacion);
							filter.FechaVencimientoHasta 	= util.dateSPdate(filtros.kngFechaOperacionFin);
						}
						filter.TipoInstrumento			= 'PAC'
						filter.FechaIngreso				= util.dateSPdate(fechaSistema);
		  			    modo = 'ANT';
						
				        break;
					case 'VEN':
						filter.IdMesaOrigen 			= filtros.oConfiguracionMesas.idActual;
						filter.IdTipoProducto 			= filtros.oConfiguracionMesas.seleccionado.IdTipoProducto;
						filter.FechaLiquidacion			= util.dateSPdate(filtros.kngFechaOperacion);
						filter.FechaLiquidacionHasta	= util.dateSPdate(filtros.kngFechaOperacionFin);
						filter.TipoInstrumento			= 'PAC'
			  			modo = 'VEN';
						break;
					default ://'OPE':
				        filter.IdMesaOrigen			= filtros.oConfiguracionMesas.idActual;
						filter.IdTipoProducto 		= filtros.oConfiguracionMesas.seleccionado.IdTipoProducto;
						if(filtros.opcionesFiltro.opPorDia){
							filter.FechaOperacion 		= util.dateSPdate(fechaSistema);
							filter.FechaOperacionHasta 	= util.dateSPdate(fechaSistema);
						}
						else if(filtros.opcionesFiltro.opPorEstadoFecha){
							filter.FechaOperacionPorEstadoFecha 		= util.dateSPdate(filtros.kngFechaOperacion);
							filter.FechaOperacionHastaPorEstadoFecha 	= util.dateSPdate(filtros.kngFechaOperacionFin);
							filter.IdEstadoPorEstadoFecha = filtros.cmbbx.IdEstado;
						}

						if(filtros.opcionesPacto.confPacto){
							filter.ConfirmaAdemdum = 'S';
						} else {
							filter.ConfirmaAdemdum = '';
						}
			  			modo = 'OPE';
				        break;
				} 				
				
				/* SVARESGIN-7712 GDIAZ */
				var filtrosActuales = apiGrid.dataSource.filter();
				var nuevosFiltros =
				    obj.validarFiltros(filter, 'OPERACION');				
				var camposConservar = [
				    "FolioOperacion",
				    "IdOperacion",
				    "NombreEstado",
				    "Intermesa",
				    "NombreMesaOrigen",
				    "NombreMesaDestino",
				    "NombreOperacion",
				    "RazonSocial",
				    "DocumentoIdentidad",
				    "CodigoPortafolio",
				    "Tasa",
				    "PlazoEnDias",
				    "MontoOperacionInicialMoneda",
				    "IdTipoReajuste",
				    "MontoOperacionInicial",
				    "IdMoneda",
				    "MontoOperacionFinal",
				    //"FechaOperacion",
				    "FechaVencimiento",
				    "FechaLiquidacionString",
				    "IdAgente",
				    "CodigoUsuario",
				    "ConfirmaAdemdum",
				    "EstadoConfirmacion",
				    "CodigoUsuarioConfirma",
				    "Glosa",
				    "IdOperacionOrigenReasignacion",
				    "ValorizacionManual",
				    "Spread"
				];
				
				if(filtrosActuales && filtrosActuales.filters) {
				
				    filtrosActuales.filters.forEach(function(filtroActual) {
				
				        if(camposConservar.indexOf(filtroActual.field) !== -1) {
				
				            nuevosFiltros.push(filtroActual);
				        }
				    });
				}
				apiGrid.dataSource.filter({
				    logic: "and",
				    filters: nuevosFiltros
				});
				/* SVARESGIN-7712 Fin */
				filtros.seleccionOperacionesMesaRF = undefined;
			},
			
			options : {
				sortable: true,
	      		reorderable: true,
	      		filterable: true,
				groupable: true,
				pageable: true,
				selectable: "single, row",
				reorderable: true,
				allowCopy: true,
				resizable: true,
	      		autoBind: false,
	      		scrollable: true,
	      		height:550,
				userSettings: true,

				rowColorBackground	: function(row) {		
							switch(row.get("IdEstado")) {
					            case 'PEN':
					                return "blue";
					                break;
					            case 'ANU':
					                return "red";
					                break;
					             case 'LIQ':
					                return "green";
					                break;
					             case 'EMI':
					                return "orange";
					                break;    
					             case 'RGN':
					                return "brown-font";
					                break;	
					             case 'ANT':
					                return "light-green-font";
					                break;					                				                    
					            default:
					            	return "black";
					                break;     
							  }	
		  				},			
				boldRow: function() {
					return "bold";
				},

				dataSource: new bcs.DataSource({
		  			type : "odata",
		  			definition : {
						serverFiltering : true, 
						serverPaging : true, 
						serverSorting : true, 
						sort: [{ field: "FolioOperacion", dir: "desc" },
						       { field: "FechaOperacion", dir: "desc" }],
						pageSize : 200,
					    transport: {
							read: {
								url:bcsConsultasMesa('ConsultarOperacionesMesaRF'),
								dataType: "json"
							}
						},
						schema : obj.getSchemaOperacionesMRF(),
					},
		  		}),
	  			columns: [
				      	 getConfiguracionColumna("FolioOperacion")
				      	,getConfiguracionColumna("IdOperacion")
				      	,getConfiguracionColumna("NombreEstado",{ groupable: true})
				      	,getConfiguracionColumna("Intermesa",{ groupable: true})
				      	,getConfiguracionColumna("NombreMesaOrigen",{ groupable: true})
				      	,getConfiguracionColumna("NombreMesaDestino",{ groupable: true})
				      	,getConfiguracionColumna("NombreOperacion",{ groupable: true})
	  			        ,getConfiguracionColumna("RazonSocial",{ groupable: true})
	  			        ,getConfiguracionColumna("DocumentoIdentidad",{ groupable: true})
	  			        ,getConfiguracionColumna("CodigoPortafolio",{ groupable: true})
	  			        ,getConfiguracionColumna("Tasa")
	  			        ,getConfiguracionColumna("PlazoEnDias",{ groupable: true})
	  			        
	  			        ,getConfiguracionColumna("MontoOperacionInicialMoneda",{aggregates: ["sum"],groupFooterTemplate: "#= util.formateaNumero(sum,2) #"})
	  			        ,getConfiguracionColumna("IdTipoReajuste",{ groupable: true})
	  			        
	  			        ,getConfiguracionColumna("MontoOperacionInicial",{aggregates: ["sum"],groupFooterTemplate: "#= util.formateaNumero(sum,2) #"})
	  			        ,getConfiguracionColumna("IdMoneda",{ groupable: true})

	  			        ,getConfiguracionColumna("MontoOperacionFinal",{aggregates: ["sum"],groupFooterTemplate: "#= util.formateaNumero(sum,2) #"})
	  			        
	  			        ,getConfiguracionColumna("FechaOperacion",{ groupable: true})
	  			        ,getConfiguracionColumna("FechaVencimiento",{ groupable: true})
	  			        ,getConfiguracionColumna("FechaLiquidacionString",{ groupable: true})
	  			        ,getConfiguracionColumna("IdAgente",{ groupable: true})
	  			        ,getConfiguracionColumna("CodigoUsuario",{ groupable: true})
	  			        ,getConfiguracionColumna("ConfirmaAdemdum",{ groupable: true})
	  			        ,getConfiguracionColumna("EstadoConfirmacion",{ groupable: true})
	  			        ,getConfiguracionColumna("CodigoUsuarioConfirma",{ groupable: true})
	  			        ,getConfiguracionColumna("Glosa")
						,getConfiguracionColumna("IdOperacionOrigenReasignacion",{ groupable: true})
						,getConfiguracionColumna("ValorizacionManual",{ groupable: true})
						,getConfiguracionColumna("Spread",{ groupable: true})
	  			        
				],
				
			}
		};
		return config;
	};

	
	obj.getConfiguracionOperacionesMesaRF_Anticipos = function() {
		var config = {};
		config = {
			buscar : function(apiGrid,filtros) {
				var filter = {};
				var fechaSistema = util.dateDS($rootScope.SesionFechaSistema,"FechaSistema","CALL");			
				
				filter.Modo = filtros.Modo;
				switch(filtros.Modo) {
					case 'ANT':
				        filter.IdMesaOrigen				= filtros.oConfiguracionMesas.idActual;
						filter.IdTipoProducto 			= filtros.oConfiguracionMesas.seleccionado.IdTipoProducto;
						if(filtros.kngFechaOperacion && filtros.kngFechaOperacionFin){
							filter.FechaVencimiento			= util.dateSPdate(filtros.kngFechaOperacion);
							filter.FechaVencimientoHasta 	= util.dateSPdate(filtros.kngFechaOperacionFin);
						}
						filter.TipoInstrumento			= 'PAC'
						filter.FechaIngreso				= util.dateSPdate(fechaSistema);
						
				        break;
					/*case 'VEN':
						filter.IdMesaOrigen 			= filtros.oConfiguracionMesas.idActual;
						filter.IdTipoProducto 			= filtros.oConfiguracionMesas.seleccionado.IdTipoProducto;
						filter.FechaLiquidacion			= util.dateSPdate(filtros.kngFechaOperacion);
						filter.FechaLiquidacionHasta	= util.dateSPdate(filtros.kngFechaOperacionFin);
						filter.TipoInstrumento			= 'PAC'
						break;*/
					default ://'OPE':
				        filter.IdMesaOrigen			= filtros.oConfiguracionMesas.idActual;
						filter.IdTipoProducto 		= filtros.oConfiguracionMesas.seleccionado.IdTipoProducto;
						if(filtros.opcionesFiltro.opPorDia){
							filter.FechaOperacion 		= util.dateSPdate(fechaSistema);
							filter.FechaOperacionHasta 	= util.dateSPdate(fechaSistema);
						}
						else if(filtros.opcionesFiltro.opPorEstadoFecha){
							filter.FechaOperacionPorEstadoFecha 		= util.dateSPdate(filtros.kngFechaOperacion);
							filter.FechaOperacionHastaPorEstadoFecha 	= util.dateSPdate(filtros.kngFechaOperacionFin);
							filter.IdEstadoPorEstadoFecha = filtros.cmbbx.IdEstado;
						}
				        break;
				} 				
				
				/* SVARESGIN-7712 GDL */
				var nuevosFiltros = obj.validarFiltros(filter, 'OPERACION');
				
				var camposConservar = [
				    "FolioOperacion",
				    "IdOperacion",
				    "NombreEstado",
				    "Intermesa",
				    "NombreMesaOrigen",
				    "NombreMesaDestino",
				    "NombreOperacion",
				    "RazonSocial",
				    "DocumentoIdentidad",
				    "CodigoPortafolio",
				    "Tasa",
				    "PlazoEnDias",
				    "MontoOperacionInicialMoneda",
				    "IdTipoReajuste",
				    "MontoOperacionInicial",
				    "IdMoneda",
				    "MontoOperacionFinalAnticipo",
				    "FechaOperacion",
				    "FechaVencimiento",
				    "FechaLiquidacion",
				    "IdAgente",
				    "CodigoUsuario",
				    "Glosa",
				    "IdOperacionOrigenReasignacion"
				];
				
				nuevosFiltros = obj.conservarFiltrosGrid(
				    apiGrid,
				    nuevosFiltros,
				    camposConservar
				);
				
				apiGrid.dataSource.filter({
				    logic: "and",
				    filters: nuevosFiltros
				});
				/* SVARESGIN-7712 Fin */
				
				filtros.seleccionOperacionesMesaRF = undefined;
			},
			
			options : {
				sortable: true,
	      		reorderable: true,
	      		filterable: true,
				groupable: true,
				pageable: true,
				selectable: "multiple, row",
				reorderable: true,
				allowCopy: true,
				resizable: true,
	      		autoBind: false,
	      		scrollable: true,
	      		height:600,
				userSettings: true,
				rowColorBackground	: function(row) {		
					switch(row.get("IdEstado")) {
			             case 'ANT':			                
			                return "light-green-font";
			                break;					                				                    
			            default:
			            	return "black";
			                break;     
					  }	
				},
				boldRow: function() {
					return "bold";
				},
				
			
				dataSource: new bcs.newDataSource({
		  			type : "odata",
		  			definition : {
						serverFiltering : true, 
						serverPaging : true, 
						serverSorting : true, 
						sort: [{ field: "FolioOperacion", dir: "desc" },
						       { field: "FechaOperacion", dir: "desc" }],
						pageSize : 200,
					    transport: {
							read: {
								url:bcsConsultasMesa('ConsultarOperacionesMesaRF'),
								dataType: "json"
							}
						},
						schema : obj.getSchemaOperacionesMRF(),
					},
		  		}),
	  			columns: [
				      	 getConfiguracionColumna("FolioOperacion")
				      	,getConfiguracionColumna("IdOperacion")
				      	,getConfiguracionColumna("NombreEstado",{ groupable: true})
				      	,getConfiguracionColumna("Intermesa",{ groupable: true})
				      	,getConfiguracionColumna("NombreMesaOrigen",{ groupable: true})
				      	,getConfiguracionColumna("NombreMesaDestino",{ groupable: true})
				      	,getConfiguracionColumna("NombreOperacion",{ groupable: true})
	  			        ,getConfiguracionColumna("RazonSocial",{ groupable: true})
	  			        ,getConfiguracionColumna("DocumentoIdentidad",{ groupable: true})
	  			        ,getConfiguracionColumna("CodigoPortafolio",{ groupable: true})
	  			        ,getConfiguracionColumna("Tasa")
	  			        ,getConfiguracionColumna("PlazoEnDias",{ groupable: true})
	  			        
	  			        ,getConfiguracionColumna("MontoOperacionInicialMoneda")
	  			        ,getConfiguracionColumna("IdTipoReajuste",{ groupable: true})
	  			        
	  			        ,getConfiguracionColumna("MontoOperacionInicial")
	  			        ,getConfiguracionColumna("IdMoneda",{ groupable: true})

	  			        ,getConfiguracionColumna("MontoOperacionFinalAnticipo")
	  			        
	  			        ,getConfiguracionColumna("FechaOperacion",{ groupable: true})
	  			        ,getConfiguracionColumna("FechaVencimiento",{ groupable: true})
	  			        ,getConfiguracionColumna("FechaLiquidacion",{ groupable: true})
	  			        ,getConfiguracionColumna("IdAgente",{ groupable: true})
	  			        ,getConfiguracionColumna("CodigoUsuario",{ groupable: true})
	  			        ,getConfiguracionColumna("Glosa")
						,getConfiguracionColumna("IdOperacionOrigenReasignacion",{ groupable: true})
	  			        
				],
				
			}
		};
		return config;
	};
	
	obj.getConfiguracionOperacionesMesaRF_Reasignacion = function() {
		var config = {};
		config = {
			buscar : function(apiGrid,filtros) {
				var filter = {};
				filter.p_IdEmpresa 					= Number(filtros.SesionIdEmpresa);
				filter.p_IdMesa 					= filtros.oConfiguracionMesas.idActual;
				filter.p_IdTipoProducto 			= filtros.oConfiguracionMesas.seleccionado.IdTipoProducto;
				filter.p_FechaVencimiento			= util.dateSPdate(filtros.kngFechaOperacion);
				filter.p_FechaVencimientoFin		= util.dateSPdate(filtros.kngFechaOperacionFin);
				filter.p_TipoOperacion				= 	[{IdTipoOperacion : 'COMPAC'}
														,{IdTipoOperacion : 'VENPAC'}];
				filter.p_Estados					= 	[{IdEstado : 'LIQ'}
														,{IdEstado : 'VIG'}
														,{IdEstado : 'ANT'}
														];
				modelFiltros=filter;
				/*
				 * CELLINCE-1518 antes se llamaba a un servicio API, ahora este es gatillado por .filter
				 */
				apiGrid.dataSource.filter({});
				filtros.seleccionOperacionesMesaRF = undefined;
			},
			
			options : {
				sortable: true,
	      		reorderable: true,
	      		filterable: true,
				groupable: true,
				pageable: true,
				selectable: "multiple, row",
				reorderable: true,
				allowCopy: true,
				resizable: true,
	      		autoBind: false,
	      		scrollable: true,
	      		height:600,
				userSettings: true,
/*jala nuevo es que se cambio al segmento definition agregando transport.read llamando a una funcion, esto hace que ahora que los filtros en la new Grid funcionen de forma correcta CELLINCE-1518
 * 
 */				
				dataSource: new bcs.DataSource({
		  			type : "call",
		  			definition : {
						serverFiltering : false, 
						serverPaging : true, 
						serverSorting : true, 
						sort: [{ field: "FolioOperacion", dir: "desc" },
						       { field: "FechaOperacion", dir: "desc" }],
						pageSize : 200,
					    transport: {
							read: function(operation) {
								var data = operation.data.data || [];
								
								if(modelFiltros.p_IdEmpresa) {
									bcsServiciosMesa.ConsultarOperacionesMesaRF(modelFiltros,function(result){
										operation.success(result.P_OPERACIONES_OUT);
									},function(result){
										operation.error(result);
									});
								}
								else {
									operation.success(data);	
								}
							}
						},
						schema : obj.getSchemaOperacionesMRF(),
					},
		  		}),
	  			columns: [
				      	 getConfiguracionColumna("FolioOperacion")
				      	,getConfiguracionColumna("IdOperacion")
				      	,getConfiguracionColumna("NombreEstado",{ groupable: true})
				      	,getConfiguracionColumna("Intermesa",{ groupable: true})
				      	,getConfiguracionColumna("NombreMesaOrigen",{ groupable: true})
				      	,getConfiguracionColumna("NombreMesaDestino",{ groupable: true})
				      	,getConfiguracionColumna("NombreOperacion",{ groupable: true})
				        ,getConfiguracionColumna("RazonSocial",{ groupable: true})
				        ,getConfiguracionColumna("DocumentoIdentidad",{ groupable: true})
				        ,getConfiguracionColumna("CodigoPortafolio",{ groupable: true})
				        ,getConfiguracionColumna("Tasa")
				        ,getConfiguracionColumna("PlazoEnDias",{ groupable: true})
				        ,getConfiguracionColumna("MontoOperacionInicialMoneda")
				        ,getConfiguracionColumna("IdTipoReajuste",{ groupable: true})	        
				        ,getConfiguracionColumna("MontoOperacionInicial")
				        ,getConfiguracionColumna("IdMoneda",{ groupable: true})
	  			        ,getConfiguracionColumna("MontoOperacionFinal")
				        ,getConfiguracionColumna("SaldoCaja")
				        ,getConfiguracionColumna("FechaOperacion",{ groupable: true})
				        ,getConfiguracionColumna("FechaVencimiento",{ groupable: true})
				        ,getConfiguracionColumna("FechaCondicionLiquidacion",{ groupable: true})
				        ,getConfiguracionColumna("IdAgente",{ groupable: true})
				        ,getConfiguracionColumna("CodigoUsuario",{ groupable: true})
				        ,getConfiguracionColumna("TipoDocumento",{ groupable: true})
				        ,getConfiguracionColumna("Glosa")
						,getConfiguracionColumna("IdOperacionOrigenReasignacion",{ groupable: true})

				],
				
			}
		};
		return config;
	};
	
	obj.getConfiguracionOperacionesMesaRF_Vencimiento = function() {
		var config = {};
		config = {
			buscar : function(apiGrid,filtros) {
				var filter = {};
				filter.p_IdEmpresa 					= Number(filtros.SesionIdEmpresa);
				filter.p_IdMesa 					= filtros.oConfiguracionMesas.idActual;
				filter.p_IdTipoProducto 			= filtros.oConfiguracionMesas.seleccionado.IdTipoProducto;
				filter.p_FechaVencimiento			= util.dateSPdate(filtros.kngFechaOperacion);
				filter.p_FechaVencimientoFin		= util.dateSPdate(filtros.kngFechaOperacionFin);
				filter.p_TipoOperacion				= 	[{IdTipoOperacion : 'COMPAC'}
														,{IdTipoOperacion : 'VENPAC'}];
				filter.p_Estados					= 	[{IdEstado : 'LIQ'}
														,{IdEstado : 'VIG'}
														,{IdEstado : 'ANT'}
														];
				modelFiltros=filter;
				/*
				 * CELLINCE-1518 antes se llamaba a un servicio API, ahora este es gatillado por .filter
				 */
				 
				/* SVARESGIN-7712 GDIAZ */
				var filtrosActuales = apiGrid.dataSource.filter();
				
				if (filtrosActuales &&
				    filtrosActuales.filters &&
				    filtrosActuales.filters.length > 0) {
				
				    apiGrid.dataSource.filter(filtrosActuales);
				
				} else {
				
				    apiGrid.dataSource.filter({});
				}
				/* SVARESGIN-7712 Fin */
				filtros.seleccionOperacionesMesaRF = undefined;
			},
			
			options : {
				sortable: true,
	      		reorderable: true,
	      		filterable: true,
				groupable: true,
				pageable: true,
				selectable: "multiple, row",
				reorderable: true,
				allowCopy: true,
				resizable: true,
	      		autoBind: false,
	      		scrollable: true,
	      		height:600,
				userSettings: true,
				rowColorBackground	: function(row) {		
					switch(row.get("IdEstado")) {
			             case 'ANT':
			                return "light-green-font";
			                break;			
			             case 'LIQ':
			                return "green";			               		                				                    
			            default:
			            	return "black";
			                break;     
					  }	
				},				
				boldRow: function() {
					return "bold";
				},
				
/*jala nuevo es que se cambio al segmento definition agregando transport.read llamando a una funcion, esto hace que ahora que los filtros en la new Grid funcionen de forma correcta CELLINCE-1518
 * 
 */				
				dataSource: new bcs.DataSource({
		  			type : "call",
		  			definition : {
						serverFiltering : false, 
						serverPaging : true, 
						serverSorting : true, 
						sort: [{ field: "FolioOperacion", dir: "desc" },
						       { field: "FechaOperacion", dir: "desc" }],
						pageSize : 200,
					    transport: {
							read: function(operation) {
								var data = operation.data.data || [];
								
								if(modelFiltros.p_IdEmpresa) {
									bcsServiciosMesa.ConsultarOperacionesMesaRF(modelFiltros,function(result){
										operation.success(result.P_OPERACIONES_OUT);
									},function(result){
										operation.error(result);
									});
								}
								else {
									operation.success(data);	
								}
							}
						},
						schema : obj.getSchemaOperacionesMRF(),
					},
		  		}),
	  			columns: [
				      	 getConfiguracionColumna("FolioOperacion")
				      	,getConfiguracionColumna("IdOperacion")
				      	,getConfiguracionColumna("NombreEstado",{ groupable: true})
				      	,getConfiguracionColumna("Intermesa",{ groupable: true})
				      	,getConfiguracionColumna("NombreMesaOrigen",{ groupable: true})
				      	,getConfiguracionColumna("NombreMesaDestino",{ groupable: true})
				      	,getConfiguracionColumna("NombreOperacion",{ groupable: true})
				        ,getConfiguracionColumna("RazonSocial",{ groupable: true})
				        ,getConfiguracionColumna("DocumentoIdentidad",{ groupable: true})
				        ,getConfiguracionColumna("CodigoPortafolio",{ groupable: true})
				        ,getConfiguracionColumna("Tasa")
				        ,getConfiguracionColumna("PlazoEnDias",{ groupable: true})
				        ,getConfiguracionColumna("MontoOperacionInicialMoneda")
				        ,getConfiguracionColumna("IdTipoReajuste",{ groupable: true})	        
				        ,getConfiguracionColumna("MontoOperacionInicial")
				        ,getConfiguracionColumna("IdMoneda",{ groupable: true})
	  			        ,getConfiguracionColumna("MontoOperacionFinal")
				        ,getConfiguracionColumna("SaldoCaja")
				        ,getConfiguracionColumna("FechaOperacion",{ groupable: true})
				        ,getConfiguracionColumna("FechaVencimiento",{ groupable: true})

						
				        ,getConfiguracionColumna("FechaCondicionLiquidacion",{ groupable: true})
				        ,getConfiguracionColumna("IdAgente",{ groupable: true})
				        ,getConfiguracionColumna("CodigoUsuario",{ groupable: true})
				        ,getConfiguracionColumna("TipoDocumento",{ groupable: true})
				        ,getConfiguracionColumna("Glosa")
						,getConfiguracionColumna("IdOperacionOrigenReasignacion",{ groupable: true})

				],
				
			}
		};
		return config;
	};

	obj.getSchemaOperacionesMRF = function(){
		var vari = {
			model: {
				fields : {
					 "IdOperacion"		: { type: "number"}
					,"FolioOperacion" : { type: "number"}
					,"Intermesa"		: {type: "string"}
					,"FechaOperacion" : { type: "sysdate"}
					//,"IdTipoOperacion" : { type: "string"}
					,"NombreOperacion" : { type: "string"}
					,"IdMesaOrigen"	: { type: "number"}
					,"NombreMesaOrigen"	: { type: "string"}
					,"IdMesaDestino"	: { type: "number"}
					,"NombreMesaDestino"	: { type: "string"}
					,"IdCliente" : { type: "number"}
					,"IdPortafolio" : { type: "number"}
					,"RazonSocial" : { type: "string"}
					,"DocumentoIdentidad"	: { type: "string"}
					,"CodigoPortafolio"	: { type: "string"}
					,"MontoOperacion" : { type: "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}}
					,"MontoOperacionInicial" : { type: "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}}//, excelFormat: "#,##0.00"}
					,"MontoOperacionFinal" : { type: "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}}//, excelFormat: "#,##0.00"}
					,"MontoOperacionMoneda" : { type: "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdTipoReajuste,'','').excelFormat;}}//, excelFormat: "#,##0.00"}
					,"MontoOperacionInicialMoneda" : { type: "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdTipoReajuste,'','').excelFormat;}}//, excelFormat: "#,##0.00"}
					,"MontoOperacionFinalMoneda" : { type: "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdTipoReajuste,'','').excelFormat;}}//, excelFormat: "#,##0.00"}
					,"MontoOperacionFinalAnticipo" : { type: "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}}//, excelFormat: "#,##0.00"}
					,"IdTipoReajuste" : { type: "string"}
					,"Tasa" : { type: "number"}
					,"PlazoEnDias" : { type: "number"}
					,"FechaVencimiento": { type: "sysdate"}
					,"FechaLiquidacion": { type: "sysdate"}
					,"FechaLiquidacionString": { type: "string"}
					,"IdCondicionLiquidacion": { type: "string"}
					,"FechaCondicionLiquidacion": { type: "sysdate"}
					,"IdAgente" : { type: "number"}
					,"IdEstado": { type: "string"}
					,"NombreEstado" : { type: "string"}
					,"FechaCreacion": { type: "sysdate"}
					,"IdUsuarioCreacion" : { type: "number"}
					,"CodigoUsuario" : {type: "string"}
					,"Glosa":  { type: "string"}
					,"IdOperacionOrigenReasignacion": {type: "number"}
					,"IdMoneda" : {type: "string"}
					,"SaldoCaja": { type: "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdTipoReajuste,'','').excelFormat;}}
					,"MontoValorizadoReserva" : { type: "number"}
					,"FechaVencimientoUI" : { type : "string"}
					,"TipoDocumento" : { type : "string"}
					,"ConfirmaAdemdum" : { type : "string"}
					,"EstadoConfirmacion" : { type : "string"}
					,"IdUsuarioConfirma" : { type : "string"}
					,"CodigoUsuarioConfirma" : { type : "string"}
					,"ValorizacionManual": { type : "string"}
					,"Spread": { type: "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}}
				}
			}
		};
		return vari;
	};
	obj.getSchemaFormaPagoMesa = function(){
		var vari = {
			model: {
				fields : {
					"IdFormaDePago"				: {type: "string"}
					,"NombreFormaDePago"		: {type: "string"}
					,"Dias"						: {type: "number"}
					,"PorDefecto"				: {type: "string"}
					,"RecibePagaPortafolioDesc"	: {type: "string"}
					,"IdPreferenciaPago"		: {type: "string"}
					,"IdTipoOperacion"			: {type: "string"}
					,"IdMedioDePago"			: {type: "string"}
					,"IdMoneda"					: {type: "string"}
					,"UsaPersonaRelacionada"	: {type: "string"}
					,"FechaCreacion"			: {type: "sysdate"}
					,"FechaModificacion"		: {type: "sysdate"}
					,"IdUsuarioCreador"			: {type: "number"}
					,"IdEstado"					: {type: "string"}
					,"IdEmpresa"				: {type: "number"}
					,"NombreTipoOperacion"		: {type: "string"}
					,"IdTipoProducto"			: {type: "string"}
					,"NombreTipoProducto"		: {type: "string"}
					,"RecibePagaPortafolio"		: {type: "string"}
					,"NombreMedioDePago"		: {type: "string"}
					,"NombreMoneda"				: {type: "string"}
					,"UsaCuentaBancaria"		: {type: "string"}
					,"UsaTarjetaBancaria"		: {type: "string"}
					,"NombreEstado" 			: {type: "string"}
				}
			}
		};
		return vari;
	};
	
	obj.getConfiguracionFormaPagoMesa = function() {
		var config = {};
		config = {
			buscar : function(apiGrid,filtros) {
				var filter = {};
				filter.SesionIdEmpresa 	= Number(filtros.SesionIdEmpresa);
				filter.IdTipoProducto 	= filtros.oConfiguracionTipoProducto.seleccionado.IdTipoProductoMesa;
				filter.IdEstado			= filtros.oConfiguracionEstado.seleccionado.IdEstado;
				apiGrid.dataSource.filter(obj.validarFiltros(filter,'OPERACION'));
				filtros.seleccionOperacionesMesaRF = undefined;
			},
			
			options : {
				sortable: true,
	      		reorderable: true,
	      		filterable: true,
				groupable: true,
				pageable: true,
				selectable: "multiple, row",
				reorderable: true,
				allowCopy: true,
				resizable: true,
	      		autoBind: false,
	      		scrollable: true,
	      		height:500,
				userSettings: true,
				dataSource: new bcs.DataSource({
		  			type : "odata",
		  			definition : {
						serverFiltering : true, 
						serverPaging : true, 
						serverSorting : true, 
						sort: [{ field: "IdFormaDePago", dir: "desc" },
						       { field: "IdTipoOperacion", dir: "desc" }],
						pageSize : 200,
					    transport: {
							read: {
								url:bcsConsultasMesa('ConsultarFormaDePago'),
								dataType: "json"
							}
						},
						schema : obj.getSchemaFormaPagoMesa()
					},
		  		}),
	  			columns: [
	  				/*
	  				 * "IdFormaDePago"				: {type: "string"}
					,"NombreFormaDePago"		: {type: "string"}
					,"Dias"						: {type: "number"}
					,"PorDefecto"				: {type: "string"}
					,"RecibePagaPortafolioDesc"	: {type: "string"}
					,"IdPreferenciaPago"		: {type: "string"}
					,"IdTipoOperacion"			: {type: "string"}
					,"IdMedioDePago"			: {type: "string"}
					,"IdMoneda"					: {type: "string"}
					,"UsaPersonaRelacionada"	: {type: "string"}
					,"FechaCreacion"			: {type: "sysdate"}
					,"FechaModificacion"		: {type: "sysdate"}
					,"IdUsuarioCreador"			: {type: "number"}
					,"IdEstado"					: {type: "string"}
					,"IdEmpresa"				: {type: "number"}
					,"NombreTipoOperacion"		: {type: "string"}
					,"IdTipoProducto"			: {type: "string"}
					,"NombreTipoProducto"		: {type: "string"}
					,"RecibePagaPortafolio"		: {type: "string"}
					,"NombreMedioDePago"		: {type: "string"}
					,"NombreMoneda"				: {type: "string"}
					,"UsaCuentaBancaria"		: {type: "string"}
					,"UsaTarjetaBancaria"		: {type: "string"}
					,"NombreEstado" 			: {type: "string"}
	  				 */
	  				  	 getConfiguracionColumna("NombreFormaDePago",{ groupable: true})
	  				  	,getConfiguracionColumna("NombreMedioDePago",{ groupable: true})
				      	,getConfiguracionColumna("NombreTipoOperacion",{ groupable: true})
				      	,getConfiguracionColumna("Dias",{ groupable: true})
				      	,getConfiguracionColumna("NombreMoneda",{ groupable: true})
				      	,getConfiguracionColumna("PorDefecto",{ groupable: true})
				      	,getConfiguracionColumna("NombreEstado",{ groupable: true})
				      		  			        
				],
				
			}
		};
		return config;
	};
	
	
	
	
	
	var editorFechaVencimiento = function(container, options){
		console.log(container);
		var fechaSistema = util.dateDS($rootScope.SesionFechaSistema,"FechaSistema","CALL");
		
		var inputDateBcs = $('<input name="'+options.field+'"/>');
		inputDateBcs.appendTo(container);
		inputDateBcs.kendoDatePicker({min: fechaSistema});
		//var inputDateBcs = $('<input name="' + options.field + '" required="required" />');
        
        
	};
	
	// ------------------------------------------------------------
	// Cartera en el ingreso de la operacion
	// ------------------------------------------------------------
	obj.getConfiguracionIngresoOperacionMesaRF = function() {
		var config = {};
		config = {
			/*buscar : function(apiGrid,filtros) {
				apiGrid.dataSource.filter(obj.validarFiltros(filtros,'OPERACION'));
			},
			*/
			options : {
				sortable: true,
	      		reorderable: true,
	      		filterable: true,
				groupable: true,
				pageable: false,
				selectable: "row",
				reorderable: true,
				allowCopy: true,
				resizable: true,
	      		autoBind: false,
	      		scrollable: true,
	      		navigatable: true,
	      		height:390,
	      		userSettings: true,
				//save: function(e) 	{ console.log( e.values); console.log( e.model);},
				dataSource: new bcs.DataSource({
		  			type : "array",
		  			definition : {
		  				sort: [{ field: "IdClasificacionTipoInstrumento", dir: "desc" }],
		  				data : [],
		  				schema : obj.getSchemaCarteraMesa(),
					},
		  		}),
	  			columns: [
	  			         getConfiguracionColumna("FolioCartera"						)
	  			        ,getConfiguracionColumna("IdUsuarioReserva"					) 
	  			        ,getConfiguracionColumna("IdTipoInstrumento"				,{ groupable: true})//familia
						,getConfiguracionColumna("IdClasificacionTipoInstrumento"	,{ groupable: true})//Mercado	  			        
						//,getConfiguracionColumna("ClasificacionTipoInstrumento"		,{ groupable: true})//Mercado						
						,getConfiguracionColumna("Nemotecnico"						,{ groupable: true})//newmotecnico
						,getConfiguracionColumna("IdEmisor"							,{ groupable: true})//Emisor		
						,getConfiguracionColumna("IdTipoReajuste"					,{ groupable: true})//tipo reajuste
						//,getConfiguracionColumna("Moneda"							,{ groupable: true})//Moneda de Emision
						,getConfiguracionColumna("IdMonedaInstrumento"				,{ groupable: true})//MonedaLiquidacion						
						,getConfiguracionColumna("INGCantidad"    					    ,{ groupable: true})//Cantidad
						,getConfiguracionColumna("INGMontoATasaCompraActual"		,{ groupable: true})//Monto Cartera
						,getConfiguracionColumna("INGMontoCompraActualMonedaInstrumento")//Valor Presente
						
						,getConfiguracionColumna("INGValorRescate"					,{ groupable: true})//valor rescate
						,getConfiguracionColumna("EsFungible"						,{ groupable: true})//fungible
						,getConfiguracionColumna("FechaVencimientoInstrumento"		,{ groupable: true})//fecha vencimiento						
						,getConfiguracionColumna("IdBoveda"							,{ groupable: true})//Boveda
						,getConfiguracionColumna("CarteraPacto"						,{ groupable: true})//CarteraPacto
						,getConfiguracionColumna("CodigoDescIFRS"						,{ groupable: true})//CodigoIFRS
						,getConfiguracionColumna("ValorClasificacionRiesgo"			,{ groupable: true})						
						,getConfiguracionColumna("CorteMinimo"						)//numero de cortes
						,getConfiguracionColumna("INGTasaCompra"					,{ groupable: true})//INGTasaCompra
						,getConfiguracionColumna("INGTasaMercadoActual"				,{ groupable: true})//tasaIngresoOperacion
						,getConfiguracionColumna("INGPrecioMercadoActual"			,{ groupable: true})//PrecioIngresoOperacion
						,getConfiguracionColumna("TipoCarteraCorredor"			    ,{ groupable: true})//PrecioIngresoOperacion
				],
				
			}
		};
		return config;
	};
	obj.getConfiguracionIngresoOperacionMesaRFJava = function() {
		var config = {};
		config = {
			
			options : {
				sortable: true,
	      		reorderable: true,
	      		filterable: true,
				groupable: true,
				pageable: false,
				selectable: "row",
				reorderable: true,
				allowCopy: true,
				resizable: true,
	      		autoBind: false,
	      		scrollable: true,
	      		navigatable: true,
	      		height:390,
	      		userSettings: true,
				//save: function(e) 	{ console.log( e.values); console.log( e.model);},
				dataSource: new bcs.DataSource({
		  			type : "call",
		  			definition : {
		  				sort: [{ field: "IdClasificacionTipoInstrumento", dir: "desc" }],
		  				data : [],
		  				schema : obj.getSchemaCarteraMesa(),
					},
		  		}),
	  			columns: [
	  			         getConfiguracionColumna("FolioCartera"						)
	  			        ,getConfiguracionColumna("IdUsuarioReserva"					) 
	  			        ,getConfiguracionColumna("IdTipoInstrumento"				,{ groupable: true})//familia
						,getConfiguracionColumna("IdClasificacionTipoInstrumento"	,{ groupable: true})//Mercado	  			        
						//,getConfiguracionColumna("ClasificacionTipoInstrumento"		,{ groupable: true})//Mercado						
						,getConfiguracionColumna("Nemotecnico"						,{ groupable: true})//newmotecnico
						,getConfiguracionColumna("IdEmisor"							,{ groupable: true})//Emisor		
						,getConfiguracionColumna("IdTipoReajuste"					,{ groupable: true})//tipo reajuste
						//,getConfiguracionColumna("Moneda"							,{ groupable: true})//Moneda de Emision
						,getConfiguracionColumna("IdMonedaInstrumento"				,{ groupable: true})//MonedaLiquidacion						
						,getConfiguracionColumna("INGCantidad"    					    ,{ groupable: true})//Cantidad
						,getConfiguracionColumna("INGMontoATasaCompraActual"		,{ groupable: true})//Monto Cartera
						,getConfiguracionColumna("INGMontoCompraActualMonedaInstrumento")//Valor Presente
						
						,getConfiguracionColumna("INGValorRescate"					,{ groupable: true})//valor rescate
						,getConfiguracionColumna("EsFungible"						,{ groupable: true})//fungible
						,getConfiguracionColumna("FechaVencimientoInstrumento"		,{ groupable: true})//fecha vencimiento						
						,getConfiguracionColumna("IdBoveda"							,{ groupable: true})//Boveda
						,getConfiguracionColumna("CarteraPacto"						,{ groupable: true})//CarteraPacto
						,getConfiguracionColumna("CodigoDescIFRS"						,{ groupable: true})//CodigoIFRS
						,getConfiguracionColumna("ValorClasificacionRiesgo"			,{ groupable: true})						
						,getConfiguracionColumna("CorteMinimo"						)//numero de cortes
						,getConfiguracionColumna("INGTasaCompra"					,{ groupable: true})//INGTasaCompra
						,getConfiguracionColumna("INGTasaMercadoActual"				,{ groupable: true})//tasaIngresoOperacion
						,getConfiguracionColumna("INGPrecioMercadoActual"			,{ groupable: true})//PrecioIngresoOperacion
						,getConfiguracionColumna("TipoCarteraCorredor"			    ,{ groupable: true})//PrecioIngresoOperacion
				],
				
			}
		};
		return config;
	};

	/*
	 *  GRILLA DE INGRESO DE OPERACION MRF
	 * */
	obj.getConfiguracionCarteraEnIngreso = function() {
		var config = {};
		config = {
			/*buscar : function(apiGrid,filtros) {
				apiGrid.dataSource.filter(obj.validarFiltros(filtros,'OPERACION'));
			},
			*/
			options : {
				sortable: true,
	      		reorderable: true,
	      		filterable: true,
				groupable: true,
				pageable: false,
				selectable: "row",
				reorderable: true,
				allowCopy: true,
				resizable: true,
	      		autoBind: false,
	      		scrollable: true,
	      		navigatable: true,
	      		height:390,
	      		userSettings: true,
				//save: function(e) 	{ console.log( e.values); console.log( e.model);},
				dataSource: new bcs.DataSource({
		  			type : "array",
		  			definition : {
		  				sort: [{ field: "IdClasificacionTipoInstrumento", dir: "desc" }],
		  				data : [],
		  				schema : obj.getSchemaCarteraMesa(),
					},
		  		}),
	  			columns: [
	  			         getConfiguracionColumna("FolioCartera"						)
	  			        ,getConfiguracionColumna("IdTipoInstrumento"				,{ groupable: true})//familia
						,getConfiguracionColumna("IdClasificacionTipoInstrumento"	,{ groupable: true})//Mercado	  			        
						,getConfiguracionColumna("Nemotecnico"						,{ groupable: true})//newmotecnico
						,getConfiguracionColumna("IdEmisor"							,{ groupable: true})//Emisor		
						,getConfiguracionColumna("IdTipoReajuste"					,{ groupable: true})//tipo reajuste
						//,getConfiguracionColumna("Moneda"					,{ groupable: true})//Moneda de Emision
						,getConfiguracionColumna("MonedaInstrumento"				,{ groupable: true})//MonedaLiquidacion
						,getConfiguracionColumna("FechaVencimientoInstrumento"      ,{ groupable: true})//fecha 
						,getConfiguracionColumna("Nominal"							,{ groupable: true})//Cantidad
						//,getConfiguracionColumna("INGMontoATasaCompraActual"		,{ groupable: true})//Monto Cartera
						,getConfiguracionColumna("INGTasaCompra"					,{ groupable: true})//INGTasaCompra
						,getConfiguracionColumna("INGPrecioCompraActual"			,{ groupable: true})//INGTasaCompra
						,getConfiguracionColumna("Monto"							,{ groupable: true,template_ : " <label> #:  (util.bcs_formato(Cantidad,'GEN','Monto','CLP',IdTipoInstrumento,'')).valor #</label>",
						format : function(value, dataItem) {
							return util.bcs_formato(value,'GEN','Monto','CLP',dataItem.IdTipoInstrumento,'');
						}})//valor rescate
						,getConfiguracionColumna("MontoLiquidacionOperacion"		,{ groupable: true})//valor rescate moneda liquidacion
						,getConfiguracionColumna("TipoCarteraCorredor"              ,{ groupable: true})
						,getConfiguracionColumna("INGMontoAPlazoDetalle"   			,{ groupable: true})
						,getConfiguracionColumna("INGMontoAPlazoMonedaInstrumentoDetalle",{ groupable: true})
						//,getConfiguracionColumna("EsFungible"						,{ groupable: true})//fungible
						//,getConfiguracionColumna("FechaVencimientoInstrumento"		,{ groupable: true})//fecha vencimiento						
						//,getConfiguracionColumna("IdBoveda"							,{ groupable: true})//Boveda
						//,getConfiguracionColumna("CarteraPacto"						,{ groupable: true})//CarteraPacto
						//,getConfiguracionColumna("CodigoIFRS"						,{ groupable: true})//CodigoIFRS
						//,getConfiguracionColumna("CorteMinimo"						)//numero de cortes
						//,getConfiguracionColumna("INGTasaCompra"					,{ groupable: true})//INGTasaCompra
						//,getConfiguracionColumna("INGTasaMercadoActual"				,{ groupable: true})//tasaIngresoOperacion
						//,getConfiguracionColumna("INGPrecioMercadoActual"			,{ groupable: true})//PrecioIngresoOperacion
						
				],
				
			}
		};
		return config;
	};
	
	obj.getSchemaConsultaReprocesarDatatec = function(){
		return {
			model: {
				fields : {
					  "IdUnicoDatatec"			: { type: "number"}
					, "NumeroRegistro"			: { type: "number"}
					, "NumeroIntentos"			: { type: "number"}
					, "IdEstadoRegistro"		: { type: "string"}
					, "GlosaProceso"			: { type: "string"}
					, "IdEmpresa"				: { type: "number"}
					, "IdOperacionSpot"			: { type: "number"}
					, "FechaOperacion"			: { type: "sysdate"}
					, "IdCliente"				: { type: "number"}
					, "IdPortafolio"			: { type: "number"}
					, "NombreCliente"			: { type: "string"}
					, "IdInstrumento"			: { type: "number"}
					, "IdTipoOperacion"			: { type: "string"}
					, "IdMonedaPrincipal"		: { type: "string"}
					, "IdMonedaSecundaria"		: { type: "string"}
					, "IdMonedaLocal"			: { type: "string"}
					, "MontoMonedaPrincipal"	: { type: "number"}
					, "TipoCambioCierre"		: { type: "number"}
					, "MontoMonedaSecundaria"	: { type: "number"}
					, "TipoCambioCosto"			: { type: "number"}
					, "IdFormaPagoRecibo"		: { type: "string"}
					, "FechaValutaRecibo"		: { type: "sysdate"}
					, "IdFormaPagoEntrego"		: { type: "string"}
					, "FechaValutaEntrego"		: { type: "sysdate"}
					, "Origen"					: { type: "string"}
					, "IdUsuarioCreador"		: { type: "number"}
					, "FechaCreacion"			: { type: "sysdate"}
				}
			}
		};
	};
	
	/*
	obj.getConfiguracionFormaDePago = function() {
		var config = {};
		config = {
			/*buscar : function(apiGrid,filtros) {
				apiGrid.dataSource.filter(obj.validarFiltros(filtros,'OPERACION'));
			},
			* /
			options : {
				sortable: true,
	      		reorderable: true,
	      		filterable: true,
				groupable: true,
				pageable: false,
				selectable: "row",
				reorderable: true,
				allowCopy: true,
				resizable: true,
	      		autoBind: false,
	      		scrollable: true,
	      		navigatable: true,
	      		height:390,
	      		userSettings: true,
	      		rowColorBackground  : function(row) {
                 if (row.FormaDePago === 'ENTREGAMOS')
                 	return "red";
                 else
                	return "green";
                 
                 return "";
                },
				//save: function(e) 	{ console.log( e.values); console.log( e.model);},
				dataSource: new bcs.DataSource({
		  			type : "array",
		  			definition : {
		  				
		  				sort: [{ field: "FormaDePago", dir: "asc" }],
		  				data : [],
		  				schema : obj.getSchemaFormaDePago(),
		  				
					},
		  		}),
	  			columns: [
	  			         getConfiguracionColumna("FormaDePago"		,{ groupable: true})
	  			        ,getConfiguracionColumna("NombreMedioDePago",{ groupable: true})
						,getConfiguracionColumna("IdMoneda"			,{ groupable: true})
						,getConfiguracionColumna("Monto"			,{ groupable: true})
						,getConfiguracionColumna("FechaPagoMedio"	,{ groupable: true})
						
				],
				
			}
		};
		return config;
	};*/
	
	return obj;
});