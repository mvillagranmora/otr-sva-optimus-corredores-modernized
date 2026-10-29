(function() {
	angular.module('ModuloMesa').controller('ConsultaOperacionesCtrl',
	function($scope,$translate,$resource,$timeout,bcsComunes,bcsServiciosMesa,bcsConsultasMesa,bcsServicioPlataforma,$http)
	{	

		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		bcsComunes.ventanaAutoOverlay(idVentana,false,1000);
		
		var parametrosVentana = bcsComunes.ventanaParams(idVentana);
		$scope.data = {};
		var ParametrosForward = [];
		var idCartera = 0;
		var folioOperacion = 0;
		var idOperacion = 0;
		var tipoOperacion = '';
		var subReports = [];
		$scope.dataTemplate= {};

		
		//Configurar al principio
		$scope.data.kngFecha = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.kngFechaDesde = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.kngFechaHasta = util.dateInicioDia($scope.SesionFechaSistema);
    	$scope.data.todos = 'S';
    	$scope.data.habilita = true;
    	$scope.data.HabilitaFecha = false;
    	$scope.data.txtFH = true;
    	$scope.data.txtFD = true;
    	$scope.HabilitoBotonAnular = false;
		$scope.HabilitoBotonModificar = false;
		$scope.data.activarReporte = false;
		/* EDN - Anticipo de Operaciones Forward*/
		$scope.HabilitoBotonAnticipo = false;
		$scope.ObjSeleccionGrilla = {};
		
		$scope.ModificarSpotOrigenFWD = false;
		$scope.RelacionSpotOperFFMM = false;
		$scope.HabilitoCheckFFMM = false;
		$scope.HabilitoBotonFFMM = false;
		
		//Combobox Mesa
		$scope.data.oConfiguracionMesa={
			filtros : {
				logic : "or",
				IdTipoProducto1 : { "field"   : "IdTipoProducto","operator": "eq", "value" : "MEME"},
				IdTipoProducto2	: { "field"   : "IdTipoProducto","operator": "eq", "value" : "MEFW"}
			}
		};
		
		//Combobox Tipo Instrumento
		$scope.data.oConfiguracionTipoInstrumento={
			filtros : {
				IdEstado			: { field : "IdEstado",			operator : "eq",	value : "ACT" },
				IdTipoInstrumento	: { field : "IdTipoInstrumento",operator : "eq",	value : "" },
				Instrumento			: { field : "Instrumento",		operator : "neq",	value : null }
			}
		};
		//Combobox Operación
		$scope.data.oConfiguracionOperacion={
			filtros : {
				logic : "or",
				IdTipoProducto1 : { "field"   : "IdTipoProducto","operator": "eq", "value" : "MEME" },
				IdTipoProducto1 : { "field"   : "IdTipoProducto","operator": "eq", "value" : "MEFW" }
			}
		};
		
		/*Busca Configuración Mesa*/
		var fnConsultarConfiguracionMesa = function(){
			var params = {
				p_IdEmpresa 	 	: $scope.SesionIdEmpresa,	
				p_IdTipoProducto 	: 'MEFW',
				p_CodigoParametro 	: ''
			};
			
			bcsServiciosMesa.ConsultarConfiguracionMesa(params).$promise.then(function(respuesta){
				$.each(respuesta.P_OUT_DATOSMESA,function(key,value){
					var ObjParametros = {
			            "CodigoParametro" : value.CodigoParametro,
			            "Valor"           : value.Valor
					};
					ParametrosForward.push(ObjParametros);
				});
			});
		};
		
		var fnBuscoValorParametro = function(strParam){
			var valor = undefined;
			for(var i = 0, len = ParametrosForward.length; i < len; i++){
				if(ParametrosForward[i].CodigoParametro === strParam) {
					valor = ParametrosForward[i].Valor;
					break;
				}
			}			
			return valor;
		};
		
		fnConsultarConfiguracionMesa();
		
		
		//Busca valor configuracion Modificar Operacion Spot Origen FWD
		var fnConsultarConfiguracionMesaSpot = function(){
			var params = {
				p_IdEmpresa 	 	: $scope.SesionIdEmpresa,	
				p_IdTipoProducto 	: 'MEME',
				p_CodigoParametro 	: 'MODIFICARSPOTORIGENFWD'
			};
			
			bcsServiciosMesa.ConsultarConfiguracionMesa(params).$promise.then(function(respuesta){
				$.each(respuesta.P_OUT_DATOSMESA,function(key,value){
					$scope.ModificarSpotOrigenFWD = value.Valor === 'S' ? true : false;
				});
			});
		};
		fnConsultarConfiguracionMesaSpot();
		
		//Busca valor configuracion Relacion de Spot con un Fondos mutuos Dolar
		var fnConsultarConfiguracionRelacionSpotFFMM = function(){
			var params = {
				p_IdEmpresa 	 	: $scope.SesionIdEmpresa,	
				p_IdTipoProducto 	: 'MEME',
				p_CodigoParametro 	: 'RELACION_SPT_OPERACION_FMM_DOLAR'
			};
			
			bcsServiciosMesa.ConsultarConfiguracionMesa(params).$promise.then(function(respuesta){
				$.each(respuesta.P_OUT_DATOSMESA,function(key,value){
					$scope.RelacionSpotOperFFMM = value.Valor === 'S' ? true : false;
				});
			});
		};
		fnConsultarConfiguracionRelacionSpotFFMM();
		
		/*********************************************
		 * WATCHES
		 ********************************************/
		$scope.$watch('data.oConfiguracionMesa.seleccionado',function(newval,oldval) {   
            if (newval === oldval) return;
            
            if ($scope.data.oConfiguracionMesa.seleccionado.IdTipoProducto === undefined){
            	$scope.data.oConfiguracionTipoInstrumento.filtros.IdTipoInstrumento.value ='';
            }
            else if($scope.data.oConfiguracionMesa.seleccionado.IdTipoProducto === 'MEME'){
            	$scope.data.oConfiguracionTipoInstrumento.filtros.IdTipoInstrumento.value ='SPT';
            }
            else if($scope.data.oConfiguracionMesa.seleccionado.IdTipoProducto === 'MEFW'){
            	$scope.data.oConfiguracionTipoInstrumento.filtros.IdTipoInstrumento.value ='FWD';
            }
		});
		
		
		$scope.$watch('data.tabMovimientoActive',function(newval,oldval) {   
            if (newval === oldval) return;
                  
            if($scope.data.tabMovimientoActive === true){
            	$scope.data.todos='S';
            	$scope.data.oConfiguracionMesa.filtros.IdTipoProducto1.value = 'MEME';
              	$scope.data.oConfiguracionMesa.filtros.IdTipoProducto2.value = 'MEFW';
              	$scope.data.habilita = true;
              	$scope.data.HabilitaFecha = false;
        		$scope.data.kngFechaDesde = util.dateInicioDia($scope.SesionFechaSistema);
            	$scope.data.txtFH = true;
            	$scope.data.txtFD = true;
            }
		});
		
		
		$scope.$watch('data.tabCarteraActive',function(newval,oldval) {   
            if (newval === oldval) return;
                  
            if($scope.data.tabCarteraActive === true){
            	$scope.data.todos='N';
            	$scope.data.oConfiguracionMesa.filtros.IdTipoProducto1.value = '';
              	$scope.data.oConfiguracionMesa.filtros.IdTipoProducto2.value = 'MEFW';
              	$scope.data.habilita = false;
              	$scope.data.kngFechaDesde = util.dateInicioDia('1900-01-01');
              	$scope.data.HabilitaFecha = true;
              	$scope.data.kngFechaHasta = util.dateInicioDia($scope.SesionFechaSistema);
            	$scope.data.txtFH = false;
            	$scope.data.txtFD = false;
            }
		});
		
		
		$scope.$watch('data.tabVencimientoActive',function(newval,oldval) {   
            if (newval === oldval) return;
                  
            if($scope.data.tabVencimientoActive === true){
            	$scope.data.todos='N';
            	$scope.data.oConfiguracionMesa.filtros.IdTipoProducto1.value = '';
              	$scope.data.oConfiguracionMesa.filtros.IdTipoProducto2.value = 'MEFW';
              	$scope.data.habilita = false;
              	$scope.data.HabilitaFecha = false;
            	$scope.data.txtFH = true;
            	$scope.data.txtFD = true;
        		$scope.data.kngFechaDesde = util.dateInicioDia($scope.SesionFechaSistema);
            }
		});
		
		
		$scope.$watch('ObjSeleccionGrilla.seleccionGrillaMovimiento',function(newval,oldval){
			$scope.HabilitoBotonAnular = false;
			$scope.HabilitoBotonModificar = false;
			if($scope.ObjSeleccionGrilla.seleccionGrillaMovimiento === undefined){
				return;
			}
			
			idCartera = newval.IdCartera;
			folioOperacion = newval.FolioOperacion;
			idOperacion = newval.IdOperacion;
			tipoOperacion = newval.Mesa;
			$scope.data.activarReporte = true;
			
			// si FechaOperacion es menor que FechaProceso botones Modificar y Anular se deshabilitan
			var difFechas = Math.round(util.datediff($scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.FechaOperacion,$scope.data.kngFecha).asDays());
			if(difFechas === 0){
				var tipIns = $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.IdTipoInstrumento;
				var estado = $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.Estado;
				var estCnf = $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.EstadoConfirmacion;

				/*if(tipIns === 'SPT' && estCnf === 'CNF'){
					$scope.HabilitoBotonAnular = false;
					$scope.HabilitoBotonModificar = false;
					return;
				}*/

				$scope.HabilitoBotonAnular = estado === 'VIG' || estado === 'PEN';
				
				var tipoCanalIngreso = $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.IdTipoCanalIngreso;
				if($scope.HabilitoBotonAnular){
					$scope.HabilitoBotonModificar = tipIns === 'SPT' && tipoCanalIngreso === 'FWD' ? $scope.ModificarSpotOrigenFWD : true;
				}
				else{
					$scope.HabilitoBotonModificar = false;
				}
			}
		});
		
		
		$scope.$watch('ObjSeleccionGrilla.seleccionGrillaCartera',function(newval,oldval){
			if($scope.ObjSeleccionGrilla.seleccionGrillaCartera === undefined){
				$scope.HabilitoBotonAnticipo = false;
				return;
			}
			
			var numDiasParaAnticipar = Number(fnBuscoValorParametro('DIAANTICIPO'));
			var FechaAnticipo = util.date($scope.ObjSeleccionGrilla.seleccionGrillaCartera.FechaOperacion).add(numDiasParaAnticipar,'days').toDate()
			
			if(FechaAnticipo <= util.dateInicioDia($scope.SesionFechaSistema) && $scope.ObjSeleccionGrilla.seleccionGrillaCartera.Estado === 'VIG'){
				$scope.HabilitoBotonAnticipo = true;
			}else{
				$scope.HabilitoBotonAnticipo = true;
			}
			
			// si FechaVencimiento es igual a FechaProceso, boton Anticipo se deshabilita
			var difFechas = Math.round(util.datediff($scope.ObjSeleccionGrilla.seleccionGrillaCartera.FechaVencimiento,$scope.data.kngFecha).asDays());
			if(difFechas <= 0){
				$scope.HabilitoBotonAnticipo = false;
			}
		});
		$scope.$watch('data.oConfiguracionMesa.seleccionado.IdTipoProducto',function(newval,oldval){
			if (newval != oldval && newval=='MEME' && $scope.RelacionSpotOperFFMM === true) {
				$scope.HabilitoCheckFFMM = true;
			}else{$scope.HabilitoCheckFFMM = false;$scope.data.FmDolar=false};
			
		});
		
		/*********************************************
		 * DATASOURCES
		 ********************************************/
		//tab Movimientos
		var dsDefConsultaMovimientosMesa = {
			type: "odata",
			definition: {
				serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                sort 			: [
                    { field: "IdTipoInstrumento",	dir: "desc" },
                    { field: "IdOperacionSpotVigente",	dir: "desc" },
                    { field: "IdOperacion",			dir: "desc" },
                    { field: "IdCartera",			dir: "desc" },
                    //{ field: "FechaOperacionOPE",	dir: "desc" },
                ],
                pageSize: 25,
                transport: {
                	read: {
                		url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=513",
                		dataType:"json"
                	}
                },
                schema: {
                	model: {
                		fields: {
                			IdOperacion							: {type: "number"},
                			IdOperacionUI						: {type: "number"},
                			FolioOrigenFW						: {type: "number"},
							FechaOperacion						: {type: "sysdate"},
							FechaLiquidacion					: {type: "sysdate"},							
							FechaEntregamos  					: {type: "sysdate"},
							FechaRecibimos  					: {type: "sysdate"},
							HoraOperacion  	        			: {type:"string"},
							FechaOperacionOPE					: {type: "systime"},
							NombreInstrumento       			: {type: "string"},
							Nemotecnico       					: {type: "string"},
							IdTipoOperacion						: {type: "string"},
							TipoOperacion						: {type: "string"},
							RutCliente							: {type: "string"},
							NombreCliente						: {type: "string"},
							CodigoPropositoPortafolio 			: {type: "string"},
							IdMonedaPrincipal					: {type: "string"},
							IdMonedaSecundaria					: {type: "string"},
							MontoMonedaPrincipal				: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'','').excelFormat;}},
							MontoMonedaSecundaria				: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'','').excelFormat;}},
							ResultadoMonedaLocalPuntas 			: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto','CLP','','').excelFormat;}},
							ResultadoMonedaLocalBidAsk 			: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto','CLP','','').excelFormat;}},
							ResultadoMonedaLocalDO 				: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto','CLP','','').excelFormat;}},
							ResultadoMonedaLocalTCSpot 			: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto','CLP','','').excelFormat;}},
							ResultadoMonedaLocalTCCierreMesa	: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto','CLP','','').excelFormat;}},
							TipoCambioCierre					: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'','').excelFormat;}},
							TipoCambioCosto						: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'','').excelFormat;}},
							ParidadCierre						: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'','').excelFormat;}},
							ParidadCosto						: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'','').excelFormat;}},
							MontoEquivalente					: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'','').excelFormat;}},
							Margen								: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'','').excelFormat;}},
							Utilidad							: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'','').excelFormat;}},
							MontoDiferido						: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'','').excelFormat;}},
							Modalidad							: {type: "string"},
							ModalidadLiquidacion				: {type: "string"},
							Plazo								: {type: "number"},
							PlazoResidual						: {type: "number"},
							FechaVencimiento					: {type: "sysdate"},
							FechaEfectiva						: {type: "sysdate"},
							Agente								: {type: "number"},
							NombreAgente						: {type: "string"},
							OperacionFicticia					: {type: "string"},
							CodigoUsuario						: {type: "string"},
							Estado								: {type: "string"},
							Mesa								: {type: "string"},
							IdTipoCanalIngreso					: {type: "string"},
							INGCantidad							: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'','').excelFormat;}},
							INGMontoMonedaPrincipal				: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'','').excelFormat;}},
							INGMontoMonedaSecundaria			: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'','').excelFormat;}},
							IdOperacionSpotReferencia			: {type: "number"},
							IdOperacionSpotOrigen				: {type: "number"},
							IdOperacionSpotVigente				: {type: "number"},
							IdEstado							: {type: "string"},
							TipoVencimiento						: {type: "string"},
							//Fondos mutuos
							IdSolicitudAporteRescate			: {type: "number"},
							EstadoAporteRescate					: {type: "string"}, 
							TipoAporteRescate					: {type: "string"},
							FechaLiquidacionAporteRescate		: {type: "sysdate"}
							
                		}
                	}
            	},
   			 aggregate			: [ //Para resumen de totales
			       { field: "MontoMonedaPrincipal", aggregate: "sum" }
			      ,{ field: "IdMonedaSecundaria", aggregate: "sum" }]
			}
		};
		
		
		//tab Cartera
		var dsDefConsultaMovimientosFWD = {
			type : "odata",
			definition: {
                serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                sort: [
					{ field: "IdOperacion",	dir: "desc" },
					{ field: "IdCartera",	dir: "desc" },
   	            ],
                pageSize: 200,
                transport: {
                	read: {
                		url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=515",
                		dataType:"json"
                	}
                },
                schema: {
                	model: {
                		fields: {
                			IdOperacion				: {type: "number"},
							FechaOperacion			: {type: "sysdate"},
							FechaLiquidacion		: {type: "sysdate"},
							HoraOperacion  	        : {type:"string"},							
							FechaOperacionOPE		: {type: "systime"},
							HoraOperacion  	        : {type:"string"},
							NombreInstrumento       : {type: "string"},
							Nemotecnico       		: {type: "string"},
							IdTipoOperacion			: {type: "string"},
							TipoOperacion			: {type: "string"},
							RutCliente				: {type: "string"},
							NombreCliente			: {type: "string"},
							CodigoPropositoPortafolio:{type: "string"},
							IdMonedaPrincipal		: {type: "string"},
							IdMonedaSecundaria		: {type: "string"},
							MontoMonedaPrincipal	: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'','').excelFormat;}},
							MontoMonedaSecundaria	: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'','').excelFormat;}},
							TipoCambioCierre		: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'','').excelFormat;}},
							TipoCambioCosto			: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'','').excelFormat;}},
							ParidadCierre			: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'','').excelFormat;}},
							ParidadCosto			: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'','').excelFormat;}},
							MontoEquivalente		: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'','').excelFormat;}},
							Margen					: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'','').excelFormat;}},
							Utilidad				: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'','').excelFormat;}},
							MontoDiferido			: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'','').excelFormat;}},
							Modalidad				: {type: "string"},
							ModalidadLiquidacion	: {type: "string"},
							Plazo					: {type: "number"},
							PlazoResidual			: {type: "number"},
							FechaVencimiento		: {type: "sysdate"},
							FechaEfectiva			: {type: "sysdate"},
							Agente					: {type: "number"},
							NombreAgente			: {type: "string"},
							OperacionFicticia		: {type: "string"},
							CodigoUsuario			: {type: "string"},
							Estado					: {type: "string"},
							Mesa					: {type: "string"},
							INGCantidad				: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'','').excelFormat;}},
							INGMontoMonedaPrincipal	: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'','').excelFormat;}},
							INGMontoMonedaSecundaria: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'','').excelFormat;}},
							//MontoMonedaPrincipalIngreso	: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'','').excelFormat;}},
							//MontoMonedaSecundariaIngreso: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'','').excelFormat;}},
							FechaVencimientoFiltro 	: {type: "sysdate"},
							IdEstado				: {type: "string"},
							TipoVencimiento			: {type: "string"}
						}
                	}
                }
			}
		};
		
		
		//tab Vencimiento
		var dsDefConsultaVencimientosFWD = {
			type: "odata",
			definition: {
	            serverFiltering : true, // Filtrado de datos en el servidor.
	            serverPaging    : true, // Paginado de los datos en el servidor.
	            serverSorting   : true, // Ordenamiento de los datos en el servidor.
	            sort: [
					{ field: "IdOperacion",	dir: "desc" },
					{ field: "IdCartera",	dir: "desc" },
	            ],
	            pageSize: 200,
	            transport: {
	            	read: {
	            		url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=539",
	            		dataType:"json"
	            	}
	            },
	            schema: {
	            	model: {
	            		fields: {
	            			IdOperacion				: {type: "number"},
							FechaOperacion			: {type: "sysdate"},
							FechaLiquidacion		: {type: "sysdate"},
							FechaAnticipo			: {type: "sysdate"},
							FechaOperacionOPE		: {type: "systime"},
							HoraOperacion  	        : {type:"string"},
							NombreInstrumento       : {type: "string"},
							Nemotecnico       		: {type: "string"},
							IdTipoOperacion			: {type: "string"},
							TipoOperacion			: {type: "string"},
							RutCliente				: {type: "string"},
							NombreCliente			: {type: "string"},
							CodigoPropositoPortafolio:{type: "string"},
							IdMonedaPrincipal		: {type: "string"},
							IdMonedaSecundaria		: {type: "string"},
							MontoMonedaPrincipal	: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'','').excelFormat;}},
							MontoMonedaSecundaria	: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'','').excelFormat;}},
							TipoCambioCierre		: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'','').excelFormat;}},
							TipoCambioCosto			: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'','').excelFormat;}},
							ParidadCierre			: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'','').excelFormat;}},
							ParidadCosto			: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'','').excelFormat;}},
							TipoCambioAnticipo		: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'','').excelFormat;}},
							ParidadAnticipo			: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'','').excelFormat;}},
							MontoEquivalente		: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'','').excelFormat;}},
							Margen					: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'','').excelFormat;}},
							Utilidad				: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'','').excelFormat;}},
							MontoDiferido			: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'','').excelFormat;}},
							Modalidad				: {type: "string"},
							ModalidadLiquidacion	: {type: "string"},
							Plazo					: {type: "number"},
							PlazoResidual			: {type: "number"},
							FechaVencimiento		: {type: "sysdate"},
							FechaEfectiva			: {type: "sysdate"},
							Agente					: {type: "number"},
							NombreAgente			: {type: "string"},
							OperacionFicticia		: {type: "string"},
							CodigoUsuario			: {type: "string"},
							Estado					: {type: "string"},
							Mesa					: {type: "string"},
							INGCantidad				: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'','').excelFormat;}},
							INGMontoMonedaPrincipal	: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'','').excelFormat;}},
							INGMontoMonedaSecundaria: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'','').excelFormat;}},
							//MontoMonedaPrincipalIngreso	: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'','').excelFormat;}},
							//MontoMonedaSecundariaIngreso: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'','').excelFormat;}},
							IdEstado				: {type: "string"},
							TipoVencimiento			: {type: "string"}
	            		}
	            	}
	            }
			}
		};
		
		
		var ConsultaTemplate = new bcs.DataSource({
			type 		: "odata",
			definition 	: {
				serverFiltering: true,
				transport: {
					read: {
						url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=3015",
						dataType : "json",
					}
				},
				schema: {
					model: {
						fields: {
							CompanyId	: {type: "string"},
							Id			: {type: "string"},
							ReportName	: {type: "string"}					
						}
					}
				}
			},
		});
		
		// Validación Spot Facturado
		var ConsultarSpotFacturado = new bcs.DataSource({
			type 		: "odata",
			definition 	: {
				serverFiltering	: true,
				transport: {
					read : {
						url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=485",
						dataType : "json",
					}
				},
				schema: {
					model: {
						fields: {
							IdOperacionSpot	: {type: "number"},
							IdResultado		: {type: "number"},
							Codigo			: {type: "string"}
						}
					}
				}
			},
		});
		
		
		/*********************************************
		 * GRILLAS
		 ********************************************/
		var dsGrillaMovimientos = new bcs.DataSource(dsDefConsultaMovimientosMesa);
		$scope.confMovimientos = {
			dataSource: dsGrillaMovimientos,
			columns: [
			     //{field: "IdOperacion", title: "UI_FOLIO_OPERACION", width: "80px", sortable : true }
			     {field: "IdOperacionUI", title: "UI_FOLIO_OPERACION", width: "80px", sortable : true }
			    ,{field: "FolioOrigenFW", title: "UI_FOLIO_FORWARD_ORIGEN", width: "95px", sortable : true }
			    ,{field: "FechaOperacion", title: "UI_FECHA_OPERACION", width: "92px", sortable : false, groupable: true}
			    ,{field: "FechaLiquidacion", title: "UI_FECHA_LIQUIDACION", width: "92px", sortable : false , groupable: true}
			    ,{field: "FechaEntregamos", title: "UI_FECHA_ENTREGAMOS", width: "92px", sortable : false }
			    ,{field: "FechaRecibimos", title: "UI_FECHA_RECIBIMOS", width: "92px", sortable : false }
			    ,{field: "HoraOperacion", title: "UI_HORA_OPERACION", width: "82px", sortable : false , groupable: true}
			    ,{field: "Nemotecnico", title: "UI_NEMOTECNICO", width: "80px", sortable : false, hidden: true, groupable: true }
			    ,{field: "NombreInstrumento", title: "UI_NOMBRE_TIPO_INSTRUMENTO", width: "130px", sortable : false, groupable: true}
			    ,{field: "IdTipoOperacion", title: "UI_ID_TIPO_OPERACION", width: "90px", sortable : false, hidden: true, groupable: true }				       				    
			    ,{field: "TipoOperacion", title: "UI_TIPO_OPERACION", width: "90px", sortable : false , groupable: true}
			    ,{field: "IdTipoCanalIngreso", title: "UI_CANAL", width: "40px", sortable : false, groupable: true }
			    ,{field: "RutCliente", title: "UI_RUT_CLIENTE", width: "90px", sortable : false }
			    ,{field: "NombreCliente", title: "UI_NOMBRE_CLIENTE", width: "120px", sortable : false }
			    ,{field: "CodigoPropositoPortafolio", title: "UI_PORTAFOLIO", width: "120px", sortable : false }
			    ,{field: "IdMonedaPrincipal", title: "UI_MONEDA_PRINCIPAL", width: "90px", sortable : false, groupable: true}
			    ,{field: "IdMonedaSecundaria", title: "UI_MONEDA_SECUNDARIA", width: "100px", sortable : false, groupable: true}
				   				    
			   // ,{field: "MontoMonedaPrincipal", title: "UI_MONTO_MONEDA_PRINCIPAL", width: "100px", sortable : false,				template : " <label> #:  (util.bcs_formato(MontoMonedaPrincipal,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#= util.formateaNumero(sum,0) #", footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','USD', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}}
			    ,{field: "MontoMonedaPrincipal", title: "UI_MONTO_MONEDA_PRINCIPAL", width: "100px", sortable : false,				template : " <label> #:  (util.bcs_formato(MontoMonedaPrincipal,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#= util.formateaNumero(sum,0) #", footerAttributes:{style:"text-align: right; padding-right:0;"}}

			    ,{field: "MontoMonedaSecundaria", title: "UI_MONTO_MONEDA_SECUNDARIA", width: "110px", sortable : false,			template : " <label> #:  (util.bcs_formato(MontoMonedaSecundaria,IdTipoInstrumento,'Monto',IdMonedaSecundaria,'','')).valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#= util.formateaNumero(sum,0) #", footerAttributes:{style:"text-align: right; padding-right:0;"}}				       				    
			    //,{field: "INGMontoMonedaSecundaria", title: "UI_MONTO_MONEDA_SECUNDARIA", width: "110px", sortable : false,			template : " <label> #:  (util.bcs_formato(INGMontoMonedaSecundaria,IdTipoInstrumento,'Monto',IdMonedaSecundaria,'','')).valor #</label>" }				       				    
   				,{field: "ResultadoMonedaLocalPuntas", title: "UI_RESULTADO_MONEDA_LOCAL_PUNTAS", width: "110px", sortable : false,	template : " <label> #:  (util.bcs_formato(ResultadoMonedaLocalPuntas,IdTipoInstrumento,'Monto','CLP','','')).valor #</label>" }
   				,{field: "ResultadoMonedaLocalBidAsk", title: "UI_RESULTADO_MONEDA_LOCAL_BIASK", width: "110px", sortable : false,	template : " <label> #:  (util.bcs_formato(ResultadoMonedaLocalBidAsk,IdTipoInstrumento,'Monto','CLP','','')).valor #</label>" }
   				,{field: "ResultadoMonedaLocalDO", title: "UI_RESULTADO_MONEDA_LOCAL_DO", width: "110px", sortable : false,			template : " <label> #:  (util.bcs_formato(ResultadoMonedaLocalDO,IdTipoInstrumento,'Monto','CLP','','')).valor #</label>" }
   				,{field: "ResultadoMonedaLocalTCSpot", title: "UI_RESULTADO_MONEDA_LOCAL_TCSPOT", width: "110px", sortable : false,	template : " <label> #:  (util.bcs_formato(ResultadoMonedaLocalTCSpot,IdTipoInstrumento,'Monto','CLP','','')).valor #</label>" }
   				,{field: "ResultadoMonedaLocalTCCierreMesa", title: "UI_RESULTADO_MONEDA_LOCAL_TCCIERREMESA", width: "110px", sortable : false 	, template : " <label> #:  (util.bcs_formato(ResultadoMonedaLocalTCCierreMesa,IdTipoInstrumento,'Monto','CLP','','')).valor #</label>" }				       				 
			    ,{field: "TipoCambioCierre", title: "UI_TC_CIERRE", width: "60px", sortable : false,								template : " <label> #:  (util.bcs_formato(TipoCambioCierre,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>" }				       				    
			    ,{field: "TipoCambioCosto", title: "UI_TC_COSTO", width: "60px", sortable : false,									template : " <label> #:  (util.bcs_formato(TipoCambioCosto,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>" }				       				    
			    ,{field: "ParidadCierre", title: "UI_PARIDAD_CIERRE", width: "90px", sortable : false,								template : " <label> #:  (util.bcs_formato(ParidadCierre,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>" }				       				    
			    ,{field: "ParidadCosto", title: "UI_PARIDAD_COSTO", width: "90px", sortable : false,								template : " <label> #:  (util.bcs_formato(ParidadCosto,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>" }				       				    
			    ,{field: "INGMontoMonedaSecundaria", title: "UI_MONTO_EQUIVALENTE", width: "100px", sortable : false,				template : " <label> #:  (util.bcs_formato(INGMontoMonedaSecundaria,IdTipoInstrumento,'Monto',IdMonedaSecundaria,'','')).valor #</label>" }				       				    
			    ,{field: "Margen", title: "UI_MARGEN", width: "60px", sortable : false,												template : " <label> #:  (util.bcs_formato(Margen,IdTipoInstrumento,'Monto',IdMonedaSecundaria,'','')).valor #</label>" }				       				    
			    ,{field: "Utilidad", title: "UI_UTILIDAD", width: "80px", sortable : false,											template : " <label> #:  (util.bcs_formato(Utilidad,IdTipoInstrumento,'Monto',IdMonedaSecundaria,'','')).valor #</label>" }				       				    
			    ,{field: "MontoDiferido", title: "UI_MONTO_DIFERIDO", width: "80px", sortable : false,								template : " <label> #:  (util.bcs_formato(MontoDiferido,IdTipoInstrumento,'Monto',IdMonedaSecundaria,'','')).valor #</label>" }				       				    
			    
			    ,{field: "Modalidad", title: "UI_MODALIDAD", width: "60px", sortable : false, hidden : true }				       				    
			    ,{field: "ModalidadLiquidacion", title: "UI_MODALIDAD", width: "120px", sortable : false }
			    ,{field: "Plazo", title: "UI_PLAZO_FORWARD", width: "30px", sortable : false }				       				    
			    ,{field: "PlazoResidual", title: "UI_PLAZO_RESIDUAL", width: "80px",  sortable : false }				       				    
			    ,{field: "FechaVencimiento", title: "UI_FECHA_VENCIMIENTO", width: "100px",  sortable : false }				       				    
			    ,{field: "FechaEfectiva", title: "UI_FECHA_EFECTIVA", width: "80px",  sortable : false }
			    ,{field: "Agente", title: "UI_AGENTE", width: "60px",  sortable : false, hidden: true , groupable: true}	
			    ,{field: "NombreAgente", title: "UI_NOMBRE_AGENTE", width: "120px",  sortable : false }				       				    
			    ,{field: "OperacionFicticia", title: "UI_OPERACION_FICTICIA", width: "90px",  sortable : false }				       				    
			    ,{field: "CodigoUsuario", title: "UI_NOMBRE_USUARIO", width: "90px",  sortable : false }				       				    
			    ,{field: "Estado", title: "UI_ESTADO", width: "40px", sortable : false , groupable: true}				       				    
		    	,{field: "Mesa", title: "UI_MESA", width: "40px",  sortable : false , groupable: true}
		    	,{field: "IdOperacionSpotReferencia", title: "UI_FOLIO_REFERENCIA", width: "80px", sortable : true }
		    	//Fondos mutuos
		    	,{field: "IdSolicitudAporteRescate", title: "UI_SECUENCIA_FM", width: "80px", sortable : true, hidden: true }
		    	,{field: "EstadoAporteRescate", title: "UI_ESTADO_FM", width: "80px", sortable : true, hidden: true }
		    	,{field: "TipoAporteRescate", title: "UI_TIPO_FM", width: "80px", sortable : true, hidden: true }
		    	,{field: "FechaLiquidacionAporteRescate", title: "UI_FECHA_LIQUIDACION_FM", width: "80px", sortable : true, hidden: true }
		    	
		    
		    	
		    	
		    ],		
			sortable		: true,
			pageable		: true,
			filterable		: true,
			navigatable 	: true,
			userSettings 	: true,
			height			: 560,
			excel 			: { allPages: true },
			scrollable		: false,
			selectable 		: "row",
		    autoBind 		: false,
		    gridCreated     : function(g){
				$scope.ApiMovimientos = g;
			}
			
			
       	};
		
		
		var dsGrillaCartera = new bcs.DataSource(dsDefConsultaMovimientosFWD);
		$scope.confCartera = {
			dataSource: dsGrillaCartera,
			columns: [
			     {field: "IdOperacion", title: "UI_FOLIO_OPERACION", width: "80px", sortable : true }
			    ,{field: "FechaOperacion", title: "UI_FECHA_OPERACION", width: "92px", sortable : false}
			    ,{field: "FechaLiquidacion", title: "UI_FECHA_LIQUIDACION", width: "92px", sortable : false }
			    ,{field: "HoraOperacion", title: "UI_HORA_OPERACION", width: "82px", sortable : false }
			    ,{field: "Nemotecnico", title: "UI_NEMOTECNICO", width: "80px", sortable : false, hidden: true }
			    ,{field: "NombreInstrumento", title: "UI_NOMBRE_TIPO_INSTRUMENTO", width: "130px", sortable : false}
			    ,{field: "IdTipoOperacion", title: "UI_ID_TIPO_OPERACION", width: "90px", sortable : false, hidden: true }				       				    
			    ,{field: "TipoOperacion", title: "UI_TIPO_OPERACION", width: "90px", sortable : false }
			    ,{field: "RutCliente", title: "UI_RUT_CLIENTE", width: "90px", sortable : false }
			    ,{field: "NombreCliente", title: "UI_NOMBRE_CLIENTE", width: "120px", sortable : false }
			    ,{field: "CodigoPropositoPortafolio", title: "UI_PORTAFOLIO", width: "120px", sortable : false }
			    ,{field: "IdMonedaPrincipal", title: "UI_MONEDA_PRINCIPAL", width: "90px", sortable : false}
			    ,{field: "IdMonedaSecundaria", title: "UI_MONEDA_SECUNDARIA", width: "100px", sortable : false}
			    //,{field: "MontoMonedaPrincipal", title: "UI_MONTO_MONEDA_PRINCIPAL", width: "100px", sortable : false,	template : " <label> #:  (util.bcs_formato(MontoMonedaPrincipal,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>" }
			    //,{field: "MontoMonedaPrincipalIngreso", title: "UI_MONTO_MONEDA_PRINCIPAL_INGRESO", width: "100px", sortable : false,	template : " <label> #:  (util.bcs_formato(MontoMonedaPrincipalIngreso,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>" }
			    ,{field: "INGMontoMonedaPrincipal", title: "UI_MONTO_MONEDA_PRINCIPAL", width: "100px", sortable : false,	template : " <label> #:  (util.bcs_formato(INGMontoMonedaPrincipal,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>" }
			    //,{field: "MontoMonedaSecundaria", title: "UI_MONTO_MONEDA_SECUNDARIA", width: "110px", sortable : false,	template : " <label> #:  (util.bcs_formato(MontoMonedaSecundaria,IdTipoInstrumento,'Monto',IdMonedaSecundaria,'','')).valor #</label>" }
			    ,{field: "INGMontoMonedaSecundaria", title: "UI_MONTO_MONEDA_SECUNDARIA", width: "110px", sortable : false,	template : " <label> #:  (util.bcs_formato(INGMontoMonedaSecundaria,IdTipoInstrumento,'Monto',IdMonedaSecundaria,'','')).valor #</label>" }
			    ,{field: "TipoCambioCierre", title: "UI_TC_CIERRE", width: "60px", sortable : false,					template : " <label> #:  (util.bcs_formato(TipoCambioCierre,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>" }				       				    
			    ,{field: "TipoCambioCosto", title: "UI_TC_COSTO", width: "60px", sortable : false,						template : " <label> #:  (util.bcs_formato(TipoCambioCosto,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>" }				       				    
			    ,{field: "ParidadCierre", title: "UI_PARIDAD_CIERRE", width: "90px", sortable : false,					template : " <label> #:  (util.bcs_formato(ParidadCierre,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>" }				       				    
			    ,{field: "ParidadCosto", title: "UI_PARIDAD_COSTO", width: "90px", sortable : false,					template : " <label> #:  (util.bcs_formato(ParidadCosto,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>" }				       				    
			    ,{field: "INGMontoMonedaSecundaria", title: "UI_MONTO_EQUIVALENTE", width: "100px", sortable : false,	template : " <label> #:  (util.bcs_formato(INGMontoMonedaSecundaria,IdTipoInstrumento,'Monto',IdMonedaSecundaria,'','')).valor #</label>" }			       				    
			    ,{field: "Margen", title: "UI_MARGEN", width: "60px", sortable : false,									template : " <label> #:  (util.bcs_formato(Margen,IdTipoInstrumento,'Monto',IdMonedaSecundaria,'','')).valor #</label>" }				       				    
			    ,{field: "Utilidad", title: "UI_UTILIDAD", width: "80px", sortable : false,								template : " <label> #:  (util.bcs_formato(Utilidad,IdTipoInstrumento,'Monto',IdMonedaSecundaria,'','')).valor #</label>" }			       				    
			    ,{field: "MontoDiferido", title: "UI_MONTO_DIFERIDO", width: "80px", sortable : false,					template : " <label> #:  (util.bcs_formato(MontoDiferido,IdTipoInstrumento,'Monto',IdMonedaSecundaria,'','')).valor #</label>" }			       				    
			    
			    ,{field: "Modalidad", title: "UI_MODALIDAD", width: "60px", sortable : false, hidden : true }				       				    
			    ,{field: "ModalidadLiquidacion", title: "UI_MODALIDAD", width: "120px", sortable : false }
			    ,{field: "Plazo", title: "UI_PLAZO_FORWARD", width: "60px", sortable : false }				       				    
			    ,{field: "PlazoResidual", title: "UI_PLAZO_RESIDUAL", width: "80px", sortable : false }				       				    
			    ,{field: "FechaVencimiento", title: "UI_FECHA_VENCIMIENTO", width: "100px", sortable : false }				       				    
			    ,{field: "FechaEfectiva", title: "UI_FECHA_EFECTIVA", width: "80px", sortable : false }
			    ,{field: "Agente", title: "UI_AGENTE", width: "60px", sortable : false, hidden: true }	
			    ,{field: "NombreAgente", title: "UI_NOMBRE_AGENTE", width: "120px", sortable : false }				       				    
			    ,{field: "OperacionFicticia", title: "UI_OPERACION_FICTICIA", width: "90px", sortable : false }				       				    
			    ,{field: "CodigoUsuario", title: "UI_NOMBRE_USUARIO", width: "90px", sortable : false }				       				    
			    ,{field: "Estado", title: "UI_ESTADO", width: "70px", sortable : false }				       				    
		    	,{field: "Mesa", title: "UI_MESA", width: "60px", sortable : false, hidden: true }
		    ],		
			sortable		: true,
			pageable		: true,
			filterable		: true,
			navigatable 	: true,
			userSettings 	: true,
			height			: 605,
			excel 			: { allPages: true },
			scrollable		: true,
			selectable 		: "row",
		    autoBind 		: false
       	};
		
		
		var dsGrillaVencimiento = new bcs.DataSource(dsDefConsultaVencimientosFWD);
		$scope.confVencimiento = {
			dataSource: dsGrillaVencimiento,
			columns: [
			     {field: "IdOperacion", title: "UI_FOLIO_OPERACION", width: "80px", sortable : true }
			    ,{field: "FechaOperacion", title: "UI_FECHA_OPERACION", width: "92px", sortable : false}
			    ,{field: "HoraOperacion", title: "UI_HORA_OPERACION", width: "82px", sortable : false }
			    ,{field: "FechaLiquidacion", title: "UI_FECHA_LIQUIDACION", width: "92px", sortable : false }
			    ,{field: "FechaAnticipo", title: "UI_FECHA_ANTICIPO", width: "92px", sortable : false }
			    ,{field: "Nemotecnico", title: "UI_NEMOTECNICO", width: "80px", sortable : false, hidden: true }
			    ,{field: "NombreInstrumento", title: "UI_NOMBRE_TIPO_INSTRUMENTO", width: "130px", sortable : false}
			    ,{field: "IdTipoOperacion", title: "UI_ID_TIPO_OPERACION", width: "90px", sortable : false, hidden: true }				       				    
			    ,{field: "TipoOperacion", title: "UI_TIPO_OPERACION", width: "90px", sortable : false }
			    ,{field: "RutCliente", title: "UI_RUT_CLIENTE", width: "90px", sortable : false }
			    ,{field: "NombreCliente", title: "UI_NOMBRE_CLIENTE", width: "120px", sortable : false }
			    ,{field: "CodigoPropositoPortafolio", title: "UI_PORTAFOLIO", width: "120px", sortable : false }
			    ,{field: "IdMonedaPrincipal", title: "UI_MONEDA_PRINCIPAL", width: "90px", sortable : false}
			    ,{field: "IdMonedaSecundaria", title: "UI_MONEDA_SECUNDARIA", width: "100px", sortable : false}
			    
			    //,{field: "MontoMonedaPrincipalIngreso", title: "UI_MONTO_MONEDA_PRINCIPAL_INGRESO", width: "100px", sortable : false,	template : " <label> #:  (util.bcs_formato(MontoMonedaPrincipalIngreso,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>" }
			    ,{field: "MontoMonedaPrincipal", title: "UI_MONTO_MONEDA_PRINCIPAL", width: "100px", sortable : false,	template : " <label> #:  (util.bcs_formato(MontoMonedaPrincipal,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>" }
			    //,{field: "INGMontoMonedaPrincipal", title: "UI_MONTO_MONEDA_PRINCIPAL", width: "100px", sortable : false,	template : " <label> #:  (util.bcs_formato(INGMontoMonedaPrincipal,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>" }
			    ,{field: "MontoMonedaSecundaria", title: "UI_MONTO_MONEDA_SECUNDARIA", width: "110px", sortable : false,	template : " <label> #:  (util.bcs_formato(MontoMonedaSecundaria,IdTipoInstrumento,'Monto',IdMonedaSecundaria,'','')).valor #</label>" }
			    //,{field: "INGMontoMonedaSecundaria", title: "UI_MONTO_MONEDA_SECUNDARIA", width: "110px", sortable : false,	template : " <label> #:  (util.bcs_formato(INGMontoMonedaSecundaria,IdTipoInstrumento,'Monto',IdMonedaSecundaria,'','')).valor #</label>" }
			    ,{field: "TipoCambioCierre", title: "UI_TC_CIERRE", width: "60px", sortable : false,					template : " <label> #:  (util.bcs_formato(TipoCambioCierre,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>" }				       				    
			    ,{field: "TipoCambioCosto", title: "UI_TC_COSTO", width: "60px", sortable : false,						template : " <label> #:  (util.bcs_formato(TipoCambioCosto,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>" }				       				    
			    ,{field: "ParidadCierre", title: "UI_PARIDAD_CIERRE", width: "90px", sortable : false,					template : " <label> #:  (util.bcs_formato(ParidadCierre,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>" }				       				    
			    ,{field: "ParidadCosto", title: "UI_PARIDAD_COSTO", width: "90px", sortable : false,					template : " <label> #:  (util.bcs_formato(ParidadCosto,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>" }				       				    
			    ,{field: "INGMontoMonedaSecundaria", title: "UI_MONTO_EQUIVALENTE", width: "100px", sortable : false,	template : " <label> #:  (util.bcs_formato(INGMontoMonedaSecundaria,IdTipoInstrumento,'Monto',IdMonedaSecundaria,'','')).valor #</label>" }				       				    
			    ,{field: "Margen", title: "UI_MARGEN", width: "60px", sortable : false,									template : " <label> #:  (util.bcs_formato(Margen,IdTipoInstrumento,'Monto',IdMonedaSecundaria,'','')).valor #</label>" }				       				    
			    ,{field: "Utilidad", title: "UI_UTILIDAD", width: "80px", sortable : false,								template : " <label> #:  (util.bcs_formato(Utilidad,IdTipoInstrumento,'Monto',IdMonedaSecundaria,'','')).valor #</label>" }				       				    
			    ,{field: "MontoDiferido", title: "UI_MONTO_DIFERIDO", width: "80px", sortable : false,					template : " <label> #:  (util.bcs_formato(MontoDiferido,IdTipoInstrumento,'Monto',IdMonedaSecundaria,'','')).valor #</label>" }				       				    
			    ,{field: "TipoCambioAnticipo", title: "UI_TC_ANTICIPO", width: "60px", sortable : false,				template : " <label> #:  (util.bcs_formato(TipoCambioAnticipo,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>" }				       				    
			    ,{field: "ParidadAnticipo", title: "UI_PARIDAD_ANTICIPO", width: "60px", sortable : false,				template : " <label> #:  (util.bcs_formato(ParidadAnticipo,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>" }	
			    
			    ,{field: "Modalidad", title: "UI_MODALIDAD", width: "60px", sortable : false, hidden : true }				       				    
			    ,{field: "ModalidadLiquidacion", title: "UI_MODALIDAD", width: "120px", sortable : false }
			    ,{field: "Plazo", title: "UI_PLAZO_FORWARD", width: "60px", sortable : false }				       				    
			    ,{field: "PlazoResidual", title: "UI_PLAZO_RESIDUAL", width: "80px", sortable : false }				       				    
			    ,{field: "FechaVencimiento", title: "UI_FECHA_VENCIMIENTO", width: "100px", sortable : false }				       				    
			    ,{field: "FechaEfectiva", title: "UI_FECHA_EFECTIVA", width: "80px", sortable : false }
			    ,{field: "Agente", title: "UI_AGENTE", width: "60px", sortable : false, hidden: true }	
			    ,{field: "NombreAgente", title: "UI_NOMBRE_AGENTE", width: "120px", sortable : false }				       				    
			    ,{field: "OperacionFicticia", title: "UI_OPERACION_FICTICIA", width: "90px", sortable : false }				       				    
			    ,{field: "CodigoUsuario", title: "UI_NOMBRE_USUARIO", width: "90px", sortable : false }				       				    
			    ,{field: "Estado", title: "UI_ESTADO", width: "70px", sortable : false }				       				    
		    	,{field: "Mesa", title: "UI_MESA", width: "60px", sortable : false, hidden: true }
			],		  
			sortable		: true,
			pageable		: true,
			filterable		: true,
			navigatable 	: true,
			userSettings 	: true,
			height			: 605,
			excel 			: { allPages: true },
			scrollable		: true,
			selectable 		: "row",
		    autoBind 		: false
       	};
		
		
		/*********************************************
		 * FUNCIONES
		 ********************************************/
						// funcion WebSocket
						$scope.msgWebsocket = function(message) {    	
					    	if(message.Evento==='ACTSPOT' ||message.Evento==='ACTFWD' ){
					    		$scope.data.filtrarMovimientos(1,undefined);
					    	}
					    };
		//funcion BUSCAR
		$scope.data.filtrarMovimientos = function(nroPag, filtrosAnteriores){
			var filtroBusqueda = new Array();
			
			if(filtrosAnteriores === undefined){
				if($scope.data.oConfiguracionMesa.idActual){
		    		filtroBusqueda.push({ field: "Mesa", operator: "eq", value: $scope.data.oConfiguracionMesa.seleccionado.IdTipoProducto });
		    	}
		    	if($scope.data.oConfiguracionTipoInstrumento.seleccionado && 
		    	   $scope.data.oConfiguracionTipoInstrumento.seleccionado.Instrumento !== " TODOS" && 
		    	   $scope.data.oConfiguracionTipoInstrumento.seleccionado.Instrumento !== " UI_TODOS"){
		    		filtroBusqueda.push({ field: "Nemotecnico", operator: "eq", value: $scope.data.oConfiguracionTipoInstrumento.seleccionado.Nemotecnico });
		    	}
		    	if($scope.data.oConfiguracionOperacion.seleccionado && 
		    	   $scope.data.oConfiguracionOperacion.seleccionado.Accion !== " TODOS" && 
		    	   $scope.data.oConfiguracionOperacion.seleccionado.Accion !== " UI_TODOS"){
		    		filtroBusqueda.push({ field: "Accion", operator: "eq", value: $scope.data.oConfiguracionOperacion.seleccionado.Accion });
		    	}
				if($scope.data.kngFechaDesde){
					filtroBusqueda.push({ field: "FechaOperacion", operator: "gte", value: util.dateInicioDia($scope.data.kngFechaDesde) });
				}
				if($scope.data.kngFechaHasta){
					filtroBusqueda.push({ field: "FechaOperacion", operator: "lte", value: util.dateTerminoDia($scope.data.kngFechaHasta) });
				}
			}
			else{
				filtroBusqueda = filtrosAnteriores;
			}
	    	
			
			if($scope.data.tabMovimientoActive === true){
				
				if ($scope.RelacionSpotOperFFMM === true && $scope.data.oConfiguracionMesa.seleccionado.IdTipoProducto === 'MEME')
				{
					$scope.ApiMovimientos.showColumn('IdSolicitudAporteRescate'); 	
					$scope.ApiMovimientos.showColumn('EstadoAporteRescate'); 						
					$scope.ApiMovimientos.showColumn('TipoAporteRescate');
					$scope.ApiMovimientos.showColumn('FechaLiquidacionAporteRescate');
					
					if($scope.data.FmDolar){
						$scope.HabilitoBotonFFMM = true;
						filtroBusqueda.push({ field: "IdSolicitudAporteRescate", operator: "neq", value: 0 });
					}else{$scope.HabilitoBotonFFMM = false;};
					
				}else{
					$scope.ApiMovimientos.hideColumn('IdSolicitudAporteRescate'); 	
					$scope.ApiMovimientos.hideColumn('EstadoAporteRescate'); 						
					$scope.ApiMovimientos.hideColumn('TipoAporteRescate');
					$scope.ApiMovimientos.hideColumn('FechaLiquidacionAporteRescate');    
					
				}
				
				dsGrillaMovimientos.filter(filtroBusqueda);
				if(nroPag !== 1){
					$scope.confMovimientos.dataSource = dsGrillaMovimientos.fetchPage(nroPag);
				}
				$scope.ObjSeleccionGrilla.seleccionGrillaMovimiento = undefined;
				$scope.ObjSeleccionGrilla.seleccionGrillaCartera = undefined;
				$scope.ObjSeleccionGrilla.seleccionGrillaVencimiento = undefined;
				
			}
			else if($scope.data.tabCarteraActive === true){
				if(filtrosAnteriores === undefined){
					filtroBusqueda.push({ field: "Estado", operator: "eq", value: 'VIG' });
					filtroBusqueda.push({ field: "FechaVencimientoFiltro", operator: "gte", value: $scope.data.kngFecha });
				}
				else{
					filtroBusqueda = filtrosAnteriores;
				}
				
				dsGrillaCartera.filter(filtroBusqueda);
				if(nroPag !== 1){
					$scope.confCartera.dataSource = dsGrillaCartera.fetchPage(nroPag);
				}
				$scope.ObjSeleccionGrilla.seleccionGrillaMovimiento = undefined;
				$scope.ObjSeleccionGrilla.seleccionGrillaCartera = undefined;
				$scope.ObjSeleccionGrilla.seleccionGrillaVencimiento = undefined;
			}
			else if($scope.data.tabVencimientoActive === true){
				var filtroBusquedaVencimiento = new Array();
				
				if(filtrosAnteriores === undefined){
					filtroBusquedaVencimiento.push({ field: "FechaFiltroGrillaVcto", operator: "gte", value: util.dateInicioDia($scope.data.kngFechaDesde)  });
					filtroBusquedaVencimiento.push({ field: "FechaFiltroGrillaVcto", operator: "lte", value: util.dateTerminoDia($scope.data.kngFechaHasta) });
					
					if($scope.data.oConfiguracionTipoInstrumento.seleccionado && 
					   $scope.data.oConfiguracionTipoInstrumento.seleccionado.Instrumento != " TODOS" && 
					   $scope.data.oConfiguracionTipoInstrumento.seleccionado.Instrumento != " UI_TODOS"){
						filtroBusquedaVencimiento.push({ field: "Nemotecnico", operator: "eq", value: $scope.data.oConfiguracionTipoInstrumento.seleccionado.Nemotecnico });
					}
					if($scope.data.oConfiguracionOperacion.seleccionado && 
					   $scope.data.oConfiguracionOperacion.seleccionado.Accion != " TODOS" && 
					   $scope.data.oConfiguracionOperacion.seleccionado.Accion != " UI_TODOS"){
						filtroBusquedaVencimiento.push({ field: "Accion", operator: "eq", value: $scope.data.oConfiguracionOperacion.seleccionado.Accion });
					}
				}
				else{
					filtroBusquedaVencimiento = filtrosAnteriores;
				}
				
				dsGrillaVencimiento.filter(filtroBusquedaVencimiento);
				if(nroPag !== 1){
					$scope.confVencimiento.dataSource = dsGrillaVencimiento.fetchPage(nroPag);
				}
				$scope.ObjSeleccionGrilla.seleccionGrillaMovimiento = undefined;
				$scope.ObjSeleccionGrilla.seleccionGrillaCartera = undefined;
				$scope.ObjSeleccionGrilla.seleccionGrillaVencimiento = undefined;
				
			}
		}
		
		
		var cargarOperacion = function(varOpcion){
			if($scope.data.tabMovimientoActive === true && $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento !== undefined && 
			   $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.Mesa === 'MEME'){
				var form = {};
				form.Opcion			= varOpcion;
				form.IdOperacion	= $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.IdOperacion;
				form.IdOperacionUI	= $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.IdOperacionUI;
				form.IdTipoProducto = $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.Mesa;
				form.FechaOperacion = util.dateSPdate($scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.FechaOperacion);

				if(varOpcion === 'MODIFICAR' || varOpcion === 'ANULAR'){
					var filtros = [ { field: "IdOperacionSpot",	operator: "eq", value: $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.IdOperacion },
					                { field: "IdEmpresa",		operator: "eq", value: $scope.$root.SesionIdEmpresa } ];
					ConsultarSpotFacturado.query({ filter: filtros }).then(function(){
						if(ConsultarSpotFacturado.view().length > 0){
							if(ConsultarSpotFacturado.view()[0].PuedeModificarAnular === 0){
								ConsultarSpotFacturado.view()[0].P_OUT_ERRORESCONTADOR = 1;
								bcsComunes.ventanaMsg({
									mensaje:$translate.instant(ConsultarSpotFacturado.view()[0].CodigoModificarAnular),
									conflictos:util.parseServerResponse(ConsultarSpotFacturado.view()[0])
								},idVentana);
								return;
							}
							else{
								var IdVentanamodal = bcsComunes.ventanaAbrir("consutaOperacionSpot", form, false, false, idVentana);
								var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
								promesa.then(function(data){
									$scope.data.filtrarMovimientos(dsGrillaMovimientos.page(), dsGrillaMovimientos.filter());
								});
							}
						}
						else{
							var IdVentanamodal = bcsComunes.ventanaAbrir("consutaOperacionSpot", form, false, false, idVentana);
							var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
							promesa.then(function(data){
								$scope.data.filtrarMovimientos(dsGrillaMovimientos.page(), dsGrillaMovimientos.filter());
							});
						}
					});
				}
				else{
					var IdVentanamodal = bcsComunes.ventanaAbrir("consutaOperacionSpot", form, false, false, idVentana);
					var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
					promesa.then(function(data){
						$scope.data.filtrarMovimientos(dsGrillaMovimientos.page(), dsGrillaMovimientos.filter());
					});
				}
			}
			else if($scope.data.tabMovimientoActive === true && $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento !== undefined && 
					$scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.Mesa === 'MEFW'){
				if($scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.FechaOperacion.getTime() !== $scope.data.kngFecha.getTime() && 
				   (varOpcion === "MODIFICAR" || varOpcion ==="ANULAR")){
					bcsComunes.msg("warning","warning",$translate.instant("Para modificar la operación la fecha debe ser igual a la de sistema."));
					return;
				}
				
				var form = {};
				form.Opcion			= varOpcion;
				form.IdCartera		= $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.IdCartera;
				form.IdOperacion	= $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.FolioOperacion;
				form.IdTipoProducto = $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.Mesa;
				form.FechaOperacion = util.dateSPdate($scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.FechaOperacion);
				form.IdEstado		= $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.IdEstado;
				form.EstaAnticipada	= $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.TipoVencimiento === 'ANTICIPADA' ? true : false;
				
				var IdVentanamodal = bcsComunes.ventanaAbrir("MesaForwardModal", form, false, false, idVentana);
				var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
				promesa.then(function(data){
					$scope.data.filtrarMovimientos(dsGrillaMovimientos.page(), dsGrillaMovimientos.filter());
		        });
			}
			else if($scope.data.tabCarteraActive === true && $scope.ObjSeleccionGrilla.seleccionGrillaCartera !== undefined && 
					$scope.ObjSeleccionGrilla.seleccionGrillaCartera.Mesa === 'MEFW' && varOpcion === "CONSULTAR"){
				var form = {};
				form.Opcion			= varOpcion;
				form.IdCartera		= $scope.ObjSeleccionGrilla.seleccionGrillaCartera.IdCartera;
				form.IdOperacion	= $scope.ObjSeleccionGrilla.seleccionGrillaCartera.FolioOperacion;
				form.IdTipoProducto = $scope.ObjSeleccionGrilla.seleccionGrillaCartera.Mesa;
				form.FechaOperacion = util.dateSPdate($scope.ObjSeleccionGrilla.seleccionGrillaCartera.FechaOperacion);
				form.IdEstado		= $scope.ObjSeleccionGrilla.seleccionGrillaCartera.IdEstado;
				form.EstaAnticipada	= $scope.ObjSeleccionGrilla.seleccionGrillaCartera.TipoVencimiento === 'ANTICIPADA' ? true : false;
				
				var IdVentanamodal = bcsComunes.ventanaAbrir("MesaForwardModal", form, false, false, idVentana);
				var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
				promesa.then(function(data){
					$scope.data.filtrarMovimientos(dsGrillaCartera.page(), dsGrillaCartera.filter());
		        });
			}
			else if($scope.data.tabVencimientoActive === true && $scope.ObjSeleccionGrilla.seleccionGrillaVencimiento !== undefined && 
					$scope.ObjSeleccionGrilla.seleccionGrillaVencimiento.Mesa === 'MEFW' && varOpcion === "CONSULTAR"){
				var form = {};
				form.Opcion			= varOpcion;
				form.IdCartera		= $scope.ObjSeleccionGrilla.seleccionGrillaVencimiento.IdCartera;
				form.IdOperacion	= $scope.ObjSeleccionGrilla.seleccionGrillaVencimiento.FolioOperacion;
				form.IdTipoProducto = $scope.ObjSeleccionGrilla.seleccionGrillaVencimiento.Mesa;
				form.FechaOperacion = util.dateSPdate($scope.ObjSeleccionGrilla.seleccionGrillaVencimiento.FechaOperacion);
				form.IdEstado		= $scope.ObjSeleccionGrilla.seleccionGrillaVencimiento.IdEstado;
				form.EstaAnticipada	= $scope.ObjSeleccionGrilla.seleccionGrillaVencimiento.TipoVencimiento === 'ANTICIPADA' ? true : false;
				
				var IdVentanamodal = bcsComunes.ventanaAbrir("MesaForwardModal", form, false, false, idVentana);
				var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
				promesa.then(function(data){
					$scope.data.filtrarMovimientos(dsGrillaVencimiento.page(), dsGrillaVencimiento.filter());
				});
			}
		};
		$scope.ActualizarSpotFondoMutuo = function() {
			 var IdVentanamodal = bcsComunes.ventanaAbrir("ModificarSpotFondoMutuo", {Prevfecha : util.dateInicioDia($scope.data.kngFechaDesde)}, true, false, idVentana);
				var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
				promesa.then(function(data){
					$scope.data.filtrarMovimientos(1,undefined);
				});
		};

		$scope.ConsultarOperacionUI = function(){
			cargarOperacion('CONSULTAR');
		};
		
		$scope.ModificaOperacionUI = function(){
			cargarOperacion('MODIFICAR');
		};
		
		$scope.AnularOperacionUI = function(){
			cargarOperacion('ANULAR');
		};
		
		$scope.AnticipoOperacionUI = function(){
			AnticipoOperacion();
		};
		
		
		var AnticipoOperacion = function(){
			var form = {};
			form.Opcion			= 'ANTICIPO';
			form.IdCartera		= $scope.ObjSeleccionGrilla.seleccionGrillaCartera.IdCartera;
			form.IdOperacion	= $scope.ObjSeleccionGrilla.seleccionGrillaCartera.FolioOperacion;
			form.IdEstado		= $scope.ObjSeleccionGrilla.seleccionGrillaCartera.IdEstado;
			form.EstaAnticipada	= $scope.ObjSeleccionGrilla.seleccionGrillaCartera.TipoVencimiento === 'ANTICIPADA' ? true : false;
			
			var IdVentanamodal = bcsComunes.ventanaAbrir("MesaForwardModal", form, false, false, idVentana);
			var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
			promesa.then(function(data){
				$scope.data.filtrarMovimientos(dsGrillaCartera.page(), dsGrillaCartera.filter());
	        }); 
			
			$scope.HabilitoBotonAnticipo = false;
		};
		
		
		//REPORTES
		$scope.generarPapeleta = function(){
			var template	= '';
			var serviceId	= '';
			var parametrosRep	= {};
			var parametrosSubRep= {};
			if(tipoOperacion === 'MEME'){
				template	= 'MSP_INFORME_COMPROBANTE_OPERACION';
				serviceId	= '553';
				parametrosRep	= {
					"P_IDEMPRESA"	: $scope.SesionIdEmpresa,
					"P_IDOPERACION"	: idOperacion,
					"$schema"		: [{ "field" : "LogoCorredora", "type" : "blob" }]
				};
				parametrosSubRep= parametrosRep;
			}
			else if(tipoOperacion === 'MEFW'){
				template	= 'FMPAPELETAFWD';
				serviceId	= '567';
				parametrosRep	= {
					"P_IDEMPRESA"		: $scope.SesionIdEmpresa,
					"P_FOLIOOPERACION"	: idCartera,
					"$schema"			: [{ "field" : "LogoCorredora", "type" : "blob" }]
				};
				parametrosSubRep= { "P_IDEMPRESA": $scope.SesionIdEmpresa, "P_IDOPERACION": idCartera };
			}
			else return;
			
			var filterTemplate = [{ field: "ReportName", operator: "eq", value: template }];
			ConsultaTemplate.query({ filter: filterTemplate }).then(function(){
				if(ConsultaTemplate.view().length > 0) {
					$scope.dataTemplate.TipoReporte	= 'pdf';
					$scope.dataTemplate.Id			= ConsultaTemplate.view()[0].Id;
					$scope.dataTemplate.ReportName	= ConsultaTemplate.view()[0].ReportName;
					$scope.dataTemplate.data		= { "serviceId": serviceId, "serviceData": parametrosRep };
					
					var subReports = new Array();
					subReports.push({ "reportName" 	: "FormasDePagoRecibimos"
			              			 ,"data" 		: { "serviceId": '557', "serviceData": parametrosSubRep }
					});
					subReports.push({ "reportName" 	: "FormasDePagoEntregamos"
			              		     ,"data" 		: { "serviceId": '558', "serviceData": parametrosSubRep }
					});
					subReports.push({ "reportName" 	: "Limites"
			              			 ,"data" 		: { "serviceId": '560', "serviceData": parametrosSubRep }
					});
					$scope.dataTemplate.subReports = subReports;
	
					$scope.getReport($scope.dataTemplate);
				}
			});
		};
		
		$scope.generarReportesFWD = function(tipoReporteFWD){
			var template = '';
			var serviceId= '';
			if(tipoReporteFWD === 'CONFIRMACION'){
				template = 'CONFIRMACIONFWD';
				serviceId = '561';
			}
			else if(tipoReporteFWD === 'CONTRATO'){
				template = 'CONTRATOFWD';
				serviceId = '571';
			}
			else return;
			
			var parametros = {
				"P_IDEMPRESA"		: $scope.SesionIdEmpresa,
				"P_FOLIOOPERACION"	: idCartera,
				"$schema"			: [{ "field" : "LogoCorredora", "type" : "blob" }]
			};
			var filterTemplate	= [{ field: "ReportName", operator: "eq", value: template }];
			ConsultaTemplate.query({ filter: filterTemplate }).then(function(){
				if(ConsultaTemplate.view().length > 0) {
					$scope.dataTemplate.TipoReporte	= 'pdf';
					$scope.dataTemplate.Id			= ConsultaTemplate.view()[0].Id;
					$scope.dataTemplate.ReportName	= ConsultaTemplate.view()[0].ReportName;
					$scope.dataTemplate.data		= { "serviceId": serviceId, "serviceData": parametros };
					$scope.dataTemplate.subReports	= new Array();
				
					$scope.getReport($scope.dataTemplate);
				}
			});
		};
		
		$scope.getReport = function(dataTemplate){
			var parameters = {
				"exportFormat"	: dataTemplate.TipoReporte,
				"reportId"		: dataTemplate.Id,
				"parameters"	: "",
				"configuration"	: {
					"mainReport": {
						"reportName": dataTemplate.ReportName,
						"data"		: dataTemplate.data,
						"parameters": {}
					},
					"subReports": dataTemplate.subReports 
				}
			};
			
			$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, {"responseType": "arraybuffer"})
				.success(function (response) {
					var file = new Blob([response], {type: 'application/pdf'});
					var nombreArchivo = dataTemplate.ReportName+'_'+$scope.SesionFechaSistema.replace(/-/g,'');
					saveAs(file, nombreArchivo+"."+dataTemplate.TipoReporte);
					bcsComunes.ventanaOverlay($scope.winID,false);
					$scope.data.activarReporte = false;
				})
				.error(function(response) {
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"),
						conflictos: { errores: [{ Codigo : 'COD', Descripcion : 'Un error inesperado ha ocurrido' }] }
					},idVentana);
				});
		};
	});
}());