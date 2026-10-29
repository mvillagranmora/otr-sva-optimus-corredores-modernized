angular.module('ModuloPlataforma').factory('bcsCierreModulosGrid',['bcsComunes', 
       function(bcsComunes){
	var obj = {};
	
	//MODULOS
	modeloModulos = function(){
		return {
			model: {
				fields: {
					IdCREModuloEmpresa	: { type: "number"},
					IdEmpresa			: { type: "number"},
					NombreEmpresa		: { type: "string"},
					IdModulo			: { type: "string"},
					Nombre				: { type: "string"},
					NombreModuloEmpresa	: { type: "string"},
					TipoApertura		: { type: "string"},
					HoraApertura		: { type: "string"},
					TipoCierre			: { type: "string"},
					HoraCierre			: { type: "string"},
					IdEstado			: { type: "string"},
					TipoEstado			: { type: "string"},
					NombreTipoEstado	: { type: "string"},
					Hora				: { type: "string"},
				}
			}
		};
	};
	
	obj.dataSourceModulos = function (){
		return new bcs.DataSource ({
			type		: "odata",
			definition	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: true, // Ordenamiento de los datos en el servidor.
							sort 				: { field: "IdCREModuloEmpresa", dir: "asc" },
							pageSize 			: 50,
							transport 			: {read : {
															url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1558",
															dataType : "json"
												          }
												  },
							schema 				: modeloModulos()
						  }
		});
	};
	
	obj.columnasModulos = function() {
		return [
		        {field: "Nombre", 			title: "UI_NOMBRE_MODULO",		width: "120px", 	attributes:{style:"text-align:left;"}},
		        {field: "NombreTipoEstado",	title: "UI_ESTADO",				width: "120px"},
		        {field: "Hora", 			title: "UI_FECHA",	width: "120px"},
		        {field: "IdEstado", 		title: "UI_ESTADO_SOLICITUD",	width: "120px"},
		        ];
	};
	
	//LOG DE PROCESOS
	modeloLogProcesos = function() {
		return {
			model: {
				fields: {
					IdLog			: { type: "string"},
					Tipo			: { type: "string"},
					IdUsuario		: { type: "string"},
					CodigoUsuario	: { type: "string"},
					IdEmpresa		: { type: "string"},
					FechaProceso	: { type: "date"},
					HoraProceso		: { type: "sysdatetime"},
					IdRegistro		: { type: "string"},
					TipoEvento		: { type: "string"},
					IdModuloCierre	: { type: "string"}
				}
			}
			
		};
	};
	
	obj.dataSourceLogProcesos = function() {
		return new bcs.DataSource ({
			type 		: "odata",
			definition 	: {
				serverFiltering 	: true, // Filtrado de datos en el servidor.
				serverPaging 		: true, // Paginado de los datos en el servidor.
				serverSorting 		: true, // Ordenamiento de los datos en el servidor.
				sort 				: { field: "FechaProceso", dir: "desc" },
				pageSize 			: 50,
				transport 			: {read : {
												url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1559",
												dataType : "json"
									          }
									  },
				schema 				: modeloLogProcesos()
			  }
		});
	};
	
	obj.columnasLogProcesos = function() {
		return [
		        {field: "Tipo",					title: "UI_TIPO", 			width: "120px"},
		        {field: "TipoEvento",			title: "UI_EVENTOS",		width: "120px"},
		        {field: "IdModuloCierre",		title: "UI_MODULO",	 		width: "120px"},
		        {field: "CodigoUsuario",		title: "UI_USUARIO",	 	width: "120px"},
		        {field: "HoraProceso",			title: "UI_FECHA_EJECUCION", 			width: "120px"}
		        ];
	};
	
	//PROCESOS Y FUNCIONES
	modeloProcesosFunciones = function (){
		return {
			model : {
				fields :{
					IdModuloFuncionProceso 	: { type: "number"},
					IdCREModuloEmpresa		: { type: "number"},
					Descripcion				: { type: "string"},
					NombreFuncionProceso	: { type: "string"},
					Tipo					: { type: "string"},
					PermiteEjecucion		: { type: "string"},
					IdEstado				: { type: "string"}
				}
			}
		};
	};
	
	obj.dataSourceProcesosFunciones = function() {
		return new bcs.DataSource ({
			type 		: "odata",
			definition 	: {
				serverFiltering 	: true, // Filtrado de datos en el servidor.
				serverPaging 		: true, // Paginado de los datos en el servidor.
				serverSorting 		: true, // Ordenamiento de los datos en el servidor.
				sort 				: { field: "IdModuloFuncionProceso", dir: "asc" },
				pageSize 			: 50,
				transport 			: {read : {
												url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1560",
												dataType : "json"
									          }
									  },
				schema 				: modeloProcesosFunciones()
			  }
		});
	};
	
	obj.columnasProcesosFunciones = function() {
		return [
		        {field: "NombreFuncionProceso",	title: "UI_NOMBRE_FUNCION_PROCESO", width: "120px"},
		        {field: "Descripcion",			title: "UI_DESCRIPCION",			width: "120px"},
		        {field: "Tipo",					title: "UI_TIPO",	 				width: "120px"},
		        {field: "PermiteEjecucion",		title: "UI_PERMITE_EJECUCION",		width: "120px"},
		        {field: "IdEstado",				title: "UI_ESTADO", 				width: "120px"}
		        ];
	};
	
	
	//Requisitos
	modeloRequisitos = function (){
		return {
			model : {
				fields :{
					 IdModuloRequisitoAperturaCierre	: { type: "number"},
					 IdCREModuloEmpresa					: { type: "number"},
					 IdTipoRequisito					: { type: "string"},
					 Valor								: { type: "string"},
					 Tipo								: { type: "string"},
					 Nombre								: { type: "string"},
					 IdEstado							: { type: "string"}
				}
			}
		};
	};
	
	obj.dataSourceRequisitos = function() {
		return new bcs.DataSource ({
			type 		: "odata",
			definition 	: {
				serverFiltering 	: true, // Filtrado de datos en el servidor.
				serverPaging 		: true, // Paginado de los datos en el servidor.
				serverSorting 		: true, // Ordenamiento de los datos en el servidor.
				sort 				: { field: "IdModuloRequisitoAperturaCierre", dir: "asc" },
				pageSize 			: 50,
				transport 			: {read : {
												url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1561",
												dataType : "json"
									          }
									  },
				schema 				: modeloRequisitos()
			  }
		});
	};
	
	obj.columnasRequisitos = function() {
		return [
		        {field: "Tipo",		title: "UI_TIPO",		width: "120px"},
		        {field: "Nombre",	title: "UI_NOMBRE",		width: "120px"},
		        {field: "Valor",	title: "UI_VALOR",		width: "120px"},
		        ];
	};
	
	
	
	return obj;
	
}]);