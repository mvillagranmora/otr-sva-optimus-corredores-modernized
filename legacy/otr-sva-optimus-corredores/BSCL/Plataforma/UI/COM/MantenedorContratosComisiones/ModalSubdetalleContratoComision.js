angular.module('ModuloPlataforma').controller('ModalSubdetalleContratoComisionCtrl',function ($scope, bcsComunes) {

	const subdetalleContratoComision 				= this;
	const idVentana 								= bcsComunes.ventanaWinId($scope);
	subdetalleContratoComision.parametros 			= bcsComunes.ventanaParams(idVentana);
	subdetalleContratoComision.domVentana			= $("[ventanaid='"+idVentana+"']");
	subdetalleContratoComision.dataOriginal			= angular.copy(subdetalleContratoComision.parametros);
	subdetalleContratoComision.data 				= {};
	subdetalleContratoComision.porcentajeOptions 	= {min:0,max:100};
	subdetalleContratoComision.montoMaximoStr       = null;
    subdetalleContratoComision.montoMaximoTexto     = '';
    subdetalleContratoComision.placeholderMonto     = '0';

	subdetalleContratoComision.formatearMonto = function (val) {
        if (!val) return '';
   		var limpio = String(val).replace(/[^0-9]/g, '');
    	return limpio.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
    };

    subdetalleContratoComision.onMontoMaximoChange = function () {
		var val = subdetalleContratoComision.montoMaximoTexto || '';

		var soloValido = val.replace(/[^0-9]/g, '');

		if (soloValido.length > 19) soloValido = soloValido.slice(0, 19);

		var formateado = subdetalleContratoComision.formatearMonto(soloValido);

		if (formateado !== val) {
			subdetalleContratoComision.montoMaximoTexto = formateado;
		}

		subdetalleContratoComision.montoMaximoStr = soloValido || null;
	};
					
	subdetalleContratoComision.cargaDatosPantalla = function(){
		subdetalleContratoComision.data.IdSubdetalleContratoComision	= subdetalleContratoComision.dataOriginal.IdSubdetalleContratoComision || null;
		subdetalleContratoComision.data.Monto 							= subdetalleContratoComision.dataOriginal.Monto || null;
		subdetalleContratoComision.data.Porcentaje 						= subdetalleContratoComision.dataOriginal.Porcentaje || null;

		var monto = subdetalleContratoComision.dataOriginal.MontoMaximo || null;
        subdetalleContratoComision.montoMaximoStr   = monto !== null ? String(monto) : null;
        subdetalleContratoComision.montoMaximoTexto = monto !== null ? subdetalleContratoComision.formatearMonto(String(monto)) : '';		
	};	

	$scope.$watch(
        function () { return subdetalleContratoComision.montoMaximoTexto; },
        function (newVal, oldVal) {
            if (newVal === oldVal) return;
            subdetalleContratoComision.onMontoMaximoChange();
        }
    );
	
	subdetalleContratoComision.cancelar = function () {
		bcsComunes.ventanaData(idVentana, false);
		bcsComunes.ventanaCerrar(idVentana);
	};	

	subdetalleContratoComision.aceptar = function () {
		var datos = angular.copy(subdetalleContratoComision.data);
        datos.MontoMaximo = subdetalleContratoComision.montoMaximoStr ? Number(subdetalleContratoComision.montoMaximoStr) : 0;
		bcsComunes.ventanaData(idVentana, datos);
		bcsComunes.ventanaCerrar(idVentana);	
	};

	subdetalleContratoComision.puedeIngresarMonto = function() {
		return subdetalleContratoComision.dataOriginal.IdCobro == 'COFI' || subdetalleContratoComision.dataOriginal.IdCobro == 'DEFI';
	}
	
	subdetalleContratoComision.puedeIngresarPorcentaje = function() {
		return subdetalleContratoComision.dataOriginal.IdCobro == 'COVA' || subdetalleContratoComision.dataOriginal.IdCobro == 'DEVA';
	}
	
	subdetalleContratoComision.cargaDatosPantalla();
			
});