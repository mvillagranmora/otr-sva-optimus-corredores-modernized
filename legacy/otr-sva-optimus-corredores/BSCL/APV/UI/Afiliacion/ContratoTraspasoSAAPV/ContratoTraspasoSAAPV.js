(function() {
	angular.module('ModuloAPV').controller('ContratoTraspasoSAAPVCtrl',
	function($compile,$scope,$translate,$resource,$timeout,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,bcsConsultasAPV,bcsServiciosAPV)
	{
/******************************************************/
/*** INICIO DE VARIABLES Y PRECARGA					***/
/******************************************************/
		
		//Obtiene datos desde la ventana principal 
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var parametrosVentana = bcsComunes.ventanaParams(idVentana);
		var DocumentoIdentidadClienteM 		= '';
		var DocumentoIdentidadEmpleadorM	= '';
		var contadorHabilita = 0;
		var varIdPosicionInicial;
		var IdCiudad = 0;
		var IdComuna = 0;
		
		var ctrasp = this;
		ctrasp.kOptFechaSuscripcion = {};
		ctrasp.kOptFechaSuscripcion.max = new Date();
		
		ctrasp.parametrosEntrada = bcsComunes.ventanaParams(idVentana);
		ctrasp.limpiaDistribucion = false;
		ctrasp.limpiarClienteHabilitado = false;
		ctrasp.paramVentanaDistribuir = {}
		ctrasp.validaPortafolio = false;
		
		$scope.data = {};
		ctrasp.datosForm = {};

		ctrasp.paramModificaDistribucion	= 0;
		ctrasp.IdPersona					= 0;
		ctrasp.IdCliente					= 0;
		ctrasp.IdEmpleador					= 0;
		ctrasp.IdContratoSAAPV 				= 0;
		ctrasp.IdPortafolioExiste 			= 0;
		//ctrasp.kngFechaSuscripcion 			= util.dateInicioDia($scope.SesionFechaSistema);
		$scope.kOptFechaNacimiento 			= {};
		$scope.kOptFechaNacimiento.max		= new Date(); //$scope.$root.SesionFechaSistema;
		$scope.data.bloqueoCliente 			= true;	//BLOQUEA ZONA PERSONA
		$scope.fechaActual 					= new Date();
		
		ctrasp.TipoSolicitudIdActual		= 'TRA';
		ctrasp.TipoSolicitudIdPostFiltro	= 'TRA';
		ctrasp.OrigenTraspasoIdActualPost = 'ING';
		ctrasp.OrigenTraspasoIdActual = 'ING';
		ctrasp.IdPaisResidenciaIdActualPost = 44;
		
		ctrasp.habilitoTipoSolicitud = true;
		
		$scope.ctrasp.petrasp = {};
		ctrasp.ValoresPanelDefecto = function(){
			/*TAB EMPLEADOR*/
			$scope.ctrasp.petrasp.RazonSocial = "";	    
			$scope.ctrasp.petrasp.DocumentoIdentidadEmpleador = "";
			$scope.ctrasp.petrasp.comunaEmpleadorSeleccionada = "";
			$scope.ctrasp.petrasp.DireccionComercialEmpleador = "";
			$scope.ctrasp.petrasp.kngNumeroDireccionEmpleador = 0;
			$scope.ctrasp.petrasp.comunaEmpleadorSeleccionada.IdCiudad = 0;
			$scope.ctrasp.petrasp.comunaEmpleadorSeleccionada.IdDivisionPais = 0;  
			$scope.ctrasp.petrasp.TelefonoEmpleador = ""; 
			$scope.ctrasp.petrasp.NombreEncargado = "";
			$scope.ctrasp.petrasp.EmailEncargado = "";
			$scope.ctrasp.petrasp.TelefonoEncargado = "";
			$scope.ctrasp.petrasp.IdMedioRecaudacionTrabajador = 0;
			$scope.ctrasp.petrasp.entidadRecaudadora = 0;
			$scope.ctrasp.petrasp.entidadRecaudadora = "";
			$scope.ctrasp.petrasp.pensionadoSeleccion = "";
			$scope.ctrasp.petrasp.trabajadorSeleccion = "";
			$scope.ctrasp.petrasp.DiaDescuentoIdActual = 0;
			$scope.ctrasp.petrasp.kngComisionTransferencia = 0
			$scope.ctrasp.petrasp.NombreEntidadOrigen = "";
			$scope.ctrasp.petrasp.RutEntidadOrigen = "";
			$scope.ctrasp.petrasp.seleccionadotipoAhorro = "";
			$scope.ctrasp.petrasp.fondoOrigen = "";
			$scope.ctrasp.petrasp.tipoTraspaso = "";
			$scope.ctrasp.petrasp.kngMontoTraspasoPeso = 0;
			$scope.ctrasp.petrasp.kngUFTraspaso = 0;
			$scope.ctrasp.petrasp.kngporcentajeTraspaso = 0;
		};
		ctrasp.ValoresPanelDefecto();

		var datosEmpleador= Array ();
		var datosPortafolio= Array ();
		var ParametrosModificados = Array();
		
		/******************************************************/
		/*** FILTROS										***/
		/******************************************************/
		
		ctrasp.domVentana = $("[ventanaid='"+idVentana+"']");
		ctrasp.datosEmpleador= {};	
		ctrasp.datosPortafolio= [];	
		ctrasp.tabs = { 				
				PanelEmpleadoContratoTraspaso	: { id : 'PanelEmpleadoContratoTraspaso',	url : '<div ng-include = "\'../../APV/UI/Afiliacion/ContratoTraspasoSAAPV/EMP/PanelEmpleadoContratoTraspaso.html\'" ></div>'	, active : false,  cargado: false, datosCambiados: false },
				PanelPortafolioContratoTraspaso	: { id : 'PanelPortafolioContratoTraspaso',	url : '<div ng-include = "\'../../APV/UI/Afiliacion/ContratoTraspasoSAAPV/POR/PanelPortafolioContratoTraspaso.html\'" ></div>'	, active : true,  cargado: false, datosCambiados: false },
			
		};
		
		ctrasp.fnTabSelect = function(ambito,nombreTab)
		{
			if (ctrasp.tabs[nombreTab].cargado !== true)
			{
				ctrasp.tabs[nombreTab].cargado = true;
				ctrasp.domVentana.find('#'+ctrasp.tabs[nombreTab].id).append($compile(ctrasp.tabs[nombreTab].url)($scope));
			}
		};
		
		ctrasp.reagimenTributarioFitro =
		{
				filtros : {
					IdEstado : {field: "IdEstado", operator: "eq", value: "VIG"},
					Codigo	 : {field: "Codigo", operator: "contains", value: "APV"}
				}
		};
		
		ctrasp.oConfPerfilComercial=
		{
				tipo 	: 'combobox-perfil-comercial', 
				filtros : { 
					IdCategoriaInversionista : {
							"field"   : "IdCategoriaInversionista",
							"operator": "eq",
							"value"   : "" }},
		};
		
		ctrasp.filtroCiudad = 
		{
			IdPais : {field: "IdPais", operator: "eq", value: ""}
		};
				
		ctrasp.filtroDivisionPais =
		{
			IdPais   : {field: "IdPais"  , operator: "eq" , value: -1},//$scope.$root.SesionIdPaisSistema},
			IdCiudad : {field: "IdCiudad", operator: "neq", value: 0} //no pueden haber divisiones sin ciudad!
		};	
		
		ctrasp.filtroEjecutivo = {
			IdEmpresa : {field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa},
			IdEstado  : {field: "IdEstado" , operator: "eq", value: 'ACT'}
		};
		/******************************************************/
		/*** DATASOURCES									***/
		/******************************************************/
		
		var  datosSAAPV = 
		{
			type : "odata",
			definition : {
				serverFiltering : true, // Filtrado de datos en el servidor.
				serverPaging    : true, // Paginado de los datos en el servidor.
				serverSorting   : true, // Ordenamiento de los datos en el servidor.
				sort 			: { field: "IdContratoSAAPV", dir: "DESC" },
				pageSize        : 10,
				transport		: {
					read: {
						url 		: bcsConsultasAPV('ConsultarGestionContratosSAAPV'),
						dataType	: "json"
					}
				},
                schema: { 
                    model: {
						fields: {													
							"IdContratoSAAPV"				: {type: "number"},
							"FolioContratoSAAPV" 			: {type: "string"},
							"IdPlan" 						: {type: "number"},
							"NombrePlan" 					: {type: "string"},
							"FechaSuscripcion" 				: {type: "sysdate"},
							"FechaCreacion" 				: {type: "sysdate"},
							"FechaAprobacion" 				: {type: "sysdate"},
							"FechaNotificacion" 			: {type: "sysdate"},
							"FechaRecepcion"				: {type: "sysdate"},
							"Folio"							: {type: "string"},
							"IdEnrolamiento"				: {type: "number"},
							"IdPortafolio"					: {type: "number"},
							
							/*Cliente*/
							"IdPersona" 					: {type: "number"},
							"DocumentoIdentidadCliente"		: {type: "string"},
							"NombrePersona"					: {type: "string"},
							"Tipo"							: {type: "string"},
							"IdCliente"						: {type: "number"},
							"FechaNacimientoCliente"		: {type: "sysdate"},
							"EstadoCivil"					: {type: "string"},
							"Genero"						: {type: "string"},
							"DireccionCliente"				: {type: "string"},													
							"TelefonoFijo"					: {type: "string"},
							"TelefonoMovil"					: {type: "string"},
							"TelefonoLaboral"				: {type: "string"},
							"EmailCliente"					: {type: "string"},
							"IdEmpleadoEjecutivo"			: {type: "number"},
							"NombrePersonaEjecutivo"		: {type: "string"},
							/*Empleador*/
							"IdEmpleador" 					: {type: "number"},
							"DocumentoIdentidadEmpleador" 	: {type: "string"},
							"NombreEmpleador" 				: {type: "string"},												
							"DireccionComercialEmpleador"	: {type: "string"},
							"IdComunaEmpleador"				: {type: "number"},
							"TelefonoEmpleador"				: {type: "string"},
							"NombreEncargado"				: {type: "string"},
							"EmailEncargado"				: {type: "string"},
							"TelefonoEncargado"				: {type: "string"},
							
							"IdMedioRecaudacionTrabajador"	: {type: "number"},
							"IdEntidadRecaudadora"			: {type: "number"},
							"CodigoPrevision"				: {type: "number"},
							"Pensionado"		        	: {type: "string"},
							"Trabajador"					: {type: "string"},
							"OrigenAhorro" 					: {type: "string"},
							"MontoPesos" 					: {type: "number"},
							"MontoUF"						: {type: "number"},
							"PorcentajeRegimenImponible"	: {type: "number"},
							"Pago" 							: {type: "string"},
							"PeriodoInicio" 				: {type: "string"},
							"PeriodoTermino"				: {type: "string"},
							"RegimenTributario" 			: {type: "string"},
							"FormaDePago"					: {type: "string"},
							"IdMedioRecaudacionFormaPago"   : {type: "number"},
							"CodigoMedioRecaudacion"		: {type: "string"},
							"NombreMedioRecaudacion"		: {type: "string"},
							"DiaDescuento" 					: {type: "number"},
							"Institucion" 					: {type: "string"},
							"ComisionTransferencia" 		: {type: "number"},
							"NombreEntidadOrigen" 			: {type: "string"},
							"RutEntidadOrigen"				: {type: "string"},
							"TipoDeAhorro" 					: {type: "string"},
							"FondoDeOrigen" 				: {type: "string"},
							"TipoTraspaso"					: {type: "string"},
							"MontoTraspaso" 				: {type: "number"},
							"UFTraspaso"					: {type: "number"},
							"PorcentajeTraspaso"			: {type: "number"},
							"IdEstado"						: {type: "string"},
							"IdMatrizAgrupadorAPV"			: {type: "number"},
							"Proposito"						: {type: "string"},
							"IdProductoAgrupador"			: {type: "number"},
							"CodigoAgrupador"				: {type: "string"}
						}
					}
				}
    		}
		};
		
		var datosFolioContrato = 
		{
			type : "odata",
			definition : 
			{
                serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                sort 			: { field: "IdContratoSAAPV", dir: "DESC" },
                pageSize        : 10,
                transport		: {
					read: {
						url 		: bcsConsultasAPV('ConsultarFolioContratoSAAPV'),
						dataType	: "json"
					}
				},
                schema: { 
                	model: {
						fields : {													
							"IdContratoSAAPV"		: {type: "number"},
							"FolioContratoSAAPV" 	: {type: "string"},
							"IdCliente" 			: {type: "number"},
							"IdPersona" 			: {type: "number"}
						}
					}
				}
			 }
		};
		
		var datosConsultarTrabajador = 
		{
			type : "odata",
			definition : 
			{
                serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                sort 			: { field: "IdPersona", dir: "DESC" },
                pageSize        : 10,
                transport		: {
					read: {
						url 		: bcsConsultasAPV('ConsultarTrabajadorSAAPV'),
						dataType	: "json"
					}
				},
		        schema: {
		         	model: {
		           		fields: {
							IdPersona					: {type: "number"},
							IdDocumentoIdentidad		: {type: "number"},
							TipoPersona					: {type: "string"},
							DocumentoIdentidad			: {type: "string"},
							Nombres						: {type: "string"},
							ApellidoPaterno         	: {type: "string"},
							ApellidoMaterno         	: {type: "string"},
							NombreCompleto          	: {type: "string"},
							FechaNacimiento				: {type: "sysdate"},
							IdEmpleadoEjecutivo			: {type: "number"},
							NombrePersonaEjecutivo		: {type: "string"},
							EstadoCivil					: {type: "string"},
							Genero						: {type: "string"},
							IdPaisNacimiento			: {type: "number"},
							PaisNacimiento				: {type: "string"},
							IdPaisResidencia			: {type: "number"},
							PaisResidencia				: {type: "string"},
							IdCiudad					: {type: "number"},
							NombreCiudad				: {type: "string"},
							IdComuna					: {type: "number"},
							NombreComuna				: {type: "string"},
							DireccionCliente			: {type: "string"},
							NumeroDirecionCliente		: {type: "string"},
							IdProfesion					: {type: "number"},
							Cargo						: {type: "string"},
							IdCategoriaInversionista    : {type: "string"},
							IdPerfilComercial           : {type: "string"},
							TelefonoFijo             	: {type: "string"},
							TelefonoMovil    			: {type: "string"},
							TelefonoLaboral      		: {type: "string"},
							DireccionElectronica        : {type: "string"}
						}
					}
	            }
			}
		};
		
		var datosConsultarEmpleador =
		{
			type : "odata",
			definition : 
			{
                serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                sort 			: { field: "IdPersona", dir: "DESC" },
                pageSize        : 10,
                transport		: {
					read: {
						url 		: bcsConsultasAPV('ConsultarEmpleadorSAAPV'),
						dataType	: "json"
					}
				},
		        schema: {
		         	model: {
		           		fields: {
							IdPersona					: {type: "number"},
							IdDocumentoIdentidad		: {type: "number"},
							TipoPersona					: {type: "string"},
							DocumentoIdentidadEmpleador	: {type: "string"},
							RazonSocialEmpleador		: {type: "string"},
							IdComunaEmpleador			: {type: "number"},
							DireccionEmpleador			: {type: "string"},
							NumeroDireccionEmpleador	: {type: "string"},
							TelefonoEmpleador			: {type: "string"},
							NombreEncargado				: {type: "string"},
							EmailEncargado				: {type: "string"},
							TelefonoEncargado			: {type: "string"}
						}
					}
	            }
			}
		};
		
		var defDistribucion = 
		{
			type : "odata",
			definition : {
				serverFiltering : true, // Filtrado de datos en el servidor.
				serverPaging    : true, // Paginado de los datos en el servidor.
				serverSorting   : true, // Ordenamiento de los datos en el servidor.
				pageSize        : 100,
				sort 			: false,
				transport		: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6010",
						dataType	: "json"
					}
				},
				schema: {
					model: {
						fields: {
							FolioContratoSAAPV  		: {type: "strng"},
							IdInstrumento     			: {type: "number"},
							RazonSocial					: {type: "string"},
							Nemotecnico					: {type: "string"},
							MontoPesos					: {type: "number"},
							PorcentajeRegimenImponible	: {type: "float"},
							IdEstado					: {type: "string"}
						}
					}
				}
			}
		};
		
		var datosCiudad = 
		{
			type : "odata",
			definition : 
			{
                serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                pageSize        : 10,
                transport		: {
					read: {
						url 		: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1627",
						dataType	: "json"
					}
				},
		        schema: {
		         	model: {
		           		fields: {
							IdCiudad			: {type: "number"},
							NombreCiudad		: {type: "string"},
							NombreCiudadPais	: {type: "string"},
							IdPais				: {type: "number"},
							NombrePais			: {type: "string"},
							CodigoPais			: {type: "string"}
						}
					}
	            }
			}
		};

		/******************************************************/
		/*** FUNCIONES										***/
		/******************************************************/
		
		$scope.data.buscarCliente = function() {
			bcsComunes.ventanaOverlay($scope.ventanaId,false);
			$scope.etapa=='buscar';
			if($scope.$root.SesionIdPaisSistema == 44){
				if(!util.validarRut($scope.searchData.NumeroDocumentoIdentidad).valid){
					bcsComunes.msg("error",'Error',$translate.instant('UI_RUT_INVALIDO'));
					return;
				}
				if(!$scope.data.NumeroDocumentoIdentidad || $scope.searchData.NumeroDocumentoIdentidad.length==0){
					bcsComunes.msg("error",'Error',$translate.instant('UI_DATOS_INCOMPLETOS'));
					return;
				}
			}
			bcsComunes.ventanaOverlay($scope.ventanaId,true);
			var urlConsultarEntidadEnrolamiento = "/BSCL/Plataforma/GWJAVA/PLA/call?t=53";
			var r = $resource("", {}, {
				'ConsultarEntidadEnrolamiento': { method: "POST", url: urlConsultarEntidadEnrolamiento}
			});
			
			var datosConsulta = {
				p_IdEmpresa 				: $scope.SesionIdEmpresa,
				p_IdUsuario					: $scope.SesionIdUsuario,
				p_IdModulo					: 'CLI',
				p_UsaReglasNegocio			: 'S',
				p_IdTipoEntidad				: 'CLIENTE',
				p_IdTipoDocumentoIdentidad	: $scope.searchData.IdTipoDocumentoIdentidad,
				p_DocumentoIdentidad		: $scope.searchData.NumeroDocumentoIdentidad,
				p_IdTipoDocumentoIdentidad	: $scope.searchData.IdTipoDocumentoIdentidad,
				p_DocumentoIdentidad		: $scope.searchData.NumeroDocumentoIdentidad,
				p_IdTipoPersona 			: $scope.recordData.tipoPersona,
				p_esRutInterno 				: 'N'
			};
			
			var post = r.ConsultarEntidadEnrolamiento(datosConsulta);
			post.$promise.then(function(result){
				bcsComunes.ventanaOverlay($scope.ventanaId,false);
				$scope.etapa='';
				//ERROR
				if (result.P_OUT_PERSONAEXISTE === 'S' && result.P_OUT_ENTIDADEXISTE === 'S') {
					bcsComunes.ventanaMsgErr($translate.instant('UI_CLIENTE_EXISTE'),$scope.ventanaId);
				}
				//EXISTE SOLO PERSONA
				else if(result.P_OUT_PERSONAEXISTE === 'S' && result.P_OUT_ENTIDADEXISTE === 'N') {
					if(result.P_OUT_CONSULTARENROLAMIENTO["0"]) {
						var dataIN = result.P_OUT_CONSULTARENROLAMIENTO["0"];
						$scope.recordData = {
							apellidoMaterno 				: dataIN.ApellidoMaterno,
							apellidoPaterno 				: dataIN.ApellidoPaterno,
							idCategoriaInversionistaDefecto : dataIN.CLI_IdCategoriaInversionista,
							idCategoriaInversionista 		: dataIN.CLI_IdCategoriaInversionista,
							idTipoCliente 					: dataIN.CLI_IdTipoCliente,
							idClaseCliente 					: dataIN.CLI_IdClaseCliente,
							direccionCalle 					: dataIN.DireccionCalle,
							direccionNumero					: dataIN.DireccionNumero,
							direccionDepartamento 			: dataIN.DireccionDepartamento,
							email 							: dataIN.Email,
							idEstadoCivil 					: dataIN.EstadoCivil,
							fechaNacimiento 				: util.date(dataIN.FechaNacimiento).toDate(),
							idPaisResidenciaDefecto			: dataIN.IdPais,
							idPaisResidencia				: dataIN.IdPais,
							idCiudadDefecto 				: dataIN.IdCiudad,
							idCiudad 						: dataIN.IdCiudad,
							idDivisionPaisDefecto			: dataIN.IdDivisionPais,
							idDivisionPais 					: dataIN.IdDivisionPais,
							idPaisNacimiento 				: dataIN.IdPaisNacimiento,
							idPaisNacimientoDefecto 		: dataIN.IdPaisNacimiento,
							nombres 						: dataIN.Nombres,
							idPerfilComercial 				: dataIN.POR_IdPerfilComercial,
							razonSocial 					: dataIN.RazonSocial,
							idGenero 						: dataIN.Sexo,
							idTipoTelefono 					: dataIN.IdTipoTelefono,
							idTipoTelefonoDefecto 			: dataIN.IdTipoTelefono,
							telefono 						: dataIN.Telefono,
							tipoPersona 					: dataIN.Tipo,
							idPersonaConyuge 				: dataIN.IdPersonaConyuge,
							RegimenConyugal					: dataIN.RegimenConyugal,
							RegimenConyugalOriginal			: dataIN.RegimenConyugal,
							idEmpleadoEjecutivo				: dataIN.CLI_IdEmpleadoEjecutivo,
							idEmpleadoEjecutivoDefecto		: dataIN.CLI_IdEmpleadoEjecutivo,
							idClaseCliente					: dataIN.CLI_IdClaseCliente,
							idClaseClienteDefecto			: dataIN.CLI_IdClaseCliente,
							codigoAdicionalHolding			: dataIN.CLI_CodigoAdicionalHolding
						};
					}else{
						bcsComunes.ventanaMsgErr($translate.instant('UI_ERROR_CARGAR_CLIENTE'),$scope.ventanaId);
						console.log(JSON.stringify(result));
					}
					$scope.configData.bloqueoBuscador = true;
					$scope.configData.bloqueoPersona = true;
					$scope.configData.bloqueoCliente = false;
					$scope.configData.TipoDocumentoIdentidadBloqueado = true;
				}
				//NO EXISTE PERSONA -> PUEDE HABER BORRADOR
				else if (result.P_OUT_PERSONAEXISTE === 'N' && result.P_OUT_ENTIDADEXISTE === 'N') {
					if (result.P_OUT_CONSULTARENROLAMIENTO["0"]) {
						var dataIN = result.P_OUT_CONSULTARENROLAMIENTO["0"];
						$scope.recordData = {
							apellidoMaterno 				: dataIN.ApellidoMaterno,
							apellidoPaterno 				: dataIN.ApellidoPaterno,
							idCategoriaInversionistaDefecto : dataIN.CLI_IdCategoriaInversionista,
							idCategoriaInversionista 		: dataIN.CLI_IdCategoriaInversionista,
							idTipoCliente 					: dataIN.CLI_IdTipoCliente,
							idClaseCliente 					: dataIN.CLI_IdClaseCliente,
							direccionCalle 					: dataIN.DireccionCalle,
							direccionNumero 				: dataIN.DireccionNumero,
							direccionDepartamento 			: dataIN.DireccionDepartamento,
							email 							: dataIN.Email,
							idEstadoCivil 					: dataIN.EstadoCivil,
							fechaNacimiento 				: util.date(dataIN.FechaNacimiento).toDate(),
							idPaisResidenciaDefecto			: dataIN.IdPais,
							idPaisResidencia				: dataIN.IdPais,
							idCiudadDefecto 				: dataIN.IdCiudad,
							idCiudad 						: dataIN.IdCiudad,
							idDivisionPaisDefecto 			: dataIN.IdDivisionPais,
							idDivisionPais 					: dataIN.IdDivisionPais,
							idPaisNacimiento 				: dataIN.IdPaisNacimiento,
							idPaisNacimientoDefecto 		: dataIN.IdPaisNacimiento,
							nombres							: dataIN.Nombres,
							idPerfilComercial 				: dataIN.POR_IdPerfilComercial,
							razonSocial 					: dataIN.RazonSocial,
							idGenero 						: dataIN.Sexo,
							idTipoTelefono 					: dataIN.IdTipoTelefono,
							idTipoTelefonoDefecto 			: dataIN.IdTipoTelefono,
							telefono 						: dataIN.Telefono,
							tipoPersona 					: dataIN.Tipo,
							idPersonaConyuge 				: dataIN.IdPersonaConyuge,
							RegimenConyugal					: dataIN.RegimenConyugal,
							RegimenConyugalOriginal			: dataIN.RegimenConyugal,
							idEmpleadoEjecutivo				: dataIN.CLI_IdEmpleadoEjecutivo,
							idEmpleadoEjecutivoDefecto		: dataIN.CLI_IdEmpleadoEjecutivo,
							idClaseCliente					: dataIN.CLI_IdClaseCliente,
							idClaseClienteDefecto			: dataIN.CLI_IdClaseCliente,
							codigoAdicionalHolding			: dataIN.CLI_CodigoAdicionalHolding
						};
					}else{
					}
					//BLOQUEAR CONTROLES DE ARRIBA
					$scope.configData.bloqueoBuscador = true;
					$scope.configData.bloqueoPersona  = false;
					$scope.configData.bloqueoCliente  = false;
					$scope.configData.TipoDocumentoIdentidadBloqueado = true;
				}
				//obtener IdSocioDeNegocio para conyuge en caso de existir:
				if(($scope.recordData.idEstadoCivil==='CA'||$scope.recordData.idEstadoCivil==='CC')&&$scope.recordData.idPersonaConyuge>0){
					var filter = { field: "IdPersona", operator: "eq", value: Number($scope.recordData.idPersonaConyuge) }
					var conyugeDatasource=bcsConfiguracionSociodeNegocio.getDatasourceSocioNegocio(filter);
					conyugeDatasource.query().then(function(result){
						if(conyugeDatasource.data().length>0){
							$scope.recordData.IdSocNegPersonaConyuge=conyugeDatasource.data()[0].IdSocioDeNegocio;
							$scope.recordData.IdSocNegPersonaConyugeOriginal=conyugeDatasource.data()[0].IdSocioDeNegocio;
						} 
					});
				}
				
				//Datos por defecto en pantalla!
				if($scope.recordData)  {
					$scope.recordData.idPaisResidenciaDefecto 		= $scope.recordData.idPaisResidenciaDefecto || $scope.$root.SesionIdPaisSistema;
					$scope.recordData.idPaisResidencia 				= $scope.recordData.idPaisResidenciaDefecto || $scope.$root.SesionIdPaisSistema;
					$scope.recordData.idCiudadDefecto 				= $scope.recordData.idCiudadDefecto;
					$scope.recordData.idCiudad 						= $scope.recordData.idCiudadDefecto;
					$scope.recordData.idDivisionPaisDefecto 		= $scope.recordData.idDivisionPaisDefecto;
					$scope.recordData.idDivisionPais 				= $scope.recordData.idDivisionPaisDefecto;
					$scope.recordData.idEmpleadoEjecutivoDefecto	= $scope.recordData.idEmpleadoEjecutivoDefecto;
					$scope.recordData.idEmpleadoEjecutivo		 	= $scope.recordData.idEmpleadoEjecutivoDefecto;
					$scope.recordData.RegimenConyugalOriginal 		= $scope.recordData.RegimenConyugal;
					$scope.recordData.idPaisNacimientoDefecto 		= $scope.recordData.idPaisNacimientoDefecto||$scope.$root.SesionIdPaisSistema;
				}else {
					$scope.recordData = {};
					$scope.recordData.idPaisResidenciaDefecto 	= $scope.$root.SesionIdPaisSistema;
					$scope.recordData.idPaisResidencia 			= $scope.$root.SesionIdPaisSistema;
					$scope.recordData.IdEmpleadoEjecutivo 		= $scope.$root.SesionIdEmpleado;
				}
			},function(result){
				bcsComunes.ventanaOverlay($scope.ventanaId,false);
				$scope.etapa='';
				bcsComunes.ventanaMsgErr($translate.instant('UI_ERROR_CARGAR_BORRADOR'),$scope.ventanaId);
				console.log(JSON.stringify(result));
			});		
		};
		
		ctrasp.DistribuirInversionSAAPV = function(){
			var filterVentana = Array(); 
			filterVentana.modoVentana = ctrasp.parametrosEntrada.modoVentana;
			filterVentana.limpiaDistribucion = ctrasp.limpiaDistribucion;
			
			var MontoTotalPesos = 0;
			var MontoTotalUF = 0;
			
			if($scope.ctrasp.ptrasp.dsOrigenTraspasoAhorro.view().length <= 0){
				bcsComunes.ventanaMsgErr('Debe ingresar Monto Origen Ahorro.',$scope.ventanaId);
				return;
			}
				
			$.each($scope.ctrasp.ptrasp.dsOrigenTraspaso.view(),function(key,value){
				
				MontoTotalPesos = MontoTotalPesos + value.MontoTraspaso;
				MontoTotalUF =  MontoTotalUF + value.UFTraspaso;
				
			});
			
			if(filterVentana.modoVentana === 'I'){
				filterVentana.IdContratoSAAPV = ctrasp.FolioContrato;
				filterVentana.MontoTotal = MontoTotalPesos; 
				filterVentana.paramVentanaDistribuir = ctrasp.paramVentanaDistribuir;
				filterVentana.IdTipoInstrumento = ctrasp.NombrePlanAhorroSeleccionado.NombrePlanTipoInversion === 'A. ABIERTO' ? "FFMM" : "";
				filterVentana.FolioContratoSAAPV = 0;
			}
			else{				
				
				filterVentana.paramVentanaDistribuir = $scope.ctrasp.ptrasp.dsOrigenTraspaso;				
				filterVentana.IdContratoSAAPV = ctrasp.IdContratoSAAPV;
				filterVentana.FolioContratoSAAPV = ctrasp.FolioBusqueda;
				filterVentana.MontoTotal = MontoTotalPesos;
				filterVentana.paramVentanaDistribuir = ctrasp.paramVentanaDistribuir;
				filterVentana.IdTipoInstrumento = ctrasp.NombrePlanAhorroSeleccionado.NombrePlanTipoInversion === 'A. ABIERTO' ? "FFMM" : "";
				filterVentana.paramModificaDistribucion;
			}
			
			var IdVentanamodal = bcsComunes.ventanaAbrir("DistribuirInversionSAAPV", filterVentana, true, false, idVentana);
			var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
			promesa.then(function(data){
				if(data !== undefined)
					ctrasp.paramVentanaDistribuir = data;
				
				if(ctrasp.paramVentanaDistribuir.length === 0 || ctrasp.paramVentanaDistribuir.length === undefined)
					ctrasp.limpiaDistribucion = true;
				else
					ctrasp.limpiaDistribucion = false;
				
	        });				
		};	
		
		ctrasp.fnCargarValoresPersona = function(tipoPersona)
		{ 
			console.log("Consulto Persona");
			
			var IdPersona = 0;
			var IdCliente = 0;
			
			if(ctrasp.parametrosEntrada.modoVentana === 'M')
			{
				var rutPersona = tipoPersona === 'N' ? DocumentoIdentidadClienteM : DocumentoIdentidadEmpleadorM;				
			}	
			else{
				var rutPersona = tipoPersona === 'N' ? ctrasp.datosForm.SocioNegocioSeleccionado.DocumentoIdentidadDefecto : $scope.ctrasp.petrasp.datosForm.SocioNegocioSeleccionadoEmp.DocumentoIdentidadDefecto;
			}
			
			if(rutPersona === '')
				return;
			
			if(tipoPersona === 'N')
			{
				var ConsultaPersona = new bcs.DataSource(datosConsultarTrabajador);
				var filterPersona = { field: "DocumentoIdentidad"   ,operator:"eq", value: rutPersona};
				
				ConsultaPersona.query({ filter: filterPersona }).then(function()
				{
					if(ConsultaPersona.view().length > 0)
					{
						ctrasp.DocumentoIdentidadCliente 		= ConsultaPersona.view()[0].DocumentoIdentidad;
						ctrasp.Nombres 							= ConsultaPersona.view()[0].Nombres;	
						ctrasp.ApellidoPaterno					= ConsultaPersona.view()[0].ApellidoPaterno;
						ctrasp.ApellidoMaterno					= ConsultaPersona.view()[0].ApellidoMaterno;
						ctrasp.kngFechaNacimiento				= util.date(ConsultaPersona.view()[0].FechaNacimiento).toDate();
						ctrasp.EstadoCivilIdActualPost 			= ConsultaPersona.view()[0].EstadoCivil;
						ctrasp.GeneroIdActualPost				= ConsultaPersona.view()[0].Genero;
						ctrasp.IdPaisResidenciaIdActualPost		= ConsultaPersona.view()[0].IdPaisResidencia;
						ctrasp.Ciudad							= ConsultaPersona.view()[0].NombreCiudad;
						ctrasp.CiudadIdActual					= ConsultaPersona.view()[0].IdCiudad;
						ctrasp.CiudadIdActualPost				= ConsultaPersona.view()[0].IdCiudad;
						ctrasp.Comuna							= ConsultaPersona.view()[0].NombreComuna;
						ctrasp.idDivisionPaisComuna				= ConsultaPersona.view()[0].IdComuna;
						ctrasp.idDivisionPaisComunaPost			= ConsultaPersona.view()[0].IdComuna;
						ctrasp.Calle							= ConsultaPersona.view()[0].DireccionCliente;
						ctrasp.kngNumeroDireccionCliente		= ConsultaPersona.view()[0].NumeroDirecionCliente;
						ctrasp.IdActividadCargoPost				= ConsultaPersona.view()[0].IdProfesion;
						ctrasp.IdEmpleadoEjecutivoPost			= ConsultaPersona.view()[0].IdEmpleadoEjecutivo;
						//CATEGORIA INVERSIONISTA
						if(ConsultaPersona.view()[0].IdCategoriaInversionista === null)
							bcsComunes.msg("warning","warning",$translate.instant("UI_PERSONA_NO_TIENE_CATERGORIA_INVERSIONISTA_FAVOR_COMPLETAR_INFO_EN_SOCIO_NEGOCIO"));
						else
							ctrasp.IdCategoriaInversionistaPost	= ConsultaPersona.view()[0].IdCategoriaInversionista;
						//PERFIL COMERCIAL
						if(ConsultaPersona.view()[0].IdPerfilComercial === null)
							bcsComunes.msg("warning","warning",$translate.instant("UI_PERSONA_NO_TIENE_PERFIL_COMERCIAL_FAVOR_COMPLETAR_INFO_EN_SOCIO_NEGOCIO"));
						else
							ctrasp.IdPerfilComercialBaseOrigenPost	= ConsultaPersona.view()[0].IdPerfilComercial;
						ctrasp.TelefonoFijo						= ConsultaPersona.view()[0].TelefonoFijo;
						ctrasp.TelefonoMovil					= ConsultaPersona.view()[0].TelefonoMovil;
						ctrasp.TelefonoLaboral					= ConsultaPersona.view()[0].TelefonoLaboral;
						ctrasp.EmailCliente						= ConsultaPersona.view()[0].DireccionElectronica;
						ctrasp.CiudadTrabajador					= ConsultaPersona.view()[0].NombreCiudad;
						
						ctrasp.fnDesBloquearOBloquearIdentificacionTrabajador(true);
					}
					else
					{																			
						bcsComunes.msg("warning","warning",$translate.instant("UI_PERSONA_NO_EXISTE_FAVOR_INGRESAR_DATOS_MANUALMENTE")); 	
					}
				});
			}
			else if(tipoPersona === 'J')
			{				
				var ConsultaEmpleador = new bcs.DataSource(datosConsultarEmpleador);
				var filterPersona = { field: "DocumentoIdentidadEmpleador"   ,operator:"eq", value: rutPersona};

				ConsultaEmpleador.query({ filter: filterPersona }).then(function()
				{
					if(ConsultaEmpleador.view().length > 0)
					{
						//Identificación Empleador
						$scope.ctrasp.petrasp.DocumentoIdentidadEmpleador	= ConsultaEmpleador.view()[0].DocumentoIdentidadEmpleador;
						$scope.ctrasp.petrasp.RazonSocial					= ConsultaEmpleador.view()[0].RazonSocialEmpleador;
						$scope.ctrasp.petrasp.ComunaEmpleadorIdActual		= ConsultaEmpleador.view()[0].IdComunaEmpleador;
						$scope.ctrasp.petrasp.CiudadEmpleador				= ConsultaEmpleador.view()[0].NombreCiudad;
						$scope.ctrasp.petrasp.DireccionComercialEmpleador	= ConsultaEmpleador.view()[0].DireccionEmpleador;
						$scope.ctrasp.petrasp.kngNumeroDireccionEmpleador	= ConsultaEmpleador.view()[0].NumeroDireccionEmpleador;
						$scope.ctrasp.petrasp.TelefonoEmpleador			= ConsultaEmpleador.view()[0].TelefonoEmpleador;
						//Encargado RRHH
						$scope.ctrasp.petrasp.NombreEncargado				= ConsultaEmpleador.view()[0].NombreEncargado;
						$scope.ctrasp.petrasp.EmailEncargado				= ConsultaEmpleador.view()[0].EmailEncargado;
						$scope.ctrasp.petrasp.TelefonoEncargado			= ConsultaEmpleador.view()[0].TelefonoEncargado;
						
						//Identificación Empleador
						$scope.ctrasp.petrasp.datosForm.RazonSocialDeshabilitado 		= true;
						$scope.ctrasp.petrasp.datosForm.DoctoIdentidadEmpDeshabilitado	= true;
						$scope.ctrasp.petrasp.datosForm.ComunaEmpDeshabilitado			= false;
						$scope.ctrasp.petrasp.datosForm.DirComercialEmpDeshabilitado	= true;
						$scope.ctrasp.petrasp.datosForm.NumeroEmpDeshabilitado			= true;
						$scope.ctrasp.petrasp.datosForm.telEmpDeshabilitado			= true;
						//Encargado RRHH
						$scope.ctrasp.petrasp.datosForm.nomEncDeshabilitado			= true;
						$scope.ctrasp.petrasp.datosForm.emailEncDeshabilitado			= true;
						$scope.ctrasp.petrasp.datosForm.telEncEncDeshabilitado			= true;
					}
					else
					{																			
						bcsComunes.msg("warning","warning",$translate.instant("UI_EMPLEADOR_NO_EXISTE_FAVOR_INGRESAR_DATOS_MANUALMENTE")); 	
					}
				});
			}
		};
			
		ctrasp.fnDesBloquearOBloquearIdentificacionTrabajador = function(bolHabilito)
		{
			ctrasp.datosForm.DoctoIdentDeshabilitado		= bolHabilito;
			ctrasp.datosForm.NombreDeshabilitado 			= bolHabilito;
			ctrasp.datosForm.ApellidoPatDeshabilitado 		= bolHabilito;
			ctrasp.datosForm.ApellidoMatDeshabilitado		= bolHabilito;
			ctrasp.datosForm.FechaNacDeshabilitado			= bolHabilito;
			ctrasp.datosForm.EstadoCivilDeshabilitado		= bolHabilito;
			ctrasp.datosForm.GeneroDeshabilitado			= bolHabilito;
			ctrasp.datosForm.PaisDeshabilitado				= bolHabilito;
			ctrasp.datosForm.ComunaDeshabilitado			= bolHabilito;
			ctrasp.datosForm.CiudadDeshabilitado			= bolHabilito;
			ctrasp.datosForm.CalleDeshabilitado				= bolHabilito;
			ctrasp.datosForm.CalleNumeroDeshabilitado		= bolHabilito;
			ctrasp.datosForm.ActividadCargoDeshabilitado	= bolHabilito;
			ctrasp.datosForm.CatInverDeshabilitado			= bolHabilito;
			ctrasp.datosForm.PerfilDeshabilitado			= bolHabilito;
			ctrasp.datosForm.telefonoFijoDeshabilitado		= bolHabilito;
			ctrasp.datosForm.telefonoMovilDeshabilitado		= bolHabilito;
			ctrasp.datosForm.telefonoLaboralDeshabilitado	= bolHabilito;
			ctrasp.datosForm.EmailDeshabilitado				= bolHabilito;
			ctrasp.datosForm.EjecutivoDeshabilitado			= bolHabilito;
			
			if(!bolHabilito)
				ctrasp.fnCamposRequeridoCliente(false);			
			
		};
		
		ctrasp.fnCamposRequeridoCliente = function(bolRequerido){
			ctrasp.habilitoTipoSolicitud		= true;
			ctrasp.limpiarClienteHabilitado 	= true;
			
			ctrasp.DocumentoIdentidadRequerido 	= bolRequerido;
			ctrasp.NombresRequerido				= bolRequerido;
			ctrasp.ApellidoPaternoRequerido		= bolRequerido;
			ctrasp.ApellidoMaternoRequerido		= bolRequerido;
			ctrasp.PaisResidenciaRequerido		= bolRequerido;
			ctrasp.CiudadRequerido				= bolRequerido;
			ctrasp.DivisionPaisRequerido		= bolRequerido;
			ctrasp.CalleRequerido				= bolRequerido;
			ctrasp.NumeroDireccionRequerido		= bolRequerido;
			ctrasp.EjecutivoRequerido			= bolRequerido;
		};
		
		ctrasp.fnValidaContrato = function()
		{ 
			var valido = false;
			
			if(	ctrasp.DocumentoIdentidadCliente		=== undefined	||
				ctrasp.DocumentoIdentidadCliente 		=== '' 			||
				ctrasp.Nombres							=== undefined	||
				ctrasp.Nombres 							=== '' 			||
				ctrasp.kngFechaSuscripcion 				=== undefined	||
				ctrasp.FolioContrato 					=== undefined	||
				//ctrasp.NombrePlanAhorroSeleccionado 	=== undefined	||
				ctrasp.TipoSolicitudSeleccionado		=== undefined	
			   )
			{
				valido = false;
			}
			else
			{
				valido = true;
			}
			
			return valido;
		};
		
		ctrasp.IdCiudad = function(){
			if(ctrasp.parametrosEntrada.modoVentana === 'M'){
				IdCiudad = ctrasp.CiudadIdActual;
			}else{
				IdCiudad = ctrasp.paisResidenciaSeleccionado.IdPais === 44 ? ctrasp.comunaSeleccionada.IdCiudad : ctrasp.ciudadSeleccionada.IdCiudad
			}	
		};	
		
		ctrasp.IdComuna = function(){
			if(ctrasp.parametrosEntrada.modoVentana === 'M'){
				IdComuna = ctrasp.idDivisionPaisComuna;
			}else{
				IdComuna = ctrasp.comunaSeleccionada === false ? null : ctrasp.comunaSeleccionada.IdDivisionPais;
			}	
		};	
		
		ctrasp.crearContrato = function()
		{
			//$scope.etapa=='guardar';
			var idVentana = bcsComunes.ventanaWinId($scope);
			bcsComunes.ventanaOverlay(idVentana,true);
			
			var filtro = Array(); 
			filtro.push({ field: "FolioContratoSAAPV" , operator: "eq", value: ctrasp.FolioContrato });
			var dsFolioContrato = new bcs.DataSource(datosFolioContrato);
			dsFolioContrato.query({filter: filtro}).then(function()
			{
				if(dsFolioContrato.view().length > 0 && ctrasp.parametrosEntrada.modoVentana === 'I')
				{
					bcsComunes.ventanaMsgErr($translate.instant('UI_FOLIO_EXISTE'),$scope.ventanaId);
					bcsComunes.ventanaOverlay(idVentana,false);
					return;				
				}
				else if(dsFolioContrato.view().length > 0 && ctrasp.parametrosEntrada.modoVentana === 'M')
				{
					bcsComunes.ventanaMsgErr($translate.instant('UI_FOLIO_EXISTE'),$scope.ventanaId);
					bcsComunes.ventanaOverlay(idVentana,false);
					return;				
				}	
				
				if($scope.ctrasp.petrasp.Directa !== undefined || $scope.ctrasp.petrasp.Indirecta !== undefined)
					if($scope.ctrasp.petrasp.Directa === true){
						if($scope.ctrasp.petrasp.DiaDescuentoIdActual === ''){
							bcsComunes.ventanaMsgErr('Si la forma de pago es directa debe agregar día de descuento.',$scope.ventanaId);
							bcsComunes.ventanaOverlay(idVentana,false);
							return;					
						}
					}else{
						if($scope.ctrasp.petrasp.ComisionTransferencia === ''){
							bcsComunes.ventanaMsgErr('Si la forma de pago es Indirecta debe agregar comisión de transferencia.',$scope.ventanaId);
							bcsComunes.ventanaOverlay(idVentana,false);
							return;					
						}
					}	
				
				if(ctrasp.NombrePlanAhorroSeleccionado === undefined || ctrasp.NombrePlanAhorroSeleccionado === false){
					bcsComunes.ventanaMsgErr('Falta seleccionar Plan de Ahorro.',$scope.ventanaId);
					bcsComunes.ventanaOverlay(idVentana,false);
					return;				
				}
				
				var fSuscripcion = ctrasp.kngFechaSuscripcion === undefined ? "" : ctrasp.kngFechaSuscripcion.toISOString().slice(0,10);
				var fActual = $scope.fechaActual.toISOString().slice(0,10);
				var fNacimiento = "0001-01-01";

				if(ctrasp.kngFechaNacimiento !== undefined && ctrasp.kngFechaNacimiento !== ""){
					
					fNacimiento = ctrasp.kngFechaNacimiento.toISOString().slice(0,10);
					
					var mayor = new Date($scope.SesionFechaSistema)
					var menor = ctrasp.kngFechaNacimiento.getFullYear()
					res = mayor.getFullYear() - menor
					
					if(res < 18){
						bcsComunes.msg("warning","warning",$translate.instant('UI_EL_SAAPV_QUE_SE_ESTA_INGRESANDO_PERTENECE_A_UN_MENOR_DE_EDAD'));
					}
				}
				
				ctrasp.IdCiudad();
				ctrasp.IdComuna();

				var parametros = {				
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo  : "APV",
					p_Tipo: ctrasp.parametrosEntrada.modoVentana === 'M' ? 'M' : "I",  ///Individual Manual o Por Servicio 
					p_Parametros : [{
						IdContratoSAAPV				 : ctrasp.parametrosEntrada.modoVentana === 'M'? ctrasp.parametrosEntrada.IdContratoSAAPV : 0,
						IdEmpresa					 : $scope.SesionIdEmpresa,
						FolioContratoSAAPV 			 : ctrasp.FolioContrato,
						IdPlan						 : ctrasp.NombrePlanAhorroSeleccionado.IdPlanTipoInversion === undefined ? 0 : ctrasp.NombrePlanAhorroSeleccionado.IdPlanTipoInversion, // DDS PRONET IT
						NombrePlan					 : ctrasp.NombrePlanAhorroSeleccionado.NombrePlanTipoInversion === undefined ? "" : ctrasp.NombrePlanAhorroSeleccionado.NombrePlanTipoInversion, // DDS PRONET IT
					    FechaSuscripcion			 : fSuscripcion,
					    FechaCreacion				 : fActual,
					    TipoMovimiento				 : ctrasp.OrigenTraspasoSeleccionado === undefined || ctrasp.OrigenTraspasoSeleccionado === false ? "" : ctrasp.OrigenTraspasoSeleccionado.Nombre,
					    RazonRetiro					 : ctrasp.OrigenTraspasoSeleccionado === undefined || ctrasp.OrigenTraspasoSeleccionado === false ? "" : ctrasp.OrigenTraspasoSeleccionado.Nombre === 'EGRESO' ? ctrasp.ptrasp.RazonRetiroIdActual : "",
					    FechaAprobacion				 : "0001-01-01",
					    FechaNotificacion			 : "0001-01-01",
					    FechaRecepcion				 : "0001-01-01",
					    CodigoTipoSolicitud          : ctrasp.TipoSolicitudSeleccionado.Codigo,
					    Folio						 : ctrasp.TipoSolicitudSeleccionado.Codigo === "TRA" ? "" : ctrasp.FolioBusqueda,
					    IdCategoriaInversionista	 : ctrasp.CategoriaInversionistaSeleccionado === undefined || ctrasp.CategoriaInversionistaSeleccionado === false ? "" : ctrasp.CategoriaInversionistaSeleccionado.IdCategoriaInversionista,
					    IdPerfilComercial			 : ctrasp.PerfilComercialSeleccionado === undefined || ctrasp.PerfilComercialSeleccionado === false ? "" : ctrasp.PerfilComercialSeleccionado.IdPerfilComercialBase,
					    IdPersona					 : ctrasp.parametrosEntrada.modoVentana === 'M'? ctrasp.IdPersona : 0,
					    IdCliente					 : ctrasp.parametrosEntrada.modoVentana === 'M'? ctrasp.IdCliente : 0,
					    IdEmpleador					 : ctrasp.parametrosEntrada.modoVentana === 'M'? ctrasp.IdEmpleador : 0,
					    IdEnrolamiento				 : 0,
					    IdPortafolio				 : 0,
					    NombreCliente				 : ctrasp.Nombres,
					    ApellidoPaternoCliente		 : ctrasp.ApellidoPaterno === undefined ? "" : ctrasp.ApellidoPaterno,
					    ApellidoMaternoCliente		 : ctrasp.ApellidoMaterno === undefined ? "" : ctrasp.ApellidoMaterno, 
					    DocumentoIdentidadCliente	 : ctrasp.DocumentoIdentidadCliente.replace(/[.]/gi,''),
					    FechaNacimientoCliente		 : fNacimiento === undefined || fNacimiento === false ? "0001-01-01" : fNacimiento,
					    EstadoCivil					 : ctrasp.EstadoCivil.Seleccionado.IdTipoEstadoCivil === undefined ? "" : ctrasp.EstadoCivil.Seleccionado.IdTipoEstadoCivil,
					    IdTipoGenero				 : ctrasp.Genero.Seleccionado.IdTipoGenero === undefined ? "" : ctrasp.Genero.Seleccionado.IdTipoGenero,
					    IdPaisResidenciaCliente		 : ctrasp.paisResidenciaSeleccionado.IdPais === undefined ? 0 : ctrasp.paisResidenciaSeleccionado.IdPais,
					    IdCiudadCliente				 : IdCiudad === undefined ? 0 : IdCiudad,
					    IdComunaCliente				 : IdComuna === null ? 0 : IdComuna,
					    DireccionCliente			 : ctrasp.Calle === undefined ? "" : ctrasp.Calle,
					    NumeroDireccionCliente		 : ctrasp.kngNumeroDireccionCliente === undefined ? 0 : ctrasp.kngNumeroDireccionCliente, 		
					    IdTipoCargoPersona			 : ctrasp.ActividadCargoSeleccionado.IdProfesion === undefined ? 0 : ctrasp.ActividadCargoSeleccionado.IdProfesion,
					    TelefonoFijo				 : ctrasp.TelefonoFijo === undefined || ctrasp.TelefonoFijo === null ? "" : ctrasp.TelefonoFijo,
					    TelefonoMovil	 			 : ctrasp.TelefonoMovil  === undefined || ctrasp.TelefonoMovil === null ? "" : ctrasp.TelefonoMovil,
					    TelefonoLaboral		         : ctrasp.TelefonoLaboral === undefined || ctrasp.TelefonoLaboral === null ? "" : ctrasp.TelefonoLaboral,
					    EmailCliente				 : ctrasp.EmailCliente === undefined ? "" : ctrasp.EmailCliente,
					    RazonSocialEmpleador		 : $scope.ctrasp.petrasp.RazonSocial === undefined ? "" :  $scope.ctrasp.petrasp.RazonSocial,	    
					    DocumentoIdentidadEmpleador	 : $scope.ctrasp.petrasp.DocumentoIdentidadEmpleador === undefined ? "" : $scope.ctrasp.petrasp.DocumentoIdentidadEmpleador.replace(/[.]/gi,''),
						IdPaisEmpleador				 : $scope.ctrasp.petrasp.comunaEmpleadorSeleccionada.IdPais === undefined ? 0 : $scope.ctrasp.petrasp.comunaEmpleadorSeleccionada.IdPais, // DDS PRONET IT
						DireccionComercialEmpleador	 : $scope.ctrasp.petrasp.DireccionComercialEmpleador === undefined ? "" : $scope.ctrasp.petrasp.DireccionComercialEmpleador,
						NumeroDireccionEmpleador	 : $scope.ctrasp.petrasp.kngNumeroDireccionEmpleador === undefined ? 0 : $scope.ctrasp.petrasp.kngNumeroDireccionEmpleador,
						IdCiudadEmpleador			 : $scope.ctrasp.petrasp.comunaEmpleadorSeleccionada.IdCiudad === undefined ? 0 : $scope.ctrasp.petrasp.comunaEmpleadorSeleccionada.IdCiudad, // DDS PRONET IT
						IdComunaEmpleador			 : $scope.ctrasp.petrasp.comunaEmpleadorSeleccionada.IdDivisionPais === undefined ? 0 : $scope.ctrasp.petrasp.comunaEmpleadorSeleccionada.IdDivisionPais,  
					    TelefonoEmpleador			 : $scope.ctrasp.petrasp.TelefonoEmpleador === undefined ? "" : $scope.ctrasp.petrasp.TelefonoEmpleador, 
					    NombreEncargado				 : $scope.ctrasp.petrasp.NombreEncargado === undefined ? "" : $scope.ctrasp.petrasp.NombreEncargado,
					    EmailEncargado				 : $scope.ctrasp.petrasp.EmailEncargado === undefined ? "" : $scope.ctrasp.petrasp.EmailEncargado,
					    TelefonoEncargado			 : $scope.ctrasp.petrasp.TelefonoEncargado === undefined ? "" : $scope.ctrasp.petrasp.TelefonoEncargado,
					    IdMedioRecaudacionTrabajador : $scope.ctrasp.petrasp.IdMedioRecaudacionTrabajador.Codigo === undefined ? 0 : $scope.ctrasp.petrasp.IdMedioRecaudacionTrabajador.Codigo,
					    IdEntidadRecaudadora		 : $scope.ctrasp.petrasp.entidadRecaudadora.IdEntidadRecaudadora === undefined ? 0 : $scope.ctrasp.petrasp.entidadRecaudadora.IdEntidadRecaudadora,
					    CodigoPrevision				 : $scope.ctrasp.petrasp.entidadRecaudadora.Codigo === undefined ? 0 : $scope.ctrasp.petrasp.entidadRecaudadora.Codigo,
					    Pensionado					 : $scope.ctrasp.petrasp.pensionadoSeleccion.Codigo === undefined ? "" : $scope.ctrasp.petrasp.pensionadoSeleccion.Codigo,
					    Trabajador					 : $scope.ctrasp.petrasp.trabajadorSeleccion.Nombre === undefined ? "" : $scope.ctrasp.petrasp.trabajadorSeleccion.Nombre,
					    OrigenAhorro 				 : "",
					    MontoPesos 				 	 : 0,
						MontoUF  					 : 0,
						PorcentajeRegimenImponible   : 0,
					    Pago						 : "",
					    PeriodoInicio				 : "",
					    PeriodoTermino				 : "",
					    RegimenTributario			 : "",
					    FormaDePago					 : "",//$scope.ctrasp.petrasp.Directa === true ? "DIRECTA" : "INDIRECTA",
					    IdMedioRecaudacionFormaPago	 : 0,//$scope.ctrasp.petrasp.Directa === true ? $scope.ctrasp.petrasp.FormaPagoDirecta.Codigo : $scope.ctrasp.petrasp.IdMedioRecaudacionTrabajador.Codigo,
					    DiaDescuento				 : 0,//parseInt($scope.ctrasp.petrasp.DiaDescuentoIdActual === undefined || $scope.ctrasp.petrasp.DiaDescuentoIdActual === false ? 0 : $scope.ctrasp.petrasp.DiaDescuentoIdActual),
					    Institucion				 	 : 0,//$scope.ctrasp.petrasp.Directa === true ? 0 : ($scope.ctrasp.petrasp.entidadRecaudadora.Codigo === undefined ? 0 : Number($scope.ctrasp.petrasp.entidadRecaudadora.Codigo)),
					    ComisionTransferencia		 : 0,//$scope.ctrasp.petrasp.kngComisionTransferencia === undefined  ? 0  : $scope.ctrasp.petrasp.kngComisionTransferencia,
					    NombreEntidad			 	 : $scope.ctrasp.ptrasp.EntidadRecaudadoraSeleccionado === undefined || $scope.ctrasp.ptrasp.EntidadRecaudadoraSeleccionado === false ? "" : $scope.ctrasp.ptrasp.EntidadRecaudadoraSeleccionado.Nombre,
					    DocumentoEntidad			 : $scope.ctrasp.ptrasp.DocumentoIdentidadEntidadOrigen === undefined ? "" : $scope.ctrasp.ptrasp.DocumentoIdentidadEntidadOrigen,
					    IdPersonaOrigen				 : $scope.ctrasp.ptrasp.EntidadRecaudadoraSeleccionado === undefined || $scope.ctrasp.ptrasp.EntidadRecaudadoraSeleccionado === false ? 0 : $scope.ctrasp.ptrasp.EntidadRecaudadoraSeleccionado.IdPersona,
					    TipoDeAhorro				 : $scope.ctrasp.ptrasp.TipoAhorroSeleccionado === undefined || $scope.ctrasp.ptrasp.TipoAhorroSeleccionado === false ? "" : $scope.ctrasp.ptrasp.TipoAhorroSeleccionado.Codigo,
					    FondoDeOrigen				 : $scope.ctrasp.ptrasp.FondoOrigenSeleccionado === undefined || $scope.ctrasp.ptrasp.FondoOrigenSeleccionado === false ? "" : $scope.ctrasp.ptrasp.FondoOrigenSeleccionado.Codigo,
					    TipoTraspaso				 : $scope.ctrasp.ptrasp.TipoTraspasoIdActual === undefined || $scope.ctrasp.ptrasp.TipoTraspasoIdActual === false ? "" : $scope.ctrasp.ptrasp.TipoTraspasoIdActual,
					    MontoTraspaso				 : $scope.ctrasp.ptrasp.kngMontoTraspaso === undefined ? 0  : $scope.ctrasp.ptrasp.kngMontoTraspaso,
					    UFTraspaso					 : $scope.ctrasp.ptrasp.kngUFTraspaso === undefined ? 0  : $scope.ctrasp.ptrasp.kngUFTraspaso,
					    PorcentajeTraspaso			 : $scope.ctrasp.ptrasp.kngPorcentajeTraspaso	=== undefined ? 0  : $scope.ctrasp.ptrasp.kngPorcentajeTraspaso,
					    Descripcion					 : $scope.ctrasp.ptrasp.GlosaTraspasoRetiro === undefined ? "" : $scope.ctrasp.ptrasp.GlosaTraspasoRetiro,
					    IdEstadoEnvioNotificacion	 : "NEV",
					    IdEstadoSolicitud			 : "",
					    IdEstado					 : "PDI",
					    IdCargaMasiva				 : null,
					    //Usuario
						IdEmpleado					 : ctrasp.IdEmpleadoEjecutivo === false || ctrasp.IdEmpleadoEjecutivo === undefined ? 0 : ctrasp.IdEmpleadoEjecutivo,
						IdPropositoPortafolio        : "",
						IdMatrizAgrupadorAPV         : 0,
						CodigoAgrupador              : "",
						}],
						p_DistribucionInversion 	 : ctrasp.paramVentanaDistribuir.length === undefined ? [] : ctrasp.paramVentanaDistribuir,
						p_DetalleOrigenTraspaso		 : 0
						//p_ParametrosModificados		 : 0
					};
				
					if($scope.ctrasp.ptrasp.dsOrigenTraspaso.view().length > 0){
						var DetalleOrigenTraspaso = Array();
						$.each($scope.ctrasp.ptrasp.dsOrigenTraspaso.view(),function(key,value){
							DetalleOrigenTraspaso.push({
								 IdDetalleOrigenTraspaso	: 0
								,IdTraspaso					: 0
								,IdEmpresa					: $scope.SesionIdEmpresa
								,CodigoTipoAhorro			: value.TipoAhorro
								,NombreEntidadOrigen		: value.NombreEntidad
								,DocumentoEntidadOrigen		: value.DocumentoIdentidad
								,FondoOrigen				: value.FondoOrigen
								,MontoTotalTraspaso			: value.MontoTraspaso
								,UFTotalTraspaso			: value.UFTraspaso
								,PorcentajeTotalTraspaso	: value.PorcentajeTraspaso
								,IdEstado					: 'VIG'
							});
						});
						parametros.p_DetalleOrigenTraspaso = DetalleOrigenTraspaso;
					};
				
					if(ctrasp.parametrosEntrada.modoVentana === 'M'){
						//LLAMAR MODIFICAR 
						bcsServiciosAPV.GrabarContratoTraspaso(parametros).$promise.then(function(respuesta){
					   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
							if (respuesta.P_OUT_ERRORESCONTADOR === 0){
								ctrasp.LimpioVariablesContrato();
								bcsComunes.ventanaMsg({mensaje: $translate.instant("UI_PROCESO_COMPLETADO"),conflictos:util.parseServerResponse(respuesta)},idVentana);
								bcsComunes.ventanaOverlay(idVentana,false);
							}
							else{
								var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
								$.each(ErrorDetalle,function(key,value){	
									bcsComunes.msg("error","Error","Existen problemas:" + value );
								});
								bcsComunes.ventanaOverlay(idVentana,false);
						    }
				   		},function(respuesta){
							var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
				   			$.each(ErrorDetalle,function(key,value){	
								bcsComunes.msg("error","Error","Existen problemas:" + value );
							});
				   			bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas:" ),conflictos:util.parseServerResponse(respuesta)},idVentana);
				   		});
					}else if(ctrasp.parametrosEntrada.modoVentana === 'I'){
						//LLAMAR INGRESO 
						bcsServiciosAPV.GrabarContratoTraspaso(parametros).$promise.then(function(respuesta){
					   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
							if (respuesta.P_OUT_ERRORESCONTADOR === 0){
								ctrasp.LimpioVariablesContrato();
								bcsComunes.ventanaMsg({mensaje: $translate.instant("UI_PROCESO_COMPLETADO"),conflictos:util.parseServerResponse(respuesta)},idVentana);
								bcsComunes.ventanaOverlay(idVentana,false);
							}
							else{
								var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
								$.each(ErrorDetalle,function(key,value){	
									bcsComunes.msg("error","Error","Existen problemas:" + value );
								});
								bcsComunes.ventanaOverlay(idVentana,false);
						    }
				   		},function(respuesta){
							var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
				   			$.each(ErrorDetalle,function(key,value){	
								bcsComunes.msg("error","Error","Existen problemas:" + value );
							});
				   			bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas:" ),conflictos:util.parseServerResponse(respuesta)},idVentana);
				   		});
							
					}				
			});
		};

		// Valida formato Telefono
		ctrasp.placeholderTelefono = $scope.$root.SesionValoresDefectoSistema.PLACEHOLDER_TELEFONO;
		ctrasp.validaFormatoTelefono = function(telefono,tipo)
		{
			if($scope.SesionLocale=='PE')return true;
	    	
    		if (telefono !== undefined && telefono !== '' && region.ValidaFormatoNumeroTelefono(telefono) == false)
    		{
    			if(tipo === "TFIJO")
    				ctrasp.TelefonoFijo = '';	
    			
    			if(tipo === "TMOVIL")
    				ctrasp.TelefonoMovil = '';

    			if(tipo === "TLABO")
    				ctrasp.TelefonoLaboral = '';

    			if(tipo === "TEMP")
    	    		ctrasp.petrasp.TelefonoEmpleador = '';

    			if(tipo === "TENC")
    	    		ctrasp.petrasp.TelefonoEncargado = '';

    			bcsComunes.msg("warning","warning",$translate.instant("UI_FORMATO_NUMERO_TELEFONO_INCORRECTO").replace("XXPLACEHOLDERXX",ctrasp.placeholderTelefono));
				$scope.formContratoSAAPV.$valid = undefined;
				return false;
			}

	    	return true;
	    };
		
		// Valida formato email
		ctrasp.validaFormatoEmail = function(email)
		{
			if(email === 'CLI')
			{
				if (ctrasp.EmailCliente !== undefined && ctrasp.EmailCliente !== '' && region.ValidaFormatoEmail(ctrasp.EmailCliente)==false)
				{
					ctrasp.EmailCliente = '';
		    		bcsComunes.msg("warning","warning",$translate.instant("UI_FORMATO_EMAIL_INCORRECTO"));
					$scope.formContratoSAAPV.$valid = undefined;
					return false;
				}	
			}
	    	
	    	return true;
	    };
	    
		// Valida fecha nacimiento
		ctrasp.validaFechaNacimiento = function()
		{
			if (ctrasp.kngFechaNacimiento !== undefined 
					&& ctrasp.kngFechaNacimiento !== '' 
						&& region.validarFechaNacimiento(ctrasp.kngFechaNacimiento, new Date($scope.$root.SesionFechaSistema) , 100)==false){
				bcsComunes.msg("warning","warning",$translate.instant("UI_FECHA_IMPROBABLE"));
				return false;
			}
			return true;
		};
		
		ctrasp.ValidaDocumentoIdentidad = function(tipo,persona){
			if(!tipo) return;
			var d;
			if(persona === 'CLI')
				d = ctrasp.DocumentoIdentidadCliente || "";
			if(persona === 'EMP')
				d = ctrasp.petrasp.DocumentoIdentidadEmpleador || "";
			
			if(d.length==0) return;
			$scope.errorValidarDocumento=false;
			if($scope.$root.SesionLocale == "CL" && tipo=='R') {
				var result = util.validarRut(d);
				if(result.valid){
					d = result.rutFormat;
				}else{
					d = "";
					bcsComunes.msg("warning","warning",$translate.instant("UI_RUT_INVALIDO") + " " + $scope.$root.SesionValoresDefectoSistema.PLACEHOLDER_RUT);
					$scope.errorValidarDocumento=true;
				}
			}
			if(persona === 'CLI')
				ctrasp.DocumentoIdentidadCliente = d;
			if(persona === 'EMP')
				ctrasp.petrasp.DocumentoIdentidadEmpleador = d;
		};
		
		
		/*----------CONSULTA CONTRATOS -----------------------------*/		
		ctrasp.ConsultaContratoSAAPV = function(IdContratoSAAPV)
		{
			if(ctrasp.IdContratoSAAPV === undefined)
				return;
			
			var filtro = Array(); 
			filtro.push({ field: "IdContratoSAAPV" , operator: "eq", value: Number(ctrasp.IdContratoSAAPV) });
			
			var ConsultaContrato = new bcs.DataSource(datosSAAPV);
			ConsultaContrato.query({ filter: filtro }).then(function(){
				if(ConsultaContrato.view().length > 0)
				{
					//Plan Ahorro
					ctrasp.kngFechaSuscripcion				= ConsultaContrato.view()[0].FechaSuscripcion;
					ctrasp.PlanAhorroIdActual				= ConsultaContrato.view()[0].IdPlan;
					ctrasp.PlanAhorroIdActualPost			= ConsultaContrato.view()[0].IdPlan;
					ctrasp.TipoSolicitudIdActual			= 'MOD';
					ctrasp.FolioBusqueda					= ConsultaContrato.view()[0].FolioContratoSAAPV;
				
					//Identificación Trabajador
					ctrasp.IdPersona					    = ConsultaContrato.view()[0].IdPersona;
					ctrasp.IdCliente					    = ConsultaContrato.view()[0].IdCliente;
					ctrasp.IdEmpleador					    = ConsultaContrato.view()[0].IdEmpleador;
					ctrasp.DocumentoIdentidadCliente		= ConsultaContrato.view()[0].DocumentoIdentidadCliente;
					ctrasp.Nombres 							= ConsultaContrato.view()[0].NombreCLiente;	
					ctrasp.ApellidoPaterno					= ConsultaContrato.view()[0].ApellidoPaternoCliente;
					ctrasp.ApellidoMaterno					= ConsultaContrato.view()[0].ApellidoMaternoCliente;
					ctrasp.kngFechaNacimiento				= ConsultaContrato.view()[0].FechaNacimientoCliente;
					ctrasp.EstadoCivilIdActual 				= ConsultaContrato.view()[0].EstadoCivil;
					ctrasp.EstadoCivilIdActualPost			= ConsultaContrato.view()[0].EstadoCivil;
					ctrasp.GeneroIdActual					= ConsultaContrato.view()[0].Genero;
					ctrasp.GeneroIdActualPost				= ConsultaContrato.view()[0].Genero;
					ctrasp.IdPaisResidenciaIdActual			= ConsultaContrato.view()[0].IdPaisResidenciaCliente;
					ctrasp.IdPaisResidenciaIdActualPost		= ConsultaContrato.view()[0].IdPaisResidenciaCliente;
					ctrasp.Ciudad							= ConsultaContrato.view()[0].NombreCiudad;
					ctrasp.CiudadIdActual					= ConsultaContrato.view()[0].IdCiudadCliente;
					ctrasp.CiudadIdActualPost				= ConsultaContrato.view()[0].IdCiudadCliente;
					ctrasp.Comuna							= ConsultaContrato.view()[0].NombreComuna;
					ctrasp.idDivisionPaisComuna				= ConsultaContrato.view()[0].IdComunaCliente;
					ctrasp.idDivisionPaisComunaPost			= ConsultaContrato.view()[0].IdComunaCliente;
					ctrasp.Calle							= ConsultaContrato.view()[0].DireccionCliente;
					ctrasp.kngNumeroDireccionCliente		= ConsultaContrato.view()[0].NumeroDirecionCliente;
					ctrasp.IdActividadCargo					= ConsultaContrato.view()[0].IdTipoCargoPersona;
					ctrasp.IdActividadCargoPost				= ConsultaContrato.view()[0].IdTipoCargoPersona;
					ctrasp.TelefonoFijo						= ConsultaContrato.view()[0].TelefonoFijo;
					ctrasp.TelefonoMovil					= ConsultaContrato.view()[0].TelefonoMovil;
					ctrasp.TelefonoLaboral					= ConsultaContrato.view()[0].TelefonoLaboral;
					ctrasp.EmailCliente						= ConsultaContrato.view()[0].EmailCliente;
					ctrasp.IdEmpleadoEjecutivo				= ConsultaContrato.view()[0].IdEmpleadoEjecutivo;
					ctrasp.IdEmpleadoEjecutivoPost  		= ConsultaContrato.view()[0].IdEmpleadoEjecutivo;
					ctrasp.IdCategoriaInversionista			= ConsultaContrato.view()[0].IdCategoriaInversionista;
					ctrasp.IdCategoriaInversionistaPost		= ConsultaContrato.view()[0].IdCategoriaInversionista;
					ctrasp.IdPerfilComercialBaseOrigen	    = ConsultaContrato.view()[0].IdPerfilComercialBaseOrigen;
					ctrasp.IdPerfilComercialBaseOrigenPost	= ConsultaContrato.view()[0].IdPerfilComercialBaseOrigen;
					ctrasp.CiudadTrabajador					= ConsultaContrato.view()[0].NombreCiudad;
					
					ctrasp.fnDesBloquearOBloquearIdentificacionTrabajador(true);
					
					datosEmpleador.push(
					{
						//Identificación Empleador
						RazonSocial					: ConsultaContrato.view()[0].NombreEmpleador,
						DocumentoIdentidadEmpleador	: ConsultaContrato.view()[0].DocumentoIdentidadEmpleador,
						ComunaEmpleador				: ConsultaContrato.view()[0].IdComunaEmpleador,
						CiudadEmpleador				: ConsultaContrato.view()[0].NombreCiudadEmpleador,
						DireccionComercialEmpleador	: ConsultaContrato.view()[0].DireccionComercialEmpleador,
						NumeroDireccionEmpleador	: ConsultaContrato.view()[0].NUMERODIRECIONEMPLEADOR,
						TelefonoEmpleador			: ConsultaContrato.view()[0].TelefonoEmpleador,
						//Encargado RRHH
						NombreEncargado				: ConsultaContrato.view()[0].NombreEncargado,
						EmailEncargado				: ConsultaContrato.view()[0].EmailEncargado,
						TelefonoEncargado			: ConsultaContrato.view()[0].TelefonoEncargado,
						//Tipo Trabajador
						MedioRecaudacionTrabajador	: ConsultaContrato.view()[0].IdMedioRecaudacionTrabajador,
						IdentidadRecaudadora		: ConsultaContrato.view()[0].CodigoPrevision,
						Pensionado					: ConsultaContrato.view()[0].Pensionado,
						Trabajador					: ConsultaContrato.view()[0].Trabajador  === "DEPENDIENTE" ? "DEP" : "IND",	
						//Forma Pago	   
						Directa						: ConsultaContrato.view()[0].FormaDePago === "DIRECTA" ? true : false,
						FormaPagoDirecta			: ConsultaContrato.view()[0].IdMedioRecaudacionFormaPago,
						DiaDescuento				: ConsultaContrato.view()[0].DiaDescuento,
						Indirecta					: ConsultaContrato.view()[0].FormaDePago === "INDIRECTA" ? true : false,
						MedioRecaudacionIndirecta	: ConsultaContrato.view()[0].FormaDePagoIndirecta,
						Institucion 				: ConsultaContrato.view()[0].Institucion,
						ComisionTransferencia 		: ConsultaContrato.view()[0].ComisionTransferencia,
						
						//Identificación Empleador
						RazonSocialDesabilitado					: true,
						DocumentoIdentidadEmpleadorDesabilitado	: true,
						ComunaEmpleadorDesabilitado				: false,
						DireccionComercialEmpleadorDesabilitado	: true,
						NumeroDireccionEmpleadorDesabilitado	: true,
						TelefonoEmpleadorDesabilitado			: true,
						//Encargado RRHH
						NombreEncargadoDesabilitado				: true,
						EmailEncargadoDesabilitado				: true,
						TelefonoEncargadoDesabilitado			: true,
					});
					
					ctrasp.datosEmpleador = datosEmpleador;
					
					var ConsultaDistribucion = new bcs.DataSource(defDistribucion);
					ConsultaDistribucion.query({ filter : filtro }).then(function(){
						if(ConsultaDistribucion.view().length > 0){
							ctrasp.paramVentanaDistribuir = ConsultaDistribucion.view(); 
						}
					});
				}
			});
		};
		
		$scope.ctrasp.limpiarCliente = function()
		{			
			ctrasp.datosForm.SocioNegocioidActual 	= false;
			ctrasp.DocumentoIdentidadCliente 		= "";
			ctrasp.Nombres 							= "";	
			ctrasp.ApellidoPaterno					= "";
			ctrasp.ApellidoMaterno					= "";
			ctrasp.IdPersona 						= "";
			ctrasp.kngFechaNacimiento				= "";
			ctrasp.EstadoCivilIdActual 				= false;
			ctrasp.EstadoCivilIdActualPost			= false;
			ctrasp.GeneroIdActual					= false;
			ctrasp.GeneroIdActualPost				= false;
			ctrasp.IdPaisResidenciaIdActual			= false;
			ctrasp.IdPaisResidenciaIdActualPost		= false;
			ctrasp.CiudadIdActual					= false;
			ctrasp.CiudadIdActualPost				= false;
			ctrasp.idDivisionPaisComuna				= false;
			ctrasp.idDivisionPaisComunaPost			= false;
			ctrasp.Calle							= "";
			ctrasp.kngNumeroDireccionCliente		= "";
			ctrasp.IdActividadCargo					= false;
			ctrasp.IdActividadCargoPost				= false;
			ctrasp.IdEmpleadoEjecutivo				= false;
			ctrasp.IdCategoriaInversionista			= false;
			ctrasp.IdCategoriaInversionistaPost		= false;
			ctrasp.IdPerfilComercialBaseOrigen		= false;
			ctrasp.IdPerfilComercialBaseOrigenPost	= false;
			ctrasp.TelefonoFijo						= "";
			ctrasp.TelefonoMovil					= "";
			ctrasp.TelefonoLaboral					= "";
			ctrasp.EmailCliente						= "";
			ctrasp.CiudadTrabajador					= "";
			
			ctrasp.datosForm.NombreDeshabilitado 			= false;
			ctrasp.datosForm.EjecutivoDeshabilitado			= false;
			ctrasp.datosForm.ApellidoPatDeshabilitado 		= false;
			ctrasp.datosForm.ApellidoMatDeshabilitado		= false;
			ctrasp.datosForm.DoctoIdentDeshabilitado		= false;
			ctrasp.datosForm.FechaNacDeshabilitado			= false;
			ctrasp.datosForm.EstadoCivilDeshabilitado		= false;
			ctrasp.datosForm.GeneroDeshabilitado			= false;
			ctrasp.datosForm.PaisDeshabilitado				= false;
			ctrasp.datosForm.ComunaDeshabilitado			= false;
			ctrasp.datosForm.CiudadDeshabilitado			= false;
			ctrasp.datosForm.CalleDeshabilitado				= false;
			ctrasp.datosForm.CalleNumeroDeshabilitado		= false;
			ctrasp.datosForm.ActividadCargoDeshabilitado	= false;
			ctrasp.datosForm.CatInverDeshabilitado			= false;
			ctrasp.datosForm.PerfilDeshabilitado			= false;
			ctrasp.datosForm.telefonoFijoDeshabilitado		= false;
			ctrasp.datosForm.telefonoMovilDeshabilitado		= false;
			ctrasp.datosForm.telefonoLaboralDeshabilitado	= false;
			ctrasp.datosForm.EmailDeshabilitado				= false;
			
			ctrasp.DocumentoIdentidadRequerido 	= true;
			ctrasp.NombresRequerido				= true;
			ctrasp.ApellidoPaternoRequerido		= true;
			ctrasp.ApellidoMaternoRequerido		= true;
			ctrasp.PaisResidenciaRequerido		= true;
			ctrasp.CiudadRequerido				= true;
			ctrasp.DivisionPaisRequerido		= true;
			ctrasp.CalleRequerido				= true;
			ctrasp.NumeroDireccionRequerido		= true;
		}

		ctrasp.LimpioVariablesContrato = function() 
		{	
			//Plan Ahorro
			ctrasp.FolioContrato 		= undefined;
			ctrasp.kngFolioContrato		= undefined;
			ctrasp.PlanAhorroIdActual	= undefined;
			ctrasp.FolioBusqueda 		= undefined;
			
			//Identificación Trabajador
			ctrasp.limpiarCliente();
			
			//Origen Y Monto Ahorro
			ctrasp.idMatrizAgrupador				= undefined;
			ctrasp.ptrasp.RegimenAgrupadorIdActual	= undefined;
			ctrasp.ptrasp.kngMontoPesoOrigen			= undefined;
			ctrasp.ptrasp.kngMontoUFOrigen			= undefined;
			ctrasp.ptrasp.kngPorcentajeOrigen			= undefined;
			ctrasp.ptrasp.TipoPagoIdActual			= undefined;
			$scope.ctrasp.ptrasp.dsOrigenTraspaso.removeAll();
			
			//Identificación Empleador
			//Encargado RRHH
			if(ctrasp.petrasp.limpiarEmpleador != undefined)
				ctrasp.petrasp.limpiarEmpleador();
			
			//Tipo Trabajador
			ctrasp.petrasp.MedioRecaudacionTrabajadorIdActual	= undefined;
			ctrasp.petrasp.IdentidadRecaudadoraIdActual		= undefined;
			ctrasp.petrasp.Codigo								= undefined;
			ctrasp.petrasp.PensionadoIdActual					= undefined;
			ctrasp.petrasp.TrabajadorIdActual					= undefined;
			
			//Forma Pago
			ctrasp.petrasp.FormaPagoDirectaIdActual					= undefined;
			ctrasp.petrasp.DiaDescuentoIdActual						= undefined;
			ctrasp.petrasp.MedioRecaudacionFormaPagoIndirectaIdActual	= undefined;
			ctrasp.petrasp.EntidadRecaudadoraInstitucion				= undefined;
			ctrasp.petrasp.kngComisionTransferencia					= undefined;
			ctrasp.ptrasp.LimpioVariablesOrigen();
			
			ctrasp.habilitoPlanAhorro	= false;
		};
		
		/******************************************************/
		/*** WATCH											***/
		/******************************************************/

		$scope.$watch('ctrasp.OrigenTraspasoSeleccionado',function(newval,oldval){
			if(newval && newval !== oldval)
				if(ctrasp.OrigenTraspasoSeleccionado !== undefined && ctrasp.OrigenTraspasoSeleccionado !== false){
					if(ctrasp.OrigenTraspasoSeleccionado.Nombre === 'INGRESO'){
						if(ctrasp.ptrasp !== undefined){
							ctrasp.ptrasp.habilitoFondoOrigen = true;
							ctrasp.fnDesBloquearOBloquearIdentificacionTrabajador(false);	
						}	
					}else if(ctrasp.OrigenTraspasoSeleccionado.Nombre === 'EGRESO'){
						ctrasp.tabs['PanelPortafolioContratoTraspaso'].active = true;
						ctrasp.fnDesBloquearOBloquearIdentificacionTrabajador(true);
						ctrasp.ptrasp.habilitoFondoOrigen = false;
					}
					if(ctrasp.ptrasp !== undefined){
						ctrasp.ptrasp.FondoOrigenIdActual = undefined;
						ctrasp.ptrasp.FondoOrigenPostFiltro = undefined;
					}
				};
		});

		$scope.$watch('ctrasp.datosForm.SocioNegocioSeleccionado',function(newval,oldval)
		{
			if(ctrasp.datosForm.SocioNegocioSeleccionado !==  false && ctrasp.datosForm.SocioNegocioSeleccionado !==  undefined)
			{
				ctrasp.fnCargarValoresPersona('N');
				
				ctrasp.DocumentoIdentidadRequerido 	= false;
				ctrasp.NombresRequerido				= false;
				ctrasp.ApellidoPaternoRequerido		= false;
				ctrasp.ApellidoMaternoRequerido		= false;
				ctrasp.PaisResidenciaRequerido		= false;
				ctrasp.CiudadRequerido				= false;
				ctrasp.DivisionPaisRequerido		= false;
				ctrasp.CalleRequerido				= false;
				ctrasp.NumeroDireccionRequerido		= false;
			}
		});
		
		$scope.$watch('ctrasp.idDivisionPaisComuna',function(newval,oldval)
		{
			if(ctrasp.parametrosEntrada.modoVentana === 'I')
			{
				if(ctrasp.idDivisionPaisComuna !== undefined && ctrasp.idDivisionPaisComuna !== false && ctrasp.comunaSeleccionada.IdCiudad !==undefined)
				{
					var ConsultaCiudad = new bcs.DataSource(datosCiudad);
					var filtro = { field: "IdCiudad", operator:"eq", value: Number(ctrasp.comunaSeleccionada.IdCiudad) };
					
					ConsultaCiudad.query({ filter: filtro }).then(function()
					{
						ctrasp.CiudadTrabajador = ConsultaCiudad.view()[0].NombreCiudad;
					});
				}
				else
				{
					ctrasp.CiudadTrabajador = undefined;
				}
			}
		});
	/******************************************************/
	/*** FINAL											***/
	/******************************************************/
	});
}());