(function() {
    angular.module('ModuloNormativa').controller('PanelCIRCMFCtrl', function($scope,$timeout,Model,Utils,$translate,bcsComunes,$compile,bcsServiciosNormativa, bcsVisorCircularConfig,$rootScope){

        var pctCMF = this;
        pctCMF.idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
        pctCMF.domVentana = $("[ventanaid='" + pctCMF.idVentana + "']");
        pctCMF.IdVersionEsquema = '';
        pctCMF.IdTipoDocumento = 'CIRCMF';
        $scope.dataTemplate = {};
		var idVentana = bcsComunes.ventanaWinId($scope);
		var paramNeg = 'N';
		var existenCarNeg = 'N';
		
		pctCMF.opcionFiltro='AM';
	
				
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
		  ModeloSolicitar = function() {
			return {
				model: {
					fields: {
						Fecha: { type: "date" },						
						IdClasificacionTipoInstrumento: { type: "string" },
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
					pageSize : 100,
					transport : {
						read : {
							url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010104",
							dataType : "json"
						}
					},
				schema: ModeloSolicitar(),
				}
			});
		};
	
		 // DataSource para parametro Carteras Negativas
		 ModeloCarteraNegativa = function() {
			return {
				model: {
					fields: {						
						Valor: { type: "string" },
						Fecha: { type: "date" }, // se incluye las fechas donde hay carteras negativas. Luego se preguntará si al momento de generar existen carteras negativas para esa fecha solicitada
					}
				}
			};
		};

		dataSourceCarteraNegativa = function() {
			return new bcs.DataSource({
				type : "odata", 
				definition : {
					serverFiltering : true, 
					serverPaging : true, 
					serverSorting : true, 
					pageSize : 1000,
					transport : {
						read : {
							url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010110",
							dataType : "json"
						}
					},
				schema: ModeloCarteraNegativa(),
				}
			});
		};
		

		/*********************************************
		 * FUNCIONES
		 ********************************************/

		$scope.pctCMF.clear = function(){
			pctCMF.FecInicio 	 = util.dateInicioDia($scope.SesionFechaSistema);
			pctCMF.FecTermino 	 = util.dateInicioDia($scope.SesionFechaSistema);
			
		};
		var successCrearSolicitud = function(respuesta)
		{	
			if (respuesta.P_OUT_ERRORESCONTADOR == 0)
			{
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				bcsComunes.msg("info","info",'Solicitud creada con éxito');
				IdSolicitudCircular =respuesta.P_OUT_RESULTADO;
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
			bcsComunes.ventanaOverlay(pctCMF.idVentana, false);
			bcsComunes.ventanaData(pctCMF.idVentana, respuesta);
			bcsComunes.ventanaCerrar(pctCMF.idVentana);
			
		};
		
		var errorCrearSolicitud = function(respuesta)
		{	
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
			bcsComunes.msg("error","error",$translate.instant('INGRESO ERROR'));
			bcsComunes.ventanaOverlay(pctCMF.idVentana, false);
		};
		$scope.imprimirCircular = function() {
			var clasificacion;
		   if (pctCMF.IdClasificacionTipoInstrumento!== 0) 
		   {clasificacion =  pctCMF.IdClasificacionTipoInstrumento;}
		   else{
			   clasificacion='';
		   };
		
				var filterTemplate = [ { field: "IdTipoDocumento",operator:"eq", value: pctCMF.IdTipoDocumento },
									   { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
									   { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
				$scope.solCir.ConsultaArchivoNormativa.query({ filter: filterTemplate }).then(function(){
					if($scope.solCir.ConsultaArchivoNormativa.view().length > 0) {
						$scope.dataTemplate.TipoReporte		= $scope.solCir.ConsultaArchivoNormativa.view()[0].TipoArchivo;
						$scope.dataTemplate.IdReporte		= $scope.solCir.ConsultaArchivoNormativa.view()[0].IdArchivo;
						$scope.dataTemplate.NombreReporte	= $scope.solCir.ConsultaArchivoNormativa.view()[0].NombreArchivo;
						$scope.dataTemplate.ServicioReporte	= $scope.solCir.ConsultaArchivoNormativa.view()[0].ServicioArchivo;
						
						$scope.dataTemplate.dataSource	= $scope.dataTemplate.ServicioReporte+"(p_IdEmpresa =>"+$scope.$root.SesionIdEmpresa+",p_FechaOperacionDesde =>'" + util.dateSPdate(pctCMF.FecInicio) + "',p_FechaOperacionHasta =>'" +  util.dateSPdate(pctCMF.FecTermino)+"',p_TipoInformacion =>'" + pctCMF.opcionFiltro +"',p_IdClasificacionTipoInstrumento =>'" + clasificacion + "',p_out_TablaTransac => ?)";
						$scope.solCir.getReport($scope.dataTemplate);					
									
					}
				});			
	 };	

	pctCMF.solicitar = function() {
		var fechaInicio = util.dateOdata(util.date(pctCMF.FecInicio).hours(0).minutes(0).seconds(0).toDate());
		var clasificacion = pctCMF.IdClasificacionTipoInstrumento !== 0;
	
		var parametrosConsulta = {
			FechaDesde: fechaInicio,
			IdClasificacionTipoInstrumento: clasificacion
		};
	
		// Consulta de carteras negativas
		$scope.fechasCarNegativasDataSource.fetch(function() {
			var data = this.data();
			var existeCarterasNegativas = $scope.existeCarterasNegativas(pctCMF.FecInicio);
	
			// Obtenemos el valor del primer registro, asumiendo que es el mismo para todos
			var valor = data.length > 0 ? data[0].Valor : 'N';
				dataSourceFechasSolicitar().query({ data: parametrosConsulta }).then(function() {
					parametrosSP.p_IdEmpresa = $scope.SesionIdEmpresa;
					parametrosSP.p_IdUsuario = $scope.SesionIdUsuario;
					parametrosSP.p_IdModulo = $scope.solCir.modulo;
					
					// Específicos
					parametrosSP.p_FechaInicio = util.dateSPdate(pctCMF.FecTermino);
					parametrosSP.p_FechaTermino = util.dateSPdate(pctCMF.FecTermino);
					var fh = util.dateSPdate(pctCMF.FecInicio);
	
					// Creación Documento
					parametrosSP.p_IdTipoDocumento = pctCMF.IdTipoDocumento;
					parametrosSP.p_TablaParametros = params.p_TablaParametrosAux;
					
					parametrosSP.p_TablaParametros.push({ Tipo: 'SOLICITUD', NombreParametro: 'FINI', Valor: fh });
					parametrosSP.p_TablaParametros.push({ Tipo: 'SOLICITUD', NombreParametro: 'FTER', Valor: fh });
	
					var nomarch = 'IVCR' + util.dateSPdate(pctCMF.FecInicio);
					nomarch = nomarch.replace("-", "").replace("-", "");
	
					parametrosSP.p_TablaParametros.push({ Tipo: 'SOLICITUD', NombreParametro: 'NOMSAL', Valor: nomarch });
					parametrosSP.p_TablaParametros.push({ Tipo: 'SOLICITUD', NombreParametro: 'NOMCOLSAL', Valor: 'N' });
	
					// Mostrar mensaje de carga y realizar la solicitud
					bcsComunes.msg('info', 'Carga', 'En proceso...');
					bcsServiciosNormativa.CircularCrearSolicitud(parametrosSP, successCrearSolicitud, errorCrearSolicitud);
				});
		
		});
	};		 

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
							url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010104",
							dataType : "json"
						}
					},
				schema: ModeloFechas(),
				}
			});
		};		
		
		$scope.fechas = [];
		$scope.fechasDataSource = dataSourceFechas();
		$scope.fechasDataSource.fetch(function() {
			var data = this.data();
			$scope.$apply(function() {
				$scope.fechas = data;
			});
		});		
		
		$scope.existeFecha = function(fechaBuscada) {            
            var fechaBuscadaStr = util.dateSPdate(fechaBuscada);
			
			var existeFecha = $scope.fechas.some(function(fecha) {
                var fechaStr = util.dateSPdate(fecha.Fecha);
				return fechaStr === fechaBuscadaStr;
			});
		
			return existeFecha;
		};

		//carteras negativas
		$scope.fechasCarNegativas = [];
		$scope.fechasCarNegativasDataSource = dataSourceCarteraNegativa();
		$scope.fechasCarNegativasDataSource.fetch(function() {
			var data = this.data();
			$scope.$apply(function() {
				$scope.fechasCarNegativas = data;
			});
		});

		$scope.existeCarterasNegativas = function(fechaBuscada) {            
            var fechaBuscadaStr = util.dateSPdate(fechaBuscada);
			var existeCarterasNegativas = $scope.fechasCarNegativas.some(function(fecha) {
                var fechaStr = util.dateSPdate(fecha.Fecha);
				return fechaStr === fechaBuscadaStr;
			});
		
			return existeCarterasNegativas;
		};
	
		pctCMF.visorCircular = function(idVentana) {
			var fechaSeleccionada = pctCMF.FecInicio;
			
			if ($scope.existeFecha(fechaSeleccionada)) {
				$rootScope.fechaSeleccionadaParaGrilla = util.dateSPdate(pctCMF.FecInicio);

				bcsComunes.ventanaOverlay(idVentana, true);
				$scope.accionBoton = false;
				var ventanaModalId = bcsComunes.ventanaAbrir("ConsultarCMF" );
		
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
					alert('No existen Registros para la fecha ' + fechaFormateada);
				}
			
		};	
		
$scope.pctCMF.clear();
			
	});
}());