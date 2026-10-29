angular.module('ModuloOrdenes').controller('ConsultarCorredorCorresponsalCtrl',	function($scope,bcsComunes,bcsConsultasOrdenes){
		
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	
	$scope.configuracion =  {
		dataSource		: new bcs.DataSource({
			type 		: "odata",
			definition 	: {
				serverFiltering : true,
				serverPaging 	: true,
				serverSorting 	: true,
				sort: { field: "IdCorredorCorresponsal", dir: "desc" },
				filter : { field : "IdEmpresa",operator : "eq" , value : $scope.SesionIdEmpresa },
				schema 			: {
					model	: {
	                    fields	: {
	                    	IdPais						: { type: "number"},
	                    	NombrePais					: { type: "string"},
	                    	CodigoCorredor				: { type: "string"},
	                    	IdPersona					: { type: "number"},
	                    	IdEmpresa					: { type: "number"},
	                    	NombreCompleto 				: { type: "string"},
	                    	IdEstado					: { type: "string"},
	                    	NombreEstado				: { type: "string"},
	                    	IdPerfilCobros				: { type: "string"},
	                    	DescripcionPerfilCobros		: { type: "string"},
	                    	DefaultPais					: { type: "string"},
	                    	DefaultPaisUI				: { type: "string"}
                        }
               		}
				},
				transport : {
					read : {
						url :bcsConsultasOrdenes('ConsultarCorredorCorresponsal'),
						dataType : "json"
					}
				},
			}
		}),
		columns	: [
			{field: "CodigoCorredor",			width: "100px",	title: "UI_CODIGO"},
			{field: "NombrePais",				width: "80px",	title: "UI_PAIS"},
			{field: "NombreCompleto",			width: "150px",	title: "UI_NOMBRE"},
			{field: "DescripcionPerfilCobros",	width: "200px",	title: "UI_PERFIL_COBROS"},
			{field: "DefaultPaisUI",			width: "80px",	title: "UI_POR_DEFECTO_PAIS"},
			{field: "NombreEstado",				title: "UI_ESTADO"},
		 	
		],
		gridCreated : function(e){
			$scope.gridApi = e;
		},
		autoBind			: true,
		height				: 200,
		userSettings		: true,
		navigatable			: true,
	    selectable 			: "row"
	};
	
	if ($scope.SesionLocale === 'PE') {	
		$scope.configuracionPlatillaProductos =  {
			dataSource		: new bcs.DataSource({
				type 		: "odata",
				definition 	: {
					serverFiltering : true,
					serverPaging 	: true,
					serverSorting 	: true,
					schema 	: {
						model	: {
		                    fields	: {
		                    	IdCorredorCorresponsal	: { type: "number"},
		                    	IdPais					: { type: "number"},
		                    	IdPersona				: { type: "number"},
		                    	CodigoCorredor			: { type: "string"},
		                    	IdTipoProducto			: { type: "string"},
		                    	NombreTipoProducto		: { type: "string"},
		                    	IdBoveda				: { type: "string"},
		                    	Codigo					: { type: "string"},
		                    	Nombre					: { type: "string"},
		                    	IdEstadoBoveda			: { type: "string"},
		                    	IdPerfilCobros			: { type: "string"}
	                        }
	               		}
					},
					transport : {
						read : {
							url :bcsConsultasOrdenes('ConsultarProductoBovedaCorredorCorresponsal'),
							dataType : "json"
						}
					},/*
					group: [
			        	{field: "NombreTipoProducto"}
			        ]*/
			        
				}
			}),
			columns	: [
				{field: "NombreTipoProducto",	width: "160px",	title: "UI_TIPO_PRODUCTO"},
				{field: "Codigo",				width: "60px",	title: "UI_BOVEDA"},
				{field: "Nombre",				width: "150px",	title: "UI_NOMBRE"},
				{field: "IdEstadoBoveda",		width: "50px",	title: "UI_ESTADO"},
				//{field: "IdPerfilCobros",	width: "80px",	title: "UI_PERFIL_COBROS"}
			 	
			],
			gridCreated : function(e){
				$scope.gridApiProducto = e;
			},
			autoBind			: false,
			height				: 130,
			userSettings		: true,
			navigatable			: true,
		    selectable 			: "row"
		};
	}
	$scope.ingresarCorredorCorresponsal = function() {
		var id1 = bcsComunes.ventanaAbrir("CrearCorredorCorresponsal", {},false,false,$scope.idVentana);
		var promesa=bcsComunes.ventanaPromesa(id1);
		promesa.then(function(data){ 
			if (data) 
				$scope.gridApi.dataSource.read();
		});
		
	}
	
	$scope.modificarCorredorCorresponsal = function(seleccionado) {
		if (!seleccionado) return;
		var id1 = bcsComunes.ventanaAbrir("ModificarCorredorCorresponsal", seleccionado ,false,false,$scope.idVentana);
		var promesa=bcsComunes.ventanaPromesa(id1);
		promesa.then(function(data){ 
			if (data === true) {
				$scope.seleccionado = undefined;
				$scope.gridApi.dataSource.read();
				if($scope.SesionLocale === 'PE'){
					$scope.gridApiProducto.dataSource.read();	
				}
			}
		});	
	}
	
	$scope.consultarProductoCorredor = function(seleccionado){
		if (!seleccionado) return;
		$scope.gridApiProducto.dataSource.filter({ field: "IdCorredorCorresponsal", operator: "eq", value: seleccionado.IdCorredorCorresponsal });
	}
		
});