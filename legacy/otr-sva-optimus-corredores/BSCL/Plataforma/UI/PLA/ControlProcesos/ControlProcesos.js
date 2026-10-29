angular.module('ModuloPlataforma').controller('ConsultaControlProcesosCtrl',
		function($scope,$translate,$modal,$log,
				bcsComunes, bcsConsultaProcesosGrid,bcsServicioPlataforma)
{
	$scope.data	= {};
	$scope.filtros = {};
	$scope.filtros.estado = "";
	var idVentana 	= bcsComunes.ventanaWinId($scope); 
	var parametros 	= bcsComunes.ventanaParams(idVentana);
	 var monedaSistema = typeof $scope.SesionIdMonedaSistema != undefined ? 
	 ($scope.SesionIdMonedaSistema != null ? $scope.SesionIdMonedaSistema : 'CLP') : 'CLP';
	 $scope.monedaSistema =monedaSistema;
	 
	var overlay = bcsComunes.overlay;
	$scope.data.oConfiguracionTipoLog={
			tipo : "combobox-tipo-log",
			configuracion : {},
			filtros : {
			
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};
	
	$scope.data.oConfiguracionTipoRegistro={
			tipo : "combobox-tipo-registro",
			configuracion : {},
			filtros : {
			
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};
	
	//GRILLA PRINCIPAL
	$scope.confControlProcesos = {
		dataSource			: bcsConsultaProcesosGrid.dataSourceConsultaControlProcesos(),
		columns				: bcsConsultaProcesosGrid.columnasConsultaControlProcesos($scope),
		sorteable			: true,
		pageable			: true,
		filterable			: true,
		groupable			: true,
		height				: 500,
		userSettings		: true,
		navigatable			: true,
		excel : {
			allPages: true,
		},
		selectable			: "row",
		gridCreated			: function(grid) { //paso de parametros
			if(parametros.IdProceso && parametros.IdProceso>0){
				$scope.filtros.idProceso = parametros.IdProceso;
				$scope.buscar();
			}
		},
	};
	
	//SUB-GRILLA
	$scope.confDetallesProcesos = function (dataItem){
		var DetLog = bcsConsultaProcesosGrid.dataSourceDetConsultaControlProcesos();
		DetLog.definition.filter = {field: "IdLog", operator: "eq", value: Number(dataItem.IdLog)};
		return {
			dataSource			: DetLog,
			columns				: bcsConsultaProcesosGrid.columnasDetConsultaControlProcesos(),
			sorteable			: true,
			pageable			: true,
			height				: 120,
			width				: 350,
			selectable			: "row",
			autoBind			: true
		}
	};
	
	//**************************************
	// Consulta Fecha de Sistema
	//**************************************	        	
	
	$scope.filtros.fecInicio = util.date(new Date()).hours(0).minutes(0).seconds(0).milliseconds(0).toDate(); ;//util.date(util.datefix($scope.SesionFechaSistema)).hour(0).minute(0).second(0).toDate();
	$scope.filtros.fecTermino = util.date(new Date()).hours(23).minutes(59).seconds(59).milliseconds(999).toDate();//util.date(util.datefix($scope.SesionFechaSistema)).hour(23).minute(59).second(59).toDate();
	
	//bcsServicioPlataforma.FechaSistema(datos,successFecha,errorFecha);
	
	//****************************************
	// Fin Consulta Fecha de Sistema
	//****************************************
	
	
	//Filtros para el grid
	$scope.buscar =  function(){
		var filters = new Array();
		
		if($scope.$root.SesionIdEmpresa !== undefined && $scope.$root.SesionIdEmpresa != "")
			filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa });
		
		if ($scope.filtros.TipoLogObj.IdTipoLog !== undefined && $scope.filtros.TipoLogObj.IdTipoLog != "0")
			filters.push({ field: "IdTipoLog", operator: "eq", value: $scope.filtros.TipoLogObj.IdTipoLog});
		if ($scope.filtros.tipoRegistroObj.IdTipoRegistro !== undefined && $scope.filtros.tipoRegistroObj.IdTipoRegistro != "0")
			filters.push({ field: "IdTipoRegistro", operator: "eq", value: $scope.filtros.tipoRegistroObj.IdTipoRegistro});
		
		if ($scope.filtros.fecInicio !== null)
		{
			var rangoSeis = 186;
			var resp = util.validarRangoFecha($scope.filtros.fecInicio, $scope.filtros.fecTermino, rangoSeis);
			if(resp.estado){
				bcsComunes.msg("error", "Error", $translate.instant(resp.mensaje));
				return;
			}
			
			if(util.date(util.dateOdata($scope.filtros.fecInicio)).add(6,'month').toDate() < util.dateOdata($scope.filtros.fecTermino)){
				bcsComunes.msg("error", "Error", $translate.instant('FECHA_RANGO_CONSULTA_INVALIDA'));
				return;
			}
			filters.push({ field: "FechaRegistro", operator: "gte", value: util.dateOdata($scope.filtros.fecInicio)});
		}
		if ($scope.filtros.fecTermino !== null){
			filters.push({ field: "FechaRegistro", operator: "lte", value: util.dateOdata($scope.filtros.fecTermino)});
		}
		if($scope.filtros.idProceso && $scope.filtros.idProceso>0)
			filters.push({ field: "IdProceso", operator: "eq", value: $scope.filtros.idProceso });
		
		console.log("FILTROS: " + JSON.stringify(filters));
		//$scope.gridApi.dataSource.filters(filters);
		$scope.confControlProcesos.dataSource.filters(filters);
	};
	
	$scope.limpiarFiltros =  function()
	{
		$scope.filtros.fecIniciong 		= util.date(new Date()).hours(0).minutes(0).seconds(0).milliseconds(0).toDate();
		$scope.filtros.fecInicio 		= util.date(new Date()).hours(0).minutes(0).seconds(0).milliseconds(0).toDate();
		
		$scope.filtros.fecTerminong 	= util.date(new Date()).hours(23).minutes(59).seconds(59).milliseconds(999).toDate();
		$scope.filtros.fecTermino 		= util.date(new Date()).hours(23).minutes(59).seconds(59).milliseconds(999).toDate();

		$scope.filtros.idTipoLog	   	= " Todos";
		$scope.filtros.idTipoRegistro	= 0;
		
		$scope.filtros.idProceso = undefined;
		
		$scope.confControlProcesos.dataSource.removeAll();
	};
	
	$scope.data.opcionNoHabilitada = function(){
		bcsComunes.msg("warning", "warning", "Esta opcion no se encuentra habilitada.");
	};
});

angular.module('ModuloPlataforma').factory('bcsConsultaProcesosGrid',['bcsComunes', 
        function(bcsComunes)
{
	var obj = {};
	
	transporteConsultaControlProcesos = function() {
		return {
			read :{
				url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1562",
				dataType : "json"
			}
		};
	};
	
	modeloConsultaControlProcesos = function(){
		return{
			model: {
				fields : {
					IdLog						: {type: "string"},
					IdTipoLog					: {type: "string"},
					NombreModulo				: {type: "string"},
					IdTipoRegistro				: {type: "string"},
					CodigoUsuario				: {type: "string"},
					NombreEmpresa				: {type: "string"},
					FechaLog					: {type: "date"},
					PeriodoLog					: {type: "string"},
					FechaRegistro				: {type: "sysdatetime"},
					CodigoUsuarioAtendido		: {type: "string"},
					IdCliente					: {type: "string"},
					EstadoCliente				: {type: "string"},
					NombreCliente				: {type: "string"},
					IdTipoDocumentoIdentidad	: {type: "string"},
					IdDocumentoIdentidad		: {type: "string"},
					IdPortafolio				: {type: "string"},
					CodigoPortafolio 			: {type: "string"},
					EstadoPortafolio			: {type: "string"},
					IdRegistro					: {type: "string"},
					EstadoRegistro				: {type: "string"},
					IdTipoOperacion				: {type: "string"},
					Folio						: {type: "string"},
					Cantidad					: {type: "string"},
					PrecioTasa					: {type: "string"},
					Monto						: {type: "string"},
					IdMonedaOrigen				: {type: "string"},
					IdMonedaLiquidacion			: {type: "string"},
					TipoCambio					: {type: "string"},
					Nemo						: {type: "string"},
					IdProceso					: {type: "string"},
					EstadoProceso				: {type: "string"},
					NombreProceso				: {type: "string"}
				}
			}
		};
	};
	
	
	definicionODataConsultaControlProcesos = function(){
		return {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: true, // Ordenamiento de los datos en el servidor.
			groupable 			: true, 
			sort 				: { field: "FechaRegistro", dir: "desc" }, // Criterios de Ordenamiento.
			pageSize 			: 25, // Número de registros por página.
			transport 			: transporteConsultaControlProcesos(),
			schema 				: modeloConsultaControlProcesos(),
		};
	};
	
	obj.dataSourceConsultaControlProcesos = function() {
		return new bcs.DataSource ({
			type		: "odata",
			definition	: definicionODataConsultaControlProcesos()
		});
	};
	
	obj.columnasConsultaControlProcesos = function(scope) {
		return [
		        	{field:	"IdLog", 					title: "UI_ID_LOG", 				format: "{0:n5}", width: "50px"},
		        	
		        	{field:	"IdTipoLog", 				title: "UI_TIPO_LOG", 				format: "{0:n5}", width: "100px"},
		        	{field:	"NombreModulo", 			title: "UI_NOMBRE_MODULO", 			format: "{0:n5}", width: "100px"},
		        	{field:	"IdTipoRegistro", 			title: "UI_ID_TIPO_REGISTRO", 		format: "{0:n5}", width: "100px"},
		        	{field:	"CodigoUsuario", 			title: "UI_CODIGO_USUARIO", 		format: "{0:n5}", width: "100px"},
		        	{field:	"NombreEmpresa", 			title: "UI_NOMBRE_EMPRESA", 		format: "{0:n5}", width: "100px"},
		        	{field:	"FechaLog", 				title: "UI_FECHA_SISTEMA", 				format: "{0:d}",  width: "80px"}, //template :"<label> #:  util.formatoDate(util.datefix(FechaLog)) #</label>"
		        	{field: "FechaRegistro",			title: "UI_HORA", 		 			template :"<label> #:  util.formatoTime(FechaRegistro) #</label>", width: "80px"},
		        	{field:	"PeriodoLog", 				title: "UI_PERIODO_LOG", 			format: "{0:n5}", width: "100px"},
		        	{field:	"CodigoUsuarioAtendido", 	title: "UI_CODIGO_USUARIO_ATENDIDO", format: "{0:n5}",width: "100px"},
		        	{field:	"IdCliente", 				title: "UI_ID_CLIENTE", 			format: "{0:n5}", width: "100px"},
		        	{field:	"NombreCliente", 			title: "UI_NOMBRE_CLIENTE",			format: "{0:n5}", width: "100px"},
		        	{field:	"EstadoCliente", 			title: "UI_ESTADO_CLIENTE", 		format: "{0:n5}", width: "100px"},
		        	{field:	"IdTipoDocumentoIdentidad", title: "UI_TIPO_DOC_IDENTIDAD", 	format: "{0:n5}", width: "100px"},
		        	{field:	"IdDocumentoIdentidad", 	title: "UI_DOC_IDENTIDAD", 			format: "{0:n5}", width: "100px"},
		        	{field:	"IdPortafolio", 			title: "UI_ID_PORTAFOLIO", 			format: "{0:n5}", width: "100px"},
		        	{field:	"CodigoPortafolio", 		title: "UI_CODIGO_PORTAFOLIO", 		format: "{0:n5}", width: "100px"},
		        	{field:	"EstadoPortafolio", 		title: "UI_ESTADO_PORTAFOLIO", 		format: "{0:n5}", width: "100px"},
		        	{field:	"IdRegistro", 				title: "UI_ID_REGISTRO", 			format: "{0:n5}", width: "100px"},
		        	{field:	"EstadoRegistro", 			title: "UI_ESTADO_REGISTRO", 		format: "{0:n5}", width: "100px"},
		        	{field:	"IdTipoOperacion", 			title: "UI_ID_TIPO_OPERACION", 		format: "{0:n5}", width: "100px"},
		        	{field:	"Folio", 					title: "UI_FOLIO", 					format: "{0:n5}", width: "80px"},
		        	{field: "Cantidad",					title: "UI_CANTIDAD", 				template :"<label> #:  util.bcs_formato(Cantidad,'GEN','Cantidad','" + scope.monedaSistema + "','','').valor #</label>", width: "100px"},
		        	{field:	"PrecioTasa", 				title: "UI_PRECIOTASA", 			format: "{0:n5}", width: "100px"},
		        	{field: "Monto",					title: "UI_MONTO", 					template :"<label> #:  util.bcs_formato(Monto,'GEN','Monto','" + scope.monedaSistema + "','','').valor #</label>", width: "80px"}, 
		        	{field:	"IdMonedaOrigen",			title: "UI_ID_MONEDA_ORIGEN",		format: "{0:n5}", width: "100px"},
		        	{field:	"IdMonedaLiquidacion",		title: "UI_ID_MONEDA_LIQUIDACION",	format: "{0:n5}", width: "100px"},
		        	{field:	"TipoCambio",				title: "UI_TIPO_CAMBIO",			template :"<label> #:  (IdMonedaOrigen ? util.bcs_formato(TipoCambio,'GEN','Monto',IdMonedaOrigen,'','').valor : '') #</label>", width: "100px"},
		        	{field:	"Nemo", 					title: "UI_LOG_NEMO", 				format: "{0:n5}", width: "150px"},
		        	{field:	"IdProceso", 				title: "UI_ID_PROCESO", 			format: "{0:n5}", width: "100px"},
		        	{field:	"EstadoProceso", 			title: "UI_ESTADO_PROCESO", 		format: "{0:n5}", width: "100px"},
		        	{field:	"NombreProceso", 			title: "UI_NOMBRE_PROCESO", 		format: "{0:n5}", width: "150px"},
					{field: "FechaRegistro",			title: "UI_FECHA_LOG", 				width: "80px"},
		        ];
	};
	
	//MOVIEMIENTOS DETALLE
	transporteDetConsultaControlProcesos = function() {
		return {
			read : {
				url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1563",
				dataType : "json"
			}
		};
	};
	
	modeloDetConsultaControlProcesos = function() {
		return {
			model: {
				fields: {
					IdDetalleLog 	: { type: "string"},
					IdLog 			: { type: "string"},
					SubRegistro 	: { type: "string"},
					CampoRegistro 	: { type: "string"},
					ValorAntiguo 	: { type: "string"},
					ValorNuevo 		: { type: "string"}
				}
			}
		};
	 };
	 
	 definicionODataDetConsultaControlProcesos = function() {
		 return {
			 serverFiltering 	: true, // Filtrado de datos en el servidor.
			 serverPaging 		: false, // Paginado de los datos en el servidor.
			 serverSorting 		: true, // Ordenamiento de los datos en el servidor.
			 sort 				: { field: "SubRegistro", dir: "asc" }, // Criterios de Ordenamiento.
			 pageSize 			: 5, // Número de registros por página.
			 transport 			: transporteDetConsultaControlProcesos(),
			 schema 			: modeloDetConsultaControlProcesos(),
			 /*filtersFn 			: function (row) {
				 					return { field: "IdLog", operator: "eq", value: Number(row.IdLog) };
			 }*/
		 };
	 };
	 
	 obj.dataSourceDetConsultaControlProcesos = function(){
		 return new bcs.DataSource({
			 type		: "odata",
			 definition	: definicionODataDetConsultaControlProcesos()
		 });
	 };
	 
	 obj.columnasDetConsultaControlProcesos = function() {
		 return [	
		         	{field:	"IdDetalleLog", 	title: "UI_ID_DETALLE_LOG", format: "{0:n5}", width: "0px", hidden:true},
		        	{field:	"IdLog", 			title: "UI_ID_LOG", 		format: "{0:n5}", width: "0px", hidden:true},
		        	{field:	"SubRegistro", 		title: "UI_SUBREGISTRO", 	format: "{0:n5}", width: "40px"},
		        	{field:	"CampoRegistro", 	title: "UI_CAMPO_REGISTRO", format: "{0:n5}", width: "150px"},
		        	{field:	"ValorAntiguo", 	title: "UI_VALOR_ANTIGUO", 	format: "{0:n5}", width: "60px"},
		        	{field:	"ValorNuevo", 		title: "UI_VALOR_NUEVO", 	format: "{0:n5}", width: "60px"}
		        ];
	 };

	return obj;
}]);