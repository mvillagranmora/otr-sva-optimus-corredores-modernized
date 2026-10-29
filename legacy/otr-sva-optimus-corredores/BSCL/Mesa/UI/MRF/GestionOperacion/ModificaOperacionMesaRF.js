angular.module('ModuloMesa').controller('ModificarOperacionMesaRFCtrl',
			function($scope,$translate,$timeout,$rootScope,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,bcsServiciosMesa
					,bcsServiciosGrillasMesaRF ,bcsServicioPlataforma)
{
	$scope.data = {};

	$scope.dataNew = {};
	$scope.data.IdMonedaMontoDisplay = $scope.SesionIdMonedaSistema;
	$scope.data.IdDecimalMontoDisplay = util.bcs_formato(0,'GEN','Monto',$scope.data.IdMonedaMontoDisplay, '','').cantidadDecimales;	
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	//$scope.data.plazoAnticipo = 0;
	var parametrosVentana = bcsComunes.ventanaParams($scope.winID);
	$scope.data.IdDecimalTipoReajusteDisplay 	= (util.bcs_formato(0,'GEN','Monto',parametrosVentana.Operacion.IdTipoReajuste,'','')).cantidadDecimales;
	$scope.data.IdDecimalTasaDisplay = (util.bcs_formato(0,'GEN','Tasa',parametrosVentana.Operacion.IdTipoReajuste,parametrosVentana.Operacion.IdTipoInstrumento,'')).cantidadDecimales;
	var buscaParidad = true;
	var dataSourceDetalleOperacion = bcsServiciosGrillasMesaRF.getDataSourceDetalleOperacionMesaRF();
	$scope.data.AccionVentana = parametrosVentana.Accion;
	
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
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},
		idDefecto : ''
	};
	
	$scope.data.oConfiguracionPortafolio={
			tipo : "combobox-portafolio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
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
				Accion : {"field": "Accion","operator": "eq", "value"   :  '' }
			},
			idDefecto : ''
	};
	
	$scope.data.oConfiguracionTipoReajusteOperacion={
		tipo : "combobox-tipo-reajuste",
		configuracion : {
			template: '<span >#: IdTipoReajuste # - #: Nombre #</span>'
		},
		idActual : parametrosVentana.Operacion.IdTipoReajuste,
		seleccionado : parametrosVentana.Operacion.IdTipoReajuste,
		filtros : {
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }		
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
			IdMoneda : { "field"   : "IdMoneda","operator": "eq", "value"   : "" }
		},
		idDefecto : ''
	};
	
	$scope.data.oConfiguracionMontoOperacionMoneda = {
			max: 0
	};
	
	// --------------------------------------------------
	// CONFIGURACION DE WATCHES
	// --------------------------------------------------


	$scope.$watch('data.oConfiguracionPortafolio.idActual', function (newval, oldval) {
		$scope.data.idFormaCustodiaPortafolio = 'CUS';
		var filtros = [];
		if (parametrosVentana.Operacion){
			filtros = bcsServiciosGrillasMesaRF.validarFiltros({"SesionIdEmpresa" : $scope.SesionIdEmpresa
																,"IdOperacion":parametrosVentana.Operacion.IdOperacion}
															,'OPERACION');
															
			 dataSourceDetalleOperacion.query({filter : filtros}).then(function(){
				var data = dataSourceDetalleOperacion.data();
				
				if (data.length > 0){
					$scope.data.idFormaCustodiaPortafolio = data[0].AfectaCustodia === 'S' ? 'CUS': 'NOM';	
				}
				
			});
		}

	});
	
	
	var success = function(respuesta)
	{	var operaciones = [];
		var id;
		//si vuelve ref id, se creo la asignacion
		
		if (respuesta.P_OUT_ERRORESCONTADOR === 0) 
		{		
			
			if (respuesta.P_OUT_OPERACION[0].Accion === 'GUARDAR'){
				id = bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_OPERACION_INGRESO_EXITO")+": " +$translate.instant("UI_FOLIO_OPERACION")+" " + respuesta.P_OUT_OPERACION[0].FolioOperacion,
							conflictos:util.parseServerResponse(respuesta)
						},$scope.winID);
			}
			else if(respuesta.P_OUT_OPERACION[0].Accion === 'CREAR'){
				id = bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_OPERACION_INGRESO_EXITO")+": " +$translate.instant("UI_FOLIO_OPERACION")+" " + respuesta.P_OUT_OPERACION[0].IdOperacion,
							conflictos:util.parseServerResponse(respuesta)
						},$scope.winID);
			}
			else {
				$.each(respuesta.P_OUT_OPERACION,function(key,value) {
				operaciones.push({id: value.IdOperacion , resultado: $translate.instant('UI_EXITO'), mensaje: $translate.instant('UI_OPERACION_ANTICIPADA_EXITO')});
				});
		
				id = bcsComunes.ventanaAbrir("ResumenOperacionesMasivas", { 
						tituloVentana : $translate.instant("UI_MESA_RENTA_FIJA"), 
						nombreEntidad : $translate.instant("UI_SECUENCIA"), 
						operaciones : operaciones   
					},false,false,$scope.winID);
			}
			bcsComunes.ventanaPromesa(id).then(function(result){
				reset();
				bcsComunes.ventanaOverlay($scope.winID, false);
				bcsComunes.ventanaCerrar($scope.winID);
			});			
		}
		else
		{					
			id = bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);			
			bcsComunes.ventanaPromesa(id).then(function(result){
				bcsComunes.ventanaOverlay($scope.winID, false);
				bcsComunes.ventanaCerrar($scope.winID);
			});			
		}
	};
	
	var error = function(respuesta)
	{				
		var id = bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);
		bcsComunes.ventanaPromesa(id).then(function(result){
			bcsComunes.ventanaOverlay($scope.winID, false);
		});
		
	};
	
	var resetCabecera = function (){
		$scope.data.oConfiguracionSocioNegocio.idActual = false;
		$scope.data.oConfiguracionPortafolio.idActual = false;
		$scope.data.oConfiguracionTipoOperacion.idActual = false;
		$scope.data.oConfiguracionTipoReajusteOperacion.idActual = false;
		$scope.data.oConfiguracionMonedaLiquidacionOperacion.idActual = false;
		$scope.data.EsCustodia = false;
		$scope.data.SesionIdEmpresa = $scope.SesionIdEmpresa;
		$scope.data.kngMontoOperacion = '';
		$scope.data.kngTasaOperacion = '';
		$scope.data.kngMontoOperacionMoneda = '';
		$scope.data.kngSecuenciaOperacion = '';
		$scope.data.kngFolioOperacion = '';
		$scope.data.plazoOperacion = '';		
		$scope.data.IdCondicionDeLiquidacion = '';
		$scope.data.EstadoOperacion = '';
		$scope.data.oConfiguracionTipoReajusteOperacion.idDefecto = parametrosVentana.Operacion.IdTipoReajuste;
		$scope.data.oConfiguracionMonedaLiquidacionOperacion.idDefecto = $scope.data.IdMonedaMontoDisplay;
		$scope.data.fechaPlazoAnticipo='';
		$scope.data.kngFechaOperacion='';
	};
	
	
	var reset = function (){
		 resetCabecera();
	};


	var cargarOperacion = function () {
		var filtros = [];
		if (parametrosVentana.Operacion){
			//console.log(parametrosVentana.IdClienteCorredor);
			//console.log(parametrosVentana.IdPortafolioCorredor);
			console.log(parametrosVentana.Operacion);
			bcsComunes.ventanaOverlay($scope.winID, true);
			
			filtros = bcsServiciosGrillasMesaRF.validarFiltros({"SesionIdEmpresa" : $scope.SesionIdEmpresa
																,"IdOperacion":parametrosVentana.Operacion.IdOperacion}
															,'OPERACION');
			
															
			 dataSourceDetalleOperacion.query({filter : filtros}).then(function(){
				var data = dataSourceDetalleOperacion.data();
				var fechaHabil = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
				var plazoTemp = Number(data[0].PlazoEnDias)-1;
				fechaHabil = util.date(fechaHabil).startOf('day').add(plazoTemp,'d').toDate();
				if (data.length > 0){
					if(util.esFechaHabil( $scope.$root.SesionIdPaisSistema, fechaHabil)){
						for( i=0; i<10; i++ ) {
							fechaHabil = util.date(fechaHabil).startOf('day').add(1,'d').toDate();
							plazoTemp++;
					 	    if ( util.esFechaHabil( $scope.$root.SesionIdPaisSistema, fechaHabil )) break;
						}
					}
					
					
					
					$scope.data.oConfiguracionMesas.idActual = Number(data[0].IdMesaDestino);
					$scope.data.oConfiguracionMesas.idDefecto = Number(data[0].IdMesaDestino);
					$scope.data.esIntermesa = data[0].Intermesa === 'S';
					//datos cliente
					$scope.data.IdCliente = Number(data[0].IdCliente);
					$scope.data.oConfiguracionSocioNegocio.idActual	= Number(data[0].IdSocioDeNegocio);
					$scope.data.oConfiguracionSocioNegocio.NombreCompleto = data[0].NombreCompleto;
					$scope.data.IdPortafolio = Number(data[0].IdPortafolio);
					$scope.data.oConfiguracionPortafolio.idActual = Number(data[0].IdPortafolio) || 0;
					$scope.data.oConfiguracionPortafolio.idDefecto = Number(data[0].IdPortafolio) || 0;
					$scope.data.oConfiguracionPortafolio.seleccionado = {'IdCliente' : Number(data[0].IdCliente)};
					$scope.data.oConfiguracionPortafolio.CodigoPropositoPortafolio = data[0].CodigoPropositoPortafolio;
					$scope.data.kngSecuenciaOperacion = Number(data[0].IdOperacion);
					$scope.data.kngFolioOperacion = Number(data[0].FolioOperacion);
					
					//tipoOperacion
					$scope.data.oConfiguracionTipoOperacion.idActual = data[0].IdTipoOperacion;
					$scope.data.oConfiguracionTipoOperacion.idDefecto = data[0].IdTipoOperacion;
					$scope.data.oConfiguracionTipoOperacion.seleccionado= {'Accion' : (data[0].IdTipoOperacion === 'COMPAC' || data[0].IdTipoOperacion === 'COMTER')?'COMPRA':'VENTA'};
					
					$scope.data.kngFechaOperacion = parametrosVentana.Accion === 'ANTICIPAR'? util.dateDS(data[0].FechaOperacion,"FechaHoraSistema","ODATA") : util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
		
					//Glosa Anticipo
					$scope.data.Comentarios = parametrosVentana.Accion === 'ANTICIPAR'? "ANTICIPO PACTO MESA RF/IF" : data[0].Glosa;

					$scope.data.EstadoOperacion = data[0].NombreEstado;
					$scope.data.oConfiguracionTipoReajusteOperacion.idActual = data[0].IdTipoReajuste;
					$scope.data.oConfiguracionTipoReajusteOperacion.idDefecto = data[0].IdTipoReajuste;
					$scope.data.kngTasaOperacion = Number(data[0].Tasa);
					$scope.data.plazoOperacion = parametrosVentana.Accion === 'ANTICIPAR'? Number(data[0].PlazoEnDias) : plazoTemp;
					$scope.data.fechaVencimientoOperacion = parametrosVentana.Accion === 'ANTICIPAR'? data[0].FechaVencimiento : fechaHabil; 					
					$scope.data.kngMontoOperacionMoneda = parametrosVentana.Accion === 'ANTICIPAR'? Number(data[0].MontoOperacionInicialMoneda) : Number(data[0].MontoOperacionFinalMoneda);
					$scope.data.kngMontoOperacion = Number(data[0].MontoOperacionInicial);															
					$scope.data.oConfiguracionMonedaLiquidacionOperacion.idActual = data[0].IdMonedaLiquidacion;
					$scope.data.oConfiguracionMonedaLiquidacionOperacion.idDefecto = data[0].IdMonedaLiquidacion;					
					$scope.data.IdCondicionDeLiquidacion = data[0].IdCondicionLiquidacion;
					$scope.data.fechaLiquidacionOperacion  = util.dateDS(util.dateSPdate(data[0].FechaCondicionLiquidacion),"FechaSistema","CALL");
					$scope.data.fechaPlazoAnticipo = $scope.SesionFechaSistema;
					// Datos corredor
					$scope.data.IdClienteCorredor = Number(data[0].IdClienteCorredor);
					$scope.data.IdPortafolioCorredor = Number(data[0].IdPortafolioCorredor);

					//carga de valores finales y plazoAnticipo
					calculoPlazo();

				}
				else {
					//no existe operacion
					bcsComunes.msg("error","Error",$translate.instant("UI_OPERACION_INVALIDA"));
					bcsComunes.ventanaOverlay($scope.winID, false);
				}
			});
		}
		else {
			//cargo la mesa en la que estoy realizando la operacion
			$scope.data.oConfiguracionMesas.idActual = parametrosVentana.IdMesa;
			$scope.data.oConfiguracionMesas.idDefecto = parametrosVentana.IdMesa;
		}
		bcsComunes.ventanaAutoHeight($scope.winID);



	//	return defrred.promise;
	}
	
	/*
	 * CALCULAR PLAZO 
	 * */
	
	var calculoPlazo = function (){
		var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo : 'PLA',
				p_UsaReglasNegocio : 'S',
				p_IdCondicionDeLiquidacion : 'T0',
				p_FechaDesde : util.dateSPdate( $scope.data.kngFechaOperacion),
				p_FechaHasta : $scope.SesionFechaSistema,
				p_DiasHabiles : 'N',
				p_IdPais :  $scope.SesionIdPaisSistema
			};
		
			bcsServicioPlataforma.Plazo(datos,function(result)
			{
				if(result.P_OUT_PLAZODIAS!=0){
					$scope.data.plazoAnticipo = Number(result.P_OUT_PLAZODIAS);
					calcularMontoOperacionMoneda();
					calcularMontoOperacion();
				}
			}
			,function(result)
			{
				console.log("ERROR: " + JSON.stringify(result));
			});
		
	}
	
	var calcularParidadMonto = function(montoLocal,callback){
		var data = Object();
		if (!$scope.data.oConfiguracionTipoReajusteOperacion.idActual){// || Number($scope.data.kngMontoOperacionMoneda) <= 0){
			$scope.data.kngMontoOperacion = 0;
			if(callback) callback();
		}
		else {
			data.p_IdEmpresa =  $scope.SesionIdEmpresa;
			data.p_IdTrabajo =  1315;
			data.p_CalculosEntrada = [{
				"IdTrabajo"			: data.p_IdTrabajo
				,"IdInterno"		: 1
				,"Fecha"			: util.dateSPdate($scope.data.kngFechaOperacion)
				,"IdMonedaOrigen" 	: !montoLocal?$scope.data.oConfiguracionTipoReajusteOperacion.idActual || $scope.data.IdMonedaMontoDisplay:$scope.data.IdMonedaMontoDisplay
				,"IdMonedaDestino" 	: !montoLocal?$scope.data.IdMonedaMontoDisplay:$scope.data.oConfiguracionTipoReajusteOperacion.idActual || $scope.data.IdMonedaMontoDisplay
				,"ValorCambioLocalModificado" : 0
				,"MontoOrigen"		: !montoLocal?$scope.data.kngMontoOperacionMoneda || 0:$scope.data.kngMontoOperacion || 0
				,"IdUso"			: 'MESA'
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
	
	var setData = function(opcion){
		var datosParam = new Object();
		var carteras = [];
		var fechaVen = '';

		fechaVen = new Date($scope.data.fechaVencimientoOperacion || $scope.SesionFechaSistema || '0001-01-01');
		
		datosParam.p_IdEmpresa = $scope.SesionIdEmpresa;
		datosParam.p_IdUsuario = $scope.SesionIdUsuario;
    	datosParam.p_IdModulo = 'MRF';
    	datosParam.p_UsaReglasNegocio = (opcion === 'CERRAR'?'S':'N');
		datosParam.p_Operacion = [{
							"Accion"					: opcion
							,"IdInternoOperacion"		: 1
							,"IdOperacion"				: Number(0)
							,"FolioOperacion"			: Number(0)
							,"IdCliente" 				: Number($scope.data.oConfiguracionPortafolio.seleccionado.IdCliente)//Number(opcion==='PENDIENTE'?$scope.data.IdCliente:$scope.data.oConfiguracionPortafolio.seleccionado.IdCliente)
							,"IdPortafolio"				: Number($scope.data.IdPortafolio)//Number($scope.data.oConfiguracionPortafolio.seleccionado.IdPortafolio)//Number(opcion==='PENDIENTE'?$scope.data.IdPortafolio:$scope.data.oConfiguracionPortafolio.seleccionado.IdPortafolio)
							,"IdClienteCorredor"		: Number($scope.data.IdClienteCorredor)
							,"IdPortafolioCorredor"		: Number($scope.data.IdPortafolioCorredor)
							,"Intermesa"				: $scope.data.esIntermesa?'S':'N'
							,"IdMesaOrigen"				: Number(parametrosVentana.Operacion.IdMesaOrigen)
							,"IdMesaDestino"			: Number($scope.data.oConfiguracionMesas.seleccionado.IdMesa)
							,"AfectaCustodia"			: $scope.data.formaCustodiaSeleccionada.IdFormaCustodiaOrden === 'CUS' ?'S':'N'
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
							,"FechaOperacion"			: $scope.SesionFechaSistema
							,"FechaVencimiento" 		: util.dateSPdate(fechaVen)
							,"IdCondicionLiquidacion"	: $scope.data.IdCondicionDeLiquidacion
						 	,"FechaLiquidacion" 		: util.dateSPdate($scope.data.fechaLiquidacionOperacion || '0001-01-01')
						 	,"IdAgente"					: Number($scope.data.oConfiguracionSocioNegocio.seleccionado.IdPersonaEjecutivo || 0)
						 	,"IdOperador"				: $scope.SesionIdUsuario
						 	,"IdEstado"					: 'PEN'//$scope.data.oConfiguracionEstado.seleccionado || ''
						 	,"Glosa"					: $scope.data.Comentarios || ''
						 	,"IdTipoCanalIngreso"		: $scope.SesionTipoCanalIngreso
						 	,"Glosa"					: $scope.data.Comentarios
						 	
		}];
		datosParam.p_DetalleOperacion = [];
		
		
		return datosParam;
	};
	
	/*
	 * REVISAR EL CALCULO Y ENVIARLO AL ANTICIPO
	 * */
	var calcularMontoOperacionMoneda = function(){
		var monto = 0;

		
		if ($scope.data.kngMontoOperacionMoneda && $scope.data.plazoAnticipo && $scope.data.kngTasaOperacion){
			if ($scope.data.oConfiguracionTipoReajusteOperacion.seleccionado.IdTipoReajuste != $scope.data.IdMonedaMontoDisplay){
				monto = Number(bcsComunes.calcularMontoaPlazo(Number($scope.data.kngMontoOperacionMoneda),Number($scope.data.plazoAnticipo),Number($scope.data.kngTasaOperacion),360)).toFixed($scope.data.IdDecimalTipoReajusteDisplay);
			}
			else {
				monto = Number(bcsComunes.calcularMontoPlazo(Number($scope.data.kngMontoOperacionMoneda),Number($scope.data.plazoAnticipo),Number($scope.data.kngTasaOperacion))).toFixed($scope.data.IdDecimalTipoReajusteDisplay);
			}
		
			$scope.data.kngMontoValorFinalOperacionMoneda  = Number(monto);
			
		}
		else {
			$scope.data.kngMontoValorFinalOperacionMoneda  = $scope.data.kngMontoOperacionMoneda;
		}
		
	};
	var calcularMontoOperacionMonedaRenovacion = function(){
		var monto = 0;
		if ($scope.data.kngMontoOperacionMoneda && $scope.data.plazoOperacion && $scope.data.kngTasaOperacion){
			if ($scope.data.oConfiguracionTipoReajusteOperacion.idActual != $scope.data.IdMonedaMontoDisplay){
				monto = Number(bcsComunes.calcularMontoaPlazo(Number($scope.data.kngMontoOperacionMoneda),Number($scope.data.plazoOperacion),Number($scope.data.kngTasaOperacion),360)).toFixed($scope.data.IdDecimalTipoReajusteDisplay);
			}
			else {
				monto = Number(bcsComunes.calcularMontoPlazo(Number($scope.data.kngMontoOperacionMoneda),Number($scope.data.plazoOperacion),Number($scope.data.kngTasaOperacion))).toFixed($scope.data.IdDecimalTipoReajusteDisplay);
			}
		
			$scope.data.kngMontoValorFinalOperacionMoneda  = Number(monto);
			
		}
		else {
			$scope.data.kngMontoValorFinalOperacionMoneda  = $scope.data.kngMontoOperacionMoneda;
		}
		
	};
	var calcularMontoOperacionRenovacion = function(){
		var monto = 0;
		if ($scope.data.kngMontoOperacion && $scope.data.plazoOperacion && $scope.data.kngTasaOperacion){
			if ($scope.data.oConfiguracionTipoReajusteOperacion.idActual != $scope.data.IdMonedaMontoDisplay){
				monto = Number(bcsComunes.calcularMontoaPlazo(Number($scope.data.kngMontoOperacion),Number($scope.data.plazoOperacion),Number($scope.data.kngTasaOperacion),360)).toFixed($scope.data.IdDecimalMontoDisplay);
			}
			else {
				monto = Number(bcsComunes.calcularMontoPlazo(Number($scope.data.kngMontoOperacion),Number($scope.data.plazoOperacion),Number($scope.data.kngTasaOperacion))).toFixed($scope.data.IdDecimalMontoDisplay);
			}
	
			$scope.data.kngMontoValorFinalOperacion = Number(monto);
			
		}
		else {
			$scope.data.kngMontoValorFinalOperacion = $scope.data.kngMontoOperacion;
		}
		calcularMontoSimulacionRenovacion();
	};
	var calcularMontoOperacion = function(){
		var monto = 0;
		
		
		if ($scope.data.kngMontoOperacion && $scope.data.plazoAnticipo && $scope.data.kngTasaOperacion){
			if ($scope.data.oConfiguracionTipoReajusteOperacion.seleccionado.IdTipoReajuste != $scope.data.IdMonedaMontoDisplay){
				monto = Number(bcsComunes.calcularMontoaPlazo(Number($scope.data.kngMontoOperacion),Number($scope.data.plazoAnticipo),Number($scope.data.kngTasaOperacion),360)).toFixed($scope.data.IdDecimalMontoDisplay);
			}
			else {
				monto = Number(bcsComunes.calcularMontoPlazo(Number($scope.data.kngMontoOperacion),Number($scope.data.plazoAnticipo),Number($scope.data.kngTasaOperacion))).toFixed($scope.data.IdDecimalMontoDisplay);
			}
	
			$scope.data.kngMontoValorFinalOperacion = Number(monto);
			
		}
		else {
			$scope.data.kngMontoValorFinalOperacion = $scope.data.kngMontoOperacion;
		}
	
	};

	var calcularParidadMontoRenovacion = function(montoLocal,callback){
	
		
		var IdMonedaOrigen = "CLP";
		var IdMonedaDestino = "CLP";
		var data = Object();
		
		if (!$scope.data.oConfiguracionTipoReajusteOperacion.idActual){//} || Number($scope.data.kngMontoOperacionMoneda) <= 0){
			$scope.data.MontoCLP_Org = 0;
			$scope.data.MontoMON_Org = 0;
			$scope.data.kngMontoOperacion = 0;
			$scope.data.kngMontoOperacionMoneda = 0;				
			if(callback) callback();
		}
		else {
			
			IdMonedaOrigen = !montoLocal?$scope.data.oConfiguracionTipoReajusteOperacion.idActual || $scope.data.IdMonedaMontoDisplay:$scope.data.IdMonedaMontoDisplay;
			IdMonedaDestino = !montoLocal?$scope.data.IdMonedaMontoDisplay:$scope.data.oConfiguracionTipoReajusteOperacion.idActual || $scope.data.IdMonedaMontoDisplay;
		
			data.p_IdEmpresa =  $scope.SesionIdEmpresa;
			data.p_IdTrabajo =  1315;
			data.p_CalculosEntrada = [{
				"IdTrabajo"			: data.p_IdTrabajo
				,"IdInterno"		: 1
				,"Fecha"			: util.dateSPdate($scope.data.kngFechaOperacion)
				,"IdMonedaOrigen" 	: IdMonedaOrigen 
				,"IdMonedaDestino" 	: IdMonedaDestino
				,"ValorCambioLocalModificado" : 0
				,"MontoOrigen"		: !montoLocal?$scope.data.kngMontoOperacionMoneda || 0:$scope.data.kngMontoOperacion || 0
				,"IdUso"			: 'MESA'
			}];
				
			bcsServiciosMesa.ObtenerParidadTipoCambio(data,function(value){
				if (value.p_out_CalculosSalida.length > 0){
					
					var montoLoc = value.p_out_CalculosSalida[0].MontoLocal;
					var montoDes = value.p_out_CalculosSalida[0].MontoDestino <= 0? value.p_out_CalculosSalida[0].MontoOrigen:value.p_out_CalculosSalida[0].MontoDestino;
					var montoOri = value.p_out_CalculosSalida[0].MontoOrigen;
					
					
					if (value.p_out_CalculosSalida[0].IdMonedaOrigen === value.p_out_CalculosSalida[0].IdMonedaDestino){
						$scope.data.kngMontoOperacion 		= value.p_out_CalculosSalida[0].MontoLocal;
						$scope.data.kngMontoOperacionMoneda = value.p_out_CalculosSalida[0].MontoLocal;
						$scope.data.MontoMON_Org = $scope.data.kngMontoOperacionMoneda;
						$scope.data.MontoCLP_Org = $scope.data.kngMontoOperacion;
					}
					else {					
						if ( Number($scope.data.kngMontoOperacionMoneda) !== Number($scope.data.MontoMON_Org) || Number($scope.data.kngMontoOperacionMoneda) != Number(montoLoc) 
							|| (Number($scope.data.kngMontoOperacionMoneda) === Number(montoLoc) && Number($scope.data.kngMontoOperacion) != Number(montoDes))) {
							$scope.data.kngMontoOperacion = value.p_out_CalculosSalida[0].MontoLocal;	
							$scope.data.MontoMON_Org = $scope.data.kngMontoOperacionMoneda;
							$scope.data.MontoCLP_Org = $scope.data.kngMontoOperacion;
						}
						
						if ( Number($scope.data.kngMontoOperacion) !== Number($scope.data.MontoCLP_Org) || Number($scope.data.kngMontoOperacion) != Number(montoDes)) {
							$scope.data.kngMontoValorFinalOperacion = value.p_out_CalculosSalida[0].MontoDestino <= 0? value.p_out_CalculosSalida[0].MontoOrigen:value.p_out_CalculosSalida[0].MontoDestino;
							$scope.data.MontoMON_Org = $scope.data.kngMontoOperacionMoneda;
							$scope.data.MontoCLP_Org = $scope.data.kngMontoOperacion;						
						}
					}					
				}
				else {
					$scope.data.kngMontoOperacion = 0;
					$scope.data.MontoCLP_Org = 0;
				}
				if(callback) callback();
			});
		}
		
	};


	var calcularMontoSimulacionRenovacion = function(){
		
		var val = 0;
		var val2 = 0;
		var d = $scope.data.gridApiCarteraInstrumento?$scope.data.gridApiCarteraInstrumento.dataSource.data():[];
		$.each(d,function(key,value) {
			val+= value.Monto;// || 0;
			val2+= value.MontoLiquidacionOperacion;// || 0;
		});
		$scope.data.kngMontoAsignado = val;
		$scope.data.kngMontoAsignadoMonedaLiquidacion = val2;
		$scope.data.kngMontoDiferencia = Number($scope.data.kngMontoOperacion) - Number(val);	
		$scope.data.kngMontoFinal = Number($scope.data.kngMontoAsignado)+Number($scope.data.kngMontoDiferencia)
		
	};


	
	$scope.$watchGroup(['data.kngMontoOperacionMoneda','data.plazoOperacion','data.kngTasaOperacion'],function(newval,oldval){
		if($scope.data.AccionVentana==='RENOVAR'){
			//buscaParidad = true;
			calcularMontoOperacionMonedaRenovacion();
				if (buscaParidad){
					calcularParidadMontoRenovacion(false);
					buscaParidad = true;
						
				}

		}
		
		
	});
	$scope.$watchGroup(['data.kngMontoOperacion','data.plazoOperacion','data.kngTasaOperacion'],function(newval,oldval){
		if($scope.data.AccionVentana!=='RENOVAR'){
			calcularMontoOperacion();
			calcularMontoOperacionMoneda();	
		}else{
			calcularMontoOperacionRenovacion();
			calcularMontoSimulacionRenovacion();
		}
		
		
	});
	
	$scope.$watch('data.oConfiguracionTipoReajusteOperacion.idActual',function(newval,oldval){
		
		$scope.data.oConfiguracionMontoOperacionMoneda.max = $scope.data.oConfiguracionTipoReajusteOperacion.idActual === 'CLP'?999999999999999:9999999999;
		//$scope.data.kngMontoOperacionMoneda = 0;
		$scope.data.IdDecimalTipoReajusteDisplay 	= (util.bcs_formato(0,'GEN','Monto',$scope.data.oConfiguracionTipoReajusteOperacion.seleccionado.IdTipoReajuste	,'','')).cantidadDecimales;
		$scope.data.IdDecimalTasaDisplay = (util.bcs_formato(0,'GEN','Tasa',parametrosVentana.Operacion.IdTipoReajuste,parametrosVentana.Operacion.IdTipoInstrumento,'')).cantidadDecimales;
	});	
	
	
	$scope.data.guardarOperacion = function(opcion){
		var datos= new Object();
		//buscaParidad = true;
		if (($scope.data.oConfiguracionTipoOperacion.idActual === 'COMPAC' || $scope.data.oConfiguracionTipoOperacion.idActual==='VENPAC') && 
				( $scope.data.kngFechaOperacion === $scope.data.fechaVencimientoOperacion || $scope.data.plazoOperacion === 0)){
			bcsComunes.msg("warning",$translate.instant('UI_ADVERTENCIA'),$translate.instant('PLAZO_MAYOR_CERO'));
			return;
		}
		bcsComunes.ventanaOverlay($scope.winID,true);
		datos = setData(opcion);
		
		bcsServiciosMesa.CrearOperacionMesaRF(datos,success,error);
	};
	
	$scope.data.anticiparOperacion = function(){
		var datos= new Object();
		//buscaParidad = true;
		bcsComunes.ventanaOverlay($scope.winID, true);
		
		datos.p_IdEmpresa = $scope.SesionIdEmpresa;
		datos.p_IdUsuario = $scope.SesionIdUsuario;
    	datos.p_IdModulo = 'MRF';
    	datos.p_UsaReglasNegocio = 'S';
    	datos.p_Operacion = [{
			"IdInternoOperacion"		: 1
			,"IdOperacion"				: Number($scope.data.kngSecuenciaOperacion)
			,"FolioOperacion"			: Number($scope.data.kngFolioOperacion)
			,"IdCliente" 				: Number($scope.data.oConfiguracionPortafolio.seleccionado.IdCliente)//Number(opcion==='PENDIENTE'?$scope.data.IdCliente:$scope.data.oConfiguracionPortafolio.seleccionado.IdCliente)
			,"IdPortafolio"				: parametrosVentana.Accion === 'ANTICIPAR'? $scope.data.IdPortafolio :Number($scope.data.oConfiguracionPortafolio.seleccionado.IdPortafolio)//Number(opcion==='PENDIENTE'?$scope.data.IdPortafolio:$scope.data.oConfiguracionPortafolio.seleccionado.IdPortafolio)
			,"IdClienteCorredor"		: Number(parametrosVentana.Operacion.IdClienteCorredor)
			,"IdPortafolioCorredor"		: Number(parametrosVentana.Operacion.IdPortafolioCorredor)
			,"Intermesa"				: $scope.data.esIntermesa?'S':'N'
			,"IdMesaOrigen"				: Number(parametrosVentana.Operacion.IdMesaOrigen)
			,"IdMesaDestino"			: Number($scope.data.oConfiguracionMesas.seleccionado.IdMesa)
			,"AfectaCustodia"			: $scope.data.esCustodia?'CUS':'ENV'
			,"IdTipoProducto"			: $scope.data.oConfiguracionTipoOperacion.seleccionado.IdTipoProducto
			,"IdTipoOperacion"			: $scope.data.oConfiguracionTipoOperacion.seleccionado.IdTipoOperacion
			,"IdTipoReajuste"			: $scope.data.oConfiguracionTipoReajusteOperacion.seleccionado.IdTipoReajuste
			,"Monto"					: Number($scope.data.kngMontoOperacion || 0)
			,"MontoMoneda"				: Number($scope.data.kngMontoOperacionMoneda || 0) //cambiar al monto en moneda local
			,"IdMoneda"					: $scope.data.IdMonedaMontoDisplay
	   		,"IdMonedaLiquidacion"		: $scope.data.oConfiguracionMonedaLiquidacionOperacion.seleccionado.IdMonedaLiquidacion
			,"Tasa"						: Number($scope.data.kngTasaOperacion || 0)
			,"ValorFinal"				: Number($scope.data.kngMontoValorFinalOperacion || 0)
			,"ValorFinalMoneda"			: Number($scope.data.kngMontoValorFinalOperacionMoneda || 0)
			,"IdCondicionLiquidacion"	: $scope.data.IdCondicionDeLiquidacion
		 	,"FechaLiquidacion" 		: util.dateSPdate($scope.data.fechaLiquidacionOperacion || '0001-01-01')
		 	,"IdEstadoSiguiente"		: 'ANT'//$scope.data.oConfiguracionEstado.seleccionado || ''
		 	,"PlazoAnticipo"			: $scope.data.plazoAnticipo
		 	,"Glosa"					: 'ANTICIPO PACTO MESA RF/IF' 
		 	
		}];
		
		
		
		bcsServiciosMesa.AnticiparOperacionesMesaRF(datos,success,error);
	
	};

	
	$scope.data.limpiarFiltros = function (){
		reset();
	};
	
	reset();
	cargarOperacion();
});