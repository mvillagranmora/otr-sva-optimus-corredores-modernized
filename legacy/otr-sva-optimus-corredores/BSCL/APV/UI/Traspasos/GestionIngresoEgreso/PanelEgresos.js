/*************************************************************
 * Purpose	: Panel Cliente Portafolios
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloAPV').controller('PanelEgresosCtrl',	function($scope,
			$translate,
			$resource,
			bcsComunes,
			$timeout,
			$state,
			$modal,
			$http,
			$q,
			bcsConsultasAPV,
			bcsServiciosAPV,
			bcsAlerta,
			OPCIONES_BCSALERTA){
		
		$scope.data = {};	
		//Recupera parametros desde la ventana recibida
		var IdVentana    = bcsComunes.ventanaWinId ($scope);	
		$scope.parametros 	= bcsComunes.ventanaParams($scope.IdVentana);	
		bcsComunes.ventanaAutoOverlay($scope.IdVentana,false,3000);

		var panEgr = this;
		panEgr.datosForm = {};
		panEgr.fn = {};
		panEgr.datasource = {};
		panEgr.ConfiguracionCmbo = {};
		panEgr.consulta = {};
		panEgr.HabilitoBotonesTraspaso = false;
		//panEgr.datosForm.fechaTraspaso = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
		$scope.gesIngEgr.panEgr = panEgr;
		
		
		$scope.panEgr.ConfiguracionCmbo.RazonRetiro = {
				filtros : {
    	    		Codigo1 : { "field"   : "Codigo","operator": "neq", "value"   : "RES" },
    	    		Codigo2 : { "field"   : "Codigo","operator": "neq", "value"   : "REI" }
    	    } 
		};

		//*****************************************************************************************************************/
		//*********************************************** DataSource ******************************************************/
		//*****************************************************************************************************************/
		var defGestionIngresoEgreso = {
			type: "odata",
			definition : {
				serverFiltering : true, // Filtrado de datos en el servidor.
	            serverPaging    : true, // Paginado de los datos en el servidor.
	            serverSorting   : true, // Ordenamiento de los datos en el servidor.
	            pageSize        : 100,
	            sort 			: { field: "IdTraspaso", dir: "DESC" },
	            pageSize        : 10,
	            transport		: {
								read: {
									url 		: bcsConsultasAPV('ConsultarTraspasos'),
									dataType	: "json"
							    }
				},
				schema: {
					model: {
						fields: {
							IdTraspaso				: { type: "number"},
							IdSocioNegocioCli		: { type: "number"},
							IdPersona				: { type: "number"},
							DocumentoIdentidadDefecto : { type: "string"},
							NombreCompleto			: { type: "string"},
							IdSocioNegocioEMP		: { type: "number"},
							RazonSocial				: { type: "string"},
							DocumentoEntidad		: { type: "string"},
							CodigoEntidad			: { type: "number"},
							NombreEntidad			: { type: "string"},
							FechaIngreso		    : { type: "sysdate"},
							FechaEgreso			    : { type: "sysdate"},
							IdProductoAgrupador		: { type: "number"},	
							Producto				: { type: "string"},
							IdPlanTipoInversion		: { type: "number"},
							NombrePlan				: { type: "string"},
							CodigoRazonRetiro		: { type: "string"},
							RazonRetiro				: { type: "string"},
							IdMatrizAgrupadorAPV	: { type: "number"},
							CodigoAgrupador			: { type: "string"},
							IdPropositoPortafolio	: { type: "string"},
							MontoTotalTraspaso		: { type: "number"},
							UFTotalTraspaso			: { type: "number"},
							PorcentajeTotalTraspaso	: { type: "number"},
							MontoDisponible			: { type: "number"},
							MontoOrdenTraspaso		: { type: "number"},
							CodigoTipoTraspaso		: { type: "string"},
							Descripcion				: { type: "string"},
							Dias					: { type: "number"},
							IdEstado				: { type: "string"},
							EstadoNombre			: { type: "string"},
							Canal					: { type: "string"}
						}
					}
				}
			}	
		};

		var defSaldoCuentaInversion = {
			type: "array",
			definition : {
				serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                pageSize        : 10,
                sort 			: false,
				schema: {
					model: {
						fields: {
							NombrePortafolio	: { type: "string"},
							IdProductoAgrupador : { type: "number"},
							Producto			: { type: "string"},
							Instrumento			: { type: "string"},
							IdPortafolio		: { type: "number"},
							IdPlanTipoInversion : { type: "number"},
							NombrePlan			: { type: "string"},
							Regimen				: { type: "string"},
							SaldoDisponible		: { type: "number"},
							IdMoneda			: { type: "string"},
							UFTraspaso			: {	type: "number"},
							Porcentaje			: { type: "number"},								
							CantidadCuotas		: { type: "number"},								
							ValorCuota			: { type: "number"}								
						}
					}
				}
			}	
		};

		var defDetalleCartera = {
			type: "array",
			definition : {
				serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                pageSize        : 10,
                sort 			: false,
				schema: {
					model: {
						fields: {
							TipoMonto	: { type: "string"},
							MontoPesos  : { type: "number"}							
						}
					}
				}
			}	
		};

		var defDetalleRegimen = {
			type: "array",
			definition : {
				serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                pageSize        : 10,
                sort 			: false,
				schema: {
					model: {
						fields: {
							Regimen  		        : { type: "string"},
							CantidadCustodia 		: { type: "number"}			
						}
					}
				}
			}	
		};
		//*****************************************************************************************************************/
		//****************************************** Definicion de Grillas ************************************************/
		//*****************************************************************************************************************/
		var dsGestionEgreso = new bcs.DataSource(defGestionIngresoEgreso);
		panEgr.datasource.GestionIngresoEgreso = {
				dataSource: dsGestionEgreso,
				columns: [
					{field: "IdTraspaso"				,title: $translate.instant('UI_FOLIO_TRASPASO')					, width: "90px", align: "left", sortable : true, editable: false },
					{field: "DocumentoIdentidadDefecto"	,title: $translate.instant('UI_DOCUMENTO_IDENTIDAD')			, width: "110px", align: "left", sortable : true, editable: false},
					{field: "NombreCompleto"			,title: $translate.instant('UI_SOCIO_NEGOCIO')					, width: "150px", align: "rigth", sortable : true, editable: false},
					{field: "DocumentoEntidad"			,title: $translate.instant('UI_DOCUMENTO_IDENTIDAD_DESTINO')	, width: "120px", align: "left", sortable : true, editable: false},
					{field: "NombreEntidad"				,title: $translate.instant('UI_ENTIDAD_DESTINO')				, width: "150px", align: "left" , sortable : true, editable: false},
					{field: "CodigoTipoTraspaso"		,title: $translate.instant('UI_TIPO_TRASPASO')					, width: "80px", align: "left", sortable : true, editable: false},
					{field: "RazonRetiro"				,title: $translate.instant('UI_RAZON_RETIRO')					, width: "80px", align: "left", sortable : true, editable: false},
					{field: "FechaEgreso"				,title:	$translate.instant('UI_FECHA_EGRESO')					, width: "80px", align: "left" , sortable : true, editable: false},
				    {field: "PorcentajeTotalTraspaso"	,title: $translate.instant('UI_PORCENTAJE')						, width: "80px", align: "rigth", sortable : true, editable: false},
					{field: "MontoTotalTraspaso"		,title: $translate.instant('UI_MONTO_PESOS')					, width: "80px", align: "rigth", sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(MontoTotalTraspaso,'ORD','Monto','CLP','','')).valor #</label>"},
					{field: "UFTotalTraspaso" 			,title: $translate.instant('UI_MONTO_UF')						, width: "80px", align: "rigth", sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(UFTotalTraspaso,'ORD','Monto','CLP','','')).valor #</label>"},
					{field: "MontoDisponible" 			,title: $translate.instant('UI_MONTO_DISPONIBLE')				, width: "80px", align: "rigth", sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(MontoDisponible,'ORD','Monto','CLP','','')).valor #</label>"},
					{field: "MontoOrdenTraspaso" 		,title: $translate.instant('UI_MONTO_ORDEN_TRASPASO')			, width: "80px", align: "rigth", sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(MontoOrdenTraspaso,'ORD','Monto','CLP','','')).valor #</label>"},
					{field: "Dias"					    ,title:	$translate.instant('UI_DIAS')							, width: "50px", align: "rigth", sortable : true, editable: false},
					{field: "Canal"					    ,title:	$translate.instant('UI_CANAL')							, width: "50px", align: "rigth", sortable : true, editable: false},
				    {field: "IdEstado"	 				,title: $translate.instant('UI_ESTADO')							, width: "50px", align: "rigth", sortable : true, editable: false, hidden: true},
					{field: "EstadoNombre"	 			,title: $translate.instant('UI_ESTADO')							, width: "100px", align: "rigth", sortable : true, editable: false }
				    ],	
				sortable		: true,
				pageable		: true,
				autoBind		: false,
		    	filterable		: true,
				height			: 265,
			    selectable 		: "multiple row",
			    userSettings	: false,
				navigatable		: true
		};

		var dsSaldoCuentaInversion = new bcs.DataSource(defSaldoCuentaInversion);
		panEgr.datasource.SaldoCuentaInversion = {
				dataSource: dsSaldoCuentaInversion,
				columns: [
					{field: "NombrePortafolio"	,title: $translate.instant('UI_NOMBRE_PORTAFOLIO')	, width: "80px", align: "rigth" , sortable : true, editable: false, hidden: true},
					{field: "Producto"	 		,title: $translate.instant('UI_PRODUCTO')			, width: "80px", align: "rigth" , sortable : true, editable: false },
					{field: "Instrumento" 		,title: $translate.instant('UI_INSTRUMENTO')		, width: "80px", align: "rigth" , sortable : true, editable: false },
					{field: "Regimen"	 		,title: $translate.instant('UI_REGIMEN')			, width: "80px", align: "rigth" , sortable : true, editable: false },
					{field: "IdMoneda"	 		,title: $translate.instant('UI_MONEDA')				, width: "80px", align: "rigth" , sortable : true, editable: false },
					{field: "SaldoDisponible"	,title: $translate.instant('UI_MONTO_PESOS')		, width: "80px", align: "rigth" , sortable : true, editable: false	,template : " <label> #:  (util.bcs_formato(SaldoDisponible,'ORD','Monto',IdMoneda,'','')).valor #</label>"},
				    {field: "UFTraspaso"		,title: $translate.instant('UI_MONTO_UF')			, width: "80px", align: "rigth" , sortable : true, editable: false	,template : " <label> #:  (util.bcs_formato(UFTraspaso,'ORD','Monto','CLP','','')).valor #</label>"},
					{field: "Porcentaje"		,title: $translate.instant('UI_PORCENTAJE')			, width: "80px", align: "rigth" , sortable : true, editable: false	,template : " <label> #:  (util.bcs_formato(Porcentaje,'ORD','Monto','CLP','','')).valor #</label>"},
					{field: "CantidadCuotas"	,title: $translate.instant('UI_NUMERO_CUOTAS_CANTIDAD'), width: "80px", align: "rigth" , sortable : true, editable: false,template : " <label> #:  (util.bcs_formato(CantidadCuotas,'ORD','Monto','CLP','','')).valor #</label>"},
					{field: "ValorCuota"	 	,title: $translate.instant('UI_VALOR_CUOTA_PRECIO')	, width: "80px", align: "rigth" , sortable : true, editable: false	,template : " <label> #:  (util.bcs_formato(ValorCuota,'ORD','Monto','CLP','','')).valor #</label>" }
				    ],	
				sortable		: false,
				pageable		: true,
				autoBind		: true,
				height			: 157,
			    selectable 		: "multiple row",
			    userSettings	: true,
				navigatable		: true
		};

		var dsDetalleCartera = new bcs.DataSource(defDetalleCartera);
		panEgr.datasource.DetalleCartera = {
				dataSource: dsDetalleCartera,
				columns: [
					{field: "TipoMonto"	,title: $translate.instant('UI_TIPO_MONTO'), width: "80px", align: "rigth" , sortable : true, editable: false},
					{field: "MontoPesos"	,title: $translate.instant('UI_MONTO_PESOS'), width: "80px", align: "rigth" , sortable : true, editable: false}
				    ],	
				sortable		: false,
				pageable		: true,
				autoBind		: true,
				height			: 157,
			    selectable 		: "multiple row",
			    userSettings	: false,
				navigatable		: true
		};

		var dsDetalleRegimen = new bcs.DataSource(defDetalleRegimen);
		panEgr.datasource.DetalleRegimen = {
				dataSource: dsDetalleRegimen,
				columns: [
					{field: "Regimen"	,title: $translate.instant('UI_TIPO_AHORRO'), width: "80px", align: "rigth" , sortable : true, editable: false},
					{field: "CantidadCustodia"	,title: $translate.instant('UI_MONTO_PESOS'), width: "80px", align: "rigth" , sortable : true, editable: false},
				    ],	
				sortable		: false,
				pageable		: true,
				autoBind		: true,
				height			: 157,
			    selectable 		: "multiple row",
			    userSettings	: true,
				navigatable		: true
		};
		
		//*****************************************************************************************************************/
		//*********************************************** Consultas *******************************************************/
		//*****************************************************************************************************************/
		panEgr.consulta.GestionIngresoEgreso = function(){
			var filtro = Array();
			filtro.push({ field: "TipoMovimiento" , operator: "eq", value: "EGRESO" });
			filtro.push({ field: "IdEstado" , operator: "neq", value: 'TPD' });
			
			if(panEgr.datosForm.SocioNegocioSeleccionado !== undefined && panEgr.datosForm.SocioNegocioSeleccionado !== false){
				filtro.push({ field: "IdSocioNegocioCli" , operator: "eq", value: Number(panEgr.datosForm.SocioNegocioSeleccionado.IdSocioDeNegocio) });	
			}

			if (panEgr.datosForm.fechaTraspaso != undefined) {
				filtro.push({ field: "FechaIngreso", operator: "eq", value: panEgr.datosForm.fechaTraspaso });
			}

			if(panEgr.TipoTraspasoSeleccionado !== undefined && panEgr.TipoTraspasoSeleccionado !== false){
				filtro.push({ field: "CodigoTipoTraspaso", operator: "eq", value: panEgr.TipoTraspasoSeleccionado.Nombre});
			}
			
			if(panEgr.datosForm.RazonRetiroSeleccionado !== undefined && panEgr.datosForm.RazonRetiroSeleccionado !== false){
				filtro.push({ field: "CodigoRazonRetiro", operator: "eq", value: panEgr.datosForm.RazonRetiroSeleccionado.Codigo});
			}
			
			if(panEgr.RangoDiasTraspasoSeleccionado !== undefined && panEgr.RangoDiasTraspasoSeleccionado !== false){
				if (panEgr.RangoDiasTraspasoSeleccionado.Codigo === "MAYOR20"){
					filtro.push({ field: "Dias", operator: "ge", value: "20"});	
				}
				if (panEgr.RangoDiasTraspasoSeleccionado.Codigo === "MENOR20"){
					filtro.push({ field: "Dias", operator: "le", value: "20"});	
				}
			}
			
			panEgr.datasource.GestionIngresoEgreso.dataSource.filter(filtro);
		};

		panEgr.consulta.ConsultaDetalleTraspaso = function(SeleccionTraspaso){		//desde aquí se llaman a los distintos detalles
			panEgr.consulta.buscarDetalleFondos(SeleccionTraspaso);
			panEgr.consulta.buscarDetalleCartera(SeleccionTraspaso);
		};
		
		panEgr.consulta.buscarDetalleFondos = function(SeleccionTraspaso) {	 //1ra grilla de detalle	
			var filter 			= [];
			var EstadoTraspaso 	= '';
			panEgr.datasource.SaldoCuentaInversionSeleccion = undefined;
			dsSaldoCuentaInversion.data(new Array());
			
			if (SeleccionTraspaso[0].IdCliente){
				filter.push({ field: "IdCliente", operator: "eq", value: Number(SeleccionTraspaso[0].IdCliente) });
			}
			
			TipoAgrupadorCuentaInversion = 'PR';
			EstadoTraspaso = (SeleccionTraspaso[0].IdEstado !== "PRO"  ) ? SeleccionTraspaso[0].IdEstado : '';
			
			//SALDOS
			if (filter.length > 0){
				var filtersSaldos = { 
					p_IdEmpresa 				: $scope.SesionIdEmpresa,				
					p_IdUsuario         		: $scope.SesionIdUsuario,
					p_IdModulo					: 'APV',
					p_UsaReglasNegocio			: 'N',
					p_IdTraspaso				: SeleccionTraspaso[0].IdTraspaso,
					p_TipoAgrupador				: 'PO',
					p_CuentaInversionAConsultar	: [{IdEjecutivo			: null,
													IdCliente			: SeleccionTraspaso[0].IdCliente,
													IdPortafolio  	  	: null,
													IdMoneda      	  	: 'CLP'
												  }],
					p_FechaDesde				: $scope.SesionFechaSistema.slice(0,10) || '0001-01-01',
					p_FechaHasta				: $scope.SesionFechaSistema.slice(0,10) || '0001-01-01',
					p_EstadoTraspaso			: EstadoTraspaso,
					
				};
				
				bcsServiciosAPV.ConsultarCuentaInversionOrdenTraspaso(filtersSaldos).$promise.then(function(respuesta){							
						if (respuesta.P_OUT_ERRORESCONTADOR === 0){						
							var Saldos = Array();							
							var n = respuesta.P_OUT_SALDOPORFECHAMOVIMIENTO.length;
							var RespuestaSaldo = respuesta.P_OUT_SALDOPORFECHAMOVIMIENTO;
							var totalDisponible = 0;

							for(var i=0; i<n ; i++){
								totalDisponible = totalDisponible + RespuestaSaldo[i].SaldoDisponible;
							}
							
							for(var i=0; i<n ; i++){
								if (RespuestaSaldo[i].SaldoDisponible > 0){
								    RespuestaSaldo[i].Porcentaje = Math.round((RespuestaSaldo[i].SaldoDisponible*10000)/totalDisponible)/100;
								}else if (EstadoTraspaso == ''){
							        RespuestaSaldo[i].Porcentaje = 0;
								}
								Saldos.push(RespuestaSaldo[i]);
							}
							dsSaldoCuentaInversion.data(Saldos);
							panEgr.datosForm.kngSaldoTotal = totalDisponible;
							panEgr.datosForm.kngSaldoTotalInvertido = totalDisponible;
						}
						bcsComunes.ventanaOverlay($scope.winID, false);
					},
					function(respuesta){
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_PROBLEMAS_AL_RECUPERAR_SALDOS_CLIENTES"),
							conflictos:util.parseServerResponse(respuesta)
						},$scope.winID);				
					}
				);
			}		
		};

		panEgr.consulta.buscarDetalleCartera = function(SeleccionTraspaso) {	 //2ra grilla de detalle
				var filter = [];
				var filterAut = [];

				if(SeleccionTraspaso[0].IdCliente === false || SeleccionTraspaso[0].IdCliente === undefined){
	    			bcsComunes.msg("warning","",$translate.instant("UI_DEBE_SELECCIONAR_CLIENTE"));
	    			bcsComunes.ventanaOverlay(IdVentana,false);
					return;
				}
		
				//CUSTODIA
				var filtersCustodia = { 
					p_IdEmpresa 						: $scope.SesionIdEmpresa,				
					p_IdUsuario         				: $scope.SesionIdUsuario,
					p_IdModulo							: 'APV',
					p_IdCliente							: SeleccionTraspaso[0].IdCliente,
					p_IdPortafolio						: 0,
					p_IdClasificacionTipoInstrumento	: '',
					p_IdTipoInstrumento					: '',
					p_IdInstrumento						: 0,
					p_IdBoveda							: '',
					p_AgrupaPorBoveda					: 'N',
					p_AgrupaPorRelacion					: 'N',
					p_FechaConsulta						: '0001-01-01',
					p_TipoFechaConsulta					: 'LIQ',
					p_IdRegimen							: 0, 
					p_IdProducto						: 0,
					p_IdPlan							: 0
				};				
					
				bcsServiciosAPV.ConsultarCustodiaPortafolioAPV(filtersCustodia).$promise.then(function(respuesta){		
					if (respuesta.P_OUT_CUSTODIA.length > 0){	
						var Custodia = Array();		
						var Regimen = Array();		

						Custodia.push({ TipoMonto: "Total RV", MontoPesos: Number(respuesta.P_OUT_TOTALRV)});
						Custodia.push({ TipoMonto: "Total FFMM", MontoPesos: Number(respuesta.P_OUT_TOTALFFMM)});

						for(var i=0; i < respuesta.P_OUT_CUSTODIA.length ; i++){
							Regimen.push(respuesta.P_OUT_CUSTODIA[i]);	
						}
					
						dsDetalleCartera.data(Custodia);
						dsDetalleRegimen.data(Regimen);
						
					}else {
						dsDetalleCartera.removeAll();
						dsDetalleRegimen.removeAll();
					}
					
				},
				function(respuesta){
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_PROBLEMAS_AL_RECUPERAR_SALDOS_CLIENTES"),
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);		
					bcsComunes.ventanaOverlay(IdVentana, false);
				});
			};
			

		//*****************************************************************************************************************/
		//*********************************************** Funciones *******************************************************/
		//*****************************************************************************************************************/
		panEgr.fn.EgresoTraspaso = function(ModoVentana){
			var filterVentana = Array();
			
			if(ModoVentana === 'I'){
				filterVentana.ModoVentana = ModoVentana;
			}
			else{
				filterVentana.ModoVentana = ModoVentana;
				filterVentana.IdContratoSAAPV = panEgr.datasource.GestionIngresoEgresoSeleccion[0].IdContratoSAAPV;
				filterVentana.IdTraspaso = panEgr.datasource.GestionIngresoEgresoSeleccion[0].IdTraspaso;
			}
			//if(dsDetalleTraspaso.data().length === 0){
				var IdVentanamodal = bcsComunes.ventanaAbrir("EgresoTraspasoAPV", filterVentana, true, false, IdVentana);
				var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
				promesa.then(function(data){
					//
		        });	
			//}
		};
		
		panEgr.fn.CompletarTraspaso = function(seleccionTraspaso){
			
			if(seleccionTraspaso[0].IdEstado === false || seleccionTraspaso[0].IdEstado !== 'PRO'){
    			bcsComunes.msg("warning","",$translate.instant('UI_DEBE_SELECCIONAR_TRASPASOS_EN_ESTADO_PROCESADO'));
    			bcsComunes.ventanaOverlay(IdVentana,false);
				return;
			}
			var t = bcsAlerta.crear("PREGUNTA_SI_NO","Se cambiará el Estado ha COMPLETADO al Traspaso , ¿Desea cambiar el Estado?" ,IdVentana);
			t .then(function(result)
			{
				if (result === OPCIONES_BCSALERTA.NO)
				{
					bcsComunes.ventanaOverlay(IdVentana,false);
					return;
				}
				
				var datos = { 
						p_IdEmpresa 						: $scope.SesionIdEmpresa,				
						p_IdUsuario         				: $scope.SesionIdUsuario,
						p_IdModulo							: 'APV',
						p_Parametros						: [{
																IdEstado : 'COM',
																IdEmpresa: $scope.SesionIdEmpresa,
																IdTraspaso: seleccionTraspaso[0].IdTraspaso,
																IdContratoSAAPV: seleccionTraspaso[0].IdContratoSAAPV === undefined ? null : seleccionTraspaso[0].IdContratoSAAPV
															  }]
					};				
				
				bcsServiciosAPV.CambiarEstadoTraspaso(datos).$promise.then(function(respuesta){		
					if (respuesta.P_OUT_ERRORESCONTADOR === 0){
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_PROCESO_COMPLETADO"),
							conflictos:util.parseServerResponse(respuesta)
						},$scope.winID);
						panEgr.consulta.GestionIngresoEgreso();	
					}else {
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_PROBLEMAS_AL_CAMBIAR_ESTADO_TRASPASO"),
							conflictos:util.parseServerResponse(respuesta)
						},$scope.winID);		
					}
				},
				function(respuesta){
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_PROBLEMAS_AL_CAMBIAR_ESTADO_TRASPASO"),
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);		
					bcsComunes.ventanaOverlay(IdVentana, false);
				});
			});
			
			
		};
		
		panEgr.fn.Desinversion = function(ModoVentana){
			var filterVentana = Array();
			
			if(!panEgr || !panEgr.datasource || !panEgr.datasource.GestionIngresoEgresoSeleccion || !panEgr.datasource.GestionIngresoEgresoSeleccion[0] || panEgr.datasource.GestionIngresoEgresoSeleccion[0].IdEstado !== "ING"){
				bcsComunes.msg("warning",'',$translate.instant('UI_DEBE_SELECCIONAR_TRASPASOS_EN_ESTADO_INGRESADO'));
				return;
			}
			
			filterVentana.SocioSeleccionado = false;
			
			if(panEgr.datasource.GestionIngresoEgresoSeleccion !== undefined){
				filterVentana.Glosa = panEgr.datasource.GestionIngresoEgresoSeleccion[0].Descripcion;
				filterVentana.ModoVentana = ModoVentana;
				filterVentana.IdContratoSAAPV = panEgr.datasource.GestionIngresoEgresoSeleccion[0].IdContratoSAAPV;
				filterVentana.IdTraspaso = panEgr.datasource.GestionIngresoEgresoSeleccion[0].IdTraspaso;
				filterVentana.IdSocioNegocio = panEgr.datasource.GestionIngresoEgresoSeleccion[0].IdSocioNegocioCli;
				filterVentana.RazonRetiro = panEgr.datasource.GestionIngresoEgresoSeleccion[0].RazonRetiro;

				filterVentana.MontoTotalPesos = panEgr.datasource.GestionIngresoEgresoSeleccion[0].MontoTotalTraspaso;
				filterVentana.MontoTotalUF = panEgr.datasource.GestionIngresoEgresoSeleccion[0].UFTotalTraspaso;
				filterVentana.PorcentajeTotal = panEgr.datasource.GestionIngresoEgresoSeleccion[0].PorcentajeTotalTraspaso;
				filterVentana.Glosa = panEgr.datasource.GestionIngresoEgresoSeleccion[0].Descripcion;
				filterVentana.SocioSeleccionado = true;
				filterVentana.FechaTraspaso = panEgr.datosForm.fechaTraspaso;
				filterVentana.FechaEgreso = panEgr.datasource.GestionIngresoEgresoSeleccion[0].FechaEgreso;
				filterVentana.CodigoTipoTraspaso = panEgr.datasource.GestionIngresoEgresoSeleccion[0].CodigoTipoTraspaso;
				filterVentana.RazonRetiro = panEgr.datasource.GestionIngresoEgresoSeleccion[0].CodigoRazonRetiro;
				filterVentana.CodigoEntidadDestino = panEgr.datasource.GestionIngresoEgresoSeleccion[0].CodigoEntidad;
			}

			var IdVentanamodal = bcsComunes.ventanaAbrir("DesinversionAPV", filterVentana, true, false, IdVentana);
			var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
			promesa.then(function(data){
				panEgr.consulta.GestionIngresoEgreso();	
		    });	
		};
		

		panEgr.fn.Anular = function(items){
			var traspasosAnulacionValidos = [];
			var traspasosAnulacionNoValidos = [];


		for(let i =0; i< panEgr.datasource.GestionIngresoEgresoSeleccion.length; i++){
			let element = panEgr.datasource.GestionIngresoEgresoSeleccion[i];
			if (element.IdEstado === 'ING'){
					traspasosAnulacionValidos.push(element.IdTraspaso)

				}else{
					traspasosAnulacionNoValidos.push(element.IdTraspaso)
				}
		}
			
			
			console.log ("Validos",traspasosAnulacionValidos)
			console.log ("No Validos",traspasosAnulacionNoValidos)
			var EntradaServicio = { 
					p_IdEmpresa 				: $scope.SesionIdEmpresa,				
					p_IdUsuario         		: $scope.SesionIdUsuario,
					p_IdModulo					: 'APV',
					p_TraspasosValidos			: traspasosAnulacionValidos,
					p_TraspasosInValidos		: traspasosAnulacionNoValidos
				};
			console.log(JSON.stringify(EntradaServicio));
				
			bcsServiciosAPV.AnularTraspasosEgresosMasivo(EntradaServicio).$promise.then(function(respuesta){							
						if (respuesta.P_OUT_ERRORESCONTADOR === 0){						
							//Se levanta ventana de success
							bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_SUCCESS_ANULAR_TRASPASO_EGRESO")
						},$scope.winID);
						buscarLuegoDeCerrarVentana($scope.winID);
						
						}else{
							//se levanta ventana de error
							bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_ERROR_AL_ANULAR_TRASPASO_EGRESO"),
							conflictos:util.parseServerResponse(respuesta)
						},$scope.winID);
						}
						bcsComunes.ventanaOverlay($scope.winID, false);
					},
					function(respuesta){
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_ERROR_AL_ANULAR_TRASPASO_EGRESO"),
							conflictos:util.parseServerResponse(respuesta)
						},$scope.winID);	
					bcsComunes.ventanaOverlay($scope.winID, false);
			
					}
				); 
			
		}

		
		var buscarLuegoDeCerrarVentana = function(id){
			 var promesa=bcsComunes.ventanaPromesa(id);                               
		     $.when(promesa).then(function(data){
				panEgr.consulta.GestionIngresoEgreso();	
			 });
		}
	
		panEgr.fn.AgregoFormaPago = function(items){
			var traspasos = [];
			var traspaso = Array();					
			var n = panEgr.datasource.SaldoCuentaInversion.dataSource.data().length;
			var totalSaldo = 0;
			var totalTraspasos = 0;
			var totalEgreso = 0;
			var valClienteOK = true;
			var valRazonOK = true;
			var selCliente = '';
			var selRazon = '';
			
			if(panEgr.datasource.GestionIngresoEgresoSeleccion[0].IdEstado !== "ING"){
				bcsComunes.msg("warning",'',$translate.instant('UI_DEBE_SELECCIONAR_TRASPASOS_EN_ESTADO_INGRESADO'));
				return;
			}
			
			for(var i=0; i < n; i++){
				if(panEgr.datasource.SaldoCuentaInversion.dataSource.data()[i].SaldoDisponible !== undefined){
					totalSaldo = totalSaldo + panEgr.datasource.SaldoCuentaInversion.dataSource.data()[i].SaldoDisponible;	
				}
			}
			
			if(totalSaldo === 0){
				bcsComunes.msg("warning",'warning',$translate.instant('UI_SALDO_EN_CERO'));
				return
			}
			
			$.each(items,function(key,value){
				if (selCliente === "") selCliente = value.IdPersonaCli;
				else if (selCliente !== value.IdPersonaCli) valClienteOK = false;

				if (selRazon === "") selRazon = value.RazonRetiro;
				else if (selRazon !== value.RazonRetiro) valRazonOK = false;
				if (panEgr.datasource.GestionIngresoEgresoSeleccion[0].CodigoTipoTraspaso === "TOTAL")
					totalTraspasos = totalTraspasos + totalSaldo;
				else{
					if(value.MontoOrdenTraspaso > 0){
						totalTraspasos = totalTraspasos + value.MontoOrdenTraspaso;
					}
//					if(value.MontoTotalTraspaso > 0){
//						totalTraspasos = totalTraspasos + value.MontoTotalTraspaso;
//					}else if(value.PorcentajeTotalTraspaso > 0){
//						totalTraspasos = (totalSaldo * value.PorcentajeTotalTraspaso)/100;
//					}else if(value.UFTotalTraspaso > 0){
//						totalTraspasos = totalTraspasos + value.UFTotalTraspaso;
//					}	
				}
					
			});	
			
			if(totalTraspasos === 0){
				bcsComunes.msg("warning",'warning',$translate.instant('UI_MONTO_ORDEN_EN_CERO'));
				return
			}
			
			if (valClienteOK === false || valRazonOK === false){
				if (valClienteOK === false) bcsComunes.msg("warning",'warning',$translate.instant('UI_DEBE_SELECCIONAR_SOLO_UN_CLIENTE'));
				if (valRazonOK === false) bcsComunes.msg("warning",'warning',$translate.instant('UI_DEBE_SELECCIONAR_SOLO_UNA_RAZON'));
				return;
			}
			
			if(totalSaldo >= totalTraspasos)
				totalEgreso = totalTraspasos;
				
			if(totalSaldo <= totalTraspasos)
				totalEgreso = totalSaldo;
				
			$.each(items,function(key,value){
				traspaso = Array();	
				
				traspaso.IdContratoSAAPV 	= value.IdContratoSAAPV;
				traspaso.IdTraspaso 		= value.IdTraspaso;
				
				if (   value.CodigoRazonRetiro === "JUB"
					|| value.CodigoRazonRetiro === "TRA"
					|| value.CodigoRazonRetiro === "HER"){
					//IdPersona es el de entidad, y IdPersonaCli es del cliente valga la redundancia
					traspaso.IdPersona		= value.IdPersona;
				}else{
					traspaso.IdPersona 		= value.IdPersonaCli;
				}
				
				traspaso.IdPersonaCliente	= value.IdPersonaCli;
				traspaso.IdPersonaEntidad	= value.IdPersona;
				
				traspaso.IdSocioNegocioCli 	= value.IdSocioNegocioCli;
				traspaso.CodigoEntidad 		= String(value.CodigoEntidad);
				traspaso.MontoPesos 		= value.MontoOrdenTraspaso;
				traspaso.CodigoTipoRetiro 	= value.CodigoRazonRetiro;
				
				traspasos.push(traspaso);
			});
				
			//if(dsDetalleTraspaso.data().length === 0){
				var IdVentanamodal = bcsComunes.ventanaAbrir("FormaDePagoAPV", traspasos, true, false, IdVentana);
				var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
				promesa.then(function(data){
					//
		        });	
			//}
		};
		
		panEgr.fn.Limpiar = function(){

			panEgr.datosForm.SocioNegocioIdActual 	= false;
			panEgr.datosForm.fechaTraspaso			= undefined;
			panEgr.RangoDiasTraspasoidActual		= false;
			panEgr.TipoTraspasoIdActual				= false;
			panEgr.datosForm.RazonRetiroIdActual	= false;
			panEgr.datasource.GestionIngresoEgreso.dataSource.removeAll();
			panEgr.datasource.SaldoCuentaInversion.dataSource.removeAll();
			panEgr.datasource.DetalleCartera.dataSource.removeAll();
			panEgr.datasource.DetalleRegimen.dataSource.removeAll();
			panEgr.datasource.GestionIngresoEgresoSeleccion = undefined;
		}
		
		panEgr.fn.ValidoBotones = function(tipo){
			if(tipo === 'DT'){
				if(panEgr.datasource.GestionIngresoEgresoSeleccion !== undefined){
					if(panEgr.datasource.GestionIngresoEgresoSeleccion[0].IdEstado === 'PEN'){
						return true;
					}
					else{
						return false;
					}
				}else{
					return false;
				}
			}
		}
		
		panEgr.VentanaIngresoTraspaso = function(){
			var IdVentanamodal = bcsComunes.ventanaAbrir("RegistroTraspasoAPV", { modoVentana : 'I', CodigoSolicitud : 'TRA', tipoTraspaso: 'EGR',SocioNegocio:panEgr.datosForm.SocioNegocioSeleccionado}, true, false, IdVentana);
			var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
			promesa.then(function(data){
				panEgr.consulta.GestionIngresoEgreso();	
	        });
		};
		panEgr.VentanaContratoSAAPV = function(modoVentana){
			var filterVentana = Array();
			filterVentana.modoVentana = modoVentana;
			if(modoVentana === 'I'){
				var IdVentanamodal = bcsComunes.ventanaAbrir("ContratoSAAPV", filterVentana, true, false, IdVentana);
			}else if(modoVentana === 'M'){
				if(panEgr.datasource.GestionIngresoEgresoSeleccion !== undefined){
					if(panEgr.datasource.GestionIngresoEgresoSeleccion[0].IdEstado === 'PDI' && panEgr.datasource.GestionIngresoEgresoSeleccion[0].IdEstadoSolicitud === 'PDA')
						{
							bcsComunes.msg("warning",'warning',$translate.instant('UI_PARA_MODIFICAR_CONTRATO_NO_DEBE_ESTAR_RECHAZADO_PENDIENTE_AUTORIZACION'));
							return;
						}
						else if(panEgr.datasource.GestionIngresoEgresoSeleccion[0].IdEstado === 'REV')
						{
							bcsComunes.msg("warning",'warning',$translate.instant('UI_NO_PUEDE_MODIFICAR_CONTRATO_REVOCADO'));
							return;
						}else if(panEgr.datasource.GestionIngresoEgresoSeleccion[0].IdEstado === 'COM'){
							bcsComunes.msg("warning",'warning',$translate.instant('UI_NO_PUEDE_MODIFICAR_CONTRATO_COMPLETADO'));
							return;
						}else if(panEgr.datasource.GestionIngresoEgresoSeleccion[0].IdEstado === 'CER'){
							bcsComunes.msg("warning",'warning',$translate.instant('UI_NO_PUEDE_MODIFICAR_CONTRATO_CERRADO'));
							return;
						}else if(panEgr.datasource.GestionIngresoEgresoSeleccion[0].IdEstado === 'ANU'){
							bcsComunes.msg("warning",'warning',$translate.instant('UI_NO_PUEDE_MODIFICAR_CONTRATO_ANULADO'));
							return;
						}
						filterVentana.modoVentana 				= 'M';
						filterVentana.CodigoTipoSolicitud 		= 'TRA';
						filterVentana.tipoTraspaso 				= 'EGR';
						filterVentana.IdContratoSAAPV 			= panEgr.datasource.GestionIngresoEgresoSeleccion[0].IdContratoSAAPV;
						filterVentana.DocumentoIdentidad		= panEgr.datasource.GestionIngresoEgresoSeleccion[0].DocumentoIdentidadDefecto;
						filterVentana.DocumentoIdentidadOrigen	= panEgr.datasource.GestionIngresoEgresoSeleccion[0].CodigoEntidad;
						filterVentana.CodigoTipoTraspaso		= panEgr.datasource.GestionIngresoEgresoSeleccion[0].CodigoTipoTraspaso;	
						filterVentana.IdTraspaso				= panEgr.datasource.GestionIngresoEgresoSeleccion[0].IdTraspaso;

				}else
					{
						bcsComunes.msg("warning",'warning',$translate.instant('UI_DEBE_SELECCIONAR_CONTRATO_PARA_MODIFICAR'));
						return;
					}
							
					var IdVentanamodal = bcsComunes.ventanaAbrir("ContratoSAAPV", filterVentana, true, false, IdVentana);
			}else if(modoVentana === 'C'){
				filterVentana.IdContratoSAAPV = panEgr.datasource.GestionIngresoEgresoSeleccion[0].IdContratoSAAPV;
				var IdVentanamodal = bcsComunes.ventanaAbrir("ConsultaContratoSAAPV", filterVentana, true, false, IdVentana);
			}
			
			var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
			promesa.then(function(data){
				if(modoVentana === 'M')
					panEgr.consulta.GestionIngresoEgreso();	
	        });

		};

		
		panEgr.VentanaIngresoTraspaso = function(){
			var IdVentanamodal = bcsComunes.ventanaAbrir("RegistroTraspasoAPV", { modoVentana : 'I', CodigoSolicitud : 'TRA', tipoTraspaso: 'EGR' }, true, false, IdVentana);
			var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
			promesa.then(function(data){
				panEgr.consulta.GestionIngresoEgreso();	
	        });
		};

		panEgr.VentanaNomina = function(){
			var IdVentanamodal = bcsComunes.ventanaAbrir("ReporteNominas", {}, true, false, IdVentana);
			var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
			promesa.then(function(data){
				//
	        });
		};
	});

}());