(function() {
	angular.module('ModuloNormativa').controller('PanelUAFDOFCtrl',function($scope,$timeout,Model,Utils,$translate,bcsComunes,$compile,bcsServiciosNormativa, bcsVisorCircularConfig){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var pct = this;
		pct.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		//pct.parametros	= bcsComunes.ventanaParams($scope.idVentana);
		pct.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		pct.cmbbx		= {};
		pct.TipoArchivo			= 'pdf';
	    pct.IdVersionEsquema 	= '';
		pct.IdTipoDocumento = 'UAFDOF';
		//pct.IdTipoDocumentoINFO = 'UAFDOFINFO';
		
		$scope.dataTemplate	= {};
		/*********************************************
		 * COMBOBOX
		 ********************************************/
		pct.opcionFiltro='AM';
				
		
		pct.cmbbx.Agente={
			filtros : {
				CodigoTipoCargoEmpleado : { field: "CodigoTipoCargoEmpleado", operator: "eq", value: "AGE" }
			}
		};
		
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

		
				//Funcion Obtener version del Esquema
		pct.fnVersionEsquema = function() {
			if (pct.FecInicio !=undefined)
				{
						var fd = util.dateSPdate(pct.FecInicio);
						var paramServicio = {};
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
								pct.IdVersionEsquema=respuesta.P_OUT_VERSIONESQUEMA[0].P_OUT_VERSION;
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
		
		
		
		pct.imprimirCircular = function() {
					if(pct.HabFecTermino===false){
						pct.FecTermino=pct.FecInicio;
					};
			
			var fd = util.dateSPdate(pct.FecInicio);	
			var fh = util.dateSPdate(pct.FecTermino);		
			if (fh < fd){
	 			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("FECHA_TERMINO_RANGO_CONSULTA_INVALIDA"));
	 			return;
	 		};
			// Obtener  version del tipo documento
					var filterTemplate = [ { field: "IdTipoDocumento",operator: "eq", value: pct.IdTipoDocumento },
					                       { field: "TipoArchivo",  operator: "eq", value: pct.TipoArchivo },
					                       { field: "Version",  	  operator: "eq", value: pct.IdVersionEsquema },
					                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
					                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
					$scope.solCir.ConsultaArchivoNormativa.query({ filter: filterTemplate }).then(function(){
						if($scope.solCir.ConsultaArchivoNormativa.view().length > 0) {
							$scope.dataTemplate.TipoReporte		= $scope.solCir.ConsultaArchivoNormativa.view()[0].TipoArchivo;
							$scope.dataTemplate.IdReporte		= $scope.solCir.ConsultaArchivoNormativa.view()[0].IdArchivo;
							$scope.dataTemplate.NombreReporte	= $scope.solCir.ConsultaArchivoNormativa.view()[0].NombreArchivo;
							
							//PARAMETROS
							var parametros =  { "p_IdEmpresa"				: $scope.$root.SesionIdEmpresa,
												"p_FechaOperacionDesde"		: util.dateSPdate(pct.FecInicio),
												"p_FechaOperacionHasta"		: util.dateSPdate(pct.FecTermino),	
												"p_Monto"					: 0,
												"p_IdSucursal"				: pct.IdSucursal,
												"p_IdEmpleado"				: pct.IdSucursal,
					                           "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
  											  };
							//'Carta1Art171BCS.rpt'
							//DATA 
							$scope.dataTemplate.data= {  "serviceId"   :	$scope.solCir.ConsultaArchivoNormativa.view()[0].ServicioArchivo
  													   , "serviceData" :	parametros
													  };
							var subrep = new Array();
							$scope.dataTemplate.subReports=subrep;							
							
							$scope.solCir.getReport($scope.dataTemplate);
							
										
						}
					});
				
				
		 };		
		 pct.solicitar = function() {
	    	var parametrosSP = {};
	    	//Comunes
	    	parametrosSP.p_IdEmpresa 				=  $scope.SesionIdEmpresa;
	    	parametrosSP.p_IdUsuario              	=  $scope.SesionIdUsuario;
	    	parametrosSP.p_IdModulo 				=  $scope.solCir.modulo;
			
		
			//Especificos
			parametrosSP.p_FechaInicio 				=  util.dateSPdate(pct.FecInicio);
			parametrosSP.p_FechaTermino 		    =  util.dateSPdate(pct.FecTermino);
			
			parametrosSP.p_IdTipoDocumento 			= pct.IdTipoDocumento;
			
			
					
			
			var fd = util.dateSPdate(pct.FecInicio);
			params.p_TablaParametrosAux.push({Tipo: 'BATCH',NombreParametro : 'fechaDesde',Valor : fd});
			
			var fh = util.dateSPdate(pct.FecTermino);
			params.p_TablaParametrosAux.push({Tipo: 'BATCH',NombreParametro : 'fechaHasta',Valor : fh});
			
			parametrosSP.p_TablaParametros = params.p_TablaParametrosAux;
			
			
			parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'FINI' , Valor : fd });
			parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'FTER' , Valor : fh });	
			//parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'NOMSAL' , Valor : 'arc'});
			if (pct.IdAgente  !== 0) {parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'IDEMPLEADO' , Valor :  pct.IdAgente });};
			if (pct.IdSucursal!== 0) {parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'IDSUCURSAL' , Valor :  pct.IdSucursal }) ;};
		
			
			if (fh < fd){
	 			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("FECHA_TERMINO_RANGO_CONSULTA_INVALIDA"));
	 			return;
	 		};
	 		
			bcsComunes.msg('info', 'Carga', 'En proceso...');
			bcsServiciosNormativa.CircularCrearSolicitud(parametrosSP,successCrearSolicitud,errorCrearSolicitud);
			
	      
		};
		
		pct.visorCircular = function() {
			
			var paramServicio = {};
			var fd = util.dateSPdate(pct.FecInicio);
			var fh = util.dateSPdate(pct.FecTermino);
			
			if (fh < fd){
	 			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("FECHA_TERMINO_RANGO_CONSULTA_INVALIDA"));
	 			return;
	 		};			
			
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
								//tablaParametros.push({TipoParametro : 'TIPINF',Valor :  pct.opcionFiltro});
								tablaParametros.push({TipoParametro : 'fechaDesde',Valor : fd});
								tablaParametros.push({TipoParametro : 'fechaHasta',Valor : fh});
								
								if (pct.IdSucursal!== 0) {
									tablaParametros.push({TipoParametro : 'IdSucursal',Valor :  pct.IdSucursal});
								};
								
								if (pct.IdAgente!== 0) {
									tablaParametros.push({TipoParametro : 'IdAgente',Valor :  pct.IdAgente});
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
		$scope.$watch('pct.FecInicio', function(newval,oldval){
			pct.fnVersionEsquema();
		},true);
		
		$scope.pct.clear();
		
	});
}());