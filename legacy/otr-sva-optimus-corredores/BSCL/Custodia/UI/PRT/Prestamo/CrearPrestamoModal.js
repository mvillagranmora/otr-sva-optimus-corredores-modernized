/*************************************************************
 * Purpose	: Crear Préstamo de Títulos
 * Author	: TEAM CUSTODIA - BCS
 * Date		: 12/07/2016 [Norm.]
 *************************************************************/
 (function() {
	angular.module('ModuloCustodia').controller('CrearPrestamoModalCtrl',
	function ($rootScope,$scope,$translate,$timeout,bcsComunes,bcsServiciosCustodia,
			bcsServicioPlataforma,bcsServicioCalcularMontoTipoInstrumento,bcsServiciosPrestamo) {
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		$scope.data = {};
		$scope.data.muestraDatosRF = false;
		$scope.data.yaValorizo = false;
		$scope.data.MaximoDiasPrestamo = 90;
		$scope.data.MinimoDiasPrestamo = 1;
		$scope.data.checkCustodiaExterna = false;
		$scope.data.monedaCorteMinimo = "";
		$scope.data.monedaSistema = $scope.SesionIdMonedaSistema;
		$scope.data.monedaInstrumento = $scope.SesionIdMonedaSistema;
		$scope.data.FechaVencInstrOptions = {};
		$scope.data.kngCantidad = 0;
		$scope.data.kngPrecio = 0;
		$scope.data.oDecimales={};
		$scope.data.oDecimales.corteMinimo={};
		$scope.data.oDecimales.corteMinimo.decimales = 0;
		$scope.data.nginstrumento="-1";
		
	
		$scope.data.fechaInicialCondicionDeLiquidacion = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
		$scope.data.idTipoOperacion = "PRESTA";
		
		var idVentana = bcsComunes.ventanaWinId($scope);
		$scope.idVentana = idVentana;
		var v_fechaMinimaPrestamo = util.dateInicioDia($scope.SesionFechaSistema);
		var v_fechaMaximaPrestamo = util.dateInicioDia($scope.SesionFechaSistema);
		
		var valorizadorEnEjecucion = false;

		/*********************************************
		 * COMBOBOX
		 *********************************************/
		 $scope.data.oConfiguracionTipoPrestamo={
			tipo : "combobox-tipo-prestamo",
			configuracion : {},
			idActual : false,
			seleccionado : false,
			idDefecto : '',
			itemPostFiltro : undefined
		};

		 bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise('CUS.PrestamosRentaFija', $scope.SesionIdEmpresa, 'PLATAFORMA')
		 .then(function ( parametroPrestamosRentaFija ) {

			 if ( parametroPrestamosRentaFija.Valor === 'S' ) {
				$scope.data.oConfiguracionMercado.filtros = {
					"logic": "or",
					RV : { "field"   : "IdClasificacionTipoInstrumento","operator": "eq", "value"   : "RV" },
					RF : { "field"   : "IdClasificacionTipoInstrumento","operator": "eq", "value"   : "RF" },
				 }
			 }else{
				$scope.data.oConfiguracionMercado.filtros = {
					"logic": "or",
					RV : { "field"   : "IdClasificacionTipoInstrumento","operator": "eq", "value"   : "RV" },
				 }
			 }
		});		
		
		$scope.data.oConfiguracionBolsa={
			tipo : "combobox-bolsa",
			configuracion : {},	
			filtros : {
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
				EstadoBolsaTipoInstrumento : { "field"   : "EstadoBolsaTipoInstrumento","operator": "eq", "value"   : "ACT" }//,
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};
		
		$scope.data.oConfiguracionMercado={
				tipo : "combobox-clasificacion-tipo-instrumento",
				configuracion : {},	
				idActual : false,
				seleccionado : false,
				idDefecto : '',
				filtros : {
					RV : { "field"   : "IdClasificacionTipoInstrumento","operator": "eq", "value"   : "-" }
				}
			};			
			

		$scope.data.oConfiguracionTipoInstrumento={
			tipo : "combobox-tipo-instrumento",
			configuracion : {},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};
		
		
		$scope.oClientePortafolio={
			tipo : "combobox-cliente-portafolio",
			configuracion 	: {},
			idActual 		: false,
			seleccionado 	: false,
			filtros: {
				IdEmpresa 		: {"field": "IdEmpresa","operator": "eq", "value"   		:  $scope.SesionIdEmpresa },
				IdEstado  		: {"field": "IdEstado","operator": "eq", "value"   			:  "ACT"},
				EsContraparte   : {"field": "EsContraparte","operator": "eq", "value"   	:  ""}
					
			},
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
		
	
		/*********************************************
		 * CARGA INICIAL
		 ********************************************/
		$scope.data.kngPlazo 				= $scope.data.MinimoDiasPrestamo;
		$scope.data.cantidadOptions 		= {format: 'n0',min:0,spinners: false};
		$scope.data.tasaOptions 			= {format: 'n'+(util.bcs_formato(0,'GEN','Tasa',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:-999999999999999999999,spinners: false};
		$scope.data.precioOptions 			= {format: 'n'+(util.bcs_formato(0,'GEN','Precio',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.montoOptions 			= {format: 'c'+(util.bcs_formato(0,'GEN','Monto',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.plazoOptions 			= {format: 'n0',min:0, max:$scope.data.MaximoDiasPrestamo, spinners: false};
		$scope.data.porcentajePrimaOptions 	= {format: 'n'+(util.bcs_formato(0,'GEN','Tasa',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.montoPrimaOptions 		= {format: 'c'+(util.bcs_formato(0,'GEN','Monto',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.montoPlazoOptions 		= {format: 'c'+(util.bcs_formato(0,'GEN','Monto',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.accionDisabled 			= true;

		$scope.data.fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.kngFechaVencimiento= undefined;

		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		$scope.data.btnCerrar = function(refrescar) {
			bcsComunes.ventanaData(idVentana,refrescar);
			bcsComunes.ventanaCerrar(idVentana);
		};

		$scope.data.crearPrestamo = function()
	    {
			
			if (!$scope.oClientePortafolio || !$scope.data.oConfiguracionTipoPrestamo) return;
			
			if ($scope.oClientePortafolio.seleccionado !== undefined && $scope.data.oConfiguracionTipoPrestamo.seleccionado !== undefined){
				if ($scope.oClientePortafolio.seleccionado.EsCuentaPropia != 'S' && $scope.data.oConfiguracionTipoPrestamo.seleccionado.SoloCuentaPropia == 'S'){
					bcsComunes.ventanaMsg({
					    mensaje:$translate.instant("UI_PRTADM_SOLO_CTA_PROPIA")
					  },idVentana);

					$scope.oClientePortafolio.seleccionado = undefined;
					$scope.oClientePortafolio.idActual = false;
					$scope.oClientePortafolio.filtros = { IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  -547548 } };
					return;
					
				}
			}
			
			
			
			if (!$scope.data.checkCustodiaExterna && ($scope.data.oConfiguracionTipoPrestamo.seleccionado.IdTipoPrestamo == 'PRTVTC' || $scope.data.oConfiguracionTipoPrestamo.seleccionado.IdTipoPrestamo == 'PRVCRF'))
			{
				bcsComunes.ventanaMsg({mensaje: $translate.instant("UI_PRESTAMO_SOLO_CLIENTE_CUSTODIA_EXTERNA")},$scope.idVentana);
				return;
			}
			
			if ($scope.data.oConfiguracionTipoPrestamo.seleccionado.IdTipoPrestamo == 'PRTADM' && $scope.oClientePortafolio.seleccionado.EsCuentaPropia == 'N')
			{
				bcsComunes.ventanaMsg({mensaje: $translate.instant("UI_PRESTAMO_SOLO_CORREDORA_CUENTA_PROPIA")},$scope.idVentana);
				return;
			}
			
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
			
			
			if ($scope.data.oConfiguracionMercado.seleccionado.IdClasificacionTipoInstrumento == 'RF'){
				var laCantidad = Number($scope.data.kngCantidad) || 0;
				var elCorteMin = $scope.data.instrumento.CorteMinimo || 1;
				
				if (laCantidad > 0 && elCorteMin > 1) {
					if(laCantidad % elCorteMin !==0){
						bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("CORTE_MINIMO_INVALIDO"));
						$scope.data.kngCantidad = 0;
						return;								
					}
				}
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
						bcsComunes.ventanaMsgErr($translate.instant('UI_FECHA_VENCIMIENTO_INHABIL'),$scope.ventanaId);
					} else {

						var datos = {};	
						bcsComunes.ventanaOverlay(idVentana,true); 
						$scope.data.accionDisabled = undefined;
						
						datos.p_IdEmpresa = $scope.SesionIdEmpresa;
						datos.p_IdUsuario = $scope.SesionIdUsuario;
						datos.p_IdEmpleadoDigitador = $scope.SesionIdEmpleado;
						datos.p_IdModulo = 'CUS';
						datos.p_UsaReglasNegocio = 'S';
						
						datos.p_IdClienteSolicita 		= $scope.oClientePortafolio.seleccionado.IdCliente || -1;
						datos.p_IdPortafolioSolicita	= $scope.oClientePortafolio.seleccionado.IdPortafolio || -1;
						datos.p_IdBolsa 				= $scope.data.oConfiguracionBolsa.IdActual || '';
						datos.p_FechaMovimiento 		= util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema)) || '1900-01-01';
						datos.p_FechaVencimiento 		= util.dateSPdate($scope.data.kngFechaVencimiento) ||'1900-01-01';
						datos.p_IdInstrumentoPrestamo 	= $scope.data.oConfiguracionTipoPrestamo.seleccionado.IdInstrumentoPrestamo || 0;
						datos.p_IdInstrumento 			= $scope.data.instrumento.IdInstrumento || 0;
						datos.p_IdMercado 				= $scope.data.oConfiguracionMercado.seleccionado.IdClasificacionTipoInstrumento;
						datos.p_Nemo 					= $scope.data.instrumento.Nemotecnico || '';
						datos.p_Cantidad 				= Number($scope.data.kngCantidad) || 0;
						datos.p_Precio 					= Number($scope.data.kngPrecio) || 0;
						datos.p_Monto 					= Number($scope.data.kngMonto) || 0;
						datos.p_MontoPrima 				= Number($scope.data.kngMontoPrima) || 0;
						datos.p_TasaPrima 				= Number($scope.data.kngPorcentajePrima) || 0;
						datos.p_TasaInstrumento 		= Number($scope.data.kngTasa) || 0;
						datos.p_MontoAPlazo 			= Number($scope.data.kngMontoPlazo) || 0;
						datos.p_ConGarantiaEspecifica 	= ($scope.data.checkConGarantiasEspecificas === true)? 'S' : 'N';
				       	datos.p_FolioEntidad 			= $scope.data.folioEntidad || '';
						datos.p_FolioTransaccion 		= $scope.data.folioTransaccion || '';
						($scope.data.checkCobroAlInicio)? datos.p_CobraPrimaAlInicio = 'S': datos.p_CobraPrimaAlInicio = 'N';
						($scope.data.checkTipoCustodia)? datos.p_TipoCustodia = 'S': datos.p_TipoCustodia = 'N';
						datos.p_IdAgente 				= $scope.SesionIdUsuario; //TODO --ver cuando este el modulo de agente
						datos.p_IdSucursal 				= 1; //TODO --ver cuando este el modulo de agente
						datos.p_PlazoEnDias 			= Number($scope.data.kngPlazo) || 0;
						datos.p_IdTipoCanalIngreso 		= $scope.SesionTipoCanalIngreso || '';
						datos.p_IdMonedaSistema 		= $scope.SesionIdMonedaSistema || '';
						datos.p_esNominal 				= ($scope.data.checkCustodiaExterna === true ? 'S' : 'N');  
						datos.p_TipoCustodia			= ($scope.data.checkTipoCustodia === true ? 'S' : 'N');  
						
					
						datos.p_IdCondicionLiquidacion 	= $scope.data.condicionLiquidacionSeleccionada.IdCondicionDeLiquidacion || '';
						datos.p_FechaLiquidacion 		= util.dateSPdate(util.dateInicioDia($scope.data.fechaLiquidacion)) || '0001-01-01';										
					
						
						
				   		bcsServiciosPrestamo.CrearPrestamo(datos,successCrearPrestamo,errorCrearPrestamo); 
					}
				}
			}
			,
			function(result) {});
			
	   	};
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		 $scope.data.tipoPrestamoSeleccionado = function (newval, oldval) {
			
			if (typeof $scope.data.oConfiguracionTipoPrestamo.seleccionado !== 'undefined' && 
				$scope.data.oConfiguracionTipoPrestamo.seleccionado !== false ){
				
				$scope.data.checkCobroAlInicio = ($scope.data.oConfiguracionTipoPrestamo.seleccionado.CobraPrimaAlInicio==='S')?true:false;
				$scope.data.checkTipoCustodia = ($scope.data.oConfiguracionTipoPrestamo.seleccionado.TipoCustodia==='S')?true:false;
				$scope.data.MaximoDiasPrestamo = $scope.data.oConfiguracionTipoPrestamo.seleccionado.MaximoDiasPrestamo;
				$scope.data.MinimoDiasPrestamo = $scope.data.oConfiguracionTipoPrestamo.seleccionado.MinimoDiasPrestamo;
				
				if ($scope.data.oConfiguracionTipoPrestamo.seleccionado.MaximoDiasPrestamo !== 0){				
					$scope.data.plazoOptions = {format: 'n0',min:$scope.data.MinimoDiasPrestamo, max:$scope.data.MaximoDiasPrestamo, spinners: false};
					bcsComunes.ventanaOverlay(idVentana,true);

					//--------- FECHA MINIMA -------\\
					calculaFechaVencimiento($scope.data.MinimoDiasPrestamo || 0, 'min');
					//--------- FECHA MAXIMA -------\\
					calculaFechaVencimiento($scope.data.MaximoDiasPrestamo || 0, 'max');
	
					bcsComunes.ventanaOverlay(idVentana,false);
					
				}else{

					$scope.data.MaximoDiasPrestamo = 1;
					$scope.data.MinimoDiasPrestamo = 1;
					$scope.data.checkCobroAlInicio = true;
					$scope.data.kngFechaVencimiento = util.dateInicioDia($scope.SesionFechaSistema);
					$scope.data.FechaVencimientoOptions = {min:v_fechaMinimaPrestamo,max:v_fechaMaximaPrestamo};
				}

			}else{
				$scope.data.MaximoDiasPrestamo = 1;
				$scope.data.MinimoDiasPrestamo = 1;
				$scope.data.checkCobroAlInicio = true;
				$scope.data.kngFechaVencimiento = util.dateInicioDia($scope.SesionFechaSistema);
				$scope.data.FechaVencimientoOptions = {min:v_fechaMinimaPrestamo,max:v_fechaMaximaPrestamo};
			}		
		};

		var calculaFechaVencimiento = function(plazo, tipo) {
			var datos = {
				p_IdEmpresa 			: $rootScope.SesionIdEmpresa,
				p_IdUsuario 			: $rootScope.SesionIdUsuario,
				p_IdPais 				: $rootScope.SesionIdPaisSistema,
				p_IdModulo 				: 'PLA',
				p_UsaReglasNegocio 		: 'S',
				p_FechaIngreso 			: $scope.SesionFechaSistema,
				p_IdTipoOperacion 		: $scope.data.idTipoOperacion,
				P_IdCondicionDeLiquidacion : 'T0',
				p_Plazo 				: plazo || 0,
				p_DiasHabiles 			: $scope.data.oConfiguracionTipoPrestamo.seleccionado.EsDiaHabilPlazoPrestamo 
			};
			if($scope.data.fechaLiquidacion !== undefined){
				datos.p_FechaIngreso=($scope.data.UsaFechaLiquidacionEnVencimiento)? util.dateSPdate($scope.data.fechaLiquidacion): $scope.SesionFechaSistema;
			};

			bcsServicioPlataforma.ConsultarFechaVencimiento(datos).then(function(result)
			{
				if (result.P_OUT_ERRORESCONTADOR === 0){
					$scope.data.FechaVencimientoOptions = tipo === 'min' ? 
						{min: util.dateInicioDia(result.P_OUT_FECHAVENCIMIENTO)} : 
						{max: util.dateInicioDia(result.P_OUT_FECHAVENCIMIENTO)};					
				}else{
					console.log("ERROR: " + JSON.stringify(result));
				}
			}
			,function(result){
				console.log("ERROR: " + JSON.stringify(result));
			});	
		};

	 	var successCrearPrestamo = function(respuesta)
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
		
		var errorCrearPrestamo = function(respuesta)
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
				&& typeof $scope.data.kngCantidad !== 'undefined' 
				&& typeof $scope.data.instrumento !== 'undefined')
			{
				if ($scope.data.instrumento.IdTipoInstrumento !== undefined){

					if ($scope.data.instrumento.IdClasificacionTipoInstrumento == "RV"){

						var datos = {};
						//Rescata valores ingresados en formulario
						//VARIABLES DE ENTRADA OBLIGATORIAS
						datos.p_IdEmpresa 			= $scope.SesionIdEmpresa;
						datos.p_IdUsuario 			= $scope.SesionIdUsuario;
						datos.p_IdModulo 			= 'CUS';
						datos.p_UsaReglasNegocio 	= 'S';

						$scope.data.kngMonto = 0;

						//ENUMERAR VARIABLES DE ENTRADA
						datos.p_Cantidad 			= $scope.data.kngCantidad || 0;
						datos.p_Precio 				= $scope.data.kngPrecio || 0;
						datos.p_Tamano 				= 1;
						datos.p_IdTipoInstrumento 	= $scope.data.instrumento.IdTipoInstrumento || ''; 
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


					} else if ($scope.data.instrumento.IdClasificacionTipoInstrumento == "RF" ){
						
						var laCantidad = Number($scope.data.kngCantidad) || 0;
						var elCorteMin = $scope.data.instrumento.CorteMinimo || 1;
						
						if (laCantidad > 0 && elCorteMin > 1) {
							if(laCantidad % elCorteMin !==0){
								bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("CORTE_MINIMO_INVALIDO"));
								$scope.data.kngCantidad = 0;
								return;								
							}
						}
						if (valorizadorEnEjecucion) return;
						
						$scope.data.kngMonto = 0;
						$scope.data.yaValorizo = false;
					}
				}
			}

			if ($scope.data.kngCantidad === null) 
				$scope.data.kngCantidad = 0;


		};	
		
		var fnCambioInstrumento = function(newValue, oldValue) {
			if (typeof $scope.data.instrumento !== 'undefined' && typeof $scope.SesionFechaSistema !== 'undefined'){
				$scope.data.kngPrecio = 0;
				
				$scope.data.monedaCorteMinimo = $scope.data.instrumento.IdMoneda;
				$scope.data.monedaInstrumento = $scope.data.instrumento.IdMoneda;
				$scope.data.yaValorizo = false;

				if (!$scope.data.oConfiguracionTipoInstrumento.idActual) 
					$scope.data.oConfiguracionTipoInstrumento.idActual = $scope.data.instrumento.IdTipoInstrumento;
				
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
					if ($scope.data.instrumento.FechaVencimiento < util.dateInicioDia($scope.SesionFechaSistema)){
						bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_INSTRUMENTO_VENCIDO"));
						return;
					}
					if($scope.data.instrumento.FechaVencimiento !== undefined && !util.dateisdefault($scope.data.instrumento.FechaVencimiento)){
						$scope.data.kngFechaVencimientoInstr = util.dateInicioDia($scope.data.instrumento.FechaVencimiento);
						$scope.data.kngPlazoInstr = Math.round(util.datediff(util.dateInicioDia($scope.data.instrumento.FechaVencimiento),util.dateInicioDia($scope.SesionFechaSistema)).asDays());
					}
					$scope.data.kngCorteMinimo = $scope.data.instrumento.CorteMinimo || 1;
					$scope.data.oConfiguracionEmisor.filtros.IdEmisor.operator =  'eq';
					$scope.data.oConfiguracionEmisor.filtros.IdEmisor.value =  $scope.data.instrumento.IdEmisor;
					$scope.data.oConfiguracionEmisor.idActual = $scope.data.instrumento.IdEmisor;
					$scope.data.oConfiguracionEmisor.idPostFiltro = $scope.data.instrumento.IdEmisor;
					
					if ($scope.data.instrumento.IdTipoReajuste !== '') {
						$scope.data.oConfiguracionTipoReajuste.filtros.IdInstrumento.value = Number($scope.data.instrumento.IdInstrumento) || 0;
						$scope.data.oConfiguracionTipoReajuste.idPostFiltro = $scope.data.instrumento.IdTipoReajuste;
						$scope.data.kngIdTipoReajuste = $scope.data.instrumento.IdTipoReajuste;						
					} else {
						$scope.data.oConfiguracionTipoReajuste.idPostFiltro = $scope.data.instrumento.IdTipoReajuste;
						$scope.data.kngIdTipoReajuste = $scope.data.instrumento.IdTipoReajuste;						
					}

					
				}
				
				
				if ($scope.data.instrumento.IdTipoInstrumento !== undefined){
					$scope.data.kngPrecio = Number($scope.data.instrumento.PrecioCierre);
				}else {
					$scope.data.kngPrecio = 0;
				}
				$scope.data.kngCantidad = 0;
			}
		};
				
		var fnCambioCliente = function(newValue, oldValue) {
			if (!$scope.oClientePortafolio.seleccionado) return;
			
			var datosLlamadaA = {
		       	p_IdEmpresa 	: $scope.SesionIdEmpresa,
		        p_IdUsuario 	: $scope.SesionIdUsuario,
		        p_IdModulo 		: 'CUS',
		        p_IdMercado 	: $scope.data.oConfiguracionMercado.seleccionado.IdClasificacionTipoInstrumento,
				p_IdCliente 	: $scope.oClientePortafolio.seleccionado.IdCliente || -345341,
				p_IdPortafolio 	: $scope.oClientePortafolio.seleccionado.IdPortafolio || -345341
			};
			bcsServiciosPrestamo.ConsultarContratoCustodia(datosLlamadaA,function(result)
			{
				if (result.P_OUT_TIENECONTRATO == 'N')
				{	//Usa custodia externa
					$scope.data.checkCustodiaExterna = true;
			    } 
				else 
				{
			    	$scope.data.checkCustodiaExterna = false;
			    	//NO Usa custodia externa
					if (typeof $scope.data.oConfiguracionTipoPrestamo.seleccionado !== 'undefined' &&  $scope.data.oConfiguracionTipoPrestamo.seleccionado !== false )
					{	
						if ($scope.data.oConfiguracionTipoPrestamo.seleccionado.IdTipoPrestamo == 'PRTVTC' || $scope.data.oConfiguracionTipoPrestamo.seleccionado.IdTipoPrestamo == 'PRVCRF')
						{	
							bcsComunes.ventanaMsg({mensaje: $translate.instant("UI_PRESTAMO_SOLO_CLIENTE_CUSTODIA_EXTERNA")},$scope.idVentana);
							return;
						}
					}
					
			    }
			},function(result) {
				
			});
			
			
		};
		
		bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise('CUS.UsaFechaLiquidacionEnVencimiento', $scope.SesionIdEmpresa, 'PLATAFORMA')
				.then(function (parametroUsaFechaLiquidacion) {
					$scope.data.UsaFechaLiquidacionEnVencimiento = parametroUsaFechaLiquidacion.Valor === 'S';
					$scope.data.fechaInicialVencimiento = ($scope.data.UsaFechaLiquidacionEnVencimiento && $scope.data.fechaLiquidacion !== undefined) ? $scope.data.fechaLiquidacion : $scope.data.fechaSistema;
				});

		/*********************************************
		 * WATCHERS
		 *********************************************/
		$scope.$watch('data.oConfiguracionTipoPrestamo.seleccionado', function(newval,oldval)
		 {	
			 if ( newval === false || newval === undefined 
				|| $scope.data.oConfiguracionMercado.seleccionado === false || $scope.data.oConfiguracionMercado.seleccionado === undefined ) return;

			 $scope.oClientePortafolio.seleccionado 	= undefined;
			 $scope.oClientePortafolio.idActual 		= false;
			 $scope.data.checkCustodiaExterna 			= false;

			 if ( $scope.data.oConfiguracionTipoPrestamo.idActual === 'PRTADM' || $scope.data.oConfiguracionTipoPrestamo.idActual === 'PRADRF' ) {
				$scope.oClientePortafolio.filtros = {IdEmpresa      : {"field": "IdEmpresa",     "operator": "eq", "value" :  $scope.SesionIdEmpresa },
					IdEstado       : {"field": "IdEstado",      "operator": "eq", "value" :  "ACT"},
					EsCuentaPropia : {"field": "EsCuentaPropia","operator": "eq", "value" :  'S'} 
				};
				$scope.data.checkTipoCustodiaHabilita = true;
			 }

			 else if ( $scope.data.oConfiguracionTipoPrestamo.idActual === 'PRTFIN' || $scope.data.oConfiguracionTipoPrestamo.idActual === 'PRFGRF' ) {
				$scope.oClientePortafolio.filtros = {
					IdEmpresa 		: {"field": "IdEmpresa","operator": "eq", "value"   		:  $scope.SesionIdEmpresa },
					IdEstado  		: {"field": "IdEstado","operator": "eq", "value"   	:  "ACT"}
				};
 
				$scope.data.checkTipoCustodiaHabilita = false;
			 }

			 else if ( $scope.data.oConfiguracionTipoPrestamo.idActual === 'PRTVTC' || $scope.data.oConfiguracionTipoPrestamo.idActual === 'PRVCRF' ) {
				$scope.oClientePortafolio.filtros = {
					IdEmpresa 		: {"field": "IdEmpresa","operator": "eq", "value"   		:  $scope.SesionIdEmpresa },
					IdEstado  		: {"field": "IdEstado","operator": "eq", "value"   	:  "ACT"},
					EsContraparte	: {"field": "EsContraparte","operator": "eq", "value"   	:  "S"} 
				};
				 $scope.data.checkTipoCustodiaHabilita = true;
			 }

			$scope.data.tipoPrestamoSeleccionado();
			 
		});

		$scope.$watchGroup(['data.kngCantidad','data.kngPrecio','data.kngTasa'], fnCambioPQ );
		
		$scope.$watch('oClientePortafolio.seleccionado', fnCambioCliente, true);
		
		$scope.$watch('data.instrumento', fnCambioInstrumento,true);
				
	   	$scope.$watch('data.oConfiguracionMercado.seleccionado',function(newval,oldval) {
				
	   		if ( newval === oldval || newval === false || newval === undefined )
	   			return;
	   		
			$scope.data.oConfiguracionTipoInstrumento.filtros = {
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "VIG" },
				IdClasificacionTipoInstrumento : { "field"   : "IdClasificacionTipoInstrumento","operator": "eq", "value"   : newval.IdClasificacionTipoInstrumento },
			};

			$scope.data.oConfiguracionTipoPrestamo.filtros = {
				IdClasificacionTipoInstrumento : { "field"   : "IdClasificacionTipoInstrumento","operator": "eq", "value"   : newval.IdClasificacionTipoInstrumento },
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : 'ACT' },
			};

			$scope.data.oConfiguracionTipoPrestamo.itemPostFiltro = '1';

	   		if ($scope.data.oConfiguracionMercado.seleccionado.IdClasificacionTipoInstrumento === 'RF'){
	   			$scope.data.muestraDatosRF = true;
				   $scope.data.oConfiguracionTipoInstrumento.filtros.IdTipoOperacion = { "field" : "IdTipoOperacion", "operator" : "eq", "value" : "VCRF"};
	   		} else {
	   			$scope.data.muestraDatosRF = false;
	   		}
	   		
	   		if ($scope.data.instrumento){
	   			$scope.data.nginstrumento = "-1";
	   		}
	   		$scope.data.yaValorizo = false;
	   		bcsComunes.ventanaTamanoAutomatico(idVentana);
	   		
		});

		$scope.$watch('data.oConfiguracionTipoInstrumento.seleccionado',function(newval,oldval) {	
	   		if (newval === oldval)
	   			return;
	   		
	   		if ($scope.data.instrumento){
	   			$scope.data.nginstrumento = "-1";
	   		}
		});
				
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

		$scope.$watchGroup(['data.fechaLiquidacion'], function (newValue, oldValue){
			if (!newValue[0] && !oldValue[0]) return;
			if (oldValue[0] && util.dateSPdate(newValue[0]) === util.dateSPdate(oldValue[0])) return;
			$scope.data.fechaInicialVencimiento = ($scope.data.UsaFechaLiquidacionEnVencimiento && $scope.data.fechaLiquidacion !== undefined) ? $scope.data.fechaLiquidacion : $scope.data.fechaSistema;
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
				FechaValorizacion			: util.dateSPdate($scope.data.fechaLiquidacion),
				FechaOperacion				: $scope.$root.SesionFechaSistema,
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
		
		var fnvalorizarRF = function(Cantidad,Precio,Tasa,Monto){
			var Instrumento = $scope.data.instrumento || undefined;
			if(!Instrumento){
				valorizando(false);
				return;
			}
			

			var IdOperacion = "INGRESO";			
			var FechaVencimiento=(Instrumento.FechaVencimiento)?util.dateSPdate(Instrumento.FechaVencimiento):undefined;
			var Plazo=$scope.data.kngPlazoInstr;
			
			if(!FechaVencimiento || FechaVencimiento==$scope.$root.SesionFechaSistema){
				valorizando(false);
				return;
			}			
			var IdClasificacionTipoInstrumento = Instrumento.IdClasificacionTipoInstrumento;
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
			var cval=generaCabeceraValorizar(Instrumento.IdMoneda);	
			var dvalCompra=generaBaseDetalleWS(1);			

			dvalCompra.Cantidad							=Cantidad;
			dvalCompra.IdTipoInstrumento				=Instrumento.IdTipoInstrumento;
			dvalCompra.IdInstrumento					=Instrumento.IdInstrumento;
			dvalCompra.Nemotecnico						=Instrumento.Nemotecnico;
			dvalCompra.FechaEmision						=Instrumento.FechaEmision;
			dvalCompra.FechaVencimiento					=FechaVencimiento;
			dvalCompra.IdMonedaInstrumento				=Instrumento.IdMoneda;
			dvalCompra.IdMonedaEmisionInstrumento		=Instrumento.IdMonedaEmision;
			dvalCompra.IdTipoReajusteInstrumento		=$scope.data.kngIdTipoReajuste || Instrumento.IdTipoReajuste;
			dvalCompra.ValorRescate						=null;
			dvalCompra.PrecioCompra						=Precio||null;
			dvalCompra.TasaCompra						=Tasa||null;				
			dvalCompra.MontoCompra						=Monto||null;
			dvalCompra.Emisor 							=IdOperacion=='RETIRO' ? Instrumento.IdEmisor : $scope.data.oConfiguracionEmisor.idActual; //Instrumento.IdEmisor;
			dvalCompra.IdCondicionDeLiquidacion			=$scope.data.condicionLiquidacionSeleccionada.IdCondicionDeLiquidacion||null;
			dvalCompra.IdClasificacionTipoInstrumento	=Instrumento.IdClasificacionTipoInstrumento;
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
				if ( $scope.data.instrumento.IdMonedaEmision.trim() === '' || $scope.data.instrumento.IdMonedaEmision === 'undefined' || $scope.data.instrumento.IdMonedaEmision === undefined || $scope.data.instrumento.IdMonedaEmision === null ) {
					bcsComunes.msg('error',$translate.instant("UI_ERROR"),$translate.instant('UI_INSTRUMENTO_SIN_MONEDA_EMISION'));
					return;
				}
				
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
	
		
/************************************************************************
 *********** FIN FUNCIONES PARA VALORIZACION ****************************
 ************************************************************************/
	});
}());
//------------------------- FIN CONTROLLER --------------------------
