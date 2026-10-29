angular.module('ModuloNormativa').factory('bcsConsultarCartaUno',['bcsComunes',
		function(bcsComunes)  
{
	var obj = {};	
	

	transporte = function(Grid) {
		// vistas que utiliza cada grilla
		if (Grid === 'TransCartaUno')
		
			//url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1055";   
			url = "/BSCL/Normativa/XSODATA/Normativa.xsodata/CartaUno";
		return {
			read : {
				url : url,
				dataType : "json" 
			}
		};
	};
	


	modeloCartaUno = function() {
		return {
    		model: {
    			fields: {					
    				TipoOperacion				: { type: "string"},
    				Instrumento					: { type: "string"},
    				Liquidacion					: { type: "string"},
    				FechaVencimiento			: { type: "sysdate"},
    				Relacion					: { type: "string"},
    				Cantidad					: { type: "string"},
    				Precio						: { type: "number"},
    				Total 						: { type: "number"},
				
    			}  
        	}
		};
	};
 

	obj.dataSourceCartaUno = function() {
		return new bcs.DataSource({
			
			type 		: "odata", //Los tipos posibles son: odata, call, array y object
			definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: true, // Ordenamiento de los datos en el servidor.
							//filter 				: { field: "FechaVencimiento", operator: "eq" },
							pageSize 			: 25,
							transport 			: transporte('TransCartaUno'),
							schema 				: modeloCartaUno()
						  }
		});
	};

	obj.columnasCartaUno = function() {
		return  [
		 {field: "TipoOperacion", 				title: 'TipoOperacion', 						width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "Instrumento",					title: 'Instrumento',							width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "Liquidacion", 				title: 'Liquidacion', 							width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "FechaVencimiento", 			title: 'FechaVencimiento', 						width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true} 									
		,{field: "Relacion", 					title: 'Relacion', 								width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
		,{field: "Cantidad", 					title: 'Cantidad', 								width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
		,{field: "Precio", 						title: 'Precio', 								width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
		,{field: "Total", 						title: 'Total', 								width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
		
		
        ];
	};
	return obj;
}]);	

