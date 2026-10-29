angular.module('ModuloNormativa').controller('MantenedorConceptoCtrl',
	function($scope,$uibModal,$translate,bcsComunes, $resource, $timeout, bcsAlerta,OPCIONES_BCSALERTA,bcsServiciosNormativa){
	
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);

	//Inicializacion de variables
	$scope.Concepto = "";	
	$scope.filtro = {NombreConcepto: '', NombreCorto: ''};	
	$scope.habilitado = false;
	$scope.habilitadoItem = false;
	$scope.habilitadoGrupoConcepto = false;
	$scope.habilitadoEliminar = false;
	$scope.habilitadoOperacion = false;
	$scope.habilitadoGrabar = false;
	
	$scope.inputDisabled = true;
	$scope.inputDisabledfactor = true;
	
    //---------------------------------------------
    //----------------- AGREGAR -------------------
    //---------------------------------------------
	$scope.agregarConcepto = function(tipoOperacion) {
		var IdConcepto = 0;
		var NombreConcepto = '';
		var NombreCorto = '';
		if(tipoOperacion === 'M'){
			IdConcepto = $scope.seleccionConcepto.IdConcepto;
			NombreConcepto = $scope.seleccionConcepto.NombreConcepto;
			NombreCorto = $scope.seleccionConcepto.NombreCortoConcepto;
		}
		var parametros = {
			 tipoOperacion 			: tipoOperacion
			,IdConcepto 			: IdConcepto
			,Nombre 				: NombreConcepto
			,NombreCortoConcepto 	: NombreCorto
		};
		
		// Envio de parametros a la nueva ventana 
		var ventanaId = bcsComunes.ventanaAbrir("AgregarConcepto",parametros, false, false, $scope.winID);
		var promesa = bcsComunes.ventanaPromesa(ventanaId);
		
		// Actualiza la grilla que lista los Conceptos
		$.when(promesa).then(function() {			
			
			$scope.habilitado = false;						
			$scope.limpiar();
			$scope.limpiarConcepto();
			dataSourceConcepto.read();						
			dataSourceConceptoConfiguracion.removeAll();
			
		});
		
		//Deshabilita los campos
		$scope.habilitado = false;
	};

	$( "#agregarConcepto" ).click(function() {
		  alert( "Handler for .click() called." );
		});
	
	$scope.MantenedorConceptoGrupoConfiguracion = function(tipoOperacion) {
		var tablaConceptoGrupoConfiguracion	= [];
		if ($scope.modificaGrupo != undefined   && $scope.modificaGrupo === 'S'  ){
			tipoOperacion = 'M';
		}
		if(tipoOperacion === 'I'){
			if($scope.seleccionConcepto.IdConcepto === undefined)
			{
				bcsComunes.msg("warning", "Error", "Debe seleccionar un Concepto.");
				return;
			} 
			
			
			tablaConceptoGrupoConfiguracion.push({
				 IdConceptoGrupo 		: 0
				,IdConcepto				: $scope.seleccionConcepto.IdConcepto
				,NombreConceptoGrupo 	: $scope.nombreGrupoConcepto
				,NombreCortoConceptoGrupo :$scope.nombreGrupoCortoConcepto
				,Factor					: $scope.factorGrupo
			});
		}else if(tipoOperacion === 'M'){
			tablaConceptoGrupoConfiguracion.push({
				IdConceptoGrupo 			: $scope.seleccionConceptoGrupoConfiguracion.IdConceptoGrupo
				,IdConcepto    				: $scope.seleccionConcepto.IdConcepto
				,NombreConceptoGrupo 		: $scope.nombreGrupoConcepto
				,NombreCortoConceptoGrupo 	: $scope.nombreGrupoCortoConcepto
				,Factor						: $scope.factorGrupo
			});
			$scope.modificaGrupo = "N";
			
		}else if(tipoOperacion === 'E'){
			tablaConceptoGrupoConfiguracion.push({
				IdConceptoGrupo 	: $scope.seleccionConceptoGrupoConfiguracion.IdConceptoGrupo
			  , IdConcepto    		: $scope.seleccionConcepto.IdConcepto
			  ,NombreConceptoGrupo 	: ''
			  ,NombreCortoConceptoGrupo : ''
			  ,Factor					: 0
			});
		}

		var dataParametros = {
			p_IdEmpresa 					: $scope.SesionIdEmpresa,
			p_IdUsuario 					: $scope.SesionIdUsuario,
			p_IdModulo  					: 'NOR',
			p_UsaReglasNegocio 				: 'N',
			p_TipoOperacion					: tipoOperacion,
			p_tablaConceptoConfiguracion	: tablaConceptoGrupoConfiguracion
		};
		
		bcsComunes.ventanaOverlay($scope.winID,false);
		bcsServiciosNormativa.MantenedorConceptoGrupoConfiguracion(dataParametros).$promise.then(function(respuesta){
   			
	   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
	   		var mensajeOk = '';
	   		var mensajeErr = '';
	   		if(tipoOperacion === 'I'){
	   			mensajeOk = 'UI_CONFIG_CONCEPTO_EXITO';
		   		mensajeErr = 'UI_CONFIG_CONCEPTO_ERROR';	
	   		}else if(tipoOperacion === 'M'){
	   			mensajeOk = 'UI_CONFIG_CONCEPTO_MODIFICAR_EXITO';
		   		mensajeErr = 'UI_CONFIG_CONCEPTO_MODIFICAR_ERROR';
	   		}else if(tipoOperacion === 'I'){
	   			mensajeOk = 'UI_CONFIG_CONCEPTO_ELIMINAR_EXITO';
		   		mensajeErr = 'UI_CONFIG_CONCEPTO_ELIMINAR_ERROR';
	   		}
			//se valido la orden
			if (respuesta.P_OUT_ERRORESCONTADOR === 0) 
			{				
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant(mensajeOk),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
			}
			else
		    {
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				bcsComunes.ventanaMsg({
					mensaje: $translate.instant(mensajeErr),
					conflictos: util.parseServerResponse(respuesta),
					errores : [ {
				                 Codigo : 'COD',
				                 Descripcion : respuesta.P_OUT_ERRORESDETALLE[0].Descripcion
				                } 
					          ]
				},$scope.winID);
				var i = 0;
	            for(i; i<respuesta.P_OUT_ERRORESCONTADOR; i++)
	            {
	            	bcsComunes.log().info("Error: " + respuesta.P_OUT_ERRORESDETALLE[i].Descripcion);       
	            }  
		    }
			
			$scope.limpiarConceptoGrupo();
			bcsComunes.ventanaOverlay($scope.winID,false);
			var filters = new Array();
			//filters.push( { field: "IdConceptoGrupo", operator: "eq", value: $scope.seleccionConcepto.IdConceptoGrupo });

			filters.push( { field: "IdConcepto", operator: "eq", value: $scope.seleccionConcepto.IdConcepto });
			//CGO
			
   		},function(respuesta){
   			//ERROR AL CREAR LA ORDEN
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant('UI_OPERACION_CON_ERRORES'),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
   		});	
	};
	
	$scope.MantenedorConceptoConfiguracion = function(tipoOperacion) {
		var tablaConceptoConfiguracion	= [];
		if(tipoOperacion === 'I'){
			if($scope.seleccionConcepto.IdConcepto === undefined)
			{
				bcsComunes.msg("warning", "Error", "Debe seleccionar un Concepto.");
				return;
			} 
			if($scope.itemSeleccionado.IdItem === undefined)
			{
				bcsComunes.msg("warning", "Error", "Debe seleccionar un Item.");
				return;
			}
			if($scope.operacionseleccionado.IdOperacionCtb === undefined)
			{
				bcsComunes.msg("warning", "Error", "Debe seleccionar un tipo de operación.");
				return;
			}/*
			if($scope.kngFactor === undefined || $scope.kngFactor < 0)
			{
				bcsComunes.msg("warning", "Error", "El Factor no debe ser meno a 0.");
				return;
			}*/
			tablaConceptoConfiguracion.push({
				IdConcepto		: $scope.seleccionConcepto.IdConcepto
			  ,	IdConceptoGrupo	: $scope.seleccionConceptoGrupoConfiguracion.IdConceptoGrupo 
			  , IdItem    	: $scope.itemSeleccionado.IdItem
			  , Operacion	: $scope.operacionseleccionado.CodigoOperacionCtb
			  , Factor		: $scope.seleccionConceptoGrupoConfiguracion.Factor//$scope.kngFactor
			});
		}else{
			tablaConceptoConfiguracion.push({
				IdConcepto		: $scope.seleccionConcepto.IdConcepto
			  ,	IdConceptoGrupo	: $scope.seleccionConceptoGrupoConfiguracion.IdConceptoGrupo
			  , IdItem    	: $scope.seleccionConceptoConfiguarcion.IdItem
			  , Operacion	: $scope.seleccionConceptoConfiguarcion.Operacion
			  , Factor		: $scope.seleccionConceptoConfiguarcion.Factor
			});
		}

		var dataParametros = {
			p_IdEmpresa 					: $scope.SesionIdEmpresa,
			p_IdUsuario 					: $scope.SesionIdUsuario,
			p_IdModulo  					: 'NOR',
			p_UsaReglasNegocio 				: 'N',
			p_TipoOperacion					: tipoOperacion,
			p_tablaConceptoConfiguracion	: tablaConceptoConfiguracion
		};
		
		bcsComunes.ventanaOverlay($scope.winID,false);
		bcsServiciosNormativa.MantenedorConceptoConfiguracion(dataParametros).$promise.then(function(respuesta){
   			
	   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
	   		var mensajeOk = '';
	   		var mensajeErr = '';
	   		if(tipoOperacion === 'I'){
	   			mensajeOk = 'UI_CONFIG_CONCEPTO_EXITO';
		   		mensajeErr = 'UI_CONFIG_CONCEPTO_ERROR';	
	   		}else{
	   			mensajeOk = 'UI_CONFIG_CONCEPTO_ELIMINAR_EXITO';
		   		mensajeErr = 'UI_CONFIG_CONCEPTO_ELIMINAR_ERROR';
	   		}
			//se valido la orden
			if (respuesta.P_OUT_ERRORESCONTADOR === 0) 
			{				
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant(mensajeOk),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
			}
			else
		    {
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				bcsComunes.ventanaMsg({
					mensaje: $translate.instant(mensajeErr),
					conflictos: util.parseServerResponse(respuesta),
					errores : [ {
				                 Codigo : 'COD',
				                 Descripcion : respuesta.P_OUT_ERRORESDETALLE[0].Descripcion
				                } 
					          ]
				},$scope.winID);
				var i = 0;
	            for(i; i<respuesta.P_OUT_ERRORESCONTADOR; i++)
	            {
	            	bcsComunes.log().info("Error: " + respuesta.P_OUT_ERRORESDETALLE[i].Descripcion);       
	            }  
		    }
			
			$scope.limpiar();
			bcsComunes.ventanaOverlay($scope.winID,false);
			var filters = new Array();
			filters.push( { field: "IdConceptoGrupo", operator: "eq", value: $scope.seleccionConceptoGrupoConfiguracion.IdConceptoGrupo });
			dataSourceConceptoConfiguracion.filter(filters);
			
   		},function(respuesta){
   			//ERROR AL CREAR LA ORDEN
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant('UI_OPERACION_CON_ERRORES'),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
   		});	
	};
	
	$scope.habilitaGrabarGrupo = function(){
		if($scope.nombreGrupoConcepto != undefined && $scope.nombreGrupoCortoConcepto != undefined && $scope.kngFactorGrupo != undefined && $scope.nombreGrupoConcepto != '' && $scope.nombreGrupoCortoConcepto != '' && $scope.factorGrupo != '')
			return true;
		else
			return false;
	};
	
	$scope.habilitaGrabar = function(){
		if($scope.itemSeleccionado != false && $scope.operacionseleccionado != false)// && $scope.Factor != undefined && $scope.Factor > 0)
			return true;
		else
			return false;
	};
	
	$scope.eliminarConcepto = function(tipoOperacion) {
		
		var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_ELIMINARA_CONCEPTO_SELECCIONADO') );
		t.result.then(function(result){
			if (result === OPCIONES_BCSALERTA.ACEPTAR){
				var tablaConcepto	= [];
				tablaConcepto.push({
					IdConcepto 	: $scope.seleccionConcepto.IdConcepto 
				  , Nombre    	: $scope.seleccionConcepto.NombreConcepto
				  , NombreCorto	: $scope.seleccionConcepto.NombreCortoConcepto
				});
				
				var dataParametros = {
					p_IdEmpresa 					: $scope.SesionIdEmpresa,
					p_IdUsuario 					: $scope.SesionIdUsuario,
					p_IdModulo  					: 'NOR',
					p_UsaReglasNegocio 				: 'N',
					p_TipoOperacion					: tipoOperacion,
					p_tablaConcepto					: tablaConcepto
				};
				
				bcsComunes.ventanaOverlay($scope.winID,false);
				bcsServiciosNormativa.MantenedorConcepto(dataParametros).$promise.then(function(respuesta){
			   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			   		//$scope.habilitadoItem = false;
			   		$scope.habilitadoGrupoConcepto = false;

					//se valido la orden
					if (respuesta.P_OUT_ERRORESCONTADOR === 0) 
					{				
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant('UI_CONCEPTO_ELIMINAR_EXITO'),
							conflictos:util.parseServerResponse(respuesta)
						},$scope.winID);
					}
					else
				    {
						bcsComunes.setConflictos(util.parseServerResponse(respuesta));
						bcsComunes.ventanaMsg({
							mensaje: "Error",
							conflictos: util.parseServerResponse(respuesta),
							errores : [ {Codigo : 'COD', Descripcion : $translate.instant('UI_CONCEPTO_ELIMINAR_ERROR')} ]
						},$scope.winID);
				    }
					
					bcsComunes.ventanaOverlay($scope.winID,false);
					$scope.habilitado = false;						
					$scope.limpiar();
					$scope.limpiarConceptoGrupo();
					$scope.limpiarConcepto();
					dataSourceConcepto.read();	
					dataSourceConceptoConfiguracion.removeAll();
		   		},function(respuesta){
		   			//ERROR AL CREAR LA ORDEN
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant('UI_CONCEPTO_ELIMINAR_ERROR'),
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);
		   		});	
			};
		});
	};
	
    //---------------------------------------------
    //----------------- MODIFICAR -----------------
    //---------------------------------------------
	
	$scope.modificarConcepto = function(){
		// Envio de parametros a la nueva ventana
		var ventanaId = bcsComunes.ventanaAbrir("AgregarConcepto",{Concepto: $scope.seleccionConcepto}, false, false, $scope.winID);
		var promesa = bcsComunes.ventanaPromesa(ventanaId);
		
		// Actualiza la grilla que lista los Conceptos
		$.when(promesa).then(function(){			
			$scope.habilitado = false;						
			$scope.limpiar();
			$scope.limpiarConcepto();
			dataSourceConceptoConfiguracion.removeAll();
			dataSourceConcepto.read();
//			$scope.ConceptoConfiguracion = '';
		});
		
		//Deshabilita los campos
		$scope.habilitado = false;
	};
	
    //---------------------------------------------
    //----------------- SERVICIOS -----------------
    //---------------------------------------------
	
	transporte = function(Grid) {
		if (Grid === 'TipoConcepto')			
			url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=12088";  //ConsultarConceptos
		if (Grid === 'TipoConceptoConfiguracion')			
			url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=12089";  //ConsultarConceptosConfiguracion
		if (Grid === 'TipoConceptoGrupoConfiguracion')			
			url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=20079";  //ConsultarConceptosConfiguracion
		return {
			read : {
				url : url,
				dataType : "json"
			}
		};
	};  
	
    //---------------------------------------------
    //------------------ MODELOS ------------------
    //---------------------------------------------
	
	// Definir modelo Vista Concepto.
	ModeloConcepto = function() {
		return {
			model: {
				fields : {
					IdConcepto 		 	: { type: "number"},
					NombreConcepto 	 	: { type: "string"},
					NombreCortoConcepto	: { type: "string"},
					Estado 				: { type: "string"},
	        	}
			}
		};
	}; 
	
	// Definir modelo Vista Concepto Configuracion.
	ModeloConceptoConfiguracion = function() {
		return {
			model: {
    	        	fields:{
    	        		IdConceptoGrupo			: { type: "number"},
    	        		IdItem 					: { type: "number"},
    	        		NombreItem				: { type: "string"},
    	        		Factor 					: { type: "string"},
    	        		Operacion 				: { type: "string"},
    	        	} 
			}
		};
	};
	
	// Definir modelo Vista Concepto Configuracion.
	ModeloConceptoGrupoConfiguracion = function() {
		return {
			model: {
    	        	fields:{
    	        		IdConceptoGrupo 		: { type: "number"},
    	        		IdConcepto				: { type: "number"},
    	        		Nombre					: { type: "string"},
    	        		NombreCorto 			: { type: "string"},
    	        		Factor					: { type: "number"},
    	        	} 
			}
		};
	};
	
    //---------------------------------------------
    //---------------- DATASOURCES ----------------
    //---------------------------------------------
	
	// Función definida para evento - Conceptos
	dataSourceConcepto =  new bcs.newDataSource({
		type 		: "odata",
		definition 	: {
						serverFiltering 	: true, // Filtrado de datos en el servidor.
						serverPaging 		: true, // Paginado de los datos en el servidor.
						serverSorting 		: false, // Ordenamiento de los datos en el servidor.
						sortable 			: true,
						pageSize 			: 50,
						transport 			: transporte('TipoConcepto'),
						schema 				: ModeloConcepto(),
				  	}
	});			

	// Función definida para Concepto Configuracion
	dataSourceConceptoConfiguracion = new bcs.newDataSource({
		type 		: "odata", 
		definition 	: {
						serverFiltering 	: true, // Filtrado de datos en el servidor.
						serverPaging 		: true, // Paginado de los datos en el servidor.
						serverSorting 		: true, // Ordenamiento de los datos en el servidor.
						pageSize 			: 50,
						transport 			: transporte('TipoConceptoConfiguracion'),
						schema 				: ModeloConceptoConfiguracion(),
						filter              : [{ field: "IdConceptoGrupo", operator: "eq", value: 0 }]
					  }
 
	});
	
	// Función definida para Concepto Grupo Configuracion
	dataSourceConceptoGrupoConfiguracion = new bcs.newDataSource({
		type 		: "odata", 
		definition 	: {
						serverFiltering 	: true, // Filtrado de datos en el servidor.
						serverPaging 		: true, // Paginado de los datos en el servidor.
						serverSorting 		: true, // Ordenamiento de los datos en el servidor.
						pageSize 			: 50,
						transport 			: transporte('TipoConceptoGrupoConfiguracion'),
						schema 				: ModeloConceptoGrupoConfiguracion(),
						filter              : [{ field: "IdConcepto", operator: "eq", value: 0 }]
					  }
 
	});
	
    //---------------------------------------------
    //------------------ COLUMNAS -----------------
    //---------------------------------------------

	// Columnas de la grilla izquierda, Conceptos 
	columnasConcepto = function() {
		return [ 
		        {field: "IdConcepto"			, title: "UI_CONCEPTO"			, width: "30px"}
		       ,{field: "NombreConcepto"		, title: "UI_NOMBRE_CONCEPTO"	, width: "140px"}
		       ,{field: "NombreCortoConcepto"	, title: "UI_NOMBRE_CORTO"		, width: "40px"}
				];
	};
	
	// Columnas de la grilla derecha, Conceptos Configuracion 
	columnasConceptoConfiguracion = function() {
		return  [
	         	{field: "IdItem"				, title: "UI_ITEM", width: "20px"}
			   ,{field: "NombreItem"			, title: "UI_NOMBRE_ITEM", width: "120px"}
		       ,{field: "Operacion"				, title: "UI_OPERACION", width: "50px"}
		       ,{field: "Factor"				, title: "UI_FACTOR", width: "60px"} 
	            ];
	};	
	// Columnas de la grilla derecha, Conceptos Configuracion Grupo
	columnasConceptoGrupoConfiguracion = function() {
		return  [
				{field: "IdConceptoGrupo"			, title: "UI_CONCEPTO_GRUPO"			, width: "30px"}
		       ,{field: "Nombre"		, title: "UI_NOMBRE_CONCEPTO"	, width: "140px"}
		       ,{field: "NombreCorto"	, title: "UI_NOMBRE_CORTO"		, width: "40px"}
		       ,{field: "Factor"	, title: "UI_FACTOR"		, width: "40px"}
	            ];
	};
	onChange  = function() { 
        var filters = new Array();
        	filtersAutori = new Array();
	        filters.push( { field: "", operator: "eq", value: $scope.seleccionConcepto });
    };
    
    // Configuración Conceptos
	$scope.Concepto = {
			autoBind		: true,
			dataSource		: dataSourceConcepto,
			columns			: columnasConcepto(),		
			filterable		: true,
			sortable		: true,
			pageable		: true,
			height			: 567,
		    selectable 		: "row",
		    change			: onChange(),
	};
	
	// Configuración ConceptoConfiguracion
	$scope.ConceptoConfiguracion = {
			dataSource			: dataSourceConceptoConfiguracion,
			columns				: columnasConceptoConfiguracion(),		
			sortable			: true,
			pageable			: true,
			autoBind			: false,
			height				: 150,
		    selectable 			: "row"
	};
	
	// Configuración ConceptoGrupoConfiguracion
	$scope.ConceptoGrupoConfiguracion = {
			dataSource			: dataSourceConceptoGrupoConfiguracion,
			columns				: columnasConceptoGrupoConfiguracion(),		
			sortable			: true,
			pageable			: true,
			autoBind			: false,
			height				: 150,
		    selectable 			: "row"
	};
	
    //---------------------------------------------
    //------------------ EVENTOS ------------------
    //---------------------------------------------
	
	// Evento que capta la selección de un Concepto
	$scope.$watch('seleccionConcepto', function(newValue, oldValue) {  
		console.log(dataSourceConceptoGrupoConfiguracion);
		$scope.TipoOperacion = 'I'; //CELCONDOR-581
		
		if (angular.equals(newValue,oldValue)) return;
		if (newValue!==false) {
			
			// Habilita los inputs  del grid derecho
			$scope.habilitado = true;
			$scope.habilitadoGrupoConcepto = true;
			$scope.inputDisabled = false;
			$scope.habilitadoGrupoEliminar = false;
			$scope.limpiar();
			$scope.filtro = {NombreConcepto	: $scope.seleccionConcepto.NombreConcepto, 
							 NombreCorto	: $scope.seleccionConcepto.NombreCortoConcepto
							};

			if(dataSourceConceptoConfiguracion !== undefined && $scope.seleccionConcepto.IdConcepto !== undefined && $scope.seleccionConcepto.IdConcepto !== '')
			{
				var filters = new Array();
				filters.push( { field: "IdConcepto", operator: "eq", value: $scope.seleccionConcepto.IdConcepto });
				dataSourceConceptoGrupoConfiguracion.filter(filters);
				dataSourceConceptoConfiguracion.removeAll();
			}
		}
	},true);
	
	//CGO
	// Doble click Evento que capta la selección de un Concepto Grupo
		
		$scope.detalleConceptoGrupoConfiguracion = function(datos) {
			$scope.nombreGrupoConcepto 		= datos.Nombre;
			$scope.nombreGrupoCortoConcepto	= datos.NombreCorto;
			$scope.kngFactorGrupo 			= datos.Factor;
			$scope.modificaGrupo			= "S";

		};
		
	// Doble click Evento que capta la selección de un Item	
	/*	$scope.detalleConceptoConfiguarcion = function(datos) {
			$scope.NombreItem				= datos.IdItem;
			$scope.Signo					= datos.Operacion;
			$scope.kngFactor 				= datos.Factor;
		};
	*/	
	//CGO
	// Evento que capta la selección de un Concepto Grupo
	$scope.$watch('seleccionConceptoGrupoConfiguracion', function(newValue, oldValue) {  
		if (angular.equals(newValue,oldValue)) return;
		if (newValue!==false) {
			
			// Habilita los inputs  del grid derecho
			$scope.limpiarConceptoGrupo();//cgo
			$scope.habilitadoItem = true;
			$scope.habilitadoConcepto = true;
			$scope.inputDisabled = false;
			$scope.habilitadoEliminar = false;
			$scope.limpiar();
			$scope.filtro = {NombreConcepto	: $scope.seleccionConcepto.NombreConcepto, 
							 NombreCorto	: $scope.seleccionConcepto.NombreCortoConcepto
							};

			if(dataSourceConceptoGrupoConfiguracion !== undefined && $scope.seleccionConceptoGrupoConfiguracion !== undefined && $scope.seleccionConceptoGrupoConfiguracion.IdConceptoGrupo !== '')
			{
				var filters = new Array();
				filters.push( { field: "IdConceptoGrupo", operator: "eq", value: $scope.seleccionConceptoGrupoConfiguracion.IdConceptoGrupo });
				dataSourceConceptoConfiguracion.filter(filters);
			}
		}
	},true);
	
	// Evento que capta la selección de un Item
	$scope.$watch('itemSeleccionado', function(newValue, oldValue) {  
		if (angular.equals(newValue,oldValue)) return;
		if (newValue!==false) {
			
			// Habilita los inputs  del grid derecho
			
			$scope.inputDisabledfactor = false;
		}
	},true);

	// Evento que capta la selección 
	$scope.$watch('operacionseleccionado', function(newValue, oldValue) {  
		if (angular.equals(newValue,oldValue)) return;
		if (newValue!=="") { 
			
			$scope.habilitadoGrabar = true;
		}
	},true);
	
	

	// Evento que capta la selección de un Concepto
	$scope.$watch('seleccionConceptoConfiguarcion', function(newValue, oldValue) {  
		if (angular.equals(newValue,oldValue)) return;
		if (newValue!==false) {
			
			// Habilita los inputs  del grid derecho
			$scope.habilitadoEliminar = true;
			
		}
	},true);
	
	// Evento que capta la selección de un Concepto Grupo
	$scope.$watch('seleccionConceptoGrupoConfiguracion', function(newValue, oldValue) {				   
		if (angular.equals(newValue,oldValue)) return;
		if (newValue!==false) {
			
			// Habilita los inputs  del grid derecho
			$scope.habilitadoGrupoEliminar = true;
			
		}
	},true);

	//---------------------------------------------
	//----------------- LIMPIAR ------------------
	//---------------------------------------------
	
	// Función para limpiar los campos de la búsqueda 
	$scope.limpiarConceptoGrupo = function(){
		$scope.nombreGrupoConcepto = undefined;
		$scope.nombreGrupoCortoConcepto = undefined;
		$scope.kngFactorGrupo = undefined;
	}
	
	$scope.limpiar = function(){
		$scope.NombreItem = undefined;
		//$scope.kngFactor = undefined;
		$scope.Operacion = undefined;
		$scope.inputDisabledfactor = true;
		//$scope.habilitado = false;
	}
	
	$scope.limpiarConcepto = function(){
		$scope.filtro.NombreConcepto = '';
		$scope.filtro.NombreCorto = '';
	}

});
