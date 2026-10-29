angular.module('ModuloAPV').directive('bcsFechasSolicitudFondo', function($rootScope,$resource,bcsServiciosAPV) {
	return {
		restrict: 'E',
		replace: false,
		scope: {
			fechaEmision			  : '=?',
			idSerieFondo 			  : '=?',
			traspaso				  : '=?',
			tipoSolicitud			  : '=?',
			mostrarFechaAceptacion 	  : '=?',
			mostrarFechaValorCuota    : '=?',
			mostrarFechaCertificacion : '=?',
			mostrarFechaLiquidacion	  : '=?',
			fechaAceptacionSaldo	  : '=?'
		},		
		templateUrl: '../../APV/UI/InversionDesinversion/Inversion/bcs-fechas-solicitud-fondo/bcs-fechas-solicitud-fondo.html',
    	link: function(scope, element, attrs, fn) {
    		scope.fechaAceptacion = util.dateInicioDia($rootScope.SesionFechaSistema);
    		scope.fechaValorCuota = util.dateInicioDia($rootScope.SesionFechaSistema);
			scope.fechaCertificacion = util.dateInicioDia($rootScope.SesionFechaSistema);
			scope.fechaLiquidacion = util.dateInicioDia($rootScope.SesionFechaSistema);
			scope.$watchGroup(['fechaEmision','idSerieFondo','traspaso','tipoSolicitud'],function(newval,oldval){
    			
    			if (!scope.fechaEmision || !scope.idSerieFondo || !scope.traspaso || !scope.tipoSolicitud){
    				return;
    			}
    			
    			var datos = {
    				p_IdEmpresa 		: $rootScope.SesionIdEmpresa, 
    				p_IdUsuario     	: $rootScope.SesionIdUsuario,
    				p_IdModulo     		: 'FMM',
    				p_SimularOperacion	: 'N',
    				p_UsaReglasNegocio	: 'S',
    				p_SolicitudAporteRescate 	: [{
							    					IdInterno : 0,
							    					FechaEmision : util.dateSPdate(scope.fechaEmision),
							    					IdSerieFondo : scope.idSerieFondo,
													TipoSolicitud : scope.tipoSolicitud,
												}],
				}

    			bcsServiciosAPV.ConsultarFechaSolicitudAporteRescate(datos).$promise.then(function(result){
    				scope.fechaAceptacion = util.dateInicioDia($rootScope.SesionFechaSistema);
    				scope.fechaCertificacion = util.dateInicioDia($rootScope.SesionFechaSistema);
    				scope.fechaLiquidacion = util.dateInicioDia($rootScope.SesionFechaSistema);
    				if(result.P_OUT_RESULTADO[0] != undefined && result.P_OUT_ERRORESCONTADOR === 0){
						scope.fechaAceptacion = util.formatoDate(result.P_OUT_RESULTADO[0].FechaAceptacion);
						scope.fechaValorCuota = util.formatoDate(result.P_OUT_RESULTADO[0].FechaValorCuota);
						scope.fechaCertificacion = util.formatoDate(result.P_OUT_RESULTADO[0].FechaCertificacion);
						scope.fechaLiquidacion = util.formatoDate(result.P_OUT_RESULTADO[0].FechaLiquidacion);
						scope.fechaAceptacionSaldo = util.dateTerminoDia(result.P_OUT_RESULTADO[0].FechaAceptacion);
					}
				},function(result){
					console.log(result);
				});
    		});   		
    	}
	}
});
