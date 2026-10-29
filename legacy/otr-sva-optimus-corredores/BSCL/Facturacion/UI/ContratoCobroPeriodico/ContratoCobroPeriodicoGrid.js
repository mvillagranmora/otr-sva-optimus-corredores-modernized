angular.module('ModuloFacturacion').factory('bcsContratoCobroPeriodicoGrid',['bcsComunes',
		function(bcsComunes)  
{
	
	var obj = {};
	
	//Contrato Cobros	
	modeloContratoCobroPeriodico = function() {
		return {
			model: {
				fields : {
					IdComprobante            : { type: "number"},
					FolioComprobante         : { type: "number"}, 
 					IdTipoComprobante        : { type: "string"},
					FechaComprobante         : { type: "sysdate"},
					IdCondicionLiquidacion   : { type: "string"}, 
					DocumentoIdentidad       : { type: "string"},
					IdCliente                : { type: "number"},
					NombreCliente            : { type: "string"}, 
					IdPortafolio             : { type: "number"},
					CodigoPortafolio         : { type: "string"},
					NombreConcepto       	 : { type: "string"},
					IdClasificacionTipoInstrumento 	  	  : { type: "string"},
					CodigoModoFacturacionComprobante 	  : { type: "string"},
					DescripcionModoFacturacionComprobante : { type: "string"},
					IdContratoComision       : { type: "number"},
					NombreContrato       	 : { type: "string"},
					Monto            	     : { type: "number"},
					MontoComprobante         : { type: "number"},
					MontoLiquidacion         : { type: "number"},
					IdComprobanteCobroMensual: { type: "number"},
					IdComprobanteComisionMensual		   : { type: "number"},
					Estado       	 		 : { type: "string"},
					IdComprobanteReferencia: { type: "number"},
					IdTipoContrato			 : { type: "string"},
				}
			}
		};
	};
			
	transporteContratoCobroPeriodico = function() {
		return {
			read : {
				url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=700299",
				dataType : "json"
			}
		};
	};	
	
	
	obj.dataSourceContratoCobroPeriodico = new bcs.DataSource({
		type 		: "odata", //Los tipos posibles son: odata, call, array y object
		definition 	: {
			            serverFiltering 	: true, // Filtrado de datos en el servidor.
			            serverPaging 		: false, // Paginado de los datos en el servidor.
			            serverSorting 		: true, // Ordenamiento de los datos en el servidor.
			            sort 				: { field: "IdComprobante", dir: "desc" }, // Criterios de Ordenamiento.
			            transport 			: transporteContratoCobroPeriodico(),
			            schema 				: modeloContratoCobroPeriodico(),			
	                    aggregate			: [ //Para resumen de totales de cobro
	                 	  		                { field: "Monto", aggregate: "sum" },
	                 	  		                { field: "MntoCobro", aggregate: "sum" },	                 	  		                
                                              ]
		              }
	});
	
	obj.columnasContratoCobroPeriodico = function() {	
		return  [
			 {field: "IdComprobante", 		title: "UI_SECUENCIA_COMPROBANTE",width: "75px",  align: "right"},
			 {field: "FolioComprobante", 	title: "UI_FOLIO_COMPROBANTE",width: "75px",  align: "right"},
	   	     {field: "IdComprobanteReferencia"	,title: "UI_COMPROBANTE_REFERENCIA", width: "75px",  align: "left", groupable: true },			 
			 {field: "IdTipoComprobante", 	title: "UI_TIPO_COMPROBANTE",width: "50px",  align: "left"},
			 {field: "FechaComprobante", 	title: "UI_FECHA_COMPROBANTE",width: "75px",  align: "right"},			 
			 {field: "IdCondicionLiquidacion", title: "UI_CONDICION_LIQUIDACION", width: "40px"},
			 {field: "NombreCliente", 		title: "UI_CLIENTE",width: "200px", align: "left", groupable: true },
			 {field: "DocumentoIdentidad", 	title: "UI_DOCUMENTO_IDENTIDAD",width: "80px",  align: "left", groupable: true },
			 {field: "CodigoPortafolio", 	title: "UI_PORTAFOLIO",width: "60px", align: "right"},
	   	     {field: "DescripcionModoFacturacionComprobante", title: "UI_MODO_FACTURACION",width: "80px", align: "left", groupable: true },
	   	     {field: "NombreContrato", 		title: "UI_NOMBRE_CONVENIO_CONTRATO",width: "120px", align: "left", groupable: true },	 			 
			 {field: "IdClasificacionTipoInstrumento"		, title: "UI_MERCADO",width: "50px", align: "left", groupable: true },			 
	   	     {field: "NombreConcepto",      title: "UI_CONCEPTO",width: "200px", align: "left", groupable: true },			 
	   	     {field: "Monto",	            title: "UI_MONTO",format: "{0:n0}",width: "75px",  align: "left", groupable: true },
	   	     {field: "MontoComprobante",	title: "UI_MONTO_LOCAL",format: "{0:n0}",width: "75px",  align: "left", groupable: true },
	   	     {field: "MontoLiquidacion",	title: "UI_MONTO_LIQUIDACION",format: "{0:n0}",width: "75px",  align: "left", groupable: true },
	   	     {field: "Estado",				title: "UI_ESTADO",format: "{0:n0}",width: "80px",  align: "left", groupable: true },
	   	     {field: "IdComprobanteComisionMensual"	,title: "UI_FACTURA_GENERADA", width: "75px",  align: "left", groupable: true },
		];  
	};
	
	obj.fnRowColor = function(row) {
        if (row.Estado === 'PENDIENTE') return 'yellow';
		if (row.Estado === 'FACTURADA' || row.Estado === 'NO DISPONIBLE') return 'green';
		return '';		
  	};
	
    return obj;
    
}]);	