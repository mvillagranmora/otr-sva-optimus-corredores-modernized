angular.module('ModuloMesa').controller('ConsultarCarteraCtrl',
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
	
	$scope.data.fechaOptions = {
			max: util.dateTerminoDia($scope.SesionFechaSistema)
	}

	
	//Configurar al principio
	$scope.data.kngFecha = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.data.kngFechaDesde = util.dateInicioDia('1900-01-01');
	$scope.data.kngFechaHasta = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.data.todos = 'N';
	$scope.data.habilita = false;
	$scope.data.HabilitaFecha = true;
	$scope.data.txtFH = false;
    $scope.data.txtFD = false;
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
	$scope.data.oConfiguracionMesa.filtros.IdTipoProducto1.value = '';
    $scope.data.oConfiguracionMesa.filtros.IdTipoProducto2.value = 'MEFW';

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
	//tab Cartera Operaciones Vigentes
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
						HoraOperacion    		: {type: "string"},						
						FechaOperacionOPE		: {type: "systime"},
						NombreInstrumento       : {type: "string"},
						Nemotecnico       		: {type: "string"},
						IdTipoOperacion			: {type: "string"},
						TipoOperacion			: {type: "string"},
						RutCliente				: {type: "string"},
						NombreCliente			: {type: "string"},
						CodigoPropositoPortafolio:{type: "string"},
						Proposito				: {type:"string"},
						IdMonedaPrincipal		: {type: "string"},
						IdMonedaSecundaria		: {type: "string"},
						MontoMonedaPrincipal	: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'',item.Nemotecnico).excelFormat;}},
						MontoMonedaSecundaria	: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'',item.Nemotecnico).excelFormat;}},
						TipoCambioCierre		: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'',item.Nemotecnico).excelFormat;}},
						TipoCambioCosto			: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'',item.Nemotecnico).excelFormat;}},
						ParidadCierre			: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'',item.Nemotecnico).excelFormat;}},
						ParidadCosto			: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'',item.Nemotecnico).excelFormat;}},
						MontoEquivalente		: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'',item.Nemotecnico).excelFormat;}},
						Margen					: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'',item.Nemotecnico).excelFormat;}},
						Utilidad				: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'',item.Nemotecnico).excelFormat;}},
						MontoDiferido			: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'',item.Nemotecnico).excelFormat;}},
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
						INGCantidad				: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'',item.Nemotecnico).excelFormat;}},
						INGMontoMonedaPrincipal	: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'',item.Nemotecnico).excelFormat;}},
						INGMontoMonedaSecundaria: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'',item.Nemotecnico).excelFormat;}},
						//MontoMonedaPrincipalIngreso	: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'','').excelFormat;}},
						//MontoMonedaSecundariaIngreso: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'','').excelFormat;}},
						FechaVencimientoFiltro 	: {type: "sysdate"},
						FolioCarga				: {type: "number"},
						IdMonedaCompensacion	: {type: "string"}
					}
            	}
            }
		}
	};
	
	//tab Cartera Historico Operaciones
	var dsDefConsultaMovimientosFWDHistorico = {
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
            		url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=526",
            		dataType:"json"
            	}
            },
            schema: {
            	model: {
            		fields: {
            			IdOperacion				: {type: "number"},
						FechaOperacion			: {type: "sysdate"},
						FechaLiquidacion		: {type: "sysdate"},
						HoraOperacion   		: {type: "systime"},							
						FechaOperacionOPE		: {type: "systime"},
						NombreInstrumento       : {type: "string"},
						Nemotecnico       		: {type: "string"},
						IdTipoOperacion			: {type: "string"},
						TipoOperacion			: {type: "string"},
						RutCliente				: {type: "string"},
						NombreCliente			: {type: "string"},
						CodigoPropositoPortafolio:{type: "string"},
						Proposito				: {type:"string"},
						IdMonedaPrincipal		: {type: "string"},
						IdMonedaSecundaria		: {type: "string"},
						MontoMonedaPrincipal	: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'',item.Nemotecnico).excelFormat;}},
						MontoMonedaSecundaria	: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'',item.Nemotecnico).excelFormat;}},
						TipoCambioCierre		: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'',item.Nemotecnico).excelFormat;}},
						TipoCambioCosto			: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'',item.Nemotecnico).excelFormat;}},
						ParidadCierre			: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'',item.Nemotecnico).excelFormat;}},
						ParidadCosto			: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'',item.Nemotecnico).excelFormat;}},
						MontoEquivalente		: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'',item.Nemotecnico).excelFormat;}},
						Margen					: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'',item.Nemotecnico).excelFormat;}},
						Utilidad				: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'',item.Nemotecnico).excelFormat;}},
						MontoDiferido			: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'',item.Nemotecnico).excelFormat;}},
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
						INGCantidad				: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'',item.Nemotecnico).excelFormat;}},
						INGMontoMonedaPrincipal	: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'',item.Nemotecnico).excelFormat;}},
						INGMontoMonedaSecundaria: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'',item.Nemotecnico).excelFormat;}},
						FechaVencimientoFiltro 	: {type: "sysdate"},
					}
            	}
            }
		}
	};
	
	var ConsultaTemplate = new bcs.newDataSource({
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
	var ConsultarSpotFacturado = new bcs.newDataSource({
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
	$scope.gridCarteras = {};
	$scope.gridHistorico = false;
	var dsGrillaCartera = new bcs.newDataSource(dsDefConsultaMovimientosFWD);
	var dsGrillaCarteraHistorica = new bcs.newDataSource(dsDefConsultaMovimientosFWDHistorico);
	$scope.confCartera = {
		gridCreated : function(grid) {
			$scope.gridCarteras = grid;
		},
		dataSource: dsGrillaCartera,
		columns: [
		     {field: "IdOperacion", title: "UI_FOLIO_OPERACION", width: "80px", sortable : true }
		    ,{field: "FechaOperacion", title: "UI_FECHA_OPERACION", width: "92px", sortable : true, groupable: true}
		    ,{field: "FechaLiquidacion", title: "UI_FECHA_LIQUIDACION", width: "92px", sortable : true, groupable: true }
		    ,{field: "HoraOperacion", title: "UI_HORA_OPERACION", width: "82px", sortable : true, groupable: true }
		    ,{field: "Nemotecnico", title: "UI_NEMOTECNICO", width: "80px", sortable : true, hidden: true, groupable: true }
		    ,{field: "NombreInstrumento", title: "UI_NOMBRE_TIPO_INSTRUMENTO", width: "130px", sortable : true, groupable: true}
		    ,{field: "IdTipoOperacion", title: "UI_ID_TIPO_OPERACION", width: "90px", sortable : true, hidden: true, groupable: true }				       				    
		    ,{field: "TipoOperacion", title: "UI_TIPO_OPERACION", width: "90px", sortable : true, groupable: true }
		    ,{field: "RutCliente", title: "UI_RUT_CLIENTE", width: "90px", sortable : true }
		    ,{field: "NombreCliente", title: "UI_NOMBRE_CLIENTE", width: "120px", sortable : true }
		    ,{field: "CodigoPropositoPortafolio", title: "UI_PORTAFOLIO", width: "120px", sortable : true }
			,{field: "Proposito", title: "UI_PROPOSITO", width: "120px", sortable : true }
		    ,{field: "IdMonedaPrincipal", title: "UI_MONEDA_PRINCIPAL", width: "90px", sortable : true}
		    ,{field: "IdMonedaSecundaria", title: "UI_MONEDA_SECUNDARIA", width: "100px", sortable : true}
		    //,{field: "MontoMonedaPrincipal", title: "UI_MONTO_MONEDA_PRINCIPAL", width: "100px", sortable : true,	template : " <label> #:  (util.bcs_formato(MontoMonedaPrincipal,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>" }
		    //,{field: "MontoMonedaPrincipalIngreso", title: "UI_MONTO_MONEDA_PRINCIPAL_INGRESO", width: "100px", sortable : true,	template : " <label> #:  (util.bcs_formato(MontoMonedaPrincipalIngreso,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>" }
		    ,{field: "INGMontoMonedaPrincipal", title: "UI_MONTO_MONEDA_PRINCIPAL", width: "100px", sortable : true,	template : " <label> #:  (util.bcs_formato(INGMontoMonedaPrincipal,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'',Nemotecnico)).valor #</label>" }
		    //,{field: "MontoMonedaSecundaria", title: "UI_MONTO_MONEDA_SECUNDARIA", width: "110px", sortable : true,	template : " <label> #:  (util.bcs_formato(MontoMonedaSecundaria,IdTipoInstrumento,'Monto',IdMonedaSecundaria,'','')).valor #</label>" }
		    ,{field: "INGMontoMonedaSecundaria", title: "UI_MONTO_MONEDA_SECUNDARIA", width: "110px", sortable : true,	template : " <label> #:  (util.bcs_formato(INGMontoMonedaSecundaria,IdTipoInstrumento,'Monto',IdMonedaSecundaria,'',Nemotecnico)).valor #</label>" }
		    ,{field: "TipoCambioCierre", title: "UI_TC_CIERRE", width: "60px", sortable : true,					template : " <label> #:  (util.bcs_formato(TipoCambioCierre,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'',Nemotecnico)).valor #</label>" }				       				    
		    ,{field: "TipoCambioCosto", title: "UI_TC_COSTO", width: "60px", sortable : true,						template : " <label> #:  (util.bcs_formato(TipoCambioCosto,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'',Nemotecnico)).valor #</label>" }				       				    
		    ,{field: "ParidadCierre", title: "UI_PARIDAD_CIERRE", width: "90px", sortable : true,					template : " <label> #:  (util.bcs_formato(ParidadCierre,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'',Nemotecnico)).valor #</label>" }				       				    
		    ,{field: "ParidadCosto", title: "UI_PARIDAD_COSTO", width: "90px", sortable : true,					template : " <label> #:  (util.bcs_formato(ParidadCosto,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'',Nemotecnico)).valor #</label>" }				       				    
		    ,{field: "INGMontoMonedaSecundaria", title: "UI_MONTO_EQUIVALENTE", width: "100px", sortable : true,	template : " <label> #:  (util.bcs_formato(INGMontoMonedaSecundaria,IdTipoInstrumento,'Monto',IdMonedaSecundaria,'',Nemotecnico)).valor #</label>" }			       				    
		    ,{field: "Margen", title: "UI_MARGEN", width: "60px", sortable : true,									template : " <label> #:  (util.bcs_formato(Margen,IdTipoInstrumento,'Monto',IdMonedaSecundaria,'',Nemotecnico)).valor #</label>" }				       				    
		    ,{field: "Utilidad", title: "UI_UTILIDAD", width: "80px", sortable : true,								template : " <label> #:  (util.bcs_formato(Utilidad,IdTipoInstrumento,'Monto',IdMonedaSecundaria,'',Nemotecnico)).valor #</label>" }			       				    
		    ,{field: "MontoDiferido", title: "UI_MONTO_DIFERIDO", width: "80px", sortable : true,					template : " <label> #:  (util.bcs_formato(MontoDiferido,IdTipoInstrumento,'Monto',IdMonedaSecundaria,'',Nemotecnico)).valor #</label>" }			       				    
		    
		    ,{field: "Modalidad", title: "UI_MODALIDAD", width: "60px", sortable : true, hidden : true }				       				    
		    ,{field: "ModalidadLiquidacion", title: "UI_MODALIDAD", width: "120px", sortable : true }
		    ,{field: "Plazo", title: "UI_PLAZO_FORWARD", width: "60px", sortable : true }				       				    
		    ,{field: "PlazoResidual", title: "UI_PLAZO_RESIDUAL", width: "80px", sortable : true }				       				    
		    ,{field: "FechaVencimiento", title: "UI_FECHA_VENCIMIENTO", width: "100px", sortable : true }				       				    
		    ,{field: "FechaEfectiva", title: "UI_FECHA_EFECTIVA", width: "80px", sortable : true }
		    ,{field: "Agente", title: "UI_AGENTE", width: "60px", sortable : true, hidden: true }	
		    ,{field: "NombreAgente", title: "UI_NOMBRE_AGENTE", width: "120px", sortable : true }				       				    
		    ,{field: "OperacionFicticia", title: "UI_OPERACION_FICTICIA", width: "90px", sortable : true }				       				    
		    ,{field: "CodigoUsuario", title: "UI_NOMBRE_USUARIO", width: "90px", sortable : true }				       				    
		    ,{field: "Estado", title: "UI_ESTADO", width: "70px", sortable : true }				       				    
	    	,{field: "Mesa", title: "UI_MESA", width: "60px", sortable : true, hidden: true }
	    	,{field: "FolioCarga", title: "UI_FOLIO_OPERACION_CARGA", width: "140px", sortable : true }
	    	,{field: "IdMonedaCompensacion",title: "UI_MONEDA_COMPENSACION",width: "140px",sortable: true , groupable: true }
	    ],		
		sortable		: true,
		pageable		: true,
		filterable		: true,
		navigatable 	: true,
		userSettings 	: true,
		height			: 560,
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
			filtroBusqueda.push({ field: "Estado", operator: "eq", value: 'VIG' });
			if($scope.data.kngFecha){
				filtroBusqueda.push({ field: "FechaVencimientoFiltro", operator: "gt", value: $scope.data.kngFecha });
				
				//IMPCREDOPT-3190
				if(util.dateSPdate($scope.data.kngFecha) !== $scope.SesionFechaSistema){
					filtroBusqueda.push({ field: "FechaRespaldo", operator: "gte", value: util.dateInicioDia($scope.data.kngFecha) });
					filtroBusqueda.push({ field: "FechaRespaldo", operator: "lte", value: util.dateTerminoDia($scope.data.kngFecha) });
				}
	    	}
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
		}
		else{
			filtroBusqueda = filtrosAnteriores;
		}
		
		//IMPCREDOPT-3190
		if($scope.data.kngFecha && util.dateSPdate($scope.data.kngFecha) !== $scope.SesionFechaSistema){
			if(!$scope.gridHistorico){
				$scope.gridCarteras.setOptions({dataSource : dsGrillaCarteraHistorica});
				$scope.gridHistorico = true;
			}
			dsGrillaCarteraHistorica.filter(filtroBusqueda);
	        if(nroPag !== 1){
	            $scope.confCartera.dataSource = dsGrillaCarteraHistorica.fetchPage(nroPag);
	        }
    	}
		else{
			if($scope.gridHistorico){
				$scope.gridCarteras.setOptions({dataSource : dsGrillaCartera});
				$scope.gridHistorico = false;
			}
			dsGrillaCartera.filter(filtroBusqueda);
	        if(nroPag !== 1){
	            $scope.confCartera.dataSource = dsGrillaCartera.fetchPage(nroPag);
	        }
		}
		
		/*if($scope.data.kngFecha && util.dateSPdate($scope.data.kngFecha) !== $scope.SesionFechaSistema){
			//dsDefConsultaMovimientosFWD.definition.transport.read.url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=526";
			dsGrillaCartera.transport.options.read.url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=526";
    	}
		else{
			dsGrillaCartera.transport.options.read.url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=515";
		}*/
		
		/*dsGrillaCartera.filter(filtroBusqueda);
        if(nroPag !== 1){
            $scope.confCartera.dataSource = dsGrillaCartera.fetchPage(nroPag);
        }*/
		
        $scope.ObjSeleccionGrilla.seleccionGrillaCartera = undefined;		
	}
	
	$scope.data.limpiarFiltros = function() {
		$scope.data.oConfiguracionTipoInstrumento.idActual = " TODOS";
		$scope.data.kngFechaDesde = util.dateInicioDia('1900-01-01');
		$scope.data.kngFechaHasta = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.oConfiguracionOperacion.idActual = 0;
		$scope.data.FmDolar = false;
	}

	var cargarOperacion = function(varOpcion){
		if($scope.ObjSeleccionGrilla.seleccionGrillaCartera !== undefined && 
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
	
	$scope.AnticipoOperacionUI = function(){
		AnticipoOperacion();
	};
	
	
	var AnticipoOperacion = function(){
		var form = {};
		form.Opcion			= 'ANTICIPO';
		form.IdCartera		= $scope.ObjSeleccionGrilla.seleccionGrillaCartera.IdCartera;
		form.IdOperacion	= $scope.ObjSeleccionGrilla.seleccionGrillaCartera.FolioOperacion;
		form.IdTipoProducto = $scope.ObjSeleccionGrilla.seleccionGrillaCartera.Mesa;
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