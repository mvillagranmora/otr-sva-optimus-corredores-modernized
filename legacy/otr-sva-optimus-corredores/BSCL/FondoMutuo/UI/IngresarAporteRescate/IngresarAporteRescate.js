angular.module('ModuloFFM').controller('IngresarFFMCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,$timeout){
	var ingresarFFM = this;            
	$scope.formData = {};
	$scope.masInformacion = false;
	$scope.formData.setInstrumento = "-1";
	$scope.datosFondo = {};
	$scope.formData.IngresoConfirmacion = 'I';
	$scope.formData.AporteRescate = 'A';
	$scope.formData.TipoRescate = '';
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.formData.DatosAdicionales = "N";
	$scope.formData.visibleTruco = true;
	
	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,7);
    }
		
	$scope.ajustaVentana();
		
	$scope.formData.FiltrosBusquedaFondo = {
			IdCategoriaInversionista : { "field"   : "IdCategoriaInversionista","operator": "eq", "value"   :  undefined}
	};
	
	$scope.formData.filtrosFormaPago = {
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  0},
			IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   :  "-1"}
	};
	$scope.oSocioNegocio={
			tipo : "combobox-socio-negocio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},
		};

	$scope.oPortafolio={
		 tipo : "combobox-portafolio",
		 configuracion :{},
		 idActual : false,
		 seleccionado : false,
		 filtros: {
		 	 IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
		 },
	 };
	
	 $scope.formData.masDatos = function()
	 {
		 if ($scope.formData.DatosAdicionales === 'S'){
			 $scope.formData.DatosAdicionales = 'N';
	     }
		 else{
			 $scope.formData.DatosAdicionales = 'S';
	     }
	 }
	
	 $scope.formData.ingresarAporteRescate = function()
     {	
		 bcsComunes.ventanaOverlay($scope.winID,true);
		 var datosIngresoAporteRescate = {
				 p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
				 p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
				 p_IdModulo : 'FMM',
				 p_simularOperacion : 'N',
				 p_UsaReglasNegocio : 'S',
				 
				 p_SolicitudesAporteRescate : [{
					    IdInterno		      		:	0,
						IdCliente 	          		:	$scope.oPortafolio.seleccionado.IdCliente,
				    	IdPortafolio          		:	$scope.oPortafolio.seleccionado.IdPortafolio,
				    	TipoSolicitud         		:	$scope.oOperacion.seleccionado.IndicadorAporteRescate,
				    	IdSerieFondo          		:	$scope.datosFondo.IdSerieFondo,
				    	IdSerieFondoDestino   		:   '',
				    	IdEmisorDestino 	  		:	'',
				    	IdClienteDestino 	  		:	0,
				    	IdPortafolioDestino   		:	0,
				    	IdTipoCartera 		  		:   '',
				    	CalculoPorMontoCuota  		:	$scope.oOperacion.seleccionado.IndicadorAporteRescate === 'RES' ? $scope.formData.TipoRescate : 'M',
				    	EsPorMontoLiquido			:	'N',
				    	FechaEmision                :   util.dateSPdate($scope.formData.fechaAporte),
				    	Monto 				  		:   $scope.formData.montoInicial,
						NumeroCuotas 	  	  		:	$scope.formData.numeroCuotas,
				    	IdMoneda 			  		:	$scope.oOperacion.seleccionado.IndicadorAporteRescate === 'APO' ? $scope.formData.IdMonedaAporte : $scope.datosFondo.IdMoneda,
				    	FormaPago					:	$scope.formData.IdFormaPago ? $scope.formData.IdFormaPago : 0,
				    	SolicitudBatch				:	'N',
				    	Glosa						: 	$scope.formData.comentario,
				    	Porcentaje					:	0,
				    	FolioContrato				:	0
				 }]
		 }
	     bcsServicioFondosMutuos.CrearSolicitudAporteRescateNormal(datosIngresoAporteRescate).$promise.then(function(respuesta){
	     	 console.log(respuesta);
	     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		$scope.formData.ResultadoSolicitudAporteRescate = {
	    			    SecuenciaOperacion              : respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate,
						IdOperacion						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FondoMutuo            			: respuesta.P_OUT_RESULTADO[0].IdSerieFondo,
						TipoFondo 						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FechaAporte  			 		: util.formatoDate(respuesta.P_OUT_RESULTADO[0].FechaAceptacion),
						MontoInicial	 				: respuesta.P_OUT_RESULTADO[0].Monto,
						NumeroCuotas			 		: util.truncarNumero(respuesta.P_OUT_RESULTADO[0].NumeroCuotas, $scope.formData.decimalesCantidad),
						DocumentoIdentidad              : respuesta.P_OUT_RESULTADO[0].DocumentoIdentidad,
						CodigoPortafolio                : respuesta.P_OUT_RESULTADO[0].CodigoPortafolio,
						NombreCliente                   : respuesta.P_OUT_RESULTADO[0].NombreCliente,
						FechaLiquidacion                : util.formatoDate(respuesta.P_OUT_RESULTADO[0].FechaLiquidacion),
						FechaCertificacion              : util.formatoDate(respuesta.P_OUT_RESULTADO[0].FechaCertificacion),
						MontoTransferencia              : respuesta.P_OUT_RESULTADO[0].MontoTransferencia,
						NumeroCuotaTransferencia        : util.truncarNumero(respuesta.P_OUT_RESULTADO[0].NumeroCuotaTransferencia, $scope.formData.decimalesCantidad),
						IdEmisorOrigen                  : respuesta.P_OUT_RESULTADO[0].IdEmisorOrigen,
						IdSerieFondoDestino             : respuesta.P_OUT_RESULTADO[0].IdSerieFondoDestino,
						IdEmisorDestino                 : respuesta.P_OUT_RESULTADO[0].IdEmisorDestino,
						IndicadorMLT                    : respuesta.P_OUT_RESULTADO[0].IndicadorMLT,
	     			};
	     		$scope.formData.IngresoConfirmacion = 'C';
	     		$scope.ajustaVentana();
	     		bcsComunes.ventanaMsg({mensaje:"Operacion ingresada: " + respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate,conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     	 }else{
	     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_INGRESO_APORTE_RESCATE"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     	 }
   		 },function(respuesta){
   			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_INGRESO_APORTE_RESCATE"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
            console.log('me cai jeje ' + respuesta);
   		 });
     }
	 
	 $scope.$watch('datosFondo',function(newval,oldval){
		 if (newval && newval.IdFondo) {
				$scope.formData.decimalesCantidad = newval.DecimalesCuota;
				$scope.calcularMontoCuota(newval,$scope.formData.montoInicial,$scope.formData.numeroCuotas,$scope.formData.TipoRescate);
				$scope.formData.IdMonedaAporte = newval.IdMoneda;
		    }
			else {
				$scope.formData.decimalesCantidad = 0;
				$scope.formData.numeroCuotas = 0;
				$scope.formData.montoInicial = 0;
				$scope.formData.fechaAporte = util.dateInicioDia($scope.SesionFechaSistema);
			}
		 $scope.ajustaVentana();
	 });
	 
	 $scope.$watch('formData.IdMonedaAporte', function(newval,oldaval){
		 if(newval){
			 $scope.formData.decimalesMonto = util.bcs_formato(0,'FMM','Monto',newval,'','').cantidadDecimales;
		 }else{
			 $scope.formData.decimalesMonto = 0;
		 }
		 $scope.ajustaVentana();
	 });
	 
	 $scope.$watch('formData.montoInicial',function(newval,oldval){
		if(newval && newval != oldval){
			$scope.calcularMontoCuota($scope.datosFondo,newval,$scope.formData.numeroCuotas,$scope.formData.TipoRescate);
		}
	 });
	 
	 $scope.$watch('masInformacion',function(newval,oldval){
		 $scope.ajustaVentana();
	 });
	 
	 $scope.$watch('formData.TipoRescate',function(newval,oldval){
		 if(newval === 'T'){
			 $scope.calcularMontoCuota($scope.datosFondo,$scope.formData.SaldoMonto,$scope.formData.SaldoCuotas,newval); 
		 }else{
			 $scope.calcularMontoCuota($scope.datosFondo,0,0,newval); 
		 }
		 
	 });
	 
	 $scope.$watch('formData.numeroCuotas',function(newval,oldval){
		 if(newval && newval != oldval){
			 $scope.calcularMontoCuota($scope.datosFondo,$scope.formData.montoInicial,newval,$scope.formData.TipoRescate);
		 }
	 });
	 
	 $scope.$watch('oOperacion.seleccionado', function(newval,oldval){
		 if (newval && newval.IdTipoMovimientoFondoMutuo){
			 if(newval.IndicadorAporteRescate != oldval.IndicadorAporteRescate){
				 $scope.formData.montoInicial = undefined;
				 $scope.formData.numeroCuotas = undefined;
			 }
			 if(newval.IndicadorAporteRescate === 'APO'){
				$scope.formData.AporteRescate = 'A';
				$scope.formData.TipoRescate = 'M';
			 }
			 else{
				$scope.formData.AporteRescate = 'R';
			 }
			 
			 $scope.ajustaVentana();
		 }
		 
	 });
	 
	 $scope.limpiarFMM = function(volver){
		 if(!volver){
			 $scope.formData.tipoOperacion = 0;
			 $scope.formData.AporteRescate = 'A';
		 }
		 $scope.formData.IngresoConfirmacion = 'I';
		 $scope.formData.TipoRescate = 'M';
		 $scope.oSocioNegocio.idActual = 0;
		 $scope.oPortafolio.idActual = 0;
		 $scope.formData.setFondo = '-1';
		 $scope.formData.fechaAporte = util.dateInicioDia($scope.SesionFechaSistema);
		 $scope.formData.montoInicial = undefined;
		 $scope.formData.numeroCuotas = undefined;
		 $scope.formData.montoComisiones = undefined;
		 $scope.formData.IdFormaPago = '0';
		 $scope.formData.comentario = '';
		 $scope.formData.FiltrosBusquedaFondo = {
			IdCategoriaInversionista : { "field"   : "IdCategoriaInversionista","operator": "eq", "value"   :  undefined}
		 };
		 $scope.ajustaVentana();
	 }
	 
	 $scope.calcularMontoCuota = function(fondo,monto,cantidad,tipo){
		 if(fondo && fondo.ValorCuota > 0){
			 if(tipo === 'M'){
				 $scope.formData.numeroCuotas = monto > 0 ? monto/fondo.ValorCuota : 0;
				 $scope.formData.montoInicial = monto;
			 }
			 if(tipo === 'C'){
				 $scope.formData.montoInicial = cantidad > 0 ? fondo.ValorCuota * cantidad : 0;
				 $scope.formData.numeroCuotas = cantidad;
			 }
			 if(tipo === 'T'){
				 $scope.formData.numeroCuotas = monto;
				 $scope.formData.montoInicial = cantidad;
			 }
		 }
	 }
	 
	 $scope.formData.resetFoco =  function() {
	 	$scope.formData.focoVolver = false;
		if($scope.formData.IngresoConfirmacion === 'I')
		{
			$scope.formData.focoOperacion = true;
			$scope.formData.focoVolver = false;
		}
	 };
	 
	 $scope.limpiarFMM(); //Limpiar inicial
	 

});