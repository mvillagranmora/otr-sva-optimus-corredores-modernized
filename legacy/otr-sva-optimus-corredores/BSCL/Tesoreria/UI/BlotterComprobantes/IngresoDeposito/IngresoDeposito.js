angular.module('ModuloTesoreria').controller('IngresoDepositosCtrl',
			function($scope,$translate,$timeout,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,$translate,
					bcsServiciosTesoreria, bcsBlotterComprobantesGrid)
{ 
	$scope.data = {};
	
	//Obtiene los parametros traspasados desde la ventana de documentos tesorería
	$scope.idVentana = bcsComunes.ventanaWinId ($scope);
	bcsComunes.ventanaAutoOverlay($scope.idVentana,false,3000);
	
	$scope.parametrosVentana = bcsComunes.ventanaParams($scope.idVentana);	
	$scope.documentosADepositar = {};          //objeto con los documentos
	$scope.data.MismoBanco 		= true;
	$scope.data.OtroBanco 		= false;
	$scope.data.SoloEfectivo	= false;

	// --------------------------------------------------
	// CONFIGURACION DE COMBOS
	// --------------------------------------------------
	//Configuración combo "Cuenta Bancaria Corredora"
	$scope.data.oConfiguracionCuentaBancoCorredora={
			tipo : "combobox-cuentas-bancarias-corredora",
			configuracion : {},
			filtros : {
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
				IdMoneda : { "field"   : "IdMoneda","operator": "eq", "value"	: 0}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	// --------------------------------------------------
	// FIN CONFIGURACION DE COMBOS
	// --------------------------------------------------
	
	
	// --------------------------------------------------
	//  CONFIGURACION GRILLAS
	// --------------------------------------------------	

	//Redefine dataSource para filtro dinámico.
	var dataSourceDocumentos = bcsBlotterComprobantesGrid.dsDefDocumentosDeposito;
	dataSourceDocumentos.definition.filter = {};
	if (Object.keys($scope.parametrosVentana).length > 0){
		var documentosADepositar = [];
		for (i=0;i<Object.keys($scope.parametrosVentana).length;i++){
			documentosADepositar.push({field: "IdComprobanteFormaDePago",	operator: "eq", value: $scope.parametrosVentana[i].IdComprobanteFormaDePago });
		}

		var filtrosFijos = documentosADepositar;		
		dataSourceDocumentos.definition.filter.logic = 'or';
	} else {		
		var filtrosFijos = [{ field: "DocumentoEnviaADeposito",	operator: "eq", 	value: "S" }];
		dataSourceDocumentos.definition.filter.logic = 'and';
	}
	dataSourceDocumentos.definition.filter.filters = filtrosFijos;


	//Grilla Documentos	
	$scope.confBasicaDocumentos = {
		dataSource			: new bcs.DataSource(dataSourceDocumentos),
		columns				: bcsBlotterComprobantesGrid.columnasIngresoDeposito(),		
		sortable			: true,
		pageable			: false,
		autoBind			: false,
		filterable			: true,
		height				: 300,
		selectable 			: "multiple row",
		userSettings		: true,
		gridCreated 		: function(g) { $scope.gridDocumentosDeposito = g; },
	};
	// --------------------------------------------------
	//  FIN CONFIGURACION DE GRILLAS
	// --------------------------------------------------
	
	filtrarGrilla = function(operador){
		if (typeof $scope.gridDocumentosDeposito === 'undefined' ) return;
		var filtros 	= [];
		var filtross 	= {};

		if (Object.keys($scope.parametrosVentana).length === 0){
			filtros = _.clone(filtrosFijos);
		}

		if (typeof $scope.data.oConfiguracionCuentaBancoCorredora.seleccionado !== 'undefined' && $scope.data.oConfiguracionCuentaBancoCorredora.seleccionado !== false){
			filtros.push({ field: "IdMoneda", operator: "eq", value: $scope.data.oConfiguracionCuentaBancoCorredora.seleccionado.IdMoneda });

			if ($scope.data.SoloEfectivo){
				filtros.push({ field: "IdMedioDePago", operator: "eq", value: "EFE" });
			} else {
				filtros.push({ field: "IdBancoSocio", operator: operador, value: $scope.data.oConfiguracionCuentaBancoCorredora.seleccionado.IdBanco });
			}

			if ($scope.data.OtrosBanco){
				filtros.push({ field: "IdMedioDePago", operator: "neq", value: "EFE" });
			}
		}
		
		if (Object.keys($scope.parametrosVentana).length > 0){					
			filtross = {
						filters: [
									{
									  logic: "or",
									  filters: filtrosFijos
									},
									{
									  logic: "and",
									  filters: filtros	
									}
								 ]
					   };

			$scope.gridDocumentosDeposito.dataSource.filter(filtross);
		} else {			
			$scope.gridDocumentosDeposito.dataSource.filters(filtros);
		}

		$scope.data.TotalDeposito = 0;	
	};
	
	//Al cambiar "Cuenta Bancaria de la corredora", actualizan la información de la cuenta. 	
	$scope.$watch('data.oConfiguracionCuentaBancoCorredora.seleccionado',function(newval,oldval){
		if (typeof $scope.data.oConfiguracionCuentaBancoCorredora.seleccionado !== 'undefined' && $scope.data.oConfiguracionCuentaBancoCorredora.seleccionado !== false)
		{
			$scope.data.TipoCuentaCorredora   	= $scope.data.oConfiguracionCuentaBancoCorredora.seleccionado.NombreTipoCuentaBancaria;
			$scope.data.BancoCuentaCorredora 	= $scope.data.oConfiguracionCuentaBancoCorredora.seleccionado.NombreBanco;			
			$scope.data.NumeroCuentaCorredora	= $scope.data.oConfiguracionCuentaBancoCorredora.seleccionado.NumeroCuentaBancaria;
			$scope.data.MonedaCuentaCorredora 	= $scope.data.oConfiguracionCuentaBancoCorredora.seleccionado.NombreMoneda;
			$scope.data.Sucursal 				= $scope.data.oConfiguracionCuentaBancoCorredora.seleccionado.Sucursal;
			$scope.data.Plaza 					= $scope.data.oConfiguracionCuentaBancoCorredora.seleccionado.NombrePlaza;			
			$scope.data.MismoBanco				= true;
			filtrarGrilla("eq");
			
			
			//Editar Info de la grilla para sumar días Valuta
			var formaDePago      = $scope.confBasicaDocumentos.dataSource.data();
			for (i= 0 ; i < formaDePago.length ; i++){				
				if ($scope.data.oConfiguracionCuentaBancoCorredora.seleccionado.IdBanco === formaDePago[i].IdBancoSocio)
					var dia = $scope.data.oConfiguracionCuentaBancoCorredora.seleccionado.PlazoRetencionMismoBanco;
				else
					var dia = $scope.data.oConfiguracionCuentaBancoCorredora.seleccionado.PlazoRetencionOtroBanco;

				formaDePago[i].DiasRetencion = dia;
				formaDePago[i].FechaLiquidacion = util.date(formaDePago[i].FechaLiquidacionComprobante).add( dia ,'days').toDate();
			};			
			$scope.gridDocumentosDeposito.refresh();
		}
	});

	$scope.$watch('data.MismoBanco',function(newval,oldval){
		$scope.data.OtrosBanco = false; 
		$scope.data.SoloEfectivo = false; 
		$scope.data.MismoBanco = true;  
		filtrarGrilla("eq");
	});	
	$scope.$watch('data.OtrosBanco',function(newval,oldval){

		$scope.data.OtrosBanco = true; 
		$scope.data.SoloEfectivo = false; 
		$scope.data.MismoBanco = false;  
		filtrarGrilla("neq");
	});
	$scope.$watch('data.SoloEfectivo',function(newval,oldval){
		$scope.data.OtrosBanco = false; 
		$scope.data.SoloEfectivo = true; 
		$scope.data.MismoBanco = false; 
		filtrarGrilla("eq");
	});	


	$scope.$watch('seleccionDocumentos',function(newval,oldval){
		if (typeof $scope.data.oConfiguracionCuentaBancoCorredora.seleccionado !== 'undefined' && $scope.data.oConfiguracionCuentaBancoCorredora.seleccionado !== false)
		{
			var totalADeposito = 0;
			for (i= 0 ; i < $scope.seleccionDocumentos.length ; i++){
				totalADeposito = totalADeposito + $scope.seleccionDocumentos[i].Monto;
			}
			$scope.data.TotalDeposito 			= totalADeposito;
			$scope.data.TotalDepositoSimbolo 	= $scope.data.oConfiguracionCuentaBancoCorredora.seleccionado.IdMoneda;
			$scope.data.TotalDepositoDecimales 	= (util.bcs_formato(totalADeposito,'GEN','Monto',$scope.data.TotalDepositoSimbolo,'','')).cantidadDecimales;
		}
	});	
	
	// --------------------------------------------------
	// SERVICIOS INGRESO DEPOSITO  
	// --------------------------------------------------
	$scope.ingresarDeposito = function(){

		if(($scope.data.NumeroCuentaCorredora === 'undefined'||$scope.data.NumeroCuentaCorredora === null ||$scope.data.NumeroCuentaCorredora === '') || ($scope.data.Plaza ==='undefined' || $scope.data.Plaza === null || $scope.data.Plaza === '' )){  //null 
			bcsComunes.ventanaMsg({
			mensaje:$translate.instant("UI_DOCUMENTOS_NO_CUMPLEN_REQUISITOS_DEPOSITO"),
			},$scope.idVentana);

		}else{
			var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_REALIZARA_DEPOSITO_DE_DOCUMENTOS'),$scope.idVentana);
			t.then(function(result){
				if (result===OPCIONES_BCSALERTA.ACEPTAR){
					var parametros = {};
					bcsComunes.ventanaOverlay($scope.idVentana, true);
	
	
	
					var documentosADepositar = [];
					for (i=0; i<$scope.seleccionDocumentos.length; i++){
						documentosADepositar.push({	"NumeroDeposito" : 0, 
													"IdCuentaBancariaCorredora" : $scope.data.oConfiguracionCuentaBancoCorredora.seleccionado.IdCuentaBanco, 
													"IdComprobanteFormaDePago": $scope.seleccionDocumentos[i].IdComprobanteFormaDePago});
													
					}
	
					parametros.p_IdEmpresa   		 		= $scope.SesionIdEmpresa;
					parametros.p_IdUsuario   		 		= $scope.SesionIdUsuario;
					parametros.p_IdModulo 			 		= 'TES';
					parametros.p_simularOperacion 		 	= 'N';
					parametros.p_UsaReglasNegocio     		= 'S';
					parametros.p_FechaMovimiento			= util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));				
					parametros.p_DocumentosADepositar		= documentosADepositar;
	
					
					bcsServiciosTesoreria.CrearDeposito(
						parametros,
						function(respuesta){
							bcsComunes.ventanaOverlay($scope.idVentana, false);
	
							if (respuesta.P_OUT_ERRORESCONTADOR !== undefined && respuesta.P_OUT_ERRORESCONTADOR == 0){	
								bcsComunes.ventanaMsg({
									mensaje:$translate.instant("UI_CREADA_NOMINA_DE_DOCUMENTOS_A_DEPOSITAR"),
									conflictos:util.parseServerResponse(respuesta)
								},$scope.idVentana);
	
								$scope.cancelar();
							} else {	
								bcsComunes.ventanaMsg({
									mensaje:$translate.instant("UI_DOCUMENTOS_NO_CUMPLEN_REQUISITOS_DEPOSITO"),
									conflictos:util.parseServerResponse(respuesta)
								},$scope.idVentana);
							}
						},
						function(respuesta){
							bcsComunes.ventanaOverlay($scope.idVentana, false);
	
							bcsComunes.ventanaMsg({
								mensaje:$translate.instant("UI_ERROR_AL_CREAR_NOMINA_DOCUMENTOS_A_DEPOSITAR"),
								conflictos:util.parseServerResponse(respuesta)
							},$scope.idVentana);
						}
					);
				}
			});
		}
	};

	$scope.cancelar = function(){
		bcsComunes.ventanaData($scope.idVentana);
		bcsComunes.ventanaCerrar($scope.idVentana);
	};
});