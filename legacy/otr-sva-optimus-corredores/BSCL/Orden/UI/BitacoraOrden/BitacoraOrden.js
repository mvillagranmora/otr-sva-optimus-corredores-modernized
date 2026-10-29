angular.module('ModuloOrdenes').controller('BitacoraOrdenCtrl',function ($scope,bcsComunes,bcsConfiguracionOrdenesV2,bcsConsultasOrdenes) {

		$scope.datos = {};
		var idVentana = bcsComunes.ventanaWinId($scope);
		
		$scope.parametros = bcsComunes.ventanaParams(bcsComunes.ventanaWinId($scope))
		if (!$scope.parametros.IdOrden)
			console.log("Orden no existe!");
		else {

			//cargar grilla aqui
			$scope.configuracionBitacora = {
					autoBind: true,
		      		allowCopy: true,
		      		excel : {
						allPages: true,
					},
					filterable: false,
					groupable: false,
					height:230,
					selectable: "multiple, row",
		      		scrollable: true,
		      		navigatable: true,
		      		
					dataSource: new bcs.DataSource({
			  			type : "odata",
			  			definition : {
							serverFiltering : true, 
							serverPaging : true, 
							pageSize: 200,
							serverSorting : true, 
							sort: { field: "FechaEventoBitacora", dir: "asc" },
							transport: {
								read: {
									url:bcsConsultasOrdenes('ConsultarEventoBitacoraUI'),
									dataType: "json"
								}
							},
							filter: { field: "IdOrden", operator: "eq", value: $scope.parametros.IdOrden },
							schema : {
								model : {
									fields :{
										IdEventoBitacora 				: { type : "number" },
										IdOrden 						: { type : "number" },
										FechaEventoBitacora 			: { type : "sysdatetime" },
										MensajeEventoBitacora 			: { type : "string" },
										DescripcionTipoEventoBitacora 	: { type : "string" },
									}
								}
							}
						},
			  		}),
		  			columns: [
						{field: "IdEventoBitacora",				title: 'UI_SECUENCIA_EVENTO',	width: "100px"},
						{field: "FechaEventoBitacora",			title: 'UI_FECHA_EVENTO',		width: "125px"},
						{field: "DescripcionTipoEventoBitacora",title: 'UI_TIPO_EVENTO',		width: "150px"},
						{field: "MensajeEventoBitacora",		title: 'UI_MENSAJE_EVENTO'},
						
					],
					
				};
		}
		
});
