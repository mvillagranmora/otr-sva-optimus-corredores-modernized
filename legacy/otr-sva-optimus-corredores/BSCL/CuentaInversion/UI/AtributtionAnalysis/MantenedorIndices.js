angular.module('ModuloCuentaInversion').controller('MantenedorIndicesCorrCtrl',
		function($scope,bcsComunes,$translate,bcsServicioPlataforma,bcsServiciosCuentaInversion,bcsAlerta,OPCIONES_BCSALERTA){
	
	$scope.filtros = {};
	$scope.formData = {};
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	var datosNuevos = {};
    const TRANSACCION_UPDATE = "M";
    const TRANSACCION_DELETE = "E";

	//$scope.filtros.FechaHasta = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
	//$scope.filtros.FechaDesde =  new Date($scope.filtros.FechaHasta.getTime() - 30 * 24 * 60 * 60 * 1000);
	
	
	$scope.formData.buscar = function(filtros) {
		var filters = new Array();
		datosNuevos = {};		
		$scope.formData.apiGrid.dataSource.filter(filters);
	};
	
	$scope.formData.ingresar = function() {
		
		var id = bcsComunes.ventanaAbrir("CrearParametroFinanciero",{});
		var p = bcsComunes.ventanaPromesa(id);
		p.then(function(data){
			if (data && data.guardar) {
				$scope.formData.buscar($scope.filtros);
			}
		});
		
	};
	
////CREA INDICE	
	$scope.formData.crear = function() {
		
		var id = bcsComunes.ventanaAbrir("CrearIndice",{});
		var p = bcsComunes.ventanaPromesa(id);
		p.then(function(data){
			if (data && data.guardar) {
				$scope.formData.buscar($scope.filtros);
			}
		});
		
	};
////ELIMINAR INDICE		
	$scope.formData.eliminar = function(IdIndices) {		
		/////////////////////////////////////////////////////////	
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		if(!IdIndices || IdIndices.length == 0){
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			return;
		};
		var t = bcsAlerta.crear('PREGUNTA',$translate.instant('UI_ELIMINARA_INDICE'));
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
				
				var datos = {
						p_IdEmpresa 		: $scope.SesionIdEmpresa,
			    		p_IdUsuario 		: $scope.SesionIdUsuario,
			    		p_IdModulo 			: 'PLA',
			    		p_simularOperacion 	: 'N',
						p_UsaReglasNegocio 	: 'S',
						p_Tarea				: TRANSACCION_DELETE, 
						p_Registros : []
					};
		    	$.each(IdIndices,function(key,IdIndices){
		  			datos.p_Registros.push({p_IdIndiceFinanciero : IdIndices.IdIndiceFinanciero});
		  		});
		    	bcsComunes.ventanaOverlay($scope.idVentana,true);
				var p = bcsServiciosCuentaInversion.ProcesaIndicesAtributtionAnalisys(datos).$promise;
				p.then(function(result){
					bcsComunes.ventanaOverlay($scope.idVentana,false);
					var idP = bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_REGISTROS_ELIMINADOS"),
							conflictos:util.parseServerResponse(result)
						},$scope.idVentana);
					var p = bcsComunes.ventanaPromesa(idP);
					p.then(function(result){
						$scope.formData.buscar($scope.filtros);
					})
				},
				function(result){
					bcsComunes.ventanaOverlay($scope.idVentana,false);
					bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_ERROR_ELIMINAR_REGISTROS"),
							conflictos:util.parseServerResponse(result)
						},$scope.idVentana);
				}
				);
			}
			else{
				bcsComunes.ventanaOverlay($scope.idVentana,false);
			}
		});
	}

////MODIFICA INDICE
	$scope.formData.guardar = function() {
		
		var datos = {
			p_IdEmpresa 		: $scope.SesionIdEmpresa,
    		p_IdUsuario 		: $scope.SesionIdUsuario,
    		p_IdModulo 			: 'PLA',
    		p_simularOperacion 	: 'N',
			p_UsaReglasNegocio 	: 'S',
			p_Tarea				: TRANSACCION_UPDATE, 
			p_Registros : []
		};
		
		$.each(datosNuevos,function(key,value){
			datos.p_Registros.push({				
				p_IdIndiceFinanciero  : key,
				p_Periodo  			  : value["Periodo"],
				p_Nombre  			  : value["Nombre"],
				p_Descripcion  	 	  : value["Descripcion"],
				p_IdMonedaParametro	  : value["IdMonedaParametro"]|| null,
				p_IdMonedaValor  	  : value["IdMonedaValor"]|| null 								
			})
	   }); 
		//console.log("Servicio",datos);
		
		if (datos.p_Registros.length === 0) {
			bcsComunes.msg("","UI_INFORMACION","UI_NO_HAY_REGISTROS_MODIFICADOS");
			return;
		}
        
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		var p = bcsServiciosCuentaInversion.ProcesaIndicesAtributtionAnalisys(datos).$promise;
		p.then(function(result){
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			var idP = bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_REGISTROS_MODIFICADOS"),
					conflictos:util.parseServerResponse(result)
				},$scope.idVentana);
			var p = bcsComunes.ventanaPromesa(idP);
			p.then(function(result){
				$scope.formData.buscar($scope.filtros);
			})
		},
		function(result){
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_GUARDAR_REGISTROS"),
					conflictos:util.parseServerResponse(result)
				},$scope.idVentana);
		}
		);
	};
	
	var gridIndices = null;

	$scope.formData.defaultOptions = {
			dataSource : new bcs.DataSource({
	      		type : 'odata',
	      		definition : {
					serverFiltering: true,
					serverPaging: true,
					serverSorting: true,
					sort: [
					       { field: "IdIndiceFinanciero", dir: "asc" }
					],
					pageSize:250,
					schema: {
						model : {
							fields : {
								IdIndiceFinanciero  : { type: "string"},
								Periodo  			: { type: "string"},
								Nombre  			: { type: "string"},
								Descripcion  		: { type: "string"},
								IdMonedaParametro	: { type: "string"},
								IdMonedaValor  		: { type: "string"}
							}
						}
					},
					transport: {
						read: {
							url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=700011",
							dataType: "json"
						}
					}
				}
	      	}),
	      	
	      	columns : [
		           {field: "IdIndiceFinanciero",	title : "UI_INDICE",			width: "75px",groupable: true},
		           {field: "Periodo",				title : "UI_PERIODO",			width: "75px",groupable: true},
		           {field: "Nombre",				title : "UI_NOMBRE",			width: "125px",editable: true},
		           {field: "Descripcion",			title : "UI_DESCRIPCION",		width: "75px",editable: true},
		           {field: "IdMonedaParametro",		title : "UI_MONEDA_VALOR",		width: "75px",groupable: true}
		          // {field: "IdMonedaValor",			title : "UI_MONEDA_PARAMETRO",  width: "100px",groupable: true}
			           
		           ],
		    
           save :function(e) {
				
        	   var obj = datosNuevos[e.model.IdIndiceFinanciero] || {};
        	   //Cargo los valores nuevos
        	   $.each(e.values,function(key,value){
        		   	if(value){
        		   		obj[key] = value;
        		   	}
			   });   
        	  
        	   datosNuevos[e.model.IdIndiceFinanciero] = obj;  
			},
	      	
			allowCopy: true,
			autoBind: false,
			userSettings : true,
			navigatable : true,
			excel : {
				allPages: true,
			},
	      	filterable: true,
	      	height:350,
	      	pageable : true,
	      	reorderable: true,
			resizable: true,
			scrollable: true,
	      	selectable: "multiple, row",
			sortable: true,
			gridCreated	: function(grid) {
				gridIndices = grid;
			},
			
			
			

	};
	let firstHeight = $('#Win_MantenedorIndicesCorr');
	firstHeight.css('width', '946px');
});
