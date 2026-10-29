angular.module('ModuloCuentaInversion').controller('LiquidarSaldoCtrl',
		function($scope,$timeout,bcsAlerta,OPCIONES_BCSALERTA,$modal,$state,bcsComunes,$translate,$rootScope,
				bcsServiciosCuentaInversion, bcsServicioPlataforma)
{
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	
	var filtersSaldos = {};
	$scope.data = {};		
	$scope.btnLiquidarHabilitado = true;
	
	//SETEO INICIAL	
	$scope.data.kngFechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.data.showCamposCuentaBancariaSocioNegocio = false;
	calcularHeight($scope.data.showCamposCuentaBancariaSocioNegocio);
	
	
	//----------------------------------------------------
	// CONFIGURACION DE COMBOS
	//----------------------------------------------------	
	/*$scope.data.oConfiguracionSocioNegocio={
			tipo : "combobox-socio-negocio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},
	};
	
	$scope.data.oConfiguracionPortafolio={
			tipo : "combobox-portafolio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
			},
	};*/
	$scope.data.oConfiguracionMedioDePago={
			tipo : "combobox-medio-pago-cuenta-inversion",
			configuracion : {},
			filtros : {
				IdEstado 		 	: { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
				IdTipoMovimiento 	: { "field"   : "IdTipoMovimiento","operator": "eq", "value"   : "MOVEGR" },
				DesplegarEnCombobox : { "field"   : "DesplegarEnCombobox","operator": "eq", "value"   : "S" },
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};	
	
	
	$scope.data.oConfiguracionModalidadPago={
			tipo : "combobox-modalidad-pago",
			configuracion : {},
			filtros : {
				IdEstado      : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
				IdMedioDePago : { "field"   : "IdMedioDePago","operator": "eq", "value"   : "" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : '',
			//idpf: false,
	};	
	
	
	$scope.MonedaCuentaBancoSocio  	= {"field" : "IdMoneda","operator": "eq", "value"   : 0 };
	$scope.data.oConfiguracionCuentaBancoSocioNegocio={
			tipo : "combobox-persona-cuentas-bancarias",
			configuracion : {},
			filtros : {
				IdEstado  : { "field"   : "IdEstado", "operator": "eq", "value"   : "ACT" },
				IdPersona : { "field"   : "IdPersona","operator": "eq", "value"   : 0 },				
				IdMoneda  : $scope.MonedaCuentaBancoSocio
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	
	//----------------------------------------------------
	// CONFIGURACION DE GRILLAS
	//----------------------------------------------------	
	var dsSaldosCuentas = new bcs.DataSource({
		type : "call",
		definition : {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.
			//pageSize 			: 100,								
			callParameters      : filtersSaldos,
			sort 				: { field: "IdMoneda", dir: "asc" },
			callTransaction 	: "21",
			callResultSet 		: "P_OUT_SALDOPORFECHAEFECTIVA",
			schema : {
				model : {
					fields : {										
						NombreMoneda  	: {type : "string"},
						SaldoDisponible	: {type : "number"}
					}
				}
			}
		}
	}); 
 
	
	$scope.confBasicaSaldosCuentas = {
		dataSource  : dsSaldosCuentas,
		columns 	: [	 {field : "NombreMoneda", 		title:"UI_CUENTA_INVERSION", width: "150px"}
						,{field : "SaldoDisponible",	title:"UI_DISPONIBLE", 		template : " <label> #:  (util.bcs_formato(SaldoDisponible,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
					  ],
		contextMenu : true,
		filterable	: false,
		sortable 	: true,
		pageable 	: false,
		height 		: 120,
		selectable 	: "row",
		autoBind 	: false,
		userSettings: true
	};


	//No tiene type(odata/call) ya que es una grilla desconectada
	var dsFormaDePago = new bcs.DataSource({
		type : "array",
		definition : {
			data   		: [],
			///pageSize 	: 20,
			serverSorting	: false, // Ordenamiento de los datos en el servidor.
			schema : {
				model : {
					fields : {
						NombreMedioDePago           		: { type: "string"},   
						NombreModalidadDePago       		: { type: "string"},
						Monto              		    		: { type: "number"},
						NombreMoneda			       		: { type: "string"},
						NombreBancoSocio					: { type: "string"},
						IdTipoCuentaBancariaSocio			: { type: "string"},
						NombreTipoCuentaBancariaSocio		: { type: "string"},					 
						NombreCuentaBancariaSocio			: { type: "string"},
						NumeroCuentaBancariaSocio			: { type: "string"},
					    IdMedioDePago                      	: { type: "string"},
					    IdModalidadDePago					: { type: "string"},
					    IdCuentaBancariaSocio				: { type: "number"},						     
					    IdMoneda							: { type: "string"},
					    IdBancoSocio						: { type: "number"},
					    IdTipoCuentaBancariaSocio			: { type: "string"},
					}
				}
			},
			aggregate : ([   // se usa para calculos (sum)
			                 { field: "Monto", aggregate: "sum" },
			                 { field: "NombreMedioDePago", aggregate: "count" }
						])			
		}
	});


	
	$scope.confBasicaFormaDePago = {
		dataSource  : dsFormaDePago,
		columns 	: [
						      {field : "NombreMedioDePago",     title:'UI_MEDIO_DE_PAGO', width : "80px", align: "right", sortable : false }
							 ,{field : "NombreModalidadDePago", title:'UI_MODALIDAD', width : "80px", sortable : false}
							 ,{field : "Monto", title:'UI_MONTO', width : "50px", align: "left", sortable : false, template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>" }			 
							 ,{field : "NombreMoneda", title: "UI_MONEDA", width: "60px", align: "left", sortable : false}
							 ,{title   : "UI_SOCIO_NEGOCIO", align: "center",
							     columns : [
										 {field : "NombreBancoSocio",     		   title:"UI_BANCO",             width : "100px", align: "right", sortable : false }
										,{field : "NombreTipoCuentaBancariaSocio", title:'UI_TIPO_CUENTA_BANCO', width : "100px", align: "right", sortable : false }			           
										,{field : "NombreCuentaBancariaSocio",     title:'UI_CUENTA_BANCARIA',   width : "100px", align: "right", sortable : false }
										,{field : "NumeroCuentaBancariaSocio",     title:'UI_NUMERO_CUENTA',     width : "60px",  align: "left",  sortable : false }
							     ]}
							 
						     //Columnas ocultas (Se usan para editar forma de pago en forma desconectada)
						     ,{field : "IdMedioDePago",                title:'UI_MEDIO_DE_PAGO',     hidden: true  }
						     ,{field : "IdModalidadDePago",            title:'UI_MODALIDAD',         hidden: true  }
						     ,{field : "IdCuentaBancariaSocio",        title:'UI_TIPO_CUENTA_BANCO', hidden: true  }						     
						     ,{field : "IdMoneda",                     title:"UI_MONEDA",            hidden: true  }
						     ,{field : "IdBancoSocio",                 title:'UI_BANCO',             hidden: true  }
						     ,{field : "IdTipoCuentaBancariaSocio",    title:'UI_TIPO_CUENTA_BANCO', hidden: true  }
						   
					  ],
		contextMenu : true,
		filterable	: false,
		sortable 	: true,
		pageable 	: false,
		height 		: 170,
		selectable 	: "multiple row",
		autoBind 	: false,
		userSettings: true
	};

	
	
	//----------------------------------------------------
	// FUNCIONES
	//----------------------------------------------------	
	var buscarSaldosCuentaInversion  = function() {		
		filtersSaldos.p_IdEmpresa 					= $scope.SesionIdEmpresa;
		filtersSaldos.p_IdUsuario         			= $scope.SesionIdUsuario;
		filtersSaldos.p_IdModulo					= 'CTA';
		filtersSaldos.p_UsaReglasNegocio			= 'S';
		filtersSaldos.p_IdPais						= $scope.SesionIdPaisSistema;
		filtersSaldos.p_CuentaInversionAConsultar	= [{IdEjecutivo		  : null,
														IdPortafolio  	  : Number($scope.data.oConfiguracionPortafolio.seleccionado.IdPortafolio),
														IdMoneda      	  : null
													  }];
		filtersSaldos.p_FechaDesde					= $scope.SesionFechaSistema || '0001-01-01';
		filtersSaldos.p_FechaHasta					= $scope.SesionFechaSistema || '0001-01-01';
		
		
		dsSaldosCuentas.setCallParameters(filtersSaldos);
		dsSaldosCuentas.read();
	};
	

	//Quita una forma de pago de la grilla(solo visualmente) 
	$scope.eliminarFormaPago = function()
	{	
		if ($scope.seleccionFormaDePago === undefined)
		{	
			bcsComunes.msg("warning","info",$translate.instant('UI_DEBE_SELECCIONAR_FORMA_DE_PAGO'));		
		}
		else
		{	
			dsFormaDePago.remove($scope.seleccionFormaDePago);			
			$scope.seleccionFormaDePago = undefined;			
		}
	}
	
	//Al cambiar selección de cta. inv., actualiza monto y cuentas bancarias(por tipo moneda)	
	$scope.cambiarCuentaInversion = function(seleccionSaldosCuentas)
	{			
		$scope.MonedaCuentaBancoSocio.value = seleccionSaldosCuentas.IdMoneda; //Actualiza moneda de las ctas. corrientes del cliente
		$scope.limpiarFormaPago();
		dsFormaDePago.removeAll();
		$scope.data.kngMontoFormaPago 		= seleccionSaldosCuentas.SaldoDisponible; //Actualiza monto(después de limpiar)
		$scope.data.montoFormaPagoSimbolo	= seleccionSaldosCuentas.IdMoneda;
		$scope.data.decimales				= {format: 'n'+(util.bcs_formato(0,'GEN','MONTO',seleccionSaldosCuentas.IdMoneda,'','')).cantidadDecimales,min:0,spinners: false};
	};

	
	//Limpia los datos disabled de la forma de pago
	$scope.limpiarDatosDisabled = function()
	{	
		//Limpia datos de la cuenta bancaria del socio de negocio
		$scope.data.oConfiguracionCuentaBancoSocioNegocio.idActual = 0;
		$scope.data.TipoCuentaSocio    = '';
		$scope.data.BancoCuentaSocio   = '';
		$scope.data.NumeroCuentaSocio  = '';
		$scope.data.MonedaCuentaSocio  = '';
		$scope.data.showCamposCuentaBancariaSocioNegocio = false;
		calcularHeight($scope.data.showCamposCuentaBancariaSocioNegocio);
	}
	
	
	//Limpia los campos de la Forma de pago
	$scope.limpiarFormaPago = function()
	{
		$scope.data.oConfiguracionMedioDePago.idActual = 0;
		$scope.data.oConfiguracionMedioDePago.seleccionado = false;
		$scope.data.oConfiguracionModalidadPago.idActual = 0;		
		$scope.data.oConfiguracionModalidadPago.seleccionado = false;
		$scope.data.kngMontoFormaPago = 0;
		
		$scope.limpiarDatosDisabled();
	};
	
	//----------------------------------------------------
	// WATCH
	//----------------------------------------------------	
	$scope.$watch('data.oConfiguracionModalidadPago.seleccionado',function(newval,oldval){		
		if (typeof $scope.data.oConfiguracionModalidadPago.seleccionado !== 'undefined' && $scope.data.oConfiguracionModalidadPago.seleccionado !== false)
		{	
			var IdModalidadDePago = $scope.data.oConfiguracionModalidadPago.seleccionado.IdModalidadDePago;
			
			if (IdModalidadDePago === 'MODDEP')  //Depósito en cta. del cliente
			{
				$scope.data.showCamposCuentaBancariaSocioNegocio = true;	
				calcularHeight($scope.data.showCamposCuentaBancariaSocioNegocio);			
			}
			else   //Cliente retira por caja
			{
				$scope.data.showCamposCuentaBancariaSocioNegocio = false;			
				calcularHeight($scope.data.showCamposCuentaBancariaSocioNegocio);					
			}
		}		
	});

	
	
	$scope.$watch('data.oConfiguracionCuentaBancoSocioNegocio.seleccionado',function(newval,oldval){		
		if (typeof $scope.data.oConfiguracionCuentaBancoSocioNegocio.seleccionado !== 'undefined' && $scope.data.oConfiguracionCuentaBancoSocioNegocio.seleccionado !== false)
		{	
			$scope.data.TipoCuentaSocio   = $scope.data.oConfiguracionCuentaBancoSocioNegocio.seleccionado.NombreTipoCuentaBancaria;												
			$scope.data.BancoCuentaSocio  = $scope.data.oConfiguracionCuentaBancoSocioNegocio.seleccionado.NombreBanco;			
			$scope.data.NumeroCuentaSocio = $scope.data.oConfiguracionCuentaBancoSocioNegocio.seleccionado.NumeroCuentaBancaria;
			$scope.data.MonedaCuentaSocio = $scope.data.oConfiguracionCuentaBancoSocioNegocio.seleccionado.NombreMoneda;
		}
	});		
	
	
	$scope.$watch('data.oConfiguracionPortafolio.idActual',function(newval,oldval){
		if (typeof $scope.data.oConfiguracionPortafolio !== 'undefined' &&
			$scope.data.oConfiguracionPortafolio.idActual !== false)
		{	
			buscarSaldosCuentaInversion();
		}
	});	
	
	
	//Validaciones de forma de pago(para datos desconectados) 
	$scope.validarDatosFormaPago = function()
	{	
		
		if ($scope.data.kngMontoFormaPago === undefined || $scope.data.kngMontoFormaPago === 0 || $scope.data.kngMontoFormaPago === ''|| $scope.data.kngMontoFormaPago === null)
		{	
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_DEBE_INGRESAR_MONTO")
			},$scope.winID);			
			return(false);
		} 
		
		if (!$scope.seleccionSaldosCuentas)
		{
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("CUENTA_INVERSION_INVALIDA")
			},$scope.winID);			
			return(false);
		}
		
		var saldoCuentaInversion = $scope.seleccionSaldosCuentas.SaldoDisponible;  // Saldo de la cta. inv. seleccionada		
		if (saldoCuentaInversion < 1)
		{
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_CUENTA_INVERSION_SIN_SALDO")
			},$scope.winID);						
			return(false);
		}
		
		var sumMontoFormaDePago  = Number(dsFormaDePago.aggregates().Monto.sum) + Number($scope.data.kngMontoFormaPago);
		if (sumMontoFormaDePago > saldoCuentaInversion)
		{
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_MONTO_FORMA_PAGO_EXCEDE_MONTO_MOVIMIENTO")
			},$scope.winID);									
			return(false);
		}
		
		return(true);
	}
	
	
	
	//Agrega una nueva forma de pago(un nuevo row a la grilla) (solo visualmente)
	$scope.agregarFormaPago = function()
	{
		if ($scope.validarDatosFormaPago())
		{				
		
			var objFormaPago = {};

			objFormaPago.IdMedioDePago				= $scope.data.oConfiguracionMedioDePago.seleccionado.IdMedioDePago || 0;
			objFormaPago.NombreMedioDePago			= $scope.data.oConfiguracionMedioDePago.seleccionado.Nombre || '';
			objFormaPago.IdModalidadDePago			= $scope.data.oConfiguracionModalidadPago.seleccionado.IdModalidadDePago || '';			
			objFormaPago.NombreModalidadDePago		= $scope.data.oConfiguracionModalidadPago.seleccionado.Nombre;
			objFormaPago.IdMoneda					= $scope.seleccionSaldosCuentas.IdMoneda || '';
			objFormaPago.NombreMoneda				= $scope.seleccionSaldosCuentas.NombreMoneda || '';
			objFormaPago.Monto						= $scope.data.kngMontoFormaPago || 0;
			
			objFormaPago.IdCuentaBancariaSocio 				= 0;
			objFormaPago.IdTipoCuentaBancariaSocio 			= '';
			objFormaPago.NombreTipoCuentaBancariaSocio 		= '';
			objFormaPago.NombreCuentaBancariaSocio			= '';
			objFormaPago.NumeroCuentaBancariaSocio			= '';
			
			objFormaPago.IdBancoSocio						= 0;
			objFormaPago.NombreBancoSocio                   = '';
						
						
			if (objFormaPago.IdModalidadDePago === "MODDEP")
			{	
				//Datos de la cuenta bancaria del socio de negocio								
				objFormaPago.IdCuentaBancariaSocio 			= Number($scope.data.oConfiguracionCuentaBancoSocioNegocio.seleccionado.IdCuentaBancaria || 0);
				objFormaPago.IdBancoSocio					= $scope.data.oConfiguracionCuentaBancoSocioNegocio.seleccionado.IdBanco || 0;
				objFormaPago.IdTipoCuentaBancariaSocio 		= $scope.data.oConfiguracionCuentaBancoSocioNegocio.seleccionado.IdTipoCuentaBancaria || '';
				objFormaPago.NombreTipoCuentaBancariaSocio 	= $scope.data.oConfiguracionCuentaBancoSocioNegocio.seleccionado.NombreTipoCuentaBancaria || '';
				objFormaPago.NombreCuentaBancariaSocio		= $scope.data.oConfiguracionCuentaBancoSocioNegocio.seleccionado.NombreCuentaBancariaFull || '';
				objFormaPago.NumeroCuentaBancariaSocio		= $scope.data.oConfiguracionCuentaBancoSocioNegocio.seleccionado.NumeroCuentaBancaria || '';
				objFormaPago.NombreBancoSocio 				= $scope.data.oConfiguracionCuentaBancoSocioNegocio.seleccionado.NombreBanco || '';
			}
			
			console.log(JSON.stringify(objFormaPago));
			dsFormaDePago.add(objFormaPago);
											
			
			$scope.limpiarFormaPago();  //Limpia formas de pago
		}
	}	
 	

	//----------------------------------------------------------------
	//SERVICIOS
	//----------------------------------------------------------------
	$scope.liquidarSaldo = function()
    {	
		if (dsFormaDePago.aggregates().NombreMedioDePago.count === 0)
		{	
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_DEBE_SELECCIONAR_FORMA_DE_PAGO")
			},$scope.winID);
			return(false);
		}
		
		var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_LIQUIDARA_SALDO_CUENTA_INVERSION') );
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR)
			{
				bcsComunes.ventanaOverlay($scope.winID, true);
				
				var datos = {};
				datos.p_IdEmpresa                       = $scope.SesionIdEmpresa;
				datos.p_IdUsuario                       = $scope.SesionIdUsuario;
				datos.p_IdModulo                        = 'CTA';
				datos.p_simularOperacion                = 'N';
				datos.p_UsaReglasNegocio                = 'S';
				datos.p_FechaSistema                    = $scope.SesionFechaSistema || '0001-01-01';
				 
				datos.p_CuentaInversionALiquidar		= [{
															"NumeroReferencia"  : $scope.seleccionSaldosCuentas.IdCuentaInversion || 0,
															"IdCuentaInversion" : $scope.seleccionSaldosCuentas.IdCuentaInversion || 0,
															"IdPersona"         : Number($scope.data.oConfiguracionPortafolio.seleccionado.IdPersona || 0),
														    "IdPortafolio"		: Number($scope.data.oConfiguracionPortafolio.seleccionado.IdPortafolio || 0),
															"IdMoneda"			: $scope.seleccionSaldosCuentas.IdMoneda || '',
															"MontoALiquidar"	: Number(dsFormaDePago.aggregates().Monto.sum)	  //Sumatoria del monto de la grilla Forma de Pago
														   }];
				
				 
				
				//Obtiene forma de pago desde la grilla
				var arrDataFormaPago = new Array();			
				arrDataFormaPago = dsFormaDePago.dataColumns([ 'IdMedioDePago',
				                                               'IdModalidadDePago',
				                                               'IdMoneda',
				                                               'Monto',
															   'IdCuentaBancariaSocio',
															   'IdBancoSocio',
															   'NombreCuentaBancariaSocio',
															   'NumeroCuentaBancariaSocio',
															   'IdTipoCuentaBancariaSocio',
															   'NombreTipoCuentaBancariaSocio',
															   'NumeroSerieSocio',
															   'NumeroDocumentoSocio',
															   'FechaDocumentoPagoSocio',
															   'CertificadoAutomatico']);
				
				 

				//Agrega los campos de Forma de Pago que no se usan para este egreso.
				if (arrDataFormaPago !== undefined)
				{
					$.each(arrDataFormaPago, function(key,value){
						    arrDataFormaPago[key].NumeroReferencia 			 	 = $scope.seleccionSaldosCuentas.IdCuentaInversion || 0;
							arrDataFormaPago[key].FechaDocumentoPagoSocio 		 = '0001-01-01';
							arrDataFormaPago[key].NumeroDocumentoSocio 			 = 0;
							arrDataFormaPago[key].NumeroSerieSocio 				 = '';			
							arrDataFormaPago[key].IdTipoTarjetaBancariaSocio 	 = '';
							arrDataFormaPago[key].NombreTipoTarjetaBancariaSocio = '';
							arrDataFormaPago[key].NumeroTarjetaBancariaSocio 	 = '';
							arrDataFormaPago[key].IdTarjetaBancariaSocio 		 = 0;					
						    arrDataFormaPago[key].IdCuentaBancariaCorredora 	 = 0;				   
						    arrDataFormaPago[key].IdTarjetaBancariaCorredora 	 = 0;				   
						    arrDataFormaPago[key].IdChequera 					 = 0;				   
						    arrDataFormaPago[key].NumeroDocumentoCorredora 		 = 0;				   
						    arrDataFormaPago[key].FechaDocumentoPagoCorredora 	 = '0001-01-01';
					});
				}				
				datos.p_TableFormaPago 			= arrDataFormaPago || [];
					 
				bcsServiciosCuentaInversion.LiquidarSaldo(datos).$promise.then(				   
				   function(result) {
						$scope.limpiarFormaPago(); 
						$scope.seleccionSaldosCuentas = false;
						dsFormaDePago.removeAll();
						buscarSaldosCuentaInversion();  //Actualiza la visualización de los saldos
	
						bcsComunes.ventanaOverlay($scope.winID, false);
						bcsComunes.ventanaMsg({
							conflictos:util.parseServerResponse(result)
						},$scope.winID);						                                          
				   },
				   function(result){          
						bcsComunes.ventanaOverlay($scope.winID, false);
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
							conflictos:util.parseServerResponse(result)
						},$scope.winID);                               
					}
				);
		       
			}
		});		
    };	

	function calcularHeight($val){
		let firstHeight = $('#Win_LiquidarSaldo');
		if($val){
			firstHeight.css('height', '625px');
		}else{
			firstHeight.css('height', '580px');
		}
	}
});
