(function() {
	angular.module('ModuloAPV').controller('ContratoSAAPVCtrl',
	function($compile,$scope,$translate,$resource,$element,$timeout,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,bcsConsultasAPV,bcsServiciosAPV)
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
		
		var csaapv = this;
		csaapv.kOptFechaSuscripcion = {};
		csaapv.kOptFechaSuscripcion.max = new Date();
		
		csaapv.parametrosEntrada = bcsComunes.ventanaParams(idVentana);		
		csaapv.limpiaDistribucion = false;
		csaapv.limpiarClienteHabilitado = false;
		csaapv.paramVentanaDistribuir = {};
		csaapv.validaPortafolio = false;
		csaapv.TabTraspasoConsulta = false;
		csaapv.OcultoControlesEgreso = false;
		$scope.data = {};
		csaapv.datosForm = {};
		csaapv.trasp = {};
		csaapv.IdEstado = '';
		csaapv.paramModificaDistribucion	= 0;
		csaapv.IdPersona					= 0;
		csaapv.IdCliente					= 0;
		csaapv.IdEmpleador					= 0;
		csaapv.IdContratoSAAPV 				= 0;
		csaapv.IdPortafolioExiste 			= 0;
		csaapv.FolioContratoDisable			= true;
		//csaapv.kngFechaSuscripcion 			= util.dateInicioDia($scope.SesionFechaSistema);
		$scope.kOptFechaNacimiento 			= {};
		$scope.kOptFechaNacimiento.max		= new Date(); //$scope.$root.SesionFechaSistema;
		$scope.data.bloqueoCliente 			= true;	//BLOQUEA ZONA PERSONA
		$scope.fechaActual 					= new Date();
		csaapv.filtros = 1;
		$scope.csaapv.pecs = {};
		
		//
		/*
		csaapv.datosForm.NombreDeshabilitado 			= true;
		csaapv.datosForm.EjecutivoDeshabilitado			= true;
		csaapv.datosForm.ApellidoPatDeshabilitado 		= true;
		csaapv.datosForm.ApellidoMatDeshabilitado		= true;
		csaapv.datosForm.DoctoIdentDeshabilitado		= true;
		csaapv.datosForm.FechaNacDeshabilitado			= true;
		csaapv.datosForm.EstadoCivilDeshabilitado		= true;
		csaapv.datosForm.GeneroDeshabilitado			= true;
		csaapv.datosForm.PaisDeshabilitado				= true;
		csaapv.datosForm.ComunaDeshabilitado			= true;
		csaapv.datosForm.CiudadDeshabilitado			= true;
		csaapv.datosForm.CalleDeshabilitado				= true;
		csaapv.datosForm.CalleNumeroDeshabilitado		= true;
		csaapv.datosForm.ActividadCargoDeshabilitado	= true;
		csaapv.datosForm.CatInverDeshabilitado			= true;
		csaapv.datosForm.PerfilDeshabilitado			= true;
		csaapv.datosForm.telefonoFijoDeshabilitado		= true;
		csaapv.datosForm.telefonoMovilDeshabilitado		= true;
		csaapv.datosForm.telefonoLaboralDeshabilitado	= true;
		csaapv.datosForm.EmailDeshabilitado				= true;

		csaapv.DocumentoIdentidadRequerido 			= false;
		csaapv.NombresRequerido						= false;
		csaapv.ApellidoPaternoRequerido				= false;
		csaapv.ApellidoMaternoRequerido				= false;
		csaapv.PaisResidenciaRequerido				= false;
		csaapv.CiudadRequerido						= false;
		csaapv.DivisionPaisRequerido				= false;
		csaapv.CalleRequerido						= false;
		csaapv.NumeroDireccionRequerido				= false;
		csaapv.EjecutivoRequerido					= false;
		csaapv.CategoriaoRequerido					= false;
		csaapv.PerfilRequerido						= false;
		*/
		csaapv.ValoresPanelDefecto = function(){
			/*TAB EMPLEADOR*/
			$scope.csaapv.pecs.RazonSocial = "";	    
			$scope.csaapv.pecs.DocumentoIdentidadEmpleador = "";
			$scope.csaapv.pecs.comunaEmpleadorSeleccionada = "";
			$scope.csaapv.pecs.DireccionComercialEmpleador = "";
			$scope.csaapv.pecs.kngNumeroDireccionEmpleador = 0;
			$scope.csaapv.pecs.comunaEmpleadorSeleccionada.IdCiudad = 0;
			$scope.csaapv.pecs.comunaEmpleadorSeleccionada.IdDivisionPais = 0;  
			$scope.csaapv.pecs.TelefonoEmpleador = ""; 
			$scope.csaapv.pecs.NombreEncargado = "";
			$scope.csaapv.pecs.EmailEncargado = "";
			$scope.csaapv.pecs.TelefonoEncargado = "";
			$scope.csaapv.pecs.IdMedioRecaudacionTrabajador = 0;
			$scope.csaapv.pecs.entidadRecaudadora = 0;
			$scope.csaapv.pecs.entidadRecaudadora = "";
			$scope.csaapv.pecs.pensionadoSeleccion = "";
			$scope.csaapv.pecs.trabajadorSeleccion = "";
			$scope.csaapv.pecs.DiaDescuentoIdActual = 0;
			$scope.csaapv.pecs.kngComisionTransferencia = 0
			$scope.csaapv.pecs.NombreEntidadOrigen = "";
			$scope.csaapv.pecs.RutEntidadOrigen = "";
			$scope.csaapv.pecs.seleccionadotipoAhorro = "";
			$scope.csaapv.pecs.fondoOrigen = "";
			$scope.csaapv.pecs.tipoTraspaso = "";
			$scope.csaapv.pecs.kngMontoTraspasoPeso = 0;
			$scope.csaapv.pecs.kngUFTraspaso = 0;
			$scope.csaapv.pecs.kngporcentajeTraspaso = 0;
		};
		csaapv.ValoresPanelDefecto();

		var datosEmpleador= Array ();
		var datosPortafolio= Array ();
		var ParametrosModificados = Array();
		csaapv.SocData={};
		
		/******************************************************/
		/*** FILTROS										***/
		/******************************************************/
		
		
		$scope.categoriaInversionista ={ 
			
				configuracion :{},
				idActual : 0,
				seleccionado : false,
				filtros: {
					IdPlan : {"field": "IdPlan","operator": "eq", "value"   : 0},
				

				},
				
		};
		
		
		csaapv.reagimenTributarioFitro =
		{
				filtros : {
					IdEstado : {field: "IdEstado", operator: "eq", value: "VIG"},
					Codigo	 : {field: "Codigo", operator: "contains", value: "APV"}
				}
		};
		
		csaapv.oConfPerfilComercial=
		{
				filtros : { 
					IdCategoriaInversionista : {
							"field"   : "IdCategoriaInversionista",
							"operator": "eq",
							"value"   : "" },
					IdPlan : {"field": "IdPlan","operator": "eq", "value"   : 0}
				}
		};
		
		csaapv.oConfPerfilComercial1=
		{
				filtros : { 
					IdCategoriaInversionista : {
							"field"   : "IdCategoriaInversionista",
							"operator": "eq",
							"value"   : "" },
				}
		};
		
		csaapv.filtroCiudad = 
		{
			IdPais : {field: "IdPais", operator: "eq", value: ""}
		};
				
		csaapv.filtroDivisionPais =
		{
			IdPais   : {field: "IdPais"  , operator: "eq" , value: -1},//$scope.$root.SesionIdPaisSistema},
			IdCiudad : {field: "IdCiudad", operator: "neq", value: 0} //no pueden haber divisiones sin ciudad!
		};	
		
		csaapv.filtroEjecutivo = {
			IdEmpresa : {field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa},
			IdEstado  : {field: "IdEstado" , operator: "eq", value: 'ACT'}
		};
		
		/*Valido Tipo Solicitud al Abrir Formulario desde el Menu*/
		if(parametrosVentana !== undefined){
			if(csaapv.parametrosEntrada.modoVentana === 'M'){
				csaapv.TipoSolicitud = {
						filtros : {
							Codigo : { field: "Codigo", operator: "eq", value: 'MOD'}
						}
				};	
			}else if(parametrosVentana.CodigoSolicitud === 'TRA'){
				csaapv.TipoSolicitud = {
						filtros : {
							Codigo : { field: "Codigo", operator: "eq", value: 'TRA'}
						}
				};
			}else if(parametrosVentana.CodigoSolicitud === 'AUT'){
				csaapv.TipoSolicitud = {
						filtros : {
							Codigo : { field: "Codigo", operator: "eq", value: 'AUT'}
						}
				};
			}else{
				csaapv.TipoSolicitud = {
						filtros : {
							Codigo : { field: "Codigo", operator: "neq", value: 'MOD'}
						}
				};
			}
		}	
		/*
		csaapv.TipoSolicitud = {
			filtros : {
				Codigo : parametrosVentana.CodigoSolicitud === 'TRA' ? { field: "Codigo", operator: "eq", value: 'TRA'} 
							: parametrosVentana.CodigoSolicitud === 'AUT' ? { field: "Codigo", operator: "eq", value: 'AUT'} 
								: csaapv.parametrosEntrada.modoVentana === 'M' ? { field: "Codigo", operator: "eq", value: 'MOD'} : { field: "Codigo", operator: "neq", value: 'MOD'}
			}
		};
		*/		
		/*Valido Tipo Solicitud al Abrir Formulario desde el Menu*/
		
		csaapv.domVentana = $("[ventanaid='"+idVentana+"']");
		csaapv.datosEmpleador = {};	
		csaapv.datosPortafolio = [];	
		csaapv.TabOcultoTraspaso = false;
		csaapv.tabs = {
				PanelEmpleadoContrato			: { id : 'PanelEmpleadoContrato', url : '<div ng-include = "\'../../APV/UI/Afiliacion/ContratoSAAPV/EMP/PanelEmpleadoContrato.html\'" ></div>'	, active : false,  cargado: false, datosCambiados: false },
				PanelPortafolioContratoTraspaso : { id : 'PanelPortafolioContratoTraspaso',	url : '<div ng-include = "\'../../APV/UI/Afiliacion/ContratoSAAPV/TRA/PanelPortafolioContratoTraspaso.html\'" ></div>'	, active : false,  cargado: false, datosCambiados: false },
				PanelPortafolioContrato			: { id : 'PanelPortafolioContrato',	url : '<div ng-include = "\'../../APV/UI/Afiliacion/ContratoSAAPV/POR/PanelPortafolioContrato.html\'" ></div>'	, active : true,  cargado: false, datosCambiados: false }
				
		};
		
		csaapv.fnTabSelect = function(ambito,nombreTab)
		{
			if (csaapv.tabs[nombreTab].cargado !== true)
			{
				csaapv.tabs[nombreTab].cargado = true;
				csaapv.domVentana.find('#'+csaapv.tabs[nombreTab].id).append($compile(csaapv.tabs[nombreTab].url)($scope));
			}
		};
		
		csaapv.fnTituloVentana = function(){
			if(csaapv.parametrosEntrada.modoVentana === 'M' && csaapv.TipoSolicitudSeleccionado.Codigo !== 'TRA')
				bcsComunes.ventanaTitulo(idVentana,$translate.instant('UI_MODIFICA_CONTRATO'));
			
			if(csaapv.parametrosEntrada.modoVentana === 'M' && csaapv.TipoSolicitudSeleccionado.Codigo === 'TRA')
				bcsComunes.ventanaTitulo(idVentana,$translate.instant('UI_MODIFICA_CONTRATO_TRASPASO'));
			
			if(csaapv.parametrosEntrada.modoVentana === 'I' && csaapv.TipoSolicitudSeleccionado.Codigo === 'TRA')
				bcsComunes.ventanaTitulo(idVentana,$translate.instant('UI_CONTRATO_TRASPASO'));
			
			if(csaapv.parametrosEntrada.modoVentana === 'I' && csaapv.TipoSolicitudSeleccionado.Codigo !== 'TRA')
				bcsComunes.ventanaTitulo(idVentana,$translate.instant('UI_CONTRATO_SAAPV'));
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
							"Nacionalidad"					: {type: "string"},
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
		
		if(csaapv.parametrosEntrada.modoVentana === 'M')
		{
			var defTraspaso = {
				type: "odata",
				definition : {
					serverFiltering : true, // Filtrado de datos en el servidor.
		            serverPaging    : true, // Paginado de los datos en el servidor.
		            serverSorting   : true, // Ordenamiento de los datos en el servidor.
		            pageSize        : 100,
		            sort 			: { field: "IdTraspaso", dir: "DESC" },
		            pageSize        : 10,
		            transport		: {
						read: {
							url 		: bcsConsultasAPV('ConsultarTraspasos'),
							dataType	: "json"
					    }
					},
					schema: {
						model: {
							fields: {
								   IdTraspaso				: {type: "number"}
								 , IdContratoSAAPV			: {type: "number"}
								 , TipoMovimiento			: {type: "string"}
								 , RazonRetiro				: {type: "string"}
								 , FechaIngreso				: {type: "sysdate"}
								 , CodigoTipoTraspaso		: {type: "string"}
								 , IdCliente				: {type: "number"}
								 , IdPersona				: {type: "number"}
								 , Descripcion				: {type: "string"}
								 , IdSocioNegocioCli		: {type: "number"}
								 , IdSocioNegocioEMP		: {type: "number"}
								 , IdDetalleOrigenTraspaso	: {type: "number"}
								 , CodigoTipoAhorro			: {type: "string"}
								 , IdEntidadRecaudadora		: {type: "number"}
								 , NombreEntidad			: {type: "string"}
								 , DocumentoEntidad			: {type: "string"}
								 , Fondo					: {type: "string"}
								 , MontoTraspaso			: {type: "number"}
								 , UFTraspaso				: {type: "number"}
								 , PorcentajeTraspaso		: {type: "number"}
								 , IdEstado					: {type: "string"}
							}
						}
					}
				}	
			};
			
			var defOrigenAhorro = {
				type: "odata",
				definition : {
					serverFiltering : true, // Filtrado de datos en el servidor.
		            serverPaging    : true, // Paginado de los datos en el servidor.
		            serverSorting   : true, // Ordenamiento de los datos en el servidor.
		            pageSize        : 100,
		            sort 			: { field: "IdDetalleOrigenTraspaso", dir: "DESC" },
		            pageSize        : 10,
		            transport		: {
						read: {
							url 		: bcsConsultasAPV('ConsultarOrigenTraspaso'),
							dataType	: "json"
					    }
					},
					schema: {
						model: {
							fields: {
								   IdTraspaso				: { type: "number"}
								 , IdContratoSAAPV			: { type: "number"}
								 , TipoMovimiento			: { type: "string"}
								 , RazonRetiro				: { type: "string"}
								 , FechaIngreso				: { type: "sysdate"}
								 , CodigoTipoTraspaso		: { type: "string"}
								 , IdCliente				: { type: "number"}
								 , IdPersona				: { type: "number"}
								 , Descripcion				: { type: "string"}
								 , IdSocioNegocioCli		: { type: "number"}
								 , IdSocioNegocioEMP		: { type: "number"}
								 , IdDetalleOrigenTraspaso	: { type: "number"}
								 , CodigoTipoAhorro			: { type: "string"}
								 , IdEntidadRecaudadora		: { type: "number"}
								 , NombreEntidad			: { type: "string"}
								 , DocumentoEntidad			: { type: "string"}
								 , Fondo					: { type: "string"}
								 , MontoTraspaso			: { type: "number"}
								 , UFTraspaso				: { type: "number"}
								 , PorcentajeTraspaso		: { type: "number"}
								 , IdEstado					: { type: "string"}
							}
						}
					}
				}	
			};	
			
			var defDetalleTraspaso = {
				type: "odata",
				definition : {
					serverFiltering : true, // Filtrado de datos en el servidor.
		            serverPaging    : true, // Paginado de los datos en el servidor.
		            serverSorting   : true, // Ordenamiento de los datos en el servidor.
		            pageSize        : 10,
		            sort 			: { field: "IdDetalleTraspaso", dir: "ASC" },
		            transport		: {
						read: {
							url 		: bcsConsultasAPV('ConsultarDetalleTraspasos'),
							dataType	: "json"
					    }
		            },
					schema: {
						model: {
							fields: {
								IdSocioNegocioCli		: { type: "number"},
								NombreCompleto			: { type: "string"},
								IdSocioNegocioEMP		: { type: "number"},
								RazonSocial				: { type: "string"},
								DocumentoEntidadOrigen	: { type: "string"},	
								NombreEntidadOrigen		: { type: "string"},
								FechaIngreso			: { type: "sysdate"},
								IdProductoAgrupador		: { type: "number"},	
								Producto				: { type: "string"},
								IdPlanTipoInversion		: { type: "number"},
								NombrePlan				: { type: "string"},
								IdRegimenTributario		: { type: "number"},
								IdMatrizAgrupadorAPV	: { type: "number"},
								CodigoAgrupador			: { type: "string"},
								IdPropositoPortafolio	: { type: "string"},
								MontoTotalTraspaso		: { type: "number"},
								UFTotalTraspaso			: { type: "number"},
								PorcentajeTotalTraspaso	: { type: "number"},
								CodigoTipoTraspaso		: { type: "string"},
								Codigo					: { type: "string"},
								IdDetalleTraspaso		: { type: "number"},
								FechaIngresoDetalle		: { type: "sysdate"},
								MontoPeso				: { type: "number"},
								MontoUF					: { type: "number"},
								Porcentaje				: { type: "number"},
								IdEstadoDetalle			: { type: "string"},
								IdEstado				: { type: "string"}	
							}
						}
					}
				}	
			};
			
				var defOrigenTraspasoEgresos = {
					type: "odata",
					definition: {
						serverFiltering: true, // Filtrado de datos en el servidor.
						serverPaging: true, // Paginado de los datos en el servidor.
						serverSorting: true, // Ordenamiento de los datos en el servidor.
						pageSize: 100,
						sort: { field: "IdDetalleTraspaso", dir: "DESC" },
						pageSize: 10,
						transport: {
							read: {
								url: bcsConsultasAPV('ConsultaDetalleTraspasosEgresos'),
								dataType: "json"
							}
						},
						schema: {
							model: {
								fields: {
									IdTraspaso: { type: "number" }
									, IdContratoSAAPV: { type: "number" }
									, TipoMovimiento: { type: "string" }
									, RazonRetiro: { type: "string" }
									, FechaIngreso: { type: "sysdate" }
									, CodigoTipoTraspaso: { type: "string" }
									, IdCliente: { type: "number" }
									, IdPersona: { type: "number" }
									, Descripcion: { type: "string" }
									, IdSocioNegocioCli: { type: "number" }
									, IdSocioNegocioEMP: { type: "number" }
									, IdDetalleOrigenTraspaso: { type: "number" }
									, FechaSuscripcion: {type:"string"}
									, CodigoTipoAhorro: { type: "string" }
									, IdEntidadRecaudadora: { type: "number" }
									, NombreEntidad: { type: "string" }
									, DocumentoEntidad: { type: "string" }
									, IdEntidadRecaudadora: {type: "number"}
									, FechaHoraSuscripcion: {type:"string"}
									, Fondo: { type: "string" }
									, MontoTraspaso: { type: "number" }
									, UFTraspaso: { type: "number" }
									, PorcentajeTraspaso: { type: "number" }
									, IdEstado: { type: "string" }
									, CodigoPropositoPortafolio: { type: "string" }
									, IdPortafolio: { type: "number" }
									, IdInstrumento: { type: "number" }
									, Nemotecnico: { type: "string" }
									, CodigoRegimenTributario: { type: "string" }
								}
							}
						}
					}
				};
			csaapv.FolioContratoDisable			= false;
		}
		
		
		/******************************************************/
		/*** FUNCIONES										***/
		/******************************************************/
		//Funcion Trim
		function ltrim(s) {
		   return s.replace(/^\s+/, "");
		};

		function rtrim(s) {
		   return s.replace(/\s+$/, "");
		};

		function trim(s) {
		   return rtrim(ltrim(s));
		};
		
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
		
		csaapv.DistribuirInversionSAAPV = function()
		{
			
			if(csaapv.IdPerfilComercialBaseOrigen === false && csaapv.IdCategoriaInversionista === false){
				bcsComunes.msg("warning","warning",$translate.instant("UI_FALTA_CATEGORIA_DE_INVERSION_Y_PERFIL_COMERCIAL"));
				return;
			}else if(csaapv.IdPerfilComercialBaseOrigen === false){
				bcsComunes.msg("warning","warning",$translate.instant("UI_FALTA_PERFIL_COMERCIAL"));
				return;
			}else if(csaapv.IdCategoriaInversionista === false){
				bcsComunes.msg("warning","warning",$translate.instant("UI_FALTA_CATEGORIA_DE_INVERSION"));
				return;
			}
			
			var filterVentana = Array();
			
			filterVentana.modoVentana = csaapv.parametrosEntrada.modoVentana;
			filterVentana.limpiaDistribucion = csaapv.limpiaDistribucion;
			
			if(csaapv.TipoSolicitudSeleccionado.Codigo === 'MOD')
				if(csaapv.OrigenTraspasoSeleccionado !== undefined)
					filterVentana.TipoSolicitud =  "TRA";
				else
					filterVentana.TipoSolicitud =  csaapv.TipoSolicitudSeleccionado.Codigo; 
			else
				filterVentana.TipoSolicitud =  csaapv.TipoSolicitudSeleccionado.Codigo;
			
			var MontoTotalPesos = 0;
			var MontoTotalUF = 0;
			
			var MontoTotalPortafolio = 0;
			var TipoMontoPortafolio = '';
			
			var IdTipoProducto = "";
			
			
			if(filterVentana.TipoSolicitud !== "TRA"){
				if($scope.csaapv.ppcs.dsOrigenAhorro.view().length <= 0)
				{
					bcsComunes.ventanaMsgErr('Debe ingresar Monto Origen Ahorro.',$scope.ventanaId);
					return;
				}
					
				$.each($scope.csaapv.ppcs.dsOrigenAhorro.view(),function(key,value)
				{
					if(value.MontoPesos > 0)
					{
						MontoTotalPortafolio = MontoTotalPortafolio + value.MontoPesos;
						TipoMontoPortafolio = 'PESOS';
					}
					else if(value.MontoUF > 0)
					{
						MontoTotalPortafolio =  MontoTotalPortafolio + value.MontoUF;
						TipoMontoPortafolio = 'UF';
					}
					
					IdTipoProducto = value.IdProducto;
				});
				
				
				
				if(filterVentana.modoVentana === 'I')
				{
					filterVentana.IdContratoSAAPV = csaapv.FolioContrato;
					filterVentana.MontoTotal = MontoTotalPortafolio;
					filterVentana.TipoMonto = TipoMontoPortafolio;
					filterVentana.paramVentanaDistribuir = csaapv.paramVentanaDistribuir;
					
					var tipo='';
					//if (IdTipoProducto === 2){tipo='ACN'}else{tipo='0'};
					if (IdTipoProducto === 4){tipo='FFM'}else{tipo='0'};
					filterVentana.IdTipoInstrumento = csaapv.NombrePlanAhorroSeleccionado.NombrePlanTipoInversion === 'A. ABIERTO' ? "FFM" : tipo;
					filterVentana.FolioContratoSAAPV = 0;
					filterVentana.IdPerfilProductos = csaapv.PerfilComercialSeleccionado.IdPerfilProductos;
				}
				else
				{				
					filterVentana.paramVentanaDistribuir = $scope.csaapv.ppcs.dsOrigenAhorro;				
					filterVentana.IdContratoSAAPV = csaapv.IdContratoSAAPV;
					filterVentana.FolioContratoSAAPV = csaapv.FolioBusqueda;
					filterVentana.MontoTotal = MontoTotalPortafolio;
					filterVentana.TipoMonto = TipoMontoPortafolio;
					filterVentana.paramVentanaDistribuir = csaapv.paramVentanaDistribuir;
					filterVentana.IdTipoInstrumento = csaapv.NombrePlanAhorroSeleccionado.NombrePlanTipoInversion === 'A. ABIERTO' ? "FFMM" : "";
					filterVentana.paramModificaDistribucion;
					filterVentana.IdPerfilProductos = csaapv.PerfilComercialSeleccionado.IdPerfilProductos;
				}
			}else{
				if(filterVentana.modoVentana === 'I'){
					filterVentana.IdContratoSAAPV = csaapv.FolioContrato;
					filterVentana.MontoTotal = 0;
					filterVentana.TipoMonto = '';
					filterVentana.paramVentanaDistribuir = csaapv.paramVentanaDistribuir;
					filterVentana.IdTipoInstrumento = csaapv.NombrePlanAhorroSeleccionado.NombrePlanTipoInversion === 'A. ABIERTO' ? "FFMM" : "";
					filterVentana.FolioContratoSAAPV = 0;
					filterVentana.IdPerfilProductos = csaapv.PerfilComercialSeleccionado.IdPerfilProductos;
				}else{
					filterVentana.IdContratoSAAPV = csaapv.IdContratoSAAPV;
					filterVentana.FolioContratoSAAPV = csaapv.FolioBusqueda;
					filterVentana.MontoTotal = 0;
					filterVentana.TipoMonto = '';
					filterVentana.paramVentanaDistribuir = csaapv.paramVentanaDistribuir;
					filterVentana.IdTipoInstrumento = csaapv.NombrePlanAhorroSeleccionado.NombrePlanTipoInversion === 'A. ABIERTO' ? "FFMM" : "";
					filterVentana.paramModificaDistribucion;
					filterVentana.IdPerfilProductos = csaapv.PerfilComercialSeleccionado.IdPerfilProductos;
				}
			}
			
			
			
			var IdVentanamodal = bcsComunes.ventanaAbrir("DistribuirInversionSAAPV", filterVentana, true, false, idVentana);
			var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
			promesa.then(function(data)
			{
				if(data !== undefined)
					csaapv.paramVentanaDistribuir = data;
				
				if(csaapv.paramVentanaDistribuir.length === 0 || csaapv.paramVentanaDistribuir.length === undefined)
					csaapv.limpiaDistribucion = true;
				else
					csaapv.limpiaDistribucion = false;
	        });				
		};	
		
		csaapv.fnCargarValoresPersona = function(tipoPersona)
		{ 
			console.log("Consulto Persona");
			
			var IdPersona = 0;
			var IdCliente = 0;		
			var rutPersona = tipoPersona === 'N' ? csaapv.datosForm.SocioNegocioSeleccionado.DocumentoIdentidadDefecto : $scope.csaapv.pecs.datosForm.SocioNegocioSeleccionadoEmp.DocumentoIdentidadDefecto;
			
			if(rutPersona === '')
				return;
			
			if(tipoPersona === 'N')
			{
				var ConsultaPersona = new bcs.DataSource(datosConsultarTrabajador);
				var filterPersona = { field: "DocumentoIdentidad"   ,operator:"eq", value: rutPersona!==undefined?rutPersona:parametrosVentana.DocumentoIdentidad};
				
				ConsultaPersona.query({ filter: filterPersona }).then(function()
				{
					if(ConsultaPersona.view().length > 0)
					{
						csaapv.DocumentoIdentidadCliente 		= ConsultaPersona.view()[0].DocumentoIdentidad;
						csaapv.Nombres 							= ConsultaPersona.view()[0].Nombres;	
						csaapv.ApellidoPaterno					= ConsultaPersona.view()[0].ApellidoPaterno;
						csaapv.ApellidoMaterno					= ConsultaPersona.view()[0].ApellidoMaterno;
						csaapv.kngFechaNacimiento				= util.date(ConsultaPersona.view()[0].FechaNacimiento).toDate();
						csaapv.EstadoCivilIdActualPost 			= ConsultaPersona.view()[0].EstadoCivil;
						csaapv.GeneroIdActualPost				= ConsultaPersona.view()[0].Genero;
						csaapv.IdPaisResidenciaIdActualPost		= ConsultaPersona.view()[0].IdPaisResidencia;
						csaapv.Ciudad							= ConsultaPersona.view()[0].NombreCiudad;
						csaapv.CiudadIdActual					= ConsultaPersona.view()[0].IdCiudad;
						csaapv.CiudadIdActualPost				= ConsultaPersona.view()[0].IdCiudad;
						csaapv.Comuna							= ConsultaPersona.view()[0].NombreComuna;
						csaapv.idDivisionPaisComuna				= ConsultaPersona.view()[0].IdComuna;
						csaapv.idDivisionPaisComunaPost			= ConsultaPersona.view()[0].IdComuna;
						csaapv.Calle							= ConsultaPersona.view()[0].DireccionCliente;
						csaapv.kngNumeroDireccionCliente		= ConsultaPersona.view()[0].NumeroDirecionCliente;
						csaapv.IdActividadCargoPost				= ConsultaPersona.view()[0].IdProfesion;
						csaapv.IdEmpleadoEjecutivoPost			= ConsultaPersona.view()[0].IdEmpleadoEjecutivo;
						csaapv.NacionalidadIdActual				= ConsultaPersona.view()[0].PaisNacimiento;
						csaapv.NacionalidadIdActualPost			= ConsultaPersona.view()[0].PaisNacimiento;
						//CATEGORIA INVERSIONISTA
						if(ConsultaPersona.view()[0].IdCategoriaInversionista === null)
							bcsComunes.msg("warning","warning",$translate.instant("UI_PERSONA_NO_TIENE_CATERGORIA_INVERSIONISTA_FAVOR_COMPLETAR_INFO_EN_SOCIO_NEGOCIO"));
						else
							csaapv.IdCategoriaInversionistaPost	= ConsultaPersona.view()[0].IdCategoriaInversionista;
						//PERFIL COMERCIAL
						if(ConsultaPersona.view()[0].IdPerfilComercial === null){
							//bcsComunes.msg("warning","warning",$translate.instant("UI_PERSONA_NO_TIENE_PERFIL_COMERCIAL_FAVOR_COMPLETAR_INFO_EN_SOCIO_NEGOCIO"));
							csaapv.datosForm.PerfilDeshabilitado	= false;
							csaapv.IdPerfilComercialBaseOrigenPost	= ConsultaPersona.view()[0].IdPerfilComercial;
							csaapv.PerfilRequerido = true;
							
						}else{
							csaapv.IdPerfilComercialBaseOrigenPost	= ConsultaPersona.view()[0].IdPerfilComercial;
							csaapv.TelefonoFijo						= ConsultaPersona.view()[0].TelefonoFijo;
							csaapv.TelefonoMovil					= ConsultaPersona.view()[0].TelefonoMovil;
							csaapv.TelefonoLaboral					= ConsultaPersona.view()[0].TelefonoLaboral;
							csaapv.EmailCliente						= ConsultaPersona.view()[0].DireccionElectronica;
							csaapv.CiudadTrabajador					= ConsultaPersona.view()[0].NombreCiudad;
							csaapv.fnBloquearIdentificacionTrabajador();
						}	
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
						$scope.csaapv.pecs.DocumentoIdentidadEmpleador	= ConsultaEmpleador.view()[0].DocumentoIdentidadEmpleador;
						$scope.csaapv.pecs.RazonSocial					= ConsultaEmpleador.view()[0].RazonSocialEmpleador;
						$scope.csaapv.pecs.ComunaEmpleadorIdActual		= ConsultaEmpleador.view()[0].IdComunaEmpleador;
						$scope.csaapv.pecs.CiudadEmpleador				= ConsultaEmpleador.view()[0].NombreCiudad;
						$scope.csaapv.pecs.DireccionComercialEmpleador	= ConsultaEmpleador.view()[0].DireccionEmpleador;
						$scope.csaapv.pecs.kngNumeroDireccionEmpleador	= ConsultaEmpleador.view()[0].NumeroDireccionEmpleador;
						$scope.csaapv.pecs.TelefonoEmpleador			= ConsultaEmpleador.view()[0].TelefonoEmpleador;
						//Encargado RRHH
						$scope.csaapv.pecs.NombreEncargado				= ConsultaEmpleador.view()[0].NombreEncargado;
						$scope.csaapv.pecs.EmailEncargado				= ConsultaEmpleador.view()[0].EmailEncargado;
						$scope.csaapv.pecs.TelefonoEncargado			= ConsultaEmpleador.view()[0].TelefonoEncargado;
						
						//Identificación Empleador
						$scope.csaapv.pecs.datosForm.RazonSocialDeshabilitado 		= true;
						$scope.csaapv.pecs.datosForm.DoctoIdentidadEmpDeshabilitado	= true;
						$scope.csaapv.pecs.datosForm.ComunaEmpDeshabilitado			= false;
						$scope.csaapv.pecs.datosForm.DirComercialEmpDeshabilitado	= true;
						$scope.csaapv.pecs.datosForm.NumeroEmpDeshabilitado			= true;
						$scope.csaapv.pecs.datosForm.telEmpDeshabilitado			= true;
						//Encargado RRHH
						$scope.csaapv.pecs.datosForm.nomEncDeshabilitado			= true;
						$scope.csaapv.pecs.datosForm.emailEncDeshabilitado			= true;
						$scope.csaapv.pecs.datosForm.telEncEncDeshabilitado			= true;
					}
					else
					{																			
						bcsComunes.msg("warning","warning",$translate.instant("UI_EMPLEADOR_NO_EXISTE_FAVOR_INGRESAR_DATOS_MANUALMENTE")); 	
					}
				});
			}
		};
			
		csaapv.fnBloquearIdentificacionTrabajador = function()
		{
			csaapv.datosForm.DoctoIdentDeshabilitado		= true;
			csaapv.datosForm.NombreDeshabilitado 			= true;
			csaapv.datosForm.ApellidoPatDeshabilitado 		= true;
			csaapv.datosForm.ApellidoMatDeshabilitado		= true;
			csaapv.datosForm.FechaNacDeshabilitado			= true;
			csaapv.datosForm.EstadoCivilDeshabilitado		= true;
			csaapv.datosForm.GeneroDeshabilitado			= true;
			csaapv.datosForm.PaisDeshabilitado				= true;
			csaapv.datosForm.ComunaDeshabilitado			= true;
			csaapv.datosForm.CiudadDeshabilitado			= true;
			csaapv.datosForm.CalleDeshabilitado				= true;
			csaapv.datosForm.CalleNumeroDeshabilitado		= true;
			csaapv.datosForm.ActividadCargoDeshabilitado	= true;
			csaapv.datosForm.CatInverDeshabilitado			= true;
			csaapv.datosForm.PerfilDeshabilitado			= true;
			csaapv.datosForm.telefonoFijoDeshabilitado		= true;
			csaapv.datosForm.telefonoMovilDeshabilitado		= true;
			csaapv.datosForm.telefonoLaboralDeshabilitado	= true;
			csaapv.datosForm.EmailDeshabilitado				= true;
			csaapv.datosForm.EjecutivoDeshabilitado			= true;

		}

		csaapv.fnValidaContrato = function()
		{ 
			var valido = false;
			
			if(	csaapv.DocumentoIdentidadCliente		=== undefined	||
				csaapv.DocumentoIdentidadCliente 		=== '' 			||
				//csaapv.datosForm.SocioNegocioSeleccionado === false	    ||
				//csaapv.Nombres							=== undefined	||
				//csaapv.Nombres 							=== '' 			||
				csaapv.kngFechaSuscripcion 				=== undefined	|| 
				csaapv.kngFechaSuscripcion 				=== null	    || 
				//csaapv.FolioContrato 					=== undefined	||
				//csaapv.NombrePlanAhorroSeleccionado 	=== undefined	||
				csaapv.TipoSolicitudSeleccionado		=== undefined	||
				csaapv.OrigenTraspasoSeleccionado       === false       ||
				csaapv.PerfilComercialSeleccionado      === false
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
		
		csaapv.IdCiudad = function(){
			if(csaapv.parametrosEntrada.modoVentana === 'M'){
				IdCiudad = csaapv.CiudadIdActual;
			}else{
				IdCiudad = csaapv.paisResidenciaSeleccionado.IdPais === 44 ? csaapv.comunaSeleccionada.IdCiudad : csaapv.ciudadSeleccionada.IdCiudad
			}	
		}	
		
		csaapv.IdComuna = function(){
			if(csaapv.parametrosEntrada.modoVentana === 'M'){
				IdComuna = csaapv.idDivisionPaisComuna;
			}else{
				IdComuna = csaapv.comunaSeleccionada === false ? null : csaapv.comunaSeleccionada.IdDivisionPais;
			}	
		}	
		
		csaapv.crearContrato = function()
		{
			//$scope.etapa=='guardar';
			var idVentana = bcsComunes.ventanaWinId($scope);
			bcsComunes.ventanaOverlay(idVentana,true);
			
			if(csaapv.FolioContrato === undefined || csaapv.FolioContrato === false || csaapv.FolioContrato === null) csaapv.FolioContrato = "";
			var filtro = Array();
			if(csaapv.FolioContrato != ""){
				filtro.push({ field: "FolioContratoSAAPV" , operator: "eq", value: csaapv.FolioContrato });
			}else
				filtro.push({ field: "FolioContratoSAAPV" , operator: "eq", value: "-1" });
					
			var dsFolioContrato = new bcs.DataSource(datosFolioContrato);
			dsFolioContrato.query({filter: filtro}).then(function()
			{
				if(dsFolioContrato.view().length > 0 && csaapv.parametrosEntrada.modoVentana === 'I')
				{
					bcsComunes.ventanaMsgErr($translate.instant('UI_FOLIO_EXISTE'),$scope.ventanaId);
					bcsComunes.ventanaOverlay(idVentana,false);
					return;				
				}
				
				if($scope.csaapv.pecs.Directa !== undefined || $scope.csaapv.pecs.Indirecta !== undefined)
					if($scope.csaapv.pecs.Directa === true){
						if(csaapv.pecs.FormaPagoDirecta.Nombre === "PAC"){
							if($scope.csaapv.pecs.DiaDescuentoIdActual === ''){
								bcsComunes.ventanaMsgErr('Si la forma de pago es directa debe agregar día de descuento.',$scope.ventanaId);
								bcsComunes.ventanaOverlay(idVentana,false);
								return;					
							}
						}
					}else{
						if($scope.csaapv.pecs.ComisionTransferencia === ''){
							bcsComunes.ventanaMsgErr('Si la forma de pago es Indirecta debe agregar comisión de transferencia.',$scope.ventanaId);
							bcsComunes.ventanaOverlay(idVentana,false);
							return;					
						}
					}	
				
				/*
				if(csaapv.NombrePlanAhorroSeleccionado === undefined || csaapv.NombrePlanAhorroSeleccionado === false){
					bcsComunes.ventanaMsgErr('Falta seleccionar Plan de Ahorro.',$scope.ventanaId);
					bcsComunes.ventanaOverlay(idVentana,false);
					return;				
				}
				*/
					//var fSuscripcion = csaapv.kngFechaSuscripcion === undefined ? "" : csaapv.kngFechaSuscripcion.toISOString().slice(0,10);
					var fSuscripcion;

					if (csaapv.kngFechaSuscripcion === undefined) {
					    fSuscripcion = "";
					} else if (typeof csaapv.kngFechaSuscripcion === 'string' && csaapv.kngFechaSuscripcion.startsWith('/Date(') && csaapv.kngFechaSuscripcion.endsWith(')/')) {
					    const timestamp = parseInt(csaapv.kngFechaSuscripcion.substring(7, csaapv.kngFechaSuscripcion.length - 2));
					    const date = new Date(timestamp);
					    fSuscripcion = date.toISOString().slice(0, 10);
					} else if (csaapv.kngFechaSuscripcion instanceof Date) {
					    fSuscripcion = csaapv.kngFechaSuscripcion.toISOString().slice(0, 10);
					} else {
					    fSuscripcion = "";
					}
				var fActual = $scope.fechaActual.toISOString().slice(0,10);
				var fNacimiento = "0001-01-01";

				if(csaapv.kngFechaNacimiento !== undefined && csaapv.kngFechaNacimiento !== "")
				{
					fNacimiento = csaapv.kngFechaNacimiento.toISOString().slice(0,10);
					
					var mayor = new Date($scope.SesionFechaSistema)
					var menor = csaapv.kngFechaNacimiento.getFullYear()
					res = mayor.getFullYear() - menor
					
					if(res < 18){
						bcsComunes.msg("warning","warning",$translate.instant('UI_EL_SAAPV_QUE_SE_ESTA_INGRESANDO_PERTENECE_A_UN_MENOR_DE_EDAD'));
					}
				}
				/*HMR*/
				csaapv.IdCiudad();
				csaapv.IdComuna();
				/*Inicio - Datos de Traspaso*/
				if(csaapv.TipoSolicitudSeleccionado.Codigo === "TRA" || csaapv.TipoTraspasoHabilito === true){
					csaapv.trasp.TipoMovimiento		= csaapv.OrigenTraspasoSeleccionado === undefined || csaapv.OrigenTraspasoSeleccionado === false ? "" : csaapv.OrigenTraspasoSeleccionado.Nombre;
					//csaapv.trasp.RazonRetiro		= csaapv.OrigenTraspasoSeleccionado === undefined || csaapv.OrigenTraspasoSeleccionado === false ? "" : csaapv.OrigenTraspasoSeleccionado.Nombre === 'EGRESO' ? csaapv.ptrasp.RazonRetiroIdActual : "";
					csaapv.trasp.RazonRetiro		= csaapv.OrigenTraspasoSeleccionado.Nombre === 'EGRESO' && csaapv.ptrasp.RazonRetiroIdActual !== false? csaapv.ptrasp.RazonRetiroIdActual : "";
					csaapv.trasp.NombreEntidadOrigen= $scope.csaapv.ptrasp.EntidadRecaudadoraSeleccionado === undefined || $scope.csaapv.ptrasp.EntidadRecaudadoraSeleccionado === false ? "" : $scope.csaapv.ptrasp.EntidadRecaudadoraSeleccionado.Nombre;
					csaapv.trasp.RutEntidadOrigen	= $scope.csaapv.ptrasp.DocumentoIdentidadEntidadOrigen === undefined ? "" : $scope.csaapv.ptrasp.DocumentoIdentidadEntidadOrigen;
					csaapv.trasp.IdPersonaOrigen	= $scope.csaapv.ptrasp.EntidadRecaudadoraSeleccionado === undefined || $scope.csaapv.ptrasp.EntidadRecaudadoraSeleccionado === false ? 0 : $scope.csaapv.ptrasp.EntidadRecaudadoraSeleccionado.IdPersona;
					csaapv.trasp.TipoDeAhorro		= $scope.csaapv.ptrasp.TipoAhorroSeleccionado === undefined || $scope.csaapv.ptrasp.TipoAhorroSeleccionado === false ? "" : $scope.csaapv.ptrasp.TipoAhorroSeleccionado.Codigo;
					csaapv.trasp.FondoDeOrigen		= $scope.csaapv.ptrasp.FondoOrigenSeleccionado === undefined || $scope.csaapv.ptrasp.FondoOrigenSeleccionado === false ? "" : $scope.csaapv.ptrasp.FondoOrigenSeleccionado.Codigo;
					csaapv.trasp.TipoTraspaso		= $scope.csaapv.ptrasp.TipoTraspasoIdActual === undefined || $scope.csaapv.ptrasp.TipoTraspasoIdActual === false ? "" : $scope.csaapv.ptrasp.TipoTraspasoIdActual;
					csaapv.trasp.MontoTraspaso		= $scope.csaapv.ptrasp.kngMontoTraspaso === undefined ? 0  : $scope.csaapv.ptrasp.kngMontoTraspaso;
					csaapv.trasp.UFTraspaso			= $scope.csaapv.ptrasp.kngUFTraspaso === undefined ? 0  : $scope.csaapv.ptrasp.kngUFTraspaso;
					csaapv.trasp.PorcentajeTraspaso	= $scope.csaapv.ptrasp.kngPorcentajeTraspaso	=== undefined ? 0  : $scope.csaapv.ptrasp.kngPorcentajeTraspaso;
					csaapv.trasp.Descripcion		= $scope.csaapv.ptrasp.GlosaTraspasoRetiro === undefined ? "" : $scope.csaapv.ptrasp.GlosaTraspasoRetiro;
					csaapv.trasp.IdTraspaso = csaapv.IdTraspaso;
			    }else{
			    	csaapv.trasp.TipoMovimiento		= "";
			    	csaapv.trasp.RazonRetiro		= "";
			    	csaapv.trasp.NombreEntidadOrigen= "";
			    	csaapv.trasp.RutEntidadOrigen	= "";
			    	csaapv.trasp.IdPersonaOrigen	= 0;
			    	csaapv.trasp.TipoDeAhorro		= "";
			    	csaapv.trasp.FondoDeOrigen		= "";
			    	csaapv.trasp.TipoTraspaso		= "";
			    	csaapv.trasp.MontoTraspaso		= 0;
			    	csaapv.trasp.UFTraspaso			= 0;
			    	csaapv.trasp.PorcentajeTraspaso	= 0;
			    	csaapv.trasp.Descripcion		= "";
			    }
				/*Inicio - Se asigna FolioContratoSAAPV al arreglo distribucion cuando se ingresa el foliocontratosaapv despues de haber agregado la distribucion*/
				if(csaapv.FolioContrato !== undefined){
					if(csaapv.paramVentanaDistribuir.length !== undefined){
						var tablaDistribucion = [];
						$.each(csaapv.paramVentanaDistribuir,function(key,value){
							var distribucion = { 
							   FolioContratoSAAPV : csaapv.FolioContrato,
							   IdInstrumento 	  : value.IdInstrumento,
							   RazonSocial   	  : value.RazonSocial,
							   Nemotecnico		  : value.Nemotecnico,
							   MontoPesos		  : value.MontoPesos,
							   Porcentaje		  : value.Porcentaje,
							   IdEstado			  : value.IdEstado
							};
							tablaDistribucion.push(distribucion);
						});
						csaapv.paramVentanaDistribuir = tablaDistribucion;
					}	
				}
				/*Fin - Se asigna FolioContratoSAAPV al arreglo distribucion cuando se ingresa el foliocontratosaapv despues de haber agregado la distribucion*/
				
				/*Fin - Datos de Traspaso*/
				var parametros = {				
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo  : "APV",
					p_Tipo: csaapv.parametrosEntrada.modoVentana === 'M' ? 'M' : "I",  ///Individual Manual o Por Servicio 
					p_Parametros : [{
						IdContratoSAAPV				 : csaapv.parametrosEntrada.modoVentana === 'M'? csaapv.parametrosEntrada.IdContratoSAAPV : 0,
						IdEmpresa					 : $scope.SesionIdEmpresa,
						FolioContratoSAAPV 			 : csaapv.FolioContrato === undefined || csaapv.FolioContrato === false ? "" : csaapv.FolioContrato,
						IdPlan						 : csaapv.NombrePlanAhorroSeleccionado.IdPlanTipoInversion === undefined || csaapv.NombrePlanAhorroSeleccionado === false ? 0 : csaapv.NombrePlanAhorroSeleccionado.IdPlanTipoInversion, // DDS PRONET IT
						NombrePlan					 : csaapv.NombrePlanAhorroSeleccionado.NombrePlanTipoInversion === undefined || csaapv.NombrePlanAhorroSeleccionado === false ? "" : csaapv.NombrePlanAhorroSeleccionado.NombrePlanTipoInversion, // DDS PRONET IT
					    FechaSuscripcion			 : fSuscripcion === undefined || fSuscripcion === false || fSuscripcion === "" ? "0001-01-01" : fSuscripcion,
					    FechaCreacion				 : $scope.$root.SesionFechaSistema,
					    FechaAprobacion				 : "0001-01-01",
					    FechaNotificacion			 : "0001-01-01",
					    FechaRecepcion				 : "0001-01-01",
					    CodigoTipoSolicitud			 : csaapv.TipoSolicitudSeleccionado.Codigo === "AUT" ? "" : csaapv.TipoTraspasoHabilito === true ? "TRA" : csaapv.TipoSolicitudSeleccionado.Codigo,
					    Folio						 : csaapv.TipoSolicitudSeleccionado.Codigo === "AUT" ? "" : (csaapv.IdEstado === 'VIG' ? csaapv.FolioBusqueda : ""),
					    IdCategoriaInversionista	 : csaapv.CategoriaInversionistaSeleccionado === undefined || csaapv.CategoriaInversionistaSeleccionado === false ? "" : csaapv.CategoriaInversionistaSeleccionado.IdCategoriaInversionista,
					    IdPerfilComercial			 : csaapv.PerfilComercialSeleccionado === undefined || csaapv.PerfilComercialSeleccionado === false ? "" : csaapv.PerfilComercialSeleccionado.IdPerfilComercialBase,
					    IdPersona					 : csaapv.parametrosEntrada.modoVentana === 'M'? csaapv.IdPersona : 0,
					    IdCliente					 : csaapv.parametrosEntrada.modoVentana === 'M'? csaapv.IdCliente : 0,
					    IdEmpleador					 : csaapv.parametrosEntrada.modoVentana === 'M'? csaapv.IdEmpleador : 0,
					    IdEnrolamiento				 : 0,
					    IdPortafolio				 : csaapv.IdPortafolioExiste === undefined ? 0 : csaapv.IdPortafolioExiste,
					    NombreCliente				 : csaapv.Nombres === undefined ? "" : csaapv.Nombres,
					    ApellidoPaternoCliente		 : csaapv.ApellidoPaterno === undefined ? "" : csaapv.ApellidoPaterno,
					    ApellidoMaternoCliente		 : csaapv.ApellidoMaterno === undefined ? "" : csaapv.ApellidoMaterno, 
					    DocumentoIdentidadCliente	 : csaapv.DocumentoIdentidadCliente.replace(/[.]/gi,''),
					    FechaNacimientoCliente		 : fNacimiento === undefined || fNacimiento === false ? "0001-01-01" : fNacimiento,
					    EstadoCivil					 : csaapv.EstadoCivil.Seleccionado.IdTipoEstadoCivil === undefined ? "" : csaapv.EstadoCivil.Seleccionado.IdTipoEstadoCivil,
					    IdTipoGenero				 : csaapv.Genero.Seleccionado.IdTipoGenero === undefined ? "" : csaapv.Genero.Seleccionado.IdTipoGenero,
					    IdPaisResidenciaCliente		 : csaapv.paisResidenciaSeleccionado.IdPais === undefined ? 0 : csaapv.paisResidenciaSeleccionado.IdPais,
					    IdCiudadCliente				 : IdCiudad === undefined ? 0 : IdCiudad,
					    IdComunaCliente				 : IdComuna === null ? 0 : IdComuna,
					    DireccionCliente			 : csaapv.Calle === undefined ? "" : csaapv.Calle,
					    NumeroDireccionCliente		 : csaapv.kngNumeroDireccionCliente === undefined ? 0 : csaapv.kngNumeroDireccionCliente, 		
					    IdTipoCargoPersona			 : csaapv.ActividadCargoSeleccionado.IdProfesion === undefined ? 0 : csaapv.ActividadCargoSeleccionado.IdProfesion,
					    TelefonoFijo				 : csaapv.TelefonoFijo === undefined || csaapv.TelefonoFijo === null ? "" : csaapv.TelefonoFijo,
					    TelefonoMovil	 			 : csaapv.TelefonoMovil  === undefined || csaapv.TelefonoMovil === null ? "" : csaapv.TelefonoMovil,
					    TelefonoLaboral		         : csaapv.TelefonoLaboral === undefined || csaapv.TelefonoLaboral === null ? "" : csaapv.TelefonoLaboral,
					    EmailCliente				 : csaapv.EmailCliente === undefined ? "" : csaapv.EmailCliente,
					    RazonSocialEmpleador		 : $scope.csaapv.pecs.RazonSocial === undefined ? "" :  $scope.csaapv.pecs.RazonSocial,	    
					    DocumentoIdentidadEmpleador	 : $scope.csaapv.pecs.DocumentoIdentidadEmpleador === undefined ? "" : $scope.csaapv.pecs.DocumentoIdentidadEmpleador.replace(/[.]/gi,''),
						IdPaisEmpleador				 : $scope.csaapv.pecs.comunaEmpleadorSeleccionada.IdPais === undefined ? 0 : $scope.csaapv.pecs.comunaEmpleadorSeleccionada.IdPais, // DDS PRONET IT
						DireccionComercialEmpleador	 : $scope.csaapv.pecs.DireccionComercialEmpleador === undefined ? "" : $scope.csaapv.pecs.DireccionComercialEmpleador,
						NumeroDireccionEmpleador	 : $scope.csaapv.pecs.kngNumeroDireccionEmpleador === undefined ? 0 : $scope.csaapv.pecs.kngNumeroDireccionEmpleador,
						IdCiudadEmpleador			 : $scope.csaapv.pecs.comunaEmpleadorSeleccionada.IdCiudad === undefined ? 0 : $scope.csaapv.pecs.comunaEmpleadorSeleccionada.IdCiudad, // DDS PRONET IT
						IdComunaEmpleador			 : $scope.csaapv.pecs.comunaEmpleadorSeleccionada.IdDivisionPais === undefined ? 0 : $scope.csaapv.pecs.comunaEmpleadorSeleccionada.IdDivisionPais,  
					    TelefonoEmpleador			 : $scope.csaapv.pecs.TelefonoEmpleador === undefined ? "" : $scope.csaapv.pecs.TelefonoEmpleador, 
					    NombreEncargado				 : $scope.csaapv.pecs.NombreEncargado === undefined ? "" : $scope.csaapv.pecs.NombreEncargado,
					    EmailEncargado				 : $scope.csaapv.pecs.EmailEncargado === undefined ? "" : $scope.csaapv.pecs.EmailEncargado,
					    TelefonoEncargado			 : $scope.csaapv.pecs.TelefonoEncargado === undefined ? "" : $scope.csaapv.pecs.TelefonoEncargado,
					    IdMedioRecaudacionTrabajador : $scope.csaapv.pecs.IdMedioRecaudacionTrabajador.Codigo === undefined ? 0 : $scope.csaapv.pecs.IdMedioRecaudacionTrabajador.Codigo,
					    IdEntidadRecaudadora		 : $scope.csaapv.pecs.entidadRecaudadora.IdEntidadRecaudadora === undefined ? 0 : $scope.csaapv.pecs.entidadRecaudadora.IdEntidadRecaudadora,
					    CodigoPrevision				 : $scope.csaapv.pecs.entidadRecaudadora.Codigo === undefined ? 0 : $scope.csaapv.pecs.entidadRecaudadora.Codigo,
					    Pensionado					 : $scope.csaapv.pecs.pensionadoSeleccion.Codigo === undefined ? "" : $scope.csaapv.pecs.pensionadoSeleccion.Codigo,
					    Trabajador					 : $scope.csaapv.pecs.trabajadorSeleccion.Nombre === undefined ? "" : $scope.csaapv.pecs.trabajadorSeleccion.Nombre,
					    OrigenAhorro 				 : "",
					    MontoPesos 				 	 : 0,
						MontoUF  					 : 0,
						PorcentajeRegimenImponible   : 0,
					    Pago						 : "",
					    PeriodoInicio				 : "",
					    PeriodoTermino				 : "",
					    RegimenTributario			 : "",
					    FormaDePago					 : $scope.csaapv.pecs.Directa === true ? "DIRECTA" : "INDIRECTA",
					    IdMedioRecaudacionFormaPago	 : $scope.csaapv.pecs.Directa === true ? $scope.csaapv.pecs.FormaPagoDirecta.Codigo : $scope.csaapv.pecs.IdMedioRecaudacionTrabajador.Codigo,
					    DiaDescuento				 : parseInt($scope.csaapv.pecs.DiaDescuentoIdActual === undefined || $scope.csaapv.pecs.DiaDescuentoIdActual === false ? 0 : $scope.csaapv.pecs.DiaDescuentoIdActual),
					    Institucion				 	 : $scope.csaapv.pecs.Directa === true ? 0 : ($scope.csaapv.pecs.entidadRecaudadora.Codigo === undefined ? 0 : Number($scope.csaapv.pecs.entidadRecaudadora.Codigo)),
					    ComisionTransferencia		 : $scope.csaapv.pecs.kngComisionTransferencia === undefined  ? 0  : $scope.csaapv.pecs.kngComisionTransferencia,
					    /*Inicio - Datos de Traspaso*/		
				    	TipoMovimiento				 : csaapv.trasp.TipoMovimiento,
						RazonRetiro					 : csaapv.trasp.RazonRetiro,
			    		NombreEntidadOrigen		 	 : csaapv.trasp.NombreEntidadOrigen,
					    RutEntidadOrigen		 	 : csaapv.trasp.RutEntidadOrigen,
					    IdPersonaOrigen				 : csaapv.trasp.IdPersonaOrigen,
					    TipoDeAhorro				 : csaapv.trasp.TipoDeAhorro,
					    FondoDeOrigen				 : csaapv.trasp.FondoDeOrigen,
					    TipoTraspaso				 : csaapv.trasp.TipoTraspaso,
					    MontoTraspaso				 : csaapv.trasp.MontoTraspaso,
					    UFTraspaso					 : csaapv.trasp.UFTraspaso,
					    PorcentajeTraspaso			 : csaapv.trasp.PorcentajeTraspaso,
					    Descripcion					 : csaapv.trasp.Descripcion,
					    /*Fin - Datos de Traspaso*/
					    IdEstadoEnvioNotificacion	 : "NEV",
					    IdEstadoSolicitud			 : "",
					    IdEstado					 : "PDI",
					    IdCargaMasiva				 : null,
					    //Usuario
						IdEmpleado					 : csaapv.IdEmpleadoEjecutivo === false || csaapv.IdEmpleadoEjecutivo === undefined ? 0 : csaapv.IdEmpleadoEjecutivo,
						IdPropositoPortafolio        : "",
						IdMatrizAgrupadorAPV         : 0,
						CodigoAgrupador              : "",
						}],
						p_DistribucionInversion 	 : csaapv.paramVentanaDistribuir.length === undefined ? [] : csaapv.paramVentanaDistribuir,
						p_ParametrosModificados		 : 0,
						p_DetalleOrigenTraspaso		 : [],
						Canal						 : "OPT"
					};
					ParametrosModificados = Array();
					if($scope.csaapv.ppcs.dsOrigenAhorro.view().length > 0){
						if(csaapv.parametrosEntrada.modoVentana === 'M'){
							
							$.each($scope.csaapv.ppcs.dsOrigenAhorro.view(),function(key,value){
								
								ParametrosModificados.push ({
									CodigoAgrupador				:	value.CodigoAgrupador,
									IdMatrizAgrupadorAPV		: 	value.IdMatrizAgrupadorAPV,
									OrigenAhorro				:	value.OrigenAhorro,
									IdPortafolio				:	value.IdPortafolio === undefined ? 0  :	value.IdPortafolio ,
									IdPropositoPortafolio		:	value.IdPropositoPortafolio,
									MesInicio					:	value.MesInicio,
									MesTermino					:	value.MesTermino,
									MontoPesos					:	value.MontoPesos,
									MontoUF						:	value.MontoUF,
									Pago						:	value.Pago,
									PagoNombre					:	value.PagoNombre,
									PorcentajeRegimenImponible	:	value.PorcentajeRegimenImponible,
									Regimen						:	value.Regimen,
									Producto 					: 	value.Producto,
									IdContratoSAAPVOrigen       :   value.IdPortafolio === undefined ? 0 : csaapv.parametrosEntrada.IdContratoSAAPV //se agrega saapv origen ya que no se guardaba al modificar
								});
									
							});
							parametros.p_PortafoliosModificados = ParametrosModificados;
						}else{
							$.each($scope.csaapv.ppcs.dsOrigenAhorro.view(),function(key,value){
								parametros.p_Parametros[0].IdPropositoPortafolio = value.IdPropositoPortafolio;
								    if(value.Regimen.substring(0, 3) === 'APV') parametros.p_Parametros[0].OrigenAhorro = 'APV';
									if(value.Regimen.substring(0, 2) === 'DC')parametros.p_Parametros[0].OrigenAhorroDC = 'DC';
									parametros.p_Parametros[0].MontoPesos  = value.MontoPesos;
									parametros.p_Parametros[0].MontoUF	  = value.MontoUF;
									parametros.p_Parametros[0].PorcentajeRegimenImponible = value.PorcentajeRegimenImponible;
									parametros.p_Parametros[0].IdMatrizAgrupadorAPV = value.IdMatrizAgrupadorAPV;
									parametros.p_Parametros[0].CodigoAgrupador = value.CodigoAgrupador;
									parametros.p_Parametros[0].RegimenTributario = value.Regimen;
									parametros.p_Parametros[0].Pago = value.Pago;
									parametros.p_Parametros[0].PeriodoInicio = value.MesInicio === undefined ? "0001-01" : value.MesInicio;
									parametros.p_Parametros[0].PeriodoTermino = value.MesTermino === undefined ? "9999-01" : value.MesTermino;
									parametros.p_Parametros[0].Producto = value.Producto;
							});

							$.each($scope.csaapv.ppcs.dsOrigenAhorro.view(),function(key,value){
								
								ParametrosModificados.push ({
									CodigoAgrupador				:	value.CodigoAgrupador,
									OrigenAhorro				:	value.OrigenAhorro,
									IdMatrizAgrupadorAPV		:	value.IdMatrizAgrupadorAPV,
									IdPortafolio				:	value.IdPortafolio === undefined ? 0  :	value.IdPortafolio ,
									IdPropositoPortafolio		:	value.IdPropositoPortafolio,
									MesInicio					:	value.MesInicio,
									MesTermino					:	value.MesTermino,
									MontoPesos					:	value.MontoPesos,
									MontoUF						:	value.MontoUF,
									Pago						:	value.Pago,
									PagoNombre					:	value.PagoNombre,
									PorcentajeRegimenImponible	:	value.PorcentajeRegimenImponible,
									Regimen						:	value.Regimen,
									Producto 					: 	value.Producto
								});
									
							});
							parametros.p_PortafoliosModificados = ParametrosModificados;
						}
					}
					
					/*INICIO - Esto aplica para los Traspasos*/
					if(csaapv.TipoSolicitudSeleccionado.Codigo === 'TRA' || csaapv.TipoTraspasoHabilito === true){
						if($scope.csaapv.ptrasp.dsOrigenTraspaso.view().length > 0){
							var DetalleOrigenTraspaso = Array();
							$.each($scope.csaapv.ptrasp.dsOrigenTraspaso.view(),function(key,value){
								DetalleOrigenTraspaso.push({
									 IdDetalleOrigenTraspaso	: 0
									,IdTraspaso					: csaapv.parametrosEntrada.modoVentana === 'M'?csaapv.parametrosEntrada.IdTraspaso:0
									,IdContratoSAAPVOrigen		: csaapv.parametrosEntrada.modoVentana === 'M'? csaapv.parametrosEntrada.IdContratoSAAPV : 0
									,IdEmpresa					: $scope.SesionIdEmpresa
									,CodigoTipoAhorro			: value.CodigoTipoAhorro
									,IdEntidadRecaudadora		: value.IdEntidadRecaudadora
									,NombreEntidad				: value.NombreEntidad
									,DocumentoEntidad			: value.DocumentoEntidad
									,Fondo						: value.Fondo
									,MontoTraspaso				: value.MontoTraspaso
									,UFTraspaso					: value.UFTraspaso
									,PorcentajeTraspaso			: value.PorcentajeTraspaso
									,IdEstado					: 'VIG'
									,IdInstrumento				: value.IdInstrumento
									,IdPortafolio				: value.IdPortafolio
									,CodigoRegimenTributario	: value.CodigoRegimenTributario
								});
							});
							parametros.p_DetalleOrigenTraspaso = DetalleOrigenTraspaso;
						};
					};
					/*FIN*/
					
					if(csaapv.parametrosEntrada.modoVentana === 'M'){
						//LLAMAR MODIFICAR 
						bcsServiciosAPV.ModificarContratoSAAPV(parametros).$promise.then(function(respuesta){
					   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
							if (respuesta.P_OUT_ERRORESCONTADOR === 0){
								csaapv.LimpioVariablesContrato();
								if(csaapv.TipoTraspasoHabilito === true)
									csaapv.ptrasp.LimpiarTraspaso();
								bcsComunes.ventanaMsg({mensaje: $translate.instant("UI_PROCESO_COMPLETADO"),conflictos:util.parseServerResponse(respuesta)},idVentana);
								bcsComunes.ventanaOverlay(idVentana,false);
							}
							else{
								var ErrorDetalle = respuesta.P_OUT_MENSAJES || [];
								$.each(ErrorDetalle,function(key,value){
									if(value.tipo === 'ERROR')
										bcsComunes.msg("error","Error","Existen problemas:" + value.descripcion );
								});
								bcsComunes.ventanaOverlay(idVentana,false);
						    }
				   		},function(respuesta){
							var ErrorDetalle = respuesta.P_OUT_MENSAJES || [];
				   			$.each(ErrorDetalle,function(key,value){
				   				if(value.tipo === 'ERROR')
				   					bcsComunes.msg("error","Error","Existen problemas:" + value.descripcion );
							});
				   			bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas:" ),conflictos:util.parseServerResponse(respuesta)},idVentana);
				   		});
					}else if(csaapv.parametrosEntrada.modoVentana === 'I'){
						//LLAMAR INGRESO 
						bcsServiciosAPV.CrearContratoSAAPV(parametros).$promise.then(function(respuesta){
					   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
							if (respuesta.P_OUT_ERRORESCONTADOR === 0){
								csaapv.LimpioVariablesContrato();
								
								if(csaapv.TipoSolicitudSeleccionado.Codigo === 'TRA')
									csaapv.ptrasp.LimpiarTraspaso();
								
								var mensajeTotal = new Array();
								$.each(respuesta.P_OUT_MENSAJES,function(key,value){
									 var objMensaje = {
											Id : value.id,
											tipo : value.tipo === 'OK' ? 'INFORMACION' : value.tipo,
											codigo : value.codigo,
											descripcion : $translate.instant(value.descripcion)
									 };
									 mensajeTotal.push(objMensaje);
								});
								respuesta.P_OUT_MENSAJES = mensajeTotal;
								
								bcsComunes.ventanaMsg({mensaje: $translate.instant("UI_PROCESO_COMPLETADO"),conflictos:util.parseServerResponse(respuesta)},idVentana);
								bcsComunes.ventanaOverlay(idVentana,false);
							}
							else{
								var ErrorDetalle = respuesta.P_OUT_MENSAJES || [];
								$.each(ErrorDetalle,function(key,value){	
									if(value.tipo === 'ERROR')
					   					bcsComunes.msg("error","Error","Existen problemas: " + value.descripcion);
								});
								bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas: " ),conflictos:util.parseServerResponse(respuesta)},idVentana);
								bcsComunes.ventanaOverlay(idVentana,false);
						    }
				   		},function(respuesta){
							var ErrorDetalle = respuesta.P_OUT_MENSAJES || [];
				   			$.each(ErrorDetalle,function(key,value){
				   				if(value.tipo === 'ERROR')
				   					bcsComunes.msg("error","Error","Existen problemas: " + value.descripcion);
							});
				   			bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas: " ),conflictos:util.parseServerResponse(respuesta)},idVentana);
				   		});
							
					}				
			});
		};

		// Valida formato Telefono
		csaapv.placeholderTelefono = $scope.$root.SesionValoresDefectoSistema.PLACEHOLDER_TELEFONO;
		csaapv.validaFormatoTelefono = function(telefono,tipo)
		{
			if($scope.SesionLocale=='PE')return true;
	    	
    		if (telefono !== undefined && telefono !== '' && region.ValidaFormatoNumeroTelefono(telefono) == false)
    		{
    			if(tipo === "TFIJO")
    				csaapv.TelefonoFijo = '';	
    			
    			if(tipo === "TMOVIL")
    				csaapv.TelefonoMovil = '';

    			if(tipo === "TLABO")
    				csaapv.TelefonoLaboral = '';

    			if(tipo === "TEMP")
    	    		csaapv.pecs.TelefonoEmpleador = '';

    			if(tipo === "TENC")
    	    		csaapv.pecs.TelefonoEncargado = '';

    			bcsComunes.msg("warning","warning",$translate.instant("UI_FORMATO_NUMERO_TELEFONO_INCORRECTO").replace("XXPLACEHOLDERXX",csaapv.placeholderTelefono));
				$scope.formContratoSAAPV.$valid = undefined;
				return false;
			}

	    	return true;
	    };
		
		// Valida formato email
		csaapv.validaFormatoEmail = function(email)
		{
			if(email === 'CLI')
			{
				if (csaapv.EmailCliente !== undefined && csaapv.EmailCliente !== '' && region.ValidaFormatoEmail(csaapv.EmailCliente)==false)
				{
					csaapv.EmailCliente = '';
		    		bcsComunes.msg("warning","warning",$translate.instant("UI_FORMATO_EMAIL_INCORRECTO"));
					$scope.formContratoSAAPV.$valid = undefined;
					return false;
				}	
			}
	    	
	    	return true;
	    };
	    
		// Valida fecha nacimiento
		csaapv.validaFechaNacimiento = function()
		{
			if (csaapv.kngFechaNacimiento !== undefined 
					&& csaapv.kngFechaNacimiento !== '' 
						&& region.validarFechaNacimiento(csaapv.kngFechaNacimiento, new Date($scope.$root.SesionFechaSistema) , 100)==false){
				bcsComunes.msg("warning","warning",$translate.instant("UI_FECHA_IMPROBABLE"));
				return false;
			}
			return true;
		};

		
		csaapv.ValidaDocumentoIdentidad = function(tipo,persona){
			if(!tipo) return;
			var d;
			if(persona === 'CLI')
				d = csaapv.DocumentoIdentidadCliente || "";
			if(persona === 'EMP')
				d = csaapv.pecs.DocumentoIdentidadEmpleador || "";
			
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
				csaapv.DocumentoIdentidadCliente = d;
			if(persona === 'EMP')
				csaapv.pecs.DocumentoIdentidadEmpleador = d;
		};
		
		
		/*----------CONSULTA CONTRATOS -----------------------------*/		
		csaapv.ConsultaContratoSAAPV = function(IdContratoSAAPV)
		{
			if(csaapv.IdContratoSAAPV === undefined)
				return;
				csaapv.HabilitarFolio = (csaapv.TipoSolicitudSeleccionado.Codigo !== 'TRA' || (csaapv.TipoSolicitudSeleccionado.Codigo === 'TRA' && csaapv.OrigenTraspasoSeleccionado.Codigo !== 'EGR'))?true:false;
				var filtro = Array();
				if (IdContratoSAAPV == 0) {
					filtro.push({ field: "IdTraspaso", operator: "eq", value: Number(csaapv.parametrosEntrada.IdTraspaso) });

					var ConsultaTraspaso = new bcs.DataSource(defOrigenTraspasoEgresos);
					ConsultaTraspaso.query({ filter: filtro }).then(function() {
						if (ConsultaTraspaso.view().length > 0) {
							if (csaapv.parametrosEntrada.CodigoTipoSolicitud === 'TRA') {
								/*Activo TAB de Traspaso*/

								csaapv.IdTraspaso = ConsultaTraspaso.view()[0].IdTraspaso;
								csaapv.IdContratoSAAPV = ConsultaTraspaso.view()[0].IdContratoSAAPV
								csaapv.TipoMovimiento = ConsultaTraspaso.view()[0].TipoMovimiento
								csaapv.RazonRetiro = ConsultaTraspaso.view()[0].RazonRetiro
								csaapv.FechaIngreso = ConsultaTraspaso.view()[0].FechaIngreso
								csaapv.CodigoTipoTraspaso = ConsultaTraspaso.view()[0].CodigoTipoTraspaso
								csaapv.IdCliente = ConsultaTraspaso.view()[0].IdCliente
								csaapv.IdPersona = ConsultaTraspaso.view()[0].IdPersona
								csaapv.Descripcion = ConsultaTraspaso.view()[0].Descripcion
								csaapv.IdSocioNegocioCli = ConsultaTraspaso.view()[0].IdSocioNegocioCli
								csaapv.IdSocioNegocioEMP = ConsultaTraspaso.view()[0].IdSocioNegocioEMP
								csaapv.IdDetalleOrigenTraspaso = ConsultaTraspaso.view()[0].IdDetalleOrigenTraspaso
								csaapv.TipoSolicitudSeleccionado = true;
								csaapv.TipoSolicitudSeleccionado.Codigo = 'TRA';
								csaapv.CodigoTipoAhorro = ConsultaTraspaso.view()[0].CodigoTipoAhorro
								csaapv.pecs.entidadRecaudadora.IdEntidadRecaudadora = ConsultaTraspaso.view()[0].IdEntidadRecaudadora
								csaapv.NombreEntidad = ConsultaTraspaso.view()[0].NombreEntidad
								csaapv.DocumentoEntidad = parametrosVentana.DocumentoIdentidad
								csaapv.Fondo = ConsultaTraspaso.view()[0].Fondo
								csaapv.kngFechaSuscripcion = ConsultaTraspaso.view()[0].FechaSuscripcion;
								csaapv.MontoTraspaso = ConsultaTraspaso.view()[0].MontoTraspaso
								csaapv.UFTraspaso = ConsultaTraspaso.view()[0].UFTraspaso
								csaapv.PorcentajeTraspaso = ConsultaTraspaso.view()[0].PorcentajeTraspaso
								csaapv.IdEstado = ConsultaTraspaso.view()[0].IdEstado
								csaapv.datosForm.SocioNegocioSeleccionado = ConsultaTraspaso.view()[0];
								csaapv.datosForm.SocioNegocioSeleccionado.IdCliente = ConsultaTraspaso.view()[0].IdCliente;
								csaapv.kNgDisabled = true;
								csaapv.fnCargarValoresPersona('N');
								csaapv.tabs['PanelPortafolioContratoTraspaso'].active = true;
								csaapv.tabs['PanelPortafolioContrato'].active = false;
								csaapv.TabOcultoTraspaso = false;
								csaapv.OrigenTraspasoIdActual = ConsultaTraspaso.view()[0].TipoMovimiento === 'INGRESO' ? 'ING' : 'EGR';
								csaapv.OrigenTraspasoIdActualDefecto = ConsultaTraspaso.view()[0].TipoMovimiento === 'INGRESO' ? 'ING' : 'EGR';
								csaapv.habilitoOrigenTraspaso = false;
								csaapv.CodigoTipoSolicitud = 'TRA';
								csaapv.TabOcultoEmpleador = false;
								csaapv.TabOcultoPortafolio = true;
								csaapv.TipoTraspasoHabilito = true;
								csaapv.TipoSolicitudIdActual = 'MOD';
								csaapv.kngPorcentajeTraspaso = ConsultaTraspaso.view()[0].PorcentajeTraspaso;
								csaapv.FolioContratoDisable = false;
								csaapv.TabOcultoTraspaso = true;
								csaapv.HabilitarFolio = false;
								var filtroTraspaso = Array();
								filtroTraspaso.push({ field: "IdTraspaso", operator: "eq", value: Number(ConsultaTraspaso.view()[0].IdTraspaso) });
								if (ConsultaTraspaso.view()[0].IdEstado === 'ING') {
									csaapv.tabs['PanelPortafolioContratoTraspaso'].active = true;
									csaapv.TabOcultoTraspaso = true;
									csaapv.TabTraspasoConsulta = false;
									var ConsultaDetalleTraspaso = new bcs.DataSource(defDetalleTraspaso);
									ConsultaDetalleTraspaso.query({ filter: filtroTraspaso }).then(function() {
										if (ConsultaDetalleTraspaso.view().length > 0) {
											//console.log(ConsultaDetalleTraspaso.view()[0].IdTraspaso);
										}
									});
								}
							}

						}
					});
				}
				else {
			filtro.push({ field: "IdContratoSAAPV" , operator: "eq", value: Number(csaapv.IdContratoSAAPV) });
			
			var ConsultaContrato = new bcs.DataSource(datosSAAPV);
			ConsultaContrato.query({ filter: filtro }).then(function(){
				if(ConsultaContrato.view().length > 0)
				{
					//Plan Ahorro
					if(ConsultaContrato.view()[0].IdEstado !== 'VIG')
						csaapv.FolioContrato 					= ConsultaContrato.view()[0].FolioContratoSAAPV;
					
					csaapv.kngFechaSuscripcion				= ConsultaContrato.view()[0].FechaSuscripcion;
					csaapv.PlanAhorroIdActual				= ConsultaContrato.view()[0].IdPlan;
					csaapv.PlanAhorroIdActualPost			= ConsultaContrato.view()[0].IdPlan;
					csaapv.TipoSolicitudIdActual			= 'MOD';
					csaapv.FolioBusqueda					= ConsultaContrato.view()[0].FolioContratoSAAPV;
					csaapv.IdEstado							= ConsultaContrato.view()[0].IdEstado;
					//Identificación Trabajador
					csaapv.IdPersona					    = ConsultaContrato.view()[0].IdPersona;
					csaapv.IdCliente					    = ConsultaContrato.view()[0].IdCliente;
					csaapv.IdEmpleador					    = ConsultaContrato.view()[0].IdEmpleador;
					csaapv.DocumentoIdentidadCliente		= ConsultaContrato.view()[0].DocumentoIdentidadCliente;
					csaapv.Nombres 							= ConsultaContrato.view()[0].NombreCLiente;	
					csaapv.ApellidoPaterno					= ConsultaContrato.view()[0].ApellidoPaternoCliente;
					csaapv.ApellidoMaterno					= ConsultaContrato.view()[0].ApellidoMaternoCliente;
					csaapv.kngFechaNacimiento				= ConsultaContrato.view()[0].FechaNacimientoCliente;
					csaapv.NacionalidadIdActual				= ConsultaContrato.view()[0].Nacionalidad;
					csaapv.NacionalidadIdActualPost			= ConsultaContrato.view()[0].Nacionalidad;
					csaapv.EstadoCivilIdActual 				= ConsultaContrato.view()[0].EstadoCivil;
					csaapv.EstadoCivilIdActualPost			= ConsultaContrato.view()[0].EstadoCivil;
					csaapv.GeneroIdActual					= ConsultaContrato.view()[0].Genero;
					csaapv.GeneroIdActualPost				= ConsultaContrato.view()[0].Genero;
					csaapv.IdPaisResidenciaIdActual			= ConsultaContrato.view()[0].IdPaisResidenciaCliente;
					csaapv.IdPaisResidenciaIdActualPost		= ConsultaContrato.view()[0].IdPaisResidenciaCliente;
					csaapv.Ciudad							= ConsultaContrato.view()[0].NombreCiudad;
					csaapv.CiudadIdActual					= ConsultaContrato.view()[0].IdCiudadCliente;
					csaapv.CiudadIdActualPost				= ConsultaContrato.view()[0].IdCiudadCliente;
					csaapv.Comuna							= ConsultaContrato.view()[0].NombreComuna;
					csaapv.idDivisionPaisComuna				= ConsultaContrato.view()[0].IdComunaCliente;
					csaapv.idDivisionPaisComunaPost			= ConsultaContrato.view()[0].IdComunaCliente;
					csaapv.Calle							= ConsultaContrato.view()[0].DireccionCliente;
					csaapv.kngNumeroDireccionCliente		= ConsultaContrato.view()[0].NumeroDirecionCliente;
					csaapv.IdActividadCargo					= ConsultaContrato.view()[0].IdTipoCargoPersona;
					csaapv.IdActividadCargoPost				= ConsultaContrato.view()[0].IdTipoCargoPersona;
					csaapv.TelefonoFijo						= ConsultaContrato.view()[0].TelefonoFijo;
					csaapv.TelefonoMovil					= ConsultaContrato.view()[0].TelefonoMovil;
					csaapv.TelefonoLaboral					= ConsultaContrato.view()[0].TelefonoLaboral;
					csaapv.EmailCliente						= ConsultaContrato.view()[0].EmailCliente;
					csaapv.IdEmpleadoEjecutivo				= ConsultaContrato.view()[0].IdEmpleadoEjecutivo;
					csaapv.IdEmpleadoEjecutivoPost  		= ConsultaContrato.view()[0].IdEmpleadoEjecutivo;
					csaapv.IdCategoriaInversionista			= ConsultaContrato.view()[0].IdCategoriaInversionista;
					csaapv.IdCategoriaInversionistaPost		= ConsultaContrato.view()[0].IdCategoriaInversionista;
					csaapv.IdPerfilComercialBaseOrigen	    = ConsultaContrato.view()[0].IdPerfilComercialBaseOrigen;
					csaapv.IdPerfilComercialBaseOrigenPost	= ConsultaContrato.view()[0].IdPerfilComercialBaseOrigen;
					csaapv.CiudadTrabajador					= ConsultaContrato.view()[0].NombreCiudad;
					
					/*Esto aplica solo para los traspasos*/
					if(ConsultaContrato.view()[0].CodigoTipoSolicitud === 'TRA')
					{
						/*Activo TAB de Traspaso*/
						csaapv.tabs['PanelPortafolioContratoTraspaso'].active = true;
						csaapv.TabOcultoTraspaso = false;
						
						var ConsultaTraspaso = new bcs.DataSource(defTraspaso);
						ConsultaTraspaso.query({ filter: filtro }).then(function(){
							if(ConsultaTraspaso.view().length > 0)
							{
								csaapv.TipoTraspasoHabilito = true;
								csaapv.habilitoOrigenTraspaso = false;
								
								csaapv.OrigenTraspasoIdActual = ConsultaTraspaso.view()[0].TipoMovimiento === 'INGRESO' ? 'ING' : 'EGR';
								csaapv.OrigenTraspasoIdActualDefecto = ConsultaTraspaso.view()[0].TipoMovimiento === 'INGRESO' ? 'ING' : 'EGR';
								
								var filtroTraspaso = Array(); 
								filtroTraspaso.push({ field: "IdTraspaso" , operator: "eq", value: Number(ConsultaTraspaso.view()[0].IdTraspaso) });
								if(ConsultaTraspaso.view()[0].IdEstado === 'ING'){
									//csaapv.tabs['PanelPortafolioContratoTraspaso'].active = true;
									csaapv.TabOcultoTraspaso = true;
									csaapv.TabTraspasoConsulta = true;
									var ConsultaDetalleTraspaso = new bcs.DataSource(defDetalleTraspaso);
									ConsultaDetalleTraspaso.query({ filter: filtroTraspaso }).then(function(){
										if(ConsultaDetalleTraspaso.view().length > 0){
											
										}
									});
								}
							}
						});
					}else{
						/*Activo TAB de Portafolio*/
						csaapv.tabs['PanelPortafolioContratoTraspaso'].active = false;
						csaapv.tabs['PanelPortafolioContrato'].active = true;
						csaapv.TabOcultoTraspaso = true;
						csaapv.TipoTraspasoHabilito = false;
						csaapv.TabOcultoEmpleador = false;
						csaapv.habilitoOrigenTraspaso = true;
					}	
					
					csaapv.fnBloquearIdentificacionTrabajador();
					
					datosEmpleador.push(
					{
						//Identificación Empleador
						RazonSocial					: ConsultaContrato.view()[0].NombreEmpleador,
						DocumentoIdentidadEmpleador	: ConsultaContrato.view()[0].DocumentoIdentidadEmpleador,
						ComunaEmpleador				: ConsultaContrato.view()[0].IdComunaEmpleador,
						CiudadEmpleador				: ConsultaContrato.view()[0].NombreCiudadEmpleador,
						DireccionComercialEmpleador	: ConsultaContrato.view()[0].DireccionComercialEmpleador,
						NumeroDireccionEmpleador	: ConsultaContrato.view()[0].NumeroDirecionEmpleador,
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
					
					csaapv.datosEmpleador = datosEmpleador;
					
					var ConsultaDistribucion = new bcs.DataSource(defDistribucion);
					ConsultaDistribucion.query({ filter : filtro }).then(function(){
						if(ConsultaDistribucion.view().length > 0){
							csaapv.paramVentanaDistribuir = ConsultaDistribucion.view(); 
						}
					});
				}
				
				if(ConsultaContrato.view().length > 0)
				{	
					if(ConsultaContrato.view()[0].IdPortafolio > 0)
					{
						var Pagonombre = '';
						$.each(ConsultaContrato.view(),function(key,value)
						{
							/*ORIGEN Y MONTOS*/
							if(value.Pago === 'IND')
								Pagonombre = 'INDEFINIDO';
							else if(value.Pago === 'TEM')
								Pagonombre = 'TEMPORAL';
							else if(value.Pago === 'UNI')
								Pagonombre = 'UNICO';
							else
								Pagonombre = '';
							
							csaapv.datosPortafolio.push({
								IdPortafolio		  		: value.IdPortafolio,
								Producto 			  		: value.Producto,
								OrigenAhorro		  		: value.OrigenAhorro,
								MontoPesos			  		: value.MontoPesos,
								MontoUF				  		: value.MontoUF,
								PorcentajeRegimenImponible	: value.PorcentajeRegimenImponible,
								Pago				  		: value.Pago,
								PagoNombre			  		: Pagonombre,
								MesInicio			  		: value.PeriodoInicio,
								MesTermino			  		: value.PeriodoTermino,
								Regimen				  		: value.RegimenTributario,
								IdMatrizAgrupadorAPV  		: value.IdMatrizAgrupadorAPV,
								CodigoAgrupador		  		: value.CodigoAgrupador,
								IdPropositoPortafolio 		: value.Proposito
							});	
						});
					}else {
						$scope.csaapv.habilitoPlanAhorro = false;
					}
					$scope.csaapv.ppcs.dsOrigenAhorro.data(csaapv.datosPortafolio)
				}
			});
      }
		};
		
		$scope.csaapv.limpiarCliente = function()
		{			
			csaapv.datosForm.SocioNegocioidActual 	= false;
			csaapv.DocumentoIdentidadCliente 		= "";
			csaapv.Nombres 							= "";	
			csaapv.ApellidoPaterno					= "";
			csaapv.ApellidoMaterno					= "";
			csaapv.IdPersona 						= "";
			csaapv.kngFechaNacimiento				= "";
			csaapv.EstadoCivilIdActual 				= false;
			csaapv.EstadoCivilIdActualPost			= false;
			csaapv.GeneroIdActual					= false;
			csaapv.GeneroIdActualPost				= false;
			csaapv.CiudadIdActual					= false;
			csaapv.CiudadIdActualPost				= false;
			csaapv.idDivisionPaisComuna				= false;
			csaapv.idDivisionPaisComunaPost			= false;
			csaapv.IdPaisResidenciaIdActual			= 44;
			csaapv.IdPaisResidenciaIdActualPost		= 44;
			csaapv.Calle							= "";
			csaapv.kngNumeroDireccionCliente		= "";
			csaapv.IdActividadCargo					= false;
			csaapv.IdActividadCargoPost				= false;
			csaapv.IdEmpleadoEjecutivo				= false;
			csaapv.IdEmpleadoEjecutivoPost			= false;
			csaapv.IdCategoriaInversionista			= false;
			csaapv.IdCategoriaInversionistaPost		= false;
			csaapv.IdPerfilComercialBaseOrigen		= false;
			csaapv.IdPerfilComercialBaseOrigenPost	= false;
			csaapv.TelefonoFijo						= "";
			csaapv.TelefonoMovil					= "";
			csaapv.TelefonoLaboral					= "";
			csaapv.EmailCliente						= "";
			csaapv.CiudadTrabajador					= "";
			csaapv.NacionalidadIdActual				= "";
			csaapv.NacionalidadIdActualPost			= "";
			
			csaapv.datosForm.NombreDeshabilitado 			= true;
			csaapv.datosForm.EjecutivoDeshabilitado			= true;
			csaapv.datosForm.ApellidoPatDeshabilitado 		= true;
			csaapv.datosForm.ApellidoMatDeshabilitado		= true;
			csaapv.datosForm.DoctoIdentDeshabilitado		= true;
			csaapv.datosForm.FechaNacDeshabilitado			= true;
			csaapv.datosForm.EstadoCivilDeshabilitado		= true;
			csaapv.datosForm.GeneroDeshabilitado			= true;
			csaapv.datosForm.PaisDeshabilitado				= true;
			csaapv.datosForm.ComunaDeshabilitado			= true;
			csaapv.datosForm.CiudadDeshabilitado			= true;
			csaapv.datosForm.CalleDeshabilitado				= true;
			csaapv.datosForm.CalleNumeroDeshabilitado		= true;
			csaapv.datosForm.ActividadCargoDeshabilitado	= true;
			csaapv.datosForm.CatInverDeshabilitado			= true;
			csaapv.datosForm.PerfilDeshabilitado			= true;
			csaapv.datosForm.telefonoFijoDeshabilitado		= true;
			csaapv.datosForm.telefonoMovilDeshabilitado		= true;
			csaapv.datosForm.telefonoLaboralDeshabilitado	= true;
			csaapv.datosForm.EmailDeshabilitado				= true;

			csaapv.DocumentoIdentidadRequerido 	= false;
			csaapv.NombresRequerido				= false;
			csaapv.ApellidoPaternoRequerido		= false;
			csaapv.ApellidoMaternoRequerido		= false;
			csaapv.PaisResidenciaRequerido		= false;
			csaapv.CiudadRequerido				= false;
			csaapv.DivisionPaisRequerido		= false;
			csaapv.CalleRequerido				= false;
			csaapv.NumeroDireccionRequerido		= false;
			csaapv.EjecutivoRequerido			= false;
			csaapv.CategoriaoRequerido			= false;
			csaapv.PerfilRequerido				= false;
		}

		csaapv.LimpioVariablesContrato = function() 
		{	
			//Plan Ahorro
			csaapv.FolioContrato 		= undefined;
			csaapv.kngFolioContrato		= undefined;
			csaapv.PlanAhorroIdActual	= undefined;
			csaapv.FolioBusqueda 		= undefined;
			
			//Identificación Trabajador
			csaapv.limpiarCliente();
			
			//Origen Y Monto Ahorro
			csaapv.idMatrizAgrupador				= undefined;
			csaapv.ppcs.RegimenAgrupadorIdActual	= undefined;
			csaapv.ppcs.kngMontoPesoOrigen			= undefined;
			csaapv.ppcs.kngMontoUFOrigen			= undefined;
			csaapv.ppcs.kngPorcentajeOrigen			= undefined;
			csaapv.ppcs.TipoPagoIdActual			= undefined;
			csaapv.ppcs.kngPeriodoInicio			= undefined;
			csaapv.ppcs.kngPeriodoTermino			= undefined;
			$scope.csaapv.ppcs.dsOrigenAhorro.removeAll();
			
			//Identificación Empleador
			//Encargado RRHH
			if(csaapv.pecs.limpiarEmpleador != undefined)
				csaapv.pecs.limpiarEmpleador();
			
			//Tipo Trabajador
			csaapv.pecs.MedioRecaudacionTrabajadorIdActual	= undefined;
			csaapv.pecs.IdentidadRecaudadoraIdActual		= undefined;
			csaapv.pecs.Codigo								= undefined;
			csaapv.pecs.PensionadoIdActual					= undefined;
			csaapv.pecs.TrabajadorIdActual					= undefined;
			
			//Forma Pago
			csaapv.pecs.FormaPagoDirectaIdActual					= undefined;
			csaapv.pecs.DiaDescuentoIdActual						= undefined;
			csaapv.pecs.MedioRecaudacionFormaPagoIndirectaIdActual	= undefined;
			csaapv.pecs.EntidadRecaudadoraInstitucion				= undefined;
			csaapv.pecs.kngComisionTransferencia					= undefined;
			
			//Otros
			csaapv.habilitoPlanAhorro								= false;
			if(csaapv.pecs.limpiarEmpleador != undefined){
				csaapv.datosForm.SocioNegocioDesabilitado			= true;
				csaapv.pecs.datosForm.SocioNegocioEmpDesabilitado	= true;	
			}
			csaapv.limpiarClienteHabilitado			= true;
			csaapv.pecs.limpiarEmpleadorHabilitado	= true;
			
			if(csaapv.parametrosEntrada.CodigoSolicitud === 'AUT'){
				csaapv.TipoSolicitudIdActual				= 'AUT';
				csaapv.TipoSolicitudIdPostFiltro			= 'AUT';
			}else if(csaapv.parametrosEntrada.CodigoSolicitud === 'TRA'){
				csaapv.TipoSolicitudIdActual				= 'TRA';
				csaapv.TipoSolicitudIdPostFiltro			= 'TRA';
			}
			/*
			csaapv.TipoSolicitudIdActual		= 'AUT';
			csaapv.TipoSolicitudIdPostFiltro	= 'AUT';
			*/
			csaapv.IdPaisResidenciaIdActual 	= 44;
			csaapv.IdPaisResidenciaIdActualPost = 44;
			
			ParametrosModificados = {};
			csaapv.paramVentanaDistribuir = {};
			csaapv.parametrosEntrada.modoVentana = 'I';
			csaapv.kngFechaSuscripcion = undefined;
			
			if(parametrosVentana.CodigoSolicitud === "TRA"){
				csaapv.ptrasp.GlosaTraspasoRetiro = undefined;
				csaapv.ptrasp.RazonRetiroIdActual = undefined;
				csaapv.ptrasp.RazonRetiroSeleccionado = false;
			}
		};
		
		$scope.csaapv.HabilitaCampos = function()
		{	
			csaapv.datosForm.NombreDeshabilitado 			= false;
			csaapv.datosForm.EjecutivoDeshabilitado			= false;
			csaapv.datosForm.ApellidoPatDeshabilitado 		= false;
			csaapv.datosForm.ApellidoMatDeshabilitado		= false;
			csaapv.datosForm.DoctoIdentDeshabilitado		= false;
			csaapv.datosForm.FechaNacDeshabilitado			= false;
			csaapv.datosForm.EstadoCivilDeshabilitado		= false;
			csaapv.datosForm.GeneroDeshabilitado			= false;
			csaapv.datosForm.PaisDeshabilitado				= false;
			csaapv.datosForm.ComunaDeshabilitado			= false;
			csaapv.datosForm.CiudadDeshabilitado			= false;
			csaapv.datosForm.CalleDeshabilitado				= false;
			csaapv.datosForm.CalleNumeroDeshabilitado		= false;
			csaapv.datosForm.ActividadCargoDeshabilitado	= false;
			csaapv.datosForm.CatInverDeshabilitado			= false;
			csaapv.datosForm.telefonoFijoDeshabilitado		= false;
			csaapv.datosForm.telefonoMovilDeshabilitado		= false;
			csaapv.datosForm.telefonoLaboralDeshabilitado	= false;
			csaapv.datosForm.EmailDeshabilitado				= false;
			csaapv.datosForm.PerfilDeshabilitado			= false;
			
			csaapv.DocumentoIdentidadRequerido 	= true;
			csaapv.NombresRequerido				= true;
			csaapv.ApellidoPaternoRequerido		= true;
			csaapv.ApellidoMaternoRequerido		= true;
			csaapv.PaisResidenciaRequerido		= true;
			csaapv.CiudadRequerido				= true;
			csaapv.DivisionPaisRequerido		= true;
			csaapv.CalleRequerido				= true;
			csaapv.NumeroDireccionRequerido		= true;
			csaapv.EjecutivoRequerido			= true;
			csaapv.CategoriaoRequerido			= true;
			csaapv.PerfilRequerido				= true;
			
			/*INICIO*/
			/*Cuando es traspaso debe quedar deshabilitado ya que esto se llena en el formulario de Ingreso Manual de Traspaso*/
			/*
			if(csaapv.TipoSolicitudSeleccionado.Codigo === 'TRA'){
				csaapv.datosForm.PerfilDeshabilitado 		= true;
				csaapv.IdPerfilComercialBaseOrigen 			= false;
				csaapv.PerfilRequerido						= false;
			}else{
				csaapv.datosForm.PerfilDeshabilitado		= false;
			}
			*/
			/*FIN*/
			
		};
		
		/******************************************************/
		/*** WATCH											***/
		/******************************************************/		
		$scope.$watch('csaapv.OrigenTraspasoSeleccionado',function(newval,oldval){
			if(csaapv.OrigenTraspasoSeleccionado !== undefined && csaapv.OrigenTraspasoSeleccionado !== false)
			{
				if(csaapv.OrigenTraspasoSeleccionado.Nombre === 'EGRESO'){
					csaapv.tabs['PanelPortafolioContratoTraspaso'].active = true;
					csaapv.TabOcultoTraspaso = false;
					csaapv.TabOcultoEmpleador = true;
					csaapv.OcultoControlesEgreso = true;
					csaapv.TipoTraspasoHabilito = true;
				}else{
					csaapv.tabs['PanelPortafolioContratoTraspaso'].active = true;
					csaapv.TabOcultoTraspaso = false;
					csaapv.TabOcultoEmpleador = false;
					csaapv.TipoTraspasoHabilito = true;
					csaapv.OcultoControlesEgreso = false;
				}	
			}
		});		
		
		$scope.$watch('csaapv.TipoSolicitudSeleccionado',function(newval,oldval)
		{
			if(csaapv.TipoSolicitudSeleccionado !== undefined && csaapv.TipoSolicitudSeleccionado !== false)
			{
				if(csaapv.TipoSolicitudSeleccionado.Codigo === 'AUT'){
					csaapv.FolioBusqueda = '';
					csaapv.tabs['PanelPortafolioContratoTraspaso'].active = false;
					csaapv.tabs['PanelPortafolioContrato'].active = true;
					csaapv.TabOcultoTraspaso = true;
					csaapv.TipoTraspasoHabilito = false;
					csaapv.TabOcultoEmpleador = false;
					csaapv.OcultoControlesEgreso = false;
				}else if(csaapv.TipoSolicitudSeleccionado.Codigo === 'TRA' && (csaapv.parametrosEntrada.modoVentana === 'I' || csaapv.parametrosEntrada.modoVentana === 'M')){
					csaapv.tabs['PanelPortafolioContratoTraspaso'].active = true;
					csaapv.TabOcultoTraspaso = false;
					csaapv.TabOcultoEmpleador = false;
					csaapv.TipoTraspasoHabilito = true;
					
					if (csaapv.parametrosEntrada.modoVentana === 'I' && csaapv.parametrosEntrada.tipoTraspaso !== undefined ){
						csaapv.habilitoOrigenTraspaso = false;
					}
				}
				csaapv.fnTituloVentana();
				
				if(csaapv.parametrosEntrada.modoVentana === 'I')
					csaapv.limpiarCliente();
					if(csaapv.parametrosEntrada.SocioNegocio!=undefined){
						console.log(csaapv.parametrosEntrada.SocioNegocio)
						csaapv.TipoSolicitudSeleccionado = true;
						csaapv.TipoSolicitudSeleccionado.Codigo = 'TRA';
						csaapv.datosForm.SocioNegocioSeleccionado = csaapv.parametrosEntrada.SocioNegocio;
					}	
			}
		});
		
		$scope.$watch('csaapv.datosForm.SocioNegocioSeleccionado',function(newval,oldval)
		{
			if(csaapv.datosForm.SocioNegocioSeleccionado !==  false && csaapv.datosForm.SocioNegocioSeleccionado !==  undefined)
			{
				csaapv.fnCargarValoresPersona('N');
				csaapv.DocumentoIdentidadRequerido 	= false;
				csaapv.NombresRequerido				= false;
				csaapv.ApellidoPaternoRequerido		= false;
				csaapv.ApellidoMaternoRequerido		= false;
				csaapv.PaisResidenciaRequerido		= false;
				csaapv.CiudadRequerido				= false;
				csaapv.DivisionPaisRequerido		= false;
				csaapv.CalleRequerido				= false;
				csaapv.NumeroDireccionRequerido		= false;
				if(csaapv.TipoSolicitudSeleccionado.Codigo === 'TRA' && csaapv.OrigenTraspasoSeleccionado.Nombre === 'EGRESO'){
					if(csaapv.datosForm.SocioNegocioSeleccionado.IdCliente === undefined 
							|| csaapv.datosForm.SocioNegocioSeleccionado.IdCliente === false
								||csaapv.datosForm.SocioNegocioSeleccionado.IdCliente === 0){
						bcsComunes.msg("warning","warning","La Persona seleccionada no es Cliente.");
						csaapv.limpiarCliente();
						return;
					}
				}
			}
			
			csaapv.fnValidaContrato();
		});
		
		$scope.$watch('csaapv.SocData._prev',function(newval,oldval)
		{
			if(newval === "" && oldval !== undefined ){
				/*No se debe permitir igresar cliente a mano si es de tipo Traspaso Egreso*/
				if(csaapv.TipoSolicitudSeleccionado !== undefined && csaapv.OrigenTraspasoSeleccionado !== undefined){
					if(csaapv.TipoSolicitudSeleccionado.Codigo === 'TRA' && csaapv.OrigenTraspasoSeleccionado.Nombre === 'EGRESO'){
						return;
					}	
				}
				
				$scope.csaapv.limpiarCliente();
				if (isNaN(oldval.substring(0,1)) === false ){
					var largo=(oldval).length;
					if (largo < 12)
					{
						csaapv.DocumentoIdentidadCliente = trim(oldval);
						$scope.csaapv.HabilitaCampos();
						
					}else{
						csaapv.DocumentoIdentidadCliente	= "";
						if(csaapv.DocumentoIdentidadCliente	!== ""){
							
						}
					}
				}else{
					$scope.csaapv.HabilitaCampos();
				}
			}
		});		
		
		$scope.$watch('csaapv.idDivisionPaisComuna',function(newval,oldval)
		{
			if(csaapv.parametrosEntrada.modoVentana === 'I')
			{
				if(csaapv.idDivisionPaisComuna !== undefined && csaapv.idDivisionPaisComuna !== false && csaapv.comunaSeleccionada.IdCiudad !==undefined)
				{
					var ConsultaCiudad = new bcs.DataSource(datosCiudad);
					var filtro = { field: "IdCiudad", operator:"eq", value: Number(csaapv.comunaSeleccionada.IdCiudad) };
					
					ConsultaCiudad.query({ filter: filtro }).then(function()
					{
						csaapv.CiudadTrabajador = ConsultaCiudad.view()[0].NombreCiudad;
					});
				}
				else
				{
					csaapv.CiudadTrabajador = undefined;
				}
			}
		});
				
		if(csaapv.parametrosEntrada.modoVentana === 'I')
		{
			if(csaapv.parametrosEntrada.CodigoSolicitud === 'AUT'){
				csaapv.TipoSolicitudIdActual				= 'AUT';
				csaapv.TipoSolicitudIdPostFiltro			= 'AUT';
			}else if(csaapv.parametrosEntrada.CodigoSolicitud === 'TRA'){
				csaapv.TipoSolicitudIdActual				= 'TRA';
				csaapv.TipoSolicitudIdPostFiltro			= 'TRA';
			}
			csaapv.habilitoTipoSolicitud				= true;
			csaapv.limpiarClienteHabilitado 			= true;	
			csaapv.datosForm.SocioNegocioDesabilitado	= true;
			csaapv.DocumentoIdentidadRequerido 			= false;
			csaapv.NombresRequerido						= false;
			csaapv.ApellidoPaternoRequerido				= false;
			csaapv.ApellidoMaternoRequerido				= false;
			csaapv.PaisResidenciaRequerido				= false;
			csaapv.CiudadRequerido						= false;
			csaapv.DivisionPaisRequerido				= false;
			csaapv.CalleRequerido						= false;
			csaapv.NumeroDireccionRequerido				= false;
			csaapv.IdPaisResidenciaIdActualPost 		= 44;
		}
		else if(csaapv.parametrosEntrada.modoVentana === 'M')
		{
			csaapv.habilitoPlanAhorro					= true;
			csaapv.habilitoTipoSolicitud				= true;
			csaapv.datosForm.SocioNegocioDesabilitado	= false;
			csaapv.DocumentoIdentidadRequerido 			= false;
			csaapv.NombresRequerido						= false;
			csaapv.ApellidoPaternoRequerido				= false;
			csaapv.ApellidoMaternoRequerido				= false;
			csaapv.PaisResidenciaRequerido				= false;
			csaapv.CiudadRequerido						= false;
			csaapv.DivisionPaisRequerido				= false;
			csaapv.CalleRequerido						= false;
			csaapv.NumeroDireccionRequerido				= false;
			csaapv.IdContratoSAAPV = csaapv.parametrosEntrada.IdContratoSAAPV;
		}
		
		/*Este watch sirve para cuando necesiten llamar a un evento justo luego de que el DOM se haya cargado completamente. 
		 * Por lo que ese watch se ejecutará solo una vez*/
		$scope.$watch('$viewContentLoaded', function(){	    
			/*
			$('#TabEmpleador').trigger('click');
			document.getElementById('TabEmpleador').click();
			 */

			csaapv.limpiarCliente();
			csaapv.TipoTraspasoPostFiltro = csaapv.parametrosEntrada.tipoTraspaso === undefined ? 'ING' : csaapv.parametrosEntrada.tipoTraspaso;
			/*csaapv.tabs['PanelPortafolioContrato'].select = false;
			csaapv.tabs['PanelPortafolioContrato'].active = false;
			csaapv.tabs['PanelEmpleadoContrato'].select = true;
			csaapv.tabs['PanelEmpleadoContrato'].active = true;
			csaapv.domVentana.find('#'+csaapv.tabs['PanelEmpleadoContrato'].id).append($compile(csaapv.tabs['PanelEmpleadoContrato'].url)($scope));
						
			csaapv.tabs['PanelEmpleadoContrato'].select = true;
			csaapv.fnTabSelect('Empleado','PanelEmpleadoContrato') = true;*/
			
		});
		$scope.pestañaSeleccionada = '';
			$scope.seleccionarPestana = function(textoPestana) {
				$scope.pestañaSeleccionada = textoPestana;
				let elementId = $element[0].id;
				
				let hijoElement = document.getElementById(elementId);

			  	let elementoAncestroConClase = buscarAncestroConClase(hijoElement, "k-window");

				if (elementoAncestroConClase) {
					let idDelAncestro = elementoAncestroConClase.id;
					let winId = $("#"+idDelAncestro);
					if(textoPestana==='empleador'){
						winId.css('height', '785px');
					}else{
						winId.css('height', '700px');
					}
				  }
			  };
			  
			  function buscarAncestroConClase(elemento, claseBuscada) {
				while (elemento && !elemento.classList.contains(claseBuscada)) {
				  elemento = elemento.parentElement;
				}
				return elemento;
			  }
	/******************************************************/
	/*** FINAL											***/
	/******************************************************/
	});
}());