angular.module('ModuloPlataforma').controller('MantenedorDocumentosCtrl',function($scope,bcsComunes,$resource,$translate){
	
	$scope.data = {};

	

	
	
	dataSourceTipoDocumento = function() {
	return {
			type : "odata", 
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				pageSize : 50,
				transport : {
					read : {
						url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1544", //asignar ODATA
						dataType : "json"
					}
				},
				schema : ModeloTipoDocumento()
			}
		};
	};
	
	columnasTipoDocumento = function() {
		return [ 
    	   	    {field: "IdTipoDocumento", title: $translate.instant('UI_NOMBRE'), width: "150px", attributes:{style:"text-align:center;"} }
    	   	    ,{field: "NombreDocumento", title: $translate.instant('UI_NOMBRE'), width: "75px", attributes:{style:"text-align:center;"}  }
    	   	    ,{field: "SolicitarFechaFirma", title: $translate.instant('UI_SOLICITAR_FECHA_FIRMA'), width: "60px", attributes:{style:"text-align:left;"}  }
    	   	    ,{field: "NumeroFirmantesEmpresa", title: $translate.instant('UI_CANTIDAD_FIRMANTES_EMPRESA'), width: "100px", attributes:{style:"text-align:center;"} }
    	   	    ,{field: "NumeroFirmantesCliente", title: $translate.instant('UI_CANTIDAD_FIRMANTES_CLIENTE'),  attributes:{style:"text-align:left;"} }
    	   	    ,{field: "Clasificacion", title: $translate.instant('UI_CLASIFICACION'),  attributes:{style:"text-align:left;"} }
    	   	    ,{field: "ValidaFechaVigencia", title: $translate.instant('UI_VALIDAR_FECHA'),  attributes:{style:"text-align:left;"} }
				];
	};
	
	ModeloTipoDocumento = function() {
		return {
			model: {
    	        	fields:{
    	        		IdTipoDocumento 		: { type : "string"},
    	        		IdEmpresa 				: { type : "number"},
    	        		NombreDocumento 		: { type : "string"},
    	        		SolicitarFechaFirma	: { type : "string"},
    	        		NumeroFirmantesEmpresa 	: { type : "number"},
    	        		NumeroFirmantesCliente	: { type : "number"},
    	        		Clasificacion 			: { type : "string"},
    	        		ValidaFechaVigencia		: { type : "string"}
    	        	} 
			}
		};
	}; 
	
	$scope.data.TipoDocumentos = {
			dataSource			: dataSourceTipoDocumento(),
			columns				: columnasTipoDocumento(),		
			sortable			: true,
			pageable			: true,
			autoBind			: false,  //comentar
			height				: 280,
		    selectable 			: "row"
		};
	
	
	
	
	
	
	
});