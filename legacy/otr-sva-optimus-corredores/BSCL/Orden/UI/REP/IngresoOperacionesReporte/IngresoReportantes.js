angular.module('ModuloOrdenes').controller('IngresoReportantesCtrl',function($scope,$translate,bcsComunes,bcsServicioOrden,bcsServicioPlataforma,bcsConfiguracionOrdenes,bcsConfiguracionOrdenesV2,$timeout){
	
	$scope.formData={};
	$scope.formData.fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
	
	var plazoMaximoValido = true;
	var mensajeErrorPlazo = '';
	var idVentana = bcsComunes.ventanaWinId($scope)
	$scope.ActualizarPlazoMaximo=function() {
		/*parametro plazo maximo reportes RF*/
		var dataSourceConfiguracionRF = new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering	: true, 
				serverPaging 	: true, 
				serverSorting	: true,
				transport: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=30000",
						dataType: "json"
					}		
				},
				schema: {
			     	model: {
			     		fields: { Valor : { type: "string"}	}
			     	}
				},
				filter: [{ field: "CodigoParametro"	, operator: "eq", value: 'PlazoMaximoReporteRF' },
				         { field: "IdEmpresa"		, operator: "eq", value: $scope.SesionIdEmpresa }]
			}
		});
		
		dataSourceConfiguracionRF.fetch(function(){
			if(dataSourceConfiguracionRF.data().length > 0){
				$scope.formData.plazoReportante = $scope.formData.TipoInstrumento.IdTipoInstrumento === 'RRF' ? dataSourceConfiguracionRF.data()[0].Valor : $scope.formData.plazoReportante;
			}
	 	});

		/*parametro plazo maximo reportes RV*/
		var dataSourceConfiguracionRV = new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering	: true, 
				serverPaging 	: true, 
				serverSorting	: true,
				transport: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=30000",
						dataType: "json"
					}		
				},
				schema: {
			     	model: {
			     		fields: { Valor : { type: "string"}	}
			     	}
				},
				filter: [{ field: "CodigoParametro"	, operator: "eq", value: 'PlazoMaximoReporteRV' },
				         { field: "IdEmpresa"		, operator: "eq", value: $scope.SesionIdEmpresa }]
			}
		});			
			
		dataSourceConfiguracionRV.fetch(function(){
			if(dataSourceConfiguracionRV.data().length > 0){
				$scope.formData.plazoReportante = $scope.formData.TipoInstrumento.IdTipoInstrumento === 'RRV' ? dataSourceConfiguracionRV.data()[0].Valor : $scope.formData.plazoReportante;
			}
	 	});
		
		$scope.$watch('formData.plazoReportante',function(newval,oldval){
			if(oldval != undefined){				
				var plazoMaximo = 0;

				if(dataSourceConfiguracionRV.data().length > 0)
					plazoMaximo = $scope.formData.TipoInstrumento.IdTipoInstrumento === 'RRV' ? dataSourceConfiguracionRV.data()[0].Valor : plazoMaximo;
				
				if(dataSourceConfiguracionRF.data().length > 0)
					plazoMaximo = $scope.formData.TipoInstrumento.IdTipoInstrumento === 'RRF' ? dataSourceConfiguracionRF.data()[0].Valor : plazoMaximo;
				
				if(plazoMaximo === 0)
					return;
				
				if(newval > plazoMaximo){
					mensajeErrorPlazo = 'El Plazo ' + newval + ' excede al plazo Maximo ' + plazoMaximo;
					bcsComunes.msg("error","Error", mensajeErrorPlazo);
					plazoMaximoValido = false;
				}else{
					plazoMaximoValido = true;
				}
			}	
			bcsComunes.ventanaAutoHeight(idVentana);
		});
	};
	
	$scope.formData= 
	{
			idMedioPagoDefecto	 			:	'CIN',
			modo				 			:	'INGRESO',
			fechaSistema		 			:	util.dateInicioDia($scope.SesionFechaSistema),
			//fechaVencimientoVigencia		:	$scope.formData.fechaSiguiente,//util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL"),
			//fechaVencimiento		 		:	util.dateInicioDia($scope.SesionFechaSistema),
			cuentaInversion					: 	true,
			idBaseTasaDefecto           	: 	2,
			idAutorizadoDarOrdenesDefecto	:	undefined,
			idMecanismoDeCaptacionDefecto	:	undefined,
			idTipoInstrumento	 			:	{},
			idTipoInstrumentoDefecto		:	'RRV',
			idMonedaReportanteDefecto		:	$scope.SesionIdMonedaSistema,
			plazoReportante					:	bcsConfiguracionOrdenes.CONFIGURACION.PlazoMaximoReporteRV,
			vigenciaReportado				:	0,
			idCondicionDeLiquidacionDefecto	:	'T0',
			TipoInstrumento					:   {},	
			
			configuracionMecanismoCaptacion : {
				filtros: {
					CodigoTipoIndicador : { "field"   : "CodigoTipoIndicador","operator": "eq", "value"   :  "ORDMODALIDAD" },
					IdEntidad : {"field":"IdEntidad","operator":"eq","value": -1 },
				},
			}
	};
		
	$scope.idBolsa='0';//'BCS';
	$scope.especificaBolsa='N';//'S';
	$scope.filtros = {};
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.idMonedaDefecto = $scope.SesionIdMonedaSistema;
	$scope.parametros = bcsComunes.ventanaParams($scope.winID);
	$scope.esReportado = $scope.parametros.ordenReportado ? false : true;
	$scope.todos = $scope.parametros.ordenReportado ? 'N' : 'S';
	$scope.desdeReportado = false;
	$scope.datosResumen = {};
	
	$scope.oConfiguracionTipoInstrumento= {
			filtros:{
				Codigo				:{"field" : "IdClasificacionTipoInstrumento", "operator": "eq", "value":"RE"},
				Accion				:{"field" : "Accion", "operator": "eq", "value":"COMPRA"}
			}
	}
	$scope.filtros.configuracionCuentaCustodio = {
			
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   : "" },
			IdEstadoAutorizacion : { "field"   : "IdEstadoAutorizacion","operator": "eq", "value"   : "AUT" },
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
			IdModeloCustodia : { "field"   : "IdModeloCustodia","operator": "eq", "value"   : "LOCAL" }
	
	};
	//Cargada desde vista reportado, para agregar nuevo reportante	
	if($scope.parametros.ordenReportado){
		var operacion = $scope.parametros.ordenReportado.IdTipoOperacion === 'VENRRF' ? 'RRF' : 'RRV';
		$scope.formData.idTipoInstrumentoDefecto=operacion;
		$scope.oConfiguracionTipoInstrumento.filtros.IdTipoInstrumento = {"field":"IdTipoInstrumento","operator":"eq","value":operacion}
		var aux=$scope.parametros.ordenReportado.TasaReportanteReferencia;//-1;
		$scope.formData.tasaReportante = aux<=0? 0:aux;//TasaReportanteReferencia;
		$scope.formData.plazoReportante=$scope.parametros.ordenReportado.Plazo;
		$scope.formData.fechaVencimiento=$scope.parametros.ordenReportado.FechaVencimiento;
		$scope.formData.montoReportante=$scope.parametros.ordenReportado.MontoDisponibleAsociar;
		$scope.desdeReportado = true;
		$scope.formData.tasaReportadoReferencia= $scope.parametros.ordenReportado.TasaReportadoReferencia;
		
		$scope.formData.IdInstrumentoSubyacente = $scope.parametros.ordenReportado.IdInstrumentoSubyacente;
		$scope.formData.IdEmisor = $scope.parametros.ordenReportado.IdEmisor;
		$scope.formData.Cantidad = $scope.parametros.ordenReportado.CantidadDisponibleAsociar;
		$scope.formData.IdBolsa = $scope.parametros.ordenReportado.IdBolsa;
		$scope.formData.IdBoveda = $scope.parametros.ordenReportado.IdBoveda;
		
		$scope.formData.MonedaReporte = $scope.parametros.ordenReportado.IdMonedaOrden;
		$scope.formData.PrecioOrden = $scope.parametros.ordenReportado.PrecioOrden;
		
		
	}else {
		$scope.formData.IdInstrumentoSubyacente = 0;
		$scope.formData.IdEmisor = 0;
		$scope.formData.Cantidad = 1;
		$scope.formData.IdBolsa = $scope.SesionBolsaDefectoSistema;
		$scope.formData.IdBoveda = $scope.SesionBovedaDefectoSistema;
	}

	$scope.oConfiguracionPortafolio= 
	{
		filtros: {
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  0 },
			IdEmpresa : {"field":"IdEmpresa","operator":"eq","value": $scope.SesionIdEmpresa}
			
		}
	}
	$scope.oConfiguracionMonedaPortafolio=
	{
		filtros : {
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   : undefined },
			
		},
		
	}
	$scope.filtros.filtrosFormaCustodia = {
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
	};
	
	$scope.oConfiguracionMedioDePagoCargo=
	{
			tipo : "combobox-medio-pago",
			configuracion : {},
			filtros : {},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	bcsComunes.ventanaTamanoAutomatico($scope.winID,50);
	
	$scope.oConfiguracionPreferenciaPagoPortafolio=
	{
			filtros 			: {
				idMedioDePago : { "field"   : "IdMedioDePago",operator: "neq", "value"   :  "CIN" },
		        IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  "-1" },
		        IdMoneda : { "field"   : "IdMoneda","operator": "eq", "value"   :  $scope.SesionIdMonedaSistema },
		        IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   : $scope.formData.TipoInstrumento ? $scope.formData.TipoInstrumento.IdTipoOperacion : "" },
		        
			},
	};
	
	$scope.$watchGroup(['oConfiguracionPreferenciaPagoPortafolio.filtros.IdPortafolio'],function(newval,oldval) {
		
		if ($scope.filtros.autorizadoDarOrdenes && $scope.filtros.autorizadoDarOrdenes.IdPortafolio && newval[0]) {			
			$scope.filtros.autorizadoDarOrdenes.IdPortafolio=newval[0];
		}else{
		     	$scope.filtros.autorizadoDarOrdenes.IdPortafolio=undefined;		
			 }		
	});
	
	
	$scope.filtros.autorizadoDarOrdenes = {
			IdPortafolio:{"field":"IdPortafolio","operator":"eq","value":-1},
			IdEstado:{"field":"IdEstado","operator":"eq","value":'ACT'}
		};
	
		
	$scope.filtros.mecanismoDeCaptacion = {
			IdPortafolio:{"field":"CodigoTipoIndicador","operator":"eq","value":"ORDMODALIDAD"}
		};
	
	$scope.verComprobante=function(){
		bcsConfiguracionOrdenes.FUNCIONES.verComprobante($scope.SesionIdEmpresa,
				$scope.datosResumen.documentoIdentidad,
				$scope.datosResumen.fechaOperacion,
				$scope.datosResumen.idOrden,
				$scope.datosResumen.montoLiquidacionContado,
				$scope.winID);
	};
	
	$scope.verDetalle=function() {
		bcsComunes.ventanaAbrir("DetalleReportante", {IdOrden : $scope.datosResumen.idOrden} );
	};
	
	
	
	
	
	$scope.$watch('formData.clienteSeleccionado',function(newval,oldval){
		if (newval){
			         $scope.formData.configuracionMecanismoCaptacion = {
					    filtros: {
						    CodigoTipoIndicador : { "field"   : "CodigoTipoIndicador","operator": "eq", "value"   :  "ORDMODALIDAD" },
						    IdEntidad : {"field":"IdEntidad","operator":"eq","value": newval.IdPortafolio },
				 	     }
				     };			
			   }
	 });
	
	
	$scope.formData.ingresarOrdenReportante = function(a) {
	
		if(!plazoMaximoValido){
			bcsComunes.msg("error","Error", mensajeErrorPlazo );
			return;
		}
		
		var IdVentana=$scope.winID;
		var datosIngresoOrden = {
				p_IdEmpresa 				:	$scope.SesionIdEmpresa, //saca de variables de entorno
				p_IdUsuario 				:	$scope.SesionIdUsuario,//4 //saca de variables de entorno
				p_IdModulo  				:	'ORD',
				p_NuevoIngreso 				:	'S',
				p_simularOperacion  		:	'N',
				p_UsaReglasNegocio  		:	'S',
				p_Garantias					:	[],
				p_CobrosModificados 		: 	[],
				p_Ordenes 					:	[{
					IdInterno 						:	0,
				    FechaRecepcion 					:	'0001-01-01 00:00:00',
					FechaVigencia  					: 	($scope.formData.fechaVencimientoVigencia)? util.dateSPdate($scope.formData.fechaVencimientoVigencia) :  '0001-01-01',
					FechaEmision   					: 	'0001-01-01',
					FechaOperacion 					:	'0001-01-01', //se calcula en el server
					IdTipoOperacion 				:	$scope.formData.TipoInstrumento.IdTipoOperacion,
					IdCondicionDeLiquidacion 		:	$scope.formData.idCondicionDeLiquidacionDefecto,
					IdTipoVigencia 					: 	'FF',
					Glosa 							:	$scope.formData.comentario || '',
					Cantidad 						: 	$scope.formData.Cantidad,//1,
					MontoNeto 						:	$scope.formData.montoReportante,
					ValorRescate	 				: 	0,
					IdBoveda 						: 	$scope.formData.IdBoveda,//null,
					IdFormaCustodia 				:	$scope.formData.formaCustodiaSeleccionada.IdFormaCustodiaOrden,//'NOM',
					IdPorcentajeVisibilidad 		:	1,
					PrecioLimite 					:	$scope.parametros.ordenReportado?$scope.formData.PrecioOrden:0,
					PrecioMercado 					:	$scope.parametros.ordenReportado?$scope.formData.PrecioOrden:0,
					EsFacturaInmediata 				:	'N',
					IdTipoPrecio 					:	'M',
					IdPortafolio 					:	$scope.formData.clienteSeleccionado.IdPortafolio,
					IdTipoLibroOrden 				:	'NORMAL',
					IdTipoCanalIngreso 				:	$scope.SesionTipoCanalIngreso,
					IdTipoReajuste 					:	'',
					IdEmisor						:	$scope.formData.IdEmisor,//'0',
					IdBolsa 						:	$scope.formData.IdBolsa,
					EspecificaBolsa 				:	$scope.especificaBolsa,
					IdMonedaLiquidacionOrden 		:	$scope.parametros.ordenReportado?$scope.formData.MonedaReporte:$scope.formData.monedaSeleccionada.IdMoneda,
					TipoCambio 						:	null,
					IdInstrumento 					:	$scope.formData.TipoInstrumento.IdInstrumento,
					IdInstrumentoSubyacente 		:	$scope.formData.IdInstrumentoSubyacente,//0,
					IdCondicionDeLiquidacionPlazo 	: 	'',
					TasaVentaCorta 					:	0,
					TasaSimultanea 					:	0,
					TasaRFIF 						:	0,
					PlazoIF 						:	0,
					PlazoSimultanea 				:	0,
					PlazoVentaCorta 				:	0,
					EsVentaCorta 					:	'N',
					EsOD							:	'N',
					EsFungible 						:	'N',
					IdCorredorCorresponsal 			:	null,
					MontoPlazo 						:	0,
					IdTipoCanalDistribucion 		:	'MAN',
					TasaReporte						:	$scope.formData.tasaReportante,
					TasaPromedioReporte				:	$scope.formData.tasaReportante,
					PlazoReporte					:	$scope.formData.plazoReportante,
					BaseTasa						:	$scope.formData.baseTasa.ValorBaseTasaCobro,
					PagoCobroCuentaInversion		:	$scope.formData.cuentaInversion ? 'S' : 'N',
					IdPreferenciaPagoPortafolio		:	$scope.formData.cuentaInversion ? null:($scope.formData.idPreferenciaPagoPortafolio ? $scope.formData.idPreferenciaPagoPortafolio:null),
					TipoInteres						:	'C',
					
					IdPersonaAutorizadoDarOrdenes 	: 	($scope.formData.autorizadoDarOrdenes&&$scope.formData.autorizadoDarOrdenes.IdPersona)?$scope.formData.autorizadoDarOrdenes.IdPersona:null,
					IdMecanismoCaptacion 			: 	$scope.formData.mecanismoDeCaptacion?$scope.formData.mecanismoDeCaptacion.idActual:'',
					IdCuentaCustodioPortafolio		:	$scope.formData.cuentaCustodioSeleccionada.IdCuentaCustodioPortafolio || null,
						
				}]
			
				
		};
				
		bcsComunes.ventanaOverlay($scope.winID,true);
		console.log(JSON.stringify(datosIngresoOrden));
		if($scope.parametros.ordenReportado){
			datosIngresoOrden.p_Ordenes[0].TasaPromedioReporte=$scope.parametros.ordenReportado.TasaPromedioReporte;
			//Segun se define: No debe haber una validación entre la tasa del Reportado y Reportante
			if(/*$scope.formData.tasaReportante > $scope.formData.tasaReportadoReferencia || */$scope.formData.montoReportante>$scope.parametros.ordenReportado.MontoDisponibleAsociar){
				bcsComunes.ventanaOverlay($scope.winID,false);
				bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_NO_SE_PUDO_INGRESAR_ORDEN"),
				
				},$scope.winID);
				return;
			}
				
		}
		
		var promise = bcsServicioOrden.CrearOrdenes(datosIngresoOrden).$promise;
		promise.then(function(respuesta){
			console.log("Respuesta:");
			console.log(JSON.stringify(respuesta));
			if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_RESULTADO[0].IdOrden > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0) 
			{
				
				$scope.datosResumen = {
						idOrden				:	respuesta.P_OUT_RESULTADO[0].IdOrden,
						clientePortafolio	:	respuesta.P_OUT_RESULTADO[0].DocumentoIdentidad+' '+respuesta.P_OUT_RESULTADO[0].NombreCompleto,//$scope.formData.clienteSeleccionado.NombreCompletoPortafolio,
						moneda				:	respuesta.P_OUT_RESULTADO[0].IdMonedaLiquidacionOrden,//.IdMonedaOrden,
						tasa				:	respuesta.P_OUT_RESULTADO[0].TasaReporte,	//$scope.formData.tasaReportada,
						fechaVencimiento	:	$scope.formData.fechaVencimiento,//util.dateDS($scope.formData.fechaVencimiento,"FechaSistema","CALL"),//$scope.formData.fechaVencimiento,
						base				:	respuesta.P_OUT_RESULTADO[0].BaseTasa,//BaseReporte,//$scope.formData.base,
						fechaVigencia		:	respuesta.P_OUT_RESULTADO[0].FechaVigencia,//$scope.formData.fechaVencimientoVigencia,
						idMoneda			:	respuesta.P_OUT_RESULTADO[0].IdMonedaOrden,//$scope.formData.monedaSeleccionada.IdMoneda,
						idMonedaLiquidacion	:	respuesta.P_OUT_RESULTADO[0].IdMonedaLiquidacionOrden,//$scope.formData.monedaSeleccionada.IdMoneda,
						monto				:	respuesta.P_OUT_RESULTADO[0].MontoNeto,//$scope.formData.montoReportante,
						medioPago			:	respuesta.P_OUT_RESULTADO[0].IdMedioDePago,//$scope.oConfiguracionMedioDePagoCargo.seleccionado.Nombre,
						comentario			:	respuesta.P_OUT_RESULTADO[0].Glosa,//'',//$scope.formData.comentario,
						montoTotal			:	respuesta.P_OUT_RESULTADO[0].MontoTotal,
						cantidad			:	respuesta.P_OUT_RESULTADO[0].Cantidad,
						totalCobro			:	respuesta.P_OUT_RESULTADO[0].TotalCobros,
						preferenciaPago		:	$scope.formData.PreferenciaPagoPortafolioseleccionado.TextoPreferenciaPago || 'Cuenta Inversión',
						fechaOperacion		:	util.dateDS(respuesta.P_OUT_RESULTADO[0].FechaOperacionOrden,"FechaHoraSistema","CALL"),
						documentoIdentidad	: 	respuesta.P_OUT_RESULTADO[0].DocumentoIdentidad,
						nombreFormaCustodia :   $scope.formData.formaCustodiaSeleccionada.Nombre,
				
						
						decimales			: 	util.bcs_formato(0,'ORD','Monto',$scope.formData.monedaSeleccionada.IdMoneda,'RRV','').cantidadDecimales,
						decimalesPrecio		:	util.bcs_formato(0,'ORD','Precio', $scope.formData.montoReportante,'RRV','').cantidadDecimales,
						decimalesCantidad	:	util.bcs_formato(0,'ORD','Cantidad',$scope.formData.baseTasa.ValorBaseTasaCobro,'RRV','').cantidadDecimales,
				
				};
				
				bcsComunes.ventanaOverlay($scope.winID,false);
				$scope.formData.modo = 'RESUMEN';
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ORDEN_INGRESADA")+": " + respuesta.P_OUT_RESULTADO[0].IdOrden,
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
				
				
				if($scope.parametros.ordenReportado){
					console.log("reportado"+JSON.stringify($scope.parametros.ordenReportado));
					console.log("reportante"+JSON.stringify(respuesta.P_OUT_RESULTADO[0]));
					//No debe haber una validación entre la tasa del Reportado y Reportante Segun Jimmy Lam
					if(respuesta.P_OUT_RESULTADO[0].MontoNeto<=$scope.parametros.ordenReportado.MontoDisponibleAsociar /*&& $scope.datosResumen.tasa<=$scope.parametros.ordenReportado.TasaReportadoReferencia*/)
					{
						var datos = {
							p_IdEmpresa   : $scope.SesionIdEmpresa,
							p_IdUsuario   : $scope.SesionIdUsuario,
							p_IdModulo 	  : 'ORD',
							p_simularOperacion : 'N',
							p_UsaReglasNegocio : 'S',
							p_Asociaciones : [{
								IdInterno: respuesta.P_OUT_RESULTADO[0].IdOrden, 
								IdOrdenN : $scope.parametros.ordenReportado.IdOrden,
								IdOrdenM: respuesta.P_OUT_RESULTADO[0].IdOrden,
								Precio	 : $scope.parametros.ordenReportado.PrecioOrden,
								ModalidadCalculo : $scope.parametros.ordenReportado.IdTipoInstrumento === 'RRF'?'R':'M',
								Monto :respuesta.P_OUT_RESULTADO[0].MontoNeto,
								Cantidad: 0,
								Tasa : $scope.parametros.ordenReportado.Tasa,
								}]
						}
						bcsServicioOrden.CrearAsociaciones(datos).$promise.then(function(respuesta){
						if(respuesta.P_OUT_ERRORESCONTADOR == 0){
							bcsComunes.ventanaOverlay($scope.winID,false);
							var id = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ASOCIAR_REPORTANTE"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
							var promesa=bcsComunes.ventanaPromesa(id);                               
						     $.when(promesa).then(function(data){
						    	 $timeout(function() {
						    		 bcsComunes.ventanaCerrar($scope.winID);
						    	 },20);
						     });
						}else{
							bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ASOCIAR_REPORTANTE"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
						}
				     	bcsComunes.ventanaCerrar($scope.winID);
						},function(respuesta){
							bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ASOCIAR_REPORTANTE"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
							bcsComunes.ventanaCerrar($scope.winID);
						});
					
					}
					else
					{
						var id = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ASOCIAR_REPORTANTE"),conflictos:''},$scope.winID);
						var promesa=bcsComunes.ventanaPromesa(id);                               
					     $.when(promesa).then(function(data){
					    	 $timeout(function() {
					    		 bcsComunes.ventanaCerrar($scope.winID);
					    	 },20);
					     });
						
						
					}
					
				}else{
					/*bcsComunes.ventanaOverlay($scope.winID,false);
					$scope.formData.modo = 'RESUMEN';
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_ORDEN_INGRESADA")+": " + respuesta.P_OUT_RESULTADO[0].IdOrden,
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);*/
				}
				
			}
			else
			{
				bcsComunes.ventanaOverlay($scope.winID,false);
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_NO_SE_PUDO_INGRESAR_ORDEN"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
			}
		},
			function(respuesta){
	   			bcsComunes.ventanaOverlay($scope.winID,false);
				bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_EXISTEN_PROBLEMAS_PARA_INGRESAR_ORDEN"),
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);
			});
		
								
	}
	$scope.formData.cleanData = function() {
				
		
		//$scope.formData.fechaVencimiento=undefined;
		//$scope.formData.baseTasa=undefined;
		//$scope.formData.fechaVencimientoVigencia=undefined;
		//$scope.formData.montoReportante=undefined;
		//$scope.formData.plazoReportante=180;
		//$scope.formData.vigenciaReportado=undefined;
		//$scope.formData.comentario=undefined;
		//$scope.formData.idMedioDePagoCargo=$scope.formData.idMedioPagoDefecto;
		//$scope.formData.idAutorizadoDarOrdenes=$scope.formData.idAutorizadoDarOrdenesDefecto;
		//$scope.formData.idMecanismoDeCaptacion=$scope.formData.idMecanismoDeCaptacionDefecto;
		//$scope.formData.idMonedaReportanteDefecto=$scope.SesionIdMonedaSistema;
		
		$scope.formData.comentario=undefined;
		$scope.formData.montoReportante					= undefined;
		$scope.formData.idMedioPagoDefecto	 			=	'CIN';
		$scope.formData.modo				 			=	'INGRESO';
		$scope.formData.fechaSistema		 			=	util.dateInicioDia($scope.SesionFechaSistema);
		//fechaVencimientoVigencia		:	$scope.formData.fechaSiguiente,//util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL"),
		//fechaVencimiento		 		:	util.dateInicioDia($scope.SesionFechaSistema),
		$scope.formData.clienteDefecto					=   "-1";
		$scope.formData.cuentaInversion					= 	true;
		$scope.formData.idBaseTasaDefecto           	= 	2;
		$scope.formData.idBaseTasa						=	2;
		$scope.formData.idAutorizadoDarOrdenesDefecto	=	undefined;
		$scope.formData.idMecanismoDeCaptacionDefecto	=	undefined;
		$scope.formData.idTipoInstrumento	 			=	{};
		$scope.formData.idTipoInstrumentoDefecto		=	'RRV';
		$scope.formData.idTipoInstrumento				=	'RRV';
		$scope.formData.idTipoInstrumentoPostFiltro		= 	'RRV';
		$scope.formData.idMonedaReportanteDefecto		=	$scope.SesionIdMonedaSistema;
		
		//$scope.formData.plazoReportante					=	bcsConfiguracionOrdenes.CONFIGURACION.PlazoMaximoReporteRV;
		$scope.ActualizarPlazoMaximo();
		
		$scope.formData.vigenciaReportado				=	0;
		$scope.formData.idCondicionDeLiquidacionDefecto	=	'T0';
		$scope.formData.TipoInstrumento					= {};
		$scope.formData.IdSocioNegocio                  = false;
		$scope.formData.clienteSeleccionado             = undefined;
		$scope.formData.mecanismoDeCaptacion.idActual  = false;
					
	};
	
	$scope.formData.volver = function(respuesta) {
		$scope.formData.modo = 'INGRESO';
		$scope.formData.cleanData();
		bcsComunes.ventanaTamanoAutomatico($scope.winID,30);
				
	};
	
	$scope.$watch('formData.TipoInstrumento',function(newval,oldval)
	{
		$scope.ActualizarPlazoMaximo();			
	});
	
	$scope.ActualizarPlazoMaximo();
	bcsComunes.ventanaAutoHeight(idVentana);
});
