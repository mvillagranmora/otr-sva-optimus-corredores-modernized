		
angular.module('ModuloSocioNegocio').factory('bcsConsultarReporteRentabilidadGrid',
		function(bcsComunes, $rootScope)  
{
	
	var obj = {};
	
	
	
	transporteReporteRentabilidad = function() {
		return {
			read : {
				url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=700025",
				dataType : "json"
			}
		};
	};	
	
	
	modeloReporteRentabilidad = function() {
		return {
			model: {
				fields : {
					Cliente 			        : { type: "string"},
					PerfilRiesgo				: { type: "string"},
					YTD							: { type: "number"},
					MTD				            : { type: "number"},
					Pesos				        : { type: "number"},
					ITD		 					: { type: "number"},
					NombreAgente	 			: { type: "string"}
				}
			}
		};
	};
	
	
	
	obj.dataSourceReporteRentabilidad = new bcs.DataSource({
		type 		: "odata", //Los tipos posibles son: odata, call, array y object
		definition 	: {
			            serverFiltering 	: true, // Filtrado de datos en el servidor.
			            serverPaging 		: true, // Paginado de los datos en el servidor.
			            serverSorting 		: true, // Ordenamiento de los datos en el servidor.
			            sort 				: { field: "Cliente", dir: "desc" }, // Criterios de Ordenamiento.
			            pageSize 			: 50, // Número de registros por página.
			            transport 			: transporteReporteRentabilidad(),
			            schema 				: modeloReporteRentabilidad()
		              }  
	});
	
	
	
	obj.columnasReporteRentabilidad = function() {
		return[
			{field : "Cliente", title:'UI_CLIENTE_ABR', width : "200px", sortable : false }
		   ,{field : "PerfilRiesgo", title:'UI_PERFIL_RIESGO', width : "110px", sortable : false }
		   ,{field : "YTD", title:'UI_YTD', width : "110px", align: "left", sortable : false }
		   ,{field : "MTD", title:'UI_MTD', width : "110px", sortable : false}
		   ,{field : "Pesos", title:'UI_PESOS', width : "110px", align: "left", sortable : false,  template : " <label> #:  (util.bcs_formato(Pesos,'GEN','Monto','CLP','','')).valor #</label>" }
		   ,{field : "ITD", title:'UI_ITD', width : "100px", sortable : false}
		   ,{field : "NombreAgente", title:'UI_EJECUTIVO', width : "200px", sortable : false }			
		];
	};
		
	
		
	return obj;
});	


