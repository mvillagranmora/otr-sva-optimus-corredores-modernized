angular.module('ModuloCustodia').controller('AnularEmprestitoModalCtrl',
		function ($scope,$timeout,bcsComunes,$translate,
				  bcsServicioPlataforma,bcsServicioCalcularMontoTipoInstrumento,bcsServiciosPrestamo) {

	$scope.data = {};
	$scope.winID=bcsComunes.ventanaWinId($scope);
	
	var FechaCierre =  new Date();
	$scope.data.plazoAnticipo = 0;
	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	$scope.data.oDatosEmprestito = parametros;
	
	$scope.data.kngFolioPrsetamo = $scope.data.oDatosEmprestito.FolioPrestamo;
	$scope.data.kngFolioEmprestito = $scope.data.oDatosEmprestito.FolioEmprestito;
	$scope.data.tipoPrsetamo = $scope.data.oDatosEmprestito.NombreTipoPrestamo;
	$scope.data.kngFechaInicio = util.date($scope.data.oDatosEmprestito.FechaInicioEmprestito).toDate();
	$scope.data.tipoInstrumento = $scope.data.oDatosEmprestito.NombreTipoInstrumentoSubyacente;
	$scope.data.instrumento = $scope.data.oDatosEmprestito.NemoSubyacente;
	$scope.data.checkConGarantiasEspecificas = ($scope.data.oDatosEmprestito.UsaGarantiaEspecifica==='S')?true:false;
	$scope.data.prestamista = $scope.data.oDatosEmprestito.DocumentoIdentidad + ' ' +  $translate.instant('UI_PORTAFOLIO') + ': ' +  $scope.data.oDatosEmprestito.CodigoPortafolio + ' ' +  $translate.instant('UI_RAZON_SOCIAL') + ': ' + $scope.data.oDatosEmprestito.RazonSocial;
	
	$scope.data.kngCantidad = $scope.data.oDatosEmprestito.INGCantidad;
	$scope.data.kngPrecio = $scope.data.oDatosEmprestito.INGPrecioInstrumento;
	$scope.data.kngMonto = $scope.data.oDatosEmprestito.INGMontoInstrumento;
	$scope.data.kngFechaAnulacion = $scope.SesionFechaSistema;
	$scope.data.kngPlazo = $scope.data.oDatosEmprestito.PlazoEnDiasOperacion
	$scope.data.kngPorcentajePremio = $scope.data.oDatosEmprestito.INGTasaEmprestito;
	$scope.data.kngMontoPremio = $scope.data.oDatosEmprestito.INGMontoPremio;
	$scope.data.kngMontoPlazo = ($scope.data.oDatosEmprestito.INGMontoPremio + $scope.data.kngMonto);
	
	$scope.data.monedaInstrumento = $scope.data.oDatosEmprestito.IdMonedaInstrumento;
	//cargar al inicio
	$scope.data.folioPrsetamoOptions = {format: 'n0',min:0,spinners: false};
	$scope.data.folioEmprestitoOptions = {format: 'n0',min:0,spinners: false};
	$scope.data.cantidadOptions = {format: 'n'+(util.bcs_formato(0,'PRT','Cantidad',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
	$scope.data.precioOptions = {format: 'n'+(util.bcs_formato(0,'PRT','Precio',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
	$scope.data.montoOptions = {format: 'c'+(util.bcs_formato(0,'PRT','Monto',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
	$scope.data.plazoOptions = {format: 'n0',min:0,spinners: false};
	$scope.data.porcentajePremioOptions = {format: 'n'+(util.bcs_formato(0,'PRT','Tasa',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
	$scope.data.montoPremioOptions = {format: 'c'+(util.bcs_formato(0,'PRT','Monto',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
	$scope.data.montoPlazoOptions = {format: 'c'+(util.bcs_formato(0,'PRT','Monto',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
	
	
	$scope.data.accionDisabled = true;

	
	$scope.data.btnCerrar = function(refrescar) {
		bcsComunes.ventanaData(idVentana,refrescar);
		bcsComunes.ventanaCerrar(idVentana);
	};
	
	$scope.data.anularEmprestito = function()
    {
		
		
		var datos = {};	
		bcsComunes.ventanaOverlay($scope.winID,true); 
		$scope.data.accionDisabled = undefined;
		
		datos.p_IdEmpresa = $scope.SesionIdEmpresa; //saca de variables de entorno
		datos.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
		datos.p_IdModulo = 'CUS';
		datos.p_IdEmpleadoDigitador = $scope.SesionIdEmpleado;
		datos.p_UsaReglasNegocio = 'S';
		datos.p_IdPais = $scope.SesionIdPaisSistema;
		datos.p_FechaAnulacion = $scope.SesionFechaSistema;
		datos.p_FolioEmprestito = Number($scope.data.oDatosEmprestito.FolioEmprestito);
		
   		bcsServiciosPrestamo.AnularEmprestito
							(datos,
							function(respuesta){	  		
								bcsComunes.ventanaMsg({
									conflictos:util.parseServerResponse(respuesta)
								},$scope.winID);
								bcsComunes.ventanaOverlay($scope.winID,false);
								$scope.data.btnCerrar(true);
							},
							function(respuesta){
								bcsComunes.ventanaMsg({
									conflictos:util.parseServerResponse(respuesta)
								},$scope.winID);
								bcsComunes.ventanaOverlay($scope.winID,false);
							});
   	};
		

	
	//Primer evento - filtro inicial
	$timeout(function(){
		//todo ObtenerPlazo();
	},1000);
});
