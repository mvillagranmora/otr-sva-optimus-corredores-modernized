(function() {
	angular.module('ModuloNormativa').controller('PanelCir1720Ctrl',
	function($scope,$translate,bcsComunes,bcsServiciosNormativa,$compile,
				 bcsAlerta, $rootScope, OPCIONES_BCSALERTA,$http,$timeout){

		
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var pct 		= this;
		pct.idVentana	= bcsComunes.ventanaWinId($scope);
		pct.params = bcsComunes.ventanaParams(pct.idVentana);
		
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
		
		$scope.downloadFile = function (filename, text) {
            var element = document.createElement('a');
            element.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(text));
            element.setAttribute('download', filename);
            element.style.display = 'none';
            document.body.appendChild(element);
            element.click();
            document.body.removeChild(element);
        };
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/	
		pct.grilla.Cir1720 = {
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
					callTransaction 	: "3576",
					callResultSet 		: "P_OUT_TABLASALIDA",
					schema: {
						model: {
							fields: {

							    Nemotecnico						: { type: "string"},
							    NemotecnicoCMF                  : { type: "string"},
								NombreEmisor					: { type: "string"},
								TipoInstrumento					: { type: "string"},
								CantidadUnidadesNominal			: { type: "number"},
								TipoUnidades					: { type: "string"},
								Valorizacion					: { type: "number"},
								CarteraPropia					: { type: "number"},
								CustodiaTerceros				: { type: "number"},
								CustodiaPropiaDCV				: { type: "number"},
								CustodiaTercerosDCV				: { type: "number"},
								CustodiaSociedadEmisora			: { type: "number"},
								CustodiaIntermediario			: { type: "number"},
								CustodiaBolsas					: { type: "number"},
								CustodiaOtros					: { type: "number"},
								Tipo							: { type: "string"},
							    IdCuadratura					: { type: "number"},
							    Habilitado						: { type: "string"}
							}
						}
					}
				}
			}),
			columns: [
				    {field: "Nemotecnico",					title: $translate.instant("UI_NEMO"),					    width: "150px"	},
				    {field: "NemotecnicoCMF",				title: $translate.instant("UI_NEMO_CMF"),					width: "150px"	},
				    {field: "NombreEmisor",					title: $translate.instant("UI_EMISOR"),					    width: "150px"	},
				    {field: "TipoInstrumento",				title: $translate.instant("UI_TIPO_INSTRUMENTO"),		    width: "150px"	},
				    {field: "CantidadUnidadesNominal",		title: $translate.instant("UI_CANTIDAD_NOMINALES"),			width: "100px"	, template : " <label> #:  (util.bcs_formato(CantidadUnidadesNominal,'CUS','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" },
					{field: "TipoUnidades",					title: $translate.instant("UI_TIPO_UNIDADES"),				width: "100px"	},
					{field: "Valorizacion",					title: $translate.instant("UI_VALORIZACION"),				width: "100px"	, template : " <label> #:  (util.bcs_formato(Valorizacion,'CUS','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" },
					{field: "CarteraPropia",				title: $translate.instant("UI_CARTERA_PROPIA"),				width: "100px"	, template : " <label> #:  (util.bcs_formato(CarteraPropia,'CUS','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" },
					{field: "CustodiaTerceros",				title: $translate.instant("UI_CUSTODIA_TERCEROS"),			width: "100px"	, template : " <label> #:  (util.bcs_formato(CustodiaTerceros,'CUS','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" },
					{field: "CustodiaPropiaDCV",			title: $translate.instant("UI_CUSTODIA_PROPIA_DCV"),		width: "100px"	, template : " <label> #:  (util.bcs_formato(CustodiaPropiaDCV,'CUS','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" },
					{field: "CustodiaTercerosDCV",			title: $translate.instant("UI_CUSTODIA_TERCEROS_DCV"),		width: "100px"	, template : " <label> #:  (util.bcs_formato(CustodiaTercerosDCV,'CUS','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" },
					{field: "CustodiaSociedadEmisora",		title: $translate.instant("UI_CUSTODIA_SOCIEDAD_EMISORA"),	width: "100px"	, template : " <label> #:  (util.bcs_formato(CustodiaSociedadEmisora,'CUS','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" },
					{field: "CustodiaIntermediario",		title: $translate.instant("UI_CUSTODIA_INTERMEDIARIO"),		width: "100px"	, template : " <label> #:  (util.bcs_formato(CustodiaIntermediario,'CUS','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" },
					{field: "CustodiaBolsas",				title: $translate.instant("UI_CUSTODIA_BOLSAS"),			width: "100px"	, template : " <label> #:  (util.bcs_formato(CustodiaBolsas,'CUS','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" },
					{field: "CustodiaOtros",				title: $translate.instant("UI_CUSTODIA_OTROS"),				width: "100px"	, template : " <label> #:  (util.bcs_formato(CustodiaOtros,'CUS','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" },
					
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
		             case 'DES':
		             	return {column: "Nemotecnico", color: "red"};
		                break;
		            default:
		            	 return {column: "Nemotecnico", color: "white"};
		                break;     
				  };	
				}
		};

		pct.grilla.Cir1720.gridCreated = function(grid){ // para evitar que la tabla se renderice vacia sin altura
			$('bcs-grid#gridCir1720 .k-grid-content.k-auto-scrollable').css("height", "422px")
			
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
			filters.p_IdTipoDocumento				='CIR1720';
		
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
			
			pct.grilla.Cir1720.dataSource.setCallParameters(filters);			
			
						
		
	    	pct.grilla.Cir1720Sel = undefined;
	    	
			
			
			
			
				    	
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
						ventanaEdicionMultiple = bcsComunes.ventanaAbrir("ModalCir1720", {PrevSeleccion :seleccionado,PrevVentana:pct.idVentana}, true, false, pct.idVentana);
						var promesa=bcsComunes.ventanaPromesa(ventanaEdicionMultiple);
						 promesa.then(function(data){
				        	pct.fn.buscar();
							 
						 });
			}			 
		};
		
		pct.GenerarArchivoTxt = function(){
			var parametrosSP = {};
			var TablaParametros = [];
			
			parametrosSP.p_IdEmpresa = $scope.SesionIdEmpresa;
			parametrosSP.p_FechaConsulta = util.dateSPdate(pct.FecTermino);
			parametrosSP.p_IdSolicitudCircular	= 0;
			parametrosSP.p_IdTipoDocumento		= 'CIR1720';
			
			TablaParametros.push({
				TipoParametro : 'IdEmpresa',
				Valor : $scope.SesionIdEmpresa
			});
			TablaParametros.push({
				TipoParametro : 'fechaHasta',
				Valor : util.dateSPdate(pct.FecTermino)
			});
			
	        parametrosSP.P_TABLAPARAMETROS		= TablaParametros;	        		
        
        	bcsComunes.ventanaOverlay(pct.idVentana,true);
        	bcsServiciosNormativa.GenerarArchivoCircular1720(parametrosSP).$promise.then(function(result) {
        		if (result.P_OUT_ARCHIVO){        			
        			var nombre = 'cart' + pct.FecTermino.getFullYear() + (String(pct.FecTermino.getMonth() + 1) < 10? '0' + String(pct.FecTermino.getMonth() + 1): String(pct.FecTermino.getMonth() + 1)) + (String(pct.FecTermino.getDate()) < 10? '0' + String(pct.FecTermino.getDate()): String(pct.FecTermino.getDate())) +'.TXT';
        			
        			$scope.downloadFile(nombre,result.P_OUT_ARCHIVO);
        			bcsComunes.ventanaOverlay(pct.idVentana,false);       				
        		}
        	},
        	function(result){
        		bcsComunes.msg("error",$translate.instant("UI_SOLICITUD_CIRCULAR1720"),$translate.instant("UI_ARCHIVO_CIRCULAR1720_ERROR"));
        		bcsComunes.ventanaOverlay(pct.idVentana,false);
        	});
        	
        	bcsComunes.ventanaOverlay(pct.idVentana,false);
		}
				
		pct.fn.limpiar = function(){
			pct.grilla.Cir1720.dataSource.removeAll();
		};
		
		pct.Agregar = function(){
			ventanaEdicionMultiple = bcsComunes.ventanaAbrir("IngresarCir1720", {PrevVentana:pct.idVentana,FechaConsulta:pct.FecTermino}, true, false, pct.idVentana);
			var promesa=bcsComunes.ventanaPromesa(ventanaEdicionMultiple);
			promesa.then(function(data){
					 pct.FecTermino;
			 });
			
		};

	});
}());