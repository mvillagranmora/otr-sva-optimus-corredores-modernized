angular.module('ModuloMesa').controller('MantenedorConceptoCtrl',
	function($scope,$uibModal,$translate,bcsComunes, $resource, $timeout, bcsAlerta,OPCIONES_BCSALERTA){
	
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);

	//Inicializacion de variables
	$scope.Concepto = "";	
	$scope.filtro = {Concepto: '', nombreCorto:''};	
	$scope.habilitado = false;
	$scope.inputDisabled = true;

    //---------------------------------------------
    //----------------- AGREGAR -------------------
    //---------------------------------------------
	$scope.agregarConcepto = function() {
		// Envio de parametros a la nueva ventana 
		var ventanaId = bcsComunes.ventanaAbrir("AgregarConcepto",{}, false, false, $scope.winID);
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
	
	$scope.agregarConceptoConfiguracion = function() {
		
		var tablaConceptoConfiguracion	= [];
		tablaConceptoConfiguracion.push({
											IdConcepto 	: $scope.seleccionConcepto.IdConcepto 
										  , IdItem    	: $scope.itemSeleccionado.IdItem
										  , Operacion	: $scope.operacion.seleccionado
										  , Factor		: $scope.kngFactor
										});	
		
		var dataParametros = {
			p_IdEmpresa 					: $scope.SesionIdEmpresa,
			p_IdUsuario 					: $scope.SesionIdUsuario,
			p_IdModulo  					: 'NOR',
			p_UsaReglasNegocio 				: 'N',
			p_TipoOperacion					: 'I',
			p_tablaConceptoConfiguracion	: tablaConceptoConfiguracion
		};
		bcsComunes.ventanaOverlay(idVentana,false);
		bcsServiciosMesa.IngresarCuentaContableItem(dataParametros).$promise.then(function(respuesta){
   			
	   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
	   		var mensaje = '';
			//se valido la orden
			if (respuesta.P_OUT_ERRORESCONTADOR === 0) 
			{				
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("Cuenta Contable ingresada correctamente."),
					conflictos:util.parseServerResponse(respuesta)
				},idVentana);
			}
			else
		    {
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				bcsComunes.ventanaMsg({
					mensaje: "Error",
					conflictos: util.parseServerResponse(respuesta),
					errores : [ {
				                 Codigo : 'COD',
				                 Descripcion : 'Existen problemas al ingresar Cuentas Contables.'
				                } 
					          ]
				},idVentana);
				
		    }
			bcsComunes.ventanaOverlay(idVentana,false);
			var filters = new Array();
			filters.push( { field: "IdItem", operator: "eq", value: $scope.seleccionItem.IdItem });
			dataSourceItemConfiguracion.filters(filters);
			
   		},function(respuesta){
   			//ERROR AL CREAR LA ORDEN
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("Existen problemas al ingresar la operación"),
				conflictos:util.parseServerResponse(respuesta)
			},idVentana);
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
			dataSourceConcepto.read();						
			dataSourceConceptoConfiguracion.removeAll();
			
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
					Estado 			: { type: "string"},
	        	}
			}
		};
	}; 
	
	// Definir modelo Vista Concepto Configuracion.
	ModeloConceptoConfiguracion = function() {
		return {
			model: {
    	        	fields:{
    	        		IdConcepto 				: { type: "number"},
    	        		IdItem 					: { type: "number"},
    	        		Factor 					: { type: "string"},
    	        		Operacion 				: { type: "string"},
    	        	} 
			}
		};
	};
	
    //---------------------------------------------
    //---------------- DATASOURCES ----------------
    //---------------------------------------------
	
	// Función definida para evento - Conceptos
	dataSourceConcepto =  new bcs.DataSource({
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
	dataSourceConceptoConfiguracion = new bcs.DataSource({
		type 		: "odata", 
		definition 	: {
						serverFiltering 	: true, // Filtrado de datos en el servidor.
						serverPaging 		: true, // Paginado de los datos en el servidor.
						serverSorting 		: true, // Ordenamiento de los datos en el servidor.
						pageSize 			: 50,
						transport 			: transporte('TipoConceptoConfiguracion'),
						schema 				: ModeloConceptoConfiguracion(),
						filter              : [{ field: "IdConcepto", operator: "eq", value: 0 }]
					  }
 
	});
	
    //---------------------------------------------
    //------------------ COLUMNAS -----------------
    //---------------------------------------------

	// Columnas de la grilla izquierda, Conceptos 
	columnasConcepto = function() {
		return [ 
		        {field: "IdConcepto"			, title: "UI_CONCEPTO", width: "50px", align: "center"}
		       ,{field: "NombreConcepto"		, title: "UI_NOMBRE_CONCEPTO", width: "100px"}
		       ,{field: "NombreCortoConcepto"	, title: "UI_NOMBRE_CORTO", width: "80px", align: "center"}
				];
	};
	
	// Columnas de la grilla derecha, Conceptos Configuracion 
	columnasConceptoConfiguracion = function() {
		return  [
	         	{field: "IdItem"				, title: "UI_ITEM", width: "50px"}
			   ,{field: "IdItem"				, title: "UI_NOMBRE_ITEM", width: "120px"}
		       ,{field: "Operacion"				, title: "UI_OPERACION", width: "50px"}
		       ,{field: "Factor"				, title: "UI_FACTOR", width: "60px"} 
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
			height			: 450,
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
	
    //---------------------------------------------
    //------------------ EVENTOS ------------------
    //---------------------------------------------
	
	// Evento que capta la selección de un Concepto
	$scope.$watch('seleccionConcepto', function(newValue, oldValue) {  
		if (angular.equals(newValue,oldValue)) return;
		if (newValue!==false) {
			
			// Habilita los inputs  del grid derecho
			$scope.habilitado = true;
			$scope.inputDisabled = false;
			
			$scope.filtro = {NombreConcepto	: $scope.seleccionConcepto.NombreConcepto, 
							 NombreCorto	: $scope.seleccionConcepto.NombreCortoConcepto
							};

			if(dataSourceConceptoConfiguracion !== undefined && $scope.seleccionConcepto.IdConcepto !== undefined && $scope.seleccionConcepto.IdConcepto !== '')
			{
				var filters = new Array();
				filters.push( { field: "IdConcepto", operator: "eq", value: $scope.seleccionConcepto.IdConcepto });
				dataSourceConceptoConfiguracion.filters(filters);
			}
		}
	},true);

	//---------------------------------------------
	//----------------- LIMPIAR ------------------
	//---------------------------------------------
	
	// Función para limpiar los campos de la búsqueda 
	$scope.limpiar = function(){
		
		$scope.BuscarCuentaContable = undefined;
		$scope.Factor = undefined;
		$scope.filtro.Operacion = 0;
		$scope.filtro.Saldo = 0;
		$scope.filtro.Condicion = 0;
		$scope.Monto = undefined;		
	}
	
	$scope.limpiarConcepto = function(){
		
		$scope.filtro.Concepto = '';
		$scope.filtro.nombreCorto = '';
		$scope.filtro.tipo = '';		
		
	}
	//---------------------------------------------
	//----------------- ELIMINAR ------------------
	//--------------------------------------------- 
	
	var EliminarConcepto = function(v_Dato){
		
    	var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=12079";
    	var r = $resource ("", {}, {
    		'urlConsulta': {method: "POST", url: urlConsulta}
    	});
    	var post = r.urlConsulta(v_Dato);
    	return post.$promise;
    };
    
	$scope.eliminarConcepto = function (){
		
		//FIXME CAMBIAR MENSAJE 
		var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_ELIMINARA_CAJA_SELECCIONADA') );
		
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR){
				
				bcsComunes.ventanaOverlay($scope.winID, true);
				
				// Declaracion de parametros
				var parametrosSP = {
						p_Estado : '',
						p_FechaModificacion : util.dateSPdate(new Date()),
						p_IdEmpresa : $scope.SesionIdEmpresa,
						p_IdConcepto: $scope.seleccionConcepto.IdConcepto,
						p_IdModulo : 'ILS',
						p_IdTipoConcepto : $scope.seleccionConcepto.IdTipoConcepto,
						p_IdUsuario : $scope.SesionIdUsuario,
						p_Mantenedor : 'Concepto',					
						p_Nombre : '',
						p_NombreCorto : '',
						p_SimularOperacion : 'N',
						p_TipoOperacion : 'E',
						p_UsaReglasNegocio : 'S'
				};
				bcsComunes.log().info(JSON.stringify(parametrosSP));
				
				// Eliminar el Concepto
				EliminarConcepto(parametrosSP).then(
						function(result){
							if (result.P_OUT_ADVERTENCIASCONTADOR){
								
								//FIXME CAMBIAR MENSAJE (Traduccion) 
								bcsComunes.msg("warning", "Advertencia", "La Operacion tiene advertencias");
							}else if (result.P_OUT_ERRORESCONTADOR){
								bcsComunes.msg("error", "Error", "La operacion tiene errores, " + result.P_OUT_ERRORESDETALLE[0].Descripcion);
								var i = 0;
								for(i; i<result.P_OUT_ERRORESCONTADOR; i++)
								{
									bcsComunes.log().info("Error: " + result.P_OUT_ERRORESDETALLE[i].Descripcion);       
								}    
							}else {
								//FIXME CAMBIAR MENSAJE (Traduccion)
								bcsComunes.msg("info", "info", "Operacion realizada con exito");
								
								$scope.habilitado = false;						
								$scope.limpiar();
								$scope.limpiarConcepto();
								dataSourceConcepto.read();	
								dataSourceConceptoConfiguracion.removeAll();
							}
							bcsComunes.ventanaOverlay($scope.winID, false);
						},
						function(result){
							//FIXME CAMBIAR MENSAJE (Traduccion)
							bcsComunes.msg("error", "Error", "La operacion tiene errores");
							bcsComunes.ventanaOverlay($scope.winID, false);
						});
			}
		});	
		return;				
	};
	

});
