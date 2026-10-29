angular.module('ModuloNormativa').controller('MantenedorIndiceCtrl',
	function($scope,$uibModal,$translate,bcsComunes, $resource, $timeout, bcsAlerta,OPCIONES_BCSALERTA,bcsServiciosNormativa){

	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);

	$scope.Indice = "";	
	$scope.filtro = {indice: '', tipo: '', nombreCorto:''};	
	$scope.habilitado = false;
	$scope.inputDisabled = true;	
	$scope.Factor = null;
	
	$scope.tipoOperacion = null;

    //---------------------------------------------
    //--------- AGREGAR Y MODIFICAR INDICES -------
    //---------------------------------------------
	$scope.agregarIndice = function() {
		var ventanaId = bcsComunes.ventanaAbrir("AgregarIndice",{}, false, false, $scope.winID);
		var promesa = bcsComunes.ventanaPromesa(ventanaId);
		
		$.when(promesa).then(function() {			
			dataSourceIndice.read();
		});
		$scope.habilitado = false;
	};
	
	$scope.modificarIndice = function(){
		var ventanaId = bcsComunes.ventanaAbrir("AgregarIndice",{Indice: $scope.seleccionIndice}, false,false,$scope.winID);
		var promesa = bcsComunes.ventanaPromesa(ventanaId);
		
		$.when(promesa).then(function(){			
			dataSourceIndice.read();
		});
		$scope.habilitado = false;
	};

//	CELCONDOR-581
	$scope.detalleIndice = function(datos) {
		$scope.data.Concepto								=  datos.IdConcepto ; 
		$scope.filtro.Posicion								= (datos.NombrePosicionFraccion === 'Denominador' ? 'D' : 'N');
		$scope.Factor										=  datos.Factor;
		$scope.filtro.Operacion								= (datos.Operacion === '+' ? '1' : '2');		
		$scope.habilitado = false;
		$scope.tipoOperacion = 'M';
		
	};
	
	
//	CELCONDOR-581
	
	$scope.eliminarIndice = function (){
		var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_ELIMINARA_INDICE_SELECCIONADA'));
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR){

				bcsComunes.ventanaOverlay($scope.winID, true);		
				var parametrosSP = {
						p_IdEmpresa : $scope.SesionIdEmpresa,
						p_IdUsuario : $scope.SesionIdUsuario,
						p_IdModulo : 'ILS',
						p_UsaReglasNegocio : 'S',
						p_TipoOperacion : 'E',
						p_tablaIndice : [{IdIndice: $scope.seleccionIndice.IdIndice, 
							  Nombre: '',
							  NombreCorto: 0,
							  Tipo: '',
							  Estado: 'VIG'}]
				};
				bcsComunes.log().info(JSON.stringify(parametrosSP));
				bcsServiciosNormativa.MantenedorIndice(parametrosSP, 
						function(respuesta){
							if (respuesta.P_OUT_ADVERTENCIASCONTADOR){
								bcsComunes.msg("warning", "Advertencia", $translate.instant('UI_OPERACION_CON_ADVERTENCIAS'));
							}else if (respuesta.P_OUT_ERRORESCONTADOR){
								bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
								
								bcsComunes.ventanaMsg({
								      mensaje:$translate.instant('UI_OPERACION_CON_ERRORES'),
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
							}else {
								bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
								bcsComunes.ventanaMsg({
									mensaje:$translate.instant('UI_INDICE_ELIMINAR_EXITO'),
									conflictos:util.parseServerResponse(respuesta)
								},$scope.winID);
								bcsComunes.ventanaOverlay($scope.winID, false);								$scope.habilitado = false;
								dataSourceIndice.read();
								$scope.filtro.indice = '';
								$scope.filtro.nombreCorto = '';
							}
						}, 
						function(respuesta){
							bcsComunes.msg("error", "Error", $translate.instant('UI_INDICE_ELIMINAR_ERROR'));
							bcsComunes.ventanaOverlay($scope.winID, false);
						}
						);
			}
		});
		return;				
	};
	
    //---------------------------------------------
    //--------- AGREGAR Y MODIFICAR CONCEPTOS -----
    //---------------------------------------------

	var successCrearIndiceConcepto = function (respuesta){
		//AGREGAR
		if($scope.Indice && respuesta.P_OUT_IDINDICECONCEPTO !== undefined && respuesta.P_OUT_IDINDICECONCEPTO.length > 0 && respuesta.P_OUT_ERRORESCONTADOR == 0 && $scope.tipoOperacion === 'I'){
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant('UI_CONCEPTO_INGRESAR_EXITO'),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
			bcsComunes.ventanaOverlay($scope.winID, false);	
			
            dataSourceIndiceConfiguracion.read();
			$scope.limpiar();
		}
		//MODIFICAR
		else if ($scope.Indice && respuesta.P_OUT_ERRORESCONTADOR == 0 && $scope.tipoOperacion === 'M'){
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant('UI_CONCEPTO_MODIFICAR_EXITO'),//CGO
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
			bcsComunes.ventanaOverlay($scope.winID, false);
			dataSourceIndiceConfiguracion.read();
			$scope.limpiar();
			$scope.habilitadoIndiceConfiguracion = false;
		}
		//ERROR
		
		//ELIMINAR
		else if ($scope.Indice && respuesta.P_OUT_ERRORESCONTADOR == 0){
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant('UI_CONCEPTO_ELIMINAR_EXITO'),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
			bcsComunes.ventanaOverlay($scope.winID, false);
			dataSourceIndiceConfiguracion.read();
			$scope.limpiar();
			$scope.habilitadoIndiceConfiguracion = false;
		}
		//ERROR
		else {
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
			
			bcsComunes.ventanaMsg({
			      mensaje:$translate.instant("UI_OPERACION_ERROR"),
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
		bcsComunes.ventanaOverlay($scope.winID, false);
	};
	
	var errorCrearIndiceConcepto = function(respuesta)
	{	
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
		bcsComunes.msg("error","error",$translate.instant('UI_CONCEPTO_INGRESAR_ERROR'));
		bcsComunes.ventanaOverlay($scope.winID, false);
	};
	
	$scope.agregarConcepto = function() {
		bcsComunes.ventanaOverlay($scope.winID, true);
		var parametrosSP = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo : 'NOR',
				p_UsaReglasNegocio : 'S',
				p_TipoOperacion : 'I',
				p_tablaIndiceConcepto :
					[{IdIndice: $scope.seleccionIndice.IdIndice, 
					  IdConcepto: $scope.seleccionIndiceConfiguracion.Concepto,
					  Posicion: '',
					  Factor: 0,
					  Operacion: '',
					  Estado: 'VIG'}]
			};
		bcsComunes.log().info(JSON.stringify(parametrosSP));
		bcsServiciosNormativa.MantenedorIndiceConfiguracion(parametrosSP, successCrearIndiceConcepto, errorCrearIndiceConcepto);
	};

	$scope.habilitaGrabar = function(){
		
		if($scope.conceptoSeleccionado != false && $scope.posicionSeleccionado != false && 
		   $scope.operacionSeleccionado != false && $scope.Factor != undefined && $scope.Factor > 0)
			return true;
		else
			return false;
	};
	$scope.$watch('data.kngFactor', function(newValue, oldValue) {  
		if (angular.equals(newValue,oldValue)) return;
		if (newValue!==false) {
			if(newValue <= 0)
				bcsComunes.msg("warning", "Error", "El Factor debe der mayor a 0.");
		}
	},true);
	
	$scope.MantenedorIndiceConfiguracion = function(tipoOperacion) {
		var tablaIndiceConcepto = [];

		if(tipoOperacion === 'I' || $scope.tipoOperacion === 'M'){
			
			if ($scope.tipoOperacion === 'M' ){
				
				tipoOperacion = $scope.tipoOperacion;
			}
		

			if($scope.seleccionIndice.IdIndice === undefined)
			{
				bcsComunes.msg("warning", "Error", "Debe seleccionar un Indice.");
				return;
			} 
			if($scope.conceptoSeleccionado.IdConcepto === undefined)
			{
				bcsComunes.msg("warning", "Error", "Debe seleccionar un Concepto.");
				return;
			}
			if($scope.operacionSeleccionado.IdOperacionCtb === undefined)
			{
				bcsComunes.msg("warning", "Error", "Debe seleccionar un tipo de operación.");
				return;
			}
//			if($scope.data.kngFactor === undefined || $scope.data.kngFactor < 0)
			if($scope.Factor === undefined || $scope.Factor < 0)
			{
				bcsComunes.msg("warning", "Error", "El Factor no debe ser menor a 0.");
				return;
			}
			
			tablaIndiceConcepto.push({
				    IdIndice: $scope.seleccionIndice.IdIndice, 
					IdConcepto: $scope.data.Concepto,
					Posicion: $scope.filtro.Posicion,
					Factor: $scope.Factor,
					Operacion: $scope.operacionSeleccionado.CodigoOperacionCtb,
				    Estado: 'VIG'
				});
		}else{
			tablaIndiceConcepto.push({
				    IdIndice: $scope.seleccionIndiceConfiguracion.IdIndice , 
				    IdConcepto: $scope.seleccionIndiceConfiguracion.IdConcepto ,
				    Posicion: '',
				    Factor: 0,
				    Operacion: '',
				    Estado: 'VIG'
				});
		}

		var dataParametros = {
			p_IdEmpresa 					: $scope.SesionIdEmpresa,
			p_IdUsuario 					: $scope.SesionIdUsuario,
			p_IdModulo  					: 'NOR',
			p_UsaReglasNegocio 				: 'N',
			p_TipoOperacion					: tipoOperacion,
			p_tablaIndiceConcepto    : tablaIndiceConcepto
		};
		
		bcsComunes.ventanaOverlay($scope.winID,false);
		bcsServiciosNormativa.MantenedorIndiceConfiguracion(dataParametros, successCrearIndiceConcepto, errorCrearIndiceConcepto);
	};
	
	transporte = function(Grid) {
		if (Grid === 'Indice')			
			url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=13008";  //ConsultarCajas
		if (Grid === 'Concepto')			
			url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=13009";  //ConsultarCajaMoneda
		if (Grid === 'IndiceConfiguracion')	
			url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=13014"; //ConsultarCajaUsuario
 
		return {
			read : {
				url : url,
				dataType : "json"
			}
		};
	};  

	// Definir modelo, Vista Indice.
	ModeloIndice = function() {
		return {
			model: {
				fields : {
					IdIndice	 : { type: "number"},
	        		IdEmpresa 	 : { type: "number"},
	        		Nombre   	 : { type: "string"},
	        		NombreCorto  : { type: "string"},
	        		Tipo		 : { type: "string"},
	        		Estado       : { type: "string"},
	        		}
			}
		};
	}; 

			 
	// Definir modelo Vista Moneda.
	ModeloIndiceConfiguracion  = function() {
		return {
			model: {
    	        	fields:{
    	        		IdIndice 			    : { type: "number"},
    	        		IdConcepto 				: { type: "number"},
    	        		NombreConcepto 			: { type: "string"},    	        		
    	        		IdEmpresa 				: { type: "number"},
    	        		NombrePosicionFraccion  : { type: "string"},
    	        		Factor 			        : { type: "number"},
    	        		Operacion 				: { type: "string"},
    	        		Estado 			        : { type: "string"},
    	        	} 
			}
		};
	};
	
	// Función definida para evento - cajas
	dataSourceIndice =  new bcs.newDataSource({
		type 		: "odata", 
		definition 	: {
						serverFiltering 	: true, // Filtrado de datos en el servidor.
						serverPaging 		: true, // Paginado de los datos en el servidor.
						serverSorting 		: false, // Ordenamiento de los datos en el servidor.
	                    sort 				: { field: "IdIndice", dir: "ASC" },
						sortable 			: true,
						pageSize 			: 50,
						transport 			: transporte('Indice'),
						schema 				: ModeloIndice(),
						filter              : [{ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa }],	
					  }
	});			

	// Función definida para Moneda
	dataSourceIndiceConfiguracion = new bcs.newDataSource({
		type 		: "odata", 
		definition 	: {
						serverFiltering 	: true, // Filtrado de datos en el servidor.
						serverPaging 		: true, // Paginado de los datos en el servidor.
						serverSorting 		: true, // Ordenamiento de los datos en el servidor.
	                    sort 				: { field: "IdConcepto", dir: "ASC" },
						pageSize 			: 50,
						transport 			: transporte('IndiceConfiguracion'),
						schema 				: ModeloIndiceConfiguracion(),
						filter              : [{ field: "IdConcepto", operator: "eq", value: 0 }]
					  }
 
	});

	// Columnas de la grilla izquierda, Indices 
	columnasIndice = function() {
		return [ 
		        {field: "IdIndice", title: "UI_ID_INDICE", width: "50px" }
		       ,{field: "Nombre", title: "UI_NOMBRE", width: "100px"}
		       ,{field: "NombreCorto", title: "UI_NOMBRE_CORTO", width: "70px"}
		       ,{field: "Tipo", title: "UI_TIPO", width: "30px"}	       
				];
	};
	
	// Columnas de la grilla derecha
	columnasIndiceConfiguracion = function() {
		return  [
				 {field: "IdConcepto", title: "UI_ID_CONCEPTO", width: "50px"}
				,{field: "NombreConcepto", title: "UI_NOMBRE_CONCEPTO", width: "100px"}  
				,{field: "NombrePosicionFraccion", title: "UI_POSICION", width: "50px"}
				,{field: "Operacion", title: "UI_OPERACION", width: "50px"}
				,{field: "Factor", title: "UI_FACTOR", width: "50px"}
	            ];
	};	

	onChange  = function() {
        var filters = new Array();
        	filtersAutori = new Array();
	        filters.push( { field: "", operator: "eq", value: $scope.seleccionIndice });
    };

	$scope.Indice = {
			autoBind		: true,
			dataSource		: dataSourceIndice,
			columns			: columnasIndice(),		
			filterable		: true,
			sortable		: true,
			pageable		: true,
			height			: 450,
		    selectable 		: "row",
		    change			: onChange(),
	};
	
	$scope.IndiceConfiguracion = {
			dataSource			: dataSourceIndiceConfiguracion,
			columns				: columnasIndiceConfiguracion(),		
			sortable			: true,
			pageable			: true,
			autoBind			: false,  // false comentar, Mostrar data al
			height				: 340,
		    selectable 			: "row"
	};
	
	$scope.$watch('seleccionIndice', function(newValue, oldValue) {  
		if (angular.equals(newValue,oldValue)) return;
		if (newValue!==false) {

			$scope.tipoOperacion = 'I';//CGO
			
			$scope.habilitado = true;
			$scope.habilitadoIndiceConfiguracion = false;
			$scope.habilitadoUsuario =  false;
			$scope.habilitadoProducto =  false;
			$scope.inputDisabled = false;
			
			$scope.filtro.NombreIndice = $scope.seleccionIndice.Nombre;
			$scope.filtro = {indice: $scope.seleccionIndice.Nombre, nombreCorto:$scope.seleccionIndice.NombreCorto};

			if(dataSourceIndiceConfiguracion !== undefined && $scope.seleccionIndice.IdIndice !== undefined && $scope.seleccionIndice.Nombre !== '')
			{
				var filters = new Array();
				filters.push( { field: "IdIndice", operator: "eq", value: $scope.seleccionIndice.IdIndice });
				dataSourceIndiceConfiguracion.filter(filters);
			}
		}
	},true);

	$scope.$watch('seleccionIndiceConfiguracion', function(newValue, oldValue) {  
		if (angular.equals(newValue,oldValue)) return;
		if (newValue!==false) {
			
			$scope.habilitadoIndiceConfiguracion = true;
		}
	},true);

	// Función para limpiar los campos de la búsqueda 
	$scope.limpiar = function(){
		$scope.data.Concepto = 0;
		$scope.Factor = undefined;
		$scope.data.kngFactor = undefined;
		$scope.filtro.Operacion = 0;
		$scope.filtro.Posicion = 0;
	}
	
});
