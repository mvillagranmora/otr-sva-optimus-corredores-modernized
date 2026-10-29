 angular.module('ModuloPlataforma').factory('bcsMantenedorEventosGrid', function($translate) {

	var obj = {};
	
	 
	
	//paso 1    

	


	
	
	
	ModeloConsultarNotificacionesDetalle= function() {
		return {
			model: {
				fields : {

 						 IdNotificacion 				: {type: "number"}
						,IdNotificacionDetalle			: {type: "number"}
						,IdEvento 						: {type: "number"}
						,Despachado						: {type: "code"}
						,IdEstado						: {type: "code"}
						,CodigoTipoEvento				: {type: "string"}
						,Descripcion					: {type: "string"}
 					
				}
			}
		};
	}; 
	
	ModeloTipoEventos= function() {
		return {
			model: {
				fields : {

						 CoditoTipoEvento					: {type: "string"}
						,DireccionEvento 					: {type: "string"}
						,Descripcion						: {type: "string"}
						 
					
				}
			}
		};
	}; 
	
	ModeloConfiguracionEvento= function() {
		return {
			model: {
				fields : {
					
					
					IdConfiguracionEvento 		: {type: "number"}
			 		,Descripcion				: {type: "string"}
					,IdUsuario					: {type: "number"} 
					,FechaCreacion				: {type: "date"}
					,IdEmpresa					: {type: "number"} 
					,IdEstado					: {type: "string"}
					,IdEstadoUI					: {type: "string"}
					,NombreFormaDespachoUI		: {type: "string"}
					,NombreTiempoDespachoUI		: {type: "string"}
					,PermitePersonalizar		: {type: "string"} 
					,FormaDespacho				: {type: "string"} 
					,TiempoDespacho				: {type: "string"} 
					,ConfirmacionDespacho		: {type: "string"} 
					,ConfirmacionDespachoUI		: {type: "string"} 
					,FechaVencimiento			: {type: "date"} 
					,FechaRecordatorio			: {type: "date"} 
					,IdPlantilla				: {type: "number"} 
					,IdPlantillaRecordatorio	: {type: "number"}
					,CodigoTipoEvento			: {type: "string"}
					
				}
			} 
		};
	};  
  
	
	obj.datasourceConsultarNotificacionesRecibidas = function(){
		return {
			type 		: "odata", //Los tipos posibles son: odata, call, array y object
			definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: true, // Ordenamiento de los datos en el servidor.
							sort 				: { field: "IdNotificacion", dir: "desc" },
							pageSize 			: 50,
							transport 			: transporte('NotificacionRecibidas'),
							schema 				: ModeloConsultarNotificaciones(),
							filter : {
										logic: "or",
										filters : [
									           {field : "DireccionEvento",operator: "eq", value : 'EMPEMP'},  
									           {field : "DireccionEvento",operator: "eq", value : 'SYSEMP'},
									           {field : "DireccionEvento",operator: "eq", value : 'CLIEMP'},
									          ]
									 }
							 
							
				}
		};
	};
	
	obj.datasourceConsultarNotificacionesEnviadas = function(){
		return {
			type 		: "odata", //Los tipos posibles son: odata, call, array y object
			definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: true, // Ordenamiento de los datos en el servidor.
							sort 				: { field: "IdNotificacion", dir: "desc" },
							pageSize 			: 50,
							transport 			: transporte('NotificacionEnviadas'),
							schema 				: ModeloConsultarNotificaciones()
							
							 
							
						}
		};
	};
	
 
 
	//paso 3 
	obj.dataSourceEvento = function(){
		return {
			type 		: "odata", //Los tipos posibles son: odata, call, array y object
			definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: true, // Ordenamiento de los datos en el servidor.
							sort 				: { field: "CodigoTipoEvento", dir: "desc" },
							pageSize 			: 50,
							transport 			: transporte('TipoEvento'),
							schema 				: ModeloTipoEventos(),
							/*filtersFn 			: function (row) {
												  var filterDetalle = new Array();
												  filterDetalle.push({ field: "IdConfiguracionEvento", operator: "eq", value: row.IdConfiguracionEvento });
												  return filterDetalle;
												}*/
							
						  }
		};
	};
	// resumen de log de tareas  
	obj.dataSourceConfiguracionEvento = function(){
		return {
			type 		: "odata", //Los tipos posibles son: odata, call, array y object
			definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: true, // Ordenamiento de los datos en el servidor.
							sort 				: { field: "FechaCreacion", dir: "desc" },
							pageSize 			: 50,
							transport 			: transporte('ConfiguracionEventos'),
							schema 				: ModeloConfiguracionEvento()
							
						}
		};
	};
	// paso 4 las columnas 
	//detalle operaciones 
	obj.columnasEvento = function() {  
		return  [
	                    { field: "DescripcionTipoEvento"		,title: 'UI_NOMBRE_EVENTO'				, width: "321px", attributes:{style:"text-align:right;"}, headerAttributes:{style:"text-align: center;"} },
	                    { field: "DireccionEvento"				,title: 'UI_DIRECCION_EVENTO'			, width: "321px"},
 	                    { field: "IdEstado"			   			,title: 'UI_ESTADO'					,  attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: center;"} },
	            
	                    ];
	};
	obj.columnasEventoAsociados = function() {  
		return  [
	                    { field: "Descripcion"				    ,title: 'UI_NOMBRE_EVENTO'				, width: "264px", attributes:{style:"text-align:right;"}, headerAttributes:{style:"text-align: center;"} },
	                    { field: "DireccionEvento"				,title: 'UI_DIRECCION_EVENTO'			,  attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: center;"} },
  	            
	                    ];
	};
	obj.columnasEventoDetalle = function() {  
		return  [
	                    { field: "Llave"				    ,title: 'UI_LLAVE'				, width: "50px", attributes:{style:"text-align:right;"}, headerAttributes:{style:"text-align: center;"} },
   	                	
	            ];
	};      
	
	obj.columnasConfiguracionEvento = function() {
		return  [    
	                   
	                    { field: "Descripcion"					,title: $translate.instant('UI_NOMBRE_NOTIFICACION')		, width: "117px"},
	                    { field: "PermitePersonalizar"			,title: $translate.instant('UI_PERSONALIZAR')				, width: "117px"},
	                    { field: "NombreFormaDespachoUI"		,title: $translate.instant('UI_FORMA_DESPACHO')				, width: "117px"},
	                    { field: "NombreTiempoDespachoUI"		,title: $translate.instant('UI_TIEMPO_DESPACHO')			, width: "117px"},
	                    { field: "ConfirmacionDespachoUI"		,title: $translate.instant('UI_CONFIRMACION_DESPACHO')		, width: "117px"},
	                    { field: "ControlProceso"        		,title: $translate.instant('UI_CONTROL_PROCESO')    		, width: "100px",  template: kendo.template("#if (ControlProceso==='S') {##='SI'##} else {##='NO'##}#")},
	                    { field: "FechaVencimiento"				,title: $translate.instant('UI_FECHA_VENCIMIENTO')			, width: "117px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: center;"},format:"{0:d}",template: kendo.template("#if (util.dateisdefault(FechaVencimiento) ) {##} else {##=util.formatoSecondate(FechaVencimiento)##}#") },
	                    { field: "FechaRecordatorio"			,title: $translate.instant('UI_FECHA_RECORDATORIO')			, width: "117px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: center;"},format:"{0:d}",template: kendo.template("#if (util.dateisdefault(FechaRecordatorio) ) {##} else {##=util.formatoSecondate(FechaRecordatorio)##}#") },
	                    { field: "IdEstadoUI"					,title: $translate.instant('UI_ESTADO')			        	 },
	                    
	           ];
	};
	
 	 
	obj.columnasConsultarNotificaciones = function() {
		return  [
		         
	                   
                   { field: "DescripcionConfiguracion"		,title: 'UI_NOTIFICACION'					, width: "140px"},
                   { field: "Asunto"						,title: 'UI_ASUNTO'		        			, width: "145px"},
                   { field: "FechaCreacion"					,title: 'UI_FECHA_CREACION'					, width: "145px", headerAttributes:{style:"text-align: center;"},format:"{0:d}",template: kendo.template("#if (util.dateisdefault(FechaCreacion))  {##} else {##=util.formatoSecondate(FechaCreacion)##}#")   },
                   { field: "Enviado"						,title: 'UI_ENVIADO'						, width: "100px"},
                   { field: "FechaEnviado"					,title: 'UI_FECHA_DESPACHO'					, width: "145px",  headerAttributes:{style:"text-align: center;"},format:"{0:d}",template: kendo.template("#if (util.dateisdefault(FechaEnviado) ) {##} else {##=util.formatoSecondate(FechaEnviado)##}#")},
                   { field: "IdDestinatario"				,title: 'UI_IDENTIDAD_DESTINATARIO'			, width: "100px" },
	               { field: "NombreDestinatario"			,title: 'UI_NOMBRE_DESTINATARIO'			, width: "170px"},
	               { field: "Direccion"						,title: 'UI_DIRECCION'						,  width: "100px"},
	               { field: "ConfirmacionDespacho"			,title: 'UI_REQUIERE_CONFIRMACION'			, width: "100px"},
                   { field: "Confirmado"					,title: 'UI_CONFIRMADO'					, width: "100px"},
	               { field: "AccionConfirmada"				,title: 'UI_RESPUESTA_DESTINATARIO'				, width: "100px"},
	               { field: "Nemo"                          ,title: 'UI_NEMO'                           , width: "150px" },
                   { field: "FormaDespacho"					,title: 'UI_FORMA_DESPACHO'					, width: "100px"},
                   { field: "TiempoDespacho"				,title: 'UI_TIEMPO_DESPACHO'				, width: "100px"},
                   { field: "FechaDespachoRecordatorio"		,title: 'UI_FECHA_DESPACHO_RECORDATORIO'	, width: "145px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: center;"},format:"{0:d}",template: kendo.template("#if (util.dateisdefault(FechaDespachoRecordatorio) ) {##} else {##=util.formatoSecondate(FechaDespachoRecordatorio)##}#") },
                   { field: "Enviar"						,title: 'UI_ENVIAR'							, width: "100px"},                  
                   { field: "FechaEnviado"					,title: 'UI_FECHA_ENVIADO'					, width: "145px", attributes:{style:"text-align:center;white-space:nowrap;"}, headerAttributes:{style:"text-align: center;"},format:"{0:d}",template: kendo.template("#if (util.dateisdefault(FechaEnviado) ) {##} else {##=util.formatoSecondate(FechaEnviado)##}#")},
 	               { field: "UsuarioEnviado"				,title: 'UI_USUARIO_ENVIADO'				, width: "100px"},
 	               { field: "IdRemitente"					,title: 'UI_IDENTIDAD_REMITENTE'			, width: "100px"},
	               { field: "NombreRemitente"				,title: 'UI_NOMBRE_REMITENTE'				, width: "170px" },
	               { field: "ReEnviar"						,title: 'UI_REENVIAR'						, width: "100px"},
	               { field: "ReEnviado"						,title: 'UI_REENVIADO'						, width: "100px"},
	               { field: "FechaReEnviado"				,title: 'UI_FECHA_REENVIADO'				, width: "145px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: center;"},format:"{0:d}",template: kendo.template("#if (util.dateisdefault(FechaReEnviado) ) {##} else {##=util.formatoSecondate(FechaReEnviado)##}#") },
	               { field: "UsuarioReEnviado"				,title: 'UI_USUARIO_REENVIADO'				, width: "100px"},
	               { field: "ConfirmacionDespacho"			,title: 'UI_CONFIRMACION_DESPACHO'			, width: "100px"},
		           { field: "TokenConfirmacion"				,title: 'UI_TOKEN_CONFIRMACION'				, width: "100px"},
                   { field: "Confirmado"					,title: 'UI_CONFIRMACION'					, width: "100px"},
                   { field: "FechaConfirmado"				,title: 'UI_FECHA_CONFIRMADO'				, width: "145px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: center;"},format:"{0:d}",template: kendo.template("#if (util.dateisdefault(FechaConfirmado) ) {##} else {##=util.formatoSecondate(FechaConfirmado)##}#") },
                   { field: "UsuarioConfirma"				,title: 'UI_PERSONA_CONFIRMA'				, width: "200px"},
                   { field: "FechaVencimiento"				,title: 'UI_FECHA_VENCIMIENTO'				, width: "145px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: center;"},format:"{0:d}",template: kendo.template("#if (util.dateisdefault(FechaVencimiento) ) {##} else {##=util.datefix(FechaVencimiento)##}#") },
                   { field: "FechaRecordatorio"				,title: 'UI_FECHA_RECORDATORIO'				, width: "145px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: center;"},format:"{0:d}",template: kendo.template("#if (util.dateisdefault(FechaRecordatorio) ) {##} else {##=util.datefix(FechaRecordatorio)##}#") },
                   { field: "Observacion"					,title: 'UI_OBSERVACION'					, width: "100px"},
 	               { field: "Descartado"					,title: 'UI_DESCARTADO'						, width: "100px"},
	               { field: "UsuarioDescartado"				,title: 'UI_USUARIO_DESCARTE'				, width: "100px" },
	               { field: "FechaDescarte"					,title: 'UI_FECHA_DESCARTE'					, width: "100px",format:"{0:d}",template: kendo.template("#if (util.dateisdefault(FechaDescarte) ) {##} else {##=util.formatoSecondate(FechaDescarte)##}#")},
	               { field: "Secuencia"						,title: 'UI_SECUENCIA'						, width: "100px"},
				   { field: "NombreAdjunto"					,title: 'UI_NOMBRE_ARCHIVO_ADJUNTO'			, width: "270px"},
	               { field: "LogEnvio"						,title: 'UI_LOG_ENVIO'						, width: "500px"},
	               
                ];   
	};
	
	obj.columnasConsultarNotificacionesRecibidas = function() {
		return  [
		         
	                   
                   { field: "DescripcionConfiguracion"		,title: 'UI_NOTIFICACION'					, width: "140px"},
                   { field: "Asunto"						,title: 'UI_ASUNTO'		        			, width: "145px"},
                   { field: "FechaDespachoOrginal"			,title: 'UI_FECHA_DESPACHO_ORIGINAL'		, width: "145px", attributes:{style:"text-align:center;white-space:nowrap;"}, headerAttributes:{style:"text-align: center;"},format:"{0:d}",template: kendo.template("#if (util.dateisdefault(FechaDespachoOrginal))  {##} else {##=util.formatoSecondate(FechaDespachoOrginal)##}#")   },
                   { field: "Enviado"						,title: 'UI_ENVIADO'						, width: "100px"},
                   { field: "FechaEnviado"					,title: 'UI_FECHA_ENVIADO'					, width: "145px", attributes:{style:"text-align:center;white-space:nowrap;"}, headerAttributes:{style:"text-align: center;"},format:"{0:d}",template: kendo.template("#if (util.dateisdefault(FechaEnviado) ) {##} else {##=util.formatoSecondate(FechaEnviado)##}#")},
	               { field: "IdDestinatario"				,title: 'UI_IDENTIDAD_DESTINATARIO'			, width: "100px" },
	               { field: "NombreDestinatario"			,title: 'UI_NOMBRE_DESTINATARIO'			, width: "170px"},
	               { field: "Direccion"						,title: 'UI_DIRECCION'						,  width: "100px"},
	               { field: "ConfirmacionDespacho"			,title: 'UI_REQUIERE_CONFIRMACION'			, width: "100px"},
                   { field: "AccionConfirmada"				,title: 'UI_RESPUESTA_DESTINATARIO'				, width: "100px"},

                   { field: "IdRemitente"					,title: 'UI_IDENTIDAD_REMITENTE'			, width: "100px"  },
                   { field: "NombreRemitente"				,title: 'UI_NOMBRE_REMITENTE'				, width: "170px" },

                   { field: "FechaDespachoRecordatorio"		,title: 'UI_FECHA_DESPACHO_RECORDATORIO'	, width: "145px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: center;"},format:"{0:d}",template: kendo.template("#if (util.dateisdefault(FechaDespachoRecordatorio) ) {##} else {##=util.formatoSecondate(FechaDespachoRecordatorio)##}#") },
                   { field: "Enviar"						,title: 'UI_ENVIAR'							, width: "100px"},                  
 	               { field: "UsuarioEnviado"				,title: 'UI_USUARIO_ENVIADO'				, width: "100px"},
	               { field: "ReEnviar"						,title: 'UI_REENVIAR'						, width: "100px"},
	               { field: "ReEnviado"						,title: 'UI_REENVIADO'						, width: "100px"},
	               { field: "FechaReEnviado"				,title: 'UI_FECHA_REENVIADO'				, width: "145px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: center;"},format:"{0:d}",template: kendo.template("#if (util.dateisdefault(FechaReEnviado) ) {##} else {##=util.formatoSecondate(FechaReEnviado)##}#") },
	               { field: "UsuarioReEnviado"				,title: 'UI_USUARIO_REENVIADO'				, width: "100px"},
		           { field: "TokenConfirmacion"				,title: 'UI_TOKEN_CONFIRMACION'				, width: "100px"},
                   { field: "Confirmado"					,title: 'UI_PENDIENTE_CONFIRMACION'					, width: "100px"},
                   { field: "FechaConfirmado"				,title: 'UI_FECHA_CONFIRMADO'				, width: "145px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: center;"},format:"{0:d}",template: kendo.template("#if (util.dateisdefault(FechaConfirmado) ) {##} else {##=util.formatoSecondate(FechaConfirmado)##}#") },
                   { field: "Observacion"					,title: 'UI_OBSERVACION'					, width: "100px"},
                   { field: "UsuarioConfirmado"				,title: 'UI_USUARIO_CONFIRMA'				, width: "100px"},
                   { field: "FormaDespacho"					,title: 'UI_FORMA_DESPACHO'					, width: "100px"},
                   { field: "TiempoDespacho"				,title: 'UI_TIEMPO_DESPACHO'				, width: "100px"},

                   
                   
                   
                ];
	};
	
	obj.columnasConsultarNotificacionesDetalle = function() {
		return  [
		         
	                   
		           { field: "IdNotificacion"				,title: 'UI_NOTIFICACION'			, width: "307px"},
                   { field: "Descripcion"					,title: 'UI_EVENTO'					, width: "307px"},
                   { field: "Despachado"					,title: 'UI_DESPACHADO'				 },

	            
                ] ;
	};
	
	obj.columnasConsultarConfirmarRechazarNotificaciones = function() {
		return  [
		         
	                   
                   { field: "CodigoTipoEvento"				,title: 'UI_CODIGO_TIPO_EVENTO'		, width: "145px"},
                   { field: "FechaIngreso"					,title: 'UI_FECHA_INGRESO'			, width: "145px" },
                   { field: "IdEstado"  					,title: 'UI_ESTADO'     			, width: "145px" },
                   { field: "CantidadEventos"				,title: 'UI_CANTIDAD_EVENTO'		, width: "145px" },
                   { field: "IdEmpresa" 					,title: 'UI_EMPRESA'    			, width: "145px" },
                   { field: "IdOrigenEvento"				,title: 'UI_ORIGEN_EVENTO'			, width: "145px" },
                   { field: "DescripcionOrigen"				,title: 'UI_DESCRIPCION_ORIGEN'		, width: "145px" }, 
                   { field: "SecuenciaOrigen" 				,title: 'UI_SECUENCIA_ORIGEN'    	, width: "145px" }, 
                   { field: "TipoEventoOrigen"				,title: 'UI_TIPO_EVENTO_ORIGEN'		, width: "145px" },
				 
                ];   
	};
    
	obj.columnasConsultarDetalleEventos = function() {
		return  [
		         
	                   
                   { field: "CodigoTipoEvento"				,title: 'UI_CODIGO_TIPO_EVENTO'		, width: "145px"},
                   { field: "FechaCreacion"					,title: 'UI_FECHA_CREACION'			, width: "145px"},
                      
                ];   
	};
	
	obj.columnasConsultarNotificados = function() {
		return  [
		         
	                   
                   { field: "Direccion"				,title: 'UI_DIRECCION_ELECTORNICA'		, width: "50px"},
                   { field: "TipoDestinatario"		,title: 'UI_TIPO_DESTINATARIO'			, width: "50px"},
                      
                ];   
	};
	
	return obj; 

} );
