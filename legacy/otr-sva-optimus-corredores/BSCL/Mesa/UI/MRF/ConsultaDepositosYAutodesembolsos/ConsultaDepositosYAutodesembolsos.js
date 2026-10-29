angular.module('ModuloMesa').controller('depositosYAutodesembolsosCtrl',function($scope,$timeout,$modal,$state,bcsComunes,$translate,$rootScope,bcsAlerta,OPCIONES_BCSALERTA,
		bcsServiciosMesa, bcsServicioPlataforma){
	var dya = this;
	var idVentana = bcsComunes.ventanaWinId($scope);
	var datosGuardar = {};
	var datosPorGuardar = false;
	var parametrosVentana = bcsComunes.ventanaParams(idVentana);
	
	$scope.data		= {};
	dya.options 	= {};
	dya.filtro		= {};
	dya.fn			= {};
	dya.filtro.FechaDesde = util.dateInicioDia($scope.SesionFechaSistema);
	dya.filtro.FechaHasta = util.dateInicioDia($scope.SesionFechaSistema);
	dya.filtro.ValorParametroDef = 'CLIENTE';
	var fechaMinima = util.dateInicioDia($scope.SesionFechaSistema);
	dya.dataTemplate	= {};
	dya.parametros		= {};
	dya.options.oConfiguracionFechaDesde = {
			max: fechaMinima
			//min: dya.filtro.Fecha
	};
	dya.options.oConfiguracionFechaHasta = {
			min: dya.filtro.FechaDesde,
			max: fechaMinima//fechaSistema
	};
			
	$scope.data.oConfiguracionTipoOperacion={
			tipo : "combobox-tipo-operacion",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdTipoProducto : {"field": "IdTipoProducto","operator": "eq", "value"   :  'MERF' },
				EsAnticipo : {"field": "EsAnticipo","operator": "eq", "value"   :  'N' },
				EsAnulacion : {"field": "EsAnulacion","operator": "eq", "value"   :  'N' },
				Accion : {"field": "Accion","operator": "eq", "value"   :  '' }
			},
			idDefecto : ''
	};
	
	$scope.$watch('dya.filtro.FechaDesde',function(newval,oldval){
		
		dya.options.oConfiguracionFechaHasta = {
				//max: dya.filtro.FechaHasta
				min: dya.filtro.FechaDesde,
				max: fechaMinima//fechaSistema
		};
		if(dya.filtro.FechaDesde > dya.filtro.FechaHasta){
			//dya.filtro.Fecha = dya.filtro.FechaHasta
			dya.filtro.FechaHasta = dya.filtro.FechaDesde
		}
	});
	
	
	dya.generar = function(){

		if (parametrosVentana.data != undefined)
		{			
			var datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo  : 'MES',
					p_TipoPrograma : [{
						IdInterno : 0,
						IdTipoPrograma : 'GENERAR_DEPOSITOS_Y_AUTODESEMBOLSOS',
						SegundosDesfase : 0
					}],
					p_Parametro : [{
						IdInterno : 0,
						NombreParametro : 'IdEmpresa',
						Valor :  $scope.SesionIdEmpresa
					},{
						IdInterno : 0,
						NombreParametro : 'IdUsuario',
						Valor : $scope.SesionIdUsuario
					},{
						IdInterno : 0,
						NombreParametro : 'IdInstrumento',
						Valor : $scope.data.oConfiguracionTipoOperacion.seleccionado.IdInstrumento
					},{
						IdInterno : 0,
						NombreParametro : 'IdTipoInstrumento',
						Valor : $scope.data.oConfiguracionTipoOperacion.seleccionado.IdTipoInstrumento
					},{
						IdInterno : 0,
						NombreParametro : 'IdCliente',
						Valor : parametrosVentana.data.oConfiguracionMesaActiva.seleccionado.IdCliente != undefined ? Number(parametrosVentana.data.oConfiguracionMesaActiva.seleccionado.IdCliente) : null
					},{
						IdInterno : 0,
						NombreParametro : 'FechaDesde',
						Valor : util.dateSPdate(dya.filtro.FechaDesde)
					},{
						IdInterno : 0,
						NombreParametro : 'FechaHasta',
						Valor : util.dateSPdate(dya.filtro.FechaHasta)
					}]
			}
			bcsComunes.ventanaOverlay(idVentana,true);
			bcsServicioPlataforma.Servicios().InsertarProcesoBatch(datos).$promise.then( function(result) {
				bcsComunes.ventanaOverlay($scope.winID,false);
				if(result.P_OUT_ERRORESCONTADOR === 0){
					var fechaLocal=util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion,"FechaHora","CALL");
					bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO').replace("::IdPrograma",result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion",util.formatoSecondate(fechaLocal)),
						conflictos : util.parseServerResponse(result)}, $scope.winID);
				}else{
					bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
				}
				bcsComunes.ventanaOverlay(idVentana,false);
			},
			function(result){
				bcsComunes.ventanaOverlay(idVentana,false);
				bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
			});
		}
	}
	
	
	
	
});