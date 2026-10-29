angular.module('ModuloPlataforma').controller('PanelContratosCtrl',function ($scope,bcsComunes,$translate, bcsAlerta, servicioContratoComision, bcsAlerta,OPCIONES_BCSALERTA) {
	
	const panelContratos 		= this;
	const idVentana				= bcsComunes.ventanaWinId($scope);
	panelContratos.filtros 		= {};
	panelContratos.datos 		= {};
	panelContratos.dataItem 	= {};


	//Grilla Contratos
	panelContratos.configuracionGrillaContratos = {
			dataSource: new bcs.DataSource({
				type: "odata",
				definition 	: {
					serverFiltering : true, 
					serverPaging 	: true,
					serverSorting 	: true,
					sortable 		: true,
					pageSize 		: 50,
	                transport		: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700275",
							dataType	: "json"
					    }
	                },
					schema: {
						model: {
							fields: {
								IdContratoComision			: { type: "number"},
								NombreContratoComision		: { type: "string"},
								DescripcionContratoComision	: { type: "string"},
								DescripcionSegmento			: { type: "string"},
								FormatoCobro				: { type: "string"},
								NombreTipoInstrumento		: { type: "string"},
								NombreMoneda				: { type: "string"},
								DescripcionArea				: { type: "string"},
								DocumentoIdentidad			: { type: "string"},
								NombrePersonaCompleto		: { type: "string"},
								CodigoPortafolio			: { type: "string"},
								NombreEstado				: { type: "string"},
								RazonSocialPortafolio		: { type: "string"}
							}
						}
					},
				}
			}),
			columns: [
				{field: "NombreContratoComision",	title: $translate.instant('UI_NOMBRE'), width: "100px", align: "left", sortable : true, editable: false },
				{field: "DescripcionContratoComision",	title: $translate.instant('UI_DESCRIPCION'), width: "100px", align: "left", sortable : true, editable: false },
				{field: "DocumentoIdentidad",	title: $translate.instant('UI_DOCUMENTO_IDENTIDAD'), width: "100px", align: "left", sortable : true, editable: false },
				{field: "NombrePersonaCompleto",	title: $translate.instant('UI_CLIENTE'), width: "100px", align: "left", sortable : true, editable: false },
				{field: "RazonSocialPortafolio",	title: $translate.instant('UI_RAZON_SOCIAL_PORTAFOLIO'), width: "100px", align: "left", sortable : true, editable: false },
				{field: "CodigoPortafolio",	title: $translate.instant('UI_PORTAFOLIO'), width: "100px", align: "left", sortable : true, editable: false },
				{field: "FormatoCobro",	title: $translate.instant('UI_FORMATO_CONTRATO_COBRO'), width: "100px", align: "left", sortable : true, editable: false },
				{field: "NombreTipoInstrumento",	title: $translate.instant('UI_CLASIFICACION'), width: "75px", align: "left", sortable : true, editable: false },
				{field: "DescripcionArea",	title: $translate.instant('UI_AREA_CLIENTE'), width: "100px", align: "left", sortable : true, editable: false },
			    {field: "NombreMoneda"	,title: $translate.instant('UI_MONEDA'), width: "75px", align: "left", sortable : true, editable: false },
			    {field: "DescripcionSegmento"	,title: $translate.instant('UI_SEGMENTO_CLIENTE'), width: "100px", align: "left", sortable : true, editable: false },
			    {field: "NombreEstado",	title: $translate.instant('UI_ESTADO'), width: "60px", align: "left", sortable : true, editable: false }
			],
			gridCreated : function(grid){
				panelContratos.ajustaVentana();
			},
			detailExpand : function(e){
				this.collapseRow(this.tbody.find(' > tr.k-master-row').not(e.masterRow));
				this.clearSelection();
				this.select(e.masterRow);
				panelContratos.dataItem.kNgModel = undefined;
			},
			
			sortable		: false,
			pageable		: true,
			autoBind		: true,
	    	filterable		: true,
			height			: 550,
		    selectable 		: "row",
		    scrollable		: true,
		    userSettings	: true,
			navigatable		: true
	};
	
	
	//Grilla DetalleComision
	panelContratos.configuracionGrillaDetalleComision = function(dataItem){
		
		const filtros = [{field : "IdContratoComision", operator: "eq", value: dataItem.IdContratoComision}];
		
		return  {	
					dataSource: new bcs.DataSource({
					type: "odata",
					definition 	: {
						serverFiltering : true, 
						serverPaging 	: true,
						serverSorting 	: true,
						sortable 		: true,
						pageSize 		: 50,
		                transport		: {
							read: {
								url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700289",
								dataType	: "json"
						    }
		                },
						schema: {
							model: {
								fields: {
									IdDetalleContratoComision	: { type: "number"},
									IdCobroClasificacion		: { type: "number"},
									CodigoCobroClasificacion	: { type: "string"},
									NombreCobro					: { type: "string"},
									EsPorRangos					: { type: "string"},
									NombreConcepto				: { type: "string"}
								}
							}
						},
						filter : filtros,
					}
					}),
					columns: [
					    {field: "CodigoCobroClasificacion" 	,title: $translate.instant('UI_CLASIFICACION_COBRO'),  width: "100px", align: "left", sortable : true, editable: false },
					    {field: "NombreCobro" 	,title: $translate.instant('UI_TIPO_COBRO'), width: "100px", align: "left", sortable : true, editable: false },
					    {field: "EsPorRangos" 	,title: $translate.instant('UI_POR_RANGOS'), width: "75px", align: "left", sortable : true, editable: false },
					    {field: "NombreConcepto"	,title: $translate.instant('UI_CONCEPTO'), width: "75px", align: "left", sortable : true, editable: false }
					],
					gridCreated : function(grid){
						panelContratos.ajustaVentana();
					},
					sortable			: false,
					pageable			: true,
					autoBind			: true,
			    	filterable			: false,
					height				: 140,
				    selectable 			: "row",
				    scrollable			: true,
				    userSettings		: true,
					navigatable			: true
				};
	};

	panelContratos.crear = function(){
		const id = bcsComunes.ventanaAbrir("agregarModificarContratoComision", {}, false, false, idVentana);  
		bcsComunes.ventanaPromesa(id).then(function(){
			panelContratos.datos.kNgModel = undefined;
			panelContratos.configuracionGrillaContratos.dataSource.read();
         }); 
  
	};

  	panelContratos.editar = function(datos) {
		if(!datos) return;
		const id = bcsComunes.ventanaAbrir("agregarModificarContratoComision", datos, false, false, idVentana);                              
        bcsComunes.ventanaPromesa(id).then(function(){  
        	panelContratos.datos.kNgModel = undefined;
        	panelContratos.configuracionGrillaContratos.dataSource.read();
        }); 
  	};
  	
  	panelContratos.crearDetalle = function(datos) {
  		if(!datos) return;
  		const data =  {IdContratoComision	: datos.IdContratoComision,
  						IdMoneda			: datos.IdMoneda,
  						formModo 			: 'add',
						IdClasificacionTipoInstrumento : datos.IdClasificacionTipoInstrumento
  					}
		const id = bcsComunes.ventanaAbrir("agregarModificarDetalleContratoComision",data, false, false, idVentana);  
		bcsComunes.ventanaPromesa(id).then(function(){
			panelContratos.datos.kNgModel = undefined;
			panelContratos.configuracionGrillaContratos.dataSource.read();
         });  
  	};

  	panelContratos.editarDetalle = function(datos) {
  	
  		datos.IdMoneda = panelContratos.datos.kNgModel.IdMoneda;		  
		datos.IdClasificacionTipoInstrumento = panelContratos.datos.kNgModel.IdClasificacionTipoInstrumento;

		const id = bcsComunes.ventanaAbrir("agregarModificarDetalleContratoComision", datos, false, false, idVentana); 
		datos.formModo = 'edit'; 
		bcsComunes.ventanaPromesa(id).then(function(){
			panelContratos.dataItem.kNgModel = undefined;
			panelContratos.datos.kNgModel = undefined;
			panelContratos.configuracionGrillaContratos.dataSource.read();
         }); 
  	};
	
	panelContratos.eliminarDetalle = function(datos){
		var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_ELIMINARA_DETALLE_SELECCIONADO') );
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR)
			{
				var parametros = {};
				
				parametros.p_IdEmpresa               = $scope.SesionIdEmpresa;
				parametros.p_IdUsuario               = $scope.SesionIdUsuario;
				parametros.p_IdModulo				 = "PLA";
				parametros.p_UsaReglasNegocio	     = "N";
				parametros.p_DetallesContratoComision= [{
													IdContratoComision				: datos.IdContratoComision,
													IdDetalleContratoComision		: datos.IdDetalleContratoComision || null
													}];				   
				   				
				servicioContratoComision.eliminarDetalleContratoComision(parametros, function (result){
					if (result.P_OUT_ERRORESCONTADOR !== undefined && result.P_OUT_ERRORESCONTADOR == 0)
					{	
							bcsComunes.ventanaPromesa({									
								mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_EXITO"),
								conflictos:util.parseServerResponse(result)
							},idVentana);							
					}
					else
					{	
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
							conflictos:util.parseServerResponse(result)
						},idVentana);
					}
					bcsComunes.ventanaOverlay(idVentana, false);
				}, function(resError){
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
						conflictos:util.parseServerResponse(resError)
					},idVentana);
					bcsComunes.ventanaOverlay(idVentana, false);
				});

				panelContratos.buscar();
			}
		});
	};

	panelContratos.ajustaVentana = function() {
		bcsComunes.ventanaTamanoAutomatico(idVentana,40);
   }

	panelContratos.buscar = function() {
		let filtroBusqueda = new Array();
		if(panelContratos.filtros.IdPortafolio){
			filtroBusqueda.push({ field: "IdPortafolio", operator: "eq", value: panelContratos.filtros.IdPortafolio});
		}
		panelContratos.configuracionGrillaContratos.dataSource.filter(filtroBusqueda);
	};

	panelContratos.limpiarFiltros = function() {
  		panelContratos.filtros.IdPortafolio = false;
	};
	
	panelContratos.limpiarFiltros();	
});