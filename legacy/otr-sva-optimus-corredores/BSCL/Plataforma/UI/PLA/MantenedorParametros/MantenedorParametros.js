
angular.module('ModuloPlataforma').controller('MantenedorParametrosCtrl',function($scope,bcsComunes,$resource,$translate){
	
	$scope.data = {};
	$scope.data.ignorarPrimeraLinea = true;
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	
	if (BCS_ACCESOS.ADMIN) $scope.InfoAdmin="Listado de parametros del sistema:";
	else $scope.InfoAdmin="Listado de parametros del usuario:";
		
	$scope.data.defaultOptions = {
			columns : [
			           {field: "Modulo",width: "100px"},
			           {field: "CodigoParametro",width: "100px"},
			           {field: "Descripcion",width: "200px"},
			           {field: "Valor",width: "100px"},
			           {field: "Uso",width: "100px"},
			           {field: "ValorDefecto",width: "100px"},
			           {field: "ValoresPosibles",width: "100px"},
			           {field: "ModificablePorUsuario",width: "100px"},
			           {field: "ModificablePostProduccion",width: "100px"},
			           {field: "CreadorParametro",width: "100px"},
			           {field: "FechaCreacion",width: "100px"},
			           {field: "ModificadorParametro",width: "100px"},
			           {field: "FechaModificacion",width: "100px"},
			           {field: "Observaciones",width: "200px"},
			           ],
			sortable: true,
	      	reorderable: true,
	      	filterable: true,
			groupable: true,
			selectable: "row",
			reorderable: true,
			allowCopy: true,
			resizable: true,
	      	autoBind: false,
	      	pageable: true,
	      	scrollable: true,
	      	height:400,
	      	userSettings		: true,
			navigatable			: true,
			excel : {
				allPages: true,
			},
	      	filterable: true,
	      	dataSource : new bcs.DataSource({
	      		type: "odata",
	      		definition : {
	      			serverFiltering: true,
					serverSorting: true,
					serverPagina: true,
					pageSize: 1000,
					schema: {
						model : {
							fields : {
								Modulo : { type: "string"},
								CodigoParametro  : { type: "string"},
								Descripcion  : { type: "string"},
								Valor  : { type: "string"},
								Uso : { type: "string"},
								ValorDefecto  : { type: "string"},
								ValoresPosibles  : { type: "string"},
								ModificablePorUsuario  : { type: "string"},
								ModificablePostProduccion : { type: "string"},
								CreadorParametro  : { type: "string"},
								FechaCreacion  : { type: "datetime"},
								ModificadorParametro  : { type: "string"},
								FechaModificacion : { type: "datetime"},
								Observaciones  : { type: "string"},
				           	}
						}
					},
					filter : [ 
		   			          	// Configuración de filtros sobre los datos en el detalle.
								{ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa},
							],
					transport: {
						read: {
							url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=30000",
							dataType: "json"
						}
					}
	      			
	      		}
	      	})
	};
	
	$scope.data.buscar = function() {
		String 
		var filters = new Array();		
		filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa});
		if (BCS_ACCESOS.ADMIN!=true) filters.push({ field: "ModificablePorUsuario", operator: "eq", value: 'S'});	
		$scope.data.defaultOptions.dataSource.filter(filters);
	};
	

	$scope.editarParametro = function(datos){		
		if (datos.length > 1) {
			bcsComunes.msg("error","error",$translate.instant('UI_DEBE_SELECCIONAR_UNICO_REGISTRO'))
		}
		if(datos.ModificablePorUsuario === "N" ) {
			return
		}		
		var idVentanaEdicion = bcsComunes.ventanaAbrir("MantenedorParametrosEditar", {oParametro: datos},false,false,$scope.idVentana)		
		var promesa = bcsComunes.ventanaPromesa(idVentanaEdicion);			

		promesa.then(function(){
			bcsComunes.ventanaOverlay(idVentanaEdicion,false);
			$scope.data.buscar();
		});
	};
	
});