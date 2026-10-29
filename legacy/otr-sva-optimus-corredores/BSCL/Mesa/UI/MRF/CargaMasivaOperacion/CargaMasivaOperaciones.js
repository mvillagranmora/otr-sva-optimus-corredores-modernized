(function() {
	angular.module('ModuloMesa').controller('CargaMasivaOperacionesCtrl',
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
		

		cmDep.cmbbx.TipoOperacion={
				filtros: {
					IdTipoProducto : {"field": "IdTipoProducto","operator": "eq", "value"   :  'MERF' },
					Accion : {"field": "Accion","operator": "eq", "value"   :  '' }
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
								url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3164",
								dataType: "json"
							}
						},
						schema: {
							model: {
								fields: {
									FechaProceso	: { type: "string"},
									IdRegistroCarga : { type: "number"},
									RutCliente		: { type: "string"},
									SecuenciaRut	: { type: "number"},
									IdTipoReajusteCabecera : { type: "string"},
									IdTipoCustodia	: { type: "string"},
									IdFormaPago		: { type: "string"},
									IdIFRS			: { type: "string"},
									IdCondicionLiquidacion : { type: "string"},
									IdIngreso		: { type: "string"},
									IdClasificacionTipoInstrumento : { type: "string"},
									Nemo			: { type: "string"},
									Emisor			: { type: "string"},
									IdTipoReajusteDetalle : { type: "string"},
									FechaVencimiento : { type: "string"},
									Cantidad 		: { type: "number"},
									MontoNominal	: { type: "number"},
									MontoValorizado	: { type: "number"},					
									FechaCarga		: { type: "string"},
									ArchivoCarga	: { type: "string"},					
									EstadoRegistro	: { type: "string"},
									Glosa			: { type: "string"}
									
								}
							}
						}
					}
				}),
				columns: [
				    {field: "FechaProceso",		title: $translate.instant('UI_FECHA_PROCESO'), width: "80px", align: "rihgt" },
				    {field: "IdRegistroCarga",	title: $translate.instant("UI_SECUENCIA"), width: "100px"	},
				    {field: "RutCliente",		title: $translate.instant("UI_RUT"), width: "110px", align: "rihgt"  },
				    {field: "SecuenciaRut",		title: $translate.instant("UI_SECUENCIA"), width: "110px"	},

				    //{field: "IdTipoReajusteCabecera", title: $translate.instant("UI_TIPO_REAJUSTE"),	width: "50px"	},
				    {field: "IdTipoCustodia",	title: $translate.instant("UI_TIPO_CUSTODIA"),	width: "50px"	},
				    {field: "IdFormaPago",		title: $translate.instant("UI_FORMA_PAGO"),	width: "50px"	},
				    {field: "IdIFRS",			title: $translate.instant("UI_CATEGORIA_IFRS"),	width: "50px"	},
				    {field: "IdCondicionLiquidacion", title: $translate.instant("UI_CONDICION_LIQUIDACION"), width: "50px"	},
				    {field: "IdIngreso",			title: $translate.instant("UI_TIPO_INGRESO"), width: "50px"	},
				    {field: "IdClasificacionTipoInstrumento", title: $translate.instant("UI_MERCADO"), width: "50px"	},
				    {field: "Nemo",				title: $translate.instant("UI_NEMO"),	width: "100px"	},
				    {field: "Emisor",			title: $translate.instant("UI_EMISOR"),	width: "100px"	},
				    {field: "IdTipoReajusteDetalle", title: $translate.instant("UI_TIPO_REAJUSTE"),	width: "50px"	},
				    {field: "FechaVencimiento",	title: $translate.instant('UI_FECHA_VENCIMIENTO'), width: "80px", align: "rihgt" },
				    {field: "Cantidad",			title: $translate.instant("UI_CANTIDAD"), width: "100px",	template: "<label> #: (util.bcs_formato(Cantidad, 'MES','MONTO','CLP','','')).valor #</label>"},
				    {field: "MontoNominal",		title: $translate.instant("UI_MONTO_NOMINAL"), width: "100px",	template: "<label> #: (util.bcs_formato(MontoNominal, 'MES','MONTO','CLP','','')).valor #</label>"},
				    {field: "MontoValorizado",	title: $translate.instant("UI_MONTO_VALORIZADO"), width: "100px",	template: "<label> #: (util.bcs_formato(MontoValorizado, 'MES','MONTO','CLP','','')).valor #</label>"},					
				    {field: "ArchivoCarga",		title: $translate.instant("UI_SECUENCIA"), width: "100px"	},
				    
				    {field: "FechaCarga",		title: $translate.instant('UI_FECHA_CARGA'), width: "80px", align: "rihgt" },
				    {field: "EstadoRegistro",	title: $translate.instant('UI_ESTADO'),	width: "120px"	},
				    {field: "Glosa",			title: $translate.instant('UI_GLOSA'), width: "180px"	}
				],
				pageable	: true,
				pageSize	: 200,
				height		: 500,
				autoBind	: false,
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
			var datosArchivo = {codigoArchivo: 'CARGA_MASIVA_OPERACIONES_MRF', titulo: $translate.instant('UI_CARGA_MASIVA_DE_OPERACIONES')};
			var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral", datosArchivo, false, false, cmDep.idVentana);
			
			var promesa=bcsComunes.ventanaPromesa(id1);
			promesa.then(function(data){
				console.log(data);
				
				if(data && data.fileData && data.fileName){
					var fileName	= data.fileName.replace(/\D/g,''); //obtiene fecha de nombre del archivo DDMMYYYY
					var dateFileName= fileName.substring(4)+'-'+fileName.substring(2,4)+'-'+fileName.substring(0,2); //DDMMYYYY -> YYYY-MM-DD
					
					for(var key in data.fileData){
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
				
				var parametrosSP = {};
				parametrosSP.p_IdEmpresa	= $scope.$root.SesionIdEmpresa;
				parametrosSP.p_IdUsuario 	= $scope.$root.SesionIdUsuario;
		        parametrosSP.p_IdModulo		= 'MRF';
		        parametrosSP.p_NombreArchivo= data.fileName;
		        parametrosSP.p_DataArchivo	= data.fileData;
		        
		        if(data.fileData.length){
		        	bcsComunes.ventanaOverlay(cmDep.idVentana,true);
		        	bcsServiciosMesa.ValidaArchivoMasivoRF(parametrosSP).$promise.then(function(result) {
		        		if (result.P_OUT_ADVERTENCIASCONTADOR){
		        			bcsComunes.msg("warning",$translate.instant("CARGA_MASIVA_OPERACIONES_MRF"),result.P_OUT_ADVERTENCIASDETALLE[0].Codigo + ' : ' + result.P_OUT_ADVERTENCIASDETALLE[0].Descripcion);
		        		}
		        		else if (result.P_OUT_ERRORESCONTADOR){
		        			bcsComunes.msg("error",$translate.instant("CARGA_MASIVA_OPERACIONES_MRF"),result.P_OUT_ERRORESDETALLE[0].Codigo + ' : ' + result.P_OUT_ERRORESDETALLE[0].Descripcion);
		        		}
		        		else{
		        			
		        			cmDep.grilla.Depositos.dataSource.data([]);
		        			for(var key in result.ResultSet_0){
		        				cmDep.grilla.Depositos.dataSource.add(result.ResultSet_0[key]);
		        			}
		        			cmDep.win.esCargaArchivo= true;
		        			cmDep.win.esProcesaCarga= false;
		        			cmDep.grilla.v_Depositos= result.ResultSet_0;
		        			
	        				bcsComunes.msg("info",$translate.instant("CARGA_MASIVA_OPERACIONES_MRF"),$translate.instant("UI_CARGA_DAP_REALIZADA"));
		        		}
		        		bcsComunes.ventanaOverlay(cmDep.idVentana,false);
		        	},
		        	function(result){
		        		bcsComunes.msg("error",$translate.instant("CARGA_MASIVA_OPERACIONES_MRF"),$translate.instant("UI_CARGA_CON_ERRORES"));
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
	        	bcsServiciosMesa.GrabaCargaMasivaOperacionesRF(parametrosSP).$promise.then(function(result) {
	        		if (result.P_OUT_ADVERTENCIASCONTADOR){
	        			bcsComunes.msg("warning",$translate.instant("UI_CARGA_MASIVA_OPERACIONES_MRF"),result.P_OUT_ADVERTENCIASDETALLE[0].Codigo + ' : ' + result.P_OUT_ADVERTENCIASDETALLE[0].Descripcion);
	        		}
	        		else if (result.P_OUT_ERRORESCONTADOR){
	        			bcsComunes.msg("error",$translate.instant("UI_CARGA_MASIVA_OPERACIONES_MRF"),result.P_OUT_ERRORESDETALLE[0].Codigo + ' : ' + result.P_OUT_ERRORESDETALLE[0].Descripcion);
	        		}
	        		else{
        				bcsComunes.msg("info",$translate.instant("UI_CARGA_MASIVA_OPERACIONES_MRF"),$translate.instant("UI_GRABAR_DAP_REALIZADO"));
        				cmDep.grilla.Depositos.dataSource.data([]);
        				cmDep.grilla.v_Depositos = {};
        				cmDep.win.esCargaArchivo= false;
	        		}
	        		bcsComunes.ventanaOverlay(cmDep.idVentana,false);
	        		console.log(result);
	        	},
	        	function(result){
	        		bcsComunes.msg("error",$translate.instant("UI_CARGA_MASIVA_OPERACIONES_MRF"),$translate.instant("UI_GRABAR_CON_ERRORES"));
	        		bcsComunes.ventanaOverlay(cmDep.idVentana,false);
	        	});
	        }
	        else bcsComunes.ventanaOverlay(cmDep.idVentana,false);
		};
		
		
		cmDep.fn.procesar = function(){
			var parametrosSP = {};
			var parametroTbl = {};
			var registroTbl = {};
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
	        }
	        else{	// Todos los registros
	        	msjProcesar = $translate.instant("UI_MENSAJE_PROCESAR_REGISTROS_DAP_TODOS");
	        }
	        
	        var t = bcsAlerta.crear("PREGUNTA_SI_NO",msjProcesar);
			t.result.then(function(result){
				if(result === OPCIONES_BCSALERTA.SI){
					bcsComunes.ventanaOverlay(cmDep.idVentana,true);
					
					if(cmDep.grilla.depositoSel.length){
						
						// Construyo array con info a procesar...
	        			for(var key in cmDep.grilla.depositoSel){
	        				registroTbl = new Object();
	        				registroTbl.IdRegistroCarga = cmDep.grilla.depositoSel[key].IdRegistroCarga;
	        				registroTbl.FolioOperacion = cmDep.grilla.depositoSel[key].FolioOperacion;
	        				registroTbl.FechaProceso = cmDep.grilla.depositoSel[key].FechaProceso;
	        				parametroTbl[key] = registroTbl; 
	        			}
	        			parametrosSP.p_OperacionesRF = parametroTbl;
						
			        	bcsServiciosMesa.ProcesarCargaMasivaOperacionesRF(parametrosSP).$promise.then(function(result) {
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