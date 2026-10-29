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
			selectable 			: "multiple row",
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
		bcsComunes.ventanaAbrir("DetalleCargaMasivaCustodia", {oCargaMasiva: seleccion[0]}, false, false );
	};	
	
	
	
	//Carga data de archivo a BD(sin procesar)
	$scope.cargarArchivo = function(){
	var tipo = '';
		
		if ($scope.oConfiguracionTipoCargaMasiva.seleccionado.IdTipoCargaMasiva === 0 || $scope.oConfiguracionTipoCargaMasiva.seleccionado.IdTipoCargaMasiva === undefined)
		{
			bcsComunes.msg("warning","warning",$translate.instant('DEBE_INDICAR_TIPO_CARGA'));
			return;			
		};
		
		if ($scope.oConfiguracionTipoCargaMasiva.seleccionado.IdTipoCargaMasiva < 5) {
		 	var tipo = 'CUS'
		}
		else
		{
			var tipo = 'GAR'
		};


		if (tipo == 'CUS') {
		 	var datosArchivo = {codigoArchivo: 'CARGA_MASIVA_CUSTODIA'}
		}
		else
		{
			var datosArchivo = {codigoArchivo: 'CARGA_MASIVA_GARANTIA'}
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
					    	DocumentoIdentidad		: value.RUTCLIENTE			|| '',
					    	Portafolio				: value.PORTAFOLIO			|| '',
					    	CondicionLiquidacion	: value.CONDICIONLIQUIDACION || '',
					    	Boveda					: value.BOVEDA				|| '',
					    	Concepto				: value.CONCEPTO			|| '',
					    	Nemotecnico				: value.NEMO				|| '',
					    	Emisor					: value.EMISOR				|| '',
					    	Reajuste				: value.REAJUSTE			|| '',
					    	FechaVencimiento		: value.FECHAVENCIMIENTO	|| '1900-01-01',
					    	Cantidad				: value.CANTIDAD			|| 0,
					    	Precio					: value.PRECIO				|| 0,
					    	Tasa					: value.TASA				|| 0,
					    	Monto					: value.MONTO				|| 0,
					    	Glosa					: value.GLOSA				|| '',
					    	NemoGarantizado			: value.NEMOGARANTIZADO		|| '',
					    	Moneda					: value.MONEDA				|| '',
							Fungible				: value.FUNGIBLE			|| '',
							Bolsa					: value.BOLSA				|| ''  	
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
				$.each(seleccion, function(index, value){
					if (seleccion[index].IdEstadoCargaMasiva != 'PRO'){ // SOLO ENVÍA LOS REGISTROS QUE NO ESTEN "PROCESADOS"
						arrCargaMasivaEliminar.push({IdCargaMasiva          : value.IdCargaMasiva})
					}
				});

				var datos = {};
				datos.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
				datos.p_IdUsuario               	= $scope.SesionIdUsuario; 
				datos.p_IdModulo                	= 'CUS';
				datos.p_UsaReglasNegocio			= 'S';
				datos.p_CargaMasivaEliminar			= arrCargaMasivaEliminar;		
				 
				bcsServiciosCargaMasivaCustodia.EliminarCargaMasivaCustodia(
					datos,
					function(respuesta){
						bcsComunes.ventanaOverlay($scope.IdVentana, false);
						 		
						if (respuesta.P_OUT_ERRORESCONTADOR === 0){
							
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

	
	//Procesa cargas masivas(genera comprobantes tesorería)
	$scope.procesarCargarMasiva = function(seleccion) {
		
		if (seleccion.length === 0){
			bcsComunes.msg("warning","warning",$translate.instant('UI_SELECCIONAR_MOVIMIENTOS_VALIDOS'));
			return;
		}

		var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_PROCESARAN_LAS_CARGAS_SELECCIONADAS') );
		
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR){
				
				bcsComunes.ventanaOverlay($scope.IdVentana, true);
				
				var arrCargaMasivaProcesar = [];     	   	    
				$.each(seleccion, function(index, value){
					if (seleccion[index].IdEstadoCargaMasiva == 'ING'){ // SOLO ENVÍA LOS REGISTROS EN ESTADO "INGRESADO"
						arrCargaMasivaProcesar.push({	IdCargaMasiva          : value.IdCargaMasiva,
														IdTipoCargaMasiva      : value.IdTipoCargaMasiva});
					};
				});

				var datos = {};
				datos.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
				datos.p_IdUsuario               	= $scope.SesionIdUsuario; 
				datos.p_IdModulo                	= 'CUS';
				datos.p_UsaReglasNegocio			= 'S';
				datos.p_FechaProceso	   		    = util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
				datos.p_CargaMasivaProcesar			= arrCargaMasivaProcesar;
													  
				bcsServiciosCargaMasivaCustodia.ProcesarCargaMasivaCustodia(
					datos,
					function(respuesta){
						bcsComunes.ventanaOverlay($scope.IdVentana, false);

						if (respuesta.P_OUT_ERRORESCONTADOR === 0){

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

	
});
