angular.module('ModuloOrdenes').controller('ConsultarArchivosContratosCtrl',
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
	$scope.filtroTipoCliente = {
		filtros : {
			IdEmpresa	: {field : "IdEmpresa", operator : "eq", value : $scope.$root.SesionIdEmpresa},
			IdEstado : {field : "IdEstado", operator : "eq", value : "ACT"}
		}
	};
	
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
																		$select: "\"IdEmpresa\",\"CodigoArchivo\",\"Descripcion\",\"IdUsuario\",\"CodigoUsuario\",\"FechaModificacion\",\"TipoBinario\",\"TipoPresentacion\",\"EsEstatico\",\"IdTemplate\",\"Datasource\",\"IdTipoPersona\", \"TipoPersona\", \"TipoCliente\", \"TipoClienteCMF\", \"IdTipoClienteCMF\", \"IdEstado\",\"Estado\""
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
								{field: "TipoClienteCMF",title: "UI_TIPO_CLIENTE_CMF", width: "85px", template: "#: (TipoClienteCMF!=undefined)? TipoClienteCMF : ''#"},
								{field: "idTipoClienteCMF",title: "UI_TIPO_CLIENTE_CMF", width: "85px", hidden:true},
								{field: "TipoPersona",title: "UI_TIPO_PERSONA", width: "85px"},
								{field: "Datasource",title: "UI_DATASOURCE", width: "150px", hidden:true},
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
			selectable 			: "row"
		};
	
	$scope.buscar = function(){
		//TODO agregar filtros
		var filters1 = {};
		var filters = new Array();
		filters1.logic = 'and';
		
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
    	if($scope.data.tipoPersona){
			var objfiltersTP = {};
			objfiltersTP.logic = 'or';
			objfiltersTP.filters = new Array();
	    	
			objfiltersTP.filters.push({ field: "IdTipoPersona", operator: "eq", value: $scope.data.tipoPersona });
			objfiltersTP.filters.push({ field: "IdTipoPersona", operator: "isnull" });
			
			filters.push(objfiltersTP);
    	}
		
    	if($scope.recordData.tipoClienteCMF){
    		var objfiltersCMF = {};
			objfiltersCMF.logic = 'or';
			objfiltersCMF.filters = new Array();
	    	
			objfiltersCMF.filters.push({ field: "IdTipoClienteCMF", operator: "eq", value: $scope.recordData.tipoClienteCMF });
			objfiltersCMF.filters.push({ field: "IdTipoClienteCMF", operator: "isnull" });
			
			filters.push(objfiltersCMF);    	
			
    	}
		filters1.filters = filters;
		
		$scope.data.mainGrid.dataSource.filters(filters1);
	};
	
	$scope.limpiarFiltros = function(){
		$scope.data.codigoArchivo = "";
		$scope.data.nombreUsuario = "";
		$scope.data.estado = "0";
		$scope.data.tipoPersona = undefined;
		$scope.recordData.tipoClienteCMF = undefined;
	};
	
	var ConsultaParametros = new bcs.DataSource({
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
										url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1435",
										dataType : "json",
									}
								},
			schema 				: {
				model: {
					fields : {
						IdParametroContrato: {type: "number"},
						CodigoArchivo: {type: "string"},
						Nombre: {type: "string"},
						Tipo: {type: "string"},
						Descripcion: {type:"string"}
					}
				}
			}
		},
	});
	
	$scope.imprimirArchivo = function(){
		var contrato = $scope.seleccionArchivoContrato;
		
		if(contrato!=undefined){
			console.log(contrato);
			bcsComunes.ventanaOverlay(idVentana,true);

			//obtener parametros del contrato.
			var parametros = [];
			ConsultaParametros.query({filter: {field: "CodigoArchivo", operator: "eq", value: contrato.CodigoArchivo}}).then(function(){
				//construir arreglo de parametros solicitados.
				ConsultaParametros.data().forEach(function(currentValue,index)
				{
					TipoParametros = {
						Nombre 	: ConsultaParametros.data()[index].Nombre ,
						Tipo 	: ConsultaParametros.data()[index].Tipo,
						Descripcion	: ConsultaParametros.data()[index].Descripcion,
						IdentificaPersona	: ConsultaParametros.data()[index].IdentificaPersona
					};
					parametros.push(TipoParametros);
				});

				//imprimir contrato seleccionado
				if(contrato.TipoBinario === 'pdf' && contrato.EsEstatico === "S"){
					//no tiene parametros de entrada y es PDF.
					var config = {
						responseType: "arraybuffer",
						params:{
							"OP" : "CONTRACT",
							"filename": contrato.CodigoArchivo,
							"estatico": "S"	
						}
					}

					$http.get('/BSCL/Plataforma/GWJAVA/PLA/DatosContratosBin', config)
					  .success(function (response) {
						   var file = new Blob([response], {type: contrato.TipoPresentacion});
						   //var fileURL = URL.createObjectURL(file);
						   saveAs(file, contrato.CodigoArchivo+"."+contrato.TipoBinario);
						   bcsComunes.ventanaOverlay(idVentana,false);
					}).error(function(){
						bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_AL_DESCARGAR_ARCHIVO"));
						bcsComunes.ventanaOverlay(idVentana,false);
					});

				}else {
					var datos = {};
					if(parametros.length>0){
						var id1 = bcsComunes.ventanaAbrir("ModalArchivosContratos", {parametros : parametros, IdTipoPersona : contrato.TipoPersona});
						var promesa=bcsComunes.ventanaPromesa(id1);  
						promesa.then(function(data){
							console.log(data);
							console.log(contrato);
							if (data.length == 0){
								bcsComunes.ventanaOverlay(idVentana,false);
								return;
							}
							if (contrato.TipoBinario === 'pdf') {
								datos.p_IdEmpresa = $scope.data.IdEmpresa;
								datos.p_CodigoContrato = contrato.CodigoArchivo;
								datos.p_IdUsuario = $scope.data.IdUsuario;
								datos.p_IdModulo = $scope.data.IdModulo;
								datos.p_DocumentoIdentidad = data.p_DocumentoIdentidad?data.p_DocumentoIdentidad:data.p_Rut;
								bcsServicioPlataforma.Servicios().ImprimirContratos(datos).$promise.then(function(result){
									
									if (result.P_OUT_ERRORESCONTADOR > 0) {
										bcsComunes.ventanaMsg({
											mensaje:$translate.instant("UI_ERROR"),
											conflictos:util.parseServerResponse(result)
										},idVentana);
									}
									else {
										var leBlob = util.b64toBlob(result.p_out_ContratoB64,'application/pdf',512);
			      						saveAs(leBlob, result.p_out_NombreContrato);
										
									}
									bcsComunes.ventanaOverlay(idVentana,false);		
									
								},
								function(result){
									bcsComunes.ventanaOverlay(idVentana,false);	
								});
								
							}
							else {
								//cuando el usuario ingresa los datos.
								datos = data;
								var datasource = $scope.buildDatasource(contrato, datos);
								$scope.getReport(contrato, datasource);
								bcsComunes.ventanaOverlay(idVentana,false);					
							}

						});
					}else{
						//obtener el reporte vacio
						$scope.getReport(contrato, []);
						bcsComunes.ventanaOverlay(idVentana,false);
					}
				}

			});
		}
	};

	$scope.buildDatasource = function(contrato, datos){
		var data = {};
		data.serviceId = parseInt(contrato.Datasource);
		data.serviceResponse = "resultSet";
		
		data.serviceData = {};
		data.serviceData["p_IdEmpresa"] = $scope.data.IdEmpresa;
		data.serviceData["p_IdUsuario"] = $scope.data.IdUsuario;
		data.serviceData["p_IdModulo"] = $scope.data.IdModulo;

		$.each(datos, function(key, value){
			data.serviceData[key] = value;
		});
		return data;
	};
	
	$scope.getReport = function(contrato, datasource){
		var parameters = {
				"parameters": [],
				"configuration": {
					"mainReport": {
					"data": datasource,
					"parameters": [],
					"reportName": contrato.CodigoArchivo
					},
					"subReports": []
				},
				"exportFormat": "pdf",
				"reportId": contrato.IdTemplate
			};
							
			$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, {"responseType": "arraybuffer"})
			  .success(function (response) {
				   var file = new Blob([response], {type: 'application/pdf'});
				   //var fileURL = URL.createObjectURL(file);
				   saveAs(file, contrato.CodigoArchivo+".pdf");
				   bcsComunes.ventanaOverlay(idVentana,false);
			}).error(function (response) {
				bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_AL_DESCARGAR_ARCHIVO"));
				bcsComunes.ventanaOverlay(idVentana,false);
			});
	};

});	
