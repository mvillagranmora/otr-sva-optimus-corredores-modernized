(function() {
	angular.module('ModuloNormativa').controller('PanelCIR1720Ctrl',function($scope,$timeout,Model,Utils,$translate,bcsComunes,$compile,bcsServiciosNormativa, bcsVisorCircularConfig){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var pct = this;
		pct.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		//pct.parametros	= bcsComunes.ventanaParams($scope.idVentana);
		pct.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		pct.IdTipoDocumento = 'CIR1720';
		$scope.dataTemplate	= {};
		/*********************************************
		 * COMBOBOX
		 ********************************************/
		pct.opcionFiltro='AM';
				
		/*********************************************
		 * VARIABLES PARAMETROS
		 ********************************************/
		
		var p_TablaParametrosaux ; //parametros generales que puede ver el jobmanager
		var params = {
				p_TablaParametrosAux : [
				    {	Tipo			: 'BATCH',
				    	NombreParametro : 'idUsuario',
				    	Valor : $scope.SesionIdUsuario
				    }, {Tipo			: 'BATCH',
				    	NombreParametro : 'schema',
				    	Valor : Model.getSchema()
				    }, {Tipo			: 'BATCH',
				    	NombreParametro : 'grabar',
				    	Valor : $scope.solCir.grabar
				    }, {Tipo			: 'BATCH',
				    	NombreParametro : 'reemplazar',
				    	Valor : $scope.solCir.reemplazar
				    }, {Tipo			: 'BATCH',
				    	NombreParametro : 'separadorListas',
				    	Valor : $scope.solCir.separadorListas
				    }, {Tipo			: 'BATCH',
				    	NombreParametro : 'simboloDecimal',
				    	Valor : $scope.solCir.simboloDecimal
				    }, {Tipo			: 'BATCH',
				    	NombreParametro : 'simboloFecha',
				    	Valor : $scope.solCir.simboloFecha
				    }, {Tipo			: 'BATCH',
				    	NombreParametro : 'aceptarFechasNull',
				    	Valor : $scope.solCir.aceptarFechasNull
				    },]};
		var ListadoRuts = [];
		var ListadoRutAuts = [];
		var ListadoNemos = [];
		var ListadoRutEmpl = [];
		var parametrosSP = {};
		
		/*********************************************
		 * DataSource
		 ********************************************/	
		
			var dsCuadraturaCustodia = new bcs.DataSource({
	               type : "odata",
	               definition : {
	                               serverFiltering 	: true, // Filtrado de datos en el servidor.
	                               serverPaging     : true, // Paginado de los datos en el servidor.
	                               serverSorting    : true, // Ordenamiento de los datos en el servidor.
	                               ///sort 			: { field: "IdMovimientoCarteraTransito", dir: "desc" },
	                               filter 			: { field: "IdEstado", operator: "contains", value: 'CER' },
	                               pageSize         : 100,
	                               transport 		: {
														read : {
															url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=521",
															dataType : "json"
												}
	                               },
	                              schema 				: {
												model: {
													fields : {
													    FechaCuadraturaCustodia				: {type: "string"},
														IdClasificacionTipoInstrumento		: {type: "string"}
													}
												}
											}
	               }
		});
		
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		$scope.pct.clear = function(){
			//$scope.formData = {};
			//	$scope.formData.tipoSocioNegocio = 0;
			//	$scope.formData.tipoDocumentoIdentidad = 0;
			pct.FecInicio 	 = util.dateInicioDia($scope.SesionFechaSistema);
			pct.FecTermino 	 = util.dateInicioDia($scope.SesionFechaSistema);
			
		};
		
		var successCrearSolicitud = function(respuesta)
		{	
			if (respuesta.P_OUT_ERRORESCONTADOR == 0)
			{
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				bcsComunes.msg("info","info",'Solicitud creada con éxito');
				IdSolicitudCircular =respuesta.P_OUT_RESULTADO;
				//$scope.data.limpiarFormulario();
			}
			else
			{	
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
				bcsComunes.msg("error","error",$translate.instant('Solicitud no fue creada'));
							
				var i = 0;
				for(i; i<respuesta.P_OUT_ERRORESCONTADOR; i++)
				{
					bcsComunes.log().info("Error: " + respuesta.P_OUT_ERRORESDETALLE[i].Descripcion);       
				}            
			}		
			bcsComunes.ventanaOverlay(pct.idVentana, false);
			bcsComunes.ventanaData(pct.idVentana, respuesta);
			bcsComunes.ventanaCerrar(pct.idVentana);
			
		};
		
		var errorCrearSolicitud = function(respuesta)
		{	
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
			bcsComunes.msg("error","error",$translate.instant('INGRESO ERROR'));
			bcsComunes.ventanaOverlay(pct.idVentana, false);
		};

		$scope.imprimirCircular = function() {
				var clasificacion;
			   if (pct.IdClasificacionTipoInstrumento!== 0) 
			   {clasificacion =  pct.IdClasificacionTipoInstrumento;}
			   else{
				   clasificacion='';
			   };
			
					var filterTemplate = [ { field: "IdTipoDocumento",operator:"eq", value: pct.IdTipoDocumento },
					                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
					                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
					$scope.solCir.ConsultaArchivoNormativa.query({ filter: filterTemplate }).then(function(){
						if($scope.solCir.ConsultaArchivoNormativa.view().length > 0) {
							$scope.dataTemplate.TipoReporte		= $scope.solCir.ConsultaArchivoNormativa.view()[0].TipoArchivo;
							$scope.dataTemplate.IdReporte		= $scope.solCir.ConsultaArchivoNormativa.view()[0].IdArchivo;
							$scope.dataTemplate.NombreReporte	= $scope.solCir.ConsultaArchivoNormativa.view()[0].NombreArchivo;
							$scope.dataTemplate.ServicioReporte	= $scope.solCir.ConsultaArchivoNormativa.view()[0].ServicioArchivo;

							
							$scope.dataTemplate.dataSource	= $scope.dataTemplate.ServicioReporte+"(p_IdEmpresa =>"+$scope.$root.SesionIdEmpresa+",p_FechaOperacionDesde =>'" + util.dateSPdate(pct.FecInicio) + "',p_FechaOperacionHasta =>'" +  util.dateSPdate(pct.FecTermino)+"',p_TipoInformacion =>'" + pct.opcionFiltro +"',p_IdClasificacionTipoInstrumento =>'" + clasificacion + "',p_out_TablaTransac => ?)";
							$scope.solCir.getReport($scope.dataTemplate);
							
										
						}
					});
				
		 };		
		 pct.solicitar = function() {
			var auxDate1;
			auxDate1 =  util.dateOdata(util.date(pct.FecTermino).hours(0).minutes(0).seconds(0).toDate());
		    var filterCus = [ { field: "IdEstado",operator:"eq", value: 'CER' },
		                      {field: "FechaCuadraturaCustodia",operator:"eq", value: auxDate1 }];
		    
		    if (pct.IdClasificacionTipoInstrumento!== 0) {
				filterCus.push({field :"IdClasificacionTipoInstrumento",operator:"eq" , value : pct.IdClasificacionTipoInstrumento });
			};
		    
			dsCuadraturaCustodia.query({ filter: filterCus }).then(function(){
					
						parametrosSP.p_IdEmpresa 				=  $scope.SesionIdEmpresa;
				    	parametrosSP.p_IdUsuario              	=  $scope.SesionIdUsuario;
				    	parametrosSP.p_IdModulo 				=  $scope.solCir.modulo;
						//Especificos
						parametrosSP.p_FechaInicio 				=  util.dateSPdate(pct.FecTermino);
						parametrosSP.p_FechaTermino 		    =  util.dateSPdate(pct.FecTermino);
			
						
						var fh = util.dateSPdate(pct.FecTermino);
						
					
						//Creacion Documento
						parametrosSP.p_IdTipoDocumento 			= pct.IdTipoDocumento;
			
						parametrosSP.p_TablaParametros = params.p_TablaParametrosAux;
						
						parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'FTER' , Valor : fh });
						
						var nomarch=  'CIRCULAR1720_' +  util.dateSPdate(pct.FecTermino);
						parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'NOMSAL' , Valor : nomarch});
							
						if (pct.IdClasificacionTipoInstrumento!== 0) {
							parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'CLAINS' , Valor : pct.IdClasificacionTipoInstrumento });
						};
						
								
						bcsComunes.msg('info', 'Carga', 'En proceso...');
						bcsServiciosNormativa.CircularCrearSolicitud(parametrosSP,successCrearSolicitud,errorCrearSolicitud);					
				
			});
	 						 
			 
			
	    	//Comunes
	    
			
	      
		};
		
		pct.visorCircular = function() {
			
			var paramServicio = {};
			var fd = util.dateSPdate(pct.FecTermino);
			var fh = util.dateSPdate(pct.FecTermino);
			
			paramServicio.p_TipoDocumento = pct.IdTipoDocumento;
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
					bcsVisorCircularConfig.getDataSource(respuesta.P_OUT_VERSIONESQUEMA[0].P_OUT_ESQUEMA , respuesta.P_OUT_VERSIONESQUEMA[0].P_OUT_VERSION, pct.IdTipoDocumento, 
							function (){
								var tablaParametros = [];
								
								tablaParametros.push({TipoParametro : 'IdEmpresa',Valor : $scope.SesionIdEmpresa});
								tablaParametros.push({TipoParametro : 'fechaDesde',Valor : fd});
								tablaParametros.push({TipoParametro : 'fechaHasta',Valor : fh});
								
								if (pct.IdClasificacionTipoInstrumento!== 0) {
									tablaParametros.push({TipoParametro : 'CLAINS',Valor :  pct.IdClasificacionTipoInstrumento});
								};
								
								bcsComunes.ventanaAbrir("VisorCircular", {
								tablaParametros : tablaParametros,
								IdTipoDocumento : pct.IdTipoDocumento
							});
						}, function(error){
							bcsComunes.msg('error', $scope.TITLE, error.message);
						});
				}
				else
				{	
					bcsComunes.msg('error', $scope.TITLE, 'UI_ERROR_VERSIONESQUEMA_NO_DEFINIDO');
				}		
				
			}, function(error){
				bcsComunes.msg('error', $scope.TITLE, error.message);
			});
		};
		
		$scope.pct.clear();
		
	});
}());