angular.module('ModuloCuentaInversion').controller('BlotterCargaMasivaCuentaInversionCtrl',
			function($scope,$translate,$timeout,$rootScope,bcsComunes,$translate,bcsAlerta,OPCIONES_BCSALERTA,
					bcsServiciosCuentaInversion,bcsBlotterCargaMasivaGrid)
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
			tipo 			: "combobox-tipo-carga-masiva-cuentainversion",
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
			tipo : "combobox-estado-cuenta-inversion",
			configuracion	: {},
			filtros 		: {
								 NombreEntidad : {"field": "NombreEntidad","operator": "eq", "value"   : 'CTA.CargaMasiva'},
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
		bcsComunes.ventanaAbrir("DetalleCargaMasivaCtaInversion", seleccion, false, false,$scope.IdVentana );
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
			 
			 if(!data || !data.fileData)
			 {
				 return;
			 }
			 
        
			 var cargaMasiva = [];
			 var cargaMasivaDetalle = [];			 
			 var idRegistro = 1;
			 var TipoCargaSeleccionada ="";
			 
			 cargaMasiva.push({
			    	  IdInterno             : 1,
			    	  NombreArchivo	        : data.fileName,
					  CodigoTipoCargaMasiva : $scope.oConfiguracionTipoCargaMasiva.seleccionado.CodigoTipoCargaMasiva || ''
			 });
			 
			 if ($scope.oConfiguracionTipoCargaMasiva.seleccionado.CodigoTipoCargaMasiva === 'CARGA_ABONO' || $scope.oConfiguracionTipoCargaMasiva.seleccionado.CodigoTipoCargaMasiva === 'CARGA_CARGO')
			 {
				$.each(data.fileData, function(key, value){
					  
					cargaMasivaDetalle.push({
						IdInterno:  idRegistro,					 
						RutCliente: value.UI_RUT_CLIENTE ? value.UI_RUT_CLIENTE.toString() : '',
						Portafolio: value.UI_PORTAFOLIO != null ? value.UI_PORTAFOLIO.toString() : '',
						Moneda:     value.UI_MONEDA ? value.UI_MONEDA.toString() : '',
					
						TipoMovimiento:    value.UI_TIPO_MOVIMIENTO ? value.UI_TIPO_MOVIMIENTO.toString() : '',
						IdConcepto:        value.UI_CONCEPTO ? value.UI_CONCEPTO.toString() : '',
						Monto:             value.UI_MONTO ? value.UI_MONTO.toString() : '',
						Descripcion:       value.UI_DESCRIPCION ? value.UI_DESCRIPCION.toString() : '',
						Plan:              value.UI_PLAN ? value.UI_PLAN.toString() : '',
						ProductoAgrupador: value.UI_PRODUCTO_AGRUPADOR ? value.UI_PRODUCTO_AGRUPADOR.toString() : ''
					});
					idRegistro++;
			    });								      
			 }
				if ($scope.oConfiguracionTipoCargaMasiva.seleccionado.CodigoTipoCargaMasiva === 'CARGA_ABONO') 
				{
					TipoCargaSeleccionada = "MOVABN";
				}
				
			else if ($scope.oConfiguracionTipoCargaMasiva.seleccionado.CodigoTipoCargaMasiva === 'CARGA_CARGO')
				{
					TipoCargaSeleccionada = "MOVCAR";
				}
			
			 var pregunta = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_CARGARAN_LOS_DATOS_DESDE_EL_ARCHIVO') );		
			 pregunta.result.then(
					 function(result){
					if (result===OPCIONES_BCSALERTA.ACEPTAR){		
						
						bcsComunes.ventanaOverlay($scope.IdVentana, true);

						var parametros = {};
						parametros.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
						parametros.p_IdUsuario               	= $scope.SesionIdUsuario; 
						parametros.p_IdModulo                	= 'CTA';
						parametros.p_UsaReglasNegocio			= 'S';
						parametros.p_TipoCargaSeleccionada		= TipoCargaSeleccionada;
						parametros.p_FechaCarga					= util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
						parametros.p_CargaMasiva				= cargaMasiva;
						parametros.p_CargaMasivaDetalle 		= cargaMasivaDetalle;
						
						bcsComunes.log().info(JSON.stringify(parametros));
						bcsServiciosCuentaInversion.CrearCargaMasiva(parametros,
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
				arrCargaMasivaEliminar.push({p_IdCarga  : seleccion.IdCargaMasiva});

				var datos = {};
				datos.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
				datos.p_IdUsuario               	= $scope.SesionIdUsuario; 
				datos.p_IdModulo                	= 'CTA';
				datos.p_UsaReglasNegocio			= 'S';
				datos.p_IdCargaMasiva    		    = arrCargaMasivaEliminar;
							
				
				bcsServiciosCuentaInversion.EliminaCargaSeleccionada(
					datos,
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
		return;					
	};	

	
	
	
	$scope.procesarCargarMasiva = function(seleccion) {
		 
		if (seleccion.IdEstadoCargaMasiva === 'PRO')
		{	
			bcsComunes.ventanaMsg({mensaje:$translate.instant('CARGA_MASIVA_PROCESADA')},$scope.IdVentana);
			return false;
		}
		if (seleccion.IdCargaMasiva === 0  || seleccion.IdCargaMasiva === undefined || seleccion.IdCargaMasiva === '')
		{
			bcsComunes.ventanaMsg({mensaje: $translate.instant('UI_FOLIO_CARGA_INCORRECTO')}, $scope.IdVentana);			
			return;
		}	
		else if (seleccion.IdCargaMasiva > 0 )
		{		
		 var pregunta = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_PROCESARA_LA_CARGA_SELECCIONADA') );		
		 pregunta.result.then(
				 function(result){
				if (result===OPCIONES_BCSALERTA.ACEPTAR){
					bcsComunes.ventanaOverlay($scope.IdVentana, true);
					var parametros = {};
					parametros.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
					parametros.p_IdUsuario               	= $scope.SesionIdUsuario; 
					parametros.p_IdModulo                	= 'CTA';
					parametros.p_UsaReglasNegocio			= 'S';
					parametros.p_FechaCarga					= util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
					parametros.p_IdCargaMasiva				= seleccion.IdCargaMasiva;					
					bcsComunes.log().info(JSON.stringify(parametros));
					bcsServiciosCuentaInversion.ProcesaCargaSeleccionada(parametros,
						function(respuesta){
							bcsComunes.ventanaOverlay($scope.IdVentana, false);
							bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: $translate.instant('UI_RESULTADO'), campoSecuencia : "id" ,nombreSecuencia : 'UI_CARGA_MASIVA',datos : respuesta.P_OUT_MENSAJES } ,false,false,$scope.IdVentana);
							$scope.buscarCargaMasiva();
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
		 		 
		}
	}
	var firstHeight = $('#Win_CargaMasivaCuentaInversion');
        firstHeight.css('height', '510px');
});
