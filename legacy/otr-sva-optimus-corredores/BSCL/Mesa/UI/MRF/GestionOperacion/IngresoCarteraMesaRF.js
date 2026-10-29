angular.module('ModuloMesa').controller('IngresoCarteraMesaRFCtrl',
	function ($scope,$translate,$timeout,$rootScope,
				bcsComunes,
				bcsServiciosMesa,
				bcsServiciosGrillasMesaRF,
				bcsServicioPlataforma) {
	/****************************************
	 *
	 * Declaraciones
	 *
	 * **************************************/
	//Obtiene los parametros pasado a la ventana
	$scope.dataNominal = {};
	$scope.IdVentana = bcsComunes.ventanaWinId ($scope); 
	bcsComunes.ventanaAutoHeight($scope.IdVentana);
	$scope.dato = bcsComunes.ventanaParams($scope.IdVentana);
	var fechaSistema = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
	$scope.dataNominal.valorizar = true;
	var fechaLiquidacionOperacion = $scope.dato.FechaLiquidacionOperacion;
	var fechaVencimiento = new Date();//($scope.dato.FechaLiquidacionOperacion).getDate()+1;
	$scope.dataNominal.origenCompraConPacto = false;
	fechaVencimiento.setDate((fechaLiquidacionOperacion).getDate()+1);
	$scope.dataNominal.muestraDatosRF = false;
	$scope.dataNominal.PrimeraValorizacion = true;
	
	// Fecha Anterior para la paridad USD
	var fechaHabilAnterior = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");	
	for( i=0; i<10; i++ ) {
		fechaHabilAnterior = util.date(fechaHabilAnterior).startOf('day').add(-1,'d').toDate();
		if ( util.esFechaHabil( $scope.$root.SesionIdPaisSistema, fechaHabilAnterior ) ) {
			i == 10;
		}
	}


	// 0 - Nominal/Cantidad + Tasa => Mto. Valorizado
	// 1 - Nominal/Cantidad + Mto. Valorizado => Tasa
	// 2 - Mto. Valorizado + Tasa => Nominal/Cantidad
	$scope.TipoIncognita = 0;
	$scope.MontoEquivalente = 0;
	$scope.MontoEquivalenteUltimo = 0;
	$scope.IdMonedaOrigen = "";
	$scope.IdMonedaDestino = "";
	$scope.CantidadCartera = 0;
	$scope.ValorRescateCartera = 0;
	$scope.MontoAValorizar = 0;
	
	
	// Valorizador solo soporta 2 decimales en precios y tasas
	$scope.dataNominal.IdDecimalTasa = (util.bcs_formato(0,'GEN','Tasa',$scope.dato.IdMonedaLiquidacion ,$scope.dato.IdTipoInstrumento,'')).cantidadDecimales;
	$scope.dataNominal.IdDecimalPrecio = (util.bcs_formato(0,'GEN','Precio',$scope.dato.IdMonedaLiquidacion,$scope.dato.IdTipoInstrumento,'')).cantidadDecimales;

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
	$scope.dataNominal.oConfiguracionClasificacionIfrs = {
		tipo : "combobox-codigo-ifrs",
		configuracion : {
			//template: '<span >#: Codigo # - #: Nombre #	</span>'
		},
		filtros : {
			Codigo : { "field"   : "Codigo","operator": "eq", "value"   : "" },
		},
		idActual:false,
		idPostFiltro:false,
		idDefecto:false,
	};
	/****************************************
	 *
	 * Eventos
	 *
	 * **************************************/

	var calcularParidadMonto = function(){
		var decimales = 1;
		var data = Object();

		if ( $scope.dataNominal.kngMonto === $scope.MontoEquivalenteUltimo ) {
			// Si no ha cambiado el monto calculado inicialmente... 
			valorizar();
		} else { 

			data.p_IdEmpresa =  $scope.SesionIdEmpresa;
			data.p_IdTrabajo =  1315;
			data.p_CalculosEntrada = [{
				"IdTrabajo"			: data.p_IdTrabajo
				,"IdInterno"		: 1
				,"Fecha"			: util.dateSPdate(fechaSistema)
				,"IdMonedaOrigen" 	: 'CLP'
				,"IdMonedaDestino" 	: $scope.dataNominal.MonedaLiquidacionInstrumento === 'CLP' ? 
									  $scope.dataNominal.IdTipoReajuste :
									  $scope.dataNominal.MonedaLiquidacionInstrumento
				,"ValorCambioLocalModificado" : 0
				,"MontoOrigen"		: $scope.dataNominal.kngMonto
				,"IdUso"			: 'MESA'
			}];
	
			$scope.MontoEquivalente = -1;
			bcsServiciosMesa.ObtenerParidadTipoCambio( data ).$promise.then(function(respuesta) {
				if (respuesta.p_out_CalculosSalida.length > 0){
					$scope.MontoEquivalente = Math.round(respuesta.p_out_CalculosSalida[0].MontoDestino*dec_x_1)/dec_x_1;
					// Ahora envia a valorizar...
					valorizar();
				}
			});
		};
	};

	
	var calcularParidadLiquidacion = function(){
		var data = Object();

		data.p_IdEmpresa =  $scope.SesionIdEmpresa;
		data.p_IdTrabajo =  1315;
		data.p_CalculosEntrada = [{
			"IdTrabajo"			: data.p_IdTrabajo
			,"IdInterno"		: 1
			,"Fecha"			: $scope.SesionFechaSistema
			,"IdMonedaOrigen" 	: 'CLP'	// El monto valorizado siempre esta en pesos  => hay que llevarlo a la moneda de liquidacion
			,"IdMonedaDestino" 	: $scope.dato.MonedaLiquidacionOperacion
			,"ValorCambioLocalModificado" : 0
			,"MontoOrigen"		: $scope.dataNominal.kngMonto
			,"IdUso"			: 'MESA'
		}];

		$scope.MontoEquivalente = -1;
		bcsServiciosMesa.ObtenerParidadTipoCambio( data ).$promise.then(function(respuesta) {
			if (respuesta.p_out_CalculosSalida.length > 0){
				$scope.dataNominal.kngMontoLiquidacionOperacion = respuesta.p_out_CalculosSalida[0].MontoDestino;				
			}
		});

	};	
	

	var valorizar = function(){
		var datos = new Object();
		var ParamMercado = new Object();
		var rescate = new Big(0);
		
		console.log(">> valorizar");		
		bcsComunes.ventanaOverlay($scope.IdVentana,true);
		$scope.dataNominal.valorizar = false;
		
		$scope.dataNominal.valkngCantidad = $scope.dataNominal.kngCantidad;		
		$scope.dataNominal.valkngValorRescate = $scope.dataNominal.kngValorRescate;
		$scope.dataNominal.valkngMonto = $scope.dataNominal.kngMonto;
		
		var corte = new Big($scope.dataNominal.kngCorteMinimo);
		var cantidad = new Big($scope.dataNominal.kngCantidad);
		var nominal = cantidad.mod(corte);
		
		// Si esta buscando el nominal no corre esta validacion
		if ($scope.TipoIncognita < 2) {
			if ( $scope.dataNominal.IdClasificacionTipoInstrumento === "IF" && $scope.dataNominal.switchFungible === false )	{
				rescate = new Big($scope.dataNominal.kngValorRescate);
				nominal =  rescate.mod(corte);
			}
			
			if (nominal > 0){
				bcsComunes.msg("error","error",$translate.instant('CORTE_MINIMO_INVALIDO'));
				bcsComunes.ventanaOverlay($scope.IdVentana,false);
				$scope.dataNominal.valorizar = true;
				return;
			}	
		}

		// Esta buscando el valor rescate y las monedas son distintas ?
		$scope.MontoAValorizar = $scope.dataNominal.kngMonto;
		if ( ( $scope.TipoIncognita > 1 ) &&
			 ( "CLP" !== $scope.dataNominal.IdTipoReajuste || "CLP" !== $scope.dataNominal.MonedaLiquidacionInstrumento )
		   ) {
			// Viene desde calculo de paridades
			$scope.MontoAValorizar = $scope.MontoEquivalente;
		}
	
		$scope.MontoAValorizar = $scope.TipoIncognita == 0 ? null : $scope.MontoAValorizar;
		
		// Busca la tasa?
		if ( $scope.TipoIncognita == 1 ) { 
			PrecioValorizacion = null;
			TasaValorizacion = null;

		} else {

			PrecioValorizacion = Number($scope.dataNominal.kngPrecio);
			if ( $scope.dataNominal.IdClasificacionTipoInstrumento === "IF" ) {
				TasaValorizacion = Number($scope.dataNominal.kngTasa);
				PrecioValorizacion = null;
			} else {
				TasaValorizacion = $scope.dataNominal.kngTasa === null || $scope.dataNominal.kngTasa === "" ? null : Number($scope.dataNominal.kngTasa);
				if ( TasaValorizacion !== null ) {
					PrecioValorizacion = null;
				}
			}

		}		
				
		datos.p_IdEmpresa = $scope.SesionIdEmpresa;
		datos.p_IdUsuario = $scope.SesionIdUsuario;		
		datos.p_IdModulo = 'MES';
		datos.p_IdPais	= $scope.$root.SesionIdPaisSistema;
		datos.p_OrigenProcesamiento = 'MESA_UI';
		datos.p_IdMonedaAdicional =  $scope.dato.IdMonedaLiquidacion;
		datos.p_TicketProcesamiento	= 5604,
		datos.p_Instrumentos = [{
				IdValorizacion					: 1,
				FechaOperacion					: $scope.SesionFechaSistema,
				FechaValorizacion				: util.dateSPdate(fechaLiquidacionOperacion||fechaSistema),//$scope.SesionFechaSistema
				//FechaVencimientoOperacion 		: util.dateSPdate($scope.dataNominal.kngFechaVencimiento||fechaSistema),
				//PlazoEnDiasOperacion 			: 0,//$scope.formData.diasFechaVencimiento, //Solo para operaciones a plazo				
				IdClasificacionTipoInstrumento	: $scope.dato.IdClasificacionTipoInstrumento, 
				
				IdTipoInstrumento			: $scope.dato.IdTipoInstrumento, 
				IdInstrumento				: $scope.dato.IdInstrumento, 
				FechaVencimiento			: util.dateSPdate($scope.dataNominal.kngFechaVencimiento||fechaSistema),
				FechaEmision				: $scope.dato.FechaEmisionInstrumento,
				IdMonedaInstrumento			: $scope.dato.MonedaLiquidacionInstrumento,//$scope.dato.IdMonedaLiquidacion,//$scope.dato.IdMonedaInstrumento,//****
				IdTipoReajusteInstrumento	: $scope.dato.IdTipoReajuste,
				Cantidad					: $scope.dataNominal.valkngCantidad || null, 
				ValorRescate				: $scope.dataNominal.valkngValorRescate || null,
				PrecioCompra				: PrecioValorizacion,
				TasaCompra					: TasaValorizacion,
				MontoCompra					: $scope.MontoAValorizar || null,//en moneda del instrumento		
				MontoCompraMonedaSistema	: null,					
				Emisor						: $scope.dato.IdEmisor,
				PrecioSubyacente			: null, 
				TasaSubyacente				: null, 
				PrecioGarantia				: null, 
				TasaGarantia				: null, 
				TasaTIF						: null,		
				TieneCorteDeCupon			: null,
				IdCliente					: null, 
				IdPortafolio				: null,
				
				TablaOrigen				: 'CARTERA',
				
				IdUso						: null, 
				TipoCartera					: null, 
				IdTipoCriterioTasa			: null,
				Nemotecnico					: $scope.dato.Nemotecnico,
				IdCondicionDeLiquidacion	: $scope.dato.IdCondicionDeLiquidacion,
				IdMonedaEmisionInstrumento	: $scope.dato.IdTipoReajuste,//$scope.dato.MonedaEmisionInstrumento,//$scope.dato.IdMonedaInstrumento || null,
			}];
		
		 let ValoresTasaPrecioValidos = true;   
         
      if(ValoresTasaPrecioValidos == true){
      	bcsServicioPlataforma.ValorizarInstrumento(datos,function(respuesta)
		{	
			bcsComunes.ventanaOverlay($scope.IdVentana,false);
			
			if (respuesta.P_OUT_ERRORESCONTADOR > 0){
				console.error('Error valorizacion',$translate.instant('UI_ERROR_RUTINA_VALORIZACION'));
				var error = '';
				$.each(respuesta.P_OUT_MENSAJES,function(key,value) {
					if (value.Tipo === 'ERROR'){
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

						// Esta buscando el valor rescate, pero puede ser que no sea multiplo del corte minimo
						if ( $scope.TipoIncognita === 2 ) {
							if ($scope.dato.IdClasificacionTipoInstrumento === 'IF') {
								rescate = new Big(value.ValorRescate);
								nominal =  rescate.mod(corte);
								if (nominal > 0){
									bcsComunes.msg("error","error",$translate.instant('CORTE_MINIMO_INVALIDO'));
									$scope.dataNominal.valorizar = true;
									return;
								}
								$scope.dataNominal.kngValorRescate = Number(value.ValorRescate);
							}
						}

						if ($scope.dato.IdClasificacionTipoInstrumento === 'IF') {
							$scope.dataNominal.kngPrecio = "";
						}
						
						$scope.dataNominal.kngMonto = Number(value.MontoMonedaSistema || 0);
						$scope.dataNominal.kngMontoLiquidacion = Number(value.MontoMonedaInstrumento || 0);
						$scope.dataNominal.kngMontoLiquidacionOperacion = Number(value.MontoMonedaAdicional || 0);
						$scope.dataNominal.kngPrecio = Number(value.Precio ||0);
						$scope.dataNominal.kngTasa = Number(value.Tasa || 0);

						if ( Number($scope.MontoAValorizar || 0) > 0 ) {
							$scope.dataNominal.MontoValorizadoTipoReajuste = Number($scope.MontoAValorizar || 0);	
						} else if (value.ValorTipoReajuste > 0) {
							$scope.dataNominal.MontoValorizadoTipoReajuste = value.MontoMonedaSistema/value.ValorTipoReajuste;	
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
				$scope.dataNominal.ValoresPrevios();

			}
			
						
			
		},function(respuesta)
		{
			console.log(">> respuesta");
			$scope.dataNominal.kngMonto =  0;
			$scope.dataNominal.kngMontoLiquidacion = 0;
			$scope.dataNominal.kngMontoLiquidacionOperacion = 0;
			$scope.dataNominal.kngPrecio = $scope.dato.IdClasificacionTipoInstrumento === "IF"?"":0;
			$scope.dataNominal.kngTasa =  0;
			bcsComunes.ventanaOverlay($scope.IdVentana,false);
		});
      }else{
      	$scope.dataNominal.valorizar = true;
			return;
      }	
	};

	var valida = function(){
		console.log(">> valida");
		var res = '';
		var corte = new Big($scope.dataNominal.kngCorteMinimo);
		var cantidad = new Big($scope.dataNominal.kngCantidad);
		var nominal = cantidad.mod(corte);
		if ( $scope.dataNominal.IdClasificacionTipoInstrumento === "IF" && $scope.dataNominal.switchFungible === false )	{
			var rescate = new Big($scope.dataNominal.kngValorRescate);
			nominal =  rescate.mod(corte);
		}				
		if (nominal > 0){
			res = $translate.instant('CORTE_MINIMO_INVALIDO');
		}
		/*
		if ($scope.dato.IdClasificacionTipoInstrumento === 'IF') {
			// <Cantidad> + <Val.Rescate> [+ <Tasa|Monto>]
			if ((Number($scope.dataNominal.kngCantidad) <= 0) || (Number($scope.dataNominal.kngMonto) <= 0)) {
				res = $translate.instant('MONTO_MAYOR_CERO');
			}
		} else {
			// <Cantidad> + <Tasa|Precio> [+ Monto]
			if (Number($scope.dataNominal.kngCantidad) <= 0) {
				res = $translate.instant('MONTO_MAYOR_CERO');
			}
		}*/
		if (Number($scope.dataNominal.kngMonto) <= 0){
			res = $translate.instant('MONTO_MAYOR_CERO');
		}
		return res;
	};
		
	$scope.dataNominal.valoriza = function(){
		Mercado = $scope.dataNominal.IdClasificacionTipoInstrumento
		Cantidad = Number($scope.dataNominal.kngCantidad);
		if ( Mercado === 'IF' ) {
			Cantidad = Number($scope.dataNominal.kngValorRescate);
		}
		
		Tasa = $scope.dataNominal.kngTasa === null || $scope.dataNominal.kngTasa === "" ? null : Number($scope.dataNominal.kngTasa)
		Precio = $scope.dataNominal.kngPrecio === null || $scope.dataNominal.kngPrecio === "" ? null : Number($scope.dataNominal.kngPrecio);
		Monto = Number($scope.dataNominal.kngMonto);		
	
		if ( Cantidad === 0 && Monto === 0 && Tasa === 0 && Precio === 0 ) {
			bcsComunes.msg("error","error", "UI_INGRESE_VALOR");
			return;
		} else if ( Cantidad === 0  && Monto === 0 ) {
			bcsComunes.msg("error","error", "UI_FALTAN_VALORES");
			return;
		} else if (Cantidad === 0) {
			if (Mercado === 'RF') {
				bcsComunes.msg("error","error", "UI_INGRESE_CANTIDAD");
			} else if (Mercado === 'IF') {
				bcsComunes.msg("error","error", $translate.instant("VALOR_RESCATE_MENOR_IGUAL_A_CERO"));
			}
			return;
		} else if ( $scope.CantidadCartera < Cantidad && Mercado === 'RF' && $scope.dato.IdCartera > 0 ) {
			bcsComunes.msg("error","error", "UI_CANTIDAD_EXCEDE_CARTERA");
			return;
		} else if ( $scope.ValorRescateCartera < Cantidad && Mercado === 'IF' && $scope.dato.IdCartera > 0 ) {
			bcsComunes.msg("error","error", "UI_VALOR_RESCATE_EXCEDE_CARTERA");
			return;
		}
		//Agregar Error por corte = 0
		else if ($scope.dataNominal.kngCorteMinimo === 0 ) {
			bcsComunes.msg("error","error", "UI_CORTE_MINIMO");
			return;
		}
	
		// La primera vez que va a valorizar
		if ( $scope.dataNominal.PrimeraValorizacion ) {
			$scope.TipoIncognita = -1;			
			// Si no cambia valores propuesto, debe valorizar por Monto + Cantidad, asi asegura monto propuesto
			if ( $scope.dataNominal.valkngCantidad === $scope.dataNominal.kngCantidad &&
				 $scope.dataNominal.valkngValorRescate === $scope.dataNominal.kngValorRescate &&
				 $scope.dataNominal.valkngPrecio === $scope.dataNominal.kngPrecio &&
				 $scope.dataNominal.valkngTasa === $scope.dataNominal.kngTasa &&
				 $scope.dataNominal.valkngMonto === $scope.dataNominal.kngMonto ) {
				// 1 - Nominal/Cantidad + Mto. Valorizado => Tasa/Precio
				$scope.TipoIncognita = 1;
			} else {
				// Solo si cambia valores valoriza mormal
				$scope.dataNominal.PrimeraValorizacion = false;
			}
		}

		if ( $scope.TipoIncognita === -1 ) 
		{
			if ( Mercado === "IF" && Tasa != null  && Cantidad > 0 ) {
				// 0 - Nominal + Tasa => Mto. Valorizado			
				$scope.TipoIncognita = 0;
			} else if ( Mercado === "RF" && (Tasa != null || Precio != null ) && Cantidad > 0 ) {
				// 0 - Cantidad + Tasa|Precio => Mto. Valorizado			
				$scope.TipoIncognita = 0;
			} else if ( Cantidad > 0 && Monto > 0 ) {
				// 1 - Nominal/Cantidad + Mto. Valorizado => Tasa
				$scope.TipoIncognita = 1;
			} else if ( Monto > 0 && ( Tasa !== null || Precio != null ) ){
				// 2 - Mto. Valorizado + Tasa => Nominal/Cantidad
				$scope.TipoIncognita = 2;
			} else {
				// Borde
				$scope.TipoIncognita = 0;
			}
		}

		//Cuando se valoriza por monto este debe ir en CLP aunque el tipo de reajuste sea distinto de CLP
		if ( ( $scope.TipoIncognita > 1 ) &&
			 ( ( "CLP" !== $scope.dataNominal.IdTipoReajuste ) ||
			   ( "CLP" !== $scope.dataNominal.MonedaLiquidacionInstrumento ) )
		   ) {
			// Calcula la paridad
			calcularParidadMonto();
		} else {
			valorizar();	
		}		
	};

	
	$scope.dataNominal.cancelar = function(){
		// Boton Modificar
		$scope.dataNominal.valorizar = true;
		
	};
	
	
	$scope.dataNominal.cerrar = function() {
		// Boton Cancelar
		var data = {boton:'CANCELAR', data: {}};
		bcsComunes.ventanaData($scope.IdVentana,data);
		bcsComunes.ventanaCerrar($scope.IdVentana);
		
	};
	$scope.dataNominal.aceptar = function() {
		// Boton Agregar cartera
		var data = {boton:'ACEPTAR', data: $scope.dataNominal};
		var res = valida();
		if (res.length > 0){
			bcsComunes.msg("error","error",res);
			return;
		}
		bcsComunes.ventanaData($scope.IdVentana,data);
		bcsComunes.ventanaCerrar($scope.IdVentana);			
	};



	$scope.dataNominal.ValoresPrevios = function(){
		// Para despues verificar que enviar a valorizar....
		$scope.dataNominal.valkngCantidad = $scope.dataNominal.kngCantidad;
		$scope.dataNominal.valkngValorRescate = $scope.dataNominal.kngValorRescate;
		$scope.dataNominal.valkngPrecio = $scope.dataNominal.kngPrecio;
		$scope.dataNominal.valkngTasa = $scope.dataNominal.kngTasa;
		$scope.dataNominal.valkngMonto = $scope.dataNominal.kngMonto;		
	} 
	
	/****************************************
	 *
	 * Carga de datos....
	 *
	 * **************************************/

	if ($scope.dato.FolioCartera!== undefined && $scope.dato.FolioCartera !== "") {
		
		$scope.dataNominal.IdClasificacionTipoInstrumento = $scope.dato.IdClasificacionTipoInstrumento;
		$scope.dataNominal.GlsMercado = $scope.dato.ClasificacionTipoInstrumento;
		$scope.dataNominal.GlsInstrumento =  $scope.dato.Nemotecnico;
		$scope.dataNominal.GlsEmisor = $scope.dato.IdEmisor;
		$scope.dataNominal.GlsValorClasificacionRiesgo =  $scope.dato.ValorClasificacionRiesgo;		
		$scope.dataNominal.IdBoveda= $scope.dato.IdBoveda;
		$scope.dataNominal.GlsIFRS = $scope.dato.CodigoDescIFRS;
		$scope.dataNominal.oConfiguracionClasificacionIfrs.idActual = $scope.dato.CodigoIFRS===''?$scope.dato.MesaCodigoIFRS:$scope.dato.CodigoIFRS;
        $scope.dataNominal.oConfiguracionClasificacionIfrs.idDefecto = $scope.dato.CodigoIFRS===''?$scope.dato.MesaCodigoIFRS:$scope.dato.CodigoIFRS;
		$scope.dataNominal.GlsBoveda= $scope.dato.NombreBoveda;
		$scope.dataNominal.switchFungible = $scope.dato.EsFungible === "S";
		$scope.dataNominal.IdCartera = $scope.dato.IdCartera;
		$scope.dataNominal.activarEmisor = true;
		$scope.dataNominal.activarTipoReajuste = true;
		$scope.dataNominal.valorizar = true;
		
		$scope.dataNominal.kngFechaVencimiento = $scope.dato.FechaVencimientoInstrumento;//util.dateDS(util.dateSPdate($scope.dato.FechaVencimientoInstrumento),"FechaSistema","CALL");;
		$scope.dataNominal.kngFechaVencimientoDisplay = util.formatoDate($scope.dato.FechaVencimientoInstrumento);
		$scope.dataNominal.IdTipoReajuste = $scope.dato.IdTipoReajuste;
		//$scope.dataNominal.MonedaEmisionInstrumento = $scope.dato.MonedaEmisionInstrumento;
		$scope.dataNominal.MonedaLiquidacionInstrumento = $scope.dato.MonedaLiquidacionInstrumento;
		$scope.dataNominal.kngCorteMinimo = $scope.dato.CorteMinimo;
		$scope.dataNominal.activarCantidad = false;
		$scope.dataNominal.kngCantidad = $scope.dato.Cantidad;
		$scope.dataNominal.activarPrecio = $scope.dato.IdClasificacionTipoInstrumento === "IF";
		$scope.dataNominal.precioRequerido = $scope.dato.IdClasificacionTipoInstrumento === "RF";
		$scope.dataNominal.valorRescateRequerido = $scope.dato.IdClasificacionTipoInstrumento === "IF";
		$scope.dataNominal.activarValorRescate = $scope.dato.IdClasificacionTipoInstrumento === "RF";

		if ( $scope.dato.IdClasificacionTipoInstrumento === "IF" && $scope.dato.EsFungible === "S" ) {
			$scope.dataNominal.activarCantidad = true;
		}

		$scope.dataNominal.kngTasaMercado = $scope.dato.INGTasaMercadoActual;
		if ( $scope.dato.IdClasificacionTipoInstrumento === "RF" ) {
			$scope.dataNominal.kngPrecioMercado = $scope.dato.INGPrecioMercadoActual;
		}

		$scope.dataNominal.IdDecimalPrecioDisplay = (util.bcs_formato(0,'MES', 'Precio', $scope.dato.IdTipoReajuste, $scope.dato.IdTipoInstrumento, '')).cantidadDecimales;
		$scope.dataNominal.IdDecimalMontoDisplay = (util.bcs_formato(0,'MES', 'Monto', $scope.SesionIdMonedaSistema, $scope.dato.IdTipoInstrumento, '')).cantidadDecimales;
		$scope.dataNominal.IdMonedaMontoDisplay = $scope.SesionIdMonedaSistema;
		$scope.dataNominal.IdDecimalMontoLiquidacionDisplay = (util.bcs_formato(0,'GEN','Monto',$scope.dato.MonedaLiquidacionOperacion,'','')).cantidadDecimales;
		$scope.dataNominal.IdMonedaMontoLiquidacionDisplay = $scope.dato.IdMonedaLiquidacion;//$scope.dataNominal.MonedaLiquidacionInstrumento;
		$scope.dataNominal.IdMonedaRescateDisplay = $scope.dato.IdTipoReajuste;//$scope.dato.MonedaEmisionInstrumento;
		//$scope.dataNominal.IdDecimalRescateDisplay = (util.bcs_formato(0,'MES', 'Monto', $scope.dato.IdTipoReajuste, $scope.dato.IdTipoInstrumento, '')).cantidadDecimales;
		$scope.dataNominal.IdDecimalValorCaptacionDisplay = (util.bcs_formato(0,'MES','Monto', $scope.dato.IdTipoReajuste, $scope.dato.IdTipoInstrumento, '')).cantidadDecimales;		


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


		$scope.CantidadCartera = $scope.dato.Cantidad;
		$scope.ValorRescateCartera = $scope.dato.ValorRescate;
		dec_x_1 = Math.pow(10,$scope.dataNominal.IdDecimalRescateDisplay);

		
		// Calcula el Valor Rescate/Cantidad a porponer de acuerdo a lo que falte para la asignación
		//Diferencia = Number($scope.dato.MontoOperacion) - Number($scope.dato.MontoAsignado);
		Diferencia = Number($scope.dato.MontoFinalCLP) - Number($scope.dato.MontoAsignado);
		// Variables para el calculo
		CorteMinimo = Number($scope.dato.CorteMinimo);
		
		INGValorRescate = Number($scope.dato.IdClasificacionTipoInstrumento === "RF"?0:$scope.dato.ValorRescate);
		INGCantidad = Number($scope.dato.Cantidad);
		if(INGCantidad === 0){
			$scope.dataNominal.muestraDatosRF = true;
		}else{
			$scope.dataNominal.muestraDatosRF = false;
		}
		
		INGMontoATasaMercadoActual = Number($scope.dato.INGMontoATasaMercadoActual); 
		if ( INGMontoATasaMercadoActual === 0 ) {
			INGMontoATasaMercadoActual = $scope.dato.INGMontoATasaCompraActual;
		}
		
		CantidadPropuesta = INGCantidad; 
		MontoPropuesto = INGMontoATasaMercadoActual;
		ValorRescatePropuesto = $scope.dato.IdClasificacionTipoInstrumento === "RF"?"":$scope.dato.ValorRescate;		
		if (Diferencia > 0 && Diferencia < MontoPropuesto) { 
			if ($scope.dato.IdClasificacionTipoInstrumento === "RF") {
				if ( CorteMinimo <= 1) {
			 		Cortes	= Math.trunc( Diferencia / 
			 				    ( ( INGMontoATasaMercadoActual * CorteMinimo / 
			 				        INGCantidad )
			 				  ));    
			 		CantidadPropuesta = (Number(Cortes) * CorteMinimo );    
	            	MontoPropuesto	  = ( ( INGMontoATasaMercadoActual / INGCantidad ) *  Number(INGCantidad));
				}
				else {
					Cortes	= Math.trunc( Diferencia / 
							 			 (( INGMontoATasaMercadoActual * CorteMinimo )
							 			  / INGCantidad
							 			 ));
					if ( Cortes === 0 ) {
						Cortes = 1;
					}

					if ( $scope.dato.IdTipoReajuste !== 'CLP' ) {//no reajustable
						CantidadPropuesta = Math.trunc((Cortes * CorteMinimo));    
		            }
		            else {  
		            	CantidadPropuesta = Cortes * CorteMinimo;  
		            }
		            MontoPropuesto = Math.round((INGMontoATasaMercadoActual / INGCantidad ) * CantidadPropuesta);
				}
				if ( CantidadPropuesta >= INGCantidad){
					CantidadPropuesta = INGCantidad;
					MontoPropuesto = INGMontoATasaMercadoActual;
				}

			} else if ($scope.dato.IdClasificacionTipoInstrumento === 'IF'){
				// if ( INGMontoATasaMercadoActual <= Diferencia ){
				//	CantidadPropuesta = INGCantidad;
				//	ValorRescatePropuesto = INGValorRescate;  
				// }
				if ($scope.dato.EsFungible === "S"){
					CantidadPropuesta = INGCantidad;
					
					if ( $scope.dato.IdTipoReajuste !== 'CLP' ) {
						ValorRescatePropuesto = ( Diferencia * INGValorRescate ) / 	INGMontoATasaMercadoActual;
						MontoPropuesto = (INGMontoATasaMercadoActual / INGValorRescate ) * ValorRescatePropuesto;
					} else {
						ValorRescatePropuesto = Math.round( ( Diferencia * INGValorRescate ) / INGMontoATasaMercadoActual);
						MontoPropuesto = Math.round( (INGMontoATasaMercadoActual / INGValorRescate ) * ValorRescatePropuesto) ;
					}
					 
				}
				else {
					CantidadPropuesta = Math.floor( ( (Diferencia * INGValorRescate) / INGMontoATasaMercadoActual) / CorteMinimo ,0, 1);
					//CantidadPropuesta = $scope.dato.IdTipoReajuste !== 'CLP'? Math.round( Cortes * CorteMinimo ) : Cortes * CorteMinimo;
					if( CantidadPropuesta === 0 ) {
						CantidadPropuesta = 1;
					}
					//ValorRescatePropuesto = ( CantidadPropuesta * INGValorRescate ) / INGCantidad;
					ValorRescatePropuesto = Math.trunc(Number(CantidadPropuesta) * Number(CorteMinimo));
					MontoPropuesto = Math.round((INGMontoATasaMercadoActual / INGValorRescate ) * ValorRescatePropuesto);
				}
			}

		} 
		else if (Diferencia <= 0){
			CantidadPropuesta = $scope.dataNominal.oConfiguracionCantidad.min; 
			MontoPropuesto = 0;
			ValorRescatePropuesto = $scope.dato.IdClasificacionTipoInstrumento === "RF"?"":0;
		}

		
		$scope.dataNominal.kngCantidad = CantidadPropuesta;
		$scope.dataNominal.kngMonto = MontoPropuesto;
		$scope.dataNominal.kngValorRescate = ValorRescatePropuesto;
		$scope.dataNominal.kngMontoLiquidacion = $scope.dato.MontoLiquidacion;
			
		if ( $scope.dato.MontoATasaMercado > 0 ) {
			$scope.dataNominal.kngPrecio = $scope.dato.IdClasificacionTipoInstrumento === "IF"?"":$scope.dato.INGPrecioMercadoActual;
			$scope.dataNominal.kngTasa = $scope.dato.INGTasaMercadoActual;
			// $scope.dataNominal.kngMonto = $scope.dato.INGMontoATasaMercadoActual;
		} else {
			$scope.dataNominal.kngPrecio = $scope.dato.IdClasificacionTipoInstrumento === "IF"?"":$scope.dato.INGPrecioCompra;
			$scope.dataNominal.kngTasa = $scope.dato.INGTasaCompra;
			// $scope.dataNominal.kngMonto = $scope.dato.INGMontoATasaCompraActual;			
		}

		// if ( $scope.dataNominal.IdTipoReajuste !==  $scope.dato.MonedaLiquidacionOperacion && $scope.dato.MonedaLiquidacionOperacion !== "CLP") {
		if ( $scope.dato.MonedaLiquidacionOperacion !== "CLP") {
			$scope.dataNominal.kngMontoLiquidacionOperacion = $scope.dato.MontoLiquidacionOperacion;		
		} else {
			$scope.dataNominal.kngMontoLiquidacionOperacion = $scope.dataNominal.kngMonto;
		}
		
		//Circular 514 - Si la venta con pacto viene de una compra con pacto y parámetro esta en 'S' se debe bloquear el cambio de tasa y precio
		bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise('TASARECOMPRAVENTACONPACTO', $scope.SesionIdEmpresa, 'MESA')
		.then(function (parametroTasaRecompraVentaConPacto) {
			if( $scope.dato.IdTipoOperacionCartera === "VENPAC" && parametroTasaRecompraVentaConPacto.Valor ==='S' && $scope.dato.TipoCarteraCorredor === "CC"){
				$scope.dataNominal.activarTasa = true;
				$scope.dataNominal.activarPrecio = true;
				$scope.dataNominal.origenCompraConPacto = true;
			}
		});
		
		$scope.dataNominal.ValoresPrevios(); 
	}

	$scope.$watch('dataNominal.kngCantidad', function(newval,oldval ){
		if ($scope.dataNominal.kngCantidad && Number($scope.dataNominal.kngCantidad) > 0 && newval !== oldval &&
			$scope.dataNominal.IdClasificacionTipoInstrumento === "IF" && $scope.dataNominal.switchFungible === false && 
			$scope.dataNominal.kngCorteMinimo > 1 ) {
			$scope.dataNominal.kngValorRescate = Math.trunc(Number($scope.dataNominal.kngCantidad) * Number($scope.dataNominal.kngCorteMinimo));
		}
		
	});
			
	$scope.$watch('dataNominal.kngValorRescate', function(newval,oldval ){
		if ($scope.dataNominal.kngValorRescate && Number($scope.dataNominal.kngValorRescate) > 0 && newval !== oldval &&
			$scope.dataNominal.IdClasificacionTipoInstrumento === "IF" && $scope.dataNominal.switchFungible === false &&
			$scope.dataNominal.kngCorteMinimo > 1 ){
			$scope.dataNominal.kngCantidad = Math.trunc(Number($scope.dataNominal.kngValorRescate) / Number($scope.dataNominal.kngCorteMinimo));
		}
	});		
	
});
