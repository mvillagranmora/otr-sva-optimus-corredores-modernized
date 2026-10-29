angular.module('ModuloFFM').controller('IngresarInstrumentoFFMCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,$timeout){
	var ingresarInstrumentoFFM = this;  
	ingresarInstrumentoFFM.etapa='inicio';
	ingresarInstrumentoFFM.workingNet= { };
	ingresarInstrumentoFFM.formData = {};
	ingresarInstrumentoFFM.formData.decimalesCantidad = 2;
	ingresarInstrumentoFFM.formData.decimalesMonto = 2;
	ingresarInstrumentoFFM.winID=bcsComunes.ventanaWinId($scope);
	ingresarInstrumentoFFM.Moneda = $scope.SesionIdMonedaSistema;
	ingresarInstrumentoFFM.datosAntes = {};
	ingresarInstrumentoFFM.datosDespues = {};
	ingresarInstrumentoFFM.TipoFondo = {};
	$scope.parametros = bcsComunes.ventanaParams(ingresarInstrumentoFFM.winID);
	ingresarInstrumentoFFM.parametros = $scope.parametros;
	ingresarInstrumentoFFM.existeRegimen = 'N';
	ingresarInstrumentoFFM.FormValido = false;
	ingresarInstrumentoFFM.Edicion = ingresarInstrumentoFFM.parametros.Edicion || 'S';
	ingresarInstrumentoFFM.porcentajeOptions = {min:0,max:100};
	ingresarInstrumentoFFM.datosDespues.PatrimonioAltosUI = false;
	ingresarInstrumentoFFM.datosDespues.MontoPatrimonioEmisor = 0;
	ingresarInstrumentoFFM.datosDespues.FechaValorCuota = util.dateInicioDia($scope.SesionFechaSistema);
	ingresarInstrumentoFFM.agregarValorCuota = true;
	
	ingresarInstrumentoFFM.datosAntes.IdTipoInstrumento = 'FFM';
		
	$scope.placeholder = {};
	$scope.placeholder.documentoIdentidad = $scope.$root.SesionValoresDefectoSistema.PLACEHOLDER_RUT;
	
	ingresarInstrumentoFFM.IdSocioDeNegocio = 0;
	var conf = bcsConfiguracionFFM.getDataSourceFondo();
	var confRegimen = bcsConfiguracionFFM.getDataSourceConsultaRegimen();
	var confRegimenSerieFondo = bcsConfiguracionFFM.getDataSourceConsultaRegimenSerieFondo();
	ingresarInstrumentoFFM.matrizRegimen = {};
	ingresarInstrumentoFFM.checkRegimen = {};
	ingresarInstrumentoFFM.regimenFondoMutuoUI = {};
	ingresarInstrumentoFFM.documentoIdentidadFondo = {};
	ingresarInstrumentoFFM.documentoIdentidadFondoOriginal = {};
	ingresarInstrumentoFFM.documentoIdentidadFondo.Valido = false;
	ingresarInstrumentoFFM.documentoIdentidadFondoOriginal.Valido = false;
	
	ingresarInstrumentoFFM.configuracionFechas = {
			Aporte: {
				FechaCurse : {
					bloqueo : false,
					calculaValutaMoneyMarket : function(){
						return 0;
					}
				},
				FechaValorCuota : {
					bloqueo : false,
					calculaValutaMoneyMarket : function(){
						return -1;
					},
					configuracion :  { min: -1 }
				},
				FechaCertificacion : {
					bloqueo : false,
					validacionValutaMoneyMarket : function(valutaCertificacion){
						return valutaCertificacion > 0;
					}
					
				},
				FechaRetencion : {
					bloqueo : false,
				}
			},
			Rescate: {
				FechaCurse : {
					bloqueo : false,
					calculaValutaMoneyMarket : function(){
						return 0;
					}
				},
				FechaValorCuota : {
					bloqueo : false,
					calculaValutaMoneyMarket : function(valutaCertificacion){
						return valutaCertificacion - 1;
					}
				},
				FechaCertificacion : {
					bloqueo : false,
					validacionValutaMoneyMarket : function(valutaCertificacion){
						return valutaCertificacion > 0;
					}
				},
				FechaRetencion : {
					bloqueo : false,
				}
			},
			esValutaMoneyMarketAporte: function(esMoneyMarket){
				return esMoneyMarket;
			},
			esValutaMoneyMarketRescate: function(idMercadoFondoMutuo, esMoneyMarket){
				return idMercadoFondoMutuo === 'RF' || esMoneyMarket;
			}
	}
	
	confRegimen.query({filter:{field: "IdEstado", operator: "eq", value: 'ACT'}}).then(function(){
		if(confRegimen.data().length > 0){
			var datos = confRegimen.data();
			$.each(datos,function(key,value){
				ingresarInstrumentoFFM.matrizRegimen[value.IdRegimenFondoMutuo] = value;
				ingresarInstrumentoFFM.regimenFondoMutuoUI[value.IdRegimenFondoMutuo] = $translate.instant('UI_NO');
				if(!$scope.parametros.IdSerieFondo && value.EsPorDefecto === 'S'){
					ingresarInstrumentoFFM.checkRegimen[value.IdRegimenFondoMutuo] = true;
				}
				bcsComunes.ventanaTamanoAutomatico(ingresarInstrumentoFFM.winID,10);
			});
		}
	});
	
	$scope.ingresarInstrumentoFFM.oConfiguracionMedioDePagoCargo={
			tipo : "combobox-medio-pago",
			configuracion : {},
			filtros : {},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.ingresarInstrumentoFFM.oConfiguracionMedioDePagoAbono={
			tipo : "combobox-medio-pago-tesoreria",
			configuracion : {},
			filtros : {},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.ingresarInstrumentoFFM.oClientePortafolio={
		tipo : "combobox-cliente-portafolio",
		configuracion :{},
		idDefecto : false,
		idActual : false,
		seleccionado : false,
		filtros: {
			IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value" : $scope.SesionIdEmpresa },
			IdEstado  : {"field": "IdEstadoCliente","operator": "eq", "value" : 'ACT'}
		},
	};
	
	ingresarInstrumentoFFM.formData.filtrosEmisor = {
			IndicadorAGF : { "field"   : "IndicadorAGF","operator": "eq", "value"   :  "S"}
	};
	
	ingresarInstrumentoFFM.formData.filtrosTipoDocumentoFondo = {
			TipoRut : { "field"   : "IdTipoDocumentoIdentidad","operator": "eq", "value"   :  "R"},
			TipoTaxID : { "field"   : "IdTipoDocumentoIdentidad","operator": "eq", "value"   :  "TAX"}
	};
	
	ingresarInstrumentoFFM.formData.filtrosTipoInstrumento = {
		IdClasificacionTipoInstrumento: {"field" : "IdClasificacionTipoInstrumento", "operator": "eq", "value":"FM"}
	}
	
	ingresarInstrumentoFFM.formData.filtrosMercado = {
		IdEstado: {"field" : "IdEstado", "operator": "eq", "value":"ACT"}
	}
	
	ingresarInstrumentoFFM.buscarSerieFondo = function(){
    	var filter = {field: "IdSerieFondo", operator: "eq", value: $scope.parametros.IdSerieFondo};
		
    	conf.query({filter:filter}).then(function(){
			if (conf.data().length<1)
			{
				console.log("Serie no existe");
				bcsComunes.ventanaOverlay(ingresarInstrumentoFFM.winID,false);
				bcsComunes.ventanaCerrar(ingresarInstrumentoFFM.winID);
				return false;
			}
			else
			{
				var datosSerieFondo = conf.data()[0];
				ingresarInstrumentoFFM.datosAntes = conf.data()[0];
				ingresarInstrumentoFFM.datosAntes.InstrumentoOWP = datosSerieFondo.InstrumentoOWP === 'S' ? true : false;
				ingresarInstrumentoFFM.datosAntes.EsAPV = datosSerieFondo.EsAPV === 'S' ? true : false;
				ingresarInstrumentoFFM.datosAntes.RestringeAporte = datosSerieFondo.RestringeAporte === 'S' ? true : false;
				ingresarInstrumentoFFM.datosAntes.RestringeRescate = datosSerieFondo.RestringeRescate === 'S' ? true : false;
				ingresarInstrumentoFFM.datosAntes.AportesAltos = datosSerieFondo.AportesAltos === 'S' ? true : false;
				ingresarInstrumentoFFM.datosAntes.PatrimonioAltos = datosSerieFondo.PatrimonioAltos === 'S' ? true : false;
				ingresarInstrumentoFFM.datosAntes.FondoExtranjero = datosSerieFondo.FondoExtranjero === 'S' ? true : false;
				ingresarInstrumentoFFM.datosAntes.MoneyMarket = datosSerieFondo.MoneyMarket === 'S' ? true : false;
				ingresarInstrumentoFFM.datosAntes.ExcluyeGarantia = datosSerieFondo.ExcluyeGarantia === 'S' ? true : false;
				ingresarInstrumentoFFM.datosAntes.CobraComision = datosSerieFondo.CobraComision === 'S' ? true : false;
				ingresarInstrumentoFFM.datosAntes.Diferido = datosSerieFondo.Diferido === 'S' ? true : false;
				ingresarInstrumentoFFM.datosAntes.Omnibus = datosSerieFondo.Omnibus === 'S' ? true : false;
				ingresarInstrumentoFFM.datosAntes.AperturaAutomaticaFondo = datosSerieFondo.AperturaAutomaticaFondo === 'S' ? true : false;
				ingresarInstrumentoFFM.datosAntes.FondoNormativo = datosSerieFondo.FondoNormativo === 'S' ? true : false;
				ingresarInstrumentoFFM.datosAntes.CambioFondo = datosSerieFondo.CambioFondo === 'S' ? true : false;
				ingresarInstrumentoFFM.Moneda = datosSerieFondo.IdMoneda;
				ingresarInstrumentoFFM.datosAntes.AportesAltosUI = $translate.instant(ingresarInstrumentoFFM.datosAntes.AportesAltosUI);
				ingresarInstrumentoFFM.datosAntes.InstrumentoOWPUI = $translate.instant(ingresarInstrumentoFFM.datosAntes.InstrumentoOWPUI);
				ingresarInstrumentoFFM.datosAntes.EsAPVUI = $translate.instant(ingresarInstrumentoFFM.datosAntes.EsAPVUI);
				ingresarInstrumentoFFM.datosAntes.RestringeAporteUI =  $translate.instant(ingresarInstrumentoFFM.datosAntes.RestringeAporteUI);
				ingresarInstrumentoFFM.datosAntes.RestringeRescateUI =  $translate.instant(ingresarInstrumentoFFM.datosAntes.RestringeRescateUI);
				ingresarInstrumentoFFM.datosAntes.PatrimonioAltosUI = $translate.instant(ingresarInstrumentoFFM.datosAntes.PatrimonioAltosUI);
				ingresarInstrumentoFFM.datosAntes.EsFondoExtranjeroUI = $translate.instant(ingresarInstrumentoFFM.datosAntes.EsFondoExtranjeroUI);
				ingresarInstrumentoFFM.datosAntes.EsMoneyMarketUI	  =	$translate.instant(ingresarInstrumentoFFM.datosAntes.EsMoneyMarketUI);
				ingresarInstrumentoFFM.datosAntes.ExcluyeGarantiaUI   = $translate.instant(ingresarInstrumentoFFM.datosAntes.ExcluyeGarantiaUI);
				ingresarInstrumentoFFM.datosAntes.CobraComisionUI     = $translate.instant(ingresarInstrumentoFFM.datosAntes.CobraComisionUI);
				ingresarInstrumentoFFM.datosAntes.EsDiferidoUI     = $translate.instant(ingresarInstrumentoFFM.datosAntes.EsDiferidoUI);
				ingresarInstrumentoFFM.datosAntes.OmnibusUI     = $translate.instant(ingresarInstrumentoFFM.datosAntes.OmnibusUI);
				ingresarInstrumentoFFM.datosAntes.AperturaAutomaticaFondoUI = $translate.instant(ingresarInstrumentoFFM.datosAntes.AperturaAutomaticaFondoUI);
				ingresarInstrumentoFFM.datosAntes.FondoNormativoUI = $translate.instant(ingresarInstrumentoFFM.datosAntes.FondoNormativoUI);
				ingresarInstrumentoFFM.datosAntes.CambioFondoUI     = $translate.instant(ingresarInstrumentoFFM.datosAntes.CambioFondoUI); //Luis
				ingresarInstrumentoFFM.datosAntes.FechaValorCuota   =  (ingresarInstrumentoFFM.datosAntes.ValorCuota <= 1 )?  util.dateInicioDia($scope.SesionFechaSistema) : ingresarInstrumentoFFM.datosAntes.FechaValorCuota  

				ingresarInstrumentoFFM.datosDespues = angular.copy(ingresarInstrumentoFFM.datosAntes);
				ingresarInstrumentoFFM.IdSocioDeNegocio=ingresarInstrumentoFFM.datosDespues.IdSocioDeNegocio || -1;
				ingresarInstrumentoFFM.IdSocioDeNegocioPostFiltro=ingresarInstrumentoFFM.datosDespues.IdSocioDeNegocio || -1;
				ingresarInstrumentoFFM.documentoIdentidadFondo.DocumentoIdentidad = ingresarInstrumentoFFM.datosDespues.DocumentoIdentidadFondo;
				ingresarInstrumentoFFM.documentoIdentidadFondo.NombreTipoDocumentoIdentidad = ingresarInstrumentoFFM.datosDespues.NombreTipoDocumentoIdentidadFondo;
				ingresarInstrumentoFFM.documentoIdentidadFondo.IdTipoDocumentoIdentidad = ingresarInstrumentoFFM.datosDespues.IdTipoDocumentoIdentidadFondo;
				ingresarInstrumentoFFM.documentoIdentidadFondo.DocumentoIdentidadAsociado = ingresarInstrumentoFFM.datosDespues.DocumentoIdentidadFondo;
				ingresarInstrumentoFFM.documentoIdentidadFondo.IdTipoDocumentoIdentidadAsociado = ingresarInstrumentoFFM.datosDespues.IdTipoDocumentoIdentidadFondo;
				ingresarInstrumentoFFM.documentoIdentidadFondo.NombreTipoDocumentoIdentidadAsociado = ingresarInstrumentoFFM.datosDespues.NombreTipoDocumentoIdentidadFondo;
				
				ingresarInstrumentoFFM.documentoIdentidadFondoOriginal = angular.copy(ingresarInstrumentoFFM.documentoIdentidadFondo);
				
				console.log("Serie si existe");
				//bcsComunes.ventanaOverlay(ingresarInstrumentoFFM.winID,false);
				
				//console.log("ffmm dp",ingresarInstrumentoFFM.oSocioNegocio,ingresarInstrumentoFFM.IdSocioDeNegocio);

				return true;
			}
		});
    }
	
	ingresarInstrumentoFFM.buscarRegimenSerieFondo = function(){
		bcsComunes.ventanaOverlay(ingresarInstrumentoFFM.winID,true);
		var filtersRegimenSerieFondo = [];
		filtersRegimenSerieFondo.push({field: "IdSerieFondo", operator: "eq", value: $scope.parametros.IdSerieFondo});
		filtersRegimenSerieFondo.push({field: "IdEstadoRegimenSerieDeFondo", operator: "eq", value: 'ACT'});
		filtersRegimenSerieFondo.push({field: "IdEstadoRegimenSerieDeFondo", operator: "eq", value: 'ACT'});
		confRegimenSerieFondo.query({filter:filtersRegimenSerieFondo}).then(function(){
			if(confRegimenSerieFondo.data().length < 1){
				console.log("No existen regimenes asociados");
				bcsComunes.ventanaOverlay(ingresarInstrumentoFFM.winID,false);
				return false;
			}
			else{
				var regimenesSerieFondo = confRegimenSerieFondo.data();
				for(var i = 0; i < regimenesSerieFondo.length; i++){
					ingresarInstrumentoFFM.checkRegimen[regimenesSerieFondo[i].IdRegimenFondoMutuo] = true;
					ingresarInstrumentoFFM.regimenFondoMutuoUI[regimenesSerieFondo[i].IdRegimenFondoMutuo] = $translate.instant('UI_SI');
				}
			}
		})
	}
    //INGMOD: Ingreso/modificacion
	if($scope.parametros.Edicion=='S'){
		bcsComunes.ventanaTitulo(ingresarInstrumentoFFM.winID, $translate.instant("UI_MODIFICAR_INSTRUMENTO_FFM"));
		$timeout(function() {
			ingresarInstrumentoFFM.buscarSerieFondo();
			ingresarInstrumentoFFM.buscarRegimenSerieFondo();
			ingresarInstrumentoFFM.documentoIdentidadFondo.Valido = true;
			ingresarInstrumentoFFM.documentoIdentidadFondoOriginal.Valido = true;
			ingresarInstrumentoFFM.muestraDatosAGF=true;
		},50);
	}else if ($scope.parametros.Edicion=='N'){
		bcsComunes.ventanaTitulo(ingresarInstrumentoFFM.winID, $translate.instant("UI_CONSULTAR_INSTRUMENTO_FFM"));
		$timeout(function() {
			ingresarInstrumentoFFM.buscarSerieFondo();
			ingresarInstrumentoFFM.buscarRegimenSerieFondo();
			ingresarInstrumentoFFM.documentoIdentidadFondo.Valido = true;
			ingresarInstrumentoFFM.documentoIdentidadFondoOriginal.Valido = true;
			ingresarInstrumentoFFM.muestraDatosAGF=true;
		},50);
	}
	else{
		bcsComunes.ventanaTitulo(ingresarInstrumentoFFM.winID, $translate.instant("UI_INGRESAR_INSTRUMENTO_FFM"));
		ingresarInstrumentoFFM.muestraDatosAGF=true;
	}
	
	
	
	ingresarInstrumentoFFM.buscarFondo = function(){
		var filter = {field: "IdFondo", operator: "eq", value: ingresarInstrumentoFFM.datosDespues.IdFondo};
		
    	conf.query({filter:filter}).then(function(){
			if (conf.data().length<1)
			{
				console.log("Fondo no existe");
				return false;
			}
			else
			{
				ingresarInstrumentoFFM.datosDespues.NombreFondo = conf.data()[0].NombreFondo;
				ingresarInstrumentoFFM.datosDespues.IdEmisor = conf.data()[0].IdEmisor;
				ingresarInstrumentoFFM.datosDespues.IdMoneda = conf.data()[0].IdMoneda;
				ingresarInstrumentoFFM.datosDespues.IdTipoInstrumento = conf.data()[0].IdTipoInstrumento;
				ingresarInstrumentoFFM.datosDespues.IdTipoFondo = conf.data()[0].IdTipoFondo;
				ingresarInstrumentoFFM.datosDespues.CodigoSVS = conf.data()[0].CodigoSVS;
				ingresarInstrumentoFFM.datosDespues.IdRegimenTributario = conf.data()[0].IdRegimenTributario;
				ingresarInstrumentoFFM.datosDespues.IdCategoriaPerfilFondoMutuo = conf.data()[0].IdCategoriaPerfilFondoMutuo;
				ingresarInstrumentoFFM.datosDespues.IdPaisOrigen = conf.data()[0].IdPaisOrigen;
				ingresarInstrumentoFFM.datosDespues.FondoExtranjero = conf.data()[0].FondoExtranjero === 'S' ? true : false;
				ingresarInstrumentoFFM.datosDespues.MoneyMarket = conf.data()[0].MoneyMarket === 'S' ? true : false;
				ingresarInstrumentoFFM.datosDespues.AperturaAutomaticaFondo = conf.data()[0].AperturaAutomaticaFondo === 'S' ? true : false;
				ingresarInstrumentoFFM.datosDespues.FondoNormativo = conf.data()[0].FondoNormativo === 'S' ? true : false;
				ingresarInstrumentoFFM.datosDespues.EsFondoExtranjeroUI = $translate.instant(ingresarInstrumentoFFM.datosDespues.EsFondoExtranjeroUI);
				ingresarInstrumentoFFM.datosDespues.EsMoneyMarketUI	=	$translate.instant(ingresarInstrumentoFFM.datosDespues.EsMoneyMarketUI);
				ingresarInstrumentoFFM.datosDespues.IdPaisEmisor	=	conf.data()[0].IdPaisEmisor;
				ingresarInstrumentoFFM.documentoIdentidadFondo.DocumentoIdentidadAsociado	=	conf.data()[0].DocumentoIdentidadFondo;
				
				ingresarInstrumentoFFM.Moneda = conf.data()[0].IdMoneda;
				console.log("Consulta si existe");
				return true;
			}
		});
    	
    }
	
	$scope.$watch('ingresarInstrumentoFFM.datosDespues.IdFondo',function(newval,oldval){
		if(newval && newval != oldval && newval != ''){
			ingresarInstrumentoFFM.Moneda = $scope.SesionIdMonedaSistema;
			ingresarInstrumentoFFM.buscarFondo();
		}
	})
	
	$scope.$watch('ingresarInstrumentoFFM.TipoFondo',function(newval,oldval){
		if(newval && newval != oldval){
			if(newval.IndicadorMoneyMarket != 'S') ingresarInstrumentoFFM.datosDespues.MoneyMarket = false
		}
	})
    
	$scope.$watch('ingresarInstrumentoFFM.datosDespues.IdMoneda',function(newval,oldval){
    	if(newval){
    		ingresarInstrumentoFFM.decimalesCantidad = util.bcs_formato(0,'FMM','Cantidad',ingresarInstrumentoFFM.Moneda,'','').cantidadDecimales
    		ingresarInstrumentoFFM.decimalesMonto = util.bcs_formato(0,'FMM','Monto',ingresarInstrumentoFFM.Moneda,'','').cantidadDecimales
    	}
    });
	
	$scope.$watch('ingresarInstrumentoFFM.datosDespues.IdMercadoFondoMutuo',function(newval,oldval){
		if(ingresarInstrumentoFFM.configuracionFechas.esValutaMoneyMarketRescate(newval,ingresarInstrumentoFFM.datosDespues.MoneyMarket)){
			bloquearValutasRescates();
		}else{
			desbloquearValutasRescates();
		}
    	calcularFechas();
    });
	
	$scope.$watch('ingresarInstrumentoFFM.datosDespues.MoneyMarket',function(newval,oldval){
		if(ingresarInstrumentoFFM.configuracionFechas.esValutaMoneyMarketAporte(newval)){
			bloquearValutasAportes();
		}else{
			desbloquearValutasAportes();
		}
		
		if(ingresarInstrumentoFFM.configuracionFechas.esValutaMoneyMarketRescate(ingresarInstrumentoFFM.datosDespues.IdMercadoFondoMutuo,newval)){
			bloquearValutasRescates();
		}else{
			desbloquearValutasRescates();
		}
		
		calcularFechas();
    });
	
	$scope.$watchGroup(['ingresarInstrumentoFFM.datosDespues.DiasValutaCertificacionAporte','ingresarInstrumentoFFM.datosDespues.DiasValutaCertificacionRescate'],function(newval,oldval){
		calcularFechas();
    });
		
	$scope.$watch('ingresarInstrumentoFFM.datosDespues.IdPaisOrigen',function(newVal){
		if(newVal!==undefined && ingresarInstrumentoFFM.documentoIdentidadFondo.DocumentoIdentidadAsociado!==undefined){
			if (newVal === $scope.$root.SesionIdPaisSistema){
				ingresarInstrumentoFFM.datosDespues.FondoExtranjero = false;
				ingresarInstrumentoFFM.blurDocumentoIdentidad(ingresarInstrumentoFFM.documentoIdentidadFondo.IdTipoDocumentoIdentidadAsociado,ingresarInstrumentoFFM.documentoIdentidadFondo.DocumentoIdentidadAsociado,!ingresarInstrumentoFFM.datosDespues.FondoExtranjero);
				ingresarInstrumentoFFM.documentoIdentidadFondo.IdTipoDocumentoIdentidadDefecto='R';
				ingresarInstrumentoFFM.documentoIdentidadFondo.CambiarIdTipoDocumentoIdentidad='R';
				$scope.placeholder.documentoIdentidad = $scope.$root.SesionValoresDefectoSistema.PLACEHOLDER_RUT;
			}
			else {
				//ingresarInstrumentoFFM.documentoIdentidadFondo.DocumentoIdentidadAsociado = '';
				ingresarInstrumentoFFM.blurDocumentoIdentidad(ingresarInstrumentoFFM.documentoIdentidadFondo.IdTipoDocumentoIdentidadAsociado,ingresarInstrumentoFFM.documentoIdentidadFondo.DocumentoIdentidadAsociado,!ingresarInstrumentoFFM.datosDespues.FondoExtranjero);
				ingresarInstrumentoFFM.datosDespues.FondoExtranjero = true;
				ingresarInstrumentoFFM.documentoIdentidadFondo.IdTipoDocumentoIdentidadDefecto='TAX';
				ingresarInstrumentoFFM.documentoIdentidadFondo.CambiarIdTipoDocumentoIdentidad='TAX';
				$scope.placeholder.documentoIdentidad = '';
			}
		}
	});
	
	$scope.$watch('ingresarInstrumentoFFM.documentoIdentidadFondo.IdTipoDocumentoIdentidadAsociado',function(newVal){
		if(newVal!==undefined){
			if(newVal==='R') 
				$scope.placeholder.documentoIdentidad = $scope.$root.SesionValoresDefectoSistema.PLACEHOLDER_RUT;
			else if(newVal==='P')
				$scope.placeholder.documentoIdentidad = '';
			else $scope.placeholder.documentoIdentidad = '';
		}
	});
	
	$scope.$watchGroup(['FormIngresoInstrumentoFFM.$valid','ingresarInstrumentoFFM.existeRegimen'],function(newval,oldval){
		if($scope.FormIngresoInstrumentoFFM.$valid && ingresarInstrumentoFFM.existeRegimen === 'S') ingresarInstrumentoFFM.FormValido = true;
		else ingresarInstrumentoFFM.FormValido = false;
	})
	
	bloquearValutasAportes = function(){
		ingresarInstrumentoFFM.configuracionFechas.Aporte.FechaCurse.bloqueo = true;
		ingresarInstrumentoFFM.configuracionFechas.Aporte.FechaValorCuota.bloqueo = true;
	}
	
	desbloquearValutasAportes = function(){
		ingresarInstrumentoFFM.configuracionFechas.Aporte.FechaCurse.bloqueo = false;
		ingresarInstrumentoFFM.configuracionFechas.Aporte.FechaValorCuota.bloqueo = false;
	}
	
	bloquearValutasRescates = function(){
		ingresarInstrumentoFFM.configuracionFechas.Rescate.FechaCurse.bloqueo = true;
		ingresarInstrumentoFFM.configuracionFechas.Rescate.FechaValorCuota.bloqueo = true;
	}
	
	desbloquearValutasRescates = function(){
		ingresarInstrumentoFFM.configuracionFechas.Rescate.FechaCurse.bloqueo = false;
		ingresarInstrumentoFFM.configuracionFechas.Rescate.FechaValorCuota.bloqueo = false;
	}
	
	calcularFechas = function(){
		
		if(ingresarInstrumentoFFM.configuracionFechas.esValutaMoneyMarketAporte(ingresarInstrumentoFFM.datosDespues.MoneyMarket)){
			if(!ingresarInstrumentoFFM.configuracionFechas.Aporte.FechaCertificacion.validacionValutaMoneyMarket(ingresarInstrumentoFFM.datosDespues.DiasValutaCertificacionAporte)){
				ingresarInstrumentoFFM.datosDespues.DiasValutaCertificacionAporte = undefined;
			}
			ingresarInstrumentoFFM.datosDespues.DiasValutaCurseAporte = ingresarInstrumentoFFM.configuracionFechas.Aporte.FechaCurse.calculaValutaMoneyMarket();
			ingresarInstrumentoFFM.datosDespues.DiasValutaValorCuotaAporte = ingresarInstrumentoFFM.configuracionFechas.Aporte.FechaValorCuota.calculaValutaMoneyMarket();
		}
		
		if(ingresarInstrumentoFFM.configuracionFechas.esValutaMoneyMarketRescate(ingresarInstrumentoFFM.datosDespues.IdMercadoFondoMutuo,ingresarInstrumentoFFM.datosDespues.MoneyMarket)){
			if(!ingresarInstrumentoFFM.configuracionFechas.Rescate.FechaCertificacion.validacionValutaMoneyMarket(ingresarInstrumentoFFM.datosDespues.DiasValutaCertificacionRescate)){
				ingresarInstrumentoFFM.datosDespues.DiasValutaCertificacionRescate = undefined;
			}
			ingresarInstrumentoFFM.datosDespues.DiasValutaCurseRescate = ingresarInstrumentoFFM.configuracionFechas.Rescate.FechaCurse.calculaValutaMoneyMarket();
			ingresarInstrumentoFFM.datosDespues.DiasValutaValorCuotaRescate = ingresarInstrumentoFFM.configuracionFechas.Rescate.FechaValorCuota.calculaValutaMoneyMarket(ingresarInstrumentoFFM.datosDespues.DiasValutaCertificacionRescate);
		}
	}
        
	ingresarInstrumentoFFM.formData.ingresarInstrumentoFondoMutuo = function(){
		
		if (ingresarInstrumentoFFM.datosDespues.MontoMaximo >0 && ingresarInstrumentoFFM.datosDespues.MontoMinimo > ingresarInstrumentoFFM.datosDespues.MontoMaximo ){
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_MONTO_MINIMO_INVALIDO"));
 			return;
		};
		
		bcsComunes.ventanaOverlay(ingresarInstrumentoFFM.winID,true);
		var datosIngresoInstrumentoFondoMutuo = {
				p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
				p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
				p_IdModulo : 'FMM',
				p_UsaReglasNegocio : 'S',
				p_Fondo : [{
					IdInterno					:	0,
					IdFondo						:	ingresarInstrumentoFFM.datosDespues.IdFondo,
					IdEmisor					:	ingresarInstrumentoFFM.datosDespues.IdEmisor ? ingresarInstrumentoFFM.datosDespues.IdEmisor : '',
					Nemotecnico 				:	ingresarInstrumentoFFM.datosDespues.IdEmisor+ingresarInstrumentoFFM.datosDespues.IdMoneda+ingresarInstrumentoFFM.datosDespues.IdFondo,
					NombreFondo					:	ingresarInstrumentoFFM.datosDespues.NombreFondo,
					IdTipoFondo					:	ingresarInstrumentoFFM.datosDespues.IdTipoFondo ? ingresarInstrumentoFFM.datosDespues.IdTipoFondo : '',
					IdPaisOrigen				:	ingresarInstrumentoFFM.datosDespues.IdPaisOrigen ? ingresarInstrumentoFFM.datosDespues.IdPaisOrigen : $scope.SesionIdPaisSistema,
					CodigoSVS					:	ingresarInstrumentoFFM.datosDespues.CodigoSVS,
					MoneyMarket					:	ingresarInstrumentoFFM.TipoFondo.IndicadorMoneyMarket == 'S' && ingresarInstrumentoFFM.datosDespues.MoneyMarket ? 'S' : 'N',
					Vigencia					:	'S',
					FondoExtranjero				:	ingresarInstrumentoFFM.datosDespues.FondoExtranjero ? 'S' : 'N',
					IdMoneda					:	ingresarInstrumentoFFM.datosDespues.IdMoneda,
					IdTipoInstrumento			:	ingresarInstrumentoFFM.datosDespues.IdTipoInstrumento ? ingresarInstrumentoFFM.datosDespues.IdTipoInstrumento : '',
					IdCategoriaPerfilFondoMutuo	:	ingresarInstrumentoFFM.datosDespues.IdCategoriaPerfilFondoMutuo ? ingresarInstrumentoFFM.datosDespues.IdCategoriaPerfilFondoMutuo : '',
					AperturaAutomaticaFondo		:	ingresarInstrumentoFFM.datosDespues.AperturaAutomaticaFondo ? 'S' : 'N',
					FondoNormativo				:	ingresarInstrumentoFFM.datosDespues.FondoNormativo ? 'S' : 'N',
					IdPaisEmisor				:	ingresarInstrumentoFFM.datosDespues.IdPaisEmisor ? ingresarInstrumentoFFM.datosDespues.IdPaisEmisor : $scope.SesionIdPaisSistema,
					IdTipoDocumentoIdentidadFondo:	ingresarInstrumentoFFM.documentoIdentidadFondo.IdTipoDocumentoIdentidadAsociado ? ingresarInstrumentoFFM.documentoIdentidadFondo.IdTipoDocumentoIdentidadAsociado : '0',
					DocumentoIdentidadFondo		:	ingresarInstrumentoFFM.documentoIdentidadFondo.DocumentoIdentidadAsociado ? ingresarInstrumentoFFM.documentoIdentidadFondo.DocumentoIdentidadAsociado : '',
					IdMercadoFondoMutuo			:	ingresarInstrumentoFFM.datosDespues.IdMercadoFondoMutuo ? ingresarInstrumentoFFM.datosDespues.IdMercadoFondoMutuo : ''		
							
				}],
				p_SerieFondo :[{
					IdInterno							:	0,
					IdSerieFondo						:	ingresarInstrumentoFFM.datosDespues.IdSerieFondo,
					IdFondo								:	ingresarInstrumentoFFM.datosDespues.IdFondo,
					NombreSerieFondo					:	ingresarInstrumentoFFM.datosDespues.NombreSerieFondo,
					CodigoSerie							:	ingresarInstrumentoFFM.datosDespues.CodigoSerie || '',
					Alias								:	'',
					DecimalesValorCuota					:	ingresarInstrumentoFFM.datosDespues.DecimalesValorCuota,
					DecimalesCuota						:	ingresarInstrumentoFFM.datosDespues.DecimalesCuota,
					MontoMinimo							:	ingresarInstrumentoFFM.datosDespues.MontoMinimo,
					MontoMaximo							:	ingresarInstrumentoFFM.datosDespues.MontoMaximo,
					DiasRetencionesAporte				:	ingresarInstrumentoFFM.datosDespues.DiasRetencionesAporte,
					DiasRetencionesRescate				:	ingresarInstrumentoFFM.datosDespues.DiasRetencionesRescate,
					ExcluyeGarantia						:	ingresarInstrumentoFFM.datosDespues.ExcluyeGarantia ? 'S' : 'N',
					CobraComision						:	ingresarInstrumentoFFM.datosDespues.CobraComision ? 'S' : 'N',
					Diferido							:	ingresarInstrumentoFFM.datosDespues.Diferido ? 'S' : 'N',
					InstrumentoDCV						:	ingresarInstrumentoFFM.datosDespues.InstrumentoDCV ? ingresarInstrumentoFFM.datosDespues.InstrumentoDCV : '',
					Omnibus								:	ingresarInstrumentoFFM.datosDespues.Omnibus ? 'S' : 'N',
					PatrimonioMinimo					:	ingresarInstrumentoFFM.datosDespues.PatrimonioMinimo,
					IdSerieFondoOrigen					:	'',
					IdEstado							:	ingresarInstrumentoFFM.datosDespues.IdEstado,
					IdCliente							:	ingresarInstrumentoFFM.oClientePortafolio.seleccionado.IdCliente,
					IdPortafolio						:   ingresarInstrumentoFFM.oClientePortafolio.seleccionado.IdPortafolio,
					IdMedioDePagoAbono			  	   	:	ingresarInstrumentoFFM.oConfiguracionMedioDePagoAbono.seleccionado.IdMedioDePago,
					IdMedioDePagoCargo			       	:	ingresarInstrumentoFFM.oConfiguracionMedioDePagoCargo.seleccionado.IdMedioDePago,
					DiasValutaValorCuotaAporte			:	ingresarInstrumentoFFM.datosDespues.DiasValutaValorCuotaAporte,
					DiasValutaValorCuotaRescate			:	ingresarInstrumentoFFM.datosDespues.DiasValutaValorCuotaRescate,
					DiasValutaCertificacionAporte 	   	:	ingresarInstrumentoFFM.datosDespues.DiasValutaCertificacionAporte,
					DiasValutaCertificacionRescate	   	:	ingresarInstrumentoFFM.datosDespues.DiasValutaCertificacionRescate,
					DiasValutaCurseAporte				:	ingresarInstrumentoFFM.datosDespues.DiasValutaCurseAporte,
					DiasValutaCurseRescate				:	ingresarInstrumentoFFM.datosDespues.DiasValutaCurseRescate,
					IdFamiliaFondo						:	ingresarInstrumentoFFM.datosDespues.IdFamiliaFondo ? ingresarInstrumentoFFM.datosDespues.IdFamiliaFondo : '',
					CambioFondo							:	ingresarInstrumentoFFM.datosDespues.CambioFondo? 'S' : 'N',	
					DiasComision						:	ingresarInstrumentoFFM.datosDespues.DiasComision? ingresarInstrumentoFFM.datosDespues.DiasComision : 0,
					PorcentajeComision					:	ingresarInstrumentoFFM.datosDespues.PorcentajeComision? ingresarInstrumentoFFM.datosDespues.PorcentajeComision : 0,
					PorcentajeRemuneracion				:	ingresarInstrumentoFFM.datosDespues.PorcentajeRemuneracion? ingresarInstrumentoFFM.datosDespues.PorcentajeRemuneracion : 0,
					PorcentajeGastoOpera				:	ingresarInstrumentoFFM.datosDespues.PorcentajeGastoOpera? ingresarInstrumentoFFM.datosDespues.PorcentajeGastoOpera : 0,
					PorcentajeCostoAdm					:	ingresarInstrumentoFFM.datosDespues.PorcentajeCostoAdm? ingresarInstrumentoFFM.datosDespues.PorcentajeCostoAdm : 0,
					ValorCuota							:	ingresarInstrumentoFFM.datosDespues.ValorCuota? ingresarInstrumentoFFM.datosDespues.ValorCuota : 0,
					FechaValorCuota						:	ingresarInstrumentoFFM.datosDespues.FechaValorCuota ? util.date(ingresarInstrumentoFFM.datosDespues.FechaValorCuota) : '',
					AportesAltos						:	ingresarInstrumentoFFM.datosDespues.AportesAltos ? 'S' : 'N',
					PatrimonioAltos						:	ingresarInstrumentoFFM.datosDespues.PatrimonioAltos ? 'S' : 'N',
					MontoPatrimonioEmisor				:	ingresarInstrumentoFFM.datosDespues.MontoPatrimonioEmisor,
					InstrumentoOWP						:	ingresarInstrumentoFFM.datosDespues.InstrumentoOWP ? 'S' : 'N',
					EsAPV								:	ingresarInstrumentoFFM.datosDespues.EsAPV ? 'S' : 'N',
					RestringeAporte						:	ingresarInstrumentoFFM.datosDespues.RestringeAporte ? 'S' : 'N',
					RestringeRescate					:	ingresarInstrumentoFFM.datosDespues.RestringeRescate ? 'S' : 'N',
					NemoCMF								:	ingresarInstrumentoFFM.datosDespues.NemoCMF || ''
						/*
					 FechaValorCuota						:	ingresarInstrumentoFFM.datosDespues.FechaValorCuota && (ingresarInstrumentoFFM.datosDespues.ValorCuota != undefined)? ingresarInstrumentoFFM.datosDespues.FechaValorCuota : ''
					 */
				}],
				p_RegimenSerieFondo : []
		};
		
		
		$.each(ingresarInstrumentoFFM.checkRegimen,function(key,value){
			if(ingresarInstrumentoFFM.checkRegimen[key]){
				datosIngresoInstrumentoFondoMutuo.p_RegimenSerieFondo.push({
					IdInterno	:	0,
					IdRegimenFondoMutuo : ingresarInstrumentoFFM.matrizRegimen[key].IdRegimenFondoMutuo,
					IdSerieFondo	:	ingresarInstrumentoFFM.datosDespues.IdSerieFondo
				});
			}
		});
		
		if($scope.parametros.IdSerieFondo){
			bcsServicioFondosMutuos.ModificarInstrumentoFondoMutuoMM(datosIngresoInstrumentoFondoMutuo).$promise.then(function(respuesta){
		     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_RESULTADO[0].IdFolio != '' && respuesta.P_OUT_ERRORESCONTADOR === 0){
		     		var id = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_MODIFICAR_INSTRUMENTO_FONDO_MUTUO"),conflictos:util.parseServerResponse(respuesta)},ingresarInstrumentoFFM.winID);
		     		bcsConfiguracionFFM.ingresarCerrarVentana(id, ingresarInstrumentoFFM.winID);
		     	 }else{
		     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_MODIFICAR_INSTRUMENTO_FONDO_MUTUO"),conflictos:util.parseServerResponse(respuesta)},ingresarInstrumentoFFM.winID);
		     	 }
	  		 },function(respuesta){
	  		   bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_MODIFICAR_INSTRUMENTO_FONDO_MUTUO"),conflictos:util.parseServerResponse(respuesta)},ingresarInstrumentoFFM.winID);
	  		 });
		}else{
			bcsServicioFondosMutuos.CrearInstrumentoFondoMutuoMM(datosIngresoInstrumentoFondoMutuo).$promise.then(function(respuesta){
		     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_RESULTADO[0].IdFolio != '' && respuesta.P_OUT_ERRORESCONTADOR === 0){
		     		var id = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_INGRESO_INSTRUMENTO_FONDO_MUTUO"),conflictos:util.parseServerResponse(respuesta)},ingresarInstrumentoFFM.winID);
		     		bcsConfiguracionFFM.ingresarCerrarVentana(id, ingresarInstrumentoFFM.winID);
		     	 }else{
		     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_INGRESO_INSTRUMENTO_FONDO_MUTUO"),conflictos:util.parseServerResponse(respuesta)},ingresarInstrumentoFFM.winID);
		     	 }
	  		 },function(respuesta){
	  		   bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_INGRESO_INSTRUMENTO_FONDO_MUTUO"),conflictos:util.parseServerResponse(respuesta)},ingresarInstrumentoFFM.winID);
	  		 });
		}
		
	}
    
	ingresarInstrumentoFFM.formData.resetFoco =  function() {
		ingresarInstrumentoFFM.formData.focoNemotecnico = true;
	 };
	 
	 ingresarInstrumentoFFM.formData.editarInstrumento = function(){
		bcsComunes.ventanaTitulo(ingresarInstrumentoFFM.winID, $translate.instant("UI_MODIFICAR_INSTRUMENTO_FFM"));
		
		 if(util.date(ingresarInstrumentoFFM.datosDespues.FechaValorCuota).utc().format('YYYY-MM-DD') == $scope.SesionFechaSistema)
			 ingresarInstrumentoFFM.agregarValorCuota = true;
		 else
			 ingresarInstrumentoFFM.agregarValorCuota = false;
		 ingresarInstrumentoFFM.Edicion = 'S';
		 bcsComunes.ventanaTamanoAutomatico(ingresarInstrumentoFFM.winID,10);
	 }
	 
	 $scope.$watchCollection('ingresarInstrumentoFFM.checkRegimen',function(newValue, oldValue){
		 if(newValue && oldValue && JSON.stringify(newValue)!=JSON.stringify(oldValue)){
			 ingresarInstrumentoFFM.existeRegimen = 'N';
			 $.each(newValue,function(key,value){
				if(value){
					ingresarInstrumentoFFM.existeRegimen = 'S';
					return false;
				}
			});		
		 }
		 
	 })
	 
	 ingresarInstrumentoFFM.blurDocumentoIdentidad = function(tipo,documento,validar){
		 ingresarInstrumentoFFM.documentoIdentidadFondo.Valido = true;
		 if(!tipo)return;
		 if(documento.length==0)return;
		 if(validar===false)return;
		 
		 if(tipo=='RUT'){
			 if(documento.length>8){
				 bcsComunes.msg("warning","warning",$translate.instant("UI_SUPERA_LARGO_PERMITIDO"));
				 documento="";
				 ingresarInstrumentoFFM.documentoIdentidadFondo.Valido = false;
			 }else documento=util.PadLeft(documento,'0',8);
		 }if(tipo=='IUP'){
			 if(documento.length<14){
				 bcsComunes.msg("warning","warning",$translate.instant("UI_NO_CUMPLE_LARGO_PERMITIDO"));
				 $scope.errorValidarDocumento=true;
			 }if(documento.length>14){
				 bcsComunes.msg("warning","warning",$translate.instant("UI_SUPERA_LARGO_PERMITIDO"));
				 documento="";
				 ingresarInstrumentoFFM.documentoIdentidadFondo.Valido = false;
			 }else{
				 var validacion=util.validarIUP(documento);
				 console.log(validacion);
				 if(!validacion.escorrecta){
					 bcsComunes.msg("warning","warning",$translate.instant(validacion.mensaje));
					 ingresarInstrumentoFFM.documentoIdentidadFondo.Valido = false;
				 }
			 }
		 }
		 if(tipo=='DNI'){
			 if(documento.length<8){
				 bcsComunes.msg("warning","warning",$translate.instant("UI_NO_CUMPLE_LARGO_PERMITIDO"));
				 $scope.errorValidarDocumento=true;
			 }if(documento.length>8){
				 bcsComunes.msg("warning","warning",$translate.instant("UI_SUPERA_LARGO_PERMITIDO"));
				 documento="";
				 ingresarInstrumentoFFM.documentoIdentidadFondo.Valido = false;
			 }}
		 if(tipo=='RUC'){
			 if(documento.length<11){
				 bcsComunes.msg("warning","warning",$translate.instant("UI_NO_CUMPLE_LARGO_PERMITIDO"));
				 $scope.errorValidarDocumento=true;
			 }if(documento.length>11){
				 bcsComunes.msg("warning","warning",$translate.instant("UI_SUPERA_LARGO_PERMITIDO"));
				 documento="";
				 ingresarInstrumentoFFM.documentoIdentidadFondo.Valido = false;
			 }}
		 if(tipo=='PAS'){
			 if(documento.length>11){
				 bcsComunes.msg("warning","warning",$translate.instant("UI_NO_CUMPLE_LARGO_PERMITIDO"));
				 ingresarInstrumentoFFM.documentoIdentidadFondo.Valido = false;
			 }
		 }
		 if(tipo=='TAX'){
			 if(documento.length>15){
				 bcsComunes.msg("warning","warning",$translate.instant("UI_NO_CUMPLE_LARGO_PERMITIDO"));
				 ingresarInstrumentoFFM.documentoIdentidadFondo.Valido = false;
			 }
		 }		
	
		 if($scope.$root.SesionLocale === "CL" && tipo=='R') {
			 var result = util.validarRut(documento);
			 if(result.valid){
				 documento = result.rutFormat;
			 }else{
				 bcsComunes.msg("warning","warning",$translate.instant("UI_RUT_INVALIDO"));
				 ingresarInstrumentoFFM.documentoIdentidadFondo.Valido = false;
			 }
		 }
		};

		/**************************
		 * 	WORKING NET COMBOBOX
		 **************************/ 
		//los procesados no deben tener caché. No aplicar a combobox-socio-negocio
		var wknDefinition=['ingresarInstrumentoFFM.workingNet.TipoFondo'];
		var wknDef=[];for(vm in wknDefinition)wknDef.push(wknDefinition[vm].replace('ingresarInstrumentoFFM.workingNet.',''));
		var errorRed=false;
		$scope.$watchGroup(wknDefinition,function(newVal){
			if(errorRed===true)return;
			for(vm in wknDef){//verificar error en llamada de red.
				if(ingresarInstrumentoFFM.workingNet[wknDef[vm]]===-1){
					bcsComunes.msg("warning","warning",$translate.instant("UI_ERROR_EN_COMUNICACION")+'. No se pudo cargar combobox: '+wknDef[vm]);
					var p=bcsComunes.ventanaMsgErr($translate.instant('UI_ERROR_EN_COMUNICACION'),ingresarInstrumentoFFM.winID);
					bcsComunes.ventanaPromesa(p).then(function(){bcsComunes.ventanaCerrar(ingresarInstrumentoFFM.winID);});                                       
					errorRed=true;
					return;
				}}
			for(vm in wknDef){console.log('workingNet:'+wknDef[vm],ingresarInstrumentoFFM.workingNet[wknDef[vm]]);//verificar si no se han inicializado todos.
				if(ingresarInstrumentoFFM.workingNet[wknDef[vm]]===undefined)return;}
			if(ingresarInstrumentoFFM.etapa=='inicio'){
				for(vm in wknDef)if(ingresarInstrumentoFFM.workingNet[wknDef[vm]]===0)return;
				bcsComunes.ventanaOverlay(ingresarInstrumentoFFM.winID,false);
				ingresarInstrumentoFFM.etapa='';
				
			}else{for(vm in wknDef){if(ingresarInstrumentoFFM.workingNet[wknDef[vm]]==0){
				bcsComunes.ventanaOverlay(ingresarInstrumentoFFM.winID,true);return;}}
				if(ingresarInstrumentoFFM.etapa==''){bcsComunes.ventanaOverlay(ingresarInstrumentoFFM.winID,false);}
			}
		});	 
	 
	 bcsComunes.ventanaTamanoAutomatico(ingresarInstrumentoFFM.winID,10);
});