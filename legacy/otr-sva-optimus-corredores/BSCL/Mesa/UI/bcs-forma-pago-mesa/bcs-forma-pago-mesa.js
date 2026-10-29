angular.module('ModuloMesa').directive('bcsFormaPagoMesa', function(bcsComunes,$resource,$timeout,$rootScope,$q,bcsServiciosGrillasMesaRF,$translate,bcsServicioPlataforma,bcsServiciosMesa) {
	return {
		restrict: 'E',
    	replace: false,
		scope : {
			idPortafolio : '=?',
			idTipoProducto : '=?',
			idTipoOperacionPrincipal : '=?',
			idTipoOperacionSecundaria : '=?',
			montoOperacionMonedaPrincipal : '=?',
			monedaPrincipal : '=?',
			montoOperacionMonedaSecundaria : '=?',
			monedaSecundaria : '=?',
			fechaOperacion :'=?',
			limpiarDatos : '=?',
			activado : '=?',
			idOperacion : '=?',
			operaCuentaInversion : '=?',
			formaDePagoEntregamos :'=?',
			formaDePagoRecibimos :'=?',
			montoAsignadoRecibimos : '=?',
			montoAsignadoEntregamos : '=?',
			opcionVentana : '=?',
			modulo : '=?',
			pagaConCtaInv: '=?',
			matrizFormaPagoEntregamos: '=?',
			matrizFormaPagoRecibimos: '=?',
			idPortafolioCorredora: '=?',
			idInstrumento: '=?',
			cargaPortafolio: '=?'
		},
		templateUrl: '../../Mesa/UI/bcs-forma-pago-mesa/bcs-forma-pago-mesa.html', 
    	link: function (scope, element, attrs, fn) {
			var cargarDatos = false;
    		var soloIdOperacionMesaRF = false;
    		var idOperacionAux = 0;
    		var confMesAux = [];
			
    		
			//Se agregan varios console.log para revisar por donde pasa el código.
			//No sirve mucho poner puntos de interrupcion debido a la ejecucion asincrona y la gran cantidad de $watch.
			//Se utiliza la variable muestraLog para controlar el despliegue de los console.log
			var muestraLog = false;
    		
    		scope.formaPagoCliente = {};
			scope.formaPagoCorredora = {};
			scope.formaPagoMesa = {};
			scope.formaPagoMesaSpot = {};
    		scope.operaCuentaInversionCliente = '';
    		scope.operaCuentaInversion = false;
    		scope.pagaConCtaInv = false;
    		scope.operaCuentaInversionEnabled = true;
    		scope.idUsoMonedaFormato = 'GEN';
    		scope.cambioPlazo = false;

			if(scope.modulo === 'SPOT'){
				if (scope.idPortafolio == false || scope.idPortafolio == undefined){scope.idPortafolio = 0;}
				if (scope.idPortafolioCorredora == false || scope.idPortafolioCorredora == undefined ){scope.idPortafolioCorredora = 0;}
				scope.Portafolio	= {"field": "IdPortafolio",		"operator": "eq", "value": scope.idPortafolio};
				scope.PortafolioCorredora	= {"field": "IdPortafolio",		"operator": "eq", "value": scope.idPortafolioCorredora};
			}


    		scope.TipoProducto	= {"field": "IdTipoProducto",	"operator": "eq", "value": scope.idTipoProducto };
			scope.TipoOperacion1= {"field": "IdTipoOperacion",	"operator": "eq", "value": scope.idTipoOperacionPrincipal };		// Movimiento Compra significa corredora/mesa compra | cliente vende
			scope.TipoOperacion2= {"field": "IdTipoOperacion",	"operator": "eq", "value": scope.idTipoOperacionSecundaria };
    		scope.Moneda1		= {"field": "IdMoneda",			"operator": "eq", "value": scope.monedaPrincipal };
    		scope.Moneda2		= {"field": "IdMoneda",			"operator": "eq", "value": scope.monedaSecundaria };
    		scope.Estado		= {"field": "IdEstado",			"operator": "eq", "value": 'ACT' };


    		var conf = bcsServiciosGrillasMesaRF.getDataSourceFormasPagoPortafolioMesa(); //formas de pago del cliente(asociado a los medios de pago)
    		var confMes = bcsServiciosGrillasMesaRF.getDataSourceFormasPagoMesa();//formas de pago configuradas en la mesa
    		var dataSourceDetalleComprobantePagoOperacion = bcsServiciosGrillasMesaRF.getDataSourceComprobantePagoOperacionMesaSpot();
    		var dataSourcePreferenciaPortafolio = bcsServiciosGrillasMesaRF.getDataSourcePreferenciaPortafolio();
			var dataSourcePreferenciaPagoPortafolioCliente = bcsServiciosGrillasMesaRF.getDataSourcePreferenciaPagoPortafolioCliente();
			var dataSourcePreferenciaPagoPortafolioCorredora = bcsServiciosGrillasMesaRF.getDataSourcePreferenciaPagoPortafolioCorredor();
			var monedasPorProductoFeriadoMonPrinc;//solo monedas por producto con check "Aplica feriado moneda principal"
			var monedasPorProducto= function(){
				bcsServicioPlataforma.Servicios().MonedasPorProductoFeriadoMonPrinc().$promise.then(function(result){
					monedasPorProductoFeriadoMonPrinc=result.d.results;
				},function(result){
					bcsComunes.msg("warning",$translate.instant("UI_ERROR"),$translate.instant('UI_ERROR_AL_OBTENER_FECHA_SIGUIENTE_HABIL'));
				});
			}
			monedasPorProducto();
			var calculaFechaBase = function (ultimoDiaHabil){
				var datoDia = {
					p_diaHabil: 'S',
					p_soloCaeDiaHabil: 'S',
					p_fechas: []
				};
				datoDia.p_fechas.push({
					"fechaInicial"	: ultimoDiaHabil,//fecha base para el calculo del siguiente dia hábil
					"numeroDias"	: 1,
					"paises"        : [$rootScope.SesionIdPaisSistema,223]
				});
				
				bcsServicioPlataforma.Servicios().ObtenerDiaSiguienteMultiplesPaises(datoDia,function(result){//calculo dia habil chile
					if(result.P_OUT_ERRORESCONTADOR==0){
						fechaBase=result.p_out_fechas[0].fechaCalculada;
					}else{
						bcsComunes.msg("warning",$translate.instant("UI_ERROR"),$translate.instant('UI_ERROR_AL_OBTENER_FECHA_SIGUIENTE_HABIL'));		
					}
				},function(result){
					bcsComunes.msg("warning",$translate.instant("UI_ERROR"),$translate.instant('UI_ERROR_AL_OBTENER_FECHA_SIGUIENTE_HABIL'));
				});
			}


			var calculaUltimoDiaHabil = function (){
				var datosDiaAnterior = {
					p_IdEmpresa			: $rootScope.SesionIdEmpresa,
					p_IdUsuario			: '',
					p_IdModulo			: 'MSP',
					p_UsaReglasNegocio	: 'N',
					p_Fecha				: angular.isDate(scope.fechaOperacion) ? util.dateSPdate(scope.fechaOperacion) : $rootScope.SesionFechaSistema,
					p_NumeroDias		: 1,
					p_EsHabil			: 'S',
					p_IdPais			: $rootScope.SesionIdPaisSistema
				};
				bcsServicioPlataforma.FechaAnterior(datosDiaAnterior,function(respuesta) {
					if(respuesta.P_OUT_FECHASIGUIENTE && respuesta.P_OUT_ERRORESCONTADOR === 0){

						calculaFechaBase(respuesta.P_OUT_FECHASIGUIENTE);
					}
					else{
						console.log("ERROR: " + JSON.stringify(respuesta));
					}
				},function(respuesta) {
					console.log("ERROR: " + JSON.stringify(respuesta));
				});
			}
			calculaUltimoDiaHabil();
    		/***********************
    		 * CONFIGURACION COMBOS 
    		 ***********************/

    		scope.oConfiguracionFormaPago1={
				tipo : scope.modulo === 'SPOT' ?  "combobox-forma-pago-corredora-spot" : "combobox-forma-pago-mesa",
				configuracion :{
					placeholder: $translate.instant("UI_FORMA_DE_PAGO"),
					template: scope.modulo === 'SPOT' ? '<span >#: Descripcion # - #:IdMoneda#</span>' : '<span >#: NombreFormaDePago # - #:IdMoneda#</span>'
				},
				idActual : false,
				seleccionado : false,
				filtros: {
					complejo : [ scope.TipoProducto, scope.TipoOperacion1, scope.Moneda1, scope.Estado ],
					logic: "and",
    				filters: [
    					 scope.TipoOperacion1
    				    ,scope.Moneda1
    				    ,scope.TipoProducto
    				    ,scope.Estado


    				]
				},
				idDefecto : false,
				idPostFiltro : false,
				itemPostFiltro : false
			};
			if(scope.modulo === 'SPOT'  ){
				if(scope.idPortafolioCorredora == false || scope.idPortafolioCorredora == undefined){
					scope.oConfiguracionFormaPago1.filtros.complejo.push(scope.PortafolioCorredora);
					scope.oConfiguracionFormaPago1.filtros.filters.push(scope.PortafolioCorredora);
				}
				
			}

			scope.oConfiguracionFormaPago2={
				tipo : scope.modulo === 'SPOT' ?  "combobox-forma-pago-cliente-spot" : "combobox-forma-pago-mesa",
				configuracion :{
					placeholder: $translate.instant("UI_FORMA_DE_PAGO"),
					template: scope.modulo === 'SPOT' ? '<span >#: Descripcion # - #:IdMoneda#</span>' : '<span >#: NombreFormaDePago # - #:IdMoneda#</span>'
				},
				idActual : false,
				seleccionado : false,
				filtros: {
					complejo : [ scope.TipoProducto, scope.TipoOperacion2, scope.Moneda2, scope.Estado  ],
					logic: "and",
    				filters: [
    						 scope.TipoOperacion2
    				        ,scope.Moneda2
    				        ,scope.TipoProducto
    				        ,scope.Estado
    				]
				},
				idDefecto : false,
				idPostFiltro : false,
				itemPostFiltro : false
			};
			if(scope.modulo === 'SPOT'){
				if(scope.idPortafolio == false || scope.idPortafolio == undefined){
					scope.oConfiguracionFormaPago2.filtros.complejo.push(scope.Portafolio);
					scope.oConfiguracionFormaPago2.filtros.filters.push(scope.Portafolio);
				}
			}
			
    		
    		/***********************
    		 * FUNCIONES
    		 ***********************/
    		var limpiar = function(){
				if (muestraLog) console.log("bcs-forma-pago-mesa.js :: var limpiar");
				
				scope.matrizFormaPagoEntregamos = {};
	    		scope.formaDePagoEntregamos ={};
				scope.Paga = false;
				scope.montoAsignadoEntregamos = 0;
				scope.monedaAsignadaEntregamos = '';
				scope.cantDecmonedaAsignadaEntregamos = '';
				
				scope.matrizFormaPagoRecibimos = {};
	    		scope.formaDePagoRecibimos ={};
	    		scope.Recibe = false;
				scope.montoAsignadoRecibimos = 0;
				scope.monedaAsignadaRecibimos = '';
				scope.cantDecmonedaAsignadaRecibimos = '';
    		};

			
    		
    		var asignaDefecto = function(combo, tipoOperacion, moneda, entregaRecibe){
    			if (muestraLog) console.log("bcs-forma-pago-mesa.js :: var asignaDefecto");
    			
    			if (combo.seleccionado) return;
    			if (cargarDatos || (idOperacionAux && !soloIdOperacionMesaRF)) return;
    			
    			if (muestraLog) console.log("bcs-forma-pago-mesa.js :: var asignaDefecto - operaCuentaInversionCliente = " + scope.operaCuentaInversionCliente);
				if (scope.operaCuentaInversionCliente === 'N'){
					if(scope.modulo === 'FFMM'){
						$.each(scope.formaPagoCliente,function(key,value){
							if (!combo.idActual && value.PorDefecto ==='S' && value.IdTipoOperacion === tipoOperacion && value.IdMoneda === moneda){//asigno el primero del cliente por defecto
								combo.idActual = value.IdFormaDePago;	
								combo.idDefecto = value.IdFormaDePago;	
								combo.idPostFiltro = value.IdFormaDePago;
							}
						});
						
						$.each(scope.formaPagoCliente,function(key,value){//asigno el primero del cliente
							if (!combo.idActual && value.IdTipoOperacion === tipoOperacion && value.IdMoneda === moneda){
								combo.idActual = value.IdFormaDePago;
								combo.idDefecto = value.IdFormaDePago;
								combo.idPostFiltro = value.IdFormaDePago;	
							}
						});
	
						if (!combo.idActual){
							if (Object.keys(scope.formaPagoMesa).length < 1) {
								$.each(confMesAux,function(key,value){
		    						if (value.IdTipoOperacion === scope.idTipoOperacionPrincipal || value.IdTipoOperacion === scope.idTipoOperacionSecundaria) {
		    							scope.formaPagoMesa[value.IdFormaDePago] = value;
		    						}
		    					});
			    			}
							$.each(scope.formaPagoMesa,function(key,value){ // asigna lo de la corredora por defecto
								if(!combo.idActual && value.PorDefecto === 'S' && value.IdTipoOperacion === tipoOperacion && value.IdMoneda === moneda ){
									combo.idActual = value.IdFormaDePago;
									combo.idDefecto = value.IdFormaDePago;
									combo.idPostFiltro = value.IdFormaDePago;	
								}
							});
							$.each(scope.formaPagoMesa,function(key,value){//asigna el primero de la corredora
								if(!combo.idActual && value.IdTipoOperacion === tipoOperacion && value.IdMoneda === moneda ){
									combo.idActual = value.IdFormaDePago;
									combo.idDefecto = value.IdFormaDePago;
									combo.idPostFiltro = value.IdFormaDePago;	
								}
							});
						}
					}
					else{
						
					if (angular.equals(scope.formaDePagoClienteAuxiliar, {})){
						if (Object.keys(scope.formaPagoMesa).length < 1) {
							$.each(confMesAux,function(key,value){
	    						if (value.IdTipoOperacion === scope.idTipoOperacionPrincipal || value.IdTipoOperacion === scope.idTipoOperacionSecundaria) {
	    							scope.formaPagoMesa[value.IdFormaDePago] = value;
	    						}
	    					});
		    			}
						if (Object.keys(scope.formaPagoMesa).length < 1) {
								$.each(confMesAux,function(key,value){
		    						if (value.IdTipoOperacion === scope.idTipoOperacionPrincipal || value.IdTipoOperacion === scope.idTipoOperacionSecundaria) {
		    							scope.formaPagoMesa[value.IdFormaDePago] = value;
		    						}
		    					});
			    			}
							$.each(scope.formaPagoMesa,function(key,value){//asigna lo de la corredora por defecto
								if(!combo.idActual && value.PorDefecto === 'S' && value.IdTipoOperacion === tipoOperacion && value.IdMoneda === moneda ){
									combo.idActual = value.IdFormaDePago;
									combo.idDefecto = value.IdFormaDePago;
									combo.idPostFiltro = value.IdFormaDePago;	
								}
							});
							$.each(scope.formaPagoMesa,function(key,value){//asigna el primero de la corredora
								if(!combo.idActual && value.IdTipoOperacion === tipoOperacion && value.IdMoneda === moneda ){
									combo.idActual = value.IdFormaDePago;
									combo.idDefecto = value.IdFormaDePago;
									combo.idPostFiltro = value.IdFormaDePago;	
								}
							});
				}else{
					//identifico si es fisica o compensada en el contexto por defecto
					if(!angular.equals(scope.formaDePagoClienteAuxiliar, {}) && !angular.equals(scope.formaDePagoMesaAuxiliar, {})){
						
							if (Object.keys(scope.formaPagoMesa).length < 1) {
								$.each(confMesAux,function(key,value){
		    						if (value.IdTipoOperacion === scope.idTipoOperacionPrincipal || value.IdTipoOperacion === scope.idTipoOperacionSecundaria) {
		    							scope.formaPagoMesa[value.IdFormaDePago] = value;
		    						}
		    					});
			    			}
							
							if(!angular.equals(scope.formaDePagoClienteAuxiliar, {})){
								$.each(scope.formaPagoMesa,function(key,value){
									if (!combo.idActual && value.IdFormaDePago === Object.getOwnPropertyNames(scope.formaDePagoClienteAuxiliar)[0] && value.IdTipoOperacion === tipoOperacion && value.IdMoneda === moneda){
										combo.idActual = value.IdFormaDePago;
										combo.idDefecto = value.IdFormaDePago;
										combo.idPostFiltro = value.IdFormaDePago;
									}
								});
							}
							if(!angular.equals(scope.formaDePagoMesaAuxiliar, {})){
								$.each(scope.formaPagoMesa,function(key,value){
									if (!combo.idActual && value.IdFormaDePago === Object.getOwnPropertyNames(scope.formaDePagoMesaAuxiliar)[0] && value.IdTipoOperacion === tipoOperacion && value.IdMoneda === moneda){
										combo.idActual = value.IdFormaDePago;
										combo.idDefecto = value.IdFormaDePago;
										combo.idPostFiltro = value.IdFormaDePago;
									}
								});
							}					
							
												
					}else{
						//esto es para compensadas
						if(!angular.equals(scope.formaDePagoClienteAuxiliar, {})){
							if (Object.keys(scope.formaPagoMesa).length < 1) {
								$.each(confMesAux,function(key,value){
		    						if (value.IdTipoOperacion === scope.idTipoOperacionPrincipal || value.IdTipoOperacion === scope.idTipoOperacionSecundaria) {
		    							scope.formaPagoMesa[value.IdFormaDePago] = value;
		    						}
		    					});
			    			}
							$.each(scope.formaPagoMesa,function(key,value){
								if (!combo.idActual && value.IdFormaDePago === Object.getOwnPropertyNames(scope.formaDePagoClienteAuxiliar)[0] && value.IdTipoOperacion === tipoOperacion && value.IdMoneda === moneda){
									combo.idActual = value.IdFormaDePago;
									combo.idDefecto = value.IdFormaDePago;
									combo.idPostFiltro = value.IdFormaDePago;
								}
							});
						}
					}
						
				}
						
						//asignacion por defecto con parametro
						if(!combo.idActual){ 
							 if (tipoOperacion == "VENSPT"){
								var params = {
									p_IdEmpresa 	 	: '1',	
									p_IdTipoProducto 	: 'MEME',
									p_CodigoParametro 	: 'FORMAPAGODEFECTO'
								};
								bcsServiciosMesa.ConsultarConfiguracionMesa(params).$promise.then(function(respuesta){
									$.each(respuesta.P_OUT_DATOSMESA,function(key,value){
										combo.idActual = value.Valor;
										combo.idDefecto = value.Valor;
										combo.idPostFiltro = value.Valor;	
									});
								});
						    } else if (tipoOperacion == "COMSPT"){
						    	var params = {
									p_IdEmpresa 	 	: '1',	
									p_IdTipoProducto 	: 'MEME',
									p_CodigoParametro 	: 'FORMAPAGODEFECTOCOMPRA'
								};
								bcsServiciosMesa.ConsultarConfiguracionMesa(params).$promise.then(function(respuesta){
									$.each(respuesta.P_OUT_DATOSMESA,function(key,value){
										combo.idActual = value.Valor;
										combo.idDefecto = value.Valor;
										combo.idPostFiltro = value.Valor;	
									});
								});
						    }
						}
					}
				}
				else{//solo sirve cuando opera con cuenta de inversion
				
					if (scope.operaCuentaInversionCliente === 'S' && angular.equals(scope.formaDePagoClienteAuxiliar, {})){
						if (Object.keys(scope.formaPagoMesa).length < 1) {
							$.each(confMesAux,function(key,value){
	    						if (value.IdTipoOperacion === scope.idTipoOperacionPrincipal || value.IdTipoOperacion === scope.idTipoOperacionSecundaria) {
	    							scope.formaPagoMesa[value.IdFormaDePago] = value;
	    						}
	    					});
		    			}
						$.each(scope.formaPagoMesa,function(key,value){
							if (!combo.idActual && value.IdMedioDePago === 'CIN' && value.IdTipoOperacion === tipoOperacion && value.IdMoneda === moneda){
								combo.idActual = value.IdFormaDePago;
								combo.idDefecto = value.IdFormaDePago;
								combo.idPostFiltro = value.IdFormaDePago;
							}
						});
				}else{
					//identifico si es fisica o compensada en el contexto por defecto
					if(!angular.equals(scope.formaDePagoClienteAuxiliar, {}) && !angular.equals(scope.formaDePagoMesaAuxiliar, {})){
						
							if (Object.keys(scope.formaPagoMesa).length < 1) {
								$.each(confMesAux,function(key,value){
		    						if (value.IdTipoOperacion === scope.idTipoOperacionPrincipal || value.IdTipoOperacion === scope.idTipoOperacionSecundaria) {
		    							scope.formaPagoMesa[value.IdFormaDePago] = value;
		    						}
		    					});
			    			}
							
							if(!angular.equals(scope.formaDePagoClienteAuxiliar, {})){
								$.each(scope.formaPagoMesa,function(key,value){
									if (!combo.idActual && value.IdFormaDePago === Object.getOwnPropertyNames(scope.formaDePagoClienteAuxiliar)[0] && value.IdTipoOperacion === tipoOperacion && value.IdMoneda === moneda){
										combo.idActual = value.IdFormaDePago;
										combo.idDefecto = value.IdFormaDePago;
										combo.idPostFiltro = value.IdFormaDePago;
									}
								});
							}
							if(!angular.equals(scope.formaDePagoMesaAuxiliar, {})){
								$.each(scope.formaPagoMesa,function(key,value){
									if (!combo.idActual && value.IdFormaDePago === Object.getOwnPropertyNames(scope.formaDePagoMesaAuxiliar)[0] && value.IdTipoOperacion === tipoOperacion && value.IdMoneda === moneda){
										combo.idActual = value.IdFormaDePago;
										combo.idDefecto = value.IdFormaDePago;
										combo.idPostFiltro = value.IdFormaDePago;
									}
								});
							}					
							
												
					}else{
						//esto es para compensadas
						if(!angular.equals(scope.formaDePagoClienteAuxiliar, {})){
							if (Object.keys(scope.formaPagoMesa).length < 1) {
								$.each(confMesAux,function(key,value){
		    						if (value.IdTipoOperacion === scope.idTipoOperacionPrincipal || value.IdTipoOperacion === scope.idTipoOperacionSecundaria) {
		    							scope.formaPagoMesa[value.IdFormaDePago] = value;
		    						}
		    					});
			    			}
							$.each(scope.formaPagoMesa,function(key,value){
								if (!combo.idActual && value.IdFormaDePago === Object.getOwnPropertyNames(scope.formaDePagoClienteAuxiliar)[0] && value.IdTipoOperacion === tipoOperacion && value.IdMoneda === moneda){
									combo.idActual = value.IdFormaDePago;
									combo.idDefecto = value.IdFormaDePago;
									combo.idPostFiltro = value.IdFormaDePago;
								}
							});
						}
					}
						
				}
				if (muestraLog) console.log("bcs-forma-pago-mesa.js :: var asignaDefecto - combo.idActual : " + combo.idActual);} 
    		}

			var asignaDefectoSpotCliente = function(combo, tipoOperacion, moneda, entregaRecibe){
    			if (muestraLog) console.log("bcs-forma-pago-mesa.js :: var asignaDefecto");
    			
    			if (combo.seleccionado) return;
    			if (cargarDatos || (idOperacionAux && !soloIdOperacionMesaRF)) return;
    			
    			if (muestraLog) console.log("bcs-forma-pago-mesa.js :: var asignaDefecto - operaCuentaInversionCliente = " + scope.operaCuentaInversionCliente);
				
					if (angular.equals(scope.formaDePagoClienteAuxiliar, {})){
						
						
							$.each(scope.formaPagoCliente,function(key,value){//asigna lo de la ClformaPago por defecto
								if(!combo.idActual && value.PorDefecto === 'S' && value.IdTipoOperacion === tipoOperacion && value.IdMoneda === moneda ){
									combo.idActual = value.Descripcion;
									combo.idDefecto = value.Descripcion;
									combo.idPostFiltro = value.Descripcion;	
								}
							});
							$.each(scope.formaPagoCliente,function(key,value){
								if (!combo.idActual && value.IdMedioDePago === 'CIN' && value.IdTipoOperacion === tipoOperacion && value.IdMoneda === moneda && value.Dias === 0){
									combo.idActual = value.Descripcion;
									combo.idDefecto = value.Descripcion;
									combo.idPostFiltro = value.Descripcion;	
								}
							});
					}
						
						
					
				
				
    		}
			var asignaDefectoSpotCorredora = function(combo, tipoOperacion, moneda, entregaRecibe){
    			if (muestraLog) console.log("bcs-forma-pago-mesa.js :: var asignaDefecto");
    			
    			if (combo.seleccionado) return;
    			if (cargarDatos || (idOperacionAux && !soloIdOperacionMesaRF)) return;
    			
    			if (muestraLog) console.log("bcs-forma-pago-mesa.js :: var asignaDefecto - operaCuentaInversionCliente = " + scope.operaCuentaInversionCliente);
				
					if (angular.equals(scope.formaDePagoClienteAuxiliar, {})){
						
						
							$.each(scope.formaPagoCorredora,function(key,value){//asigna lo de la ClformaPago por defecto
								if(!combo.idActual && value.PorDefecto === 'S' && value.IdTipoOperacion === tipoOperacion && value.IdMoneda === moneda ){
									combo.idActual = value.Descripcion;
									combo.idDefecto = value.Descripcion;
									combo.idPostFiltro = value.Descripcion;	
								}
							});
							$.each(scope.formaPagoCorredora,function(key,value){
								if (!combo.idActual && value.IdMedioDePago === 'CIN' && value.IdTipoOperacion === tipoOperacion && value.IdMoneda === moneda && value.Dias === 0){
									combo.idActual = value.Descripcion;
									combo.idDefecto = value.Descripcion;
									combo.idPostFiltro = value.Descripcion;
								}
							});
					}
						
			}
    		
    		
    		scope.Agregar = function (comboSelec) {
    			if (muestraLog) console.log("bcs-forma-pago-mesa.js :: scope.Agregar");
    			
    			if (cargarDatos || (idOperacionAux && !soloIdOperacionMesaRF)) return;
    			
				if (!(comboSelec && scope.operaCuentaInversionCliente != '' && comboSelec.seleccionado)) return;
					
				if (!(scope.idPortafolio > 0 && ((scope.idTipoOperacionPrincipal && scope.montoOperacionMonedaPrincipal > 0) || (scope.idTipoOperacionSecundaria && scope.montoOperacionMonedaSecundaria > 0)))) {
					comboSelec.idActual = false;
					return;
				}
				if (comboSelec.seleccionado.IdMedioDePago === 'CIN' && scope.operaCuentaInversionCliente === 'N'){
					comboSelec.idActual = false;

					bcsComunes.msg("warning",$translate.instant("UI_ERROR"),$translate.instant('PORTAFOLIO_NO_OPERA_CON_CUENTA_INVERSION'));
					return;
				}				
				if ((comboSelec.seleccionado.RecibePagaPortafolio === 'R' && scope.montoAsignadoEntregamos >= scope.montoOperacionMonedaPrincipal) ||
					(comboSelec.seleccionado.RecibePagaPortafolio === 'P' && scope.montoAsignadoRecibimos >= scope.montoOperacionMonedaSecundaria)) {
					comboSelec.idActual = false;

					if (muestraLog) console.log("bcs-forma-pago-mesa.js :: scope.Agregar - IF MONTOS : " + comboSelec.seleccionado.IdFormaDePago);
					return;
				}
				if ((comboSelec.seleccionado.RecibePagaPortafolio === 'R' && scope.matrizFormaPagoEntregamos[comboSelec.seleccionado.IdFormaDePago]) || 
					(comboSelec.seleccionado.RecibePagaPortafolio === 'P' && scope.matrizFormaPagoRecibimos[comboSelec.seleccionado.IdFormaDePago])){

					comboSelec.idActual = false;
					if (muestraLog) console.log("bcs-forma-pago-mesa.js :: scope.Agregar - IF FORMA_PAGO_YA_ASIGNADO : " + comboSelec.seleccionado.IdFormaDePago);
					bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('FORMA_PAGO_ASIGNADO'));
					return;	
				}				
		
				
				var formaPagoClienteSel = scope.formaPagoCliente[comboSelec.seleccionado.IdFormaDePago];
				var datos = (cargarDatos && scope.matrizFormaPagoExist) ? scope.matrizFormaPagoExist : formaPagoClienteSel ? [formaPagoClienteSel] : [comboSelec.seleccionado];

				var datoDia = {
					p_IdPais	: $rootScope.SesionIdPaisSistema,
					p_DiaHabil	: 'S',
					p_FechasIN	: []
				};
				for(i=0;i < datos.length;i++){
					datoDia.p_FechasIN.push({
						"FechaInicial"	: angular.isDate(scope.fechaOperacion) ? util.dateSPdate(scope.fechaOperacion) : $rootScope.SesionFechaSistema,
						"NumeroDias"	: datos[i].Dias ? datos[i].Dias : 0
					});
				}
				

				var verificaFeriadosUSA = function(monedasPorProductoFeriadoMonPrinc){//verifica si aplica feriado moneda principal
					var resultado=false;
					for(m=0;m < monedasPorProductoFeriadoMonPrinc.length;m++){

						if ((monedasPorProductoFeriadoMonPrinc[m].IdInstrumento==scope.idInstrumento)
							&& monedasPorProductoFeriadoMonPrinc[m].IdMonedaPrincipal=='USD'
							&& (scope.monedaPrincipal=='USD'||(scope.monedaPrincipal=='CLP'&& scope.idTipoOperacionPrincipal=='COMSPT'))
							&& monedasPorProductoFeriadoMonPrinc[m].IdMonedaSecundaria=='CLP'
							&& (scope.monedaSecundaria=='CLP'||(scope.monedaSecundaria=='USD'&& scope.idTipoOperacionPrincipal=='COMSPT'))
							&& monedasPorProductoFeriadoMonPrinc[m].IdEstado=='ACT'
							&& monedasPorProductoFeriadoMonPrinc[m].FeriadoMonedaPrincipal=='SI'
							){
							resultado=true;
						}
					}
					return resultado;

				}

				var IdFormaDePago = '';

				var FechaLiquidacion="";
				
				//calculo de fecha de liquidacion segun feriados de EEUU
				var calculoFechaLiquidacionFeriadosEEUU = function(dato, fechaBase){
					$.each(datos,function(key,value){
						if(datos[key]==dato){
							var datoDia = {
								p_diaHabil: 'S',
								p_soloCaeDiaHabil: 'S',
								p_fechas: []
							};
							datoDia.p_fechas.push({
								"fechaInicial"	: fechaBase,
								"numeroDias"	: datos[key].Dias,
								"paises"        : [$rootScope.SesionIdPaisSistema,223]
							});

							console.log(datos[key].Dias)
							bcsServicioPlataforma.Servicios().ObtenerDiaSiguienteMultiplesPaises(datoDia).$promise.then(function(result){//calculo dia habil chile
								if(result.P_OUT_ERRORESCONTADOR==0){
									datos[key].FechaLiquidacion = result.p_out_fechas[key].fechaCalculada;
									console.log(datos[key].FechaLiquidacion)
								}else{
									bcsComunes.msg("warning",$translate.instant("UI_ERROR"),$translate.instant('UI_ERROR_AL_OBTENER_FECHA_SIGUIENTE_HABIL'));		
								}
							},function(result){
								bcsComunes.msg("warning",$translate.instant("UI_ERROR"),$translate.instant('UI_ERROR_AL_OBTENER_FECHA_SIGUIENTE_HABIL'));
							});
						}
					});	
				}
				
				console.log(verificaFeriadosUSA(monedasPorProductoFeriadoMonPrinc));
				
				bcsServicioPlataforma.Servicios().ConsultarDiaSiguienteMasivo(datoDia).$promise.then(function(result){
					if (result.P_FECHASOUTF.length > 0) {
						for(i=0;i < datos.length;i++){
							for(j=0;j < result.P_FECHASOUTF.length;j++){
								if (result.P_FECHASOUTF[j]){
									if(verificaFeriadosUSA(monedasPorProductoFeriadoMonPrinc)){//se debe calcular nueva fecha de liquidacion incluyendo feriados de USA
										calculoFechaLiquidacionFeriadosEEUU(datos[i], (angular.isDate(scope.fechaOperacion) ? util.dateSPdate(scope.fechaOperacion) : $rootScope.SesionFechaSistema))//cambiar por id de EEUU segun corredora	
									}else{
										FechaLiquidacion = util.dateInicioDia(result.P_FECHASOUTF[j].FechaCalculada);
									}
									datos[i].FechaLiquidacion = FechaLiquidacion;
									console.log(datos[i])
								}
							}
							
							if (datos[i].RecibePagaPortafolio === 'R') {
								scope.Paga = true;
								if (!datos[i].cargarDatos)		
									datos[i].Monto = scope.montoAsignadoEntregamos === scope.montoOperacionMonedaPrincipal ? scope.montoOperacionMonedaPrincipal : scope.montoAsignadoEntregamos < scope.montoOperacionMonedaPrincipal ? scope.montoOperacionMonedaPrincipal - scope.montoAsignadoEntregamos : 0;
								scope.matrizFormaPagoEntregamos[datos[i].IdFormaDePago] = datos[i];
								scope.formaDePagoEntregamos[datos[i].IdFormaDePago] = datos[i].Monto;
								
							}
							else if (datos[i].RecibePagaPortafolio === 'P') {
								scope.Recibe = true;
								if (!datos[i].cargarDatos)		
									datos[i].Monto = scope.montoAsignadoRecibimos === scope.montoOperacionMonedaSecundaria ? scope.montoOperacionMonedaSecundaria : scope.montoAsignadoRecibimos < scope.montoOperacionMonedaSecundaria ? scope.montoOperacionMonedaSecundaria - scope.montoAsignadoRecibimos : 0;
								scope.matrizFormaPagoRecibimos[datos[i].IdFormaDePago] = datos[i];
								scope.formaDePagoRecibimos[datos[i].IdFormaDePago] = datos[i].Monto;
								
							}
							if (muestraLog) console.log("bcs-forma-pago-mesa.js :: scope.Agregar - datos[i] i : " + i + " - " + datos[i].IdFormaDePago);
						}
						if (muestraLog) console.log("bcs-forma-pago-mesa.js :: scope.Agregar - sale ciclo for para asignar en scope.matrizFormaPagoEntregamos y matrizFormaPagoRecibimos");
					}
					else {
						bcsComunes.msg("warning",$translate.instant("UI_ERROR"),$translate.instant('UI_ERROR_AL_OBTENER_FECHA_SIGUIENTE_HABIL'));
					}
					
					comboSelec.idActual = false;

					if (cargarDatos){ //es porque termino de cargar

						scope.matrizFormaPagoExist = null;
						scope.oConfiguracionFormaPago1.seleccionado = false;
						scope.oConfiguracionFormaPago1.idActual = false;
						scope.oConfiguracionFormaPago1.itemPostFiltro = false;
						scope.oConfiguracionFormaPago1.idPostFiltro = false;
						scope.oConfiguracionFormaPago2.seleccionado = false;
						scope.oConfiguracionFormaPago2.idActual = false;
						scope.oConfiguracionFormaPago2.itemPostFiltro = false;
						scope.oConfiguracionFormaPago2.idPostFiltro = false;
						cargarDatos = false;
					}
				},function(result){
					bcsComunes.msg("warning",$translate.instant("UI_ERROR"),$translate.instant('UI_ERROR_AL_OBTENER_FECHA_SIGUIENTE_HABIL'));
				});
    		};

			scope.AgregarFPSpot = function (comboSelec) {
    			if (muestraLog) console.log("bcs-forma-pago-mesa.js :: scope.Agregar");
    			
    			if (cargarDatos || (idOperacionAux && !soloIdOperacionMesaRF)) return;
    			
				if (!(comboSelec && scope.operaCuentaInversionCliente != '' && comboSelec.seleccionado)) return;
					
				if (!(scope.idPortafolio > 0 && ((scope.idTipoOperacionPrincipal && scope.montoOperacionMonedaPrincipal > 0) || (scope.idTipoOperacionSecundaria && scope.montoOperacionMonedaSecundaria > 0)))) {
					comboSelec.idActual = false;
					return;
				}
				if ((comboSelec.seleccionado.RecibePagaPortafolio === 'R' && scope.montoAsignadoEntregamos >= scope.montoOperacionMonedaPrincipal) ||
					(comboSelec.seleccionado.RecibePagaPortafolio === 'P' && scope.montoAsignadoRecibimos >= scope.montoOperacionMonedaSecundaria)) {
					comboSelec.idActual = false;

					if (muestraLog) console.log("bcs-forma-pago-mesa.js :: scope.Agregar - IF MONTOS : " + comboSelec.seleccionado.IdPreferenciaPagoPortafolioKey);
					return;
				}
				if ((comboSelec.seleccionado.RecibePagaPortafolio === 'R' && scope.matrizFormaPagoEntregamos[comboSelec.seleccionado.IdPreferenciaPagoPortafolioKey]) || 
					(comboSelec.seleccionado.RecibePagaPortafolio === 'P' && scope.matrizFormaPagoRecibimos[comboSelec.seleccionado.IdPreferenciaPagoPortafolioKey])){

					comboSelec.idActual = false;
					if (muestraLog) console.log("bcs-forma-pago-mesa.js :: scope.Agregar - IF FORMA_PAGO_YA_ASIGNADO : " + comboSelec.seleccionado.IdPreferenciaPagoPortafolioKey);
					bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('FORMA_PAGO_ASIGNADO'));
					return;	
				}				
				
				
				//var formaPagoClienteSel = scope.formaPagoCliente[comboSelec.seleccionado.IdPreferenciaPagoPortafolioKey];
				var datos = (cargarDatos && scope.matrizFormaPagoExist) ? scope.matrizFormaPagoExist :  [comboSelec.seleccionado];

				var datoDia = {
					p_IdPais	: $rootScope.SesionIdPaisSistema,
					p_DiaHabil	: 'S',
					p_FechasIN	: []
				};
				for(i=0;i < datos.length;i++){
					datoDia.p_FechasIN.push({
						"FechaInicial"	: angular.isDate(scope.fechaOperacion) ? util.dateSPdate(scope.fechaOperacion) : $rootScope.SesionFechaSistema,
						"NumeroDias"	: datos[i].Dias ? datos[i].Dias : 0
					});
				}

				var verificaFeriadosUSA = function(monedasPorProductoFeriadoMonPrinc){//verifica si aplica feriado moneda principal
					var resultado=false;
					for(m=0;m < monedasPorProductoFeriadoMonPrinc.length;m++){
						//validación operaciones spot
						if ((monedasPorProductoFeriadoMonPrinc[m].IdInstrumento==scope.idInstrumento)
							&& monedasPorProductoFeriadoMonPrinc[m].IdMonedaPrincipal=='USD'
							&& (scope.monedaPrincipal=='USD'||(scope.monedaPrincipal=='CLP'&& scope.idTipoOperacionPrincipal=='COMSPT'))
							&& monedasPorProductoFeriadoMonPrinc[m].IdMonedaSecundaria=='CLP'
							&& (scope.monedaSecundaria=='CLP'||(scope.monedaSecundaria=='USD'&& scope.idTipoOperacionPrincipal=='COMSPT'))
							&& monedasPorProductoFeriadoMonPrinc[m].IdEstado=='ACT'
							&& monedasPorProductoFeriadoMonPrinc[m].FeriadoMonedaPrincipal=='SI'
							){
							resultado=true;
						}
					}
					return resultado;

				}

				var IdFormaDePago = '';

				var FechaLiquidacion="";
				
				//calculo de fecha de liquidacion segun feriados de EEUU
				var calculoFechaLiquidacionFeriadosEEUU = function(dato, fechaBase){
					$.each(datos,function(key,value){
						if(datos[key]==dato){
							var datoDia = {
								p_diaHabil: 'S',
								p_soloCaeDiaHabil: 'S',
								p_fechas: []
							};
							datoDia.p_fechas.push({
								"fechaInicial"	: fechaBase,
								"numeroDias"	: datos[key].Dias,
								"paises"        : [$rootScope.SesionIdPaisSistema,223]
							});
							bcsServicioPlataforma.Servicios().ObtenerDiaSiguienteMultiplesPaises(datoDia).$promise.then(function(result){//calculo dia habil chile
								if(result.P_OUT_ERRORESCONTADOR==0){
									datos[key].FechaLiquidacion = result.p_out_fechas[key].fechaCalculada;
								}else{
									bcsComunes.msg("warning",$translate.instant("UI_ERROR"),$translate.instant('UI_ERROR_AL_OBTENER_FECHA_SIGUIENTE_HABIL'));		
								}
							},function(result){
								bcsComunes.msg("warning",$translate.instant("UI_ERROR"),$translate.instant('UI_ERROR_AL_OBTENER_FECHA_SIGUIENTE_HABIL'));
							});
						}
					});	
				}

				bcsServicioPlataforma.Servicios().ConsultarDiaSiguienteMasivo(datoDia).$promise.then(function(result){//calculo dia habil chile
					if (result.P_FECHASOUTF.length > 0) {
						for(i=0;i < datos.length;i++){
							for(j=0;j < result.P_FECHASOUTF.length;j++){
								if (result.P_FECHASOUTF[j]){
									if(verificaFeriadosUSA(monedasPorProductoFeriadoMonPrinc)){//se debe calcular nueva fecha de liquidacion incluyendo feriados de USA
										calculoFechaLiquidacionFeriadosEEUU(datos[i], fechaBase)//cambiar por id de EEUU segun corredora	
									}else{
										FechaLiquidacion = util.dateInicioDia(result.P_FECHASOUTF[j].FechaCalculada);
									}
									datos[i].FechaLiquidacion = FechaLiquidacion;
								}
							}
							dias = datos[i].Dias ? datos[i].Dias : 0;
							IdFormaDePago = scope.formaPagoMesaSpot[datos[i].IdPreferenciaPago+"_"+dias].IdFormaDePago;
							datos[i].IdFormaDePago = IdFormaDePago;
							
							if (datos[i].RecibePagaPortafolio === 'R') {
								scope.Paga = true;
								if (!datos[i].cargarDatos)		
									datos[i].Monto = scope.montoAsignadoEntregamos === scope.montoOperacionMonedaPrincipal ? scope.montoOperacionMonedaPrincipal : scope.montoAsignadoEntregamos < scope.montoOperacionMonedaPrincipal ? scope.montoOperacionMonedaPrincipal - scope.montoAsignadoEntregamos : 0;
								scope.matrizFormaPagoEntregamos[datos[i].IdPreferenciaPagoPortafolioKey] = datos[i];
								scope.formaDePagoEntregamos[datos[i].IdPreferenciaPagoPortafolioKey] = datos[i].Monto;
								
							}
							else if (datos[i].RecibePagaPortafolio === 'P') {
								scope.Recibe = true;
								if (!datos[i].cargarDatos)		
									datos[i].Monto = scope.montoAsignadoRecibimos === scope.montoOperacionMonedaSecundaria ? scope.montoOperacionMonedaSecundaria : scope.montoAsignadoRecibimos < scope.montoOperacionMonedaSecundaria ? scope.montoOperacionMonedaSecundaria - scope.montoAsignadoRecibimos : 0;
								scope.matrizFormaPagoRecibimos[datos[i].IdPreferenciaPagoPortafolioKey] = datos[i];
								scope.formaDePagoRecibimos[datos[i].IdPreferenciaPagoPortafolioKey] = datos[i].Monto;
								
							}
							if (muestraLog) console.log("bcs-forma-pago-mesa.js :: scope.Agregar - datos[i] i : " + i + " - " + datos[i].IdPreferenciaPagoPortafolioKey);
						}
						if (muestraLog) console.log("bcs-forma-pago-mesa.js :: scope.Agregar - sale ciclo for para asignar en scope.matrizFormaPagoEntregamos y matrizFormaPagoRecibimos");
					}
					else {
						bcsComunes.msg("warning",$translate.instant("UI_ERROR"),$translate.instant('UI_ERROR_AL_OBTENER_FECHA_SIGUIENTE_HABIL'));
					}
					
					comboSelec.idActual = false;

					if (cargarDatos){ //es porque termino de cargar

						scope.matrizFormaPagoExist = null;
						scope.oConfiguracionFormaPago1.seleccionado = false;
						scope.oConfiguracionFormaPago1.idActual = false;
						scope.oConfiguracionFormaPago1.itemPostFiltro = false;
						scope.oConfiguracionFormaPago1.idPostFiltro = false;
						scope.oConfiguracionFormaPago2.seleccionado = false;
						scope.oConfiguracionFormaPago2.idActual = false;
						scope.oConfiguracionFormaPago2.itemPostFiltro = false;
						scope.oConfiguracionFormaPago2.idPostFiltro = false;
						cargarDatos = false;
					}
				},function(result){
					bcsComunes.msg("warning",$translate.instant("UI_ERROR"),$translate.instant('UI_ERROR_AL_OBTENER_FECHA_SIGUIENTE_HABIL'));
				});
    		};
    		
    		
    		/***********************
    		 * FUNCIONES WATCH
    		 ***********************/
    		scope.$watch('limpiarDatos',function(){
    			if (muestraLog) console.log("bcs-forma-pago-mesa.js :: $watch - limpiarDatos");
    			
    			if (scope.limpiarDatos){
    				scope.limpiarDatos = false;
    				limpiar();
    			}
    		});

			scope.$watch('idInstrumento',function(){
				limpiar();
			});
    		
    		scope.$watch('idPortafolio',function(newval, oldval){
    			if (muestraLog) console.log("bcs-forma-pago-mesa.js :: $watch - idPortafolio - newval/oldval : " + newval + "/" + oldval);
    			if (newval === oldval || !newval) return;
    			if (cargarDatos) return;
    			
				//PreferenciaPortafolio
    			var filter = [];
				
    			filter.push({ "field": "IdPortafolio","operator": "eq", "value": newval });
				
				
				
    			if(!scope.activado){
    				dataSourcePreferenciaPortafolio.query({filter:filter}).then(function(){
    					if (muestraLog) console.log("bcs-forma-pago-mesa.js :: $watch - idPortafolio - PreferenciaPortafolio");
    					
    				scope.operaCuentaInversionCliente = 'N';
				    scope.operaCuentaInversion = false;
    					
    					$.each(dataSourcePreferenciaPortafolio.data(),function(key,value){
    						if (value.OperaCuentaInversion === 'S') {
    							scope.operaCuentaInversionCliente = 'S';
    							scope.operaCuentaInversion = true;
    						}
    					});
						if(scope.modulo === 'SPOT'){
							AsignaFormaPagoDefectoCliente(newval);
							AsignaFormaPagoDefectoCorredora();
						}
						else{
							//Formas de Pago Cliente
							scope.formaPagoCliente = {};
    					
							filter.push({ "field": "IdEstado",		"operator": "eq", "value": 'ACT' });
							
							if (scope.idTipoProducto) {
								filter.push({ "field": "IdTipoProducto","operator": "eq", "value": scope.idTipoProducto });
							}

							var orderBy = [{ field: "PorDefectoPortafolio", dir: "asc" }];

								conf.query({sort: orderBy, filter:filter}).then(function(){
									if (muestraLog) console.log("bcs-forma-pago-mesa.js :: $watch - idPortafolio - Formas de Pago Cliente");
									
									if (conf.data().length < 1){
										bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('CLIENTE_SIN_FORMA_DE_PAGO'));
									}
									
									$.each(conf.data(),function(key,value){							
										scope.formaPagoCliente[value.IdFormaDePago] = value;
									});
									scope.oConfiguracionFormaPago1.seleccionado = false; 
									scope.oConfiguracionFormaPago1.idActual = false;
									scope.oConfiguracionFormaPago2.seleccionado = false; 
									scope.oConfiguracionFormaPago2.idActual = false;
									
									if (muestraLog) console.log("bcs-forma-pago-mesa.js :: $watch - idPortafolio - invoca asignaDefecto : " + newval);
									asignaDefecto(scope.oConfiguracionFormaPago1, scope.idTipoOperacionPrincipal, scope.monedaPrincipal, 'E');
									asignaDefecto(scope.oConfiguracionFormaPago2, scope.idTipoOperacionSecundaria, scope.monedaSecundaria, 'R');
								});
						}
    					
    					
							
							
    				});
			    }else{
			    	if(scope.pagaConCtaInv=='S'){
			    		scope.operaCuentaInversion= true;
			    	}else{
			    		scope.operaCuentaInversion= false;
			    	}
			    }
				
    		});

			var AsignaFormaPagoDefectoCliente = function(portafolio){
				var filter = [];
				filter.push({ "field": "IdPortafolio","operator": "eq", "value": portafolio });
				filter.push({ "field": "IdTipoProducto","operator": "eq", "value": scope.idTipoProducto });
				filter.push({ "field": "IdTipoOperacion","operator": "eq", "value": scope.idTipoOperacionSecundaria });
				filter.push({ "field": "IdMoneda",	"operator": "eq", "value": scope.monedaSecundaria });
				filter.push({ "field": "IdEstado",		"operator": "eq", "value": 'ACT' });
				dataSourcePreferenciaPagoPortafolioCliente.query({filter:filter}).then(function(){
					if (muestraLog) console.log("bcs-forma-pago-mesa.js :: $watch - idPortafolio - Formas de Pago Cliente");
					
					if (dataSourcePreferenciaPagoPortafolioCliente.data().length < 1){
						bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('CLIENTE_SIN_FORMA_DE_PAGO'));
					}
					
					$.each(dataSourcePreferenciaPagoPortafolioCliente.data(),function(key,value){							
						scope.formaPagoCliente[value.IdPreferenciaPagoPortafolioKey] = value;
					});
					scope.oConfiguracionFormaPago2.seleccionado = false; 
					scope.oConfiguracionFormaPago2.idActual = false;
					
					if (muestraLog) console.log("bcs-forma-pago-mesa.js :: $watch - idPortafolio - invoca asignaDefecto : " + newval);
					asignaDefectoSpotCliente(scope.oConfiguracionFormaPago2, scope.idTipoOperacionSecundaria, scope.monedaSecundaria, 'R');
				});
			}

			var AsignaFormaPagoDefectoCorredora = function(){
				scope.formaPagoCliente = {};
				var filterCorredora = [];
    			filterCorredora.push({ "field": "IdPortafolio","operator": "eq", "value": scope.idPortafolioCorredora });
				filterCorredora.push({ "field": "IdTipoProducto","operator": "eq", "value": scope.idTipoProducto });
				filterCorredora.push({ "field": "IdTipoOperacion","operator": "eq", "value": scope.idTipoOperacionPrincipal });
				filterCorredora.push({ "field": "IdMoneda",	"operator": "eq", "value": scope.monedaPrincipal });
				filterCorredora.push({ "field": "IdEstado",		"operator": "eq", "value": 'ACT' });
				dataSourcePreferenciaPagoPortafolioCorredora.query({filter:filterCorredora}).then(function(){
					if (muestraLog) console.log("bcs-forma-pago-mesa.js :: $watch - idPortafolio - Formas de Pago corredora");
					
					if (dataSourcePreferenciaPagoPortafolioCorredora.data().length < 1){
						bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('CORREDORA_SIN_FORMA_DE_PAGO'));
					}
					
					$.each(dataSourcePreferenciaPagoPortafolioCorredora.data(),function(key,value){							
						scope.formaPagoCorredora[value.IdPreferenciaPagoPortafolioKey] = value;
					});
					scope.oConfiguracionFormaPago1.seleccionado = false; 
					scope.oConfiguracionFormaPago1.idActual = false;
					
					if (muestraLog) console.log("bcs-forma-pago-mesa.js :: $watch - idPortafolio - invoca asignaDefecto : " + newval);
					asignaDefectoSpotCorredora(scope.oConfiguracionFormaPago1, scope.idTipoOperacionPrincipal, scope.monedaPrincipal, 'E');
					
				});
			}

			scope.$watch('idPortafolioCorredora',function(newval, oldval){
    			if (muestraLog) console.log("bcs-forma-pago-mesa.js :: $watch - idPortafolioCorredora - newval/oldval : " + newval + "/" + oldval);
    			if (newval === oldval || !newval) return;
    			if (scope.idPortafolioCorredora){
					scope.PortafolioCorredora.value = scope.idPortafolioCorredora;
				}
				
				
    		});
    		
    		scope.$watchGroup(['idTipoOperacionPrincipal','monedaPrincipal'],function(newval,oldval){
    			if (muestraLog) console.log("bcs-forma-pago-mesa.js :: $watchGroup - idTipoOperacionPrincipal, monedaPrincipal");
    			
    			if (cargarDatos) return;
    			
    			if (scope.idTipoOperacionPrincipal && scope.monedaPrincipal && scope.idTipoProducto){
    				
					
					limpiar();
    				scope.formaDePagoClienteAuxiliar ={};
    				scope.TipoOperacion1.value = scope.idTipoOperacionPrincipal;
    				scope.TipoProducto.value = scope.idTipoProducto;
    				scope.Moneda1.value = scope.monedaPrincipal;
    				scope.oConfiguracionFormaPago1.filtros.read = true;
    			}
    		});
    		if(scope.modulo==='SPOT'){
				scope.$watchGroup(['idTipoOperacionSecundaria','monedaSecundaria','idPortafolio'],function(newval,oldval){
					if (muestraLog) console.log("bcs-forma-pago-mesa.js :: $watchGroup - idTipoOperacionSecundaria, monedaSecundaria, idPortafolio");
					
					if (cargarDatos) return;
					
					if (scope.idTipoOperacionSecundaria && scope.monedaSecundaria && scope.idTipoProducto  && scope.idPortafolio && scope.idPortafolioCorredora){
		
						
						limpiar();
						scope.formaDePagoClienteAuxiliar ={};
						scope.TipoOperacion2.value = scope.idTipoOperacionSecundaria;
						scope.TipoProducto.value = scope.idTipoProducto;
						scope.Moneda2.value = scope.monedaSecundaria; 
						scope.Portafolio.value = scope.idPortafolio;
						scope.oConfiguracionFormaPago2.filtros.read = true;
					}
				});
			}else{
				scope.$watchGroup(['idTipoOperacionSecundaria','monedaSecundaria'],function(newval,oldval){
					if (muestraLog) console.log("bcs-forma-pago-mesa.js :: $watchGroup - idTipoOperacionSecundaria, monedaSecundaria");
					
					if (cargarDatos) return;
					
					if (scope.idTipoOperacionSecundaria && scope.monedaSecundaria && scope.idTipoProducto){
		
						
						limpiar();
						scope.formaDePagoClienteAuxiliar ={};
						scope.TipoOperacion2.value = scope.idTipoOperacionSecundaria;
						scope.TipoProducto.value = scope.idTipoProducto;
						scope.Moneda2.value = scope.monedaSecundaria; 
						scope.oConfiguracionFormaPago2.filtros.read = true;
					}
				});
			}
    		
			scope.$watchGroup(['montoOperacionMonedaPrincipal','montoOperacionMonedaSecundaria'],function(newval,oldval){
				if (muestraLog) console.log("bcs-forma-pago-mesa.js :: $watchGroup - montoOperacionMonedaPrincipal, montoOperacionMonedaSecundaria");
				
				if (cargarDatos || (idOperacionAux && !soloIdOperacionMesaRF)) return;
				
				if (Object.keys(scope.formaDePagoRecibimos).length > 1 || Object.keys(scope.formaDePagoEntregamos).length > 1) {
					limpiar();
				}
				
				if(Object.keys(scope.formaDePagoRecibimos).length === 1){
    				$.each(scope.formaDePagoRecibimos,function(key,value){
    					if(scope.formaDePagoRecibimos[key] != 0){
    						scope.formaDePagoRecibimos[key] = scope.montoOperacionMonedaSecundaria;
    					}      				
        			});
    			}
    			
    			if(Object.keys(scope.formaDePagoEntregamos).length === 1){
    				$.each(scope.formaDePagoEntregamos,function(key,value){
    					if(scope.formaDePagoEntregamos[key] != 0){
    						scope.formaDePagoEntregamos[key] = scope.montoOperacionMonedaPrincipal;
    					}      				
        			});
    			}
    		});

    		//AGISNACIONES POR DEFECTO 
    		scope.$watchGroup(['idTipoOperacionPrincipal','monedaPrincipal','montoOperacionMonedaPrincipal','monedaSecundaria','montoOperacionMonedaSecundaria','fechaOperacion'],function(newval,oldval){
    			if (muestraLog) console.log("bcs-forma-pago-mesa.js :: $watchGroup - idTipoOperacionPrincipal, monedaPrincipal, montoOperacionMonedaPrincipal, monedaSecundaria, montoOperacionMonedaSecundaria");
    			
    			if (cargarDatos || (idOperacionAux && !soloIdOperacionMesaRF)) return;
    			
    			if (scope.oConfiguracionFormaPago1 && scope.idPortafolio && scope.idTipoOperacionPrincipal && (scope.monedaPrincipal || scope.monedaPrincipal === '') && scope.montoOperacionMonedaPrincipal > 0){
    				if(scope.modulo === 'SPOT'){
						AsignaFormaPagoDefectoCorredora();	
					}
					else
					{
						asignaDefecto(scope.oConfiguracionFormaPago1, scope.idTipoOperacionPrincipal,scope.monedaPrincipal,'E');
					}
					
					
    			}
    		});

    		scope.$watchGroup(['idTipoOperacionSecundaria','monedaSecundaria','montoOperacionMonedaSecundaria','monedaPrincipal','montoOperacionMonedaPrincipal','fechaOperacion'],function(newval,oldval){
    			if (muestraLog) console.log("bcs-forma-pago-mesa.js :: $watchGroup - idTipoOperacionSecundaria, monedaSecundaria, montoOperacionMonedaSecundaria, monedaPrincipal, montoOperacionMonedaPrincipal");
    			
    			if (cargarDatos || (idOperacionAux && !soloIdOperacionMesaRF)) return;
    			
    			if (scope.oConfiguracionFormaPago2 && scope.idPortafolio && scope.idTipoOperacionSecundaria && (scope.monedaSecundaria || scope.monedaSecundaria === '') && scope.montoOperacionMonedaSecundaria > 0){
    				if(scope.modulo === 'SPOT'){
						AsignaFormaPagoDefectoCliente(scope.idPortafolio);
					}
					else{
						asignaDefecto(scope.oConfiguracionFormaPago2, scope.idTipoOperacionSecundaria,scope.monedaSecundaria,'R');
					}
					
    			}
    		});
    		//************************************************************************************************************************************************************************
    		
    		scope.$watchCollection('formaDePagoRecibimos',function(newval,oldval){
    			if (muestraLog) console.log("bcs-forma-pago-mesa.js :: $watchCollection - formaDePagoRecibimos");
    			
    			if (newval === oldval) return;
    			if (Object.keys(scope.formaDePagoRecibimos).length == 0) return;
    			
    			var monto = 0;
    			var moneda = '';
			 			
    			$.each(scope.formaDePagoRecibimos,function(key,value){
    				moneda = scope.matrizFormaPagoRecibimos[key].IdMoneda;
    				if (!value || value === 0){
    					delete scope.matrizFormaPagoRecibimos[key];
						delete scope.formaDePagoRecibimos[key];
						
						if(Object.keys(scope.formaDePagoRecibimos).length === 1){
		    				$.each(scope.formaDePagoRecibimos,function(key,value){
		    					if(scope.formaDePagoRecibimos[key] != 0){
		    						scope.formaDePagoRecibimos[key] = scope.montoOperacionMonedaSecundaria;
		    					}      				
		        			});
		    			}
    				}
    				monto += value;
    			});
    			
    			scope.montoAsignadoRecibimos = monto;
    			scope.monedaAsignadaRecibimos = moneda;
    			
    			if (muestraLog) console.log("bcs-forma-pago-mesa.js :: $watchCollection - formaDePagoRecibimos - moneda : " + moneda);
    			if (moneda) {
    				scope.cantDecmonedaAsignadaRecibimos = util.bcs_formato(0,scope.idUsoMonedaFormato,'Monto',moneda,'','').cantidadDecimales;
    			}
    			
    			if (cargarDatos) {
    				$timeout(function(){
    					cargarDatos = false;
    					idOperacionAux = 0;
    					if (muestraLog) console.log("bcs-forma-pago-mesa.js :: $watchCollection - formaDePagoRecibimos - idOperacion : " + idOperacionAux);
					}, 2000);
    			}
    		});
    		
    		scope.$watchCollection('formaDePagoEntregamos',function(newval,oldval){
    			if (muestraLog) console.log("bcs-forma-pago-mesa.js :: $watchCollection - formaDePagoEntregamos");
    			
    			if (newval === oldval) return;
    			if (Object.keys(scope.formaDePagoEntregamos).length == 0) return;
    			
    			var monto = 0;
    			var moneda = '';
    			
    			$.each(scope.formaDePagoEntregamos,function(key,value){
    				moneda = scope.matrizFormaPagoEntregamos[key].IdMoneda;
    				if (!value || value === 0){
    					delete scope.matrizFormaPagoEntregamos[key];
						delete scope.formaDePagoEntregamos[key];
						
						if(Object.keys(scope.formaDePagoEntregamos).length === 1){
		    				$.each(scope.formaDePagoEntregamos,function(key,value){
		    					if(scope.formaDePagoEntregamos[key] != 0){
		    						scope.formaDePagoEntregamos[key] = scope.montoOperacionMonedaPrincipal;
		    					}      				
		        			});
		    			}
    				}
    				monto += value;
    			});
    			
    			scope.montoAsignadoEntregamos = monto;
    			scope.monedaAsignadaEntregamos = moneda;
    			
    			if (muestraLog) console.log("bcs-forma-pago-mesa.js :: $watchCollection - formaDePagoEntregamos - moneda : " + moneda);
    			if (moneda) {
    				scope.cantDecmonedaAsignadaEntregamos = util.bcs_formato(0,scope.idUsoMonedaFormato,'Monto',moneda,'','').cantidadDecimales;
    			}
    			
    			if (cargarDatos) {
    				$timeout(function(){
    					cargarDatos = false;
    					idOperacionAux = 0;
    					if (muestraLog) console.log("bcs-forma-pago-mesa.js :: $watchCollection - formaDePagoEntregamos - idOperacion : " + idOperacionAux);
					}, 2000);
    			}
    		});
    		
    		scope.$watch('oConfiguracionFormaPago1.seleccionado',function(newval,oldval){
    			if (muestraLog) console.log("bcs-forma-pago-mesa.js :: $watch - oConfiguracionFormaPago1.seleccionado - newval/oldval : " + newval.IdFormaDePago + "/" + oldval.IdFormaDePago);
    			
    			if (newval === oldval) return;
    			if (idOperacionAux && !soloIdOperacionMesaRF) return;
    			
    			if (scope.oConfiguracionFormaPago1.seleccionado){
    				if (!cargarDatos) {
    					if (muestraLog) console.log("bcs-forma-pago-mesa.js :: $watch - oConfiguracionFormaPago1.seleccionado - scope.Agregar : " + newval.IdFormaDePago);
    					if(scope.modulo === 'SPOT'){
							scope.AgregarFPSpot(scope.oConfiguracionFormaPago1);
						}
						else{
							//console.log("---SE CREA MATRIZ---");
							$timeout(function(){								
								scope.Agregar(scope.oConfiguracionFormaPago1);
							},2000);
						}
						
    				}
    			}
    		});
    		
    		scope.$watch('oConfiguracionFormaPago2.seleccionado',function(newval,oldval){
    			if (muestraLog) console.log("bcs-forma-pago-mesa.js :: $watch - oConfiguracionFormaPago2.seleccionado - newval/oldval : " + newval.IdFormaDePago + "/" + oldval.IdFormaDePago);
    			
    			if (newval === oldval) return;
    			if (idOperacionAux && !soloIdOperacionMesaRF) return;
    			
    			if (scope.oConfiguracionFormaPago2.seleccionado){
    				if (!cargarDatos) {
    					if (muestraLog) console.log("bcs-forma-pago-mesa.js :: $watch - oConfiguracionFormaPago2.seleccionado - scope.Agregar : " + newval.IdFormaDePago);
    					if(scope.modulo === 'SPOT'){
							scope.AgregarFPSpot(scope.oConfiguracionFormaPago2);
						}
						else{
							//console.log("---SE CREA MATRIZ FORMA PAGO 2---");
							$timeout(function(){							
								scope.Agregar(scope.oConfiguracionFormaPago2);
							},2000);
						}
    				}
    			}
    		});
    		
    		scope.$watch('fechaOperacion',function(newval,oldval){
    			if (muestraLog) console.log("bcs-forma-pago-mesa.js :: $watch - fechaOperacion - newval / oldval : " + newval + " / " + oldval);
    			if (cargarDatos || (idOperacionAux && !soloIdOperacionMesaRF)) return;
    			
    			if (!newval) return;
    			if (angular.isDate(newval) && angular.isDate(oldval) && util.dateSPdate(newval) === util.dateSPdate(oldval)) return;
    			
				scope.cambioPlazo = true;
				scope.formaDePagoClienteAuxiliar= {};
				scope.formaDePagoMesaAuxiliar ={};
				//este if cubre solo si es compensada o paga o recibe
				if(scope.idTipoOperacionPrincipal === 'COMFWD'){
					scope.formaDePagoClienteAuxiliar = scope.formaDePagoRecibimos;
					//si recibe y paga === true es entrega fisica
					if(scope.Paga && scope.Recibe){
						scope.formaDePagoMesaAuxiliar = scope.formaDePagoEntregamos;
					}else{
						scope.formaDePagoMesaAuxiliar ={}
					}
				}else{
					scope.formaDePagoClienteAuxiliar = scope.formaDePagoEntregamos;
					if(scope.Paga && scope.Recibe){
						scope.formaDePagoMesaAuxiliar = scope.formaDePagoRecibimos;
					}else{
						scope.formaDePagoMesaAuxiliar = {}
					}
				}
				
				limpiar();
    		});
    		
    		scope.$watch('idTipoProducto',function(newval,oldval){
    			if (muestraLog) console.log("bcs-forma-pago-mesa.js :: $watch - idTipoProducto");
    			
    			if (!newval) return;
    			if (cargarDatos) return;
    			
				scope.idUsoMonedaFormato = scope.idTipoProducto === 'MEME' ? 'SPT' : scope.idTipoProducto === 'MEFW' ? 'FWD' : scope.idUsoMonedaFormato;
				
				var filter = [
    				{"field": "IdTipoProducto",	"operator": "eq", "value": scope.idTipoProducto},
    				{"field": "IdEstado",		"operator": "eq", "value": 'ACT'},
    				{"field": "IdEmpresa",		"operator": "eq", "value": $rootScope.SesionIdEmpresa}
    			];
				var ordenar = [{ field: "PorDefecto", dir: "desc" },
	         		   { field: "IdMedioDePago", dir: "asc" },
	         		   { field: "Dias", dir: "asc" }];
	         		   
    			confMes.query({sort:ordenar, filter:filter}).then(function(){
    				confMesAux = confMes.data();
    				
					if (confMesAux.length < 1){
						bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('MESA_SIN_FORMA_DE_PAGO'));
						return;
					}
					$.each(confMesAux,function(key,value){
						if (value.IdTipoOperacion === scope.idTipoOperacionPrincipal || value.IdTipoOperacion === scope.idTipoOperacionSecundaria)
							scope.formaPagoMesa[value.IdFormaDePago] = value;
							if(scope.modulo === 'SPOT'){
								scope.formaPagoMesaSpot[value.IdPreferenciaPago+"_"+value.Dias] = value;
							}
							
					}); 
    			});
    		});
    		
    		scope.$watchGroup(['idOperacion','idTipoProducto','cargaPortafolio'],function(newval,oldval){
    			if (muestraLog) console.log("bcs-forma-pago-mesa.js :: $watchGroup - idOperacion / idTipoProducto : " + scope.idOperacion + " / " + scope.idTipoProducto);
    			
    			if (!(scope.idOperacion && scope.idTipoProducto)) return;
    			
    			idOperacionAux = scope.idOperacion;
    			scope.idUsoMonedaFormato = scope.idTipoProducto === 'MEME' ? 'SPT' : scope.idTipoProducto === 'MEFW' ? 'FWD' : scope.idUsoMonedaFormato;
				var filtros = bcsServiciosGrillasMesaRF.validarFiltros({"IdEmpresa": $rootScope.SesionIdEmpresa, "IdOperacion": idOperacionAux, "IdTipoProducto": scope.idTipoProducto}, 'OPERACION');
				dataSourceDetalleComprobantePagoOperacion.query({filter : filtros}).then(function(){
					$timeout(function(){
						var data = dataSourceDetalleComprobantePagoOperacion.data();
						scope.matrizFormaPagoExist = [];
						for(i=0; i<data.length; i++){
							data[i].cargarDatos = true;
							scope.matrizFormaPagoExist.push(data[i]);
						}
						
						if (scope.matrizFormaPagoExist.length < 1) {
							soloIdOperacionMesaRF = scope.idTipoProducto === 'MERF' ? true : false;
							return;
						}
						
						if (scope.oConfiguracionFormaPago1 && scope.matrizFormaPagoExist[0].RecibePaga ==='R'){
							scope.oConfiguracionFormaPago1.idDefecto = scope.matrizFormaPagoExist[0].IdFormaDePago;
						}
						if (scope.oConfiguracionFormaPago2 && scope.matrizFormaPagoExist[0].RecibePaga ==='P'){
							scope.oConfiguracionFormaPago2.idDefecto = '';
						}
						
						var datos = scope.matrizFormaPagoExist;
						var datoDia = {
							p_IdPais	: $rootScope.SesionIdPaisSistema,
							p_DiaHabil	: 'S',
							p_FechasIN	: []
						};


						if(scope.opcionVentana != undefined && (scope.opcionVentana !== 'MODIFICAR' && scope.opcionVentana !== 'ANULAR' && 
							scope.opcionVentana !== 'CONSULTAR'))
						{
							for(i=0;i < datos.length;i++){
								datoDia.p_FechasIN.push({
									"FechaInicial"	: angular.isDate(scope.fechaOperacion)? util.dateSPdate(scope.fechaOperacion): $rootScope.SesionFechaSistema,
									"NumeroDias"	: datos[i].Dias ? datos[i].Dias : 0
								});
							}
							bcsServicioPlataforma.Servicios().ConsultarDiaSiguienteMasivo(datoDia).$promise.then(function(result){
								if (result.P_FECHASOUTF.length > 0) {
									scope.matrizFormaPagoEntregamos = {};
	    							scope.matrizFormaPagoRecibimos = {};
	    							
									for(i=0;i < datos.length;i++){
										for(j=0;j < result.P_FECHASOUTF.length;j++){
											if (datos[i].Dias === result.P_FECHASOUTF[j].NumeroDias){
												datos[i].FechaLiquidacion = util.dateInicioDia(result.P_FECHASOUTF[j].FechaCalculada);
											}
										}
										
										if (datos[i].RecibePagaPortafolio === 'R') {
											scope.Paga = true;
											if(!datos[i].cargarDatos){
																			
												datos[i].Monto = 0;
												if(scope.montoAsignadoEntregamos === scope.montoOperacionMonedaPrincipal){
													datos[i].Monto = scope.montoOperacionMonedaPrincipal;
												}
												else if(scope.montoAsignadoEntregamos < scope.montoOperacionMonedaPrincipal){
													datos[i].Monto = scope.montoOperacionMonedaPrincipal - scope.montoAsignadoEntregamos;
												}
											}
											scope.matrizFormaPagoEntregamos[datos[i].IdFormaDePago] = datos[i];
											scope.formaDePagoEntregamos[datos[i].IdFormaDePago] = datos[i].Monto;
										}
										if(datos[i].RecibePagaPortafolio === 'P') {
											scope.Recibe = true;
											if (!datos[i].cargarDatos){
												
												datos[i].Monto = 0;
												if(scope.montoAsignadoRecibimos === scope.montoOperacionMonedaSecundaria){
													datos[i].Monto = scope.montoOperacionMonedaSecundaria;
												}
												else if(scope.montoAsignadoRecibimos < scope.montoOperacionMonedaSecundaria){
													datos[i].Monto = scope.montoOperacionMonedaSecundaria-scope.montoAsignadoRecibimos;
												}
											}
											scope.matrizFormaPagoRecibimos[datos[i].IdFormaDePago] = datos[i];
											scope.formaDePagoRecibimos[datos[i].IdFormaDePago] = datos[i].Monto;
										}
									}
									
									cargarDatos = true;
									if (muestraLog) console.log("bcs-forma-pago-mesa.js :: $watchGroup - idOperacion, idTipoProducto - termina setear formas de pago : " + cargarDatos);
								}
								else{
									bcsComunes.msg("warning",$translate.instant("UI_ERROR"),$translate.instant('UI_ERROR_AL_OBTENER_FECHA_SIGUIENTE_HABIL'));
								}
								
								scope.matrizFormaPagoExist = null;
								scope.oConfiguracionFormaPago1.seleccionado = false;
								scope.oConfiguracionFormaPago1.idActual = false;
								scope.oConfiguracionFormaPago1.itemPostFiltro = false;
								scope.oConfiguracionFormaPago1.idPostFiltro = false;
								scope.oConfiguracionFormaPago2.seleccionado = false;
								scope.oConfiguracionFormaPago2.idActual = false;
								scope.oConfiguracionFormaPago2.itemPostFiltro = false;
								scope.oConfiguracionFormaPago2.idPostFiltro = false;
								
							},function(result){
								bcsComunes.msg("warning",$translate.instant("UI_ERROR"),$translate.instant('UI_ERROR_AL_OBTENER_FECHA_SIGUIENTE_HABIL'));
							});
						}
						else{
							scope.matrizFormaPagoEntregamos = {};
							scope.matrizFormaPagoRecibimos = {};
							
							
							for(i=0; i<data.length; i++){
								data[i].cargarDatos = true;
								scope.matrizFormaPagoExist.push(data[i]);
							}
							for(i=0;i < datos.length;i++){
								var fechaExtraida = angular.isDate(datos[i].FechaLiquidacionComprobantePagoOperacion)? util.dateSPdate(datos[i].FechaLiquidacionComprobantePagoOperacion): $rootScope.SesionFechaSistema;
								datos[i].FechaLiquidacion = util.dateInicioDia(fechaExtraida);
								
								if (datos[i].RecibePagaPortafolio === 'R' ) {
									scope.Paga = true;
									if(!datos[i].cargarDatos){
																	
										datos[i].Monto = 0;
										if(scope.montoAsignadoEntregamos === scope.montoOperacionMonedaPrincipal){
											datos[i].Monto = scope.montoOperacionMonedaPrincipal;
										}
										else if(scope.montoAsignadoEntregamos < scope.montoOperacionMonedaPrincipal){
											datos[i].Monto = scope.montoOperacionMonedaPrincipal - scope.montoAsignadoEntregamos;
										}
									}
									if(scope.modulo ==='SPOT' && scope.cargaPortafolio === true){
										scope.matrizFormaPagoEntregamos[datos[i].IdPreferenciaPagoPortafolio+'-'+datos[i].Dias] = datos[i];
										scope.formaDePagoEntregamos[datos[i].IdPreferenciaPagoPortafolio+'-'+datos[i].Dias] = datos[i].Monto;
									}
									else{
										scope.matrizFormaPagoEntregamos[datos[i].IdFormaDePago] = datos[i];
										scope.formaDePagoEntregamos[datos[i].IdFormaDePago] = datos[i].Monto;
									}
									
								}
								if(datos[i].RecibePagaPortafolio === 'P' ) {
									scope.Recibe = true;
									if (!datos[i].cargarDatos){
										
										datos[i].Monto = 0;
										if(scope.montoAsignadoRecibimos === scope.montoOperacionMonedaSecundaria){
											datos[i].Monto = scope.montoOperacionMonedaSecundaria;
										}
										else if(scope.montoAsignadoRecibimos < scope.montoOperacionMonedaSecundaria){
											datos[i].Monto = scope.montoOperacionMonedaSecundaria-scope.montoAsignadoRecibimos;
										}
									}
									if(scope.modulo ==='SPOT' && scope.cargaPortafolio === true){
										scope.matrizFormaPagoRecibimos[datos[i].IdPreferenciaPagoPortafolio+'-'+datos[i].Dias + '-'+datos[i].IdPreferenciaPago] = datos[i];
										scope.formaDePagoRecibimos[datos[i].IdPreferenciaPagoPortafolio+'-'+datos[i].Dias + '-'+datos[i].IdPreferenciaPago] = datos[i].Monto;
									}
									else{
										scope.matrizFormaPagoRecibimos[datos[i].IdFormaDePago] = datos[i];
										scope.formaDePagoRecibimos[datos[i].IdFormaDePago] = datos[i].Monto;
									}
								}
							}
							cargarDatos = true;
							if (muestraLog) console.log("bcs-forma-pago-mesa.js :: $watchGroup - idOperacion, idTipoProducto - termina setear formas de pago : " + cargarDatos);
							
							
							scope.matrizFormaPagoExist = null;
							scope.oConfiguracionFormaPago1.seleccionado = false;
							scope.oConfiguracionFormaPago1.idActual = false;
							scope.oConfiguracionFormaPago1.itemPostFiltro = false;
							scope.oConfiguracionFormaPago1.idPostFiltro = false;
							scope.oConfiguracionFormaPago2.seleccionado = false;
							scope.oConfiguracionFormaPago2.idActual = false;
							scope.oConfiguracionFormaPago2.itemPostFiltro = false;
							scope.oConfiguracionFormaPago2.idPostFiltro = false;
						}
					},3000);
				});
    		});
    		
    		limpiar();
    	}
	}
});