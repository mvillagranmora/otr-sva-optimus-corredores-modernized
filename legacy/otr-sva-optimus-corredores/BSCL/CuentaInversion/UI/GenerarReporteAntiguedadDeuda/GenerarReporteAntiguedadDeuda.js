angular.module('ModuloCustodia').controller('GenearReporteAntiguedadDeudaCtrl', 
		function($scope, bcsComunes, $translate, bcsServicioInterfaces , bcsServicioPlataforma){
	//*** configuracion de objetos ***//
	var idVentana = bcsComunes.ventanaWinId($scope);
	$scope.idVentana = idVentana;
	let parametros = bcsComunes.ventanaParams(idVentana);
	let fechaSistema = util.dateDS($scope.$root.SesionFechaSistema,"FechaSistema","CALL");

	/*Inicio filtros*/	
    $scope.filtros = {};

	$scope.filtros.fechaTopeArchivoGenerar;
	$scope.filtros.kOptFechaDeposito = {};
    $scope.filtros.kOptFechaDeposito.max;

	var datos = {
		p_IdEmpresa			: $scope.$root.SesionIdEmpresa,
		p_IdUsuario			: $scope.$root.SesionIdUsuario,
		p_IdModulo			: 'CTA',
		p_UsaReglasNegocio	: 'N',
		p_Fecha				: util.dateSPdate(fechaSistema),
		p_NumeroDias		: 1,
		p_EsHabil			: 'S',
		p_IdPais			: $scope.$root.SesionIdPaisSistema
	};

	bcsServicioPlataforma.FechaAnterior(datos,function(respuesta) {
		if(respuesta.P_OUT_FECHASIGUIENTE && respuesta.P_OUT_ERRORESCONTADOR === 0){
			$scope.filtros.fechaTopeArchivoGenerar = respuesta.P_OUT_FECHASIGUIENTE;
			$scope.filtros.kOptFechaDeposito.max = respuesta.P_OUT_FECHASIGUIENTE;
		}
		else{
			console.log("ERROR: " + JSON.stringify(respuesta));
		}
	},function(respuesta) {
		console.log("ERROR: " + JSON.stringify(respuesta));
	});

    $scope.filtros.oConfiguracionCuentaInversion = {
		tipo : "combobox-moneda-tesoreria", //Muestras las monedas que estan configuradas en Tesoreria.
		configuracion : {},
		filtros : {},
		idActual : false,
		idDefecto : $scope.SesionIdMonedaSistema || '',
		seleccionado : false,					
	};

	$scope.filtros.parametrosValidosParaNuevoReporte = {
		valido : true
	}

	$scope.filtros.ngSeparaIntermediacion = false;
    $scope.filtros.ngAgrupaPortafolios = false;
    $scope.filtros.ngConsolidar= false;
    $scope.filtros.ConsolidarDeshabilitado= true;
    $scope.filtros.AgrupaPortafoliosDeshabilitado= false;
	/*Fin filtros*/

	/*Inicio Observadores*/
	$scope.$watch('filtros.oConfiguracionCuentaInversion.seleccionado.IdMoneda',function(newval,oldval){
		if(newval === undefined){
			$scope.filtros.parametrosValidosParaNuevoReporte.valido = false;
		}
		else{
			$scope.filtros.parametrosValidosParaNuevoReporte.valido = true;
			$scope.filtros.ConsolidarDeshabilitado= $scope.filtros.oConfiguracionCuentaInversion.seleccionado.IdMoneda !== 0 ? false : true;
			$scope.filtros.AgrupaPortafoliosDeshabilitado = $scope.filtros.oConfiguracionCuentaInversion.seleccionado.IdMoneda !== 0 ? true : false;
		}
    });
	/*Fin Observadores*/

	/*Inicio botones*/
	$scope.generarReporte = function(){
		bcsComunes.ventanaOverlay(idVentana, true);
		const datos = {
			p_IdEmpresa: $scope.SesionIdEmpresa,
			p_IdUsuario: $scope.SesionIdUsuario,
			p_IdModulo: 'CTA',
			p_TipoPrograma: [{
				IdInterno: 0,
				IdTipoPrograma: "GenerarReporteAntiguedadDeudaCTA",
				SegundosDesfase: 0
			}],
			p_Parametro: llenarParametrosPrograma()
		};
		if(datos!== undefined && Object.entries(datos).length!==0){
			bcsServicioInterfaces.InscribirProcesoBatch(datos).$promise.then(
				function (result) {
					bcsComunes.ventanaOverlay(idVentana, false);
					var fechaLocal = util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion, "FechaHora", "CALL");
					bcsComunes.msg("INFO",$translate.instant('UI_INFORMACION'),$translate.instant('PROCESO_BATCH_EXITOSO'));
					bcsComunes.ventanaCerrar($scope.idVentana);
				},
				function (result) {
					bcsComunes.msg("ERROR","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
			});
		}
	}

	$scope.limpiaFiltrosGenerarReporte = function(){
        $scope.filtros.ngSeparaIntermediacion = false;
        $scope.filtros.ngAgrupaPortafolios = false;
        $scope.filtros.oConfiguracionCuentaInversion.idActual = 0;
    }
	/*Fin botones*/

	/*Inicio Metodos internos de controlador*/
	llenarParametrosPrograma = function(){
		let parametros = {};
		const fecha = new Date($scope.filtros.fechaTopeArchivoGenerar);
		return parametros = [
			{
				IdInterno: 0,
				NombreParametro: 'IdEmpresa',
				Valor: $scope.SesionIdEmpresa
			}, {
				IdInterno: 0,
				NombreParametro: 'IdUsuario',
				Valor: $scope.SesionIdUsuario
			},
			{
				IdInterno: 0,
				NombreParametro: 'P_FechaSolicitud',
				Valor: fecha.toJSON().slice(0,10)
			},
			{
				IdInterno: 0,
				NombreParametro: 'P_Moneda',
				Valor: ($scope.filtros.oConfiguracionCuentaInversion.seleccionado.IdMoneda===0) ? 'TODAS' : $scope.filtros.oConfiguracionCuentaInversion.seleccionado.IdMoneda
			},
			{
				IdInterno: 0,
				NombreParametro: 'P_SeparaIntermediacion',
				Valor: 'N'
			},
			{
				IdInterno: 0,
				NombreParametro: 'P_AgrupaPortafolios',
				Valor: ($scope.filtros.ngAgrupaPortafolios===true) ? 'S' : 'N'
			},
			{
				IdInterno: 0,
				NombreParametro: 'P_Consolidar',
				Valor: ($scope.filtros.ngConsolidar===true) ? 'S' : 'N'
			},
			{
				IdInterno: 0,
				NombreParametro: 'NombreInternoArchivo',
				Valor:  generarNombreRandom(30),
			}
		];
	}

	function generarNombreRandom(num) {
		const characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		const charactersLength = characters.length;
		let result = "";
		let ch;
		while (result.length < num){
			ch = characters.charAt(Math.floor(Math.random() * charactersLength));
			if (!result.includes(ch)){
				result += ch;
			}
		}
		return result;
	}

	var firstHeight = $('#Win_GenerarReporteAntiguedadDeuda');
	firstHeight.css("height", "190px");
	/*Fin Metodos internos de controlador*/
});
