angular.module('ModuloCustodia').controller('ConsultarCuadraturaCustodiaCtrl', 
		function($scope,
				$timeout,
				$modal,
				$state,
				bcsComunes,
				bcsAlerta, 
				OPCIONES_BCSALERTA, 
				bcsServicioPlataforma, 
				bcsBlotterCuadraturaCustodiaGrid,
				bcsServiciosMovTransito,
				bcsServicioPlataforma,
				bcsServiciosCuadraturaCustodia,
				$translate,
				$resource)
{	
	
	//bcsBlotterCuadraturaCustodiaGrid
	
	//*** configuracion de objetos ***//
	
	$scope.filtro = {};
	$scope.data ={};
	var idVentana = bcsComunes.ventanaWinId($scope);
	$scope.idVentana = idVentana;
	
	$scope.data.oConfiguracionClasificacionTipoInstrumento={
			tipo : "combobox-clasificacion-tipo-instrumento",
			configuracion : {},
			filtros : { 	
				IdClasificacionRE : { "field"   : "IdClasificacionTipoInstrumento","operator": "neq", "value"   : "RE"},
				IdClasificacionDV : { "field"   : "IdClasificacionTipoInstrumento","operator": "neq", "value"   : "DV"}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};	
	$scope.data.oConfiguracionTipoInstrumento={
			tipo : "combobox-tipo-instrumento",
			configuracion : {},
			filtros : { 	
				IdClasificacionTipoInstrumento : {
			        "field"   : "IdClasificacionTipoInstrumento",
			        "operator": "eq",
			        "value"   : ""
				}				
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};	
	$scope.data.oConfiguracionBoveda={
			tipo : "combobox-boveda",
			configuracion : {},
			filtros : {
				
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	var dsGrillaCuadraturaCustodia = new bcs.DataSource({
	               type : "odata",
	               definition : {
	                               serverFiltering 	: true, // Filtrado de datos en el servidor.
	                               serverPaging     : true, // Paginado de los datos en el servidor.
	                               serverSorting    : true, // Ordenamiento de los datos en el servidor.
	                               //sort 			: { field: "IdMovimientoCarteraTransito", dir: "desc" },
	                               filter 			: { field: "IdEstado", operator: "contains", value: 'ACT' },
	                               pageSize         : 100,
	                               transport 		: {
														read : {
															url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=521",
															dataType : "json"
												}
	                               },
	                               schema 			: modeloCuadraturaCustodia()
	               }
	});
	
   	//GRILLA PRINCIPAL
	$scope.confBasicaCuadraturaCustodia = {
		dataSource			: dsGrillaCuadraturaCustodia,
		columns				: bcsBlotterCuadraturaCustodiaGrid.columnasCuadraturaCustodia(),		
		sortable			: true,
		filterable 			: true,
		userSettings 		: true,
		excel 				: { allPages : true },
		pageable			: true,
		height				: 450,
		selectable 			: "multiple row",
	    gridCreated : function(grid) {
	    	grillaCustPortafolio = grid;
	    	if ($scope.SesionLocale !== 'PE'){
	    		grid.showColumn("CantidadSimultaneaVenta");
	    		grid.showColumn("CantidadSimultaneaCompra");
			}
	    }
		
	};
	
   	//********* fin configuracion de objetos *********//
	
/*************************************************************************
 * BUSCAR
 *************************************************************************/
	
	$scope.buscarMovimientos = function(){
		var filters = new Array();
		
		try {
        	if ($scope.filtro.clasificacionTipoInstrumentoObj !== undefined && $scope.filtro.clasificacionTipoInstrumentoObj != "" && $scope.filtro.clasificacionTipoInstrumentoObj.IdClasificacionTipoInstrumento != "0")
        		filters.push({ field: "IdClasificacionTipoInstrumento", operator: "contains", value: $scope.filtro.clasificacionTipoInstrumentoObj.IdClasificacionTipoInstrumento });
		
        	if ($scope.filtro.tipoInstrumento.IdTipoInstrumento !== undefined && $scope.filtro.tipoInstrumento.IdTipoInstrumento != "" && $scope.filtro.tipoInstrumento.IdTipoInstrumento != "0")
        		filters.push({ field: "IdTipoInstrumento", operator: "contains", value: $scope.filtro.tipoInstrumento.IdTipoInstrumento });
        	
        	if($scope.filtro.boveda.IdBoveda !== undefined && $scope.filtro.boveda.IdBoveda != "")
				filters.push({ field: "IdBoveda", operator: "contains", value: $scope.filtro.boveda.IdBoveda});
		}catch(e){
		}

		try {
			
        	if ($scope.filtro.instrumentoObjeto !== undefined){       		
        		if ($scope.filtro.instrumentoObjeto.IdInstrumento > 0 && ($scope.filtro.instrumentoObjeto.ConteoNemotecnico === undefined || $scope.filtro.instrumentoObjeto.ConteoNemotecnico === 1) )
					filters.push({ field: "IdInstrumento", operator: "eq", value: Number($scope.filtro.instrumentoObjeto.IdInstrumento) });
					
				if ($scope.filtro.instrumentoObjeto.Nemotecnico != "" && $scope.filtro.instrumentoObjeto.ConteoNemotecnico > 1)
					filters.push({ field: "Nemotecnico", operator: "eq", value: $scope.filtro.instrumentoObjeto.Nemotecnico });
        	}
		}catch(e){
		}
		
    	var auxDate1;
    	if ($scope.filtro.fecInicio !== undefined && $scope.filtro.fecInicio !== "" && $scope.filtro.fecInicio !== null){
    		auxDate1 =  util.dateOdata(util.date($scope.filtro.fecInicio).hours(0).minutes(0).seconds(0).toDate());
			filters.push({ field: "FechaCuadraturaCustodia", operator: "eq", value: auxDate1 });
    	}
    	
		dsGrillaCuadraturaCustodia.filters(filters);
	};
	
/*************************************************************************
 * LIMPIAR FILTROS
 *************************************************************************/
	
	$scope.limpiarFiltros = function() {
		$scope.filtro.clasificacionTipoInstrumento = "";
		$scope.filtro.tipoInstrumento = "";				
		$scope.filtro.buscarinstrumento = "-1";
		$scope.filtro.boveda = "";
		$scope.filtro.fecInicio  = util.dateInicioDia($scope.SesionFechaSistema);
	}
   	

	var datos = {};
	datos.p_IdEmpresa = $scope.$root.SesionIdEmpresa; //se obtiene de variables de sistema
	datos.p_IdUsuario = $scope.$root.SesionIdUsuario; //se obtiene de variables de sistema
	datos.p_IdModulo = 'CUS';
	datos.p_UsaReglasNegocio = 'S';
    
	$scope.filtro.fecInicio = util.dateInicioDia($scope.SesionFechaSistema);

	
	
	/************************************************
	* CARGA CUADRATURA DE CUSTODIA
	************************************************/
	$scope.CargarCuadraturaCustodia = function() {
		
	var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_CARGAR_CUADRATURA_CUSTODIA "));		
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.SI) {
			    var datos = {};
			   
			    datos.p_IdEmpresa				=	$scope.$root.SesionIdEmpresa || 0	;
			    datos.p_IdUsuario				=	$scope.SesionIdUsuario || 0	;
			    datos.p_IdModulo				=	'CUS'	;
			    datos.p_AgrupaPorBoveda			= 	'S';
				datos.p_FechaConsulta	 		= 	util.dateSPdate(util.dateInicioDia($scope.filtro.fecInicio));

				bcsComunes.ventanaOverlay($scope.idVentana,true);
				
				if($scope.SesionLocale === 'PE'){
					bcsServiciosCuadraturaCustodia.GenerarMovimientosCuadraturaCustodiaBVL
					(datos,
					function(respuesta){	  		
						bcsComunes.ventanaMsg({
							conflictos:util.parseServerResponse(respuesta)
						},$scope.idVentana);
						bcsComunes.ventanaOverlay($scope.idVentana,false);
					},
					function(respuesta){
						bcsComunes.ventanaMsg({
							conflictos:util.parseServerResponse(respuesta)
						},$scope.idVentana);
						bcsComunes.ventanaOverlay($scope.idVentana,false);
					});
				}else{
					bcsServiciosCuadraturaCustodia.GenerarMovimientosCuadraturaCustodia
					(datos,
					function(respuesta){	  		
						bcsComunes.ventanaMsg({
							conflictos:util.parseServerResponse(respuesta)
						},$scope.idVentana);
						bcsComunes.ventanaOverlay($scope.idVentana,false);
					},
					function(respuesta){
						bcsComunes.ventanaMsg({
							conflictos:util.parseServerResponse(respuesta)
						},$scope.idVentana);
						bcsComunes.ventanaOverlay($scope.idVentana,false);
					});
				}
			    
			}
			else 
				bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
		});
		return;  
	};	
	

	/************************************************
	* CIERRE CUADRATURA DE CUSTODIA
	************************************************/
	$scope.CierreCuadraturaCustodia = function() {
		
	var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_CERRAR_CUADRATURA_CUSTODIA "));		
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.SI) {
				if($scope.SesionLocale === 'PE'){
					var datos = {
							p_IdEmpresa : $scope.SesionIdEmpresa,
							p_IdUsuario : $scope.SesionIdUsuario,
							p_IdModulo  : 'CUS',
							p_TipoPrograma : [{
								IdInterno : 0,
								IdTipoPrograma : 'GENERAR_REPORTE_CUSTODIA_CUADRATURA',
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
								NombreParametro : 'IdBoveda',
								Valor : ''
							},{
								IdInterno : 0,
								NombreParametro : 'FechaCuadratura',
								Valor : util.dateSPdate(util.dateInicioDia($scope.filtro.fecInicio))
							}]
					}
					bcsServicioPlataforma.Servicios().InsertarProcesoBatch(datos).$promise.then(
					function(result) {
						bcsComunes.ventanaOverlay(idVentana,false);
						if(result.P_OUT_ERRORESCONTADOR === 0){
							var fechaLocal=util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion,"FechaHora","CALL");
							bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO').replace("::IdPrograma",result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion",util.formatoSecondate(fechaLocal)),
								conflictos : util.parseServerResponse(result)}, $scope.winID);
						}else{
							bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
						}
						

					},
					function(result){
						bcsComunes.ventanaOverlay(idVentana,false);
						bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
					});
				}else{
					var datos = {};
					   
				    datos.p_IdEmpresa				=	$scope.$root.SesionIdEmpresa || 0	;
				    datos.p_IdUsuario				=	$scope.SesionIdUsuario || 0	;
				    datos.p_IdModulo				=	'CUS'	;
					datos.p_Fecha			 		= 	util.dateSPdate(util.dateInicioDia($scope.filtro.fecInicio));

					bcsComunes.ventanaOverlay($scope.idVentana,true);
			
				    bcsServiciosCuadraturaCustodia.CerrarCuadraturaCustodia
								(datos,
								function(respuesta){	  		
									bcsComunes.ventanaMsg({
										conflictos:util.parseServerResponse(respuesta)
									},$scope.idVentana);
									bcsComunes.ventanaOverlay($scope.idVentana,false);
								},
								function(respuesta){
									bcsComunes.ventanaMsg({
										conflictos:util.parseServerResponse(respuesta)
									},$scope.idVentana);
									bcsComunes.ventanaOverlay($scope.idVentana,false);
								});
				}
			    
			}
			else 
				bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
		});
		return;  
	};	
	
		
/*************************************************************************
* CARGA ARCHIVO L02
**************************************************************************/
  $scope.cargarArchivoL002 = function(){
	  	var codArch = 'ARCHIVO_L02';
		var titulo  = 'Cuadratura de Custodia - Archivo L02';
		
		var datosArchivo = {codigoArchivo: codArch, titulo: titulo};
		var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,true,false,$scope.idVentana,false);

		var ArchivoL02=bcsComunes.ventanaPromesa(id1);
		ArchivoL02.then(function(data){
			bcsComunes.ventanaOverlay(idVentana,true);
			
			//el objeto data, contiene la informacion del archivo en formato JSON.
			console.log(data);
			if(!data || !data.fileData){
				bcsComunes.ventanaOverlay(idVentana,false);
				return;
			}
			
			for(let i =0; i< data.fileData.length; i++){
				data.fileData[i].NEMO = data.fileData[i].NEMO.trim(); 
			}

			
			var parametrosSP = {};
			parametrosSP.p_IdEmpresa		= $scope.$root.SesionIdEmpresa;
			parametrosSP.p_IdUsuario 		= $scope.SesionIdUsuario; //saca de variables de entorno
	        parametrosSP.p_IdModulo			= 'CUS';
	        parametrosSP.p_Fecha			= util.dateSPdate($scope.filtro.fecInicio);/*util.dateOdata(util.date($scope.filtro.fecInicio).hours(0).minutes(0).seconds(0).toDate());*/
	        parametrosSP.p_DataArchivo		= data.fileData;
	        parametrosSP.p_DataArchivoEmisores = [];
	        
	        if(data.fileData.length){
	        	// llamado a SP para que trabaje la información del archivo.
	        	obtenerDatosDesdeArchivoL02SP(parametrosSP).then(function(result) {
	        		if (result.P_OUT_ADVERTENCIASCONTADOR){
	        			
	                    var i = 0;
            			var errordetalle ="";
           
			            for(i; i<result.P_OUT_ERRORESCONTADOR; i++){
			                console.log("warning: " + result.P_OUT_ERRORESDETALLE[i].Descripcion);
			                errordetalle = errordetalle  + "("+ (i + 1) +") " + result.P_OUT_ERRORESDETALLE[i].Descripcion;
			            }
			            
			            
	        			bcsComunes.msg("warning","Advertencia","La operacion tiene advertencias");
	        			bcsComunes.ventanaMsgErr('Operación con Advertencias. Detalle: ' + errordetalle, $scope.winID);
	        		}
	        		else if (result.P_OUT_ERRORESCONTADOR){
	        			//bcsComunes.msg("error","Error",result.P_OUT_ERRORESDETALLE);
	                    var i = 0;
            			var errordetalle ="";
           
			            for(i; i<result.P_OUT_ERRORESCONTADOR; i++){
			                console.log("Error: " + result.P_OUT_ERRORESDETALLE[i].Descripcion);
			                errordetalle = errordetalle  + "("+ (i + 1) +") " + result.P_OUT_ERRORESDETALLE[i].Descripcion;
			            }
			            
			            bcsComunes.ventanaMsgErr('Operación no realizada. Detalle: ' + errordetalle, $scope.winID);
	        		}
	        		else{
	        			// se limpian las grillas
	        			bcsComunes.ventanaMsg({ mensaje : 'Archivo Cargado Exitosamente'}, $scope.winID);
	        		}
	        		bcsComunes.ventanaOverlay(idVentana,false);
	        	},
	        	function(result){
	        		bcsComunes.ventanaMsgErr('Error en la operacion', $scope.winID);
	        		bcsComunes.ventanaOverlay(idVentana,false);
	        	});
	        }
	        else bcsComunes.ventanaOverlay(idVentana,false); 
		}); 
	return;
	};
	
	
	var obtenerDatosDesdeArchivoL02SP = function(p_datos){
		var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1741";
		var r			= $resource("", {}, { 'urlConsulta': { method: "POST", url: urlConsulta} });
		var post		= r.urlConsulta(p_datos);
		
		return post.$promise;
    };
		

/*************************************************************************
* CARGA ARCHIVO L026
**************************************************************************/
  $scope.cargarArchivoL026 = function(){
	  	var codArch = 'ARCHIVO_L026';
		var titulo  = 'Cuadratura de Custodia - Archivo L026';
		
		var datosArchivo = {codigoArchivo: codArch, titulo: titulo};
		var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,true,false,$scope.idVentana,false);

		var ArchivoL026=bcsComunes.ventanaPromesa(id1);
		ArchivoL026.then(function(data){
			bcsComunes.ventanaOverlay(idVentana,true);
			
			//el objeto data, contiene la informacion del archivo en formato JSON.
			console.log(data);
			if(!data || !data.fileData){
				bcsComunes.ventanaOverlay(idVentana,false);
				return;
			}
			
			var parametrosSP = {};
			parametrosSP.p_IdEmpresa		= $scope.$root.SesionIdEmpresa;
			parametrosSP.p_IdUsuario 		= $scope.SesionIdUsuario; //saca de variables de entorno
	        parametrosSP.p_IdModulo			= 'CUS';
	        parametrosSP.p_Fecha			= util.dateOdata(util.date($scope.filtro.fecInicio).hours(0).minutes(0).seconds(0).toDate());
	        parametrosSP.p_DataArchivo		= data.fileData;
	        
	        if(data.fileData.length){
	        	// llamado a SP para que trabaje la información del archivo.
	        	obtenerDatosDesdeArchivoL026SP(parametrosSP).then(function(result) {
	        		if (result.P_OUT_ADVERTENCIASCONTADOR){
	        			
	                    var i = 0;
            			var errordetalle ="";
           
			            for(i; i<result.P_OUT_ERRORESCONTADOR; i++){
			                console.log("warning: " + result.P_OUT_ERRORESDETALLE[i].Descripcion);
			                errordetalle = errordetalle  + "("+ (i + 1) +") " + result.P_OUT_ERRORESDETALLE[i].Descripcion;
			            }
			            
			            
	        			bcsComunes.msg("warning","Advertencia","La operacion tiene advertencias");
	        			bcsComunes.ventanaMsgErr('Operación con Advertencias. Detalle: ' + errordetalle, $scope.winID);
	        		}
	        		else if (result.P_OUT_ERRORESCONTADOR){
	        			//bcsComunes.msg("error","Error",result.P_OUT_ERRORESDETALLE);
	                    var i = 0;
            			var errordetalle ="";
           
			            for(i; i<result.P_OUT_ERRORESCONTADOR; i++){
			                console.log("Error: " + result.P_OUT_ERRORESDETALLE[i].Descripcion);
			                errordetalle = errordetalle  + "("+ (i + 1) +") " + result.P_OUT_ERRORESDETALLE[i].Descripcion;
			            }
			            
			            bcsComunes.ventanaMsgErr('Operación no realizada. Detalle: ' + errordetalle, $scope.winID);
	        		}
	        		else{
	        			// se limpian las grillas
						bcsComunes.ventanaMsg({ mensaje : 'Archivo Cargado Exitosamente'}, $scope.winID);
	        		}
	        		bcsComunes.ventanaOverlay(idVentana,false);
	        	},
	        	function(result){
	        		bcsComunes.ventanaMsgErr('Error en la operacion', $scope.winID);
	        		bcsComunes.ventanaOverlay(idVentana,false);
	        	});
	        }
	        else bcsComunes.ventanaOverlay(idVentana,false); 
		}); 
	return;
	};
	
	
	var obtenerDatosDesdeArchivoL026SP = function(p_datos){
		var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1747";
		var r			= $resource("", {}, { 'urlConsulta': { method: "POST", url: urlConsulta} });
		var post		= r.urlConsulta(p_datos);
		
		return post.$promise;
    };
	
	/**************************************************
	 * FIN CARGA ARCHIVO L026 DCV
	 *************************************************/
	   
	
	/*************************************************************************
	* CARGA ARCHIVO OTROS SALDOS
	**************************************************************************/
  $scope.cargarArchivoOtrosSaldos = function(){
	  	var codArch = 'ARCHIVO_OTROS_SALDOS';
		var titulo  = 'Cuadratura de Custodia - Archivo Otros Saldos';
		
		var datosArchivo = {codigoArchivo: codArch, titulo: titulo};
		var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,true,false,$scope.idVentana,false);

		var ArchivoOtrosSaldos=bcsComunes.ventanaPromesa(id1);
		ArchivoOtrosSaldos.then(function(data){
			bcsComunes.ventanaOverlay(idVentana,true);
			
			//el objeto data, contiene la informacion del archivo en formato JSON.
			console.log(data);
			if(!data || !data.fileData){
				bcsComunes.ventanaOverlay(idVentana,false);
				return;
			}
			
			var parametrosSP = {};
			parametrosSP.p_IdEmpresa		= $scope.$root.SesionIdEmpresa;
			parametrosSP.p_IdUsuario 		= $scope.SesionIdUsuario; //saca de variables de entorno
	        parametrosSP.p_IdModulo			= 'CUS';
	        parametrosSP.p_Fecha			= util.dateOdata(util.date($scope.filtro.fecInicio).hours(0).minutes(0).seconds(0).toDate());
	        parametrosSP.p_DataArchivo		= data.fileData;
	        
	        if(data.fileData.length){
	        	// llamado a SP para que trabaje la información del archivo.
	        	obtenerDatosDesdeArchivoOtrosSaldos(parametrosSP).then(function(result) {
	        		if (result.P_OUT_ADVERTENCIASCONTADOR){
	        			
	                    var i = 0;
            			var errordetalle ="";
           
			            for(i; i<result.P_OUT_ERRORESCONTADOR; i++){
			                console.log("warning: " + result.P_OUT_ERRORESDETALLE[i].Descripcion);
			                errordetalle = errordetalle  + "("+ (i + 1) +") " + result.P_OUT_ERRORESDETALLE[i].Descripcion;
			            }
			            
			            
	        			bcsComunes.msg("warning","Advertencia","La operacion tiene advertencias");
	        			bcsComunes.ventanaMsgErr('Operación con Advertencias. Detalle: ' + errordetalle, $scope.winID);
	        		}
	        		else if (result.P_OUT_ERRORESCONTADOR){
	        			//bcsComunes.msg("error","Error",result.P_OUT_ERRORESDETALLE);
	                    var i = 0;
            			var errordetalle ="";
           
			            for(i; i<result.P_OUT_ERRORESCONTADOR; i++){
			                console.log("Error: " + result.P_OUT_ERRORESDETALLE[i].Descripcion);
			                errordetalle = errordetalle  + "("+ (i + 1) +") " + result.P_OUT_ERRORESDETALLE[i].Descripcion;
			            }
			            
			            bcsComunes.ventanaMsg({conflictos:util.parseServerResponse(result)},$scope.IdVentana);
	        		}
	        		else{
	        			// se limpian las grillas
	        			bcsComunes.ventanaMsg({ mensaje : 'Archivo Cargado Exitosamente'}, $scope.winID);
	        		}
	        		bcsComunes.ventanaOverlay(idVentana,false);
	        	},
	        	function(result){
	        		bcsComunes.ventanaMsgErr('Error en la operacion', $scope.winID);
	        		bcsComunes.ventanaOverlay(idVentana,false);
	        	});
	        }
	        else bcsComunes.ventanaOverlay(idVentana,false); 
		}); 
	return;
	};
	
	
	var obtenerDatosDesdeArchivoOtrosSaldos = function(p_datos){
		var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1751";					// PONER EL NUMERO CORREsTO JOAJOAJOA
		var r			= $resource("", {}, { 'urlConsulta': { method: "POST", url: urlConsulta} });
		var post		= r.urlConsulta(p_datos);
		
		return post.$promise;
    };
	/**************************************
	* FIN CARGA ARCHIVO OTROS SALDOS
	***************************************/	
 
    /*************************************************************************
    * CARGA ARCHIVO BOVEDAS
    **************************************************************************/
      $scope.cargarArchivoBovedas = function(){
    	  	var codArch = 'ARCHIVO_BOVEDAS'//'ARCHIVO_OTROS_SALDOS_GARANTIA';
    		var titulo  = 'Cuadratura de Custodia - Bovedas';
    		
    		var datosArchivo = {codigoArchivo: codArch, titulo: titulo};
    		var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,true,false,$scope.idVentana,false);

    		var Bovedas = bcsComunes.ventanaPromesa(id1);
    		Bovedas.then(function(data){
    			bcsComunes.ventanaOverlay(idVentana,true);
    			
    			//el objeto data, contiene la informacion del archivo en formato JSON.
    			console.log(data);
    			if(!data || !data.fileData){
    				bcsComunes.ventanaOverlay(idVentana,false);
    				return;
    			}
    			
    			var parametrosSP = {};
    			parametrosSP.p_IdEmpresa		= $scope.$root.SesionIdEmpresa;
    			parametrosSP.p_IdUsuario 		= $scope.SesionIdUsuario; //saca de variables de entorno
    	        parametrosSP.p_IdModulo			= 'CUS';
    	        parametrosSP.p_Fecha			= util.dateSPdate($scope.filtro.fecInicio);
    	        parametrosSP.p_DataArchivo		= [];
    	        parametrosSP.p_DataArchivoEmisores = data.fileData;
    	        if(data.fileData.length){
    	        	// llamado a SP para que trabaje la información del archivo.
    	        	obtenerDatosDesdeArchivoL02SP(parametrosSP).then(function(result) {
    	        		if (result.P_OUT_ERRORESCONTADOR){
    	        			//bcsComunes.msg("error","Error",result.P_OUT_ERRORESDETALLE);
    	                    var i = 0;
                			var errordetalle ="";
               
    			            for(i; i<result.P_OUT_ERRORESCONTADOR; i++){
    			                console.log("Error: " + result.P_OUT_ERRORESDETALLE[i].Descripcion);
    			                errordetalle = errordetalle  + "("+ (i + 1) +") " + result.P_OUT_ERRORESDETALLE[i].Descripcion;
    			            }
    			            
    			            bcsComunes.ventanaMsgErr('Operación no realizada. Detalle: ' + errordetalle, $scope.winID);
    	        		}
    	        		else{
    	        			// se limpian las grillas
    	        			bcsComunes.ventanaMsg({ mensaje : 'Archivo Cargado Exitosamente'}, $scope.winID);
    	        		}
    	        		bcsComunes.ventanaOverlay(idVentana,false);
    	        	},
    	        	function(result){
    	        		bcsComunes.ventanaMsgErr('Error en la operacion', $scope.winID);
    	        		bcsComunes.ventanaOverlay(idVentana,false);
    	        	});
    	        }
    	        else bcsComunes.ventanaOverlay(idVentana,false); 
    		}); 
    	return;
    	};
        /*************************************************************************
         * CARGA ARCHIVO OTROS SALDOS GTA
         **************************************************************************/
    
    
/*************************************************************************
* CARGA ARCHIVO CUSTODIA PERU 
**************************************************************************/
    
    $scope.cargarArchivoCustodia = function(id) {
		var datosArchivo = {codigoArchivo: id, titulo: id};
		bcsComunes.ventanaAbrir("CargaArchivoCuadraturaCustodia",datosArchivo,false,false,idVentana);
	}
 /*************************************************************************
 * GUARDAR CANMBIOS EXPLICACION DESCUADRATURA 
 **************************************************************************/
    $scope.GuardarCambios = function(){
    	var parametro = {};
    	parametro.p_IdEmpresa = $scope.SesionIdEmpresa;
    	parametro.p_ExplicacionDescuadratura = [];
    	$.each($scope.confBasicaCuadraturaCustodia.dataSource.ds._view,function(key,value) {
    		 parametro.p_ExplicacionDescuadratura.push({
    			 IdCuadraturaCustodia : value.IdCuadraturaCustodia,
    			 ExplicacionDescuadratura : value.ExplicacionDescuadratura
    		 })		
    	})
    	bcsComunes.log().info(JSON.stringify(parametro));
	     
	     bcsServiciosCuadraturaCustodia.GuardarCuadraturaCustodia(parametro).$promise.then(function(respuesta){
	     bcsComunes.ventanaOverlay(idVentana, false);
	      
	       if (respuesta.P_OUT_ERRORESCONTADOR === 0){ 
	        bcsComunes.ventanaMsg({
	        mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_EXITO"),
	        conflictos:util.parseServerResponse(respuesta)
	       },idVentana);
	         
	  
	        } else {
	         bcsComunes.ventanaOverlay(idVentana, false);
	         bcsComunes.ventanaMsg({
	          mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
	          conflictos:util.parseServerResponse(respuesta)
	         },idVentana);
	        }
	
	       });
    }
/*************************************************************************
* FIN CARGA ARCHIVO CUSTODIA PERU 
**************************************************************************/

//   	var Traspasosuccess = function(result){
//		if (result.p_out_IngresosRealizados !== 0){
//			bcsComunes.msg('info',$translate.instant("UI_INFORMACION"), "Se generaron: " + result.p_out_IngresosRealizados  +  " movimientos en tránsito.");
//			$scope.buscarMovimientos();
//			
//		}else{
//			
//			console.log("Error." + result.P_OUT_ERRORESCONTADOR);
//			
//            var i = 0;
//            var errordetalle ="";
//           
//            for(i; i<result.P_OUT_ERRORESCONTADOR; i++){
//                console.log("Error: " + result.P_OUT_ERRORESDETALLE[i].Descripcion);
//                errordetalle = errordetalle  + "("+ (i + 1) +") " + result.P_OUT_ERRORESDETALLE[i].Descripcion;
//            }
//            
//            bcsComunes.msg('error',$translate.instant("UI_ERROR"), "Operación no realizada. Detalle: " + errordetalle);
//	    }
//	};
//************************************************	

	
    //***************************************************************
    // Fin Certificacion del movimiento en transito
    //***************************************************************
   	
	
});