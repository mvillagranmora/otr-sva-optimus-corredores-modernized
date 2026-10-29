angular.module('ModuloMesa').controller('ConsultarReasignacionOperacionesMesaRFCtrl',
		function($scope,$timeout,$modal,$state,bcsComunes,$translate, bcsServiciosMesa,$http,bcsServiciosGrillasMesaRF,bcsAlerta,OPCIONES_BCSALERTA)
{	
	var conReasignacion = this;
	$scope.show = false;
	$scope.dataReasignacion = {};
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	$scope.dataReasignacion.confBasicaOperacionesMesaRF = {};
	
	// --------------------------------------------------
	// CONFIGURACION DE GRILLAS
	// --------------------------------------------------
	var promise = Promise.resolve(bcsServiciosGrillasMesaRF.getConfiguracionOperacionesMesaRF_Reasignacion());
	promise.then(function(confGridOperaciones){
		
		$scope.dataReasignacion.confBasicaOperacionesMesaRF = confGridOperaciones.options;
		
		console.log(confGridOperaciones.options.dataSource);
		
		$scope.dataReasignacion.buscarOperacionesMesaRF = confGridOperaciones.buscar;
		$scope.show = true;
	});
	
	
	var fechaVencimientos = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
	var dataSourceConsultarEstadoMesa = bcsServiciosGrillasMesaRF.getDataSourceConsultarEstadoMesa();
	$scope.NombreArchivo = "ConsultaReasignacionOperacionesMesaRF_"+$scope.SesionFechaSistema;
	$scope.dataReasignacion.SesionIdEmpresa = $scope.SesionIdEmpresa;
	
	// --------------------------------------------------
	// CONFIGURACION DE COMBOS
	// --------------------------------------------------
	$scope.dataReasignacion.oConfiguracionMesas = {
			tipo : "combobox-mesas",
			configuracion : {},
			idActual : false,
			seleccionado : false,
			filtros : { IdEstado : { "field"   : "IdEstadoRegistro", "operator": "eq", "value" : "ACT" },
					    IdTipoProducto : { "field"   : "IdTipoProducto", "operator": "eq", "value" : "MERF" }
					},
	};
	
	$scope.$watch('dataReasignacion.oConfiguracionMesas.seleccionado', function(newval,oldval ){
		if ($scope.dataReasignacion.oConfiguracionMesas.seleccionado) {
			$scope.blotterOpMesa.IdMesa = $scope.dataReasignacion.oConfiguracionMesas.seleccionado.IdMesa;
			if ($scope.dataReasignacion.gridApiOperacionesMesaRF != undefined) $scope.blotterOpMesa.VENBUSCA();
		}
		
	});
	$scope.$watch('blotterOpMesa.IdMesa', function(newval,oldval ){
		if ($scope.blotterOpMesa.IdMesa) {
			$scope.dataReasignacion.oConfiguracionMesas.idActual = $scope.blotterOpMesa.IdMesa;
		}
		
	});
	
	$scope.dataReasignacion.kngFechaOperacion = fechaVencimientos;
	$scope.dataReasignacion.kngFechaOperacionFin = fechaVencimientos;
	
	$scope.dataReasignacion.Modo = 'VEN';
	$scope.blotterOpMesa.VENBUSCA = function(){
		$scope.dataReasignacion.buscarOperacionesMesaRF($scope.dataReasignacion.gridApiOperacionesMesaRF,$scope.dataReasignacion);
	};
	$scope.dataReasignacion.cargaGrillas = function(){
		$scope.dataReasignacion.buscarOperacionesMesaRF($scope.dataReasignacion.gridApiOperacionesMesaRF,$scope.dataReasignacion);
		$scope.blotterOpMesa.OPEBUSCA();
		$scope.blotterOpMesa.ANTBUSCA();
	};
	$scope.dataReasignacion.detalleOperacion = function(datos){
		bcsComunes.ventanaAbrir("DetalleOperacionMesaRF", {
					IdOperacion: $scope.dataReasignacion.seleccionOperacionesMesaRF[0].IdOperacion,
					IdTipoProducto : $scope.dataReasignacion.seleccionOperacionesMesaRF[0].IdTipoProducto,
				},false);
	};
	
	var success = function(respuesta)
	{
		var operaciones = [];
		var id;
		//si vuelve ref id, se creo la asignacion
		if (respuesta.P_OUT_ERRORESCONTADOR === 0) 
		{		
			$.each(respuesta.P_OUT_OPERACIONES,function(key,value) {
				operaciones.push({id: value.IdOperacion , resultado: $translate.instant('UI_EXITO'), mensaje: $translate.instant('UI_OPERACION_REVERSA_ANTICIPO_EXITO')});
			});
	
			id = bcsComunes.ventanaAbrir("ResumenOperacionesMasivas", { 
				tituloVentana : $translate.instant("UI_MESA_RENTA_FIJA"), 
				nombreEntidad : $translate.instant("UI_SECUENCIA"), 
				operaciones : operaciones   
			},false,false,$scope.winID);
			
		}
		else
		{			
			id = bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);
		}
		
		bcsComunes.ventanaPromesa(id).then(function(result){
			bcsComunes.ventanaOverlay($scope.winID, false);
			$scope.dataReasignacion.cargaGrillas();
		});
		
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
});