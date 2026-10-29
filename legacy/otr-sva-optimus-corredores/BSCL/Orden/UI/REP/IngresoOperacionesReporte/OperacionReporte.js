angular.module('ModuloOrdenes').controller('OperacionReporteCtrl',function($scope,$translate,bcsComunes,bcsServicioOrden,bcsConsultasOrdenes,bcsServicioPlataforma,bcsConfiguracionOrdenes,bcsConfiguracionOrdenesV2,bcsServiciosPlataformaMercado,$timeout,$q){
	
	var controllerName = 'OperacionReporteCtrl';
	$scope.parametros = bcsComunes.ventanaParams(bcsComunes.ventanaWinId($scope));
	console.log("PARAMETROS:"+JSON.stringify($scope.parametros));
	var plazoMaximoValido = true;
	var mensajeErrorPlazo = '';
	var idVentana = bcsComunes.ventanaWinId($scope)
	
	$scope.simulado=false;
	$scope.formData={};
	$scope.formEditar={};
	$scope.formResumen={};
	
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
				$scope.formData.diasFechaVencimiento = $scope.formData.TipoInstrumento.IdTipoInstrumento === 'RRF' ? dataSourceConfiguracionRF.data()[0].Valor : $scope.formData.diasFechaVencimiento;
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
			if(dataSourceConfiguracionRV.data().length > 0 && $scope.formData.TipoInstrumento.IdClasificacionTipoInstrumento != undefined ){
				$scope.formData.diasFechaVencimiento = $scope.formData.TipoInstrumento.IdClasificacionTipoInstrumento === 'RV' ? dataSourceConfiguracionRV.data()[0].Valor : $scope.formData.diasFechaVencimiento;
			}
	 	});
		
		$scope.$watch('formData.diasFechaVencimiento',function(newval,oldval){
			if(oldval != undefined){				
				var plazoMaximo = 0;

				if(dataSourceConfiguracionRV.data().length > 0)
					plazoMaximo = $scope.formData.TipoInstrumento.IdClasificacionTipoInstrumento === 'RV' ? dataSourceConfiguracionRV.data()[0].Valor : plazoMaximo;
				
				if(dataSourceConfiguracionRF.data().length > 0)
					plazoMaximo = $scope.formData.TipoInstrumento.IdClasificacionTipoInstrumento === 'RF' ? dataSourceConfiguracionRF.data()[0].Valor : plazoMaximo;
				
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

	
	var reportantesCobros = {};
	$scope.reportantes = {};
	if($scope.parametros)
		$scope.modo=$scope.parametros.param=='EDITAR'?'EDITAR':'INGRESAR';
	else
		$scope.modo='INGRESAR';
	
	if($scope.modo==='EDITAR'){
		console.log($scope.parametros.orden[0].FechaLiquidacion);
		
		$scope.formEditar=
		{
			idOrden					 :$scope.parametros.orden[0].IdOrden,
			tipoInstrumento			 :$scope.parametros.orden[0].IdTipoInstrumento+' - '+$scope.parametros.orden[0].NombreTipoInstrumento,
			clientePortafolio		 :$scope.parametros.orden[0].DocumentoIdentidad+' '+$scope.parametros.orden[0].NombreCompleto+'('+$scope.parametros.orden[0].CodigoPortafolio+')',
			instrumentoSubyacente	 :$scope.parametros.orden[0].InstrumentoSubyacente,
			idEmisor				 :$scope.parametros.orden[0].IdEmisor,
			precio					 :$scope.parametros.orden[0].PrecioOrden,
			mercado					 :$scope.parametros.orden[0].PrecioMercado,
			montoContado			 :$scope.parametros.orden[0].MontoOrden,
			tasa					 :$scope.parametros.orden[0].Tasa,
			moneda					 :$scope.parametros.orden[0].IdMonedaOrden,
			monedaLiquidacion		 :$scope.parametros.orden[0].IdMonedaLiquidacionOrden,	
			precioTc				 :0,
			cantidad				 :$scope.parametros.orden[0].CantidadOrden,
			precioMercado			 :$scope.parametros.orden[0].PrecioMercado,
			codigoCondicionLiquidacion  :$scope.parametros.orden[0].CodigoCondicionDeLiquidacion,
			fechaLiquidacion		 : $scope.parametros.orden[0].FechaLiquidacion,
			plazoDiasFechaVencimiento:$scope.parametros.orden[0].Plazo,
			plazoFechaVencimiento	 :$scope.parametros.orden[0].FechaVencimiento,
			medioPago				 :$scope.parametros.orden[0].IdMedioDePago === 'CIN' ? '' : $scope.parametros.orden[0].IdMedioDePago,
			baseTasa				 :$scope.parametros.orden[0].BaseTasa,
			comisionSabReportado	 :'',
			glosa					 :$scope.parametros.orden[0].GlosaOrden,
			tasaPromedioR			 :$scope.parametros.orden[0].TasaPromedioReporte,
			tieneReportante			 :$scope.parametros.orden[0].TasaReportante!=-1?true:false,
			cuentaInversion			 :$scope.parametros.orden[0].IdMedioDePago === 'CIN' ? true : false,
		
			simulado				 :	false,
			idTipoInstrumento		 : $scope.parametros.orden[0].IdTipoInstrumento,
			tasaReportanteV			 : $scope.parametros.orden[0].TasaReportanteReferencia,
			tasaReportadoV			 : $scope.parametros.orden[0].TasaReporte,
			idBolsa					 : $scope.parametros.orden[0].IdBolsa,
			idPortafolio			 : $scope.parametros.orden[0].IdPortafolio,
			fechaVigencia			 :($scope.parametros.orden[0].FechaVigencia)? util.dateSPdate($scope.parametros.orden[0].FechaVigencia) :  '0001-01-01',
			idTipoOperacion			     : $scope.parametros.orden[0].IdTipoOperacion,
			idCondicionDeLiquidacion     : $scope.parametros.orden[0].IdCondicionDeLiquidacion,
			idInstrumento 			     : $scope.parametros.orden[0].IdInstrumento,
			idInstrumentoSubyacente	     : $scope.parametros.orden[0].IdInstrumentoSubyacente,
			idPersonaAutorizadoDarOrdenes: $scope.parametros.orden[0].IdPersonaAutorizadoDarOrdenes,
			codigoClaseOrden			 : $scope.parametros.orden[0].CodigoClaseOrden,
			textoPreferenciaPago		 : $scope.parametros.orden[0].TextoPreferenciaPago,
			nombreFormaCustodia			 : $scope.parametros.orden[0].NombreFormaCustodia,
			idFormaCustodia			 	 : $scope.parametros.orden[0].IdFormaCustodia,
			idTipoInstrumento			 : $scope.parametros.orden[0].IdTipoInstrumento,
			idCuentaCustodioPortafolio	 : $scope.parametros.orden[0].IdCuentaCustodioPortafolio,				
		};
	
		$scope.formResumen =
		{		principalContado		:$scope.parametros.orden[0].MontoOrden,
				montoLiquidacionContado	:$scope.parametros.orden[0].MontoLiquidacion,
				principalPlazo			:$scope.parametros.orden[0].MontoOrdenPlazo,
				montoLiquidacionPlazo	:$scope.parametros.orden[0].MontoLiquidacionPlazo,
				precioPlazo				:$scope.parametros.orden[0].PrecioPlazo,//respuesta.P_OUT_RESULTADO[0].PrecioPlazo,
				//MontoInteresPlazo			:$scope.parametros.orden[0].MontoOrdenPlazo-$scope.parametros.orden[0].MontoOrden,//(respuesta.P_OUT_RESULTADO[0].MontoNetoPlazo-respuesta.P_OUT_RESULTADO[0].MontoNeto),
				saldoAsignar			:$scope.parametros.orden[0].CantidadDisponibleAsociar,//respuesta.P_OUT_RESULTADO[0].MontoNeto,
				tasaPromedio			:$scope.parametros.orden[0].TasaPromedioReporte,//promedio tasa reportante y tasa reportado
				monedaOrden				:$scope.parametros.orden[0].IdMonedaOrden,//respuesta.P_OUT_RESULTADO[0].IdMonedaOrden,
				monedaLiq				:$scope.parametros.orden[0].IdMonedaLiquidacionOrden,//respuesta.P_OUT_RESULTADO[0].IdMonedaLiquidacionOrden
				MontoInteresContado		:$scope.parametros.orden[0].MontoInteresContado,
				MontoInteresPlazo		:$scope.parametros.orden[0].MontoInteresPlazo,
		};
		
	}
	else{
	
		//Seteamos data por defecto
		$scope.formData= 
		{
				idMedioPagoDefecto	 			:	'CIN',
				fechaInicial		 			:	util.dateInicioDia($scope.SesionFechaSistema),
				fechaVigencia		 			:	util.dateInicioDia($scope.SesionFechaSistema),//util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL"),
				mostrarReportado	 			:	true,
				mostrarReportante	 			:	false,
				TipoInstrumento		 			:	{},
				Bolsaseleccionado	 			:	{},
				idTipoInstrumento	 			:	{},
			
				factorConversion				: 	undefined,
				idTipoInstrumentoDefecto		:	'RV',
				bloqueo							:	false,
				simulado						:	false,
				ingresado						:	false,
				idBaseTasaDefecto       		:   2,
				cuentaInversion					: 	true,
				idAutorizadoDarOrdenesDefecto	:	undefined,
				idMecanismoDeCaptacionDefecto	:	undefined,
				diasFechaVencimiento			:	bcsConfiguracionOrdenes.CONFIGURACION.PlazoMaximoReporteRV,
				porcentajeDescuentoInstrumento	: 	undefined,//rrf,
				tasaReportanteV					: 	0,
				formaCustodiaActual				:	'CUS',
				idUso							:	'REPORTES',
				
				configuracionMecanismoCaptacion : {
					filtros: {
						CodigoTipoIndicador : { "field"   : "CodigoTipoIndicador","operator": "eq", "value"   :  "ORDMODALIDAD" },
						IdEntidad : {"field":"IdEntidad","operator":"eq","value": -1 },
					},
				}
								
		};
		//Obtenemos valor de tasa reportante de referencia
		bcsConfiguracionOrdenes.CONFIGURACION.promise.then(function(result){
			$scope.formData.tasaReportanteV=bcsConfiguracionOrdenes.CONFIGURACION.TasaReferenciaReportante;
		    $scope.formData.diasFechaVencimiento = bcsConfiguracionOrdenes.CONFIGURACION.PlazoMaximoReporteRV;
		    $scope.formData.PorcentajeCastigoInstrumentoOrden = bcsConfiguracionOrdenes.CONFIGURACION.PorcentajeCastigoInstrumentoOrden
		});
		
		
	};
	
	$scope.filtros = {};
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.idBolsa=$scope.SesionBolsaDefectoSistema;
	$scope.especificaBolsa='S';
			
	
	var configuracionColumnas = function() {
		return { 
			
			Secuencia					:{field: "IdOrdenAsociada",				title: $translate.instant('UI_SECUENCIA'),		width: "100px", align: "left"},
			NombreCliente	  			:{field: "NombreCompletoReportante",	title: $translate.instant('UI_CLIENTE'),		width: "100px",	align: "left"},
			DescripcionPortafolio	  	:{field: "CodigoPortafolioReportante",	title: $translate.instant('UI_PORTAFOLIO'),		width: "100px",	align: "left"},
			Tasa	  					:{field: "TasaReportante",				title: $translate.instant('UI_TASA'),			width: "100px",	align: "left"},
			Cantidad					:{field: "CantidadAsociada",			title: $translate.instant('UI_CANTIDAD'),		width: "100px",	align: "left", template : " <label> #:  (util.bcs_formato(CantidadAsociada,'ORD','Cantidad',IdMonedaOrdenReportante, IdTipoInstrumento,'')).valor #</label>"},
			Monto						:{field: "MontoAsociado",				title: $translate.instant('UI_MONTO'), 			width: "100px", align: "left", template : " <label> #:  (util.bcs_formato(MontoAsociado,'ORD','Monto',IdMonedaOrdenReportante, IdTipoInstrumento,'')).valor #</label>"},
			MontoOrdenReportante		:{field: "MontoOrdenReportante",		title: $translate.instant('UI_PRINCIPAL_CONTADO'), 					width: "100px", align: "left", template : " <label> #:  (util.bcs_formato(MontoOrdenReportante,'ORD','Monto',IdMonedaOrdenReportante, IdTipoInstrumento,'')).valor #</label>"},
			MontoLiquidacion			:{field: "MontoLiquidacion",			title: $translate.instant('UI_MONTO_LIQUIDACION_CONTADO'), 			width: "100px", align: "left", template : " <label> #:  (util.bcs_formato(MontoLiquidacion,'ORD','Monto',IdMonedaOrdenReportante, IdTipoInstrumento,'')).valor #</label>"},
			MontoOrdenPlazo		    	:{field: "MontoOrdenPlazo",				title: $translate.instant('UI_PRINCIPAL_PLAZO'), 					width: "100px", align: "left", template : " <label> #:  (util.bcs_formato(MontoOrdenPlazo,'ORD','Monto',IdMonedaOrdenReportante, IdTipoInstrumento,'')).valor #</label>"},
			MontoLiquidacionPlazo		:{field: "MontoLiquidacionPlazo",		title: $translate.instant('UI_MONTO_LIQUIDACION_PLAZO'), 			width: "100px", align: "left", template : " <label> #:  (util.bcs_formato(MontoLiquidacionPlazo,'ORD','Monto',IdMonedaOrdenReportante, IdTipoInstrumento,'')).valor #</label>"},
			IdMoneda	  				:{field: "IdMonedaOrdenReportante",		title: $translate.instant('UI_MONEDA'),			width: "100px",	align: "left"}
			
		}     
	};
	
	var getConfiguracionColumna = function(campo,extras) {
		if (!configuracionColumnas()[campo]) console.log("FALTA COLUMNA " + campo);
		return _.extend( {}, configuracionColumnas()[campo], extras);
	};
	
	var idOrdenReportado;
	if( $scope.parametros.orden)
		idOrdenReportado = $scope.parametros.orden[0].IdOrden || -1;
	else
		idOrdenReportado=-1;
	
	var filtroReportante = [{field:"IdOrdenReportado", operator: "eq", value: idOrdenReportado}]
	
	$scope.filtros.filtrosReportantes = {
			IdOrdenReportado : {field:"IdOrdenReportado", operator: "eq", value: -1}
	}
	
	var ds = bcsConfiguracionOrdenesV2.getDataSourceAsociaciones();
	ds.definition.filter = filtroReportante
	
	var conf = {
			options : {
				userSettings:true,
				sortable: true,
	      		reorderable: true,
	      		filterable: true,
				groupable: false,
				pageable: true,
				selectable: "row",
				reorderable: true,
				allowCopy: true,
				resizable: true,
	      		autoBind: true,
	      		scrollable: true,
	      		height:200,
		
				dataSource: ds,
			
				columns: [
				           getConfiguracionColumna("Secuencia")
				          ,getConfiguracionColumna("NombreCliente")
				          ,getConfiguracionColumna("DescripcionPortafolio")
				          ,getConfiguracionColumna("Tasa")
				          ,getConfiguracionColumna("Cantidad")
				          ,getConfiguracionColumna("Monto")
				          ,getConfiguracionColumna("MontoOrdenReportante")
				          ,getConfiguracionColumna("MontoLiquidacion")
				          ,getConfiguracionColumna("MontoOrdenPlazo")
				          ,getConfiguracionColumna("MontoLiquidacionPlazo")
				          ,getConfiguracionColumna("IdMoneda")
				],
				gridCreated: function(grid) {
					$scope.actualizaTasa(grid);
					
				},
			}
	};
	
	
	$scope.formData.confGridReportes = conf.options;
	$scope.filtros.filtrosTipoInstrumento = {
			Codigo				:{"field" : "IdClasificacionTipoInstrumento", "operator": "eq", "value":"RV"},
			IdTipoInstrumento	:{"field" : "IdTipoInstrumento", "operator": "eq", "value":"REP"}
			
	}
	
	$scope.oConfiguracionPortafolio= 
	{
		filtros: {
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  0 },
			IdEmpresa : {"field":"IdEmpresa","operator":"eq","value": $scope.SesionIdEmpresa}
			
		}
	}
	
	$scope.oConfiguracionPreferenciaPagoPortafolio=
	{
			filtros 			: {
				dMedioDePago : { "field"   : "IdMedioDePago",operator: "neq", "value"   :  "CIN" },
		        IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  "-1" },
		        IdMoneda : { "field"   : "IdMoneda","operator": "eq", "value"   :  $scope.SesionIdMonedaSistema },
		        IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   : $scope.formData.TipoInstrumento ? $scope.formData.TipoInstrumento.IdTipoOperacion : $scope.parametros.orden[0].IdTipoInstrumento }
			},
	};
	
	$scope.filtros.filtrosFormaCustodia = {
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
	};
	
	
	$scope.filtros.filtrosBolsa = {
			Codigo: {"field" : "Codigo", "operator": "eq", "value":$scope.SesionBolsaDefectoSistema}
	};
	
	
	$scope.filtros.filtrosMoneda = {
			IdTipoInstrumento: {"field" : "IdTipoInstrumento", "operator": "eq", "value":''},
			IdMoneda		 : {"field" : "IdMoneda","operator":"eq","value":""}
					
	};	
	
	$scope.filtros.autorizadoDarOrdenes = {
			IdPortafolio:{"field":"IdPortafolio","operator":"eq","value":-1},
			IdEstado:{"field":"IdEstado","operator":"eq","value":'ACT'}
	};
		
	$scope.filtros.mecanismoDeCaptacion = {
			IdPortafolio:{"field":"CodigoTipoIndicador","operator":"eq","value":"ORDMODALIDAD"}
	};
	
	$scope.filtros.configuracionCuentaCustodio = {
	
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   : "" },
			IdEstadoAutorizacion : { "field"   : "IdEstadoAutorizacion","operator": "eq", "value"   : "AUT" },
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
			IdModeloCustodia : { "field"   : "IdModeloCustodia","operator": "eq", "value"   : "LOCAL" }
	
	};
	
	$scope.$watch('formData.idTipoInstrumento',function(newval,oldval) {
		if ($scope.formData.instrumento) {
			//$scope.formData.instrumento = undefined;
			$scope.formData.setInstrumento = "-1";
		}
		bcsComunes.ventanaAutoHeight(idVentana);
	});
	
	$scope.$watch('formData.TipoInstrumento',function(newval,oldval) {
		$scope.ActualizarPlazoMaximo();			
	});
	
	
	$scope.$watch('formData.formaCustodiaActual',function(newval,oldval) {
		if ($scope.formData.formaCustodiaActual !== undefined && $scope.formData.formaCustodiaActual === 'CUS') {
			$scope.formData.validaFlujoCustodia = 'S';			
		} else {
			$scope.formData.validaFlujoCustodia = 'N';	
		}
	});
	
	$scope.$watchGroup(['formData.instrumento'],function(newval,oldval) {
		
		if ($scope.formData.instrumento) {
			/*if ($scope.formData.instrumento.IdMoneda === $scope.formData.IdMoneda){
				$scope.formData.precio = Number($scope.formData.instrumento.Precio);//PrecioCierre);
			}
			else {
				$scope.formData.precio = Number($scope.formData.precioTc?$scope.formData.precioTc:0);
			}*/
			$scope.formData.mercado =Number($scope.formData.instrumento.Precio);//Cierre);
			if($scope.formData.instrumento.IdMoneda){
				//$scope.formData.IdMoneda=$scope.formData.instrumento.IdMoneda;
				$scope.formData.IdMonedaFiltro = $scope.formData.instrumento.IdMoneda;
				$scope.filtros.filtrosMoneda.IdMoneda.value=$scope.formData.instrumento.IdMoneda;
				
			}
								
		}
		else{
			$scope.formData.precio = undefined;
			$scope.formData.mercado = undefined;
			$scope.filtros.filtrosMoneda.IdMoneda.value=undefined;
			
			}
	});
	
	$scope.$watchGroup(['formData.idTipoInstrumento'],function(newval,oldval) {
		if(newval===oldval)
			return;
		 if($scope.formData.instrumento)
			$scope.formData.IdMoneda=$scope.formData.instrumento.IdMoneda;
	
	});
	
	$scope.$watchGroup(['formData.montoContado','formData.cantidad','formData.precio','formData.monedaSeleccionada','formData.factorConversion','formData.precioRf'],function(newval,oldval) {
		if(newval && newval != oldval){
			if ($scope.formData.TipoInstrumento){// && $scope.formData.TipoInstrumento.IdTipoInstrumento !== 'RRF'){
				if(!$scope.formData.modoMonto){
					
					if ($scope.formData.cantidad && $scope.formData.precio && $scope.formData.monedaSeleccionada){// && $scope.formData.factorConversion) {
						var precio = 0;
						if ($scope.formData.TipoInstrumento.IdClasificacionTipoInstrumento === 'RF') precio= $scope.formData.precioRf / 100;
						else precio = $scope.formData.precio;
						y = new Big(precio);//*$scope.formData.factorConversion);
						$scope.formData.montoContado = y.round(2) * $scope.formData.cantidad;
					}
				}
				else{
					if ($scope.formData.montoContado && $scope.formData.precio && $scope.formData.monedaSeleccionada){// && $scope.formData.factorConversion) {
						y = new Big($scope.formData.precio);//*$scope.formData.factorConversion);
						$scope.formData.cantidad = ($scope.formData.montoContado/(y.round(2)));
					}
				}
			}
		}
	});/*
	$scope.$watchGroup(['formData.montoContado','formData.cantidad','formData.precio','formData.monedaSeleccionada','formData.factorConversion','formData.precioRf'],function(newval,oldval) {
		if(newval && newval != oldval){
			if ($scope.formData.TipoInstrumento){// && $scope.formData.TipoInstrumento.IdTipoInstrumento !== 'RRF'){
				if(!$scope.formData.modoMonto){
				/*case "R":  //Redondear
			          formatoFinal.valorNumerico = Number(new Big(valor).toFixed(cantidadDecimales));
			          formatoFinal.valor = kendo.toString(formatoFinal.valorNumerico, "n"+cantidadDecimales);
			          formatoFinal.cantidadDecimales = cantidadDecimales;
			          formatoFinal.trucaRedondea = trucarRedondear;
			          formatoFinal.excelFormat = "#,##0".concat(cantidadDecimales > 0? ".".concat("0".repeat(cantidadDecimales)):"");
			            break;
					* /
					
					if ($scope.formData.cantidad && $scope.formData.precio && $scope.formData.monedaSeleccionada && $scope.formData.factorConversion) {
						var precio = 0;
						if ($scope.formData.TipoInstrumento.IdTipoInstrumento === 'RRF') precio= $scope.formData.precioRf / 100;
						else precio = $scope.formData.precio;
						var y = new Big(precio*$scope.formData.factorConversion);
						
						//if ($scope.formData.factorConversion < 0.5){
						if (y < 0.5){
							y = y.toFixed(10);
							s = precio*$scope.formData.factorConversion;
							//p= Number(util.truncarNumero(y,3));// - 0.0005;
							y = Number(util.truncarNumero(y,3));// - 0.0005;
							//y = y.toFixed(3);
						}
						else {
							y = y.toFixed(2);
						}
						$scope.formData.montoContado = (y * $scope.formData.cantidad);
					}
				}
				else{
					if ($scope.formData.montoContado && $scope.formData.precio && $scope.formData.monedaSeleccionada && $scope.formData.factorConversion) {
						y = new Big($scope.formData.precio*$scope.formData.factorConversion);
						$scope.formData.cantidad = ($scope.formData.montoContado/(y));
					}
				}
			}
		}
	});*/
	/*
	$scope.$watchGroup(['formData.montoContado','formData.cantidad','formData.precioTc','formData.monedaSeleccionada','formData.factorConversion','formData.precioRf'],function(newval,oldval) {
		if(newval && newval != oldval){
			if ($scope.formData.TipoInstrumento && $scope.formData.instrumento){// && $scope.formData.TipoInstrumento.IdTipoInstrumento !== 'RRF'){
				if(!$scope.formData.modoMonto){
					
					if ($scope.formData.cantidad && $scope.formData.precio && $scope.formData.monedaSeleccionada && $scope.formData.factorConversion) {
						var precio = 0;
						if ($scope.formData.TipoInstrumento.IdTipoInstrumento === 'RRF') precio= $scope.formData.precioRf / 100;
						else precio = $scope.formData.precioTc;
						//y = new Big(precio);//*$scope.formData.factorConversion);
						$scope.formData.montoContado = (precio.toFixed(2) * $scope.formData.cantidad).toFixed(2);//y.round(2) * $scope.formData.cantidad;
					}
				}
				else{
					if ($scope.formData.montoContado && $scope.formData.precio && $scope.formData.monedaSeleccionada && $scope.formData.factorConversion) {
						y = new Big($scope.formData.precioTc);//*$scope.formData.factorConversion);
						$scope.formData.cantidad = ($scope.formData.montoContado/ formData.precioTc.toFixed(2)).toFixed(2);//(y.round(2)));
					}
				}
			}
		}
	});
	*/
	var calcularParidad = function(IdMonedaOrigen,IdMonedaDestino,IdUso){
		var datos = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdTrabajo : 0,
			p_CalculosEntrada : [{
				IdTrabajo : 0,
				IdInterno : 0,
				Fecha : $scope.SesionFechaSistema,
				IdMonedaOrigen : IdMonedaOrigen,
				IdMonedaDestino : IdMonedaDestino,
				ValorCambioLocalModificado : 0,
				MontoOrigen : 1,
				IdUso : IdUso
			}]
		}
		var promise = bcsServiciosPlataformaMercado.CalcularParidadYTipoCambio(datos);
		promise.then(function(result){
			if (result.p_out_CalculosSalida[0].Resultado === 'RECHAZO') {
				$scope.formData.paridadReferencia = $translate.instant("UI_NO_SE_ENCONTRO_PARIDAD") + ":" + IdMonedaOrigen + " - " + IdMonedaDestino;
				$scope.formData.factorConversionGarantia=undefined;
			}
			else{
				$scope.formData.paridadReferencia = IdMonedaOrigen + " - " + IdMonedaDestino + ":" + result.p_out_CalculosSalida[0].FactorOrigenDestino;
				$scope.formData.factorConversionGarantia=result.p_out_CalculosSalida[0].MontoDestino;
				console.log("Factor Conversion:"+$scope.formData.factorConversionGarantia)
			}
				
			});

	};
	//watch paridades
	$scope.$watchGroup(["formData.instrumento","formData.monedaSeleccionada.IdMonedaLiquidacion"],function(newval,oldval){

		if (!$scope.formData.instrumento || !$scope.formData.instrumento.IdMoneda || !$scope.formData.IdMoneda) 
			return;
		var idMonedaOrigen = $scope.formData.instrumento.IdMoneda;
		var idMonedaDestino = $scope.formData.IdMoneda;
		var datos = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdTrabajo : 0,
			p_CalculosEntrada : [{
				IdTrabajo : 0,
				IdInterno : 0,
				Fecha : $scope.SesionFechaSistema,
				IdMonedaOrigen : idMonedaOrigen,
				IdMonedaDestino : idMonedaDestino,
				ValorCambioLocalModificado : 0,
				MontoOrigen : 1,
				IdUso : 'REPORTES'
			}]
	};
	
	
		var promise = bcsServiciosPlataformaMercado.CalcularParidadYTipoCambio(datos);
		promise.then(function(result){
			if (result.p_out_CalculosSalida[0].Resultado === 'RECHAZO') {
				$scope.formData.paridadReferencia = $translate.instant("UI_NO_SE_ENCONTRO_PARIDAD") + ":" + idMonedaOrigen + " - " + idMonedaDestino;
				$scope.formData.factorConversion=undefined;
			}
			else
				$scope.formData.paridadReferencia = idMonedaOrigen + " - " + idMonedaDestino + ":" + result.p_out_CalculosSalida[0].FactorOrigenDestino;
				$scope.formData.factorConversion=result.p_out_CalculosSalida[0].MontoDestino;
				console.log("Factor Conversion:"+$scope.formData.factorConversion)
				
			});
			
		
	});
	$scope.$watchGroup(['formData.tasaReportadoV','formData.tasaReportanteV'],function(newval,oldval) {
		if(!newval && newval == oldval){
			return;
		}
		if($scope.formData.tasaReportadoV && $scope.formData.tasaReportanteV){
			$scope.formData.tasaProm=(Number($scope.formData.tasaReportadoV)+Number($scope.formData.tasaReportanteV))/2;
		}
		else
		{
			$scope.formData.tasaEditar=undefined;
			
		}
	
		
	});
	/*$scope.$watchGroup(['formEditar.tasaReportanteV','formEditar.tasaReportadoV'],function(newval,oldval) {
		if(!newval || newval == oldval){
			return;
		}
		if($scope.formEditar.tasaReportanteV && $scope.formEditar.tasaReportadoV){
			$scope.formEditar.tasaPromedioR=($scope.formEditar.tasaReportanteV+$scope.formEditar.tasaReportadoV)/2;
		}
		else
		{
			$scope.formEditar.tasaPromedioR=undefined;
			
		}
			
	});*/
	
	
	$scope.$watchGroup(['formData.mercado','formData.factorConversion'],function(newval,oldval) {
		
		if($scope.modo=='INGRESAR'){
			console.log("Mercado:"+$scope.formData.mercado);
			console.log("FactorConversion:"+$scope.formData.factorConversion);
			if($scope.formData.mercado && $scope.formData.factorConversion){
				/*var pre = new Big($scope.formData.mercado*$scope.formData.factorConversion);
				pre = pre.toFixed(10);
				$scope.formData.precioTc=Number(util.truncarNumero(pre,3));*/
				$scope.formData.precioTc=$scope.formData.instrumento.PrecioMercado*$scope.formData.factorConversion;
				$scope.formData.precioTc=$scope.formData.precioTc;
				$scope.formData.tipoCambioOrigen=1;
				$scope.formData.tipoCambioLiquidacion=$scope.formData.factorConversion;
				
				if ($scope.formData.instrumento.IdMoneda === $scope.formData.IdMoneda){
					$scope.formData.precio = Number($scope.formData.instrumento.PrecioMercado);//PrecioCierre);
				}
				else {
					$scope.formData.precio = Number($scope.formData.precioTc?$scope.formData.precioTc:0);
				}
				
			}
			else{
				$scope.formData.precioTc=undefined;
				$scope.formData.tipoCambioOrigen=undefined;
				$scope.formData.tipoCambioLiquidacion=undefined;
				$scope.formData.montoContado=undefined;
				
				
			}
		}
	});
	
	$scope.$watch('formData.asociacionReportanteSeleccionado',function(newval,oldval) {
		
		if(newval && newval.IdOrdenAsociada && $scope.reportantes[newval.IdOrdenAsociada]){
			$scope.reportantes[newval.IdOrdenAsociada].visible = true;
			
		}
		if(oldval && oldval.IdOrdenAsociada && $scope.reportantes[oldval.IdOrdenAsociada]){
			$scope.reportantes[oldval.IdOrdenAsociada].visible = false;
			
		}
		
	});
	
	$scope.$watchGroup(['formData.instrumento'],function(newval,oldval){
		
		if(!newval[0] || !newval[0].IdInstrumento){
			return;
		}
		else{
			
			if($scope.formData.TipoInstrumento.IdClasificacionTipoInstrumento==='RF'){
				if(oldval[0] && oldval[0].IdInstrumento && oldval[0].IdInstrumento==newval[0].IdInstrumento)
					return;
				$scope.formData.porcentajeDescuentoInstrumento=$scope.formData.instrumento.PorcentajeCastigo === 0 ? $scope.formData.PorcentajeCastigoInstrumentoOrden:$scope.formData.instrumento.PorcentajeCastigo;
				$scope.formData.precioConDcto = 100 -$scope.formData.porcentajeDescuentoInstrumento;
				$scope.formData.precioRf= $scope.formData.precioConDcto;
				//$scope.valorizarMonto();
			}else{
				$scope.formData.porcentajeDescuentoInstrumento = 100 - $scope.formData.precioRf;
			}
		}
	});
	
	$scope.$watch('formData.precioRf',function(newval,oldval) {
		
		if($scope.formData.precioRf > 100){
			$scope.formData.precioRf = $scope.formData.precioConDcto;
		}else{
			$scope.formData.porcentajeDescuentoInstrumento = 100 - $scope.formData.precioRf;
		}
		
	});
	/*
	$scope.$watchGroup(['formData.cantidad'],function(newval,oldval){
		if(!newval[0]){
			return;
		}
		if($scope.formData.TipoInstrumento.IdTipoInstrumento==='RRF'){
			$scope.valorizarMonto();
			
		}
			
	});*/
	
	
	
	$scope.valorizarMonto = function(){	 
   		
		if(!$scope.formData.instrumento || !$scope.formData.instrumento.IdInstrumento || !$scope.formData.cantidad ||$scope.formData.cantidad<=0 )
			return;
		$scope.formData.corteMinimo = ($scope.formData.instrumento.CorteMinimo===0)?1:$scope.formData.instrumento.CorteMinimo;
		if(!$scope.formData.corteMinimo)
   			return;
   		$scope.formData.valorNominal = $scope.formData.cantidad;//*$scope.formData.corteMinimo;
   		bcsComunes.ventanaOverlay($scope.winID,true);
   		/*
		var datos = {
			p_IdEmpresa 			: $scope.$root.SesionIdEmpresa,
			p_IdUsuario 			: $scope.$root.SesionIdUsuario,
			p_IdModulo 				: 'CUS',
			p_IdPais 				: $scope.$root.SesionIdPaisSistema,
			p_OrigenProcesamiento 	: 'UIINGRESOORDENRF',
			p_InstrumentosPQ		: [],
			p_InstrumentosWS	: [{
				IdValorizacion		: 1,
				FechaValorizacion	: $scope.formData.fechaInicial,
				IdInstrumento		: $scope.formData.instrumento.IdInstrumento,
				Nemotecnico			: $scope.formData.instrumento.Nemotecnico,
				IdMonedaInstrumento	: $scope.formData.instrumento.IdMoneda,
				Cantidad			: $scope.formData.valorNominal,//ordenRF.datos.valorNominal||null,
				Precio				: $scope.formData.precioRf,//ordenRF.datos.tir||null,
				Tasa				: null,
				Monto		 		: null,
			}],
		};
		*/
   		var datos = {
   			p_IdEmpresa 			: $scope.$root.SesionIdEmpresa,
			p_IdUsuario 			: $scope.$root.SesionIdUsuario,
			p_IdModulo 				: 'CUS',
			p_IdPais 				: $scope.$root.SesionIdPaisSistema,
			p_OrigenProcesamiento 	: 'UIINGRESOREPORTERF',
			p_TicketProcesamiento	: 5602,
			p_Instrumentos			: [ //Contado
			{
				IdValorizacion					: 1,
				FechaOperacion					: $scope.SesionFechaSistema,
				FechaVencimientoOperacion 		: util.dateSPdate($scope.formData.fechaVencimientoVigencia),
				PlazoEnDiasOperacion 			: $scope.formData.diasFechaVencimiento, //Solo para operaciones a plazo
				FechaValorizacion				: util.dateSPdate($scope.formData.fechaInicial), 		
				IdClasificacionTipoInstrumento	: $scope.formData.instrumento.IdClasificacionTipoInstrumento, 
				IdTipoInstrumento			: $scope.formData.instrumento.IdTipoInstrumento, 
				IdInstrumento				: $scope.formData.instrumento.IdInstrumento, 
				Nemotecnico					: $scope.formData.instrumento.Nemotecnico, 
				FechaVencimiento			: null,
				FechaEmision				: null,
				IdMonedaInstrumento			: $scope.formData.instrumento.IdMoneda,
				IdTipoReajusteInstrumento	: $scope.formData.instrumento.IdTipoReajuste,
				Cantidad					: ($scope.formData.valorNominal!=null&&$scope.formData.valorNominal!=undefined)?$scope.formData.valorNominal:null, 
				ValorRescate				: null,
				PrecioCompra				: ($scope.formData.precioRf!=null&&$scope.formData.precioRf!=undefined)?$scope.formData.precioRf:null,
				TasaCompra					: null,
				MontoCompra					: null,//en moneda del instrumento		
				PrecioSubyacente			: null, 
				TasaSubyacente				: null, 
				PrecioGarantia				: null, 
				TasaGarantia				: null, 
				TasaTIF						: null,		
				TieneCorteDeCupon			: null,
				IdCliente					: null, 
				IdPortafolio				: null,
				TablaOrigen					: null,	
				IdUso						: null, 
				TipoCartera					: null, 
				IdTipoCriterioTasa			: null,
			}],
		}
		console.info("Datos valorizar",JSON.stringify(datos));
		bcsServicioPlataforma.ValorizarInstrumento(datos,function(response){
			bcsComunes.ventanaOverlay($scope.winID,false);
			var itemval = response.P_OUT_INSTRUMENTOSVALORIZADOSWS[0];
			$scope.formData.montoContado= itemval.MontoTotalMonedaInstrumento;
					
   		},function(response){
   			bcsComunes.ventanaOverlay($scope.winID,false);
   			
   		});
   	}
		
	$scope.ingresarReportante = function(){
		var resp = validaDisponibleReportado();
		if (resp){
			bcsComunes.ventanaOverlay($scope.winID,true);
			//actualizamos con tasa por si se modifico manualmente
			//$scope.parametros.orden[0].TasaReportanteReferencia=$scope.formEditar.tasaReportanteV;
			$scope.parametros.orden[0].TasaReportadoReferencia=$scope.formEditar.tasaReportadoV;
			
			var id = bcsComunes.ventanaAbrir("IngresoReportantes",{ordenReportado: $scope.parametros.orden[0]},false); 
	        var promesa=bcsComunes.ventanaPromesa(id);                               
	        $.when(promesa).then(function(data){
	        	
	        	conf.options.dataSource.read();
	        	$scope.formData.reportantesSeleccionadas = undefined;
	        	bcsComunes.ventanaOverlay($scope.winID,false);
	        
	        });
		}
		else {
			bcsComunes.msg("error",$translate.instant('UI_ERROR'),$translate.instant("UI_ERROR_ASOCIAR_REPORTANTE"));
		}
	}
	
	$scope.buscarReportante = function(){
		var resp = validaDisponibleReportado();
		if (resp){
			bcsComunes.ventanaOverlay($scope.winID,true);
			
			//actualizamos con tasa por si se modifico manualmente
			//$scope.parametros.orden[0].TasaReportanteReferencia=$scope.formEditar.tasaReportanteV;
			
			$scope.parametros.orden[0].TasaReportadoReferencia=$scope.formEditar.tasaReportadoV;
			var id = bcsComunes.ventanaAbrir("BuscarReportantes",{reportado: $scope.parametros.orden[0]},false);
			var promesa=bcsComunes.ventanaPromesa(id);                               
	        $.when(promesa).then(function(data){  
	        	
	        	conf.options.dataSource.read();
	        	$scope.formData.reportantesSeleccionadas = undefined;
	        	bcsComunes.ventanaOverlay($scope.winID,false);
	      
	        });
		}
		else {
			bcsComunes.msg("error",$translate.instant('UI_ERROR'),$translate.instant("UI_ERROR_ASOCIAR_REPORTANTE"));
		}
	}
	var validaDisponibleReportado = function(){
		var data = $scope.formData.confGridReportes.dataSource.data();
		var totalReportado = 0;
		$.each(data,function(key,value) {
			totalReportado = totalReportado + value.MontoAsociado;
		});
		$scope.parametros.orden[0].MontoDisponibleAsociar = $scope.formEditar.montoContado - totalReportado;
		if (totalReportado >= $scope.formEditar.montoContado){
			return false;
		}
		else return true;
	};
		
	$scope.retirarReportante = function(reportante){
		if(!reportante) return;
		bcsComunes.ventanaOverlay($scope.winID,true);
		
		var datos = {
			p_IdEmpresa   : $scope.SesionIdEmpresa,
			p_IdUsuario   : $scope.SesionIdUsuario,
			p_IdModulo 	  : 'ORD',
			p_simularOperacion : 'N',
			p_UsaReglasNegocio : 'S',
			p_Asociaciones : [{IdInterno: reportante.IdOrdenAsociada, IdOrdenAsociada :  reportante.IdOrdenAsociada}]
		}
			
		bcsServicioOrden.AnularAsociaciones(datos).$promise.then(function(respuesta){
			var idv;
			if(respuesta.P_OUT_ERRORESCONTADOR == 0){
				idv=bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ANULAR_ASOCIAR_REPORTANTE"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			}else{
				idv=bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ANULAR_ASOCIAR_REPORTANTE"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			}
			
	     	var promesa=bcsComunes.ventanaPromesa(idv); 
            $.when(promesa).then(function(){
            	conf.options.dataSource.read();
            	$scope.formData.reportantesSeleccionadas = undefined;
                bcsComunes.ventanaOverlay($scope.winID,false);
              
             });
		},function(respuesta){
  			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ANULAR_ASOCIAR_REPORTANTE"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
  			conf.options.dataSource.read();
  			bcsComunes.ventanaOverlay($scope.winID,false);
  			$scope.formData.reportantesSeleccionadas = undefined;
		});
		
	}
	
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
	
	$scope.formData.ingresarOrdenReportado = function(a) {
		
		if(!plazoMaximoValido){
			bcsComunes.msg("error","Error", mensajeErrorPlazo );
			return;
		}
		
		bcsComunes.ventanaOverlay($scope.winID,true);
		
		if($scope.modo==='INGRESAR'){
			
			var cobrosModificadosContado = [];
			
			$.each($scope.formData.cobrosModificadosVentaContado,function(key,value) {
				cobrosModificadosContado.push({IdInterno:0, IdCobro: key, ValorCobroAplicado: value});
			});
						
			var cobrosModificados = cobrosModificadosContado;
			
			var garantias = [];
			
			var dataGrillaGarantias = $scope.formData.gridGarantiasCartera.dataSource.data();
			
			for (var i = 0; i < dataGrillaGarantias.length; i++) {
				if (dataGrillaGarantias[i].CantidadSimulacionGarantiaCustodia > 0) {
					
					var gar = {
						IdInstrumento 				: dataGrillaGarantias[i].IdInstrumento,
						IdInstrumentoGarantizado 	: $scope.formData.TipoInstrumento.IdInstrumento,
						IdCliente    				: $scope.formData.clienteSeleccionado.IdCliente,
						IdPortafolio 				: $scope.formData.clienteSeleccionado.IdPortafolio,
						Cantidad					: dataGrillaGarantias[i].CantidadSimulacionGarantiaCustodia,
						Monto						: dataGrillaGarantias[i].MontoSimulacionGarantiaCustodia,
						MontoCoberturaSimular		: dataGrillaGarantias[i].MontoCoberturaSimular, //Guz
						Precio						: dataGrillaGarantias[i].PrecioGarantiaCustodia,
						IdBolsa						: $scope.formData.idBolsa,
						IdMoneda					: $scope.formData.monedaSeleccionada.IdMonedaLiquidacion,//'',
						IdTipoGarantia				: 'CUS'
						
					}
					garantias.push(gar);
				}
			}
			
			var dataGrillaGarantiasCtaInv = $scope.formData.gridGarantiasCtaInv.dataSource.data();
			
			for (var i = 0; i < dataGrillaGarantiasCtaInv.length; i++) {
				if (dataGrillaGarantiasCtaInv[i].MontoSimulacionGarantiaCtaInv > 0) {
					var gar = {
						IdInstrumento 				: 0,
						IdInstrumentoGarantizado 	: $scope.formData.TipoInstrumento.IdInstrumento,
						IdCliente    				: $scope.formData.clienteSeleccionado.IdCliente,
						IdPortafolio 				: $scope.formData.clienteSeleccionado.IdPortafolio,
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
			
			var datosIngresoOrden = {
					p_IdEmpresa 				:	$scope.SesionIdEmpresa, //saca de variables de entorno
					p_IdUsuario 				:	$scope.SesionIdUsuario, //saca de variables de entorno
					p_IdModulo  				:	'ORD',
					p_simularOperacion  		:	'N',
					p_UsaReglasNegocio  		:	'S',
					p_Garantias					:	garantias,
					p_CobrosModificados 		: 	cobrosModificados,
					p_Ordenes 					:	[{
					
						IdInterno 						:	0,
					    FechaRecepcion 					:	'0001-01-01 00:00:00',
						FechaVigencia  					: 	($scope.formData.fechaVigencia)? util.dateSPdate($scope.formData.fechaVigencia) :  '0001-01-01',
						FechaEmision   					: 	'0001-01-01',
						FechaOperacion 					:	'0001-01-01', //se calcula en el server
						IdTipoOperacion 				:	$scope.formData.TipoInstrumento.IdTipoOperacion,//'COMRRV'
						IdCondicionDeLiquidacion 		:	$scope.formData.condicionLiquidacion.IdCondicionDeLiquidacion,//'T0',
						IdTipoVigencia 					: 	'SH',
						Glosa 							:	$scope.formData.glosa,
						Cantidad 						: 	$scope.formData.cantidad,
						MontoNeto 						:	$scope.formData.montoContado,//montoCalculado
						ValorRescate	 				: 	0,
						IdBoveda 						: 	$scope.formData.cuentaCustodioSeleccionada.IdBoveda || $scope.SesionValoresDefectoSistema.IdBovedaCuentaCustodia,
						IdFormaCustodia 				:	$scope.formData.formaCustodiaSeleccionada.IdFormaCustodiaOrden,//'CUS', 'NOM' -> no valida custodia,
						IdPorcentajeVisibilidad 		:	1,
						PrecioLimite 					:	$scope.formData.TipoInstrumento.IdClasificacionTipoInstrumento === 'RF'?$scope.formData.precioRf:$scope.formData.precio,
						PrecioMercado 					:	$scope.formData.mercado,
						EsFacturaInmediata 				:	'N',
						IdTipoPrecio 					:	'M',
						IdPortafolio 					:	$scope.formData.clienteSeleccionado.IdPortafolio,
						IdTipoLibroOrden 				:	'NORMAL',
						IdTipoCanalIngreso 				:	$scope.SesionTipoCanalIngreso,
						IdTipoReajuste 					:	'',
						IdEmisor						:	'0',
						IdBolsa 						:	$scope.formData.idBolsa,
						EspecificaBolsa 				:	$scope.especificaBolsa,
						IdMonedaLiquidacionOrden 		:	$scope.formData.IdMoneda,//formData.monedaSeleccionada.IdMoneda,
						TipoCambio 						:	null,//$scope.formData.tipoCambioLiquidacion,
						IdInstrumento 					:	$scope.formData.TipoInstrumento.IdInstrumento,
						IdInstrumentoSubyacente 		:	$scope.formData.instrumento.IdInstrumento,
						IdCondicionDeLiquidacionPlazo 	: 	'TP',
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
						IdPersonaAutorizadoDarOrdenes 	: 	($scope.formData.autorizadoDarOrdenes&&$scope.formData.autorizadoDarOrdenes.IdPersona)?$scope.formData.autorizadoDarOrdenes.IdPersona:null,
						IdTipoCanalDistribucion 		:	'MAN',
						TasaReporte						:	$scope.formData.tasaReportadoV,
						TasaReportanteReferencia		:	$scope.formData.tasaReportanteV,
						TasaPromedioReporte				:	$scope.formData.tasaProm,
						PlazoReporte					:	$scope.formData.diasFechaVencimiento,
						BaseTasa						:	$scope.formData.baseTasa.ValorBaseTasaCobro,
						PagoCobroCuentaInversion		:	$scope.formData.cuentaInversion ? 'S' : 'N',
						IdPreferenciaPagoPortafolio		:	$scope.formData.cuentaInversion ? null:($scope.formData.idPreferenciaPagoPortafolio ? $scope.formData.idPreferenciaPagoPortafolio:null),
						TipoInteres						:	'C',
						IdMecanismoCaptacion 			: 	$scope.formData.mecanismoDeCaptacion?$scope.formData.mecanismoDeCaptacion:'',
						MontoInteresContado				:	$scope.formResumen.MontoInteresContado||0,
						MontoInteresPlazo				:	$scope.formResumen.MontoInteresPlazo||0,
						IdCuentaCustodioPortafolio		:	$scope.formData.cuentaCustodioSeleccionada.IdCuentaCustodioPortafolio || null,
					}]
				
					
			};
					
			bcsComunes.ventanaOverlay($scope.winID,true);
			var promise = bcsServicioOrden.CrearOrdenes(datosIngresoOrden).$promise;
			promise.then(function(respuesta){
				bcsComunes.ventanaOverlay($scope.winID,false);
				if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_RESULTADO[0].IdOrden > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0) 
				{
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_ORDEN_INGRESADA")+": " + respuesta.P_OUT_RESULTADO[0].IdOrden,
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);
					$scope.formData.ingresado=true;
					$scope.formResumen.idOrden=respuesta.P_OUT_RESULTADO[0].IdOrden;
					
				}
				else
				{
					
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
		}else if($scope.modo==='EDITAR'){
			
			var cobroReportadoContado = [];
						
			$.each($scope.formData.cobrosModificadosVentaContado,function(key,value) {
				cobroReportadoContado.push({IdOrden:$scope.formEditar.idOrden, IdCobro: key, ValorCobroAplicado: value});
			});
						
			var cobrosReportado = cobroReportadoContado;
			
			var ordenReportado = [{
					IdInterno : 0,
					IdOrden	: $scope.formEditar.idOrden,
					ModificaCantidad	:	'N',
					Cantidad	:	0,
					ModificaCobros	:	'S',
					IdAsociacion : 0,
					ModificaTasaPromedioReporte	:	'S',
					TasaPromedioReporte	:	$scope.formEditar.tasaPromedioR,
					
					IdInstrumentoSubyacente  : $scope.formEditar.idInstrumentoSubyacente,
					IdEmisor				 : $scope.formEditar.idEmisor,
					IdMonedaLiquidacionOrden : $scope.formEditar.monedaLiquidacion,
					MontoInteresContado				:	$scope.formResumen.MontoInteresContado||0,
					MontoInteresPlazo				:	$scope.formResumen.MontoInteresPlazo||0,
				}]
			
			var cobrosReportantes = [];
			var ordenReportantes = [];
			
			$.each($scope.reportantes,function(key,value) {
				var cobroContado = [];
				
				$.each(value.cobrosReportanteModificadosContado,function(key1,value1) {
					cobroContado.push({IdOrden:value.IdOrden, IdCobro: key1, ValorCobroAplicado: value1});
				});
								
				cobrosReportantes = cobrosReportantes.concat(cobroContado);
				
				var orden = {
					IdInterno : key+1,
					IdOrden	: value.IdOrden,
					ModificaCantidad	:	'N',
					Cantidad	:	0,
					ModificaCobros	:	'S',
					IdAsociacion : key,
					ModificaTasaPromedioReporte	:	'S',
					TasaPromedioReporte	:	$scope.formEditar.tasaPromedioR,	
					
					IdInstrumentoSubyacente  : $scope.formEditar.idInstrumentoSubyacente,
					IdEmisor				 : $scope.formEditar.idEmisor,
					IdMonedaLiquidacionOrden : $scope.formEditar.monedaLiquidacion,
					MontoInteresContado				:	$scope.formResumen.MontoInteresContado||0,
					MontoInteresPlazo				:	$scope.formResumen.MontoInteresPlazo||0,
				}
				
				ordenReportantes.push(orden);
				
			});
			
			var cobros = cobrosReportado.concat(cobrosReportantes);
			
			var ordenes = ordenReportado.concat(ordenReportantes);
			
			var datosModificacionOrden = {
					p_IdEmpresa 				:	$scope.SesionIdEmpresa, //saca de variables de entorno
					p_IdUsuario 				:	$scope.SesionIdUsuario, //saca de variables de entorno
					p_IdModulo  				:	'ORD',
					p_simularOperacion  		:	'N',
					p_UsaReglasNegocio  		:	'S',
					p_CobrosModificados			: 	cobros,
					p_Ordenes					:	ordenes
			};
			
			var promise = bcsServicioOrden.ModificarOrdenes(datosModificacionOrden).$promise;
			promise.then(function(respuesta){
				bcsComunes.ventanaOverlay($scope.winID,false);
				if (respuesta.P_OUT_ERRORESCONTADOR === 0) 
				{				
					var id=bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ORDEN_MODIFICADA"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
					var promesa=bcsComunes.ventanaPromesa(id);                               
				     $.when(promesa).then(function(data){
				    	 $timeout(function() {
				    		 bcsComunes.ventanaCerrar($scope.winID);
				    	 },20);
				     });
					
					
			    	 
				}
				else
				{
					bcsComunes.ventanaOverlay($scope.winID,false);
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_NO_SE_PUDO_MODIFICAR_ORDEN"),
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);	
				}
			},
			function(respuesta){
	   			bcsComunes.ventanaOverlay($scope.winID,false);
				bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_EXISTEN_PROBLEMAS_PARA_MODIFICAR_ORDEN"),
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);
	
			});
		}
		
		
								
	}
	
	$scope.formData.actualizarDisponible = function() {
		var dataGridGta = $scope.formData.gridGarantiasCartera.dataSource.data();
		
		var CantidadSimulacionGarantiaPorNemo = {};
		var CantidadSimulacionSimultaneaPorNemo = {};

		//calcular lo asignado
		for (var i=0; i < dataGridGta.length; i++){
			CantidadSimulacionGarantiaPorNemo[dataGridGta[i].Instrumento] = dataGridGta[i].CantidadSimulacionGarantiaCustodia;
		}
		
		CantidadSimulacionSimultaneaPorNemo[$scope.formData.instrumento.Nemotecnico] = $scope.formData.cantidad;
		
		
		//calcular lo disponible
		for (var i=0; i < dataGridGta.length; i++){
			dataGridGta[i].CantidadDisponibleGarantiaCustodia = dataGridGta[i].CantidadOriginalGarantiaCustodia - (CantidadSimulacionSimultaneaPorNemo[dataGridGta[i].Instrumento] || 0) - dataGridGta[i].CantidadSimulacionGarantiaCustodia;
			dataGridGta[i].CantidadDisponibleGarantiaCustodia = (dataGridGta[i].CantidadDisponibleGarantiaCustodia > 0)? dataGridGta[i].CantidadDisponibleGarantiaCustodia :0;
		}
		
		$scope.formData.gridGarantiasCartera.dataSource.sync();
	};
	
	var confCustodiaGarantizable = bcsConfiguracionOrdenesV2.getConfiguracionGridCustodiaGarantizable();
		
	
	confCustodiaGarantizable.save = function(e) {
		
		var dataGridGta = $scope.formData.gridGarantiasCartera.dataSource.data();
		
		
		e.model.PorcentajeCoberturaEntidad =Number(0);
		e.model.MontoCoberturaEntidad=Number(0);
		e.model.PorcentajeCoberturaCorredora=Number(0);
		e.model.MontoCoberturaCorredora=Number(0);
		e.model.MontoCoberturaSimular=Number(0);
		
		//e.model.MontoCoberturaSimular=Number(e.values.MontoCoberturaSimular);
		/*
		//Exigibilidad
		var operaciones = [];
		for (var i = 0;i< e.sender.dataSource.data().length;i++)
		{
			
			if (e.sender.dataSource.data()[i].MontoSimulacionGarantiaCustodia === 0) continue;
			operaciones.push({
				EsVentaCorta		: false,
				IdTipoInstrumento	: $scope.formData.TipoInstrumento.IdTipoInstrumento,
				IdBolsa				: $scope.formData.idBolsa,
				IdPortafolio		: $scope.formData.clienteSeleccionado.IdPortafolio,
				IdInstrumentoExige	: $scope.formData.TipoInstrumento.IdInstrumento,
				IdInstrumento		: e.sender.dataSource.data()[i].IdInstrumento,
				Cantidad			: 0,//$scope.formData.cantidad,
				Precio				: 0,//$scope.formData.precio,
				Monto 				: e.sender.dataSource.data()[i].MontoSimulacionGarantiaCustodia,            				
			});
		}
		//if (operaciones.length > 0) 
			$scope.formData.operacionesExigibilidad = operaciones;
		
		*/
		//update cobertura
		var operaciones = [];
		for (var i = 0;i< e.sender.dataSource.data().length;i++)
		{
			if (e.model.IdInstrumento === e.sender.dataSource.data()[i].IdInstrumento){
				if (e.values.CantidadSimulacionGarantiaCustodia >= 0 ){
					e.model.CantidadSimulacionGarantiaCustodia = Math.round(e.values.CantidadSimulacionGarantiaCustodia,0);
					e.model.MontoSimulacionGarantiaCustodia = e.model.PrecioGarantiaCustodia*e.model.CantidadSimulacionGarantiaCustodia;
				}
				if (e.values.MontoSimulacionGarantiaCustodia >= 0){	
					if (e.values.MontoSimulacionGarantiaCustodia > e.sender.dataSource.data()[i].CantidadDisponibleGarantiaCustodia){
						e.values.MontoSimulacionGarantiaCustodia = e.sender.dataSource.data()[i].CantidadDisponibleGarantiaCustodia;
					}	
					e.model.MontoSimulacionGarantiaCustodia = e.values.MontoSimulacionGarantiaCustodia;
					e.model.CantidadSimulacionGarantiaCustodia = Math.round(e.model.MontoSimulacionGarantiaCustodia/e.model.PrecioGarantiaCustodia,0);
				}
				if(e.values.MontoCoberturaSimular >= 0){
					
					e.model.MontoCoberturaSimular = e.values.MontoCoberturaSimular;
					e.model.MontoSimulacionGarantiaCustodia = e.values.MontoSimulacionGarantiaCustodia;
					e.model.CantidadSimulacionGarantiaCustodia = Math.round(e.model.MontoSimulacionGarantiaCustodia/e.model.PrecioGarantiaCustodia,0);

				}
				if (e.sender.dataSource.data()[i].MontoSimulacionGarantiaCustodia === 0) continue;
			
				operaciones.push({
					IdPortafolio		: $scope.formData.clienteSeleccionado.IdPortafolio,
					IdBolsa				: $scope.formData.idBolsa,
					IdInstrumentoExige	: $scope.formData.TipoInstrumento.IdInstrumento || 0,
					EsVentaCorta		: false,
					IdTipoInstrumentoExige	: $scope.formData.TipoInstrumento.IdTipoInstrumento,

					IdInstrumento		: e.sender.dataSource.data()[i].IdInstrumento,
					Cantidad			: 0,//$scope.formData.cantidad,
					Precio				: 0,//$scope.formData.precio,
					Monto 				: e.sender.dataSource.data()[i].MontoSimulacionGarantiaCustodia,
					MontoCoberturaSimular		: e.sender.dataSource.data()[i].MontoCoberturaSimular,
					MonedaDestino		: $scope.formData.monedaSeleccionada.IdMonedaLiquidacion,
					
					     				
				});
			}
		}
		//if (operaciones.length > 0) {
			//$scope.formData.operacionesCobertura = operaciones;
		//}
				
		/* CALCULO DE COBERTURAS DE OPERACIONES */	  			
		var datosCobertura = {
				p_EsVentaCorta 		: 'N',
				p_UsaReglasNegocio 	: 'S',
				p_IdBolsa 			: $scope.formData.idBolsa,
				p_IdEmpresa			: $scope.SesionIdEmpresa,
				p_IdInstrumento		: $scope.formData.TipoInstrumento.IdInstrumento,
				p_IdModulo			: 'ORD',
				p_IdPortafolio		: $scope.formData.clienteSeleccionado.IdPortafolio,
				p_IdTipoInstrumento	: $scope.formData.TipoInstrumento.IdTipoInstrumento,
				p_IdUsuario			: $scope.SesionIdUsuario,
				p_IdMonedaOrigen	: e.model.MontoCoberturaSimular===0?e.model.IdMonedaInstrumento:$scope.formData.monedaSeleccionada.IdMonedaLiquidacion,      
				p_IdMoneda 			: $scope.formData.monedaSeleccionada.IdMonedaLiquidacion,      
				p_Simulacion		: operaciones,						
		};
		//if(e.model.IdMonedaInstrumento !== $scope.formData.IdMoneda)
			calcularParidad(e.model.IdMonedaInstrumento,$scope.formData.IdMoneda,'REPORTESGARANTIA');//Obtengo Paridad por Instrumento a Garantizar		
			
		var p = bcsServicioOrden.CalcularCoberturaOrdenes(datosCobertura).$promise;
		p.then(function(result){

			$.each(result.p_out_TableCoberturaSimulacion,function(key,value) {
				
				var op = _.find(e.sender.dataSource.data(),function(operacion){
					return operacion.IdInstrumento == value.IdInstrumento
				});
				if (op)
				{
					
					op.PorcentajeCoberturaEntidad=Number(value.PorcentajeCoberturaEntidad);
					op.PorcentajeCoberturaCorredora=Number(value.PorcentajeCoberturaCorredora);
					op.MontoSimulacionGarantiaCustodia = Number(value.Monto);
					op.CantidadSimulacionGarantiaCustodia =op.MontoCoberturaSimular===0?Number(Math.round(op.MontoSimulacionGarantiaCustodia/op.PrecioGarantiaCustodia)):Number(Math.ceil(op.MontoSimulacionGarantiaCustodia/(op.PrecioGarantiaCustodia*$scope.formData.factorConversionGarantia)));

					//Se recalcula el monto con la cantidad aproximada (Esto es por que el calculo del servicio el precio instrumento y la cantidad
					// no son consideradas) para obtener el valor cobertura REAL
					if(e.model.IdMonedaInstrumento !== $scope.formData.IdMoneda){
						op.MontoSimulacionGarantiaCustodia = Number(op.CantidadSimulacionGarantiaCustodia*op.PrecioGarantiaCustodia*$scope.formData.factorConversionGarantia);
					}else{
						op.MontoSimulacionGarantiaCustodia = Number(op.CantidadSimulacionGarantiaCustodia*op.PrecioGarantiaCustodia);
					}
						

					op.MontoCoberturaEntidad=Number(op.MontoSimulacionGarantiaCustodia*(value.PorcentajeCoberturaEntidad/100));
					op.MontoCoberturaCorredora=Number(op.MontoSimulacionGarantiaCustodia*(value.PorcentajeCoberturaCorredora/100));
					if(op.MontoCoberturaSimular===0 ||  op.MontoCoberturaSimular===undefined){
							op.MontoCoberturaSimular = op.MontoCoberturaCorredora
					}
					
					
				};
				
				
			});
			var operaciones = [];
			$.each(e.sender.dataSource.data(),function(key,value) {
				if (value.CantidadSimulacionGarantiaCustodia > 0 ){
					operaciones.push({
						IdPortafolio		: $scope.formData.clienteSeleccionado.IdPortafolio,
						IdBolsa				: $scope.formData.idBolsa,
						IdInstrumentoExige	: $scope.formData.TipoInstrumento.IdInstrumento || 0,
						EsVentaCorta		: false,
						IdTipoInstrumentoExige	: $scope.formData.TipoInstrumento.IdTipoInstrumento,

						IdInstrumento		: value.IdInstrumento,
						Cantidad			: 0,//$scope.formData.cantidad,
						Precio				: 0,//$scope.formData.precio,
						Monto 				: value.MontoSimulacionGarantiaCustodia,
						MontoCoberturaSimular		: value.MontoCoberturaSimular,
						MonedaDestino		: $scope.formData.monedaSeleccionada.IdMonedaLiquidacion,
						SesionLocale		: $scope.SesionLocale
					});
				}
				
			});
			$scope.formData.operacionesCobertura = operaciones;
			/* FIN CALCULO DE COBERTURAS DE OPERACIONES */ 
			return true;
			
		},function(result){
		
		}).then(function(){
			e.sender.refresh();
			e.preventDefault();
			$scope.formData.actualizarDisponible();
			bcsComunes.ventanaOverlay($scope.winID,false);
		});
		
		e.preventDefault();
		$scope.formData.actualizarDisponible();
  	};
	
	
	$scope.formData.optionsGrillaGtiaCartera = confCustodiaGarantizable;
	
	var confCtaInvGarantizable = bcsConfiguracionOrdenesV2.getConfiguracionGridCtaInvGarantizable();
	
	$scope.formData.optionsGrillaGtiaCtaInv = confCtaInvGarantizable;
	
	$scope.formData.simularOperacion = function(){
		if(!plazoMaximoValido){
			bcsComunes.msg("error","Error", mensajeErrorPlazo );
			return;
		}
		
		bcsComunes.ventanaOverlay($scope.winID,true);
		
		if($scope.modo === 'INGRESAR'){
			var operaciones = [];
			operaciones.push({
				IdPortafolio		: $scope.formData.clienteSeleccionado.IdPortafolio,
				IdBolsa				: $scope.formData.idBolsa,
				IdInstrumentoExige	: $scope.formData.TipoInstrumento.IdInstrumento || 0,
				EsVentaCorta		: false,
				IdTipoInstrumentoExige	: $scope.formData.TipoInstrumento.IdTipoInstrumento,
				
				IdInstrumento		: 0,//$scope.formData.instrumento.IdInstrumento,
				Cantidad			: 0,//$scope.formData.cantidad,
				Precio				: 0,//$scope.formData.precio,
				Monto 				: 0,//$scope.formData.montoContado,
				SesionLocale		: $scope.SesionLocale
			});
			
			if (operaciones.length > 0) {
				$scope.formData.operacionesCobertura = operaciones;
			}
			
			var operaciones = [];
			
			operaciones.push({
				IdPortafolio		: $scope.formData.clienteSeleccionado.IdPortafolio,
				IdTipoOperacion		: $scope.formData.TipoInstrumento.IdTipoOperacion,
				EsVentaCorta		: false,
				EsODCuentaPropia	: false,
				IdInstrumento		: $scope.formData.TipoInstrumento.IdInstrumento,
				Monto 				: 0,
				Tasa				: 0,
				PlazoEnDias			: 0,
				IdBolsa				: $scope.formData.idBolsa,
				
			});
			
			if (operaciones.length > 0) {
				$scope.formData.operacionesLineaRiesgo = operaciones;
			}
			
			var op = {
					IdInstrumento  					: $scope.formData.TipoInstrumento.IdInstrumento,
					IdInstrumentoSubyacente			: $scope.formData.instrumento.IdInstrumento,
					DiasPlazo 						: $scope.formData.diasFechaVencimiento,
					CantidadOrden   				: $scope.formData.cantidad,
					PrecioOrden		   				: $scope.formData.TipoInstrumento.IdClasificacionTipoInstrumento === 'RF'?$scope.formData.precioRf:$scope.formData.precio,
					MontoOrden						: $scope.formData.montoContado,
					TasaReporte 					: $scope.formData.tasaProm,
					TasaReportado 					: $scope.formData.tasaReportadoV,
					IdMonedaLiquidacionOrden        : $scope.formData.IdMoneda,
					IdPortafolio 					: $scope.formData.clienteSeleccionado.IdPortafolio,
					IdTipoProducto 					: $scope.formData.TipoInstrumento.IdTipoProducto,
					IdTipoOperacion 				: $scope.formData.TipoInstrumento.IdTipoOperacion,
					IdCanalIngreso 					: $scope.SesionTipoCanalIngreso,
					IdBolsa 						: $scope.formData.idBolsa,
					PlazoReporte					: $scope.formData.diasFechaVencimiento,
					BaseTasa 						: $scope.formData.baseTasa.ValorBaseTasaCobro,
					IdCorredorCorresponsal			: null,
					FechaOperacion					: $scope.SesionFechaSistema,
					TipoInteres						: 'C',
			}
			
			var operaciones = []
			
			operaciones.push(op);
			
			if (operaciones.length > 0) {
				$scope.formData.operacionesVentaContado = operaciones;
			}
			
			var datosIngresoOrden = {
					p_IdEmpresa 				:	$scope.SesionIdEmpresa, //saca de variables de entorno
					p_IdUsuario 				:	$scope.SesionIdUsuario, //saca de variables de entorno
					p_IdModulo  				:	'ORD',
					p_simularOperacion  		:	'S',
					p_UsaReglasNegocio  		:	'S',
					p_Garantias					:	[],
					p_CobrosModificados 		: 	[],
					p_Ordenes 					:	[{
					
						IdInterno 						:	0,
					    FechaRecepcion 					:	'0001-01-01 00:00:00',
						FechaVigencia  					: 	($scope.formData.fechaVigencia)? util.dateSPdate($scope.formData.fechaVigencia) :  '0001-01-01',
						FechaEmision   					: 	'0001-01-01',
						FechaOperacion 					:	'0001-01-01', //se calcula en el server
						IdTipoOperacion 				:	$scope.formData.TipoInstrumento.IdTipoOperacion,//'COMRRV'
						IdCondicionDeLiquidacion 		:	$scope.formData.condicionLiquidacion.IdCondicionDeLiquidacion,//'T0',
						IdTipoVigencia 					: 	'SH',
						Glosa 							:	$scope.formData.glosa,
						Cantidad 						: 	$scope.formData.cantidad,
						MontoNeto 						:	$scope.formData.montoContado,//montoCalculado
						ValorRescate	 				: 	0,
						IdBoveda 						: 	$scope.formData.cuentaCustodioSeleccionada.IdBoveda || $scope.SesionValoresDefectoSistema.IdBovedaCuentaCustodia,
						IdFormaCustodia 				:	$scope.formData.formaCustodiaSeleccionada.IdFormaCustodiaOrden,//'NOM',
						IdPorcentajeVisibilidad 		:	1,
						PrecioLimite 					:	$scope.formData.TipoInstrumento.IdClasificacionTipoInstrumento === 'RF'?$scope.formData.precioRf:$scope.formData.precio,
						PrecioMercado 					:	$scope.formData.TipoInstrumento.IdClasificacionTipoInstrumento === 'RF'?$scope.formData.precioRf:$scope.formData.precioTc,
						EsFacturaInmediata 				:	'N',
						IdTipoPrecio 					:	'M',
						IdPortafolio 					:	$scope.formData.clienteSeleccionado.IdPortafolio,
						IdTipoLibroOrden 				:	'NORMAL',
						IdTipoCanalIngreso 				:	$scope.SesionTipoCanalIngreso,
						IdTipoReajuste 					:	'',
						IdEmisor						:	'',
						IdBolsa 						:	$scope.formData.idBolsa,
						EspecificaBolsa 				:	$scope.especificaBolsa,
						IdMonedaLiquidacionOrden 		:	$scope.formData.IdMoneda,
						TipoCambio 						:	null,//$scope.formData.tipoCambioLiquidacion,
						IdInstrumento 					:	$scope.formData.TipoInstrumento.IdInstrumento,
						IdInstrumentoSubyacente 		:	$scope.formData.instrumento.IdInstrumento,
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
						IdPersonaAutorizadoDarOrdenes 	: 	($scope.formData.autorizadoDarOrdenes&&$scope.formData.autorizadoDarOrdenes.IdPersona)?$scope.formData.autorizadoDarOrdenes.IdPersona:null,//null,//null,
						IdTipoCanalDistribucion 		:	'MAN',
						TasaReporte						:	$scope.formData.tasaReportadoV,//$scope.formData.tasaProm,//$scope.formData.tasaReportadoV,//tasaProm
						TasaReportanteReferencia		:	$scope.formData.tasaReportanteV,
						TasaPromedioReporte				:	$scope.formData.tasaProm,
						PlazoReporte					:	$scope.formData.diasFechaVencimiento,
						BaseTasa						:	$scope.formData.baseTasa.ValorBaseTasaCobro,
						PagoCobroCuentaInversion		:	$scope.formData.cuentaInversion ? 'S' : 'N',
						IdPreferenciaPagoPortafolio		:	$scope.formData.cuentaInversion ? null:($scope.formData.idPreferenciaPagoPortafolio ? $scope.formData.idPreferenciaPagoPortafolio:null),		
						TipoInteres						:	'C',
						IdMecanismoCaptacion 			: 	$scope.formData.mecanismoDeCaptacion?$scope.formData.mecanismoDeCaptacion:'',
						MontoInteresContado				:	$scope.formResumen.MontoInteresContado||0,
						MontoInteresPlazo				:	$scope.formResumen.MontoInteresPlazo||0,
						IdCuentaCustodioPortafolio		:	$scope.formData.cuentaCustodioSeleccionada.IdCuentaCustodioPortafolio || null,
					}]
				
					
			};
			var promise = bcsServicioOrden.CrearOrdenes(datosIngresoOrden).$promise;
			promise.then(function(respuesta){
				if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_RESULTADO[0].IdOrden > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0) 
				{
					$scope.formResumen.principalContado= respuesta.P_OUT_RESULTADO[0].MontoNeto;
					$scope.formResumen.montoLiquidacionContado=respuesta.P_OUT_RESULTADO[0].MontoTotal;
					$scope.formResumen.principalPlazo=respuesta.P_OUT_RESULTADO[0].MontoNetoPlazo;
					$scope.formResumen.montoLiquidacionPlazo=respuesta.P_OUT_RESULTADO[0].MontoTotalPlazo;
					$scope.formResumen.precioPlazo=respuesta.P_OUT_RESULTADO[0].PrecioPlazo; 
					//$scope.formResumen.interesPlazo=respuesta.P_OUT_RESULTADO[0].MontoNetoPlazo-respuesta.P_OUT_RESULTADO[0].MontoLiquidacionOperacion;//MontoNeto;
					$scope.formResumen.saldoAsignar=respuesta.P_OUT_RESULTADO[0].MontoLiquidacionOperacion;//respuesta.P_OUT_RESULTADO[0].MontoNeto;
					$scope.formResumen.tasaPromedio=respuesta.P_OUT_RESULTADO[0].TasaPromedioReporte;//promedio tasa reportante y tasa reportado
					$scope.formResumen.monedaOrden=respuesta.P_OUT_RESULTADO[0].IdMonedaOrden;
					$scope.formResumen.monedaLiq=respuesta.P_OUT_RESULTADO[0].IdMonedaLiquidacionOrden;
					$scope.formResumen.fechaOperacion=util.dateDS(respuesta.P_OUT_RESULTADO[0].FechaOperacionOrden,"FechaHoraSistema","CALL");
					$scope.formResumen.documentoIdentidad= respuesta.P_OUT_RESULTADO[0].DocumentoIdentidad;
					
					$scope.formResumen.MontoInteresContado = respuesta.P_OUT_RESULTADO[0].MontoInteresContado;
					$scope.formResumen.MontoInteresPlazo = respuesta.P_OUT_RESULTADO[0].MontoInteresPlazo;
					
					var operacionesExigibilidad = [];	
					operacionesExigibilidad.push({
						EsVentaCorta		: false,
						IdTipoInstrumento	: $scope.formData.TipoInstrumento.IdTipoInstrumento,
						IdBolsa				: $scope.formData.idBolsa,
						IdPortafolio		: $scope.formData.clienteSeleccionado.IdPortafolio,
						IdInstrumentoExige	: $scope.formData.TipoInstrumento.IdInstrumento,
						IdInstrumento		: $scope.formData.instrumento.IdInstrumento,
						Cantidad			: 0,//$scope.formData.cantidad,
						Precio				: 0,//$scope.formData.precio,
						Monto 				: $scope.formResumen.principalPlazo,
						SesionLocale		: $scope.SesionLocale
					});
					if (operaciones.length > 0) {
						$scope.formData.operacionesExigibilidad = operacionesExigibilidad;
					}
						
					
					if($scope.formData.TipoInstrumento.IdTipoOperacion === 'VENREP' && $scope.formData.TipoInstrumento.IdClasificacionTipoInstrumento === 'RV'){
						
						$scope.formData.gridGarantiasCartera.dataSource.removeAll();
						$scope.formData.gridGarantiasCtaInv.dataSource.removeAll();
						
						
						/* CUSTODIA GARANTIZABLE */
						
						var datosGarantia = {
								p_IdEmpresa 					: $scope.SesionIdEmpresa,
								p_IdUsuario 					: $scope.SesionIdUsuario,
								p_IdModulo 						: 'ORD',
								p_IdCliente 					: $scope.formData.clienteSeleccionado.IdCliente,
								p_IdPortafolio 					: $scope.formData.clienteSeleccionado.IdPortafolio,
								p_IdBolsa 						: $scope.formData.idBolsa,
								p_DiasLiquidacion 				: 0,	
								p_IdInstrumentoAGarantizar		: $scope.formData.TipoInstrumento.IdInstrumento,
								p_IdEntidad						: "CAV",					//nuevo a enviar de la interfaz
								p_IdTipoInstrumentoAGarantizar	: $scope.formData.idTipoInstrumento,	//nuevo a enviar de la interfaz
								p_IdTipoOperacion				: $scope.formData.TipoInstrumento.IdTipoOperacion,				//nuevo a enviar de la interfaz

						};
						console.log('datosGarantia:' + JSON.stringify(datosGarantia));
						var p1 = bcsServicioOrden.ConsultarCustodiaGarantizableReportes(datosGarantia).$promise;
						p1.then(function(result) {
						
							var custodiaGarantizable = [];
							for (var custodiaIndex in result.P_OUT_CUSTODIAGARANTIA)
							{
								var cantidad = 0;
								if (result.P_OUT_CUSTODIAGARANTIA[custodiaIndex].IdInstrumento === $scope.formData.instrumento.IdInstrumento){
									cantidad = result.P_OUT_CUSTODIAGARANTIA[custodiaIndex].CantidadDisponibleGarantia - $scope.formData.cantidad;
								}else {
									cantidad = result.P_OUT_CUSTODIAGARANTIA[custodiaIndex].CantidadDisponibleGarantia;
								}
								
								var custodiaInstrumento = {};
								custodiaInstrumento.IdInstrumento 						= result.P_OUT_CUSTODIAGARANTIA[custodiaIndex].IdInstrumento
								custodiaInstrumento.Instrumento 						= result.P_OUT_CUSTODIAGARANTIA[custodiaIndex].Instrumento
								custodiaInstrumento.IdTipoInstrumento 					= result.P_OUT_CUSTODIAGARANTIA[custodiaIndex].IdTipoInstrumento
								custodiaInstrumento.IdMonedaInstrumento 				= result.P_OUT_CUSTODIAGARANTIA[custodiaIndex].IdMonedaInstrumento
								custodiaInstrumento.IdMonedaSistema		 				= result.P_OUT_CUSTODIAGARANTIA[custodiaIndex].IdMonedaSistema
								custodiaInstrumento.MontoDisponibleGarantiaCustodia 	= Number(result.P_OUT_CUSTODIAGARANTIA[custodiaIndex].Precio) * Number(cantidad);
								custodiaInstrumento.CantidadDisponibleGarantiaCustodia 	= Number(cantidad);
								custodiaInstrumento.CantidadOriginalGarantiaCustodia 	= Number(cantidad);
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

								custodiaInstrumento.PorcentajeCoberturaEntidad = 0;
								custodiaInstrumento.PorcentajeCoberturaCorredora = 0;
								
								custodiaInstrumento.MontoCoberturaEntidad = 0;
								custodiaInstrumento.MontoCoberturaCorredora = 0;
								
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
								"IdPortafolio" 	: $scope.formData.clienteSeleccionado.IdPortafolio,
								"IdMoneda"		: null
							}],

							p_FechaDesde : $scope.SesionFechaSistema,
							p_FechaHasta : $scope.SesionFechaSistema
						};
					
						var p2 = bcsServicioOrden.ConsultarCtaInversionGarantia(datosCtaInvGarantia).$promise;
						p2.then(function(result) {
						
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
						
						
						$q.all([p1]).then(function(result) {
							$scope.formData.simulado = true;
							bcsComunes.ventanaOverlay($scope.winID,false);
						} , function(result) {
							$scope.formData.simulado = true;
							bcsComunes.ventanaOverlay($scope.winID,false);
						} );
						
					}
					if($scope.formData.TipoInstrumento.IdTipoOperacion === 'VENREP' && $scope.formData.TipoInstrumento.IdClasificacionTipoInstrumento === 'RF'){
						//$scope.formData.obtenerCobros();
						$scope.formData.simulado = true;
						bcsComunes.ventanaOverlay($scope.winID,false);
						
					}
					
					
					
				}
				else {
					$scope.formResumen.principalContado=0;
					$scope.formResumen.montoLiquidacionContado=0;
					$scope.formResumen.principalPlazo=0;
					$scope.formResumen.montoLiquidacionPlazo=0;
					$scope.formResumen.precioPlazo=0; 
					$scope.formResumen.MontoInteresPlazo=0;
					$scope.formResumen.MontoInteresContado=0;
					$scope.formResumen.saldoAsignar=0;
					$scope.formResumen.tasaPromedio=0;//promedio tasa reportante y tasa reportado
					$scope.formResumen.monedaOrden="";
					$scope.formResumen.monedaLiq="";
					$scope.formResumen.fechaOperacion="";
					$scope.formResumen.documentoIdentidad= "";
					//bcsComunes.msg("error",$translate.instant("UI_ADVERTENCIA"),respuesta.P_OUT_MENSAJES[0].descripcion);
					$scope.formData.simulado = false;
				}
				if (respuesta.P_OUT_ERRORESCONTADOR > 0){
					bcsComunes.ventanaMsg({
	                  conflictos:util.parseServerResponse(respuesta)
	                },$scope.winID);
				}
				bcsComunes.ventanaOverlay($scope.winID,false);
			},
			function(respuesta){
				console.log('error al simular');
				bcsComunes.ventanaOverlay($scope.winID,false);
				$scope.formData.simulado = false;
			});
			
			
			
		}else{
						
			$scope.filtros.filtrosReportantes.IdOrdenReportado.value = $scope.parametros.orden[0].IdOrden;
			$scope.formData.obtenerCobros();
			$scope.formData.simulado = true;
		
			var datosIngresoOrden = {
					p_IdEmpresa 				:	$scope.SesionIdEmpresa, //saca de variables de entorno
					p_IdUsuario 				:	$scope.SesionIdUsuario, //saca de variables de entorno
					p_IdModulo  				:	'ORD',
					p_simularOperacion  		:	'S',
					p_UsaReglasNegocio  		:	'S',
					p_Garantias					:	[],
					p_CobrosModificados 		: 	[],
					p_Ordenes 					:	[{
					
						IdInterno 						:	0,
					    FechaRecepcion 					:	'0001-01-01 00:00:00',
						FechaVigencia  					: 	($scope.formEditar.fechaVigencia)? $scope.formEditar.fechaVigencia :  '0001-01-01',
					    FechaEmision   					: 	'0001-01-01',
						FechaOperacion 					:	'0001-01-01', //se calcula en el server
						IdTipoOperacion 				:	$scope.formEditar.idTipoOperacion,//'COMRRV'
						IdCondicionDeLiquidacion 		:	$scope.formEditar.idCondicionDeLiquidacion,//'T0',
						IdTipoVigencia 					: 	'SH',
						Glosa 							:	$scope.formEditar.glosa,
						Cantidad 						: 	$scope.formEditar.cantidad,
						MontoNeto 						:	$scope.formEditar.montoContado,//montoCalculado
						ValorRescate	 				: 	0,
						IdBoveda 						: 	$scope.SesionValoresDefectoSistema.IdBovedaCuentaCustodia,
						IdFormaCustodia 				:	$scope.formEditar.idFormaCustodia,
						IdPorcentajeVisibilidad 		:	1,
						PrecioLimite 					:	$scope.formData.TipoInstrumento.IdTipoInstrumento === 'RRF'?$scope.formEditar.precioRf:$scope.formEditar.precio,
						PrecioMercado 					:	$scope.formData.TipoInstrumento.IdTipoInstrumento === 'RRF'?$scope.formEditar.precioRf:$scope.formEditar.precioTc,
						EsFacturaInmediata 				:	'N',
						IdTipoPrecio 					:	'M',
						IdPortafolio 					:	$scope.formEditar.idPortafolio,
						IdTipoLibroOrden 				:	'NORMAL',
						IdTipoCanalIngreso 				:	$scope.SesionTipoCanalIngreso,
						IdTipoReajuste 					:	'',
						IdEmisor						:	'',
						IdBolsa 						:	$scope.formEditar.idBolsa,
						EspecificaBolsa 				:	$scope.especificaBolsa,
						IdMonedaLiquidacionOrden 		:	$scope.formEditar.monedaLiquidacion,
						TipoCambio 						:	null,//$scope.formData.tipoCambioLiquidacion,
						IdInstrumento 					:	$scope.formEditar.idInstrumento,
						IdInstrumentoSubyacente 		:	$scope.formEditar.idInstrumentoSubyacente,
						IdCondicionDeLiquidacionPlazo 	: 	'',//'TP',
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
						IdPersonaAutorizadoDarOrdenes 	: 	($scope.formEditar.idPersonaAutorizadoDarOrdenes)?$scope.formEditar.idPersonaAutorizadoDarOrdenes:null,//null,//null,
						IdTipoCanalDistribucion 		:	'MAN',
						TasaReporte						:	$scope.formEditar.tasaReportadoV,//$scope.formData.tasaProm,//$scope.formData.tasaReportadoV,//tasaProm
						TasaReportanteReferencia		:	$scope.formEditar.tasaReportanteV,
						TasaPromedioReporte				:	$scope.formEditar.tasaPromedioR,
						PlazoReporte					:	$scope.formEditar.plazoDiasFechaVencimiento,
						BaseTasa						:	$scope.formEditar.baseTasa,
						PagoCobroCuentaInversion		:	$scope.formEditar.cuentaInversion?'S':'N',//$scope.formEditar.idMedioDePago=='CIN'?'S' : 'N',
						IdPreferenciaPagoPortafolio		:	$scope.formEditar.idMedioDePago=='CIN'? null:($scope.formEditar.IdPreferenciaPagoPortafolio ? $scope.formEditar.idPreferenciaPagoPortafolio:null),
						TipoInteres						:	'C',
						IdMecanismoCaptacion 			: 	$scope.formData.mecanismoDeCaptacion==''?$scope.formEditar.codigoClaseOrden:($scope.formData.mecanismoDeCaptacion?$scope.formData.mecanismoDeCaptacion:''), //$scope.formEditar.codigoClaseOrden?$scope.formEditar.codigoClaseOrden:''
						MontoInteresContado				:	$scope.formResumen.MontoInteresContado||0,
						MontoInteresPlazo				:	$scope.formResumen.MontoInteresPlazo||0,
						IdCuentaCustodioPortafolio		:	$scope.formEditar.idCuentaCustodioPortafolio || null,
													
					}] };
			
			var promise = bcsServicioOrden.CrearOrdenes(datosIngresoOrden).$promise;
			promise.then(function(respuesta){
				if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_RESULTADO[0].IdOrden > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0) 
				{
						$scope.formResumen.principalContado= respuesta.P_OUT_RESULTADO[0].MontoNeto;
						$scope.formResumen.montoLiquidacionContado=respuesta.P_OUT_RESULTADO[0].MontoTotal;
						$scope.formResumen.principalPlazo=respuesta.P_OUT_RESULTADO[0].MontoNetoPlazo;
						$scope.formResumen.montoLiquidacionPlazo=respuesta.P_OUT_RESULTADO[0].MontoTotalPlazo;
						
						$scope.formResumen.precioPlazo=respuesta.P_OUT_RESULTADO[0].PrecioPlazo; 
						//$scope.formResumen.interesPlazo=respuesta.P_OUT_RESULTADO[0].MontoNetoPlazo-respuesta.P_OUT_RESULTADO[0].MontoLiquidacionOperacion;//MontoNeto;
						$scope.formResumen.saldoAsignar=respuesta.P_OUT_RESULTADO[0].MontoLiquidacionOperacion;//MontoNeto;
						$scope.formResumen.tasaPromedio=respuesta.P_OUT_RESULTADO[0].TasaPromedioReporte;//promedio tasa reportante y tasa reportado
						$scope.formResumen.monedaOrden=respuesta.P_OUT_RESULTADO[0].IdMonedaOrden;
						$scope.formResumen.monedaLiq=respuesta.P_OUT_RESULTADO[0].IdMonedaLiquidacionOrden;
						$scope.formResumen.fechaOperacion=util.dateDS(respuesta.P_OUT_RESULTADO[0].FechaOperacionOrden,"FechaHoraSistema","CALL");
						$scope.formResumen.documentoIdentidad= respuesta.P_OUT_RESULTADO[0].DocumentoIdentidad;
						
						$scope.formResumen.MontoInteresContado = respuesta.P_OUT_RESULTADO[0].MontoInteresContado;
						$scope.formResumen.MontoInteresPlazo = respuesta.P_OUT_RESULTADO[0].MontoInteresPlazo;
				}
				else {
					$scope.formResumen.principalContado=0;
					$scope.formResumen.montoLiquidacionContado=0;
					$scope.formResumen.principalPlazo=0;
					$scope.formResumen.montoLiquidacionPlazo=0;
					$scope.formResumen.precioPlazo=0; 
					$scope.formResumen.MontoInteresPlazo=0;
					$scope.formResumen.MontoInteresContado=0;
					$scope.formResumen.saldoAsignar=0;
					$scope.formResumen.tasaPromedio=0;//promedio tasa reportante y tasa reportado
					$scope.formResumen.monedaOrden="";
					$scope.formResumen.monedaLiq="";
					$scope.formResumen.fechaOperacion="";
					$scope.formResumen.documentoIdentidad= "";
					//bcsComunes.msg("error",$translate.instant("UI_ADVERTENCIA"),respuesta.P_OUT_MENSAJES[0].descripcion);
				}
				if (respuesta.P_OUT_ERRORESCONTADOR > 0){
					bcsComunes.ventanaMsg({
	                  conflictos:util.parseServerResponse(respuesta)
	                },$scope.winID);
				}
				bcsComunes.ventanaOverlay($scope.winID,false);
			},
			function(respuesta){
				console.log('error al simular');
				bcsComunes.ventanaOverlay($scope.winID,false);
			});
			
			
			
			//bcsComunes.ventanaOverlay($scope.winID,false);
		}
		
	};
	
	$scope.refreshGrids = function(gridArray) {
		$timeout(function() {
			$scope.formData.gridGarantiasCtaInv.refresh();
			$scope.formData.gridGarantiasCartera.refresh();
			bcsComunes.ventanaTamanoAutomatico($scope.winID,40);
		},50);
	};
	
	$scope.formData.cancelar = function(){
		$scope.formData.simulado = false;
		$scope.formData.operacionesVenta = undefined;
		$scope.formData.cabeceraVenta = undefined;
		$scope.formData.tabResumen.active = true;
		$scope.filtros.filtrosReportantes.IdOrdenReportado.value = -1;
	};
	
	$scope.formData.limpiar=function(){
		$scope.formData.idMecanismoDeCaptacion = false;
		$scope.formData.IdMoneda = false;
		$scope.formData.IdMonedaFiltro = false;
		$scope.formData.IdCondicionLiquidacion = 'T0';
		$scope.ActualizarPlazoMaximo();
		$scope.formData.IdSocioNegocio = false;
		$scope.formData.clienteSeleccionado = undefined;
		$scope.formData.cuentaCustodioSeleccionada = undefined;
		$scope.formData.idCuentaCustodio = false; 
		$scope.formData.monedaSeleccionada = false;
		$scope.filtros.configuracionCuentaCustodio.IdPortafolio.value = "";
		$scope.formData.glosa = undefined;
		
		$scope.formData.operacionesCobertura = {};
		$scope.formData.operacionesLineaRiesgo = undefined;
		$scope.formData.operacionesVentaContado = undefined;
		$scope.formData.operacionesExigibilidad = {};
			
		$scope.formData.gridGarantiasCtaInv.dataSource.data({});		
		$scope.formData.gridGarantiasCartera.dataSource.data({});

		$scope.formResumen.principalContado = undefined;
		$scope.formResumen.montoLiquidacionContado = undefined;
		$scope.formResumen.MontoInteresContado = undefined;
		$scope.formResumen.principalPlazo = undefined;
		$scope.formResumen.montoLiquidacionPlazo = undefined;
		$scope.formResumen.MontoInteresPlazo = undefined;
		$scope.formResumen.saldoAsignar = undefined;
		$scope.formResumen.tasaPromedio = undefined;
		$scope.formResumen.precioPlazo = undefined;
		
		
		/*$scope.formData.simulado=false;
		$scope.formData.ingresado=false;
		$scope.formData.IdMoneda=undefined;
		$scope.formData.precioTc=undefined;
		$scope.formData.modoMonto=false;
		$scope.formData.montoContado=undefined;
		$scope.formData.precio=undefined;
		$scope.formData.mercado=undefined;
		$scope.formData.cantidad=undefined;
		$scope.formData.tasaReportadoV=undefined;
		$scope.formData.tasaReportanteV=undefined;
		$scope.formData.idBaseTasa=$scope.formData.IdBaseTasaDefecto;
		$scope.formData.tipoCambioOrigen=undefined;
		$scope.formData.tipoCambioLiquidacion=undefined;
		$scope.formData.fechaVencimientoVigencia=undefined;
		$scope.formData.diasFechaVencimiento=undefined;
		$scope.formData.idMedioDePagoCargo=$scope.formData.idMedioPagoDefecto;
		$scope.formData.comisionSabReportado=undefined;
		$scope.formData.glosa=undefined;
		$scope.formData.setInstrumento=-1;
		$scope.formData.idAutorizadoDarOrdenes=$scope.formData.idAutorizadoDarOrdenesDefecto;
		$scope.formData.idMecanismoDeCaptacion=$scope.formData.idMecanismoDeCaptacionDefecto;
		$scope.formData.IdMonedaFiltro=undefined;
		$scope.formData.tasaProm=undefined;*/
		$scope.formData.cantidad=undefined;
		$scope.formData.idMedioPagoDefecto	 			=	'CIN';
		$scope.formData.fechaInicial		 			=	util.dateInicioDia($scope.SesionFechaSistema);
		$scope.formData.fechaVigencia		 			=	util.dateInicioDia($scope.SesionFechaSistema);//util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL"),
		$scope.formData.mostrarReportado	 			=	true;
		$scope.formData.mostrarReportante	 			=	false;
		$scope.formData.TipoInstrumento		 			=	{};
		$scope.formData.Bolsaseleccionado	 			=	{};
		$scope.formData.idTipoInstrumento	 			=	{};
			
		$scope.formData.factorConversion				= 	undefined;
		$scope.formData.idTipoInstrumentoDefecto		=	'RV';
		$scope.formData.bloqueo							=	false;
		$scope.formData.simulado						=	false;
		$scope.formData.ingresado						=	false;
		$scope.formData.idBaseTasaDefecto       		=   2;
		$scope.formData.idBaseTasa       				=   2;
		$scope.formData.cuentaInversion					= 	true;
		$scope.formData.idAutorizadoDarOrdenesDefecto	=	undefined;
		$scope.formData.idMecanismoDeCaptacionDefecto	=	undefined;

		//$scope.formData.diasFechaVencimiento			=	bcsConfiguracionOrdenes.CONFIGURACION.PlazoMaximoReporteRV;
		$scope.ActualizarPlazoMaximo();

		$scope.formData.porcentajeDescuentoInstrumento	= 	undefined;
		$scope.formData.tasaReportanteV					= 	undefined;
		$scope.formData.tasaReportadoV					=	undefined;
		$scope.formData.formaCustodiaActual				=	'CUS';
		$scope.formData.IdSocioNegocio					= 	false;
		
		//Obtenemos valor de tasa reportante de referencia
		bcsConfiguracionOrdenes.CONFIGURACION.promise.then(function(result){
			$scope.formData.tasaReportanteV=bcsConfiguracionOrdenes.CONFIGURACION.TasaReferenciaReportante;
		    $scope.formData.diasFechaVencimiento = bcsConfiguracionOrdenes.CONFIGURACION.PlazoMaximoReporteRV;
		    $scope.formData.PorcentajeCastigoInstrumentoOrden = bcsConfiguracionOrdenes.CONFIGURACION.PorcentajeCastigoInstrumentoOrden
		});
	}
	
	$scope.actualizaTasa = function(grid){
		
		grid.dataSource.bind('change',function(){
			
			$scope.formEditar.actualizaTieneReportante();
		});
	};
	
	$scope.formData.actualizarDatosReportante = function(){
		bcsComunes.ventanaOverlay($scope.winID,true);
		$scope.filtros.filtrosReportantes.IdOrdenReportado.value = -1;
		var confReportante = bcsConfiguracionOrdenesV2.getDataSourceOrdenesReporte();
		var filter = {"field"   : "IdOrden","operator": "eq", "value"   : $scope.parametros.orden[0].IdOrden }
		confReportante.query({filter:filter}).then(function(){
			if (confReportante.data().length<1)
			{
				console.log("Orden no existe");
				$scope.formEditar.tieneReportante=false;
			}else{
				var dataReportante = confReportante.data()[0];
				$scope.formResumen.tasaPromedio = dataReportante.TasaPromedioReporte;
				$scope.formResumen.saldoAsignar = dataReportante.CantidadDisponibleAsociar;
				$scope.formEditar.tieneReportante=true;
			}
			bcsComunes.ventanaOverlay($scope.winID,false);
		})
	}
			
	$scope.formEditar.actualizaTieneReportante=function(){
		
		if($scope.modo==='EDITAR'){
			
			var aux=conf.options;
			
			if(aux && aux.dataSource.ds._data && aux.dataSource.ds._data.length>0 && aux.dataSource.ds._data[0].TasaReportante){
				if(aux.dataSource.ds._data[0].TasaReportante!=-1){
					$scope.formEditar.tieneReportante =true;
					var sum=$scope.formEditar.tasaReportadoV;
					var count=1;
					angular.forEach(aux.dataSource.ds._data, function(value, key) {
						 sum=sum+value.TasaReportante;
						 count=count+1;
					});
					$scope.formEditar.tasaPromedioR=sum/count;
					
					angular.forEach(aux.dataSource.ds._data, function(value, key) {
						value.TasaPromedio=$scope.formEditar.TasaPromedioReporte;
				   });
					
					// tasaPromedioR $scope.formEditar.tasaPromedioR=($scope.formEditar.tasaReportanteV+$scope.formEditar.tasaReportadoV)/2;

					/*$scope.formEditar.tasaReportante=aux.dataSource.ds._data[0].TasaReportante;
					$scope.formEditar.tasaPromedioSugerida=($scope.formEditar.tasaReportante+$scope.formEditar.tasa)/2;*/
					
				}
				else{
					$scope.formEditar.tieneReportante =false;
					$scope.formEditar.tasaPromedioR=0;
					/*$scope.formEditar.tasaReportante=0;
					$scope.formEditar.tasaPromedio=0;
					$scope.formEditar.tasaPromedioSugerida=0;*/			
				}
				
			}
			else{
				$scope.formEditar.tieneReportante =false;
				/*$scope.formEditar.tasaReportante=0;
				$scope.formEditar.tasaPromedio=0;
				$scope.formEditar.tasaPromedioSugerida=0;*/	
								
			}
			
		}
	}; 
	
	$scope.formData.obtenerCobros = function(){
		bcsComunes.ventanaOverlay($scope.winID,true);
		
		var dsCobrosVentaContado = new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering:true,
					transport: {
						read: {
							url: bcsConsultasOrdenes('ConsultarCobrosOrden'),
							dataType: "json"
							}
					},
					schema: {
						model : {
							fields : {
								IdCobro : { type : "string" },
								ValorCobroAplicado : { type : "number" }
							}
						}
					}
				}
		});
		
		var dsCobrosReportanteContado = angular.copy(dsCobrosVentaContado);
		
		var aux=conf.options;
		$scope.reportantes = {};
		
		var filtroCobroReportantes = {"logic": "or", "filters":[]} 
		if(aux && aux.dataSource.ds._data && aux.dataSource.ds._data.length>0){
			for(var i =0; i<aux.dataSource.ds._data.length;i++){
				if(aux.dataSource.ds._data[i].IdOrdenAsociada){
					
					$scope.reportantes[aux.dataSource.ds._data[i].IdOrdenAsociada] = {
						IdOrdenAsociada : aux.dataSource.ds._data[i].IdOrdenAsociada,
						operacionesReportanteContado:[{
							IdOrdenAsociada					: aux.dataSource.ds._data[i].IdOrdenAsociada,
							IdInstrumento  					: aux.dataSource.ds._data[i].IdInstrumentoReportante,
							IdInstrumentoSubyacente			: aux.dataSource.ds._data[i].IdInstrumentoSubyacente,
							DiasPlazo 						: aux.dataSource.ds._data[i].PlazoReportante,
							CantidadOrden   				: aux.dataSource.ds._data[i].CantidadAsociada,
							PrecioOrden		   				: aux.dataSource.ds._data[i].PrecioOrden,
							MontoOrden						: aux.dataSource.ds._data[i].MontoAsociado,
							MontoTotal						: aux.dataSource.ds._data[i].MontoAsociado,
							TasaSimultanea  				: 0,
							IdMonedaLiquidacionOrden        : aux.dataSource.ds._data[i].IdMonedaLiquidacionOrdenReportante,
							IdPortafolio 					: aux.dataSource.ds._data[i].IdPortafolioReportante,
							IdTipoProducto 					: aux.dataSource.ds._data[i].IdTipoProducto,
							IdTipoOperacion 				: aux.dataSource.ds._data[i].IdTipoOperacionReportante,
							IdCanalIngreso 					: aux.dataSource.ds._data[i].IdTipoCanalIngresoReportante,
							IdBolsa 						: aux.dataSource.ds._data[i].IdBolsa,
							IdEmisor						: '',
							IdCorredorCorresponsal			: null,
							PlazoReporte					: aux.dataSource.ds._data[i].PlazoReportante,
							BaseTasa 						: aux.dataSource.ds._data[i].BaseReporteReportante,
							FechaOperacion					: util.dateSPdate(aux.dataSource.ds._data[i].FechaOperacionReportante),
							TasaReporte						: $scope.formEditar.tasaPromedioR,
							TasaReportado					: aux.dataSource.ds._data[i].TasaReportante,
							TipoInteres						: 'C',
							EsAPlazo						: 'N',
					}],
						operacionesReportantePlazo:[{
							IdOrdenAsociada					: aux.dataSource.ds._data[i].IdOrdenAsociada,
							IdInstrumento  					: aux.dataSource.ds._data[i].IdInstrumentoReportante,
							IdInstrumentoSubyacente			: aux.dataSource.ds._data[i].IdInstrumentoSubyacente,
							DiasPlazo 						: aux.dataSource.ds._data[i].PlazoReportante,
							CantidadOrden   				: aux.dataSource.ds._data[i].CantidadAsociada,
							PrecioOrden		   				: aux.dataSource.ds._data[i].PrecioOrden,
							MontoOrden						: aux.dataSource.ds._data[i].MontoAsociado,
							MontoTotal						: aux.dataSource.ds._data[i].MontoAsociado,
							TasaSimultanea  				: 0,
							IdMonedaLiquidacionOrden        : aux.dataSource.ds._data[i].IdMonedaLiquidacionOrdenReportante,
							IdPortafolio 					: aux.dataSource.ds._data[i].IdPortafolioReportante,
							IdTipoProducto 					: aux.dataSource.ds._data[i].IdTipoProducto,
							IdTipoOperacion 				: aux.dataSource.ds._data[i].IdTipoOperacionReportante,
							IdCanalIngreso 					: aux.dataSource.ds._data[i].IdTipoCanalIngresoReportante,
							IdBolsa 						: aux.dataSource.ds._data[i].IdBolsa,
							IdEmisor						: '',
							IdCorredorCorresponsal			: null,
							PlazoReporte					: aux.dataSource.ds._data[i].PlazoReportante,
							BaseTasa 						: aux.dataSource.ds._data[i].BaseReporteReportante,
							FechaOperacion					: util.dateSPdate(aux.dataSource.ds._data[i].FechaOperacionReportante),
							TasaReporte						: $scope.formEditar.tasaPromedioR,
							TasaReportado					: aux.dataSource.ds._data[i].TasaReportante,
							TipoInteres						: 'C',
							EsAPlazo						: 'N',
							
					}], cobrosReportanteModificadosContado:[],cobrosReportanteModificadosPlazo:[], IdOrden: aux.dataSource.ds._data[i].IdOrdenReportante, visible: false};
					filtroCobroReportantes.filters.push({"field": "IdGrupoCobro","operator": "eq","value": aux.dataSource.ds._data[i].IdGrupoCobroReportante})
				}
				
			}
		}
		
		var op = {
					IdInstrumento  					: $scope.parametros.orden[0].IdInstrumento,
					IdInstrumentoSubyacente			: $scope.parametros.orden[0].IdInstrumentoSubyacente,
					DiasPlazo 						: $scope.parametros.orden[0].Plazo,
					CantidadOrden   				: $scope.parametros.orden[0].CantidadOrden,
					PrecioOrden		   				: $scope.parametros.orden[0].PrecioOrden,
					MontoOrden						: $scope.parametros.orden[0].MontoOrden,
					TasaReporte  					: $scope.formEditar.tasaPromedioR,
					TasaReportado 					: $scope.parametros.orden[0].TasaReporte,
					IdMonedaLiquidacionOrden        : $scope.parametros.orden[0].IdMonedaLiquidacionOrden,
					IdPortafolio 					: $scope.parametros.orden[0].IdPortafolio,
					IdTipoProducto 					: $scope.parametros.orden[0].IdTipoProducto,
					IdTipoOperacion 				: $scope.parametros.orden[0].IdTipoOperacion,
					IdCanalIngreso 					: $scope.parametros.orden[0].IdTipoCanalIngreso,
					IdBolsa 						: $scope.parametros.orden[0].IdBolsa,
					PlazoReporte					: $scope.parametros.orden[0].Plazo,
					BaseTasa						: $scope.parametros.orden[0].BaseTasa,
					FechaOperacion					: util.dateSPdate($scope.parametros.orden[0].FechaOperacionOrden),
					IdCorredorCorresponsal			: null,
					TipoInteres						: 'C',
			}
			
		var operaciones = []
		
		operaciones.push(op);
		
		if (operaciones.length > 0)
			$scope.formData.operacionesVentaContado = operaciones;
		
		if($scope.formEditar.tasaPromedioR == $scope.parametros.orden[0].TasaPromedioReporte){
			
			$timeout(function() {
				var ventaContado = dsCobrosVentaContado.query({filter:{field: "IdGrupoCobro", operator: "eq", value: $scope.parametros.orden[0].IdGrupoCobro  }});
				
				if(filtroCobroReportantes.filters.length == 0){
					filtroCobroReportantes = {field: "IdGrupoCobro", operator: "eq", value: -1  }
				}
				var cobrosReportantesContado = dsCobrosReportanteContado.query({filter:filtroCobroReportantes});
				
				ventaContado.then(function(){
					if(dsCobrosVentaContado.data().length > 0){
						var matrizModificada = {};
						var cobros = dsCobrosVentaContado.data();
						for(var i = 0; i<cobros.length;i++){
							matrizModificada[cobros[i].IdCobro] = cobros[i].ValorCobroAplicado;
						}
						
						$scope.formData.cobrosModificadosVentaContado = matrizModificada;
					}
				});				

				cobrosReportantesContado.then(function(){
					if(dsCobrosReportanteContado.data().length > 0){
						var cobros = dsCobrosReportanteContado.data();
						var matrizModificada = {};
						for(var i = 0; i<cobros.length;i++){
							if(!matrizModificada[cobros[i].IdAsociacion]){
								matrizModificada[cobros[i].IdAsociacion] = {
										cobrosModificados:{}
								}
							}
							matrizModificada[cobros[i].IdAsociacion].cobrosModificados[cobros[i].IdCobro] = cobros[i].ValorCobroAplicado;
						}
						$.each(matrizModificada,function(key,value) {
							if($scope.reportantes[key]){
								$scope.reportantes[key].cobrosReportanteModificadosContado = value.cobrosModificados;
							}
						});
					}
				});
				
				
				$q.all([ventaContado,cobrosReportantesContado]).then(function(result) {
					console.log("REPORTANTES",$scope.reportantes);
					console.log("VENTA_CONTADO",$scope.formData.cobrosModificadosVentaContado);
					bcsComunes.ventanaOverlay($scope.winID,false);
				} , function(result) {
					bcsComunes.ventanaOverlay($scope.winID,false);
				} );
			},2000);
			
		}else{
			bcsComunes.ventanaOverlay($scope.winID,false);
		}
		
	}
	
	$scope.verDetalle=function() {
		bcsComunes.ventanaAbrir("DetalleOrdenReporte", {IdOrden : $scope.formResumen.idOrden},false,false );
	};
	$scope.verComprobante=function(){
		bcsConfiguracionOrdenes.FUNCIONES.verComprobante($scope.SesionIdEmpresa,
				$scope.formResumen.documentoIdentidad,
				$scope.formResumen.fechaOperacion,
				$scope.formResumen.idOrden,
				$scope.formResumen.montoLiquidacionContado,
				$scope.winID);
	}
	
	$scope.ActualizarPlazoMaximo();
	bcsComunes.ventanaAutoHeight(idVentana);
});