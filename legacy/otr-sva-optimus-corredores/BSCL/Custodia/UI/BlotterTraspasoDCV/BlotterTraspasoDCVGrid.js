angular.module('ModuloCustodia').factory('bcsBlotterTraspasoDCVGrid',['bcsComunes',
		function(bcsComunes)  
{
	var obj = {};	
	
	//Operaciones Pendientes
	modeloOperacionesPendientes = function() {
		return {
    		model: {
    			fields: {				
					FolioOperacion			  		: { type: "number"},		//	SEC_MOVTO
//					COD_MOVTO                 		: { type: "string"},
					NombreOperacion					: { type: "string"},		// 	DESC_OPER
					IdTipoInstrumento				: { type: "string"},		//	TIPO_INST
					Nemo                      		: { type: "string"},		//	NEMO
					IdClasificacionTipoInstrumento  : { type: "string"},		//	MERCADO
					IdCondicionLiquidacion			: { type: "string"},		//	IND_LIQUID
					IdTipoOperacion                 : { type: "string"},		//	TIPO_OPER
					Accion            				: { type: "string"},		//	DESC_TIPO_OPER
					OperadorDirecto              	: { type: "string"},		//	OPER_DIRECTO
					FechaIngresoRegistro            : { type: "date"},			//	FEC_MVTO
					FechaVencimiento                : { type: "date"},			//	FEC_VCTO
					DocumentoIdentidad				: { type: "string"},		//	RUT_CLI
					IdPortafolio               		: { type: "number"},		//	SEC_RUT_CLI
					FolioComprobante            	: { type: "number"},		//	FOLIO_COMP_ADJ
					FolioComprobanteReferencia      : { type: "number"},		//	FOLIO_COMP_ASOC
					FolioComprobanteRelacionado     : { type: "number"},		//	FOLIO_COMP_CONT
					IdOrden                 		: { type: "number"},		//	SEC_ORDEN
					Cantidad                  		: { type: "number"},		//	CANTIDAD
					Precio                    		: { type: "number"},		//	PRECIO
					Tasa                      		: { type: "number"},		//	TASA
					Monto                     		: { type: "number"},		//	MONTO
					MontoComprom             		: { type: "number"},		//	MONTO_COMPROM
					IdEstado                    	: { type: "string"},		//	ESTADO
					IdAgente                    	: { type: "string"},		//	AGENTE
					IdMoneda                    	: { type: "string"},		//	MONEDA
					TipoGranatia             		: { type: "string"},		//	TIPO_GARANTIA
					FondoAfp                 		: { type: "string"},		//	FONDO_AFP
					IdTipoComprobante             	: { type: "string"},		//	TIPO_COMP_ADJ
					CodigoTipoProducto              : { type: "string"},		//	PRODUCTO
					IdBolsa                   		: { type: "string"},		//	ENTIDAD
					DocumentoIdentidadEntidad       : { type: "string"},		// 	RUT_ENTI
					IdBoveda                		: { type: "string"},		//	COD_BOVEDA
					FolioTransaccion               	: { type: "number"},		//	FOLIO_TRANS
					DescEstadoCclv          		: { type: "string"},		//	DESC_ESTADO_CCLV
					UsoGarantia          			: { type: "string"},		//	IND_USO_GARANTIA
					TopoOpePendiente              	: { type: "string"},		//	TIPO_OPE_PEN
					BlockEnvio            			: { type: "string"},		//	IND_BLOK_ENVIO
					IdConcepto             			: { type: "string"},		//	CONCEPTO_MVTO
//					IND_ENV_MVTO              		: { type: "string"},		//	IND_ENV_MVTO
					IdEmisor                    	: { type: "string"},		//	EMISOR
					EsFungible          			: { type: "string"},		//	IND_FUNGIBILIDAD
					NemoSVS                  		: { type: "string"},		//	NEMO_SVS
					IdTipoReajuste                 	: { type: "string"},		//	TIPO_REAJ
					Categoria                 		: { type: "string"}        	//CATEGORIA	
					} 
        	}
		};
	};
	
	


	//columnas grilla evento de capital
	obj.columnasOperacionesPendientes= function() {
		return  [
			{field: "DESC_PRODUCTO", title: 'UI_PRODUCTO', width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "NombreOperacion", title: 'UI_OPER_PENDIENTE', width: "200px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			//,{field: "COD_MOVTO"    ,title: 'UI_COD_MOVTO', width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "IdTipoInstrumento", title: 'UI_ID_CLASIFICACION_TIPO_INSTRUMENTO', width: "150px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "Nemo", title: 'UI_NEMO_ABR', width: "150px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "IdClasificacionTipoInstrumento", title: 'UI_MERCADO', width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "IdCondicionLiquidacion", title: 'UI_CONDICION_LIQUIDACION_ABR', width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "IdTipoOperacion", title: 'UI_TIPO_OPERACION', width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			//,{field: "DESC_TIPO_OPER"       ,title: 'UI_NOMBRE_TIPO_OPERACION', 					width: "120px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "OperadorDirecto", title: 'UI_OPER_DIRECTO', width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "IdEstado", title: 'UI_ESTADO', width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "DescEstadoCclv", title: 'UI_DESCRIPCION_CCLV', width: "120px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "FolioOperacion", title: 'UI_SECUENCIA', format: "{0:n0}", width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "FechaIngresoRegistro", title: 'UI_FECHA_REGISTRO', template : " <label> #:  util.formatoDate(FechaIngresoRegistro) #</label>" ,			 	width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }
			,{field: "FechaVencimiento", title: 'UI_FECHA_VENCIMIENTO', template : " <label> #:  util.formatoDate(FechaVencimiento) #</label>" ,			 	width: "100px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }
			,{field: "DocumentoIdentidad", title: 'UI_CLIENTE', width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "IdPortafolio", title: 'UI_PORTAFOLIO', width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "FolioComprobante", title: 'UI_FOLIO_COMPROBANTE', format: "{0:n0}", width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "FolioComprobanteReferencia", title: 'UI_FOLIO_COMP_ASOC', format: "{0:n0}",		width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "FolioComprobanteRelacionado", title: 'UI_FOLIO_COMP_CONT', format: "{0:n0}",		width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "IdOrden", title: 'UI_SECUENCIA_ORDEN', format: "{0:n0}",		width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "Cantidad", title: 'UI_CANTIDAD', format: "{0:n0}",		width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "Precio", title: 'UI_PRECIO', format: "{0:n0}",		width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "Tasa", title: 'UI_TASA', width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "Monto", title: 'UI_MONTO', format: "{0:n0}",		width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "MontoComprom", title: 'UI_MONTO_COMPROBANTE',	format: "{0:n0}",		width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "IdAgente", title: 'UI_AGENTE', width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "IdMoneda", title: 'UI_MONEDA', width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "TipoGranatia", title: 'UI_TIPO_GARANTIA', width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "FondoAfp", title: 'UI_FONDO_AFP', width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "IdTipoComprobante", title: 'UI_TIPO_COMP_ADJ', width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			//,{field: "PRODUCTO"             ,title: 'UI_PRODUCTO', 									width: "70px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "IdBolsa", title: 'UI_ENTIDAD', width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "DocumentoIdentidadEntidad", title: 'UI_RUT_ENTI', width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "IdBoveda", title: 'UI_BOVEDA', width: "70px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "FolioTransaccion", title: 'UI_FOLIO_TRANSACCION', format: "{0:n0}",	width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "UsoGarantia", title: 'UI_GARANTIA', width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "TopoOpePendiente", title: 'UI_TIPO_OPE_PEN', width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "BlockEnvio", title: 'UI_IND_BLOK_ENVIO', width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "NombreConcepto", title: 'UI_CONCEPTO', width: "170px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "IND_ENV_MVTO", title: 'UI_IND_ENV_MVTO', width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "IdEmisor", title: 'UI_EMISOR', width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "EsFungible", title: 'UI_FUNGIBLE', width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "NemoSVS", title: 'UI_NEMO_SVS', width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "IdTipoReajuste", title: 'UI_TIPO_REAJUSTE', width: "70px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "Categoria", title: 'UI_CATEGORIA', width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}		         ];
	};
	
	return obj;
}]);	

