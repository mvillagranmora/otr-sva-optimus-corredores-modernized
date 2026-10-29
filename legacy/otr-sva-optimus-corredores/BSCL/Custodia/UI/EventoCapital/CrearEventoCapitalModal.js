/***************************************************************************
* Purpose	: Controlador ingresar Evento Capital.
* Author	: CGuzman - BCS
* Date	:15/02/016 .
**************************************************************************/

angular.module('ModuloCustodia').controller('CrearEventoCapitalModalCtrl',
		function ($rootScope,$scope,$translate,$timeout,bcsComunes, bcsAlerta,OPCIONES_BCSALERTA,
				  bcsServiciosEventoCapital) {

	$scope.data = {};
	$scope.formData = {};

	$scope.data.instrumentoObjeto 		= undefined;

	var overlay = bcsComunes.overlay;
	var idVentana = bcsComunes.ventanaWinId($scope);
	var parametrosVentana = bcsComunes.ventanaParams(idVentana);
	var objEventoCapital = $.extend({},parametrosVentana.EventoCapital)[0];
	$scope.MuestraRetencionArt104		= false;
	if ( $scope.SesionLocale === 'PE' ){
		$scope.MuestraImpuesto		= true;
	}else{
		$scope.MuestraImpuesto		= false;
	};	

	$scope.idVentana = idVentana;

	$scope.data.AplicaImpuestoDivExtranjero = 'N';
	$scope.data.PorcentajeImpuestoExtranjero = 0;
	$scope.data.MuestraImpuestoDivExtranjero = false;

	$scope.data.AplicaGastoTransferencia = 'N';
	$scope.data.MuestraGastoTransferencia = false;
	
	$scope.data.oConfiguracionTipoEvento={
		tipo : "combobox-tipo-evento-capital",
		configuracion : {},
		filtros : { 	
			IdEstado :	{"field" : "IdEstado", "operator" : "eq", "value" : 'ACT'},
			IdTipoEventoCapital: {"field": "IdTipoEventoCapital", "operator": "neq", "value": 'SU'} //Quitar opcion de Emision Suscripcion SU
		},
		idActual : 'DV',
		seleccionado : false,
		idDefecto : 'DV'
	};
	
	//GCBSXNEC-1 jmedina_ext
	$scope.data.oConfiguracionSecuenciaRelacionada={
		tipo: "combobox-secuencia-relacionada",
		configuracion: {},
		filtros: {},
		activado: false,
		autoCargar: false
	};
	
	$scope.data.oConfiguracionBoveda={
			tipo : "combobox-boveda",
			configuracion : {},
			filtros : { 	
				IdEstado :	{"field" : "IdEstado", "operator" : "eq", "value" : 'VIG'}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : 'INT'
		};
	    
	$scope.data.oConfiguracionMoneda={
		tipo : "combobox-moneda",
		configuracion : {},
		filtros : { 	
			IdEstado :	{field : "IdEstado", operator : "eq", value : "ACT"}
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};

	$scope.data.oConfiguracionMonedaLiquidacion={
		tipo : "combobox-moneda-tesoreria",
		configuracion : {},
		filtros : { 	
			IdEstado :	{field : "IdEstado", operator : "eq", value : "ACT"}
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	var admEveCap = this;
  		admEveCap.winId 		= bcsComunes.ventanaWinId($scope);
  		
	var ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico(idVentana,40);
    }
	
	
	var initUI = function(Codigo){
		switch (Codigo){
			case "CN":
				$scope.data.MuestraInstrumentoNuevo = true;
				$scope.data.KngDisabledNumDividendo = false;
				$scope.data.KngDisabledFactor = true;
				$scope.data.KngDisabledValorFactor = true;
				$scope.data.ngDisabledSwitchInscrito = true;
				$scope.data.oConfiguracionMoneda.activado = false;
				$scope.data.oConfiguracionMoneda.idActual = '';
				$scope.data.oConfiguracionMoneda.seleccionado = false;
				$scope.data.oConfiguracionMonedaLiquidacion.activado = false;
				$scope.data.oConfiguracionMonedaLiquidacion.idActual = '';
				$scope.data.oConfiguracionMonedaLiquidacion.seleccionado = false;
				$scope.data.kngDisabledDescripcion = false;
				$scope.data.kngDisabledFechaLimite = false;
				$scope.data.kngDisabledFechaPago = false;
				$scope.data.oConfiguracionBoveda.visible = false;
				$scope.data.oConfiguracionBoveda.idActual = 'DCV';
				$scope.data.ngDisabledSwitchISFUT = true;
				$scope.data.MuestraSecuenciaRealacionada = false;
				$scope.data.MuestraSuscripcionComplementaria = false;
				$scope.data.ngDisabledSwitchswitchSuscripcionComplementaria = true;
				$scope.data.ngDisabledSwitchRETART104 = true;
				$scope.data.MuestraImpuestoDivExtranjero = false;
				$scope.data.KngDisabledImpuestoDivExtranjero = true;
				$scope.data.kngImpuestoDivExtranjero = 0;
				$scope.data.MuestraGastoTransferencia = false;
				$scope.data.KngDisabledGastoTransferencia = true;
				$scope.data.kngGastoTransferencia = 0;
				ajustaVentana();
				break;
			case "CA":
				$scope.data.MuestraInstrumentoNuevo = false;
				$scope.data.KngDisabledNumDividendo = false;
				$scope.data.KngDisabledFactor = false;
				$scope.data.KngDisabledValorFactor = false;
				$scope.data.ngDisabledSwitchInscrito = true;
				$scope.data.oConfiguracionMoneda.activado = false;
				$scope.data.oConfiguracionMoneda.idActual = '';
				$scope.data.oConfiguracionMoneda.seleccionado = false;
				$scope.data.oConfiguracionMonedaLiquidacion.activado = false;
				$scope.data.oConfiguracionMonedaLiquidacion.idActual = '';
				$scope.data.oConfiguracionMonedaLiquidacion.seleccionado = false;
				$scope.data.kngDisabledDescripcion = false;
				$scope.data.kngDisabledFechaLimite = false;
				$scope.data.kngDisabledFechaPago = false;
				$scope.data.oConfiguracionBoveda.visible = false;
				$scope.data.oConfiguracionBoveda.idActual = 'DCV';
				$scope.data.ngDisabledSwitchISFUT = true;
				$scope.data.MuestraSecuenciaRealacionada = false;
				$scope.data.MuestraSuscripcionComplementaria = false;
				$scope.data.ngDisabledSwitchswitchSuscripcionComplementaria = true;
				$scope.data.ngDisabledSwitchRETART104 = true;
				$scope.data.MuestraImpuestoDivExtranjero = false;
				$scope.data.KngDisabledImpuestoDivExtranjero = true;
				$scope.data.kngImpuestoDivExtranjero = 0;
				$scope.data.MuestraGastoTransferencia = false;
				$scope.data.KngDisabledGastoTransferencia = true;
				$scope.data.kngGastoTransferencia = 0;
				ajustaVentana();
				break;
			case "DS":
				$scope.data.MuestraInstrumentoNuevo = true;
				$scope.data.KngDisabledNumDividendo = false;
				$scope.data.KngDisabledFactor = false;
				$scope.data.KngDisabledValorFactor = false;
				$scope.data.ngDisabledSwitchInscrito = true;
				$scope.data.oConfiguracionMoneda.activado = false;
				$scope.data.oConfiguracionMoneda.idActual = '';
				$scope.data.oConfiguracionMoneda.seleccionado = false;
				$scope.data.oConfiguracionMonedaLiquidacion.activado = false;
				$scope.data.oConfiguracionMonedaLiquidacion.idActual = '';
				$scope.data.oConfiguracionMonedaLiquidacion.seleccionado = false;
				$scope.data.kngDisabledDescripcion = false;
				$scope.data.kngDisabledFechaLimite = false;
				$scope.data.kngDisabledFechaPago = false;
				$scope.data.oConfiguracionBoveda.visible = false;
				$scope.data.ngDisabledSwitchISFUT = true;
				$scope.data.MuestraSecuenciaRealacionada = false;
				$scope.data.MuestraSuscripcionComplementaria = false;
				$scope.data.ngDisabledSwitchswitchSuscripcionComplementaria = true;
				$scope.data.ngDisabledSwitchRETART104 = true;
				$scope.data.MuestraImpuestoDivExtranjero = false;
				$scope.data.KngDisabledImpuestoDivExtranjero = true;
				$scope.data.kngImpuestoDivExtranjero = 0;
				$scope.data.MuestraGastoTransferencia = false;
				$scope.data.KngDisabledGastoTransferencia = true;
				$scope.data.kngGastoTransferencia = 0;
				ajustaVentana();
				break;
			case "DF":
				$scope.data.MuestraInstrumentoNuevo = false;
				$scope.data.KngDisabledNumDividendo = false;
				$scope.data.KngDisabledFactor = false;
				$scope.data.KngDisabledValorFactor = true;
				$scope.data.ngDisabledSwitchInscrito = true;
				$scope.data.oConfiguracionMoneda.activado = false;
				$scope.data.oConfiguracionMoneda.idActual = '';
				$scope.data.oConfiguracionMoneda.seleccionado = false;
				$scope.data.oConfiguracionMonedaLiquidacion.activado = false;
				$scope.data.oConfiguracionMonedaLiquidacion.idActual = '';
				$scope.data.oConfiguracionMonedaLiquidacion.seleccionado = false;
				$scope.data.kngDisabledDescripcion = false;
				$scope.data.kngDisabledFechaLimite = false;
				$scope.data.kngDisabledFechaPago = false;
				$scope.data.oConfiguracionBoveda.visible = false;
				$scope.data.ngDisabledSwitchISFUT = true;
				$scope.data.MuestraSecuenciaRealacionada = false;
				$scope.data.MuestraSuscripcionComplementaria = false;
				$scope.data.ngDisabledSwitchswitchSuscripcionComplementaria = true;
				$scope.data.ngDisabledSwitchRETART104 = true;
				$scope.data.MuestraImpuestoDivExtranjero = false;
				$scope.data.KngDisabledImpuestoDivExtranjero = true;
				$scope.data.kngImpuestoDivExtranjero = 0;
				$scope.data.MuestraGastoTransferencia = false;
				$scope.data.KngDisabledGastoTransferencia = true;
				$scope.data.kngGastoTransferencia = 0;
				if($scope.data.Accion == 'Crear'){
					
					$scope.data.oConfiguracionMoneda.idActual = $scope.SesionIdMonedaSistema;
					$scope.data.oConfiguracionMonedaLiquidacion.idActual = $scope.SesionIdMonedaSistema;
				}
				else if($scope.data.Accion == 'Modificar'){
					$scope.data.oConfiguracionMoneda.idActual = objEventoCapital.IdMoneda;
					$scope.data.oConfiguracionMonedaLiquidacion.idActual = objEventoCapital.IdMonedaLiquidacion;
				}

				ajustaVentana();
				break;
			case "EL":
				$scope.data.MuestraInstrumentoNuevo = false;
				$scope.data.KngDisabledNumDividendo = false;
				$scope.data.KngDisabledFactor = false;
				$scope.data.KngDisabledValorFactor = true;
				$scope.data.ngDisabledSwitchInscrito = true;
				$scope.data.oConfiguracionMoneda.activado = false;
				$scope.data.oConfiguracionMoneda.seleccionado = false;
				$scope.data.oConfiguracionMonedaLiquidacion.activado = false;
				$scope.data.oConfiguracionMonedaLiquidacion.seleccionado = false;
				$scope.data.kngDisabledDescripcion = false;
				$scope.data.kngDisabledFechaLimite = false;
				$scope.data.kngDisabledFechaPago = false;
				$scope.data.oConfiguracionBoveda.visible = false;
				$scope.data.oConfiguracionBoveda.idActual = 'DCV';
				$scope.data.ngDisabledSwitchISFUT = true;
				$scope.data.ngDisabledSwitchRETART104 = true;
				$scope.data.MuestraImpuestoDivExtranjero = false;
				$scope.data.KngDisabledImpuestoDivExtranjero = true;
				$scope.data.kngImpuestoDivExtranjero = 0;
				$scope.data.MuestraGastoTransferencia = false;
				$scope.data.KngDisabledGastoTransferencia = true;
				$scope.data.kngGastoTransferencia = 0;
				if($scope.data.Accion == 'Crear'){
					$scope.data.oConfiguracionMoneda.idActual = '';
					$scope.data.oConfiguracionMonedaLiquidacion.idActual = '';
				}
				else if($scope.data.Accion == 'Modificar'){
					$scope.data.oConfiguracionMoneda.idActual = objEventoCapital.IdMoneda;
					$scope.data.oConfiguracionMonedaLiquidacion.idActual = objEventoCapital.IdMonedaLiquidacion;
				}
        $scope.data.MuestraSecuenciaRealacionada = false;
				$scope.data.MuestraSuscripcionComplementaria = false;
				$scope.data.ngDisabledSwitchswitchSuscripcionComplementaria = true;
				ajustaVentana();
				break;
			case "EP":
				$scope.data.MuestraInstrumentoNuevo = false;
				$scope.data.KngDisabledNumDividendo = false;
				$scope.data.KngDisabledFactor = false;
				$scope.data.KngDisabledValorFactor = false;
				$scope.data.ngDisabledSwitchInscrito = true;
				$scope.data.oConfiguracionMoneda.activado = true;
				$scope.data.oConfiguracionMoneda.idActual = $scope.data.idMoneda;
				$scope.data.oConfiguracionMoneda.seleccionado = ($scope.data.idMoneda != ''  && $scope.data.idMoneda != undefined) ? true : false;
				$scope.data.oConfiguracionMonedaLiquidacion.activado = true;
				$scope.data.oConfiguracionMonedaLiquidacion.idActual = $scope.data.idMonedaLiquidacion;
				$scope.data.oConfiguracionMonedaLiquidacion.seleccionado = ($scope.data.idMonedaLiquidacion != ''  && $scope.data.idMonedaLiquidacion != undefined) ? true : false;
				$scope.data.kngDisabledDescripcion = false;
				$scope.data.kngDisabledFechaLimite = false;
				$scope.data.kngDisabledFechaPago = false;
				$scope.data.oConfiguracionBoveda.visible = false;
				$scope.data.oConfiguracionBoveda.idActual = 'DCV';
				$scope.data.ngDisabledSwitchISFUT = true;
				$scope.data.MuestraSecuenciaRealacionada = true;
				$scope.data.MuestraSuscripcionComplementaria = true;
				$scope.data.MuestraImpuestoDivExtranjero = false;
				$scope.data.KngDisabledImpuestoDivExtranjero = true;
				$scope.data.kngImpuestoDivExtranjero = 0;
				$scope.data.MuestraGastoTransferencia = false;
				$scope.data.KngDisabledGastoTransferencia = true;
				$scope.data.kngGastoTransferencia = 0;
				if($scope.data.Accion == 'Crear'){
					$scope.data.ngDisabledSwitchswitchSuscripcionComplementaria = true;                 
				}
				else if($scope.data.Accion == 'Modificar'){
					$scope.data.ngDisabledSwitchswitchSuscripcionComplementaria = $scope.data.switchIsEmpty ? true : false;
				}
				$scope.data.ngDisabledSwitchRETART104 = true;
				ajustaVentana();
				break;
			case "SU":
				$scope.data.MuestraInstrumentoNuevo = false;
				$scope.data.KngDisabledNumDividendo = false;
				$scope.data.KngDisabledFactor = false;
				$scope.data.KngDisabledValorFactor = false;
				$scope.data.ngDisabledSwitchInscrito = true;
				$scope.data.oConfiguracionMoneda.activado = false;
				$scope.data.oConfiguracionMoneda.idActual = '';
				$scope.data.oConfiguracionMoneda.seleccionado = false;
				
		
				$scope.data.oConfiguracionMonedaLiquidacion.activado = false;
				$scope.data.oConfiguracionMonedaLiquidacion.idActual = '';
				$scope.data.oConfiguracionMonedaLiquidacion.seleccionado = false;
				
				
				$scope.data.kngDisabledDescripcion = false;
				$scope.data.kngDisabledFechaLimite = false;
				$scope.data.kngDisabledFechaPago = false;
				$scope.data.oConfiguracionBoveda.visible = false;
				$scope.data.oConfiguracionBoveda.idActual = 'DCV';
				$scope.data.ngDisabledSwitchISFUT = true;
				$scope.data.MuestraSecuenciaRealacionada = false;
				$scope.data.MuestraSuscripcionComplementaria = false;
				$scope.data.ngDisabledSwitchswitchSuscripcionComplementaria = true;
				$scope.data.ngDisabledSwitchRETART104 = true;
				$scope.data.MuestraImpuestoDivExtranjero = false;
				$scope.data.KngDisabledImpuestoDivExtranjero = true;
				$scope.data.kngImpuestoDivExtranjero = 0;
				$scope.data.MuestraGastoTransferencia = false;
				$scope.data.KngDisabledGastoTransferencia = true;
				$scope.data.kngGastoTransferencia = 0;
				ajustaVentana();
				break;
			case "DA":
				$scope.data.KngDisabledNumDividendo = false;
				$scope.data.MuestraInstrumentoNuevo = false;
				$scope.data.oConfiguracionMoneda.activado = false;
				$scope.data.oConfiguracionMonedaLiquidacion.activado = false;
				$scope.data.KngDisabledFactor = false;
				$scope.data.KngDisabledValorFactor = true;
				$scope.data.oConfiguracionMoneda.idActual = '';
				$scope.data.oConfiguracionMoneda.seleccionado = false;
				$scope.data.oConfiguracionMonedaLiquidacion.idActual = '';
				$scope.data.oConfiguracionMonedaLiquidacion.seleccionado = false;
				$scope.data.oConfiguracionBoveda.activado = true;
				$scope.data.oConfiguracionBoveda.visible = true;
				$scope.data.kngDisabledDescripcion = false;
				$scope.data.kngDisabledFechaLimite = false;
				$scope.data.kngDisabledFechaPago = false;
				$scope.data.oConfiguracionBoveda.idActual = 'DCV';
				$scope.data.ngDisabledSwitchISFUT = true;
				$scope.data.MuestraSecuenciaRealacionada = false;
				$scope.data.MuestraSuscripcionComplementaria = false;
				$scope.data.ngDisabledSwitchswitchSuscripcionComplementaria = true;
				$scope.data.ngDisabledSwitchRETART104 = true;
				$scope.data.MuestraImpuestoDivExtranjero = false;
				$scope.data.KngDisabledImpuestoDivExtranjero = true;
				$scope.data.kngImpuestoDivExtranjero = 0;
				$scope.data.MuestraGastoTransferencia = false;
				$scope.data.KngDisabledGastoTransferencia = true;
				$scope.data.kngGastoTransferencia = 0;
				ajustaVentana();
				break;
			case "DV":
				$scope.data.KngDisabledNumDividendo = false;
				$scope.data.MuestraInstrumentoNuevo = false;
				$scope.data.KngDisabledFactor = false;
				$scope.data.kngValorFactor = 1;
				$scope.data.KngDisabledValorFactor = true;
				$scope.data.kngDisabledDescripcion = false;
				$scope.data.kngDisabledFechaLimite = false;
				$scope.data.kngDisabledFechaPago = false;
				$scope.data.oConfiguracionBoveda.activado = true;
				$scope.data.oConfiguracionMoneda.activado = true;
				$scope.data.oConfiguracionMonedaLiquidacion.activado = true;
				$scope.data.oConfiguracionBoveda.visible = true;
				$scope.data.oConfiguracionBoveda.idActual = 'DCV';
				$scope.data.ngDisabledSwitchISFUT = false;
				$scope.data.MuestraSecuenciaRealacionada = false;
				$scope.data.MuestraSuscripcionComplementaria = false;
				$scope.data.ngDisabledSwitchswitchSuscripcionComplementaria = true;
				$scope.data.ngDisabledSwitchRETART104 = true;
				$scope.data.MuestraImpuestoDivExtranjero = true;
				$scope.data.KngDisabledImpuestoDivExtranjero = true;
				$scope.data.MuestraGastoTransferencia = true;
				$scope.data.KngDisabledGastoTransferencia = true;
				
				if($scope.data.Accion == 'Crear'){
					$scope.data.kngImpuestoDivExtranjero = 0;
					$scope.data.kngGastoTransferencia = 0;
					$scope.data.oConfiguracionMoneda.idActual = $scope.SesionIdMonedaSistema;
					$scope.data.oConfiguracionMonedaLiquidacion.idActual = $scope.SesionIdMonedaSistema;
				}
				else if($scope.data.Accion == 'Modificar'){
					$scope.data.kngImpuestoDivExtranjero = objEventoCapital.ImpuestoDivExtranjero;
					$scope.data.kngGastoTransferencia = objEventoCapital.GastoTransferencia;
					$scope.data.oConfiguracionMoneda.idActual = objEventoCapital.IdMoneda;
					$scope.data.oConfiguracionMonedaLiquidacion.idActual = objEventoCapital.IdMonedaLiquidacion;
				}
				ajustaVentana();
				break;
				
				
			
			case "RC":       //  REPARTO DE CAPITAL
				$scope.data.KngDisabledNumDividendo = false;
				$scope.data.MuestraInstrumentoNuevo = false;
				$scope.data.KngDisabledFactor = false;
				$scope.data.kngValorFactor = 1;
				$scope.data.KngDisabledValorFactor = true;
				$scope.data.kngDisabledDescripcion = false;
				$scope.data.kngDisabledFechaLimite = false;
				$scope.data.kngDisabledFechaPago = false;
				$scope.data.oConfiguracionBoveda.activado = true;
				$scope.data.oConfiguracionMoneda.activado = true;
				$scope.data.oConfiguracionMonedaLiquidacion.activado = true;
				$scope.data.oConfiguracionBoveda.visible = true;
				$scope.data.oConfiguracionBoveda.idActual = 'DCV';
				$scope.data.ngDisabledSwitchISFUT = true;
				$scope.data.MuestraSecuenciaRealacionada = false;
				$scope.data.MuestraSuscripcionComplementaria = false;
				$scope.data.ngDisabledSwitchswitchSuscripcionComplementaria = true;
				$scope.data.ngDisabledSwitchRETART104 = true;
				$scope.data.MuestraImpuestoDivExtranjero = false;
				$scope.data.KngDisabledImpuestoDivExtranjero = true;
				$scope.data.kngImpuestoDivExtranjero = 0;
				$scope.data.MuestraGastoTransferencia = false;
				$scope.data.KngDisabledGastoTransferencia = true;
				$scope.data.kngGastoTransferencia = 0;
				if($scope.data.Accion == 'Crear'){
					
					$scope.data.oConfiguracionMoneda.idActual = $scope.SesionIdMonedaSistema;
					$scope.data.oConfiguracionMonedaLiquidacion.idActual = $scope.SesionIdMonedaSistema;
				}
				else if($scope.data.Accion == 'Modificar'){
					$scope.data.oConfiguracionMoneda.idActual = objEventoCapital.IdMoneda;
					$scope.data.oConfiguracionMonedaLiquidacion.idActual = objEventoCapital.IdMonedaLiquidacion;
				}
				ajustaVentana();
				break;
				
			case "CC":
				$scope.data.MuestraInstrumentoNuevo = false;
				$scope.data.KngDisabledNumDividendo = false;
				$scope.data.KngDisabledFactor = false;
				$scope.data.KngDisabledValorFactor = false;
				$scope.data.ngDisabledSwitchInscrito = false;
				$scope.data.oConfiguracionMoneda.activado = true;
				$scope.data.oConfiguracionMonedaLiquidacion.activado =  true;
				$scope.data.oConfiguracionMonedaLiquidacion.idActual = '';
				$scope.data.oConfiguracionMonedaLiquidacion.seleccionado = false;
				$scope.data.ngDisabledSwitchISFUT = true;
				$scope.data.MuestraSecuenciaRealacionada = false;
				$scope.data.MuestraSuscripcionComplementaria = false;
				$scope.data.ngDisabledSwitchswitchSuscripcionComplementaria = true;
				$scope.data.ngDisabledSwitchRETART104 = true;
				$scope.MuestraRetencionArt104 = true;
				$scope.data.MuestraImpuestoDivExtranjero = false;
				$scope.data.KngDisabledImpuestoDivExtranjero = true;
				$scope.data.kngImpuestoDivExtranjero = 0;
				$scope.data.MuestraGastoTransferencia = false;
				$scope.data.KngDisabledGastoTransferencia = true;
				$scope.data.kngGastoTransferencia = 0;
				if($scope.data.Accion == 'Crear'){
					
					$scope.data.oConfiguracionMoneda.idActual = $scope.SesionIdMonedaSistema;
					$scope.data.oConfiguracionMonedaLiquidacion.idActual = $scope.SesionIdMonedaSistema;
				}
				else if($scope.data.Accion == 'Modificar'){
					$scope.data.oConfiguracionMoneda.idActual = objEventoCapital.IdMoneda;
					$scope.data.oConfiguracionMonedaLiquidacion.idActual = objEventoCapital.IdMonedaLiquidacion;
				}
				ajustaVentana();
				break;
				
			default:
				$scope.data.MuestraInstrumentoNuevo = false;
				$scope.data.KngDisabledNumDividendo = false;
				$scope.data.KngDisabledFactor = false;
				$scope.data.KngDisabledValorFactor = false;
				$scope.data.ngDisabledSwitchInscrito = false;
				$scope.data.oConfiguracionMoneda.activado = ($scope.data.oConfiguracionTipoEvento.seleccionado.Codigo === "PB") ? false : true;
				$scope.data.oConfiguracionMonedaLiquidacion.activado =  ($scope.data.oConfiguracionTipoEvento.seleccionado.Codigo === "PB") ? false : true;
				$scope.data.oConfiguracionMonedaLiquidacion.idActual = '';
				$scope.data.oConfiguracionMonedaLiquidacion.seleccionado = false;
				$scope.data.ngDisabledSwitchISFUT = true;
				$scope.data.MuestraSecuenciaRealacionada = false;
				$scope.data.MuestraSuscripcionComplementaria = false;
				$scope.data.ngDisabledSwitchswitchSuscripcionComplementaria = true;
				$scope.data.ngDisabledSwitchRETART104 = true;
				$scope.data.MuestraImpuestoDivExtranjero = false;
				$scope.data.KngDisabledImpuestoDivExtranjero = true;
				$scope.data.kngImpuestoDivExtranjero = 0;
				$scope.data.MuestraGastoTransferencia = false;
				$scope.data.KngDisabledGastoTransferencia = true;
				$scope.data.kngGastoTransferencia = 0;

				ajustaVentana();
		}
	};

	// CONSULTA NÚMERO DE SECUENCIA
	$scope.$watch('data.kngNumDividendo', function(newval,oldval){
		if (objEventoCapital != undefined)
			return;
		var datos = {};	
		
		datos.p_IdEmpresa 			= $scope.SesionIdEmpresa; //saca de variables de entorno
		datos.p_IdUsuario 			= $scope.SesionIdUsuario; //saca de variables de entorno
		datos.p_IdModulo 			= 'CUS';
		
		datos.p_IdTipoEventoCapital = $scope.data.oConfiguracionTipoEvento.seleccionado.Codigo || '';
		datos.p_NumeroDividendo 	= Number($scope.data.kngNumDividendo) || 0;
		

		bcsServiciosEventoCapital.ConsultaSecuenciaEvento(datos,
			function(respuesta)	{	
				if (respuesta.P_OUT_ERRORESCONTADOR == "0" && respuesta.P_OUT_ADVERTENCIASCONTADOR == "0"){
					$scope.data.kngSecuencia = respuesta.P_OUT_SECUENCIAEVENTOCAPITAL;
				}else{
					$scope.data.kngSecuencia = 0;
				}	 
			}
		);
	});


	// $scope.$watch('filtro.instrumentoObjeto', function(newval,oldval){
	// 	if ($scope.filtro == undefined || $scope.filtro.instrumentoObjeto == undefined)
	// 	return;
	// 	$scope.data.switchISFUT = $scope.filtro.instrumentoObjeto.AplicaISFUT == "S" ? true : false;
	// });

	$scope.$watch('data.switchISFUT', function(newval, oldval){
		if(newval === false) {
			$scope.data.kngTasaEfectiva = 0;
		}
		if(newval === true) {
			$scope.data.kngTasaEfectiva = objEventoCapital?.TasaEfectiva ? objEventoCapital.TasaEfectiva : 0.1;
		}
	});

	$scope.$watch('data.oConfiguracionTipoEvento.seleccionado', function(newval,oldval){		
		//se deben ocultar o mostrar ciertos campos dependiendo del tipo de evento que se trate.
		if(newval && parametrosVentana.Accion === 'Crear'){
			initUI($scope.data.oConfiguracionTipoEvento.seleccionado.Codigo);
			$scope.data.instrumentoObjeto 		= undefined;
			$scope.data.IdInstrumento 			= '-1';
			$scope.data.instrumentoNuevoObjeto 	= undefined;
			$scope.data.IdInstrumentoNuevo 		= '-1';
			
			if ($scope.data.oConfiguracionTipoEvento.seleccionado.Codigo !== undefined){
				$scope.data.disabledInstrumento = false;
				
				if ($scope.data.oConfiguracionTipoEvento.seleccionado.IdClasificacionTipoInstrumento === '' || $scope.data.oConfiguracionTipoEvento.seleccionado.IdClasificacionTipoInstrumento === undefined){
					$scope.data.filtroClasificacionTipoInstrumento = ['RV','RF'];
				} else {
					$scope.data.filtroClasificacionTipoInstrumento = $scope.data.oConfiguracionTipoEvento.seleccionado.IdClasificacionTipoInstrumento;
				}
				
			} else {
				$scope.data.disabledInstrumento = true;
				bcsComunes.ventanaMsg({mensaje: $translate.instant("SELECCIONAR_TIPO_EVENTO")},$scope.IdVentana);
			}
		}
		
		if ($scope.data.oConfiguracionTipoEvento.seleccionado.Codigo === 'DV'){
			$scope.data.MuestraImpuestoDivExtranjero = true;
			$scope.data.MuestraGastoTransferencia = true;
		} else{
			$scope.data.MuestraImpuestoDivExtranjero = false;
			$scope.data.MuestraGastoTransferencia = false;
		}

		InicializarFechasEventoCapital();
  	},true);
	
	$scope.$watchGroup(['data.oConfiguracionMoneda.seleccionado','data.oConfiguracionMonedaLiquidacion.seleccionado'], function(newval,oldval){
		if ($scope.data.oConfiguracionMoneda.seleccionado.Codigo != $scope.data.oConfiguracionMonedaLiquidacion.seleccionado.Codigo 
		&& ['COP','PEN'].includes($scope.data.oConfiguracionMoneda.seleccionado.Codigo)
		&& $scope.data.oConfiguracionTipoEvento.seleccionado.Codigo == 'DV') {
			ConsultarParidad();
			$scope.data.KngDisabledParidad = false;
		} else if ($scope.data.oConfiguracionMoneda.seleccionado.Codigo != $scope.data.oConfiguracionMonedaLiquidacion.seleccionado.Codigo){
			$scope.data.kngParidad = (objEventoCapital === undefined || objEventoCapital.Paridad === undefined) ? 1 : objEventoCapital.Paridad;
			$scope.data.KngDisabledParidad = false;
		} else {
			$scope.data.kngParidad = 1;
			$scope.data.KngDisabledParidad = true;
		}
	},true);
	
	$scope.$watch('data.kngFechaLimite', function(newval, oldval) {
		if (newval == oldval) return;
		const accionVentana = parametrosVentana.Accion;
		const fechaLimiteDate = util.date($scope.data.kngFechaLimite).toDate();
		let fechaPagoDate = util.dateInicioDia(fechaLimiteDate);
	
		if (accionVentana === 'Modificar' && objEventoCapital) {
			const { FechaPago } = objEventoCapital;
			fechaPagoDate = util.dateInicioDia(FechaPago);
		}

		const fechaPagoFinal = fechaLimiteDate > fechaPagoDate
								? util.dateInicioDia(fechaLimiteDate)
								: fechaPagoDate;

		$scope.data.kngFechaPago = fechaPagoFinal;
		$scope.data.kngLimiteFechaPago = fechaLimiteDate;
	});

    $scope.$watch('filtro.instrumentoObjeto', function(newval,oldval){
        if ($scope.filtro == undefined || $scope.filtro.instrumentoObjeto == undefined) {
            return
        }
        $scope.data.oConfiguracionSecuenciaRelacionada.autoCargar = true;
        $scope.data.oConfiguracionSecuenciaRelacionada.filtros = {
            IdInstrumento:  { "field" : "IdInstrumento", "operator" : "eq", "value" : $scope.filtro.instrumentoObjeto.IdInstrumento }
        }
		if($scope.data.oConfiguracionTipoEvento.seleccionado.Codigo == 'DV' && ['COP','PEN'].includes($scope.filtro.instrumentoObjeto.IdMoneda)){
			$scope.data.oConfiguracionMoneda.idActual = $scope.filtro.instrumentoObjeto.IdMoneda;
		}
		$scope.data.switchInscrito = $scope.filtro.instrumentoObjeto.EstaInscrito == "S" ? true : false;
    });

	$scope.$watchGroup(['data.oConfiguracionTipoEvento.seleccionado','filtro.instrumentoObjeto'], function(newval,oldval){
		if($scope.data.oConfiguracionTipoEvento.seleccionado.Codigo != 'DV'){
			$scope.data.AplicaImpuestoDivExtranjero = 'N';
			$scope.data.PorcentajeImpuestoExtranjero = 0;
			$scope.data.KngDisabledImpuestoDivExtranjero = true;
			$scope.data.AplicaGastoTransferencia = 'N';
			$scope.data.KngDisabledImpuestoDivExtranjero = true;
		}
		if($scope.data.oConfiguracionTipoEvento.seleccionado.Codigo === 'DV'){
			ConsultarImpuestoDivExtranjero();
		}
	},true);

	$scope.$watch('data.AplicaImpuestoDivExtranjero', function(newval,oldval){
		if ($scope.data.AplicaImpuestoDivExtranjero === 'S'){
			$scope.data.KngDisabledImpuestoDivExtranjero = false;
			if ($scope.data.Accion === "Crear"){
				$scope.data.kngImpuestoDivExtranjero = $scope.data.PorcentajeImpuestoExtranjero;
			}else if ($scope.data.Accion === "Modificar"){
				$scope.data.kngImpuestoDivExtranjero = objEventoCapital.ImpuestoDivExtranjero;
			}
		}else{
			$scope.data.KngDisabledImpuestoDivExtranjero = true;
			$scope.data.kngImpuestoDivExtranjero = 0;
		}
	});

	$scope.$watch('data.AplicaGastoTransferencia', function(newval,oldval){
		if ($scope.data.AplicaGastoTransferencia === 'S'){
			$scope.data.KngDisabledGastoTransferencia = false;
			if ($scope.data.Accion === "Crear"){
				$scope.data.kngGastoTransferencia = 0;
			}else if ($scope.data.Accion === "Modificar"){
				$scope.data.kngGastoTransferencia = objEventoCapital.GastoTransferencia;
			}
		}else{
			$scope.data.KngDisabledGastoTransferencia = true;
			$scope.data.kngGastoTransferencia = 0;
		}
	});

	$scope.$watch('data.kngImpuestoDivExtranjero', function(newval,oldval){
		if ($scope.data.kngImpuestoDivExtranjero > 100){
			$scope.data.kngImpuestoDivExtranjero = 100;
		}
	})

    $scope.$watch('data.oConfiguracionSecuenciaRelacionada.seleccionado', function(newval,oldval){
        $scope.data.ngDisabledSwitchswitchSuscripcionComplementaria = true;
		$scope.data.switchIsEmpty = newval === false ? true : false;

        if($scope.data.Accion == 'Crear') {
            if ($scope.data.oConfiguracionSecuenciaRelacionada.seleccionado === undefined ||
                $scope.data.oConfiguracionSecuenciaRelacionada.seleccionado === false ||
                $scope.data.oConfiguracionSecuenciaRelacionada.seleccionado === '')
            {
                $scope.data.switchSuscripcionComplementaria = false;
                $scope.data.ngDisabledSwitchswitchSuscripcionComplementaria = true;
                return
            } else {
                $scope.data.switchSuscripcionComplementaria = false;
                $scope.data.ngDisabledSwitchswitchSuscripcionComplementaria = false;
            }
        } else if($scope.data.Accion == 'Modificar'){
            if ($scope.data.switchIsEmpty === true) {
                $scope.data.ngDisabledSwitchswitchSuscripcionComplementaria = true;
                $scope.data.switchSuscripcionComplementaria = false;
            } else {
                $scope.data.ngDisabledSwitchswitchSuscripcionComplementaria = false;
				        $scope.data.switchSuscripcionComplementaria = true;
            }
        }
    });

	$scope.$watch('data.oConfiguracionMoneda.idActual', function(newval,oldval){
		if ($scope.data.oConfiguracionTipoEvento.seleccionado.Codigo != 'DV') return;
		if (['COP','PEN'].includes(newval)){
			$scope.data.oConfiguracionMonedaLiquidacion.idActual = 'USD';
			$scope.data.oConfiguracionMonedaLiquidacion.activado = false;
		} else {
			$scope.data.oConfiguracionMonedaLiquidacion.activado = true;
		}
	});
    
/*************************************************************************
* CARGA INICIAL
**************************************************************************/
  	$scope.data.Accion = parametrosVentana.Accion;
	$scope.data.kngSecuencia = 0;
	$scope.data.kngNumDividendo = 0;
	$scope.data.UsaSecuenciaRelacionada = $scope.data.Accion !== 'Modificar' || objEventoCapital.IdEstado === 'VIG';
	$scope.data.NumDividendoOptions = {format: 'n0'};	
	$scope.data.porcentajeFactorOptions = {format: 'n'+(util.bcs_formato(0,'CUS','Tasa',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
	$scope.data.kngFactor = 0;
	//$scope.data.porcentajeValorFactorOptions = {format: 'n'+(util.bcs_formato(0,'CUS','Tasa',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
	$scope.data.porcentajeImpuestoOptions = {format: 'n'+(util.bcs_formato(0,'CUS','Tasa',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
	$scope.data.cuotasOptions = {format: 'n'+(util.bcs_formato(0,'CUS','Cantidad',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
	$scope.data.montoOptions = {format: 'n'+(util.bcs_formato(0,'CUS','Monto',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
	$scope.data.kngValorFactor = 1;
	$scope.data.accionDisabled = true;
	$scope.data.MuestraCuentaDCV = false;
	$scope.data.MuestraParidad = true;	
	$scope.data.btnCerrar = function(refrescar) {
		bcsComunes.ventanaData(idVentana,refrescar);
		bcsComunes.ventanaCerrar(idVentana);
	};

	$scope.data.kngTasaEfectiva = 0;
	$scope.data.TasaEfectivaOptions = {format: 'n'+(util.bcs_formato(0,'CUS','Tasa',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales, min: 0.1, max: 30};

 
/*************************************************************************
* MODIFICAR
**************************************************************************/	
 	if (parametrosVentana.EventoCapital){
		$scope.data.idPostFiltro 			= objEventoCapital.IdTipoEventoCapital;
		$scope.data.IdInstrumento 			= objEventoCapital.Nemo;
		$scope.data.kngSecuencia 			= objEventoCapital.Secuencia;
		$scope.data.kngNumDividendo 		= objEventoCapital.NumeroDividendo;
		$scope.data.Descripcion				= objEventoCapital.Descripcion;		
		$scope.data.kngNumDividendo			= objEventoCapital.NumeroDividendo;	
		$scope.data.kngFactor 				= objEventoCapital.Factor;
		$scope.data.kngValorImpuesto		= objEventoCapital.Impuesto;
		$scope.data.kngValorFactor 			= objEventoCapital.FactorNuevo;
		$scope.data.switchInscrito 			= objEventoCapital.EsInscrito == "S" ? true : false;
		$scope.data.kngFechaLimite 			= objEventoCapital.FechaLimite;
		$scope.data.kngFechaPago 			= objEventoCapital.FechaPago;
		$scope.data.kngParidad 				= objEventoCapital.Paridad;
		
		$scope.data.TipoEventoTXT 			= objEventoCapital.TipoEvento;
		$scope.data.idInstrumentoTXT		= objEventoCapital.Nemo;
		$scope.data.idInstrumentoNuevoTXT	= objEventoCapital.NemoNuevo;
		$scope.data.kngSecuenciaTXT			= Number(objEventoCapital.Secuencia);
		$scope.data.kngNumDividendo			= Number($scope.data.kngNumDividendo) || 0;

		$scope.data.kngCuentaDCV			= objEventoCapital.CuentaDepositanteSorteoLetras;

		$scope.data.switchISFUT 			= objEventoCapital.ISFUT == "S" ? true : false;
		$scope.data.switchRETART104			= objEventoCapital.RETART104 == "S" ? true : false;
		$scope.data.kngTasaEfectiva			= objEventoCapital.TasaEfectiva;
		$scope.data.kngImpuestoDivExtranjero= objEventoCapital.ImpuestoDivExtranjero;
		$scope.data.kngGastoTransferencia	= objEventoCapital.GastoTransferencia;
    $scope.data.SecuenciaRelacionada    = objEventoCapital.SecuenciaRelacionada;
    $scope.data.ngDisabledSwitchswitchSuscripcionComplementaria = false;
    $scope.data.switchSuscripcionComplementaria = objEventoCapital.SuscripcionComplementaria === 'S' ? true : false;
	
		if (objEventoCapital.IdTipoEventoCapital == 'SL'){
			$scope.data.MuestraCuentaDCV = true;	
		}else{
			$scope.data.MuestraCuentaDCV = false;
		};
		
		switch (objEventoCapital.IdTipoEventoCapital){
			case "DA":
				$scope.data.KngDisabledValorFactor = true;
				$scope.data.oConfiguracionMoneda.activado = false;
				$scope.data.oConfiguracionMonedaLiquidacion.activado = false;
				$scope.data.ngDisabledSwitchInscrito = true;
				$scope.data.oConfiguracionMoneda.activado = false;
				$scope.data.oConfiguracionMoneda.seleccionado = false;
				$scope.data.oConfiguracionMonedaLiquidacion.activado = false;
				$scope.data.oConfiguracionMonedaLiquidacion.seleccionado = false;
				$scope.data.oConfiguracionMoneda.idActual = '';
				$scope.data.oConfiguracionMonedaLiquidacion.idActual = '';
				$scope.data.oConfiguracionMoneda.idDefecto = '';
				$scope.data.oConfiguracionMonedaLiquidacion.idDefecto = '';
				ajustaVentana();
				break;
			case "DV":
				$scope.data.idMoneda = objEventoCapital.IdMoneda;
				$scope.data.idMonedaLiquidacion = objEventoCapital.IdMonedaLiquidacion;
				$scope.data.oConfiguracionMoneda.idDefecto = objEventoCapital.IdMoneda;
				$scope.data.oConfiguracionMonedaLiquidacion.idDefecto = objEventoCapital.IdMonedaLiquidacion;
				$scope.data.KngDisabledValorFactor = true;
				$scope.data.ngDisabledSwitchInscrito = true;
				$scope.data.oConfiguracionMoneda.activado = true;
				$scope.data.oConfiguracionMoneda.seleccionado = true;
				$scope.data.oConfiguracionMonedaLiquidacion.activado = true;
				$scope.data.oConfiguracionMonedaLiquidacion.seleccionado = true;
				ajustaVentana();
				break;
				
			
			case "RC":   // REPARTO DE CAPITAL
				$scope.data.idMoneda = objEventoCapital.IdMoneda;
				$scope.data.idMonedaLiquidacion = objEventoCapital.IdMonedaLiquidacion;
				$scope.data.oConfiguracionMoneda.idDefecto = objEventoCapital.IdMoneda;
				$scope.data.oConfiguracionMonedaLiquidacion.idDefecto = objEventoCapital.IdMonedaLiquidacion;
				$scope.data.KngDisabledValorFactor = true;
				$scope.data.ngDisabledSwitchInscrito = true;
				$scope.data.oConfiguracionMoneda.activado = true;
				$scope.data.oConfiguracionMoneda.seleccionado = true;
				$scope.data.oConfiguracionMonedaLiquidacion.activado = true;
				$scope.data.oConfiguracionMonedaLiquidacion.seleccionado = true;
				ajustaVentana();
				break;
				
			case "EP":
				$scope.data.idMoneda = objEventoCapital.IdMoneda;
				$scope.data.idMonedaLiquidacion = objEventoCapital.IdMonedaLiquidacion;
				$scope.data.oConfiguracionMoneda.idActual = objEventoCapital.IdMoneda;
				$scope.data.oConfiguracionMonedaLiquidacion.idActual = objEventoCapital.IdMonedaLiquidacion;
				ajustaVentana();
				break;
			case "DF":
				$scope.data.kngCuotas = objEventoCapital.CantidadCuotas;
				ajustaVentana();
				break;
			default:
				ajustaVentana();
		}
		
 	}
 	
/*************************************************************************
* INGRESO - MODIFICACION
**************************************************************************/	
 	
 	
	$scope.data.CrearEventoCapital = function()
    {
		let t = "";
		
		
		if($scope.data.oConfiguracionBoveda.seleccionado.IdBoveda !== 'DCV'){
		if($scope.data.oConfiguracionBoveda.seleccionado.IdBoveda !== 'INT' && 
				($scope.filtro.instrumentoObjeto.IdTipoInstrumento== 'ACX' ||
				 $scope.filtro.instrumentoObjeto.IdTipoInstrumento== 'CFX' ||
				 $scope.filtro.instrumentoObjeto.IdTipoInstrumento== 'ETX'))
			{
			t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_GRABAR_INST_INT_EN_BOVEDA_NAC")+ "" ,$scope.idVentana);
			t.then(function(result)
				      {		
						 if (result === OPCIONES_BCSALERTA.SI){
							 GeneraEventoCap();
					      }
					   });	
			
			}else if($scope.data.oConfiguracionBoveda.seleccionado.IdBoveda == 'INT' && 
				       !($scope.filtro.instrumentoObjeto.IdTipoInstrumento== 'ACX' ||
						 $scope.filtro.instrumentoObjeto.IdTipoInstrumento== 'CFX' ||
						 $scope.filtro.instrumentoObjeto.IdTipoInstrumento== 'ETX'))
			{
			t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_GRABAR_INST_NAC_EN_BOVEDA_INT")+ "" ,$scope.idVentana);
			t.then(function(result)
				      {		
						 if (result === OPCIONES_BCSALERTA.SI){
							 GeneraEventoCap();
					      }
					   });	
			}else{
				GeneraEventoCap();
			}
		}else{
			GeneraEventoCap();
		}
		
  };
  
	function GeneraEventoCap(){
		var datos = {};	
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		$scope.data.accionDisabled = undefined;
		var eventosCapital	= [];
					
		datos.p_IdEmpresa = $scope.SesionIdEmpresa; //saca de variables de entorno
		datos.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
		datos.p_IdModulo = 'CUS';
		datos.p_UsaReglasNegocio = 'S';
					
		eventosCapital.push({
			"p_IdTipoEventoCapital" : objEventoCapital===undefined?$scope.data.oConfiguracionTipoEvento.seleccionado.Codigo:objEventoCapital.IdTipoEventoCapital ,// $scope.data.oConfiguracionTipoEvento.seleccionado.Codigo || '',
			"p_Secuencia" : Number($scope.data.kngSecuencia) || 0,
			"p_Descripcion" : $scope.data.Descripcion || '',
			"p_EsInscrito" : $scope.data.switchInscrito ? 'S' : 'N',
			"p_IdClasificacionTipoInstrumento" : $scope.filtro.instrumentoObjeto.IdClasificacionTipoInstrumento,
			"p_IdTipoInstrumento" : $scope.filtro.instrumentoObjeto.IdTipoInstrumento || 0,
			"p_IdInstrumento" : $scope.filtro.instrumentoObjeto.IdInstrumento || 0,
			"p_Nemo" : $scope.filtro.instrumentoObjeto.Nemotecnico || 0,
			"p_Factor" : Number($scope.data.kngFactor) || 0,
			"p_Impuesto" : Number($scope.data.kngValorImpuesto) || 0,
			"p_IdMoneda" : $scope.data.oConfiguracionMoneda.seleccionado ? $scope.data.oConfiguracionMoneda.seleccionado.IdMoneda||  objEventoCapital.IdMoneda : '',
			"p_IdMonedaLiquidacion" : $scope.data.oConfiguracionMonedaLiquidacion.seleccionado ? $scope.data.oConfiguracionMonedaLiquidacion.seleccionado.IdMoneda|| objEventoCapital.IdMonedaLiquidacion : '',
			"p_Paridad" : $scope.data.kngParidad|| 0,
			"p_IdInstrumentoNuevo" : $scope.filtro.instrumentoNuevoObjeto ? $scope.filtro.instrumentoNuevoObjeto.IdInstrumento || 0 : 0,
			"p_NemoNuevo" : $scope.filtro.instrumentoNuevoObjeto ? $scope.filtro.instrumentoNuevoObjeto.Nemotecnico || 0 : 0,
			"p_FactorNuevo" : Number($scope.data.kngValorFactor) || 0,
			"p_FechaActualizacion" : $scope.SesionFechaSistema,
			"p_FechaLimite" : util.dateSPdate($scope.data.kngFechaLimite) ||'1900-01-01',
			"p_FechaPago" : util.dateSPdate($scope.data.kngFechaPago) ||'1900-01-01',			
			"p_NumeroDividendo" : Number($scope.data.kngNumDividendo) || 0,
			"p_IdTipoDividendo" : '',//preguntar
			"p_PrecioAnterior" : 0, //preguntar
			"p_FechaHoraIngresoRegistro" : '1900-01-01',
			"p_FechaHoraUltimaActualizacion" : '1900-01-01',
			"p_TipoIngreso" : 0,
			"p_IdBoveda" :  $scope.data.oConfiguracionBoveda.seleccionado.IdBoveda || '', 
			"p_IdEstado" : '',
			"p_IdEmisor" : $scope.filtro.instrumentoObjeto.IdEmisor || '',
			"p_IdEmisorNuevo" : $scope.filtro.instrumentoNuevoObjeto ? $scope.filtro.instrumentoNuevoObjeto.IdEmisor || '' : '',
			"p_Monto" : $scope.data.kngMonto,
			"p_Cuotas" : $scope.data.kngCuotas,
			"p_EsISFUT" : $scope.data.switchISFUT ? 'S' : 'N',
			"p_ImpuestoDivExtranjero": $scope.data.kngImpuestoDivExtranjero || 0,
			"p_GastoTransferencia": $scope.data.kngGastoTransferencia || 0,
      "p_TasaEfectiva" : Number($scope.data.kngTasaEfectiva) || 0,
      "p_SecuenciaRelacionada" : $scope.data.oConfiguracionSecuenciaRelacionada?.seleccionado.Secuencia || '',
      "p_SuscripcionComplementaria" : ($scope.data.switchSuscripcionComplementaria) ? 'S' : 'N',
		});
		datos.p_EventoCapital = eventosCapital;
			  
		if (parametrosVentana.EventoCapital){
			datos.p_EventoCapital[0].p_IdEventoCapital = objEventoCapital.IdEventoCapital;
			datos.p_EventoCapital[0].p_IdTipoEventoCapital = "";
			bcsServiciosEventoCapital.ModificarEventoCapital(datos,
			function(respuesta){	
				if (respuesta.P_OUT_ERRORESCONTADOR == "0") {
					var vMsg = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_OPERACION_INGRESO_EXITO"),
						conflictos:util.parseServerResponse(respuesta)},$scope.idVentana);
					bcsComunes.ventanaPromesa(vMsg).then(function() {$scope.data.btnCerrar(true);});
				}
				else{
					if (respuesta.P_OUT_ERRORESCONTADOR > 0){
						bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"),
							conflictos:util.parseServerResponse(respuesta)},$scope.idVentana);
					}
				}	 
			});
		}
		else{
			bcsServiciosEventoCapital.CrearEventoCapital(datos,
			function(respuesta){
				if (respuesta.P_OUT_ERRORESCONTADOR == "0") {
					var vMsg = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_OPERACION_INGRESO_EXITO"),
						conflictos:util.parseServerResponse(respuesta)},$scope.idVentana);
					bcsComunes.ventanaPromesa(vMsg).then(function() {$scope.data.btnCerrar(true);});
				}
				else{
					if (respuesta.P_OUT_ERRORESCONTADOR > 0)
						bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"),
							conflictos:util.parseServerResponse(respuesta)},$scope.idVentana);
				}	 
			}				
		);
		$scope.data.btnActivos  = true;
		$scope.data.accionDisabled = true;
		bcsComunes.ventanaOverlay($scope.idVentana, false);
      }	
	}

	function ConsultarParidad() {
		var datos = {};
		datos.p_IdEmpresa = $scope.SesionIdEmpresa;
		datos.p_IdTrabajo = 0;
		calculosEntrada = [];
		calculosEntrada.push({
			"IdTrabajo": 0,
			"IdInterno": 0,
			"Fecha": $scope.SesionFechaSistema,
			"IdMonedaOrigen": $scope.data.oConfiguracionMoneda.seleccionado.Codigo,
			"IdMonedaDestino": $scope.data.oConfiguracionMonedaLiquidacion.seleccionado.Codigo,
			"ValorCambioLocalModificado": 0,
			"MontoOrigen": 1,
			"IdUso": ''
		})
		datos.p_CalculosEntrada = calculosEntrada;
		bcsServiciosEventoCapital.CalcularParidad(datos,
		function(respuesta){
			if (respuesta.P_OUT_ERRORESCONTADOR == "0" && respuesta.p_out_CalculosSalida.length > 0) {
				$scope.data.kngParidad = respuesta.p_out_CalculosSalida[0].FactorOrigenDestino;
			}
		});
	}

	function ConsultarImpuestoDivExtranjero() {
		if (!$scope.filtro || !$scope.filtro.instrumentoObjeto) return;
		var datos = {};
		datos.p_IdEmpresa = $scope.SesionIdEmpresa; 
		datos.p_IdUsuario = $scope.SesionIdUsuario;
		datos.p_IdModulo = 'CUS';
		emisores = [];
		emisores.push({
			"IdEmisor": $scope.filtro.instrumentoObjeto.IdEmisor
		});
		datos.p_Emisores = emisores;
		bcsServiciosEventoCapital.ConsultarImpuestoDivExtranjero(datos,
		function(respuesta){
			if (respuesta.P_OUT_ERRORESCONTADOR == "0" && respuesta.P_OUT_CONSULTA_IMPUESTO.length > 0){
				$scope.data.AplicaImpuestoDivExtranjero = respuesta.P_OUT_CONSULTA_IMPUESTO[0].AplicaImpuesto;
				$scope.data.PorcentajeImpuestoExtranjero = respuesta.P_OUT_CONSULTA_IMPUESTO[0].PorcentajeImpuestoDefecto;
				$scope.data.AplicaGastoTransferencia = respuesta.P_OUT_CONSULTA_IMPUESTO[0].AplicaGastoTransferencia || 'N';
			}
		});
	}
	
	function InicializarFechasEventoCapital(){
		const accionVentana = parametrosVentana.Accion;
		const fechaSistemaDate = util.date($scope.SesionFechaSistema).toDate();
		if(accionVentana === 'Crear'){
			$scope.data.kngFechaLimite = fechaSistemaDate;
			$scope.data.kngFechaPago = fechaSistemaDate;
			$scope.data.kngLimiteFechaPago = fechaSistemaDate;
			return;
		}
		
		if(accionVentana === 'Modificar' && objEventoCapital) {
			const { FechaLimite, FechaPago } = objEventoCapital;
			const fechaLimiteDate = util.dateInicioDia(FechaLimite);
			const fechaPagoDate = util.dateInicioDia(FechaPago);
	
			$scope.data.kngFechaLimite = fechaLimiteDate;
			$scope.data.kngFechaPago = fechaPagoDate;
			$scope.data.kngLimiteFechaPago = fechaLimiteDate;
			return;
		}
	}

		
 	//Primer evento - filtro inicial
	$timeout(function(){
		$scope.data.oConfiguracionTipoEvento.filtros.read = true;
		if($scope.data.Accion == 'Crear')
			initUI($scope.data.oConfiguracionTipoEvento.seleccionado.Codigo);
		if($scope.data.Accion == 'Modificar')
			initUI(objEventoCapital.IdTipoEventoCapital);
	},1000);
	
});
