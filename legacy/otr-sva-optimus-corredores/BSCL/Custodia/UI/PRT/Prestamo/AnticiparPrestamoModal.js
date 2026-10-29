/*************************************************************
 * Purpose	: Anticipar prestamos
 *************************************************************/
(function() {
	angular.module('ModuloCustodia').controller('AnticiparPrestamoModalCtrl',
	function ($rootScope,$scope,$translate,$timeout,bcsComunes,bcsServiciosCustodia,bcsServiciosValorizacion,
			bcsServicioPlataforma,bcsServicioCalcularMontoTipoInstrumento,bcsServiciosPrestamo, bcsConfiguracionPRT) {
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var idVentana = bcsComunes.ventanaWinId($scope);
		var prtAnticipado = bcsComunes.ventanaParams(idVentana);  
		var modMtoPlazo = false;
		
		var miFechaSis = $scope.SesionFechaSistema;
		$scope.data = {};
		$scope.data.oDecimales={};
		$scope.data.oDecimales.corteMinimo={};
		$scope.data.oDecimales.corteMinimo.decimales = 0; 
		/*********************************************
		 * CARGA INICIAL
		 ********************************************/
		$scope.data.cantidadOptions 			= {format: 'n'+(util.bcs_formato(0,'GEN','Cantidad',prtAnticipado.IdMonedaSubyacente,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.precioOptions 				= {format: 'c'+(util.bcs_formato(0,'GEN','Precio',prtAnticipado.IdMonedaSubyacente,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.tasaOptions 				= {format: 'n'+(util.bcs_formato(0,'GEN','Tasa',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:-999999999999999999999,spinners: false};
		$scope.data.montoOptions 				= {format: 'c'+(util.bcs_formato(0,'GEN','Monto',prtAnticipado.IdMonedaSubyacente,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.plazoOptions 				= {format: 'n0',min:0, max:prtAnticipado.MaximoDiasPrestamo, spinners: false};
		$scope.data.porcentajePrimaOptions 		= {format: 'n'+(util.bcs_formato(0,'GEN','Tasa',prtAnticipado.IdMonedaSubyacente,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.montoPrimaOptions 			= {format: 'c'+(util.bcs_formato(0,'GEN','Monto',prtAnticipado.IdMonedaSubyacente,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.montoPlazoOptions 			= {format: 'c'+(util.bcs_formato(0,'GEN','Monto',prtAnticipado.IdMonedaSubyacente,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.accionDisabled 				= true;
		$scope.data.kngDiasCobradosPrestatario 	= 0;
		
		$scope.data.oShow = {};
		$scope.data.oShow.nombreCliente 			= prtAnticipado.RazonSocial;
		$scope.data.oShow.NombreTipoPrestamo 		= prtAnticipado.NombreTipoPrestamo;
		$scope.data.oShow.checkCobroAlInicio 		= prtAnticipado.CobraPrimaAlInicio=="S";
		$scope.data.oShow.folioPrestamo 			= prtAnticipado.FolioPrestamo;
		$scope.data.oShow.checkCustodiaExterna 		= false;
		$scope.data.oShow.Nemotecnico 				= prtAnticipado.NemoSubyacente;
		$scope.data.oShow.kngCantidad 				= prtAnticipado.INGCantidad;
		$scope.data.oShow.kngPrecio 				= prtAnticipado.INGPrecioInstrumento;
		$scope.data.oShow.kngMonto 					= prtAnticipado.INGCantidad * prtAnticipado.INGPrecioInstrumento;
		$scope.data.oShow.kngFechaVencimiento 		= prtAnticipado.FechaVencimientoPrestamo;
		$scope.data.oShow.kngPorcentajePrima 		= prtAnticipado.TasaPrestamo;
		$scope.data.monedaInstrumento				= prtAnticipado.IdMonedaInstrumento;
		$scope.data.oShow.kngMontoPrima 			= 0;
		$scope.data.oShow.kngMontoPlazoPrestatario 	= 0;
		$scope.data.oShow.kngTotalPremio 			= 0;
		$scope.data.oShow.kngPlazoPrestatario 		= 0;
		$scope.data.PorcentajePrimaDisabled			= true;
		$scope.data.ConsultaFlujoCustodia 			= undefined;	
		$scope.data.fechaLiquidacion 				= prtAnticipado.FechaLiquidacion;
		$scope.data.IdCondicionDeLiquidacion		= prtAnticipado.IdCondicionLiquidacion;
		$scope.data.kngCorteMinimo					= prtAnticipado.CorteMinimo; 
		
		const MERCADO_RENTA_FIJA = 'RF';

		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		$scope.data.oConfiguracionTipoInstrumento={
			tipo : "combobox-tipo-instrumento",
			configuracion : {},
			filtros : {
				unico : { "field"   : "IdTipoInstrumento","operator": "eq", "value"   : prtAnticipado.IdTipoInstrumentoSubyacente },
			},
			idActual : prtAnticipado.IdTipoInstrumentoSubyacente,
			seleccionado : false,
			idDefecto : prtAnticipado.IdTipoInstrumentoSubyacente
		};

		$scope.oPortafolio={
			tipo : "combobox-portafolio",
			configuracion :{},
			idActual : prtAnticipado.IdPortafolio,
			idDefecto : prtAnticipado.IdPortafolio,
			filtros: {
				IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  prtAnticipado.IdCliente },
				IdPortafolio : { "field": "IdPortafolio","operator": "eq", "value"   :  prtAnticipado.IdPortafolio }
			},

		};
		
		
		var fnCambioPlazo = function() {
	   		var datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario, 
					p_IdModulo : 'CUS', 
					p_UsaReglasNegocio : 'S',
					p_FechaDesde : $scope.data.UsaFechaLiquidacionEnVencimiento && $scope.data.fechaLiquidacion && $scope.data.fechaLiquidacion > util.dateInicioDia(miFechaSis) ? util.dateSPdate($scope.data.fechaLiquidacion) : util.dateSPdate(util.dateInicioDia(miFechaSis)),
					p_FechaHasta : util.dateSPdate($scope.data.oShow.kngFechaVencimiento),
					p_DiasHabiles : 'N',
					p_IdCondicionDeLiquidacion : 'T0',
					p_IdPais : $scope.SesionIdPaisSistema,
				};
	   		bcsServicioPlataforma.Plazo(datos,
	   				function(result) {
						if (result.P_OUT_ERRORESCONTADOR === 0){
							$scope.data.oShow.kngPlazoPrestatario = result.P_OUT_PLAZODIAS;
							modMtoPlazo = false;
							$scope.data.kngDiasCobradosPrestatario = prtAnticipado.PlazoEnDiasOperacion - Number($scope.data.oShow.kngPlazoPrestatario);
						}
					},
					function(result){}
			);
		};
		
		CalcularPremios = function() {
			var datos = {};
			//Rescata valores ingresados en formulario
	   		datos.p_IdEmpresa 		= $scope.SesionIdEmpresa;
	   		datos.p_IdUsuario 		= $scope.SesionIdUsuario;
	   		datos.p_IdModulo 		= "CUS";
	   		datos.p_IdPais			= $scope.SesionIdPaisSistema;
	   		datos.p_FolioPrestamo 	= Number(prtAnticipado.FolioPrestamo) || 0;
	   		datos.p_FechaAnticipo 	= util.dateSPdate(util.date(miFechaSis).toDate());
	   		datos.p_Precio 			= $scope.data.oShow.kngPrecio;
	   		datos.p_CantidadAnticipo= $scope.data.oShow.kngCantidad;

	 		bcsComunes.ventanaOverlay(idVentana,true);
	 		bcsServiciosPrestamo.CalcularPremios(datos,function(result)
			{
				bcsComunes.ventanaOverlay(idVentana,false);
				if (result.P_OUT_TOTALPRIMA){
					if (typeof result.P_OUT_TOTALPRIMA !== 'undefined'){
						$scope.data.oShow.kngTotalPremio = Number(result.P_OUT_TOTALPRIMA);
					}
				}
			},function(result) {
				$scope.data.oShow.kngTotalPremio = Number(0);
				bcsComunes.ventanaOverlay(idVentana,false);
			});
		};	
		
		bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise('CUS.UsaFechaLiquidacionEnVencimiento', $scope.SesionIdEmpresa, 'PLATAFORMA')
		.then(function (parametroUsaFechaLiquidacion) {
			$scope.data.UsaFechaLiquidacionEnVencimiento = parametroUsaFechaLiquidacion.Valor === 'S';
			fnCambioPlazo();
		});
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		$scope.data.btnCerrar = function(refrescar) {
			bcsComunes.ventanaData(idVentana,refrescar);
			bcsComunes.ventanaCerrar(idVentana);
		};
		
		$scope.$watchGroup(['data.oShow.kngCantidad','data.oShow.kngPrecio'], function(newValue, oldValue) {
			if (   typeof $scope.data.oShow.kngPrecio   !== 'undefined' 
				&& typeof $scope.data.oShow.kngCantidad !== 'undefined' 
				&& typeof prtAnticipado.IdInstrumentoSubyacente !== 'undefined')
			{
				if( $scope.data.oShow.kngCantidad > prtAnticipado.INGCantidad) {
					 $scope.data.oShow.kngCantidad = prtAnticipado.INGCantidad;
				}
				if (prtAnticipado.IdTipoInstrumentoSubyacente !== undefined){

					if (prtAnticipado.IdClasificacionTipoInstrumentoSubyacente == "RV"){
						ConsultaFlujo($scope.data.oShow.kngCantidad);
						var datos = {};
						//Rescata valores ingresados en formulario
						//VARIABLES DE ENTRADA OBLIGATORIAS
						datos.p_IdEmpresa 			= $scope.SesionIdEmpresa;
						datos.p_IdUsuario 			= $scope.SesionIdUsuario;
						datos.p_IdModulo 			= 'CUS';
						datos.p_UsaReglasNegocio 	= 'S';

						$scope.data.oShow.kngMonto = 0;

						//ENUMERAR VARIABLES DE ENTRADA
						datos.p_Cantidad 			= $scope.data.oShow.kngCantidad || 0;
						datos.p_Precio 				= $scope.data.oShow.kngPrecio || 0;
						datos.p_Tamano 				= 1;
						datos.p_IdTipoInstrumento 	= prtAnticipado.IdTipoInstrumentoSubyacente || ''; 
						datos.p_AplicaValidaciones 	= 'N';

						bcsComunes.ventanaOverlay(idVentana,true);
						bcsServicioCalcularMontoTipoInstrumento.llamar(datos,function(result){
							bcsComunes.ventanaOverlay(idVentana,false);
							if (result.P_OUT_ERRORESCONTADOR === 0) {
								$scope.data.oShow.kngMonto = Number(result.P_OUT_MONTO);
								CalcularPremios();
							}
							else {
								$scope.data.oShow.kngMonto = 0;
								console.log("Error." + result.P_OUT_ERRORESCONTADOR);
							}
						},function(result) {
							bcsComunes.ventanaOverlay(idVentana,false);
							console.log(JSON.stringify(result));
							console.log("revisar");
						});
						bcsComunes.ventanaTamanoAutomatico(idVentana);


					} else if ( prtAnticipado.IdClasificacionTipoInstrumento == MERCADO_RENTA_FIJA ) {
						
						$scope.data.muestraDatosRF = true;
						var laCantidad = Number($scope.data.oShow.kngCantidad) || 0;
						var elCorteMin = prtAnticipado.CorteMinimo || 1;
						
						if (laCantidad > 0 && elCorteMin > 1) {
							if(laCantidad % elCorteMin !==0){
								bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("CORTE_MINIMO_INVALIDO"));
								$scope.data.oShow.kngCantidad = 0;
								return;								
							}
						}			
							$scope.data.oShow.kngMonto 	= prtAnticipado.INGMontoCompraActualMonedaInstrumento * ($scope.data.oShow.kngCantidad/prtAnticipado.INGCantidad);
							CalcularPremios();
					}
				}
			}

			if ($scope.data.oShow.kngCantidad === null) 
				$scope.data.oShow.kngCantidad = 0;
		} );
		
		
		$scope.data.fnAnticipar = function(){
			bcsComunes.ventanaOverlay(idVentana,true);
			var registro = [];
			registro.push({
				FolioPrestamo 				: Number(prtAnticipado.FolioPrestamo) || 0,
				FechaAnticipo 				: util.dateSPdate(util.date(miFechaSis).toDate()) || '1900-01-01',
				IdPais 						: $scope.SesionIdPaisSistema || 0,
				IdMonedaLiquidacion 		: $scope.SesionIdMonedaSistema || '',
				IdCondicionLiquidacion 		: 'T0',
				FechaLiquidacion 			: util.dateSPdate(util.date(miFechaSis).toDate()) || '1900-01-01',
				DevolverCustodia 			: 'S',
				IdEmpleadoDigitador 		: $scope.SesionIdEmpleado,
				Cantidad 					: $scope.data.oShow.kngCantidad, //$scope.data.oShow.kngCantidad, //fmanzana-20190916: parcial //prtAnticipado.INGCantidad, 
				PrecioInstrumento		 	: $scope.data.oShow.kngPrecio,
				TasaAnticipo 				: Number($scope.data.oShow.kngPorcentajePrima),
				IdBolsa						: $scope.SesionBolsaDefectoSistema,
				MontoPrima 					: $scope.data.oShow.kngMontoPrima,
				MontoPremio					: $scope.data.oShow.kngTotalPremio,
				Monto 						: $scope.data.oShow.kngMonto,
				MontoAPlazo 				: $scope.data.oShow.kngMontoPlazoPrestatario,
				FolioSistemaExterno			: prtAnticipado.FolioSistemaExterno //JALA al 30/05/2019 para anticipar Prestamo BlockChain
				
			});
		
			var datos = {};	
			 
			datos.p_IdEmpresa = $scope.SesionIdEmpresa;
			datos.p_IdUsuario = $scope.SesionIdUsuario;
			datos.p_IdModulo = 'CUS';
			datos.p_UsaReglasNegocio = 'S';
			datos.p_IdPais = $scope.SesionIdPaisSistema;
			datos.p_DatosPrestamos = registro;
			datos.p_UsaPrecioMedio = 'N';
			datos.p_FechaAnticipo = util.dateSPdate(util.date(miFechaSis).toDate());
		
			bcsServiciosPrestamo.AnticiparPrestamo
				(datos,
				function(respuesta){
					bcsComunes.ventanaOverlay(idVentana,false);
					var a = bcsComunes.ventanaMsg({
								conflictos:util.parseServerResponse(respuesta)
							},$scope.winID);
					
					var promesa=bcsComunes.ventanaPromesa(a);
					var datos = {};
					promesa.then(function(data){
						$scope.data.btnCerrar(true);	
					});
					
					
				},
				function(respuesta){
					bcsComunes.ventanaOverlay(idVentana,false);
					var a = bcsComunes.ventanaMsg({
							conflictos:util.parseServerResponse(respuesta)
						},$scope.winID);
					
					promesa.then(function(data){
							
					});
			});		
		};

		
		/*********************************************
		 * FUNCIONES
		 *********************************************/

		var ConsultaFlujo = function (cantidad){
			
			var tblInstr = [];
			tblInstr.push({ 
	  			 IdClasificacionTipoInstrumento	: prtAnticipado.IdClasificacionTipoInstrumentoSubyacente,
	  			 IdTipoInstrumento				: prtAnticipado.IdTipoInstrumentoSubyacente,
	  			 IdInstrumento					: prtAnticipado.IdInstrumentoSubyacente,
	  			 IdEmisor						: null,
	  			 FechaVencimiento				: null,
	  			 IdTipoReajuste					: null,
	  			 EsFungible						: null,
	  			 IdBoveda						: null
	  		 });
			
			
			var tblAgrupaPor = [];
			tblAgrupaPor.push({
				ClasificacionTipoInstrumento	: 'S',
				TipoInstrumento					: 'S',
				Instrumento						: 'S',
				Emisor							: 'N',
				Nemotecnico						: 'N',
				NemoSVS							: 'N',
				FechaEmisionInstrumento			: 'N',
				FechaVencimientoInstrumento		: 'N',
				FechaLiquidacionInstrumento		: 'N',
				TipoReajuste					: 'N',
				EsFungible						: 'N',
				CodigoIFRS						: 'N',
				Boveda							: 'N',
				Bolsa							: 'N',
				TipoOperacion					: 'N',
				Mesa							: 'N',
				FolioRegistro					: 'N',
				ItemOrigen						: 'N'  
			});	
			
			var tblUso = [];
			tblUso.push({
				UsoTipoOperacion				: 'PREDIS',
				IdCliente						: prtAnticipado.IdCliente,
				IdPortafolio					: prtAnticipado.IdPortafolio,
				BusquedaEspecifica				: 'PRESTAMISTAS_DISPONIBLES',
				IdTipoInstrumentoEspecifico		: null				
			});

			
			var paramInSP = {};
			paramInSP.p_IdEmpresa			= $scope.SesionIdEmpresa;
			paramInSP.p_IdUsuario			= $scope.SesionIdUsuario;
			paramInSP.p_IdModulo			= 'SIS';
			paramInSP.p_FechaSaldo			= miFechaSis;
			paramInSP.p_IdPais				= $scope.SesionIdPaisSistema;
			paramInSP.p_DiasProyeccion		= 0;
			paramInSP.p_DiaHabil			= 'S';
			paramInSP.p_ValorizarFlujo		= 'N';
			paramInSP.p_Uso					= tblUso;
			paramInSP.p_FiltrosBusqueda		= tblInstr;
			paramInSP.p_EsAgrupadoPor		= tblAgrupaPor;
			
			
			bcsServiciosCustodia.ConsultarFlujoInstrumento(paramInSP,
	   				function(result) {
						if (result.P_OUT_ERRORESCONTADOR === 0){
							if (result.P_OUT_SALIDA[0].T0 < cantidad && $scope.data.ConsultaFlujoCustodia === 'S'){
								bcsComunes.msg("error","info",$translate.instant('UI_PRESTATARIO_SIN_CUSTODIA_PARA_ANTICIPAR'));
								$scope.data.accionDisabled = false;
							} else {
								$scope.data.accionDisabled = true
							}
						}
					},
					function(result){}
			);
		};
		ConsultaFlujo(prtAnticipado.INGCantidad);

		var ConsultaConfiguracionConsultaFlujoSistema = function (){
			
			var dataSourceConfiguracion = new kendo.data.DataSource({
				type: "odata",
				serverFiltering:true,
				transport: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1556",
						dataType: "json"
						  }		
						},
					filter: [{ field: "CodigoParametro", operator: "eq", value: 'CUS.ConsultaFlujoCustodia' },
							 { field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa }],
			});

			dataSourceConfiguracion.fetch(function(){
				$scope.$apply(function() {
					$scope.Configuracion = dataSourceConfiguracion.data()[0];
				});
				$scope.data.ConsultaFlujoCustodia = $scope.Configuracion.Valor;
			});
		};
		ConsultaConfiguracionConsultaFlujoSistema();
		
		/*********************************************
		 * WATCHERS
		 *********************************************/
		$scope.$watchGroup(['data.oShow.kngPorcentajePrima','data.oShow.kngMonto','data.oShow.kngMontoPlazoPrestatario','data.oShow.kngPlazoPrestatario'], function (newValue, oldValue){
			if (   typeof $scope.data.oShow.kngMonto !== 'undefined' 
				&& typeof $scope.data.oShow.kngPorcentajePrima !== 'undefined' 
				&& typeof $scope.data.oShow.kngPlazoPrestatario !== 'undefined'
				&& typeof $scope.data.oShow.kngPrecio !== 'undefined')
			{
				modMtoPlazo = false;
				const base = prtAnticipado.IdMonedaInstrumento === 'CLP' ? 30 : 360;
				$scope.data.oShow.kngMontoPlazoPrestatario = bcsComunes.calcularMontoaPlazo($scope.data.oShow.kngMonto,prtAnticipado.PlazoEnDiasOperacion - Number($scope.data.oShow.kngPlazoPrestatario),Number($scope.data.oShow.kngPorcentajePrima), base);
				$scope.data.oShow.kngMontoPrima = Number($scope.data.oShow.kngMontoPlazoPrestatario) - Number($scope.data.oShow.kngMonto);
			}
		});
		
		
		
		$scope.$watchGroup(['data.oShow.kngMontoPrima'],  function (newValue, oldValue){
			if (modMtoPlazo) {
				$scope.data.oShow.kngMontoPlazoPrestatario = $scope.data.oShow.kngMonto + $scope.data.oShow.kngMontoPrima;
			}
			modMtoPlazo = true;
		});
		
	   	
		/*********************************************
		 * GO!
		 *********************************************/
		
		$timeout(function(){
			
			bcsComunes.ventanaTamanoAutomatico(idVentana);
			
		},250);
	});
}());
//------------------------- FIN CONTROLLER --------------------------
