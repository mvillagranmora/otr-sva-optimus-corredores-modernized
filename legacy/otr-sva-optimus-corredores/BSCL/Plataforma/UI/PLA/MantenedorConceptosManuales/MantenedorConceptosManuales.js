angular.module('ModuloPlataforma').controller('MantenedorConceptosManualesCtrl',
		function($scope,$translate,bcsComunes,bcsServicioPlataforma)
{
	
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	$scope.data = {}; 

	$scope.data.selTipoConcepto={
		tipo : "combobox-tipo-concepto",
		configuracion : {},
		filtros : {},
		idActual 		: false,
		seleccionado 	: false,
		idDefecto 		: 'MANUAL',
		idPostFiltro 	: false
	};

	var dsConceptos = new bcs.DataSource({
        type : "odata",
		definition : {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.
			pageSize 	        : 20,
			sort 				: [],
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1786",dataType : "json"}},
			schema : {
				model : {
					fields : {								
						IdConcepto							: {type : "string"},
						TipoConcepto						: {type : "string"},
						NombreConcepto						: {type : "string"},						
						IdEstado							: {type : "string"},
						NombreEstado						: {type : "string"},
						IdTipoCriterioNeteo					: {type : "number"},
						NombreTipoCriterioNeteo				: {type : "string"},
						IdConceptoCustodia					: {type	: "number"},
						UsoCUS								: {type : "string"},
						IdClasificacionTipoInstrumento		: {type : "string"},
						NombreClasificacionTipoInstrumento	: {type : "string"},
						IdTipoInstrumento					: {type : "string"},
						NombreTipoInstrumento				: {type : "string"},
						IdInstrumento						: {type : "string"},
						Nemotecnico							: {type : "string"},
						IdContraparte						: {type : "string"},
						NombreContraparte					: {type : "string"},
						IdTipoCuenta						: {type : "string"},
						TipoCuenta							: {type : "string"},
						ConCertificadoAutomatico			: {type : "string"},
						GeneraMovimientoEnTransito			: {type : "string"},
						GeneraTraspaso						: {type : "string"},
						EsGarantiaVendible					: {type : "string"},
						RetieneDividendos					: {type : "string"},
						FolioObligatorio					: {type : "string"},
						GeneraTransferenciaBoveda			: {type : "string"},
						EsTransferenciaPortafolio			: {type : "string"},
						EsTransferenciaCliente				: {type : "string"},
						VisiblePorEjecutivoCUS				: {type : "string"},
						ConCertificadoAutomaticoTra			: {type : "string"},
						GeneraMovimientoEnTransitoTra		: {type : "string"},
						GeneraTraspasoTra					: {type : "string"},
						EsGarantiaVendibleTra				: {type : "string"},
						GeneraTransferenciaBovedaTra		: {type : "string"},
						EsTransferenciaPortafolioTra		: {type : "string"},
						EsTransferenciaClienteTra			: {type : "string"},
						VisiblePorEjecutivoCUSTra			: {type : "string"},
						UsoTES								: {type : "string"},						
						DisponibleIngresoManualTES			: {type : "string"},
						VisiblePorEjecutivoTES				: {type : "string"},
						NoValidaSaldoCajaTES				: {type : "string"},
						GeneraComprobanteAutomaticamenteTES : {type : "string"},
						GeneraComprobanteAutomaticamenteTESTra : {type : "string"},
						AfectaRentabilidadTESTra 			: {type : "string"},
						DisponibleIngresoManualTESTra		: {type : "string"},
						VisiblePorEjecutivoTESTra			: {type : "string"},
						DisponibleIngresoManualCTA			: {type : "string"},
						VisiblePorEjecutivoCTA				: {type : "string"},
						
						AfectaRentabilidad					: {type : "string"},
						AfectaRentabilidadTra				: {type : "string"},
							
						
						DisponibleIngresoManualCTATra		: {type : "string"},
						VisiblePorEjecutivoCTATra			: {type : "string"},
						RetieneIntereses	    			: {type : "string"},
						RetieneAmortizacion	    			: {type : "string"},
						EsOtrosConceptos					: {type : "string"},
						EsReembolso							: {type : "string"},
						CentroCosto							: {type : "string"},
						CodigoProducto						: {type : "string"},
						InterfazSIG							: {type : "string"},
						Contrapartida						: {type : "string"},
						AbonoClpContrapartida				: {type : "string"},					
						ConfiguradoEnModuloFACTra			: {type : "string"},
						ConfiguradoEnModuloCUSTra			: {type : "string"},
						ConfiguradoEnModuloTESTra			: {type : "string"},
						ConfiguradoEnModuloCTATra			: {type : "string"},
						ConceptoAbonoReplica					: {type : "string"},
						ConceptoCargoReplica					: {type : "string"}
					}
				}
			}
		}
	});	
	$scope.confConceptoManual = {
		dataSource  : dsConceptos,
		columns 	: [  {field : "IdConcepto",					title:"UI_CONCEPTO",				width: "60px"}
						,{field : "NombreConcepto",				title:"UI_NOMBRE",					width: "200px"}						
						,{field : "NombreEstado",				title:"UI_ESTADO",					width: "60px"}
						,{field : "NombreTipoCriterioNeteo",	title:"UI_TIPO_CRITERIO_NETEO",		width: "120px"}
						,{field : "VisiblePorEjecutivoCUSTra", 	title:'UI_VISIBLE_POR_EJECUTIVO', 	width: "45px"}
						,{title : "UI_FACTURACION", align: "center",
							  columns : [ {field : "ConfiguradoEnModuloFACTra", 	title:'UI_CONCEPTO_CONFIGURADO_PARA_MODULO', 	width: "45px"}
							             ,{field : "EsOtrosConceptos", 				title:'UI_OTROSCONCEPTOS', 						width: "45px"}
							  			 ,{field : "EsReembolso", 					title:'UI_REEMBOLSO', 							width: "45px"}
										 ,{field : "CentroCosto", 					title:'UI_CENTRO_COSTO', 						width: "80px"}
										 ,{field : "InterfazSIG",					title:'UI_INTERFAZ_SIG',						width: "80px"}
										 ,{field : "Contrapartida", 				title:'UI_CONTRAPARTIDA',						width: "45px"}
										 ,{field : "AbonoClpContrapartida", 		title:'UI_ABONO_CLP_CONTRAPARTIDA',				width: "45px"}
										 ,{field : "CodigoProducto",				title:'UI_CODIGO_PRODUCTO',						width: "80px"}		  
							 ]}
						,{title : "UI_CUSTODIA", align: "center",
						  columns : [ {field : "ConfiguradoEnModuloCUSTra", 		title:'UI_CONCEPTO_CONFIGURADO_PARA_MODULO', 	width: "45px"}
						             ,{field : "UsoCUS", 							 title:'UI_USO', 								width: "100px"}
						  			 ,{field : "NombreClasificacionTipoInstrumento", title:'UI_CLASIFICACION', 						width: "150px"}
									 ,{field : "NombreTipoInstrumento", 			title:'UI_TIPO_INSTRUMENTO', 					width: "150px"} 
									 ,{field : "Nemotecnico", 						title:'UI_INSTRUMENTO', 						width: "150px"}
									 ,{field : "NombreContraparte", 				title:"UI_ENTIDAD_DESTINO", 					width: "150px"}
									 ,{field : "TipoCuenta", 						title:"UI_TIPO_CUENTA", 						width: "150px"}
									 ,{field : "ConCertificadoAutomaticoTra", 		title:'UI_CERTIFICADO_AUTOMATICO', 				width: "45px"}
									 ,{field : "GeneraMovimientoEnTransitoTra", 	title:'UI_GENERA_MOVIMIENTO_EN_TRANSITO', 		width: "45px"}
									 ,{field : "GeneraTraspasoTra", 				title:'UI_GENERATRASPASO', 						width: "45px"}									 
									 ,{field : "GeneraTransferenciaBovedaTra", 		title:'UI_GENERA_TRANSFERENCIA_BOVEDA', 		width: "45px"}
									 ,{field : "EsTransferenciaPortafolioTra", 		title:'UI_ES_TRANSFERENCIA_PORTAFOLIO', 		width: "45px"}
									 ,{field : "EsTransferenciaClienteTra", 		title:'UI_ES_TRANSFERENCIA_CLIENTE', 			width: "45px"}
									 ,{field : "EsGarantiaVendibleTra", 			title:'UI_ES_GARANTIA_VENDIBLE', 				width: "45px"}
						 ]}
						,{title : "UI_TESORERIA", align: "center",
						  columns : [ {field : "ConfiguradoEnModuloTESTra", 		title:'UI_CONCEPTO_CONFIGURADO_PARA_MODULO',	width: "45px"}						  			 						  			 
						             ,{field : "UsoTES", 							title:'UI_USO', 								width: "100px"}
									 ,{field : "DisponibleIngresoManualTESTra", 	title:'UI_DISPONIBLE_INGRESO_MANUAL', 			width: "45px"}	
									 ,{field : "NoValidaSaldoCajaTES", 	            title:'UI_NO_VALIDA_SALDO_CAJA', 		    	width: "45px"}
									,{field  : "GeneraComprobanteAutomaticamenteTESTra",title:'UI_GENERA_COMPROBANTE_AUTOMATICAMENTE', width: "45px"}		

						 ]}
						,{title : "UI_CUENTA_INVERSION", align: "center",
						  columns : [ {field : "AfectaRentabilidadTESTra", 	 	    title:'UI_AFECTA_RENTABILIDAD',             	width: "45px"}//CELLZORRO-11 -MBI Mejoramiento Cartola - Clasificaci�n de cargos y abonos
							  		 ,{field : "ConfiguradoEnModuloCTATra", 		title:'UI_CONCEPTO_CONFIGURADO_PARA_MODULO', 	width: "45px"}
						             ,{field : "DisponibleIngresoManualCTATra", 	title:'UI_DISPONIBLE_INGRESO_MANUAL', 			width: "45px"}
						 ]}
						 ,{title : "UI_CONCEPTO_REPLICA", align: "center",
						  columns : [ 
							  		  {field : "ConceptoAbonoReplica", 	title:'UI_CONCEPTO_ABONO_REPLICA', 	width: "45px"}
						             ,{field : "ConceptoCargoReplica", 	title:'UI_CONCEPTO_CARGO_REPLICA', 	width: "45px"}
						 ]}
                      ],
		contextMenu : true,
		filterable	: true,
		sortable 	: true,
		pageable 	: {refresh: true},//true,
		excel 		: { allPages : true },
		height 		: 360,
		selectable 	: "row",
		autoBind 	: false,
		userSettings: true
	};

	$scope.buscar = function(){
		var filters = new Array();
 				
		if ($scope.data.selTipoConcepto.TipoConcepto)
			filters.push({ field: "TipoConcepto", operator: "eq", value: $scope.data.selTipoConcepto.TipoConcepto});
		
		dsConceptos.filter(filters);
	};
	
	$scope.agregar = function(){
		var AgregarModificarConcepto = bcsComunes.ventanaAbrir("AgregarModificarConceptosManuales",[],false,false,$scope.winID);		
        var promesa=bcsComunes.ventanaPromesa(AgregarModificarConcepto);

        promesa.then(function(data){
        	$scope.buscar();
			//dsConceptos.read();
		});
	};
	$scope.modificar = function(datos){
		if (typeof datos === 'undefined' || datos.length > 1){
			bcsComunes.msg("error","error",$translate.instant('UI_DEBE_SELECCIONAR_UNICO_REGISTRO'));
			return;
		}
		
		var AgregarModificarConcepto = bcsComunes.ventanaAbrir("AgregarModificarConceptosManuales",$scope.seleccionConceptoManual,false,false,$scope.winID);		
        var promesa=bcsComunes.ventanaPromesa(AgregarModificarConcepto);

        promesa.then(function(data){
        	$scope.buscar();
			//dsConceptos.read();
		});
	};
});