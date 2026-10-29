angular.module('ModuloTesoreria').controller('RevisionCierreDiarioCtrl',
		function($scope,$translate,$modal,$log,$resource,$timeout,
				bcsComunes,bcsAlerta,OPCIONES_BCSALERTA, bcsBlotterComprobantesGrid, bcsServiciosTesoreria)
{

	var filtrosResumenCierreDiario = {};
	
	$scope.tab = {}; //workaround para el manejo de grillas dentro de tabs.
	$scope.tab.active = 0;
	

	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	
	$scope.kngFechaCierre = util.dateInicioDia($scope.SesionFechaSistema);	
	
	 
	
	//Obtiene datos de la Empresa
	if ($scope.SesionIdEmpresa)
	{
			var empresaDatasource = new kendo.data.DataSource({
				type: "odata",
				serverFiltering:true,
	    		transport: {
	    			read: {	    				
	    				url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1234",
	    				dataType: "json"
			    	}
				},
				filter: { field: "IdEmpresa", operator: "eq", value: Number($scope.SesionIdEmpresa) },
			});
	
	        empresaDatasource.fetch().then(function(){
				 if (empresaDatasource.data().length != 1 ) 
				 {
					 console.log("Empresa no existe!");
					 return false;
				 }
				 else 
				 {
					 $scope.$apply(function() {						
						$scope.Empresa = empresaDatasource.data()[0].DocumentoIdentidad +' - '+ $scope.SesionEmpresa;
						return true;
					 });
				 }
		    });		
	};
	
	

	//-----------------------------------
	// CONFIGURACION DE GRILLAS
	//-----------------------------------
	
	//Grilla Resumen Revisión Cierre Diario
	var dsResumenCierreDiario = new bcs.DataSource({
		type : "call",
		definition : {
			serverFiltering 	: false, // Filtrado de datos en el servidor.
			serverPaging 		: false, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.						
			callParameters      : filtrosResumenCierreDiario,
			callTransaction 	: "1783",     //BSCL.Tesoreria.BD.Servicios.CIE::ConsultarResumenCierreDiario
			callResultSet 		: "P_OUT_RESUMENCIERREDIARIO",
			schema : {
				model : {
					fields : {
						IdModuloFuncionProceso			: { type : "number" },
						NombreFuncionProceso			: { type : "string" },
						DescripcionFuncionProceso		: { type : "string" },
						PermiteEjecucion				: { type : "string" },						
						IdRevisionCierreDiario       	: { type : "number" },
						IdUsuario                    	: { type : "number" },												
						CodigoUsuario					: { type : "string" },															
						FechaCierre						: { type : "sysdate"},
						IdEstadoRevisionCierreDiario	: { type : "string" },
						NombreEstadoRevisionCierreDiario: { type : "string" },
						TotalOperaciones				: { type : "number" },
						TotalConProblemas				: { type : "number" },
						Resultado						: { type : "string" }
					}
				}
			}
		}
	});

	
	$scope.confBasicaResumenRevisionCierreDiario = {
		dataSource: dsResumenCierreDiario,
			
		gridCreated: function(grid) {
			$scope.gridApiResumenRevisionCierreDiario = grid;
		},
		
		columns: [
					{field : "DescripcionFuncionProceso",		title : "UI_PROCESO", 			width : "250px"},					
					{field : "TotalOperaciones",				title : "UI_TOTAL_OPERACIONES",	width : "80px"},
					{field : "TotalConProblemas",				title : "UI_TOTAL_PROBLEMAS",	width : "80px"},
					{field : "Resultado", 						title : "UI_RESULTADO",			width : "200px"},					
					{field : "NombreEstadoRevisionCierreDiario",title : "UI_ESTADO",			width : "70px"},					
					{field : "CodigoUsuario",					title : "UI_USUARIO",			width : "70px"},					
				],

		sortable		: true,
		pageable		: false,
		filterable		: false,
		scrollable		: true,
		height			: 175,
		selectable 		: "multiple row",
		userSettings	: true,
		navigatable		: true,
  		autoBind 		: false
	};
	
	
	
	//Configuración grilla Comprobantes sin liquidar	
    var dsComprobantesSinLiquidar = new bcs.DataSource(bcsBlotterComprobantesGrid.dsDefComprobantes);
    $scope.confBasicaComprobantesSinLiquidar = {
			dataSource			: dsComprobantesSinLiquidar,
			columns				: bcsBlotterComprobantesGrid.columnasComprobantes(),
			sortable			: true,
			pageable			: false,
			autoBind			: false,
			filterable			: true,
			height				: 340,
			selectable 			: "multiple row",
			userSettings		: true,
			rowColorBackground : function(row) {
				console.log(row);
				//Certificado / Liquidado / Enviado Boveda / Enviado a Deposito
				if(row.get("IdEstado")!=='CER' && row.get("IdEstado")!=='LIQ' && row.get("IdEstado")!=='EBV' && row.get("IdEstado")!=='NDP')					
				{	
					return "red";
				}
		  	}
	};	   
    

    
	//Grilla Documentos	sin Certificar
	var dsDocumentosSinCertificar = new bcs.DataSource(bcsBlotterComprobantesGrid.dsDefDocumentos);
	$scope.confBasicaDocumentosSinCertificar = {
			dataSource			: dsDocumentosSinCertificar,
			columns				: bcsBlotterComprobantesGrid.columnasDocumentos(),
			sortable			: true,
			pageable			: false,
			autoBind			: false,
			filterable			: true,
			height				: 340,
			selectable 			: "multiple row",
			userSettings		: true,								  
			rowColorBackground : function(row) {
				console.log(row);
				if(row.get("IdEstado")=='ING')
				{	
					return "red";
				}
		  	}		  	
	};

	

	
	
			
	$scope.$watch('seleccionResumenRevisionCierreDiario',function(nval,oval) 
	{
		if(nval && nval[0].NombreFuncionProceso === 'INF_CMBT_SLIQ')
		{
			$scope.tab.t1 = 1;
		}
		else if(nval && nval[0].NombreFuncionProceso === 'INF_DOCT_SCER')
		{
			$scope.tab.t2 = 1;
		}
	});
	


	
	
	$scope.gridSelecionada = "";
	$scope.select = function(grid)
	{
		$scope.gridSelecionada = grid;
		util.refreshGridSize(grid, 50);
	};
	
	$scope.exportarExcel = function(){
		//console.log($scope.gridSelecionada);
		if($scope.gridSelecionada==="grid-comprobantes-sin-liquidar")
		{
			$scope.tab.exportarExcel1();			           
		}
		else if($scope.gridSelecionada==="grid-documentos-sin-certificar")
		{
			$scope.tab.exportarExcel2();			
		}
	};

	
	//--------------------------------------
	// FUNCIONES
	//--------------------------------------	
	
	$scope.limpiarData = function()
	{
		dsResumenCierreDiario.removeAll();
		dsComprobantesSinLiquidar.removeAll();
	  	dsDocumentosSinCertificar.removeAll();
	}
	
	
    $scope.buscarComprobantesSinLiquidar = function()
    {
		var filtrosComprobantes = [];
		
		filtrosComprobantes.push({ field: "IdEmpresa", operator: "eq", value: Number($scope.SesionIdEmpresa)});
		filtrosComprobantes.push({ field: "FechaLiquidacion", operator: "eq", value: util.dateInicioDia($scope.kngFechaCierre) });
		filtrosComprobantes.push({ field: "IdEstado", operator: "neq", value: 'ANU'});  //No considerar anulados
		filtrosComprobantes.push({ field: "IdEstado", operator: "neq", value: 'BLO'});  //No considerar bloqueados
		filtrosComprobantes.push({ field: "IdEstado", operator: "neq", value: 'MOD'});  //No considerar modificados
		
		dsComprobantesSinLiquidar.filter(filtrosComprobantes);    	
    };
    

    $scope.buscarDocumentosSinCertificar = function()
    {
    	var filtrosDocumentos = {};
    	var filtros1 = [];
    	var filtros2 = [];
    	
    	filtros1.push({ field: "IdEmpresa", operator: "eq", value: Number($scope.SesionIdEmpresa)});
    	filtros1.push({ field: "FechaLiquidacion", operator: "eq", value: util.dateInicioDia($scope.kngFechaCierre) });
    	filtros1.push({ field: "IdTipoMovimiento", operator: "eq", value: 'MOVING'});																		
    	filtros1.push({ field: "IdEstado", operator: "neq", value: 'ANU'});
    	filtros1.push({ field: "IdEstado", operator: "neq", value: 'GIR'});
    	filtros1.push({ field: "IdEstado", operator: "neq", value: 'EMI'});
    	
    	filtros2.push({ field: "IdMedioDePago", operator: "eq", value: 'CHE'});
    	filtros2.push({ field: "IdMedioDePago", operator: "eq", value: 'VVI'});
    	filtros2.push({ field: "IdMedioDePago", operator: "eq", value: 'BDE'});
    	filtros2.push({ field: "IdMedioDePago", operator: "eq", value: 'EFE'});
    	filtros2.push({ field: "IdMedioDePago", operator: "eq", value: 'EBA'});
    	
    	filtrosDocumentos = {
    			filters: [
    						{
    						  logic: "or",
    						  filters: filtros2
    						},
    						{
    						  logic: "and",
    						  filters: filtros1	
    						}
    					 ]
    		   };
    	dsDocumentosSinCertificar.filter(filtrosDocumentos);		    	
    };
	

	$scope.buscarResumen =  function()
	{	
		$scope.limpiarData();
		
		filtrosResumenCierreDiario.p_IdEmpresa 			= $scope.SesionIdEmpresa;
		filtrosResumenCierreDiario.p_IdUsuario         	= $scope.SesionIdUsuario;
		filtrosResumenCierreDiario.p_IdModulo			= 'TES';		
		filtrosResumenCierreDiario.p_simularOperacion 	= 'N';
		filtrosResumenCierreDiario.p_UsaReglasNegocio 	= 'S';
		filtrosResumenCierreDiario.p_FechaCierre 		= util.dateSPdate($scope.kngFechaCierre);
		
		 
		dsResumenCierreDiario.setCallParameters(filtrosResumenCierreDiario); // Carga resumen		
		$scope.buscarComprobantesSinLiquidar();  //Carga detalle: comprobantes sin liquidar 
		$scope.buscarDocumentosSinCertificar();  //Carga detalle: documentos sin certificar
	};
	
	

	$scope.cerrarProcesoFuncion = function()
	{
		
		if ($scope.seleccionResumenRevisionCierreDiario === undefined || $scope.seleccionResumenRevisionCierreDiario === false)
		{	
			bcsComunes.msg("warning","warning",$translate.instant('UI_DEBE_SELECCIONAR_LOS_PROCESOS_A_CERRAR'));
			return false;
		}

		
		var procesosACerrar = [];
		$.each($scope.seleccionResumenRevisionCierreDiario, function(key, value){
			procesosACerrar.push({
									NombreFuncionProceso	: value.NombreFuncionProceso,
									IdModuloFuncionProceso	: value.IdModuloFuncionProceso,
									TotalConProblemas		: value.TotalConProblemas,
									TotalOperaciones		: value.TotalOperaciones,
								    TotalConProblemas		: value.TotalConProblemas,
								    Resultado				: value.Resultado								  
								}); 
		});
 
		var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_CERRARAN_LOS_PROCESOS_SELECCIONADOS'));
		t.result.then(function(result)
		{
			if (result===OPCIONES_BCSALERTA.ACEPTAR)
			{
				bcsComunes.ventanaOverlay($scope.winID, true);
				
				var datos = {};
				datos.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
				datos.p_IdUsuario               	= $scope.SesionIdUsuario; 
				datos.p_IdModulo                	= 'TES';			
				datos.p_simularOperacion			= 'N';
				datos.p_UsaReglasNegocio			= 'S';
				datos.p_FechaCierre 		    	= util.dateSPdate($scope.kngFechaCierre);				
				datos.p_ProcesosACerrar			    = procesosACerrar;
					
				bcsComunes.log().info(JSON.stringify(datos));


				bcsServiciosTesoreria.CierreDiario(
					datos,
					function(respuesta){
						bcsComunes.ventanaOverlay($scope.winID, false);
						 		
						if (respuesta.P_OUT_ERRORESCONTADOR === 0){	
							
							if (respuesta.P_OUT_ADVERTENCIASCONTADOR === 0)
							{
								bcsComunes.ventanaMsg({									
									mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_EXITO"),
									conflictos:util.parseServerResponse(respuesta)
								},$scope.winID);
							}
							else
							{
								bcsComunes.ventanaMsg({
									mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ADVERTENCIAS"),
									conflictos:util.parseServerResponse(respuesta)
								},$scope.winID);
							}								
								
							
							
							//Actualiza resumen cierre diario
							$scope.gridApiResumenRevisionCierreDiario.dataSource.read();
			
						} 
						else 
						{
							bcsComunes.ventanaMsg({
								mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
								conflictos:util.parseServerResponse(respuesta)
							},$scope.winID);
						}
					},
					function(respuesta){						
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
							conflictos:util.parseServerResponse(respuesta)
						},$scope.winID);
						bcsComunes.ventanaOverlay($scope.winID, false);
					}
				);			

			}
		});		
		
		return;	
	};	


	$scope.cerrarModulo = function()
	{	
		var ventanaCierreModulos = bcsComunes.ventanaAbrir("CierreModulos", {}, false, false, $scope.winID);
	};

	var firstHeightCrear = $('#Win_RevisionCierreDiario');
        firstHeightCrear.css('height', '741px');
	
});