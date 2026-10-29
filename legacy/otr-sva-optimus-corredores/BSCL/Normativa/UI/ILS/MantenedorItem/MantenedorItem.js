angular.module('ModuloNormativa').controller('MantenedorItemCtrl',
	function($scope,$uibModal,$translate,bcsComunes,bcsServiciosNormativa, $resource, $timeout, bcsAlerta,OPCIONES_BCSALERTA){
	
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	
	//Inicializacion de variables
	$scope.data = {};
	$scope.Item = "";	
	$scope.filtro = {item: '', tipo: '', nombreCorto:''};	
	$scope.habilitado = false;
	$scope.habilitadoDigitada = false;
	$scope.habilitadoCtaCtble = false;
	$scope.inputDisabled = true;
		
	var IdItemConfiguracion = 0;
	
	$scope.$watch('seleccionItemConfiguracion', function(newValue, oldValue) { 
		if($scope.seleccionItemConfiguracion!==undefined && $scope.seleccionItemConfiguracion!=='')
		{ 
			var datos = $scope.seleccionItemConfiguracion;
		  $scope.filtro.CuentaContable = datos[0].CuentaContable;
			$scope.filtro.Operacion = datos[0].Operacion === "+" ? 1 : (datos[0].Operacion === "-" ? 2 : '');
			$scope.filtro.Saldo = datos[0].SaldoDebeHaber == 'D' ? 1 : (datos[0].SaldoDebeHaber == 'H' ? 2 : (datos[0].SaldoDebeHaber == 'S' ? 3 :''));
			$scope.filtro.Condicion = datos[0].Condicion === ">" ? 1 : (datos[0].Condicion === "<" ? 2 : (datos[0].Condicion === "=" ? 3 : '' ));
			$scope.data.kngMonto = datos[0].Monto;
			$scope.data.kngFactor = datos[0].Factor;
			$scope.IdConfiguracionItem = datos[0].IdConfiguracionItem;
		}
	},true);
	
    //---------------------------------------------
    //----------------- MODIFICAR -----------------
    //---------------------------------------------
	
	$scope.modificarItem = function(){
		// Envio de parametros a la nueva ventana
		var ventanaId = bcsComunes.ventanaAbrir("AgregarItem",{Item: $scope.seleccionItem}, false, false,$scope.winID);
		var promesa = bcsComunes.ventanaPromesa(ventanaId);
		
		// Actualiza la grilla que lista los items
		promesa.then(function(){			
			
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
    	        		CuentaOptimus			: { type: "string"},
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
    	        		IdProcesoFuncion 		: { type: "number"},
    	        		Nombre 					: { type: "string"},
    	        		Nemo 					: { type: "string"},    	        		
    	        		Orden 					: { type: "string"},
    	        		Estado 					: { type: "string"},
    	        	} 
			}
		};
	};
	
    //---------------------------------------------
    //---------------- DATASOURCES ----------------
    //---------------------------------------------
	
	// Función definida para evento - Items
	dataSourceItem =  new bcs.newDataSource({
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
	
	var dataSourceItemConfiguracion = new bcs.newDataSource(defItemConfiguracion);
	
	
	// Función definida para Item Programado
	dataSourceItemProgramado = new bcs.newDataSource({
		type 		: "odata", 
		definition 	: {
						serverFiltering 	: true, // Filtrado de datos en el servidor.
						serverPaging 		: true, // Paginado de los datos en el servidor.
						serverSorting 		: true, // Ordenamiento de los datos en el servidor.
						pageSize 			: 50,
						transport 			: transporte('TipoItemProgramado'),
						schema 				: ModeloItemProgramado(),
						//filter              : [{ field: "Estado", operator: "eq", value: 'VIG' }]
					  }
 
	});
	
    //---------------------------------------------
    //------------------ COLUMNAS -----------------
    //---------------------------------------------

	// Columnas de la grilla izquierda, Items 
	columnasItem = function() {
		return [ 
		        {field: "IdItem"    , title: "UI_ITEM"		 , width: "35px"}
		       ,{field: "NombreItem", title: "UI_NOMBRE_ITEM", width: "170px"}
		       ,{field: "IdTipoItem", title: "UI_TIPO_ITEM"	 , width: "40px"}
				];
	};
	
	// Columnas de la grilla derecha, Items Configuracion 
	columnasItemConfiguracion = function() {
		return  [
		        {field: "IdItem"		, title: "UI_ITEM"					, width: "10px", hidden: true}
			   ,{field: "CuentaContable", title: "UI_CUENTA_CONTABLE"		, width: "60px"}
			   //,{field: "NombreCuenta"	, title: "UI_NOMBRE_CUENTA_CONTABLE", width: "100px"}
		       ,{field: "SaldoDebeHaber", title: "UI_PREFIJO_DEBE_HABER"	, width: "20px"}
		       ,{field: "Condicion"		, title: "UI_CONDICION"				, width: "40px"}  
		       ,{field: "Monto"			, title: "UI_MONTO"					, width: "50px"}
		       ,{field: "Factor"		, title: "UI_FACTOR"				, width: "30px"}
		       ,{field: "Operacion"		, title: "UI_OPERACION"				, width: "40px"}
	            ];
	};	
	
	// Columnas de la grilla derecha, Items Programado
	columnasItemProgramado = function() {
		return  [
		        {field: "IdProcesoFuncion"	, title: "UI_ITEM"		, width: "70px"}
			   ,{field: "Nombre"			, title: "UI_NOMBRE"	, width: "150px"}
			   ,{field: "Nemo"				, title: "UI_NEMO"		, width: "100px" 	, hidden: true}
		       ,{field: "Orden"				, title: "UI_ORDEN"		, width: "40px" 	, hidden: true}
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
			height			: 625,
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
		    selectable 			: "row",
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
			IdItemConfiguracion = 0;
			$scope.data.IdProcesoFuncion = '';
			$scope.data.NombreServicioProgramado = '';
			
				$scope.habilitado = true;
				$scope.habilitadoItem = true;

				$scope.filtro = {item			: $scope.seleccionItem.NombreItem, 
							 tipo			: $scope.seleccionItem.IdTipoItem, 
							 nombreCorto	: $scope.seleccionItem.NombreCortoItem,
							 NombreItem 	: $scope.seleccionItem.NombreItem
							};			
			
			if(newValue.IdTipoItem !== ''){
				// Habilita los inputs  del grid derecho
				$scope.habilitadoDigitada = true;
				$scope.habilitadoCtaCtble = true;
				$scope.inputDisabled = true;
				$scope.habilitadoProgramado = true;
				
				//Cuenta Contable
				dataSourceItemConfiguracion.query({ filter: [{ field: "IdItem" ,operator:"eq", value: $scope.seleccionItem.IdItem},{ field: "TipoItem" ,operator:"eq", value: "C"}
				                                             //{ field: "IdTipoItem" ,operator:"eq", value: "CTB" }
															] }).then(function(){
					if(dataSourceItemConfiguracion.view().length > 0 && $scope.seleccionItem.IdItem !== undefined && $scope.seleccionItem.IdItem !== '')
					{	
						$scope.filtro.CuentaContable = dataSourceItemConfiguracion.view()[0].CuentaContable;
						$scope.filtro.Operacion = dataSourceItemConfiguracion.view()[0].Operacion === "+" ? 1 : (dataSourceItemConfiguracion.view()[0].Operacion === "-" ? 2 : '');
						$scope.filtro.Saldo = dataSourceItemConfiguracion.view()[0].SaldoDebeHaber == 'D' ? 1 : (dataSourceItemConfiguracion.view()[0].SaldoDebeHaber == 'H' ? 2 : (dataSourceItemConfiguracion.view()[0].SaldoDebeHaber == 'S' ? 3 :''));
						$scope.filtro.Condicion = dataSourceItemConfiguracion.view()[0].Condicion === ">" ? 1 : (dataSourceItemConfiguracion.view()[0].Condicion === "<" ? 2 : (dataSourceItemConfiguracion.view()[0].Condicion === "=" ? 3 : '' ));
						$scope.data.kngMonto = dataSourceItemConfiguracion.view()[0].Monto;
						$scope.data.kngFactor = dataSourceItemConfiguracion.view()[0].Factor;
						dataSourceItemConfiguracion.page(1);
					}else{
						$scope.filtro.CuentaContable = 0;
						$scope.filtro.Operacion = 0;
						$scope.filtro.Saldo = 0;
						$scope.filtro.Condicion = 0;
						$scope.data.kngMonto = undefined;
						$scope.data.kngFactor = undefined;
					}	
				});
				
				//Digitado
				var ConsultaItemDigitadoConfiguracion = new bcs.newDataSource(dataSourceItemConfiguracion);
				var filters = new Array();
				ConsultaItemDigitadoConfiguracion.query({ filter: [{ field: "IdItem" ,operator:"eq", value: $scope.seleccionItem.IdItem },{ field: "TipoItem" ,operator:"eq", value: "D"}] }).then(function(){
					if(ConsultaItemDigitadoConfiguracion.view()[0] !== undefined){
						$scope.data.ChkMayorCero = (ConsultaItemDigitadoConfiguracion.view()[0].MayorACero === 'S' ? true : false);
						$scope.data.ChkMenorCero = (ConsultaItemDigitadoConfiguracion.view()[0].MenorACero === 'S' ? true : false);
						$scope.data.ChkPermiteCero = (ConsultaItemDigitadoConfiguracion.view()[0].PermiteCero === 'S' ? true : false);
					}
					else{
						$scope.data.ChkMayorCero = false;
						$scope.data.ChkMenorCero =  false;
						$scope.data.ChkPermiteCero =  false;
					}
				});
				$scope.filtro.CuentaContable = 0;
				$scope.filtro.Operacion = 0;
				$scope.filtro.Saldo = 0;
				$scope.filtro.Condicion = 0;
				$scope.data.kngMonto = undefined;
				$scope.data.kngFactor = undefined;
				

				//Programado
				var ConsultaItemConfiguracion = new bcs.newDataSource(defItemConfiguracion);
				var filters = new Array();
				ConsultaItemConfiguracion.query({ filter: [{ field: "IdItem" ,operator:"eq", value: $scope.seleccionItem.IdItem},{ field: "TipoItem" ,operator:"eq", value: "P"},
															] }).then(function(){
					if(ConsultaItemConfiguracion.view()[0] !== undefined){
						$scope.data.IdProcesoFuncion = ConsultaItemConfiguracion.view()[0].IdProcesoFuncion || '';
						$scope.data.NombreServicioProgramado = ConsultaItemConfiguracion.view()[0].NombreProcesoFuncion || ''; 					
						filters.push( { field: "IdProcesoFuncion", operator: "Ne", value: (ConsultaItemConfiguracion.view()[0].IdProcesoFuncion || 0) });
						filters.push( { field: "Estado", operator: "eq", value: 'VIG' });
					}
					else
					{
						$scope.data.IdProcesoFuncion = '';
						$scope.data.NombreServicioProgramado = '';
						filters.push( { field: "IdProcesoFuncion", operator: "Ne", value: 0 });
						filters.push( { field: "Estado", operator: "eq", value: 'VIG' });
					}
					if(dataSourceItemProgramado !== undefined)
					{
						dataSourceItemProgramado.filter(filters);
					}
					
				});
				$scope.filtro.CuentaContable = 0;
				$scope.filtro.Operacion = 0;
				$scope.filtro.Saldo = 0;
				$scope.filtro.Condicion = 0;
				$scope.data.kngMonto = undefined;
				$scope.data.kngFactor = undefined;
			}
		}
	},true);
	

	
	$scope.$watch('seleccionItemProgramado', function(newValue, oldValue) {  
		if (angular.equals(newValue,oldValue)) return;
		if (newValue!==false) {
			$scope.habilitadoProgramado = true;
		}
	},true);

	//---------------------------------------------
	//----------------- LIMPIAR ------------------
	//---------------------------------------------
	
	// Función para limpiar los campos de la búsqueda 
	$scope.limpiar = function(){
		
		$scope.BuscarCuentaContable = undefined;
		$scope.filtro.Operacion = 0;
		$scope.filtro.Saldo = 0;
		$scope.filtro.Condicion = 0;
		$scope.data.kngMonto = undefined;
		$scope.data.kngFactor = undefined;
		$scope.data.ChkMayorCero = false;
		$scope.data.ChkMenorCero =  false;
		$scope.data.ChkPermiteCero =  false;
		$scope.data.IdProcesoFuncion = '';
		$scope.data.NombreServicioProgramado = '';
	}
	
	$scope.limpiarItem = function(){
		$scope.filtro.item = '';
		$scope.filtro.nombreCorto = '';
		$scope.filtro.tipo = '';		
		$scope.data.ChkMayorCero = false;
		$scope.data.ChkMenorCero =  false;
		$scope.data.ChkPermiteCero =  false;
		$scope.data.IdProcesoFuncion = '';
		$scope.data.NombreServicioProgramado = '';
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
	
	$scope.agregarItemConfiguracion = function(TipoOperacion) {
		$scope.data.TipoOperacion = TipoOperacion;
	}
	
	document.getElementById('Grabar').onclick = function() {
			if(event.pointerType === 'mouse' 
			&& event.srcElement.innerText == "GRABAR" ){
					
						if ($scope.seleccionItemProgramado !== undefined){
							IdProcesoFuncion = $scope.seleccionItemProgramado.IdProcesoFuncion;
						}
							else{
								IdProcesoFuncion = 0;
								}		
						
							
				//CELCONDOR-581		
						var tablaCuentaContable	= [];
						tablaCuentaContable.push({
													IdConfiguracionItem : $scope.IdConfiguracionItem // IdItemConfiguracion 
												   ,IdItem		 		: $scope.seleccionItem.IdItem
												   ,IdProcesoFuncion	: IdProcesoFuncion 
												   ,CuentaContable 		: $scope.cuentacontableSeleccionado.CuentaContabilidad || ''
												   ,CuentaOptimus		: $scope.cuentacontableSeleccionado.Cuenta || ''
											       ,SaldoDebeHaber 		: $scope.saldoSeleccionado.CodigoTipoSaldoDH || ''
											       ,Condicion			: $scope.condicionSeleccionado.CodigoCondicionCtb || ''
											       ,Monto				: $scope.data.kngMonto || 0 
											       ,Operacion      		: $scope.operacionSeleccionado.CodigoOperacionCtb || ''
											       ,Factor		 		: $scope.data.kngFactor || 0
											       ,MayorACero			: ($scope.data.ChkMayorCero === true ? 'S' : 'N') || ''
											       ,MenorACero			: ($scope.data.ChkMenorCero === true ? 'S' : 'N') || ''
											       ,PermiteCero			: ($scope.data.ChkPermiteCero === true ? 'S' : 'N') || ''
											       ,Estado				: 'VIG'
											       ,TipoItem			: $scope.data.TipoOperacion
												});	
						
						var dataParametros = {
							p_IdEmpresa 				: $scope.SesionIdEmpresa,
							p_IdUsuario 				: $scope.SesionIdUsuario,
							p_IdModulo  				: 'MFW',
							p_UsaReglasNegocio 			: 'N',
							p_tablaItemConfiguracion	: tablaCuentaContable
						};
						bcsComunes.ventanaOverlay($scope.winID,false);
						var mensaje = '';
				   		var error = '';
						//se valido la orden
				   		if($scope.seleccionItem.IdTipoItem === 'CTB')
				   		{
				   			mensaje = 'UI_CTA_CONTABLE_INGRESAR_EXITO';
				   			error = 'UI_CTA_CONTABLE_INGRESAR_ERROR';
						}
				   		else if($scope.seleccionItem.IdTipoItem === 'PRG')
						{
				   			mensaje = 'UI_SERV_PROGRAMADO_INGRESAR_EXITO';
				   			error = 'UI_SERV_PROGRAMADO_INGRESAR_ERROR';
						}
				   		else
						{
							mensaje = 'UI_PROC_DIGITACION_INGRESAR_EXITO';
							error = 'UI_PROC_DIGITACION_INGRESAR_ERROR';
						}
						var promesa1 = bcsServiciosNormativa.MantenedorItemConfiguracion(dataParametros).$promise.then(function(respuesta){
				   			
					   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
							if (respuesta.P_OUT_ERRORESCONTADOR === 0) 
							{				
								bcsComunes.ventanaMsg({
									mensaje:$translate.instant(mensaje),
									conflictos:util.parseServerResponse(respuesta)
								},$scope.winID);
							}
							else
						    {
								bcsComunes.setConflictos(util.parseServerResponse(respuesta));
								bcsComunes.ventanaMsg({
									mensaje: "Error",
									conflictos: util.parseServerResponse(respuesta),
									errores : [ {Codigo : 'COD',Descripcion : $translate.instant(error)} ]
								},$scope.winID);
								
						    }
							bcsComunes.ventanaOverlay($scope.winID,false);
							
							if($scope.seleccionItem.IdTipoItem === 'DIG'){
								dataSourceItemConfiguracion.query({ filter: [{ field: "IdItem" ,operator:"eq", value: $scope.seleccionItem.IdItem }, { field: "TipoItem" ,operator:"eq", value: "C"}
								                                            // { field: "IdTipoItem" ,operator:"eq", value: "CTB" }
																		] }).then(function(){
								});
							}
							
							if($scope.seleccionItem.IdTipoItem === 'CTB'){
								var filters = new Array();
								filters.push( { field: "IdItem", operator: "eq", value: $scope.seleccionItem.IdItem },{ field: "TipoItem" ,operator:"eq", value: "C"});
								//filters.push( { field: "IdTipoItem", operator: "eq", value: "CTB" });
								//dataSourceItemConfiguracion.filters(filters);
								$scope.BuscarCuentaContable = undefined;
								$scope.filtro.CuentaContable = 0;
								$scope.filtro.Operacion = 0;
								$scope.filtro.Saldo = 0;
								$scope.filtro.Condicion = 0;
								$scope.data.kngMonto = undefined;
								$scope.data.kngFactor = undefined;
							}
							
							if($scope.seleccionItem.IdTipoItem === 'PRG'){
								var filter = new Array();
								filter.push( { field: "IdProcesoFuncion", operator: "Ne", value: IdProcesoFuncion });
								filter.push( { field: "Estado", operator: "eq", value: 'VIG' });
								//dataSourceItemProgramado.filters(filter);
								$scope.data.IdProcesoFuncion = IdProcesoFuncion;
								//console.log("aca",$scope.seleccionItemProgramado);
								//$scope.data.NombreServicioProgramado = $scope.seleccionItemProgramado.Nombre;
							}
				   		},function(respuesta){
				   			//ERROR AL CREAR LA ORDEN
							bcsComunes.ventanaMsg({
								mensaje:$translate.instant(error),
								conflictos:util.parseServerResponse(respuesta)
							},$scope.winID);
				   		}).then(function() {			

							recargarGrilla();
						});
						
				   			  
				  }
	}
	
	var recargarGrilla = function(){
		dataSourceItemConfiguracion.removeAll();
		dataSourceItemProgramado.removeAll();
		$scope.data.ChkMayorCero = false;
		$scope.data.ChkMenorCero =  false;
		$scope.data.ChkPermiteCero =  false;
		IdItemConfiguracion = 0;
		$scope.data.IdProcesoFuncion = '';
		$scope.data.NombreServicioProgramado = '';
		
			$scope.habilitado = true;
			$scope.habilitadoItem = true;

			$scope.filtro = {item			: $scope.seleccionItem.NombreItem, 
						 tipo			: $scope.seleccionItem.IdTipoItem, 
						 nombreCorto	: $scope.seleccionItem.NombreCortoItem,
						 NombreItem 	: $scope.seleccionItem.NombreItem
						};			
		let newValue1 = $scope.seleccionItem;
		
		if(newValue1.IdTipoItem !== ''){
			// Habilita los inputs  del grid derecho
			$scope.habilitadoDigitada = true;
			$scope.habilitadoCtaCtble = true;
			$scope.inputDisabled = true;
			$scope.habilitadoProgramado = true;
			
			//Cuenta Contable
			dataSourceItemConfiguracion.query({ filter: [{ field: "IdItem" ,operator:"eq", value: $scope.seleccionItem.IdItem},{ field: "TipoItem" ,operator:"eq", value: "C"}
			                                             //{ field: "IdTipoItem" ,operator:"eq", value: "CTB" }
														] }).then(function(){
				if(dataSourceItemConfiguracion.view().length > 0 && $scope.seleccionItem.IdItem !== undefined && $scope.seleccionItem.IdItem !== '')
				{	
					$scope.filtro.CuentaContable = dataSourceItemConfiguracion.view()[0].CuentaContable;
					$scope.filtro.Operacion = dataSourceItemConfiguracion.view()[0].Operacion === "+" ? 1 : (dataSourceItemConfiguracion.view()[0].Operacion === "-" ? 2 : '');
					$scope.filtro.Saldo = dataSourceItemConfiguracion.view()[0].SaldoDebeHaber == 'D' ? 1 : (dataSourceItemConfiguracion.view()[0].SaldoDebeHaber == 'H' ? 2 : (dataSourceItemConfiguracion.view()[0].SaldoDebeHaber == 'S' ? 3 :''));
					$scope.filtro.Condicion = dataSourceItemConfiguracion.view()[0].Condicion === ">" ? 1 : (dataSourceItemConfiguracion.view()[0].Condicion === "<" ? 2 : (dataSourceItemConfiguracion.view()[0].Condicion === "=" ? 3 : '' ));
					$scope.data.kngMonto = dataSourceItemConfiguracion.view()[0].Monto;
					$scope.data.kngFactor = dataSourceItemConfiguracion.view()[0].Factor;
					dataSourceItemConfiguracion.page(1);
				}else{
					$scope.filtro.CuentaContable = 0;
					$scope.filtro.Operacion = 0;
					$scope.filtro.Saldo = 0;
					$scope.filtro.Condicion = 0;
					$scope.data.kngMonto = undefined;
					$scope.data.kngFactor = undefined;
				}	
			});
			
			//Digitado
			var ConsultaItemDigitadoConfiguracion = new bcs.newDataSource(dataSourceItemConfiguracion);
			var filters = new Array();
			ConsultaItemDigitadoConfiguracion.query({ filter: [{ field: "IdItem" ,operator:"eq", value: $scope.seleccionItem.IdItem },{ field: "TipoItem" ,operator:"eq", value: "D"}] }).then(function(){
				if(ConsultaItemDigitadoConfiguracion.view()[0] !== undefined){
					$scope.data.ChkMayorCero = (ConsultaItemDigitadoConfiguracion.view()[0].MayorACero === 'S' ? true : false);
					$scope.data.ChkMenorCero = (ConsultaItemDigitadoConfiguracion.view()[0].MenorACero === 'S' ? true : false);
					$scope.data.ChkPermiteCero = (ConsultaItemDigitadoConfiguracion.view()[0].PermiteCero === 'S' ? true : false);
				}
				else{
					$scope.data.ChkMayorCero = false;
					$scope.data.ChkMenorCero =  false;
					$scope.data.ChkPermiteCero =  false;
				}
			});
			$scope.filtro.CuentaContable = 0;
			$scope.filtro.Operacion = 0;
			$scope.filtro.Saldo = 0;
			$scope.filtro.Condicion = 0;
			$scope.data.kngMonto = undefined;
			$scope.data.kngFactor = undefined;
			

			//Programado
			var ConsultaItemConfiguracion = new bcs.newDataSource(defItemConfiguracion);
			var filters = new Array();
			ConsultaItemConfiguracion.query({ filter: [{ field: "IdItem" ,operator:"eq", value: $scope.seleccionItem.IdItem},{ field: "TipoItem" ,operator:"eq", value: "P"},
														] }).then(function(){
				if(ConsultaItemConfiguracion.view()[0] !== undefined){
					$scope.data.IdProcesoFuncion = ConsultaItemConfiguracion.view()[0].IdProcesoFuncion || '';
					$scope.data.NombreServicioProgramado = ConsultaItemConfiguracion.view()[0].NombreProcesoFuncion || ''; 					
					filters.push( { field: "IdProcesoFuncion", operator: "Ne", value: (ConsultaItemConfiguracion.view()[0].IdProcesoFuncion || 0) });
					filters.push( { field: "Estado", operator: "eq", value: 'VIG' });
				}
				else
				{
					$scope.data.IdProcesoFuncion = '';
					$scope.data.NombreServicioProgramado = '';
					filters.push( { field: "IdProcesoFuncion", operator: "Ne", value: 0 });
					filters.push( { field: "Estado", operator: "eq", value: 'VIG' });
				}
				if(dataSourceItemProgramado !== undefined)
				{
					dataSourceItemProgramado.filter(filters);
				}
				
			});
			$scope.filtro.CuentaContable = 0;
			$scope.filtro.Operacion = 0;
			$scope.filtro.Saldo = 0;
			$scope.filtro.Condicion = 0;
			$scope.data.kngMonto = undefined;
			$scope.data.kngFactor = undefined;
		}
		
	}
	

	
	
	$scope.agregarItemConfiguracionDigitadoProgramado = function(TipoOperacion) {

		if ($scope.seleccionItemProgramado !== undefined){
			IdProcesoFuncion = $scope.seleccionItemProgramado.IdProcesoFuncion;
		}
			else{
				IdProcesoFuncion = 0;
				}		
		
			
		var tablaCuentaContable	= [];
		tablaCuentaContable.push({
									IdConfiguracionItem : $scope.IdConfiguracionItem // IdItemConfiguracion 
								   ,IdItem		 		: $scope.seleccionItem.IdItem
								   ,IdProcesoFuncion	: IdProcesoFuncion 
								   ,CuentaContable 		: $scope.cuentacontableSeleccionado.CuentaContabilidad || ''
								   ,CuentaOptimus		: $scope.cuentacontableSeleccionado.Cuenta || ''
							       ,SaldoDebeHaber 		: $scope.saldoSeleccionado.CodigoTipoSaldoDH || ''
							       ,Condicion			: $scope.condicionSeleccionado.CodigoCondicionCtb || ''
							       ,Monto				: $scope.data.kngMonto || 0 
							       ,Operacion      		: $scope.operacionSeleccionado.CodigoOperacionCtb || ''
							       ,Factor		 		: $scope.data.kngFactor || 0
							       ,MayorACero			: ($scope.data.ChkMayorCero === true ? 'S' : 'N') || ''
							       ,MenorACero			: ($scope.data.ChkMenorCero === true ? 'S' : 'N') || ''
							       ,PermiteCero			: ($scope.data.ChkPermiteCero === true ? 'S' : 'N') || ''
							       ,Estado				: 'VIG'
							       ,TipoItem			: TipoOperacion
								});	
		
		var dataParametros = {
			p_IdEmpresa 				: $scope.SesionIdEmpresa,
			p_IdUsuario 				: $scope.SesionIdUsuario,
			p_IdModulo  				: 'MFW',
			p_UsaReglasNegocio 			: 'N',
			p_tablaItemConfiguracion	: tablaCuentaContable
		};
		bcsComunes.ventanaOverlay($scope.winID,false);
		var mensaje = '';
   		var error = '';
		//se valido la orden
   		if($scope.seleccionItem.IdTipoItem === 'CTB')
   		{
   			mensaje = 'UI_CTA_CONTABLE_INGRESAR_EXITO';
   			error = 'UI_CTA_CONTABLE_INGRESAR_ERROR';
		}
   		else if($scope.seleccionItem.IdTipoItem === 'PRG')
		{
   			mensaje = 'UI_SERV_PROGRAMADO_INGRESAR_EXITO';
   			error = 'UI_SERV_PROGRAMADO_INGRESAR_ERROR';
		}
   		else
		{
			mensaje = 'UI_PROC_DIGITACION_INGRESAR_EXITO';
			error = 'UI_PROC_DIGITACION_INGRESAR_ERROR';
		}
		bcsServiciosNormativa.MantenedorItemConfiguracion(dataParametros).$promise.then(function(respuesta){
   			
	   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			if (respuesta.P_OUT_ERRORESCONTADOR === 0) 
			{				
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant(mensaje),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
			}
			else
		    {
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				bcsComunes.ventanaMsg({
					mensaje: "Error",
					conflictos: util.parseServerResponse(respuesta),
					errores : [ {Codigo : 'COD',Descripcion : $translate.instant(error)} ]
				},$scope.winID);
				
		    }
			bcsComunes.ventanaOverlay($scope.winID,false);
			
			if($scope.seleccionItem.IdTipoItem === 'DIG'){
				dataSourceItemConfiguracion.query({ filter: [{ field: "IdItem" ,operator:"eq", value: $scope.seleccionItem.IdItem }, { field: "TipoItem" ,operator:"eq", value: "C"}
				                                            // { field: "IdTipoItem" ,operator:"eq", value: "CTB" }
														] }).then(function(){
				});
			}
			
			if($scope.seleccionItem.IdTipoItem === 'CTB'){
				var filters = new Array();
				filters.push( { field: "IdItem", operator: "eq", value: $scope.seleccionItem.IdItem },{ field: "TipoItem" ,operator:"eq", value: "C"});
				//filters.push( { field: "IdTipoItem", operator: "eq", value: "CTB" });
				try{dataSourceItemConfiguracion.filters(filters);}catch(e){}
				$scope.BuscarCuentaContable = undefined;
				$scope.filtro.CuentaContable = 0;
				$scope.filtro.Operacion = 0;
				$scope.filtro.Saldo = 0;
				$scope.filtro.Condicion = 0;
				$scope.data.kngMonto = undefined;
				$scope.data.kngFactor = undefined;
			}
			
			if($scope.seleccionItem.IdTipoItem === 'PRG'){
				var filter = new Array();
				filter.push( { field: "IdProcesoFuncion", operator: "Ne", value: IdProcesoFuncion });
				filter.push( { field: "Estado", operator: "eq", value: 'VIG' });
				try{
					dataSourceItemProgramado.filters(filter);
				}catch(e){}
				$scope.data.IdProcesoFuncion = IdProcesoFuncion;
				$scope.data.NombreServicioProgramado = $scope.seleccionItemProgramado.Nombre;
			}
   		},function(respuesta){
   			//ERROR AL CREAR LA ORDEN
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant(error),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
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
									   ,CuentaOptimus		: value.Cuenta || ''
								       ,SaldoDebeHaber 		: value.SaldoDebeHaber || ''
								       ,Condicion			: value.Condicion || ''
								       ,Monto				: value.Monto || 0
								       ,Operacion      		: value.Operacion || ''
								       ,Factor		 		: value.Factor || 0
								       ,MayorACero			: value.MayorACero || ''
								       ,MenorACero			: value.MenorACero || ''
								       ,PermiteCero			: value.PermiteCero || ''
								       ,Estado				: value.Estado || ''
								       ,TipoItem			: 'E'
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
		
		bcsComunes.ventanaOverlay($scope.winID,false);
		bcsServiciosNormativa.MantenedorItemConfiguracion(dataParametros).$promise.then(function(respuesta){
   			
	   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
	   		var mensaje = '';
			//se valido la orden
			if (respuesta.P_OUT_ERRORESCONTADOR === 0) 
			{	
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant('UI_CTA_CONTABLE_ELIMINAR_EXITO'),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
			}
			else
		    {
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				bcsComunes.ventanaMsg({
					mensaje: "Error",
					conflictos: util.parseServerResponse(respuesta),
					errores : [ {Codigo : 'COD',Descripcion : $translate.instant('UI_CTA_CONTABLE_ELIMINAR_ERROR')} ]
				},$scope.winID);
				
		    }
			bcsComunes.ventanaOverlay($scope.winID,false);
			var filters = new Array();
			filters.push( { field: "IdItem", operator: "eq", value: $scope.seleccionItem.IdItem });
			filters.push( { field: "IdTipoItem", operator: "eq", value: "CTB" });
			//dataSourceItemConfiguracion.filters(filters);
			
   		},function(respuesta){
   			//ERROR AL CREAR LA ORDEN
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
   		}).then(function() {			

			recargarGrilla();
		});;	
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
								bcsComunes.msg("warning", "Advertencia", $translate.instant('UI_OPERACION_CON_ADVERTENCIAS'));
								
							}else if (result.P_OUT_ERRORESCONTADOR){
								//bcsComunes.msg("error", "Error", "La operacion tiene errores, " + result.P_OUT_ERRORESDETALLE[0].Descripcion);
								bcsComunes.setConflictos(util.parseServerResponse(result));
								bcsComunes.ventanaMsg({
									mensaje: "Error",
									conflictos: util.parseServerResponse(result),
									errores : [ {Codigo : 'COD',Descripcion : result.P_OUT_ERRORESDETALLE[0].Descripcion}]
								},$scope.winID);
								var i = 0;
								for(i; i<result.P_OUT_ERRORESCONTADOR; i++)
								{
									bcsComunes.log().info("Error: " + result.P_OUT_ERRORESDETALLE[i].Descripcion);       
								}    
							}else {
								//FIXME CAMBIAR MENSAJE (Traduccion)
								bcsComunes.ventanaMsg({
									mensaje:$translate.instant('UI_ITEM_ELIMINAR_EXITO'),
									conflictos:util.parseServerResponse(result)
								},$scope.winID);
								
								bcsComunes.ventanaOverlay($scope.winID,false);
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
							bcsComunes.ventanaMsg({
								mensaje:$translate.instant('UI_ITEM_ELIMINAR_ERROR'),
								conflictos:util.parseServerResponse(result)
							},$scope.winID);
							bcsComunes.ventanaOverlay($scope.winID, false);
						});
			}
		});	
		return;				
	};

});
