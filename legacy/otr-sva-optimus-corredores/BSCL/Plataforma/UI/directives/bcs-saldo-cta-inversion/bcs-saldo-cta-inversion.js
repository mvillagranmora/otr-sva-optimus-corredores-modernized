angular.module('ModuloPlataforma').directive('bcsSaldoCtaInversion', function($rootScope,$resource,bcsComunes) {
	return {
		restrict: 'E',
		replace: false,
		scope: {
			idMoneda : '=?',
			idPortafolio : '=?',
			fechaSaldo : '=?',
			uso : '=?',
			valoresSaldo  : '=?',
		},		
		templateUrl: 'directives/bcs-saldo-cta-inversion/bcs-saldo-cta-inversion.html',
    	link: function(scope, element, attrs, fn) {
    		scope.objvaloresSaldo = {};
			scope.$watchGroup(['idMoneda','idPortafolio','fechaSaldo'],function(newval,oldval){
    			
    			if (!scope.idMoneda || !scope.idPortafolio || !scope.fechaSaldo) {
    				scope.saldoCuentaInversion = undefined;
    				scope.objvaloresSaldo.saldoCuentaInversion=scope.saldoCuentaInversion;
    				return;
    			}
    			
    			var datos = {
					p_IdEmpresa: $rootScope.SesionIdEmpresa,
					p_IdUsuario: $rootScope.SesionIdUsuario,
					p_UsaReglasNegocio : 'N',
					p_Uso : scope.uso,
					p_IdModulo: 'CTA',
				
					p_PortafolioAConsultar : [{
						"Identificador" : 0,
						"IdPortafolio" 	: scope.idPortafolio,
						"IdMoneda"		: scope.idMoneda
					}],
	
					p_FechaDesde : util.dateSPdate(scope.fechaSaldo,"FechaSistema","CALL"),
					p_FechaHasta : util.dateSPdate(scope.fechaSaldo,"FechaSistema","CALL")
				};
				
				var urlConsultaCtainv = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1673";
				var r = $resource("", {},
					{
						'ConsultaCtainv': { method: "POST", url: urlConsultaCtainv}
					});
				var p =  r.ConsultaCtainv(datos).$promise;
				p.then(function(result) {
					scope.idMonedaSaldo = scope.idMoneda;
					scope.decimales = util.bcs_formato(0,'GEN','Monto',scope.idMoneda,'','').cantidadDecimales;
					if (result.P_OUT_FLUJOCAJA["0"] && result.P_OUT_FLUJOCAJA["0"].IdPortafolio)
						{
							$.each(result.P_OUT_FLUJOCAJA,function(key,value) {
								if (value.IdMoneda === scope.idMoneda)
									scope.saldoCuentaInversion = value.Saldo;
								    scope.objvaloresSaldo.saldoCuentaInversion=scope.saldoCuentaInversion;
							});
						}
				},function(result){
					bcsComunes.msg("error","error","UI_ERROR_OBTENER_SALDO_CAJA");
				});
    		});    
			scope.valoresSaldo = function()
			{
				
 			 return scope.objvaloresSaldo;
				
			};
    	}
	}
});
