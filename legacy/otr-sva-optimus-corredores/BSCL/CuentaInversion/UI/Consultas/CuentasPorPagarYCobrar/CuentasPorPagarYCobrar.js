angular.module('ModuloCuentaInversion').controller('CuentasPorPagarYCobrarCtrl',
				function($scope,$translate,$timeout,bcsComunes,$translate,
						bcsServiciosCuentaInversion,bcsServicioPlataforma)
{

	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);

	var filters = {};

	//SETEO INICIAL	
	$scope.kngFechaEfectiva 	= util.dateInicioDia($scope.SesionFechaSistema);
	$scope.ngCuentaPorCobrar 	= true;
	$scope.ngCuentaPorPagar 	= false;
	
	//CONFIGURACION PANTALLA
	$scope.oConfiguracionEjecutivo={
		tipo : "combobox-ejecutivo",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {},
	};
	$scope.oConfiguracionSocioNegocio={
		tipo : "combobox-socio-negocio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},
	};
	$scope.oConfiguracionPortafolio={
		tipo : "combobox-portafolio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
		},
	};


	var dsBasicCuentasPagaryCobrar = new bcs.DataSource({
		type : "call",
		definition : {
            serverFiltering	: false, // Filtrado de datos en el servidor.
            serverPaging	: false,	// Paginado de los datos en el servidor.
            serverSorting	: false,	// Ordenamiento de los datos en el servidor.
            pageSize		: 200,
            callParameters	: filters,
            callTransaction	: "1422",
            callResultSet	: "P_OUT_CUENTASPORPAGARCOBRAR",
			schema : {
				model : {
					fields : {
						FechaSistema				: {type : "sysdate"},
						FechaDeuda  				: {type : "sysdate"},
						FechaEfectivaMovimiento		: {type : "sysdate"},
						TipoCliente					: {type : "string"},
						CodigoProposito 			: {type : "string"},
						NombreCliente				: {type : "string"},
						DocumentoIdentidadCliente	: {type : "string"},
						NombreConcepto				: {type : "string"},
						CodigoMoneda				: {type : "string"},
						Monto 						: {type : "number"},
					
						MontoDeuda					: {type : "number"},
						SaldoLibroInventario		: {type : "number"},
						AntiguedadDeuda				: {type : "number"},
						IdCargoAbonoMovimiento		: {type : "number"},
						NombreEjecutivo				: {type : "string"},
						IdCliente					: {type : "number"},
						IdPortafolio				: {type : "number"}
					}
				}
			}
		}
	});
	$scope.confBasicCuentasPagaryCobrar = {
		dataSource  : dsBasicCuentasPagaryCobrar,
		columns 	: [	 {field : "FechaEfectivaMovimiento",	title:"UI_FECHA_EFECTIVA",			width: "85px"}
						,{field : "NombreEjecutivo",			title:"UI_EJECUTIVO",				width: "200px", groupable: true}							
						,{field : "NombreCliente",				title:"UI_CLIENTE",					width: "200px", groupable: true}
						,{field : "DocumentoIdentidadCliente",	title:"UI_DOCUMENTO_IDENTIDAD",		width: "90px"}
						,{field : "TipoCliente",				title:"UI_TIPO_CLIENTE",			width: "100px", groupable: true}
						,{field : "CodigoProposito",			title:"UI_PORTAFOLIO",				width: "130px", groupable: true}
						,{field : "IdCargoAbonoMovimiento",		title:"UI_SECUENCIA_MOVIMIENTO",	width: "70px"}					
						,{field : "NombreConcepto", 			title:"UI_CONCEPTO", 				width: "200px", groupable: true}
						,{field : "CodigoMoneda", 				title:"UI_MONEDA", 					width: "50px", 	groupable: true}
						,{field : "Monto", 						title:"UI_MONTO", 					width: "85px",	template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
						
					
						,{field : "MontoDeuda", 				title:"UI_MONTO_DEUDA_TOTAL", 		width: "85px",	template : " <label> #:  (util.bcs_formato(MontoDeuda,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
						//,{field : "AntiguedadDeuda",			title:"UI_ANTIGUEDAD_DEUDA",		width: "80px"}
						,{field : "FechaSistema", 				title:"UI_FECHA_MOVIMIENTO", 		width: "80px"}
						,{field : "FechaDeuda", 				title:"UI_FECHA_DEUDA", 		width: "80px"}
											
					  ],
		contextMenu : true,
		filterable	: true,
		sortable 	: true,
		pageable 	: false,
		height 		: 520,
		selectable 	: "multiple row",
		autoBind 	: false,
		userSettings: true,
		excel 		: { allPages: true }
	};

	//WATCH
	$scope.$watch('ngCuentaPorCobrar',function(newval,oldval){
		switch (newval){
			case true:  $scope.ngCuentaPorPagar = false; break;					
			case false: $scope.ngCuentaPorPagar = true;  break; 
		}			
	});	
	$scope.$watch('ngCuentaPorPagar',function(newval,oldval){
		switch (newval){
			case true:  $scope.ngCuentaPorCobrar = false; break;					
			case false: $scope.ngCuentaPorCobrar = true;  break;  
		}
	});

	//OPCIONES
	$scope.buscarMovimientos = function() {
		var filters	= {};

		filters.p_IdEmpresa 				= $scope.SesionIdEmpresa;
        filters.p_IdUsuario 				= $scope.SesionIdUsuario;
        filters.p_IdModulo 					= 'CTA';
        
        filters.p_IdEmpleado 				= $scope.oConfiguracionEjecutivo.idActual !== undefined && $scope.oConfiguracionEjecutivo.idActual !== 0 && $scope.oConfiguracionEjecutivo.seleccionado !== false && $scope.oConfiguracionEjecutivo.seleccionado !== null ? 
        										Number($scope.oConfiguracionEjecutivo.seleccionado.IdEmpleadoEjecutivo) : $scope.SesionIdEmpleado;
		filters.p_SaldoLibroInventario		= 0;
		filters.p_IdEstado					= 'CRT';
		filters.p_FechaEfectivaMovimiento	= util.dateSPdate($scope.kngFechaEfectiva);
		filters.p_IdCliente					= $scope.oConfiguracionPortafolio.idActual ? Number($scope.oConfiguracionPortafolio.seleccionado.IdCliente) : null ;
		filters.p_IdPortafolio				= $scope.oConfiguracionPortafolio.idActual ? Number($scope.oConfiguracionPortafolio.seleccionado.IdPortafolio) : null;
		filters.p_IdTipoMovimiento			= $scope.ngCuentaPorCobrar ? 'MOVCAR' : 'MOVABN';

        dsBasicCuentasPagaryCobrar.setCallParameters(filters);
	};
	
	$scope.limpiarFiltros = function()
	{	
		$scope.oConfiguracionEjecutivo.idActual = 0;
		$scope.oConfiguracionPortafolio.idActual = false;
		$scope.kngFechaEfectiva	= util.dateInicioDia($scope.SesionFechaSistema);
		$scope.ngCuentaPorCobrar = true;
		$scope.ngCuentaPorPagar = false;

		dsBasicCuentasPagaryCobrar.removeAll();
	};
	
	
	//************************************************************************************************
	// Solicita(Inscripción en JobManager) generación del archivo "Anexo 13 - Detalle Saldo de Clientes" (PERU)  
	//************************************************************************************************	       
	$scope.generarArchivoAnexo13DetalleSaldosClientes = function() {
		
		if ($scope.confBasicCuentasPagaryCobrar.dataSource.ds._total == undefined || $scope.confBasicCuentasPagaryCobrar.dataSource.ds._total < 1)
		{
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_DEBE_FILTRAR_DATOS_PARA_GENERAR_ARCHIVO")
			},$scope.winID);
			return;
		}
		
		bcsComunes.ventanaOverlay($scope.winID, true);
		
		//Parámetros de entrada al job
		var dataCols = [];

		//Parámetros fijos de entrada para el job
		dataCols.push({NombreParametro : 'IdEmpresa',    			Valor : $scope.SesionIdEmpresa});
		dataCols.push({NombreParametro : 'IdUsuario',    			Valor : $scope.SesionIdUsuario});
		dataCols.push({NombreParametro : 'CodigoFormatoArchivo',    Valor : 'ANEXO13_DETALLE_SALDOS_CLIENTES'});
		
		
		//Parámetros Variables de entrada: los usa el SP que consulta data para el archivo (son los mismos que los filtros de búsqueda)
		dataCols.push({NombreParametro : 'IdEmpleado',    			Valor : $scope.oConfiguracionEjecutivo.idActual !== undefined && $scope.oConfiguracionEjecutivo.idActual !== 0 ? Number($scope.oConfiguracionEjecutivo.seleccionado.IdEmpleadoEjecutivo) : $scope.SesionIdEmpleado});		
		dataCols.push({NombreParametro : 'SaldoLibroInventario',    Valor : 0});
		dataCols.push({NombreParametro : 'IdEstado',    			Valor : 'CRT'});
		dataCols.push({NombreParametro : 'FechaEfectivaMovimiento', Valor : util.dateSPdate($scope.kngFechaEfectiva)});
		dataCols.push({NombreParametro : 'IdCliente',    			Valor : $scope.oConfiguracionPortafolio.idActual ? Number($scope.oConfiguracionPortafolio.seleccionado.IdCliente) : 0});
		dataCols.push({NombreParametro : 'IdPortafolio',    		Valor : $scope.oConfiguracionPortafolio.idActual ? Number($scope.oConfiguracionPortafolio.seleccionado.IdPortafolio) : 0});
		dataCols.push({NombreParametro : 'IdTipoMovimiento',    	Valor : $scope.ngCuentaPorCobrar ? 'MOVCAR' : 'MOVABN'});
		
		
		if (dataCols.length > 0)
		{
			var datos = {};
			datos.p_IdEmpresa               = $scope.SesionIdEmpresa; 
			datos.p_IdUsuario               = $scope.SesionIdUsuario; 
			datos.p_IdModulo                = 'CTA';
			datos.p_UsaReglasNegocio        = 'S';
			datos.p_IdTipoPrograma		    = 'ANEXO13_DETALLE_SALDOS_CLIENTES';		
			datos.p_ParametrosPrograma	    = dataCols;
			bcsComunes.log().info(JSON.stringify(datos));
			
			bcsServiciosCuentaInversion.SolicitudGenerarArchivo(datos).$promise.then(
					function(result) {
						bcsComunes.ventanaOverlay($scope.winID,false);
						bcsComunes.ventanaMsg({
							conflictos:util.parseServerResponse(result)
						},$scope.winID);
						
					},
					function(result){
						bcsComunes.ventanaOverlay($scope.winID,false);					
						bcsComunes.msg("error","Error",$translate.instant('UI_SOLICITUD_GENERACION_ARCHIVO_ERROR'));
					});
		}
		
		
	};

	let firstHeight = $('#Win_CuentasPorPagarYCobrar');
		firstHeight.css('height', '668px');
	
});