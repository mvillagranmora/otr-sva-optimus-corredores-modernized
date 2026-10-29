angular.module('ModuloOrdenes').controller('BitacoraInstruccionCtrl', function ($scope, bcsComunes, $translate, bcsConsultasOrdenes,bcsConfiguracionOrdenesV2) {
	(function init() {
		$scope.idVentana = bcsComunes.ventanaWinId($scope);
		$scope.parametros = bcsComunes.ventanaParams(bcsComunes.ventanaWinId($scope)) || {};

		if (!$scope.parametros.idInstruccion) {
			const id = bcsComunes.ventanaMsgErr($translate.instant('UI_DEBE_SELECCIONAR_INSTRUCCION'), $scope.idVentana);
			bcsComunes.ventanaPromesa(id).then(function () {
				bcsComunes.ventanaCerrar($scope.idVentana);
			});
		}
	})();

	$scope.grillaBitacoraConfig = {
		autoBind: true,
		allowCopy: true,
		excel: {
			allPages: true,
		},
		filterable: false,
		groupable: false,
		height: 230,
		selectable: 'multiple, row',
		scrollable: true,
		navigatable: true,
		dataSource: new bcs.DataSource({
			type: 'odata',
			definition: {
				serverFiltering: true,
				serverPaging: true,
				pageSize: 200,
				serverSorting: true,
				sort: { field: 'FechaEventoBitacora', dir: 'asc' },
				transport: {
					read: {
						url: bcsConsultasOrdenes('ConsultarEventoBitacoraUI'),
						dataType: 'json'
					}
				},
				filter: [
					{ field: 'NombreEntidad', operator: 'eq', value: 'INSTRUCCION' },
					{ field: 'IdEntidad', operator: 'eq', value: $scope.parametros.idInstruccion },
				],
				schema: {
					model: {
						fields: {
							IdEventoBitacora: { type: 'number' },
							IdEntidad: { type: 'number' },
							IdOrden: { type: 'number' },
							IdTransaccion: { type: 'number' },
							IdAsignacion: { type: 'number' },
							FechaEventoBitacora: { type: 'sysdatetime' },
							MensajeEventoBitacora: { type: 'string' },
							DescripcionTipoEventoBitacora: { type: 'string' },
						}
					}
				}
			},
		}),
		columns: [
			{ field: 'IdEventoBitacora', title: 'UI_SECUENCIA_EVENTO', width: '100px' },
			{ field: 'FechaEventoBitacora', title: 'UI_FECHA_EVENTO', width: '125px' },
			{ field: 'DescripcionTipoEventoBitacora', title: 'UI_TIPO_EVENTO', width: '150px' },
			{ field: 'MensajeEventoBitacora', title: 'UI_MENSAJE_EVENTO' },
		],
	};
	
	$scope.grillaArchivosConfig = {
			dataSource : new bcs.DataSource({
				type 		: "odata",
				definition 	: {
					serverFiltering 	: false,
					serverPaging 		: true,
					serverSorting 		: true,
					groupable 			: true, 
					sort 				: { field: "FechaActualizacion", dir: "asc" },
					pageSize 			: 25,
					transport : {
	                    read : {
	                        url: bcsConsultasOrdenes('ConsultarDocumentoInstruccion'),
	                        dataType: "json"
	                    }
	                },
					filter: [
					    { field: "IdInstruccion", operator: "eq", value: Number($scope.parametros.idInstruccion) },
					    { field: "Borrado", operator: "eq", value: 'N' }
					],
					schema : bcsConfiguracionOrdenesV2.getSchemaDocumentoInstruccion()
				}
			}),
			columns : [
				{field: "NombreDocumentoAdjunto",title: "UI_NOMBRE_ARCHIVO",width: "150px"},
				{field: "Descripcion",title: "UI_DESCRIPCION",width: "150px"},
				{field: "FechaActualizacion",title: "UI_FECHA_ACTUALIZACION", width: "100px"}
				
			],
			
			sortable			: false,
			pageable			: false,
			filterable			: false,
			groupable           : false,
			autoBind			: true,
			height				: 120,
			width				: 820,
			userSettings		: true,
			navigatable			: true,
			excel : {
				allPages: true,
			},
			selectable 			: "row"
		};
});
