angular.module('ModuloTesoreria').controller('EdicionMultipleCtrl',
		function($scope,$uibModal,$translate,$timeout,
		bcsComunes,bcsServiciosTesoreria,bcsAlerta,OPCIONES_BCSALERTA,$translate)
{	
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	$scope.parametros = bcsComunes.ventanaParams($scope.winID);
	$scope.showMedioDePago 				= false;
	$scope.showMedioDePagoSeleccion 	= false;
	$scope.showDatosSocioNegocio 		= false;
	$scope.showLiquidacionComprobante 	= false;
	$scope.disabledIndicadores 			= true;
	$scope.ngLiquidaComprobante 		= false;
	$scope.showListaCuentasSocioNegocio = $scope.parametros[0].showListaCuentasSocioNegocio;
	$scope.exigeChequera 				= ($scope.IdMedioDePago == 'CHE');
	$scope.Entidad		= $scope.parametros[0].Entidad;
	$scope.ListadoMov 	= $scope.parametros[1];
	$scope.kngFechaDocumentoSocio = util.dateInicioDia($scope.SesionFechaSistema);
	
	$scope.objFilter   = {};
	$scope.Movimientos = [];
	if ($scope.Entidad === 'TES.IngresoEgreso'){
		$scope.disabledIndicadores = false; 
		for (var i = 0; i<Object.keys($scope.ListadoMov).length;i++) {
			$scope.Movimientos.push({"field" : "IdIngresoEgreso","operator": "eq", "value" : Number($scope.ListadoMov[i].Id) });
		};
	};
	if ($scope.Entidad === 'TES.Comprobante'){
		$scope.showLiquidacionComprobante 	= true;
		$scope.ngLiquidaComprobante 		= true;
		for (var i = 0; i<Object.keys($scope.ListadoMov).length;i++) {
			$scope.Movimientos.push({"field" : "IdComprobante","operator": "eq", "value" : Number($scope.ListadoMov[i].Id) });
		};
	};
	$scope.objFilter.logic = 'or';
	$scope.objFilter.filters = $scope.Movimientos;

	$scope.oConfiguracionMedioDePago={
		tipo : "combobox-medio-pago-tesoreria",
		configuracion : {},
		filtros : {
			IdEstado 		 	: { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
			DesplegarEnCombobox	: { "field"   : "DesplegarEnCombobox","operator": "eq", "value"   : "S" },
			IdTipoMovimiento 	: { "field"   : "IdTipoMovimiento","operator": "eq", "value"   : "" },
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	$scope.oConfiguracionModalidadPago={
		tipo : "combobox-modalidad-pago",
		configuracion : {},
		filtros : {
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
			IdMedioDePago : { "field"   : "IdMedioDePago","operator": "eq", "value"   : "" }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : '',					
	};
	$scope.oConfiguracionCuentaBancoCorredora={
		tipo : "combobox-cuentas-bancarias-corredora",
		configuracion : {},
		filtros : {
			IdEstado  : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },				
			IdMoneda  : { "field"   : "IdMoneda","operator": "eq", "value"   : ""  },
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	$scope.oConfiguracionChequerasSeries={
		tipo : "combobox-chequeras-series",
		configuracion : {},
		filtros : {
			IdEstado		: { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
			IdCuentaBanco 	: { "field"   : "IdCuentaBanco","operator": "eq", "value"   :"" }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : false
	};
	$scope.oConfiguracionBancoCuentaSocio={
		tipo          : "combobox-banco",
		configuracion : {},
		filtros       : {
			IdEstado	: { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
		}, 
		idActual      : false,
		seleccionado  : false,
		idDefecto     : '',		
		idPostFiltro  :	false
	};	
	$scope.oConfiguracionListaCuentaBancoSocioNegocio={				   
		tipo          : "combobox-persona-cuentas-bancarias",
		configuracion : {} ,
		filtros       : {
			IdEstado  : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
			IdPersona : { "field"   : "IdPersona","operator": "eq", "value"   : Number($scope.parametros[0].IdPersona) },						
			IdMoneda  : { "field"   : "IdMoneda", "operator": "eq", "value"   : $scope.parametros[0].IdMoneda},
			IdTipoCuentaBancaria  : { "field"   : "IdTipoCuentaBancaria", "operator": "eq", "value"   : "CUCO"},
		}, 
		idActual      : false,
		seleccionado  : false,
		idDefecto     : '',		
		idPostFiltro  :	false
	};


	obtenerCajaUsuario = function(IdMoneda){
		var cajaDatasource = new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering	:true,
				transport		: {read: {url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1046",dataType: "json"}},
				schema 			: {model : {fields : {	NombreCaja		: { type: "string"},
														NombreLargoCaja	: { type: "string"}
													  }
											}
				},		
				filter			: [{ "field": "IdEmpresa",			"operator": "eq", "value" : $scope.SesionIdEmpresa },
								  { "field": "IdUsuario",       	"operator": "eq", "value" : $scope.SesionIdUsuario },
								  { "field": "IdEstadoCaja", 		"operator": "eq", "value" : "ABT" },
								  { "field": "IdEstadoCajaMoneda", 	"operator": "eq", "value" : "ACT" },
								  { "field": "IdEstadoCajaUsuario", "operator": "eq", "value" : "ACT" },				
								  { "field": "FechaInicio",     	"operator": "lte","value" : $scope.SesionFechaSistema },
								  { "field": "FechaTermino",     	"operator": "gte","value" : $scope.SesionFechaSistema },
								  { "field": "IdMoneda",     		"operator": "eq", "value" : IdMoneda}
								 ]
			}
		});
		cajaDatasource.query().then(function(){
			if (typeof cajaDatasource.data()[0] !== 'undefined'){
				$scope.ngNombreCaja = cajaDatasource.data()[0].NombreCaja;
				$scope.ngMonedaCaja	= cajaDatasource.data()[0].IdMoneda;
			}
		});
	};
	
		
	var MovimientosDatasource = new bcs.DataSource({
		type: "odata",
		definition : {
			serverFiltering	:true,
			transport		: {read:  {url: "/BSCL/Plataforma/GWJAVA/PLA/call?t="+(($scope.Entidad === 'TES.IngresoEgreso')?"1031":"1020"),dataType: "json"}},
			schema 			: {model: {
				fields : {
					AgrupaComprobante			: { type: "string"},
					FechaDocumentoPagoSocio 	: {type: 'sysdate'},
					IdPersona 					: { type: "number"},
					IdMedioDePago 				: { type: "string"},
					IdTipoMovimiento 			: { type: "string"},
					IdCuentaBancoCorredora 		: { type: "number"},
					IdChequera					: { type: "number"},
					IdBancoSocio 				: { type: "number"},
					IdMoneda 					: { type: "string"},
					IdMedioDePago               : { type: "string"},
					IdModalidadDePago           : { type: "string"},
					Monto                       : { type: "number"},
					NombreTipoMovimiento        : { type: "string"},
					NumeroCuentaBancariaSocio   : { type: "string"},
					NumeroSerieSocio            : { type: "string"},
					NumeroDocumentoSocio		: { type: "number"},		
					NeteaComprobante			: { type: "string"},
					NombreMedioDePago		    : { type: "string"},
					NombreModalidadDePago       : { type: "string"},
					}
				}
			},
			filter			: $scope.objFilter
		}
	});
	
	MovimientosDatasource.fetch().then(function(){
		$scope.Monto = 0;
		$scope.CountMedioDePago = 0;
		$scope.IdPersona = 0;
		$scope.CountDistPersona	= 0;
		for (var i = 0; i<MovimientosDatasource.data().length;i++) {
			
			if ($scope.IdMedioDePago !== MovimientosDatasource.data()[i].IdMedioDePago){				
				$scope.IdMedioDePago = MovimientosDatasource.data()[i].IdMedioDePago;
				$scope.CountMedioDePago	= $scope.CountMedioDePago + 1;
			}
			if ($scope.IdMedioDePago === "S/I"){
				$scope.CountMedioDePago	= $scope.CountMedioDePago + 1;
			}			
			if ($scope.IdPersona !== MovimientosDatasource.data()[i].IdPersona){
				$scope.IdPersona = MovimientosDatasource.data()[i].IdPersona;
				$scope.CountDistPersona	= $scope.CountDistPersona + 1;
			}

			$scope.IdTipoMovimiento			 = MovimientosDatasource.data()[i].IdTipoMovimiento
			$scope.ngTipoMovimiento 		 = MovimientosDatasource.data()[i].NombreTipoMovimiento;
			$scope.IdMoneda					 = MovimientosDatasource.data()[i].IdMoneda;
			$scope.Monto					 = $scope.Monto + Number(MovimientosDatasource.data()[i].Monto);
			
			$scope.IdCuentaBancoCorredora	 = MovimientosDatasource.data()[i].IdCuentaBancoCorredora;
			$scope.IdChequera				 = MovimientosDatasource.data()[i].IdChequera;
			
			$scope.IdBancoSocio				 = MovimientosDatasource.data()[i].IdBancoSocio;
			$scope.NumeroCuentaBancariaSocio = MovimientosDatasource.data()[i].NumeroCuentaBancariaSocio;
			$scope.NumeroSerieSocio   		 = MovimientosDatasource.data()[i].NumeroSerieSocio;
			$scope.NumeroDocumentoSocio		 = MovimientosDatasource.data()[i].NumeroDocumentoSocio;
			$scope.FechaDocumentoPagoSocio   = MovimientosDatasource.data()[i].FechaDocumentoPagoSocio;
			
			$scope.AgrupaComprobante		 = (MovimientosDatasource.data()[i].AgrupaComprobante == 'SI');
			$scope.NeteaComprobante		     = (MovimientosDatasource.data()[i].NeteaComprobante == 'SI');

			$scope.IdMedioDePago			 = MovimientosDatasource.data()[i].IdMedioDePago;
			$scope.IdModalidadDePago		 = MovimientosDatasource.data()[i].IdModalidadDePago;
			$scope.ngMedioDePago			 = MovimientosDatasource.data()[i].NombreMedioDePago;			
			$scope.ngModalidadDePago		 = MovimientosDatasource.data()[i].NombreModalidadDePago;
			$scope.exigeChequera 			 = (MovimientosDatasource.data()[i].IdMedioDePago == 'CHE');
		};

		$scope.ngAgrupa = $scope.AgrupaComprobante;
		$scope.ngNetea = $scope.NeteaComprobante;
		
		$scope.kngMonto				= $scope.Monto;
		$scope.kngMontoSimbolo		= $scope.IdMoneda;
		$scope.kngMontoDecimales	= (util.bcs_formato($scope.kngMonto,'GEN','Monto',$scope.kngMontoSimbolo,'','')).cantidadDecimales;
		$scope.ngRegistrosProcesar	= Number(MovimientosDatasource.data().length);
		
		$scope.oConfiguracionCuentaBancoCorredora.filtros.IdMoneda.value  = $scope.IdMoneda;
		$scope.oConfiguracionCuentaBancoCorredora.filtros.read = true;
		$scope.oConfiguracionMedioDePago.filtros.IdTipoMovimiento.value = MovimientosDatasource.data()[0].IdTipoMovimiento;
		$scope.oConfiguracionMedioDePago.filtros.read = true;	
		
		$scope.oConfiguracionCuentaBancoCorredora.idPostFiltro = $scope.IdCuentaBancoCorredora;
		$scope.oConfiguracionChequerasSeries.idPostFiltro = $scope.IdChequera;
		$scope.oConfiguracionBancoCuentaSocio.idPostFiltro = $scope.IdBancoSocio;
		
		if ($scope.CountMedioDePago > 1) {
			$scope.showMedioDePagoSeleccion = true;
		}else {
			$scope.showMedioDePago = true;
		};
		if ($scope.CountDistPersona === 1 && MovimientosDatasource.data()[0].IdTipoPersona !== 'CLI'){
			$scope.showDatosSocioNegocio = true;			
		};

		obtenerCajaUsuario($scope.IdMoneda);
		$scope.ConfiguracionMedioPago($scope.IdTipoMovimiento,$scope.IdMedioDePago);		
	});


	$scope.$watch('ngNetea',function(newval,oldval){
		switch (newval){
			case true:  $scope.ngAgrupa = true; break;
		}			
	});
	
	$scope.$watch('ngAgrupa',function(newval,oldval){
		switch (newval){
			case false:  $scope.ngNetea = false; break;
		}			
	});
	
	$scope.$watch('ngEditarFP',function(newval,oldval){
		if ($scope.ngEditarFP){
			$scope.showMedioDePagoSeleccion = true;
			$scope.showMedioDePago = false;
		}		
	});
	$scope.$watch('oConfiguracionCuentaBancoCorredora.seleccionado',function(newval,oldval){
		$scope.ngTipoCuentaCorredora 	= $scope.oConfiguracionCuentaBancoCorredora.seleccionado.NombreTipoCuentaBancaria;
		$scope.ngBancoCuentaCorredora 	= $scope.oConfiguracionCuentaBancoCorredora.seleccionado.NombreBanco;			
		$scope.ngNumeroCuentaCorredora 	= $scope.oConfiguracionCuentaBancoCorredora.seleccionado.NumeroCuentaBancaria;
		$scope.ngMonedaCuentaCorredora 	= $scope.oConfiguracionCuentaBancoCorredora.seleccionado.NombreMoneda;
	});
	
	$scope.$watch('oConfiguracionBancoCuentaSocio.seleccionado',function(newval,oldval){
		$scope.NumeroCuentaSocio 	    = $scope.NumeroCuentaBancariaSocio;
		$scope.SerieDocumentoSocio 		= $scope.NumeroSerieSocio;	
		$scope.FolioDocumentoSocio 	    = $scope.NumeroDocumentoSocio;
		$scope.kngFechaDocumentoSocio   = $scope.FechaDocumentoPagoSocio;
	});
	$scope.$watch('oConfiguracionListaCuentaBancoSocioNegocio.seleccionado',function(newval,oldval){		
		$scope.ngTipoCuentaSocioNegocio 	= $scope.oConfiguracionListaCuentaBancoSocioNegocio.seleccionado.NombreTipoCuentaBancaria;
		$scope.ngBancoCuentaSocioNegocio 	= $scope.oConfiguracionListaCuentaBancoSocioNegocio.seleccionado.NombreBanco;			
		$scope.ngNumeroCuentaSocioNegocio 	= $scope.oConfiguracionListaCuentaBancoSocioNegocio.seleccionado.NumeroCuentaBancaria;
		$scope.NumeroCuentaSocio			= $scope.oConfiguracionListaCuentaBancoSocioNegocio.seleccionado.NumeroCuentaBancaria;
		$scope.ngMonedaCuentaSocioNegocio 	= $scope.oConfiguracionListaCuentaBancoSocioNegocio.seleccionado.NombreMoneda;
		$scope.SerieDocumentoSocio 			= $scope.NumeroSerieSocio;	
		$scope.FolioDocumentoSocio 	  		= $scope.NumeroDocumentoSocio;
		$scope.kngFechaDocumentoSocio  		= $scope.FechaDocumentoPagoSocio;		
	});
	
	$scope.$watchGroup(['oConfiguracionModalidadPago.idActual','oConfiguracionMedioDePago.idActual'],function(newval,oldval){
		if (!$scope.showMedioDePagoSeleccion)
			return;

		$scope.IdMedioDePago		= $scope.oConfiguracionMedioDePago.seleccionado.IdMedioDePago;
		$scope.IdModalidadDePago	= $scope.oConfiguracionModalidadPago.seleccionado.IdModalidadDePago;
		$scope.ConfiguracionMedioPago($scope.IdTipoMovimiento,$scope.oConfiguracionMedioDePago.seleccionado.IdMedioDePago);			

		if($scope.IdMedioDePago === 'CHE'){
			$scope.exigeChequera = true;
		}else{
			$scope.exigeChequera = false;
		}
			
			
	});
	
	$scope.$watch('oConfiguracionChequerasSeries.seleccionado',function(newval,oldval){
		if (newval === oldval){
			return;
		};

		if (typeof $scope.oConfiguracionChequerasSeries.seleccionado !== 'undefined' )
		{
			$scope.remanente 	= Number($scope.oConfiguracionChequerasSeries.seleccionado.Remanente) - 
								  Number($scope.ListadoMov.length);
			
			$scope.FolioDocumentoCorredoraDesde	= $scope.oConfiguracionChequerasSeries.seleccionado.NumeroChequeActual;


			if ($scope.remanente >= 0){
				$scope.FolioDocumentoCorredoraHasta = Number($scope.oConfiguracionChequerasSeries.seleccionado.NumeroChequeActual) + 
												  	  Number($scope.ListadoMov.length === 1 ? 0 : $scope.ListadoMov.length -1);
			} else {
				$scope.FolioDocumentoCorredoraHasta = Number($scope.oConfiguracionChequerasSeries.seleccionado.NumeroChequeActual) + 
												  	  Number($scope.oConfiguracionChequerasSeries.seleccionado.Remanente);
			};
		}else {
			$scope.FolioDocumentoCorredoraDesde = undefined;
			$scope.FolioDocumentoCorredoraHasta = undefined;
		}
	});

	$scope.ConfiguracionMedioPago = function(IdTipoMovimiento, IdMedioDePago){
		$scope.limpiar();

		switch (IdTipoMovimiento){
			case 'MOVING':
				switch (IdMedioDePago){					
					case "BDE":
						$scope.DisabledFolioDocumentoSocio = false;
						$scope.exigeChequera = false;
						break;
					case "CHE":
						$scope.DisabledSerieDocumentoSocio = false;
						$scope.oConfiguracionChequerasSeries.activado = true;
						$scope.DisabledFolioDocumentoSocio = false;
						$scope.exigeChequera = false;
						break;
					case "EBA":
						$scope.DisabledSerieDocumentoSocio = false;
						$scope.DisabledFolioDocumentoSocio = false;
						$scope.exigeChequera = false;
						break;
					case "VVI":
						$scope.DisabledSerieDocumentoSocio = false;
						$scope.DisabledFolioDocumentoSocio = false;
						$scope.exigeChequera = false;
						break;
					case "EFE": 
						$scope.oConfiguracionCuentaBancoCorredora.activado = false; 
						$scope.oConfiguracionCuentaBancoCorredora.requerido = false; 
						$scope.oConfiguracionBancoCuentaSocio.activado	= false;
						$scope.DisabledNumeroCuentaSocio	= true;
						$scope.exigeChequera = false;
						break;	
				};
				break;
			case 'MOVEGR':
				switch (IdMedioDePago){
					case "CHE":
						$scope.DisabledNumeroCuentaSocio	= true;
						$scope.oConfiguracionChequerasSeries.activado = true;
						$scope.oConfiguracionBancoCuentaSocio.activado	= false;
						$scope.oConfiguracionListaCuentaBancoSocioNegocio.seleccionado.value = undefined;
						$scope.exigeChequera = true;
						break;
					case "EBA":
						$scope.DisabledNumeroCuentaSocio	= true;
						$scope.oConfiguracionChequerasSeries.activado = true;
						$scope.exigeChequera = false;
						$scope.oConfiguracionBancoCuentaSocio.activado	= false;
						break;
					case "VVI":
						$scope.oConfiguracionBancoCuentaSocio.activado	= false;
						$scope.DisabledFolioDocumentoCorredoraDesde	= false;
						$scope.DisabledNumeroCuentaSocio		= true;
						$scope.exigeChequera = false;
						break;
					case "EFE": 
						$scope.oConfiguracionCuentaBancoCorredora.activado = false; 
						$scope.oConfiguracionCuentaBancoCorredora.requerido = false; 
						$scope.oConfiguracionBancoCuentaSocio.activado	= false;
						$scope.oConfiguracionListaCuentaBancoSocioNegocio.activado	= false;
						$scope.DisabledNumeroCuentaSocio	= true;
						$scope.exigeChequera = false;
						break;
				};
				break;
		};

		$timeout(function() {			
			bcsComunes.ventanaTamanoAutomatico(bcsComunes.ventanaWinId($scope));
		},10);		
	};
	
	$scope.limpiar = function(){
		$scope.oConfiguracionChequerasSeries.activado	= false;
		$scope.oConfiguracionBancoCuentaSocio.activado	= true;
		$scope.oConfiguracionListaCuentaBancoSocioNegocio.activado = true;		
		$scope.oConfiguracionCuentaBancoCorredora.activado = true;
		
		$scope.DisabledFolioDocumentoCorredoraDesde = true;
		$scope.DisabledNumeroCuentaSocio	= false;
		$scope.DisabledSerieDocumentoSocio 	= true;
		$scope.DisabledFolioDocumentoSocio 	= true;

		$scope.FolioDocumentoCorredoraDesde	= undefined;
		$scope.FolioDocumentoCorredoraHasta	= undefined;
	
		$scope.NumeroCuentaSocio		= undefined;
		$scope.SerieDocumentoSocio		= undefined;
		$scope.FolioDocumentoSocio		= undefined;
		$scope.ngBancoCuentaSocioNegocio = undefined;
		$scope.ngTipoCuentaSocioNegocio = undefined;
		$scope.ngNumeroCuentaSocioNegocio = undefined;
		$scope.ngMonedaCuentaSocioNegocio = undefined;
		
				
		
		$scope.kngFechaDocumentoSocio 	= util.dateInicioDia($scope.SesionFechaSistema);
	};

	$scope.confirmar = function()
	{							
		var dataCols = [];
		var formasDePago = [];
		var NumeroChequeActual = Number($scope.oConfiguracionChequerasSeries.seleccionado.NumeroChequeActual);
		var Remanente	= $scope.remanente;

		for (i=0;i<MovimientosDatasource.data().length;i++){
			if (Remanente >= 0){
				NumeroChequeActual = Number(NumeroChequeActual) + i;
				Remanente = Remanente - 1;		
			} else {
				NumeroChequeActual = 0;
				Remanente = 0;
			};
			var formaDePago = {
				NumeroReferencia				: $scope.Entidad === 'TES.IngresoEgreso' ? MovimientosDatasource.data()[i].IdIngresoEgreso : MovimientosDatasource.data()[i].IdComprobante,
				IdMedioDePago					: $scope.IdMedioDePago,
				IdModalidadDePago				: $scope.IdModalidadDePago,
				IdMoneda						: MovimientosDatasource.data()[i].IdMoneda,
				Monto							: MovimientosDatasource.data()[i].Monto,

				//Corredora
				IdCuentaBancariaCorredora		: $scope.oConfiguracionCuentaBancoCorredora.seleccionado.IdCuentaBanco || 0,
				IdTarjetaBancariaCorredora		: 0,
				IdChequera						: $scope.oConfiguracionChequerasSeries.seleccionado.IdChequera || 0,
				NumeroDocumentoCorredora		: Number(NumeroChequeActual) || 0,
				FechaDocumentoPagoCorredora		: util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema)),

				//Socio
				IdCuentaBancariaSocio			: null,
				IdBancoSocio					: MovimientosDatasource.data()[i].IdBancoSocio,
				NombreCuentaBancariaSocio		: MovimientosDatasource.data()[i].NombreBancoSocio,
				NumeroCuentaBancariaSocio       : MovimientosDatasource.data()[i].NumeroCuentaBancariaSocio,
				IdTipoCuentaBancariaSocio		: null,
				NombreTipoCuentaBancariaSocio	: null,
				

				
				
				IdTipoCuentaBancariaSocio		: null,
				NombreTipoCuentaBancariaSocio	: null,
				IdTarjetaBancariaSocio			: null,
				IdTipoTarjetaBancariaSocio		: null,
				NombreTipoTarjetaBancariaSocio	: null,
				NumeroTarjetaBancariaSocio		: null,
				NumeroSerieSocio				: $scope.SerieDocumentoSocio || null,
				NumeroDocumentoSocio			: $scope.FolioDocumentoSocio || null,
				FechaDocumentoPagoSocio			: util.dateSPdate(util.dateInicioDia($scope.kngFechaDocumentoSocio))
			}
			
			if($scope.showDatosSocioNegocio){
				if ($scope.showListaCuentasSocioNegocio){
					formaDePago.IdCuentaBancariaSocio			= $scope.oConfiguracionListaCuentaBancoSocioNegocio.seleccionado.IdCuentaBancariaSocio || null;
					formaDePago.IdBancoSocio					= $scope.oConfiguracionListaCuentaBancoSocioNegocio.seleccionado.IdBanco || null;
					formaDePago.NombreCuentaBancariaSocio		= $scope.oConfiguracionListaCuentaBancoSocioNegocio.seleccionado.NombreBanco || null;
					formaDePago.IdTipoCuentaBancariaSocio		= $scope.oConfiguracionListaCuentaBancoSocioNegocio.seleccionado.IdTipoCuentaBancaria || null;
					formaDePago.NombreTipoCuentaBancariaSocio	= $scope.oConfiguracionListaCuentaBancoSocioNegocio.seleccionado.NombreTipoCuentaBancaria || null;
					formaDePago.NumeroCuentaBancariaSocio       = $scope.NumeroCuentaSocio || null;					
				} else {
					formaDePago.IdCuentaBancariaSocio			= null,
					formaDePago.IdBancoSocio					= $scope.oConfiguracionBancoCuentaSocio.seleccionado.IdBanco || null;
					formaDePago.NombreCuentaBancariaSocio		= $scope.oConfiguracionBancoCuentaSocio.seleccionado.NombreBanco || null;
					formaDePago.NumeroCuentaBancariaSocio       = $scope.NumeroCuentaSocio != null && $scope.NumeroCuentaSocio != '' ? $scope.NumeroCuentaSocio : MovimientosDatasource.data()[i].NumeroCuentaBancariaSocio;
				}
			}
						

			formasDePago.push(formaDePago);

			dataCols.push({Id: $scope.ListadoMov[i].Id, Entidad: $scope.Entidad, Accion: ($scope.ngAgrupa? 'AGS': 'AGN') });
			dataCols.push({Id: $scope.ListadoMov[i].Id, Entidad: $scope.Entidad, Accion: ($scope.ngNetea? 'NTS': 'NTN') });
		};
		
		var datos = {};
		datos.p_IdEmpresa				= $scope.SesionIdEmpresa;
	    datos.p_IdUsuario				= $scope.SesionIdUsuario;
	    datos.p_IdModulo				= 'TES';
	    datos.p_UsaReglasNegocio		= 'S';
	    datos.p_LiquidaComprobante		= $scope.ngLiquidaComprobante? 'S': 'N';
		datos.p_FechaMovimiento			= util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
		datos.p_Entidad					= $scope.Entidad;
		datos.p_FormasDePago			= formasDePago;
		datos.p_RegistrosAProcesar		= dataCols;

		
		bcsComunes.ventanaOverlay($scope.winID, true);
		bcsServiciosTesoreria.EdicionMultipleJava(datos).$promise.then(		   
		   function(respuesta) {
				bcsComunes.ventanaOverlay($scope.winID, false);

				var promesa = bcsComunes.ventanaMsg({
									conflictos:util.parseServerResponse(respuesta)
								},$scope.winID);
				bcsComunes.ventanaPromesa(promesa).then(function(data){
					if (respuesta.P_OUT_ERRORESCONTADOR === 0){
						$scope.cancelar('true');
					};
				});
		   },
		   function(respuesta){          
				 bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
				bcsComunes.ventanaOverlay($scope.winID, false);                                 
			}
		);
	};

	$scope.cancelar = function(status)
	{					
		bcsComunes.ventanaData($scope.winID,status);
		bcsComunes.ventanaCerrar($scope.winID);
	};
});	