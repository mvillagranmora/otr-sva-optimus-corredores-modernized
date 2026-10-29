angular.module('ModuloSocioNegocio').controller('ConsultaGruposClientesCtrl',
		function($scope,$translate,
				bcsComunes, servicioSociodeNegocio)
{

	$scope.data	= {};
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	
	//Seteo de variables de sistema
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdModulo	= 'SOC';
	
	//GRILLA PRINCIPAL DE MOVIMIENTOS A FACTURAR
	$scope.confBasicaGrupos = {
		dataSource : new bcs.DataSource({
			type 		: "odata", 
			definition 	: {
				filters :  
					{ field: "IdEmpresa", operator: "eq", value: $scope.data.IdEmpresa}
				,
				serverFiltering 	: true, 
				serverSorting 		: true, 
				groupable 			: true,
				sort 				: { field: "IdGrupo", dir: "asc" },
				transport 			: {
					read : {
						url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=700003",
						dataType : "json"
					}
				},
				schema : {
					model: {
						fields : {
							IdGrupo 		: {type: "string"},
							Nombre 			: {type: "string"},
							IdEstado 		: {type: "string"},
							Codigo	 		: {type: "string"},
							EstadoGrupo		: {type:"string"}
									}
								}
							},
						}
		}),
					
		gridCreated: function(grid) {
			$scope.gridApiGrupos = grid;
		},
		columns : [
		           {field: "Nombre",title: "UI_NOMBRE",width: "300px"},
		           {field: "Codigo",title: "UI_CODIGO",width: "150px"},
		           {field: "EstadoGrupo",title: "UI_ESTADO"}
				],		
		sortable			: true,
		autoBind			: true,
		height				: 340,
		userSettings		: true,
		navigatable			: true,
		excel : {
			allPages: true,
		},
		selectable 			: "row"
	};
	
	//SUB-GRILLA DE PERFILES 
	$scope.confBasicaPortafolio = function(dataItem) {
		var dsDef = {
				type 		: "odata",
				definition 	: {
					serverFiltering 	: true, 
					serverSorting 		: true, 
					sort 				: { field: "DocumentoIdentidad", dir: "asc" }, 
					transport 			: {
						read : {
							url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=700004",
							dataType : "json"
						}
					},
					schema	: {
						model: {
							fields : {
								IdGrupoPortafolio 		: {type: "number"},
								IdGrupo 				: {type: "number"},
								NombreGrupo 			: {type: "string"},
								IdCliente 				: {type: "string"},
								IdEstadoCliente 		: {type: "string"},
								EstadoCliente			: {type:"string"},
								DocumentoIdentidad 		: {type: "string"},
								RazonSocial 			: {type: "string"},
								NombreCompleto 			: {type: "string"},
								IdSegmentoCliente 		: {type: "string"},
								NombreSegmentoCliente 	: {type: "string"},
								IdPortafolio 			: {type: "string"}, 
								IdMoneda				: {type: "string"},
								IdEstadoPortafolio 		: {type: "string"}, 
								EstadoPortafolio 		: {type: "string"}, 
								CodigoPortafolio 		: {type: "string"},
								Proposito				: {type: "string"},
								DescripcionPortafolio 	: {type: "string"}
							}
						}
					},
					filter : [{ field: "IdGrupo", operator: "eq", value: Number(dataItem.IdGrupo)}]
				}
			};
		return {
			dataSource: new bcs.DataSource(dsDef),
			sortable			: true,
			height				: 200,
			selectable 			: "row",
			autoBind            : true,
			columns				: [
									{field: "DocumentoIdentidad",	title: "UI_DOCUMENTO_IDENTIDAD",	width: "75px"},
									{field: "NombreCompleto",		title: "UI_NOMBRE_COMPLETO",		width: "150px"},
									{field: "CodigoPortafolio",		title: "UI_CODIGO_PORTAFOLIO",		width: "85px"},
									{field: "EstadoPortafolio",		title: "UI_ESTADO_PORTAFOLIO",		width: "75px"}
			 ],
			gridCreated: function(grid) {
				$scope.data.detalleGrid = grid;
			},
		};
	};
	
	//COMBOBOX Estados
	$scope.data.oConfiguracionEstadosGrupoPortafolio={
		idActual : false,
		seleccionado : false,
	};
	
	
	$scope.buscar = function(){
		var filters = new Array();
		
		filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.data.IdEmpresa });
		
    	if ($scope.data.NombreGrupo){
    		filters.push({ field: "tolower(Nombre)", operator: "contains", value: $scope.data.NombreGrupo.toLowerCase() });
    	}
    	
    	if ($scope.data.oConfiguracionEstadosGrupoPortafolio.idActual!=0){
    		filters.push({ field: "tolower(IdEstado)", operator: "contains", value: $scope.data.oConfiguracionEstadosGrupoPortafolio.idActual.toLowerCase() });
    	}
		$scope.gridApiGrupos.dataSource.filters(filters);	
       
	};
	
	$scope.limpiarFiltros = function()
	{	
		$scope.data.NombreGrupo = '';
		$scope.data.oConfiguracionEstadosGrupoPortafolio.idActual= '';
	};
	
	$scope.crearGrupo = function(){	
		var id1 = bcsComunes.ventanaAbrir("ModificarGrupoClientes", {Modo : "NUEVO"},false,false,$scope.idVentana);
		var promesa=bcsComunes.ventanaPromesa(id1);                               
		promesa.then(function(data){
			$scope.seleccionDetalleGrupo = undefined;
			$scope.gridApiGrupos.dataSource.read();
		});
	};
	
	$scope.modificarGrupo = function(sel){
		if (!sel) return;
		var grupo = {
			Modo 	: "EDITAR",
			IdGrupo	: $scope.seleccionDetalleGrupo.IdGrupo,
			Nombre	: $scope.seleccionDetalleGrupo.Nombre,
			Codigo	: $scope.seleccionDetalleGrupo.Codigo,
			Estado	: $scope.seleccionDetalleGrupo.IdEstado
		};
		
		var id1 = bcsComunes.ventanaAbrir("ModificarGrupoClientes", grupo,false,false,$scope.idVentana);
		var promesa=bcsComunes.ventanaPromesa(id1);                               
		promesa.then(function(data){
			$scope.seleccionDetalleGrupo = undefined;
			$scope.gridApiGrupos.dataSource.read();
		});
	}
	
});	