		
angular.module('ModuloMesa').factory('bcsConsultarForwardPorVencerGrid',
		function(bcsComunes, $rootScope)  
{
	
	var obj = {};
	
	
	
	transporteForwardPorVencer = function() {
		return {
			read : {
				url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=515",
				dataType : "json"
			}
		};
	};	
	
	
	modeloForwardPorVencer = function() {
		return {
			model: {
				fields : {
					FolioOperacion 				: { type: "number"},
					IdOperacion 				: { type: "number"},
					FechaOperacion 			    : { type: "sysdate"},
					RutCliente 			    	: { type: "string"},
					NombreCliente 			    : { type: "string"},
					CodigoPropositoPortafolio 	: { type: "string"},
					TipoOperacion 				: { type: "string"},
					IdMonedaPrincipal           : { type: "string"},
					IdMonedaSecundaria          : { type: "string"},
					MontoMonedaPrincipal        : { type: "number"},
					MontoMonedaSecundaria       : { type: "number"},
					TipoCambioCosto 			: { type: "number"},
					TipoCambioCierre 			: { type: "number"},
					Plazo 					    : { type: "number"},
					MontoDiferido 				: { type: "number"},
					MontoValorRazonableNeto 	: { type: "number"},
					ModalidadLiquidacion		: { type: "string"},
					NombreAgente				: { type: "string"},
					IdMonedaCompensacion		: { type: "string"}
				}
			}
		};
	};
	
	
	
	obj.dataSourceForwardPorVencer = new bcs.DataSource({
		type 		: "odata", //Los tipos posibles son: odata, call, array y object
		definition 	: {
			            serverFiltering 	: true, // Filtrado de datos en el servidor.
			            serverPaging 		: true, // Paginado de los datos en el servidor.
			            serverSorting 		: true, // Ordenamiento de los datos en el servidor.
			            sort 				: { field: "NombreCliente", dir: "desc" }, // Criterios de Ordenamiento.
			            pageSize 			: 50, // Número de registros por página.
			            transport 			: transporteForwardPorVencer(),
			            schema 				: modeloForwardPorVencer()
		              }  
	});
	
	
	
	obj.columnasForwardPorVencer = function() {
		return[
		    {field : "FolioOperacion", title:'UI_FOLIO_OPERACION', width : "80px", sortable : true },
			{field : "IdOperacion", title:'UI_FOLIO_CARTERA', width : "80px", sortable : true },
			{field : "FechaOperacion", title:'UI_FECHA_INICIO', width : "80px", sortable : true },
			{field : "RutCliente", title:'UI_RUT_CLIENTE', width : "200px", sortable : true },
		   	{field : "NombreCliente", title:'UI_NOMBRE_CLIENTE', width : "200px", sortable : true },
		   	{field : "CodigoPropositoPortafolio", title:'UI_CUENTA', width : "110px", sortable : false },
		   	{field : "TipoOperacion", title:'UI_TIPO', width : "110px", sortable : false },
			{field : "IdMonedaPrincipal", title:'UI_MONEDA_PRINCIPAL', width : "110px", sortable : false },
			{field : "IdMonedaSecundaria", title:'UI_MONEDA_SECUNDARIA', width : "110px", sortable : false },
		   	{field : "MontoMonedaPrincipal", title:'UI_MONTO_MONEDA_PRINCIPAL', width : "100px", sortable : false, template : "<label> #: (util.bcs_formato(MontoMonedaPrincipal,'GEN','Monto',IdMonedaPrincipal,'','')).valor #</label>"},
			{field : "MontoMonedaSecundaria", title:'UI_MONTO_MONEDA_SECUNDARIA', width : "100px", sortable : false, template : "<label> #: (util.bcs_formato(MontoMonedaSecundaria,'GEN','Monto',IdMonedaSecundaria,'','')).valor #</label>"},
			{field : "TipoCambioCierre", title:'UI_TIPO_CAMBIO_CIERRE_FORWARD', width : "100px", sortable : false},
		   	{field : "TipoCambioCosto", title:'UI_TIPO_CAMBIO_COSTO_FORWARD', width : "100px", sortable : false},
		   	{field : "Plazo", title:'UI_PLAZO', width : "100px", sortable : false},
			{field : "MontoDiferido", title:'UI_MONTO_DIFERIDO', width : "100px", sortable : false, template : "<label> #: (util.bcs_formato(MontoDiferido,'GEN','Monto',IdMonedaPrincipal,'','')).valor #</label>"},
			{field : "MontoValorRazonableNeto", title:'UI_VALOR_RAZONABLE', width : "100px", sortable : false, template : "<label> #: (util.bcs_formato(MontoValorRazonableNeto,'GEN','Monto',IdMonedaPrincipal,'','')).valor #</label>"},
			{field : "ModalidadLiquidacion", title:'UI_MODALIDAD', width : "100px", sortable : false},
		   	{field : "NombreAgente", title:'UI_NOMBRE_AGENTE', width : "200px", sortable : false },
		   	{field : "IdMonedaCompensacion", title:'UI_MONEDA_COMPENSACION', width : "140px", sortable : false }
		];
	};
		
	
		
	return obj;
});	


