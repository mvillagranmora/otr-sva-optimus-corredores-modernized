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
	angular.module('ModuloNormativa').controller('PanelNCG380Ctrl',function($scope,$timeout,Model,Utils,$translate,FileUpload,bcsAlerta,OPCIONES_BCSALERTA,bcsComunes,$compile,bcsServiciosNormativa,bcsBlotterSolicitudCircular){

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
		
		/******************************************************/
		/*** FUNCIONES DE CARGA DE FOLIOS					***/
		/******************************************************/
		
		var dsConsultaFolioOrdenes = 
		{
			type : "odata",
			definition : {
				serverFiltering 	: true, // Filtrado de datos en el servidor.
				transport: {
					read: {
						url: 		"/BSCL/Plataforma/GWJAVA/PLA/call?t=601192",
						dataType: 	"json"}
				},
                schema: { 
                	model: {
                		fields : {													
                			"Folio" 			: {type: "string"},
                			"FechaAceptacion" 	: {type: "date"}
                		}
                	}
                }
			}
		};
		
		/*********************************************
		 * COMBOBOX
		 ********************************************/
		$scope.data ={
		oCircularConfiguracion : {},
		Filtros: {},
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
				tipo : "combobox-tipo-relacion",
				//configuracion :{},
				filtros: {
					//IdNormativa :  {field    : "IdNormativa",   operator: "eq", value : 'NCG380'}
			    },			 
				idActual : false,
				seleccionado : false,
				idDefecto : ''
		};
		
		
		
		/**
		 * GRILLAS PARA MODO VISUALIZACION.
		 */

		$scope.confBasicaListadoRut= {
				dataSource			: bcsBlotterSolicitudCircular.dataSourceParametrosSolicitudCircular(),
				columns				: bcsBlotterSolicitudCircular.columnasParametrosRut(),		
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
				dataSource			: bcsBlotterSolicitudCircular.dataSourceParametrosSolicitudCircular(),
				columns				: bcsBlotterSolicitudCircular.columnasParametrosNemo(),		
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

		/*********************************************
		 * FUNCIONES
		 ********************************************/
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
			pcn.FecInicio 	 = util.dateInicioDia($scope.SesionFechaSistema);
			pcn.FecTermino 	 = util.dateInicioDia($scope.SesionFechaSistema);
			pcn.FechaRealizacion = util.dateInicioDia($scope.SesionFechaSistema);
			$scope.pcn.registroCliente = false;
			$scope.pcn.registroAutorizado= false;
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

		function pad (n, length) {
		    var  n = n.toString();
		    while(n.length < length)
		         n = '0' + n;
		return n;
		};
		 
		pcn.solicitar = function() {
			 
			consultarFoliosVacios()

		};
		
		function consultarFoliosVacios(){
			fnOntenerTipoRegistro();
			
			 var tipoOrden = false
			 var preguntaOrden = true
			
			$.each(vTipoRegistro,function(key,value){
				if (vTipoRegistro[key].IdTipoDocumento=='NCG380ORD')
					{tipoOrden = true};
			});
			
			if (tipoOrden){
				
				//Verificar si existen Folios con valores '' para asi desplegar un Alert.
				var fechaDesde = util.dateSPdate(pcn.FecInicio);
				var fechaHasta = util.dateSPdate(pcn.FecTermino);
				
				var filtros = [{ field: "Folio", 				operator: "eq",  value: ''},
								{ field: "FechaAceptacion", 	operator: "gte", value: fechaDesde},
								{ field: "FechaAceptacion", 	operator: "lte", value: fechaHasta}];
				
						
				 var consultarFolios = new bcs.DataSource(dsConsultaFolioOrdenes);
				 consultarFolios.query({ filter : filtros}).then(function()
				 {
					if(consultarFolios.view().length > 0)
					{
						var t = bcsAlerta.crear("PREGUNTA_SI_NO","Existen "+ consultarFolios.view().length +" Ordenes con folios vacios, ¿Desea continuar?" ,pcn.idVentana);
							t .then(function(result)
							{
								if (result === OPCIONES_BCSALERTA.NO)
								{
									bcsComunes.ventanaOverlay(pcn.idVentana,false);
									return;									
								}
								//SI
								enviarSolicitud()
						});
					}else{
						//No hay folios vacios
						enviarSolicitud()
					}
				}); 
			}else{
				//No selecciono ordenes
				enviarSolicitud()
			};

		};
		
		//Crea la solicitud para crear Circular 
		function enviarSolicitud() {

			//Comunes
	    	parametrosSP.p_IdEmpresa 				=  $scope.SesionIdEmpresa;
	    	parametrosSP.p_IdUsuario              	=  $scope.SesionIdUsuario;
	    	parametrosSP.p_IdModulo 				=  $scope.solCir.modulo;
		//	parametrosSP.p_FechaIngresoSolicitud 	=  $scope.solCir.fechaInicioSol
	    	parametrosSP.p_FechaInicio              =  '0001-01-01';
			//fnOntenerTipoRegistro();
			if (pcn.FecInicio==null || pcn.FecInicio==undefined)// || $scope.seleccionFecInicio == true)
				{
					pcn.FecInicio=util.dateInicioDia('0001-01-01');
				};
			if (pcn.FecTermino==null || pcn.FecTermino==undefined)
				{
					pcn.FecTermino=util.dateInicioDia($scope.SesionFechaSistema);
				};
			
			var fd = util.dateSPdate(pcn.FecInicio);	
			var fh = util.dateSPdate(pcn.FecTermino); 
			
			if ( $scope.pcn.registroCliente || $scope.pcn.registroTrabajador ) {
				fd = fh;
			}
			
			if (fh < fd){
    	 			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("FECHA_TERMINO_RANGO_CONSULTA_INVALIDA"));
    	 			return;
    	 	};	      
			
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
				
				var fechaDesde = util.dateInicioDia(pcn.FecInicio);
				var fechaHasta = util.dateTerminoDia(pcn.FecTermino);
				//especificos
				if(parametrosSP.p_IdTipoDocumento=='NCG380OPE'){
					parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'FINI' , Valor : util.dateSPdate(fechaDesde) });
					parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'FTER' , Valor : util.dateSPdate(fechaHasta) });	
				}else{
					parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'FINI' , Valor : util.dateSPseconddate(fechaDesde) });
    				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'FTER' , Valor : util.dateSPseconddate(fechaHasta) });
				}
				
    			
    			
    			//TODOS_CLI
    			if($scope.data.Filtros.TodosCli===true && $scope.activadoCli && parametrosSP.p_IdTipoDocumento !='NCG380ORD'&& parametrosSP.p_IdTipoDocumento !='NCG380FAC' && parametrosSP.p_IdTipoDocumento !='NCG380ASIG' && parametrosSP.p_IdTipoDocumento !='NCG380DEU' ){
					parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'TODOS_CLI' , Valor : 'S' });
	    		}else{
					if (parametrosSP.p_IdTipoDocumento =='NCG380ORD' && $scope.data.Filtros.TodosCli===true){parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'RUT' , Valor :null });}
					if (parametrosSP.p_IdTipoDocumento =='NCG380FAC' && $scope.data.Filtros.TodosCli===true){parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'RUT' , Valor :null });}
					if (parametrosSP.p_IdTipoDocumento =='NCG380ASIG' && $scope.data.Filtros.TodosCli===true){parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'RUT' , Valor :null });}
					if (parametrosSP.p_IdTipoDocumento =='NCG380DEU' && $scope.data.Filtros.TodosCli===true){parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'RUT' , Valor :null });}
				};
				
				//TODOS_NEMO
				if($scope.data.Filtros.TodosNemo===true && $scope.seleccionNemo&& parametrosSP.p_IdTipoDocumento !='NCG380ORD'&& parametrosSP.p_IdTipoDocumento !='NCG380FAC' 
					&& parametrosSP.p_IdTipoDocumento !='NCG380ASIG' && parametrosSP.p_IdTipoDocumento !='NCG380CUS' ){
					
		    		parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'TODOS_NEMO' , Valor : 'S' });
				}else{
					if (parametrosSP.p_IdTipoDocumento =='NCG380ORD' && $scope.data.Filtros.TodosNemo===true){parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'NEMO' , Valor :null });}
					if (parametrosSP.p_IdTipoDocumento =='NCG380FAC' && $scope.data.Filtros.TodosNemo===true){parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'NEMO' , Valor :null });}
					if (parametrosSP.p_IdTipoDocumento =='NCG380ASIG' && $scope.data.Filtros.TodosNemo===true){parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'NEMO' , Valor :null });}
					if (parametrosSP.p_IdTipoDocumento =='NCG380CUS' && $scope.data.Filtros.TodosNemo===true){parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'NEMO' , Valor :null });}
				};
				
				//TODOS_AUT
				if($scope.data.Filtros.TodosAut===true && $scope.activadoAut){
		    		parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'TODOS_AUT' , Valor : 'S' });
				};
				//TODOS_EMP
				if($scope.data.Filtros.TodosEmpl===true && $scope.activadoEmpl&& parametrosSP.p_IdTipoDocumento !='NCG380TRA'){
					
		    		parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'TODOS_EMP' , Valor : 'S' });
				}else{
					if (parametrosSP.p_IdTipoDocumento =='NCG380TRA' && $scope.data.Filtros.TodosEmpl===true){parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'RUTEMPL' , Valor :null });}
				};
				//TODOS_REL
				if($scope.data.Filtros.TodosRel===true && $scope.activadoRel){
		    		parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'TODOS_REL' , Valor : 'S' });
				};
				
				//IDCLI
				if ($scope.pcn.oConfiguracionSocioNegocio.seleccionado && $scope.activadoCli && $scope.data.Filtros.TodosCli===false){
					
					if (parametrosSP.p_IdTipoDocumento =='NCG380ORD' || parametrosSP.p_IdTipoDocumento =='NCG380FAC'|| parametrosSP.p_IdTipoDocumento =='NCG380ASIG' || parametrosSP.p_IdTipoDocumento =='NCG380DEU' || parametrosSP.p_IdTipoDocumento =='NCG380CUS'){
						parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'RUT' , Valor : $scope.pcn.oConfiguracionSocioNegocio.seleccionado.DocumentoIdentidadDefecto});
						
					}else
						{
						parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'IDCLI' , Valor : $scope.pcn.oConfiguracionSocioNegocio.seleccionado.IdCliente });						
						};
					
				};
				//OFICIAL DETALLADO
				if($scope.solCir.CheckOfiDet == "Oficial" && $scope.solCir.checkCliente==true){
					parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'CLI_OFICIAL' , Valor : 'S' });
				}
				if($scope.solCir.CheckOfiDet == "Detallado"&& $scope.solCir.checkCliente==true){
					parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'CLI_OFICIAL' , Valor : 'N' });
				}
			

				
				//NEMO
				if ($scope.data.instrumentoDatos){$scope.data.Filtros.Nemo=$scope.data.instrumentoDatos.Nemotecnico;};
				 
				if ($scope.data.Filtros.Nemo && $scope.data.Filtros.TodosNemo===false){
					parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'NEMO' , Valor : $scope.data.Filtros.Nemo });					
				};

				
	    		//CLAINS
	    		if($scope.filtros.clasificacionTipoInstrumento.IdClasificacionTipoInstrumento !=undefined && $scope.filtros.clasificacionTipoInstrumento.IdClasificacionTipoInstrumento != '' && $scope.seleccionMcdo)
	    		{
	    			parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'CLAINS' , Valor : $scope.filtros.clasificacionTipoInstrumento.IdClasificacionTipoInstrumento || '' });
	    		}else{
					if (parametrosSP.p_IdTipoDocumento =='NCG380ORD' && $scope.seleccionMcdo){parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'CLAINS' , Valor :null });}
					if (parametrosSP.p_IdTipoDocumento =='NCG380FAC' && $scope.seleccionMcdo){parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'CLAINS' , Valor :null });}
					if (parametrosSP.p_IdTipoDocumento =='NCG380ASIG' && $scope.seleccionMcdo){parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'CLAINS' , Valor :null });}
				};
	    		
	    		//RUTEMPL
	    		if($scope.data.oConfiguracionEmpleado.seleccionado && $scope.activadoEmpl && $scope.data.Filtros.TodosEmpl===false)
	    		{
	    			if (parametrosSP.p_IdTipoDocumento =='NCG380TRA'){
						parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'RUTEMPL' , Valor : $scope.data.oConfiguracionEmpleado.seleccionado.DocumentoIdentidadDefecto});
						
					}else
						{
							parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'IDEMPLEADO' , Valor : $scope.data.oConfiguracionEmpleado.seleccionado.IdPersona });
						};
	    			
	    		};
	    		
	    		if($scope.data.oConfiguracionAutorizado.seleccionado && $scope.activadoAut && $scope.data.Filtros.TodosAut===false)
	    		{
	    			parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'IDAUTORIZADO' , Valor : $scope.data.oConfiguracionAutorizado.seleccionado.IdPersona });
	    		};
	    		
 		
	    		if ($scope.data.Filtros.Oficio && !$scope.seleccionOficio){
					parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'OFI' , Valor : $scope.data.Filtros.Oficio });
					var codigo =pad($scope.data.Filtros.Oficio,3);
					var nomarch=  parametrosSP.p_IdTipoDocumento + pcn.FecTermino.getFullYear() + '_' + codigo;
					parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'NOMSAL' , Valor : nomarch});
					parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'EXTSAL' , Valor : 'csv'});
					
				};			
				
	    		
	    		if(pcn.oConfiguracionRelaciones.seleccionado.IdTipoRelacion !=undefined && pcn.oConfiguracionRelaciones.seleccionado.IdTipoRelacion !=''  && $scope.data.Filtros.TodosRel===false)
	    		{
	    			parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'RELA' , Valor : pcn.oConfiguracionRelaciones.seleccionado.IdTipoRelacion.toString()|| '' });
				};
	    		 
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'DESCCOL' , Valor : 'TIT' });//Usar descripcion por titulos en la salida
				
				
	    		bcsServiciosNormativa.CircularCrearSolicitud(parametrosSP,successCrearSolicitud,errorCrearSolicitud);
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
	};
		//Funcion que permite llenar los parametros espcificos del servicio segun Tipo Documento
		function fnOntenerParametrosEspecificos(TipoDocumento) {
			
			
			//OBTENCION DE LISTADO DE RUT, RUTAUT,RUTEMPL,NEMO
			if($scope.data.FiltrosRut!=undefined)
			{
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
			vTipoRegistro= [];
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
		
			var DatosDataSource = bcsBlotterSolicitudCircular.dataSourceParametrosSolicitudCircular();
			
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
			pcn.registroAutorizado = false; 
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
			
			
			$scope.data.Filtros.TodosCli= true;
			$scope.data.Filtros.TodosNemo= true;
			$scope.data.Filtros.TodosEmpl = true;
			$scope.data.Filtros.TodosAut= true;
			$scope.data.Filtros.TodosRel= true;
			
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
					//$scope.seleccionNemo = false;  
					$scope.listNemo = true;
					break;					
				case false:  
					//$scope.seleccionNemo = false;  
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
		                    'pcn.registroAutorizado',
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
					pcn.filtroActivo(false);
				}
		});
		
		pcn.filtroActivo = function(newval) {
			$scope.seleccionNemo= newval;
			$scope.seleccionMcdo= newval;
			$scope.seleccionOficio= !newval;
			$scope.activadoEmpl=newval;
			$scope.activadoAut=newval;
			$scope.activadoCli=newval; 
				   
			$scope.seleccionTodosRut=!newval;
			$scope.seleccionTodosNemo=!newval;
			$scope.seleccionTodosEmpl=!newval;
			$scope.seleccionTodosAut=!newval;
			$scope.seleccionTodosRel = !newval;

			$scope.listRut = newval;				
			$scope.listRutAut = newval;
			$scope.listNemo = newval;
			$scope.listRutEmpl = newval;
			
			$scope.activadoRel = newval;
			
			$scope.seleccionFechaRealizacion = !newval;
			$scope.seleccionFecInicio = !newval;
			$scope.seleccionFecFin= !newval;
			pcn.oConfiguracionRelaciones.idActual=newval;
		};
		
		
		pcn.QuitarSeleccion = function(excepcion) {
			if (excepcion !='registroCliente'){pcn.registroCliente = false;}; 
			if (excepcion !='registroAutorizado'){pcn.registroAutorizado = false;}; 
			if (excepcion !='registroOrdenes'){pcn.registroOrdenes = false;};
			if (excepcion !='registroOperaciones'){pcn.registroOperaciones = false;};
			if (excepcion !='registroAsignacion'){pcn.registroAsignacion = false;};
			if (excepcion !='registroFacturacion'){pcn.registroFacturacion = false;};
			if (excepcion !='registroCustodia'){pcn.registroCustodia = false;};
			if (excepcion !='registroTrabajador'){pcn.registroTrabajador = false;};
			if (excepcion !='registroInstrucciones'){pcn.registroInstrucciones = false;};
			if (excepcion !='registroRepactaciones'){pcn.registroRepactaciones = false;};
			if (excepcion !='registroDeudores'){pcn.registroDeudores = false;};
			if (excepcion !='registroIntBursatil'){pcn.registroIntBursatil = false;};
			pcn.filtroActivo(false);
		};
		
		
		$scope.$watch('pcn.registroIntBursatil', function(newValue, oldValue) {
			if (newValue===true) {pcn.QuitarSeleccion('registroIntBursatil');};
			if($scope.pcn.registroIntBursatil==true){
				$scope.activadoCli		= false;
				$scope.seleccionTodosRut		= true;
				$scope.listRut	= true;	
				$scope.seleccionFecInicio = false;
				$scope.seleccionFecFin= false;
			};
			
			
			
		});

		$scope.$watch('pcn.registroInstrucciones', function(newValue, oldValue) {
			if (newValue===true) {pcn.QuitarSeleccion('registroInstrucciones');};
			if($scope.pcn.registroInstrucciones==true){
				$scope.activadoCli =true;
				$scope.listRut = false;
				$scope.seleccionNemo		= true;
				$scope.seleccionMcdo		= false;
				$scope.seleccionTodosNemo	= false;
				
				$scope.seleccionTodosRut=false;
				$scope.activadoCli=true
				
				$scope.listNemo 			= false;
				$scope.seleccionFechaRealizacion = false;
				pcn.FecInicio 	 = util.dateInicioDia($scope.SesionFechaSistema);
				$scope.seleccionFecInicio = true;
				$scope.seleccionFecFin= true;
			};
			
		});

		
		$scope.$watch('pcn.registroTrabajador', function(newValue, oldValue) {
			if (newValue===true) {pcn.QuitarSeleccion('registroTrabajador');};
			if  ($scope.pcn.registroTrabajador==true){
				pcn.filtroActivo(false);
				//$scope.activadoCli  =true;
				//$scope.activadoAut  =true;
				//$scope.activadoRel  =true;
				$scope.activadoEmpl =true;
				$scope.seleccionTodosEmpl=false;
				pcn.FecInicio 	 = util.dateInicioDia($scope.SesionFechaSistema);
				$scope.seleccionFecInicio 	= true;
				$scope.seleccionFecFin		= false;
				$scope.seleccionOficio		= false;
				
				$scope.listRut = false;
				$scope.listRutEmpl = false;
			};
			
		
			
		
			
		});
		
		
		$scope.$watch('pcn.registroAutorizado', function(newValue, oldValue) {
			if (newValue===true) {pcn.QuitarSeleccion('registroAutorizado');};
			if ($scope.pcn.registroAutorizado == true){
				pcn.filtroActivo(false);
				$scope.activadoCli=true
				$scope.activadoAut=true;
				$scope.activadoRel = true;
				$scope.seleccionTodosRut=false;
				$scope.seleccionTodosAut=false;
				$scope.seleccionTodosRel = false;
				pcn.FecInicio=util.dateInicioDia('0001-01-01');
				$scope.seleccionFecInicio = true;
				$scope.seleccionFecFin= false;
				$scope.data.Filtros.TodosRel= false;
				pcn.oConfiguracionRelaciones.idActual='AUTZDO';
				$scope.listRut = false;
			};
			
			
			
		});
			
		$scope.$watch('pcn.registroRepactaciones', function(newValue, oldValue) {
			if (newValue===true) {pcn.QuitarSeleccion('registroRepactaciones');};
			if($scope.pcn.registroRepactaciones==true){
				$scope.activadoCli =true;
				$scope.listRut = false;
				$scope.seleccionFecInicio = false;
				$scope.seleccionFecFin= false;
				
				$scope.seleccionTodosRut=false;

			};
			
			
		});
		
		$scope.$watch('pcn.registroOperaciones', function(newValue, oldValue) {
			if (newValue===true) {pcn.QuitarSeleccion('registroOperaciones');};
			if($scope.pcn.registroOperaciones==true){
				$scope.activadoCli =true;
				$scope.listRut = false;
				$scope.seleccionNemo		= false;
				$scope.seleccionMcdo		= true;
				$scope.seleccionTodosNemo	= false;
				$scope.listNemo 			= false;
				$scope.seleccionFecInicio = false;
				$scope.seleccionFecFin= false;
			};
			
		});
		$scope.$watch('pcn.registroFacturacion', function(newValue, oldValue) {
			if (newValue===true) {pcn.QuitarSeleccion('registroFacturacion');};
			if($scope.pcn.registroFacturacion==true){
				$scope.activadoCli			= true;
				
				$scope.listRut 				= false;
				$scope.listNemo 			= false;
				
				$scope.seleccionNemo		= false;
				$scope.seleccionMcdo		= true;
				$scope.seleccionTodosNemo	= false;
				$scope.seleccionTodosRut	= false;
				$scope.seleccionOficio		= false;
				
				
				$scope.seleccionFecInicio = false;
				$scope.seleccionFecFin= false;

			};
			
		});
		
		$scope.$watch('pcn.registroCliente', function(newValue, oldValue) {
			if (newValue===true) {pcn.QuitarSeleccion('registroCliente');};
			if($scope.pcn.registroCliente==true){
				$scope.activadoCli			= true;
				$scope.listRut 				= false;
				$scope.seleccionTodosRut	= false;				
				pcn.FecInicio 	 = util.dateInicioDia($scope.SesionFechaSistema);
				$scope.seleccionFecInicio = true;
				$scope.seleccionFecFin= false;
				$scope.solCir.checkCliente = true;
				
	
			}
			else{
				$scope.solCir.checkCliente = false;
			};
		});	
		
		$scope.$watch('pcn.registroDeudores', function(newValue, oldValue) {
			if (newValue===true) {pcn.QuitarSeleccion('registroDeudores');};
			if($scope.pcn.registroDeudores==true){
				$scope.activadoCli =true;
				$scope.listRut = false;
				$scope.seleccionFecInicio = false;
				$scope.seleccionFecFin= false;
				
				$scope.seleccionTodosRut=false;

			};
		});	

		
		$scope.$watch('pcn.registroOrdenes', function(newValue, oldValue) {
			if (newValue===true) {pcn.QuitarSeleccion('registroOrdenes');};
			if($scope.pcn.registroOrdenes==true){
				$scope.activadoCli 			= true;
				
				$scope.listRut 				= false;
				$scope.listNemo 			= false;
				
				$scope.seleccionNemo		= false;
				$scope.seleccionMcdo		= true;
				$scope.seleccionTodosNemo	= false;
				$scope.seleccionTodosRut	= false;
				$scope.seleccionOficio		= false;
				
				$scope.seleccionFecInicio = false;
				$scope.seleccionFecFin= false;

			};
		});
		
		$scope.$watch('pcn.registroAsignacion', function(newValue, oldValue) {
			if (newValue===true) {pcn.QuitarSeleccion('registroAsignacion');};
			if($scope.pcn.registroAsignacion==true){
				$scope.activadoCli =true;
				$scope.listRut = false;
				$scope.seleccionNemo		= false;
				$scope.seleccionMcdo		= true;
				$scope.seleccionTodosNemo	= false;
				
				$scope.listNemo 			= false;
				$scope.seleccionFecInicio = false;
				$scope.seleccionFecFin= false;

			};
			
		});	
			
		$scope.$watch('pcn.registroCustodia', function(newValue, oldValue) {
			if (newValue===true) {pcn.QuitarSeleccion('registroCustodia');};
			if($scope.pcn.registroCustodia==true){
				$scope.activadoCli =true;
				$scope.listRut = false;
				$scope.seleccionNemo		= false;
				$scope.seleccionMcdo		= true;
				$scope.seleccionTodosNemo	= false;
				$scope.seleccionTodosRut	= false;
				
				$scope.listNemo 			= false;
				$scope.seleccionFecInicio = false;
				$scope.seleccionFecFin= false;

			};
			
		});	
		
		
			$scope.$watch('pcn.oConfiguracionSocioNegocio.seleccionado',function(newval,oldval){
				if (pcn.oConfiguracionSocioNegocio)
					{			
						if (pcn.oConfiguracionSocioNegocio.seleccionado.IdCliente)
						{
							$scope.data.Filtros.TodosCli=false;
						}else{
							$scope.data.Filtros.TodosCli=true;
						};
					};					
				});
					
			$scope.$watch('pcn.oConfiguracionRelaciones.idActual',function(newval,oldval){
				if (pcn.oConfiguracionRelaciones.idActual)
				{
					$scope.data.Filtros.TodosRel=false;
				}else{
					$scope.data.Filtros.TodosRel=true;
				};
					
			});
			
			
			$scope.$watch('pcn.oConfiguracionAutorizado.seleccionado',function(newval,oldval){
				if (pcn.oConfiguracionAutorizado)
					{			
						if (pcn.oConfiguracionAutorizado.seleccionado.IdCliente)
						{
							$scope.data.Filtros.TodosAut=false;
						}else{
							$scope.data.Filtros.TodosAut=true;
						};
					};					
				});
			
			
					
			$scope.$watch('pcn.FechaRealizacion', function(newValue, oldValue) {
				if (newValue !== oldValue) {
					pcn.FecInicio 	 = pcn.FechaRealizacion;
					pcn.FecTermino 	 = pcn.FechaRealizacion;
					};

			});	
		
		$scope.pcn.clear();
		
		
	});
	
	
	
}());




