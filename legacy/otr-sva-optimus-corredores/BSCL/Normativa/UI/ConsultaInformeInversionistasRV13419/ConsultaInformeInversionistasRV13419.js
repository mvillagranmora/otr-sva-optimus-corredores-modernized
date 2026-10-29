angular.module('ModuloNormativa').controller('ConsultaInformeInversionistasRV13419Ctrl',
	function($scope,$translate,$resource,$timeout,bcsComunes,bcsServicioInterfaces)
	{	

		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var row = '';

		$scope.seRealizoBusqueda = false;

		$scope.data = {};
		
		var cInvRV = this;
		
		cInvRV.options 	= {};
		cInvRV.filtro		= {};
		cInvRV.filtro.FechaDesde = util.dateInicioDia($scope.SesionFechaSistema);
		cInvRV.filtro.FechaHasta = util.dateInicioDia($scope.SesionFechaSistema);
				
		//Configurar al principio

		cInvRV.data = {};
		cInvRV.PeriodoOptions = {format: 'MMMM yyyy', start: 'year',depth: 'year'};

		var fechaMinima = util.dateInicioDia($scope.SesionFechaSistema);
		
		cInvRV.options.oConfiguracionFechaDesde = {
				format: 'MMMM yyyy', 
				start: 'year',
				depth: 'year',
				max: fechaMinima
				//min: dya.filtro.Fecha
		};
		
		cInvRV.options.oConfiguracionFechaHasta = {
				format: 'MMMM yyyy', 
				start: 'year',
				depth: 'year',
				min: cInvRV.filtro.FechaDesde,
				max: fechaMinima//fechaSistema
		};
		
		cInvRV.buscarRegistros = function(nroPag, filtrosAnteriores){

			const datos = {};
			datos.p_IdEmpresa		= $scope.SesionIdEmpresa;
			datos.p_IdUsuario		= $scope.SesionIdUsuario;
			datos.p_IdModulo		= "NOR";
			datos.p_FechaInicio		= util.dateSPdate(cInvRV.filtro.FechaDesde);
			datos.p_FechaTermino	= util.dateSPdate(cInvRV.filtro.FechaHasta); 
			
			dsInversionistasRV.setCallParameters(datos);
			dsInversionistasRV.read();
			
			$scope.seRealizoBusqueda = true;
		};


		/******************************************************
		 * GRILLAS
		 * ***************************************************/
		
		var dsInversionistasRV = new bcs.DataSource({
			type: "call",
			definition 	: {
				pageSize 		: 15,
				callTransaction	: "600089",
				callResultSet	: "P_OUT_TABLA",
				schema: {
					model: {
						fields: {
							Periodo												: { type: "string"},
					    	NombreCorredor										: { type: "string"},
					    	CodigoCorredor										: { type: "string"},
					    	ClientesExtranjerosNoMilaMontoTotal					: { type: "number"},
					    	ClientesExtranjerosNoMilaMontoCompras				: { type: "number"},
					    	ClientesExtranjerosNoMilaMontoVentas				: { type: "number"},
					    	ClientesExtranjerosNoMilaCantidadTotal				: { type: "number"},
					    	ClientesExtranjerosNoMilaCantidadCompras			: { type: "number"},
					    	ClientesExtranjerosNoMilaCantidadVentas				: { type: "number"},
					    	ClientesExtranjerosNoMilaCantidadClientes			: { type: "number"},
					    	ClientesExtranjerosMilaMontoTotal					: { type: "number"},
					    	ClientesExtranjerosMilaMontoCompras					: { type: "number"},
					    	ClientesExtranjerosMilaMontoVentas					: { type: "number"},
					    	ClientesExtranjerosMilaCantidadTotal				: { type: "number"},
					    	ClientesExtranjerosMilaCantidadCompras				: { type: "number"},
					    	ClientesExtranjerosMilaCantidadVentas				: { type: "number"},
					    	ClientesExtranjerosMilaCantidadClientes				: { type: "number"},
					    	ClientesInstitucionalesNacionalesMontoTotal			: { type: "number"},
					    	ClientesInstitucionalesNacionalesMontoCompras		: { type: "number"},
					    	ClientesInstitucionalesNacionalesMontoVentas		: { type: "number"},
					    	ClientesInstitucionalesNacionalesCantidadTotal		: { type: "number"},
					    	ClientesInstitucionalesNacionalesCantidadCompras	: { type: "number"},
					    	ClientesInstitucionalesNacionalesCantidadVentas		: { type: "number"},
					    	ClientesInstitucionalesNacionalesCantidadClientes	: { type: "number"},
					    	ClientesNoInstitucionalesNacionalesMontoTotal		: { type: "number"},
					    	ClientesNoInstitucionalesNacionalesMontoCompras		: { type: "number"},
					    	ClientesNoInstitucionalesNacionalesMontoVentas		: { type: "number"},
					    	ClientesNoInstitucionalesNacionalesCantidadTotal	: { type: "number"},
					    	ClientesNoInstitucionalesNacionalesCantidadCompras	: { type: "number"},
					    	ClientesNoInstitucionalesNacionalesCantidadVentas	: { type: "number"},
					    	ClientesNoInstitucionalesNacionalesCantidadClientes	: { type: "number"},
					    	CuentaPropiaYRelacionadosMontoTotal					: { type: "number"},
					    	CuentaPropiaYRelacionadosMontoCompras				: { type: "number"},
					    	CuentaPropiaYRelacionadosMontoVentas				: { type: "number"},
					    	CuentaPropiaYRelacionadosCantidadTotal				: { type: "number"},
					    	CuentaPropiaYRelacionadosCantidadCompras			: { type: "number"},
					    	CuentaPropiaYRelacionadosCantidadVentas				: { type: "number"},
					    	CuentaPropiaYRelacionadosCantidadClientes			: { type: "number"},
						}
					}
				}
			}
		});


		cInvRV.gridInversionistasRV = {
			dataSource: dsInversionistasRV,
			columns: [				
			    {title: 'UI_FECHA', align: "left" , editable: false,
			    		columns : [ {field : "Periodo", 	title:'UI_MES_ANO', width: "90px", sortable : true}]},
			    {title: 'UI_IDENTIFICACION_CORREDOR', align: "left" , editable: false,
		    			columns : [ {field : "NombreCorredor", 	title:'UI_NOMBRE', 	width: "250px", sortable : true},
		    						{field : "CodigoCorredor", 	title:'UI_CODIGO', 	width: "90px", sortable : true}]},
			    {title: 'UI_MONTOS_CLIENTES_EXTRANJEROS_NO_MILA', align: "rigth",  editable: false,
		    							columns : [ {field : "ClientesExtranjerosNoMilaMontoTotal", 	title:$translate.instant('UI_TOTAL'), 	width: "90px", sortable : true},
		    										{field : "ClientesExtranjerosNoMilaMontoCompras", 	title:$translate.instant('UI_COMPRAS'),	width: "90px", sortable : true},
		    										{field : "ClientesExtranjerosNoMilaMontoVentas", 	title:$translate.instant('UI_VENTAS'), 	width: "90px", sortable : true}]},								
		    	{title:'UI_NEGOCIOS_CLIENTES_EXTRANJEROS_NO_MILA',
		    			    			columns : [ {field : "ClientesExtranjerosNoMilaCantidadTotal", 	title:$translate.instant('UI_TOTAL'), 	width: "90px", sortable : true},
		    			    						{field : "ClientesExtranjerosNoMilaCantidadCompras", 	title:$translate.instant('UI_COMPRAS'),	width: "90px", sortable : true},
		    			    						{field : "ClientesExtranjerosNoMilaCantidadVentas", 	title:$translate.instant('UI_VENTAS'), 	width: "90px", sortable : true}]},
		    	{title:'UI_CLIENTES_EXT_NO_MILA',
		    			    			columns : [ {field : "ClientesExtranjerosNoMilaCantidadClientes", 	title:$translate.instant('UI_NUMERO'), 	width: "90px", sortable : true}]},
		    	{title: 'UI_MONTOS_CLIENTES_EXTRANJEROS_MILA', align: "rigth",  editable: false,
		    							columns : [ {field : "ClientesExtranjerosMilaMontoTotal", 	title:$translate.instant('UI_TOTAL'), 	width: "90px", sortable : true},
		    										{field : "ClientesExtranjerosMilaMontoCompras", 	title:$translate.instant('UI_COMPRAS'),	width: "90px", sortable : true},
		    										{field : "ClientesExtranjerosMilaMontoVentas", 	title:$translate.instant('UI_VENTAS'), 	width: "90px", sortable : true}]},
		    	{title:'UI_NEGOCIOS_CLIENTES_EXTRANJEROS_MILA',
		    			    			columns : [ {field : "ClientesExtranjerosMilaCantidadTotal", 	title:$translate.instant('UI_TOTAL'), 	width: "90px", sortable : true},
		    			    						{field : "ClientesExtranjerosMilaCantidadCompras", 	title:$translate.instant('UI_COMPRAS'),	width: "90px", sortable : true},
		    			    						{field : "ClientesExtranjerosMilaCantidadVentas", 	title:$translate.instant('UI_VENTAS'), 	width: "90px", sortable : true}]},
		    	{title:'UI_CLIENTES_EXTRANJEROS_MILA',
		    			    			columns : [ {field : "ClientesExtranjerosMilaCantidadClientes", 	title:$translate.instant('UI_NUMERO'), 	width: "90px", sortable : true}]},
		    	{title: 'UI_MONTOS_CLIENTES_INSTITUCIONALES_LOCALES', align: "rigth",  editable: false,
		    							columns : [ {field : "ClientesInstitucionalesNacionalesMontoTotal", 	title:$translate.instant('UI_TOTAL'), 	width: "90px", sortable : true},
		    										{field : "ClientesInstitucionalesNacionalesMontoCompras", 	title:$translate.instant('UI_COMPRAS'),	width: "90px", sortable : true},
		    										{field : "ClientesInstitucionalesNacionalesMontoVentas", 	title:$translate.instant('UI_VENTAS'), 	width: "90px", sortable : true}]},
		    	{title:'UI_NEGOCIOS_CLIENTES_INSTITUCIONALES_LOCALES',
		    			    			columns : [ {field : "ClientesInstitucionalesNacionalesCantidadTotal", 	title:$translate.instant('UI_TOTAL'), 	width: "90px", sortable : true},
		    			    						{field : "ClientesInstitucionalesNacionalesCantidadCompras", 	title:$translate.instant('UI_COMPRAS'),	width: "90px", sortable : true},
		    			    						{field : "ClientesInstitucionalesNacionalesCantidadVentas", 	title:$translate.instant('UI_VENTAS'), 	width: "90px", sortable : true}]},
		    	{title:'UI_CLIENTES_INST_LOCALES',
		    			    			columns : [ {field : "ClientesInstitucionalesNacionalesCantidadClientes", 	title:$translate.instant('UI_NUMERO'), 	width: "90px", sortable : true}]},
		    	{title: 'UI_MONTOS_CLIENTES_NO_INSTITUCIONALES_LOCALES', align: "rigth",  editable: false,
		    							columns : [ {field : "ClientesNoInstitucionalesNacionalesMontoTotal", 	title:$translate.instant('UI_TOTAL'), 	width: "90px", sortable : true},
		    										{field : "ClientesNoInstitucionalesNacionalesMontoCompras", 	title:$translate.instant('UI_COMPRAS'),	width: "90px", sortable : true},
		    				    					{field : "ClientesNoInstitucionalesNacionalesMontoVentas", 	title:$translate.instant('UI_VENTAS'), 	width: "90px", sortable : true}]},
		    	{title:'UI_NEGOCIOS_CLIENTES_NO_INSTITUCIONALES_LOCALES',
		    				    		columns : [ {field : "ClientesNoInstitucionalesNacionalesCantidadTotal", 	title:$translate.instant('UI_TOTAL'), 	width: "90px", sortable : true},
		    				    					{field : "ClientesNoInstitucionalesNacionalesCantidadCompras", 	title:$translate.instant('UI_COMPRAS'),	width: "90px", sortable : true},
		    				    					{field : "ClientesNoInstitucionalesNacionalesCantidadVentas", 	title:$translate.instant('UI_VENTAS'), 	width: "90px", sortable : true}]},
		    	{title:'UI_CLIENTES_NO_INST_LOCALES',
		    				    		columns : [ {field : "ClientesNoInstitucionalesNacionalesCantidadClientes", 	title:$translate.instant('UI_NUMERO'), 	width: "90px", sortable : true}]},
		    	{title: 'UI_MONTOS_CLIENTES_CUENTA_PROPIA_Y_RELACIONADOS', align: "rigth",  editable: false,
		       							columns : [ {field : "CuentaPropiaYRelacionadosMontoTotal", 	title:$translate.instant('UI_TOTAL'), 	width: "90px", sortable : true},
		       										{field : "CuentaPropiaYRelacionadosMontoCompras", 	title:$translate.instant('UI_COMPRAS'),	width: "90px", sortable : true},
		       										{field : "CuentaPropiaYRelacionadosMontoVentas", 	title:$translate.instant('UI_VENTAS'), 	width: "90px", sortable : true}]},
		       {title:'UI_NEGOCIOS_CLIENTES_CUENTA_PROPIA_Y_RELACIONADOS',
		       			    			columns : [ {field : "CuentaPropiaYRelacionadosCantidadTotal", 	title:$translate.instant('UI_TOTAL'), 	width: "90px", sortable : true},
		       			    						{field : "CuentaPropiaYRelacionadosCantidadCompras", 	title:$translate.instant('UI_COMPRAS'),	width: "90px", sortable : true},
		       			    						{field : "CuentaPropiaYRelacionadosCantidadVentas", 	title:$translate.instant('UI_VENTAS'), 	width: "90px", sortable : true}]},
		       {title:'UI_CLIENTES_CTA_PROPIA_Y_RELACIONADOS',
		       			    			columns : [ {field : "CuentaPropiaYRelacionadosCantidadClientes", 	title:$translate.instant('UI_NUMERO'), 	width: "90px", sortable : true}]}],		
			sortable			: true,
			pageable			: true,
			autoBind			: false,
	    	filterable			: true,
			height				: 550,
		    selectable 			: "multiple row",
		    userSettings		: true,
			navigatable			: true,
			excel : {
		        allPages: true,
		      },
		};

		$scope.generarInformeInversionistasRV13419 = function (){
		bcsComunes.ventanaOverlay(idVentana, true)
		const datos = {
			p_IdEmpresa: $scope.SesionIdEmpresa,
					p_IdUsuario: $scope.SesionIdUsuario,
					p_IdModulo: 'NOR',
					p_TipoPrograma: [{
						IdInterno: 0,
						IdTipoPrograma: "GenerarInformeInversionistasRV",
						SegundosDesfase: 0
					}],
					p_Parametro: [{
						IdInterno: 0,
						NombreParametro: 'IdEmpresa',
						Valor: $scope.SesionIdEmpresa
					}, {
						IdInterno: 0,
						NombreParametro: 'IdUsuario',
						Valor: $scope.SesionIdUsuario
					}, {
						IdInterno: 0,
						NombreParametro: 'IdModulo',
						Valor: 'NOR'
					}, {
						IdInterno: 0,
						NombreParametro: 'FechaInicio',
						Valor: util.dateSPdate(cInvRV.filtro.FechaDesde)
					}, {
						IdInterno: 0,
						NombreParametro: 'FechaTermino',
						Valor: util.dateSPdate(cInvRV.filtro.FechaHasta)
					}]
		};
		
		if(cInvRV.gridInversionistasRV.dataSource.total() > 0){
			bcsServicioInterfaces.InscribirProcesoBatch(datos).$promise.then(
				function (result) {
					bcsComunes.ventanaOverlay(idVentana, false);
					var fechaLocal = util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion, "FechaHora", "CALL");
					bcsComunes.ventanaMsg({
						mensaje: $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO').replace("::IdPrograma", result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion", util.formatoSecondate(fechaLocal)),
						conflictos: util.parseServerResponse(result)
					}, $scope.winID);
	
				},
				function (result) {
					bcsComunes.ventanaOverlay(idVentana, false);
					bcsComunes.msg("error", "Error", $translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
			});
		}else{
			bcsComunes.ventanaOverlay(idVentana, false);
			bcsComunes.msg("error", "Error", $translate.instant('UI_SIN_DATOS_INFORME_RV'));
		}

		}

		
		$scope.$watch('cInvRV.filtro.FechaDesde',function(newval,oldval){
			
			cInvRV.options.oConfiguracionFechaHasta = {
					format: 'MMMM yyyy', 
					start: 'year',
					depth: 'year',
					min: cInvRV.filtro.FechaDesde,
					max: fechaMinima//fechaSistema
			};
			if(cInvRV.filtro.FechaDesde > cInvRV.filtro.FechaHasta){
				cInvRV.filtro.FechaHasta = cInvRV.filtro.FechaDesde
			}
		});			 
	});