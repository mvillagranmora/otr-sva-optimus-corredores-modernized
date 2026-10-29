angular.module('ModuloFFM').directive('bcsFechasSolicitudFondo', function($rootScope,$resource,bcsServicioFondosMutuos) {
	return {
		restrict: 'E',
		replace: false,
		scope: {
			valoresFechas			  : '=?',
			fechaEmision			  : '=?',
			idSerieFondo 			  : '=?',
			idSerieFondoDestino		  : '=?',
			traspaso				  : '=?',
			tipoSolicitud			  : '=?',
			idMoneda				  : '=?',
			idMonedaPago			  : '=?',
			idMonedaFondoDestino	  : '=?',
			mostrarFechaAceptacion 	  : '=?',
			mostrarFechaValorCuota    : '=?',
			mostrarFechaCertificacion : '=?',
			mostrarFechaLiquidacion	  : '=?',
			fechaAceptacionSaldo	  : '=?',
			fechaValorCuotaSaldo	  : '=?',
			fechaAceptacion			  : '=?'
		},		
		templateUrl: '../../FondoMutuo/UI/bcs-fechas-solicitud-fondo/bcs-fechas-solicitud-fondo.html',
    	link: function(scope, element, attrs, fn) {
    		scope.fechaAceptacion = util.dateInicioDia($rootScope.SesionFechaSistema);
    		scope.fechaValorCuota = util.dateInicioDia($rootScope.SesionFechaSistema);
			scope.fechaCertificacion = util.dateInicioDia($rootScope.SesionFechaSistema);
			scope.fechaLiquidacion = util.dateInicioDia($rootScope.SesionFechaSistema);
			scope.objvaloresFechas = {};
			scope.$watchGroup(['fechaEmision','idSerieFondo','traspaso','tipoSolicitud','idSerieFondoDestino','idMoneda','idMonedaPago','idMonedaFondoDestino'],function(newval,oldval){
			scope.objvaloresFechas.fechaAceptacion=util.formatoDate(scope.fechaAceptacion);
			scope.objvaloresFechas.fechaValorCuota=util.formatoDate(scope.fechaValorCuota);
			scope.objvaloresFechas.fechaCertificacion=util.formatoDate(scope.fechaCertificacion);
			scope.objvaloresFechas.fechaLiquidacion=util.formatoDate(scope.fechaLiquidacion);
			scope.objvaloresFechas.fechaAceptacionSaldo=util.formatoDate(scope.fechaAceptacionSaldo);
			scope.objvaloresFechas.fechaAceptacionRes=scope.fechaAceptacion;
			
    			if (!scope.fechaEmision || !scope.idSerieFondo || !scope.traspaso || !scope.tipoSolicitud)
    				return;    				
    			
				if(!scope.idSerieFondoDestino){
	    			var datos = {
	    				p_IdEmpresa 		: $rootScope.SesionIdEmpresa, 
	    				p_IdUsuario     	: $rootScope.SesionIdUsuario,
	    				p_IdModulo     		: 'FMM',
	    				p_SimularOperacion	: 'N',
	    				p_UsaReglasNegocio	: 'S',
	    				p_SolicitudAporteRescate 	: [{
								    					IdInterno 			: 0,
								    					FechaEmision 		: typeof(scope.fechaEmision)=="string"?'':util.dateSPdate(scope.fechaEmision),
								    					IdSerieFondo 		: scope.idSerieFondo,
								    					TipoSolicitud 		: scope.tipoSolicitud,
								    					IdMoneda			: scope.idMoneda,
								    					IdMonedaPago		: scope.idMonedaPago,
								    				}],
					}
    			} else{
    				var datos = {
    	    				p_IdEmpresa 		: $rootScope.SesionIdEmpresa, 
    	    				p_IdUsuario     	: $rootScope.SesionIdUsuario,
    	    				p_IdModulo     		: 'FMM',
    	    				p_SimularOperacion	: 'N',
    	    				p_UsaReglasNegocio	: 'S',
    	    				p_SolicitudAporteRescate 	: [{
    								    					IdInterno 			: 0,
    								    					FechaEmision 		: typeof(scope.fechaEmision)=="string"?'':util.dateSPdate(scope.fechaEmision),
    								    					IdSerieFondo 		: scope.idSerieFondo,
    								    					TipoSolicitud 		: scope.tipoSolicitud,
    								    					IdMoneda			: scope.idMoneda,
    								    					IdMonedaPago		: scope.idMonedaPago,
														},{
															IdInterno 			: 0,
									    					FechaEmision 		: typeof(scope.fechaEmision)=="string"?'':util.dateSPdate(scope.fechaEmision),
									    					IdSerieFondo 		: scope.idSerieFondoDestino,
									    					TipoSolicitud 		: scope.tipoSolicitud,
									    					IdMoneda			: scope.idMonedaFondoDestino,
									    					IdMonedaPago		: scope.idMonedaPago,
    													}],
    					}
    			}

    			bcsServicioFondosMutuos.ConsultarFechaSolicitudAporteRescate(datos).$promise.then(function(result){
    				scope.fechaAceptacion = util.dateInicioDia($rootScope.SesionFechaSistema);
    				scope.fechaCertificacion = util.dateInicioDia($rootScope.SesionFechaSistema);
    				scope.fechaLiquidacion = util.dateInicioDia($rootScope.SesionFechaSistema);
    				if(result.P_OUT_RESULTADO[0] != undefined && result.P_OUT_ERRORESCONTADOR === 0){
						scope.fechaAceptacion = util.formatoDate(result.P_OUT_RESULTADO[0].FechaAceptacion);
						scope.fechaValorCuota = util.formatoDate(result.P_OUT_RESULTADO[0].FechaValorCuota);
						scope.fechaCertificacion = util.formatoDate(result.P_OUT_RESULTADO[0].FechaCertificacion);
						scope.fechaLiquidacion = util.formatoDate(result.P_OUT_RESULTADO[0].FechaLiquidacion);
						scope.fechaAceptacionSaldo = util.dateTerminoDia(result.P_OUT_RESULTADO[0].FechaAceptacion);
						scope.fechaValorCuotaSaldo = result.P_OUT_RESULTADO[0].FechaValorCuota;
						scope.objvaloresFechas.fechaAceptacion=scope.fechaAceptacion;
						scope.objvaloresFechas.fechaValorCuota=scope.fechaValorCuota;
						scope.objvaloresFechas.fechaCertificacion=scope.fechaCertificacion;
						scope.objvaloresFechas.fechaLiquidacion=scope.fechaLiquidacion;
						scope.objvaloresFechas.fechaAceptacionSaldo=scope.fechaAceptacionSaldo;
						scope.objvaloresFechas.fechaAceptacionRes=result.P_OUT_RESULTADO[0].FechaAceptacion;
						if (scope.$parent.rescateNormal) {
							scope.$parent.rescateNormal.FechaCurse = result.P_OUT_RESULTADO[0].FechaAceptacion;
						} else {
							console.error("rescateNormal is undefined");
							// Opcionalmente, inicializa rescateNormal si es necesario
							scope.$parent.rescateNormal = { FechaCurse: result.P_OUT_RESULTADO[0].FechaAceptacion };
						}
						
					}
				},function(result){
					console.log(result);
				});
    		});
			scope.valoresFechas = function()
			{
				
 			 return scope.objvaloresFechas;
				
			};
    	}

	}
});
