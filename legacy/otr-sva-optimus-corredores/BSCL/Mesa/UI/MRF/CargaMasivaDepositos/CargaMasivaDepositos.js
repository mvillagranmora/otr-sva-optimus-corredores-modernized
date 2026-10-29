(function() {
	angular.module('ModuloMesa').controller('CargaMasivaDepositosCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosMesa,bcsAlerta,OPCIONES_BCSALERTA){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var cmDep 		= this;
		cmDep.idVentana	= bcsComunes.ventanaWinId($scope);
		cmDep.win		= {};
		cmDep.grilla	= {};
		cmDep.cmbbx		= {};
		cmDep.filtro	= {};
		cmDep.fn		= {};
		cmDep.ds		= {};
		
		cmDep.win.esCargaArchivo= false;
		cmDep.win.esProcesaCarga= false;
		cmDep.filtro.FechaDesde	= util.dateInicioDia($scope.SesionFechaSistema);
		cmDep.filtro.FechaHasta	= util.dateTerminoDia($scope.SesionFechaSistema);
		
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		cmDep.ds.Configuracion = new kendo.data.DataSource({
			type: "odata",
			serverFiltering : true,
			transport: {
				read: {
					url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=432",
					dataType: "json"
				}
			},
			schema: {
				model: { 
					fields: {		 	
						IdEmpresa		: { type: "number"},
						CodigoParametro	: { type: "string"},
						Descripcion		: { type: "string"},
						Valor			: { type: "string"}
					}
				}
			}
		});
		
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		cmDep.cmbbx.SocioDeNegocio={
			filtros: {
				IdEmpresa : {field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa },
				EsCliente : {field: "EsCliente", operator: "eq", value: 'UI_SI'}
			}
		};
		
		cmDep.cmbbx.Estado={
			filtros : {
				NombreEntidad : { field: "toupper(NombreEntidad)", operator: "eq", value: "CARGAMASIVADEPOSITOS" } // TODO revisar valores del combobox 
			}
		};
		
		
		/*********************************************
		 * GRILLAS
		 *********************************************/
		
		cmDep.grilla.Depositos = {
			dataSource: new bcs.DataSource({
				type: "odata",
				definition: {
					serverFiltering : true, // Filtrado de datos en el servidor.
					serverPaging 	: true, // Paginado de los datos en el servidor.
					serverSorting 	: true, // Ordenamiento de los datos en el servidor.
					sortable 		: true,
					//pageSize 		: 100,
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=447",
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								FechaProceso			: { type: "string"},
								IdSecuencia				: { type: "number"},
								RutClienteComprador		: { type: "string"},
								SecuenciaRutComprador	: { type: "number"},
								CustodiaComprador		: { type: "string"},
								NumeroCompra			: { type: "number"},
								RutClienteVendedor		: { type: "string"},
								SecuenciaRutVendedor    : { type: "number"},
								CustodiaVendedor		: { type: "string"},
								NumeroVenta				: { type: "number"},
								Emisor					: { type: "string"},
								FechaInicioOperacion	: { type: "string"},
								FechaTerminoPapel		: { type: "string"},
								TipoReajuste			: { type: "string"},
								MontoFinal				: { type: "number"},
								MontoInicial			: { type: "number"},
								EstadoDeposito			: { type: "string"},
								Glosa					: { type: "string"}
							}
						}
					}
				}
			}),
			columns: [
			    {field: "FechaProceso",			title: $translate.instant('UI_FECHA_PROCESO'),			width: "80px"	},
			    {field: "IdSecuencia",			title: $translate.instant("UI_SECUENCIA"),				width: "100px"	},
			    {field: "RutClienteComprador",	title: $translate.instant("UI_DOCUMENTO_IDENTIDAD_COMPRADOR"),			width: "110px",	template: "<label> #: TipoDocumentoIdentidad === 'R' ? util.formatRut(RutClienteComprador) : RutClienteComprador #</label>"},
			    {field: "SecuenciaRutComprador",title: $translate.instant("UI_SECUENCIA_DOCUMENTO_IDENTIDAD_COMPRADOR"),width: "110px"	},
			    {field: "CustodiaComprador",	title: $translate.instant("UI_CUSTODIA_COMPRADOR"),		width: "110px"	},
			    {field: "NumeroCompra",			title: $translate.instant("UI_NUMERO_COMPRA"),			width: "100px",	hidden: cmDep.win.esCargaArchivo	},
			    {field: "RutClienteVendedor",	title: $translate.instant("UI_DOCUMENTO_IDENTIDAD_VENDEDOR"),			width: "110px",	template: "<label> #: TipoDocumentoIdentidad === 'R' ? util.formatRut(RutClienteVendedor) : RutClienteVendedor #</label>"},
			    {field: "SecuenciaRutVendedor",	title: $translate.instant("UI_SECUENCIA_DOCUMENTO_IDENTIDAD_VENDEDOR"),	width: "110px"	},
			    {field: "CustodiaVendedor",		title: $translate.instant("UI_CUSTODIA_VENDEDOR"),		width: "110px"	},
			    {field: "NumeroVenta",			title: $translate.instant("UI_NUMERO_VENTA"),			width: "100px"	},
			    {field: "Emisor",				title: $translate.instant("UI_EMISOR"),					width: "100px"	},
			    {field: "FechaInicioOperacion",	title: $translate.instant('UI_FECHA_INICIO_OPERACION'),	width: "80px"	},
			    {field: "FechaTerminoPapel",	title: $translate.instant('UI_FECHA_TERMINO_PAPEL'),	width: "80px"	},
			    {field: "TipoReajuste",			title: $translate.instant('UI_TIPO_REAJUSTE'),			width: "80px"	},
			    {field: "MontoFinal",			title: $translate.instant("UI_MONTO_FINAL"),			width: "100px",	template: "<label> #: (util.bcs_formato(MontoFinal,	 'MES','MONTO',IdMoneda,'','')).valor #</label>"},
			    {field: "MontoInicial",			title: $translate.instant("UI_MONTO_INICIAL"),			width: "100px",	template: "<label> #: (util.bcs_formato(MontoInicial,'MES','MONTO',IdMoneda,'','')).valor #</label>"},
			    {field: "EstadoDeposito",		title: $translate.instant('UI_ESTADO'),					width: "70px"	},
			    {field: "Glosa",				title: $translate.instant('UI_GLOSA'),					width: "180px",	template : "<span translate>#: Glosa # </span>"	}
			],
			//pageable	: true,
			//pageSize	: 100,
			autoBind	: false,
			height		: 520,
		    selectable 	: "multiple row",
		    filterable	: true
		};
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		cmDep.fn.buscar = function(){
			var objfilters = { };
			var niv1filters = new Array();
			objfilters.logic = 'and';
			objfilters.filters = niv1filters;
	    	
			niv1filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa });
			if(cmDep.filtro.FechaDesde !== null){				
				niv1filters.push({ field: "FechaProceso", operator: "gte", value: util.dateSPdate(cmDep.filtro.FechaDesde)   });
	    	}
			if(cmDep.filtro.FechaHasta !== null){				
				niv1filters.push({ field: "FechaProceso", operator: "lte", value: util.dateSPdate(cmDep.filtro.FechaHasta)   });
	    	}
			if(cmDep.filtro.IdSocioDeNegocio && cmDep.filtro.IdSocioDeNegocio > 0){
	    		niv1filters.push({ field: "IdClienteVendedor", operator: "eq", value: cmDep.dataModal.SocioDeNegocioObj.IdCliente });
	    	}
			if(cmDep.filtro.IdEstado){
	    		niv1filters.push({ field: "IdEstado", operator: "eq", value: cmDep.filtro.IdEstado });
	    	}
	    	
			cmDep.grilla.v_Depositos = {};
	    	if (niv1filters.length>0){
	    		cmDep.grilla.Depositos.dataSource.query({filter: objfilters}).then(function(){
	    			if(cmDep.grilla.Depositos.dataSource.view().length){
	    				cmDep.win.esProcesaCarga= true;
			    		cmDep.win.esCargaArchivo= false;
			    		cmDep.grilla.v_Depositos= cmDep.grilla.Depositos.dataSource.data();
	    			}
	    			else{
	    				cmDep.win.esProcesaCarga= false;
	    			}
	    		});
	    	}
	    	else cmDep.grilla.Depositos.dataSource.filters(new Array());
	    	
	    	cmDep.grilla.depositosSel = undefined;
		};
		
		cmDep.fn.limpiar = function(){
			cmDep.filtro.FechaDesde			= util.dateInicioDia($scope.SesionFechaSistema);
			cmDep.filtro.FechaHasta			= util.dateInicioDia($scope.SesionFechaSistema);
			cmDep.filtro.IdSocioDeNegocio	= 0;
			cmDep.filtro.IdEstado			= 0;
			cmDep.grilla.Depositos.dataSource.removeAll();
		}
		
		
		//Se obtiene valor Configuracion si hay validacion entre fecha proceso con fecha archivo
		var filterConfiguracion = [ { field: "IdEmpresa",		operator: "eq", value: $scope.$root.SesionIdEmpresa },
		                            { field: "CodigoParametro",	operator: "eq", value: 'FECHAPROCESOARCHIVO'} ];
		cmDep.ds.Configuracion.query({ filter: filterConfiguracion }).then(function(){
			if(cmDep.ds.Configuracion.view().length > 0) {
				cmDep.win.validacionFechas = cmDep.ds.Configuracion.view()[0].Valor === 'S' ? true : false;
			}
		});
		
		
		cmDep.fn.cargarArchivo = function(){
			var datosArchivo = {codigoArchivo: 'CARGA_MASIVA_DEPOSITOS', titulo: $translate.instant('UI_CARGA_MASIVA_DE_DEPOSITOS')};
			var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral", datosArchivo, false, false, cmDep.idVentana);
			
			var promesa=bcsComunes.ventanaPromesa(id1);
			promesa.then(function(data){
				console.log(data);
				
				if(data && data.fileData && data.fileName){
					var fileName	= data.fileName.replace(/\D/g,''); //obtiene fecha de nombre del archivo DDMMYYYY
					var dateFileName= fileName.substring(4)+'-'+fileName.substring(2,4)+'-'+fileName.substring(0,2); //DDMMYYYY -> YYYY-MM-DD
					
					for(var key in data.fileData){
						var fecProc = data.fileData[key].FECHAPROCESO.trim(); //YYYY-MM-DD
						if(cmDep.win.validacionFechas && fecProc !== dateFileName){
							bcsComunes.msg("error",$translate.instant("UI_CARGA_MASIVA_DE_DEPOSITOS"),$translate.instant("UI_NOMBRE_ARCHIVO_INCORRECTO"));
							return;
						}
						for(var field in data.fileData[key]){
							if(data.fileData[key][field] && data.fileData[key][field] !== '' && typeof data.fileData[key][field] === 'string' ){
								data.fileData[key][field] = data.fileData[key][field].trim();
							}
						}
					}
				}
				else{
					bcsComunes.ventanaOverlay(cmDep.idVentana,false);
					return;
				}
				cmDep.grilla.v_Depositos = {};
				
				var parametrosSP = {};
				parametrosSP.p_IdEmpresa	= $scope.$root.SesionIdEmpresa;
				parametrosSP.p_IdUsuario 	= $scope.$root.SesionIdUsuario;
		        parametrosSP.p_IdModulo		= 'MRF';
		        parametrosSP.p_NombreArchivo= data.fileName;
		        parametrosSP.p_DataArchivo	= data.fileData;
		        
		        if(data.fileData.length){
		        	bcsComunes.ventanaOverlay(cmDep.idVentana,true);
		        	bcsServiciosMesa.ObtenerDatosDesdeArchivoDAP(parametrosSP).$promise.then(function(result) {
		        		if (result.P_OUT_ADVERTENCIASCONTADOR){
		        			bcsComunes.msg("warning",$translate.instant("UI_CARGA_MASIVA_DE_DEPOSITOS"),result.P_OUT_ADVERTENCIASDETALLE[0].Codigo + ' : ' + result.P_OUT_ADVERTENCIASDETALLE[0].Descripcion);
		        		}
		        		else if (result.P_OUT_ERRORESCONTADOR){
		        			bcsComunes.msg("error",$translate.instant("UI_CARGA_MASIVA_DE_DEPOSITOS"),result.P_OUT_ERRORESDETALLE[0].Codigo + ' : ' + result.P_OUT_ERRORESDETALLE[0].Descripcion);
		        		}
		        		else{
		        			
		        			cmDep.grilla.Depositos.dataSource.data([]);
		        			for(var key in result.P_OUT_DEPOSITOS){
		        				cmDep.grilla.Depositos.dataSource.add(result.P_OUT_DEPOSITOS[key]);
		        			}
		        			cmDep.win.esCargaArchivo= true;
		        			cmDep.win.esProcesaCarga= false;
		        			cmDep.grilla.v_Depositos= result.P_OUT_DEPOSITOS;
		        			
	        				bcsComunes.msg("info",$translate.instant("UI_CARGA_MASIVA_DE_DEPOSITOS"),$translate.instant("UI_CARGA_DAP_REALIZADA"));
		        		}
		        		bcsComunes.ventanaOverlay(cmDep.idVentana,false);
		        	},
		        	function(result){
		        		bcsComunes.msg("error",$translate.instant("UI_CARGA_MASIVA_DE_DEPOSITOS"),$translate.instant("UI_CARGA_CON_ERRORES"));
		        		bcsComunes.ventanaOverlay(cmDep.idVentana,false);
		        	});
		        }
		        else bcsComunes.ventanaOverlay(cmDep.idVentana,false);
			}); 
		};
		
		
		cmDep.fn.grabar = function(){
			bcsComunes.ventanaOverlay(cmDep.idVentana,true);
			
			var parametrosSP = {};
			parametrosSP.p_IdEmpresa		= $scope.$root.SesionIdEmpresa;
			parametrosSP.p_IdUsuario		= $scope.$root.SesionIdUsuario;
	        parametrosSP.p_IdModulo			= 'MRF';
	        parametrosSP.p_UsaReglasNegocio	= 'N';
	        parametrosSP.p_RegistrosGrabar	= cmDep.grilla.v_Depositos;
	        
	        if(cmDep.grilla.v_Depositos.length){
	        	bcsServiciosMesa.GrabarArchivoCargaDAP(parametrosSP).$promise.then(function(result) {
	        		if (result.P_OUT_ADVERTENCIASCONTADOR){
	        			bcsComunes.msg("warning",$translate.instant("UI_CARGA_MASIVA_DE_DEPOSITOS"),result.P_OUT_ADVERTENCIASDETALLE[0].Codigo + ' : ' + result.P_OUT_ADVERTENCIASDETALLE[0].Descripcion);
	        		}
	        		else if (result.P_OUT_ERRORESCONTADOR){
	        			bcsComunes.msg("error",$translate.instant("UI_CARGA_MASIVA_DE_DEPOSITOS"),result.P_OUT_ERRORESDETALLE[0].Codigo + ' : ' + result.P_OUT_ERRORESDETALLE[0].Descripcion);
	        		}
	        		else{
        				bcsComunes.msg("info",$translate.instant("UI_CARGA_MASIVA_DE_DEPOSITOS"),$translate.instant("UI_GRABAR_DAP_REALIZADO"));
        				cmDep.grilla.Depositos.dataSource.data([]);
        				cmDep.grilla.v_Depositos = {};
        				cmDep.win.esCargaArchivo= false;
	        		}
	        		bcsComunes.ventanaOverlay(cmDep.idVentana,false);
	        		console.log(result);
	        	},
	        	function(result){
	        		bcsComunes.msg("error",$translate.instant("UI_CARGA_MASIVA_DE_DEPOSITOS"),$translate.instant("UI_GRABAR_CON_ERRORES"));
	        		bcsComunes.ventanaOverlay(cmDep.idVentana,false);
	        	});
	        }
	        else bcsComunes.ventanaOverlay(cmDep.idVentana,false);
		};
		
		
		cmDep.fn.procesar = function(){
			var parametrosSP = {};
			parametrosSP.p_IdEmpresa		= $scope.$root.SesionIdEmpresa;
			parametrosSP.p_IdUsuario		= $scope.$root.SesionIdUsuario;
	        parametrosSP.p_IdModulo			= 'MRF';
	        parametrosSP.p_UsaReglasNegocio	= 'S';
	        parametrosSP.p_simularOperacion	= 'N';
	        parametrosSP.p_IdBolsa			= $scope.$root.SesionBolsaDefectoSistema;
	        parametrosSP.p_IdPais			= $scope.$root.SesionIdPaisSistema;
	        
	        var msjProcesar = '';
	        if(cmDep.grilla.depositoSel && cmDep.grilla.depositoSel.length){	// Registros seleccionados
	        	msjProcesar = $translate.instant("UI_MENSAJE_PROCESAR_REGISTROS_DAP_SELECCIONADOS");
	        	parametrosSP.p_tablaDeposito = cmDep.grilla.depositoSel;
	        }
	        else{	// Todos los registros
	        	msjProcesar = $translate.instant("UI_MENSAJE_PROCESAR_REGISTROS_DAP_TODOS");
	        	parametrosSP.p_tablaDeposito = cmDep.grilla.v_Depositos;
	        }
	        
	        var t = bcsAlerta.crear("PREGUNTA_SI_NO",msjProcesar);
			t.result.then(function(result){
				if(result === OPCIONES_BCSALERTA.SI){
					bcsComunes.ventanaOverlay(cmDep.idVentana,true);
					
					if(parametrosSP.p_tablaDeposito.length){
			        	bcsServiciosMesa.ProcesarArchivoCargaDAP(parametrosSP).$promise.then(function(result) {
			        		if (result.P_OUT_ADVERTENCIASCONTADOR){
			        			bcsComunes.msg("warning",$translate.instant("UI_CARGA_MASIVA_DE_DEPOSITOS"),result.P_OUT_ADVERTENCIASDETALLE[0].Codigo + ' : ' + result.P_OUT_ADVERTENCIASDETALLE[0].Descripcion);
			        		}
			        		else if (result.P_OUT_ERRORESCONTADOR){
			        			bcsComunes.msg("error",$translate.instant("UI_CARGA_MASIVA_DE_DEPOSITOS"),result.P_OUT_ERRORESDETALLE[0].Codigo + ' : ' + result.P_OUT_ERRORESDETALLE[0].Descripcion);
			        		}
			        		else{
		        				bcsComunes.msg("info",$translate.instant("UI_CARGA_MASIVA_DE_DEPOSITOS"),$translate.instant("UI_PROCESAR_DAP_REALIZADO"));
		        				cmDep.fn.buscar();
			        		}
			        		bcsComunes.ventanaOverlay(cmDep.idVentana,false);
			        	},
			        	function(result){
			        		bcsComunes.msg("error",$translate.instant("UI_CARGA_MASIVA_DE_DEPOSITOS"),$translate.instant("UI_PROCESAR_CON_ERRORES"));
			        		bcsComunes.ventanaOverlay(cmDep.idVentana,false);
			        	});
			        }
				}
				else{
					cmDep.grilla.Depositos.dataSource.read();
					cmDep.grilla.depositoSel = undefined;
					bcsComunes.ventanaOverlay(cmDep.idVentana,false);
				}
			});
		};
		
		
		cmDep.fn.anular = function(){
			var parametrosSP = {};
			parametrosSP.p_IdEmpresa		= $scope.$root.SesionIdEmpresa;
			parametrosSP.p_IdUsuario		= $scope.$root.SesionIdUsuario;
	        parametrosSP.p_IdModulo			= 'MRF';
	        parametrosSP.p_UsaReglasNegocio	= 'N';
	        parametrosSP.p_IdEstado			= 'ANU';
	        parametrosSP.p_Glosa			= 'UI_REGISTRO_ANULADO';
	        
	        var msjAnular = '';
	        if(cmDep.grilla.depositoSel && cmDep.grilla.depositoSel.length){	// Registros seleccionados
	        	msjAnular = $translate.instant("UI_MENSAJE_ANULAR_REGISTROS_DAP_SELECCIONADOS");
	        	parametrosSP.p_tablaDeposito = cmDep.grilla.depositoSel;
	        }
	        else{	// Todos los registros
	        	msjAnular = $translate.instant("UI_MENSAJE_ANULAR_REGISTROS_DAP_TODOS");
	        	parametrosSP.p_tablaDeposito = cmDep.grilla.v_Depositos;
	        }
	        
	        var t = bcsAlerta.crear("PREGUNTA_SI_NO",msjAnular);
			t.result.then(function(result){
				if(result === OPCIONES_BCSALERTA.SI){
					bcsComunes.ventanaOverlay(cmDep.idVentana,true);
					
					if(parametrosSP.p_tablaDeposito.length){
			        	bcsServiciosMesa.ModificarEstadoArchivoCargaDAP(parametrosSP).$promise.then(function(result) {
			        		if (result.P_OUT_ADVERTENCIASCONTADOR){
			        			bcsComunes.msg("warning",$translate.instant("UI_CARGA_MASIVA_DE_DEPOSITOS"),result.P_OUT_ADVERTENCIASDETALLE[0].Codigo + ' : ' + result.P_OUT_ADVERTENCIASDETALLE[0].Descripcion);
			        		}
			        		else if (result.P_OUT_ERRORESCONTADOR){
			        			bcsComunes.msg("error",$translate.instant("UI_CARGA_MASIVA_DE_DEPOSITOS"),result.P_OUT_ERRORESDETALLE[0].Codigo + ' : ' + result.P_OUT_ERRORESDETALLE[0].Descripcion);
			        		}
			        		else{
		        				bcsComunes.msg("info",$translate.instant("UI_CARGA_MASIVA_DE_DEPOSITOS"),$translate.instant("UI_ANULAR_DAP_REALIZADO"));
		        				cmDep.fn.buscar();
			        		}
			        		bcsComunes.ventanaOverlay(cmDep.idVentana,false);
			        	},
			        	function(result){
			        		bcsComunes.msg("error",$translate.instant("UI_CARGA_MASIVA_DE_DEPOSITOS"),$translate.instant("UI_ANULAR_CON_ERRORES"));
			        		bcsComunes.ventanaOverlay(cmDep.idVentana,false);
			        	});
			        }
				}
				else{
					cmDep.grilla.Depositos.dataSource.read();
					cmDep.grilla.depositoSel = undefined;
					bcsComunes.ventanaOverlay(cmDep.idVentana,false);
				}
			});
		};
		
	});
}());