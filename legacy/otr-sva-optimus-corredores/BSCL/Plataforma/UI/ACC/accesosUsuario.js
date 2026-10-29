angular.module('ModuloPlataforma').controller('ConsultarAccesosUsuarioCtrl',
		function($scope,$translate,$modal,$log,
				$resource,
				bcsComunes, bcsServicioPlataforma)
{

	$scope.data	= {};
	$scope.modificaciones = {};	
	var overlay = bcsComunes.overlay;
	var idVentana = bcsComunes.ventanaWinId($scope);
	
	//Seteo de variables de sistema
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdModulo	= 'PLA';
	
	//GRILLA PRINCIPAL DE ARCHIVOS
	$scope.confGridAccesosUsuarios= {
		dataSource			: new bcs.DataSource({
								type 		: "odata", //Los tipos posibles son: odata, call, array y object
								definition 	: {
									serverFiltering 	: true, // Filtrado de datos en el servidor.
									serverPaging 		: true, // Paginado de los datos en el servidor.
									serverSorting 		: true, // Ordenamiento de los datos en el servidor.
									groupable 			: true, 
									//sort 				: { field: "FechaModificacion", dir: "asc" }, // Criterios de Ordenamiento.
									pageSize 			: 200, // Número de registros por página.
									filter :  // Configuración de filtros sobre los datos. Es equivalente a una operación "AND".
										[
											{ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa},
											{ field: "Existe ", operator: "eq", value: "S"}
										 ]
									,
									transport 			: {
															read : {
																url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1697",
																dataType : "json",
																data:{
																	$select: "\"IdUsuario\",\"CodigoUsuario\",\"IdAcceso\",\"NombreAcceso\",\"TipoAcceso\",\"Padre\",\"Orden\",\"IdServicio\",\"IdElementoHtml\",\"EsVisibleMenu\",\"Existe\",\"MostrarAcceso\", \"NombreTipoAcceso\"",
																}
															}
														},
									schema 				: {
										model: {
											fields : {
												IdUsuario 		: {type : "number"},
												CodigoUsuario 	: {type : "string"},
												IdAcceso		: {type : "number"},
												NombreAcceso	: {type : "string"},
												TipoAcceso		: {type : "string"},
												Padre			: {type : "number"},
												Orden			: {type : "number"},
												IdServicio		: {type : "number"},
												IdElementoHtml	: {type : "string"},
												EsVisibleMenu	: {type : "string"},
												Existe			: {type : "string"},
												MostrarAcceso	: {type : "string"},
												NombreTipoAcceso: {type : "string"},
											}
										}
									}
								}
							}),
		columns				: [
								{field : "CodigoUsuario",title: "UI_USUARIO",width: "150px"},
								{field : "NombreAcceso",title: "UI_ACCESO",width: "150px"},
								{field : "TipoAcceso",title: "UI_TIPO_ACCESO",width: "150px", hidden:true},
								{field : "NombreTipoAcceso",title: "UI_OPCION",width: "150px"}
						],
		gridCreated: function(grid) {
			$scope.data.mainGrid = grid;
		},
		userSettings		: true ,
		navigatable			: true,
		excel : {
			allPages: true,
		},
		sortable			: true,
		pageable			: true,
		filterable			: true,
		groupable           : true,
		autoBind			: false,
		height				: 300,
		selectable 			: "row"
	};
	
	$scope.buscar = function(){
		var filters = new Array();
		
		filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa });
		filters.push({ field: "Existe", operator: "eq", value: "S" });
		if($scope.data.estado!=0)
			filters.push({ field: "IdAcceso", operator: "eq", value: parseInt($scope.data.estado) });
		
		$scope.data.mainGrid.dataSource.filters(filters);
	};
	
});	