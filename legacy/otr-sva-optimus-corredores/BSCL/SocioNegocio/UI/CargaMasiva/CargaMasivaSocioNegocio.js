angular.module('ModuloSocioNegocio').controller('CargaMasivaSocioNegocioCtrl',
			function($scope,$translate,$timeout,$rootScope,bcsComunes,$translate,bcsAlerta,OPCIONES_BCSALERTA,
					servicioSociodeNegocio,bcsCargaMasivaGrid,bcsServicioPlataforma)
{
	
	$scope.data = {};
	
	$scope.IdVentana = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.IdVentana,false,3000);
	
	$scope.kngFechaCargaDesde = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.kngFechaCargaHasta = util.dateInicioDia($scope.SesionFechaSistema);
	
	
	//------------------------------------------------
	// CONFIGURACION COMBOBOX
	//------------------------------------------------
	       
	$scope.oConfiguracionTipoCargaMasiva = {
			tipo 			: "combobox-tipo-carga-masiva-socio-negocio",
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
			tipo 			: "combobox-estado-socio-negocio",
			configuracion	: {},
			filtros 		: {NombreEntidad : {"field": "NombreEntidad","operator": "eq", "value"   : 'CARGAMASIVA'},},
			idActual 		: false,
			seleccionado 	: false,
			idDefecto 		: ''
	};
	
	
	//------------------------------------------------
	// CONFIGURACION DE GRILLA
	//------------------------------------------------	
	var dsCargaMasiva = bcsCargaMasivaGrid.dsCargaMasiva;
	$scope.confBasicaCargaMasiva = {
			dataSource			: dsCargaMasiva,		
			columns				: bcsCargaMasivaGrid.columnasCargaMasiva(),		
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
		bcsComunes.ventanaAbrir("DetalleCargaMasivaSocioNegocio", seleccion, false, false );
	};
	
	
	//Carga data de archivo a BD(sin procesar)
	$scope.cargarArchivo = function(){
		if (BCS_ESTADO_MODULO['SOC'] === "CER") {
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
			if(!data || !data.fileData){
				return;
			}

			var cargaMasiva = [];
			cargaMasiva.push({
				IdInterno		: 1,
				NombreArchivo	: data.fileName
			});
			
			for(var key in data.fileData){
				if(data.fileData[key].MENSAJEVALIDACION!='OK'){
					bcsComunes.msg("error",$translate.instant("UI_CARGA_MASIVA_SOCIO_NEGOCIO"),$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"));
				return;
				}
			}

			var cargaMasivaDetalle = [];
			$.each(data.fileData, function(key, value){
				cargaMasivaDetalle.push({
					IdInterno             	: 1,						  
					DocumentoIdentidad  	: value.UI_DOCUMENTO_IDENTIDAD,
					CodigoPortafolio  		: value.UI_CODIGO_PORTAFOLIO,
					Entidad					: value.UI_ENTIDAD,
					CodigoIndicador			: value.UI_CODIGO_INDICADOR,
					ValorIndicador			: value.UI_VALOR_INDICADOR
				});
			});
			 
			 
			var pregunta = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_CARGARAN_LOS_DATOS_DESDE_EL_ARCHIVO'));			 
			pregunta.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.ACEPTAR){

					bcsComunes.ventanaOverlay($scope.IdVentana, true);
						
					var parametros = {};
					parametros.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
					parametros.p_IdUsuario               	= $scope.SesionIdUsuario; 
					parametros.p_IdModulo                	= 'SOC';
					parametros.p_UsaReglasNegocio			= 'S';
					parametros.p_IdTipoCargaMasiva    		= $scope.oConfiguracionTipoCargaMasiva.seleccionado.IdTipoCargaMasiva || 0;						
					parametros.p_CodigoArchivo				= $scope.oConfiguracionTipoCargaMasiva.seleccionado.CodigoArchivo || '';
					parametros.p_FechaCarga					= util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
					parametros.p_CargaMasiva				= cargaMasiva;
					parametros.p_DetalleCargaMasiva         = cargaMasivaDetalle;
						
					bcsComunes.log().info(JSON.stringify(parametros));
						
					servicioSociodeNegocio.CrearCargaMasiva(parametros,
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
		if (BCS_ESTADO_MODULO['SOC'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}
		if (seleccion.length === 0){
			bcsComunes.ventanaMsg({mensaje: $translate.instant('UI_SELECCIONAR_REGISTROS_VALIDOS')}, $scope.IdVentana);		
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
				datos.p_IdModulo                	= 'SOC';
				datos.p_UsaReglasNegocio			= 'S';
				datos.p_CargaMasivaEliminar			= arrCargaMasivaEliminar;		
				  
				servicioSociodeNegocio.EliminarCargaMasiva(datos,
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
		if (BCS_ESTADO_MODULO['SOC'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}
		if (seleccion.IdEstadoCargaMasiva === 'PRO')
		{	
			bcsComunes.ventanaMsg({mensaje:$translate.instant('CARGA_MASIVA_PROCESADA')},$scope.IdVentana);
			return false;
		}
		
		var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_PROCESARA_LA_CARGA_MASIVA') );
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR){
				bcsComunes.ventanaOverlay($scope.IdVentana, true);
				
				var cargaMasivaProcesar = [];
				cargaMasivaProcesar.push({
					IdCargaMasiva	: $scope.seleccionCargaMasiva.IdCargaMasiva,
					CodigoArchivo	: $scope.seleccionCargaMasiva.CodigoArchivoTipoCargaMasiva,
				});
				
				var datos = {};
				datos.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
				datos.p_IdUsuario               	= $scope.SesionIdUsuario; 
				datos.p_IdModulo                	= 'SOC';
				datos.p_UsaReglasNegocio			= 'S';
				datos.p_CargaMasivaProcesar			= cargaMasivaProcesar;
				datos.p_FechaProceso	   		    = util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
				
				servicioSociodeNegocio.ProcesarCargaMasiva(datos,function(respuesta){
					bcsComunes.ventanaOverlay($scope.IdVentana, true);
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
						bcsComunes.ventanaData($scope.IdVentana,1);
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
				});
			}
		});
		
		return;
	}
	
	
});
