angular.module('ModuloTesoreria').controller('ConsultarDepositosCtrl',
			function($scope,$translate,$timeout,bcsComunes,$translate,bcsAlerta,OPCIONES_BCSALERTA,
					bcsServiciosTesoreria, bcsBlotterDepositosGrid)
{
	$scope.data ={};
	
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	$scope.btnActivo = true;
	
	$scope.data.kngFechaDeposito = util.dateInicioDia($scope.SesionFechaSistema);
	
	$scope.data.kngFechaHoy = util.dateInicioDia($scope.SesionFechaSistema);
	
	// --------------------------------------------------
	// CONFIGURACION DE COMBOS
	// --------------------------------------------------
	$scope.data.oConfiguracionCuentaBancoCorredora={
			tipo : "combobox-cuentas-bancarias-corredora",
			configuracion : {},
			filtros : {
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.data.oConfiguracionMoneda = {
			tipo : "combobox-moneda-tesoreria",
			configuracion : {},
			filtros : {},
			idActual : false,
			seleccionado : false,
			idDefecto : ''			
	};

	
	
	// --------------------------------------------------
	// CONFIGURACION DE GRILLAS
	// --------------------------------------------------
	var dsDepositos = new bcs.DataSource(bcsBlotterDepositosGrid.dsDepositos);
	$scope.confBasicaDepositos = {
		dataSource			: dsDepositos,
		columns				: bcsBlotterDepositosGrid.columnasDepositos(),		
		sortable			: true,
		pageable			: false,
		autoBind			: false,
		filterable			: true,
		height				: 530,
		selectable 			: "multiple row",	
		rowColorBackground	: function(row) {
								if (row.IdEstado == 'ANU')
									return "red";
								return "";
							  },
		userSettings		: true							  
	};	

	
	
	$scope.buscarDepositos = function(){
		var filters = new Array();
		if($scope.data.oConfiguracionCuentaBancoCorredora.seleccionado)
			filters.push({field: "IdCuentaBanco", operator: "eq", value: Number($scope.data.oConfiguracionCuentaBancoCorredora.seleccionado.IdCuentaBanco) })
		if($scope.data.oConfiguracionMoneda.seleccionado)
			filters.push({field: "IdMoneda", operator: "eq", value: $scope.data.oConfiguracionMoneda.seleccionado.IdMoneda })
		if($scope.data.kngFechaDeposito)
			filters.push({field: "FechaMovimiento", operator: "gte", value: util.dateInicioDia($scope.data.kngFechaDeposito)})
		if($scope.data.kngFechaDeposito)
			filters.push({field: "FechaMovimiento", operator: "lte", value: util.dateTerminoDia($scope.data.kngFechaDeposito)})
		if($scope.data.FolioComprobante)
			filters.push({field: "FechaMovimiento", operator: "eq", value: $scope.data.FolioComprobante})
						
		dsDepositos.filter(filters);
	};
	
	$scope.limpiarFiltros = function() {
		$scope.data.oConfiguracionCuentaBancoCorredora.idActual = 0;
		$scope.data.oConfiguracionMoneda.idActual = 0;		
		$scope.data.kngFechaDeposito = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.FechaDeposito = '';
		$scope.data.FolioComprobante = '';
		
		dsDepositos.removeAll();
	};
	
	
	$scope.detalleNominaDeposito = function(row){
		var ventama = bcsComunes.ventanaAbrir("DetalleNominaDeposito", row, false, false, $scope.winID );

		var promesa = bcsComunes.ventanaPromesa(ventama);
		promesa.then(function(data){
			$scope.buscarDepositos();
		});
	};


	// --------------------------------------------------
	// SERVICIO CERTIFICAR DEPOSITOS
	// --------------------------------------------------
	$scope.GestionDeDeposito = function(TipoEjecucion, MotivoAnulacion){
		var parametros = {};
		var documentos = [];
		$.each($scope.seleccionDepositos, function(index, value){
			var ObjCus = {
				"Id"			: value.IdDeposito || 0,
				"Entidad"		: 'TES.Deposito',
				"IdConcepto"	: MotivoAnulacion.length > 0 ? MotivoAnulacion[0].IdConceptoAnulacion : '',
				"Descripcion"	: MotivoAnulacion.length > 0 ? MotivoAnulacion[0].Descripcion : ''
			};
			documentos.push(ObjCus);
		});


		parametros.p_IdEmpresa   		= $scope.SesionIdEmpresa;
		parametros.p_IdUsuario   		= $scope.SesionIdUsuario;
		parametros.p_IdModulo 			= 'TES';
		parametros.p_simularOperacion 	= 'N';
		parametros.p_UsaReglasNegocio   = 'S';
		parametros.p_FechaMovimiento	= util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
		parametros.p_TipoEjecucion		= TipoEjecucion;
		parametros.p_Registros			= documentos;
		
		bcsComunes.ventanaOverlay($scope.winID, true);
		bcsServiciosTesoreria.GestionDeDeposito(
			parametros,
			function(respuesta){
				bcsComunes.ventanaOverlay($scope.winID, false);

				if (respuesta.P_OUT_ERRORESCONTADOR === 0){
					bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);

					$scope.buscarDepositos();
				} else {	
					bcsComunes.ventanaMsg({
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
	};
	

	$scope.EnviarADeposito = function(){
		var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_ENVIARA_NOMINAS_A_DEPOSITO'),$scope.winID);
		t.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR){
				$scope.GestionDeDeposito('EDP',[]);
			}
		});
		return;
	}

	$scope.LiquidarDepositos = function(){
		var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_CERTIFICAR_DEPOSITO_Y_LIQUIDAR'),$scope.winID);
		t.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR){
				
				var dt = `${$scope.data.kngFechaHoy.getFullYear().toString().padStart(4, '0')}-${($scope.data.kngFechaHoy.getMonth()+1).toString().padStart(2, '0')}-${$scope.data.kngFechaHoy.getDate().toString().padStart(2, '0')}`;
				
				if (util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema)) == dt && $scope.seleccionDepositos[0].IdTipoDeposito == 'OTBNCO'){
					var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_PREGUNTA_FECHA_LIQUIDACION_DEPOSITO_OTRO_BANCO'),$scope.winID);
					t.then(function(result){
						if (result === OPCIONES_BCSALERTA.ACEPTAR){
							$scope.GestionDeDeposito('LIQ',[]);
						}
					});
				}
				
				else
				{
					$scope.GestionDeDeposito('LIQ',[]);
				}
			}
		});
		return;
	};

	$scope.AnularDepositos = function(){
		var Parametros = {
			"NombreEntidad"			: 'TES.Deposito',
			"TipoConceptoTesoreria"	: 'ANU'
		};
		
		var ventanaMotivoAnulacion = bcsComunes.ventanaAbrir("MotivoAnulacion", Parametros, false, false, $scope.winID);
		var promesa = bcsComunes.ventanaPromesa(ventanaMotivoAnulacion);
		promesa.then(function(data){
			if (data.length > 0){
				$scope.GestionDeDeposito('ANU', data);
			}
		});
	};

	var firstWindow = $('#Win_BlotterDepositos')
			firstWindow.css("width", "960px");
			firstWindow.css("height","716px")

});