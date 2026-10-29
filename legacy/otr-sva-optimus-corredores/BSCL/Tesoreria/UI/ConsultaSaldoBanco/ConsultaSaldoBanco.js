angular.module('ModuloTesoreria').controller('ConsultaSaldoBancoCtrl',
		function($scope,$timeout,$modal,$state,bcsComunes,$translate,bcsServiciosTesoreria)
{	
	 
	var filters = {};
	$scope.filtros = {};
	$scope.winID = bcsComunes.ventanaWinId($scope);

	
	//SETEO INICIAL
	$scope.filtros.kngFechaSistema 	= util.dateInicioDia($scope.SesionFechaSistema);
	$scope.incluyeSaldosFuturos    = "N";     //Parámetro tesorería que indica si se muestran saldos futuros (T1, T2, T3)					 
	$scope.incluyeSaldosPendientes = "N";
	$scope.incluyeSaldosContables  = "N";
 
	$scope.filtros.chkSoloMovimientosQueAfactanBanco = true;  // Permite excluir los movimientos que no tienen asociado banco ni cuenta bancaria en su forma de pago
	
	
	//COMBOBOX
	$scope.filtros.oConfiguracionBanco={
		tipo : "combobox-banco-cuentas-bancarias-corredora",
		configuracion : {},
		filtros : {
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	$scope.filtros.oConfiguracionMonedaTesoreria={
		tipo : "combobox-moneda-tesoreria",
		configuracion : {},
		filtros : {
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};

	var excelFormat = function (item) {
		return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;
	};


	var dsSaldo = new bcs.DataSource({
		type : "call",
		definition : {
			serverFiltering 	: false, // Filtrado de datos en el servidor.
			serverPaging 		: false, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.						
			callParameters      : filters,
			callTransaction 	: "314" ,
			callResultSet 		: "P_OUT_SALDOCUENTABANCO",
			aggregate			: [
								   { field: "SaldoDisponibleEgreso", aggregate: "sum" },
								   { field: "SaldoDisponibleIngreso", aggregate: "sum" },
								   { field: "SaldoDisponible", aggregate: "sum" },
								   
								   { field: "SaldoPendienteIngreso", aggregate: "sum" },
								   { field: "SaldoPendienteEgreso", aggregate: "sum" },
								   { field: "SaldoPendiente", aggregate: "sum" },

								   { field: "SaldoContableEgreso", aggregate: "sum" },
								   { field: "SaldoContableIngreso", aggregate: "sum" },
								   { field: "SaldoContable", aggregate: "sum" },

								   { field: "SaldoIngresoT1", aggregate: "sum" },
								   { field: "SaldoEgresoT1", aggregate: "sum" },
								   { field: "SaldoT1", aggregate: "sum" },

								   { field: "SaldoIngresoT2", aggregate: "sum" },
								   { field: "SaldoEgresoT2", aggregate: "sum" },
								   { field: "SaldoT2", aggregate: "sum" },

								   { field: "SaldoIngresoT3", aggregate: "sum" },
								   { field: "SaldoEgresoT3", aggregate: "sum" },
								   { field: "SaldoT3", aggregate: "sum" },
								  ],

			schema : {
				model : {
					fields : {										
						IdBanco					: { type : "number" },
						IdCuentaBanco			: { type : "number" },
						IdMoneda				: { type : "string" },
						NombreBanco				: { type : "string" },
						NumeroCuentaBancaria    : { type : "string" },
						NombreCuentaBanco		: { type : "string" },
						NombreMoneda			: { type : "string" },

						//Disponible
					  	SaldoDisponibleInicial	: { type : "number", excelFormat: excelFormat },
					  	SaldoDisponibleIngreso	: { type : "number", excelFormat: excelFormat },
					  	SaldoDisponibleEgreso	: { type : "number", excelFormat: excelFormat },
					  	SaldoDisponible			: { type : "number", excelFormat: excelFormat },

					  	//Pendiente
					  	SaldoPendienteIngreso	: { type : "number", excelFormat: excelFormat },
					  	SaldoPendienteEgreso	: { type : "number", excelFormat: excelFormat },
					  	SaldoPendiente			: { type : "number", excelFormat: excelFormat },

					  	//Contable
					  	SaldoContableIngreso	: { type : "number", excelFormat: excelFormat },
					  	SaldoContableEgreso		: { type : "number", excelFormat: excelFormat },
					  	SaldoContable			: { type : "number", excelFormat: excelFormat },

					  	//T1
					  	FechaT1					: { type : "string", excelFormat: excelFormat },
					  	SaldoIngresoT1			: { type : "number", excelFormat: excelFormat },
					  	SaldoEgresoT1			: { type : "number", excelFormat: excelFormat },
					  	SaldoT1					: { type : "number", excelFormat: excelFormat },

					  	//T2
					  	FechaT2					: { type : "date" },
					  	SaldoIngresoT2			: { type : "number", excelFormat: excelFormat },
					  	SaldoEgresoT2			: { type : "number", excelFormat: excelFormat },
					  	SaldoT2					: { type : "number", excelFormat: excelFormat },

					  	//T3
					  	FechaT3					: { type : "date" },
					  	SaldoIngresoT3			: { type : "number", excelFormat: excelFormat },
					  	SaldoEgresoT3			: { type : "number", excelFormat: excelFormat },
					  	SaldoT3					: { type : "number", excelFormat: excelFormat }
					}
				}
			}
		}
	});


	
	var gridInit = function(grid) {
		$scope.buscarInfoSaldos();
		$scope.ApiGridInfoSaldos = grid;
	}
	
	$scope.confBasicaSaldoBanco = {
		dataSource  : dsSaldo,
		columns 	: [
					 {field : "NombreBanco", 			title:"UI_BANCO", 				width: "200px", groupable: true}						
					,{field : "IdMoneda", 				title:"UI_MONEDA", 				width: "57px", 	groupable: true}
					,{field : "NombreCuentaBanco", 		title:"UI_ALIAS_CUENTA_BANCO", 	width: "150px"}					
					,{field : "NumeroCuentaBancaria", 	title:"UI_NUMERO_CUENTA", 	    width: "200px"}
					
					//Datos saldo disponible						
					,{title   : $translate.instant("UI_SALDO_DISPONIBLE")+" "+$translate.instant("UI_AL")+" " +util.formatoDate($scope.SesionFechaSistema), align: "center",
					  columns : [{field : "SaldoDisponibleInicial", title:"UI_INICIAL", width : "80px",	template : " <label> #:(util.bcs_formato(SaldoDisponibleInicial,'GEN','Monto',IdMoneda,'','')).valor #</label>"}									
								,{field : "SaldoDisponibleEgreso",	title:'UI_EGRESO',	width : "80px",	template : " <label> #:(util.bcs_formato(SaldoDisponibleEgreso,'GEN','Monto',IdMoneda,'','')).valor #</label>",	aggregates: ["sum"], groupFooterTemplate:$translate.instant("UI_TOTAL")+': '+"#=sum#"}
								,{field : "SaldoDisponibleIngreso",	title:'UI_INGRESO',	width : "80px",	template : " <label> #:(util.bcs_formato(SaldoDisponibleIngreso,'GEN','Monto',IdMoneda,'','')).valor #</label>",	aggregates: ["sum"], groupFooterTemplate:$translate.instant("UI_TOTAL")+': '+"#=sum#"}
								,{field : "SaldoDisponible", 		title:"UI_SALDO",	width : "80px",	template : " <label> #:(util.bcs_formato(SaldoDisponible,'GEN','Monto',IdMoneda,'','')).valor #</label>",	aggregates: ["sum"], groupFooterTemplate:$translate.instant("UI_TOTAL")+': '+"#=sum#"}
					 ]}
		
					//Datos saldo pendientes
					,{title   : $translate.instant("UI_SALDO_PENDIENTE")+" "+$translate.instant("UI_AL")+" " +util.formatoDate($scope.SesionFechaSistema), align: "center",
					  columns : [{field : "SaldoPendienteEgreso",	title:'UI_EGRESO',	width : "80px",	template : " <label> #:(util.bcs_formato(SaldoPendienteEgreso,'GEN','Monto',IdMoneda,'','')).valor #</label>",	aggregates: ["sum"], groupFooterTemplate:$translate.instant("UI_TOTAL")+': '+"#=sum#"}
								,{field : "SaldoPendienteIngreso",	title:'UI_INGRESO',	width : "80px",	template : " <label> #:(util.bcs_formato(SaldoPendienteIngreso,'GEN','Monto',IdMoneda,'','')).valor #</label>",	aggregates: ["sum"], groupFooterTemplate:$translate.instant("UI_TOTAL")+': '+"#=sum#"}
								,{field : "SaldoPendiente", 		title:"UI_SALDO",	width : "80px",	template : " <label> #:(util.bcs_formato(SaldoPendiente,'GEN','Monto',IdMoneda,'','')).valor #</label>",	aggregates: ["sum"], groupFooterTemplate:$translate.instant("UI_TOTAL")+': '+"#=sum#"}
					 ]}
		
					//Datos saldo contable
					,{title   : $translate.instant("UI_SALDO_CONTABLE")+" "+$translate.instant("UI_AL")+" " +util.formatoDate($scope.SesionFechaSistema), align: "center",
					  columns : [{field : "SaldoContableEgreso",	title:'UI_EGRESO',	width : "80px",	template : " <label> #:(util.bcs_formato(SaldoContableEgreso,'GEN','Monto',IdMoneda,'','')).valor #</label>",	aggregates: ["sum"], groupFooterTemplate:$translate.instant("UI_TOTAL")+': '+"#=sum#"}
								,{field : "SaldoContableIngreso",	title:'UI_INGRESO',	width : "80px",	template : " <label> #:(util.bcs_formato(SaldoContableIngreso,'GEN','Monto',IdMoneda,'','')).valor #</label>",	aggregates: ["sum"], groupFooterTemplate:$translate.instant("UI_TOTAL")+': '+"#=sum#"}
								,{field : "SaldoContable", 			title:"UI_SALDO",	width : "80px",	template : " <label> #:(util.bcs_formato(SaldoContable,'GEN','Monto',IdMoneda,'','')).valor #</label>",	aggregates: ["sum"], groupFooterTemplate:$translate.instant("UI_TOTAL")+': '+"#=sum#"}
					 ]}
		
					//Datos en T1
					,{title   : "UI_SALDO_T1", align: "center",
					  columns : [{field : "SaldoEgresoT1",			title:'UI_EGRESO',	width : "80px",	template : " <label> #:(util.bcs_formato(SaldoEgresoT1,'GEN','Monto',IdMoneda,'','')).valor #</label>",	aggregates: ["sum"], groupFooterTemplate:$translate.instant("UI_TOTAL")+': '+"#=sum#"}
								,{field : "SaldoIngresoT1",			title:'UI_INGRESO',	width : "80px",	template : " <label> #:(util.bcs_formato(SaldoIngresoT1,'GEN','Monto',IdMoneda,'','')).valor #</label>",	aggregates: ["sum"], groupFooterTemplate:$translate.instant("UI_TOTAL")+': '+"#=sum#"}
								,{field : "SaldoT1", 				title:"UI_SALDO",	width : "80px",	template : " <label> #:(util.bcs_formato(SaldoT1,'GEN','Monto',IdMoneda,'','')).valor #</label>",	aggregates: ["sum"], groupFooterTemplate:$translate.instant("UI_TOTAL")+': '+"#=sum#"}
					 ]}
		
					 //Datos en T2
					,{title   : "UI_SALDO_T2", align: "center",
					  columns : [{field : "SaldoEgresoT2",			title:'UI_EGRESO',	width : "80px",	template : " <label> #:(util.bcs_formato(SaldoEgresoT2,'GEN','Monto',IdMoneda,'','')).valor #</label>",	aggregates: ["sum"], groupFooterTemplate:$translate.instant("UI_TOTAL")+': '+"#=sum#"}
								,{field : "SaldoIngresoT2",			title:'UI_INGRESO',	width : "80px",	template : " <label> #:(util.bcs_formato(SaldoIngresoT2,'GEN','Monto',IdMoneda,'','')).valor #</label>",	aggregates: ["sum"], groupFooterTemplate:$translate.instant("UI_TOTAL")+': '+"#=sum#"}
								,{field : "SaldoT2", 				title:"UI_SALDO",	width : "80px",	template : " <label> #:(util.bcs_formato(SaldoT2,'GEN','Monto',IdMoneda,'','')).valor #</label>",	aggregates: ["sum"], groupFooterTemplate:$translate.instant("UI_TOTAL")+': '+"#=sum#"}									
					 ]}
		
					 //Datos en T3
					,{title   : "UI_SALDO_T3", align: "center",
					  columns : [{field : "SaldoEgresoT3",			title:'UI_EGRESO',	width : "80px",	template : " <label> #:(util.bcs_formato(SaldoEgresoT3,'GEN','Monto',IdMoneda,'','')).valor #</label>",	aggregates: ["sum"], groupFooterTemplate:$translate.instant("UI_TOTAL")+': '+"#=sum#"}
								,{field : "SaldoIngresoT3",			title:'UI_INGRESO',	width : "80px",	template : " <label> #:(util.bcs_formato(SaldoIngresoT3,'GEN','Monto',IdMoneda,'','')).valor #</label>",	aggregates: ["sum"], groupFooterTemplate:$translate.instant("UI_TOTAL")+': '+"#=sum#"}
								,{field : "SaldoT3", 				title:"UI_SALDO",	width : "80px",	template : " <label> #:(util.bcs_formato(SaldoT3,'GEN','Monto',IdMoneda,'','')).valor #</label>",	aggregates: ["sum"], groupFooterTemplate:$translate.instant("UI_TOTAL")+': '+"#=sum#"}
					 ]}
		  ],
		contextMenu : true,
		filterable	: true,
		sortable 	: true,
		pageable 	: false,
		height 		: 520,
		selectable 	: "multiple row",
		//autoBind 	: true
		navigatable	: true,
		allowCopy	: true,
		userSettings: true,
		gridCreated : gridInit		  
	};


	$scope.buscarInfoSaldos = function(){
		filters.p_IdEmpresa 						= $scope.SesionIdEmpresa;
		filters.p_IdUsuario         				= $scope.SesionIdUsuario;
		filters.p_IdModulo							= 'TES';
		filters.p_UsaReglasNegocio					= 'S';
		filters.p_FechaDesde						= util.dateSPdate($scope.filtros.kngFechaSistema) || '0001-01-01';
		filters.p_FechaHasta						= util.dateSPdate($scope.filtros.kngFechaSistema) || '0001-01-01';
		filters.p_CuentasAConsultar					= [{IdCuentaBanco 	: null, 
														 IdBanco 		: $scope.filtros.oConfiguracionBanco.seleccionado.IdBanco || null, 
														 IdMoneda 		: $scope.filtros.oConfiguracionMonedaTesoreria.seleccionado.IdMoneda || null
														}];
		
		filters.p_IncluyeSaldosPendientes = $scope.incluyeSaldosPendientes || 'N';
		filters.p_IncluyeSaldosContables  = $scope.incluyeSaldosContables || 'N';
		filters.p_IncluyeSaldosFuturo     = $scope.incluyeSaldosFuturos || 'N';
		
		
		dsSaldo.setCallParameters(filters);

	};


	$scope.limpiarFiltros = function(){
		$scope.filtros.oConfiguracionBanco.idActual = 0;
		$scope.filtros.oConfiguracionMonedaTesoreria.idActual = 0;
		dsSaldo.removeAll();
	};


	$scope.MostrarCartolaMovimientos = function(filaSeleccionada){
		var datos = [];
		datos	= [{IdCuentaBanco 			: filaSeleccionada[0].IdCuentaBanco,
					NombreBanco				: filaSeleccionada[0].NombreBanco,
					NombreCuentaBanco		: filaSeleccionada[0].NombreCuentaBanco,
					NombreMoneda			: filaSeleccionada[0].NombreMoneda,
					NombreEmpresa			: filaSeleccionada[0].NombreEmpresa,
					SaldoDisponibleInicial	: filaSeleccionada[0].SaldoDisponibleInicial,
					SaldoDisponible			: filaSeleccionada[0].SaldoDisponible,
					IdMoneda				: filaSeleccionada[0].IdMoneda,					
					FechaDesde 				: $scope.filtros.kngFechaSistema, 
					FechaHasta 				: util.date($scope.filtros.kngFechaSistema).add(3,'days').toDate()
				   }];

		var idModal = bcsComunes.ventanaAbrir("CartolaMovimientosTesoreria", datos,false,false,$scope.winID);
		promesa = bcsComunes.ventanaPromesa(idModal);
	};
	
	
	
	$scope.libretaBanco = function(filaSeleccionada){
		var datos = [];
		datos	  = [{
						IdCuentaBanco 	: filaSeleccionada[0].IdCuentaBanco,
						IdMoneda		: filaSeleccionada[0].IdMoneda,
						IdBanco			: filaSeleccionada[0].IdBanco,
						FechaConsulta   : util.dateInicioDia($scope.SesionFechaSistema)
				   }];

		var idModal = bcsComunes.ventanaAbrir("LibretaBanco", datos, false,false,$scope.winID);
		promesa = bcsComunes.ventanaPromesa(idModal);
	};

	
	$scope.consultarDocumentos = function(filaSeleccionada){
		//Si cta = -1(SIN DEFINIR) es saldo de mov. sin forma de pago
		if (filaSeleccionada[0].IdCuentaBanco !== -1)    
		{
			var datos = [];
			datos	= [{IdCuentaBanco 			: filaSeleccionada[0].IdCuentaBanco,
						FechaDesde 				: $scope.filtros.kngFechaSistema,
					   }];
	  		
			var idModal = bcsComunes.ventanaAbrir("DocumentosTesoreria", { datos }, false,false,$scope.winID);
			promesa = bcsComunes.ventanaPromesa(idModal);
		}
		else
		{
			bcsComunes.msg("warning","warning",$translate.instant('UI_SIN_FORMAS_DE_PAGO_DEFINIDAS'));
		}
	};
	
	

	
	//CONSULTA PARAMETROS TESORERIA "CONSULTA SALDO BANCO INCLUYE SALDOS FUTUROS (T1,T2,T3)"
	if ($scope.SesionIdEmpresa)
	{	
		//Filtros para parámetros tesorería
		$scope.filtroEmpresa		 = { "field"   : "IdEmpresa", "operator": "eq", "value" : $scope.SesionIdEmpresa  };
		
		$scope.filtroCodigoParametro = [{ field: "CodigoParametro", operator: "eq", value: 'CONSULTASALDOBANCOINCLUYESALDOSFUTUROS' },
		                    		    { field: "CodigoParametro", operator: "eq", value: 'CONSULTASALDOBANCOINCLUYESALDOSPENDIENTE' },
		                    		    { field: "CodigoParametro", operator: "eq", value: 'CONSULTASALDOBANCOINCLUYESALDOSCONTABLES' }];
		
		
			var dsParametrosTesoreria = new kendo.data.DataSource({
				type: "odata",
				serverFiltering:true,
	    		transport: {
	    			read: {
	    				url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1037",
	    				dataType: "json"
			    	}
				},
				
				filter : {
					complejo : [$scope.filtroEmpresa, $scope.filtroCodigoParametro],
				  	logic: "and",
				  	filters: [
					{
					    logic: "or",
					    filters: $scope.filtroCodigoParametro
					},
					$scope.filtroEmpresa				
				  ]},				
			});
	 
			dsParametrosTesoreria.fetch().then(function()
			{
				 if (dsParametrosTesoreria.data().length < 0 ) 
				 {   //Si no existe parámetro
					 $scope.incluyeSaldosFuturos    = "N";					 
					 $scope.incluyeSaldosPendientes = "N";
					 $scope.incluyeSaldosContables  = "N";
				 }
				 else 
				 {
					 $scope.$apply(function() 
					 { 
						 $.each(dsParametrosTesoreria.data(), function(key, registro){
								//console.log(k+" -> " + v.Valor);
								
								if (registro.CodigoParametro === "CONSULTASALDOBANCOINCLUYESALDOSFUTUROS")
								{	
									 $scope.incluyeSaldosFuturos    = registro.Valor;
								}
								
								if (registro.CodigoParametro === "CONSULTASALDOBANCOINCLUYESALDOSPENDIENTE")
								{	 					 
									 $scope.incluyeSaldosPendientes = registro.Valor;
								}
								
								if (registro.CodigoParametro === "CONSULTASALDOBANCOINCLUYESALDOSCONTABLES")
								{	 
									 $scope.incluyeSaldosContables  = registro.Valor;									
								}								
								
						 });
						 
					 });
				 }
					
				 
				 $timeout(function(){
					 
					 if ($scope.incluyeSaldosPendientes === "N")
					 {
						 $scope.ApiGridInfoSaldos.hideColumn("SaldoPendienteEgreso");
						 $scope.ApiGridInfoSaldos.hideColumn("SaldoPendienteIngreso");
						 $scope.ApiGridInfoSaldos.hideColumn("SaldoPendiente");
					 }
					 
					 if ($scope.incluyeSaldosContables === "N")
					 {
						 $scope.ApiGridInfoSaldos.hideColumn("SaldoContableEgreso");
						 $scope.ApiGridInfoSaldos.hideColumn("SaldoContableIngreso");
						 $scope.ApiGridInfoSaldos.hideColumn("SaldoContable");
					 }
					 
					 if ($scope.incluyeSaldosFuturos === "N")
					 { 
						 $scope.ApiGridInfoSaldos.hideColumn("SaldoEgresoT1");
						 $scope.ApiGridInfoSaldos.hideColumn("SaldoIngresoT1");
						 $scope.ApiGridInfoSaldos.hideColumn("SaldoT1");
							
						 $scope.ApiGridInfoSaldos.hideColumn("SaldoEgresoT2");
						 $scope.ApiGridInfoSaldos.hideColumn("SaldoIngresoT2");
						 $scope.ApiGridInfoSaldos.hideColumn("SaldoT2");
							
						 $scope.ApiGridInfoSaldos.hideColumn("SaldoEgresoT3");
						 $scope.ApiGridInfoSaldos.hideColumn("SaldoIngresoT3");
						 $scope.ApiGridInfoSaldos.hideColumn("SaldoT3");							 
					 }
				 },1000);

				return true;
		    });		
	};

	$scope.exportaExcelPdf = function(){

		$scope.seleccionSaldoBanco= false;
	    $scope.exportarExcel();
	    $scope.exportarPDF();
	}

	var firstHeight = $('#Win_ConsultaSaldoBanco');
	firstHeight.css("height", "707px"); 
	
});