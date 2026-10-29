angular.module('ModuloCustodia').controller('BlotterCargaMasivaCustodiaCtrl',
			function($scope,$translate,$timeout,$rootScope,bcsComunes,$translate,bcsAlerta,OPCIONES_BCSALERTA,
					bcsServiciosCustodia,bcsServiciosCargaMasivaCustodia,bcsBlotterCargaMasivaCustodiaGrid)
{
	
	$scope.data = {};
	
	$scope.IdVentana = bcsComunes.ventanaWinId ($scope);
	bcsComunes.ventanaAutoOverlay($scope.IdVentana,false,3000);
	

	$scope.kngFechaCargaDesde = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.kngFechaCargaHasta = util.dateInicioDia($scope.SesionFechaSistema);
	
	
	//------------------------------------------------
	// CONFIGURACION COMBOBOX
	//------------------------------------------------
	       
	$scope.oConfiguracionTipoCargaMasiva = {
			tipo 			: "combobox-tipo-carga-masiva-custodia",
			configuracion  	: {},
			filtros 		: {
								 IdEstado	: { "field"   : "IdEstado",  "operator": "eq", "value"   : "ACT" },
								 IdEmpresa	: { "field"   : "IdEmpresa", "operator": "eq", "value"   : $scope.SesionIdEmpresa }
							  },							  
			idActual 	 	: false,
			seleccionado 	: false,
			idDefecto    	: ''			
	};	
	 
	
	$scope.oConfiguracionEstado = {
			tipo : "combobox-estado-custodia",
			configuracion	: {},
			filtros 		: {
								 NombreEntidad : {"field": "NombreEntidad","operator": "eq", "value"   : 'CUSTODIA.CARGAMASIVA'},
							  },
			idActual 		: false,
			seleccionado 	: false,
			idDefecto 		: ''			
	};
	
	

	
	//------------------------------------------------
	// CONFIGURACION DE GRILLA
	//------------------------------------------------	
	var dsCargaMasiva = bcsBlotterCargaMasivaCustodiaGrid.dsCargaMasiva;
	$scope.confBasicaCargaMasiva = {
			dataSource			: dsCargaMasiva,		
			columns				: bcsBlotterCargaMasivaCustodiaGrid.columnasCargaMasiva(),		
			sortable			: true,
			pageable			: true,		
			height				: 320,
			selectable 			: "single row",
			filterable			: true,
			rowColorBackground	: function(row) { 
									  var color = "";
									  if (row.IdEstadoCargaMasiva == 'INV')
										 color = "red";
									  return color;
								  },
	};
					
	
	// --------------------------------------------------
	// FUNCIONES 
	// --------------------------------------------------
	$scope.buscarCargaMasiva = function(){
		var filters = new Array();
		           
		if ($scope.oConfiguracionTipoCargaMasiva.seleccionado.IdTipoCargaMasiva)
			filters.push({ field: "IdTipoCargaMasiva", operator: "eq", value: Number($scope.oConfiguracionTipoCargaMasiva.seleccionado.IdTipoCargaMasiva || 0)});		
		if ($scope.kngFechaCargaDesde)
			filters.push({ field: "FechaCarga",        operator: "gte", value: util.dateOdata($scope.kngFechaCargaDesde) });		
		if ($scope.kngFechaCargaHasta)
			filters.push({ field: "FechaCarga",        operator: "lte", value: util.dateTerminoDia($scope.kngFechaCargaHasta) });		
		if ($scope.oConfiguracionEstado.seleccionado.IdEstado)
			filters.push({ field: "IdEstadoCargaMasiva", operator: "eq", value: $scope.oConfiguracionEstado.seleccionado.IdEstado});
		
		dsCargaMasiva.filters(filters);	
		
	};
	

	
	$scope.limpiarFiltros = function(){
		$scope.oConfiguracionTipoCargaMasiva.idActual = false;
		$scope.kngFechaCargaDesde = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.kngFechaCargaHasta = util.dateInicioDia($scope.SesionFechaSistema);
		dsCargaMasiva.removeAll();
	};
	
	
	$scope.detalleCargaMasiva = function (seleccion)
	{	
		bcsComunes.ventanaAbrir("DetalleCargaMasivaCustodia", {oCargaMasiva: seleccion}, false, false );
	};	
	
	
	
	//Carga data de archivo a BD(sin procesar)
	$scope.cargarArchivo = function(){
	var tipo = '';
		
		if ($scope.oConfiguracionTipoCargaMasiva.seleccionado.IdTipoCargaMasiva === 0 || $scope.oConfiguracionTipoCargaMasiva.seleccionado.IdTipoCargaMasiva === undefined)
		{
			bcsComunes.msg("warning","warning",$translate.instant('DEBE_INDICAR_TIPO_CARGA'));
			return;			
		};
		
		
		if(	$scope.oConfiguracionTipoCargaMasiva.seleccionado.Codigo == "INGRESO_DE_TITULOS_RV" 					
			|| $scope.oConfiguracionTipoCargaMasiva.seleccionado.Codigo == "RETIRO_DE_TITULOS_RV")
		 {
			var tipo = 'CUSRV'
		}
		else if (   $scope.oConfiguracionTipoCargaMasiva.seleccionado.IdTipoCargaMasiva < 5 
			|| $scope.oConfiguracionTipoCargaMasiva.seleccionado.IdTipoCargaMasiva == 13   //GCMBISEGCU-1 Carga de registros Ingreso Custodia Externa
			|| $scope.oConfiguracionTipoCargaMasiva.seleccionado.IdTipoCargaMasiva == 14)   //GCMBISEGCU-1 Carga de registros Retiro Custodia Externa 
		{
			var tipo = 'CUS'
		}
		else if ($scope.oConfiguracionTipoCargaMasiva.seleccionado.IdTipoCargaMasiva == 9) {
			var tipo = 'REP'
		}
		else if ($scope.oConfiguracionTipoCargaMasiva.seleccionado.IdTipoCargaMasiva == 22) {        /// INGRESO DE TITULOS INTERNACIONALES
			var tipo = 'INGINTER'
		}
		else if ($scope.oConfiguracionTipoCargaMasiva.seleccionado.IdTipoCargaMasiva == 23) {        /// EGRESO DE TITULOS INTERNACIONALES
			var tipo = 'EGREINTER'
		} else if ($scope.oConfiguracionTipoCargaMasiva.seleccionado.Codigo === "DIVIDENDO_EN_CUOTAS_FFMM") {
			var tipo = 'DF';
		} else if ($scope.oConfiguracionTipoCargaMasiva.seleccionado.Codigo === "RETIRO_DE_TITULOS_RF") {
			var tipo = 'RETTITRF';
		} else {
			var tipo = 'GAR'
		};

		
		//Inicio GCMBISEGCU-1 Carga de registros Custodia Externa 
		if (tipo == 'CUS' && (   $scope.oConfiguracionTipoCargaMasiva.seleccionado.IdTipoCargaMasiva == 13 
							  || $scope.oConfiguracionTipoCargaMasiva.seleccionado.IdTipoCargaMasiva == 14) )
		{
		 	var datosArchivo = {codigoArchivo: 'CARGA_MASIVA_CUSTODIA_EXT'}
		}
		//Fin GCMBISEGCU-1
		else if (tipo == 'CUSRV'){
			var datosArchivo = {codigoArchivo: 'CARGA_MASIVA_CUSTODIA_RV'}
		}
		else if (tipo == 'CUS') {
		 	var datosArchivo = {codigoArchivo: 'CARGA_MASIVA_CUSTODIA'}
		}
		else if (tipo == 'REP') {
		 	var datosArchivo = {codigoArchivo: 'CARGA_MASIVA_REPORTES'}
		}
		else if (tipo == 'INGINTER') {
		 	var datosArchivo = {codigoArchivo: 'INGRESO_RETIRO_TITULOS_INTERNACIONALES'}             /// INGRESO DE TITULOS INTERNACIONALES
		}
		else if (tipo == 'EGREINTER') {
		 	var datosArchivo = {codigoArchivo: 'INGRESO_RETIRO_TITULOS_INTERNACIONALES'}             /// EGRESO DE TITULOS INTERNACIONALES
		}
		else if (tipo == 'DF') {
			var datosArchivo = { codigoArchivo: 'CARGA_DIVIDENDO_EN_CUOTAS_FFMM' }
		}
		else if (tipo == 'RETTITRF') {
				var datosArchivo = { codigoArchivo: 'CARGA_MASIVA_RETIRO_CUS_RF' }
		} else {
			var datosArchivo = { codigoArchivo: 'CARGA_MASIVA_GARANTIA' }
		};
		
		 var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,true,true);			
		 var promesa = bcsComunes.ventanaPromesa(id1);
		 
		 promesa.then(function(data){
			 //El objeto data, contiene la informacion del archivo en formato JSON.
			 
			 if(!data || !data.fileData)
			 {
				 return;
			 }

			 var cargaMasiva = [];
			 cargaMasiva.push({
			    	  IdInterno             : 1,
			    	  IdTipoCargaMasiva    	: $scope.oConfiguracionTipoCargaMasiva.seleccionado.IdTipoCargaMasiva || 0,
			    	  NombreArchivo	        : data.fileName
			 });
			 

		 		var cargaMasivaDetalle = [];
				 $.each(data.fileData, function(key, value){
					    cargaMasivaDetalle.push({
					    	IdInterno				: 1,
					    	DocumentoIdentidad		: value.RUTCLIENTE					|| '',
					    	Portafolio				: value.PORTAFOLIO					|| '',
					    	CondicionLiquidacion	: value.CONDICIONLIQUIDACION 		|| '',
					    	Boveda					: value.BOVEDA						|| '',
					    	CuentaCustodio			: value.CUENTACUSTODIO				|| '',
					    	Concepto				: value.CONCEPTO					|| '',
					    	Nemotecnico				: value.NEMO						|| '',
					    	Emisor					: value.EMISOR						|| '',
					    	Reajuste				: value.REAJUSTE					|| '',
					    	FechaVencimiento		: value.FECHAVENCIMIENTO			|| '1900-01-01',
					    	Cantidad				: value.CANTIDAD					|| 0,
					    	Precio					: value.PRECIO						|| 0,
					    	Tasa					: value.TASA						|| 0,
					    	Monto					: value.MONTO						|| 0,
					    	ValorRescate			: value.VALORRESCATE				|| 0,
					    	Glosa					: value.GLOSA						|| '',
					    	NemoGarantizado			: value.NEMOGARANTIZADO				|| '',
					    	Moneda					: value.MONEDA						|| '',
							Fungible				: value.FUNGIBLE					|| '',
							Bolsa					: value.BOLSA						|| '',
							CorrelativoReporte		: value.CORRELATIVOREPORTE			|| 0,
							TipoOperacion			: value.TIPOOPERACION				|| '',
							ClasificacionInstrumento: value.CLASIFICACIONINSTRUMENTO	|| '',
							Plazo					: value.PLAZO						|| 0,
							FechaLiquidacion		: value.FECHALIQUIDACION			|| '1900-01-01',
							FechaOperacion			: value.FECHAOPERACION				|| '1900-01-01',
							TipoCambio				: value.TIPOCAMBIO					|| 0,
							MontoContado			: value.MONTOCONTADO				|| 0,
							MontoPlazo				: value.MONTOPLAZO					|| 0,
							EsNominal				: value.ESNOMINAL					|| '',
							ReferenciaReportado		: value.REFERENCIAREPORTADO			|| '',
							AsociadoCorelativo		: value.ASOCIADOCORELATIVO			|| 0,
							FolioCartera			: value.FOLIOCARTERA				|| 0,
							TipoEvento				: value.TIPOEVENTO					|| '',
							NumeroDividendo			: value.NUMERODIVIDENDO				|| 0,
							FechaLimite				: value.FECHALIMITE					|| '1900-01-01',
							FechaPago				: value.FECHAPAGO					|| '1900-01-01',
							Descripcion				: value.DESCRIPCION					|| '',
							Factor					: value.FACTOR						|| 0,
							Cuotas					: value.CUOTAS						|| 0,
					 	});
				 });			
			 


			 var pregunta = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_CARGARAN_LOS_DATOS_DESDE_EL_ARCHIVO') );		
			 pregunta.result.then(function(result){
					if (result===OPCIONES_BCSALERTA.ACEPTAR){

						bcsComunes.ventanaOverlay($scope.IdVentana, true);
						
						var parametros = {};
					
						parametros.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
						parametros.p_IdUsuario               	= $scope.SesionIdUsuario; 
						parametros.p_IdModulo                	= 'CUS';
						parametros.p_UsaReglasNegocio			= 'S';
						parametros.p_FechaCarga					= util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
						parametros.p_CargaMasiva				= cargaMasiva;
						parametros.p_DetalleCargaMasiva         = cargaMasivaDetalle;
						
						bcsComunes.log().info(JSON.stringify(parametros));
						bcsServiciosCargaMasivaCustodia.CrearCargaMasivaCustodia(parametros,
							function(respuesta){
								bcsComunes.ventanaOverlay($scope.IdVentana, false);
								 		
								if (respuesta.P_OUT_ERRORESCONTADOR === 0){	
									bcsComunes.ventanaMsg({
										mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_EXITO"),
										conflictos:util.parseServerResponse(respuesta)
									},$scope.IdVentana);
									$scope.buscarCargaMasiva();									
								} else {	
									bcsComunes.ventanaMsg({
										mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
										conflictos:util.parseServerResponse(respuesta)
									},$scope.IdVentana);
								}
							},
							function(respuesta){
								bcsComunes.ventanaMsg({
									mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
									conflictos:util.parseServerResponse(respuesta)
								},$scope.IdVentana);
								bcsComunes.ventanaOverlay($scope.IdVentana, false);
							}
						);
					}
			 });
						 

  		}); 
	};
	

	//Elimina cargas masivas sin procesar
	$scope.eliminarCargarMasiva = function(seleccion) {
		
		if (seleccion.length === 0){
			bcsComunes.msg("warning","warning",$translate.instant('UI_SELECCIONAR_MOVIMIENTOS_VALIDOS'));
			return;
		}
		
		var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_ELIMINARAN_LAS_CARGAS_SELECCIONADAS') );
		
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR){

				bcsComunes.ventanaOverlay($scope.IdVentana, true);
								
				var arrCargaMasivaEliminar = [];
				arrCargaMasivaEliminar.push({IdCargaMasiva  : seleccion.IdCargaMasiva});
				var arrCargaMasivaEstado = seleccion.IdEstadoCargaMasiva;
				
			var datos = {};
			datos.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
			datos.p_IdUsuario               	= $scope.SesionIdUsuario; 
			datos.p_IdModulo                	= 'CUS';
			datos.p_UsaReglasNegocio			= 'S';
			datos.p_CargaMasivaEliminar			= arrCargaMasivaEliminar;				
			datos.p_CargaMasivaEstado			= arrCargaMasivaEstado;
			
				bcsServiciosCargaMasivaCustodia.EliminarCargaMasivaCustodia(
					datos,
					function(respuesta){
						bcsComunes.ventanaOverlay($scope.IdVentana, false);
						 		
						if (respuesta.P_OUT_ERRORESCONTADOR === 0){
							if(respuesta.P_OUT_ADVERTENCIA === 1){
								bcsComunes.ventanaMsg({				
									mensaje:$translate.instant("UI_OPERACION_NO_VALIDA"),
									conflictos:util.parseServerResponse(respuesta)
								},$scope.IdVentana);
							}else{
							if (respuesta.P_OUT_ADVERTENCIASCONTADOR === 0)
							{
								bcsComunes.ventanaMsg({				
									mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_EXITO"),
									conflictos:util.parseServerResponse(respuesta)
								},$scope.IdVentana);								
							}
							else
							{   
								bcsComunes.ventanaMsg({				
									mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ADVERTENCIAS"),
									conflictos:util.parseServerResponse(respuesta)
								},$scope.IdVentana);
							}
							$scope.buscarCargaMasiva();
						}
							} else {	
							bcsComunes.ventanaMsg({
								mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
								conflictos:util.parseServerResponse(respuesta)
							},$scope.IdVentana);
						}
					},
					function(respuesta){
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
							conflictos:util.parseServerResponse(respuesta)
						},$scope.IdVentana);
						bcsComunes.ventanaOverlay($scope.IdVentana, false);
					}
				);
			}
		});
		return;				
	};	

	
	$scope.abrirModal = function(oSeleccion)
	{
		
		var id1 = bcsComunes.ventanaAbrir("ProcesarCargaMasivaCustodiaModal",{CargaMasiva : oSeleccion},false,false,$scope.IdVentana);
        var promesa=bcsComunes.ventanaPromesa(id1);
        
        promesa.then(function(data){ 
        	if (data === true)
			{	
        		$scope.gridApi.dataSource.read();	
			}
    	});  
        
	};

	//Procesa cargas masivas(genera comprobantes tesorería)
	$scope.procesarCargarMasiva = function(seleccion) {
		
		if (seleccion.length === 0){
			bcsComunes.msg("warning","warning",$translate.instant('UI_SELECCIONAR_MOVIMIENTOS_VALIDOS'));
			return;
		}

		if (seleccion.IdEstadoCargaMasiva != "ING"){
			bcsComunes.msg("warning","warning",$translate.instant('UI_SELECCIONAR_MOVIMIENTOS_VALIDOS'));
			return;
		}else{
			if ((seleccion.IdTipoCargaMasiva == 5) || (seleccion.IdTipoCargaMasiva == 9)){ // Levanta Ventana Emergente
				$scope.abrirModal(seleccion);
			}else{
				var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_PROCESARAN_LAS_CARGAS_SELECCIONADAS') );

				t.result.then(function(result){
					if (result===OPCIONES_BCSALERTA.ACEPTAR){
						bcsComunes.ventanaOverlay($scope.IdVentana, true);
						
						if (seleccion && seleccion.CantidadRegistros > 500) {
							bcsComunes.ventanaOverlay($scope.IdVentana, false);
							var parametrosBatch = {
								p_IdEmpresa : $scope.SesionIdEmpresa,
								p_IdUsuario: $scope.SesionIdUsuario,
								p_IdModulo  : 'PLA',
								p_TipoPrograma : [{
									IdInterno : 0,
									IdTipoPrograma : 'PROCESA_CARGA_MASIVA_CUSTODIA',
									SegundosDesfase : 0
								}],
								p_Parametro : [{
									IdInterno : 0,
									NombreParametro: "CargaMasivaProcesar",
									Valor: seleccion.IdCargaMasiva
								},{
									IdInterno : 0,
									NombreParametro: "FechaProceso",
									Valor: util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema))
								},{
									IdInterno : 0,
									NombreParametro: "UsaCarterasSistema",
									Valor: "S"
								},{
									IdInterno : 0,
									NombreParametro: "IdUsuario",
									Valor: $scope.SesionIdUsuario
								}]
							}
							
							bcsServiciosCargaMasivaCustodia.InscribirProcesoBatch(parametrosBatch).$promise.then(
							function(result) {
								bcsComunes.ventanaOverlay($scope.idVentana,false);
								var fechaLocal=util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion,"FechaHora","CALL");
								bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO').replace("::IdPrograma",result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion",util.formatoSecondate(fechaLocal)),
									conflictos : util.parseServerResponse(result)}, $scope.idVentana);

							},
							function(result){
								bcsComunes.ventanaOverlay($scope.idVentana,false);
								bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
							});
						} else {
							var datos = {};
							datos.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
							datos.p_IdUsuario               	= $scope.SesionIdUsuario; 
							datos.p_IdModulo                	= 'CUS';
							datos.p_UsaReglasNegocio			= 'S';
							datos.p_FechaProceso	   		    = util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
							datos.p_CargaMasivaProcesar			= seleccion.IdCargaMasiva;
							datos.p_UsaCarterasSistema			= "S";
	
							bcsServiciosCargaMasivaCustodia.ProcesarCargaMasivaCustodia(
								datos,
								function(respuesta){
									bcsComunes.ventanaOverlay($scope.IdVentana, false);
	
									if (respuesta.P_OUT_ERRORESCONTADOR === 0){
										if (respuesta.P_OUT_ADVERTENCIASCONTADOR === 0){
											bcsComunes.ventanaMsg({				
												mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_EXITO"),
												conflictos:util.parseServerResponse(respuesta)
											},$scope.IdVentana);
											$scope.buscarCargaMasiva();
										} else {
											bcsComunes.ventanaMsg({
												conflictos:util.parseServerResponse(respuesta)
											},$scope.IdVentana);
											$scope.buscarCargaMasiva();
										}
									} else {
										bcsComunes.ventanaMsg({
											mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
											conflictos:util.parseServerResponse(respuesta)
										},$scope.IdVentana);
									}
								},
								function(respuesta){
									bcsComunes.ventanaMsg({
										mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
										conflictos:util.parseServerResponse(respuesta)
									},$scope.IdVentana);
									bcsComunes.ventanaOverlay($scope.IdVentana, false);
								}
							);
						}
					}
				});			
			};			
		}

		return;					
	};		
});