angular.module('ModuloMesa').controller('ConsultarMovimientosCtrl',
	function($scope,$translate,$resource,$timeout,bcsComunes,bcsServiciosMesa,bcsConsultasMesa,bcsServicioPlataforma,$http)
	{	
	
	//Obtiene datos desde la ventana principal
	var idVentana = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay(idVentana,false,1000);
	
	var parametrosVentana = bcsComunes.ventanaParams(idVentana);
	$scope.data = {};
	var ParametrosForward = [];
	var idCartera = 0;
	var rutCliente = '';
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
	$scope.HabilitoBotonModificarFecha = false;
	$scope.HabilitoBotonModificarMultiPantalla = true;
	$scope.data.activarReporte = false;
	/* EDN - Anticipo de Operaciones Forward*/
	$scope.HabilitoBotonAnticipo = false;
	$scope.ObjSeleccionGrilla = {};
	
	$scope.ModificarSpotOrigenFWD = false;
	$scope.RelacionSpotOperFFMM = false;
	$scope.HabilitoCheckFFMM = false;
	$scope.HabilitoBotonFFMM = false;
	$scope.MuestroBotonFFMM = false;

	//Combobox Mesa
	$scope.data.oConfiguracionMesa={
		filtros : {
			logic : "or",
			IdTipoProducto1 : { "field"   : "IdTipoProducto","operator": "eq", "value" : "MEME"},
			IdTipoProducto2	: { "field"   : "IdTipoProducto","operator": "eq", "value" : "MEFW"}
		}
	};
	$scope.data.oConfiguracionMesa.filtros.IdTipoProducto1.value = 'MEME';
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

	$scope.data.CanalPermitidos = [
			{ field: "IdTipoCanalIngreso", operator: "eq", value: "FMM" },
			{ field: "IdTipoCanalIngreso", operator: "eq", value: "MES" },
			{ field: "IdTipoCanalIngreso", operator: "eq", value: "AGE" }
		]
		$scope.data.oConfiguracionCanal = {
			// tipo : "combobox-moneda",
			configuracion : {},
			filtros            : {
				complejo : [$scope.IdEstado, $scope.data.CanalPermitidos],
				  logic: "and",
				  filters: [
				{
					logic: "or",
					filters: $scope.data.CanalPermitidos
				}             
			  ]},    
			idActual : false,
			seleccionado : false,
			idDefecto : ''
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

	$scope.$watch('ObjSeleccionGrilla.seleccionGrillaMovimiento',function(newval,oldval){
		$scope.HabilitoBotonAnular = false;
		$scope.HabilitoBotonModificar = false;
		$scope.HabilitoBotonModificarFecha = false;
		if($scope.ObjSeleccionGrilla.seleccionGrillaMovimiento === undefined){
			return;
		}
		
		idCartera = newval.IdCartera;
		rutCliente = newval.RutCliente;
		folioOperacion = newval.FolioOperacion;
		idOperacion = newval.IdOperacion;
		tipoOperacion = newval.Mesa;
		$scope.data.activarReporte = true;
		
		// si FechaOperacion es menor que FechaProceso botones Modificar y Anular se deshabilitan
		var difFechas = Math.round(util.datediff($scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.FechaOperacionLocal,$scope.data.kngFecha).asDays());
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
			
			if(tipoCanalIngreso === 'FMM'){
				$scope.HabilitoBotonModificar = false;
				$scope.HabilitoBotonModificarFecha = true;
				return;
			}
			
			if($scope.HabilitoBotonAnular && estCnf == ''){
				
				$scope.HabilitoBotonModificar = tipoCanalIngreso === 'FWD' ? $scope.ModificarSpotOrigenFWD : true;
				$scope.HabilitoBotonModificarFecha = tipoCanalIngreso === 'FMM' ? true: false;
	
			}
			
			if($scope.HabilitoBotonAnular && (estCnf == 'PEN' || estCnf == 'NCF') ){
				/*$scope.HabilitoBotonModificar = tipIns === 'SPT' && tipoCanalIngreso === 'FWD' ? $scope.ModificarSpotOrigenFWD : true; */
				$scope.HabilitoBotonModificar = tipIns === 'SPT' && tipoCanalIngreso === 'FWD' ? $scope.ModificarSpotOrigenFWD : true;
				$scope.HabilitoBotonModificarFecha = tipoCanalIngreso === 'FMM' ? true : false;
			}
			/*else{
				$scope.HabilitoBotonModificar = false;
			}*/
		}
	});

		$scope.$watch('data.oConfiguracionMesa.seleccionado.IdTipoProducto', function (newval, oldval) {
			if (newval != oldval && newval == 'MEME' && $scope.RelacionSpotOperFFMM === true) {
				$scope.HabilitoCheckFFMM = true;
			} else {
				$scope.HabilitoCheckFFMM = false;
				$scope.data.ffmmDolar = false;
			};

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
                { field: "IdCartera",			dir: "desc" }
                //{ field: "FechaOperacionOPE",	dir: "desc" },
            ],
            pageSize: 25,
            transport: {
            	read: {
            		url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700187",
            		dataType:"json"
            	}
            },
            schema: {
            	model: {
            		fields: {
            			IdOperacion							: {type: "number"},
            			FechaModificacion					: {type: "date"},
						HoraModificacion					: {type: "string"},
						UsuarioMod							: {type: "string"},
            			IdOperacionUI						: {type: "number"},
            			FolioOrigenFW						: {type: "number"},
						FechaOperacionLocal					: {type: "date"},
						FechaLiquidacion					: {type: "date"},							
						FechaEntregamos  					: {type: "date"},
						FechaRecibimos  					: {type: "date"},
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
						MontoMonedaPrincipal				: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'',item.Nemotecnico).excelFormat;}},
						MontoMonedaSecundaria				: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'',item.Nemotecnico).excelFormat;}},
						ResultadoMonedaLocalPuntas 			: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto','CLP','',item.Nemotecnico).excelFormat;}},
						ResultadoMonedaLocalBidAsk 			: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto','CLP','',item.Nemotecnico).excelFormat;}},
						ResultadoMonedaLocalDO 				: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto','CLP','',item.Nemotecnico).excelFormat;}},
						ResultadoMonedaLocalTCSpot 			: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto','CLP','',item.Nemotecnico).excelFormat;}},
						ResultadoMonedaLocalTCCierreMesa	: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto','CLP','',item.Nemotecnico).excelFormat;}},
						TipoCambioCierre					: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'',item.Nemotecnico).excelFormat;}},
						TipoCambioCosto						: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'',item.Nemotecnico).excelFormat;}},
						ParidadCierre						: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'',item.Nemotecnico).excelFormat;}},
						ParidadCosto						: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'',item.Nemotecnico).excelFormat;}},
						MontoEquivalente					: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'',item.Nemotecnico).excelFormat;}},
						Margen								: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'',item.Nemotecnico).excelFormat;}},
						Utilidad							: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'',item.Nemotecnico).excelFormat;}},
						MontoDiferido						: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'',item.Nemotecnico).excelFormat;}},
						Modalidad							: {type: "string"},
						ModalidadLiquidacion				: {type: "string"},
						Plazo								: {type: "number"},
						PlazoResidual						: {type: "number"},
						FechaVencimiento					: {type: "date"},
						FechaEfectiva						: {type: "date"},
						Agente								: {type: "number"},
						NombreAgente						: {type: "string"},
						OperacionFicticia					: {type: "string"},
						CodigoUsuario						: {type: "string"},
						Estado								: {type: "string"},
						EstadoConfirmacion					: {type: "string"},
						Mesa								: {type: "string"},
						IdTipoCanalIngreso					: {type: "string"},
						INGCantidad							: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'',item.Nemotecnico).excelFormat;}},
						INGMontoMonedaPrincipal				: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'',item.Nemotecnico).excelFormat;}},
						INGMontoMonedaSecundaria			: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'',item.Nemotecnico).excelFormat;}},
						IdOperacionSpotReferencia			: {type: "number"},
						IdOperacionSpotOrigen				: {type: "number"},
						IdOperacionSpotVigente				: {type: "number"},
						MontoCalce							: {type: "number"},
						MontoDisponible						: {type: "number"},
						IdAsignacion						: {type: "number"},
						
						//Fondos mutuos
						IdSolicitudAporteRescate			: {type: "number"},
						EstadoAporteRescate					: {type: "string"}, 
						TipoAporteRescate					: {type: "string"},
						FechaLiquidacionAporteRescate		: {type: "date"},
						FechaRespaldo						: {type: "date"},
						Glosa								: {type: "string"},
						InstrumentoFM						: {type: "string"},
						AfectoADerivado						: {type: "string"},
						DolarObservado						: {type: "string"},

						//Plataforma Negociacion
						IdPlataformaNegociacion			: {type: "string"},
						IdMonedaCompensacion			: {type: "string"},

						// Formas de Pago
						FormaPagoEntregamos 				: {type: "string"},
						FormaPagoRecibe 					: {type: "string"},
						NombreEmpleadoDigitador				: {type: "string"}
            		}
            	}
        	},
			 /*aggregate			: [ //Para resumen de totales
		       { field: "MontoMonedaPrincipal", aggregate: "sum" }
		      ,{ field: "IdMonedaSecundaria", aggregate: "sum" }] 
            */
            
            
            
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
	$scope.gridMovimientosMesa = {};
	$scope.gridHistoricoMovimiento = false;
	var dsGrillaMovimientos = new bcs.newDataSource(dsDefConsultaMovimientosMesa);
	$scope.confMovimientos = {
		dataSource: dsGrillaMovimientos,
		columns: [
		     //{field: "IdOperacion", title: "UI_FOLIO_OPERACION", width: "80px", sortable : true }
		     {field: "IdOperacionUI", title: "UI_FOLIO_OPERACION", width: "80px", sortable : true }
		    ,{field: "FolioOrigenFW", title: "UI_FOLIO_FORWARD_ORIGEN", width: "95px", sortable : true }
		    ,{field: "FechaOperacionLocal", title: "UI_FECHA_OPERACION", width: "92px", sortable : false, groupable: true}
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
		    ,{field: "MontoMonedaPrincipal", title: "UI_MONTO_MONEDA_PRINCIPAL", width: "100px", sortable : false,				template : " <label> #:  (util.bcs_formato(MontoMonedaPrincipal,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'',Nemotecnico)).valor #</label>",aggregates: ["sum"], groupFooterTemplate: function(group) {
				// Aquí puedes acceder a `group` y obtener el valor de `IdMonedaPrincipal`
				var moneda = group.value;  // El valor del grupo
				var totalSum = group.aggregates.MontoMonedaPrincipal.sum;  // La suma del grupo
				if(group.field=='IdMonedaPrincipal'){
					// Formatear 
					return util.bcs_formato(totalSum, 'GEN', 'Monto', moneda, '', '').valor;
				}else{
					return util.formateaNumero(totalSum,2);
				}		
			  }, footerAttributes:{style:"text-align: right; padding-right:0;"}}

		    ,{field: "MontoMonedaSecundaria", title: "UI_MONTO_MONEDA_SECUNDARIA", width: "110px", sortable : false,			template : " <label> #:  (util.bcs_formato(MontoMonedaSecundaria,IdTipoInstrumento,'Monto',IdMonedaSecundaria,'',Nemotecnico)).valor #</label>",aggregates: ["sum"], groupFooterTemplate: function(group) {
				var moneda = group.value;  
				var totalSum = group.aggregates.MontoMonedaSecundaria.sum;  
				if(group.field=='IdMonedaSecundaria'){
					return util.bcs_formato(totalSum, 'GEN', 'Monto', moneda, '', '').valor;
				}else{
					return util.formateaNumero(totalSum,2);
				}		
			}, footerAttributes:{style:"text-align: right; padding-right:0;"}}				       				    
		    //,{field: "INGMontoMonedaSecundaria", title: "UI_MONTO_MONEDA_SECUNDARIA", width: "110px", sortable : false,			template : " <label> #:  (util.bcs_formato(INGMontoMonedaSecundaria,IdTipoInstrumento,'Monto',IdMonedaSecundaria,'','')).valor #</label>" }				       				    
				,{field: "ResultadoMonedaLocalPuntas", title: "UI_RESULTADO_MONEDA_LOCAL_PUNTAS", width: "110px", sortable : false,	template : " <label> #:  (util.bcs_formato(ResultadoMonedaLocalPuntas,IdTipoInstrumento,'Monto','CLP','',Nemotecnico)).valor #</label>" }
				,{field: "ResultadoMonedaLocalBidAsk", title: "UI_RESULTADO_MONEDA_LOCAL_BIASK", width: "110px", sortable : false,	template : " <label> #:  (util.bcs_formato(ResultadoMonedaLocalBidAsk,IdTipoInstrumento,'Monto','CLP','',Nemotecnico)).valor #</label>" }
				,{field: "ResultadoMonedaLocalDO", title: "UI_RESULTADO_MONEDA_LOCAL_DO", width: "110px", sortable : false,			template : " <label> #:  (util.bcs_formato(ResultadoMonedaLocalDO,IdTipoInstrumento,'Monto','CLP','',Nemotecnico)).valor #</label>" }
				,{field: "ResultadoMonedaLocalTCSpot", title: "UI_RESULTADO_MONEDA_LOCAL_TCSPOT", width: "110px", sortable : false,	template : " <label> #:  (util.bcs_formato(ResultadoMonedaLocalTCSpot,IdTipoInstrumento,'Monto','CLP','',Nemotecnico)).valor #</label>" }
				,{field: "ResultadoMonedaLocalTCCierreMesa", title: "UI_RESULTADO_MONEDA_LOCAL_TCCIERREMESA", width: "110px", sortable : false 	, template : " <label> #:  (util.bcs_formato(ResultadoMonedaLocalTCCierreMesa,IdTipoInstrumento,'Monto','CLP','',Nemotecnico)).valor #</label>" }				       				 
		    ,{field: "TipoCambioCierre", title: "UI_TC_CIERRE", width: "60px", sortable : false,								template : " <label> #:  (util.bcs_formato(TipoCambioCierre,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'',Nemotecnico)).valor #</label>" }				       				    
		    ,{field: "TipoCambioCosto", title: "UI_TC_COSTO", width: "60px", sortable : false,									template : " <label> #:  (util.bcs_formato(TipoCambioCosto,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'',Nemotecnico)).valor #</label>" }				       				    
		    ,{field: "ParidadCierre", title: "UI_PARIDAD_CIERRE", width: "90px", sortable : false,								template : " <label> #:  (util.bcs_formato(ParidadCierre,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'',Nemotecnico)).valor #</label>" }				       				    
		    ,{field: "ParidadCosto", title: "UI_PARIDAD_COSTO", width: "90px", sortable : false,								template : " <label> #:  (util.bcs_formato(ParidadCosto,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'',Nemotecnico)).valor #</label>" }				       				    
		    ,{field: "INGMontoMonedaSecundaria", title: "UI_MONTO_EQUIVALENTE", width: "100px", sortable : false,				template : " <label> #:  (util.bcs_formato(INGMontoMonedaSecundaria,IdTipoInstrumento,'Monto',IdMonedaSecundaria,'',Nemotecnico)).valor #</label>" }				       				    
		    ,{field: "Margen", title: "UI_MARGEN", width: "60px", sortable : false,												template : " <label> #:  (util.bcs_formato(Margen,IdTipoInstrumento,'Monto',IdMonedaSecundaria,'',Nemotecnico)).valor #</label>" }				       				    
		    ,{field: "Utilidad", title: "UI_UTILIDAD", width: "80px", sortable : false,											template : " <label> #:  (util.bcs_formato(Utilidad,IdTipoInstrumento,'Monto',IdMonedaSecundaria,'',Nemotecnico)).valor #</label>" }				       				    
		    ,{field: "MontoDiferido", title: "UI_MONTO_DIFERIDO", width: "80px", sortable : false,								template : " <label> #:  (util.bcs_formato(MontoDiferido,IdTipoInstrumento,'Monto',IdMonedaSecundaria,'',Nemotecnico)).valor #</label>" }				       				    
		    ,{field: "Modalidad", title: "UI_MODALIDAD", width: "60px", sortable : false, hidden : true }				       				    
		    ,{field: "ModalidadLiquidacion", title: "UI_MODALIDAD", width: "120px", sortable : false }
		    ,{field: "Plazo", title: "UI_PLAZO_FORWARD", width: "30px", sortable : false }				       				    
		    ,{field: "PlazoResidual", title: "UI_PLAZO_RESIDUAL", width: "80px",  sortable : false }				       				    
		    ,{field: "FechaVencimiento", title: "UI_FECHA_VENCIMIENTO", width: "100px",  sortable : false }				       				    
		    ,{field: "FechaEfectiva", title: "UI_FECHA_EFECTIVA", width: "80px",  sortable : false }
		    ,{field: "Agente", title: "UI_AGENTE", width: "60px",  sortable : false, hidden: true , groupable: true}	
		    ,{field: "NombreAgente", title: "UI_NOMBRE_AGENTE", width: "120px",  sortable : false }	
			,{field: "Glosa", title: "UI_OBSERVACIONES", width: "120px",  sortable : false }					       				    
		    ,{field: "OperacionFicticia", title: "UI_OPERACION_FICTICIA", width: "90px",  sortable : false }				       				    
		    ,{field: "CodigoUsuario", title: "UI_NOMBRE_USUARIO", width: "90px",  sortable : false }				       				    
		    ,{field: "Estado", title: "UI_ESTADO", width: "40px", sortable : false , groupable: true}
		    ,{field: "EstadoConfirmacion", title: "UI_ESTADO_CONFIRMACION_PAGOS", width: "50px", sortable : false , groupable: true}				       				    
	    	,{field: "Mesa", title: "UI_MESA", width: "40px",  sortable : false , groupable: true}
	    	,{field: "IdOperacionSpotReferencia", title: "UI_FOLIO_REFERENCIA", width: "80px", sortable : true }
	    	,{field: "IdAsignacion", title: "UI_SECUENCIA_ASIGNACION_RV", width: "50px", sortable : false , groupable: true}	    	
	    	//Fondos mutuos
	    	,{field: "IdSolicitudAporteRescate", title: "UI_SECUENCIA_FM", width: "80px", sortable : true, hidden: true }
	    	,{field: "EstadoAporteRescate", title: "UI_ESTADO_FM", width: "80px", sortable : true, hidden: true }
	    	,{field: "TipoAporteRescate", title: "UI_TIPO_FM", width: "80px", sortable : true, hidden: true }
	    	,{field: "FechaLiquidacionAporteRescate", title: "UI_FECHA_LIQUIDACION_FM", width: "80px", sortable : true, hidden: true }
			,{field: "InstrumentoFM", title: "UI_INSTRUMENTO_FM", width: "90px", sortable : false, groupable: true}
			,{field: "AfectoADerivado", title: "UI_AFECTO_A_DERIVADO", width: "50px", sortable : true }
	    	,{field: "DolarObservado", title: "UI_DOLAR_OBSERVADO", width: "50px", sortable : true }
	    	,{field: "FechaModificacion", title: "UI_FECHA_MODIFICACION", width: "92px", sortable : true , hidden: false }
			,{field: "HoraModificacion", title: "UI_HORA_MODIFICACION", width: "80px", sortable : true , hidden: false }
			,{field: "UsuarioMod", title: "UI_USUARIO_MODIFICACION", width: "92px", sortable : true , hidden: false }

			//Plataforma negociacion}
			,{field: "IdPlataformaNegociacion",title: "UI_PLATAFORMA_NEGOCIACION",width: "120px",sortable: false}
	    ,{field: "IdMonedaCompensacion",title: "UI_MONEDA_COMPENSACION",width: "140px",sortable: true , groupable: true }

			// FORMAS DE PAGO
			,{field: "FormaPagoEntregamos", title: "UI_FORMA_PAGO_ENTREGAMOS", width: "200px", sortable : true , hidden: false }
			,{field: "FormaPagoRecibe", title: "UI_FORMA_PAGO_RECIBIMOS", width: "200px", sortable : true , hidden: false }
			,{field: "NombreEmpleadoDigitador", title: "UI_EMPLEADO_DIGITADOR", width: "170px", sortable : true, hidden: true }
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
	    	if($scope.data.oConfiguracionCanal.seleccionado && 
	    	   $scope.data.oConfiguracionCanal.seleccionado.IdTipoCanalIngreso !== 0){
	    		filtroBusqueda.push({ field: "IdTipoCanalIngreso", operator: "eq", value: $scope.data.oConfiguracionCanal.seleccionado.IdTipoCanalIngreso});
	    	}
	    	if($scope.data.oConfiguracionOperacion.seleccionado && 
	    	   $scope.data.oConfiguracionOperacion.seleccionado.Accion !== " TODOS" && 
	    	   $scope.data.oConfiguracionOperacion.seleccionado.Accion !== " UI_TODOS"){
	    		filtroBusqueda.push({ field: "Accion", operator: "eq", value: $scope.data.oConfiguracionOperacion.seleccionado.Accion });
	    	}	    	
	    	
			if($scope.data.kngFechaDesde){
				filtroBusqueda.push({ field: "FechaOperacionLocal", operator: "gte", value: $scope.data.kngFechaDesde });
			}
			if($scope.data.kngFechaHasta){
	    	 	if (util.dateTerminoDia($scope.data.kngFechaHasta)  < $scope.data.kngFechaDesde)
	    	 	{
	    	 		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("FECHA_TERMINO_RANGO_CONSULTA_INVALIDA"));
	    	 		return;
	    	 	}
				filtroBusqueda.push({ field: "FechaOperacionLocal", operator: "lte", value: util.dateTerminoDia($scope.data.kngFechaHasta) });
			}
			
			//IMPCREDOPT-3190
			if(util.dateSPdate($scope.data.kngFecha) !== $scope.SesionFechaSistema){
				filtroBusqueda.push({"logic": "or", filters:[{ field: "FechaRespaldo", operator: "gte", value: util.dateInicioDia($scope.data.kngFecha)},{ field: "Mesa", operator: "eq", value: 'MEME' }]});
				filtroBusqueda.push({"logic": "or", filters:[{ field: "FechaRespaldo", operator: "lte", value: util.dateTerminoDia($scope.data.kngFecha)},{ field: "Mesa", operator: "eq", value: 'MEME' }]});
			} else {
                filtroBusqueda.push({"logic": "or", filters:[{ field: "FechaRespaldo", operator: "eq", value: '0001-01-01'},{ field: "Mesa", operator: "eq", value: 'MEME' }]});			
			}
			//IMPSURAOPT-583 se cambia aca este push pues aqui se debe hacer
			if ($scope.RelacionSpotOperFFMM === true && $scope.data.oConfiguracionMesa.seleccionado.IdTipoProducto === 'MEME' && $scope.data.ffmmDolar?.seleccionado?.Codigo === 'GENERAL') {
				filtroBusqueda.push({ field: "IdSolicitudAporteRescate", operator: "neq", value: 0 });	
				filtroBusqueda.push({ field: "FondosNacionalesValutaCero", operator: "eq", value: 'N' });	
			}

			if ($scope.RelacionSpotOperFFMM === true && $scope.data.oConfiguracionMesa.seleccionado.IdTipoProducto === 'MEME' && $scope.data.ffmmDolar?.seleccionado?.Codigo === 'NACIONALES_VALUTA_CERO') {
				filtroBusqueda.push({ field: "IdSolicitudAporteRescate", operator: "neq", value: 0 });
				filtroBusqueda.push({ field: "FondosNacionalesValutaCero", operator: "eq", value: 'S' });
			}
		}
		else{
			filtroBusqueda = filtrosAnteriores;
		}

		if ($scope.RelacionSpotOperFFMM === true && $scope.data.oConfiguracionMesa.seleccionado.IdTipoProducto === 'MEME')
		{
			$scope.ApiMovimientos.showColumn('IdSolicitudAporteRescate'); 	
			$scope.ApiMovimientos.showColumn('EstadoAporteRescate'); 						
			$scope.ApiMovimientos.showColumn('TipoAporteRescate');
			$scope.ApiMovimientos.showColumn('FechaLiquidacionAporteRescate');

			const fmmDolar = $scope.data.ffmmDolar?.seleccionado?.Codigo || 0;
			$scope.HabilitoBotonFFMM = fmmDolar !== 0 && $scope.data.oConfiguracionOperacion.idActual!=0? true : false;
			$scope.MuestroBotonFFMM = fmmDolar !== 0 ? true : false;
		}else{
			$scope.ApiMovimientos.hideColumn('IdSolicitudAporteRescate'); 	
			$scope.ApiMovimientos.hideColumn('EstadoAporteRescate'); 						
			$scope.ApiMovimientos.hideColumn('TipoAporteRescate');
			$scope.ApiMovimientos.hideColumn('FechaLiquidacionAporteRescate');    
		}
		
		//IMPCREDOPT-3190
		if($scope.data.kngFecha && util.dateSPdate($scope.data.kngFecha) !== $scope.SesionFechaSistema){
			if(!$scope.gridHistoricoMovimiento){
				$scope.gridHistoricoMovimiento = true;
			}
			dsGrillaMovimientos.filter(filtroBusqueda);
	        if(nroPag !== 1){
	            $scope.confMovimientos.dataSource = dsGrillaMovimientos.fetchPage(nroPag);
	        }
    	}
		else{
			if($scope.gridHistoricoMovimiento){
				$scope.gridHistoricoMovimiento = false;
			}
			dsGrillaMovimientos.filter(filtroBusqueda);
			if(nroPag !== 1){
				$scope.confMovimientos.dataSource = dsGrillaMovimientos.fetchPage(nroPag);
			}
		}
		$scope.ObjSeleccionGrilla.seleccionGrillaMovimiento = undefined;
	}
	
	$scope.data.limpiarFiltros = function() {
		$scope.data.oConfiguracionMesa.idActual = 0;
		$scope.data.oConfiguracionTipoInstrumento.idActual = " TODOS";
		$scope.data.kngFechaDesde = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.kngFechaHasta = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.oConfiguracionOperacion.idActual = 0;
		$scope.data.oConfiguracionCanal.idActual = 0;
		$scope.data.ffmmDolar = false;
		$scope.HabilitoBotonFFMM = false;
		$scope.MuestroBotonFFMM = false;
		$scope.data.HabilitoBotonModificarFecha = false;
	}
	
	var cargarOperacion = function(varOpcion){
		if($scope.ObjSeleccionGrilla.seleccionGrillaMovimiento !== undefined && $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.Mesa === 'MEME'){
			var form = {};
			form.Opcion			= varOpcion;
			form.IdOperacion	= $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.IdOperacion;
			form.IdOperacionUI	= $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.IdOperacionUI;
			form.IdTipoProducto = $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.Mesa;
			form.FechaOperacion = util.dateSPdate(Date.parse($scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.FechaOperacionLocal));

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
		else if($scope.ObjSeleccionGrilla.seleccionGrillaMovimiento !== undefined && $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.Mesa === 'MEFW'){
			if($scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.FechaOperacionLocal.getTime() !== $scope.data.kngFecha.getTime() && 
			   (varOpcion === "MODIFICAR" || varOpcion ==="ANULAR")){
				bcsComunes.msg("warning","warning",$translate.instant("Para modificar la operación la fecha debe ser igual a la de sistema."));
				return;
			}
			
			var form = {};
			form.Opcion			= varOpcion;
			form.IdCartera		= $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.IdCartera;
			form.IdOperacion	= $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.FolioOperacion;
			form.IdTipoProducto = $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.Mesa;
			form.FechaOperacion = util.dateSPdate(Date.parse($scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.FechaOperacionLocal));
			form.IdEstado		= $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.IdEstado;
			form.EstaAnticipada	= $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.TipoVencimiento === 'ANTICIPADA' ? true : false;
			
			var IdVentanamodal = bcsComunes.ventanaAbrir("MesaForwardModal", form, false, false, idVentana);
			var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
			promesa.then(function(data){
				$scope.data.filtrarMovimientos(dsGrillaMovimientos.page(), dsGrillaMovimientos.filter());
	        });
		}
	};
	
	var cargarOperacion = function(varOpcion){
		if($scope.ObjSeleccionGrilla.seleccionGrillaMovimiento !== undefined && $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.Mesa === 'MEME'){
			var form = {};
			form.Opcion			= varOpcion;
			form.IdOperacion	= $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.IdOperacion;
			form.IdOperacionUI	= $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.IdOperacionUI;
			form.IdTipoProducto = $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.Mesa;
			form.FechaOperacion = util.dateSPdate($scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.FechaOperacionLocal);

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
								$scope.HabilitoBotonModificarMultiPantalla = true;
								$scope.data.filtrarMovimientos(dsGrillaMovimientos.page(), dsGrillaMovimientos.filter());
							});
						}
					}
					else{
						var IdVentanamodal = bcsComunes.ventanaAbrir("consutaOperacionSpot", form, false, false, idVentana);
						var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
						promesa.then(function(data){
							$scope.HabilitoBotonModificarMultiPantalla = true;
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
		else if($scope.ObjSeleccionGrilla.seleccionGrillaMovimiento !== undefined && $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.Mesa === 'MEFW'){
			if($scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.FechaOperacionLocal.getTime() !== $scope.data.kngFecha.getTime() &&
			   (varOpcion === "MODIFICAR" || varOpcion ==="ANULAR")){
				bcsComunes.msg("warning","warning",$translate.instant("Para modificar la operación la fecha debe ser igual a la de sistema."));
				return;
			}
			
			var form = {};
			form.Opcion			= varOpcion;
			form.IdCartera		= $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.IdCartera;
			form.IdOperacion	= $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.FolioOperacion;
			form.IdTipoProducto = $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.Mesa;
			form.FechaOperacion = util.dateSPdate($scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.FechaOperacionLocal);
			form.IdEstado		= $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.IdEstado;
			form.EstaAnticipada	= $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.TipoVencimiento === 'ANTICIPADA' ? true : false;
			
			var IdVentanamodal = bcsComunes.ventanaAbrir("MesaForwardModal", form, false, false, idVentana);
			var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
			promesa.then(function(data){
				$scope.data.filtrarMovimientos(dsGrillaMovimientos.page(), dsGrillaMovimientos.filter());
	        });
		}
	};

	$scope.ActualizarSpotFondoMutuo = function() {
		var IdVentanamodal = bcsComunes.ventanaAbrir("ModificarSpotFondoMutuo", {Prevfecha : util.dateInicioDia($scope.data.kngFechaDesde),TipoOperacion:$scope.data.oConfiguracionOperacion, FondoMutuoDolar: $scope.data.ffmmDolar.seleccionado.Codigo }, true, false, idVentana);
			var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
			promesa.then(function(data){
				$scope.data.filtrarMovimientos(1,undefined);
			});
	};

	$scope.ConsultarOperacionUI = function(){
		cargarOperacion('CONSULTAR');
	};
	
	$scope.ModificaOperacionUI = function(){	
		
		var tipoCanalIngreso = $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.IdTipoCanalIngreso;	
		var existeMontoDisponible = $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.MontoDisponible === 0 ? false : true;
		
		if(tipoCanalIngreso == "DATATEC"){
			bcsComunes.msg("warning","warning",$translate.instant("La operación no puede ser modificada si el Canal pertenece a DATATEC."));
		}
		else if(!existeMontoDisponible && $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.IdTipoInstrumento !== 'SPT'){
			bcsComunes.msg("warning","warning",$translate.instant("La operación no puede ser modificada ya que se encuentra calzada."));	
		}
		else{
			$scope.HabilitoBotonModificarMultiPantalla = false;	
			cargarOperacion('MODIFICAR');
		}
	};
	
	
	$scope.ModificarFechaOperacionUI = function(){	
		var datos = [];		
		var seleccionEstado = $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.IdEstado;
		var tipocanalingreso = $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.IdTipoCanalIngreso;
		
		
		if (seleccionEstado !== 'VIG'){
			if (tipocanalingreso!== 'FMM' && seleccionEstado !== 'PEN'){
				bcsComunes.msg("error","error",$translate.instant("Para modificar la fecha de la operación debe estar en estado vigente."));
				return;
			}
		}
		
		datos.IdOperacion = $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.IdOperacion;
		datos.FechaOperacion = $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.FechaOperacionLocal;
		
		var id1 = bcsComunes.ventanaAbrir(
				"ConsultaOperacionesModificarFecha", datos,
				false, false, idVentana);
		var promesa = bcsComunes.ventanaPromesa(id1);
		promesa.then(function(data) {
			if (data === true) {
				data.filtrarMovimientos();
			}
		});
	};
	
	
	$scope.AnularOperacionUI = function(){
		
		var tipoCanalIngreso = $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.IdTipoCanalIngreso;	
		var IdSolicitud=  $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.IdSolicitudAporteRescate;
		var TipoSolicitud =  $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.TipoAporteRescate;
		var EstadoAporteRescate = $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.EstadoAporteRescate;
		
		if(tipoCanalIngreso == "DATATEC"){
			bcsComunes.msg("warning","warning",$translate.instant("La operación no puede ser anulada si el Canal pertenece a DATATEC."));
		}else{
			$scope.HabilitoBotonModificarMultiPantalla = false;
			cargarOperacion('ANULAR');
		}
	};

	//REPORTES
	$scope.generarPapeleta = function(){
		var template	= '';
		var serviceId	= '';
		var parametrosRep	= {};
		var parametrosSubRep= {};
		var parametrosFormasDePago = {};
		if(tipoOperacion === 'MEME'){
			template	= 'MSP_INFORME_COMPROBANTE_OPERACION';
			serviceId	= '553';
			parametrosRep	= {
				"P_IDEMPRESA"	: $scope.SesionIdEmpresa,
				"P_IDOPERACION"	: idOperacion,
				"$schema"		: [{ "field" : "LogoCorredora", "type" : "blob" }]
			};
			parametrosSubRep= parametrosRep;
			parametrosFormasDePago = {
				"P_IDEMPRESA"	: $scope.SesionIdEmpresa,
				"P_IDOPERACION"	: idOperacion,
				"P_IDTIPOPRODUCTO" : tipoOperacion,
				"$schema"		: [{ "field" : "LogoCorredora", "type" : "blob" }]
			};
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
			parametrosFormasDePago = { 
				"P_IDEMPRESA": $scope.SesionIdEmpresa, 
				"P_IDOPERACION": idCartera, 
				"P_IDTIPOPRODUCTO" : tipoOperacion 
			};
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
		              			 ,"data" 		: { "serviceId": '557', "serviceData": parametrosFormasDePago }
				});
				subReports.push({ "reportName" 	: "FormasDePagoEntregamos"
		              		     ,"data" 		: { "serviceId": '558', "serviceData": parametrosFormasDePago }
				});
				subReports.push({ "reportName" 	: "Limites"
		              			 ,"data" 		: { "serviceId": '560', "serviceData": parametrosSubRep }
				});
				$scope.dataTemplate.subReports = subReports;

				$scope.getReport($scope.dataTemplate);
			}
		});
	};
	
	const abrirModal = function(parametros){
		var template = 'CONTRATOFWD';
		var serviceId = '571';
		parametros.template = template;
		parametros.serviceId = serviceId;
		bcsComunes.ventanaAbrir("GenerarContrato", parametros, false, false, idVentana);
	}

	$scope.generarReportesFWD = function(tipoReporteFWD){
		var template = '';
		var serviceId= '';
		var parametros = {
			"P_IDEMPRESA"		: $scope.SesionIdEmpresa,
			"P_FOLIOOPERACION"	: idCartera,
			"P_RUTCLIENTE"      : rutCliente,
			"$schema"			: [{ "field" : "LogoCorredora", "type" : "blob" }]
		};
		if(tipoReporteFWD === 'CONFIRMACION'){
			template = 'CONFIRMACIONFWD';
			serviceId = '561';
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
		}
		else if(tipoReporteFWD === 'CONTRATO'){
			if($scope.ObjSeleccionGrilla.seleccionGrillaMovimiento && $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.Estado && $scope.ObjSeleccionGrilla.seleccionGrillaMovimiento.Estado === 'ANU'){
				bcsComunes.msg("error","error",$translate.instant("ERROR_GENERAR_CONTRATO_MOVIMIENTO_ANULADO"));
			}else{
				abrirModal(parametros);
			}
		}
		else return;
		
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
				bcsComunes.ventanaOverlay(idVentana,false);
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
