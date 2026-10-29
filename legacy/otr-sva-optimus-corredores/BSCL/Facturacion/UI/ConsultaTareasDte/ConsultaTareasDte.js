angular.module('ModuloFacturacion').controller('ConsultarTareasDteCtrl',
		function($scope,$translate,$modal,$log,
				bcsComunes, bcsServicioPlataforma)
{

	$scope.conf = {}; 
	$scope.data	= {};
	$scope.tareasGrid = {};
	var overlay = bcsComunes.overlay;
	
	//Seteo de variables de sistema
	var fecha = Date.now();
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.conf.kngFechaTareaDesde = util.date(fecha).hour(0).minute(0).second(0).toDate();
	$scope.data.IdModulo	= 'PLA';
	$scope.data.btnActivos  = true;   //Activa botones
	$scope.perfiles = {};
	$scope.accesos = {};
	
	var idVentana = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaTitulo(idVentana, bcsComunes.ventanaGetTitulo(idVentana)+ " - "+ $scope.SesionEmpresa );
	
	
	$scope.confBasicaTareas = {
			dataSource: new bcs.DataSource({
				type : "odata", //Los tipos posibles son: odata, call, array y object
				definition : {
					filter : [ // Configuración de filtros sobre los datos. Es equivalente a una operación "AND".
								{ field: "IdEmpresa", operator: "eq", value: $scope.data.IdEmpresa},
								{ field: "FechaEnvioDte", operator: "eq", value: $scope.conf.kngFechaTareaDesde},
							],
							serverFiltering : true, // Filtrado de datos en el servidor.
							serverPaging : false, // Paginado de los datos en el servidor.
							serverSorting : true, // Ordenamiento de los datos en el servidor.
							transport : {
								read : {
									url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1850",
									dataType : "json"
								}
							},
							schema : {
								model: {
									fields : {
										IdTareaDTE: {type:"number"},
										IdTareaDTEGrupo: {type:"number"},
										TipoTareaDTE: {type:"string"},
										IdRespuestaDTE: {type:"number"},
										IdComprobante: {type:"number"},
										RespuestaDTEEnvioSII: {type: "string"},
										IdTipoComprobante: {type:"string"},
										FolioComprobanteRespuestaDTE: {type:"number"},
										FechaComprobante: {type:"sysdate"},
										IdEstadoTareaDTE: {type:"string"},
										IdEstadoDTE: {type:"string"},
										IdEstadoSII: {type:"string"},
										FechaEnvioDte: {type:"sysdatetime"}
									}
								}
							}
						}
			}),
			sortable: true,
			sort : { field: "IdTareaDTE", dir: "DESC" }, // Criterios de Ordenamiento.
			columns: [
						{field: "IdTareaDTE",title : "UI_ID_TAREA",width : "80px"},
						{field: "IdTareaDTEGrupo",title : "UI_ID_TAREA_GRUPO",width : "80px"},
						{field: "TipoTareaDTE",title : "UI_TIPO_TAREA",width : "80px"},
						{field: "IdComprobante",title : "UI_SECUENCIA_COMPROBANTE",width : "120px"},
						{field: "IdTipoComprobante",title : "UI_TIPO_COMPROBANTE",width : "80px"},
						{field: "FolioComprobanteRespuestaDTE",title : "UI_FOLIO_COMPROBANTE_RESPUESTA",width : "80px"},
						{field: "FechaComprobante",title : "UI_FECHA_COMPROBANTE",width : "120px"},
						{field: "IdEstadoTareaDTE",title : "UI_ESTADO_TAREA",width : "80px"},
						{field: "IdEstadoDTE",title : "UI_ESTADO_DTE",width : "80px"},
						{field: "IdEstadoSII",title : "UI_ESTADO_SII",width : "80px"},
						{field: "RespuestaDTEEnvioSII", attributes:{ style:"white-space:nowrap;" },title : "UI_ID_RESPUESTA_ENVIO_SII",width : "250px"},
						{field: "FechaEnvioDte",title : "UI_FECHA_ENVIO",width : "80px"}
					],
			gridCreated: function(grid) {
				$scope.tareasGrid = grid;
			},

			pageable: true,
			filterable: true,
			height: 400,
			selectable : " row",
			userSettings		: true,
			navigatable			: true,
			excel : {
				allPages: true,
			},
	  		autoBind : false
	};

	
	//COMBOBOX Estados
	$scope.data.oConfiguracionEstadoComprobante = {
			tipo : "combobox-estado-comprobante",
			configuracion : {},
			filtros : {
				read : false
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.buscarTareas = function(){
		var filters = new Array();
		
		//filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.data.IdEmpresa });
		
    	if ($scope.data.IdComprobante){
    		filters.push({ field: "IdComprobante", operator: "contains", value: $scope.data.IdComprobante });
    	}
    	
    	if ($scope.data.oConfiguracionEstadoComprobante.idActual!=0){
    		filters.push({ field: "tolower(IdEstadoTareaDTE)", operator: "contains", value: $scope.data.oConfiguracionEstadoComprobante.idActual.toLowerCase() });
    	}

    	
		if ($scope.conf.kngFechaTareaDesde)
			filters.push({ field: "FechaEnvioDte", operator: "gte", value: util.dateOdata($scope.conf.kngFechaTareaDesde)});

		$scope.tareasGrid.dataSource.filters(filters);	
       
	};
	

	$scope.limpiarFiltros = function()
	{	
		$scope.data.IdComprobante = '';
		$scope.tareasGrid.dataSource.removeAll();		
	};

});