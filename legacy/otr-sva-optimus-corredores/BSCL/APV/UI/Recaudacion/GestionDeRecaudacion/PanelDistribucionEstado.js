+(function() {
	angular.module('ModuloAPV').controller('DistEstadosCtrl',
	function($scope,$translate,$resource,$timeout,bcsComunes)
	{
		$scope.ventanaId = bcsComunes.ventanaWinId($scope);
		var distest = this;
		var param = bcsComunes.ventanaParams($scope.ventanaId);
		/* datasources*/
		var defDistEstados = {
				type : "odata",
				definition : {
	                serverFiltering : true, // Filtrado de datos en el servidor.
	                serverPaging    : true, // Paginado de los datos en el servidor.
	                serverSorting   : true, // Ordenamiento de los datos en el servidor.
	                sort 			: { field: "Fecha", dir: "DESC" },
	                pageSize        : 100,
	                filter: { field: "IdRegistro", operator: "eq", value: param.IdRegistro },
	                transport		: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6034",
							dataType	: "json"
					    }
	                },
	                
	                
	                schema 			: {
	                	model: {
	                		fields: {
	                			IdRegistro		: {type: "number"},
	                			Fecha			: {type: "sysdate"},
	                			Estado			: {type: "string"},
								Observacion		: {type: "string"},
	                			Usuario			: {type: "string"}
	                		}
	                	}
	                }
				}
		};
		
		var dsDistribInversion = new bcs.DataSource(defDistEstados);
		
		distest.DistribucionEstados = {
			dataSource: dsDistribInversion,
			
			columns: [
			     {field: "Fecha"		, title: "UI_FECHA_INGRESO", width: "50px", align: "right" },
			     {field: "Estado"		, title: "UI_ESTADO", width: "50px", align: "left"},
			     {field: "Observacion"	, title: "UI_OBSERVACION"  , width: "250px", align: "left"},
			     {field: "Usuario"		, title: "UI_USUARIO"		, width: "50px", align: "left"}
			],
			sortable	: true,
			pageable	: true,
			filterable	: false,
			height		: 200,
			selectable 	: "row",
			autoBind	: true,
			gridCreated : function(Distribucion){
				/*$.each(ParamDistri,function(key,value){
					DistribInversion.add({ 
						   FolioContratoSAAPV : value.FolioContratoSAAPV,
						   IdInstrumento 	  : value.IdInstrumento,
						   RazonSocial   	  : value.RazonSocial,
						   Nemotecnico		  : value.Nemotecnico,
						   MontoPesos		  : value.MontoPesos,
						   Porcentaje		  : value.Porcentaje,
						   IdEstado			  : value.IdEstado
						 });
				});*/
			}
		};
	});
}());