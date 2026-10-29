angular.module('ModuloMesa').controller('MantenedorInformeCtrl',
	function($scope,$uibModal,$translate,bcsComunes, $resource, $timeout, bcsAlerta,OPCIONES_BCSALERTA){
	
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
//	
	$scope.Informe = "";	
	$scope.filtro = {NombreInforme: ''};
	$scope.inputDisabled = true;	
//	$scope.habilitado = false;
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
	$scope.agregarInforme = function() {
		var ventanaId = bcsComunes.ventanaAbrir("AgregarInforme",{}, false, false, $scope.winID);
		var promesa = bcsComunes.ventanaPromesa(ventanaId);
		
		$.when(promesa).then(function() {			
			dataSourceCaja.read();
		});
		$scope.habilitado = false;
	};
	
	$scope.modificarInforme = function(){
		var ventanaId = bcsComunes.ventanaAbrir("AgregarInforme",
				{Caja: $scope.seleccionInforme}, false,false,$scope.winID);
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

	// Definir modelo, Vista Informe.
	ModeloInforme = function() {
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
	dataSourceInforme =  new bcs.DataSource({
		type 		: "odata", 
		definition 	: {
						serverFiltering 	: true, // Filtrado de datos en el servidor.
						serverPaging 		: true, // Paginado de los datos en el servidor.
						serverSorting 		: false, // Ordenamiento de los datos en el servidor.
						sortable 			: true,
						pageSize 			: 50,
						transport 			: transporte('TipoEvento'),
						schema 				: ModeloInforme(),
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

	// Grilla principal, Cajas.
	columnasInforme = function() {
		return [ 
		        {field: "CodigoCaja", title: "Id Tipo Informe", width: "70px" }
		       ,{field: "NombreCaja", title: "Nombre", width: "100px"}
		       ,{field: "NombreEstado", title: "Empresa", width: "100px"}
				];
	};
	
	// Grilla Monedas
	columnasMoneda = function() {
		return  [
				{field: "IdMoneda", title: "Moneda", width: "50px"}
			   ,{field: "Saldo", title: "UI_SALDO", width: "120px", template : " <label> #:  (util.bcs_formato(Saldo,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
		       ,{field: "IdTipoMovimiento", title: "Tipo Movimiento", width: "50px"}
		       ,{field: "NombreEstadoCajaMoneda", title: "Estado", width: "60px"}  
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
	
	$scope.Moneda = {
			dataSource			: dataSourceMoneda,
			columns				: columnasMoneda(),		
			sortable			: true,
			pageable			: true,
			autoBind			: false,  // false comentar, Mostrar data al
			height				: 358,
		    selectable 			: "row"
	};
	
	$scope.$watch('seleccionInforme', function(newValue, oldValue) {  
		if (angular.equals(newValue,oldValue)) return;
		if (newValue!==false) {
			
			$scope.habilitado = true;
			$scope.habilitadoMoneda = false;
			$scope.habilitadoUsuario =  false;
			$scope.habilitadoProducto =  false;
			$scope.inputDisabled = false;			
			
			$scope.filtro.NombreInforme = $scope.seleccionInforme.NombreCaja;

			if(dataSourceMoneda !== undefined && $scope.seleccionInforme.IdCaja !== undefined && $scope.seleccionInforme.IdCaja !== '')
			{
				var filters = new Array();
				filters.push( { field: "IdCaja", operator: "eq", value: $scope.seleccionInforme.IdCaja });
				dataSourceMoneda.filters(filters);
			}
		}
	},true);

//	llenarDatos = function (NombreMantenedor, Estado){
//		var bloq =  new Array()
//		if (NombreMantenedor === 'CAJA'){
//			bloq.push(	{NumFila: 1, Campo: 'IdCaja', Valor: $scope.seleccionInforme.IdCaja, ValorAnterior: $scope.seleccionInforme.IdCaja},
//						{NumFila: 1, Campo: 'IdEstado', Valor: Estado, ValorAnterior: $scope.seleccionInforme.IdEstadoCaja});
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
	$scope.eliminarCaja = function (){
		
		var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_ELIMINARA_CAJA_SELECCIONADA') );
//		t.result.then(function(result){
//			if (result===OPCIONES_BCSALERTA.ACEPTAR){
//
//				bcsComunes.ventanaOverlay($scope.winID, true);		
//				var parametrosSP = {
//						p_IdEmpresa : $scope.SesionIdEmpresa,
//						p_IdUsuario : $scope.SesionIdUsuario,
//						p_IdModulo : 'TES',
//						p_UsaReglasNegocio : 'S',
//						p_IdCaja: $scope.seleccionInforme.IdCaja,
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
		return;				
	};
	
	// Función para limpiar los campos de la búsqueda 
	$scope.limpiar = function(){
		$scope.BuscarCuentaContable = undefined;
		$scope.BuscarIndice = undefined;
		$scope.OrdenIndice = undefined;
	}
});
