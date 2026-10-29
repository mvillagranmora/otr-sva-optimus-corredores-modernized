angular.module('ModuloPlataforma').factory('bcsConfiguracionPauta',function($translate,$rootScope,bcsComunes,$timeout,$http) {
	var obj = {};
	
	obj.ingresarCerrarVentana = function(id, idPadre){
		 if(id){
			 var promesa=bcsComunes.ventanaPromesa(id);                               
		     $.when(promesa).then(function(data){
		    	 $timeout(function() {
		    		 bcsComunes.ventanaCerrar(idPadre);
		    	 },20);
		     }); 
		 }else{
			 bcsComunes.ventanaCerrar(idPadre); 
		 }
	 }
	
	obj.validarFiltros = function(filtros,modo)
	{
		var filters = new Array();
		if (filtros.CodigoModulo)
			filters.push({ field: "CodigoModulo", operator: "eq", value: filtros.CodigoModulo});
		
		if (filtros.FechaSistema)
			filters.push({ field: "FechaSistema", operator: "eq", value: filtros.FechaSistema});
		
		if (filtros.EstadoProceso)
			filters.push({ field: "EstadoProceso", operator: "eq", value: filtros.EstadoProceso});
		
		return filters;
	}
	
	
	
	obj.configuracionColumnas = function() {
		return { 
			CodigoModulo				  	:{field: "CodigoModulo",					title: $translate.instant('UI_CODIGO'),					        width: "60px", align: "left"},
			NombreEstado		  			:{field: "NombreEstado",					title: $translate.instant('UI_ESTADO'),							width: "100px", align: "left"},
			CodigoModuloDependenciaCierre1	:{field: "CodigoModuloDependenciaCierre1",	title: $translate.instant('UI_CODIGO_MODULO_DEPENDENCIA_1'),	width: "100px", align: "left"},
			CodigoModuloDependenciaCierre2	:{field: "CodigoModuloDependenciaCierre2",	title: $translate.instant('UI_CODIGO_MODULO_DEPENDENCIA_2'),	width: "100px", align: "left"},
			CodigoModuloDependenciaCierre3	:{field: "CodigoModuloDependenciaCierre3",	title: $translate.instant('UI_CODIGO_MODULO_DEPENDENCIA_3'),	width: "100px", align: "left"},
			CodigoModuloDependenciaCierre4	:{field: "CodigoModuloDependenciaCierre4",	title: $translate.instant('UI_CODIGO_MODULO_DEPENDENCIA_4'),	width: "100px", align: "left"},
			CodigoModuloDependenciaCierre5	:{field: "CodigoModuloDependenciaCierre5",	title: $translate.instant('UI_CODIGO_MODULO_DEPENDENCIA_5'),	width: "100px",	align: "left"},
			NombreModulo		  			:{field: "NombreModulo",					title: $translate.instant('UI_NOMBRE_MODULO'),					width: "100px", align: "left"},
			DescripcionProceso		  		:{field: "DescripcionProceso",				title: $translate.instant('UI_DESCRIPCION_PROCESO'),			width: "100px", align: "left"},
			FechaEjecucion		  			:{field: "FechaEjecucion",					title: $translate.instant('UI_FECHA_EJECUCION'),				width: "80px", align: "left"},
			EstadoProceso		  			:{field: "EstadoProceso",					title: $translate.instant('UI_ESTADO_PROCESO'),					width: "80px", align: "left"},
			NombreEstadoProceso		  		:{field: "NombreEstadoProceso",				title: $translate.instant('UI_ESTADO_PROCESO'),					width: "80px", align: "left"},
			CodigoUsuario		  		    :{field: "CodigoUsuario",				    title: $translate.instant('UI_CODIGO_USUARIO'),					width: "70px", align: "left"},
			FechaUltimaActualizacion		:{field: "FechaUltimaActualizacion",		title: $translate.instant('UI_ULTIMA_ACTUALIZACION'),			width: "100px", align: "left"},
			TipoProceso						:{field: "TipoProceso",						title: $translate.instant('UI_TIPO_PROCESO'),					width: "100px", align: "left"},
			IdTipoPrograma					:{field: "IdTipoPrograma",					title: $translate.instant('UI_TIPO_PROGRAMA'),					width: "100px", align: "left"},
			Descripcion					    :{field: "Descripcion",						title: $translate.instant('UI_DESCRIPCION'),					width: "100px", align: "left"},
			Mensaje					   		:{field: "Mensaje",							title: $translate.instant('UI_MENSAJE'),						width: "100px", align: "left"},
			RequiereTipoPrograma			:{field: "RequiereTipoPrograma",			title: $translate.instant('UI_REQUIERE_TIPO_PROGRAMA'),			width: "100px", align: "left"},
			SiguienteTipoPrograma			:{field: "SiguienteTipoPrograma",			title: $translate.instant('UI_SIGUIENTE_TIPO_PROGRAMA'),		width: "100px", align: "left"},
			EsProcesoManual					:{field: "EsProcesoManual",					title: $translate.instant('UI_ES_PROCESO_MANUAL'),				width: "100px", align: "left"},		
			FechaEjecucionPrograma			:{field: "FechaEjecucionPrograma",			title: $translate.instant('UI_FECHA_EJECUCION'),				width: "100px", align: "left"},		
			IdPrograma						:{field: "IdPrograma",						title: $translate.instant('UI_ID_PROGRAMA'),					width: "100px", align: "left"},
			
			FechaSistema	  	:{field: "FechaSistema",	title: $translate.instant('UI_FECHA_SISTEMA'),	width: "80px"},
			IdEjecucion		  	:{field: "IdEjecucion",		title: $translate.instant('UI_NRO_EJECUCION'),	width: "50px"},
			CodigoProceso	  	:{field: "CodigoProceso",	title: $translate.instant('UI_PROCESO'),		width: "250px"},
			FechaInicio		  	:{field: "FechaInicio",		title: $translate.instant('UI_FECHA_INICIO'),	width: "100px"},
			FechaFin		  	:{field: "FechaFin",		title: $translate.instant('UI_FECHA_FIN'),		width: "100px"},
			Estado			  	:{field: "Estado",			title: $translate.instant('UI_ESTADO'),		width: "50px"},
			Descripcion		  	:{field: "Descripcion",		title: $translate.instant('UI_DESCRIPCION')	},
			Mensaje				:{field: "Mensaje",			title: $translate.instant('UI_MENSAJE'),		width: "200px"},
			
			ResultadoProceso		  		:{field: "ResultadoProceso",				title: $translate.instant('UI_RESULTADO_PROCESO'),			width: "100px", align: "left"}

	    }
	};

	obj.getConfiguracionColumna = function(campo,extras) {
		if (!obj.configuracionColumnas()[campo]) console.log("FALTA COLUMNA " + campo);
		return _.extend( {}, obj.configuracionColumnas()[campo], extras);
	};
	
	obj.getSchemaConsultaModuloDetalle = function() {
		return { 
			model: {
				fields : {
					IdEmpresa					  	  : { type : "number"},
					CodigoModulo				   	  : { type : "string"},
					NombreModulo		      		  : { type : "string"},
					NombreEstado		      		  : { type : "string"},
					CodigoUsuario		      		  : { type : "string"},
					FechaUltimaActualizacion		  : { type : "datetime"},
				}
			}
		}
	};
	
	obj.getSchemaConsultaControlAperturaCierre = function() {
		return { 
			model: {
				fields : {
					IdEmpresa		    : { type : "number"},
					NombreModulo	    : { type : "string"},
					CodigoProceso	    : { type : "string"},
					EstadoProceso 	    : { type : "string"},
					FechaSistema	    : { type : "date"},
					FechaEjecucion      : { type : "datetime"},
					DescripcionProceso  : { type : "string"},
					NombreEstado        : { type : "string"},
					NombreEstadoProceso : { type : "string"},
					CodigoUsuario		: { type : "string"},
					TipoProceso 		: { type : "string"},
					ResultadoProceso	: { type : "string"}
				}
			}
		}
	};
	
	obj.getSchemaConsultaLogPauta = function() {
		return { 
			model: {
				fields : {
					IdEmpresa		    : { type : "number"},
					FechaSistema		: { type : "string"},
					IdEjecucion			: { type : "number"},
					CodigoProceso		: { type : "string"},
					FechaInicio			: { type : "datetime"},
					FechaFin			: { type : "datetime"},
					Estado				: { type : "string"},
					Descripcion			: { type : "string"},
					RutaLog				: { type : "string"},
					Orden				: { type : "number"},
					Mensaje				: { type : "string"},
					IdEjecucion			: { type : "number"},
					
				}
			}
		}
	};
	
	obj.getDataSourceConsultaModuloDetalle = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverSorting : true, 
				sort: { field: "CodigoModulo", dir: "asc" },
				transport: {
		    		read: {
		    			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4178",
						dataType:"json"
		    		}
		    	},
				schema : obj.getSchemaConsultaModuloDetalle()			
			}
		})
	};
	
	obj.getDataSourceConsultaControlAperturaCierre = function () {
		return new bcs.DataSource({
			type : "call",
			definition : {
				pageSize		: 100,
	            callTransaction : "4196",
	            callResultSet   : "P_OUT_TBL",
				schema : obj.getSchemaConsultaControlAperturaCierre()			
			}
		})
	};
	
	obj.getDataSourceConsultaLogPauta = function () {
		return new bcs.DataSource({
			type : "call",
			definition : {
				pageSize		: 100,
				sort: [{ field: "IdEjecucion", dir: "asc" },{ field: "Orden", dir: "asc" },{ field: "CodigoProceso", dir: "asc" }],
				callTransaction : "4197",
	            callResultSet   : "P_OUT_TBL",
				schema : obj.getSchemaConsultaLogPauta()			
			}
		})
	};
	
	obj.getConsultaModuloDetalle = function() {
		var config = {};
		config = {
				buscar : function(apiGrid,filtros) {
					apiGrid.dataSource.filter(obj.validarFiltros(filtros,''));
				},
				options : {
					userSettings:true,
					sortable: true,
		      		reorderable: true,
		      		filterable: true,
					groupable: true,
					pageable: true,
					selectable: "multiple, row",
					reorderable: true,
					allowCopy: true,
					resizable: true,
		      		autoBind: true,
		      		scrollable: true,
		      		height:300,
			
					dataSource: obj.getDataSourceConsultaModuloDetalle(),
				
					columns: [
					           obj.getConfiguracionColumna("CodigoModulo")
					          ,obj.getConfiguracionColumna("NombreModulo")
					          ,obj.getConfiguracionColumna("NombreEstado")
					          ,obj.getConfiguracionColumna("FechaUltimaActualizacion")
					          ,obj.getConfiguracionColumna("CodigoUsuario")
					]
				}
		};
		return config;
	};
	
	obj.getConsultaControlAperturaCierre = function() {
		var config = {};
		config = {
				buscar : function(apiGrid,filtros) {
					apiGrid.dataSource.filter(obj.validarFiltros(filtros,''));
				},
				options : {
					userSettings:true,
					sortable: true,
		      		reorderable: true,
		      		filterable: true,
					groupable: true,
					pageable: true,
					selectable: "multiple, row",
					reorderable: true,
					allowCopy: true,
					resizable: true,
		      		autoBind: false,
		      		scrollable: true,
		      		height:300,
			
					dataSource: obj.getDataSourceConsultaControlAperturaCierre(),
				
					columns: [
					           obj.getConfiguracionColumna("DescripcionProceso")
					          ,obj.getConfiguracionColumna("FechaEjecucion")
					          ,obj.getConfiguracionColumna("NombreEstadoProceso")
					          ,obj.getConfiguracionColumna("NombreModulo")
					          ,obj.getConfiguracionColumna("TipoProceso")
					          ,obj.getConfiguracionColumna("CodigoUsuario")
							  ,obj.getConfiguracionColumna("ResultadoProceso")
					          
					]
				}
		};
		return config;
	};
	
	obj.ConsultaEstadoSistema = new bcs.DataSource({
		type 		: "odata",
		definition 	: {
			serverFiltering	: true,
			transport		: {
				read : {
					url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=4178",
					dataType : "json",
				}
			},
			schema 				: {
				model: {
					fields : {
						IdEmpresa				: {type: "number"},
						CodigoModulo			: {type: "string"},
						Nombre					: {type: "string"}					
					}
				}
			}
		},
	});	
	
	obj.getConsultaLogPauta = function() {
		var config = {};
		config = {
				buscar : function(apiGrid,filtros) {
					apiGrid.dataSource.filter(obj.validarFiltros(filtros,''));
				},
				options : {
					userSettings:true,
					sortable: true,
		      		reorderable: true,
		      		filterable: true,
					groupable: true,
					pageable: true,
					selectable: "multiple, row",
					allowCopy: true,
					resizable: true,
		      		autoBind: false,
		      		scrollable: true,
		      		height:500,
			
					dataSource: obj.getDataSourceConsultaLogPauta(),
				
					columns: [
						obj.getConfiguracionColumna("CodigoProceso")
						,obj.getConfiguracionColumna("Descripcion")
				        ,obj.getConfiguracionColumna("FechaInicio")
				        ,obj.getConfiguracionColumna("FechaFin")
				        ,obj.getConfiguracionColumna("Estado")
				        ,obj.getConfiguracionColumna("Mensaje")
					],
					rowColorBackground	: function(row) {								
						if (row.Estado == 'ERR')
							return "red";
						if (row.Estado == 'PRO')
							return "yellow";
						if (row.Estado == 'TER')
							return "green";
						return "";
					  }	
				}
		};
		return config;
	};
	
	return obj;
});