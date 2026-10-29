angular.module('ModuloFacturacion').controller('IngresarMovimientoFacturarOtrosCtrl',
		function($scope,$translate,$modal,bcsComunes,bcsServiciosFacturacion,bcsComunes,bcsComprobanteFacturacionGrid,
				 bcsServicioPlataforma,bcsServiciosPlataformaMercado)
{
		
	var idVentana = bcsComunes.ventanaWinId ($scope);
	bcsComunes.ventanaAutoOverlay(idVentana,false,3000);
		
	$scope.data	= {};
	$scope.dataTab	= {};
	var datosEntrada = bcsComunes.ventanaParams(idVentana);
	
	$scope.data.accion = datosEntrada.accion; //Se necesita en este formato para HTML.
	$scope.data.condicionLiquidacion = datosEntrada.registro.IdCondicionLiquidacion;
	$scope.data.tipoOperacion = datosEntrada.registro.IdTipoOperacion;
	
	
	//Seteo de variables de sistema
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdMonedaLocal = $scope.SesionIdMonedaSistema;
	$scope.data.IdModulo	= 'FAC';
	
	$scope.data.btnActivos  = true;   //Activa botones

	$scope.data.oComprobanteAutomatico = false;	
	$scope.data.oComprobanteAutomaticoDisabled = false;
	$scope.data.oIndActTesManual = true;
	$scope.data.oIndActTesDisabled = true;
	$scope.data.oFechaMovimientoComprobante = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
	$scope.data.oFechaLiquidacion = datosEntrada.registro.FechaLiquidacion || util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
	$scope.data.oMontoComprobanteLiquidacion = 0;
	$scope.data.oMontoComprobanteNetoLiquidacion = 0;
	$scope.data.oMontoCobroLiquidacion = 0;
	$scope.data.oMontoLiquidacion = 0;
	$scope.data.oIVA = 0;
	var valorIGV = 0;

	$scope.data.montoOptions	= {format: 'c'+(util.bcs_formato(0,'GEN','Monto',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
		
	// obtenemos valor del IVA
	var datosLlamadaA = {
		p_IdEmpresa 	: $scope.SesionIdEmpresa,
		p_IdUsuario 	: $scope.SesionIdUsuario,
		p_IdModulo 		: 'CUS',
		p_IdCobro		: 'IVA'

	};

	bcsServiciosFacturacion.ConsultarIVA(datosLlamadaA,function(result) {
		if (result.P_OUT_VALORCOBRO > 0){
			$scope.data.oIVA = result.P_OUT_VALORCOBRO;
			valorIGV = result.P_OUT_VALORCOBRO;
			$scope.data.oMontoCobroLiquidacion = datosEntrada.registro.EsExento ==='S' && $scope.data.accion === 'NCM' ? 0 : Number($scope.data.oMontoComprobanteNetoLiquidacion * (valorIGV / 100));
			$scope.data.oMontoComprobanteNetoLiquidacion = $scope.data.oMontoComprobanteNetoLiquidacion;
            $scope.data.oMontoTotalLiquidacion = Number($scope.data.oMontoComprobanteNetoLiquidacion) + Number($scope.data.oMontoCobroLiquidacion);
		}
	});

	const obtenerIdTipoOperacion = function() {
		if("NCM" === $scope.data.accion && "COMOTC" === datosEntrada.registro.IdTipoOperacion){
			return "ANCOTC";
		} 

		if("NCM" === $scope.data.accion && "VENOTC" === datosEntrada.registro.IdTipoOperacion){
			return "ANVOTC";
		} 

		if("NCA" === $scope.data.accion && "NEO" === $scope.data.oTipoComprobante.seleccionado.IdTipoComprobante && "COMOTC" === $scope.data.oTipoOperacion.seleccionado.IdTipoOperacion){
			return "ANCOTC";
		}

		if("NCA" === $scope.data.accion && "NEO" === $scope.data.oTipoComprobante.seleccionado.IdTipoComprobante && "VENOTC" === $scope.data.oTipoOperacion.seleccionado.IdTipoOperacion){
			return "ANVOTC";
		}

		return $scope.data.oTipoOperacion.seleccionado.IdTipoOperacion || datosEntrada.registro.IdTipoOperacion;
	}

	const obtenerIdCondicionLiquidacion = function() {
		if("NCM" === $scope.data.accion && datosEntrada.registro.IdCondicionLiquidacion){
			return datosEntrada.registro.IdCondicionLiquidacion;
		} 

		if("NCA" === $scope.data.accion && $scope.data.oCondicionLiquidacion.IdCondicionDeLiquidacion){
			return $scope.data.oCondicionLiquidacion.IdCondicionDeLiquidacion;
		}

		return "T0";
	}


bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise('FACTURACIONAUTOMATICAOTROSCONCEPTOS', $scope.SesionIdEmpresa, 'FACTURACION')
			.then(
				function (parametroUsaFechaLiquidacion) {
					$scope.data.oComprobanteAutomatico = parametroUsaFechaLiquidacion.Valor === 'S';
				}
			);




	//Disabled campos
	$scope.data.activaroFechaMovimientoComprobante = true;
	$scope.data.activaroMonedaSubyacente = false;
	$scope.data.activaroEmisor = false;
	$scope.data.activaroInstrumentoSubyacente = false;
	$scope.data.activaroInstrumentoRelacionado = false;
	$scope.data.activaroTipoReajuste = false;
	$scope.data.activaroConcepto = true;
	$scope.data.activaroSecCmbte = true;
	$scope.data.activaroSecCmbteRef = true;
	$scope.data.activaroCorteMinimo = true;
	$scope.data.activaroInstrumentoSubyacente = false;
	$scope.data.activaroInstrumentoRelacionado = false;
	$scope.data.activaroMontoAPlazo = true;	
	$scope.data.activaroFolioPlazo = true;	
	$scope.data.activaroValorCaptacion = true;	
	$scope.data.activaroDiasPlazo = false;
	$scope.data.activaroFechaVencimiento = false;
	$scope.data.activaroFechaEmision = true;	
	$scope.data.activaroTasaEmision = true;	
	$scope.data.activaroValorRescate = true;
	$scope.data.activaroMonedaCobro = true;
	$scope.data.activaroIndPrima = true;
	$scope.data.activaroIndAPlazo = true;
	
	
	//COMBOBOX
	$scope.data.oClientePortafolio={
			tipo : "combobox-cliente-portafolio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdEstado : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},
	};
		
	$scope.data.oTipoOperacion={
			tipo : "combobox-tipo-operacion", 
			configuracion : {},
			filtros : {				
				EsOtrosConceptos :	{"field"   : "EsOtrosConceptos","operator": "eq","value"   : "S"},
				EsMovimientoComprobante : {"field"   : "EsMovimientoComprobante","operator": "eq","value"   : "S"}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : false,
			idPostFiltro  : false,
			
	};
	
	$scope.data.oSucursal={
			tipo : "combobox-sucursal",
			configuracion : {},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.data.oEjecutivo={
			tipo : "combobox-empleado",
			configuracion : {},
			filtros : {
				IdSucursal : {"field" : "IdSucursal","operator": "eq","value"   :0 },
				IdEstado : {"field"   : "IdEstado","operator": "eq","value"   : 'ACT' }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.data.oConcepto={
			tipo : "combobox-concepto-facturacion",
			configuracion : {},
			filtros : {
				IdEstado 		 	: {"field"   : "IdEstado", 		  "operator" : "eq", "value" :"VIG" },
				EsOtrosConceptos 	: {"field"   : "EsOtrosConceptos", "operator" : "eq", "value" :"S" },
				ConfiguradoEnModulo : {"field"   : "ConfiguradoEnModulo","operator": "eq", "value" : "S" }				
//				IdTipoOperacion : {field   : "IdTipoOperacion", operator : "eq", value :'' },
			
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	

	
	$scope.data.oCondicionLiquidacion={
			tipo : "combobox-condicion-liquidacion",
			configuracion : {}, 
			filtros : { 	
				Estado : {field : "EstadoTipoOperacionYCondicionDeLiquidacion", operator : "eq", value : "ACT"},
				IdTipoOperacion : {field : "IdTipoOperacion", operator : "eq", value : ""}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.data.oBolsa={
		tipo : "combobox-bolsa",
		configuracion : {},
		filtros : {
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
			EstadoBolsaTipoInstrumento : { "field"   : "EstadoBolsaTipoInstrumento","operator": "eq", "value"   : "ACT" }
		}
	};
	

	
	$scope.data.oMonedaLiquidacion={
		tipo : "combobox-moneda-tesoreria",
		configuracion : {},
		filtros : {
					IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : 'ACT' }
				  },
		idActual : false,
		seleccionado : false,
	};
	
	$scope.data.oMonedaLiquidacion.idDefecto = $scope.SesionIdMonedaSistema;
	$scope.activarMoneda = false;

	$scope.data.oTipoComprobante={
			tipo : "combobox-tipo-comprobante",
			configuracion : {},
			filtros : {
			    IdEstadoFacturacion :	{field : "IdEstadoFacturacion", operator : "eq", value : "ACT"}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.data.oTipoReferencia={
		tipo: "combobox-tipo-referencia",
		configuracion: {},
		filtros: {},
		idActual: false,
		seleccionado: false,
		idDefecto: ''
	}
	
	$scope.data.filtrosPreferenciaPago = {
			IdMedioDePago 	: { "field"   : "IdMedioDePago",operator: "neq", "value"   :  "CIN" },
			IdPortafolio 	: { "field"   : "IdPortafolio","operator": "eq", "value"   :  "" },
			IdMoneda 		: { "field"   : "IdMoneda","operator": "eq", "value"   :  "" },
			IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   : "" }
	};
	
	var defGrid = {
		 		          type : "array",
				          definition : {                                         
				        	             data	: [],
				        	             schema	: {model: {
														fields : {
															NombreCobro						: { type : "string"},
															DescripcionCobroClasificacion	: { type : "string"},
															NombreFormaCobro				: { type : "string"},			        
															NombreMoneda					: { type : "string"},
															Monto							: { type : "number"},
															MontoMovimientoComprobanteCobro : { type : "number"},
															NombreMonedaLiquidacion 		: { type : "string"},
															MontoComprobante    			: { type : "number"},					
															Porcentaje		    			: { type : "string"},
															EsRecalculable      			: { type : "string"},
														}
													}
												}
    			                       }
		};

	var dsGridFDePago = new bcs.DataSource(defGrid)	

	$scope.confBasicaIngresarMovimientoComprobanteFormaDePago = { 
		dataSource			: dsGridFDePago,
		columns				: [
								 { field: "NombreCobro",      				title: "UI_TIPO_COBRO",  			width:"170px", 	align: "left",  sortable : false, filterable: false },
								 { field: "DescripcionCobroClasificacion",	title: "UI_CLASIFICACION_COBRO",	width:"100px", 	align: "left",  sortable : false, filterable: false },
								 { field: "NombreFormaCobro", 				title: "UI_FORMA_COBRO", 			width:"80px", 	align: "left",  sortable : false, filterable: false },
								 { field: "NombreMoneda", 					title: "UI_MONEDA_COBRO", 			width:"120px",	align: "left",  sortable : false, filterable: false },
								 { field: "Monto",            				title: "UI_MONTO_COBRO", 			width:"100px",  align: "right", sortable : false, filterable: false, template : "<label> #: (util.bcs_formato(Monto,'GEN','Monto','CLP','','')).valor #</label>"},
								 { field: "MontoMovimientoComprobanteCobro",title: "UI_MONTO_LOCAL",			width:"100px",  align: "right", sortable : false, filterable: false, template : "<label> #: (util.bcs_formato(MontoMovimientoComprobanteCobro,'GEN','Monto','CLP','','')).valor #</label>"},
								 { field: "NombreMonedaLiquidacion", 		title: "UI_MONEDA_LIQUIDACION",		width:"120px", 	align: "left",  sortable : false, filterable: false },
								 { field: "MontoComprobante", 				title: "UI_MONTO_COMPROBANTE", 		width:"100px",  align: "right", sortable : false, filterable: false, template : "<label> #: (util.bcs_formato(MontoLiquidacion,'GEN','Monto','CLP','','')).valor #</label>"},
								 { field: "Porcentaje",       				title: "UI_PORCENTAJE",				width:"100px",  align: "right", sortable : false, filterable: false },	
								 { field: "EsRecalculable",   				title: "UI_RECALCULABLE", 			width:"50px", 	align: "left",  sortable : false, filterable: false }
							],
		sortable			: true,
		pageable			: true,
		filterable			: true,
		userSettings        : true,
		height				: 150,
		selectable 			: "multiple row"
		
	};
	
	
	//**************************************************************
	//Asigna valores a Ui cuando viene desde nota de credito manual
	//**************************************************************
	
	if ($scope.data.accion === 'NCM')
      {
		//Buscar Información del Comprobante
		var definicionDataComprobante = function() {
			return {
				serverFiltering 	: true, // Filtrado de datos en el servidor.
				serverPaging 		: true, // Paginado de los datos en el servidor.
				serverSorting 		: true, // Ordenamiento de los datos en el servidor.
				sort 				: { field: "IdComprobante", dir: "desc" }, // Criterios de Ordenamiento.
				filter				: { field: "IdComprobante", operator: "eq", value: Number(datosEntrada.registro.IdComprobante)},
				transport 			: { read: {
												url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1240",
												dataType: "json"
											}
									},
				schema				: bcsComprobanteFacturacionGrid.modeloComprobantes()
			};
		};
			
		var consultarDataComprobante = new bcs.DataSource({
			type 		: "odata",
			definition : definicionDataComprobante()
		});
		
		consultarDataComprobante.fetch()
			.then(function() {
				$scope.data.oReferenciaManual = consultarDataComprobante.data()[0]?.Referencia || '';
				$scope.data.oTipoReferenciaManual = consultarDataComprobante.data()[0]?.TipoReferencia || '';
			})

	     //Socio de negocio
   	     $scope.data.oClientePortafolioManual = datosEntrada.registro.NombreLargoCliente;  	
         //Portafolio
   	     $scope.data.oPortafolioManual = datosEntrada.registro.NombrePortafolio;
   	     //FechaMovimiento
   	     $scope.data.oFechaMovimientoComprobanteManual = datosEntrada.registro.FechaComprobante; 
    	 //Sucursal
   	     $scope.data.oSucursalManual = datosEntrada.registro.NombreSucursalEmpleadoEjecutivoPortafolio;
   	     //Empleado Digitador
   	     $scope.data.oEjecutivoManual = datosEntrada.registro.NombreEmpleadoEjecutivoPortafolio;
	     //Tipo Operacion
	     $scope.data.oTipoOperacionManual = datosEntrada.registro.NombreTipoOperacion;   	   	   	
     	 //Concepto
   	     $scope.data.oConceptoManual = datosEntrada.registro.NombreConcepto;
   	     //Bolsa
   	     $scope.data.oBolsaManual = datosEntrada.registro.NombreBolsa;
   	     //Condicion de liquidacion
   	     $scope.data.oCondicionLiquidacionManual	= datosEntrada.registro.IdCondicionLiquidacion;
   	     //Moneda
   	     $scope.data.oMonedaLiquidacionManual = datosEntrada.registro.NombreMonedaLiquidacion;
   	     //Tipo comprobante
   	     if(datosEntrada.registro.IdTipoComprobante === 'POL')
   	       {	 
   	          $scope.data.oTipoComprobanteManual = 'NOTA DE CREDITO';
   	       }else $scope.data.oTipoComprobanteManual = 'NOTA DE CREDITO ELECTRONICA OTROS CONCEPTOS';
   	                     
   	                 
   	     //Secuencia agrupacion   	
   	     $scope.data.oSecuenciaAgrupacionManual = 0; 
   	     //Monto
		if ($scope.SesionLocale === 'PE') {
			$scope.data.oMontoComprobanteLiquidacion = datosEntrada.registro.ComisionSABDisponible ? datosEntrada.registro.ComisionSABDisponible : 0;
		} else {
			$scope.data.oMontoComprobanteLiquidacion = datosEntrada.registro.TotalComisLiq ? datosEntrada.registro.TotalComisLiq : 0;
		}
   	     
   	     //Comprobante asociado
   	     $scope.data.polizaAsociadaManual = datosEntrada.registro.IdComprobante? datosEntrada.registro.IdComprobante: 0;
   	     //Comprobante Individual
   	     $scope.data.oIndCmbteIndivManual = true; 
   	     //Afecto a impuesto
   	     $scope.data.afectoImpuesto = true;   	     
   	     $scope.data.oIndImptoIncl = false;   	    	    	 
   	     
   	     //Comentario
   	     $scope.data.oComentarioManual = datosEntrada.registro.Glosa;
      };
	

    //***********************************************************
	// Asigna campos segun $watch 
	//***********************************************************	
	$scope.$watch('data.oTipoComprobante.seleccionado',function(newVal,oldVal)
	  {
		 if (!newVal) return;	   	 
			
			if ($scope.data.oTipoComprobante.seleccionado.AfectoImpuesto == 'N'){
			   $scope.data.ActivaIva = false;
			}else{
					$scope.data.ActivaIva = true;
				 }
	  }); 
	
	
	
	//*******************************************
	// Limpiar
	//*******************************************		
	$scope.data.limpiar = function()	 
     {	     
		
		$scope.data.oClientePortafolio.idActual = -1;		
    	$scope.data.oClientePortafolio.idActual = 0;

    	$scope.data.oTipoOperacion.idActual = 0;
    	$scope.data.oSucursal.idActual = 0;
    	$scope.data.oEjecutivo.idActual = 0;
    	$scope.data.oConcepto.idActual = 0;

    	$scope.data.oCondicionLiquidacion.idActual = 0;
    	$scope.data.oFechaTransaccion = '';
    	$scope.data.oFechaLiquidacion = $scope.data.oFechaMovimientoComprobante;
    	$scope.data.oBolsa.idActual = 0;

    	$scope.data.oMonedaLiquidacion.idDefecto = $scope.SesionIdMonedaSistema;
		$scope.activarMoneda = false;
    	$scope.data.oFolioTransaccion = 0;
    	$scope.data.oOrden = 0;
    	$scope.data.oTipoCambio = 0;    	    	    
    	$scope.data.oMontoComprobanteLiquidacion = 0;
    	$scope.data.oMontoComprobanteNetoLiquidacion = 0;
    	$scope.data.oMontoCobroLiquidacion = 0;
    	$scope.data.oMontoTotalLiquidacion = 0;
    	
    	$scope.data.oPrecio = 0;
    	$scope.data.oCantidad = 0;
    	$scope.data.oMontoLocal = 0;
    	$scope.data.oMontoAPlazo = 0;
    	$scope.data.oFolioPlazo = 0;
    	$scope.data.oTasa = 0;
    	$scope.data.oValorCaptacion = 0;
    	$scope.data.oDiasPlazo = 0;
    	$scope.data.oTasaEmision = 0;
    	$scope.data.oValorRescate = 0;
    	$scope.data.oCorteMinimo = 0;
		$scope.data.oReferencia = '';
		$scope.data.oTipoReferencia.idActual=0;
    	$scope.data.oComentario = '';
    	$scope.data.oTipoComprobante.idActual = 0;
    	$scope.data.oSecuenciaAgrupacion = '';
    	$scope.data.oIndCmbteIndiv = false;
    	$scope.data.oIndFueraRueda = false;
    	$scope.data.oIndOrdenDirecta = false;
    	$scope.data.oIndCuentaPropia = false;
    	$scope.data.oIndVentaCorta = false;
    	$scope.data.oIndActCus = false;
    	$scope.data.oIndActCusRel = false;    	
    	$scope.data.oIndActTes = false;
    	
    	$scope.data.oMontoCobro = 0;
    	$scope.data.oMontoCobroLiquidacion = 0;
    	$scope.data.oPorcentajeCobro = 0;
    	
	 }; 
	 
	//*******************************************
	// Ingresar movimiento comprobante
	//*******************************************		
	$scope.data.ingresarMovimientoComprobante = function()	
    {    	
		
		$scope.data.accionDisabled = false;
		$scope.data.limpiarDisabled = false;
		let montoComprobanteNetoLiquidacion = 	isNaN($scope.data.oMontoComprobanteNetoLiquidacion) 
												? parseFloat($scope.data.oMontoComprobanteNetoLiquidacion.replace(',','.')) 
												: $scope.data.oMontoComprobanteNetoLiquidacion;

		var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdTrabajo : 0,
				p_CalculosEntrada : [{
					IdTrabajo : 0,
					IdInterno : 0,
					Fecha : $scope.SesionFechaSistema,
					IdMonedaOrigen : $scope.data.oMonedaLiquidacion.idActual?$scope.data.oMonedaLiquidacion.idActual:datosEntrada.registro.IdMonedaLiquidacion,
					IdMonedaDestino : $scope.SesionIdMonedaSistema,
					ValorCambioLocalModificado : 0,
					MontoOrigen : montoComprobanteNetoLiquidacion,
					IdUso : 'ORDENES'
				}]
			};
		
		if ($scope.data.afectoImpuesto) {
			
			//SI ESTA AFECTO
			datos.p_CalculosEntrada.push(
			{
				IdTrabajo : 0,
				IdInterno : 1,
				Fecha : $scope.SesionFechaSistema,
				IdMonedaOrigen : $scope.data.oMonedaLiquidacion.idActual?$scope.data.oMonedaLiquidacion.idActual:datosEntrada.registro.IdMonedaLiquidacion,
				IdMonedaDestino : $scope.SesionIdMonedaSistema,
				ValorCambioLocalModificado : 0,
				MontoOrigen : montoComprobanteNetoLiquidacion*valorIGV/100,
				IdUso : 'ORDENES'
			}
			);
			
		}

		bcsComunes.ventanaOverlay(idVentana, true);



		if ( $scope.data.oComentario && $scope.data.oComentario.length > 100 ) {
				bcsComunes.ventanaOverlay(idVentana, false);
				bcsComunes.ventanaMsgErr($translate.instant('UI_COMENTARIO_MAX_CARACTERES'),$scope.idVentana);
			return;
		}


		var promise = bcsServiciosPlataformaMercado.CalcularParidadYTipoCambio(datos);
		promise.then(function(result){
			bcsComunes.ventanaOverlay(idVentana, false);
			if (result.p_out_CalculosSalida[0].Resultado === 'RECHAZO') {
				bcsComunes.msg("error","UI_ERROR","NO_SE_OBTUVO_PARIDAD");
			  }else { 
				
				        var v_MvtoCmbteAGenerar = [];
				        var TipComp
				        
				        if ($scope.data.accion === 'NCM')
				          {
				        	if (datosEntrada.registro.IdTipoComprobante === 'POL')
	             	          {	 
			        		     TipComp = 'NCR';
	             	          }else TipComp = 'NEO';
				          }else
				        	  {
				        	    TipComp = $scope.data.oTipoComprobante.seleccionado.IdTipoComprobante;
				        	  };
				        
				        
				        if ($scope.SesionLocale === 'PE')
				        	{
				        	    MonedaLiq = 'PEN';
				        	    CompInd = $scope.data.oIndCmbteIndivManual? 'S': 'N';
				        	}else {
				        		     MonedaLiq = $scope.SesionIdMonedaSistema;
				        		     CompInd = $scope.data.oIndCmbteIndiv? 'S': 'N';     
				        	      }
             	        
             	        const fechaSistema = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
             	        if (!$scope.data.oFechaLiquidacion || $scope.data.oFechaLiquidacion < fechaSistema) {
							$scope.data.oFechaLiquidacion = fechaSistema;
						}
             	                     
				        v_MvtoCmbteAGenerar.push({
					                                "NumeroMvtoCmbte": 1,
					                                "IdAgente" : $scope.data.oEjecutivo.seleccionado.IdEmpleado || 0, //$scope.data.oEjecutivo.seleccionado.IdEmpleadoEjecutivo,
					                                "IdCliente" : $scope.data.oClientePortafolio.seleccionado.IdCliente?$scope.data.oClientePortafolio.seleccionado.IdCliente:datosEntrada.registro.IdCliente || 0,
					                                "IdConcepto" : $scope.data.oConcepto.seleccionado.IdConcepto?$scope.data.oConcepto.seleccionado.IdConcepto:datosEntrada.registro.IdConcepto || '',
					                                "IdEmpresa" : $scope.SesionIdEmpresa,
					                                "IdPortafolio" : $scope.data.oClientePortafolio.seleccionado.IdPortafolio?$scope.data.oClientePortafolio.seleccionado.IdPortafolio:datosEntrada.registro.IdPortafolio || 0,
					                                "IdEmpleadoDigitador" : $scope.data.oEjecutivo.seleccionado.IdEmpleado?$scope.data.oEjecutivo.seleccionado.IdEmpleado: datosEntrada.registro.IdEmpleadoDigitador || 0,
					                                "IdSucursal" : $scope.data.oSucursal.seleccionado.IdSucursal?$scope.data.oSucursal.seleccionado.IdSucursal:datosEntrada.registro.IdSucursal || 0,
					                                "IdInstrumento" : datosEntrada.registro.IdInstrumento,
					                                "IdInstrumentoRelacionado" : 0,
					                                "IdInstrumentoSubyacente" : 0,
					                                "IdTipoProducto" : datosEntrada.registro.CodigoTipoProducto? datosEntrada.registro.CodigoTipoProducto: datosEntrada.registro.CodigoTipoProducto || '',
					                                "IdMoneda" : $scope.data.oMonedaLiquidacion.seleccionado.IdMoneda? $scope.data.oMonedaLiquidacion.seleccionado.IdMoneda: datosEntrada.registro.IdMoneda || MonedaLiq,
					                                "IdMonedaSubyacente" : '',
					                                "IdMonedaLiquidacion" : MonedaLiq,
					                                "IdTipoComprobante" : TipComp,
					                                "IdTipoOperacion" : obtenerIdTipoOperacion(),
					                                "IdBoveda" : '',
					                                "IdCorredoraContraparte" : 0,
					                                "IdCondicionLiquidacion" : obtenerIdCondicionLiquidacion(),
					                                "IdBolsa" : $scope.data.oBolsa.seleccionado.IdBolsa? $scope.data.oBolsa.seleccionado.IdBolsa: datosEntrada.registro.IdBolsa || '',
					                                "IdTipoReajuste" : $scope.data.oMonedaLiquidacion.seleccionado.IdMoneda || 'CLP',
					                                "IdTipoConvenio" : 'SCV',
					                                "IdFormaCustodia" : '',
					                                "IdEmisor" : datosEntrada.registro.IdEmisor,
					                                "IdOrden" : 0,
					                                "IdAsignacion" : 0,
					                                "IdAsignacionRef" : 0,
					                                "IdTipoCanalIngreso" : 'AGE',
					                                "ModPrefFact" : 'MODPREFNORM',
					                                "SecuenciaAgrupacion" : $scope.data.oSecuenciaAgrupacion || '',
					                                "FechaLiquidacion" : util.dateSPdate($scope.data.oFechaLiquidacion),
					                                "FechaOrden" : '0001-01-01',
					                                "IdTransaccion" : 0, //Va cero porque el Id es interno del modulo orden
					                                "IdComprobanteReferencia" : $scope.data.polizaAsociada? $scope.data.polizaAsociada: datosEntrada.registro.IdComprobante,
					                                "FolioTransaccion" : 0,
					                                "FechaTransaccion" : '0001-01-01',
					                                "FechaVencimiento" : '0001-01-01',
					                                "PrecioAPlazo" : 0,
					                                "MontoAPlazo" : 0,
					                                "FechaParidad" : '0001-01-01',
					                                "ValorParidad" : 0,
					                                "FechaTipoCambio" : '0001-01-01',
					                                "ValorTipoCambio" : 0,
					                                "ValorCaptacionMonedaOrigen" : 0,
					                                "Tasa" : 0,
					                                "Cantidad" : 1,
					                                "Precio" : 0,
					                                "Monto" : util.truncarNumero(montoComprobanteNetoLiquidacion, 2),
					                                "MontoMovimientoComprobante" : result.p_out_CalculosSalida[0].MontoDestino,
					                                "MontoLiquidacion" : util.truncarNumero(result.p_out_CalculosSalida[0].MontoDestino, 2),
					                                "MontoDescuento" : 0,
					                                "MontoInteres" : 0,
													"Referencia": $scope.data.accion === 'NCM'? $scope.data.oReferenciaManual: $scope.data.oReferencia || '' ,
													"TipoReferencia": $scope.data.accion === 'NCM'? $scope.data.oTipoReferenciaManual: $scope.data.oTipoReferencia.seleccionado.TipoReferencia || '' ,
					                                "Glosa"	: $scope.data.oComentario? $scope.data.oComentario || '' :$scope.data.oComentarioManual || '',
					                                "ValorRescate" : 0,
					                                "TasaAgente" : 0,
					                                "Spread" : 0,
					                                "DiasPlazo" : 0,
					                                "FechaMovimientoComprobante" : util.dateSPdate($scope.data.oFechaMovimientoComprobante),
					                                "NombreAgente" : $scope.data.oEjecutivo.seleccionado.NombreCompleto || '',
					                                "NombreSucursal" : $scope.data.oSucursal.seleccionado.NombreSucursal || '',
					                                "FolioCartera" : 0,
					                                "FechaEmision" : $scope.data.oFechaEmision?util.dateSPdate($scope.data.oFechaEmision) : '0001-01-01',
					                                "TasaEmision" : 0,
					                                "EsComprobIndividual" : CompInd,
					                                "EsCuentaPropia" : 'N',
					                                "EsOrdenDirecta" : 'N',
					                                "EsVentaCorta" : 'N',
					                                "EsPrecioPrima" : 'N',
					                                "EsEnRueda" : 'N',
					                                "EsRuteo" : 'N',
					                                "EsAPlazo" : 'N',
					                                "EsFungible" : 'N',
					                                "ActualizarCustodia" : 'N',
					                                "ActualizarCuentaInversion"	: 'S',
					                                "ActualizarTesoreria" : $scope.data.oIndActTes ? 'S' : 'N',
					                                "ActualizarCustodiaRelacionado" : 'N',
					                                "CrearComprobanteAutomatico" : $scope.data.oComprobanteAutomatico? 'S': 'N',
					                                "IdUsuario"	: $scope.SesionIdUsuario,
													"Contrapartida"	: $scope.data.oConcepto.seleccionado.Contrapartida === "S" ? 'S' : 'N',
													"IdTipoComprobanteReferencia" :  $scope.data.accion === 'NCM'? datosEntrada.registro.CodigoTipoComprobante : '' || ''
				                                 });
				
						
				
				var v_MvtoCmbteCobroAGenerar = [];		
				
				//Tabla Cobro
				if ($scope.SesionLocale === 'CL') {
					var ObjMvtoCobro = {
						  "NumeroMvtoCmbte" 				: 1, 
						  "IdCobro"         				: $scope.data.afectoImpuesto ? 'IVA':'', 
						  "IdMonedaCobro"   				: $scope.data.oMonedaLiquidacion.seleccionado.IdMoneda? $scope.data.oMonedaLiquidacion.seleccionado.IdMoneda: datosEntrada.registro.IdMoneda || MonedaLiq,
						  "IdMonedaLiquidacionCobro" 		: MonedaLiq, 
						  "MontoCobro"      				: Number($scope.data.afectoImpuesto ? ((montoComprobanteNetoLiquidacion * $scope.data.oIVA )/100): $scope.data.oMontoCobro || 0),
						  "MontoMovimientoComprobanteCobro" : Number($scope.data.afectoImpuesto ? ((result.p_out_CalculosSalida[0].MontoDestino * $scope.data.oIVA )/100): $scope.data.oMontoCobro || 0),
						  "MontoLiquidacionCobro"    		: Number($scope.data.afectoImpuesto ? ((result.p_out_CalculosSalida[0].MontoDestino * $scope.data.oIVA )/100): $scope.data.oMontoCobro || 0),
						  "Porcentaje"      				: $scope.data.afectoImpuesto ? $scope.data.oIVA : 0
					};
					v_MvtoCmbteCobroAGenerar.push(ObjMvtoCobro);
				}

				if ($scope.SesionLocale == 'PE' && $scope.data.afectoImpuesto) {
					var ObjMvtoCobro = {
							  "NumeroMvtoCmbte" 				: 1, 
							  "IdCobro"         				: 'IGV_OC', 
							  "IdMonedaCobro"   				: $scope.data.oMonedaLiquidacion.seleccionado.IdMoneda? $scope.data.oMonedaLiquidacion.seleccionado.IdMoneda: datosEntrada.registro.IdMoneda || MonedaLiq,
							  "IdMonedaLiquidacionCobro" 		: $scope.data.oMonedaLiquidacion.seleccionado.IdMoneda? $scope.data.oMonedaLiquidacion.seleccionado.IdMoneda: datosEntrada.registro.IdMoneda || MonedaLiq, 
							  "MontoCobro"      				: $scope.data.oMontoComprobanteLiquidacion * valorIGV / 100,
							  "MontoMovimientoComprobanteCobro" : result.p_out_CalculosSalida[1].MontoDestino,
							  "MontoLiquidacionCobro"    		: $scope.data.oMontoComprobanteLiquidacion * valorIGV / 100,
							  "Porcentaje"      				: valorIGV || 0
						};
						v_MvtoCmbteCobroAGenerar.push(ObjMvtoCobro);
				}
				
				
				//Tabla Forma de Pago
				var v_MvtoCmbteForPagoAGenerar = [];
				
				if ($scope.data.oIndActTes) {
					var ObjMvtoFormaPago = {
							  "NumeroMvtoCmbte" 				: 1, 
							  "NumeroMvtoCmbteFormaDePago"      : 1,
							  "IdMedioDePago"					: $scope.data.formaDePagoSeleccionada.IdMedioDePago,
							  "NumeroSerie"   					: 0,
							  "NumeroDocumento"					: 0,
							  "IdCuentaBancaria" 				: $scope.data.formaDePagoSeleccionada.IdCuentaBancaria || 0,
							  "IdTipoCuentaBancaria" 			: $scope.data.formaDePagoSeleccionada.IdTipoCuentaBancaria || '',
							  "NumeroCuentaBancaria" 			: $scope.data.formaDePagoSeleccionada.NumeroCuentaBancaria || '',
							  "IdTarjetaBancaria" 				: $scope.data.formaDePagoSeleccionada.IdTarjetaBancaria || 0,
							  "IdTipoTarjetaBancaria" 			: $scope.data.formaDePagoSeleccionada.IdTipoTarjetaBancaria || '',
							  "NumeroTarjetaBancaria" 			: $scope.data.formaDePagoSeleccionada.NumeroTarjetaBancaria || '',
							  "FechaVctoTarjetaBancaria" 		: $scope.data.formaDePagoSeleccionada.FechaVctoTarjetaBancaria || '0001-01-01',
							  "IdModalidadDePago" 				: $scope.data.formaDePagoSeleccionada.IdModalidadDePago || '',
							  "IdBanco" 						: $scope.data.formaDePagoSeleccionada.IdBanco || 0,
							  "IdMoneda" 						: $scope.data.formaDePagoSeleccionada.IdMoneda || '',
							  "FechaDocumentoPago" 				: '0001-01-01',
							  "MontoLiquidacionFormaDePago" 	: $scope.data.oMontoComprobanteLiquidacion
						};
					v_MvtoCmbteForPagoAGenerar.push(ObjMvtoFormaPago);
				}

				//VARIABLES DEL SERVICIO
				var datos = {};
				datos.p_IdEmpresa                       = $scope.SesionIdEmpresa;
				datos.p_IdUsuario                       = $scope.SesionIdUsuario;
				datos.p_IdModulo                        = 'FAC';
				datos.p_simularOperacion                = 'N';
				datos.p_UsaReglasNegocio                = 'S';
				datos.p_EsIngresoAutomatico             = 'S';				
				datos.p_Movimientos 					= v_MvtoCmbteAGenerar
				datos.p_MovimientosFormaDePago       	= v_MvtoCmbteForPagoAGenerar;
				datos.p_MovimientosCobros         		= v_MvtoCmbteCobroAGenerar;
	
				bcsComunes.ventanaOverlay(idVentana, true);
				bcsServiciosFacturacion.CrearMovimientoComprobante(datos,function(respuesta){
					bcsComunes.ventanaOverlay(idVentana, false);

					var id = bcsComunes.ventanaMsg({
							conflictos:util.parseServerResponse(respuesta)
						},idVentana);
					$scope.data.btnActivos  = true;
					bcsComunes.ventanaPromesa(id).then(function(respuestaPromesa){
						if (respuesta.P_OUT_ERRORESCONTADOR === 0)
							bcsComunes.ventanaCerrar(idVentana);
					})

				},function(respuesta){
					bcsComunes.ventanaOverlay(idVentana, false);
					bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(respuesta)
					},idVentana);
					$scope.data.btnActivos  = true;     //Activa botones
				});
			}
		},function(result){
			bcsComunes.ventanaOverlay(idVentana, false);
			bcsComunes.msg("error","UI_ERROR","NO_SE_OBTUVO_PARIDAD");
		});
					
    };	
    
    
    $scope.$watch('data.oTipoComprobante.seleccionado',function(newVal,oldVal)
   	 {
    	 if (!newVal) return;
    	 
    	 if (($scope.data.oTipoComprobante.seleccionado.IdTipoComprobante === 'FOE' || $scope.data.oTipoComprobante.seleccionado.IdTipoComprobante === 'BOE'))
    	 {
    	 	$scope.data.oIndCmbteIndiv = true;
    	 	$scope.data.afectoImpuesto = true;
    	 	$scope.data.oIndActTes = false;
    	 	
    	 }
    	 else
    	 {
    	 	$scope.data.oIndCmbteIndiv = false;
    	 	$scope.data.afectoImpuesto = false;
    	 };  	
    	 
    	 
    	 if (($scope.data.oTipoComprobante.seleccionado.IdTipoComprobante === 'FOE' || $scope.data.oTipoComprobante.seleccionado.IdTipoComprobante === 'BOE') && $scope.data.oConcepto.seleccionado.IdConcepto === 'COBCUS')
    		 {
    		    $scope.data.oTipoOperacion.idActual = 'COMOTC';
    		 }
   	 });
    
            
    $scope.$watch('data.oTipoOperacion.seleccionado',function(newVal,oldVal)
     {
    	if (!newVal) return;
    	 	 
    	if (($scope.data.oTipoComprobante.seleccionado.IdTipoComprobante === 'FOE' || $scope.data.oTipoComprobante.seleccionado.IdTipoComprobante === 'BOE'))
    	  {
    	     $scope.data.oIndCmbteIndiv = true;
    	     $scope.data.afectoImpuesto = true;
    	     $scope.data.oIndActTes = false;
    	  }
    	else
    	  {
    	     $scope.data.oIndCmbteIndiv = false;
    	     $scope.data.afectoImpuesto = false;
    	  };
     });
    
    
    $scope.$watch('data.oConcepto.seleccionado',function(newVal,oldVal)
    	   	 {
    	    	 if (!newVal) return;
    	    	 
    	    	 if (($scope.data.oTipoComprobante.seleccionado.IdTipoComprobante === 'FOE' || $scope.data.oTipoComprobante.seleccionado.IdTipoComprobante === 'BOE') && $scope.data.oConcepto.seleccionado.IdConcepto === 'COBCUS')
    	    	 {
    	    		 $scope.data.oTipoOperacion.idActual = 'COMOTC';
    	    	 	
    	    	 };  			
    	   	 });
    
    
   $scope.$watch('data.oIndImptoIncl',function(newVal,oldVal)
   	  {
    	if($scope.data.oIndImptoIncl && $scope.data.afectoImpuesto)
	    {        	
	       $scope.data.oMontoComprobanteNetoLiquidacion = ($scope.data.oMontoComprobanteLiquidacion / (valorIGV / 100 + 1));
	       $scope.data.oMontoCobroLiquidacion = Number($scope.data.oMontoComprobanteNetoLiquidacion * (valorIGV/100));
	       $scope.data.oMontoTotalLiquidacion = Number($scope.data.oMontoComprobanteNetoLiquidacion) + Number($scope.data.oMontoCobroLiquidacion);
	    }else {
	      	     if($scope.data.oIndImptoIncl && !$scope.data.afectoImpuesto)
	      	        {
	      	    	   $scope.data.oMontoComprobanteNetoLiquidacion = $scope.data.oMontoComprobanteLiquidacion;
	      	           $scope.data.oMontoCobroLiquidacion = 0;
	      	       	   $scope.data.oMontoTotalLiquidacion = Number($scope.data.oMontoComprobanteNetoLiquidacion) + Number($scope.data.oMontoCobroLiquidacion);
	      	    	}else {
	      	    	        if(!$scope.data.oIndImptoIncl && $scope.data.afectoImpuesto)
	      	    	          {
	      	    	     	     $scope.data.oMontoComprobanteNetoLiquidacion = $scope.data.oMontoComprobanteLiquidacion;
	     	    	             $scope.data.oMontoCobroLiquidacion = Number($scope.data.oMontoComprobanteNetoLiquidacion * (valorIGV / 100));
	     	    	             $scope.data.oMontoTotalLiquidacion = Number($scope.data.oMontoComprobanteNetoLiquidacion) + Number($scope.data.oMontoCobroLiquidacion);
	      	    	          }else{
	      	    	        	      if(!$scope.data.oIndImptoIncl && !$scope.data.afectoImpuesto)
	      	    	        	    	{
	      	    	       	  	           $scope.data.oMontoComprobanteNetoLiquidacion = $scope.data.oMontoComprobanteLiquidacion;
	     	    	                       $scope.data.oMontoCobroLiquidacion = 0;
	     	    	                       $scope.data.oMontoTotalLiquidacion = Number($scope.data.oMontoComprobanteNetoLiquidacion) + Number($scope.data.oMontoCobroLiquidacion);   	
	      	    	       	            }  
	      	    	               }
                          }
	      	  };
   	  });
    
    
    
    $scope.$watch('data.oMontoComprobanteLiquidacion',function(newVal,oldVal)
      {
         if (!newVal) return;         

         if ($scope.SesionLocale === 'PE'){
        	 if (newVal > datosEntrada.registro.ComisionSABDisponible)
               {
          	      $scope.data.oMontoComprobanteLiquidacion = datosEntrada.registro.ComisionSABDisponible;           	  
          	      $scope.data.btnActivos   = true;     //Activa botones
  			      bcsComunes.msg("warning","warning",$translate.instant('UI_MONTO_MAYOR_FACTURA_ORIGINAL'));			
  			      return;  	 
               } 
         }else{
        	 if (newVal > datosEntrada.registro.TotalComisLiq )
               {
          	     $scope.data.oMontoComprobanteLiquidacion = datosEntrada.registro.TotalComisLiq;           	  
          	     $scope.data.btnActivos   = true;     //Activa botones
    			 bcsComunes.msg("warning","warning",$translate.instant('UI_MONTO_MAYOR_FACTURA_ORIGINAL'));			
    			 return;  	 
               }
         }
         
         if( isNaN($scope.data.oMontoComprobanteLiquidacion) ) 
		 	$scope.data.oMontoComprobanteLiquidacion =  parseFloat($scope.data.oMontoComprobanteLiquidacion.replace(',','.'));
         
         if($scope.data.oIndImptoIncl && $scope.data.afectoImpuesto)
 	      {        	
 	         $scope.data.oMontoComprobanteNetoLiquidacion = ($scope.data.oMontoComprobanteLiquidacion / (valorIGV / 100 + 1));
 	         $scope.data.oMontoCobroLiquidacion = Number($scope.data.oMontoComprobanteNetoLiquidacion * (valorIGV/100));
 	         $scope.data.oMontoTotalLiquidacion = Number($scope.data.oMontoComprobanteNetoLiquidacion) + Number($scope.data.oMontoCobroLiquidacion);
 	      }
 	         else {
 	      	       if($scope.data.oIndImptoIncl && !$scope.data.afectoImpuesto)
 	      	          {
 	      	    	     $scope.data.oMontoComprobanteNetoLiquidacion = $scope.data.oMontoComprobanteLiquidacion;
 	      	             $scope.data.oMontoCobroLiquidacion = 0;
 	      	       	     $scope.data.oMontoTotalLiquidacion = Number($scope.data.oMontoComprobanteNetoLiquidacion) + Number($scope.data.oMontoCobroLiquidacion);
 	      	    	  }else {
 	      	    	           if(!$scope.data.oIndImptoIncl && $scope.data.afectoImpuesto)
 	      	    	            {
									if(datosEntrada.registro.EsExento === 'N'){
 	      	    	     	      		$scope.data.oMontoComprobanteNetoLiquidacion = $scope.data.oMontoComprobanteLiquidacion;
 	     	    	               		$scope.data.oMontoCobroLiquidacion = Number($scope.data.oMontoComprobanteNetoLiquidacion * (valorIGV / 100));
 	     	    	               		$scope.data.oMontoTotalLiquidacion = Number($scope.data.oMontoComprobanteNetoLiquidacion) + Number($scope.data.oMontoCobroLiquidacion);
									}else{
										$scope.data.oMontoComprobanteNetoLiquidacion = $scope.data.oMontoComprobanteLiquidacion;
 	     	    	               		$scope.data.oMontoCobroLiquidacion = $scope.data.afectoImpuesto ? Number($scope.data.oMontoComprobanteNetoLiquidacion * (valorIGV / 100)) : 0;;
 	     	    	               		$scope.data.oMontoTotalLiquidacion = Number($scope.data.oMontoComprobanteNetoLiquidacion) + Number($scope.data.oMontoCobroLiquidacion);
									}
								}else{
 	      	    	        	        if(!$scope.data.oIndImptoIncl && !$scope.data.afectoImpuesto)
 	      	    	        	    	   {
 	      	    	       	  	              $scope.data.oMontoComprobanteNetoLiquidacion = $scope.data.oMontoComprobanteLiquidacion;
 	     	    	                          $scope.data.oMontoCobroLiquidacion = 0;
 	     	    	                          $scope.data.oMontoTotalLiquidacion = Number($scope.data.oMontoComprobanteNetoLiquidacion) + Number($scope.data.oMontoCobroLiquidacion);   	
 	      	    	       	               }  
 	      	    	                 }
                            }
 	      	    };
      });
         
    
      $scope.$watch('data.afectoImpuesto',function(newVal,oldVal)
       {
    	  if($scope.data.oIndImptoIncl && $scope.data.afectoImpuesto)
    	    {        	
    	       $scope.data.oMontoComprobanteNetoLiquidacion = ($scope.data.oMontoComprobanteLiquidacion / (valorIGV / 100 + 1));
    	       $scope.data.oMontoCobroLiquidacion = Number($scope.data.oMontoComprobanteNetoLiquidacion * (valorIGV/100));
    	       $scope.data.oMontoTotalLiquidacion = Number($scope.data.oMontoComprobanteNetoLiquidacion) + Number($scope.data.oMontoCobroLiquidacion);
    	    }else {
    	      	     if($scope.data.oIndImptoIncl && !$scope.data.afectoImpuesto)
    	      	        {
    	      	    	   $scope.data.oMontoComprobanteNetoLiquidacion = $scope.data.oMontoComprobanteLiquidacion;
    	      	           $scope.data.oMontoCobroLiquidacion = 0;
    	      	       	   $scope.data.oMontoTotalLiquidacion = Number($scope.data.oMontoComprobanteNetoLiquidacion) + Number($scope.data.oMontoCobroLiquidacion);
    	      	    	}else {
    	      	    	        if(!$scope.data.oIndImptoIncl && $scope.data.afectoImpuesto)
    	      	    	          {
    	      	    	     	     $scope.data.oMontoComprobanteNetoLiquidacion = $scope.data.oMontoComprobanteLiquidacion;
    	     	    	             $scope.data.oMontoCobroLiquidacion = Number($scope.data.oMontoComprobanteNetoLiquidacion * (valorIGV / 100));
    	     	    	             $scope.data.oMontoTotalLiquidacion = Number($scope.data.oMontoComprobanteNetoLiquidacion) + Number($scope.data.oMontoCobroLiquidacion);
    	      	    	          }else{
    	      	    	        	      if(!$scope.data.oIndImptoIncl && !$scope.data.afectoImpuesto)
    	      	    	        	    	{
    	      	    	       	  	           $scope.data.oMontoComprobanteNetoLiquidacion = $scope.data.oMontoComprobanteLiquidacion;
    	     	    	                       $scope.data.oMontoCobroLiquidacion = 0;
    	     	    	                       $scope.data.oMontoTotalLiquidacion = Number($scope.data.oMontoComprobanteNetoLiquidacion) + Number($scope.data.oMontoCobroLiquidacion);   	
    	      	    	       	            }  
    	      	    	               }
                              }
    	      	  };
    	});    

	$scope.$watch('data.oConcepto.seleccionado', function (newVal, oldVal) {
		if ($scope.data.oConcepto.seleccionado.Contrapartida === 'S') {
			$scope.data.oTipoOperacion.idActual = 'COMOTC';
			$scope.data.oMonedaLiquidacion.idActual = 'USD';
			$scope.data.oTipoComprobante.idActual = 'FOE';
			$scope.data.oIndCmbteIndiv = false;
			$scope.data.afectoImpuesto = true;
			$scope.data.oIndActTes = false;
		}
	});
        
});