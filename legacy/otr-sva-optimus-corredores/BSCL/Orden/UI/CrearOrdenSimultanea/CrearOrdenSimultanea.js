angular.module('ModuloOrdenes').controller('CrearOrdenSimultaneaCtrl',
		function($scope,bcsComunes,$translate,bcsServicioOrden,bcsServicioOrdenV2,bcsConfiguracionOrdenes,
				$timeout,bcsConfiguracionOrdenesV2,bcsServicioPlataforma,$q,bcsAlerta,OPCIONES_BCSALERTA)
{
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var encuentraCobro = false;
	$scope.operacion = {};  //GUARDA DATOS DE LA OPERACION
	$scope.otrosDatos = {
			idVentana : idVentana,
	};
	$scope.formData = {
		operacionesLineaRiesgo : [],
		modoIngreso 	: true,
		plazoDiaHabil 	: false,
		Simulando 		: false

	};	
	$scope.resumen = {};
	//Inicializa y oculta mensaje de porcentaje de garantia bcs
	$scope.formData.porcentajeGarantia = "";
	
	
	//watch cabecera simultanea venta
	$scope.$watchGroup([
		'operacion.portafolioFinanciado',
		'operacion.bolsaSeleccionada',
		'operacion.operacionSeleccionada'],

		function(newval,oldval){
		
		if ($scope.operacion.portafolioFinanciado && $scope.operacion.portafolioFinanciado.IdPortafolio !== undefined && 
				$scope.operacion.bolsaSeleccionada && $scope.operacion.bolsaSeleccionada.IdBolsa !== undefined &&
				$scope.operacion.operacionSeleccionada&& $scope.operacion.operacionSeleccionada.IdTipoOperacion !== undefined &&
				$scope.operacion.operacionSeleccionada.IdTipoOperacion === 'VENSIM') {
			
			var cabecera = {
					IdPortafolio 			: $scope.operacion.portafolioFinanciado.IdPortafolio,
					IdTipoProducto 			: 'INSI',
					IdTipoOperacion 		: $scope.operacion.operacionSeleccionada.IdTipoOperacion,
					IdCanalIngreso 			: $scope.SesionTipoCanalIngreso,
					IdBolsa 				: $scope.operacion.bolsaSeleccionada.IdBolsa,
					IdCorredorCorresponsal	: null,
			}
			$scope.formData.cabeceraVENSIM = cabecera;
		}
	},true);
	
	$scope.updateOperacionesSimultaneaVenta =  function() {
		if ($scope.operacion.operacionSeleccionada && $scope.operacion.operacionSeleccionada.IdTipoOperacion !== undefined &&
				$scope.operacion.operacionSeleccionada.IdTipoOperacion === 'VENSIM' && 
				$scope.operacion.montoReferencialVenta !== undefined && 
				$scope.operacion.tasaSugerida !== undefined &&
				$scope.operacion.plazoSugerido !== undefined) {
			var operaciones = []
			
			var ventas = $scope.formData.gridVenta.dataSource.data();
			$.each(ventas,function(key,value){
				if (value.CantidadSimulacionSimultanea === 0) return true;
				var op = {
					IdInterno	  					: value.IdInterno,
					IdInstrumento  					: value.IdInstrumento,
					DiasPlazo 						: $scope.operacion.plazoSugerido,
					CantidadOrden   				: value.CantidadSimulacionSimultanea,
					PrecioOrden		   				: value.Precio,
					MontoOrden						: 0,
					TasaSimultanea  				: $scope.operacion.tasaSugerida,
					IdMonedaLiquidacionOrden        : $scope.SesionIdMonedaSistema	
				}
				operaciones.push(op);
			})
			
			if (operaciones.length > 0)
				$scope.formData.operacionesVenta = operaciones;

		}	
	}
	
	//watch cabecera simultanea compra
	$scope.$watchGroup([
		'operacion.portafolioFinancista',
		'operacion.bolsaSeleccionada',
		'operacion.operacionSeleccionada'],

		function(newval,oldval){
		
		if ($scope.operacion.portafolioFinancista && $scope.operacion.portafolioFinancista.IdPortafolio !== undefined && 
				$scope.operacion.bolsaSeleccionada && $scope.operacion.bolsaSeleccionada.IdBolsa !== undefined &&
				$scope.operacion.operacionSeleccionada&& $scope.operacion.operacionSeleccionada.IdTipoOperacion !== undefined &&
				$scope.operacion.operacionSeleccionada.IdTipoOperacion === 'COMSIM') {
			
			var cabecera = {
					IdPortafolio 			: $scope.operacion.portafolioFinancista.IdPortafolio,
					IdTipoProducto 			: 'INSI',
					IdTipoOperacion 		: $scope.operacion.operacionSeleccionada.IdTipoOperacion,
					IdCanalIngreso 			: $scope.SesionTipoCanalIngreso,
					IdBolsa 				: $scope.operacion.bolsaSeleccionada.IdBolsa,
					IdCorredorCorresponsal	: null,
			}
			$scope.formData.cabeceraCOMSIM = cabecera;
		}
	},true);
	
	//watch operacion simultanea compra
	$scope.$watchGroup(['operacion.montoReferencialVenta','operacion.tasaSugerida','operacion.plazoSugerido'],function(newval,oldval) {
		
		if ($scope.operacion.operacionSeleccionada && $scope.operacion.operacionSeleccionada.IdTipoOperacion !== undefined &&
				$scope.operacion.operacionSeleccionada.IdTipoOperacion === 'COMSIM' && 
				$scope.operacion.montoReferencialVenta !== undefined && 
				$scope.operacion.tasaSugerida !== undefined &&
				$scope.operacion.plazoSugerido !== undefined) {
			var operaciones = [{
				IdInterno	  					: 0,
				IdInstrumento  					: $scope.operacion.operacionSeleccionada.IdInstrumento,
				DiasPlazo 						: $scope.operacion.plazoSugerido,
				CantidadOrden   				: 1,
				MontoOrden						: $scope.operacion.montoReferencialVenta,
				TasaSimultanea  				: $scope.operacion.tasaSugerida,
				IdMonedaLiquidacionOrden        : $scope.SesionIdMonedaSistema
			}]
			
			if (operaciones.length > 0)
				$scope.formData.operacionesCompra = operaciones;

		}		
	},true);
	
	//watch exigibilidad (futuros y venta corta)
	$scope.$watchGroup([
	            		'operacion.portafolioFinanciado.IdPortafolio',
	            		'operacion.bolsaSeleccionada.IdBolsa',
	            		'operacion.operacionSeleccionada.IdInstrumento'
	            	],function(newval,oldval){
	            		
	            		var operaciones = [];
	            		if ($scope.operacion.operacionSeleccionada && $scope.operacion.operacionSeleccionada.IdTipoInstrumento
	            		&& $scope.operacion.bolsaSeleccionada && $scope.operacion.bolsaSeleccionada.IdBolsa
	            		&& $scope.operacion.portafolioFinanciado && $scope.operacion.portafolioFinanciado.IdPortafolio) {
	            			
	            			operaciones.push({
	            				EsVentaCorta		: false,
	            				IdTipoInstrumento	: $scope.operacion.operacionSeleccionada.IdTipoInstrumento,
	            				IdBolsa				: $scope.operacion.bolsaSeleccionada.IdBolsa,
	            				IdPortafolio		: $scope.operacion.portafolioFinanciado.IdPortafolio,
	            				IdInstrumento		: 0,
	            				IdInstrumentoExige	: $scope.operacion.operacionSeleccionada.IdInstrumento,
	            				Cantidad			: 0,
	            				Precio				: 0,
	            				Monto 				: 0,	            				
	            			});
	            			
	            		}
	            		if (operaciones.length > 0) 
	            			$scope.formData.operacionesExigibilidad = operaciones;
	            		
	            	});
	
	
	
	//watch cobertura (futuros y venta corta)
	$scope.$watchGroup([
	            		'operacion.portafolioFinanciado.IdPortafolio',
	            		'operacion.bolsaSeleccionada.IdBolsa',
	            		'operacion.operacionSeleccionada.IdInstrumento'
	            	],function(newval,oldval){
	            		
	            		var operaciones = [];
	            		if ($scope.operacion.operacionSeleccionada && $scope.operacion.operacionSeleccionada.IdTipoInstrumento
	            		&& $scope.operacion.bolsaSeleccionada && $scope.operacion.bolsaSeleccionada.IdBolsa
	            		&& $scope.operacion.portafolioFinanciado && $scope.operacion.portafolioFinanciado.IdPortafolio) {
	            			
	            			operaciones.push({
	            				EsVentaCorta		: false,
	            				IdBolsa				: $scope.operacion.bolsaSeleccionada.IdBolsa,
	            				IdPortafolio		: $scope.operacion.portafolioFinanciado.IdPortafolio,
	            				IdInstrumento		: 0,
	            				IdInstrumentoExige	: $scope.operacion.operacionSeleccionada.IdInstrumento || 0,
	            				IdTipoInstrumentoExige	: $scope.operacion.operacionSeleccionada.IdTipoInstrumento, 
	            				Cantidad			: 0,
	            				Precio				: 0,
	            				Monto 				: 0,	            				
	            			});
	            			
	            		}
	            		if (operaciones.length > 0) 
	            			$scope.formData.operacionesCobertura = operaciones;
	            		
	            	});
	
	
	//watch linea de riesgo
	$scope.$watchGroup([
		'operacion.operacionSeleccionada.IdTipoOperacion',
		'operacion.bolsaSeleccionada.IdBolsa',
		'operacion.portafolioFinanciado.IdPortafolio',
		'operacion.portafolioFinancista.IdPortafolio',
		'operacion.tasaSugerida',
		'operacion.montoReferencialVenta',
		'operacion.plazoSugerido',
		'operacion.EsOD',
		'formData.EsCuentaPropiaFinancista',


	],function(newval,oldval){
		
		var operaciones = [];
		if ($scope.operacion.operacionSeleccionada && $scope.operacion.operacionSeleccionada.IdTipoOperacion === 'COMSIM'
				&& $scope.operacion.bolsaSeleccionada && $scope.operacion.bolsaSeleccionada.IdBolsa
				&& $scope.operacion.portafolioFinancista && $scope.operacion.portafolioFinancista.IdPortafolio) {
			
			operaciones.push({
				IdPortafolio		: $scope.operacion.portafolioFinancista.IdPortafolio,
				IdTipoOperacion		: $scope.operacion.operacionSeleccionada.IdTipoOperacion,
				EsVentaCorta		: false,
				EsODCuentaPropia	: false,
				IdInstrumento		: $scope.operacion.operacionSeleccionada.IdInstrumento,
				Monto 				: $scope.operacion.montoReferencialVenta || 0,
				Tasa				: $scope.operacion.tasaSugerida || 0,
				PlazoEnDias			: $scope.operacion.plazoSugerido || 0,
				IdBolsa				: $scope.operacion.bolsaSeleccionada.IdBolsa,
				
			});
			
		}
		if ($scope.operacion.operacionSeleccionada && $scope.operacion.operacionSeleccionada.IdTipoOperacion === 'VENSIM'
				&& $scope.operacion.bolsaSeleccionada && $scope.operacion.bolsaSeleccionada.IdBolsa
				&& $scope.operacion.portafolioFinanciado && $scope.operacion.portafolioFinanciado.IdPortafolio) {
			
			operaciones.push({
				IdPortafolio		: $scope.operacion.portafolioFinanciado.IdPortafolio,
				IdTipoOperacion		: $scope.operacion.operacionSeleccionada.IdTipoOperacion,
				EsVentaCorta		: false,
				EsODCuentaPropia	: $scope.operacion.EsOD && $scope.formData.EsCuentaPropiaFinancista,
				IdInstrumento		: $scope.operacion.operacionSeleccionada.IdInstrumento,
				Monto 				: 0,
				Tasa				: 0,
				PlazoEnDias			: 0,
				IdBolsa				: $scope.operacion.bolsaSeleccionada.IdBolsa,
				
			});
			
		}
		if (operaciones.length > 0) 
			$scope.formData.operacionesLineaRiesgo = operaciones;
		
	});

	$scope.$watch('operacion.FechaVencimiento',function(newval,oldval){
		if (newval)
			$scope.formData.diaVencimiento = util.dateToFormat($scope.operacion.FechaVencimiento,'dddd');
	})
	
	$scope.formData.imprimirSimulacion = function() {
		
		var obj = {};
		var ordenes = [];
		var idTipoOperacion = $scope.operacion.operacionSeleccionada.IdTipoOperacion;
		
		for (var i = 0 ; i < $scope.formData.gridVenta.dataSource.data().length ; i++) {
			var t = $scope.formData.gridVenta.dataSource.data()[i];
			if (!t.CantidadSimulacionSimultanea) continue;
			var h = {
				IdMoneda	: t.IdMonedaInstrumento,
				Instrumento : t.Instrumento,
				Cantidad : t.CantidadSimulacionSimultanea,
				Monto : t.MontoSimulacionSimultanea,
				Precio : t.Precio ,
				IdTipoInstrumento : t.IdTipoInstrumento
			}
			
			ordenes.push(h);
		}
		
		if (ordenes.length === 0 && idTipoOperacion === 'VENSIM') 
		{
			bcsComunes.msg("error","UI_ERROR","UI_DEBE_AGREGAR_OPERACIONES");
			return;
		}
		var garantias = [];
		for (var i = 0 ; i < $scope.formData.gridGarantiasCartera.dataSource.data().length ; i++) {
			var t = $scope.formData.gridGarantiasCartera.dataSource.data()[i];
			if (!t.CantidadSimulacionGarantiaCustodia) continue;
			var h = {
				IdMoneda	: t.IdMonedaInstrumento,
				Instrumento : t.Instrumento,
				Cantidad : t.CantidadSimulacionGarantiaCustodia,
				Monto : t.MontoSimulacionGarantiaCustodia,
				Precio : t.PrecioGarantiaCustodia ,
				IdTipoInstrumento : t.IdTipoInstrumento,
				Origen : $translate.instant('UI_CUSTODIA')
			}
			
			garantias.push(h);
		}
		
		for (var i = 0 ; i < $scope.formData.gridGarantiasCtaInv.dataSource.data().length ; i++) {
			var t = $scope.formData.gridGarantiasCtaInv.dataSource.data()[i];
			if (!t.MontoSimulacionGarantiaCtaInv) continue;
			var h = {
				IdMoneda	: t.IdMoneda,
				Instrumento : '',
				Cantidad : '',
				Monto : t.MontoSimulacionGarantiaCtaInv,
				Precio : '' ,
				IdTipoInstrumento : '',
				Origen : $translate.instant('UI_CUENTA_INVERSION')
			}
			
			garantias.push(h);
		}
		
		var cobros = [];
		for (var i = 0 ; i < $scope.operacion.matrizCobros.length ; i++) {
			var t = $scope.operacion.matrizCobros[i];
			var t2 = $scope.operacion.matrizMontos[t.IdCobro];
			var h = {
				IdCobro	: t.IdCobro,
				NombreCobro : t.NombreCobro,
				FormaCobro : t.FormaCobro,
				NombreFormaCobro : t.FormaCobro === 'V'?'Variable':'Fijo',
				ValorCobro : t.ValorCobro,
				IdMoneda : t.IdMoneda,
				MontoCobroMonedaOrden : t2.MontoCobroMonedaOrden,
				MontoCobroMonedaLiquidacion : t2.MontoCobroMonedaLiquidacion,				
				IdMonedaCobro : t2.IdMonedaCobro,
				IdMonedaLiquidacionOrden : t2.IdMonedaLiquidacionOrden
			}
			
			cobros.push(h);
		}

		obj.simultanea = {
				NombreCompleto              : (idTipoOperacion === 'VENSIM') ? $scope.formData.oConfiguracionSocioNegocioFinanciado.seleccionado.NombreCompleto : $scope.formData.oConfiguracionSocioNegocioFinancista.seleccionado.NombreCompleto,
				DocumentoIdentidad 			: 	(idTipoOperacion === 'VENSIM') ? $scope.formData.oConfiguracionSocioNegocioFinanciado.seleccionado.DocumentoIdentidadDefecto : $scope.formData.oConfiguracionSocioNegocioFinancista.seleccionado.DocumentoIdentidadDefecto,
				NombreBolsa					: 	$scope.operacion.bolsaSeleccionada.Nombre,
				CodigoPortafolio			:	(idTipoOperacion === 'VENSIM') ? $scope.operacion.portafolioFinanciado.CodigoPortafolio : $scope.operacion.portafolioFinancista.CodigoPortafolio,
				FechaOrden					:	util.dateInicioDia($scope.SesionFechaSistema),
				NombreTipoOperacion 		:	$scope.operacion.operacionSeleccionada.NombreTipoOperacion,
				NombreCondicionLiquidacion 	:	$scope.operacion.condicionDeLiquidacion.NombreLargo,
				Plazo						:	$scope.operacion.plazoSugerido,
				Tasa						:	$scope.operacion.tasaSugerida,
				FechaVencimiento			:	$scope.operacion.FechaVencimiento,
				ordenes 					:	ordenes,
				garantias					: 	garantias,
				cobros						:	cobros
		}
		bcsComunes.ventanaAbrir('DetalleSimultanea',obj,false,false);
		
	};
	
	$scope.formData.volver = function() {
		$scope.formData.modoIngreso = true;
		$scope.formData.Simulando = false;
		$scope.formData.cabeceraCOMSIM = undefined;
		$scope.formData.cabeceraVENSIM = undefined;
		$scope.formData.operacionesCompra = undefined;
		$scope.formData.operacionesVenta = undefined;
		$scope.formData.gridVenta.dataSource.data([]);
		$scope.formData.gridGarantiasCartera.dataSource.data([]);
		$scope.formData.gridGarantiasCtaInv.dataSource.data([]);
		$scope.formData.oConfiguracionSocioNegocioFinanciado.IdActual = false;
		$scope.formData.oConfiguracionSocioNegocioFinancista.IdActual = false;
		$scope.operacion.montoReferencialVenta = 0;
		$scope.operacion.plazoSugerido = 7;
		$scope.operacion.tasaSugerida = 0;
		$scope.operacion.tasaCorredora = 0;
		$scope.operacion.tasaMercado = 0;
		$scope.operacion.montoTotalIngresado = 0;
		$scope.operacion.montoTotalPorIngresar = 0;
		$scope.operacion.montoPlazo = 0;
		$scope.formData.porcentajeGarantia = "";
	};
	
	$scope.formData.cancelar = function() {
		
		$scope.formData.Simulando = false;
		$scope.formData.gridVenta.dataSource.data([]);
		$scope.formData.gridGarantiasCartera.dataSource.data([]);
		$scope.formData.gridGarantiasCtaInv.dataSource.data([]);
		$scope.formData.porcentajeGarantia = "";
		
	};
	

	/* MENSAJE DE CALCULO DE PORCENTAJE DE GARANTIAS */	
	function mensajePorcentajeGarantiaBcs() {
		var mensajeSalida;
		var porcentajeGarantia = 0;
		var montoOperacionesACubrir = 0;
		var montoGarantiaCoberturaEntidad = 0;
		$scope.porcentajeGarantiaStyle = {color:'red'};

		var dataGrillaGarantias = $scope.formData.gridGarantiasCartera.dataSource.data();
		for (var i = 0; i < dataGrillaGarantias.length; i++) {
			montoGarantiaCoberturaEntidad += dataGrillaGarantias[i].MontoCoberturaEntidad;
		}
		var dataGrillaOperaciones = $scope.formData.gridVenta.dataSource.data();
		for (var i = 0; i < dataGrillaOperaciones.length; i++) {
			montoOperacionesACubrir += dataGrillaOperaciones[i].MontoACubrir;
		}		
		if (montoOperacionesACubrir==0){
			mensajeSalida = porcentajeGarantia + $translate.instant('UI_GARANTIA_NO_CUBIERTA') + " " + $scope.operacion.operacionSeleccionada.IdMoneda + " " + montoGarantiaCoberturaEntidad;
		} else {
			// porcentajeGarantia = (SUM(montoCoberturaEntidad en Garantia) / SUM(montoACubrir en Operaciones) * 100)
			porcentajeGarantia = ((montoGarantiaCoberturaEntidad / montoOperacionesACubrir) * 100).toFixed(2);
			if (porcentajeGarantia<100) {
    			var montoDiferencia = montoGarantiaCoberturaEntidad - montoOperacionesACubrir;
				mensajeSalida = porcentajeGarantia + $translate.instant('UI_GARANTIA_NO_CUBIERTA') + " " + $scope.operacion.operacionSeleccionada.IdMoneda + " " + util.bcs_formato(Math.abs(montoDiferencia),'ORD','Monto',$scope.operacion.operacionSeleccionada.IdMoneda,'','').valor;
			} else {
				$scope.porcentajeGarantiaStyle = {color:'green'};
				mensajeSalida = porcentajeGarantia + "%";
			}
		}

    	return mensajeSalida;
    }

	$scope.formData.aceptarSimulacion = function() {
		bcsComunes.ventanaOverlay(idVentana,true);
		var montoDiferencia = $scope.operacion.montoReferencialVenta - $scope.operacion.montoTotalIngresado;
		
		if(montoDiferencia < 0){
			mensaje = $translate.instant('UI_PORCENTAJE_GARANTIA_BCS') + " " + mensajePorcentajeGarantiaBcs() + " " + $translate.instant('UI_CONTINUAR');
			var t = bcsAlerta.crear('PREGUNTA_SI_NO', mensaje,idVentana);
			t.then(function(result){
				if (result===OPCIONES_BCSALERTA.NO)  {
					$scope.operacion.montoReferencialVenta = $scope.operacion.montoTotalIngresado;
				}else{
					$scope.formData.aceptarSimulacionGuardar();
				}
			})
		}else{
			$scope.formData.aceptarSimulacionGuardar();
		}
		
	};	
	
	$scope.formData.aceptarSimulacionGuardar = function() {
		
		var ops = $scope.formData.gridVenta.dataSource.data();
		for (var i = 0; i < ops.length; i++) {
			if (ops[i].CantidadDisponibleSubyacente < 0) {
				bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant("UI_CANTIDAD_ORDEN_SUPERA_DISPONIBLE"));
				return;
			}
		}
		
		/*var gtias = $scope.formData.gridGarantiasCartera.dataSource.data();
		for (var i = 0; i < gtias.length; i++) {
			if (gtias[i].CantidadDisponibleGarantiaCustodia < 0) {
				bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant("UI_CANTIDAD_GARANTIA_SUPERA_DISPONIBLE"));
				return;
			}
		}*/

		var cobrosModificados = ($scope.operacion.operacionSeleccionada.IdTipoOperacion === "COMSIM")? $scope.operacion.cobrosModificadosCompra : $scope.operacion.cobrosModificadosVenta;
		var cobros = [];
		$.each(cobrosModificados,function(key,value) {
			cobros.push({ IdInterno: 0 ,IdCobro : key, ValorCobroAplicado : value  });
		});
		
		var datosIngresoOrden = {
				p_IdEmpresa					: $scope.SesionIdEmpresa, //saca de variables de entorno
				p_IdUsuario 				: $scope.SesionIdUsuario, //saca de variables de entorno
				p_IdModulo 					: 'ORD',
				p_simularOperacion 			: 'N',
				p_UsaReglasNegocio 			: 'S',
				p_Ordenes : [],
				p_CobrosModificados : cobrosModificados
				
		};
		
		var Ordenes = [];
		if ($scope.operacion.operacionSeleccionada.IdTipoOperacion === "COMSIM")
		{
			Ordenes = [{
					IdInterno :0,
				    FechaRecepcion : $scope.operacionesComun.fechaRecepcion,
					FechaVigencia  : '0001-01-01',
					FechaEmision : '0001-01-01',
					FechaOperacion : '0001-01-01', //se calcula en el server
					IdTipoVigencia : 'SH',
					Glosa : '',
					Cantidad : 1,
					IdTipoOperacion 			: $scope.operacion.operacionSeleccionada.IdTipoOperacion,
					IdCondicionDeLiquidacion	: $scope.operacion.condicionDeLiquidacion.IdCondicionDeLiquidacion,
					MontoNeto : $scope.operacion.montoReferencialVenta,
					ValorRescate : 0,
					IdBoveda : $scope.formData.oConfiguracionBoveda.seleccionado.IdBoveda,
					IdFormaCustodia : 'CUS',
					IdPorcentajeVisibilidad : 1,
					PrecioLimite : 0,
					PrecioMercado : 0,
					EsFacturaInmediata : '',
					IdTipoPrecio : 'M',
					IdPortafolio : $scope.operacion.portafolioFinancista.IdPortafolio,
					IdTipoCanalIngreso : $scope.SesionTipoCanalIngreso,
					IdTipoReajuste : '',
					IdEmisor: '',
					IdBolsa : $scope.operacion.bolsaSeleccionada.IdBolsa,
					EspecificaBolsa : 'S',
					IdMonedaLiquidacionOrden : $scope.operacionesComun.monedaLiquidacionOperacion,
					IdMonedaLiquidacionCobros : $scope.operacionesComun.monedaLiquidacionCobros,
					TipoCambio : $scope.operacionesComun.tipoCambio,
					IdInstrumento : $scope.operacion.operacionSeleccionada.IdInstrumento,
					IdInstrumentoSubyacente : 0,
					IdCondicionDeLiquidacionPlazo : 'T0',
					TasaVentaCorta : 0,
					PlazoVentaCorta : 0,
					EsVentaCorta : 'N',
					TasaSimultanea : $scope.operacion.tasaSugerida,
					PlazoSimultanea : $scope.operacion.plazoSugerido,
					PlazoIF : 0,
					TasaRFIF : 0,
					EsOD: 'N',
					EsFungible : 'N',
					MontoPlazo: 0,
					IdTipoLibroOrden : 'NORMAL',
					IdLibroOrdenes : null,
					IdCuentaCustodioPortafolio : null,
					EsPorMonto : "N",
					IdCorredorCorresponsal : null,
					IdPersonaAutorizadoDarOrdenes : null,
					IdTipoCanalDistribucion : 	'MAN',
					EsOrdenEspecial : "N",
					PagoCobroCuentaInversion : $scope.operacion.pagoCobroCuentaInversionFinancista?"S":"N",
					BaseTasa : 30,
					TipoInteres : "S",
					IdMecanismoCaptacion : $scope.operacion.mecanismoCaptacion.ValorIndicador,
					IdPreferenciaPagoPortafolio : $scope.operacion.IdPreferenciaPagoPortafolioFinancista?$scope.operacion.IdPreferenciaPagoPortafolioFinancista:null,
					EsOrdenEspecial : "N",
					IngresoDesde : "COR"
				}];				
		}
		else if ($scope.operacion.operacionSeleccionada.IdTipoOperacion === "VENSIM")
		{
			var dataGrillaOperacionesVenta = $scope.formData.gridVenta.dataSource.data();
			var idinterno = 0;
			$.each(dataGrillaOperacionesVenta,function(key,value){
				if (value.CantidadSimulacionSimultanea <= 0) return true;
				Ordenes.push({
					IdInterno :idinterno++,
				    FechaRecepcion : $scope.operacionesComun.fechaRecepcion,
					FechaVigencia  : '0001-01-01',
					FechaEmision : '0001-01-01',
					FechaOperacion : '0001-01-01', //se calcula en el server
					IdTipoVigencia : 'SH',
					Glosa : '',
					Cantidad : value.CantidadSimulacionSimultanea,
					MontoNeto : value.MontoSimulacionSimultanea,
					ValorRescate : 0,
					IdTipoOperacion 			: $scope.operacion.operacionSeleccionada.IdTipoOperacion,
					IdCondicionDeLiquidacion	: $scope.operacion.condicionDeLiquidacion.IdCondicionDeLiquidacion,
					IdBoveda	 : $scope.formData.oConfiguracionBoveda.seleccionado.IdBoveda,
					IdFormaCustodia : 'CUS',
					IdPorcentajeVisibilidad : 1,
					PrecioLimite : value.Precio,
					PrecioMercado : value.Precio,
					EsFacturaInmediata : '',
					IdTipoPrecio : 'M',
					IdPortafolio : $scope.operacion.portafolioFinanciado.IdPortafolio,
					IdTipoCanalIngreso : $scope.SesionTipoCanalIngreso,
					IdTipoReajuste : '',
					IdEmisor: '',
					IdBolsa : $scope.operacion.bolsaSeleccionada.IdBolsa,
					EspecificaBolsa : 'S',
					IdMonedaLiquidacionOrden : $scope.operacionesComun.monedaLiquidacionOperacion,
					IdMonedaLiquidacionCobros : $scope.operacionesComun.monedaLiquidacionCobros,
					TipoCambio : $scope.operacionesComun.tipoCambio,
					IdInstrumento : $scope.operacion.operacionSeleccionada.IdInstrumento,
					IdInstrumentoSubyacente : value.IdInstrumento,
					IdCondicionDeLiquidacionPlazo : 'T0',
					TasaVentaCorta : 0,
					PlazoVentaCorta : 0,
					EsVentaCorta : 'N',
					TasaSimultanea : $scope.operacion.tasaSugerida,
					PlazoSimultanea : $scope.operacion.plazoSugerido,
					PlazoIF : 0,
					TasaRFIF : 0,
					EsOD: $scope.operacion.EsOD? 'S':'N',
					MontoPlazo: value.MontoPlazo,
					EsFungible : 'N',
					IdTipoLibroOrden : 'NORMAL',
					IdLibroOrdenes : null,
					IdCuentaCustodioPortafolio : null,
					EsPorMonto : "N",
					IdCorredorCorresponsal : null,
					IdPersonaAutorizadoDarOrdenes : null,
					IdTipoCanalDistribucion : 	'MAN',
					PagoCobroCuentaInversion : $scope.operacion.pagoCobroCuentaInversionFinanciado?"S":"N",
					BaseTasa : 30,
					TipoInteres : "S",
					IdMecanismoCaptacion : $scope.operacion.mecanismoCaptacion.ValorIndicador,
					IdPreferenciaPagoPortafolio : $scope.operacion.IdPreferenciaPagoPortafolioFinanciado?$scope.operacion.IdPreferenciaPagoPortafolioFinanciado:null,
					EsOrdenEspecial : "N"
				});	
			});
			
		}

		/*if (Ordenes.length === 0) {
			bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant("UI_NO_SE_INGRESARON_ORDENES"));
			bcsComunes.ventanaOverlay(idVentana,false);
			return;
		}*/
		
		datosIngresoOrden.p_Ordenes = Ordenes;
		
		
		//GARANTIAS
		var garantias = [];
		
		var dataGrillaGarantias = $scope.formData.gridGarantiasCartera.dataSource.data();
		for (var i = 0; i < dataGrillaGarantias.length; i++) {
			if (dataGrillaGarantias[i].CantidadSimulacionGarantiaCustodia > 0) {
				
				var gar = {
					IdInstrumento 				: dataGrillaGarantias[i].IdInstrumento,
					IdInstrumentoGarantizado 	: $scope.operacion.operacionSeleccionada.IdInstrumento,
					IdCliente    				: $scope.operacion.portafolioFinanciado.IdCliente,
					IdPortafolio 				: $scope.operacion.portafolioFinanciado.IdPortafolio,
					Cantidad					: dataGrillaGarantias[i].CantidadSimulacionGarantiaCustodia,
					Monto						: dataGrillaGarantias[i].MontoSimulacionGarantiaCustodia,
					Precio						: dataGrillaGarantias[i].PrecioGarantiaCustodia,
					IdBolsa						: $scope.operacion.bolsaSeleccionada.IdBolsa,
					IdMoneda					: '',
					IdTipoGarantia				: 'CUS'
					
				};
				garantias.push(gar);
				
			}
			
		}
		
		var dataGrillaGarantiasCtaInv = $scope.formData.gridGarantiasCtaInv.dataSource.data();
		for (var i = 0; i < dataGrillaGarantiasCtaInv.length; i++) {
			if (dataGrillaGarantiasCtaInv[i].MontoSimulacionGarantiaCtaInv > 0) {
				
				var gar = {
					IdInstrumento 				: 0,
					IdInstrumentoGarantizado 	: $scope.operacion.operacionSeleccionada.IdInstrumento,
					IdCliente    				: $scope.operacion.portafolioFinanciado.IdCliente,
					IdPortafolio 				: $scope.operacion.portafolioFinanciado.IdPortafolio,
					Cantidad					: 0,
					Monto						: dataGrillaGarantiasCtaInv[i].MontoSimulacionGarantiaCtaInv,
					Precio						: 0,
					IdBolsa						: '',
					IdMoneda					: dataGrillaGarantiasCtaInv[i].IdMoneda,
					IdTipoGarantia				: 'CTA'
					
				};
				garantias.push(gar);
				
			}
			
		}
		
		datosIngresoOrden.p_Garantias = garantias;

		bcsComunes.ventanaOverlay(idVentana,true);
		bcsServicioOrden.CrearOrdenes(datosIngresoOrden).$promise.then(function(respuesta){
			bcsComunes.ventanaOverlay(idVentana,false);
			respuesta.P_OUT_ERRORESDETALLE = _.uniq(respuesta.P_OUT_ERRORESDETALLE,function(key,value){
				return key.Codigo;
			})

			respuesta.P_OUT_ADVERTENCIASDETALLE = _.uniq(respuesta.P_OUT_ADVERTENCIASDETALLE,function(key,value){
				return key.Codigo;
			})

			respuesta.P_OUT_INFORMACIONDETALLE = _.uniq(respuesta.P_OUT_INFORMACIONDETALLE,function(key,value){
				return key.Codigo;
			})
			
			if (respuesta.P_OUT_ERRORESCONTADOR > 0) {

				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_EXISTEN_PROBLEMAS_PARA_INGRESAR_OPERACION"),
					conflictos:util.parseServerResponse(respuesta)
				},idVentana);
				
			}
			else {

				$scope.formData.modoIngreso = false;
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_OPERACIONES_INGRESADAS"),
					conflictos:util.parseServerResponse(respuesta)
				},idVentana);

				//ordenes
				var ordenes = [];
				$.each(respuesta.P_OUT_RESULTADO,function(key,value) {
					ordenes.push( { 
						IdOrden							: value.IdOrden,
						FechaOperacionOrden 			: util.dateDS(value.FechaOperacionOrden,"FechaHoraSistema","CALL"),
						AccionOrden 					: value.AccionOrden,
						NombreTipoOperacion				: value.NombreTipoOperacion, 
						NombreTipoInstrumento			: value.NombreTipoInstrumento,
						Instrumento						: value.Instrumento,
						InstrumentoSubyacente			: (value.InstrumentoSubyacente)?value.InstrumentoSubyacente: "-",
						NombreCondicionDeLiquidacion	: value.NombreCondicionDeLiquidacion,
						Cantidad 						: (value.IdTipoOperacion !== 'COMSIM')? value.Cantidad : "-",
						MontoNeto 						: value.MontoNeto,
						MontoNetoPlazo					: value.MontoNetoPlazo,
						IdTipoVigenciaOrden				: value.IdipoVigenciaOrden,
						NombreTipoVigenciaOrden 		: value.NombreTipoVigenciaOrden,
						FechaVigencia			 		: value.FechaVigencia,
						Tasa			 				: Number(value.TasaSimultanea),
						Plazo					 		: value.PlazoSimultanea,
						TotalCobros 					: Number(value.TotalCobros),
						MontoTotal 						: Number(value.MontoTotal),
						EsVentaCorta 					: value.EsVentaCorta,
						CodigoPortafolio				: value.CodigoPortafolio,
						IdMonedaOrden 					: value.IdMonedaOrden,
						IdMonedaLiquidacionOrden 		: value.IdMonedaLiquidacionOrden,
						
						Cliente							: value.NombreCompleto,
						EsOD							: value.EsOD,
						
						
						CobrosOrden						: respuesta.P_OUT_COBROS
								
					} );
					$scope.formData.decimalesMonto = util.bcs_formato(0,'ORD','Monto',value.IdMonedaLiquidacionOrden,'','').cantidadDecimales,
					$scope.formData.decimalesTasa = util.bcs_formato(0,'ORD','Tasa',value.IdMonedaLiquidacionOrden,'','').cantidadDecimales,
					$scope.resumen.IdTipoOperacion = value.IdTipoOperacion;
				});

				var garantiasCustodia = [];
				var garantiasCtaInv = [];
				$.each(respuesta.P_OUT_GARANTIAS,function(key,value) {
					if (value.IdTipoGarantia === 'CUS') {
						garantiasCustodia.push({
							FolioGarantia		: value.FolioGarantia,
							Instrumento			: value.Instrumento,
							IdMonedaInstrumento	: value.IdMonedaInstrumento,
							Cantidad			: value.Cantidad,
							Monto				: value.Monto,
							IdTipoGarantia		: value.IdTipoGarantia

						});
					}
					if (value.IdTipoGarantia === 'CTA') {
						garantiasCtaInv.push({
							FolioGarantia		: value.FolioGarantia,
							Monto				: value.Monto,
							IdMoneda			: value.IdMoneda,
							IdTipoGarantia		: value.IdTipoGarantia

						});
					}
					
				});
				$scope.resumen.ordenes = ordenes;
				$scope.resumen.garantiasCustodia = garantiasCustodia;
				$scope.resumen.garantiasCtaInv = garantiasCtaInv;
				
			}
			
			
		},function(respuesta) {
			bcsComunes.ventanaOverlay(idVentana,false);
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_EXISTEN_PROBLEMAS_PARA_INGRESAR_OPERACION"),
				conflictos:util.parseServerResponse(respuesta)
			},idVentana);
			
		});		
	}
		
	/* CONSULTA Y BUSQUEDA DE TASA */
	$scope.formData.buscarTasaSugerida = function(){
		return; //COMENTADO MIENTRAS NO ARREGLEN LIMITES
		if (!$scope.operacion.operacionSeleccionada)
			return;
		
		var datos = {};
		var filtros = {};
		
		filtros.IdTipoLimite = 'LIMRVCPMARGEN';//'LIMRVCPMARGEN';
		filtros.IdTipoOperacion = $scope.operacion.operacionSeleccionada.IdTipoOperacion;//'VENSIM','COMSIM'
		filtros.IdTipoInstrumento = '';//'SIM'
		filtros.PlazoDesde = Number($scope.operacion.plazoSugerido);//11;
		filtros.MontoDesde = Number($scope.operacion.montoReferencialVenta);//6000000;
		
		datos.cantidadDecimalesLimite = 0;//cantidadDecimalesLimite;
		bcsServicioOrdenV2.ConsultarTasa(filtros).then(function(result)
		{
			if (!result)
				return;
			$.each(result.data,function(key,value){
				
				if (Number($scope.operacion.plazoSugerido) >= Number(value.PlazoDesde)
					&& Number($scope.operacion.montoReferencialVenta) >= Number(value.MontoDesde)) 
				{
					$scope.operacion.tasaMercado = Number(value.Tasa);
					$scope.operacion.tasaCorredora = Number(value.Tasa);
				}
				
			});
			
			$scope.$apply();			
		});
	};
	
	$scope.abrirConsultaTasas = function(datos) {
		
		var id1 = bcsComunes.ventanaAbrir("ConsultarTasas",{ Tasas : datos },false,true); 		
	};
	
	$scope.$watch('operacion.operacionSeleccionada.IdTipoOperacion',function(newval,oldval){
		if (newval === 'COMSIM') {
			$scope.formData.tabCobros.active = true;
			$scope.operacion.EsOD = false;
		}
	})
	
	$scope.$watchGroup(['operacion.plazoSugerido','operacion.montoReferencialVenta','operacion.tasaSugerida'],function(newval,oldval){
		$scope.formData.buscarTasaSugerida();
		var monto = 0;
		monto = bcsComunes.calcularMontoPlazo(Number($scope.operacion.montoReferencialVenta),Number($scope.operacion.plazoSugerido),Number($scope.operacion.tasaSugerida));
		$scope.operacion.montoPlazo = monto; 
		
	});
	
	/* FIN CONSULTA Y BUSQUEDA DE TASA */
	/* Fecha Plazo*/
	$scope.$watchGroup(['operacion.plazoSugerido','operacion.condicionDeLiquidacion'],function(newval,oldval){
		if ($scope.operacion.plazoSugerido && $scope.operacion.condicionDeLiquidacion) {
			var datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo : 'ORD',
					p_UsaReglasNegocio : 'S',
					p_FechaIngreso : $scope.SesionFechaSistema,
					p_IdTipoOperacion : $scope.operacion.operacionSeleccionada.IdTipoOperacion,
					P_IdCondicionDeLiquidacion : $scope.operacion.condicionDeLiquidacion.IdCondicionDeLiquidacion,
					p_IdPais : $scope.SesionIdPaisSistema,
					p_Plazo : Number($scope.operacion.plazoSugerido)
			};
		
			bcsServicioPlataforma.ConsultarFechaVencimiento(datos).then(function(result)
			{
				if (result.P_OUT_ERRORESCONTADOR === 0 && util.dateDS(result.P_OUT_FECHAVENCIMIENTO,"FechaSistema","CALL") != $scope.operacion.FechaVencimiento){
					$scope.operacion.FechaVencimiento = util.dateDS(result.P_OUT_FECHAVENCIMIENTO,"FechaSistema","CALL")
				}
				else
			    {
					$scope.operacion.FechaVencimiento = util.dateDS("0001-01-01","FechaSistema","CALL")
					console.log("ERROR: " + JSON.stringify(result));
			    }
				$scope.operacion.esHabil = result.P_OUT_ESHABIL;
				if (result.P_OUT_ESHABIL === "N"){
					bcsComunes.msg("error","Error",$translate.instant("UI_FECHA_VENCIMIENTO_INHABIL"));
					$scope.formData.plazoDiaHabil = false;
				}
				else {
					$scope.formData.plazoDiaHabil = true;
				}
			}
			,function(result)
			{
				console.log("ERROR: " + JSON.stringify(result));
			});
		
		}
	});
	/* Fin Fecha Plazo */
	$scope.formData.actualizarDisponibleBlablum = function() {
		var dataGridGta = $scope.formData.gridGarantiasCartera.dataSource.data();
		var dataGridCustodia = $scope.formData.gridVenta.dataSource.data();
		
		var CantidadSimulacionGarantiaPorNemo = {};
		var CantidadSimulacionSimultaneaPorNemo = {};

		//calcular lo asignado
		for (var i=0; i < dataGridGta.length; i++){
			CantidadSimulacionGarantiaPorNemo[dataGridGta[i].Instrumento] = dataGridGta[i].CantidadSimulacionGarantiaCustodia;
		}
		for (var i=0; i < dataGridCustodia.length; i++){
			CantidadSimulacionSimultaneaPorNemo[dataGridCustodia[i].Instrumento] = dataGridCustodia[i].CantidadSimulacionSimultanea;
		}
		
		//calcular lo disponible
		for (var i=0; i < dataGridGta.length; i++){
			dataGridGta[i].CantidadDisponibleGarantiaCustodia = dataGridGta[i].CantidadOriginalGarantiaCustodia - (CantidadSimulacionSimultaneaPorNemo[dataGridGta[i].Instrumento] || 0) - dataGridGta[i].CantidadSimulacionGarantiaCustodia;
			dataGridGta[i].CantidadDisponibleGarantiaCustodia = (dataGridGta[i].CantidadDisponibleGarantiaCustodia > 0)? dataGridGta[i].CantidadDisponibleGarantiaCustodia :0;
		}
		for (var i=0; i < dataGridCustodia.length; i++){
			dataGridCustodia[i].CantidadDisponibleSubyacente = dataGridCustodia[i].CantidadOriginalSubyacente - (CantidadSimulacionGarantiaPorNemo[dataGridCustodia[i].Instrumento] || 0) - dataGridCustodia[i].CantidadSimulacionSimultanea;
			dataGridCustodia[i].CantidadDisponibleSubyacente = (dataGridCustodia[i].CantidadDisponibleSubyacente > 0)? dataGridCustodia[i].CantidadDisponibleSubyacente :0;
		}
		
		$scope.formData.gridVenta.dataSource.sync();
		$scope.formData.gridGarantiasCartera.dataSource.sync();
	};

	//fin actualza saldo custodia gtia
	/* BUSCAR CUSTODIA Y GARANTIAS */
	$scope.formData.buscarCustodia = function() {
		
		if ($scope.operacion.esHabil === "N")
		{
			bcsComunes.msg("error","Error",$translate.instant("UI_FECHA_VENCIMIENTO_INHABIL"));
			return;
		}
		
		bcsComunes.ventanaOverlay(idVentana,true);
		
		$scope.formData.gridVenta.dataSource.removeAll();
		$scope.formData.gridGarantiasCartera.dataSource.removeAll();
		$scope.formData.gridGarantiasCtaInv.dataSource.removeAll();		
		
		var datosCustodia = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo : 'ORD',
				p_IdBolsa : $scope.operacion.bolsaSeleccionada.IdBolsa,
				p_IdPortafolio : $scope.operacion.portafolioFinanciado.IdPortafolio,
				p_plazoDias : $scope.operacion.plazoSugerido,
				p_IdBoveda : $scope.formData.oConfiguracionBoveda.seleccionado.IdBoveda,
				p_Tasa : $scope.operacion.tasaSugerida
				
		};

		/* CUSTODIA SIMULTANEABLE */
		var p1 = bcsServicioOrden.ConsultarCustodiaSimultaneable(datosCustodia).$promise;
		var result1 = undefined;
		p1.then(function(result) {
			
			$scope.formData.tabOperacionesVenta.active = true;
			result1 = result;
			
			
			var custodiaSimultaneable = [];
			for (var custodiaIndex in result.P_CUSTODIA)
			{
				var custodiaInstrumento = {};
				custodiaInstrumento.IdInterno	 					= result.P_CUSTODIA[custodiaIndex].IdInterno;
				custodiaInstrumento.IdInstrumento 					= result.P_CUSTODIA[custodiaIndex].IdInstrumento;
				custodiaInstrumento.Instrumento 					= result.P_CUSTODIA[custodiaIndex].Instrumento;
				custodiaInstrumento.IdTipoInstrumento	 			= result.P_CUSTODIA[custodiaIndex].IdTipoInstrumento;
				custodiaInstrumento.IdMonedaInstrumento 			= result.P_CUSTODIA[custodiaIndex].IdMonedaInstrumento;
				custodiaInstrumento.MontoDisponibleSubyacente 		= Number(result.P_CUSTODIA[custodiaIndex].Precio) * Number(result.P_CUSTODIA[custodiaIndex].CantidadDisponible);
				custodiaInstrumento.CantidadDisponibleSubyacente 	= Number(result.P_CUSTODIA[custodiaIndex].CantidadDisponible);
				custodiaInstrumento.CantidadOriginalSubyacente 		= Number(result.P_CUSTODIA[custodiaIndex].CantidadDisponible);
				custodiaInstrumento.ValorClasificacionRiesgo 		= result.P_CUSTODIA[custodiaIndex].ValorClasificacionRiesgo;
				custodiaInstrumento.CantidadSimulacionSimultanea 	= 0;
				custodiaInstrumento.Tasa 							= $scope.operacion.tasaSugerida;
				custodiaInstrumento.Precio 							= Number(result.P_CUSTODIA[custodiaIndex].Precio);//1000;				
				custodiaInstrumento.Plazo 							= $scope.operacion.plazoSugerido;
				custodiaInstrumento.MontoSimulacionSimultanea 		= 0;
				custodiaInstrumento.MontoAVenderSimulado            = 0;
				custodiaInstrumento.MontoPlazo 						= 0;
				custodiaInstrumento.MontoExigibilidadCorr 			= 0;
				custodiaInstrumento.PorcExigibilidadCorr 			= 0;
				custodiaInstrumento.MontoExigibilidadEnt 			= 0;				
				custodiaInstrumento.PorcExigibilidadEnt 			= 0;
				custodiaInstrumento.MontoCobro 			 			= 0;
				custodiaInstrumento.PrecioVaR						= Number(result.P_CUSTODIA[custodiaIndex].PrecioVaR);
				custodiaInstrumento.PorcentajeVaR					= Number(result.P_CUSTODIA[custodiaIndex].PorcentajeVaR);
				custodiaSimultaneable.push(custodiaInstrumento);
			}
			
			$scope.formData.gridVenta.dataSource.data(custodiaSimultaneable);					
			
		});	
		
		/* CUSTODIA GARANTIZABLE */
		
		var datosGarantia = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo : 'ORD',
				p_IdCliente : $scope.operacion.portafolioFinanciado.IdCliente,
				p_IdPortafolio :$scope.operacion.portafolioFinanciado.IdPortafolio,
				p_IdBolsa : $scope.operacion.bolsaSeleccionada.IdBolsa,
				p_DiasLiquidacion : 0,	
				p_IdInstrumentoAGarantizar : $scope.operacion.operacionSeleccionada.IdInstrumento,
				p_IdEntidad						: "BCS",					//nuevo a enviar de la interfaz
				p_IdTipoInstrumentoAGarantizar	: $scope.operacion.operacionSeleccionada.IdTipoInstrumento,	//nuevo a enviar de la interfaz
				p_IdTipoOperacion				: $scope.operacion.operacionSeleccionada.IdTipoOperacion,				//nuevo a enviar de la interfaz
				p_plazoDias : $scope.operacion.plazoSugerido,
				p_Tasa : $scope.operacion.tasaSugerida
				

		};
		
		var p2 = bcsServicioOrden.ConsultarCustodiaGarantizableSimultanea(datosGarantia).$promise;
		p2.then(function(result) {
		
			var custodiaGarantizable = [];
			for (var custodiaIndex in result.P_OUT_CUSTODIAGARANTIA)
			{
				var custodiaInstrumento = {};
				custodiaInstrumento.IdInstrumento 						= result.P_OUT_CUSTODIAGARANTIA[custodiaIndex].IdInstrumento
				custodiaInstrumento.Instrumento 						= result.P_OUT_CUSTODIAGARANTIA[custodiaIndex].Instrumento
				custodiaInstrumento.IdTipoInstrumento 					= result.P_OUT_CUSTODIAGARANTIA[custodiaIndex].IdTipoInstrumento
				custodiaInstrumento.IdMonedaInstrumento 				= result.P_OUT_CUSTODIAGARANTIA[custodiaIndex].IdMonedaInstrumento
				custodiaInstrumento.MontoDisponibleGarantiaCustodia 	= Number(result.P_OUT_CUSTODIAGARANTIA[custodiaIndex].Precio) * Number(result.P_OUT_CUSTODIAGARANTIA[custodiaIndex].CantidadDisponibleGarantia);
				custodiaInstrumento.CantidadDisponibleGarantiaCustodia 	= Number(result.P_OUT_CUSTODIAGARANTIA[custodiaIndex].CantidadDisponibleGarantia);
				custodiaInstrumento.CantidadOriginalGarantiaCustodia 	= Number(result.P_OUT_CUSTODIAGARANTIA[custodiaIndex].CantidadDisponibleGarantia);
				custodiaInstrumento.CantidadSimulacionGarantiaCustodia 	= 0;
				custodiaInstrumento.PrecioGarantiaCustodia 				= Number(result.P_OUT_CUSTODIAGARANTIA[custodiaIndex].Precio);
				custodiaInstrumento.MontoSimulacionGarantiaCustodia 	= 0;
				custodiaInstrumento.ValorClasificacionRiesgo 			= result.P_OUT_CUSTODIAGARANTIA[custodiaIndex].ValorClasificacionRiesgo;
				custodiaInstrumento.IdTipoReajuste 						= result.P_OUT_CUSTODIAGARANTIA[custodiaIndex].IdTipoReajuste;
				custodiaInstrumento.IdEmisor 							= result.P_OUT_CUSTODIAGARANTIA[custodiaIndex].IdEmisor;
				custodiaInstrumento.FechaEmision 						= result.P_OUT_CUSTODIAGARANTIA[custodiaIndex].FechaEmisionInstrumento;
				custodiaInstrumento.FechaVencimiento 					= result.P_OUT_CUSTODIAGARANTIA[custodiaIndex].FechaVencimientoInstrumento;
				custodiaInstrumento.EsFungible 							= result.P_OUT_CUSTODIAGARANTIA[custodiaIndex].EsFungible;
				custodiaInstrumento.NemoSVS 							= result.P_OUT_CUSTODIAGARANTIA[custodiaIndex].NemoSVS;
				custodiaInstrumento.PrecioVaR							= Number(result.P_OUT_CUSTODIAGARANTIA[custodiaIndex].PrecioVaR);
				custodiaInstrumento.PorcentajeVaR						= Number(result.P_OUT_CUSTODIAGARANTIA[custodiaIndex].PorcentajeVaR);

				custodiaInstrumento.PorcentajeCoberturaEntidad = 0;
				custodiaInstrumento.PorcentajeCoberturaCorredora = 0;
				
				custodiaInstrumento.MontoCoberturaEntidad = 0;
				custodiaInstrumento.MontoCoberturaCorredora = 0;
				custodiaInstrumento.MontoCoberturaCorredora = 0;
				custodiaInstrumento.PrecioVaR 	= Number(result.P_OUT_CUSTODIAGARANTIA[custodiaIndex].PrecioVaR)
				
				
				custodiaGarantizable.push(custodiaInstrumento);
			}
			
			$scope.formData.gridGarantiasCartera.dataSource.data(custodiaGarantizable);					
		});
	
		/* CTA INVERSION GARANTIZABLE */
		var datosCtaInvGarantia = {
			p_IdEmpresa: $scope.SesionIdEmpresa,
			p_IdUsuario: $scope.SesionIdUsuario,
			p_IdModulo: 'CTA',
			p_UsaReglasNegocio : 'N',
			p_Uso : 'CUENTA_INVERSION',
			p_PortafolioAConsultar : [{
				"IdPortafolio" 	: $scope.operacion.portafolioFinanciado.IdPortafolio,
				"IdMoneda"		: null
			}],

			p_FechaDesde : $scope.SesionFechaSistema,
			p_FechaHasta : $scope.SesionFechaSistema
		};
	
		var p3 = bcsServicioOrden.ConsultarCtaInversionGarantia(datosCtaInvGarantia).$promise;
		p3.then(function(result) {
		
			var ctaInversionFinanciado = [];
			for (var ctainvIndex in result.P_OUT_FLUJOCAJA)
			{
				var ctaInvMoneda = result.P_OUT_FLUJOCAJA[ctainvIndex];
				ctaInvMoneda.MontoSimulacionGarantiaCtaInv = 0;
				
				
				ctaInvMoneda.MontoCoberturaEntidad = 0;
				ctaInvMoneda.PorcentajeCoberturaEntidad = 0;
				ctaInvMoneda.MontoCoberturaCorredora = 0;
				ctaInvMoneda.PorcentajeCoberturaCorredora = 0;
				
				ctaInversionFinanciado.push(ctaInvMoneda);
				
			}
			$scope.formData.gridGarantiasCtaInv.dataSource.data(ctaInversionFinanciado)	
		});
		
		//$q.all([p1,p2,p3]).then(function(result) {
		$q.all([p1]).then(function(result) {
			$scope.formData.Simulando = true;
			bcsComunes.ventanaOverlay(idVentana,false);
			
			if ($scope.formData.gridVenta.dataSource.data().length === 0) {
				bcsComunes.ventanaMsg({
					mensaje: $translate.instant("UI_NO_EXISTEN_OPERCIONES"),
					conflictos : util.parseServerResponse(result1)
				},idVentana);
			}
			
			
		} , function(result) {
			$scope.formData.Simulando = true;
			bcsComunes.ventanaOverlay(idVentana,false);
		} );
			
		
	};
	/* FIN BUSCAR CUSTODIA Y GARANTIAS */
	
	
	/* OTROS */
	$scope.refreshGrids = function(gridArray) {
		$timeout(function() {
			$scope.formData.gridGarantiasCtaInv.refresh();
			$scope.formData.gridGarantiasCartera.refresh();
			$scope.formData.gridVenta.refresh();
		},50);
	};
	/* FIN OTROS*/
	
	/* SETEO INICIAL */
	
	$scope.operacion = { //GUARDA DATOS DE LA OPERACION
			montoReferencialVenta : "0",
			plazoSugerido : "7",//debe ser configurable por un parametro de la simultanea
			tasaSugerida : "0",
			tasaCorredora : "0",
			tasaMercado : "0",
			montoTotalIngresado : 0,
			montoTotalPorIngresar : 0,
			montoPlazo : 0
	};
	
	$scope.formData.modalidad = "Normal";
	$scope.formData.comboBolsa={
		filtros : {
			IdTipoInstrumento : { "field"   : "toupper(IdTipoInstrumento)","operator": "eq", "value"   : "SIM" },
		}
	};
	
	$scope.formData.oConfiguracionCondicionLiquidacion={
		filtros : {
			IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   : "" },
			EstadoTipoOperacionYCondicionDeLiquidacion : { "field"   : "EstadoTipoOperacionYCondicionDeLiquidacion","operator": "eq", "value"   : "ACT" }
		},
	};

	$scope.formData.oConfiguracionSocioNegocioFinanciado={
		filtros: {
			IdEmpresa : {"field" : "IdEmpresa", "operator" : "eq", "value" : $scope.SesionIdEmpresa},
			EstadoCliente : { "field"   : "EstadoCliente","operator": "eq", "value"   :  'ACT' },
			EsCliente : {"field"   : "EsCliente","operator": "eq", "value"   :  'UI_SI' }
		},
	};
	$scope.formData.oConfiguracionPortafolioFinanciado={
		filtros: {
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  0 }
		},
	};
	
	$scope.select = function(grid)
	{
		$timeout(function() {
			if (Array.isArray(grid)) {
				$.each(grid,function(key,value) {
					if ($("#" + value).find("[kendo-grid]").data("kendo-grid"))
						$("#" + value).find("[kendo-grid]").data("kendo-grid").refresh()
				})
			}
			else
				if ($("#" + grid).find("[kendo-grid]").data("kendo-grid"))
					$("#" + grid).find("[kendo-grid]").data("kendo-grid").refresh()
		},50);
	};
	
	$scope.formData.oConfiguracionSocioNegocioFinancista={
		filtros: {
			IdEmpresa : {"field" : "IdEmpresa", "operator" : "eq", "value" : $scope.SesionIdEmpresa},
			EstadoCliente : { "field"   : "EstadoCliente","operator": "eq", "value"   :  'ACT' },
			EsCliente : {"field"   : "EsCliente","operator": "eq", "value"   :  'UI_SI' }
		},
	};
	$scope.formData.oConfiguracionPortafolioFinancista={
			filtros: {
				IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  0 }
			},
		};
	
	$scope.formData.configuracionMecanismoCaptacion ={
			filtros: {
				IdPortafolio : { "field"   : "CodigoTipoIndicador","operator": "eq", "value"   :  "ORDMODALIDAD" },
				IdEntidad    : { "field" : "IdEntidad", "operator" : "eq", "value": "" }
			},
		};
	
	$scope.formData.configuracionPreferenciaPagoPortafolioFinancista = {
			filtros : {
				IdMedioDePago : { "field"   : "IdMedioDePago",operator: "neq", "value"   :  "CIN" },
				IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  "" },
				IdMoneda : { "field"   : "IdMoneda","operator": "eq", "value"   :  $scope.SesionIdMonedaSistema },
				IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   : "" }
			}
		}
	
	$scope.formData.configuracionPreferenciaPagoPortafolioFinanciado = {
		filtros : {
			IdMedioDePago : { "field"   : "IdMedioDePago",operator: "neq", "value"   :  "CIN" },
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  "" },
			IdMoneda : { "field"   : "IdMoneda","operator": "eq", "value"   :  $scope.SesionIdMonedaSistema },
			IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   :  "" }
		}
	}

	$scope.formData.comboTipoSimultanea={
		filtros : {
			IdTipoInstrumento : { "field"   : "toupper(IdTipoInstrumento)","operator": "eq", "value"   : "SIM" },
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	$scope.formData.oConfiguracionBoveda={
			tipo : "combobox-boveda",
			configuracion : {},
			filtros : { 	
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "VIG" }
			},
			idActual : undefined,
			seleccionado : undefined,
			idDefecto : ''
	};
	
	$scope.$watch('operacion.pagoCobroCuentaInversionFinancista',function(newval,oldva){
   		if (newval === true)
   			$scope.operacion.IdPreferenciaPagoPortafolioFinancista = false;
   	});
	
	$scope.$watch('operacion.pagoCobroCuentaInversionFinanciado',function(newval,oldva){
   		if (newval === true)
   			$scope.operacion.IdPreferenciaPagoPortafolioFinanciado = false;
   	});
   	
	$scope.$watch('operacion.operacionesCobrosSalida',function(newval,oldval){
   		if ($scope.operacion.operacionesCobrosSalida && $scope.operacion.operacionesCobrosSalida.length > 0 && newval != oldval){
   			var dataGridCustodia = $scope.formData.gridVenta.dataSource.data();
   			for (var i=0; i < dataGridCustodia.length; i++){
   				encuentraCobro = false;
   				for (var j=0; j < $scope.operacion.operacionesCobrosSalida.length; j++){
					if (dataGridCustodia[i].IdInterno === $scope.operacion.operacionesCobrosSalida[j].idInterno){
						dataGridCustodia[i].MontoCobro = $scope.operacion.operacionesCobrosSalida[j].totalCobros;
						encuentraCobro = true;
					}	
					if (!encuentraCobro) dataGridCustodia[i].MontoCobro = 0;
   				}
			}
   			$scope.formData.gridVenta.dataSource.sync();
   		}
   	});
	
	var conf11 = bcsConfiguracionOrdenesV2.getConfiguracionGridCustodiaSimultaneable();
	conf11.save = function(e) {
		
		var dataGridGta = $scope.formData.gridGarantiasCartera.dataSource.data();
		var dataGridCustodia = $scope.formData.gridVenta.dataSource.data();

		var CantidadSimulacionGarantiaPorNemo = {};
		var CantidadSimulacionSimultaneaPorNemo = {};

		//calcular lo asignado
		for (var i=0; i < dataGridGta.length; i++){
			CantidadSimulacionGarantiaPorNemo[dataGridGta[i].Instrumento] = dataGridGta[i].CantidadSimulacionGarantiaCustodia;
		}
		for (var i=0; i < dataGridCustodia.length; i++){
			CantidadSimulacionSimultaneaPorNemo[dataGridCustodia[i].Instrumento] = dataGridCustodia[i].CantidadSimulacionSimultanea;
		}

		if (e.values.CantidadSimulacionSimultanea > (e.model.CantidadOriginalSubyacente - (CantidadSimulacionGarantiaPorNemo[e.model.Instrumento] || 0)))
			e.values.CantidadSimulacionSimultanea = e.model.CantidadOriginalSubyacente - (CantidadSimulacionGarantiaPorNemo[e.model.Instrumento] || 0);
		if (e.values.CantidadSimulacionSimultanea < 0)
			e.values.CantidadSimulacionSimultanea = 0;
        if (e.values.MontoAVenderSimulado > 0 && e.model.Precio > 0) {
        	e.model.MontoAVenderSimulado = e.values.MontoAVenderSimulado;
            e.model.CantidadSimulacionSimultanea = Math.round((e.model.MontoAVenderSimulado/e.model.Precio)+(1/2));
        } else {
            e.model.CantidadSimulacionSimultanea = e.values.CantidadSimulacionSimultanea;
			e.model.MontoAVenderSimulado = Math.round((e.model.CantidadSimulacionSimultanea*e.model.Precio)+(1/2));
        }
		
		e.model.MontoSimulacionSimultanea = e.model.Precio*e.model.CantidadSimulacionSimultanea;
		e.model.MontoPlazo = bcsComunes.calcularMontoPlazo(e.model.MontoSimulacionSimultanea,e.model.Plazo,e.model.Tasa);
		e.model.MontoACubrir = (e.model.MontoPlazo.toFixed(0)*(e.model.PorcentajeVaR.toFixed(4)/100)).toFixed(0);
		
		$scope.updateOperacionesSimultaneaVenta();
		
		var datosExig = {
			p_IdEmpresa				: $scope.SesionIdEmpresa,
			p_IdUsuario				: $scope.SesionIdUsuario,
			p_IdModulo				: 'ORD',
			p_simularOperacion		: 'N',
			p_UsaReglasNegocio		: 'S',
			p_TableExigibilidadSimulacion:[],
			p_EsVentaCorta 			: 'N',
			p_IdTipoInstrumento		: $scope.operacion.operacionSeleccionada.IdTipoInstrumento,
			p_IdInstrumento			: $scope.operacion.operacionSeleccionada.IdInstrumento,
			p_IdPortafolio			: $scope.operacion.portafolioFinanciado.IdPortafolio,
			p_IdBolsa				: $scope.operacion.bolsaSeleccionada.IdBolsa    						
		};
		
		/* CALCULO DEL MONTO POR INGRESAR */
		var d = e.sender.dataSource.data();
		var val = 0;
		$.each(d,function(key,value) {
			val+=value.MontoSimulacionSimultanea;
		});
		$scope.operacion.montoTotalIngresado = val;
		$scope.operacion.montoTotalPorIngresar = $scope.operacion.montoReferencialVenta - $scope.operacion.montoTotalIngresado>=0? $scope.operacion.montoReferencialVenta - $scope.operacion.montoTotalIngresado:0;
			
		//update exigibilidad
		var operaciones = [];
		for (var i = 0;i< e.sender.dataSource.data().length;i++)
		{
			if (e.sender.dataSource.data()[i].MontoPlazo === 0) continue;
			operaciones.push({
				EsVentaCorta		: false,
				IdTipoInstrumento	: $scope.operacion.operacionSeleccionada.IdTipoInstrumento,
				IdBolsa				: $scope.operacion.bolsaSeleccionada.IdBolsa,
				IdPortafolio		: $scope.operacion.portafolioFinanciado.IdPortafolio,
				IdInstrumento		: e.sender.dataSource.data()[i].IdInstrumento,
				IdInstrumentoExige	: $scope.operacion.operacionSeleccionada.IdInstrumento,
				Cantidad			: 0,
				Precio				: 0,
				Monto 				: e.sender.dataSource.data()[i].MontoPlazo,	
				
				
				
			});
		}
		if (operaciones.length > 0) {
			$scope.formData.operacionesExigibilidad = operaciones;
		}
		
		//update linea de riesgo
		var operaciones = [];
		var sumaMonto = 0;
		for (var i = 0;i<e.sender.dataSource.data().length;i++)
		{
			sumaMonto+=e.sender.dataSource.data()[i].MontoPlazo;
		}
		if (sumaMonto > 0) {
			operaciones.push({
				IdPortafolio		: $scope.operacion.portafolioFinanciado.IdPortafolio,
				IdTipoOperacion		: $scope.operacion.operacionSeleccionada.IdTipoOperacion,
				EsVentaCorta		: false,
				EsODCuentaPropia	: $scope.operacion.EsOD && $scope.formData.EsCuentaPropiaFinancista,
				IdInstrumento		: $scope.operacion.operacionSeleccionada.IdInstrumento,
				Monto 				: sumaMonto || 0,
				Tasa				: $scope.operacion.tasaSugerida || 0,
				PlazoEnDias			: $scope.operacion.plazoSugerido || 0,
				IdBolsa				: $scope.operacion.bolsaSeleccionada.IdBolsa
				
			});
			$scope.formData.operacionesLineaRiesgo = operaciones;

		}

		
		//
		for (var i = 0;i<e.sender.dataSource.data().length;i++)
		{
			datosExig.p_TableExigibilidadSimulacion.push(
			{
				IdTipoInstrumento : 'SIM', 
				IdInstrumento: e.sender.dataSource.data()[i].IdInstrumento, 
				IdInterno : i,
				Cantidad : 0, 
				Precio : 0, 
				Monto: e.sender.dataSource.data()[i].MontoPlazo, 
				IdBolsa: $scope.operacion.bolsaSeleccionada.IdBolsa
			});
		}
		
		bcsComunes.ventanaOverlay(idVentana,true);
		bcsServicioOrden.CalcularExigibilidadOrdenes(datosExig).$promise.then(function(result){
		//bcsServicioPlataforma.Servicios().CalcularExigibilidad(datosExig).$promise.then(function(result){
			var exigSimEntidad = 0;
			var exigSimCorredora = 0;
		
			$.each(result.p_out_TableExigibilidadSimulacion,function(key,value) {
				
				var op = _.find(e.sender.dataSource.data(),function(operacion){
					return operacion.IdInstrumento === value.IdInstrumento
				});
				if (op)
				{
					op.PorcExigibilidadEnt=Number(value.PorcentajeExigibilidadEntidad);
					op.MontoExigibilidadCorr=Number(value.MontoExigibilidadCorredora);
					op.PorcExigibilidadCorr=Number(value.PorcentajeExigibilidadCorredora);
					op.MontoExigibilidadEnt=Number(value.MontoExigibilidadEntidad);
				};
			});
			$scope.formData.porcentajeGarantia = mensajePorcentajeGarantiaBcs();
			
			/* FIN BUSQUEDA DE EXIGIBILIDAD DE LA OPERACION */
			return true;
			
		},function(result){
		}).then(function(){
			//FIN
			e.sender.refresh();
			e.preventDefault();
			$scope.formData.actualizarDisponibleBlablum();
			bcsComunes.ventanaOverlay(idVentana,false);
		},function(){
			//FIN
			e.sender.refresh();
			e.preventDefault();
			$scope.formData.actualizarDisponibleBlablum();
			bcsComunes.ventanaOverlay(idVentana,false);
		});

		e.preventDefault();
		$scope.formData.actualizarDisponibleBlablum();
	};

	$scope.formData.optionsGrillaVenta = conf11;


	var conf22 = bcsConfiguracionOrdenesV2.getConfiguracionGridCustodiaGarantizable();


	conf22.save = function(e) {
		
		var dataGridGta = $scope.formData.gridGarantiasCartera.dataSource.data();
		var dataGridCustodia = $scope.formData.gridVenta.dataSource.data();

		var CantidadSimulacionGarantiaPorNemo = {};
		var CantidadSimulacionSimultaneaPorNemo = {};

		//calcular lo asignado
		for (var i=0; i < dataGridGta.length; i++){
			CantidadSimulacionGarantiaPorNemo[dataGridGta[i].Instrumento] = dataGridGta[i].CantidadSimulacionGarantiaCustodia;
		}
		for (var i=0; i < dataGridCustodia.length; i++){
			CantidadSimulacionSimultaneaPorNemo[dataGridCustodia[i].Instrumento] = dataGridCustodia[i].CantidadSimulacionSimultanea;
		}

		if (e.values.CantidadSimulacionGarantiaCustodia > (e.model.CantidadOriginalGarantiaCustodia - (CantidadSimulacionSimultaneaPorNemo[e.model.Instrumento] || 0)))
			e.values.CantidadSimulacionGarantiaCustodia = e.model.CantidadOriginalGarantiaCustodia - (CantidadSimulacionSimultaneaPorNemo[e.model.Instrumento] || 0);
		if (e.values.CantidadSimulacionGarantiaCustodia < 0)
			e.values.CantidadSimulacionGarantiaCustodia = 0;

		if (e.values.CantidadSimulacionGarantiaCustodia != undefined){
			e.model.CantidadSimulacionGarantiaCustodia = e.values.CantidadSimulacionGarantiaCustodia;
			e.model.MontoSimulacionGarantiaCustodia = e.model.PrecioGarantiaCustodia*e.model.CantidadSimulacionGarantiaCustodia;
			e.model.MontoCoberturaSimular = e.model.PrecioVaR*e.model.CantidadSimulacionGarantiaCustodia;
			
		}
		
		if(e.values.MontoCoberturaSimular > e.model.MontoDisponibleGarantiaCustodia)
			e.values.MontoCoberturaSimular = e.model.MontoDisponibleGarantiaCustodia;
		
		if (e.values.MontoCoberturaSimular != undefined){
			e.model.MontoCoberturaSimular = e.values.MontoCoberturaSimular;
			e.model.MontoSimulacionGarantiaCustodia = e.model.MontoCoberturaSimular;
			e.model.CantidadSimulacionGarantiaCustodia = e.model.MontoCoberturaSimular/e.model.PrecioGarantiaCustodia;
		}
		
        if (e.values.MontoCoberturaSimular == undefined && e.values.CantidadSimulacionGarantiaCustodia == undefined){
			e.model.MontoCoberturaSimular = 0;
			e.model.MontoSimulacionGarantiaCustodia = 0;
			e.model.CantidadSimulacionGarantiaCustodia = 0;        	
        }
        
		//update cobertura
		var operaciones = [];
		for (var i = 0;i< e.sender.dataSource.data().length;i++)
		{
			if (e.sender.dataSource.data()[i].MontoSimulacionGarantiaCustodia === 0) continue;
			operaciones.push({
				EsVentaCorta		: false,
				IdBolsa				: $scope.operacion.bolsaSeleccionada.IdBolsa,
				IdPortafolio		: $scope.operacion.portafolioFinanciado.IdPortafolio,
				IdInstrumento		: e.sender.dataSource.data()[i].IdInstrumento,
				IdInstrumentoExige	: $scope.operacion.operacionSeleccionada.IdInstrumento,
				IdTipoInstrumentoExige	: $scope.operacion.operacionSeleccionada.IdTipoInstrumento, 
				Cantidad			: 0,
				Precio				: 0,
				Monto 				: e.sender.dataSource.data()[i].MontoSimulacionGarantiaCustodia,
			});
		}
		if (operaciones.length > 0) {
			$scope.formData.operacionesCobertura = operaciones;
		}
				
		/* CALCULO DE COBERTURAS DE OPERACIONES */	  			
		var datosCobertura = {
				p_EsVentaCorta 		: 'N',
				p_UsaReglasNegocio 	: 'S',
				p_IdBolsa 			: $scope.operacion.bolsaSeleccionada.IdBolsa,
				p_IdEmpresa			: $scope.SesionIdEmpresa,
				p_IdInstrumento		: $scope.operacion.operacionSeleccionada.IdInstrumento,
				p_IdModulo			: 'ORD',
				p_IdPortafolio		: $scope.operacion.portafolioFinanciado.IdPortafolio,
				p_IdTipoInstrumento	: $scope.operacion.operacionSeleccionada.IdTipoInstrumento,
				p_IdUsuario			: $scope.SesionIdUsuario,
				p_Simulacion: [] 						
		};
	
		for (var i = 0;i<e.sender.dataSource.data().length;i++)
		{
			datosCobertura.p_Simulacion.push(
			{
				IdInstrumento: e.sender.dataSource.data()[i].IdInstrumento,
				Cantidad : 0, 
				PrecioMercado : 0, 
				Monto: e.sender.dataSource.data()[i].MontoSimulacionGarantiaCustodia, 
			});
		}

		//var p = bcsServicioPlataforma.Servicios().CalcularCobertura(datosCobertura).$promise;
		var p = bcsServicioOrden.CalcularCoberturaOrdenes(datosCobertura).$promise;
		p.then(function(result){

			$.each(result.p_out_TableCoberturaSimulacion,function(key,value) {
				
				var op = _.find(e.sender.dataSource.data(),function(operacion){
					return operacion.IdInstrumento == value.IdInstrumento
				});
				if (op)
				{							
					op.PorcentajeCoberturaEntidad=Number(value.PorcentajeCoberturaEntidad);
					op.MontoCoberturaEntidad=Number(value.MontoCoberturaEntidad);
					op.PorcentajeCoberturaCorredora=Number(value.PorcentajeCoberturaCorredora);
					op.MontoCoberturaCorredora=Number(value.MontoCoberturaCorredora);
				};
				
				
			});
			
			$scope.formData.porcentajeGarantia = mensajePorcentajeGarantiaBcs();
			
			/* FIN CALCULO DE COBERTURAS DE OPERACIONES */ 
			return true;
			
		},function(result){
		
		}).then(function(){
			e.sender.refresh();
			e.preventDefault();
			$scope.formData.actualizarDisponibleBlablum();
			bcsComunes.ventanaOverlay(idVentana,false);
		});
		
		e.preventDefault();
		$scope.formData.actualizarDisponibleBlablum();
  	};
	
	$scope.formData.optionsGrillaGtiaCartera = conf22;
	
	var conf33 = bcsConfiguracionOrdenesV2.getConfiguracionGridCtaInvGarantizable();
	
	$scope.formData.optionsGrillaGtiaCtaInv = conf33;
	
	//tabs
	$scope.formData.tabCobros = {
			disabled : false,
			active: false
	};
	$scope.tabGarantias = {
			disabled : false,
			active: false
	};
	$scope.tabOperacionesVenta = {
			disabled : false,
			active: true
	};
	
	$scope.operacionesComun = {
			canal : $scope.SesionTipoCanalIngreso,
			monedaLiquidacionOperacion : $scope.SesionIdMonedaSistema,
			monedaLiquidacionCobros : $scope.SesionIdMonedaSistema,
			porMontoCompraSimultanea : true,
			porMontoVentaSimultanea : false,
			tipoCambio : 0,
			fechaRecepcion : util.dateSPseconddate(util.date($scope.SesionFechaSistema).hour(util.date(new Date()).hour()).minute(util.date(new Date()).minute()).second(util.date(new Date()).second()).toDate())
		
	};
	$scope.formData.montoDecimales = util.bcs_formato(0,'ORD','Monto',$scope.SesionIdMonedaSistema,'').cantidadDecimales;
	$scope.formData.tasaDecimales = util.bcs_formato(0,'ORD','Tasa',$scope.SesionIdMonedaSistema,'').cantidadDecimales;
	/* FIN SETEO INICIAL */
	
	

	$scope.$watch('operacion.portafolioFinanciado',function(newval,oldval){
			if (newval)
			{	
				bcsConfiguracionOrdenes.FUNCIONES.getInformacionPortafolios([$scope.operacion.portafolioFinanciado.IdPortafolio]).then(function(result){
					var indicadores = result[0];
					
					if (indicadores.OperaCuentaInversion === "S") 
					{	
						$scope.operacion.pagoCobroCuentaInversionFinanciado = true;
					}
					else 
					{	
						$scope.operacion.pagoCobroCuentaInversionFinanciado = false;
					}
					
				});
			}
		}
	);
	

	$scope.$watch('operacion.portafolioFinancista',function(newval,oldval){
				if (newval)
				{
					bcsConfiguracionOrdenes.FUNCIONES.getInformacionPortafolios([$scope.operacion.portafolioFinancista.IdPortafolio]).then(function(result){
						var indicadores = result[0];
						
						if (indicadores.OperaCuentaInversion === "S") 
						{	
							$scope.operacion.pagoCobroCuentaInversionFinancista = true;
						}
						else 
						{	
							$scope.operacion.pagoCobroCuentaInversionFinancista = false;
						}
						
					});
				}
			}
		);
	
	$scope.tabClick = function (){
			var ventana = $("[ventanaid=" + idVentana +"]").closest(".k-window");
			ventana.css('height', 'auto');
		}
	
	$scope.$watchGroup(['operacion.EsOD','operacion.operacionSeleccionada'],function(newval,oldval){
		var ventana = $("[ventanaid=" + idVentana +"]").closest(".k-window");
		ventana.css('height', 'auto');	
		});
	
});