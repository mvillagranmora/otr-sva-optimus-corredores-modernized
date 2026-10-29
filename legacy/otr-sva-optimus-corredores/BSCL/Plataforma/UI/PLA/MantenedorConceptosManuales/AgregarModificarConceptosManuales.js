angular.module('ModuloPlataforma').controller('AgregarModificarConceptosManualesCtrl',
		function($scope,$translate,bcsComunes,bcsServicioPlataforma)
{
	
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	$scope.parametros = bcsComunes.ventanaParams($scope.winID);
	$scope.ActivarCheckPorConceptoManual = true;
	$scope.ngTipoConcepto = 'MANUAL';	
	$scope.FAC = {};
	$scope.CUS = {};
	$scope.TES = {};
	$scope.CTA = {};
	$scope.conceptosSinFlujo = [];
	$scope.showCheckSinvalidacion = false;
	$scope.AbonoClpContrapartidaHabilitado = false;

	bcsServicioPlataforma.ConceptosCheckboxSinValidacionFlujoCaja( function(respuesta) {
    	$scope.conceptosSinFlujo = respuesta;
		$scope.showCheckSinvalidacion = $scope.conceptosSinFlujo.includes($scope.parametros.IdConcepto);
	});

	bcsServicioPlataforma.Servicios().ConsultarConceptoAbonoClpContrapartida().$promise.then( function(respuesta) {
		let concepto = respuesta.d.results;
		if( concepto.length == 0 ) $scope.AbonoClpContrapartidaHabilitado = true;
		else {
			if( !!$scope.parametros.IdConcepto && $scope.parametros.IdConcepto === concepto[0].IdConcepto )
				$scope.AbonoClpContrapartidaHabilitado = true;
			else
				$scope.AbonoClpContrapartidaHabilitado = false;
		}
	});
	
	$scope.oConfiguracionEstado={
		tipo : "combobox-estado-conceptos",
		configuracion : {},
		filtros : {},
		idActual : false,
		seleccionado : false,
		idDefecto : ($scope.parametros.IdEstado!==undefined) ? $scope.parametros.IdEstado : 'VIG'
	};

	//CONFIGURACION CUSTODIA
	$scope.CUS.oConfiguracionClasificacionTipoInstrumento={
		tipo : "combobox-clasificacion-tipo-instrumento",
		configuracion : {},
		filtros : {
			IdEstado		: { "field" : "IdEstado", "operator": "eq", "value" : "ACT" },
			ExigeGarantia	: { "field"	: "ExigeGarantia","operator": "eq", "value"   : "S" },
		},
		idActual 		: false,
		seleccionado 	: false,
		idDefecto 		: $scope.parametros.IdClasificacionTipoInstrumento,
		idPostFiltro 	: false
	};


   	$scope.CUS.oConfiguracionTipoInstrumento={
		tipo : "combobox-tipo-instrumento",
		configuracion : {},
		filtros : {
			IdClasificacionTipoInstrumento 	: { "field" : "IdClasificacionTipoInstrumento", "operator": "eq", "value" : "" },
			IdEstado 						: { "field" : "IdEstado",                       "operator": "eq", "value" : "VIG" }
		},
		idActual 		: false,
		seleccionado 	: false,
		idDefecto 		: $scope.parametros.IdTipoInstrumento,
		idPostFiltro 	: false
	};  


   	$scope.CUS.oConfiguracionInstrumento={
			tipo : "combobox-instrumento",
			configuracion : {},
			filtros : {
				IdTipoInstrumento : { "field" : "IdTipoInstrumento",  "operator": "eq",  "value" : "" },
				IdInstrumento : { "field" : "IdInstrumento",  "operator": "ge",  "value" : -1 }
			},
			idActual : false,
			idDefecto : false,
			idPostFiltro: false,
			seleccionado : false,
	};

	$scope.CUS.oConfiguracionEntidadDestinoGarantia={
		tipo : "combobox-entidad-garantia",
		configuracion : {},
		filtros : {
			IdEstado		: { "field" : "IdEstadoEntidadDestinoGarantia", "operator": "eq", "value" : "ACT" }
		},
		idActual 		: false ,
		seleccionado 	: false,
		idDefecto 		: $scope.parametros.IdContraparte
	};

	$scope.CUS.oConfiguracionTipoCuentaCusto={
		tipo : "combobox-tipo-cuenta",
		configuracion : {},
		filtros : {
			IdEstado 	: { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
			IdModulo 	: { "field"   : "IdModulo","operator": "eq", "value"   : "CUS" },
		},
		idActual 		: false,
		seleccionado 	: false,
		idDefecto 		: $scope.parametros.IdTipoCuenta
	};	

	
	// FACTURACION
	$scope.FAC.ngCentroCostoConcepto			= "";
	$scope.FAC.ConceptoConfiguradoEnModulo 		= {	Configurado	: true}; 
	$scope.FAC.Indicadores 						= {	
		EsOtrosConceptos			: true,
		EsReembolso					: false
	};
	$scope.FAC.Indicadores2						= {
		EsInterfazSIG				: false
	};

	$scope.FAC.Indicadores3 = {
		Contrapartida: false
	}

	$scope.FAC.Indicadores4 = {
		AbonoClpContrapartida: false
	}

	$scope.FAC.ngCodigoProductoInterfazSIG		="";
	$scope.FAC.ConceptoConfiguradoEnAplicacion	= {	
		ConceptoConfiguradoOptimus  : true,
		ConceptoConfiguradoFiori    : false,
		ConceptoConfiguradoWs       : false
	};
	

	// CUSTODIA
	$scope.CUS.ConceptoConfiguradoEnModulo 		= {Configurado	: true	};
	$scope.CUS.UsoCustodia = {
		Titulo					: false,
		Garantia				: false,
		Prestamo				: false,
		SorteoLetras			: false,
		CargaMasivaCustodia		: false,		
	};
	$scope.CUS.Indicadores = {		
		CertificadoAutomatico   	: true
	};
	$scope.CUS.Indicadores1 = {		
		GeneraTransferenciaBoveda	: false,
		EsTransferenciaPortafolio	: false,
		EsTransferenciaCliente		: false
	};	
	$scope.CUS.Indicadores2 = {
		EsGarantiaPrendaria			: false,
		EsGarantiaVendible			: false
	};	
	$scope.CUS.Indicadores3 = {
		RetieneDividendos			: false,
		RetieneAmortizacion  		: false,
		RetieneIntereses			: false,
		FolioObligatorio			: false
	};
	$scope.CUS.ConceptoConfiguradoEnAplicacion	= {	
		ConceptoConfiguradoOptimus  : true,
		ConceptoConfiguradoFiori    : false,
		ConceptoConfiguradoWs       : false
	};
	

	// TESORERIA
	$scope.TES.ConceptoConfiguradoEnModulo  = {Configurado	: true	};
	$scope.TES.UsoTesoreria = {
		Comprobante : false,
		Deposito	: true,
		CargaMasiva	: false,		
	};
	
	$scope.TES.Indicadores	= {
		DisponibleIngresoManual	: true,
		GeneraComprobanteAutomaticamente : true
	
	};
	$scope.TES.ConceptoConfiguradoEnAplicacion	= {	
		ConceptoConfiguradoOptimus  : true,
		ConceptoConfiguradoFiori    : false,
		ConceptoConfiguradoWs       : false
	};
	
	$scope.TES.Validaciones = {	NoValidaSaldoCaja : false};
	
	$scope.TES.ConceptoConfiguradoEnParametrosSistema = { Configurado : true};
	// CUENTA INVERSION
	$scope.CTA.ConceptoConfiguradoEnModulo 		= {Configurado	: true	};
	//CELLZORRO-11 -MBI Mejoramiento Cartola - Clasificación de cargos y abonos
	$scope.CTA.AfectaRentabilidad  = {AfectaRentabilidad : true}; 
	$scope.CTA.ConceptoConfiguradoEnAplicacion	= {	
		ConceptoConfiguradoOptimus  : true,
		ConceptoConfiguradoFiori    : false,
		ConceptoConfiguradoWs       : false,
		
	};
	$scope.CTA.Indicadores	= {DisponibleIngresoManual	: true	};

	$scope.$watch("FAC.ConceptoConfiguradoEnModulo.Configurado", function(nval, oval){
		if(nval===false){
			$scope.FAC.Indicadores.EsOtrosConceptos									= false;
			$scope.FAC.Indicadores.EsReembolso										= false;
			$scope.FAC.Indicadores2.EsInterfazSIG									= false;
			$scope.FAC.Indicadores3.Contrapartida									= false;
			$scope.FAC.Indicadores4.AbonoClpContrapartida							= false;
			$scope.FAC.ngCentroCostoConcepto										= "";
			$scope.FAC.ngCodigoProductoInterfazSIG									= "";
			$scope.FAC.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoOptimus	= false;
			$scope.FAC.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoFiori		= false;
			$scope.FAC.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoWs	    = false;
		}
	});
	$scope.$watch("FAC.Indicadores.EsOtrosConceptos", function(nval , oval){
		if(nval === false){
			$scope.FAC.Indicadores2.EsInterfazSIG									= false;
			$scope.FAC.ngCodigoProductoInterfazSIG									= "";
		}
	})
	$scope.$watch("FAC.Indicadores2.EsInterfazSIG", function(nval , oval){
		if(nval === false){
			$scope.FAC.ngCodigoProductoInterfazSIG									= "";
		}
	})
	$scope.$watch("CUS.ConceptoConfiguradoEnModulo.Configurado", function(nval, oval){
		if(nval===false){
			$scope.CUS.UsoCustodia.Titulo											= false;
			$scope.CUS.UsoCustodia.Garantia											= false;
			$scope.CUS.UsoCustodia.Prestamo											= false;
			$scope.CUS.UsoCustodia.SorteoLetras										= false;
			$scope.CUS.UsoCustodia.CargaMasivaCustodia								= false;
			$scope.CUS.Indicadores.CertificadoAutomatico							= false;
			$scope.CUS.Indicadores1.GeneraTransferenciaBoveda						= false;
			$scope.CUS.Indicadores1.EsTransferenciaPortafolio						= false;
			$scope.CUS.Indicadores1.EsTransferenciaCliente							= false;
			$scope.CUS.Indicadores2.EsGarantiaPrendaria								= false;
			$scope.CUS.Indicadores2.EsGarantiaVendible								= false;
			$scope.CUS.Indicadores3.RetieneDividendos								= false;
			$scope.CUS.Indicadores3.RetieneAmortizacion								= false;
			$scope.CUS.Indicadores3.RetieneIntereses								= false;
			$scope.CUS.Indicadores3.FolioObligatorio								= false;
			$scope.CUS.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoOptimus	= false;
			$scope.CUS.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoFiori		= false;
			$scope.CUS.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoWs	    = false;
			$scope.CUS.oConfiguracionClasificacionTipoInstrumento.idActual			= false;
			$scope.CUS.oConfiguracionTipoInstrumento.idActual			= false;
			$scope.CUS.oConfiguracionInstrumento.idActual			= false;
			$scope.CUS.oConfiguracionEntidadDestinoGarantia.idActual			= false;
			$scope.CUS.oConfiguracionTipoCuentaCusto.idActual			= false;
		}
	});
	$scope.$watch("CTA.ConceptoConfiguradoEnModulo.Configurado", function(nval, oval){
		if(nval===false){
			$scope.CTA.Indicadores.DisponibleIngresoManual  						= false;
			$scope.CTA.AfectaRentabilidad.AfectaRentabilidad						= false; 
			//CELLZORRO-11 -MBI Mejoramiento Cartola - Clasificación de cargos y abonos
			$scope.CTA.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoOptimus	= false;
			$scope.CTA.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoFiori 	= false;
			$scope.CTA.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoWs 	    = false;
		}
	});
	$scope.$watch("TES.ConceptoConfiguradoEnModulo.Configurado", function(nval, oval){
		if(nval===false){
			$scope.TES.UsoTesoreria.Comprobante										= false;
			$scope.TES.UsoTesoreria.Deposito										= false;
			$scope.TES.UsoTesoreria.CargaMasiva										= false;
			$scope.TES.Indicadores.DisponibleIngresoManual							= false;
			$scope.TES.Indicadores.GeneraComprobanteAutomaticamente					= false;
			$scope.TES.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoOptimus	= false;
			$scope.TES.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoFiori  	= false;
			$scope.TES.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoWs  	    = false;
		}
	});
		
	
	//MODIFICACION
	if (!!$scope.parametros.IdConcepto){	
		$scope.ngCodigoConceptoDisabled 	= true;
		$scope.botonAgregarConceptoManual 	= false;
		$scope.botonModificarConceptoManual = true;
		
		//----------Generico
		$scope.ngCodigoConcepto 		= $scope.parametros.IdConcepto;
		$scope.ngTipoConcepto			= $scope.parametros.TipoConcepto;
		$scope.ngNombreConcepto 		= $scope.parametros.NombreConcepto;
		$scope.ngVisiblePorEjecutivo	= $scope.parametros.VisiblePorEjecutivoCUS === 'S'? true:false;
		$scope.idTipoCriterioNeteo 		= $scope.parametros.IdTipoCriterioNeteo;
		$scope.idTipoCriterioNeteoDef 	= $scope.parametros.IdTipoCriterioNeteo;
		$scope.ngNombreConceptoCartola 	= $scope.parametros.NombreConceptoCartola;
		$scope.ngExclusivoCartola	    = $scope.parametros.ExclusivoCartola === 'S'? true:false;
		
		$scope.CUS.ConceptoConfiguradoEnModulo.Configurado = $scope.parametros.ConfiguradoEnModuloCUSTra === 'SI' ? true:false;
		$scope.CTA.ConceptoConfiguradoEnModulo.Configurado = $scope.parametros.ConfiguradoEnModuloCTATra === 'SI' ? true:false;
		$scope.FAC.ConceptoConfiguradoEnModulo.Configurado = $scope.parametros.ConfiguradoEnModuloFACTra === 'SI' ? true:false;
		$scope.TES.ConceptoConfiguradoEnModulo.Configurado = $scope.parametros.ConfiguradoEnModuloTESTra === 'SI' ? true:false;


		//----------Facturacion
		$scope.FAC.Indicadores.EsOtrosConceptos									= $scope.parametros.EsOtrosConceptos === 'S' ? true:false;
		$scope.FAC.Indicadores.EsReembolso										= $scope.parametros.EsReembolso === 'S' ? true:false;
		$scope.FAC.Indicadores2.EsInterfazSIG									= $scope.parametros.InterfazSIG === 'SI' ? true:false;
		$scope.FAC.Indicadores3.Contrapartida									= $scope.parametros.Contrapartida === 'S' ? true:false;
		$scope.FAC.Indicadores4.AbonoClpContrapartida							= $scope.parametros.AbonoClpContrapartida === 'S' ? true:false;
		$scope.FAC.ngCentroCostoConcepto										= $scope.parametros.CentroCosto ;
		$scope.FAC.ngCodigoProductoInterfazSIG									= $scope.parametros.CodigoProducto;
		$scope.FAC.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoOptimus	= $scope.parametros.ConceptoConfiguradoOptimusFac 	=== 'S' ? true:false;
		$scope.FAC.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoFiori		= $scope.parametros.ConceptoConfiguradoFioriFac 	=== 'S' ? true:false;
		$scope.FAC.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoWs	    = $scope.parametros.ConceptoConfiguradoWsFac 	=== 'S' ? true:false;
		
		//----------Tesoreria
		$scope.TES.UsoTesoreria.Comprobante										= $scope.parametros.UsoTES === 'Comprobante' ? true:false;
		$scope.TES.UsoTesoreria.Deposito										= $scope.parametros.UsoTES === 'Deposito' ? true:false;
		$scope.TES.UsoTesoreria.CargaMasiva										= $scope.parametros.UsoTES === 'CargaMasiva' ? true:false;
		$scope.TES.Indicadores.DisponibleIngresoManual							= $scope.parametros.DisponibleIngresoManualTES === 'S' ? true:false;
		$scope.TES.Indicadores.GeneraComprobanteAutomaticamente					= $scope.parametros.GeneraComprobanteAutomaticamenteTES === 'S' ? true:false;
		$scope.TES.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoOptimus	= $scope.parametros.ConceptoConfiguradoOptimusTes === 'S' ? true:false;
		$scope.TES.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoFiori  	= $scope.parametros.ConceptoConfiguradoFioriTes === 'S' ? true:false;
		$scope.TES.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoWs  	    = $scope.parametros.ConceptoConfiguradoWsTes === 'S' ? true:false;
		$scope.TES.Validaciones.NoValidaSaldoCaja								= $scope.parametros.NoValidaSaldoCajaTES === 'S' ? true:false;
		$scope.TES.ConceptoAbonoReplica											= $scope.parametros.ConceptoAbonoReplica ?? "";
		$scope.TES.ConceptoCargoReplica											= $scope.parametros.ConceptoCargoReplica ?? "";
		
		//----------Cuenta inversion
		$scope.CTA.Indicadores.DisponibleIngresoManual  						= $scope.parametros.DisponibleIngresoManualCTA === 'S' ? true:false;
		//CELLZORRO-11 -MBI Mejoramiento Cartola - Clasificación de cargos y abonos
		$scope.CTA.AfectaRentabilidad.AfectaRentabilidad						= $scope.parametros.AfectaRentabilidadTES === 'S' ? true:false;		
		$scope.CTA.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoOptimus	= $scope.parametros.ConceptoConfiguradoOptimusCta === 'S' ? true:false;
		$scope.CTA.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoFiori 	= $scope.parametros.ConceptoConfiguradoFioriCta === 'S' ? true:false;
		$scope.CTA.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoWs 	    = $scope.parametros.ConceptoConfiguradoWsCta === 'S' ? true:false;
		
		//----------Custodia
		$scope.CUS.IdConceptoCustodia											= $scope.parametros.IdConceptoCustodia;
		$scope.CUS.UsoCustodia.Titulo											= $scope.parametros.UsoCUS === 'Titulo' ? true:false;
		$scope.CUS.UsoCustodia.Garantia											= $scope.parametros.UsoCUS === 'Garantia' ? true:false;		
		$scope.CUS.UsoCustodia.Prestamo											= $scope.parametros.UsoCUS === 'Prestamo' ? true:false;
		$scope.CUS.UsoCustodia.SorteoLetras										= $scope.parametros.UsoCUS === 'SorteoLetras' ? true:false;
		$scope.CUS.UsoCustodia.CargaMasivaCustodia								= $scope.parametros.UsoCUS === 'CargaMasivaCustodia' ? true:false;

		$scope.CUS.Indicadores.CertificadoAutomatico							= $scope.parametros.ConCertificadoAutomatico 	=== 'S' ? true:false;		
		$scope.CUS.Indicadores1.GeneraTransferenciaBoveda						= $scope.parametros.GeneraTransferenciaBoveda	=== 'S' ? true:false;
		$scope.CUS.Indicadores1.EsTransferenciaPortafolio						= $scope.parametros.EsTransferenciaPortafolio 	=== 'S' ? true:false;
		$scope.CUS.Indicadores1.EsTransferenciaCliente							= $scope.parametros.EsTransferenciaCliente 		=== 'S' ? true:false;		
		$scope.CUS.Indicadores2.EsGarantiaPrendaria								= $scope.parametros.EsGarantiaPrendaria			=== 'S' ? true:false;
		$scope.CUS.Indicadores2.EsGarantiaVendible								= $scope.parametros.EsGarantiaVendible 			=== 'S' ? true:false;		
		$scope.CUS.Indicadores3.RetieneDividendos								= $scope.parametros.RetieneDividendos 			=== 'S' ? true:false;		
		$scope.CUS.Indicadores3.RetieneAmortizacion								= $scope.parametros.RetieneAmortizacion		    === 'S' ? true:false;
		$scope.CUS.Indicadores3.RetieneIntereses								= $scope.parametros.RetieneIntereses			=== 'S' ? true:false;
		$scope.CUS.Indicadores3.FolioObligatorio								= $scope.parametros.FolioObligatorio 			=== 'S' ? true:false;
		$scope.CUS.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoOptimus	= $scope.parametros.ConceptoConfiguradoOptimusCus === 'S' ? true:false;
		$scope.CUS.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoFiori		= $scope.parametros.ConceptoConfiguradoFioriCus === 'S' ? true:false;
		$scope.CUS.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoWs	    = $scope.parametros.ConceptoConfiguradoWsCus === 'S' ? true:false;
		
		if ($scope.parametros.TipoConcepto === 'SISTEMA') {
			$scope.ActivarCheckPorConceptoManual = false;
			
			//$scope.CUS.UsoCustodia.Titulo	= false;
			$scope.CUS.UsoCustodia.Garantia	= false;
		}
	}
	else{
		$scope.ngCodigoConceptoDisabled = false;
		$scope.botonAgregarConceptoManual = true;
		$scope.botonModificarConceptoManual = false;
	};
	$scope.CUS.IdInstrumento = -1;
	$scope.$watch("CUS.oConfiguracionInstrumento.filtros.IdTipoInstrumento", function(nval, oval){
		if($scope.parametros.IdInstrumento!==undefined && nval.value!==undefined && nval.value!==""){
			$scope.CUS.IdInstrumento = parseInt($scope.parametros.IdInstrumento);
		}
	});
	
	//FUNCIONES
	$scope.obtenerUso = function(USO){
		$scope.NombreUso = '';
		switch (USO){
			case "CUSTODIA":
				if ($scope.CUS.UsoCustodia.Titulo)
					$scope.NombreUso = 'Titulo';
				if ($scope.CUS.UsoCustodia.Garantia)
					$scope.NombreUso = 'Garantia';
				if ($scope.CUS.UsoCustodia.Prestamo)
					$scope.NombreUso = 'Prestamo';
				if ($scope.CUS.UsoCustodia.SorteoLetras)
					$scope.NombreUso = 'SorteoLetras';
				if ($scope.CUS.UsoCustodia.CargaMasivaCustodia)
					$scope.NombreUso = 'CargaMasivaCustodia';
				break;
			case "TESORERIA":
				if ($scope.TES.UsoTesoreria.Comprobante)
					$scope.NombreUso = 'Comprobante';
				if ($scope.TES.UsoTesoreria.Deposito)
					$scope.NombreUso = 'Deposito';
				if ($scope.TES.UsoTesoreria.CargaMasiva)
					$scope.NombreUso = 'CargaMasiva';
				break;
		};
		return $scope.NombreUso;
	};
	
	$scope.fnPushCustodia = function(dataCols,Uso){
		var v_EsGarantiaPrendaria='N';
		var v_EsGarantiaVendible='N';
		var v_RetieneDividendos='N';
		var v_RetieneAmortizacion='N';
		var v_RetieneIntereses='N';
		var v_FolioObligatorio='N';
		var v_GeneraTransferenciaBoveda='N';
		var v_EsTransferenciaPortafolio='N';
		var v_EsTransferenciaCliente='N';
		
		if (Uso==='Titulo')
		{
			v_GeneraTransferenciaBoveda		= $scope.CUS.Indicadores1.GeneraTransferenciaBoveda ? 'S':'N';
			v_EsTransferenciaPortafolio		= $scope.CUS.Indicadores1.EsTransferenciaPortafolio ? 'S':'N';
			v_EsTransferenciaCliente		= $scope.CUS.Indicadores1.EsTransferenciaCliente ? 'S':'N';
		};
		
		if (Uso==='Garantia'){
			v_EsGarantiaPrendaria			= $scope.CUS.Indicadores2.EsGarantiaPrendaria ? 'S':'N';
			v_EsGarantiaVendible			= $scope.CUS.Indicadores2.EsGarantiaVendible ? 'S':'N';
		};
					
		if (Uso==='Garantia' || Uso=== 'Titulo' || Uso==='Prestamo' || Uso==='SorteoLetras' || Uso==='CargaMasivaCustodia'){				
			v_RetieneDividendos				= $scope.CUS.Indicadores3.RetieneDividendos ? 'S': 'N';			
			v_RetieneAmortizacion			= $scope.CUS.Indicadores3.RetieneAmortizacion ? 'S': 'N';
			v_RetieneIntereses				= $scope.CUS.Indicadores3.RetieneIntereses ? 'S': 'N';
			v_FolioObligatorio				= $scope.CUS.Indicadores3.FolioObligatorio ? 'S': 'N';
		};

		dataCols.push({"IdConcepto"						: $scope.ngCodigoConcepto,
					   "IdConceptoCustodia"				: $scope.CUS.IdConceptoCustodia,
					   "USO"							: Uso,
					   "ConceptoConfiguradoEnModulo"	: $scope.CUS.ConceptoConfiguradoEnModulo.Configurado ? 'S':'N',
					   "CertificadoAutomatico"			: $scope.CUS.Indicadores.CertificadoAutomatico ? 'S':'N',
					   "GeneraTransferenciaBoveda"		: v_GeneraTransferenciaBoveda,
					   "EsTransferenciaPortafolio"		: v_EsTransferenciaPortafolio,
					   "EsTransferenciaCliente"			: v_EsTransferenciaCliente,					   
					   "EsGarantiaPrendaria"			: v_EsGarantiaPrendaria,
					   "EsGarantiaVendible"				: v_EsGarantiaVendible,					   
					   "IdClasificacionTipoInstrumento"	: $scope.CUS.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento || null,
					   "IdTipoInstrumento"				: $scope.CUS.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento || null,
					   "IdInstrumento"					: $scope.CUS.oConfiguracionInstrumento.idActual || null,
					   "IdContraparte"					: $scope.CUS.oConfiguracionEntidadDestinoGarantia.seleccionado.IdContraparte || null,
					   "IdTipoCuenta"					: $scope.CUS.oConfiguracionTipoCuentaCusto.seleccionado.IdTipoCuenta || null,					   
					   "RetieneDividendos"				: v_RetieneDividendos,					   
					   "RetieneAmortizacion"			: v_RetieneAmortizacion,
					   "RetieneIntereses"				: v_RetieneIntereses,
					   "FolioObligatorio"				: v_FolioObligatorio,
					   "ConceptoConfiguradoOptimus"		: $scope.CUS.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoOptimus ? 'S': 'N',
					   "ConceptoConfiguradoFiori"		: $scope.CUS.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoFiori ? 'S': 'N',
					   "ConceptoConfiguradoWs"		    : $scope.CUS.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoWs ? 'S': 'N',
					   "ConceptoAbonoReplica"				: $scope.TES.ConceptoAbonoReplica ?? "",
					   "ConceptoCargoReplica"				: $scope.TES.ConceptoCargoReplica ?? ""	
				 });
		return dataCols;
	};
	
	//CONFIGURACIONES
	$scope.configuracionFacturacion = function(){
		var dataCols = [];
		dataCols.push({	"IdConcepto"				 : $scope.ngCodigoConcepto,
						"CentroCosto"				 : $scope.FAC.ngCentroCostoConcepto,
						"CodigoProducto"			 : $scope.FAC.ngCodigoProductoInterfazSIG,
						"EsOtrosConceptos"		     : $scope.FAC.Indicadores.EsOtrosConceptos ? 'S' : 'N',
						"EsReembolso"				 : $scope.FAC.Indicadores.EsReembolso ? 'S' : 'N',
						"EsInterfazSIG"				 : $scope.FAC.Indicadores2.EsInterfazSIG ? 'S':'N',
						"Contrapartida"				 : $scope.FAC.Indicadores3.Contrapartida ? 'S' : 'N',
						"AbonoClpContrapartida"		 : $scope.FAC.Indicadores4.AbonoClpContrapartida ? 'S' : 'N',			
						"ConceptoConfiguradoEnModulo": $scope.FAC.ConceptoConfiguradoEnModulo.Configurado ? 'S' : 'N',
						"ConceptoConfiguradoOptimus" : $scope.FAC.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoOptimus ? 'S' : 'N',
						"ConceptoConfiguradoFiori"   : $scope.FAC.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoFiori ? 'S' : 'N',
						"ConceptoConfiguradoWs"   	 : $scope.FAC.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoWs ? 'S' : 'N'
					 });
		return dataCols;
	};
	$scope.configuracionCuentaInversion = function(){
		var dataCols = [];
		dataCols.push({"IdConcepto"						: $scope.ngCodigoConcepto,
					   "DisponibleIngresoManual"		: $scope.CTA.Indicadores.DisponibleIngresoManual ? 'S' : 'N',
 					   //CELLZORRO-11 -MBI Mejoramiento Cartola - Clasificación de cargos y abonos							   
					   "AfectaRentabilidad"             : $scope.CTA.AfectaRentabilidad.AfectaRentabilidad ? 'S' : 'N',							   
					   "ConceptoConfiguradoEnModulo"	: $scope.CTA.ConceptoConfiguradoEnModulo.Configurado ? 'S':'N',
					   "ConceptoConfiguradoOptimus"  	: $scope.CTA.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoOptimus ? 'S':'N',
					   "ConceptoConfiguradoFiori"  	    : $scope.CTA.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoFiori ? 'S':'N',
					   "ConceptoConfiguradoWs"  	    : $scope.CTA.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoWs ? 'S':'N'
					 });
		return dataCols;
	};
	$scope.configuracionTesoreria = function(){
		if($scope.TES.ConceptoConfiguradoEnModulo.Configurado){
			if ($scope.obtenerUso('TESORERIA') === ''){
				bcsComunes.msg("error","error",$translate.instant('UI_INDICAR_USO_TESORERIA'));
				return;
			};	
		}

		var dataCols = [];
		if ($scope.obtenerUso('CUSTODIA') !== 'Garantia'){
			dataCols.push({"IdConcepto"				: $scope.ngCodigoConcepto,
				   "USO"							: $scope.obtenerUso('TESORERIA'),				  
				   "DisponibleIngresoManual"		: $scope.TES.Indicadores.DisponibleIngresoManual ? 'S' : 'N',
				   "ConceptoConfiguradoEnModulo"	: $scope.TES.ConceptoConfiguradoEnModulo.Configurado ? 'S':'N',
				   "ConceptoConfiguradoOptimus"  	: $scope.TES.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoOptimus ? 'S':'N',
				   "ConceptoConfiguradoFiori"  	    : $scope.TES.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoFiori ? 'S':'N',
				   "ConceptoConfiguradoWs"  	    : $scope.TES.ConceptoConfiguradoEnAplicacion.ConceptoConfiguradoWs ? 'S':'N',
				   "NoValidaSaldoCaja"  	    	: $scope.TES.Validaciones.NoValidaSaldoCaja ? 'S':'N',
				   "GeneraComprobanteAutomaticamente": $scope.TES.Indicadores.GeneraComprobanteAutomaticamente ? 'S' : 'N',
				 });
		};	
		
		return dataCols;
	};
	
	$scope.configuracionCustodia = function(){
		var dataCols = [];
		if($scope.CUS.ConceptoConfiguradoEnModulo.Configurado){
			if ($scope.obtenerUso('CUSTODIA') === ''){
				bcsComunes.msg("error","error",$translate.instant('UI_INDICAR_USO_CUSTODIA'));
				return;
			};
			if ($scope.CUS.UsoCustodia.Titulo)
				dataCols=$scope.fnPushCustodia(dataCols,'Titulo');
			if ($scope.CUS.UsoCustodia.Garantia)
				dataCols=$scope.fnPushCustodia(dataCols,'Garantia');
			if ($scope.CUS.UsoCustodia.Prestamo)
				dataCols=$scope.fnPushCustodia(dataCols,'Prestamo');
			if ($scope.CUS.UsoCustodia.SorteoLetras)
				dataCols=$scope.fnPushCustodia(dataCols,'SorteoLetras');
			if ($scope.CUS.UsoCustodia.CargaMasivaCustodia)
				dataCols=$scope.fnPushCustodia(dataCols,'CargaMasivaCustodia');

		}else{
			dataCols=$scope.fnPushCustodia(dataCols,'');
		}

		
		return dataCols;
	};
	
	//OPCIONES
	$scope.agregarConceptoManual = function(){
		//if ($scope.ngCodigoConcepto === '' || $scope.ngCodigoConcepto === undefined || $scope.ngNombreConcepto === '' || $scope.ngNombreConcepto === undefined){
		//	bcsComunes.msg("error","error",$translate.instant('UI_INDICAR_CODIGO_Y_NOMBRE_CONCEPTO'));
		//	return;
		//}; -------------------------------------------- Se incorpora la validacion del campo NombreConceptoCartola --------------------------------------------
		if ($scope.ngCodigoConcepto === '' || $scope.ngCodigoConcepto === undefined || $scope.ngNombreConcepto === '' || $scope.ngNombreConcepto === undefined || $scope.ngNombreConceptoCartola === '' || $scope.ngNombreConceptoCartola === undefined){
			bcsComunes.msg("error","error",$translate.instant('UI_INDICAR_CODIGO_NOMBRE_CONCEPTO_Y_NOMBRE_CONCEPTO_CARTOLA'));
			return;
		};
		
		var dataCols = [];
		dataCols.push({"IdConcepto"						: $scope.ngCodigoConcepto,
					   "Nombre"							: $scope.ngNombreConcepto,
					   "VisiblePorEjecutivo"			: $scope.ngVisiblePorEjecutivo ? 'S':'N',
					   "IdEstado"						: $scope.oConfiguracionEstado.seleccionado.IdEstado,
					   "IdTipoCriterioNeteo"			: $scope.idTipoCriterioNeteo || 0,
					   "NombreConceptoCartola"			: $scope.ngNombreConceptoCartola,
					   "ExclusivoCartola"			    : $scope.ngExclusivoCartola ? 'S':'N',
					   "ConceptoAbonoReplica"				: $scope.TES.ConceptoAbonoReplica ?? "",
					   "ConceptoCargoReplica"				: $scope.TES.ConceptoCargoReplica ?? ""	
					 });
		
		
		//VARIABLES DEL SERVICIO
		var datos = {};
		datos.p_IdEmpresa						= $scope.SesionIdEmpresa;
		datos.p_IdUsuario						= $scope.SesionIdUsuario;
		datos.p_IdModulo						= 'PLA';
		datos.p_UsaReglasNegocio				= 'S';
		datos.p_Conceptos						= dataCols;
		datos.p_configuracionFacturacion		= $scope.configuracionFacturacion();
		datos.p_configuracionCustodia			= $scope.configuracionCustodia();
		datos.p_configuracionTesoreria			= $scope.configuracionTesoreria();
		datos.p_configuracionCuentaInversion	= $scope.configuracionCuentaInversion();
		
		if (typeof datos.p_configuracionCustodia === 'undefined' || typeof datos.p_configuracionTesoreria === 'undefined' ||
			typeof datos.p_configuracionCuentaInversion === 'undefined'){
			return;
		}
			
		
		bcsComunes.ventanaOverlay($scope.winID, true);
		bcsServicioPlataforma.Servicios().CrearConceptoManual(datos).$promise.then(
			function(respuesta){
				bcsComunes.ventanaOverlay($scope.winID, false);								
				
				var Msg = bcsComunes.ventanaMsg({
							conflictos:util.parseServerResponse(respuesta)
						  },$scope.winID);

				  bcsComunes.ventanaPromesa(Msg).then(function(){
					if (respuesta.P_OUT_ERRORESCONTADOR === 0){
						$scope.Limpiar();
						$scope.cancelar();
					};
				  });				
			},
			function(respuesta){
				bcsComunes.ventanaOverlay($scope.winID, false);
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_AGREGAR_CONCEPTO"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
			}
		);
	};

	$scope.modificarConceptoManual = function(){
		if ($scope.ngNombreConcepto === '' || $scope.ngNombreConcepto === undefined){
			bcsComunes.msg("error","error",$translate.instant('UI_INDICAR_NOMBRE_CONCEPTO'));
			return;
		};

		
		var dataCols = [];
		dataCols.push({"IdConcepto"						: $scope.ngCodigoConcepto,
					   "Nombre"							: $scope.ngNombreConcepto,
					   "VisiblePorEjecutivo"			: $scope.ngVisiblePorEjecutivo ? 'S':'N',
					   "IdEstado"						: $scope.oConfiguracionEstado.seleccionado.IdEstado,
					   "IdTipoCriterioNeteo"			: $scope.idTipoCriterioNeteo || 0,
					   "NombreConceptoCartola"			: $scope.ngNombreConceptoCartola,
					   "ExclusivoCartola"			    : $scope.ngExclusivoCartola ? 'S':'N',
					   "ConceptoAbonoReplica"				: $scope.TES.ConceptoAbonoReplica ?? "",
					   "ConceptoCargoReplica"				: $scope.TES.ConceptoCargoReplica ?? ""	
					 });
		
		
		//VARIABLES DEL SERVICIO
		var datos = {};
		datos.p_IdEmpresa						= $scope.SesionIdEmpresa;
		datos.p_IdUsuario						= $scope.SesionIdUsuario;
		datos.p_IdModulo						= 'PLA';
		datos.p_UsaReglasNegocio				= 'S';
		datos.p_Conceptos						= dataCols;
		datos.p_configuracionFacturacion		= $scope.configuracionFacturacion();
		datos.p_configuracionCustodia			= $scope.configuracionCustodia();
		datos.p_configuracionTesoreria			= $scope.configuracionTesoreria();
		datos.p_configuracionCuentaInversion	= $scope.configuracionCuentaInversion();		

		if (typeof datos.p_configuracionCustodia === 'undefined' || typeof datos.p_configuracionTesoreria === 'undefined' ||
			typeof datos.p_configuracionCuentaInversion === 'undefined'){
			return;
		}
		
		bcsComunes.ventanaOverlay($scope.winID, true);
		bcsServicioPlataforma.Servicios().EditarConceptoManual(datos).$promise.then(
			function(respuesta){
				bcsComunes.ventanaOverlay($scope.winID, false);
				var Msg = bcsComunes.ventanaMsg({
							conflictos:util.parseServerResponse(respuesta)
						  },$scope.winID);
				var promesa=bcsComunes.ventanaPromesa(Msg);

				promesa.then(function(data){				
					if (respuesta.P_OUT_ERRORESCONTADOR === 0){
						$scope.Limpiar();
						$scope.cancelar();
					};
				});
			},
			function(respuesta){
				bcsComunes.ventanaOverlay($scope.winID, false);
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_EDITAR_CONCEPTO"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
			}
		);
	};
	
	$scope.Limpiar = function(){
		$scope.ngCodigoConcepto = '';
		$scope.ngNombreConcepto	= '';
		$scope.ngNombreConceptoCartola	= '';
	};

	$scope.cancelar = function(status)
	{					
		bcsComunes.ventanaData($scope.winID,status);
		bcsComunes.ventanaCerrar($scope.winID);
	};
});