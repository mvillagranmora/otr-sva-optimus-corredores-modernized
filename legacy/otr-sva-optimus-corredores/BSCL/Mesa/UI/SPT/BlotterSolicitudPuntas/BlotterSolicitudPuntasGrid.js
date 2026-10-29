angular.module('ModuloMesa').factory('bcsBlotterSolicitudPuntasGrid',['bcsComunes','$rootScope',
		function(bcsComunes,$rootScope,$translate,$timeout,$modal,$state,$scope)  
{
	var obj = {};		
	
	modeloSolicitudes = function() {
		return {
    		model: {
    			fields: {
    				IdSolicitudPuntas				: { type: "number"},														
					FechaRegistro					: { type: "sysdate"},
					FechaVigencia			 		: { type: "sysdate"},
					FolioOperacion					: { type: "number"},
					IdTipoOperacion 				: { type: "string"},
					TipoOperacion					: { type: "string"},
					IdCliente						: { type: "number"},
					NombreCompleto					: { type: "string"},	
					NombreCompletoPortafolio		: { type: "string"},
					IdUsuarioSolicitud				: { type: "number"},
					UsuarioSolicitud				: { type: "string"},
					IdUsuarioAutorizador	 		: { type: "number"},
					UsuarioAutorizador 			 	: { type: "string"},
					TipoCambioCostoSolicitado		: { type: "number"},
					TipoCambioCostoAprobado			: { type: "number"},
					Monto							: { type: "number"},
					IdMoneda 						: { type: "string"},
					IdEstadoOperacion				: { type: "string"},
					NombreEstado					: { type: "string"},
					ObservacionSolicitud			: { type: "string"},
					ObservacionAutorizador			: { type: "string"},
        		} 
        	}
		};
	};
	

	obj.columnasSolicitudes = function() {
		return  [
    		 {field: "IdSolicitudPuntas", 				title: 'UI_ID_SOLICITUD_PUNTAS', 			width: "70px" }
    		,{field: "FechaRegistro", 					title: 'UI_FECHA_REGISTRO', 				width: "70px", groupable: true}
    		,{field: "FechaVigencia", 					title: 'UI_FECHA_VIGENCIA', 				width: "70px", groupable: true }
    		,{field: "FolioOperacion",					title: 'UI_FOLIO_OPERACION',				width: "80px", groupable: true , hidden: true}
    		,{field: "IdTipoOperacion", 				title: 'UI_TIPO_OPERACION', 				width: "50px", groupable: true , hidden: true}
    		,{field: "TipoOperacion", 					title: 'UI_TIPO_OPERACION', 				width: "40px", groupable: true }
    		,{field: "IdCliente",						title: 'UI_CLIENTE', 						width: "80px", groupable: true, hidden: true}
    		,{field: "NombreCompletoPortafolio",		title: 'UI_NOMBRE',							width: "120px", groupable: true	}    		
    		,{field: "Monto", 							title: 'UI_PRECIO', 						width: "90px", template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
    		,{field: "IdMoneda", 						title: 'UI_MONEDA', 						width: "75px", 	groupable: true }
    		,{field: "TipoCambioCostoSolicitado",		title: 'UI_TC_COSTO_SOLICITADO',			width: "90px", template : " <label> #:  (util.bcs_formato(TipoCambioCostoSolicitado,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
    		,{field: "TipoCambioCostoAprobado", 		title: 'UI_TC_COSTO_APROBADO', 				width: "90px", template : " <label> #:  (util.bcs_formato(TipoCambioCostoAprobado,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
	    	,{field: "NombreEstado", 					title: 'UI_ESTADO', 						width: "75px", 	groupable: true	} 
	    	,{field: "IdEstadoOperacion",				title: 'UI_ESTADO', 						width: "75px", 	groupable: true, hidden: true}  
	    	,{field: "IdUsuarioSolicitud", 				title: 'UI_USUARIO_SOLICITUD',	 			width: "50px", groupable: true, hidden: true }
    		,{field: "UsuarioSolicitud", 				title: 'UI_USUARIO_SOLICITUD', 				width: "120px", groupable: true }
    		,{field: "IdUsuarioAutorizador", 			title: 'UI_USUARIO_AUTORIZADOR', 			width: "80px", groupable: true, hidden: true }
    		,{field: "UsuarioAutorizador", 				title: 'UI_USUARIO_AUTORIZADOR', 			width: "120px", groupable: true } 
		];
	};
	
	
	return obj;
}]);