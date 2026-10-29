angular.module('ModuloTesoreria').controller('CartolaMovimientosTesoreriaCtrl',
		function($scope,$timeout,$modal,$state,bcsComunes,$translate,bcsServiciosTesoreria)
{	
	$scope.filtros = {};	
	var bCartolaMov = this;

	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	var idVentana = bcsComunes.ventanaWinId($scope);
	var parametros = bcsComunes.ventanaParams(idVentana);
		

	//SETEO INICIAL
	bCartolaMov.NombreBanco					= parametros[0].NombreBanco;
	bCartolaMov.NombreCuentaBanco			= parametros[0].NombreCuentaBanco;
	bCartolaMov.NombreEmpresa				= parametros[0].NombreEmpresa;
	bCartolaMov.NombreMoneda				= parametros[0].NombreMoneda;
	bCartolaMov.SaldoDisponibleInicial		= parametros[0].SaldoDisponibleInicial;	
	bCartolaMov.SaldoDisponible				= parametros[0].SaldoDisponible;
	bCartolaMov.IdMoneda					= parametros[0].IdMoneda;
	
	$scope.filtros.kngFechaDesde 			= parametros[0].FechaDesde;
	$scope.filtros.kngFechaHasta 			= parametros[0].FechaHasta;
	$scope.filtros.SaldoPorFechaDisponible 	= true;
	$scope.filtros.SaldoPorFechaContable 	= false;


	$scope.filtros.oConfiguracionSocioNegocio={
		tipo : "combobox-socio-negocio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa }
		},
	};
	$scope.filtros.oConfiguracionPortafolio={
		tipo : "combobox-portafolio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdCliente : { "field": "IdCliente","operator": "eq", "value":  0 }
		},
	};
	$scope.filtros.oConfiguracionConcepto={
		tipo : "combobox-concepto-tesoreria",
		configuracion : {},
		filtros : {
			IdEstado : { "field": "IdEstado", "operator": "eq", "value": "VIG" },
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''			
	};
	$scope.oConfiguracionMedioDePago={
		tipo : "combobox-medio-pago",
		configuracion : {},
		filtros : {
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};

	
	var dsCartola = new bcs.DataSource({
		type : "odata",
		definition : {
			transport : {
				read : {
					url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1024",
					dataType : "json"
				}
			},		
			serverPaging 	: true,
			serverSorting 	: true,
			serverFiltering : true,
			sort 			: [ { field: "FechaAfectaSaldo", dir: "desc" },
								{ field: "FechaHoraLiquidacion", dir: "asc" },
								{ field: "IdComprobanteFormaDePago", dir: "asc" }
							  ], // Criterios de Ordenamiento.

			schema : {
				model : {
					fields : {										
						NombreTipoMovimiento	: { type : "string" },
						NombreConcepto			: { type : "string" },
						NombreMedioDePago		: { type : "string" },
						MontoIngreso			: { type : "number" },
						MontoEgreso				: { type : "number" },
						SaldoDisponible			: { type : "number" },
						SaldoContable			: { type : "number" },						
						NombreSocio				: { type : "string" },
						DocumentoIdentidadSocio	: { type : "string" },
						NombreEstado			: { type : "string" },
						IdMoneda				: { type : "string" },
						IdComprobanteFormaDePago: { type : "number" },
						IdEstado				: { type : "string" },
						IdComprobante 			: { type : "number" },

						FechaMovimiento			: { type : "sysdate" },
						FechaHoraMovimiento		: { type : "sysdatetime" },
						FechaAfectaSaldo		: { type : "sysdate" },
						FechaHoraAfectaSaldo	: { type : "sysdatetime" },
						FechaLiquidacion		: { type : "sysdate" },
						FechaHoraLiquidacion	: { type : "sysdatetime" }
					}
				}
			}
		}
	});
	var gridCartolaMovimiento;
	var gridInit = function(grid) {
		$scope.buscarMovimientos();
	}
	$scope.confBasicaCartolaMovimiento = {
		dataSource  : dsCartola,
		columns 	: [	 {field : "FechaAfectaSaldo", 		title:"UI_FECHA_DISPONIBLE", 		width: "70px"}
						,{field : "FechaLiquidacion", 		title:"UI_FECHA_LIQUIDACION", 		width: "70px"}
						,{field : "NombreMedioDePago", 		title:"UI_MEDIO_DE_PAGO", 			width: "120px"}
						,{field : "NombreConcepto", 		title:"UI_CONCEPTO", 				width: "250px"}
						,{field : "MontoEgreso", 			title:"UI_EGRESO", 					width: "80px",	template : "<label> #:(util.bcs_formato(MontoEgreso,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
						,{field : "MontoIngreso", 			title:"UI_INGRESO", 				width: "80px",	template : "<label> #:(util.bcs_formato(MontoIngreso,'GEN','Monto',IdMoneda,'','')).valor #</label>"}						
						,{field : "SaldoDisponible", 		title:"UI_SALDO_DISPONIBLE",		width: "80px",	template : "<label> #:(util.bcs_formato(SaldoDisponible,'GEN','Monto',IdMoneda,'','')).valor #</label>", hidden: true}
						,{field : "SaldoContable", 			title:"UI_SALDO_CONTABLE",			width: "80px",	template : "<label> #:(util.bcs_formato(SaldoContable,'GEN','Monto',IdMoneda,'','')).valor #</label>", 	hidden: true}
						,{field : "NombreSocio", 			title:"UI_SOCIO_DE_NEGOCIO", 		width: "200px"}
						,{field : "DocumentoIdentidadSocio",title:"UI_DOCUMENTO_IDENTIDAD", 	width: "80px"}						
						,{field : "NombreEstado", 			title:"UI_ESTADO", 					width: "80px"}
						,{field : "IdComprobante", 			title:"UI_SECUENCIA_COMPROBANTE", 	width: "70px",	template: "<label> #:  util.formateaNumero(IdComprobante,0) #</label>" }
					  ],
		contextMenu : true,
		filterable	: true,
		sortable 	: true,
		pageable 	: false,
		userSettings: true,
		height 		: 400,
		selectable 	: "multiple row",	
		gridCreated : gridInit,
		gridCreated : function(g) { gridCartolaMovimiento = g; }
	};

	$scope.$watch('filtros.SaldoPorFechaDisponible',function(newval,oldval){
		switch (newval){
			case true:  
					$scope.filtros.SaldoPorFechaContable = false;
					$scope.filtros.kngFechaHasta = parametros[0].FechaDesde;
				break;
			case false: 
					$scope.filtros.SaldoPorFechaContable = true; 
					$scope.filtros.kngFechaHasta = parametros[0].FechaHasta;
				break;
		}			
	});	
	$scope.$watch('filtros.SaldoPorFechaContable',function(newval,oldval){
		switch (newval){
			case true:  
					$scope.filtros.SaldoPorFechaDisponible = false;
					$scope.filtros.kngFechaHasta = parametros[0].FechaHasta;
				break;
			case false: 
					$scope.filtros.SaldoPorFechaDisponible = true;
					$scope.filtros.kngFechaHasta = parametros[0].FechaDesde;  
				break;
		}
	});
	

	$scope.buscarMovimientos = function(filtros){
		var filters = new Array();

		if (filtros.SaldoPorFechaDisponible){
			gridCartolaMovimiento.showColumn("SaldoDisponible");
			gridCartolaMovimiento.hideColumn("SaldoContable");
		}			
		else{
			gridCartolaMovimiento.showColumn("SaldoContable");
			gridCartolaMovimiento.hideColumn("SaldoDisponible");			
		}
		
		filters.push({ field: "IdCuentaBancariaCorredora", operator: "eq", value: parametros[0].IdCuentaBanco });
		if ($scope.filtros.SaldoPorFechaDisponible){
			filters.push({ field: "FechaAfectaSaldo", operator: "gte", value: util.dateInicioDia(parametros[0].FechaDesde) });
			filters.push({ field: "FechaAfectaSaldo", operator: "lte", value: util.dateInicioDia(parametros[0].FechaDesde) });
		}

		if (filtros != undefined){
			var filters = new Array();
			filters.push({ field: "IdCuentaBancariaCorredora", operator: "eq", value: parametros[0].IdCuentaBanco });

			if (filtros.oConfiguracionSocioNegocio.seleccionado.IdPersona)
				filters.push({ field: "IdPersonaRelacionadoSocio", operator: "eq", value: Number(filtros.oConfiguracionSocioNegocio.seleccionado.IdPersona) });
			if (filtros.oConfiguracionPortafolio.seleccionado.IdPortafolio)
				filters.push({ field: "IdPortafolioRelacionadoSocio", operator: "eq", value: Number(filtros.oConfiguracionPortafolio.seleccionado.IdPortafolio) });
			if (filtros.oConfiguracionConcepto.seleccionado.IdConcepto && filtros.oConfiguracionConcepto.seleccionado.IdConcepto != 0)
				filters.push({ field: "IdConcepto", operator: "eq", value: filtros.oConfiguracionConcepto.seleccionado.IdConcepto });
			if (filtros.oConfiguracionMedioDePago.seleccionado.IdMedioDePago)
				filters.push({ field: "IdMedioDePago", operator: "eq", value: filtros.oConfiguracionMedioDePago.seleccionado.IdMedioDePago });
			
			if ($scope.filtros.SaldoPorFechaDisponible){
				filters.push({ field: "FechaAfectaSaldo", operator: "gte", value: filtros.kngFechaDesde });
				filters.push({ field: "FechaAfectaSaldo", operator: "lte", value: filtros.kngFechaHasta });
			}
			if ($scope.filtros.SaldoPorFechaContable){
				filters.push({ field: "FechaLiquidacion", operator: "gte", value: filtros.kngFechaDesde });
				filters.push({ field: "FechaLiquidacion", operator: "lte", value: filtros.kngFechaHasta });
			}
			
		}		
		dsCartola.filters(filters);
		/*dsCartola.sort(
			[{ field: "FechaHoraLiquidacion", dir: "asc" },
			 { field: "IdComprobanteFormaDePago", dir: "asc" }]
		);*/
	};

	$scope.limpiarFiltros = function(){
		$scope.filtros.oConfiguracionSocioNegocio.idActual 	= false;
		$scope.filtros.oConfiguracionPortafolio.idActual 	= 0;
		$scope.filtros.oConfiguracionConcepto.idActual 		= 0;
		$scope.filtros.oConfiguracionMedioDePago.idActual 	= 0;

		$scope.filtros.SaldoPorFechaDisponible 	= true;
		$scope.buscarMovimientos();
	};
});