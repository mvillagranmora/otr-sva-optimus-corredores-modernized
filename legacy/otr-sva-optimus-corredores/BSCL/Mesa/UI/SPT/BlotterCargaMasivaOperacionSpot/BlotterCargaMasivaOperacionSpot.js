angular.module('ModuloMesa').controller('BlotterCargaMasivaOperacionSpotCtrl',function($scope,$translate,$timeout,$rootScope,bcsComunes,$translate,bcsAlerta,OPCIONES_BCSALERTA,
																						bcsServiciosMesa,
																						bcsServiciosCargaMasivaOperacionSpot,bcsBlotterCargaMasivaOperacionSpotGrid)
{
	
	$scope.data = {};
	
	$scope.IdVentana = bcsComunes.ventanaWinId ($scope);
	bcsComunes.ventanaAutoOverlay($scope.IdVentana,false,3000);
	

	$scope.kngFechaCargaDesde = $scope.SesionFechaSistema;
	$scope.kngFechaCargaHasta = util.dateInicioDia($scope.SesionFechaSistema);
	
	
	//------------------------------------------------
	// CONFIGURACION COMBOBOX
	//------------------------------------------------
	       
	$scope.oConfiguracionTipoCargaMasiva = {
			tipo 			: "combobox-tipo-carga-operacion-spot",
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
			tipo : "combobox-Estado-Carga-Masiva",
			configuracion	: {},
			filtros 		: {},
			idActual 		: false,
			seleccionado 	: false,
			idDefecto 		: ''			
	};
	
	
	//------------------------------------------------
	// CONFIGURACION DE GRILLA
	//------------------------------------------------	
	var dsCargaMasiva = bcsBlotterCargaMasivaOperacionSpotGrid.dsCargaMasiva;
	$scope.confBasicaCargaMasiva = {
			dataSource			: dsCargaMasiva,		
			columns				: bcsBlotterCargaMasivaOperacionSpotGrid.columnasCargaMasiva(),		
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
			filters.push({ field: "FechaCarga",        operator: "gte", value: $scope.kngFechaCargaDesde});		
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
		bcsComunes.ventanaAbrir("DetalleCargaMasivaOperacionSpot", {oCargaMasiva: seleccion}, false, false );
	};	
	
	
	//Carga data de archivo a BD(sin procesar)
	$scope.cargarArchivo = function(){

		if ($scope.oConfiguracionTipoCargaMasiva.seleccionado.IdTipoCargaMasiva === 0 || $scope.oConfiguracionTipoCargaMasiva.seleccionado.IdTipoCargaMasiva === undefined)
		{
			bcsComunes.msg("warning","warning",$translate.instant('DEBE_INDICAR_TIPO_CARGA'));
			return;			
		};
		if($scope.oConfiguracionTipoCargaMasiva.seleccionado.CodigoTipoCargaMasiva =="DISTRIBUCION"){
			var datosArchivo = {codigoArchivo: 'CARGA_MASIVA_OPERACIONES_SPOT_DISTRIBUCION'};
		};
		if($scope.oConfiguracionTipoCargaMasiva.seleccionado.CodigoTipoCargaMasiva =="INTERBANCARIO"){
			var datosArchivo = {codigoArchivo: 'CARGA_MASIVA_OPERACIONES_SPOT_INTERBANCARIO'};
		};
		
		var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,true,true);			
		var promesa = bcsComunes.ventanaPromesa(id1);
		 
		promesa.then(function(data){
			 
			if(!data || !data.fileData)
			{
				return;
			}

			var cargaMasiva = [];
			cargaMasiva.push({
				IdCargaMasiva             	: 1,
				IdTipoCargaMasiva    		: $scope.oConfiguracionTipoCargaMasiva.seleccionado.IdTipoCargaMasiva || 0,
				NombreArchivo	        	: data.fileName
			});
			 

			var cargaMasivaDetalle = [];
			let mensajeErrorValidacion = {
				P_OUT_RESULTADO : [],
				P_OUT_ERRORESCONTADOR : 0
			};
			$.each(data.fileData, function(key, value){
				cargaMasivaDetalle.push({

					IdCargaDetalle 					: 1	,	//solo referencio
					DocumentoIdentidad				: value.DOCUMENTOIDENTIDAD			|| '',
					CodigoPortafolio				: value.CODIGOPORTAFOLIO			|| '',
					ProductoAgrupador				: value.PRODUCTOAGRUPADOR			|| '',
					TipoOperacion					: value.TIPOOPERACION				|| '',
					FechaOperacion					: value.FECHAOPERACION				|| '',
					HoraOperacion					: value.HORAOPERACION				|| '090000' ,
					CanalIngreso					: value.CANALINGRESO				|| '',
					MontoMonedaPrincipal			: value.MONTOMONEDAPRINCIPAL		|| 0,
					TipoCambioCierre				: value.TIPOCAMBIOCIERRE			|| 0,
					TipoCambioCosto					: value.TIPOCAMBIOCOSTO				|| 0,
					PuntaCompra						: value.PUNTACOMPRA					|| 0,
					PuntaVenta						: value.PUNTAVENTA					|| 0,
					FormaPagoEntregamos				: value.FORMAPAGOENTREGAMOS			|| '',
					FormaPagoRecibimos				: value.FORMAPAGORECIBIMOS			|| '',
					Observacion						: value.OBSERVACION					|| '',
					AfectoADerivado					: value.AFECTODERIVADO				|| '',
					DolarObservado					: value.DOLAROBSERVADO				|| '',
					CodigoComercio					: value.CODIGOCOMERCIO				|| 0,
					TipoComprobanteFacturacion		: value.TIPOCOMPROBANTEFACTURACION	|| ''
				});
				if(value.MENSAJEVALIDACION != "OK"){
					let mensaje = {
						IdInterno : cargaMasivaDetalle.length,
						tipo : "ERROR",
						descripcion : value.MENSAJEVALIDACION,
						codigo : "UI_REGISTROS_CARGA_ERROR"
					};
					mensajeErrorValidacion.P_OUT_RESULTADO.push(mensaje);
				} 
			});
			
			mensajeErrorValidacion.P_OUT_ERRORESCONTADOR = mensajeErrorValidacion.P_OUT_RESULTADO.length;
			
			if(mensajeErrorValidacion.P_OUT_ERRORESCONTADOR > 0){
				bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: $translate.instant('UI_RESULTADOS'), campoSecuencia : "IdInterno" ,nombreSecuencia : 'UI_SECUENCIA',datos : mensajeErrorValidacion.P_OUT_RESULTADO } ,false,false,$scope.winID);
				bcsComunes.ventanaOverlay($scope.IdVentana, false);
				return;
			}
			
			var pregunta = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_CARGARAN_LOS_DATOS_DESDE_EL_ARCHIVO') );		
			pregunta.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.ACEPTAR){

					bcsComunes.ventanaOverlay($scope.IdVentana, true);
					
					var parametros = {};
					parametros.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
					parametros.p_IdUsuario               	= $scope.SesionIdUsuario; 
					parametros.p_IdModulo                	= 'MSP';
					parametros.p_FechaCarga					= util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
					parametros.p_Archivo					= cargaMasiva;
					parametros.p_DetalleCargaMasiva         = cargaMasivaDetalle;
					
					bcsComunes.log().info(JSON.stringify(parametros));
					bcsServiciosCargaMasivaOperacionSpot.CrearCargaMasivaOperacionSpot(parametros, function(respuesta){
						bcsComunes.ventanaOverlay($scope.IdVentana, false);
								
						if (respuesta.P_OUT_ERRORESCONTADOR === 0){	
							bcsComunes.ventanaMsg({
								mensaje:$translate.instant("UI_CARGA_MASIVA_EXITOSA"),
								conflictos:util.parseServerResponse(respuesta)
							},$scope.IdVentana);
							$scope.buscarCargaMasiva();									
						} else {	
							respuesta.P_OUT_MENSAJES.forEach(element => {
								if(element.tipo == "ERROR"){
									respuesta.P_OUT_ERRORESCONTADOR ++;
								}
								element.descripcion = "Registro " + element.id + ": " + element.descripcion;
								
							});
							bcsComunes.ventanaMsg({
								mensaje:$translate.instant("UI_ERROR_GUARDAR"),
								conflictos:util.parseServerResponse(respuesta)
							},$scope.IdVentana);
						}
					},function(respuesta){
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_ERROR_GUARDAR"),
							conflictos:util.parseServerResponse(respuesta)
						},$scope.IdVentana);
						bcsComunes.ventanaOverlay($scope.IdVentana, false);
					});
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
		if (seleccion.IdEstadoCargaMasiva !== "ING"){
			bcsComunes.msg("warning","warning",$translate.instant('UI_SELECCIONAR_MOVIMIENTOS_VALIDOS'));
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
			datos.p_IdModulo                	= 'MSP';
			datos.p_UsaReglasNegocio			= 'S';
			datos.p_SimularOperacion			= 'N';
			datos.p_Operaciones					= arrCargaMasivaEliminar;				
			
				bcsServiciosCargaMasivaOperacionSpot.AnularRegistroCargaMasivaOperacionSpot(
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
								mensaje:$translate.instant("UI_ERROR_ANULAR"),
								conflictos:util.parseServerResponse(respuesta)
							},$scope.IdVentana);
						}
					},
					function(respuesta){
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_ERROR_ANULAR"),
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
		
		var id1 = bcsComunes.ventanaAbrir("ProcesarCargaMasivaOperacionSpotModal",{CargaMasiva : oSeleccion},false,false,$scope.IdVentana);
        var promesa=bcsComunes.ventanaPromesa(id1);
        
        promesa.then(function(data){ 
        	if (data === true)
			{	
        		$scope.gridApi.dataSource.read();	
			}
    	});  
        
	};

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
						var p_CargaMasivaProcesar = [];
						p_CargaMasivaProcesar.push({IdCargaMasiva  : seleccion.IdCargaMasiva});

						var datos = {};
						datos.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
						datos.p_IdUsuario               	= $scope.SesionIdUsuario; 
						datos.p_IdModulo                	= 'MSP';
						datos.p_UsaReglasNegocio			= 'S';
						datos.p_FechaProceso	   		    = util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
						datos.p_CargaMasivaProcesar			= p_CargaMasivaProcesar;

						bcsServiciosCargaMasivaOperacionSpot.ProcesarCargaMasivaOperacionSpot(
							datos,
							function(respuesta){
								bcsComunes.ventanaOverlay($scope.IdVentana, false);
								respuesta.P_OUT_MENSAJES.forEach(element => {
									if(element.tipo == "ERROR"){
										respuesta.P_OUT_ERRORESCONTADOR ++;
									}
									element.descripcion = "Registro " + element.id + ": " + element.descripcion;
									
								});

									var mensajeCarga = new Array();
									$.each(respuesta.P_OUT_MENSAJES,function(key,value){
										var objCarga = {
												IdInterno : value.id,
												tipo : value.tipo === 'OK' ? 'INFORMACION' : value.tipo,
												codigo : value.codigo,
												descripcion : $translate.instant(value.descripcion)
										};
										mensajeCarga.push(objCarga);
									});
									respuesta.P_OUT_RESULTADO = mensajeCarga;
									bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: $translate.instant('UI_RESULTADOS'), campoSecuencia : "IdInterno" ,nombreSecuencia : 'UI_SECUENCIA',datos : respuesta.P_OUT_RESULTADO } ,false,false,$scope.winID);							
					
									
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
			};			
		}

		return;					
	};		
});
