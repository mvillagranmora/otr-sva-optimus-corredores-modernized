angular.module('ModuloPlataforma').controller('ModificarRolesCtrl',
		function($scope,$translate,$modal,$log,$resource,
				bcsComunes, bcsServicioPlataforma)
{

	$scope.data	= {};
	$scope.perfilesGrid = {};
	$scope.perfilesActualesGrid = {};
	$scope.accesosGrid = {};
	
	var overlay = bcsComunes.overlay;
	
	//Seteo de variables de sistema
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdModulo	= 'PLA';
	
	//Obtener datos desde la pantalla consulta usuarios
	$scope.idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams($scope.idVentana);
	$scope.data.IdRol = parametros.IdRol;
	$scope.data.nombreRol = parametros.Nombre;
	//Si el estado es ACT: 1, si no, es INA: 2
	$scope.data.idEstado = parametros.IdEstado;

	bcsComunes.ventanaTitulo($scope.idVentana, bcsComunes.ventanaGetTitulo($scope.idVentana)+ " - "+ $scope.SesionEmpresa );
	
	
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
	
	// Configuración Básica grilla de órdenes
	$scope.confBasicaPerfiles = {
		dataSource: new bcs.DataSource({
			type : "odata", //Los tipos posibles son: odata, call, array y object
			definition : {
						filter : 
						[ // Configuración de filtros sobre los datos. Es equivalente a una operación "AND".
							{ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa },
							{ field: "IdRol", operator: "eq", value: $scope.data.IdRol },
							{ field: "Existe", operator: "eq", value:"N"},
							{ field: "IdEstadoPerfil", operator: "eq", value: 'ACT'}
						],
						serverFiltering : true, // Filtrado de datos en el servidor.
						serverPaging : true, // Paginado de los datos en el servidor.
						serverSorting : true, // Ordenamiento de los datos en el servidor.
						sort : { field: "IdRol", dir: "ASC" }, // Criterios de Ordenamiento.
						pageSize : 20, // Número de registros por página.
						transport : {
							read : {
								url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1529",
								dataType : "json"
							}
						},
						schema : {
							model: {
								fields : {
									IdRolPerfil : { type : "number" },
									IdEmpresa : { type : "string" },
									IdRol: {type: "number"},
									NombreRol: {type: "string"},
									IdPerfil: {type: "number"},
									NombrePerfil:{type: "string"},
									Existe:{type: "string"}
								}
							}
						}
					}
		}),
		columns: [
		          	{field : "IdRol", title : "UI_ID_ROL", width : "50px", hidden:true},
		          	{field : "IdEmpresa", title : "UI_ID_ROL", width : "50px", hidden:true},
					{field : "IdPerfil", title : "UI_ID_ROL", width : "50px", hidden:true},
					{field : "NombrePerfil",title : "UI_ROL",width : "150px"}
				],
		gridCreated: function(grid) {
			$scope.perfilesGrid = grid;
		},
		sortable: true,
		pageable: true,
		height: 200,
		selectable : "multiple row",
  		autoBind : true
	};
	
	$scope.confBasicaPerfilesRol = {
		dataSource: new bcs.DataSource({
			type : "odata", //Los tipos posibles son: odata, call, array y object
			definition : {
				filter : [ // Configuración de filtros sobre los datos. Es equivalente a una operación "AND".
							{ field: "IdRol", operator: "eq", value: $scope.data.IdRol},
							{ field: "IdEstadoPerfil", operator: "eq", value: 'ACT'}
						],
						serverFiltering : true, // Filtrado de datos en el servidor.
						serverPaging : true, // Paginado de los datos en el servidor.
						serverSorting : true, // Ordenamiento de los datos en el servidor.
						sort : { field: "IdPerfil", dir: "ASC" }, // Criterios de Ordenamiento.
						pageSize : 20, // Número de registros por página.
						transport : {
							read : {
								url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1528",
								dataType : "json"
							}
						},
						schema : {
							model: {
								fields : {
									IdEmpresa: {type:"number"},
									IdRolPerfil : { type : "number" },
									IdRol: {type: "number"},
									IdPerfil : { type : "number" },
									NombrePerfil : { type : "string" }
								}
							}
						}
					}
		}),
		columns: [
		          	{field: "IdEmpresa", title:"UI_ID_EMPRESA", width:"50px", hidden:true},
		          	{field : "IdPerfil", title : "UI_ID_PERFIL", width : "50px", hidden:true},
					{field : "NombrePerfil",title : "UI_PERFIL",width : "150px"}
					
				],
		gridCreated: function(grid) {
			$scope.perfilesActualesGrid = grid;
		},
		sortable: true,
		pageable: true,
		height: 200,
		selectable : "multiple row",
  		autoBind : true
	};
	
	$scope.confBasicaAccesosPerfil = {
		dataSource: new bcs.DataSource({
			type : "odata", //Los tipos posibles son: odata, call, array y object
			definition : {
				filter: [ // Configuración de filtros sobre los datos. Es equivalente a una operación "AND".
				            { field: "IdRol", operator: "eq", value: $scope.data.IdRol},
				            { field: "IdEstadoAccesoPerfil", operator: "eq", value: 'ACT'}
						],
						serverFiltering : true, // Filtrado de datos en el servidor.
						serverPaging : true, // Paginado de los datos en el servidor.
						serverSorting : true, // Ordenamiento de los datos en el servidor.
						sort : { field: "IdRol", dir: "ASC" }, // Criterios de Ordenamiento.
						pageSize : 20, // Número de registros por página.
						transport : {
							read : {
								url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1524",
								dataType : "json"
							}
						},
						schema : {
							model: {
								fields : {
									NombrePerfil: {type: "string"},
									IdAcceso: {type: "number"},
									NombreAcceso: {type:"string"},
									TipoAcceso: {type:"string"},
									Padre: {type: "number"},
									Orden: {type: "number"},
									IdServicio: {type: "number"},
									IdElementoHtml: {type: "string"},
									EsVisibleMenu: {type: "string"},
								}
							}
						}
					}
		}),
		columns: [
		          	{field: "NombrePerfil",title: $translate.instant("UI_PERFIL"), width: "100px"},
					{field: "IdAcceso",title: $translate.instant("UI_ID_ACCESO"), width: "25px", hidden:true},
					{field: "NombreAcceso",title: $translate.instant("UI_ACCESO"), width: "100px"},
					{field: "TipoAcceso",title: $translate.instant("UI_TIPO_ACCESO"), width: "40px"},
					{field: "Padre",title: $translate.instant("UI_PADRE"),width: "75px", template: kendo.template("# ##=util.getNombreAccesoPadre(Padre)## #")},
					{field: "Orden",title: $translate.instant("UI_ORDEN"),width: "25px", hidden:true},
					{field: "IdServicio",title: $translate.instant("UI_ID_SERVICIO"),width: "25px", hidden:true},
					{field: "EsVisibleMenu",title: $translate.instant("UI_ES_VISIBLE"),width: "30px"}
				],
		gridCreated: function(grid){
			$scope.accesosGrid = grid;
		},
		sortable: true,
		pageable: true,
		height: 200,
		selectable : "row",
  		autoBind : true
	};
	
	$scope.actualizarRol = function(){
		if($scope.data.nombreRol.trim().length>0){
				 if($scope.data.oConfiguracionEstadoRoles.seleccionado){
					 //llamar procedimiento almacenado de actualizacion.
					 
					 
					 var urlUpdateRol = "/BSCL/Plataforma/GWJAVA/PLA/call?t=132";
						var r = $resource("", {},
					    {
							'ActualizarRol': { method: "POST", url: urlUpdateRol}
						});
						
						var datosUpdate= {
							p_IdUsuario: $scope.SesionIdUsuario,
							p_IdEmpresa : $scope.SesionIdEmpresa,
							p_IdModulo: 'PLA',
							p_IdRol: $scope.data.IdRol,
							p_Nombre: $scope.data.nombreRol,
							p_IdEstado: $scope.data.oConfiguracionEstadoRoles.seleccionado.IdEstado
						};
						
						var post = r.ActualizarRol(datosUpdate);
						bcsComunes.ventanaOverlay($scope.idVentana, true);
						
						post.$promise.then(function(result){
                            bcsComunes.ventanaOverlay($scope.idVentana, false);
							bcsComunes.ventanaMsg({ 
								mensaje: $translate.instant("UI_ROL_ACTUALIZADO_OK"),
								conflictos:util.parseServerResponse(result)
							},$scope.idVentana);
					
						}, function(result){
							bcsComunes.ventanaOverlay($scope.idVentana, false);
							bcsComunes.ventanaMsg({ 
								mensaje: $translate.instant("UI_ERROR"),
								conflictos:util.parseServerResponse(result)
							},$scope.idVentana);
						});
					 
				 }else{
						bcsComunes.msg("error",'Error',"Estado Rol vacio.");
				 }
		}else{
			bcsComunes.msg("error",'Error',"Nombre Rol Vacio.");
		}
		
	};
	
	$scope.addPerfil = function(){
		if($scope.seleccionPerfiles && $scope.seleccionPerfiles.length >0){
			
			$.each($scope.seleccionPerfiles,function(key,value){
				value.operacion = 'Agregar Perfil: ' +value.NombrePerfil ;
				value.dataOperacion = {
						p_IdEmpresa : $scope.SesionIdEmpresa,
						p_IdUsuario: $scope.SesionIdUsuario,
						p_IdModulo: 'PLA',
						//Datos
						p_IdRol: value.IdRol,
						p_IdPerfil: value.IdPerfil,
						p_IdEstado: 'ACT'
					};
			});
			
			$scope.callModalRol($scope.seleccionPerfiles, 128);
			
		}else{
			bcsComunes.msg("error",'Error',$translate.instant("NO_PERF_ADD"));
		}
	};
	
	$scope.removePerfil= function(){
		if($scope.seleccionPerfilesRol && $scope.seleccionPerfilesRol.length > 0){
			$.each($scope.seleccionPerfilesRol,function(key,value){
				value.operacion = 'Eliminar Perfil: ' +value.NombrePerfil;
				value.dataOperacion = {
						p_IdEmpresa : $scope.SesionIdEmpresa,
						p_IdUsuario: $scope.SesionIdUsuario,
						p_IdModulo: 'PLA',
						//Datos
						p_IdRolPerfil: value.IdRolPerfil
					};
			});
			
			$scope.callModalRol($scope.seleccionPerfilesRol, 129);
			
		}else{
			bcsComunes.msg("error",'Error',$translate.instant("NO_PERF_DEL"));
		}
	};
	
	$scope.callModalRol = function(seleccion, codOperacion){
		
		var id1 = bcsComunes.ventanaAbrir("RolesModal",{data: seleccion, operacion: codOperacion}, false,true);
		
	    var promesa=bcsComunes.ventanaPromesa(id1);                               
	    promesa.then(function(data){ 
	    	if (data === true)
			{
				//Vaciar objeto de roles seleccionados
	    		$scope.seleccionPerfiles = [];
	    		$scope.seleccionPerfilesRol = [];
	    		//Refrescar grilla para visualizar cambios.
	    		$scope.perfilesGrid.dataSource.read();
	    		$scope.perfilesActualesGrid.dataSource.read();
	    		$scope.accesosGrid.dataSource.read();
			}
		});
	};
	/*
	$scope.getNombrePadre = function(padre){
		$each($scope.gridApiAccesosPerfil.dataSource.data(), function(key, value){
			if(padre == value.Padre ){
				return value.NombreAcceso;
			}
		});
	};
	*/
	
});


