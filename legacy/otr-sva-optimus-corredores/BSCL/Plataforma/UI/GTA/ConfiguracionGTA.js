angular.module('ModuloPlataforma').factory('bcsServiciosGta',['$resource',function($resource) {
		  var urlCrearExigibilidad	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=25";
		  var urlCrearCoberturaGarantia	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=29";
		  var urlModificarCoberturaGarantia	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=34";
	      return $resource("", {},
	      {
	    	  'CrearExigibilidad': 			{ method: "POST", url: urlCrearExigibilidad},
	    	  'CrearCoberturaGarantia': 	{ method: "POST", url: urlCrearCoberturaGarantia},
	    	  'ModificarCoberturaGarantia': { method: "POST", url: urlModificarCoberturaGarantia},
	       });
}]);


angular.module('ModuloPlataforma').factory('bcsConfiguracionGTA',function($translate) {

	var obj = {};
	

	transporte = function(Grid) {
		if (Grid === 'Cobertura')    url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1368"; //BSCL.Plataforma.BD.Vistas::ConsultarCoberturaGarantia		
		if (Grid === 'Exigibilidad') url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1369"; //BSCL.Plataforma.BD.Vistas::ConsultarExigenciaGarantia
		
		return {
			read : {
				url : url,
				dataType : "json"
			}
		};
	};

	//------------------------------------
	// EXIGIBILIDAD
	//------------------------------------	
	ModeloMantenedorExigibilidad = function() {
		return {
			model: {
				fields : {
					IdInstrumentoSubyacente 		: { type: "number"},
					IdBolsa 						: { type: "string"},
					IdTipoInstrumento 				: { type: "string"},
					IdTipoInstrumentoSubyacente 	: { type: "string"},
					IdTipoClasificacionRiesgo 		: { type: "string"},
					NombreTipoInstrumento 			: { type: "string"},
					Nemotecnico						: { type: "string"},
					ExigibilidadEntidad 			: { type: "number", editable: true ,validation: { min: 0, required: true }},
					ExigibilidadCorredora 			: { type: "number", editable: true ,validation: { min: 0, required: true }},
					IdExigibilidadGarantia 			: { type: "number"},
					ClasificacionRiesgo    			: { type: "string"},
					IdInstrumento          			: { type: "number"},
					IdEntidadClasificadoraRiesgo    : { type: "string"},
					NombreTipoInstrumentoSubyacente	: { type: "string"}
				}
			}
		};
	}; 
	 
	obj.dataSourceExigibilidad = new bcs.DataSource({
			type 		: "odata", //Los tipos posibles son: odata, call, array y object
			definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: true, // Ordenamiento de los datos en el servidor.
							sort 				: { field: "IdTipoInstrumento", dir: "desc" },
							pageSize 			: 50,
							transport 			: transporte('Exigibilidad'),
							schema 				: ModeloMantenedorExigibilidad()
						  }

	});
	 
	
	obj.columnasExigibilidad = function(local) {
		return  [
    			 {field: (local==='PE'?"IdEntidadClasificadoraRiesgo":"IdBolsa")						,title: 'UI_ENTIDAD'					,width: "66px"}
				 ,{field: "IdTipoInstrumento"			,title: 'UI_TIPO_INSTRUMENTO'			,width: "107px"}
				 ,{field: "NombreTipoInstrumento"		,title: 'UI_NOMBRE_TIPO_INSTRUMENTO'	,width: "105px"}
				 ,{field: "IdTipoInstrumentoSubyacente"	,title: 'UI_ID_INSTRUMENTO_SUBYACENTE'	,width: "121px"	}
				 ,{field: "NombreTipoInstrumentoSubyacente"	,title: 'UI_INSTRUMENTO_SUBYACENTE'	,width: "121px"	}
				 ,{field: "Nemotecnico"					,title: 'UI_INSTRUMENTO_EXIGIBLE'		,width: "167px"}
				 ,{field: "ClasificacionRiesgo"			,title: 'UI_TIPO_CLASIFICACION_RIESGO'	,width: "99px"}
				 ,{field: "ExigibilidadEntidad"			,title: 'UI_EXIGIBILIDAD_ENTIDAD'		,width: "128px"	,editable:true	}
				 ,{field: "ExigibilidadCorredora"		,title: 'UI_EXIGIBILIDAD_CORREDORA'		,width: "126px"	,editable:true	}
		        ];
	};

	//------------------------------------
    // COBERTURA DE GARANTIA
	//------------------------------------
	ModeloCobertura = function() {
		return {
			model: {
				fields : {					
					IdCoberturaGarantia 					: { type: "number"},					
					IdBolsa 								: { type: "string"},
				    IdTipoInstrumentoExigeGarantia 			: { type: "string"},
				    NombreTipoInstrumentoExigeGarantia 		: { type: "string"},
					IdInstrumentoExigeGarantia 				: { type: "number"},
					NombreInstrumentoExigeGarantia 			: { type: "string"},
					IdTipoInstrumentoComoGarantia 			: { type: "string"},
				    NombreTipoInstrumentoComoGarantia 		: { type: "string"},
				    IdTipoClasificacionRiesgo 				: { type: "string"},
				    NombreClasificacionRiesgo 				: { type: "string"},
					PlazoTope 								: { type: "number"},
					UnidadPlazo								: { type: "string"},
					NombreUnidadPlazo 						: { type: "string"},					
					CoberturaEntidad 						: { type: "number"},   
					CoberturaCorredora						: { type: "number"},
					IdEstado 								: { type: "string"},
					NombreEstado 							: { type: "string"},
					IdEntidadClasificadoraRiesgo            : { type: "string"},
					IdClasificacionRiesgo                   : { type: "number"},

				}
			}
		};
	};
  

	obj.dataSourceCobertura = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.
			sort 				: { field: "IdTipoInstrumentoComoGarantia", dir: "desc" },
			pageSize 			: 25,
			transport 			: transporte('Cobertura'),
			schema 				: ModeloCobertura()
		}
	});	

	
 
	obj.columnasCobertura = function() {
		return  [

				 {field: "NombreTipoInstrumentoExigeGarantia",title:'UI_TIPO_INSTRUMENTO_GARANTIZADO'   , width: "130px", groupable: false}
				,{field: "NombreInstrumentoExigeGarantia"    ,title:'UI_INSTRUMENTO_GARANTIZADO'        , width: "145px", groupable: false}
				,{field: "NombreTipoInstrumentoComoGarantia",title:'UI_INSTRUMENTO_COMO_GARANTIA'       , width: "145px", groupable: false}
				,{field: "NombreClasificacionRiesgo"   	    ,title:'UI_CLASIFICACION_RIESGO'       		, width: "75px", groupable: false}
				,{field: "IdEntidadClasificadoraRiesgo"     ,title:'UI_EMPRESA_CLASIFICACION_RIESGO'	, width: "75px", groupable: false}
				,{field: "CoberturaEntidad"            	    ,title:'UI_COBERTURA_ENTIDAD'    			, width: "75px", template : " <label> #:  util.formateaNumero(CoberturaEntidad,4) #</label>"}
				,{field: "CoberturaCorredora"        	    ,title:'UI_COBERTURA_CORREDOR'   			, width: "75px", template : " <label> #:  util.formateaNumero(CoberturaCorredora,4) #</label>"}									
				,{field: "PlazoTope"                 	    ,title:'UI_PLAZO_TOPE'               		, width: "70px", template: "#=PlazoTope# #=UnidadPlazo#" }							
				,{field: "NombreEstado"              	    ,title:'UI_ESTADO'                   		, width: "75px", groupable: false}
			];
	};
		
	return obj;
	
});




