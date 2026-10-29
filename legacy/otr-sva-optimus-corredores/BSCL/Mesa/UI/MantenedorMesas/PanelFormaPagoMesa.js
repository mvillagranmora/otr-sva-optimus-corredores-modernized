(function() {
	angular.module('ModuloMesa').controller('PanelFormaPagoMesaCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosMesa,bcsServiciosGrillasMesaRF){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var confGridFormaPago= bcsServiciosGrillasMesaRF.getConfiguracionFormaPagoMesa();
		var pFormaPagoMesa		= this;
		pFormaPagoMesa.idVentana= bcsComunes.ventanaWinId($scope);
		pFormaPagoMesa.win		= {};
		pFormaPagoMesa.fn		= {};
		pFormaPagoMesa.data 	= {};
		pFormaPagoMesa.data.confBasicaFormaPagoMesa = confGridFormaPago.options;
		pFormaPagoMesa.data.buscarFormaPagoMesa = confGridFormaPago.buscar;
		pFormaPagoMesa.data.SesionIdEmpresa = $scope.SesionIdEmpresa;
		pFormaPagoMesa.data.HabilitaBotones = true;
		
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		pFormaPagoMesa.data.oConfiguracionTipoProducto={
			tipo : "combobox-tipo-producto-mesa-modulo",
			configuracion : {dataValueField : "IdTipoProductoMesa"},
			filtros: {
				IdEmpresa	: {field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdEstado	: {field: "IdEstado",	operator: "eq", value: 'ACT'}
			},
			idActual: false
		};
		
		pFormaPagoMesa.data.oConfiguracionEstado={
			tipo : "combobox-estado-mesas",
			configuracion : {dataValueField : "IdEstado"},
			filtros: {
				NombreEntidad : { field: "toupper(NombreEntidad)", operator: "eq", value: "MESA" }
			},
			idActual: false
		};
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		pFormaPagoMesa.fn.limpiar = function(){
			pFormaPagoMesa.data.oConfiguracionTipoProducto.idActual = false;
			pFormaPagoMesa.data.oConfiguracionEstado.idActual = false;
			pFormaPagoMesa.data.confBasicaFormaPagoMesa.dataSource.removeAll();
		};
		pFormaPagoMesa.fn.modificarFormaPago = function (accion){
			var id1 = bcsComunes.ventanaAbrir("ModalMantenedorFormaPagoMesa", 
						{
							Accion: accion
							,IdTipoProducto : pFormaPagoMesa.data.oConfiguracionTipoProducto.idActual
							,dataGrilla : pFormaPagoMesa.data.seleccionFormaPagoMesa
						},false,false);//,$scope.winID);
			var promesa=bcsComunes.ventanaPromesa(id1);
			pFormaPagoMesa.data.HabilitaBotones = false;
			$.when(promesa).then(function(data){
				pFormaPagoMesa.data.HabilitaBotones = true;
				pFormaPagoMesa.data.buscarFormaPagoMesa($scope.pFormaPagoMesa.data.gridApiFormaPagoMesa,$scope.pFormaPagoMesa.data);
			});
		};
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		
	});
}());