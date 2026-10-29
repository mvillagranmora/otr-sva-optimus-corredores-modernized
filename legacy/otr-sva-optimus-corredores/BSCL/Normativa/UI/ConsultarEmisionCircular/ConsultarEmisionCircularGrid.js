angular.module('ModuloNormativa').factory('bcsBlotterEmisionCircular',['bcsComunes',
		function(bcsComunes)  
{
	var obj = {};	
	

	transporteGR = function() {
		// vistas que utiliza cada grilla
		//if (Grid === 'SolicitudCircular')
		url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1482";
		return {
			read : {
				url : url,
				dataType : "json"
			}
		};
	};
	


	modeloSolicitudCircular = function() {
		return {
    		model: {
    			fields: {					
    				IdSolicitudCircular			: { type: "number"},
    				IdTipoCircular				: { type: "string"},
    				FechaIngresoSolicitud		: { type: "date"},
    				//FechaVencimientoSolicitud	: { type: "date"},
    				IdUsuario					: { type: "number"},
    				EstadoSolicitud				: { type: "string"},
    				Observaciones				: { type: "string"},
    				IdTipoDocumento				: { type: "string"},
    				IdProceso					: { type: "number"},
    				Rut					    	: { type: "string"},
    				Cliente					    : { type: "string"},
    				//FechaInicio				: { type: "sysdate"},
    				//FechaFin					: { type: "sysdate"},
    				ListaRut					: { type: "number"},
    				ListaRutAut					: { type: "number"},
    				ListaNemo					: { type: "number"},
    				Periodicidad				: { type: "string"},
    				RutaArchivo					: { type: "string"},
    				NombreArchivo				: { type: "string"},
    				NumeroRegistros				: { type: "number"},
    				CodigoUsuario				: { type: "string"},
    				EsPauta						: { type: "string"},
    				RutaLog					    : { type: "string"},
    				IdEstado				    : { type: "string"},
    			} 
        	}
		};
	};

	//data source solicitud circular
	obj.dataSourceSolicitudCircular = function() {
		return new bcs.DataSource( {
			type 		: "odata", //Los tipos posibles son: odata, call, array y object
			definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: true, // Ordenamiento de los datos en el servidor.
							sortable 			: true,
							columnMenu			: false,
							//filter 				: { field: "IdTipoCircular", operator: "eq" },
							pageSize 			: 25,
							transport 			: transporteGR(),
							schema 				: modeloSolicitudCircular() 
						  }
		});
		
	}; 

	obj.columnasCircular = function() {
		return  [
		{field: "FechaIngresoSolicitud", 			title: 'Fecha Ing. Solicitud', 						width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
		,{field: "IdSolicitudCircular", 			title: 'Secuencia Solicitud', 						width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "IdTipoCircular",					title: 'Tipo Circular',								width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "IdTipoDocumento",					title: 'Tipo Documento',							width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "Rut", 						    title: 'Rut', 										width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "Cliente", 						title: 'Cliente', 									width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "CodigoUsuario", 					title: 'Cod. Usuario', 								width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
		,{field: "EstadoSolicitud", 				title: 'Estado Solicitud', 							width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }
		,{field: "NumeroRegistros", 				title: 'Num. Registros', 							width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }
		,{field: "NombreArchivo", 					title: 'Nombre Archivo', 							width: "100px", template : kendo.template("#if (NumeroRegistros === 0 || NumeroRegistros === '') {#Sin Archivo#}else{##=NombreArchivo##}#") }
		,{field: "RutaArchivo",                     title: 'Ruta Archivo', 								width: "100px" , template : kendo.template("#if (NumeroRegistros === 0 || NumeroRegistros === '') {#-#}else{#<a class='fileDownload'>Descargar</a> #}#") }
		,{field: "Periodicidad", 					title: 'Periodicidad', 								width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
		,{field: "IdProceso", 			    		title: 'Proceso', 								    width: "80px",  template : kendo.template("#if (IdProceso === 0 || IdProceso === '' || IdProceso === null ) {#-#}else{##=IdProceso##}#") }
		,{field: "EsPauta", 			    		title: 'Pauta', 								    width: "40px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		,{field: "RutaLog",                         title: 'Log', 									    width: "100px" , template : kendo.template("#if (NumeroRegistrosLog === '0' || NumeroRegistrosLog === '') {#Sin Errores#}else{#<a class='fileDownload2'>Descargar</a> #}#") }
		];
	};  
	
	obj.columnasDetalleSolicitudCircular = function() {
		return [
		        {field: "IdSolicitudCircular", 			title: 'UI_ID_SOLICITUD_CIRCULAR',  		width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} },
		        {field: "IdTipoCircular", 				title: 'UI_ID_TIPO_CIRCULAR', 				width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} },
		        {field: "FechaIngresoSolicitud", 		title: 'UI_FECHA_INGRESO_SOLICITUD',		width: "145px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} },
		        {field: "FechaVencimientoSolicitud", 	title: 'UI_FECHA_VENCIMIENTO_SOLICITUD', 	width: "145px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} },
		        {field: "EstadoSolicitud", 				title: 'UI_ESTADO_SOLICITUD', 				width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} },
		        {field: "Observaciones", 				title: 'UI_OBSERVACIONES',					width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} },
		        {field: "IdDocumento", 					title: 'UI_ID_DOCUMENTO', 					width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} },
		        {field: "Cliente", 					    title: 'UI_ID_CLIENTE', 					width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} },
		        {field: "FechaInicio", 					title: 'UI_FECHA_INICIO',					width: "145px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} },
		        {field: "FechaFin", 					title: 'UI_FECHA_TERMINO', 				width: "145px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} },
		        {field: "ListaRut", 					title: 'UI_LISTA_RUT', 					width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} },
		        {field: "ListaRutAut", 					title: 'UI_LISTA_RUT_AUTORIZADO', 			width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} },
		        {field: "ListaNemo", 					title: 'UI_LISTA_NEMO', 					width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} },
		        {field: "Periodicidad", 				title: 'UI_PERIODICIDAD', 					width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} },
		        {field: "RutaArchivo", 					title: 'UI_RUTA_ARCHIVO', 					width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} },//template: "<a href='www.google.cl'> fddsfsd </a>"},
		        {field: "NombreArchivo", 				title: 'UI_NOMBRE_ARCHIVO',				width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} },
		        {field: "NumeroRegistros", 				title: 'UI_NUMERO_REGISTROS',				width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} },
		        {field: "CodigoUsuario", 				title: 'UI_USUARIO', 						width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
		        ];
	};
	
	/** 
	 * Parametros Solicitud Circular
	 */
	transporteParametrosSolicitudCircular = function() {
		return {
			read : {
				url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1413",
				dataType : "json"
			}
		};
	};
	
	modeloParametrosSolicitudCircular = function() {
		return {
			model: {
				fields: {
					IdSolicitudParametros 	: { type: "number"},
					IdSolicitudCircular		: { type: "number"},
					TipoParametro			: { type: "string"},
					Valor					: { type: "string"},
					DocumentoIdentidad		: { type: "string"},
					RazonSocial				: { type: "string"},
					Nemotecnico				: { type: "string"},
					NombreClasificacionTipoInstrumento	: { type: "string"},
				}
			}
		};
	};
	 
	definicionODataParametrosSolicitudCircular =  function() {
		return {
			serverFiltering	: true,
			serverPaging	: true,
			serverSorting	: true,
			pageSize		: 20,
			sort			: {field: "IdSolicitudParametros", dir: "asc" },
			transport		: transporteParametrosSolicitudCircular(),
			schema			: modeloParametrosSolicitudCircular(),
		};
	};
	
	obj.dataSourceParametrosSolicitudCircular = function () {
		return new bcs.DataSource ({
			type		: "odata",
			definition	: definicionODataParametrosSolicitudCircular()
		});
	};
	
	obj.columnasParametrosSolicitudCircular = function(){
		return [
		        	{field: "Valor", title: "UI_RUT", width: "80px" }
		        ];
	};
	
	
	obj.columnasParametrosRut= function(){
		return [
		        	{field: "DocumentoIdentidad", title: "UI_RUT", width: "80px" },
		        	{field: "RazonSocial", title: "UI_RAZON_SOCIAL", width: "80px" }
		        ];
	};
	
	obj.columnasParametrosNemo= function(){
		return [
		        	{field: "Nemotecnico", title: "UI_NEMOTECNICO", width: "80px" },
		        	{field: "NombreClasificacionTipoInstrumento", title: "UI_CLASIFICACION_TIPO_INSTRUMENTO", width: "80px" }
		        ];
	};
	 
	
	return obj;
	
}]);	

