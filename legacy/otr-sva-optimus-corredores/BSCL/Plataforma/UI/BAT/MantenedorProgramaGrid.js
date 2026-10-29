angular.module('ModuloPlataforma').factory('bcsMantenedorTareasGrid',function($translate) {

	var obj = {};
	
	//paso 1 
	transporte = function(Grid) {
		if (Grid === 'Programas')
			url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1536";
		if (Grid === 'LogPrograma')
			url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1537";	 
		return {
			read : {
				url : url,
				dataType : "json"
			}
		};
	};  
	
	//paso 2 --resumen Tareas Programadas 
	ModeloTareasProgramadas= function() {
		return {
			model: {
				fields : {
					
					
					 	 IdPrograma 						: {type: "number"}
						,IdGrupoParametro 					: {type: "number"}
						,IdAgenda 							: {type: "number"}
						,IdTipoTarea 						: {type: "string"}
						,Intento 							: {type: "number"}
						,IdUsuario 							: {type: "string"}
						,idEstado							: {type: "string"}
						,Resultado							: {type: "string"}
						,fechaCreacion						: {type: "datetime"}
						,fechaEjecucion						: {type: "datetime"}
						,fechaFin							: {type: "datetime"}
						,Nombre								: {type: "string"}
						,Descripcion						: {type: "string"}
						,CodigoUsuario						: {type: "string"}
						,IdProgramaReprocesado				: {type: "number"}
						,TipoSalida							: {type: "string"}
						,TipoSalidaUI						: {type: "code"}
						,FormatoSalida						: {type: "string"}
						,TieneLog							: {type: "string"}
						,RutaSalida							: {type: "string"}
						,RutaLog							: {type: "string"}
						,Comentario							: {type: "string"}						
				}
			}
		};
	}; 
	
	ModeloLogTareas= function() {
		return {
			model: {
				fields : {
					
					
					 IdPrograma 				: {type: "number"}
				 	,IdLog						: {type: "number"}
					,IdTipoLog					: {type: "string"} 
					,IdModulo					: {type: "string"} 
					,IdUsuario					: {type: "number"}
					,IdEmpresa					: {type: "number"} 
					,FechaLog					: {type: "sysdatetime"} 
					,PeriodoLog					: {type: "string"} 
					,IdUsuarioAtendido			: {type: "number"} 
					,IdCliente					: {type: "number"} 
					,EstadoCliente				: {type: "string"} 
					,NombreCliente				: {type: "string"} 
					,IdTipoDocumentoIdentidad	: {type: "string"} 
					,IdDocumentoIdentidad		: {type: "string"} 
					,IdPortafolio				: {type: "number"} 
					,EstadoPortafolio			: {type: "string"} 
					,IdRegistro					: {type: "number"} 
					,EstadoRegistro				: {type: "string"} 
					,IdTipoRegistro				: {type: "string"} 
					,IdTipoOperacion			: {type: "string"} 
					,Folio						: {type: "number"} 
					,Cantidad					: {type: "number"} 
					,PrecioTasa					: {type: "number"} 
					,Monto						: {type: "number"} 
					,Nemo						: {type: "string"} 
					,IdProceso					: {type: "number"} 
					,EstadoProceso				: {type: "string"} 
					,NombreProceso 				: {type: "string"}
					,NombreEstado				: {type: "string"}
					,Nombre						: {type: "string"}
					,FechaRegistro				: {type: "sysdatetime"}
					
					
					 	 
						
						
				}
			} 
		};
	}; 
  
	//paso 3 
	// resumen de tareas programadas
	obj.dataSourceResumenOperacion = function(){
		return new bcs.DataSource({
			type 		: "odata", //Los tipos posibles son: odata, call, array y object
			definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: true, // Ordenamiento de los datos en el servidor.
							sort 				: { field: "fechaCreacion", dir: "desc" },
							pageSize 			: 50,
							transport 			: transporte('Programas'),
							schema 				: ModeloTareasProgramadas()
							
						  }
		});
	};
	// resumen de log de tareas  
	obj.dataSourceLog = function(){
		return new bcs.DataSource({
			type 		: "odata", //Los tipos posibles son: odata, call, array y object
			definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: true, // Ordenamiento de los datos en el servidor.
							sort 				: { field: "FechaRegistro", dir: "desc" },
							pageSize 			: 50,
							transport 			: transporte('LogPrograma'),
				 			schema 				: ModeloLogTareas()
							
						  }
		});
	};
	    
	// paso 4 las columnas 
	//detalle operaciones 
	obj.columnasDetalleOperaciones = function() {
		return  [
	                    { field: "IdPrograma"						,title: 'UI_PROGRAMA'					, width: "50px", },
	                    { field: "Descripcion"						,title: 'UI_TIPO_PROGRAMA'				, width: "200px"},
	                    { field: "CodigoUsuario"					,title: 'UI_USUARIO'					, width: "44px"},
	                    { field: "fechaCreacion"					,title: 'UI_FECHA_CREACION'				, width: "64px"},
	                    { field: "fechaEjecucion"					,title: 'UI_FECHA_INICIO'				, width: "64px"},
	                    { field: "fechaFin"							,title: 'UI_FECHA_TERMINO'				, width: "64px"},
	                    { field: "Nombre"							,title: 'UI_ESTADO'						, width: "50px"},
	                    { field: "Resultado"						,title: 'UI_DESCRIPCION'				, width: "50px"},
	                    { field: "Comentario"						,title: 'UI_COMENTARIO'					, width: "80px"},
	                    { field: "TipoSalidaUI"						,title: 'UI_TIPO_SALIDA'				, width: "80px"},
	                    { field: "IdProgramaReprocesado"			,title: 'UI_PROGRAMA_REPROCESADO'		, width: "80px"},
	                   ];
	};
	
	obj.columnasDetalleLogTarea = function() {
		return  [
	                   
	                    { field: "FechaRegistro"		,title: 'UI_FECHA_EJECUCION'	, width: "64px"},
	                    { field: "NombreProceso"		,title: 'UI_DESCRIPCION'		, width: "63px"},
	                	];
	};

	return obj;
});
