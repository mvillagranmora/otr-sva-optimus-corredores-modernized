angular.module('ModuloOrdenes').controller('CrearTransaccionCtrl',
			function($scope,$translate,bcsComunes,bcsConfiguracionOrdenes,bcsConfiguracionOrdenesV2,bcsServiciosPlataformaMercado,
				bcsServicioOrden,
				bcsServicioPlataforma )
{

	var controllerName = 'CrearTransaccionCtrl';
	$scope.data = {};	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametrosVentana = bcsComunes.ventanaParams(idVentana);
	var condicionLiquidacionPrevia = null;
	
	$scope.transaccionActual = {};
	$scope.formData = {};

	$scope.data.oConfiguracionClasificacion = {};
	$scope.data.oConfiguracionTipoInstrumento = {};
	$scope.data.oConfiguracionOperacion = {};
	$scope.data.oConfiguracionBolsa = {};
	$scope.data.oConfiguracionBoveda = {};
	$scope.data.oConfiguracionCorredor = {};
	$scope.data.oConfiguracionOperadorCorredor = {};
	$scope.data.oConfiguracionOperadorContraparte = {};
	$scope.data.oConfiguracionInstrumentoTipoReajuste = {};
	$scope.data.oConfiguracionCondicionDeLiquidacion = {};
	$scope.data.oConfiguracionEmisor = {};
	$scope.data.oConfiguracionMonedaTransaccion = {};

	$scope.data.tipInsOpc = false;
	$scope.data.switchIndicadorBilateralDisabled = false;	
	$scope.data.oConfiguracionCorredor.itemPostFiltro = undefined;

	$scope.data.inputInstrumentoRequerido = true;	
	$scope.data.BaseTasaSimultaneaRvBcs=0;
	$scope.data.BaseTasaSimultaneaRvBec=0;
		
	$scope.formData.filtrosCorredorCorresponsal = ($scope.SesionLocale === 'PE') ? {
		IdEmpresa	: { field   : "IdEmpresa",operator: "eq", value   : $scope.SesionIdEmpresa },
	} : {
			IdPais		: { field   : "IdPais",operator: "eq", value   : "" },
			IdEmpresa	: { field   : "IdEmpresa",operator: "eq", value   : $scope.SesionIdEmpresa }
	}
	
	bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise('CUS.PrestamosRentaFija', $scope.SesionIdEmpresa, 'PLATAFORMA')
		 .then(function ( parametroPrestamosRentaFija ) {
			 $scope.formData.parametroPrestamosRentaFija = parametroPrestamosRentaFija.Valor;
			 if ( $scope.data.oConfiguracionClasificacion.idActual && $scope.data.oConfiguracionClasificacion.idActual === 'RF' && $scope.formData.parametroPrestamosRentaFija === 'N' ) {
				$scope.formData.filtrosOperacion.IdTipOperacion = { field: 'IdTipoOperacion', operator: 'neq', value: 'VCRF' };
			 }
		});	

	bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise('BaseTasaSimultaneaRvBcs', $scope.SesionIdEmpresa, 'ORDENES')
		.then(function ( parametroSimultanea ) {
			if (parametroSimultanea.Valor){
				$scope.data.BaseTasaSimultaneaRvBcs= parametroSimultanea.Valor;
			}else{
				$scope.data.BaseTasaSimultaneaRvBcs=30;
			}
	   });	
	   bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise('BaseTasaSimultaneaRvBec', $scope.SesionIdEmpresa, 'ORDENES')
	   .then(function ( parametroSimultanea ) {
		   if (parametroSimultanea.Valor){
			   $scope.data.BaseTasaSimultaneaRvBec= parametroSimultanea.Valor;
		   }else{
			   $scope.data.BaseTasaSimultaneaRvBec=30;
		   }
	  });	


	
	$scope.data.oConfiguracionClasificacion.idDefecto = parametrosVentana.IdClasificacionTipoInstrumento?parametrosVentana.IdClasificacionTipoInstrumento:'RV';
	
	$scope.formData.filtrosSistemaBursatil	=
	{
		IdEmpresa                      : {"field": "IdEmpresa",                      "operator":"eq", "value": $scope.SesionIdEmpresa},
		IdClasificacionTipoInstrumento : {"field": "IdClasificacionTipoInstrumento", "operator":"eq", "value": ''}
	};

	$scope.formData.filtrosClasificacionTipoInstrumento = {
			logic : "or",
			RV : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "RV"},
			DV : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "DV"},
			RF : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "RF"},
			IF : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "IF"},
	};
	
	if ($scope.SesionLocale === 'PE') {
		$scope.formData.filtrosClasificacionTipoInstrumento.RE = {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "RE"};
		$scope.data.oConfiguracionMonedaTransaccion.filtros = {
				logic : "or",
				USD : {"field": "IdMoneda","operator": "eq","value": "USD"},
				PEN : {"field": "IdMoneda","operator": "eq","value": "PEN"},
				EUR : {"field": "IdMoneda","operator": "eq","value": "EUR"}
		};
	}
	else {
		$scope.data.oConfiguracionMonedaTransaccion.filtros = {
				logic : "or",
				COP: {"field": "IdMoneda","operator": "eq","value": "COP"},
				USD : {"field": "IdMoneda","operator": "eq","value": "USD"},
				CLP : {"field": "IdMoneda","operator": "eq","value": "CLP"},
				EUR : {"field": "IdMoneda","operator": "eq","value": "EUR"},
				PEN : {"field": "IdMoneda","operator": "eq","value": "PEN"}
		};
	}
	
	$scope.formData.filtrosTipoInstrumento = {
			IdEstado 		: { "field"   : "IdEstado","operator": "eq", "value"   : "VIG" },
			IdBolsa 		: { "field"   : "IdBolsa","operator": "eq", "value"   : "" },
			IdClasificacionTipoInstrumento : { "field"   : "IdClasificacionTipoInstrumento","operator": "eq", "value"   : "" },
			EsTransaccion 	: { "field"   : "EsTransaccion","operator": "eq", "value"   : "S" }
	};
	$scope.formData.filtrosOperacion = {
			IdClasificacionTipoInstrumento : { "field"   : "IdClasificacionTipoInstrumento","operator": "eq", "value"   : "" },
			IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value"   : "" },
			EsTransaccion : { "field"   : "EsTransaccion","operator": "eq", "value"   : "S" },
			read : true
	}
	$scope.formData.filtrosBolsa = {
			IdInstrumento : { "field"   : "IdInstrumento","operator": "eq", "value"   : 0 },
	}
	$scope.formData.filtrosCorredora = {
			IdBolsa : { "field"   : "IdBolsa","operator": "eq", "value"   : '' },
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
	}
	$scope.formData.filtrosOperadorCorredor = {
			EstadoOperador : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
			
	}
	$scope.formData.filtrosOperadorCorredorContraparte = {
			EstadoOperador : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
	}
	$scope.formData.filtrosInstrumentoTipoReajuste = {
			IdInstrumento		: { "field"   : "IdInstrumento","operator": "eq", "value"   : "" },
			IdTipoInstrumento	: { "field"   : "IdTipoInstrumento","operator": "eq", "value"   : "" },
	}
	$scope.formData.filtrosEmisor = {
			IdEstado : { "field"   	: "IdEstado","operator": "eq", "value"   : "ACT" },
			EmiteIIF : { "field"   	: "EmiteIIF","operator": "eq", "value"   : "S" },
	}
	$scope.formData.filtrosLibroOrdenes = {
		EstadoLibroOrdenes 	: {field:"EstadoLibroOrdenes",operator:"eq",value:"ABT"},
		IdEmpresa: {field:"IdEmpresa",operator:"eq",value:$scope.SesionIdEmpresa},
		IdLibroOrdenes: {field:"IdLibroOrdenes",operator:"eq",value: ""}

	};
	
	
	//INICIALIZACION DE VARIABLES
	limpiar = function(limpiezaCompleta)
	{

		limpiezaCompleta = limpiezaCompleta || false;

		if(limpiezaCompleta){
			$scope.data.oConfiguracionCondicionDeLiquidacion.idActual = "T0";
		}else{
			$scope.formData.defectoOperacion = $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoOperacion;
			$scope.data.oConfiguracionCondicionDeLiquidacion.idActual = ($scope.transaccionActual.condicionDeLiquidacionSeleccionada 
				&& $scope.transaccionActual.condicionDeLiquidacionSeleccionada.IdCondicionDeLiquidacion) 
				|| "T0";
		}

		$scope.data.showTransaccionPlazo = false; //PARA SIMULTANEAS
		$scope.data.showCantidad = true; //PARA RF Y RV
		$scope.data.showPrecio = true; //PARA RF Y RV
		$scope.data.showMonto = true; //PARA RF Y RV
		$scope.data.showMontoVal = false;
		$scope.data.showPlazo = false;
		$scope.data.IngresoConfirmacion = 'I';
		$scope.formData.IdMonedaTransaccion = undefined;
		$scope.data.kngPlazo = 0;
		//IF
		$scope.data.showEmisor  = false;
		$scope.data.showTipoReajuste = false;
		$scope.data.showFungible = false;
		$scope.data.showFechaEmision = false;
		$scope.data.showValorRescate = false;
		$scope.data.showTamano = false;
		$scope.data.montoDisabled = true;
		//RF
		$scope.data.showTasa = false; //PARA IF Y RF, Venta corta, Simultaneas	
		$scope.data.monedaEnabled = false;
		$scope.data.corredorContraparteEnabled = true;
		
		$scope.data.tipInsOpc = false;
		$scope.data.inputInstrumentoRequerido = true;
		
		if ($scope.data.oConfiguracionBolsa.seleccionado && $scope.data.oConfiguracionBolsa.seleccionado.EsExtranjera === 'S'){
			$scope.data.corredorContraparteEnabled = false;
		}
		if ($scope.data.oConfiguracionTipoInstrumento.seleccionado && $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento === 'SIM'){
			$scope.data.showTransaccionPlazo = true;
			$scope.data.showTasa = true;
		}
		if ($scope.data.oConfiguracionTipoInstrumento.seleccionado && $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento === 'RE'){
			$scope.data.showTransaccionPlazo = true;
			$scope.data.showTasa = true;
			$scope.data.monedaEnabled = true;
			$scope.data.corredorContraparteEnabled = false;
			$scope.data.oConfiguracionCorredor.idActual = $scope.SesionIdCorredoraSistema;
		}
		if ($scope.data.oConfiguracionClasificacion.seleccionado && $scope.data.oConfiguracionClasificacion.seleccionado.IdClasificacionTipoInstrumento === 'RF'){
			$scope.data.showTasa = true;
			$scope.data.montoDisabled = false;
			$scope.data.showMontoVal = true;
		};		
		if ($scope.data.oConfiguracionClasificacion.seleccionado && $scope.data.oConfiguracionClasificacion.seleccionado.IdClasificacionTipoInstrumento === 'RV'){
			$scope.data.monedaEnabled = true;	
			$scope.data.NemotecnicoRaiz = undefined;		
		}
		if ($scope.data.oConfiguracionClasificacion.seleccionado && $scope.data.oConfiguracionClasificacion.seleccionado.IdClasificacionTipoInstrumento === 'IF'){
			$scope.data.showFechaEmision = true;
			$scope.data.showTasa = true;
			$scope.data.showFungible = true;
			$scope.data.showValorRescate = true;
			$scope.data.showEmisor  = true;
			$scope.data.showTipoReajuste = true;
			$scope.data.showCantidad = true;
			$scope.data.showPrecio = false;
			$scope.data.montoDisabled = false;
			$scope.data.showMontoVal = true;
			$scope.data.showPlazo = true;
			$scope.data.monedaEnabled = true;
		}
		
		if ($scope.data.oConfiguracionClasificacion.seleccionado && $scope.data.oConfiguracionClasificacion.seleccionado.IdClasificacionTipoInstrumento === 'DV'){
			
			$scope.data.showTamano = true;
			if ($scope.data.oConfiguracionTipoInstrumento.seleccionado.CodigoTipoInstrumento === 'FUT_BCU'){
				$scope.data.showPrecio = false;
				$scope.data.showMontoVal = true;
				$scope.data.showTasa = true;
			}			
			$scope.data.inputInstrumentoRequerido = false;
			if($scope.data.oConfiguracionTipoInstrumento.seleccionado.IdGrupoProducto === 'FU' || $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdGrupoProducto === 'OP')
				$scope.data.tipInsOpc = true;
			
		}
		
		
		$scope.transaccionActual.tipoCambioCorresponsal = undefined;
		$scope.transaccionActual.idCorredorCorresponsal = undefined;
		$scope.transaccionActual.idMonedaLiquidacionCorresponsal = undefined;
		$scope.transaccionActual.montoLiquidacionCorresponsal = undefined;
		$scope.transaccionActual.gastosCorresponsal = undefined; 
		
		$scope.data.IdMonedaPrecioDisplay = '';
		$scope.data.IdMonedaMontoDisplay = '';
		$scope.data.IdMonedaRescateDisplay = '';
		$scope.data.enabledFungible = true;
		
		bcsConfiguracionOrdenes.CONFIGURACION.promise.then(function(result){
			if (bcsConfiguracionOrdenes.CONFIGURACION.CrearTransaccionDefectoFueraRueda === 'S') {
				$scope.data.esFueraRueda = true;
			} else {
				$scope.data.esFueraRueda = false;
			}
		})
		
		$scope.data.esFueraRueda = undefined;
		$scope.data.indicadorDCV = undefined;		
		$scope.data.folio = undefined;
		$scope.data.folioPlazo = undefined;
		$scope.data.marcaNegociacion = undefined;
		$scope.data.switchFungible = false;
		$scope.data.switchVentaCorta = ( $scope.data.oConfiguracionOperacion.seleccionado && $scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion === 'VCRF') ? true : false;
		$scope.data.kngCantidad = undefined;
		$scope.data.kngInteres = undefined;
		$scope.data.kngReajuste = undefined;			
		$scope.data.kngPrecio = undefined;
		$scope.data.kngPrecioRaiz= undefined;
		$scope.data.kngPrecioAplazo = undefined;
		$scope.data.kngValorRescate = undefined;
		$scope.data.kngTasa = undefined;
		$scope.data.kngMonto = undefined;
		$scope.data.kngMontoRaiz= undefined;
		$scope.data.kngTamano = undefined;
		$scope.data.kngCorte = undefined;
		$scope.data.kngIntereses = undefined;
		$scope.data.kngReajustes = undefined;
		
		$scope.data.kngMontoPlazo = undefined;
		$scope.data.buscarInstrumento = "-1";
		$scope.data.oConfiguracionInstrumentoTipoReajuste.idActual = undefined;
		$scope.formData.filtrosInstrumentoTipoReajuste.IdInstrumento.value = 0;
		$scope.formData.filtrosInstrumentoTipoReajuste.IdTipoInstrumento.value = " ";
		$scope.data.oConfiguracionEmisor.idActual = 0;
						
		$scope.data.kngFechaSistema = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
		$scope.data.kngHora = new Date($scope.SesionFechaSistema + " " + $scope.SesionHoraSistema)
		$scope.data.kngFechaEmision = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
		$scope.formData.fechaSistema = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");

	};
	
	$scope.$watch('data.oConfiguracionMonedaTransaccion.seleccionado',function(newval,oldval) {
		if (newval) {
			if ($scope.data.oConfiguracionClasificacion.seleccionado.IdClasificacionTipoInstrumento === 'RV') {
				obtenerDatosNemoNegociado();				
			}					
			setMonedasDecimal();		
		}
	},true);
	
	
	
	
	$scope.$watch('data.oConfiguracionClasificacion.seleccionado',function(newval,oldval) {
		if (newval) {
			limpiar();
			refrescarSistemaBursatil();
			
			if ($scope.data.oConfiguracionClasificacion.idActual === 'RF') {
				if($scope.SesionLocale==='PE'){
					$scope.formData.showTipoInstrumento = true;
				}else{
					$scope.formData.showTipoInstrumento = false;
				}
				$scope.formData.defectoTipoInstrumento = undefined;
				$scope.formData.filtrosOperacion.IdTipoInstrumento.value = 0;
				$scope.formData.filtrosOperacion.read = true;
				$scope.formData.defectoOperacion = 'COMRF';
				if ( $scope.formData.parametroPrestamosRentaFija && $scope.formData.parametroPrestamosRentaFija === 'N' ) {
					$scope.formData.filtrosOperacion.IdTipOperacion = { field: 'IdTipoOperacion', operator: 'neq', value: 'VCRF' };
				}
			}
			else if ($scope.data.oConfiguracionClasificacion.idActual === 'IF') {
				$scope.formData.showTipoInstrumento = false;
				$scope.formData.defectoTipoInstrumento = 'PAG';
				$scope.formData.defectoOperacion = 'COMIF';
			}
			else if ($scope.data.oConfiguracionClasificacion.idActual === 'DV') {
				
				if($scope.SesionLocale==='PE'){
					$scope.formData.defectoTipoInstrumento = 'OPC';
					$scope.formData.defectoOperacion = 'COMOPC';
				}else{
					$scope.formData.defectoTipoInstrumento = 'FDO';
					$scope.formData.defectoOperacion = 'COMFUT';
				}
				$scope.formData.showTipoInstrumento = true;
				
			}
			else if ($scope.data.oConfiguracionClasificacion.idActual ==='RV') {
				if($scope.SesionLocale==='PE'){
					$scope.formData.defectoTipoInstrumento = 'ES';
				}else{
					$scope.formData.defectoTipoInstrumento = 'ACN';
				}
				$scope.formData.showTipoInstrumento = true;
				$scope.formData.defectoOperacion = 'COMACN';
			}
			if ($scope.data.oConfiguracionClasificacion.idActual === 'RE') {
				$scope.formData.defectoTipoInstrumento = 'RRV';
				$scope.formData.defectoOperacion = 'VENRRV';
				$scope.formData.defectoBolsa = 'BVL';
				$scope.formData.showTipoInstrumento = true;
			}
		}
	},true);
		
	$scope.$watch('data.oConfiguracionTipoInstrumento.seleccionado',function(newval,oldval) {		
		if (newval) {
			limpiar();
			setMonedasDecimal();
			if($scope.data.oConfiguracionTipoInstrumento.idActual ==='RRF'){
				$scope.formData.defectoOperacion = 'VENRRF';
			}
			if($scope.data.oConfiguracionTipoInstrumento.idActual ==='RRV'){
				$scope.formData.defectoOperacion = 'VENRRV';
			}
			if($scope.data.oConfiguracionTipoInstrumento.idActual ==='MIL' || $scope.data.oConfiguracionTipoInstrumento.idActual ==='ACX'){
				if(!$scope.transaccionActual.condicionDeLiquidacionSeleccionada.IdCondicionDeLiquidacion){
					$scope.data.oConfiguracionCondicionDeLiquidacion.idActual = "T3";
				}
			}
			if($scope.data.oConfiguracionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento === 'DV'){									
				$scope.formData.defectoOperacion = $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoOperacion;				
				$scope.data.inputInstrumentoRequerido = false;
				if($scope.data.oConfiguracionTipoInstrumento.seleccionado.IdGrupoProducto === 'FU' || $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdGrupoProducto === 'OP')
					$scope.data.tipInsOpc = true;
				else
					$scope.data.tipInsOpc = false;
				
			}			
			
		}
	},true);
	
	$scope.$watch('data.oConfiguracionOperacion.seleccionado', function(newval,oldval){
		
		if ($scope.data.oConfiguracionClasificacion.idActual === 'RF'){
			$scope.data.switchVentaCortaHabilita = true;
			$scope.data.switchVentaCorta = ($scope.data.oConfiguracionOperacion.seleccionado.EsVentaCorta === 'S');
			
		} else {
			if ($scope.data.oConfiguracionOperacion.seleccionado && $scope.data.oConfiguracionOperacion.seleccionado.EsVentaCorta === 'S'){
				$scope.data.switchVentaCortaHabilita = false;	 
			}
			else {
				$scope.data.switchVentaCortaHabilita = true;
			}
			$scope.data.switchVentaCorta = false;			
		}
		

	});
	
	
	$scope.$watch('data.oConfiguracionCorredor.seleccionado', function (newValue, oldValue) {
		if (newValue){
			if ($scope.SesionIdPersonaCorredora ===  $scope.data.oConfiguracionCorredor.seleccionado.IdPersona && $scope.data.oConfiguracionTipoInstrumento.seleccionado.CodigoTipoInstrumento != 'SIM')
			{
				$scope.data.operadorContraparteDisabled = false;
				$scope.data.switchOrdenDirectaOD = true;
			}
			else
			{
				$scope.data.operadorContraparteDisabled = true;
				$scope.data.switchOrdenDirectaOD = false;
				$scope.data.switchOrdenDirectaOE = false;
			}
		}
	});
	
	
	$scope.$watch('data.oConfiguracionBolsa.seleccionado', function (newValue, oldValue) {
		if (newValue)
		{	
			//limpiar();
			refrescarSistemaBursatil();
			
			if ($scope.data.oConfiguracionBolsa.seleccionado.EsExtranjera === 'S')
			{	
				//Toda transaccion del exterior debe venir con el check bilateral por defecto
				$scope.data.switchIndicadorBilateral = true;
				$scope.data.switchIndicadorBilateralDisabled = true;
				
				//Cuando transaccion es extranjera, se limpia y deshabilita "Corredor contraparte" y "OD" 
				$scope.data.oConfiguracionCorredor.itemPostFiltro = undefined;
				$scope.data.switchOrdenDirectaOD = false;
				
			}
			else
			{
				//Transacciona nacional habilita "Bilateral" desmarcado
				$scope.data.switchIndicadorBilateral = false;
				$scope.data.switchIndicadorBilateralDisabled = false;				
				$scope.data.oConfiguracionCorredor.itemPostFiltro = 1;
			}
		}
	});

	$scope.$watch('data.oConfiguracionOperadorCorredor.seleccionado', function (newValue, oldValue) {
		if(newValue){		
		
			let IdLibroOrdenes = $scope.data.oConfiguracionOperadorCorredor.seleccionado.IdLibroOrdenes;					
			if(IdLibroOrdenes === null || IdLibroOrdenes === 0){
				IdLibroOrdenes = $scope.formData.idDefectoLibroOrdenes;
			}
			$scope.formData.filtrosLibroOrdenes.IdLibroOrdenes.value = IdLibroOrdenes;
			
			
		
		}
	});
	
	

	var refrescarSistemaBursatil = function()
	{	
		if ($scope.data.oConfiguracionClasificacion.idActual === 'RF')
		{
			
			if ($scope.data.oConfiguracionBolsa.seleccionado.TieneFormaNegociacion === 'S') 
			{
				$scope.data.activadoSistemaBursatil = true;
				$scope.formData.filtrosSistemaBursatil.IdClasificacionTipoInstrumento.value  = "RF";
				$scope.formData.filtrosSistemaBursatil.read = true;
			}
			else
			{
				$scope.data.activadoSistemaBursatil = false;
				$scope.data.idSistemaBursatil = undefined;
			}
			
		}
		else
		{
			$scope.data.activadoSistemaBursatil = false;
			$scope.data.idSistemaBursatil = 'MANUAL';
		}		
	};
	
	
	$scope.$watch('data.oConfiguracionInstrumentoTipoReajuste.seleccionado',function (newval,oldval){
		if ($scope.data.oConfiguracionInstrumentoTipoReajuste.seleccionado){
			$scope.data.kngCorte = $scope.data.oConfiguracionInstrumentoTipoReajuste.seleccionado.CorteMinimo;
			if ($scope.data.instrumento && ($scope.data.instrumento.Nemotecnico === 'PRBC' || $scope.data.instrumento.Nemotecnico === 'PDBC')){
				$scope.data.showValorRescate = false;
			}
			else if ($scope.data.instrumento && $scope.data.oConfiguracionClasificacion.idActual === 'IF') {
				$scope.data.kngValorRescate = undefined;
				$scope.data.showValorRescate = true;
			}
			else {
				$scope.data.showValorRescate = false;
				$scope.data.kngValorRescate = undefined;
			}
			setMonedasDecimal();
		}
	});
	$scope.$watch('data.oConfiguracionEmisor.seleccionado',function(newval,oldval){
		if ($scope.data.oConfiguracionEmisor.seleccionado){
			if ($scope.data.oConfiguracionClasificacion.idActual === 'IF'){
				if ($scope.data.oConfiguracionEmisor.seleccionado.EmiteFungible === "N"){
					$scope.data.switchFungible = false;
					$scope.data.enabledFungible = true; 
					$scope.data.kngCantidad = 0;
				}
				else {
					$scope.data.switchFungible = true;
					$scope.data.enabledFungible = false; 
					$scope.data.kngCantidad = 1;
				}
			}
		}
	});
	
	var setMonedasDecimal = function (){
		if ($scope.data.instrumento){
			if ($scope.data.instrumento.IdClasificacionTipoInstrumento === 'RF'){
				$scope.data.IdMonedaPrecioDisplay = '';
				$scope.data.IdMonedaMontoDisplay = $scope.data.instrumento.IdMoneda;
				$scope.data.IdMonedaRescateDisplay = $scope.data.instrumento.IdMoneda;
			}
			else if ($scope.data.instrumento.IdClasificacionTipoInstrumento === 'IF'){
				$scope.data.IdMonedaPrecioDisplay = $scope.data.instrumento.IdMoneda;
				$scope.data.IdMonedaMontoDisplay = $scope.data.oConfiguracionMonedaTransaccion.seleccionado.IdMoneda != $scope.data.instrumento.IdMoneda ? $scope.data.oConfiguracionMonedaTransaccion.seleccionado.IdMoneda : $scope.data.instrumento.IdMoneda;
				$scope.data.IdMonedaRescateDisplay = $scope.data.oConfiguracionInstrumentoTipoReajuste.seleccionado.IdTipoReajuste;
			}
			else{		
				$scope.data.IdMonedaPrecioDisplay = $scope.data.instrumento.IdMoneda;
				$scope.data.IdMonedaMontoDisplay = $scope.data.instrumento.IdMoneda;
				$scope.data.IdMonedaRescateDisplay = $scope.data.instrumento.IdMoneda;
			}
			if ($scope.data.oConfiguracionClasificacion.seleccionado.IdClasificacionTipoInstrumento === 'RV'&& $scope.data.oConfiguracionMonedaTransaccion.seleccionado.IdMoneda && $scope.data.instrumento.IdMoneda != $scope.data.oConfiguracionMonedaTransaccion.seleccionado.IdMoneda){
				$scope.data.IdMonedaPrecioDisplay = $scope.data.oConfiguracionMonedaTransaccion.seleccionado.IdMoneda;
				$scope.data.IdMonedaMontoDisplay = $scope.data.oConfiguracionMonedaTransaccion.seleccionado.IdMoneda;
				$scope.data.IdMonedaRescateDisplay = $scope.data.oConfiguracionMonedaTransaccion.seleccionado.IdMoneda;				
			}
					
			$scope.data.IdDecimalCantidadDisplay 		= (util.bcs_formato(0,'GEN','Cantidad',$scope.data.instrumento.IdMoneda		,$scope.data.instrumento.IdTipoInstrumento,'')).cantidadDecimales;
			$scope.data.IdDecimalPrecioDisplay 			= (util.bcs_formato(0,'GEN','Precio',$scope.data.instrumento.IdMoneda		,$scope.data.instrumento.IdTipoInstrumento,'')).cantidadDecimales;			
			$scope.data.IdDecimalTasaDisplay 		    = (util.bcs_formato(0,'GEN','Tasa',$scope.data.instrumento.IdMoneda		    ,$scope.data.instrumento.IdTipoInstrumento,'')).cantidadDecimales;			
			$scope.data.IdDecimalMontoDisplay			= (util.bcs_formato(0,'GEN','Monto',$scope.data.IdMonedaMontoDisplay		,$scope.data.instrumento.IdTipoInstrumento,'')).cantidadDecimales;
			$scope.data.IdDecimalRescateDisplay 		= (util.bcs_formato(0,'GEN','Monto',$scope.data.IdMonedaRescateDisplay		,$scope.data.instrumento.IdTipoInstrumento,'')).cantidadDecimales;
			$scope.data.IdDecimalInteresesDisplay		= (util.bcs_formato(0,'GEN','Monto',$scope.data.IdMonedaMontoDisplay		,$scope.data.instrumento.IdTipoInstrumento,'')).cantidadDecimales;
			$scope.data.IdDecimalReajustesDisplay		= (util.bcs_formato(0,'GEN','Monto',$scope.data.IdMonedaMontoDisplay		,$scope.data.instrumento.IdTipoInstrumento,'')).cantidadDecimales;
			
			$scope.data.IdMonedaPrecioRaizDisplay 	= $scope.data.IdMonedaRaiz;
			$scope.data.IdDecimalPrecioTransadoDisplay	= (util.bcs_formato(0,'GEN','Precio',$scope.data.instrumento.IdMoneda		,$scope.data.instrumento.IdTipoInstrumento,'')).cantidadDecimales;			
			$scope.data.IdMonedaMontoTransadoDisplay 	= $scope.data.IdMonedaRaiz;
			$scope.data.IdDecimalMontoRaizDisplay	= (util.bcs_formato(0,'GEN','Monto',$scope.data.IdMonedaMontoTransadoDisplay		,$scope.data.instrumento.IdTipoInstrumento,'')).cantidadDecimales;			
		
		}
	};

	obtenerPuntas = function(IdInstrumentoNegociado){				
		if (!$scope.data.instrumento || !$scope.data.oConfiguracionClasificacion.seleccionado.IdClasificacionTipoInstrumento === 'RV' || !$scope.data.oConfiguracionMonedaTransaccion.seleccionado || !$scope.transaccionActual.condicionDeLiquidacionSeleccionada){ 
			return;
		}
		else
		{
			var getPreciosDataSource = function(IdInstrumentoNegociado) {
			return new bcs.DataSource({
				type : "call",
				definition : {
								serverFiltering 	: false, // Filtrado de datos en el servidor.
								serverPaging 		: false, // Paginado de los datos en el servidor.
								serverSorting 		: false, // Ordenamiento de los datos en el servidor.						
								callParameters      : {
								p_IncluyeNemoNegociacion: "S",
								p_Instrumentos : [{ 
													IdEmpresa 					: $scope.SesionIdEmpresa,
													IdInstrumento				: IdInstrumentoNegociado,
													IdBolsa 					: $scope.data.oConfiguracionBolsa.seleccionado.IdBolsa, 
													Fecha 						: $scope.SesionFechaSistema,
													IdCondicionDeLiquidacion 	: $scope.transaccionActual.condicionDeLiquidacionSeleccionada.IdCondicionDeLiquidacion
								
													}
												]
											},
								callTransaction 	: 3252,
								callResultSet 		: "P_OUT_INSTRUMENTO",
								schema: {
									model: {
										fields: {
													IdInstrumento	: { type : "number"},
													Fecha 			: { type : "string"},
													IdBolsa 		: { type : "string"},
													PrecioCierre 	: { type : "number"},		    		    			
													PrecioMercado	: { type : "number"},
									 				TasaMercado		: { type : "number"},
												}
											}
									}
							}
				});
			}
			dsPrecios = getPreciosDataSource($scope.data.IdInstrumentoNegociado);
			dsPrecios.fetch().then(function(){	
				var precios = dsPrecios.data();
				if (dsPrecios.data().length > 0){
					$scope.data.kngPrecio = precios[0].PrecioMercado;
					$scope.data.instrumento.PuntaCompra = precios[0].PuntaCompra;
					$scope.data.instrumento.PuntaVenta =  precios[0].PuntaVenta
					$scope.data.instrumento.PrecioCierre = precios[0].PrecioCierre;
					$scope.data.instrumento.IdMoneda = precios[0].IdMoneda;
				}
				else
				{
					$scope.data.instrumento.PrecioCierre = undefined;
					$scope.data.kngPrecio = undefined;
					$scope.data.instrumento.PuntaCompra = undefined;
					$scope.data.instrumento.PuntaVenta =  undefined;
					$scope.data.instrumento.IdMoneda = undefined;
				}										
			})				
		};
	};
	
	obtenerParidades = function(){

		if (!$scope.data.instrumento || !$scope.data.instrumento.IdMoneda || !$scope.data.oConfiguracionMonedaTransaccion.seleccionado) 
			if ($scope.data.instrumento.IdMoneda === $scope.data.IdMonedaRaiz)
			return;
			
		if ($scope.data.oConfiguracionClasificacion.seleccionado.IdClasificacionTipoInstrumento === 'RV') {
			var idMonedaOrigen = $scope.data.IdMonedaRaiz;
			var idMonedaDestino = $scope.data.oConfiguracionMonedaTransaccion.seleccionado.IdMoneda;
			var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdTrabajo : 0,
				p_CalculosEntrada : [{
					IdTrabajo : 0,
					IdInterno : 0,
					Fecha : $scope.SesionFechaSistema,
					IdMonedaOrigen : idMonedaOrigen,
					IdMonedaDestino : idMonedaDestino,
					ValorCambioLocalModificado : 0,
					MontoOrigen : 1,
					IdUso : 'ORDENES'
				}]
			};
		
			var promise = bcsServiciosPlataformaMercado.CalcularParidadYTipoCambio(datos);	
			var combinatoria = idMonedaOrigen + idMonedaDestino;		

				promise.then(function(result){
						if (result.p_out_CalculosSalida[0].Resultado === 'RECHAZO') {
							ordenOP.datosForm.paridadReferencia = $translate.instant("UI_NO_SE_ENCONTRO_PARIDAD") + ":" + idMonedaOrigen + " - " + idMonedaDestino;
						}
						else {
							var montoCalculado = 	Math.round(($scope.data.kngCantidad) * ($scope.data.kngPrecio));
							var miMonto = Math.round($scope.data.kngMonto);
							switch (combinatoria) {
		  						case "CLPCLP":			
							 		$scope.data.kngPrecio = ($scope.data.kngPrecioRaiz) / (result.p_out_CalculosSalida[0].FactorOrigenDestino || 1);
							 		$scope.data.kngMonto  = ($scope.data.kngCantidad || 0) * ($scope.data.kngPrecioRaiz/result.p_out_CalculosSalida[0].FactorOrigenDestino || 1);
		  							break;																	  					
		  						case "USDUSD":
									if (montoCalculado != miMonto){
										$scope.data.kngPrecioRaiz = ($scope.data.kngPrecio) / (result.p_out_CalculosSalida[0].FactorLocalCambio || 1);
							 			$scope.data.kngMontoRaiz  = ($scope.data.kngCantidad || 0) * ($scope.data.kngPrecio) / (result.p_out_CalculosSalida[0].FactorLocalCambio || 1);
							 			$scope.data.kngMonto  = ($scope.data.kngCantidad || 0) * ($scope.data.kngPrecioRaiz)/(result.p_out_CalculosSalida[0].FactorLocalCambio || 1);
									}else{								 		
							 			$scope.data.kngPrecio = ($scope.data.kngPrecioRaiz || 0) * (result.p_out_CalculosSalida[0].FactorOrigenCambio || 0);
							 			$scope.data.kngMonto  = ($scope.data.kngCantidad || 0) * ($scope.data.kngPrecioRaiz*result.p_out_CalculosSalida[0].FactorOrigenCambio || 0);
							 		}
							 		break;		  						
		  						
		  						case "CLPUSD":
								case "COPCOP":									
									if (montoCalculado != $scope.data.kngMonto){
										$scope.data.kngPrecioRaiz = ($scope.data.kngPrecio) / (result.p_out_CalculosSalida[0].FactorOrigenDestino || 1);
							 			$scope.data.kngMontoRaiz  = ($scope.data.kngCantidad || 0) * ($scope.data.kngPrecio) / (result.p_out_CalculosSalida[0].FactorOrigenDestino || 1);
							 			$scope.data.kngMonto  = ($scope.data.kngCantidad || 0) * ($scope.data.kngPrecioRaiz) * (result.p_out_CalculosSalida[0].FactorOrigenDestino || 1);
									}else{																													  					
							 			$scope.data.kngPrecio = ($scope.data.kngPrecioRaiz) * (result.p_out_CalculosSalida[0].FactorOrigenDestino || 1);
							 			$scope.data.kngMonto  = ($scope.data.kngCantidad || 0) * ($scope.data.kngPrecioRaiz * result.p_out_CalculosSalida[0].FactorOrigenDestino || 1);
							 		}
						 			break;		
							 	case "USDCLP":  	
									if (montoCalculado != $scope.data.kngMonto){
										$scope.data.kngPrecioRaiz = ($scope.data.kngPrecio) / (result.p_out_CalculosSalida[0].FactorOrigenDestino || 1); //tambien puede ser así ($scope.data.kngPrecio) * (result.p_out_CalculosSalida[0].FactorLocalCambio || 1);
							 			$scope.data.kngMontoRaiz  = ($scope.data.kngCantidad || 0) * ($scope.data.kngPrecio) / (result.p_out_CalculosSalida[0].FactorOrigenDestino || 1); 
							 			$scope.data.kngMonto  = ($scope.data.kngCantidad || 0) * ($scope.data.kngPrecioRaiz)*(result.p_out_CalculosSalida[0].FactorOrigenDestino || 1);
									}else{								 		
							 			$scope.data.kngPrecio = ($scope.data.kngPrecioRaiz || 0) * (result.p_out_CalculosSalida[0].FactorOrigenDestino || 0);
							 			$scope.data.kngMonto  = ($scope.data.kngCantidad || 0) * ($scope.data.kngPrecioRaiz*result.p_out_CalculosSalida[0].FactorOrigenDestino || 0);
							 		}
							 		break;
							 	case "COPUSD":  		
							 		$scope.data.kngPrecio = ($scope.data.kngPrecioRaiz || 0) * (result.p_out_CalculosSalida[0].FactorOrigenCambio || 0);
							 		$scope.data.kngMonto  = ($scope.data.kngCantidad || 0) * ($scope.data.kngPrecioRaiz*result.p_out_CalculosSalida[0].FactorOrigenCambio || 0);
							 		break;		
							 	default:
							 		$scope.data.kngPrecio = undefined;
							 		$scope.data.kngMonto = undefined;	
							 		break;							 			 				 		
							}		
						}
					});

			}
	};


	$scope.$watch('transaccionActual.condicionDeLiquidacionSeleccionada.IdCondicionDeLiquidacion', function(newval,oldval){

        if(newval){
            condicionLiquidacionPrevia = newval;
        }

        if(!newval && condicionLiquidacionPrevia){
            $scope.data.oConfiguracionCondicionDeLiquidacion.idActual = condicionLiquidacionPrevia;
        }

		obtenerDatosNemoNegociado();		
	},true);
	
	obtenerDatosNemoNegociado = function(){				
		if (!$scope.data.instrumento || !$scope.data.oConfiguracionClasificacion.seleccionado.IdClasificacionTipoInstrumento === 'RV' || !$scope.data.oConfiguracionMonedaTransaccion.seleccionado || !$scope.transaccionActual.condicionDeLiquidacionSeleccionada){ 
			return;
		}
		else
		{	var parametrosSP = {
				p_DatosInstrumentoConsulta : [{
					IdInstrumentoRaiz	: $scope.data.instrumento.IdInstrumento,
					IdMoneda			: $scope.data.oConfiguracionMonedaTransaccion.seleccionado.IdMoneda,
					IdCondLiquidacion	: $scope.transaccionActual.condicionDeLiquidacionSeleccionada.IdCondicionDeLiquidacion
					}]
			}

		bcsServicioOrden.ConsultarNemoNegociacion(parametrosSP).$promise.then(function(result){			
			if (result.P_OUT_ERRORESCONTADOR == 0 ){
				$scope.data.NemotecnicoRaiz = result.P_InstrumentoSalida[0].Nemotecnico;
				$scope.data.IdInstrumentoNegociado = result.P_InstrumentoSalida[0].IdInstrumento;
				obtenerPuntas($scope.data.IdInstrumentoNegociado);
			}
			else
			{
			bcsComunes.ventanaMsg({
								mensaje:$translate.instant("UI_INSTRUMENTO_NEGOCIACION_NO_EXISTE"),
								conflictos:util.parseServerResponse(result)}
								,idVentana);
			}
		})		 
		}			
	};	

	$scope.$watchGroup(['data.kngPrecio','data.kngCantidad','data.kngTasa','data.kngMonto','data.kngPlazo','data.kngValorRescate'], function(newval,oldval) {		
		if ($scope.data.instrumento === undefined){
			return;
		}
		
		if ($scope.data.instrumento.IdInstrumento === undefined){
			return;
		}
		
		var datos = new Object();
		var clasificacion = ($scope.data.oConfiguracionClasificacion.seleccionado.IdClasificacionTipoInstrumento === 'DV' 
							&& $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento === "FBC")?
							"RF":$scope.data.oConfiguracionClasificacion.seleccionado.IdClasificacionTipoInstrumento;

		switch(clasificacion) {
			case 'IF':
				if ($scope.data.instrumento.IdClasificacionTipoInstrumento && $scope.data.instrumento.IdClasificacionTipoInstrumento === 'IF' && 
					($scope.data.instrumento.Nemotecnico === 'PRBC' || $scope.data.instrumento.Nemotecnico === 'PDBC' || !$scope.data.switchFungible)){
						$scope.data.kngValorRescate = $scope.data.kngCorte * $scope.data.kngCantidad;
					}
					break;

			case 'RF':
				//aqui iba la valorizacion
				break;
			case 'RE':
				if ($scope.data.oConfiguracionOperacion.seleccionado.IdTipoInstrumento === 'RRF'){
					$scope.data.kngMonto = ($scope.data.kngCantidad || 0) * ($scope.data.kngPrecio/100 || 0);
					break;
				}
			case 'RV':					
				if ($scope.data.IdMonedaRaiz === $scope.data.oConfiguracionMonedaTransaccion.seleccionado.IdMoneda){	
					$scope.data.kngPrecioRaiz = $scope.data.kngPrecio;
					if($scope.data.IdMonedaRaiz == "USD"){
						$scope.data.kngMonto = (($scope.data.kngCantidad || 0) * ($scope.data.kngPrecioRaiz || 0) * ($scope.data.kngTamano || 1));
						$scope.data.kngMontoRaiz = (($scope.data.kngCantidad || 0) * ($scope.data.kngPrecioRaiz || 0) * ($scope.data.kngTamano || 1));
					}else{
						$scope.data.kngMonto = (($scope.data.kngCantidad || 0) * ($scope.data.kngPrecioRaiz || 0) * ($scope.data.kngTamano || 1)).toFixed(0);
						$scope.data.kngMontoRaiz = (($scope.data.kngCantidad || 0) * ($scope.data.kngPrecioRaiz || 0) * ($scope.data.kngTamano || 1)).toFixed(0);
					}
				}
				else{
					if (oldval[1] != undefined && newval[1] == $scope.data.kngPrecio  && ($scope.data.instrumento.IdMoneda == $scope.data.oConfiguracionMonedaTransaccion.seleccionado.IdMoneda))
						return;
						bcsComunes.ventanaOverlay(idVentana,true);
						obtenerParidades();
			 			bcsComunes.ventanaOverlay(idVentana,false);
			 			setMonedasDecimal();											
				}
				break;
			case 'DV':
				$scope.data.kngMonto = (($scope.data.kngCantidad || 0) * ($scope.data.kngPrecio || 0) * ($scope.data.kngTamano || 1)).toFixed(0);
				break;
		}
	} );
	
	
	$scope.$watch('data.instrumento.IdInstrumento', function(newval,oldval){

		if (!$scope.data.instrumento || !$scope.data.instrumento.IdInstrumento)
			return;
		if ( $scope.data.switchVentaCorta && $scope.data.instrumento.IdClasificacionTipoInstrumento === 'RF'
			&& ( $scope.data.instrumento.IdMonedaEmision.trim() === '' || $scope.data.instrumento.IdMonedaEmision === undefined || $scope.data.instrumento.IdMonedaEmision === 'undefined' || $scope.data.instrumento.IdMonedaEmision === null ) ) {
			bcsComunes.msg('error', $translate.instant("UI_ERROR"), $translate.instant('UI_INSTRUMENTO_SIN_MONEDA_EMISION'));
			return;
		}

		$scope.data.oConfiguracionCorredor.idPostFiltro = $scope.data.oConfiguracionCorredor.idActual;
		$scope.formData.filtrosInstrumentoTipoReajuste.IdInstrumento.value = parseInt($scope.data.instrumento.IdInstrumento);
		$scope.formData.filtrosInstrumentoTipoReajuste.IdTipoInstrumento.value = $scope.data.instrumento.IdTipoInstrumento;
		$scope.formData.filtrosBolsa.IdInstrumento.value = $scope.data.instrumento.IdInstrumento;
		$scope.formData.IdMonedaTransaccion = $scope.data.instrumento.IdMoneda;
		setMonedasDecimal();
		
		$scope.data.kngTamano = $scope.data.instrumento.Tamano;
		$scope.data.kngCorte = $scope.data.instrumento.CorteMinimo;
			
		if ($scope.data.oConfiguracionClasificacion.seleccionado.IdClasificacionTipoInstrumento === 'IF' || $scope.data.oConfiguracionClasificacion.seleccionado.IdClasificacionTipoInstrumento === 'RF'){
			if ($scope.data.instrumento.IdEmisor !== ""){
				$scope.data.oConfiguracionEmisor.idActual = $scope.data.instrumento.IdEmisor;
				$scope.data.showEmisor = false;
			}
			else {
				$scope.data.showEmisor = true;
			}
			
			if ($scope.data.instrumento.IdTipoInstrumento === 'BR' || $scope.data.instrumento.IdTipoInstrumento === 'CER' || $scope.data.instrumento.IdTipoInstrumento === 'XER' || $scope.data.instrumento.IdTipoInstrumento === 'ZER') {
				$scope.data.showPrecio = false;
			}
			else {
				$scope.data.showPrecio = true;
			}
		}
		
		if ($scope.data.oConfiguracionClasificacion.seleccionado.IdClasificacionTipoInstrumento === 'RV' || $scope.data.oConfiguracionClasificacion.seleccionado.IdClasificacionTipoInstrumento === 'DV') {
			$scope.data.kngPrecio = $scope.data.instrumento.PrecioCierre;
			$scope.data.kngPrecioRaiz = $scope.data.instrumento.PrecioCierre;
			$scope.data.IdMonedaRaiz = $scope.data.instrumento.IdMoneda; 
		}

		if ($scope.data.oConfiguracionInstrumentoTipoReajuste.seleccionado){
			$scope.data.kngCorte = $scope.data.oConfiguracionInstrumentoTipoReajuste.seleccionado.CorteMinimo;
			if ($scope.data.instrumento.IdClasificacionTipoInstrumento && $scope.data.instrumento.IdClasificacionTipoInstrumento === 'IF' && 
					($scope.data.instrumento.Nemotecnico === 'PRBC' || $scope.data.instrumento.Nemotecnico === 'PDBC' || !$scope.data.switchFungible)){
				$scope.data.showValorRescate = false;
			}
			if ($scope.data.instrumento.IdClasificacionTipoInstrumento && $scope.data.instrumento.IdClasificacionTipoInstrumento === 'IF' 
			&& $scope.data.switchFungible){
				$scope.data.kngValorRescate = 0;
				$scope.data.showValorRescate = true;
			}
		}
		
		if($scope.data.tipInsOpc){			
			$scope.data.oConfiguracionModalidadLiquidacion.IdModalidadLiquidacion = $scope.data.instrumento.IdModoOpcion;
			$scope.data.modalidadEjercicio = $scope.data.instrumento.IdModalidadEjercicio;
			$scope.data.oConfiguracionTipoOpcion.IdTipoOpcion = $scope.data.instrumento.IdTipoOpcion;
			$scope.data.oConfiguracionEstiloOpcion.IdEstiloOpcion = $scope.data.instrumento.IdEstilo;
			$scope.data.kngFechaVencimiento = $scope.data.instrumento.FechaVencimiento;
			$scope.data.strike = $scope.data.instrumento.PrecioStrike;
			$scope.data.tamano = $scope.data.instrumento.Tamano;
			//$scope.data.vencimieto =  $scope.data.instrumento.FechaVencimiento;;

			bcsComunes.ventanaTamanoAutomatico(idVentana);
		}
	},true);

	$scope.$watchGroup(['data.kngMonto','data.kngTasa','data.kngPlazo','data.kngPrecio'], function (newValue, oldValue){
		if ($scope.data.kngMonto !== undefined && $scope.data.kngTasa !== undefined && $scope.data.kngPlazo !== undefined && $scope.data.kngPrecio !== undefined){
			var precioAPlazo = 0;
			if ($scope.data.oConfiguracionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento === 'RE') {
				precioAPlazo = bcsComunes.calcularMontoPlazoInteresCompuesto($scope.data.kngPrecio,$scope.data.kngPlazo,Number($scope.data.kngTasa),360);
			}
			else {
				if($scope.data.oConfiguracionTipoInstrumento.idActual ==='SIM'){
                   if ($scope.data.oConfiguracionBolsa.idActual==='BCS'){
						precioAPlazo = bcsComunes.calcularMontoaPlazo($scope.data.kngPrecio,$scope.data.kngPlazo,Number($scope.data.kngTasa),Number($scope.data.BaseTasaSimultaneaRvBcs));	
					}else{
						precioAPlazo = bcsComunes.calcularMontoaPlazo($scope.data.kngPrecio,$scope.data.kngPlazo,Number($scope.data.kngTasa),Number($scope.data.BaseTasaSimultaneaRvBec));	
					}
			 }else{
					precioAPlazo = bcsComunes.calcularMontoPlazo($scope.data.kngPrecio,$scope.data.kngPlazo,Number($scope.data.kngTasa));	
				}	
			}
			if($scope.data.oConfiguracionTipoInstrumento.idActual ==='SIM' && precioAPlazo !== undefined && precioAPlazo !== null){
				$scope.data.kngPrecioAPlazo = precioAPlazo;
			}else {
				$scope.data.kngPrecioAPlazo = util.bcs_formato(precioAPlazo,'GEN','Precio','CLP',$scope.data.oConfiguracionTipoInstrumento.idActual,'').valorNumerico;
			}
			if ($scope.data.oConfiguracionOperacion.seleccionado.IdTipoInstrumento === 'RRF'){
				$scope.data.kngMontoPlazo = ($scope.data.kngPrecioAPlazo/100) * $scope.data.kngCantidad;
			}
			else{
				$scope.data.kngMontoPlazo = $scope.data.kngPrecioAPlazo * $scope.data.kngCantidad;
			}
		}
	});

	$scope.data.btnLimpiar = function()
    {
		limpiar(true);
    };
	
	/* SVARESGIN-6420 (GDL) Inicio */
	$scope.validarFolio = function ($event) {
		var input = $event.target;
		var valorActual = input.value || '';
		var tecla = $event.which || $event.keyCode;

		// Permitir borrar, delete, flechas, tab
		if ([8, 9, 37, 38, 39, 40, 46].indexOf(tecla) !== -1) {
			return;
		}

		// Caracter nuevo
		var caracter = String.fromCharCode(tecla);

		// Posición cursor
		var inicio = input.selectionStart;
		var fin = input.selectionEnd;

		// Valor resultante real
		var nuevoValor =
			valorActual.substring(0, inicio) +
			caracter +
			valorActual.substring(fin);

		if (nuevoValor.length > 7) {
			$event.preventDefault();
		}
	};
	
	$scope.validarPegadoFolio = function ($event) {
		var input = $event.target;
		var textoPegado = ($event.originalEvent || $event).clipboardData.getData('text') || '';
		var valorActual = input.value || '';

		var inicio = input.selectionStart;
		var fin = input.selectionEnd;

		var nuevoValor =
			valorActual.substring(0, inicio) +
			textoPegado +
			valorActual.substring(fin);

		if (nuevoValor.length > 7) {
			$event.preventDefault();

			input.value = nuevoValor.substring(0, 7);
			$scope.data.folio = input.value;
		}
	};
	/* SVARESGIN-6420 (GDL) Fin */

	$scope.data.btnVolver = function() {
		$scope.data.oConfiguracionClasificacion.idActual = parametrosVentana.IdClasificacionTipoInstrumento?parametrosVentana.IdClasificacionTipoInstrumento:'RV';
		limpiar();
	};
    
    $scope.data.CrearTransaccion = function()
    {
		
		if ( $scope.data.switchVentaCorta && $scope.data.instrumento.IdClasificacionTipoInstrumento === 'RF'
			&& ( $scope.data.instrumento.IdMonedaEmision.trim() === '' || $scope.data.instrumento.IdMonedaEmision === undefined || $scope.data.instrumento.IdMonedaEmision === 'undefined' || $scope.data.instrumento.IdMonedaEmision === null ) ) {
			bcsComunes.msg('error', $translate.instant("UI_ERROR"), $translate.instant('UI_INSTRUMENTO_SIN_MONEDA_EMISION'));
			return;
		}
		
		var transaccion = {
			IdInterno 						: 0,
			FolioTransaccion				: $scope.data.folio,
			FechaCreacion					: util.dateSPdate($scope.data.kngFechaSistema),
			HoraCreacion					: util.fixUtcDate($scope.data.kngHora).utc().format('HH:mm:ss'),
			FechaEmision					: util.dateSPdate($scope.data.kngFechaEmision),
			Plazo							: $scope.data.kngPlazo > 0? $scope.data.kngPlazo : 0,
			EsCuentaPropia					: ($scope.data.switchCuentaPropia)? 'S':'N',
			EsFungible						: ($scope.data.switchFungible)? 'S':'N',
			EsVentaCorta					: ($scope.data.switchVentaCorta)? 'S':'N',
			CodigoOperadorContraparte		: $scope.data.switchOrdenDirectaOD? $scope.data.oConfiguracionOperadorContraparte.seleccionado.CodigoOperador || '' : $scope.data.CodigoOperadorContraparte || '',
			CodigoOperadorCorredor			: $scope.data.oConfiguracionOperadorCorredor.seleccionado.CodigoOperador || '',
			IdSistemaBursatil				: bcsConfiguracionOrdenes.CONSTANTES.IdSistemaBursatilIngresoManual,
			IdEmisor						: $scope.data.oConfiguracionEmisor.seleccionado.IdEmisor || $scope.data.instrumento.IdEmisor,
			Cantidad						: $scope.data.kngCantidad,
			Monto							: util.bcs_formato($scope.data.kngMonto,"GEN", "MONTO", $scope.formData.IdMonedaTransaccion,$scope.data.instrumento.IdTipoInstrumento,$scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion).valorNumerico,
			Precio							: $scope.data.kngPrecio || 0,
			Tasa							: $scope.data.kngTasa || 0,
			MontoPlazo						: $scope.data.kngMontoPlazo || 0,
			PrecioPlazo						: $scope.data.kngPrecioAPlazo || 0,
			ValorRescate					: $scope.data.kngValorRescate || 0,
			IdTipoReajuste					: $scope.data.oConfiguracionInstrumentoTipoReajuste.seleccionado.IdTipoReajuste || $scope.data.instrumento.IdMoneda,
			IdMonedaEmision					: $scope.data.oConfiguracionInstrumentoTipoReajuste.seleccionado.IdTipoReajuste || $scope.data.instrumento.IdMoneda,
			IdEmpleado						: $scope.SesionIdEmpleado,
			IdTipoOperacion					: $scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion,
			IdCondicionDeLiquidacionContado	: $scope.transaccionActual.condicionDeLiquidacionSeleccionada.IdCondicionDeLiquidacion,
			FechaLiquidacionContado			: util.dateSPdate($scope.transaccionActual.fechaLiquidacion),
			FechaVencimiento				: "0001-01-01",
			IdCorredoraContraparte			: $scope.data.oConfiguracionCorredor.seleccionado.IdCorredora || 0,
			IdBolsa							: $scope.data.oConfiguracionBolsa.seleccionado.IdBolsa,
			MarcaNegociacion				: $scope.data.marcaNegociacion || '',
			MejorPrecioCompra				: $scope.data.kngPrecio || 0,
			MejorPrecioVenta				: $scope.data.kngPrecio || 0,
			
			TipoCambioCorresponsal			: $scope.transaccionActual.tipoCambioCorresponsal || 0,
			IdCorredoraCorresponsal			: $scope.transaccionActual.idCorredorCorresponsal || 0,
			MontoLiquidacionCorresponsal	: $scope.transaccionActual.montoLiquidacionCorresponsal || 0,
			GastosCorresponsal				: $scope.transaccionActual.gastosCorresponsal || 0,
			IdMonedaLiquidacionCorresponsal	: $scope.transaccionActual.idMonedaLiquidacionCorresponsal || '',
			
			EsFueraRueda 					: ($scope.data.esFueraRueda)? 'S':'N',
			IndicadorDCV 					: ($scope.data.indicadorDCV)? 'S':'N',						
			EsBilateral 					: ($scope.data.switchIndicadorBilateral)? 'S':'N',
			FolioTransaccionPlazo 			: $scope.data.folioPlazo || 0,
			NemotecnicoEspecificoBolson 	: $scope.data.instrumento.Nemotecnico || '',
			IndicadorOrdenDirecta			: $scope.data.switchOrdenDirectaOD? ($scope.data.switchOrdenDirectaOE?("OE"):("OD")  ) : ("N"),
			IdMoneda						: $scope.formData.IdMonedaTransaccion,
			Intereses 						: $scope.data.kngIntereses || 0,
			Reajustes 						: $scope.data.kngReajustes || 0,
			IdLibroOrdenes					: $scope.transaccionActual.libroOrdenesSeleccionado.IdLibroOrdenes,
			IdBoveda 						: ($scope.SesionLocale === 'PE') ? $scope.data.oConfiguracionBoveda.seleccionado.IdBoveda : ''
		}
		
		if ($scope.transaccionActual.libroOrdenesSeleccionado.EsLibroOfertaPublica === 'S') {
			transaccion.IdTipoOperacion = $scope.transaccionActual.libroOrdenesSeleccionado.IdTipoOperacionOfertaPublica;
		}
		
		if ($scope.data.oConfiguracionTipoInstrumento.idActual === 'SIM') {
			transaccion.IdInstrumento = $scope.data.oConfiguracionOperacion.seleccionado.IdInstrumento;
			transaccion.IdInstrumentoSubyacente = $scope.data.instrumento.IdInstrumento;
		}
		else if ($scope.data.oConfiguracionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento === 'RE') {
			transaccion.IdInstrumento = $scope.data.oConfiguracionOperacion.seleccionado.IdInstrumento;
			transaccion.IdInstrumentoSubyacente = $scope.data.instrumento.IdInstrumento;
		}
		else {
			transaccion.IdInstrumento = $scope.data.instrumento.IdInstrumento;
			transaccion.IdInstrumentoSubyacente = 0;
		}
		
		if ($scope.SesionLocale === 'PE') {
			transaccion.IdSistemaBursatil = $scope.data.idSistemaBursatil;
		}
		
		var datos = {
    		p_IdEmpresa : $scope.SesionIdEmpresa,
    		p_IdUsuario : $scope.SesionIdUsuario,
    		p_IdModulo : 'ORD',
    		p_UsaReglasNegocio : 'S',
    		p_Transacciones : [transaccion]
    	};
  	
  	
		bcsComunes.ventanaOverlay(idVentana,true);
		bcsServicioOrden.CrearTransacciones(datos).$promise.then(function(respuesta)
		{
			bcsComunes.ventanaOverlay(idVentana,false);
			if (respuesta.P_OUT_RESULTADO !== undefined && respuesta.P_OUT_RESULTADO[0] && respuesta.P_OUT_RESULTADO[0].IdTransaccion > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0) 
			{
				bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(respuesta)
				},idVentana);
				
				$scope.data.IdTransaccion = respuesta.P_OUT_RESULTADO[0].IdTransaccion;
				$scope.data.IngresoConfirmacion = 'C';
				bcsComunes.ventanaAutoHeight(idVentana);
			}
			else
			{
				bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(respuesta)
				},idVentana);
			}
		},function(respuesta)
		{
			bcsComunes.ventanaOverlay(idVentana,false);
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant('UI_OPERACION_INGRESO_ERROR'),
				conflictos:util.parseServerResponse(respuesta)
			},idVentana);
		});
    };
		
    //GOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO	
	limpiar(true); //LIMPIAR INICIALMENTE	

});