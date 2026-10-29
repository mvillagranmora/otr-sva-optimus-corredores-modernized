angular.module('ModuloCustodia').controller('ConsultarTarjetaExistenciaCtrl', 
		function($scope,$translate,bcsComunes,bcsServiciosCustodia,bcsServicioPlataforma)
{	
	$scope.filtro = {};
	$scope.data = {};
	var filters = {};
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	$scope.activarDescargaReportes = false;

	$scope.data.kngFechaProceso 			= util.dateInicioDia($scope.SesionFechaSistema);
	$scope.filtro.kngFechaConsultaDesde 	= util.dateInicioDia($scope.SesionFechaSistema);
	$scope.filtro.kngFechaConsultaHasta 	= util.dateInicioDia($scope.SesionFechaSistema);

	$scope.filtro.oConfiguracionConcepto={
		configuracion : {},
		filtros : {},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};

	var dsTarjetaExistencia = new bcs.DataSource({
        type : "odata",
		definition : {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.
			pageSize 	        : 20,
			sort 				: [ { field: "IdPortafolio", dir: "asc" },
									{ field: "Nemotecnico", dir: "asc" },
									{ field: "FechaProceso", dir: "asc" },
									{ field: "Nemotecnico", dir: "asc" },
								  ],
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1792",dataType : "json"}},
			schema : {
				model : {
					fields : {								
						IdTarjetaExistencia					: {type : "number"},
						IdEmpresa							: {type : "number"},
						FechaProceso						: {type : "sysdate"},
						FechaSistema						: {type : "sysdate"},
						IdCliente							: {type : "number"},
						NombreCliente						: {type : "string"},
						DocumentoIdentidad					: {type : "string"},
						DocumentoIdentidadAdicional			: {type : "string"},
						IdPortafolio						: {type : "number"},
						CodigoPortafolio					: {type : "string"},
						DescripcionPortafolio				: {type : "string"},
						Proposito							: {type : "string"},
						FechaOperacion						: {type : "sysdate"},
						Tipo								: {type : "string"},
						TipoUI								: {type : "string"},
						FolioCartera						: {type : "number"},
						IdConcepto							: {type : "string"},
						NombreConcepto						: {type : "string"},
						IdClasificacionTipoInstrumento		: {type : "string"},
						IdTipoInstrumento					: {type : "string"},
						IdInstrumento						: {type : "number"},
						Nemotecnico							: {type : "string"},
						ISIN								: {type : "string"},
						IdTipoReajuste						: {type : "string"},
						IdEmisor							: {type : "string"},
						IdMoneda							: {type : "string"},
						CantidadIngreso						: {type : "number"},
						CantidadEgreso						: {type : "number"},
						SaldoCantidad						: {type : "number"},
						PrecioAdquisicion					: {type : "number"},
						MontoIngreso						: {type : "number"},
						MontoEgreso							: {type : "number"},
						SaldoMontoPrecioAdquisicion			: {type : "number"},
						PrecioPromedioPonderado				: {type : "number"},
						MontoAPrecioPromedioPonderado		: {type : "number"},
						SaldoMontoAPrecioPromedioPonderado	: {type : "number"},
						Perdida								: {type : "number"},
						Ganancia							: {type : "number"},
						Acumulado							: {type : "number"},
						UltimoIngreso						: {type : "string"},
						UltimoRegistro						: {type : "string"},
						RegistroDeArrastre					: {type : "string"},
						IdEstado							: {type : "string"}
					}
				}
			}
		}
	});	
	$scope.confConsultaTarjetaExistencia = {
		dataSource  : dsTarjetaExistencia,
		columns 	: [  {field : "FechaProceso",						title:"UI_FECHA_PROCESO",					width: "60px"}
						,{field : "TipoUI",								title:"UI_TIPO",							width: "60px"}
						,{field : "FolioCartera",						title:"UI_SECUENCIA_MOVIMIENTO",			width: "90px"}
						,{field : "NombreConcepto",						title:"UI_CONCEPTO",						width: "200px"}						
						,{field : "Nemotecnico",						title:"UI_NEMOTECNICO",						width: "100px"}						
						,{field : "CantidadIngreso",					title:"UI_CANTIDAD_COMPRA",					width: "120px", template : " <label> #:  (util.bcs_formato(CantidadIngreso,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"}
						,{field : "CantidadEgreso",						title:"UI_CANTIDAD_VENTA",					width: "120px",	template : " <label> #:  (util.bcs_formato(CantidadEgreso,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"}						
						,{field : "SaldoCantidad",						title:"UI_SALDO_CANTIDAD",					width: "120px",	template : " <label> #:  (util.bcs_formato(SaldoCantidad,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
						,{field : "PrecioAdquisicion",					title:"UI_PRECIO_ADQUISICION",				width: "120px",	template : " <label> #:  (util.bcs_formato(PrecioAdquisicion,'GEN','Precio',IdMoneda,'','')).valor #</label>"}						
						,{field : "MontoIngreso",						title:"UI_MONTO_COMPRA",					width: "120px",	template : " <label> #:  (util.bcs_formato(MontoIngreso,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
						,{field : "MontoEgreso",						title:"UI_MONTO_VENTA",						width: "120px",	template : " <label> #:  (util.bcs_formato(MontoEgreso,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
						,{field : "SaldoMontoPrecioAdquisicion",		title:"UI_SALDO_MONTO_PRECIO_ADQUISICION",	width: "120px",	template : " <label> #:  (util.bcs_formato(SaldoMontoPrecioAdquisicion,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
						,{field : "PrecioPromedioPonderado",			title:"UI_PRECIO_PROMEDIO_PONDERADO",		width: "120px",	template : " <label> #:  (util.bcs_formato(PrecioPromedioPonderado,'GEN','Precio',IdMoneda,'','')).valor #</label>"}						
						,{field : "MontoAPrecioPromedioPonderado",		title:"UI_MONTO_A_PROMEDIO_PONDERADO",		width: "120px",	template : " <label> #:  (util.bcs_formato(MontoAPrecioPromedioPonderado,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
						,{field : "SaldoMontoAPrecioPromedioPonderado",	title:"UI_SALDO_MONTO_PRECIO_PONDERADO",	width: "120px",	template : " <label> #:  (util.bcs_formato(SaldoMontoAPrecioPromedioPonderado,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
						,{field : "Perdida",							title:"UI_PERDIDA",							width: "120px",	template : " <label> #:  (util.bcs_formato(Perdida,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
						,{field : "Ganancia",							title:"UI_GANANCIA",						width: "120px",	template : " <label> #:  (util.bcs_formato(Ganancia,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
						,{field : "Acumulado",							title:"UI_ACUMULADO",						width: "120px",	template : " <label> #:  (util.bcs_formato(Acumulado,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
                      ],
		contextMenu : true,
		filterable	: true,
		sortable 	: true,
		pageable 	: {refresh: true},
		height 		: 380,
		selectable 	: "row",
		rowColorBackground	: function(row) {
								 if (row.Tipo == 'EGRESO')
								 	return "green";								 
								 return "";
							  },
		autoBind 	: false,
		userSettings: true,
		gridCreated: function(grid) {
			$scope.gridDetalle = grid;
		},
	};

	var dsTarjetaExistenciaResumen = new bcs.DataSource({
		type : "call",
		definition : {
			serverFiltering 	: false, // Filtrado de datos en el servidor.
			//serverPaging 		: false, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.								
			callParameters      : filters,
			callTransaction 	: "1794",
			callResultSet 		: "P_OUT_TARJETAEXISTENCIARESUMEN",
			schema : {
				model : {
					fields : {										
						Nemotecnico							: {type : "string"},
						FechaProceso						: {type : "sysdate"},
						SaldoCantidad						: {type : "number"},
						SaldoMontoPrecioAdquisicion			: {type : "number"},
						PrecioPromedioPonderado				: {type : "number"},
						SaldoMontoAPrecioPromedioPonderado	: {type : "number"},
						UtilidadPerdidaAcumulada			: {type : "number"},
						PrecioAMercado						: {type : "number"},
						SaldoMontoAPrecioMercado			: {type : "number"},
						UtilidadPerdidaDevengada			: {type : "number"},
						UtilidadPerdidaRealizada			: {type : "number"},
						IdClasificacionTipoInstrumento		: {type : "string"}
					}
				}
			}
		}
	});	
	$scope.confConsultaTarjetaExistenciaResumen = {
		dataSource  : dsTarjetaExistenciaResumen,
		columns 	: [  {field : "Nemotecnico",						title:"UI_NEMOTECNICO",						width: "100px"}
						,{field : "FechaProceso",						title:"UI_FECHA_PROCESO",					width: "60px"}
						,{field : "SaldoCantidad",						title:"UI_SALDO_CANTIDAD",					width: "120px",	template : " <label> #:  (util.bcs_formato(SaldoCantidad,'GEN','Monto',IdMoneda,'','')).valor #</label>"}						
						,{field : "SaldoMontoPrecioAdquisicion",		title:"UI_SALDO_MONTO_PRECIO_ADQUISICION",	width: "120px",	template : " <label> #:  (util.bcs_formato(SaldoMontoPrecioAdquisicion,'GEN','Monto',IdMoneda,'','')).valor #</label>"}						
						,{field : "PrecioPromedioPonderado",			title:"UI_PRECIO_PROMEDIO_PONDERADO",		width: "120px",	template : " <label> #:  (util.bcs_formato(PrecioPromedioPonderado,'GEN','Precio',IdMoneda,'','')).valor #</label>"}
						,{field : "SaldoMontoAPrecioPromedioPonderado",	title:"UI_SALDO_MONTO_PRECIO_PONDERADO",	width: "120px",	template : " <label> #:  (util.bcs_formato(SaldoMontoAPrecioPromedioPonderado,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
						,{field : "UtilidadPerdidaAcumulada",			title:"UI_UT_PERD_ACUMULADA",				width: "120px",	template : " <label> #:  (util.bcs_formato(UtilidadPerdidaAcumulada,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
						
						,{field : "PrecioAMercado",						title:"UI_PRECIO_MERCADO",					width: "120px",	template : " <label> #:  (util.bcs_formato(PrecioAMercado,'GEN','Precio',IdMoneda,'','')).valor #</label>"}
						,{field : "SaldoMontoAPrecioMercado",			title:"UI_SALDO_MONTO_PRECIO_MERCADO",		width: "120px",	template : " <label> #:  (util.bcs_formato(SaldoMontoAPrecioMercado,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
						,{field : "UtilidadPerdidaDevengada",			title:"UI_UT_PERD_DEVENGADA",				width: "120px",	template : " <label> #:  (util.bcs_formato(UtilidadPerdidaDevengada,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
						,{field : "UtilidadPerdidaRealizada",			title:"UI_UT_PERD_REALIZADA",				width: "120px",	template : " <label> #:  (util.bcs_formato(UtilidadPerdidaRealizada,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
						,{field : "IdClasificacionTipoInstrumento",		title:"UI_NEMOTECNICO",						width: "100px", hidden:true}//no mostrar, es solo para separar los archivos al momento de exportar
                      ],
		contextMenu : true,
		filterable	: true,
		sortable 	: true,
		//pageable 	: {refresh: true},
		height 		: 380,
		selectable 	: "row",
		autoBind 	: false,
		userSettings: true,
		gridCreated: function(grid) {
			$scope.gridResumen = grid;
		},
	};

	$scope.Limpiar = function(){
		$scope.data.kngFechaProceso 			= util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtro.kngFechaConsultaDesde 	= util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtro.kngFechaConsultaHasta 	= util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtro.oConfiguracionConcepto.seleccionado.IdActual = 0;
		$scope.activarDescargaReportes = false;
		dsTarjetaExistencia.removeAll();
	};

	$scope.Buscar = function(){
		var filtros	= [];
		
		//Grilla detalle
		filtros.push({ field: "IdEstado", operator: "eq", value: 'VIG' });
		filtros.push({ field: "RegistroDeArrastre", operator: "eq", value: 'N' });
		filtros.push({ field: "FechaProceso", operator: "gte", value: util.dateOdata($scope.filtro.kngFechaConsultaDesde) });
		filtros.push({ field: "FechaProceso", operator: "lte", value: util.dateOdata($scope.filtro.kngFechaConsultaHasta) });

		if (typeof $scope.filtro.oInstrumento !== 'undefined'){
			filters.p_IdInstrumento	= Number($scope.filtro.oInstrumento.datos.IdInstrumento);
			filtros.push({ field: "IdInstrumento", operator: "eq", value: Number($scope.filtro.oInstrumento.datos.IdInstrumento) });
		}

		if ($scope.filtro.oConfiguracionConcepto.seleccionado.IdConcepto)
			filtros.push({ field: "IdConcepto", operator: "eq", value: $scope.filtro.oConfiguracionConcepto.seleccionado.IdConcepto });
				
		dsTarjetaExistencia.filter(filtros);


		//Grilla resumen
		filters.p_IdEmpresa 			= $scope.SesionIdEmpresa;
		filters.p_IdUsuario         	= $scope.SesionIdUsuario;
		filters.p_IdModulo				= 'CUS';		
		filters.p_IdInstrumento			= filters.p_IdInstrumento || null;
		filters.p_IdConcepto			= $scope.filtro.oConfiguracionConcepto.seleccionado.IdConcepto || null;
		filters.p_FechaConsultaDesde	= util.dateSPdate($scope.filtro.kngFechaConsultaDesde) || '0001-01-01';
		filters.p_FechaConsultaHasta	= util.dateSPdate($scope.filtro.kngFechaConsultaHasta) || '0001-01-01';
		dsTarjetaExistenciaResumen.setCallParameters(filters);
		$scope.activarDescargaReportes = true;
	};

	$scope.procesarTarjetaExistencia = function(){
		if ($scope.data.kngFechaProceso === null){
			bcsComunes.msg("error","error",$translate.instant('UI_INDICAR_FECHA_EJECUCION_PROCESO'));
			return;
		};

				
		//VARIABLES DEL SERVICIO
		var datos = {};
		datos.p_IdEmpresa						= $scope.SesionIdEmpresa;
		datos.p_IdUsuario						= $scope.SesionIdUsuario;
		datos.p_IdModulo						= 'CUS';
		datos.p_UsaReglasNegocio				= 'S';
		datos.p_FechaProceso					= util.dateSPdate($scope.data.kngFechaProceso);

		bcsComunes.ventanaOverlay($scope.winID, true);
		bcsServiciosCustodia.TarjetaExistencia(datos).$promise.then(
			function(respuesta){
				bcsComunes.ventanaOverlay($scope.winID, false);
				var Msg = bcsComunes.ventanaMsg({
							conflictos:util.parseServerResponse(respuesta)
						  },$scope.winID);
				var promesa=bcsComunes.ventanaPromesa(Msg);

				promesa.then(function(data){				
					if (respuesta.P_OUT_ERRORESCONTADOR === 0){
						$scope.Limpiar();						
					};
					$scope.Buscar();
				});
			},
			function(respuesta){
				bcsComunes.ventanaOverlay($scope.winID, false);
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_EJECUTAR_TARJETA_EXISTENCIA"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
			}
		);
	};

	$scope.obtenerTarjetaExistencia = function(){
		var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo  : 'SOC',
				p_TipoPrograma : [{
					IdInterno : 0,
					IdTipoPrograma : 'GENERAR_DETALLE_POSICIONES',
					SegundosDesfase : 0
				}],
				p_Parametro : [{
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
				}
				]
		}
		//valores
		if(!!filters.p_IdInstrumento)
			datos.p_Parametro.push({
						IdInterno : 0,
						NombreParametro : 'IdInstrumento',
						Valor : filters.p_IdInstrumento || null
					});
		if(!!$scope.filtro.oConfiguracionConcepto.seleccionado.IdConcepto)
			datos.p_Parametro.push({
						IdInterno : 0,
						NombreParametro : 'IdConcepto',
						Valor : $scope.filtro.oConfiguracionConcepto.seleccionado.IdConcepto || null
					});
					
		datos.p_Parametro.push({
					IdInterno : 0,
					NombreParametro : 'FechaConsultaDesde',
					Valor : util.dateSPdate($scope.filtro.kngFechaConsultaDesde) || '0001-01-01'
				});
		datos.p_Parametro.push({
					IdInterno : 0,
					NombreParametro : 'FechaConsultaHasta',
					Valor : util.dateSPdate($scope.filtro.kngFechaConsultaHasta) || '2999-01-01'
				});

		bcsServicioPlataforma.Servicios().InsertarProcesoBatch(datos).$promise.then( function(result) {
			bcsComunes.ventanaOverlay($scope.winID,false);
			if(result.P_OUT_ERRORESCONTADOR === 0){
				var fechaLocal=util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion,"FechaHora","CALL");
				bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO').replace("::IdPrograma",result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion",util.formatoSecondate(fechaLocal)),
					conflictos : util.parseServerResponse(result)}, $scope.winID);
			}else{
				bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
			}
		},
		function(result){
			bcsComunes.ventanaOverlay($scope.winID,false);
			bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
		});

	}

	$scope.obtenerPosiciones = function(){
		
		var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo  : 'SOC',
				p_TipoPrograma : [{
					IdInterno : 0,
					IdTipoPrograma : 'GENERAR_RESUMEN_POSICIONES',
					SegundosDesfase : 0
				}],
				p_Parametro : [{
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
				}
				]
		}
		//valores
		if(!!filters.p_IdInstrumento)
			datos.p_Parametro.push({
						IdInterno : 0,
						NombreParametro : 'IdInstrumento',
						Valor : filters.p_IdInstrumento || null
					});
		if(!!$scope.filtro.oConfiguracionConcepto.seleccionado.IdConcepto)
			datos.p_Parametro.push({
						IdInterno : 0,
						NombreParametro : 'IdConcepto',
						Valor : $scope.filtro.oConfiguracionConcepto.seleccionado.IdConcepto || null
					});
					
		datos.p_Parametro.push({
					IdInterno : 0,
					NombreParametro : 'FechaConsultaDesde',
					Valor : util.dateSPdate($scope.filtro.kngFechaConsultaDesde) || '0001-01-01'
				});
		datos.p_Parametro.push({
					IdInterno : 0,
					NombreParametro : 'FechaConsultaHasta',
					Valor : util.dateSPdate($scope.filtro.kngFechaConsultaHasta) || '2999-01-01'
				});

		bcsServicioPlataforma.Servicios().InsertarProcesoBatch(datos).$promise.then( function(result) {
			bcsComunes.ventanaOverlay($scope.winID,false);
			if(result.P_OUT_ERRORESCONTADOR === 0){
				var fechaLocal=util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion,"FechaHora","CALL");
				bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO').replace("::IdPrograma",result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion",util.formatoSecondate(fechaLocal)),
					conflictos : util.parseServerResponse(result)}, $scope.winID);
			}else{
				bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
			}
		},
		function(result){
			bcsComunes.ventanaOverlay($scope.winID,false);
			bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
		});

	}

});