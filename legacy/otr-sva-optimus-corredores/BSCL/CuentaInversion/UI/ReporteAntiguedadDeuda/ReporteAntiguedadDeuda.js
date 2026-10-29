angular.module('ModuloCustodia').controller('ReporteAntiguedadDeudaCtrl', 
		function($scope, bcsComunes, bcsConsultaReporteGrid, $http, $translate){
	//*** configuracion de objetos ***//
	var idVentana = bcsComunes.ventanaWinId($scope);
    $scope.data = {};
    $scope.data.btnBuscarReportesValido = false;
    var parametros 	= bcsComunes.ventanaParams(idVentana);
	$scope.EstadoBotonDescargaReporte = false;

    /*Inicio filtros*/
    $scope.filtros = {};
    let fechaTope =  util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
    $scope.filtros.fechaTopeArchivoGenerar = fechaTope;
    $scope.filtros.fechaTopeArchivoBuscar = fechaTope;

    $scope.filtros.kngFecha = util.dateInicioDia($scope.SesionFechaSistema);
    $scope.filtros.kOptFechaDeposito = {};
    $scope.filtros.kOptFechaDeposito.max = $scope.$root.SesionFechaSistema;
    /*Fin filtros*/

    //GRILLA-PRINCIPAL
	$scope.confControlReportes = {
		dataSource			: bcsConsultaReporteGrid.dataSourceConsultaControlReportes(),
		columns				: bcsConsultaReporteGrid.columnasConsultaControlReportes(),
		sorteable			: true,
		pageable			: true,
		filterable			: true,
		groupable			: true,
		height				: 400,
		userSettings		: true,
		navigatable			: true,
		excel : {
			allPages: true,
		},
		selectable			: "row",
		gridCreated			: function(grid) { $scope.buscarReportes(); },
	};
	
	//SUB-GRILLA
	$scope.confDetallesReporte = function (dataItem){
		var DetReporte = bcsConsultaReporteGrid.dataSourceDetConsultaControlReporte();
		DetReporte.definition.filter = {field: "IdPrograma", operator: "eq", value: Number(dataItem.IdPrograma)};
		return {
			dataSource			: DetReporte,
			columns				: bcsConsultaReporteGrid.columnasDetConsultaControlReporte(),
			sorteable			: true,
			pageable			: true,
			height				: 120,
			width				: 350,
			selectable			: "row",
			autoBind			: true
		}
	};      

    /*Inicio Botones*/
    $scope.solicitarReporte = function(){
        parametros = {}
        parametros.FechaSistema = $scope.SesionFechaSistema;
		$scope.buscarReportes(parametros.FechaSistema);
		var idVentanaGenerarReporteAntiguedadDeuda = bcsComunes.ventanaAbrir("GenerarReporteAntiguedadDeuda", parametros , false, false, idVentana);
        var promesa=bcsComunes.ventanaPromesa(idVentanaGenerarReporteAntiguedadDeuda); 	
		$.when(promesa).then(function(respuesta){
			$scope.buscarReportes(parametros.FechaSistema);
        });
    }

    $scope.buscarReportes = function(fechaBusqueda){
		var filters = new Array();
		let fechaFin    =   $scope.filtros.fechaTopeArchivoBuscar.toISOString().slice(0,10);
		if(!(typeof($scope.data.FechaInicioBusqueda) === 'undefined') === true){
			let fechaInicio =   $scope.data.FechaInicioBusqueda.toISOString().slice(0,10);
			if(fechaInicio>fechaFin){
				bcsComunes.msg("error","ERROR","Fecha inicio no puede ser mayor a fecha fin");
				return;
			}
			if(fechaInicio===fechaFin){
				filters.push({ field: "FechaSolicitudArchivo", operator: "eq", value: fechaInicio});
			}
			else{
				filters.push({ field: "FechaSolicitudArchivo", operator: "gte", value: fechaInicio});
				filters.push({ field: "FechaSolicitudArchivo", operator: "lte", value: fechaFin});
			}
			$scope.confControlReportes.dataSource.filter(filters);
		}
		else if(!(typeof(fechaBusqueda) === 'undefined') === true){
			filters.push({ field: "FechaSolicitudArchivo", operator: "eq", value: fechaBusqueda});
		}
		else{
			filters.push({ field: "FechaSolicitudArchivo", operator: "eq", value: fechaFin});
		}
		$scope.confControlReportes.dataSource.filter(filters);
    }

    $scope.descargarReporte = function(seleccionGrilla){
		let ruta = {file:seleccionGrilla.RutaSalidaArchivo}
		let nombre = seleccionGrilla.NombreArchivo;
		bcsComunes.ventanaOverlay(idVentana,true);
		var url = "/BSCL/Plataforma/GWJAVA/PLA/download?";
		$http({
			method: 'POST',
			url: url,
			params: ruta,
			responseType:'arraybuffer'
		}).success(function(datos){
				var file = new File([datos], nombre);
				saveAs(file);
				bcsComunes.msg("info",$translate.instant("UI_DESCARGA_REPORTE"),$translate.instant("UI_DESCARGA_REPORTE_EXITOSA"));
				bcsComunes.ventanaOverlay(idVentana,false);
		})
		.error(function (err){
				bcsComunes.msg("error",$translate.instant("UI_DESCARGA_REPORTE"),$translate.instant('UI_DESCARGA_REPORTE_ERROR'));
				bcsComunes.ventanaOverlay(idVentana,false);
		});
	}

    $scope.limpiaFiltrosBuscarReportes = function(){
        $scope.data.FechaInicioBusqueda = null;
    }
    /*Fin Botones*/

    /*Inicio Observadores*/
    $scope.$watch('data.FechaInicioBusqueda',function(newval,oldval){	
        if(newval!==undefined && newval!==null){
            $scope.data.btnBuscarReportesValido = true;
        }
        else{
            $scope.data.btnBuscarReportesValido = false;
        }
    });

	$scope.$watch('seleccionControlReportes',function(newval,oldval){	
        if(newval===undefined) return;
		$scope.EstadoBotonDescargaReporte = ('seleccionControlReportes' && newval.IdEstadoProgramaRelacionado === 'TER' 
											&& newval.NombreArchivo !== '' && newval.GeneroArchivo === 'SI') ? true : false;
    });

	var firstWindow= $('#Win_ReporteAntiguedadDeuda');
	firstWindow.css("height", "621px");
	firstWindow.css("width", "737px")
    /*Fin Observadores*/
});

/**Inicio Config Grilla**/
angular.module('ModuloPlataforma').factory('bcsConsultaReporteGrid',['bcsComunes', function(bcsComunes) {
	var obj = {};
    /*Inicio Grilla Principal */
	transporteConsultaControlReportes = function() {
		return {
			read :{
				url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=44290882",
				dataType : "json"
			}
		};
	};
	
	modeloConsultaControlReportes = function(){
		return{
			model: {
				fields : {
					IdPrograma									: {type: "string"},
					CodigoUsuario								: {type: "string"},
					IdEstadoProgramaRelacionado				    : {type: "string"},
					EstadoProgramaRelacionado					: {type: "string"},
					FechaSolicitudArchivo							: {type: "string"},
					NombreArchivo								: {type: "string"},
					ErrorArchivo								: {type: "string"},
				    RutaSalidaArchivo							: {type: "string"},
					GeneroArchivo								: {type: "string"},
				}
			}
		};
	};
	
	definicionODataConsultaControlReportes = function(){
		return {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: true, // Ordenamiento de los datos en el servidor.
			groupable 			: true, 
			sort 				: { field: "FechaSolicitudArchivo", dir: "desc" }, // Criterios de Ordenamiento.
			pageSize 			: 25, // N�mero de registros por p�gina.
			transport 			: transporteConsultaControlReportes(),
			schema 				: modeloConsultaControlReportes(),
		};
	};
	
	obj.dataSourceConsultaControlReportes = function() {
		return new bcs.DataSource ({
			type		: "odata",
			definition	: definicionODataConsultaControlReportes()
		});
	};
	
	obj.columnasConsultaControlReportes = function() {
		return [
		    {field:	"IdPrograma", 									title: "UI_PROGRAMA_RELACIONADO", 	format: "{0:n5}", width: "50px"},
		    {field:	"CodigoUsuario", 								title: "UI_CODIGO_USUARIO", 		format: "{0:n5}", width: "100px"},
		    {field:	"EstadoProgramaRelacionado", 			        title: "UI_ESTADO", 			    format: "{0:n5}", width: "100px"},
		    {field:	"FechaSolicitudArchivo", 						title: "UI_FECHA_SOLICITUD", 		format: "{0:n5}", width: "100px"},
		    {field:	"NombreArchivo", 								title: "UI_NOMBRE_ARCHIVO", 		format: "{0:n5}", width: "100px"},
			{field:	"GeneroArchivo", 								title: "UI_GENERO_ARCHIVO", 		format: "{0:n5}", width: "100px"},
			{field:	"ErrorArchivo", 								title: "UI_ERROR_ARCHIVO", 					format: "{0:n5}", width: "100px"},
		];
	};
	/*Fin Grilla Principal */

	/*Inicio SubGrilla*/
	transporteDetConsultaControlReporte = function() {
		return {
			read : {
				url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=44290883",
				dataType : "json"
			}
		};
	};
	
	modeloDetConsultaControlReporte = function() {
		return {
			model: {
				fields: {
					IdDetalleReporte 	: { type: "string"},
					IdPrograma 			: { type: "string"},
                    NombreParametro 	: { type: "string"},
					Valor 	            : { type: "string"},
				}
			}
		};
	};
	
	definicionODataDetConsultaControlProcesos = function() {
	    return {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: false, // Paginado de los datos en el servidor.
			serverSorting 		: true, // Ordenamiento de los datos en el servidor.
			sort 				: { field: "IdDetalleReporte", dir: "asc" }, // Criterios de Ordenamiento.
			pageSize 			: 5, // N�mero de registros por p�gina.
			transport 			: transporteDetConsultaControlReporte(),
			schema 			    : modeloDetConsultaControlReporte(),
		};
	};

	obj.dataSourceDetConsultaControlReporte = function(){
	    return new bcs.DataSource({
			type		: "odata",
			definition	: definicionODataDetConsultaControlProcesos()
		});
	};

	obj.columnasDetConsultaControlReporte = function() {
		return [	
            {field:	"IdDetalleReporte", 	title: "UI_DETALLE_REPOTE", format: "{0:n5}", width: "0px", hidden:true},
            {field:	"IdPrograma", 			title: "UI_PROGRAMA", 		format: "{0:n5}", width: "0px", hidden:true},
            {field:	"NombreParametro", 	    title: "UI_PARAMETROS", 	    format: "{0:n5}", width: "40px"},
            {field:	"Valor", 	            title: "UI_VALOR", 	        format: "{0:n5}", width: "60px"},
		];
	};
    /*Fin SubGrilla*/

	return obj;

}]);
/**Fin Config Grilla**/




