angular.module('ModuloFacturacion').controller('BlotterCargaMasivaFacturacionCtrl',
			function($scope,$translate,$timeout,$rootScope,bcsComunes,$translate,bcsAlerta,OPCIONES_BCSALERTA,
					bcsServiciosFacturacion,bcsBlotterCargaMasivaGrid,bcsServicioPlataforma)
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
			tipo 			: "combobox-tipo-carga-masiva-facturacion",
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
			tipo : "combobox-estado-facturacion",
			configuracion	: {},
			filtros 		: {
								 NombreEntidad : {"field": "NombreEntidad","operator": "eq", "value"   : 'FAC.CargaMasiva'},
							  },
			idActual 		: false,
			seleccionado 	: false,
			idDefecto 		: ''			
	};
	
	
 
	
	//------------------------------------------------
	// CONFIGURACION DE GRILLA
	//------------------------------------------------	
	var dsCargaMasiva = bcsBlotterCargaMasivaGrid.dsCargaMasiva;
	$scope.confBasicaCargaMasiva = {
			dataSource			: dsCargaMasiva,		
			columns				: bcsBlotterCargaMasivaGrid.columnasCargaMasiva(),		
			sortable			: true,
			pageable			: true,		
			height				: 320,
			selectable 			: "row",	
			userSettings        : true,
			rowColorBackground	: function(row) { 
									  var color = "";
									  if (row.IdEstadoCargaMasiva == 'ING')
										 color = "yellow";
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
			filters.push({ field: "FechaCarga",        operator: "gte", value: util.dateInicioDia($scope.kngFechaCargaDesde) });
		if ($scope.kngFechaCargaHasta)
			filters.push({ field: "FechaCarga",        operator: "lte", value: util.dateTerminoDia($scope.kngFechaCargaHasta) });		
		if ($scope.oConfiguracionEstado.seleccionado.IdEstado)
			filters.push({ field: "IdEstadoCargaMasiva", operator: "eq", value: $scope.oConfiguracionEstado.seleccionado.IdEstado});
		
		dsCargaMasiva.filter(filters);	
		
	};
	
	
	
	$scope.limpiarFiltros = function(){
		$scope.oConfiguracionTipoCargaMasiva.idActual = false;		
		$scope.oConfiguracionEstado.idActual = false;
		$scope.kngFechaCargaDesde = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.kngFechaCargaHasta = util.dateInicioDia($scope.SesionFechaSistema);
		dsCargaMasiva.removeAll();
	};
	


	
	$scope.detalleCargaMasiva = function (seleccion)
	{	
		bcsComunes.ventanaAbrir("DetalleCargaMasivaFacturacion", seleccion, false, false );
	};	
	
	
	
	//Carga data de archivo a BD(sin procesar)
	$scope.cargarArchivo = function(){
		if (BCS_ESTADO_MODULO['FAC'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}
		 if ($scope.oConfiguracionTipoCargaMasiva.seleccionado.IdTipoCargaMasiva === 0 || $scope.oConfiguracionTipoCargaMasiva.seleccionado.IdTipoCargaMasiva === undefined || $scope.oConfiguracionTipoCargaMasiva.seleccionado.CodigoArchivo === '' || $scope.oConfiguracionTipoCargaMasiva.seleccionado.CodigoArchivo === undefined)
		 {
			bcsComunes.ventanaMsg({mensaje: $translate.instant('DEBE_INDICAR_TIPO_CARGA')},$scope.IdVentana);
			return;			
		 };
		 
		 var datosArchivo = {codigoArchivo: $scope.oConfiguracionTipoCargaMasiva.seleccionado.CodigoArchivo || ''};		 
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
			    	  NombreArchivo	        : data.fileName
			 });

			 var cargaMasivaDetalle = [];
			 $.each(data.fileData, function(key, value){
				    cargaMasivaDetalle.push({
				    		IdInterno             	: 1,						  
							IdConcepto	  			: value.CONCEPTO,
							IdTipoOperacion			: value.TIPOOPERACION,
							DocumentoIdentidad  	: value.DOCUMENTO_IDENTIDAD,
							CodigoPortafolio  		: value.CODIGOPORTAFOLIO,
							IdTipoComprobante  		: value.TIPO_COMPROBANTE,
							IdCondicionDeLiquidacion: value.CONDICION_LIQUIDACION,  //T0,T1...
							IdMoneda  				: value.MONEDA,
							Monto  					: Number(value.MONTO),
							Glosa  					: value.GLOSA,
							Referencia				: value.Referencia || '',
							TipoReferencia			: value.TipoReferencia || '',
							IdMedioDePago			: (value.MEDIO_DE_PAGO === undefined) ? "" : String(value.MEDIO_DE_PAGO || ''), 
							IdModalidadDePago		: (value.MODALIDAD_DE_PAGO === undefined) ? "" : String(value.MODALIDAD_DE_PAGO).replace("\r",""),
						    TipoComprobanteDestino  : value.TIPO_COMPROBANTE_DESTINO ||'',
							FolioComprobante		: value.FOLIO_COMPROBANTE || '',
							IdMatrizAgrupador       : (value.IDMATRIZAGRUPADOR === undefined)? 0: value.IDMATRIZAGRUPADOR ,
							FechaLiquidacion        : value.FECHALIQUIDACION || ''
				 	});
			 });
			 
			 
			 var pregunta = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_CARGARAN_LOS_DATOS_DESDE_EL_ARCHIVO'));			 
			 pregunta.result.then(function(result){
					if (result===OPCIONES_BCSALERTA.ACEPTAR){

						bcsComunes.ventanaOverlay($scope.IdVentana, true);
						
						var parametros = {};
						parametros.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
						parametros.p_IdUsuario               	= $scope.SesionIdUsuario; 
						parametros.p_IdModulo                	= 'TES';
						parametros.p_UsaReglasNegocio			= 'S';
						parametros.p_IdTipoCargaMasiva    		= $scope.oConfiguracionTipoCargaMasiva.seleccionado.IdTipoCargaMasiva || 0;						
						parametros.p_CodigoArchivo				= $scope.oConfiguracionTipoCargaMasiva.seleccionado.CodigoArchivo || '';
						parametros.p_FechaCarga					= util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
						parametros.p_CargaMasiva				= cargaMasiva;
						parametros.p_DetalleCargaMasiva         = cargaMasivaDetalle;
						
						bcsComunes.log().info(JSON.stringify(parametros));
						
						bcsServiciosFacturacion.CrearCargaMasiva(parametros,
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
		if (BCS_ESTADO_MODULO['FAC'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}
		if (seleccion.length === 0){
			bcsComunes.ventanaMsg({mensaje: $translate.instant('UI_SELECCIONAR_MOVIMIENTOS_VALIDOS')}, $scope.IdVentana);			
			return;
		}

		var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_ELIMINARAN_LAS_CARGAS_SELECCIONADAS') );
		
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR){

				bcsComunes.ventanaOverlay($scope.IdVentana, true);
				
				var arrCargaMasivaEliminar = [];
				arrCargaMasivaEliminar.push({IdCargaMasiva  : seleccion.IdCargaMasiva});

				var datos = {};
				datos.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
				datos.p_IdUsuario               	= $scope.SesionIdUsuario; 
				datos.p_IdModulo                	= 'TES';
				datos.p_UsaReglasNegocio			= 'S';
				datos.p_CargaMasivaEliminar			= arrCargaMasivaEliminar;		
				  
				bcsServiciosFacturacion.EliminarCargaMasiva(
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

	
	$scope.procesarCargarMasiva = function(seleccion) {
		if (BCS_ESTADO_MODULO['FAC'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}
		if (seleccion.IdEstadoCargaMasiva === 'PRO') // Carga masiya ya procesada
		{	
			bcsComunes.ventanaMsg({mensaje:$translate.instant('CARGA_MASIVA_PROCESADA')},$scope.IdVentana);
			return false;
		}
		
		if (seleccion.IdEstadoCargaMasiva === 'INV') // Carga masiya es invalida(tiene registros invalidos)
		{	
			bcsComunes.ventanaMsg({mensaje:$translate.instant('CARGA_MASIVA_INVALIDA')},$scope.IdVentana);
			return false;
		}
			
		var ventanaComplemento = bcsComunes.ventanaAbrir("ComplementoCargaMasivaFacturacion",seleccion,false,false,$scope.IdVentana);		
		bcsComunes.ventanaPromesa(ventanaComplemento).then(function(respuesta){
			
			if (!respuesta)
				return;

			$scope.buscarCargaMasiva();
		});
		
	}
	
	
});
