angular.module('ModuloTesoreria').controller('BlotterCargaMasivaTesoreriaCtrl',
			function($scope,$translate,$timeout,$rootScope,bcsComunes,$translate,bcsAlerta,OPCIONES_BCSALERTA,
					bcsServiciosTesoreria,bcsBlotterCargaMasivaGrid)
{
	
	$scope.data = {};
	
	$scope.IdVentana = bcsComunes.ventanaWinId ($scope);
	bcsComunes.ventanaAutoOverlay($scope.IdVentana,false,3000);
	

	$scope.kngFechaCargaDesde = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.kngFechaCargaHasta = util.dateInicioDia($scope.SesionFechaSistema);
	var gridCargaMasiva;
	
	//------------------------------------------------
	// CONFIGURACION COMBOBOX
	//------------------------------------------------

	$scope.oConfiguracionTipoCargaMasiva = {
			tipo 			: "combobox-tipo-carga-masiva-tesoreria",
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
			tipo : "combobox-estado-tesoreria",
			configuracion	: {},
			filtros 		: {
								 NombreEntidad : {"field": "NombreEntidad","operator": "eq", "value"   : 'TES.CargaMasiva'},
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
			userSettings 		: true,
			rowColorBackground	: function(row) { 
									  var color = "";
									  if (row.IdEstadoCargaMasiva == 'ING')
										 color = "yellow";
									  if (row.IdEstadoCargaMasiva == 'INV')
											 color = "red";
									  if (row.IdEstadoCargaMasiva == 'PRO')
											 color = "green";									  
									  return color;
								  },
			gridCreated: function(g)
			{	
				gridCargaMasiva = g;
			}
	};
	
	// --------------------------------------------------
	// FUNCIONES 
	// --------------------------------------------------
	$scope.buscarCargaMasiva = function(){
		var filters = new Array();
		
		//Oculta columnas según tipo de carga           
		if ($scope.oConfiguracionTipoCargaMasiva.seleccionado.CodigoArchivo == 'CONCILIACION_RECAUDACION_BCP')			
			gridCargaMasiva.hideColumn("NombreCaja");
		else
			gridCargaMasiva.showColumn("NombreCaja");
		
		if ($scope.oConfiguracionTipoCargaMasiva.seleccionado.IdTipoCargaMasiva)
			filters.push({ field: "IdTipoCargaMasiva", operator: "eq", value: Number($scope.oConfiguracionTipoCargaMasiva.seleccionado.IdTipoCargaMasiva || 0)});		

		if ($scope.kngFechaCargaDesde)			
			filters.push({ field: "FechaCarga",        operator: "gte", value: util.dateSPdate($scope.kngFechaCargaDesde) });
			
		if ($scope.kngFechaCargaHasta)			
			filters.push({ field: "FechaCarga",        operator: "lte", value: util.dateSPdate($scope.kngFechaCargaHasta) });		
		
		if ($scope.oConfiguracionEstado.seleccionado.IdEstado)
			filters.push({ field: "IdEstadoCargaMasiva", operator: "eq", value: $scope.oConfiguracionEstado.seleccionado.IdEstado});
		
		dsCargaMasiva.filter(filters);	
		
	};
	

	
	$scope.limpiarFiltros = function(){
		$scope.oConfiguracionTipoCargaMasiva.idActual = false;
		$scope.kngFechaCargaDesde = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.kngFechaCargaHasta = util.dateInicioDia($scope.SesionFechaSistema);
		dsCargaMasiva.removeAll();
	};
	
	

	
	$scope.detalleCargaMasiva = function (seleccion)
	{	
		bcsComunes.ventanaAbrir("DetalleCargaMasiva", seleccion, false, false,$scope.IdVentana );
	};	
	
	
	
	//------------------------------------------------------
	//CARGAR DATA DE ARCHIVO EN BD (sin procesar aun)
	//------------------------------------------------------
	$scope.cargarArchivo = function(){
		
		 if ($scope.oConfiguracionTipoCargaMasiva.seleccionado.IdTipoCargaMasiva === 0 || $scope.oConfiguracionTipoCargaMasiva.seleccionado.IdTipoCargaMasiva === undefined || $scope.oConfiguracionTipoCargaMasiva.seleccionado.CodigoTipoCargaMasiva === '' || $scope.oConfiguracionTipoCargaMasiva.seleccionado.CodigoTipoCargaMasiva === undefined)
		 {
			bcsComunes.ventanaMsg({mensaje: $translate.instant('DEBE_INDICAR_TIPO_CARGA')},$scope.IdVentana);
			return;			
		 };
		 
		 var datosArchivo = {codigoArchivo: $scope.oConfiguracionTipoCargaMasiva.seleccionado.CodigoTipoCargaMasiva || ''};		 
		 var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,false,false,$scope.IdVentana);			
		 var promesa = bcsComunes.ventanaPromesa(id1);
		 
		 promesa.then(function(data){
			 //El objeto data, contiene la informacion del archivo en formato JSON.
			 
			 if(!data || !data.fileData)
			 {
				 return;
			 }
			 
			 var countRegistrosInvalidos = 0;
			 $.each(data.fileData, function(key, value){
			     if (value.MENSAJEVALIDACION.toString() != "OK")
			     {
			    	countRegistrosInvalidos++;
			     }
			});
			 
			 if (countRegistrosInvalidos>0)
			 {
				 bcsComunes.ventanaMsg({mensaje: $translate.instant('UI_ARCHIVO_CON_REGISTROS_INVALIDOS')},$scope.IdVentana);
				 return; 
			 }
			 
			 var cargaMasiva = [];
			 var cargaMasivaDetalle = [];			 
			 var idRegistro = 1;
			 
			 
			 cargaMasiva.push({
			    	  IdInterno             : 1,
			    	  NombreArchivo	        : data.fileName,
					  CodigoTipoCargaMasiva : $scope.oConfiguracionTipoCargaMasiva.seleccionado.CodigoTipoCargaMasiva || ''
			 });
			 
			 
			 //Detalle para carga "EGRESOS_POR_ABONOS_BCP_PAC" y "INGRESOS_POR_CARGOS_BCP_PAC"
			 if ($scope.oConfiguracionTipoCargaMasiva.seleccionado.CodigoTipoCargaMasiva === 'EGRESOS_POR_ABONOS_BCP_PAC' || $scope.oConfiguracionTipoCargaMasiva.seleccionado.CodigoTipoCargaMasiva === 'INGRESOS_POR_CARGOS_BCP_PAC')
			 {
				 $.each(data.fileData, function(key, value){
					    cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'TipoCuenta',	ValorCampo: value.TIPO_CUENTA.toString()});
					    cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'NumeroCuenta',	ValorCampo: value.NUMERO_CUENTA.toString()});
					    cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'TipoIDC',		ValorCampo: value.TIPO_IDC.toString()});
					    cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'NumeroIDC',	ValorCampo: value.NUMERO_IDC.toString()});
					    cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'TipoMoneda',	ValorCampo: value.TIPO_MONEDA.toString()});
					    cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'Monto',		ValorCampo: value.MONTO.toString()});
					    cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'Glosa',		ValorCampo: value.GLOSA_1.toString()});						    
					    idRegistro++;
				 });								      
			 }
				 
				 
			 //Detalle para "CARGA_GENERICA_MOVIMIENTOS"
			 if ($scope.oConfiguracionTipoCargaMasiva.seleccionado.CodigoTipoCargaMasiva === 'CARGA_GENERICA_MOVIMIENTOS')
			 {			 
				 $.each(data.fileData, function(key, value){
					cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'TipoMovimiento',	    ValorCampo: value.TCG_TIPOMOVIMIENTO.toString()});
					cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'DocumentoIdentidad',	ValorCampo: value.TCG_DOCUMENTOIDENTIDAD.toString()});
					cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'CodigoPortafolio',		ValorCampo: value.TCG_CODIGOPORTAFOLIO.toString()});
					cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'Glosa',				ValorCampo: value.TCG_GLOSA.toString()});
					cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'Monto',				ValorCampo: value.TCG_MONTO.toString()});
					cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'TipoMoneda',			ValorCampo: value.TCG_IDMONEDA.toString()});					
					cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'PlanTipoInversion',	ValorCampo: value.TCG_PLAN.toString()});
					cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'ProductoAgrupador',	ValorCampo: value.TCG_PRODUCTO.toString()});
					cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'RegimenTributario',	ValorCampo: value.TCG_REGIMEN.toString()});
					idRegistro++;
				 });
			 }
			 
			 
			 //Detalle para "CARGA_RECAUDACION_SERVIPAG"
			 if ($scope.oConfiguracionTipoCargaMasiva.seleccionado.CodigoTipoCargaMasiva === 'CARGA_RECAUDACION_SERVIPAG')
			 {			 
				 $.each(data.fileData, function(key, value){
						 cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'TipoMovimiento',		ValorCampo: value.TIPOMOVIMIENTO.toString()});
						 cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'DocumentoIdentidad',	ValorCampo: value.DOCUMENTOIDENTIDAD.toString()});
						 cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'Monto',				ValorCampo: value.MONTO.toString()});
					     idRegistro++;
				 });
			 }
			 
			 
			 //Detalle para carga "CONCILIACION_RECAUDACION_BCP"
			 if ($scope.oConfiguracionTipoCargaMasiva.seleccionado.CodigoTipoCargaMasiva === 'CONCILIACION_RECAUDACION_BCP')
			 {
				 $.each(data.fileData, function(key, value){
					    cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'CodigoCuenta',				ValorCampo: value.BCP_CODIGO_CUENTA.toString()});		// BIGINT
					    cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'CodigoMoneda',				ValorCampo: value.BCP_CODIGO_MONEDA.toString()});		// BIGINT
					    cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'NumeroCuenta',				ValorCampo: value.BCP_NUMERO_CUENTA.toString()});		// BIGINT
					    cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'IdentificacionDepositante',ValorCampo: value.BCP_IDENTIFICADOR_DEPOSITANTE.toString()});// NVARCHAR(14)
					    cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'FechaTransaccion',			ValorCampo: value.BCP_FECHA_TRANSACCION.toString()}); 	// NVARCHAR(10)
					    cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'Monto',					ValorCampo: value.BCP_MONTO.toString()});   			// DECIMAL(26,8)
					    cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'NumeroOperacion',			ValorCampo: value.BCP_NUMERO_OPERACION.toString()});	// BIGINT
					    cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'ReferenciaOperacion',		ValorCampo: value.BCP_REFERENCIA_OPERACION.toString()});// BIGINT
					    cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'IndicadorExtorno',			ValorCampo: value.BCP_INDICADOR_EXTORNO.toString()});	// NVARCHAR(1)
					    cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'NumeroOperacionBD',		ValorCampo: value.BCP_NUMERO_OPERACION_BD.toString()});	// BIGINT
					    
					    idRegistro++;
				 });								      
			 }
			 
			 

			 var pregunta = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_CARGARAN_LOS_DATOS_DESDE_EL_ARCHIVO') );		
			 pregunta.result.then(function(result){
					if (result===OPCIONES_BCSALERTA.ACEPTAR){

						bcsComunes.ventanaOverlay($scope.IdVentana, true);

						var parametros = {};
						parametros.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
						parametros.p_IdUsuario               	= $scope.SesionIdUsuario; 
						parametros.p_IdModulo                	= 'TES';
						parametros.p_UsaReglasNegocio			= 'S';
						parametros.p_FechaCarga					= util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
						parametros.p_CargaMasiva				= cargaMasiva;
						parametros.p_CargaMasivaDetalle 		= cargaMasivaDetalle;
						
						bcsComunes.log().info(JSON.stringify(parametros));
						bcsServiciosTesoreria.CrearCargaMasiva(parametros,
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
					else
					{
						return; 
					}
			 });
						 

  		}); 
	};
	

	//Elimina cargas masivas sin procesar
	$scope.eliminarCargarMasiva = function(seleccion) {
		
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
				 
				bcsServiciosTesoreria.EliminarCargaMasiva(
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
			
		var ventanaComplemento = bcsComunes.ventanaAbrir("ComplementoCargaMasiva",seleccion,true,false,$scope.IdVentana);		
		bcsComunes.ventanaPromesa(ventanaComplemento).then(function(respuesta){
			
			if (!respuesta)
				return;

			$scope.buscarCargaMasiva();
		});
		
	}
	
	var firstHeightCrear = $('#Win_CargaMasivaTesoreria');
        firstHeightCrear.css('height', '510px');
	
});
