angular.module('ModuloNormativa').controller('ConsultarLibroOperacionesCtrl',
		function($scope, $timeout, bcsComunes, bcsAlerta, OPCIONES_BCSALERTA, $translate, bcsBlotterLibroOperaciones){
	
	
	
	$scope.filtros = {};
	$scope.data = {};
	
	
	$scope.filtros.FecInicio 	 = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.filtros.FecTermino	 = util.dateInicioDia($scope.SesionFechaSistema);
	
	$scope.data.confBasicaLibroOperaciones = {
			dataSource 			: bcsBlotterLibroOperaciones.dataSourceLibroOperaciones,
			columns 			: bcsBlotterLibroOperaciones.columnasLibroOperaciones(),
			sortable			: true,
			pageable			: true,
			userSettings 		: true,
			autoBind			: false,
			filterable			: true,
			height 				: 500,
			selectable 			: "multiple row",
			excel 				: { allPages : true },
			rowColorBackground	: function(row) {		
				switch(row.get("AccionOperacion")) {
		             case 'VENTA':
		                return "red";
		                break;
		             case 'RESCATE':
		                return "red";
		                break;		            
		             case 'COMPRA':
		                return "green";
		                break;
		             case 'APORTE':
			            return "green";
			            break;     
		            default:
		            	return "white";
		                break;     
				  }	
				}
			
			
		};
	
	$scope.data.oConfiguracionCliente={
			tipo : "combobox-socio-negocio",
			configuracion :{},
			filtros: {
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},			
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};
	
	$scope.data.oConfiguracionBolsa={
			tipo : "combobox-bolsa",
			configuracion :{},
			filtros: {
				//IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				//EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},			
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};
	
	$scope.data.oConfiguracionMoneda={
			tipo : "combobox-moneda",
			configuracion : {},
			filtros : { 	
				
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};
	
	$scope.data.oConfiguracionClasificacionTipoInstrumento={
			tipo : "combobox-clasificacion-tipo-instrumento",
			configuracion : {},
			idActual : false,
			seleccionado : false,
			idDefecto : '',
			idPostFiltro : false
		};	
	
   	$scope.data.oConfiguracionTipoInstrumento={
   			tipo : "combobox-tipo-instrumento",
   			configuracion : {},
   				filtros : {
   					IdClasificacionTipoInstrumento 	: { "field" : "IdClasificacionTipoInstrumento", "operator": "eq", "value" : "" },
   					IdEstado 						: { "field" : "IdEstado",                       "operator": "eq", "value" : "VIG" }
   				},
   				idActual : false,
   				seleccionado : false,
   				idDefecto : '',
   				idPostFiltro : false
   		};
   	
	$scope.data.oConfiguracionTipoOperacion={
			tipo : "combobox-tipo-operacion", // "combobox-operacion",
			configuracion : {},
			filtros : {
			
				IdClasificacionTipoInstrumento  	:	{"field"   : "IdClasificacionTipoInstrumento","operator": "eq","value"   : ""},			
				IdTipoInstrumento 					:	{"field"   : "IdTipoInstrumento","operator": "eq","value"   : ""},
				EstadoTipoOperacionTipoInstrumento 	: {"field" : "EstadoTipoOperacionTipoInstrumento","operator":"eq","value" : "ACT"}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
   	$scope.oConfiguracionInstrumento={
			buscar          : "-1"    
	};	
	
	$scope.buscarLibroOperaciones = function(){
		if ($scope.filtros.FecInicio == undefined || $scope.filtros.FecInicio == undefined){return;};
		
		var handler = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_FechaDesde : util.dateSPdate($scope.filtros.FecInicio),
			p_FechaHasta : util.dateSPdate($scope.filtros.FecTermino),

			p_IdCliente : ($scope.data.oConfiguracionCliente.seleccionado !== false && $scope.data.oConfiguracionCliente.seleccionado !== undefined) ? $scope.data.oConfiguracionCliente.seleccionado.IdCliente : null,
			p_IdMoneda : ($scope.filtros.monedaObj.IdMoneda != 0) ? $scope.filtros.monedaObj.IdMoneda : null,
			p_IdBolsa : ($scope.filtros.bolsaObj.IdBolsa != 0) ? $scope.filtros.bolsaObj.IdBolsa : null,
			p_IdClasificacion : ($scope.filtros.clasificacionTipoInstrumentoObj.IdClasificacionTipoInstrumento != 0) ? $scope.filtros.clasificacionTipoInstrumentoObj.IdClasificacionTipoInstrumento : null,
			p_IdTipoInstrumento : ($scope.filtros.TipoInstrumentoObj.IdTipoInstrumento != 0) ? $scope.filtros.TipoInstrumentoObj.IdTipoInstrumento : null,
			p_IdInstrumento : ($scope.filtros.instrumento	!== undefined && $scope.filtros.instrumento.IdInstrumento > 0) ? Number($scope.filtros.instrumento.IdInstrumento) : null,
			p_IdTipOperacion : ($scope.filtros.TipoOperacionObj.IdTipoOperacion != 0) ? $scope.filtros.TipoOperacionObj.IdTipoOperacion : null,
			
			p_MostrarPactos:$scope.filtros.TipoOperacionObj.NombreTipoProducto == "MESA PACTOS" ||
							$scope.filtros.TipoOperacionObj.NombreTipoProducto == undefined  ? 'S': 'N',
			p_MostrarEVC: 'S'
		
		}

		bcsBlotterLibroOperaciones.dataSourceLibroOperaciones.setCallParameters(handler);
		bcsBlotterLibroOperaciones.dataSourceLibroOperaciones.read();
	};
	
});