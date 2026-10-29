(function() {
	angular.module('ModuloMesa').controller('posicionCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosMesa,bcsServicioPlataforma)
	{	
		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		
		$scope.data = {};
		
		var posicion = this;
		posicion.valorBreakeven = "N";
		posicion.datosAntes = {}; 
		posicion.datosDespues = {};
		posicion.datosAntes = angular.copy(posicion.datosDespues);
		$scope.puedeDescargarExcel = false;
		let monedaSeleccionada = null;
		
		//Configurar al principio
		posicion.data = {};
		$scope.fechaOperacion = {};
		$scope.data.kngFechaOperacion = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.oActivaComboboxMoneda = false;

		$scope.MonedaIdTipoInstrumentoSPT = { "field"   : "IdTipoInstrumento","operator": "eq", "value" : "SPT" };
		$scope.MonedaIdTipoInstrumentoFWD = { "field"   : "IdTipoInstrumento","operator": "eq", "value" : "FWD" };
		$scope.IdEstado					  = { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" };
		$scope.oConfiguracionMoneda = {
			tipo : "combobox-moneda-principal",
			configuracion : {},
			filtros : { 	
			    complejo : [$scope.MonedaIdTipoInstrumentoSPT, $scope.MonedaIdTipoInstrumentoFWD, $scope.IdEstado],
					logic: "and",
					filters: [{
						logic : "or",
						filters: [$scope.MonedaIdTipoInstrumentoSPT, $scope.MonedaIdTipoInstrumentoFWD]
		        		},
		        		$scope.IdEstado
		        	]
			},
			idActual : false,
			seleccionado : false,
			idDefecto : 'USD'
		};
		
		var defConfiguracionMesa = 
		{
			type : "odata",
			definition : 
			{
				serverFiltering : true, // Filtrado de datos en el servidor.
	            serverSorting   : true, // Ordenamiento de los datos en el servidor.
	            sort 			: { field: "", dir: "DESC" },
	            transport		: {
			        	            read: {
			        	            		url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=432",
			        						dataType:"json"
			        	            	}
			        	          },
	            schema 			: { 
	                    			model: 
	                    				{
											fields : 
											{
												IdEmpresa			: {type: "int"},
												IdTipoProducto		: {type: "string"},
												CodigoParametro		: {type: "string"},
												Descripcion			: {type: "string"},
												Valor				: {type: "string"},
											}
										}
									}

	    	}
		};
		
		bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise('USAR_PUNTA_UTILIDAD_PERDIDA', $scope.SesionIdEmpresa, 'MESA').then(
			function (usaPuntaUtilidadPerdida) {
				$scope.oActivaComboboxMoneda = usaPuntaUtilidadPerdida.Valor === 'S';
			}
		);
			
		// VER TIPO CAMBIO BREAKEVEN
		var dsConfiguracionMesabreak = new bcs.DataSource(defConfiguracionMesa);
		var filtroConfiguracionMesabreak = new Array();
		filtroConfiguracionMesabreak.push({ field: "CodigoParametro", operator: "eq", value: 'VER_BREAKEVEN' });
		
		dsConfiguracionMesabreak.query({ filter: filtroConfiguracionMesabreak }).then(function(){
			$scope.valorBreakeven = dsConfiguracionMesabreak.data();
	
			posicion.valorBreakeven = ($scope.valorBreakeven[0] && $scope.valorBreakeven[0].Valor) ? $scope.valorBreakeven[0].Valor : "N";				
		});
		
		// actualizacion Web Socket
		$scope.msgWebsocket = function(message) {
			if (message.Evento === 'ACTSPOT' || message.Evento === 'ACTFWD' || message.Evento === 'ACTPTC') {
				$scope.data.consultarPosicion();
			}
		}
		
		$scope.data.consultarPosicion = function() {	
			bcsComunes.ventanaOverlay(idVentana,true);
			
			if($scope.data.kngFechaOperacion === undefined)
			{
				bcsComunes.msg("error","error",$translate.instant('UI_FECHA_VACIA'));
				bcsComunes.ventanaOverlay(idVentana,false);
			}
			else{
				var parametros = {};
				parametros.p_IdMoneda          = $scope.oConfiguracionMoneda.seleccionado.IdMonedaPrincipal;
				parametros.p_IdEmpresa         = $scope.SesionIdEmpresa;
				parametros.p_IdUsuario         = $scope.SesionIdUsuario;
				parametros.p_Resultado         = 1;
				parametros.p_FechaOperacion    = util.dateSPdate($scope.data.kngFechaOperacion);
	
				bcsServiciosMesa.ConsultarPosicion(parametros, successConsultarPosicion,errorConsultarPosicion);
			}
		};
		
		var successConsultarPosicion = function(respuesta) {
			if (respuesta.P_OUT_ERRORESCONTADOR == 0){
				posicion.datosPosicion = respuesta;
				posicion.datosPosicion.IdMoneda = $scope.SesionIdMonedaSistema;
				posicion.datosPosicion.IdMonedaEquivalente = $scope.oConfiguracionMoneda.seleccionado.IdMonedaPrincipal;
				monedaSeleccionada = $scope.oConfiguracionMoneda.seleccionado.IdMonedaPrincipal;
				$scope.puedeDescargarExcel = true;
			}
			else
			{						
				bcsComunes.ventanaMsg({
				      mensaje:$translate.instant("UI_ERROR"),
				      conflictos:{
				       errores : [ {
				           Codigo : respuesta.P_OUT_ERRORESDETALLE[0].Codigo,
				           Descripcion : respuesta.P_OUT_ERRORESDETALLE[0].Descripcion
				          } ],
				      }
				},idVentana);
				
			}
			bcsComunes.ventanaOverlay(idVentana,false);
          };	
		
		var errorConsultarPosicion = function(respuesta) {
			bcsComunes.ventanaMsg({
			      mensaje:$translate.instant("UI_ERROR"),
			      conflictos:{
			       errores : [ {
			           Codigo : 'COD',
			           Descripcion : 'Un error inesperado ha ocurrido'
			          } ],
			      }
			},idVentana);
			bcsComunes.ventanaOverlay(idVentana,false);
		};
		
		
		$scope.data.exportarPosicion = function()
		{	
			var arLineaEnBlanco = [	{
									col1 : "",
									col2 : "",
									col3 : "",
									col4 : "",
									col5 : "",
									col6 : "",
									col7 : ""
									}
								];
								
			var arTipoCambio = [	
									{
									col1 : $translate.instant("UI_FECHA_OPERACION"),
									col2 : "",
									col3 : util.formatoDate($scope.data.kngFechaOperacion),
									col4 : "",
									col5 : "",
									col6 : "",
									col7 : ""
									},{
									col1 : $translate.instant("UI_HORA"),
									col2 : "",
									col3 : posicion.datosPosicion.P_OUT_HORA,
									col4 : "",
									col5 : "",
									col6 : "",
									col7 : ""
									},{
									col1 : "",
									col2 : $translate.instant('UI_MONEDA'),
									col3 : $translate.instant("UI_CONSOLIDADO"),
									col4 : $translate.instant("UI_T0"),
									col5 : $translate.instant("UI_T1"),
									col6 : $translate.instant("UI_T2"),
									col7 : $translate.instant("UI_T3")
									},{
									col1 : $translate.instant("UI_POSICION_INICIAL"),
									col2 : posicion.datosPosicion.IdMonedaEquivalente,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_TIPODECAMBIO.PosicionInicial  ,'GEN','Monto',monedaSeleccionada,'','').valor,
									col4 : util.bcs_formato(posicion.datosPosicion.P_OUT_TIPODECAMBIO.PosicionInicialT0,'GEN','Monto',monedaSeleccionada,'','').valor,
									col5 : util.bcs_formato(posicion.datosPosicion.P_OUT_TIPODECAMBIO.PosicionInicialT1,'GEN','Monto',monedaSeleccionada,'','').valor,
									col6 : util.bcs_formato(posicion.datosPosicion.P_OUT_TIPODECAMBIO.PosicionInicialT2,'GEN','Monto',monedaSeleccionada,'','').valor,
									col7 : util.bcs_formato(posicion.datosPosicion.P_OUT_TIPODECAMBIO.PosicionInicialT3,'GEN','Monto',monedaSeleccionada,'','').valor
									},{
									col1 : $translate.instant("UI_TIPO_CAMBIO_INICIAL"),
									col2 : posicion.datosPosicion.IdMoneda,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_TIPODECAMBIO.TipoCambioInicial  ,'GEN','Monto',monedaSeleccionada,'','').valor,
									col4 : util.bcs_formato(posicion.datosPosicion.P_OUT_TIPODECAMBIO.TipoCambioInicialT0,'GEN','Monto',monedaSeleccionada,'','').valor,
									col5 : util.bcs_formato(posicion.datosPosicion.P_OUT_TIPODECAMBIO.TipoCambioInicialT1,'GEN','Monto',monedaSeleccionada,'','').valor,
									col6 : util.bcs_formato(posicion.datosPosicion.P_OUT_TIPODECAMBIO.TipoCambioInicialT2,'GEN','Monto',monedaSeleccionada,'','').valor,
									col7 : util.bcs_formato(posicion.datosPosicion.P_OUT_TIPODECAMBIO.TipoCambioInicialT3,'GEN','Monto',monedaSeleccionada,'','').valor
									},{
									col1 : $translate.instant("UI_TIPO_CAMBIO_CIERRE_INGRESADO"),
									col2 : posicion.datosPosicion.IdMoneda,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_TIPODECAMBIO.TipoCambioCierreIngresado  ,'GEN','Monto',monedaSeleccionada,'','').valor,
									col4 : util.bcs_formato(posicion.datosPosicion.P_OUT_TIPODECAMBIO.TipoCambioCierreIngresadoT0,'GEN','Monto',monedaSeleccionada,'','').valor,
									col5 : util.bcs_formato(posicion.datosPosicion.P_OUT_TIPODECAMBIO.TipoCambioCierreIngresadoT1,'GEN','Monto',monedaSeleccionada,'','').valor,
									col6 : util.bcs_formato(posicion.datosPosicion.P_OUT_TIPODECAMBIO.TipoCambioCierreIngresadoT2,'GEN','Monto',monedaSeleccionada,'','').valor,
									col7 : util.bcs_formato(posicion.datosPosicion.P_OUT_TIPODECAMBIO.TipoCambioCierreIngresadoT3,'GEN','Monto',monedaSeleccionada,'','').valor
									},{
									col1 : $translate.instant("UI_RESULTADO_POSICION_INICIAL"),
									col2 : posicion.datosPosicion.IdMoneda,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_TIPODECAMBIO.ResultadoPosicionInicial  ,'GEN','Monto',posicion.datosPosicion.IdMoneda,'','').valor,
									col4 : util.bcs_formato(posicion.datosPosicion.P_OUT_TIPODECAMBIO.ResultadoPosicionInicialT0,'GEN','Monto',posicion.datosPosicion.IdMoneda,'','').valor,
									col5 : util.bcs_formato(posicion.datosPosicion.P_OUT_TIPODECAMBIO.ResultadoPosicionInicialT1,'GEN','Monto',posicion.datosPosicion.IdMoneda,'','').valor,
									col6 : util.bcs_formato(posicion.datosPosicion.P_OUT_TIPODECAMBIO.ResultadoPosicionInicialT2,'GEN','Monto',posicion.datosPosicion.IdMoneda,'','').valor,
									col7 : util.bcs_formato(posicion.datosPosicion.P_OUT_TIPODECAMBIO.ResultadoPosicionInicialT3,'GEN','Monto',posicion.datosPosicion.IdMoneda,'','').valor
									}
								];
		
			var arInterBancario = [	{
									col1 : $translate.instant("UI_INTERBANCARIO"),
									col2 : $translate.instant('UI_MONEDA'),
									col3 : $translate.instant("UI_CONSOLIDADO"),
									col4 : $translate.instant("UI_T0"),
									col5 : $translate.instant("UI_T1"),
									col6 : $translate.instant("UI_T2"),
									col7 : $translate.instant("UI_T3")
									},{
									col1 : $translate.instant("UI_COMPRAS_PUNTAS"),
									col2 : posicion.datosPosicion.IdMonedaEquivalente,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_INTERBANCARIO.ComprasPuntas  ,'GEN','Monto',monedaSeleccionada,'','').valor,
									col4 : util.bcs_formato(posicion.datosPosicion.P_OUT_INTERBANCARIO.ComprasPuntasT0,'GEN','Monto',monedaSeleccionada,'','').valor,
									col5 : util.bcs_formato(posicion.datosPosicion.P_OUT_INTERBANCARIO.ComprasPuntasT1,'GEN','Monto',monedaSeleccionada,'','').valor,
									col6 : util.bcs_formato(posicion.datosPosicion.P_OUT_INTERBANCARIO.ComprasPuntasT2,'GEN','Monto',monedaSeleccionada,'','').valor,
									col7 : util.bcs_formato(posicion.datosPosicion.P_OUT_INTERBANCARIO.ComprasPuntasT3,'GEN','Monto',monedaSeleccionada,'','').valor
									},{
									col1 : $translate.instant("UI_VENTAS_PUNTAS"),
									col2 : posicion.datosPosicion.IdMonedaEquivalente,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_INTERBANCARIO.VentasPuntas  ,'GEN','Monto',monedaSeleccionada,'','').valor,
									col4 : util.bcs_formato(posicion.datosPosicion.P_OUT_INTERBANCARIO.VentasPuntasT0,'GEN','Monto',monedaSeleccionada,'','').valor,
									col5 : util.bcs_formato(posicion.datosPosicion.P_OUT_INTERBANCARIO.VentasPuntasT1,'GEN','Monto',monedaSeleccionada,'','').valor,
									col6 : util.bcs_formato(posicion.datosPosicion.P_OUT_INTERBANCARIO.VentasPuntasT2,'GEN','Monto',monedaSeleccionada,'','').valor,
									col7 : util.bcs_formato(posicion.datosPosicion.P_OUT_INTERBANCARIO.VentasPuntasT3,'GEN','Monto',monedaSeleccionada,'','').valor
									},{
									col1 : $translate.instant("UI_POSICION"),
									col2 : posicion.datosPosicion.IdMonedaEquivalente,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_INTERBANCARIO.Posicion  ,'GEN','Monto',monedaSeleccionada,'','').valor,
									col4 : util.bcs_formato(posicion.datosPosicion.P_OUT_INTERBANCARIO.PosicionT0,'GEN','Monto',monedaSeleccionada,'','').valor,
									col5 : util.bcs_formato(posicion.datosPosicion.P_OUT_INTERBANCARIO.PosicionT1,'GEN','Monto',monedaSeleccionada,'','').valor,
									col6 : util.bcs_formato(posicion.datosPosicion.P_OUT_INTERBANCARIO.PosicionT2,'GEN','Monto',monedaSeleccionada,'','').valor,
									col7 : util.bcs_formato(posicion.datosPosicion.P_OUT_INTERBANCARIO.PosicionT3,'GEN','Monto',monedaSeleccionada,'','').valor
									},{
									col1 : $translate.instant("UI_PRECIO_PROMEDIO_COMPRA"),
									col2 : posicion.datosPosicion.IdMoneda,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_INTERBANCARIO.PromedioDeCompra  ,'GEN','Monto',monedaSeleccionada,'','').valor,
									col4 : util.bcs_formato(posicion.datosPosicion.P_OUT_INTERBANCARIO.PromedioDeCompraT0,'GEN','Monto',monedaSeleccionada,'','').valor,
									col5 : util.bcs_formato(posicion.datosPosicion.P_OUT_INTERBANCARIO.PromedioDeCompraT1,'GEN','Monto',monedaSeleccionada,'','').valor,
									col6 : util.bcs_formato(posicion.datosPosicion.P_OUT_INTERBANCARIO.PromedioDeCompraT2,'GEN','Monto',monedaSeleccionada,'','').valor,
									col7 : util.bcs_formato(posicion.datosPosicion.P_OUT_INTERBANCARIO.PromedioDeCompraT3,'GEN','Monto',monedaSeleccionada,'','').valor
									},{
									col1 : $translate.instant("UI_PRECIO_PROMEDIO_VENTA"),
									col2 : posicion.datosPosicion.IdMoneda,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_INTERBANCARIO.PromedioDeVenta  ,'GEN','Monto',monedaSeleccionada,'','').valor,
									col4 : util.bcs_formato(posicion.datosPosicion.P_OUT_INTERBANCARIO.PromedioDeVentaT0,'GEN','Monto',monedaSeleccionada,'','').valor,
									col5 : util.bcs_formato(posicion.datosPosicion.P_OUT_INTERBANCARIO.PromedioDeVentaT1,'GEN','Monto',monedaSeleccionada,'','').valor,
									col6 : util.bcs_formato(posicion.datosPosicion.P_OUT_INTERBANCARIO.PromedioDeVentaT2,'GEN','Monto',monedaSeleccionada,'','').valor,
									col7 : util.bcs_formato(posicion.datosPosicion.P_OUT_INTERBANCARIO.PromedioDeVentaT3,'GEN','Monto',monedaSeleccionada,'','').valor
									},{
									col1 : $translate.instant("UI_RESULTADO_INTERBANCARIO"),
									col2 : posicion.datosPosicion.IdMoneda,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_INTERBANCARIO.ResultadoInterbancario  ,'GEN','Monto',posicion.datosPosicion.IdMoneda,'','').valor,
									col4 : util.bcs_formato(posicion.datosPosicion.P_OUT_INTERBANCARIO.ResultadoInterbancarioT0,'GEN','Monto',posicion.datosPosicion.IdMoneda,'','').valor,
									col5 : util.bcs_formato(posicion.datosPosicion.P_OUT_INTERBANCARIO.ResultadoInterbancarioT1,'GEN','Monto',posicion.datosPosicion.IdMoneda,'','').valor,
									col6 : util.bcs_formato(posicion.datosPosicion.P_OUT_INTERBANCARIO.ResultadoInterbancarioT2,'GEN','Monto',posicion.datosPosicion.IdMoneda,'','').valor,
									col7 : util.bcs_formato(posicion.datosPosicion.P_OUT_INTERBANCARIO.ResultadoInterbancarioT3,'GEN','Monto',posicion.datosPosicion.IdMoneda,'','').valor
									},{
									col1 : $translate.instant("UI_POSICION_POR_ARBITRAJES"),
									col2 : posicion.datosPosicion.IdMonedaEquivalente,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_ARBITRAJEINTERBANCARIO.PosicionPorArbitrajes  ,'GEN','Monto',monedaSeleccionada,'','').valor,
									col4 : "",
									col5 : "",
									col6 : "",
									col7 : ""
									},{
									col1 : $translate.instant("UI_RESULTADO_ARBITRAJE_INTERBANCARIO"),
									col2 : posicion.datosPosicion.IdMoneda,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_ARBITRAJEINTERBANCARIO.ResultadoArbitrajes  ,'GEN','Monto',posicion.datosPosicion.IdMoneda,'','').valor,
									col4 : "",
									col5 : "",
									col6 : "",
									col7 : ""
									}
								];
		
			var arDistribucion = [	{
									col1 : $translate.instant("UI_DISTRIBUCION"),
									col2 : $translate.instant('UI_MONEDA'),
									col3 : $translate.instant("UI_CONSOLIDADO"),
									col4 : $translate.instant("UI_T0"),
									col5 : $translate.instant("UI_T1"),
									col6 : $translate.instant("UI_T2"),
									col7 : $translate.instant("UI_T3")
									},{
									col1 : $translate.instant("UI_COMPRAS_DISTRIBUCION"),
									col2 : posicion.datosPosicion.IdMonedaEquivalente,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_DISTRIBUCION.ComprasDistribucion  ,'GEN','Monto',posicion.datosPosicion.IdMonedaEquivalente,'','').valor,
									col4 : util.bcs_formato(posicion.datosPosicion.P_OUT_DISTRIBUCION.ComprasDistribucionT0,'GEN','Monto',posicion.datosPosicion.IdMonedaEquivalente,'','').valor,
									col5 : util.bcs_formato(posicion.datosPosicion.P_OUT_DISTRIBUCION.ComprasDistribucionT1,'GEN','Monto',posicion.datosPosicion.IdMonedaEquivalente,'','').valor,
									col6 : util.bcs_formato(posicion.datosPosicion.P_OUT_DISTRIBUCION.ComprasDistribucionT2,'GEN','Monto',posicion.datosPosicion.IdMonedaEquivalente,'','').valor,
									col7 : util.bcs_formato(posicion.datosPosicion.P_OUT_DISTRIBUCION.ComprasDistribucionT3,'GEN','Monto',posicion.datosPosicion.IdMonedaEquivalente,'','').valor
									},{
									col1 : $translate.instant("UI_VENTAS_DISTRIBUCION"),
									col2 : posicion.datosPosicion.IdMonedaEquivalente,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_DISTRIBUCION.VentasDistribucion  ,'GEN','Monto',monedaSeleccionada,'','').valor,
									col4 : util.bcs_formato(posicion.datosPosicion.P_OUT_DISTRIBUCION.VentasDistribucionT0,'GEN','Monto',monedaSeleccionada,'','').valor,
									col5 : util.bcs_formato(posicion.datosPosicion.P_OUT_DISTRIBUCION.VentasDistribucionT1,'GEN','Monto',monedaSeleccionada,'','').valor,
									col6 : util.bcs_formato(posicion.datosPosicion.P_OUT_DISTRIBUCION.VentasDistribucionT2,'GEN','Monto',monedaSeleccionada,'','').valor,
									col7 : util.bcs_formato(posicion.datosPosicion.P_OUT_DISTRIBUCION.VentasDistribucionT3,'GEN','Monto',monedaSeleccionada,'','').valor
									},{
									col1 : $translate.instant("UI_POSICION"),
									col2 : posicion.datosPosicion.IdMonedaEquivalente,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_DISTRIBUCION.Posicion  ,'GEN','Monto',monedaSeleccionada,'','').valor,
									col4 : util.bcs_formato(posicion.datosPosicion.P_OUT_DISTRIBUCION.PosicionT0,'GEN','Monto',monedaSeleccionada,'','').valor,
									col5 : util.bcs_formato(posicion.datosPosicion.P_OUT_DISTRIBUCION.PosicionT1,'GEN','Monto',monedaSeleccionada,'','').valor,
									col6 : util.bcs_formato(posicion.datosPosicion.P_OUT_DISTRIBUCION.PosicionT2,'GEN','Monto',monedaSeleccionada,'','').valor,
									col7 : util.bcs_formato(posicion.datosPosicion.P_OUT_DISTRIBUCION.PosicionT3,'GEN','Monto',monedaSeleccionada,'','').valor
									},{
									col1 : $translate.instant("UI_PRECIO_PROMEDIO_COMPRA"),
									col2 : posicion.datosPosicion.IdMoneda,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_DISTRIBUCION.PromedioDeCompra  ,'GEN','Monto',monedaSeleccionada,'','').valor,
									col4 : util.bcs_formato(posicion.datosPosicion.P_OUT_DISTRIBUCION.PromedioDeCompraT0,'GEN','Monto',monedaSeleccionada,'','').valor,
									col5 : util.bcs_formato(posicion.datosPosicion.P_OUT_DISTRIBUCION.PromedioDeCompraT1,'GEN','Monto',monedaSeleccionada,'','').valor,
									col6 : util.bcs_formato(posicion.datosPosicion.P_OUT_DISTRIBUCION.PromedioDeCompraT2,'GEN','Monto',monedaSeleccionada,'','').valor,
									col7 : util.bcs_formato(posicion.datosPosicion.P_OUT_DISTRIBUCION.PromedioDeCompraT3,'GEN','Monto',monedaSeleccionada,'','').valor
									},{
									col1 : $translate.instant("UI_PRECIO_PROMEDIO_VENTA"),
									col2 : posicion.datosPosicion.IdMoneda,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_DISTRIBUCION.PromedioDeVenta  ,'GEN','Monto',monedaSeleccionada,'','').valor,
									col4 : util.bcs_formato(posicion.datosPosicion.P_OUT_DISTRIBUCION.PromedioDeVentaT0,'GEN','Monto',monedaSeleccionada,'','').valor,
									col5 : util.bcs_formato(posicion.datosPosicion.P_OUT_DISTRIBUCION.PromedioDeVentaT1,'GEN','Monto',monedaSeleccionada,'','').valor,
									col6 : util.bcs_formato(posicion.datosPosicion.P_OUT_DISTRIBUCION.PromedioDeVentaT2,'GEN','Monto',monedaSeleccionada,'','').valor,
									col7 : util.bcs_formato(posicion.datosPosicion.P_OUT_DISTRIBUCION.PromedioDeVentaT3,'GEN','Monto',monedaSeleccionada,'','').valor
									},{
									col1 : $translate.instant("UI_RESULTADO_DISTRIBUCION"),
									col2 : posicion.datosPosicion.IdMoneda,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_DISTRIBUCION.ResultadoDistribucion  ,'GEN','Monto',posicion.datosPosicion.IdMoneda,'','').valor,
									col4 : util.bcs_formato(posicion.datosPosicion.P_OUT_DISTRIBUCION.ResultadoDistribucionT0,'GEN','Monto',posicion.datosPosicion.IdMoneda,'','').valor,
									col5 : util.bcs_formato(posicion.datosPosicion.P_OUT_DISTRIBUCION.ResultadoDistribucionT1,'GEN','Monto',posicion.datosPosicion.IdMoneda,'','').valor,
									col6 : util.bcs_formato(posicion.datosPosicion.P_OUT_DISTRIBUCION.ResultadoDistribucionT2,'GEN','Monto',posicion.datosPosicion.IdMoneda,'','').valor,
									col7 : util.bcs_formato(posicion.datosPosicion.P_OUT_DISTRIBUCION.ResultadoDistribucionT3,'GEN','Monto',posicion.datosPosicion.IdMoneda,'','').valor
									},{
									col1 : $translate.instant("UI_POSICION_POR_ARBITRAJES"),
									col2 : posicion.datosPosicion.IdMonedaEquivalente,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_ARBITRAJEDISTRIBUCION.PosicionPorArbitrajes  ,'GEN','Monto',monedaSeleccionada,'','').valor,
									col4 : "",
									col5 : "",
									col6 : "",
									col7 : ""
									},{
									col1 : $translate.instant("UI_RESULTADO_ARBITRAJE_DISTRIBUCION"),
									col2 : posicion.datosPosicion.IdMoneda,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_ARBITRAJEDISTRIBUCION.ResultadoArbitrajes  ,'GEN','Monto',posicion.datosPosicion.IdMoneda,'','').valor,
									col4 : "",
									col5 : "",
									col6 : "",
									col7 : ""
									}
								];
			var arForward = 	[	{
									col1 : $translate.instant("UI_FORWARD"),
									col2 : $translate.instant('UI_MONEDA'),
									col3 : "",
									col4 : "",
									col5 : "",
									col6 : "",
									col7 : ""
									},{
									col1 : $translate.instant("UI_COMPRAS_FORWARD"),
									col2 : posicion.datosPosicion.IdMonedaEquivalente,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_FWD.ComprasFwd,'GEN','Monto',monedaSeleccionada,'','').valor  ,
									col4 : "",
									col5 : "",
									col6 : "",
									col7 : ""
									},{
									col1 : $translate.instant("UI_VENTAS_FORWARD"),
									col2 : posicion.datosPosicion.IdMonedaEquivalente,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_FWD.VentasFwd,'GEN','Monto',monedaSeleccionada,'','').valor  ,
									col4 : "",
									col5 : "",
									col6 : "",
									col7 : ""
									},{
									col1 : $translate.instant("UI_DESCALCE"),
									col2 : posicion.datosPosicion.IdMonedaEquivalente,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_FWD.Descalce,'GEN','Monto',monedaSeleccionada,'','').valor  ,
									col4 : "",
									col5 : "",
									col6 : "",
									col7 : ""
									},{
									col1 : $translate.instant("UI_VENCIMIENTO_FISICO_COMPRAS"),
									col2 : posicion.datosPosicion.IdMonedaEquivalente,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_FWD.VFisicoCompras,'GEN','Monto',monedaSeleccionada,'','').valor  ,
									col4 : "",
									col5 : "",
									col6 : "",
									col7 : ""
									},{
									col1 : $translate.instant("UI_VENCIMIENTO_FISICO_VENTAS"),
									col2 : posicion.datosPosicion.IdMonedaEquivalente,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_FWD.VFisicoVentas,'GEN','Monto',monedaSeleccionada,'','').valor  ,
									col4 : "",
									col5 : "",
									col6 : "",
									col7 : ""
									},{
									col1 : $translate.instant("UI_VENCIMIENTO_FISICO_NETO"),
									col2 : posicion.datosPosicion.IdMonedaEquivalente,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_FWD.VFisicoNeto,'GEN','Monto',monedaSeleccionada,'','').valor  ,
									col4 : "",
									col5 : "",
									col6 : "",
									col7 : ""
									},{
									col1 : $translate.instant("UI_VENCIMIENTO_FUTURO_COMPRAS"),
									col2 : posicion.datosPosicion.IdMonedaEquivalente,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_FWD.VFutCompras,'GEN','Monto',monedaSeleccionada,'','').valor  ,
									col4 : "",
									col5 : "",
									col6 : "",
									col7 : ""
									},{
									col1 : $translate.instant("UI_VENCIMIENTO_FUTURO_VENTAS"),
									col2 : posicion.datosPosicion.IdMonedaEquivalente,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_FWD.VFutVentas,'GEN','Monto',monedaSeleccionada,'','').valor  ,
									col4 : "",
									col5 : "",
									col6 : "",
									col7 : ""
									},{
									col1 : $translate.instant("UI_VENCIMIENTO_FUTURO_NETO"),
									col2 : posicion.datosPosicion.IdMonedaEquivalente,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_FWD.VFutNeto,'GEN','Monto',monedaSeleccionada,'','').valor  ,
									col4 : "",
									col5 : "",
									col6 : "",
									col7 : ""
									}
								];
								
			var arConsolidadoSpot = [	{
									col1 : $translate.instant("UI_CONSOLIDADO_SPOT"),
									col2 : $translate.instant('UI_MONEDA'),
									col3 : $translate.instant("UI_CONSOLIDADO"),
									col4 : $translate.instant("UI_T0"),
									col5 : $translate.instant("UI_T1"),
									col6 : $translate.instant("UI_T2"),
									col7 : $translate.instant("UI_T3")
									},{
									col1 : $translate.instant("UI_COMPRA"),
									col2 : posicion.datosPosicion.IdMonedaEquivalente,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.Compra  ,'GEN','Monto',monedaSeleccionada,'','').valor,
									col4 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.CompraT0,'GEN','Monto',monedaSeleccionada,'','').valor,
									col5 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.CompraT1,'GEN','Monto',monedaSeleccionada,'','').valor,
									col6 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.CompraT2,'GEN','Monto',monedaSeleccionada,'','').valor,
									col7 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.CompraT3,'GEN','Monto',monedaSeleccionada,'','').valor
									},{
									col1 : $translate.instant("UI_VENTA"),
									col2 : posicion.datosPosicion.IdMonedaEquivalente,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.Venta  ,'GEN','Monto',monedaSeleccionada,'','').valor,
									col4 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.VentaT0,'GEN','Monto',monedaSeleccionada,'','').valor,
									col5 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.VentaT1,'GEN','Monto',monedaSeleccionada,'','').valor,
									col6 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.VentaT2,'GEN','Monto',monedaSeleccionada,'','').valor,
									col7 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.VentaT3,'GEN','Monto',monedaSeleccionada,'','').valor
									},{
									col1 : $translate.instant("UI_POSICION_INICIAL"),
									col2 : posicion.datosPosicion.IdMonedaEquivalente,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_TIPODECAMBIO.PosicionInicial  ,'GEN','Monto',monedaSeleccionada,'','').valor,
									col4 : util.bcs_formato(posicion.datosPosicion.P_OUT_TIPODECAMBIO.PosicionInicialT0,'GEN','Monto',monedaSeleccionada,'','').valor,
									col5 : util.bcs_formato(posicion.datosPosicion.P_OUT_TIPODECAMBIO.PosicionInicialT1,'GEN','Monto',monedaSeleccionada,'','').valor,
									col6 : util.bcs_formato(posicion.datosPosicion.P_OUT_TIPODECAMBIO.PosicionInicialT2,'GEN','Monto',monedaSeleccionada,'','').valor,
									col7 : util.bcs_formato(posicion.datosPosicion.P_OUT_TIPODECAMBIO.PosicionInicialT3,'GEN','Monto',monedaSeleccionada,'','').valor
									},{
									col1 : $translate.instant("UI_POSICION_NETA"),
									col2 : posicion.datosPosicion.IdMonedaEquivalente,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.Posicion  ,'GEN','Monto',monedaSeleccionada,'','').valor,
									col4 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.PosicionT0,'GEN','Monto',monedaSeleccionada,'','').valor,
									col5 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.PosicionT1,'GEN','Monto',monedaSeleccionada,'','').valor,
									col6 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.PosicionT2,'GEN','Monto',monedaSeleccionada,'','').valor,
									col7 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.PosicionT3,'GEN','Monto',monedaSeleccionada,'','').valor
									},{
									col1 : $translate.instant("UI_UTILIDAD_PERDIDA_POSICION"),
									col2 : posicion.datosPosicion.IdMoneda,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.UtilidadPerdidaPosicion  ,'GEN','Monto',posicion.datosPosicion.IdMoneda,'','').valor,
									col4 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.UtilidadPerdidaPosicionT0,'GEN','Monto',posicion.datosPosicion.IdMoneda,'','').valor,
									col5 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.UtilidadPerdidaPosicionT1,'GEN','Monto',posicion.datosPosicion.IdMoneda,'','').valor,
									col6 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.UtilidadPerdidaPosicionT2,'GEN','Monto',posicion.datosPosicion.IdMoneda,'','').valor,
									col7 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.UtilidadPerdidaPosicionT3,'GEN','Monto',posicion.datosPosicion.IdMoneda,'','').valor
									},{
									col1 : $translate.instant("UI_PRECIO_PROMEDIO_COMPRA"),
									col2 : posicion.datosPosicion.IdMoneda,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.PrecioPromedioCompra  ,'GEN','Monto',monedaSeleccionada,'','').valor,
									col4 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.PrecioPromedioCompraT0,'GEN','Monto',monedaSeleccionada,'','').valor,
									col5 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.PrecioPromedioCompraT1,'GEN','Monto',monedaSeleccionada,'','').valor,
									col6 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.PrecioPromedioCompraT2,'GEN','Monto',monedaSeleccionada,'','').valor,
									col7 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.PrecioPromedioCompraT3,'GEN','Monto',monedaSeleccionada,'','').valor
									},{
									col1 : $translate.instant("UI_PRECIO_PROMEDIO_VENTA"),
									col2 : posicion.datosPosicion.IdMoneda,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.PrecioPromedioVenta  ,'GEN','Monto',monedaSeleccionada,'','').valor,
									col4 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.PrecioPromedioVentaT0,'GEN','Monto',monedaSeleccionada,'','').valor,
									col5 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.PrecioPromedioVentaT1,'GEN','Monto',monedaSeleccionada,'','').valor,
									col6 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.PrecioPromedioVentaT2,'GEN','Monto',monedaSeleccionada,'','').valor,
									col7 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.PrecioPromedioVentaT3,'GEN','Monto',monedaSeleccionada,'','').valor
									},{
									col1 : $translate.instant("UI_RESULTADO_OPERACIONES"),
									col2 : posicion.datosPosicion.IdMoneda,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.ResultadoOperaciones  ,'GEN','Monto',monedaSeleccionada,'','').valor,
									col4 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.ResultadoOperacionesT0,'GEN','Monto',monedaSeleccionada,'','').valor,
									col5 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.ResultadoOperacionesT1,'GEN','Monto',monedaSeleccionada,'','').valor,
									col6 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.ResultadoOperacionesT2,'GEN','Monto',monedaSeleccionada,'','').valor,
									col7 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.ResultadoOperacionesT3,'GEN','Monto',monedaSeleccionada,'','').valor
									},{
									col1 : $translate.instant("UI_PRECIO_PROMEDIO_REFERENCIAL"),
									col2 : posicion.datosPosicion.IdMoneda,
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.PrecioPromedioReferencial  ,'GEN','Monto',monedaSeleccionada,'','').valor,
									col4 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.PrecioPromedioReferencialT0,'GEN','Monto',monedaSeleccionada,'','').valor,
									col5 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.PrecioPromedioReferencialT1,'GEN','Monto',monedaSeleccionada,'','').valor,
									col6 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.PrecioPromedioReferencialT2,'GEN','Monto',monedaSeleccionada,'','').valor,
									col7 : util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.PrecioPromedioReferencialT3,'GEN','Monto',monedaSeleccionada,'','').valor
									}
								];				
			
			if (posicion.valorBreakeven === 'S') {
				arConsolidadoSpot.push(
					{
						col1: $translate.instant("UI_MONTO_TOTAL"),
						col2: posicion.datosPosicion.IdMoneda,
						col3: util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.MontoTotalConsolidado, 'GEN', 'Monto', monedaSeleccionada, '', '').valor,
						col4: util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.MontoTotalT0, 'GEN', 'Monto', monedaSeleccionada, '', '').valor,
						col5: util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.MontoTotalT1, 'GEN', 'Monto', monedaSeleccionada, '', '').valor,
						col6: util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.MontoTotalT2, 'GEN', 'Monto', monedaSeleccionada, '', '').valor,
						col7: util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.MontoTotalT3, 'GEN', 'Monto', monedaSeleccionada, '', '').valor
					}
				);
				arConsolidadoSpot.push(
					{
						col1: $translate.instant("UI_TIPO_CAMBIO_BREAKEVEN"),
						col2: posicion.datosPosicion.IdMoneda,
						col3: util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.BreakEvenConsolidado, 'GEN', 'precio', monedaSeleccionada, '', '').valor,
						col4: util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.BreakEvenT0, 'GEN', 'precio', monedaSeleccionada, '', '').valor,
						col5: util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.BreakEvenT1, 'GEN', 'precio', monedaSeleccionada, '', '').valor,
						col6: util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.BreakEvenT2, 'GEN', 'precio', monedaSeleccionada, '', '').valor,
						col7: util.bcs_formato(posicion.datosPosicion.P_OUT_CONSOLIDADOSPOT.BreakEvenT3, 'GEN', 'precio', monedaSeleccionada, '', '').valor
					}
				);
			}
		
			
			var arResumenOpSpot = 	[	{
									col1 : $translate.instant("UI_RESUMEN_DE_OPERACIONES_SPOT"),
									col2 : "",
									col3 : "",
									col4 : "",
									col5 : "",
									col6 : "",
									col7 : ""
									},{
									col1 : $translate.instant("UI_NUMERO_DE_ANULADAS"),
									col2 : "",
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_RESUMENOPERACIONESSPOT.NumeroAnuladas  ,'GEN','Cantidad','CLP','','').valor,
									col4 : "",
									col5 : "",
									col6 : "",
									col7 : ""
									},{
									col1 : $translate.instant("UI_NUMERO_DE_COMPRAS"),
									col2 : "",
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_RESUMENOPERACIONESSPOT.NumeroCompras  ,'GEN','Cantidad','CLP','','').valor,
									col4 : "",
									col5 : "",
									col6 : "",
									col7 : ""
									},{
									col1 : $translate.instant("UI_NUMERO_DE_VENTAS"),
									col2 : "",
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_RESUMENOPERACIONESSPOT.NumeroVentas  ,'GEN','Cantidad','CLP','','').valor,
									col4 : "",
									col5 : "",
									col6 : "",
									col7 : ""
									},{
									col1 : $translate.instant("UI_TOTAL_DE_OPERACIONES"),
									col2 : "",
									col3 : util.bcs_formato(posicion.datosPosicion.P_OUT_RESUMENOPERACIONESSPOT.TotalOperaciones  ,'GEN','Cantidad','CLP','','').valor,
									col4 : "",
									col5 : "",
									col6 : "",
									col7 : ""
									}
								];
								
			//datasource
			var data = [];
			var columns = [  {field: "col1", title: "", width: "200px", align: "left" , sortable : false}
							,{field: "col2", title: "", width: "100px" , align: "left" , sortable : false}
							,{field: "col3", title: "", width: "130px", align: "right" , sortable : false}										
							,{field: "col4", title: "", width: "130px", align: "right" , sortable : false}
							,{field: "col5", title: "", width: "130px", align: "right" , sortable : false}				   				    
							,{field: "col6", title: "", width: "130px", align: "right" , sortable : false}
							,{field: "col7", title: "", width: "130px", align: "right" , sortable : false}
						  ];		
			data = _.map(columns,function(element, index){
				return { field: element.field, title: element.title};
			});
				
			var dsExcel = new bcs.DataSource({
				type : "array",
				definition : {
					data : arTipoCambio.concat(arLineaEnBlanco).concat(arInterBancario).concat(arLineaEnBlanco).concat(arDistribucion).concat(arLineaEnBlanco).concat(arForward).concat(arLineaEnBlanco).concat(arConsolidadoSpot).concat(arLineaEnBlanco).concat(arResumenOpSpot),
					pageSize: 100,
					schema : {
						model : {
							fields : {
								col1	: {type: "string"},
								col2	: {type: "string"},
								col3	: {type: "string"},
								col4	: {type: "string"},
								col5	: {type: "string"},
								col6	: {type: "string"},
								col7	: {type: "string"}
							}
						}
					}
				},
			});
		
			dsExcel.fetch().then(function(){
    			var nombreTitulo = "Posiciones";
    			dsExcel.exportToExcel(data, nombreTitulo, nombreTitulo);
    		});		
		
		};
		
	

//------------		
	});
}());