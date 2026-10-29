angular.module('ModuloOrdenes').controller('IngresarInstruccionCtrl', function($scope,OPCIONES_BCSALERTA,bcsServiciosValorizacion,bcsServiciosPlataformaMercado,$translate,bcsComunes,bcsServicioOrden,bcsServicioPlataforma,$timeout){
	
	let instruccion = this;
	let idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	let parametros = bcsComunes.ventanaParams(idVentana);
	
	instruccion.modoVentana = 'NORMAL';	//NORMAL, OFERTAPUBLICA
	instruccion.SW = 'N';
	instruccion.MonedaSistema = $scope.SesionIdMonedaSistema;
	instruccion.datosInstruccion = {};
	
	instruccion.datosResumen = {};
	
	$scope.data = {};
	$scope.data.oConfiguracionClasificacion = {};
	$scope.data.oConfiguracionTipoInstrumento = {};
	$scope.data.oConfiguracionOperacion = {};
	$scope.data.oConfiguracionMonedaLiquidacion = {};
	$scope.data.oConfiguracionBoveda = {};
	$scope.data.oConfiguracionCondicionLiquidacion = {};
	$scope.data.oConfiguracionTipoInstrumentoSub = {};
	$scope.data.oConfiguracionPortafolio = {};
	$scope.data.oConfiguracionIntermediario = {};
	$scope.data.oConfiguracionInstrumentoTipoReajuste = {};
	$scope.data.oConfiguracionEmisor = {};
	
	$scope.data.fTradeOptions = {};
	$scope.fechaInicial = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
	$scope.fechaMinima = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
	$scope.fechaMinima.setDate($scope.fechaMinima.getDate() - 2);
	$scope.data.plazoVencimiento = 0;
	$scope.data.fTradeOptions.max = $scope.fechaInicial;
	$scope.data.showFechaVencimiento = false;
	$scope.data.showTipoReajuste = false;
	$scope.win = {};
	
	instruccion.datosInstruccion.tipInsOpc = false;
	instruccion.datosInstruccion.inputInstrumentoRequerido = true;
	let flimpiarEnEjecucion = false;
	
	$scope.formData = {
		bolsaDefecto : 'BCS',
		bovedaDefecto: 'DCV',
		montoTotalManual: false,
		calculandoMontoTotal: false,
		ingresoCobros: false,
		valorizando: false,
		validaFlujoCustodia: 'N',
		listoParaIngresar: false,
		decimalesCantidad: 0,
		decimalesTasa: 0,
		decimalesMonto: 0,
		decimalesPrecio: 0,
		tirTasaOptions: {min:-999999999999999999999},
		idUso: "ORDEN",
		filtrosClasificacionTipoInstrumento: {
			IdClasificacionTipoInstrumento : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "IR"}
		},
		filtrosTipoInstrumento: {
			IdEstado 		: { "field"   : "IdEstado","operator": "eq", "value"   : "VIG" },
			IdBolsa 		: { "field"   : "IdBolsa","operator": "eq", "value"   : "BCS" },
			IdClasificacionTipoInstrumento : { "field"   : "IdClasificacionTipoInstrumento","operator": "eq", "value"   : "IR" }
		},
		filtrosTipoInstrumentoSubyacente: {
			IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value"   : "" }
		},
		filtrosOperacion: {
			IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value"   : "" }
		},
		filtrosMonedaLiquidacion: {
			IdTipoInstrumento:{"field":"IdTipoInstrumento","operator":"eq","value":""},
			IdMonedaInstrumento:{"field":"IdMoneda","operator":"eq","value":""}
		},
		filtrosEmisor : {
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
			IdEmisor : { "field"   : "IdEmisor","operator": "neq", "value"   : "" },
			IdEmiteIFI : { "field"   : "EmiteIIF","operator": "eq", "value"   : "" },
			IdEmiteIRF : { "field"   : "EmiteIRF","operator": "eq", "value"   : "" }
		},
		filtrosInstrumentoTipoReajuste: {
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
			IdInstrumento : { "field"   : "IdInstrumento","operator": "eq", "value"   : "" }	
		},
		filtrosIntermediario: {
			CodigoCorredora: {"field" : "CodigoCorredora" , "operator": "lte" , "value": 100},
			IdEstado: {"field" : "IdEstado" , "operator": "eq" , "value": "ACT" }
		}
		
	};

	$scope.data.oConfiguracionEmisor = {
		configuracion:{ template: '<span >#: IdEmisor #</span>' },
		idActual : false
	};
	
	$scope.$watch('data.oConfiguracionTipoInstrumento.idActual',function(newval,oldval){
		if(newval){
			bcsComunes.ventanaAutoHeight(idVentana);
			if( $scope.data.oConfiguracionTipoInstrumento.idActual === 'IRF'){
				$scope.data.showValorRescate = false;
				$scope.data.enabledFechaVencimiento = false;
				$scope.data.showFechaVencimiento = true;
				$scope.data.obtienePrecio = true;
				$scope.data.enabledReajuste = false;
			}
			if( $scope.data.oConfiguracionTipoInstrumento.idActual === 'IFI' ){
				$scope.data.showValorRescate = true;
				$scope.data.showFechaVencimiento = true;
				$scope.data.enabledFechaVencimiento = true;
				$scope.data.obtienePrecio = false;
			}
			if( $scope.data.oConfiguracionTipoInstrumento.idActual === 'IRV' ){
				$scope.data.showValorRescate = false;
				$scope.data.showFechaVencimiento = false;
				$scope.data.enabledFechaVencimiento = false;
				$scope.data.obtienePrecio = true;
			}
		}
	});
	let setEmisor = function (){
		
		if ($scope.data.instrumento.IdEmisor !== undefined) {
			if ($scope.data.instrumento.IdEmisor !== ''){
				$scope.data.enabledEmisor = false;
				$scope.data.enabledReajuste = false; 
				$scope.formData.filtrosEmisor.IdEmisor.operator =  'eq';
				$scope.formData.filtrosEmisor.IdEmisor.value =  $scope.data.instrumento.IdEmisor;
				$scope.data.oConfiguracionEmisor.idActual = $scope.data.instrumento.IdEmisor;
				$scope.data.oConfiguracionEmisor.idPostFiltro = $scope.data.instrumento.IdEmisor;
			}else{
				$scope.data.enabledEmisor = true; 
				$scope.data.enabledReajuste = true; 
				$scope.formData.filtrosEmisor.IdEmisor.operator =  'neq';
				$scope.formData.filtrosEmisor.IdEmisor.value =  '';
			}
		}
	};
	$scope.$watch('data.oConfiguracionEmisor.idActual', function(newval,oldval){
		if (typeof $scope.data.oConfiguracionEmisor.emisorSeleccionado !== 'undefined' && 
		   	       $scope.data.oConfiguracionEmisor.emisorSeleccionado  !== false){
	
		   	if ($scope.data.oConfiguracionOperacion.operacionSeleccionada.AccionCustodia !== 'INGRESO'){
				if($scope.data.oConfiguracionTipoInstrumento.idActual === 'IFI'){
					$scope.data.esFungible = $scope.data.oConfiguracionEmisor.emisorSeleccionado.EmiteFungible === 'S';
					$scope.data.cantidadInicial = $scope.data.esFungible === true ? 1 : 0;
				}
				if(typeof $scope.data.instrumento !== 'undefined' && !$scope.data.instrumento){
					$scope.data.corteMinimo = $scope.data.oConfiguracionInstrumentoTipoReajuste.tipoReajusteSeleccionado && $scope.data.oConfiguracionInstrumentoTipoReajuste.tipoReajusteSeleccionado.CorteMinimo > 0 ? $scope.data.oConfiguracionInstrumentoTipoReajuste.tipoReajusteSeleccionado.CorteMinimo : 1;
				}
		   	} else {
				if(typeof $scope.data.instrumento !== 'undefined' && $scope.data.oConfiguracionTipoInstrumento.idActual === 'IFI'){
					$scope.data.esFungible = $scope.data.oConfiguracionEmisor.emisorSeleccionado.EmiteFungible === 'S';
					$scope.data.cantidadInicial = $scope.data.esFungible === true ? 1 : 0;
				}			   		
		   	}
		}
	});
	let fnSeleccionaInstrumento = function (newval,oldval){
		if(typeof $scope.data.instrumento !== 'undefined' && $scope.data.instrumento !== false && $scope.data.instrumento.IdInstrumento !== undefined){
			if (!$scope.data.instrumento || !$scope.data.instrumento.IdInstrumento)
				return;
			fnLimpiarBloqueValorizacion();
			
			let varcambio='data.instrumento';
			if($scope.data.instrumento.IdMoneda !== ''){
				$scope.formData.decimalesCantidad = util.bcs_formato(0,'GEN','Cantidad',$scope.data.instrumento.IdMoneda,$scope.data.instrumento.IdTipoInstrumento,'').cantidadDecimales;
				$scope.formData.decimalesTasa = util.bcs_formato(0,'GEN','Tasa',$scope.data.instrumento.IdMoneda,$scope.data.instrumento.IdTipoInstrumento,'').cantidadDecimales;
				$scope.formData.decimalesMonto = util.bcs_formato(0,'GEN','Monto',$scope.data.instrumento.IdMoneda,$scope.data.instrumento.IdTipoInstrumento,'').cantidadDecimales;
				$scope.formData.decimalesPrecio = util.bcs_formato(0,'GEN','Precio',$scope.data.instrumento.IdMoneda,$scope.data.instrumento.IdTipoInstrumento,'').cantidadDecimales;
			}else{
				$scope.formData.decimalesCantidad = util.bcs_formato(0,'GEN','Cantidad',$scope.SesionIdMonedaSistema,$scope.data.instrumento.IdTipoInstrumento,'').cantidadDecimales;
				$scope.formData.decimalesTasa = util.bcs_formato(0,'GEN','Tasa',$scope.SesionIdMonedaSistema,$scope.data.instrumento.IdTipoInstrumento,'').cantidadDecimales;
				$scope.formData.decimalesMonto = util.bcs_formato(0,'GEN','Monto',$scope.SesionIdMonedaSistema,$scope.data.instrumento.IdTipoInstrumento,'').cantidadDecimales;
				$scope.formData.decimalesPrecio = util.bcs_formato(0,'GEN','Precio',$scope.SesionIdMonedaSistema,$scope.data.instrumento.IdTipoInstrumento,'').cantidadDecimales;
			}
			$scope.data.oConfiguracionMonedaLiquidacion.idActual = $scope.data.instrumento.IdMoneda;
			
			if($scope.data.cantidadInicial && $scope.data.corteMinimo){
				$scope.data.valorRescate = $scope.data.cantidadInicial * Number($scope.data.corteMinimo); 
			}
			
			if( $scope.data.oConfiguracionTipoInstrumento.idActual === 'IRF'){
				
				if ($scope.data.instrumento.FechaVencimiento < $scope.SesionFechaSistema){
					bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_INSTRUMENTO_VENCIDO"));
					return;
				}
				$scope.data.enabledReajuste = false;
				$scope.data.showFechaVencimiento = true;
				$scope.data.enabledFechaVencimiento = false;
				$scope.data.showTipoReajuste = true;
				setEmisor();
				$scope.data.corteMinimo = ($scope.data.oConfiguracionInstrumentoTipoReajuste.tipoReajusteSeleccionado && $scope.data.oConfiguracionInstrumentoTipoReajuste.tipoReajusteSeleccionado.CorteMinimo > 0 ? $scope.data.oConfiguracionInstrumentoTipoReajuste.tipoReajusteSeleccionado.CorteMinimo : 1) || 1;
				$scope.data.oConfiguracionInstrumentoTipoReajuste.idActual  = $scope.data.instrumento.IdTipoReajuste;
			}
			
			if($scope.data.oConfiguracionTipoInstrumento.idActual === 'IFI' ){
				
				$scope.data.enabledFechaVencimiento= true;
				$scope.data.fechaVencimiento = util.dateInicioDia($scope.data.instrumento.FechaVencimiento);
				$scope.data.oConfiguracionInstrumentoTipoReajuste.idActual = $scope.data.instrumento.IdTipoReajuste;
				setEmisor();
				$scope.data.corteMinimo = $scope.data.oConfiguracionInstrumentoTipoReajuste.tipoReajusteSeleccionado && $scope.data.oConfiguracionInstrumentoTipoReajuste.tipoReajusteSeleccionado.CorteMinimo > 0 ? $scope.data.oConfiguracionInstrumentoTipoReajuste.tipoReajusteSeleccionado.CorteMinimo : 1;
				if ($scope.data.instrumento.IdEmisor === ''){
					$scope.data.enabledReajuste = true;
				}else{
					$scope.data.enabledReajuste = false;
				}
			}
		
			$scope.data.oConfiguracionInstrumentoTipoReajuste.idActual  = $scope.data.instrumento.IdTipoReajuste;
			
			if ($scope.data.showFechaVencimiento === true) {
				$scope.data.fechaVencimiento = util.dateInicioDia($scope.SesionFechaSistema);
			}
			else {
				$scope.data.fechaVencimiento = util.dateInicioDia('2099-12-31');
			}
			
			if($scope.data.showFechaVencimiento === true && $scope.data.instrumento.FechaVencimiento !== undefined && !util.dateisdefault($scope.data.instrumento.FechaVencimiento)){
				$scope.data.fechaVencimiento = util.dateInicioDia($scope.data.instrumento.FechaVencimiento);		
			}
			
			//$scope.data.corteMinimo = ($scope.data.instrumento.CorteMinimo >0 ? $scope.data.instrumento.CorteMinimo : 1) || 1;
			
			if(Number($scope.data.corteMinimo)===1) {
				$scope.data.enabledValorRescate = true;
			}
			else {
				$scope.data.enabledValorRescate = false;
			}
			
			if($scope.data.cantidadInicial && $scope.data.corteMinimo){
				$scope.data.valorRescate = $scope.data.cantidadInicial * Number($scope.data.corteMinimo); 
			}
			
			if ($scope.data.obtienePrecio === true) {
				if($scope.data.oConfiguracionTipoInstrumento.idActual === 'IRV'){
					if($scope.data.instrumento.PrecioMercado>0){
						$scope.data.precio = $scope.data.instrumento.ValorizaPor === 'P' ? $scope.data.instrumento.PrecioMercado : undefined;
					}
				}else{
					if($scope.data.instrumento.PrecioMercado>0){
						$scope.data.precio = $scope.data.instrumento.ValorizaPor === 'P' ? $scope.data.instrumento.PrecioMercado : undefined;
					}
					if($scope.data.instrumento.Tasa>0){
						$scope.data.tirTasaNegociacionMercado = $scope.data.instrumento.Tasa;
					}
				}
			}
			if ($scope.data.instrumento.Reajustable !== undefined && $scope.data.showFechaVencimiento === true) {
				$scope.formData.filtrosInstrumentoTipoReajuste.IdInstrumento.value = Number($scope.data.instrumento.IdInstrumento) || 0;
				$scope.data.oConfiguracionInstrumentoTipoReajuste.idPostFiltro = $scope.data.instrumento.IdTipoReajuste;
				$scope.data.oConfiguracionInstrumentoTipoReajuste.idActual = $scope.data.instrumento.IdTipoReajuste;
				$scope.formData.filtrosInstrumentoTipoReajuste.read = true;
			}
			$scope.formData.filtrosMonedaLiquidacion.IdTipoInstrumento.value = $scope.data.instrumento.IdTipoInstrumento;
			$scope.formData.filtrosMonedaLiquidacion.IdMonedaInstrumento.value = $scope.data.instrumento.IdMoneda || '';
			$scope.formData.filtrosMonedaLiquidacion.read = true;
			
			fnSeleccionaMonedas();
		}
	}
	$scope.$watch('data.instrumento', fnSeleccionaInstrumento ,true);
	
	let fnSeleccionaMonedas = function(newValue, oldValue) {
		
		let IdMonedaLiquidacionValorRescate 	= '';
		$scope.formData.listoParaIngresar= false;
		
		if($scope.data.instrumento === undefined || $scope.data.instrumento === false){
			return
		}
		if (typeof $scope.data.oConfiguracionMonedaLiquidacion !== 'undefined' && 
		   	       $scope.data.oConfiguracionMonedaLiquidacion.monedaLiquidacionSeleccionada  !== false &&
		   	       $scope.data.oConfiguracionMonedaLiquidacion.monedaLiquidacionSeleccionada.IdMoneda  !== '' &&
		   	       typeof $scope.data.instrumento !== 'undefined' &&
		   	       $scope.data.instrumento.IdClasificacionTipoInstrumento === 'IFI'){
		
			IdMonedaLiquidacion = $scope.data.oConfiguracionMonedaLiquidacion.monedaLiquidacionSeleccionada.IdMonedaLiquidacion;
			IdMonedaLiquidacionValorRescate	= $scope.data.oConfiguracionMonedaLiquidacion.monedaLiquidacionSeleccionada.IdMoneda;
		} else if ($scope.data.instrumento.IdTipoReajuste){
			IdMonedaLiquidacionValorRescate	= $scope.data.instrumento.IdTipoReajuste;
			IdMonedaLiquidacion = $scope.data.oConfiguracionMonedaLiquidacion.monedaLiquidacionSeleccionada.IdMonedaLiquidacion;
		}else if (typeof $scope.data.instrumento !== 'undefined' && typeof $scope.data.instrumento.IdMoneda !== 'undefined'){
			//IdMonedaPago = $scope.data.instrumento.IdMoneda;				
			IdMonedaLiquidacion 		= $scope.data.oConfiguracionMonedaLiquidacion.monedaLiquidacionSeleccionada.IdMonedaLiquidacion;
			IdMonedaLiquidacionOrigen	= $scope.data.oConfiguracionMonedaLiquidacion.monedaLiquidacionSeleccionada.IdMoneda;
		} else if ($scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion !== 'INGTIT' && $scope.data.checkPorCartera){
			IdMonedaLiquidacion = $scope.data.instrumento.IdMoneda;
		};
		
		if (typeof $scope.data.instrumento !== 'undefined'){
			IdTipoInstrumento = $scope.data.instrumento.IdTipoInstrumento;
		}else if($scope.data.instrumento.IdTipoInstrumento !== ''){
			IdTipoInstrumento = $scope.data.instrumento.IdTipoInstrumento;
		}
		
		if (typeof $scope.data.instrumento !== 'undefined' && $scope.data.instrumento.IdClasificacionTipoInstrumento === 'RV'){
			$scope.data.monedaMontoValorizadoMonedaOrigen 	= IdMonedaLiquidacionOrigen;		
			$scope.data.monedaInstrumento 					= IdMonedaLiquidacionOrigen;
		}else{
			$scope.data.monedaMontoValorizadoMonedaOrigen 	= IdMonedaLiquidacion;		
			$scope.data.monedaInstrumento 					= IdMonedaLiquidacion;
		};
		
		if ($scope.data.oConfiguracionOperacion.operacionSeleccionada.AccionCustodia === 'INGRESO'){ 
			switch ($scope.data.instrumento.IdClasificacionTipoInstrumento){		
				case "RV":
					$scope.data.corteMinimo 		= 1;
					break;
				case "RF":
				case "IF":
					$scope.data.corteMinimo = $scope.data.oConfiguracionInstrumentoTipoReajuste.tipoReajusteSeleccionado && $scope.data.oConfiguracionInstrumentoTipoReajuste.tipoReajusteSeleccionado.CorteMinimo > 0 ? $scope.data.oConfiguracionInstrumentoTipoReajuste.tipoReajusteSeleccionado.CorteMinimo : 1;
					$scope.formData.decimalesMonto 	= (util.bcs_formato(0,'GEN','Monto',IdMonedaLiquidacionValorRescate,'','')).cantidadDecimales;						
					break;
			};				
		};
	};
	
	$scope.$watchGroup(['data.oConfiguracionMonedaLiquidacion.monedaLiquidacionSeleccionada'], fnSeleccionaMonedas );
	$scope.$watchGroup(['data.oConfiguracionInstrumentoTipoReajuste.tipoReajusteSeleccionado'], function () {
		fnSeleccionaMonedas();
		$scope.data.corteMinimo = $scope.data.oConfiguracionInstrumentoTipoReajuste.tipoReajusteSeleccionado && $scope.data.oConfiguracionInstrumentoTipoReajuste.tipoReajusteSeleccionado.CorteMinimo > 0 ? $scope.data.oConfiguracionInstrumentoTipoReajuste.tipoReajusteSeleccionado.CorteMinimo : 1;
	});
	
	$scope.$watchGroup(['data.oConfiguracionPortafolio.portafolioSeleccionado','data.oConfiguracionOperacion.operacionSeleccionada'],function(newval,oldval){
		if ($scope.data.oConfiguracionPortafolio.portafolioSeleccionado && $scope.data.oConfiguracionPortafolio.portafolioSeleccionado.IdPortafolio
				&& $scope.data.oConfiguracionOperacion.operacionSeleccionada && $scope.data.oConfiguracionOperacion.operacionSeleccionada.IdTipoOperacion) {
			
			let datos = {
					p_IdEmpresa 		: $scope.SesionIdEmpresa,
					p_IdUsuario			: $scope.SesionIdUsuario,
					p_IdModulo			: 'ORD', //consultar
					p_IdEmpleado		: $scope.SesionIdEmpleado,
					p_IdTipoOperacion	: $scope.data.oConfiguracionOperacion.operacionSeleccionada.IdTipoOperacion,
					P_IdPortafolio		: $scope.data.oConfiguracionPortafolio.portafolioSeleccionado.IdPortafolio
			}
			
			validaFlujoCustodia();
			
		}
	});
	$scope.btnLimpiar = function() {
		$scope.data.oConfiguracionPortafolio.idActual = false;
		$scope.data.oConfiguracionTipoInstrumentoSub.idActual = false;
		$scope.data.oConfiguracionTipoInstrumento.idActual = false;
		$scope.data.oConfiguracionOperacion.idActual = false;
		$scope.cleanData();
	};
	
	$scope.cleanData = function() {
		instruccion.SW = 'N';		
		$scope.formData.decimalesCantidad = 0;
		$scope.formData.decimalesTasa = 4;
		$scope.formData.decimalesMonto = 0;
		$scope.formData.decimalesPrecio = 0;
		
		$scope.formData.operacionSalida = [];
		$scope.formData.operaciones = [];
		$scope.formData.setInstrumento = "-1";
		$scope.data.oConfiguracionIntermediario.idActual = false;
		$scope.data.oConfiguracionMonedaLiquidacion.idActual = false;
		$scope.data.oConfiguracionCondicionLiquidacion.idActual = "T0";
		$scope.data.oConfiguracionEmisor.idActual = false;
		
		$scope.data.fechaTrade 			= undefined;
		$scope.data.cantidadInicial 		= undefined;
		$scope.data.tirTasaNegociacion 		= undefined;
		$scope.data.precio 		= undefined;
		$scope.data.montoNeto		= undefined;
		$scope.data.totalCobros	= 0;
		$scope.data.montoTotal = undefined;
		$scope.data.folio 			= undefined;
		$scope.data.comentario		= undefined;
		$scope.data.valorRescate = undefined;
		$scope.data.valorNominal = undefined;
		$scope.data.corteMinimo = undefined;
		$scope.data.EsFungible = 'N';
		
		if (instruccion.modoVentana != 'NORMAL') {
			instruccion.modoVentana = 'NORMAL';
			instruccion.formData.configuracionCompraVenta.activado = true;
			instruccion.formData.instrumentoDeshabilitado = false;
		}
	};
	
	$scope.formData.volver = function() {
		instruccion.formData.modo = 'INGRESO';
		$scope.cleanData();
	};
	
	$scope.ingresarOrden = function(a) {
		if( $scope.data.fechaVencimiento && util.dateSPdate($scope.data.fechaVencimiento) == util.dateSPdate($scope.data.fechaTrade) || $scope.data.plazoVencimiento <= 0){
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), '[INGRESO INSTRUCCION]: No se cumple plazo vencimiento mayor a 0. Saliendo del proceso');
			return;
		}
		let datosIngreso= {
				p_IdEmpresa : $scope.SesionIdEmpresa, 
				p_IdUsuario : $scope.SesionIdUsuario, 
				p_IdModulo : 'ORD',
				p_UsaReglasNegocio : 'S',
				p_IdTipoCanalIngreso : $scope.SesionTipoCanalIngreso,
				
				p_Instrucciones : [{
					IdInterno :0,
					FechaTrade : angular.isDate($scope.data.fechaTrade)? util.dateSPdate($scope.data.fechaTrade) :  '0001-01-01',
					FechaLiquidacion : ($scope.data.fechaLiquidacion)? util.dateSPdate($scope.data.fechaLiquidacion) : '0001-01-01',
				    IdClasificacionTipoInstrumento: $scope.data.oConfiguracionTipoInstrumentoSub.tipoInstrumentoSubSeleccionado.IdClasificacionTipoInstrumentoSubyacente,
					IdClasificacionTipoInstrumentoInstruccion: $scope.data.oConfiguracionTipoInstrumentoSub.tipoInstrumentoSubSeleccionado.IdClasificacionTipoInstrumento,
					IdTipoInstrumentoInstruccion: $scope.data.oConfiguracionTipoInstrumento.idActual,
					IdTipoOperacionInstruccion : $scope.data.oConfiguracionOperacion.operacionSeleccionada.IdTipoOperacion ,
					IdTipoInstrumento: $scope.data.oConfiguracionTipoInstrumentoSub.idActual,
					IdInstrumento : $scope.data.instrumento.IdInstrumento,
					IdEmisor	: $scope.data.oConfiguracionTipoInstrumento.idActual === 'IRV'?$scope.data.instrumento.IdEmisor:$scope.data.oConfiguracionEmisor.emisorSeleccionado.IdEmisor,
					Nemotecnico 	: $scope.data.instrumento.Nemotecnico || '',
					IdCliente: $scope.data.oConfiguracionPortafolio.portafolioSeleccionado.IdCliente,
					IdEstadoCliente : $scope.data.oConfiguracionPortafolio.portafolioSeleccionado.IdEstadoCliente,
					IdPortafolioCliente : $scope.data.oConfiguracionPortafolio.portafolioSeleccionado.IdPortafolio,
					IdEstadoPortafolio : $scope.data.oConfiguracionPortafolio.portafolioSeleccionado.IdEstado,
					DocumentoIdentidad: $scope.data.oConfiguracionPortafolio.portafolioSeleccionado.DocumentoIdentidad,
					NombreCompleto: $scope.data.oConfiguracionPortafolio.portafolioSeleccionado.NombreCompleto,
					IdBolsa: $scope.formData.bolsaDefecto,
					IdBoveda 		: $scope.data.oConfiguracionBoveda.bovedaSeleccionada.IdBoveda,
					IdIntermediario: $scope.data.oConfiguracionIntermediario.intermediarioSeleccionado.IdCorredora,
					FolioFactura 	: $scope.data.folio, 
					IdMonedaLiquidacion : $scope.data.oConfiguracionMonedaLiquidacion.idActual,
					IdCondicionDeLiquidacion : $scope.data.oConfiguracionCondicionLiquidacion.condicionLiquidacionSeleccionada.IdCondicionDeLiquidacion,
					Cantidad : $scope.data.oConfiguracionTipoInstrumento.idActual === 'IFI' || $scope.data.oConfiguracionTipoInstrumento.idActual === 'IRV' ? $scope.data.cantidadInicial : $scope.data.valorNominal ,
					PrecioInstruccion: $scope.data.precio,
					MontoNeto :  $scope.data.montoNeto,
					TotalCobros : $scope.data.totalCobros || 0,
					MontoLiquidacion : $scope.data.montoTotal,
					Glosa : $scope.data.comentario || '',
					FechaVencimiento: ($scope.data.fechaVencimiento)? util.dateSPdate($scope.data.fechaVencimiento) : '0001-01-01',
					PlazoVencimiento: $scope.data.plazoVencimiento || 0,
					TirTasa : $scope.data.tirTasaNegociacion || 0,
					ValorRescate: $scope.data.oConfiguracionTipoInstrumento.idActual === 'IFI' ? ($scope.data.valorRescate || 0) : 0,
					ValorNominal: $scope.data.oConfiguracionTipoInstrumento.idActual === 'IRF' ? ($scope.data.valorNominal || 0) : 0	,
					IdTipoReajuste : $scope.data.oConfiguracionInstrumentoTipoReajuste.idActual || '',
					EsFungible: $scope.data.oConfiguracionTipoInstrumento.idActual === 'IFI' ?  $scope.data.oConfiguracionEmisor.emisorSeleccionado.EmiteFungible:'N',
					CorteMinimo: $scope.data.corteMinimo ? $scope.data.corteMinimo : 0,
					CodMotivoInstruccion: 'INCUS'
				}]
				
		};
		
		bcsComunes.ventanaOverlay(idVentana,true);
		
		bcsServicioOrden.CrearInstrucciones(datosIngreso).$promise.then(function (respuesta){
			bcsComunes.ventanaOverlay(idVentana,false);
			if (respuesta.P_OUT_REFID !== undefined && respuesta.P_OUT_REFID[0] && respuesta.P_OUT_REFID[0].IdEntidad > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0) 
			{
				/*bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_INSTRUCCION_INGRESADA")+": " + respuesta.P_OUT_REFID[0].IdEntidad,
					conflictos:util.parseServerResponse(respuesta)
				},idVentana);*/
				bcsComunes.msg("success",'Instruccion','Instrucción ingresada correctamente.');
                bcsComunes.ventanaData(idVentana, true);
                bcsComunes.ventanaCerrar(idVentana);

				bcsComunes.ventanaAbrir("DetalleInstruccion", {IdInstruccion : respuesta.P_OUT_REFID[0].IdEntidad},false,false);
			}
		},function(respuesta){
   			bcsComunes.ventanaOverlay(idVentana,false);
			bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"),
					conflictos:util.parseServerResponse(respuesta)
				},idVentana);
   		});
		
   	};

	$scope.calcularPlazoVencimiento = function (){

		let datos = {
    				p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo : 'PLA',
					p_UsaReglasNegocio : 'S',
					p_IdCondicionDeLiquidacion : $scope.data.oConfiguracionCondicionLiquidacion.condicionLiquidacionSeleccionada.IdCondicionDeLiquidacion,
					p_FechaDesde : angular.isDate($scope.data.fechaLiquidacion)? util.dateSPdate($scope.data.fechaLiquidacion): util.dateSPdate($scope.fechaInicial),
					p_FechaHasta : util.dateSPdate($scope.data.fechaVencimiento),
					p_DiasHabiles : 'N',
					p_IdPais :  $scope.SesionIdPaisSistema
				};
    			
    			bcsServicioPlataforma.Plazo(datos,function(result)
				{
					if (result.P_OUT_PLAZODIAS && result.P_OUT_PLAZODIAS != $scope.data.plazoVencimiento){
						$scope.data.plazoVencimiento = Number(result.P_OUT_PLAZODIAS);
					}else{
						if(result.P_OUT_PLAZODIAS && result.P_OUT_PLAZODIAS <= 0 ){
							$scope.data.plazoVencimiento = 0;
							bcsComunes.msg('error',$translate.instant("UI_ERROR"), '[CALCULO PLAZO]: No se cumple plazo. Saliendo del proceso');
						}						
					}
				}
				,function(result)
				{
					console.log("ERROR: " + JSON.stringify(result));
				});
		
	};
	
	$scope.$watch('data.fechaVencimiento',function(newval,oldval){
		if(newval){
			$scope.formData.listoParaIngresar= false;
			$scope.calcularPlazoVencimiento();
		}
	});
	
	$scope.$watch('data.fechaLiquidacion',function(newval,oldval){
		if(newval && $scope.data.fechaVencimiento && util.dateSPdate($scope.data.fechaVencimiento)){
			$scope.formData.listoParaIngresar= false;
			$scope.calcularPlazoVencimiento();
		}
	});
	
	$scope.$watch('data.totalCobros',function(newval,oldval){
		if(!$scope.formData.calculandoMontoTotal && !$scope.formData.valorizando){
			$scope.formData.listoParaIngresar= false;
			if ($scope.data.montoNeto && $scope.data.totalCobros){
				$scope.formData.ingresoCobros = true;
				$scope.data.montoTotal = Number($scope.data.montoNeto) + Number($scope.data.totalCobros || 0);
			}else{
				$scope.data.montoTotal = $scope.data.montoNeto;
			}
		}else{
			$scope.formData.montoTotalManual = false;
			$scope.formData.calculandoMontoTotal = false;
		}
		if ($scope.data.oConfiguracionTipoInstrumento.idActual === 'IRV') {
			$scope.data.precio = $scope.data.montoTotal / $scope.data.cantidadInicial;
		}
	});
			
	
	$scope.$watch('data.montoTotal',function(newval,oldval){
		if(!$scope.formData.calculandoMontoTotal  && !$scope.formData.ingresoCobros && !$scope.formData.valorizando && !$scope.formData.montoTotalManual){
			$scope.formData.listoParaIngresar= false;
			$scope.formData.montoTotalManual= true;
			if($scope.data.oConfiguracionTipoInstrumento.idActual === 'IRF' && $scope.data.valorNominal > 0){
				if($scope.data.totalCobros && $scope.data.totalCobros>0){
					$scope.data.montoTotal += $scope.data.totalCobros;
				}
				$scope.data.montoNeto = $scope.data.totalCobros ? $scope.data.montoTotal - $scope.data.totalCobros : Number(newval);
			}
			if($scope.data.oConfiguracionTipoInstrumento.idActual === 'IRV'){
				if($scope.data.totalCobros && $scope.data.totalCobros>0){
					$scope.data.montoTotal = Number(newval) + $scope.data.totalCobros;
				}
				$scope.data.precio = $scope.data.montoTotal / $scope.data.cantidadInicial;
				$scope.data.montoNeto = $scope.data.totalCobros ? $scope.data.montoTotal - $scope.data.totalCobros : Number(newval);
			}
			if($scope.data.oConfiguracionTipoInstrumento.idActual === 'IFI'){
				if($scope.data.totalCobros && $scope.data.totalCobros>0){
					$scope.data.montoTotal = Number(newval) + $scope.data.totalCobros;
				}
				$scope.data.montoNeto = Number(newval);
			}
			
		}else {
			$scope.data.montoNeto = $scope.data.montoTotal - ($scope.data.totalCobros || 0);
			$scope.formData.montoTotalManual = false;
			$scope.formData.calculandoMontoTotal = false;
		}
	});
	
	$scope.$watch('data.valorNominal', function(newval,oldval){
		if($scope.data.oConfiguracionTipoInstrumento.idActual === 'IRF'){
			$scope.formData.listoParaIngresar= false;
			if ($scope.data.corteMinimo===0 || !$scope.data.corteMinimo || !$scope.data.valorNominal)return;
			if ($scope.SesionLocale === 'CL'){
				let corte = new Big($scope.data.corteMinimo);
				let cantidad = new Big($scope.data.valorNominal);
				let nominal = cantidad.mod(corte);
				if(nominal > 0){
					bcsComunes.msg("warning",'Valor Nominal','No es múltiplo del corte mínimo.');
					$scope.data.valorNominal = 0;
					$scope.data.montoNeto = undefined;
					$scope.data.montoNetoValorizado = undefined;
					$scope.data.montoTotal = undefined;
					$scope.data.valorNominal.foco = true;
					$scope.data.tirTasaNegociacion = undefined;
					return;
				}else {
					$scope.data.precio = undefined;
					$scope.data.montoNeto = undefined;
					$scope.data.montoNetoValorizado = undefined;
					$scope.data.montoTotal = undefined;
					$scope.data.tirTasaNegociacion = undefined;
				}
				
			}
		}
	});
	
	$scope.$watch('data.valorRescate', function(newval,oldval){
		if($scope.data.oConfiguracionTipoInstrumento.idActual === 'IFI'){
			$scope.formData.listoParaIngresar= false;
			if(!$scope.formData.calculandoMontoTotal && !$scope.formData.montoTotalManual && !$scope.formData.ingresoCobros && !$scope.formData.valorizando){

				if($scope.data.oConfiguracionEmisor.idActual !== "CENTRAL") {
					$scope.data.corteMinimo = Math.trunc(Number($scope.data.valorRescate) / Number($scope.data.cantidadInicial));
				} else{
					$scope.data.cantidadInicial = Math.trunc(Number($scope.data.valorRescate) / Number($scope.data.corteMinimo));
				}
			}else{
				$scope.formData.montoTotalManual= false;
				$scope.formData.calculandoMontoTotal = false;
				$scope.formData.ingresoCobros = false;
				$scope.formData.valorizando = false;
			}
				
		}
	});
	
	$scope.$watchGroup(['data.cantidadInicial','data.totalCobros'],function(newval,oldval){
		if(!$scope.formData.calculandoMontoTotal && !$scope.formData.montoTotalManual && !$scope.formData.ingresoCobros && !$scope.formData.valorizando){
			if ($scope.data.precio && $scope.data.cantidadInicial){
				$scope.formData.calculandoMontoTotal = true;
				$scope.data.montoNeto = Number($scope.data.precio) * Number($scope.data.cantidadInicial);
				$scope.data.montoTotal = Number($scope.data.montoNeto) + Number($scope.data.totalCobros || 0);
			}
		}else {
			$scope.formData.montoTotalManual= false;
			$scope.formData.calculandoMontoTotal = false;
			$scope.formData.ingresoCobros = false;
		}
	});
	
	$scope.$watchGroup(['data.oConfiguracionOperacion.operacionSeleccionada','data.instrumento.IdInstrumento'],function(newval,oldval) {
		if (!newval) {
			return;
		}
		if($scope.data.instrumento === undefined) {
			return;
		}
		let idTipoInstrumento = $scope.data.instrumento.IdTipoInstrumento;
		if (!idTipoInstrumento)
			return;
		
		let tipoOperacion = undefined;
		$.each(BCS_OPERACION_INSTRUMENTO,function(key,value){
			
			if (value.IdTipoInstrumento === idTipoInstrumento && value.AccionOrden === $scope.data.oConfiguracionOperacion.operacionSeleccionada.AccionOrden) {
				tipoOperacion = value;
				return false;
			}

		})
		if(tipoOperacion === undefined){
			tipoOperacion = $scope.data.oConfiguracionOperacion.operacionSeleccionada;
		}
		/*if (tipoOperacion === undefined) {
			console.error('Tipo operacion no encontrada');
			return;
		}*/
		//CONSULTAR CON PABLO QUE ES SW 
		/*if (instruccion.SW === 'N') {
			$scope.data.oConfiguracionOperacion.operacionSeleccionada = tipoOperacion;
		} else {
			$scope.data.oConfiguracionOperacion.operacionSeleccionada = tipoOperacion;
		}*/
			calcularFechaCondicionLiquidacion(tipoOperacion);
		
	});
	
	//watch condicion de liquidacion y switch ingreso rapido activado
	$scope.$watchGroup(['data.oConfiguracionCondicionLiquidacion.condicionLiquidacionSeleccionada', 'data.fechaTrade'], function(newval, oldval) {
		if (newval) {
			let tipoOperacion = undefined;
			if($scope.data.instrumento === undefined) {
				return;
			}
			let idTipoInstrumento = $scope.data.instrumento.IdTipoInstrumento;
			$.each(BCS_OPERACION_INSTRUMENTO,function(key,value) {
				if (value.IdTipoInstrumento === idTipoInstrumento && value.AccionOrden === $scope.data.oConfiguracionOperacion.operacionSeleccionada.AccionOrden) {
					tipoOperacion = value;
					return false;
				}
			});
			if(tipoOperacion === undefined){
				tipoOperacion = $scope.data.oConfiguracionOperacion.operacionSeleccionada;
			}
			calcularFechaCondicionLiquidacion(tipoOperacion);
		}
	});

	//watch forma de pago ultima orden
	$scope.$watchGroup([
		'instruccion.datosInstruccion.portafolioSeleccionado.IdPortafolio',
		'instruccion.datosInstruccion.operacionSeleccionada.IdTipoOperacion',
		'instruccion.datosInstruccion.monedaLiquidacionSeleccionada.IdMonedaLiquidacion'],function(newval,oldval){
		if (instruccion.datosInstruccion.portafolioSeleccionado && instruccion.datosInstruccion.portafolioSeleccionado.IdPortafolio && 
			instruccion.datosInstruccion.operacionSeleccionada && instruccion.datosInstruccion.operacionSeleccionada.IdTipoOperacion && 
			instruccion.datosInstruccion.monedaLiquidacionSeleccionada && instruccion.datosInstruccion.monedaLiquidacionSeleccionada.IdMonedaLiquidacion) {
			
			let datos = {
				p_IdPortafolio				:	instruccion.datosInstruccion.portafolioSeleccionado.IdPortafolio,
				p_IdTipoOperacion			:	instruccion.datosInstruccion.operacionSeleccionada.IdTipoOperacion,
				p_IdMonedaLiquidacionOrden	:	instruccion.datosInstruccion.monedaLiquidacionSeleccionada.IdMonedaLiquidacion
			}
			
			bcsServicioOrden.ConsultarFormaDePagoUltimaOrden(datos).$promise.then(function(result){
				if (!result.p_out_FormaDePagoOrden[0])
					return;
				
				if (result.p_out_FormaDePagoOrden[0].IdMedioDePago === 'CIN') {
					instruccion.formData.IdPreferenciaPagoPortafolio = undefined;
					if (instruccion.formData.pagoCobroCuentaInversionDisabled === false) {
						instruccion.datosInstruccion.pagoCobroCuentaInversion = true;
					}
				}
				else {
					instruccion.datosInstruccion.pagoCobroCuentaInversion = false;
				}
				
			});
			
		}
	});
	
	$scope.$watch('data.oConfiguracionOperacion.operacionSeleccionada',function(newval,oldval) {
		if (!newval)
			return;
		
		if (instruccion.SW === 'N' && $scope.data.oConfiguracionOperacion.operacionSeleccionada.idActual !== undefined){
			$scope.data.oConfiguracionCondicionLiquidacion.filtros.IdTipoOperacion.value = $scope.data.oConfiguracionOperacion.operacionSeleccionada.IdTipoOperacion;
		}
		
	});
	
	$scope.$watchGroup(['data.oConfiguracionOperacion.operacionSeleccionada','data.oConfiguracionTipoInstrumentoSub.tipoInstrumentoSubSeleccionado','data.oConfiguracionPortafolio.portafolioSeleccionado',
						'data.oConfiguracionTipoInstrumento.tipoInstrumentoSeleccionado'],function(newval,oldval) {
		if (flimpiarEnEjecucion === false){
			flimpiarEnEjecucion = true;
			$scope.cleanData();
			$timeout(function(){
				flimpiarEnEjecucion = false;
			},1000);			
		}

		if (newval[3]) {
			const tipoInstrumento = newval[3];
			console.log('TipoInstrumento', tipoInstrumento);
			$scope.formData.filtrosEmisor.IdEmiteIFI.value = tipoInstrumento.IdTipoInstrumento === 'IFI' ? 'S' : '';
			$scope.formData.filtrosEmisor.IdEmiteIRF.value = tipoInstrumento.IdTipoInstrumento === 'IRF' ? 'S' : '';
		}
	});
	
	$scope.$watch('data.corteMinimo',function(newval,oldval){
		let varcambio='data.corteMinimo';
		calculaValorNominal(varcambio);
	});
	
	let calcularFechaCondicionLiquidacion = function(tipoOperacion) {
		let datos = {};
		
		datos.p_IdEmpresa = $scope.SesionIdEmpresa;
		datos.p_IdUsuario = $scope.SesionIdUsuario; 
		datos.p_IdModulo = 'PLA'; 
		datos.p_UsaReglasNegocio = 'S';
		datos.p_FechaIngreso = angular.isDate($scope.data.fechaTrade)?util.dateSPdate($scope.data.fechaTrade):util.dateSPdate($scope.fechaInicial);//util.dateSPdate($scope.fechaInicial);
		datos.p_IdTipoOperacion = tipoOperacion.IdTipoOperacion || null;
		datos.P_IdCondicionDeLiquidacion = $scope.data.oConfiguracionCondicionLiquidacion.condicionLiquidacionSeleccionada.IdCondicionDeLiquidacion;
		datos.p_IdPais = Number($scope.SesionIdPaisSistema);

		bcsServicioPlataforma.FechaLiquidacion(datos,function(result) {

			if (result.P_OUT_ERRORESCONTADOR === 0){
				$scope.data.fechaLiquidacion = util.dateDS(result.P_OUT_FECHALIQUIDACION,"FechaSistema","CALL");
			}
			else {
				$scope.data.fechaLiquidacion = util.dateDS("0001-01-01","FechaSistema","CALL");
				console.log("Error." + result.P_OUT_ERRORESCONTADOR);
			}
		},function(result) {

			console.error(result);
		});
	};
	
	$scope.data.confGridArchivos = {
		dataSource : new bcs.DataSource({
			type 		: "odata", //Los tipos posibles son: odata, call, array y object
			definition 	: {
				serverFiltering 	: false, // Filtrado de datos en el servidor.
				serverPaging 		: true, // Paginado de los datos en el servidor.
				serverSorting 		: true, // Ordenamiento de los datos en el servidor.
				groupable 			: true, 
				//sort 				: { field: "FechaModificacion", dir: "asc" }, // Criterios de Ordenamiento.
				pageSize 			: 25, // Número de registros por página.
				transport : {
                    read : {
                        url: "/BSCL/Plataforma/GWJAVA/PLA/call?c=1&t=9999999999999",
                        dataType: "json"
                    }
                },
				schema : {
					model : {
						fields : {
							IdEmpresa: {type: "number"},
							CodigoArchivo: {type: "string"},
							Descripcion: {type: "string"},
							IdUsuario: {type: "number"},
							CodigoUsuario: {type: "string"},
							FechaIngreso: {type: "date"}
						}
					}
				}
			}
		}),
		columns : [
			{field: "IdEmpresa",title: "UI_ID_EMPRESA", format: "{0:n0}",width: "150px", hidden:true},
			{field: "CodigoArchivo",title: "UI_CODIGO_ARCHIVO",width: "80px"},
			{field: "Descripcion",title: "UI_DESCRIPCION",width: "80px"},
			{field: "IdUsuario",title: "UI_ID_USUARIO_MODIFICADOR",width: "50px", hidden:true},
			{field: "CodigoUsuario",title: "UI_NOMBRE_USUARIO",width: "80px"},
			{field: "FechaIngreso",title: "UI_FECHA_INGRESO", width: "80px"}
			
		],
		
		sortable			: false,
		pageable			: false,
		filterable			: false,
		groupable           : false,
		autoBind			: true,
		height				: 85,
		userSettings		: true,
		navigatable			: true,
		excel : {
			allPages: true,
		},
		selectable 			: "row"
	};
	
	
	let valorizando = function(valor){
		$scope.formData.valorizando=valor;
		bcsComunes.ventanaOverlay($scope.ventanaId,valor);
	};
	
	let generaCabeceraValorizar = function(clasificacion, monedaLiq){
		return {
			p_IdEmpresa 		: $scope.SesionIdEmpresa,
			p_IdUsuario 		: $scope.SesionIdUsuario,
			p_IdModulo 			: 'CUS',
			p_IdPais 			: $scope.SesionIdPaisSistema,  
			p_OrigenProcesamiento : 'MESA_UI',
			p_IdMonedaAdicional : monedaLiq,
			p_Instrumentos		: [],
		};
	};
	
	let generaEntradaInstrumento = function(idVal){
		return {
			IdValorizacion				: idVal,
			FechaValorizacion			: util.dateSPdate($scope.data.fechaLiquidacion), //$scope.$root.SesionFechaSistema,
			FechaOperacion				: util.dateSPdate($scope.data.fechaTrade),
//			FechaVencimientoOperacion	: util.dateSPdate($scope.data.kngFechaCondicionLiquidacion),
			PlazoEnDiasOperacion		: null,
			IdClasificacionTipoInstrumento	: null,
			IdTipoInstrumento			: null,
			IdInstrumento				: null,
			FechaVencimiento			: null,
			FechaEmision				: null,
			IdMonedaInstrumento			: null,		
			IdTipoReajusteInstrumento	: null,
			Cantidad					: null,
			ValorRescate				: null,
			PrecioCompra				: null,
			TasaCompra					: null,
			MontoCompra					: null,		
			PrecioSubyacente			: null,
			TasaSubyacente				: null,
			PrecioGarantia				: null,
			TasaGarantia				: null,
			TasaTIF						: null,
			IdPortafolio				: null,				
			TablaOrigen					: 'CARTERA',		
			IdUso						: null,
			TipoCartera					: null,
			IdTipoCriterioTasa			: null,
		};
	};
	
	$scope.ObtenerValorizacion = function(cambio){

		
		if($scope.data.fechaTrade === undefined || $scope.data.fechaTrade === "0001-01-01"){
			bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_INDICAR_FECHA_TRADE"));
			return;
		}
		if (typeof $scope.data.precio !== 'undefined' && $scope.data.precio > 0)
			cambio = 'Precio';				
		if (typeof $scope.data.tirTasaNegociacion !== 'undefined' && $scope.data.tirTasaNegociacion > 0)
			cambio = 'Tasa';

		
		switch ($scope.data.oConfiguracionTipoInstrumento.idActual)
		{		
			
			case "IRF":
				switch ($scope.SesionLocale){
					case "CL":	Cantidad = $scope.data.valorNominal; break; //$scope.data.cantidadInicial;
					case "PE":	Cantidad = $scope.data.valorNominal; break;
				}
				
				Precio		=  undefined;
				Tasa 		=  undefined;	
				Monto		= $scope.data.montoTotal || undefined;

				$scope.fnvalorizarRF(Cantidad,Precio,Tasa,Monto);
				break;
			case "IFI":

				ValorRescate	= $scope.data.valorRescate || undefined;
				Precio		= 0;
				Tasa 		= 0;
				Monto		= $scope.data.montoTotal || undefined;  //$scope.data.kngMonto;
									
				$scope.fnvalorizarIF(ValorRescate,Precio,Tasa,Monto);
				break;
		};
		
	};

	
	$scope.fnvalorizarRF = function(cantidad,precio,tasa,monto){
		console.log('[VALORIZACION RF]: Iniciando validaciones');

		let IdOperacion = $scope.data.oConfiguracionOperacion.operacionSeleccionada.AccionCustodia;			
		let FechaVencimiento=($scope.data.fechaVencimiento)?util.dateSPdate($scope.data.fechaVencimiento):undefined;
		let Plazo=$scope.data.plazoVencimiento;
		let Instrumento=undefined;
		$scope.formData.valorizando = true;
		
		if(IdOperacion=='INGRESO')Instrumento=($scope.data.instrumento)?$scope.data.instrumento:undefined;
		if(IdOperacion=='RETIRO')
			if($scope.data.checkPorCartera) Instrumento=($scope.instrumentoCustodia)?$scope.instrumentoCustodia:undefined;
			else Instrumento=($scope.data.instrumento)?$scope.data.instrumento:undefined;
		if(!Instrumento){
			console.log('[VALORIZACION RF]: No se ha seleccionado el instrumento. Saliendo del proceso');
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), '[VALORIZACION RF]: No se ha seleccionado el instrumento. Saliendo del proceso');
			valorizando(false);
			return;
		}
		if(!FechaVencimiento || FechaVencimiento == util.dateSPdate($scope.data.fechaTrade)){
			console.log('[VALORIZACION RF]: No se cumple plazo. Saliendo del proceso');
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), '[VALORIZACION RF]: No se cumple plazo. Saliendo del proceso');
			valorizando(false);
			return;
		}
		if(!$scope.data.fechaTrade){
			console.log('[VALORIZACION RF]: Debe especificar Fecha de Trade');
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), '[VALORIZACION RF]: Debe especificar Fecha de Trade');
			valorizando(false);
			return;
		}			
		let IdClasificacionTipoInstrumento = Instrumento.IdClasificacionTipoInstrumento;

		let Cantidad 			= cantidad;
		let Tasa 				= tasa;
		let Precio	 			= precio;
		let Monto 				= monto;
		let PrecioReferencia 	= $scope.data.precioMercado;
		let TasaReferencia 		= $scope.data.tirTasaNegociacionMercado;
				
		let condicion = '';
		if(Cantidad > 0 && Precio ==undefined && Tasa == undefined && Monto>0) condicion='A'
		else if(Cantidad > 0 && Precio>0 && Tasa > 0 && Monto > 0) condicion ='B';
		else{
			console.log('[VALORIZACION RF]: No cumple condiciones');
			//bcsComunes.msg('error',$translate.instant("UI_ERROR"), '[VALORIZACION RF]: No cumple condiciones');
			valorizando(false);
			return;
		}

		let valorizarMercado=false;
		if((!PrecioReferencia||PrecioReferencia==0)&&!TasaReferencia){
			console.log('[VALORIZACION RF]: No hay valores de mercado');
		} else{
			valorizarMercado=true;
			console.log('[VALORIZACION RF]: Se valorizará a mercado');
			//bcsComunes.msg('error',$translate.instant("UI_ERROR"), '[VALORIZACION RF]: Se valorizará a mercado');
		}
		
		let cval=generaCabeceraValorizar(IdClasificacionTipoInstrumento, $scope.data.oConfiguracionMonedaLiquidacion.idActual);	
		let dvalCompra=generaEntradaInstrumento(1);
		dvalCompra.IdTipoInstrumento				=Instrumento.IdTipoInstrumento;
		dvalCompra.IdInstrumento					=Instrumento.IdInstrumento;
		dvalCompra.Nemotecnico						=Instrumento.Nemotecnico;
		dvalCompra.FechaEmision						=Instrumento.FechaEmision;
		dvalCompra.FechaVencimiento					=FechaVencimiento;
		dvalCompra.IdMonedaInstrumento				=$scope.data.oConfiguracionMonedaLiquidacion.idActual;
		dvalCompra.IdMonedaEmisionInstrumento		=$scope.data.oConfiguracionInstrumentoTipoReajuste.idActual || Instrumento.IdTipoReajuste;
		dvalCompra.IdTipoReajusteInstrumento		=$scope.data.oConfiguracionInstrumentoTipoReajuste.idActual || Instrumento.IdTipoReajuste;			
		dvalCompra.Cantidad							=Cantidad||null;
		dvalCompra.PrecioCompra						=null;
		dvalCompra.TasaCompra						=null;
		dvalCompra.MontoCompra						=Monto||null,				
		dvalCompra.Emisor 							=Instrumento.IdEmisor;
		//dvalCompra.TipoValorizacion					=Instrumento.TipoValorizacion;
		dvalCompra.IdCondicionDeLiquidacion			=$scope.data.oConfiguracionCondicionLiquidacion.condicionLiquidacionSeleccionada.IdCondicionDeLiquidacion||null;
		dvalCompra.IdClasificacionTipoInstrumento	=Instrumento.IdClasificacionTipoInstrumento;
		cval.p_Instrumentos.push(dvalCompra);
		callValorizador(cval,IdClasificacionTipoInstrumento,valorizarMercado);
		var valorizado = true;
		return valorizado;
	};
	
	$scope.fnvalorizarIF = function(valorRescate,precio,tasa,monto){
		console.log('[VALORIZACION IF]: Iniciando validaciones');
		let IdOperacion = $scope.data.oConfiguracionOperacion.operacionSeleccionada.AccionCustodia;
		let FechaVencimiento=($scope.data.fechaVencimiento)?util.dateSPdate($scope.data.fechaVencimiento):undefined;
		let Plazo=$scope.data.plazoVencimiento;
		let Instrumento=undefined;
		$scope.formData.valorizando = true;
		
		if(IdOperacion=='INGRESO') Instrumento=($scope.data.instrumento)?$scope.data.instrumento:undefined;
		if(IdOperacion=='RETIRO')
			if($scope.data.checkPorCartera) Instrumento=($scope.instrumentoCustodia)?$scope.instrumentoCustodia:undefined;
			else Instrumento=($scope.data.instrumento)?$scope.data.instrumento:undefined;
		if(!Instrumento){
			console.log('[VALORIZACION IF]: No se ha seleccionado el instrumento. Saliendo del proceso');
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), '[VALORIZACION IF]: No se ha seleccionado el instrumento. Saliendo del proceso');
			valorizando(false);
			return;
		}	
		if(!FechaVencimiento || FechaVencimiento == util.dateSPdate($scope.data.fechaTrade)){
			console.log('[VALORIZACION IF]: No se cumple plazo. Saliendo del proceso');
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), '[VALORIZACION IF]: No se cumple plazo. Saliendo del proceso');
			valorizando(false);
			return;
		}
		if(!$scope.data.fechaTrade || $scope.data.fechaTrade ==="0001-01-01"){
			console.log('[VALORIZACION IF]: Debe especificar Fecha de Trade');
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), '[VALORIZACION IF]: Debe especificar Fecha de Trade');
			valorizando(false);
			return;
		}
		if(!$scope.data.oConfiguracionEmisor.idActual){
			console.log('[VALORIZACION IF]: Debe especificar Emisor');
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), '[VALORIZACION IF]: Debe especificar Emisor');
			valorizando(false);
			return;
		}
		let IdClasificacionTipoInstrumento = Instrumento.IdClasificacionTipoInstrumento;
		
		let ValorRescate 		= valorRescate;
		let Tasa 				= tasa;
		let Monto 				= monto || 0;
		let TasaReferencia 		= $scope.data.tirTasaNegociacionMercado;
		let IdTipoReajuste 		= $scope.data.oConfiguracionInstrumentoTipoReajuste.idActual;

		
		var condicion;
		if(ValorRescate > 0 && FechaVencimiento && IdTipoReajuste && Tasa== undefined && Monto > 0)
			condicion='A';
		else if(ValorRescate > 0 && FechaVencimiento && IdTipoReajuste && Tasa >= 0 && Monto > 0)
			condicion='B';
		else{
			console.log('[VALORIZACION IF]: No cumple condiciones');
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), '[VALORIZACION IF]: No cumple condiciones');
			valorizando(false);
			return;
		}
		
		var valorizarMercado=false;
		if(!TasaReferencia||TasaReferencia==0){
			console.log('[VALORIZACION IF]: No hay valores de mercado');
		} else{
			valorizarMercado=true;
			console.log('[VALORIZACION IF]: Se valorizará a mercado');
			//bcsComunes.msg('error',$translate.instant("UI_ERROR"), '[VALORIZACION IF]: Se valorizará a mercado');
		}
		if(condicion=='TODOS'){
			console.log('[VALORIZACION IF]: Se define que datos limpiar');
			//bcsComunes.msg('error',$translate.instant("UI_ERROR"), '[VALORIZACION IF]: Se define que datos limpiar');
			Monto=null;
		}
		
		var cval=generaCabeceraValorizar(IdClasificacionTipoInstrumento, $scope.data.oConfiguracionMonedaLiquidacion.idActual);	
		var dvalCompra=generaEntradaInstrumento(1);
		dvalCompra.IdTipoInstrumento				=Instrumento.IdTipoInstrumento;
		dvalCompra.IdInstrumento					=Instrumento.IdInstrumento;
		dvalCompra.Nemotecnico						=Instrumento.Nemotecnico;
		dvalCompra.FechaEmision						=Instrumento.FechaEmision;
		dvalCompra.FechaVencimiento					=FechaVencimiento;
		dvalCompra.IdMonedaInstrumento				=$scope.data.oConfiguracionMonedaLiquidacion.idActual;
//		dvalCompra.IdMonedaEmisionInstrumento		=$scope.data.kngIdMonedaEmision || $scope.instrumentoCustodia.NombreCompletoMonedaEmision;
		dvalCompra.IdMonedaEmisionInstrumento		=$scope.data.oConfiguracionInstrumentoTipoReajuste.idActual || Instrumento.IdTipoReajuste;
		dvalCompra.IdTipoReajusteInstrumento		=$scope.data.oConfiguracionInstrumentoTipoReajuste.idActual || Instrumento.IdTipoReajuste;
		dvalCompra.ValorRescate						=ValorRescate||null;
		dvalCompra.TasaCompra						=null;	
		dvalCompra.MontoCompra						=Monto||null;
		dvalCompra.Emisor 							=$scope.data.oConfiguracionEmisor.idActual; //Instrumento.IdEmisor;
		dvalCompra.IdCondicionDeLiquidacion			=$scope.data.oConfiguracionCondicionLiquidacion.condicionLiquidacionSeleccionada.IdCondicionDeLiquidacion||null;
		dvalCompra.IdClasificacionTipoInstrumento	=Instrumento.IdClasificacionTipoInstrumento;
		cval.p_Instrumentos.push(dvalCompra);
		callValorizador(cval,IdClasificacionTipoInstrumento,valorizarMercado);
		
	};
		
	let callValorizador = function(cval,IdClasificacionTipoInstrumento,valorizarMercado){
		bcsComunes.ventanaOverlay(idVentana,true);
		bcsServiciosValorizacion.ValorizarInstrumento(cval,
			function(respuesta){
				bcsComunes.ventanaOverlay(idVentana,false);
				if(respuesta['P_OUT_INSTRUMENTOSVALORIZADOS'].length===0){
					//console.log(inicioLog+'Sin datos valorizados');
					console.log(respuesta);
					var p=bcsComunes.ventanaMsgErr($translate.instant('UI_SIN_DATOS_VALORIZADOS'),$scope.ventanaId);
					bcsComunes.ventanaPromesa(p).then(function(){
						$scope.errorValorizacionSinDatos=true;
						valorizando(false);
						limpiarMonto(true);
					});
					return;
				}
				let cobros = 0;
				$.each(respuesta.P_OUT_INSTRUMENTOSVALORIZADOS,function(key,item) {
					if (item.DimensionTasa === 'COMP' && item.DimensionTiempo === "AC"){
						if (item.TipoValorizacion === 'PQ'){								
							$scope.data.montoNeto = Number(item.MontoMonedaSistema);
							$scope.data.montoNetoValorizado = Number(item.MontoMonedaInstrumento);
							$scope.data.cantidadInicial = Number(item.Cantidad);
							$scope.data.precio = Number(item.Precio);
							$scope.data.tirTasaNegociacion = Number(item.Tasa);
						}
						else {
							$scope.data.montoNeto			= item.MontoMonedaSistema - $scope.data.totalCobros;
							$scope.data.montoNetoValorizado	= item.MontoMonedaSistema - $scope.data.totalCobros;
							
							$scope.data.montoTotal 			= item.MontoMonedaSistema;
							$scope.data.tirTasaNegociacion	= item.Tasa;
							$scope.data.precio				= item.Precio || 0;
						}
					}
				});
				$timeout(function(){valorizando(false);},200).then(function(){
					console.log($scope.data);
					$scope.formData.valorizando= false;
					$scope.formData.listoParaIngresar= true;
				});
			},function(error){
				bcsComunes.ventanaOverlay(idVentana,false);
				console.log(error);
				var p=bcsComunes.ventanaMsgErr($translate.instant('UI_ERROR_EN_COMUNICACION'),$scope.ventanaId);
				bcsComunes.ventanaPromesa(p).then(function(){
					$scope.errorRed=true;
					valorizando(false);
				});
				return;
			}
		);
		
	};
	
	let setCorteMinimo = function(varcambio){
		console.log('fn:setCorteMinimo,watch:'+varcambio);
		
		$scope.data.kngCorteMinimo = ($scope.data.instrumento.CorteMinimo >0 ? $scope.data.instrumento.CorteMinimo : 1) || 1;

	};
	
	let calculaValorNominal = function(varcambio){
		console.log('fn:calculaValorNominal,watch:'+varcambio);
		if($scope.data.corteMinimo>0){
			$scope.data.valorNominal = $scope.data.corteMinimo;
		}
	};
	
	let validaFlujoCustodia = function(){
		
		let IdOperacion =$scope.data.oConfiguracionOperacion.operacionSeleccionada.AccionCustodia;
		let IdPortafolio = $scope.data.oConfiguracionPortafolio.portafolioSeleccionado.IdPortafolio;
		
		if (IdPortafolio>0 && IdOperacion==='RETIRO'/*&&ordenRF.datos.idFormaCustodia==='CUS'*/){
			$scope.formData.validaFlujoCustodia = 'S';
		}else {
			$scope.formData.validaFlujoCustodia = 'N';
		}
	};
	
	let fnLimpiarBloqueValorizacion = function(){
		//Campos de Valorización Compra
		$scope.data.cantidadInicial= undefined;
		$scope.data.montoNeto= undefined;
		$scope.data.montoNetoValorizado = undefined;
		$scope.data.tirTasaNegociacion = undefined;
		$scope.data.precio = undefined;
		$scope.data.valorRescate= undefined;
		$scope.data.valorNominal= undefined;
		//Campos de Valorización Mercado
		$scope.data.precioMercado= undefined;
		$scope.data.montoNetoValorizadoMercado = undefined;
		$scope.data.montoNetoMercado =undefined;
		$scope.data.tirTasaNegociacionMercado = undefined;
	};
	
	$scope.win.blurValorRescate = function() {
		let idClas = '';
		let corteMin = 0;
		$scope.formData.listoParaIngresar= false;
		
		if(!$scope.data.instrumento && $scope.data.oConfiguracionOperacion.operacionSeleccionada.AccionCustodia === 'INGRESO')return;
		if (typeof $scope.data.instrumento === 'undefined' &&  $scope.data.oConfiguracionOperacion.operacionSeleccionada.AccionCustodia === 'INGRESO') return;
			
		if ($scope.data.oConfiguracionOperacion.operacionSeleccionada.AccionCustodia === 'INGRESO'){
			idClas = $scope.data.oConfiguracionTipoInstrumento.idActual;

			if (idClas==='IFI' && !$scope.data.esFungible){
				corteMin = $scope.data.corteMinimo;
			} else {
				corteMin = $scope.data.instrumento.CorteMinimo >0 ? $scope.data.instrumento.CorteMinimo : 1;	
			}				
		}else if ($scope.data.oConfiguracionOperacion.operacionSeleccionada.AccionCustodia === 'RETIRO'){
			idClas = $scope.data.oConfiguracionTipoInstrumento.idActual;
			corteMin = $scope.data.instrumento.CorteMinimo;
		} else return;
		
		if(idClas === 'IFI'){
			if($scope.data.cantidadInicial>=0){
				let CorteMin = corteMin === 0 ? 1 : corteMin;
				
				/*if (!$scope.data.esFungible) {
					$scope.data.valorRescate = CorteMin * $scope.data.cantidadInicial;
				}*/
				/*else {						
					$scope.data.cantidadInicial = 1;
				}*/
				let cantidad =  $scope.data.valorRescate;
				let rescate = cantidad%CorteMin;
				if(rescate > 0){
					bcsComunes.msg("warning",'Valor Rescate','No es múltiplo del corte mínimo.');
					$scope.data.valorRescate = 0;
				}
				$scope.data.montoNeto = undefined;
				$scope.data.montoNetoValorizado = undefined;
				$scope.data.tirTasaNegociacion = undefined;
				
			}
		}
	};

	bcsComunes.ventanaAutoHeight(idVentana);
});
