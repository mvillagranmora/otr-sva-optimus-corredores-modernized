angular.module('ModuloCuentaInversion').controller('BlotterCobroComisionesCtrl',
		function($scope,$timeout,$modal,$state,bcsComunes,$translate,bcsServicioPlataforma,bcsAlerta,OPCIONES_BCSALERTA,$rootScope, bcsServiciosCuentaInversion  ){
	
	var bcc = this
	var obj = {};
	$scope.data = {};
	$scope.filtros = {};
	bcc.filtro	= {};

	bcc.idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	$scope.parametros = bcsComunes.ventanaParams(bcc.idVentana);
	$scope.winID = bcsComunes.ventanaWinId($scope);
	let fechaSistema = util.dateDS($scope.$root.SesionFechaSistema,"FechaSistema","CALL");

	bcc.parametros = function (){
		bcsComunes.ventanaOverlay(bcc.idVentana, true);
		var id1 = bcsComunes.ventanaAbrir("ParametrosCobroComision", {},false,false,bcc.idVentana); 
		var promesa=bcsComunes.ventanaPromesa(id1);                               
        promesa.then(function(){ 
        	bcsComunes.ventanaOverlay(bcc.idVentana, false);
    	}); 
	};
	
/******************************************************/
/*** FILTROS										***/
/******************************************************/
	
	bcc.optionsFecha = {};
	bcc.validador = null;
	
	$scope.data.filtrosInstrumentos = {
		IdClasificacionTipoInstrumento : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": ""},
		IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value"   : "" }
	};
	
	$scope.oSocioNegocio = {
		filtros: {
			IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},
	};
	
	$scope.oProducto = {
		idActual : false,
		seleccionado : false,			
	};
	
	$scope.oPortafolio = {
		filtros: {
			IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 },
			IdProductoAgrupador : { "field": "IdProductoAgrupador","operator": "eq", "value"   :  0 }
		},
	};

	$scope.bcc.Estado={
		tipo          : "combobox-estado-cuenta-inversion",
		configuracion : {},
		filtros       : {
			Nombre : {field   : "NombreEntidad", operator: "eq", value   : "CTA.CobroComisionRegistros"}
		},
		idActual      : false,
		seleccionado  : false,
		idDefecto     : '',
		idPostFiltro  :	false
	};
	
	$scope.bcc.Periodos={
		tipo          : "combobox-periodos-comisiones",
		configuracion : {},
		idActual      : false,
		seleccionado  : false,
		idDefecto     : '',
		idPostFiltro  :	false
	};

	var configuracionColumnas = function() {
		return {
			RutCliente			:{field: "RutCliente",			title: $translate.instant('UI_RUT_CLIENTE'),		width: "75px",	align: "left"},
			Nombre				:{field: "Nombre",				title: $translate.instant('UI_NOMBRE_CLIENTE'),		width: "75px",	align: "left"},
			IdPortafolio		:{field: "IdPortafolio",		title: $translate.instant('UI_PORTAFOLIO'),			width: "75px",	align: "left", hidden: "true"},
			Portafolio			:{field: "Portafolio",			title: $translate.instant('UI_PORTAFOLIO'),			width: "75px",	align: "left"},
			Producto			:{field: "Producto",			title: $translate.instant('UI_PRODUCTO'),			width: "75px",	align: "left"},
			MontoComision		:{field: "MontoComision",		title: $translate.instant('UI_MONTO_COMISION'),		width: "75px",	align: "left", template : " <label> #:  (util.bcs_formato(MontoComision,'GEN','Monto','"+$scope.SesionIdMonedaSistema+"', '', '')).valor #</label>"},
			EstadoComision		:{field: "EstadoComision",		title: $translate.instant('UI_ESTADO'),				width: "75px",	align: "left", hidden: "true"},
			NombreEstado		:{field: "NombreEstado",		title: $translate.instant('UI_ESTADO'),				width: "75px",	align: "left"},

		};
	};
	
	obj.getConfiguracionColumna = function(campo,extras) {
		if (!configuracionColumnas()[campo]) console.log("FALTA COLUMNA " + campo);
		return _.extend( {}, configuracionColumnas()[campo], extras);
	};
	
	obj.getSchemaRegistrosCobroComision = function() {
		return {
			model: {
				fields : {
					RutCliente		: { type : "string"},
					Nombre			: { type : "string"},
					IdPortafolio	: { type : "number"},
					Portafolio 		: { type : "string"},
					Producto 		: { type : "string"},
					MontoComision	: { type : "number"},
					EstadoComision	: { type : "string"},
					NombreEstado	: { type : "string"},
				}
			}
		};
	};
	
	var ds = new bcs.DataSource({ 
		type : "call",
		definition : {
			callTransaction	: "702164",
			serverFiltering : false, 
			serverPaging : false, 
			serverSorting : false,
			callParameters  : {},
			callResultSet   : "P_OUT_TABLA",
			sort: {},
			pageSize : 200,
			schema : obj.getSchemaRegistrosCobroComision(),
			aggregate: []
		},			
	});
	
	
	$scope.defaultOptions = {
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
      		autoBind: false,
      		scrollable: true,
      		height:450,
	  		dataSource: ds,
		
			columns: [
			          obj.getConfiguracionColumna("RutCliente",{ groupable: true})
			          ,obj.getConfiguracionColumna("Nombre",{ groupable: true})
			          ,obj.getConfiguracionColumna("IdPortafolio",{ groupable: true})
			          ,obj.getConfiguracionColumna("Portafolio",{ groupable: true})
			          ,obj.getConfiguracionColumna("Producto",{ groupable: true})
			          ,obj.getConfiguracionColumna("MontoComision",{ groupable: true})
			          ,obj.getConfiguracionColumna("EstadoComision",{ groupable: true})
			          ,obj.getConfiguracionColumna("NombreEstado",{ groupable: true})
	
			]
	};
	
	bcc.buscar = function buscar() {
		var datos = {};		

		bcsComunes.ventanaOverlay($scope.winID, true);
		datos.p_IdEmpresa = $scope.SesionIdEmpresa;
		datos.p_IdUsuario = $scope.SesionIdUsuario;
		datos.p_IdModulo  ='CUS';
		datos.p_UsaReglasNegocio = 'N';
		datos.p_Cliente 	 	 = $scope.oSocioNegocio.seleccionado.IdCliente;
		datos.p_Portafolio 		 = $scope.oPortafolio.seleccionado.IdPortafolio;
		datos.p_Periodo			 = bcc.Periodos.idActual;
		datos.p_FechaDesde 		 = util.dateSPdate(bcc.filtro.FechaDesde);
		datos.p_FechaHasta 		 = util.dateSPdate(bcc.filtro.FechaHasta);
		datos.p_IdEstado         = bcc.Estado.seleccionado.IdEstado == 0 ? "" : bcc.Estado.seleccionado.IdEstado;
		datos.p_IdProductoAgrupador	 = $scope.oProducto.idActual || 0;
		ds.setCallParameters(datos);

    	bcsComunes.ventanaOverlay($scope.winID, false);
		
	};

	bcc.limpiar = function (){
		$scope.oSocioNegocio.idActual = false;
		$scope.oPortafolio.idActual = false;
		bcc.Estado.idActual = 0;
		$scope.oProducto = 0;
		bcc.Periodos = false;
		bcc.filtro.FechaDesde = new Date('0001-01-01');
		bcc.filtro.FechaHasta = new Date('0001-01-01');
		
	};
	
	$scope.validadorPeriodo = function (fechaHasta, fechaSistema) {
		var dateSistema = new Date(fechaSistema);
   	 	var dateHasta = new Date(fechaHasta);

	
    
    		return dateSistema >= dateHasta;
	}
	
	$scope.bcc.editarRegistro = function(ListaEntrada) {
		let params = {};
		
		if(ListaEntrada == undefined){
			bcsComunes.msg("error","error"," Seleccione 1 registro");
			return;
		}else if(ListaEntrada.length>1){
			bcsComunes.msg("error","error"," Seleccione s�lo 1 registro");
			return;
		}
		
		params.Nombre = ListaEntrada[0].Nombre;
		params.RutCliente = ListaEntrada[0].RutCliente;
		params.IdPortafolio = ListaEntrada[0].IdPortafolio;
		params.MontoComision = ListaEntrada[0].MontoComision;
		params.Portafolio = ListaEntrada[0].Portafolio
		params.EstadoComision = ListaEntrada[0].EstadoComision;
		params.Periodo = bcc.Periodos.seleccionado.IdPeriodo
				
		bcsComunes.ventanaOverlay($scope.winID, true);   
		var id1 = bcsComunes.ventanaAbrir("EditarRegistrosCobroComision",params,
			false,false,$scope.winID); 
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        $.when(promesa).then(function(){ 
			bcsComunes.ventanaOverlay($scope.winID, false);
			ds.read();
	        });
	};
	
	$scope.bcc.EliminarRegistros = function(ListaEntrada) {
		let datos = {};
		datos.p_IdEmpresa 		 = $scope.SesionIdEmpresa;
		datos.p_IdUsuario 		 = $scope.SesionIdUsuario;
		datos.p_IdModulo		 = "CTA";
		datos.p_UsaReglasNegocio = 'N';
		datos.p_FechaDesde 		 = util.dateSPdate(bcc.filtro.FechaDesde);
		datos.p_FechaHasta 		 = util.dateSPdate(bcc.filtro.FechaHasta);
		datos.p_Periodo          =  bcc.Periodos.seleccionado.IdPeriodo;
		
		let portafolios = "";
		
		if(ListaEntrada == undefined){
			bcsComunes.msg("error","error"," Seleccione registros");
			return;
		}
		if(ListaEntrada.length == 1){
			datos.p_IdPortafolios = ListaEntrada[0].IdPortafolio
		}
		if(ListaEntrada.length > 1){

			ListaEntrada.forEach(por => {
				portafolios += por.IdPortafolio+ ";";
				
			});

			datos.p_IdPortafolios = portafolios;
		}
		
		var t = ListaEntrada.length>1?
		bcsAlerta.crear("PREGUNTA_SI_NO","¿Está Seguro que desea eliminar los "+ListaEntrada.length+" registros seleccionados? este proceso no se puede revertir" ,$scope.winID):
		bcsAlerta.crear("PREGUNTA_SI_NO","Se eliminará registro seleccionado ¿ Esta seguro ? este proceso no se puede revertir." ,$scope.winID);

		t .then(function(result)
		{
			bcsComunes.ventanaOverlay($scope.winID, true);

			if (result === OPCIONES_BCSALERTA.NO)
			
			{
				ds.read()
				bcsComunes.ventanaOverlay($scope.winID, false);
				return;
			}
			if (result === OPCIONES_BCSALERTA.SI){
				bcsServiciosCuentaInversion.EliminarCobroComisionRegistros(datos).$promise.then(function(respuesta){
					bcsComunes.ventanaOverlay($scope.winID, false);
					  
					if (respuesta.P_OUT_ERRORESCONTADOR == 0){
						
						bcsComunes.ventanaMsg({
							conflictos:util.parseServerResponse(respuesta)
						},$scope.winID);
						ds.read();
		
					} else{
						if (respuesta.P_OUT_ERRORESCONTADOR > 0){
							
							bcsComunes.ventanaMsg({
								mensaje:$translate.instant(""),
								conflictos:util.parseServerResponse(respuesta)
							}, $scope.winID);
							ds.read();
							
						} else if (respuesta.P_OUT_ADVERTENCIASCONTADOR > 0){
			
							bcsComunes.ventanaMsg({
								mensaje:$translate.instant(""),
								conflictos:util.parseServerResponse(respuesta)
							}, $scope.winID);
							ds.read();
						}
					}
					
				},function(respuesta){
					bcsComunes.ventanaOverlay($scope.winID, false);
					bcsComunes.msg("error","Error",'UI_ERROR_ELIMINACION');
					bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				});
			}
		});		
	};
	
	$scope.bcc.ProcesarRegistros = function(ListaEntrada) {
		if(ListaEntrada == undefined){
			bcsComunes.msg("error","error"," Seleccione registros");
			return;
		}
		
		var t = ListaEntrada.length>1?
		bcsAlerta.crear("PREGUNTA_SI_NO","Se procesarán los "+ListaEntrada.length+" registros seleccionados. ¿Desea continuar?" ,$scope.winID):
		bcsAlerta.crear("PREGUNTA_SI_NO","Se procesará "+ListaEntrada.length+" registro seleccionado. ¿Desea continuar?" ,$scope.winID);
		
		t .then(function(result)
		{
			bcsComunes.ventanaOverlay($scope.winID, true);

			if (result === OPCIONES_BCSALERTA.NO)
			{
				bcsComunes.ventanaOverlay($scope.winID, false);
				return;
			}
			if (result === OPCIONES_BCSALERTA.SI){
				let params = {};
				params.p_IdEmpresa 		 = $scope.SesionIdEmpresa;
				params.p_IdUsuario 		 = $scope.SesionIdUsuario;
				params.p_IdModulo		 = "CTA";
				params.p_UsaReglasNegocio	 = 'N';
				params.p_EsProcesoMasivo 	 = "N";
				
				let filtros = {};
				filtros.IdCliente 	 	 = $scope.oSocioNegocio.seleccionado.IdCliente || 0;
				filtros.IdPortafolio 		 = $scope.oPortafolio.seleccionado.IdPortafolio || 0;
				filtros.IdProductoAgrupador	 = $scope.oProducto.idActual || 0;
				filtros.IdEstado		 = bcc.Estado.seleccionado.IdEstado == 0 ? "" : bcc.Estado.seleccionado.IdEstado;
				filtros.FechaDesde = util.dateSPdate(bcc.filtro.FechaDesde);
				filtros.FechaHasta = util.dateSPdate(bcc.filtro.FechaHasta);
				filtros.Periodo 		 = bcc.Periodos.seleccionado.IdPeriodo || 0;
				params.p_Filtros = filtros;
				
				params.p_PortafoliosSeleccionados = [];
				
				$.each(ListaEntrada,function(key,value){
					params.p_PortafoliosSeleccionados.push({IdPortafolio: value.IdPortafolio});
				});
				
				bcsServiciosCuentaInversion.ProcesarCobroComisionPatrimonio(params).$promise.then(function(respuesta){
					bcsComunes.ventanaOverlay($scope.winID, false);
					  
					if (respuesta.P_OUT_ERRORESCONTADOR == 0){
						
						bcsComunes.ventanaMsg({
							conflictos:util.parseServerResponse(respuesta)
						},$scope.winID);
						ds.read();
		
					} else{
						if (respuesta.P_OUT_ERRORESCONTADOR > 0){
							
							bcsComunes.ventanaMsg({
								mensaje:$translate.instant(""),
								conflictos:util.parseServerResponse(respuesta)
							}, $scope.winID);
							ds.read();
							
						} else if (respuesta.P_OUT_ADVERTENCIASCONTADOR > 0){
			
							bcsComunes.ventanaMsg({
								mensaje:$translate.instant(""),
								conflictos:util.parseServerResponse(respuesta)
							}, $scope.winID);
							ds.read();
						}
					}
					
				},function(respuesta){
					bcsComunes.ventanaOverlay($scope.winID, false);
					bcsComunes.msg("error","Error",'UI_ERROR_PROCESAR');
					bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				});
			}
		});	
	};
	
	$scope.bcc.ProcesarTodo = function() {
		
		if(ds.view() == undefined || !ds.view() || ds.view().length === 0){
			bcsComunes.msg("error","error"," No se encontraron registros");
			return;
		}
		
		var t = bcsAlerta.crear("PREGUNTA_SI_NO","Se procesarán todos los registros filtrados. ¿Desea continuar?" ,$scope.winID)
		
		t .then(function(result)
		{
			bcsComunes.ventanaOverlay($scope.winID, true);

			if (result === OPCIONES_BCSALERTA.NO)
			{
				bcsComunes.ventanaOverlay($scope.winID, false);
				return;
			}
			if (result === OPCIONES_BCSALERTA.SI){
				let params = {};
				params.p_IdEmpresa 		 = $scope.SesionIdEmpresa;
				params.p_IdUsuario 		 = $scope.SesionIdUsuario;
				params.p_IdModulo		 = "CTA";
				params.p_UsaReglasNegocio	 = 'N';
				params.p_EsProcesoMasivo 	 = "S";
				
				let filtros = {};
				filtros.IdCliente 	 	 = $scope.oSocioNegocio.seleccionado.IdCliente || 0;
				filtros.IdPortafolio 		 = $scope.oPortafolio.seleccionado.IdPortafolio || 0;
				filtros.IdProductoAgrupador	 = $scope.oProducto.idActual || 0;
				filtros.IdEstado		 = bcc.Estado.seleccionado.IdEstado == 0 ? "" : bcc.Estado.seleccionado.IdEstado;
				filtros.FechaDesde = util.dateSPdate(bcc.filtro.FechaDesde);
				filtros.FechaHasta = util.dateSPdate(bcc.filtro.FechaHasta);
				filtros.Periodo 		 = bcc.Periodos.seleccionado.IdPeriodo || 0;
				params.p_Filtros = filtros;
				
				bcsServiciosCuentaInversion.ProcesarCobroComisionPatrimonio(params).$promise.then(function(respuesta){
					bcsComunes.ventanaOverlay($scope.winID, false);
					  
					if (respuesta.P_OUT_ERRORESCONTADOR == 0){
						
						bcsComunes.ventanaMsg({
							conflictos:util.parseServerResponse(respuesta)
						},$scope.winID);
						ds.read();
		
					} else{
						if (respuesta.P_OUT_ERRORESCONTADOR > 0){
							
							bcsComunes.ventanaMsg({
								mensaje:$translate.instant(""),
								conflictos:util.parseServerResponse(respuesta)
							}, $scope.winID);
							ds.read();
							
						} else if (respuesta.P_OUT_ADVERTENCIASCONTADOR > 0){
			
							bcsComunes.ventanaMsg({
								mensaje:$translate.instant(""),
								conflictos:util.parseServerResponse(respuesta)
							}, $scope.winID);
							ds.read();
						}
					}
					
				},function(respuesta){
					bcsComunes.ventanaOverlay($scope.winID, false);
					bcsComunes.msg("error","Error",'UI_ERROR_PROCESAR');
					bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				});
			}
			
		});	
	};
	
			/******************************************************/
	/*** BOTON GENERAR INFORME PATRIMONIO DIARIO - INFORME RESUMEN COBRO 	***/
	/******************************************************/

	$scope.bcc.generarInformePatrimonioDiario =function generarInformePatrimonioDiario() {
		let programa = 'GENERAR_INFORME_PATRIMONIO_DIARIO'
		const datos = {
			p_IdEmpresa: $scope.SesionIdEmpresa,
			p_IdUsuario: $scope.SesionIdUsuario,
			p_IdModulo: 'CUS',
			p_TipoPrograma: [{
				IdInterno: 0,
				IdTipoPrograma: programa,
				SegundosDesfase: 0
			}],
			p_Parametro:  [{
					IdInterno : 0,
					NombreParametro : 'IdEmpresa',
					Valor : $scope.SesionIdEmpresa
				},{
					IdInterno : 0,
					NombreParametro : 'IdUsuario',
					Valor : $scope.SesionIdUsuario
				},{
					IdInterno : 0,
					NombreParametro : 'IdModulo',
					Valor : 'CUS'
				},{
					IdInterno : 0,
					NombreParametro : 'Archivo',
					ValorBlob : 'GenerarInformePatrimonioDiario'
				},
				{
					IdInterno : 0,
					NombreParametro : 'IdCliente',
					ValorBlob :	$scope.oSocioNegocio.seleccionado.IdCliente || 0
				},
				{
					IdInterno : 0,
					NombreParametro : 'IdPortafolio',
					ValorBlob :	$scope.oPortafolio.seleccionado.IdPortafolio || 0
				},
				{
					IdInterno : 0,
					NombreParametro : 'IdEstado',
                    			ValorBlob :  bcc.Estado.seleccionado.IdEstado || ''
				},
				{
					IdInterno : 0,
					NombreParametro : 'IdProductoAgrupador',
					ValorBlob :	 $scope.oProducto.seleccionado.IdProductoAgrupador || 0
                },
                {
                    IdInterno : 0,
                    NombreParametro : 'Periodo',
                    ValorBlob :  bcc.Periodos.seleccionado.IdPeriodo|| 0
				},
				{
					IdInterno : 0,
					NombreParametro : 'FechaDesde',
					ValorBlob :	util.dateSPdate(bcc.filtro.FechaDesde) || null
				},
				{
					IdInterno : 0,
					NombreParametro : 'FechaHasta',
					ValorBlob :	util.dateSPdate(bcc.filtro.FechaHasta) || null
				}]
		}
	
		bcsServicioPlataforma.Servicios().InsertarProcesoBatch(datos).$promise.then(
			function(result) {
				if(result.P_OUT_ERRORESCONTADOR>0){
					bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
				}else{
					const fechaLocal = util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion,"FechaHora","CALL");
					bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO').replace("::IdPrograma",result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion",util.formatoSecondate(fechaLocal)),
					conflictos : util.parseServerResponse(result)}, $scope.winID);
				}
			},
			function(){
				bcsComunes.ventanaOverlay($scope.winID,false);
				bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
			}
		);
	
	};	

	$scope.bcc.generarInformeResumenCobro =function generarInformeResumenCobro() {
		let programa = 'GENERAR_INFORME_RESUMEN_COBRO'
		const datos = {
			p_IdEmpresa: $scope.SesionIdEmpresa,
			p_IdUsuario: $scope.SesionIdUsuario,
			p_IdModulo: 'CUS',
			p_TipoPrograma: [{
				IdInterno: 0,
				IdTipoPrograma: programa,
				SegundosDesfase: 0
			}],
			p_Parametro:  [{
					IdInterno : 0,
					NombreParametro : 'IdEmpresa',
					Valor : $scope.SesionIdEmpresa
				},{
					IdInterno : 0,
					NombreParametro : 'IdUsuario',
					Valor : $scope.SesionIdUsuario
				},{
					IdInterno : 0,
					NombreParametro : 'IdModulo',
					Valor : 'CUS'
				},{
					IdInterno : 0,
					NombreParametro : 'Archivo',
					ValorBlob : 'GenerarInformeResumenCobro'
				},
				{
					IdInterno : 0,
					NombreParametro : 'IdCliente',
					ValorBlob :	$scope.oSocioNegocio.seleccionado.IdCliente || 0
				},
				{
					IdInterno : 0,
					NombreParametro : 'IdPortafolio',
					ValorBlob :	$scope.oPortafolio.seleccionado.IdPortafolio || 0
				},
				{
					IdInterno : 0,
					NombreParametro : 'IdEstado',
		                    	ValorBlob :  bcc.Estado.seleccionado.IdEstado || ''
		                },
		                {
		                    	IdInterno : 0,
		                    	NombreParametro : 'IdProductoAgrupador',
		                    	ValorBlob :  $scope.oProducto.seleccionado.IdProductoAgrupador || 0
		                },
		                {
		                    	IdInterno : 0,
		                    	NombreParametro : 'Periodo',
		                    	ValorBlob :  bcc.Periodos.seleccionado.IdPeriodo|| 0
				},
				{
					IdInterno : 0,
					NombreParametro : 'FechaDesde',
					ValorBlob :	util.dateSPdate(bcc.filtro.FechaDesde) || null
				},
				{
					IdInterno : 0,
					NombreParametro : 'FechaHasta',
					ValorBlob :	util.dateSPdate(bcc.filtro.FechaHasta) || null
				}]
		}
	
		bcsServicioPlataforma.Servicios().InsertarProcesoBatch(datos).$promise.then(
			function(result) {
				if(result.P_OUT_ERRORESCONTADOR>0){
					bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
				}else{
					const fechaLocal = util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion,"FechaHora","CALL");
					bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO').replace("::IdPrograma",result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion",util.formatoSecondate(fechaLocal)),
					conflictos : util.parseServerResponse(result)}, $scope.winID);
				}
			},
			function(){
				bcsComunes.ventanaOverlay($scope.winID,false);
				bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
			}
		);
	
	};
	
	$scope.$watch('bcc.Periodos.seleccionado', function(newValue, oldValue) {
	        if (newValue !== oldValue) {
				if(newValue){
					bcc.opcionesSeleccionadas = null;
					bcc.filtro.FechaDesde = util.dateOdata(newValue.FechaDesde);
					bcc.filtro.FechaHasta = util.dateOdata(newValue.FechaHasta);
					bcc.validador = $scope.validadorPeriodo(util.dateSPdate(bcc.filtro.FechaHasta), fechaSistema)
				}
				if(newValue && oldValue){
					bcc.validador = $scope.validadorPeriodo(util.dateSPdate(bcc.filtro.FechaHasta), fechaSistema)
					bcc.opcionesSeleccionadas = null;
					ds.removeAll();
				}
				else{
					bcc.validador = $scope.validadorPeriodo(util.dateSPdate(bcc.filtro.FechaHasta), fechaSistema)
					bcc.opcionesSeleccionadas = null;
					ds.removeAll();
				};
			
			
			
	        }
	});

	

	$scope.$watch('bcc.validador', function(newValue, oldValue) {
        if (newValue !== oldValue) {
			if(newValue == false){
				bcsComunes.msg("warning","warning"," Fecha actual no permite editar, eliminar y/o procesar registros");
			}
        }
    });
	
});
