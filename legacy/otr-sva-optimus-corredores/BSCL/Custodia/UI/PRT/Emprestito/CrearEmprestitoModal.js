/*************************************************************
 * Purpose	: Crear Emprestitos
 * Author	: TEAM CUSTODIA - BCS
 * Date		: 14-07-2016 [Norm]
 *************************************************************/
 (function() {
	angular.module('ModuloCustodia').controller('CrearEmprestitoModalCtrl',
	function ($scope,$timeout,bcsComunes,$translate,bcsServiciosCustodia,
		bcsServicioPlataforma,bcsServicioCalcularMontoTipoInstrumento,bcsServiciosPrestamo, bcsServiciosValorizacion) {
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		$scope.data = {};
		$scope.data.plazoMaximoVencimiento = 0;
		var calcularFechaEnEjecucion = false;
		var idVentana = bcsComunes.ventanaWinId($scope);
		var parametros = bcsComunes.ventanaParams(idVentana);
		var valorizadorEnEjecucion = false;
		var esPrestamistaExterno;
		
		$scope.data.oDatosPrestamo = parametros;
		$scope.data.fechaVencimientoValida = true;
		$scope.data.muestraDatosRF = $scope.data.oDatosPrestamo.IdClasificacionTipoInstrumentoSubyacente == "RF";
		$scope.data.yaValorizo = false;
		$scope.data.BaseTasaVentaCortaRvBcs=0;
		$scope.data.BaseTasaVentaCortaRvBec=0;
		$scope.data.BaseTasaVentaCortaRfBcs=0;
		//Obtiene parametro TasaPrestamoIgualTasaEmprestito --> indica si la tasa emprestito podrá ser diferente a la tasa del prestamo  
		var tasaPrestamoIgualTasaEmprestito = BCS_CONFIGURACION.CUSTODIA["TasaPrestamoIgualTasaEmprestito"];				
		$scope.data.PorcentajePremioDisabled = tasaPrestamoIgualTasaEmprestito === "S" ? true : false;
		c1 = true;
		c2 = true;

		//obtencion parametros de sistema
		bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise('BaseTasaVentaCortaRvBcs', $scope.SesionIdEmpresa, 'CUSTODIA')
		.then(function ( parametroVentaCorta ) {
			if (parametroVentaCorta.Valor){
				$scope.data.BaseTasaVentaCortaRvBcs= parametroVentaCorta.Valor;
			}else{
				$scope.data.BaseTasaVentaCortaRvBcs=30;
			}
	   });	
	   bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise('BaseTasaVentaCortaRvBec', $scope.SesionIdEmpresa, 'CUSTODIA')
	   .then(function ( parametroVentaCorta ) {
		   if (parametroVentaCorta.Valor){
			   $scope.data.BaseTasaVentaCortaRvBec= parametroVentaCorta.Valor;
		   }else{
			   $scope.data.BaseTasaVentaCortaRvBec=30;
		   }
	  });	
	  bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise('BaseTasaVentaCortaRfBcs', $scope.SesionIdEmpresa, 'CUSTODIA')
	  .then(function ( parametroVentaCorta ) {
		  if (parametroVentaCorta.Valor){
			  $scope.data.BaseTasaVentaCortaRfBcs= parametroVentaCorta.Valor;
		  }else{
			  $scope.data.BaseTasaVentaCortaRfBcs=30;
		  }
	 });	

		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		$scope.data.oConfiguracionCondicionLiquidacion={
			tipo : "combobox-condicion-liquidacion",
			configuracion : {},
			filtros : {
				IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   : "" },
				EstadoTipoOperacionYCondicionDeLiquidacion : { "field"   : "EstadoTipoOperacionYCondicionDeLiquidacion","operator": "eq", "value"   : "ACT" },
				IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   : "EMPRES" },
				IdCondicionDeLiquidacion : { "field"   : "IdCondicionDeLiquidacion","operator": "eq", "value"   : $scope.data.oDatosPrestamo.IdCondicionLiquidacion}
			},
			idActual : $scope.data.oDatosPrestamo.IdCondicionLiquidacion,
			seleccionado : false,
			idDefecto : ''
			,activado: true
		};

		
		$scope.oSocioNegocio2 = {
				InfoCliente    : "",
				NombreCliente  : "",
				IdCliente      : 0,
				IdPortafolio   : 0,
				TotalCartera   : 0
		};

	
		$scope.oPortafolio={
			tipo : "combobox-portafolio",
			configuracion :{},
			idActual : 0,
			seleccionado : false,
			filtros: {
				IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 },
				IdEstado  : { "field": "IdEstado" ,"operator": "eq", "value"   :  'ACT' }
			},
		};

		// jvillagran ... nuevo ********** 
		$scope.data.oClientePortafolio={
				tipo 			: "combobox-cliente-portafolio",
				configuracion 	: {},
				seleccionado 	: false,
				idActual		: 0,
				filtros: {
					IdEstado  : {"field": "IdEmpresa", "operator": "eq", "value": $scope.SesionIdEmpresa },
					EsCliente : {"field": "EsCliente", "operator": "eq", "value": 'UI_SI'}
				},
		};
		
		
		/*********************************************
		 * EVALUACION INICIAL
		 ********************************************/

		$scope.data.fechaLiquidacion 			= $scope.data.oDatosPrestamo.FechaLiquidacion;
		$scope.data.IdCondicionDeLiquidacion	= $scope.data.oDatosPrestamo.IdCondicionLiquidacion;
		$scope.data.tipoPrestamo 				= $scope.data.oDatosPrestamo.NombreTipoPrestamo;
		$scope.data.tipoInstrumento 			= $scope.data.oDatosPrestamo.NombreTipoInstrumentoSubyacente;
		$scope.data.checkConGarantiasEspecificas= ($scope.data.oDatosPrestamo.UsaGarantiaEspecifica==='S')?true:false;
		$scope.data.kngFolioPrestamo 			= $scope.data.oDatosPrestamo.FolioPrestamo;
		//cargar al inicio
		$scope.data.kngFechaInicioPrestamo 		= $scope.data.oDatosPrestamo.Accion === "CREAR"?util.dateInicioDia($scope.data.oDatosPrestamo.FechaPrestamoNuevo):$scope.data.oDatosPrestamo.FechaPrestamo;
		$scope.data.kngFechaVencimientoPrestamo = $scope.data.oDatosPrestamo.FechaVencimientoPrestamo;
		$scope.data.kngCorteMinimo 				= $scope.data.oDatosPrestamo.CorteMinimo;
		$scope.data.instrumento 				= $scope.data.oDatosPrestamo.NemoSubyacente;
		$scope.data.monedaInstrumento 			= $scope.data.oDatosPrestamo.IdMonedaInstrumento;
		$scope.data.CorteMinimoOptions 			= {format: 'n0',min:0,spinners: false};
		
		$scope.data.FechaInicioOptions 			= {min:$scope.data.kngFechaInicioPrestamo,max:$scope.data.kngFechaVencimientoPrestamo};
		$scope.data.kngFechaVencimiento 		= util.dateInicioDia($scope.data.oDatosPrestamo.FechaVencimientoPrestamo);
		
		$scope.data.kngFechaLiquidacion 		= util.dateInicioDia(parametros.FechaLiquidacion);
		$scope.data.cantidadAsignadaOptions 	= {format: 'n0',min:0,spinners: false};
		$scope.data.CantidadPorAsignarOptions 	= {format: 'n0',min:0,spinners: false};
		$scope.data.cantidadOptions 			= {format: 'n0',min:0,spinners: false, min:0, max:$scope.data.oDatosPrestamo.CantidadPorAsignar};
		$scope.data.precioOptions 				= {format: 'n'+(util.bcs_formato(0,'GEN','Precio',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.tasaOptions 				= {format: 'n'+(util.bcs_formato(0,'GEN','Tasa',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:-999999999999999999999,spinners: false}; 
		$scope.data.montoOptions 				= {format: 'c'+(util.bcs_formato(0,'GEN','Monto',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.plazoOptions 				= {format: 'n0',min:0,spinners: false};
		$scope.data.porcentajePremioOptions 	= {format: 'n'+(util.bcs_formato(0,'GEN','Tasa',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.montoPremioOptions 			= {format: 'c'+(util.bcs_formato(0,'GEN','Monto',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.montoPlazoOptions 			= {format: 'c'+(util.bcs_formato(0,'GEN','Monto',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.data.folioPrestamoOptions 		= {format: 'n0',min:0,spinners: false};
		$scope.data.plazoMaximoVencimiento 		= $scope.data.oDatosPrestamo.PlazoEnDiasOperacion;
		
		$scope.data.kngPrecio 					= $scope.data.oDatosPrestamo.INGPrecioInstrumento || 0;
		$scope.data.kngTasa 					= 0;//$scope.data.oDatosPrestamo.INGTasaInstrumento || 0;
		$scope.data.accionDisabled 				= true;
		$scope.data.kngCantidadAsignada 		= $scope.data.oDatosPrestamo.CantidadAsignada;
		$scope.data.kngCantidadPorAsignar 		= $scope.data.oDatosPrestamo.CantidadPorAsignar;
		$scope.data.kngCantidad 				= $scope.data.oDatosPrestamo.CantidadPorAsignar;
		$scope.data.kngPorcentajePremio 		= $scope.data.oDatosPrestamo.TasaPrestamo;
		$scope.data.checkExigirGarantia         = ($scope.data.oDatosPrestamo.checkExigirGarantia==='S')?true:false; 
		$scope.data.kngPlazo 					= undefined;
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		$scope.data.btnCerrar = function(refrescar) {
			bcsComunes.ventanaData(idVentana,refrescar);
			bcsComunes.ventanaCerrar(idVentana);
		};
		
		$scope.data.crearEmprestito = function() {
			var datos = {};
			
			if ($scope.data.muestraDatosRF){
				var laCantidad = Number($scope.data.kngCantidad) || 0;
				var elCorteMin = $scope.data.oDatosPrestamo.CorteMinimo || 1;
				
				if (laCantidad > 0 && elCorteMin > 1) {
					if(laCantidad % elCorteMin !==0){
						bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("CORTE_MINIMO_INVALIDO"));
						$scope.data.kngCantidad = 0;
						return;								
					}
				}
			}
			
			if ($scope.oSocioNegocio2.IdCliente == 0 || $scope.oSocioNegocio2.IdPortafolio == 0){
				bcsComunes.ventanaMsgErr($translate.instant('UI_DEBE_SELECCIONAR_CLIENTE'),$scope.ventanaId);
				return;
			};
			
			if ($scope.data.kngFechaVencimiento == null){
				bcsComunes.ventanaMsgErr($translate.instant('UI_FECHA_VENCIMIENTO_INVALIDA'),$scope.ventanaId);
				return;
			};
			
			if ($scope.data.kngFechaVencimiento === undefined){
				bcsComunes.ventanaMsgErr($translate.instant('UI_FECHA_VENCIMIENTO_INVALIDA'),$scope.ventanaId);
				return;
			};
			
			if ($scope.data.kngFechaVencimiento > util.dateInicioDia($scope.data.oDatosPrestamo.FechaVencimientoPrestamo)){
				bcsComunes.ventanaMsgErr($translate.instant('UI_FECHA_VENCIMIENTO_INVALIDA'),$scope.ventanaId);
				return;
			};
			
			if ($scope.data.kngFechaVencimiento <= $scope.data.kngFechaInicio){
				bcsComunes.ventanaMsgErr($translate.instant('UI_FECHA_VENCIMIENTO_INVALIDA'),$scope.ventanaId);
				return;
			};

			
			if ($scope.data.kngPrecio < $scope.data.oDatosPrestamo.INGPrecioInstrumento){
				bcsComunes.ventanaMsgErr($translate.instant('UI_PRECIO_PRESTAMISTA_MENOR_A_PRECIO_PRESTAMO'),$scope.ventanaId);
				return;
			};

			
			if (esPrestamistaExterno == false && ($scope.data.kngCantidad > $scope.oSocioNegocio2.TotalCartera)){
				bcsComunes.ventanaMsgErr($translate.instant('UI_CANTIDAD_INVALIDA'),$scope.ventanaId);
				return;
			};
			
			
			
			if ($scope.data.kngCantidad > $scope.data.kngCantidadPorAsignar){
				bcsComunes.ventanaMsgErr($translate.instant('UI_CANTIDAD_INVALIDA'),$scope.ventanaId);
				return;
			};
			
			
			bcsComunes.ventanaOverlay(idVentana,true);
			$scope.data.accionDisabled 			= undefined;
			datos.p_IdEmpresa 					= $scope.SesionIdEmpresa; 
			datos.p_IdUsuario 					= $scope.SesionIdUsuario;
			datos.p_IdModulo 					= 'CUS';
			datos.p_UsaReglasNegocio 			= 'S';
			datos.p_IdClientePrestamista 		= $scope.oSocioNegocio2.IdCliente || -1;
			datos.p_IdPortafolioPrestamista 	= $scope.oSocioNegocio2.IdPortafolio || -1;
			datos.p_FechaMovimiento	 			= util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema)) || '1900-01-01';
			datos.p_FechaVencimiento 			= util.dateSPdate($scope.data.kngFechaVencimiento) || '1900-01-01';
			datos.p_IdCondicionDeLiquidacion 	= $scope.data.condicionLiquidacionSeleccionada.IdCondicionDeLiquidacion || '';
			datos.p_FechaLiquidacion 			= util.dateSPdate($scope.data.kngFechaLiquidacion) || '1900-01-01';
			datos.p_IdInstrumento 				= $scope.data.oDatosPrestamo.IdInstrumentoSubyacente || 0;
			datos.p_Nemo 						= $scope.data.oDatosPrestamo.NemoSubyacente || '';
			datos.p_Cantidad 					= Number($scope.data.kngCantidad) || 0;
			datos.p_Cantidad_Asignar 			= Number($scope.data.kngCantidadPorAsignar) || 0;
			datos.p_Precio 						= Number($scope.data.kngPrecio) || 0;
			datos.p_TasaInstrumento 			= ($scope.data.muestraDatosRF ? Number($scope.data.kngTasa) : 0);
			datos.p_Monto 						= Number($scope.data.kngMonto) || 0;
			datos.p_MontoPremio 				= Number($scope.data.kngMontoPremio) || 0;
			datos.p_TasaPremio 					= Number($scope.data.kngPorcentajePremio) || 0;
			datos.p_MontoAPlazo 				= Number($scope.data.kngMontoPlazo) || 0;
			datos.p_FolioCarteraPrestamo 		= $scope.data.oDatosPrestamo.FolioPrestamo || 0;
			($scope.data.checkPrestaTitulosNominales)? datos.p_PrestaTitulosNominales = 'S': datos.p_PrestaTitulosNominales = 'N';
			datos.p_IdAgente 					= $scope.SesionIdUsuario; //TODO --ver cuando este el modulo de agente
			datos.p_IdEmpleado 					= $scope.SesionIdEmpleado;
			datos.p_PlazoEnDias 				= Number($scope.data.kngPlazo) || 0;
			datos.p_IdTipoCanalIngreso 			= $scope.SesionTipoCanalIngreso || '';
			datos.p_IdMonedaSistema 			= $scope.SesionIdMonedaSistema || '';
			datos.p_ExigirGarantia			    = ($scope.data.checkExigirGarantia === true ? 'S' : 'N');
			datos.p_EsPrestamistaExterno=(esPrestamistaExterno ===true ? 'S':'N');
			bcsServiciosPrestamo.CrearEmprestito(datos,successCrearEmprestito,errorCrearEmprestito);
	   		
	   	};
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
	 	var successCrearEmprestito = function(respuesta) {
	 		bcsComunes.ventanaOverlay(idVentana,false);
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			$scope.data.accionDisabled = true;
			//se valido la orden
			if (respuesta.P_OUT_ERRORESCONTADOR === 0)  {

				if($scope.data.oDatosPrestamo.Accion){
					AnticiparPrestamoOriginal();
				}
				
				respuesta.P_OUT_MENSAJES.forEach(function(mensajes){
					if(mensajes.tipo === 'ADVERTENCIA'){
						bcsComunes.ventanaMsg({
				    	mensaje:$translate.instant("UI_CANTIDAD_PRESTAMISTA_MENOR_A_CANTIDAD_PRESTAMO"),
				    	conflictos:util.parseServerResponse(mensajes.codigo)
				  		},idVentana);
					}
				});
				bcsComunes.msg('info',$translate.instant("UI_INFORMACION"), $translate.instant("UI_OPERACION_INGRESO_EXITO"));
				$scope.data.btnCerrar(true);
			}
			else {
				bcsComunes.ventanaMsg({
				    mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"),
				    conflictos:util.parseServerResponse(respuesta)
				  },idVentana);
				$scope.data.accionDisabled = true;
		    }
		};
	
		var errorCrearEmprestito = function(respuesta) {
			bcsComunes.ventanaOverlay(idVentana,false);
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_OPERACION_INGRESO_ERROR"));
			$scope.data.accionDisabled = true;
		};

		AnticiparPrestamoOriginal = function(){

			var datosPrestamos={};
	   	    datosPrestamos.p_IdEmpresa = $scope.SesionIdEmpresa;
	   	    datosPrestamos.p_IdUsuario = $scope.SesionIdUsuario;
	   	    datosPrestamos.p_IdModulo  = 'CUS';
	   	    datosPrestamos.p_FolioPrestamosOriginal= $scope.data.oDatosPrestamo.p_FolioPrestamosOriginal;		
		    datosPrestamos.p_Accion ='ANTICIPAR'
		    datosPrestamos.p_Empresito = $scope.data.oDatosPrestamo.p_Empresito;

			bcsServiciosPrestamo.AnticipoEmprestitoSinCustodia(datosPrestamos,function(respuesta){
				if(respuesta.P_OUT_ERRORESCONTADOR==0){
					//mensajes de salida
					//validar si no hay agregar otro emprestito
					
				}else{
					//error
				}

				let msj=bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(respuesta)
					},$scope.idVentana);
				
			});
			
		}
			   	
	   	var fnCambioPQ = function(newValue, oldValue) {
			if (   typeof $scope.data.kngPrecio !== 'undefined' 
				&& typeof $scope.data.kngCantidad !== 'undefined')
			{
				var datos = {};
				
				if ($scope.data.muestraDatosRF){
					var laCantidad = Number($scope.data.kngCantidad) || 0;
					var elCorteMin = $scope.data.oDatosPrestamo.CorteMinimo || 1;
					
					if (laCantidad > 0 && elCorteMin > 1) {
						if(laCantidad % elCorteMin !==0){
							bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("CORTE_MINIMO_INVALIDO"));
							$scope.data.kngCantidad = 0;
							return;								
						}
					}
					return;
				}
				
				
				//Rescata valores ingresados en formulario
		        //VARIABLES DE ENTRADA OBLIGATORIAS
	       		datos.p_IdEmpresa = $scope.SesionIdEmpresa;
	        	datos.p_IdUsuario = $scope.SesionIdUsuario;
	        	datos.p_IdModulo = 'CUS';
	        	datos.p_UsaReglasNegocio = 'S';
	        	//ENUMERAR VARIABLES DE ENTRADA
	        	datos.p_Cantidad = $scope.data.kngCantidad || 0;
	    		datos.p_Precio = $scope.data.kngPrecio || 0;
				
				datos.p_Tamano = 1;
				datos.p_IdTipoInstrumento = $scope.data.oDatosPrestamo.IdTipoInstrumentoSubyacente || ''; 
		 		datos.p_AplicaValidaciones = 'N';
		 		bcsComunes.ventanaOverlay(idVentana,true);
				bcsServicioCalcularMontoTipoInstrumento.llamar(datos,function(result)
				{
					bcsComunes.ventanaOverlay(idVentana,false);
					if (result.P_OUT_ERRORESCONTADOR === 0){
						$scope.data.kngMonto = Number(result.P_OUT_MONTO);
						$scope.data.yaValorizo = true;
					}else {
						$scope.data.kngMonto = 0;
						console.log("Error." + result.P_OUT_ERRORESCONTADOR);
			            var i = 0;
			            for(i; i<result.P_OUT_ERRORESCONTADOR; i++) {
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
		};	

		
		var fnFechaLiquidacion = function(newValue, oldValue) {
			if (newValue === oldValue)
	   			return
			if (typeof $scope.data.oConfiguracionCondicionLiquidacion.seleccionado !== 'undefined' && 
					$scope.data.oConfiguracionCondicionLiquidacion.seleccionado !== false){
				var datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario, 
					p_IdModulo : 'CUS', 
					p_UsaReglasNegocio : 'S', 
		       		p_FechaIngreso : util.dateSPdate($scope.data.kngFechaInicio),
		        	p_IdTipoOperacion : 'EMPRES',
		        	p_IdCondicionDeLiquidacion : $scope.data.oConfiguracionCondicionLiquidacion.seleccionado.IdCondicionDeLiquidacion,
		        	p_IdPais : $scope.SesionIdPaisSistema
				};
				
				bcsServicioPlataforma.FechaLiquidacion(datos,function(result)
				{
					if (result.P_OUT_ERRORESCONTADOR === 0){
						$scope.data.kngFechaLiquidacion = util.dateInicioDia(result.P_OUT_FECHALIQUIDACION);
					}
					else {
						$scope.data.kngFechaLiquidacion = util.dateInicioDia($scope.SesionFechaSistema);
						var i = 0;
			            for(i; i<result.P_OUT_ERRORESCONTADOR; i++)
			            {
			                console.log("Error: " + result.P_OUT_ERRORESDETALLE[i].Descripcion);
			                console.log("Error2.");
			            }
				    }
				},function(result) {
					console.log(JSON.stringify(result));
				});
			}
		};
		
		$scope.data.CalcularPlazo = function() {
			var datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario, 
					p_IdModulo : 'CUS', 
					p_UsaReglasNegocio : 'S',
					p_FechaDesde : $scope.data.UsaFechaLiquidacionEnVencimiento && $scope.data.fechaLiquidacion && $scope.data.fechaLiquidacion > util.dateInicioDia($scope.SesionFechaSistema) ? util.dateSPdate($scope.data.fechaLiquidacion) : util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema)),
					p_FechaHasta : (typeof $scope.data.kngFechaVencimiento == "string" ? util.dateSPdate(util.dateInicioDia($scope.data.kngFechaVencimiento)) : util.dateSPdate($scope.data.kngFechaVencimiento)),
					p_DiasHabiles : 'N',
					p_IdCondicionDeLiquidacion : 'T0',
					p_IdPais : $scope.SesionIdPaisSistema,
				};
	   		bcsComunes.ventanaOverlay(idVentana,true);
	   		bcsServicioPlataforma.Plazo(datos,function(result) {
				if (result.P_OUT_ERRORESCONTADOR === 0){
					$scope.data.kngPlazo = result.P_OUT_PLAZODIAS;
					$scope.data.SeteaFechaInicialEmprestito();
					if (typeof $scope.data.kngMonto !== 'undefined' && typeof $scope.data.kngPorcentajePremio !== 'undefined'){
						if($scope.data.oDatosPrestamo.IdConcepto==='VCACRV'){
							if ($scope.data.oDatosPrestamo.IdBolsa==='BCS'){
								$scope.data.kngMontoPlazo =bcsComunes.calcularMontoaPlazo($scope.data.kngMonto,Number($scope.data.kngPlazo),Number($scope.data.kngPorcentajePremio),$scope.data.BaseTasaVentaCortaRvBcs );				
							}else { 
								$scope.data.kngMontoPlazo =bcsComunes.calcularMontoaPlazo($scope.data.kngMonto,Number($scope.data.kngPlazo),Number($scope.data.kngPorcentajePremio),$scope.data.BaseTasaVentaCortaRvBec );
							}						
						}else if( $scope.data.oDatosPrestamo.IdConcepto==='VCRF'&&  $scope.data.oDatosPrestamo.IdBolsa==='BCS' ){
							$scope.data.kngMontoPlazo =bcsComunes.calcularMontoaPlazo($scope.data.kngMonto,Number($scope.data.kngPlazo),Number($scope.data.kngPorcentajePremio),$scope.data.BaseTasaVentaCortaRfBcs );					
						}else{
							const base = $scope.data.monedaInstrumento === 'CLP' ? 30 : 360;
							$scope.data.kngMontoPlazo = bcsComunes.calcularMontoaPlazo($scope.data.kngMonto,Number($scope.data.kngPlazo),Number($scope.data.kngPorcentajePremio), base);
						}
						$scope.data.kngMontoPremio = Number($scope.data.kngMontoPlazo) - Number($scope.data.kngMonto); 
					}
				}
				bcsComunes.ventanaOverlay(idVentana,false);
				c2=true;
			}
			,function(result){
				bcsComunes.ventanaOverlay(idVentana,false);
				c2=true;
				}
			);
		}

		$scope.data.SeteaFechaInicialEmprestito = function() {
			$scope.data.fechaInicialInputPlazo 			 = $scope.data.UsaFechaLiquidacionEnVencimiento && $scope.data.fechaLiquidacion && $scope.data.fechaLiquidacion > util.dateInicioDia($scope.SesionFechaSistema) ? $scope.data.fechaLiquidacion : util.dateInicioDia($scope.SesionFechaSistema);
			$scope.data.kngFechaInicio 					 = $scope.data.fechaInicialInputPlazo;
			$scope.data.RequeridoInputPlazo				 = true;
		}

		bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise('CUS.UsaFechaLiquidacionEnVencimiento', $scope.SesionIdEmpresa, 'PLATAFORMA')
				.then(function (parametroUsaFechaLiquidacion) {
					$scope.data.UsaFechaLiquidacionEnVencimiento = parametroUsaFechaLiquidacion.Valor === 'S';
					$scope.data.CalcularPlazo();
				});
			
	   	/*********************************************
		 * WATCHERS
		 *********************************************/
		$scope.$watchGroup(['data.kngPrecio','data.kngCantidad'], fnCambioPQ );
		

		$scope.$watchGroup(['data.kngMonto','data.kngPorcentajePremio','data.kngPlazo'], function (newValue, oldValue){
			if (typeof $scope.data.kngMonto !== 'undefined' && typeof $scope.data.kngPorcentajePremio !== 'undefined'){		
				if($scope.data.oDatosPrestamo.IdConcepto==='VCACRV'){
					if ($scope.data.oDatosPrestamo.IdBolsa==='BCS'){
						$scope.data.kngMontoPlazo =bcsComunes.calcularMontoaPlazo($scope.data.kngMonto,Number($scope.data.kngPlazo),Number($scope.data.kngPorcentajePremio),$scope.data.BaseTasaVentaCortaRvBcs );					
					}else { 
						$scope.data.kngMontoPlazo =bcsComunes.calcularMontoaPlazo($scope.data.kngMonto,Number($scope.data.kngPlazo),Number($scope.data.kngPorcentajePremio),$scope.data.BaseTasaVentaCortaRvBec );
					}
				
				}else if( $scope.data.oDatosPrestamo.IdConcepto==='VCRF'&&  $scope.data.oDatosPrestamo.IdBolsa==='BCS' ){
					$scope.data.kngMontoPlazo =bcsComunes.calcularMontoaPlazo($scope.data.kngMonto,Number($scope.data.kngPlazo),Number($scope.data.kngPorcentajePremio),$scope.data.BaseTasaVentaCortaRfBcs );					
					
				}else{
					const base = $scope.data.monedaInstrumento === 'CLP' ? 30 : 360;
					$scope.data.kngMontoPlazo = bcsComunes.calcularMontoaPlazo($scope.data.kngMonto,Number($scope.data.kngPlazo),Number($scope.data.kngPorcentajePremio), base);
				}
				$scope.data.kngMontoPremio = Number($scope.data.kngMontoPlazo) - Number($scope.data.kngMonto); 
			}
		});
		
		$scope.$watch('data.oConfiguracionCondicionLiquidacion.seleccionado', fnFechaLiquidacion );
		
		$scope.data.btnBuscarPrestamistas = function(){
			var v_fecha = '0001-01-01';
			esPrestamistaExterno=false;
			if ($scope.data && typeof $scope.data.kngFechaCondicionLiquidacion !== 'undefined')
				 v_fecha = util.dateSPdate($scope.data.kngFechaCondicionLiquidacion);
			var datos = {};
			datos.Filtrado = true;
			datos.IdClasificacionTipoInstrumento = $scope.data.oDatosPrestamo.IdClasificacionTipoInstrumentoSubyacente || '';
			datos.IdTipoInstrumento = $scope.data.oDatosPrestamo.IdTipoInstrumentoSubyacente || '';
			datos.IdInstrumento = $scope.data.oDatosPrestamo.IdInstrumentoSubyacente;
			datos.FechaConsulta = $scope.SesionFechaSistema;
			datos.CantidadMinima = $scope.data.oDatosPrestamo.CantidadPorAsignar;
			datos.IdPais = $scope.SesionIdPaisSistema;
			datos.IdCliente = $scope.data.oDatosPrestamo.IdCliente;

			var id1 = bcsComunes.ventanaAbrir("ConsultarPrestamistasDisponibles",datos,false, false, idVentana); 			
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){
	        	if (data && data.IdCliente){
					
					if($scope.data.muestraDatosRF){
						$scope.data.kngMonto = undefined;
						$scope.data.kngTasa	 = undefined;
					}	

	        		$scope.oSocioNegocio2.InfoCliente = data.DocumentoIdentidad + " " + data.NombreCliente + " - (" + data.CodigoPortafolio + ")";
	        		$scope.oSocioNegocio2.NombreCliente = data.NombreCliente + " (" + data.Proposito + ")";
	        		$scope.oSocioNegocio2.IdCliente     = Number(data.IdCliente);
	        		$scope.oSocioNegocio2.IdPortafolio  = Number(data.IdPortafolio);
	        		$scope.oSocioNegocio2.TotalCartera  = Number(data.Cantidad);
	        		
			    	if ($scope.data.kngCantidadPorAsignar > data.Cantidad){
			    		$scope.data.kngCantidad = data.Cantidad;
			    	}else{
			    		$scope.data.kngCantidad = $scope.data.kngCantidadPorAsignar;
			    	}
			    	
	        	}
	    	}); 
		};

		
		$scope.data.btnBuscarPrestamistasSinCustodia = function(){
			var v_fecha = '0001-01-01';
			esPrestamistaExterno=true;
			if ($scope.data && typeof $scope.data.kngFechaCondicionLiquidacion !== 'undefined')
				 v_fecha = util.dateSPdate($scope.data.kngFechaCondicionLiquidacion);
			var datos = {};
			datos.Filtrado = true;
			datos.IdClasificacionTipoInstrumento = $scope.data.oDatosPrestamo.IdClasificacionTipoInstrumentoSubyacente || '';
			datos.IdTipoInstrumento = $scope.data.oDatosPrestamo.IdTipoInstrumentoSubyacente || '';
			datos.IdInstrumento = $scope.data.oDatosPrestamo.IdInstrumentoSubyacente;
			datos.FechaConsulta = $scope.SesionFechaSistema;
			datos.CantidadMinima = $scope.data.oDatosPrestamo.CantidadPorAsignar;
			datos.IdPais = $scope.SesionIdPaisSistema;
			datos.IdCliente = $scope.data.oDatosPrestamo.IdCliente;

			var id1 = bcsComunes.ventanaAbrir("ConsultarPrestamistasDisponiblesSinCustodia",datos,false, false, idVentana); 			
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){
	        	if (data && data.IdCliente){

					if($scope.data.muestraDatosRF){
						$scope.data.kngMonto = undefined;
						$scope.data.kngTasa	 = undefined;
					}
	        		
					$scope.oSocioNegocio2.InfoCliente = data.DocumentoIdentidad + " " + data.NombreCliente + " - (" + data.CodigoPortafolio + ")";
	        		$scope.oSocioNegocio2.NombreCliente = data.NombreCliente + " (" + data.Proposito + ")";
	        		$scope.oSocioNegocio2.IdCliente     = Number(data.IdCliente);
	        		$scope.oSocioNegocio2.IdPortafolio  = Number(data.IdPortafolio);
	        		
			    	if ($scope.data.kngCantidadPorAsignar > data.Cantidad){
			    		$scope.data.kngCantidad = data.Cantidad;
			    	}else{
			    		$scope.data.kngCantidad = $scope.data.kngCantidadPorAsignar;
			    	}
			    	
	        	}
	    	}); 
		};
		
	
		
		/********************************************
		****** FUNCIONES PARA VALORIZACION **********
		*********************************************/
				
				var valorizando = function(valor){
					valorizadorEnEjecucion=valor;
					bcsComunes.ventanaOverlay(idVentana ,valor);
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
					var FechaVencimiento= util.dateSPdate($scope.data.kngFechaVencimiento);
					var Plazo = $scope.data.kngPlazo;
					
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
					var cval=generaCabeceraValorizar($scope.data.oDatosPrestamo.IdMonedaInstrumento);	
					var dvalCompra=generaBaseDetalleWS(1);			
		
					dvalCompra.Cantidad							=Cantidad;
					dvalCompra.IdTipoInstrumento				=$scope.data.oDatosPrestamo.IdTipoInstrumentoSubyacente;
					dvalCompra.IdInstrumento					=$scope.data.oDatosPrestamo.IdInstrumentoSubyacente;
					dvalCompra.Nemotecnico						=$scope.data.instrumento;
					dvalCompra.FechaEmision						=$scope.data.oDatosPrestamo.FechaEmisionInstrumento;
					dvalCompra.FechaVencimiento					=util.dateSPdate($scope.data.oDatosPrestamo.FechaVencimientoInstrumento);
					dvalCompra.IdMonedaInstrumento				=$scope.data.oDatosPrestamo.IdMonedaSubyacente;					
					dvalCompra.IdMonedaEmisionInstrumento		=$scope.data.oDatosPrestamo.IdMonedaEmisionSubyacente;
					dvalCompra.IdTipoReajusteInstrumento		=$scope.data.oDatosPrestamo.IdTipoReajusteSubyacente;
					dvalCompra.ValorRescate						=null;
					dvalCompra.PrecioCompra						=Precio||null;
					dvalCompra.TasaCompra						=Tasa||null;				
					dvalCompra.MontoCompra						=Monto||null;
					dvalCompra.Emisor 							=$scope.data.oDatosPrestamo.IdEmisorSubyacente;
					dvalCompra.IdCondicionDeLiquidacion			=$scope.data.condicionLiquidacionSeleccionada.IdCondicionDeLiquidacion||null;
					dvalCompra.IdClasificacionTipoInstrumento	=$scope.data.oDatosPrestamo.IdClasificacionTipoInstrumentoSubyacente;
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
			
				$timeout(function(){
					$scope.data.oConfiguracionCondicionLiquidacion.activado = false;
				},500);
				
		/************************************************************************
		 *********** FIN FUNCIONES PARA VALORIZACION ****************************
		 ************************************************************************/
	});
}());
//------------------------- FIN CONTROLLER --------------------------