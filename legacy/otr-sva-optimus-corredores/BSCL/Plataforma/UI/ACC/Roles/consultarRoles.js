angular.module('ModuloPlataforma').controller('ConsultaRolesCtrl',
		function($scope,$translate,$modal,$log,
				bcsComunes, bcsServicioPlataforma)
{

	$scope.data	= {};	
	var overlay = bcsComunes.overlay;
	$scope.rolesGrid = {};
	
	//Seteo de variables de sistema
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdModulo	= 'PLA';
	$scope.data.btnActivos  = true;   //Activa botones
	
	var idVentana = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaTitulo(idVentana, bcsComunes.ventanaGetTitulo(idVentana)+ " - "+ $scope.SesionEmpresa );
	
	
	$scope.confBasicaRoles = {
			dataSource: new bcs.DataSource({
				type : "odata", //Los tipos posibles son: odata, call, array y object
				definition : {
					filter : [ // Configuración de filtros sobre los datos. Es equivalente a una operación "AND".
								{ field: "IdEmpresa", operator: "eq", value: $scope.data.IdEmpresa}
							],
							serverFiltering : true, // Filtrado de datos en el servidor.
							serverPaging : false, // Paginado de los datos en el servidor.
							serverSorting : true, // Ordenamiento de los datos en el servidor.
							sort : { field: "IdRol", dir: "ASC" }, // Criterios de Ordenamiento.
							transport : {
								read : {
									url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1527",
									dataType : "json"
								}
							},
							schema : {
								model: {
									fields : {
										IdEmpresa: {type:"number"},
										IdRol : { type : "number" },
										Nombre : { type : "string" },
										IdEstado: {type: "string"},
										EstadoRol: {type: "string"}
									}
								}
							}
						}
			}),
			columns: [
			          	{field: "IdEmpresa", title:"UI_ID_EMPRESA", width:"50px", hidden:true},
			          	{field : "IdRol", title : "UI_ID_ROL", width : "50px", hidden:true},
						{field : "Nombre",title : "UI_ROL",width : "150px"},
						{field : "IdEstado",title : "UI_ESTADO",width : "50px", hidden:true},
						{field : "EstadoRol",title : "UI_ESTADO",width : "50px"}
						
					],
			gridCreated: function(grid) {
				$scope.rolesGrid = grid;
			},
			sortable: true,
			pageable: false,
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
	
	$scope.confBasicaPerfiles = function(dataItem) {
		var dsDef = {
				type : "odata", //Los tipos posibles son: odata, call, array y object
				definition : {
							serverFiltering : true, // Filtrado de datos en el servidor.
							serverPaging : true, // Paginado de los datos en el servidor.
							serverSorting : true, // Ordenamiento de los datos en el servidor.
							sort : { field: "IdPerfil", dir: "ASC" }, // Criterios de Ordenamiento.
							pageSize : 5, // Número de registros por página.
							transport : {
								read : {
									url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1528",
									dataType : "json"
								}
							},
							schema : {
								model: {
									fields : {
										IdRol:{type: "number"},
										IdPerfil: {type:"number"},
										NombrePerfil : { type : "string" },
										IdEmpresa: {type: "number"},
										IdEstadoPerfil: {type: "string"},
										EstadoPerfil: {type: "string"},
										
									}
								}
							},
							filter : [ 
									{ field: "IdRol", operator: "eq", value: dataItem.IdRol},
									{ field: "IdEmpresa", operator: "eq", value: $scope.data.IdEmpresa},
									{ field: "IdEstadoPerfil", operator: "eq", value: 'ACT'}
								]
						}
		};
		
		return {
			dataSource: new bcs.DataSource(dsDef),
			columns: [
			          	{field : "IdRol",title : "UI_ID_ROL",width : "50px", hidden:true},
			          	{field : "IdPerfil",title : "UI_ID_PERFIL",width : "50px", hidden:true},
						{field : "NombrePerfil",title : "UI_PERFIL",width : "150px"},
						{field : "IdEstadoPerfil",title : "UI_ESTADO",width : "150px", hidden:true},
						{field : "EstadoPerfil",title : "UI_ESTADO",width : "150px"},
						{field : "IdEmpresa",title : "UI_EMPRESA",width : "150px", hidden:true}
					],
			sortable: true,
			pageable: true,
			height: 100,
			selectable : "row",
			autoBind : true
		};

	};
	
	//COMBOBOX Estados
	$scope.data.oConfiguracionEstadoRoles={
		tipo : "combobox-estado-rol",
		configuracion : {},
		filtros : {
			read : false
		},
		idActual : false,
		seleccionado : false,
		idDefecto : 'S'
	};
	
	$scope.buscarRoles = function(){
		var filters = new Array();
		
		filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.data.IdEmpresa });
		
    	if ($scope.data.NombreRol){
    		filters.push({ field: "tolower(Nombre)", operator: "contains", value: $scope.data.NombreRol.toLowerCase() });
    	}
    	
    	if ($scope.data.oConfiguracionEstadoRoles.idActual!=0){
    		filters.push({ field: "tolower(IdEstado)", operator: "contains", value: $scope.data.oConfiguracionEstadoRoles.idActual.toLowerCase() });
    	}
    
			 
	
		$scope.rolesGrid.dataSource.filter(filters);	
       
	};
	
	$scope.exportarRolesCompleto = function(){
		var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo  : 'PLA',
				p_TipoPrograma : [{
					IdInterno : 0,
					IdTipoPrograma : 'ExportarRoles',
					SegundosDesfase : 0
				}],
				p_Parametro : [{
					IdInterno : 0,
					NombreParametro : 'IdEmpresa',
					Valor : $scope.SesionIdEmpresa
				},{
					IdInterno : 0,
					NombreParametro : 'IdUsuario',
					Valor : $scope.SesionIdUsuario
				},{
					IdInterno : 0,
					NombreParametro : 'IdModulo',
					Valor : 'PLA'
				}]
		}
		bcsServicioPlataforma.Servicios().InscribirProcesoBatch(datos).$promise.then(
		function(result) {
			bcsComunes.ventanaOverlay(idVentana,false);
			var fechaLocal=util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion,"FechaHora","CALL");
			bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO').replace("::IdPrograma",result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion",util.formatoSecondate(fechaLocal)),
				conflictos : util.parseServerResponse(result)}, idVentana);

		},
		function(result){
			bcsComunes.ventanaOverlay(idVentana,false);
			bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
		});
	}
	
	
	
	$scope.limpiarFiltros = function()
	{	
		$scope.data.NombreRol = '';
		$scope.rolesGrid.dataSource.removeAll();		
	};
	
	$scope.crearRol = function (){
		
		var id1 = bcsComunes.ventanaAbrir("CrearRoles",{});
		
        var promesa=bcsComunes.ventanaPromesa(id1);             
        bcsComunes.ventanaOverlay(idVentana,true);
        promesa.then(function(data){ 
        	$scope.rolesGrid.dataSource.read();
        	bcsComunes.ventanaOverlay(idVentana,false);
    	}); 
	};
	
	$scope.modificarRol = function (){
		if($scope.seleccionRoles){
			var id1 = bcsComunes.ventanaAbrir("ModificarRoles",$scope.seleccionRoles);
			bcsComunes.ventanaOverlay(idVentana,true);
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){ 
	        bcsComunes.ventanaOverlay(idVentana,false);
	        	$scope.rolesGrid.dataSource.read();
	    	}); 
		}else{
			bcsComunes.msg("error",'Error',$translate.instant("MOD_NO_ROL_SELECT"));
		}
	};
	
});