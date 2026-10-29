angular.module('ModuloPlataforma').controller('ConsultarPerfilesCtrl',
		function($scope,$translate,$modal,$log,
				bcsComunes, bcsServicioPlataforma)
{

	$scope.data	= {};
	$scope.perfilesGrid = {};
	var overlay = bcsComunes.overlay;
	
	//Seteo de variables de sistema
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdModulo	= 'PLA';
	$scope.data.btnActivos  = true;   //Activa botones
	$scope.perfiles = {};
	$scope.accesos = {};
	
	var idVentana = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaTitulo(idVentana, bcsComunes.ventanaGetTitulo(idVentana)+ " - "+ $scope.SesionEmpresa );
	
	
	$scope.confBasicaPerfiles = {
			dataSource: new bcs.DataSource({
				type : "odata", //Los tipos posibles son: odata, call, array y object
				definition : {
					filter : [ // Configuración de filtros sobre los datos. Es equivalente a una operación "AND".
								{ field: "IdEmpresa", operator: "eq", value: $scope.data.IdEmpresa}
							],
							serverFiltering : true, // Filtrado de datos en el servidor.
							serverPaging : false, // Paginado de los datos en el servidor.
							serverSorting : true, // Ordenamiento de los datos en el servidor.
							sort : { field: "IdPerfil", dir: "ASC" }, // Criterios de Ordenamiento.
							transport : {
								read : {
									url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1525",
									dataType : "json"
								}
							},
							schema : {
								model: {
									fields : {
										IdEmpresa: {type:"number"},
										IdPerfil : { type : "number" },
										Nombre : { type : "string" },
										IdEstado: {type: "string"},
										EstadoPerfil: {type: "string"}
									}
								}
							}
						}
			}),
			columns: [
			          	{field: "IdEmpresa", title:"UI_ID_EMPRESA", width:"50px", hidden:true},
			          	{field : "IdPerfil", title : "UI_ID_PERFIL", width : "50px", hidden:true},
						{field : "Nombre",title : "UI_PERFIL",width : "150px"},
						{field : "IdEstado",title : "UI_ESTADO",width : "50px", hidden:true},
						{field : "EstadoPerfil",title : "UI_ESTADO",width : "50px"}
						
					],
			gridCreated: function(grid) {
				$scope.perfilesGrid = grid;
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
	
	$scope.confBasicaAccesos = function(dataItem) {
		var dsDef = {
				type : "odata", //Los tipos posibles son: odata, call, array y object
				definition : {
							serverFiltering : true, // Filtrado de datos en el servidor.
							serverPaging : true, // Paginado de los datos en el servidor.
							serverSorting : true, // Ordenamiento de los datos en el servidor.
							sort : [{ field: "Padre", dir: "ASC"},{field: "Orden", dir:"DESC" }], // Criterios de Ordenamiento.
							pageSize : 5, // Número de registros por página.
							transport : {
								read : {
									url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1526",
									dataType : "json"
								}
							},
							schema : {
								model: {
									fields : {
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
							},
							filter : 
								  [ 
								          	// Configuración de filtros sobre los datos en el detalle.
									{ field: "IdPerfil", operator: "eq", value: dataItem.IdPerfil},
									{ field: "IdEmpresaPerfil", operator: "eq", value: $scope.data.IdEmpresa},
									{ field: "IdEmpresaAcceso", operator: "eq", value: $scope.data.IdEmpresa}
									]
						}
			};
		return {
			dataSource: new bcs.DataSource(dsDef),
			columns: [
						{field: "IdAcceso",title: $translate.instant("UI_ID_ACCESO"), width: "25px", hidden:true},
						{field: "NombreAcceso",title: $translate.instant("UI_ACCESO"), width: "100px"},
						{field: "TipoAcceso",title: $translate.instant("UI_TIPO_ACCESO"), width: "50px"},
						{field: "Padre",title: $translate.instant("UI_PADRE"),width: "75px", template: kendo.template("# ##=util.getNombreAccesoPadre(Padre)## #")},
						{field: "Orden",title: $translate.instant("UI_ORDEN"),width: "25px", hidden:true },
						{field: "IdServicio",title: $translate.instant("UI_ID_SERVICIO"),width: "25px", hidden:true},
						{field: "EsVisibleMenu",title: $translate.instant("UI_ES_VISIBLE"),width: "60px"}
					],
			sortable: true,
			pageable: true,
			height: 200,
			selectable : "row",
			excel : {
				allPages: true,
			},
			autoBind : true
		};
	};
	
	//COMBOBOX Estados
	$scope.data.oConfiguracionEstadoPerfiles={
		tipo : "combobox-estado-perfil",
		configuracion : {},
		filtros : {
			read : false
		},
		idActual : false,
		seleccionado : false,
		idDefecto : 'S'
	};
	
	$scope.buscarPerfiles = function(){
		var filters = new Array();
		
		filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.data.IdEmpresa });
		
    	if ($scope.data.NombrePerfil){
    		filters.push({ field: "tolower(Nombre)", operator: "contains", value: $scope.data.NombrePerfil.toLowerCase() });
    	}
    	
    	if ($scope.data.oConfiguracionEstadoPerfiles.idActual!=0){
    		filters.push({ field: "tolower(IdEstado)", operator: "contains", value: $scope.data.oConfiguracionEstadoPerfiles.idActual.toLowerCase() });
    	}
    
		$scope.perfilesGrid.dataSource.filters(filters);	
       
	};
	
	
	$scope.exportarPerfilesCompletos = function(){
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
		$scope.data.NombrePerfil = '';
		$scope.perfilesGrid.dataSource.removeAll();		
	};
	
	$scope.crearPerfil = function (){
		
		var id1 = bcsComunes.ventanaAbrir("CrearPerfil",{});

        var promesa=bcsComunes.ventanaPromesa(id1);
        bcsComunes.ventanaOverlay(idVentana,true);
        promesa.then(function(data){ 
        	$scope.perfilesGrid.dataSource.read();
        	bcsComunes.ventanaOverlay(idVentana,false);
    	}); 
	};
	
	$scope.modificarPerfil = function (){
		if($scope.seleccionPerfiles){
			var id1 = bcsComunes.ventanaAbrir("ModificarPerfil",$scope.seleccionPerfiles);
			
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        bcsComunes.ventanaOverlay(idVentana,true);
	        promesa.then(function(data){ 
	        	$scope.perfilesGrid.dataSource.read();
	        	bcsComunes.ventanaOverlay(idVentana,false);
	    	}); 
		}else{
			bcsComunes.msg("error",'Error',$translate.instant("MOD_NO_PERFIL_SELECT"));
		}
	};	

});