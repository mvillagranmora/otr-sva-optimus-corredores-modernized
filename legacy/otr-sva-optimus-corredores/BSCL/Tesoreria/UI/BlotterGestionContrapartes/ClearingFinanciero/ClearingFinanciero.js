angular.module('ModuloTesoreria').controller('ClearingFinancieroCtrl',
		function($rootScope, $scope,bcsComunes,$translate,bcsServiciosTesoreria,bcsBlotterGestionContrapartesGrid,bcsAlerta,OPCIONES_BCSALERTA)
{
	 
	$scope.winID = bcsComunes.ventanaWinId($scope);	
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	$scope.data = {};
	$scope.datosForm = {};
	
	
	//SETEO INICIAL
	$scope.btnActivo = true;
	$scope.kngFechaLiquidacion = util.dateInicioDia($scope.SesionFechaSistema);	

	
	//---------------------------------------------------
	// OBTIENE DATOS DE LA EMPRESA
	//---------------------------------------------------	
	if ($scope.SesionIdEmpresa)
	{
		
			var dsEmpresa = new bcs.DataSource({
				type: "odata",
				
				definition 	: {			
					serverFiltering 	: true, // Filtrado de datos en el servidor.
					transport 			: {
											read : {
												 url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1234",
												 dataType : "json",
											  }
										  },
					
					filter: { field: "IdEmpresa", operator: "eq", value: Number($scope.SesionIdEmpresa) },
										  
					schema 				: {
							model: {
								fields : {
									IdEmpresa          : {type: "number"},
									DocumentoIdentidad : {type: "string"}
								}
							}
						}
										  
				},
			});
			
	        dsEmpresa.fetch().then(function(){
				 if (dsEmpresa.data().length <= 0 ) 
				 {
					 $scope.Empresa = $scope.SesionEmpresa;
					 console.log("Empresa no existe!");
					 return false;
				 }
				 else 
				 {
					 $scope.$apply(function() {						
						$scope.Empresa = dsEmpresa.data()[0].DocumentoIdentidad +' - '+ $scope.SesionEmpresa;
						return true;
					 });
				 }
		    });
	};
	
	

		
	if ($scope.SesionLocale === 'CL')
	{
		$scope.IdCamaraDeCompensacion = 'CCLV';
	}
	else if ($scope.SesionLocale === 'PE')
	{
		$scope.IdCamaraDeCompensacion = 'CAVALI';
	}
	
	 
	
	//CONFIGURACION DE COMBOBOX
	$scope.oConfiguracionGrupoCamara={
			tipo 			: "combobox-grupo-camara",
			configuracion 	: {},
			filtros 		: {	
								 IdCamaraDeCompensacion     : { "field"   : "IdCamaraDeCompensacion",    "operator": "eq", "value"   : $scope.IdCamaraDeCompensacion},								
								 IdPaisCamaraDeCompensacion : { "field"   : "IdPaisCamaraDeCompensacion","operator": "eq", "value"   : $scope.SesionIdPaisSistema},
								 IdEstadoGrupoCamara        : { "field"   : "IdEstadoGrupoCamara",       "operator": "eq", "value"   : "ACT"},								
								 SecuenciaAgrupacion        : { "field"   : "SecuenciaAgrupacion",       "operator": "eq", "value"   : "" },  //Este filtro se actualiza después de optener el parámetro "CambiosCCLV"
							  },
			idActual 		: false,
			seleccionado 	: false,
			idDefecto 		: ''			
	};	
	
	 
	
	
	//BUSCA PARAMETROS DE CONFIGURACION ("CambiosCCLV": indica agrupación de camaras del CCLV)(solo para Chile)
	if ($scope.SesionIdEmpresa && $scope.SesionLocale === 'CL')
	{			
			var usaCamaraHSDs = new kendo.data.DataSource({
				type: "odata",
        		serverFiltering: true,
        		transport: {
            	read: {
                		url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1037",
                		dataType: "json"
            	}
        		}, 
        	   filter: [{ field: "IdEmpresa", operator: "eq", value: 1},
             			{ field: "CodigoParametro", operator: "eq", value: "UsaCamaraHS" }]
			});
		
			//Consulta valor de parámetro
			var configuracionDatasource = new kendo.data.DataSource({
				type: "odata",
				serverFiltering:true,
	    		transport: {
	    		read: {
	    				url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1037",
	    				dataType: "json"
			    }
				},
				filter: [{ field: "IdEmpresa", operator: "eq", value: 1 },
				         { field: "CodigoParametro", operator: "eq", value: 'CAMBIOSCCLV' }],
			});
			
			usaCamaraHSDs.fetch().then(function () {
				var valor = 'N';
        		if (usaCamaraHSDs.data().length === 1) {
            		   valor = usaCamaraHSDs.data()[0].Valor || 'N';
        			}
        			$scope.$apply(function () {
            			$scope.UsaCamaraHS = valor;  // Guardamos S/N en scope
            			if ($scope.UsaCamaraHS === 'S'){
							$scope.oConfiguracionGrupoCamara.filtros.VisibleConCamaraHS = {field : "VisibleConCamaraHS", operator : "eq", value : $scope.UsaCamaraHS};
						}
        				});
				});
			
	 
			configuracionDatasource.fetch().then(function(){
				 if (configuracionDatasource.data().length != 1 ) 
				 {   //Si no existe parámetro, deja opción 'N' por defecto 
					 $scope.oConfiguracionGrupoCamara.filtros.SecuenciaAgrupacion.value = 'GRUPO2';
					 return false;
				 }
				 else 
				 {   //Parámetro CambiosCCLV = S/N
					 $scope.$apply(function() {
						 
						 if (configuracionDatasource.data()[0].Valor === 'S')
							 $scope.oConfiguracionGrupoCamara.filtros.SecuenciaAgrupacion.value = 'GRUPO1';
						 else // N
							 $scope.oConfiguracionGrupoCamara.filtros.SecuenciaAgrupacion.value = 'GRUPO2'							 
						 return true;
					 });
				 }
		    });		
	};
	
	
	
	//CONFIGURACION DE GRILLA PRINCIPAL
	$scope.confBasicaClearingFinanciero = {
		dataSource			: bcsBlotterGestionContrapartesGrid.dsClearingFinanciero,
		columns				: bcsBlotterGestionContrapartesGrid.columnasClearingFinanciero(),		
		sortable			: true,
		pageable			: true,
		autoBind			: false,
		filterable			: true,
		height				: 460,
		selectable 			: "row",
		userSettings		: true,
		allowCopy			: { delimeter: "\t" },
		excel 				: { allPages: true },
		rowColorBackground	: function(row) { 
			var color = "";
			 
			if (row.IdComprobanteTesoreria === 0)
				color = "yellow";
			else
				color = "green";
			
			return color;
		},		
	};
	

	
	//CONFIGURACION SUB-GRILLA TRANSACCIONES
	$scope.confBasicaClearingFinancieroDetalle = function(item) {		
		var parametros = {};
		parametros = $scope.parametrosClearing('buscar');		

		var dsDetalleConf = bcsBlotterGestionContrapartesGrid.dsClearingFinancieroDetalleConf();
		dsDetalleConf.definition.callParameters = parametros;

		dsDetalleConf.definition.filter = 
			[
			 	{ field: "IdBolsa", 				 		operator: "eq", value: item.IdBolsa },
			 	{ field: "NombreGrupoCamara",  				operator: "eq", value: item.NombreGrupoCamara },
			 	{ field: "IdClasificacionTipoInstrumento", 	operator: "eq", value: item.IdClasificacionTipoInstrumento },			 	
			 	{ field: "RutContraparte", 			 		operator: "eq", value: item.RutContraparte },
			 	{ field: "IdComprobanteTesoreria",   		operator: "eq", value: item.IdComprobanteTesoreria }
			];
		
	    
		return {
			dataSource			: new bcs.DataSource(dsDetalleConf),
			columns				: bcsBlotterGestionContrapartesGrid.columnasClearingFinancieroDetalle(),
			sortable			: false,
			pageable			: false,
			filterable			: false,
			autoBind			: true,
			height				: 150,
			selectable 			: "multiple row"
		}
	};	
	

	
	//CONFIGURACION SUB-GRILLA DIVIDENDOS (Solo para CL, Perú no considera Dividendos)
	$scope.confBasicaClearingFinancieroDividendos = function(item) 
	{
		var parametros = {};
		parametros = $scope.parametrosClearing('buscar');		

		var dsDividendosConf = bcsBlotterGestionContrapartesGrid.dsClearingFinancieroDividendosConf();
		dsDividendosConf.definition.callParameters = parametros;

		dsDividendosConf.definition.filter = 
			[
			 	{ field: "IdBolsa", 				 		operator: "eq", value: item.IdBolsa },
			 	{ field: "NombreGrupoCamara",  		        operator: "eq", value: item.NombreGrupoCamara },
			 	{ field: "IdClasificacionTipoInstrumento", 	operator: "eq", value: item.IdClasificacionTipoInstrumento },			 	
			 	{ field: "RutContraparte", 			 		operator: "eq", value: item.RutContraparte },
			 	{ field: "IdComprobanteTesoreria",   		operator: "eq", value: item.IdComprobanteTesoreria }
			];

		
		return {
			dataSource			: new bcs.DataSource(dsDividendosConf),
			columns				: bcsBlotterGestionContrapartesGrid.columnasClearingFinancieroDividendos(),
			sortable			: false,
			pageable			: false,
			filterable			: false,
			autoBind			: true,
			height				: 150,
			selectable 			: "multiple row"
		}
	};	

	//CONFIGURACION SUB-GRILLA SIMULTANEAS (Solo para CL, Perú no considera Dividendos)
	$scope.confBasicaClearingFinancieroSimultaneas = function(item) 
	{
		var parametros = {};
		parametros = $scope.parametrosClearing('buscar');		

		var dsSimultaneasConf = bcsBlotterGestionContrapartesGrid.dsClearingFinancieroSimultaneasConf();
		dsSimultaneasConf.definition.callParameters = parametros;

		dsSimultaneasConf.definition.filter = 
			[
			 	{ field: "IdBolsa", 				 		operator: "eq", value: item.IdBolsa },
			 	{ field: "NombreGrupoCamara",  		        operator: "eq", value: item.NombreGrupoCamara },
			 	{ field: "IdClasificacionTipoInstrumento", 	operator: "eq", value: item.IdClasificacionTipoInstrumento },			 	
			 	{ field: "RutContraparte", 			 		operator: "eq", value: item.RutContraparte },
			 	{ field: "IdComprobanteTesoreria",   		operator: "eq", value: item.IdComprobanteTesoreria }
			];

		
		return {
			dataSource			: new bcs.DataSource(dsSimultaneasConf),
			columns				: bcsBlotterGestionContrapartesGrid.columnasClearingFinancieroSimultaneas(),
			sortable			: false,
			pageable			: false,
			filterable			: false,
			autoBind			: true,
			height				: 150,
			selectable 			: "multiple row"
		}
	};	


	
	$scope.limpiarFiltros = function ()
	{
		$scope.oConfiguracionGrupoCamara.idActual 	=  0;
		$scope.kngFechaLiquidacion 	= util.dateInicioDia($scope.SesionFechaSistema);
		bcsBlotterGestionContrapartesGrid.dsClearingFinanciero.removeAll();
	}
	 
	//setea parámetros de consulta para la búsqueda y procesamiento.
	$scope.parametrosClearing = function(accion)
	{	
		var parametros = {};
		var camaras = '';
		var agrupacionCamaras = new Array();
		
		parametros.p_IdEmpresa 			= $scope.SesionIdEmpresa;
		parametros.p_IdUsuario         	= $scope.SesionIdUsuario;
		parametros.p_IdModulo			= 'TES';
		parametros.p_UsaReglasNegocio	= 'S';
		parametros.p_FechaLiquidacion	= util.dateSPdate($scope.kngFechaLiquidacion) || '0001-01-01';

		$scope.NombreGrupoCamara = $scope.oConfiguracionGrupoCamara.seleccionado.NombreGrupoCamara || '';

		if ($scope.SesionLocale === 'CL')
		{
			
			switch ($scope.NombreGrupoCamara)
			{	
				case 'RV':				
					agrupacionCamaras.push({ Camara: "RV" });
					break;
				case 'PM':				
					agrupacionCamaras.push({ Camara: "PM" });
					break;
				case 'HS':
					agrupacionCamaras.push({Camara: "HS"});
					agrupacionCamaras.push({Camara: "PM"});
					agrupacionCamaras.push({Camara: "PH"});				
					agrupacionCamaras.push({Camara: "SM"});
					break;				
				case 'PH + SM':
					agrupacionCamaras.push({ Camara: "PH" });				
					agrupacionCamaras.push({ Camara: "SM" });				
					break;			
				case 'RV + PM':
					agrupacionCamaras.push({ Camara: "RV" });
					agrupacionCamaras.push({ Camara: "PM" });				
					break;
				default:  //Todos
					agrupacionCamaras.push({ Camara: "RV" });
					agrupacionCamaras.push({ Camara: "PM" });
					agrupacionCamaras.push({ Camara: "PH" });
					agrupacionCamaras.push({ Camara: "SM" });
					agrupacionCamaras.push({ Camara: "DV" });
					agrupacionCamaras.push({ Camara: "HS" });		
			}
		} // if ($scope.SesionLocale === 'CL')
		
		
		if ($scope.SesionLocale === 'PE')
		{
			switch ($scope.NombreGrupoCamara)
			{	
				case 'RV':				
					agrupacionCamaras.push({ Camara: "RV" });
					break;
				case 'RF':				
					agrupacionCamaras.push({ Camara: "RF" });
					break;				
				case 'RP': // Reportes
					agrupacionCamaras.push({ Camara: "RP" });
					break;			
				case 'RM': 
					agrupacionCamaras.push({ Camara: "RM" });									
					break;
				default:  //Todos
					agrupacionCamaras.push({ Camara: "RV" });
					agrupacionCamaras.push({ Camara: "RF" });
					agrupacionCamaras.push({ Camara: "RP" });
					agrupacionCamaras.push({ Camara: "RM" });			
			}
		} // if ($scope.SesionLocale === 'PE')		
		
		parametros.p_Camaras   = agrupacionCamaras;
		
		if (accion === 'procesar')
		{
			parametros.p_IdPais    = $scope.SesionIdPaisSistema;
		}
		return parametros;
	}
	
 
	
	
	$scope.buscar = function()
	{
		var parametros = {};
		parametros = $scope.parametrosClearing('buscar');
		console.log("PARAMETROS:" + JSON.stringify(parametros));
		bcsBlotterGestionContrapartesGrid.dsClearingFinanciero.setCallParameters(parametros);	
	}

	

	//---------------------------------------------------------------------
	// PROCESAR CLEARING (crea comprobante de tesoreria por grupo camara)
	//---------------------------------------------------------------------
	var success = function(respuesta)
	{	
		if (respuesta.P_OUT_ERRORESCONTADOR !== undefined && respuesta.P_OUT_ERRORESCONTADOR == 0)
		{	
			bcsComunes.ventanaOverlay($scope.winID, false);
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_EXITO"),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
			
			$scope.buscar();
		}
		else
		{
			bcsComunes.ventanaOverlay($scope.winID, false);
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
		}
	};
	
	var error = function(respuesta)
	{	
		bcsComunes.ventanaOverlay($scope.winID, false);
		bcsComunes.ventanaMsg({
			mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
			conflictos:util.parseServerResponse(respuesta)
		},$scope.winID);
	};
	

	$scope.procesarClearing = function()
    {	
		var parametros = {};		
		var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_PROCESARAN_LAS_TRANSACCIONES_PARA_CLEARING') );
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR){				
				
				bcsComunes.ventanaOverlay($scope.winID, true);		
				parametros = $scope.parametrosClearing('procesar');
				console.log("PARAMETROS:" + JSON.stringify(parametros));
				bcsServiciosTesoreria.ProcesarClearing(parametros, success, error);				

			}
		});
    };
  
    //Abre ventana(del módulo "Orden") con el detalle de la transacción
	$scope.data.abrirDetalleTransaccion = function (row) {
		if (!row) return;
		bcsComunes.ventanaAbrir("DetalleTransaccion", {IdTransaccion : row[0].IdTransaccion} ,false,false);
	};

	
		
	//Permite actualizar la cámara de las transacciones desde el SCL(con WS)
	$scope.actualizarCamaraTransaccionDesdeSCL = function(){		
		var parametros = {};
		parametros.fecha = $scope.kngFechaLiquidacion;			
		bcsComunes.ventanaAbrir('ComplementarTransaccion',parametros,false,false);
	};
	
	$scope.datosForm.exportarExcel = function () {
    	try {
        	if ($scope.ApiClearingFinanciero && $scope.ApiClearingFinanciero.exportarExcel) {
            	$scope.ApiClearingFinanciero.exportarExcel();
        	} else if ($scope.ApiClearingFinanciero && $scope.ApiClearingFinanciero.saveAsExcel) {
            	// Algunas implementaciones de bcs-grid usan saveAsExcel
            	$scope.ApiClearingFinanciero.saveAsExcel();
        	} else {
            	bcsAlerta.mostrar($translate.instant('UI_ERROR_EXPORTAR_EXCEL'), OPCIONES_BCSALERTA.ERROR);
        	}
    	} catch (e) {
        	console.error('Error al exportar Excel:', e);
        	bcsAlerta.mostrar($translate.instant('UI_ERROR_EXPORTAR_EXCEL'), OPCIONES_BCSALERTA.ERROR);
    	}
	};

});


