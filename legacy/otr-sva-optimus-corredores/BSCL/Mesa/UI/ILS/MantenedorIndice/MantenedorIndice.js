angular.module('ModuloMesa').controller('MantenedorIndiceCtrl',
	function($scope,$uibModal,$translate,bcsComunes, $resource, $timeout, bcsAlerta,OPCIONES_BCSALERTA){
	
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);

	$scope.Indice = "";	
	$scope.filtro = {indice: '', tipo: '', nombreCorto:''};	
	$scope.habilitado = false;
	$scope.inputDisabled = true;	
//	$scope.habilitadoMoneda = false;
//	$scope.habilitadoUsuario = false;
//	$scope.habilitadoProducto = false;
//	//var seleccionado = "";
//	//var seleccionadoMoneda = "";
//    $scope.grillas = {};
//    
//
//	$scope.select = function(grid)
//	{
//		$timeout(function() {			
//			var t = $("."+grid).find("[kendo-grid]");
//			for (var i = 0; i < t.length; i++ ) {
//				if ($(t[i]).data("kendo-grid")) $(t[i]).data("kendo-grid").refresh()
//			}
//		},10);
//	};

    //---------------------------------------------
    //--------- AGREGAR Y MODIFICAR CAJAS ---------
    //---------------------------------------------
	$scope.agregarIndice = function() {
		var ventanaId = bcsComunes.ventanaAbrir("AgregarIndice",{}, false, false, $scope.winID);
		var promesa = bcsComunes.ventanaPromesa(ventanaId);
		
		$.when(promesa).then(function() {			
			dataSourceCaja.read();
		});
		$scope.habilitado = false;
	};
	
	$scope.modificarIndice = function(){
		var ventanaId = bcsComunes.ventanaAbrir("AgregarIndice",
				{Caja: $scope.seleccionIndice}, false,false,$scope.winID);
		var promesa = bcsComunes.ventanaPromesa(ventanaId);
		
		$.when(promesa).then(function(){			
			dataSourceCaja.read();
		});
		$scope.habilitado = false;
	};

	transporte = function(Grid) {
		if (Grid === 'TipoEvento')			
			url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1027";  //ConsultarCajas
		if (Grid === 'TipoMoneda')			
			url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1028";  //ConsultarCajaMoneda
		if (Grid === 'TipoUsuario')	
			url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1029"; //ConsultarCajaUsuario
		if (Grid === 'TipoProducto')
			url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1030"; //ConsultarCajaProductoEmpresa
 
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
					IdCaja 		 : { type: "number"},
	        		IdEmpresa 	 : { type: "number"},
	        		CodigoCaja 	 : { type: "string"},
	        		NombreCaja 	 : { type: "string"},
	        		IdEstadoCaja : { type: "string"},
	        		NombreEstado : { type: "string"},
	        		}
			}
		};
	}; 
	
	// Definir modelo Vista Moneda.
	ModeloMoneda = function() {
		return {
			model: {
    	        	fields:{
    	        		IdCajaMoneda 			: { type: "number"},
    	        		IdCaja 					: { type: "number"},
    	        		IdEmpresaCaja 			: { type: "number"},    	        		
    	        		NombreCaja 				: { type: "string"},
    	        		NombreLargoCaja 		: { type: "string"},
    	        		IdEstadoCaja 			: { type: "string"},
    	        		IdMoneda 				: { type: "string"},
    	        		NombreMoneda 			: { type: "string"},
    	        		NombreLargoCajaMoneda 	: { type: "string"},
    	        		Saldo 					: { type: "number"},
    	        		IdTipoMovimiento 		: { type: "string"},    	        		
    	        		IdEstadoCajaMoneda 		: { type: "string"},
    	        		NombreEstadoCajaMoneda 	: { type: "string"},
    	        	} 
			}
		};
	};
	
	// Función definida para evento - cajas
	dataSourceIndice =  new bcs.DataSource({
		type 		: "odata", 
		definition 	: {
						serverFiltering 	: true, // Filtrado de datos en el servidor.
						serverPaging 		: true, // Paginado de los datos en el servidor.
						serverSorting 		: false, // Ordenamiento de los datos en el servidor.
						sortable 			: true,
						pageSize 			: 50,
						transport 			: transporte('TipoEvento'),
						schema 				: ModeloIndice(),
						filter              : [{ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa }],	
					  }
	});			

	// Función definida para Moneda
	dataSourceMoneda = new bcs.DataSource({
		type 		: "odata", 
		definition 	: {
						serverFiltering 	: true, // Filtrado de datos en el servidor.
						serverPaging 		: true, // Paginado de los datos en el servidor.
						serverSorting 		: true, // Ordenamiento de los datos en el servidor.
						pageSize 			: 50,
						transport 			: transporte('TipoMoneda'),
						schema 				: ModeloMoneda(),
						filter              : [{ field: "IdCaja", operator: "eq", value: 0 }]
					  }
 
	});

	// Columnas de la grilla izquierda, Indices 
	columnasIndice = function() {
		return [ 
		        {field: "CodigoCaja", title: "Id Indice", width: "50px" }
		       ,{field: "NombreCaja", title: "Nombre", width: "100px"}
		       ,{field: "NombreEstado", title: "Nombre Corto", width: "70px"}
				];
	};
	
	// Columnas de la grilla derecha
	columnasMoneda = function() {
		return  [
				 {field: "IdMoneda", title: "Id Concepto", width: "50px"}
				,{field: "NombreEstadoCajaMoneda", title: "Nombre Concepto", width: "100px"}  
				,{field: "IdTipoMovimiento", title: "Posición", width: "50px"}
				,{field: "IdMoneda", title: "Operación", width: "50px"}
				,{field: "Saldo", title: "Factor", width: "50px"}
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
	
	$scope.Moneda = {
			dataSource			: dataSourceMoneda,
			columns				: columnasMoneda(),		
			sortable			: true,
			pageable			: true,
			autoBind			: false,  // false comentar, Mostrar data al
			height				: 340,
		    selectable 			: "row"
	};
	
	$scope.$watch('seleccionIndice', function(newValue, oldValue) {  
		if (angular.equals(newValue,oldValue)) return;
		if (newValue!==false) {
			
			$scope.habilitado = true;
			$scope.habilitadoMoneda = false;
			$scope.habilitadoUsuario =  false;
			$scope.habilitadoProducto =  false;
			$scope.inputDisabled = false;
			
			$scope.filtro.NombreIndice = $scope.seleccionIndice.NombreCaja;
			$scope.filtro = {indice: $scope.seleccionIndice.NombreCaja, tipo: $scope.seleccionIndice.CodigoCaja, nombreCorto:$scope.seleccionIndice.NombreEstado};

			if(dataSourceMoneda !== undefined && $scope.seleccionIndice.IdCaja !== undefined && $scope.seleccionIndice.IdCaja !== '')
			{
				var filters = new Array();
				filters.push( { field: "IdCaja", operator: "eq", value: $scope.seleccionIndice.IdCaja });
				dataSourceMoneda.filters(filters);
			}
		}
	},true);

//	llenarDatos = function (NombreMantenedor, Estado){
//		var bloq =  new Array()
//		if (NombreMantenedor === 'CAJA'){
//			bloq.push(	{NumFila: 1, Campo: 'IdCaja', Valor: $scope.seleccionIndice.IdCaja, ValorAnterior: $scope.seleccionIndice.IdCaja},
//						{NumFila: 1, Campo: 'IdEstado', Valor: Estado, ValorAnterior: $scope.seleccionIndice.IdEstadoCaja});
//		}
//		return bloq;
//	};
//	
//	var ModificarMantenedorCaja = function(v_Dato)
//    {
//	       var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=144";
//	       var r = $resource
//         ("", {}, {
//	                 'urlConsulta': { method: "POST", url: urlConsulta}
//                  }
//         );
//	       var post = r.urlConsulta(v_Dato);
//	       return post.$promise;
//    };
//	
//	$scope.bloquear = function (NombreMantenedor, Estado) {
//		bcsComunes.ventanaOverlay($scope.winID, true);
//		
//		var parametrosSP = {
//				p_IdEmpresa : $scope.SesionIdEmpresa,
//				p_IdUsuario : $scope.SesionIdUsuario,
//				p_IdModulo : 'TES',
//				p_SimularOperacion : 'N',
//				p_UsaReglasNegocio : 'S',
//				p_Mantenedor : NombreMantenedor,
//				p_FechaModificacion : util.dateSPdate(new Date()),
//				p_Dato : llenarDatos(NombreMantenedor, Estado)
//		};
//		bcsComunes.log().info(JSON.stringify(parametrosSP));
//		
//		ModificarMantenedorCaja(parametrosSP).then(
//				function(result) {
//					if (result.P_OUT_ADVERTENCIASCONTADOR){
//						bcsComunes.msg("warning","Advertencia","La operacion tiene advertencias");
//					}else if (result.P_OUT_ERRORESCONTADOR){
//						bcsComunes.msg("error","Error","La operacion tiene errores");
//					}else
//						{
//							bcsComunes.msg("info","info","Operacion realizada con exito");							
//							dataSourceCaja.read();
//						}
//					bcsComunes.ventanaOverlay($scope.winID, false);
//				},
//				function(result){
//					bcsComunes.msg("error","Error","La operacion tiene errores");
//					bcsComunes.ventanaOverlay($scope.winID, false);
//				});
//	};
//	
//	var EliminarCaja = function(v_Dato){
//    	var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=155";
//    	var r = $resource ("", {}, {
//    		'urlConsulta': {method: "POST", url: urlConsulta}
//    	});
//    	var post = r.urlConsulta(v_Dato);
//    	return post.$promise;
//    };
//    
//	$scope.eliminarCaja = function (){
//		
//		var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_ELIMINARA_CAJA_SELECCIONADA') );
//		t.result.then(function(result){
//			if (result===OPCIONES_BCSALERTA.ACEPTAR){
//
//				bcsComunes.ventanaOverlay($scope.winID, true);		
//				var parametrosSP = {
//						p_IdEmpresa : $scope.SesionIdEmpresa,
//						p_IdUsuario : $scope.SesionIdUsuario,
//						p_IdModulo : 'TES',
//						p_UsaReglasNegocio : 'S',
//						p_IdCaja: $scope.seleccionIndice.IdCaja,
//						p_FechaModificacion : util.dateSPdate(new Date())
//				};
//				bcsComunes.log().info(JSON.stringify(parametrosSP));
//				EliminarCaja(parametrosSP).then(
//						function(result){
//							if (result.P_OUT_ADVERTENCIASCONTADOR){
//								bcsComunes.msg("warning", "Advertencia", "La Operacion tiene advertencias");
//							}else if (result.P_OUT_ERRORESCONTADOR){
//								bcsComunes.msg("error", "Error", "La operacion tiene errores, " + result.P_OUT_ERRORESDETALLE[0].Descripcion);
//								var i = 0;
//								for(i; i<result.P_OUT_ERRORESCONTADOR; i++)
//								{
//									bcsComunes.log().info("Error: " + result.P_OUT_ERRORESDETALLE[i].Descripcion);       
//								}    
//							}else {
//								bcsComunes.msg("info", "info", "Operacion realizada con exito");
//								$scope.habilitado = false;						
//								dataSourceCaja.read();						
//								dataSourceProductoEmpresa.read();
//
//								dataSourceUsuario.read();
//								dataSourceMoneda.read();
//								$scope.habilitado = false;
//								$scope.habilitadoMoneda = false;
//								$scope.habilitadoUsuario = false;
//								$scope.habilitadoProducto = false;
//							}
//							bcsComunes.ventanaOverlay($scope.winID, false);
//						},
//						function(result){
//							bcsComunes.msg("error", "Error", "La operacion tiene errores");
//							bcsComunes.ventanaOverlay($scope.winID, false);
//						});
//			}
//		});
//		return;				
//	};
	
	// Función para limpiar los campos de la búsqueda 
	$scope.limpiar = function(){
		$scope.BuscarConcepto = undefined;
		$scope.Factor = undefined;
		$scope.filtro.Operacion = 0;
		$scope.filtro.Posicion = 0;
	}
	
});
