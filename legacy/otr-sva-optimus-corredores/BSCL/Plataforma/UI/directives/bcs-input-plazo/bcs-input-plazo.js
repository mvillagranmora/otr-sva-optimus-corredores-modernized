angular.module('ModuloPlataforma').directive('bcsInputPlazo', function($rootScope,bcsServicioPlataforma,bcsComunes,$translate) {
	return {
		restrict: 'E',
		replace: false,
		scope: {
			fechaInicial : '=?',
			fechaVencimiento : '=?',
			plazo : '=?',
			idCondicionDeLiquidacion : '=?',
			idTipoOperacion : '=?',
			vencimientoValido : '=?',
			activaPlazo :'=?',
			activaFecha :'=?',
			bcsRequerido :'=?',
			todoDias : '=?', //defecto false
			diasHabiles : '=?',//defecto N
			idTipoPrestamo : '=?'
			
		},		
		templateUrl: 'directives/bcs-input-plazo/bcs-input-plazo.html',
		controller : function($scope){
			this.getSeleccionado = function(){
				if ($scope.vencimientoValido === false) return false;
				return $scope.plazo;
			}
		},
    	link: function(scope, element, attrs, fn) {
    		
    		if (scope.activaPlazo === undefined) scope.activaPlazo = true;
    		if (scope.activaFecha === undefined) scope.activaFecha = true;
    		if (scope.todoDias 	  === undefined) scope.todoDias    = false;

    		var calculaPlazo = function() {
    			var datos = {
    				p_IdEmpresa : $rootScope.SesionIdEmpresa,
					p_IdUsuario : $rootScope.SesionIdUsuario,
					p_IdModulo : 'PLA',
					p_UsaReglasNegocio : 'S',
					p_IdCondicionDeLiquidacion : scope.idCondicionDeLiquidacion,
					p_FechaDesde : angular.isDate(scope.fechaInicial)? util.dateSPdate(scope.fechaInicial): scope.fechaInicial,
					p_FechaHasta : util.dateSPdate(scope.fechaVencimiento),
					p_DiasHabiles : scope.diasHabiles === undefined ? 'N': scope.diasHabiles,
					p_IdPais :  $rootScope.SesionIdPaisSistema
				};

    			bcsServicioPlataforma.Plazo(datos,function(result)
				{
					if (result.P_OUT_PLAZODIAS != scope.plazo)
						scope.plazo = Number(result.P_OUT_PLAZODIAS);
				}
				,function(result)
				{
					console.log("ERROR: " + JSON.stringify(result));
				});
    		};
			
			var calculaFechaVencimiento = function() {
				var datos = {
					p_IdEmpresa : $rootScope.SesionIdEmpresa,
					p_IdUsuario : $rootScope.SesionIdUsuario,
					p_IdModulo : 'PLA',
					p_UsaReglasNegocio : 'S',
					p_FechaIngreso : angular.isDate(scope.fechaInicial)? util.dateSPdate(scope.fechaInicial): scope.fechaInicial,//$rootScope.SesionFechaSistema,
					p_IdTipoOperacion : scope.idTipoOperacion,
					P_IdCondicionDeLiquidacion : scope.idCondicionDeLiquidacion,
					p_IdPais : $rootScope.SesionIdPaisSistema,
					p_Plazo : scope.plazo || 0,
					p_DiasHabiles : scope.diasHabiles === undefined ? 'N': scope.diasHabiles
				};

				bcsServicioPlataforma.ConsultarFechaVencimiento(datos).then(function(result)
				{
					if (result.P_OUT_ERRORESCONTADOR === 0 && (!scope.fechaVencimiento || result.P_OUT_FECHAVENCIMIENTO !== util.dateSPdate(scope.fechaVencimiento) )  ){
						scope.fechaVencimiento = util.dateDS(result.P_OUT_FECHAVENCIMIENTO,"FechaSistema","CALL")						
					}
					else
					{
						console.log("ERROR: " + JSON.stringify(result));
					}
					//if (result.P_OUT_ESHABIL === "N" && scope.todoDias === false){
						//bcsComunes.msg("error","Error",$translate.instant("UI_FECHA_VENCIMIENTO_INHABIL"));
						//scope.vencimientoValido = false;
					//}
					if (result.P_OUT_ESHABIL === "N"){
						//bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_FECHA_VENCIMIENTO_INHABIL"));
						if(scope.todoDias) {
							scope.vencimientoValido = true;
						} else {
							bcsComunes.msg("error","Error",$translate.instant("UI_FECHA_VENCIMIENTO_INHABIL"));
							scope.vencimientoValido = false;
						}
					} else {
						scope.vencimientoValido = true;
					}
				}
				,function(result)
				{
					console.log("ERROR: " + JSON.stringify(result));
				});	
			};
			
			scope.$watch('fechaInicial',function(newval,oldval){
				//fechainicial debe ser tipo date
    			if (scope.idCondicionDeLiquidacion && scope.fechaInicial && scope.idTipoOperacion && util.dateSPdate(scope.fechaInicial) !== '0001-01-01') {
    				if(newval=== undefined || util.dateSPdate(newval) === '0001-01-01')return;//sin fecha vencimiento
    				if (newval && ( !oldval || newval.toString() !== oldval.toString() )){
    					calculaFechaVencimiento();
						if (scope.opt.min) scope.opt.min = util.dateSPdate(newval);
    				}
    			};
    		});

			scope.$watchGroup(['fechaVencimiento'],function(newval,oldval){
				//fechainicial debe ser tipo date
    			if (scope.idCondicionDeLiquidacion && scope.fechaInicial && scope.idTipoOperacion && util.dateSPdate(scope.fechaInicial) !== '0001-01-01') {
    				if(!newval[0] || util.dateSPdate(newval[0]) === '0001-01-01')return;//sin fecha vencimiento
    				if (newval[0] && ( !oldval[0] || newval[0].toString() !== oldval[0].toString() )){
    					calculaPlazo();    				
					}
    			};
    		});
    		
    		scope.$watchGroup(['plazo','idCondicionDeLiquidacion', 'idTipoPrestamo'],function(newval,oldval){
    			if (scope.idCondicionDeLiquidacion && scope.fechaInicial && scope.idTipoOperacion) {
    				if (newval[0] !== undefined && (newval[0] !== oldval[0] || newval[1] !== oldval[1] || newval[2] !== oldval[2])){
    					calculaFechaVencimiento();
    				}
    			};
    		});

			scope.$watch('todoDias',function(newval,oldval){
				if (newval === oldval) return;
    			scope.opt.month ={								
					content: 	 '# if ('+scope.todoDias+' == false  && util.esFechaFeriado('+$rootScope.SesionIdPaisSistema+',data.date)==true )  { #' +
									'<div class="dia-nohabil dia-feriado">#= data.value #</div>' +
								 '# }else if ('+scope.todoDias+' == false  && util.esFechaHabil('+$rootScope.SesionIdPaisSistema+',data.date)==false )  { #' +
									'<div class="dia-nohabil">#= data.value #</div>' +
								'# } else { #' +
								'#= data.value #' +
								'# } #' 
					};
    		});
    		
    		//GO
			scope.opt = {
				min : $rootScope.SesionFechaSistema,
    		};
    		
    		if (scope.plazo >= 0 && scope.idCondicionDeLiquidacion && scope.fechaInicial && scope.idTipoOperacion)
    		{
    			calculaFechaVencimiento();
    		}
    	}
	}
});
