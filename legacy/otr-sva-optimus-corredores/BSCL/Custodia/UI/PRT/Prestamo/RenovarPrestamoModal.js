/*************************************************************
 * Purpose	: Renovar Préstamo de Títulos
 * Author	: GAT-BLAC - BCS
 * Date		: 10-04-2018
 *************************************************************/
 (function() {
	angular.module('ModuloCustodia').controller('RenovarPrestamoModalCtrl',
	function ($rootScope,$scope,$translate,$timeout,bcsComunes,bcsServiciosCustodia,
			bcsServicioPlataforma,bcsServicioCalcularMontoTipoInstrumento,bcsServiciosPrestamo) {
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		$scope.data = {};
		var cargando = true;
		
		var idVentana = bcsComunes.ventanaWinId($scope);
		$scope.data.prtRenovado = bcsComunes.ventanaParams(idVentana);
		
		$scope.idVentana = idVentana;
		bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise('CUS.PrestamosRentaFija', $scope.SesionIdEmpresa, 'PLATAFORMA')
		.then(function ( parametroPrestamosRentaFija ) {
			if ( $scope.data.prtRenovado.IdClasificacionTipoInstrumentoSubyacente === 'RF' ) {
				$scope.data.muestraDatosRF = true;
				if ( parametroPrestamosRentaFija.Valor === 'N' ) {
					bcsComunes.ventanaMsgErr($translate.instant('PRESTAMOS_MERCADO_RENTA_FIJA_DESHABILITADO'),$scope.ventanaId);
				}
			}
		});
		$scope.data.checkCobroAlInicio = ($scope.data.prtRenovado.CobraPrimaAlInicio==='S')?true:false;
		
		
		//verificar contrato de custodia
		$scope.data.checkCustodiaExterna = false;
		$scope.data.yaValorizo = false;
		$scope.data.monedaCorteMinimo = "";
		$scope.data.monedaSistema = $scope.SesionIdMonedaSistema;
		$scope.data.monedaInstrumento = $scope.data.prtRenovado.IdMonedaSubyacente;
		$scope.data.FechaVencInstrOptions = {};
		$scope.data.oDecimales={};
		$scope.data.oDecimales.corteMinimo={};
		$scope.data.oDecimales.corteMinimo.decimales = 0;
		$scope.data.nginstrumento="-1";
		$scope.data.IdEstado = $scope.data.prtRenovado.IdEstado;
		$scope.data.FolioPrestamoRelacionado = $scope.data.prtRenovado.FolioCarteraRelacionada === 0 ? $scope.data.prtRenovado.FolioPrestamo : $scope.data.prtRenovado.FolioCarteraRelacionada;
		
		var valorizadorEnEjecucion = false;

		$scope.data.fechaInicial = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
		$scope.data.idTipoOperacion = "PRESTA";
		$scope.data.checkTipoCustodia = $scope.data.prtRenovado.TipoCustodia === "S";
		$scope.data.cantidadOptionsMax = $scope.data.prtRenovado.INGCantidad;

		if ($scope.data.IdEstado = 'VCD' && $scope.data.prtRenovado.CantidadPorAsignar > 0) {
			$scope.data.cantidadOptionsMax = $scope.data.prtRenovado.CantidadAsignada;
		}
	
		/*********************************************
		 * CARGA INICIAL
		 ********************************************/
		$scope.data.kngPlazo 				= $scope.data.prtRenovado.MinimoDiasPrestamo;
		$scope.data.cantidadActualOptions	= {format: 'n'+(util.bcs_formato(0,'GEN','Cantidad',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.cantidadOptions 		= {format: 'n'+(util.bcs_formato(0,'GEN','Cantidad',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false, max: $scope.data.cantidadOptionsMax};
		$scope.data.tasaOptions 			= {format: 'n'+(util.bcs_formato(0,'GEN','Tasa',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:-999999999999999999999,spinners: false};
		$scope.data.precioOptions 			= {format: 'n'+(util.bcs_formato(0,'GEN','Precio',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.montoOptions 			= {format: 'c'+(util.bcs_formato(0,'GEN','Monto',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.plazoOptions 			= {format: 'n0',min:0, max:$scope.data.prtRenovado.MaximoDiasPrestamo, spinners: false};
		$scope.data.porcentajePrimaOptions 	= {format: 'n'+(util.bcs_formato(0,'GEN','Tasa',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.montoPrimaOptions 		= {format: 'c'+(util.bcs_formato(0,'GEN','Monto',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.montoPlazoOptions 		= {format: 'c'+(util.bcs_formato(0,'GEN','Monto',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.accionDisabled 			= true;
		
		$scope.data.kngCantidadAct 			= $scope.data.prtRenovado.INGCantidad;
		$scope.data.kngPrecioAct 			= $scope.data.prtRenovado.INGPrecioInstrumento;
		$scope.data.kngTasaAct 				= $scope.data.prtRenovado.INGTasaPrestamo;
		$scope.data.kngMontoAct 			= ($scope.data.muestraDatosRF ? $scope.data.prtRenovado.INGMontoAPlazoMonedaInstrumento : 0);
		$scope.data.kngFechaVencimientoAct 	= $scope.data.prtRenovado.FechaVencimientoPrestamo;
		$scope.data.kngPlazoAct 			= $scope.data.prtRenovado.PlazoEnDiasOperacion;
		$scope.data.kngPorcentajePrimaAct 	= $scope.data.prtRenovado.TasaPrestamo;
		$scope.data.kngMontoPrimaAct 		= $scope.data.prtRenovado.INGMontoPrima;
		$scope.data.kngMontoPlazoAct 		= $scope.data.prtRenovado.INGMontoCompraActualMonedaInstrumento;
		
		$scope.data.kngCantidad 			  		 = $scope.data.prtRenovado.INGCantidad;
		$scope.data.kngPrecio 				  		 = 0;
		$scope.data.kngIdCondicionLiquidacion 		 = $scope.data.prtRenovado.IdCondicionLiquidacion;
		$scope.data.kngTasa 				  		 = $scope.data.prtRenovado.INGTasaSubyacente;
		$scope.data.kngMonto 				  		 = $scope.data.prtRenovado.INGMontoInstrumento;
		$scope.data.kngPorcentajePrima 		  		 = $scope.data.prtRenovado.INGTasaPrestamo;
		$scope.data.MaximoDiasPrestamo 		  		 = $scope.data.prtRenovado.PlazoDisponibleRenovacion;
		$scope.data.MinimoDiasPrestamo 		  		 = $scope.data.prtRenovado.MinimoDiasPrestamo;
		$scope.data.UsaFechaLiquidacionEnVencimiento = false;
		$scope.data.kngPlazo 						 = $scope.data.prtRenovado.PlazoEnDiasOperacion;
		$scope.data.kngFechaVencimiento 			 = undefined;		
		$scope.data.fechaSistema 					 = util.dateInicioDia($scope.SesionFechaSistema);

		bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise('CUS.UsaFechaLiquidacionEnVencimiento', $scope.SesionIdEmpresa, 'PLATAFORMA')
				.then(function (parametroUsaFechaLiquidacion) {
					$scope.data.UsaFechaLiquidacionEnVencimiento = parametroUsaFechaLiquidacion.Valor === 'S';
				});

		$scope.data.PlazoConsumido 				= Number($scope.data.prtRenovado.MaximoDiasPrestamo) - Number($scope.data.prtRenovado.PlazoDisponibleRenovacion) + ' / ' + $scope.data.prtRenovado.MaximoDiasPrestamo;
		$scope.data.PlazoVencimientoActivaFecha	= true;
		$scope.data.PlazoVencimientoActivaPlazo	= true;
		$scope.data.PlazoVencimientoIdCondicionLiquidacion = 'T0';

		if ($scope.data.MaximoDiasPrestamo < $scope.data.prtRenovado.MinimoDiasPrestamo) {
			$scope.data.PlazoVencimientoActivaFecha				= !$scope.data.PlazoVencimientoActivaFecha;
			$scope.data.PlazoVencimientoActivaPlazo				= !$scope.data.PlazoVencimientoActivaPlazo;
			$scope.data.PlazoVencimientoIdCondicionLiquidacion 	= undefined;
			$scope.data.kngPlazo								= $scope.data.MaximoDiasPrestamo;
			$scope.data.MinimoDiasPrestamo 		  		 		= $scope.data.MaximoDiasPrestamo;
		}
		

		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		$scope.data.btnCerrar = function(refrescar) {
			bcsComunes.ventanaData(idVentana,refrescar);
			bcsComunes.ventanaCerrar(idVentana);
		};

		$scope.data.RenovarPrestamo = function renovarPrestamo() {
			const form = $scope.data;
			const prestamoOriginal = form.prtRenovado;
			const payload = {
				p_IdEmpresa: $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: 'CUS',
				p_UsaReglasNegocio: 'S',
				p_IdTipoCanalIngreso: 'OPT',
				p_SolicitudesRenovacion: [
					{
						IdInterno: 1,
						IdCartera: prestamoOriginal.IdCartera,
						IdEmpleadoDigitador: $scope.SesionIdEmpleado,
						Cantidad: form.kngCantidad,
						Precio: form.kngPrecio,
						TasaInstrumento: form.kngTasa || 0,
						Monto: form.kngMonto,
						MontoPrima: form.kngMontoPrima,
						TasaPrima: form.kngPorcentajePrima,
						MontoPlazo: form.kngMontoPlazo,
						PlazoDiasVencimiento: form.kngPlazo,
						IdMonedaSistema: $scope.SesionIdMonedaSistema || '',
						IdCondicionLiquidacion: form.kngCondicionLiquidacion.IdCondicionDeLiquidacion,
						FechaLiquidacion: form.kngFechaLiquidacion,
						FechaVencimiento: form.kngFechaVencimiento,
					},
				],
			};

			bcsComunes.ventanaOverlay(idVentana, false);
			$scope.data.accionDisabled = undefined;
			
			bcsServiciosPrestamo.RenovarPrestamoMasivo(
					payload,
					function success(respuesta) {
						if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
							const vtnMsg =  bcsComunes.ventanaMsg(
									{
										mensaje: $translate.instant("UI_OPERACION_INGRESO_EXITO"),
										conflictos: util.parseServerResponse(respuesta),
									},
									idVentana,
							);
							bcsComunes.ventanaPromesa(vtnMsg).then(function () {
								$scope.data.btnCerrar(true);
							});
						} else {
							bcsComunes.ventanaMsg(
									{
										mensaje: $translate.instant("UI_OPERACION_INGRESO_ERROR"),
										conflictos: util.parseServerResponse(respuesta),
									},
									idVentana,
							);
						}
					},
					function error() {
						bcsComunes.ventanaOverlay(idVentana, false);
						bcsComunes.setConflictos(util.parseServerResponse(respuesta));
						bcsComunes.ventanaMsg({
							mensaje: $translate.instant("UI_OPERACION_INGRESO_ERROR"),
							conflictos: util.parseServerResponse(respuesta)
						},idVentana);

						$scope.data.accionDisabled = true;
					}
			);
	   	};
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		 var fnCambioInstrumento = function(newValue, oldValue) {
			if (typeof $scope.data.instrumento !== 'undefined' && typeof $scope.SesionFechaSistema !== 'undefined'){
				$scope.data.kngPrecio = 0;
				
				$scope.data.monedaCorteMinimo = $scope.data.instrumento.IdMoneda;
				$scope.data.monedaInstrumento = $scope.data.instrumento.IdMoneda;
				$scope.data.yaValorizo = false;
				
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
					if ($scope.data.instrumento.FechaVencimiento < util.dateInicioDia($scope.SesionFechaSistema)){
						bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_INSTRUMENTO_VENCIDO"));
						return;
					}

					$scope.data.fechaVencInstrOptions = {min:util.dateInicioDia($scope.data.instrumento.FechaVencimiento), max:util.dateInicioDia($scope.data.instrumento.FechaVencimiento)};	
					$scope.data.kngFechaVencimientoInstr = util.dateInicioDia($scope.data.instrumento.FechaVencimiento);

				}
				
				if ($scope.data.instrumento.IdClasificacionTipoInstrumento === 'RF'){
					if($scope.data.instrumento.FechaVencimiento !== undefined && !util.dateisdefault($scope.data.instrumento.FechaVencimiento)){
						$scope.data.kngFechaVencimientoInstr = util.dateInicioDia($scope.data.instrumento.FechaVencimiento);
						$scope.data.kngPlazoInstr = Math.round(util.datediff(util.dateInicioDia($scope.data.instrumento.FechaVencimiento),util.dateInicioDia($scope.SesionFechaSistema)).asDays());
					}
					$scope.data.kngCorteMinimo = $scope.data.instrumento.CorteMinimo || 1;	
				}
				
				if ($scope.data.instrumento.IdTipoInstrumento !== undefined){
					$scope.data.kngPrecio = Number($scope.data.instrumento.PrecioCierre || 0);
				}else {
					$scope.data.kngPrecio = 0;
				}

			}
		};

		var fnCambioPQ = function(newValue, oldValue) {
			if (   typeof $scope.data.kngPrecio   !== 'undefined' 
				&& typeof $scope.data.kngCantidad !== 'undefined' )
			{

					if ($scope.data.prtRenovado.IdClasificacionTipoInstrumentoSubyacente == "RV"){
						if (!cargando) $scope.data.kngMonto = 0;
						var datos = {};
						//Rescata valores ingresados en formulario
						//VARIABLES DE ENTRADA OBLIGATORIAS
						datos.p_IdEmpresa 			= $scope.SesionIdEmpresa;
						datos.p_IdUsuario 			= $scope.SesionIdUsuario;
						datos.p_IdModulo 			= 'CUS';
						datos.p_UsaReglasNegocio 	= 'S';

						//ENUMERAR VARIABLES DE ENTRADA
						datos.p_Cantidad 			= $scope.data.kngCantidad || 0;
						datos.p_Precio 				= $scope.data.kngPrecio || 0;
						datos.p_Tamano 				= 1;
						datos.p_IdTipoInstrumento 	= $scope.data.prtRenovado.IdTipoInstrumentoSubyacente || ''; 
						datos.p_AplicaValidaciones 	= 'N';

						bcsComunes.ventanaOverlay(idVentana,true);
						bcsServicioCalcularMontoTipoInstrumento.llamar(datos,function(result){
							bcsComunes.ventanaOverlay(idVentana,false);
							if (result.P_OUT_ERRORESCONTADOR === 0) {
								$scope.data.kngMonto = Number(result.P_OUT_MONTO);
								$scope.data.yaValorizo = true;
							}
							else {
								$scope.data.kngMonto = 0;
								console.log("Error." + result.P_OUT_ERRORESCONTADOR);
							}
						},function(result) {
							bcsComunes.ventanaOverlay(idVentana,false);
							console.log(JSON.stringify(result));
							console.log("revisar");
						});
						bcsComunes.ventanaTamanoAutomatico(idVentana);


					} else if ($scope.data.prtRenovado.IdClasificacionTipoInstrumentoSubyacente == "RF" ){
						
						var laCantidad = Number($scope.data.kngCantidad) || 0;
						var elCorteMin = $scope.data.prtRenovado.CorteMinimo || 1;
						
						if (laCantidad > 0 && elCorteMin > 1) {
							if(laCantidad % elCorteMin !==0){
								bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("CORTE_MINIMO_INVALIDO"));
								$scope.data.kngCantidad = 0;
								return;								
							}
						}
						if (valorizadorEnEjecucion) return;
						
						if (!cargando) $scope.data.kngMonto = 0;
						
						$scope.data.yaValorizo = false;
					}

			}

			if ($scope.data.kngCantidad === null) 
				$scope.data.kngCantidad = 0;
		};	

		
		/*********************************************
		 * WATCHERS
		 *********************************************/
		
		$scope.$watchGroup(['data.kngCantidad','data.kngPrecio','data.kngTasa'], fnCambioPQ );

		$scope.$watchGroup(['data.kngMonto','data.kngPorcentajePrima','data.kngPlazo'], function (newValue, oldValue){
			if (   typeof $scope.data.kngMonto !== 'undefined' 
				&& typeof $scope.data.kngPorcentajePrima !== 'undefined' 
				&& typeof $scope.data.kngPlazo !== 'undefined')
			{
				const base = $scope.data.monedaInstrumento === 'CLP' ? 30 : 360;
				$scope.data.kngMontoPlazo = bcsComunes.calcularMontoaPlazo($scope.data.kngMonto,$scope.data.kngPlazo,Number($scope.data.kngPorcentajePrima), base);
				$scope.data.kngMontoPrima = Number($scope.data.kngMontoPlazo) - Number($scope.data.kngMonto); 
			}
		});
		
		$scope.$watch('data.instrumento', fnCambioInstrumento,true);
	   	
		$scope.$watchGroup(['data.kngFechaLiquidacion'], function (newValue, oldValue){
			if (!newValue[0] && !oldValue[0]) return;
			if (oldValue[0] && util.dateSPdate(newValue[0]) === util.dateSPdate(oldValue[0])) return;
			$scope.data.kngTasa = undefined;
		});

		/*********************************************
		 * GO!
		 *********************************************/
	 	//Primer evento - filtro inicial
		$timeout(function(){
			bcsComunes.ventanaTamanoAutomatico(idVentana);
		},1000);
		
		
		
		/********************************************
		****** FUNCIONES PARA VALORIZACION **********
		*********************************************/
		
		var valorizando = function(valor){
			valorizadorEnEjecucion=valor;
			bcsComunes.ventanaOverlay($scope.idVentana ,valor);
		};
		
		//funcion genera template entrada valorizador
		var generaCabeceraValorizar = function(moneda){
			return {
				p_IdEmpresa 		: $scope.$root.SesionIdEmpresa,
				p_IdUsuario 		: $scope.$root.SesionIdUsuario,
				p_IdModulo 			: 'CUS',
				p_IdPais 			: $scope.$root.SesionIdPaisSistema,  
				p_OrigenProcesamiento : 'CUSTODIA_UI',
				p_IdMonedaAdicional : moneda,
				p_Instrumentos		: [],
			};
		};
		//funcion genera template detalle entrada valorizador modo PQ(RV)
		var generaBaseDetallePQ = function(idVal){
			return {
				IdValorizacion		: idVal,
				FechaValorizacion	: $scope.$root.SesionFechaSistema,
				IdInstrumento		: null,
				Nemotecnico			: null,
				IdMonedaInstrumento	: null,
				Cantidad			: null,
				Precio				: null,
				Monto				: null
			}
		};
		//funcion genera detalle entrada valorizador modo WS(RF,IF)
		var generaBaseDetalleWS = function(idVal){
			return {
				IdValorizacion				: idVal,
				FechaValorizacion			: util.dateSPdate($scope.data.kngFechaLiquidacion),
				FechaOperacion				: $scope.$root.SesionFechaSistema,
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
		
		//funcion que resuelve respuesta valorizador modo PQ(RV)
		var resolverResptaValorizacionPQ = function(item,valorizarMercado){
			if(item.ResultadoProceso === 'ERROR') {
				$scope.data.kngMonto = undefined;
				bcsComunes.msg("warning",'Valorizador',item.DescripcionMensaje);
			} else {

				$scope.data.kngMonto = Number(item.MontoMonedaSistema);
				$scope.data.kngCantidad = Number(item.Cantidad);
				$scope.data.kngPrecio = Number(item.Precio);
				$scope.data.yaValorizo = true;
			}
		};
		//funcion que resuelve respuesta valorizador modo WS(RF,IF)
		var resolverResptaValorizacionWS = function(item,valorizarMercado){
			if(item.ResultadoProceso === 'ERROR'){
				bcsComunes.msg("warning",'Valorizador',item.DescripcionWSCaller);
			} else {
				$scope.data.kngMonto	= Number(item.MontoMonedaInstrumento);
				$scope.data.kngTasa 	= item.Tasa;
				$scope.data.kngPrecio 	= item.Precio;
				$scope.data.yaValorizo 	= true;
			}
		};	
		//funcion para llamar al servicio de valorizacion
		var callValorizador = function(cval,IdClasificacionTipoInstrumento,valorizarMercado){
			var nombreRespuesta='P_OUT_INSTRUMENTOSVALORIZADOS';
			valorizando(true);
			bcsServicioPlataforma.ValorizarInstrumento(cval,
				function(respuesta){
					if(respuesta[nombreRespuesta].length==0){
						var p=bcsComunes.ventanaMsgErr($translate.instant('UI_SIN_DATOS_VALORIZADOS'),$scope.ventanaId);
						bcsComunes.ventanaPromesa(p).then(function(){
							$scope.errorValorizacionSinDatos=true;
							valorizando(false);
						});
						return;
					}
					var resultadoCompra = respuesta[nombreRespuesta][0];
					if(IdClasificacionTipoInstrumento=='RV') resolverResptaValorizacionPQ(resultadoCompra,false);
					else resolverResptaValorizacionWS(resultadoCompra,false);
					if(valorizarMercado){
						if(respuesta[nombreRespuesta].length==2){
							var resultadoMercado = respuesta[nombreRespuesta][1];
							if(IdClasificacionTipoInstrumento=='RV') resolverResptaValorizacionPQ(resultadoMercado,valorizarMercado);
							else resolverResptaValorizacionWS(resultadoMercado,valorizarMercado);
						}else{
							bcsComunes.msg("warning",'Valorizador',$translate.instant("UI_SIN_VALORIZACION_MERCADO"));
							valorizando(false);
						}
					}
					$timeout(function(){valorizando(false);},200);//evita que inmediatamente valorice tras cambio de valores.
				},function(error){
					var p=bcsComunes.ventanaMsgErr($translate.instant('UI_ERROR_EN_COMUNICACION'),$scope.ventanaId);
					bcsComunes.ventanaPromesa(p).then(function(){
						$scope.errorRed=true;
						valorizando(false);
					});
					return;
				}
			);
		};
		

		var fnvalorizarRF = function( Cantidad, Precio, Tasa, Monto ) {
			var Instrumento = $scope.data.instrumento || undefined;
			if ( !Instrumento ) {
				valorizando( false );
				return;
			}

			var FechaVencimiento 	= ( Instrumento.FechaVencimiento ) ? util.dateSPdate(Instrumento.FechaVencimiento) : undefined;					
			
			if ( !FechaVencimiento || FechaVencimiento == $scope.$root.SesionFechaSistema ) {
				valorizando( false );
				return;
			}			
			var IdClasificacionTipoInstrumento = Instrumento.IdClasificacionTipoInstrumento;
			var Cantidad 			= Cantidad !== undefined ? Cantidad : 0;
			var Tasa 				= Tasa !== undefined ? Tasa : 0;
			var Precio	 			= Precio !== undefined ? Precio :0;
			var Monto 				= Monto !== undefined ? Monto : 0;

			var condicion;
			if ( Cantidad > 0 && Precio > 0 && Tasa === undefined && ( Monto === undefined || Monto ===0 ) ) condicion = 'A';
			else if ( Cantidad > 0 && Tasa > 0 && Precio === undefined && ( Monto === undefined || Monto === 0 ) ) condicion = 'B';
			else if ( Cantidad > 0 && Tasa >= 0 && Precio >= 0 ) condicion = 'TODOS';
			else {
				valorizando( false );
				return;
			}

			var valorizarMercado 	= false;
			var cval 				= generaCabeceraValorizar( $scope.data.prtRenovado.IdMonedaSubyacente );	
			var dvalCompra 			= generaBaseDetalleWS(1);			

			dvalCompra.Cantidad							= Cantidad;
			dvalCompra.IdTipoInstrumento				= Instrumento.IdTipoInstrumento;
			dvalCompra.IdInstrumento					= Instrumento.IdInstrumento;
			dvalCompra.Nemotecnico						= Instrumento.Nemotecnico;
			dvalCompra.FechaEmision						= Instrumento.FechaEmision;
			dvalCompra.FechaVencimiento					= FechaVencimiento;
			dvalCompra.IdMonedaInstrumento				= Instrumento.IdMoneda;				
			dvalCompra.IdMonedaEmisionInstrumento		= Instrumento.IdMonedaEmision;
			dvalCompra.IdTipoReajusteInstrumento		= $scope.data.prtRenovado.IdTipoReajusteSubyacente || Instrumento.IdTipoReajuste;
			dvalCompra.ValorRescate						= null;
			dvalCompra.PrecioCompra						= Precio || null;
			dvalCompra.TasaCompra						= Tasa || null;				
			dvalCompra.MontoCompra						= Monto || null;
			dvalCompra.Emisor 							= $scope.data.prtRenovado.IdEmisorSubyacente;
			dvalCompra.IdCondicionDeLiquidacion			= $scope.data.kngCondicionLiquidacion.IdCondicionDeLiquidacion || null;
			dvalCompra.IdClasificacionTipoInstrumento	= $scope.data.prtRenovado.IdClasificacionTipoInstrumentoSubyacente;
			cval.p_Instrumentos.push(dvalCompra);
			callValorizador(cval,IdClasificacionTipoInstrumento,valorizarMercado);
		};
		
		
		$scope.data.ObtenerValorizacion = function(cambio){
			$scope.data.yaValorizo = false;
			if ($scope.data.muestraDatosRF){
				if (typeof $scope.data.kngPrecio !== 'undefined' && $scope.data.kngPrecio > 0  && typeof $scope.data.kngTasa !== 'undefined' && $scope.data.kngTasa > 0){
					bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_INDICAR_CANTIDAD_O_PRECIO"));
					return;
				};
				
				if (typeof $scope.data.kngPrecio !== 'undefined' && $scope.data.kngPrecio > 0)
					cambio = 'Precio';
				if (typeof $scope.data.kngTasa !== 'undefined' && $scope.data.kngTasa > 0)
					cambio = 'Tasa';
			};
			
			switch (cambio){
				case "Tasa"		: $scope.data.kngPrecio = undefined; break;
				case "Precio"	: $scope.data.kngTasa = undefined; break;
			}
			switch ($scope.SesionLocale){
				case "CL":	Cantidad = $scope.data.kngCantidad; break; 
				case "PE":	Cantidad = $scope.data.kngValorRescate; break;
			}
			
			Precio		= $scope.data.kngPrecio;
			Tasa 		= $scope.data.kngTasa;
			Monto		= undefined;

			fnvalorizarRF(Cantidad,Precio,Tasa,Monto);
		};
	
		cargando = false;
/************************************************************************
 *********** FIN FUNCIONES PARA VALORIZACION ****************************
 ************************************************************************/
	});
}());
//------------------------- FIN CONTROLLER --------------------------
