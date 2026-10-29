(function() {
	angular.module('ModuloNormativa').controller('PanelPerfilCtrl',
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
	
		pct.grilla.Perfil = {
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
							   IdPerfilProductos			: { type: "string"},
							   IdTipoProducto				: { type: "string"},
							   TipoProducto					: { type: "string"},
							   IdTipoOperacion				: { type: "string"},
							   TipoOperacion				: { type: "string"},
							   IdOperacion					: { type: "number"},
							   MontoUtilizado				: { type: "number"},
							   IdMoneda						: { type: "string"},
							   Moneda						: { type: "string"},
							   IdTipoInstrumento			: { type: "string"},
							   IdClasificacionTipoInstrumento		: { type: "string"},
							   FechaOperacion				: { type: "string"},
							   TipoEvaluacion				: { type: "string"},
							   FechaIngreso					: { type: "date"},
							   FechaCreacion				: { type: "date"},
							   CodigoUsuarioCreador			: { type: "string"}
							}
						}
					}
				}
			}),
			columns: [
				    {field: "FechaIngreso",					title: $translate.instant("UI_FECHA"),					    width: "80px"	},
				    {field: "IdOperacion",					title: $translate.instant("UI_OPERACION"),					width: "80px"	},
				    {field: "DocumentoIdentidad",			title: $translate.instant('UI_DOCUMENTO_IDENTIDAD'),		width: "80px", align: "left"},
					{field: "NombreCliente",				title: $translate.instant("UI_NOMBRE"),						width: "200px"	},
					{field: "CodigoPortafolio",				title: $translate.instant('UI_PORTAFOLIO'),					width: "100px", align: "left"},
					{field: "TipoProducto",					title: $translate.instant("UI_PRODUCTO"),					width: "130px"	},
					{field: "TipoOperacion",				title: $translate.instant("UI_TIPO_OPERACION"),				width: "130px"	},
					{field: "MontoUtilizado",				title: $translate.instant("UI_MONTO"),	template: "<label> #: (util.bcs_formato(MontoUtilizado,'ORD','MONTO',IdMoneda,'','')).valor #</label>",						width: "80px"	},
					{field: "Moneda",						title: $translate.instant("UI_MONEDA"),						width: "80px"	},

					
					
			],
			hideDetailButton: function(row) {
				var hide = false;
				return hide;
			},
			pageable	: true,
			pageSize	: 50,
			//autoBind	: true,
			autoBind	: false,
			height		: 540,//580,
		    //selectable 	: "row",
		   rowCellColor : function(row) {
		    	var column = new Array();
		    	column.push({column: "TipoProducto", color: "red"});
		    	column.push({column: "TipoOperacion", color: "red"});
		    	
		    	return column;
		    },
			pageable: {
                refresh: true,
                buttonCount: 5
            },
		    selectable 			: "multiple row",
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
		
		pct.cmbbx.perfilProductos={
			filtros : {
				EsPerfilPersonalizado 		: { field: "EsPerfilPersonalizado",  operator: "eq", value: "N" },
				IdCategoriaInversionista 	: { field : "IdCategoriaInversionista", operator: "eq", value : "" }
			}
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
		
		
		
		pct.fn.Notificar= function(seleccionado) {

			if(seleccionado!==undefined)
			{
				var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_NOTIFICAR") + ", " + $translate.instant("UI_REGISTROS") + "  " +  $translate.instant("UI_SELECCIONADOS"),pct.idVentana);
				t .then(function(result)
			    //t.result.then(function(result)
			      {		
					 if (result===OPCIONES_BCSALERTA.SI)
				       {	
						var a= seleccionado;
								
						var registro = [];		
						parametrosSP= {}; 
				    	//Comunes
				    	parametrosSP.p_IdEmpresa 				=  $scope.SesionIdEmpresa;
				    	parametrosSP.p_IdUsuario              	=  $scope.SesionIdUsuario;
				    	parametrosSP.p_IdModulo 				=  pct.modulo;
				    	
						//Especificos
				    	
				    	 $.each(seleccionado,function(key,value)
			      	                {       		     
			      	    	           registro.push(
				      		             {   
				      			           IdEvaluacion : seleccionado[key].IdEvaluacion,
				      			           
				      		             });
			      	                });
			      	    
						parametrosSP.p_TablaNotificacion=registro;
								
						bcsComunes.msg('info', 'Notificacion', 'En proceso...');
						bcsComunes.ventanaOverlay(pct.idVentana,true);
						pct.salidaMensaje='NOTIFICACION';
						bcsServiciosNormativa.CumplimientoNotificarEvaluacionSOI(parametrosSP,successCrearSolicitud,errorCrearSolicitud);
						bcsComunes.ventanaOverlay(pct.idVentana,false);
						pct.fn.buscar();
				       };
				});
			}else{
				
				
			};
					
		};
		
		
		
		pct.fn.buscar = function(){
				//Datos grillas	
				
			
			var objfilters = { };
			var niv1filters = new Array();
	
			objfilters.filters = niv1filters;
	    	
			niv1filters.push({ field: "TipoEvaluacion", operator: "eq", value: 'PERFIL_PRODUCTO' });

			if (pct.FecInicio) {
				niv1filters.push({ field: "FechaIngreso", operator: "gte", value:util.dateInicioDia(pct.FecInicio) });

			};
			
			
			if (pct.FecTermino) {
				niv1filters.push({ field: "FechaIngreso", operator: "lte", value:util.dateTerminoDia(pct.FecTermino) });

			};
			
	    	if(pct.IdTipoProducto && pct.IdTipoProducto != "0"){
	    		niv1filters.push({ field: "IdTipoProducto", operator: "eq", value:pct.IdTipoProducto });
	    	};
	    	
			
			if(pct.filtro.IdSocioDeNegocio && pct.filtro.IdSocioDeNegocio > 0){
	    		niv1filters.push({ field: "IdCliente", operator: "eq", value: pct.SocioDeNegocioObj.IdCliente });
			};
	    	
	    	if (niv1filters.length>0) pct.grilla.Perfil.dataSource.filter(objfilters);
	    	else pct.grilla.Perfil.dataSource.filter(new Array());
	    	
	    	pct.grilla.PerfilSel = undefined;
	    	
			
			
			
			
				    	
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
		
		
		
		
		
	
/*
		
		$scope.$watch('pct.grilla.PerfilSel', function(newval,oldval){
			
			if (angular.equals(newval,oldval)) return;
			if (newval!==false) {
				if(newval !== 'undefined')
				{
					pct.fnVersionEsquemaComprobante(newval);
				}
			}
			
			
		},true);
	*/	
				
		pct.fn.limpiar = function(){
			
			
			pct.grilla.Perfil.dataSource.removeAll();
		};
		

	});
}());