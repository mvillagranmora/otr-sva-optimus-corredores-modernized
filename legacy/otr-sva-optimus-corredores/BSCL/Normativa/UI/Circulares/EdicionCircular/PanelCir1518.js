(function() {
	angular.module('ModuloNormativa').controller('PanelCir1518Ctrl',
	function($scope,$translate,bcsComunes,bcsServiciosNormativa,
				 bcsAlerta, OPCIONES_BCSALERTA, $rootScope,$http){

		
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
	    pct.IdTipoDocumentoComprobante='UAFPerfilCOMP';
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
		
		pct.grilla.Cir1518 = {
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
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3081",
							dataType: "json"
						}
					},
					*/
					
					callParameters      : filters,
					callTransaction 	: "3563",
					callResultSet 		: "P_OUT_TABLASALIDA",
					schema: {
						model: {
							fields: {

							    Nemotecnico						: { type: "string"},
								CantidadCustodia				: { type: "number"},
								CantidadGarantia				: { type: "number"},
								CantidadPlazoVenta				: { type: "number"},
								CantidadPlazoCompra				: { type: "number"},
								CantidadTotal					: { type: "number"},
								CantidadDCVRelacionado			: { type: "number"},
								CantidadDCVTerceros				: { type: "number"},
								CantidadDCVCuentaPropia			: { type: "number"},
								CantidadBCS						: { type: "number"},
								CantidadBEC						: { type: "number"},
								CantidadBOVALPO					: { type: "number"},
								CantidadEmisores				: { type: "number"},
								CantidadVentasEnTransito		: { type: "number"},
								CantidadComprasEnTransito		: { type: "number"},
								CantidadTransitoIngreso			: { type: "number"},
								CantidadTransitoEgreso			: { type: "number"},
								TotalCustodiosyEmisores			: { type: "number"},
								Diferencia						: { type: "string"},
								ExplicacionDescuadratura		: { type: "string"},
								Tipo							: { type: "string"},
							    IdCuadratura					: { type: "number"},
								SorteoLetras					: { type: "number"}

							}
						}
					}
				}
			}),
			columns: [
				    {field: "Nemotecnico",					title: $translate.instant("UI_NEMO"),					    width: "150px"	},
				    {field: "CantidadCustodia",				title: $translate.instant("UI_CANTIDAD_CUSTODIA"),			width: "100px"	},
					{field: "CantidadGarantia",				title: $translate.instant("UI_CANTIDAD_GARANTIA"),			width: "100px"	},
					{field: "CantidadPlazoVenta",			title: $translate.instant("UI_CANTIDAD_PLAZO_VENTA"),		width: "100px"	},
					{field: "CantidadPlazoCompra",			title: $translate.instant("UI_CANTIDAD_PLAZO_COMPRA"),		width: "100px"	},
					{field: "CantidadTotal",				title: $translate.instant("UI_CANTIDAD_TOTAL"),				width: "100px"	},
					{field: "CantidadDCVRelacionado",		title: $translate.instant("UI_CANTIDAD_DCV_RELACIONADO"),	width: "100px"	},
					{field: "CantidadDCVTerceros",			title: $translate.instant("UI_CANTIDAD_DCV_TERCEROS"),		width: "100px"	},
					{field: "CantidadDCVCuentaPropia",		title: $translate.instant("UI_CANTIDAD_DCV_CTA_PROPIA"),	width: "100px"  },
					{field: "CantidadBCS",					title: $translate.instant("UI_CANTIDAD_BCS"),				width: "100px"	},
					{field: "CantidadBEC",					title: $translate.instant("UI_CANTIDAD_BEC"),				width: "100px"	},
					{field: "CantidadBOVALPO",				title: $translate.instant("UI_CANTIDAD_BOVALPO"),			width: "100px"	},
					{field: "CantidadEmisores",				title: $translate.instant("UI_CANTIDAD_EMISORES"),			width: "100px"	},
					{field: "CantidadVentasEnTransito",		title: $translate.instant("UI_CANTIDADVENTASENTRANSITO"),	width: "100px"	},
					{field: "CantidadComprasEnTransito",	title: $translate.instant("UI_CANTIDADCOMPRASENTRANSITO"),	width: "100px"	},
					{field: "CantidadTransitoIngreso",		title: $translate.instant("UI_CANTIDAD_TRANSITO_INGRESO"),	width: "100px"	},
					{field: "CantidadTransitoEgreso",		title: $translate.instant("UI_CANTIDAD_TRANSITO_EGRESO"),	width: "100px"	},
					{field: "TotalCustodiosyEmisores",		title: $translate.instant("UI_TOTALCUSTODIOSEMISORES"),		width: "100px"	},
					{field: "Diferencia",					title: $translate.instant("UI_DIFERENCIA"),					width: "100px"	},
					{field: "ExplicacionDescuadratura",		title: $translate.instant("UI_EXPLICACION"),				width: "100px"	},
					{field: "SorteoLetras",					title: $translate.instant("UI_SORTEO_LETRAS"),				width: "100px"	},
					
					
					
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
			height		: 490,//580,
		    //selectable 	: "row",
		    selectable 			: "multiple row",
		    userSettings		: true,
			navigatable			: true,
			excel : {
				allPages: true,
			},
		    filterable	: true,
		    rowCellColor	: function(row) {		
				switch(row.get("Tipo")) {
		             case 'MOD':
		            	 return {column: "Nemotecnico", color: "green"};
		                break;
		            default:
		            	return {column: "Nemotecnico", color: "white"};
		                break;     
				  }	
				}
		};

		pct.grilla.Cir1518.gridCreated = function(grid){ // para evitar que la tabla se renderice vacia sin altura
			$('bcs-grid#gridCir1518 .k-grid-content.k-auto-scrollable').css("height", "422px")
		}
		
	
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		pct.cmbbx.Clasificacion={
			tipo : "combobox-clasificacion-tipo-instrumento",
			configuracion : {},
			filtros : {
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};
		
		pct.cmbbx.TipoInstrumento={
			tipo : "combobox-tipo-instrumento",
			configuracion : {},
			filtros : {
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "VIG" },
				//IdBolsa : { "field"   : "IdBolsa","operator": "eq", "value"   : $scope.SesionBolsaDefectoSistema },
				IdClasificacionTipoInstrumento : { "field"   : "IdClasificacionTipoInstrumento","operator": "eq", "value"   : "" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};
		
		
		

		/*********************************************
		 * FUNCIONES
		 *********************************************/
		var successCrearSolicitud = function(respuesta)
		{	
			//bcsComunes.ventanaOverlay(pc1.idVentana,true);
			if (respuesta.P_OUT_ERRORESCONTADOR == 0)
			{
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				pct.salidaMensaje=$translate.instant("UI_" + pct.salidaMensaje + "_INGRESO_EXITO");
				
				bcsComunes.msg("info","info", pct.salidaMensaje);
				IdSolicitudCircular =respuesta.P_OUT_RESULTADO;
				//$scope.data.limpiarFormulario();
			}
			else
			{	
				
				pct.salidaMensaje=$translate.instant("UI_ERROR_INGRESO");
			/*	bcsComunes.ventanaMsg({ mensaje : pc1.salidaMensaje, 
										conflictos : util.parseServerResponse(respuesta)}, pc1.idVentana);
			*/
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
				bcsComunes.msg("error","error",pct.salidaMensaje);
							
				var i = 0;
				for(i; i<respuesta.P_OUT_ERRORESCONTADOR; i++)
				{
					bcsComunes.log().info("Error: " + respuesta.P_OUT_ERRORESDETALLE[i].Descripcion);       
				}            
			}		
			bcsComunes.ventanaMsg({ mensaje : pct.salidaMensaje,
				conflictos : util.parseServerResponse(respuesta)}, pct.idVentana),true,true;
			
			bcsComunes.ventanaData(pct.idVentana, respuesta);
			//bcsComunes.ventanaCerrar(pct.idVentana);
			
		};
		
		var errorCrearSolicitud = function(respuesta)
		{	

			pct.salidaMensaje=$translate.instant("UI_ERROR");
			bcsComunes.ventanaMsg({ mensaje : pct.salidaMensaje, 
								 conflictos : util.parseServerResponse(respuesta)}, pct.idVentana);

			bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
			bcsComunes.msg("error","error",pct.salidaMensaje);
			bcsComunes.ventanaOverlay(pct.idVentana, false);
		};		
		
		
		pct.fn.envioFecha = function(){
			var date = '';
			date= util.dateSPdate(pct.Periodo);
			pct.PeriodoEnv= date.substring(0,4)  + date.substring(5,7);
						
			var dateFin = new Date();
			dateFin 	 = pct.Periodo;
			var ultimoDia = new Date(dateFin.getFullYear(), dateFin.getMonth() + 1, 0);
			pct.PeriodoEnvFinal= util.dateInicioDia(ultimoDia);
			
		};
		
		
		
		
		
		
		
		pct.fn.buscar = function(){
				//Datos grillas	
			var fh = util.dateSPdate(pct.FecTermino);
			var filters = {};
			filters.p_IdSolicitudCircular 			= 0;
			filters.p_IdTipoDocumento				='CIR1518';
		
			var tablaParametros = [];
								
			tablaParametros.push({TipoParametro : 'IdEmpresa',Valor : $scope.SesionIdEmpresa});
			tablaParametros.push({TipoParametro : 'fechaHasta',Valor : fh});
			
			if (pct.IdClasificacion !== undefined && pct.IdClasificacion!== 0) {
				tablaParametros.push({TipoParametro : 'CLAINS',Valor :  pct.IdClasificacion});
			};
			
			
			if (pct.IdTipoInstrumento !== undefined && pct.IdTipoInstrumento != "")	{
        		tablaParametros.push({TipoParametro : 'TIPINS',Valor :  pct.IdTipoInstrumento});
			};
			
			if (pct.instrumento	!== undefined){
	      		if (pct.instrumento.IdInstrumento > 0) {
        			tablaParametros.push({TipoParametro : 'NEMO',Valor :  pct.instrumento.Nemotecnico});
       			};
			};
			
			filters.p_TablaParametros=tablaParametros;
			
			pct.grilla.Cir1518.dataSource.setCallParameters(filters);			
			
						
		
	    	pct.grilla.Cir1518Sel = undefined;
	    	
			
			
			
			
				    	
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
		
		pct.Modificar = function(seleccionado) {
			if (seleccionado)
			{
						ventanaEdicionMultiple = bcsComunes.ventanaAbrir("ModalCir1518", {PrevSeleccion :seleccionado,PrevVentana:pct.idVentana}, true, false, pct.idVentana);
						var promesa=bcsComunes.ventanaPromesa(ventanaEdicionMultiple);
						 promesa.then(function(data){
				        	pct.fn.buscar();
						 });
			}			 
		};
				
		pct.fn.limpiar = function(){
			
			
			pct.grilla.Cir1518.dataSource.removeAll();
		};
		

	});
}());