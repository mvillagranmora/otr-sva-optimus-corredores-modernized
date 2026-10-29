angular.module('ModuloNormativa').controller('EnvioArchivoInterfacesCtrl',function($scope,$translate,bcsComunes,bcsServicioInterfaces,$timeout,$http){
	
	$scope.datos = {};
	
	$scope.winID=bcsComunes.ventanaWinId($scope);
	
	$scope.filtros = {
			fechaDesde : util.dateInicioDia($scope.SesionFechaSistema),
			fechaHasta : util.dateTerminoDia($scope.SesionFechaSistema),
			idInterfaz : undefined
	};
	
	var validarFiltros = function(filtros)
	{
		var filters = new Array();
		if (filtros.fechaDesde)
			filters.push({ field: "fechaEjecucion", operator: "gte", value: util.dateOdata(filtros.fechaDesde)});
		if (filtros.fechaHasta)
			filters.push({ field: "fechaEjecucion", operator: "lte", value: util.dateOdata(filtros.fechaHasta)});
		if (filtros.idInterfaz){
			filters.push({ field: "IdInterfaz", operator: "eq", value: filtros.idInterfaz});
		}
		return filters;
	}
	
	var configuracionColumnas = function() {
		return { 
			IdPrograma:{field: "IdPrograma",title: $translate.instant('UI_ID_PROGRAMA'),width: "100px",	align: "left"},
			Nombre:{field: "Nombre",title: $translate.instant('UI_NOMBRE'),width: "100px",	align: "left"},
			Descripcion:{field: "Descripcion",title: $translate.instant('UI_DESCRIPCION'),width: "100px",	align: "left"},
			fechaEjecucion:{field: "fechaEjecucion",title: $translate.instant('UI_FECHA'),width: "100px",	align: "left"},
			RutaSalida:{field: "RutaSalida",title: $translate.instant('UI_RUTA_SALIDA'),width: "100px",	align: "left"},
			RutaLog:{field: "RutaLog",title: $translate.instant('UI_RUTA_LOG'),width: "100px",	align: "left"},
		}
	}
	
	getConfiguracionColumna = function(campo,extras) {
		if (!configuracionColumnas()[campo]) console.log("FALTA COLUMNA " + campo);
		return _.extend( {}, configuracionColumnas()[campo], extras);
	};
	
	var getSchemaConsultaArchivoInterfaces = function(){
		return {
			model : {
				fields : {
					IdPrograma		: { type : "number"},
					Nombre			: { type : "string"},
					Descripcion		: {	type : "string"},
					fechaEjecucion 	: { type : "sysdatetime"},
					RutaSalida 		: { type : "string"},
					RutaLog 		: { type : "string"}
				}
			}
		};
	}
	
	getDataSourceConsultaArchivoInterfaces = function(){
		return new bcs.DataSource({ 
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				sort: { field: "IdPrograma", dir: "desc" },
				pageSize : 200,
				transport: {
					read: {
						url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=4211",
						dataType: "json"
					}
				},
				schema : getSchemaConsultaArchivoInterfaces()
			},
			
		});
	}
	
	var getConsultaArchivoInterfaces = function() {
		var config = {};
		config = {
				buscar : function(apiGrid,filtros) {
					apiGrid.dataSource.filter(validarFiltros(filtros));
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
		      		height:450,
			
					dataSource: getDataSourceConsultaArchivoInterfaces(),
				
					columns: [
					           getConfiguracionColumna("IdPrograma",{ groupable: true})
					          ,getConfiguracionColumna("Nombre",{ groupable: true})
					          ,getConfiguracionColumna("Descripcion",{ groupable: true})
					          ,getConfiguracionColumna("fechaEjecucion",{ groupable: true})
					          ,getConfiguracionColumna("RutaSalida",{ groupable: true})
					          ,getConfiguracionColumna("RutaLog",{ groupable: true})
					]
				}
		};
		return config;
	};
	
	var conf = getConsultaArchivoInterfaces();
	
	$scope.defaultOptions = conf.options;
	$scope.buscar = conf.buscar;
	$scope.winID=bcsComunes.ventanaWinId($scope);
			
	$scope.enviar = function (datos) {
		if(!datos || datos.length == 0) return;
		//TODO:Enviar Documentos
  	};
  	
  	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,40);
    }
	
	$scope.defaultOptions.gridCreated = function(grid){
		$scope.ajustaVentana();
	}
	
	$scope.verSalida = function (datos){
		if(!datos || datos.length == 0) return;
		var datosD = {file:datos[0].RutaSalida}
		var nombreArchivo = datos[0].RutaSalida;
		bcsComunes.ventanaOverlay($scope.winID,true);
		var url = "/BSCL/Plataforma/GWJAVA/PLA/download?";
		
		$http({

			method: 'POST',
			url: url,
			params: datosD
			
		}).success(function(datos){
				var file = new File([datos],nombreArchivo);
				saveAs(file);
				bcsComunes.msg("info",$translate.instant("UI_DESCARGA_REPORTE"),$translate.instant("UI_DESCARGA_REPORTE_EXITOSA"));
				bcsComunes.ventanaOverlay($scope.winID,false);
				
		})
		.error(function (err){
				bcsComunes.msg("error",$translate.instant("UI_DESCARGA_REPORTE"),$translate.instant('UI_DESCARGA_REPORTE_ERROR'));
				bcsComunes.ventanaOverlay($scope.winID,false);
		});

	}
	
	$scope.verLog = function (datos){
		if(!datos || datos.length == 0) return;
		var datosD = {file:datos[0].RutaLog}
		var nombreArchivo = datos[0].RutaLog;
		bcsComunes.ventanaOverlay($scope.winID,true);
		var url = "/BSCL/Plataforma/GWJAVA/PLA/download?";
		
		$http({

			method: 'POST',
			url: url,
			params: datosD
			
		}).success(function(datos){
				var file = new File([datos],nombreArchivo);
				saveAs(file);
				bcsComunes.msg("info",$translate.instant("UI_DESCARGA_LOG"),$translate.instant("UI_DESCARGA_LOG_EXITOSA"));
				bcsComunes.ventanaOverlay($scope.winID,false);
				
		})
		.error(function (err){
				bcsComunes.msg("error",$translate.instant("UI_DESCARGA_LOG"),$translate.instant('UI_DESCARGA_LOG_ERROR'));
				bcsComunes.ventanaOverlay($scope.winID,false);
		});

	}
  	
	$scope.limpiarFiltros = function()
	{
		$scope.filtros = {
				fechaDesde : util.dateInicioDia($scope.SesionFechaSistema),
				fechaHasta : util.dateTerminoDia($scope.SesionFechaSistema),
				idInterfaz : undefined
		};
	};
		
	$scope.limpiarFiltros();
});