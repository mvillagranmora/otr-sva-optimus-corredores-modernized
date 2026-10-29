(function() {
	angular.module('ModuloFFM').controller('PanelEgresosCtrl',
	function($rootScope,$scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsServicioInterfaces,bcsServiciosCuentaInversion,
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
		$scope.listaTraspaso = "";
		pct.filtro.IdClasificacionTipoInstrumentoDef	= 0;
			
		pct.salidaMensaje='';
		pct.FecInicio 	 = util.dateInicioDia($scope.SesionFechaSistema);
		pct.FecTermino 	 = util.dateInicioDia($scope.SesionFechaSistema);
		pct.activaConfirmar = false;
		pct.activaDescargarCertificado= false;
		
		
		$scope.dataTemplate	= {};
		var filters = {};
		var auxDate1 =util.dateOdata(util.date(pct.FecInicio).hours(0).minutes(0).seconds(0).toDate());
		var auxDate2 =util.dateOdata(util.date(pct.FecInicio).add(23,'hours').minutes(59).seconds(59).toDate());
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		var defTraspasos = {
				type: "odata",
				definition : {
					serverFiltering : true, // Filtrado de datos en el servidor.
	                serverPaging    : true, // Paginado de los datos en el servidor.
	                serverSorting   : true, // Ordenamiento de los datos en el servidor.
	                pageSize        : 25,
	                sort			: {field: "IdTraspaso", dir: "desc" },
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=300014",
							dataType: "json"
						}
					},
					filter: [
					    { field: "IdEmpresa ", operator: "eq", value: $scope.SesionIdEmpresa },
					    { field: "IdTipoMovimientoTraspaso ", operator: "eq", value: 'TRAEGR' },
					    { field: "FechaIngreso ", operator: "gte", value: auxDate1 },
					    { field: "FechaIngreso ", operator: "lte", value: auxDate2 }
					    
					    
					],
	                //sort 			: false,
					schema: {
						model: {
							fields: {
								IdTraspaso					: 	{ type: "number"},
								FolioTraspaso				: 	{ type: "number"},
								IdMotivoTraspaso         	:	{ type: "string"},
								IdTipoTraspaso				:	{ type: "string"},
								Glosa						:	{ type: "string"},
								FechaHoraSuscripcion		:	{ type: "string"},
								IdEstado					:	{ type: "string"},
								IntMontoTraspaso			: 	{ type: "number"},
								Monto						: 	{ type: "number"},
								FechaIngreso				:	{ type: "date"},
								CodigoPortafolio			:	{ type: "string"},
								RazonSocial					:	{ type: "string"},
		        				NombreCompleto				:	{ type: "string"},
		        				DocumentoIdentidad			:	{ type: "string"},
		        				IdPersona					: 	{ type: "number"},
		        				EstadoTraspaso				:	{ type: "string"},
		        				NombreCortoEmisor			:	{ type: "string"},
		        				DocumentoIdentidadEmisor	:	{ type: "string"},
		        				MotivoTraspaso				:	{ type: "string"},
		        				TipoMovimientoTraspaso		:	{ type: "string"},
		        				TipoTraspaso				:	{ type: "string"},
		        				CodigoUsuario				:	{ type: "string"},
		        				DiasTranscurridos			: 	{ type: "number"},
		        				NombreEntidadRecaudadora	:	{ type: "string"},
		        				DocumentoIdentidadEntidadRecaudadora	:	{ type: "string"},
		        				IdMonedaFondo				:	{ type: "string"}
							}
						}
					}
				}	
			};
		
		var dsTraspasos = new bcs.DataSource(defTraspasos);
		pct.Traspasos = {
				dataSource: dsTraspasos,
				columns: [
						{field: "FolioTraspaso",			title: $translate.instant('UI_FOLIO_TRASPASO'),					width: "80px", align: "right", editable: false},
						{field: "FechaIngreso",				title: $translate.instant('UI_FECHA_INGRESO'),					width: "80px", align: "right", editable: false},
						
						{field: "DocumentoIdentidad",		title: $translate.instant('UI_DOCUMENTO_IDENTIDAD'),			width: "80px", align: "left"},
						{field: "NombreCompleto",			title: $translate.instant('UI_CLIENTE'),						width: "140px", align: "left"},
						{field: "CodigoPortafolio",			title: $translate.instant('UI_PORTAFOLIO'),						width: "100px", align: "left"},
						{field: "EstadoTraspaso",			title: $translate.instant('UI_ESTADO'),							width: "100px", align: "left"},
						{field: "IdTraspaso",				title: $translate.instant('UI_SECUENCIA'),						width: "60px", align: "right", editable: false},
						{field: "NombreEntidadRecaudadora",	title: $translate.instant('UI_ADMINISTRADORA_DESTINO'),			width: "100px", align: "left"},
						{field: "DocumentoIdentidadEntidadRecaudadora",	title: $translate.instant('UI_DOCUMENTO_IDENTIDAD_DESTINO'),		width: "100px", align: "left"},
						{field: "TipoMovimientoTraspaso",	title: $translate.instant('UI_TIPO_MOVIMIENTO'),				width: "100px", align: "left"},
						{field: "TipoTraspaso",				title: $translate.instant('UI_TIPO_TRASPASO'),					width: "100px", align: "left"},
						{field: "MotivoTraspaso",			title: $translate.instant('UI_MOTIVO_TRASPASO'),				width: "100px", align: "left"},
						{field: "DiasTranscurridos",		title: $translate.instant('UI_DIAS'),							width: "40px", align: "left"},
						{field: "Glosa",					title: $translate.instant('UI_GLOSA'),							width: "100px", align: "left"},
						{field: "CodigoUsuario",			title: $translate.instant('UI_USUARIO'),						width: "100px", align: "left"},
						{field: "IdMonedaFondo",			title: $translate.instant('UI_MONEDA'),							width: "100px", align: "left"}
					    ],	
				sortable		: true,
				filterable		: true,
				selectable: "multiple, row",
				pageable: {
                    refresh: true,
                    buttonCount: 5
                },                
				autoBind		: true,
				height			: 460,				
			    userSettings	: true,
				navigatable		: true,
								
				gridCreated     : function(g){
					pct.ApiTraspasos = g;
				},
				rowColorBackground	: function(row) {		
					switch(row.get("IdEstado")) {
			             case 'ANU':
			                return "red";
			                break;
			              case 'VIG':
			                return "green";
			                break;
			            default:
			            	return "white";
			                break;     
					  }	
					},
				excel : {
					allPages: true,
				},
				allowCopy: true
		
		};
				
	
		pct.Origen = function(dataItem){
			return{
				dataSource: new bcs.DataSource({
					type: "odata",
					definition: {
						serverFiltering : true, // Filtrado de datos en el servidor.
						serverPaging 	: true, // Paginado de los datos en el servidor.
						serverSorting 	: true, // Ordenamiento de los datos en el servidor.
						sortable 		: true,
						pageSize 		: 50,
						transport: {
							read: {
								url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=300015",
								dataType: "json"
							}
						},
						filter: [
						    { field: "IdTraspaso"		  , operator: "eq", value: (dataItem && dataItem.IdTraspaso) || -1  }
						   ,{ field: "IdEstado " 		  , operator: "eq", value: 'ACT' }
						   ,{ field: "IdRegimenTributario"  , operator: "eq", value: '18Q'}
						],
						schema: {
							model: {
								fields: {
									
									IdDetalleTraspaso			: 	{ type: "number"},
									IdTraspaso					: 	{ type: "number"},
									IdSerieFondo				:	{ type: "string"},
									Cantidad					: 	{ type: "number"},
									IdEstado					:	{ type: "string"},
									FechaIngreso				:	{ type: "date"},
								    FechaModificacion			:	{ type: "date"},
								    FechaIngresoRegistro		:	{ type: "date"},
								    FechaModificacionRegistro	:	{ type: "date"},
									Estado						:	{ type: "string"},
									NombreSerieFondo			:	{ type: "string"},
									CodigoSerie					:	{ type: "string"},
									
									IdSolicitudAporteRescate	: 	{ type: "number"},
									IdTipoMovimientoFondoMutuo	:	{ type: "string"},
									FechaCertificacion			:	{ type: "date"},
									FechaLiquidacion			:	{ type: "date"},
									IdMoneda					:	{ type: "string"},
									Monto						: 	{ type: "number"},
									NumeroCuotas				: 	{ type: "number"},
									ValorCuota					: 	{ type: "number"},
									DecimalesCuota				:   { type : "number"},
									DecimalesValorCuota   	  	:   { type : "number"},
																	
									MontoMonedaSistema			: 	{ type: "number"},
									EstadoSolicitud				:	{ type: "string"},
									EstadoLiquidacion			:	{ type: "string"},
									NombreEstadoSolicitud		:	{ type: "string"},
									NombreEstadoLiquidacion		:	{ type: "string"},
									DescripcionTipoMovimiento	:	{ type: "string"},
									
									IdEstadoIngresoEgreso		:	{ type: "string"},
									IdIngresoEgreso				: 	{ type: "number"},
									FechaIngresoEgreso			:	{ type: "date"},
									TipoMovimientoIngresoEgreso	:	{ type: "string"},
									NombreEstadoIngresoEgreso	:	{ type: "string"}
								}
							}
						}
					}
				}),
				columns: [
				    {field: "IdSerieFondo",					title: $translate.instant("UI_INSTRUMENTO")	,							width: "100px"},
				    {field: "Cantidad",						title: $translate.instant("UI_CANTIDAD")	,							width: "60px"		, template : " <label> #: util.bcs_formato(Cantidad,'CUS','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','').valor #</label>"},
				    {field: "FechaIngreso",					title: $translate.instant('UI_FECHA_INGRESO'),							width: "80px", align: "right"},
				    
				    {field: "DescripcionTipoMovimiento",	title: $translate.instant('UI_OPERACION'),								width: "90px", align: "left"},
				    {field: "IdSolicitudAporteRescate",		title: $translate.instant('UI_SECUENCIA'),								width: "60px",	align: "left"},
				    {field: "NombreEstadoSolicitud",		title: $translate.instant('UI_ESTADO_SOLICITUD'),						width: "90px", align: "left"},
				    {field: "FechaCertificacion",	    	title: $translate.instant('UI_FECHA_CERTIFICACION'),					width: "80px", align: "left"},
				  
				    {field: "ValorCuota",					title: $translate.instant('UI_VALOR_CUOTA'),							width: "75px", 	align: "left" , template : " <label> #:   util.formateaNumero(ValorCuota, DecimalesValorCuota) #</label>"},
				    {field: "NumeroCuotas",		    		title: $translate.instant('UI_CANTIDAD'),		    					width: "75px", 	align: "left" , template : " <label> #:   util.formateaNumero(NumeroCuotas, DecimalesCuota) #</label>"},
				    {field: "IdMoneda",				    	title: $translate.instant('UI_MONEDA'),			    					width: "60px",	align: "left"},
				    {field: "Monto",						title: $translate.instant('UI_MONTO'),								width: "80px", align: "left", template : " <label> #:  (util.bcs_formato(Monto,'FMM','Monto',IdMoneda != '' ? IdMoneda : '" + $rootScope.SesionIdMonedaSistema + "', '','')).valor #</label>"},
				    {field: "TipoMovimientoIngresoEgreso",	title: $translate.instant('UI_MOVIMIENTO_TESORERIA'),				width: "100px", align: "left"},
				    {field: "IdIngresoEgreso",				title: $translate.instant('UI_SECUENCIA_TESORERIA'),				width: "60px",	align: "left"},
				    {field: "NombreEstadoIngresoEgreso",	title: $translate.instant('UI_ESTADO_TESORERIA'),					width: "100px", align: "left"},
				    {field: "FechaIngresoEgreso",			title: $translate.instant('UI_FECHA_TESORERIA'),					width: "80px", align: "right"}
				    
				    
				],
				sortable	: false,
				pageable	: false,
				autoBind	: true,
				//dataBound: function(e) 	{dataBound(e);},
			    selectable 	: "row",
			    rowCellColor : function(row) {
			    	var column = new Array();
			    	if (row.get("EstadoSolicitud")=='CRT'){column.push({column: "NombreEstadoSolicitud", color: "green"});};
			    	if (row.get("IdEstadoIngresoEgreso")=='EMI'){column.push({column: "NombreEstadoIngresoEgreso", color: "green"});};
			    	if (row.get("EstadoSolicitud")=='ANU'){column.push({column: "NombreEstadoSolicitud", color: "red"});};
			    	if (row.get("IdEstadoIngresoEgreso")=='ANU'){column.push({column: "NombreEstadoIngresoEgreso", color: "red"});};
			    	return column;

			    }
			
			}
		};
		
		var colorColumn = function(row){
			switch(row.get("EstadoSolicitud")) {
			case 'CRT':
				return {column: "NombreEstadoSolicitud", color: "green"};
                break;
			case 'ANU':
				return {column: "NombreEstadoSolicitud", color: "red"};
	            break;
		    default:
            	return {column: "NombreEstadoSolicitud", color: "white"};
                break;  	
			}
			
		};
		
		
		pct.fn.descargarCertificado  = function (){
			console.log("DESCARGA");
			bcsComunes.ventanaOverlay(pct.idVentana, true);
			if(pct.TraspasosSel!= null){
				
				let listaTraspaso =""
				
				pct.TraspasosSel.map(function(traspaso,index){					
						if( listaTraspaso ===""){
							listaTraspaso = traspaso.IdTraspaso+",";
						}else{
							listaTraspaso = listaTraspaso+traspaso.IdTraspaso+",";
						}				
				});
			
			 listaTraspaso = listaTraspaso.substring(0,listaTraspaso.length-1);
			 $scope.listaTraspaso = listaTraspaso;
		
			 bcsComunes.ventanaAbrir("GenerarCertificado31",{datoLista: listaTraspaso},false,false,pct.idVentana);
				
			}else {
				bcsComunes.ventanaOverlay(pct.idVentana, false);
				bcsComunes.msg("error", "Error", $translate.instant('UI_GENERAR_REPORTE_MASIVO_ERROR'));
			}
		}
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		pct.cmbbx.SocioDeNegocio={
			filtros: {
				IdEmpresa : {field: "IdEmpresa",operator: "eq", value: $scope.$root.SesionIdEmpresa },
				EsCliente : {field: "EsCliente",operator: "eq", value: 'UI_SI'}
			}
		};
		
		pct.cmbbx.perfilProductos={
			filtros : {
				EsPerfilPersonalizado 		: { field: "EsPerfilPersonalizado",  operator: "eq", value: "N" },
				IdCategoriaInversionista 	: { field : "IdCategoriaInversionista", operator: "eq", value : "" }
			}
		};
		
		pct.cmbbx.TipoTraspaso={
				filtros : {
					IdEstado : { field: "IdEstado", operator: "eq", value: 'ACT' }
				},
			};
		pct.cmbbx.Motivo={
				filtros : {
					IdEstado : { field: "IdEstado", operator: "eq", value: 'ACT' }
				},
			};
		pct.cmbbx.Emisor={
				filtros : {
					IdEstado : { field: "IdEstado", operator: "eq", value: 'ACT' },
					IndicadorAGF : { field: "IndicadorAGF", operator: "eq", value: 'S' }
					/*,IdRegimenFondoMutuo : { field: "IdRegimenFondoMutuo"	, operator: "eq", value: '18Q'}*/
				},
			};	
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		pct.fn.buscar = function(){
				//Datos grillas	
				
			
			//var objfilters = { };
			var niv1filters = new Array();
	
			//objfilters.filters = niv1filters;
	    	
			niv1filters.push({ field: "IdEmpresa ", operator: "eq", value: $scope.SesionIdEmpresa });
			niv1filters.push({ field: "IdTipoMovimientoTraspaso ", operator: "eq", value: 'TRAEGR'});
			
			if (pct.FecInicio) {
				niv1filters.push({ field: "FechaIngreso", operator: "gte", value:util.dateInicioDia(pct.FecInicio) });
			};
			if (pct.FecTermino) {
				niv1filters.push({ field: "FechaIngreso", operator: "lte", value:util.dateTerminoDia(pct.FecTermino) });
			};
				
			if(pct.filtro.IdSocioDeNegocio && pct.filtro.IdSocioDeNegocio > 0){
	    		niv1filters.push({ field: "IdCliente", operator: "eq", value: pct.SocioDeNegocioObj.IdCliente });
			};
			if(pct.cmbbx.IdTipoTraspaso ){
	    		niv1filters.push({ field: "IdTipoTraspaso", operator: "eq", value:pct.cmbbx.IdTipoTraspaso });
	    	};
			if(pct.cmbbx.IdEmisor){
	    		niv1filters.push({ field: "IdEmisor", operator: "eq", value:pct.cmbbx.IdEmisor });
	    	};
			if(pct.cmbbx.IdMotivo){
	    		niv1filters.push({ field: "IdMotivoTraspaso", operator: "eq", value:pct.cmbbx.IdMotivo });
	    	};	    	
	    	
	    	
	    	if (niv1filters.length>0) pct.Traspasos.dataSource.filter(niv1filters);
	    	else pct.Traspasos.dataSource.filter(new Array());
	    	
	    	pct.Traspasos.dataSource.read();
	    	pct.TraspasosSel=undefined;
			
		}
		pct.fn.limpiar = function(){
			pct.FecInicio 	 = util.dateInicioDia($scope.SesionFechaSistema);
			pct.FecTermino 	 = util.dateInicioDia($scope.SesionFechaSistema);
			pct.filtro.IdSocioDeNegocio=0;
			pct.cmbbx.IdTipoTraspaso=0;
			pct.cmbbx.IdEmisor=0;
			pct.cmbbx.IdMotivo=0;
		};
		
		pct.fn.ingresar= function(){
			bcsComunes.ventanaAbrir("IngresoTraspasosFFM", {IdTipoMovimiento: 'TRAEGR'} );
			
		};
		pct.fn.editar= function(){
			if (pct.TraspasosSel)
				{
					bcsComunes.ventanaAbrir("IngresoTraspasosFFM", {IdTipoMovimiento: 'TRAEGR',IdTraspaso: pct.TraspasosSel.IdTraspaso} );
				};
			
		};
		
		pct.fn.anular = function() {
			 var IdVentanamodal = bcsComunes.ventanaAbrir("AnularTraspasoFMM", {IdTipoMovimiento: 'TRAEGR',IdTraspaso: pct.TraspasosSel.IdTraspaso}, true, false, pct.idVentana);
				var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
				promesa.then(function(data){
					pct.fn.buscar();
				});
		};
		
		pct.fn.confirmar = function() {
			var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_CONFIRMAR" ) + ", ¿"  + $translate.instant("UI_DESEA_CONTINUAR" ) +"?",pct.idVentana);
			t .then(function(result)
			      {		
					 if (result===OPCIONES_BCSALERTA.SI)
				       {	
						 
						 
						 var datos = {
								 p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
								 p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
								 p_IdModulo : 'FMM',
									 
								 p_Traspaso : [{
						    		 	IdTraspaso		      		:	pct.TraspasosSel[0].IdTraspaso
								 }]
								 
						 }
						
						bcsServicioFondosMutuos.ConfirmarTraspaso(datos).$promise.then(function(respuesta){
					     	 console.log(respuesta);
					     	 if (respuesta.P_OUT_ERRORESCONTADOR === 0){
					     		var id = bcsComunes.ventanaMsg({mensaje:"Operacion Confirmada"},pct.idVentana);
				     			bcsComunes.ventanaOverlay($scope.winID,false);
					     	 }else{
					     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_CONFIRMAR_TRASPASO"),conflictos:util.parseServerResponse(respuesta)},pct.idVentana);
					     		bcsComunes.ventanaOverlay(pct.idVentana,false);
					     	 }
				   		 },function(respuesta){
				   			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_CONFIRMAR_TRASPASO"),conflictos:util.parseServerResponse(respuesta)},pct.idVentana);
				   			bcsComunes.ventanaOverlay(pct.idVentana,false);
				   		 });
						
						
						
						
	
						 
				       };
				});
		 };
		
		 pct.fn.certificarCargo = function(){		
	
				 var datos = {
						 p_IdEmpresa 		: $scope.SesionIdEmpresa, //saca de variables de entorno
						 p_IdUsuario 		: $scope.SesionIdUsuario, //saca de variables de entorno
						 p_IdModulo			: 'FMM',
						 p_UsaReglasNegocio :  'S',
						 p_FechaMovimiento 	: $scope.SesionFechaSistema,
						 p_IdPais		 	: $scope.SesionIdPaisSistema,
						 p_CargosAbonosACertificar : [{
							 IdCargoAbonoMovimiento		      		:	pct.OrigenSel.IdCargoAbonoMovimiento
						 }]
						 
				 }
				
			
				bcsComunes.ventanaOverlay($scope.winID, true);
		       	bcsServiciosCuentaInversion.CertificarCargoAbono(
		       		datos,
		       		function(respuesta){
		       			bcsComunes.ventanaOverlay($scope.winID, false);
						bcsComunes.ventanaMsg({
							conflictos:util.parseServerResponse(respuesta)
						},$scope.winID);
						$scope.buscarMovimientos();
						
					},
					function(respuesta){
						bcsComunes.ventanaOverlay($scope.winID, false);
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_ERROR_CERTIFICAR_MOVIMIENTO"),
							conflictos:util.parseServerResponse(respuesta)
						},$scope.winID);				
					}
		       	); 
			};
			
			pct.fn.validaTraspasos = function (){				
				pct.TraspasosSel.map(function(item){
					if(item.IdEstado!= 'VIG'){
						pct.activaDescargarCertificado = false;						
					}else{
						pct.activaDescargarCertificado = true;
					}
				});
			};
		
			/*********************************************
			 * WATCH
			 ********************************************/
			$scope.$watch('pct.TraspasosSel',function(newval,oldval){
				pct.OrigenSel=undefined;
				
				if(pct.TraspasosSel!= null && newval!= undefined  ){
					if(pct.TraspasosSel.length===1){
						pct.fn.validaTraspasos();
						pct.activaConfirmar = newval.IdEstado='ING'?true:false;	
					}else{
						pct.activaConfirmar = false;
					}					
				}else {
					pct.activaConfirmar = false;
				}
				
			});	
		
		

	});
}());