angular.module('ModuloPlataforma').controller('ModificarRolesUsuarioCtrl',
		function($scope,$translate,$modal,$log,
				bcsComunes, bcsServicioPlataforma)
{

	$scope.data	= {};	
	var overlay = bcsComunes.overlay;
	
	//Seteo de variables de sistema
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdModulo	= 'PLA';
	
	$scope.rolesGrid = {};
	$scope.rolesUsuarioGrid = {};
	$scope.perfilesUsuarioGrid = {};
	
	//Obtener datos desde la pantalla consulta usuarios
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	$scope.data.CodigoUsuario = parametros.CodigoUsuario;
	$scope.data.IdUsuarioRol = parametros.IdUsuario;
	
	bcsComunes.ventanaTitulo(idVentana, bcsComunes.ventanaGetTitulo(idVentana)+" - Usuario: "+ $scope.data.CodigoUsuario+ " - "+ $scope.SesionEmpresa );
	
	// Configuración Básica grilla de órdenes
	$scope.confBasicaRoles = {
		dataSource: new bcs.DataSource({
			type : "odata", //Los tipos posibles son: odata, call, array y object
			definition : {
				filter : 
						[ // Configuración de filtros sobre los datos. Es equivalente a una operación "AND".
							{ field: "IdUsuario", operator: "eq", value: parseInt($scope.data.IdUsuarioRol) },
							{ field: "IdEmpresa", operator: "eq", value: parseInt($scope.data.IdEmpresa) },
							{ field: "IdEstado", operator: "eq", value: "ACT"},
							{ field: "Existe", operator: "eq", value:"N"}
						],
						serverFiltering : true, // Filtrado de datos en el servidor.
						serverPaging : true, // Paginado de los datos en el servidor.
						serverSorting : true, // Ordenamiento de los datos en el servidor.
						sort : { field: "IdRol", dir: "ASC" }, // Criterios de Ordenamiento.
						pageSize : 20, // Número de registros por página.
						transport : {
							read : {
								url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1522",
								dataType : "json"
							}
						},
						schema : {
							model: {
								fields : {
									IdRol : { type : "number" },
									Nombre : { type : "string" },
									EsAutorizado: {type: "string"}
								}
							}
						}
					}
		}),
		columns: [
					{field : "IdRol", title : "UI_ID_ROL", width : "50px", hidden:true},
					{field : "Nombre",title : "UI_ROL",width : "150px"}
				],
		gridCreated: function(grid) {
			$scope.rolesGrid = grid;
		},
		sortable: true,
		pageable: true,
		height: 200,
		selectable : "multiple row",
  		autoBind : true
	};

	$scope.confBasicaRolesUsuario = {
		dataSource: new bcs.DataSource({
			type : "odata", //Los tipos posibles son: odata, call, array y object
			definition : {
				filter : [ // Configuración de filtros sobre los datos. Es equivalente a una operación "AND".
							{ field: "IdUsuario", operator: "eq", value: parseInt($scope.data.IdUsuarioRol)},
							{ field: "EsAutorizado", operator: "eq", value: 'S'}
						],
						serverFiltering : true, // Filtrado de datos en el servidor.
						serverPaging : true, // Paginado de los datos en el servidor.
						serverSorting : true, // Ordenamiento de los datos en el servidor.
						sort : { field: "IdUsuario", dir: "ASC" }, // Criterios de Ordenamiento.
						pageSize : 20, // Número de registros por página.
						transport : {
							read : {
								url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1523",
								dataType : "json"
							}
						},
						schema : {
							model: {
								fields : {
									IdEmpresa: {type:"number"},
									IdRol : { type : "number" },
									NombreRol : { type : "string" },
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
					{field : "NombreRol",title : "UI_ROL",width : "150px"},
					{field : "IdEstado", title : "UI_ESTADO", width : "50px", hidden:true},
					{field : "EstadoRol", title : "UI_ESTADO", width : "50px"},
					
				],
		gridCreated: function(grid) {
			$scope.rolesUsuarioGrid = grid;
		},
		sortable: true,
		pageable: true,
		height: 200,
		selectable : "multiple row",
  		autoBind : true
	};

	$scope.confBasicaPerfilesUsuario = {
		dataSource: new bcs.DataSource({
			type : "odata", //Los tipos posibles son: odata, call, array y object
			definition : {
				filter : [ // Configuración de filtros sobre los datos. Es equivalente a una operación "AND".
							{ field: "IdUsuario", operator: "eq", value: parseInt($scope.data.IdUsuarioRol)},
							{ field: "EsAutorizado", operator: "eq", value: 'S'}
						],
						serverFiltering : true, // Filtrado de datos en el servidor.
						serverPaging : true, // Paginado de los datos en el servidor.
						serverSorting : true, // Ordenamiento de los datos en el servidor.
						sort : { field: "IdUsuario", dir: "ASC" }, // Criterios de Ordenamiento.
						pageSize : 20, // Número de registros por página.
						transport : {
							read : {
								url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1521",
								dataType : "json"
							}
						},
						schema : {
							model: {
								fields : {
									IdPerfil: {type:"string"},
									NombrePerfil : { type : "string" },
									IdEstadoPerfil: {type: "string"},
									EstadoPerfil: {type: "string"}
								}
							}
						}
					}
		}),
		columns: [
		          	{field : "IdPerfil",title : "UI_ID_PERFIL",width : "50px", hidden:true},
					{field : "NombrePerfil",title : "UI_PERFIL",width : "150px"},
					{field : "IdEstadoPerfil",title : "UI_ESTADO",width : "150px", hidden:true},
					{field : "EstadoPerfil",title : "UI_ESTADO",width : "150px"}
				],
		gridCreated: function(grid) {
			$scope.perfilesUsuarioGrid = grid;
		},
		sortable: true,
		pageable: true,
		height: 200,
		selectable : "row",
  		autoBind : true
	};
	
	$scope.addRol = function(){
		if($scope.seleccionRoles && $scope.seleccionRoles.length >0){
			//Agregar titulo operacion
			
			$.each($scope.seleccionRoles,function(key,value){
				value.operacion = 'Agregar Rol: ' +value.Nombre ;
				value.codigoUsuario = $scope.data.CodigoUsuario;
			});
			
			$scope.callModalRoles($scope.seleccionRoles, "ADDROL");
			
		}else{
			bcsComunes.msg("error",'Error',$translate.instant("NO_ROL_ADD"));
		}
	};
	
	$scope.removeRol= function(){
		
		if($scope.seleccionRolesUsuario && $scope.seleccionRolesUsuario.length > 0){
			$.each($scope.seleccionRolesUsuario,function(key,value){
				value.operacion = 'Eliminar Rol: ' +value.NombreRol ;
				value.codigoUsuario = $scope.data.CodigoUsuario;
			});
			
			$scope.callModalRoles($scope.seleccionRolesUsuario, "DELROL")
			
		}else{
			bcsComunes.msg("error",'Error',$translate.instant("NO_ROL_DEL"));
		}
	};
	
	$scope.callModalRoles = function(data, codOperacion){
		
		var id1 = bcsComunes.ventanaAbrir("RolesUsuarioModal",{rolesData: data, operacion: codOperacion}, false,true);
		
	    var promesa=bcsComunes.ventanaPromesa(id1);                               
	    promesa.then(function(data){ 
	    	if (data === true)
			{
				//Vaciar objeto de roles seleccionados
	    		$scope.seleccionRoles = [];
	    		$scope.seleccionRolesUsuario = [];
	    		//Refrescar grilla para visualizar cambios.
	    		$scope.rolesGrid.dataSource.read();
	    		$scope.rolesUsuarioGrid.dataSource.read();
	    		$scope.perfilesUsuarioGrid.dataSource.read();
			}
		});
	};
	
});