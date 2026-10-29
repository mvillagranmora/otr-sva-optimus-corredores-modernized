angular.module('ModuloSocioNegocio', ['ui.bootstrap','ui.utils','ui.router','ngAnimate']);

angular.module('ModuloSocioNegocio').factory('servicioSociodeNegocio',['$resource',function($resource) {
	var urlSaveSocioDeNegocio									= "/BSCL/Plataforma/GWJAVA/PLA/call?t=8";
	var urlModificarPersonaDatosGenerales						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=68";
	var urlCrearPersonaDocumentoIdentidad						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=72";
	var urlModificarPersonaDocumentoIdentidad					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=74";
	var urlCrearPersonaCuentaBancaria							= "/BSCL/Plataforma/GWJAVA/PLA/call?t=75";
	var urlModificarPersonaCuentaBancaria						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=76";
	var urlCrearPersonaTarjetaBancaria							= "/BSCL/Plataforma/GWJAVA/PLA/call?t=92";
	var urlModificarPersonaTarjetaBancaria						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=93";
	var urlCrearPersonaTelefono									= "/BSCL/Plataforma/GWJAVA/PLA/call?t=79";
	var urlModificarPersonaTelefono								= "/BSCL/Plataforma/GWJAVA/PLA/call?t=80";
	var urlCrearPersonaDireccionFisica							= "/BSCL/Plataforma/GWJAVA/PLA/call?t=81";
	var urlModificarPersonaDireccionFisica						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=82";
	var urlCrearPersonaDireccionElectronica						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=83";
	var urlModificarPersonaDireccionElectronica					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=84";
	var urlCrearPersonaRelacion									= "/BSCL/Plataforma/GWJAVA/PLA/call?t=152";
	var urlModificarPersonaRelacion								= "/BSCL/Plataforma/GWJAVA/PLA/call?t=153";
	var urlCrearPersonaNombreFantasia							= "/BSCL/Plataforma/GWJAVA/PLA/call?t=164";
	var urlModificarPersonaNombreFantasia						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=100020";
	var urlModificarClienteDatosGenerales 						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=95";
	var urlModificarEmpleadoDatosGenerales 						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=98";
	var urlModificarContraparteDatosGenerales 					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=110";
	var urlModificarProveedorDatosGenerales						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=118";
	var urlRecarterizacionClientesJava							= "/BSCL/Plataforma/GWJAVA/PLA/call?t=20016";
	var urlModificarConfiguracionBloqueoAutomaticoClientesJava 	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=20023";
	var urlGenerarReporteClienteFallecidos 						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=20043";
	var urlModificarRelacionLaboral 							= "/BSCL/Plataforma/GWJAVA/PLA/call?t=6003";
	var urlGenerarReporteSocioNegocio 							= "/BSCL/Plataforma/GWJAVA/PLA/call?t=20043";
	var urlConsultarConfiguracionSocioNegocio 					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=600004";
	var urlAnexoUrl                            			 		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=601122";
	var urlValidaCondicionesMinimasFirmaBlockChain 				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=601129";
	var urlValidaConsultarContratoBlockChain 					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=601130";
	var urlvalidaFirmarContratoBlockchain    					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=601133";
	var urlValidaEstadoDocumentosBlockChain   	 				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=601141";
	var urlConsultarNotificacionesCliente 						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=13043";
	var urlConsultaDocumentos 									= "/BSCL/Plataforma/GWJAVA/PLA/call?t=601185";
	var urlModificarGrupoClientes	 							= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700008";
	var urlConsultarGrupoClientes 								= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700003";
	var urlConsultarClientesGrupoCliente 						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700004";
	var urlConsultarEstadoClientesGrupoCliente 					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700009";
	var urlReportePatrimonio 									= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700023";
	var urlCrearEnrolamientoCuentaBancaria 						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700225";
	var urlCrearFatcaPaisResidencia								= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700231";
	var urlModificarFatcaPaisResidencia							= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700232";
	var urlEliminarFatcaPaisResidencia							= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700233";
	var urlCrearBorradorGrupoDireccion                          = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700226";
    var urlModificarBorradorGrupoDireccion                      = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700247";
	var urlEliminarBorradorGrupoDireccion                       = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700246";
	var urlCrearBorradorGrupoDireccionDetalle                   = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700234";
	var urlEliminarBorradorGrupoDireccionDetalle                = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700235";
	var urlCrearBorradorTarjetaBancaria                  		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700240";
	var urlModificarBorradorTarjetaBancaria                  	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700241";
	var urlEliminarBorradorTarjetaBancaria                  	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700242";
	var urlCrearBorradorRelacionPersona                  		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700237";
	var urlModificarBorradorRelacionPersona                  	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700238";
	var urlEliminarBorradorRelacionPersona                  	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700239";
	var urlModificarBorradorPersonaDatosGenerales               = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700248";
    var urlCrearBorradorNombreFantasia                          = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700721";
	var urlCrearBorradorCuentaBancaria 							= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700225";
	var urlModificarBorradorCuentaBancaria						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700230";
	var urlEliminarBorradorCuentaBancaria 						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700236"
	var urlCrearBorradorTelefono								= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700243";
	var urlModificarBorradorTelefono                  			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700244";
	var urlEliminarBorradorTelefono                  			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700245";
	var urlCrearBorradorDireccionElectronica					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700252";
	var urlModificarBorradorDireccionElectronica				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700253";
	var urlEliminarBorradorDireccionElectronica					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700254";
	var urlCrearBorradorDireccionFisica							= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700283";
	var urlModificarBorradorDireccionFisica						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700284";
	var urlEliminarBorradorDireccionFisica						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700286";
	var urlCrearBorradorDocumentoIdentidad                      = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700250";
	var urlModificarBorradorDocumentoIdentidad                  = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700251";
	var urlEliminarBorradorDocumentoIdentidad                   = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700249";
	var urlModificarBorradorClienteDatosGenerales               = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700257";
	var urlModificarBorradorFatca              					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700261";
	var urlCrearBorradorFatcaPaisResidencia         			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700263";
	var urlModificarBorradorFatcaPaisResidencia              	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700264";
	var urlEliminarBorradorFatcaPaisResidencia         			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700265";
	var urlCrearBorradorPreferenciaPagoPortafolio				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700268";
	var urlModificarBorradorPreferenciaPagoPortafolio			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700269";
	var urlEliminarBorradorPreferenciaPagoPortafolio			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700270";
	var urlCrearBorradorCuentaCustodioPortafolio				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700271";
	var urlModificarBorradorCuentaCustodioPortafolio			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700272";
	var urlEliminarBorradorCuentaCustodioPortafolio				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700273";
	var urlConsultarFormularioSocioNegocio                      = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700280";
	var urlCrearClienteFull                      				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700285";
	var urlValidarImpresionContrato								= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700319";
	var urlActualizarSupervisorAEjecutivo						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700399";
	var urlEliminarPersonaTelefonoMasivo						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700403";
	var urlEliminarPersonaTarjetaBancariaMasivo					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700404";
	var urlEliminarPersonaGrupoDireccionElectronicaMasivo		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700405";
	var urlEliminarPersonaDocumentoIdentidadMasivo				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700406";
	var urlEliminarPersonaDireccionFisicaMasivo					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700407";
	var urlEliminarPersonaDireccionElectronicaMasivo			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700408";
	var urlEliminarPersonaCuentaBancariaMasivo					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700409";
	var urlCrearCargaMasiva										= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700422";
	var urlEliminarCargaMasiva									= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700423";
	var urlProcesarCargaMasiva									= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700426";
	var urlRegenerarContratosClientes							= "/BSCL/Plataforma/GWJAVA/PLA/call?t=4300009";
	var urlBloqueoDesbloqueoMasivoClientesPortafolios			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700730";
	var urlCrearConfiguracionPerfilComercialAdicional			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700415";
	var urlModificarConfiguracionPerfilComercialAdicional		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700416";
	var urlEliminarConfiguracionPerfilComercialAdicional		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700417";
	var urlEliminarPortafolioCliente							= "/BSCL/Plataforma/GWJAVA/PLA/call?t=12661";
	var urlCrearModificarPaisResidenciaFiscal					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010130";
	var urlCrearExclusionLimpiezaLineaOperacional				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=702174";
	var urlEliminarExclusionLimpiezaLineaOperacional			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=702172";

	return $resource("", {},
			{
	    	  
			  'saveSocioDeNegocio'								:{method: "POST", url:urlSaveSocioDeNegocio},
	    	  'modificarPersonaDatosGenerales'					:{method: "POST", url:urlModificarPersonaDatosGenerales},
	    	  'crearPersonaDocumentoIdentidad'					:{method: "POST", url:urlCrearPersonaDocumentoIdentidad},
	    	  'modificarPersonaDocumentoIdentidad'				:{method: "POST", url:urlModificarPersonaDocumentoIdentidad},
	    	  'crearPersonaCuentaBancaria'						:{method: "POST", url:urlCrearPersonaCuentaBancaria},
	    	  'modificarPersonaCuentaBancaria'					:{method: "POST", url:urlModificarPersonaCuentaBancaria},
	    	  'crearPersonaTarjetaBancaria'						:{method: "POST", url:urlCrearPersonaTarjetaBancaria},
	    	  'modificarPersonaTarjetaBancaria'					:{method: "POST", url:urlModificarPersonaTarjetaBancaria},
	    	  'crearPersonaTelefono'							:{method: "POST", url:urlCrearPersonaTelefono},
	    	  'modificarPersonaTelefono'						:{method: "POST", url:urlModificarPersonaTelefono},
	    	  'crearPersonaDireccionFisica'						:{method: "POST", url:urlCrearPersonaDireccionFisica},
	    	  'modificarPersonaDireccionFisica'					:{method: "POST", url:urlModificarPersonaDireccionFisica},
	    	  'crearPersonaDireccionElectronica'				:{method: "POST", url:urlCrearPersonaDireccionElectronica},
	    	  'modificarPersonaDireccionElectronica'			:{method: "POST", url:urlModificarPersonaDireccionElectronica},
	    	  'crearPersonaRelacion'							:{method: "POST", url:urlCrearPersonaRelacion},
	    	  'modificarPersonaRelacion'						:{method: "POST", url:urlModificarPersonaRelacion},
	    	  'crearPersonaNombreFantasia'						:{method: "POST", url:urlCrearPersonaNombreFantasia},
	    	  'modificarPersonaNombreFantasia'					:{method: "POST", url:urlModificarPersonaNombreFantasia},
	    	  'modificarClienteDatosGenerales'					:{method: "POST", url:urlModificarClienteDatosGenerales},
	    	  'modificarEmpleadoDatosGenerales'					:{method: "POST", url:urlModificarEmpleadoDatosGenerales},
	    	  'modificarContraparteDatosGenerales'				:{method: "POST", url:urlModificarContraparteDatosGenerales},
	    	  'modificarProveedorDatosGenerales'				:{method: "POST", url:urlModificarProveedorDatosGenerales},
	    	  'recarterizacionClientes'							:{method: "POST", url:urlRecarterizacionClientesJava},
	    	  'modificarConfiguracionBloqueoAutomaticoClientes'	:{method: "POST", url:urlModificarConfiguracionBloqueoAutomaticoClientesJava},
	    	  'generarReporteClienteFallecidos'					:{method: "POST", url:urlGenerarReporteClienteFallecidos},
	    	  'modificarRelacionLaboral'						:{method: "POST", url:urlModificarRelacionLaboral},
	    	  'generarReporteSocioNegocio'						:{method: "POST", url:urlGenerarReporteSocioNegocio},
	    	  'consultarConfiguracionSocioNegocio'				:{method: "POST", url:urlConsultarConfiguracionSocioNegocio},
	    	  'FirmaAnexoUrl'	                    			:{method: "POST", url:urlAnexoUrl},
	    	  'validaCondicionesMinimasFirmaBlockhain'			:{method: "POST", url:urlValidaCondicionesMinimasFirmaBlockChain},
	    	  'validaConsultarContratoBlockChain'   			:{method: "POST", url:urlValidaConsultarContratoBlockChain},
	    	  'validaFirmarContratoBlockchain'      			:{method: "POST", url:urlvalidaFirmarContratoBlockchain},
	    	  'ValidaEstadoDocumentosBlockChain'    			:{method: "POST", url:urlValidaEstadoDocumentosBlockChain},
	    	  'ConsultarNotificacionesCliente'					:{method: "POST", url:urlConsultarNotificacionesCliente},
	    	  'ConsultaDocumentos'								:{method: "POST", url:urlConsultaDocumentos},
			  'modificarGrupoClientes' 							:{method: "POST", url:urlModificarGrupoClientes},
	    	  'ConsultarGrupoClientes'							:{method: "POST", url:urlConsultarGrupoClientes},
	    	  'ConsultarClientesGrupoCliente'					:{method: "POST", url:urlConsultarClientesGrupoCliente},
	    	  'ConsultarEstadoClientesGrupoCliente'				:{method: "POST", url:urlConsultarEstadoClientesGrupoCliente},
			  'ReportePatrimonio'								:{method: "POST", url:urlReportePatrimonio},
			  'crearEnrolamientoCuentaBancaria'					:{method: "POST", url:urlCrearEnrolamientoCuentaBancaria},
			  'CrearFatcaPaisResidencia'						:{method: "POST", url:urlCrearFatcaPaisResidencia},
			  'ModificarFatcaPaisResidencia'					:{method: "POST", url:urlModificarFatcaPaisResidencia},
			  'EliminarFatcaPaisResidencia'						:{method: "POST", url:urlEliminarFatcaPaisResidencia},
			  'crearBorradorGrupoDireccion'                     :{method: "POST", url:urlCrearBorradorGrupoDireccion},
			  'modificarBorradorGrupoDireccion'                 :{method: "POST", url:urlModificarBorradorGrupoDireccion},
		      'eliminarBorradorGrupoDireccion'                  :{method: "POST", url:urlEliminarBorradorGrupoDireccion},
		      'crearBorradorGrupoDireccionDetalle'              :{method: "POST", url:urlCrearBorradorGrupoDireccionDetalle},
			  'eliminarBorradorGrupoDireccionDetalle'           :{method: "POST", url:urlEliminarBorradorGrupoDireccionDetalle},
			  'crearBorradorTarjetaBancaria'					:{method: "POST", url:urlCrearBorradorTarjetaBancaria},
			  'modificarBorradorTarjetaBancaria'				:{method: "POST", url:urlModificarBorradorTarjetaBancaria},
			  'eliminarBorradorTarjetaBancaria'					:{method: "POST", url:urlEliminarBorradorTarjetaBancaria},
			  'crearBorradorRelacionPersona'					:{method: "POST", url:urlCrearBorradorRelacionPersona},
			  'modificarBorradorRelacionPersona'				:{method: "POST", url:urlModificarBorradorRelacionPersona},
			  'eliminarBorradorRelacionPersona'					:{method: "POST", url:urlEliminarBorradorRelacionPersona},
			  'modificarBorradorPersonaDatosGenerales'			:{method: "POST", url:urlModificarBorradorPersonaDatosGenerales},
	 		  'crearBorradorNombreFantasia'						:{method: "POST", url:urlCrearBorradorNombreFantasia},
			  'crearBorradorCuentaBancaria'						:{method: "POST", url:urlCrearBorradorCuentaBancaria},
			  'modificarBorradorCuentaBancaria'					:{method: "POST", url:urlModificarBorradorCuentaBancaria},
			  'eliminarBorradorCuentaBancaria'					:{method: "POST", url:urlEliminarBorradorCuentaBancaria},
			  'crearBorradorTelefono'							:{method: "POST", url:urlCrearBorradorTelefono},
			  'modificarBorradorTelefono'						:{method: "POST", url:urlModificarBorradorTelefono},
			  'eliminarBorradorTelefono'						:{method: "POST", url:urlEliminarBorradorTelefono},
			  'crearBorradorDireccionElectronica'				:{method: "POST", url:urlCrearBorradorDireccionElectronica},
			  'modificarBorradorDireccionElectronica'			:{method: "POST", url:urlModificarBorradorDireccionElectronica},
			  'eliminarBorradorDireccionElectronica'			:{method: "POST", url:urlEliminarBorradorDireccionElectronica},
			  'crearBorradorDireccionFisica'					:{method: "POST", url:urlCrearBorradorDireccionFisica},
			  'modificarBorradorDireccionFisica'				:{method: "POST", url:urlModificarBorradorDireccionFisica},
			  'eliminarBorradorDireccionFisica'					:{method: "POST", url:urlEliminarBorradorDireccionFisica},
			  'crearBorradorDocumentoIdentidad'                 :{method: "POST", url:urlCrearBorradorDocumentoIdentidad},
			  'modificarBorradorDocumentoIdentidad'             :{method: "POST", url:urlModificarBorradorDocumentoIdentidad},
			  'eliminarBorradorDocumentoIdentidad'              :{method: "POST", url:urlEliminarBorradorDocumentoIdentidad},
			  'modificarBorradorClienteDatosGenerales'          :{method: "POST", url:urlModificarBorradorClienteDatosGenerales},
 		      'modificarBorradorFatca'          				:{method: "POST", url:urlModificarBorradorFatca},
			  'crearBorradorFatcaPaisResidencia'          		:{method: "POST", url:urlCrearBorradorFatcaPaisResidencia},
			  'modificarBorradorFatcaPaisResidencia'        	:{method: "POST", url:urlModificarBorradorFatcaPaisResidencia},
			  'eliminarBorradorFatcaPaisResidencia'          	:{method: "POST", url:urlEliminarBorradorFatcaPaisResidencia},
			  'crearBorradorPreferenciaPagoPortafolio'          :{method: "POST", url:urlCrearBorradorPreferenciaPagoPortafolio},
			  'modificarBorradorPreferenciaPagoPortafolio'      :{method: "POST", url:urlModificarBorradorPreferenciaPagoPortafolio},
			  'eliminarBorradorPreferenciaPagoPortafolio'       :{method: "POST", url:urlEliminarBorradorPreferenciaPagoPortafolio},
			  'crearBorradorCuentaCustodioPortafolio'          	:{method: "POST", url:urlCrearBorradorCuentaCustodioPortafolio},
			  'modificarBorradorCuentaCustodioPortafolio'      	:{method: "POST", url:urlModificarBorradorCuentaCustodioPortafolio},
			  'eliminarBorradorCuentaCustodioPortafolio'       	:{method: "POST", url:urlEliminarBorradorCuentaCustodioPortafolio},
			  'consultarFormularioSocioNegocio'                 :{method: "POST", url:urlConsultarFormularioSocioNegocio},
			  'crearClienteFull'                 				:{method: "POST", url:urlCrearClienteFull},
			  'validarImpresionContrato'						:{method: "POST", url:urlValidarImpresionContrato},
			  'ActualizarSupervisorAEjecutivo'					:{method: "POST", url:urlActualizarSupervisorAEjecutivo},
			  'eliminarPersonaTelefonoMasivo'					:{method: "POST", url:urlEliminarPersonaTelefonoMasivo},
	    	  'eliminarPersonaTarjetaBancariaMasivo'   			:{method: "POST", url:urlEliminarPersonaTarjetaBancariaMasivo},
	    	  'eliminarPersonaGrupoDireccionElectronicaMasivo'	:{method: "POST", url:urlEliminarPersonaGrupoDireccionElectronicaMasivo},
	    	  'eliminarPersonaDocumentoIdentidadMasivo'			:{method: "POST", url:urlEliminarPersonaDocumentoIdentidadMasivo},
	    	  'eliminarPersonaDireccionFisicaMasivo'			:{method: "POST", url:urlEliminarPersonaDireccionFisicaMasivo},
	    	  'eliminarPersonaDireccionElectronicaMasivo'		:{method: "POST", url:urlEliminarPersonaDireccionElectronicaMasivo},
	    	  'eliminarPersonaCuentaBancariaMasivo'				:{method: "POST", url:urlEliminarPersonaCuentaBancariaMasivo},
			  'regenerarContratosClientes'						:{method: "POST", url:urlRegenerarContratosClientes},
			  'crearConfiguracionPerfilComercialAdicional'		:{method: "POST", url:urlCrearConfiguracionPerfilComercialAdicional},
			  'modificarConfiguracionPerfilComercialAdicional'	:{method: "POST", url:urlModificarConfiguracionPerfilComercialAdicional},
			  'eliminarConfiguracionPerfilComercialAdicional'	:{method: "POST", url:urlEliminarConfiguracionPerfilComercialAdicional},

			  'CrearCargaMasiva'						    	:{method: "POST", url:urlCrearCargaMasiva},
			  'EliminarCargaMasiva'						    	:{method: "POST", url:urlEliminarCargaMasiva},
			  'ProcesarCargaMasiva'								:{method: "POST", url:urlProcesarCargaMasiva},
			  'BloqueoDesbloqueoMasivoClientesPortafolios'		:{method: "POST", url:urlBloqueoDesbloqueoMasivoClientesPortafolios},
			  
			  'eliminarPortafolioCliente'            		    :{method: "POST", url:urlEliminarPortafolioCliente},
			  'CrearModificarPaisResidenciaFiscal'				:{method: "POST", url:urlCrearModificarPaisResidenciaFiscal},
			  'CrearExclusionLimpiezaLineaOperacional'			:{method: "POST", url:urlCrearExclusionLimpiezaLineaOperacional},
			  'EliminarExclusionLimpiezaLineaOperacional'		:{method: "POST", url:urlEliminarExclusionLimpiezaLineaOperacional},
		});
}]);

angular.module('ModuloSocioNegocio').factory('bcsConfiguracionSociodeNegocio',function($translate, $rootScope, $q, bcsConsultasSocioNegocio) {

	var obj = {};
	
	var CONFIGURACION = {};
	CONFIGURACION.defer = $q.defer();
	CONFIGURACION.promise = CONFIGURACION.defer.promise;
	
	var cargarConfiguracion = function() {
		var ds = new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering : true,
				filter : { field : "CodigoParametro", operator : "eq", value : "CLI.BloqueoDocIdentidad"},
				transport: {
					read: {
						url: bcsConsultasSocioNegocio("ConsultarConfiguracionSocioNegocio"),
						dataType: "json"
					}
				},
				schema : {
					model : {
						fields : {
							IdEmpresa 		: { type : "number"},
							CodigoParametro : { type : "string"},
							Descripcion 	: { type : "string"},
							Valor 			: { type : "string"},
						}
					}
				}
			}
		});
		
		ds.fetch().then(function(){
			var data = ds.data();
			$.each(data,function(key,value){
				CONFIGURACION[value.CodigoParametro] = value.Valor;
			})
			CONFIGURACION.defer.resolve();
		},function() {
			console.error("ERROR AL CARGAR CONFIGURACION SOCIO NEGOCIO");
		})
		
	};
	cargarConfiguracion();

	obj.getConfiguracion = function(elem)
	{
		var configPromise = {};

				
		if (elem === "grid-relacionados")
		{
			configPromise = obj.getConfigGridMantenedorRelacionados();
		}
		
		if (elem === "grid-relaciones-corredora")
		{
			configPromise = obj.getConfigGridRelacionCorredor();
		}

		return configPromise;
	}
	
	


	obj.getSchemaSocioNegocio = function() {
		return {
			model: {
				fields : {
					
					IdSocioDeNegocio : { type: "number"},
					IdEmpresa : { type: "number"},
					IdPersona : { type: "number"},
					IdEmpleado : { type: "number"},
					IdCliente : { type: "number"},
					IdProveedor : { type: "number"},
					IdOperador : { type: "number"},
					IdContraparte : { type: "number"},
					FechaIngreso : { type: "date"},
					FechaCreacion : { type: "date"},
					FechaModificacion : { type: "date"},
					IdTipoDocumentoIdentidadDefecto : { type: "string"},
					NombreTipoDocumentoIdentidadDefecto : { type: "string"},
					NombreAbrTipoDocumentoIdentidadDefecto : { type: "string"},
					DocumentoIdentidadDefecto : { type: "string"},
					CodigoTipoPersona : { type: "string"},
					TipoPersona : { type: "string"},
					NombreCompleto : { type: "string"},
					RazonSocial : { type: "string"},
					Alias : { type: "string"},
					Nombres : { type: "string"},
					ApellidoPaterno : { type: "string"},
					ApellidoMaterno : { type: "string"},
					FechaNacimiento : { type: "date"},
					IdPaisResidencia : { type: "number"},
					CodigoPaisResidencia : { type: "string"},
					NombrePaisResidencia : { type: "string"},
					EsCliente : { type: "string"},
					EsEmpleado : { type: "string"},
					EsOperador : { type: "string"},
					EsProveedor : { type: "string"},
				}
			}
		};
	 };
	 
	 obj.getSchemaPersona = function() {
		return {
			model: {
				fields : {
					
					IdPersona  : { type: "number"},
					IdEstado : { type: "string"},
					IdDocumentoIdentidad : { type: "number"},
					Tipo : { type: "string"},
					Alias : { type: "string"},
					Nombres : { type: "string"},
					ApellidoPaterno : { type: "string"},
					ApellidoMaterno : { type: "string"},
					RazonSocial : { type: "string"},
					NombreCompleto : { type: "string"},
					IdGiroPrincipal : { type: "number"},
					IdPaisNacimiento : { type: "number"},
					IdPaisResidencia : { type: "number"},
					FechaNacimiento : { type: "date"},
					FechaDefuncion : { type: "date"},
					Sexo : { type: "string"},
					EsExtranjero : { type: "string"},
					EstadoCivil : { type: "string"},
					RegimenConyugal : { type: "string"},
					Cargo : { type: "string"},
					Empleador : { type: "string"},
					IdDireccionFisicaPrincipal : { type: "number"},
					IdDireccionElectronicaPrincipal: { type: "number"},
					IdTelefonoPrincipal: { type: "number"},
					IdProfesion: { type: "number"},
					FechaInicioActividad: { type: "date"},
					FechaIngreso: { type: "date"},
					FechaCreacion: { type: "date"},
					FechaModificacion: { type: "date"},
					EsEnrolado: { type: "string"},
					IdCarga: { type: "number"},
					DocumentoIdentidad: { type: "string"},
					TipoDocumentoIdentidad : { type: "string"},
					PersonaBusqueda: { type: "string"},
					IdPersonaConyuge : { type: "number"},
					IdPersonaEmpleador : { type: "number"},
					IdPersonaRepresentante : { type: "number"},
					IdGradoInstruccion : { type: "number"},//
					AgnosServicio : { type: "number"},
					IngresoPromedioMensual : { type: "number"},
					IdGiroCIIU : { type: "number"},
					IdPaisResidenciaSecundaria : { type: "number"},
					IdPaisNacionalidadSecundaria : { type: "number"},
					IdRangoPatrimonio : { type: "number"},
					NombreTipoBloqueo: { type: "string"},
					IdTipoBloqueo: { type: "string"},
					FechaBloqueo : { type: "date"}
				}
			}
		};
	 };

	 obj.getSchemaEnrolamiento = function() {
			return {
				model: {
					fields : {
						
						IdEnrolamiento : { type: "number"}
	        			,FechaIngreso : { type: "date"}
	        			,FechaModificacion : { type: "date"}
	        			,IdTipoDocumentoIdentidadDefecto : { type: "string"}
	        			,NombreTipoDocumentoIdentidadDefecto : { type: "string"}
						,NombreAbrTipoDocumentoIdentidadDefecto : { type: "string"}
	        			,NombreCompleto : { type: "string"}
	        			,DocumentoIdentidadDefecto : { type: "string"}
	        			,TipoPersona : { type: "string"}
	        			,RazonSocial : { type: "string"}
	        			,Nombres : { type: "string"}
	        			,ApellidoPaterno : { type: "string"}
	        			,ApellidoMaterno : { type: "string"}
	        			,Alias : { type: "string"}
	        			,FechaNacimiento : { type: "date"}
	        			,IdPaisResidencia : { type: "string"}
	        			,CodigoPaisResidencia : { type: "string"}
	        			,NombrePaisResidencia : { type: "string"}
						/*
						IdEnrolamiento  : { type: "number"},
						IdEstado : { type: "string"},
						IdDocumentoIdentidad : { type: "number"},
						Tipo : { type: "string"},
						Alias : { type: "string"},
						Nombres : { type: "string"},
						ApellidoPaterno : { type: "string"},
						ApellidoMaterno : { type: "string"},
						RazonSocial : { type: "string"},
						NombreCompleto : { type: "string"},
						IdGiroPrincipal : { type: "number"},
						IdPaisNacimiento : { type: "number"},
						IdPaisResidencia : { type: "number"},
						FechaNacimiento : { type: "date"},
						Sexo : { type: "string"},
						EstadoCivil : { type: "string"},
						RegimenConyugal : { type: "string"},
						Cargo : { type: "string"},
						Empleador : { type: "string"},
						IdTelefonoPrincipal: { type: "number"},
						IdProfesion: { type: "number"},
						FechaInicioActividad: { type: "date"},
						FechaIngreso: { type: "date"},
						FechaCreacion: { type: "date"},
						FechaModificacion: { type: "date"},
						EsEnrolado: { type: "string"},
						IdCarga: { type: "number"},
						DocumentoIdentidad: { type: "string"},
						TipoDocumentoIdentidad : { type: "string"},
						PersonaBusqueda: { type: "string"},
						IdPersonaConyuge : { type: "number"},
						IdPersonaEmpleador : { type: "number"},
						IdPersonaRepresentante : { type: "number"},
						IdGradoInstruccion : { type: "number"},//
						AgnosServicio : { type: "number"},
						IngresoPromedioMensual : { type: "number"},
						IdGiroCIIU : { type: "number"},
						IdPaisResidenciaSecundaria : { type: "number"},
						IdPaisNacionalidadSecundaria : { type: "number"},
						IdRangoPatrimonio : { type: "number"},*/
					}
				}
			};
		 };
	 
	 obj.getSchemaCliente = function() {
		return {
			model: {
				fields : {
					IdCliente  : { type: "number"},
					IdEstado : { type: "string"},
					IdSegmentoCliente: { type: "number"},
					IdCategoriaInversionista: { type: "string"},
					IdTipoBloqueo: { type: "string"},
					FechaBloqueo: { type: "date"},
					ComentarioBloqueo: { type: "string"},
					CodigoEjecutivoExterno: { type: "string"},
					IdTipoCliente: { type: "string"},
					IdClaseCliente: { type: "string"}
				}
			}
		};
	 };

	 obj.getSchemaBorradorCliente = function getSchemaBorradorCliente() {
	 	return {
	 		model: {
	 			fields: {
					CLI_IdTipoCliente: { type: "string" },
					CLI_IdCategoriaInversionista: { type: "string" },
					CLI_IdSegmentoCliente: { type: "number" },
					CLI_IdEmpleadoEjecutivo: { type: "number" },
					CLI_CodigoEjecutivoExterno: { type: "string" },
					CLI_IdSucursalPreferencia: { type: "number" },
					CLI_EsClienteFinal: { type: "string" }
				}
			}
		}
	 }
	 	 
	 obj.getSchemaEmpleado = function() {
		return {
			model: {
				fields : {
					
					IdEmpleado  : { type: "number"},
					IdEstado : { type: "string"}
					/*
					IdSegmentoCliente: { type: "number"},
					IdCategoriaInversionista: { type: "string"},
					IdTipoBloqueo: { type: "string"},
					FechaBloqueo: { type: "date"},
					ComentarioBloqueo: { type: "string"}
					*/
			
				}
			}
		};
	 };

	 obj.getSchemaProveedor = function() {
		return {
			model: {
				fields : {
					
					IdProveedor  : { type: "number"},
					IdEstado : { type: "string"}
				}
			}
		};
	 };
	 	 		 
	 
	obj.getSchemaMantenedorRelacionados = function() {
		return {
			model: {
				fields : {
					 IdRelacionPersona						: { type: "number"}
					 ,IdTipoRelacion						: { type: "string"}
					 ,DocumentoIdentidad					: { type: "string"}
					 ,NombreTipoRelacion					: { type: "string"}
					 ,IdPersonaOrigen						: { type: "number"}
					 ,RazonSocialOrigen						: { type: "string"}
					 ,IdPersonaDestino						: { type: "number"}
				 	 ,RazonSocialDestino					: { type: "string"}
				 	 ,DocumentoIdentidadDestino				: { type: "string"}
					 ,IdEstado								: { type: "string"}
					 ,FechaIngresoRegistro					: { type: "date"}
					 ,FechaInicioRelacion					: { type: "date"}
					 ,FechaFinRelacion						: { type: "date"}
					 ,IdRelacionPersonaOrigen				: { type: "number"}
					 ,TipoRegistro							: { type: "string"}
					
				}
			}
		};
	 };
	 	  
     obj.getConfigGridMantenedorRelacionados = function()
	 {
		var translate_array = ['UI_TIPO_RELACION','UI_NUMERO_DOCUMENTO','UI_RAZON_SOCIAL','UI_FECHA_INICIO','UI_FECHA_TERMINO',
		                       ];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				buscar : function(filtros) {
					var filters = new Array();
					
					    if (filtros.IdPersonaOrigen)
							filters.push({ field: "IdPersonaOrigen", operator: "eq", value: filtros.IdPersonaOrigen });
					    
						if (filtros.IdEstado)
							filters.push({ field: "IdEstado", operator: "eq", value: filtros.IdEstado });
						
						// if (filtros.IdPersonaDestino)
						//	filters.push({ field: "IdPersonaDestino", operator: "eq", value: filtros.IdPersonaDestino });
				
						config.options.dataSource.filter(filters);
				},
				options : {
 		 
					columns :
					[ 
				 	 {field: "NombreTipoRelacion",title: translations.UI_TIPO_RELACION,width: "70px",attributes:{style:"text-align:left;white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}}
				 	 ,{field: "DocumentoIdentidadDestino",title: translations.UI_NUMERO_DOCUMENTO,width: "40px", attributes:{style:"text-align:left;white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}}
					 ,{field: "RazonSocialDestino",title: translations.UI_RAZON_SOCIAL,width: "100px", attributes:{style:"text-align:left;white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}}
 					 ,{field: "FechaInicioRelacion",title: translations.UI_FECHA_INICIO,width: "40px", attributes:{style:"text-align:center;white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"},  template : " <label> #:  util.formatoDate(FechaInicioRelacion)#</label>" }
					 ,{field: "FechaFinRelacion",title: translations.UI_FECHA_TERMINO,width: "40px", attributes:{style:"text-align:center;white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"},  template : " <label> #:  util.formatoDate(FechaFinRelacion)#</label>" }
					
				 	],
				sortable: false,
				groupable:false,
				filterable:false,
	            reorderable: true,
				pageable: true,
				allowCopy: true,
				editable:false,
				selectable: "single, row",
				reorderable: true,
				resizable: true,
				save: function(e) {
					console.log("grabe");
				},
	            autoBind: false,
	            scrollable: true,
	            //height:200,
			 	dataSource : new bcs.DataSource( {
					type: "odata",
					definition : {
						serverFiltering: false,
						serverPaging: true,
						pageSize:200,
						serverSorting: true,
						sort: { field: "NombreTipoRelacion", dir: "desc" },
						transport: {
							read: {
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1293", //ConsultarRelacion
								dataType: "json"
							}
						},
						schema: obj.getSchemaMantenedorRelacionados(),
					}
				 }),
			 	}
			};
			return config;
		});
		return configPromise;
	};
		
	obj.getSchemaMantenedorRelacionadosCorredor = function() {
		return {
			model: {
				fields : {
					 IdRelacionPersona						: { type: "number"}
					 ,IdTipoRelacion						: { type: "string"}
					 ,DocumentoIdentidad					: { type: "string"}
					 ,NombreTipoRelacion					: { type: "string"}
					 ,IdPersonaOrigen						: { type: "number"}
					 ,RazonSocialOrigen						: { type: "string"}
					 ,IdPersonaDestino						: { type: "number"}
				 	 ,RazonSocialDestino					: { type: "string"}
				 	 ,DocumentoIdentidadDestino				: { type: "string"}
					 ,IdEstado								: { type: "string"}
					 ,FechaIngresoRegistro					: { type: "date"}
					 ,FechaInicioRelacion					: { type: "date"}
					 ,FechaFinRelacion						: { type: "date"}
					 ,IdRelacionPersonaOrigen				: { type: "number"}
					 ,TipoRegistro							: { type: "string"}
					
				}
			}
		};
	 };
	 obj.getConfigGridRelacionCorredor = function()
	 {
		var translate_array = ['UI_RELACION_CORREDORA','UI_FECHA_INICIO','UI_FECHA_TERMINO',
		                       ];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				buscar : function(filtros) {
					var filters = new Array();
					
					    if (filtros.IdPersonaOrigen)
							filters.push({ field: "IdPersonaOrigen", operator: "eq", value: filtros.IdPersonaOrigen });
					    
						if (filtros.IdEstado)
							filters.push({ field: "IdEstado", operator: "eq", value: filtros.IdEstado });
						
					    if (filtros.IdPersonaDestino)
							filters.push({ field: "IdPersonaDestino", operator: "eq", value: filtros.IdPersonaDestino });
				
						config.options.dataSource.filter(filters);
				},
				options : {
 		 
					columns :
					[ 
				 	 {field: "NombreTipoRelacion",title: translations.UI_RELACION_CORREDORA,width: "70px",attributes:{style:"text-align:left;white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}}
				 	 ,{field: "FechaInicioRelacion",title: translations.UI_FECHA_INICIO,width: "40px", attributes:{style:"text-align:center;white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"},  template : " <label> #:  util.formatoDate(FechaInicioRelacion)#</label>" }
					 ,{field: "FechaFinRelacion",title: translations.UI_FECHA_TERMINO,width: "40px", attributes:{style:"text-align:center;white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"},  template : " <label> #:   util.formatoDate(FechaFinRelacion)#</label>" }
					
				 	],
				sortable: false,
				groupable:false,
				filterable:false,
	            reorderable: true,
				pageable: true,
				allowCopy: true,
				editable:false,
				selectable: "single, row",
				reorderable: true,
				resizable: true,
				save: function(e) {
					console.log("grabe");
				},
	            autoBind: false,
	            scrollable: true,
	            //height:200,
			 	dataSource : new bcs.DataSource( {
					type: "odata",
					definition : {
						serverFiltering: false,
						serverPaging: true,
						pageSize:200,
						serverSorting: true,
						sort: { field: "NombreTipoRelacion", dir: "desc" },
						transport: {
							read: {
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1293", //ConsultarRelacion
								dataType: "json"
							}
						},
						schema: obj.getSchemaMantenedorRelacionadosCorredor(),
					}
				 }),
			 	}
			};
			return config;
		});
		return configPromise;
	};
	
	
	obj.getSchemaSocioDeNegocioRelacionado = function() {
		return {
			model: {
				fields : {
					IdSocioDeNegocio: { type: "number"},
					IdPersona		: { type: "number"},
					IdEmpresa		: { type: "number"},
					IdCliente		: { type: "number"},
					IdEmpleado		: { type: "number"},
					IdOperador		: { type: "number"},
					IdProveedor		: { type: "number"},
					IdContraparte	: { type: "number"},
					Borrado			: { type: "number"}
				}
			}
		};
	 };
		
	obj.getSchemaSolicitudDeCambio = function() {
		return {
			model: {
				fields : {
					IdSolicitudCambio		: { type: "number"},
					IdSecuenciaCambio		: { type: "number"},
					IdTipoEntidad			: { type: "string"},
					IdEntidadOrigen			: { type: "string"},
					CampoModificado			: { type: "string"},
					TipoDato				: { type: "string"},
					ValorAntiguo			: { type: "string"},
					ValorNuevo				: { type: "string"},
					FechaSolicitud			: { type: "string"},
					FechaRevision			: { type: "string"},
					IdEstadoAutorizacion	: { type: "string"}
				}
			}
		};
	 };
	 
	 obj.getSchemaAutorizacionEvento = function() {
		 return {
			 model: {
				 fields : {
					 IdEvento				: {type: "number"},
					 IdTipoEvento			: {type: "sting"},
					 IdCliente				: {type: "number"},
					 IdEstado				: {type: "sting"}
				 }
			 }
		 }
	 }
	 
	 obj.getSchemaSocioDeNegocioEjecutivo = function() {
		return {
			model: {
				fields : {
					IdSocioDeNegocio: { type: "number"},
					IdPersona		: { type: "number"},
					IdEmpresa		: { type: "number"},
					IdCliente		: { type: "number"},
					IdEmpleado		: { type: "number"},
					IdOperador		: { type: "number"},
					IdProveedor		: { type: "number"},
					IdContraparte	: { type: "number"},
					Borrado			: { type: "number"}
				}
			}
		};
	 };
	 
	 
	
	 obj.getDatasourceSocioNegocio = function(filter){
		 return new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering:true,
				transport: { read: {
						url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1051",
						dataType: "json"}},
				filter:filter,
				schema: obj.getSchemaSocioNegocio()
			}
		});
	 };
	 
	 obj.CONFIGURACION = CONFIGURACION;
	 
	 return obj;
});

angular.module('ModuloSocioNegocio').factory('bcsConsultasSocioNegocio',function() {
	var url_base = "/BSCL/Plataforma/GWJAVA/PLA/call?c=1&t=";
		var consultas = {
				ConsultarConfiguracionSocioNegocio : 600004,
				
		}
		return function(nombreConsulta) {
			return "/BSCL/Plataforma/GWJAVA/PLA/call?t=" + consultas[nombreConsulta];
		};
});
