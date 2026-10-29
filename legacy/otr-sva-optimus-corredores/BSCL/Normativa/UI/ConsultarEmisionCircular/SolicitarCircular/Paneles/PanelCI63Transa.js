(function() {
	angular.module('ModuloNormativa').controller('PanelCI63TransaCtrl',function($scope,$timeout,Model,Utils,$translate,bcsComunes,$compile,bcsServiciosNormativa, bcsVisorCircularConfig){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var pct = this;
		pct.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		//pct.parametros	= bcsComunes.ventanaParams($scope.idVentana);
		pct.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		pct.IdTipoDocumento = 'CI63Transa';
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
		p_TablaParametrosaux : [ //{NombreParametro : 'IdSolicitudCircular',Valor : IdSolicitudCircular}
		// ,{NombreParametro : 'idDocumento',Valor : IdDocumento},
		{
			NombreParametro : 'idUsuario',
			Valor : $scope.SesionIdUsuario
		},{
			NombreParametro : 'schema',
			Valor :  Model.getSchema()
		}, {
			NombreParametro : 'grabar',
			Valor : $scope.solCir.grabar
		}, {
			NombreParametro : 'reemplazar',
			Valor : $scope.solCir.reemplazar
		},{
			NombreParametro : 'separadorListas',
			Valor : $scope.solCir.separadorListas
		},{
			NombreParametro : 'simboloDecimal',
			Valor : $scope.solCir.simboloDecimal
		},{
			NombreParametro : 'simboloFecha',
			Valor : $scope.solCir.simboloFecha
		},{
			NombreParametro : 'aceptarFechasNull',
			Valor : $scope.solCir.aceptarFechasNull
		},]};
		
		var ListadoRuts = [];
		var ListadoRutAuts = [];
		var ListadoNemos = [];
		var ListadoRutEmpl = [];
		
		
		
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
	    	var parametrosSP = {};
	    	//Comunes
	    	parametrosSP.p_IdEmpresa 				=  $scope.SesionIdEmpresa;
	    	parametrosSP.p_IdUsuario              	=  $scope.SesionIdUsuario;
	    	parametrosSP.p_IdModulo 				=  $scope.solCir.modulo;
			parametrosSP.p_FechaIngresoSolicitud 	=  $scope.solCir.fechaInicioSol
		
			//Especificos
			parametrosSP.p_FechaInicio 				=  util.dateSPdate(pct.FecInicio);
			parametrosSP.p_FechaTermino 		    =  util.dateSPdate(pct.FecTermino);
			parametrosSP.p_TipoInformacion 			=  pct.opcionFiltro;
			if (pct.IdClasificacionTipoInstrumento!== 0) {parametrosSP.p_IdClasificacionTipoInstrumento =  pct.IdClasificacionTipoInstrumento;};
			
			
			
			//Tipo Listas 
			parametrosSP.p_ListaRut 				= ListadoRuts 		|| []; 
			parametrosSP.p_ListaRutAut			 	= ListadoRutAuts 	|| [];
			parametrosSP.p_ListaNemo 				= ListadoNemos 		|| [];
			parametrosSP.p_ListaRutEmpl 			= ListadoRutEmpl 	|| [];
			
			//Creacion Documento
			parametrosSP.p_IdTipoDocumento 			= pct.IdTipoDocumento;
			parametrosSP.p_IdEstadoDocumento 		= $scope.solCir.IdEstadoDocumento;

			//Tarea Programada			
			parametrosSP.p_CreaParam 				= 'S';
			
			var fd = util.dateSPdate(pct.FecInicio);
			params.p_TablaParametrosaux.push({NombreParametro : 'fechaDesde',Valor : fd});
			
			var fh = util.dateSPdate(pct.FecTermino);
			params.p_TablaParametrosaux.push({NombreParametro : 'fechaHasta',Valor : fh});
			
			parametrosSP.p_TablaParametros = params.p_TablaParametrosaux;
			
			bcsComunes.msg('info', 'Carga', 'En proceso...');
			bcsServiciosNormativa.CrearSolicitudCircular(parametrosSP,successCrearSolicitud,errorCrearSolicitud);
			
	      
		};
		
		pct.visorCircular = function() {
			
			var paramServicio = {};
			var fd = util.dateSPdate(pct.FecInicio);
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
								tablaParametros.push({TipoParametro : 'TIPINF',Valor :  pct.opcionFiltro});
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