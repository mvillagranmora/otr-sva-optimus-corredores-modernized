angular.module('ModuloMesa').controller('IngresoNominalesMesaRFCtrl',function ($scope,$translate,$timeout,$rootScope,bcsComunes,bcsServiciosMesa,bcsServiciosGrillasMesaRF ,bcsServicioPlataforma) {
	/****************************************
	 *
	 * Declaraciones
	 *
	 * **************************************/
	//Obtiene los parametros pasado a la ventana
	$scope.dataNominal = {};
	$scope.IdVentana = bcsComunes.ventanaWinId ($scope);   
	$scope.dato = bcsComunes.ventanaParams($scope.IdVentana);
	var fechaSistema = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
	var fechaLiquidacionOperacion = $scope.dato.FechaLiquidacionOperacion;
	var fechaVencimientoOperacion = $scope.dato.FechaVencimientoOperacion;
	var BovedaDefault = $scope.dato.BovedaDefault;
	var fechaVencimiento = $scope.dato.IdTipoOperacion == "COMTER" || $scope.dato.IdTipoOperacion == "VENTER"?new Date(fechaLiquidacionOperacion):new Date(fechaVencimientoOperacion);//($scope.dato.FechaLiquidacionOperacion).getDate()+1;
	fechaVencimiento.setDate((fechaVencimiento).getDate()+1);

	$scope.dataNominal.activarEmisor = true;
	$scope.dataNominal.activarTipoReajuste = true;
	$scope.dataNominal.valorizar = true;
	$scope.dataNominal.PuedeCambiarFungible = false;
	$scope.dataNominal.IdMonedaLiquidacionOperacion = $scope.dato.IdMonedaLiquidacion;//($scope.dato.IdTipoOperacion === 'COMTER' ||  $scope.dato.IdTipoOperacion === 'VENTER')? : null;
	$scope.dataNominal.CodigoCarteraIFRS=  $scope.dato.CodigoIFRS;
	$scope.dataNominal.esResetCartera = false;
	
	// 0 - Nominal/Cantidad + Tasa => Mto. Valorizado
	// 1 - Nominal/Cantidad + Mto. Valorizado => Tasa
	// 2 - Mto. Valorizado + Tasa => Nominal/Cantidad
	$scope.TipoIncognita = 0;
	$scope.MontoEquivalente = 0;
	$scope.MontoEquivalenteUltimo = 0;
	$scope.MontoAValorizar = 0;	
	
	// Parametro Decimales Tasa
	// ----------------------------------------------------------------------------------
	var ErrParm = function(respuesta){
		bcsComunes.msg("error","error",$translate.instant('UI_ERROR_AL_RESCATAR_PARAMETROS_MESA'));
	};

	var OkParam = function(respuesta){
		if (respuesta.P_OUT_DATOSMESA[0] === undefined){
			
        }
		else{
			$.each(respuesta.P_OUT_DATOSMESA,function(key,value) {
				datos.p_ValorDefecto = value.Valor;	
			});

			$scope.dataNominal.IdDecimalTasa = datos.p_ValorDefecto;
        }
	};
	
	var datos = {};
	datos.p_IdEmpresa = $scope.SesionIdEmpresa;
	datos.p_IdTipoProducto = 'MERF';
	datos.p_CodigoParametro = 'CANTIDAD_DECIMALES_TASA';
	datos.p_ValorDefecto = "4";
	bcsServiciosMesa.ConsultarConfiguracionMesa(datos, OkParam, ErrParm);
	// ----------------------------------------------------------------------------------
	
	// Valorizador solo soporta 2 decimales en precios y tasas
	$scope.dataNominal.IdDecimalTasa;
	$scope.dataNominal.IdDecimalPrecio = 2;
	$scope.dataNominal.IdDecimalCorteMinimo = 0;
	$scope.dataNominal.UltimoCorteMinimo = null;

	
	$scope.dataNominal.oConfiguracionClasificacion={
		tipo : "combobox-clasificacion-tipo-instrumento",
		configuracion : {},
		filtros : {
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
			IdTipoInstrumento : {"field"   : "IdTipoInstrumento","operator": "neq", "value"   : "PAC" }
		}
	};
		
	$scope.dataNominal.oConfiguracionEmisor={
		tipo : "combobox-emisor",
		configuracion : {},
		filtros : {
			//IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
		}
	};
	$scope.dataNominal.oConfiguracionInstrumentoTipoReajuste={
		tipo : "combobox-instrumento-tipo-reajuste",
		configuracion : {
			template: '<span >#: IdTipoReajuste # - #: NombreTipoReajuste #</span>'
		},
		filtros : {
			IdInstrumento		: { "field"   : "IdInstrumento","operator": "eq", "value"   : "" },
			IdTipoInstrumento	: { "field"   : "IdTipoInstrumento","operator": "eq", "value"   : "" },
						
		}
	};
	$scope.dataNominal.oConfiguracionMonedaEmision={
		tipo : "combobox-moneda-emision",
		configuracion : {
			template: '<span >#: IdMonedaEmision #</span>'
		},
		filtros : {
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
			IdInstrumento : { "field"   : "IdInstrumento","operator": "eq", "value"   : "" }			
		},
		idActual : false,
		seleccionado : false,
		idDefecto: false,
		idPostFiltro : false
	};
	$scope.dataNominal.oConfiguracionMonedaLiquidacionInstrumento={
		tipo : "combobox-tipo-instrumento-moneda-liquidacion",
		configuracion : {
			template: '<span >#: IdMonedaLiquidacion # - #: NombreMonedaLiquidacion #</span>',
			dataTextField: "IdMonedaLiquidacion",
		},
		filtros : {
			IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value"   : "" },
			IdMoneda :  { "field"   : "IdMoneda","operator": "eq", "value"   : "" },
		},
		idActual : false,
		seleccionado : false,
		idDefecto: false,
		idPostFiltro : false
	};
	$scope.dataNominal.oConfiguracionBoveda = {
		tipo : "combobox-boveda",
		configuracion : {
			template: '<span >#: Codigo # - #: Nombre #</span>'
		},
		filtros : {
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "VIG" },						
		},
		idDefecto : ''
	};
	$scope.dataNominal.oConfiguracionClasificacionIfrs = {
		tipo : "combobox-codigo-ifrs",		
		configuracion : {
			//template: '<span >#: Codigo # - #: Nombre #</span>'
		},
		filtros : {
			Codigo : { "field"   : "Codigo" , "operator": "eq", "value"   : "" }
		}
	};			
	$scope.dataNominal.oConfiguracionCantidad = {
			min: 1
	};
	$scope.dataNominal.oConfiguracionNumeroRepeticiones = {
			min: 1
	};
	$scope.dataNominal.oConfiguracionFechaVencimiento = {
			min: fechaVencimiento//fechaSistema
	};
	$scope.dataNominal.oConfiguracionTasa = {
			min:-9999999
	};



	// Parametro BovedaDefault
	// ----------------------------------------------------------------------------------
	var ErrParm = function(respuesta){
		bcsComunes.msg("error","error",$translate.instant('UI_ERROR_AL_RESCATAR_PARAMETROS_MESA'));
	};

	var OkParam = function(respuesta){

		if (respuesta.P_OUT_DATOSMESA[0] === undefined){
			
        }
		else{
			$.each(respuesta.P_OUT_DATOSMESA,function(key,value) {
				$scope.dataNominal.CodigoBovedaDefault = value.Valor;	
			});

			$scope.dataNominal.oConfiguracionBoveda.idActual = $scope.dataNominal.CodigoBovedaDefault;
        }
	};
	
	var datos = {};
	datos.p_IdEmpresa = $scope.SesionIdEmpresa;
	datos.p_IdTipoProducto = 'MERF';
	datos.p_CodigoParametro = 'BOVEDA_DEFAULT_NOMINAL';
	$scope.dataNominal.CodigoBovedaDefault = "DCV";
	bcsServiciosMesa.ConsultarConfiguracionMesa(datos, OkParam, ErrParm);
	// ----------------------------------------------------------------------------------


	/****************************************
	 *
	 * Eventos
	 *
	 * **************************************/

	var SetCorteMinimo = function() {
		if ($scope.dataNominal.oConfiguracionClasificacion.idActual != undefined && $scope.dataNominal.instrumento != undefined) {
			if ($scope.dataNominal.oConfiguracionClasificacion.idActual === "IF" &&$scope.dataNominal.instrumento.IdEmisor != undefined && $scope.dataNominal.instrumento.IdEmisor !== "CENTRAL" ) {  
				$scope.dataNominal.kngCorteMinimo = 1;
				if ($scope.dataNominal.oConfiguracionInstrumentoTipoReajuste.seleccionado !== null ) {
					$scope.dataNominal.kngCorteMinimo  = $scope.dataNominal.oConfiguracionInstrumentoTipoReajuste.seleccionado.CorteMinimo;
				}
			} else if ( $scope.dataNominal.instrumento !== null ) {
				$scope.dataNominal.kngCorteMinimo = $scope.dataNominal.instrumento.CorteMinimo;
			} else {
				$scope.dataNominal.kngCorteMinimo = $scope.dataNominal.oConfiguracionInstrumentoTipoReajuste.seleccionado.CorteMinimo;
			}
	
			$scope.dataNominal.UltimoCorteMinimo = $scope.dataNominal.kngCorteMinimo;
	
			// Horrrorsss
			$scope.dataNominal.IdDecimalRescateDisplay = 0;
			if ($scope.dataNominal.kngCorteMinimo === 0.1) {
				$scope.dataNominal.IdDecimalRescateDisplay = 1;
			} else if ($scope.dataNominal.kngCorteMinimo === 0.01) {
				$scope.dataNominal.IdDecimalRescateDisplay = 2
			} else if ($scope.dataNominal.kngCorteMinimo === 0.001) {
				$scope.dataNominal.IdDecimalRescateDisplay = 3
			} else if ($scope.dataNominal.kngCorteMinimo === 0.0001) {
				$scope.dataNominal.IdDecimalRescateDisplay = 4
			}
		}
	};
	
	
	
	var enableCarteraOperacion = function(){
		$scope.dataNominal.switchFungible = false;
		$scope.dataNominal.oConfiguracionBoveda.idActual = $scope.dataNominal.CodigoBovedaDefault;
		if ($scope.dataNominal.oConfiguracionClasificacion.idActual === "RF"){
			$scope.dataNominal.activarEmisor = false;
			$scope.dataNominal.activarTipoReajuste = false;
			$scope.dataNominal.activarCantidad = false;
// MSH		$scope.dataNominal.activarNumeroRepeticiones = true;
			$scope.dataNominal.activarPrecio = false;
			$scope.dataNominal.activarTasa = false;
			$scope.dataNominal.activarMonto = false;
			$scope.dataNominal.activarValorRescate = true;
			$scope.dataNominal.activarTipoReajuste = false;
			$scope.dataNominal.activarMonedaLiquidacion = false;
			$scope.dataNominal.activarFechaVencimiento = true;

			$scope.dataNominal.emisorRequerido = false;
			$scope.dataNominal.tipoReajusteRequerido = false;
			$scope.dataNominal.cantidadRequerido = true;
// MSH		$scope.dataNominal.numeroRepeticionesRequerido = false;
// MSH		$scope.dataNominal.numeroRepeticiones = false;
			$scope.dataNominal.precioRequerido = false;
			$scope.dataNominal.tasaRequerido = false;
			$scope.dataNominal.montoRequerido = false;
			$scope.dataNominal.valorRescateRequerido = false;
			$scope.dataNominal.fechaVencimientoRequerido = false;
		}
		if ($scope.dataNominal.oConfiguracionClasificacion.idActual === "IF"){
			$scope.dataNominal.activarEmisor = true;
			$scope.dataNominal.activarTipoReajuste = true;
			$scope.dataNominal.activarCantidad = false;
// MSH		$scope.dataNominal.activarNumeroRepeticiones = $scope.dataNominal.instrumento && $scope.dataNominal.instrumento.CorteMinimo >1? false:true;
			$scope.dataNominal.activarPrecio = true;
			$scope.dataNominal.activarTasa = false;
			$scope.dataNominal.activarMonto = false;
			$scope.dataNominal.activarValorRescate = false;
			$scope.dataNominal.activarTipoReajuste = true;
			$scope.dataNominal.activarMonedaLiquidacion = true;
			$scope.dataNominal.activarFechaVencimiento = false;

			$scope.dataNominal.emisorRequerido = true;
			$scope.dataNominal.tipoReajusteRequerido = true;
			$scope.dataNominal.cantidadRequerido = true;
// MSH		$scope.dataNominal.numeroRepeticionesRequerido = $scope.dataNominal.instrumento && $scope.dataNominal.instrumento.CorteMinimo >1? true:false;
// MSH		$scope.dataNominal.numeroRepeticiones = true;
			$scope.dataNominal.precioRequerido = false;
			$scope.dataNominal.tasaRequerido = false;
			$scope.dataNominal.montoRequerido = false;
			$scope.dataNominal.valorRescateRequerido = true;
			$scope.dataNominal.fechaVencimientoRequerido = true;
		}
		/*
		if ($scope.dataNominal.oConfiguracionEmisor){
			$scope.dataNominal.oConfiguracionEmisor.filtros.TieneInstrumentos.value = 'N';
			$scope.dataNominal.oConfiguracionEmisor.idActual = false;
			
		}*/
		if ($scope.dataNominal.instrumento){
			$scope.dataNominal.oConfiguracionInstrumentoTipoReajuste.filtros.IdInstrumento.value = parseInt($scope.dataNominal.instrumento.IdInstrumento);
			$scope.dataNominal.oConfiguracionInstrumentoTipoReajuste.filtros.IdTipoInstrumento.value = $scope.dataNominal.instrumento.IdTipoInstrumento;
			//$scope.dataNominal.oConfiguracionMonedaEmision.filtros.IdInstrumento.value = parseInt($scope.dataNominal.instrumento.IdInstrumento);
			//$scope.dataNominal.oConfiguracionMonedaEmision.filtros.IdTipoInstrumento.value = $scope.dataNominal.instrumento.IdTipoInstrumento;
			$scope.dataNominal.oConfiguracionEmisor.idActual = false;
			if ($scope.dataNominal.instrumento.IdEmisor !== '' && $scope.dataNominal.instrumento.IdEmisor !== 'OPTIMUS' && $scope.dataNominal.oConfiguracionEmisor){
				//$scope.dataNominal.oConfiguracionEmisor.filtros.TieneInstrumentos.value = 'S';
				$scope.dataNominal.oConfiguracionEmisor.idActual = $scope.dataNominal.instrumento.IdEmisor;
				$scope.dataNominal.activarEmisor = false;
				if ($scope.dataNominal.oConfiguracionEmisor.seleccionado && $scope.dataNominal.oConfiguracionEmisor.seleccionado.EmiteFungible === 'S'){
					$scope.dataNominal.switchFungible = $scope.dataNominal.oConfiguracionClasificacion.idActual !== 'RF'?true:false;
					$scope.dataNominal.PuedeCambiarFungible = $scope.dataNominal.oConfiguracionClasificacion.idActual !== 'RF'?true:false;
				}
			}
			if ($scope.dataNominal.instrumento.FechaVencimiento && $scope.dataNominal.oConfiguracionClasificacion.idActual === "RF"){
				$scope.dataNominal.kngFechaVencimiento = util.dateInicioDia($scope.dataNominal.instrumento.FechaVencimiento);
			}

			SetCorteMinimo();
			
		}
	};
	
	
	
	var setMonedasDecimalCartera = function (){
		if ($scope.dataNominal.instrumento){
			if ($scope.dataNominal.instrumento.IdClasificacionTipoInstrumento === 'RF'){
				$scope.dataNominal.IdMonedaPrecioDisplay = '';
				$scope.dataNominal.IdMonedaMontoDisplay = $scope.SesionIdMonedaSistema;
				$scope.dataNominal.IdMonedaRescateDisplay = $scope.dataNominal.instrumento.IdMoneda;
				//$scope.dataNominal.IdMonedaValorCaptacionDisplay = $scope.dataNominal.instrumento.IdMoneda;
			}
			else if ($scope.dataNominal.instrumento.IdClasificacionTipoInstrumento === 'IF'){
				$scope.dataNominal.IdMonedaPrecioDisplay = $scope.dataNominal.instrumento.IdMoneda;
				$scope.dataNominal.IdMonedaMontoDisplay = $scope.SesionIdMonedaSistema;
				$scope.dataNominal.IdMonedaRescateDisplay = $scope.dataNominal.oConfiguracionInstrumentoTipoReajuste.seleccionado.IdTipoReajuste;//$scope.dataNominal.oConfiguracionMonedaEmision.seleccionado.IdMonedaEmision;
				//$scope.dataNominal.IdMonedaValorCaptacionDisplay = $scope.dataNominal.oConfiguracionMonedaLiquidacionInstrumento.seleccionado.IdMonedaLiquidacion;//$scope.dataNominal.oConfiguracionMonedaEmision.seleccionado.IdMonedaEmision;
			}
			else{		
				$scope.dataNominal.IdMonedaPrecioDisplay = $scope.dataNominal.instrumento.IdMoneda;
				$scope.dataNominal.IdMonedaMontoDisplay = $scope.dataNominal.instrumento.IdMoneda;
				$scope.dataNominal.IdMonedaRescateDisplay = $scope.dataNominal.instrumento.IdMoneda;
				//$scope.dataNominal.IdMonedaValorCaptacionDisplay = $scope.dataNominal.instrumento.IdMoneda;
			}
			$scope.dataNominal.IdMonedaMontoLiquidacionDisplay = $scope.dataNominal.IdMonedaLiquidacionOperacion;//$scope.dataNominal.oConfiguracionMonedaLiquidacionInstrumento.seleccionado.IdMonedaLiquidacion;
			$scope.dataNominal.IdDecimalPrecioDisplay = (util.bcs_formato(0,'GEN','Precio',$scope.dataNominal.IdMonedaPrecioDisplay		,$scope.dataNominal.instrumento.IdTipoInstrumento,'')).cantidadDecimales;
			$scope.dataNominal.IdDecimalMontoDisplay = (util.bcs_formato(0,'GEN','Monto',$scope.dataNominal.IdMonedaMontoDisplay			,$scope.dataNominal.instrumento.IdTipoInstrumento,'')).cantidadDecimales;
			$scope.dataNominal.IdDecimalMontoLiquidacionDisplay = (util.bcs_formato(0,'GEN','Monto',$scope.dataNominal.IdMonedaLiquidacionOperacion			,$scope.dataNominal.instrumento.IdTipoInstrumento,'')).cantidadDecimales;//$scope.dataNominal.oConfiguracionMonedaLiquidacionInstrumento.seleccionado.IdMonedaLiquidacion
//			$scope.dataNominal.IdDecimalRescateDisplay 		= (util.bcs_formato(0,'MES','Cantidad',$scope.dataNominal.oConfiguracionInstrumentoTipoReajuste.idActual		,$scope.dataNominal.instrumento.IdTipoInstrumento,'')).cantidadDecimales;			
			$scope.dataNominal.IdDecimalValorCaptacionDisplay 	= (util.bcs_formato(0,'GEN','Monto',$scope.dataNominal.IdMonedaValorCaptacionDisplay	,$scope.dataNominal.instrumento.IdTipoInstrumento,'')).cantidadDecimales;

		}
	};
	var resetCartera = function (){
		// $scope.dataNominal.oConfiguracionBoveda.idDefecto = BovedaDefault;
		$scope.dataNominal.oConfiguracionBoveda.idActual = $scope.dataNominal.CodigoBovedaDefault;

		$scope.dataNominal.valorizar = true;
		$scope.dataNominal.oConfiguracionEmisor.idActual = false;
		$scope.dataNominal.oConfiguracionInstrumentoTipoReajuste.idActual = $scope.dataNominal.instrumento===undefined?false:$scope.dataNominal.instrumento.IdTipoReajuste;
		//$scope.dataNominal.oConfiguracionMonedaEmision.idActual = false;
		$scope.dataNominal.kngCantidad = '';
// MSH  $scope.dataNominal.kngNumeroRepeticiones = '';
		$scope.dataNominal.kngPrecio = '';
		$scope.dataNominal.kngPrecioMercado = '';
		$scope.dataNominal.kngTasa = '';
		$scope.dataNominal.kngMonto = '';
		$scope.dataNominal.kngValorRescate = '';
		$scope.dataNominal.kngFechaVencimiento = fechaVencimiento;//fechaSistema;
		//$scope.dataNominal.kngCorteMinimo = '';

		$scope.dataNominal.valoConfiguracionInstrumentoTipoReajuste = false;
		$scope.dataNominal.valkngCantidad = '';
//		$scope.dataNominal.valkngNumeroRepeticiones = '';
		$scope.dataNominal.valkngPrecio = '';
		$scope.dataNominal.valkngTasa = '';
		$scope.dataNominal.valkngMonto = '';
		$scope.dataNominal.valkngValorRescate = '';
		$scope.dataNominal.valkngFechaVencimiento = fechaVencimiento;//fechaSistema;
		$scope.dataNominal.esResetCartera = true;
		setMonedasDecimalCartera();
	};
	
	
	var valorizar = function(){

		var ParamMercado = new Object();
		var datos = new Object();
		var TasaValorizacion = 0;
		var PrecioValorizacion = 0;
		var IdMonedaEmisionInstrumento = '';
		
		bcsComunes.ventanaOverlay($scope.IdVentana,true);
		$scope.dataNominal.valorizar = false;
		
		if ($scope.dato.IdTipoOperacion === 'COMTER' ||  $scope.dato.IdTipoOperacion === 'VENTER'){
			if(util.dateSPdate($scope.dataNominal.kngFechaVencimiento) == '0001-01-01' || $scope.dataNominal.kngFechaVencimiento == ""){
				if($scope.dataNominal.instrumento.IndicadorBolson == "S" && $scope.dataNominal.instrumento.IdClasificacionTipoInstrumento == 'RF'){
					bcsComunes.msg("error","error",$translate.instant('UI_FECHA_VENCIMIENTO_INSTRUMENTO_INVALIDA'));
					bcsComunes.ventanaOverlay($scope.IdVentana,false);
					$scope.dataNominal.valorizar = true;
					return;
				}
			}
			if (fechaLiquidacionOperacion > $scope.dataNominal.kngFechaVencimiento){
				bcsComunes.msg("error","error",$translate.instant('UI_FECHA_LIQUIDACION_INVALIDA'));
				bcsComunes.ventanaOverlay($scope.IdVentana,false);
				$scope.dataNominal.valorizar = true;
				return;
			}
			if ($scope.dataNominal.IdMonedaLiquidacionOperacion != $scope.dataNominal.oConfiguracionMonedaLiquidacionInstrumento.seleccionado.IdMonedaLiquidacion){
				bcsComunes.msg("error","error",$translate.instant('UI_MONEDA_LIQUIDACION_INVALIDA'));
				bcsComunes.ventanaOverlay($scope.IdVentana,false);
				$scope.dataNominal.valorizar = true;
				return;
			}
		} 
		if ($scope.dato.IdTipoOperacion === 'COMPAC' ||  $scope.dato.IdTipoOperacion === 'VENPAC'){
			if(util.dateSPdate($scope.dataNominal.kngFechaVencimiento) == '0001-01-01' || $scope.dataNominal.kngFechaVencimiento == ""){
				if($scope.dataNominal.instrumento.IndicadorBolson == "S" && $scope.dataNominal.instrumento.IdClasificacionTipoInstrumento == 'RF'){
					bcsComunes.msg("error","error",$translate.instant('UI_FECHA_VENCIMIENTO_INSTRUMENTO_INVALIDA'));
					bcsComunes.ventanaOverlay($scope.IdVentana,false);
					$scope.dataNominal.valorizar = true;
					return;
				}
			}
			if (fechaVencimientoOperacion > $scope.dataNominal.kngFechaVencimiento){
				bcsComunes.msg("error","error",$translate.instant('UI_FECHA_LIQUIDACION_INVALIDA'));
				bcsComunes.ventanaOverlay($scope.IdVentana,false);
				$scope.dataNominal.valorizar = true;
				return;
			}
		}
		
		$scope.dataNominal.valkngValorRescate = $scope.dataNominal.kngValorRescate;
		$scope.dataNominal.valkngMonto = $scope.dataNominal.kngMonto;
		$scope.dataNominal.valkngPrecio = $scope.dataNominal.kngPrecio;
		$scope.dataNominal.valkngTasa = $scope.dataNominal.kngTasa;
		var corte = new Big($scope.dataNominal.kngCorteMinimo);
		var cantidad = new Big($scope.dataNominal.kngCantidad);
		var nominal = cantidad.mod(corte);
		if ( $scope.dataNominal.oConfiguracionClasificacion.idActual === "IF")	{
			if ( $scope.dataNominal.switchFungible === false ){
				$scope.dataNominal.PuedeCambiarFungible = false;
			}
			// Si esta buscando el nominal no corre esta validacion
			if ($scope.TipoIncognita !== 2) {
				var rescate = new Big($scope.dataNominal.kngValorRescate);
				nominal =  rescate.mod(corte);
			} else {
				nominal = Big(0);
			}
		}
		if (nominal.gt(0)){
			bcsComunes.msg("error","error",$translate.instant('CORTE_MINIMO_INVALIDO'));
			bcsComunes.ventanaOverlay($scope.IdVentana,false);
			$scope.dataNominal.valorizar = true;
			return;
		}

		
		// Esta buscando el valor rescate y las monedas son distintas ?
		$scope.MontoAValorizar = $scope.dataNominal.kngMonto;
		if ( ( $scope.TipoIncognita >= 1 ) &&
			 ( ( "CLP" !== $scope.dataNominal.oConfiguracionInstrumentoTipoReajuste.seleccionado.IdTipoReajuste ) ||
			   ( "CLP" !== $scope.dataNominal.oConfiguracionMonedaLiquidacionInstrumento.seleccionado.IdMonedaLiquidacion ) )
		   ) {
			// Viene desde calculo de paridades
			$scope.MontoAValorizar = $scope.MontoEquivalente;
		}

		// Busca la tasa?
		if ( $scope.TipoIncognita == 1 ) { 
			PrecioValorizacion = null;
			TasaValorizacion = null;

		} else {
			PrecioValorizacion = Number($scope.dataNominal.kngPrecio);
			if ( PrecioValorizacion !== 0 && $scope.dataNominal.oConfiguracionClasificacion.idActual === "RF" )
				// Si es RF solo se debe enviar 1 solo => Prevalece Precio
				TasaValorizacion = null;
			else
				TasaValorizacion = Number($scope.dataNominal.kngTasa);
				if ($scope.dataNominal.oConfiguracionClasificacion.idActual === "RF" ) {
					PrecioValorizacion = null;
				}
		}

		datos.p_IdEmpresa = $scope.SesionIdEmpresa;
		datos.p_IdUsuario = $scope.SesionIdUsuario;		
		datos.p_IdModulo = 'MES';
		datos.p_IdPais	= $scope.$root.SesionIdPaisSistema;
		datos.p_OrigenProcesamiento = 'MESA_UI'
		datos.p_IdMonedaAdicional = $scope.dataNominal.IdMonedaLiquidacionOperacion;
		datos.p_TicketProcesamiento	= 5604,
		datos.p_Instrumentos = [{
			IdValorizacion					: 1,
			FechaOperacion					: util.dateSPdate(fechaSistema),
			//FechaVencimientoOperacion 	: util.dateSPdate($scope.dataNominal.kngFechaVencimiento||fechaSistema),
			//PlazoEnDiasOperacion 			: 0,//$scope.formData.diasFechaVencimiento, //Solo para operaciones a plazo
			FechaValorizacion				: util.dateSPdate(fechaLiquidacionOperacion||fechaSistema),//$scope.SesionFechaSistema
			IdClasificacionTipoInstrumento	: $scope.dataNominal.instrumento.IdClasificacionTipoInstrumento, 			
			IdTipoInstrumento			: $scope.dataNominal.instrumento.IdTipoInstrumento, 
			IdInstrumento				: $scope.dataNominal.instrumento.IdInstrumento, 
			FechaVencimiento			: util.dateSPdate($scope.dataNominal.kngFechaVencimiento||fechaSistema),
			FechaEmision				: $scope.dataNominal.instrumento.FechaEmision||fechaSistema,
			IdMonedaInstrumento			: $scope.dataNominal.oConfiguracionMonedaLiquidacionInstrumento.seleccionado.IdMonedaLiquidacion,//$scope.dataNominal.instrumento.IdMonedaInstrumento,//****
			IdTipoReajusteInstrumento	: $scope.dataNominal.oConfiguracionInstrumentoTipoReajuste.seleccionado.IdTipoReajuste,
			Cantidad					: $scope.dataNominal.kngCantidad || null, 
			ValorRescate				: $scope.dataNominal.kngValorRescate || null,
			PrecioCompra				: PrecioValorizacion,
			TasaCompra					: TasaValorizacion,
			MontoCompra					: $scope.MontoAValorizar || null,//en moneda del instrumento	
			MontoCompraMonedaSistema	: $scope.dataNominal.kngMonto || null,	
			Emisor						: $scope.dataNominal.oConfiguracionEmisor.seleccionado.IdEmisor,
			PrecioSubyacente			: null, 
			TasaSubyacente				: null, 
			PrecioGarantia				: null, 
			TasaGarantia				: null, 
			TasaTIF						: null,		
			TieneCorteDeCupon			: null,
			IdCliente					: null, 
			IdPortafolio				: null,
			TablaOrigen					: null,	
			IdUso						: null, 
			TipoCartera					: null, 
			IdTipoCriterioTasa			: null,
			Nemotecnico					: $scope.dataNominal.instrumento.Nemotecnico,
			IdCondicionDeLiquidacion	: $scope.dato.IdCondicionDeLiquidacion,
			IdMonedaEmisionInstrumento	: $scope.dataNominal.oConfiguracionInstrumentoTipoReajuste.seleccionado.IdTipoReajuste
		}];
		bcsServicioPlataforma.ValorizarInstrumento(datos,function(respuesta)
		{	
			bcsComunes.ventanaOverlay($scope.IdVentana,false);
			
			if (respuesta.P_OUT_ERRORESCONTADOR > 0){
				//console.error('Error rutina de Valorizacion', $translate.instant('UI_ERROR_RUTINA_VALORIZACION'));
				var error = '';
				$.each(respuesta.P_OUT_MENSAJES,function(key,value) {
					if (value.tipo === 'ERROR'){
						error = value.descripcion;
					}
				});
				bcsComunes.msg("warning",'Valorizador',$translate.instant('UI_ERROR_RUTINA_VALORIZACION') + ': ' + error);
				$scope.dataNominal.kngMonto =  0;
				$scope.dataNominal.kngMontoLiquidacion = 0;
				$scope.dataNominal.kngMontoLiquidacionOperacion = 0;
				$scope.dataNominal.kngPrecio =  $scope.dato.IdClasificacionTipoInstrumento === "IF"?"":0;
				$scope.dataNominal.kngTasa =  0;
			}
			else if(respuesta.P_OUT_INSTRUMENTOSVALORIZADOS.length==0){
				console.error('Sin datos valorizados');
				var p=bcsComunes.ventanaMsgErr($translate.instant('UI_SIN_DATOS_VALORIZADOS'),idVentana);
				bcsComunes.ventanaPromesa(p).then(function(){		
					console.timeEnd(vtime);
					console.groupEnd(vgroup);
				});
				$scope.dataNominal.kngMonto =  0;
				$scope.dataNominal.kngMontoLiquidacion = 0;
				$scope.dataNominal.kngMontoLiquidacionOperacion = 0;
				$scope.dataNominal.kngPrecio =  $scope.dato.IdClasificacionTipoInstrumento === "IF"?"":0;
				$scope.dataNominal.kngTasa =  0;
			}else{
				$.each(respuesta.P_OUT_INSTRUMENTOSVALORIZADOS,function(key,value) {
					if (value.DimensionTasa === 'COMP' && value.DimensionTiempo === 'AC' && value.IdValorizacion === 1){	//A Tasa Compra
						console.info('Valorizacion sin Error');
						$scope.MontoEquivalenteUltimo = Number(value.MontoMonedaSistema || 0);
						$scope.dataNominal.kngMonto = Number(value.MontoMonedaSistema || 0);
						$scope.dataNominal.kngMontoLiquidacion = Number(value.MontoMonedaInstrumento || 0);
						$scope.dataNominal.kngMontoLiquidacionOperacion = Number(value.MontoMonedaAdicional || 0);
						$scope.dataNominal.kngPrecio = Number(value.Precio || 0);
						$scope.dataNominal.kngTasa = Number(value.Tasa || 0);

						if ( Number($scope.MontoAValorizar || 0) > 0 ) {
							$scope.dataNominal.MontoValorizadoTipoReajuste = $scope.MontoAValorizar;	
						} else if (value.ValorTipoReajuste > 0) {
							$scope.dataNominal.MontoValorizadoTipoReajuste = value.MontoMonedaSistema/value.ValorTipoReajuste;	
						}

						if ($scope.dataNominal.instrumento.IdClasificacionTipoInstrumento === 'IF') {
							$scope.dataNominal.kngPrecio = "";
							if ( $scope.TipoIncognita === 2 ) {
								// Solo sobre-escribe si esta buscando el valor rescate....
								$scope.dataNominal.kngValorRescate = Number(value.ValorRescate);
							}
						}
					}
					if (value.DimensionTasa === 'MERC' && value.DimensionTiempo === 'AC' && value.IdValorizacion === 1){	//Mercado
						console.info('Valorizacion sin Error');
						
						$scope.dataNominal.kngTasaMercado = Number(value.Tasa || 0);
						//if ($scope.dato.IdClasificacionTipoInstrumento==='RF') {
						$scope.dataNominal.kngPrecioMercado = Number(value.Precio || 0);
						//}
					}
					
				});
				
				$scope.dataNominal.valkngFechaVencimiento = $scope.dataNominal.kngFechaVencimiento;
				$scope.dataNominal.valoConfiguracionInstrumentoTipoReajuste = $scope.dato.IdTipoReajuste;

			}
			
			
		},function(respuesta)
		{
			$scope.dataNominal.kngMonto =  0;
			$scope.dataNominal.kngMontoLiquidacion = 0;
			$scope.dataNominal.kngMontoLiquidacionOperacion = 0;
			$scope.dataNominal.kngPrecio =  0;
			$scope.dataNominal.kngTasa =  0;
			//$scope.dataNominal.kngValorRescate = 0;
			bcsComunes.ventanaOverlay($scope.IdVentana,false);
		});
	};
	var comparaDataVal = function(){
		return (
			$scope.dataNominal.valoConfiguracionInstrumentoTipoReajuste === $scope.dataNominal.oConfiguracionInstrumentoTipoReajuste.idActual &&
			//$scope.dataNominal.valoConfiguracionInstrumentoMoneda === $scope.dataNominal.oConfiguracionMonedaEmision.idActual &&
			$scope.dataNominal.valkngCantidad === $scope.dataNominal.kngCantidad &&
			//$scope.dataNominal.valkngNumeroRepeticiones === $scope.dataNominal.kngNumeroRepeticiones &&
			$scope.dataNominal.valkngValorRescate === $scope.dataNominal.kngValorRescate &&
			$scope.dataNominal.valkngFechaVencimiento === $scope.dataNominal.kngFechaVencimiento &&
			$scope.dataNominal.valkngMonto === $scope.dataNominal.kngMonto &&
			$scope.dataNominal.valkngPrecio === $scope.dataNominal.kngPrecio &&
			$scope.dataNominal.valkngTasa === $scope.dataNominal.kngTasa) 
			
	}
	var valida = function(){
		var res = '';
		var cantidad = new Big($scope.dataNominal.kngCantidad);
		var corte = new Big($scope.dataNominal.kngCorteMinimo);
		var nominal = cantidad.mod(corte);
		if ( $scope.dataNominal.oConfiguracionClasificacion.idActual === "IF" && $scope.dataNominal.switchFungible === false )	{
			var rescate = new Big($scope.dataNominal.kngValorRescate);
			nominal = rescate.mod(corte);
		}
		if (nominal > 0){
			res = $translate.instant('CORTE_MINIMO_INVALIDO');
		}
		if (Number($scope.dataNominal.kngMonto) <= 0){
			res = $translate.instant('MONTO_MAYOR_CERO');
		}
		return res;
	};
	
	$scope.dataNominal.valoriza = function(){
	
		Mercado = $scope.dataNominal.oConfiguracionClasificacion.idActual
		Cantidad = Number($scope.dataNominal.kngCantidad);
		if ( Mercado === 'IF' ) {
			Cantidad = Number($scope.dataNominal.kngValorRescate);
		}

		Tasa = $scope.dataNominal.kngTasa === null || $scope.dataNominal.kngTasa === "" ? null : Number($scope.dataNominal.kngTasa)
		Precio = $scope.dataNominal.kngPrecio === null || Number($scope.dataNominal.kngPrecio) === 0 ? null : Number($scope.dataNominal.kngPrecio);
		Monto = Number($scope.dataNominal.kngMonto);		
	
		if ( Cantidad === 0 && Monto === 0 && Tasa === null && Precio === null ) {
			bcsComunes.msg("error","error", "UI_INGRESE_VALOR");
			return;
		} else if ( Cantidad > 0 &&  Monto > 0 && ( Tasa !== null || Precio != null ) ) {
			bcsComunes.msg("error","error", "UI_DEMASIADOS_VALORES");
			return;
		} else if ( Cantidad === 0  && Monto === 0 ) {
			bcsComunes.msg("error","error", "UI_FALTAM_VALORES");
			return;
		} else if ( Cantidad === 0 && Mercado === 'RF' ) {
			bcsComunes.msg("error","error", "UI_INGRESE_CANTIDAD");
			return;
		}
		//Agregar Error por corte = 0
		else if ($scope.dataNominal.kngCorteMinimo === 0 ) {
			bcsComunes.msg("error","error", "UI_CORTE_MINIMO");
			return;
		}

		
		// 0 - Nominal/Cantidad + Tasa => Mto. Valorizado
		$scope.TipoIncognita = 0;
		
		if ( Cantidad > 0 && Monto > 0 ) {
			// 1 - Nominal/Cantidad + Mto. Valorizado => Tasa
			$scope.TipoIncognita = 1;
		} else if ( Mercado === "IF" && Monto > 0 && Tasa !== null ){
			// 2 - Mto. Valorizado + Tasa => Cantidad 
			$scope.TipoIncognita = 2;
		} else if ( Mercado === "RF" && Monto > 0 && ( Tasa !== null || Precio != null ) ){
			// 2 - Mto. Valorizado + Tasa => Nominal 
			$scope.TipoIncognita = 2;
		}

		if ( ( $scope.TipoIncognita >= 1 ) &&
			 ( ( "CLP" !== $scope.dataNominal.oConfiguracionInstrumentoTipoReajuste.seleccionado.IdTipoReajuste ) ||
			   ( "CLP" !== $scope.dataNominal.oConfiguracionMonedaLiquidacionInstrumento.seleccionado.IdMonedaLiquidacion ) )
		   ) {
			// Calcula la paridad
			calcularParidadMonto();
		} else {
			valorizar();	
		}
		
	};
	
	
	$scope.dataNominal.cancelar = function(){
		$scope.dataNominal.valorizar = true;
		//$scope.dataNominal.kngTasa = '';
		if ( $scope.TipoIncognita === 0 ) {
			// 0 - Nominal/Cantidad + Tasa => Mto. Valorizado
			$scope.dataNominal.kngMonto = '';
			$scope.dataNominal.kngMontoLiquidacion = '';
			$scope.dataNominal.kngMontoLiquidacionOperacion = '';
		} else if ( $scope.TipoIncognita === 1 ) {
			// 1 - Nominal/Cantidad + Mto. Valorizado => Tasa
			$scope.dataNominal.kngPrecio = '';
			$scope.dataNominal.kngTasa = '';
		} else {
			// 2 - Mto. Valorizado + Tasa => Nominal/Cantidad
			if ( $scope.dataNominal.oConfiguracionEmisor.seleccionado.EmiteFungible !== "S") {
				$scope.dataNominal.kngCantidad = '0';
			}
			$scope.dataNominal.kngValorRescate = '';
		}

		//
	};
	$scope.dataNominal.cerrar = function() {
		//bcsComunes.ventanaData($scope.IdVentana,"CLICK BTN CERRAR");
		var data = {boton:'CANCELAR', data: {}};
		bcsComunes.ventanaData($scope.IdVentana,data);
		bcsComunes.ventanaCerrar($scope.IdVentana);
		
	};
	$scope.dataNominal.aceptar = function() {
		if($scope.dataNominal.instrumento.IdClasificacionTipoInstrumento=='IF' 
		&& $scope.dataNominal.instrumento.Reajustable=='S' 
		&& $scope.dataNominal.oConfiguracionInstrumentoTipoReajuste.seleccionado.IdTipoReajuste == 'UF'){		
			$scope.dataNominal.IdMonedaEmisionInstrumento = $scope.dataNominal.instrumento.IdMonedaEmision;
		}else{
			$scope.dataNominal.IdMonedaEmisionInstrumento = $scope.dataNominal.instrumento.IdClasificacionTipoInstrumento=='IF' && $scope.dataNominal.instrumento.Reajustable=='S' ?$scope.dataNominal.oConfiguracionInstrumentoTipoReajuste.seleccionado.IdTipoReajuste :$scope.dataNominal.instrumento.IdMonedaEmision;

		}

		var data = {boton:'ACEPTAR', data: $scope.dataNominal};
		var res = valida();
		if (res.length > 0){
			bcsComunes.msg("error","error",res);
			return;
		}
		bcsComunes.ventanaData($scope.IdVentana,data);
		bcsComunes.ventanaCerrar($scope.IdVentana);			
	};

	
	
	var calcularParidadMonto = function(){
		var decimales = 1;
		var data = Object();
		var IdMonedaDestino = "UF";

		if ( $scope.dataNominal.kngMonto === $scope.MontoEquivalenteUltimo ) {
			// Si no ha cambiado el monto calculado inicialmente... 
			valorizar();
		} else { 

			IdMonedaDestino = $scope.dataNominal.oConfiguracionMonedaLiquidacionInstrumento.seleccionado.IdMonedaLiquidacion === "CLP" ? 
							  $scope.dataNominal.oConfiguracionInstrumentoTipoReajuste.seleccionado.IdTipoReajuste : 
							  $scope.dataNominal.oConfiguracionMonedaLiquidacionInstrumento.seleccionado.IdMonedaLiquidacion;

			var datos = {
					p_IdEmpresa			: $scope.SesionIdEmpresa,
					p_IdUsuario			: $scope.SesionIdUsuario,
					p_IdModulo			: 'MFW',
					p_UsaReglasNegocio	: 'N',
					p_Fecha				: util.dateSPdate(fechaSistema),
					p_NumeroDias		: 1,
					p_EsHabil			: 'S',
					p_IdPais			: $scope.SesionIdPaisSistema
			};
			
			bcsServicioPlataforma.FechaAnterior(datos,function(respuesta) {
				if(respuesta.P_OUT_FECHASIGUIENTE && respuesta.P_OUT_ERRORESCONTADOR === 0){
					$scope.MontoEquivalente = -1;
					data.p_IdEmpresa =  $scope.SesionIdEmpresa;
					data.p_IdTrabajo =  1315;
					data.p_CalculosEntrada = [{
						"IdTrabajo"			: data.p_IdTrabajo
						,"IdInterno"		: 1
						,"Fecha"			: respuesta.P_OUT_FECHASIGUIENTE
						,"IdMonedaOrigen" 	: "CLP"
						,"IdMonedaDestino" 	: IdMonedaDestino
						,"ValorCambioLocalModificado" : 0
						,"MontoOrigen"		: $scope.dataNominal.kngMonto
						,"IdUso"			: 'MESA'
					}];
					bcsServiciosMesa.ObtenerParidadTipoCambio( data ).$promise.then(function(respuesta) {
						if (respuesta.p_out_CalculosSalida.length > 0){
							$scope.MontoEquivalente = Number(Number(respuesta.p_out_CalculosSalida[0].MontoDestino).toFixed($scope.dataNominal.IdDecimalRescateDisplay));
							// Ahora envia a valorizar...
							valorizar();
						}
					});
				}
				else{
					console.log("ERROR: " + JSON.stringify(respuesta));
				}
			},function(respuesta) {
				console.log("ERROR: " + JSON.stringify(respuesta));
			});
		};
	};

	

	
	/****************
	 * 
	 * 
	 * watches
	 * 
	 * 
	 * ******************/
	$scope.$watch('dataNominal.instrumento'/*'dataNominal.instrumento.IdInstrumento'*/, function(newval,oldval ){
		if ($scope.dataNominal.instrumento /*&& $scope.dataNominal.instrumento.IdInstrumento*/) {			
			resetCartera();
			enableCarteraOperacion();
		}
		
	},true);
	$scope.$watch('dataNominal.oConfiguracionInstrumentoTipoReajuste.idActual', function(newval,oldval ){
		if ($scope.dataNominal.oConfiguracionInstrumentoTipoReajuste && $scope.dataNominal.oConfiguracionInstrumentoTipoReajuste.idActual) {
			setMonedasDecimalCartera();
			
			// $scope.dataNominal.kngCorteMinimo = $scope.dataNominal.oConfiguracionInstrumentoTipoReajuste.seleccionado.CorteMinimo || $scope.dataNominal.instrumento.CorteMinimo;
			SetCorteMinimo();
			
			if ($scope.dataNominal.instrumento && $scope.dataNominal.instrumento.IdTipoInstrumento){
				$scope.dataNominal.oConfiguracionMonedaLiquidacionInstrumento.filtros.IdTipoInstrumento.value = $scope.dataNominal.instrumento.IdTipoInstrumento;
				$scope.dataNominal.oConfiguracionMonedaLiquidacionInstrumento.filtros.IdMoneda.value = $scope.dataNominal.oConfiguracionInstrumentoTipoReajuste.idActual;
				$scope.dataNominal.oConfiguracionMonedaLiquidacionInstrumento.idDefecto = $scope.dataNominal.instrumento.IdClasificacionTipoInstrumento === 'RF'?$scope.dataNominal.instrumento.IdMoneda : $scope.dato.IdMonedaLiquidacion;
				//$scope.dataNominal.oConfiguracionMonedaLiquidacionInstrumento.idActual = $scope.dataNominal.instrumento.IdClasificacionTipoInstrumento === 'RF'?$scope.dataNominal.instrumento.IdMoneda :$scope.dato.IdMonedaLiquidacion;
				//$scope.dataNominal.oConfiguracionMonedaLiquidacionInstrumento.idPostFiltro = $scope.dataNominal.instrumento.IdClasificacionTipoInstrumento === 'RF'?$scope.dataNominal.instrumento.IdMoneda : $scope.dato.IdMonedaLiquidacion;
			}
			if($scope.dataNominal.esResetCartera && $scope.dataNominal.oConfiguracionInstrumentoTipoReajuste.seleccionado.Nemotecnico == "PAGARE R"){
				$scope.dataNominal.oConfiguracionInstrumentoTipoReajuste.seleccionado = null;
				$scope.dataNominal.oConfiguracionInstrumentoTipoReajuste.idActual = null;
				$scope.dataNominal.esResetCartera = false;
			}			
		}
		
	});
	$scope.$watch('dataNominal.oConfiguracionMonedaLiquidacionInstrumento.idActual', function(newval,oldval ){
		if ($scope.dataNominal.oConfiguracionMonedaLiquidacionInstrumento && $scope.dataNominal.oConfiguracionMonedaLiquidacionInstrumento.idActual) {
			setMonedasDecimalCartera();
		}

		if ( ($scope.dato.IdTipoOperacion === 'COMTER' ||  $scope.dato.IdTipoOperacion === 'VENTER') && 
			 ($scope.dataNominal.oConfiguracionMonedaLiquidacionInstrumento.seleccionado.IdMonedaLiquidacion !== undefined && $scope.dataNominal.IdMonedaLiquidacionOperacion != $scope.dataNominal.oConfiguracionMonedaLiquidacionInstrumento.seleccionado.IdMonedaLiquidacion) ){
			bcsComunes.msg("error","error",$translate.instant('UI_MONEDA_LIQUIDACION_INVALIDA'));
			bcsComunes.ventanaOverlay($scope.IdVentana,false);
		}


		
	});/*
	$scope.$watch('dataNominal.oConfiguracionMonedaEmision.idActual', function(newval,oldval ){
		if ($scope.dataNominal.oConfiguracionMonedaEmision && $scope.dataNominal.oConfiguracionMonedaEmision.idActual && $scope.dataNominal.instrumento && $scope.dataNominal.instrumento.IdTipoInstrumento){
			setMonedasDecimalCartera();
			$scope.dataNominal.oConfiguracionMonedaLiquidacionInstrumento.filtros.IdTipoInstrumento.value = $scope.dataNominal.instrumento.IdTipoInstrumento;
			$scope.dataNominal.oConfiguracionMonedaLiquidacionInstrumento.filtros.IdMoneda.value = $scope.dataNominal.oConfiguracionMonedaEmision.idActual;
		}
	});*/
	
	$scope.$watch('dataNominal.oConfiguracionEmisor.idActual', function(newval,oldval ){
		if(newval !== oldval && newval && $scope.dataNominal.oConfiguracionEmisor ) {
			if( $scope.dataNominal.oConfiguracionClasificacion.idActual === 'IF' ) {				
				$scope.dataNominal.switchFungible = true;
				$scope.dataNominal.PuedeCambiarFungible = true;
				$scope.dataNominal.kngCantidad = 1;
				$scope.dataNominal.activarCantidad = true;
				$scope.dataNominal.activarValorRescate = false;
				$scope.dataNominal.activarTipoReajuste = true;
				$scope.dataNominal.activarMonedaLiquidacion = true;
				$scope.dataNominal.valorRescateRequerido = true;

				if ($scope.dataNominal.oConfiguracionEmisor.idActual === 'CENTRAL'&& 
				   ( $scope.dataNominal.oConfiguracionInstrumentoTipoReajuste.seleccionado.IdTipoInstrumento !== 'PDBC' || 
				     $scope.dataNominal.oConfiguracionInstrumentoTipoReajuste.seleccionado.IdTipoInstrumento !== 'PRBC') ) {
					// IF + CENTRAL => EL INSTRUMENTO DEBE SER PDBC
					$scope.dataNominal.setInstrumento = "PDBC";
					$scope.dataNominal.switchFungible = false;
					$scope.dataNominal.PuedeCambiarFungible = false;
				}
				
			} else {
				$scope.dataNominal.PuedeCambiarFungible = false;
				$scope.dataNominal.switchFungible = false;
				$scope.dataNominal.activarCantidad = false; 
				$scope.dataNominal.kngCantidad = '';
				$scope.dataNominal.activarValorRescate = true;
				$scope.dataNominal.activarTipoReajuste = false;
				$scope.dataNominal.activarMonedaLiquidacion = false;
				$scope.dataNominal.valorRescateRequerido = false;				
			}
			
			SetCorteMinimo();
		}
		/*
		if( $scope.dataNominal.oConfiguracionClasificacion.idActual === 'IF' && 
				$scope.dataNominal.oConfiguracionEmisor.idActual === 'CENTRAL'&& 
				( $scope.dataNominal.oConfiguracionInstrumentoTipoReajuste.seleccionado.IdTipoInstrumento !== 'PDBC' || 
				  $scope.dataNominal.oConfiguracionInstrumentoTipoReajuste.seleccionado.IdTipoInstrumento !== 'PRBC') ) {
				// IF + CENTRAL => EL INSTRUMENTO DEBE SER PDBC
				$scope.dataNominal.setInstrumento = "PDBC";
			}
		
		if ($scope.dataNominal.oConfiguracionEmisor.idActual && $scope.dataNominal.oConfiguracionEmisor.seleccionado.EmiteFungible === "S" ){
			$scope.dataNominal.switchFungible = $scope.dataNominal.oConfiguracionClasificacion.idActual !== 'RF'?true:false;
			$scope.dataNominal.PuedeCambiarFungible = $scope.dataNominal.oConfiguracionClasificacion.idActual !== 'RF'?true:false;
			$scope.dataNominal.kngCantidad = 1;
			//$scope.dataNominal.kngCorteMinimo = 1;
			$scope.dataNominal.activarCantidad = true;
			$scope.dataNominal.activarValorRescate = $scope.dataNominal.oConfiguracionClasificacion.idActual !== 'RF'?false:true;
			$scope.dataNominal.activarTipoReajuste = $scope.dataNominal.oConfiguracionClasificacion.idActual !== 'RF'?true:false;
			$scope.dataNominal.activarMonedaLiquidacion = $scope.dataNominal.oConfiguracionClasificacion.idActual !== 'RF'?true:false;
			$scope.dataNominal.valorRescateRequerido = $scope.dataNominal.oConfiguracionClasificacion.idActual !== 'RF'?true:false;
			//$scope.dataNominal.kngCorteMinimo = 1;
			
		}
		else {
			
			//if ($scope.dataNominal.oConfiguracionEmisor.idActual && $scope.dataNominal.oConfiguracionEmisor.seleccionado.EmiteFungible === "S" && !$scope.dataNominal.switchFungible) {
			//	// Era fungible y ahora NO => Corte Minimo = 1
			//	$scope.dataNominal.kngCorteMinimo = 1;
			//

			$scope.dataNominal.PuedeCambiarFungible = false;
			$scope.dataNominal.switchFungible = false;
			$scope.dataNominal.activarCantidad = false; 
			$scope.dataNominal.kngCantidad = '';
			$scope.dataNominal.activarValorRescate = $scope.dataNominal.oConfiguracionClasificacion.idActual !== 'RF'?false:true;
			$scope.dataNominal.activarTipoReajuste = $scope.dataNominal.oConfiguracionClasificacion.idActual !== 'RF'?true:false;
			$scope.dataNominal.activarMonedaLiquidacion = $scope.dataNominal.oConfiguracionClasificacion.idActual !== 'RF'?true:false;
			$scope.dataNominal.valorRescateRequerido = $scope.dataNominal.oConfiguracionClasificacion.idActual !== 'RF'?true:false;
		}
		*/

	});
	$scope.$watch('dataNominal.oConfiguracionClasificacion.idActual', function(newval,oldval ){
		$scope.dataNominal.oConfiguracionBoveda.idDefecto = BovedaDefault;
		enableCarteraOperacion();
		if (newval === 'IF') {
			if ( $scope.dataNominal.IdMonedaLiquidacionOperacion === 'CLP' ) {
				$scope.dataNominal.setInstrumento = "PAGARE NR";
			} else {
				$scope.dataNominal.setInstrumento = "PAGARE R";
			}
		} else {
			$scope.dataNominal.setInstrumento = "-1";
			$scope.dataNominal.activarValorRescate = true;
			$scope.dataNominal.activarTipoReajuste = false;
			$scope.dataNominal.activarMonedaLiquidacion = false;
		}
		resetCartera();
		if ($scope.dataNominal.oConfiguracionClasificacion.seleccionado && $scope.dataNominal.oConfiguracionClasificacion.seleccionado.IdClasificacionTipoInstrumento === 'IF'){
			$scope.dataNominal.oConfiguracionEmisor.filtros = {};
			$scope.dataNominal.oConfiguracionEmisor.filtros.EmiteIIF = { "field"   : "EmiteIIF","operator": "eq", "value"   :'S'};
			$scope.dataNominal.oConfiguracionEmisor.filtros.IdEstado = { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" };
			$scope.dataNominal.oConfiguracionEmisor.filtros.read = true;
		}
		if ($scope.dataNominal.oConfiguracionClasificacion.seleccionado && $scope.dataNominal.oConfiguracionClasificacion.seleccionado.IdClasificacionTipoInstrumento === 'RF'){
			$scope.dataNominal.oConfiguracionEmisor.filtros = {};
			$scope.dataNominal.oConfiguracionEmisor.filtros.EmiteIRF = { "field"   : "EmiteIRF","operator": "eq", "value"   :'S'};
			$scope.dataNominal.oConfiguracionEmisor.filtros.IdEstado = { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" };
			$scope.dataNominal.oConfiguracionEmisor.filtros.read = true;
		}
	});
	
	$scope.$watch('dataNominal.kngValorRescate', function(newval,oldval ){

		var NroTmp;
		if ($scope.dataNominal.kngValorRescate && Number($scope.dataNominal.kngValorRescate) > 0 && newval !== oldval &&
			$scope.dataNominal.oConfiguracionClasificacion.idActual === "IF" && $scope.dataNominal.switchFungible === false &&
			$scope.dataNominal.instrumento && Number($scope.dataNominal.kngCorteMinimo) !== 0){
			
			if($scope.dataNominal.instrumento.IdEmisor !== "CENTRAL") {
				// IF No fungible => Corte Minimo = V.Rescate / Cantidad
				NroTmp = Number($scope.dataNominal.kngValorRescate)/Number($scope.dataNominal.kngCantidad);
				$scope.dataNominal.kngCorteMinimo =  NroTmp; 
				$scope.dataNominal.UltimoCorteMinimo = $scope.dataNominal.kngCorteMinimo;
			} else {
				// PDBC รณ PRBC => Cantidad = V.Rescate / Corte Minimo 
				$scope.dataNominal.kngCantidad = Math.trunc(Number($scope.dataNominal.kngValorRescate) / Number($scope.dataNominal.kngCorteMinimo));	
				// En caso que la cantidad no varie, pero el Val. Rescate no sea multiplo, vuelvo a calcular el Valor Rescate. (EJ 10.000.000 > 14.000.000)
				$scope.dataNominal.kngValorRescate = Math.trunc(Number($scope.dataNominal.kngCantidad) * Number($scope.dataNominal.kngCorteMinimo));
			}
			
		}
	});

	
	$scope.$watch('dataNominal.switchFungible', function(newval,oldval ){
		if ( newval !== oldval) {
			if ( $scope.dataNominal.oConfiguracionClasificacion.idActual === 'IF' ) {
				if ( newval ) {
					$scope.dataNominal.kngCantidad = 1;
					//$scope.dataNominal.kngCorteMinimo = 1;
					$scope.dataNominal.activarCantidad = true;
				} else {
					$scope.dataNominal.activarCantidad = false;
				}
			}
		}
	});	


	$scope.$watch('dataNominal.kngCorteMinimo', function(newval,oldval ){
		if(newval != oldval) {
			if($scope.dataNominal.UltimoCorteMinimo  !== newval) {
				$scope.dataNominal.kngCorteMinimo = $scope.dataNominal.UltimoCorteMinimo;
				console.info('Truco, porque fuera de este Form Setea otro monto....');
			}
		}
	});
	

	$scope.$watch('dataNominal.kngCantidad', function(newval,oldval ){
		
		var NroTmp;
		if ($scope.dataNominal.kngCantidad && Number($scope.dataNominal.kngCantidad) > 0 && newval !== oldval &&
				$scope.dataNominal.oConfiguracionClasificacion.idActual === "IF" && $scope.dataNominal.switchFungible === false && 
				Number($scope.dataNominal.instrumento && $scope.dataNominal.kngCorteMinimo) !== 0) {
				
				if ( Number(oldval) === 0 && Number($scope.dataNominal.kngValorRescate) > 0  ) {
					// Ya hay valor de rescate => calculo Corte Minimo de acuerdo a la cantidad ingresada.
					// $scope.dataNominal.kngCorteMinimo = Math.trunc(Number($scope.dataNominal.kngValorRescate) / Number($scope.dataNominal.kngCantidad));
					NroTmp = Number($scope.dataNominal.kngValorRescate)/Number($scope.dataNominal.kngCantidad);
					$scope.dataNominal.kngCorteMinimo = NroTmp; 
					$scope.dataNominal.UltimoCorteMinimo = $scope.dataNominal.kngCorteMinimo;
				} else {
					NroTmp = Number($scope.dataNominal.kngCantidad) * Number($scope.dataNominal.kngCorteMinimo);
					$scope.dataNominal.kngValorRescate = NroTmp; 
				}
			}
		
	});
	
	
	
	
	/****************************************
	 *
	 * Logica
	 *
	 * **************************************/
	if ($scope.dato == undefined || $scope.dato=="") {
		$scope.dato="SIN PARAMETRO";
	}

	$scope.dataNominal.oConfiguracionBoveda.idDefecto = BovedaDefault;
	
});
