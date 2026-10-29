/*************************************************************
 * Purpose	: Modificar Préstamo de Títulos
 * Author	: TEAM CUSTODIA - BCS
 * Date		: 16-02-2018
 *************************************************************/
 (function() {
	angular.module('ModuloCustodia').controller('ModificarPrestamoModalCtrl',
	function ($rootScope,$scope,$translate,$timeout,bcsComunes,bcsServiciosCustodia,bcsAlerta,OPCIONES_BCSALERTA,
			bcsServicioPlataforma,bcsServicioCalcularMontoTipoInstrumento,bcsServiciosPrestamo) {
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		$scope.data 	= {};
		var cargando 	= true;
		
		var idVentana 				= bcsComunes.ventanaWinId($scope);
		$scope.data.prtModificado 	= bcsComunes.ventanaParams(idVentana);
		
		
		$scope.idVentana = idVentana;
		$scope.data.muestraDatosRF = $scope.data.prtModificado.IdClasificacionTipoInstrumentoSubyacente == 'RF';
		$scope.data.checkCobroAlInicio = ($scope.data.prtModificado.CobraPrimaAlInicio==='S')?true:false;

		$scope.data.operaCustodiaExterna 				= false;
		$scope.data.yaValorizo 							= false;
		$scope.data.MaximoDiasPrestamo 					= $scope.data.prtModificado.PlazoEnDiasOperacion + $scope.data.prtModificado.MaximoDiasPrestamo;
		$scope.data.MinimoDiasPrestamo 					= $scope.data.prtModificado.PlazoEnDiasOperacion;
		$scope.data.monedaCorteMinimo 					= "";
		$scope.data.monedaSistema 						= $scope.SesionIdMonedaSistema;
		$scope.data.monedaInstrumento 					= $scope.data.prtModificado.IdMonedaSubyacente;
		$scope.data.FechaVencInstrOptions 				= {};
		$scope.data.oDecimales							= {};
		$scope.data.oDecimales.corteMinimo				= {};
		$scope.data.oDecimales.corteMinimo.decimales 	= 0;
		$scope.data.nginstrumento						= "-1";
		$scope.data.precioFormat						= (util.bcs_formato(0,'GEN','Precio',$scope.SesionIdMonedaSistema,'',''));
		var valorizadorEnEjecucion						= false;

		// Obtiene la fecha del dia siguiente a la fecha de ingreso del prestamo
		$scope.data.diaSiguienteIngresoPrestamo = "0001-01-01";	
		$scope.data.prtModificado.FechaVencimientoPrestamo = $scope.data.prtModificado.Accion=== "CREAR" ?util.dateInicioDia($scope.data.prtModificado.FechaPrestamoVencimiento):$scope.data.prtModificado.FechaVencimientoPrestamo
		$scope.data.prtModificado.FechaLiquidacion = $scope.data.prtModificado.Accion=== "CREAR" ?util.dateInicioDia($scope.data.prtModificado.FechaLiquidacionPrestamo):$scope.data.prtModificado.FechaLiquidacion
		$scope.data.prtModificado.INGFechaOperacion = $scope.data.prtModificado.Accion=== "CREAR" ?util.dateInicioDia($scope.data.prtModificado.INGFechaOperacion):$scope.data.prtModificado.INGFechaOperacion
		$scope.data.fechaInicial 				= $scope.data.prtModificado.INGFechaOperacion;
		
		
		
				
		var datos = {
			p_fechas  : [
							{ fechaInicial :util.dateSPdate($scope.data.prtModificado.INGFechaOperacion ), numeroDias : 1, pais : $scope.SesionIdPaisSistema }
						],
			p_diaHabil 			: 'S',
			p_soloCaeDiaHabil 	: 'S',
			p_idPais 			: $scope.SesionIdPaisSistema
		};
		
		bcsServicioPlataforma.Servicios().ObtenerDiaSiguiente(datos,function(result) {
				if (result.P_OUT_ERRORESCONTADOR == 0)
				{
					$scope.data.diaSiguienteIngresoPrestamo = util.dateDS(result.p_out_fechas[0].fechaCalculada,"FechaSistema","CALL");
				}				
			}
			,function(result){
				console.log(result);
				}
			);

		/*********************************************
		 * CARGA INICIAL
		 ********************************************/
		$scope.data.cantidadOptions 		= {format: 'n'+(util.bcs_formato(0,'GEN','Cantidad',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.tasaOptions 			= {format: 'n'+(util.bcs_formato(0,'GEN','Tasa',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:-999999999999999999999,spinners: false};
		$scope.data.precioOptions 			= {format: 'n'+$scope.data.precioFormat.cantidadDecimales,min:0,spinners: false};
		$scope.data.montoOptions 			= {format: 'c'+(util.bcs_formato(0,'GEN','Monto',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.plazoOptions 			= {format: 'n0', min:$scope.data.MinimoDiasPrestamo, max:$scope.data.MaximoDiasPrestamo, spinners: false};
		$scope.data.porcentajePrimaOptions 	= {format: 'n'+(util.bcs_formato(0,'GEN','Tasa',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.montoPrimaOptions 		= {format: 'c'+(util.bcs_formato(0,'GEN','Monto',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.montoPlazoOptions 		= {format: 'c'+(util.bcs_formato(0,'GEN','Monto',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.accionDisabled 			= true;
		
		$scope.data.precioDisabled 			 = true;
		$scope.data.tasaDisabled 			 = true;
		$scope.data.porcentajePrimaDisabled  = true;
		$scope.data.fechaVencimientoDisabled = true;
		$scope.data.fechaVencimientoDesactivado= false;

		$scope.data.kngCantidadAct 			= $scope.data.prtModificado.Cantidad;		
		$scope.data.kngPrecioAct 			= $scope.data.prtModificado.INGPrecioInstrumento;
		$scope.data.kngTasaAct 				= $scope.data.prtModificado.INGTasaInstrumento;
		$scope.data.kngMontoAct 			= ($scope.data.muestraDatosRF ? $scope.data.prtModificado.INGMontoInstrumento : 0);
		$scope.data.kngFechaVencimientoAct 	= $scope.data.prtModificado.FechaVencimientoPrestamo;
		$scope.data.kngPlazoAct  			= $scope.data.prtModificado.PlazoEnDiasOperacion;
		$scope.data.kngPorcentajePrimaAct 	= $scope.data.prtModificado.TasaPrestamo;
		$scope.data.kngMontoPrimaAct 		= $scope.data.prtModificado.INGMontoPrima;
		$scope.data.kngMontoPlazoAct 		= $scope.data.prtModificado.INGMontoTotalPrestamo;			
		$scope.data.fechaLiquidacion 		= $scope.data.prtModificado.FechaLiquidacion;
		$scope.data.IdCondicionDeLiquidacion= $scope.data.prtModificado.IdCondicionLiquidacion;

		$scope.data.kngFechaVencimiento 	= $scope.data.prtModificado.FechaVencimientoPrestamo;
		$scope.data.kngPlazo 				= $scope.data.prtModificado.PlazoEnDiasOperacion;
		$scope.data.kngPorcentajePrima 		= $scope.data.prtModificado.TasaPrestamo;
		$scope.data.kngCantidad 			= $scope.data.prtModificado.Cantidad;
		$scope.data.kngPrecio 				= ($scope.data.precioFormat.trucaRedondea === 'T') ? Number(util.truncarNumero($scope.data.prtModificado.INGPrecioInstrumento, $scope.data.precioFormat.cantidadDecimales)) : $scope.data.prtModificado.INGPrecioInstrumento;
		$scope.data.kngTasa 				= $scope.data.prtModificado.INGTasaInstrumento;
		$scope.data.kngMonto 				= $scope.data.prtModificado.INGMontoInstrumento;
		
		$scope.data.InteresDiarioOptions 	= $scope.data.prtModificado.INGMontoInteresCompraProximo;
		$scope.data.InteresAcumuladoOptions	= $scope.data.prtModificado.INGMontoInteresCorridoCompraActual;
	
		
		
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		$scope.data.btnCerrar = function(refrescar) {
			bcsComunes.ventanaData(idVentana,refrescar);
			bcsComunes.ventanaCerrar(idVentana);
		};
		

		$scope.data.ModificarPrestamo = function()
	    {
			
			if (util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema)) ===  util.dateSPdate($scope.data.kngFechaVencimiento)){
				bcsComunes.ventanaMsg({
				    mensaje:$translate.instant("UI_FECHA_VENCIMIENTO_INVALIDA")
				  },idVentana);
				
				return;
			}
			
			if (Number($scope.data.kngPlazo) == 0){
				bcsComunes.ventanaMsg({
				    mensaje:$translate.instant("PLAZO_INVALIDO")
				  },idVentana);
				
				return;
			}
			
		
			var datos = {
				p_IdEmpresa 		: $scope.SesionIdEmpresa,
				p_IdUsuario 		: $scope.SesionIdUsuario,
				p_IdModulo 			: 'CUS',
				p_UsaReglasNegocio 	: 'S',
				p_Fecha 			: util.dateSPdate($scope.data.kngFechaVencimiento),
				p_NumeroDias 		: 0,
				p_EsHabil 			: 'N',
				p_IdPais 			: $scope.SesionIdPaisSistema
			};
			
			bcsServicioPlataforma.FechaSiguiente(datos,function(result) {
				$scope.data.esHabil = 'N';
				if (result.P_OUT_ERRORESCONTADOR === 0){
					if (result.P_OUT_ESHABIL === 'N'){
						bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_FECHA_VENCIMIENTO_INHABIL")},idVentana);
					}
					else 
					{
						var datos = {};	
						bcsComunes.ventanaOverlay(idVentana,true); 
						$scope.data.accionDisabled = undefined;
						
						datos.p_IdEmpresa = $scope.SesionIdEmpresa;
						datos.p_IdUsuario = $scope.SesionIdUsuario;
						datos.p_IdModulo = 'CUS';
						datos.p_IdCartera = $scope.data.prtModificado.IdCartera;
						datos.p_IdSubyacente = $scope.data.prtModificado.IdSubyacente;
						
						datos.p_FechaVencimiento 		= util.dateSPdate($scope.data.kngFechaVencimiento) ||'1900-01-01';
						datos.p_PrecioInstrumento		= Number($scope.data.kngPrecio) || 0;
						datos.p_TasaInstrumento 		= Number($scope.data.kngTasa) || 0;
						datos.p_Monto 					= Number($scope.data.kngMonto) || 0;
						datos.p_MontoMdaInstr 			= Number($scope.data.kngMontoMdaInstr) || 0;
						datos.p_MontoPrima 				= Number($scope.data.kngMontoPrima) || 0;
						datos.p_TasaPrima 				= Number($scope.data.kngPorcentajePrima) || 0;
						datos.p_MontoAPlazo 			= Number($scope.data.kngMontoPlazo) || 0;
						datos.p_PlazoEnDias 			= Number($scope.data.kngPlazo) || 0;
						datos.p_InteresDiario 			= Number($scope.data.kngInteresDiario) || 0;
						datos.p_InteresAcumulado		= Number($scope.data.kngInteresAcumulado) || 0;
						
				   		bcsServiciosPrestamo.ModificarPrestamo(datos,successModificarPrestamo,errorModificarPrestamo);
					}
				}
			}
			,
			function(result) {});
			
	   	};
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
	 	var successModificarPrestamo = function(respuesta)
		{
	   		bcsComunes.ventanaOverlay(idVentana,false);
			$scope.data.accionDisabled = true;
			if (respuesta.P_OUT_ERRORESCONTADOR === 0) {

				bcsComunes.ventanaOverlay(idVentana,false);
				let msj=bcsComunes.ventanaMsg({
				conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);

				if($scope.data.prtModificado.Accion==='CREAR'){
					var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_AGREGAR_PRESTAMISTA"), idVentana); // mensaje "Desea Agregar un prestamista" 
						t.then(function(result)
						  {
							if (result===OPCIONES_BCSALERTA.SI)
							   {
								   bcsComunes.ventanaCerrar(idVentana);
								   var id1 = bcsComunes.ventanaAbrir("CrearEmprestitoModal", $scope.data.prtModificado,false,false,$scope.winID); 
								   var promesa=bcsComunes.ventanaPromesa(id1);                               
								   promesa.then(function(data){ 
									if (data === true){
										// actulizar prestamo original
									}else{
										
									}
								}); 
							   }
							else{

								bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
								bcsComunes.ventanaCerrar(idVentana);
							 } 							  
						  });
				}else{
					var a =  bcsComunes.ventanaMsg({
				    mensaje:$translate.instant("UI_OPERACION_INGRESO_EXITO"),
				    conflictos:util.parseServerResponse(respuesta)
					},idVentana);
				
					var promesa=bcsComunes.ventanaPromesa(a);
					promesa.then(function(data){ 
			        	$scope.data.btnCerrar(true);
			    	}); 
				}
		        
				
				
				
			}
			else {
				bcsComunes.ventanaMsg({
				    mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"),
				    conflictos:util.parseServerResponse(respuesta)
				  },idVentana);
		    }
		};
		
		var errorModificarPrestamo = function(respuesta)
		{
			bcsComunes.ventanaOverlay(idVentana,false);
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.ventanaMsg({
			    mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"),
			    conflictos:util.parseServerResponse(respuesta)
			  },idVentana);
			
			$scope.data.accionDisabled = true;
		};
		
		var fnCambioPQ = function(newValue, oldValue) {
			if (   typeof $scope.data.kngPrecio   !== 'undefined' 
				&& typeof $scope.data.kngCantidad !== 'undefined' )
			{

					if ($scope.data.prtModificado.IdClasificacionTipoInstrumentoSubyacente == "RV"){
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
						datos.p_IdTipoInstrumento 	= $scope.data.prtModificado.IdTipoInstrumentoSubyacente || ''; 
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


					} else if ($scope.data.prtModificado.IdClasificacionTipoInstrumentoSubyacente == "RF" ){
						
						var laCantidad = Number($scope.data.KngCantidad) || 0;
						var elCorteMin = $scope.data.prtModificado.CorteMinimo || 1;
						
						if (laCantidad > 0 && elCorteMin > 1) {
							if(laCantidad % elCorteMin !==0){
								bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("CORTE_MINIMO_INVALIDO"));
								$scope.data.KngCantidad = 0;
								return;								
							}
						}
						if (valorizadorEnEjecucion) return;
						
						
						$scope.data.yaValorizo = false;
					}
			}

			if ($scope.data.kngCantidad === null) 
				$scope.data.kngCantidad = 0;
		};	

		bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise('CUS.UsaFechaLiquidacionEnVencimiento', $scope.SesionIdEmpresa, 'PLATAFORMA')
				.then(function (parametroUsaFechaLiquidacion) {
					$scope.data.UsaFechaLiquidacionEnVencimiento = parametroUsaFechaLiquidacion.Valor === 'S';
				});

		//Calcula Interes Diario = (Valor Inicial * Prima)/30
		var calcularInteresDiario = function(monto, tasa)
		{
			return Math.ceil(Number((monto * (tasa/100)) / 30));
		};	 
		
		var habilitarCamposParaModificacion = function ()
		{
			
			// Prestamo Fines Generales (se puede modificar Precio y Prima cualquier dia)
			if ( $scope.data.prtModificado.IdTipoPrestamo === "PRTFIN" || $scope.data.prtModificado.IdTipoPrestamo === 'PRFGRF' )
			{
				$scope.data.precioDisabled 			 	= false;
				$scope.data.porcentajePrimaDisabled  	= false;
				$scope.data.fechaVencimientoDesactivado	= true;
			}
			
			// Prestamo Administrativo (se puede modificar Precio, Prima y Plazo el dia del prestamo o el dia siguiente)
			if ( $scope.data.prtModificado.IdTipoPrestamo === "PRTADM" || $scope.data.prtModificado.IdTipoPrestamo === 'PRADRF' )
			{
				//Plazo y prima solo se pueden modificar el dia del ingreso del prestamo
				$scope.data.porcentajePrimaDisabled  	= (util.dateSPdate($scope.data.prtModificado.INGFechaOperacion) == util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema))) ? false : true;	
				$scope.data.fechaVencimientoDesactivado = true;
				
				//Precio se puede modificar hasta el dia siguiente del ingreso del prestamo
				$scope.data.precioDisabled 			 	= (util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema)) >= util.dateSPdate($scope.data.prtModificado.INGFechaOperacion) && util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema)) <= util.dateSPdate(util.dateInicioDia($scope.data.diaSiguienteIngresoPrestamo))) ? false : true;			
			}
			
			
			// Prestamo VENTA CORTA con Custodia INTERNA (Cliente tiene contrato de custoria)
			if (($scope.data.prtModificado.IdTipoPrestamo === 'PRTVTC' || $scope.data.prtModificado.IdTipoPrestamo === 'PRVCRF') && !$scope.data.operaCustodiaExterna)
			{
				//Precio y tasa se pueden modificar hasta el dia siguiente del ingreso del prestamo
				$scope.data.precioDisabled 			 = (util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema)) >= util.dateSPdate($scope.data.prtModificado.INGFechaOperacion) && util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema)) <= util.dateSPdate(util.dateInicioDia($scope.data.diaSiguienteIngresoPrestamo))) ? false : true;		
				$scope.data.tasaDisabled 			 = (util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema)) >= util.dateSPdate($scope.data.prtModificado.INGFechaOperacion) && util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema)) <= util.dateSPdate(util.dateInicioDia($scope.data.diaSiguienteIngresoPrestamo))) ? false : true;			

				//Prima solo se pueden modificar el dia del ingreso del prestamo
				$scope.data.porcentajePrimaDisabled  = (util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema)) >= util.dateSPdate($scope.data.prtModificado.INGFechaOperacion) && util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema)) <= util.dateSPdate(util.dateInicioDia($scope.data.diaSiguienteIngresoPrestamo))) ? false : true;			
				$scope.data.fechaVencimientoDesactivado	= true;
			}	
			
			// Prestamo VENTA CORTA con Custodia EXTERNA (Cliente no tiene contrato de custoria)
			if (($scope.data.prtModificado.IdTipoPrestamo === 'PRTVTC' || $scope.data.prtModificado.IdTipoPrestamo === 'PRVCRF') && $scope.data.operaCustodiaExterna)
			{
				//Plazo y prima solo se pueden modificar el dia del ingreso del prestamo
				$scope.data.porcentajePrimaDisabled  	= (util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema)) >= util.dateSPdate($scope.data.prtModificado.INGFechaOperacion) && util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema)) <= util.dateSPdate(util.dateInicioDia($scope.data.diaSiguienteIngresoPrestamo))) ? false : true;			
				$scope.data.fechaVencimientoDesactivado	= true;
				
				//Precio se puede modificar hasta el dia siguiente del ingreso del prestamo
				$scope.data.precioDisabled 			 	= (util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema)) >= util.dateSPdate($scope.data.prtModificado.INGFechaOperacion) && util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema)) <= util.dateSPdate(util.dateInicioDia($scope.data.diaSiguienteIngresoPrestamo))) ? false : true;
				$scope.data.tasaDisabled 			 	= (util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema)) >= util.dateSPdate($scope.data.prtModificado.INGFechaOperacion) && util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema)) <= util.dateSPdate(util.dateInicioDia($scope.data.diaSiguienteIngresoPrestamo))) ? false : true;			
			}	
			
		};	

		var consultarContratoCustodia = function() {			
			var datos = {
		       	p_IdEmpresa 	: $scope.SesionIdEmpresa,
		        p_IdUsuario 	: $scope.SesionIdUsuario,
		        p_IdModulo 		: 'CUS',
		        p_IdMercado 	: $scope.data.prtModificado.IdClasificacionTipoInstrumento,
				p_IdCliente 	: $scope.data.prtModificado.IdCliente,
				p_IdPortafolio 	: $scope.data.prtModificado.IdPortafolio
			};
			
			bcsServiciosPrestamo.ConsultarContratoCustodia(datos,function(result)
			{
				if (result.P_OUT_TIENECONTRATO == 'N')
				{	
					$scope.data.operaCustodiaExterna = true;    //Usa custodia externa
			    } 
				else 
				{
			    	$scope.data.operaCustodiaExterna = false;	//Usa custodia interna				
			    }
				
				habilitarCamposParaModificacion();	
			},function(result) {
				console.log(result);
			});
			
			
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
				
				$scope.data.kngInteresDiario    = calcularInteresDiario($scope.data.kngMonto, $scope.data.kngPorcentajePrima);
				$scope.data.kngInteresAcumulado = util.calcularInteresAcumulado($scope.data.kngInteresDiario, util.dateInicioDia($scope.data.prtModificado.FechaPrestamo), util.dateInicioDia($scope.SesionFechaSistema));
				if ( $scope.data.prtModificado.IdTipoPrestamo === 'PRTVTC' || $scope.data.prtModificado.IdTipoPrestamo === 'PRVCRF' || $scope.data.prtModificado.IdTipoPrestamo === 'PRTADM' ) {
					$scope.data.porcentajePrimaDisabled	= newValue[2] === oldValue[2];
				}
			}
			
			
		});

		/*********************************************
		 * GO!
		 *********************************************/
	 	//Primer evento - filtro inicial
		$timeout(function(){
			bcsComunes.ventanaTamanoAutomatico(idVentana);

			if($scope.data.prtModificado.IdTipoPrestamo === 'PRTVTC' || $scope.data.prtModificado.IdTipoPrestamo === 'PRVCRF')
			{
				consultarContratoCustodia();				
			}
			else
			{
				habilitarCamposParaModificacion();					
			}
					
					
		},1300);
		
		
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
				FechaValorizacion			: util.dateSPdate($scope.data.prtModificado.FechaLiquidacion),
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
				$scope.data.kngMonto	= Number(item.MontoMonedaSistema);
				$scope.data.kngMontoMdaInstr	= Number(item.MontoMonedaInstrumento);
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
		
		var fnvalorizarRF = function(Cantidad,Precio,Tasa,Monto){
			var Instrumento = $scope.data.prtModificado.IdInstrumentoSubyacente;
			if(!Instrumento){
				valorizando(false);
				return;
			}
			

			var IdOperacion = "INGRESO";			
			var FechaVencimientoIns=util.dateSPdate($scope.data.prtModificado.FechaVencimientoInstrumento);
			var Plazo=$scope.data.kngPlazoInstr;
			
			if(!FechaVencimientoIns || FechaVencimientoIns==$scope.$root.SesionFechaSistema){
				valorizando(false);
				return;
			}			
			var IdClasificacionTipoInstrumento = "RF";
			var Cantidad 			= Cantidad !== undefined ? Cantidad : 0;
			var Tasa 				= Tasa !== undefined ? Tasa : 0;
			var Precio	 			= Precio !== undefined ? Precio :0;
			var Monto 				= Monto !== undefined ? Monto : 0;
			var PrecioReferencia 	= 0;
			var TasaReferencia 		= 0;
			
					
			var condicion;
			if(Cantidad>0&&Precio>0&&Tasa===undefined&&(Monto===undefined||Monto===0)) condicion='A';
			else if(Cantidad>0&&Tasa>0&&Precio===undefined&&(Monto===undefined||Monto===0)) condicion='B';
			else if(Cantidad>0&&Tasa>=0&&Precio>=0) condicion='TODOS';
			else{
				valorizando(false);
				return;
			}

			var valorizarMercado=false;
			var cval=generaCabeceraValorizar($scope.data.monedaInstrumento);	
			var dvalCompra=generaBaseDetalleWS(1);			

			dvalCompra.Cantidad							=Cantidad;
			dvalCompra.IdTipoInstrumento				=$scope.data.prtModificado.IdTipoInstrumentoSubyacente;
			dvalCompra.IdInstrumento					=Instrumento;
			dvalCompra.Nemotecnico						=$scope.data.prtModificado.NemoSubyacente;
			dvalCompra.FechaEmision						=$scope.data.prtModificado.FechaEmisionInstrumento;
			dvalCompra.FechaVencimiento					=FechaVencimientoIns;
			dvalCompra.IdMonedaInstrumento				=$scope.data.prtModificado.IdMonedaSubyacente;
			dvalCompra.IdMonedaEmisionInstrumento		=$scope.data.prtModificado.IdMonedaEmisionSubyacente;
			dvalCompra.IdTipoReajusteInstrumento		=$scope.data.prtModificado.IdTipoReajusteSubyacente;
			dvalCompra.ValorRescate						=null;
			dvalCompra.TasaCompra						=Tasa||null;
			dvalCompra.PrecioCompra						=Precio||null;				
			dvalCompra.MontoCompra						=Monto||null;
			dvalCompra.Emisor 							=$scope.data.prtModificado.IdEmisorSubyacente; //Instrumento.IdEmisor;
			dvalCompra.IdCondicionDeLiquidacion			=$scope.data.condicionLiquidacionSeleccionada.IdCondicionDeLiquidacion||null;
			dvalCompra.IdClasificacionTipoInstrumento	=IdClasificacionTipoInstrumento;
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
