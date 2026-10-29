(function() {
	angular.module('ModuloNormativa').controller('PanelOperacionesCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosNormativa,bcsNormativaDOF,
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
	    pct.IdTipoDocumentoComprobante='UAFDOFCOMP';
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
				
		pct.grilla.EST = {
			dataSource: new bcs.DataSource({
				type: "odata",
				//type : "call",
				definition: {
					serverFiltering : false, // Filtrado de datos en el servidor.
					serverPaging 	: false, // Paginado de los datos en el servidor.
					serverSorting 	: false, // Ordenamiento de los datos en el servidor.
					sortable 		: false,
					pageSize 		: 100,
					
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3083",
							dataType: "json"
						}
					},
					/*
					callParameters      : filters,
					callTransaction 	: "3093",
					callResultSet 		: "P_OUT_TABLADOF",
					*/
					schema: {
						model: {
							fields: {
								   IdEmpresa						: { type: "number"},
								   IdCliente						: { type: "number"},
								   DocumentoIdentidad				: { type: "string"},
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
							   
							}
						}
					}
				}
			}),
			columns: [
				    {field: "FechaIngreso",					title: $translate.instant("UI_FECHA_HASTA"),			    width: "80px"	},
				    {field: "DocumentoIdentidad",			title: $translate.instant("UI_DOCUMENTO_IDENTIDAD"),	    width: "80px"	},
					{field: "NombreCliente",				title: $translate.instant("UI_NOMBRE"),						width: "200px"	},
					{field: "ClasificacionTipoInstrumento",	title: $translate.instant("UI_CLASIFICACION"),			    width: "130px"	},
					{field: "TipoInstrumento",				title: $translate.instant("UI_TIPO_INSTRUMENTO"),			width: "130px"	},
					{field: "MontoTotalDias",				title: $translate.instant("UI_MONTO_TOTAL"),				width: "80px"	},
					{field: "CantidadDias",					title: $translate.instant("UI_DIAS"),						width: "40px"	},
					{field: "MontoPromedioDiario",			title: $translate.instant("UI_MONTO_PROMEDIO"),					width: "80px"	},
					{field: "MontoMaximo",					title: $translate.instant("UI_MONTO_MAXIMO"),				width: "80px"	},
					{field: "Moneda",						title: $translate.instant("UI_MONEDA"),						width: "80px"	},
					
			],
			hideDetailButton: function(row) {
				var hide = false;
				//if(row.IdTipoLimite === 'LIMCTPEXCLUIREMISOR' || row.IdTipoLimite === 'LIMCTPMARGENTASAS') 
				//	hide = false;
				return hide;
			},
			pageable	: true,
			pageSize	: 50,
			//autoBind	: true,
			autoBind	: false,
			height		: 420,//580,
		    selectable 	: "row",
		    //selectable 			: "multiple row",
		    //detailInit : function(e) {
			//    	pct.initDetalle(e);},
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
			if (seleccionado[0].FechaLiquidacion !=undefined)
				{
						var fd = util.dateSPdate(seleccionado[0].FechaLiquidacion);
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
				//Datos grillas	
			var objfilters = { };
			var niv1filters = new Array();
	
			objfilters.filters = niv1filters;
	    	
			
			if(pct.filtro.IdSocioDeNegocio && pct.filtro.IdSocioDeNegocio > 0){
	    		niv1filters.push({ field: "IdCliente", operator: "eq", value: pct.SocioDeNegocioObj.IdCliente });
			};
	    	
	    	if (niv1filters.length>0) pct.grilla.EST.dataSource.filters(objfilters);
	    	else pct.grilla.EST.dataSource.filters(new Array());
	    	
	    	pct.grilla.ESTSel = undefined;
				    	
		};
	
		
	
		
	
				
		pct.fn.limpiar = function(){
			
			
			pct.grilla.DOF.dataSource.removeAll();
		};
		

	});
}());