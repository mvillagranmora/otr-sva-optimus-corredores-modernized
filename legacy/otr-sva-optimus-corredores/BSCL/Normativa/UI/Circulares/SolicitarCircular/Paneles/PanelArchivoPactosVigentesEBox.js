(function() {
    angular.module('ModuloNormativa').controller('PanelArchivoPactosVigentesEBoxCtrl', function($scope,$timeout,Model,Utils,$translate,bcsComunes,$compile,bcsServiciosNormativa, bcsVisorCircularConfig,$rootScope){

        var pvEBox = this;
        pvEBox.idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
        pvEBox.domVentana = $("[ventanaid='" + pvEBox.idVentana + "']");
        pvEBox.IdVersionEsquema = '';
        pvEBox.IdTipoDocumento = 'PACVIGEBOX';
        $scope.dataTemplate = {};
		var idVentana = bcsComunes.ventanaWinId($scope);
		var idEmpresa = $scope.SesionIdEmpresa;
		
		pvEBox.opcionFiltro='AM';
		pvEBox.CodigoCorredora = 0;
				
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
				    	Valor : '',
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
		  // DataSource
		ModeloSolicitar = function() {
			return {
				model: {
					fields: {
						Fecha: { type: "date" },						
					}
				}
			};
		};

		ModeloCodigoCorredora = function() {
			return {
				model: {
					fields: {
						CodigoCorredora: { type: "string" },						
					}
				}
			};
		};
		
		// DataSource para las fechas
		dataSourceFechasSolicitar = function() {
			return new bcs.DataSource({
				type : "odata", 
				definition : {
					serverFiltering : true, 
					serverPaging : true, 
					serverSorting : true, 
					pageSize : 50,
					transport : {
						read : {
							url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=2470",
							dataType : "json"
						}
					},
					filter: [
						{ field: "FechaOperacion", operator: "lte", value: idEmpresa },
					],
				schema: ModeloSolicitar(),
				}
			});
		};
	
		// DataSource para el CodigoCorredora
		dataSourceCodigoCorredora = function() {
			return new bcs.DataSource({
				type : "odata", 
				definition : {
					serverFiltering : true, 
					serverPaging : true, 
					serverSorting : true, 
					pageSize : 50,
					transport : {
						read : {
							url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=3096",
							dataType : "json"
						}
					},
					filter: [
						{ field: "IdEmpresa", operator: "eq", value: idEmpresa },
						{ field: "IdBolsa", operator: "eq", value:  'BCS' }
					],
				schema: ModeloCodigoCorredora(),
				}
			});
		};


		/*********************************************
		 * FUNCIONES
		 ********************************************/

		$scope.pvEBox.clear = function(){
			pvEBox.FecInicio 	 = util.dateInicioDia($scope.SesionFechaSistema);
			pvEBox.FecTermino 	 = util.dateInicioDia($scope.SesionFechaSistema);
			
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
			bcsComunes.ventanaOverlay(pvEBox.idVentana, false);
			bcsComunes.ventanaData(pvEBox.idVentana, respuesta);
			bcsComunes.ventanaCerrar(pvEBox.idVentana);
			
		};
		
		var errorCrearSolicitud = function(respuesta)
		{	
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
			bcsComunes.msg("error","error",$translate.instant('INGRESO ERROR'));
			bcsComunes.ventanaOverlay(pvEBox.idVentana, false);
		};
		$scope.imprimirCircular = function() {
			var clasificacion;
		   if (pvEBox.IdClasificacionTipoInstrumento!== 0) 
		   {clasificacion =  pvEBox.IdClasificacionTipoInstrumento;}
		   else{
			   clasificacion='';
		   };
		
				var filterTemplate = [ { field: "IdTipoDocumento",operator:"eq", value: pvEBox.IdTipoDocumento },
									   { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
									   { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
				$scope.solCir.ConsultaArchivoNormativa.query({ filter: filterTemplate }).then(function(){
					if($scope.solCir.ConsultaArchivoNormativa.view().length > 0) {
						$scope.dataTemplate.TipoReporte		= $scope.solCir.ConsultaArchivoNormativa.view()[0].TipoArchivo;
						$scope.dataTemplate.IdReporte		= $scope.solCir.ConsultaArchivoNormativa.view()[0].IdArchivo;
						$scope.dataTemplate.NombreReporte	= $scope.solCir.ConsultaArchivoNormativa.view()[0].NombreArchivo;
						$scope.dataTemplate.ServicioReporte	= $scope.solCir.ConsultaArchivoNormativa.view()[0].ServicioArchivo;

						
						$scope.dataTemplate.dataSource	= $scope.dataTemplate.ServicioReporte+"(p_IdEmpresa =>"+$scope.$root.SesionIdEmpresa+",p_FechaOperacionDesde =>'" + util.dateSPdate(pvEBox.FecInicio) + "',p_FechaOperacionHasta =>'" +  util.dateSPdate(pvEBox.FecTermino)+"',p_TipoInformacion =>'" + pvEBox.opcionFiltro +"',p_IdClasificacionTipoInstrumento =>'" + clasificacion + "',p_out_TablaTransac => ?)";
						$scope.solCir.getReport($scope.dataTemplate);
						
									
					}
				});
			
	 };		
      
		

	 pvEBox.solicitar = function() {
		var fechaInicio = util.dateOdata(util.date(pvEBox.FecInicio).hours(0).minutes(0).seconds(0).toDate());

		var respuesta = {};

		if($scope.operacionCompleta()){
			console.log("Todos los atributos de las fechas son válidos.");
		} else {
			var mensajeTotal = new Array();
			$scope.operacionesConError.forEach(function(numeroOperacion) {
				var _objMensaje = {
							Id : 1,
							tipo : 'ERROR',
							codigo : '',
							descripcion : "Falta informacion para la Operacion Nº: " + $scope.operacionesConError[0]
					};
				mensajeTotal.push(_objMensaje);
			});
			respuesta.P_OUT_MENSAJES = mensajeTotal;
			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_PROBLEMAS_EMITIR_ARCHIVO"),conflictos:util.parseServerResponse(respuesta)},idVentana);
			bcsComunes.ventanaOverlay(idVentana,false);
			return;
		}
		
		if ($scope.existeFecha(pvEBox.FecInicio)) {
			var parametrosConsulta = {
				FechaDesde: fechaInicio,
			};

			var parametrosConsultaCodigoCorredora = {
				IdEmpresa: idEmpresa,
			};

			$scope.codCorredora = [];
			$scope.CodigoCorredoraDataSource = dataSourceCodigoCorredora();
			$scope.CodigoCorredoraDataSource.fetch(function() {
				var data = this.data();
				$scope.$apply(function() {
					pvEBox.CodigoCorredora = data;
				});
			});
		
			dataSourceFechasSolicitar().query({ data: parametrosConsulta }).then(function() {
					
						parametrosSP.p_IdEmpresa 				=  $scope.SesionIdEmpresa;
						parametrosSP.p_IdUsuario              	=  $scope.SesionIdUsuario;
						parametrosSP.p_IdModulo 				=  $scope.solCir.modulo;
						//Especificos
						parametrosSP.p_FechaInicio 				=  util.dateSPdate(pvEBox.FecInicio );
						parametrosSP.p_FechaTermino 		    =  util.dateSPdate(pvEBox.FecTermino ) ;
			
						
						var fh = util.dateSPdate(pvEBox.FecInicio);
						
						var fechaNom = moment(pvEBox.FecInicio).format('MM-DD');	
						fechaNom =  fechaNom.replace("-","").replace("-","");

						var codigoCorredoraNom = pvEBox.CodigoCorredora[0].CodigoCorredora;

						//Creacion Documento
						parametrosSP.p_IdTipoDocumento = pvEBox.IdTipoDocumento;
			
						parametrosSP.p_TablaParametros = params.p_TablaParametrosAux;
						
						parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'FINI' , Valor : fh });

						parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'FTER' , Valor : fh });
						
						var nomarch=  'PA' + codigoCorredoraNom + fechaNom;

						nomarch= nomarch.replace("-","").replace("-","");
						
						parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'NOMSAL' , Valor : nomarch});
						
						parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'NOMCOLSAL' , Valor : 'N'});

							
								
						bcsComunes.msg('info', 'Carga', 'En proceso...');
						bcsServiciosNormativa.CircularCrearSolicitud(parametrosSP,successCrearSolicitud,errorCrearSolicitud);	
			}); 
		} else {

			var fechaFormateada = new Date(pvEBox.FecInicio).toLocaleDateString('es-CL', {
				day: '2-digit',
				month: '2-digit',
				year: 'numeric'
			});
	
			// Mostrar mensaje de error con la fecha formateada
			//bcsComunes.msg('ADVERTENCIA',$translate.instant("UI_ERROR"), $translate.instant("NO_HAY_OPERACIONES"));
			bcsComunes.msg("error","error",$translate.instant('NO_HAY_OPERACIONES'));	
			
		}
	}
	     
		 

		 ModeloFechas = function() {
			return {
				model: {
					fields: {
						Fecha: { type: "date" }						
											}
				}
			};
		};
		
		// DataSource para las fechas
		dataSourceFechas = function() {
			return new bcs.DataSource({
				type : "odata", 
				definition : {
					serverFiltering : true, 
					serverPaging : true, 
					serverSorting : true, 
					transport : {
						read : {
							url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=2470",
							dataType : "json"
						}
					},
				schema: ModeloFechas(),
				}
			});
		};
		
		
		$scope.fechas = [];

		$scope.existeFecha = function(fechaBuscada) {
			return new Promise(function(resolve, reject) {
				var fechasDataSource = dataSourceFechas();
				
				fechasDataSource.fetch(function() {
					var data = this.data();
					$scope.$apply(function() {
						$scope.fechas = data;
		
						var fechaBuscadaStr = util.dateSPdate(fechaBuscada);
		
						var existeFecha = $scope.fechas.some(function(fecha) {
							var fechaStr = fecha.FechaOperacion;
							return fechaStr <= fechaBuscadaStr;
						});
		
						resolve(existeFecha);
					});
				});
			});
		};

		$scope.operacionesConError = [];
		$scope.operacionCompleta = function() {
			$scope.operacionesConError = [];
			$scope.fechas.forEach(function(fecha) {
				var faltanDatos = false;
				// Iterar sobre cada propiedad del objeto fecha
				for (var propiedad in fecha) {
					if (propiedad !== "Fecha") {
						if (fecha[propiedad] === null || fecha[propiedad] === '' || fecha[propiedad] === undefined) {
							faltanDatos = true;
							break;
						}
					}
				}
				if (faltanDatos && new Date(fecha.FechaOperacion) < pvEBox.FecInicio) {
					$scope.operacionesConError.push(fecha.NumeroOperacion);
				}
			});
			// Devolver true si no hay operaciones con error, de lo contrario, false
			return $scope.operacionesConError.length === 0;
		};

	
		pvEBox.visorCircular = function(idVentana) {
			var fechaSeleccionada = pvEBox.FecInicio;
					
			if ($scope.existeFecha(fechaSeleccionada)) {
				$rootScope.fechaSeleccionadaParaGrilla = util.dateSPdate(pvEBox.FecInicio);

				bcsComunes.ventanaOverlay(idVentana, true);
				$scope.accionBoton = false;
				var ventanaModalId = bcsComunes.ventanaAbrir("ConsultarPactosVigentesEBox" );
		
				var promesa = bcsComunes.ventanaPromesa(ventanaModalId);
				$.when(promesa).then(function() {
					bcsComunes.ventanaOverlay(idVentana, false);
				});
			} else {
				    // Formatear la fecha para mostrarla en el mensaje de error
					var fechaFormateada = new Date(fechaSeleccionada).toLocaleDateString('es-CL', {
						day: '2-digit',
						month: '2-digit',
						year: 'numeric'
					});
			
					// Mostrar mensaje de error con la fecha formateada
					//bcsComunes.msg('ADVERTENCIA',$translate.instant("UI_ERROR"), $translate.instant("NO_HAY_OPERACIONES"));
					bcsComunes.msg("error","error",$translate.instant('NO_HAY_OPERACIONES'));
				}
			
		};
		
$scope.pvEBox.clear();
			
	});
}());