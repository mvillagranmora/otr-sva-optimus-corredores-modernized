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
	angular.module('ModuloNormativa').controller('PanelNCG380Ctrl',function($scope,$timeout,Model,Utils,$translate,FileUpload,bcsAlerta,OPCIONES_BCSALERTA,bcsComunes,$compile,bcsServiciosNormativa,bcsBlotterEmisionCircular){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var pcn = this;
		
		
		pcn.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		var parametros = bcsComunes.ventanaParams(pcn.idVentana);
		$scope.SolicitudCircular = $.extend({}, parametros.SolicitudCircular);
		$scope.ParametrosSolicitud = $.extend({}, parametros.ParametrosSolicitud);
		pcn.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
	
		var vTipoRegistro	= [];
		var parametrosSP 	= {};
		
	    //seteo base de filtros 380 
	
		//INICIO seteo incial de filtros habilitados
		//$scope.seleccionCli=false;
		$scope.activadoEmpl =false;
		$scope.activadoCli =true;
		$scope.activadoAut =false;
		$scope.seleccionFecInicio=false;
		$scope.seleccionFecFin=false;
		$scope.seleccionFechaRealizacion=false;
		$scope.seleccionNemo=true;
		$scope.seleccionMcdo=false;
		$scope.seleccionOficio=true;
			
		$scope.activadoRel = false;


		$scope.listRut = false;
		$scope.listRutAut = true;
		$scope.listNemo = true;
		$scope.listRutEmpl = true;

		$scope.seleccionTodosRut = false;
		$scope.seleccionTodosNemo = true;
		$scope.seleccionTodosEmpl = true;

		$scope.seleccionTodosAut = true;
		$scope.seleccionTodosRel = true;


		$scope.pcn.registroCliente = false; 
		$scope.pcn.registroAutorizado = false; 
		$scope.pcn.registroOrdenes = false; 
		$scope.pcn.registroOperaciones = false; 
		$scope.pcn.registroAsignacion = false; 
		$scope.pcn.registroFacturacion = false; 
		$scope.pcn.registroCustodia = false; 
		$scope.pcn.registroTrabajador = false; 
		$scope.pcn.registroInstrucciones = false; 
		$scope.pcn.registroRepactaciones = false;
		$scope.pcn.registroDeudores = false;
		$scope.pcn.registroIntBursatil = false;
		//FIN seteo incial de filtros habilitados 

		
		
		/*********************************************
		 * COMBOBOX
		 ********************************************/
		$scope.data ={
		oCircularConfiguracion : {},
		Filtros: {},
		//FiltrosRut: {},
		//FiltrosRutAut : {},
		//FiltrosRutEmpl : {},
		//FiltrosNemo: {},
		modoLectura:false
		};
		
		$scope.data.oConfiguracionSocioNegocio={
				tipo : "combobox-socio-negocio",
				configuracion :{},
				filtros: {
				EsCliente             : {"field"    : "EsCliente",         "operator": "eq", "value"   :  'UI_SI'}				
				},			 
				idActual : false,
				seleccionado : false,
				idDefecto : ''
		};
		
		$scope.data.oConfiguracionEmpleado={
				tipo : "combobox-socio-negocio",
				configuracion :{},
				filtros: {
				EsEmpleado             : {"field"    : "EsEmpleado",         "operator": "eq", "value"   :  'UI_SI'}				
				},			 
				idActual : false,
				seleccionado : false,
				idDefecto : ''
		};

		
		pcn.oConfiguracionRelaciones={
				tipo : "combobox-relacion-normativa",
				//configuracion :{},
				filtros: {
					IdNormativa :  {field    : "IdNormativa",   operator: "eq", value : 'NCG380'}
			    },			 
				idActual : false,
				seleccionado : false,
				idDefecto : ''
		};
		
		
		
		/**
		 * GRILLAS PARA MODO VISUALIZACION.
		 */

		$scope.confBasicaListadoRut= {
				dataSource			: bcsBlotterEmisionCircular.dataSourceParametrosSolicitudCircular(),
				columns				: bcsBlotterEmisionCircular.columnasParametrosRut(),		
				sortable			: true,
				pageable			: true,
				height				: 200, 
				selectable 			: "row",
				filterable			: true,
				autoBind			: false,
				gridCreated			: function(grid) {
											if (parametros.SolicitudCircular) 
												filtrosRut();
										}
			};
		
		$scope.confBasicaListadoNemo= {
				dataSource			: bcsBlotterEmisionCircular.dataSourceParametrosSolicitudCircular(),
				columns				: bcsBlotterEmisionCircular.columnasParametrosNemo(),		
				sortable			: true,
				pageable			: true,
				height				: 200, 
				selectable 			: "row",
				filterable			: true,
				autoBind			: false,
				gridCreated			: function(grid) {
											if (parametros.SolicitudCircular) 
													filtrosNemo();
										}
			};

				
		/*********************************************
		 * VARIABLES PARAMETROS
		 ********************************************/
		
		var p_TablaParametrosaux ; //parametros generales que puede ver el jobmanager
		var params = {
		p_TablaParametrosaux : [ //{NombreParametro : 'IdSolicitudCircular',Valor : IdSolicitudCircular}
 		// ,{NombreParametro : 'idDocumento',Valor : IdDocumento},
 		{	Tipo			: 'BATCH',
 			NombreParametro : 'idUsuario',
 			Valor : $scope.SesionIdUsuario
 		},{ Tipo			: 'BATCH',
 			NombreParametro : 'schema',
 			Valor :  Model.getSchema()
 		}, {Tipo			: 'BATCH',
 			NombreParametro : 'grabar',
 			Valor : $scope.solCir.grabar
 		}, {Tipo			: 'BATCH',
 			NombreParametro : 'reemplazar',
 			Valor : $scope.solCir.reemplazar
 		},{ Tipo			: 'BATCH',
 			NombreParametro : 'separadorListas',
 			Valor : $scope.solCir.separadorListas
 		},{ Tipo			: 'BATCH',
 			NombreParametro : 'simboloDecimal',
 			Valor : $scope.solCir.simboloDecimal
 		},{	Tipo			: 'BATCH',
 			NombreParametro : 'simboloFecha',
 			Valor : $scope.solCir.simboloFecha
 		},{	Tipo			: 'BATCH',
 			NombreParametro : 'aceptarFechasNull',
 			Valor : $scope.solCir.aceptarFechasNull
	},]};

		var ListadoRuts 	= [];
		var ListadoRutAuts 	= [];
		var ListadoNemos 	= [];
		var ListadoRutEmpl 	= [];
		
		
		
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		 
		 
		//LECTURA DE ARCHIVO//

	
		/*var csvToArray = function (csvString,skipFirstLine){

			var csvArray   = [];

			var csvRows    = csvString.split(/\n/);

			for(var rowIndex = 0; rowIndex < csvRows.length; ++rowIndex){
		
				var rowArray  = csvRows[rowIndex].split(';');		

			for(var propIndex = 0; propIndex < rowArray.length; ++propIndex){
				
					var propValue =   rowArray[propIndex].replace(/^"|"$/g,'') || '';

				csvArray.push(propValue);
				}
		
			}
		return csvArray;
		};*/
		function SplitTxt(csvString,skipFirstLine) {
		
			var csvArray   = [];
			csvString =	csvString + '';
			var csvRows    = csvString.split(/\n/);

			for(var rowIndex = 0; rowIndex < csvRows.length; ++rowIndex){
		
				var rowArray  = csvRows[rowIndex].split(';');		

			for(var propIndex = 0; propIndex < rowArray.length; ++propIndex){
				
					var propValue =   rowArray[propIndex].replace(/^"|"$/g,'') || '';

				csvArray.push(propValue);
				}
		
			}
		return csvArray;
					
		};

		
		

		$scope.pcn.clear = function(){
			//$scope.formData = {};
			//	$scope.formData.tipoSocioNegocio = 0;
			//	$scope.formData.tipoDocumentoIdentidad = 0;
			pcn.FecInicio 	 = util.dateInicioDia($scope.SesionFechaSistema);
			pcn.FecTermino 	 = util.dateInicioDia($scope.SesionFechaSistema);
			pcn.FechaRealizacion = util.dateInicioDia($scope.SesionFechaSistema);
			$scope.pcn.registroCliente = false;
			$scope.pcn.registroAutorizados= false;
			$scope.pcn.registroOrdenes= false;
			$scope.pcn.registroAsignacion= false;
			$scope.pcn.registroOperaciones= false;
			$scope.pcn.registroFacturacion= false;
			$scope.pcn.registroCustodia= false;
			$scope.pcn.registroTrabajador= false;
			$scope.pcn.registroInstrucciones= false;
			$scope.pcn.registroRepactaciones= false;
			$scope.pcn.registroDeudores= false;
			$scope.pcn.registroIntBursatil= false;
			
			$scope.data.Filtros.Nemo = '';
			$scope.data.Filtros.Mercados = '';
			$scope.data.Filtros.RutTrabajador = '';
			$scope.data.Filtros.Oficio = '';
			$scope.data.oConfiguracionSocioNegocio.seleccionado =0;
					
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
			bcsComunes.ventanaOverlay(pcn.idVentana, false);
			bcsComunes.ventanaData(pcn.idVentana, respuesta);
			bcsComunes.ventanaCerrar(pcn.idVentana);
			
		};
		
		var errorCrearSolicitud = function(respuesta)
		{	
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
			bcsComunes.msg("error","error",$translate.instant('INGRESO ERROR'));
			bcsComunes.ventanaOverlay(pcn.idVentana, false);
		};

		 
		 pcn.solicitar = function() {
	    	
			//Comunes
	    	parametrosSP.p_IdEmpresa 				=  $scope.SesionIdEmpresa;
	    	parametrosSP.p_IdUsuario              	=  $scope.SesionIdUsuario;
	    	parametrosSP.p_IdModulo 				=  $scope.solCir.modulo;
		//	parametrosSP.p_FechaIngresoSolicitud 	=  $scope.solCir.fechaInicioSol

			fnOntenerTipoRegistro();

			
			
      	    $.each(vTipoRegistro,function(key,value){
      	    	bcsComunes.msg('info', 'Carga', 'En proceso...');
				
			//	fnOntenerParametrosComunes();
				
				//Creacion Documento
      			parametrosSP.p_IdTipoDocumento =  vTipoRegistro[key].IdTipoDocumento;
      			//parametrosSP.p_Esquema =  vTipoRegistro[key].Esquema;
      			
    			parametrosSP.p_TablaParametros = params.p_TablaParametrosaux;

	      	fnOntenerParametrosEspecificos(parametrosSP.p_IdTipoDocumento);
	      		
			if(($scope.pcn.registroCliente === true 
					|| $scope.pcn.registroAutorizado === true 
					|| $scope.pcn.registroOrdenes === true 
					|| $scope.pcn.registroOperaciones === true 
					|| $scope.pcn.registroAsignacion === true 
					|| $scope.pcn.registroFacturacion === true
					|| $scope.pcn.registroCustodia === true
					|| $scope.pcn.registroTrabajador === true
					|| $scope.pcn.registroInstrucciones === true
					|| $scope.pcn.registroRepactaciones === true
					|| $scope.pcn.registroDeudores === true
					|| $scope.pcn.registroIntBursatil === true) &&
					 ($scope.pcn.FecInicio != undefined))
			{
	      		
				//especificos
				
    			parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'FINI' , Valor : util.dateSPdate(pcn.FecInicio) });
    			parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'FTER' , Valor : util.dateSPdate(pcn.FecTermino) });
    			parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'FREA' , Valor : util.dateSPdate(pcn.FechaRealizacion) });

    			parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'FEDES' , Valor : util.dateSPdate(pcn.FechaRealizacion) });
    			parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'FEHAS' , Valor : util.dateSPdate(pcn.FechaRealizacion) });

//encerrar esto dentro de un if x tipo doc
    			
    			if($scope.data.Filtros.TodosCli===true){
				
	    		parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'TODOS_CLI' , Valor : '1' });

				}else{
		    	
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'TODOS_CLI' , Valor : '0' });
				}
				
				if($scope.data.Filtros.TodosNemo===true){
		    		parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'TODOS_NEMO' , Valor : '1' });
				}else{
		    		parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'TODOS_NEMO' , Valor : '0' });
				}
				
				if($scope.data.Filtros.TodosAut===true){
		    		parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'TODOS_AUT' , Valor : '1' });
				}else{
		    		parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'TODOS_AUT' , Valor : '0' });
				}
				
				if($scope.data.Filtros.TodosEmpl===true){
		    		parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'TODOS_EMP' , Valor : '1' });
				}else{
		    		parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'TODOS_EMP' , Valor : '0' });
				}
				
				if($scope.data.Filtros.TodosRel===true){
		    		parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'TODOS_REL' , Valor : '1' });
				}else{
		    		parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'TODOS_REL' , Valor : '0' });
				}
				
				
	    		parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'IDCLI' , Valor : $scope.pcn.oConfiguracionSocioNegocio.seleccionado.IdCliente || '0' });
	    		
	    		if($scope.data.Filtros.Nemo !=undefined && $scope.data.Filtros.Nemo != '')
	    		{
	    			
		    		parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'NEMO' , Valor : $scope.data.Filtros.Nemo });

	    		}
	    		
	    		if($scope.filtros.clasificacionTipoInstrumento.IdClasificacionTipoInstrumento !=undefined && $scope.filtros.clasificacionTipoInstrumento.IdClasificacionTipoInstrumento  != '')
	    		{
	    		parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'CLAINS' , Valor : $scope.filtros.clasificacionTipoInstrumento.IdClasificacionTipoInstrumento || '' });
	    		}
	    		
	    		
	    		parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'OBS' , Valor : '' });
	    		
	    		if($scope.data.oConfiguracionEmpleado.seleccionado.DocumentoIdentidadDefecto !=undefined && $scope.data.oConfiguracionEmpleado.seleccionado.DocumentoIdentidadDefecto != '')
	    		{
	    		parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'RUTEMPL' , Valor : $scope.data.oConfiguracionEmpleado.seleccionado.DocumentoIdentidadDefecto || ''});
	    		}
	    		
	    		if($scope.data.oConfiguracionAutorizado.seleccionado.DocumentoIdentidadDefecto !=undefined && $scope.data.oConfiguracionAutorizado.seleccionado.DocumentoIdentidadDefecto != '')
	    		{
	    		parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'RUTAUT' , Valor : $scope.data.oConfiguracionAutorizado.seleccionado.DocumentoIdentidadDefecto || '' });
	    		}
	    		
	    		parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'OFI' , Valor : $scope.data.Filtros.Oficio });

	    		
	    		if(pcn.oConfiguracionRelaciones.seleccionado.IdTipoRelacion !=undefined && pcn.oConfiguracionRelaciones.seleccionado.IdTipoRelacion !='')
	    		{
					
	    		parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'RELA' , Valor : pcn.oConfiguracionRelaciones.seleccionado.IdTipoRelacion.toString()|| '' });
				
				}
	    		 

	    		
	    		
	    		bcsServiciosNormativa.CircularCrearSolicitud(parametrosSP,successCrearSolicitud,errorCrearSolicitud);
      	    	//bcsServiciosNormativa.CrearSolicitudCircular(parametrosSP,successCrearSolicitud,errorCrearSolicitud);
			}
			else
			{
	
				bcsComunes.msg("info","info",'Debe seleccionar al menos 1 tipo registro');
				
			}//fin if condicion seleccion registros
			      	    	
			      	    	
      	    	
      	    	fnLimpiarParametros();
      	    });
			
			
	      
		};
		
		//Funcion que permite llenar los parametros comunes del servicio
		function fnOntenerParametrosComunes() {
			
		  params = {
						p_TablaParametrosaux : [ 
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
			
				 	//Comunes
		          //parametrosSP.p_IdEmpresa 				=  $scope.SesionIdEmpresa;
				  //parametrosSP.p_IdUsuario              	=  $scope.SesionIdUsuario;
			      //parametrosSP.p_IdModulo 				=  $scope.solCir.modulo;
				  //parametrosSP.p_FechaIngresoSolicitud 	=  $scope.solCir.fechaInicioSol
					//parametrosSP.p_IdTipoCircular         	=  $scope.solCir.cmbbx.IdNormativa;// pcn.IdTipoCircular;
					
					//Creacion Documento
					//parametrosSP.p_IdTipoDocumento 		= pcn.IdTipoDocumento;
					//parametrosSP.p_Esquema 				= pcn.Esquema;
					//parametrosSP.p_Version 					= util.dateInicioDia($scope.SesionFechaSistema);//evaluar en el SP
					//parametrosSP.p_Version 					= parametrosSP.p_Version.getFullYear();
					parametrosSP.p_IdEstadoDocumento 		= $scope.solCir.IdEstadoDocumento;
		
					//Tarea Programada			
					//parametrosSP.p_IdTipoTarea 				= pcn.IdTipoTarea;
					parametrosSP.p_CreaParam 				= 'S';
					
					
					parametrosSP.p_FechaCreacion 			= $scope.SesionFechaSistema;
					parametrosSP.p_FechaPresentacion 		= $scope.SesionFechaSistema;
					parametrosSP.p_IdAgenda 				= 0;
					parametrosSP.p_Segundos 				= 0;
					
					parametrosSP.p_Oficio 					= $scope.data.Filtros.Oficio;
			
		};
		//Limpiar variables de parametros
		function fnLimpiarParametros() {
			parametrosSP			=	{};
			p_TablaParametrosaux	=	[];
			params 					= 	{};
			ListadoRuts 			= 	[];
		 	ListadoRutAuts			= 	[];
		 	ListadoNemos 			= 	[];
		 	ListadoRutEmpl 			= 	[];
	};
		//Funcion que permite llenar los parametros espcificos del servicio segun Tipo Documento
		function fnOntenerParametrosEspecificos(TipoDocumento) {
			
			
			//OBTENCION DE LISTADO DE RUT, RUTAUT,RUTEMPL,NEMO
			
			var ListadoRuts = [];
			if($scope.data.FiltrosRut!=undefined)
			{
				var csvToArray
				pcn;
				var RutsCli = SplitTxt($scope.data.FiltrosRut.fileContent,true);
					 
					//Recorremos las filas seleccionadas
				var v_iter ;
				 v_iter=1;
				 
				 if(RutsCli.length>0){
					 $.each(RutsCli,function(key,value){

				  parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'RUT' , Valor : RutsCli[key] }); 

				   v_iter=v_iter+1;
				   
					});
				 }

			}else{
				
				var RutsCli = [];
			}

			//Obtiene datos de archivo con listado de rut de personas autorizadas

			var ListadoRutAuts = []; 
			if($scope.data.FiltrosRutAut!=undefined)
			{
				var RutsAut = SplitTxt($scope.data.FiltrosRutAut.fileContent,true);
					 
					//Recorremos las filas seleccionadas
				var v_iter ;
				 v_iter=1;
				 
				 if(RutsAut.length>0){
					 $.each(RutsAut,function(key,value){

						
			    parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'RUTAUT' , Valor : RutsAut[key] }); 
	 											
						 v_iter=v_iter+1;
				   });
				 }

			}else{
				
				var RutsAut = [];
			}

			var ListadoRutEmpl = [];  
			if($scope.data.FiltrosRutEmpl!=undefined)
			{
				var RutsEmpl = SplitTxt($scope.data.FiltrosRutEmpl.fileContent,true);
					 
					//Recorremos las filas seleccionadas
				var v_iter ;
				 v_iter=1;
				 
				 if(RutsEmpl.length>0){
					 $.each(RutsEmpl,function(key,value){

					parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'RUTEMPL' , Valor : RutsEmpl[key] }); 

						 v_iter=v_iter+1;
				   });
				 }

			}else{
				
				var RutsEmpl = [];
			}


			//Obtiene datos de archivo con listado de Nemos

			var ListadoNemos = [];
			if($scope.data.FiltrosNemo!=undefined)
			{
				var Nemos = SplitTxt($scope.data.FiltrosNemo.fileContent,true);
					 
					//Recorremos las filas seleccionadas
				var v_iter ;
				 v_iter=1;
				 
				 if(Nemos.length>0){
					 $.each(Nemos,function(key,value){

					parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'NEMO' , Valor : Nemos[key] }); 

					v_iter=v_iter+1;
				   }); 
				 }

			}else{
				
				var Nemos = [];
			}

		
		};
		
		
		//Funcion que permite llenar el arreglo de seleccion
		function fnOntenerTipoRegistro() {
			if($scope.pcn.registroAutorizado === true)
			{
				vTipoRegistro.push({IdTipoDocumento: 'NCG380AUT',Esquema:'NCG380Aut'});	
			};
			if($scope.pcn.registroTrabajador === true)
			{
				vTipoRegistro.push({IdTipoDocumento: 'NCG380TRA',Esquema:'NCG380Tra'});	
			};
			if($scope.pcn.registroOperaciones === true)
			{
				vTipoRegistro.push({IdTipoDocumento: 'NCG380OPE',Esquema:'NCG380Ope'});
			};
			if($scope.pcn.registroCustodia === true)
			{
				vTipoRegistro.push({IdTipoDocumento: 'NCG380CUS',Esquema:'NCG380Cus'});
			};
			if($scope.pcn.registroFacturacion === true)
			{
				vTipoRegistro.push({IdTipoDocumento: 'NCG380FAC',Esquema:'NCG380Fac'});
			};
			if($scope.pcn.registroCliente === true)
			{
				vTipoRegistro.push({IdTipoDocumento: 'NCG380CLI',Esquema:'NCG380Cli'});
			};
			if($scope.pcn.registroAsignacion === true)
			{
				vTipoRegistro.push({IdTipoDocumento: 'NCG380ASIG',Esquema:'NCG380Asig'});
			};
			if($scope.pcn.registroOrdenes === true)
			{
				vTipoRegistro.push({IdTipoDocumento: 'NCG380ORD',Esquema:'NCG380Ord'});	
			};
			if($scope.pcn.registroInstrucciones === true)
			{
				vTipoRegistro.push({IdTipoDocumento: 'NCG380INS',Esquema:'NCG380Ins'});	
			};
			if($scope.pcn.registroRepactaciones === true)
			{
				vTipoRegistro.push({IdTipoDocumento: 'NCG380REP',Esquema:'NCG380Rep'});	
			};
			if($scope.pcn.registroDeudores === true)
			{
				vTipoRegistro.push({IdTipoDocumento: 'NCG380DEU',Esquema:'NCG380Deu'});	
			};
			if($scope.pcn.registroIntBursatil === true)
			{
				vTipoRegistro.push({IdTipoDocumento: 'NCG380INTB',Esquema:'NCG380IntB'});	
			};
			
		};	
		function filtrosRut() {
			var filters = [];
			filters.push({field: "IdSolicitudCircular", operator: "eq", value: $scope.SolicitudCircular.IdSolicitudCircular});
			filters.push({field: "TipoParametro", operator: "eq", value: "RUT"});
			$scope.confBasicaListadoRut.dataSource.filters(filters);
		};
		
		function filtrosNemo() {
			var filters = [];
			filters.push({field: "IdSolicitudCircular", operator: "eq", value: $scope.SolicitudCircular.IdSolicitudCircular});
			filters.push({field: "TipoParametro", operator: "eq", value: "NEMO"});
			$scope.confBasicaListadoNemo.dataSource.filters(filters);
		};
		
		if (parametros.SolicitudCircular){
			$scope.modoVisualizacion =  true;
			var filterDatos = [];
			filterDatos.push({field: "IdSolicitudCircular", operator: "eq", value: $scope.SolicitudCircular.IdSolicitudCircular});
			filterDatos.push({field: "TipoParametro", operator: "neq", value: "RUT"});
			filterDatos.push({field: "TipoParametro", operator: "neq", value: "NEMO"});
		
			var DatosDataSource = bcsBlotterEmisionCircular.dataSourceParametrosSolicitudCircular();
			
			DatosDataSource.query({filter: filterDatos}).then(function(e){
				console.log(DatosDataSource.data());
				cargarDatosParametros(DatosDataSource.data(), parametros.SolicitudCircular);
				$scope.data.modoLectura = true;
			});
		}
		pcn.pantallaInicial = function() {
			$scope.seleccionTodosRut = false;
			$scope.seleccionTodosNemo = false;
			$scope.seleccionTodosEmpl = false;
			$scope.seleccionTodosAut = false;
			
			pcn.registroCliente = false; 
			pcn.registroAutorizados = false; 
			pcn.registroOrdenes = false; 
			pcn.registroOperaciones = false; 
			pcn.registroAsignacion = false; 
			pcn.registroFacturacion = false; 
			pcn.registroCustodia = false; 
			pcn.registroTrabajador = false;
			pcn.registroInstrucciones = false;
			pcn.registroRepactaciones = false;
			pcn.registroDeudores = false;
			pcn.registroIntBursatil = false;
		};
		

		pcn.pantallaInicial();
		
		
		
		$scope.$watch('data.Filtros.TodosCli',function(newval,oldval){
			switch (newval){
				case true:  
					$scope.activadoCli = false;  
					$scope.listRut = true;
					break;					
				case false: 
					$scope.activadoCli = true;
					$scope.listRut = false;
					break; 
			}			
		});
	
		
		$scope.$watch('data.Filtros.TodosNemo',function(newval,oldval){
			switch (newval){
				case true:  
					$scope.seleccionNemo = true;  
					$scope.listNemo = true;
					break;					
				case false:  
					$scope.seleccionNemo = false;  
					$scope.listNemo = false;
					break; 
			}			
		});	

		$scope.$watch('data.Filtros.TodosAut',function(newval,oldval){
			switch (newval){
				case true:  
					$scope.activadoAut = false;  
					$scope.listRutAut = false;
					break;					
				case false: 
					$scope.activadoAut = true;  
					$scope.listRutAut = true;
					break; 
			}			
		});	

		$scope.$watch('data.Filtros.TodosEmpl',function(newval,oldval){
			switch (newval){
				case true:  
					$scope.listRutEmpl = true;  
					$scope.activadoEmpl = false;
					break;					
				case false: 
					$scope.listRutEmpl = false;  
					$scope.activadoEmpl = true;
					break; 
			}			
		});	
		
		$scope.$watchGroup(['data.Filtros.FechaInicio','data.Filtros.FechaFin',
		                    'pcn.registroCliente',
		                    'pcn.registroAutorizados',
		                    'pcn.registroOrdenes',
		                    'pcn.registroOperaciones',
		                    'pcn.registroAsignacion',
		                    'pcn.registroFacturacion',
		                    'pcn.registroCustodia',
		                    'pcn.registroTrabajador',
		                    'pcn.registroInstrucciones',
		                    'pcn.registroRepactaciones',
		                    'pcn.registroDeudores',
		                    'pcn.registroIntBursatil'],function(newval,oldval){

				
				if(( $scope.pcn.registroAutorizado == true || $scope.pcn.registroCliente==true|| $scope.pcn.registroOperaciones==true || $scope.pcn.registroFacturacion==true || $scope.pcn.registroTrabajador==true || $scope.pcn.registroOrdenes==true || $scope.pcn.registroAsignacion==true || $scope.pcn.registroCustodia==true || $scope.pcn.registroInstrucciones==true || $scope.pcn.registroRepactaciones==true || $scope.pcn.registroDeudores==true || $scope.pcn.registroIntBursatil==true)	&& ($scope.pcn.FecInicio && $scope.pcn.FecTermino ))
				{
					$scope.seleccionGrabar = true;	
				}else{
					
					$scope.seleccionGrabar = false;
				}
		});
		
		
		
		$scope.$watchGroup(['pcn.registroCliente','pcn.registroAutorizado','pcn.registroOrdenes','pcn.registroOperaciones','pcn.registroAsignacion','pcn.registroFacturacion','pcn.registroCustodia','pcn.registroTrabajador','pcn.registroInstrucciones','pcn.registroDeudores','pcn.registroIntBursatil'],function(newval,oldval){
			
			//$scope.seleccionCli = true; filtro estatico
			$scope.seleccionFecInicio = false;
			$scope.seleccionFecFin= false;
			$scope.seleccionNemo= true;
			$scope.seleccionMcdo= false;
			$scope.seleccionOficio= true;
			//$scope.seleccionRutTra=true;
			$scope.activadoEmpl=false;
			$scope.activadoAut=false;
			$scope.activadoCli=true; 

			$scope.seleccionTodosRut=false;
			$scope.seleccionTodosNemo=true;
			$scope.seleccionTodosEmpl=true;
			$scope.seleccionTodosAut=true;

			$scope.listRut = false;				
			$scope.listRutAut = true;
			$scope.listNemo = true;
			$scope.listRutEmpl = true;
			
			$scope.activadoRel = false;
			$scope.seleccionTodosRel = true;
			$scope.seleccionFechaRealizacion = true;
				
				

			//da igual ya que los filtros de cliente aplican a todos los registros restantes
			/*	if($scope.pcn.registroCliente==true) 
				{
					$scope.seleccionCli 		= false;
					$scope.seleccionFecInicio 	= false;
					$scope.seleccionFecFin		= false;
					$scope.activado				= true;
					$scope.listRut 				= false;
					
					if($scope.data.Filtros.TodosCli ===true)
					{
						$scope.activado = false;
						$scope.data.oConfiguracionSocioNegocio.seleccionado = 0;
						
					}
					
			    }*/ 
				
			if($scope.pcn.registroIntBursatil==true) 
			{
				$scope.activadoCli		= false;
				$scope.seleccionTodosRut		= true;
				$scope.listRut	= true;
			}
			
			if($scope.pcn.registroInstrucciones==true) 
			{
				$scope.seleccionNemo		= false;
				$scope.seleccionMcdo		= false;
				$scope.seleccionTodosNemo	= false;
				
				$scope.listNemo 			= false;
				$scope.seleccionFechaRealizacion = false;
				$scope.seleccionFecInicio = true;
				$scope.seleccionFecFin= true;
			}
			
			if($scope.pcn.registroCliente==true || $scope.pcn.registroRepactaciones==true || $scope.pcn.registroDeudores==true) 
			{
				$scope.seleccionFecInicio = false;
				$scope.seleccionFecFin= false;
			}

			if($scope.pcn.registroTrabajador==true) 
			{
				//$scope.seleccionRutTra=false;
				
				$scope.activadoEmpl = true;
				$scope.listRutEmpl = false;
				$scope.seleccionTodosEmpl=false;
				if($scope.pcn.registroCliente==false && $scope.pcn.registroOrdenes==false && $scope.pcn.registroAsignacion==false && $scope.pcn.registroCustodia==false && $scope.pcn.registroOperaciones==false && $scope.pcn.registroFacturacion==false && $scope.pcn.registroAutorizado==false) 
				{
					$scope.activadoCli =false;
					$scope.listRut = true;
				}
				$scope.seleccionFecInicio = false;
				$scope.seleccionFecFin= false;
							
			}
				

			if($scope.pcn.registroOrdenes==true || $scope.pcn.registroAsignacion==true || $scope.pcn.registroCustodia==true ) 
			{
				$scope.seleccionNemo		= false;
				$scope.seleccionMcdo		= true;
				$scope.seleccionTodosNemo	= false;
				
				$scope.listNemo 			= false;
				$scope.seleccionFecInicio = false;
				$scope.seleccionFecFin= false;
			}
			
			if($scope.pcn.registroOperaciones==true || $scope.pcn.registroFacturacion==true) 
			{
				$scope.seleccionNemo		= false;
				$scope.seleccionMcdo		= true;
				$scope.seleccionTodosNemo	= false;
				//$scope.seleccionRueda		= false;
				//$scope.seleccionNoRueda	= false;

				$scope.listNemo 			= false;
				$scope.seleccionFecInicio = false;
				$scope.seleccionFecFin= false;
							
			}
			if($scope.pcn.registroAutorizado==true ) 
			{
				$scope.activadoAut=true;
				$scope.listRutAut = false;
				$scope.seleccionTodosAut = false;
				$scope.activadoRel = true;
				$scope.seleccionTodosRel = false;
				$scope.seleccionFecInicio = false;
				$scope.seleccionFecFin= false;
				
				
				if($scope.pcn.registroOrdenes==false && $scope.pcn.registroAsignacion==false && $scope.pcn.registroCustodia==false && $scope.pcn.registroOperaciones==false && $scope.pcn.registroFacturacion==false )
				{
					$scope.seleccionNemo		= true;
					$scope.seleccionTodosNemo	= true ;
					$scope.listNemo 			= true;
				}
				
				$scope.seleccionMcdo		= false;

				//$scope.seleccionRueda		= false;
				//$scope.seleccionNoRueda	= false;

							
			}

				

			});		
		
		$scope.pcn.clear();
		
	});
	
	
	
}());




