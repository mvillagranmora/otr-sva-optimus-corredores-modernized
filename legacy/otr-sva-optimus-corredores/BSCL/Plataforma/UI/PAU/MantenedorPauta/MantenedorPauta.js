/******************************************
 * Purpose	: Mantenedor de agenda job
 * Author	: JG
 * Date		: 30/11/2016
 *****************************************/
(function() {
	angular.module('ModuloPlataforma').controller('MantenedorPautaCtrl',
	function($scope,bcsComunes,$translate,$resource,bcsConfiguracionPauta, bcsServicioPlataforma,$http){
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var idVentana 	= bcsComunes.ventanaWinId($scope); 
		var parametros 	= bcsComunes.ventanaParams(idVentana);
		var mpauta 		= this;
		mpauta.conf 	= {};
		mpauta.datos	= {};
		mpauta.cmbbx 	= {};
		mpauta.win 		= {}; 
		mpauta.grid 	= {};
		
		var confControl = bcsConfiguracionPauta.getConsultaLogPauta();
		var dsLogs = confControl.options.dataSource;
		mpauta.grid.pauta = confControl.options;
		
		$scope.mpauta.win.buscar = function(){
			dsLogs.setCallParameters($scope.preparaParametros());
			
		}
		
		$scope.mpauta.win.descargarLog = function(elemento){
			
			var datos = {file:elemento.RutaLog}
			bcsComunes.ventanaOverlay(idVentana,true);
			var url = "/BSCL/Plataforma/GWJAVA/PLA/download?";
			
			$http({

				method: 'POST',
				url: url,
				params: datos
				
			}).success(function(datos){
					var file = new File([datos],elemento.RutaLog.split('/')[elemento.RutaLog.split('/').length - 1]);
					saveAs(file);
					bcsComunes.msg("info",$translate.instant("UI_DESCARGA_LOG"),$translate.instant("UI_DESCARGA_LOG_EXITOSA"));
					bcsComunes.ventanaOverlay(idVentana,false);
					
			})
			.error(function (err){
					bcsComunes.msg("error",$translate.instant("UI_DESCARGA_LOG"),$translate.instant('UI_DESCARGA_LOG_ERROR'));
					bcsComunes.ventanaOverlay(idVentana,false);
			});
			
		}
		
		$scope.preparaParametros = function(){
			parametrosConsulta = {};
			parametrosConsulta.p_IdEmpresa = $scope.SesionIdEmpresa;
			parametrosConsulta.p_IdUsuario =$scope.SesionIdUsuario;
			parametrosConsulta.p_IdModulo = "PLA";
			parametrosConsulta.p_FechaEjecucion = util.dateSPdate($scope.mpauta.datos.Fecha);
			return parametrosConsulta;
		};
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		mpauta.cmbbx.tipoPrograma ={
			idActual : false,
			seleccionado : false
		};
		
		mpauta.win.limpiar = function (){
			$scope.mpauta.datos.Fecha = util.dateInicioDia($scope.SesionFechaSistema);
			//mpauta.grid.pauta.dataSource.filter([{field: "IdTipoPrograma", operator: "eq", value: ''}]);
			mpauta.datos.seleccionado = undefined;
		};
		
		mpauta.win.ejecutarManual = function(sel){
			if(!sel || sel.length == 0){
				bcsComunes.msg("warning",'Ejecutar Manual','Seleccione registro');
				return;
			}
			bcsComunes.ventanaOverlay(idVentana,true);
			var r = $resource("",{},{'ejecutarProgramaManual':{method:"POST",url:'/BSCL/Plataforma/GWJAVA/PLA/call?t=20043'}});
			
			var jobsString = "";
			$.each(sel,function(key,value){
				jobsString+=value.CodigoProceso+"|"
			})
			
			var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo  : 'PLA',
				p_TipoPrograma : [{
					IdInterno : 0,
					IdTipoPrograma : 'PAUTA_BASE',
					SegundosDesfase : 0
				}],
				p_Parametro : [{
					IdInterno : 0,
					NombreParametro : 'IdEmpresa',
					Valor : $scope.$root.SesionIdEmpresa
				},{
					IdInterno : 0,
					NombreParametro : 'IdUsuario',
					Valor : $scope.SesionIdUsuario
				},{
					IdInterno : 0,
					NombreParametro : 'FechaSistemaPauta',
					Valor : sel[0].FechaSistema
				},{
					IdInterno : 0,
					NombreParametro : 'Jobs',
					Valor : jobsString
				}]
			};
			
			var post = r.ejecutarProgramaManual(datos);
			post.$promise.then(function(result){
				if(result.P_OUT_ERRORESCONTADOR==0){
					var id = bcsComunes.ventanaMsg({
						mensaje:$translate.instant('Programa agendado OK'),
						conflictos:util.parseServerResponse(result)
					},$scope.winID);
					var promesa=bcsComunes.ventanaPromesa(id); 
		     		$.when(promesa).then(function(data){
		     			bcsComunes.ventanaOverlay(idVentana,false);
		     		});
				}else{
					bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.ventanaMsgErr($translate.instant('UI_ERROR_EJECUTAR_PROGRAMA_MANUAL. '+result.P_OUT_MENSAJE),idVentana);
				}
			},function(result){
				bcsComunes.ventanaOverlay(idVentana,false);
				bcsComunes.ventanaMsgErr($translate.instant('UI_ERROR_EJECUTAR_PROGRAMA_MANUAL'),idVentana);
				console.error(JSON.stringify(result));
			});
			mpauta.datos.seleccionado = undefined;
		};
		
		mpauta.win.pautaDetalle = function(registro){
			if(registro && registro[0].IdPrograma>0){
				var id1 = bcsComunes.ventanaAbrir("MantenedorPautaDetalle",{ IdPrograma: registro[0].IdPrograma},false, false, idVentana); 
	            var promesa=bcsComunes.ventanaPromesa(id1);                               
	            $.when(promesa).then(function(data){ 
	            	mpauta.win.buscar();
	             }); 
			}
		};
		
		var procesosConLogValorizadorPauta = {
			"VALORIZAR_CARTERA_RF": true,
			"VALORIZAR_CARTERA_IF": true,
			"VALORIZAR_GARANTIAS_RV_RF_IF": true
		};
		
		$scope.mpauta.win.tieneLogValorizadorPauta = function(registro) {
			if (!registro) {
				return false;
			}
		
			if (angular.isArray(registro)) {
				if (registro.length === 0) {
					return false;
				}
		
				registro = registro[0];
			}
		
			if (!registro.CodigoProceso) {
				return false;
			}
		
			var codigoProceso = registro.CodigoProceso.toString().trim().toUpperCase();
		
			return procesosConLogValorizadorPauta[codigoProceso] === true;
		};
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
	 	$scope.mpauta.win.ConsultarLogValorizadorPauta = function(registro) {	
			var datos = {
					p_IdEmpresa 		: $scope.SesionIdEmpresa,
					p_IdUsuario 		: $scope.SesionIdUsuario,
					p_IdModulo       	: 'PLA',
					p_IdEjecucion		: registro.IdEjecucion,
					p_CodigoProceso		: registro.CodigoProceso					
			};
			bcsComunes.ventanaOverlay($scope.winID,true);
			bcsServicioPlataforma.Servicios().ConsultarLogValorizadorPauta(datos).$promise.then(function(result){				
				if (result.P_OUT_ERRORESCONTADOR === 0) {
					result.RutaLog = result.P_OUT_RUTA_LOG;
					$scope.mpauta.win.descargarLog(result)
				} else {
					bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.msg("error",$translate.instant('UI_ERROR_CONSULTAR_LOG_VALORIZADOR'), result.P_OUT_MENSAJE);
				}
			},function(result){
				bcsComunes.ventanaOverlay($scope.winID,false);
				bcsComunes.msg("error","UI_ERROR","UI_ERROR_CONSULTAR_LOG_VALORIZADOR");
			});
		}
		/*********************************************
		 * GO!!!
		 *********************************************/
		mpauta.win.limpiar();
	});
}());
//------------------------- FIN CONTROLLER --------------------------
