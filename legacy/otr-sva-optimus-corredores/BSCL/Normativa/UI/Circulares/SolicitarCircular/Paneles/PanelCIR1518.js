(function() {
	angular.module('ModuloNormativa').controller('PanelCIR1518Ctrl',function($scope,$timeout,Model,Utils,$translate,bcsComunes,$compile,bcsServiciosNormativa, bcsVisorCircularConfig){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var pct = this;
		
		/*********************************************
		 * checks
		 ********************************************/
		pct.chckTodos = false;
		pct.chckDerivados = false;
		pct.chckFFMM = false;
		pct.chckInstrucciones = false;
		pct.chckIF = false;
		pct.chckRF = false;
		pct.chckRV = false;
		pct.chckSpot = false;
		pct.DesabilitaChks=  false;
		
		
		
		/*********************************************
		 * watch todos
		 ********************************************/
		$scope.$watch('pct.chckTodos',function(newval,oldval){
			if(newval!= oldval){
				if(newval == true){
					pct.DesabilitaChks=  true;
					//deshabilitamos otros campos
					pct.chckDerivados = false;
					pct.chckFFMM = false;
					pct.chckInstrucciones = false;
					pct.chckIF = false;
					pct.chckRF = false;
					pct.chckRV = false;
					pct.chckSpot = false;
				}else{
					pct.DesabilitaChks=  false;
				}
			}		
		});
		
		
		pct.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		//pct.parametros	= bcsComunes.ventanaParams($scope.idVentana);
		pct.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		pct.IdTipoDocumento = 'CIR1518';
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
		    
		    parametrosSP.p_IdEmpresa 				=  $scope.SesionIdEmpresa;
			parametrosSP.p_IdUsuario              	=  $scope.SesionIdUsuario;
			parametrosSP.p_IdModulo 				=  $scope.solCir.modulo;
					//Especificos
			parametrosSP.p_FechaInicio 				=  util.dateSPdate(pct.FecTermino);
			parametrosSP.p_FechaTermino 		    =  util.dateSPdate(pct.FecTermino);
            parametrosSP.p_IdTipoDocumento 			=  pct.IdTipoDocumento;

				
			llenarTablaParametros();						


			if(!pct.chckTodos){
              
            let filterCus = [ { field: "IdEstado",operator:"eq", value: 'CER' },
		                      {field: "FechaCuadraturaCustodia",operator:"eq", value: auxDate1 }];

            for(let i =0; i< parametrosSP.p_TablaParametros.length; i++){
				
				if(parametrosSP.p_TablaParametros[i].NombreParametro == 'CLAINS'){					
					filterCus.push({field :"IdClasificacionTipoInstrumento",operator:"eq" , value : parametrosSP.p_TablaParametros[i].Valor });//pct.IdClasificacionTipoInstrumento 

				}
			}

				var flag =  false;
                var cont = 0;
                var contPeticion =0;
				filterCus.map(filtro=>{
					if(filtro.field == "IdClasificacionTipoInstrumento"){
							dsCuadraturaCustodia.query({ filter: filtro }).then(function(){
								if(dsCuadraturaCustodia.view().length == 0) {
                                    console.log("no cuadrado")
                                    cont++;


					            }

				           }).then(function(){

								if (cont>0){
                                    if(contPeticion==0){
										contPeticion = contPeticion+1;
										bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("ERROR_NO_SE_HA_GENERADO_EL_PROCESO_DE_CUADRATURA_CUSTODIA"));
									    return;
									}else{
										   contPeticion = 0;
									}
								}else{
									if(contPeticion==0){
									contPeticion = contPeticion+1;
									bcsServiciosNormativa.CircularCrearSolicitud(parametrosSP,successCrearSolicitud,errorCrearSolicitud);	
									}else{
										   contPeticion = 0;
									}
									
								}
				           })
					}
				});
		 }else{
			 bcsServiciosNormativa.CircularCrearSolicitud(parametrosSP,successCrearSolicitud,errorCrearSolicitud);
		 }
		};

		
		var llenarTablaParametros = function(){
              if(params.p_TablaParametrosAux.length >= 8 ){
                	params.p_TablaParametrosAux = []

                	params = {
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

                }

			parametrosSP.p_TablaParametros = params.p_TablaParametrosAux;
			
			var fh = util.dateSPdate(pct.FecTermino);
					
			parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'FTER' , Valor : fh });


			var aleatorio = 1+Math.random()*35;
					const  generateRandomString = (num) => {
						const characters ='ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
						let result1= Math.random().toString(36).substring(0,num);       

						return result1;
					}
            var NameCode = generateRandomString(Math.round(aleatorio));

			var nomarch=  'CIRCULAR1518_' +  util.dateSPdate(pct.FecTermino)+"_"+NameCode;
			parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'NOMSAL' , Valor : nomarch});
					
					
					
				
				
			bcsComunes.msg('info', 'Carga', 'En proceso...');
					
			if(pct.chckDerivados){
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'CLAINS' , Valor : 'DV' });
			}
			if(pct.chckFFMM){
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'CLAINS' , Valor : 'FM' });
			}
			if(pct.chckInstrucciones){
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'CLAINS' , Valor : 'IR' });
			}
			if(pct.chckIF){
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'CLAINS' , Valor : 'IF' });
			}
			if(pct.chckRF){
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'CLAINS' , Valor : 'RF' });
			}
			if(pct.chckRV){
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'CLAINS' , Valor : 'RV' });
			}
			if(pct.chckSpot){
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'CLAINS' , Valor : 'SP' });
			}
		};
		
		var verificarCierreCustodia = function(contador, tablaParametros, filterCus){
			//verificamos cierre
		
			filterCus.push({field :"IdClasificacionTipoInstrumento",operator:"eq" , value : tablaParametros.Valor });//pct.IdClasificacionTipoInstrumento 
				   //se hace la query
                    dsCuadraturaCustodia.query({ filter: filterCus }).then(function(){
						if(dsCuadraturaCustodia.view().length == 0) {
							bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("ERROR_NO_SE_HA_GENERADO_EL_PROCESO_DE_CUADRATURA_CUSTODIA"));
							cont= cont+1;
						}else{
							
						}
					 
					 	
				
              });
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
								
								var tablaParametros = llenarTablaParametrosVisorCircular(fd, fh);
								
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
		
		var llenarTablaParametrosVisorCircular = function(fechaDesde, fechaHasta){
			
			var tablaParametros = [];
			
			tablaParametros.push({TipoParametro : 'IdEmpresa', Valor : $scope.SesionIdEmpresa});
			tablaParametros.push({TipoParametro : 'fechaDesde', Valor : fechaDesde});
			tablaParametros.push({TipoParametro : 'fechaHasta', Valor : fechaHasta});
			
			if (pct.chckDerivados) {
				tablaParametros.push({TipoParametro: 'CLAINS', Valor : 'DV' });
			}
			
			if (pct.chckFFMM) {
				tablaParametros.push({TipoParametro: 'CLAINS', Valor : 'FM' });
			}
			
			if (pct.chckInstrucciones) {
				tablaParametros.push({TipoParametro: 'CLAINS', Valor : 'IR' });
			}
		
			if (pct.chckIF) {
				tablaParametros.push({TipoParametro: 'CLAINS', Valor : 'IF' });
			}
			
			if (pct.chckRF) {
				tablaParametros.push({TipoParametro: 'CLAINS', Valor : 'RF' });
			}
			
			if (pct.chckRV) {
				tablaParametros.push({TipoParametro: 'CLAINS', Valor : 'RV' });
			}
			
			if (pct.chckSpot) {
				tablaParametros.push({TipoParametro: 'CLAINS', Valor : 'SP' });
			}
			
			return tablaParametros;
		};
		
		$scope.pct.clear();
		
	});
}());