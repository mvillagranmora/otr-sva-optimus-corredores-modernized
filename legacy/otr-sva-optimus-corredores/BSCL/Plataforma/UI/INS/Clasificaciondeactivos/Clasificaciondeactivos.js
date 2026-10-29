(function() {
	angular.module('ModuloPlataforma').controller('ClasificaciondeactivosCtrl',
	function($rootScope,$scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsServiciosCuentaInversion,
				 bcsAlerta, OPCIONES_BCSALERTA,$http,$resource){

		$rootScope.$on("CallParentEventos",function(){
			$scope.leerEventos();
		});	
		$rootScope.$on("CallParentVerDetalle",function(event, data){
		 	$scope.verDetalle(data.IdConfiguracionEvento);
	 	});
	 	$scope.$watch("conf.seleccionadoevento", function(oval, nval){
	 	});
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var pLCP 		= this;
		pLCP.idVentana	= bcsComunes.ventanaWinId($scope);
		pLCP.win		= {};
		pLCP.grilla		= {};
		pLCP.cmbbx		= {};
		pLCP.filtro		= {};
		pLCP.fn			= {};
		
		var formData 		= this;
		formData.idVentana	= bcsComunes.ventanaWinId($scope);
		formData.win		= {};
		formData.grilla		= {};
		formData.cmbbx		= {};
		formData.filtro		= {};
		formData.fn			= {};
		
		formData.filtro.IdClasificacionTipoInstrumentoDef	= 0;
		$scope.dataTemplate	= {};
		var filters = {};

		/*********************************************
		 * DATASOURCES
		 *********************************************/
		var defMercados = {
				type: "odata",
				definition : {
					serverFiltering : true, // Filtrado de datos en el servidor.
	                serverPaging    : true, // Paginado de los datos en el servidor.
	                serverSorting   : true, // Ordenamiento de los datos en el servidor.
	                pageSize        : 25,
	                sort			: {field: "IdArbolClasificacionActivo", dir: "desc" },
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=601171",
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								IdArbolClasificacionActivo	: 	{ type: "number"},
								Descripcion					: 	{ type: "string"},
								Padre		         		:	{ type: "number"},
								Indice	: 	{ type: "string"}
							}
						}
					}
				}	
			};
		
		var defSubMercados = {
				type: "odata",
				definition: {
					serverFiltering : true, // Filtrado de datos en el servidor.
					serverPaging 	: true, // Paginado de los datos en el servidor.
					serverSorting 	: true, // Ordenamiento de los datos en el servidor.
					sortable 		: true,
					pageSize 		: 50,
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=601165", // 601165 9 antigua
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								
								IdArbolClasificacionActivo	: 	{ type: "number"},
								Descripcion					: 	{ type: "string"},
								Padre						:	{ type: "number"},
								Tipo 						:	{ type: "string"},
								Indice	: 	{ type: "string"}

							}
						}
					}
				}
			};
		
		var defClasificacion = {
				type: "odata",
				definition: {
					serverFiltering : true, // Filtrado de datos en el servidor.
					serverPaging 	: true, // Paginado de los datos en el servidor.
					serverSorting 	: true, // Ordenamiento de los datos en el servidor.
					sortable 		: true,
					pageSize 		: 50,
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=601165",
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								
								IdArbolClasificacionActivo	: 	{ type: "number"},
								Descripcion					: 	{ type: "string"},
								Padre						:	{ type: "number"},
								Tipo 						:	{ type: "string"}
							}
						}
					}
				}	
			};
		
		var defInstrumentosMercados = {
				type: "odata",
				definition: {
					serverFiltering : true, // Filtrado de datos en el servidor.
					serverPaging 	: true, // Paginado de los datos en el servidor.
					serverSorting 	: true, // Ordenamiento de los datos en el servidor.
					sortable 		: true,
					pageSize 		: 25,
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=601166", // 601165 9 antigua
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								
								IdRelacionClasificacion							: 	{ type: "number"},
								IdArbolClasificacionActivo						: 	{ type: "number"},
								IdTipoInstrumento								:	{ type: "string"},
								IdInstrumento 									:	{ type: "number"},
								Nemotecnico										:	{ type: "string"}

							}
						}
					}
				}	
			};
		

		/****************************************************************
		 * CONFIGURACION DE CADA GRILLA *
		 ****************************************************************/
		
		var dsMercados = new bcs.DataSource(defMercados);
		formData.grillaMercados = {
				dataSource: dsMercados,
				columns: [
					    
						  {field: "Descripcion",			title: $translate.instant('UI_MERCADOS'),			width: "250px", align: "left"},
						  {field: "Indice",			        title: $translate.instant('UI_INDICE_MAY'),			width: "50px", align: "left"}
				
					    ],	
				sortable		: true,
				filterable		: true,
				pageable		: false,
				
				pageable: {
                    refresh: true,
                    buttonCount: 5
                },
                
				autoBind		: true,
				height			: 300,
				selectable		: "row",
			    userSettings	: true,
				navigatable		: true,
				gridCreated     : function(g){
					formData.ApiTraspasos = g;
				},
				excel : {
					allPages: true,
				},
				allowCopy: true
		
		};
	
		var dsSubMercados = new bcs.DataSource(defSubMercados);
		formData.grillaSubMercados = {
				dataSource: dsSubMercados,
				columns: [
					    
						  {field: "Descripcion",			title: $translate.instant('UI_SUBMERCADOS'),			width: "250px", align: "left"},
						  {field: "Indice",			        title: $translate.instant('UI_INDICE_MAY'),			    width: "50px", align: "left"}
					    ],	
				sortable		: true,
				filterable		: true,
				pageable		: false,
				
				pageable: {
                    refresh: true,
                    buttonCount: 5
                },
                
				autoBind		: false,
				height			: 300,
				selectable		: "row",
			    userSettings	: true,
				navigatable		: true,
				gridCreated     : function(g){
					formData.ApiTraspasos = g;
				},
				excel : {
					allPages: true,
				},
				allowCopy: true
		
		};
		
		var dsClasificacion = new bcs.DataSource(defClasificacion);
		formData.grillaClasificacion = {
				dataSource: dsClasificacion,
				columns: [
					    
					{field: "Descripcion",					title: $translate.instant("UI_CLASIFICACION_INSTRUMENTO"),	width: "250px"}
				
					    ],	
				sortable		: true,
				filterable		: true,
				pageable		: false,
				
				pageable: {
                    refresh: true,
                    //pageSizes: true,
                    buttonCount: 5
                },
                
				autoBind		: false,
				height			: 300,
				selectable		: "row",
			    userSettings	: true,
				navigatable		: true,
				gridCreated     : function(g){
					formData.ApiTraspasos = g;
				},
				excel : {
					allPages: true,
				},
				allowCopy: true
		
		};
		
		
		var dsInstrumentosMercados = new bcs.DataSource(defInstrumentosMercados);
		formData.grillaInstrumentosMercados = {
				dataSource: dsInstrumentosMercados,
				columns: [
															
					{field: "Nemotecnico",					title: $translate.instant("UI_INSTRUMENTOS_MAY"),	width: "250px"}
				
					    ],
					    rowColorBackground : function(row) {
							console.log(row);
							if(row.get("Nemotecnico")!=""){
								return "green";
							}
					  	},
				sortable		: false,
				filterable		: false,
				pageable		: false,
				
				pageable: {
                    refresh: true,
                    buttonCount: 5
                },
                
				autoBind		: false,
				height			: 200,
				selectable		: "row",
			    userSettings	: true,
				navigatable		: true,
				gridCreated     : function(g){
					formData.ApiTraspasos = g;
				},
				excel : {
					allPages: true,
				},
				allowCopy: true
		
		};
		
		var dsInstrumentosSubMercados = new bcs.DataSource(defInstrumentosMercados);
		formData.grillaInstrumentosSubMercados = {
				dataSource: dsInstrumentosSubMercados,
				columns: [
					    
					{field: "Nemotecnico",					title: $translate.instant("UI_INSTRUMENTOS_MAY"),	width: "250px"}
				
					    ],
					    rowColorBackground : function(row) {
							console.log(row);
							if(row.get("Nemotecnico")!=""){
								return "green";
							}
					  	},
				sortable		: false,
				filterable		: false,
				pageable		: false,
				
				pageable: {
                    refresh: true,
                    buttonCount: 5
                },
				autoBind		: false,
				height			: 200,
				selectable		: "row",
			    userSettings	: true,
				navigatable		: true,
				gridCreated     : function(g){
					formData.ApiTraspasos = g;
				},
				excel : {
					allPages: true,
				},
				allowCopy: true
		
		};
		var dsInstrumentosClasificacion = new bcs.DataSource(defInstrumentosMercados);
		formData.grillaInstrumentosClasificacion = {
				dataSource: dsInstrumentosClasificacion,
				columns: [
					    
					{field: "Nemotecnico",					title: $translate.instant("UI_INSTRUMENTOS_MAY"),	width: "250px"}
				
					    ],
					    rowColorBackground : function(row) {
							console.log(row);
							if(row.get("Nemotecnico")!=""){
								return "green";
							}
					  	},
				sortable		: false,
				filterable		: false,
				pageable		: false,
				
				pageable: {
                    refresh: true,
                    buttonCount: 5
                },
                
				autoBind		: false,
				height			: 200,
				selectable		: "row",
			    userSettings	: true,
				navigatable		: true,
				gridCreated     : function(g){
					formData.ApiTraspasos = g;
				},
				excel : {
					allPages: true,
				},
				allowCopy: true
		
		};
		/****************************************************************
		 * CASCADA DE LAS GRILLAS LLAMADAS DESDE LA SELECION DE CADA UNA *
		 ****************************************************************/
		
		// LLENA LAS TABLAS DE SUBMERCADO E INSTRUMENTOS ASOCIADOS AL MERCADO SELECCIOANDO (LA DE DEBAJO DE MERCADOS)
		
		$scope.verDetalleMercado = function()
		{
				var filters = new Array();
				filters.push({  field: "Padre", operator: "eq", value: $scope.formData.MercadoSeleccionado.IdArbolClasificacionActivo }); 
				dsSubMercados.filter(filters);
				$scope.verDetalleMercadoInstrumentos();
				
				if($scope.formData.MercadoSeleccionado!= undefined)
				{
					$scope.formData.SubMercadoSeleccionado    =undefined;
					$scope.formData.ClasifiacionSeleccionado  =undefined;
				}
				dsInstrumentosSubMercados.removeAll();
				dsClasificacion.removeAll();
				dsInstrumentosClasificacion.removeAll();
				
		};
		$scope.verDetalleMercadoInstrumentos = function()
		{
				var filters = new Array();
					filters.push({ field: "IdArbolClasificacionActivo", operator: "eq", value: $scope.formData.MercadoSeleccionado.IdArbolClasificacionActivo}); 
					dsInstrumentosMercados.filter(filters);
		};
		
		// LLENA LAS TABLAS DE CLASIFICACION  E INSTRUMENTOS ASOCIADOS AL SUBMERCADO SELECCIOANDO (LA DE DEBAJO DE SUBMERCADOS)
		
		$scope.verDetalleSubMercado = function()
		{
			var filters = new Array();
				filters.push({  field: "Padre", operator: "eq", value: $scope.formData.SubMercadoSeleccionado.IdArbolClasificacionActivo }); 
				dsClasificacion.filter(filters);
	
				if($scope.formData.SubMercadoSeleccionado!= undefined)
				{
					
					$scope.formData.MercadoSeleccionado       =undefined;
					$scope.formData.ClasifiacionSeleccionado  =undefined;
				}
				
				$scope.verDetalleSubMercadoInstrumentos();
				$scope.verDetalleClasifiacion();
				dsInstrumentosClasificacion.removeAll();
				
		};
		$scope.verDetalleSubMercadoInstrumentos = function()
		{	
			var filters = new Array();
			filters.push({ field: "IdArbolClasificacionActivo", operator: "eq", value: $scope.formData.SubMercadoSeleccionado.IdArbolClasificacionActivo}); 
			dsInstrumentosSubMercados.filter(filters);
		};
		
		// LLENA LA TABLA DE INSTRUMENTOS ASOCIADOS A LA CLASIFICACION (DEBAJO DE DE CLASIFICACION)
		
		$scope.verDetalleClasifiacion = function()
		{
			var filters = new Array();
			filters.push({ field: "IdArbolClasificacionActivo", operator: "eq", value: $scope.formData.ClasifiacionSeleccionado.IdArbolClasificacionActivo}); 
			dsInstrumentosClasificacion.filter(filters);
	
			if($scope.formData.ClasifiacionSeleccionado!= undefined)
				{
				
				$scope.formData.MercadoSeleccionado       =undefined;
				$scope.formData.SubMercadoSeleccionado    =undefined;
				}

		};
		$scope.limpiaSeleccionesInstrumentoMercado = function()
		{
			if($scope.formData.MercadoInstrumentoSeleccionado != undefined)
			{
				$scope.formData.SubMercadoInstrumentoSeleccionado    =undefined;
				$scope.formData.ClasifiacionInstrumentoSeleccionado  = undefined;
				
				$scope.formData.MercadoSeleccionado       =undefined;
				$scope.formData.SubMercadoSeleccionado    =undefined;
				$scope.formData.ClasifiacionSeleccionado  =undefined;
			}
		}
		$scope.limpiaSeleccionesInstrumentoSubMercado = function()
		{
			if($scope.formData.SubMercadoInstrumentoSeleccionado != undefined)
			{
				$scope.formData.MercadoInstrumentoSeleccionado       =undefined;
				$scope.formData.ClasifiacionInstrumentoSeleccionado  = undefined;
				
				$scope.formData.MercadoSeleccionado       =undefined;
				$scope.formData.SubMercadoSeleccionado    =undefined;
				$scope.formData.ClasifiacionSeleccionado  =undefined;
			}
		}
		$scope.limpiaSeleccionesInstrumentoClasificacion = function()
		{
			if($scope.formData.ClasifiacionInstrumentoSeleccionado != undefined)
			{
				$scope.formData.SubMercadoInstrumentoSeleccionado    =undefined;
				$scope.formData.MercadoInstrumentoSeleccionado       =undefined;
				
				$scope.formData.MercadoSeleccionado       =undefined;
				$scope.formData.SubMercadoSeleccionado    =undefined;
				$scope.formData.ClasifiacionSeleccionado  =undefined;
			}
		}
		
		/***********************************************************************
		 * FUNCIONES DE LOS BOTONES NUEVO-MODIFICAR-ELIMINAR.CLASIFICAR ACTIVO *
		 ***********************************************************************/
		
		$scope.EliminarDatos = function(){
			
			if($scope.formData.MercadoSeleccionado!= undefined || $scope.formData.SubMercadoSeleccionado!= undefined || $scope.formData.ClasifiacionSeleccionado!= undefined)
			{	
				if($scope.formData.MercadoSeleccionado!= undefined){$scope.formData.NivelSeleccionado =$scope.formData.MercadoSeleccionado;}
				if($scope.formData.SubMercadoSeleccionado!=undefined){$scope.formData.NivelSeleccionado =$scope.formData.SubMercadoSeleccionado;}
				if($scope.formData.ClasifiacionSeleccionado!=undefined){$scope.formData.NivelSeleccionado =$scope.formData.ClasifiacionSeleccionado;}
				
			var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_ELIMINAR_EVENTO") + "\n" + $translate.instant("\nEstá apunto de eliminar : "+ $scope.formData.NivelSeleccionado.Descripcion) + "" ,pLCP.idVentana);
			t.then(function(result)
		      {		
				 if (result===OPCIONES_BCSALERTA.SI)
			       {	
					 var urlEliminar = "/BSCL/Plataforma/GWJAVA/PLA/call?t=601181";
						var r = $resource("", {}, {'Eliminar': { method: "POST", url: urlEliminar}});
						
						var datosEliminar = {			
								p_IdArbolClasificacionActivo    :$scope.formData.NivelSeleccionado.IdArbolClasificacionActivo,
								p_Descripcion    				:$scope.formData.NivelSeleccionado.Descripcion,
								p_Padre							:$scope.formData.NivelSeleccionado.Padre
						};
						
						var post = r.Eliminar(datosEliminar);
						post.$promise.then(function(result){
							if(result.P_OUT_MENSAJES[0].tipo =="OK")
							{
								var titulo ="UI_DETALLE_TITULO";
								bcsComunes.ventanaMsg({mensaje:$translate.instant(titulo),
									conflictos:util.parseServerResponse(result)},$scope.ventanaId);
							}
							else
								{
								var titulo ="UI_DETALLE_TITULO";
								bcsComunes.ventanaMsg({mensaje:$translate.instant(titulo),
									conflictos:util.parseServerResponse(result)},$scope.ventanaId);
								}
							bcsComunes.setConflictos(util.parseServerResponse(result));
						},function(result){
							bcsComunes.ventanaOverlay($scope.ventanaId,false);
							$scope.etapa='';
							bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_ERROR'),
								conflictos:util.parseServerResponse(result)},$scope.ventanaId);
							bcsComunes.setConflictos(util.parseServerResponse(result));
						});
											
			       };
			});
			}
			else if($scope.formData.MercadoInstrumentoSeleccionado!= undefined || $scope.formData.SubMercadoInstrumentoSeleccionado!= undefined || $scope.formData.ClasifiacionInstrumentoSeleccionado!= undefined )
			{
				
				if($scope.formData.MercadoInstrumentoSeleccionado!= undefined){$scope.formData.InstrumentosSeleccionado =$scope.formData.MercadoInstrumentoSeleccionado;}
				if($scope.formData.SubMercadoInstrumentoSeleccionado!=undefined){$scope.formData.InstrumentosSeleccionado =$scope.formData.SubMercadoInstrumentoSeleccionado;}
				if($scope.formData.ClasifiacionInstrumentoSeleccionado!=undefined){$scope.formData.InstrumentosSeleccionado =$scope.formData.ClasifiacionInstrumentoSeleccionado;}
				
				var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_ELIMINAR_EVENTO") + ", " + $translate.instant(""+ $scope.formData.InstrumentosSeleccionado.Nemotecnico) + "" ,pLCP.idVentana);
				t.then(function(result)
			      {		
					 if (result===OPCIONES_BCSALERTA.SI)
				       {
						 
						 var urlEliminar = "/BSCL/Plataforma/GWJAVA/PLA/call?t=601182";
							var r = $resource("", {}, {'Eliminar': { method: "POST", url: urlEliminar}});
							
							var datosEliminar = {			
									p_IdInstrumento    :$scope.formData.InstrumentosSeleccionado.IdInstrumento
							};
							var post = r.Eliminar(datosEliminar);
							post.$promise.then(function(result){
								if(result.P_OUT_MENSAJES[0].tipo =="OK")
								{
									var titulo ="UI_DETALLE_TITULO";
									bcsComunes.ventanaMsg({mensaje:$translate.instant(titulo),
										conflictos:util.parseServerResponse(result)},$scope.ventanaId);
								}
								else
									{
									var titulo ="UI_DETALLE_TITULO";
									bcsComunes.ventanaMsg({mensaje:$translate.instant(titulo),
										conflictos:util.parseServerResponse(result)},$scope.ventanaId);
									}
								bcsComunes.setConflictos(util.parseServerResponse(result));
							},function(result){
								bcsComunes.ventanaOverlay($scope.ventanaId,false);
								$scope.etapa='';
								bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_ERROR'),
									conflictos:util.parseServerResponse(result)},$scope.ventanaId);
								bcsComunes.setConflictos(util.parseServerResponse(result));
								//console.log(JSON.stringify(result));
							});
												
				       };
				});
				}
			else if ($scope.formData.InstrumentosSeleccionado!= undefined && $scope.formData.NivelSeleccionado!= undefined)
			{
				var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_ELIMINAR_EVENTO") + ", " + $translate.instant("Está apunto de eliminar : "+ $scope.formData.InstrumentosSeleccionado.Nemotecnico) + "" ,pLCP.idVentana);
				t.then(function(result)
			      {		
					 if (result===OPCIONES_BCSALERTA.SI)
				       {
						 
						 var urlModificar = "/BSCL/Plataforma/GWJAVA/PLA/call?t=601176";
							var r = $resource("", {}, {'Modificar': { method: "POST", url: urlModificar}});
							
							var datosModificar = {			
									p_IdArbolClasificacionActivo   :$scope.filtros.clasificacionTipoInstrumento,
									p_Descripcion    				:$scope.formData.NuevoNombreMercado				
							};
							var post = r.Modificar(datosModificar);
							post.$promise.then(function(result){
								if(result.P_OUT_MENSAJES[0].tipo =="OK")
								{
									var titulo ="UI_DETALLE_TITULO";
									bcsComunes.ventanaMsg({mensaje:$translate.instant(titulo),
										conflictos:util.parseServerResponse(result)},$scope.ventanaId);
								}
								else
									{
									var titulo ="UI_DETALLE_TITULO";
									bcsComunes.ventanaMsg({mensaje:$translate.instant(titulo),
										conflictos:util.parseServerResponse(result)},$scope.ventanaId);
									}
								bcsComunes.setConflictos(util.parseServerResponse(result));
							},function(result){
								bcsComunes.ventanaOverlay($scope.ventanaId,false);
								$scope.etapa='';
								bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_ERROR'),
									conflictos:util.parseServerResponse(result)},$scope.ventanaId);
								bcsComunes.setConflictos(util.parseServerResponse(result));
							});
												
				      };
				});
				}
			
			
			
			console.log($scope.formData.NivelSeleccionado);
			console.log($scope.formData.InstrumentosSeleccionado);
			
		};	
		/*********************************************
		 * COMBOBOX
		 *********************************************/
	
		/*********************************************
		 * FUNCIONES
		 *********************************************/

		formData.fn.buscar = function(){
				//Datos grillas	
			var niv1filters = new Array();
	
	    	
			niv1filters.push({ field: "IdEmpresa ", operator: "eq", value: $scope.SesionIdEmpresa });
			niv1filters.push({ field: "IdTipoMovimientoTraspaso ", operator: "eq", value: 'TRAEGR'});
			
	
			if (formData.FecTermino) {
				niv1filters.push({ field: "FechaIngreso", operator: "lte", value:util.dateTerminoDia(formData.FecTermino) });
			};
				
			if(formData.filtro.IdSocioDeNegocio && formData.filtro.IdSocioDeNegocio > 0){
	    		niv1filters.push({ field: "IdCliente", operator: "eq", value: formData.SocioDeNegocioObj.IdCliente });
			};
			if(formData.cmbbx.IdTipoTraspaso ){
	    		niv1filters.push({ field: "IdTipoTraspaso", operator: "eq", value:formData.cmbbx.IdTipoTraspaso });
	    	};
			if(formData.cmbbx.IdEmisor){
	    		niv1filters.push({ field: "IdEmisor", operator: "eq", value:formData.cmbbx.IdEmisor });
	    	};
			if(formData.cmbbx.IdMotivo){
	    		niv1filters.push({ field: "IdMotivoTraspaso", operator: "eq", value:formData.cmbbx.IdMotivo });
	    	};	    	
	    	
	    	
	    	if (niv1filters.length>0) formData.Traspasos.dataSource.filter(niv1filters);
	    	else formData.Traspasos.dataSource.filter(new Array());
	    	
	    	formData.Traspasos.dataSource.read();
	    	formData.TraspasosSel=undefined;
		}
		
		formData.fn.ingresar= function(){
			bcsComunes.ventanaAbrir("IngresoNuevoMercado", {IdTipoMovimiento: 'TRAEGR'} );
			
		};
		
		formData.fn.modificar= function(){
			bcsComunes.ventanaAbrir("ModificarMercado", {IdTipoMovimiento: 'TRAEGR'} );
			
		};
		
		formData.fn.eliminar= function(){
			bcsComunes.ventanaAbrir("EliminarMercado", {IdTipoMovimiento: 'TRAEGR'} );
			
		};
		
		formData.fn.Clasificar= function(){
			bcsComunes.ventanaAbrir("ClasificarActivo", {IdTipoMovimiento: 'TRAEGR'} );
			
		};
		formData.fn.Indices= function(){
			bcsComunes.ventanaAbrir("Clasificacion_Indices", {IdTipoMovimiento: 'TRAEGR'} );
			
		};
			/*********************************************
			 * WATCH
			 ********************************************/
	});
}());