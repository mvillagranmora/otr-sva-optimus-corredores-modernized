/*************************************************************
 * Purpose	: Anticipar Emprestito.
 * Author	: TEAM CUSTODIA - BCS
 * Date		: 13/07/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloCustodia').controller('AnticiparEmprestitoModalCtrl',
	function ($scope,$translate,$timeout,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,
		bcsServicioPlataforma,bcsServicioCalcularMontoTipoInstrumento,bcsServiciosPrestamo,bcsServiciosCustodia) {
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		$scope.data = {};
		$scope.data.plazoAnticipo = 0;
		$scope.data.CantidadMaxima = 0;
		var idVentana = bcsComunes.ventanaWinId($scope);
		var parametros = bcsComunes.ventanaParams(idVentana);
		$scope.data.oDatosEmprestito = parametros;
		var miFechaSis = $scope.SesionFechaSistema;
		$scope.data.oDecimales={};
		$scope.data.oDecimales.corteMinimo={};
		$scope.data.oDecimales.corteMinimo.decimales = 0; 
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		$scope.data.oConfiguracionCondicionLiquidacion={
			tipo : "combobox-condicion-liquidacion",
			configuracion : {},
			filtros : {
				IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   : "" },
				EstadoTipoOperacionYCondicionDeLiquidacion : { "field"   : "EstadoTipoOperacionYCondicionDeLiquidacion","operator": "eq", "value"   : "ACT" },
				IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   : "EMPRES" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};
		
		/*********************************************
		 * CARGA INICIAL
		 ********************************************/
		$scope.data.kngPlazo 						= 0;
		$scope.data.kngDiasCobrados 				= 0;
		$scope.data.folioPrsetamoOptions 			= {format: 'n0',min:0,spinners: false};
		$scope.data.folioEmprestitoOptions 			= {format: 'n0',min:0,spinners: false};
		$scope.data.cantidadOptions 				= {format: 'n'+(util.bcs_formato(0,'CUS','Cantidad',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.cantidadPorAnticiparOptions 	= {format: 'n'+(util.bcs_formato(0,'CUS','Cantidad',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:1,spinners: false};
		$scope.data.precioOptions 					= {format: 'n'+(util.bcs_formato(0,'CUS','Precio',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.montoOptions 					= {format: 'c'+(util.bcs_formato(0,'CUS','Monto',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.plazoOptions 					= {format: 'n0',min:0,spinners: false};
		$scope.data.porcentajePremioOptions 		= {format: 'n'+(util.bcs_formato(0,'CUS','Tasa',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.montoPremioOptions 				= {format: 'c'+(util.bcs_formato(0,'CUS','Monto',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.montoPlazoOptions 				= {format: 'c'+(util.bcs_formato(0,'CUS','Monto',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.kngFolioPrsetamo 				= $scope.data.oDatosEmprestito.FolioPrestamo;
		$scope.data.kngFolioEmprestito 				= $scope.data.oDatosEmprestito.FolioEmprestito;
		$scope.data.tipoPrsetamo	 				= $scope.data.oDatosEmprestito.NombreTipoPrestamo;
		$scope.data.kngFechaInicio 					= util.date($scope.data.oDatosEmprestito.FechaEmprestito).toDate();
		$scope.data.kngFechaVencimiento 			= util.date($scope.data.oDatosEmprestito.FechaVencimientoEmprestito).toDate();
		$scope.data.CantidadMaxima 					= $scope.data.oDatosEmprestito.INGCantidad;
		$scope.data.kngCantidad 					= $scope.data.oDatosEmprestito.INGCantidad;
		$scope.data.tipoInstrumento 				= $scope.data.oDatosEmprestito.NombreTipoInstrumentoSubyacente;
		$scope.data.instrumento 					= $scope.data.oDatosEmprestito.NemoSubyacente;
		$scope.data.checkConGarantiasEspecificas 	= ($scope.data.oDatosEmprestito.UsaGarantiaEspecifica==='S')?true:false;
		$scope.data.prestamista 					= $scope.data.oDatosEmprestito.DocumentoIdentidad + ' ' +  $translate.instant('UI_PORTAFOLIO') + ': ' +  $scope.data.oDatosEmprestito.CodigoPortafolio + ' ' +  $translate.instant('UI_RAZON_SOCIAL') + ': ' + $scope.data.oDatosEmprestito.RazonSocial;
		$scope.data.kngCantidadPorAnticipar 		= $scope.data.oDatosEmprestito.INGCantidad; 
		$scope.data.kngFechaLiquidacion				= util.dateInicioDia(miFechaSis); 
		$scope.data.kngFechaAnticipo 				= miFechaSis;
		$scope.data.kngPorcentajePremio 			= $scope.data.oDatosEmprestito.INGTasaEmprestito;
		$scope.data.kngPrecio 						= Number($scope.data.oDatosEmprestito.INGPrecioInstrumento);
		$scope.data.kngMonto 						= $scope.data.oDatosEmprestito.INGMontoInstrumento;
		$scope.data.kngPorcentajePremio 			= Number($scope.data.oDatosEmprestito.INGTasaEmprestito);
		$scope.data.accionDisabled 					= true;
		$scope.data.PorcentajePremioDisabled		= true;
		$scope.data.IdCondicionDeLiquidacion		= $scope.data.oDatosEmprestito.IdCondicionLiquidacion;
		$scope.data.kngCorteMinimo					= $scope.data.oDatosEmprestito.CorteMinimo;
		$scope.data.monedaInstrumento				= $scope.data.oDatosEmprestito.IdMonedaInstrumento; 

		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		$scope.data.btnCerrar = function(refrescar) {
			bcsComunes.ventanaData(idVentana,refrescar);
			bcsComunes.ventanaCerrar(idVentana);
		};
		
		$scope.data.anticiparEmprestito = function() {
			
			if (Number($scope.data.kngCantidadPorAnticipar) > $scope.data.CantidadMaxima){
				bcsComunes.msg("error","info",$translate.instant('UI_ANTICIPAR_EMPRESTITO_CANTIDAD_SOBRE_MAXIMO'));
				return false;
			}
		
			
			if (Number($scope.data.kngCantidadPorAnticipar) <= 0){
				bcsComunes.msg("error","info",$translate.instant('UI_ANTICIPAR_EMPRESTITO_CANTIDAD_MENOR_CERO'));
				return false;
			}
			
			if (util.dateSPdate($scope.data.kngFechaInicio) >=  util.dateSPdate(util.dateInicioDia(miFechaSis)) ||
				util.dateSPdate(util.dateInicioDia(miFechaSis)) < util.dateSPdate($scope.data.oDatosEmprestito.FechaLiquidacion)) {
				bcsComunes.msg("error","info",$translate.instant("FECHA_ANTICIPO_MAYOR_INGRESO"));
				return false;
			}
			
			bcsComunes.ventanaOverlay(idVentana,true);
			$scope.data.accionDisabled = undefined;
			
			var registro = [];
			registro.push({
				FolioEmprestito 			: Number($scope.data.oDatosEmprestito.FolioEmprestito) || 0,
				FechaEmprestito 			: util.dateSPdate($scope.data.oDatosEmprestito.FechaEmprestito),
				PrecioInstrumento 			: Number($scope.data.kngPrecio) || 0,
				Cantidad 					: Number($scope.data.kngCantidadPorAnticipar) || 0,
				MontoInstrumento 			: Number($scope.data.kngMonto) || 0,
				TasaPremio 					: Number($scope.data.kngPorcentajePremio) || 0,
				MontoPremio 				: Number($scope.data.kngMontoPremio) || 0,
				MontoAPlazo 				: Number($scope.data.kngMontoPlazo) || 0,
				FechaAnticipo 				: $scope.data.kngFechaAnticipo || '1900-01-01',
				PlazoAlVencimiento			: Number($scope.data.kngPlazo) || 0,
				IdPais 						: $scope.SesionIdPaisSistema,
				IdMonedaLiquidacion 		: $scope.SesionIdMonedaSistema || '',
				IdCondicionLiquidacion 		: $scope.data.oConfiguracionCondicionLiquidacion.seleccionado.IdCondicionDeLiquidacion || '',
				FechaLiquidacion 			: $scope.data.kngFechaLiquidacion || '1900-01-01',
				DevolverCustodia 			: 'S',

				IdEmpleadoDigitador 		: $scope.SesionIdEmpleado,
				FolioPrestamo               : Number($scope.data.oDatosEmprestito.FolioPrestamo) || 0				
			
			});
			
			
			var datos = {};
			datos.p_IdEmpresa 					= $scope.SesionIdEmpresa;
			datos.p_IdUsuario 					= $scope.SesionIdUsuario;
			datos.p_IdModulo 					= 'CUS';
			datos.p_UsaReglasNegocio 			= 'S';
			datos.p_IdPais 						= $scope.SesionIdPaisSistema;
			datos.p_DatosEmprestitos 			= registro;
			datos.p_RebajaPrestamo				= 'S';
			
		
			datos.p_AnticiparPrestamoAsociado   = 'N'; 
			datos.p_FechaAnticipo 				=$scope.data.kngFechaAnticipo || '1900-01-01';
			
			bcsServiciosPrestamo.AnticiparEmprestito(datos,function(respuesta){							
							
					let prestamistaSinCartera = respuesta.P_OUT_MENSAJES.find( m => m.tipo === "ERROR" && m.codigo === "NO_SE_ENCONTRO_CARTERA_PARA_RETIRAR");
					bcsComunes.ventanaOverlay(idVentana,false);								
				
					if(prestamistaSinCartera){
						//Mensaje de confirmación
						var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_PRESTATARIO_SIN_CARTERA"), idVentana); // mensaje "Cliente sin custodia. ¿Desea crear nuevo prestamo para anticipar?" 
						t.then(function(result)
						  {
							 if (result===OPCIONES_BCSALERTA.SI)
							   {									
								  bcsComunes.ventanaCerrar(idVentana);
								  CrearPrestamoAnticipo(datos)
								   
							   }
							 else{
								
								let msj=bcsComunes.ventanaMsg({
									conflictos:util.parseServerResponse(respuesta)
								},$scope.winID);
								 
								 bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
								 bcsComunes.ventanaOverlay(idVentana,false);
								 bcsComunes.ventanaCerrar(idVentana);
							 } 
						  });	
					}else{
						bcsComunes.ventanaOverlay(idVentana,false);
						var aaa=bcsComunes.ventanaMsg({
							conflictos:util.parseServerResponse(respuesta)
						},$scope.winID);
						bcsComunes.ventanaPromesa(aaa).then(function(){
							bcsComunes.ventanaData(idVentana, true);
							bcsComunes.ventanaCerrar(idVentana);	
						});
						$scope.data.accionDisabled = true;
					}
				},
				function(respuesta){
					bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);
					bcsComunes.ventanaOverlay(idVentana,false);
					var datos = {};
					$scope.data.accionDisabled = true;
				});			
			
			
	   	};
	   	
	   	/*********************************************
		 * FUNCIONES
		 *********************************************/
		 
		CrearPrestamoAnticipo = function(datos){
			var datosPrestamos={};
	   	    datosPrestamos.p_IdEmpresa = $scope.SesionIdEmpresa;
	   	    datosPrestamos.p_IdUsuario = $scope.SesionIdUsuario;
	   	    datosPrestamos.p_IdModulo  = 'CUS';
	   	    datosPrestamos.p_FolioPrestamosOriginal= Number($scope.data.oDatosEmprestito.FolioPrestamo) || 0			
		    datosPrestamos.p_Accion ='CREAR'
		    datosPrestamos.p_Empresito = datos.p_DatosEmprestitos;
			
		    
		    bcsComunes.ventanaOverlay(idVentana,true);
		    bcsServiciosPrestamo.AnticipoEmprestitoSinCustodia(datosPrestamos,function(respuesta){	
				
				bcsComunes.ventanaOverlay(idVentana,false);
				if (respuesta.P_OUT_ERRORESCONTADOR === 0){
									
					respuesta.P_OUT_PRESTAMO.Accion ='CREAR'
					respuesta.P_OUT_PRESTAMO.p_FolioPrestamosOriginal= Number($scope.data.oDatosEmprestito.FolioPrestamo) || 0	
					respuesta.P_OUT_PRESTAMO.p_Empresito= datos.p_DatosEmprestitos;
					

					bcsComunes.ventanaOverlay(idVentana,false);
					let msj=bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);
							   
				   var id1 = bcsComunes.ventanaAbrir("ModificarPrestamoModal",respuesta.P_OUT_PRESTAMO,false,false,$scope.winID); 
			       var promesa=bcsComunes.ventanaPromesa(id1);                               
			       promesa.then(function(data){ 
			       	if (data === true) {
						
					}
			       }); 
			       
				}else{
					// mensajes de error
				}
			
			
			})	
		    
		    
			
		   
			
		}; 
		 
		 

	   	ObtenerPlazoAnticipo = function() {
			var datos = {
				p_IdEmpresa 		: $scope.SesionIdEmpresa,
				p_IdUsuario 		: $scope.SesionIdUsuario,
				p_IdModulo 			: 'CUS',
				p_UsaReglasNegocio 	: 'S',
				p_FechaDesde 		: $scope.data.UsaFechaLiquidacionEnVencimiento && $scope.data.oDatosEmprestito.FechaLiquidacion && $scope.data.oDatosEmprestito.FechaLiquidacion > util.date($scope.data.kngFechaAnticipo).toDate() ? util.dateSPdate(util.date($scope.data.oDatosEmprestito.FechaLiquidacion).toDate()) :util.dateSPdate(util.date($scope.data.kngFechaAnticipo).toDate()),
				p_FechaHasta 		: util.dateSPdate(util.date($scope.data.oDatosEmprestito.FechaVencimientoEmprestito).toDate()),
				p_DiasHabiles 		: 'N',
				p_IdPais 			: $scope.SesionIdPaisSistema
				
			};
			bcsServicioPlataforma.Plazo(datos,
				function(result) {
					$scope.data.plazoAnticipo = Number(result.P_OUT_PLAZODIAS);
				}
				,function(result){
					$scope.data.plazoAnticipo = 0;
					console.log(result);
				}
			);
		};
	   	
	   	ObtenerPlazo = function(){
	   		var datos = {
				p_IdEmpresa 		: $scope.SesionIdEmpresa,
				p_IdUsuario 		: $scope.SesionIdUsuario,
				p_IdModulo 			: 'CUS',
				p_UsaReglasNegocio 	: 'S',
				p_FechaDesde 		: $scope.data.UsaFechaLiquidacionEnVencimiento && $scope.data.oDatosEmprestito.FechaLiquidacion && $scope.data.oDatosEmprestito.FechaLiquidacion > util.date($scope.data.kngFechaAnticipo).toDate() ? util.dateSPdate(util.date($scope.data.oDatosEmprestito.FechaLiquidacion).toDate()) : util.dateSPdate(util.date($scope.data.kngFechaAnticipo).toDate()),
				p_FechaHasta 		: util.dateSPdate(util.date($scope.data.oDatosEmprestito.FechaVencimientoEmprestito).toDate()),
				p_DiasHabiles 		: 'N',
				p_IdPais 			: $scope.SesionIdPaisSistema
			};
			bcsServicioPlataforma.Plazo(datos,
				function(result) {
					$scope.data.kngPlazo = Number(result.P_OUT_PLAZODIAS);
				}
				,function(result){
					$scope.data.kngPlazo = 0;
					console.log(result);
				}
			);
	   	};
	   	
	   	ObtenerPrecio = function(instrumento, nemo, tipoInstrumento, estado, clasificacion, emisor, moneda, bolsa, muestraPrecio, subyacente)
	    {
			var datos = {};
			//Rescata valores ingresados en formulario
	   		datos.p_IdEmpresa 				= $scope.SesionIdEmpresa; //saca de variables de entorno
	    	datos.p_FiltroIdInstrumento 	= instrumento || '';
	   		datos.p_FiltroInstrumento 		= nemo || '';
			datos.p_FiltroTipoInstrumento 	= tipoInstrumento || ''; 
			datos.p_FiltroEstado 			= estado || ''; 
	 		datos.p_FiltroClasificacion 	= clasificacion || '';
	 		datos.p_FiltroEmisor 			= emisor || '';
	 		datos.p_FiltroMoneda 			= moneda || '';
	 		datos.p_FiltroBolsa 			= bolsa || '';
	 		datos.p_MuestraPrecio 			= muestraPrecio || '';
	 		datos.p_FiltroSubyacente 		= subyacente || '';
	 		bcsComunes.ventanaOverlay(idVentana,true);
			bcsServicioPlataforma.BolsaInstrumentoPrecioActual(datos).then(function(result) {
				bcsComunes.ventanaOverlay(idVentana,false);
				if (result.P_OUT_ERRORESCONTADOR === 0){
					$scope.data.kngPrecio = 0;
					if (typeof result.P_OUT_BOLSAINSTRUMENTOPRECIOS[0] !== 'undefined'){
							if (util.dateSPdate(result.P_OUT_BOLSAINSTRUMENTOPRECIOS[0].FechaCierre) == miFechaSis) {
							$scope.data.kngPrecio = Number(result.P_OUT_BOLSAINSTRUMENTOPRECIOS[0].UltimoPrecioTransado); 
						}
					}
				}
				else
			    {
					$scope.data.kngPrecio = 0;
					console.log("Error." + result.P_OUT_ERRORESCONTADOR);
		            var i = 0;
		            for(i; i<result.P_OUT_ERRORESCONTADOR; i++)
		            {
		                console.log("Error: " + result.P_OUT_ERRORESDETALLE[i].Descripcion);
		                console.log("Error2.");
		            }
			    }
			},function(result) {
				bcsComunes.ventanaOverlay(idVentana,false);
				console.log(JSON.stringify(result));
		   		console.log("revisar");
			});
		};	
	   	
	   	var fnCambio = function(newValue, oldValue) {
			$scope.data.kngMonto = 0;
			if ($scope.data.oDatosEmprestito.IdClasificacionTipoInstrumentoSubyacente == "RF" ){
				$scope.data.muestraDatosRF = true;
				var laCantidad = Number(newValue[1]) || 0;
				var elCorteMin = $scope.data.oDatosEmprestito.CorteMinimo || 1;
				
				if (laCantidad > 0 && elCorteMin > 1) {
					if(laCantidad % elCorteMin !==0){
						bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("CORTE_MINIMO_INVALIDO"));
						$scope.data.kngCantidad = 0;
						return;								
					}
				}	
				
				const base = $scope.data.monedaInstrumento === 'CLP' ? 30 : 360;

				$scope.data.kngMonto 		= $scope.data.oDatosEmprestito.INGMontoCompraActualMonedaInstrumento * (laCantidad/$scope.data.kngCantidad);
				$scope.data.kngMontoPremio	= bcsComunes.calcularMontoaPlazo($scope.data.kngMonto, parametros.PlazoEnDiasOperacion - Number($scope.data.plazoAnticipo),Number($scope.data.kngPorcentajePremio), base) - $scope.data.kngMonto;
			}else{
				if (typeof $scope.data.kngPrecio !== 'undefined' && typeof $scope.data.kngCantidadPorAnticipar !== 'undefined' && typeof $scope.data.instrumento !== 'undefined'){
					if($scope.data.kngCantidadPorAnticipar > $scope.data.oDatosEmprestito.INGCantidad){
						$scope.data.kngCantidadPorAnticipar = $scope.data.oDatosEmprestito.INGCantidad;
					}
					var datos = {};
					//Rescata valores ingresados en formulario
					//VARIABLES DE ENTRADA OBLIGATORIAS
					datos.p_IdEmpresa = $scope.SesionIdEmpresa; 
					datos.p_IdUsuario = $scope.SesionIdUsuario;
					datos.p_IdModulo = 'CUS';
					datos.p_UsaReglasNegocio = 'S';
					//ENUMERAR VARIABLES DE ENTRADA
					datos.p_Cantidad = $scope.data.kngCantidadPorAnticipar || 0;
					datos.p_Precio = $scope.data.kngPrecio || 0;
					datos.p_Tamano = 1;
					datos.p_IdTipoInstrumento = $scope.data.oDatosEmprestito.IdTipoInstrumentoSubyacente || ''; 
					datos.p_AplicaValidaciones = 'N';
					bcsComunes.ventanaOverlay(idVentana,true);
					bcsServicioCalcularMontoTipoInstrumento.llamar(datos,function(result)
					{
						bcsComunes.ventanaOverlay(idVentana,false);
						if (result.P_OUT_ERRORESCONTADOR === 0){
							$scope.data.kngMonto = Number(result.P_OUT_MONTO);}
						else {
							$scope.data.kngMonto = 0;
							console.log("Error." + result.P_OUT_ERRORESCONTADOR);
							var i = 0;
							for(i; i<result.P_OUT_ERRORESCONTADOR; i++)
							{
								console.log("Error: " + result.P_OUT_ERRORESDETALLE[i].Descripcion);
								console.log("Error2.");
							}
						}
					},function(result) {
						bcsComunes.ventanaOverlay(idVentana,false);
						console.log(JSON.stringify(result));
						console.log("revisar");
					});
				}
			}
		};

		bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise('CUS.UsaFechaLiquidacionEnVencimiento', $scope.SesionIdEmpresa, 'PLATAFORMA')
		.then(function (parametroUsaFechaLiquidacion) {
			$scope.data.UsaFechaLiquidacionEnVencimiento = parametroUsaFechaLiquidacion.Valor === 'S';
			ObtenerPlazoAnticipo();
			ObtenerPlazo();
		});

		/*********************************************
		 * WATCHERS
		 *********************************************/
		$scope.$watchGroup(['data.kngPrecio','data.kngCantidadPorAnticipar'], fnCambio );
		
		$scope.$watchGroup(['data.kngMonto','data.kngPorcentajePremio','data.plazoAnticipo'], function (newValue, oldValue){	
			if (typeof $scope.data.kngMonto !== 'undefined' && typeof $scope.data.kngPorcentajePremio !== 'undefined' && typeof $scope.data.plazoAnticipo !== 'undefined'){
				const base = $scope.data.monedaInstrumento === 'CLP' ? 30 : 360;
				$scope.data.kngDiasCobrados = parametros.PlazoEnDiasOperacion - Number($scope.data.plazoAnticipo);
				$scope.data.kngMontoPlazo = bcsComunes.calcularMontoaPlazo($scope.data.kngMonto, parametros.PlazoEnDiasOperacion - Number($scope.data.plazoAnticipo),Number($scope.data.kngPorcentajePremio), base);
				$scope.data.kngMontoPremio = Number($scope.data.kngMontoPlazo) - Number($scope.data.kngMonto); 
			}
		});
	   	
		$scope.$watch('data.oConfiguracionCondicionLiquidacion.seleccionado',function(newval,oldval){
			if (typeof $scope.data.oConfiguracionCondicionLiquidacion.seleccionado !== 'undefined' && 
					$scope.data.oConfiguracionCondicionLiquidacion.seleccionado !== false){
				var datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario, 
					p_IdModulo : 'CUS', 
					p_UsaReglasNegocio : 'S', 
					p_FechaIngreso : $scope.data.kngFechaAnticipo,
		        	p_IdTipoOperacion : $scope.data.oDatosEmprestito.IdTipoOperacion,
		        	P_IdCondicionDeLiquidacion : $scope.data.oConfiguracionCondicionLiquidacion.seleccionado.IdCondicionDeLiquidacion,
		        	p_IdPais : $scope.SesionIdPaisSistema 
				};
				
				bcsServicioPlataforma.FechaLiquidacion(datos,function(result)
				{
					if (result.P_OUT_ERRORESCONTADOR === 0) {
						$scope.data.kngFechaLiquidacion = result.P_OUT_FECHALIQUIDACION;
					}
					else {
						$scope.data.kngFechaLiquidacion = '0001-01-01';
						$scope.data.plazoAnticipo = 0;
						var i = 0;
			            for(i; i<result.P_OUT_ERRORESCONTADOR; i++) {
			                console.log("Error: " + result.P_OUT_ERRORESDETALLE[i].Descripcion);
			                console.log("Error2.");
			            }
				    }
				},function(result)
				{
					console.log(JSON.stringify(result));
				});
			}
		});
		
		
		var ConsultaFlujo = function (){
			
			var tblInstr = [];
			tblInstr.push({ 
	  			 IdClasificacionTipoInstrumento	: $scope.data.oDatosEmprestito.IdClasificacionTipoInstrumentoSubyacente,
	  			 IdTipoInstrumento				: $scope.data.oDatosEmprestito.IdTipoInstrumentoSubyacente,
	  			 IdInstrumento					: $scope.data.oDatosEmprestito.IdInstrumentoSubyacente,
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
				IdCliente						: $scope.data.oDatosEmprestito.IdClientePrestamo,
				IdPortafolio					: $scope.data.oDatosEmprestito.IdPortafolioPrestamo,
				BusquedaEspecifica				: 'PRESTAMISTAS_DISPONIBLES',
				IdTipoInstrumentoEspecifico		: null				
			});

			
			var paramInSP = {};
			paramInSP.p_IdEmpresa			= $scope.SesionIdEmpresa;
			paramInSP.p_IdUsuario			= $scope.SesionIdUsuario;
			paramInSP.p_IdModulo			= 'SIS';
			paramInSP.p_FechaSaldo			= miFechaSis; //tFechaHoraSistema.substring(0, 10) ;
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
							if (result.P_OUT_SALIDA[0].T0 < $scope.data.oDatosEmprestito.INGCantidad){
								bcsComunes.msg("error","info",$translate.instant('UI_PRESTATARIO_SIN_CUSTODIA_PARA_ANTICIPAR'));
								$scope.data.accionDisabled = false;
							}
						}
					},
					function(result){}
			);
			
			
		};
		//ConsultaFlujo();

		
		/*********************************************
		 * GO!
		 *********************************************/
		/*$timeout(function(){
			ObtenerPlazo();
		},1000);*/
		
		
		

	});
}());
//------------------------- FIN CONTROLLER --------------------------