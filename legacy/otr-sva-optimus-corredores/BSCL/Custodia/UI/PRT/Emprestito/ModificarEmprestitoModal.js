(function() {
	angular.module('ModuloCustodia').controller('ModificarEmprestitoModalCtrl',
	function ($scope,$translate,$timeout,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,
		bcsServicioPlataforma,bcsServicioCalcularMontoTipoInstrumento,bcsServiciosPrestamo,bcsServiciosCustodia) {
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		$scope.data = {};
		
		var idVentana = bcsComunes.ventanaWinId($scope);
		var parametros = bcsComunes.ventanaParams(idVentana);
		$scope.data.oDatosEmprestito = parametros;
		var miFechaSis = $scope.SesionFechaSistema;
		
		

		
		/*********************************************
		 * CARGA INICIAL
		 ********************************************/
		$scope.data.kngPlazo 						= $scope.data.oDatosEmprestito.PlazoEnDiasOperacion;
		$scope.data.folioPrsetamoOptions 			= {format: 'n0',min:0,spinners: false};
		$scope.data.folioEmprestitoOptions 			= {format: 'n0',min:0,spinners: false};
		$scope.data.cantidadOptions 				= {format: 'n'+(util.bcs_formato(0,'CUS','Cantidad',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.precioOptions 					= {format: 'c'+(util.bcs_formato(0,'CUS','Precio',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.montoOptions 					= {format: 'c'+(util.bcs_formato(0,'CUS','Monto',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.plazoOptions 					= {format: 'n0',min:0,spinners: false};
		$scope.data.porcentajePremioOptions 		= {format: 'n'+(util.bcs_formato(0,'CUS','Tasa',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.montoPremioOptions 				= {format: 'c'+(util.bcs_formato(0,'CUS','Monto',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.montoPlazoOptions 				= {format: 'c'+(util.bcs_formato(0,'CUS','Monto',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.kngFolioPrsetamo 				= $scope.data.oDatosEmprestito.FolioPrestamo;
		$scope.data.kngFolioEmprestito 				= $scope.data.oDatosEmprestito.FolioEmprestito;
		$scope.data.tipoPrsetamo	 				= $scope.data.oDatosEmprestito.NombreTipoPrestamo;
		$scope.data.kngFechaInicio 					= util.date($scope.data.oDatosEmprestito.FechaEmprestito).toDate();
		$scope.data.kngFechaVencimiento 			= util.date($scope.data.oDatosEmprestito.FechaVencimientoEmprestito).toDate();
		$scope.data.CantidadMaxima 					= $scope.data.oDatosEmprestito.INGCantidad;
		$scope.data.kngCantidad 					= $scope.data.oDatosEmprestito.INGCantidad;
		$scope.data.tipoInstrumento 				= $scope.data.oDatosEmprestito.NombreTipoInstrumentoSubyacente;
		$scope.data.instrumento 					= $scope.data.oDatosEmprestito.NemoSubyacente;
		$scope.data.prestamista 					= $scope.data.oDatosEmprestito.DocumentoIdentidad + ' ' +  $translate.instant('UI_PORTAFOLIO') + ': ' +  $scope.data.oDatosEmprestito.CodigoPortafolio + ' ' +  $translate.instant('UI_RAZON_SOCIAL') + ': ' + $scope.data.oDatosEmprestito.RazonSocial; 
		$scope.data.kngFechaLiquidacion				= util.dateInicioDia(miFechaSis);
		$scope.data.kngPorcentajePremio 			= $scope.data.oDatosEmprestito.INGTasaEmprestito;
		$scope.data.kngPrecio 						= Number($scope.data.oDatosEmprestito.INGPrecioInstrumento);
		$scope.data.kngMonto 						= $scope.data.oDatosEmprestito.INGMontoInstrumento;
		$scope.data.kngPorcentajePremio 			= Number($scope.data.oDatosEmprestito.INGTasaEmprestito);
		$scope.data.accionDisabled 					= true;
		$scope.data.InteresDiarioOptions 			= $scope.data.oDatosEmprestito.INGMontoInteresCompraProximo;
		$scope.data.InteresAcumuladoOptions			= $scope.data.oDatosEmprestito.INGMontoInteresCorridoCompraActual;
		$scope.data.monedaInstrumento				= $scope.data.oDatosEmprestito.IdMonedaInstrumento;
		
		
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		$scope.data.btnCerrar = function(refrescar) {
			bcsComunes.ventanaData(idVentana,refrescar);
			bcsComunes.ventanaCerrar(idVentana);
		};
		
	 

		/*********************************************
		 * WATCHERS
		 *********************************************/

		$scope.$watchGroup(['data.kngMonto','data.kngPorcentajePremio'], function (newValue, oldValue){
			if (typeof $scope.data.kngMonto !== 'undefined' && typeof $scope.data.kngPorcentajePremio !== 'undefined'){

				const base = $scope.data.monedaInstrumento === 'CLP' ? 30 : 360;

				$scope.data.kngMontoPlazo  = bcsComunes.calcularMontoaPlazo($scope.data.kngMonto, parametros.PlazoEnDiasOperacion,Number($scope.data.kngPorcentajePremio), base);
				$scope.data.kngMontoPremio = Number($scope.data.kngMontoPlazo) - Number($scope.data.kngMonto);
				$scope.data.kngInteresDiario = calcularInteresDiario($scope.data.kngMonto, $scope.data.kngPorcentajePremio);
				$scope.data.kngInteresAcumulado = util.calcularInteresAcumulado($scope.data.kngInteresDiario, util.dateInicioDia($scope.data.kngFechaInicio), util.dateInicioDia(miFechaSis));
			}
		});
	   	
		

		//Calcula Interes Diario = (Valor Inicial * Prima)/30
		var calcularInteresDiario = function(monto, tasa)
		{	
			return Math.ceil(Number((monto * (tasa/100)) / 30));
		};	

		$scope.data.ModificarEmprestito = function()
	    {
			
			var datos = {};	
			bcsComunes.ventanaOverlay(idVentana,true); 
			$scope.data.accionDisabled = undefined;
			
			datos.p_IdEmpresa 		= $scope.SesionIdEmpresa;
			datos.p_IdUsuario 		= $scope.SesionIdUsuario;
			datos.p_IdModulo 		= 'CUS';
			datos.p_IdCartera 		= $scope.data.oDatosEmprestito.IdCartera;
			datos.p_IdSubyacente 	= $scope.data.oDatosEmprestito.IdSubyacente;
			datos.p_MontoPremio 	= Number($scope.data.kngMontoPremio) || 0;
			datos.p_TasaPremio 		= Number($scope.data.kngPorcentajePremio) || 0;
			datos.p_MontoAPlazo 	= Number($scope.data.kngMontoPlazo) || 0;
			datos.p_InteresDiario 			= Number($scope.data.kngInteresDiario) || 0;
			datos.p_InteresAcumulado		= Number($scope.data.kngInteresAcumulado) || 0;
			
	   		bcsServiciosPrestamo.ModificarEmprestito(datos,successModificarEmprestito,errorModificarEmprestito);
	   	};
	   	
	   	
	 	var successModificarEmprestito = function(respuesta)
		{
	   		bcsComunes.ventanaOverlay(idVentana,false);
			$scope.data.accionDisabled = true;
			if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
		        
				var a =  bcsComunes.ventanaMsg({
				    mensaje:$translate.instant("UI_OPERACION_INGRESO_EXITO"),
				    conflictos:util.parseServerResponse(respuesta)
				  },idVentana);
				
				var promesa=bcsComunes.ventanaPromesa(a);
				promesa.then(function(data){ 
		        	$scope.data.btnCerrar(true);
		    	}); 
				
				
			}
			else {
				bcsComunes.ventanaMsg({
				    mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"),
				    conflictos:util.parseServerResponse(respuesta)
				  },idVentana);
		    }
		};
		
		var errorModificarEmprestito = function(respuesta)
		{
			bcsComunes.ventanaOverlay(idVentana,false);
			bcsComunes.ventanaMsg({
			    mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"),
			    conflictos:util.parseServerResponse(respuesta)
			  },idVentana);
			
			$scope.data.accionDisabled = true;
		};	   	
		

	});
}());
