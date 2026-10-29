/*************************************************************
 * Purpose	: Blotter de Consulta de Socio de Negocio.
 * Author	: TEAM PRONET IT SPA - BCS
 * Date		: 13/03/2018
 **************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('GestionEnrolamientoCtrl',
	function($scope,bcsComunes,servicioSociodeNegocio,$translate)
	{
/******************************************************/
/*** INICIO DE VARIABLES Y PRECARGA					***/
/******************************************************/
		
		var conSocNeg = this;
		$scope.formData = {};
		conSocNeg.win = {};
		conSocNeg.ventanaId = bcsComunes.ventanaWinId($scope);
		conSocNeg.win.defSinValor = '-';
		var PestanaPortafolioAPVHabilitada = BCS_CONFIGURACION.PLATAFORMA["APV.Habilitado"] === 'S' ? 'S' : 'N';
/******************************************************/
/*** FUNCIONES DE CARGA DE TABLAS					***/
/******************************************************/
		/*
		var dataSourceConfiguracion = new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true,
				transport: {
					read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1556",
							dataType: "json"
						  }		
				},
				schema: {
			     		model: {
				        		fields: { Valor : { type: "string"}	}
			     		}
				},
				filter: [{ field: "CodigoParametro"	, operator: "eq", value: 'APV.Habilitado' },
				         { field: "IdEmpresa"		, operator: "eq", value: $scope.SesionIdEmpresa }]
			}
		});
		*/
		
		var PortafolioClienteDataSource = new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering:true,
				transport: {
					read: {
						url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1265",
						dataType: "json"
					}
				},
				schema: {
					model: {
						fields: {
							IdPortafolio 		: { type: "string"},
							CodigoPortafolio 	: { type: "string"},
							Proposito 			: { type: "string"},
							EstadoPortafolio 	: { type: "string"},
							FechaCreacion 		: { type: "date"}
						}
					}
				}
			}
		});
		
		var ClienteDataSource = new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering:true,
				transport: {
					read: {
						url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1275",
						dataType: "json"
				    }
				},
				schema: {
		        	model: {
		        		fields: {
			    			IdCliente 						: { type: "number"},
			    			ComentarioBloqueo				: { type: "string"},
			    			EstaHabilitado					: { type: "string"},
			    			FechaBloqueo					: { type: "date"},
			    			FechaCreacion					: { type: "date"},
			    			FechaIngreso					: { type: "date"},
			    			FechaModificacion				: { type: "date"},
			    			IdPortafolioClientePorDefecto	: { type: "number"},
			    			IdSegmentoCliente				: { type: "number"},
			    			NombreSegmentoCliente			: { type: "string"},
		    			}
		    		}
		    	}
			}
		});
		
		var EmpleadoDataSource = new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering:true,
				transport: {
					read: {
						url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1285",
						dataType: "json"
				    }
				},
				schema: {
		        	model: {
		        		fields: {
			    			IdEmpleado 					: { type: "number"},
			    			CodigoEmpleado				: { type: "string"},
			    			CodigoTipoCargoEmpleado		: { type: "string"},
			    			NombreTipoCargoEmpleado		: { type: "string"},
			    			CodigoSucursal				: { type: "string"},
			    			NombreSucursal				: { type: "string"},
			    			CodigoEmpleadoSupervisor	: { type: "string"},
			    			IdEstado					: { type: "string"},
			    			NombreEstado				: { type: "string"},
			    			FechaInicioCargo			: { type: "date"  },
			    			FechaTerminoCargo			: { type: "date"  },
		    			}
		    		}
		    	}
			}
		});
		
		var ProveedorDataSource = new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering:true,
				transport: {
					read: {
						url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1290",
						dataType: "json"
				    }
				},
				schema: {
		        	model: {
		        		fields: {
			    			IdProveedor				: { type: "number"},
			    			CodigoEmpleado			: { type: "string"},
			    			IdAreaProveedor			: { type: "number"},
			    			NombreAreaProveedor		: { type: "string"},
			    			IdEstado				: { type: "string"},
			    			NombreEstado			: { type: "string"},
		    			}
		    		}
		    	}
			}
		});
		
		var ContraparteDataSource = new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering:true,
				transport: {
					read: {
						url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1284",
						dataType: "json"
				    }
				},
				schema: {
		        	model: {
		        		fields: {
			    			IdContraparte 					: { type: "number"},
			    			Corredora						: { type: "string"},
			    			Codigo							: { type: "string"},
			    			Emisor							: { type: "string"},
			    			CamaraDeCompensacion			: { type: "string"},
			    			Depositos						: { type: "string"},
			    			Banco							: { type: "string"},
			    			Bolsa							: { type: "string"},
		    			}
		    		}
		    	}
			}
		});
		
		$scope.dskOptions = new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering: true,
	        	serverPaging: true,
	        	serverSorting: true,
	       	 	pageSize:100,
	    		sort: [
	    		    { field: "NombreTipoDocumentoIdentidadDefecto", dir: "asc" },
	          	  	{ field: "DocumentoIdentidadDefecto", dir: "asc" },
	               	{ field: "NombreCompleto", dir: "asc" }
	            ],
	        	transport: {
	            	read: {
	            		url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1806",
	            		dataType: "json"
	            	}
	        	},
	        	schema: {
	        		model: {
		        		fields: {
		        			IdSocioDeNegocio					: { type: "number"},
		        			FechaIngreso						: { type: "date"},
		        			FechaCreacion						: { type: "date"},
		        			FechaModificacion					: { type: "date"},
		        			IdTipoDocumentoIdentidadDefecto		: { type: "string"},
		        			NombreTipoDocumentoIdentidadDefecto : { type: "string"},
		        			NombreCompleto						: { type: "string"},
		        			DocumentoIdentidadDefecto			: { type: "string"},
		        			TipoPersona							: { type: "string"},
		        			RazonSocial							: { type: "string"},
		        			NombreCompleto						: { type: "string"},
		        			Nombres								: { type: "string"},
		        			ApellidoPaterno						: { type: "string"},
		        			ApellidoMaterno						: { type: "string"},
		        			Alias								: { type: "string"},
		        			IdEstadoCivil						: { type: "string"},
		        			EstadoCivil							: { type: "string"},
		        			FechaNacimiento						: { type: "date"},
		        			IdPaisResidencia					: { type: "string"},
		        			CodigoPaisResidencia				: { type: "string"},
		        			NombrePaisResidencia				: { type: "string"},
		        			FechaBloqueo						: { type: "date"},
		        			DireccionCalle						: { type: "string"},
		        			DireccionNumero						: { type: "string"},
		        			DireccionDepartamento				: { type: "string"},
		        			IdTipoTelefono						: { type: "string"},
    	        			Telefono							: { type: "string"},
    	        			Email								: { type: "string"}
	        			}
	        		}
	        	}
			}
		});
		
/******************************************************/
/*** DATASOURCES									***/
/******************************************************/
			
		$scope.kOptions =
		{
			columns :
			[
				 {field: "FechaIngreso",						title: $translate.instant('UI_FECHA_INGRESO_ABR'), format: "{0:d}",align: "left",width: "105px"}
				,{field: "NombreTipoDocumentoIdentidadDefecto",	title: $translate.instant('UI_TIPO_DOCUMENTO_IDENTIDAD_ABR'),align: "left",width: "80px"}
				,{field: "DocumentoIdentidadDefecto",			title: $translate.instant('UI_DOCUMENTO_IDENTIDAD_ABR'),align: "left",width: "100px"}
				,{field: "TipoPersona",							title: $translate.instant('UI_TIPO_PERSONA_ABR'),align: "left",width: "100px"}
				,{field: "NombreCompleto",						title: $translate.instant('UI_NOMBRE'),align: "left",width: "300px"}
				,{field: "NombrePaisResidencia",				title: $translate.instant('UI_PAIS_RESIDENCIA_ABR'),align: "left",width: "100px"}
				,{field: "EstadoCivil",							title: $translate.instant('UI_ESTADO_CIVIL'),align: "left",width: "70px"}
				,{field: "DireccionCalle",						title: $translate.instant('UI_CALLE'),align: "left",width: "70px"}
				,{field: "DireccionNumero",						title: $translate.instant('UI_NUMERO'),align: "left",width: "100px"}
				,{field: "DireccionDepartamento",				title: $translate.instant('UI_COMPLEMENTO'),align: "left",width: "100px"}
				,{field: "IdTipoTelefono",						title: $translate.instant('UI_TIPO_TELEFONO'),align: "left",width: "70px"}
				,{field: "Telefono",							title: $translate.instant('UI_TELEFONO'),align: "left",width: "100px"}
				,{field: "Email",								title: $translate.instant('UI_EMAIL'),align: "left",width: "100px"}
			],
			selectable		: "row",
			dataSource		: $scope.dskOptions,
			pageable		: true,
			sortable		: true,
			filterable		: true,
			pageSize		: 50,
			autoBind		: false,
			userSettings	: true,
			navigatable		: true,
			excel 			: {
				allPages: true,
			},
			height			: 520,
			detailInit		: function(e) {
				$scope.initDetalle(e.data);
			}
		};
		
/******************************************************/
/*** FUNCIONES										***/
/******************************************************/
		
		$scope.initDetalle = function(dataItem)
		{
			var filterCli = [];
			var filterEmp = [];
			var filterPro = [];
			var filterCnt = [];
			dataItem.datosCli = {};
			dataItem.datosEmp = {};
			dataItem.datosPro = {};
			dataItem.datosCnt = {};
			
			// DATOS TAB CLIENTE
			if(dataItem.IdCliente>0) {
				filterCli.push({ field: "IdCliente", operator: "eq", value: Number(dataItem.IdCliente) });
				ClienteDataSource.query({filter:filterCli}).then(function() {
					if (ClienteDataSource.view().length > 0){
						var datos = ClienteDataSource.data();
						if (datos[0])
						{
							dataItem.datosCli = datos[0];
							PortafolioClienteDataSource.query({filter:filterCli}).then(function() { //obtener cantidad de portafolios.
								var cantPort = PortafolioClienteDataSource.view().length;
								dataItem.datosCli.cantidadPortafolios = cantPort;
							});
						}
					}
				});
			}
			
			// DATOS TAB EMPLEADO
			if(dataItem.IdEmpleado>0) {
				filterEmp.push({ field: "IdEmpleado", operator: "eq", value: Number(dataItem.IdEmpleado) });
				EmpleadoDataSource.query({filter:filterEmp}).then(function() {
					if (EmpleadoDataSource.view().length > 0){
						var datos = EmpleadoDataSource.data();
						if (datos[0]) dataItem.datosEmp = datos[0];
					}
				});
			}
			
			// DATOS TAB PROVEEDOR
			if(dataItem.IdProveedor>0) {
				filterPro.push({ field: "IdProveedor", operator: "eq", value: Number(dataItem.IdProveedor) });
				ProveedorDataSource.query({filter:filterPro}).then(function() {
					if (ProveedorDataSource.view().length > 0){
						var datos = ProveedorDataSource.data();
						if (datos[0]) dataItem.datosPro = datos[0];
					}
				});
			}
			
			// DATOS TAB CONTRAPARTE
			if(dataItem.IdContraparte>0) {
				filterCnt.push({ field: "IdContraparte", operator: "eq", value: Number(dataItem.IdContraparte) });
				ContraparteDataSource.query({filter:filterCnt}).then(function() {
					if (ContraparteDataSource.view().length > 0){
						var datos = ContraparteDataSource.data();
						if (datos[0]) { 
							dataItem.datosCnt = datos[0];
							dataItem.datosCnt.Corredora 			= (dataItem.datosCnt.Corredora==='S')?true:false;
							dataItem.datosCnt.Emisor			 	= (dataItem.datosCnt.Emisor==='S')?true:false;
							dataItem.datosCnt.CamaraDeCompensacion 	= (dataItem.datosCnt.CamaraDeCompensacion==='S')?true:false;
							dataItem.datosCnt.Depositos 			= (dataItem.datosCnt.Depositos==='S')?true:false;
							dataItem.datosCnt.Banco 				= (dataItem.datosCnt.Banco==='S')?true:false;
							dataItem.datosCnt.Bolsa 				= (dataItem.datosCnt.Bolsa==='S')?true:false;
						}
					}
				});
			}
		};
		
		/*
		dataSourceConfiguracion.fetch(function(){
			PestanaPortafolioAPVHabilitada = 'N';
			if(dataSourceConfiguracion.data()[0].Valor === 'S')
				PestanaPortafolioAPVHabilitada = 'S';			
	 	});
	 	*/
		
		$scope.abrirDetalleSoc = function(items)
		{
			if(items.IdSocioDeNegocio === 0 && items.IdPersona === 0)
			{
				bcsComunes.msg("warning",'warning',$translate.instant('UI_ESTE_EROLAMIENTO_NO_ES_UNA_PERSONA_DEBES_INGRESARLO_COMO_TAL'));
				
				var IdVentanamodal = bcsComunes.ventanaAbrir("CrearPersona", {tipoPersonaCrear : "N", modoEditar : true, documentoIdentidadCliente : items.DocumentoIdentidadDefecto, IdDocumentoIdentidad : items.DocumentoIdentidadDefecto } );
				var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
				
				promesa.then(function(data){
					$scope.read();
				});
			}
			else
			{
				bcsComunes.ventanaAbrir("administrarSocioNegocio", {IdSocioNegocio : items.IdSocioDeNegocio, Entidad: "SOC", IdPersona : items.IdPersona, PestanaPortafolioAPVHabilitada : PestanaPortafolioAPVHabilitada},false);
			}
		};
		
		$scope.crearPersona = function(){		
			bcsComunes.ventanaAbrir("CrearPersona", {} );
		};

		$scope.crearCliente = function(){
			bcsComunes.ventanaAbrir("CrearCliente", {} );
		};
		
		$scope.crearEmpleado = function(){
			bcsComunes.ventanaAbrir("CrearEmpleado", {} );
		};
		
		$scope.crearContraparte = function(){
			bcsComunes.ventanaAbrir("CrearContraparte", {} );
		};
		
		$scope.crearProveedor = function(){
			bcsComunes.ventanaAbrir("CrearProveedor", {} );
		};
		
		$scope.crearClienteNomina = function(){
			bcsComunes.ventanaAbrir("CrearClienteNomina", {} );
		};
	
		$scope.clear = function(){
			$scope.formData = {};
				$scope.formData.tipoSocioNegocio = 0;
				$scope.formData.tipoDocumentoIdentidad = 0;
		}
		
		var keyEnter = false;
		$scope.fnKeyEnter = function(){
			if(keyEnter == true && $scope.dskOptions.data().length==1) {
				$scope.abrirDetalleSoc($scope.dskOptions.data()[0]);
				keyEnter = false;
			}else {
				keyEnter = true;
				$scope.read();
			}
		};
	
		$scope.read = function(){			
			var objfilters = { };
			var niv1filters = new Array();
			objfilters.logic = 'and';
			objfilters.filters = niv1filters;
			niv1filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa });
	    	if ($scope.formData.tipoDocumentoIdentidad){
	    		niv1filters.push({ field: "IdTipoDocumentoIdentidadDefecto", operator: "eq", value: $scope.formData.tipoDocumentoIdentidad });
	    	}
	    	if ($scope.formData.NumeroDocumentoIdentidad){
	    		niv1filters.push({ field: "tolower(DocumentoIdentidadDefecto)", operator: "contains", value: $scope.formData.NumeroDocumentoIdentidad.toLowerCase() });
	    		//niv1filters.push({ field: "tolower(ListaDocumentosIdentidad)", operator: "contains", value: $scope.formData.NumeroDocumentoIdentidad.toLowerCase() });
	    	}
	    	
	    	if ($scope.formData.fechaIngresoDesde != undefined) {
				var resp = util.validarRangoFecha($scope.formData.fechaIngresoDesde, $scope.formData.fechaIngresoHasta);
				if(resp.estado){
					bcsComunes.msg("error", "Error", $translate.instant(resp.mensaje));
					return;
				}
				niv1filters.push({ field: "FechaIngreso", operator: "ge", value: util.dateSPseconddate($scope.formData.fechaIngresoDesde) });
				
			}
			if ($scope.formData.fechaIngresoHasta != undefined) {
				niv1filters.push({ field: "FechaIngreso", operator: "le", value: util.dateSPseconddate(util.dateTerminoDia($scope.formData.fechaIngresoHasta)) });
			}
			
	    	if ($scope.formData.NombreCompleto){
				var obj2filters = { };
				var niv2filters = new Array();
				obj2filters.logic = 'or';
				obj2filters.filters = niv2filters;
				//Busqueda Alias
				niv2filters.push({ field: "tolower(Alias)", operator: "contains", value: $scope.formData.NombreCompleto.toLowerCase() });
				//Busqueda DocumentoIdentidadDefecto
				//niv1filters.push({ field: "tolower(ListaDocumentosIdentidad)", operator: "contains", value: ($scope.formData.NumeroDocumentoIdentidad||'').toLowerCase() });
				
				//Buscar socio de negocio con nombre ingresado en cualquier orden (siempre y cuando contenga mas de dos palabras)
				var nombreSplit = $scope.formData.NombreCompleto.toLowerCase().split(" ");
				if(nombreSplit.length>=1){
					var obj3filters = { };
					var niv3filters = new Array();
					obj3filters.logic = 'or';
					obj3filters.filters = niv3filters;
					for(var i=0; i< nombreSplit.length;i++){
						niv3filters.push({ field: "tolower(NombreCompleto)", operator: "contains", value: nombreSplit[i] });
						if((i+1)==nombreSplit.length){
							niv2filters.push(obj3filters);		
						}
					}
				}
				//Adjunta Filtros a listas
				niv1filters.push(obj2filters);
				
	    	}
	    	if($scope.kwidget){
				if (niv1filters.length>0) 
					$scope.kwidget.dataSource.filter(objfilters);
				else  
					$scope.kwidget.dataSource.filter(new Array());
			}
		};
		
		$scope.exportarSociosDeNegocio= function()
		{
			var data = [];
			var title = "";
			var tipo = "";
			
			var socioExportarDatasource = new bcs.DataSource({
				type: "odata",
				definition : {
					serverFiltering:true,
					transport: {
						read: {
	                		url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1806",
	                		dataType: "json"
	                	}
					},
					filter: [],
					schema: {
		        		model: {
	    	        		fields: {
	    	        			IdSocioDeNegocio : { type: "number"}
	    	        			,FechaIngreso : { type: "date"}
	    	        			,FechaCreacion : { type: "date"}
	    	        			,FechaModificacion : { type: "date"}
	    	        			,IdTipoDocumentoIdentidadDefecto : { type: "string"}
	    	        			,NombreTipoDocumentoIdentidadDefecto : { type: "string"}
	    	        			,NombreCompleto : { type: "string"}
	    	        			,DocumentoIdentidadDefecto : { type: "string"}
	    	        			,TipoPersona : { type: "string"}
	    	        			,RazonSocial : { type: "string"}
	    	        			,NombreCompleto : { type: "string"}
	    	        			,Nombres : { type: "string"}
	    	        			,ApellidoPaterno : { type: "string"}
	    	        			,ApellidoMaterno : { type: "string"}
	    	        			,Alias : { type: "string"}
	    	        			,FechaNacimiento : { type: "date"}
	    	        			,IdPaisResidencia : { type: "string"}
	    	        			,CodigoPaisResidencia : { type: "string"}
	    	        			,NombrePaisResidencia : { type: "string"}
	    	        			,FechaBloqueo: { type: "date"}
	    	        			,DireccionCalle : { type: "string"}
	    	        			,DireccionNumero : { type: "string"}
	    	        			,DireccionDepartamento : { type: "string"}
	    	        			,Telefono : { type: "string"}
	    	        			,Email: { type: "string"}
		        			}
		        		}
		        	}
				}});
			
			
			var columnsSocioNeg =
			[
				 {field: "FechaIngreso",title: $translate.instant('UI_FECHA_INGRESO_ABR'), format: "{0:d}",align: "left",width: "70px"}
				,{field: "NombreTipoDocumentoIdentidadDefecto",title: $translate.instant('UI_TIPO_DOCUMENTO_IDENTIDAD_ABR'),align: "left",width: "80px"}
				,{field: "DocumentoIdentidadDefecto",template:"<label> #:  IdTipoDocumentoIdentidadDefecto==='R'?util.formatRut(DocumentoIdentidadDefecto):DocumentoIdentidadDefecto #</label>",title: $translate.instant('UI_DOCUMENTO_IDENTIDAD_ABR'),align: "left",width: "100px"}
				,{field: "TipoPersona",title: $translate.instant('UI_TIPO_PERSONA_ABR'),align: "left",width: "100px"}
				,{field: "NombreCompleto",title: $translate.instant('UI_NOMBRE'),align: "left",width: "300px"}
				,{field: "NombrePaisResidencia",title: $translate.instant('UI_PAIS_RESIDENCIA_ABR'),align: "left",width: "100px"}
				,{field: "DireccionCalle",title: $translate.instant('UI_CALLE'),align: "left",width: "100px"}
				,{field: "DireccionNumero",title: $translate.instant('UI_NUMERO'),align: "left",width: "70px"}
				,{field: "DireccionDepartamento",title: $translate.instant('UI_COMPLEMENTO'),align: "left",width: "100px"}
				,{field: "IdTipoTelefono",title: $translate.instant('UI_TIPO_TELEFONO'),align: "left",width: "70px"}
				,{field: "Telefono",title: $translate.instant('UI_TELEFONO'),align: "left",width: "100px"}
				,{field: "Email",title: $translate.instant('UI_EMAIL'),align: "left",width: "100px"}
			];
			
					
			data = _.map(columnsSocioNeg,function(element, index){
				return { field: element.field, title: element.title};
			});
			
			title = "Socio de Negocio";
			tipo = "SOCIO_NEGOCIO";
			
			bcsComunes.ventanaOverlay(conSocNeg.ventanaId,true);
			socioExportarDatasource.fetch().then(function(result) {
				
				if (socioExportarDatasource.data().length === 0) {
					bcsComunes.msg("warning","UI_ADVERTENCIA","UI_NO_SE_ENCONTRARON_DATOS");
					return;
				}
				var nombre = tipo + "_" + util.dateSPdate(new Date());
				socioExportarDatasource.exportToExcel(data,title,nombre);
				bcsComunes.ventanaOverlay(conSocNeg.ventanaId,false);
			},function(result){
				bcsComunes.ventanaOverlay(conSocNeg.ventanaId,false);
			});
			
		};
		
		$scope.exportarClientesFallecidos = function()
		{
			var datos ={
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo  : 'SOC',
					p_TipoPrograma : [{
						IdInterno : 0,
						IdTipoPrograma : 'GenerarReporteClienteFallecido',
						SegundosDesfase : 0
					}],
					p_Parametro : [{
						IdInterno : 0,
						NombreParametro : 'IdEmpresa',
						Valor : $scope.SesionIdEmpresa
					},{
						IdInterno : 0,
						NombreParametro : 'IdUsuario',
						Valor : $scope.SesionIdUsuario
					},{
						IdInterno : 0,
						NombreParametro : 'IdModulo',
						Valor : 'SOC'
					}]
			}
			
			servicioSociodeNegocio.generarReporteClienteFallecidos(datos).$promise.then(
			function(result) {
				bcsComunes.ventanaOverlay(conSocNeg.ventanaId,false);
				var fechaLocal=util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion,"FechaHora","CALL");
				bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO').replace("::IdPrograma",result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion",util.formatoSecondate(fechaLocal)),
					conflictos : util.parseServerResponse(result)}, conSocNeg.ventanaId);

			},
			function(result){
				bcsComunes.ventanaOverlay(conSocNeg.ventanaId,false);
				bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
			});
		}
		
		$scope.bloqueoDesbloqueoMasivo= function(){};
		
/******************************************************/
/*** WATCH											***/
/******************************************************/
		
		// Se obtienen placeholder
		$scope.placeholder = {};
		$scope.$watch('formData.tipoDocumentoIdentidad',function(newVal)
		{
			if(newVal!==undefined)
			{
				if(newVal==='R') 
					$scope.placeholder.documentoIdentidad = $scope.$root.SesionValoresDefectoSistema.PLACEHOLDER_RUT;
				else if(newVal==='P')
					$scope.placeholder.documentoIdentidad = '';
				else $scope.placeholder.documentoIdentidad = '';
			}
		});
		
/******************************************************/
/*** FINAL											***/
/******************************************************/
	});
}());