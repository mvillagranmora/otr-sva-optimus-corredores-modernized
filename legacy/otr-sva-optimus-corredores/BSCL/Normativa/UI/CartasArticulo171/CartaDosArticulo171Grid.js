angular.module('ModuloNormativa').factory('bcsConsultarCartaDos',['bcsComunes',
		function(bcsComunes)  
{
	var obj = {};	
	

	transporte = function(Grid) {
		// vistas que utiliza cada grilla
		if (Grid === 'TransCartaDos')
		url = "/BSCL/Normativa/XSODATA/Normativa.xsodata/CartaDos";
		return {
			read : {
				url : url,
				dataType : "json"
			}
		};
	};
	


	modeloCartaDos = function() {
		return {
    		model: {
    			fields: {					
    				Fecha				: { type: "sysdate"},
    				TipoOperacion		: { type: "string"},
    				Instrumento			: { type: "string"},
    				Cantidad			: { type: "number"},
    				Precio				: { type: "number"},
    				Monto				: { type: "number"},
    				
    			} 
        	}
		};
	};

	
	//data source solicitud circular
	obj.dataSourceCartaDos = function() {
		return new bcs.DataSource( {
			type 		: "odata", //Los tipos posibles son: odata, call, array y object
			definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: true, // Ordenamiento de los datos en el servidor.
							//filter 				: { field: "IdTipoCircular", operator: "eq" },
							pageSize 			: 25,
							transport 			: transporte('TransCartaDos'),
							schema 				: modeloCartaDos()
						  }
		});
	};

	obj.columnasCartaDos = function() {
		return  [
		 {field: "Fecha", 				title: 'Fecha', 						width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }//template : " <label> #:  util.formatoDate(util.datefix(Fecha)) #</label>" , width: "145px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
		,{field: "TipoOperacion",		title: 'TipoOperacion',					width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "Instrumento", 		title: 'Instrumento', 					width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "Cantidad", 			title: 'Cantidad', 						width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "Precio", 				title: 'Precio', 						width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
		,{field: "Monto", 				title: 'Monto', 						width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
		
		
		
        ];
	};
	return obj;
}]);	

