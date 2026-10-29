angular.module('ModuloPlataforma').factory('bcsConfiguracionVistaAutorizaciones',function($translate) {

	var obj = {};
	
	transporte = function(Grid) {
		if (Grid === 'Evento')
			url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1396";
		if (Grid === 'Etapa')
			url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1397";
		if (Grid === 'Autorizador')
			url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1398";		
		return {
			read : {
				url : url,
				dataType : "json"
			}
		};
	};  
	

	ModeloEvento= function() {
		return {
			model: {
				fields : {
					IdEmpresa 					: {type : "number"},
					IdEvento 					: {type : "number"},
					Fecha 						: {type : "sysdate"},
	        		HoraRegistro 				: {type : "sysdatetime"},
	        		HoraVigencia 				: {type : "sysdatetime"},
	        		IdEstado 					: {type : "string"},
	        		NombreEstado 				: {type : "string"},
	        		IdTipoEvento 				: {type : "string"},
	        		Autorizacion				: {type : "string"},
	        		Cliente 					: {type : "string"},
	        		Nemo 						: {type : "string"},
					Cantidad 					: {type : "number"},
	        		Precio 						: {type : "number"},
	        		Monto	 					: {type : "number"},
	        		IdMoneda 					: {type : "string"},
					IdUsuario					: {type : "number"},
	        		Usuario						: {type : "string"},
	        		IdUsuarioSuperAutorizador 	: {type : "number"},
	        		UsuarioSuperAutorizador		: {type : "string"},
	        		IdRolSuperAutorizador 		: {type : "number"},
	        		RolSuperAutorizador			: {type : "string"},
	        		IdUsuarioCambioEstado		: {type : "number"},
	        		CodigoUsuarioCambioEstado	: {type : "string"},
	        		Glosa 						: {type : "string"},
					Comentario 					: {type : "string"},
					IdGrupo						: {type : "number"},
					IdEtapaEvento				: {type : "number"},
	        		NombreEtapa 				: {type : "string"},
	        		OrdenEtapa 					: {type : "number"},
	        		IdEstadoEtapa				: {type : "string"},
	        		NombreEstadoEtapa 			: {type : "string"},
	        		MinimoAutorizan 			: {type : "string"},
	        		IdCliente					: {type : "number"},
					NumeroOperacionConceptoUI   : {type : "number"}
				}
			}
		};
	}; 
	

	ModeloEtapa = function() {
		return {
			model: {
    	        	fields:{
    	        		IdEtapaEvento           : {type : "number"},
    	        		IdEvento 				: {type : "number"},
    	        		IdEtapa  				: {type : "number"},
    	        		Codigo  				: {type : "string"},
    	        		Nombre   				: {type : "string"},
    	        		Orden         			: {type : "number"},
    	        		TipoAutorizacion        : {type : "string"},
    	        		MinimoAutorizan 		: {type : "number"},
    	        		IdUsuarioCambioEstado   : {type : "number"},
    	        		IdRolCambioEstado       : {type : "number"},
    	        		IdEstado 				: {type : "string"},
    	        		NombreEstado			: {type : "string"},
    	        		FechaHoraCambioEstado	: {type : "sysdatetime"}
    	        	} 
			}
		};
	}; 
	
	ModeloAutorizador = function() {
		return {
			model: {
    	        	fields:{
						IdAutorizadoEtapaEvento		: {type : "number"},
    	        		IdEtapaEvento 				: {type : "number"},
    	        		IdUsuario 					: {type : "number"},
    	        		CodigoUsuario 				: {type : "string"},
    	        		IdRol 						: {type : "number"},
    	        		NombreRol 					: {type : "string"},
    	        		IdUsuarioReemplaza 			: {type : "number"},
    	        		CodigoUsuarioReemplaza		: {type : "string"},
    	        		ReemplazaDesde 				: {type : "date"},
    	        		ReemplazaHasta 				: {type : "date"},
    	        		IdEstado 					: {type : "string"},
    	        		NombreEstado 				: {type : "string"},
    	        		IdEtapa						: {type : "number"}	,
    	        		FechaHoraCambioEstado   	: {type : "sysdatetime"},
    	        		ReemplazoAutoriza           : {type : "string"},
    	        		EsUsuarioSuperAutorizador	: {type : "string"},
    	        		EsRolSuperAutorizador		: {type : "string"}
    	        	} 
			}
		};
	}; 

	obj.dataSourceEvento = function(){
		return new bcs.DataSource ({
			type 		: "odata", //Los tipos posibles son: odata, call, array y object
			definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: true, // Ordenamiento de los datos en el servidor.
							sortable 			: true,
							sort                : { field: "IdEvento", dir: "desc" },
							pageSize 			: 50,
							transport 			: transporte('Evento'),
							schema 				: ModeloEvento()
							
						  }
		});
	};
	
	obj.dataSourceEventoSolicitada = function(usuario){
		console.log(usuario);
		return new bcs.newDataSource ({
			type 		: "odata", //Los tipos posibles son: odata, call, array y object
			definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: true, // Ordenamiento de los datos en el servidor.
							sortable 			: true,
							sort                : { field: "IdEvento", dir: "desc" },
							pageSize 			: 50,
							transport 			: transporte('Evento'),
							schema 				: ModeloEvento(),
							
						  }
		});
	};
	
	obj.dataSourceEtapa = function(dataItem){
		return new bcs.DataSource ({
			type 		: "odata", //Los tipos posibles son: odata, call, array y object
			definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: true, // Ordenamiento de los datos en el servidor.
							sort                : { field: "IdEtapa", dir: "asc" },
							pageSize 			: 50,
							transport 			: transporte('Etapa'),
							schema 				: ModeloEtapa(),
							filter 				: [{ field: "IdEvento", operator: "eq", value: Number(dataItem.IdEvento) }]
						  }
		});
	}; 
	
	obj.dataSourceAutorizador = function(dataItem){
		return new bcs.DataSource ({
			type 		: "odata", //Los tipos posibles son: odata, call, array y object
			definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: true, // Ordenamiento de los datos en el servidor.
							pageSize 			: 5,
							sort                : { field: "IdAutorizadoEtapaEvento", dir: "desc" },
							transport 			: transporte('Autorizador'),
							schema 				: ModeloAutorizador(),
							filter 				: [{ field: "IdEtapaEvento", operator: "eq", value: Number(dataItem.IdEtapaEvento) }]
						  }
		});
	};
	
	obj.columnasEvento = function() {
		return [ 
			{field: "IdEvento", title: "Secuencia",groupable: true, width: "70px" },
			{field: "HoraRegistro", title: "Registro", width: "100px",groupable: true, format: "{0:HH:mm}" },
			{field: "HoraVigencia", title: "Vigencia", width: "100px",groupable: true, format: "{0:HH:mm}" },
			{field: "Autorizacion", title: "Evento", width: "270px", groupable: true, },
			{field: "NombreEstado", title: "Estado Evento", align:"center", width: "80px",groupable: true },
			{field: "NombreEtapa", title: "Etapa",groupable: true, width: "100px",  },
			{field: "NombreEstadoEtapa", title: "Estado Etapa", align:"center", width: "80px",groupable: true },
			{field: "Usuario", title: "Usuario", width: "70px", groupable: true },
			{field: "CodigoUsuarioCambioEstado", title: "Usuario Autoriza", width: "90px", groupable: true },
			{field: "UsuarioSuperAutorizador", title: "Usuario Super Autorizador", width: "80px", groupable: true },
			{field: "RolSuperAutorizador", title: "Rol Super Autorizador", width: "80px", groupable: true },
			{field: "MinimoAutorizan", title: "Mínimo Autorizan", width: "70px",groupable: true },
			{field: "Glosa", title: "Glosa", width: "430px",groupable: true },
			{field: "Comentario", title: "Comentario", width: "430px",groupable: true },
			{field: "Nemo", title: "Referencia", width: "70px",groupable: true  },
			{field: "NumeroOperacionConceptoUI", title: "Número Operación",groupable: true, width: "70px" },
			{field: "Cliente", title: "Socio Negocio",groupable: true, width: "100px" },
			{field: "Cantidad", title: "Cantidad", width: "70px",groupable: true },
			{field: "Precio", title: "Precio", width: "70px",groupable: true},
			{field: "Monto", title: "Monto", width: "70px",groupable: true},
			{field: "IdMoneda", title: "Moneda", width: "70px",groupable: true}
		];
	};
	

	obj.columnasEtapa = function() {
		return  [
			{ field: "Orden", title: "", width: "50px" },
			{ field: "Nombre", title: "Etapa" , format: "{0:d}", width: "80px" },
			{ field: "NombreEstado", title: "Estado Etapa" , width: "65px", align:"center"},
			{ field: "FechaHoraCambioEstado", title: "Última Actualización" , width: "65px",align:"center", format: "{0:HH:mm}"}  
		];
	};
	
	
	obj.columnasAutorizador = function() {
		return  [
			{ field: "CodigoUsuario", title: "Usuario Autorizador" , width: "70px" },
			{ field: "NombreRol", title: "Rol Autorizador" , width: "60px" },
			{ field: "CodigoUsuarioReemplaza", title: "Usuario Reemplazo" , width: "70px"},
			{ field: "ReemplazaDesde", title: "Reemplaza Desde" , width: "70px", align:"center"},
			{ field: "ReemplazaHasta", title: "Reemplaza Hasta" , width: "70px", align:"center"},
			{ field: "NombreEstado", title: "Estado Autorización" , width: "70px", align:"center" },
			{ field: "FechaHoraCambioEstado", title: "Fecha Autorización" , width: "80px", align:"center", format: "{0:HH:mm}"},
			{ field: "ReemplazoAutoriza", title: "Reemplazo Autorizó" , width: "90px", align:"center"},
			{ field: "EsUsuarioSuperAutorizador", title: "Usuario Super Autorizador" , width: "90px", align:"center"},
			{ field: "EsRolSuperAutorizador", title: "Rol Super Autorizador" , width: "90px", align:"center"},
		];
	};
	
	return obj;
});
