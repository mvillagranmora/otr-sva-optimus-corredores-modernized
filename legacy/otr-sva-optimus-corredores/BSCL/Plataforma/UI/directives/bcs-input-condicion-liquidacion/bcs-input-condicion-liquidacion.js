angular.module('ModuloPlataforma').directive('bcsInputCondicionLiquidacion', function($rootScope,bcsServicioPlataforma,bcsComunes,$translate) {
	return {
		restrict: 'E',
		replace: false,
		scope: {
			//IN
			fechaInicial : '=?',
			idTipoOperacion : '=?',
			activado : '=?',
			setIdCondicionLiquidacion : '=?',
			filtros : '=?',
			incluyeot : '=?',
			
			//OUT
			idCondicionDeLiquidacion : '=?',
			condicionDeLiquidacion : '=?',
			fechaLiquidacion : '=?'

		},		
		templateUrl: 'directives/bcs-input-condicion-liquidacion/bcs-input-condicion-liquidacion.html',
		controller : function($scope){
			this.getSeleccionado = function(){
				if (!$scope.condicionDeLiquidacion) return false;
				return $scope.condicionDeLiquidacion.IdCondicionDeLiquidacion;
			}
		},
    	link: function(scope, element, attrs, fn) {
    		
			var IdTipoOperacion 							= { "field"   : "IdTipoOperacion","operator": "eq", "value"   : ""};
			var EstadoTipoOperacionYCondicionDeLiquidacion	= { "field"   : "EstadoTipoOperacionYCondicionDeLiquidacion","operator": "eq", "value"   : "ACT" };
			scope.configFecha = {};
			scope.configFecha.min = $rootScope.SesionFechaSistema;

			var setFiltros = function() {
    			
    			if (scope.filtros && scope.filtros.length > 0) {
    				
    				var FiltrosLiquidacion 							= [
    					
    				];
    				
    				$.each(scope.filtros,function(i,item){
    					FiltrosLiquidacion.push({ "field"   : "IdCondicionDeLiquidacion","operator": "eq", "value"   : item});
    				})

    				scope.confCombo.filtros = {
						complejo : [IdTipoOperacion,EstadoTipoOperacionYCondicionDeLiquidacion],
						logic : "and",
						filters : [{
							logic : "or",
							filters : FiltrosLiquidacion,
						},
						IdTipoOperacion,
						EstadoTipoOperacionYCondicionDeLiquidacion
						]};
    				
    				
    			}else {
    				scope.confCombo.filtros = {
    					IdTipoOperacion : IdTipoOperacion,
						EstadoTipoOperacionYCondicionDeLiquidacion : EstadoTipoOperacionYCondicionDeLiquidacion
    				};
    			}
    			
    		}
			
    		scope.confCombo = {
    				tipo : "combobox-condicion-liquidacion",
					configuracion : {
						dataTextField: "Codigo",
						placeholder: "",
						template: '<span >#: Codigo #</span>'
					}
    		};
    		
    		setFiltros();
    		
    		scope.$watch('activado',function(newval,oldval){
    			scope.confCombo.activado = scope.activado;
    		});
			scope.$watch('incluyeot',function(newval,oldval){
    			scope.confCombo.incluyeot = scope.incluyeot;
    		});
    		scope.$watch('idTipoOperacion',function(newval,oldval){
    			if (newval)
    				IdTipoOperacion.value = scope.idTipoOperacion;    			
    		});
    		
    		scope.$watch('filtros',function(newval,oldval){
    			setFiltros();
    				    			
    		});
    		
    		scope.$watch('setIdCondicionLiquidacion',function(newval,oldval){
    			if (!scope.setIdCondicionLiquidacion) return;
    			scope.confCombo.idActual = scope.setIdCondicionLiquidacion;
    			scope.confCombo.idDefecto = scope.setIdCondicionLiquidacion;
    			scope.setIdCondicionLiquidacion = undefined;
    			
    		});
    		scope.$watchGroup(['confCombo.seleccionado','fechaInicial','idTipoOperacion'],function(newval,oldval){
    			scope.condicionDeLiquidacion = scope.confCombo.seleccionado;

				if(scope.condicionDeLiquidacion && scope.condicionDeLiquidacion.Codigo === "OT"){
					scope.bloqueaSeleccionaFecha = false;
				}else {
					scope.bloqueaSeleccionaFecha = true;
				}

				if (scope.confCombo.seleccionado && scope.confCombo.seleccionado.IdCondicionDeLiquidacion && scope.fechaInicial && scope.idTipoOperacion){
					var datos = {};
					datos.p_IdEmpresa = $rootScope.SesionIdEmpresa;
					datos.p_IdUsuario = $rootScope.SesionIdUsuario; 
					datos.p_IdModulo = 'PLA'; 
					datos.p_UsaReglasNegocio = 'S';
					datos.p_FechaIngreso = angular.isDate(scope.fechaInicial)?util.dateSPdate(scope.fechaInicial):scope.fechaInicial;//util.dateSPdate(scope.fechaInicial);
			        datos.p_IdTipoOperacion = scope.idTipoOperacion || null;
			        datos.P_IdCondicionDeLiquidacion = scope.confCombo.seleccionado.IdCondicionDeLiquidacion;
			        datos.p_IdPais = Number($rootScope.SesionIdPaisSistema);
				 	
			        
					bcsServicioPlataforma.FechaLiquidacion(datos,function(result) {
						
						if (result.P_OUT_ERRORESCONTADOR === 0){
							scope.fechaLiquidacion = util.dateDS(result.P_OUT_FECHALIQUIDACION,"FechaSistema","CALL");
						}
						else {
							scope.fechaLiquidacion = util.dateDS("0001-01-01","FechaSistema","CALL");
							console.log("Error." + result.P_OUT_ERRORESCONTADOR);
						}
					},function(result) {
						
						console.error(result);
					});
					scope.idCondicionDeLiquidacion = scope.confCombo.seleccionado.IdCondicionDeLiquidacion;
				} else {
					scope.idCondicionDeLiquidacion = '';
				}
			});
    		
    	}
	}
});
