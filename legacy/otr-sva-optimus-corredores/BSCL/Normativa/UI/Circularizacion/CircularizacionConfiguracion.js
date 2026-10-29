	
angular.module('ModuloNormativa').factory('bcsConfiguracionCircularizacion',['bcsComunes',function(bcsComunes)  
{	
	var objCircu = {};
	
	transporteParametros = function() {
		return {
			read : {
				url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=3020",
				dataType : "json"
			}
		};
	};
	
	getSchemaParametros = function() {
		return {
			model: {
				fields : {
					IdParametro  			: { type: "number"},
					Esquema		 			: { type: "string"},
					Version 	 			: { type: "string"},
					Descripcion 	 		: { type: "string"},
					Valor 	 				: { type: "string"},
					FechaIngreso			: { type: "date"},
					FechaCreacion			: { type: "date"},
					FechaModificacion		: { type: "date"},
					IdEstado				: { type: "string"},
					IdEstadoAutorizacion	: { type: "string"},
					IdUsuarioCreador 		: { type: "number"},
		  				 }
					}
				};
		};
		
	
	definicionODataParametros =  function() {
		return {
			serverFiltering	: true,
			serverPaging	: true,
			serverSorting	: true,
			pageSize		: 20,
			//sort			: {field: "IdSolicitudParametros", dir: "asc" },
			transport		: transporteParametros(),
			schema			: getSchemaParametros(),
		};
	};
	
	objCircu.dataSourceParametros = function () {
		return new bcs.DataSource ({
			type		: "odata",
			definition	: definicionODataParametros()
		});
	};
			
		
	return objCircu;
}]);