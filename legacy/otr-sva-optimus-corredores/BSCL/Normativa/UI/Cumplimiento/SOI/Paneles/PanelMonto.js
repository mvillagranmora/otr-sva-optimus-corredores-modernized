(function() {
	angular.module('ModuloNormativa').controller('PanelMontoCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosNormativa,
				 bcsAlerta, OPCIONES_BCSALERTA,$http){

		
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var pct 		= this;
		pct.idVentana	= bcsComunes.ventanaWinId($scope);
		pct.win		= {};
		pct.grilla		= {};
		pct.cmbbx		= {};
		pct.filtro		= {};
		pct.fn			= {};

		
		pct.filtro.IdClasificacionTipoInstrumentoDef	= 0;
		pct.PeriodoOptions = {format: 'MMMM yyyy', start: 'year',depth: 'year'};
		pct.Periodo 	 = util.dateInicioDia($scope.SesionFechaSistema);
		
		
		pct.TipoArchivo			= 'pdf';
	    pct.IdTipoDocumentoComprobante='UAFMontoCOMP';
		pct.IdVersionEsquemaComprobante 	= '';
		pct.modulo='NOR';
		pct.salidaMensaje='';
		pct.FecInicio 	 = util.dateInicioDia($scope.SesionFechaSistema);
		pct.FecTermino 	 = util.dateInicioDia($scope.SesionFechaSistema);
		
		$scope.dataTemplate	= {};
		var filters = {};
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		pct.dsDetalleMonto = function(){
			return new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering : true,
				transport: {
					read: {
						url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=3083",
						dataType: "json"
					}
				},
				schema: {
					model: { fields: {		 	
						 		   IdEmpresa						: { type: "number"},
								   IdCliente						: { type: "number"},
								   NombreCliente					: { type: "string"},
								   MontoTotalDias					: { type: "number"},
								   CantidadDias						: { type: "number"},
								   MontoPromedioDiario				: { type: "number"},
								   MontoMaximo						: { type: "number"},
								   IdMoneda							: { type: "number"},
								   Moneda							: { type: "string"},
								   IdTipoInstrumento				: { type: "string"},
								   TipoInstrumento					: { type: "string"},
								   IdClasificacionTipoInstrumento	: { type: "string"},
								   ClasificacionTipoInstrumento		: { type: "string"},
								   FechaIngreso						: { type: "date"},
								   FechaCreacion					: { type: "date"},
								   FechaModificacion				: { type: "date"},			
					} }
				}
				}
			})
		};
		
		pct.initDetalle = function(dataItem) {
			bcsComunes.ventanaOverlay(pct.idVentana,true);
			console.log("Inicializa el detalle!",dataItem);
			var filterDetalleMonto = [
			    { field: "IdCliente", operator: "eq", value: dataItem.IdCliente }
			];
			var dsDet = pct.dsDetalleMonto();
			dataItem.datosEst = {};
			dsDet.query({filter:filterDetalleMonto}).then(function() {
				var datos = dsDet.data();
				if (datos.length > 0){
					console.log(datos);
					dataItem.datosEst = datos[0];
					
				}
				bcsComunes.ventanaOverlay(pct.idVentana,false);
			});
		};
				
		pct.grilla.Monto = {
		dataSource: new bcs.DataSource({
				type: "odata",
				//type : "call",
				definition: {
					serverFiltering : true, // Filtrado de datos en el servidor.
					serverPaging 	: true, // Paginado de los datos en el servidor.
					serverSorting 	: true, // Ordenamiento de los datos en el servidor.
					sortable 		: true,
					pageSize 		: 100,
					
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3081",
							dataType: "json"
						}
					},
					
					callParameters      : filters,
					//callTransaction 	: "3081",
					//callResultSet 		: "P_OUT_TABLAPerfil",
					schema: {
						model: {
							fields: {
							   IdEvaluacion					: { type: "number"},
							   IdOperacion					: { type: "number"},
							   IdEmpresa					: { type: "number"},
							   IdModulo						: { type: "string"},
							   IdCliente					: { type: "number"},
							   NombreCliente				: { type: "string"},
							   CodigoPortafolio				: { type: "string"},
							   DocumentoIdentidad			: { type: "string"},
							   IdPortafolio					: { type: "number"},
							   IdTipoProducto				: { type: "string"},
							   TipoProducto					: { type: "string"},
							   IdTipoOperacion				: { type: "string"},
							   TipoOperacion				: { type: "string"},
							   IdOperacion					: { type: "number"},
							   MontoUtilizado				: { type: "number"},
							   IdMoneda						: { type: "string"},
							   Moneda						: { type: "string"},
							   IdTipoInstrumento			: { type: "string"},
							   TipoInstrumento				: { type: "string"},
							   IdClasificacionTipoInstrumento	: { type: "string"},
							   ClasificacionTipoInstrumento		: { type: "string"},
							   FechaOperacion				: { type: "string"},
							   //EVALUACION							   
							   TipoEvaluacion				: { type: "string"},
 							   EstMontoPromedioDiario		: { type: "number"},
 							   EstMontoMaximo				: { type: "number"},
 							   Patrimonio					: { type: "number"},
						   	   MontoEvaluado				: { type: "number"},
						   	   Factor						: { type: "number"},
							   
							   FechaIngreso					: { type: "date"},
							   FechaCreacion				: { type: "date"},
							   CodigoUsuarioCreador			: { type: "string"},
							   TipoEvaluacion				: { type: "string"}
							}
						}
					}
				}
			}),
			columns: [
				    {field: "FechaIngreso",					title: $translate.instant("UI_FECHA_INGRESO"),					    width: "80px"	},
					{field: "IdOperacion",					title: $translate.instant("UI_OPERACION"),					width: "80px"	},
					{field: "DocumentoIdentidad",			title: $translate.instant('UI_DOCUMENTO_IDENTIDAD'),		width: "80px", align: "left"},
					{field: "NombreCliente",				title: $translate.instant("UI_NOMBRE"),						width: "200px"	},
					{field: "CodigoPortafolio",				title: $translate.instant('UI_PORTAFOLIO'),					width: "100px", align: "left"},
					{field: "FechaOperacion",				title: $translate.instant("UI_FECHA_OPERACION"),	    width: "80px"	},
					{field: "ClasificacionTipoInstrumento", title: $translate.instant("UI_CLASIFICACION"),	width: "130px"	},
					{field: "TipoInstrumento",				title: $translate.instant("UI_TIPO_INSTRUMENTO"),				width: "130px"	},
					{field: "MontoUtilizado",				title: $translate.instant("UI_MONTO"),	template: "<label> #: (util.bcs_formato(MontoUtilizado,'ORD','MONTO',IdMoneda,'','')).valor #</label>",	width: "80px"	},
					{field: "Moneda",						title: $translate.instant("UI_MONEDA"),						width: "80px"	},
					{field: "TipoEvaluacion",				title: $translate.instant("UI_CONCEPTO_EVALUACION"),		width: "100px"	},
					{field: "MontoEvaluado",				title: $translate.instant("UI_MONTO_EVALUACION"),	template: "<label> #: (util.bcs_formato(MontoEvaluado,'ORD','MONTO',IdMoneda,'','')).valor #</label>",	width: "80px"	},
					{field: "Factor",						title: $translate.instant("UI_FACTOR_TOLERANCIA"),						width: "80px"	},
						
			],
			hideDetailButton: function(row) {
				var hide = false;
				return hide;
			},
			pageable	: true,
			pageSize	: 50,
		    selectable 	: "row",
		    rowCellColor : function(row) {
		    	var column = new Array();
		    	column.push({column: "TipoEvaluacion", color: "red"});
		    	
		    	return column;
		    },
			pageable: {
                refresh: true,
                buttonCount: 5
            },
			autoBind	: false,
			height		: 540,
		    detailInit : function(e) {
			    	pct.initDetalle(e.data);},
		    userSettings		: true,
			navigatable			: true,
			excel : {
				allPages: true,
			},
		    filterable	: true
		};
		
	
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		pct.cmbbx.SocioDeNegocio={
			filtros: {
				IdEmpresa : {field: "IdEmpresa",operator: "eq", value: $scope.$root.SesionIdEmpresa },
				EsCliente : {field: "EsCliente",operator: "eq", value: 'UI_SI'}
			}
		};
		

		/*********************************************
		 * FUNCIONES
		 *********************************************/
		pct.fn.envioFecha = function(){
			var date = '';
			date= util.dateSPdate(pct.Periodo);
			pct.PeriodoEnv= date.substring(0,4)  + date.substring(5,7);
						
			var dateFin = new Date();
			dateFin 	 = pct.Periodo;
			var ultimoDia = new Date(dateFin.getFullYear(), dateFin.getMonth() + 1, 0);
			pct.PeriodoEnvFinal= util.dateInicioDia(ultimoDia);
			
		};
		

		pct.fnVersionEsquemaComprobante = function(seleccionado) {
			if (seleccionado.FechaLiquidacion !=undefined)
				{
						var fd = util.dateSPdate(seleccionado.FechaLiquidacion);
						var paramServicio = {};
						paramServicio.p_TipoDocumento = pct.IdTipoDocumentoComprobante;
						if (fd !== null || fd !== undefined) {
							paramServicio.p_Fecha = fd;
						}
						else 
						{
							paramServicio.p_Fecha = $scope.SesionFechaSistema;
						}
						
						bcsServiciosNormativa.ObtenerVersionEsquema(paramServicio, function(respuesta) {
							if (respuesta.P_OUT_VERSIONESQUEMA)
							{
								pct.IdVersionEsquemaComprobante=respuesta.P_OUT_VERSIONESQUEMA[0].P_OUT_VERSION;
							}
							else
							{	
								bcsComunes.msg('error', $scope.TITLE, 'UI_ERROR_VERSIONESQUEMA_NO_DEFINIDO');
							}		
							
						}, function(error){
							bcsComunes.msg('error', $scope.TITLE, error.message);
						});	
				};	
		};	
		
		
		pct.fn.buscar = function(){
			var objfilters = { };
			var niv1filters = new Array();
	
			objfilters.filters = niv1filters;
			niv1filters.push({ field: "UI_Panel", operator: "eq", value: 'MONTO' });
			if (pct.FecInicio) {
				niv1filters.push({ field: "FechaIngreso", operator: "gte", value:util.dateInicioDia(pct.FecInicio) });

			};
			if (pct.FecTermino) {
				niv1filters.push({ field: "FechaIngreso", operator: "lte", value:util.dateTerminoDia(pct.FecTermino) });

			};
	    	
			if(pct.filtro.IdSocioDeNegocio && pct.filtro.IdSocioDeNegocio > 0){
	    		niv1filters.push({ field: "IdCliente", operator: "eq", value: pct.SocioDeNegocioObj.IdCliente });
			};
	    	if (niv1filters.length>0) pct.grilla.Monto.dataSource.filter(objfilters);
	    	else pct.grilla.Monto.dataSource.filter(new Array());
	    	pct.grilla.MontoSel = undefined;
		};
		
		
		
		pct.ConsultaArchivoNormativa = new bcs.DataSource({
		type 		: "odata",
		definition 	: {
			serverFiltering	: true,
			transport		: {
				read : {
					url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1674",
					dataType : "json",
				}
			},
			schema 				: {
				model: {
					fields : {
					    IdArchivoNormativa		: {type: "string"},
						VersionArchivo			: {type: "string"},
						Esquema					: {type: "string"},
						Version 				: {type: "string"},
						IdTipoDocumento 		: {type: "string"},
						Descripcion				: {type: "string"},
						NombreArchivo			: {type: "string"},
						IdArchivo				: {type: "string"},
						TipoArchivo				: {type: "string"},
						ServicioArchivo			: {type: "string"},
						IdEmpresa				: {type: "number"},
						IdEstado				: {type: "string"}						
					}
				}
			}
		},
		});	
		
		
		pct.EstadisticaCliente = function(seleccionado) {
						ventanaEstadisticaCliente = bcsComunes.ventanaAbrir("EstadisticaOperacionCliente", {PrevSeleccion :seleccionado}, true, false, pct.idVentana);
						var promesa=bcsComunes.ventanaPromesa(ventanaEstadisticaCliente);
						 promesa.then(function(data){
						 });
		};
		
		
		
	

		
		$scope.$watch('pct.grilla.MontoSel', function(newval,oldval){
			
			if (angular.equals(newval,oldval)) return;
			if (newval!==false) {
				if(newval !== 'undefined')
				{
					pct.fnVersionEsquemaComprobante(newval);
				}
			}
			
			
		},true);
		
				
		pct.fn.limpiar = function(){
			
			
			pct.grilla.Monto.dataSource.removeAll();
		};
		

	});
}());