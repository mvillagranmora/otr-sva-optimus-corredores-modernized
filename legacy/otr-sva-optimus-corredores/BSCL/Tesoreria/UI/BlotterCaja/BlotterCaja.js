angular.module('ModuloTesoreria').controller('BlotterCajaCtrl',
	function($scope,$uibModal,$translate,bcsComunes, $resource, $timeout, bcsAlerta,OPCIONES_BCSALERTA){
	
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	
	$scope.Caja = "";	
	$scope.habilitado = false;
	$scope.habilitadoMoneda = false;
	$scope.habilitadoUsuario = false;
	$scope.habilitadoProducto = false;
	//var seleccionado = "";
	//var seleccionadoMoneda = "";
    $scope.grillas = {};
    

	$scope.select = function(grid)
	{
		$timeout(function() {			
			var t = $("."+grid).find("[kendo-grid]");
			for (var i = 0; i < t.length; i++ ) {
				if ($(t[i]).data("kendo-grid")) $(t[i]).data("kendo-grid").refresh()
			}
		},10);
	};


    //---------------------------------------------
    //--------- AGREGAR Y MODIFICAR CAJAS ---------
    //---------------------------------------------
	$scope.agregarCaja = function() {
		var ventanaId = bcsComunes.ventanaAbrir("AgregarCajas",{}, false,false,$scope.winID);
		var promesa = bcsComunes.ventanaPromesa(ventanaId);
		
		$.when(promesa).then(function() {			
			dataSourceCaja.read();
		});
		$scope.habilitado = false;
	};
	
	$scope.modificarCaja = function(){
		var ventanaId = bcsComunes.ventanaAbrir("AgregarCajas",
				{Caja: $scope.seleccionCaja}, false,false,$scope.winID);
		var promesa = bcsComunes.ventanaPromesa(ventanaId);
		
		$.when(promesa).then(function(){			
			dataSourceCaja.read();
		});
		$scope.habilitado = false;
	};

	$scope.arqueoCaja = function(){
		var ventanaId = bcsComunes.ventanaAbrir("ArqueoCaja",{Caja: $scope.seleccionCaja}, false,false,$scope.winID);
		var promesa = bcsComunes.ventanaPromesa(ventanaId);
		
		$.when(promesa).then(function(){			
			dataSourceCaja.read();
		});
		$scope.habilitado = false;
	};
	
	//-----------------------------------------------------
    //--------- AGREGAR Y MODIFICAR CAJA-MONEDA -----------
    //-----------------------------------------------------
	$scope.agregarCajaMoneda = function() {
		var ventanaId = bcsComunes.ventanaAbrir("AgregarCajaMoneda", $scope.seleccionCaja, false,false,$scope.winID );
		var promesa = bcsComunes.ventanaPromesa(ventanaId);
		
		$.when(promesa).then(function() {
			dataSourceMoneda.read();
		});
		$scope.habilitadoMoneda = false;
	};
	
	$scope.modificarCajaMoneda = function(){
		var ventanaId = bcsComunes.ventanaAbrir("AgregarCajaMoneda", 
				{CajaMoneda: $scope.seleccionCajaMoneda}, false,false,$scope.winID );
		var promesa = bcsComunes.ventanaPromesa(ventanaId);
		$.when(promesa).then(function() {
			dataSourceMoneda.read();
		});
		$scope.habilitadoMoneda = false;
	};
	
	//----------------------------------------------------
    //--------- AGREGAR Y MODIFICAR CAJA-USUARIO ---------
    //----------------------------------------------------
	$scope.agregarCajaUsuario = function() {
		var ventanaId = bcsComunes.ventanaAbrir("AgregarCajaUsuario", $scope.seleccionCaja, false,false,$scope.winID);
		var promesa = bcsComunes.ventanaPromesa(ventanaId);
		
		$.when(promesa).then(function() {			
			dataSourceUsuario.read();
		});
		$scope.habilitadoUsuario = false;
	};
	
	$scope.modificarCajaUsuario = function(){
		var ventanaId = bcsComunes.ventanaAbrir("AgregarCajaUsuario", 
				{ CajaUsuario : $scope.grillas.seleccionCajaUsuario }, false,false,$scope.winID );
		var promesa = bcsComunes.ventanaPromesa(ventanaId);
		
		$.when(promesa).then(function() {			
			dataSourceUsuario.read();
		});
		$scope.habilitadoUsuario = false;
	}
	
	//-------------------------------------------------------------
    //--------- AGREGAR Y MODIFICAR CAJA-PRODUCTO EMPRESA ---------
    //-------------------------------------------------------------
	$scope.agregarCajaProducto = function() {
		var ventanaId = bcsComunes.ventanaAbrir("AgregarCajaProducto", $scope.seleccionCaja, false,false,$scope.winID );
		var promesa = bcsComunes.ventanaPromesa(ventanaId);
		
		$.when(promesa).then(function() {			
			dataSourceProductoEmpresa.read();
		});
		$scope.habilitadoUsuario = false;
	};
	
	$scope.modificarCajaProducto = function() {
		var ventanaId = bcsComunes.ventanaAbrir("AgregarCajaProducto", 
				{ CajaProducto : $scope.grillas.seleccionCajaProductoEmpresa }, false,false,$scope.winID );
		var promesa = bcsComunes.ventanaPromesa(ventanaId);
		
		$.when(promesa).then(function() {			
			dataSourceProductoEmpresa.read();
		});
		$scope.habilitadoUsuario = false;
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
	
	// Definir modelo, Vista Cajas.
	ModeloCaja= function() {
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
	
 // Modelo datos vista Usuarios por Caja
	ModeloUsuario = function() {
		return {
			model: {
    	        	fields:{
    	        		IdCajaUsuario 			: { type: "number"},
    	        		IdEmpresa				: { type: "number"}, 
    	        		IdCaja					: { type: "number"},
    	        		IdUsuario				: { type: "string"}, 
    	        		SobregiroUI				: { type: "string"},
    	        		FechaInicio 			: { type: "sysdate"},
    	        		FechaTermino 			: { type: "sysdate"},
    	        		IdEstado				: { type: "string"}, 
    	        		NombreCompleto 			: { type: "string"},
    	        		NombreEstado 			: { type: "string"},
    	        		CodigoUsuario			: { type: "string"},
    	        		NombreEstadoCajaUsuario	: { type: "string"},
    	        	} 
			}
		};
	}; 
	
	ModeloProducto = function() {
		return {
			model: {
    	        	fields:{
    	        		IdCajaProductoEmpresa		: { type : "number"},
    	        		IdEmpresa					: { type : "number"}, 
    	        		IdCaja						: { type : "number"},
    	        		CodigoCaja					: { type : "string"},    	        		
    	        		NombreCaja 					: { type : "string"},
    	        		IdProductoEmpresa 			: { type : "string"},
    	        		IdTipoProducto 				: { type : "string"},
    	        		IdEstado					: { type : "string"},
    	        		NombreTipoProducto			: { type : "string"},
    	        		NombreCajaProductoEmpresa	: { type : "string"},
    	        	} 
			}
		};
	}; 
	
	// Función definida para evento - cajas
	dataSourceCaja =  new bcs.DataSource({
		type 		: "odata", 
		definition 	: {
						serverFiltering 	: true, // Filtrado de datos en el servidor.
						serverPaging 		: true, // Paginado de los datos en el servidor.
						serverSorting 		: false, // Ordenamiento de los datos en el servidor.
						sortable 			: true,
						pageSize 			: 50,
						transport 			: transporte('TipoEvento'),
						schema 				: ModeloCaja(),
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
	
	
	dataSourceUsuario = new bcs.DataSource({		
			type 		: "odata", 
			definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: true, // Ordenamiento de los datos en el servidor.
							pageSize 			: 50,
							transport 			: transporte('TipoUsuario'),
							schema 				: ModeloUsuario(),
							filter: [{ field: "IdCaja", operator: "eq", value: 0 }]
	    				  }							
	});	
	
	dataSourceProductoEmpresa = new bcs.DataSource({		
			type 		: "odata", 
			definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: true, // Ordenamiento de los datos en el servidor.
							pageSize 			: 50,
							transport 			: transporte('TipoProducto'),
							schema 				: ModeloProducto(),
							filter: [{ field: "IdCaja", operator: "eq", value: 0 }]
	    				  }							
	});
	
	
	// Grilla principal, Cajas.
	columnasCaja = function() {
		return [ 
		        {field: "CodigoCaja", title: "Código", width: "100px" }
		       ,{field: "NombreCaja", title: "Nombre", width: "100px"}
		       ,{field: "NombreEstado", title: "Estado", width: "70px"}
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
	
	// Grilla Caja/Usuario
	columnasUsuario = function() {
		return  [
				{field: "CodigoUsuario", 	title: "Usuario", 	width: "70px" }
			   ,{field: "NombreCompleto", 	title: "Nombre", 	width: "160px" }	
			   ,{field: "SobregiroUI",	 	title: "Sobregiro",	width: "50px" }	
			   ,{field: "FechaInicio", 		title: "Desde", 	width: "70px" }
			   ,{field: "FechaTermino", 	title: "Hasta", 	width: "70px" }
			   ,{field: "NombreEstadoCajaUsuario", 		title: "Estado", 	width: "60px" }
			   ];
	};
	
	
	columnasProductoEmpresa = function() {
		return  [
				{field: "NombreTipoProducto", title: "UI_TIPO_PRODUCTO", width: "100px" }
			   ,{field: "NombreCajaProductoEmpresa", title: "UI_ID_ESTADO", width: "70px"}
			   ];
	};
	
	onChange  = function() {
        var filters = new Array();
        	filtersAutori = new Array();
        	
	        filters.push( { field: "", operator: "eq", value: $scope.seleccionCaja });
    };
    
	
	$scope.Caja = {
			dataSource			: dataSourceCaja,
			columns				: columnasCaja(),		
			sortable			: true,
			pageable			: true,
			autoBind			: true,  // comentar
			height				: 450,
		    selectable 			: "row",
		    change				: onChange(),
		};
	
	$scope.Moneda = {
			dataSource			: dataSourceMoneda,
			columns				: columnasMoneda(),		
			sortable			: true,
			pageable			: true,
			autoBind			: false,  // false comentar, Mostrar data al
											// levantar pantalla
			height				: 200,
		    selectable 			: "row"
		};
	
	$scope.Usuario = {
			dataSource			: dataSourceUsuario,
			columns				: columnasUsuario(),		
			sortable			: true,
			pageable			: true,
			autoBind			: false,  // false comentar, Mostrar data al
											// levantar pantalla
			height				: 200,
		    selectable 			: "row"
		};
	
	$scope.ProductoEmpresa = {
			dataSource			: dataSourceProductoEmpresa,
			columns				: columnasProductoEmpresa(),		
			sortable			: true,
			pageable			: true,
			autoBind			: false,  // false comentar, Mostrar data al
											// levantar pantalla
			height				: 200,
		    selectable 			: "row"
		};	
	
	$scope.$watch('seleccionCaja', function(newValue, oldValue) {  
		if (angular.equals(newValue,oldValue)) return;
		if (newValue!==false) {
			
			$scope.habilitado = true;
			$scope.habilitadoMoneda = false;
			$scope.habilitadoUsuario =  false;
			$scope.habilitadoProducto =  false;

			if(dataSourceMoneda !== undefined && $scope.seleccionCaja.IdCaja !== undefined && $scope.seleccionCaja.IdCaja !== '')
			{
				var filters = new Array();
				filters.push( { field: "IdCaja", operator: "eq", value: $scope.seleccionCaja.IdCaja });
				dataSourceMoneda.filters(filters);
			}

			if(dataSourceUsuario !== undefined && $scope.seleccionCaja.IdCaja !== undefined && $scope.seleccionCaja.IdCaja !== ''){
				var filtersUsuario = new Array();
				filtersUsuario.push( { field: "IdCaja", operator: "eq", value: $scope.seleccionCaja.IdCaja })
				dataSourceUsuario.filters(filtersUsuario);
				}
			
			if(dataSourceProductoEmpresa !== undefined && $scope.seleccionCaja.IdCaja !== undefined && $scope.seleccionCaja.IdCaja !== ''){
				var filtersProductos = new Array();
				filtersProductos.push( { field: "IdCaja", operator: "eq", value: $scope.seleccionCaja.IdCaja })				
				dataSourceProductoEmpresa.filters(filtersProductos);
				}
		}
	},true);
	
	$scope.$watch('seleccionCajaMoneda', function(newValue, oldValue){
		if (angular.equals(newValue, oldValue)) return;
		if (newValue !== false) {
			$scope.habilitadoMoneda = true;
		}
	});
	
	$scope.$watch('grillas.seleccionCajaUsuario', function(newValue, oldValue){
		if (angular.equals(newValue, oldValue)) return;
		if (newValue !== false) {
			$scope.habilitadoUsuario = true;
		}
	});
	
	$scope.$watch('grillas.seleccionCajaProductoEmpresa', function(newValue, oldValue){
		if (angular.equals(newValue, oldValue)) return;
		if (newValue !== false) {
			$scope.habilitadoProducto = true;
		}
	});
	
	llenarDatos = function (NombreMantenedor, Estado){
		var bloq =  new Array()
		if (NombreMantenedor === 'CAJA'){
			bloq.push(	{NumFila: 1, Campo: 'IdCaja', Valor: $scope.seleccionCaja.IdCaja, ValorAnterior: $scope.seleccionCaja.IdCaja},
						{NumFila: 1, Campo: 'IdEstado', Valor: Estado, ValorAnterior: $scope.seleccionCaja.IdEstadoCaja});
		}
		return bloq;
	};
	
	var ModificarMantenedorCaja = function(v_Dato)
    {
	       var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=144";
	       var r = $resource
         ("", {}, {
	                 'urlConsulta': { method: "POST", url: urlConsulta}
                  }
         );
	       var post = r.urlConsulta(v_Dato);
	       return post.$promise;
    };
	
	$scope.bloquear = function (NombreMantenedor, Estado) {
		bcsComunes.ventanaOverlay($scope.winID, true);
		
		var parametrosSP = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo : 'TES',
				p_SimularOperacion : 'N',
				p_UsaReglasNegocio : 'S',
				p_Mantenedor : NombreMantenedor,
				p_FechaModificacion : util.dateSPdate(new Date()),
				p_Dato : llenarDatos(NombreMantenedor, Estado)
		};
		bcsComunes.log().info(JSON.stringify(parametrosSP));
		
		ModificarMantenedorCaja(parametrosSP).then(
				function(result) {
					if (result.P_OUT_ADVERTENCIASCONTADOR){
						bcsComunes.msg("warning","Advertencia","La operacion tiene advertencias");
					}else if (result.P_OUT_ERRORESCONTADOR){
						bcsComunes.msg("error","Error","La operacion tiene errores");
					}else
						{
							bcsComunes.msg("info","info","Operacion realizada con exito");							
							dataSourceCaja.read();
						}
					bcsComunes.ventanaOverlay($scope.winID, false);
				},
				function(result){
					bcsComunes.msg("error","Error","La operacion tiene errores");
					bcsComunes.ventanaOverlay($scope.winID, false);
				});
	};
	
	var EliminarCaja = function(v_Dato){
    	var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=155";
    	var r = $resource ("", {}, {
    		'urlConsulta': {method: "POST", url: urlConsulta}
    	});
    	var post = r.urlConsulta(v_Dato);
    	return post.$promise;
    };
    
	$scope.eliminarCaja = function (){
		
		var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_ELIMINARA_CAJA_SELECCIONADA') );
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR){

				bcsComunes.ventanaOverlay($scope.winID, true);		
				var parametrosSP = {
						p_IdEmpresa : $scope.SesionIdEmpresa,
						p_IdUsuario : $scope.SesionIdUsuario,
						p_IdModulo : 'TES',
						p_UsaReglasNegocio : 'S',
						p_IdCaja: $scope.seleccionCaja.IdCaja,
						p_FechaModificacion : util.dateSPdate(new Date())
				};
				bcsComunes.log().info(JSON.stringify(parametrosSP));
				EliminarCaja(parametrosSP).then(
						function(result){
							if (result.P_OUT_ADVERTENCIASCONTADOR){
								bcsComunes.msg("warning", "Advertencia", "La Operacion tiene advertencias");
							}else if (result.P_OUT_ERRORESCONTADOR){
								bcsComunes.msg("error", "Error", "La operacion tiene errores, " + result.P_OUT_ERRORESDETALLE[0].Descripcion);
								var i = 0;
								for(i; i<result.P_OUT_ERRORESCONTADOR; i++)
								{
									bcsComunes.log().info("Error: " + result.P_OUT_ERRORESDETALLE[i].Descripcion);       
								}    
							}else {
								bcsComunes.msg("info", "info", "Operacion realizada con exito");
								$scope.habilitado = false;						
								dataSourceCaja.read();						
								dataSourceProductoEmpresa.read();

								dataSourceUsuario.read();
								dataSourceMoneda.read();
								$scope.habilitado = false;
								$scope.habilitadoMoneda = false;
								$scope.habilitadoUsuario = false;
								$scope.habilitadoProducto = false;
							}
							bcsComunes.ventanaOverlay($scope.winID, false);
						},
						function(result){
							bcsComunes.msg("error", "Error", "La operacion tiene errores");
							bcsComunes.ventanaOverlay($scope.winID, false);
						});
			}
		});
		return;				
	};

	var firstWidth = $('#Win_CajasTesoreria');
        firstWidth.css('width', '990px');
	var firstHeight = $('#Win_CajasTesoreria');
        firstHeight.css('height', '578px');
});
