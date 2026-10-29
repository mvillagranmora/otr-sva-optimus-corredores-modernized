angular.module('ModuloPlataforma').controller('MantenedorArchivosContratosCtrl',
		function($scope,$translate,$modal,$log,$sce,$http,
				bcsComunes, bcsServicioPlataforma)
{

	$scope.data	= {};	
	var overlay = bcsComunes.overlay;
	
	//Seteo de variables de sistema
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdModulo	= 'PLA';
	$scope.data.btnActivos  = true;   //Activa botones
	$scope.data.tipoClienteCmfEnabled = false;
	
	//Funcion para saber que tipo de persona es y asi determinar que tipo de cliente cmf mostrar
	$scope.$watch('data.tipoPersona', function(newval, oldval){
		
		var CodigoTipoIndicador = (newval == "N")?"TCLICMF":"TCLICMFJ";
		
		if(newval == "N" || newval == "J"){
			
			$scope.configData.filtroTipoClienteCMF = {
					filtros : {IdTipoIndicador : {field : "CodigoTipoIndicador", operator : "eq", value : CodigoTipoIndicador}}
				};
			$scope.tipoClienteCmfEnabled = true;
			
		}else{
			$scope.tipoClienteCmfEnabled = false;
			
			if($scope.recordData != undefined){
				$scope.recordData.tipoClienteCMF = "TODOS";
			}
		}
		
	});
	
	var idVentana = bcsComunes.ventanaWinId($scope);
		
	//GRILLA PRINCIPAL DE ARCHIVOS
	$scope.confGridArchivosContratos = {
			dataSource			: new bcs.DataSource({
									type 		: "odata", //Los tipos posibles son: odata, call, array y object
									definition 	: {
										serverFiltering 	: true, // Filtrado de datos en el servidor.
										serverPaging 		: true, // Paginado de los datos en el servidor.
										serverSorting 		: true, // Ordenamiento de los datos en el servidor.
										groupable 			: true, 
										//sort 				: { field: "FechaModificacion", dir: "asc" }, // Criterios de Ordenamiento.
										pageSize 			: 25, // Número de registros por página.
										transport 			: {
																read : {
																	url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1421",
																	dataType : "json",
																	data:{
																		$select: "\"IdEmpresa\",\"CodigoArchivo\",\"Descripcion\",\"IdUsuario\",\"CodigoUsuario\",\"FechaModificacion\",\"TipoBinario\",\"TipoPresentacion\",\"EsEstatico\",\"IdTemplate\",\"Datasource\", \"TipoCliente\", \"TipoClienteCMF\", \"IdTipoClienteCMF\", \"IdTipoPersona\", \"TipoPersona\", \"IdEstado\",\"Estado\""
																	}
																}
															},
										schema 				: {
											model: {
												fields : {
													IdEmpresa: {type: "number"},
													CodigoArchivo: {type: "string"},
													Descripcion: {type: "string"},
													IdUsuario: {type: "number"},
													CodigoUsuario: {type: "string"},
													FechaModificacion: {type: "date"},
													TipoBinario: {type: "string"},
													TipoPresentacion: {type: "string"},
													EsEstatico: {type: "string"},
													IdTemplate: {type: "string"},
													Datasource: {type: "string"},
													TipoCliente: {type: "string"},
													TipoClienteCMF: {type: "string"},
													idTipoClienteCMF: {type: "number"},
													IdTipoPersona: {type: "string"},
													TipoPersona: {type: "string"},
													IdEstado: {type: "string"},
													Estado: {type: "string"}
												}
											}
										}
									}
								}),
			columns				: [
			   					{field: "IdEmpresa",title: "UI_ID_EMPRESA", format: "{0:n0}",width: "150px", hidden:true},
								{field: "CodigoArchivo",title: "UI_CODIGO_ARCHIVO",width: "150px"},
								{field: "Descripcion",title: "UI_DESCRIPCION",width: "150px"},
								{field: "IdUsuario",title: "UI_ID_USUARIO_MODIFICADOR",width: "150px", hidden:true},
								{field: "CodigoUsuario",title: "UI_NOMBRE_USUARIO",width: "150px"},
								{field: "FechaModificacion",title: "UI_FECHA_MODIFICACION", width: "150px"},
								{field: "TipoPresentacion",title: "UI_TIPO_PRESENTACION", width: "150px", hidden:true},
								{field: "EsEstatico",title: "UI_ESTATICO", width: "150px", hidden:true},
								{field: "IdTemplate",title: "UI_TEMPLATE", width: "150px", hidden:true},
								{field: "Datasource",title: "UI_DATASOURCE", width: "150px", hidden:true},
								{field: "TipoClienteCMF",title: "UI_TIPO_CLIENTE_CMF", width: "85px", template: "#: (TipoClienteCMF!=undefined)? TipoClienteCMF : ''#"},
								{field: "idTipoClienteCMF",title: "UI_TIPO_CLIENTE_CMF", width: "85px", hidden:true},
								{field: "TipoPersona",title: "UI_TIPO_PERSONA", width: "85px"},
								{field: "IdEstado",title: "UI_ESTADO", width: "150px", hidden:true},
								{field: "Estado",title: "UI_ESTADO", width: "95px"}
							],
			gridCreated: function(grid) {
				$scope.data.mainGrid = grid;
			},
			sortable			: true,
			pageable			: false,
			filterable			: true,
			groupable           : true,
			autoBind			: true,
			height				: 300,
			userSettings		: true,
			navigatable			: true,
			excel : {
				allPages: true,
			},
			selectable 			: "row"
		};
	
	$scope.buscar = function(){
		//TODO agregar filtros
		var filters = new Array();
		
		filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.data.IdEmpresa });
		
		if ($scope.data.codigoArchivo){
    		filters.push({ field: "tolower(CodigoArchivo)", operator: "contains", value: $scope.data.codigoArchivo.toLowerCase() });
    	}
    	if ($scope.data.nombreUsuario){
    		filters.push({ field: "tolower(CodigoUsuario)", operator: "contains", value: $scope.data.nombreUsuario.toLowerCase() });
    	}
    	
    	if ($scope.data.estado!=false){
    		filters.push({ field: "tolower(IdEstado)", operator: "eq", value: $scope.data.estado.toLowerCase() });
    	}
    	if($scope.data.tipoPersona)
    		filters.push({ field: "IdTipoPersona", operator: "eq", value: $scope.data.tipoPersona });
    	
    	if($scope.recordData.tipoClienteCMF)
    		filters.push({ field: "IdTipoClienteCMF", operator: "eq", value: $scope.recordData.tipoClienteCMF });
    			
		$scope.data.mainGrid.dataSource.filter(filters);
	};
	
	$scope.limpiarFiltros = function(){
		$scope.data.codigoArchivo = "";
		$scope.data.nombreUsuario = "";
		$scope.data.estado = "0";
		$scope.data.tipoPersona = undefined;
		$scope.recordData.tipoClienteCMF = undefined;
	};
	
	$scope.subirArchivo = function(){
		bcsComunes.ventanaOverlay(idVentana,true);
		var id1 = bcsComunes.ventanaAbrir("SubirArchivosContratos", false,false);
		var promesa=bcsComunes.ventanaPromesa(id1);                               
		promesa.then(function(data){ 
			$scope.data.mainGrid.dataSource.read();
			bcsComunes.ventanaOverlay(idVentana,false);
		});
		
	}
	
	$scope.imprimirArchivo= function(){
		bcsComunes.ventanaOverlay(idVentana,true);
		var config = {
			responseType: "arraybuffer",
			params:{
				"OP" : "CONTRACT",
				"filename": $scope.seleccionArchivoContrato.CodigoArchivo,
				"estatico": "S"	
			}
		}
		
		$http.get('/BSCL/Plataforma/GWJAVA/PLA/DatosContratosBin', config)
		  .success(function (response) {
			   var file = new Blob([response], {type: $scope.seleccionArchivoContrato.TipoPresentacion});
			   //var fileURL = URL.createObjectURL(file);
			   saveAs(file, $scope.seleccionArchivoContrato.CodigoArchivo+"."+$scope.seleccionArchivoContrato.TipoBinario);
			   $scope.data.mainGrid.dataSource.read();
			   bcsComunes.ventanaOverlay(idVentana,false);
		}).error(function(){
			bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_AL_DESCARGAR_ARCHIVO"));
			bcsComunes.ventanaOverlay(idVentana,false);
		});
	};
	
	$scope.modificarArchivo = function(){
		bcsComunes.ventanaOverlay(idVentana,true);
		var id1 = bcsComunes.ventanaAbrir("ModificarArchivosContratos",$scope.seleccionArchivoContrato, false,false);
		var promesa=bcsComunes.ventanaPromesa(id1);                               
		promesa.then(function(data){ 
			$scope.data.mainGrid.dataSource.read();
			bcsComunes.ventanaOverlay(idVentana,false);
		});
	};

});	
