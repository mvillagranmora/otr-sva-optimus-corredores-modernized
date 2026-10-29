angular.module('ModuloPlataforma').factory('bcsConfiguracionDetalleAutorizaciones',function($translate) {

	var obj = {};
	
	/*obj.dataSourceEvento = function(){
		return new bcs.DataSource ({
			type 		: "odata", //Los tipos posibles son: odata, call, array y object
			definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: true, // Ordenamiento de los datos en el servidor.
							sortable 			: true,
							pageSize 			: 50,
							transport 			: transporte('Evento'),
							schema 				: ModeloEvento()
							
						  }
		});
	};*/
	
	
	
	obj.columnasDetalleMsj = function() {
		return [ 
				 {field: "IdEvento", title: "Autorizado",groupable: true, width: "70px" }
		        ,{field: "HoraRegistro", title: "Registro", width: "100px",groupable: true, format: "{0:HH:mm}" }
	   	    	,{field: "HoraVigencia", title: "Vigencia", width: "100px",groupable: true, format: "{0:HH:mm}" }
				];
	};
	return obj;
});
