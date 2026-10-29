angular.module('ModuloTesoreria').controller('IngresoDatosTalonarioChequesCtrl',
		function($scope,$state,$translate,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,bcsServiciosTesoreria)
{
	
	$scope.frm = {};
	//$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	var idVentana = bcsComunes.ventanaWinId($scope);
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	bcsComunes.ventanaAutoOverlay(idVentana,false,3000);

	var Remanente			= 0;
	var NumeroChequeDesde	= 0;
	var NumeroChequeActual	= 0;
	var NumeroChequeHasta	= 0;

	//SETEO INICIAL
	$scope.frm.nombreBanco = parametros[0].NombreBanco;
	$scope.frm.cuentaBancaria = parametros[0].NombreCuentaBancariaFull;
	$scope.frm.cantidadProcesar = parametros[0].DetalleSinCheque;

	//COMBOBOX
	$scope.frm.oConfiguracionChequerasSeries={
			tipo : "combobox-chequeras-series",
			configuracion : {},
			filtros : {
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
				IdCuentaBanco : { "field"   : "IdCuentaBanco","operator": "eq", "value"   : parametros[0].IdCuentaBanco }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : parametros[0].IdModalidadDePago
	};

	//WATCH
	$scope.$watch('frm.oConfiguracionChequerasSeries.seleccionado',function(newval,oldval){
		if (typeof $scope.frm.oConfiguracionChequerasSeries.seleccionado !== 'undefined' && $scope.frm.oConfiguracionChequerasSeries.seleccionado !== false)
		{
			Remanente			= $scope.frm.oConfiguracionChequerasSeries.seleccionado.Remanente;
			NumeroChequeDesde	= $scope.frm.oConfiguracionChequerasSeries.seleccionado.NumeroChequeDesde;
			NumeroChequeActual	= $scope.frm.oConfiguracionChequerasSeries.seleccionado.NumeroChequeActual;
			NumeroChequeHasta	= $scope.frm.oConfiguracionChequerasSeries.seleccionado.NumeroChequeHasta;
			
			$scope.frm.chequeDesde 	= NumeroChequeActual;
			$scope.frm.chequeHasta 	= Number($scope.frm.chequeDesde) + Number($scope.frm.cantidadProcesar);
			$scope.frm.remanente 	= Remanente - Number($scope.frm.cantidadProcesar);
		}		
	});
	$scope.$watchGroup(['frm.cantidadProcesar','frm.chequeDesde'],function(newval,oldval){
		if (typeof $scope.frm.cantidadProcesar !== 'undefined' && typeof $scope.frm.chequeDesde !== 'undefined' && $scope.frm.chequeDesde !== ""){
			$scope.frm.chequeHasta = $scope.frm.chequeDesde + Number($scope.frm.cantidadProcesar);
			
			var excede = Number(NumeroChequeHasta - $scope.frm.chequeHasta);
			var diferencia = Number($scope.frm.chequeHasta - $scope.frm.chequeDesde);

			
			if (Remanente >= diferencia && diferencia > 0 && excede >= 0 )
				$scope.frm.remanente = Remanente - diferencia;
			else{
				bcsComunes.msg("warning","warning",$translate.instant('UI_EXCEDE_REMANENTE_DE_CHEQUERA'));
				$scope.frm.chequeHasta 	= undefined;
				$scope.frm.remanente = Remanente;
			}	
		};
	});	
	
	//OPCIONES
	$scope.verAlerta = function(frm){
		var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_REGISTRARA_CHEQUES_TALONARIO_A_MOVIMIENTO_NOMINA') );
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR){ //Registrar cheques
				
				bcsComunes.ventanaOverlay(idVentana, true);
				$scope.btnActivo = false;
				var dataCols = [];
				dataCols.push({	Id						: parametros[0].IdNominaTransferencia,
								IdChequera				: $scope.frm.oConfiguracionChequerasSeries.seleccionado.IdChequera, 
								NumeroChequeDesde		: $scope.frm.chequeDesde,
								CantidadCheques			: $scope.frm.cantidadProcesar,
								IdCuentaBancariaSocio	: 0,
								IdTarjetaBancariaSocio	: 0,
								NumeroSerieSocio		: '',
								NumeroChequeSocio		: 0});

				//DATOS SERVICIO
				var datos = {};
				datos.p_IdEmpresa               = $scope.SesionIdEmpresa; 
				datos.p_IdUsuario               = $scope.SesionIdUsuario; 
				datos.p_IdModulo                = 'TES';
				datos.p_UsaReglasNegocio        = 'S';

				datos.p_FechaMovimiento			= util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
				datos.p_RegistrosAReprocesar	= dataCols;

				bcsComunes.log().info(JSON.stringify(datos));
				bcsServiciosTesoreria.ReprocesarNominasTransferencias(datos,success,error);

			}
		});
		return;
	};
	$scope.cancelar = function(){
		bcsComunes.ventanaData(idVentana);
		bcsComunes.ventanaCerrar(idVentana);
	};

	//MANEJO DE RESPUETA SERVICIOS
	var success = function(respuesta)
	{
		if (respuesta.P_OUT_ERRORESCONTADOR == 0){
			//Se debe mostrar el listado del proceso
			
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.msg("info","info",$translate.instant('UI_OPERACION_INGRESO_EXITO'));

			$scope.cancelar();
		}
		else
		{
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_INGRESO_ERROR'));
		}			
		bcsComunes.ventanaOverlay(idVentana, false);
		$scope.btnActivo = true;
	};
	var error = function(respuesta)
	{
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_INGRESO_ERROR'));
			
		bcsComunes.ventanaOverlay(idVentana, false);
		$scope.btnActivo = true;
	};

	var firstWindow = $('#Win_IngresoDatosTalonarioChequesModal')
			firstWindow.css("width", "380px");
});