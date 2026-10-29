/***************************************************************
 * Purpose	: Controlador de ventana Ingreso/Retiro de títulos
 * Author	: TEAM CUSTODIA - BCS
 * Date		: 30/08/2016 [Norm.]
 
 * Purpose	: Modificación lógica preparacion datos enviar a 
 * valorizar. Cambio en definición de watch y check diferencias.
 * Author	: JGarces - BCS
 * Date		: 17/03/2017
 ***************************************************************/
 (function() {
	angular.module('ModuloCustodia').controller('IngresarTituloCtrl',
	function($scope,$translate,$timeout,bcsServicioPlataforma,$modal,$state,bcsServiciosCustodia,bcsServiciosValorizacion,bcsComunes,bcsAlerta){
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var valorizadorEnEjecucion = true;
		
		$scope.data = {};
		$scope.dataUltimaVal = {};
		$scope.data.options = {};
		$scope.data.oDesplegar = {};
		$scope.data.oDesplegar.esCtaPropia = false;
		$scope.data.oDecimales = {};
		$scope.data.oDecimales.valorRescate = {};
		$scope.data.oDecimales.precio = {};
		$scope.data.oDecimales.precioPorcentaje = {};
		$scope.data.oDecimales.monto = {};
		$scope.data.oDecimales.montoValorizado = {};
		$scope.data.oDecimales.montoValorizadoMonedaOrigen = {};
		$scope.data.oDecimales.corteMinimo = {};
		$scope.data.oDecimales.cantidad = 0;
		$scope.data.oDecimales.tasa = 0;
		$scope.instrumentoCustodia = {};
		$scope.labelMontoReferencia = 'UI_MONTO';
		$scope.data.comprobanteReferenciaOptions = {format: 'n0',min:0,spinners: false};
		$scope.ventanaId = bcsComunes.ventanaWinId($scope);
		var idVentana = bcsComunes.ventanaWinId($scope); 
	    $scope.win = {};
		var calcularFechaEnEjecucion = false;
		var flimpiarEnEjecucion = false;

		 /*********************************************
		 * COMBOBOX
		 *********************************************/
		$scope.data.oConfiguracionClasificacion={
			tipo : "combobox-clasificacion-tipo-instrumento",
			configuracion : {},
			filtros : {
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
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
	
		$scope.oSocioNegocioDestino={
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
		
		$scope.oPortafolioDestino={
			tipo : "combobox-portafolio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 },
				IdPortafolio : { "field": "IdPortafolio","operator": "ne", "value"   :  0 }
			},
		};
		
		$scope.oPortafolioDestino2={
			tipo : "combobox-portafolio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
			},
		};

		$scope.data.oConfiguracionTipoInstrumento={
			tipo : "combobox-tipo-instrumento",
			configuracion : {},
			filtros : {
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "VIG" },
				//IdBolsa : { "field"   : "IdBolsa","operator": "eq", "value"   : $scope.SesionBolsaDefectoSistema },
				IdClasificacionTipoInstrumento : { "field"   : "IdClasificacionTipoInstrumento","operator": "eq", "value"   : "" },
				IdTipoInstrumentoExcluir : { "field"   : "IdTipoInstrumento","operator": "ne", "value"   : "ORF" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};
		
		$scope.data.oConfiguracionOperacion={
			tipo : "combobox-operacion",
			configuracion : {},
			filtros : {
				IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value"   : "" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};
	
		$scope.data.oConfiguracionCondicionLiquidacion={
			tipo : "combobox-condicion-liquidacion",
			configuracion : {},
			filtros : {
				IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   : "" },
				EstadoTipoOperacionYCondicionDeLiquidacion : { "field"   : "EstadoTipoOperacionYCondicionDeLiquidacion","operator": "eq", "value"   : "ACT" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};
	
		$scope.data.oConfiguracionBoveda={
			tipo : "combobox-boveda",
			configuracion : {},
			filtros : { 	
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "VIG" }
			},
			idActual : undefined,
			seleccionado : undefined,
			idDefecto : ''
		};
		
		$scope.data.oConfiguracionBovedaDestino={
			tipo : "combobox-boveda",
			configuracion : {},
			filtros : { 	
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "VIG" }
			},
			idActual : undefined,
			seleccionado : undefined,
			idDefecto : ''
		};
		
		$scope.FiltroConceptoTipoOperacion 					= { "field"   : "IdTipoOperacion","operator": "eq", "value"   : "" };
		$scope.FiltroConceptoClasificacionTipoInstrumento	= { "field"   : "IdClasificacionTipoInstrumento","operator": "eq", "value"   : "" };		
		$scope.data.oConfiguracionConcepto={
				tipo : "combobox-concepto-custodia",
				configuracion : {},			
				filtros : {
					complejo : [$scope.FiltroConceptoTipoOperacion,$scope.FiltroConceptoClasificacionTipoInstrumento],
				  	logic: "and",
				  	filters: [					
						{ "field"   : "NombreEntidad","operator": "eq", "value"   : "CUS.INGRESORETIROTITULO" },
						{ "field"   : "ConfiguradoEnModulo","operator": "eq", "value"   : "S" },
						{ "field"	: "IdEstado","operator": "eq", "value"   : "VIG" },
						{ "field" 	: "VisibleOptimus","operator": "eq", "value" : "S" },
						{
						    logic: "or",
						    filters: [{ "field"   : "IdTipoOperacion","operator": "eq", "value"   : "-" }, $scope.FiltroConceptoTipoOperacion]
						},
						{
						    logic: "or",
						    filters: [{ "field"   : "IdClasificacionTipoInstrumento","operator": "eq", "value"   : "-" },$scope.FiltroConceptoClasificacionTipoInstrumento]
						}
				  	]},
				idActual : false,
				seleccionado : false,
				idDefecto : ''
		};
		
		$scope.data.oConfiguracionEmisor={
			tipo : "combobox-emisor",
			configuracion : {},
			filtros : {
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
				IdEmisor : { "field"   : "IdEmisor","operator": "neq", "value"   : "" },
				IdEmiteIIF : { "field"   : "EmiteIIF","operator": "contains", "value"   : "" },
				IdEmiteIRF : { "field"   : "EmiteIRF","operator": "contains", "value"   : "" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};
	
		$scope.data.oConfiguracionTipoReajuste={
			tipo : "combobox-instrumento-tipo-reajuste",
			configuracion : {
				template: '<span >#: IdTipoReajuste # - #: NombreTipoReajuste #</span>'
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
		
		$scope.data.oConfiguracionMonedaEmision={
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

/*		$scope.data.oConfiguracionMonedaPagoInstrumento={
			tipo : "combobox-tipo-instrumento-moneda-liquidacion",
			configuracion : {
				template: '<span >#: IdMonedaLiquidacion # - #: NombreMonedaLiquidacion #</span>'
			},
			filtros : {
				IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value"   : "" },
				IdMoneda : { "field"   : "IdMoneda","operator": "eq", "value"   : "" }
			},
			idActual : false,
			idDefecto : ''
		};
*/	
		$scope.data.oConfiguracionMonedaPagoInstrumento={
			configuracion : {
				template: '<span >#: IdMonedaLiquidacion #</span>',
				dataTextField: 'IdMonedaLiquidacion'
			},
			filtros : {
				IdTipoInstrumento	: { "field"   : "IdTipoInstrumento","operator": "eq", "value"   : "" },
				IdMoneda			: { "field"   : "IdMoneda","operator": "eq", "value"   : "" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto: false,
			idPostFiltro : false
		};

		$scope.data.oConfiguracionCuentaCustodioPortafolio={
			tipo : "combobox-cuenta-custodio-portafolio",
			configuracion : {},
			filtros : {
				IdEstado 		: { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
				IdPortafolio	: { "field"   : "IdPortafolio","operator": "eq", "value"   : 0 },
				IdBoveda		: { "field"   : "IdBoveda","operator": "eq", "value"   : 0 }
				//IdCuentaCustodioPortafolio		: { "field"   : "IdCuentaCustodioPortafolio","operator": "eq", "value"   : 0 }
			},
			idActual : false,
			seleccionado : false,
			idDefecto: false,
			idPostFiltro : false
		};

		$scope.data.oConfiguracionCuentaCustodioPortafolioDestino={
			tipo : "combobox-cuenta-custodio-portafolio",
			configuracion : {},
			filtros : {
				IdEstado 		: { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
				IdPortafolio	: { "field"   : "IdPortafolio","operator": "eq", "value"   : 0 },
				IdBoveda		: { "field"   : "IdBoveda","operator": "eq", "value"   : 0 }
			},
			idActual : false,
			seleccionado : false,
			idDefecto: false,
			idPostFiltro : false
		};
		
		$scope.data.oConfiguracionClasificacionIfrs = {
				tipo : "combobox-codigo-ifrs",
				configuracion : {
					//template: '<span >#: Codigo # - #: Nombre #</span>'
				},
				filtros : {
					Codigo : { "field"   : "Codigo" , "operator": "eq", "value"   : "" }
				}
			};
		
		/*********************************************
		 * FUNCIONES BLUR
		 *********************************************/
		$scope.win.blurCantidad = function() {
			v_IdClas = '';
			v_corteMin = 0;

			if((!$scope.data.instrumento && $scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion === 'INGTIT') || (!$scope.instrumentoCustodia && $scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion === 'RETTIT')) return;
			if ((typeof $scope.data.instrumento === 'undefined' && $scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion === 'INGTIT') ||  (typeof $scope.instrumentoCustodia === 'undefined' && $scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion === 'RETTIT')) return;
			
			if ($scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion === 'INGTIT'){
				v_IdClas = $scope.data.instrumento.IdClasificacionTipoInstrumento;

				if (v_IdClas==='IF' && !$scope.data.checkFungible){
					v_corteMin = $scope.data.kngCorteMinimo;
				} else {
					v_corteMin = $scope.data.instrumento.CorteMinimo >0 ? $scope.data.instrumento.CorteMinimo : 1;	
				}				
			} else if ($scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion === 'RETTIT'){
				v_IdClas = $scope.instrumentoCustodia.IdClasificacionTipoInstrumento;
				v_corteMin = $scope.instrumentoCustodia.CorteMinimo;
			} else return;

			if(v_IdClas === 'RF'){
				if (v_corteMin===0)return;
				if ($scope.SesionLocale === 'CL'){
					var corte = new Big(v_corteMin);
    				var cantidad = new Big($scope.data.kngCantidad);
    				var nominal = cantidad.mod(corte);
					if(nominal > 0){
						bcsComunes.msg("warning",'Cantidad','No es múltiplo del corte mínimo.');
						$scope.data.kngCantidad = 0;
						$scope.data.kngMonto = undefined;
						$scope.data.kngMontoValorizado = undefined;
						$scope.data.kngMontoValorizadoMonedaOrigen = undefined;
						$scope.data.kngMontoValorizadoMonedaAdicional = undefined;
						$scope.data.kngCantidad.foco = true;
						return;
					}	
				}
				if ($scope.SesionLocale === 'PE'){
					if ($scope.data.kngCantidad !== undefined && $scope.data.kngCantidad > 0)
						$scope.data.kngValorNominal = v_corteMin * $scope.data.kngCantidad;
				}
			}
			if(v_IdClas === 'IF'){
				if(v_corteMin>0) {
					$scope.data.kngValorRescate = v_corteMin * $scope.data.kngCantidad;
				}
				if($scope.data.kngCantidad>=0 && v_corteMin == 0){
					var CorteMin = v_corteMin === 0 ? 1 : v_corteMin;
					
/*					if ($scope.data.kngCantidad>$scope.data.kngValorRescate){
						bcsComunes.msg("warning",'Valor Rescate','Debe ser mayor a la cantidad (Corte calculado)');
						return false;
					}
					else */
					if (!$scope.data.checkFungible) {
						$scope.data.kngValorRescate = CorteMin * $scope.data.kngCantidad;
					}
					else {						
						$scope.data.kngCantidad = 1;
					}
				};
			}
			
			$scope.win.blurPrecioTasa();
		}
		$scope.win.blurValorRescate = function() {
			v_IdClas = '';
			v_corteMin = 0;

			if((!$scope.data.instrumento && $scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion === 'INGTIT') || (!$scope.instrumentoCustodia && $scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion === 'RETTIT')) return;
			if ((typeof $scope.data.instrumento === 'undefined' && $scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion === 'INGTIT') ||  (typeof $scope.instrumentoCustodia === 'undefined' && $scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion === 'RETTIT')) return;
			
			
			if ($scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion === 'INGTIT'){
				v_IdClas = $scope.data.instrumento.IdClasificacionTipoInstrumento;

				if (v_IdClas==='IF' && !$scope.data.checkFungible){
					v_corteMin = $scope.data.kngCorteMinimo;
				} else {
					v_corteMin = $scope.data.instrumento.CorteMinimo >0 ? $scope.data.instrumento.CorteMinimo : 1;	
				}				
			} else if ($scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion === 'RETTIT'){
				v_IdClas = $scope.instrumentoCustodia.IdClasificacionTipoInstrumento;
				v_corteMin = $scope.instrumentoCustodia.CorteMinimo;
			} else return;
			
			if(v_IdClas === 'IF'){				
				//if(v_corteMin>0) {
				//	$scope.data.kngValorRescate = v_corteMin * $scope.data.kngCantidad;
				//}
				if($scope.data.kngCantidad>=0){
				//if($scope.data.kngCantidad>=0 && v_corteMin == 0){
					var CorteMin = v_corteMin === 0 ? 1 : v_corteMin;
					
					/*if ($scope.data.kngCantidad>$scope.data.kngValorRescate){
						bcsComunes.msg("warning",'Valor Rescate','Debe ser mayor a la cantidad (Corte calculado)');
						return false;
					}
					else*/
					if (!$scope.data.checkFungible) {
						$scope.data.kngValorRescate = CorteMin * $scope.data.kngCantidad;
					}
					else {						
						$scope.data.kngCantidad = 1;
					}
				}	
			}
			
			$scope.win.blurPrecioTasa();
		};
		$scope.win.blurValorNominal = function() {
			v_IdClas = '';
			v_corteMin = 0;
			
			if ($scope.data.kngValorNominal === undefined) {
				$scope.data.kngValorNominal = 0;
			}
			
			if((!$scope.data.instrumento && $scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion === 'INGTIT') || (!$scope.instrumentoCustodia && $scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion === 'RETTIT')) return;
			if ((typeof $scope.data.instrumento === 'undefined' && $scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion === 'INGTIT') ||  (typeof $scope.instrumentoCustodia === 'undefined' && $scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion === 'RETTIT')) return;
			
			if ($scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion === 'INGTIT'){
				v_IdClas = $scope.data.instrumento.IdClasificacionTipoInstrumento;

				if (v_IdClas==='IF' && !$scope.data.checkFungible){
					v_corteMin = $scope.data.kngCorteMinimo;
				} else {
					v_corteMin = $scope.data.instrumento.CorteMinimo >0 ? $scope.data.instrumento.CorteMinimo : 1;	
				}				
			} else if ($scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion === 'RETTIT'){
				v_IdClas = $scope.instrumentoCustodia.IdClasificacionTipoInstrumento;
				v_corteMin = $scope.instrumentoCustodia.CorteMinimo;
			} else return;

			if(v_IdClas === 'RF'){
				if (v_corteMin===0)return;
				if ($scope.SesionLocale === 'PE'){
					if($scope.data.kngValorNominal % v_corteMin !==0){
						bcsComunes.msg("warning",'Valor Nominal','No es múltiplo del corte mínimo.');
						$scope.data.kngCantidad = 0;
						$scope.data.kngMonto 			= undefined;
						$scope.data.kngMontoValorizado 	= undefined;
						$scope.data.kngMontoValorizadoMonedaOrigen = undefined;
						$scope.data.kngMontoValorizadoMonedaAdicional = undefined;
						$scope.data.kngValorNominal.foco = true;
						return;
					}
					if ($scope.data.kngValorNominal !== undefined && $scope.data.kngValorNominal > 0)
						$scope.data.kngCantidad = $scope.data.kngValorNominal / v_corteMin;
				}
			}
			
			$scope.win.blurPrecioTasa();
		};
		$scope.win.blurCorteMinimo = function() {			
			if($scope.data.instrumento.IdClasificacionTipoInstrumento === 'IF' && !$scope.data.checkFungible && !$scope.data.kngDisabledCorteMinimo){
				$scope.data.kngValorRescate = $scope.data.kngCorteMinimo * $scope.data.kngCantidad;
			}
		};
		$scope.win.blurPrecioTasa = function() {
			$scope.data.kngPrecio				= !$scope.data.disabled.precio ? $scope.data.kngPrecio : undefined;				
			$scope.data.kngPrecioPorcentaje		= !$scope.data.disabled.precioPorcentaje ? $scope.data.kngPrecioPorcentaje : undefined;				
			$scope.data.kngTasa 				= !$scope.data.disabled.tasa ? $scope.data.kngTasa : undefined;
			$scope.data.kngTasa_ 				= !$scope.data.disabled.tasa_ ? $scope.data.kngTasa_ : undefined;
		};
		
		/*********************************************
		 * CONFIGURACION AL INICIO
		 *********************************************/
		//Configurar al principio
		$scope.data.kngFechaOperacion = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.kngPlazo = 1;
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		var f_limpiar = function() {
			$scope.data.instrumento={};
			//$scope.data.kngFechaEmision = util.dateInicioDia($scope.SesionFechaSistema);
			
			$scope.data.kngFechaEmision = undefined;
			$scope.data.fechaVencimientoOptions = {min:util.dateInicioDia($scope.SesionFechaSistema)};	
			$scope.data.fechaEmisionOptions = {max:util.dateInicioDia($scope.SesionFechaSistema)};
			$scope.data.fechaAdquisicionOptions = {max:util.dateInicioDia($scope.SesionFechaSistema)};
			$scope.data.oConfiguracionBoveda.activado = true;
			$scope.data.oConfiguracionEmisor.idActual = false;
			$scope.data.oConfiguracionTipoReajuste.filtros.IdInstrumento.value = 0;
			$scope.data.oConfiguracionMonedaEmision.filtros.IdInstrumento.value = 0;
			$scope.data.oConfiguracionMonedaPagoInstrumento.filtros.IdTipoInstrumento.value = 0;
			$scope.data.monedaValorRescate = $scope.SesionIdMonedaSistema;
			$scope.data.monedaMonto = $scope.SesionIdMonedaSistema;
			$scope.data.monedaMontoValorizado = $scope.SesionIdMonedaSistema;
			$scope.data.monedaMontoValorizadoMonedaOrigen = $scope.SesionIdMonedaSistema;
			$scope.data.monedaCorteMinimo = "";
// Se eliminan decimales solo n
			$scope.data.precioPorcentajeOptions = {format: 'n'+(util.bcs_formato(0,'GEN','Precio',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
			$scope.data.precioPorcentajeOptions = {format: 'c',min:0,spinners: false};			
			$scope.data.oDecimales.tasa.decimales 			= 4;
			$scope.data.oDecimales.tasa.decimales_ 			= 4;
			$scope.data.options.tasa 	= {min:-999999999999999999999};			
			$scope.data.options.tasa_ 	= {min:-999999999999999999999};			
			
			
			$scope.data.oDecimales.valorRescate.decimales	= (util.bcs_formato(0,'GEN','Monto',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales;
			$scope.data.oDecimales.precio.decimales 		= (util.bcs_formato(0,'GEN','Precio',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales;
			$scope.data.oDecimales.monto.decimales 			= (util.bcs_formato(0,'GEN','Monto',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales;
			$scope.data.oDecimales.montoValorizado.decimales = (util.bcs_formato(0,'GEN','Monto',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales;
			$scope.data.oDecimales.montoValorizadoMonedaOrigen.decimales = (util.bcs_formato(0,'GEN','Monto',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales;
			$scope.data.oDecimales.cantidad 				= (util.bcs_formato(0,'GEN','Cantidad',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales;
			$scope.data.oDecimales.corteMinimo.decimales 	= 0;			
			$scope.data.plazoOptions 		= {format: 'n0',min:0,spinners: false};
			$scope.data.folioCarteraOptions = {format: 'n0',min:0,spinners: false};
			$scope.data.buscarInstrumento 	= "-1";
			$scope.data.kngCorteMinimo 		= 1;
			$scope.data.kngFechaAdquisicion = undefined;
			$scope.data.kngPrecio = undefined;
			$scope.data.kngPrecioPorcentaje = undefined;
			$scope.data.kngMonto = undefined;
			$scope.data.kngMontoValorizado = undefined;
			$scope.data.kngMontoValorizadoMonedaOrigen = undefined;
			$scope.data.kngMontoValorizadoMonedaAdicional = undefined;
			$scope.data.kngCantidad = undefined;
			$scope.data.kngTasa = undefined;
			$scope.data.kngTasa_ = undefined;
			$scope.data.kngInteresesCorridos = undefined;
			$scope.data.kngReajusteVacCorrido = undefined;
			$scope.data.kngComprobanteReferencia = undefined;
			if ($scope.data.oDesplegar.showValorRescate === true) $scope.data.kngValorRescate = undefined;
			else $scope.data.kngValorRescate = 1;
			
			if ($scope.data.oDesplegar.showValorNominal === true) $scope.data.kngValorNominal = undefined;
			else $scope.data.kngValorNominal = 1;
			
			$scope.data.kngFolioCartera = 0;
			$scope.data.checkFungible = false;
			$scope.data.kngDisabledCorteMinimo = true;
			$scope.instrumentoCustodia = {};
			//Mercado
			$scope.data.kngMontoReferencia= undefined;
			$scope.data.kngMontoValorizadoReferencia = undefined;
			$scope.data.kngMontoValorizadoMonedaOrigenReferencia = undefined;
			$scope.data.kngTasaReferencia = undefined;
			$scope.data.kngTasaReferencia_ = undefined;
			$scope.data.kngPrecioReferencia = undefined;
			$scope.data.kngPrecioPorcentajeReferencia = undefined;
			$scope.data.valorizadoMercado = false;
			$scope.data.kngReajusteVacCorridoAMercado = undefined;
			$scope.data.kngInteresesCorridosAMercado = undefined;
		};
		
		$scope.data.btnLimpiar = function() {
			valorizadorEnEjecucion = false;
			calcularFechaEnEjecucion = false;
			bcsComunes.setConflictos([]);
			$scope.data.accionDisabled = true;
			$scope.data.ngComentario = '';
			$scope.data.oConfiguracionBovedaDestino.seleccionado =  undefined;
			$scope.data.oConfiguracionCuentaCustodioPortafolioDestino.seleccionado = undefined;
			$scope.oSocioNegocioDestino.seleccionado = undefined;
			$scope.data.oDesplegar.esCtaPropia = false;
			$scope.oPortafolioDestino.seleccionado = undefined;
			$scope.data.ngComprobanteReferencia = 0;
			$scope.data.checkPorCartera = false;
//CGO
			$scope.data.oConfiguracionConcepto.idActual= false;
			$scope.data.oConfiguracionTipoReajuste.idActual = false;
			$scope.data.oConfiguracionMonedaEmision.idActual = false;
			$scope.data.oConfiguracionMonedaPagoInstrumento.idActual = false;
			$scope.oSocioNegocio.seleccionado= undefined;			
			$scope.oSocioNegocio.idActual = false;
			$scope.data.oConfiguracionBoveda.idActual= false;			
//CGO				
			f_limpiar();
			bcsComunes.ventanaTamanoAutomatico($scope.ventanaId,20);
		};
		
		//Configurar al principio
		$scope.data.btnLimpiar();
	
		$scope.data.btnConsultarCarteras = function(){
			if ($scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion == 'INGTIT'){
				return;
			}
			if ($scope.data.oConfiguracionClasificacion.seleccionado === false){
				bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("DEBE_INGRESAR_CLASIFICACION_INSTRUMENTO"));
				return
			}
			if ($scope.data.oConfiguracionTipoInstrumento.seleccionado === false){
				bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_SELECCIONAR_TIPO_INSTRUMENTO"));
				return
			}
			if ($scope.oSocioNegocio.seleccionado === false){
				bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_SELECCIONAR_CLIENTE"));
				return
			}
			if ($scope.oPortafolio.seleccionado === false){
				bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_SELECCIONAR_PORTAFOLIO"));
				return
			}
			if ($scope.data.oConfiguracionCondicionLiquidacion.seleccionado === false){
				bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_SELECCIONAR_CONDICION_LIQUIDACION"));
				return
			}
			var v_fecha = '0001-01-01';
			if ($scope.data && typeof $scope.data.kngFechaCondicionLiquidacion !== 'undefined')
				 v_fecha = util.dateSPdate($scope.data.kngFechaCondicionLiquidacion);
			var datos = {};
			datos.Filtrado 							= true;
			datos.IdClasificacionTipoInstrumento	= $scope.data.oConfiguracionClasificacion.seleccionado.IdClasificacionTipoInstrumento || '';
			datos.IdTipoInstrumento					= $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento || '';
			datos.IdCliente= $scope.oSocioNegocio.seleccionado.IdCliente || -1;
			datos.IdPortafolio = $scope.oPortafolio.seleccionado.IdPortafolio || -1;
			datos.FechaCondicionLiquidacion =  v_fecha;
			datos.SocioNegocio = $scope.oSocioNegocio.seleccionado.KdataTextTemplate;
			datos.Portafolio = $scope.oPortafolio.seleccionado.KdataTextTemplate;
			$scope.data.checkPorCartera = true;
			var id1 = bcsComunes.ventanaAbrir("ConsultarCarterasModal",datos,false, false, idVentana); 			
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){
	        	if (data && data.IdInstrumento){
	        		$scope.instrumentoCustodia = data;
	        		$scope.data.oConfiguracionBoveda.idActual = data.IdBoveda;
	        		
	        		$scope.data.oConfiguracionCuentaCustodioPortafolio.filtros.IdCuentaCustodioPortafolio = data.IdCuentaCustodioPortafolio;
	        		$scope.data.oConfiguracionCuentaCustodioPortafolio.idPostFiltro = data.IdCuentaCustodioPortafolio;
	        		//$scope.data.oConfiguracionCuentaCustodioPortafolio.idActual = data.IdCuentaCustodioPortafolio;
	        		
	        		if($scope.data.oConfiguracionClasificacion.idActual == 'IF'){
	        			$scope.data.kngCantidad = $scope.instrumentoCustodia.ValorNominal;
						$scope.data.oDecimales.valorRescate.decimales 	= (util.bcs_formato(0,'GEN','Monto',$scope.instrumentoCustodia.IdTipoReajuste,'','')).cantidadDecimales;
	        		}else{
	        			$scope.data.kngCantidad = $scope.instrumentoCustodia.Cantidad;	
	        		}
	        		$scope.data.kngCantidadMax = $scope.data.kngCantidad;
		        	
		        	$scope.data.checkFungible =($scope.instrumentoCustodia.EsFungible ==='S')? true : false;
		        	if($scope.data.oConfiguracionClasificacion.idActual == 'RF'){
		        		$scope.data.kngPrecio = $scope.instrumentoCustodia.ValorizaPor == 'P' ? Number($scope.instrumentoCustodia.Precio) : undefined;
			        	$scope.data.kngTasa = $scope.instrumentoCustodia.ValorizaPor == 'T' ? Number($scope.instrumentoCustodia.Tasa) : undefined;
		        	}else{
		        		$scope.data.kngPrecio = Number($scope.instrumentoCustodia.Precio);
			        	$scope.data.kngTasa = Number($scope.instrumentoCustodia.Tasa);
		        	}
		        	$scope.data.monedaValorRescate								= $scope.instrumentoCustodia.IdTipoReajuste;
		        	$scope.data.kngPrecioPorcentaje 							= Number($scope.instrumentoCustodia.Precio);
		        	$scope.data.kngTasa_ 										= Number($scope.instrumentoCustodia.Tasa);
		        	$scope.data.kngValorRescate 								= 0;
		        	$scope.data.kngValorRescate 								= $scope.instrumentoCustodia.ValorRescate;
		        	
//		        	$scope.data.kngMonto = $scope.instrumentoCustodia.Monto;		        	
		        	$scope.instrumentoCustodia.NombreCompletoTipoReajuste 		= $scope.instrumentoCustodia.IdTipoReajuste.concat(' - ',$scope.instrumentoCustodia.NombreTipoReajuste);
		        	$scope.instrumentoCustodia.NombreCompletoMonedaEmision 		= $scope.instrumentoCustodia.IdMonedaEmision;
		        	$scope.instrumentoCustodia.NombreCompletoMonedaInstrumento	= $scope.instrumentoCustodia.IdMoneda;

		        	
		        	if ($scope.SesionLocale === 'PE'){
		        		$scope.data.kngCantidad = $scope.instrumentoCustodia.Repeticiones;
		        		$scope.data.kngValorNominal = $scope.instrumentoCustodia.ValorNominal;
		        	};
		        	
					if($scope.instrumentoCustodia.CorteMinimo>0){
						$scope.data.enabledValorRescate = true;
						$scope.data.kngCorteMinimo = $scope.instrumentoCustodia.CorteMinimo;
					}; 
					
		        	try {
		        		$scope.data.fechaVencimientoOptions = {min:util.dateInicioDia($scope.instrumentoCustodia.FechaVencimiento)};	
						$scope.data.fechaEmisionOptions = {max:util.dateInicioDia($scope.instrumentoCustodia.FechaEmision)};
						$scope.data.kngFechaVencimiento = util.dateInicioDia($scope.instrumentoCustodia.FechaVencimiento);
						$scope.data.kngFechaEmision = util.dateInicioDia($scope.instrumentoCustodia.FechaVencimiento);
						//$scope.data.kngPlazo = $scope.data.kngPlazo = Math.round(util.datediff(util.dateInicioDia($scope.instrumentoCustodia.FechaVencimiento),util.dateInicioDia($scope.instrumentoCustodia.FechaEmision)).asDays());
		        	}catch (e){
		        		console.log(e);
		        	}
		        	//$scope.data.kngValorRescate = Number($scope.instrumentoCustodia.ValorRescate);
		        	$scope.data.kngFolioCartera = $scope.instrumentoCustodia.FolioRegistro;
	        	} else {
	        		$scope.data.checkPorCartera = false;
	        	}
	    	}); 
		};
		
		ObtenerPrecio = function()
	    {
			if (typeof $scope.data.instrumento !== 'undefined' && 
				typeof $scope.instrumentoCustodia !== 'undefined' && 
				typeof $scope.data.oConfiguracionClasificacion.seleccionado !== 'undefined' && $scope.data.oConfiguracionClasificacion.seleccionado !== false &&
				typeof $scope.data.oConfiguracionTipoInstrumento.seleccionado !== 'undefined' && $scope.data.oConfiguracionTipoInstrumento.seleccionado !== false &&
				typeof $scope.data.oConfiguracionOperacion.seleccionado !== 'undefined' && $scope.data.oConfiguracionOperacion.seleccionado !== false){
				
				var datos = new Object();
				var v_instrumento = $scope.data.instrumento.IdInstrumento || 0
				var v_nemoInstrumento = $scope.data.instrumento.Nemotecnico || '';
				
				var v_IdTipo = $scope.data.instrumento.IdTipoInstrumento || '';
				var v_IdClasificacion = $scope.data.instrumento.IdClasificacionTipoInstrumento || '';
				//Para ingreso de titulos el precio viene en el instrumento, no es necesario obtenerlo.
				if ($scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion == 'INGTIT' || $scope.data.checkPorCartera==true){
					if($scope.data.instrumento.IdClasificacionTipoInstrumento == 'RV') {
						if($scope.data.instrumento.PrecioMercado>0){
							$scope.data.kngPrecio = $scope.data.instrumento.ValorizaPor === 'P' ? $scope.data.instrumento.PrecioMercado : undefined;
							$scope.data.kngPrecioReferencia = $scope.data.instrumento.PrecioMercado;
						}
					}else {
						if($scope.data.instrumento.PrecioMercado>0){
							$scope.data.kngPrecioPorcentaje = $scope.data.instrumento.ValorizaPor === 'P' ? $scope.data.instrumento.PrecioMercado : undefined;
							$scope.data.kngPrecioPorcentajeReferencia = $scope.data.instrumento.PrecioMercado;
						}

						if($scope.data.instrumento.TasaMercado>0){
							$scope.data.kngTasaReferencia = $scope.data.instrumento.ValorizaPor === 'T' ? $scope.data.instrumento.TasaMercado : undefined;
							$scope.data.kngTasaReferencia_ = $scope.data.instrumento.TasaMercado;
						}
					}
					return;
				} else {
					datos.p_Instrumentos = [{ 
						IdEmpresa 					: $scope.SesionIdEmpresa,
						IdInstrumento				: v_instrumento,
						IdBolsa 					: $scope.SesionBolsaDefectoSistema , 
						Fecha 						: $scope.SesionFechaSistema,
						IdCondicionDeLiquidacion 	: $scope.data.oConfiguracionCondicionLiquidacion.seleccionado.IdCondicionDeLiquidacion || 'T2'
						}
					];
					bcsServicioPlataforma.ObtenerPrecioNew(datos,function(respuesta){	
						bcsComunes.ventanaOverlay($scope.ventanaId,false);
						if (respuesta.P_OUT_INSTRUMENTO){
							if($scope.data.instrumento.IdClasificacionTipoInstrumento == 'RV') {
								$scope.data.kngPrecio = Number(respuesta.P_OUT_INSTRUMENTO[0].PrecioCierre);
								$scope.data.kngPrecioReferencia = Number(respuesta.P_OUT_INSTRUMENTO[0].PrecioCierre);
							}
							else {
								$scope.data.kngPrecioPorcentaje = Number(respuesta.P_OUT_INSTRUMENTO[0].PrecioCierre);
								$scope.data.kngPrecioPorcentajeReferencia = Number(respuesta.P_OUT_INSTRUMENTO[0].PrecioCierre);
								//TODO: Falta traer la tasa!
							}	
						}else{
							$scope.data.kngPrecio = undefined;
							$scope.data.kngPrecioReferencia = undefined;
						}
					},function(respuesta){
						$scope.data.kngPrecio = 0;
						bcsComunes.ventanaOverlay($scope.ventanaId,false)
					});
				}
			}
		};	
		
		$scope.data.oConfiguracionConcepto.fnSeleccionado = function (newval, oldval) {			
			if (!$scope.data.oConfiguracionConcepto.seleccionado) return;			
			$scope.data.checkCertificadoAutomatico = ($scope.data.oConfiguracionConcepto.seleccionado.ConCertificadoAutomatico ==='S')? true : false;
			$scope.oSocioNegocioDestino.idActual = -1;
			
			if ($scope.data.oConfiguracionConcepto.seleccionado.GeneraTransferenciaBoveda === 'S'){ 
				$scope.data.oDesplegar.showPortafolioDestino = false;
				$scope.data.oDesplegar.showClienteDestino = false;
				$scope.data.oDesplegar.showBovedaDestino = true;
			}else{
				$scope.data.oDesplegar.showBovedaDestino = false;
			}
			if ($scope.data.oConfiguracionConcepto.seleccionado.EsTransferenciaPortafolio === 'S'){
				$scope.data.oDesplegar.showBovedaDestino = false;
				$scope.data.oDesplegar.showClienteDestino = false;
				$scope.data.oDesplegar.showPortafolioDestino = true;
				$scope.data.oDesplegar.ClienteDestino = $scope.oSocioNegocio.seleccionado?$scope.oSocioNegocio.seleccionado.KdataTextTemplate:'';				
				
				$scope.oSocioNegocioDestino.idActual = $scope.oSocioNegocio.idActual;				
			}else{
				$scope.data.oDesplegar.showPortafolioDestino = false;
			}
			if ($scope.data.oConfiguracionConcepto.seleccionado.EsTransferenciaCliente === 'S'){
				$scope.data.oDesplegar.showBovedaDestino = false;
				$scope.data.oDesplegar.showPortafolioDestino = true;
				$scope.data.oDesplegar.showClienteDestino = true;
				$scope.data.oDesplegar.ClienteDestino = $scope.oSocioNegocio.seleccionado?$scope.oSocioNegocio.seleccionado.KdataTextTemplate:'';
			}else{
				$scope.data.oDesplegar.showClienteDestino = false;
			}
			bcsComunes.ventanaTamanoAutomatico($scope.ventanaId);
		};
		
		$scope.$watch('data.oConfiguracionConcepto.seleccionado',$scope.data.oConfiguracionConcepto.fnSeleccionado);
		

		//$scope.$watchGroup(['data.kngMonto','data.kngCantidad','data.kngPrecio','data.kngPrecioPorcentaje','data.kngTasa','data.kngTasa_', 'data.kngValorRescate','data.instrumento','data.oConfiguracionTipoInstrumento.seleccionado','data.oConfiguracionClasificacion.seleccionado','data.oConfiguracionBolsa.seleccionado', 'data.kngFechaVencimiento'], fnValorizador );		


/****************************************************
 *  NUEVA VALORIZACION - 17/03/2017
 * --------------------------------------------------
 * -Se utilizan watch independientes por mercado
 * -Se implementa uso validador genérico(configurado)
 * -Se implementan funciones para preparar y procesar
 * datos valorizados
 ****************************************************/	
		//##############################################
		//	Funciones utilitarias de la valorización
		//##############################################
		//funcion activa/desactiva flag valorización y overlay ventana.
		var valorizando = function(valor){
			valorizadorEnEjecucion=valor;
			bcsComunes.ventanaOverlay($scope.ventanaId,valor);
		};
		//funcion genera template entrada valorizador
		var generaCabeceraValorizar = function(clasificacion, monedaLiq){
			return {
				p_IdEmpresa 		: $scope.$root.SesionIdEmpresa,
				p_IdUsuario 		: $scope.$root.SesionIdUsuario,
				p_IdModulo 			: 'CUS',
				p_IdPais 			: $scope.$root.SesionIdPaisSistema,  
				p_OrigenProcesamiento : 'CUSTODIA_UI',
				p_IdMonedaAdicional : monedaLiq,
				p_Instrumentos		: [],
			};
		};
		var generaEntradaInstrumento = function(idVal){
			return {
				IdValorizacion				: idVal,
				FechaValorizacion			: util.dateSPdate($scope.data.kngFechaCondicionLiquidacion), //$scope.$root.SesionFechaSistema,
				FechaOperacion				: util.dateSPdate($scope.data.kngFechaOperacion),
//				FechaVencimientoOperacion	: util.dateSPdate($scope.data.kngFechaCondicionLiquidacion),
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
		//funcion limpiar montos valorización, luego de enviar a valorizar
		var limpiarMonto = function(valorizaMercado){
			if(valorizaMercado){
				$scope.data.kngMontoReferencia = undefined;
				$scope.data.kngMontoValorizadoReferencia = undefined;
				$scope.data.kngMontoValorizadoMonedaOrigenReferencia = undefined;
			}else{
				$scope.data.kngMonto = undefined;
				$scope.data.kngMontoValorizado = undefined;
				$scope.data.kngMontoValorizadoMonedaOrigen = undefined;
				$scope.data.kngMontoValorizadoMonedaAdicional = undefined;
			}
		};
		//funcion para llamar al servicio de valorizacion
		var callValorizador = function(cval,IdClasificacionTipoInstrumento,valorizarMercado){
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
					
					//console.log(inicioLog+'Resolviendo datos de valorización');
					$.each(respuesta.P_OUT_INSTRUMENTOSVALORIZADOS,function(key,item) {
						if (item.DimensionTasa === 'COMP'){
							if (item.TipoValorizacion === 'PQ'){								
								$scope.data.kngMonto	 								= Number(item.MontoMonedaSistema);
								$scope.data.kngMontoValorizado 							= Number(item.MontoMonedaInstrumento);
								$scope.data.kngMontoValorizadoMonedaOrigen 				= Number(item.MontoMonedaInstrumento);
								$scope.data.kngMontoValorizadoMonedaOrigenReferencia	= Number(item.MontoMonedaAdicional);
								$scope.data.kngCantidad 								= Number(item.Cantidad);
								$scope.data.kngPrecio 									= Number(item.Precio);
							}
							else {
								$scope.data.kngMonto						= item.MontoMonedaSistema;
								$scope.data.kngMontoValorizado 				= item.MontoMonedaSistema;
								//$scope.data.kngMontoValorizadoMonedaOrigen	= item.MontoMonedaInstrumento;
								$scope.data.kngMontoValorizadoMonedaOrigen	= item.MontoMonedaAdicional;
								$scope.data.kngMontoValorizadoMonedaAdicional = item.MontoMonedaAdicional;
								$scope.data.kngTasa 						= item.Tasa;
								$scope.data.kngTasa_ 						= item.Tasa;
								$scope.data.kngPrecio 						= item.Precio;
								$scope.data.kngPrecioPorcentaje 			= item.Precio || 0;
								$scope.data.kngInteresesCorridos 			= item.MontoInteresCorteCuponMonedaInstrumento;
								$scope.data.kngReajusteVacCorrido			= item.MontoReajusteMonedaInstrumento;	
							}
						}
						
						if (item.DimensionTasa === 'MERC'){
							if (item.TipoValorizacion === 'PQ'){
								$scope.data.kngPrecioReferencia 						= Number(item.Precio);
								$scope.data.kngMontoValorizadoReferencia 				= Number(item.MontoMonedaSistema);
								$scope.data.kngMontoValorizadoMonedaOrigenReferencia	= Number(item.MontoMonedaInstrumento);
							}else {
								$scope.data.kngMontoReferencia				= Number(item.MontoMonedaSistema);
								$scope.data.kngMontoValorizadoReferencia 	= Number(item.MontoMonedaSistema);
								//$scope.data.kngMontoValorizadoMonedaOrigenReferencia = Number(item.MontoMonedaInstrumento);
								$scope.data.kngMontoValorizadoMonedaOrigenReferencia = Number(item.MontoMonedaAdicional);
								$scope.data.kngTasaReferencia 				= Number(item.Tasa);
								$scope.data.kngTasaReferencia_ 				= Number(item.Tasa);
								$scope.data.kngPrecioReferencia 			= Number(item.Precio);
								$scope.data.kngPrecioPorcentajeReferencia 	= Number(item.Precio);
								$scope.data.kngReajusteVacCorridoAMercado 	= item.MontoReajusteMonedaInstrumento;
								$scope.data.kngInteresesCorridosAMercado	= item.MontoInteresMonedaInstrumento;
							}
						}
					});
					$timeout(function(){valorizando(false);},200);//evita que inmediatamente valorice tras cambio de valores.
				},function(error){
					bcsComunes.ventanaOverlay(idVentana,false);
					//console.log(inicioLog+'Error al invocar al valorizador');
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
		
		
		//##############################################
		//		Fn principal valorización RV
		//##############################################
		var fnvalorizarRV = function(Cantidad,Precio,Monto){
			console.log('[VALORIZACION RV]: Iniciando validaciones');
			var IdOperacion = $scope.data.oConfiguracionOperacion.idActual;
			var Instrumento=undefined;
			if(IdOperacion=='INGRESO')Instrumento=($scope.data.instrumento)?$scope.data.instrumento:undefined;
			if(IdOperacion=='RETIRO')
				if($scope.data.checkPorCartera) Instrumento=($scope.instrumentoCustodia)?$scope.instrumentoCustodia:undefined;
				else Instrumento=($scope.data.instrumento)?$scope.data.instrumento:undefined;
			if(!Instrumento){
				console.log('[VALORIZACION RV]: No se ha seleccionado el instrumento. Saliendo del proceso');
				//bcsComunes.msg('error',$translate.instant("UI_ERROR"), '[VALORIZACION RV]: No se ha seleccionado el instrumento. Saliendo del proceso');
				valorizando(false);
				return;
			}
			
			var IdClasificacionTipoInstrumento 	= Instrumento.IdClasificacionTipoInstrumento;
			var Cantidad 						= Cantidad;
			var Precio 							= Precio;
			var Monto 							= Monto;
			var PrecioReferencia				= $scope.data.kngPrecioReferencia;

			var condicion;
			if(Cantidad > 0 && Precio >= 0 && (Monto === undefined || Monto === 0)) condicion='A';
			else if(Cantidad > 0 && Precio > 0 && Monto > 0) condicion='TODOS';
			else{
				console.log('[VALORIZACION RV]: No cumple condiciones');
				//bcsComunes.msg('error',$translate.instant("UI_ERROR"), '[VALORIZACION RV]: No cumple condiciones');
				valorizando(false);
				return;
			}

			var valorizarMercado=false;
			if(!PrecioReferencia || PrecioReferencia==0) console.log('[VALORIZACION RV]: No hay precio de mercado')
			else{
				valorizarMercado=true;
				console.log('[VALORIZACION RF]: Se valorizará a mercado');
				//bcsComunes.msg('error',$translate.instant("UI_ERROR"), '[VALORIZACION RV]: Se valorizará a mercado');
			}

//			var cval=generaCabeceraValorizar($scope.data.oConfiguracionClasificacion.idActual);	
			var dvalCompra=generaEntradaInstrumento(1);
			dvalCompra.IdTipoInstrumento			=Instrumento.IdTipoInstrumento;
			dvalCompra.IdInstrumento				=Instrumento.IdInstrumento;
			dvalCompra.Nemotecnico					=Instrumento.Nemotecnico;
			if (Instrumento.IdClasificacionTipoInstrumento=='RV'){
				var cval								= generaCabeceraValorizar($scope.data.oConfiguracionClasificacion.idActual, ($scope.data.kngIdMonedaPagoInstrumento));
				dvalCompra.IdMonedaInstrumento			= Instrumento.IdMoneda;
			}else{
				var cval								= generaCabeceraValorizar($scope.data.oConfiguracionClasificacion.idActual);	
				dvalCompra.IdMonedaInstrumento			= $scope.data.kngIdMonedaPagoInstrumento || $scope.instrumentoCustodia.NombreCompletoMonedaInstrumento;
			};
			dvalCompra.IdMonedaEmisionInstrumento	=$scope.data.kngIdMonedaEmision || $scope.instrumentoCustodia.NombreCompletoMonedaEmision;
			dvalCompra.IdTipoReajusteInstrumento	=$scope.data.kngIdTipoReajuste || Instrumento.IdTipoReajuste;			
			dvalCompra.Cantidad						=Cantidad||null;
			dvalCompra.PrecioCompra					=Precio||null;
			dvalCompra.MontoCompra					=Monto||null;
			dvalCompra.IdClasificacionTipoInstrumento	=Instrumento.IdClasificacionTipoInstrumento;
			cval.p_Instrumentos.push(dvalCompra);
			callValorizador(cval,IdClasificacionTipoInstrumento,valorizarMercado);
		};
		//##############################################
		//		Fn principal valorización RF
		//##############################################
		var fnvalorizarRF = function(Cantidad,Precio,Tasa,Monto){
			console.log('[VALORIZACION RF]: Iniciando validaciones');

			var IdOperacion = $scope.data.oConfiguracionOperacion.idActual;			
			var FechaVencimiento=($scope.data.kngFechaVencimiento)?util.dateSPdate($scope.data.kngFechaVencimiento):undefined;
			var Plazo=$scope.data.kngPlazo;
			var Instrumento=undefined;

			if(IdOperacion=='INGRESO')Instrumento=($scope.data.instrumento)?$scope.data.instrumento:undefined;
			if(IdOperacion=='RETIRO')
				if($scope.data.checkPorCartera) Instrumento=($scope.instrumentoCustodia)?$scope.instrumentoCustodia:undefined;
				else Instrumento=($scope.data.instrumento)?$scope.data.instrumento:undefined;
			if(!Instrumento){
				console.log('[VALORIZACION RF]: No se ha seleccionado el instrumento. Saliendo del proceso');
				//bcsComunes.msg('error',$translate.instant("UI_ERROR"), '[VALORIZACION RF]: No se ha seleccionado el instrumento. Saliendo del proceso');
				valorizando(false);
				return;
			}
			if(!FechaVencimiento || FechaVencimiento==$scope.$root.SesionFechaSistema){
				console.log('[VALORIZACION RF]: No se cumple plazo. Saliendo del proceso');
				//bcsComunes.msg('error',$translate.instant("UI_ERROR"), '[VALORIZACION RF]: No se cumple plazo. Saliendo del proceso');
				valorizando(false);
				return;
			}			
			var IdClasificacionTipoInstrumento = Instrumento.IdClasificacionTipoInstrumento;

			var Cantidad 			= Cantidad;
			var Tasa 				= Tasa;
			var Precio	 			= Precio;
			var Monto 				= Monto;
			var PrecioReferencia 	= $scope.data.kngPrecioPorcentajeReferencia;
			var TasaReferencia 		= $scope.data.kngTasaReferencia;
					
			var condicion;
			if(Cantidad>0&&Precio>0&&Tasa===undefined&&(Monto===undefined||Monto===0)) condicion='A';
			else if(Cantidad>0&&Tasa>0&&Precio===undefined&&(Monto===undefined||Monto===0)) condicion='B';
			else if(Cantidad>0&&Monto>0&&Precio===undefined&&Tasa===undefined) condicion='C';
			else if(Cantidad>0&&Tasa>=0&&Precio>=0) condicion='TODOS';
			else{
				console.log('[VALORIZACION RF]: No cumple condiciones');
				//bcsComunes.msg('error',$translate.instant("UI_ERROR"), '[VALORIZACION RF]: No cumple condiciones');
				valorizando(false);
				return;
			}

			var valorizarMercado=false;
			if((!PrecioReferencia||PrecioReferencia==0)&&!TasaReferencia){
				console.log('[VALORIZACION RF]: No hay valores de mercado');
			} else{
				valorizarMercado=true;
				console.log('[VALORIZACION RF]: Se valorizará a mercado');
				//bcsComunes.msg('error',$translate.instant("UI_ERROR"), '[VALORIZACION RF]: Se valorizará a mercado');
			}
			
			var cval=generaCabeceraValorizar(IdClasificacionTipoInstrumento, ($scope.data.kngIdMonedaPagoInstrumento || $scope.instrumentoCustodia.NombreCompletoMonedaInstrumento));	
			var dvalCompra=generaEntradaInstrumento(1);
			dvalCompra.IdTipoInstrumento				=Instrumento.IdTipoInstrumento;
			dvalCompra.IdInstrumento					=Instrumento.IdInstrumento;
			dvalCompra.Nemotecnico						=Instrumento.Nemotecnico;
			dvalCompra.FechaEmision						=Instrumento.FechaEmision;
			dvalCompra.FechaVencimiento					=FechaVencimiento;
			dvalCompra.IdMonedaInstrumento				=$scope.data.kngIdMonedaPagoInstrumento || $scope.instrumentoCustodia.NombreCompletoMonedaInstrumento;
//			dvalCompra.IdMonedaEmisionInstrumento		=$scope.data.kngIdMonedaEmision || $scope.instrumentoCustodia.NombreCompletoMonedaEmision;
			dvalCompra.IdMonedaEmisionInstrumento		=$scope.data.kngIdTipoReajuste || Instrumento.IdTipoReajuste;
			dvalCompra.IdTipoReajusteInstrumento		=$scope.data.kngIdTipoReajuste || Instrumento.IdTipoReajuste;			
			dvalCompra.Cantidad							=Cantidad||null;
			dvalCompra.PrecioCompra						=Precio||null;
			dvalCompra.TasaCompra						=Tasa||null;
			dvalCompra.MontoCompra						=Monto||null,				
			dvalCompra.Emisor 							=Instrumento.IdEmisor;
			//dvalCompra.TipoValorizacion					=Instrumento.TipoValorizacion;
			dvalCompra.IdCondicionDeLiquidacion			=$scope.data.oConfiguracionCondicionLiquidacion.seleccionado.IdCondicionDeLiquidacion||null;
			dvalCompra.IdClasificacionTipoInstrumento	=Instrumento.IdClasificacionTipoInstrumento;
			cval.p_Instrumentos.push(dvalCompra);
			callValorizador(cval,IdClasificacionTipoInstrumento,valorizarMercado);
		};
		//##############################################
		//		Fn principal valorización IF
		//##############################################
		var fnvalorizarIF = function(ValorRescate,Precio,Tasa,Monto){
			console.log('[VALORIZACION IF]: Iniciando validaciones');
			var IdOperacion = $scope.data.oConfiguracionOperacion.idActual;
			var FechaVencimiento=($scope.data.kngFechaVencimiento)?util.dateSPdate($scope.data.kngFechaVencimiento):undefined;
			var Plazo=$scope.data.kngPlazo;
			var Instrumento=undefined;
			if(IdOperacion=='INGRESO') Instrumento=($scope.data.instrumento)?$scope.data.instrumento:undefined;
			if(IdOperacion=='RETIRO')
				if($scope.data.checkPorCartera) Instrumento=($scope.instrumentoCustodia)?$scope.instrumentoCustodia:undefined;
				else Instrumento=($scope.data.instrumento)?$scope.data.instrumento:undefined;
			if(!Instrumento){
				console.log('[VALORIZACION IF]: No se ha seleccionado el instrumento. Saliendo del proceso');
				//bcsComunes.msg('error',$translate.instant("UI_ERROR"), '[VALORIZACION IF]: No se ha seleccionado el instrumento. Saliendo del proceso');
				valorizando(false);
				return;
			}	
			if(!FechaVencimiento || FechaVencimiento==$scope.$root.SesionFechaSistema){
				console.log('[VALORIZACION IF]: No se cumple plazo. Saliendo del proceso');
				//bcsComunes.msg('error',$translate.instant("UI_ERROR"), '[VALORIZACION IF]: No se cumple plazo. Saliendo del proceso');
				valorizando(false);
				return;
			}
			
			var IdClasificacionTipoInstrumento 	= Instrumento.IdClasificacionTipoInstrumento;
			var ValorRescate 					= ValorRescate;
			var Tasa 							= Tasa;
			var FechaVencimiento 				= ($scope.data.kngFechaVencimiento)?util.dateSPdate($scope.data.kngFechaVencimiento):undefined;
			var IdTipoReajuste 					= $scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion === 'INGTIT' ? $scope.data.kngIdTipoReajuste : Instrumento.IdTipoReajuste;
			var Monto 							= Monto;
			var TasaReferencia	 				= $scope.data.kngTasaReferencia_;
			
			var condicion;
			if(ValorRescate > 0 && FechaVencimiento && IdTipoReajuste && Tasa >= 0 && (Monto === undefined || Monto === 0))
				condicion='A';
			else if(ValorRescate > 0 && FechaVencimiento && IdTipoReajuste && (Tasa === undefined || Tasa === 0) && (Monto >= 0))
				condicion='B';
			else if ((ValorRescate === 0 || ValorRescate === undefined) && FechaVencimiento && IdTipoReajuste && Tasa >= 0 && Monto >= 0)
				condicion='C';
			else if(Tasa >= 0 && FechaVencimiento && IdTipoReajuste && ValorRescate > 0 && Monto > 0) 
				condicion = 'TODOS';
			else{
				console.log('[VALORIZACION IF]: No cumple condiciones');
				//bcsComunes.msg('error',$translate.instant("UI_ERROR"), '[VALORIZACION IF]: No cumple condiciones');
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
			
			var cval=generaCabeceraValorizar($scope.data.oConfiguracionClasificacion.idActual, ( $scope.instrumentoCustodia.NombreCompletoMonedaInstrumento||$scope.data.kngIdMonedaPagoInstrumento ));	
			var dvalCompra=generaEntradaInstrumento(1);
			dvalCompra.IdTipoInstrumento				=Instrumento.IdTipoInstrumento;
			dvalCompra.IdInstrumento					=Instrumento.IdInstrumento;
			dvalCompra.Nemotecnico						=Instrumento.Nemotecnico;
			dvalCompra.FechaEmision						=Instrumento.FechaEmision;
			dvalCompra.FechaVencimiento					=FechaVencimiento;
			dvalCompra.IdMonedaInstrumento				=$scope.instrumentoCustodia.NombreCompletoMonedaInstrumento||$scope.data.kngIdMonedaPagoInstrumento ;
//			dvalCompra.IdMonedaEmisionInstrumento		=$scope.data.kngIdMonedaEmision || $scope.instrumentoCustodia.NombreCompletoMonedaEmision;
			dvalCompra.IdMonedaEmisionInstrumento		=$scope.data.kngIdTipoReajuste || Instrumento.IdTipoReajuste;
			dvalCompra.IdTipoReajusteInstrumento		=$scope.data.kngIdTipoReajuste || Instrumento.IdTipoReajuste;
			dvalCompra.ValorRescate						=ValorRescate||null;
			dvalCompra.TasaCompra						=Tasa||null;				
			dvalCompra.MontoCompra						=Monto||null;
			dvalCompra.Emisor 							=IdOperacion=='RETIRO' ? Instrumento.IdEmisor : $scope.data.oConfiguracionEmisor.idActual; //Instrumento.IdEmisor;
			dvalCompra.IdCondicionDeLiquidacion			=$scope.data.oConfiguracionCondicionLiquidacion.seleccionado.IdCondicionDeLiquidacion||null;
			dvalCompra.IdClasificacionTipoInstrumento	=Instrumento.IdClasificacionTipoInstrumento;
			cval.p_Instrumentos.push(dvalCompra);
			callValorizador(cval,IdClasificacionTipoInstrumento,valorizarMercado);
		};
		//##############################################
		// 		BOTÓN DE VALORIZACION
		//##############################################
		$scope.data.ObtenerValorizacion = function(cambio){
			if ($scope.data.oDesplegar.showPrecio){
				if (typeof $scope.data.kngPrecio !== 'undefined' && $scope.data.kngPrecio > 0  && typeof $scope.data.kngTasa !== 'undefined' && $scope.data.kngTasa > 0){
					bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_INDICAR_CANTIDAD_O_PRECIO"));
					return;
				};
				
				if (typeof $scope.data.kngPrecio !== 'undefined' && $scope.data.kngPrecio > 0)
					cambio = 'Precio';
				if (typeof $scope.data.kngTasa !== 'undefined' && $scope.data.kngTasa > 0)
					cambio = 'Tasa';
			};
			if ($scope.data.oDesplegar.showPrecioPorcentaje){
				if (typeof $scope.data.kngPrecioPorcentaje !== 'undefined' && $scope.data.kngPrecioPorcentaje > 0 && typeof $scope.data.kngTasa !== 'undefined' && $scope.data.kngTasa > 0){
					bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_INDICAR_CANTIDAD_O_PRECIO"));
					return;
				};
				
				if (typeof $scope.data.precioPorcentajeOptions !== 'undefined' && $scope.data.precioPorcentajeOptions > 0)
					cambio = 'Precio';				
				if (typeof $scope.data.tasaOptions_ !== 'undefined' && $scope.data.tasaOptions_ > 0)
					cambio = 'Tasa';
			};
			
			
			
			switch ($scope.data.oConfiguracionClasificacion.idActual)
			{		
				case "RV":
					Cantidad	= $scope.data.kngCantidad || undefined;
					Precio		= $scope.data.kngPrecio || undefined;
					Monto		= $scope.data.kngMontoValorizadoMonedaOrigen || undefined;  //$scope.data.kngMonto;

					fnvalorizarRV(Cantidad,Precio,Monto);
					break;
				case "RF":
					switch (cambio){
						case "Tasa"		: $scope.data.kngPrecioPorcentaje = undefined; break;
						case "Precio"	: $scope.data.kngTasa = undefined; break;
					}
					switch ($scope.SesionLocale){
						case "CL":	Cantidad = $scope.data.kngCantidad; break; 
						case "PE":	Cantidad = $scope.data.kngValorNominal; break;
					}
					
					Precio		= $scope.data.kngPrecioPorcentaje || undefined;
					Tasa 		= $scope.data.kngTasa || undefined;
					//Monto		= $scope.data.kngMontoValorizadoMonedaOrigen || undefined;  //$scope.data.kngMonto;
					Monto		= $scope.data.kngMonto || undefined;

					fnvalorizarRF(Cantidad,Precio,Tasa,Monto);
					break;
				case "IF":
					switch (cambio){
						case "Tasa": $scope.data.kngPrecioPorcentaje = undefined; break;
						case "Precio": $scope.data.kngTasa_ = undefined; break;
					}

					Cantidad	= $scope.data.kngValorRescate || undefined;
					Precio		= $scope.data.kngPrecioPorcentaje || undefined;
					Tasa 		= $scope.data.kngTasa_ || undefined;
					//Monto		= $scope.data.kngMontoValorizadoMonedaOrigen || undefined;  //$scope.data.kngMonto;
					Monto		= $scope.data.kngMonto || undefined;  //$scope.data.kngMonto;
										
					fnvalorizarIF(Cantidad,Precio,Tasa,Monto);
					break;
			};
			
		};
		
		//##############################################
		// 		WATCH VARIABLES DE VALORIZACION
		//##############################################
		$scope.$watch('data.kngCantidad',function(newValue,oldValue){
			if(typeof $scope.data.oConfiguracionClasificacion.idActual !== 'undefined' && $scope.data.oConfiguracionClasificacion.idActual ==='RV'){
				//Validacion para cantidades mayores al seleccionar por cartera
				if($scope.data.kngCantidad > $scope.data.kngCantidadMax){
					bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_ERROR_EXCEDE_CANTIDAD"));
					$scope.data.kngCantidad = $scope.data.kngCantidadMax;
					return;
				}
				$scope.data.ObtenerValorizacion('');
			}	
		},true);
		$scope.$watch('data.kngPrecio',function(newValue,oldValue){ 
			if(typeof $scope.data.oConfiguracionClasificacion.idActual !== 'undefined' && $scope.data.oConfiguracionClasificacion.idActual==='RV') 
				$scope.data.ObtenerValorizacion('');
		},true);
		
/****************************************************
 * FIN NUEVA VALORIZACION - 17/03/2017
 ****************************************************/	
		
		var fnLimpiarBloqueValorizacion = function(){
			//Campos de Valorización Compra
			$scope.data.kngCantidad= undefined;
			$scope.data.kngMonto= undefined;
			$scope.data.kngMontoValorizado = undefined;
			$scope.data.kngMontoValorizadoMonedaOrigen = undefined;
			$scope.data.kngMontoValorizadoMonedaAdicional = undefined;
			$scope.data.kngTasa = undefined;
			$scope.data.kngTasa_ = undefined;
			$scope.data.kngPrecio = undefined;
			$scope.data.kngPrecioPorcentaje = undefined;
			$scope.data.kngValorRescate= undefined;
			$scope.data.kngValorNominal= undefined;
			$scope.data.kngInteresesCorridos = undefined;
			$scope.data.kngReajusteVacCorrido = undefined;
			//Campos de Valorización Mercado
			$scope.data.kngMontoReferencia= undefined;
			$scope.data.kngMontoValorizadoReferencia = undefined;
			$scope.data.kngMontoValorizadoMonedaOrigenReferencia =undefined;
			$scope.data.kngTasaReferencia = undefined;
			$scope.data.kngTasaReferencia_ = undefined;
			$scope.data.kngPrecioReferencia = undefined;
			$scope.data.kngPrecioPorcentajeReferencia = undefined;
			$scope.data.kngValorRescateReferencia= undefined;
			$scope.data.kngReajusteVacCorridoAMercado = undefined;
			$scope.data.kngInteresesCorridosAMercado = undefined;
		}

		var fnCambioInstrumento = function(newValue, oldValue) {
			if (typeof $scope.data.instrumento !== 'undefined' && $scope.data.instrumento !== false && $scope.data.instrumento.IdInstrumento !== undefined){
				fnLimpiarBloqueValorizacion();	
				$scope.win.blurPrecioTasa();
				

				if ($scope.data.instrumento.IdMoneda !== ''){
					$scope.data.oDecimales.cantidad =                  (util.bcs_formato(0,'GEN','Cantidad',$scope.data.instrumento.IdMoneda,$scope.data.instrumento.IdTipoInstrumento,'')).cantidadDecimales;
					$scope.data.precioPorcentajeOptions = {format: 'n'+(util.bcs_formato(0,'GEN','Precio'  ,$scope.data.instrumento.IdMoneda,$scope.data.instrumento.IdTipoInstrumento,'')).cantidadDecimales,min:0,spinners: false};
					$scope.data.precioOptions =           {format: 'n'+(util.bcs_formato(0,'GEN','Precio'  ,$scope.data.instrumento.IdMoneda,$scope.data.instrumento.IdTipoInstrumento,'')).cantidadDecimales,min:0,spinners: false};
				}else{
					$scope.data.oDecimales.cantidad =                  (util.bcs_formato(0,'GEN','Cantidad',$scope.data.instrumento.IdTipoReajuste,$scope.data.instrumento.IdTipoInstrumento,'')).cantidadDecimales;
					$scope.data.precioPorcentajeOptions = {format: 'n'+(util.bcs_formato(0,'GEN','Precio'  ,$scope.data.instrumento.IdTipoReajuste,$scope.data.instrumento.IdTipoInstrumento,'')).cantidadDecimales,min:0,spinners: false};
					$scope.data.precioOptions =           {format: 'n'+(util.bcs_formato(0,'GEN','Precio'  ,$scope.data.instrumento.IdTipoReajuste,$scope.data.instrumento.IdTipoInstrumento,'')).cantidadDecimales,min:0,spinners: false};
				}
				

				if ($scope.data.instrumento.IdClasificacionTipoInstrumento === 'RF' && 	$scope.data.instrumento.IndicadorBolson === 'S'){

					if ($scope.data.instrumento.FechaVencimiento < $scope.data.kngFechaOperacion){
						bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_INSTRUMENTO_VENCIDO"));
						return;
					}

					$scope.data.oDesplegar.showFechaVencimiento = true;
					$scope.data.oDesplegar.enabledFechaVencimiento = false;
					$scope.data.fechaVencimientoOptions = {min:util.dateInicioDia($scope.data.instrumento.FechaVencimiento), max:util.dateInicioDia($scope.data.instrumento.FechaVencimiento)};	
					$scope.data.kngFechaVencimiento = util.dateInicioDia($scope.data.instrumento.FechaVencimiento);



					$scope.data.oDesplegar.showFechaEmision = true;
					$scope.data.kngFechaEmision = util.dateInicioDia($scope.data.instrumento.FechaEmision);
					$scope.data.enabledFechaEmision = false; 



					$scope.data.enabledEmisor = false; 
					$scope.data.oConfiguracionEmisor.filtros.IdEmisor.operator =  'eq';
					$scope.data.oConfiguracionEmisor.filtros.IdEmisor.value =  $scope.data.instrumento.IdEmisor;
					$scope.data.oConfiguracionEmisor.idActual = $scope.data.instrumento.IdEmisor;
					$scope.data.oConfiguracionEmisor.idPostFiltro = $scope.data.instrumento.IdEmisor;


					if ($scope.data.instrumento.Reajustable === 'S' || $scope.data.instrumento.IdTipoReajuste != $scope.SesionIdMonedaSistema){
						$scope.data.oDesplegar.showTipoReajuste = true;
					}else{
						$scope.data.oDesplegar.showTipoReajuste = false;
					}
					$scope.data.kngIdTipoReajuste = $scope.data.instrumento.IdTipoReajuste;

					
				}
				
				if($scope.data.instrumento.IdClasificacionTipoInstrumento === 'IF' && $scope.data.instrumento.IdEmisor === '')
				{
					$scope.data.enabledEmisor = true; 
				}
				if($scope.data.instrumento.IdClasificacionTipoInstrumento === 'IF' && $scope.data.instrumento.IdEmisor !== '')
				{
					$scope.data.enabledEmisor = false; 
				}
				
				if ($scope.data.oDesplegar.showFechaVencimiento === true) {
					$scope.data.kngFechaVencimiento = util.dateInicioDia($scope.SesionFechaSistema);
				}
				else {
					$scope.data.kngFechaVencimiento = util.dateInicioDia('2099-12-31');
				}		
				if($scope.data.oDesplegar.showFechaVencimiento === true && $scope.data.instrumento.FechaVencimiento !== undefined && !util.dateisdefault($scope.data.instrumento.FechaVencimiento)){
					$scope.data.kngFechaVencimiento = util.dateInicioDia($scope.data.instrumento.FechaVencimiento);		
				}
				$scope.data.kngCorteMinimo = ($scope.data.instrumento.CorteMinimo >0 ? $scope.data.instrumento.CorteMinimo : 1) || 1;

				if(Number($scope.data.kngCorteMinimo)===1) {
					$scope.data.enabledValorRescate = true;
				}
				else {
					$scope.data.enabledValorRescate = false;
				}
				if($scope.data.kngCantidad && $scope.data.kngCorteMinimo){
					$scope.data.kngValorRescate = $scope.data.kngCantidad * Number($scope.data.kngCorteMinimo); 
				}

				if ($scope.data.oDesplegar.obtienePrecio === true) {
					if($scope.data.instrumento.IdClasificacionTipoInstrumento == 'RV') {
						if($scope.data.instrumento.PrecioMercado>0){
							$scope.data.kngPrecio = $scope.data.instrumento.ValorizaPor === 'P' ? $scope.data.instrumento.PrecioMercado : undefined;
							$scope.data.kngPrecioReferencia = $scope.data.instrumento.PrecioMercado;
							$scope.data.kngPrecio = $scope.data.instrumento.PrecioMercado;
						}
					}else {
						if($scope.data.instrumento.PrecioMercado>0){
							$scope.data.kngPrecioPorcentaje = $scope.data.instrumento.ValorizaPor === 'P' ? $scope.data.instrumento.PrecioMercado : undefined;
							$scope.data.kngPrecioPorcentajeReferencia = $scope.data.instrumento.PrecioMercado;
						}

						if($scope.data.instrumento.TasaMercado>0){
							$scope.data.kngTasaReferencia = $scope.data.instrumento.ValorizaPor === 'T' ? $scope.data.instrumento.TasaMercado : undefined;
							$scope.data.kngTasaReferencia_ = $scope.data.instrumento.TasaMercado;
						}
					}
				}

				if ($scope.data.instrumento.IdEmisor !== undefined && $scope.data.oDesplegar.showEmisor === true) {
					if ($scope.data.instrumento.IdEmisor !== ''){
						$scope.data.oConfiguracionEmisor.filtros.IdEmisor.operator =  'eq';
						$scope.data.oConfiguracionEmisor.filtros.IdEmisor.value =  $scope.data.instrumento.IdEmisor;
						$scope.data.oConfiguracionEmisor.idActual = $scope.data.instrumento.IdEmisor;
						$scope.data.oConfiguracionEmisor.idPostFiltro = $scope.data.instrumento.IdEmisor;
					}
					else {
						$scope.data.oConfiguracionEmisor.filtros.IdEmisor.operator =  'neq';
						$scope.data.oConfiguracionEmisor.filtros.IdEmisor.value =  '';
					}
				}
				if ($scope.data.instrumento.Reajustable !== undefined && $scope.data.oDesplegar.showTipoReajuste === true) {
					$scope.data.oConfiguracionTipoReajuste.filtros.IdInstrumento.value = Number($scope.data.instrumento.IdInstrumento) || 0;
					$scope.data.oConfiguracionTipoReajuste.idPostFiltro = $scope.data.instrumento.IdTipoReajuste;
					$scope.data.kngIdTipoReajuste = $scope.data.instrumento.IdTipoReajuste;
				}

				if ($scope.data.checkFungible) {
					$scope.data.kngCantidad = 1;
				}
				
				$scope.data.oConfiguracionMonedaEmision.filtros.IdInstrumento.value = Number($scope.data.instrumento.IdInstrumento) || 0;
				$scope.data.oConfiguracionMonedaEmision.filtros.read = true;
//				$scope.data.oConfiguracionMonedaPagoInstrumento.filtros.IdInstrumento.value = Number($scope.data.instrumento.IdInstrumento) || 0;
				$scope.data.oConfiguracionMonedaPagoInstrumento.filtros.IdTipoInstrumento.value = $scope.data.instrumento.IdTipoInstrumento || '';
				$scope.data.oConfiguracionMonedaPagoInstrumento.filtros.IdMoneda.value = $scope.data.instrumento.IdMoneda || '';
				$scope.data.oConfiguracionMonedaPagoInstrumento.filtros.read = true;
				
				fnSeleccionaMonedas();
			}
		};
		$scope.$watch('data.instrumento', fnCambioInstrumento,true);
		
		var fnSeleccionaMonedas = function(newValue, oldValue) {
			var IdMonedaPago 				= '';
			var IdMonedaPagoOrigen			= '';
			var IdMonedaPagoValorRescate 	= '';
			var IdTipoInstrumento			= '';
			
			if (typeof $scope.data.oConfiguracionMonedaPagoInstrumento !== 'undefined' && 
			   	       $scope.data.oConfiguracionMonedaPagoInstrumento.seleccionado  !== false &&
			   	       $scope.data.oConfiguracionMonedaPagoInstrumento.seleccionado.IdMoneda  !== '' &&
			   	       typeof $scope.data.instrumento !== 'undefined' &&
			   	       $scope.data.instrumento.IdClasificacionTipoInstrumento === 'IF'){
			
				IdMonedaPago 				= $scope.data.oConfiguracionMonedaPagoInstrumento.seleccionado.IdMonedaLiquidacion;
				IdMonedaPagoValorRescate	= $scope.data.oConfiguracionMonedaPagoInstrumento.seleccionado.IdMoneda;
			} else if ($scope.instrumentoCustodia.IdTipoReajuste){
				IdMonedaPagoValorRescate	= $scope.instrumentoCustodia.IdTipoReajuste;
				IdMonedaPago 				= $scope.data.oConfiguracionMonedaPagoInstrumento.seleccionado.IdMonedaLiquidacion;
			}else if (typeof $scope.data.instrumento !== 'undefined' && typeof $scope.data.instrumento.IdMoneda !== 'undefined'){
				//IdMonedaPago = $scope.data.instrumento.IdMoneda;				
				IdMonedaPago 		= $scope.data.oConfiguracionMonedaPagoInstrumento.seleccionado.IdMonedaLiquidacion;
				IdMonedaPagoOrigen	= $scope.data.oConfiguracionMonedaPagoInstrumento.seleccionado.IdMoneda;
			} else if ($scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion !== 'INGTIT' && $scope.data.checkPorCartera){
				IdMonedaPago = $scope.instrumentoCustodia.IdMoneda;
			};
			
			if (typeof $scope.data.instrumento !== 'undefined'){
				IdTipoInstrumento = $scope.data.instrumento.IdTipoInstrumento;
			}else if($scope.instrumentoCustodia.IdTipoInstrumento !== ''){
				IdTipoInstrumento = $scope.instrumentoCustodia.IdTipoInstrumento;
			}
			

			$scope.data.monedaValorRescate 					= IdMonedaPagoValorRescate; // IdMonedaPago;
			if (typeof $scope.data.instrumento !== 'undefined' && $scope.data.instrumento.IdClasificacionTipoInstrumento === 'RV'){
				$scope.data.monedaMontoValorizadoMonedaOrigen 	= IdMonedaPagoOrigen;		
				$scope.data.monedaInstrumento 					= IdMonedaPagoOrigen;
			}else{
				$scope.data.monedaMontoValorizadoMonedaOrigen 	= IdMonedaPago;		
				$scope.data.monedaInstrumento 					= IdMonedaPago;
			};
								
			$scope.data.oDecimales.tasa.decimales 			= (util.bcs_formato(0,'GEN','Tasa',$scope.SesionIdMonedaSistema,IdTipoInstrumento,'')).cantidadDecimales;
			$scope.data.oDecimales.tasa.decimales_ 			= (util.bcs_formato(0,'GEN','Tasa',$scope.SesionIdMonedaSistema,IdTipoInstrumento,'')).cantidadDecimales;
			$scope.data.options.tasa 						= {min:-999999999999999999999};			
			$scope.data.options.tasa_ 						= {min:-999999999999999999999};
			
			$scope.data.oDecimales.precio.decimales 			= IdMonedaPago && IdTipoInstrumento ? (util.bcs_formato(0,'GEN','Precio',IdMonedaPago,IdTipoInstrumento,'')).cantidadDecimales : (util.bcs_formato(0,'GEN','Precio',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales;
			$scope.data.oDecimales.precioPorcentaje.decimales	= (util.bcs_formato(0,'GEN','Precio',IdMonedaPago,IdTipoInstrumento,'')).cantidadDecimales;
			$scope.data.precioOptions 						= {format: 'c'+(util.bcs_formato(0,'GEN','Precio',IdMonedaPago,IdTipoInstrumento,'')).cantidadDecimales,min:0,spinners: false};
			$scope.data.precioPorcentajeOptions 			= {format: 'n'+(util.bcs_formato(0,'GEN','Precio',IdMonedaPago,IdTipoInstrumento,'')).cantidadDecimales,min:0,spinners: false};
			
			$scope.data.oDecimales.monto.decimales 			= (util.bcs_formato(0,'GEN','Monto',$scope.data.monedaMonto,'','')).cantidadDecimales;
			$scope.data.oDecimales.montoValorizado.decimales =(util.bcs_formato(0,'GEN','Monto',$scope.data.monedaMontoValorizado,'','')).cantidadDecimales;
			$scope.data.oDecimales.montoValorizadoMonedaOrigen.decimales =(util.bcs_formato(0,'GEN','Monto',$scope.data.monedaMontoValorizadoMonedaOrigen,'','')).cantidadDecimales;
			
//			$scope.data.kngCorteMinimo 						= typeof $scope.data.instrumento != 'undefined' && typeof $scope.data.instrumento.CorteMinimo != 'undefined' && $scope.data.instrumento.CorteMinimo >0 ? $scope.data.instrumento.CorteMinimo : 1;
//			$scope.data.kngCorteMinimo 						= typeof $scope.data.instrumento != 'undefined' && typeof $scope.data.instrumento.CorteMinimo != 'undefined' ? $scope.data.oConfiguracionTipoReajuste.seleccionado.CorteMinimo : 1;
			$scope.data.oDecimales.corteMinimo.decimales 	= 4;


			if ($scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion === 'INGTIT'){ 
				switch ($scope.data.oConfiguracionClasificacion.idActual){		
					case "RV":
						$scope.data.kngCorteMinimo 		= 1;
						break;
					case "RF":
						if ($scope.data.kngIdTipoReajuste != 'CLP'){
							$scope.data.kngCorteMinimo 			= typeof $scope.data.instrumento != 'undefined' && typeof $scope.data.instrumento.CorteMinimo != 'undefined' && $scope.data.instrumento.CorteMinimo >0 ? $scope.data.instrumento.CorteMinimo : 1;
							$scope.data.oDecimales.cantidad 	= $scope.data.kngIdTipoReajuste ? (util.bcs_formato(0,'GEN','Cantidad',$scope.data.kngIdTipoReajuste, '','')).cantidadDecimales :(util.bcs_formato(0,'GEN','Cantidad',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales;
						}else{
							$scope.data.kngCorteMinimo 			= typeof $scope.data.instrumento != 'undefined' && typeof $scope.data.instrumento.CorteMinimo != 'undefined' && $scope.data.instrumento.CorteMinimo >0 ? $scope.data.instrumento.CorteMinimo : 1;
							$scope.data.oDecimales.cantidad 	= IdMonedaPago ? (util.bcs_formato(0,'GEN','Cantidad',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales : (util.bcs_formato(0,'GEN','Cantidad',IdMonedaPago, '','')).cantidadDecimales;
						}
						
						break;
					case "IF":
						$scope.data.kngCorteMinimo 						= typeof $scope.data.instrumento != 'undefined' && typeof $scope.data.instrumento.CorteMinimo != 'undefined' ? $scope.data.oConfiguracionTipoReajuste.seleccionado.CorteMinimo : 1;						
						$scope.data.oDecimales.valorRescate.decimales 	= (util.bcs_formato(0,'GEN','Monto',$scope.data.monedaValorRescate,'','')).cantidadDecimales;						
						break;
				};				
			};

			
		};
		$scope.$watchGroup(['data.oConfiguracionMonedaEmision.seleccionado'], fnSeleccionaMonedas );
		$scope.$watchGroup(['data.oConfiguracionMonedaPagoInstrumento.seleccionado'], fnSeleccionaMonedas );
		
		var fnRevisaEstadoCliente = function(newValue, oldValue) {
			if (typeof $scope.oSocioNegocio.seleccionado !== 'undefined' && typeof $scope.oSocioNegocio.seleccionado.IdCliente !== 'undefined'){
																																										   
				if ($scope.oSocioNegocio.seleccionado.BloqueadoPorCumplimiento === 'S' && $scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion === 'INGTIT')
				{ 
					bcsComunes.ventanaMsg({mensaje: $translate.instant("UI_CLIENTE_BLOQUEADO_POR_CUMPLIMIENTO")},$scope.winID);	
				}
			}
		};
		$scope.$watch('oSocioNegocio.seleccionado', fnRevisaEstadoCliente );

		
		var fnCambioCartera = function(newValue, oldValue) {
			if (typeof $scope.instrumentoCustodia !== 'undefined' && $scope.data.kngFolioCartera > 0 && 
				typeof $scope.data.oConfiguracionOperacion.seleccionado !== 'undefined' && $scope.data.oConfiguracionOperacion.seleccionado !== false &&
				typeof $scope.data.oConfiguracionClasificacion.seleccionado !== 'undefined' && $scope.data.oConfiguracionClasificacion.seleccionado !== false){
				if ($scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion === 'INGTIT'){
					return;
				}
				if ($scope.data.oDesplegar.showFechaVencimiento === true) {
					$scope.data.kngFechaVencimiento = util.dateInicioDia($scope.SesionFechaSistema);
				}else {
					$scope.data.kngFechaVencimiento = util.dateInicioDia('2099-12-31');
				}
				if($scope.data.oDesplegar.showFechaVencimiento === true && $scope.instrumentoCustodia.FechaVencimiento !== undefined && !util.dateisdefault($scope.instrumentoCustodia.FechaVencimiento)){
					$scope.data.kngFechaVencimiento = util.dateInicioDia($scope.instrumentoCustodia.FechaVencimiento);
				}
				//$scope.data.kngFechaEmision = util.dateInicioDia($scope.SesionFechaSistema);
				if($scope.data.oDesplegar.showFechaEmision === true && $scope.instrumentoCustodia.FechaEmision !== undefined && !util.dateisdefault($scope.instrumentoCustodia.FechaEmision)){
					$scope.data.kngFechaEmision = util.dateInicioDia($scope.instrumentoCustodia.FechaEmision);
				}
				
				$scope.data.kngPrecioPorcentaje = Number($scope.instrumentoCustodia.Precio);
	        	$scope.data.kngTasa_ = Number($scope.instrumentoCustodia.Tasa);
	        	$scope.data.kngValorRescate = 0;
	        	$scope.data.kngValorRescate = $scope.instrumentoCustodia.ValorRescate;
//	        	$scope.data.kngMonto = $scope.instrumentoCustodia.Monto;
				$scope.instrumentoCustodia.NombreCompletoTipoReajuste = $scope.instrumentoCustodia.IdTipoReajuste.concat(' - ',$scope.instrumentoCustodia.NombreTipoReajuste); 
				$scope.instrumentoCustodia.NombreCompletoMonedaEmision = $scope.instrumentoCustodia.IdMonedaEmision;
				$scope.instrumentoCustodia.NombreCompletoMonedaInstrumento = $scope.instrumentoCustodia.IdMoneda;
				
				
				if ($scope.data.oDesplegar.showFechaVencimiento === true){
					$scope.data.kngPlazo = Math.round(util.datediff(util.dateInicioDia($scope.instrumentoCustodia.FechaVencimiento),util.dateInicioDia($scope.data.kngFechaCondicionLiquidacion)).asDays());
						//Math.round(util.datediff(util.dateInicioDia($scope.instrumentoCustodia.FechaVencimiento),util.dateInicioDia($scope.instrumentoCustodia.INGFechaOperacion)).asDays());
				}
				
				if ($scope.instrumentoCustodia.IdClasificacionTipoInstrumento === 'RF' && $scope.instrumentoCustodia.IndicadorBolson === 'S'){

					$scope.data.oDesplegar.showFechaVencimiento = true;
					$scope.data.oDesplegar.enabledFechaVencimiento = false;
					$scope.data.fechaVencimientoOptions = {min:util.dateInicioDia($scope.instrumentoCustodia.FechaVencimiento), max:util.dateInicioDia($scope.instrumentoCustodia.FechaVencimiento)};	
					$scope.data.kngFechaVencimiento = util.dateInicioDia($scope.instrumentoCustodia.FechaVencimiento);

					$scope.data.oDesplegar.showFechaEmision = true;
					$scope.data.kngFechaEmision = util.dateInicioDia($scope.instrumentoCustodia.FechaEmision);
					$scope.data.enabledFechaEmision = false; 

					$scope.data.enabledEmisor = false; 
					$scope.data.oConfiguracionEmisor.filtros.IdEmisor.operator =  'eq';
					$scope.data.oConfiguracionEmisor.filtros.IdEmisor.value =  $scope.instrumentoCustodia.IdEmisor;
					$scope.data.oConfiguracionEmisor.idActual = $scope.instrumentoCustodia.IdEmisor;
					$scope.data.oConfiguracionEmisor.idPostFiltro = $scope.instrumentoCustodia.IdEmisor;


					if ($scope.instrumentoCustodia.Reajustable === 'S' || $scope.instrumentoCustodia.IdTipoReajuste != $scope.SesionIdMonedaSistema){
						$scope.data.oDesplegar.showTipoReajuste = true;
					}else{
						$scope.data.oDesplegar.showTipoReajuste = false;
					}
					$scope.data.kngIdTipoReajuste = $scope.instrumentoCustodia.IdTipoReajuste;
				}


				if ($scope.data.oDesplegar.obtienePrecio === true) {
					ObtenerPrecio();
				}
				
				fnSeleccionaMonedas();
			}
		};
		$scope.$watch('data.kngFolioCartera', fnCambioCartera,true);
			
		var fnSeleccionaClasificacion = function(newValue, oldValue) {
			if (typeof $scope.data.oConfiguracionClasificacion.seleccionado !== 'undefined' && 
			   	       $scope.data.oConfiguracionClasificacion.seleccionado  !== false){
				$scope.data.btnLimpiar();
				if ($scope.data.oConfiguracionClasificacion.seleccionado.IdClasificacionTipoInstrumento==='RV'){
					$scope.data.oDesplegar.showEmisor = false;
					$scope.data.oDesplegar.showTipoReajuste = false;
					$scope.data.oDesplegar.showFungible = false;
					$scope.data.oDesplegar.showValorRescate = false;
					$scope.data.oDesplegar.showValorNominal = false;
					$scope.data.oDesplegar.showTasa = false;
					$scope.data.oDesplegar.showFechaVencimiento = false;
					$scope.data.oDesplegar.enabledFechaVencimiento = false;
					$scope.data.oDesplegar.enabledPlazo = false;
					$scope.data.oDesplegar.showFechaEmision = false;
					$scope.data.oDesplegar.showPrecio = true;
					$scope.data.oDesplegar.showPrecioPorcentaje = false;
					$scope.data.oDesplegar.obtienePrecio = true;
					$scope.data.oDesplegar.showBusquedaInstrumentoEgreso = true;
					$scope.data.oDesplegar.showMontoMonedaOrigen = true;
					$scope.labelMontoReferencia = 'UI_MONTO';
					$scope.data.oDesplegar.enabledMonto = false;
					$scope.data.kngValorRescate = undefined;
					$scope.data.kngValorNominal = undefined;
					$scope.data.oConfiguracionEmisor.filtros.IdEmiteIRF.value = 'Z';
					$scope.data.oConfiguracionEmisor.filtros.IdEmiteIRF.operator = 'neq';
					$scope.data.oConfiguracionEmisor.filtros.IdEmiteIIF.value = 'Z';
					$scope.data.oConfiguracionEmisor.filtros.IdEmiteIIF.operator = 'neq';
				}
				if ($scope.data.oConfiguracionClasificacion.seleccionado.IdClasificacionTipoInstrumento==='RF'){
					$scope.data.oDesplegar.showEmisor = true;
					$scope.data.oDesplegar.showTipoReajuste = true;
					$scope.data.oDesplegar.showFungible = false;
					$scope.data.oDesplegar.showValorRescate = false;
					$scope.data.oDesplegar.showValorNominal = $scope.SesionLocale==='PE'?true:false;
					$scope.data.oDesplegar.showTasa = true;
					$scope.data.oDesplegar.showFechaVencimiento = true;
					$scope.data.oDesplegar.enabledFechaVencimiento = false;
					$scope.data.oDesplegar.enabledPlazo = false;
					$scope.data.oDesplegar.showFechaEmision = false;
					$scope.data.oDesplegar.showPrecio = false;
					$scope.data.oDesplegar.showPrecioPorcentaje = true;
					$scope.data.oDesplegar.obtienePrecio = true;
					$scope.data.oDesplegar.showBusquedaInstrumentoEgreso = false;					
					$scope.data.enabledEmisor = false; 
					$scope.data.oDesplegar.showMontoMonedaOrigen = true;
					$scope.data.oDesplegar.enabledMonto = true;
					$scope.labelMontoReferencia = 'UI_VALOR_CAPTACION';
					$scope.data.oConfiguracionEmisor.filtros.IdEmiteIRF.value = 'S';
					$scope.data.oConfiguracionEmisor.filtros.IdEmiteIRF.operator = 'eq';
					$scope.data.oConfiguracionEmisor.filtros.IdEmiteIIF.value = 'Z';
					$scope.data.oConfiguracionEmisor.filtros.IdEmiteIIF.operator = 'neq';
				}
				if ($scope.data.oConfiguracionClasificacion.seleccionado.IdClasificacionTipoInstrumento==='IF'){
					$scope.data.oDesplegar.showEmisor = true;
					$scope.data.oDesplegar.showTipoReajuste = true;
					$scope.data.oDesplegar.showFungible = true;
					$scope.data.oDesplegar.showValorRescate = true;
					$scope.data.oDesplegar.showValorNominal = false;
					$scope.data.oDesplegar.showTasa = true;
					$scope.data.oDesplegar.showFechaVencimiento = true;
					$scope.data.oDesplegar.enabledFechaVencimiento = true;
					$scope.data.oDesplegar.enabledPlazo = false;
					$scope.data.oDesplegar.showFechaEmision = true;
					$scope.data.oDesplegar.showPrecio = false;
					$scope.data.oDesplegar.showPrecioPorcentaje = false;
					$scope.data.oDesplegar.obtienePrecio = false;
					$scope.data.oDesplegar.showBusquedaInstrumentoEgreso = false;
					$scope.data.enabledEmisor = false; 
					$scope.data.oDesplegar.showMontoMonedaOrigen = true; 
					$scope.data.oDesplegar.enabledMonto = true;
					$scope.labelMontoReferencia = 'UI_VALOR_CAPTACION';
					$scope.data.oConfiguracionEmisor.filtros.IdEmiteIRF.value = 'Z';
					$scope.data.oConfiguracionEmisor.filtros.IdEmiteIRF.operator = 'neq';
					$scope.data.oConfiguracionEmisor.filtros.IdEmiteIIF.value = 'S';
					$scope.data.oConfiguracionEmisor.filtros.IdEmiteIIF.operator = 'eq';
				}
			}
		};
		$scope.$watch('data.oConfiguracionClasificacion.seleccionado', fnSeleccionaClasificacion);
		
		var fnSeleccionaTipoInstrumento = function(newValue, oldValue) {
			if (typeof $scope.data.oConfiguracionTipoInstrumento.seleccionado !== 'undefined' && 
			   	       $scope.data.oConfiguracionTipoInstrumento.seleccionado  !== false){
				$scope.data.btnLimpiar();
			}
		};
		$scope.$watch('data.oConfiguracionTipoInstrumento.seleccionado', fnSeleccionaTipoInstrumento );
		
		var fnSeleccionaEmisor = function(newValue, oldValue) {
			$scope.data.checkFungible 			= false;
			$scope.data.kngDisabledCorteMinimo	= true;
			if (typeof $scope.data.oConfiguracionEmisor.seleccionado !== 'undefined' && 
			   	       $scope.data.oConfiguracionEmisor.seleccionado  !== false){

			   	if ($scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion !== 'INGTIT' && $scope.data.checkPorCartera){
					if($scope.instrumentoCustodia.IdClasificacionTipoInstrumento === 'IF'){
						$scope.data.checkFungible =($scope.data.oConfiguracionEmisor.seleccionado.EmiteFungible ==='S')? true : false;
					}
			   	} else {
					if(typeof $scope.data.instrumento !== 'undefined' && $scope.data.instrumento.IdClasificacionTipoInstrumento === 'IF'){
						$scope.data.checkFungible =($scope.data.oConfiguracionEmisor.seleccionado.EmiteFungible ==='S')? true : false;
					}			   		
			   	}
			}
			valorizadorEnEjecucion = false;
		};
		$scope.$watch('data.oConfiguracionEmisor.seleccionado', fnSeleccionaEmisor );
		
		var fnFungibilidad = function(newValue, oldValue){
			if (!$scope.data.checkFungible){
				$scope.data.kngDisabledCorteMinimo = true;		
			} else {
				$scope.data.kngDisabledCorteMinimo = true;
			}
			if ($scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion === 'INGTIT' && typeof $scope.data.instrumento != 'undefined'){
				//$scope.data.kngCorteMinimo = $scope.data.instrumento.CorteMinimo >0 ? $scope.data.instrumento.CorteMinimo : 1;
				$scope.data.kngCorteMinimo = $scope.data.instrumento.CorteMinimo >0 ? $scope.data.oConfiguracionTipoReajuste.seleccionado.CorteMinimo : 1;

			}
			fnSeleccionaMonedas();
		};
		$scope.$watch('data.checkFungible', fnFungibilidad );
				
		
		var fnSeleccionaFungible = function(newValue, oldValue) {
			if (newValue[0]===true)
				$scope.data.kngCantidad = 1;
			if (newValue[0]===false)
				$scope.data.kngCantidad = 0;
			
			if (typeof $scope.data.oDesplegar.showFungible != 'undefined' && $scope.data.oDesplegar.showFungible && $scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion != 'RETTIT')
				$scope.data.kngValorRescate = undefined;
		};
		$scope.$watchGroup(['data.checkFungible'], fnSeleccionaFungible );
		
		var fnCambioDefinicion = function(newValue, oldValue) {
			if (flimpiarEnEjecucion === false){
				flimpiarEnEjecucion = true;
				f_limpiar();
				$timeout(function(){
					flimpiarEnEjecucion = false;
				},1000);			
			}
			$scope.data.oConfiguracionConcepto.fnSeleccionado();
			if ($scope.oSocioNegocio.seleccionado){
				$scope.data.oDesplegar.esCtaPropia = $scope.oSocioNegocio.seleccionado.EsCuentaPropia === "UI_SI";	
			}else{
				$scope.data.oDesplegar.esCtaPropia = false;
			}
			
			bcsComunes.ventanaTamanoAutomatico($scope.ventanaId);
		};

		$scope.$watchGroup(['data.oConfiguracionClasificacion.seleccionado',
							'data.oConfiguracionOperacion.seleccionado',
							'oSocioNegocio.seleccionado',
							'oPortafolio.seleccionado',
							'data.oConfiguracionTipoInstrumento.seleccionado'], fnCambioDefinicion );
		
		var successIngresarTitulo = function(respuesta) {
	   		bcsComunes.ventanaOverlay($scope.ventanaId,false);
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			if (respuesta.P_OUT_ERRORESCONTADOR === 0) 
			{
				bcsComunes.msg('info',$translate.instant("UI_INFORMACION"), $translate.instant("UI_OPERACION_INGRESO_EXITO"));
				$scope.data.btnLimpiar();
			}
			else
		    {
				bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_OPERACION_INGRESO_ERROR"));
				$scope.data.accionDisabled = true;
		    }
		};
		
		var errorIngresarTitulo = function(respuesta)
		{
			bcsComunes.ventanaOverlay($scope.ventanaId,false);
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_OPERACION_INGRESO_ERROR"));
			$scope.data.accionDisabled = true;
		};
		
		$scope.$watch('data.kngValorRescate', function(newval,oldval ){
			if ($scope.data.kngValorRescate && Number($scope.data.kngValorRescate) > 0 && newval !== oldval &&
				$scope.data.oConfiguracionClasificacion.idActual === "IF" && $scope.data.checkFungible === false ){
//				&& $scope.data.instrumento){

				//if($scope.data.oConfiguracionEmisor.idActual !== "CENTRAL" && $scope.data.oConfiguracionEmisor.idActual !== "TESORERIA") {
				if($scope.data.oConfiguracionEmisor.idActual !== "CENTRAL") {
					// IF No fungible => Corte Minimo = V.Rescate / Cantidad
					$scope.data.kngCorteMinimo = Math.trunc(Number($scope.data.kngValorRescate) / Number($scope.data.kngCantidad));
				} else {
					// PDBC ó PRBC => Cantidad = V.Rescate / Corte Minimo 
					$scope.data.kngCantidad = Math.trunc(Number($scope.data.kngValorRescate) / Number($scope.data.kngCorteMinimo));	
				}

			}
		});		
		
		$scope.data.ingresarTitulo = function(param) 
	    {
			todoOk = true;
			var instrumentoCustodia = ($scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion !== 'INGTIT' && $scope.data.checkPorCartera)?$scope.instrumentoCustodia:$scope.data.instrumento;
			if(instrumentoCustodia == 'RF'){
				if (Number($scope.data.kngCorteMinimo) > 1){
					if ( Number($scope.data.kngCantidad) < Number($scope.data.kngCorteMinimo)  ){
						bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_CANTIDAD_NO_CALZA_CON_CORTE_MINIMO"));
						todoOk = false;
						return;
					}
					if ( Number($scope.data.kngCantidad) % Number($scope.data.kngCorteMinimo)  > 1 ) {
						bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_CANTIDAD_NO_CALZA_CON_CORTE_MINIMO"));
						todoOk = false;
						return;
					}
				}
			}
			if(instrumentoCustodia == 'IF'){
				if ( Number($scope.data.kngValorRescate)%Number($scope.data.kngCorteMinimo)  > 1 ) {
					bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_CANTIDAD_NO_CALZA_CON_CORTE_MINIMO"));
					todoOk = false;
					return;
				}
			}
			//TODO Validación para transferencias de portafolios, valida que portafolio origen y destino sean distintos.
			if ($scope.data.oDesplegar.showBovedaDestino && $scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion !== 'INGTIT' && 
					(($scope.SesionLocale ==='CL' && $scope.instrumentoCustodia.IdBoveda == $scope.data.oConfiguracionBovedaDestino.seleccionado.IdBoveda)
						||
					 ($scope.SesionLocale ==='PE' && $scope.instrumentoCustodia.IdBoveda == $scope.data.oConfiguracionCuentaCustodioPortafolioDestino.idActual)
					)
				){
				bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_VALIDACION_BOVEDA_ORIGEN_DESTINO_IGUALES"));
				todoOk = false;
				return;
			}

			//TODO Validación para transferencias de portafolios, valida que portafolio origen y destino sean distintos.
			if ($scope.data.oDesplegar.showPortafolioDestino && ($scope.oPortafolio.seleccionado.IdPortafolio == $scope.oPortafolioDestino.seleccionado.IdPortafolio)){
				bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_VALIDACION_PORTAFOLIO_ORIGEN_DESTINO_IGUALES"));
				todoOk = false;
				return;
			}
			//TODO Validación para transferencias entre clientes, valida que cliente origen y destino sean distintos.
			if ($scope.data.oDesplegar.showClienteDestino && ($scope.oSocioNegocio.seleccionado.IdCliente == $scope.oSocioNegocioDestino.seleccionado.IdCliente)){
				bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_VALIDACION_CLIENTE_ORIGEN_DESTINO_IGUALES"));
				todoOk = false;
				return;
			}
			
			var v_precio = Number($scope.data.kngPrecio) || 0;
			if ($scope.data.oDesplegar.showPrecioPorcentaje === true)
				v_precio = Number($scope.data.kngPrecioPorcentaje) || 0;
			var v_fechaEmision= '0001-01-01';
			if ($scope.data.oDesplegar.showFechaEmision === true && $scope.data.kngFechaEmision !== undefined && $scope.data.kngFechaEmision !== null)
				v_fechaEmision = util.dateSPdate($scope.data.kngFechaEmision);
			var v_fechaVencimiento= '0001-01-01';
			if ($scope.data.oDesplegar.showFechaVencimiento === true)
				v_fechaVencimiento = util.dateSPdate($scope.data.kngFechaVencimiento);
			var v_tasa= 0; 
			if ($scope.data.oDesplegar.showTasa === true)	
				v_tasa= Number($scope.data.kngTasa_) || 0;
			if (($scope.data.oDesplegar.showPrecio || $scope.data.oDesplegar.showPrecioPorcentaje) && $scope.data.oDesplegar.showTasa)
				v_tasa= Number($scope.data.kngTasa) || 0;
			var	v_emisor = '';
			if ($scope.data.oDesplegar.showEmisor === true){	
				v_emisor = $scope.data.oConfiguracionEmisor.seleccionado.IdEmisor || '';
				if ($scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion !== 'INGTIT' && $scope.data.checkPorCartera)
					v_emisor = $scope.instrumentoCustodia.IdEmisor || '';
			} else{
				if ($scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion !== 'INGTIT' && $scope.data.checkPorCartera){
					v_emisor = $scope.instrumentoCustodia.IdEmisor || '';
				}else{ 
					v_emisor = $scope.data.instrumento.IdEmisor || '';
				}
			}
			var v_boveda = '';
			if ($scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion !== 'INGTIT' && $scope.data.checkPorCartera)
				v_boveda = $scope.instrumentoCustodia.IdBoveda || '';
			else{
				v_boveda = $scope.data.oConfiguracionBoveda.seleccionado.IdBoveda || '';	
			} 				
			
			var v_bovedaDestino = '';
			if ($scope.SesionLocale === 'PE' && typeof $scope.data.oConfiguracionBovedaDestino.seleccionado !== 'undefined' ){
				v_bovedaDestino = $scope.data.oConfiguracionBovedaDestino.seleccionado.IdBoveda || '';
			}
			
			var v_IdCuentaCustodioPortafolio = 0;
			var v_CodigoCustodioPortafolio = '';
			if ($scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion !== 'INGTIT' && $scope.data.checkPorCartera){
				v_IdCuentaCustodioPortafolio = $scope.instrumentoCustodia.IdCuentaCustodioPortafolio || 0;
				v_CodigoCustodioPortafolio = $scope.instrumentoCustodia.CodigoCuentaCustodio || '';
			}else{
				v_IdCuentaCustodioPortafolio = $scope.data.oConfiguracionCuentaCustodioPortafolio.seleccionado.IdCuentaCustodioPortafolio || 0;	
				v_CodigoCustodioPortafolio = $scope.data.oConfiguracionCuentaCustodioPortafolio.seleccionado.Codigo || '';
			} 	

			var v_instrumento = 0;
			var v_IdTipoInstr = '';
			var v_clasificacionTipoIns = '';
			if ($scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion !== 'INGTIT' && $scope.data.checkPorCartera){
				v_instrumento = $scope.instrumentoCustodia.IdInstrumento || 0;
				v_IdTipoInstr = $scope.instrumentoCustodia.IdTipoInstrumento || '';
				v_clasificacionTipoIns = $scope.instrumentoCustodia.IdClasificacionTipoInstrumento || '';
					
			}else{
				v_instrumento = $scope.data.instrumento.IdInstrumento || 0;
				v_IdTipoInstr = $scope.data.instrumento.IdTipoInstrumento || '';
				v_clasificacionTipoIns = $scope.data.instrumento.IdClasificacionTipoInstrumento || '';
			}
			
			var v_nemotecnico = '';
			if ($scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion !== 'INGTIT' && $scope.data.checkPorCartera)
				v_nemotecnico = $scope.instrumentoCustodia.Nemotecnico || '';
			else
				v_nemotecnico = $scope.data.instrumento.Nemotecnico || '';


			var v_nemoSVS = '';
			if ($scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion !== 'INGTIT' && $scope.data.checkPorCartera)
				v_nemoSVS = '';
			else
				v_nemoSVS = ($scope.data.instrumento.IndicadorBolson === 'S' ? $scope.data.instrumento.Nemotecnico : '');

			
			var v_IdMonedaInstrumento = '';
			if ($scope.data.oDesplegar.showFechaEmision === true){
				if ($scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion !== 'INGTIT' && $scope.data.checkPorCartera)
					v_IdMonedaInstrumento = $scope.instrumentoCustodia.IdMoneda || '';
				else
					v_IdMonedaInstrumento = $scope.data.oConfiguracionMonedaPagoInstrumento.seleccionado.IdMonedaLiquidacion || '';
			}else{	
				if ($scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion !== 'INGTIT' && $scope.data.checkPorCartera)
					v_IdMonedaInstrumento = $scope.instrumentoCustodia.IdMoneda || '';
				else
					if ($scope.data.instrumento.IdClasificacionTipoInstrumento == 'RV'){
						v_IdMonedaInstrumento = $scope.data.oConfiguracionMonedaPagoInstrumento.seleccionado.IdMoneda || '';
					}else{
						v_IdMonedaInstrumento = $scope.data.oConfiguracionMonedaPagoInstrumento.seleccionado.IdMonedaLiquidacion || '';	
					}
					
			}
			
			var v_IdMonedaEmision= ''; 
			if ($scope.data.oDesplegar.showFechaEmision === true){
				if ($scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion !== 'INGTIT' && $scope.data.checkPorCartera)
					v_IdMonedaEmision = $scope.instrumentoCustodia.IdMonedaEmision || '';
				else {
					if ($scope.data.instrumento.IdClasificacionTipoInstrumento === 'IF') {
						v_IdMonedaEmision = $scope.data.oConfiguracionTipoReajuste.seleccionado.IdTipoReajuste
					}
					else {
						v_IdMonedaEmision = $scope.data.instrumento.IdMonedaEmision;
					}
					
				}
			}else{	
				if ($scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion !== 'INGTIT' && $scope.data.checkPorCartera)
					v_IdMonedaEmision = $scope.instrumentoCustodia.IdMonedaEmision || '';
				else {
					if ($scope.data.instrumento.IdClasificacionTipoInstrumento === 'IF') {
						v_IdMonedaEmision = $scope.data.instrumento.IdTipoReajuste
					}
					else {
						v_IdMonedaEmision = $scope.data.instrumento.IdMonedaEmision;
					}
				}
			}
			
			var v_tipoReajuste= ''; 
			if ($scope.data.oDesplegar.showTipoReajuste === true){	
				v_tipoReajuste= $scope.data.oConfiguracionTipoReajuste.seleccionado.IdTipoReajuste || '';
				if ($scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion !== 'INGTIT' && $scope.data.checkPorCartera)
					v_tipoReajuste = $scope.instrumentoCustodia.IdTipoReajuste || '';
			}else{	
				if ($scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion !== 'INGTIT' && $scope.data.checkPorCartera)
					v_tipoReajuste = $scope.instrumentoCustodia.IdTipoReajuste || '';
				else
					v_tipoReajuste= $scope.data.instrumento.IdTipoReajuste || '';
			}
			if (v_tipoReajuste == '')
				v_tipoReajuste = v_IdMonedaInstrumento;
			
			var v_valorRescate = 0;
			if ($scope.data.oDesplegar.showValorRescate === true){
				if ($scope.SesionLocale === 'CL' )
					v_valorRescate= Number($scope.data.kngValorRescate) || 0;				
			};
			if ($scope.data.oDesplegar.showValorNominal === true){				
				if ($scope.SesionLocale === 'PE' && $scope.data.oConfiguracionClasificacion.seleccionado.IdClasificacionTipoInstrumento === 'RF')
					v_valorRescate= 0;
			};
				
			var v_cantidad = 0;
			if ($scope.SesionLocale === 'PE' && $scope.data.oConfiguracionClasificacion.seleccionado.IdClasificacionTipoInstrumento === 'RF'){
				v_cantidad = Number($scope.data.kngValorNominal) || 0;
			} else {
				v_cantidad = Number($scope.data.kngCantidad) || 0;
			};
			
			var v_EsTransferencia = $scope.data.oConfiguracionConcepto.seleccionado.EsTransferenciaCliente		=== 'S' || 
									$scope.data.oConfiguracionConcepto.seleccionado.EsTransferenciaPortafolio 	=== 'S' ||
									$scope.data.oConfiguracionConcepto.seleccionado.GeneraTransferenciaBoveda 	=== 'S' ? 'S' : 'N';
			
			//Preparar datos a enviar al proceso
			var datos = {};
			datos.p_IdEmpresa 					= $scope.SesionIdEmpresa; 
			datos.p_IdUsuario 					= $scope.SesionIdUsuario;
			datos.p_IdModulo 					= 'CUS';
			datos.p_simularOperacion 			= 'N';
			datos.p_UsaReglasNegocio 			= 'S';
			datos.p_Certifica					= "N";  //VALOR FIJO

			var v_FechaOperacionOriginal				= '';

			if ($scope.data.kngFechaAdquisicion){
				v_FechaOperacionOriginal = util.dateSPdate($scope.data.kngFechaAdquisicion);
			}else{
				v_FechaOperacionOriginal = '0001-01-01';
			};
			
			var tablaInstrumento = [];
			var aux = {
				"IdGenerarMovimientoCustodia"	: 1,
				"FechaOperacion"				: util.dateSPdate($scope.data.kngFechaOperacion),
				"FechaOperacionOriginal"		: v_FechaOperacionOriginal,
				"FechaVencimiento" 				: '0001-01-01', //v_clasificacionTipoIns !== 'IF'?'0001-01-01':v_fechaVencimiento, //preguntar mañana
				"FechaLiquidacion" 				: util.dateSPdate($scope.data.kngFechaCondicionLiquidacion),
				"IdConcepto" 					: $scope.data.oConfiguracionConcepto.seleccionado.IdConcepto || 0,
				"IdBolsa"	 					: $scope.SesionBolsaDefectoSistema || '',
				"IdTipoOperacion" 				: $scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion || '',
				"IdTipoProducto" 				: $scope.data.oConfiguracionOperacion.seleccionado.IdTipoProducto || '',
				
				"IdCliente" 					: $scope.oSocioNegocio.seleccionado.IdCliente || -1,
				"IdPortafolio" 					: $scope.oPortafolio.seleccionado.IdPortafolio || -1,
				"IdBoveda" 						: v_boveda,
				"CuentaBoveda"					: $scope.SesionLocale === 'PE'? v_CodigoCustodioPortafolio :'',
				"IdCuentaCustodioPortafolio"	: $scope.SesionLocale === 'PE'? v_IdCuentaCustodioPortafolio :0,				
				
				"IdClienteDestino"				: $scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion !== 'INGTIT' && v_EsTransferencia === 'S' ? ($scope.data.oConfiguracionConcepto.seleccionado.EsTransferenciaCliente === 'S' ? $scope.oSocioNegocioDestino.seleccionado.IdCliente : $scope.oSocioNegocio.seleccionado.IdCliente ) : 0,
				"IdPortafolioDestino"			: $scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion !== 'INGTIT' && v_EsTransferencia === 'S' ? ($scope.data.oConfiguracionConcepto.seleccionado.EsTransferenciaPortafolio === 'S' || $scope.data.oConfiguracionConcepto.seleccionado.EsTransferenciaCliente === 'S' ? $scope.oPortafolioDestino.seleccionado.IdPortafolio : $scope.oPortafolio.seleccionado.IdPortafolio ) : 0,								
				"IdBovedaRelacionada" 			: ($scope.data.oConfiguracionConcepto.seleccionado.GeneraTransferenciaBoveda === 'S' && $scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion !== 'INGTIT') ? $scope.data.oConfiguracionBovedaDestino.seleccionado.IdBoveda : '',
				"CuentaBovedaRelacionada"		: ($scope.data.oConfiguracionConcepto.seleccionado.GeneraTransferenciaBoveda === 'S' && $scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion !== 'INGTIT' && $scope.data.oConfiguracionCuentaCustodioPortafolioDestino.seleccionado !== undefined) ? $scope.data.oConfiguracionCuentaCustodioPortafolioDestino.seleccionado.Codigo : '',
				"IdCuentaCustodioPortafolioRelacionado"	: ($scope.data.oConfiguracionConcepto.seleccionado.GeneraTransferenciaBoveda === 'S' && $scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion !== 'INGTIT' && $scope.data.oConfiguracionCuentaCustodioPortafolioDestino.seleccionado !== undefined) ? $scope.data.oConfiguracionCuentaCustodioPortafolioDestino.seleccionado.IdCuentaCustodioPortafolio : 0,
						
						
				"esTraspasoBoveda" 				: ($scope.data.oConfiguracionConcepto.seleccionado.GeneraTransferenciaBoveda === 'S' && $scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion !== 'INGTIT') ? 'S' :'N',
				"esTraspasoPortafolio" 			: ($scope.data.oConfiguracionConcepto.seleccionado.EsTransferenciaPortafolio === 'S' && $scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion !== 'INGTIT') ? 'S' : 'N',				
				"esTraspasoCliente" 			: ($scope.data.oConfiguracionConcepto.seleccionado.EsTransferenciaCliente === 'S' && $scope.data.oConfiguracionOperacion.seleccionado.IdTipoOperacion !== 'INGTIT') ? 'S' : 'N',
				"DebeCertificar"				: ($scope.data.checkCertificadoAutomatico)? 'S' : 'N',
				"EsGarantia" 					: 'N',
				"PlazoEnDias" 					: ($scope.data.oDesplegar.showFechaVencimiento === true? $scope.data.kngPlazo || 0: 0),
				"IdCartera" 					: 0,
				"FolioCartera" 					: Number($scope.data.kngFolioCartera) || 0,
				"IdCondicionLiquidacion" 		: $scope.data.oConfiguracionCondicionLiquidacion.seleccionado.IdCondicionDeLiquidacion || '',
				"FolioExternoReferencia" 		: $scope.data.kngComprobanteReferencia || '',
				"Glosa" 						: $scope.data.ngComentario || '',
				"IndicadorNominal" 				: 'N', //TODO que es indicador nominal
				"IdAgente" 						: $scope.SesionIdUsuario, //TODO falta recuperar de la sesion
				"IdSucursal" 					: 0, //TODO falta recuperar de la sesion				
				"IdMonedaSistema" 				: $scope.SesionIdMonedaSistema,
				"IdTipoCanalIngreso" 			: $scope.SesionTipoCanalIngreso, 
				"IdClasificacionTipoInstrumento" : $scope.data.oConfiguracionClasificacion.seleccionado.IdClasificacionTipoInstrumento || '',
				"IdTipoInstrumento" 			: v_IdTipoInstr || '',
				"IdInstrumento" 				: v_instrumento,
				"Nemotecnico" 					: v_nemotecnico,
				"IdEmisor" 						: v_emisor,
				"IdMonedaInstrumento" 			: v_IdMonedaInstrumento || '',
				"IdMonedaEmision" 				: v_IdMonedaEmision || '',
				"IdTipoReajuste" 				: v_tipoReajuste || '',
				"IdMonedaAdicional"				: $scope.instrumentoCustodia.NombreCompletoMonedaInstrumento||$scope.data.kngIdMonedaPagoInstrumento ,
				"FechaEmisionInstrumento" 		: v_fechaEmision,
				"FechaVencimientoInstrumento" 	: v_fechaVencimiento,
				"Fungible"						: ($scope.data.checkFungible)? 'S' : 'N',
				"NemoSVS" 						: '',
				"Precio" 						: v_precio,
				"Cantidad" 						: v_cantidad,
				"ValorRescate" 					: v_valorRescate,
				"CorteMinimo" 					: Number($scope.data.kngCorteMinimo) || 1,
				"Monto" 						: Number($scope.data.kngMonto) || 0,
				"MontoEnMonedaInstrumento" 		: Number($scope.data.kngMontoValorizadoMonedaOrigen) || 0,
				"MontoEnMonedaAdicional" 		: Number($scope.data.kngMontoValorizadoMonedaAdicional) || 0,
				"Tasa" 							: v_tasa,
				"TasaAgente" 					: 0,
				"CodigoIFRS"					: ($scope.oSocioNegocio.seleccionado.EsCuentaPropia === "UI_SI" ? $scope.data.oConfiguracionClasificacionIfrs.seleccionado.Codigo : ''),
				"EsCtaPropia" 					: ($scope.oSocioNegocio.seleccionado.EsCuentaPropia === "UI_SI" ? "S" : "N"),
				"IdCorredora"					: $scope.data.oConfiguracionCorredor.seleccionado.IdCorredora || 0, 
				"EsInstrumentoCalificado"		: $scope.data.checkPorCartera ? $scope.instrumentoCustodia.EsInstrumentoCalificado : $scope.data.instrumento.EsInstrumentoCalificado
			};				
			tablaInstrumento.push(aux);
			datos.P_INSTRUMENTOS = tablaInstrumento;
			
			if (todoOk) {
				bcsComunes.ventanaOverlay($scope.ventanaId,true);
				$scope.data.accionDisabled = undefined;
				$scope.data.callIngresoRetiroTitulo(datos);
				
			}
	   	};
	   	//respuesta.P_OUT_MENSAJES[1]
	   	$scope.data.callIngresoRetiroTitulo = function(datos){
			bcsServiciosCustodia.IngresarTituloJava(datos, function(respuesta){
				bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(respuesta)
				},$scope.ventanaId);
				$scope.data.btnLimpiar();
				valorizadorEnEjecucion = false;
				bcsComunes.ventanaOverlay($scope.ventanaId,false);
			},
			function(respuesta){
				bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(respuesta)
				},$scope.ventanaId);
				valorizadorEnEjecucion = false;
				bcsComunes.ventanaOverlay($scope.ventanaId,false);
			}
		);
	   	} 
		
	   	
	});
}());
//------------------------- FIN CONTROLLER --------------------------	 