angular.module('ModuloPlataforma').directive('bcsPreferenciasCliente',
function($translate,bcsComunes,$resource, bcsServicioPlataforma){
	return {
		restrict: 'E',
    	replace: false, 
		scope : {
			modeloCliente 	: '=?',
			tipoEntidad	 	: '=?',
			idEntidad		: '=?',
			idPersona		: '=?',
			codigoFormulario: '=?'
		},
		templateUrl: '../../Plataforma/UI/directives/bcs-preferencias/bcs-preferencias-cliente.html',
    	link: function (scope, element, attrs, fn) {
    		
			/*********************************************
			 * VARIABLES Y CONTEXTOS
			 ********************************************/
    		scope.idVentana 		= bcsComunes.ventanaWinId(scope); // bcsComunes.ventanaOverlay(scope.idVentana,true);
			scope.win 				= scope.win	|| {};
			scope.win.modoLectura 	= true;
			scope.win.uiPortafolio 	= scope.idEntidad && scope.idPersona ? true : false;
			
			scope.cmbbx				= scope.cmbbx 	|| {};
			scope.ds				= scope.ds 		|| {};
			scope.grilla			= scope.grilla 	|| {};
			scope.fn				= scope.fn 		|| {};
			
			scope.datosAntes 		= {};
			scope.datosDespues 		= {};
			scope.definicionIndicadores = {};

			scope.camposRequeridos = {};
			var idVentana = bcsComunes.ventanaWinId(scope)
			
			/*********************************************
			 * CHECK INICIAL DE VARIABLES
			 ********************************************/
			
    		if(scope.modeloCliente 	=== undefined) scope.modeloCliente=0;
			if(scope.idEntidad 		=== undefined) scope.idEntidad=0;
			if(scope.tipoEntidad 	=== undefined) scope.tipoEntidad='PORTAFOLIO';
			
			
			/*********************************************
			 * ACCIONES DE PANTALLA
			 ********************************************/					
			
			scope.win.editar = function() {
				scope.win.modoLectura = false;
			};

			scope.win.guardar = function() {
				var datosGuardar = scope.fn.preparaDatosGuardar();
				if(datosGuardar !== undefined) {

					bcsComunes.ventanaOverlay(scope.idVentana,true);
					modificarPreferenciaSP(datosGuardar).then(function(result){
						bcsComunes.ventanaOverlay(scope.idVentana,false);
						var titulo = "";
						if(result.P_OUT_ERRORESCONTADOR != undefined && result.P_OUT_ERRORESCONTADOR == 0){
							titulo = $translate.instant("UI_MODIFICAR_PREFERENCIAS");
						}
						else {
							titulo = "ERROR" + $translate.instant("UI_ERROR_MODIFICAR_PREFERENCIAS");
						}
						
						bcsComunes.ventanaMsg({
							mensaje: titulo,
							conflictos:util.parseServerResponse(result)
						},scope.idVentana);
						
						scope.win.cancelar(); 
						scope.fn.cargaDatosPantalla();
			    	},function(error){
			    		bcsComunes.msg("error","ERROR",$translate.instant("UI_ERROR_MODIFICAR_PREFERENCIAS"));
			    		console.log(error);
			    		bcsComunes.ventanaOverlay(scope.idVentana,false);
			    	});
				}
				else {
					console.log("No hay datos");
				}	
			};
			
			scope.win.cancelar = function() {
				scope.win.modoLectura	= true;
				scope.datosDespues 		= _.clone(scope.datosAntes);
				emitir('CambiosOk');
			};
			

			/*********************************************
			 * DATASOURCES
			 *********************************************/
			
			
			/*********************************************
			 * COMBOBOX
			 *********************************************/

			scope.cmbbx.oConfiguracionPortafolio = {
					filtros : { 
						IdCliente: { field:"IdCliente", operator:"eq", value: scope.idEntidad },
						IdProductoAgrupador: { field:"IdProductoAgrupador", operator:"eq", value: 9999}
					}
				};
 
			
			/*********************************************
			 * GRILLAS
			 *********************************************/
			
			
			/*********************************************
			 * FUNCIONES
			 *********************************************/
			
			scope.fn.cargaDatosPantalla = function(){
				if(scope.modeloCliente || (scope.tipoEntidad && scope.idEntidad)){
					var parametrosSP = {
						p_IdEmpresa				: scope.$root.SesionIdEmpresa,
				        p_IdEntidad				: scope.idEntidad || 0,
				        p_IdTipoEntidad			: scope.tipoEntidad || '',
				        p_CodigoTipoIndicador	: '',
				        p_GrupoIndicador		: '',
				        p_ModeloCliente			: scope.modeloCliente || '',
				        p_IdIndicador			: 0
					};
					
					bcsComunes.ventanaOverlay(scope.idVentana,true);
					consultaIndicadoresSP(parametrosSP).then(function(result) {
						bcsComunes.ventanaOverlay(scope.idVentana,false);
		        		if(result && result.P_OUT_INDICADORES){
		        			$.each(result.P_OUT_INDICADORES,function(key,value){
		        				scope.definicionIndicadores[value.GrupoIndicador + "//" +value.CodigoTipoIndicador] = value;
		        				
		        			
		        			    if(value.ClaseDato.toUpperCase() == 'CHECKBOX')
		        			    {
				        	 		if(value.ValorIndicador == "S")
				        	 			scope.definicionIndicadores[value.GrupoIndicador + "//" +value.CodigoTipoIndicador].ValorIndicador = true;
				        	 		else
				        	 			scope.definicionIndicadores[value.GrupoIndicador + "//" +value.CodigoTipoIndicador].ValorIndicador = false;
				        	 	}		
				        	 	
				        	 		
		        			});
		        			//Cuenta BlockChain, Id del Portafolio que recibira un prestamo proveniente de BlockChain
		        			scope.datosAntes['GENERALES//BLOCKCHAIN_PORTAFOLIO_PRESTAMOS'] = scope.definicionIndicadores['GENERALES//BLOCKCHAIN_PORTAFOLIO_PRESTAMOS'].ValorIndicador;
		        			scope.datosAntes['GENERALES//PORTAFOLIO_DERIVADOS'] = scope.definicionIndicadores['GENERALES//PORTAFOLIO_DERIVADOS'].ValorIndicador;

		        		}
						scope.datosDespues = _.clone(scope.datosAntes);
						//scope.win.directivaCargada = true;
		        	},function(result){
		        		bcsComunes.ventanaMsg({
							mensaje:'',
							conflictos:util.parseServerResponse(respuesta)
						},scope.idVentana);
		        		bcsComunes.ventanaOverlay(scope.idVentana,false);
		        	});
					
				}
			};
			
			var consultaIndicadoresSP = function(p_datos){
				var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=275";
				var r			= $resource("", {}, { 'urlConsulta': { method: "POST", url: urlConsulta} });
				var post		= r.urlConsulta(p_datos);
				
				return post.$promise;
		    };
			
			
			scope.fn.preparaDatosGuardar = function(){
				
				var datosGuardar = {};
				datosGuardar.p_IdEmpresa 		= scope.$root.SesionIdEmpresa;
				datosGuardar.p_IdUsuario 		= scope.$root.SesionIdUsuario;
				datosGuardar.p_IdModulo 		= 'PLA';
				datosGuardar.p_UsaReglasNegocio	= 'N';
				datosGuardar.p_IdTipoEntidad	= scope.tipoEntidad || '';
				datosGuardar.p_IdEntidad		= scope.idEntidad || 0;
				
				datosGuardar.p_RegistroAtributo	= [];
				
				$.each(scope.datosDespues,function(key){
					var indicador = scope.definicionIndicadores[key];
					if(indicador){
						IdIndicadorzz = indicador.IdIndicador;
						IdTipoIndicadorzz = indicador.IdTipoIndicador;
						
						
						if (indicador.ClaseDato === "COMBO") {
							ValorIndicadorzz = (scope.datosDespues[indicador.GrupoIndicador + "//" +indicador.CodigoTipoIndicador])?scope.datosDespues[indicador.GrupoIndicador + "//" +indicador.CodigoTipoIndicador] : null;
						}
						else if (indicador.ClaseDato === "MULTI") {
							ValorIndicadorzz = (scope.datosDespues[indicador.GrupoIndicador + "//" +indicador.CodigoTipoIndicador])?JSON.stringify(scope.datosDespues[indicador.GrupoIndicador + "//" +indicador.CodigoTipoIndicador]) : null;
						}
						else {
							ValorIndicadorzz = scope.datosDespues[indicador.GrupoIndicador + "//" +indicador.CodigoTipoIndicador];
						}
						
						datosGuardar.p_RegistroAtributo.push({IdIndicador:IdIndicadorzz, IdTipoIndicador:IdTipoIndicadorzz, ValorIndicador: ValorIndicadorzz});
					}
				});
				
				
				$.each(scope.definicionIndicadores,function(key){
					var indicador = scope.definicionIndicadores[key];
					if(indicador){
						IdIndicador     = indicador.IdIndicador;
						IdTipoIndicador = indicador.IdTipoIndicador;
						
						if (indicador.ClaseDato === "CHECKBOX") {
							ValorIndicador = (scope.definicionIndicadores[key].ValorIndicador === true) ? 'S' : 'N';
							
							datosGuardar.p_RegistroAtributo.push({IdIndicador:IdIndicador, IdTipoIndicador:IdTipoIndicador, ValorIndicador: ValorIndicador});
						}						
						
					}
				});
				
				
				datosGuardar.p_ModeloCliente = scope.modeloCliente;
				
				return datosGuardar;
			};
		    
		    var modificarPreferenciaSP = function(p_datos){
				var urlModificarPreferencia = "/BSCL/Plataforma/GWJAVA/PLA/call?t=280";
				var r = $resource("", {}, {'urlModificarPreferencia': {method:"POST", url:urlModificarPreferencia}});
				var post = r.urlModificarPreferencia(p_datos);
				
				return post.$promise;
			};

			let emitir = function (tipoCambio) {
				scope.$emit(tipoCambio, { source: "Preferencias" });	
			}
			let avisarCambioPanel = function () {
				const dataOriginal = obtenerDatosComparables(scope.datosAntes)
				const data = obtenerDatosComparables(scope.datosDespues)
				
				const sonIguales = _.isEqual(data, dataOriginal);
				if (sonIguales) {
					emitir('CambiosOk');
				} else {
					emitir('CambiosPendientes');
				}
			};
			let obtenerDatosComparables = function (data) {
				return {
					"GENERALES//BLOCKCHAIN_PORTAFOLIO_PRESTAMOS": data["GENERALES//BLOCKCHAIN_PORTAFOLIO_PRESTAMOS"] || '',
					"GENERALES//PORTAFOLIO_DERIVADOS": data["GENERALES//BLOCKCHAIN_PORTAFOLIO_PRESTAMOS"] || '',
				}
			};
			
			/*********************************************
			 * FUNCIONES WATCH
			 *********************************************/
			scope.$watch('modeloCliente',function(newval,oldval){
				if(newval !== undefined && newval!==oldval){
					scope.fn.cargaDatosPantalla();
					scope.win.modoLectura = true;
				}
				else {
					//scope.win.directivaCargada = false;
				}
				bcsComunes.ventanaAutoHeight(idVentana);
			});

			scope.$watch('scope.codigoFormulario', function () {
				if (scope.codigoFormulario) {
					bcsServicioPlataforma.CamposRequeridosFormulario(scope.codigoFormulario, function(respuesta) {
						scope.camposRequeridos = respuesta;
					});
				}
			});

			scope.$watchCollection('datosDespues', function () {
				const modoEdicion = !scope.win.modoLectura
				if (modoEdicion) {
					avisarCambioPanel();
				}
			});

			scope.$watch('definicionIndicadores', function () {
				const modoEdicion = !scope.win.modoLectura
				if (modoEdicion) {
					emitir('CambiosPendientes');
				}
			},true);


			/*********************************************
			 * GO!!!
			 **********************************************/
			if((scope.modeloCliente>0 && scope.idEntidad > 0) || (scope.modeloCliente===0 && scope.idEntidad===0)) {
				throw "directiva mal cargada";
			}
			else {
				scope.win.validForm = true;
				scope.fn.cargaDatosPantalla();
			}
			
    	} 
	}
});