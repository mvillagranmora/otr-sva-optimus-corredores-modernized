angular.module('ModuloNormativa').directive('fileReader', function() {
  return {
    scope: {
      fileReader:"="
    },
    link: function(scope, element) {
      $(element).on('change', function(changeEvent) {
        var files = changeEvent.target.files;
        if (files.length) {
          var r = new FileReader();
          r.onload = function(e) {
              var contents = e.target.result;
              scope.$apply(function () {
                scope.fileReader = contents;
              });
          };
          
          r.readAsText(files[0],'iso-8859-1');
        }
      });
    }
  };
});

(function() {
	angular.module('ModuloNormativa').controller('PanelCertificadosCtrl', 
			function($scope,$timeout,Model,Utils,$translate,bcsComunes,$compile,bcsServiciosNormativa,bcsConfiguracionMaestroDeclaraciones,bcsAlerta,OPCIONES_BCSALERTA ){
		
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var pse = this;
		pse.idVentana = bcsComunes.ventanaWinId($scope); //ID del Formulario
		pse.domVentana = $("[ventanaid='" + $scope.idVentana + "']");
		
		
		
		pse.control	= {};
		/*********************************************
		 * VARIABLES PARAMETROS
		 ********************************************/
		
		var TipoRegistro = [];
		pse.Filtros = {};
		
		var p_TablaParametrosAux; //parametros generales que puede ver el jobmanager
		var params = {
				p_TablaParametrosAux : [
				    {	Tipo			: 'BATCH',
				    	NombreParametro : 'idUsuario',
				    	Valor : $scope.SesionIdUsuario
				    }, {Tipo			: 'BATCH',
				    	NombreParametro : 'schema',
				    	Valor : Model.getSchema()
				    }]};
		var ListadoRuts = [];
		var ListadoRutAuts = [];
		var ListadoNemos = [];
		var ListadoRutEmpl = [];
		var parametrosSP = {};
		
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		
		function SplitTxt (csvString, skipFirstLine) {
			var csvArray = [];
			csvString = csvString + '';
			var csvRows = csvString.split(/\n/);
			for(var rowIndex = 0; rowIndex < csvRows.length; ++rowIndex){
				var rowArray = csvRows[rowIndex].split(';');
				
				for (var propIndex = 0; propIndex < rowArray.length; ++propIndex){
					var propValue =  rowArray[propIndex].replace(/^"|"$/g,'') || '';
					csvArray.push(propValue);
				}
			}
			return csvArray;
		};
		
		$scope.pse.clear =  function(){
			pse.FecInicio 		= util.dateInicioDia($scope.SesionFechaSistema);
			pse.FecTermino 		= util.dateInicioDia($scope.SesionFechaSistema);
			
			pse.Filtros.Nemo = '';
			//pse.oConfiguracionSocioNegocio.seleccionado = 0;
			
			
			pse.control.registroRV = true;
			pse.control.registroRF = true;
			pse.control.registroIIF = true;
			pse.control.registroFFMM = true;
			pse.control.registroSPOT= true;
			
		};
		
		pse.cargar = function(){
			
			
	var filterCertificado = [ { field: "IdTipoCertificado",operator: "eq", value:$scope.solCir.IdTipoDocumento },
	                       { field: "IdEstado",  operator: "eq", value: 'ASI' },
	                       { field: "Version",  	  operator: "eq", value: $scope.solCir.version },
	                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa } ];
	pse.ConsultarEstadoVersionCertificado.query({ filter: 	filterCertificado }).then(function(){
		if(pse.ConsultarEstadoVersionCertificado.view().length > 0) {

			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("PROCESO_DE_CERTIFICADOS_EN_EJECUCION"));
			
			
			
			BcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR"),conflictos:util.parseServerResponse(respuesta)},pse.idVentana);
			
			bcsComunes.ventanaCerrar(pse.idVentana);
			return;
			
		}else {
								
			
			var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_CONFIRMA" ) + " "  + $translate.instant("UI_ACTUALIZAR_CERTIFICADOS" ) +" ?",pse.idVentana);
			t.then(function(result)
			      {		
					 if (result===OPCIONES_BCSALERTA.SI)
				       {
			
				bcsComunes.ventanaOverlay($scope.winID,true);
				var parametrosSP = {};
		        parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
		        parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
		        parametrosSP.p_IdModulo =  $scope.solCir.modulo;
		        parametrosSP.p_SimularOperacion = 'N';
		        parametrosSP.p_UsaReglasNegocio = 'S';
		        parametrosSP.p_Version = $scope.solCir.version;
		        parametrosSP.p_Esquema = $scope.solCir.esquema;
		        parametrosSP.p_CodigoPrograma = $scope.solCir.IdTipoDocumento.replace('CERT','GenerarCertificado');
		   	    parametrosSP.p_Todos = 'S';
		   	    parametrosSP.p_Clientes = [];
		   	
		    	bcsServiciosNormativa.CrearProgramaGenerarCertificados(parametrosSP).$promise.then(function(respuesta){
		    		 
		   	     	if (respuesta.P_OUT_ERRORESCONTADOR === 0){
			     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_GENERAR_CERTIFICADO"),conflictos:util.parseServerResponse(respuesta)},pse.idVentana);
		   	     	}else{
			     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_GENERAR_CERTIFICADO"),conflictos:util.parseServerResponse(respuesta)},pse.idVentana);
			     	}
		   	     	bcsComunes.ventanaOverlay(pse.idVentana,false);
			    },function(respuesta){
				  bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_GENERAR_CERTIFICADO"),conflictos:util.parseServerResponse(respuesta)},pse.idVentana);
				  bcsComunes.ventanaOverlay(pse.idVentana,false);
			    });
		    	
		    	
				       };
					});		

					}
							});	

		}
		
		
		
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
				if (respuesta.P_OUT_ERRORESDETALLE[0].Descripcion)
				{
					bcsComunes.msg("error","error",$translate.instant('Solicitud no fue creada') + respuesta.P_OUT_ERRORESDETALLE[0].Descripcion);
				}else{
					if (respuesta.P_OUT_ERRORESDETALLE[0].Codigo)
					{
						bcsComunes.msg("error","error",$translate.instant('Solicitud no fue creada') + respuesta.P_OUT_ERRORESDETALLE[0].Codigo);
					}
					else
					{
						bcsComunes.msg("error","error",$translate.instant('Solicitud no fue creada'));
					};
					
				};		
				var i = 0;
				for(i; i<respuesta.P_OUT_ERRORESCONTADOR; i++)
				{
					bcsComunes.log().info("Error: " + respuesta.P_OUT_ERRORESDETALLE[i].Descripcion);       
				}            
			}		
			bcsComunes.ventanaOverlay(pse.idVentana, false);
			bcsComunes.ventanaData(pse.idVentana, respuesta);
			bcsComunes.ventanaCerrar(pse.idVentana);
			
		};
		
		var errorCrearSolicitud = function(respuesta)
		{	
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
			bcsComunes.msg("error","error",$translate.instant('INGRESO ERROR'));
			bcsComunes.ventanaOverlay(pse.idVentana, false);
		};
		
		function pad (n, length) {
		    var  n = n.toString();
		    while(n.length < length)
		         n = '0' + n;
		return n;
		};
		
		pse.ConsultarEstadoVersionCertificado = new bcs.DataSource({
				type 		: "odata",
				definition 	: {
					serverFiltering	: true,
					transport		: {
						read : {
							url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=4306012",
							dataType : "json",
						}
					},
					schema 				: {
						model: {
							fields : {
							    IdEstado				: {type: "string"},
								IdTipoCertificado		: {type: "string"},
								Version					: {type: "string"},
								Cantidad 				: {type: "string"}	
								
								
											
							}
						}
					}
				},
			});		
		
		
		pse.solicitar = function() {
			
			
			
					var filterCertificado = [ { field: "IdTipoCertificado",operator: "eq", value:$scope.solCir.IdTipoDocumento },
					                       { field: "IdEstado",  operator: "eq", value: 'ASI' },
					                       { field: "Version",  	  operator: "eq", value: $scope.solCir.version },
					                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa } ];
					pse.ConsultarEstadoVersionCertificado.query({ filter: 	filterCertificado }).then(function(){
						if(pse.ConsultarEstadoVersionCertificado.view().length > 0) {

							bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("PROCESO_DE_CERTIFICADOS_EN_EJECUCION"));
							
							
							
							BcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR"),conflictos:util.parseServerResponse(respuesta)},pse.idVentana);
							
							bcsComunes.ventanaCerrar(pse.idVentana);
    	 					return;
							
						}else {
							
							
							var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_CONFIRMA" ) + " "  + $translate.instant("UI_SOLICITAR_GENERACION_DE_PDF" ) +" ?",pse.idVentana);
							t.then(function(result)
					      {		
							 if (result===OPCIONES_BCSALERTA.SI)
						       {
								 
						       
										var filterTemplate = [ { field: "IdTipoDocumento",operator: "eq", value: $scope.solCir.IdTipoDocumento},
							                { field: "TipoArchivo",  operator: "eq", value: "pdf" },
							                { field: "Version",  	  operator: "eq", value: "3" },
							                { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
							                { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
											bcsConfiguracionMaestroDeclaraciones.ConsultaArchivoNormativa.query({ filter: filterTemplate }).then(function(){	
										parametrosSP.p_IdEmpresa				= $scope.SesionIdEmpresa;
										parametrosSP.p_IdUsuario              	=  $scope.SesionIdUsuario;
								    	parametrosSP.p_IdModulo 				=  $scope.solCir.modulo;
								    	//Especificos
								    	parametrosSP.p_FechaInicio 				=  util.dateSPdate(pse.FecInicio);
										parametrosSP.p_FechaTermino 		    =  util.dateSPdate(pse.FecTermino);
										
										//Creacion Documento
										parametrosSP.p_IdTipoDocumento 			= $scope.solCir.IdTipoDocumento;
										//Tarea Programada	
										
										params.p_TablaParametrosAux.push({Tipo: 'BATCH',NombreParametro : 'CodigoPrograma',Valor :  $scope.solCir.IdTipoDocumento.replace('CERT','GenerarCertificado')});
										params.p_TablaParametrosAux.push({Tipo: 'BATCH',NombreParametro : 'IdTipoDocumento',Valor : $scope.solCir.IdTipoDocumento});
										params.p_TablaParametrosAux.push({Tipo: 'BATCH',NombreParametro : 'Version',Valor :  $scope.solCir.version});
										params.p_TablaParametrosAux.push({Tipo: 'BATCH',NombreParametro : 'Esquema',Valor :  $scope.solCir.esquema});
										params.p_TablaParametrosAux.push({Tipo: 'BATCH',NombreParametro : 'NombreReporte',Valor :   bcsConfiguracionMaestroDeclaraciones.ConsultaArchivoNormativa.view()[0].NombreArchivo});
										params.p_TablaParametrosAux.push({Tipo: 'BATCH',NombreParametro : 'IdReporte',Valor :   bcsConfiguracionMaestroDeclaraciones.ConsultaArchivoNormativa.view()[0].IdArchivo});
										params.p_TablaParametrosAux.push({Tipo: 'BATCH',NombreParametro : 'ServiceId',Valor :   bcsConfiguracionMaestroDeclaraciones.ConsultaArchivoNormativa.view()[0].ServicioArchivo});
										
										params.p_TablaParametrosAux.push({Tipo: 'BATCH',NombreParametro : 'IdEmpresa',Valor :   $scope.SesionIdEmpresa});
										params.p_TablaParametrosAux.push({Tipo: 'BATCH',NombreParametro : 'IdUsuario',Valor :   $scope.SesionIdUsuario});
										params.p_TablaParametrosAux.push({Tipo: 'BATCH',NombreParametro : 'IdModulo',Valor :   $scope.solCir.modulo});
										params.p_TablaParametrosAux.push({Tipo: 'BATCH',NombreParametro : 'ListaRut',Valor :   ''});
										params.p_TablaParametrosAux.push({Tipo: 'BATCH',NombreParametro : 'VersionArchivo',Valor :   $scope.solCir.version});
										params.p_TablaParametrosAux.push({Tipo: 'BATCH',NombreParametro : 'IdTipoDocumento',Valor :   $scope.solCir.IdTipoDocumento});
										
												
								   	    
										parametrosSP.p_TablaParametros = params.p_TablaParametrosAux;
										
										if (pse.oConfiguracionSocioNegocio.seleccionado){
											parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'IDCLI' , Valor : pse.oConfiguracionSocioNegocio.seleccionado.IdCliente });					
										};
										
										if (pse.Filtros.Nemo){
											parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'NEMO' , Valor : pse.Filtros.Nemo });					
										};
										
								
										fnObtenerParametrosEspecificos()
										
										bcsComunes.msg('info', 'Carga', 'En proceso...');
										bcsServiciosNormativa.CrearProgramaGenerarCertificadosMasivos(parametrosSP,successCrearSolicitud,errorCrearSolicitud);
										
											});
											
						       };
							});	
				        					
								
								}
							});	
					
						
									
		};
		
		function fnObtenerParametrosEspecificos() {
			
			//OBTENCION DE LISTADO DE RUT
			if(pse.FiltrosRut!=undefined)
			{
				var csvToArray;
				var RutsCli = SplitTxt(pse.FiltrosRut.fileContent,true);
					 
				//Recorremos las filas seleccionadas
				var v_iter ;
				v_iter=1;
				 
				 if(RutsCli.length>0){
					 $.each(RutsCli,function(key,value){
						 
						 parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'RUT_MAS' , Valor : RutsCli[key] }); 
		
						 v_iter=v_iter+1;
					 });
				 }
			
			};
			
			//Obtiene datos de archivo con listado de Nemos
			var ListadoNemos = [];
			if(pse.FiltrosNemo!=undefined)
			{
				var Nemos = SplitTxt(pse.FiltrosNemo.fileContent,true);
					 
				//Recorremos las filas seleccionadas
				var v_iter ;
				v_iter=1;
				 
				if(Nemos.length>0){
					$.each(Nemos,function(key,value){

						parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'NEMO_MAS' , Valor : Nemos[key] });
						
						v_iter=v_iter+1;
					}); 
				};
		
			};
			//OPCION TODOS
			if(pse.control.TodosCli===true){
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'TODOS_CLI' , Valor : 'S' });
			
			}
			
			if(pse.control.TodosNemo===true){
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'TODOS_NEMO' , Valor : 'S' });
			}
			
			//CLASIFICACION DE INSTRUMENTO
			if(	pse.control.registroRV && pse.control.registroRF &&	pse.control.registroIIF &&	pse.control.registroFFMM && pse.control.registroSPOT)
			{
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'TODOS_CLAINS' , Valor : 'S' });
				
			}else{
				if (pse.control.registroRV){parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'CLAINS_MAS' , Valor : 'RV' });};
				if (pse.control.registroRF){parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'CLAINS_MAS' , Valor : 'RF' });};
				if (pse.control.registroIIF){parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'CLAINS_MAS' , Valor : 'IF' });};
				//if (pse.control.registroFFMM){parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'CLAINS_MAS' , Valor : 'FM' });};
				if (pse.control.registroSPOT){parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'CLAINS_MAS' , Valor : 'SP' });};
			};
			
			
			
		}
		
		$scope.$watch('pse.oConfiguracionSocioNegocio.seleccionado',function(newval,oldval){
			if (pse.oConfiguracionSocioNegocio)
			{
				if (pse.oConfiguracionSocioNegocio.seleccionado.IdCliente)
				{
					pse.control.TodosCli=false;
				}else{
					pse.control.TodosCli=true;
				};
			};
				
		});
							
		$scope.$watch('pse.FiltrosRut',function(newval,oldval){
			if (pse.FiltrosRut!=undefined)
			{
				pse.control.TodosCli=false;
			}else{
				pse.control.TodosCli=true;
			};
				
		});
		
		
		$scope.$watch('pse.FiltrosNemo',function(newval,oldval){
			if (pse.FiltrosNemo!=undefined)
			{
				pse.control.TodosNemo=false;
			}else{
				pse.control.TodosNemo=true;
			};
				
		});
		
		$scope.pse.clear();
		
	});
}());