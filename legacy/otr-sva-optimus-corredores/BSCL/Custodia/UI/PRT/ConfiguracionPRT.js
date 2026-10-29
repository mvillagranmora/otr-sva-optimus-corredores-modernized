
angular.module('ModuloCustodia').factory('bcsConfiguracionPRT',['$translate','$rootScope',function($translate,$rootScope) {
	var obj = {};
	
	//Configuración de schema para grilla Prestamos 
	obj.getSchemaPrestamos = function(){
		return {
			model: {
				fields : {
					FolioPrestamo : { type: "number"},
					NombreEstado : { type: "string"},
					NombreTipoPrestamo : { type: "string"},
					FechaPrestamo : { type: "sysdate"},
					FechaVencimientoPrestamo : { type: "sysdate"},
					IdCondicionLiquidacion : {type: "string"},
					FechaLiquidacion : { type: "sysdate"},
					PlazoEnDiasOperacion : { type: "number"},
					IdClasificacionTipoInstrumentoSubyacente : {type: "string"},
					NemoSubyacente : { type: "string"},
					DocumentoIdentidad: { type: "string"},
					CodigoPortafolio : { type: "number"},
					RazonSocial : { type: "string"},
					CantidadPorAsignar: { type: "number"},
					INGCantidad	: { type: "number"},
					INGMontoATasaCompraActual	: { type: "number"},
					INGMontoAPlazo	: { type: "number"},
					INGMontoPremio : { type: "number"},
					INGPrecioInstrumento : { type: "number"},
					INGTasaPrestamo: { type: "number"},
					INGMontoPrima: { type: "number"},
					InteresAcumulado : { type: "number"},
					InteresDiario : {type: "number"},
					FolioComprobante : { type: "number"},
					FolioSistemaExterno : { type: "string"},

					//Historico
					EstadoLiquidacion : { type: "string"},
					InteresCorrido : { type: "number"},
					EGRFechaTermino : {type: "sysdate"},
					EGRCantidad : { type: "number"},
					EGRPrecio : { type: "number"},
					EGRTasa : { type: "number"},
					EGRMonto : { type: "number"},
					EGRMontoPrima : { type: "number"},
					EGRMontoAPlazo : { type: "number"}
				}
			}
		};
	};
	
	obj.getSchemaEmprestitos = function(){
		return {
			model: {
				fields : {
					FolioEmprestito  			: { type: "number"},
					FechaEmprestito	     		: { type: "sysdate"},
					FechaVencimientoEmprestito	: { type: "sysdate"},
					FolioPrestamo 				: { type: "number"},
					NombreEstado 				: { type: "string"},
					NombreTipoPrestamo 			: { type: "string"},
					FechaPrestamo 				: { type: "sysdate"},
					FechaVencimientoPrestamo 	: { type: "sysdate"},
					IdCondicionLiquidacion 		: {type: "string"},
					FechaLiquidacion 			: { type: "sysdate"},
					PlazoEnDiasOperacion 		: { type: "number"},
					IdClasificacionTipoInstrumentoSubyacente : {type: "string"},
					NemoSubyacente 				: { type: "string"},
					DocumentoIdentidad			: { type: "string"},
					CodigoPortafolio 			: { type: "number"},
					RazonSocial 				: { type: "string"},
					CantidadPorAsignar			: { type: "number"},
					INGCantidad					: { type: "number"},
					INGMontoATasaCompraActual	: { type: "number"},
					INGMontoAPlazo				: { type: "number"},
					INGMontoPremio 				: { type: "number"},
					INGPrecioInstrumento 		: { type: "number"},
					INGTasaPrestamo				: { type: "number"},
					INGMontoPrima				: { type: "number"},
					FolioComprobante 			: { type: "number"},
					FolioSistemaExterno 		: { type: "string"},
					INGTasaEmprestito			: { type: "number"},
					INGMontoPremio				: { type: "number"},
					InteresCorrido 				: { type: "number"},
					InteresDiario 				: {type: "number"},

					//HISTORICO
					InteresAcumulado 			: { type: "number"},
					EGRCantidad 				: { type: "number"},
					EGRMonto					: { type: "number"},
					EGRMontoPremio				: { type: "number"},
					EGRMontoAPlazo				: { type: "number"},
					EGRFechaTermino 			: {type: "sysdate"},
					EGRPrecio 					: { type: "number"},
					EGRTasa 					: { type: "number"},
				}
			}
		};
	};
	
	obj.getSchemaAnticipos = function(){
		return {
			model: {
				fields : {
					FolioEmprestito : { type: "number"},
					FolioPrestamo : { type: "number"},
					IdEstado : { type: "string"},
					NombreEstado : { type: "string"},
					DocumentoIdentidad: { type: "string"},
					CodigoPortafolio : { type: "number"},
					RazonSocial : { type: "string"},
					PlazoEnDiasOperacion : { type: "number"},
					EGRCantidad : { type: "number"},
					EGRPrecio : { type: "number"},
					EGRMonto : { type: "number"},
					EGRTasa: { type: "number"},
					EGRPrecioAPlazo: { type: "number"},
					EGRMontoAPlazo: { type: "number"}
				}
			}
		};
	};
	
	obj.fnColorBk = function(row) {
        var color = "";
		
        if (row.IdEstado == 'ANT'){
        	color = "black";
        }else if ( Number(row.CantidadPorAsignar) > 0 && Number(row.CantidadPorAsignar) ==  Number(row.Cantidad)){
			color = "red";
		}else if(Number(row.CantidadPorAsignar) > 0 && Number(row.CantidadPorAsignar) !=  Number(row.Cantidad)) {
			color = "yellow";
		}else if(Number(row.CantidadPorAsignar) == 0){
			color = "green";
		}

        return color;
  	};
	
	
	var configuracionColumnas = {
			FolioPrestamo  	 			:{field: "FolioPrestamo",width: "66px",format:"{0:n0}",attributes: {style:"text-align:right;"}},
			NombreEstado  	 			:{field: "NombreEstado",width: "55px",attributes: {style:"text-align:left;"}, groupable: true},
			NombreTipoPrestamo 			:{field: "NombreTipoPrestamo",width: "100px",attributes: {style:"text-align:left; white-space:nowrap"}, groupable: true},
			FechaPrestamo	    		:{field: "FechaPrestamo",width: "70px",attributes: {style:"text-align:center;"}, groupable: true},
			IdCondicionLiquidacion 		:{field: "IdCondicionLiquidacion",width: "50px", groupable: true},
			FechaLiquidacion			:{field: "FechaLiquidacion",width: "75px", groupable: true},
			FechaVencimientoPrestamo	:{field: "FechaVencimientoPrestamo",width: "75px", groupable: true},
			PlazoEnDiasOperacion		:{field: "PlazoEnDiasOperacion",width: "75px",attributes: {style:"text-align:right;"}},
			IdClasificacionTipoInstrumentoSubyacente: {field: "IdClasificacionTipoInstrumentoSubyacente",width: "50px", groupable: true},
			NemoSubyacente				:{field: "NemoSubyacente",width: "85px",attributes: {style:"text-align:left;"}, groupable: true},
			DocumentoIdentidad			:{field: "DocumentoIdentidad",width: "75px",attributes: {style:"text-align:right;"}, groupable: true},
			CodigoPortafolio			:{field: "CodigoPortafolio",width: "55px",attributes: {style:"text-align:right;"}, groupable: true},
			RazonSocial		 			:{field: "RazonSocial",width: "150px",attributes: {style:"text-align:left; white-space:nowrap;"}, groupable: true},
			CantidadPorAsignar			:{field: "CantidadPorAsignar",width: "80px",attributes: {style:"text-align:right;"}},
			INGCantidad					:{field: "INGCantidad",width: "80px",attributes: {style:"text-align:right;"}},
			INGPrecioInstrumento		:{field: "INGPrecioInstrumento",width: "100px",attributes: {style:"text-align:right;"}},
			INGTasaPrestamo				:{field: "INGTasaPrestamo",width: "75px",attributes: {style:"text-align:right;"}},
			INGMontoATasaCompraActual	:{field: "INGMontoATasaCompraActual",width: "100px",attributes: {style:"text-align:right;"}},
			PrecioCompraInicial	    	:{field: "PrecioCompraInicial",width: "100px",attributes: {style:"text-align:right;"}},
			MontoCompraInicial		    :{field: "MontoCompraInicial",width: "100px",attributes: {style:"text-align:right;"}},
			INGMontoAPlazo				:{field: "INGMontoAPlazo",width: "100px",attributes: {style:"text-align:right;"}},
			INGMontoPrima				:{field: "INGMontoPrima",width: "100px",attributes: {style:"text-align:right;"}},
			FolioComprobante  			:{field: "FolioComprobante",width: "66px",format:"{0:n0}",attributes: {style:"text-align:right;"}},
			FolioSistemaExterno			:{field: "FolioSistemaExterno",width: "100px",attributes: {style:"text-align:right;"}},
			InteresDiario				:{field: "InteresDiario",width: "100px",attributes: {style:"text-align:right;"}},
			InteresAcumulado			:{field: "InteresAcumulado",width: "100px",attributes: {style:"text-align:right;"}},

			//EMPRESTITO
			FolioEmprestito  			:{field: "FolioEmprestito",width: "66px",format:"{0:n0}",attributes: {style:"text-align:right;"}},
			FechaEmprestito	     		:{field: "FechaEmprestito",width: "75px", groupable: true},
			FechaVencimientoEmprestito	:{field: "FechaVencimientoEmprestito",width: "75px", groupable: true},
			INGTasaEmprestito			:{field: "INGTasaEmprestito",width: "75px",attributes: {style:"text-align:right;"}},
			INGMontoPremio				:{field: "INGMontoPrima",width: "100px",attributes: {style:"text-align:right;"}},			

			//HISTORICO
			EstadoLiquidacion	:{field: "EstadoLiquidacion",width: "100px",attributes: {style:"text-align:right;"}},
			InteresCorrido		:{field: "InteresCorrido",width: "100px",attributes: {style:"text-align:right;"}},
			EGRFechaTermino 	:{field: "EGRFechaTermino",width: "75px", groupable: true},
			EGRCantidad			:{field: "EGRCantidad",width: "80px",attributes: {style:"text-align:right;"}},
			EGRPrecio			:{field: "EGRPrecio",width: "75px",attributes: {style:"text-align:right;"}},
			EGRTasa				:{field: "EGRTasa",width: "75px",attributes: {style:"text-align:right;"}},
			EGRMonto			:{field: "EGRMonto",width: "125px",attributes: {style:"text-align:right;"}},
			EGRMontoPrima		:{field: "EGRMontoPrima",width: "125px", groupable: true},
			EGRMontoPremio		:{field: "EGRMontoPremio",width: "125px", groupable: true},
			EGRMontoAPlazo		:{field: "EGRMontoAPlazo",width: "125px",attributes: {style:"text-align:right;"}},

			//OTROS
			IdEstado  	 		:{field: "IdEstado",width: "50px",attributes: {style:"text-align:left;"}, groupable: true},
			DescripcionEstado  	:{field: "DescripcionEstado",width: "100px",attributes: {style:"text-align:left;"}, groupable: true},
			IdTipoPrestamo    	:{field: "IdTipoPrestamo",width: "75px",attributes: {style:"text-align:left;"}, groupable: true},
			NemoPrestamo        :{field: "NemotecnicoPrestamo",width: "110px",attributes: {style:"text-align:left;"}, groupable: true},
			IdBolsa				:{field: "IdBolsa",width: "75px",attributes: {style:"text-align:left;"}, groupable: true},
			NombreBolsa			:{field: "NombreBolsa",width: "150px",attributes: {style:"text-align:left;"}, groupable: true},
			DiasRestantes     	:{field: "DiasRestantes",width: "40px",format:"{0:n0}",attributes: {style:"text-align:right;"}},
			INGFechaLiquidacionPrestamo	:{field: "INGFechaLiquidacionPrestamo",width: "75px", groupable: true},
			INGPrecio			:{field: "PrecioInstrumento",width: "75px",attributes: {style:"text-align:right;"}},
			INGMonto			:{field: "MontoInstrumento",width: "125px",attributes: {style:"text-align:right;"}},
			INGMontoPremio		:{field: "INGMontoPremio",width: "100px",attributes: {style:"text-align:right;"}},
			INGMontoTotalEmprestito:{field: "INGMontoTotalEmprestito",width: "100px",attributes: {style:"text-align:right;"}},
			EGRPremio			:{field: "EGRPremio",width: "75px",attributes: {style:"text-align:right;"}},
			EGRPrecioAPlazo		:{field: "EGRPrecioAPlazo",width: "125px",attributes: {style:"text-align:right;"}},
			INGMontoInteresCorridoCompraActual	:{field: "INGMontoInteresCorridoCompraActual",width: "125px",attributes: {style:"text-align:right;"}},
			INGMontoInteresCompraProximo	:{field: "INGMontoInteresCompraProximo",width: "125px",attributes: {style:"text-align:right;"}},
			EGRMontoAnticipo 	:{field: "EGRMontoAnticipo",width: "125px",attributes: {style:"text-align:right;"}},
			EGRFechaAnticipo 	:{field: "EGRFechaAnticipo",width: "75px", groupable: true},
			EstadoAnticipo 	    :{field: "EstadoAnticipo",width: "100px",attributes: {style:"text-align:right;"}},
			ExigirGarantia    :{field: "ExigirGarantia",width: "100px",attributes: {style:"text-align:right;"}},
			FechaEmisionInstrumento 	:{field: "FechaLiquidacion",width: "75px", groupable: true},
		};

	
	var getConfiguracionColumna = function(campo,extras) {
		return _.extend( {}, configuracionColumnas[campo], extras);
	};
	
	
	obj.columnasPrestamos = function() {
		return [
				   getConfiguracionColumna("FolioPrestamo",{title: $translate.instant('UI_FOLIO')})
				  ,getConfiguracionColumna("NombreEstado",{title: $translate.instant('UI_ESTADO')})
				  ,getConfiguracionColumna("NombreTipoPrestamo",{title: $translate.instant('UI_TIPO_PRESTAMO')})
				  ,getConfiguracionColumna("FechaPrestamo",{title: $translate.instant('UI_FECHA_OPERACION')})
				  ,getConfiguracionColumna("IdCondicionLiquidacion",{title: $translate.instant('UI_CONDICION_LIQUIDACION')})
				  ,getConfiguracionColumna("FechaLiquidacion",{title: $translate.instant('UI_FECHA_LIQUIDACION')})
				  ,getConfiguracionColumna("FechaVencimientoPrestamo",{title: $translate.instant('UI_FECHA_VENCIMIENTO')})
				  ,getConfiguracionColumna("PlazoEnDiasOperacion",{title: $translate.instant('UI_PLAZO')})
				  ,getConfiguracionColumna("IdClasificacionTipoInstrumentoSubyacente",{title: $translate.instant('UI_MERCADO')})
				  ,getConfiguracionColumna("NemoSubyacente",{title: $translate.instant('UI_INSTRUMENTO')})
				  ,getConfiguracionColumna("DocumentoIdentidad",{title: $translate.instant('UI_DOCUMENTO_IDENTIDAD')})
				  ,getConfiguracionColumna("CodigoPortafolio",{title: $translate.instant('UI_PORTAFOLIO')})
				  ,getConfiguracionColumna("RazonSocial",{title: $translate.instant('UI_PRESTATARIO')})
				  ,getConfiguracionColumna("CantidadPorAsignar",{title: $translate.instant('UI_POR_ASIGNAR'), template : " <label> #:  (util.bcs_formato(CantidadPorAsignar,'CUS','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"})
				  ,getConfiguracionColumna("INGCantidad",{title: $translate.instant('UI_CANTIDAD'), template : " <label> #:  (util.bcs_formato(INGCantidad,'CUS','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"})
				  ,getConfiguracionColumna("INGPrecioInstrumento",{title: $translate.instant('UI_PRECIO_PRESTAMO'), template : " <label> #:  (util.bcs_formato(INGPrecioInstrumento,'CUS','Precio','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"})
				  ,getConfiguracionColumna("INGMontoATasaCompraActual",{title: $translate.instant('UI_MONTO_PRESTAMO'), template : " <label> #:  (util.bcs_formato(INGMontoATasaCompraActual,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"})
				  ,getConfiguracionColumna("PrecioCompraInicial",{title: $translate.instant('UI_PRECIO_COMPRA_INICIAL'), template : kendo.template("#if(PrecioCompraInicial == 0){# #: (util.bcs_formato(INGPrecioInstrumento,'CUS','Precio','" + $rootScope.SesionIdMonedaSistema + "','','')).valor # #}else{# #: (util.bcs_formato(PrecioCompraInicial,'CUS','Precio','" + $rootScope.SesionIdMonedaSistema + "','','')).valor # #}#") })
				  ,getConfiguracionColumna("MontoCompraInicial",{title: $translate.instant('UI_MONTO_COMPRA_INICIAL'), template : kendo.template("#if(PrecioCompraInicial == 0){# #: (util.bcs_formato(INGMontoATasaCompraActual,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor # #}else{# #: (util.bcs_formato(MontoCompraInicial,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor # #}#") })
				  ,getConfiguracionColumna("INGTasaPrestamo",{title: $translate.instant('UI_PORCENTAJE_PRIMA_ABR'), template : " <label> #:  (util.bcs_formato(INGTasaPrestamo,'CUS','Tasa','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"})
				  ,getConfiguracionColumna("INGMontoPrima",{title: $translate.instant('UI_PRIMA'), template : " <label> #:  (util.bcs_formato(INGMontoPrima,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"})
				  ,getConfiguracionColumna("INGMontoAPlazo",{title: $translate.instant('UI_MONTO_PLAZO'), template : " <label> #:  (util.bcs_formato(INGMontoAPlazo,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"})
				  ,getConfiguracionColumna("InteresDiario",{title: $translate.instant('UI_INTERES_DIARIO'),    template : " <label> #:  (util.bcs_formato(InteresDiario,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"}) 
				  ,getConfiguracionColumna("InteresAcumulado",{title: $translate.instant('UI_INTERES_ACUMULADO'), template : " <label> #:  (util.bcs_formato(InteresAcumulado,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"})					
				  ,getConfiguracionColumna("FolioComprobante",{title: $translate.instant('UI_COMPROBANTE')})		
				  ,getConfiguracionColumna("FolioSistemaExterno",{width:"150px", title: $translate.instant('UI_FOLIO_BLOCKCHAIN')})
			];
  	};
  
  	obj.columnasPrestamosHistoricos = function(agrupa) {
		return [
			   getConfiguracionColumna("FolioPrestamo",{groupable:agrupa,title: $translate.instant('UI_FOLIO')})
			  ,getConfiguracionColumna("NombreEstado",{groupable:agrupa,title: $translate.instant('UI_ESTADO')})
			  ,getConfiguracionColumna("NombreTipoPrestamo",{groupable:agrupa,title: $translate.instant('UI_TIPO_PRESTAMO')})
			  ,getConfiguracionColumna("FechaPrestamo",{groupable:agrupa,title: $translate.instant('UI_FECHA_OPERACION')})
			  ,getConfiguracionColumna("FechaLiquidacion",{title: $translate.instant('UI_FECHA_LIQUIDACION')})
			  ,getConfiguracionColumna("FechaVencimientoPrestamo",{groupable:agrupa,title: $translate.instant('UI_FECHA_VENCIMIENTO')})
			  ,getConfiguracionColumna("EGRFechaTermino",{title: $translate.instant('UI_FECHA_TERMINO')})
			  ,getConfiguracionColumna("IdClasificacionTipoInstrumentoSubyacente",{title: $translate.instant('UI_MERCADO')})
			  ,getConfiguracionColumna("NemoSubyacente",{groupable:agrupa,title: $translate.instant('UI_INSTRUMENTO')})
			  ,getConfiguracionColumna("DocumentoIdentidad",{groupable:agrupa,title: $translate.instant('UI_DOCUMENTO_IDENTIDAD')})
			  ,getConfiguracionColumna("CodigoPortafolio",{groupable:agrupa,title: $translate.instant('UI_PORTAFOLIO')})
			  ,getConfiguracionColumna("RazonSocial",{groupable:agrupa,title: $translate.instant('UI_PRESTATARIO')})
			  ,getConfiguracionColumna("EGRCantidad",{groupable:agrupa,title: $translate.instant('UI_CANTIDAD'), template : " <label> #:  (util.bcs_formato(EGRCantidad,'CUS','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"})
			  ,getConfiguracionColumna("EGRPrecio",{groupable:agrupa,title: $translate.instant('UI_PRECIO_PRESTAMO'), template : " <label> #:  (util.bcs_formato(EGRPrecio,'CUS','Precio','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"})
			  ,getConfiguracionColumna("EGRMonto",{groupable:agrupa,title: $translate.instant('UI_MONTO_PRESTAMO'), template : " <label> #:  (util.bcs_formato(EGRMonto,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"})
			  ,getConfiguracionColumna("PrecioCompraInicial",{title: $translate.instant('UI_PRECIO_COMPRA_INICIAL'), template : kendo.template("#if(PrecioCompraInicial == 0){# #: (util.bcs_formato(INGPrecioInstrumento,'CUS','Precio','" + $rootScope.SesionIdMonedaSistema + "','','')).valor # #}else{# #: (util.bcs_formato(PrecioCompraInicial,'CUS','Precio','" + $rootScope.SesionIdMonedaSistema + "','','')).valor # #}#") })
			  ,getConfiguracionColumna("MontoCompraInicial",{title: $translate.instant('UI_MONTO_COMPRA_INICIAL'), template : kendo.template("#if(PrecioCompraInicial == 0){# #: (util.bcs_formato(INGMontoATasaCompraActual,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor # #}else{# #: (util.bcs_formato(MontoCompraInicial,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor # #}#") })
			  ,getConfiguracionColumna("EGRTasa",{groupable:agrupa,title: $translate.instant('UI_PORCENTAJE_PRIMA_ABR'), template : " <label> #:  (util.bcs_formato(EGRTasa,'CUS','Tasa','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"})
			  ,getConfiguracionColumna("EGRMontoPrima",{groupable:agrupa,title: $translate.instant('UI_PRIMA'), template : " <label> #:  (util.bcs_formato(EGRMontoPrima,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"})
			  ,getConfiguracionColumna("EGRMontoAPlazo",{groupable:agrupa,title: $translate.instant('UI_MONTO_PLAZO'), template : " <label> #:  (util.bcs_formato(EGRMontoAPlazo,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"})
			  ,getConfiguracionColumna("InteresCorrido",{title: $translate.instant('UI_INTERESES_CORRIDOS'), template : " <label> #:  (util.bcs_formato(InteresCorrido,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"})					
			  ,getConfiguracionColumna("FolioComprobante",{title: $translate.instant('UI_COMPROBANTE')})
			  ,getConfiguracionColumna("EstadoLiquidacion",{title: $translate.instant('UI_ESTADO_BLOCKCHAIN')})
			  ,getConfiguracionColumna("FolioSistemaExterno",{width:"150px", title: $translate.instant('UI_FOLIO_BLOCKCHAIN')})
		];
	};
  
	obj.columnasEmprestitos = function(agrupa) {
		return [
				getConfiguracionColumna("FolioEmprestito"					,{width:"75px", groupable:agrupa, title: $translate.instant('UI_FOLIO')})
				,getConfiguracionColumna("FolioPrestamo"					,{width:"75px", groupable:agrupa, title: $translate.instant('UI_PRESTAMO')})
				,getConfiguracionColumna("NombreEstado"						,{groupable:agrupa, title: $translate.instant('UI_ESTADO')})
				,getConfiguracionColumna("NombreTipoPrestamo"				,{title: $translate.instant('UI_TIPO_PRESTAMO')})
				,getConfiguracionColumna("FechaEmprestito"	            	,{groupable:agrupa, title: $translate.instant('UI_FECHA_OPERACION')})
				,getConfiguracionColumna("IdCondicionLiquidacion"			,{title: $translate.instant('UI_CONDICION_LIQUIDACION')})
				,getConfiguracionColumna("FechaLiquidacion"					,{title: $translate.instant('UI_FECHA_LIQUIDACION')})
				,getConfiguracionColumna("FechaVencimientoEmprestito"		,{groupable:agrupa, title: $translate.instant('UI_FECHA_VENCIMIENTO')})
				,getConfiguracionColumna("PlazoEnDiasOperacion"				,{groupable:agrupa, title: $translate.instant('UI_PLAZO')})
				,getConfiguracionColumna("IdClasificacionTipoInstrumentoSubyacente",{title: $translate.instant('UI_MERCADO')})
				,getConfiguracionColumna("NemoSubyacente"               	,{groupable:agrupa,title: $translate.instant('UI_INSTRUMENTO')})
				,getConfiguracionColumna("DocumentoIdentidad"				,{groupable:agrupa, title: $translate.instant('UI_DOCUMENTO_IDENTIDAD')})
				,getConfiguracionColumna("CodigoPortafolio"					,{groupable:agrupa, title: $translate.instant('UI_PORTAFOLIO')})
				,getConfiguracionColumna("RazonSocial"						,{groupable:agrupa, title: $translate.instant('UI_PRESTAMISTA')})
				,getConfiguracionColumna("INGCantidad"						,{groupable:agrupa, title: $translate.instant('UI_CANTIDAD'), template : " <label> #:  (util.bcs_formato(INGCantidad,'CUS','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"})
				,getConfiguracionColumna("INGPrecioInstrumento"				,{title: $translate.instant('UI_PRECIO_PRESTAMO'), template : " <label> #:  (util.bcs_formato(INGPrecioInstrumento,'CUS','Precio','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"})
				,getConfiguracionColumna("INGMontoATasaCompraActual"		,{title: $translate.instant('UI_MONTO_PRESTAMO'), template : " <label> #:  (util.bcs_formato(INGMontoATasaCompraActual,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"})
				,getConfiguracionColumna("PrecioCompraInicial"				,{title: $translate.instant('UI_PRECIO_COMPRA_INICIAL'), template : kendo.template("#if(PrecioCompraInicial == 0){# #: (util.bcs_formato(INGPrecioInstrumento,'CUS','Precio','" + $rootScope.SesionIdMonedaSistema + "','','')).valor # #}else{# #: (util.bcs_formato(PrecioCompraInicial,'CUS','Precio','" + $rootScope.SesionIdMonedaSistema + "','','')).valor # #}#") })
				,getConfiguracionColumna("MontoCompraInicial"				,{title: $translate.instant('UI_MONTO_COMPRA_INICIAL'), template : kendo.template("#if(PrecioCompraInicial == 0){# #: (util.bcs_formato(INGMontoATasaCompraActual,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor # #}else{# #: (util.bcs_formato(MontoCompraInicial,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor # #}#") })
				,getConfiguracionColumna("INGTasaEmprestito"				,{groupable:agrupa, title: $translate.instant('UI_PORCENTAJE_PREMIO_ABR'), template : " <label> #:  (util.bcs_formato(INGTasaEmprestito,'CUS','Tasa','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"})
				,getConfiguracionColumna("INGMontoPremio"					,{groupable:agrupa, title: $translate.instant('UI_PREMIO'), template : " <label> #:  (util.bcs_formato(INGMontoPremio,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"})
				,getConfiguracionColumna("INGMontoAPlazo"					,{groupable:agrupa, title: $translate.instant('UI_MONTO_PLAZO'), template : " <label> #:  (util.bcs_formato(INGMontoAPlazo,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"})					
				,getConfiguracionColumna("InteresDiario"					,{title: $translate.instant('UI_INTERES_DIARIO'),    template : " <label> #:  (util.bcs_formato(InteresDiario,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"}) 
				,getConfiguracionColumna("InteresAcumulado"					,{title: $translate.instant('UI_INTERES_ACUMULADO'), template : " <label> #:  (util.bcs_formato(InteresAcumulado,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"})					
				,getConfiguracionColumna("FolioSistemaExterno"				,{width:"150px", groupable:agrupa, title: $translate.instant('UI_FOLIO_BLOCKCHAIN')})	
				
			];
	};
  
	obj.columnasEmprestitosHistoricos = function(agrupa) {
		return [
			 getConfiguracionColumna("FolioEmprestito"				,{width:"75px", groupable:agrupa, title: $translate.instant('UI_FOLIO')})
			,getConfiguracionColumna("FolioPrestamo"				,{width:"75px", groupable:agrupa, title: $translate.instant('UI_PRESTAMO')})
			,getConfiguracionColumna("NombreEstado"					,{groupable:agrupa, title: $translate.instant('UI_ESTADO')})
			,getConfiguracionColumna("NombreTipoPrestamo"			,{groupable:agrupa,title: $translate.instant('UI_TIPO_PRESTAMO')})
			,getConfiguracionColumna("FechaEmprestito"	            ,{groupable:agrupa, title: $translate.instant('UI_FECHA_OPERACION')})
			,getConfiguracionColumna("FechaLiquidacion"				,{title: $translate.instant('UI_FECHA_LIQUIDACION')})
			,getConfiguracionColumna("FechaVencimientoEmprestito"	,{groupable:agrupa, title: $translate.instant('UI_FECHA_VENCIMIENTO')})	
			,getConfiguracionColumna("EGRFechaTermino"				,{title: $translate.instant('UI_FECHA_TERMINO')})
			,getConfiguracionColumna("IdClasificacionTipoInstrumentoSubyacente",{title: $translate.instant('UI_MERCADO')})
			,getConfiguracionColumna("NemoSubyacente"               ,{groupable:agrupa,title: $translate.instant('UI_INSTRUMENTO')})
			,getConfiguracionColumna("DocumentoIdentidad"			,{groupable:agrupa, title: $translate.instant('UI_DOCUMENTO_IDENTIDAD')})
			,getConfiguracionColumna("CodigoPortafolio"				,{groupable:agrupa, title: $translate.instant('UI_PORTAFOLIO')})
			,getConfiguracionColumna("RazonSocial"					,{groupable:agrupa, title: $translate.instant('UI_PRESTAMISTA')})
			,getConfiguracionColumna("EGRCantidad"					,{groupable:agrupa, title: $translate.instant('UI_CANTIDAD'), template : " <label> #:  (util.bcs_formato(EGRCantidad,'CUS','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"})
			,getConfiguracionColumna("EGRPrecio"					,{groupable:agrupa, title: $translate.instant('UI_PRECIO_PRESTAMO'), template : " <label> #:  (util.bcs_formato(INGPrecioInstrumento,'CUS','Precio','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"})
			,getConfiguracionColumna("EGRMonto"						,{groupable:agrupa, title: $translate.instant('UI_MONTO_PRESTAMO'), template : " <label> #:  (util.bcs_formato(EGRMonto,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"})
			,getConfiguracionColumna("PrecioCompraInicial"			,{title: $translate.instant('UI_PRECIO_COMPRA_INICIAL'), template : kendo.template("#if(PrecioCompraInicial == 0){# #: (util.bcs_formato(INGPrecioInstrumento,'CUS','Precio','" + $rootScope.SesionIdMonedaSistema + "','','')).valor # #}else{# #: (util.bcs_formato(PrecioCompraInicial,'CUS','Precio','" + $rootScope.SesionIdMonedaSistema + "','','')).valor # #}#") })
			,getConfiguracionColumna("MontoCompraInicial"			,{title: $translate.instant('UI_MONTO_COMPRA_INICIAL'), template : kendo.template("#if(PrecioCompraInicial == 0){# #: (util.bcs_formato(INGMontoATasaCompraActual,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor # #}else{# #: (util.bcs_formato(MontoCompraInicial,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor # #}#") })
			,getConfiguracionColumna("EGRTasa"						,{groupable:agrupa, title: $translate.instant('UI_PORCENTAJE_PREMIO_ABR'), template : " <label> #:  (util.bcs_formato(INGTasaEmprestito,'CUS','Tasa','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"})
			,getConfiguracionColumna("EGRMontoPremio"				,{groupable:agrupa, title: $translate.instant('UI_PREMIO'), template : " <label> #:  (util.bcs_formato(EGRMontoPremio,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"})
			,getConfiguracionColumna("EGRMontoAPlazo"				,{groupable:agrupa, title: $translate.instant('UI_MONTO_PLAZO'), template : " <label> #:  (util.bcs_formato(EGRMontoAPlazo,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"})
			,getConfiguracionColumna("InteresCorrido"				,{title: $translate.instant('UI_INTERESES_CORRIDOS'), template : " <label> #:  (util.bcs_formato(InteresCorrido,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"})					 
			,getConfiguracionColumna("FolioSistemaExterno"			,{width:"150px", groupable:agrupa, title: $translate.instant('UI_FOLIO_BLOCKCHAIN')})
		];
	};
    
    
    obj.columnasEmprestitosAnticipos = function() {
  		return [
			 getConfiguracionColumna("FolioEmprestito",{width:"75px", groupable:false, title: $translate.instant('UI_FOLIO')})
			,getConfiguracionColumna("DocumentoIdentidad",{groupable:false, title: $translate.instant('UI_DOCUMENTO_IDENTIDAD')})
			,getConfiguracionColumna("CodigoPortafolio",{groupable:false, title: $translate.instant('UI_PORTAFOLIO')})
			,getConfiguracionColumna("RazonSocial",{groupable:false, title: $translate.instant('UI_PRESTAMISTA')})
			,getConfiguracionColumna("PlazoEnDiasOperacion",{groupable:false, title: $translate.instant('UI_PLAZO')})
			,getConfiguracionColumna("EGRCantidad",{groupable:false, title: $translate.instant('UI_CANTIDAD'), template : " <label> #:  (util.bcs_formato(EGRCantidad,'CUS','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"})
			,getConfiguracionColumna("EGRPrecio",{groupable:false, title: $translate.instant('UI_PRECIO'), template : " <label> #:  (util.bcs_formato(EGRPrecio,'CUS','Precio','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"})
			,getConfiguracionColumna("EGRMonto",{groupable:false, title: $translate.instant('UI_MONTO'), template : " <label> #:  (util.bcs_formato(EGRMonto,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"})
			,getConfiguracionColumna("EGRTasa",{groupable:false, title: $translate.instant('UI_PORCENTAJE_PREMIO_ABR'), template : " <label> #:  (util.bcs_formato(EGRTasa,'CUS','Tasa','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"})
			,getConfiguracionColumna("EGRPremio",{groupable:false, title: $translate.instant('UI_PREMIO'), template : " <label> #:  (util.bcs_formato(EGRPremio,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"})
			,getConfiguracionColumna("EGRMontoAPlazo",{groupable:false, title: $translate.instant('UI_MONTO_PLAZO'), template : " <label> #:  (util.bcs_formato(EGRMontoAPlazo,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"})
		];
    };
	// Definición del modelo
  	// Los tipos de datos definidos son: string, date, number.
    obj.modeloPrestamos = function() {
  		return {
			model: {
				fields : {
					FolioPrestamo : { type: "number"},
					NombreEstado : { type: "string"},
					NombreTipoPrestamo : { type: "string"},
					FechaPrestamo : { type: "sysdate"},
					FechaVencimientoPrestamo : { type: "sysdate"},
					IdCondicionLiquidacion : {type: "string"},
					FechaLiquidacion : { type: "sysdate"},
					PlazoEnDiasOperacion : { type: "number"},
					IdClasificacionTipoInstrumentoSubyacente : {type: "string"},
					NemoSubyacente : { type: "string"},
					DocumentoIdentidad: { type: "string"},
					CodigoPortafolio : { type: "number"},
					RazonSocial : { type: "string"},
					CantidadPorAsignar: { type: "number"},
					INGCantidad	: { type: "number"},
					INGMontoATasaCompraActual	: { type: "number"},
					INGMontoAPlazo	: { type: "number"},
					INGMontoPremio : { type: "number"},
					INGPrecioInstrumento : { type: "number"},
					INGTasaPrestamo: { type: "number"},
					INGMontoPrima: { type: "number"},
					InteresAcumulado : { type: "number"},
					InteresDiario : {type: "number"},
					FolioComprobante : { type: "number"},
					FolioSistemaExterno : { type: "string"},

					//Historico
					EstadoLiquidacion : { type: "string"},
					InteresCorrido : { type: "number"},
					EGRFechaTermino : {type: "sysdate"},
					EGRCantidad : { type: "number"},
					EGRPrecio : { type: "number"},
					EGRTasa : { type: "number"},
					EGRMonto : { type: "number"},
					EGRMontoPrima : { type: "number"},
					EGRMontoAPlazo : { type: "number"},

					//OTROS
					INGFechaOperacion 			: { type: "sysdate"},
					IdEstado 					: { type: "string"},
					EstadoLiquidacion 			: { type: "string"},
					IdTipoPrestamo 				: { type: "string"},
					NemoPrestamo 				: { type: "string"},
					IdBolsa 					: { type: "string"},
					NombreBolsa 				: { type: "string"},
					FechaVencimientoInstrumento : {type: "sysdate"},
					INGFechaLiquidacionPrestamo : { type: "sysdate"},
					INGTasaInstrumento 			: { type: "number"},
					INGMontoInstrumento 		: { type: "number"},
					MontoCompra					: { type: "number"},
					INGMontoTotalPrestamo		: { type: "number"},
					CantidadAsignada			: { type: "number"},
					NombreMercado				: {type: "string"},
					NombreEmisorSubyacente		: {type: "string"},
					FechaIngresoRegistro		: {type: "sysdate"},
					DescripcionPortafolio		: {type: "string"},
					TipoValorizacion			: {type: "string"},
					IdCartera					: { type: "number"},
					IdSubyacente				: { type: "number"},
					INGMontoInteresCorridoCompraActual : { type: "number"},
					INGMontoInteresCompraProximo : { type: "number"},
					EGRMontoAnticipo 			: { type: "number"},
					EGRFechaAnticipo			: {type: "sysdate"},
					EstadoAnticipo 				: { type: "string"},
					ExigirGarantia 				: { type: "string"},
					InteresCorrido 				: { type: "number"},
					InteresAcumulado 			: { type: "number"},
					InteresDiario 				: {type: "number"},
					PrecioMedioInicial 			: {type: "number"},
					MontoPrecioMedio 			: {type: "number"},
					PrecioCompraInicial 		: {type: "number"},
					MontoCompraInicial 			: {type: "number"}
				}
			}
		};
  	};

  	obj.modeloEmprestitos = function() {
  		return {
			model: {
				fields : {
					FolioEmprestito  			: { type: "number"},
					FechaEmprestito	     		: { type: "sysdate"},
					FechaVencimientoEmprestito	: { type: "sysdate"},
					FolioPrestamo 				: { type: "number"},
					NombreEstado 				: { type: "string"},
					NombreTipoPrestamo 			: { type: "string"},
					FechaPrestamo 				: { type: "sysdate"},
					FechaVencimientoPrestamo 	: { type: "sysdate"},
					IdCondicionLiquidacion 		: {type: "string"},
					FechaLiquidacion 			: { type: "sysdate"},
					PlazoEnDiasOperacion 		: { type: "number"},
					IdClasificacionTipoInstrumentoSubyacente : {type: "string"},
					NemoSubyacente 				: { type: "string"},
					DocumentoIdentidad			: { type: "string"},
					CodigoPortafolio 			: { type: "number"},
					RazonSocial 				: { type: "string"},
					CantidadPorAsignar			: { type: "number"},
					INGCantidad					: { type: "number"},
					INGMontoATasaCompraActual	: { type: "number"},
					INGMontoAPlazo				: { type: "number"},
					INGMontoPremio 				: { type: "number"},
					INGPrecioInstrumento 		: { type: "number"},
					INGTasaPrestamo				: { type: "number"},
					INGMontoPrima				: { type: "number"},
					FolioComprobante 			: { type: "number"},
					FolioSistemaExterno 		: { type: "string"},
					INGTasaEmprestito			: { type: "number"},
					INGMontoPremio				: { type: "number"},
					InteresCorrido 				: { type: "number"},
					InteresDiario 				: {type: "number"},

					//HISTORICO
					InteresAcumulado 			: { type: "number"},
					EGRCantidad 				: { type: "number"},
					EGRMonto					: { type: "number"},
					EGRMontoPremio				: { type: "number"},
					EGRMontoAPlazo				: { type: "number"},
					EGRFechaTermino 			: {type: "sysdate"},
					EGRPrecio 					: { type: "number"},
					EGRTasa 					: { type: "number"},

					//OTROS
					INGFechaOperacion 			: { type: "sysdate"},
					IdEstado 					: { type: "string"},
					EstadoLiquidacion 			: { type: "string"},
					ExigirGarantia				: { type: "string"},
					NemoSubyacente				: { type: "string"},
					FechaInicioEmprestito 		: { type: "sysdate"},
					EGRCantidad 				: { type: "number"},
					INGMontoInstrumento 		: { type: "number"},
					INGMontoTotalEmprestito		: { type: "number"},	
					INGMontoInteresCorridoCompraActual : { type: "number"},
					INGMontoInteresCompraProximo : { type: "number"},
					EGRFechaTermino 			: {type: "sysdate"},
					EGRMonto 					: { type: "number"},
					EGRMontoPrima 				: { type: "number"},
					EGRMontoAPlazo 				: { type: "number"},
					PrecioMedioInicial 			: {type: "number"},
					MontoPrecioMedio 			: {type: "number"},
					PrecioCompraInicial 		: {type: "number"},
					MontoCompraInicial 			: {type: "number"}
				}
			}
		};
  	};
	
  	obj.modeloAnticipos = function() {
  		return {
			model: {
				fields : {
					FolioEmprestito : { type: "number"},
					IdEstado : { type: "string"},
					NombreEstado : { type: "string"},
					NemoSubyacente: { type: "string"},
					DocumentoIdentidad: { type: "string"},
					CodigoPortafolio : { type: "number"},
					RazonSocial : { type: "string"},
					DiasRestantes : { type: "number"},
					PlazoEnDiasOperacion : { type: "number"},
					EGRFechaOperacion : { type: "sysdate"},
					EGRCantidad : { type: "number"},
					EGRPrecio : { type: "number"},
					EGRPremio : { type: "number"},
					EGRMonto : { type: "number"},
					EGRTasa: { type: "number"},
					EGRPrecioAPlazo: { type: "number"},
					EGRMontoAPlazo: { type: "number"}
				}
			}
		};
  	};
  	

  	return obj;
	
}]);
