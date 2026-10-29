angular.module('ModuloFacturacion').factory('bcsGridSocioDeNegocioGrid',['bcsComunes',
		function(bcsComunes)  
{
	
	var obj = {};
	
	obj.columnasSocioDeNegocio = function() {
		return  [	   	    
		     {field: "DocumentoIdentidad", title: "UI_DOCUMENTO_IDENTIDAD", width: "50px", align: "left" }    
    	   	,{field: "NombreCompleto", title: "UI_NOMBRE", width: "150px", align: "left" }
    	   	,{field: "CodigoPortafolio", title: "UI_PORTAFOLIO", width: "50px", align: "left" }
		];
	};	
	
	
	obj.modeloSocioDeNegocio = function() {
		return {
			model: {
				fields : {	
				           IdSocioDeNegocio          : { type : "number"},
					       DocumentoIdentidad        : { type: "string"},
	        		       NombreCompleto	         : { type: "string"},
	        		       CodigoPortafolio          : { type: "string"}
				         }
			}
		};
	};
	

	
    return obj;
    
}]);	