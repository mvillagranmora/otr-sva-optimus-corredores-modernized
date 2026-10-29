(function() {
	angular.module('ModuloNormativa').controller('PanelCarta2SVSCtrl',function($scope,$timeout,Model,Utils,$translate,bcsComunes,$compile,bcsServiciosNormativa, bcsVisorCircularConfig){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var pc2 = this;
		pc2.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		
		pc2.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		
		pc2.TipoArchivo			= 'pdf';
	    pc2.IdVersionEsquema 	= '';
		pc2.IdTipoDocumento = 'ART171Car2';
		$scope.dataTemplate	= {};
		/*********************************************
		 * COMBOBOX
		 ********************************************/
		
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
		
		pc2.FecInicioOptions = {format: 'MMMM yyyy', start: 'year',depth: 'year'};
		
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		$scope.pc2.clear = function(){
			//$scope.formData = {};
			//	$scope.formData.tipoSocioNegocio = 0;
			//	$scope.formData.tipoDocumentoIdentidad = 0;
			pc2.FecInicio 	 = util.dateInicioDia($scope.SesionFechaSistema);
			pc2.FecTermino 	 = util.dateInicioDia($scope.SesionFechaSistema);
			
		};
		
		$scope.pc2.envioFecha = function(){
			var date = '';
			var dateFin = new Date();
			dateFin 	 = pc2.FecInicio;
			date= util.dateSPdate(pc2.FecInicio);
			pc2.FecInicio= util.dateInicioDia(date.substring(0,8) + '01');
			var ultimoDia = new Date(dateFin.getFullYear(), dateFin.getMonth() + 1, 0);
			pc2.FecTermino= util.dateInicioDia(ultimoDia);
			//pc2.FecTermino= util.dateInicioDia(util.dateInicioDia(date.substring(0,8) + ultimoDia.getDate()));
 
			
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
			bcsComunes.ventanaOverlay(pc2.idVentana, false);
			bcsComunes.ventanaData(pc2.idVentana, respuesta);
			bcsComunes.ventanaCerrar(pc2.idVentana);
			
		};
		
		var errorCrearSolicitud = function(respuesta)
		{	
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
			bcsComunes.msg("error","error",$translate.instant('INGRESO ERROR'));
			bcsComunes.ventanaOverlay(pc2.idVentana, false);
		};
		
		//Funcion Obtener version del Esquema
		pc2.fnVersionEsquema = function() {
			if (pc2.FecInicio !=undefined)
				{
						var fd = util.dateSPdate(pc2.FecInicio);
						var paramServicio = {};
						paramServicio.p_TipoDocumento = pc2.IdTipoDocumento;
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
								pc2.IdVersionEsquema=respuesta.P_OUT_VERSIONESQUEMA[0].P_OUT_VERSION;
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
		
		$scope.imprimirCircular = function() {
			//srt v=pc2.FecInicio.getMonth();
			$scope.pc2.envioFecha();
					
					var filterTemplate = [ { field: "IdTipoDocumento",operator: "eq", value: pc2.IdTipoDocumento },
					                       { field: "TipoArchivo",  operator: "eq", value: pc2.TipoArchivo },
					                       { field: "Version",  	  operator: "eq", value: pc2.IdVersionEsquema },
					                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
					                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
					$scope.solCir.ConsultaArchivoNormativa.query({ filter: filterTemplate }).then(function(){
						if($scope.solCir.ConsultaArchivoNormativa.view().length > 0) {
							$scope.dataTemplate.TipoReporte		= $scope.solCir.ConsultaArchivoNormativa.view()[0].TipoArchivo;
							$scope.dataTemplate.IdReporte		= $scope.solCir.ConsultaArchivoNormativa.view()[0].IdArchivo;
							$scope.dataTemplate.NombreReporte	= $scope.solCir.ConsultaArchivoNormativa.view()[0].NombreArchivo;
							
							//PARAMETROS
							var parametros =  { "p_IdEmpresa"				: $scope.$root.SesionIdEmpresa,
												"p_FechaOperacionDesde"		: util.dateSPdate(pc2.FecInicio) ,
												"p_FechaOperacionHasta"		: util.dateSPdate(pc2.FecTermino),												
												
					                           "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
  											  };
							//'Carta1Art171BCS.rpt'
							//DATA 
							$scope.dataTemplate.data= {  "serviceId"   :	$scope.solCir.ConsultaArchivoNormativa.view()[0].ServicioArchivo
  													   , "serviceData" :	parametros
													  };
							var subrep = new Array();
							$scope.dataTemplate.subReports=subrep;									
							
							
							
							//$scope.dataTemplate.dataSource	= $scope.dataTemplate.ServicioReporte+"(p_IdEmpresa =>"+$scope.$root.SesionIdEmpresa+",p_FechaOperacionDesde =>'" + util.dateSPdate(pc2.FecInicio) + "',p_FechaOperacionHasta =>'" +  util.dateSPdate(pc2.FecTermino) + "',p_out_TablaCarta2 => ?)";
							$scope.solCir.getReport($scope.dataTemplate);
						}
					});
				
			};		
		
		 pc2.solicitar = function() {
	    	var parametrosSP = {};
	    	//Comunes
	    	parametrosSP.p_IdEmpresa 				=  $scope.SesionIdEmpresa;
	    	parametrosSP.p_IdUsuario              	=  $scope.SesionIdUsuario;
	    	parametrosSP.p_IdModulo 				=  $scope.solCir.modulo;
			parametrosSP.p_FechaIngresoSolicitud 	=  $scope.solCir.fechaInicioSol
						
			//Especificos
			$scope.pc2.envioFecha();
			parametrosSP.p_FechaInicio 				=  util.dateSPdate(pc2.FecInicio);
			parametrosSP.p_FechaTermino 		    =  util.dateSPdate(pc2.FecTermino);
			
			
			//Tipo Listas 
			parametrosSP.p_ListaRut 				= ListadoRuts 		|| []; 
			parametrosSP.p_ListaRutAut			 	= ListadoRutAuts 	|| [];
			parametrosSP.p_ListaNemo 				= ListadoNemos 		|| [];
			parametrosSP.p_ListaRutEmpl 			= ListadoRutEmpl 	|| [];
			
			//Creacion Documento
			parametrosSP.p_IdTipoDocumento 			= pc2.IdTipoDocumento;
			parametrosSP.p_IdEstadoDocumento 		= $scope.solCir.IdEstadoDocumento;

			//Tarea Programada			
			parametrosSP.p_CreaParam 				= 'S';
			
			var fd = util.dateSPdate(pc2.FecInicio);
			params.p_TablaParametrosaux.push({NombreParametro : 'fechaDesde',Valor : fd});
			
			var fh = util.dateSPdate(pc2.FecTermino);
			params.p_TablaParametrosaux.push({NombreParametro : 'fechaHasta',Valor : fh});
			
			parametrosSP.p_TablaParametros = params.p_TablaParametrosaux;
			
			bcsComunes.msg('info', 'Carga', 'En proceso...');
			bcsServiciosNormativa.CrearSolicitudCircular(parametrosSP,successCrearSolicitud,errorCrearSolicitud);
			
	      
		};
		
		pc2.visorCircular = function() {
			$scope.pc2.envioFecha();
			var fd = util.dateSPdate(pc2.FecInicio);
			var fh = util.dateSPdate(pc2.FecTermino);
			
			var paramServicio = {};
			paramServicio.p_TipoDocumento = pc2.IdTipoDocumento;
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
					bcsVisorCircularConfig.getDataSource(respuesta.P_OUT_VERSIONESQUEMA[0].P_OUT_ESQUEMA , respuesta.P_OUT_VERSIONESQUEMA[0].P_OUT_VERSION, pc2.IdTipoDocumento, 
							function (){
								var tablaParametros = [];
								
								tablaParametros.push({TipoParametro : 'IdEmpresa',Valor : $scope.SesionIdEmpresa});
								tablaParametros.push({TipoParametro : 'fechaDesde',Valor : fd});
								tablaParametros.push({TipoParametro : 'fechaHasta',Valor : fh});
								
								bcsComunes.ventanaAbrir("VisorCircular", {
								tablaParametros : tablaParametros,
								IdTipoDocumento : pc2.IdTipoDocumento
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
		}
		$scope.$watch('pc2.FecInicio', function(newval,oldval){
			pc2.fnVersionEsquema();
		},true);
			
		
		$scope.pc2.clear();
		
	});
}());