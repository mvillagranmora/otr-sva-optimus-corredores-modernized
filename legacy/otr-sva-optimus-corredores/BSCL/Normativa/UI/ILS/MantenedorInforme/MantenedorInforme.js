angular.module('ModuloNormativa').controller('MantenedorInformeCtrl',
	function($scope,$uibModal,$translate,bcsComunes, $resource, $timeout, bcsAlerta,OPCIONES_BCSALERTA, bcsServiciosNormativa){
	
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);

	$scope.data = {};
	$scope.Informe = "";	
	$scope.inputDisabled = true;	
	$scope.data.NombreIndice = '';
	$scope.data.NombreInforme = '';
	$scope.data.OrdenIndice = undefined;
	$scope.data.HabilitaGrabar = false;
	$scope.data.IdTipoInforme = undefined;
	$scope.habilitaEliInd = false; 
	
	$scope.habilitaGrabar = false; 
	$scope.data.kngOrdenIndice = true; 
	
	var idIndiceSeleccionado = 0;
	var nombreInformeSeleccionado = '';


    //---------------------------------------------
    //--------- AGREGAR Y MODIFICAR INFORME ---------
    //---------------------------------------------
	$scope.agregarInforme = function() {
		var ventanaId = bcsComunes.ventanaAbrir("AgregarInforme",{}, false, false, $scope.winID);
		var promesa = bcsComunes.ventanaPromesa(ventanaId);
		
		$.when(promesa).then(function() {			
			dataSourceInforme.read();
			dataSourceIndice.removeAll();
			$scope.limpiar();
		});
		$scope.habilitado = false;
		$scope.habilitadoLimpiar = false;
	};
	
	$scope.modificarInforme = function(){
		var ventanaId = bcsComunes.ventanaAbrir("AgregarInforme",
				{Informe: $scope.seleccionInforme}, false,false,$scope.winID);
		var promesa = bcsComunes.ventanaPromesa(ventanaId);
		
		$.when(promesa).then(function(){			
			dataSourceInforme.read();
		});
		$scope.habilitado = false;
	};

	$scope.agregarIndiceInforme = function() {		
		if($scope.indiceSeleccionado === false || $scope.data.OrdenIndice === undefined){
			bcsComunes.ventanaMsg({
			      mensaje:$translate.instant("UI_GRABAR_ERROR"),
			      conflictos:{
			       errores : [ {
			           Codigo : 'COD',
			           Descripcion : $translate.instant("UI_INGRESE_DATOS")
			          } ],
			      }
			},$scope.winID);
		}else{
			
			var dataParametros = {
					p_IdTipoInforme			: $scope.data.IdTipoInforme,
					p_IdIndice				: $scope.indiceSeleccionado.IdIndice, 
				    p_Orden					: $scope.data.OrdenIndice,
					p_TipoOperacion			: 'I',
					p_IdEmpresa 			: $scope.SesionIdEmpresa,
					p_IdUsuario 			: $scope.SesionIdUsuario
				};
			
				bcsComunes.ventanaOverlay($scope.winID,false);
				bcsServiciosNormativa.MantenedorInformeConfiguracion(dataParametros, successAgregarIndiceInforme, errorAgregarIndiceInforme);			
		}
	};
	$scope.eliminarIndice = function() {
			
			var dataParametros = {
					p_IdTipoInforme			: $scope.data.IdTipoInforme,
					p_IdIndice				: idIndiceSeleccionado, 
				    p_Orden					: 0,
					p_TipoOperacion			: 'E',
					p_IdEmpresa 			: $scope.SesionIdEmpresa,
					p_IdUsuario 			: $scope.SesionIdUsuario
				};
			
				bcsComunes.ventanaOverlay($scope.winID,false);
				bcsServiciosNormativa.MantenedorInformeConfiguracion(dataParametros, successEliminarIndiceInforme, errorEliminarIndiceInforme);			
		
	};

	// Definir modelo, Vista Informe.
	ModeloInforme = function() {
		return {
			model: {
				fields : {
					IdTipoInforme 	: { type: "number"},
	        		Nombre 	 		: { type: "string"},
	        		IdEmpresa 	 	: { type: "number"},
	        		NombreEmpresa 	: { type: "string"},
	        		Estado 			: { type: "string"},
	        	}
			}
		};
	}; 
	
	// Definir modelo Vista Indice.
	ModeloIndice = function() {
		return {
			model: {
    	        	fields:{
    	        		IdIndice 			: { type: "number"},
    	        		Nombre 				: { type: "string"},
    	        		Orden 				: { type: "number"}
    	        	} 
			}
		};
	};
	
	// Función definida para evento - cajas
	dataSourceInforme =  new bcs.newDataSource({
		type 		: "odata", 
		definition 	: {
						serverFiltering 	: true, // Filtrado de datos en el servidor.
						serverPaging 		: true, // Paginado de los datos en el servidor.
						serverSorting 		: false, // Ordenamiento de los datos en el servidor.
						sortable 			: true,
						pageSize 			: 50,
						transport 			:	{
							read : {
								url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=13023",
								dataType : "json"
							}
						},
						schema 				: ModeloInforme(),
						filter              : [{ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa }],	
					  }
	});			

	// Función definida para Indice
	dataSourceIndice = new bcs.newDataSource({
		type 		: "odata", 
		definition 	: {
						serverFiltering 	: true, // Filtrado de datos en el servidor.
						serverPaging 		: true, // Paginado de los datos en el servidor.
						serverSorting 		: true, // Ordenamiento de los datos en el servidor.
	                    sort 				: { field: "Orden", dir: "ASC" },
						transport 			: {
												read : {
													url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=13044",
													dataType : "json"
												}
											},
						pageSize 			: 50,
						schema 				: ModeloIndice()
					  }
 
	});

	// Grilla principal, Cajas.
	columnasInforme = function() {
		return [ 
		        {field: "IdTipoInforme", 	title: "UI_ID_TIPO_INFORME", 	width: "70px" }
		       ,{field: "Nombre", 			title: "UI_NOMBRE", 			width: "100px"}
		       ,{field: "NombreEmpresa", 	title: "UI_EMPRESA", 			width: "100px"}
				];
	};
	
	// Grilla Indices
	columnasIndice = function() {
		return  [
				{field: "IdIndice", 	title: "UI_ID_INDICE", 		width: "50px"}
			   ,{field: "Nombre", 		title: "UI_NOMBRE", 		width: "200px"}
		       ,{field: "Orden", 		title: "UI_ORDEN", 		width: "60px"}  
	    ];
	};	

	onChange  = function() {
        var filters = new Array();
        	filtersAutori = new Array();
	        filters.push( { field: "", operator: "eq", value: $scope.seleccionInforme });
    };

	$scope.Informe = {
			dataSource		: dataSourceInforme,
			columns			: columnasInforme(),	
			filterable		: true,			
			sortable		: true,
			pageable		: true,
			autoBind		: true,  // comentar
			height			: 450,
		    selectable 		: "row",
		    change			: onChange(),
	};
	
	$scope.Indice = {
			dataSource			: dataSourceIndice,
			columns				: columnasIndice(),		
			sortable			: false,
			pageable			: true,
			autoBind			: false,  // false comentar, Mostrar data al
			height				: 358,
		    selectable 			: "row"
	};
	
	$scope.$watch('seleccionInforme', function(newValue, oldValue) {  
		if (angular.equals(newValue,oldValue)) return;
		if (newValue!==false) {		
			
			$scope.inputDisabled = false;
			$scope.data.NombreInforme = newValue.Nombre;
			$scope.habilitado = true;
			$scope.data.IdTipoInforme = newValue.IdTipoInforme;
			nombreInformeSeleccionado = newValue.Nombre;
		}
		if(newValue != undefined && dataSourceIndice !== undefined){
			var filters = new Array();
			filters.push( { field: "IdTipoInforme", operator: "eq", value: newValue.IdTipoInforme });
			dataSourceIndice.filter(filters);
			$scope.data.kngOrdenIndice = true;
		}//BuscarIndiceInforme(newValue.IdTipoInforme);
		$scope.limpiar();
	},true);
	

	 $scope.$watch('indiceSeleccionado', function(newValue, oldValue) {  
		if (angular.equals(newValue,oldValue)) return;
			if (newValue!==false) {		
				$scope.habilitadoLimpiar = true; 
				//$scope.data.IdIndice = 0;
				$scope.data.kngOrdenIndice = false; 
				//$scope.data.OrdenIndice = undefined;
				
			}
		},true);
	 
	 $scope.$watch('data.OrdenIndice', function(newValue, oldValue) {  
			if (angular.equals(newValue,oldValue)) return;
				if (newValue!==false) {		
					$scope.habilitaGrabar = true; 
					
				}
			},true);
	 
	$scope.$watch('seleccionIndice', function(newValue, oldValue) {  
		if (angular.equals(newValue,oldValue)) return;
		if (newValue!==false) {		
			$scope.habilitaEliInd = true;
			idIndiceSeleccionado = newValue.IdIndice;
		}		
	},true);

	$scope.eliminarInforme = function (){
		
		var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_ELIMINARA_INFORME_SELECCIONADO') );
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR){
					var parametrosSP = {
							p_IdTipoInforme : $scope.seleccionInforme.IdTipoInforme,
							p_IdEmpresa 	: $scope.seleccionInforme.IdEmpresa,
							p_Nombre 		: $scope.seleccionInforme.Nombre,
							p_Estado		: 'VIG',
							p_TipoOperacion : 'E',
							p_IdUsuario 	: $scope.SesionIdUsuario					
					};
					bcsServiciosNormativa.MantieneInforme(parametrosSP, successEliminarInforme, errorEliminarInforme);
			};
		});
	};
	var successEliminarInforme = function (respuesta){
		bcsComunes.ventanaOverlay($scope.winID,false);
		//SUCCESS
		if(respuesta.P_OUT_ERRORESCONTADOR == 0){
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
						
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_INFORME_ELIMINAR_EXITO"),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
			
			dataSourceInforme.read();
			
			$scope.inputDisabled = true;
			$scope.limpiar();
			$scope.data.NombreInforme = '';
			
			BuscarIndiceInforme($scope.seleccionInforme.IdTipoInforme);
		}
		//ERROR
		else {
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
			
			bcsComunes.ventanaMsg({
			      mensaje:$translate.instant("UI_INFORME_ELIMINAR_ERROR"),
			      conflictos:{
			       errores : [ {
			           Codigo : 'COD',
			           Descripcion : respuesta.P_OUT_ERRORESDETALLE[0].Descripcion
			          } ],
			      }
			},$scope.winID);
			
			var i = 0;
            for(i; i<respuesta.P_OUT_ERRORESCONTADOR; i++)
            {
            	bcsComunes.log().info("Error: " + respuesta.P_OUT_ERRORESDETALLE[i].Descripcion);       
            }       
		}
	};
	
	var errorEliminarInforme = function(respuesta)
	{	
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
		bcsComunes.ventanaMsg({
		      mensaje:$translate.instant("UI_INFORME_ELIMINAR_ERROR"),
		      conflictos:{
		       errores : [ {
		           Codigo : 'COD',
		           Descripcion : respuesta.P_OUT_ERRORESDETALLE[0].Descripcion
		          } ],
		      }
		},$scope.winID);
	};
	
	var successAgregarIndiceInforme = function (respuesta){
		//SUCCESS
		if(respuesta.P_OUT_ERRORESCONTADOR == 0){
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_INDICE_INGRESAR_EXITO"),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
			
			$scope.limpiar();
			$scope.data.NombreInforme = nombreInformeSeleccionado;
			BuscarIndiceInforme($scope.seleccionInforme.IdTipoInforme);
		}
		//ERROR
		else {
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
			
			bcsComunes.ventanaMsg({
			      mensaje:$translate.instant("UI_INDICE_INGRESAR_ERROR"),
			      conflictos:{
			       errores : [ {
			           Codigo : 'COD',
			           Descripcion : respuesta.P_OUT_ERRORESDETALLE[0].Descripcion
			          } ],
			      }
			},$scope.winID);
			
			var i = 0;
            for(i; i<respuesta.P_OUT_ERRORESCONTADOR; i++)
            {
            	bcsComunes.log().info("Error: " + respuesta.P_OUT_ERRORESDETALLE[i].Descripcion);       
            }       
		}
	};
	var errorAgregarIndiceInforme = function(respuesta)
	{	
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
		bcsComunes.ventanaMsg({
		      mensaje:$translate.instant('UI_INDICE_INGRESAR_ERROR'),
		      conflictos:{
		       errores : [ {
		           Codigo : 'COD',
		           Descripcion : ''
		          } ],
		      }
		},$scope.winID);
	};
	
	
	
	var successEliminarIndiceInforme = function (respuesta){
		//SUCCESS
		if(respuesta.P_OUT_ERRORESCONTADOR == 0){
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_INDICE_ELIMINAR_EXITO"),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
			
			$scope.limpiar();
			$scope.data.NombreInforme = nombreInformeSeleccionado;
			BuscarIndiceInforme($scope.seleccionInforme.IdTipoInforme);
		}
		//ERROR
		else {
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
			
			bcsComunes.ventanaMsg({
			      mensaje:$translate.instant("UI_INDICE_ELIMINAR_ERROR"),
			      conflictos:{
			       errores : [ {
			           Codigo : 'COD',
			           Descripcion : respuesta.P_OUT_ERRORESDETALLE[0].Descripcion
			          } ],
			      }
			},$scope.winID);
			
			var i = 0;
            for(i; i<respuesta.P_OUT_ERRORESCONTADOR; i++)
            {
            	bcsComunes.log().info("Error: " + respuesta.P_OUT_ERRORESDETALLE[i].Descripcion);       
            }       
		}
	};
	
	var errorEliminarIndiceInforme = function(respuesta)
	{	
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
		bcsComunes.ventanaMsg({
		      mensaje:$translate.instant('UI_INDICE_ELIMINAR_ERROR'),
		      conflictos:{
		       errores : [ {
		           Codigo : 'COD',
		           Descripcion : ''
		          } ],
		      }
		},$scope.winID);
	};
	
	$scope.buscarIndice = function() {
		if($scope.data.IdIndice != undefined){
	 		var parametros = {};
			parametros.p_IdIndice		= $scope.data.IdIndice;
			parametros.p_IdUsuario 		= $scope.SesionIdUsuario;
	    	bcsServiciosNormativa.IndiceBuscar(parametros,successIndice,errorIndice);
    	}else{
    		bcsComunes.ventanaMsg({
			      mensaje:$translate.instant("UI_INDICE_ERROR"),
			      conflictos:{
			       errores : [ {
			           Codigo : 'COD',
			           Descripcion : 'Ingrese IdIndice'
			          } ],
			      }
			},$scope.winID);
    	}
    }; 

	var BuscarIndiceInforme = function(IdTipoInforme) {
 		var parametros = {};
		parametros.p_IdTipoInforme	= IdTipoInforme;
		parametros.p_IdUsuario 		= $scope.SesionIdUsuario;
    	bcsServiciosNormativa.IndiceInformeBuscar(parametros,successIndiceInforme,errorIndice);
    }; 
    
    var successIndice = function(respuesta)
    {
    	if(respuesta.P_OUT_ERRORESCONTADOR === 0 && respuesta.P_OUT_NOMBRE != "EMPTY")
	    	$scope.data.NombreIndice = respuesta.P_OUT_NOMBRE; 
    	else{
    		bcsComunes.ventanaMsg({
			      mensaje:$translate.instant("UI_INDICE_ERROR"),
			      conflictos:{
			       errores : [ {
			           Codigo : 'COD',
			           Descripcion : 'No se encontro Índice'
			          } ],
			      }
			},$scope.winID);
    	}
    	if(respuesta.P_OUT_ERRORESCONTADOR != 0)
    	{
    		$.each(respuesta.P_OUT_ERRORESDETALLE,function(key,value)
    		{
    			bcsComunes.ventanaMsg({
				      mensaje:$translate.instant("UI_ERROR"),
				      conflictos:{
				       errores : [ {
				           Codigo : 'COD',
				           Descripcion : 'Ingrese IdIndice'
				          } ],
				      }
  				},$scope.winID);
    		});
    	}

    };
    
    var successIndiceInforme = function(respuesta)
    {
    	if(respuesta.P_OUT_ERRORESCONTADOR === 0){
			if(respuesta.P_OUT_TABLAINDICE[0].Nombre != "EMPTY")
	    		dataSourceIndice.data(respuesta.P_OUT_TABLAINDICE); 
			else
				dataSourceIndice.removeAll();
      	}
    	if(respuesta.P_OUT_ERRORESCONTADOR != 0)
    	{
    		$.each(respuesta.P_OUT_ERRORESDETALLE,function(key,value)
    		{
    			bcsComunes.ventanaMsg({
				      mensaje:$translate.instant("UI_ERROR"),
				      conflictos:{
				       errores : [ {
				           Codigo : 'COD',
				           Descripcion : value.Descripcion
				          } ],
				      }
  				},$scope.winID);
    		});
    	}

    };
    var errorIndice = function(respuesta)
    {
    	bcsComunes.ventanaMsg({
		      mensaje:$translate.instant("UI_ERROR"),
		      conflictos:{
		       errores : [ {
		           Codigo : 'COD',
		           Descripcion : ''
		          } ],
		      }
		},$scope.winID);    	
    };
	
	// Función para limpiar los campos de la búsqueda 
	$scope.limpiar = function(){
		$scope.data.IdIndice = '';
		$scope.data.NombreIndice = '';
		$scope.data.IdIndice = 0;
		$scope.data.OrdenIndice = undefined;
		$scope.data.kngOrdenIndice = true; 
		$scope.habilitaGrabar = false; 
	};
	
});
