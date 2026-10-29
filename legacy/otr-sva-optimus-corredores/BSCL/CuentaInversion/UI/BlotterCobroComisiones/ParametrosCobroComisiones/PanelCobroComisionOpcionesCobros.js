angular.module('ModuloCuentaInversion').controller('PanelCobroComisionOpcionesCobrosCtrl',
		function($scope,$timeout,$modal,$state,bcsComunes,$translate,$rootScope, bcsServiciosCuentaInversion,bcsAlerta,OPCIONES_BCSALERTA ){
	
	var pccoc = this;
	var obj = {};
	pccoc.filtros = {};
	
	var configuracionColumnas = function() {
		return {
			IdOpcionCobro					:{field: "IdOpcionCobro",					title: $translate.instant('UI_ID_OPCION'),				width: "75px",	align: "left", hidden: "true"},
			OpcionCobro						:{field: "OpcionCobro",						title: $translate.instant('UI_OPCION'),					width: "75px",	align: "left", hidden: "true"},
			DetalleIndicador				:{field: "DetalleIndicador",				title: $translate.instant('UI_OPCION'),					width: "75px",	align: "left", sortable: false},
			EsAPV							:{field: "PortafolioAPV",					title: $translate.instant('UI_PORTAFOLIO_APV'),			width: "75px",	align: "center"},
			IdClasificacionTipoInstrumento	:{field: "IdClasificacionTipoInstrumento",	title: $translate.instant('UI_MERCADO'),				width: "75px",	align: "left", hidden: "true"},
			NombreMercado					:{field: "NombreMercado",					title: $translate.instant('UI_MERCADO'),				width: "75px",	align: "left"},
			PorcentajeCobro					:{field: "PorcentajeCobro",					title: $translate.instant('UI_PORCENTAJE_COBRO'),		width: "75px",	align: "right", template : " <label> #: util.bcs_formato(PorcentajeCobro,'GEN','Tasa','"+$scope.SesionIdMonedaSistema+"','','').valor #</label>"},
			IdEstado						:{field: "IdEstado",						title: $translate.instant('UI_ESTADO'),					width: "75px",	align: "left", hidden: "true"},
			NombreEstado					:{field: "NombreEstado",					title: $translate.instant('UI_ESTADO'),					width: "75px",	align: "left"},
			FechaVigenciaDesde				:{field: "FechaVigencia",					title: $translate.instant('UI_FECHA_VIGENCIA_DESDE'),	width: "75px",	align: "left"},
		};
	};
	
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	
	$scope.parametros = bcsComunes.ventanaParams(pccoc.idVentana);
	
	$scope.pccoc.ingresarOpcionCobro = function() {
		var id1 = bcsComunes.ventanaAbrir("AgregarOpcionesCobroComision",{},false,false,$scope.idVentana); 
        var promesa=bcsComunes.ventanaPromesa(id1);     
		bcsComunes.ventanaOverlay($scope.winID, true);                          
        $.when(promesa).then(function(respuesta){
			bcsComunes.ventanaOverlay($scope.winID, false);
			pccoc.opcionesCobroComisionOptions.dataSource.read();
        });
	};
	
	$scope.pccoc.Estado={
		tipo          : "combobox-estado-cuenta-inversion",
		configuracion : {},
		filtros       : {
			Nombre : {field   : "NombreEntidad", operator: "eq", value   : "CTA.CobroComisionOpcionesCobro"}
		},
		idActual      : false,
		seleccionado  : false,
		idDefecto     : '',
		idPostFiltro  :	false
	};
	
	$scope.pccoc.editarOpcionCobro = function(ListaEntrada) {
		 
		if(ListaEntrada == undefined){
			bcsComunes.msg("error","error"," Seleccione 1 registro");
			return;
		}else if(ListaEntrada.length>1){
			bcsComunes.msg("error","error"," Seleccione sólo 1 registro");
			return;
		}
				
		bcsComunes.ventanaOverlay($scope.winID, true);   
		var entrada = ListaEntrada[0];
		
		var id1 = bcsComunes.ventanaAbrir("AgregarOpcionesCobroComision",
		{
			 IdOpcionCobro: entrada.IdOpcionCobro
			,OpcionCobro: entrada.OpcionCobro 
			,PortafolioAPV: entrada.PortafolioAPV 
			,IdClasificacionTipoInstrumento: entrada.IdClasificacionTipoInstrumento 
			,PorcentajeCobro: entrada.PorcentajeCobro 
			,IdEstado: entrada.IdEstado 
			,FechaVigencia: entrada.FechaVigencia 
		},
		false,false,$scope.winID); 
        var promesa=bcsComunes.ventanaPromesa(id1);                               
        $.when(promesa).then(function(respuesta){ 
			bcsComunes.ventanaOverlay($scope.winID, false);
			pccoc.opcionesCobroComisionOptions.dataSource.read();
        });
	};
	
	$scope.pccoc.eliminarOpcionCobro = function(ListaEntrada) {
		
		if(ListaEntrada == undefined){
			bcsComunes.msg("error","error"," Seleccione registros");
			return;
		}
		
		var t = ListaEntrada.length>1?
		bcsAlerta.crear("PREGUNTA_SI_NO","Se eliminarán los "+ListaEntrada.length+" registros seleccionados." ,$scope.winID):
		bcsAlerta.crear("PREGUNTA_SI_NO","Se eliminará "+ListaEntrada.length+" registro seleccionado." ,$scope.winID);
		
		t .then(function(result)
		{
			bcsComunes.ventanaOverlay($scope.winID, true);

			if (result === OPCIONES_BCSALERTA.NO)
			{
				bcsComunes.ventanaOverlay($scope.winID, false);
				return;
			}
			
			let opciones = [];
			
			ListaEntrada.forEach(element=>{
				opciones.push({IdOpcionCobro : element.IdOpcionCobro});
			});
			
			var datos = {
				p_IdEmpresa			: $scope.SesionIdEmpresa,
				p_IdUsuario			: $scope.SesionIdUsuario,
				p_IdModulo			: 'CTA',
				p_UsaReglasNegocio	: 'N',
		
				p_Opciones			: opciones,
			};
			
			bcsServiciosCuentaInversion.EliminarCobroComisionOpcionCobro(datos).$promise.then(
				function(result) {
					var msg = "";
					if (result.P_OUT_ADVERTENCIASCONTADOR){
						msg = "La operación tiene advertencias";
					}
					else if (result.P_OUT_ERRORESCONTADOR){
						msg = "Error al eliminar opción/es";
					}
					if(msg!=""){
						bcsComunes.ventanaMsg({
							mensaje: msg,
							conflictos:util.parseServerResponse(result)
						}, $scope.winID);
					}else{
						bcsComunes.ventanaData( $scope.winID,true);
						bcsComunes.msg("info","info","Opciones Eliminadas");
						bcsComunes.ventanaOverlay($scope.winID, false);
						pccoc.opcionesCobroComisionOptions.dataSource.read();

					}

				},
				function(result){
					bcsComunes.msg("error","error","Error al eliminar opción");
					bcsComunes.ventanaData( $scope.winID,true);
					bcsComunes.ventanaOverlay($scope.winID, false);
				}
			);
			
			pccoc.opcionesCobroComisionOptions.dataSource.read();
		});	
	};
	
	obj.getSchemaOpcionesCobroComision = function() {
		return {
			model: {
				fields : {
					IdOpcionCobro					: { type : "number"},
					OpcionCobro						: { type : "number"},
					DetalleIndicador				: { type : "string"},
					PortafolioAPV 					: { type : "string"},
					IdClasificacionTipoInstrumento	: { type : "string"},
					NombreMercado					: { type : "string"},
					PorcentajeCobro					: { type : "number"},
					IdEstado						: { type : "string"},
					NombreEstado					: { type : "string"},
					FechaVigencia					: { type : "date"},
				}
			}
		};
	};
	
	obj.getDataSourceOpcionesCobroComision = function(){
		return new bcs.DataSource({ 
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				sort: [
	                  ],
				pageSize : 200,
				transport: {
					read: {
						url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=702158",
						dataType: "json"
					}
				},
				schema : obj.getSchemaOpcionesCobroComision()
			},
			
		});
	};
	
	obj.getConfiguracionColumna = function(campo,extras) {
		if (!configuracionColumnas()[campo]) console.log("FALTA COLUMNA " + campo);
		return _.extend( {}, configuracionColumnas()[campo], extras);
	};
	
	pccoc.opcionesCobroComisionOptions = {
		userSettings:true,
		sortable: true,
  		reorderable: true,
  		filterable: true,
		groupable: true,
		pageable: true,
		selectable: "multiple, row",
		reorderable: true,
		allowCopy: true,
		resizable: true,
  		autoBind: true,
  		scrollable: true,
  		height:450,
		dataSource: obj.getDataSourceOpcionesCobroComision(),
	
		columns: [
		          obj.getConfiguracionColumna("IdOpcionCobro",{ groupable: true})
		          ,obj.getConfiguracionColumna("OpcionCobro",{ groupable: true})
		          ,obj.getConfiguracionColumna("DetalleIndicador",{ groupable: true})
		          ,obj.getConfiguracionColumna("OpcionCobro",{ groupable: true})
		          ,obj.getConfiguracionColumna("EsAPV",{ groupable: true})
		          ,obj.getConfiguracionColumna("IdClasificacionTipoInstrumento",{ groupable: true})
		          ,obj.getConfiguracionColumna("NombreMercado",{ groupable: true})
		          ,obj.getConfiguracionColumna("PorcentajeCobro",{ groupable: true})	
		          ,obj.getConfiguracionColumna("IdEstado",{ groupable: true})			
		          ,obj.getConfiguracionColumna("NombreEstado",{ groupable: true})			
		          ,obj.getConfiguracionColumna("FechaVigenciaDesde",{ groupable: true})			  
		]
	};
			
	pccoc.buscar = function buscar() {
		let filters = new Array();
		
		if(pccoc.Estado.seleccionado.IdEstado != 0){
			filters.push({ field: "NombreEstado", operator: "eq", value: pccoc.Estado.seleccionado.NombreEstado });
		}
		
		pccoc.opcionesCobroComisionOptions.dataSource.filters(filters);

    };
});
