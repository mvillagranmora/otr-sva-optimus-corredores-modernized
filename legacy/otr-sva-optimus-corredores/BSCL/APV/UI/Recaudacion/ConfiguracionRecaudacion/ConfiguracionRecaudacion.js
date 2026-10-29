(function() {
	angular.module('ModuloAPV').controller('ConfiguracionRecaudacionCtrl',
	function($scope,$translate,$resource,$timeout,bcsComunes,bcsConsultasAPV,bcsServiciosAPV)
	{
		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var parametrosVentana = bcsComunes.ventanaParams(idVentana);		
		
		var cnfgRec = this;
		cnfgRec.habilitaBancoOrigen = true;
		var contador = 0;
		$scope.data = {};
				
		cnfgRec.proData = {};
		
		cnfgRec.oConfiguracionProceso={
			tipo : "combobox-proceso-origen-destino",
			configuracion : {},
			filtros : {
				IdEmpresa  : { "field"   : "IdEmpresa", "operator": "eq", "value"   : $scope.SesionIdEmpresa },
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};	
		
		cnfgRec.oConfiguracionCuentaOrigen={
			tipo : "combobox-cuentas-bancarias-corredora",
			configuracion : {},
			filtros : {
				IdEstado  		: { "field"	: "IdEstado"		, "operator": "eq", "value"	: "ACT" },
				IdMoneda  		: { "field"	: "IdMoneda"		, "operator": "eq", "value"	: "CLP" }, // Cuentas de la misma moneda del comprobante
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};	
		
		cnfgRec.oConfiguracionCuentaDestino={
			tipo : "combobox-cuentas-bancarias-corredora",
			configuracion : {},
			filtros : {
				IdEstado  		: { "field"	: "IdEstado"		, "operator": "eq", "value" : "ACT" },
				IdMoneda  		: { "field" : "IdMoneda"		, "operator": "eq", "value" : "CLP" }, // Cuentas de la misma moneda del comprobante
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};	
		
		cnfgRec.oConfiguracionCuentaPuente={
			tipo : "combobox-cuentas-bancarias-socio-negocio-corredora",
			configuracion : {},
			filtros : {
				IdEstado			: { "field" : "IdEstado"		, "operator": "eq", "value" : "ACT" 					},
				IdEmpresa			: { "field" : "IdEmpresa"		, "operator": "eq", "value" : $scope.SesionIdEmpresa	},
				IdMoneda  			: { "field" : "IdMoneda"		, "operator": "eq", "value" : "CLP"  					},
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};	
			
		cnfgRec.oConfiguracionEstado={
			tipo : "combobox-estado-apv",
			configuracion : {},
			filtros : {
				NombreEntidad : {field: "NombreEntidad", operator: "eq", value: 'CONFIGURACION CUENTA BANCO'},
			},
		idActual : false,
			seleccionado : false,
			idDefecto : ''
		};	
		
		$scope.$watch('cnfgRec.proceso',function(newval, oldval){
			
			if(cnfgRec.procesoSeleccionado !== undefined && cnfgRec.procesoSeleccionado !== false){
				if(cnfgRec.procesoSeleccionado.HabilitaCuentaOrigen === 'N'){
					cnfgRec.habilitaBancoOrigen = false;
					cnfgRec.cuentaOrigen = false;
					cnfgRec.cuentaOrigenPost = undefined; 
					cnfgRec.bancoOrigen = "";
					cnfgRec.numeroCuentaOrigen = "";
				}
				else{
					cnfgRec.habilitaBancoOrigen = true;
					cnfgRec.cuentaOrigen		= cnfgRec.procesoSeleccionado.OrigenCtaBco;
					cnfgRec.cuentaOrigenPost	= cnfgRec.procesoSeleccionado.OrigenCtaBco;
				}
				
				cnfgRec.mueveCuentaInversion = cnfgRec.procesoSeleccionado.MueveCuentaInversion === 'S' ? true : false;
				cnfgRec.cuentaDestino		= cnfgRec.procesoSeleccionado.DestinoCtaBco;
				cnfgRec.cuentaPuente		= cnfgRec.procesoSeleccionado.SocioNegocioCtaBco;
				cnfgRec.cuentaDestinoPost	= cnfgRec.procesoSeleccionado.DestinoCtaBco;
				cnfgRec.cuentaPuentePost	= cnfgRec.procesoSeleccionado.SocioNegocioCtaBco;
				cnfgRec.estado				= cnfgRec.procesoSeleccionado.IdEstado;
			}	
		});
		
		$scope.$watch('cnfgRec.cuentaOrigen',function(newval, oldval){
			if(cnfgRec.cuentaOrigenSeleccionada !== undefined){// && cnfgRec.cuentaOrigen !== undefined){
				if(cnfgRec.procesoSeleccionado !== undefined && cnfgRec.procesoSeleccionado !== false){
					if(cnfgRec.procesoSeleccionado.HabilitaCuentaOrigen === 'N'){
						cnfgRec.bancoOrigen = "";
						cnfgRec.numeroCuentaOrigen = "";
					}else{
						cnfgRec.bancoOrigen = cnfgRec.cuentaOrigenSeleccionada.NombreBanco;
						cnfgRec.numeroCuentaOrigen = cnfgRec.cuentaOrigenSeleccionada.NumeroCuentaBancaria;
					}
				}
			}else{
				cnfgRec.bancoOrigen = "";
				cnfgRec.numeroCuentaOrigen = "";
			}
		});

		$scope.$watch('cnfgRec.cuentaDestino',function(newval, oldval){
			if(cnfgRec.cuentaDestinoSeleccionada !== undefined ){//&& cnfgRec.cuentaDestino !== undefined){
				cnfgRec.bancoDestino = cnfgRec.cuentaDestinoSeleccionada.NombreBanco;
				cnfgRec.numeroCuentaDestino = cnfgRec.cuentaDestinoSeleccionada.NumeroCuentaBancaria;
			}	
		});
		
		$scope.$watch('cnfgRec.cuentaPuente',function(newval, oldval){
			if(cnfgRec.cuentaPuenteSeleccionada !== undefined ){//&& cnfgRec.cuentaPuente !== undefined){
				cnfgRec.bancoPuente = cnfgRec.cuentaPuenteSeleccionada.NombreBanco;
				cnfgRec.numeroCuentaPuente = cnfgRec.cuentaPuenteSeleccionada.NumeroCuentaBancaria;
			}	
		});
		
		cnfgRec.GrabarConfiguracion = function(){
			bcsComunes.ventanaOverlay(idVentana,true);
			
			if(cnfgRec.procesoSeleccionado !== undefined && cnfgRec.procesoSeleccionado !== false){
				if(cnfgRec.procesoSeleccionado.HabilitaCuentaOrigen === 'S'){
					if(cnfgRec.cuentaOrigenSeleccionada === undefined || cnfgRec.cuentaOrigenSeleccionada === false){
						bcsComunes.msg("error","Error",$translate.instant("UI_SELECCIONE_CUENTA_BANCARIA_ORIGEN"));
						bcsComunes.ventanaOverlay(idVentana,false);
						return;
					}
				}
				if(cnfgRec.cuentaDestinoSeleccionada === undefined || cnfgRec.cuentaDestinoSeleccionada === false){
					bcsComunes.msg("error","Error",$translate.instant("UI_SELECCIONE_CUENTA_BANCARIA_DESTINO"));
					bcsComunes.ventanaOverlay(idVentana,false);
					return;
				}
				if(cnfgRec.cuentaPuenteSeleccionada === undefined || cnfgRec.cuentaPuenteSeleccionada === false){
					bcsComunes.msg("error","Error",$translate.instant("UI_SELECCIONE_CUENTA_BANCARIA_PUENTE"));
					bcsComunes.ventanaOverlay(idVentana,false);
					return;
				}
			}else{
				bcsComunes.msg("error","Error",$translate.instant("UI_SELECCIONE_PROCESO"));
				bcsComunes.ventanaOverlay(idVentana,false);
				return;
			}
			   
			var parametros = {				
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo  : "APV",
				p_Parametros : [{
					IdOrigenDestinoTransicion: cnfgRec.procesoSeleccionado.IdOrigenDestinoTransicion,
					Operacion: cnfgRec.procesoSeleccionado.Operacion,
					OrigenCtaBco: cnfgRec.procesoSeleccionado.HabilitaCuentaOrigen === 'N' ? 0 : cnfgRec.cuentaOrigen,
					SocioNegocioCtaBco: cnfgRec.cuentaPuente,
					DestinoCtaBco: cnfgRec.cuentaDestino,
					IdEstado: cnfgRec.estado,
				}],
			};
					
			bcsServiciosAPV.ModificarOrigenDestino(parametros).$promise.then(function(respuesta){
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));		 

				var mensajeTotal = new Array();

				$.each(respuesta.P_OUT_MENSAJES,function(key,value){
					 var objMensaje = {
						Id : value.id,
						tipo : value.tipo === 'OK' ? 'INFORMACION' : value.tipo,
						codigo : value.codigo,
						descripcion : $translate.instant(value.descripcion)
					 };
					 mensajeTotal.push(objMensaje);
				});
							
				respuesta.P_OUT_MENSAJES = mensajeTotal;
				bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_PROCESO_COMPLETADO"),conflictos:util.parseServerResponse(respuesta)},idVentana);
				bcsComunes.ventanaOverlay(idVentana,false);		
				
				cnfgRec.proData.setDataSource(new kendo.data.DataSource({ 
					 type: "odata",
			            serverFiltering: false,
			            sort: { field: "Operacion", dir: "asc" },
			            serverPaging: false,
			            transport: {
			            	read: {
			            		url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6087",
								dataType:"json"
			            	}
			            },
			            schema 			: {
							model	: {
				                   fields	: {
				                   	IdEmpresa					: { type: "number",editable: false	},
				                   	IdOrigenDestinoTransicion	: { type: "number",editable: false 	},
				                   	Operacion					: { type: "string",editable: false 	},
				                   	MueveCuentaCorriente		: { type: "string",editable: false 	},
				                   	MueveCuentaInversion		: { type: "string",editable: false 	},
				                   	OrigenCtaBco				: { type: "number",editable: false 	},
				                   	SocioNegocioCtaBco			: { type: "number",editable: false 	},
				                   	DestinoCtaBco				: { type: "number",editable: false 	},
				                   	IdEstado					: { type: "string",editable: false 	},
			                       }
		                  		}
						},
				}));

				cnfgRec.proData.refresh();

				
			},function(respuesta){
				var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
					$.each(ErrorDetalle,function(key,value){	
						bcsComunes.msg("error","Error","Existen problemas:" + value );
					});
			   	bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas:" ),conflictos:util.parseServerResponse(respuesta)},idVentana);
			});	
		};
		
		cnfgRec.AsignaValoresConfiguracion = function(IdDeposito){
			
		};
/*-------------------------------------------------------------------------------*/
	});
}());