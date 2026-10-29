angular.module('ModuloPlataforma').controller('BlotterCargaMasivaPlataformaCtrl',
			function($scope,$translate,$timeout,$rootScope,bcsComunes,$translate,bcsAlerta,OPCIONES_BCSALERTA,
					bcsServicioPlataforma,bcsServiciosCargaMasivaPlataforma,bcsBlotterCargaMasivaPlataformaGrid)
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
			tipo 			: "combobox-tipo-carga-masiva-plataforma",
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
			tipo : "combobox-estado-pla",
			configuracion	: {},
			filtros 		: {
								 NombreEntidad : {"field": "NombreEntidad","operator": "eq", "value"   : 'PLATAFORMA.CARGAMASIVA'},
							  },
			idActual 		: false,
			seleccionado 	: false,
			idDefecto 		: ''			
	};
	
	

	
	//------------------------------------------------
	// CONFIGURACION DE GRILLA
	//------------------------------------------------	
	var dsCargaMasiva = bcsBlotterCargaMasivaPlataformaGrid.dsCargaMasiva;
	$scope.confBasicaCargaMasiva = {
			dataSource			: dsCargaMasiva,		
			columns				: bcsBlotterCargaMasivaPlataformaGrid.columnasCargaMasiva(),		
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
		$scope.oConfiguracionEstado.idActual = -1;
		$scope.oConfiguracionEstado.seleccionado = undefined;
		dsCargaMasiva.removeAll();
	};
	
	
	$scope.detalleCargaMasiva = function (seleccion)
	{	
		bcsComunes.ventanaAbrir("DetalleCargaMasivaPlataforma", {oCargaMasiva: seleccion}, false, false );
	};	
	
	
	
	//Carga data de archivo a BD(sin procesar)
	$scope.cargarArchivo = function(){
	var tipo = '';
		
		if ($scope.oConfiguracionTipoCargaMasiva.seleccionado.IdTipoCargaMasiva === 0 || $scope.oConfiguracionTipoCargaMasiva.seleccionado.IdTipoCargaMasiva === undefined)
		{
			bcsComunes.msg("warning","warning",$translate.instant('DEBE_INDICAR_TIPO_CARGA'));
			return;			
		};
		
		var codigoArchivo = '';
		
		if ($scope.oConfiguracionTipoCargaMasiva.seleccionado.IdTipoCargaMasiva === 1) {
		 	codigoArchivo = 'CARGA_PORCENTAJE_DESCUENTO_RF'
		}
		
		if ($scope.oConfiguracionTipoCargaMasiva.seleccionado.IdTipoCargaMasiva === 2) {
		 	codigoArchivo = 'CARGA_VALOR_RIESGO_RV'
		}

		var datosArchivo = {codigoArchivo: codigoArchivo}
	
		
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
				   	IdInterno						: 1,
			    	Nemotecnico						: value.NEMOTECNICO			|| '',
			    	RF_PorcentajeDescuento			: value.PORCENTAJEDESCUENTO	|| '0',
			    	RV_IdEntidadClasificadoraRiesgo	: value.ENTIDAD				|| '',
			    	RV_Valor						: value.VALOR				|| ''
			 	});
			 });			
			 
			 var pregunta = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_CARGARAN_LOS_DATOS_DESDE_EL_ARCHIVO') );		
			 pregunta.result.then(function(result){
					if (result===OPCIONES_BCSALERTA.ACEPTAR){

						bcsComunes.ventanaOverlay($scope.IdVentana, true);
						
						var parametros = {};
					
						parametros.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
						parametros.p_IdUsuario               	= $scope.SesionIdUsuario; 
						parametros.p_IdModulo                	= 'PLA';
						parametros.p_UsaReglasNegocio			= 'S';
						parametros.p_FechaCarga					= util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
						parametros.p_CargaMasiva				= cargaMasiva;
						parametros.p_DetalleCargaMasiva         = cargaMasivaDetalle;
						
						bcsComunes.log().info(JSON.stringify(parametros));
						bcsServiciosCargaMasivaPlataforma.CrearCargaMasivaPlataforma(parametros,
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
		
		if (seleccion.IdEstadoCargaMasiva != "ING" && seleccion.IdEstadoCargaMasiva != "INV"){
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
			datos.p_IdModulo                	= 'PLA';
			datos.p_UsaReglasNegocio			= 'S';
			datos.p_CargaMasivaEliminar			= arrCargaMasivaEliminar;				
			datos.p_CargaMasivaEstado			= arrCargaMasivaEstado;
			
				bcsServiciosCargaMasivaPlataforma.EliminarCargaMasivaPlataforma(
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
		
		var id1 = bcsComunes.ventanaAbrir("ProcesarCargaMasivaPlataformaModal",{CargaMasiva : oSeleccion},false,false,$scope.IdVentana);
        var promesa=bcsComunes.ventanaPromesa(id1);
        
        promesa.then(function(data){ 
        	if (data === true)
			{	
        		$scope.gridApi.dataSource.read();	
			}
    	});  
        
	};

	//Procesa cargas masivas
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

						var datos = {};
						datos.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
						datos.p_IdUsuario               	= $scope.SesionIdUsuario; 
						datos.p_IdModulo                	= 'PLA';
						datos.p_UsaReglasNegocio			= 'S';
						datos.p_FechaProceso	   		    = util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
						datos.p_CargaMasivaProcesar			= seleccion.IdCargaMasiva;

						bcsServiciosCargaMasivaPlataforma.ProcesarCargaMasivaPlataforma(
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
			};			
		}

		return;					
	};		
});
