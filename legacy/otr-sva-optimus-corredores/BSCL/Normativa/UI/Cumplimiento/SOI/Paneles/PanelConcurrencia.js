(function() {
	angular.module('ModuloNormativa').controller('PanelConcurrenciaCtrl',
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
	    pct.IdTipoDocumentoComprobante='UAFConcurrenciaCOMP';
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
		pct.dsDetalleConcurrencia = function(){
			return new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering : true,
				transport: {
					read: {
						url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1277",
						dataType: "json"
					}
				},
				schema: {
					model: { fields: {		 	
						IdDeclaracionOrigenFondos 		: { type: "number"},
						IdCliente						: { type: "number"},
					} }
				}
				}
			})
		};
		
		pct.initDetalle = function(e) {
			bcsComunes.ventanaOverlay(pct.idVentana,true);
			console.log("Inicializa el detalle!",e);
			var filterDetalleConcurrencia = [
			    { field: "IdCliente", operator: "eq", value: e.data.IdCliente },
				{ field: "IdDeclaracionOrigenFondos", operator: "eq", value: e.data.IdDeclaracionOrigenFondos }
			];
			var dsDet = pct.dsDetalleConcurrencia();
			dsDet.query({filter:filterDetalleConcurrencia}).then(function() {
				var datos = dsDet.data();
				if (datos.length > 0){
					console.log(datos);
					e.data.detalleConcurrencia = datos;
				}
				bcsComunes.ventanaOverlay(pct.idVentana,false);
			});
		};
				
		pct.grilla.Concurrencia = {
			dataSource: new bcs.DataSource({
				//type: "odata",
				type : "call",
				definition: {
					serverFiltering : false, // Filtrado de datos en el servidor.
					serverPaging 	: false, // Paginado de los datos en el servidor.
					serverSorting 	: false, // Ordenamiento de los datos en el servidor.
					sortable 		: false,
					pageSize 		: 100,
					/*
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3075",
							dataType: "json"
						}
					},
					*/
					callParameters      : filters,
					callTransaction 	: "3076",
					callResultSet 		: "P_OUT_TABLAConcurrencia",
					schema: {
						model: {
							fields: {
								   FechaLiquidacion					: { type: "date"},
								   NombreCompleto					: { type: "string"},
								   RazonSocial						: { type: "string"},
								   ApellidoPaterno					: { type: "string"},
								   ApellidoMaterno					: { type: "string"},
								   MontoOperacion					: { type: "number"},
								   TipoOperacion					: { type: "string"},
								   IdComprobante					: { type: "number"},
								   MontoComprobanteFormaDePago		: { type: "number"},
								   MonedaFormaDePago				: { type: "string"},
								   NombreMedioDePago				: { type: "string"},
								   Comentario						: { type: "string"},
								   IdDeclaracionOrigenFondos		: { type: "number"},
							}
						}
					}
				}
			}),
			columns: [
				    {field: "FechaLiquidacion",				title: $translate.instant("UI_FECHA"),					    width: "50px"	},
					{field: "NombreCompleto",				title: $translate.instant("UI_NOMBRE"),						width: "200px"	},
					{field: "TipoOperacion",				title: $translate.instant("UI_TIPO_OPERACION"),				width: "130px"	},
					{field: "IdComprobante",				title: $translate.instant("UI_COMPROBANTE"),			    width: "50px"	},
					{field: "NombreMedioDePago",			title: $translate.instant("UI_MEDIO_DE_PAGO"),				width: "100px"	},
					{field: "MonedaFormaDePago",			title: $translate.instant("UI_MONEDA"),						width: "80px"	},
					{field: "MontoComprobanteFormaDePago",	title: $translate.instant("UI_MONTO"),						width: "80px"	},
					{field: "Comentario",					title: $translate.instant("UI_COMENTARIO"),					width: "100px"	},
					
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
			height		: 540,//580,
		    //selectable 	: "row",
		    selectable 			: "multiple row",
		    detailInit : function(e) {
			    	pct.initDetalle(e);},
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
			var filters = {};
			filters.p_IdEmpresa 			= $scope.SesionIdEmpresa;
			filters.p_FechaOperacionDesde	= util.dateSPdate(pct.FecInicio) || '0001-01-01';
			filters.p_FechaOperacionHasta	= util.dateSPdate(pct.FecTermino) || '0001-01-01';
			//filters.p_Monto					= 0;		

			if(pct.IdAgente>0){
	    		filters.p_IdEmpleado			= pct.IdAgente;
	    	};
			if(pct.IdSucursal>0){
	    		filters.p_IdSucursal			= pct.IdSucursal;
	    	};
			if(pct.SocioDeNegocioObj.IdCliente && pct.SocioDeNegocioObj.IdCliente > 0){
	    		filters.p_IdCliente= pct.SocioDeNegocioObj.IdCliente;
			};
			
			//dsData.setCallParameters(filters);
			pct.grilla.Concurrencia.dataSource.setCallParameters(filters);
				    	
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
		
		
		
		
		
	

		
		$scope.$watch('pct.grilla.ConcurrenciaSel', function(newval,oldval){
			
			if (angular.equals(newval,oldval)) return;
			if (newval!==false) {
				if(newval !== 'undefined')
				{
					pct.fnVersionEsquemaComprobante(newval);
				}
			}
			
			
		},true);
		
				
		pct.fn.limpiar = function(){
			
			
			pct.grilla.Concurrencia.dataSource.removeAll();
		};
		

	});
}());