(function() {
	angular.module('ModuloAPV').controller('ModalConciliacionMasivaPreviredCtrl',
	function($scope, $translate, $resource, bcsComunes, $timeout, $state, $modal, $q, bcsAlerta, OPCIONES_BCSALERTA, bcsConsultasAPV, bcsServiciosAPV){
		let idVentana = bcsComunes.ventanaWinId($scope);
		let mConcMsv = this;
		mConcMsv.habilitaBtnConciliar = false;
		mConcMsv.inicializa = true;

		let foliosRecaudacion = {
			type : "odata",
			definition : {
                serverFiltering : false, // Filtrado de datos en el servidor.
                serverPaging    : false, // Paginado de los datos en el servidor.
                serverSorting   : false, // Ordenamiento de los datos en el servidor.
                sort 			: true,
                pageSize        : 100,
				sortable		: true,
	      		reorderable		: true,
	      		filterable		: true,
				groupable		: true,
				pageable		: true,
	      		autoBind		: false,
	      		scrollable		: true,
                transport		: {
					read: {
						url 		: bcsConsultasAPV('ConsultarFolioRecaudacionConciliacionMasiva'),
						dataType	: "json"
				    }
				},
                schema 			: { 
					model: {
						fields : {		
							"IdRecaudacion"			: {type: "number"},
							"IdEmpresa"				: {type: "number"},
							"IdEstado"				: {type: "string"},
							"MontoDeposito"			: {type: "number"},
							"TotalCliente"			: {type: "number"},
							"TotalEmpleador"		: {type: "number"},
							"DiferenciaCliente"		: {type: "number"},
							"DiferenciaEmpleador"	: {type: "number"}
						}
					}
				}
			}
		};
		
		//------ GRILLA
		let  dsFoliosRecaudacion = new bcs.DataSource(foliosRecaudacion);
		mConcMsv.confGridFoliosRecaudacion = {
			dataSource		: dsFoliosRecaudacion,
			columns		: [
					{field: "IdRecaudacion"		, title: "UI_FOLIO_RECAUDACION"	, width: "130px", align: "right"	, sortable : true, editable: false }
				,	{field: "DiferenciaCliente"		, title: "UI_DIFERENCIA_CLIENTE"	, width: "130px", align: "right"	, sortable : true, editable: false }
    			,	{field: "DiferenciaEmpleador"		, title: "UI_DIFERENCIA_EMPLEADOR"	, width: "130px", align: "right"	, sortable : true, editable: false }
    							
			],
			sortable	: true,
    		pageable	: true,
    		filterable	: false,
    		navigatable	: true,
    		height		: 400,
    		width		: 400,
    		selectable	: "multiple row",
    		autoBind 	: false,
    		gridCreated 		: function(g) {
				$scope.ApiFoliosRecaudacion = g;
												
				mConcMsv.kFolioDesde = false;
				mConcMsv.kFolioHasta = false;
				
				mConcMsv.filtrarFoliosRecaudacion();
			}
		}
		
		mConcMsv.limpiarFoliosRecaudacion = function(){
			mConcMsv.kFolioDesde = false;
			mConcMsv.kFolioHasta = false;
			$scope.ApiFoliosRecaudacion.dataSource.ds._filter.filters = undefined;
		};
		
		mConcMsv.filtrarFoliosRecaudacion = function(){
			let filters = new Array();
			mConcMsv.confGridFoliosRecaudacion.dataSource.read();
			if(mConcMsv.inicializa){
				mConcMsv.inicializa = false;
				filters.push({ field: "IdRecaudacion", operator: "eq", value: 0 });
			} else{
				if(mConcMsv.kFolioDesde){
					filters.push({ field: "IdRecaudacion", operator: "gte", value: mConcMsv.kFolioDesde });
				}
				if(mConcMsv.kFolioHasta){
					filters.push({ field: "IdRecaudacion", operator: "lte", value: mConcMsv.kFolioHasta });
				}
			}
			
			dsFoliosRecaudacion.filter(filters);
		};
		
		mConcMsv.fnConciliarMasivo = function(){
			bcsComunes.ventanaOverlay(idVentana,true);
			let recaudaciones = Array();
			
			$.each(mConcMsv.seleccionFolios,function(key,value){
				recaudaciones.push({IdRecaudacion: value.IdRecaudacion});
			});
			
			let parametros = {				
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo  : "APV",
				p_AsignacionAutomaticaContrato : "S",
				p_EsProcesoMasivo : "S",
				p_Parametros : recaudaciones
			};
			
			let promesa = bcsServiciosAPV.ConciliarDeposito(parametros).$promise.then(function(respuesta){
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
						
				respuesta.P_OUT_RESULTADO = new Array();
						
				if(respuesta.P_OUT_MENSAJES.length > 0){
					$.each(obtenerMensajes(respuesta.P_OUT_MENSAJES),function(key,value){
						respuesta.P_OUT_RESULTADO.push( value );
					});
					if(respuesta.P_OUT_ERRORESCONTADOR > 0 && respuesta.P_OUT_MENSAJES.filter(mensaje => mensaje.codigo == "CONCILIADO_OK").length > 0){
						let recaudacionesAConciliar = mConcMsv.fnObtenerRecaudacionesExito(respuesta.P_OUT_MENSAJES);
						let params = {				
							p_IdEmpresa : $scope.SesionIdEmpresa,
							p_IdUsuario : $scope.SesionIdUsuario,
							p_IdModulo  : "APV",
							p_AsignacionAutomaticaContrato : "S",
							p_EsProcesoMasivo : "S",
							p_Parametros : recaudacionesAConciliar
						};
						
						bcsServiciosAPV.ConciliarDeposito(params).$promise.then(function(respuesta){
						},function(respuesta){
				 			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_CONCILIACION_MASIVA"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
						});
					}
				}
						
				bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: 'RESULTADOS', campoSecuencia : "IdInterno" ,nombreSecuencia : 'UI_SECUENCIA',datos : respuesta.P_OUT_RESULTADO } ,false,false,$scope.winID);	
				
				bcsComunes.ventanaOverlay(idVentana,false);
				mConcMsv.confGridFoliosRecaudacion.dataSource.read();
	 			mConcMsv.filtrarFoliosRecaudacion();
			},function(respuesta){
	 		   bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_CONCILIACION_MASIVA"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			});
	 		
	 		promesa.then(function () {
				mConcMsv.confGridFoliosRecaudacion.dataSource.read();
	 			mConcMsv.filtrarFoliosRecaudacion();
			});
		};
		
		$scope.$watch('mConcMsv.seleccionFolios',function(newval,oldval){
			if(mConcMsv.seleccionFolios!= null && newval!= undefined  ){
				if(mConcMsv.seleccionFolios.length >= 1){
					mConcMsv.habilitaBtnConciliar = true;	
				}else{
					mConcMsv.habilitaBtnConciliar = false;
				}					
			}else {
				mConcMsv.habilitaBtnConciliar = false;
			}
			
		});
		
		mConcMsv.fnObtenerRecaudacionesExito = function(mensajesSalida){
			let recaudacionesAConciliar = new Array();
			
			$.each(obtenerMensajes(mensajesSalida),function(key,value){
				if(value.codigo == "CONCILIADO_OK"){
					recaudacionesAConciliar.push( {IdRecaudacion: value.IdInterno});					
				}
			});
			
			return recaudacionesAConciliar;
		};
		
		$scope.$watch('mConcMsv.kFolioDesde',function(newval,oldval){
			if(!newval && (mConcMsv.kFolioHasta == null || mConcMsv.kFolioHasta == false))
				mConcMsv.habilitaBtnBuscar = false;
			if(newval)
				mConcMsv.habilitaBtnBuscar = true;
			
		});
		
		$scope.$watch('mConcMsv.kFolioHasta',function(newval,oldval){
			if(!newval && (mConcMsv.kFolioDesde == null || mConcMsv.kFolioDesde == false))
				mConcMsv.habilitaBtnBuscar = false;
			if(newval)
				mConcMsv.habilitaBtnBuscar = true;
			
		});
		
		let obtenerMensajes = function (listadoMensajes){
			let mensajeCarga = new Array();
			$.each(listadoMensajes,function(key,value){
				 let objCarga = {
						IdInterno : value.id,
						tipo : value.tipo === 'OK' ? 'INFORMACION' : value.tipo,
						codigo : value.codigo,
						descripcion : value.descripcion
				 };
				 mensajeCarga.push(objCarga);
			});
			return mensajeCarga;
		};
		
		bcsComunes.ventanaAutoHeight(idVentana);
	});
}());
//------------------------- FIN CONTROLLER PRINCIPAL -----------------------------