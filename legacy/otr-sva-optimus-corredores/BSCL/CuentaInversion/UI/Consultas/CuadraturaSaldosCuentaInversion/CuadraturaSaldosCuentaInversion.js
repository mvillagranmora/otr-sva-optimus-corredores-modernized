angular.module('ModuloCuentaInversion').controller('CuadraturaSaldosCuentaInversionCtrl',
				function($scope,$translate,$timeout,bcsComunes,$translate,
						bcsServiciosCuentaInversion,bcsServicioPlataforma)
{

	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);

	$scope.filtros = {};
	$scope.grid = {};

	$scope.filtros.kngFechaEfectivaDesde = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.filtros.kngFechaEfectivaHasta = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.filtros.SaldoPorFechaEfectiva = true;
	
	$scope.filtros.oConfiguracionCuentaInversion={
			tipo : "combobox-cuenta-inversion",
			configuracion : {},
			filtros : {
				IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   : 0 },
				IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   : 0 },
				EstadoCuentaInversion : { "field"   : "IdEstadoCuentaInversion","operator": "eq", "value"   : "ACT" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''			
	};
		
	$scope.$watch('filtros.SaldoPorFechaMovimiento',function(newval,oldval){
		switch (newval){
			case true:  $scope.filtros.SaldoPorFechaEfectiva = false; break;					
			case false: $scope.filtros.SaldoPorFechaEfectiva = true;  break; 
		}
		dsConsultaSaldo.removeAll();		
	});	
	$scope.$watch('filtros.SaldoPorFechaEfectiva',function(newval,oldval){
		switch (newval){
			case true:  $scope.filtros.SaldoPorFechaMovimiento = false; break;					
			case false: $scope.filtros.SaldoPorFechaMovimiento = true;  break;  
		}
		dsConsultaSaldo.removeAll();
	});	

	var dsConsultaSaldo = new bcs.DataSource({
		type : "call",
		definition : {
			serverFiltering 	: false, // Filtrado de datos en el servidor.
			serverPaging 		: false, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.
			pageSize			: 100,
			callParameters      : [],
			callTransaction 	: "1770",
			callResultSet 		: "P_OUT_SALDOINICIALFINAL",
			schema : {
				model : {
					fields : {										
						IdCuentaInversion	: {type : "number"},
						IdCliente			: {type : "number"},
						IdPortafolio		: {type : "number"},
						IdMoneda			: {type : "string"},
						SaldoInicial 		: {type : "number"},
						SaldoFinalReal		: {type : "number"},
						SaldoFinalCalculado : {type : "number"},
						Diferencia			: {type : "number"}
					}
				}
			}
		}
	});
	$scope.grid.confCuadraturaSaldosCuentaInversion = {
		dataSource  : dsConsultaSaldo,
		columns 	: [	 
						 {field : "IdMoneda",	title:"UI_CUENTA_INVERSION",		width: "50px"}
						,{title : "UI_SALDO", align: "center",
							columns : [
										 {field : "SaldoInicial",  		       title:'UI_INICIAL',	width : "100px", template : " <label> #:  (util.bcs_formato(SaldoInicial,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
										,{field : "SaldoFinalReal", title:'UI_FINAL', 	width : "100px", template : " <label> #:  (util.bcs_formato(SaldoFinalReal,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
									  ]}
						,{title : "UI_SALDO_RECALCULADO", align: "center",
							columns : [
										 {field : "SaldoInicial",  		       title:'UI_INICIAL',	width : "100px", template : " <label> #:  (util.bcs_formato(SaldoInicial,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
										,{field : "SaldoFinalCalculado", title:'UI_FINAL', 	width : "100px", template : " <label> #:  (util.bcs_formato(SaldoFinalCalculado,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
									  ]}
						,{field : "Diferencia",	title:"UI_DIFERENCIA",	width: "50px"}								  
					  ],
		contextMenu : true,
		filterable	: true,
		sortable 	: true,
		pageable 	: true,
		height 		: 250,
		selectable 	: "row",
		autoBind 	: false,
		userSettings: true,
		navigatable	: true,
	};


	$scope.buscar = function() {
		//Datos saldo inicial
		var filters = {};
		filters.p_IdEmpresa 				= $scope.SesionIdEmpresa;
		filters.p_IdUsuario         		= $scope.SesionIdUsuario;
		filters.p_IdModulo					= 'CTA';
		filters.p_UsaReglasNegocio			= 'S';
		filters.p_FechaDesde				= util.dateSPdate($scope.filtros.kngFechaEfectivaDesde) || '0001-01-01';
		filters.p_FechaHasta				= util.dateSPdate($scope.filtros.kngFechaEfectivaHasta) || '0001-01-01';
		filters.p_CuentaInversionAConsultar = [{IdPortafolio : ($scope.filtros.oConfiguracionPortafolio.seleccionado.IdPortafolio || null),
												IdMoneda	 : ($scope.filtros.oConfiguracionCuentaInversion.seleccionado.IdMoneda || null)
											  }];
		filters.p_ConsultaSaldoEfectivo		= $scope.filtros.SaldoPorFechaEfectiva ? 'S' : ($scope.filtros.SaldoPorFechaMovimiento ? 'S' : 'N');
		dsConsultaSaldo.setCallParameters(filters);
	};
	
	$scope.limpiarFiltros = function()
	{	
		//$scope.filtros.oConfiguracionSocioNegocio.idActual = false;
		$scope.filtros.oConfiguracionPortafolio.idActual = false;
		$scope.filtros.oConfiguracionCuentaInversion.idActual = 0;
		
		$scope.filtros.kngFechaEfectivaDesde = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtros.kngFechaEfectivaHasta = util.dateInicioDia($scope.SesionFechaSistema);

		$scope.grid.seleccionCuadraturaSaldosCuentaInversion = undefined;
		dsConsultaSaldo.removeAll();
	};

	$scope.EjecutarCuadratura = function()
	{
		if ($scope.grid.seleccionCuadraturaSaldosCuentaInversion === undefined){
			bcsComunes.msg("error","error",$translate.instant('UI_DEBE_SELECCIONAR_UNA_CUENTA_INVERSION'));
			return;
		}

		//VARIABLES DEL SERVICIO
		var datos = {};
		datos.p_IdEmpresa			= $scope.SesionIdEmpresa;
		datos.p_IdUsuario			= $scope.SesionIdUsuario;
		datos.p_IdModulo			= 'CTA';
		datos.p_UsaReglasNegocio	= 'S';
		datos.p_FechaSistema		= util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
		datos.p_CuentasACuadrar		= [{IdPortafolio : ($scope.grid.seleccionCuadraturaSaldosCuentaInversion.IdPortafolio || null),
										IdMoneda	 : ($scope.grid.seleccionCuadraturaSaldosCuentaInversion.IdMoneda || null)
									  }];
		
		bcsComunes.ventanaOverlay($scope.winID, true);
       	bcsServiciosCuentaInversion.EjecutarCuadraturaSaldos(
       		datos,
       		function(respuesta){				
				bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
				$scope.buscar();
				bcsComunes.ventanaOverlay($scope.winID, false);
			},
			function(respuesta){
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_CUADRAR_SALDOS"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
				bcsComunes.ventanaOverlay($scope.winID, false);
			}
       	); 


	};
	
});
