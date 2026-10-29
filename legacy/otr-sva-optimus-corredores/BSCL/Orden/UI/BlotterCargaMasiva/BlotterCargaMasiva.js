angular.module('ModuloOrdenes').controller('BlotterCargaMasivaOrdenesCtrl',
			function($scope,$translate,$timeout,$rootScope,bcsComunes,$translate,bcsAlerta,OPCIONES_BCSALERTA, bcsServicioOrden,bcsBlotterCargaMasivaGrid)
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
			tipo 			: "combobox-tipo-carga-masiva-ordenes",
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
			tipo : "combobox-estado-confirmacion-orden",
			configuracion	: {},
			filtros 		: {
								 NombreEntidad : {"field": "NombreEntidad","operator": "eq", "value"   : 'ORDEN.CARGAMASIVA'},
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
		bcsComunes.ventanaAbrir("DetalleCargaMasivaOperacionesEnRueda", seleccion, false, false,$scope.IdVentana );
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
			 
			 //Cabecera Carga Masiva(Datos generales de la carga)
			 cargaMasiva.push({
			    	  IdInterno             : 1,
			    	  NombreArchivo	        : data.fileName,
					  CodigoTipoCargaMasiva : $scope.oConfiguracionTipoCargaMasiva.seleccionado.CodigoTipoCargaMasiva || ''
			 });

			 //Detalle  Carga "CARGA_INGRESO_ORDEN_MASIVO"	 
			 if ($scope.oConfiguracionTipoCargaMasiva.seleccionado.CodigoTipoCargaMasiva === 'CARGA_INGRESO_ORDEN_MASIVO')
			 {
				 $.each(data.fileData, function(key, value){
					 cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'DocumentoIdentidad',			ValorCampo: value.DOCUMENTO_IDENTIDAD.toString().trim()});
					 cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'CodigoPortafolio',			ValorCampo: value.CODIGO_PORTAFOLIO.toString().trim()});
					 cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'TipoOperacion',				ValorCampo: value.TIPO_OPERACION.toString().trim()});
					 cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'IdMecanismoCaptacion',		ValorCampo: value.MECANISMO_CAPTACION.toString().trim()});
					 cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'Cantidad',					ValorCampo: value.CANTIDAD.toString()});
					 cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'Nemotecnico',					ValorCampo: value.NEMOTECNICO.toString().trim()});
					 cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'Precio',						ValorCampo: value.PRECIO.toString()});
					 cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'IdCondicionDeLiquidacion',	ValorCampo: value.CONDICION_LIQUIDACION.toString().trim()});
					 cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'IdTipoVigenciaOrden',			ValorCampo: value.TIPO_VIGENCIA.toString().trim()});
					 cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'FechaVigencia',				ValorCampo: value.FECHA_VIGENCIA.toString()});
					 cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'IdFormaCustodia',				ValorCampo: value.FORMA_CUSTODIA.toString().trim()});
					 cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'IdBolsa',						ValorCampo: value.BOLSA.toString().trim()});
					 cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'IdBoveda',					ValorCampo: value.BOVEDA.toString().trim()});				 
					 cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'IdTipoPrecioOrden',			ValorCampo: value.TIPO_PRECIO.toString().trim()});
				     idRegistro++;
				 });
			 }

			 //Detalle para carga "CARGA_INGRESO_INSTRUCCIONES_MASIVA"
			 if ($scope.oConfiguracionTipoCargaMasiva.seleccionado.CodigoTipoCargaMasiva === 'CARGA_INGRESO_INSTRUCCIONES_MASIVA')
			 {
				 $.each(data.fileData, function(key, value){
					cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'TipoInstruccion',			ValorCampo: value.CINS_TIPO_INSTRUCCION.toString()});
					cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'TipoOperacion',			ValorCampo: value.CINS_OPERACION.toString()});
					cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'Nemotecnico',				ValorCampo: value.CINS_NEMOTECNICO.toString()});
					cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'Emisor',					ValorCampo: value.CINS_EMISOR.toString()});
					cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'FechaVencimiento',			ValorCampo: value.CINS_FECHA_VENCIMIENTO.toString()});					
					cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'ValorRescate',				ValorCampo: value.CINS_VALOR_RESCATE.toString()});
					cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'FechaTrade',				ValorCampo: value.CINS_FECHA_TRADE.toString()});
					cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'IdCondicionDeLiquidacion',	ValorCampo: value.CINS_CONDICION_LIQUIDACION.toString()});
					cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'Intermediario',			ValorCampo: value.CINS_INTERMEDIARIO.toString()});
					cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'FolioFactura',				ValorCampo: value.CINS_FOLIO_FACTURA.toString()});
					cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'Cantidad',					ValorCampo: value.CINS_CANTIDAD.toString()});
					cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'MontoLiquidacion',			ValorCampo: value.CINS_TOTAL_MONTO.toString()});
					cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'Tasa',						ValorCampo: value.CINS_TASA.toString()});
					cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'DocumentoIdentidad',		ValorCampo: value.CINS_DOCUMENTO_IDENTIDAD.toString()});
					cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'CodigoPortafolio',			ValorCampo: value.CINS_PORTAFOLIO.toString()});						    						   					
					cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'Precio',					ValorCampo: value.CINS_PRECIO.toString()});
					cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'IdTipoReajuste',			ValorCampo: value.CINS_TIPO_REAJUSTE.toString()});						    						   
					cargaMasivaDetalle.push({IdInterno: 1, IdRegistro: idRegistro, NombreCampo: 'TotalCobros',				ValorCampo: value.CINS_TOTAL_COBROS.toString()});						    						   
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
					parametros.p_IdModulo                	= 'ORD';
					parametros.p_UsaReglasNegocio			= 'S';
					parametros.p_FechaCarga					= util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
					parametros.p_CargaMasiva				= cargaMasiva;
					parametros.p_CargaMasivaDetalle 		= cargaMasivaDetalle;
					
					bcsComunes.log().info(JSON.stringify(parametros));
					bcsServicioOrden.CrearCargaMasiva(parametros,
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
				datos.p_IdModulo                	= 'ORD';
				datos.p_UsaReglasNegocio			= 'S';
				datos.p_CargaMasivaEliminar			= arrCargaMasivaEliminar;		
				
				bcsServicioOrden.EliminarCargaMasiva(
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
		
		//Detalle  Carga "CARGA_INGRESO_ORDEN_MASIVO"	 
		if ($scope.oConfiguracionTipoCargaMasiva.seleccionado.CodigoTipoCargaMasiva === 'CARGA_INGRESO_ORDEN_MASIVO')
		{
			var ventanaComplemento = bcsComunes.ventanaAbrir("ComplementoCargaMasivaOperacionesEnRueda",seleccion,true,false,$scope.IdVentana);		
		}

		//Detalle para carga "CARGA_INGRESO_INSTRUCCIONES_MASIVA"
		if ($scope.oConfiguracionTipoCargaMasiva.seleccionado.CodigoTipoCargaMasiva === 'CARGA_INGRESO_INSTRUCCIONES_MASIVA')
		{
			var ventanaComplemento = bcsComunes.ventanaAbrir("ComplementoCargaMasivaOperacionesEnRueda",seleccion,true,false,$scope.IdVentana);	
		}
		
		bcsComunes.ventanaPromesa(ventanaComplemento).then(function(respuesta){			
			if (!respuesta)
				return;

			$scope.buscarCargaMasiva();
		});
	}
	
	
});
