angular.module('ModuloMesa').controller('CrearIngresoDepositoDesembolsoMesaRFCtrl',
function($scope,$translate,$timeout,$rootScope,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,bcsServiciosMesa,bcsServiciosGrillasMesaRF,bcsServicioPlataforma)
{
	var IngresoOperacion = this;
	$scope.data = {};
	$scope.dataNew = {};
	$scope.data.IdMonedaMontoDisplay = $scope.SesionIdMonedaSistema;
	$scope.data.IdDecimalMontoDisplay = util.bcs_formato(0,'MES','Monto',$scope.data.IdMonedaMontoDisplay, '','').cantidadDecimales;
	
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	var ident = '>> ';
	var parametrosVentana = bcsComunes.ventanaParams($scope.winID);
	
	var dataSourcePreferenciaPortafolio = bcsServiciosGrillasMesaRF.getDataSourcePreferenciaPortafolio();
	var dataSourceDetalleOperacion = bcsServiciosGrillasMesaRF.getDataSourceDetalleOperacionMesaRF();
	var buscaParidad = true;
	var ParametrosMesa = {};
	var params = {
			p_IdEmpresa 	 	: $scope.SesionIdEmpresa,	 
			p_IdTipoProducto 	: 'MERF',
			p_CodigoParametro 	: ''
	};

	$scope.data.habilitaCabecera = true;
	
	// --------------------------------------------------
	// CONFIGURACION DE COMBOS
	// --------------------------------------------------
	$scope.data.oConfiguracionMesas = {
			tipo : "combobox-mesas",
			configuracion : {},
			idActual : false,
			idDefecto : false,
			seleccionado : false,
			filtros : { IdEstado : { "field"   : "IdEstadoRegistro", "operator": "eq", "value" : "ACT" },
					    IdTipoProducto : { "field"   : "IdTipoProducto", "operator": "eq", "value" : "MERF" }
					},
	};
	$scope.data.oConfiguracionSocioNegocio={
		tipo : "combobox-socio-negocio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}/*,
			IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  -1 }*/
		},
		idDefecto : ''
	};
	
	$scope.data.oConfiguracionPortafolio={
			tipo : "combobox-portafolio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }/*,
				IdPortafolio 	: { "field": "IdPortafolio","operator": "eq", "value"   :  0 }*/
			},
	};
	$scope.data.oConfiguracionTipoOperacion={
			tipo : "combobox-tipo-operacion",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdTipoProducto : {"field": "IdTipoProducto","operator": "eq", "value"   :  'MERF' },
				EsAnticipo : {"field": "EsAnticipo","operator": "eq", "value"   :  'N' },
				EsAnulacion : {"field": "EsAnulacion","operator": "eq", "value"   :  'N' },
				Accion : {"field": "Accion","operator": "eq", "value"   :  '' }
			},
			idDefecto : ''
	};
		  
	
	$scope.IdEstado 			= {"field" : "IdEstado","operator": "eq", "value"   : "ACT" };
	$scope.IdTipoReajustePEN  	= {"field" : "IdTipoReajuste","operator": "contains", "value"   : "PEN" };
	$scope.IdTipoReajusteUSD  	= {"field" : "IdTipoReajuste","operator": "contains", "value"   : "USD" };
	
	$scope.data.oConfiguracionTipoReajusteOperacion={
		tipo : "combobox-tipo-reajuste",
		configuracion : {
			template: '<span >#: IdTipoReajuste # - #: Nombre #</span>'
		},
		idActual : false,
		seleccionado : false,
		filtros : {
			  complejo : [$scope.IdEstado,$scope.IdTipoReajustePEN,$scope.IdTipoReajusteUSD],
			  logic: "and",
			  filters: [
				{
				  logic: "or",
					filters: [$scope.IdTipoReajustePEN, $scope.IdTipoReajusteUSD]
				},
				$scope.IdEstado
			  ]	
		},
		idDefecto : ''
	};
	
	$scope.data.oConfiguracionMoneda={
		tipo : "combobox-tipo-instrumento-moneda-liquidacion",
		configuracion : {},
		filtros : {
			IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value"   : "" },
			IdMoneda : { "field"   : "IdMoneda","operator": "eq", "value"   : "" }
		}
	};
	
	$scope.data.oConfiguracionCondicionLiquidacionOperacion={
		tipo : "combobox-condicion-liquidacion",
		configuracion : {
			dataTextField: "Codigo",
			placeholder: "",
			template: '<span >#: Codigo #</span>'
		},
		filtros : {
			IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   : "" },
			EstadoTipoOperacionYCondicionDeLiquidacion : { "field"   : "EstadoTipoOperacionYCondicionDeLiquidacion","operator": "eq", "value"   : "ACT" }
		}
	};
	$scope.data.oConfiguracionMonedaLiquidacionOperacion={
		tipo : "combobox-tipo-instrumento-moneda-liquidacion",
		configuracion : {
			template: '<span >#: IdMonedaLiquidacion # - #: NombreMonedaLiquidacion #</span>'
		},
		filtros : {
			IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value"   : "" },
			IdMoneda : { "field"   : "IdMoneda","operator": "eq", "value"   : -1 }
		},
		idActual : false,
		idDefecto : ''
	};

	$scope.data.oConfiguracionEmisor={
		tipo : "combobox-emisor",
		configuracion : {},
		filtros : {
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
		},
		idActual : false,
		idDefecto : ''
	};
	$scope.data.oConfiguracionMontoOperacionMoneda = {
			//max: 0
	};
	
	
	//funciones
	var success = function(respuesta)
	{
		Push('success');		
		//si vuelve ref id, se creo la asignacion
		bcsComunes.ventanaOverlay($scope.winID, false);
		if (respuesta.P_OUT_ERRORESCONTADOR === 0) 
		{		
			
			//bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			//$scope.buscarMovimientos();
			bcsComunes.ventanaData($scope.winID,[{IdOperacion : respuesta.P_OUT_OPERACION[0].IdOperacion}]);
			bcsComunes.ventanaMsg({
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
			if (respuesta.P_OUT_OPERACION[0].Accion === 'CERRAR'){
				//bcsComunes.msg("info","info",$translate.instant('UI_OPERACION_INGRESO_EXITO'));
				
				reset();
				$scope.data.habilitaCabecera = true;
				$scope.data.EnProcesoGrabar = false;
				//bcsComunes.ventanaOverlay($scope.winID, false);
				//bcsComunes.ventanaCerrar($scope.winID);
				//Pop();
				return;
			}
			
			// Solo si tiene exito, sincroniza la grilla ??...
			
			$scope.data.kngSecuenciaOperacion = respuesta.P_OUT_OPERACION[0].IdOperacion;
			$scope.data.kngFolioOperacion = respuesta.P_OUT_OPERACION[0].FolioOperacion;
			$scope.data.IdEstadoOperacion = respuesta.P_OUT_OPERACION[0].IdEstado;
			$scope.data.EstadoOperacion = respuesta.P_OUT_OPERACION[0].NombreEstado;
			$scope.data.formasDePago = respuesta.p_out_FormasDePago;
			$scope.data.EnProcesoGrabar = false;
		}
		else
		{			
			//bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_INGRESO_ERROR'));
			var id = bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);
			//bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.ventanaPromesa(id).then(function(result){});
			$scope.data.EnProcesoGrabar = false;
		}
		//Pop();
		
	};
	var error = function(respuesta)
	{		
		Push('error');		
		//bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_INGRESO_ERROR'));
		//bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		//if (!respuesta.P_OUT_ERRORESCONTADOR) respuesta.P_OUT_ERRORESCONTADOR = 1;
		var id = bcsComunes.ventanaMsg({
						//mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"),
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);
		bcsComunes.ventanaPromesa(id).then(function(result){
			bcsComunes.ventanaOverlay($scope.winID, false);
		});
		//Pop();
	};

	var ErrGrabaDetalle = function(respuesta)
	{		
		Push('ErrGrabaDetalle');		
		//bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_INGRESO_ERROR'));
		//bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		//if (!respuesta.P_OUT_ERRORESCONTADOR) respuesta.P_OUT_ERRORESCONTADOR = 1;
		var id = bcsComunes.ventanaMsg({
						//mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"),
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);
		bcsComunes.ventanaPromesa(id).then(function(result){
			bcsComunes.ventanaOverlay($scope.winID, false);
		});
		//Pop();
	};
	var enableCabeceraOperacion = function(){
		Push('enableCabeceraOperacion');		
		deshabilitaIntermesa();
		$scope.data.EnabledTipoOperacion = true;
		$scope.data.RequeridoTipoOperacion = true;
		//$scope.data.kngTasaOperacion = '';
		$scope.data.plazoOperacion = '';
		
	};
	var deshabilitaIntermesa = function(){
		Push('deshabilitaIntermesa');
		if ($scope.data.oConfiguracionMesas.idActual && $scope.data.oConfiguracionMesas.idActual !== parametrosVentana.IdMesa)
			$scope.data.deshabilitaIntermesa = false;
		else
			$scope.data.deshabilitaIntermesa = true;
	};
	var resetCabecera = function (){
		Push('resetCabecera');		
		$scope.data.oConfiguracionSocioNegocio.idActual = false;
		$scope.data.oConfiguracionPortafolio.idActual = false;
		$scope.data.oConfiguracionTipoOperacion.idActual = false;
		$scope.data.oConfiguracionTipoReajusteOperacion.idActual = false;
		$scope.data.oConfiguracionMonedaLiquidacionOperacion.idActual = false;
		
		$scope.data.SesionIdEmpresa = $scope.SesionIdEmpresa;
		$scope.data.kngFechaOperacion = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
		
		$scope.data.kngMontoOperacion = '';
		$scope.data.kngTasaOperacion = '';
		$scope.data.kngMontoOperacionMoneda = '';
		$scope.data.kngSecuenciaOperacion = '';
		$scope.data.kngFolioOperacion = '';
		$scope.data.plazoOperacion = '';
		$scope.data.IdCondicionDeLiquidacion = '';
		$scope.data.EstadoOperacion = '';
		$scope.data.oConfiguracionTipoReajusteOperacion.idDefecto = $scope.data.IdMonedaMontoDisplay;
		$scope.data.oConfiguracionTipoReajusteOperacion.idActual = $scope.data.IdMonedaMontoDisplay;
		$scope.data.oConfiguracionMonedaLiquidacionOperacion.idDefecto = $scope.data.IdMonedaMontoDisplay;
		$scope.data.oConfiguracionMonedaLiquidacionOperacion.idActual = $scope.data.IdMonedaMontoDisplay;
		
		$scope.data.oConfiguracionEmisor.idActual = '';
		
	};
	var resetPie = function (){
		Push('resetPie');		
		$scope.data.NombreAgente = '';
		$scope.data.NombreOperador = $scope.SesionUsuario;
				
	};

	
	var reset = function (){
		Push('reset');		
		resetCabecera();
		enableCabeceraOperacion();
		resetPie();
		
		bcsServiciosMesa.ConsultarConfiguracionMesa(params).$promise.then(function(respuesta){
			$.each(respuesta.P_OUT_DATOSMESA,function(key,value){
				ParametrosMesa[value.CodigoParametro] = value.Valor;
			});
			
			$scope.data.habilitaVentaNominal = ParametrosMesa.ACEPTA_VENTA_NOMINAL_CORREDOR === 'S';
			
		});
		
	};
	var calcularParidadMonto = function(montoLocal,callback){
		Push('calcularParidadMonto');
		var data = Object();
		if (!$scope.data.oConfiguracionTipoReajusteOperacion.idActual){// || Number($scope.data.kngMontoOperacionMoneda) <= 0){
			$scope.data.kngMontoOperacion = 0;
			if(callback) callback();
		}
		else {
			data.p_IdEmpresa =  $scope.SesionIdEmpresa;
			data.p_IdTrabajo =  1314;
			data.p_CalculosEntrada = [{
				"IdTrabajo"			: data.p_IdTrabajo
				,"IdInterno"		: 1
				,"Fecha"			: util.dateSPdate($scope.data.kngFechaOperacion)
				,"IdMonedaOrigen" 	: !montoLocal?$scope.data.oConfiguracionTipoReajusteOperacion.idActual || $scope.data.IdMonedaMontoDisplay:$scope.data.IdMonedaMontoDisplay
				,"IdMonedaDestino" 	: !montoLocal?$scope.data.oConfiguracionTipoReajusteOperacion.idActual || $scope.data.IdMonedaMontoDisplay:$scope.data.IdMonedaMontoDisplay
				,"ValorCambioLocalModificado" : 0
				,"MontoOrigen"		: !montoLocal?$scope.data.kngMontoOperacionMoneda || 0:$scope.data.kngMontoOperacion || 0
				,"IdUso"			: 'MESA'
				//,"TipoCambio" 		: 0
				//,"MonedaCambio"		: $scope.data.IdMonedaMontoDisplay
				//,"OrigenTipoCambio" : 'OBSERVADO'
			}];

			bcsServiciosMesa.ObtenerParidadTipoCambio(data,function(value){
				if (value.p_out_CalculosSalida.length > 0){
					$scope.data.kngMontoOperacion = value.p_out_CalculosSalida[0].MontoLocal;
					$scope.data.kngMontoOperacionMoneda = value.p_out_CalculosSalida[0].MontoDestino <= 0? value.p_out_CalculosSalida[0].MontoOrigen:value.p_out_CalculosSalida[0].MontoDestino;
				}
				else
					$scope.data.kngMontoOperacion = 0;
				if(callback) callback();
			});
		}
		
	};
	var calcularMontoOperacionMoneda = function(){
		Push('calcularMontoOperacionMoneda');
		var monto = 0;
		
		if ($scope.data.kngMontoOperacionMoneda && $scope.data.plazoOperacion && $scope.data.kngTasaOperacion){
			monto = Number(bcsComunes.calcularMontoPlazo(Number($scope.data.kngMontoOperacionMoneda),Number($scope.data.plazoOperacion),Number($scope.data.kngTasaOperacion))).toFixed($scope.data.IdDecimalTipoReajusteDisplay, 360);
			//monto = Number(util.formateaNumero(bcsComunes.calcularMontoPlazo(Number($scope.data.kngMontoOperacionMoneda),Number($scope.data.plazoOperacion),Number($scope.data.kngTasaOperacion)),$scope.data.IdDecimalTipoReajusteDisplay));
			$scope.data.kngMontoValorFinalOperacionMoneda = Number(monto);
			
		}
		else {
			$scope.data.kngMontoValorFinalOperacionMoneda = $scope.data.kngMontoOperacionMoneda;
		}
		
	};
	var calcularMontoOperacion = function(){
		Push('calcularMontoOperacion');
		var monto = 0;
		if ($scope.data.kngMontoOperacion && $scope.data.plazoOperacion && $scope.data.kngTasaOperacion){
			monto = Number(bcsComunes.calcularMontoPlazo(Number($scope.data.kngMontoOperacion),Number($scope.data.plazoOperacion),Number($scope.data.kngTasaOperacion))).toFixed($scope.data.IdDecimalMontoDisplay);
			//monto = Number(util.formateaNumero(bcsComunes.calcularMontoPlazo(Number($scope.data.kngMontoOperacion),Number($scope.data.plazoOperacion),Number($scope.data.kngTasaOperacion)),$scope.data.IdDecimalMontoDisplay));
			$scope.data.kngMontoValorFinalOperacion = Number(monto);
		}
		else {
			$scope.data.kngMontoValorFinalOperacion = $scope.data.kngMontoOperacion;
		}
		//calcularMontoSimulacion();
		
	};
	
	var setData = function(opcion){
		Push('setData');
		var datosParam = new Object();
		var formaDePago = [];
		var fechaVen = '';
		fechaVen = new Date($scope.data.fechaVencimientoOperacion || $scope.SesionFechaSistema || '0001-01-01');

		datosParam.p_IdEmpresa = $scope.SesionIdEmpresa;
		datosParam.p_IdUsuario = $scope.SesionIdUsuario;
    	datosParam.p_IdModulo = 'MRF';
    	datosParam.p_UsaReglasNegocio = (opcion === 'CERRAR'?'S':'N');
		datosParam.p_Operacion = [{
							"Accion"					: opcion
							,"IdInternoOperacion"		: 1
							,"IdOperacion"				: Number($scope.data.kngSecuenciaOperacion || 0)
							,"FolioOperacion"			: Number($scope.data.kngFolioOperacion || 0)
							,"IdCliente" 				: Number($scope.data.oConfiguracionPortafolio.seleccionado.IdCliente)//Number(opcion==='PENDIENTE'?$scope.data.IdCliente:$scope.data.oConfiguracionPortafolio.seleccionado.IdCliente)
							,"IdPortafolio"				: Number($scope.data.oConfiguracionPortafolio.seleccionado.IdPortafolio)//Number(opcion==='PENDIENTE'?$scope.data.IdPortafolio:$scope.data.oConfiguracionPortafolio.seleccionado.IdPortafolio)
							,"IdClienteCorredor"		: Number(parametrosVentana.IdClienteCorredor)
							,"IdPortafolioCorredor"		: Number(parametrosVentana.IdPortafolioCorredor)
							,"Intermesa"				: $scope.data.esIntermesa?'S':'N'
							,"IdMesaOrigen"				: Number(parametrosVentana.IdMesa)
							,"IdMesaDestino"			: Number($scope.data.oConfiguracionMesas.seleccionado.IdMesa)
							,"AfectaCustodia"			: $scope.data.EsCustodia?'S':'N'
							,"IdTipoProducto"			: $scope.data.oConfiguracionTipoOperacion.seleccionado.IdTipoProducto
							,"IdTipoOperacion"			: $scope.data.oConfiguracionTipoOperacion.seleccionado.IdTipoOperacion
							,"IdTipoReajuste"			: $scope.data.oConfiguracionTipoReajusteOperacion.seleccionado.IdTipoReajuste
							,"Plazo"					: Number($scope.data.plazoOperacion || 0)
							,"Monto"					: Number($scope.data.kngMontoOperacion || 0)
							,"MontoMoneda"				: Number($scope.data.kngMontoOperacionMoneda || 0) //cambiar al monto en moneda local
							,"IdMoneda"					: $scope.data.IdMonedaMontoDisplay
		  			   		,"IdMonedaLiquidacion"		: $scope.data.oConfiguracionMonedaLiquidacionOperacion.seleccionado.IdMonedaLiquidacion
							,"Tasa"						: Number($scope.data.kngTasaOperacion || 0)
							,"ValorFinal"				: Number($scope.data.kngMontoValorFinalOperacion || 0)
							,"ValorFinalMoneda"			: Number($scope.data.kngMontoValorFinalOperacionMoneda || 0)
							,"TasaAgente"				: Number($scope.data.kngTasaAgente || 0)
							,"FechaOperacion" 			: $scope.data.kngFechaOperacion
							,"FechaVencimiento" 		: util.dateSPdate(fechaVen)
							,"IdCondicionLiquidacion"	: $scope.data.IdCondicionDeLiquidacion
						 	,"FechaLiquidacion" 		: util.dateSPdate($scope.data.fechaLiquidacionOperacion || '0001-01-01')
						 	,"IdAgente"					: Number($scope.data.oConfiguracionSocioNegocio.seleccionado.IdPersonaEjecutivo || 0)
						 	,"IdOperador"				: $scope.SesionIdUsuario
						 	,"IdEstado"					: $scope.data.IdEstadoOperacion || ''//$scope.data.oConfiguracionEstado.seleccionado || ''
						 	,"Glosa"					: $scope.data.Comentarios || ''
						 	,"IdTipoCanalIngreso"		: $scope.SesionTipoCanalIngreso
						 	,"IdEmisor"					: $scope.data.oConfiguracionEmisor.seleccionado.IdEmisor
						 	
		}];
		formaDePago.push({
			 "IdInternoPagoOperacion" : 1
			,"IdInternoOperacion" :  1
			,"IdComprobantePagoOperacion" : Number($scope.data.kngIdComprobantePagoOperacion || 0)
			,"IdFormaDePago":$scope.data.oConfiguracionTipoOperacion.seleccionado.IdTipoOperacion + '_CIN_'+ $scope.data.IdMonedaMontoDisplay +'_0'
			,"IdOperacion":Number($scope.data.kngSecuenciaOperacion || 0)
			,"RecibePagaPortafolio":'R'
			,"IdMoneda":$scope.data.IdMonedaMontoDisplay
			,"IdMonedaLiquidacion":$scope.data.oConfiguracionMonedaLiquidacionOperacion.idActual
			,"MontoFormaDePago":$scope.data.kngMontoOperacion
			,"AfectarTesoreria" :'N'});//key.includes("_CIN_")?'N':'S'});
		datosParam.p_FormasDePago = formaDePago;
		console.log(datosParam);
		
		return datosParam;
	};
	
	var validarOperacion = function(callback){
		Push('validarOperacion');
		var datosEnvio = {};
		if ($scope.data.oConfiguracionPortafolio.idActual){// && $scope.data.fechaLiquidacionOperacion){
			datosEnvio.p_IdEmpresa = $scope.SesionIdEmpresa;
			datosEnvio.p_IdUsuario = $scope.SesionIdUsuario;
			datosEnvio.p_IdModulo = 'MRF';
			datosEnvio.p_UsaReglasNegocio = 'S';
			datosEnvio.P_DATOS = [{	
				 "IdPortafolio" 			: $scope.data.oConfiguracionPortafolio.idActual
				,"IdTipoOperacion"			: $scope.data.oConfiguracionTipoOperacion.idActual || ''
				,"IdMoneda"					: $scope.data.oConfiguracionMonedaLiquidacionOperacion.idActual || ''
				,"IdCondicionDeLiquidacion" : $scope.data.IdCondicionDeLiquidacion || ''
				,"FechaOperacion"			: util.dateSPdate($scope.data.kngFechaOperacion)
				,"FechaLiquidacion"			: $scope.data.fechaLiquidacionOperacion?util.dateSPdate($scope.data.fechaLiquidacionOperacion): util.dateSPdate($scope.data.kngFechaOperacion)
				,"MontoOperacion"			: $scope.data.kngMontoOperacionMoneda
			}];
			bcsServiciosMesa.ValidarDatosOperacion(datosEnvio,function(resp){
				console.log(resp);
				if (resp.P_OUT_ERRORESCONTADOR != 0 || resp.P_OUT_ADVERTENCIASCONTADOR != 0){
					bcsComunes.ventanaMsg({
								conflictos:util.parseServerResponse(resp)
							},$scope.winID);
				}
				if(callback) callback(resp.P_OUT_ERRORESCONTADOR <= 0);
			},function(resp){
				console.log(resp);
				bcsComunes.ventanaMsg({
							conflictos:util.parseServerResponse(resp)
						},$scope.winID);
				if(callback) callback(false);
			});
		}
		
	};
	var cargarOperacion = function(){
		Push('cargarOperacion');
		var filtros = [];
		
		//para la modificacion
		if (parametrosVentana.IdOperacion && parametrosVentana.IdOperacion > 0){
			console.log(parametrosVentana.IdOperacion);
			bcsComunes.ventanaOverlay($scope.winID, true);
			$scope.data.habilitaCabecera = false;
			$scope.data.OperacionExiste = true;
			buscaParidad = false;
			//$scope.data.oConfiguracionSocioNegocio.filtros.IdCliente = {"field": "IdCliente","operator": "eq", "value"   :  parametrosVentana.IdClienteCorredor};
			
			filtros = bcsServiciosGrillasMesaRF.validarFiltros({"SesionIdEmpresa" : $scope.SesionIdEmpresa
																,"IdOperacion":parametrosVentana.IdOperacion}
															,'OPERACION');
			
			dataSourceDetalleOperacion.query({filter : filtros}).then(function(){
				var datos=[];
				var data = dataSourceDetalleOperacion.data();
				$scope.data.kngSecuenciaOperacion;
				if (data.length > 0){
					//cargo datos de la cabecera					
			
					//cargo la mesa en la que estoy realizando la operacion
					$scope.data.oConfiguracionMesas.idActual = Number(data[0].IdMesaDestino);
					$scope.data.oConfiguracionMesas.idDefecto = Number(data[0].IdMesaDestino);
					$scope.data.esIntermesa = data[0].Intermesa === 'S';
					//datos cliente
					$scope.data.IdCliente = Number(data[0].IdCliente);
					$scope.data.oConfiguracionSocioNegocio.idActual = Number(data[0].IdSocioDeNegocio);
					$scope.data.oConfiguracionSocioNegocio.idDefecto = Number(data[0].IdSocioDeNegocio);
					$scope.data.oConfiguracionSocioNegocio.seleccionado= Number(data[0].IdSocioDeNegocio);
					$scope.data.IdPortafolio = Number(data[0].IdPortafolio);
					$scope.data.oConfiguracionPortafolio.idActual = Number(data[0].IdPortafolio) || 0;
					$scope.data.oConfiguracionPortafolio.idDefecto = Number(data[0].IdPortafolio) || 0;
					$scope.data.oConfiguracionPortafolio.seleccionado = {'IdCliente' : Number(data[0].IdCliente)};
					
					$scope.data.kngSecuenciaOperacion = Number(data[0].IdOperacion);
					$scope.data.kngFolioOperacion = Number(data[0].FolioOperacion);
					
					//tipoOperacion
					$scope.data.oConfiguracionTipoOperacion.idActual = data[0].IdTipoOperacion;
					$scope.data.oConfiguracionTipoOperacion.idDefecto = data[0].IdTipoOperacion;
					$scope.data.oConfiguracionTipoOperacion.seleccionado= {'Accion' : (data[0].IdTipoOperacion === 'COMPAC' || data[0].IdTipoOperacion === 'COMTER')?'COMPRA':'VENTA'};
					//$scope.data.oConfiguracionTipoOperacion.seleccionado.IdTipoOperacion= data[0].IdTipoOperacion;
					
					$scope.data.kngFechaOperacion = data[0].FechaOperacion;
					//$scope.data.oConfiguracionEstado.idDefecto = data[0].IdEstado;
					$scope.data.EstadoOperacion = data[0].NombreEstado;
					$scope.data.oConfiguracionTipoReajusteOperacion.idActual = data[0].IdTipoReajuste;
					$scope.data.oConfiguracionTipoReajusteOperacion.idDefecto = data[0].IdTipoReajuste;
					$scope.data.kngTasaOperacion = Number(data[0].Tasa);
					
					$scope.data.plazoOperacion = Number(data[0].PlazoEnDias);
					$scope.data.fechaVencimientoOperacion = data[0].FechaVencimiento;//util.dateDS(util.dateSPdate(data[0].FechaVencimiento),"FechaSistema","CALL"); 
					
					$scope.data.kngMontoOperacionMoneda = Number(data[0].MontoOperacionInicialMoneda);
					$scope.data.kngMontoValorFinalOperacionMoneda = Number(data[0].MontoOperacionFinalMoneda);
					
					$scope.data.kngMontoOperacion = Number(data[0].MontoOperacionInicial);
					$scope.data.kngMontoValorFinalOperacion = Number(data[0].MontoOperacionFinal);
					
					$scope.data.oConfiguracionMonedaLiquidacionOperacion.idActual = data[0].IdMonedaLiquidacion;
					$scope.data.oConfiguracionMonedaLiquidacionOperacion.idDefecto = data[0].IdMonedaLiquidacion;
					
					$scope.data.IdCondicionDeLiquidacionModif = data[0].IdCondicionLiquidacion;
					$scope.data.IdCondicionDeLiquidacion = data[0].IdCondicionLiquidacion;
					$scope.data.fechaLiquidacionOperacion  = util.dateDS(util.dateSPdate(data[0].FechaCondicionLiquidacion),"FechaSistema","CALL");
					
					$scope.data.NombreAgente = data[0].NombreEjecutivo;
					$scope.data.kngTasaAgente = Number(data[0].TasaAgente);
					$scope.data.NombreOperador = data[0].CodigoUsuario;
					$scope.data.IdEstadoOperacion = data[0].IdEstado;

					$scope.data.Comentarios = data[0].Glosa;
					
										
					$scope.data.oConfiguracionEmisor.idActual = data[0].IdEmisor;
					$scope.data.oConfiguracionEmisor.idDefecto = data[0].IdEmisor;
					//> Solo para la primera vez
					$scope.data.IdTipoOperacionCartera = data[0].IdTipoOperacion;
					//
					// $scope.data.buscarCartera();
					//
					
						
					
				}
				else {
					//no existe operacion
					bcsComunes.msg("error",$translate.instant('UI_ERROR'),$translate.instant("UI_OPERACION_INVALIDA"));
					bcsComunes.ventanaOverlay($scope.winID, false);
				}
			});
		}
		else {
			//cargo la mesa en la que estoy realizando la operacion
			$scope.data.oConfiguracionMesas.idActual = parametrosVentana.IdMesa;
			$scope.data.oConfiguracionMesas.idDefecto = parametrosVentana.IdMesa;
		}
		
	};
	
	$scope.data.guardarOperacionMesa = function(estModifica){
		Push('$scope.data.guardarOperacionMesa');
		var existeDetalle = false;
		buscaParidad = true;
		//validarOperacion(function(resp){
			//if (resp){
				if (estModifica){
					$scope.data.habilitaCabecera = true;
					deshabilitaIntermesa();
					$scope.data.IdCliente = undefined;
					$scope.data.IdPortafolio = undefined;
					$scope.data.guardarOperacion('MODIFICAR');
					//calcularMontoSimulacion();
					$scope.data.OperacionExiste = false;
				}
				else {
					
					if ($scope.data.kngFechaOperacion === $scope.data.fechaVencimientoOperacion || $scope.data.plazoOperacion === 0){
						bcsComunes.msg("warning",$translate.instant('UI_ADVERTENCIA'),$translate.instant('PLAZO_MAYOR_CERO'));
						return;
					}
					$scope.data.habilitaCabecera = true;
					$scope.data.guardarOperacion('CREAR');
				}
			//}
		//});
			
	};
	
	$scope.data.guardarOperacion = function(opcion){
		Push('$scope.data.guardarOperacion');
		var datos= new Object();
		if ($scope.data.kngFechaOperacion === $scope.data.fechaVencimientoOperacion || $scope.data.plazoOperacion === 0){
			bcsComunes.msg("warning",$translate.instant('UI_ADVERTENCIA'),$translate.instant('PLAZO_MAYOR_CERO'));
			return;
		}
		bcsComunes.ventanaOverlay($scope.winID,true);
		$scope.data.OperacionExiste = false;
		
		//if(opcion === 'CREAR') {
			//opcion = opcion === 'INICIO' ? 'GRABAR' : opcion;
			$scope.data.EnProcesoGrabar = true;
			datos = setData(opcion);
			
			var i;
			for (i = 0; i < datos.p_Operacion.length; i++) { 
				datos.p_Operacion[i].FechaOperacion = util.dateSPdate(datos.p_Operacion[0].FechaOperacion);
			}
				
			bcsServiciosMesa.CrearOperacionMesaRF(datos,success,ErrGrabaDetalle);
		//}
		//else {
		//	calcularMontoSimulacion();
		//}
		//Pop();
	};
	
	var Push = function(xProc){
		ident = ident + '   ';
		console.log(ident + xProc);
	}
	
	$scope.$watch('data.oConfiguracionTipoReajusteOperacion.idActual',function(newval,oldval){
		Push('$scope.$watch(data.oConfiguracionTipoReajusteOperacion.idActual)');
		
		if ( newval !== oldval && newval.length > 0 ) {
			$scope.data.oConfiguracionMontoOperacionMoneda.max = $scope.data.oConfiguracionTipoReajusteOperacion.idActual === 'CLP'?999999999999999:9999999999;
			$scope.data.IdDecimalTipoReajusteDisplay 	= (util.bcs_formato(0,'MES','Monto',$scope.data.oConfiguracionTipoReajusteOperacion.seleccionado.IdTipoReajuste?$scope.data.oConfiguracionTipoReajusteOperacion.seleccionado.IdTipoReajuste:$scope.data.IdMonedaMontoDisplay, '','')).cantidadDecimales;
			if (parametrosVentana.Accion !== 'MODIFICAR')
				$scope.data.kngMontoOperacionMoneda = 0;
			$scope.data.oConfiguracionMonedaLiquidacionOperacion.idDefecto = newval;
			$scope.data.oConfiguracionMonedaLiquidacionOperacion.idActual = newval;
		}
		
	});

	$scope.$watchGroup(['data.kngMontoOperacionMoneda','data.plazoOperacion','data.kngTasaOperacion','data.oConfiguracionTipoReajusteOperacion.idActual'],function(newval,oldval){
		Push('$scope.$watchGroup(data.kngMontoOperacionMoneda','data.plazoOperacion','data.kngTasaOperacion','data.oConfiguracionTipoReajusteOperacion.idActual)');
		calcularMontoOperacionMoneda();
		if (buscaParidad){
			calcularParidadMonto(false);
			buscaParidad = true;
		}
	});

	$scope.$watchGroup(['data.kngMontoOperacion','data.plazoOperacion','data.kngTasaOperacion'],function(newval,oldval){
		Push('$scope.$watchGroup(data.kngMontoOperacion','data.plazoOperacion','data.kngTasaOperacion)');		
		calcularMontoOperacion();
		//calcularMontoSimulacion();
		
	});
	
	$scope.$watch('data.oConfiguracionTipoOperacion.idActual',function(newval,oldval){
		Push('$scope.$watch(data.oConfiguracionTipoOperacion.idActual)');
		if (newval === oldval){
			return;
		}
		
		if ($scope.data.oConfiguracionTipoOperacion.idActual){
			enableCabeceraOperacion();
		}
		//obtenerTasaAgente();
		///validarOperacion();
		
	});
	
	$scope.$watch('data.oConfiguracionSocioNegocio.seleccionado',function(newval,oldval){
		Push('$scope.$watch(data.oConfiguracionSocioNegocio.seleccionado)');
		if (typeof $scope.data.oConfiguracionSocioNegocio.seleccionado !== 'undefined' && $scope.data.oConfiguracionSocioNegocio.seleccionado !== false)
		{
			$scope.data.NombreAgente = $scope.data.oConfiguracionSocioNegocio.seleccionado.NombreCompletoEjecutivo;
		}
		
		
	});
	
	$scope.$watch('data.oConfiguracionPortafolio.idActual',function(newval,oldval){
		Push('$scope.$watch(data.oConfiguracionPortafolio.idActual)');
		$scope.data.EsCustodia = false;
		
		console.log("Portafolio : " + $scope.data.oConfiguracionPortafolio.idActual);
		if ($scope.data.oConfiguracionPortafolio.idActual)
		{
			var filter = [
							{ "field"   : "IdPortafolio","operator": "eq", "value"   :  $scope.data.oConfiguracionPortafolio.idActual}
							//,{ "field"   : "OperaCustodia","operator": "eq", "value"   :  scope.idTipoOperacion}
			];
			dataSourcePreferenciaPortafolio.query({filter:filter}).then(function(){
				if (dataSourcePreferenciaPortafolio.data().length<1){
					$scope.data.EsCustodia = false;
				}
				else {
					var datos = dataSourcePreferenciaPortafolio.data();
					$.each(datos,function(key,value){
						$scope.data.BovedaDefaultPortafolio = value.IdBoveda;
						if (value.OperaCustodia === 'S') {
							$scope.data.EsCustodia = true;
						}
						else {
							$scope.data.EsCustodia = false;
						}
					}); 
				}
			});
			//validarOperacion();
		}
		
	});
	
	
	reset();
	cargarOperacion();
});