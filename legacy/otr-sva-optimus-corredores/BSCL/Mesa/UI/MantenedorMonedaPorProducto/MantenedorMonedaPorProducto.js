(function() {
	angular.module('ModuloMesa').controller('MantenedorPorProducto',
	function($scope,$translate,$resource,$timeout,bcsComunes,bcsServiciosMesa)
	{		
		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var parametrosVentana = bcsComunes.ventanaParams(idVentana);

		$scope.data = {};
		
		var ingSpot = this;
		
		ingSpot.datosAntes = {};
		ingSpot.datosDespues = {};
		ingSpot.datosAntes = angular.copy(ingSpot.datosDespues);
		 
		//Configurar al principio
		 
		$scope.data.kngFecha = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.MargenPunta = $scope.data.kngMargenPunta || 0;
		ingSpot.data = {};

		
		$scope.data.oConfiguracionTipoProducto ={
			filtros : {
				IdTipoMesa : { "field"   : "IdTipoMesa","operator": "eq", "value" : "MDIN" }
			}
		};
		
		
		//Combobox Tipo Instrumento
		$scope.data.oConfiguracionTipoInstrumento={
			filtros : {
				IdTipoInstrumento : { "field"  : "IdTipoInstrumento","operator": "eq", "value" : "" }
		    }
		};
		
		
		$scope.data.oConfiguracionMonedaPrincipal={

			filtros : {
				IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value" : "" },
				Nemotecnico : { "field"   : "Nemotecnico","operator": "eq", "value" : '' },
				OrdenEnPar : { "field"   : "OrdenEnPar","operator": "eq", "value" : 1 }
				
			}
		};
		
		$scope.data.oConfiguracionMonedaSecundaria={

				filtros : {
					IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value" : "" },
					Nemotecnico : { "field"   : "Nemotecnico","operator": "eq", "value" : '' },
					OrdenEnPar : { "field"   : "OrdenEnPar","operator": "eq", "value" : 2 }
					
				}
			};
		

		//---------------------------------------------
		$scope.data.filtrarMonedaPorProducto = function()
		{
			var filtroMonedaPorProducto = new Array();
			if($scope.data.oConfiguracionTipoProducto.seleccionado.IdTipoProductoMesa != undefined)
    			filtroMonedaPorProducto.push({ field: "IdTipoProducto", operator: "eq", value: $scope.data.oConfiguracionTipoProducto.seleccionado.IdTipoProductoMesa });
    		if($scope.data.oConfiguracionTipoInstrumento.seleccionado.Nemotecnico != undefined && $scope.data.oConfiguracionTipoInstrumento.seleccionado.Nemotecnico != 0)
    			filtroMonedaPorProducto.push({ field: "Nemotecnico", operator: "eq", value: $scope.data.oConfiguracionTipoInstrumento.seleccionado.Nemotecnico });
    		if($scope.data.oConfiguracionMonedaPrincipal.idActual != 0)
    			filtroMonedaPorProducto.push({ field: "IdMonedaPrincipal", operator: "eq", value: $scope.data.oConfiguracionMonedaPrincipal.idActual });
    		if($scope.data.oConfiguracionMonedaSecundaria.idActual != 0)
    			filtroMonedaPorProducto.push({ field: "IdMonedaSecundaria", operator: "eq", value: $scope.data.oConfiguracionMonedaSecundaria.idActual });
            dsMonedaPorProducto.filters(filtroMonedaPorProducto);
            
            // Ahora llenar el combo-box con los datos???
            
		};
		
	
		var successIngreso = function(respuesta)
		{
			if (respuesta.P_OUT_ERRORESCONTADOR == 0) 
			{		
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_OPERACION_INGRESO_EXITO"),
					conflictos:util.parseServerResponse(respuesta)
				},idVentana);
				
				dsMonedaPorProducto2 = new bcs.DataSource(defDataSource());
				dsMonedaPorProducto2.read().then(function(){
					
					var filtroMonedaPorProducto = new Array();
		    		filtroMonedaPorProducto.push({ field: "IdTipoProducto", operator: "eq", value: $scope.data.oConfiguracionTipoProducto.idActual });
		    		if($scope.data.oConfiguracionTipoInstrumento.seleccionado.Nemotecnico != undefined && $scope.data.oConfiguracionTipoInstrumento.seleccionado.Nemotecnico != 0)
		    			filtroMonedaPorProducto.push({ field: "Nemotecnico", operator: "eq", value: $scope.data.oConfiguracionTipoInstrumento.seleccionado.Nemotecnico });
		    		if($scope.data.oConfiguracionMonedaPrincipal.idActual != 0)
		    			filtroMonedaPorProducto.push({ field: "IdMonedaPrincipal", operator: "eq", value: $scope.data.oConfiguracionMonedaPrincipal.idActual });
		    		if($scope.data.oConfiguracionMonedaSecundaria.idActual != 0)
		    			filtroMonedaPorProducto.push({ field: "IdMonedaSecundaria", operator: "eq", value: $scope.data.oConfiguracionMonedaSecundaria.idActual });
		            dsMonedaPorProducto.filters(filtroMonedaPorProducto);
		            $scope.confMonedaProducto.dataSource = dsMonedaPorProducto2;

//		    		var filtroMonedaPorProducto = new Array();
//		    		filtroMonedaPorProducto.push({ field: "IdTipoProducto", operator: "eq", value: parametrosVentana.idTipoProducto });
//		    		if(parametrosVentana.idTipoProducto === 'MEFW')
//		    			filtroMonedaPorProducto.push({ field: "IdTipoInstrumento", operator: "eq", value: 'FWD'});
//		    		if(parametrosVentana.idTipoProducto === 'MEME')
//		    			filtroMonedaPorProducto.push({ field: "IdTipoInstrumento", operator: "eq", value: 'SPT'});
//		            dsMonedaPorProducto.filters(filtroMonedaPorProducto);
//		            $scope.confMonedaProducto.dataSource = dsMonedaPorProducto2;
		        });
			}
			else
			{		

				
				bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(respuesta)
				},idVentana);				
				
//				bcsComunes.ventanaMsg({
//				      mensaje:$translate.instant("UI_ERROR"),
//				      conflictos:{
//				       errores : [ {
//				           Codigo : 'COD',
//				           Descripcion : ''
//				          } ],
//				      }
//  				},idVentana);
			}
			overlay.encendido = false;
			bcsComunes.ventanaOverlay($scope.winID, false);
		};	
		
		/*------------------*/
		var error = function(respuesta)
		{		
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"),
				conflictos:util.parseServerResponse(respuesta)
			},idVentana);

			bcsComunes.ventanaMsg({
			      mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"),
			      conflictos:{
			       errores : [ {
			           Codigo : 'COD',
			           Descripcion : ''
			          } ],
			      }
			},idVentana);
			
			bcsComunes.ventanaOverlay($scope.winID, false);
		};	
		
		
		//---------------------------------------------
		var defDataSource = function(){
			var dsDefMonedaPorProducto = 
			{
					type : "odata",
					definition : 
					{
	                    serverFiltering : true, // Filtrado de datos en el servidor.
	                    serverPaging    : true, // Paginado de los datos en el servidor.
	                    serverSorting   : true, // Ordenamiento de los datos en el servidor.
	                    sort 			: { field: "", dir: "DESC" },
	                    pageSize        : 25,
	                    transport		: {
			        	            		read: {
			        	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=398",
			        							dataType:"json"
			        	            		}
			        	            	},
	                    schema 			: { 
	                    				   model: 
	                    						{
													fields : 
													{
														NombreProducto			: {type: "string"},
														Instrumento				: {type: "string"},
														IdMonedaPrincipal		: {type: "string"},
														IdMonedaSecundaria		: {type: "string"},
														IdEstado				: {type: "string", editable: true ,validation: { min: 0, required: true }},
														FeriadoMonedaPrincipal   : {type: "string"}//deshacer cambio
													}
												}
										  }

	    			 }
			};
			return dsDefMonedaPorProducto;
		};
		       	
		//---------------------------------------------
		//GRILLA PUNTAS
 
		var dsMonedaPorProducto = new bcs.DataSource(defDataSource());
		$scope.confMonedaProducto = 
		{
    				dataSource		: dsMonedaPorProducto,
    					columns		: [  {field: "NombreProducto", title: "UI_PRODUCTO", width: "70px", align: "left", sortable : false } 
    									,{field: "Instrumento", title: "UI_INSTRUMENTO", width: "70px", align: "left", sortable : false }
				       				    ,{field: "IdMonedaPrincipal", title: "UI_MONEDA_PRINCIPAL", width: "30px", align: "left", sortable : false }
				       				    ,{field: "IdMonedaSecundaria", title: "UI_MONEDA_SECUNDARIA", width: "30px", align: "left", sortable : false }
				       				    ,{field: "IdEstado", title: "UI_ESTADO", width: "60px", align: "left", sortable : false }
										,{field: "FeriadoMonedaPrincipal", title: "UI_FERIADO_MONEDA_PRINCIPAL", width: "60px", align: "left", sortable : false }////deshacer cambio
									  ],		
    					sortable	: true,
    					pageable	: true,
    					filterable	: true,//deshacer cambio
    					navigatable: true,
    					height		: 250,
    					selectable 	: "row",
    					userSettings 	: true,
    					excel 			: { allPages: true },
    				    autoBind 	 : false,
    				    gridCreated: function(grid){
    				    	var filtroMonedaPorProducto = new Array();
    			    		/*filtroMonedaPorProducto.push({ field: "IdTipoProducto", operator: "eq", value: parametrosVentana.idTipoProducto });
    			    		if(parametrosVentana.idTipoProducto === 'MEFW')
    			    			filtroMonedaPorProducto.push({ field: "IdTipoInstrumento", operator: "eq", value: 'FWD'});
    			    		if(parametrosVentana.idTipoProducto === 'MEME')
    			    			filtroMonedaPorProducto.push({ field: "IdTipoInstrumento", operator: "eq", value: 'SPT'});*/
    			            dsMonedaPorProducto.filters(filtroMonedaPorProducto); 
    			            $scope.confMonedaProducto.dataSource = dsMonedaPorProducto;
    				    }
       	};
		    
		//Infgresar Moneda Por Producto
		$scope.data.ingresarMonedaPorProducto = function()
	    {
			if($scope.data.oConfiguracionTipoInstrumento.idActual === 0 || $scope.data.oConfiguracionTipoInstrumento.idActual === false)
		        bcsComunes.msg("error","error",$translate.instant('UI_INSTRUMENTO_ERROR'));
		      else if ($scope.data.oConfiguracionMonedaPrincipal.idActual === 0 )
		        bcsComunes.msg("error","error",$translate.instant('UI_MONEDA_PRINCIPAL_ERROR'));
		      else if ($scope.data.oConfiguracionMonedaSecundaria.idActual === 0 )
		        bcsComunes.msg("error","error",$translate.instant('UI_MONEDA_SECUNDARIA_ERROR'));
		      else if($scope.data.oConfiguracionMonedaPrincipal.idActual == $scope.data.oConfiguracionMonedaSecundaria.idActual)
		        bcsComunes.msg("error","error",$translate.instant('UI_MONEDAS_IGUALES_ERROR'));
		      else
			{
				var tablaMonedaPorProducto = [];
				
				var ObjMonedaPorProducto = {
						"IdInstrumento"         : $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdInstrumento
						,"IdEmpresa"			: $scope.SesionIdEmpresa
						, "Nemotecnico"   		: $scope.data.oConfiguracionTipoInstrumento.seleccionado.Nemotecnico  
						, "IdTipoProducto"		: $scope.data.oConfiguracionTipoProducto.seleccionado.IdTipoProductoMesa
						, "IdTipoInstrumento"	: $scope.data.oConfiguracionTipoProducto.seleccionado.IdTipoInstrumento
						, "IdMonedaPrincipal"   : $scope.data.oConfiguracionMonedaPrincipal.idActual   
						, "IdMonedaSecundaria"  : $scope.data.oConfiguracionMonedaSecundaria.idActual           
						, "IdEstado" 			: 'ACT'
					};
				tablaMonedaPorProducto.push(ObjMonedaPorProducto);
					
				var parametros = {};
				parametros.p_MonedaPorProducto = tablaMonedaPorProducto;
				
				bcsComunes.log().info(JSON.stringify(parametros));	
		       	bcsServiciosMesa.CrearMonedaPorProducto(parametros,successIngreso,error);
			}
		};
		
		
		

		//Crear y Modificar Moneda Por Producto
		$scope.data.modificarMonedaPorProducto = function(accion)
	    {
			
			if(accion!='Crear'){
				var items = $scope.seleccion;
			}
			var IdVentanamodal = bcsComunes.ventanaAbrir("ModalEditarEstado", {items : items} , true,true);
			var promesa=bcsComunes.ventanaPromesa(IdVentanamodal);
			promesa.then(function(data){
				if(typeof data !== 'undefined')
					if(data.Resultado === 'OK')
					{
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_ESTADO_ACTUALIZADO"),
							conflictos:util.parseServerResponse(response)
						},idVentana);
					}
					else if (data.Resultado === 'ERROR')
					{
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_ERROR_ACTUALIZAR"),
							conflictos:{
							errores : [ {
								Codigo : 'COD',
								Descripcion : ''
								} ],
							}
						},idVentana);
					}		        			
				$scope.data.filtrarMonedaPorProducto();
			}); 	
		};
		
	});
}());