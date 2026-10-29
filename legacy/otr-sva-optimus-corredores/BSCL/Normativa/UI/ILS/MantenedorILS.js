(function() {
	angular.module('ModuloMesa').controller('MantenedorILSCtrl',
	function ($state,
			  $q,
			  $scope,
			  $compile,
			  bcsComunes,
			  $translate,
			  $timeout,
			  bcsServiciosNormativa,
			  $http) {


		$scope.select = function(grid, treelist=false)	{
		    if(treelist)
		        var height = 515;
		    else
                var height = $("#" + grid).find(".k-grid").height();
            $("#" + grid).find(".k-grid-content").css("height", (height *0.925)+"px")
        };
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/

		$scope.data = {};
		var mantenedorILS = this;
		//$scope.xx = {};
		$scope.winID = bcsComunes.ventanaWinId($scope);
		mantenedorILS.params		= bcsComunes.ventanaParams($scope.winID);
		mantenedorILS.domVentana	= $("[ventanaid='"+$scope.winID+"']");
		mantenedorILS.habilitado 	= true;
		mantenedorILS.kngFechaContable = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.habilitaDig = true;
		var idVentana = bcsComunes.ventanaWinId($scope)
		/*********************************************
		 * RESULTADOS
		 ********************************************/
		mantenedorILS.ConsultarSaldos = function() {
			
			var parametros = {};
			parametros.p_IdEmpresa 				= $scope.SesionIdEmpresa; 
			parametros.p_IdUsuario 				= $scope.SesionIdUsuario;
			parametros.p_IdModulo				= 'ILS';
			parametros.p_UsaReglasNegocio 		= 'N';
	    	bcsServiciosNormativa.p_SaldosCuentasContables(parametros,successSaldos,errorGenerar);
	    };
		 
	    var successSaldos = function(respuesta)
	    {
	    	if(respuesta.P_OUT_ERRORESCONTADOR === 0){
	    		
	    		mantenedorILS.Generar(respuesta.P_OUT_SALDOSCUENTASCONTABLES);
	    	}
	    	if(respuesta.P_OUT_ERRORESCONTADOR != 0)
	    	{
	    		$.each(respuesta.P_OUT_ERRORESDETALLE,function(key,value)
	    		{
	    			bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant(value.Descripcion));
	    		});
	    	}
			$scope.data.habilitaDig = true;
	    };
	    
	    mantenedorILS.HomologarPlanDeCuentas = function(){
			var params = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdModulo  : "ILS",
				p_IdUsuario : $scope.SesionIdUsuario,
				p_FechaContable: util.dateSPdate(mantenedorILS.kngFechaContable)
			};/*
			$scope.call("601111", params, function(respuesta){
				bcsComunes.ventanaMsg({conflictos:util.parseServerResponse(respuesta)},idVentana);
				bcsComunes.ventanaOverlay(idVentana,false);
			});*/
			bcsServiciosNormativa.HomologarPlanDeCuentas(params,successObtenerCuentasContables,errorObtenerCuentasContables);
		}
	    mantenedorILS.GenerarReporteCoberturaPatrimonial = function()		
	    { 
	    	var params = {
		    	p_IdEmpresa			: $scope.SesionIdEmpresa,
				p_FechaOperacion	: util.dateSPdate(mantenedorILS.kngFechaContable),
				p_plazoDias			: 3,
				p_opcion			: 1,
				p_TipoValor			: 2,
				p_IdCliente			: null,
				p_IdPortafolio		: null,
				p_NombreMesa		: 'TODOS'
	    	};
	    	bcsServiciosNormativa.ReporteCoberturaPatrimonial(params,successReporteCoberturaPatrimonial,errorReporteCoberturaPatrimonial);
	    	
	    }

		$scope.data.GenerarReporteSaldosCuentaContable = function () {
			let params = {
				p_IdEmpresa: $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: "ILS",
				p_FechaContable: util.dateSPdate(mantenedorILS.kngFechaContable)
			};
			bcsServiciosNormativa.GenerarReporteSaldosCuentaContable(params).$promise.then(function (result) {
				if (result.P_OUT_ADVERTENCIASCONTADOR) {
					bcsComunes.msg("warning", $translate.instant("UI_GENERAR_REPORTES"), result.P_OUT_ADVERTENCIASDETALLE[0].Codigo);
					bcsComunes.ventanaOverlay($scope.winID, false);
				}
				else if (result.P_OUT_ERRORESCONTADOR) {
					bcsComunes.msg("error", $translate.instant("UI_ERROR_AL_GENERAR_REPORTE"), result.P_OUT_ERRORESDETALLE[0].Codigo);
					bcsComunes.ventanaOverlay($scope.winID, false);
				}
				else {
					var nombre = result.P_OUT_RESULTADOPROCESO[4].Valor;
					$timeout(function () {
						var url = "/BSCL/Plataforma/GWJAVA/PLA/download?";
						var data = { file: nombre };
						$http({
							method: 'POST',
							url: url,
							params: data,
							responseType: "arraybuffer"
						}).success(function (data) {
							var file = new Blob([data], { type: 'application/vnd.ms-excel' },);
							saveAs(file, nombre);
							bcsComunes.ventanaOverlay($scope.winIDa, false);
						}).error(function (err) {
							bcsComunes.msg("error", 'Error', $translate.instant('UI_ERROR_AL_GENERAR_REPORTE'));
							bcsComunes.ventanaOverlay($scope.winID, false);
						});
					}, 2000);
				}
			},
				function (result) {
					bcsComunes.msg("error", 'Error', $translate.instant('UI_ERROR_AL_GENERAR_REPORTE'));
					bcsComunes.ventanaOverlay($scope.winID, false);
				});
		}

		mantenedorILS.Generar = function() {
			
			var parametros = {};
			parametros.p_IdEmpresa 				= $scope.SesionIdEmpresa; 
			parametros.p_IdUsuario 				= $scope.SesionIdUsuario;
			parametros.p_IdModulo				= 'ILS';
			parametros.p_UsaReglasNegocio 		= 'N';
			parametros.p_FechaInforme			= util.dateSPdate(mantenedorILS.kngFechaContable);
			parametros.p_Informes				= [{p_IdTipoInforme	:	mantenedorILS.filtro.IdReporte}];
			
			bcsServiciosNormativa.GenerarILS(parametros,successGenerar,errorGenerar);
	    };
		
	    mantenedorILS.ObtenerSaldosContablesSAP = function(saldosCuentasContables) {
			
			var parametros = {};
			parametros.p_IdEmpresa 				= $scope.SesionIdEmpresa; 
			parametros.p_fechaConsulta 			= mantenedorILS.kngFechaContable;
	    	bcsServiciosNormativa.ObtenerSaldosContablesSAP(parametros,successObtenerSaldosContablesSAP,errorObtenerCuentasContables);
	    };
	    
	    var successObtenerSaldosContablesSAP = function(respuesta)
	    {
	    	if(respuesta.P_OUT_ERRORESCONTADOR === 0){
	    		bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_OPERACION_REALIZADA_EXITO"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
				mantenedorILS.BuscarResultado();				
	    	} 
	    	else 
	    	{
	    		bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
	    	}
	    };
	    
	    var successObtenerCuentasContables = function(respuesta)
	    {
	    	if(respuesta.P_OUT_ERRORESCONTADOR === 0){
	    		/*bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_OPERACION_REALIZADA_EXITO"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
				mantenedorILS.BuscarResultado();*/
				mantenedorILS.ObtenerSaldosContablesSAP();
				
	    	} 
	    	else 
	    	{
	    		bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
	    	}
	    };
	    
	    var errorObtenerCuentasContables = function(respuesta)
	    {
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_ERROR_OBTENER_CUENTAS_CONTABLES"),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
			bcsComunes.ventanaOverlay($scope.winID, false);
	    };
	    
	    var successReporteCoberturaPatrimonial = function(respuesta)
	    {
	    	if(respuesta.P_OUT_COBERTURAPATRIMONIALDETALLE.length >= 0){
	    		bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_OPERACION_REALIZADA_EXITO"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
				mantenedorILS.BuscarResultado();
	    	} 
	    	else 
	    	{
	    		bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
	    	}
	    };
	    
	    var errorReporteCoberturaPatrimonial = function(respuesta)
	    {
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_ERROR_OBTENER_CUENTAS_CONTABLES"),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
			bcsComunes.ventanaOverlay($scope.winID, false);
	    };
	    
	    var successGenerar = function(respuesta)
	    {
	    	if(respuesta.P_OUT_ERRORESCONTADOR === 0){
	    		bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_OPERACION_REALIZADA_EXITO"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
	    		
	    		mantenedorILS.BuscarResultado();
	    	}
	    	if(respuesta.P_OUT_ERRORESCONTADOR != 0)
	    	{
	    		bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
	    	}
			$scope.data.habilitaDig = true;
	    };
	    
	    var errorGenerar = function(respuesta)
	    {
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_ERROR_GENERAR"),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
			bcsComunes.ventanaOverlay($scope.winID, false);
			$scope.data.habilitaDig = true;
	    };
	    
	    var dsDefILSSaldosCuentasContables = 
		{
			type 		: "odata",
			definition	: 	{
								serverFiltering : true, // Filtrado de datos en el servidor.
				                serverPaging    : true, // Paginado de los datos en el servidor.
				                serverSorting   : true, // Ordenamiento de los datos en el servidor.
				                sort 			: { field: "IdCodigoContabilidad", dir: "ASC" },
				                transport		: {
				                	read: {
					            		url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=13046",
										dataType:"json"
					            	}
					            },
					            schema 			: { 
                    								model:{
                    										fields :{
																		IdCodigoContabilidad	: {type: "string"},
																		MontoDebe 				: {type: "number"},
																		MontoHaber    			: {type: "number"}
                    										}
                    								}
					            }
			}
		};	
	    
		mantenedorILS.BuscarResultado = function() {
	    	sptArray=[];
	    	dsGrillaResultados.removeAll();
	    	
	    	
	    	if (mantenedorILS.filtro.IdReporte != false){
	    		//REPROCESO 
		 		var parametrosReproceso = {};
				parametrosReproceso.p_IdEmpresa 		= $scope.SesionIdEmpresa; 
				parametrosReproceso.p_IdUsuario 		= $scope.SesionIdUsuario;
				parametrosReproceso.p_IdModulo			= 'ILS';
				parametrosReproceso.p_UsaReglasNegocio  = 'S';
				parametrosReproceso.p_IdTipoInforme		= mantenedorILS.filtro.IdReporte;
				parametrosReproceso.p_FechaContable		= util.dateSPdate(mantenedorILS.kngFechaContable);
				parametrosReproceso.p_FechaEmision		= '';
		    	bcsServiciosNormativa.InformeResultadoBuscar(parametrosReproceso,successResultadoIndices,errorResultadoIndices);
		    	bcsServiciosNormativa.ConceptoResultadoBuscar(parametrosReproceso,successConcepto,errorConcepto);
		    	bcsServiciosNormativa.ItemResultadoBuscar(parametrosReproceso,successGC,errorGC);
		    	bcsServiciosNormativa.ItemDetalleResultadoBuscar(parametrosReproceso,successDetalleItem,errorDetalleItem);
		    	bcsServiciosNormativa.DetalleCoberturaLlenaGrilla(parametrosReproceso,successDetalleCobertura,errorDetalleCobertura);
		    	bcsServiciosNormativa.DetalleSaldoCuentaContableLlenaGrilla(parametrosReproceso,successDetalleSaldoCuentaContable,errorDetalleSaldoCuentaContable);
	    	}
	    	
			//mantenedorILS.confDetalleSaldoCuentaContable.dataSource=[{"IdCodigoContabilidad":"210601001001","MontoDebe":138025038.00,"MontoHaber":0.00,"FechaContable":null,"IdCodigoContabilidadPadre":""}];
	    	/*mantenedorILS.DetalleSaldoCuentaContableTreeList.dataSource.data([
	    	{"IdCodigoContabilidad":"210601001001","MontoDebe":138025038.00,"MontoHaber":0.00,"FechaContable":null,"IdCodigoContabilidadPadre":"","Nivel":0},
	    	{"IdCodigoContabilidad":"210601001002","MontoDebe":9999.00,"MontoHaber":0.00,"FechaContable":null,"IdCodigoContabilidadPadre":"210601001001","Nivel":0}]);
	    	mantenedorILS.DetalleSaldoCuentaContableTreeList.refresh();*/

	    };
	    
	    var successResultadoIndices = function(respuesta)
	    {
	    	if(respuesta.P_OUT_ERRORESCONTADOR === 0){
	    		dsGrillaResultados.data(respuesta.P_TABLARESULTADOINFORME);
	    	}
	    	if(respuesta.P_OUT_ERRORESCONTADOR != 0)
	    	{
	    		$.each(respuesta.P_OUT_ERRORESDETALLE,function(key,value)
	    		{
	    			bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant(value.Descripcion));
	    		});
	    	}
	    };
	    var successConcepto = function(respuesta)
	    {
	    	if(respuesta.P_OUT_ERRORESCONTADOR === 0){
	    		respuesta.P_TABLARESULTADOCONCEPTO.forEach((element, index) => {                               
			    MontoCalculado = Math.trunc(element.MontoCalculado);                                                        
			    respuesta.P_TABLARESULTADOCONCEPTO[index].MontoCalculado = MontoCalculado;                                                                     
			});
	    		dsGrillaConceptos.data(respuesta.P_TABLARESULTADOCONCEPTO);
	    	}
	    	if(respuesta.P_OUT_ERRORESCONTADOR != 0)
	    	{
	    		$.each(respuesta.P_OUT_ERRORESDETALLE,function(key,value)
	    		{
	    			bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant(value.Descripcion));
	    		});
	    	}
	    };
	    var successGC = function(respuesta)
	    {
	    	if(respuesta.P_OUT_ERRORESCONTADOR === 0){
				
	    		dsGrillaGC.data(respuesta.P_TABLARESULTADOGC);
			
	    		dsGrillaItems.data(respuesta.P_TABLARESULTADOITEM);
	    	}
	    	if(respuesta.P_OUT_ERRORESCONTADOR != 0)
	    	{
	    		$.each(respuesta.P_OUT_ERRORESDETALLE,function(key,value)
	    		{
	    			bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant(value.Descripcion));
	    		});
	    	}
	    };
	    var successDetalleItem = function(respuesta)
	    {
	    	if(respuesta.P_OUT_ERRORESCONTADOR === 0){
				respuesta.P_TABLARESULTADOITEMDETALLE.forEach(element => {
	    			if(element.Monto < 0 && element.Factor >= 0){
	    				element.MontoCalculado = element.MontoCalculado >0? element.MontoCalculado*-1 : element.MontoCalculado;
	    				element.MontoContable = element.MontoContable >0 ? element.MontoContable*-1: element.MontoContable;
	    			}
	    		})
	    		dsGrillaDetalleItem.data(respuesta.P_TABLARESULTADOITEMDETALLE);
	    		
	    	}
	    	if(respuesta.P_OUT_ERRORESCONTADOR != 0)
	    	{
	    		$.each(respuesta.P_OUT_ERRORESDETALLE,function(key,value)
	    		{
	    			bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant(value.Descripcion));
	    		});
	    	}
	    };
	    var successDetalleCobertura = function(respuesta)
	    {
	    	if(respuesta.P_OUT_ERRORESCONTADOR === 0){
	    		dsGrillaDetalleCobertura.data(respuesta.P_TABLARESULTADODETALLECOBERTURA);
	    	}
	    	if(respuesta.P_OUT_ERRORESCONTADOR != 0)
	    	{
	    		$.each(respuesta.P_OUT_ERRORESDETALLE,function(key,value)
	    		{
	    			bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant(value.Descripcion));
	    		});
	    	}
	    };
	    var successDetalleSaldoCuentaContable = function(respuesta)
	    {
	    	if(respuesta.P_OUT_ERRORESCONTADOR === 0){
	    		//dsGrillaDetalleSaldoCuentaContable.data = [{IdCodigoContabilidad: "210601001001", MontoDebe: 138025038, MontoHaber: 0}];//(respuesta.P_OUT_SALDOSCUENTASCONTABLES);
	    		//mantenedorILS.xx.refresh();
	    		mantenedorILS.DetalleSaldoCuentaContableTreeList.dataSource.data(respuesta.P_OUT_SALDOSCUENTASCONTABLES);
	    	    mantenedorILS.DetalleSaldoCuentaContableTreeList.refresh();
	    	}
	    	if(respuesta.P_OUT_ERRORESCONTADOR != 0)
	    	{
	    		$.each(respuesta.P_OUT_ERRORESDETALLE,function(key,value)
	    		{
	    			bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant(value.Descripcion));
	    		});
	    	}
	    };
	    var errorResultadoIndices = function(respuesta)
	    {
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_ERROR_INDICE"),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
			bcsComunes.ventanaOverlay($scope.winID, false);
	    };
	    var errorConcepto = function(respuesta)
	    {
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_ERROR_CONCEPTO"),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
			bcsComunes.ventanaOverlay($scope.winID, false);
	    };
	    var errorGC = function(respuesta)
	    {
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_ERROR_GRUPO_CONCEPTO"),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
			bcsComunes.ventanaOverlay($scope.winID, false);
	    };
	    var errorDetalleItem = function(respuesta)
	    {
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_ERROR_ITEM_DETALLE"),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
			bcsComunes.ventanaOverlay($scope.winID, false);
	    };
	    
	    var errorDetalleCobertura = function(respuesta)
	    {
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_ERROR_DETALLE_COBERTURA"),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
			bcsComunes.ventanaOverlay($scope.winID, false);
	    };

	    var errorDetalleSaldoCuentaContable = function(respuesta)
	    {
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_ERROR_DETALLE_SALDO_CUENTA_CONTABLE"),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
			bcsComunes.ventanaOverlay($scope.winID, false);
	    };

		/*********************************************
		 * CONFIGURACION DE GRILLAS
		 *********************************************/	
	    
	    //RESULTADO INDICES
		
		var dsDefILSResultado = 
		{
			type 		: "array",
			definition	: 	{
								//serverFiltering : true, // Filtrado de datos en el servidor.
				                //serverPaging    : true, // Paginado de los datos en el servidor.
				                //serverSorting   : true, // Ordenamiento de los datos en el servidor.
				                sort 			: { field: "IdResultadoIndice", dir: "ASC" },
				                pageSize        : 15,
//				                transport		: {
//				                	read: {
//					            		url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=13041",
//										dataType:"json"
//					            	}
//					            },
					            schema 			: { 
                    								model:{
                    										fields :{
																		IdIndice	     		: {type: "number"},
																		NombreIndice 			: {type: "string"},
																		MontoIndice    			: {type: "number"},
																		FechaContable			: {type: "sysdate"},
																		IdIndiceAnterior		: {type: "number"},
																		FechaContableAnterior	: {type: "sysdate"},
																		Tipo					: {type: "string"},
                    										}
                    								}
					            }
			}
		};	
		var dsGrillaResultados = new bcs.newDataSource(dsDefILSResultado);
		mantenedorILS.confResultados = 
		{
    		dataSource	: dsGrillaResultados,
			columns		: 	[  { field: "IdIndice", title: "UI_ID_INDICE", width: "80px", sortable : false }
								,{field: "NombreIndice", title: "UI_NOMBRE_INDICE", width: "200px", sortable : false }								
								,{field: "MontoIndice", title: "UI_INDICE", width: "60px",  sortable : false , template : "#: (util.bcs_formato(MontoIndice,'GEN','Tasa','CLP','','INDICE')).valor #"}
								,{field: "Tipo", title: "UI_TIPO", width: "30px", sortable : false, align: "center" }								
								,{field: "FechaContable", title: "UI_FECHA_CONTABLE", width: "90px", sortable : false}
								,{field: "IdIndiceAnterior", title: "UI_INDICE_ANTERIOR", width: "60px",  sortable : false, template : "#: (util.bcs_formato(IdIndiceAnterior,'GEN','Tasa','CLP','','INDICE')).valor #"}
								,{field: "FechaContableAnterior", title: "UI_FECHA_CONTABLE_ANTERIOR", width: "100px", sortable : false }

			  				],		
    		sortable	: true,
    		pageable	: false,
    		filterable	: true,
    		navigatable : true,
    		userSettings: true,
    		height		: 500,
    		excel 		: { allPages: true },
    		scrollable	: true,
    		selectable 	: "row",
    		autoBind 	: false		
       	};		
		
		//CONCEPTOS
		 
		var dsDefILSConceptos = 
		{
			type : "array",
			definition : 
			{
				//serverFiltering : true, // Filtrado de datos en el servidor.
                //serverPaging    : true, // Paginado de los datos en el servidor.
                //serverSorting   : true, // Ordenamiento de los datos en el servidor.
                sort 			: { field: "IdIndice", dir: "DESC" },
                pageSize        : 15,
//                transport		: {
//		                    		read: 
//		                    		{
//		        	            		url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=13028",
//		        						dataType:"json"
//		        	            	}
//		        	            },
                schema 			: { 
                   				   model:
                   				   {
											fields : 
											{
												IdIndice   	            : {type: "number"},
												NombreIndice			: {type: "string"},
												IdIndiceNombre			: {type: "string"},
												IdResultadoIndice   	: {type: "number"},
												IdConcepto   	        : {type: "number"},
												NombreConcepto			: {type: "string"},
												IdConceptoNombre		: {type: "string"},
												IdResultadoConcepto		: {type: "number"},
												Monto   	            : {type: "number"},
												Posicion			    : {type: "string"},													
												Operador				: {type: "string"},													
												Factor					: {type: "string"},													
												MontoCalculado			: {type: "number"},
												Estado					: {type: "string"},													
											}
                   					}
		        }
			}
		};	
		var dsGrillaConceptos = new bcs.newDataSource(dsDefILSConceptos);
		mantenedorILS.confConceptos = 
		{
    		dataSource		: dsGrillaConceptos,
			columns		: [  
							 {field: "IdIndiceNombre", title: "UI_NOMBRE_INDICE", width: "150px", sortable : false }
							,{field: "IdConceptoNombre", title: "UI_NOMBRE_CONCEPTO", width: "450px", sortable : false }
							,{field: "Posicion", title: "UI_POSICION", width: "80px", sortable : false }									
							,{field: "Operador", title: "UI_OPER", width: "50px", sortable : false}
						    ,{field: "Factor", title: "UI_FACT", width: "50px", sortable : false }				   				    
				   			,{field: "MontoCalculado", title: "UI_MONTO_CALCULADO", width: "100px",  sortable : false, template : "#: (util.bcs_formato(MontoCalculado,'GEN','Monto','CLP','','INDICE')).valor #"}
						  ],		
    		sortable		: true,
    		pageable		: false,
    		filterable		: true,
    		navigatable 	: true,
			userSettings	: true,
    		height			: 500,
    		excel 			: { allPages: true },
    		scrollable		: true,
    		selectable 		: "row",
    		autoBind 		: false		
       	};	
		
		//GRUPO CONCEPTO
		
		var dsDefILSGC = 
		{
			type : "array",
			definition : 
			{
				//serverFiltering : true, // Filtrado de datos en el servidor.
                //serverPaging    : true, // Paginado de los datos en el servidor.
                //serverSorting   : true, // Ordenamiento de los datos en el servidor.
                sort 			: { field: "IdOperacion", dir: "DESC" },
                pageSize        : 15,
                schema 			: { 
                  				   model:{
											fields : 
											{ 												
												IdIndice       				: {type: "number"},
												IdNombreIndice       		: {type: "string"},
												IdConcepto          		: {type: "number"},
												IdNombreConcepto			: {type: "string"},
												NombreConceptoGrupo         : {type: "string"},
												IdNombreConceptoGrupo       : {type: "string"},
												Posicion					: {type: "string"},
												NombreItem  				: {type: "string"},
												Operacion					: {type: "string"},	
												Factor						: {type: "string"},													
												MontoCalculado				: {type: "number"},
												PositivoNegativo            : {type: "string"}
											}
                   					}
                }
    		}
		};
		// $scope.gridCarteras = {};
		var dsGrillaGC = new bcs.newDataSource(dsDefILSGC);
		mantenedorILS.confGC = 
		{
    		/*gridCreated : function(grid) {
                $scope.gridCarteras = grid;
            },*/
    		dataSource		: dsGrillaGC,
			columns		: [  {   field: "IdNombreIndice", title: "UI_NOMBRE_INDICE", width: "420px", sortable : false}
			                    ,{field: "IdConcepto", title: "UI_ID_CONCEPTO", width: "150px", sortable : false, hidden: true }
								,{field: "IdNombreConcepto", title: "UI_NOMBRE_CONCEPTO", width: "420px", sortable : false }
								,{field: "NombreConceptoGrupo", title: "UI_NOMBRE_GRUPO_CONCEPTO", width: "400px", sortable : false, hidden: true}
								,{field: "IdNombreConceptoGrupo", title: "UI_NOMBRE_GRUPO_CONCEPTO", width: "420px", sortable : false }
								,{field: "Posicion", title: "UI_POSICION", width: "80px", sortable : false }
								,{field: "NombreItem", title: "UI_NOMBRE", width: "100px", sortable : false, hidden: true }
			   				    ,{field: "Factor", title: "UI_FACT", width: "60px", sortable : false, align: "right"}				   				    
			   				    ,{field: "MontoCalculado", title: "UI_MONTO_CALCULADO", width: "100px", sortable : false, template : "#: (util.bcs_formato(MontoCalculado,'GEN','Monto','CLP','','INDICE')).valor #"}
						  ],		
    		sortable	: true,
    		pageable	: false,
    		filterable	: true,
    		navigatable : true,
			userSettings: true,
    		height		: 500,
    		excel 		: { allPages: true },
    		scrollable	: true,
    		selectable 	: "row",
    		autoBind 	: false		
       	};		
		
//ITEMS
		
		var dsDefILSItems = 
		{
			type : "array",
			definition : 
			{
				//serverFilering : true, // Filtrado de datos en el servidor.
                //serverPaging    : true, // Paginado de los datos en el servidor.
                //serverSorting   : true, // Ordenamiento de los datos en el servidor.
                sort 			: { field: "IdOperacion", dir: "DESC" },
                pageSize        : 15,
                schema 			: { 
                  				   model:{
											fields : 
											{ 												
												IdNombreIndice       		: {type: "string"},
												IdConcepto          		: {type: "number"},
												IdNombreConcepto			: {type: "string"},
												IdConceptoGrupo				: {type: "number"},
												NombreConceptoGrupo         : {type: "string"},
												IdNombreConceptoGrupo       : {type: "string"},
												IdNombreItem       			: {type: "string"},
												Posicion					: {type: "string"},
												NombreItem  				: {type: "string"},
												Operacion					: {type: "string"},	
												Factor						: {type: "string"},													
												MontoCalculado				: {type: "number"},
												PositivoNegativo			: {type: "string"}
											}
                   					}
                }
    		}
		};	
		var dsGrillaItems = new bcs.newDataSource(dsDefILSItems);
		mantenedorILS.confItems = 
		{
    		dataSource		: dsGrillaItems,
			columns		: [  {   field: "IdNombreIndice", title: "UI_NOMBRE_INDICE", width: "420px", sortable : true}
			                    ,{field: "IdConcepto", title: "UI_ID_CONCEPTO", width: "150px", sortable : false, hidden: true }
								,{field: "IdNombreConcepto", title: "UI_NOMBRE_CONCEPTO", width: "420px", sortable : false }
								,{field: "NombreConceptoGrupo", title: "UI_NOMBRE_GRUPO_CONCEPTO", width: "400px", sortable : false, hidden: true}
								,{field: "IdNombreConceptoGrupo", title: "UI_NOMBRE_GRUPO_CONCEPTO", width: "420px", sortable : false }
								,{field: "IdNombreItem", title: "UI_NOMBRE_ITEM", width: "250px", sortable : false, hidden: false }
								,{field: "Posicion", title: "UI_POSICION", width: "80px", sortable : false }
								,{field: "Operacion", title: "UI_OPE", width: "60px",  sortable : false, align: "center" }
			   				    ,{field: "Factor", title: "UI_FACT", width: "60px", sortable : false, align: "right"}				   				    
			   				    ,{field: "MontoCalculado", title: "UI_MONTO_CALCULADO", width: "100px", sortable : false, template : "#: (util.bcs_formato(MontoCalculado,'GEN','Monto','CLP','','INDICE')).valor #"}
						  ],		
    		sortable	: true,
    		pageable	: false,
    		filterable	: true,
    		navigatable : true,
			userSettings: true,
    		height		: 500,
    		excel 		: { allPages: true },
    		scrollable	: true,
    		selectable 	: "row",
    		autoBind 	: false		
       	};
		
		//DETALLE ITEM
		
		var dsDefILSDetalleItem = 
		{
			type : "array",
			definition : 
			{
                //serverFiltering : true, // Filtrado de datos en el servidor.
                //serverPaging    : true, // Paginado de los datos en el servidor.
                //serverSorting   : true, // Ordenamiento de los datos en el servidor.
                sort 			: { field: "NombreConcepto", dir: "DESC" },
                pageSize        : 10,
//                transport		: {
//		      	            		read: 
//		       	            		{
//		       	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=513",
//		       							dataType:"json"
//		       	            		}
//		       	            	},
                schema 			: { 
                   				   model: 
                   						{
                   					   		fields : 
											{
                   					   			IdIndice				: {type: "number"},
                   					   		   IdNombreIndice           : {type: "string"},
                   					   		   IdConcepto               : {type: "number"},	
                    						   IdNombreConcepto       	: {type: "string"},
                    						   IdConceptoGrupo       	: {type: "number"},
                    						   IdNombreConceptoGrupo    : {type: "string"},
                    						   IdItem                   : {type: "number"},                    						                       						   
                    						   NombreItem				: {type: "string"},
                    						   IdNombreItem				: {type: "string"},
                    						   Posicion					: {type: "string"},
                    						   CuentaContable			: {type: "string"},
                    						   SaldoDebeHaber			: {type: "string"},
                    						   Condicion				: {type: "string"},
                    						   MontoCondicion			: {type: "number"},
                    						   Operacion				: {type: "string"},
                    						   Factor					: {type: "string"},
                    						   MontoCalculado			: {type: "number"},
                    						   IdTipoItem				: {type: "string"},
                    						   MayorACero				: {type: "string"},
                    						   MenorACero				: {type: "string"},
                    						   PermiteCero				: {type: "string"},
                    						   ProcesoFuncion			: {type: "string"},
                    						   NombreTipoItem			: {type: "string"},
                    						   MontoContable			: {type: "number"},
                    						   MontoDigitado			: {type: "number"},
                    						   MontoProgramado			: {type: "number"},
                    						   OperacionGC				: {type: "string"},
											}
                    					}
		        }
    		}
		};	
		var dsGrillaDetalleItem = new bcs.newDataSource(dsDefILSDetalleItem);
		mantenedorILS.confDetalleItem = 
		{
    		dataSource	: dsGrillaDetalleItem,
			columns		: 	[  
 								 {field: "IdConcepto", title: "UI_ID_CONCEPTO", width: "100px", sortable : false, align: "center", hidden: true }
 								,{field: "IdNombreIndice", title: "UI_NOMBRE_INDICE", width: "420px", sortable : false} 
				       		   	,{field: "IdNombreConcepto", title: "UI_NOMBRE_CONCEPTO", width: "420px", sortable : false}
				       		   	,{field: "IdNombreConceptoGrupo", title: "UI_NOMBRE_GRUPO_CONCEPTO", width: "420px", sortable : false}
				       		   	,{field: "IdItem", title: "UI_ID_ITEM", width: "100px", sortable : false, align: "center", hidden: true }
 				       		    ,{field: "NombreItem", title: "UI_NOMBRE_ITEM", width: "400px", sortable : false, hidden: true }
				       		   	,{field: "IdNombreItem", title: "UI_NOMBRE_ITEM", width: "400px", sortable : false }
				       		   	,{field: "Posicion", title: "UI_POSICION", width: "70px", sortable : false }
			   				    ,{field: "CuentaContable", title: "UI_CUENTA_CONTABLE", width: "100px", sortable : false }				       				    
			   				    ,{field: "SaldoDebeHaber", title: "UI_D_H", width: "30px", sortable : false, align: "center" }	
			   				    ,{field: "Condicion", title: "UI_CON", width: "50px",  sortable : false , align: "center" }				       				    
			   				    ,{field: "MontoCondicion", title: "UI_MONTO_CONDICION", width: "90px",  sortable : false, align: "right" , template :"#: (util.bcs_formato(MontoCondicion,'GEN','Monto','CLP','','INDICE')).valor #"}
			   				    ,{field: "Operacion", title: "UI_OPE", width: "60px",  sortable : false, align: "center" }
			   				    ,{field: "Factor", title: "UI_FACTOR", width: "60px", sortable : false, align: "right", template : "#: (util.bcs_formato(Factor,'GEN','Cantidad','CLP','','')).valor #"}
		   			    		,{field: "MontoCalculado", title: "UI_MONTO_CALCULADO", width: "90px",  sortable : false, align: "right" , template :"#: (util.bcs_formato(MontoCalculado,'GEN','Monto','CLP','','INDICE')).valor #"}
		   			    		,{field: "MontoContable", title: "UI_MONTO_CONTABLE", width: "90px",  sortable : false, align: "right" , template :"#: (util.bcs_formato(MontoContable,'GEN','Monto','CLP','','INDICE')).valor #"}
		   			    		,{field: "MontoDigitado", title: "UI_MONTO_DIGITADO", width: "90px",  sortable : false, align: "right" , template :"#: (util.bcs_formato(MontoDigitado,'GEN','Monto','CLP','','INDICE')).valor #"}
		   			    		,{field: "MontoProgramado", title: "UI_MONTO_PROGRAMADO", width: "90px",  sortable : false, align: "right" , template :"#: (util.bcs_formato(MontoProgramado,'GEN','Monto','CLP','','INDICE')).valor #"}
		   			    		,{field: "MayorACero", title: "UI_MAYOR_CERO_SIMBOLO", width: "30px",  sortable : false , align: "center"}				       				    
			   				    ,{field: "MenorACero", title: "UI_MENOR_CERO_SIMBOLO", width: "30px",  sortable : false, align: "center"}					       				    
			   				    ,{field: "PermiteCero", title: "UI_PERMITE_CERO_SIMBOLO", width: "30px",  sortable : false, align: "center"}	
			   				    ,{field: "ProcesoFuncion", title: "UI_PROCESO_FUNCION", width: "200px", sortable : false, align: "left" }				       				    
		   			    		,{field: "NombreTipoItem", title: "UI_NOMBRE_TIPO_ITEM", width: "200px",  sortable : false, align: "left" }
		   			    	],		
		   	sortable	: true, 
    		pageable	: true,
    		filterable	: true,
    		navigatable : true,
			userSettings: true,
    		height		: 500, 
    		excel 		: { allPages: true },
    		scrollable	: true,
    		selectable 	: "row",
    		autoBind 	: false		
       	};		
		
		//DETALLE COBERTURA

		var dsDefILSDetalleCobertura = 
		{
			type : "array",
			definition : 
			{
                //serverFiltering : true, // Filtrado de datos en el servidor.
                //serverPaging    : true, // Paginado de los datos en el servidor.
                //serverSorting   : true, // Ordenamiento de los datos en el servidor.
                sort 			: { field: "IdOperacion", dir: "DESC" },
                pageSize        : 15,
//                    transport		: {
//	            		read:  
//	            		{
//	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=513",
//							dataType:"json"
//	            		}
//	            	},
                schema 			: { 
                  				   model: 
                   						{
											fields :  
											{
												Titulo					: {type: "string"},
												Categoria				: {type: "string"},
												MontoGarantia    		: {type: "number"},
												MontoBancosyEmpresas	: {type: "number"},
												FactorA					: {type: "number"},
												FactorB 			    : {type: "number"},
												MontoCobertura    		: {type: "number"},
												MontoEntidadFinanciera	: {type: "number"},
												Fecha					: {type: "string"},
												IdEmpresa				: {type: "number"},
											}
                    					}
		        }
    		}
		};	
		var dsGrillaDetalleCobertura = new bcs.newDataSource(dsDefILSDetalleCobertura);
		mantenedorILS.confDetalleCobertura = 
		{
    		dataSource		: dsGrillaDetalleCobertura,
			columns		: [  {field: "Titulo", title: "UI_CARTERA_OBLIGACIONES", width: "130px", sortable : true }
							,{field: "Categoria", title: "UI_GLOSA", width: "230px", sortable : false }
						    ,{field: "MontoGarantia", title: "UI_MONTO_GARANTIA", width: "90px",  sortable : false, align: "right",template :"#: (util.bcs_formato(MontoGarantia,'GEN','Monto','CLP','','INDICE')).valor #"}
						    ,{field: "MontoBancosyEmpresas", title: "UI_BANCOS_Y_EMPRESAS", width: "100px", sortable : false, align: "right", template :"#: (util.bcs_formato(MontoBancosyEmpresas,'GEN','Monto','CLP','','INDICE')).valor #"}
						    ,{field: "FactorA", title: "UI_FACTOR_A", width: "90px",  sortable : false}				   				    
						    ,{field: "FactorB", title: "UI_FACTOR_B", width: "90px", sortable : false }
						    ,{field: "MontoCobertura", title: "UI_COBERTURA", width: "90px", sortable : false, align: "right", template :"#: (util.bcs_formato(MontoCobertura,'GEN','Monto','CLP','','INDICE')).valor #" }
						  ],		
    		sortable		: true,
    		pageable		: false,
    		filterable		: true,
    		navigatable 	: true,
			userSettings	: true,
    		height			: 500,
    		excel 			: { allPages: true },
    		scrollable		: true,
    		selectable 		: "row",
    		autoBind 		: false		
       	};	
		
		/* Inicio de DetalleSaldoCuentaContable	*/		
		var dataSourceDetalleSaldoCuentaContable = new kendo.data.TreeListDataSource({
			data : [],
            schema: {
            	model: {
            		id: 'IdCodigoContabilidad',
                    parentId: 'IdCodigoContabilidadPadre',
                    expanded: true,
					fields: {
						IdCodigoContabilidad: {
							type: 'string'
						},
						IdCodigoContabilidadPadre: {
							type: 'string'
						},
						NombreCuenta: {
							type: 'string'
						},						
						Imputable: {
							type: 'string'
						},
						Nivel: {
							type: 'number'
						},
						MontoDebe: {
							type: 'number'
						},
						MontoHaber: {
							type: 'number'
						},						
						Saldo: {
							type: 'number'
						},					
					}
                }
            }
		});

		

		mantenedorILS.confDetalleSaldoCuentaContableTreeList = {
            dataSource: dataSourceDetalleSaldoCuentaContable,
            excel: {
                fileName: $translate.instant('UI_DETALLE_SALDO_CUENTA_CONTABLE') + '.xlsx',
                filterable: true,
            },
            sortable: true,                
            autoBind:true,
            height: 500,
            columns: [                	
              	{ field: 'IdCodigoContabilidad', title: $translate.instant('UI_CUENTA_CONTABLE'), width: '180px', sortable: true},
               	{ field: 'NombreCuenta', title: $translate.instant('UI_NOMBRE_CUENTA'), align: 'left', width: '180px', sortable: true},                	
               	{ field: 'Imputable', title: $translate.instant('UI_IMPUTABLE'), width: '120px', sortable: true},
               	{ field: 'Nivel', title: $translate.instant('UI_NIVEL'), width: '120px', sortable: true},
               	{ field: 'MontoDebe', title: $translate.instant('UI_DEBE'), align: 'right', width: '150px', sortable: true, template: '<div style="text-align:right;">#: (util.bcs_formato(MontoDebe,"GEN","Monto","CLP","","INDICE")).valor #</div>'},
                { field: 'MontoHaber', title: $translate.instant('UI_HABER'), align: 'right', width: '150px', sortable: true, template: '<div style="text-align:right;">#: (util.bcs_formato(MontoHaber,"GEN","Monto","CLP","","INDICE")).valor #</div>'},
               	{ field: 'Saldo', title: $translate.instant('UI_SALDO'), align: 'right', width: '150px', sortable: true, template: '<div style="text-align:right;">#: (util.bcs_formato(Saldo,"GEN","Monto","CLP","","")).valor #</div>'},
            ]
		};		
		/* Fin DetalleSaldoCuentaContable */ 

		//Exposicion CPRO
		var dsDefILSExposicionCPRO = 
		{
			type : "array",
			definition : 
			{
                serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                sort 			: { field: "Nemotecnico", dir: "DESC" },
                pageSize        : 15,
//                    transport		: {
//	            		read:  
//	            		{
//	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=513",
//							dataType:"json"
//	            		}
//	            	},
                schema 			: { 
                  				   model: 
                   						{
                  					 fields :  
										{
											Nemotecnico					: {type: "string"},
											DelDia						: {type: "string"},
											EnGarantia					: {type: "string"},
											Contable					: {type: "string"},
											PorCompromisos				: {type: "string"},
											TotalTenencia				: {type: "string"},
											DeudasValores				: {type: "string"},
											Precio						: {type: "string"},
											FactorAseguramiento			: {type: "string"},
											ContableValorizada			: {type: "string"},
											PorCompromisosValorizada	: {type: "string"},
											DeudasValoresValorizada		: {type: "string"},
											TotalValorizado				: {type: "string"},
											ValoresEnCirculacion		: {type: "string"},
											PorcentajeTenencia			: {type: "string"},
											FactorPonderacion			: {type: "string"},
											TenenciaPonderada			: {type: "string"},
										}
                    					}
		        }
    		}
		};	
		var dsGrillaExposicionCPRO = new bcs.DataSource(dsDefILSExposicionCPRO);
		mantenedorILS.confExposicionCPRO = 
		{
    		dataSource		: dsGrillaExposicionCPRO,
			columns		: [  {field: "Nemotecnico", title: "UI_NEMOTECNICO", width: "130px", sortable : true }
							,{field: "DelDia", title: "UI_DEL_DIA", width: "130px", sortable : true }
							,{field: "EnGarantia", title: "UI_EN_GARANTIA", width: "130px", sortable : true }
							,{field: "Contable", title: "UI_CONTABLE", width: "130px", sortable : true }
							,{field: "PorCompromisos", title: "UI_POR_COMPROMISOS", width: "130px", sortable : true }
							,{field: "TotalTenencia", title: "UI_TOTAL_TENENCIA", width: "130px", sortable : true }
							,{field: "DeudasValores", title: "UI_DEUDAS_VALORES", width: "130px", sortable : true }
							,{field: "Precio", title: "UI_PRECIO", width: "130px", sortable : true }
							,{field: "FactorAseguramiento", title: "UI_FACTOR_ASEGURAMIENTO", width: "130px", sortable : true }
							,{field: "ContableValorizada", title: "UI_CONTABLE_VALORIZADA", width: "130px", sortable : true }
							,{field: "PorCompromisosValorizada", title: "UI_POR_COMPRAS_VALORIZADA", width: "130px", sortable : true }
							,{field: "DeudasValoresValorizada", title: "UI_DEUDAS_VALORES_VALORIZADA", width: "130px", sortable : true }
							,{field: "TotalValorizado", title: "UI_TOTAL_VALORIZADO", width: "130px", sortable : true }
							,{field: "ValoresEnCirculacion", title: "UI_VALORES_EN_CIRCULACION", width: "130px", sortable : true }
							,{field: "PorcentajeTenencia", title: "UI_PORCENTAJE_TENENCIA", width: "130px", sortable : true }
							,{field: "FactorPonderacion", title: "UI_FACTOR_PONDERACION", width: "130px", sortable : true }
							,{field: "TenenciaPonderada", title: "UI_TENENCIA_PONDERADA", width: "130px", sortable : true }				   				    
						  ],		
    		sortable		: true,
    		pageable		: false,
    		filterable		: true,
    		navigatable 	: true,
			userSettings	: true,
    		height			: 500,
    		excel 			: { allPages: true },
    		scrollable		: true,
    		selectable 		: "row",
    		autoBind 		: false		
       	};
		
		//Posiciones Descubiertas
		var dsDefILSPosicionesDescubiertas = 
		{
			type : "array",
			definition : 
			{
                serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                sort 			: { field: "Nemotecnico", dir: "DESC" },
                pageSize        : 15,
//                    transport		: {
//	            		read:  
//	            		{
//	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=513",
//							dataType:"json"
//	            		}
//	            	},
                schema 			: { 
                  				   model: 
                   						{
                  					 fields :  
										{
											NombreDelComitente	: {type: "string"},
											Rut					: {type: "string"},
											FechaLiquidacion	: {type: "string"},
											FechaOperacion		: {type: "string"},
											NumeroPoliza		: {type: "string"},
											Mercado				: {type: "string"},
											Operacion			: {type: "string"},
											Cobertura			: {type: "string"},
											Moneda				: {type: "string"},
											GarantiaDinero		: {type: "string"},
											GarantiaValores		: {type: "string"},
											TotalGarantias		: {type: "string"},
											Descubierta			: {type: "string"},
											Incomplimientos		: {type: "string"},
											Total				: {type: "string"}

										}
                    				}
		        }
    		}
		};	
		var dsGrillaPosicionesDescubiertas = new bcs.DataSource(dsDefILSPosicionesDescubiertas);
		mantenedorILS.confPosicionesDescubiertas = 
		{
    		dataSource		: dsGrillaPosicionesDescubiertas,
			columns		: [  {field: "NombreDelComitente", title: "UI_NOMBRE", width: "130px", sortable : true }
							,{field: "Rut", title: "UI_RUT", width: "130px", sortable : true }
							,{field: "FechaLiquidacion", title: "UI_FECHA_LIQUIDACION", width: "130px", sortable : true }
							,{field: "FechaOperacion", title: "UI_FECHA_OPERACION", width: "130px", sortable : true }
							,{field: "NumeroPoliza", title: "UI_NUMERO_POLIZA", width: "130px", sortable : true }
							,{field: "Mercado", title: "UI_MERCADO", width: "130px", sortable : true }
							,{field: "Operacion", title: "UI_OPERACION", width: "130px", sortable : true }
							,{field: "Cobertura", title: "UI_COBERTURA", width: "130px", sortable : true }
							,{field: "Moneda", title: "UI_MONEDA", width: "130px", sortable : true }
							,{field: "GarantiaDinero", title: "UI_GARANTIA_EN_DINERO", width: "130px", sortable : true }
							,{field: "GarantiaValores", title: "UI_GARANTIA_EN_VALORES", width: "130px", sortable : true }
							,{field: "TotalGarantias", title: "UI_TOTAL_GARANTIAS", width: "130px", sortable : true }
							,{field: "Descubierta", title: "UI_DESCUBIERTA", width: "130px", sortable : true }
							,{field: "Incomplimientos", title: "UI_INCOMPLIMIENTOS", width: "130px", sortable : true }
							,{field: "Total", title: "UI_TOTAL", width: "130px", sortable : true }		   				    
						  ],		
    		sortable		: true,
    		pageable		: false,
    		filterable		: true,
    		navigatable 	: true,
			userSettings	: true,
    		height			: 500,
    		excel 			: { allPages: true },
    		scrollable		: true,
    		selectable 		: "row",
    		autoBind 		: false		
       	};
		
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		
		mantenedorILS.DigitarResultadoUI = function()
	    {
			var items = {Reporte: mantenedorILS.reporte.seleccionado.IdTipoInforme, FechaContable: mantenedorILS.kngFechaContable, IdEmpresa: mantenedorILS.empresa.seleccionado.IdEmpresa};
			var IdVentanamodal = bcsComunes.ventanaAbrir("ILSMantenedorDigitado", {items : items}, false, false);
		    var promesa=bcsComunes.ventanaPromesa(IdVentanamodal);
		    promesa.then(function(data){
		    	if(typeof data !== 'undefined')
		    		if(data.Resultado === 'OK'){
		        		bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_ESTADO_ACTUALIZADO"),
							conflictos:util.parseServerResponse(response)
						},$scope.winID);	 
		        	}
		        	else if (data.Resultado === 'ERROR')
		        	{
		    			bcsComunes.ventanaMsg({
		    				mensaje		:$translate.instant("UI_ERROR_ACTUALIZAR"),
		  			      	conflictos	:{
		  			      	errores 	: [ {
		  				           				Codigo 		: 'COD',
		  				           				Descripcion : ''
		  				          } ],
		  				    }
		    			},$scope.winID);
		        	}	
		    	$scope.data.habilitaDig = true;
		    }); 
		};	
		

		mantenedorILS.Exportar = function(){
			if($("#TabResultado").hasClass("active") && dsGrillaResultados.data().length > 0){
				var data = [];
	    		var columns = [  { field: "IdIndice", title: "UI_ID_INDICE", width: "80px", sortable : false }
				,{field: "NombreIndice", title: "UI_NOMBRE_INDICE", width: "200px", sortable : false }
				,{field: "MontoIndice", title: "UI_INDICE", width: "60px",  sortable : false , template : "#: (util.bcs_formato(MontoIndice,'GEN','Monto','CLP','','INDICE')).valor #"}
				,{field: "FechaContable", title: "UI_FECHA_CONTABLE", width: "90px", sortable : false}
				,{field: "IdIndiceAnterior", title: "UI_INDICE_ANTERIOR", width: "60px",  sortable : false, template : "#: (util.bcs_formato(IdIndiceAnterior,'GEN','Monto','CLP','','INDICE')).valor #"}
				,{field: "FechaContableAnterior", title: "UI_FECHA_CONTABLE_ANTERIOR", width: "100px", sortable : false }
				];		
	    		data = _.map(columns,function(element, index){
					return { field: element.field, title: element.title};
				});
	    		
	    		var dsExcelResultado = new bcs.DataSource({
	    			type 		: "array",
	    			definition	: 	{
										data		: dsGrillaResultados.data(),
	    				                sort 		: { field: "IdIndice", dir: "ASC" },
	    					            schema 		: { 
	                        								model:{
	                        										fields :{
	    																		IdIndice	     		: {type: "number"},
	    																		NombreIndice 			: {type: "string"},
	    																		MontoIndice    			: {type: "number"},
	    																		FechaContable			: {type: "sysdate"},
	    																		IdIndiceAnterior		: {type: "number"},
	    																		FechaContableAnterior	: {type: "sysdate"},
	                        										}
	                        								}
	    					            }
	    			}
	    		});

	    		dsExcelResultado.fetch().then(function(){
	    			var nombreTitulo = $scope.SesionFechaSistema.replace('-', '').replace('-', '') + '-' + 
	    								$scope.SesionHoraSistema.replace(':','').replace(':','') +
	    								' Resultado Informe';
	    			dsExcelResultado.exportToExcel(data, nombreTitulo, nombreTitulo);
	    		});		
			}else if($("#TabConceptos").hasClass("active") && dsGrillaConceptos.data().length > 0){
				var data = [];
	    		var columns = [  {field: "IdIndiceNombre", title: "UI_NOMBRE_INDICE", width: "150px", sortable : false }
								,{field: "IdConceptoNombre", title: "UI_NOMBRE_CONCEPTO", width: "450px", sortable : false }
								,{field: "Posicion", title: "UI_POSICION", width: "80px", sortable : false }									
								,{field: "Monto", title: "UI_CONCEPTO", width: "150px", sortable : false}
								,{field: "Operador", title: "UI_OPER", width: "50px", sortable : false}
							    ,{field: "Factor", title: "UI_FACT", width: "50px", sortable : false }				   				    
					   			,{field: "MontoCalculado", title: "UI_MONTO_CALCULADO", width: "100px",  sortable : false}];		
	    		data = _.map(columns,function(element, index){
					return { field: element.field, title: element.title};
				});
	    		
	    		var dsExcelConceptos = new bcs.DataSource({
	    			type 		: "array",
	    			definition	: 	{
										data		: dsGrillaConceptos.data(),
	    				                sort 		: { field: "IdIndiceNombre", dir: "ASC" },
	    					            schema 		: { 
	                        								model:{
	                        										fields :{
			                        											IdIndice   	            : {type: "number"},
			                    												NombreIndice			: {type: "string"},
			                    												IdIndiceNombre			: {type: "string"},
			                    												IdResultadoIndice   	: {type: "number"},
			                    												IdConcepto   	        : {type: "number"},
			                    												NombreConcepto			: {type: "string"},
			                    												IdConceptoNombre		: {type: "string"},
			                    												IdResultadoConcepto		: {type: "number"},
			                    												Monto   	            : {type: "number"},
			                    												Posicion			    : {type: "string"},													
			                    												Operador				: {type: "string"},													
			                    												Factor					: {type: "string"},													
			                    												MontoCalculado			: {type: "number"},
			                    												Estado					: {type: "string"},	
	                        										}
	                        								}
	    					            }
	    			}
	    		});

	    		dsExcelConceptos.fetch().then(function(){
	    			var nombreTitulo = $scope.SesionFechaSistema.replace('-', '').replace('-', '') + '-' + 
					$scope.SesionHoraSistema.replace(':','').replace(':','') +
					' Conceptos';
	    			dsExcelConceptos.exportToExcel(data, nombreTitulo, nombreTitulo);
	    		});		
			}else if($("#TabItems").hasClass("active") && dsGrillaItems.data().length > 0){
				var data = [];
	    		var columns = [  { field: "IdNombreIndice", title: "UI_NOMBRE_INDICE", width: "150px", sortable : false}
								,{field: "IdNombreConcepto", title: "UI_NOMBRE_CONCEPTO", width: "150px", sortable : false }
								,{field: "Posicion", title: "UI_POSICION", width: "80px", sortable : false }
								,{field: "IdNombreItem", title: "UI_NOMBRE", width: "100px", sortable : false }
							    ,{field: "Monto", title: "UI_MONTO", width: "90px",  sortable : false , template : "#: (util.bcs_formato(Monto,'GEN','Monto','CLP','','INDICE')).valor #"}
							    ,{field: "Operacion", title: "UI_OPER", width: "50px", sortable : false, align: "center"}
							    ,{field: "Factor", title: "UI_FACT", width: "60px", sortable : false, align: "right"}				   				    
							    ,{field: "MontoCalculado", title: "UI_MONTO_CALCULADO", width: "100px", sortable : false, template : "#: (util.bcs_formato(MontoCalculado,'GEN','Monto','CLP','','INDICE')).valor #"}];
	    		data = _.map(columns,function(element, index){
					return { field: element.field, title: element.title};
				});
	    		
	    		var dsExcelItems = new bcs.DataSource({
	    			type 		: "array",
	    			definition	: 	{
										data		: dsGrillaItems.data(),
	    				                sort 		: { field: "IdNombreIndice", dir: "ASC" },
	    					            schema 		: { 
	                        								model:{
	                        										fields :{
	                    												IdNombreIndice       		: {type: "string"},
	                    												IdNombreConcepto			: {type: "string"},
	                    												Posicion					: {type: "string"},
	                    												IdNombreItem				: {type: "string"},
	                    												Monto						: {type: "number"},
	                    												Operacion					: {type: "string"},	
	                    												Factor						: {type: "string"},													
	                    												MontoCalculado				: {type: "number"},
	                        										}
	                        								}
	    					            }
	    			}
	    		});

	    		dsExcelItems.fetch().then(function(){
	    			var nombreTitulo = $scope.SesionFechaSistema.replace('-', '').replace('-', '') + '-' + 
					$scope.SesionHoraSistema.replace(':','').replace(':','') +
					' Items';
	    			dsExcelItems.exportToExcel(data, nombreTitulo, nombreTitulo);
	    		});	
			}else if($("#TabDetalleItem").hasClass("active") && dsGrillaDetalleItem.data().length > 0){
				var data = [];
	    		var columns = [  {field: "IdNombreConcepto", title: "UI_NOMBRE_CONCEPTO", width: "420px", sortable : false}
       		   					,{field: "Posicion", title: "UI_POSICION", width: "70px", sortable : false }
       		   					,{field: "IdNombreItem", title: "UI_NOMBRE_ITEM", width: "400px", sortable : false }
							    ,{field: "Monto", title: "UI_MONTO", width: "90px", sortable : false , template :"#: (util.bcs_formato(Monto,'GEN','Monto','CLP','','INDICE')).valor #"}
							    ,{field: "CuentaContable", title: "UI_CUENTA_CONTABLE", width: "100px", sortable : false }				       				    
							    ,{field: "SaldoDebeHaber", title: "UI_D_H", width: "30px", sortable : false, align: "center" }	
							    ,{field: "Condicion", title: "UI_CON", width: "50px",  sortable : false , align: "center" }				       				    
							    ,{field: "MontoCondicion", title: "UI_MONTO_CONDICION", width: "90px",  sortable : false, align: "right" , template :"#: (util.bcs_formato(MontoCondicion,'GEN','Monto','CLP','','INDICE')).valor #"}
							    ,{field: "Operacion", title: "UI_OPE", width: "60px",  sortable : false, align: "center" }				       				    
							    ,{field: "Factor", title: "UI_FACTOR", width: "60px", sortable : false, align: "right", template : "#: (util.bcs_formato(Factor,'GEN','Cantidad','CLP','','')).valor #"}
					    		,{field: "MontoCalculado", title: "UI_MONTO_CALCULADO", width: "90px",  sortable : false, align: "right" , template :"#: (util.bcs_formato(MontoCalculado,'GEN','Monto','CLP','','INDICE')).valor #"}
					    		,{field: "MayorACero", title: "UI_MAYOR_CERO_SIMBOLO", width: "30px",  sortable : false , align: "center", template :"# if(IdTipoItem === 'DIG'){# #: MayorACero # #} #"}				       				    
							    ,{field: "MenorACero", title: "UI_MENOR_CERO_SIMBOLO", width: "30px",  sortable : false, align: "center", template :"# if(IdTipoItem === 'DIG'){# #: MenorACero # #} #"}					       				    
							    ,{field: "PermiteCero", title: "UI_PERMITE_CERO_SIMBOLO", width: "30px",  sortable : false, align: "center", template :"# if(IdTipoItem === 'DIG'){# #: PermiteCero # # } #"}	
							    ,{field: "ProcesoFuncion", title: "UI_PROCESO_FUNCION", width: "200px", sortable : false, align: "left" }				       				    
					    		,{field: "NombreTipoItem", title: "UI_NOMBRE_TIPO_ITEM", width: "100px",  sortable : false, align: "left" }];		
	    		data = _.map(columns,function(element, index){
	    			var nombreTitulo = $scope.SesionFechaSistema.replace('-', '').replace('-', '') + '-' + 
					$scope.SesionHoraSistema.replace(':','').replace(':','') +
					' Resultado Informe';
					return { field: element.field, title: element.title};
				});
	    		
	    		var dsExcelDetalleItem = new bcs.DataSource({
	    			type 		: "array",
	    			definition	: 	{
										data		: dsGrillaDetalleItem.data(),
	    				                sort 		: { field: "IdNombreConcepto", dir: "ASC" },
	    					            schema 		: { 
	                        								model:{
	                        										fields :{
	                        													IdNombreConcepto       	: {type: "string"},
	                        													Posicion					: {type: "string"},
				                                         						IdNombreItem				: {type: "string"},
				                                         						Monto					: {type: "number"},
				                                         						CuentaContable			: {type: "string"},
				                                         						SaldoDebeHaber			: {type: "string"},
				                                         						Condicion				: {type: "string"},
				                                         						MontoCondicion			: {type: "number"},
				                                         						Operacion				: {type: "string"},
				                                         						Factor					: {type: "string"},
				                                         						MontoCalculado			: {type: "number"},
				                                         						IdTipoItem				: {type: "string"},
				                                         						MayorACero				: {type: "string"},
				                                         						MenorACero				: {type: "string"},
				                                         						PermiteCero				: {type: "string"},
				                                         						ProcesoFuncion			: {type: "string"},
				                                         						NombreTipoItem			: {type: "string"},
	                        										}
	                        								}
	    					            }
	    			}
	    		});

	    		dsExcelDetalleItem.fetch().then(function(){
	    			var nombreTitulo = $scope.SesionFechaSistema.replace('-', '').replace('-', '') + '-' + 
					$scope.SesionHoraSistema.replace(':','').replace(':','') +
					' Detalle Items';
	    			dsExcelDetalleItem.exportToExcel(data, nombreTitulo, nombreTitulo);
	    		});	
			}else if($("#TabDetalleCobertura").hasClass("active") && dsGrillaDetalleCobertura.data().length > 0){
				var data = [];
	    		var columns = [  {field: "Titulo", title: "UI_CARTERA_OBLIGACIONES", width: "130px", sortable : true }
								,{field: "Categoria", title: "UI_GLOSA", width: "230px", sortable : false }
							    ,{field: "MontoGarantia", title: "UI_MONTO_GARANTIA", width: "90px",  sortable : false }										
							    ,{field: "MontoBancosyEmpresas", title: "UI_BANCOS_Y_EMPRESAS", width: "100px", sortable : false}
							    ,{field: "FactorA", title: "UI_FACTOR_A", width: "90px",  sortable : false}				   				    
							    ,{field: "FactorB", title: "UI_FACTOR_B", width: "90px", sortable : false }
							    ,{field: "MontoCobertura", title: "UI_COBERTURA", width: "90px", sortable : false }];		
	    		data = _.map(columns,function(element, index){
					return { field: element.field, title: element.title};
				});
	    		
	    		var dsExcelDetalleCobertura = new bcs.DataSource({
	    			type 		: "array",
	    			definition	: 	{
										data		: dsGrillaDetalleCobertura.data(),
	    				                sort 		: { field: "Titulo", dir: "ASC" },
	    					            schema 		: { 
	                        								model:{
	                        										fields :{
			                        											Titulo					: {type: "string"},
			                    												Categoria				: {type: "string"},
			                    												MontoGarantia    		: {type: "number"},
			                    												MontoBancosyEmpresas	: {type: "number"},
			                    												FactorA					: {type: "number"},
			                    												FactorB 			    : {type: "number"},
			                    												MontoCobertura    		: {type: "number"},
			                    												MontoEntidadFinanciera	: {type: "number"},
			                    												Fecha					: {type: "string"},
			                    												IdEmpresa				: {type: "number"},
	                        										}
	                        								}
	    					            }
	    			}
	    		});

	    		dsExcelDetalleCobertura.fetch().then(function(){
	    			var nombreTitulo = $scope.SesionFechaSistema.replace('-', '').replace('-', '') + '-' + 
					$scope.SesionHoraSistema.replace(':','').replace(':','') +
					' Detalle Cobertura';
	    			dsExcelDetalleCobertura.exportToExcel(data, nombreTitulo, nombreTitulo);
	    		});	
			}else{
				bcsComunes.ventanaMsg({
				      mensaje:$translate.instant("UI_ERROR_EXPORTAR"),
				      conflictos:{
				       errores : [ {
				           Codigo : 'COD',
				           Descripcion : $translate.instant("UI_NO_DATOS_EXPORTAR")
				          } ],
				      }
				},$scope.winID);
			}
		};
		
		mantenedorILS.ReporteResumidoILSUI = function(gridIndices)
	      {
									
			if ($scope.mantenedorILS.confResultados.dataSource._data.length <= 0){
				bcsComunes.msg("warning","warning",$translate.instant('UI_NO_EXISTEN_REGISTROS'));
				return;				
			}
			
			var idVentanaModal = bcsComunes.ventanaAbrir("ILSMantenedorReporteResumido", {items : gridIndices}, false, false, $scope.winID);	
		    var promesa=bcsComunes.ventanaPromesa(idVentanaModal);		    
		    
		  };
		  
		mantenedorILS.ReporteDetalladoILSUI = function(gridIndices)
	      {
									
			if ($scope.mantenedorILS.confResultados.dataSource._data.length <= 0){
				bcsComunes.msg("warning","warning",$translate.instant('UI_NO_EXISTEN_REGISTROS'));
				return;				
			}
			
			var idVentanaModal = bcsComunes.ventanaAbrir("ILSMantenedorReporteDetallado", {items : gridIndices}, false, false, $scope.winID);	
		    var promesa=bcsComunes.ventanaPromesa(idVentanaModal);		    
		    
		  };		
		
 		mantenedorILS.ConsultaLog = function () {
 			var items = {};
 			var IdVentanamodal = bcsComunes.ventanaAbrir("ConsultaLog", {items : items} , true,true);
 	        var promesa=bcsComunes.ventanaPromesa(IdVentanamodal);
 	        promesa.then(function(data){
 	        	if(typeof data !== 'undefined'){
 	        		mantenedorILS.habilitado 	= true;
 	        	};
 	        });
 		};
 		
 		mantenedorILS.Limpiar = function () {
 			dsGrillaResultados.removeAll();
 			dsGrillaConceptos.removeAll();
 			dsGrillaItems.removeAll();
 			dsGrillaDetalleItem.removeAll();
 			dsGrillaItems.removeAll();
 			dsGrillaDetalleCobertura.removeAll();
 			mantenedorILS.kngFechaContable = util.dateInicioDia($scope.SesionFechaSistema);
 		};
 		 
 		mantenedorILS.GenerarReporteMonedaExtranjera = function() {
 			
 			var parametros = {};
 			parametros.p_IdEmpresa 				= $scope.SesionIdEmpresa; 
 			parametros.p_fechaContable 			= util.dateSPdate(mantenedorILS.kngFechaContable);
 	    	bcsServiciosNormativa.GenerarReporteMonedaExtranjera(parametros,successGenerarReporteMonedaExtranjera,errorGenerarReporteMonedaExtranjera);
 	    };
 	    
 	    var successGenerarReporteMonedaExtranjera = function(respuesta)
 	    {
 	    	if(respuesta.P_OUT_ERRORESCONTADOR === 0){
 	    		bcsComunes.ventanaMsg({
 					mensaje:$translate.instant("UI_OPERACION_REALIZADA_EXITO"),
 					conflictos:util.parseServerResponse(respuesta)
 				},$scope.winID);
 	    		
 	    		mantenedorILS.BuscarResultado();
 	    	}
 	    	if(respuesta.P_OUT_ERRORESCONTADOR != 0)
 	    	{
 	    		$.each(respuesta.P_OUT_ERRORESDETALLE,function(key,value)
 	    		{
 	    			bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant(value.Descripcion));
 	    		});
 	    	}
 	    };
 	    
 	    var errorGenerarReporteMonedaExtranjera = function(respuesta)
 	    {
 			bcsComunes.ventanaMsg({
 				mensaje:$translate.instant("UI_ERROR_GENERAR_REPORTE_MONEDA_EXTRANJERA"),
 				conflictos:util.parseServerResponse(respuesta)
 			},$scope.winID);
 			bcsComunes.ventanaOverlay($scope.winID, false);
 	    };
		 bcsComunes.ventanaAutoHeight(idVentana);
	});
    
}());