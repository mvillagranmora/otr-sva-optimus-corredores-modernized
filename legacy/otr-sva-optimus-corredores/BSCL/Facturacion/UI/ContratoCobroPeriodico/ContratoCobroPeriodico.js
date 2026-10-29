angular.module('ModuloFacturacion').controller('ContratoCobroPeriodicoCtrl',
		function($scope,$translate,$modal,$rootScope, 
		bcsComunes,bcsServiciosFacturacion,bcsServicioPlataforma, bcsAlerta, OPCIONES_BCSALERTA,bcsContratoCobroPeriodicoGrid, bcsOperacionesComunes)

{
	$scope.data	= {};
	var idVentana = bcsComunes.ventanaWinId ($scope);
	bcsComunes.ventanaAutoOverlay(idVentana,false,3000);
	
	//Seteo de variables de sistema
	$scope.data.kngFechaProceso =  util.dateInicioDia($scope.SesionFechaSistema);
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdModulo	= 'FAC';
	$scope.data.btnActivos  = true;   //Activa botones
	
	$scope.data.FechaConsultaOpt = {format: 'MMMM yyyy', start: 'year',depth: 'year'};

	var parametros = bcsComunes.ventanaParams(idVentana);
	parametros.IdClasificacionTipoInstrumento = "";

	//COMBOBOX
	$scope.data.oClientePortafolio={
			tipo : "combobox-cliente-portafolio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				ValorContratoCobroPeriodico : {"field": "ValorContratoCobroPeriodico","operator": "neq", "value" : 1}
			},
	};

	
	$scope.data.oConfiguracionClasificacion={
			tipo : "combobox-clasificacion-tipo-instrumento",
			configuracion : {},
			idActual : false,
			seleccionado : false,
			idDefecto : '',
			idPostFiltro : false
	};	

	$scope.data.oConfiguracionTipoInstrumento={
			tipo : "combobox-tipo-instrumento",
			configuracion : {},
			filtros : {
				IdClasificacionTipoInstrumento 	: { "field" : "IdClasificacionTipoInstrumento", "operator": "eq", "value" : parametros.IdClasificacionTipoInstrumento },
				IdEstado 						: { "field" : "IdEstado",                       "operator": "eq", "value" : "VIG" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : '',
			idPostFiltro : false
	};

	// obtenemos valor del IVA
	var datosLlamadaA = {
		p_IdEmpresa 	: $scope.SesionIdEmpresa,
		p_IdUsuario 	: $scope.SesionIdUsuario,
		p_IdModulo 		: 'CUS',
		p_IdCobro		: 'IVA'
	};

	bcsServiciosFacturacion.ConsultarIVA(datosLlamadaA,function(result) {
		if (result.P_OUT_VALORCOBRO > 0){
			$scope.data.oIVA = result.P_OUT_VALORCOBRO;
		} 
	});
		
	
	//GRILLA 
	$scope.confBasicaClienteContCobros = { 
		dataSource			: bcsContratoCobroPeriodicoGrid.dataSourceContratoCobroPeriodico,
		columns				: bcsContratoCobroPeriodicoGrid.columnasContratoCobroPeriodico(),
		rowColorBackground 	: bcsContratoCobroPeriodicoGrid.fnRowColor,
		sortable			: true,
		pageable			: true,
		filterable			: true,
		userSettings        : true,
		height				: 350,
		selectable 			: false,
		excel 				: { allPages : true },
		navigatable         : true,
		allowCopy 			: true		
	};		

	//OPCIONES
	$scope.buscar = function(){
		var filters = new Array();
		
		if (($scope.data.oClientePortafolio.seleccionado.IdCliente))	
			filters.push({ field: "IdCliente", operator: "eq", value: Number($scope.data.oClientePortafolio.seleccionado.IdCliente)});			
		
		if (($scope.data.oClientePortafolio.seleccionado.IdPortafolio))	
			filters.push({ field: "IdPortafolio", operator: "eq", value: Number($scope.data.oClientePortafolio.seleccionado.IdPortafolio)});
		
		if ($scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento)
			filters.push( { "field": "IdTipoInstrumento","operator": "eq", "value" : $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento});
		
		if ($scope.data.oConfiguracionClasificacion.seleccionado.IdClasificacionTipoInstrumento)
	        filters.push( { "field": "IdClasificacionTipoInstrumento","operator": "eq", "value" : $scope.data.oConfiguracionClasificacion.seleccionado.IdClasificacionTipoInstrumento});		


	    auxDatePerCont =  util.dateOdata(util.date($scope.data.kngFechaProceso).hours(0).minutes(0).seconds(0).toDate());
		primerDia = new Date(auxDatePerCont.getFullYear(), auxDatePerCont.getMonth(), 1);
	    ultimoDia = new Date(auxDatePerCont.getFullYear(), auxDatePerCont.getMonth() + 1, 0);
	
		if (primerDia)
			filters.push( { "field": "FechaComprobante","operator": "gte", "value" : util.date(primerDia).hours(0).minutes(0).seconds(0).toDate() });
						
		if (ultimoDia)
			filters.push( { "field": "FechaComprobante","operator": "lte", "value" : util.date(ultimoDia).hours(0).minutes(0).seconds(0).toDate() });

		$scope.gridApiClienteContCobros.dataSource.filter(filters);	
		$scope.data.deseleccionarTodosMovimientos();
	};
	
		
	$scope.limpiarFiltros = function()
	{	
		$scope.filtros.IdPortafolio = false;			
		$scope.data.oConfiguracionClasificacion.idActual = 0;
		$scope.data.oConfiguracionTipoInstrumento.idActual = 0;
		$scope.gridApiClienteContCobros.dataSource.removeAll();	
		$scope.data.kngFechaProceso = util.dateInicioDia($scope.SesionFechaSistema);
	};
	
	
	$scope.data.crearContratoCobro = function (datos) {
		$scope.gridApiClienteContCobros.dataSource.read().then(function () {
			generarCobro(datos);
		});
	}

	function generarCobro(datos){
		if ($scope.seleccionClienteContCobros === undefined || $scope.seleccionClienteContCobros.length == 0)
		{
			var data = $scope.gridApiClienteContCobros.dataSource.data();
			datos = [];
			$.each(data,function(key, value) {				
				if (value.Estado === 'NO FACTURADA' || value.Estado === 'PENDIENTE') {
					datos.push(value);
				}					
			});
			
			data = datos;
			if(data.length === 0)
			{
				$scope.data.btnActivos   = true;     //Activa botones
				bcsComunes.msg("warning","warning",$translate.instant('UI_COMPROBANTES_SELECCIONADOS_FACTURADOS'));
				return;
			}					
			
		}
		else
		{
			// validacion de registros facturados			
			var data = $scope.seleccionClienteContCobros;			
		}
		var facturar = true;
		var pendientes = [];
		$.each(data,function(key, value) {			
			if (value.Estado === 'FACTURADA') {
				bcsComunes.msg("warning","warning",$translate.instant('UI_COMPROBANTES_SELECCIONADOS_FACTURADOS'));
				facturar = false;					
				return;
			}
			if (value.Estado === 'NO DISPONIBLE') {
				bcsComunes.msg("warning","warning",$translate.instant('UI_COMPROBANTES_SELECCIONADOS_NOTAS_DE_CREDITO'));
				facturar = false;			
				return;
			}
			if (!value.IdContratoComision>0) {
				bcsComunes.msg("warning","warning",$translate.instant('UI_COMPROBANTES_SIN_CONTRATO_ASOCIADO'));
				facturar = false;			
				return;
			}		
			if (value.IdTipoContrato === 'CONV') {
				bcsComunes.msg("warning","warning",$translate.instant('UI_CONVENIO_NO_GENERA_FACTURA_OTROS_CONCEPTOS'));
				facturar = false;					
				return;
			}	
			if (value.Estado === 'PENDIENTE'){
				pendientes.push(value.IdComprobante);				
			}
		});
		if (facturar) {

			if (pendientes.length > 0) {
				pendientes.sort(function(a,b){ return a-b; });
				var t = bcsAlerta.crear('PREGUNTA', $translate.instant('UI_ESTA_SEGURO_ENVIAR_FACTURAR_MOVIMIENTOS_SELECCIONADOS_PENDIENTES')	+ " " + pendientes.join(", "), idVentana);
			}
			else{
				var t = bcsAlerta.crear('PREGUNTA',$translate.instant('UI_ESTA_SEGURO_ENVIAR_FACTURAR_MOVIMIENTOS_SELECCIONADOS'),idVentana);
			}
			
			t.then(function(result){
				if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
					var entrada = {
						p_IdEmpresa 		: $scope.SesionIdEmpresa,
						p_IdUsuario 		: $scope.SesionIdUsuario,
						p_IdModulo			: 'FAC',
						p_simularOperacion  : 'N',
						p_UsaReglasNegocio	: 'S',
						p_PorcentajeIva		: $scope.data.oIVA,
						p_MesProcesar		: util.dateToFormat($scope.data.kngFechaProceso, "MMMM YYYY").toUpperCase(),
						p_ComprobanteCobro	    : []
					};
					console.log(data);
					$.each(data,function(key,cobros){
						
						entrada.p_ComprobanteCobro.push({
							IdComprobante	: cobros.IdComprobante,
							ModoFacturacion	: cobros.CodigoModoFacturacionComprobante
						});
					});

					bcsServiciosFacturacion.EnviarCobrosMensualesFacturar(entrada,function(result){
						bcsComunes.ventanaOverlay($scope.idVentana,false);

						var id = bcsComunes.ventanaMsg({
							mensaje: '',
							conflictos: util.parseServerResponse(result)
						},$scope.idVentana);

						$scope.buscar();
						
					},function(result){
						bcsComunes.ventanaOverlay($scope.idVentana,false);

						var id = bcsComunes.ventanaMsg({
							mensaje: '',
							conflictos: util.parseServerResponse(result)
						},$scope.idVentana);
						
						$scope.buscar();
					});
				}
			});
	    }
    };
    
    $scope.data.cancelar = function () {
    	bcsComunes.ventanaCerrar(idVentana);
    }

	$scope.detalleMovimientoContrato = function(datos){
			bcsComunes.ventanaAbrir("DetalleMovimientoContrato", {IdComprobante: datos[0].IdComprobante}, false, false, idVentana );		
	};
	
	$scope.data.seleccionarTodosMovimientos = function() {	
		$scope.gridApiClienteContCobros.select($scope.gridApiClienteContCobros.tbody.find(">tr"));		
	};
	
	$scope.data.deseleccionarTodosMovimientos = function() {
		$scope.gridApiClienteContCobros.clearSelection();
	};
	
});