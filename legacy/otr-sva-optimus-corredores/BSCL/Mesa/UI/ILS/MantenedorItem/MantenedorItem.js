angular.module('ModuloMesa').controller('MantenedorItemCtrl',
	function($scope,$uibModal,$translate,bcsComunes,bcsServiciosMesa, $resource, $timeout, bcsAlerta,OPCIONES_BCSALERTA){
	
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	var idVentana = bcsComunes.ventanaWinId($scope);
	
	//Inicializacion de variables
	$scope.data = {};
	$scope.Item = "";	
	$scope.filtro = {item: '', tipo: '', nombreCorto:''};	
	$scope.habilitado = false;
	$scope.habilitadoDigitada = false;
	$scope.inputDisabled = true;

    //---------------------------------------------
    //----------------- MODIFICAR -----------------
    //---------------------------------------------
	
	$scope.modificarItem = function(){
		// Envio de parametros a la nueva ventana
		var ventanaId = bcsComunes.ventanaAbrir("AgregarItem",{Item: $scope.seleccionItem}, false, false, $scope.winID);
		var promesa = bcsComunes.ventanaPromesa(ventanaId);
		
		// Actualiza la grilla que lista los items
		$.when(promesa).then(function(){			
			
			$scope.habilitado = false;
			$scope.habilitadoDigitada = false;
			$scope.limpiar();
			$scope.limpiarItem();
			dataSourceItem.read();						
			dataSourceItemConfiguracion.removeAll();
			dataSourceItemProgramado.removeAll();
//			$scope.ItemConfiguracion = '';
		});
		
		//Deshabilita los campos
		$scope.habilitado = false;
		$scope.habilitadoDigitada = false;
	};
	
    //---------------------------------------------
    //----------------- SERVICIOS -----------------
    //---------------------------------------------
	
	transporte = function(Grid) {
		if (Grid === 'TipoItem')			
			url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=12081";  //ConsultarItems
		if (Grid === 'TipoItemConfiguracion')			
			url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=12083";  //ConsultaItemsConfiguracion
		if (Grid === 'TipoItemProgramado')			
			url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=13006";  //ConsultaItemsProgramado
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
	
	// Definir modelo Vista Item.
	ModeloItem = function() {
		return {
			model: {
				fields : {
					IdItem 		 	: { type: "number"},
					IdTipoItem 	 	: { type: "string"},
					NombreItem 	 	: { type: "string"},
					NombreCortoItem	: { type: "string"},
					Estado 			: { type: "string"},
	        	}
			}
		};
	}; 
	
	// Definir modelo Vista Item Configuracion.
	ModeloItemConfiguracion = function() {
		return {
			model: {
    	        	fields:{
    	        		IdConfiguracionItem 	: { type: "number"},
    	        		IdItem 					: { type: "number"},
    	        		Condicion 				: { type: "string"},    	        		
    	        		CuentaContable 			: { type: "string"},
    	        		Factor 					: { type: "number"},
    	        		Monto 					: { type: "number"},
    	        		SaldoDebeHaber 			: { type: "string"},
    	        		Operacion 				: { type: "string"},
    	        	} 
			}
		};
	};
	
	// Definir modelo Vista Item Configuracion.
	ModeloItemProgramado = function() {
		return {
			model: {
    	        	fields:{
    	        		IdConfiguracionItem 	: { type: "number"},
    	        		IdItem 					: { type: "number"},
    	        		Condicion 				: { type: "string"},    	        		
    	        		CuentaContable 			: { type: "string"},
    	        		Factor 					: { type: "number"},
    	        		Monto 					: { type: "number"},
    	        		SaldoDebeHaber 			: { type: "string"},
    	        		Operacion 				: { type: "string"},
    	        	} 
			}
		};
	};
	
    //---------------------------------------------
    //---------------- DATASOURCES ----------------
    //---------------------------------------------
	
	// Función definida para evento - Items
	dataSourceItem =  new bcs.DataSource({
		type 		: "odata", 
		definition 	: {
						serverFiltering 	: true, // Filtrado de datos en el servidor.
						serverPaging 		: true, // Paginado de los datos en el servidor.
						serverSorting 		: false, // Ordenamiento de los datos en el servidor.
						sortable 			: true,
						pageSize 			: 50,
						transport 			: transporte('TipoItem'),
						schema 				: ModeloItem(),
				  	}
	});			

	// Función definida para Item Configuracion
	
	var defItemConfiguracion = {
			type 		: "odata", 
			definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: true, // Ordenamiento de los datos en el servidor.
							pageSize 			: 50,
							transport 			: transporte('TipoItemConfiguracion'),
							schema 				: ModeloItemConfiguracion(),
							//filter              : [{ field: "IdItem", operator: "eq", value: 0 }]
						  }
	};
	
	var dataSourceItemConfiguracion = new bcs.DataSource(defItemConfiguracion);
	
	/*
	dataSourceItemConfiguracion = new bcs.DataSource({
		type 		: "odata", 
		definition 	: {
						serverFiltering 	: true, // Filtrado de datos en el servidor.
						serverPaging 		: true, // Paginado de los datos en el servidor.
						serverSorting 		: true, // Ordenamiento de los datos en el servidor.
						pageSize 			: 50,
						transport 			: transporte('TipoItemConfiguracion'),
						schema 				: ModeloItemConfiguracion(),
						//filter              : [{ field: "IdItem", operator: "eq", value: 0 }]
					  }
 
	});
	*/
	
	// Función definida para Item Programado
	dataSourceItemProgramado = new bcs.DataSource({
		type 		: "odata", 
		definition 	: {
						serverFiltering 	: true, // Filtrado de datos en el servidor.
						serverPaging 		: true, // Paginado de los datos en el servidor.
						serverSorting 		: true, // Ordenamiento de los datos en el servidor.
						pageSize 			: 50,
						transport 			: transporte('TipoItemProgramado'),
						schema 				: ModeloItemProgramado(),
						//filter              : [{ field: "IdItem", operator: "eq", value: 0 }]
					  }
 
	});
	
    //---------------------------------------------
    //------------------ COLUMNAS -----------------
    //---------------------------------------------

	// Columnas de la grilla izquierda, Items 
	columnasItem = function() {
		return [ 
		        {field: "IdItem"    , title: "UI_ITEM"		 , width: "35px", align: "center"}
		       ,{field: "NombreItem", title: "UI_NOMBRE_ITEM", width: "170px"}
		       ,{field: "IdTipoItem", title: "UI_TIPO_ITEM"	 , width: "40px", align: "center"}
				];
	};
	
	// Columnas de la grilla derecha, Items Configuracion 
	columnasItemConfiguracion = function() {
		return  [
		        {field: "IdItem"		, title: "UI_ITEM"					, width: "70px", hidden: true}
			   ,{field: "CuentaContable", title: "UI_CUENTA_CONTABLE"		, width: "70px"}
			   ,{field: "NombreCuenta"	, title: "UI_NOMBRE_CUENTA_CONTABLE", width: "100px"}
		       ,{field: "SaldoDebeHaber", title: "UI_PREFIJO_DEBE_HABER"	, width: "40px"}
		       ,{field: "Condicion"		, title: "UI_CONDICION"				, width: "40px"}  
		       ,{field: "Monto"			, title: "UI_MONTO"					, width: "50px"}
		       ,{field: "Factor"		, title: "UI_FACTOR"				, width: "40px"}
		       ,{field: "Operacion"		, title: "UI_OPERACION"				, width: "50px"}
	            ];
	};	
	
	// Columnas de la grilla derecha, Items Programado
	columnasItemProgramado = function() {
		return  [
		        {field: "IdProcesoFuncion"	, title: "UI_ITEM"		, width: "70px"}
			   ,{field: "Nombre"			, title: "UI_NOMBRE"	, width: "150px"}
			   ,{field: "Nemo"				, title: "UI_NEMO"		, width: "100px" 	, hidden: true}
		       ,{field: "Orden"				, title: "UI_NEMO"		, width: "40px" 	, hidden: true}
		       ,{field: "Estado"			, title: "UI_ESTADO"	, width: "40px" 	, hidden: true}  
	            ];
	};
	
	onChange  = function() { 
        var filters = new Array();
        	filtersAutori = new Array();
	        filters.push( { field: "IdItem", operator: "eq", value: $scope.seleccionItem });
    };
    
    
    // Configuración Items
	$scope.Item = {
			autoBind		: true,
			dataSource		: dataSourceItem,
			columns			: columnasItem(),		
			filterable		: true,
			sortable		: true,
			pageable		: true,
			height			: 545,
		    selectable 		: "row",
		    change			: onChange(),
	};
	
	// Configuración ItemConfiguracion
	$scope.ItemConfiguracion = {
			dataSource			: dataSourceItemConfiguracion,
			columns				: columnasItemConfiguracion(),		
			sortable			: true,
			pageable			: true,
			autoBind			: false,
			height				: 150,
		    selectable 			: "multiple row"
	};
	
	// Configuración ItemProgramado
	$scope.ItemProgramado = {
			dataSource			: dataSourceItemProgramado,
			columns				: columnasItemProgramado(),		
			sortable			: true,
			pageable			: true,
			autoBind			: false,
			height				: 130,
		    selectable 			: "row"
	};
	
    //---------------------------------------------
    //------------------ EVENTOS ------------------
    //---------------------------------------------
	
	// Evento que capta la selección de un item
	$scope.$watch('seleccionItem', function(newValue, oldValue) {  
		if (angular.equals(newValue,oldValue)) return;
		if (newValue!==false) {
			
			dataSourceItemConfiguracion.removeAll();
			dataSourceItemProgramado.removeAll();
			$scope.data.ChkMayorCero = false;
			$scope.data.ChkMenorCero =  false;
			$scope.data.ChkPermiteCero =  false;
			
			//$scope.filtro.NombreItem = $scope.seleccionItem.NombreItem;
			
			$scope.filtro = {item			: $scope.seleccionItem.NombreItem, 
							 tipo			: $scope.seleccionItem.IdTipoItem, 
							 nombreCorto	: $scope.seleccionItem.NombreCortoItem,
							 NombreItem 	: $scope.seleccionItem.NombreItem
							};
			
			if(newValue.IdTipoItem === 'CTB'){
				// Habilita los inputs  del grid derecho
				$scope.habilitado = true;
				$scope.habilitadoDigitada = false;
				$scope.inputDisabled = false;
				$scope.habilitadoProgramado = false;

				if(dataSourceItemConfiguracion !== undefined && $scope.seleccionItem.IdItem !== undefined && $scope.seleccionItem.IdItem !== '')
				{
					var filters = new Array();
					filters.push( { field: "IdItem", operator: "eq", value: $scope.seleccionItem.IdItem });
					dataSourceItemConfiguracion.filters(filters);
				}				
			}
			if(newValue.IdTipoItem === 'DIG'){
				$scope.habilitadoDigitada = true;
				$scope.habilitado = false;
				var ConsultaItemConfiguracion = new bcs.DataSource(defItemConfiguracion);
				ConsultaItemConfiguracion.query({ filter: [{ field: "IdItem" ,operator:"eq", value: $scope.seleccionItem.IdItem }] }).then(function(){
					if(ConsultaItemConfiguracion.view()[0] !== undefined){
						$scope.data.ChkMayorCero = (ConsultaItemConfiguracion.view()[0].MayorACero === 'S' ? true : false);
						$scope.data.ChkMenorCero = (ConsultaItemConfiguracion.view()[0].MenorACero === 'S' ? true : false);
						$scope.data.ChkPermiteCero = (ConsultaItemConfiguracion.view()[0].PermiteCero === 'S' ? true : false);	
					}
					else{
						$scope.data.ChkMayorCero = false;
						$scope.data.ChkMenorCero =  false;
						$scope.data.ChkPermiteCero =  false;
					}
				});
			}
			if(newValue.IdTipoItem === 'PRG'){
				$scope.habilitadoDigitada = false;
				$scope.habilitado = false;
				
				var ConsultaItemConfiguracion = new bcs.DataSource(defItemConfiguracion);
				var filters = new Array();
				ConsultaItemConfiguracion.query({ filter: [{ field: "IdItem" ,operator:"eq", value: $scope.seleccionItem.IdItem }] }).then(function(){
					if(ConsultaItemConfiguracion.view()[0] !== undefined){
						$scope.data.IdProcesoFuncion = ConsultaItemConfiguracion.view()[0].IdProcesoFuncion || '';
						$scope.data.NombreServicioProgramado = ConsultaItemConfiguracion.view()[0].NombreProcesoFuncion || ''; 					
						filters.push( { field: "IdProcesoFuncion", operator: "Ne", value: (ConsultaItemConfiguracion.view()[0].IdProcesoFuncion || 0) });		
					}
					else
					{
						$scope.data.IdProcesoFuncion = '';
						$scope.data.NombreServicioProgramado = '';
						filters.push( { field: "IdProcesoFuncion", operator: "Ne", value: 0 });
					}
					if(dataSourceItemProgramado !== undefined)
					{
						dataSourceItemProgramado.filters(filters);
					}
				});
			}

		}
	},true);
	$scope.$watch('seleccionItemProgramado', function(newValue, oldValue) {  
		if (angular.equals(newValue,oldValue)) return;
		if (newValue!==false) {
			$scope.habilitadoProgramado = true;
		}
	},true);
		
	
	/*
	// Evento que capta la selección de los checkbox cuando el item es digitado
	$scope.$watch('data.ChkMayorCero', function(newValue, oldValue) {  
		if (angular.equals(newValue,oldValue)) return;
		
		if(newValue !== false){
			$scope.data.ChkMayorCero = true;
			$scope.data.ChkIgualCero = false;
		}else{
			$scope.data.ChkMayorCero = false;
			$scope.data.ChkIgualCero = true;
		}

	},true);
	
	$scope.$watch('data.ChkIgualCero', function(newValue, oldValue) {  
		if (angular.equals(newValue,oldValue)) return;
		
		if(newValue !== false){
			$scope.data.ChkMayorCero = false;
			$scope.data.ChkIgualCero = true;
		}
		else{
			$scope.data.ChkMayorCero = true;
			$scope.data.ChkIgualCero = false;
		}
	},true);
	*/
	// Evento que capta la selección de los checkbox cuando el item es digitado
	/*
	$scope.$watchGroup(['data.ChkMayorCero','data.ChkIgualCero','data.ChkPermiteCero'],function(newValue,oldValue){
		if (angular.equals(newValue,oldValue)) return;
		
		if(newValue[0] !== oldValue[0] && newValue[0] === true && oldValue[0] === false){
			$scope.data.ChkMayorCero = true;
			$scope.data.ChkIgualCero = false;
			$scope.data.ChkPermiteCero = false;
		}
		else if(newValue[1] !== oldValue[1] && newValue[1] === true && oldValue[1] === false){
			$scope.data.ChkMayorCero = false;
			$scope.data.ChkIgualCero = true;
			$scope.data.ChkPermiteCero = false;
		}
		else if(newValue[2] !== oldValue[2] && newValue[2] === true && oldValue[2] === false){
			$scope.data.ChkMayorCero = false;
			$scope.data.ChkIgualCero = false;
			$scope.data.ChkPermiteCero = true;
		}
	},true);
	*/

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
		$scope.data.ChkMayorCero = false;
		$scope.data.ChkMenorCero =  false;
		$scope.data.ChkPermiteCero =  false;
	}
	
	$scope.limpiarItem = function(){
		$scope.filtro.item = '';
		$scope.filtro.nombreCorto = '';
		$scope.filtro.tipo = '';		
		$scope.data.ChkMayorCero = false;
		$scope.data.ChkMenorCero =  false;
		$scope.data.ChkPermiteCero =  false;
	}

    //---------------------------------------------
    //----------------- AGREGAR -------------------
    //---------------------------------------------
	$scope.agregarItem = function() {
		// Envio de parametros a la nueva ventana 
		var ventanaId = bcsComunes.ventanaAbrir("AgregarItem",{}, false, false, $scope.winID);
		var promesa = bcsComunes.ventanaPromesa(ventanaId);
		
		// Actualiza la grilla que lista los items
		$.when(promesa).then(function() {			
			
			$scope.habilitado = false;
			$scope.habilitadoDigitada = false;
			$scope.limpiar();
			$scope.limpiarItem();
			dataSourceItem.read();						
			dataSourceItemConfiguracion.removeAll();
			dataSourceItemProgramado.removeAll();
			
		});
		
		//Deshabilita los campos
		$scope.habilitado = false;
		$scope.habilitadoDigitada = false;
	};
	
	$scope.agregarItemConfiguracion = function() {
		
		var tablaCuentaContable	= [];
		tablaCuentaContable.push({
									IdConfiguracionItem : 0
								   ,IdItem		 		: $scope.seleccionItem.IdItem
								   ,IdProcesoFuncion	: $scope.seleccionItemProgramado.IdProcesoFuncion || 0
								   ,CuentaContable 		: $scope.cuentacontableSeleccionado.Cuenta || ''
							       ,SaldoDebeHaber 		: $scope.saldo.seleccionado.CodigoTipoSaldoDH || ''
							       ,Condicion			: $scope.condicion.seleccionado.CodigoCondicionCtb || ''
							       ,Monto				: $scope.data.kngMonto || 0 
							       ,Operacion      		: $scope.operacion.seleccionado.CodigoOperacionCtb || ''
							       ,Factor		 		: $scope.data.kngFactor || 0
							       ,MayorACero			: ($scope.data.ChkMayorCero === true ? 'S' : 'N') || ''
							       ,MenorACero			: ($scope.data.ChkMenorCero === true ? 'S' : 'N') || ''
							       ,PermiteCero			: ($scope.data.ChkPermiteCero === true ? 'S' : 'N') || ''
							       ,Estado				: 'VIG'
								});	
		
		var dataParametros = {
			p_IdEmpresa 				: $scope.SesionIdEmpresa,
			p_IdUsuario 				: $scope.SesionIdUsuario,
			p_IdModulo  				: 'MFW',
			p_UsaReglasNegocio 			: 'N',
			p_TipoOperacion				: 'I',
			p_tablaItemConfiguracion	: tablaCuentaContable
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
	
	$scope.eliminarCuentaContableItem = function() {
		
		var tablaCuentaContable = [];
		var registros = $scope.seleccionItemConfiguracion || [];
		$.each(registros,function(key,value){	
			tablaCuentaContable.push({
										IdConfiguracionItem : value.IdConfiguracionItem
									   ,IdItem		 		: value.IdItem
									   ,IdProcesoFuncion	: 0
									   ,CuentaContable 		: value.CuentaContable || ''
								       ,SaldoDebeHaber 		: value.SaldoDebeHaber || ''
								       ,Condicion			: value.Condicion || ''
								       ,Monto				: value.Monto || ''
								       ,Operacion      		: value.Operacion || ''
								       ,Factor		 		: value.Factor || ''
								       ,MayorACero			: value.MayorACero || ''
								       ,MenorACero			: value.MenorACero || ''
								       ,PermiteCero			: value.PermiteCero || ''
								       ,Estado				: value.Estado || ''
									});
		});
		
		var dataParametros = {
			p_IdEmpresa 				: $scope.SesionIdEmpresa,
			p_IdUsuario 				: $scope.SesionIdUsuario,
			p_IdModulo  				: 'MFW',
			p_UsaReglasNegocio 			: 'N',
			p_TipoOperacion				: 'E',
			p_tablaItemConfiguracion	: tablaCuentaContable
		};
		
		bcsComunes.ventanaOverlay(idVentana,false);
		bcsServiciosMesa.IngresarCuentaContableItem(dataParametros).$promise.then(function(respuesta){
   			
	   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
	   		var mensaje = '';
			//se valido la orden
			if (respuesta.P_OUT_ERRORESCONTADOR === 0) 
			{				
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("Cuenta Contable eliminada correctamente."),
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
				                 Descripcion : 'Existen problemas al eliminar Cuentas Contables.'
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
	//----------------- ELIMINAR ------------------
	//--------------------------------------------- 
	
	var EliminarItem = function(v_Dato){
		
    	var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=12079";
    	var r = $resource ("", {}, {
    		'urlConsulta': {method: "POST", url: urlConsulta}
    	});
    	var post = r.urlConsulta(v_Dato);
    	return post.$promise;
    };
    
	$scope.eliminarItem = function (){
		
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
						p_IdItem: $scope.seleccionItem.IdItem,
						p_IdModulo : 'ILS',
						p_IdTipoItem : $scope.seleccionItem.IdTipoItem,
						p_IdUsuario : $scope.SesionIdUsuario,
						p_Mantenedor : 'ITEM',					
						p_Nombre : '',
						p_NombreCorto : '',
						p_SimularOperacion : 'N',
						p_TipoOperacion : 'E',
						p_UsaReglasNegocio : 'S'
				};
				bcsComunes.log().info(JSON.stringify(parametrosSP));
				
				// Eliminar el Item
				EliminarItem(parametrosSP).then(
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
								$scope.limpiarItem();
								dataSourceItem.read();	
								dataSourceItemConfiguracion.removeAll();
								dataSourceItemProgramado.removeAll();
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
