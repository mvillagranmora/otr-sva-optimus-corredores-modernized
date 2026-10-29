
const BCS_VENTANA = { };
//---------------------------------------------
//SOCIO DE NEGOCIO
//---------------------------------------------

BCS_VENTANA.MODULO_MERCADO = {
    TITLE  : "UI_MERCADO",
    IMAGE  : "sapUiIcon opportunity",
    ENABLE : true,
    WIDTH  : "0px",
    HEIGHT : "0px",
    VIEW   : '',
    PARAMS : {},
    DEPENDENCIES : [],

};

BCS_VENTANA.ConsultarCertificadoPrestamo = {
	    TITLE : "UI_CERTIFICADOS_PRESTAMOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "120px",
	    VIEW   : '../../Custodia/UI/CertificadoPrestamo/ConsultarCertificadoPrestamo.html', 
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Custodia/UI/CertificadoPrestamo/ConsultarCertificadoPrestamo.js'] }],

};

BCS_VENTANA.ConsultarRendicion = {
	    TITLE : "UI_ARCHIVO_RENDICION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1200px",
	    HEIGHT : "700px",
	    VIEW   : '../../Tesoreria/UI/ConsultarRendicion/ConsultarRendicion.html', 
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Tesoreria/UI/ConsultarRendicion/ConsultarRendicion.js'] }],

};

BCS_VENTANA.ConsultarPreciosInstrumentos = {
	    TITLE : "UI_PRECIOS_DE_INSTRUMENTOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1200px",
	    HEIGHT : "620px",
	    VIEW   : 'INS/ConsultarPreciosInstrumentos/ConsultarPreciosInstrumentos.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['INS/ConsultarPreciosInstrumentos/ConsultarPreciosInstrumentos.js'] }],

};



BCS_VENTANA.ConsultarTransaccionAnuladasModificadas = {
	    TITLE : "UI_CONSULTA_TRANSACCIONES_ANULADAS_MODIFICADAS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
	    HEIGHT : "560px",
	    VIEW   : '../../Orden/UI/ConsultarTransacciones/ConsultarTransaccionesAnuladasModificadas.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Orden/UI/ConsultarTransacciones/ConsultarTransaccionesAnuladasModificadas.js'
	    													,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'
	    													,'../../Orden/UI/ModuloOrdenesDirectivas.js'] } ],
};


BCS_VENTANA.CrearModificarEmisor = {
	    TITLE : "UI_EMISOR",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "370px",
	    HEIGHT : "auto",
	    VIEW   : 'MER/CrearModificarEmisor/CrearModificarEmisor.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['MER/CrearModificarEmisor/CrearModificarEmisor.js'] }],

};



BCS_VENTANA.CargaMasivaOperacionesEnRueda = {
	    TITLE  : "UI_CARGA_MASIVA_OPERACIONES_EN_RUEDA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "506px",    
	    VIEW   : '../../Orden/UI/BlotterCargaMasiva/BlotterCargaMasiva.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, 
	    	files: [ '../../Orden/UI/BlotterCargaMasiva/BlotterCargaMasiva.js',
	    	         '../../Orden/UI/BlotterCargaMasiva/BlotterCargaMasivaGrid.js'
	    	         ] 
		}],

};



BCS_VENTANA.DetalleCargaMasivaOperacionesEnRueda = {
	    TITLE  : "UI_DETALLE_CARGA_MASIVA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "500px",    
	    VIEW   : '../../Orden/UI/BlotterCargaMasiva/DetalleCargaMasiva/DetalleCargaMasiva.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, 
	    	files: [ '../../Orden/UI/BlotterCargaMasiva/DetalleCargaMasiva/DetalleCargaMasiva.js',
	    	         '../../Orden/UI/BlotterCargaMasiva/BlotterCargaMasivaGrid.js'
	    	         ] 
		}],
};


BCS_VENTANA.ComplementoCargaMasivaOperacionesEnRueda = {
	    TITLE  : "UI_COMPLEMENTO_CARGA_MASIVA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "300px",    
	    VIEW   : '../../Orden/UI/BlotterCargaMasiva/ComplementoCargaMasiva.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, 
	    	files: [ '../../Orden/UI/BlotterCargaMasiva/ComplementoCargaMasiva.js'] 
		}],
};

BCS_VENTANA.MODULO_SOCIO_NEGOCIO = {
    TITLE  : "UI_SOCIO_NEGOCIO",
    IMAGE  : "sapUiIcon person-placeholder",
    ENABLE : true,
    WIDTH  : "0px",
    HEIGHT : "0px",
    VIEW   : '',
    PARAMS : {},
    DEPENDENCIES : [],

};

//CELZORRO-461
BCS_VENTANA.ConsultarGruposClientes = {
	    TITLE : "UI_GRUPOS_DE_CLIENTES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "790px",
	    HEIGHT : "520px",
	    VIEW   : '../../SocioNegocio/UI/CLI/ConsultarGruposClientes.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../SocioNegocio/UI/CLI/ConsultarGruposClientes.js'
	    														] } ],
};

//CELZORRO-461
BCS_VENTANA.ModificarGrupoClientes = {
	    TITLE : "UI_EDITAR_GRUPO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "700px",
	    HEIGHT : "500px",
	    VIEW   : '../../SocioNegocio/UI/CLI/ModificarGrupoClientes.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../SocioNegocio/UI/CLI/ModificarGrupoClientes.js'
	                                                          //,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'
	                                                          ] } ],
};
BCS_VENTANA.ConsultarSocioNegocio = {
    TITLE  : "UI_GESTION_SOCIO_NEGOCIO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1100px",
    HEIGHT : "700px",
    VIEW   : '../../SocioNegocio/UI/ConsultarSocioNegocio.html',
    PARAMS : { p1: 0, p2: 0},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/ConsultarSocioNegocio.js'] }],

};

BCS_VENTANA.eliminarPortafolioSocioNegocio = {
	TITLE : "UI_ELIMINAR_PORTAFOLIO",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "400px",
	HEIGHT : "200px",
	VIEW   : '../../SocioNegocio/UI/CLI/ModalEliminarPortafolio.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/CLI/ModalEliminarPortafolio.js']}],

};


BCS_VENTANA.ImprimirArchivosContratos = {
	    TITLE : "UI_IMPRIMIR_ARCHIVOS_CONTRATOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "915px",
	    HEIGHT : "485px",
	    VIEW   : '../../SocioNegocio/UI/CTT/ConsultarArchivosContratos.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/CTT/ConsultarArchivosContratos.js'] } ],

};




BCS_VENTANA.ModalArchivosContratos = {
	    TITLE : "UI_PARAMETROS_CONTRATOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "500px",
	    VIEW   : '../../SocioNegocio/UI/CTT/ModalParametrosContratos.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../SocioNegocio/UI/CTT/ModalParametrosContratos.js' ] } ],

};


BCS_VENTANA.administrarSocioNegocio = {
    TITLE : "UI_SOCIO_DE_NEGOCIO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1170px",
    HEIGHT : "500px",
    VIEW   : '../../SocioNegocio/UI/administrarSocioNegocio.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../SocioNegocio/UI/administrarSocioNegocio.js' 
			           									, '../../SocioNegocio/UI/PER/PanelPersonaDatosGenerales.js'
			           									, '../../SocioNegocio/UI/PER/PanelPersonaDocumentosIdentidad.js'
			           									, '../../SocioNegocio/UI/PER/PanelPersonaCuentasBancarias.js'
			           									, '../../SocioNegocio/UI/PER/PanelPersonaTarjetasBancarias.js'
			           									, '../../SocioNegocio/UI/PER/PanelPersonaTelefonos.js'
			           									, '../../SocioNegocio/UI/PER/PanelPersonaDireccionesElectronicas.js'
			           									, '../../SocioNegocio/UI/PER/PanelPersonaGrupoDireccionesElectronicas.js'
			           									, '../../SocioNegocio/UI/PER/PanelPersonaDireccionesFisicas.js'
			           									, '../../SocioNegocio/UI/PER/PanelPersonaAtributos.js'
			           									, '../../SocioNegocio/UI/PER/PanelPersonaRelaciones.js'
			           									, '../../SocioNegocio/UI/PER/PanelPersonaNombresFantasia.js'
			           									, '../../SocioNegocio/UI/PER/PanelPersonaFatca.js'
														, '../../SocioNegocio/UI/PER/PanelPersonaCrs.js'
			           									, '../../SocioNegocio/UI/CLI/PanelClienteDatosGenerales.js'			           									
			           									, '../../SocioNegocio/UI/CLI/PanelClientePortafolios.js'
			           									, '../../SocioNegocio/UI/CLI/PanelClientePortafoliosCotitulares.js'
			           									, '../../SocioNegocio/UI/CLI/PanelClientePortafoliosAPV.js'
			           									, '../../SocioNegocio/UI/CLI/PanelClienteAtributos.js'
			           									, '../../SocioNegocio/UI/CLI/PanelClienteRequisitos.js'
			           									, '../../SocioNegocio/UI/CLI/PanelClienteDocumentos.js'
														, '../../SocioNegocio/UI/CLI/PanelClienteEncuesta.js'  
			           									, '../../SocioNegocio/UI/CLI/PanelClienteDOF.js'
			           									, '../../SocioNegocio/UI/CLI/PanelClienteAnotaciones.js'
			           									, '../../SocioNegocio/UI/CLI/PanelClienteNotificaciones.js'
			           									, '../../SocioNegocio/UI/CLI/PanelClientePreferenciasNegocios.js'
			           									, '../../SocioNegocio/UI/CLI/PanelClienteHistorialCambios.js'	
			           									, '../../SocioNegocio/UI/EMP/PanelEmpleadoDatosGenerales.js'			           									
			           									, '../../SocioNegocio/UI/EMP/PanelEmpleadoAtributos.js'	
			           									, '../../SocioNegocio/UI/CNT/PanelContraparteDatosGenerales.js'
			           									, '../../SocioNegocio/UI/PRO/PanelProveedorDatosGenerales.js'			           									
			           									, '../../SocioNegocio/UI/PRO/PanelProveedorAtributos.js'
				           								, '../../SocioNegocio/UI/ventanaCambios.js'] } ],

};

BCS_VENTANA.EnrolamientoSocioNegocio = {
	    TITLE : "UI_SOCIO_DE_NEGOCIO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1170px",
	    HEIGHT : "540px",
	    VIEW   : '../../SocioNegocio/UI/EnrolamientoSocioNegocio.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../SocioNegocio/UI/EnrolamientoSocioNegocio.js' 
															   , '../../SocioNegocio/UI/ENR/PER/PanelPersonaDatosGenerales.js'
															   , '../../SocioNegocio/UI/ENR/PER/PanelPersonaDocumentosIdentidad.js'
															   , '../../SocioNegocio/UI/ENR/PER/PanelPersonaCuentasBancarias.js'
															   , '../../SocioNegocio/UI/ENR/PER/PanelPersonaTarjetasBancarias.js'
															   , '../../SocioNegocio/UI/ENR/PER/PanelPersonaDireccionesFisicas.js'
															   , '../../SocioNegocio/UI/ENR/PER/PanelPersonaTelefonos.js'
															   , '../../SocioNegocio/UI/ENR/PER/PanelPersonaDireccionesElectronicas.js'
															   , '../../SocioNegocio/UI/ENR/PER/PanelPersonaGrupoDireccionesElectronicas.js'
															   , '../../SocioNegocio/UI/ENR/PER/PanelPersonaAtributos.js'
															   , '../../SocioNegocio/UI/ENR/PER/PanelPersonaRelaciones.js'
												 			   , '../../SocioNegocio/UI/ENR/PER/PanelPersonaFatca.js'
															   , '../../SocioNegocio/UI/ENR/CLI/PanelClienteDatosGenerales.js'	
															   , '../../SocioNegocio/UI/ENR/CLI/PanelClienteAtributos.js'
					           								   , '../../SocioNegocio/UI/ENR/CLI/PanelClienteRequisitos.js'
															   , '../../SocioNegocio/UI/ENR/POR/PanelPortafolioCuentaCustodio.js'
															   , '../../SocioNegocio/UI/ENR/POR/PanelPortafolioPreferenciasPago.js'
															   , '../../SocioNegocio/UI/ENR/POR/PanelPortafolioPreferenciasNegocio.js'
															   , '../../SocioNegocio/UI/ENR/POR/PanelPortafolioPreferenciasPagoClienteRecibe.js'
															   , '../../SocioNegocio/UI/ENR/POR/PanelPortafolioPreferenciasPagoClientePaga.js'
															   , '../../SocioNegocio/UI/ENR/POR/GrillaPortafolioPreferenciasPago.js'
					           								] } ],

};

BCS_VENTANA.ModalClienteAnotaciones = {
	    TITLE : "UI_ANOTACIONES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "350px",
	    VIEW   : '../../SocioNegocio/UI/CLI/ModalClienteAnotaciones.html', 
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../SocioNegocio/UI/CLI/ModalClienteAnotaciones.js'] } ],

};

BCS_VENTANA.ModalSucursalAtencion = {
	    TITLE : "UI_PUNTOS_ATENCION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "140px",
	    VIEW   : '../../SocioNegocio/UI/EMP/ModalSucursalAtencion.html', 
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../SocioNegocio/UI/EMP/ModalSucursalAtencion.js'] } ],

};
BCS_VENTANA.modificarEstadoSocioNegocio = {
	    TITLE : "UI_ESTADO_SOCIO_NEGOCIO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "200px",
	    VIEW   : '../../SocioNegocio/UI/ModalSocioNegocioBloqueo.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/ModalSocioNegocioBloqueo.js']}],

};

BCS_VENTANA.agregarModificarDocumentoIdentidad = {
    TITLE : "UI_DOCUMENTO_IDENTIDAD",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "650px",
    HEIGHT : "188px",
    VIEW   : '../../SocioNegocio/UI/PER/ModalDocumentoIdentidad.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/PER/ModalDocumentoIdentidad.js']}],

};

BCS_VENTANA.agregarModificarEnrolamientoDocumentoIdentidad = {
	TITLE : "UI_DOCUMENTO_IDENTIDAD",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "650px",
	HEIGHT : "189px",
	VIEW   : '../../SocioNegocio/UI/ENR/PER/ModalDocumentoIdentidad.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/ENR/PER/ModalDocumentoIdentidad.js']}],

};

BCS_VENTANA.agregarModificarCuentaBancaria = {
    TITLE : "UI_CUENTA_BANCARIA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "650px",
    HEIGHT : "400px",
    VIEW   : '../../SocioNegocio/UI/PER/ModalCuentaBancaria.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/PER/ModalCuentaBancaria.js']}],

};

BCS_VENTANA.agregarModificarCuentaBancariaEnrolamiento = {
    TITLE : "UI_CUENTA_BANCARIA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "650px",
    HEIGHT : "251px",
    VIEW   : '../../SocioNegocio/UI/ENR/PER/ModalCuentaBancaria.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/ENR/PER/ModalCuentaBancaria.js']}],

};

BCS_VENTANA.agregarModificarTarjetaBancaria = {
    TITLE : "UI_TARJETA_BANCARIA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "650px",
    HEIGHT : "208px",
    VIEW   : '../../SocioNegocio/UI/PER/ModalTarjetaBancaria.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/PER/ModalTarjetaBancaria.js']}],

};

BCS_VENTANA.agregarModificarTelefono = {
    TITLE : "UI_TELEFONO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "650px",
    HEIGHT : "250px",
    VIEW   : '../../SocioNegocio/UI/PER/ModalTelefono.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/PER/ModalTelefono.js']}],

};

BCS_VENTANA.agregarModificarEnrolamientoTelefono = {
    TITLE : "UI_TELEFONO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "650px",
    HEIGHT : "250px",
    VIEW   : '../../SocioNegocio/UI/ENR/PER/ModalTelefono.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/ENR/PER/ModalTelefono.js']}],

};

BCS_VENTANA.agregarModificarDireccionElectronica = {
    TITLE : "UI_DIRECCION_ELECTRONICA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "650px",
    HEIGHT : "188px",
    VIEW   : '../../SocioNegocio/UI/PER/ModalDireccionElectronica.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/PER/ModalDireccionElectronica.js']}],

};

BCS_VENTANA.agregarModificarDireccionElectronicaEnrolamiento = {
    TITLE : "UI_DIRECCION_ELECTRONICA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "650px",
    HEIGHT : "170px",
    VIEW   : '../../SocioNegocio/UI/ENR/PER/ModalDireccionElectronica.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/ENR/PER/ModalDireccionElectronica.js']}],

};

BCS_VENTANA.agregarModificarDireccionFisica = {
    TITLE : "UI_DIRECCION_FISICA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "650px",
    HEIGHT : "335px",
    VIEW   : '../../SocioNegocio/UI/PER/ModalDireccionFisica.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/PER/ModalDireccionFisica.js']}],

};

BCS_VENTANA.agregarModificarEnrolamientoDireccionFisica = {
	TITLE : "UI_DIRECCION_FISICA",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "650px",
	HEIGHT : "317px",
	VIEW   : '../../SocioNegocio/UI/ENR/PER/ModalDireccionFisica.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/ENR/PER/ModalDireccionFisica.js']}],

};

BCS_VENTANA.agregarGrupoDireccionElectronica = {
	    TITLE : "UI_GRUPO_DIRECCION_ELECTRONICA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "225px",
	    VIEW   : '../../SocioNegocio/UI/PER/ModalGrupoDireccionElectronica.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/PER/ModalGrupoDireccionElectronica.js']}],

};

BCS_VENTANA.agregarEnrolamientoGrupoDireccionElectronica = {
	TITLE : "UI_GRUPO_DIRECCION_ELECTRONICA",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "464px",
	HEIGHT : "200px",
	VIEW   : '../../SocioNegocio/UI/ENR/PER/ModalGrupoDireccionElectronica.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/ENR/PER/ModalGrupoDireccionElectronica.js']}],

};

BCS_VENTANA.agregarGrupoDireccionElectronicaDetalle = {
	    TITLE : "UI_DETALLE_GRUPO_DIRECCION_ELECTRONICA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "700px",
	    HEIGHT : "500px",
	    VIEW   : '../../SocioNegocio/UI/PER/ModalGrupoDireccionElectronicaDetalle.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/PER/ModalGrupoDireccionElectronicaDetalle.js']}],

};

BCS_VENTANA.agregarEnrolamientoGrupoDireccionElectronicaDetalle = {
	TITLE : "UI_DETALLE_GRUPO_DIRECCION_ELECTRONICA",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "700px",
	HEIGHT : "500px",
	VIEW   : '../../SocioNegocio/UI/ENR/PER/ModalGrupoDireccionElectronicaDetalle.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/ENR/PER/ModalGrupoDireccionElectronicaDetalle.js']}],

};

BCS_VENTANA.agregarModificarRelacion = {
    TITLE : "UI_RELACION",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "500px",
    HEIGHT : "350px",
    VIEW   : '../../SocioNegocio/UI/PER/ModalRelacion.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/PER/ModalRelacion.js']}],

};

BCS_VENTANA.agregarModificarNombreFantasia = {
	    TITLE : "UI_NOMBRE_FANTASIA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "650px",
	    HEIGHT : "250px",
	    VIEW   : '../../SocioNegocio/UI/PER/ModalNombresFantasia.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/PER/ModalNombresFantasia.js']}],

};

BCS_VENTANA.agregarModificarFatcaPaisResidencia = {
    TITLE : "UI_PAIS_RESIDENCIA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "500px",
    HEIGHT : "200px",
    VIEW   : '../../SocioNegocio/UI/PER/ModalFatcaPaisResidencia.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/PER/ModalFatcaPaisResidencia.js']}],
};
BCS_VENTANA.ModalPersonaFatcaCuentasBancariasExtranjeras = {
    TITLE : "UI_CUENTAS_EXTRANJERAS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1000px",
    HEIGHT : "303px",
    VIEW   : '../../SocioNegocio/UI/PER/ModalPersonaFatcaCuentasBancariasExtranjeras.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/PER/ModalPersonaFatcaCuentasBancariasExtranjeras.js']}],
};

BCS_VENTANA.ModificarEstadoCliente = {
    TITLE : "UI_CAMBIO_ESTADO_CLIENTE",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "650px",
    HEIGHT : "180px",
    VIEW   : '../../SocioNegocio/UI/CLI/ModalClienteBloqueo.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/CLI/ModalClienteBloqueo.js']}],

};

BCS_VENTANA.ModalClienteDOF = {
	    TITLE : "UI_DECLARACION_ORIGEN_FONDOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "450px",
	    HEIGHT : "412px",
	    VIEW   : '../../SocioNegocio/UI/CLI/ModalClienteDOF.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/CLI/ModalClienteDOF.js']}],

};

BCS_VENTANA.SocioNegocioSeparadorIngresos= {
		SEPARATE: true,
	    TITLE : "UI_INGRESOS",
	    ENABLE : true
};

	

BCS_VENTANA.CrearCliente = {
    TITLE : "UI_INGRESAR_CLIENTE",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "600px",
    HEIGHT : "580px",
    VIEW   : '../../SocioNegocio/UI/CLI/CrearCliente.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../SocioNegocio/UI/CLI/CrearCliente.js'] } ],

};


BCS_VENTANA.CrearPersona = {
	    TITLE : "UI_INGRESAR_PERSONA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "600px",
	    HEIGHT : "400px",
	    VIEW   : '../../SocioNegocio/UI/PER/CrearPersona.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../SocioNegocio/UI/PER/CrearPersona.js'] } ],

};

	
BCS_VENTANA.ModificarEstadoPersona = {
	    TITLE : "UI_CAMBIO_ESTADO_PERSONA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "650px",
	    HEIGHT : "180px",
	    VIEW   : '../../SocioNegocio/UI/PER/ModalPersonaBloqueo.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/PER/ModalPersonaBloqueo.js']}],

};
	
BCS_VENTANA.CrearEmpleado = {
	    TITLE : "UI_INGRESAR_EMPLEADO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "600px",
	    HEIGHT : "580px",
	    VIEW   : '../../SocioNegocio/UI/EMP/CrearEmpleado.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../SocioNegocio/UI/EMP/CrearEmpleado.js'] } ],

};

	
BCS_VENTANA.CrearContraparte = {
	    TITLE : "UI_INGRESAR_CONTRAPARTE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "600px",
	    HEIGHT : "520px",
	    VIEW   : '../../SocioNegocio/UI/CNT/CrearContraparte.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../SocioNegocio/UI/CNT/CrearContraparte.js'] } ],

};


BCS_VENTANA.CrearProveedor = {
	    TITLE : "UI_INGRESAR_PROVEEDOR",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "600px",
	    HEIGHT : "480px",
	    VIEW   : '../../SocioNegocio/UI/PRO/CrearProveedor.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../SocioNegocio/UI/PRO/CrearProveedor.js'] } ],

};

BCS_VENTANA.CrearClienteFinal = {
	    TITLE : "UI_INGRESAR_CLIENTE_FINAL",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "600px",
	    HEIGHT : "280px",
	    VIEW   : '../../SocioNegocio/UI/CLI/CrearCliente.html',
	    PARAMS : {clienteFinal:true},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../SocioNegocio/UI/CLI/CrearCliente.js'] } ],

};

	
BCS_VENTANA.CrearClienteNomina = {
		    TITLE : "UI_INGRESAR_CLIENTE_NOMINA",
		    IMAGE  : "",
		    ENABLE : true,
		    WIDTH  : "900px",
		    HEIGHT : "430px",
		    VIEW   : '../../SocioNegocio/UI/CLI/CrearClienteNomina.html',
		    PARAMS : {},
		    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../SocioNegocio/UI/CLI/CrearClienteNomina.js'] } ],

};

BCS_VENTANA.Movimientos = {
	    TITLE : "UI_CLIENTES_MOVIMIENTOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1200px",
	    HEIGHT : "579px",
	    VIEW   : '../../SocioNegocio/UI/SocioMovimientos.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../SocioNegocio/UI/SocioMovimientos.js'] } ],

};
	
BCS_VENTANA.VerCambiosPendientes = {
	    TITLE : "UI_CAMBIOS_POR_AUTORIZAR",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "515px",
	    HEIGHT : "400px",
	    VIEW   : '../../SocioNegocio/UI/ventanaCambios.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../SocioNegocio/UI/ventanaCambios.js'] } ],

};
	
	
BCS_VENTANA.FirmaAnexo = {
	    TITLE : "UI_FIRMA_DOCUMENTOS", 
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "500px",
	    HEIGHT : "400px",
	    VIEW   : '../../SocioNegocio/UI/CLI/ModalClienteFirmaAnexo.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/CLI/ModalClienteFirmaAnexo.js'] } ],

};


BCS_VENTANA.FirmaAnexoUrl = { 
	    TITLE : "UI_INGRESAR_URL_CONTRATO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "515px",
	    HEIGHT : "250px",
	    VIEW   : '../../SocioNegocio/UI/CLI/ModalClienteFirmaAnexoUrl.html', 
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/CLI/ModalClienteFirmaAnexoUrl.js'] } ],
};

BCS_VENTANA.SubirAnexo = {
	    TITLE : "UI_SUBIR_PDF",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "340px",
	    HEIGHT : "250px",
	    VIEW   : '../../SocioNegocio/UI/CLI/ModalClienteDocumentoSubir.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/CLI/ModalClienteDocumentoSubir.js'] } ],

};

BCS_VENTANA.agregarModificarDocumento = {
	    TITLE : "UI_AGREGAR_DOCUMENTOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "600px",
	    HEIGHT : "400px",
	    VIEW   : '../../SocioNegocio/UI/CLI/ModalClienteDocumento.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/CLI/ModalClienteDocumento.js'] } ],

};

BCS_VENTANA.busquedaSocioNegocio = {
	    TITLE : "UI_BUSQUEDA_SOCIO_NEGOCIO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "775px",
	    HEIGHT : "360px",
	    VIEW   : '../../SocioNegocio/UI/busquedaSocioNegocio.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/busquedaSocioNegocio.js'] } ],

};

BCS_VENTANA.SocioNegocioSeparadorUtilitarios= {
		SEPARATE: true,
	    TITLE : "UI_UTILITARIOS",
	    ENABLE : true
};

	
BCS_VENTANA.ConsultarSupervisor = {
	    TITLE : "UI_SUPERVISORES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "515px",
	    HEIGHT : "auto",
	    VIEW   : '../../SocioNegocio/UI/EJE/ConsultarSupervisor.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../SocioNegocio/UI/EJE/ConsultarSupervisor.js'] } ],

};


BCS_VENTANA.modalSupervisor = {
	    TITLE : "UI_INGRESAR_SUPERVISOR",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "auto",
	    VIEW   : '../../SocioNegocio/UI/EJE/ModalSupervisor.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/EJE/ModalSupervisor.js'] } ],

};

BCS_VENTANA.ConsultarEjecutivo = {
	    TITLE : "UI_EJECUTIVOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "950px",
	    HEIGHT : "auto",
	    VIEW   : '../../SocioNegocio/UI/EJE/ConsultarEjecutivo.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../SocioNegocio/UI/EJE/ConsultarEjecutivo.js'] } ],

};


BCS_VENTANA.modalEjecutivo = {
	    TITLE : "UI_INGRESAR_EJECUTIVO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "auto",
	    VIEW   : '../../SocioNegocio/UI/EJE/ModalEjecutivo.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/EJE/ModalEjecutivo.js'] } ],

};

BCS_VENTANA.ConsultarClienteEjecutivo = {
	    TITLE : "UI_CLIENTES_EJECUTIVOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "515px",
	    HEIGHT : "auto",
	    VIEW   : '../../SocioNegocio/UI/EJE/ConsultarCliente.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../SocioNegocio/UI/EJE/ConsultarCliente.js'] } ],

};

BCS_VENTANA.modalReemplazoEjecutivo = {
	    TITLE : "UI_REEMPLAZO_EJECUTIVO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "auto",
	    VIEW   : '../../SocioNegocio/UI/EJE/ModalReemplazo.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/EJE/ModalReemplazo.js'] } ],

};

BCS_VENTANA.modalCambiarEjecutivo = {
	    TITLE : "UI_CAMBIAR_EJECUTIVO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "auto",
	    VIEW   : '../../SocioNegocio/UI/EJE/ModalCambiarEjecutivo.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/EJE/ModalCambiarEjecutivo.js'] } ],

};


BCS_VENTANA.modalModificarSupervisorAEjecutivo = {
 
		TITLE	: "UI_ACTUALIZAR_SUPERVISOR_A_EJECUTIVO",
		IMAGE 	: "",
		ENABLE 	: true,
		WIDTH 	: "400px",
		HEIGHT	: "auto",
		VIEW	: '../../SocioNegocio/UI/EJE/ModalModificarSupervisorAEjecutivo.html',
		PARAMS 	: {},
		DEPENDENCIES : [{ type: "js", cache : true, files : ['../../SocioNegocio/UI/EJE/ModalModificarSupervisorAEjecutivo.js']}]
};

BCS_VENTANA.modalTransferirClientes = {
	    TITLE : "UI_TRANSFERIR_CLIENTES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "auto",
	    VIEW   : '../../SocioNegocio/UI/EJE/ModalTransferirClientes.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/EJE/ModalTransferirClientes.js'] } ],

};

BCS_VENTANA.BloqueoMasivoClientes = {
	    TITLE : "UI_BLOQUEO_DESBLOQUEO_MASIVO_CLIENTES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "515px",
	    HEIGHT : "auto",
	    VIEW   : '../../SocioNegocio/UI/BLO/BloqueoMasivoClientes.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../SocioNegocio/UI/BLO/BloqueoMasivoClientes.js'] } ],

};

//BCS_VENTANA.GestionEnrolamiento = {
//	    TITLE  : "UI_GESTION_ENROLAMIENTO",
//	    IMAGE  : "",
//	    ENABLE : true,
//	    WIDTH  : "1100px",
//	    HEIGHT : "700px",
//	    VIEW   : '../../SocioNegocio/UI/GestionEnrolamiento.html',
//	    PARAMS : { p1: 0, p2: 0},
//	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/GestionEnrolamiento.js'] }],
//
//	};


//req PE
BCS_VENTANA.ActivacionMasivaClientes = {
	    TITLE : "UI_ACTIVACION_MASIVA_CLIENTES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "515px",
	    HEIGHT : "auto",
	    VIEW   : '../../SocioNegocio/UI/BLO/ActivacionMasivoClientes.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../SocioNegocio/UI/BLO/ActivacionMasivoClientes.js'] } ],

};


//req PE
BCS_VENTANA.BloqueoAutomaticoClientes = {
	    TITLE : "UI_BLOQUEO_AUTOMATICO_CLIENTES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "500px",
	    HEIGHT : "auto",
	    VIEW   : '../../SocioNegocio/UI/BLO/BloqueoAutomaticoClientes.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../SocioNegocio/UI/BLO/BloqueoAutomaticoClientes.js'] } ],
	    

};


//req PE
BCS_VENTANA.RecarterizacionMasivaClientes = {
	    TITLE : "UI_RECARTERIZACION_MASIVA_CLIENTES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "auto",
	    VIEW   : '../../SocioNegocio/UI/EJE/RecarterizacionMasivaClientes.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../SocioNegocio/UI/EJE/RecarterizacionMasivaClientes.js'] } ],

};

BCS_VENTANA.ConsultarNotificacionCliente = {
	    TITLE : "UI_CONSULTAR_NOTIFICACION_CLIENTE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "600px",
	    VIEW   : '../../SocioNegocio/UI/CLI/ConsultarNotificacionCliente/ConsultarNotificacionCliente.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../SocioNegocio/UI/CLI/ConsultarNotificacionCliente/ConsultarNotificacionCliente.js'] } ],

};

//---------------------------------------------
//PORTAFOLIO
//---------------------------------------------
BCS_VENTANA.MODULO_PORTAFOLIO = {
    TITLE : "UI_PORTAFOLIO",
    IMAGE  : "fa fa-suitcase",
    ENABLE : true,
    WIDTH  : "1000px",
    HEIGHT : "780px",
    VIEW   : '',
    PARAMS : {},
    DEPENDENCIES : [],

};

BCS_VENTANA.administrarPortafolio = {
    TITLE : "UI_ADMINISTRAR_PORTAFOLIO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1000px",
    HEIGHT : "680px",
    VIEW   : '../../Portafolio/UI/AdministrarPortafolio.html',
    PARAMS : {IdCliente:0,IdPortafolio:0},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Portafolio/UI/AdministrarPortafolio.js',
                            '../../Portafolio/UI/AdministrarPortafolio/PanelPortafolioDatosGenerales.js',
                            '../../Portafolio/UI/AdministrarPortafolio/PanelPortafolioIndicadores.js',
                            '../../Portafolio/UI/AdministrarPortafolio/PanelPortafolioNotificaciones.js',
                            '../../Portafolio/UI/AdministrarPortafolio/PanelPortafolioPreferencias.js',
                            '../../Portafolio/UI/AdministrarPortafolio/PanelPortafolioRequisitos.js',
                            '../../Portafolio/UI/AdministrarPortafolio/PanelPortafolioAutorizaciones.js',
                            '../../Portafolio/UI/AdministrarPortafolio/PanelPortafolioAnotaciones.js',
                            '../../Portafolio/UI/AdministrarPortafolio/PanelPortafolioLineaSobregiro.js',
                            //PreferenciasPago
                            '../../Portafolio/UI/AdministrarPortafolio/PanelPortafolioPreferenciasPagoRecibe.js',
                            '../../Portafolio/UI/AdministrarPortafolio/PanelPortafolioPreferenciasPagoPaga.js',
                            //plantillaComercial.
				            '../../Portafolio/UI/AdministrarPortafolio/PanelPortafolioPerfilComercial.js',
				            '../../Portafolio/UI/AdministrarPortafolio/PanelPortafolioPlantillaCobros.js',
				            '../../Portafolio/UI/AdministrarPortafolio/PanelPortafolioPlantillaCobrosPeru.js',
				            '../../Portafolio/UI/AdministrarPortafolio/PanelPortafolioPlantillaProductos.js',
				            '../../Portafolio/UI/AdministrarPortafolio/PanelPortafolioPlantillaLimites.js',
				            'COM/PlantillaProductos/PlantillaProductos.js', //servicios!
				            'COM/PlantillaCobros/PlantillaCobros.js',
				            'COM/PlantillaLimites/PlantillaLimites.js',
                            '../../Portafolio/UI/AdministrarPortafolio/PanelPortafolioCuentaCustodio.js',
                            //'../../Portafolio/UI/AdministrarPortafolio/PanelPortafolioDatosGeneralesSAAPV.js',
                            //Migraciones a Directivas.
                            '../../Portafolio/UI/bcs-portafolio-preferencias-pago/bcs-portafolio-preferencias-pago.js',
				            ] } ],

};

BCS_VENTANA.crearPortafolio = {
    TITLE : "UI_CREAR_PORTAFOLIO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "480px",
    HEIGHT : "310px",
    VIEW   : '../../Portafolio/UI/CrearPortafolio/CrearPortafolio.html',
    PARAMS : {IdCliente:0,IdPortafolio:0},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Portafolio/UI/CrearPortafolio/CrearPortafolio.js' ] } ],

};

BCS_VENTANA.modificarPortafolio = {
    TITLE : "UI_MODIFICAR_PROPOSITO_PORTAFOLIO_TITULO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "480px",
    HEIGHT : "310px",
    VIEW   : '../../Portafolio/UI/CrearPortafolio/CrearPortafolio.html',
    PARAMS : {IdCliente:0,IdPortafolio:0},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Portafolio/UI/CrearPortafolio/CrearPortafolio.js' ] } ],

};


BCS_VENTANA.modalMatrizOperacional = {
    TITLE : "UI_MATRIZ_OPERACIONAL",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "700px",
    HEIGHT : "600px",
    VIEW   : '../../Portafolio/UI/CrearPortafolio/ModalMatrizOperacional.html',
    PARAMS : {IdCliente:0,IdPortafolio:0},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Portafolio/UI/CrearPortafolio/ModalMatrizOperacional.js' ] } ],

};


BCS_VENTANA.agregarModificarPortafolioAutorizacion = {
    TITLE : "UI_AGREGAR_MODIFICAR_AUTORIZACION",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "550px",
    HEIGHT : "211px",
    VIEW   : '../../Portafolio/UI/AdministrarPortafolio/AdministrarPortafolioAutorizacionesModalCrear.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Portafolio/UI/AdministrarPortafolio/AdministrarPortafolioAutorizacionesModalCrear.js']}],

};

BCS_VENTANA.consultarPortafolios = {
    TITLE : "UI_CONSULTAR_PORTAFOLIOS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "500px",
    HEIGHT : "400px",
    VIEW   : '../../Portafolio/UI/ConsultarPortafolio/ConsultarPortafolio.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Portafolio/UI/ConsultarPortafolio/ConsultarPortafolio.js'] } ],

};

BCS_VENTANA.administrarPortafolioModificarEstadoModal = {
    TITLE : "UI_MODIFICAR_ESTADO_PORTAFOLIO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "400px",
    HEIGHT : "200px",
    VIEW   : '../../Portafolio/UI/AdministrarPortafolio/AdministrarPortafolioModificarEstadoModal.html', 
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Portafolio/UI/AdministrarPortafolio/AdministrarPortafolioModificarEstadoModal.js'] } ],

};

BCS_VENTANA.ModalPortafolioAnotaciones = {
    TITLE : "UI_ANOTACIONES",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "400px",
    HEIGHT : "300px",
    VIEW   : '../../Portafolio/UI/AdministrarPortafolio/ModalPortafolioAnotaciones.html', 
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Portafolio/UI/AdministrarPortafolio/ModalPortafolioAnotaciones.js'] } ],

};

BCS_VENTANA.modalPortafolioPreferenciaPago = {
    TITLE : "UI_PREFERENCIA_PAGO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "400px",
    HEIGHT : "256px",
    VIEW   : '../../Portafolio/UI/bcs-portafolio-preferencias-pago/ModalPortafolioPreferenciaPago.html',
    PARAMS : {IdCliente:0,IdPortafolio:0},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Portafolio/UI/bcs-portafolio-preferencias-pago/ModalPortafolioPreferenciaPago.js' ] } ],

};

BCS_VENTANA.modalEnrolamientoPortafolioCrearPreferenciaPago = {
	TITLE : "UI_PREFERENCIA_PAGO",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "400px",
	HEIGHT : "193px",
	VIEW   : '../../SocioNegocio/UI/ENR/POR/ModalPortafolioCrearPreferenciaPago.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../SocioNegocio/UI/ENR/POR/ModalPortafolioCrearPreferenciaPago.js' ] } ],

};

BCS_VENTANA.modalEnrolamientoPortafolioEditarPreferenciaPago = {
	TITLE : "UI_PREFERENCIA_PAGO",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "400px",
	HEIGHT : "256px",
	VIEW   : '../../SocioNegocio/UI/ENR/POR/ModalPortafolioEditarPreferenciaPago.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../SocioNegocio/UI/ENR/POR/ModalPortafolioEditarPreferenciaPago.js' ] } ],

};

BCS_VENTANA.modalAgregarLineaSobregiro = {
    TITLE : "UI_LINEA_SOBREGIRO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "400px",
    HEIGHT : "250px",
    VIEW   : '../../Portafolio/UI/AdministrarPortafolio/ModalPortafolioLineaSobregiro.html',
    PARAMS : {IdCliente:0,IdPortafolio:0},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Portafolio/UI/AdministrarPortafolio/ModalPortafolioLineaSobregiro.js' ] } ],

};

BCS_VENTANA.ModalPortafolioCuentaCustodio = {
	    TITLE : "UI_CUENTA_CUSTODIO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "450px",
	    HEIGHT : "235px",
	    VIEW   : '../../Portafolio/UI/AdministrarPortafolio/ModalPortafolioCuentaCustodio.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Portafolio/UI/AdministrarPortafolio/ModalPortafolioCuentaCustodio.js' ] } ],

};

BCS_VENTANA.ModalCrearPortafolioCuentaCustodio = {
	TITLE : "UI_CUENTA_CUSTODIO",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "450px",
	HEIGHT : "325px",
	VIEW   : '../../SocioNegocio/UI/ENR/POR/ModalPortafolioCuentaCustodio.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../SocioNegocio/UI/ENR/POR/ModalPortafolioCuentaCustodio.js' ] } ],
};

BCS_VENTANA.ModalPortafolioCrearCuentaCustodio = {
	TITLE : "UI_CUENTA_CUSTODIO",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "450px",
	HEIGHT : "222px",
	VIEW   : '../../SocioNegocio/UI/ENR/POR/ModalPortafolioCrearCuentaCustodio.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../SocioNegocio/UI/ENR/POR/ModalPortafolioCrearCuentaCustodio.js' ] } ],
};

BCS_VENTANA.ModalPortafolioEditarCuentaCustodio = {
	TITLE : "UI_CUENTA_CUSTODIO",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "450px",
	HEIGHT : "325px",
	VIEW   : '../../SocioNegocio/UI/ENR/POR/ModalPortafolioEditarCuentaCustodio.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../SocioNegocio/UI/ENR/POR/ModalPortafolioEditarCuentaCustodio.js' ] } ],
};

//---------------------------------------------
//AGENTE
//---------------------------------------------
BCS_VENTANA.MODULO_AGENTE = {
	    TITLE  : "UI_AGENTE",
	    IMAGE  : "sapUiIcon crm-sales",
	    ENABLE : true,
	    WIDTH  : "0px", 
	    HEIGHT : "0px",
	    VIEW   : '',
	    PARAMS : {},
	    DEPENDENCIES : [], 

};


BCS_VENTANA.AgenteSeparadorInformacionAgente = {
		SEPARATE: true,
	    TITLE : "UI_AGENTE",
	    ENABLE : true
};


BCS_VENTANA.AgenteConsultarResumemEjecutivo= {
	    TITLE : "UI_RESUMEN_EJECUTIVO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "650px",
	    HEIGHT : "220px",
	    VIEW   : '../../SocioNegocio/UI/EJE/ConsultarResumenEjecutivo.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/EJE/ConsultarResumenEjecutivo.js'] } ],

};



BCS_VENTANA.AgenteSeparadorInformacionCliente = {
		SEPARATE: true,
	    TITLE : "UI_CLIENTE",
	    ENABLE : true
};


BCS_VENTANA.ResumenInversionCliente = {
	    TITLE : "UI_RESUMEN_INVERSION_CLIENTE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "785px",
	    HEIGHT : "710px",
	    VIEW   : '../../SocioNegocio/UI/CLI/ResumenInversion/ResumenInversionCliente.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: false, files: ['../../SocioNegocio/UI/CLI/ResumenInversion/ResumenInversionCliente.js'] }],
};

BCS_VENTANA.ResumenPortafolioCliente = {
	    TITLE : "UI_RESUMEN_INVERSION_PORTAFOLIO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1639px",
	    HEIGHT : "812px",
	    VIEW   : '../../SocioNegocio/UI/CLI/ResumenPortafolioCliente/ResumenPortafolioCliente.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: false, files: ['../../SocioNegocio/UI/CLI/ResumenPortafolioCliente/ResumenPortafolioCliente.js'] }],
};

	
BCS_VENTANA.AgenteConsultaAvanzadaClientes = {
	    TITLE : "UI_CONSULTA_AVANZADA_CLIENTES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
	    HEIGHT : "675px",
	    VIEW   : '../../SocioNegocio/UI/CLI/ConsultaAvanzadaClientes/ConsultaAvanzadaClientes.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: false, files: ['../../SocioNegocio/UI/CLI/ConsultaAvanzadaClientes/ConsultaAvanzadaClientes.js'] }],

};






BCS_VENTANA.AgenteSeparadorOperacionAcciones = {
		SEPARATE: true,
	    TITLE : "UI_ACCIONES",
	    ENABLE : true
};


BCS_VENTANA.AgenteIngresarOrdenMain = {
	    TITLE : "UI_INGRESAR_ORDEN",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "650px",
	    VIEW   : '../../Orden/UI/IngresarOrden/IngresarOrdenRV.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: false, files: [,'../../Orden/UI/IngresarOrden/IngresarOrdenRV.js'
	    													,'../../Orden/UI/bcsCobros/bcsCobrosBeta.js'
	    													,'../../Orden/UI/bcsCobros/bcsCobrosOrden.js'
	                                                         ,'../../Orden/UI/ModuloOrdenesDirectivas.js'
	                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

};


BCS_VENTANA.AgenteBlotterOrdenes = {
	    TITLE : "UI_GESTION_ORDENES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
	    HEIGHT : "686px",
	    VIEW   : '../../Orden/UI/BlotterOrdenes/BlotterOrdenes.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/BlotterOrdenes/BlotterOrdenes.js'
	                                                         ,'../../Orden/UI/ConsultarOrdenes/ConsultarOrdenes.js'
	                                                         ,'../../Orden/UI/ConsultarAsignaciones/ConsultarAsignaciones.js'
	                                                         ,'../../Orden/UI/ConsultarTransacciones/ConsultarTransacciones.js'
	                                                         ,'../../Orden/UI/barraFiltro/barraFiltros.js'
	                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'
	                                                         ,'../../Orden/UI/ModuloOrdenesDirectivas.js'
	                                                         ,'../../Orden/UI/ConsultarAsignaciones/EnvioFacturarModal.js'] } ],

};

	
	
BCS_VENTANA.AgenteSeparadorOperacionFFMM= {
		SEPARATE: true,
	    TITLE : "UI_FONDOS_MUTUOS",
	    ENABLE : true
};



//---------------------------------------------
//ORDEN
//---------------------------------------------
BCS_VENTANA.MODULO_ORDEN= {
    TITLE : "UI_OPERACIONES_EN_RUEDA",
    IMAGE  : "sapUiIcon business-one",
    ENABLE : true,
    WIDTH  : "0px",
    HEIGHT : "0px",
    VIEW   : '',
    PARAMS : {},
    DEPENDENCIES : [],

};


BCS_VENTANA.BlotterOrdenes = {
    TITLE : "UI_GESTION_ORDENES",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1100px",
    HEIGHT : "650px",
    VIEW   : '../../Orden/UI/BlotterOrdenes/BlotterOrdenes.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/BlotterOrdenes/BlotterOrdenes.js'
                                                         ,'../../Orden/UI/ConsultarOrdenes/ConsultarOrdenes.js'
                                                         ,'../../Orden/UI/ConsultarAsignaciones/ConsultarAsignaciones.js'
                                                         ,'../../Orden/UI/ConsultarTransacciones/ConsultarTransacciones.js'
                                                         ,'../../Orden/UI/barraFiltro/barraFiltros.js'
                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'
                                                         ,'../../Orden/UI/ModuloOrdenesDirectivas.js'
                                                         ,'../../Orden/UI/ConsultarAsignaciones/EnvioFacturarModal.js'] } ],

};

BCS_VENTANA.BlotterInstrucciones = {
	TITLE: "UI_GESTION_INSTRUCCIONES",
	IMAGE: "",
	ENABLE: true,
	WIDTH: "1100px",
	HEIGHT: "auto",
	VIEW: '../../Orden/UI/BlotterInstrucciones/BlotterInstrucciones.html',
	PARAMS: {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/BlotterInstrucciones/BlotterInstrucciones.js'
														,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'
														,'../../Orden/UI/ModuloOrdenesDirectivas.js'
														,'../../Orden/UI/ConsultarInstrucciones/ConsultarInstrucciones.js'] } ],
};

BCS_VENTANA.ConsultarOrdenes = {
	    TITLE : "UI_ORDENES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
	    HEIGHT : "700px",
	    VIEW   : '../../Orden/UI/ConsultarOrdenes/ConsultarOrdenes.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/ConsultarOrdenes/ConsultarOrdenes.js'
	    													,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'
	    													,'../../Orden/UI/ModuloOrdenesDirectivas.js'] } ],

	};

BCS_VENTANA.ConsultarPosicion = {
    TITLE : "UI_CONSULTA_DE_POSICION",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1100px",
    HEIGHT : "526px",
    VIEW   : '../../Orden/UI/ConsultarPosicion/ConsultarPosicion.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/ConsultarPosicion/ConsultarPosicion.js'
    													,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'
    													,'../../Orden/UI/ModuloOrdenesDirectivas.js'] } ],

};

BCS_VENTANA.ConsultarDemanda = {
    TITLE : "UI_DEMANDA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "700px",
    HEIGHT : "500px",
    VIEW   : '../../Orden/UI/ConsultarPosicion/ConsultarDemanda.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/ConsultarPosicion/ConsultarDemanda.js'
    													,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'
    													,'../../Orden/UI/ModuloOrdenesDirectivas.js'] } ],

};

//CGO
BCS_VENTANA.ConsultarPosicionAsignacion = {
    TITLE : "UI_CONSULTA_DE_POSICION_ASIGNACION",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1100px",
    HEIGHT : "500px",
    VIEW   : '../../Orden/UI/ConsultarPosicion/ConsultarPosicionAsignacion.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/ConsultarPosicion/ConsultarPosicionAsignacion.js'
    													,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'
    													,'../../Orden/UI/ModuloOrdenesDirectivas.js'] } ],

};

BCS_VENTANA.ConsultarDemandaAsignacion = {
    TITLE : "UI_DEMANDA_ASIGNACION",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "700px",
    HEIGHT : "500px",
    VIEW   : '../../Orden/UI/ConsultarPosicion/ConsultarDemandaAsignacion.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/ConsultarPosicion/ConsultarDemandaAsignacion.js'
    													,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'
    													,'../../Orden/UI/ModuloOrdenesDirectivas.js'] } ],

};
//CGO

BCS_VENTANA.CargaArchivosAsignacion = {
	    TITLE : "UI_CARGA_ARCHIVOS_OPERACIONES_RUEDA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "650px",
	    HEIGHT : "188px",
	    VIEW   : '../../Orden/UI/CargaArchivoOperacionesRueda/CargaArchivosAsignacion.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/CargaArchivoOperacionesRueda/CargaArchivosAsignacion.js'
	    													,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

	};

BCS_VENTANA.CargaArchivosOrdenes = {
	    TITLE : "UI_CARGA_ARCHIVOS_OPERACIONES_RUEDA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "650px",
	    HEIGHT : "214px",
	    VIEW   : '../../Orden/UI/CargaArchivosOrdenes/CargaArchivosOrdenes.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/CargaArchivosOrdenes/CargaArchivosOrdenes.js'
	    													,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

	};

BCS_VENTANA.CargaArchivosTransaccionesOrdenes = {
	    TITLE : "UI_CARGA_ARCHIVOS_TRANSACCIONES_ORDENES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "650px",
	    HEIGHT : "188px",
	    VIEW   : '../../Orden/UI/CargaArchivosTransaccionesOrdenes/CargaArchivosTransaccionesOrdenes.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/CargaArchivosTransaccionesOrdenes/CargaArchivosTransaccionesOrdenes.js'
	    													,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

	};

BCS_VENTANA.OrdenesSeparadorIngresos = {
		SEPARATE: true,
	    TITLE : "UI_INGRESOS",
	    ENABLE : true
};


BCS_VENTANA.IngresarOrdenMain = {
	    TITLE : "UI_INGRESAR_ORDEN",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "650px",
	    VIEW   : '../../Orden/UI/IngresarOrden/IngresarOrdenRV.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: false, files: [,'../../Orden/UI/IngresarOrden/IngresarOrdenRV.js'
	                                                         ,'../../Orden/UI/bcsCobros/bcsCobrosBeta.js'
	                                                         ,'../../Orden/UI/bcsCobros/bcsCobrosOrden.js'
	                                                         ,'../../Orden/UI/ModuloOrdenesDirectivas.js'
	                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

};

BCS_VENTANA.IngresarInstruccionMain = {
	    TITLE : "UI_INGRESAR_INSTRUCCION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "920px",
	    HEIGHT : "553px",
	    VIEW   : '../../Orden/UI/IngresarInstruccion/IngresarInstruccion.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: false, files: [,'../../Orden/UI/IngresarInstruccion/IngresarInstruccion.js'] } ],

};

BCS_VENTANA.ConfirmarInstruccion = {
	    TITLE : "UI_CONFIRMAR_INSTRUCCION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "720px",
	    HEIGHT : "595px",
	    VIEW   : '../../Orden/UI/ConfirmarInstruccion/ConfirmarInstruccion.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: false, files: [,'../../Orden/UI/ConfirmarInstruccion/ConfirmarInstruccion.js'] } ],

};

BCS_VENTANA.DetalleInstruccion= {
	    TITLE : "UI_INFORMACION_INSTRUCCION",
	    IMAGE  : "",
	    PRINTABLE : true,
    	ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "650px",
	    VIEW   : '../../Orden/UI/DetalleInstruccion/DetalleInstruccion.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: false, files: [ '../../Orden/UI/DetalleInstruccion/DetalleInstruccion.js'
															 ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

};

BCS_VENTANA.IngresarOrdenRF = {
	    TITLE : "UI_INGRESAR_ORDEN_RF",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "716px",
	    VIEW   : '../../Orden/UI/IngresarOrden/IngresarOrdenRF.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: false, files: [,'../../Orden/UI/IngresarOrden/IngresarOrdenRF.js'
	                                                         ,'../../Orden/UI/bcsCobros/bcsCobrosOrden.js'
	                                                         ,'../../Orden/UI/ModuloOrdenesDirectivas.js'
	                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

};


BCS_VENTANA.IngresarOrdenOP = {
	    TITLE : "UI_INGRESAR_ORDEN_OP",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "650px",
	    VIEW   : '../../Orden/UI/IngresarOrden/IngresarOrdenOP.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: false, files: [,'../../Orden/UI/IngresarOrden/IngresarOrdenOP.js'
	                                                         ,'../../Orden/UI/bcsCobros/bcsCobrosOrden.js'
	                                                         ,'../../Orden/UI/ModuloOrdenesDirectivas.js'
	                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

};


BCS_VENTANA.IngresarOrdenOpcionesPE = {
	    TITLE : "UI_INGRESAR_ORDEN_OPCION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "660px",
	    VIEW   : '../../Orden/UI/OpcionesPE/IngresarOrdenOpciones.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: false, files: [,'../../Orden/UI/OpcionesPE/IngresarOrdenOpciones.js'
	                                                         ,'../../Orden/UI/bcsCobros/bcsCobrosOrden.js'
	                                                         ,'../../Orden/UI/ModuloOrdenesDirectivas.js'
	                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

};

BCS_VENTANA.AsignacionCliente = {
    TITLE : "UI_ASIGNACION_CLIENTE",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "650px",
    HEIGHT : "300px",
    VIEW   : '../../Orden/UI/AsignacionCliente/AsignacionCliente.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: false, files: [ '../../Orden/UI/AsignacionCliente/AsignacionCliente.js'
                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],
};

BCS_VENTANA.AsignacionGrupo = {
    TITLE : "UI_ASIGNACION_GRUPO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1000px",
    HEIGHT : "auto",
    VIEW   : '../../Orden/UI/AsignacionGrupo/AsignacionGrupo.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: false, files: [ '../../Orden/UI/AsignacionGrupo/AsignacionGrupo.js'
                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],
};

BCS_VENTANA.CrearOrdenSimultanea = {
    TITLE : "UI_INGRESAR_ORDEN_SIMULTANEA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "640px",
    VIEW   : '../../Orden/UI/CrearOrdenSimultanea/CrearOrdenSimultanea.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/CrearOrdenSimultanea/CrearOrdenSimultanea.js'
                                                         ,'../../Orden/UI/CrearOrdenSimultanea/ConsultarTasas.js'
                                                         ,'../../Orden/UI/bcsCobros/bcsCobrosSimultanea.js'
                                                         ,'../../Orden/UI/ModuloOrdenesDirectivas.js'
                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

};


BCS_VENTANA.DetalleSimultanea = {
    TITLE : "UI_INFORMACION_ORDEN_SIMULTANEA",
    IMAGE  : "",
    PRINTABLE : true,
    ENABLE : true,
    WIDTH  : "750px",
    HEIGHT : "500px",
    VIEW   : '../../Orden/UI/DetalleSimultanea/DetalleSimultanea.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/DetalleSimultanea/DetalleSimultanea.js'] } ],

};

BCS_VENTANA.CrearTransaccion = {
    TITLE : "UI_INGRESAR_TRANSACCION",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "750px",
    VIEW   : '../../Orden/UI/CrearTransaccion/CrearTransaccion.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Orden/UI/CrearTransaccion/CrearTransaccion.js'
                                                          ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

};


BCS_VENTANA.OrdenesSeparadorOperaciones = {
		SEPARATE: true,
	    TITLE : "UI_OPERACIONES",
	    ENABLE : true
};


BCS_VENTANA.InterfazAsignacion = {
    TITLE : "UI_INTERFAZ_ASIGNACION",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1100px",
    HEIGHT : "720px",
    VIEW   : '../../Orden/UI/InterfazAsignacion/InterfazAsignacion.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/InterfazAsignacion/InterfazAsignacion.js'
                                                         ,'../../Orden/UI/barraFiltro/barraFiltros.js'
                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

};

BCS_VENTANA.InterfazRefacturacion = {
    TITLE : "UI_INTERFAZ_REFACTURACION",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1100px",
    HEIGHT : "720px",
    VIEW   : '../../Orden/UI/InterfazRefacturacion/InterfazRefacturacion.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/InterfazRefacturacion/InterfazRefacturacion.js'
                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

};


BCS_VENTANA.CrearMovimientosDesdeOtroComprobante = {
	    TITLE : "UI_CREAR_MOVIMIENTOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "529px",
	    HEIGHT : "200px",
	    VIEW   : '../../Facturacion/UI/CrearMovimientosDesdeOtroComprobante/CrearMovimientosDesdeOtroComprobante.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Facturacion/UI/CrearMovimientosDesdeOtroComprobante/CrearMovimientosDesdeOtroComprobante.js'] } ],
};


BCS_VENTANA.SelectorComprobantes = {
	    TITLE : "UI_SELECCIONAR",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
	    HEIGHT : "420px",
	    VIEW   : '../../Orden/UI/InterfazRefacturacion/SelectorComprobantes.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/InterfazRefacturacion/SelectorComprobantes.js'
	                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

	};


BCS_VENTANA.SelectorCuentaCustodio = {
	    TITLE : "UI_SELECCIONAR",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "300px",
	    VIEW   : '../../Plataforma/UI/PLA/SelectorCuentaCustodio/SelectorCuentaCustodio.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Plataforma/UI/PLA/SelectorCuentaCustodio/SelectorCuentaCustodio.js'] } ],

	};

BCS_VENTANA.SelectorEmisores = {
	    TITLE : "UI_SELECCIONAR",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "300px",
	    VIEW   : '../../Plataforma/UI/PLA/SelectorEmisores/SelectorEmisores.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Plataforma/UI/PLA/SelectorEmisores/SelectorEmisores.js'] } ],

	};

BCS_VENTANA.AsignacionPropCrono = {
    TITLE : "UI_ASIGNACION_AUTOMATICA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "400px",
    HEIGHT : "330px",
    VIEW   : '../../Orden/UI/AsignacionPropCrono/AsignacionPropCrono.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/AsignacionPropCrono/AsignacionPropCrono.js'
                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

};


BCS_VENTANA.InformacionAsignacionPropCrono = {
	    TITLE : "UI_INFORMACION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "520px",
	    VIEW   : '../../Orden/UI/InformacionAsignacionPropCrono/InformacionAsignacionPropCrono.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/InformacionAsignacionPropCrono/InformacionAsignacionPropCrono.js'
	                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

};

BCS_VENTANA.EjecutarDesasignacion = {
	   TITLE : "UI_EJECUTAR_DESASIGNACION",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "400px",
    HEIGHT : "300px",
    VIEW   : '../../Orden/UI/EjecutarDesasignacion/EjecutarDesasignacion.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/EjecutarDesasignacion/EjecutarDesasignacion.js'] } ],

};


BCS_VENTANA.PrevisualizarDesasignacion = {
    TITLE : "UI_PREVISUALIZAR_DESASIGNACION",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "400px",
    VIEW   : '../../Orden/UI/EjecutarDesasignacion/PrevisualizarDesasignacion.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/EjecutarDesasignacion/PrevisualizarDesasignacion.js','../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

};


BCS_VENTANA.LiberacionMasiva = {
    TITLE : "UI_LIBERACION_MASIVA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "400px",
    HEIGHT : "300px",
    VIEW   : '../../Orden/UI/LiberacionMasiva/LiberacionMasiva.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/LiberacionMasiva/LiberacionMasiva.js'] } ],

};


BCS_VENTANA.PrevisualizarLiberacionMasiva = {
    TITLE : "UI_PREVISUALIZAR_LIBERACION",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "400px",
    VIEW   : '../../Orden/UI/LiberacionMasiva/PrevisualizarLiberacionMasiva.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/LiberacionMasiva/PrevisualizarLiberacionMasiva.js'] } ],

};


BCS_VENTANA.SelectorCarteras = {
    TITLE : "UI_SELECCIONAR_CARTERA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "auto",
    VIEW   : '../../Plataforma/UI/PLA/SelectorCarteras/SelectorCarteras.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Plataforma/UI/PLA/SelectorCarteras/SelectorCarteras.js'] } ],

};

BCS_VENTANA.EdicionMultiple = {
    TITLE : "UI_EDICION_MULTIPLE",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "700px",
    HEIGHT : "400px",
    VIEW   : '../../Orden/UI/InterfazAsignacion/EdicionCobros.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/InterfazAsignacion/EdicionCobros.js'] } ],

};

BCS_VENTANA.EditarAsignacion = {
    TITLE : "UI_EDITAR_ASIGNACION",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "950px",
    HEIGHT : "473px",
    VIEW   : '../../Orden/UI/InterfazAsignacion/EditarAsignacion.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/InterfazAsignacion/EditarAsignacion.js',
    													,'../../Orden/UI/bcsCobros/bcsCobrosOrden.js'] } ],

};

BCS_VENTANA.BlotterOrdenesNegociacion = {
    TITLE : "UI_NEGOCIACION_ORDENES",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1170px",
    HEIGHT : "573px",
    VIEW   : '../../Orden/UI/BlotterOrdenesNegociacion/BlotterOrdenesNegociacion.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/BlotterOrdenesNegociacion/BlotterOrdenesNegociacion.js',
                                                         '../../Orden/UI/BlotterOrdenesNegociacion/OrdenesEnNegociacion.js',
                                                         '../../Orden/UI/BlotterOrdenesNegociacion/OrdenesPorNegociar.js'
                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

};


BCS_VENTANA.EnvioFacturarMasivo = {
    TITLE : "UI_ENVIO_FACTURACION",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "741px",
    HEIGHT : "210px",
    VIEW   : '../../Orden/UI/EnvioFacturacionMasivo/EnvioFacturacionMasivo.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/EnvioFacturacionMasivo/EnvioFacturacionMasivo.js',
                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

};


BCS_VENTANA.GenerarInformeOperacionesRueda = {
    TITLE : "UI_GENERAR_INFORMES",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "450px",
    HEIGHT : "170px",
    VIEW   : '../../Orden/UI/GenerarInforme/GenerarInforme.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/GenerarInforme/GenerarInforme.js',
                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

};

BCS_VENTANA.VencimientoOrdenes = {
	    TITLE : "UI_VENCIMIENTO_ORDENES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "350px",
	    HEIGHT : "128px",
	    VIEW   : '../../Orden/UI/VencimientoOrdenes/VencimientoOrdenes.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/VencimientoOrdenes/VencimientoOrdenes.js'] } ],

	};

BCS_VENTANA.VentanaInformativa = {
	    TITLE : "UI_VENTANA_INFORMATIVA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1020px",
	    HEIGHT : "402px",
	    VIEW   : '../../Orden/UI/VentanaInformativa/VentanaInformativa.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/VentanaInformativa/VentanaInformativa.js',
	                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

};

BCS_VENTANA.VentanaResultado = {
	    TITLE : "UI_VENTANA_RESULTADO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "700px",
	    HEIGHT : "402px",
	    VIEW   : '../../Orden/UI/VentanaResultado/VentanaResultado.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/VentanaResultado/VentanaResultado.js',
	                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

};

BCS_VENTANA.OrdenesSeparadorCierre = {
		SEPARATE: true,
	    TITLE : "UI_APERTURA_CIERRE",
	    ENABLE : true
};


BCS_VENTANA.AperturaModuloOrdenes = {
    TITLE : "UI_APERTURA_MODULO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "370px",
    HEIGHT : "160px", 
    VIEW   : '../../Orden/UI/AperturaModulo/AperturaOrdenes.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Orden/UI/AperturaModulo/AperturaOrdenes.js']}],

};


BCS_VENTANA.CuadraturaCierreDiario = {
    TITLE : "UI_CUADRATURA_CIERRE_DIARIO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1028px",
    HEIGHT : "710px", 
    VIEW   : '../../Orden/UI/CuadraturaDiariaOperaciones/CuadraturaDiariaOperaciones.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Orden/UI/CuadraturaDiariaOperaciones/CuadraturaDiariaOperaciones.js']}],

};

BCS_VENTANA.ConsultaFacturacionDte = {
	    TITLE : "UI_CONSULTA_FACTURACION_DTE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "350px",
	    HEIGHT : "100px",
	    VIEW   : '../../Facturacion/UI/ConsultaFacturacionDte/ConsultaFacturacionDte.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Facturacion/UI/ConsultaFacturacionDte/ConsultaFacturacionDte.js']}],

};

BCS_VENTANA.ConsultaTareasDte = {
	    TITLE : "UI_CONSULTA_TAREAS_DTE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "950px",
	    HEIGHT : "574px",
	    VIEW   : '../../Facturacion/UI/ConsultaTareasDte/ConsultaTareasDte.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Facturacion/UI/ConsultaTareasDte/ConsultaTareasDte.js']}],

};

BCS_VENTANA.CuadraturaDiariaOperaciones = {
	    TITLE : "UI_CUADRATURA_DIARIA_OPERACIONES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1028px",
	    HEIGHT : "710px", 
	    VIEW   : '../../Orden/UI/CuadraturaDiariaOperaciones/CuadraturaDiariaOperaciones.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Orden/UI/CuadraturaDiariaOperaciones/CuadraturaDiariaOperaciones.js']}],

	};

BCS_VENTANA.OrdenesSeparadorConfiguracion = {
		SEPARATE: true,
	    TITLE : "UI_CONFIGURACION",
	    ENABLE : true
};


BCS_VENTANA.AsignacionCronologicaPorLibro = { 
	    TITLE : "UI_ASIGNACION_POR_LIBRO",
	    IMAGE  : "",
	    ENABLE : false,
	    WIDTH  : "320px",
	    HEIGHT : "170px",
	    VIEW   : '../../Orden/UI/ConsultarAsignaciones/AsignacionCronologicaPorLibroModal.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/ConsultarAsignaciones/AsignacionCronologicaPorLibroModal.js'
	                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

};


BCS_VENTANA.ExportarLibro = {
    TITLE : "UI_EXPORTAR_LIBRO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "300px",
    HEIGHT : "163px",
    VIEW   : '../../Orden/UI/ExportarLibro/ExportarLibro.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/ExportarLibro/ExportarLibro.js'
                                                         ] } ],

};

BCS_VENTANA.AjustarTipoCambio = {
    TITLE : "UI_TIPO_CAMBIO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "300px",
    HEIGHT : "163px",
    VIEW   : '../../Orden/UI/ConsultarAsignaciones/AjustarTipoCambioModal.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/ConsultarAsignaciones/AjustarTipoCambioModal.js'
                                                         ] } ],

};

BCS_VENTANA.IngresarMotivoReservarLiberar = {
    TITLE : "UI_INGRESAR_MOTIVO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "400px",
    HEIGHT : "146px",
    VIEW   : '../../Orden/UI/IngresarMotivoReservarLiberar/IngresarMotivoReservarLiberar.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/IngresarMotivoReservarLiberar/IngresarMotivoReservarLiberar.js'
                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

};

BCS_VENTANA.ModificarOrdenRV = {
	    TITLE : "UI_MODIFICAR_ORDEN_RV",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "140px",
	    VIEW   : '../../Orden/UI/ModificarOrdenRV/ModificarOrdenRV.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/ModificarOrdenRV/ModificarOrdenRV.js'
	                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

	};

BCS_VENTANA.ModificarOrdenRF = {
	    TITLE : "UI_MODIFICAR_ORDEN_RF",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "200px",
	    VIEW   : '../../Orden/UI/ModificarOrdenRF/ModificarOrdenRF.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/ModificarOrdenRF/ModificarOrdenRF.js'] } ],

	};

BCS_VENTANA.IngresarMotivoAnularOrdenes = {
	    TITLE : "UI_INGRESAR_MOTIVO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "140px",
	    VIEW   : '../../Orden/UI/ConsultarOrdenes/IngresarMotivoAnularOrdenes.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/ConsultarOrdenes/IngresarMotivoAnularOrdenes.js'] } ],

	};

BCS_VENTANA.SelectorClientes = {
    TITLE : "UI_SELECCIONAR",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1100px",
    HEIGHT : "420px",
    VIEW   : '../../Orden/UI/InterfazAsignacion/SelectorClientes.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/InterfazAsignacion/SelectorClientes.js'
                                                         ,'../../Orden/UI/barraFiltro/barraFiltros.js'
                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

};

BCS_VENTANA.SelectorClientesReasignacionRF = {
    TITLE : "UI_SELECCIONAR",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1000px",
    HEIGHT : "auto",
    VIEW   : '../../Mesa/UI/MRF/ReasignacionMesaRF/SelectorClientes.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/MRF/ReasignacionMesaRF/SelectorClientes.js'
                                                         ,'../../Orden/UI/barraFiltro/barraFiltros.js'
                                                         ,'../../Mesa/UI/ModuloMesaRFConfGrids.js'] } ],

};

BCS_VENTANA.SelectorElementos = {
    TITLE : "UI_SELECCIONAR",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1100px",
    HEIGHT : "auto",
    VIEW   : '../../Orden/UI/SelectorElementos/SelectorElementos.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/SelectorElementos/SelectorElementos.js'
                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

};

BCS_VENTANA.SelectorTransacciones = {
	    TITLE : "UI_SELECCIONAR",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
	    HEIGHT : "600px",
	    VIEW   : '../../Orden/UI/SelectorTransacciones/SelectorTransacciones.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/SelectorTransacciones/SelectorTransacciones.js'
	                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

};

BCS_VENTANA.EnviarFacturar = {
    TITLE : "UI_ENVIAR_FACTURAR",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "800px",
    HEIGHT : "200px",
    VIEW   : '../../Orden/UI/ConsultarAsignaciones/EnvioFacturarModal.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/ConsultarAsignaciones/EnvioFacturarModal.js'
                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

};

BCS_VENTANA.BitacoraOrden = {
    TITLE : "UI_BITACORA",
    IMAGE  : "",
    PRINTABLE : true,
    ENABLE : true,
    WIDTH  : "800px",
    HEIGHT : "300px",
    VIEW   : '../../Orden/UI/BitacoraOrden/BitacoraOrden.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/BitacoraOrden/BitacoraOrden.js'
                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'
                                                         ,'../../Orden/UI/ModuloOrdenesDirectivas.js'] } ],

};

BCS_VENTANA.BitacoraInstruccion = {
    TITLE : "UI_BITACORA",
    IMAGE  : "",
    PRINTABLE : true,
    ENABLE : true,
    WIDTH  : "800px",
    HEIGHT : "450px",
    VIEW   : '../../Orden/UI/BitacoraInstruccion/BitacoraInstruccion.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/BitacoraInstruccion/BitacoraInstruccion.js'
                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'
                                                         ,'../../Orden/UI/ModuloOrdenesDirectivas.js'] } ],

};

BCS_VENTANA.DetalleOrden = {
    TITLE : "UI_INFORMACION_ORDEN",
    IMAGE  : "",
    PRINTABLE : true,
    ENABLE : true,
    WIDTH  : "750px",
    HEIGHT : "500px",
    VIEW   : '../../Orden/UI/DetalleOrden/DetalleOrden.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/DetalleOrden/DetalleOrden.js'
                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'
                                                         ,'../../Orden/UI/ModuloOrdenesDirectivas.js'] } ],

};

BCS_VENTANA.DetalleAsignacion = {
    TITLE : "UI_INFORMACION_ASIGNACION",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "auto",
    VIEW   : '../../Orden/UI/DetalleAsignacion/DetalleAsignacion.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/DetalleAsignacion/DetalleAsignacion.js'
                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'
                                                         ,'../../Orden/UI/ModuloOrdenesDirectivas.js'] } ],

};

BCS_VENTANA.DetalleTransaccion = {
    TITLE : "UI_INFORMACION_TRANSACCION",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "490px",
    VIEW   : '../../Orden/UI/DetalleTransaccion/DetalleTransaccion.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/DetalleTransaccion/DetalleTransaccion.js'
                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'
                                                         ,'../../Orden/UI/ModuloOrdenesDirectivas.js'] } ],

};

BCS_VENTANA.DetalleRefacturacion = {
    TITLE : "UI_INFORMACION_ASIGNACION",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "450px",
    VIEW   : '../../Orden/UI/InterfazAsignacion/DetalleRefacturacion.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/InterfazAsignacion/DetalleRefacturacion.js'
                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'
                                                         ,'../../Orden/UI/ModuloOrdenesDirectivas.js'] } ],

};

BCS_VENTANA.ConsultarTasas = {
    TITLE : "UI_TASAS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "500px",
    HEIGHT : "250px",
    VIEW   : '../../Orden/UI/CrearOrdenSimultanea/ConsultarTasas.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/CrearOrdenSimultanea/ConsultarTasas.js'
                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

};

BCS_VENTANA.AdministrarServTransacciones = {
    TITLE : "UI_OPERADORES_RUEDA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1000px",
    HEIGHT : "590px",
    VIEW   : 'TRA/AdministrarServTransacciones.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['TRA/AdministrarServTransacciones.js'] } ],

};


BCS_VENTANA.ModalOperador = {
    TITLE : "UI_OPERADOR",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "700px",
    HEIGHT : "270px",
    VIEW   : 'TRA/ModalOperador.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['TRA/ModalOperador.js'] } ],

};

BCS_VENTANA.ModalHomologOperador = {
	    TITLE : "UI_HOMOLOGACION_OPERADOR",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "300px",
	    HEIGHT : "220px",
	    VIEW   : 'TRA/ModalHomologOperador.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['TRA/ModalHomologOperador.js'] } ],

};

BCS_VENTANA.ConsultarCorredorCorresponsal = {
    TITLE : "UI_CORREDORES_CORRESPONSALES",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "700px",
    HEIGHT : "315px",
    VIEW   : '../../Orden/UI/ConsultarCorredorCorresponsal/ConsultarCorredorCorresponsal.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/ConsultarCorredorCorresponsal/ConsultarCorredorCorresponsal.js'] } ],

};


BCS_VENTANA.CrearCorredorCorresponsal = {
    TITLE : "UI_INGRESAR_CORREDOR_CORRESPONSAL",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "400px",
    HEIGHT : "250px",
    VIEW   : '../../Orden/UI/CrearCorredorCorresponsal/CrearCorredorCorresponsal.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/CrearCorredorCorresponsal/CrearCorredorCorresponsal.js'] } ],

};

BCS_VENTANA.ModificarCorredorCorresponsal = {
    TITLE : "UI_MODIFICAR_CORREDOR_CORRESPONSAL",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "400px",
    HEIGHT : "250px",
    VIEW   : '../../Orden/UI/ModificarCorredorCorresponsal/ModificarCorredorCorresponsal.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/ModificarCorredorCorresponsal/ModificarCorredorCorresponsal.js'] } ],

};

BCS_VENTANA.ModificarTransaccion = {
    TITLE : "UI_MODIFICAR_TRANSACCION",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "650px",
    HEIGHT : "500px",
    VIEW   : '../../Orden/UI/ModificarTransaccion/ModificarTransaccion.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Orden/UI/ModificarTransaccion/ModificarTransaccion.js' 
                                                          ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

};

BCS_VENTANA.ConsultarGruposPortafolio = {
	    TITLE : "UI_GRUPOS_DE_PORTAFOLIOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "790px",
	    HEIGHT : "515px",
	    VIEW   : '../../Orden/UI/GruposDePortafolio/consultarGruposPortafolio.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Orden/UI/GruposDePortafolio/consultarGruposPortafolio.js'
	                                                          ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

};


BCS_VENTANA.CrearGrupoPortafolio = {
	    TITLE : "UI_NUEVO_GRUPO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "200px",
	    VIEW   : '../../Orden/UI/GruposDePortafolio/crearGrupoPortafolio.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Orden/UI/GruposDePortafolio/crearGrupoPortafolio.js'
	                                                          ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

};

BCS_VENTANA.ModificarGrupoPortafolio = {
	    TITLE : "UI_EDITAR_GRUPO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "700px",
	    HEIGHT : "500px",
	    VIEW   : '../../Orden/UI/GruposDePortafolio/modificarGrupoPortafolio.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Orden/UI/GruposDePortafolio/modificarGrupoPortafolio.js'
	                                                          ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

};

BCS_VENTANA.MaestroLibroOrdenes = {
    TITLE : "UI_LIBROS_DE_ORDENES",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1000px",
    HEIGHT : "600px",
    VIEW   : '../../Orden/UI/MaestroLibroOrdenes/MaestroLibroOrdenes.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Orden/UI/MaestroLibroOrdenes/MaestroLibroOrdenes.js','../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

};


BCS_VENTANA.agregarModificarLibroOrdenes = {
    TITLE : "UI_AGREGAR_MODIFICAR_LIBRO_ORDENES",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "550px",
    HEIGHT : "190px",
    VIEW   : '../../Orden/UI/MaestroLibroOrdenes/ModalLibroOrdenes.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Orden/UI/MaestroLibroOrdenes/ModalLibroOrdenes.js']}],

};

BCS_VENTANA.libroPorDefecto = {
    TITLE : "UI_LIBRO_POR_DEFECTO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "400px",
    HEIGHT : "150px",
    VIEW   : '../../Orden/UI/MaestroLibroOrdenes/ModalLibroPorDefecto.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Orden/UI/MaestroLibroOrdenes/ModalLibroPorDefecto.js']}],

};

BCS_VENTANA.agregarLibroOrdenesOperadores = {
    TITLE : "UI_AGREGAR_LIBRO_ORDENES_OPERADORES",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "500px",
    HEIGHT : "200px",
    VIEW   : '../../Orden/UI/MaestroLibroOrdenes/ModalLibroOrdenesOperadores.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Orden/UI/MaestroLibroOrdenes/ModalLibroOrdenesOperadores.js']}],

};

BCS_VENTANA.agregarLibroOrdenesSegmento = {
    TITLE : "UI_AGREGAR_LIBRO_ORDENES_SEGMENTO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "500px",
    HEIGHT : "200px",
    VIEW   : '../../Orden/UI/MaestroLibroOrdenes/ModalLibroOrdenesSegmento.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Orden/UI/MaestroLibroOrdenes/ModalLibroOrdenesSegmento.js']}],

};

BCS_VENTANA.agregarLibroOrdenesMarcasNegociacion = {
    TITLE : "UI_AGREGAR_LIBRO_ORDENES_MARCAS_NEGOCIACION",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "500px",
    HEIGHT : "200px",
    VIEW   : '../../Orden/UI/MaestroLibroOrdenes/ModalLibroOrdenesMarcasNegociacion.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Orden/UI/MaestroLibroOrdenes/ModalLibroOrdenesMarcasNegociacion.js']}],

};

BCS_VENTANA.DesbloquearTransaccionesSistema = {
	    TITLE : "UI_DESBLOQUEAR_OPERACIONES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "965px",
	    HEIGHT : "635px", 
	    VIEW   : '../../Orden/UI/DesbloquearTransacciones/blotterDesbloqueo.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Orden/UI/DesbloquearTransacciones/blotterDesbloqueo.js',
	                                                        '../../Orden/UI/DesbloquearTransacciones/desbloquearOrdenes.js',
	                                                        '../../Orden/UI/DesbloquearTransacciones/desbloquearTransacciones.js',
	                                                        '../../Orden/UI/ModuloOrdenesConfiguracionGrids.js',
	                                                        '../../Orden/UI/barraFiltro/barraFiltros.js']}],

};


BCS_VENTANA.VencimientoOrdenModal = {
	    TITLE : "UI_VENCER_ORDENES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "255px",
	    VIEW   : '../../Orden/UI/CuadraturaCierreDiario/VencimientoModal.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Orden/UI/CuadraturaCierreDiario/VencimientoModal.js'] }], 

};

/**
BCS_VENTANA.OficializarOrdenModal = {
	    TITLE : "UI_OFICIALIZAR_ORDENES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "260px",
	    VIEW   : '../../Orden/UI/CuadraturaCierreDiario/OficializacionModal.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Orden/UI/CuadraturaCierreDiario/OficializacionModal.js'] }], 

};
**/

BCS_VENTANA.OficializacionOrdenes = {
	    TITLE : "UI_OFICIALIZAR_ORDENES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "128px",
	    VIEW   : '../../Orden/UI/OficializacionOrdenes/OficializacionOrdenes.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Orden/UI/OficializacionOrdenes/OficializacionOrdenes.js'] }],
};


BCS_VENTANA.GestionFix = {
    TITLE : "UI_GESTION_FIX",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1000px",
    HEIGHT : "700px",
    VIEW   : '../../Orden/UI/GestionFix/GestionFix.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Orden/UI/GestionFix/GestionFix.js',
                                                          ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

};
BCS_VENTANA.ConfiguracionAsignacionDirecta = {
    TITLE : "UI_ASIGNACION_DIRECTA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "700px",
    HEIGHT : "550px",
    VIEW   : '../../Orden/UI/ConfiguracionAsignacionDirecta/ConfiguracionAsignacionDirecta.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Orden/UI/ConfiguracionAsignacionDirecta/ConfiguracionAsignacionDirecta.js'] }],
};
BCS_VENTANA.ShortName = {
    TITLE : "UI_SHORTNAME",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1000px",
    HEIGHT : "590px",
    VIEW   : '../../Orden/UI/Shortname/ShortName.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Orden/UI/Shortname/ShortName.js'] }],
};
BCS_VENTANA.ModalShortName = {
    TITLE : "UI_NUEVO_SHORTNAME",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "700px",
    HEIGHT : "270px",
    VIEW   : '../../Orden/UI/Shortname/ModalShortName.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Orden/UI/Shortname/ModalShortName.js'] } ],
};

//---------------------------------------------
//FFM
//---------------------------------------------
BCS_VENTANA.MODULO_FFM = {
	TITLE  : "UI_FFM",
	IMAGE  : "sapUiIcon business-objects-experience",
	ENABLE : true,
	WIDTH  : "0px",
	HEIGHT : "0px",
	VIEW   : '',
	PARAMS : {},
	DEPENDENCIES : [],

};


BCS_VENTANA.BlotterFFM = {
	TITLE : "UI_BLOTTER_FFM",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "1100px",
	HEIGHT : "652px",
	VIEW   : '../../FondoMutuo/UI/BlotterFondosMutuos/BlotterFondosMutuos.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/BlotterFondosMutuos/BlotterFondosMutuos.js'
	                                                   ,'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] }],

};






BCS_VENTANA.ExportarBatchFFM = {
		TITLE : "UI_EXPORTACION_MASIVA_BATCH",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "400px",
		HEIGHT : "160px",
		VIEW   : '../../FondoMutuo/UI/BlotterFondosMutuos/ExportarBatchFFM.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/BlotterFondosMutuos/ExportarBatchFFM.js'] }],

	};



BCS_VENTANA.IngresarFFM = {
  TITLE : "UI_INGRESAR_FFM",
  IMAGE  : "",
  ENABLE : true,
  WIDTH  : "500px",
  HEIGHT : "400px",
  VIEW   : '../../FondoMutuo/UI/IngresarAporteRescate/IngresarAporteRescateMain.html',
  PARAMS : {},
  DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/IngresarAporteRescate/IngresarAporteRescateMain.js',
                                                      '../../FondoMutuo/UI/IngresarAporteRescate/IngresarAporteNormal.js',
                                                      '../../FondoMutuo/UI/IngresarAporteRescate/IngresarAporteOvernight.js',
                                                      '../../FondoMutuo/UI/IngresarAporteRescate/IngresarRescateNormal.js',
                                                      '../../FondoMutuo/UI/IngresarAporteRescate/IngresarAporteConRescateProgramado.js',
                                                      '../../FondoMutuo/UI/IngresarAporteRescate/IngresarRescateReinversion1N.js',
                                                      '../../FondoMutuo/UI/IngresarAporteRescate/IngresarRescateReinversion.js',
                                                      '../../FondoMutuo/UI/IngresarAporteRescate/IngresarAporteReinversion.js',
                                                      '../../FondoMutuo/UI/IngresarAporteRescate/IngresarRescateTraspaso.js',
                                                      '../../FondoMutuo/UI/IngresarAporteRescate/IngresarRescateTraspasoPortafolio.js',
                                                      '../../FondoMutuo/UI/IngresarAporteRescate/IngresarRescateTransferencia.js',
                                                      '../../FondoMutuo/UI/IngresarAporteRescate/IngresarRescateCambioSerie.js',
                                                      '../../FondoMutuo/UI/IngresarAporteRescate/IngresarTitulo.js',
                                                      '../../FondoMutuo/UI/IngresarAporteRescate/RetirarTitulo.js',
                                                      '../../FondoMutuo/UI/IngresarAporteRescate/IngresarGarantia.js',
                                                      '../../FondoMutuo/UI/IngresarAporteRescate/RetirarGarantia.js',
													  '../../FondoMutuo/UI/IngresarAporteRescate/IngresarAporteDividendoCFI.js',
                                                      '../../Mesa/UI/ModuloMesaRFConfGrids.js'] } ],

};



BCS_VENTANA.BlotterCambioSerieFFM= {
	    TITLE : "UI_CAMBIO_SERIE_MASIVO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "780px",
	    VIEW   : '../../FondoMutuo/UI/BlotterCambioSerie/BlotterCambioSerie.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../FondoMutuo/UI/BlotterCambioSerie/BlotterCambioSerie.js'] }],

};

BCS_VENTANA.BlotterTraspasosFFM= {
	    TITLE : "UI_GESTION_DE_TRASPASOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
	    HEIGHT : "700px",
	    VIEW   : '../../FondoMutuo/UI/BlotterTraspasos/BlotterTraspasos.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../FondoMutuo/UI/BlotterTraspasos/BlotterTraspasos.js',
	    												   '../../FondoMutuo/UI/BlotterTraspasos/PanelEgresos.js',
	    												   '../../FondoMutuo/UI/BlotterTraspasos/PanelIngresos.js',
	    												   '../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js',
	    												   '../../Tesoreria/UI/BlotterComprobantes/BlotterComprobantesGrid.js'] }],

};

BCS_VENTANA.GenerarCertificado31= {
	    TITLE : "UI_GENERAR_CERTIFICADO_31",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "550px",
	    HEIGHT : "160px",
	    VIEW   : '../../FondoMutuo/UI/BlotterTraspasos/GenerarCertificado31.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../FondoMutuo/UI/BlotterTraspasos/GenerarCertificado31.js'] }],

};

BCS_VENTANA.IngresoTraspasosFFM= {
	    TITLE : "UI_INGRESO_DE_TRASPASO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "650px",
	    VIEW   : '../../FondoMutuo/UI/BlotterTraspasos/Traspaso/Traspaso.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../FondoMutuo/UI/BlotterTraspasos/Traspaso/Traspaso.js',
	    												   '../../FondoMutuo/UI/BlotterTraspasos/Traspaso/Paneles/PanelEgreso.js',
	    												   '../../FondoMutuo/UI/BlotterTraspasos/Traspaso/Paneles/PanelIngreso.js',
	    												   '../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] }],

};

BCS_VENTANA.AnularTraspasoFMM = {
	    TITLE : "UI_ANULACION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "320px",
	    HEIGHT : "270px",
	    VIEW   : '../../FondoMutuo/UI/BlotterTraspasos/AnulacionTraspasoFM/AnulacionTraspasoFM.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../FondoMutuo/UI/BlotterTraspasos/AnulacionTraspasoFM/AnulacionTraspasoFM.js',
	                                                         '../../FondoMutuo/UI/BlotterTraspasos/AnulacionTraspasoFM/Paneles/PanelPrincipal.js'] } ],

};

BCS_VENTANA.IngresoDepositoTraspasoFMM = {
	    TITLE : "UI_INGRESO_DEPOSITO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "600px",//900
	    HEIGHT : "335px",
	    VIEW   : '../../FondoMutuo/UI/BlotterTraspasos/IngresoDeposito/IngresoDeposito.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../FondoMutuo/UI/BlotterTraspasos/IngresoDeposito/IngresoDeposito.js'] } ],
};


BCS_VENTANA.IngresarInstrumentoFMM = {
	    TITLE : "UI_INGRESAR_INSTRUMENTO_FFM",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
	    HEIGHT : "800px",
	    VIEW   : '../../FondoMutuo/UI/IngresarInstrumento/IngresarInstrumento.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/IngresarInstrumento/IngresarInstrumento.js'] } ],

};

BCS_VENTANA.busquedaFondos = {
  TITLE : "UI_BUSQUEDA_FONDO",
  IMAGE  : "",
  ENABLE : true,
  WIDTH  : "1100px",
  HEIGHT : "443px",
  VIEW   : '../../FondoMutuo/UI/bcs-input-fondos/busquedaFondos.html',
  PARAMS : {},
  DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/bcs-input-fondos/busquedaFondos.js'] } ],

};

BCS_VENTANA.DetalleSolicitudAporteRescate = {
	    TITLE : "UI_DETALLE_SOLICITUD_APORTE_RESCATE",
	    IMAGE  : "",
	    ENABLE : true,
	    PRINTABLE: true,
	    WIDTH  : "900px",
	    HEIGHT : "450px",
	    VIEW   : '../../FondoMutuo/UI/DetalleSolicitudAporteRescate/DetalleSolicitudAporteRescate.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/DetalleSolicitudAporteRescate/DetalleSolicitudAporteRescate.js'
	                                                        ,'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] } ],

};

BCS_VENTANA.VentanaMensajeResultado = {
	    TITLE : "NO_TITLE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "200px",
	    VIEW   : 'PLA/VentanaMensajeResultado/VentanaMensajeResultado.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ 'PLA/VentanaMensajeResultado/VentanaMensajeResultado.js'] } ],

};

BCS_VENTANA.MantenedorInstrumentoFMM = {
		TITLE : "UI_MANTENEDOR_INSTRUMENTO_FFMM",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "1100px",
		HEIGHT : "650px",
		VIEW   : '../../FondoMutuo/UI/MantenedorFondos/MantenedorFondos.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/MantenedorFondos/MantenedorFondos.js'
		                                                   ,'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] }],

};

BCS_VENTANA.CarteraInversion = {
		TITLE : "UI_CARTERAS_INVERSION",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "600px",
		HEIGHT : "400px",
		VIEW   : '../../Plataforma/UI/INS/CarteraInversion/CarteraInversion.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Plataforma/UI/INS/CarteraInversion/CarteraInversion.js'] }],

};

BCS_VENTANA.CarteraEjecutivo = {
		TITLE : "UI_CARTERAS_EJECUTIVO",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "600px",
		HEIGHT : "auto",
		VIEW   : '../../SocioNegocio/UI/EJE/CarteraEjecutivo/CarteraEjecutivo.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/EJE/CarteraEjecutivo/CarteraEjecutivo.js'] }],

};

BCS_VENTANA.MantenedorValorCuota = {
		TITLE : "UI_MANTENEDOR_VALOR_CUOTA_FFM",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "1100px",
		HEIGHT : "650px",
		VIEW   : '../../FondoMutuo/UI/MantenedorValorCuota/MantenedorValorCuota.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/MantenedorValorCuota/MantenedorValorCuota.js'
		                                                   ,'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] }],

};

BCS_VENTANA.EditarParidadValorCuota = {
		TITLE : "UI_EDITAR_PARIDAD_VALOR_CUOTA_FFM",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "400px",
		HEIGHT : "150px",
		VIEW   : '../../FondoMutuo/UI/MantenedorValorCuota/EditarParidad/EditarParidad.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/MantenedorValorCuota/EditarParidad/EditarParidad.js']}],

};

BCS_VENTANA.AdvertenciaParidad = {
		TITLE : "UI_ADVERTENCIA_PARIDAD_VALOR_CUOTA_FFM",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "570px",
		HEIGHT : "320px",
		VIEW   : '../../FondoMutuo/UI/MantenedorValorCuota/AdvertenciaParidad/AdvertenciaParidad.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/MantenedorValorCuota/AdvertenciaParidad/AdvertenciaParidad.js']}],

};

BCS_VENTANA.MantenedorAperturaCierreFFM = {
		TITLE : "UI_MANTENEDOR_APERTURA_CIERRE_FFMM",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "1100px",
		HEIGHT : "650px",
		VIEW   : '../../FondoMutuo/UI/MantenedorAperturaCierreFFM/MantenedorAperturaCierreFFM.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/MantenedorAperturaCierreFFM/MantenedorAperturaCierreFFM.js'
		                                                   ,'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] }],

};


BCS_VENTANA.MantenedorHorarioCierreFFM = {
		TITLE : "UI_MANTENEDOR_HORARIO_CIERRE_FFM",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "700px",
		HEIGHT : "700px",
		VIEW   : '../../FondoMutuo/UI/MantenedorAperturaCierreFFM/ConfiguracionHorarioCierreFFM.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/MantenedorAperturaCierreFFM/ConfiguracionHorarioCierreFFM.js'] }],

};

BCS_VENTANA.MantenedorAperturaCierreBloqueoFFM = {
		TITLE : "UI_MANTENEDOR_APERTURA_CIERRE_BLOQUEO_FFM",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "800px",
		HEIGHT : "400px",
		VIEW   : '../../FondoMutuo/UI/MantenedorAperturaCierreFFM/ConfiguracionAperturaCierreBloqueo.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/MantenedorAperturaCierreFFM/ConfiguracionAperturaCierreBloqueo.js'] }],

};

BCS_VENTANA.IngresarHorarioCierreFFM = {
		TITLE : "UI_INGRESAR_HORARIO_CIERRE_FFM",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "400px",
		HEIGHT : "300px",
		VIEW   : '../../FondoMutuo/UI/MantenedorAperturaCierreFFM/IngresarHorarioCierreFondo.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/MantenedorAperturaCierreFFM/IngresarHorarioCierreFondo.js'] }],

};

BCS_VENTANA.NCG365TransaccionesFFMM= {
	    TITLE : "UI_NCG365_INFORME_DE_TRANSACCIONES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "450px",
	    HEIGHT : "170px",
	    VIEW   : '../../FondoMutuo/UI/NCG365TransaccionesFFMM/NCG365TransaccionesFFMM.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../FondoMutuo/UI/NCG365TransaccionesFFMM/NCG365TransaccionesFFMM.js'] }],
};

BCS_VENTANA.InformeRentabilidadFFMMExtranjero= {
	TITLE : "UI_INFORME_RENTABILIDAD_FFMM_EXTRANJEROS",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "450px",
	HEIGHT : "182px",
	VIEW   : '../../FondoMutuo/UI/InformeRentabilidadFFMMExtranjero/InformeRentabilidadFFMMExtranjero.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js", cache: true, files: ['../../FondoMutuo/UI/InformeRentabilidadFFMMExtranjero/InformeRentabilidadFFMMExtranjero.js'] }],
};

BCS_VENTANA.ConsultaValorCuotaHistorico = {
		TITLE : "UI_CONSULTA_VALOR_CUOTA_HISTORICO",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "1100px",
		HEIGHT : "650px",
		VIEW   : '../../FondoMutuo/UI/ConsultaValorCuotaHistorico/ConsultaValorCuotaHistorico.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/ConsultaValorCuotaHistorico/ConsultaValorCuotaHistorico.js'
		                                                   ,'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] }],

};

BCS_VENTANA.ModificarComisionesFFM = {
		TITLE : "UI_MODIFICAR_COMISIONES_FFM",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "600px",
		HEIGHT : "450px",
		VIEW   : '../../FondoMutuo/UI/MantenedorComisiones/ModificarComisiones.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: false, files: ['../../FondoMutuo/UI/MantenedorComisiones/ModificarComisiones.js'] }],

};

BCS_VENTANA.CertificarFFM = {
		TITLE : "UI_CERTIFICACION_FFM",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "1100px",
		HEIGHT : "630px",
		VIEW   : '../../FondoMutuo/UI/CertificarAporteRescate/CertificarAporteRescate.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/CertificarAporteRescate/CertificarAporteRescate.js'
		                                                   ,'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] }],

};

BCS_VENTANA.LogCertificarFFM = {
		TITLE : "UI_LOG_CERTIFICACION_FFM",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "1000px",
		HEIGHT : "530px",
		VIEW   : '../../FondoMutuo/UI/CertificarAporteRescate/LogCertificarAporteRescate.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/CertificarAporteRescate/LogCertificarAporteRescate.js'
		                                                   ,'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] }],

};

BCS_VENTANA.PagarAGF = {
		TITLE : "UI_PAGAR_AGF_FFM",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "1100px",
		HEIGHT : "650px",
		VIEW   : '../../FondoMutuo/UI/CertificarAporteRescate/PagarAGF.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/CertificarAporteRescate/PagarAGF.js'
		                                                   ,'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] }],

};

BCS_VENTANA.BlotterFondosMutuosModificarFechas = {
		TITLE : "UI_MODIFICAR_FECHAS",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "1000px",
		HEIGHT : "145px",
		VIEW   : '../../FondoMutuo/UI/BlotterFondosMutuos/BlotterFondosMutuosModificarFechas.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/BlotterFondosMutuos/BlotterFondosMutuosModificarFechas.js'
		                                                   ,'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] }],

};

BCS_VENTANA.BlotterFondosMutuosHistorialCambiosModificarFechas = {
		TITLE : "UI_HISTORIAL_CAMBIOS_MODIFICAR_FECHAS",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "1100px",
		HEIGHT : "522px",
		VIEW   : '../../FondoMutuo/UI/BlotterFondosMutuos/BlotterFondosMutuosHistorialCambiosModificarFechas.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/BlotterFondosMutuos/BlotterFondosMutuosHistorialCambiosModificarFechas.js'
		                                                   ,'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] }],

};

BCS_VENTANA.ConfirmarCertificarFFM = {
		TITLE : "UI_CONFIRMACION_CERTIFICACION_FFM",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "1000px",
		HEIGHT : "500px",
		VIEW   : '../../FondoMutuo/UI/CertificarAporteRescate/ConfirmacionCertificarAporteRescate.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/CertificarAporteRescate/ConfirmacionCertificarAporteRescate.js'
		                                                   ,'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] }],

};

BCS_VENTANA.MantenedorFamiliasFFM = {
		TITLE : "UI_MANTENEDOR_FAMILIA_FFM",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "800px",
		HEIGHT : "600px",
		VIEW   : '../../FondoMutuo/UI/MantenedorFamilia/MantenedorFamilia.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/MantenedorFamilia/MantenedorFamilia.js'
		                                                   ,'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] }],

};

BCS_VENTANA.IngresarFamiliaFFM = {
		TITLE : "UI_INGRESAR_FAMILIA_FFM",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "700px",
		HEIGHT : "400px",
		VIEW   : '../../FondoMutuo/UI/MantenedorFamilia/IngresarFamilia.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/MantenedorFamilia/IngresarFamilia.js'] }],

};

BCS_VENTANA.AgregarModificarFondoIndustria = {
		TITLE : "UI_AGREGAR_MODIFICAR_FONDOS_INDUSTRIA",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "450px",
		HEIGHT : "250px",
		VIEW   : '../../FondoMutuo/UI/MantenedorFondosIndustria/AgregarModificarFondoIndustria.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/MantenedorFondosIndustria/AgregarModificarFondoIndustria.js'] }],

};

BCS_VENTANA.ConsultaComisionesNN = {
		TITLE : "UI_COMISIONES_NN",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "1100px",
		HEIGHT : "625px",
		VIEW   : '../../FondoMutuo/UI/ConsultaComisionesNN/ConsultaComisionesNN.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/ConsultaComisionesNN/ConsultaComisionesNN.js'
														   ,'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] }],

};

BCS_VENTANA.ConfirmarProcesarNNFFM = {
		TITLE : "UI_RESULTADO_PROCESAR_NN_FFMM",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "1000px",
		HEIGHT : "700px",
		VIEW   : '../../FondoMutuo/UI/ConsultaComisionesNN/ConfirmarComisionesNN.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/ConsultaComisionesNN/ConfirmarComisionesNN.js'
														   ,'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] }],

};

BCS_VENTANA.MantenedorFondosIndustria = {
		TITLE : "UI_MANTENEDOR_FONDOS_INDUSTRIA",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "600px",
		HEIGHT : "400px",
		VIEW   : '../../FondoMutuo/UI/MantenedorFondosIndustria/MantenedorFondosIndustria.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/MantenedorFondosIndustria/MantenedorFondosIndustria.js'
														   ,'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] }],

};
BCS_VENTANA.MantenedorPerfilesFondoMutuo = {
		TITLE : "UI_MANTENEDOR_PERFILES_FFM",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "700px",
		HEIGHT : "400px",
		VIEW   : '../../FondoMutuo/UI/MantenedorPerfilFondoMutuo/MantenedorPerfilFondoMutuo.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/MantenedorPerfilFondoMutuo/MantenedorPerfilFondoMutuo.js'] }],

};
BCS_VENTANA.ConsultaCustodiaFondosMutuos = {
		TITLE : "UI_CONSULTA_CUSTODIA_FONDOS_MUTUOS",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "1100px",
		HEIGHT : "600px",
		VIEW   : '../../FondoMutuo/UI/ConsultaCustodiaFondosMutuos/ConsultaCustodiaFondosMutuos.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/ConsultaCustodiaFondosMutuos/ConsultaCustodiaFondosMutuos.js'
		                                                   ,'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] }],

};


BCS_VENTANA.ConsultaValorTipoCambioHistorico = {
		TITLE : "UI_CONSULTA_TIPO_CAMBIO_HISTORICO",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "1100px",
		HEIGHT : "650px",
		VIEW   : '../../FondoMutuo/UI/ConsultaValorTipoCambioHistorico/ConsultaValorTipoCambioHistorico.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/ConsultaValorTipoCambioHistorico/ConsultaValorTipoCambioHistorico.js'
		                                                   ,'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] }],

};

BCS_VENTANA.MantenedorValorTipoCambio = {
		TITLE : "UI_MANTENEDOR_TIPO_CAMBIO_FFM",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "1100px",
		HEIGHT : "650px",
		VIEW   : '../../FondoMutuo/UI/MantenedorValorTipoCambio/MantenedorValorTipoCambio.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/MantenedorValorTipoCambio/MantenedorValorTipoCambio.js'
		                                                   ,'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] }],

};

BCS_VENTANA.CargaOperacionesFondosMutuos = {
		TITLE : "UI_CARGA_OPERACIONES_FONDOS_MUTUOS",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "900px",
		HEIGHT : "600px",
		VIEW   : '../../FondoMutuo/UI/BlotterCargaOperacionesFondosMutuos/BlotterCargaOperacionesFondosMutuos.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/BlotterCargaOperacionesFondosMutuos/BlotterCargaOperacionesFondosMutuos.js'
		                                                   ,'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] }],

};


BCS_VENTANA.ResumenOperacionesFondosMutuos = {
		TITLE : "UI_RESUMEN_OPERACIONES_FONDOS_MUTUOS",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "700px",
		HEIGHT : "400px",
		VIEW   : '../../FondoMutuo/UI/BlotterCargaOperacionesFondosMutuos/ResumenCargaOperacionesFondosMutuos.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/BlotterCargaOperacionesFondosMutuos/ResumenCargaOperacionesFondosMutuos.js'
		                                                   ,'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] }],

};

BCS_VENTANA.ResumenCartaLiquidacion = {
		TITLE : "UI_RESUMEN_CARTA_LIQUIDACION_FONDOS_MUTUOS",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "700px",
		HEIGHT : "450px",
		VIEW   : '../../FondoMutuo/UI/CartaLiquidacion/ResumenCartaLiquidacion.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/CartaLiquidacion/ResumenCartaLiquidacion.js'
		                                                   ,'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] }],

};

BCS_VENTANA.CargaOperacionesAgfFondosMutuos = {
		TITLE : "UI_CARGA_INTERFAZ_AGF_OPERACIONES_FONDOS_MUTUOS",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "1100px",
		HEIGHT : "600px",
		VIEW   : '../../FondoMutuo/UI/ConsultarInterfazSolicitudAgf/ConsultarInterfazSolicitudAgf.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/ConsultarInterfazSolicitudAgf/ConsultarInterfazSolicitudAgf.js'
		                                                   ,'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] }],

};
BCS_VENTANA.MantenedorAdministracionLiquidez = {
		TITLE : "UI_MANTENEDOR_ADMINISTRACION_LIQUIDEZ",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "600px",
		HEIGHT : "200px",
		VIEW   : '../../FondoMutuo/UI/MantenedorAdministracionLiquidez/MantenedorAdministracionLiquidez.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/MantenedorAdministracionLiquidez/MantenedorAdministracionLiquidez.js'
		                                                   ,'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] }],

};
BCS_VENTANA.ModificarConfiguracionAdministracionLiquidezGenericoFondoMutuo = {
		TITLE : "UI_MANTENEDOR_CONFIGURACION_ADMINISTRACION_LIQUIDEZ_GENERICO",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "1100px",
		HEIGHT : "600px",
		VIEW   : '../../FondoMutuo/UI/MantenedorAdministracionLiquidez/ModificarFondoAdministracionLiquidacionGenerico.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/MantenedorAdministracionLiquidez/ModificarFondoAdministracionLiquidacionGenerico.js'
		                                                   ,'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] }],

};
BCS_VENTANA.ModificarConfiguracionAdministracionLiquidezPortafolioFondoMutuo = {
		TITLE : "UI_MANTENEDOR_CONFIGURACION_ADMINISTRACION_LIQUIDEZ_PORTAFOLIO",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "1100px",
		HEIGHT : "600px",
		VIEW   : '../../FondoMutuo/UI/MantenedorAdministracionLiquidez/ModificarFondoAdministracionLiquidacionPortafolio.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/MantenedorAdministracionLiquidez/ModificarFondoAdministracionLiquidacionPortafolio.js'
		                                                   ,'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] }],

};
BCS_VENTANA.ConsultaAdministracionLiquidez = {
		TITLE : "UI_CONSULTA_ADMINISTRACION_LIQUIDEZ",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "1100px",
		HEIGHT : "600px",
		VIEW   : '../../FondoMutuo/UI/MantenedorAdministracionLiquidez/ConsultaFondoAdministracionLiquidacion.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/MantenedorAdministracionLiquidez/ConsultaFondoAdministracionLiquidacion.js'
		                                                   ,'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] }],

};
BCS_VENTANA.LogAdministracionLiquidacionFondoMutuo = {
		TITLE : "UI_LOG_ADMINISTRACION_LIQUIDEZ",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "1100px",
		HEIGHT : "600px",
		VIEW   : '../../FondoMutuo/UI/MantenedorAdministracionLiquidez/LogAdministracionLiquidez.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/MantenedorAdministracionLiquidez/LogAdministracionLiquidez.js'
		                                                   ,'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] }],

};
BCS_VENTANA.GenerarArchivoAGF = {
		TITLE : "UI_GENERAR_ARCHIVO_OPERACIONES_AGF",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "400px",
		HEIGHT : "220px",
		VIEW   : '../../FondoMutuo/UI/GenerarArchivoAGF/GenerarArchivoAGF.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/GenerarArchivoAGF/GenerarArchivoAGF.js'
		                                                   ,'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] }],

};

BCS_VENTANA.MantenedorReaperturaFondos = {
		TITLE : "UI_MATENEDOR_REAPERTURA_FONDOS",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "400px",
		HEIGHT : "150px",
		VIEW   : '../../FondoMutuo/UI/MantenedorAperturaCierreFFM/MantenedorReaperturaFondos.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/MantenedorAperturaCierreFFM/MantenedorReaperturaFondos.js'] }],

};

BCS_VENTANA.MantenedorRegimenFondoMutuo = {
		TITLE : "UI_MANTENEDOR_REGIMEN_FONDO_MUTUO",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "800px",
		HEIGHT : "220px",
		VIEW   : '../../FondoMutuo/UI/MantenedorRegimenes/MantenedorRegimenesFondoMutuo.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/MantenedorRegimenes/MantenedorRegimenesFondoMutuo.js'
		                                                    ,'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] }],
		

};

BCS_VENTANA.ModificarRegimenFondoMutuo = {
		TITLE : "UI_MANTENEDOR_REGIMEN_FONDO_MUTUO",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "400px",
		HEIGHT : "180px",
		VIEW   : '../../FondoMutuo/UI/MantenedorRegimenes/ModificarRegimenFondoMutuo.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/MantenedorRegimenes/ModificarRegimenFondoMutuo.js'] }],

};

BCS_VENTANA.GeneracionArchivosFFMM = {
	TITLE : "UI_GENERACION_ARCHIVOS_FFMM",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "500px",
	HEIGHT : "auto",
	VIEW   : '../../FondoMutuo/UI/GeneracionInformeFFMM/GeneracionArchivosMain.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js',
														'../../FondoMutuo/UI/GeneracionInformeFFMM/GeneracionArchivosMain.js'] }],
};







//---------------------------------------------
//CUENTA INVERSION
//---------------------------------------------
BCS_VENTANA.MODULO_CUENTA_INVERSION= {
    TITLE : "UI_CUENTA_INVERSION",
    IMAGE  : "sapUiIcon monitor-payments",
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "400px",
    VIEW   : '',
    PARAMS : {},
    DEPENDENCIES : [],

};


BCS_VENTANA.CrearCargoAbono = {
    TITLE : "UI_INGRESAR_CARGO_ABONO_MANUAL",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "500px",
    HEIGHT : "380px",
    VIEW   : '../../CuentaInversion/UI/CrearCargoAbono/CrearCargoAbono.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../CuentaInversion/UI/CrearCargoAbono/CrearCargoAbono.js'] } ],

};
//CELZORRO-264 - MBI Manejo de Activos Internacionales - Carga masiva Abonos y Cargos
BCS_VENTANA.CargaMasivaCuentaInversion = {
	    TITLE  : "UI_CARGA_MASIVA_CUENTA_INVERSION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "500px",    
	    VIEW   : '../../CuentaInversion/UI/BlotterCargaMasiva/BlotterCargaMasiva.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, 
	    	files: [ '../../CuentaInversion/UI/BlotterCargaMasiva/BlotterCargaMasiva.js',
	    	         '../../CuentaInversion/UI/BlotterCargaMasiva/BlotterCargaMasivaGrid.js'
	    	         ] 
		}],

};

BCS_VENTANA.DetalleCargaMasivaCtaInversion = {
	    TITLE  : "UI_DETALLE_CARGA_MASIVA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "500px",    
	    VIEW   : '../../CuentaInversion/UI/BlotterCargaMasiva/DetalleCargaMasiva/DetalleCargaMasiva.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, 
	    	files: [ '../../CuentaInversion/UI/BlotterCargaMasiva/DetalleCargaMasiva/DetalleCargaMasiva.js',
	    	         '../../CuentaInversion/UI/BlotterCargaMasiva/BlotterCargaMasivaGrid.js'
	    	         ] 
		}],
};


BCS_VENTANA.ComplementoCargaMasiva = {
	    TITLE  : "UI_COMPLEMENTO_CARGA_MASIVA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "380px",    
	    VIEW   : '../../CuentaInversion/UI/BlotterCargaMasiva/ComplementoCargaMasiva.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, 
	    	files: [ '../../CuentaInversion/UI/BlotterCargaMasiva/ComplementoCargaMasiva.js'] 
		}],
};

//CELZORRO-264 - MBI Manejo de Activos Internacionales - Carga masiva Abonos y Cargos


BCS_VENTANA.CartolaDeMovimientos = {
    TITLE : "UI_CARTOLA_MOVIMIENTOS_CUENTA_INVERSION",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "700px",
    VIEW   : '../../CuentaInversion/UI/CartolaDeMovimientos/CartolaDeMovimientos.html',
    PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../CuentaInversion/UI/CartolaDeMovimientos/CartolaDeMovimientos.js'] } ],

};


BCS_VENTANA.CuentaInversionCliente = {
    TITLE : "UI_CUENTA_INVERSION_CLIENTE",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "700px",
    VIEW   : '../../CuentaInversion/UI/CuentaInversion/CuentaInversionCliente/CuentaInversionCliente.html',
    PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../CuentaInversion/UI/CuentaInversion/CuentaInversionCliente/CuentaInversionCliente.js',
	                                                     '../../CuentaInversion/UI/CuentaInversion/CuentaInversionGrid.js',
	                                                     '../../CuentaInversion/UI/FormatoDocumentosCuentaInversion/FormatoDocumentosCuentaInversion.js'] } ],

};


BCS_VENTANA.FlujoCaja = {
    TITLE : "UI_FLUJO_CAJA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "800px",
    HEIGHT : "550px",
    VIEW   : '../../Plataforma/UI/PLA/ConsultarFlujoCaja/ConsultarFlujoCaja.html',
    PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Plataforma/UI/PLA/ConsultarFlujoCaja/ConsultarFlujoCaja.js'] } ],

};

BCS_VENTANA.CuentaInversionClienteModal = {
    TITLE : "UI_CUENTA_INVERSION_CLIENTE",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "700px",
    HEIGHT : "370px",
    VIEW   : '../../CuentaInversion/UI/CuentaInversion/CuentaInversionClienteModal/CuentaInversionClienteModal.html',
    PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../CuentaInversion/UI/CuentaInversion/CuentaInversionClienteModal/CuentaInversionClienteModal.js',
	                                                     '../../CuentaInversion/UI/CuentaInversion/CuentaInversionGrid.js'] } ],

};

BCS_VENTANA.LiquidarSaldo = {
	    TITLE : "UI_LIQUIDACION_DE_SALDOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "585px",
	    VIEW   : '../../CuentaInversion/UI/LiquidarSaldo/LiquidarSaldo.html',
	    PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../CuentaInversion/UI/LiquidarSaldo/LiquidarSaldo.js'] } ],
};

BCS_VENTANA.CuadraturaCuentaInversion = {
		TITLE : "UI_CUADRATURA_CUENTA_INVERSION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "200px",
	    VIEW   : '../../CuentaInversion/UI/CuadraturaCuentaInversion/CuadraturaCuentaInversion.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: [ '../../CuentaInversion/UI/CuadraturaCuentaInversion/CuadraturaCuentaInversion.js']}],
};

 
BCS_VENTANA.MantenedorCuentaInversion = {
	    TITLE : "UI_MANTENEDOR_CUENTA_INVERSION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "400px",
	    VIEW   : '../../CuentaInversion/UI/MantenedorCuentaInversion/MantenedorCuentaInversion.html',
	    PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../CuentaInversion/UI/MantenedorCuentaInversion/MantenedorCuentaInversion.js'] } ],

};


BCS_VENTANA.CuentaInversionConsultas = {
		SEPARATE: true,
	    TITLE : "UI_CONSULTAS",
	    ENABLE : true
};


BCS_VENTANA.CuentaInversionEjecutivo = {
    TITLE : "UI_CONSULTA_SALDO_MOVIMIENTOS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1100px",
    HEIGHT : "700px",
    VIEW   : '../../CuentaInversion/UI/CuentaInversion/CuentaInversionEjecutivo/CuentaInversionEjecutivo.html',
    PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../CuentaInversion/UI/CuentaInversion/CuentaInversionEjecutivo/CuentaInversionEjecutivo.js',
	                                                     '../../CuentaInversion/UI/CuentaInversion/CuentaInversionGrid.js',
	                                                     '../../CuentaInversion/UI/FormatoDocumentosCuentaInversion/FormatoDocumentosCuentaInversion.js'] } ],

};


BCS_VENTANA.CuentasPorPagarYCobrar = {
    TITLE : "UI_CUENTAS_COBRAR_CUENTAS_PAGAR",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "700px",
    VIEW   : '../../CuentaInversion/UI/Consultas/CuentasPorPagarYCobrar/CuentasPorPagarYCobrar.html',
    PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../CuentaInversion/UI/Consultas/CuentasPorPagarYCobrar/CuentasPorPagarYCobrar.js'] } ],

};


BCS_VENTANA.CuadraturaSaldosCuentaInversion = {
    TITLE : "UI_CONSULTA_CUADRATURA_SALDOS_CUENTAS_INVERSION",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "800px",
    HEIGHT : "430px",
    VIEW   : '../../CuentaInversion/UI/Consultas/CuadraturaSaldosCuentaInversion/CuadraturaSaldosCuentaInversion.html',
    PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../CuentaInversion/UI/Consultas/CuadraturaSaldosCuentaInversion/CuadraturaSaldosCuentaInversion.js'] } ],

};

BCS_VENTANA.ReporteClientesPorPagarYCobrar = {
	    TITLE : "UI_REPORTE_PAGAR_COBRAR",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "430px",
	    VIEW   : '../../CuentaInversion/UI/Consultas/ReporteClientesPorPagarYCobrar/ClientesPorPagarYCobrar.html',
	    PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../CuentaInversion/UI/Consultas/ReporteClientesPorPagarYCobrar/ClientesPorPagarYCobrar.js'] } ],

	};

BCS_VENTANA.ReporteCuentasPorCobrarAnexo16= {
	    TITLE : "UI_ANEXO16_LIBRO_DIARIO_DE_CUENTAS_POR_COBRAR",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "450px",
	    HEIGHT : "210px",
	    VIEW   : '../../CuentaInversion/UI/ReporteCuentasPorCobrarAnexo16/ReporteCuentasPorCobrarAnexo16.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../CuentaInversion/UI/ReporteCuentasPorCobrarAnexo16/ReporteCuentasPorCobrarAnexo16.js' ]} ],

};
//CELZORRO-272------------------------------------------------------------------------------------------------
BCS_VENTANA.AtributtionAnalisys= { 
	    TITLE : "UI_ATRIBUTION_ANALISIS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1200px",
	    HEIGHT : "500px",
	    VIEW   : '../../CuentaInversion/UI/AtributtionAnalysis/MaestroAtributtionAnalysis.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../CuentaInversion/UI/AtributtionAnalysis/MaestroAtributtionAnalysis.js'	    																								                                                               
                                                            ,'../../CuentaInversion/UI/AtributtionAnalysis/Indices.js'] } ],
};

BCS_VENTANA.MantenedorIndicesCorr= { 
	    TITLE : "UI_MANTENEDOR_INDICES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1200px",
	    HEIGHT : "500px",
	    VIEW   : '../../CuentaInversion/UI/AtributtionAnalysis/MantenedorIndices.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../CuentaInversion/UI/AtributtionAnalysis/MantenedorIndices.js'] } ],
};



BCS_VENTANA.CrearIndice = {
	    TITLE : "UI_CREAR",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "600px",
	    HEIGHT : "180px",
	    VIEW   :  '../../CuentaInversion/UI/AtributtionAnalysis/Crear/CrearIndice.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../CuentaInversion/UI/AtributtionAnalysis/Crear/CrearIndice.js'] }],
};

//CELZORRO-272------------------------------------------------------------------------------------------------

BCS_VENTANA.ConsultaAnexo7 = {
	    TITLE : "Consulta Anexo 7 - Saldos de Cliente",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "136px",
	    VIEW   : '../../CuentaInversion/UI/CuentaInversion/CuentaInversionEjecutivo/ConsultaAnexo7/ConsultaAnexo7.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../CuentaInversion/UI/CuentaInversion/CuentaInversionEjecutivo/ConsultaAnexo7/ConsultaAnexo7.js'] }],

};

//---------------------------------------------
//CUSTODIA
//---------------------------------------------
BCS_VENTANA.MODULO_CUSTODIA= {
    TITLE : "UI_CUSTODIA",
    IMAGE  : "sapUiIcon database",
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "400px",
    VIEW   : '',
    PARAMS : {},
    DEPENDENCIES : [],

};




BCS_VENTANA.IngresarTitulo= {
    TITLE : "UI_MENU_CUST_INGRESAR_RETIRAR_TITULO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "600px",
    HEIGHT : "650px",
    VIEW   : '../../Custodia/UI/IngresarTitulo/IngresarTitulo.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/IngresarTitulo/IngresarTitulo.js'] } ],
    AFECTOCIERRE : true,
	MODULO : "CUS",

};

BCS_VENTANA.IngresarTitulo7= {
	    TITLE : "UI7_INGRESAR_RETIRAR_TITULO7",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "600px",
	    HEIGHT : "650px",
	    VIEW   : '../../Custodia/UI/IngresarTitulo7/IngresarTitulo7.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/IngresarTitulo7/IngresarTitulo7.js'] } ],

	};


BCS_VENTANA.IngresarTituloTest= {
    TITLE : "Test Ingreso Titulo",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "600px",
    HEIGHT : "650px",
    VIEW   : '../../Custodia/UI/IngresarTitulo4/IngresarTitulo4.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/IngresarTitulo4/IngresarTitulo4.js'] } ],

};


BCS_VENTANA.ConsultarCarterasModal = {
    TITLE : "UI_MENU_CUST_CONSULTAR_CARTERAS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "874px",
    HEIGHT : "614px",
    VIEW   : '../../Custodia/UI/ConsultarCarterasModal/ConsultarCarterasModal.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/ConsultarCarterasModal/ConsultarCarterasModal.js'] } ],

};

BCS_VENTANA.ConsultarPrestamistasDisponibles = {
    TITLE : "UI_PRESTAMISTAS_DISPONIBLES",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "650px",
    HEIGHT : "350px",
    VIEW   : '../../Custodia/UI/PRT/ConsultarPrestamistasDisponibles/ConsultarPrestamistasDisponibles.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/PRT/ConsultarPrestamistasDisponibles/ConsultarPrestamistasDisponibles.js'] } ],

};

BCS_VENTANA.ConsultarPrestamistasDisponiblesSinCustodia = {
	    TITLE : "UI_PRESTAMISTAS_EXTERNOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "565px",
	    HEIGHT : "350px",
	    VIEW   : '../../Custodia/UI/PRT/ConsultarPrestamistasDisponibles/ConsultarPrestamistasDisponiblesSinCustodia.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/PRT/ConsultarPrestamistasDisponibles/ConsultarPrestamistasDisponiblesSinCustodia.js'] } ],

	};

BCS_VENTANA.MovimientoEnTransito= {
		TITLE : "UI_MENU_CUST_MOVIMIENTOS_EN_TRANSITO",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "1050px",
		HEIGHT : "720px",
		VIEW   : '../../Custodia/UI/BlotterMovimientosEnTransito/BlotterMovimientosEnTransito.html',
		PARAMS : {},
		DEPENDENCIES : ['../../Custodia/UI/BlotterMovimientosEnTransito/BlotterMovimientosEnTransito.js', '../../Custodia/UI/BlotterMovimientosEnTransito/BlotterMovimientosEnTransitoGrid.js', '../../Custodia/UI/ConsultarMovimientosEnTransitoAgrupados/ConsultarMovimientosEnTransitoAgrupados.js','../../Custodia/UI/ConsultarMovimientosEnTransito/ConsultarMovimientosEnTransito.js', '../../Custodia/UI/ConsultarMovimientosEnTransitoHistorico/ConsultarMovimientosEnTransitoHistorico.js'],
	
};

BCS_VENTANA.ActualizarPrecioMonedaInstrumento = {
    TITLE : "UI_ACTUALIZAR_PRECIO_MONEDA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "450px",
    HEIGHT : "250px",
    VIEW   : '../../Custodia/UI/ConsultarMovimientosEnTransito/ActualizarPrecioMonedaInstrumento.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [   '../../Custodia/UI/ConsultarMovimientosEnTransito/ActualizarPrecioMonedaInstrumento.js' ] } ],

};

BCS_VENTANA.ConsultarCarteras = {
    TITLE : "UI_MENU_CUST_CONSULTAR_CARTERAS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1050px",
    HEIGHT : "715px",
    VIEW   : '../../Custodia/UI/BlotterCarteras/BlotterCarteras.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/BlotterCarteras/BlotterCarteras.js', 
                                                         '../../Custodia/UI/BlotterCarteras/BlotterCarterasGrid.js', 
                                                         '../../Custodia/UI/ConsultarCarteras/ConsultarCarteras.js', 
                                                         '../../Custodia/UI/ConsultarCarterasPorInstrumento/ConsultarCarterasPorInstrumento.js',
                                                         '../../Custodia/UI/ConsultarCartolaMovimientos/ConsultarCartolaMovimientos.js',
                                                         '../../Custodia/UI/ConsultarCustodiaVencimientos/ConsultarCustodiaVencimientos.js',
                                                         '../../Custodia/UI/ConsultarCustodiaVencimientos/CustodiaSubyacenteModal.js'] } ],

};

BCS_VENTANA.InformeMayorYMenorValorCuentaPropia = {
	TITLE : "UI_INFORME_MAYOR_MENOR_VALOR",
	IMAGE  : "",
    ENABLE : true,
    WIDTH  : "450px",
    HEIGHT : "auto",
	VIEW   : '../../Custodia/UI/InformeMayorYMenorValorCuentaPropia/InformeMayorYMenorValorCuentaPropia.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/InformeMayorYMenorValorCuentaPropia/InformeMayorYMenorValorCuentaPropia.js'] } ],
};

BCS_VENTANA.InformeCarteraLeasing = {
	TITLE : "UI_INFORME_CARTERA_LEASING",
	IMAGE  : "",
    ENABLE : true,
    WIDTH  : "450px",
    HEIGHT : "auto",
	VIEW   : '../../Contabilidad/UI/Informes/InformeCarteraLeasing.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Contabilidad/UI/Informes/InformeCarteraLeasing.js'] } ], 
};

BCS_VENTANA.InformeComprasVentasTermino = {
	TITLE : "UI_INFORME_COMPRAS_VENTAS_TERMINO",
	IMAGE  : "",
    ENABLE : true,
    WIDTH  : "450px",
    HEIGHT : "auto",
	VIEW   : '../../Contabilidad/UI/Informes/InformeComprasVentasTermino.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Contabilidad/UI/Informes/InformeComprasVentasTermino.js'] } ],
};



BCS_VENTANA.ConsultarCustodia = {
    TITLE : "UI_MENU_CUST_CONSULTAR_CUSTODIA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1125px",
    HEIGHT : "745px",
    VIEW   : '../../Custodia/UI/BlotterCustodia/BlotterCustodia.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/BlotterCustodia/BlotterCustodia.js', 
                                                         '../../Custodia/UI/BlotterCustodia/BlotterCustodiaGrid.js', 
                                                         '../../Custodia/UI/ConsultarCustodiaPortafolio/ConsultarCustodiaPortafolio.js',                                                         
                                                         '../../Custodia/UI/ConsultarCustodiaGeneral/ConsultarCustodiaGeneral.js',
                                                         '../../Custodia/UI/ConsultarCustodiaMovimientos/ConsultarCustodiaMovimientos.js',
                                                         '../../Custodia/UI/ConsultarCustodiaInstrPendientes/ConsultarCustodiaInstrPendientes.js'] } ],

};


BCS_VENTANA.CustodiaSubyacenteModal = {
    TITLE 		: "UI_CARTERA_SUBYACENTE",
    IMAGE  		: "",
    ENABLE 		: true,
    PRINTABLE 	: true,
    WIDTH  		: "1000px",
    HEIGHT 		: "506px",
    VIEW   		: '../../Custodia/UI/ConsultarCustodiaVencimientos/CustodiaSubyacenteModal.html',
    PARAMS 		: {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/ConsultarCustodiaVencimientos/CustodiaSubyacenteModal.js'] } ],

};

BCS_VENTANA.ConsultarFlujoInstrumento = {
    TITLE : "UI_MENU_CUST_DETALLE_FLUJO_INSTRUMENTO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "962px",
    HEIGHT : "630px",
    VIEW   : '../../Custodia/UI/ConsultarFlujoInstrumento/ConsultarFlujoInstrumento.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/ConsultarFlujoInstrumento/ConsultarFlujoInstrumento.js'] } ],

};



BCS_VENTANA.ConsultarCustodiaPortafolio = {
    TITLE : "UI_MENU_CUST_CUSTODIA_PORTAFOLIO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1060px",
    HEIGHT : "710px",
    VIEW   : '../../Custodia/UI/ConsultarCustodiaPortafolio/ConsultarCustodiaPortafolio.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/ConsultarCustodiaPortafolio/ConsultarCustodiaPortafolio.js'] } ],

};


 
BCS_VENTANA.ConsultarCustodiaGeneral = {
	    TITLE : "UI_MENU_CUST_CUSTODIA_GENERAL",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "600px",
	    VIEW   : '../../Custodia/UI/ConsultarCustodiaGeneral/ConsultarCustodiaGeneral.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/ConsultarCustodiaGeneral/ConsultarCustodiaGeneral.js'] } ],

};




BCS_VENTANA.RevisionCoberturasGarantias = {
	    TITLE  : "UI_COBERTURA_MARGEN_DE_GARANTIAS",		
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "990px",
	    HEIGHT : "656px",
	    VIEW   : '../../Custodia/UI/RevisionCoberturasGarantias/CoberturaGarantia.html',
	    PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Custodia/UI/RevisionCoberturasGarantias/CoberturaGarantia.js'
		                                                     ,'../../Custodia/UI/RevisionCoberturasGarantias/CoberturaGarantiaGrid.js'] } ],

};



BCS_VENTANA.BlotterGarantia = {
    TITLE : "UI_MENU_CUST_GARANTIAS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1050px",
    HEIGHT : "743px",
    VIEW   : '../../Custodia/UI/GTA/BlotterGarantia/BlotterGarantia.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/GTA/BlotterGarantia/BlotterGarantia.js',
        												,'../../Custodia/UI/GTA/ConsultaGarantias/ConsultaGarantias.js'
        												,'../../Custodia/UI/GTA/ConsultaMovimientosGarantias/ConsultaMovimientosGarantias.js'] } ],

};


BCS_VENTANA.IngresarGarantia = {
    TITLE : "UI_TITULO_INGRESAR_GARANTIA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1050px",
    HEIGHT : "750px",
    VIEW   : '../../Custodia/UI/GTA/IngresarGarantia/IngresarGarantia.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/GTA/IngresarGarantia/IngresarGarantia.js',
    													 '../../Custodia/UI/GTA/BusquedaOperacionEspecifica/BusquedaOperacionEspecifica.js',
    												   ] } ],
    AFECTOCIERRE : true,
	MODULO : "CUS",

};

BCS_VENTANA.RetirarGarantia = {
    TITLE : "UI_TITULO_RETIRAR_GARANTIA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1300px",
    HEIGHT : "700px",
    VIEW   : '../../Custodia/UI/GTA/RetirarGarantia/RetirarGarantia.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/GTA/RetirarGarantia/RetirarGarantia.js'] } ],
    AFECTOCIERRE : true,
	MODULO : "CUS",

};

BCS_VENTANA.RetirarGarantiaAgrupadas = {
    TITLE : "UI_RETIRO_GARANTIA_MASIVO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1300px",
    HEIGHT : "700px",
    VIEW   : '../../Custodia/UI/GTA/RetirarGarantiaAgrupada/RetirarGarantiaAgrupada.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/GTA/RetirarGarantiaAgrupada/RetirarGarantiaAgrupada.js'] } ],
    AFECTOCIERRE : true,
	MODULO : "CUS",

};

BCS_VENTANA.RetirarGarantia2 = {
    TITLE : "UI_TITULO_RETIRAR_GARANTIA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1050px",
    HEIGHT : "700px",
    VIEW   : '../../Custodia/UI/GTA/RetirarGarantia2/RetirarGarantia2.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/GTA/RetirarGarantia2/RetirarGarantia2.js'] } ],

};

BCS_VENTANA.BusquedaOperacionEspecifica = {
    TITLE : "UI_BUSQUEDA_OPERACION_ESPECIFICA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "371px",
    VIEW   : '../../Custodia/UI/GTA/BusquedaOperacionEspecifica/BusquedaOperacionEspecifica.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/GTA/BusquedaOperacionEspecifica/BusquedaOperacionEspecifica.js',
    												   ] } ],

};


BCS_VENTANA.BlotterEventoCapital= {
	    TITLE : "UI_TITULO_EVENTO_CAPITAL",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1070px",
    	HEIGHT : "700px",
	    VIEW   : '../../Custodia/UI/BlotterEventoCapital/BlotterEventoCapital.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/BlotterEventoCapital/BlotterEventoCapital.js',
	                                                         '../../Custodia/UI/ConsultarEventoCapital/ConsultarEventoCapital.js', 
	                                                         '../../Custodia/UI/BlotterEventoCapital/BlotterEventoCapitalGrid.js',
	                                                         '../../Custodia/UI/ConsultarEventoCapitalCliente/ConsultarEventoCapitalCliente.js',
	                                                         '../../FondoMutuo/UI/bcsFormaPago/bcsFormaPago.js',
	                                                         '../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] } ],

};

BCS_VENTANA.CargaArchivoRetenciones = {
		TITLE : "UI_CARGA_ARCHIVO_RETENCIONES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "550px",
		HEIGHT : "150px",
	    VIEW   : '../../Custodia/UI/ConsultarEventoCapitalCliente/CargaArchivoRetenciones.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Custodia/UI/ConsultarEventoCapitalCliente/CargaArchivoRetenciones.js'] }],
};

BCS_VENTANA.CrearEventoCapitalModal = {
    TITLE : "UI_EVENTO_CAPITAL",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "640px",
    HEIGHT : "360px",
    VIEW   : '../../Custodia/UI/EventoCapital/CrearEventoCapitalModal.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Custodia/UI/EventoCapital/CrearEventoCapitalModal.js']}],
	AFECTOCIERRE : true,
	MODULO : "CUS",

};


BCS_VENTANA.CrearEventoCapitalClienteModal = {
	    TITLE : "UI_EVENTOCAPITALCLIENTE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "740px",
	    HEIGHT : "470px",
	    VIEW   : '../../Custodia/UI/EventoCapital/CrearEventoCapitalClienteModal.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Custodia/UI/EventoCapital/CrearEventoCapitalClienteModal.js']}],

};


BCS_VENTANA.CargarEventoCapitalModal = {
	    TITLE : "UI_CARGAR_EVENTO_CAPITAL",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "640px",
	    HEIGHT : "420px",
	    VIEW   : '../../Custodia/UI/EventoCapital/CargarEventoCapitalModal.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Custodia/UI/EventoCapital/CargarEventoCapitalModal.js']}],
};


BCS_VENTANA.ModificaFormaPagoEventoCapitalClienteModal = {
	    TITLE : "UI_EVENTOCAPITALCLIENTE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "600px",
	    HEIGHT : "170px",
	    VIEW   : '../../Custodia/UI/EventoCapital/ModificaFormaPagoEventoCapitalClienteModal.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Custodia/UI/EventoCapital/ModificaFormaPagoEventoCapitalClienteModal.js']}],

};

BCS_VENTANA.CrearSuscripcionClienteModal = {
	    TITLE : "UI_INGRESO_SUSCRIPCION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "740px",
	    HEIGHT : "470px",
	    VIEW   : '../../Custodia/UI/EventoCapital/CrearSuscripcionClienteModal.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Custodia/UI/EventoCapital/CrearSuscripcionClienteModal.js']}],

};


BCS_VENTANA.InscribirClientePrepagoBono = {
	    TITLE : "UI_EVENTOCAPITALCLIENTE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "760px",
	    HEIGHT : "480px",
	    VIEW   : '../../Custodia/UI/EventoCapital/InscribirClientePrepagoBono.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Custodia/UI/EventoCapital/InscribirClientePrepagoBono.js']}],

};


BCS_VENTANA.CerrarCorteCuponModal = {
	    TITLE : "UI_CERRAR_CORTE_CUPON",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "350px",
	    HEIGHT : "160px",
	    VIEW   : '../../Custodia/UI/EventoCapital/CerrarCorteCuponModal.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Custodia/UI/EventoCapital/CerrarCorteCuponModal.js']}],
	    AFECTOCIERRE : true,
		MODULO : "CUS",

};

BCS_VENTANA.ProcesarCorteCuponModal = {
	    TITLE : "UI_PROCESAR_CORTE_CUPON",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "350px",
	    HEIGHT : "160px",
	    VIEW   : '../../Custodia/UI/EventoCapital/ProcesarCorteCuponModal.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Custodia/UI/EventoCapital/ProcesarCorteCuponModal.js']}],
	    AFECTOCIERRE : true,
		MODULO : "CUS",

};


BCS_VENTANA.CrearSorteoLetrasModal = {
	    TITLE : "UI_INGRESO_SORTEO_LETRAS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
    	HEIGHT : "420px",	    
	    VIEW   : '../../Custodia/UI/EventoCapital/CrearSorteoLetrasModal.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Custodia/UI/EventoCapital/CrearSorteoLetrasModal.js']}],
	    AFECTOCIERRE : true,
		MODULO : "CUS",

};

BCS_VENTANA.BlotterCuadraturaCustodia = {
	    TITLE : "UI_CUADRATURA_CUSTODIA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
    	HEIGHT : "670px",
	    VIEW   : '../../Custodia/UI/BlotterCuadraturaCustodia/BlotterCuadraturaCustodia.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/BlotterCuadraturaCustodia/BlotterCuadraturaCustodia.js',
	                                                         '../../Custodia/UI/ConsultarCuadraturaCustodia/ConsultarCuadraturaCustodia.js',
	                                                         '../../Custodia/UI/BlotterCuadraturaCustodia/BlotterCuadraturaCustodiaGrid.js']}],

};



BCS_VENTANA.BlotterOperacionesPendientes = {
	    TITLE : "UI_TITULO_OPERACIONES_PENDIENTES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
    	HEIGHT : "745px",
	    VIEW   : '../../Custodia/UI/BlotterOperacionesPendientes/BlotterOperacionesPendientes.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/BlotterOperacionesPendientes/BlotterOperacionesPendientes.js',
	                                                         '../../Custodia/UI/ConsultarOperacionesPendientes/ConsultarOperacionesPendientes.js',
	                                                         '../../Custodia/UI/BlotterOperacionesPendientes/BlotterOperacionesPendientesGrid.js']}],

};



BCS_VENTANA.IngresarClaveUna= {
	    TITLE : "UI_INGRESO_CLAVE_OPERACIONES_PENDIENTES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "300px",
	    HEIGHT : "232px",
	    VIEW   : '../../Custodia/UI/ConsultarTraspasoDCV/IngresoUnaClave.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Custodia/UI/ConsultarTraspasoDCV/IngresoUnaClave.js'] }],

};

BCS_VENTANA.BlotterSimultanea = {
    TITLE : "UI_SIMULTANEAS", 
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1050px",
    HEIGHT : "auto",
    VIEW   : '../../Custodia/UI/SIM/BlotterSimultanea/BlotterSimultanea.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Custodia/UI/SIM/BlotterSimultanea/BlotterSimultanea.js',
                                                          '../../Custodia/UI/SIM/SimultaneasVigentes/SimultaneasVigentes.js',
                                                          '../../Custodia/UI/SIM/SimultaneasHistoricas/SimultaneasHistoricas.js',
                                                          '../../Custodia/UI/SIM/ConsultaAnticipos/ConsultaAnticipos.js',
                                                        ] } ],

};




BCS_VENTANA.AnticipoSimultanea = {
    TITLE : "UI_TITULO_ANTICIPO_SIMULTANEA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "500px",
    HEIGHT : "430px",
    VIEW   : '../../Custodia/UI/SIM/AnticipoSimultanea/AnticipoSimultanea.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/SIM/AnticipoSimultanea/AnticipoSimultanea.js' ] } ],

};


BCS_VENTANA.VencimientoSimultanea = {
    TITLE : "UI_TITULO_VENCIMIENTO_SIMULTANEA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "350px",
    HEIGHT : "150px",
    VIEW   : '../../Custodia/UI/SIM/VencimientoSimultanea/VencimientoSimultanea.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/SIM/VencimientoSimultanea/VencimientoSimultanea.js' ] } ],

};




BCS_VENTANA.BlotterPrestamos = {
    TITLE : "UI_MENU_CUST_PRESTAMOS_Y_EMPRESTITOS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1050px",
    HEIGHT : "725px",
    VIEW   : '../../Custodia/UI/PRT/BlotterPrestamos/BlotterPrestamos.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/PRT/BlotterPrestamos/BlotterPrestamos.js','../../Custodia/UI/PRT/ConfiguracionPRT.js' , '../../Custodia/UI/PRT/PrestamosVigentes/PrestamosVigentes.js', '../../Custodia/UI/PRT/PrestamosHistoricos/PrestamosHistoricos.js' ] } ],

};

BCS_VENTANA.GestionOpciones = {
    TITLE : "UI_MENU_CUST_GESTION_OPCIONES",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1050px",
    HEIGHT : "810px",
    VIEW   : '../../Custodia/UI/GestionOpciones/GestionOpciones.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/GestionOpciones/GestionOpciones.js' ] } ],

};

BCS_VENTANA.BlotterCarteraDerivados = {
    TITLE : "UI_MENU_CUST_GESTION_OPCIONES",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1250px",
    HEIGHT : "775px",
    VIEW   : '../../Custodia/UI/DER/BlotterCarteraDerivados.html',
    PARAMS : {},
    DEPENDENCIES : [{
		type: "js",
		cache: true,
		files: [
			'../../Custodia/UI/DER/BlotterCarteraDerivados.js',
			'../../Custodia/UI/DER/CarteraOpciones.js',
			'../../Custodia/UI/DER/CarteraFuturos.js',
			'../../Custodia/UI/DER/EjercicioOpciones.js',
			'../../Custodia/UI/DER/ContratosOpciones.js',
			'../../Custodia/UI/DER/ResultadoDiarioFuturos.js',
			'../../Custodia/UI/DER/ValorizacionFuturos.js',
		]
	}],
};

BCS_VENTANA.PrestamosPendientesPorCargar = {
	    TITLE : "UI_PRESTAMOS_PENDIENTES_POR_CARGAR",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1050px",
	    HEIGHT : "656px",
	    VIEW   : '../../Custodia/UI/PrestamosPendientesPorCargar/PrestamosPendientesPorCargar.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/PrestamosPendientesPorCargar/PrestamosPendientesPorCargar.js' ] } ],
};

BCS_VENTANA.VencimientoPactos = {
	    TITLE : "UI_TITULO_VENCIMIENTO_PACTOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "350px",
	    HEIGHT : "150px",
	    VIEW   : '../../Mesa/UI/MRF/Vencimiento/VencimientoPactos.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/MRF/Vencimiento/VencimientoPactos.js' ] } ],

	};


BCS_VENTANA.CrearPrestamoModal = {
    TITLE : "UI_MENU_CUST_CREAR_PRESTAMO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "770px",
    HEIGHT : "500px",
    VIEW   : '../../Custodia/UI/PRT/Prestamo/CrearPrestamoModal.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Custodia/UI/PRT/Prestamo/CrearPrestamoModal.js']}],

};


BCS_VENTANA.ModificarPrestamoModal = {
	    TITLE : "UI_MENU_CUST_MODIFICAR_PRESTAMO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "670px",
	    HEIGHT : "500px",
	    VIEW   : '../../Custodia/UI/PRT/Prestamo/ModificarPrestamoModal.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Custodia/UI/PRT/Prestamo/ModificarPrestamoModal.js']}],

};

BCS_VENTANA.ModificarEmprestitoModal = {
	    TITLE : "UI_MENU_CUST_MODIFICAR_EMPRESTITO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "670px",
	    HEIGHT : "470px",
	    VIEW   : '../../Custodia/UI/PRT/Emprestito/ModificarEmprestitoModal.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Custodia/UI/PRT/Emprestito/ModificarEmprestitoModal.js']}],

};
BCS_VENTANA.RenovarPrestamoModal = {
	    TITLE : "UI_MENU_CUST_RENOVAR_PRESTAMO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "670px",
	    HEIGHT : "500px",
	    VIEW   : '../../Custodia/UI/PRT/Prestamo/RenovarPrestamoModal.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Custodia/UI/PRT/Prestamo/RenovarPrestamoModal.js']}],
};


BCS_VENTANA.CrearEmprestitoModal = {
    TITLE : "UI_MENU_CUST_CREAR_EMPRESTITO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "650px",
    HEIGHT : "467px",
    VIEW   : '../../Custodia/UI/PRT/Emprestito/CrearEmprestitoModal.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Custodia/UI/PRT/Emprestito/CrearEmprestitoModal.js']}],

};

BCS_VENTANA.AnticiparPrestamoModal = {
	    TITLE : "UI_MENU_CUST_ANTICIPAR_PRESTAMO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "670px",
	    HEIGHT : "500px",
	    VIEW   : '../../Custodia/UI/PRT/Prestamo/AnticiparPrestamoModal.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Custodia/UI/PRT/Prestamo/AnticiparPrestamoModal.js']}],

	};

BCS_VENTANA.AnticiparPrestamistaModal = {
    TITLE : "UI_MENU_CUST_ANTICIPAR_EMPRESTITO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "650px",
    HEIGHT : "470px",
    VIEW   : '../../Custodia/UI/PRT/Emprestito/AnticiparPrestamistaModal.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Custodia/UI/PRT/Prestamo/AnticiparPrestamistaModal.js']}],

};

BCS_VENTANA.AnticiparEmprestitoModal = {
    TITLE : "UI_MENU_CUST_ANTICIPAR_EMPRESTITO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "650px",
    HEIGHT : "470px",
    VIEW   : '../../Custodia/UI/PRT/Emprestito/AnticiparEmprestitoModal.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Custodia/UI/PRT/Emprestito/AnticiparEmprestitoModal.js']}],

};

BCS_VENTANA.AnularEmprestitoModal = {
    TITLE : "UI_MENU_CUST_DESASIGNAR_PRESTAMISTA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "650px",
    HEIGHT : "411px",
    VIEW   : '../../Custodia/UI/PRT/Emprestito/AnularEmprestitoModal.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Custodia/UI/PRT/Emprestito/AnularEmprestitoModal.js']}],

};

BCS_VENTANA.VencerPrestamos = {
    TITLE : "UI_MENU_CUST_VENCER_PRESTAMOS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "350px",
    HEIGHT : "150px",
    VIEW   : '../../Custodia/UI/PRT/Prestamo/VencerPrestamos.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/PRT/Prestamo/VencerPrestamos.js'] } ],

};


BCS_VENTANA.ConsultarCarterasVentana = {
    TITLE : "UI_MENU_CUST_CONSULTAR_CARTERAS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "905px",
    HEIGHT : "583px",
    VIEW   : '../../Custodia/UI/ConsultarCarteras/ConsultarCarteras.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Custodia/UI/BlotterCarteras/BlotterCarteras.js', '../../Custodia/UI/BlotterCarteras/BlotterCarterasGrid.js', '../../Custodia/UI/ConsultarCarteras/ConsultarCarteras.js']}],

};

BCS_VENTANA.ValorizacionCustodia= {
    TITLE : "UI_MENU_CUST_VALORIZACION_CUSTODIA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "715px",
    HEIGHT : "395px",
    VIEW   : '../../Custodia/UI/VAL/ValorizacionCustodia.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/VAL/ValorizacionCustodia.js'] } ],
	AFECTOCIERRE : true,
	MODULO : "CUS",

};


BCS_VENTANA.BlotterCargaMasivaCustodia = {
	    TITLE : "UI_CARGA_MASIVA_CUSTODIA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
    	HEIGHT : "507px",
	    VIEW   : '../../Custodia/UI/BlotterCargaMasivaCustodia/BlotterCargaMasivaCustodia.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/BlotterCargaMasivaCustodia/BlotterCargaMasivaCustodia.js',
	                                                         '../../Custodia/UI/BlotterCargaMasivaCustodia/BlotterCargaMasivaCustodiaGrid.js']}],
		AFECTOCIERRE : true,
		MODULO : "CUS",

};


BCS_VENTANA.DetalleCargaMasivaCustodia = {
	    TITLE  : "UI_DETALLE_CARGA_MASIVA_CUSTODIA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1200px",
	    HEIGHT : "500px",    
	    VIEW   : '../../Custodia/UI/BlotterCargaMasivaCustodia/DetalleCargaMasivaCustodia/DetalleCargaMasivaCustodia.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, 
	    	files: [ '../../Custodia/UI/BlotterCargaMasivaCustodia/DetalleCargaMasivaCustodia/DetalleCargaMasivaCustodia.js',
	    	         '../../Custodia/UI/BlotterCargaMasivaCustodia/BlotterCargaMasivaCustodiaGrid.js'
	    	         ] 
		}],

};

BCS_VENTANA.ProcesarCargaMasivaCustodiaModal = {
	    TITLE  : "UI_PROCESAR_CARGA_MASIVA_CUSTODIA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "150px",    
	    VIEW   : '../../Custodia/UI/BlotterCargaMasivaCustodia/DetalleCargaMasivaCustodia/ProcesarCargaMasivaCustodia.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, 
	    	files: [ '../../Custodia/UI/BlotterCargaMasivaCustodia/DetalleCargaMasivaCustodia/ProcesarCargaMasivaCustodia.js'
	    	         ] 
		}],
};

//agregar pregunta
//agregar respuesta
//crear encuesta
BCS_VENTANA.ConfiguracionEncuesta = {
    TITLE : "UI_CONFIGURACION_ENCUESTA_CATEGORIA_INVERSIONISTA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "700px",
    HEIGHT : "400px",
    VIEW   : 'ENC/ConfiguracionEncuesta/ConfiguracionEncuesta.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: false, files: [ 'ENC/ConfiguracionEncuesta/ConfiguracionEncuesta.js'] } ],

};

BCS_VENTANA.ConfiguracionPuntajeEncuesta = {
	    TITLE : "UI_CONFIGURACION_PUNTAJE_ENCUESTA_CATEGORIA_INVERSIONISTA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "500px",
	    HEIGHT : "400px",
	    VIEW   : 'ENC/ConfiguracionEncuesta/ConfiguracionPuntajeEncuesta.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: false, files: [ 'ENC/ConfiguracionEncuesta/ConfiguracionPuntajeEncuesta.js'] } ],

	};

BCS_VENTANA.CrearDefinicionEncuesta = {
    TITLE : "UI_CREAR_DEFINICION_ENCUESTA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "700px",
    HEIGHT : "550px",
    VIEW   : 'ENC/CrearDefinicionEncuesta/CrearDefinicionEncuesta.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: false, files: [ 'ENC/CrearDefinicionEncuesta/CrearDefinicionEncuesta.js'] } ],

};

BCS_VENTANA.ModificarDefinicionEncuesta = {
	    TITLE : "UI_MODIFICAR_DEFINICION_ENCUESTA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "700px",
	    HEIGHT : "550px",
	    VIEW   : 'ENC/ModificarDefinicionEncuesta/ModificarDefinicionEncuesta.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: false, files: [ 'ENC/ModificarDefinicionEncuesta/ModificarDefinicionEncuesta.js'] } ],

	};

BCS_VENTANA.AgregarPregunta = {
    TITLE : "UI_AGREGAR_PREGUNTA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "500px",
    HEIGHT : "200px",
    VIEW   : 'ENC/AgregarPreguntaEncuesta/AgregarPreguntaEncuesta.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: false, files: [ 'ENC/AgregarPreguntaEncuesta/AgregarPreguntaEncuesta.js'] } ],

};

BCS_VENTANA.AgregarRespuesta = {
    TITLE : "UI_AGREGAR_RESPUESTA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "500px",
    HEIGHT : "250px",
    VIEW   : 'ENC/AgregarRespuestaEncuesta/AgregarRespuestaEncuesta.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: false, files: [ 'ENC/AgregarRespuestaEncuesta/AgregarRespuestaEncuesta.js'] } ],

};

BCS_VENTANA.VerDetalleEncuesta = {
	    TITLE : "UI_DETALLE_ENCUESTA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "700px",
	    HEIGHT : "592px",
	    VIEW   : 'ENC/VerDetalleEncuesta/VerDetalleEncuesta.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: false, files: [ 'ENC/VerDetalleEncuesta/VerDetalleEncuesta.js'] } ],

	};

BCS_VENTANA.BlotterCargaMasivaPlataforma = {
	    TITLE : "UI_CARGA_MASIVA_PLATAFORMA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
    	HEIGHT : "500px",
	    VIEW   : '../../Plataforma/UI/PLA/BlotterCargaMasivaPlataforma/BlotterCargaMasivaPlataforma.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Plataforma/UI/PLA/BlotterCargaMasivaPlataforma/BlotterCargaMasivaPlataforma.js',
	                                                         '../../Plataforma/UI/PLA/BlotterCargaMasivaPlataforma/BlotterCargaMasivaPlataformaGrid.js']}],
		AFECTOCIERRE : true,
		MODULO : "PLA",

};


BCS_VENTANA.DetalleCargaMasivaPlataforma = {
	    TITLE  : "UI_DETALLE_CARGA_MASIVA_PLATAFORMA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1200px",
	    HEIGHT : "500px",    
	    VIEW   : '../../Plataforma/UI/PLA/BlotterCargaMasivaPlataforma/DetalleCargaMasivaPlataforma/DetalleCargaMasivaPlataforma.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, 
	    	files: [ '../../Plataforma/UI/PLA/BlotterCargaMasivaPlataforma/DetalleCargaMasivaPlataforma/DetalleCargaMasivaPlataforma.js',
	    	         '../../Plataforma/UI/PLA/BlotterCargaMasivaPlataforma/BlotterCargaMasivaPlataformaGrid.js'
	    	         ] 
		}],

};

BCS_VENTANA.ProcesarCargaMasivaPlataformaModal = {
	    TITLE  : "UI_PROCESAR_CARGA_MASIVA_PLATAFORMA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "150px",    
	    VIEW   : '../../Plataforma/UI/PLA/BlotterCargaMasivaPlataforma/DetalleCargaMasivaPlataforma/ProcesarCargaMasivaPlataforma.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, 
	    	files: [ '../../Plataforma/UI/PLA/BlotterCargaMasivaPlataforma/DetalleCargaMasivaPlataforma/ProcesarCargaMasivaPlataforma.js'
	    	         ] 
		}],
};

BCS_VENTANA.BlotterPactosCorteSorteo = {
    TITLE : "UI_MENU_CUST_PACTOS_CORTE_SORTEO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1050px",
    HEIGHT : "700px",
    VIEW   : '../../Custodia/UI/ConsultarPactosCorteSorteo/BlotterPactosCorteSorteo/BlotterPactosCorteSorteo.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/ConsultarPactosCorteSorteo/BlotterPactosCorteSorteo/BlotterPactosCorteSorteo.js'] } ],

};

//
BCS_VENTANA.ConsultarJuntaAccionistas= {
	    TITLE : "UI_CONSULTAR_JUNTA_ACCIONISTAS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "680px",
	    VIEW   : '../../Custodia/UI/MantenedorJuntaAccionistas/ConsultarJuntaAccionistas.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Custodia/UI/MantenedorJuntaAccionistas/ConsultarJuntaAccionistas.js',
	                                                       '../../Custodia/UI/MantenedorJuntaAccionistas/ConsultarJuntaAccionistasGrid.js'] }],

};

//CGO
BCS_VENTANA.BlotterPagoCorteCupon = {
	    TITLE : "UI_PAGO_CORTE_CUPON",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
    	HEIGHT : "735px",
	    VIEW   : '../../Custodia/UI/BlotterPagoCorteCupon/BlotterPagoCorteCupon.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/BlotterPagoCorteCupon/BlotterPagoCorteCupon.js',
	                                                         '../../Custodia/UI/ConsultarPagoCorteCupon/ConsultarPagoCorteCupon.js',
	                                                         '../../Custodia/UI/BlotterPagoCorteCupon/BlotterPagoCorteCuponGrid.js']}],

};
//CGO

BCS_VENTANA.BlotterTraspasoDCV = {
	    TITLE : "UI_TITULO_TRANSFERENCIA_DE_VALORES_AL_DEPOSITO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
    	HEIGHT : "755px",
	    VIEW   : '../../Custodia/UI/BlotterTraspasoDCV/BlotterTraspasoDCV.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/BlotterTraspasoDCV/BlotterTraspasoDCV.js',
	                                                         '../../Custodia/UI/ConsultarTraspasoDCV/ConsultarTraspasoDCV.js',
	                                                         '../../Custodia/UI/BlotterTraspasoDCV/BlotterTraspasoDCVGrid.js']}],
	                                           
		AFECTOCIERRE : true,
		MODULO : "CUS",

};

BCS_VENTANA.CertificadoCustodia= {
	    TITLE : "UI_CERTIFICADO_CUSTODIA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "550",
	    HEIGHT : "600px",
	    VIEW   : '../../Custodia/UI/CertificadoCustodia/CertificadoCustodia.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: [ '../../Custodia/UI/CertificadoCustodia/CertificadoConfiguracion.js',
															'../../Custodia/UI/CertificadoCustodia/CertificadoCustodia.js',
														    '../../Custodia/UI/CertificadoCustodia/Paneles/PanelCartasCertif.js']}],
};

BCS_VENTANA.ReporteDiarioPrestamosAnexo14= {
	    TITLE : "UI_ANEXO14_REGISTRO_DE_PRESTAMOS_DE_VALORES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "450px",
	    HEIGHT : "135px",
	    VIEW   : '../../Custodia/UI/ReporteDiarioPrestamosAnexo14/ReporteDiarioPrestamosAnexo14.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/ReporteDiarioPrestamosAnexo14/ReporteDiarioPrestamosAnexo14.js' ]} ],

};



BCS_VENTANA.TarjetaExistencia= {
	    TITLE : "UI_TARJETA_EXISTENCIA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "650px",
	    VIEW   : '../../Custodia/UI/ConsultarTarjetaExistencia/ConsultarTarjetaExistencia.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Custodia/UI/ConsultarTarjetaExistencia/ConsultarTarjetaExistencia.js'] }],
};


BCS_VENTANA.ConsultarFusionCartera= {
	    TITLE : "UI_FUSION_CARTERA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1200px",
	    HEIGHT : "650px",
	    VIEW   : '../../Custodia/UI/ConsultarFusionCartera/ConsultarFusionCartera.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Custodia/UI/ConsultarFusionCartera/ConsultarFusionCartera.js'] }],
};
BCS_VENTANA.ConsultarFusionCarteraClientes= {
	    TITLE : "UI_FUSION_CARTERA_CLIENTES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1200px",
	    HEIGHT : "650px",
	    VIEW   : '../../Custodia/UI/ConsultarFusionCarteraClientes/ConsultarFusionCarteraClientes.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Custodia/UI/ConsultarFusionCarteraClientes/ConsultarFusionCarteraClientes.js'] }],
};

BCS_VENTANA.FusionarCarterasClientesModal = {
	    TITLE : "UI_CARTERA_CLIENTESMODAL",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "420px",
	    HEIGHT : "220px",
	    VIEW   : '../../Custodia/UI/ConsultarFusionCarteraClientes/FusionarCarterasClientesModal.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Custodia/UI/ConsultarFusionCarteraClientes/FusionarCarterasClientesModalCtrl.js'] }],

	};
	
BCS_VENTANA.FusionarCarterasClientesPreviewModal = {
	    TITLE : "UI_CARTERA_CLIENTESPREVIEWMODAL",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "830px",
	    HEIGHT : "400px",
	    VIEW   : '../../Custodia/UI/ConsultarFusionCarteraClientes/FusionarCarterasClientesPreviewModal.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Custodia/UI/ConsultarFusionCarteraClientes/FusionarCarterasClientesPreviewModalCtrl.js'] }],

	};	

//---------------------------------------------
//FACTURACION
//---------------------------------------------
BCS_VENTANA.MODULO_FACTURACION= {
    TITLE : "UI_FACTURACION",
    IMAGE  : "sapUiIcon customer-financial-fact-sheet",
    ENABLE : true,
    WIDTH  : "1100px",
    HEIGHT : "400px",
    VIEW   : '',
    PARAMS : {},
    DEPENDENCIES : [],

};


BCS_VENTANA.IngresarMovimiento = {
    TITLE : "UI_INGRESAR_MOVIMIENTO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1160px",
    HEIGHT : "565px",
    VIEW   : '../../Facturacion/UI/MovimientosFacturar/IngresarMovimientoFacturar.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Facturacion/UI/MovimientosFacturar/IngresarMovimientoFacturar.js',
                                                        '../../Facturacion/UI/MovimientosFacturar/IngresarMovimientoFacturarCobroGrid.js']}],

};


BCS_VENTANA.BlotterFacturacion = {
    TITLE : "UI_GESTION_FACTURACION",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1100px",
    HEIGHT : "728px",
    VIEW   : '../../Facturacion/UI/BlotterFacturacion/BlotterFacturacion.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Facturacion/UI/BlotterFacturacion/BlotterFacturacion.js',                                                         
                                                         '../../Facturacion/UI/MovimientosFacturar/MovimientoFacturar.js',                                                         
                                                         '../../Facturacion/UI/MovimientosFacturar/MovimientoFacturarGrid.js',
                                                         '../../Facturacion/UI/ComprobantesFacturacion/ComprobanteFacturacion.js',
                                                         '../../Facturacion/UI/ComprobantesFacturacion/ComprobanteFacturacionGrid.js']}],

};

BCS_VENTANA.EmisionFacturacionMasiva = {
    TITLE : "UI_EMISION_FACTURACION_MASIVA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "755px",
    HEIGHT : "530px",
    VIEW   : '../../Facturacion/UI/ComprobantesFacturacion/EmisionFacturacionMasiva.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Facturacion/UI/ComprobantesFacturacion/EmisionFacturacionMasiva.js',
                                                         '../../Facturacion/UI/ComprobantesFacturacion/EmisionFacturacionMasivaGrid.js']}],
	AFECTOCIERRE : true,
	MODULO : "FAC",

};

BCS_VENTANA.CrearOtrosConceptosModal = {
	    TITLE : "UI_MOVIMIENTOS_OTROS_CONCEPTOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "950px",
	    HEIGHT : "400px",
	    VIEW   : '../../Facturacion/UI/MovimientosFacturar/IngresarMovimientoFacturarOtrosConceptos.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Facturacion/UI/MovimientosFacturar/IngresarMovimientoFacturarOtrosConceptos.js'
	                                                       ]
	    }],

	};

BCS_VENTANA.ModificarMovimientoModal = {
    TITLE : "UI_MODIFICAR_MOVIMIENTO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "850px",
    HEIGHT : "546px",
    VIEW   : '../../Facturacion/UI/MovimientosFacturar/ModificarMovimientoModal.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Facturacion/UI/MovimientosFacturar/ModificarMovimientoModal.js',
                                                         '../../Facturacion/UI/MovimientosFacturar/ModificarMovimientoGeneralModal.js',
                                                         '../../Facturacion/UI/MovimientosFacturar/ModificarMovimientoCobroModal.js',
                                                         '../../Facturacion/UI/MovimientosFacturar/ModificarMovimientoCobroGridModal.js',
                                                         '../../Facturacion/UI/MovimientosFacturar/ModificarMovimientoFormasDePagoModal.js',
                                                         '../../Facturacion/UI/MovimientosFacturar/ModificarMovimientoFormasDePagoGridModal.js'
                                                       ]
    }],

};

BCS_VENTANA.ModificarPortafolioMovimientoModal = {
    TITLE : "UI_MODIFICAR_MOVIMIENTO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "390px",
    HEIGHT : "250px",
    VIEW   : '../../Facturacion/UI/MovimientosFacturar/ModificarPortafolioMovimientoModal.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Facturacion/UI/MovimientosFacturar/ModificarPortafolioMovimientoModal.js']
    }],

};

BCS_VENTANA.ModificarMasivoComisionesModal = {
    TITLE : "UI_MODIFICAR_MOVIMIENTO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "950px",
    HEIGHT : "685px",
    VIEW   : '../../Facturacion/UI/MovimientosFacturar/ModificarMasivoComisionesModal.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Facturacion/UI/MovimientosFacturar/ModificarMasivoComisionesModal.js']
    }],
};

BCS_VENTANA.AnularMovimientosPorAsignacionMasivaModal = {
    TITLE : "UI_ANULACION_MOVIMIENTOS_A_FACTURAR",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "350px",
    HEIGHT : "auto",
    VIEW   : '../../Facturacion/UI/MovimientosFacturar/AnularMovimientosPorAsignacionMasivaModal.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Facturacion/UI/MovimientosFacturar/AnularMovimientosPorAsignacionMasivaModal.js']
    }],
};

BCS_VENTANA.DetalleAnularMovimientos =  {
	TITLE : "UI_DETALLES_MOVIMIENTOS_ANULAR",
    IMAGE  : "",
    ENABLE : true,    
	WIDTH  : "1020px",
	HEIGHT : "350px",
    VIEW   : '../../Facturacion/UI/MovimientosFacturar/DetalleAnularMovimientos.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Facturacion/UI/MovimientosFacturar/DetalleAnularMovimientos.js']}],
};

BCS_VENTANA.MovimientoFacturarCostosModal = {
    TITLE : "UI_MOVIMIENTO_COSTOS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "550px",
    HEIGHT : "352px",
    VIEW   : '../../Facturacion/UI/MovimientosFacturar/MovimientoFacturarCostosModal.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ 
                                                         '../../Facturacion/UI/MovimientosFacturar/MovimientoFacturarCostosModal.js',
                                                         '../../Facturacion/UI/MovimientosFacturar/MovimientoFacturarCostosGridModal.js'
                                                       ]
    }],

};


BCS_VENTANA.ComprobanteInformesDTEModal = {
    TITLE : "UI_COMPROBANTE_INFORMES_DTE",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "280px",
    HEIGHT : "180px",
    VIEW   : '../../Facturacion/UI/ComprobantesFacturacion/ComprobanteInformesDTEModal.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Facturacion/UI/ComprobantesFacturacion/ComprobanteInformesDTEModal.js']
    }],

};


BCS_VENTANA.ComprobanteInformesDTEMensajesModal = {
	    TITLE : "UI_COMPROBANTE_INFORMES_DTE_MENSAJES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "500px",
	    HEIGHT : "300px",
	    VIEW   : '../../Facturacion/UI/ComprobantesFacturacion/ComprobanteInformesDTEMensajesModal.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Facturacion/UI/ComprobantesFacturacion/ComprobanteInformesDTEMensajesModal.js',
	    	                                                 '../../Facturacion/UI/ComprobantesFacturacion/ComprobanteInformesDTEMensajesModalGrid.js'
	    	                                               ]	    
	    }],

	};


BCS_VENTANA.DetalleComprobanteFacturacion = {
    TITLE : "UI_COMPROBANTE_DE_FACTURACION",
    IMAGE  : "",
    PRINTABLE : true,
    ENABLE : true,
    WIDTH  : "1080px",
    HEIGHT : "650px",
    VIEW   : '../../Facturacion/UI/DetalleComprobanteFacturacion/DetalleComprobanteFacturacion.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Facturacion/UI/DetalleComprobanteFacturacion/DetalleComprobanteFacturacion.js',
    													 '../../Facturacion/UI/ComprobantesFacturacion/ComprobanteFacturacionGrid.js'
    												   ]
                   }],
};

BCS_VENTANA.DetalleMantenedorIGK = {
    TITLE : "UI_REGULARIZACION_IGK",
    IMAGE  : "",
    PRINTABLE : true,
    ENABLE : true,
    WIDTH  : "1080px",
    HEIGHT : "650px",
    VIEW   : '../../Facturacion/UI/DetalleMantenedorIGK/DetalleMantenedorIGK.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Facturacion/UI/DetalleMantenedorIGK/DetalleMantenedorIGK.js',
    													 '../../Facturacion/UI/ComprobantesFacturacion/ComprobanteFacturacionGrid.js'
    												   ]
                   }],
};


BCS_VENTANA.DetalleMovimientoComprobante = {
    TITLE : "UI_MOVIMIENTO_COMPROBANTE",
    IMAGE  : "",
    PRINTABLE : true,
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "600px",
    VIEW   : '../../Facturacion/UI/DetalleMovimientoComprobante/DetalleMovimientoComprobante.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Facturacion/UI/DetalleMovimientoComprobante/DetalleMovimientoComprobante.js']
    }],
};

BCS_VENTANA.BlotterLibroDte = {
    TITLE : "UI_LIBRO_DTE",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "950px",
    HEIGHT : "673px",
    VIEW   : '../../Facturacion/UI/BlotterLibroDte/BlotterLibroDte.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Facturacion/UI/BlotterLibroDte/BlotterLibroDte.js',
                                                         '../../Facturacion/UI/BlotterLibroDte/LibroDteComprobante.js',                                                         
                                                         '../../Facturacion/UI/BlotterLibroDte/LibroDteComprobanteGrid.js',
                                                         '../../Facturacion/UI/BlotterLibroDte/LibroDte.js',
                                                         '../../Facturacion/UI/BlotterLibroDte/LibroDteGrid.js']}],
};


BCS_VENTANA.CargaMasivaFacturacion = {
	    TITLE  : "UI_CARGA_MASIVA_FACTURACION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "508px",    
	    VIEW   : '../../Facturacion/UI/BlotterCargaMasiva/BlotterCargaMasiva.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, 
	    	files: [ '../../Facturacion/UI/BlotterCargaMasiva/BlotterCargaMasiva.js',
	    	         '../../Facturacion/UI/BlotterCargaMasiva/BlotterCargaMasivaGrid.js'
	    	         ] 
		}],
		AFECTOCIERRE : true,
		MODULO : "FAC",
};


BCS_VENTANA.PeriodoCierre = {
	    TITLE  : "UI_PERIODO_CIERRE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "300px",
	    HEIGHT : "400px",    
	    VIEW   : '../../Facturacion/UI/PeriodoCierre/ProcesarPeriodoCierre.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, 
	    	files: [ '../../Facturacion/UI/PeriodoCierre/ProcesarPeriodoCierre.js',
	    		     '../../Facturacion/UI/PeriodoCierre/ProcesarPeriodoCierreGrid.js'
	    		   ] 
		}],
};


BCS_VENTANA.DetalleCargaMasivaFacturacion = {
	    TITLE  : "UI_DETALLE_CARGA_MASIVA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "500px",    
	    VIEW   : '../../Facturacion/UI/BlotterCargaMasiva/DetalleCargaMasiva/DetalleCargaMasiva.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, 
	    	files: [ '../../Facturacion/UI/BlotterCargaMasiva/DetalleCargaMasiva/DetalleCargaMasiva.js',
	    	         '../../Facturacion/UI/BlotterCargaMasiva/BlotterCargaMasivaGrid.js'
	    	         ] 
		}],
};


BCS_VENTANA.ComplementoCargaMasivaFacturacion = {
	    TITLE  : "UI_COMPLEMENTO_CARGA_MASIVA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "185px",    
	    VIEW   : '../../Facturacion/UI/BlotterCargaMasiva/ComplementoCargaMasiva/ComplementoCargaMasiva.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, 
	    	files: [ '../../Facturacion/UI/BlotterCargaMasiva/ComplementoCargaMasiva/ComplementoCargaMasiva.js'] 
		}],
};

BCS_VENTANA.CuadraturaFacturacion = {
		TITLE : "UI_CUADRATURA_FACTURACION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "200px",
	    VIEW   : '../../Facturacion/UI/CuadraturaFacturacion/CuadraturaFacturacion.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: [ '../../Facturacion/UI/CuadraturaFacturacion/CuadraturaFacturacion.js']}],
};



BCS_VENTANA.FacturacionSeparadorConsultas = {
	SEPARATE: true,
	TITLE : "UI_CONSULTAS",
	ENABLE : true
};




BCS_VENTANA.CuadraturaTransaccionFacturacion = {
    TITLE : "UI_CUADRATURA_TRANSACCION_FACTURACION",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "950px",
    HEIGHT : "637px",
    VIEW   : '../../Facturacion/UI/Consultas/CuadraturaTransaccionFacturacion.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Facturacion/UI/Consultas/CuadraturaTransaccionFacturacion.js',
                                                         '../../Facturacion/UI/Consultas/CuadraturaTransaccionFacturacionGrid.js'
                                                       ]
    }],

};




BCS_VENTANA.ConsultaGeneralFacturacion = {
    TITLE : "UI_CONSULTA_GENERAL_FACTURACION",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1100px",
    HEIGHT : "710px",
    VIEW   : '../../Facturacion/UI/Consultas/ConsultaGeneralFacturacion.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Facturacion/UI/Consultas/ConsultaGeneralFacturacion.js',
                                                         '../../Facturacion/UI/Consultas/ConsultarMovimientoFacturar.js',
                                                         '../../Facturacion/UI/Consultas/ConsultarMovimientoFacturarGrid.js',
                                                         '../../Facturacion/UI/Consultas/ConsultarComprobante.js',
                                                         '../../Facturacion/UI/Consultas/ConsultarComprobanteGrid.js'
                                                       ]
    }],

};


BCS_VENTANA.BlotterDeudoresAcreedores = {
	    TITLE : "UI_DEUDORES_ACREEDORES_POR_INTERMEDIACION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1150px",
	    HEIGHT : "700px",
	    VIEW   : '../../Facturacion/UI/BlotterDeudoresAcreedores/BlotterDeudoresAcreedores.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Facturacion/UI/BlotterDeudoresAcreedores/BlotterDeudoresAcreedores.js',                                                         
	                                                         '../../Facturacion/UI/DeudoresAcreedoresPorIntermediacionCliente/DeudoresAcreedoresPorIntermediacionCliente.js',                                                         
	                                                         '../../Facturacion/UI/DeudoresAcreedoresPorIntermediacionCliente/DeudoresAcreedoresPorIntermediacionClienteGrid.js',
	                                                         '../../Facturacion/UI/DeudoresAcreedoresPorIntermediacionCorredor/DeudoresAcreedoresPorIntermediacionCorredor.js',
	                                                         '../../Facturacion/UI/DeudoresAcreedoresPorIntermediacionCorredor/DeudoresAcreedoresPorIntermediacionCorredorGrid.js']}],

};

BCS_VENTANA.ContratoCobroPeriodico = {
    TITLE : "UI_FACTURACION_COBROS_MENSUAL",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1000px",
    HEIGHT : "580px",
    VIEW   : '../../Facturacion/UI/ContratoCobroPeriodico/ContratoCobroPeriodico.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Facturacion/UI/ContratoCobroPeriodico/ContratoCobroPeriodico.js',
                                                         '../../Facturacion/UI/ContratoCobroPeriodico/ContratoCobroPeriodicoGrid.js'
                                                       ]
    }],

};

BCS_VENTANA.DetalleFacturacionCobroMensual = {
	    TITLE : "UI_FACTURACION_COBROS_MENSUAL_VISTA_PRELIMINAR",
	    IMAGE  : "",
	    PRINTABLE : true,
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "480px",
	    VIEW   : '../../Facturacion/UI/ContratoCobroPeriodico/FacturacionCobroMensual.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Facturacion/UI/ContratoCobroPeriodico/FacturacionCobroMensual.js',
	    													 '../../Facturacion/UI/ContratoCobroPeriodico/FacturacionCobroMensualGrid.js']
	    }],
	};

BCS_VENTANA.DetalleMovimientoContrato = {
    TITLE : "UI_MOVIMIENTO_COMPROBANTE",
    IMAGE  : "",
    PRINTABLE : true,
    ENABLE : true,
    WIDTH  : "800px",
    HEIGHT : "680px",
    VIEW   : '../../Facturacion/UI/ContratoCobroPeriodico/DetalleMovimientoContrato.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Facturacion/UI/ContratoCobroPeriodico/DetalleMovimientoContrato.js']
    }],
};

BCS_VENTANA.IngresarContratoCobroPeriodicoModal = {
    TITLE : "UI_INGRESAR_CONTRATO_COBRO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "800px",
    HEIGHT : "680px",
    VIEW   : '../../Facturacion/UI/ContratoCobroPeriodico/IngresarContratoCobroPeriodicoModal.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Facturacion/UI/ContratoCobroPeriodico/IngresarContratoCobroPeriodicoModal.js',
                                                         '../../Facturacion/UI/ContratoCobroPeriodico/IngresarContratoCobroPeriodicoGridModal.js'
                                                       ]
    }],

};

BCS_VENTANA.CobroDetalle = {
    TITLE : "UI_COBRO_DETALLE",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "850px",
    HEIGHT : "310px",
    VIEW   : '../../Facturacion/UI/ContratoCobroPeriodico/CobroPeriodicoDetalleModal.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Facturacion/UI/ContratoCobroPeriodico/CobroPeriodicoDetalleModal.js',
                                                         '../../Facturacion/UI/ContratoCobroPeriodico/CobroPeriodicoDetalleGridModal.js'
                                                       ]
    }],

};


BCS_VENTANA.CrearContratoCobroMovimientoFacModal = {
	    TITLE : "UI_CREAR_CONTRATO_COBRO_MOVIMIENTO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "850px",
	    HEIGHT : "600px",
	    VIEW   : '../../Facturacion/UI/ContratoCobroPeriodico/CrearContratoCobroPeriodicoMovimientoFacModal.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Facturacion/UI/ContratoCobroPeriodico/CrearContratoCobroPeriodicoMovimientoFacModal.js']
	    }],

	};



BCS_VENTANA.ConsultaCobrosClientes = {
	    TITLE : "UI_CONSULTA_COBROS_CLIENTE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "625px",
	    VIEW   : '../../Facturacion/UI/Consultas/CobrosClientes/CobrosClientes.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Facturacion/UI/Consultas/CobrosClientes/CobrosClientes.js',
	    													 '../../Facturacion/UI/Consultas/CobrosClientes/ConsultaCobrosClientes.js',
	    													 '../../Facturacion/UI/Consultas/CobrosClientes/ConsultaDiferenciaCobros.js']
	    }],

};


BCS_VENTANA.FacturacionSeparadorInformes = {
	SEPARATE: true,
	TITLE : "UI_INFORMES",
	ENABLE : true
};


BCS_VENTANA.GenerarInterfazAfp = {
    TITLE : "UI_INTERFAZ_AFP",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1100px",
    HEIGHT : "600px",
    VIEW   : '../../Facturacion/UI/Informes/GenerarInterfazAfp.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Facturacion/UI/Informes/GenerarInterfazAfp.js',
                                                         '../../Facturacion/UI/Informes/GenerarInterfazAfpGrid.js'
                                                       ]
    }],

};


BCS_VENTANA.GenerarInformeGananciaCapital= {
	TITLE : "UI_INFORME_GANANCIA_DE_CAPITAL",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "450px",
	HEIGHT : "200px",
	VIEW   : '../../Facturacion/UI/Informes/InformeGananciaCapital/InformeGananciaCapital.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Facturacion/UI/Informes/InformeGananciaCapital/InformeGananciaCapital.js'] }],
};

BCS_VENTANA.InformePrefacturas = {
	TITLE : "UI_INFORME_PREFACTURAS",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "900px",
	HEIGHT : "500px",
	VIEW   : '../../Facturacion/UI/Informes/InformePrefacturas/InformePrefacturas.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Facturacion/UI/Informes/InformePrefacturas/InformePrefacturas.js' ] } ],

};

//---------------------------------------------
//TESORERIA
//---------------------------------------------
BCS_VENTANA.MODULO_TESORERIA= {
    TITLE : "UI_TESORERIA",
    IMAGE  : "sapUiIcon money-bills",
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "400px",
    VIEW   : '',
    PARAMS : {},
    DEPENDENCIES : [],

};


BCS_VENTANA.CrearIngresoEgreso = {
	    TITLE : "UI_CREAR_INGRESO_EGRESO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "695px",
	    VIEW   : '../../Tesoreria/UI/EditarIngresoEgreso/EditarIngresoEgreso.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Tesoreria/UI/EditarIngresoEgreso/EditarIngresoEgreso.js',
	                                                         '../../Tesoreria/UI/BlotterComprobantes/BlotterComprobantesGrid.js',
	                                                         '../../Tesoreria/UI/bcsFormasDePago/bcsFormasDePago.js'
	                                                         ] } ],

};


BCS_VENTANA.EditarIngresoEgreso = {
	    TITLE : "UI_EDITAR_INGRESO_EGRESO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "695px",
	    VIEW   : '../../Tesoreria/UI/EditarIngresoEgreso/EditarIngresoEgreso.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Tesoreria/UI/EditarIngresoEgreso/EditarIngresoEgreso.js',
	                                                         '../../Tesoreria/UI/BlotterComprobantes/BlotterComprobantesGrid.js',
	                                                         '../../Tesoreria/UI/bcsFormasDePago/bcsFormasDePago.js'
	                                                         ] } ],

};


BCS_VENTANA.BlotterComprobantesTesoreria = {
    TITLE : "UI_GESTION_TESORERIA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1050px",
    HEIGHT : "700px",
    VIEW   : '../../Tesoreria/UI/BlotterComprobantes/BlotterComprobantes.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Tesoreria/UI/BlotterComprobantes/BlotterComprobantes.js',
                                                         '../../Tesoreria/UI/BlotterComprobantes/BlotterComprobantesGrid.js',
                                                         '../../Tesoreria/UI/BlotterComprobantes/ConsultarIngresoEgreso/ConsultarIngresoEgreso.js',
                                                         '../../Tesoreria/UI/BlotterComprobantes/ConsultarComprobantes/ConsultarComprobantes.js',                                                         
                                                         '../../Tesoreria/UI/BlotterComprobantes/ConsultarEnvioDocumentos/ConsultarEnvioDocumentos.js',                                                         
                                                         '../../Tesoreria/UI/BlotterComprobantes/ConsultarDocumentos/ConsultarDocumentos.js' ] } ],

};

BCS_VENTANA.ModalVentanaMensajeResultado = {
	    TITLE : "UI_GESTION_TESORERIA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "600px",
	    HEIGHT : "300px",
	    VIEW   : '../../Tesoreria/UI/BlotterComprobantes/ConsultarComprobantes/ModalVentanaMensajeResultado.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Tesoreria/UI/BlotterComprobantes/ConsultarComprobantes/ModalVentanaMensajeResultado.js'] } ],
};


BCS_VENTANA.EnvioDocumentosDetalle = {
	    TITLE : "UI_ENVIO_DOCUMENTOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "620px",
	    VIEW   : '../../Tesoreria/UI/BlotterComprobantes/ConsultarEnvioDocumentos/EnvioDocumentosDetalle/EnvioDocumentosDetalle.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Tesoreria/UI/BlotterComprobantes/ConsultarEnvioDocumentos/EnvioDocumentosDetalle/EnvioDocumentosDetalle.js' ] } ],

};


BCS_VENTANA.EditarComprobanteFormaDePago = {
	    TITLE : "UI_EDITAR_FORMA_DE_PAGO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "640px",
	    VIEW   : '../../Tesoreria/UI/BlotterComprobantes/EditarComprobanteFormaDePago/EditarComprobanteFormaDePago.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Tesoreria/UI/BlotterComprobantes/EditarComprobanteFormaDePago/EditarComprobanteFormaDePago.js',
	                                                         '../../Tesoreria/UI/bcsFormasDePago/bcsFormasDePago.js' ] } ],

};















BCS_VENTANA.PartidaIngresoEgreso = {
	    TITLE : "UI_PARTIDA_INGRESO_EGRESO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "700px",
	    VIEW   : '../../Tesoreria/UI/BlotterComprobantes/CrearPartidaIngresoEgreso/CrearPartidaIngresoEgreso.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Tesoreria/UI/BlotterComprobantes/CrearPartidaIngresoEgreso/CrearPartidaIngresoEgreso.js',
	                                                         '../../Tesoreria/UI/bcsFormasDePago/bcsFormasDePago.js',
	                                                       ] } ],
};

BCS_VENTANA.GiroEgresos = {
	    TITLE : "UI_PARTIDA_EGRESO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "700px",
	    VIEW   : '../../Tesoreria/UI/BlotterComprobantes/GiroEgresos/GiroEgresos.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Tesoreria/UI/BlotterComprobantes/GiroEgresos/GiroEgresos.js',
	                                                         '../../Tesoreria/UI/bcsFormasDePago/bcsFormasDePago.js'] } ],

};
BCS_VENTANA.AgregarComprobanteGiroEgreso = {
	TITLE : "UI_AGREGAR_COMPROBANTES",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "1050px",
	HEIGHT : "600px",
	VIEW   : '../../Tesoreria/UI/BlotterComprobantes/GiroEgresos/ModalGiroEgresos.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Tesoreria/UI/BlotterComprobantes/GiroEgresos/ModalGiroEgresos.js',
														 '../../Tesoreria/UI/bcsFormasDePago/bcsFormasDePago.js'] } ],
};


BCS_VENTANA.GiroIngreso = {
	    TITLE : "UI_GIRO_INGRESOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "700px",
	    VIEW   : '../../Tesoreria/UI/BlotterComprobantes/GiroIngreso/GiroIngreso.html', 
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Tesoreria/UI/BlotterComprobantes/GiroIngreso/GiroIngreso.js',
                                                            '../../Tesoreria/UI/bcsFormasDePago/bcsFormasDePago.js'] } ],

};
BCS_VENTANA.IngresoDeposito = {
	    TITLE : "UI_INGRESO_DEPOSITOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "500px",
	    VIEW   : '../../Tesoreria/UI/BlotterComprobantes/IngresoDeposito/IngresoDeposito.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Tesoreria/UI/BlotterComprobantes/IngresoDeposito/IngresoDeposito.js' ] } ],

};

BCS_VENTANA.VistaPreviaComprobante = {
	    TITLE : "UI_VISTA_PREVIA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "600px",
	    HEIGHT : "300px",
	    VIEW   : '../../Tesoreria/UI/BlotterComprobantes/VistaPreviaComprobante/VistaPreviaComprobante.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Tesoreria/UI/BlotterComprobantes/VistaPreviaComprobante/VistaPreviaComprobante.js' ] } ],

};

BCS_VENTANA.EdicionMultipleTesoreria = {
	    TITLE : "UI_EDICION_MULTIPLE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "650px",
	    HEIGHT : "450px",
	    VIEW   : '../../Tesoreria/UI/BlotterComprobantes/EdicionMultiple/EdicionMultiple.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Tesoreria/UI/BlotterComprobantes/EdicionMultiple/EdicionMultiple.js',
	                                                         '../../Tesoreria/UI/bcsFormasDePago/bcsFormasDePago.js'] } ],

};



BCS_VENTANA.ComplementarTransaccion = {
	    TITLE : "UI_COMPLEMENTAR_TRANSACCION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "300px",
	    VIEW   : '../../Tesoreria/UI/BlotterGestionContrapartes/ComplementarTransacion/ComplementarTransacion.html',	    
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Tesoreria/UI/BlotterGestionContrapartes/ComplementarTransacion/ComplementarTransacion.js'] } ]
};




BCS_VENTANA.BlotterNominasTransferencias = {
    TITLE : "UI_GESTION_NOMINA_TRANSFERENCIA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "700px",
    VIEW   : '../../Tesoreria/UI/BlotterNominasYTransferencias/BlotterNominasYTransferencias.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Tesoreria/UI/BlotterNominasYTransferencias/BlotterNominasYTransferencias.js',
                                                         '../../Tesoreria/UI/BlotterNominasYTransferencias/ConsultarNominas/ConsultarNominas.js',
                                                         '../../Tesoreria/UI/BlotterNominasYTransferencias/ConsultarTransferencias/ConsultarTransferencias.js' ] }],
    

};


BCS_VENTANA.DetalleNominasTransferencias = {
	    TITLE  : "UI_DETALLE_NOMINA_TRANSFERENCIA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "500px",    
	    VIEW   : '../../Tesoreria/UI/BlotterNominasYTransferencias/DetalleNominasYTransferencias/DetalleNominasYTransferencias.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, 
	    	files: [ '../../Tesoreria/UI/BlotterNominasYTransferencias/DetalleNominasYTransferencias/DetalleNominasYTransferencias.js'
	    	         ] 
		}],
}; 



BCS_VENTANA.MedioDePagoSocioModal = {
    TITLE : "UI_MEDIO_PAGO_SOCIO_NEGOCIO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "500px",
    HEIGHT : "380px",
    VIEW   : '../../Tesoreria/UI/MedioDePagoSocioModal/MedioDePagoSocioModal.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Tesoreria/UI/MedioDePagoSocioModal/MedioDePagoSocioModal.js'] }],
    

};

BCS_VENTANA.IngresoDatosTalonarioChequesModal = {
    TITLE : "UI_INGRESO_DATOS_TALONARIO_CHEQUE",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "350px",
    HEIGHT : "260px",
    VIEW   : '../../Tesoreria/UI/BlotterNominasYTransferencias/IngresoDatosTalonarioChequesModal/IngresoDatosTalonarioChequesModal.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Tesoreria/UI/BlotterNominasYTransferencias/IngresoDatosTalonarioChequesModal/IngresoDatosTalonarioChequesModal.js'] }],
    

};


BCS_VENTANA.GestionContrapartes = {
	    TITLE : "UI_GESTION_CONTRAPARTES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
	    HEIGHT : "650px",
	    VIEW   : '../../Tesoreria/UI/BlotterGestionContrapartes/BlotterGestionContrapartes.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Tesoreria/UI/BlotterGestionContrapartes/BlotterGestionContrapartes.js',
	    													  '../../Tesoreria/UI/BlotterGestionContrapartes/LiquidacionBilateral/LiquidacionBilateral.js',
	                                                          '../../Tesoreria/UI/BlotterGestionContrapartes/ConsultarTransacciones/ConsultarTransacciones.js',	                                                          
	                                                          '../../Tesoreria/UI/BlotterGestionContrapartes/ClearingFinanciero/ClearingFinanciero.js',	                                                          
	                                                          '../../Tesoreria/UI/BlotterGestionContrapartes/ClearingFisico/ClearingFisico.js',	                                                          
	                                                          '../../Tesoreria/UI/BlotterGestionContrapartes/BlotterGestionContrapartesGrid.js'
	                                                       ] }],

};


BCS_VENTANA.ModificarTransaccionTesoreria = {
	    TITLE : "UI_MODIFICAR_TRANSACCION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "650px",
	    HEIGHT : "500px",
	    VIEW   : '../../Tesoreria/UI/BlotterGestionContrapartes/ModificarTransaccion/ModificarTransaccion.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Tesoreria/UI/BlotterGestionContrapartes/ModificarTransaccion/ModificarTransaccion.js'] } ],
	};


BCS_VENTANA.BlotterDepositos = {
	    TITLE : "UI_GESTION_DEPOSITOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "700px",
	    VIEW   : '../../Tesoreria/UI/BlotterDepositos/BlotterDepositos.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Tesoreria/UI/BlotterDepositos/BlotterDepositos.js',
	                                                         '../../Tesoreria/UI/BlotterDepositos/BlotterDepositosGrid.js',] } ],

};


BCS_VENTANA.DetalleNominaDeposito = {
	    TITLE : "UI_DETALLE_NOMINA_DEPOSITO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "500px",
	    VIEW   : '../../Tesoreria/UI/BlotterDepositos/DetalleNominaDeposito/DetalleNominaDeposito.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Tesoreria/UI/BlotterDepositos/DetalleNominaDeposito/DetalleNominaDeposito.js'] } ],

};

BCS_VENTANA.MotivoAnulacion = {
	    TITLE : "UI_MOTIVO_ANULACION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "500px",
	    HEIGHT : "200px",
	    VIEW   : '../../Tesoreria/UI/BlotterDepositos/MotivoAnulacion/MotivoAnulacion.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Tesoreria/UI/BlotterDepositos/MotivoAnulacion/MotivoAnulacion.js'] } ],

};

BCS_VENTANA.CartolaMovimientosTesoreria = {
    TITLE : "UI_CARTOLA_MOVIMIENTOS_TESORERIA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "800px",
    HEIGHT : "670px",
    VIEW   : '../../Tesoreria/UI/ConsultaSaldoBanco/CartolaMovimientosTesoreria/CartolaMovimientosTesoreria.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Tesoreria/UI/ConsultaSaldoBanco/CartolaMovimientosTesoreria/CartolaMovimientosTesoreria.js'] }],

};



BCS_VENTANA.DocumentosTesoreria = {
    TITLE : "UI_MOVIMIENTOS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "680px",
    VIEW   : '../../Tesoreria/UI/BlotterComprobantes/ConsultarDocumentos/ConsultarDocumentos.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Tesoreria/UI/BlotterComprobantes/ConsultarDocumentos/ConsultarDocumentos.js',
                                                         '../../Tesoreria/UI/BlotterComprobantes/BlotterComprobantesGrid.js'] } ],

};

BCS_VENTANA.DetalleComprobanteTesoreria = {
    TITLE : "UI_COMPROBANTE_DE_TESORERIA",
    IMAGE  : "",
    PRINTABLE : true,
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "500px",
    VIEW   : '../../Tesoreria/UI/DetalleComprobanteTesoreria/DetalleComprobanteTesoreria.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Tesoreria/UI/DetalleComprobanteTesoreria/DetalleComprobanteTesoreria.js']
    }],

};

BCS_VENTANA.DetalleIngresoEgresoTesoreria = {
	    TITLE : "UI_INGRESO_EGRESO_DE_TESORERIA",
	    IMAGE  : "",
	    PRINTABLE : true,
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "500px",
	    VIEW   : '../../Tesoreria/UI/DetalleIngresoEgreso/DetalleIngresoEgreso.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Tesoreria/UI/DetalleIngresoEgreso/DetalleIngresoEgreso.js']
	    }],

};




BCS_VENTANA.CargaMasivaTesoreria = {
	    TITLE  : "UI_CARGA_MASIVA_TESORERIA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "500px",    
	    VIEW   : '../../Tesoreria/UI/BlotterCargaMasiva/BlotterCargaMasiva.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, 
	    	files: [ '../../Tesoreria/UI/BlotterCargaMasiva/BlotterCargaMasiva.js',
	    	         '../../Tesoreria/UI/BlotterCargaMasiva/BlotterCargaMasivaGrid.js'
	    	         ] 
		}],

};

BCS_VENTANA.DetalleCargaMasiva = {
	    TITLE  : "UI_DETALLE_CARGA_MASIVA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "500px",    
	    VIEW   : '../../Tesoreria/UI/BlotterCargaMasiva/DetalleCargaMasiva/DetalleCargaMasiva.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, 
	    	files: [ '../../Tesoreria/UI/BlotterCargaMasiva/DetalleCargaMasiva/DetalleCargaMasiva.js',
	    	         '../../Tesoreria/UI/BlotterCargaMasiva/BlotterCargaMasivaGrid.js'
	    	         ] 
		}],
};


BCS_VENTANA.ComplementoCargaMasiva = {
	    TITLE  : "UI_COMPLEMENTO_CARGA_MASIVA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "380px",    
	    VIEW   : '../../Tesoreria/UI/BlotterCargaMasiva/ComplementoCargaMasiva.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, 
	    	files: [ '../../Tesoreria/UI/BlotterCargaMasiva/ComplementoCargaMasiva.js'] 
		}],
};
BCS_VENTANA.RevisionCierreDiario = {
	    TITLE : "UI_REVISION_CIERRE_DIARIO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1028px",
	    HEIGHT : "720px", 
	    VIEW   : '../../Tesoreria/UI/RevisionCierreDiario/RevisionCierreDiario.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, 
	    	  files: ['../../Tesoreria/UI/RevisionCierreDiario/RevisionCierreDiario.js',
	    	          '../../Tesoreria/UI/BlotterComprobantes/BlotterComprobantesGrid.js'
	    	          ]}],

};

BCS_VENTANA.CuadraturaTesoreria = {
		TITLE : "UI_CUADRATURA_TESORERIA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "200px",
	    VIEW   : '../../Tesoreria/UI/CuadraturaTesoreria/CuadraturaTesoreria.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: [ '../../Tesoreria/UI/CuadraturaTesoreria/CuadraturaTesoreria.js']}],
};


// --- MANTENEDORES TESORERIA ---
BCS_VENTANA.TesoreriaMantenedores = {
		SEPARATE: true,
	    TITLE : "UI_MANTENEDORES",
	    ENABLE : true
};


BCS_VENTANA.CajasTesoreria = {
	    TITLE : "UI_MANTENEDOR_CAJA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "600px",
	    VIEW   : '../../Tesoreria/UI/BlotterCaja/BlotterCaja.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, 
	    					files: [ '../../Tesoreria/UI/BlotterCaja/BlotterCaja.js'] 
	    				}],

};

	
BCS_VENTANA.MonedasTesoreria = {
	    TITLE : "UI_MANTENEDOR_MONEDAS_TESORERIA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "750px",
	    HEIGHT : "430px",    
	    VIEW   : '../../Tesoreria/UI/BlotterMonedas/BlotterMonedas.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, 
	    	files: [ '../../Tesoreria/UI/BlotterMonedas/BlotterMonedas.js',
	    	         '../../Tesoreria/UI/BlotterMonedas/BlotterMonedas.js'] 
		}],

};


BCS_VENTANA.BancosTesoreria = {
	    TITLE : "UI_MANTENEDOR_CUENTA_BANCARIA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "600px",    
	    VIEW   : '../../Tesoreria/UI/BlotterCuentaBancaria/BlotterCuentaBancaria.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, 
	    	files: [ '../../Tesoreria/UI/BlotterCuentaBancaria/BlotterCuentaBancaria.js',
	    	         '../../Tesoreria/UI/BlotterCuentaBancaria/BlotterCuentaBancariaGrid.js',
	    	         '../../Tesoreria/UI/BlotterCuentaBancaria/CuentasBancarias/ConsultarCuentasBancarias.js',
	    	         '../../Tesoreria/UI/BlotterCuentaBancaria/Chequeras/ConsultarChequeras.js',
	    	         '../../Tesoreria/UI/BlotterCuentaBancaria/ConfiguracionChequeras/ConsultarConfiguracionChequeras.js'] 
		}],

};


BCS_VENTANA.editarCuentasBancarias = {
		    TITLE : "UI_EDITAR_CUENTA_BANCARIA",
		    IMAGE  : "",
		    ENABLE : true,
		    WIDTH  : "650px",
		    HEIGHT : "570px",	    
		    VIEW   : '../../Tesoreria/UI/BlotterCuentaBancaria/CuentasBancarias/ModalEditarCuentasBancarias.html',
		    PARAMS : {},
		    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Tesoreria/UI/BlotterCuentaBancaria/CuentasBancarias/ModalEditarCuentasBancarias.js']}],

};

BCS_VENTANA.editarChequeras = {
		    TITLE : "UI_EDITAR_CHEQUERAS",
		    IMAGE  : "",
		    ENABLE : true,
		    WIDTH  : "650px",
		    HEIGHT : "450px",	    
		    VIEW   : '../../Tesoreria/UI/BlotterCuentaBancaria/Chequeras/ModalChequeras.html',
		    PARAMS : {},
		    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Tesoreria/UI/BlotterCuentaBancaria/Chequeras/ModalChequeras.js']}],

};

BCS_VENTANA.editarConfiguracionChequeras = {
		    TITLE : "UI_EDITAR_CONFIGURACION_CHEQUERAS",
		    IMAGE  : "",
		    ENABLE : true,
		    WIDTH  : "650px",
		    HEIGHT : "450px",	    
		    VIEW   : '../../Tesoreria/UI/BlotterCuentaBancaria/ConfiguracionChequeras/ModalConfiguracionChequeras.html',
		    PARAMS : {},
		    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Tesoreria/UI/BlotterCuentaBancaria/ConfiguracionChequeras/ModalConfiguracionChequeras.js']}],

};

BCS_VENTANA.AnularCheque = {
	    TITLE : "UI_ANULAR_CHEQUE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "300px",
	    HEIGHT : "220px",
	    VIEW   : '../../Tesoreria/UI/BlotterCuentaBancaria/Chequeras/AnularCheque.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Tesoreria/UI/BlotterCuentaBancaria/Chequeras/AnularCheque.js']}],

};

BCS_VENTANA.ComentarioRechazaAutorizacion = {
	TITLE : "UI_COMENTARIO_RECHAZA_AUTORIZACION",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "400px",
	HEIGHT : "200px",
	VIEW   : '../../Plataforma/UI/AUT/VistaAutorizaciones/ModalRechazarAutorizacion.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Plataforma/UI/AUT/VistaAutorizaciones/ModalRechazarAutorizacion.js']}],

};



BCS_VENTANA.AgregarCajas = {
		    TITLE : "UI_AGREGAR_CAJA",
		    IMAGE  : "",
		    ENABLE : true,
		    WIDTH  : "450px",
		    HEIGHT : "200px",
		    VIEW   : '../../Tesoreria/UI/BlotterCaja/ModalCaja/AgregarCajas.html',
		    PARAMS : {},
		    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Tesoreria/UI/BlotterCaja/ModalCaja/AgregarCajas.js'] }], 

};

BCS_VENTANA.AgregarCajaMoneda = {
		    TITLE : "UI_AGREGAR_MONEDA",
		    IMAGE  : "",
		    ENABLE : true,
		    WIDTH  : "450px",
		    HEIGHT : "220px",
		    VIEW   : '../../Tesoreria/UI/BlotterCaja/ModalMonedas/AgregarCajaMoneda.html',
		    PARAMS : {},
		    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Tesoreria/UI/BlotterCaja/ModalMonedas/AgregarCajaMoneda.js'] 
		    				}],

		    
};

BCS_VENTANA.AgregarCajaUsuario = {
		    TITLE : "UI_AGREGAR_USUARIOS",
		    IMAGE  : "",
		    ENABLE : true,
		    WIDTH  : "450px",
		    HEIGHT : "250px",
		    VIEW   : '../../Tesoreria/UI/BlotterCaja/ModalUsuario/AgregarCajaUsuario.html',
		    PARAMS : {},
		    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Tesoreria/UI/BlotterCaja/ModalUsuario/AgregarCajaUsuario.js'] 
		    				}],

		    
};

BCS_VENTANA.AgregarCajaProducto = {
		    TITLE : "UI_AGREGAR_PRODUCTO_EMPRESA",
		    IMAGE  : "",
		    ENABLE : true,
		    WIDTH  : "400px",
		    HEIGHT : "200px",
		    VIEW   : '../../Tesoreria/UI/BlotterCaja/ModalProducto/AgregarCajaProducto.html',
		    PARAMS : {},
		    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Tesoreria/UI/BlotterCaja/ModalProducto/AgregarCajaProducto.js'] 
		    				}],

};


BCS_VENTANA.ArqueoCaja = {
		    TITLE : "UI_ARQUEO_CAJA",
		    IMAGE  : "",
		    ENABLE : true,
		    WIDTH  : "1050px",
		    HEIGHT : "500px",
		    VIEW   : '../../Tesoreria/UI/BlotterCaja/ArqueoCaja/ArqueoCaja.html',
		    PARAMS : {},
		    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Tesoreria/UI/BlotterCaja/ArqueoCaja/ArqueoCaja.js',
		                                                        '../../Tesoreria/UI/BlotterComprobantes/BlotterComprobantesGrid.js',
		                                                         '../../Tesoreria/UI/BlotterComprobantes/IngresoDeposito/IngresoDeposito.js'] }], 

};

	
// --- CONSULTAS TESORERIA ---
BCS_VENTANA.TesoreriaConsultas = {
		SEPARATE: true,
	    TITLE : "UI_CONSULTAS",
	    ENABLE : true
};



BCS_VENTANA.ConsultaSaldoBanco = {
	    TITLE : "UI_CONSULTA_SALDO_BANCO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "700px",
	    VIEW   : '../../Tesoreria/UI/ConsultaSaldoBanco/ConsultaSaldoBanco.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Tesoreria/UI/ConsultaSaldoBanco/ConsultaSaldoBanco.js'] } ],

};



BCS_VENTANA.CuadraturaComprobantesCuentaInversion = {
	    TITLE : "UI_CUADRATURA_COMPROBANTES_CUENTAINVERSION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "950px",
	    HEIGHT : "700px",
	    VIEW   : '../../Tesoreria/UI/CuadraturaComprobantesCuentaInversion/CuadraturaComprobantesCuentaInversion.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Tesoreria/UI/CuadraturaComprobantesCuentaInversion/CuadraturaComprobantesCuentaInversion.js'] } ],

};



BCS_VENTANA.LibretaBanco = {
	    TITLE : "UI_LIBRETA_BANCO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "670px",
	    VIEW   : '../../Tesoreria/UI/ConsultaSaldoBanco/LibretaBanco/LibretaBanco.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Tesoreria/UI/ConsultaSaldoBanco/LibretaBanco/LibretaBanco.js'] }],

};	

 

BCS_VENTANA.GenerarCartaBanco = {
	    TITLE : "UI_GENERAR_CARTA_BANCO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "600px",
	    HEIGHT : "250px",
	    VIEW   : '../../Tesoreria/UI/BlotterNominasYTransferencias/GenerarCartaBanco/GenerarCartaBanco.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Tesoreria/UI/BlotterNominasYTransferencias/GenerarCartaBanco/GenerarCartaBanco.js' ] } ],

};
 



//---------------------------------------------
//MESA RF
//---------------------------------------------
BCS_VENTANA.MODULO_MESARF= {
    TITLE : "UI_RENTA_FIJA_E_IIF",
    IMAGE  : "sapUiIcon column-chart-dual-axis",
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "400px",
    VIEW   : '',
    PARAMS : {},
    DEPENDENCIES : [],

};

BCS_VENTANA.MesaRentaFija= {
	    TITLE : "UI_GESTION_MESA_RENTA_FIJA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1135px",
	    HEIGHT : "805px",
	    VIEW   : '../../Mesa/UI/MRF/BlotterMesa/BlotterMesaRF.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Mesa/UI/MRF/BlotterMesa/BlotterMesaRF.js',
	                                                       '../../Mesa/UI/ModuloMesaRFConfGrids.js',
	                                                       //'../../Mesa/UI/MRF/BlotterMesa/BlotterMesaRFGrid.js',
	                                                       '../../Mesa/UI/MRF/BlotterMesa/ConsultaOperacionesMesa/ConsultaOperacionesMesaRF.js',
	                                                       '../../Mesa/UI/MRF/BlotterMesa/ConsultaVencimientosOperacionesMesa/ConsultaVencimientosOperacionesMesaRF.js',
	                                                       '../../Mesa/UI/MRF/BlotterMesa/ConsultaAnticiposOperacionesMesa/ConsultaAnticiposOperacionesMesaRF.js',
	                                                       '../../Mesa/UI/MRF/BlotterMesa/ConsultaReasignacionOperacionesMesa/ConsultaReasignacionOperacionesMesaRF.js'] }],	                                                       
//	                                                       '../../Mesa/UI/ModuloMesaRFConfGrids.js'] }],

};

BCS_VENTANA.ResumenMRF= {
	    TITLE : "UI_GESTION_MESA_RENTA_FIJA_RESUMEN",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "350px",
	    HEIGHT : "255px",
	    VIEW   : '../../Mesa/UI/MRF/BlotterMesa/ResumenMRF/ResumenMRF.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Mesa/UI/MRF/BlotterMesa/ResumenMRF/ResumenMRF.js'] }],

};

BCS_VENTANA.ConsultaCarteraMesaRentaFija= {
	    TITLE : "UI_CARTERA_RENTA_FIJA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "700px",
	    VIEW   : '../../Mesa/UI/MRF/ConsultaCartera/ConsultarCarteraMesaRF.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Mesa/UI/MRF/ConsultaCartera/ConsultarCarteraMesaRF.js',
	                                                       '../../Mesa/UI/ModuloMesaRFConfGrids.js'] }],

};

// MSH
// --------------------------
BCS_VENTANA.CarteraReservada= {
	    TITLE : "UI_CARTERA_RESERVADA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "700px",
	    VIEW   : '../../Mesa/UI/MRF/Reserva/BlotterReserva.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Mesa/UI/MRF/Reserva/BlotterReserva.js'] }],

};



BCS_VENTANA.ReservaCartera= {
	    TITLE : "UI_CARTERA_RESERVADA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "610px",
	    HEIGHT : "260px",
	    VIEW   : '../../Mesa/UI/MRF/Reserva/ReservaCartera.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Mesa/UI/MRF/Reserva/ReservaCartera.js'] }],

};


//MSH
//--------------------------
BCS_VENTANA.CarteraConsolidada= {
	    TITLE : "UI_CARTERA_CONSOLIDADA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "559px",
	    VIEW   : '../../Mesa/UI/MRF/CarteraConsolidada/CarteraConsolidada.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Mesa/UI/MRF/CarteraConsolidada/CarteraConsolidada.js'] }],

};



//MSH
//--------------------------
BCS_VENTANA.PosicionMRF= {
	    TITLE : "UI_POSICION_MRF",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "625px",
	    HEIGHT : "595px",
	    VIEW   : '../../Mesa/UI/MRF/PosicionMRF/PosicionMRF.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Mesa/UI/MRF/PosicionMRF/PosicionMRF.js'] }],

};

BCS_VENTANA.PosicionInicialMRF= {
	    TITLE : "UI_POSICION_INICIAL_MRF",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "625px",
	    HEIGHT : "595px",
	    VIEW   : '../../Mesa/UI/MRF/PosicionInicialMRF/PosicionInicialMRF.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Mesa/UI/MRF/PosicionInicialMRF/PosicionInicialMRF.js'] }],

};


BCS_VENTANA.DetallePosicionInicialMRF= {
	    TITLE : "UI_DETALLE_POSICION_MRF_INICIAL",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "700px",
	    HEIGHT : "500px",
	    VIEW   : '../../Mesa/UI/MRF/DetallePosicionInicialMRF/DetallePosicionInicialMRF.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Mesa/UI/MRF/DetallePosicionInicialMRF/DetallePosicionInicialMRF.js'] }],

};

BCS_VENTANA.DetallePosicionMRF= {
	    TITLE : "UI_DETALLE_POSICION_MRF",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "700px",
	    HEIGHT : "500px",
	    VIEW   : '../../Mesa/UI/MRF/DetallePosicionMRF/DetallePosicionMRF.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Mesa/UI/MRF/DetallePosicionMRF/DetallePosicionMRF.js'] }],

};
				

BCS_VENTANA.IngresoOperacionMesaRF = {
	    TITLE : "UI_INGRESO_OPERACION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "800px",
	    VIEW   : '../../Mesa/UI/MRF/GestionOperacion/IngresoOperacionMesaRF.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/MRF/GestionOperacion/IngresoOperacionMesaRF.js',
															'../../Mesa/UI/bcs-forma-pago-mrf/bcs-forma-pago-mrf.js'] } ],

};

BCS_VENTANA.IngresoNominalMesaRF = {
    TITLE : "UI_NOMINAL",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "400px",
    HEIGHT : "460px",
    VIEW   : '../../Mesa/UI/MRF/GestionOperacion/IngresoNominalesMesaRF.html',
    PARAMS : { IdDetalle : 0 },
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Mesa/UI/MRF/GestionOperacion/IngresoNominalesMesaRF.js'] }],

};

BCS_VENTANA.CarteraMesaRF = {
	    TITLE : "UI_CARTERA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "540px",
	    VIEW   : '../../Mesa/UI/MRF/GestionOperacion/CarteraMesaRF.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Mesa/UI/MRF/GestionOperacion/CarteraMesaRF.js'] }],

	};

BCS_VENTANA.IngresoCarteraMesaRF = {
	    TITLE : "UI_CARTERA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "480px",
	    VIEW   : '../../Mesa/UI/MRF/GestionOperacion/IngresoCarteraMesaRF.html',
	    PARAMS : { IdDetalle : 0 },
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Mesa/UI/MRF/GestionOperacion/IngresoCarteraMesaRF.js'] }],

	};

BCS_VENTANA.InformeCarteraPropia = {
    TITLE : "UI_INFORME_CARTERA_PROPIA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "450px",
    HEIGHT : "auto",
    VIEW   : '../../Mesa/UI/MRF/Informes/InformeCarteraPropia.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Mesa/UI/MRF/Informes/InformeCarteraPropia.js' ] } ],

};

BCS_VENTANA.InformeCarteraPactosValorizadaCuentaPropia = {
	TITLE : "UI_INFORME_CARTERA_PACTOS_VALORIZADA",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "450px",
	HEIGHT : "auto",
	VIEW   : '../../Mesa/UI/MRF/InformesCuentaPropia/InformeCarteraPactosValorizadaCuentaPropia.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Mesa/UI/MRF/InformesCuentaPropia/InformeCarteraPactosValorizadaCuentaPropia.js' ] } ],

};

BCS_VENTANA.ContratoCondicionesGenerales = {
    TITLE : "UI_MRF_CCG_CONTRATO_CONDICIONES_GENERALES",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "450px",
    HEIGHT : "auto",
    VIEW   : '../../Mesa/UI/MRF/ContratoCondicionesGenerales/CondicionesGenerales.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Mesa/UI/MRF/ContratoCondicionesGenerales/CondicionesGenerales.js' ] } ],

};
	
	
BCS_VENTANA.DetalleOperacionMesaRF = {
    TITLE : "UI_INFORMACION_OPERACION",
    IMAGE  : "",
    ENABLE : true,
    PRINTABLE : true,
    WIDTH  : "1450px",
	HEIGHT : "770px",
    VIEW   : '../../Mesa/UI/MRF/GestionOperacion/DetalleOperacionMesaRF.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/MRF/GestionOperacion/DetalleOperacionMesaRF.js'] } ],

};

BCS_VENTANA.ModificaOperacionMesaRF = {
	    TITLE : "UI_OPERACION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/MRF/GestionOperacion/ModificaOperacionMesaRF.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/MRF/GestionOperacion/ModificaOperacionMesaRF.js' ] } ],

};

BCS_VENTANA.ClasificacionCartera = {
	    TITLE : "UI_CLASIFICACION_CARTERA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "300px",
	    HEIGHT : "300px",
	    VIEW   : '../../Mesa/UI/MRF/IngresoOperacion/ClasificacionCartera.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/MRF/IngresoOperacion/ClasificacionCartera.js' ] } ],

};

BCS_VENTANA.CargaMasivaMesa = {
	    TITLE  : "UI_CARGA_MASIVA_MESA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "510px",    
	    VIEW   : '../../Mesa/UI/BlotterCargaMasiva/BlotterCargaMasiva.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, 
	    	files: [ '../../Mesa/UI/BlotterCargaMasiva/BlotterCargaMasiva.js'] 
		}],

};

BCS_VENTANA.DetalleCargaOperacionTermino = {
	    TITLE  : "UI_DETALLE_CARGA_MASIVA_OPERACIONES_TERMINO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "500px",    
	    VIEW   : '../../Mesa/UI/BlotterCargaMasiva/Detalles/DetalleCargaOperacionTermino.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, 
	    	files: [ '../../Mesa/UI/BlotterCargaMasiva/Detalles/DetalleCargaOperacionTermino.js'] 
		}],

};

BCS_VENTANA.CargaMasivaDepositos= {
    TITLE : "UI_CARGA_MASIVA_DE_DEPOSITOS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1000px",
    HEIGHT : "700px",
    VIEW   : '../../Mesa/UI/MRF/CargaMasivaDepositos/CargaMasivaDepositos.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Mesa/UI/MRF/CargaMasivaDepositos/CargaMasivaDepositos.js',
                                                       ] } ],	                  

};



BCS_VENTANA.CargaMasivaOperaciones= {
	    TITLE : "UI_CARGA_MASIVA_DE_OPERACIONES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "700px",
	    VIEW   : '../../Mesa/UI/MRF/CargaMasivaOperacion/CargaMasivaOperaciones.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Mesa/UI/MRF/CargaMasivaOperacion/CargaMasivaOperaciones.js',
	                                                       ] } ],	                  

};



BCS_VENTANA.CierreMesa= {
    TITLE : "UI_CIERRE_Y_APERTURA_DE_MESA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "600px",
    VIEW   : '../../Mesa/UI/CierreMesa/CierreMesa.html',
    UNIQUE : false,
    PARAMS : {modulo: 'MERF'},
    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Mesa/UI/CierreMesa/CierreMesa.js',
                                                       ] } ],	                  

};

BCS_VENTANA.CierreMesaSpot= {
	    TITLE : "UI_CIERRE_Y_APERTURA_DE_MESA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/CierreMesa/CierreMesa.html',
	    UNIQUE : false,
	    PARAMS : {modulo: 'SPOT'},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Mesa/UI/CierreMesa/CierreMesa.js',
	                                                       ] } ],	                  

};

BCS_VENTANA.CierreMesaForward= {
	    TITLE : "UI_CIERRE_Y_APERTURA_DE_MESA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "600px",
	    VIEW   : '../../Mesa/UI/CierreMesa/CierreMesa.html',
	    UNIQUE : false,
	    PARAMS : {modulo: 'FWD'},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Mesa/UI/CierreMesa/CierreMesa.js',
	                                                       ] } ],	                  

};



BCS_VENTANA.MantenedorMesas = {
    TITLE : "UI_MANTENEDOR_MESAS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1000px",
    HEIGHT : "730px",
    VIEW   : '../../Mesa/UI/MantenedorMesas/MantenedorMesas.html',
    PARAMS : {modulo: 'MERF'},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/MantenedorMesas/MantenedorMesas.js',
                                                         '../../Mesa/UI/MantenedorMesas/PanelMesas.js',
                                                         '../../Mesa/UI/MantenedorMesas/PanelOperadoresMesa.js',
                                                         '../../Mesa/UI/MantenedorMesas/PanelFormaPagoMesa.js',
                                                         '../../Mesa/UI/ModuloMesaRFConfGrids.js'
                                                         ] } ],

};

BCS_VENTANA.MantenedorMesaSpot = {
    TITLE : "UI_MANTENEDOR_MESAS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1000px",
    HEIGHT : "auto",
    VIEW   : '../../Mesa/UI/MantenedorMesas/MantenedorMesas.html',
    PARAMS : {modulo: 'SPOT'},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/MantenedorMesas/MantenedorMesas.js',
                                                         '../../Mesa/UI/MantenedorMesas/PanelMesas.js',
                                                         '../../Mesa/UI/MantenedorMesas/PanelOperadoresMesa.js',
                                                         '../../Mesa/UI/MantenedorMesas/PanelFormaPagoMesa.js',
                                                         '../../Mesa/UI/ModuloMesaRFConfGrids.js'
                                                         ] } ],

};

BCS_VENTANA.MantenedorMesaForward = {
    TITLE : "UI_MANTENEDOR_MESAS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1000px",
    HEIGHT : "731px",
    VIEW   : '../../Mesa/UI/MantenedorMesas/MantenedorMesas.html',
    PARAMS : {modulo: 'FWD'},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/MantenedorMesas/MantenedorMesas.js',
                                                         '../../Mesa/UI/MantenedorMesas/PanelMesas.js',
                                                         '../../Mesa/UI/MantenedorMesas/PanelOperadoresMesa.js',
                                                         '../../Mesa/UI/MantenedorMesas/PanelFormaPagoMesa.js',
                                                         '../../Mesa/UI/ModuloMesaRFConfGrids.js'
                                                         ] } ],

};

BCS_VENTANA.ModalMantenedorFormaPagoMesa = {
    TITLE : "UI_FORMA_PAGO_MESA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "450px",
    HEIGHT : "auto",
    VIEW   : '../../Mesa/UI/MantenedorMesas/ModalMantenedorFormaPagoMesa.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Mesa/UI/MantenedorMesas/ModalMantenedorFormaPagoMesa.js' ] } ],

};

BCS_VENTANA.ModalMantenedorMesas = {
    TITLE : "UI_AGREGAR_MODIFICAR_MESA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "450px",
    HEIGHT : "auto",
    VIEW   : '../../Mesa/UI/MantenedorMesas/ModalMantenedorMesas.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Mesa/UI/MantenedorMesas/ModalMantenedorMesas.js' ] } ],

};


BCS_VENTANA.ModalOperadoresMesa = {
    TITLE : "UI_AGREGAR_MODIFICAR_AGENTE_OPERADOR_MESA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "450px",
    HEIGHT : "auto",
    VIEW   : '../../Mesa/UI/MantenedorMesas/ModalOperadoresMesa.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Mesa/UI/MantenedorMesas/ModalOperadoresMesa.js' ] } ],

};


BCS_VENTANA.InformeOperacionesDiariasPactos = {
    TITLE : "UI_INFORME_OPERACIONES_DIARIAS_PACTOS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1165px",
    HEIGHT : "auto",
    VIEW   : '../../Mesa/UI/InformeOperacionesDiariasPactos/InformeOperacionesDiariasPactos.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/InformeOperacionesDiariasPactos/InformeOperacionesDiariasPactos.js' ] } ],

};

BCS_VENTANA.InformeMovimientosDiarios = {
	TITLE : "UI_INFORME_MOVIMIENTOS_DIARIOS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1000px",
    HEIGHT : "auto",
    VIEW   : '../../Mesa/UI/InformeMovimientosDiarios/InformeMovimientosDiarios.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/InformeMovimientosDiarios/InformeMovimientosDiarios.js' ] } ],
	
};

BCS_VENTANA.ConsultaPlazosFechas = {
    TITLE : "UI_CONSULTA_PLAZOS_FECHAS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "600px",
    HEIGHT : "700px",
    VIEW   : '../../Mesa/UI/ConsultaPlazosFechas/ConsultaPlazosFechas.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/ConsultaPlazosFechas/ConsultaPlazosFechas.js' ] } ],

};



BCS_VENTANA.MantenedoresPosicionMRF = {
    TITLE : "UI_MANTENEDORES_POSICION_MRF",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1000px",
    HEIGHT : "auto",
    VIEW   : '../../Mesa/UI/MantenedoresPosicionMRF/MantenedoresPosicionMRF.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/MantenedoresPosicionMRF/MantenedoresPosicionMRF.js',
                                                         '../../Mesa/UI/MantenedoresPosicionMRF/PanelMetodoPosicion.js',
                                                         '../../Mesa/UI/MantenedoresPosicionMRF/PanelTipoOperacionPosicion.js',
                                                         '../../Mesa/UI/MantenedoresPosicionMRF/PanelEventoPosicion.js',
                                                         '../../Mesa/UI/MantenedoresPosicionMRF/PanelMetodoEventoPosicion.js'
                                                         ] } ],

};

BCS_VENTANA.ConsultaDepositosyAutodesembolsos = {
	    TITLE : "UI_CONSULTA_DEPOSITOS_Y_AUTODESEMBOLSOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "490px",
	    HEIGHT : "138px",
	    VIEW   : '../../Mesa/UI/MRF/ConsultaDepositosYAutodesembolsos/ConsultaDepositosYAutodesembolsos.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Mesa/UI/MRF/ConsultaDepositosYAutodesembolsos/ConsultaDepositosYAutodesembolsos.js'] }],
};

BCS_VENTANA.ModalMetodoPosicion = {
    TITLE : "UI_MODAL_METODO_POSICION",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "450px",
    HEIGHT : "auto",
    VIEW   : '../../Mesa/UI/MantenedoresPosicionMRF/ModalMetodoPosicion.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Mesa/UI/MantenedoresPosicionMRF/ModalMetodoPosicion.js' ] } ],

};

BCS_VENTANA.ModalTipoOperacionPosicion = {
    TITLE : "UI_MODAL_TIPO_OPERACION_POSICION",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "450px",
    HEIGHT : "auto",
    VIEW   : '../../Mesa/UI/MantenedoresPosicionMRF/ModalTipoOperacionPosicion.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Mesa/UI/MantenedoresPosicionMRF/ModalTipoOperacionPosicion.js' ] } ],

};

BCS_VENTANA.ModalEventoPosicion = {
    TITLE : "UI_MODAL_EVENTO_POSICION",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "450px",
    HEIGHT : "auto",
    VIEW   : '../../Mesa/UI/MantenedoresPosicionMRF/ModalEventoPosicion.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Mesa/UI/MantenedoresPosicionMRF/ModalEventoPosicion.js' ] } ],

};

BCS_VENTANA.ModalMetodoEventoPosicion = {
    TITLE : "UI_MODAL_METODO_EVENTO_POSICION",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "450px",
    HEIGHT : "auto",
    VIEW   : '../../Mesa/UI/MantenedoresPosicionMRF/ModalMetodoEventoPosicion.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Mesa/UI/MantenedoresPosicionMRF/ModalMetodoEventoPosicion.js' ] } ],

};

BCS_VENTANA.MantenedorItemsManualesPosicionMRF = {
    TITLE : "UI_MANTENEDOR_ITEMS_MANUALES_POSICION_MRF",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1000px",
    HEIGHT : "auto",
    VIEW   : '../../Mesa/UI/MantenedoresPosicionMRF/MantenedorItemsManualesPosicionMRF.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/MantenedoresPosicionMRF/MantenedorItemsManualesPosicionMRF.js' ] } ],

};


BCS_VENTANA.ModalItemsManualesPosicionMRF = {
    TITLE : "UI_MODAL_ITEM_MANUAL_POSICION_MRF",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "450px",
    HEIGHT : "auto",
    VIEW   : '../../Mesa/UI/MantenedoresPosicionMRF/ModalItemsManualesPosicionMRF.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Mesa/UI/MantenedoresPosicionMRF/ModalItemsManualesPosicionMRF.js' ] } ],

};
	
BCS_VENTANA.CoberturaPatrimonial = {
	    TITLE : "UI_COBERTURA_PATRIMONIAL",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "350px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/MRF/CoberturaPatrimonial/CoberturaPatrimonial.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/MRF/CoberturaPatrimonial/CoberturaPatrimonial.js' ] } ],

};

BCS_VENTANA.QuincenalDePactosMRF = {
		    TITLE : "UI_QUINCENAL_DE_PACTOS",
		    IMAGE  : "",
		    ENABLE : true,
		    WIDTH  : "700px",
		    HEIGHT : "auto",
		    VIEW   : '../../Mesa/UI/MRF/QuincenalDePactos/QuincenalDePactos.html',
		    PARAMS : {},
		    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/MRF/QuincenalDePactos/QuincenalDePactos.js' ] } ],

};
//Deposito (Peru)
BCS_VENTANA.IngresoDepositosDesembolsosMesaRF = {
	    TITLE : "UI_DEPOSITO_DESEMBOLSO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "320px",
	    VIEW   : '../../Mesa/UI/MRF/GestionDepositosDesembolsos/IngresoDepositoDesembolsoMesaRF.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/MRF/GestionDepositosDesembolsos/IngresoDepositoDesembolsoMesaRF.js'
	    													,'../../Mesa/UI/ModuloMesaRFConfGrids.js'] } ],
	    

};

//---------------------------------------------
// Indices de Liquidez y Solvencia
//---------------------------------------------

//TODO agregar un icono al menu de indices
BCS_VENTANA.MODULO_ILS= {
	    TITLE : "UI_ILS",
	    IMAGE  : "sapUiIcon column-chart-dual-axis",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "400px",
	    VIEW   : '',
	    PARAMS : {},
	    DEPENDENCIES : [],

};


BCS_VENTANA.ILSMantenedor = {
	    TITLE : "UI_ILS_MANTENEDOR_ILS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1130px",
	    HEIGHT : "705px",
	    VIEW   : '../../Normativa/UI/ILS/MantenedorILS.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/ILS/MantenedorILS.js',
	                                                       '../../Normativa/UI/ILS/PanelConceptos.js',
	                                                       '../../Normativa/UI/ILS/PanelDetalleItem.js',
	                                                       '../../Normativa/UI/ILS/PanelDetalleCobertura.js',	                                                       
	                                                       '../../Normativa/UI/ILS/PanelItems.js',
	                                                       '../../Normativa/UI/ILS/PanelResultado.js',
	                                                       '../../Normativa/UI/ILS/PanelExposicionCPRO.js',
	                                                       '../../Normativa/UI/ILS/PanelPosicionesDescubiertas.js',
	                                                       '../../Normativa/UI/ILS/PanelDetalleSaldoCuentaContable.js',
	                                                       ] } ],

};


BCS_VENTANA.ILSMantenedorDigitado = {
	    TITLE : "UI_ILS_MANTENEDOR_DIGITADO",
	    IMAGE  : "",
	    ENABLE : false,
	    WIDTH  : "1000px",
	    HEIGHT : "550px",
	    VIEW   : '../../Normativa/UI/ILS/MantenedorDigitado/MantenedorDigitado.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/ILS/MantenedorDigitado/MantenedorDigitado.js'] } ],

};

BCS_VENTANA.MantenedorDigitado = {
	    TITLE : "UI_ILS_MANTENEDOR_DIGITADO",
	    IMAGE  : "",
	    ENABLE : false,
	    WIDTH  : "1000px",
	    HEIGHT : "550px",
	    VIEW   : '../../Normativa/UI/ILS/MantenedorDigitado.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/ILS/MantenedorDigitado.js'] } ],

};

BCS_VENTANA.ILSMantenedorReporteResumido = {
	    TITLE : "UI_ILS_MANTENEDOR_REPORTE_RESUMIDO",
	    IMAGE  : "",
	    ENABLE : false,
	    WIDTH  : "850px",
	    HEIGHT : "650px",
	    VIEW   : '../../Normativa/UI/ILS/MantenedorReporteILS/MantenedorReporteResumidoILS.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/ILS/MantenedorReporteILS/MantenedorReporteResumidoILS.js'] } ],

};

BCS_VENTANA.ILSMantenedorReporteDetallado = {
	    TITLE : "UI_ILS_MANTENEDOR_REPORTE_DETALLADO",
	    IMAGE  : "",
	    ENABLE : false,
	    WIDTH  : "850px",
	    HEIGHT : "650px",
	    VIEW   : '../../Normativa/UI/ILS/MantenedorReporteILS/MantenedorReporteDetalladoILS.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/ILS/MantenedorReporteILS/MantenedorReporteDetalladoILS.js'] } ],

};

BCS_VENTANA.ILSMantenedorConcepto = {
	    TITLE : "UI_ILS_MANTENEDOR_CONCEPTO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "668px",
	    VIEW   : '../../Normativa/UI/ILS/MantenedorConcepto/MantenedorConcepto.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/ILS/MantenedorConcepto/MantenedorConcepto.js'] } ],

};


BCS_VENTANA.AgregarConcepto = {
	    TITLE : "UI_ILS_AGREGAR_CONCEPTO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "450px",
	    HEIGHT : "160px",
	    VIEW   : '../../Normativa/UI/ILS/MantenedorConcepto/ModalConcepto/AgregarConcepto.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Normativa/UI/ILS/MantenedorConcepto/ModalConcepto/AgregarConcepto.js'] }], 

};

BCS_VENTANA.ILSMantenedorIndice = {
	    TITLE : "UI_ILS_MANTENEDOR_INDICE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "550px",
	    VIEW   : '../../Normativa/UI/ILS/MantenedorIndice/MantenedorIndice.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/ILS/MantenedorIndice/MantenedorIndice.js'] } ],

};


BCS_VENTANA.AgregarIndice = {
	    TITLE : "UI_ILS_AGREGAR_INDICE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "450px",
	    HEIGHT : "170px",
	    VIEW   : '../../Normativa/UI/ILS/MantenedorIndice/ModalIndice/AgregarIndice.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Normativa/UI/ILS/MantenedorIndice/ModalIndice/AgregarIndice.js'] }], 

};

BCS_VENTANA.ILSMantenedorInforme = {
	    TITLE : "UI_ILS_MANTENEDOR_REPORTE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "550px",
	    VIEW   : '../../Normativa/UI/ILS/MantenedorInforme/MantenedorInforme.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/ILS/MantenedorInforme/MantenedorInforme.js'] } ],

};


BCS_VENTANA.AgregarInforme = {
	    TITLE : "UI_ILS_AGREGAR_INFORME",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "450px",
	    HEIGHT : "145px",
	    VIEW   : '../../Normativa/UI/ILS/MantenedorInforme/ModalInforme/AgregarInforme.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Normativa/UI/ILS/MantenedorInforme/ModalInforme/AgregarInforme.js'] }], 

};

BCS_VENTANA.ILSMantenedorItem = {
	    TITLE : "UI_ILS_MANTENEDOR_ITEM",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "719px",
	    VIEW   : '../../Normativa/UI/ILS/MantenedorItem/MantenedorItem.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/ILS/MantenedorItem/MantenedorItem.js'] } ],

};


BCS_VENTANA.AgregarItem = {
	    TITLE : "UI_ILS_AGREGAR_ITEM",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "450px",
	    HEIGHT : "200px",
	    VIEW   : '../../Normativa/UI/ILS/MantenedorItem/ModalItem/AgregarItem.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Normativa/UI/ILS/MantenedorItem/ModalItem/AgregarItem.js'] }], 

};


//---------------------------------------------
//MESA SPOT
//---------------------------------------------
BCS_VENTANA.MODULO_MESASPOT= {
TITLE : "UI_SPOT",
IMAGE  : "sapUiIcon citizen-connect",
ENABLE : true,
WIDTH  : "900px",
HEIGHT : "400px",
VIEW   : '',
PARAMS : {},
DEPENDENCIES : [],

};


BCS_VENTANA.MesaSpot= {
	    TITLE : "UI_INGRESO_SPOT",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "814px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/SPT/IngresarSpot/IngresarSpot.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/SPT/IngresarSpot/IngresarSpot.js'
													    	,'../../Mesa/UI/ModuloMesaRFConfGrids.js'
													    	,'../../Mesa/UI/bcs-forma-pago-mesa/bcs-forma-pago-mesa.js'] } ],

};

BCS_VENTANA.BlotterCargaMasivaOperacionSpot = {
	TITLE  : "UI_CARGA_MASIVA_OPERACIONES_SPOT",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "900px",
	HEIGHT : "500px",    
	VIEW   : '../../Mesa/UI/SPT/BlotterCargaMasivaOperacionSpot/BlotterCargaMasivaOperacionSpot.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, 
		files: [ '../../Mesa/UI/SPT/BlotterCargaMasivaOperacionSpot/BlotterCargaMasivaOperacionSpot.js',
				 '../../Mesa/UI/SPT/BlotterCargaMasivaOperacionSpot/BlotterCargaMasivaOperacionSpotGrid.js'
				 ] 
	}],

};

BCS_VENTANA.DetalleCargaMasivaOperacionSpot = {
	TITLE  : "UI_DETALLE_CARGA_MASIVA_OPERACIONES_SPOT",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "1200px",
	HEIGHT : "500px",    
	VIEW   : '../../Mesa/UI/SPT/BlotterCargaMasivaOperacionSpot/DetalleBlotterCargaMasivaOperacionSpot/DetalleCargaMasivaOperacionSpot.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, 
		files: [ '../../Mesa/UI/SPT/BlotterCargaMasivaOperacionSpot/DetalleBlotterCargaMasivaOperacionSpot/DetalleCargaMasivaOperacionSpot.js',
				 '../../Mesa/UI/SPT/BlotterCargaMasivaOperacionSpot/BlotterCargaMasivaOperacionSpotGrid.js'
				 ] 
	}],

};

BCS_VENTANA.ProcesarCargaMasivaOperacionSpotModal = {
	TITLE  : "UI_PROCESAR_CARGA_MASIVA_OPERACIONES_SPOT",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "400px",
	HEIGHT : "150px",    
	VIEW   : '../../Mesa/UI/SPT/BlotterCargaMasivaOperacionSpot/DetalleBlotterCargaMasivaOperacionSpot/ProcesarCargaMasivaOperacionSpot.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, 
		files: [ '../../Mesa/UI/SPT/BlotterCargaMasivaOperacionSpot/DetalleBlotterCargaMasivaOperacionSpot/ProcesarCargaMasivaOperacionSpot.js'
				 ] 
	}],
};

BCS_VENTANA.consutaOperacionSpot= {
	    TITLE : "UI_OPERACION_SPOT",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "814px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/SPT/IngresarSpot/IngresarSpot.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/SPT/IngresarSpot/IngresarSpot.js'
	    													,'../../Mesa/UI/ModuloMesaRFConfGrids.js'
	    													,'../../Mesa/UI/bcs-forma-pago-mesa/bcs-forma-pago-mesa.js'] } ],
};


BCS_VENTANA.PuntaSpot= {
	    TITLE : "UI_MANTENEDOR_PUNTA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "814px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/SPT/MantenedorPunta/MantenedorPunta.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/SPT/MantenedorPunta/MantenedorPunta.js' ]} ],

};

BCS_VENTANA.ModificarSpotFondoMutuo = {
	    TITLE : "UI_TC_CIERRE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "320px",
	    HEIGHT : "270px",
	    VIEW   : '../../Mesa/UI/ConsultaDeOperaciones/ModificarSpotFondoMutuo/ModificarSpotFondoMutuo.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/ConsultaDeOperaciones/ModificarSpotFondoMutuo/ModificarSpotFondoMutuo.js',
	                                                         '../../Mesa/UI/ConsultaDeOperaciones/ModificarSpotFondoMutuo/Paneles/PanelNomina.js'] } ],

};

 
BCS_VENTANA.MesaMonedaPorProductoSpot= {
	    TITLE : "UI_MONEDAS_POR_PRODUCTO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/MantenedorMonedaPorProducto/MantenedorMonedaPorProducto.html',
	    PARAMS : {idTipoProducto: 'MEME'},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/MantenedorMonedaPorProducto/MantenedorMonedaPorProducto.js' ]} ],

};


BCS_VENTANA.ConsultaOperaciones= {
	    TITLE : "UI_CONSULTA_OPERACIONES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1050px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/ConsultaDeOperaciones/ConsultaDeOperaciones2.html',
	    PARAMS : {tipoFlujo: 'Spot'},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/ConsultaDeOperaciones/ConsultaDeOperaciones2.js',
	    													 '../../Mesa/UI/ConsultaDeOperaciones/ConsultarMovimientos/ConsultarMovimientos.js',
	    													 '../../Mesa/UI/ConsultaDeOperaciones/ConsultarCartera/ConsultarCartera.js',
	    													 '../../Mesa/UI/ConsultaDeOperaciones/ConsultarVencimiento/ConsultarVencimiento.js' ]} ],

};

BCS_VENTANA.ConsultaOperacionesModificarFecha = {
	    TITLE : "UI_CREAR_MOVIMIENTOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "380px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/ConsultaDeOperaciones/ConsultarMovimientos/ConsultarMovimientoModificarFecha.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/ConsultaDeOperaciones/ConsultarMovimientos/ConsultarMovimientoModificarFecha.js'] } ],
};


BCS_VENTANA.RUTA_DATATEC= {
	    TITLE : "UI_RUTA_DATATEC",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "150px",
	    VIEW   : '../../Mesa/UI/SPT/RutaDatatec/RutaDatatec.html',
	    PARAMS : {codigoParametro: 'CODRUTADATATEC', nombreParametro: 'UI_RUTA_ARCHIVO_DATATEC'},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/SPT/RutaDatatec/RutaDatatec.js' ]} ],

};


BCS_VENTANA.RESULTADODEOPERACIONES= {
	    TITLE : "UI_RESULTADO_OPERACIONES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/SPT/ResultadoOperaciones/ResultadoOperaciones.html',
	    PARAMS : {idTipoProducto: 'MEME', idMoneda: 'USD'},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/SPT/ResultadoOperaciones/ResultadoOperaciones.js' ]} ],

};


BCS_VENTANA.POSICIONINICIALMESASPOT= {
	    TITLE : "UI_POSICION_INICIAL_MESA_SPOT",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "600px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/SPT/PosicionInicialMesaSpot/PosicionInicialMesaSpot.html',
	    PARAMS : {idTipoProducto: 'MEME'},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/SPT/PosicionInicialMesaSpot/PosicionInicialMesaSpot.js' ]} ],

};


BCS_VENTANA.POSICIONSPOT= {
	    TITLE : "UI_POSICION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/SPT/Posicion/Posicion.html',
	    PARAMS : {idMoneda: 'USD'},
	    REFRESH : true,
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/SPT/Posicion/Posicion.js' ]} ],

};


BCS_VENTANA.POSICIONPORMONEDA= {
	    TITLE : "UI_POSICION_POR_MONEDA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/SPT/PosicionPorMonedas/PosicionPorMonedas.html',
	    PARAMS : {idMoneda: 'USD'},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/SPT/PosicionPorMonedas/PosicionPorMonedas.js' ]} ],

};



BCS_VENTANA.DATATEC= {
	    TITLE : "UI_DATATEC_ONLINE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "600px",
	    VIEW   : '../../Mesa/UI/SPT/Datatec/Datatec.html',
	    PARAMS : {idTipoProducto: 'MEME'},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/SPT/Datatec/Datatec.js' ]} ],

};

BCS_VENTANA.DATATECMONITORAUTOMATICO= {
	    TITLE : "UI_DATATEC_MONITOR_AUTOMATICO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "600px",
	    VIEW   : '../../Mesa/UI/SPT/DatatecMonitorAutomatico/DatatecMonitorAutomatico.html',
	    PARAMS : {idTipoProducto: 'MEME'},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/SPT/DatatecMonitorAutomatico/bcs-datatec-fs.js',
	                                                        '../../Mesa/UI/SPT/DatatecMonitorAutomatico/bcs-datatec-proceso.js',
	                                                        '../../Mesa/UI/SPT/DatatecMonitorAutomatico/DatatecMonitorAutomatico.js' ]} ],

};
 BCS_VENTANA.REPROCESARDATATEC= {
	    TITLE : "UI_REPROCESAR_DATATEC",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/SPT/Datatec/ReprocesarDatatec.html',
	    PARAMS : {idTipoProducto: 'MEME'},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/SPT/Datatec/ReprocesarDatatec.js' ]} ],

};


BCS_VENTANA.CARGADATOSDATATEC= {
	    TITLE : "UI_CARGA_DATOS_DATATEC",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "600px",
	    VIEW   : '../../Mesa/UI/SPT/CargaDatosDatatec/CargaDatosDatatec.html',
	    PARAMS : {idTipoProducto: 'MEME'},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/SPT/CargaDatosDatatec/CargaDatosDatatec.js' ]} ],

};


BCS_VENTANA.INTERFAZOMA= {
	    TITLE : "UI_INTERFAZ_OMA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "600px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/SPT/InterfazOMA/InterfazOMA.html',
	    PARAMS : {idTipoProducto: 'MEME'},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/SPT/InterfazOMA/InterfazOMA.js' ]} ],

};


BCS_VENTANA.PlanillaBancoCentral= {
	    TITLE : "UI_MANTENEDOR_PLANILLA_BANCO_CENTRAL",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/SPT/MantenedorPlanillaBancoCentral/MantenedorPlanillaBancoCentral.html',
	    PARAMS : {idTipoProducto: 'MEME'},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/SPT/MantenedorPlanillaBancoCentral/MantenedorPlanillaBancoCentral.js' ]} ],

};


BCS_VENTANA.ModalEditarCodigoComercio= {
	    TITLE : "UI_EDITAR_CODIGO_COMERCIO",
	    IMAGE  : "",
	    ENABLE : false,
	    WIDTH  : "600px",
	    HEIGHT : "200px",
	    VIEW   : '../../Mesa/UI/SPT/MantenedorPlanillaBancoCentral/ModalEditarCodigoComercio.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/SPT/MantenedorPlanillaBancoCentral/ModalEditarCodigoComercio.js' ]} ],

};

BCS_VENTANA.ReporteOperacionesFicticias= {
	    TITLE : "UI_REPORTE_OPERACIONES_FICTICIAS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "550px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/SPT/ReporteOperacionesFicticias/ReporteOperacionesFicticias.html',
	    PARAMS : {idTipoProducto: 'MEME'},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/SPT/ReporteOperacionesFicticias/ReporteOperacionesFicticias.js' ]} ],

};


BCS_VENTANA.ReporteOperacionesSobre500000= {
	    TITLE : "UI_REPORTE_OPERACIONES_SOBRE_500000",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/SPT/ReporteOperacionesSobre500000/ReporteOperacionesSobre500000.html',
	    PARAMS : {idTipoProducto: 'MEME'},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/SPT/ReporteOperacionesSobre500000/ReporteOperacionesSobre500000.js' ]} ],

}; 


BCS_VENTANA.ReporteResultadoContableSpot= {
	    TITLE : "UI_REPORTE_RESULTADO_CONTABLE_SPOT",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "550px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/SPT/ReporteResultadoContableSpot/ReporteResultadoContableSpot.html',
	    PARAMS : {idTipoProducto: 'MEME'},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/SPT/ReporteResultadoContableSpot/ReporteResultadoContableSpot.js' ]} ],

};



BCS_VENTANA.ParidadYTipoCambio = {
	    TITLE : "UI_PARIDAD_Y_TIPOCAMBIO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "980px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/SPT/ParidadesYTipoCambio/ParidadesYTipoCambio.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [
																'../../Mesa/UI/SPT/ParidadesYTipoCambio/ParidadesYTipoCambio.js',
																'../../Mesa/UI/SPT/ParidadesYTipoCambio/IngresoValores/IngresoValores.js',
																'../../Mesa/UI/SPT/ParidadesYTipoCambio/ValoresPorDefecto/ValoresPorDefecto.js'] } ],

};


BCS_VENTANA.MantenedorEmisores = {
	    TITLE : "UI_EMISORES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "980px",
	    HEIGHT : "auto",
	    VIEW   : '../../Plataforma/UI/MER/MantenedorEmisores/MantenedorEmisores.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Plataforma/UI/MER/MantenedorEmisores/MantenedorEmisores.js'] } ],

};

BCS_VENTANA.CargaVectoresPrecio = {
	    TITLE : "UI_CARGA_VECTOR_PRECIOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
	    HEIGHT : "410px",
	    VIEW   : '../../Plataforma/UI/MER/CargaVectorPrecios/CargaVectorPrecios.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Plataforma/UI/MER/CargaVectorPrecios/CargaVectorPrecios.js'] } ],
};

BCS_VENTANA.ConfiguracionFlujoCaja = {
	    TITLE  : "UI_CONFIGURACION_FLUJO_CAJA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
	    HEIGHT : "700px",
	    VIEW   : '../../Plataforma/UI/PLA/ConfiguracionFlujoCaja/ConfiguracionFlujoCaja.html',
	    PARAMS : { p1: 0, p2: 0},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Plataforma/UI/PLA/ConfiguracionFlujoCaja/ConfiguracionFlujoCaja.js'] }],

	};

BCS_VENTANA.ConfiguracionFlujoCustodia = {
	    TITLE  : "UI_CONFIGURACION_FLUJO_CUSTODIA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
	    HEIGHT : "700px",
	    VIEW   : '../../Plataforma/UI/PLA/ConfiguracionFlujoCustodia/ConfiguracionFlujoCustodia.html',
	    PARAMS : { p1: 0, p2: 0},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Plataforma/UI/PLA/ConfiguracionFlujoCustodia/ConfiguracionFlujoCustodia.js'] }],

	};

BCS_VENTANA.LiquidarSpot = {
	    TITLE : "UI_LIQUIDAR_SPOT",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1050px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/SPT/LiquidacionSpot/LiquidacionSpot.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Mesa/UI/SPT/LiquidacionSpot/LiquidacionSpot.js'] } ],

};
BCS_VENTANA.ConfirmarMRF = {
	    TITLE : "UI_CONFIRMAR_MRF",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1050px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/MRF/ConfirmacionMRF/ConfirmacionOperacionesMRF.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Mesa/UI/MRF/ConfirmacionMRF/ConfirmacionOperacionesMRF.js',
	                                                       '../../Mesa/UI/ModuloMesaRFConfGrids.js'] } ],

};
BCS_VENTANA.ReasignacionMesaRF = {
	    TITLE : "UI_REASIGNACION_MESA_RF",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/MRF/ReasignacionMesaRF/ReasignacionMesaRF.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Mesa/UI/MRF/ReasignacionMesaRF/ReasignacionMesaRF.js'] } ],

};

BCS_VENTANA.ConsultarContactoSpot = {
	    TITLE : "UI_CONTACTO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "650px",
	    HEIGHT : "220px",
	    VIEW   : '../../Mesa/UI/SPT/LiquidacionSpot/ConsultarContactoSpot.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Mesa/UI/SPT/LiquidacionSpot/ConsultarContactoSpot.js'] } ],

};

BCS_VENTANA.ConsultarSolicitudPuntas= {
	    TITLE : "UI_SOLICITUDES_PUNTAS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1050px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/SPT/BlotterSolicitudPuntas/BlotterSolicitudPuntas.html',
	    PARAMS : {},
	    DEPENDENCIES : ['../../Mesa/UI/SPT/BlotterSolicitudPuntas/BlotterSolicitudPuntas.js', '../../Mesa/UI/SPT/BlotterSolicitudPuntas/BlotterSolicitudPuntasGrid.js', '../../Mesa/UI/SPT/ConsultarSolicitudPuntas/ConsultarSolicitudPuntas.js'],

	};

BCS_VENTANA.IngresarSolicitudPuntas = {
	    TITLE : "UI_SOLICITUD_PUNTAS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "500px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/SPT/IngresarSpot/SolicitudPuntas/ModalSolicitudPuntas.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/SPT/IngresarSpot/SolicitudPuntas/ModalSolicitudPuntas.js'] } ],

};

BCS_VENTANA.IngresarEnvioPuntas = {
	    TITLE : "UI_ENVIO_PUNTAS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "500px",
	    HEIGHT : "550px",
	    VIEW   : '../../Mesa/UI/SPT/EnvioPuntas/EnvioPuntas.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/SPT/EnvioPuntas/EnvioPuntas.js'] } ],

};

BCS_VENTANA.LimitesSpotCorredora = {
    TITLE : "UI_INFORME_LIMITES_CORREDORA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "700px",
    VIEW   : '../../Mesa/UI/SPT/LimitesSpotCorredora/LimitesSpotCorredora.html',
    PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/SPT/LimitesSpotCorredora/LimitesSpotCorredora.js'] } ],

};

//---------------------------------------------
//MESA FORWARD
//---------------------------------------------
BCS_VENTANA.MODULO_MESAFORWARD= {
  TITLE : "UI_FORWARD",
  IMAGE  : "sapUiIcon forward",
  ENABLE : true,
  WIDTH  : "900px",
  HEIGHT : "400px",
  VIEW   : '',
  PARAMS : {},
  DEPENDENCIES : [],

};


BCS_VENTANA.MesaForward= {
	    TITLE : "UI_INGRESO_FORWARD",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "814px",
	    HEIGHT : "600px",
	    VIEW   : '../../Mesa/UI/FWD/IngresarForward/IngresarForward.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/FWD/IngresarForward/IngresarForward.js'
													    	,'../../Mesa/UI/ModuloMesaRFConfGrids.js'
															,'../../Mesa/UI/bcs-forma-pago-mesa/bcs-forma-pago-mesa.js']} ],

};


BCS_VENTANA.MesaForwardModal= {
	    TITLE : "UI_INGRESO_FORWARD",
	    IMAGE  : "",
	    ENABLE : false,
	    WIDTH  : "814px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/FWD/IngresarForward/IngresarForward.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/FWD/IngresarForward/IngresarForward.js'
	    													,'../../Mesa/UI/ModuloMesaRFConfGrids.js'
	    													,'../../Mesa/UI/bcs-forma-pago-mesa/bcs-forma-pago-mesa.js' ]} ],

};


BCS_VENTANA.CalceOperaciones= {
	    TITLE : "UI_CALCE_OPERACIONES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "735px",
	    VIEW   : '../../Mesa/UI/FWD/CalceOperaciones/CalceOperaciones.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/FWD/CalceOperaciones/CalceOperaciones.js' ]} ],

};


BCS_VENTANA.VencimientoForward= {
	    TITLE : "UI_VENCIMIENTO_FORWARD",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
	    HEIGHT : "600px",
	    VIEW   : '../../Mesa/UI/FWD/VencimientoForward/VencimientoForward.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/FWD/VencimientoForward/VencimientoForward.js' ]} ],

};


BCS_VENTANA.DevengoForward= {
	    TITLE : "UI_DEVENGO_FORWARD",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "350px",
	    HEIGHT : "100px",
	    VIEW   : '../../Mesa/UI/FWD/Devengo/Devengo.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/FWD/Devengo/Devengo.js' ]} ],

};


BCS_VENTANA.ValorizacionForward= {
	    TITLE : "UI_VALORIZACION_FORWARD",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "350px",
	    HEIGHT : "100px",
	    VIEW   : '../../Mesa/UI/FWD/Valorizacion/ValorizacionForward.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/FWD/Valorizacion/ValorizacionForward.js' ]} ],

};


BCS_VENTANA.InformeArticulo = {
	    TITLE : "UI_ARTICULO_84",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "350px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/InformeArticulo/InformeArticulo.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/InformeArticulo/InformeArticulo.js' ]} ],

};


BCS_VENTANA.MesaMonedaPorProductoForward= {
	    TITLE : "UI_MONEDAS_POR_PRODUCTO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "470px",
	    VIEW   : '../../Mesa/UI/MantenedorMonedaPorProducto/MantenedorMonedaPorProducto.html',
	    PARAMS : {idTipoProducto: 'MEFW'},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/MantenedorMonedaPorProducto/MantenedorMonedaPorProducto.js' ]} ],

};


BCS_VENTANA.ModalEditarEstado= {
	    TITLE : "UI_EDITAR_MONEDA_PRODUCTO",
	    IMAGE  : "",
	    ENABLE : false,
	    WIDTH  : "400px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/MantenedorMonedaPorProducto/ModalEditarEstado.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/MantenedorMonedaPorProducto/ModalEditarEstado.js' ]} ],

};

BCS_VENTANA.ConsultaOperacionesFdw= {
	    TITLE : "UI_CONSULTA_OPERACIONES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1050px",
	    HEIGHT : "790px",
	    VIEW   : '../../Mesa/UI/ConsultaDeOperaciones/ConsultaDeOperaciones2.html',
	    PARAMS : {tipoFlujo: 'Fwd'},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/ConsultaDeOperaciones/ConsultaDeOperaciones2.js',
	    													 '../../Mesa/UI/ConsultaDeOperaciones/ConsultarMovimientos/ConsultarMovimientos.js',
	    													 '../../Mesa/UI/ConsultaDeOperaciones/ConsultarCartera/ConsultarCartera.js',
	    													 '../../Mesa/UI/ConsultaDeOperaciones/ConsultarVencimiento/ConsultarVencimiento.js' ]} ],

};


BCS_VENTANA.MantenedorCanastaFactores= {
	    TITLE : "UI_CANASTA_FACTORES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "360px",
	    VIEW   : '../../Mesa/UI/FWD/MantenedorCanastaFactores/MantenedorCanastaFactores.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/FWD/MantenedorCanastaFactores/MantenedorCanastaFactores.js' ]} ],

};


BCS_VENTANA.MantenedorCanastaModal= {
	    TITLE : "UI_CANASTA_MODAL",
	    IMAGE  : "",
	    ENABLE : false,
	    WIDTH  : "400px",
	    HEIGHT : "175px",
	    VIEW   : '../../Mesa/UI/FWD/MantenedorCanastaFactores/MantenedorCanastaModal.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/FWD/MantenedorCanastaFactores/MantenedorCanastaModal.js' ]} ],

};


BCS_VENTANA.MantenedorFactoresModal= {
	    TITLE : "UI_FACTOR_MODAL",
	    IMAGE  : "",
	    ENABLE : false,
	    WIDTH  : "400px",
	    HEIGHT : "175px",
	    VIEW   : '../../Mesa/UI/FWD/MantenedorCanastaFactores/MantenedorFactoresModal.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/FWD/MantenedorCanastaFactores/MantenedorFactoresModal.js' ]} ],

};


BCS_VENTANA.MantenedorMatrizRiesgo= {
	    TITLE : "UI_MATRIZ_RIESGO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "735px",
	    HEIGHT : "405px",
	    VIEW   : '../../Mesa/UI/FWD/MantenedorMatrizRiesgo/MantenedorMatrizRiesgo.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/FWD/MantenedorMatrizRiesgo/MantenedorMatrizRiesgo.js' ]} ],

};


BCS_VENTANA.ReporteCalceOperaciones= {
	    TITLE : "UI_REPORTE_CALCE_OPERACIONES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/FWD/ReporteCalceOperaciones/ReporteCalceOperaciones.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/FWD/ReporteCalceOperaciones/ReporteCalceOperaciones.js' ]} ],

};

//
BCS_VENTANA.AnexoCapituloIX= {
	    TITLE : "UI_CAPITULO_ANEXO_IX",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "315px",
	    HEIGHT : "175px",
	    VIEW   : '../../Mesa/UI/FWD/AnexoCapituloIX/AnexoCapituloIX.html',
	    PARAMS : {idTipoProducto: 'MEFW'},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/FWD/AnexoCapituloIX/AnexoCapituloIX.js' ]} ],

};



BCS_VENTANA.ReporteMovimientosDiarioFWDMonedas= {
	    TITLE : "UI_MOVIMIENTOS_DIARIO_MONEDA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "405px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/FWD/ReporteMovimientosDiarioFWDMonedas/ReporteMovimientosDiarioFWDMonedas.html',
	    PARAMS : {idTipoProducto: 'MEFW'},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/FWD/ReporteMovimientosDiarioFWDMonedas/ReporteMovimientosDiarioFWDMonedas.js' ]} ],

};

BCS_VENTANA.ReporteOperacionesVigentesAnexo15= {
	    TITLE : "UI_ANEXO15_OPERACIONES_FORWARD_VIGENTES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "380px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/FWD/ReporteOperacionesVigentesAnexo15/ReporteOperacionesVigentesAnexo15.html',
	    PARAMS : {idTipoProducto: 'MEFW'},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/FWD/ReporteOperacionesVigentesAnexo15/ReporteOperacionesVigentesAnexo15.js' ]} ],

};
//
BCS_VENTANA.ReporteCarteraFWDBasilea= {
	    TITLE : "UI_CARTERA_FWD_BASILEA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "350px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/FWD/ReporteCarteraFWDBasilea/ReporteCarteraFWDBasilea.html',
	    PARAMS : {idTipoProducto: 'MEFW'},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/FWD/ReporteCarteraFWDBasilea/ReporteCarteraFWDBasilea.js' ]} ],

};


//
BCS_VENTANA.ReporteResumenDescalceDiarioCliente = {
	    TITLE : "UI_DESCALCE_DIARIO_CLIENTE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "350px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/FWD/ReporteResumenDescalceDiarioCliente/ReporteResumenDescalceDiarioCliente.html',
	    PARAMS : {idTipoProducto: 'MEFW'},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/FWD/ReporteResumenDescalceDiarioCliente/ReporteResumenDescalceDiarioCliente.js' ]} ],

};

//
BCS_VENTANA.InformePosicionClientesFWD = {
	    TITLE : "UI_POSICION_CLIENTE_FWD",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "350px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/FWD/InformePosicionClientesFWD/InformePosicionClientesFWD.html',
	    PARAMS : {idTipoProducto: 'MEFW'},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/FWD/InformePosicionClientesFWD/InformePosicionClientesFWD.js' ]} ],

};

//
BCS_VENTANA.EmisionDevengoFWD = {
	    TITLE : "UI_EMISION_DEVENGO_FWD",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "320px",
	    HEIGHT : "127px",
	    VIEW   : '../../Mesa/UI/FWD/EmisionDevengoFWD/EmisionDevengoFWD.html',
	    PARAMS : {idTipoProducto: 'MEFW'},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/FWD/EmisionDevengoFWD/EmisionDevengoFWD.js' ]} ],

};

BCS_VENTANA.ContratoCondicionesGeneralesFWD = {
	    TITLE : "UI_FWD_CCG_CONTRATO_CONDICIONES_GENERALES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "450px",
	    HEIGHT : "285px",
	    VIEW   : '../../Mesa/UI/FWD/ContratoCondicionesGenerales/CondicionesGeneralesFWD.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Mesa/UI/FWD/ContratoCondicionesGenerales/CondicionesGeneralesFWD.js' ] } ],

};

BCS_VENTANA.InformeLiquidacionFWD = {
	    TITLE : "UI_FWD_INFORME_LIQUIDACION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "350px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/FWD/InformeLiquidacionFWD/InformeLiquidacionFWD.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Mesa/UI/FWD/InformeLiquidacionFWD/InformeLiquidacionFWD.js' ] } ],

};

BCS_VENTANA.ConfirmacionForward = {
	    TITLE : "UI_CONFIRMACION_FORWARD",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1050px",
	    HEIGHT : "565px",
	    VIEW   : '../../Mesa/UI/FWD/ConfirmacionOperacionesFWD/ConfirmacionOperacionesFWD.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Mesa/UI/FWD/ConfirmacionOperacionesFWD/ConfirmacionOperacionesFWD.js' ] } ],

};

BCS_VENTANA.InformeVencimientosAnticiposPactos = {
	TITLE : "UI_INFORME_VENCIMIENTOS_ANTICIPOS_PACTOS",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "450px",
	HEIGHT : "auto",
	VIEW   : '../../Contabilidad/UI/Informes/InformeVencimientosAnticiposPactos.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Contabilidad/UI/Informes/InformeVencimientosAnticiposPactos.js' ] } ],

};

//---------------------------------------------
//Normativa
//---------------------------------------------
BCS_VENTANA.MODULO_NORMATIVO= {
  TITLE : "Normativo",
  IMAGE  : "sapUiIcon study-leave",
  ENABLE : true,
  WIDTH  : "900px",
  HEIGHT : "400px",
  VIEW   : '',
  PARAMS : {},
  DEPENDENCIES : [],

};


BCS_VENTANA.ConsultarCMF = {
	TITLE : "UI_VISOR_DATOS",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "1000px",
	HEIGHT : "637px",
	VIEW   : '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/ConsultarCMF.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Normativa/UI/Circulares/SolicitarCircular/Paneles/ConsultarCMF.js']} ],
};


BCS_VENTANA.ConsultarPactosVigentesEBox = {
	TITLE : "UI_VISOR_DATOS",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "1000px",
	HEIGHT : "637px",
	VIEW   : '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/ConsultarPactosVigentesEBox.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Normativa/UI/Circulares/SolicitarCircular/Paneles/ConsultarPactosVigentesEBox.js']} ],
};

BCS_VENTANA.Circulares= {
	    TITLE : "UI_CIRCULARES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/Circulares/Circulares.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Circulares/Circulares.js',
	                                                       '../../Normativa/UI/Circulares/PanelEdicionCircular.js',
	                                                       '../../Normativa/UI/Circulares/PanelSolicitudCircular.js',
	                                                       '../../Normativa/UI/Circulares/PanelSolicitudCircularGrid.js',
	                                                       '../../Normativa/UI/Circulares/VisorCircular/VisorCircularConfiguracion.js',
	                                                       '../../Normativa/UI/Circulares/EdicionCircular/PanelCir1518.js',
	                                                       '../../Normativa/UI/Circulares/EdicionCircular/PanelCir1720.js',
														   '../../Normativa/UI/Circulares/EdicionCircular/PanelCirNCG499.js'] }],

};

BCS_VENTANA.ModalCir1518 = {
	    TITLE : "UI_EDICION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "640px",
	    HEIGHT : "460px",
	    VIEW   : '../../Normativa/UI/Circulares/EdicionCircular/ModalCir1518/ModalCir1518.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Normativa/UI/Circulares/EdicionCircular/ModalCir1518/ModalCir1518.js',
	                                                         '../../Normativa/UI/Circulares/EdicionCircular/ModalCir1518/Paneles/PanelEdicion.js'] } ],

};
BCS_VENTANA.ModalCirNCG499 = {
	TITLE : "UI_EDICION",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "640px",
	HEIGHT : "650px",
	VIEW   : '../../Normativa/UI/Circulares/EdicionCircular/ModalNCG499/ModalCirNCG499.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Normativa/UI/Circulares/EdicionCircular/ModalNCG499/ModalCirNCG499.js'] } ],

};
BCS_VENTANA.ModalCir1720 = {
	    TITLE : "UI_EDICION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "640px",
	    HEIGHT : "360px",
	    VIEW   : '../../Normativa/UI/Circulares/EdicionCircular/ModalCir1720/ModalCir1720.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Normativa/UI/Circulares/EdicionCircular/ModalCir1720/ModalCir1720.js',
	                                                         '../../Normativa/UI/Circulares/EdicionCircular/ModalCir1720/Paneles/PanelEdicion.js'] } ],

};

BCS_VENTANA.IngresarCir1720 = {
	    TITLE : "UI_EDICION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "720px",
	    HEIGHT : "330px",
	    VIEW   : '../../Normativa/UI/Circulares/EdicionCircular/IngresarCir1720/IngresarCir1720.html',
  		PARAMS : {},
  		DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Normativa/UI/Circulares/EdicionCircular/IngresarCir1720/IngresarCir1720.js'] } ],
};


BCS_VENTANA.NORMantenedorDocumentos = {
	    TITLE : "UI_MANTENEDOR_DE_DOCUMENTOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "650px",
	    VIEW   : '../../Normativa/UI/Declaraciones/GridView.html',
	    PARAMS : {TipoUI:'DOCUMENTOS'},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Declaraciones/GridController.js'] }],

};

BCS_VENTANA.ProcesosCartolasConsolidada = {
	    TITLE : "UI_PROCESOS_CARTOLAS_CONSOLIDADA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/Cartolas/ProcesosCartolasConsolidadaView.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Cartolas/ProcesosCartolasConsolidadaController.js'] }],
};

BCS_VENTANA.ProcesosCartolas = {
	    TITLE : "UI_PROCESOS_CARTOLAS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/Cartolas/ProcesosCartolasView.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Cartolas/ProcesosCartolasController.js'] }],

};


BCS_VENTANA.DetalleProcesoCartolas = {
	    TITLE : "UI_DETALLE_PROCESO_CARTOLAS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "440px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/Cartolas/DetalleProcesoCartolasView.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Cartolas/DetalleProcesoCartolasController.js'] }],

};

BCS_VENTANA.DetalleProcesoCartolasConsolidada = {
	    TITLE : "UI_DETALLE_PROCESO_CARTOLAS_CONSOLIDADA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "440px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/Cartolas/DetalleProcesoCartolasConsolidadaView.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Cartolas/DetalleProcesoCartolasConsolidadaController.js'] }],

};


BCS_VENTANA.CertificarProceso = {
	    TITLE : "UI_CERTIFICAR_PROCESO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "340px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/Cartolas/CertificarProcesoForm.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Cartolas/CertificarProcesoController.js'] }],

};

BCS_VENTANA.ReprocesarCartolas = {
	    TITLE : "UI_REPROCESAR",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "340px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/Cartolas/ReprocesarCartolasForm.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Cartolas/ReprocesarCartolasController.js'] }],

};

BCS_VENTANA.ModificarVigencia = {
	    TITLE : "UI_MODIFICAR_VIGENCIA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "340px",
	    HEIGHT : "170px",
	    VIEW   : '../../Normativa/UI/Cartolas/ModificarVigenciaForm.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Cartolas/ModificarVigenciaController.js'] }],

};

BCS_VENTANA.ModificarRecurso = {
	    TITLE : "UI_MODIFICAR_RECURSO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "150px",
	    VIEW   : '../../Normativa/UI/Cartolas/ModificarRecursoForm.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Cartolas/ModificarRecursoController.js'] }],

};

BCS_VENTANA.ModificarConfig = {
	    TITLE : "UI_MODIFICAR_CONFIGURACION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "150px",
	    VIEW   : '../../Normativa/UI/Cartolas/ModificarConfigForm.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Cartolas/ModificarConfigController.js'] }],

};

BCS_VENTANA.ConsultaCartolas = {
	    TITLE : "UI_CONSULTA_CARTOLAS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "520px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/Cartolas/ConsultaCartolasView.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Cartolas/ConsultaCartolasController.js'] }],

};

BCS_VENTANA.ConsultaCartolasConsolidada = {
	    TITLE : "UI_CONSULTA_CARTOLAS_CONSOLIDADA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/Cartolas/ConsultaCartolasConsolidadaView.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Cartolas/ConsultaCartolasConsolidadaController.js'] }],

};

BCS_VENTANA.AtributionAnalisysConsultarInforme = {
	    TITLE : "UI_ATRIBUTION_ANALISYS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "520px",
	    HEIGHT : "465px",
	    VIEW   : '../../CuentaInversion/UI/AtributtionAnalysis/Informe/ConsultaAtributtionInforme.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../CuentaInversion/UI/AtributtionAnalysis/Informe/ConsultaAtributtionInforme.js'] }],

};

BCS_VENTANA.DiferenciaSaldoCuentaInversion = {
	    TITLE : "UI_DIFERENCIA_SALDO_CUENTA_INVERSION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "310px",
	    HEIGHT : "150px",
	    VIEW   : '../../CuentaInversion/UI/Consultas/ConsultaDiferenciaSaldoCtaInversion.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../CuentaInversion/UI/Consultas/ConsultaDiferenciaSaldoCtaInversion.js'] }],

};

BCS_VENTANA.ReporteAntiguedadDeuda = {
	TITLE : "UI_REPORTE_ANTIGUEDAD_DEUDA",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "1255px",
    HEIGHT : "585px",
	VIEW   : '../../CuentaInversion/UI/ReporteAntiguedadDeuda/ReporteAntiguedadDeuda.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js", cache: true, files: ['../../CuentaInversion/UI/ReporteAntiguedadDeuda/ReporteAntiguedadDeuda.js'] }],
};

BCS_VENTANA.ReporteAcreedores = {
	TITLE : "UI_REPORTE_ACREEDORES",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "1255px",
    HEIGHT : "585px",
	VIEW   : '../../CuentaInversion/UI/ReporteAcreedores/ReporteAcreedores.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js", cache: true, files: ['../../CuentaInversion/UI/ReporteAcreedores/ReporteAcreedores.js'] }],
};

BCS_VENTANA.GenerarReporteAntiguedadDeuda = {
	TITLE : "UI_GENERAR_REPORTE_ANTIGUEDAD_DEUDA",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "595px",
    HEIGHT : "220px",
	VIEW   : '../../CuentaInversion/UI/GenerarReporteAntiguedadDeuda/GenerarReporteAntiguedadDeuda.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js", cache: true, files: ['../../CuentaInversion/UI/GenerarReporteAntiguedadDeuda/GenerarReporteAntiguedadDeuda.js'] }],
};

BCS_VENTANA.NominasCartolasConsolidada = {
	    TITLE : "UI_NOMINA_CARTOLAS_CONSOLIDADA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/Cartolas/NominasCartolasConsolidadaView.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Cartolas/NominasCartolasConsolidadaController.js',
	    													'../../Normativa/UI/Cartolas/CartolaOnline/CartolaOnline.js'] }],

};

BCS_VENTANA.NominasCartolas = {
	    TITLE : "UI_NOMINAS_CARTOLAS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/Cartolas/NominasCartolasView.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Cartolas/NominasCartolasController.js',
	    													'../../Normativa/UI/Cartolas/CartolaOnline/CartolaOnline.js'] }],

};

BCS_VENTANA.PreferenciasCartolas = {
	    TITLE : "UI_PREFERENCIAS_CARTOLAS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "580px",
	    VIEW   : '../../Normativa/UI/Cartolas/PreferenciasCartolasView.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Cartolas/PreferenciasCartolasController.js'] }],

};


BCS_VENTANA.SolicitarCircular= {
	    TITLE : "Solicitar Circular",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "550px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/Circulares/SolicitarCircular/SolicitarCircular.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Circulares/SolicitarCircular/SolicitarCircular.js',
                                                         '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelCarta1BCS.js',
                                                         '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelCarta2SVS.js',
                                                         '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelCI63Nomina.js',
                                                         '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelCI63Transa.js',
                                                         '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelNCG380.js',
                                                         '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelCI63Carta.js',
                                                         '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelUAF48PEP.js',
                                                         '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelUAFDOF.js',
                                                         '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelUAFROE.js',
                                                         '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelCIR1064.js',
                                                         '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelINFTRASEIL.js',
                                                         '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelINFTRABCS.js',
                                                         '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelCIR1064Rueda.js',
                                                         '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelCIR1064FueraRueda.js',
                                                         '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelFATCAR.js',
                                                         '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelFATCAD.js',
                                                         '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelInformeDerechos.js',
                                                         '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelAnexoCuentaPropiaEspeciales.js',
                                                         '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelAnexoCuentaPropiaOtrasBolsas.js',
                                                         '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelAnexoMontosFacturados.js',
                                                         '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelCIR1518.js',
														 '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelCIRCMF.js',
                                                         '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelCIR1720.js',
                                                         '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelCIR1804.js',
                                                         '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelCIR1985.js',
                                                         '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelNGC368PART.js',
                                                         '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelNGC368GRAV.js',
                                                         '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelInformeMensualRV.js',
                                                         '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelUAF57BFPEJ.js',
                                                         '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelNCG365FFMM.js',
                                                         '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelCIR20Anexo1.js',
                                                         '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelCIR20Anexo1Detalle.js',
							 '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelArchivoPactosVigentesEBox.js',
							 '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelINFOFWDEBOXForwardsVigentes.js',
							 '../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelINFOFWDEBOXGarantiasForward.js'
                                                         ] }],
};

BCS_VENTANA.VisorCircular= {
	    TITLE : "UI_VISOR_DE_CIRCULAR",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "573px",
	    VIEW   : '../../Normativa/UI/ConsultarEmisionCircular/VisorCircular/VisorCircular.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/ConsultarEmisionCircular/VisorCircular/VisorCircularConfiguracion.js',
	                                                       '../../Normativa/UI/ConsultarEmisionCircular/VisorCircular/VisorCircular.js'] }],

};


BCS_VENTANA.LibroOperaciones= {
	    TITLE : "UI_LIBRO_DE_OPERACIONES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "737px",
	    VIEW   : '../../Normativa/UI/ConsultarLibroOperaciones/ConsultarLibroOperaciones.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/ConsultarLibroOperaciones/ConsultarLibroOperaciones.js', '../../Normativa/UI/ConsultarLibroOperaciones/ConsultarLibroOperacionesGrid.js'] }],

};


BCS_VENTANA.Circularizacion= {
	    TITLE : "UI_CIRCULARIZACION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "780px",
	    VIEW   : '../../Normativa/UI/Circularizacion/Circularizacion.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Circularizacion/Circularizacion.js',
                                                           '../../Normativa/UI/Circularizacion/PanelNomina.js',
                                                           '../../Normativa/UI/Circularizacion/PanelResumen.js'] }],

};

BCS_VENTANA.ConsultarLibroVentas= {
	    TITLE : "UI_LIBRO_DE_VENTAS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "780px",
	    VIEW   : '../../Normativa/UI/ConsultarLibroVentas/ConsultarLibroVentas.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/ConsultarLibroVentas/ConsultarLibroVentas.js',
                                                           '../../Normativa/UI/ConsultarLibroVentas/PanelLibroVentas.js'] }],

};


BCS_VENTANA.SolicitarCircularizacion= {
	    TITLE : "UI_SOLICITAR_CIRCULARIZACION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "550",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/Circularizacion/SolicitarCircularizacion/SolicitarCircularizacion.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Circularizacion/CircularizacionConfiguracion.js',
	                                                       '../../Normativa/UI/Circularizacion/SolicitarCircularizacion/SolicitarCircularizacion.js',
	                                                       '../../Normativa/UI/Circularizacion/SolicitarCircularizacion/Paneles/PanelCartas.js'] }],

};

BCS_VENTANA.EdicionMultipleCircularizacion = {
	    TITLE : "UI_EDICION_MULTIPLE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "320px",
	    HEIGHT : "270px",
	    VIEW   : '../../Normativa/UI/Circularizacion/EdicionMultiple/EdicionMultiple.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Normativa/UI/Circularizacion/EdicionMultiple/EdicionMultiple.js',
	                                                         '../../Normativa/UI/Circularizacion/EdicionMultiple/Paneles/PanelNomina.js'] } ],

};

BCS_VENTANA.RespuestaCircularizacion = {
	    TITLE : "UI_RESPUESTA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "320px",
	    HEIGHT : "188px",
	    VIEW   : '../../Normativa/UI/Circularizacion/Respuesta/Respuesta.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Normativa/UI/Circularizacion/Respuesta/Respuesta.js'] } ],

};
BCS_VENTANA.GeneracionCartasCircularizacion = {
	    TITLE : "UI_GENERAR_CARTAS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "320px",
	    HEIGHT : "190px",
	    VIEW   : '../../Normativa/UI/Circularizacion/GeneracionCartas/GeneracionCartas.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Normativa/UI/Circularizacion/GeneracionCartas/GeneracionCartas.js'] } ],

};

BCS_VENTANA.Cumplimiento= {
	    TITLE : "UI_CUMPLIMIENTO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/Cumplimiento/Cumplimiento.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Cumplimiento/Cumplimiento.js',
                                                           '../../Normativa/UI/Cumplimiento/PanelDOF.js',
                                                           '../../Normativa/UI/Cumplimiento/SOI/SOI.js',
                                                           '../../Normativa/UI/Cumplimiento/SOI/Paneles/PanelMonto.js',
                                                           '../../Normativa/UI/Cumplimiento/SOI/Paneles/PanelConcurrencia.js',
                                                           '../../Normativa/UI/Cumplimiento/SOI/Paneles/PanelPerfil.js',
                                                           '../../Plataforma/UI/COM/PlantillaProductos/PlantillaProductos.js'] }],

};



BCS_VENTANA.EstadisticaOperacionCliente = {
	    TITLE : "UI_ESTADISTICA_CLIENTE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "600px",
	    HEIGHT : "650px",
	    VIEW   : '../../Normativa/UI/EstadisticaOperacionCliente/EstadisticaOperacionCliente.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Normativa/UI/EstadisticaOperacionCliente/EstadisticaOperacionCliente.js',
	                                                         '../../Normativa/UI/EstadisticaOperacionCliente/PanelOperaciones.js'] } ],

};

BCS_VENTANA.NotificacionOrdenes= {
	    TITLE : "UI_NOTIFICACION_DE_ORDENES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "780px",
	    VIEW   : '../../Normativa/UI/NotificacionOrdenes/NotificacionOrdenes.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/NotificacionOrdenes/NotificacionOrdenes.js',
                                                           '../../Normativa/UI/NotificacionOrdenes/PanelNomina.js'] }],

};

BCS_VENTANA.GeneracionCartasEjecucionOrdenes = {
	    TITLE : "UI_GENERAR_CARTAS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "320px",
	    HEIGHT : "205px",
	    VIEW   : '../../Normativa/UI/NotificacionOrdenes/GeneracionCartas/GeneracionCartas.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Normativa/UI/NotificacionOrdenes/GeneracionCartas/GeneracionCartas.js'] } ],

};

BCS_VENTANA.ConsultarEjecucionOrdenes= {
	    TITLE : "UI_CONSULTAR_EJECUCION_DE_ORDENES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "780px",
	    VIEW   : '../../Normativa/UI/ConsultarEjecucionOrdenes/ConsultarEjecucionOrdenes.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/ConsultarEjecucionOrdenes/ConsultarEjecucionOrdenes.js',
                                                           '../../Normativa/UI/ConsultarEjecucionOrdenes/PanelEjecucionOrdenes.js'] }],

};




BCS_VENTANA.ConsultarCertificados= {
	    TITLE : "UI_CONSULTAR_CERTIFICADOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "520px",
	    HEIGHT : "250px",
	    VIEW   : '../../Normativa/UI/Certificados/consultarCertificados.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Certificados/consultarCertificados.js', '../../Normativa/UI/ConsultarLibroOperaciones/ConsultarLibroOperacionesGrid.js'] }],

};



BCS_VENTANA.ConsultarIndicadoresNormativa= {
	    TITLE : "UI_CONSULTAR_INDICADORES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "770px",
	    VIEW   : '../../Normativa/UI/Indicadores/Indicadores.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Indicadores/Indicadores.js',
                                                           '../../Normativa/UI/Indicadores/PanelCliente.js',
                                                            '../../Normativa/UI/Indicadores/PanelPortafolio.js',
                                                            '../../Normativa/UI/Indicadores/PanelPersona.js'] }],

};

BCS_VENTANA.ConsultarSiiF29 = {
	    TITLE : "UI_CONSULTAR_SIIF29",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1280px",
	    HEIGHT : "683px",
	    VIEW   : '../../Normativa/UI/ConsultarSiiF29/ConsultarSiiF29.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Normativa/UI/ConsultarSiiF29/ConsultarSiiF29.js'] } ],

};

BCS_VENTANA.ConsultarCalculoPolizaGarantiaAPV = {
	    TITLE : "UI_CALCULO_POLIZA_GARANTIA_ANUAL_APV",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1280px",
	    HEIGHT : "651px",
	    VIEW   : '../../Normativa/UI/ConsultarCalculoPolizaGarantiaAPV/ConsultarCalculoPolizaGarantiaAPV.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Normativa/UI/ConsultarCalculoPolizaGarantiaAPV/ConsultarCalculoPolizaGarantiaAPV.js'] } ],

};

BCS_VENTANA.ConsultarInversionistasRV13419 = {
	    TITLE : "UI_CONSULTAR_INVERSIONISTAS_RV_13419",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1280px",
	    HEIGHT : "683px",
	    VIEW   : '../../Normativa/UI/ConsultaInformeInversionistasRV13419/ConsultaInformeInversionistasRV13419.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Normativa/UI/ConsultaInformeInversionistasRV13419/ConsultaInformeInversionistasRV13419.js'] } ],

};

BCS_VENTANA.ConsultarCustodiaCierreJuntaAccionista= {
	    TITLE : "UI_CONSULTAR_CUSTODIA_JUNTA_ACCIONISTAS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "680px",
	    VIEW   : '../../Custodia/UI/MantenedorJuntaAccionistas/ConsultarCustodiaCierreJuntaAccionista.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Custodia/UI/MantenedorJuntaAccionistas/ConsultarCustodiaCierreJuntaAccionista.js'] }],

};

BCS_VENTANA.MantenedorClienteCustodiaCierreJuntaAccionista= {
	    TITLE : "UI_MANTENEDOR_CUSTODIA_CLIENTE_JUNTA_ACCIONISTAS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "450px",
	    HEIGHT : "240px",
	    VIEW   : '../../Custodia/UI/MantenedorJuntaAccionistas/MantenedorClienteCustodiaCierreJuntaAccionista.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Custodia/UI/MantenedorJuntaAccionistas/MantenedorClienteCustodiaCierreJuntaAccionista.js'] }],

};

BCS_VENTANA.MantenedorNominasJuntaAccionistas= {
	    TITLE : "UI_MANTENEDOR_NOMINAS_JUNTA_ACCIONISTAS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "680px",
	    VIEW   : '../../Custodia/UI/MantenedorJuntaAccionistas/MantenedorNominasJuntaAccionistas.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Custodia/UI/MantenedorJuntaAccionistas/MantenedorNominasJuntaAccionistas.js'] }],

};

BCS_VENTANA.IngresarJuntaAccionistas= {
	    TITLE : "UI_MANTENEDOR_JUNTA_ACCIONISTAS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "580px",
	    HEIGHT : "350px",
	    VIEW   : '../../Custodia/UI/MantenedorJuntaAccionistas/IngresarJuntaAccionista.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Custodia/UI/MantenedorJuntaAccionistas/IngresarJuntaAccionista.js',
	                                                       '../../Custodia/UI/MantenedorJuntaAccionistas/ConsultarJuntaAccionistasGrid.js'] }],

};

//---------------------------------------------
//TRIBUTARIO
//---------------------------------------------
BCS_VENTANA.MODULO_TRIBUTARIO= {
    TITLE : "Tributario",
    IMAGE  : "sapUiIcon official-service",
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "400px",
    VIEW   : '',
    PARAMS : {},
    DEPENDENCIES : [],

};


BCS_VENTANA.VisorDeclaraciones = {
	    TITLE : "Visor Declaraciones",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1200px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/Declaraciones/GridView.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Declaraciones/GridController.js'] }],

};

//INICIO - ABBI
BCS_VENTANA.VisorDeclaracionesBancoCentral = {
	    TITLE : "UI_MANTENEDOR_DE_DECLARACIONES_BCC",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1200px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/Declaraciones/GridView.html',
	    PARAMS : {IdEntidadReguladora:"BCC"},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Declaraciones/GridController.js'] }],

};
//FIN - ABBI

BCS_VENTANA.MantenedorDeclaraciones = {
	    TITLE : "Mantenedor Declaraciones",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/Declaraciones/DetailView.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Declaraciones/DetailController.js'] }],

};


BCS_VENTANA.CargadorDatos = {
	    TITLE : "Cargador Datos Declaraciones",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "340px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/Declaraciones/LoadForm.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Declaraciones/LoadController.js'] }],

};


BCS_VENTANA.CargadorDatosBolsones = {
	    TITLE : "Cargador Datos Bolsones",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "340px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/Declaraciones/PanelesModalDetalles/LoadBolsonForm.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Declaraciones/PanelesModalDetalles/LoadBolsonController.js'] }],
};

BCS_VENTANA.ExportadorDatos = {
	    TITLE : "Exportador Datos Declaraciones",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "340px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/Declaraciones/ExportForm.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Declaraciones/ExportController.js'] }],

};


BCS_VENTANA.GenerarDeclaracion = {
	    TITLE : "Generar Declaracion",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "280px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/Declaraciones/GenForm.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Declaraciones/GenController.js'] }],
};


BCS_VENTANA.RectificarMarcados = {
	    TITLE : "UI_OPCIONES_RECTIFICACION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "280px",
	    HEIGHT : "180px",
	    VIEW   : '../../Normativa/UI/Declaraciones/RectifyForm.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Declaraciones/RectifyController.js'] }],
};


BCS_VENTANA.CuadraturaDatos = {
	    TITLE : "Cuadratura Datos Declaraciones",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "300px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/Declaraciones/QuadForm.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Declaraciones/QuadController.js'] }],

};

BCS_VENTANA.MantenedorEsquemaDeclaracion = {
	    TITLE : "UI_CONFIGURACION_ESQUEMA_PERIODO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/Declaraciones/Maestros/Esquema/BlotterEsquema.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Declaraciones/Maestros/Esquema/BlotterEsquema.js',
	                                                       '../../Normativa/UI/Declaraciones/Maestros/MaestroDeclaracionesConfiguracionGrid.js'] }],

};


BCS_VENTANA.AgregarEsquemaDeclaraciones = {
	    TITLE : "UI_AGREGAR_ESQUEMA_PERIODO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "300px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/Declaraciones/Maestros/Esquema/AgregarEsquema.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Declaraciones/Maestros/Esquema/AgregarEsquema.js'] }],

};
BCS_VENTANA.ActivarEsquemaDeclaraciones = {
	    TITLE : "UI_ACTIVAR_ESQUEMA_PERIODO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "300px",
	    HEIGHT : "180px",
	    VIEW   : '../../Normativa/UI/Declaraciones/Maestros/Esquema/ActivarEsquema.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Declaraciones/Maestros/Esquema/ActivarEsquema.js'] }],

};
BCS_VENTANA.AbrirPeriodoDeclaraciones = {
	    TITLE : "UI_ABRIR_PERIODO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "300px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/Declaraciones/Maestros/Esquema/AbrirPeriodo.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Declaraciones/Maestros/Esquema/AbrirPeriodo.js'] }],

};
BCS_VENTANA.ControlProcesoTributario = {
	    TITLE : "UI_CONTROL_PROCESO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/Declaraciones/Maestros/ControlProcesos/ControlProceso.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Declaraciones/Maestros/ControlProcesos/ControlProceso.js',
	                                                       '../../Normativa/UI/Declaraciones/Maestros/MaestroDeclaracionesConfiguracionGrid.js'] }],

};

BCS_VENTANA.TributariosComplementos = {
	    TITLE : "UI_COMPLEMENTOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/Declaraciones/Maestros/Complementos/ComplementosMain.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Declaraciones/Maestros/Complementos/ComplementosMain.js',
	                                                       '../../Normativa/UI/Declaraciones/Maestros/Complementos/FondoAccionario.js',
	                                                       '../../Normativa/UI/Declaraciones/Maestros/Complementos/CargaIPC.js',
	                                                       '../../Normativa/UI/Declaraciones/Maestros/Complementos/EventoCapital.js',
														   '../../Normativa/UI/Declaraciones/Maestros/Complementos/CalificacionPendiente.js',
	                                                       '../../Normativa/UI/Declaraciones/Maestros/Complementos/ActualizarMontos.js',
	                                                       '../../Normativa/UI/Declaraciones/Maestros/Complementos/ValorizarDeclaracion.js',
	                                                       '../../Normativa/UI/Declaraciones/Maestros/Complementos/AjustarRescatesDelPeriodo.js',
														   '../../Normativa/UI/Declaraciones/Maestros/Complementos/Calculo4Porciento.js',
	                                                       '../../Normativa/UI/Declaraciones/Maestros/MaestroDeclaracionesConfiguracionGrid.js'] }],

};

BCS_VENTANA.TributariosCertificados = {
	    TITLE : "UI_CERTIFICADOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/Declaraciones/Maestros/Certificados/Certificados.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Declaraciones/Maestros/Certificados/Certificados.js',
	                                                       '../../Normativa/UI/Declaraciones/Maestros/MaestroDeclaracionesConfiguracionGrid.js',
	                                                         '../../Normativa/UI/Declaraciones/Maestros/Certificados/Paneles/PanelCertificados.js'] }],

};

/* Ventana para Generar archivo csv plano REPO SICAM BCCH */

BCS_VENTANA.GenerarRepoSicam = {
	    TITLE : "UI_GENERAR_REPO_SICAM",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "500px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/Declaraciones/Maestros/BCCH/GenerarREPO/GenerarREPO.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Declaraciones/Maestros/BCCH/GenerarREPO/GenerarREPO.js',
	                                                       '../../Normativa/UI/Declaraciones/Maestros/MaestroDeclaracionesConfiguracionGrid.js',
	                                                         ] }],

};

BCS_VENTANA.CalificacionesTributarias = {
	    TITLE : "UI_CALIFICACIONES_TRIBUTARIAS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1200px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/Declaraciones/Maestros/CalificacionesTributarias/CalificacionesTributarias.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Declaraciones/Maestros/CalificacionesTributarias/CalificacionesTributarias.js',
	                                                       '../../Normativa/UI/Declaraciones/Maestros/MaestroDeclaracionesConfiguracionGrid.js'] }],

};

BCS_VENTANA.CertificadosTributariasSIGACRM = {
	    TITLE : "UI_CERTIFICADO_TRIBUTARIOS_SIGACRM",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "500px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/SIGACRM/SigaCrmHistorico.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/SIGACRM/SigaCrmHistorico.js'] }],

};


BCS_VENTANA.IngresarCalificacion = {
	    TITLE : "UI_INGRESAR_CALIFICACION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "850px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/Declaraciones/Maestros/CalificacionesTributarias/IngresarCalificacion.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Declaraciones/Maestros/CalificacionesTributarias/IngresarCalificacion.js'] }],

};

BCS_VENTANA.IngresarDetalleCalificacion = {
	TITLE : "UI_INGRESO_CALIFICACIONES_TRIBUTARIAS",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "900px",
	HEIGHT : "auto",
	VIEW   : '../../Normativa/UI/Declaraciones/Maestros/CalificacionesTributarias/IngresarDetalleCalificacion.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Declaraciones/Maestros/CalificacionesTributarias/IngresarDetalleCalificacion.js']}],
};

BCS_VENTANA.ModificarCalificacion = {
	    TITLE : "UI_MODIFICAR_CALIFICACION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/Declaraciones/Maestros/CalificacionesTributarias/ModificarCalificacion.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Declaraciones/Maestros/CalificacionesTributarias/ModificarCalificacion.js'] }],

};

BCS_VENTANA.CargaArchivoCalificacion = {
    TITLE : "UI_CARGAR_ARCHIVO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1010px",
    HEIGHT : "340px",
    VIEW   : '../../Normativa/UI/Declaraciones/Maestros/CalificacionesTributarias/CargaArchivoCalificacion.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Normativa/UI/Declaraciones/Maestros/CalificacionesTributarias/CargaArchivoCalificacion.js'] } ],
    AFECTOCIERRE : true,
    MODULO : "NOR",
};

BCS_VENTANA.AnexoCalificacionesTributarias = {
	    TITLE : "UI_ANEXO_CALIFICACIONES_TRIBUTARIAS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "750px",
	    VIEW   : '../../Normativa/UI/Declaraciones/Maestros/CalificacionesTributarias/BlotterAnexoCalificacionesTributarias.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Declaraciones/Maestros/CalificacionesTributarias/BlotterAnexoCalificacionesTributarias.js',
	                                                       '../../Normativa/UI/Declaraciones/Maestros/MaestroDeclaracionesConfiguracionGrid.js'] }],

};

BCS_VENTANA.IngresarAnexoCalificacionesTributarias = {
	    TITLE : "UI_INGRESAR_ANEXO_CALIFICACION_TRIBUTARIA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "235px",
	    VIEW   : '../../Normativa/UI/Declaraciones/Maestros/CalificacionesTributarias/IngresarAnexoCalificacionesTributarias.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Declaraciones/Maestros/CalificacionesTributarias/IngresarAnexoCalificacionesTributarias.js',
	                                                       '../../Normativa/UI/Declaraciones/Maestros/MaestroDeclaracionesConfiguracionGrid.js'] }],

};
BCS_VENTANA.ModificarAnexoCalificacionesTributarias = {
	    TITLE : "UI_MODIFICAR_ANEXO_CALIFICACION_TRIBUTARIA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "235px",
	    VIEW   : '../../Normativa/UI/Declaraciones/Maestros/CalificacionesTributarias/ModificarAnexoCalificacionesTributarias.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Declaraciones/Maestros/CalificacionesTributarias/ModificarAnexoCalificacionesTributarias.js',
	                                                       '../../Normativa/UI/Declaraciones/Maestros/MaestroDeclaracionesConfiguracionGrid.js'] }],

};

BCS_VENTANA.AsociarEventoCapitalTributario = {
	    TITLE : "UI_ASOCIAR_EVENTOS_CAPITAL",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "750px",
	    VIEW   : '../../Normativa/UI/Declaraciones/Maestros/Complementos/AsociarEventoCapital.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Declaraciones/Maestros/Complementos/AsociarEventoCapital.js',
	                                                       '../../Normativa/UI/Declaraciones/Maestros/MaestroDeclaracionesConfiguracionGrid.js'] }],

};


BCS_VENTANA.BlotterMLT= {
	    TITLE : "UI_MANTENEDOR_COSTO_18QUATER_y_RENTABILIDAD_APV",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/BlotterMLT/BlotterMLT.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/BlotterMLT/BlotterMLT.js',
	    												   '../../Normativa/UI/BlotterMLT/PanelIngresos.js'] }],
};

BCS_VENTANA.ManetenedorEntidadesAuditoras={
	TITLE : "UI_MANTENEDOR_ENTIDADES_AUDITORAS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "700px",
	    HEIGHT : "390px",
	    VIEW   : '../../Normativa/UI/MantenedorEntidadesAuditoras/MantenedorEntidadesAuditoras.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/MantenedorEntidadesAuditoras/MantenedorEntidadesAuditoras.js',] }],
};

BCS_VENTANA.AgregarModificarEntidadesAuditoras = {
	TITLE : "UI_ENTIDAD_AUDITORA",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "360px",
	HEIGHT : "224px",
	VIEW   : '../../Normativa/UI/MantenedorEntidadesAuditoras/AgregarModificarEntidadesAuditoras.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Normativa/UI/MantenedorEntidadesAuditoras/AgregarModificarEntidadesAuditoras.js'] } ],

};

BCS_VENTANA.IngresoIngresoMLT= {
	    TITLE : "UI_INGRESO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/BlotterMLT/MLT/MLT.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/BlotterMLT/MLT/MLT.js',
	    												   '../../Normativa/UI/BlotterMLT/MLT/Paneles/PanelIngreso.js'] }],

};


BCS_VENTANA.CargaArchivoMLT = {
		TITLE : "UI_ARCHIVO_DE_COSTO_18QUATER_y_RENTABILIDAD_APV",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "650px",
		HEIGHT : "auto",
		VIEW   : '../../Normativa/UI/BlotterMLT/CargaArchivoMLT/CargaArchivoMLT.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Normativa/UI/BlotterMLT/CargaArchivoMLT/CargaArchivoMLT.js'] }],
};

//---------------------------------------------
//PLATAFORMA
//---------------------------------------------
BCS_VENTANA.MODULO_PLATAFORMA= {
    TITLE : "UI_PLATAFORMA",
    IMAGE  : "sapUiIcon key",
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "400px",
    VIEW   : '',
    PARAMS : {},
    DEPENDENCIES : [],

};


//VENTANA DE ALERTA
BCS_VENTANA.VentanaAlerta = {
		TITLE : "",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "500px",
	    HEIGHT : "136px",
	    VIEW   : 'services/bcs-alerta/bcs-alerta-cc.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['services/bcs-alerta/bcs-alerta-cc.js'] }],

};

//VENTANA DE ALERTA
BCS_VENTANA.MapaRelaciones = {
		TITLE : "UI_MAPA_DE_RELACIONES",
	    IMAGE  : "",
	    PRINTABLE : true,
	    ENABLE : true,
	    WIDTH  : "700px",
	    HEIGHT : "400px",
	    VIEW   : 'PLA/MapaRelaciones/MapaRelaciones.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['PLA/MapaRelaciones/MapaRelaciones.js'] }],

};

//------------------- PLATAFORMA / AUTORIZACION  ---------------------
BCS_VENTANA.PlataformaSeparadorAutorizacion = {
		SEPARATE: true,
	    TITLE : "UI_AUTORIZACION",
	    ENABLE : true
};


BCS_VENTANA.Autorizaciones = {
	    TITLE : "UI_AUTORIZACIONES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
	    HEIGHT : "660px",
	    VIEW   : '../../Plataforma/UI/AUT/Autorizaciones/Autorizaciones.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ 'AUT/Autorizaciones/Autorizaciones.js',
	                                                         'AUT/ConfiguracionAUT.js',
	                                                         'AUT/VistaAutorizaciones/VistaAutorizaciones.js',
	                                                         'AUT/VistaAutorizaciones/ConfiguracionVistaAutorizaciones.js',
	                                                         'AUT/AutorizacionesSolicitadas/AutorizacionesSolicitadas.js',
	                                                         'AUT/PanelAutorizacionesMasivas/AutorizacionesMasivas.js'] } ],
};

	

BCS_VENTANA.MantenedorAutorizaciones = {
	    TITLE : "UI_MANTENEDOR_AUTORIZACIONES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "540px",
	    VIEW   : '../../Plataforma/UI/AUT/MantenedorAutorizacion/MantenedorAutorizacion.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['AUT/MantenedorAutorizacion/MantenedorAutorizacion.js','AUT/ConfiguracionAUT.js'] } ],

};


BCS_VENTANA.EtapaAutorizacion = {
	    TITLE : "UI_ETAPA_EVENTO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "550px",
	    HEIGHT : "300px",
	    VIEW   : 'AUT/MantenedorAutorizacion/ModalEtapas.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['AUT/MantenedorAutorizacion/ModalEtapas.js'] } ],

};

BCS_VENTANA.AutorizadorAutorizacion = {
	    TITLE : "UI_AUTORIZADOR_ETAPA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "500px",
	    HEIGHT : "300px",
	    VIEW   : '../../Plataforma/UI/AUT/MantenedorAutorizacion/ModalAutorizadores.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['AUT/MantenedorAutorizacion/ModalAutorizadores.js'] } ],

};

BCS_VENTANA.MantenedorAccesosIcliente = {
	    TITLE : "UI_MANTENEDOR_ACCESOS_ICLIENTE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "441px",
	    VIEW   : 'ACC/mantenedorAccesosICliente.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['ACC/mantenedorAccesosICliente.js'] }], 
};

BCS_VENTANA.ConsultaUsuarios = {
	    TITLE : "UI_CONSULTA_USUARIOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "480px",
	    VIEW   : 'ACC/consultarUsuarios.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['ACC/consultarUsuarios.js'] }], 

};


BCS_VENTANA.ConsultarAccesosUsuarios = {
	    TITLE : "UI_CONSULTAR_ACCESOS_USUARIO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "545px",
	    HEIGHT : "440px",
	    VIEW   : 'ACC/accesosUsuario.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['ACC/accesosUsuario.js'] } ],

};


BCS_VENTANA.CrearUsuario = {
    TITLE : "UI_CREAR_USUARIO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "420px",
    HEIGHT : "405px",
    VIEW   : 'ACC/crearUsuario.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['ACC/crearUsuario.js'] }], 

};

BCS_VENTANA.CrearUsuarioBolsa = {
	    TITLE : "UI_CREAR_USUARIO_BOLSA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "420px",
	    HEIGHT : "320px",
	    VIEW   : 'ACC/crearUsuarioBolsa.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['ACC/crearUsuarioBolsa.js'] }], 
};

BCS_VENTANA.ModificarUsuario = {
    TITLE : "UI_MODIFICAR_USUARIO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "420px",
    HEIGHT : "320px",
    VIEW   : 'ACC/modificarUsuario.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['ACC/modificarUsuario.js'] }], 

};

BCS_VENTANA.ModificarRolesUsuario = {
    TITLE : "UI_MOD_ROLES_USUARIO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "745px",
    HEIGHT : "540px",
    VIEW   : 'ACC/modificarRolesUsuario.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['ACC/modificarRolesUsuario.js'] }], 

};

BCS_VENTANA.RolesUsuarioModal = {
    TITLE : "UI_MOD_ROLES_USUARIO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "400px",
    HEIGHT : "230px",
    VIEW   : 'ACC/RolesUsuarioModal.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['ACC/RolesUsuarioModal.js'] }], 

};

BCS_VENTANA.ConsultarRoles = {
    TITLE : "UI_CONSULTAR_ROLES",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "700px",
    HEIGHT : "571px",
    VIEW   : 'ACC/Roles/consultarRoles.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['ACC/Roles/consultarRoles.js'] }], 

};
BCS_VENTANA.CrearRoles = {
    TITLE : "UI_CREAR_ROL",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "420px",
    HEIGHT : "150px",
    VIEW   : 'ACC/Roles/crearRoles.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['ACC/Roles/crearRoles.js'] }], 

};
BCS_VENTANA.ModificarRoles = {
    TITLE : "UI_MODIFICAR_ROLES",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "745px",
    HEIGHT : "615px",
    VIEW   : 'ACC/Roles/modificarRoles.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['ACC/Roles/modificarRoles.js'] }], 

};
BCS_VENTANA.RolesModal = {
    TITLE : "UI_MOD_ROLES",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "400px",
    HEIGHT : "230px",
    VIEW   : 'ACC/Roles/rolesModal.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['ACC/Roles/rolesModal.js'] }], 

};

BCS_VENTANA.ConsultarPerfiles = {
    TITLE : "UI_CONSULTAR_PERFILES",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "700px",
    HEIGHT : "576px",
    VIEW   : 'ACC/Perfiles/consultarPerfiles.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['ACC/Perfiles/consultarPerfiles.js'] }], 

};
BCS_VENTANA.CrearPerfil= {
    TITLE : "UI_CREAR_PERFIL",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "420px",
    HEIGHT : "150px",
    VIEW   : 'ACC/Perfiles/crearPerfil.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['ACC/Perfiles/crearPerfil.js'] }], 

};
BCS_VENTANA.ModificarPerfil= {
    TITLE : "UI_MODIFICAR_PERFIL",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "490px",
    HEIGHT : "590px",
    VIEW   : 'ACC/Perfiles/modificarPerfil.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['ACC/Perfiles/modificarPerfil.js'] }], 

};

BCS_VENTANA.CambiarClaveUsuario= {
    TITLE : "UI_CAMBIAR_CLAVE_USUARIO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "400px",
    HEIGHT : "350px",
    VIEW   : 'ACC/cambiarClaveUsuario.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['ACC/cambiarClaveUsuario.js'] }], 

};

BCS_VENTANA.CambiarClaveUsuarioReplica= {
    TITLE : "UI_CAMBIAR_CLAVE_USUARIO_REPLICA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "400px",
    HEIGHT : "350px",
    VIEW   : '../../Replica/UI/ACC/cambiarClaveUsuarioReplica.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Replica/UI/ACC/cambiarClaveUsuarioReplica.js'] }], 

};

BCS_VENTANA.IngresarAccesoICliente = {
	    TITLE : "UI_INGRESAR_ACCESO_ICLIENTE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "420px",
	    //HEIGHT : "320px",
	    HEIGHT : "220px",
	    VIEW   : 'ACC/ingresarAccesoICliente.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['ACC/ingresarAccesoICliente.js'] }], 

	};

BCS_VENTANA.IngresarAccesoIClienteRelacionado = {
	    TITLE : "UI_INGRESAR_ACCESO_ICLIENTE_RELACIONADO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "420px",
	    //HEIGHT : "320px",
	    HEIGHT : "220px",
	    VIEW   : 'ACC/ingresarAccesoIClienteRelacionado.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['ACC/ingresarAccesoIClienteRelacionado.js'] }], 

	};

BCS_VENTANA.ModificarAccesoICliente = {
	    TITLE : "UI_MODIFICAR_ACCESO_ICLIENTE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "420px",
	    //HEIGHT : "320px",
	    HEIGHT : "220px",
	    VIEW   : 'ACC/modificarAccesoICliente.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['ACC/modificarAccesoICliente.js'] }], 

	};

//------------------- PLATAFORMA / INSTRUMENTOS  ---------------------
BCS_VENTANA.PlataformaSeparadorInstrumentos= {
	SEPARATE: true,
    TITLE : "UI_INSTRUMENTOS",
    ENABLE : true
};




BCS_VENTANA.ConsultarPreciosSistema = {
	    TITLE : "UI_CONSULTAR_PRECIOS_SISTEMA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1200px",
	    HEIGHT : "650px",
	    VIEW   : 'INS/ConsultarPreciosSistema/ConsultarPreciosSistema.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['INS/ConsultarPreciosSistema/ConsultarPreciosSistema.js'] }],

};


BCS_VENTANA.ConsultarInstrumentos = {
	    TITLE : "UI_CONSULTAR_INSTRUMENTOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1200px",
	    HEIGHT : "648px",
	    VIEW   : 'INS/ConsultarInstrumentos/ConsultarInstrumentos.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['INS/ConsultarInstrumentos/ConsultarInstrumentos.js'] }],

};
BCS_VENTANA.Clasificaciondeactivos= {
	    TITLE : "UI_CLASIFICACION_DE_ACTIVOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "650px",
	    VIEW   : 'INS/Clasificaciondeactivos/Clasificaciondeactivos.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: [	'INS/Clasificaciondeactivos/Clasificaciondeactivos.js',
	    													'INS/Clasificaciondeactivos/NuevoMercado.js', //Paneles 
	    													'INS/Clasificaciondeactivos/Crear/PanelMercado/CrearMercado.js',// pestana crear mercado 
	    													'INS/Clasificaciondeactivos/Crear/PanelSubMercado/CrearSubMercado.js',// pestana crear submercado
	    													'INS/Clasificaciondeactivos/Crear/PanelClasificacionInstrumento/CrearClasificacionInstrumento.js',//pestana crear cla instrumento
	    													'INS/Clasificaciondeactivos/Modificar/PanelModificarMercado/ModificarMercado.js', //Pestana Modificar mercado
	    													'INS/Clasificaciondeactivos/Modificar/PanelModificarSubMercado/ModificarSubMercado.js',//Pestana Modificar submercado
	    													'INS/Clasificaciondeactivos/Modificar/PanelModificarClasificacionInstrumento/ModificarClasificacionInstrumento.js',//Pestana Modificar Clasificacion Instrumento
	    													'INS/Clasificaciondeactivos/Eliminar/PanelEliminarMercado/EliminarMercado.js',
	    													'INS/Clasificaciondeactivos/Eliminar/PanelEliminarSubMercado/EliminarSubMercado.js',
	    													'INS/Clasificaciondeactivos/Eliminar/PanelEliminarInstrumento/EliminarInstrumento.js',
	    													'INS/Clasificaciondeactivos/Eliminar/PanelEliminarClasificacionInstrumento/EliminarClasificacionInstrumento.js',//Pestana Eliminar Clasificacion Instrumento
	    													'INS/Clasificaciondeactivos/Clasificar/ClasificarActivo.js',	    														    													
	    													'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] }],

};

BCS_VENTANA.CargaEntidadVectorPrecio = {
	    TITLE : "UI_CARGA_VECTOR_PRECIO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "525px",
	    VIEW   : 'INS/CargaVectorPrecio/CargaEntidadVectorPrecio.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['INS/CargaVectorPrecio/CargaEntidadVectorPrecio.js'] }],
};

BCS_VENTANA.ConsultarVectorPrecio = {
	    TITLE : "UI_CONSULTAR_VECTOR_PRECIO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "425px",
	    VIEW   : 'INS/ConsultarVectorPrecio/ConsultarVectorPrecio.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['INS/ConsultarVectorPrecio/ConsultarVectorPrecio.js'] }],
};

BCS_VENTANA.MantenedorInstrumento = {
    TITLE : "UI_CREAR_INSTRUMENTO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "640px",
    HEIGHT : "480px",
    VIEW   : 'INS/CrearModificarInstrumento/MantenedorInstrumento.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['INS/CrearModificarInstrumento/MantenedorInstrumento.js'] }],

};

BCS_VENTANA.CrearModificarTasasGenericas = {
	    TITLE : "UI_CREAR_TASAS_GENERICAS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "457px",
	    VIEW   : 'INS/CrearModificarTasasGenericas/CrearModificarTasasGenericas.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['INS/CrearModificarTasasGenericas/CrearModificarTasasGenericas.js'] }],

	};

BCS_VENTANA.ModificarTasasGenericas = {
	    TITLE : "UI_MODIFICAR_TASAS_GENERICAS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "600px",
	    HEIGHT : "275px",
	    VIEW   : 'INS/CrearModificarTasasGenericas/ModificarTasasGenericas.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['INS/CrearModificarTasasGenericas/ModificarTasasGenericas.js'] }],

	};

BCS_VENTANA.CrearTasasGenericas = {
	    TITLE : "UI_CREAR_TASAS_GENERICAS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "275px",
	    VIEW   : 'INS/CrearModificarTasasGenericas/CrearTasasGenericas.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['INS/CrearModificarTasasGenericas/CrearTasasGenericas.js'] }],

	};
	
BCS_VENTANA.busquedaInstrumentos = {
    TITLE : "UI_BUSQUEDA_INSTRUMENTO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "600px",
    HEIGHT : "360px",
    VIEW   : 'directives/bcs-input-instrumentos/busquedaInstrumentos.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['directives/bcs-input-instrumentos/busquedaInstrumentos.js'] } ],

};


BCS_VENTANA.busquedaInstrumentosDV = {
	    TITLE : "UI_BUSQUEDA_INSTRUMENTO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "600px",
	    HEIGHT : "360px",
	    VIEW   : 'directives/bcs-input-instrumentos-dv/busquedaInstrumentos-dv.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['directives/bcs-input-instrumentos-dv/busquedaInstrumentos-dv.js'] } ],

	};

BCS_VENTANA.busquedaInstrumentosGa = {
	    TITLE : "UI_BUSQUEDA_INSTRUMENTO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "600px",
	    HEIGHT : "360px",
	    VIEW   : 'directives/bcs-input-instrumentos-ga/busquedaInstrumentos-ga.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['directives/bcs-input-instrumentos-ga/busquedaInstrumentos-ga.js'] } ],

	};

//------------------- PLATAFORMA / COMERCIAL  ---------------------
BCS_VENTANA.PlataformaSeparadorComercial= {
		SEPARATE: true,
	    TITLE : "UI_COMERCIAL",
	    ENABLE : true
};


BCS_VENTANA.administrarModelosComerciales = {
    TITLE : "UI_MODELOS_COMERCIALES",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1000px",
    HEIGHT : "580px",
    VIEW   : '../../Plataforma/UI/COM/administrarCOM.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['COM/administrarCOM.js',
                                                        'COM/PlantillaComercial/PlantillaComercial.js',
                                                        'COM/PlantillaProductos/PlantillaProductos.js',
                                                        'COM/PlantillaCobros/PlantillaCobros.js',
                                                        'COM/PlantillaCobrosPeru/PlantillaCobrosPeru.js',
                                                        'COM/GrupoActivos/GrupoActivos.js',
                                                        'COM/PlantillaEmpleado/PlantillaEmpleado.js',
                                                        'COM/PlantillaLimites/PlantillaLimites.js',
                             				           ] } ],

};

BCS_VENTANA.ModalAdministrarGrupoActivo= {
    TITLE : "UI_ADMINISTRAR_GRUPO_ACTIVOS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "340px",
    HEIGHT : "230px",
    VIEW   : '../../Plataforma/UI/COM/GrupoActivos/ModalAdministrarGrupoActivos.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['COM/GrupoActivos/ModalAdministrarGrupoActivos.js' ] } ],

};

BCS_VENTANA.ModalAdministrarGrupoActivoDetalle = {
    TITLE : "UI_ADMINISTRAR_DETALLE_GRUPO_ACTIVOS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "440px",
    HEIGHT : "170px",
    VIEW   : '../../Plataforma/UI/COM/GrupoActivos/ModalAdministrarGrupoActivosDetalle.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['COM/GrupoActivos/ModalAdministrarGrupoActivosDetalle.js' ] } ],

};


BCS_VENTANA.modalAdministrarPlantillaCobros = {
	    TITLE : "UI_ADMINISTRAR_PLANTILLA_COBROS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "340px",
	    HEIGHT : "200px",
	    VIEW   : '../../Plataforma/UI/COM/bcs-plantilla-cobros/ModalAdministrarPlantillaCobros.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['COM/bcs-plantilla-cobros/ModalAdministrarPlantillaCobros.js' ] } ],

	};

BCS_VENTANA.modalAdministrarPlantillaCobrosPeru = {
	    TITLE : "UI_ADMINISTRAR_PLANTILLA_COBROS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "340px",
	    HEIGHT : "260px",
	    VIEW   : '../../Plataforma/UI/COM/bcs-plantilla-cobros-peru/ModalAdministrarPlantillaCobrosPeru.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['COM/bcs-plantilla-cobros-peru/ModalAdministrarPlantillaCobrosPeru.js'] } ],

	};

BCS_VENTANA.modalAdministrarPlantillaCobrosDetalle = {
    TITLE : "UI_ADMINISTRAR_DETALLE_PLANTILLA_COBROS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "440px",
    HEIGHT : "350px",
    VIEW   : '../../Plataforma/UI/COM/bcs-plantilla-cobros/ModalAdministrarPlantillaCobrosDetalle.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['COM/bcs-plantilla-cobros/ModalAdministrarPlantillaCobrosDetalle.js' ] } ],

};

BCS_VENTANA.modalAdministrarPlantillaCobrosDetallePeru = {
    TITLE : "UI_ADMINISTRAR_DETALLE_PLANTILLA_COBROS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "960px",
    HEIGHT : "580px",
    VIEW   : '../../Plataforma/UI/COM/bcs-plantilla-cobros-peru/ModalAdministrarPlantillaCobrosDetallePeru.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['COM/bcs-plantilla-cobros-peru/ModalAdministrarPlantillaCobrosDetallePeru.js' ] } ],

};

BCS_VENTANA.modalEditarCobrosPeru = {
	    TITLE : "UI_EDITAR_COBROS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "300px",
	    VIEW   : '../../Plataforma/UI/COM/bcs-plantilla-cobros-peru/ModalEditarCobroPeru.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['COM/bcs-plantilla-cobros-peru/ModalEditarCobroPeru.js' ] } ],

	};

BCS_VENTANA.modalClientesAfectados = {
    TITLE : "UI_CLIENTES_AFECTADOS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "500px",
    HEIGHT : "400px",
    VIEW   : '../../Plataforma/UI/COM/bcs-plantilla-cobros/ModalClientesAfectados.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['COM/bcs-plantilla-cobros/ModalClientesAfectados.js' ] } ],

};

BCS_VENTANA.ModalAdministrarPlantillaProductos = {
    TITLE : "UI_ADMINISTRAR_PLANTILLA_PRODUCTOS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "340px",
    HEIGHT : "231px",
    VIEW   : '../../Plataforma/UI/COM/bcs-plantilla-productos/ModalAdministrarPlantillaProductos.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['COM/bcs-plantilla-productos/ModalAdministrarPlantillaProductos.js' ] } ],

};

BCS_VENTANA.ModalAdministrarPlantillaProductosDetalle = {
    TITLE : "UI_ADMINISTRAR_DETALLE_PLANTILLA_PRODUCTOS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "440px",
    HEIGHT : "250px",
    VIEW   : '../../Plataforma/UI/COM/bcs-plantilla-productos/ModalAdministrarPlantillaProductosDetalle.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['COM/bcs-plantilla-productos/ModalAdministrarPlantillaProductosDetalle.js' ] } ],

};

BCS_VENTANA.ConfiguracionCobros = {
    TITLE : "UI_CONFIGURACION_COBROS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1100px",
    HEIGHT : "520px",
    VIEW   : 'COM/ConfiguracionCobros/ConfiguracionCobros.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['COM/ConfiguracionCobros/ConfiguracionCobros.js' ] } ],

};


BCS_VENTANA.IngresoCobro = {
	    TITLE : "UI_INGRESO_COBRO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "920px",
	    HEIGHT : "520px",
	    VIEW   : 'COM/ConfiguracionCobros/IngresoCobro.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['COM/ConfiguracionCobros/IngresoCobro.js' ] } ],

};


BCS_VENTANA.AdministrarValoresCobro = {
    TITLE : "UI_ADMINISTRAR_VALORES_DE_COBRO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "920px",
    HEIGHT : "523px",
    VIEW   : '../../Plataforma/UI/COM/ValoresCobro/AdministrarValoresCobro.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['COM/ValoresCobro/AdministrarValoresCobro.js' ] } ],

};

BCS_VENTANA.ModalAdministrarValorCobro = {
	    TITLE : "UI_ADMINISTRAR_VALOR_DE_COBRO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "420px",
	    HEIGHT : "320px",
	    VIEW   : '../../Plataforma/UI/COM/ValoresCobro/ModalAdministrarValorCobro.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['COM/ValoresCobro/ModalAdministrarValorCobro.js' ] } ],

};

BCS_VENTANA.modalAgregarProductoDetalle = {
	    TITLE : "UI_AGREGAR_PRODUCTO_DETALLE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "340px",
	    HEIGHT : "260px",
	    VIEW   : '../../Plataforma/UI/COM/bcs-plantilla-productos/ModalAgregarProductoDetalle.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['COM/bcs-plantilla-productos/ModalAgregarProductoDetalle.js' ] } ],

};


BCS_VENTANA.modalAdministrarPlantillaLimites = {
    TITLE : "UI_ADMINISTRAR_PLANTILLA_LIMITES",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "440px",
    HEIGHT : "250px",
    VIEW   : '../../Plataforma/UI/COM/bcs-plantilla-limites/ModalAdministrarPlantillaLimites.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['COM/bcs-plantilla-limites/ModalAdministrarPlantillaLimites.js' ] } ],

};

BCS_VENTANA.modalAdministrarPlantillaLimitesDetalle = {
    TITLE : "UI_ADMINISTRAR_DETALLE_PLANTILLA_LIMITES",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "440px",
    HEIGHT : "300px",
    VIEW   : '../../Plataforma/UI/COM/bcs-plantilla-limites/ModalAdministrarPlantillaLimitesDetalle.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['COM/bcs-plantilla-limites/ModalAdministrarPlantillaLimitesDetalle.js' ] } ],

};


BCS_VENTANA.AdministrarModelosTipoCliente = {
	    TITLE : "UI_MODELOS_TIPO_CLIENTE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "500px",
	    VIEW   : '../../Plataforma/UI/IND/AdministrarModelosTipoCliente.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['IND/AdministrarModelosTipoCliente.js',
	                                                        'IND/PanelIndicadoresCliente.js',
	                                                        'IND/PanelIndicadoresPortafolio.js',
	                                                        'IND/PanelRequisitosCliente.js',
	                                                        'IND/PanelRequisitosPortafolio.js',
	                                                        'IND/PanelPreferencias.js',
															'IND/PanelInformacionCliente.js',
	                             				           ] } ],

};

BCS_VENTANA.ModalTipoCliente = {
	    TITLE : "UI_MODAL_TIPO_CLIENTE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "365px",
	    HEIGHT : "200px",
	    VIEW   : '../../Plataforma/UI/IND/ModalTipoCliente.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['IND/ModalTipoCliente.js'] } ],	
}


BCS_VENTANA.administrarRequisitosOperativos = {
	    TITLE : "UI_REQUISITOS_OPERATIVOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "558px",
	    VIEW   : '../../Plataforma/UI/REQ/administrarRequisitosOperativos.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['REQ/administrarRequisitosOperativos.js',
	                             				           ] } ],

};


BCS_VENTANA.agregarRequisitoOperativo = {
    TITLE : "UI_MODIFICAR_REQUISITO_OPERATIVO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "500px",
    HEIGHT : "270px",
    VIEW   : '../../Plataforma/UI/REQ/ModalRequisitoOperativo.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['REQ/ModalRequisitoOperativo.js']}],

};

BCS_VENTANA.administrarPlantillasEjecutivos = {
	    TITLE : "UI_PLANTILLAS_EJECUTIVO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "490px",
	    VIEW   : '../../Plataforma/UI/COM/PlantillaEmpleado/PlantillaEmpleado.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['COM/PlantillaEmpleado/PlantillaEmpleado.js',
	                             				           ] } ],

};


BCS_VENTANA.modalAdministrarPlantillaEjecutivos = {
	    TITLE : "UI_ADMINISTRAR_PLANTILLA_EJECUTIVO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "340px",
	    HEIGHT : "160px",
	    VIEW   : '../../Plataforma/UI/COM/PlantillaEmpleado/ModalPlantillaEmpleado.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['COM/PlantillaEmpleado/ModalPlantillaEmpleado.js' ] } ],

};

BCS_VENTANA.modalAdministrarPlantillaEjecutivosDetalle = {
	    TITLE : "UI_ADMINISTRAR_PLANTILLA_EJECUTIVO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "340px",
	    HEIGHT : "250px",
	    VIEW   : '../../Plataforma/UI/COM/PlantillaEmpleado/ModalPlantillaEmpleadoDetalle.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['COM/PlantillaEmpleado/ModalPlantillaEmpleadoDetalle.js' ] } ],

};


BCS_VENTANA.MantenedorIndicadores = {
	    TITLE : "UI_MANTENEDOR_INDICADORES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "400px",
	    VIEW   : 'IND/MantenedorIndicadores/MantenedorIndicadores.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ 'IND/MantenedorIndicadores/MantenedorIndicadores.js'] } ],

};


BCS_VENTANA.AgregarModificarTiposIndicadores = {
	    TITLE : "Agregar/Modificar Indicadores",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "400px",
	    VIEW   : 'IND/MantenedorIndicadores/ModalTipoIndicadores.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ 'IND/MantenedorIndicadores/ModalTipoIndicadores.js'] } ],

};

BCS_VENTANA.AgregarModificarDetalleTiposIndicadores = {
	    TITLE : "Agregar/Modificar Detalle Indicadores",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "180px",
	    VIEW   : 'IND/MantenedorIndicadores/ModalDetalleIndicadores.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ 'IND/MantenedorIndicadores/ModalDetalleIndicadores.js'] } ],

};

BCS_VENTANA.MantenedorContratosComisiones = {
		TITLE : "UI_DETALLE_CONTRATOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
	    HEIGHT : "700px",
	    VIEW   : 'COM/MantenedorContratosComisiones/MantenedorContratosComisiones.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['COM/MantenedorContratosComisiones/MantenedorContratosComisiones.js',
	    													'COM/MantenedorContratosComisiones/PanelContratos.js',
	    													'COM/MantenedorContratosComisiones/PanelConvenios.js']
	    				}],
};

BCS_VENTANA.agregarModificarContratoComision = {
		TITLE : "UI_CONTRATO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "250px",
	    VIEW   : 'COM/MantenedorContratosComisiones/ModalContratoComision.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['COM/MantenedorContratosComisiones/ModalContratoComision.js'] }],
};

BCS_VENTANA.agregarModificarConvenioComision = {
		TITLE : "UI_CONVENIO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "200px",
	    VIEW   : 'COM/MantenedorContratosComisiones/ModalConvenioComision.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['COM/MantenedorContratosComisiones/ModalConvenioComision.js'] }],
};

BCS_VENTANA.agregarModificarDetalleContratoComision = {
		TITLE : "UI_DETALLE_CONTRATO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "450px",
	    HEIGHT : "450px",
	    VIEW   : 'COM/MantenedorContratosComisiones/ModalDetalleContratoComision.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['COM/MantenedorContratosComisiones/ModalDetalleContratoComision.js'] }],
};

BCS_VENTANA.agregarModificarSubdetalleContratoComision = {
		TITLE : "UI_SUBDETALLE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "250px",
	    HEIGHT : "160px",
	    VIEW   : 'COM/MantenedorContratosComisiones/ModalSubdetalleContratoComision.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['COM/MantenedorContratosComisiones/ModalSubdetalleContratoComision.js'] }],
};

BCS_VENTANA.MantenedorTipoRelaciones = {
	    TITLE : "UI_MANTENEDOR_TIPO_RELACIONES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "650px",
	    VIEW   : 'REL/MantenedorTipoRelaciones.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ 'REL/MantenedorTipoRelaciones.js'] } ],

};


BCS_VENTANA.agregarModificarTipoRelacion = {
    TITLE : "UI_ADMINISTRAR_TIPO_RELACION",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "550px",
    HEIGHT : "330px",
    VIEW   : 'REL/ModalTipoRelacion.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['REL/ModalTipoRelacion.js']}],

};

BCS_VENTANA.MantenedorDocumentos = {
	    TITLE : "UI_MANTENEDOR_DOCUMENTOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "400px",
	    VIEW   : 'DOC/MantenedorDocumentos/MantenedorDocumentos.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['DOC/MantenedorDocumentos/MantenedorDocumentos.js'] } ],

};


BCS_VENTANA.ModalDocumentos = {
	    TITLE : "UI_MANTENEDOR_DOCUMENTOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "810px",
	    VIEW   : 'DOC/MantenedorDocumentos/ModalDocumentos.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['DOC/MantenedorDocumentos/ModalDocumentos.js'] } ],

};

BCS_VENTANA.ModalConfirmacion = {
	    TITLE : "UI_MANTENEDOR_DOCUMENTOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "550px",
	    VIEW   : 'DOC/MantenedorDocumentos/ModalConfirmacion.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['DOC/MantenedorDocumentos/ModalConfirmacion.js','DOC/MantenedorDocumentos/ModalDocumentos.js'] } ],

};

BCS_VENTANA.AdministrarPreferenciasPago = {
	    TITLE : "UI_ADMINISTRAR_PREFERENCIAS_PAGO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "600px",
	    VIEW   : '../../Plataforma/UI/PAG/AdministrarPreferenciasPago.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['PAG/AdministrarPreferenciasPago.js','PAG/bcs-preferencias-pago/bcs-preferencias-pago.js',
	                             				           ] } ],

};


BCS_VENTANA.ModalPreferenciaPago = {
	    TITLE : "UI_ADMINISTRAR_PREFERENCIAS_PAGO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "300px",
	    VIEW   : '../../Plataforma/UI/PAG/bcs-preferencias-pago/ModalPreferenciaPago.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['PAG/bcs-preferencias-pago/ModalPreferenciaPago.js',
	                             				           ] } ],

};



//------------------- PLATAFORMA / NEGOCIO  ---------------------
BCS_VENTANA.PlataformaSeparadorNegocio= {
		SEPARATE: true,
	    TITLE : "UI_NEGOCIO",
	    ENABLE : true
};



BCS_VENTANA.CierreModulos = {
	    TITLE : "UI_CIERRE_MODULOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "600px",
	    VIEW   : 'PLA/CierreModulos/CierreModulos.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, 
			files: [ 'PLA/CierreModulos/CierreModulos.js',
			         'PLA/CierreModulos/CierreModulosGrid.js',
					 'PLA/CierreModulos/Procesos/ProcesosCierre.js',
					 'PLA/CierreModulos/EntregaSistema/EntregaSistema.js']
			} ],

};


BCS_VENTANA.RequisitosCierre = {
	    TITLE : "UI_CONFIGURACION_CIERRE_MODULO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "600px",
	    VIEW   : 'PLA/CierreModulos/RequisitosCierre/RequisitosCierre.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, 
			files: ['PLA/CierreModulos/RequisitosCierre/RequisitosCierre.js',
			        'PLA/CierreModulos/RequisitosCierre/RequisitosProcesosFunciones.js',
			        'PLA/CierreModulos/RequisitosCierre/RequisitosAperturaCierre.js']
			} ],

};

BCS_VENTANA.MantenedorCobertura = {
    TITLE : "Mantenedor Cobertura",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "600px",
    VIEW   : 'GTA/MantenedorCobertura/MantenedorCobertura.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, 
    	files: ['GTA/MantenedorCobertura/MantenedorCobertura.js',
    	        'GTA/ConfiguracionGTA.js'] } ],

};




BCS_VENTANA.EditarCobertura = {
	    TITLE : "UI_EDITAR_COBERTURA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "600px",
	    HEIGHT : "350px",
	    VIEW   : 'GTA/MantenedorCobertura/EditarCobertura.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, 
	    	files: ['GTA/MantenedorCobertura/EditarCobertura.js',
	    	        'GTA/ConfiguracionGTA.js'] } ],

};


//------------------- PLATAFORMA / LIMITES ---------------------
BCS_VENTANA.MantenedorLimites = {
    TITLE : "UI_MANTENEDOR_LIMITES",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1100px",
    HEIGHT : "600px",
    //VIEW   : 'LIM/MantenedorLimites/MantenedorLimites.html',
    VIEW   : 'LIM/AdministrarLimites.html',
    PARAMS : {},
    //DEPENDENCIES : [{ type: "js",  cache: true, files: [ 'LIM/MantenedorLimites/MantenedorLimites.js','LIM/ConfiguracionLIM.js','LIM/MantenedorLimites/CrearLimitesModal.js'] } ],
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ 'LIM/AdministrarLimites.js',
                                                         'LIM/CuentaPropia/PanelLimitesCuentaPropiaRVPlazo.js',
                                                         'LIM/CuentaPropia/PanelLimitesCuentaPropiaRVEmisor.js',
                                                         'LIM/CuentaPropia/PanelLimitesCuentaPropiaRVClasifRiesgo.js',
                                                         'LIM/CuentaPropia/PanelLimitesCuentaPropiaRVMargenTasas.js',
                                                         'LIM/CuentaPropia/PanelLimitesCuentaPropiaRFPlazo.js',
                                                         'LIM/CuentaPropia/PanelLimitesCuentaPropiaRFEmisor.js',
			                                             'LIM/CuentaPropia/PanelLimitesCuentaPropiaRFMoneda.js',
                                                         'LIM/CuentaPropia/PanelLimitesCuentaPropiaRFClasifRiesgo.js',
                                                         'LIM/CuentaPropia/PanelLimitesCuentaPropiaRFMargenTasas.js',
														                             'LIM/CuentaPropia/PanelLimitesCuentaPropiaIFPlazo.js',
                                                         'LIM/CuentaPropia/PanelLimitesCuentaPropiaIFEmisor.js',
			                                             'LIM/CuentaPropia/PanelLimitesCuentaPropiaIFMoneda.js',
                                                         'LIM/CuentaPropia/PanelLimitesCuentaPropiaIFClasifRiesgo.js',
                                                         'LIM/CuentaPropia/PanelLimitesCuentaPropiaIFMargenTasas.js', 
														 'LIM/CuentaPropia/PanelLimitesCuentaPropiaSpot.js',
                                                         'LIM/CuentaPropia/PanelLimitesCuentaPropiaForward.js',
                                                         'LIM/CuentaPropia/PanelLimitesCuentaPropiaCarteraTipIns.js',
                                                         'LIM/CuentaPropia/PanelLimitesCuentaPropiaCarteraGruEmi.js',
                                                         'LIM/CuentaPropia/PanelLimitesCuentaPropiaCarteraEmisor.js',
                                                         'LIM/CuentaPropia/PanelLimitesCuentaPropiaAgente.js'
                                                         ] } ],

};



BCS_VENTANA.ModalLimitesCuentaPropia = {
    TITLE : "UI_AGREGAR_MODIFICAR_LIMITE_CUENTA_PROPIA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "450px",
    HEIGHT : "280px",
    VIEW   : '../../Plataforma/UI/LIM/CuentaPropia/ModalLimitesCuentaPropia.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Plataforma/UI/LIM/CuentaPropia/ModalLimitesCuentaPropia.js' ] } ],

};

BCS_VENTANA.ModalLimitesCuentaPropiaMargenTasasEditar = {
    TITLE : "UI_MODIFICAR_LIMITE_CUENTA_PROPIA_MARGEN_TASAS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1150px",
    HEIGHT : "450px",
    VIEW   : '../../Plataforma/UI/LIM/CuentaPropia/ModalLimitesCuentaPropia.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Plataforma/UI/LIM/CuentaPropia/ModalLimitesCuentaPropia.js' ] } ],

};

BCS_VENTANA.ModalLimitesCuentaPropiaEmisor = {
    TITLE : "UI_AGREGAR_MODIFICAR_EMISOR",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "400px",
    HEIGHT : "180px",
    VIEW   : '../../Plataforma/UI/LIM/CuentaPropia/ModalLimitesCuentaPropiaEmisor.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Plataforma/UI/LIM/CuentaPropia/ModalLimitesCuentaPropiaEmisor.js' ] } ],

};

BCS_VENTANA.ModalLimitesCuentaPropiaRangos = {
    TITLE : "UI_AGREGAR_MODIFICAR_RANGO_MONTO_PLAZO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "350px",
    HEIGHT : "450px",
    VIEW   : '../../Plataforma/UI/LIM/CuentaPropia/ModalLimitesCuentaPropiaRangos.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Plataforma/UI/LIM/CuentaPropia/ModalLimitesCuentaPropiaRangos.js' ] } ],

};

BCS_VENTANA.ModalLimitesCuentaPropiaSpotFwCartera = {
    TITLE : "UI_AGREGAR_MODIFICAR_LIMITE_SPOT_FW_CARTERA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "450px",
    HEIGHT : "330px",
    VIEW   : '../../Plataforma/UI/LIM/CuentaPropia/ModalLimitesCuentaPropiaSpotFwCartera.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Plataforma/UI/LIM/CuentaPropia/ModalLimitesCuentaPropiaSpotFwCartera.js' ] } ],

};

BCS_VENTANA.ModalLimiteCuentaPropiaRFIFPlazoVencimiento = {
	TITLE : "UI_AGREGAR_MODIFICAR_LIMITE_PLAZO_VENCIMIENTO",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "450px",
	HEIGHT : "330px",
	VIEW   : '../../Plataforma/UI/LIM/CuentaPropia/ModalLimiteCuentaPropiaRFIFPlazoVencimiento.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Plataforma/UI/LIM/CuentaPropia/ModalLimiteCuentaPropiaRFIFPlazoVencimiento.js' ] } ],

};

BCS_VENTANA.ModalLimiteCuentaPropiaRFIFEmisor = {
	TITLE : "UI_AGREGAR_MODIFICAR_LIMITE_EMISOR",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "450px",
	HEIGHT : "330px",
	VIEW   : '../../Plataforma/UI/LIM/CuentaPropia/ModalLimiteCuentaPropiaRFIFEmisor.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Plataforma/UI/LIM/CuentaPropia/ModalLimiteCuentaPropiaRFIFEmisor.js' ] } ],

};

BCS_VENTANA.ModalLimiteCuentaPropiaRFIFMoneda = {
	TITLE : "UI_AGREGAR_MODIFICAR_LIMITE_EMISOR",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "450px",
	HEIGHT : "330px",
	VIEW   : '../../Plataforma/UI/LIM/CuentaPropia/ModalLimiteCuentaPropiaRFIFMoneda.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Plataforma/UI/LIM/CuentaPropia/ModalLimiteCuentaPropiaRFIFMoneda.js' ] } ],

};

BCS_VENTANA.ModalLimiteCuentaPropiaRFIFClasificacionRiesgo = {
	TITLE : "UI_AGREGAR_MODIFICAR_LIMITE_CLASIFICACION_RIESGO",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "450px",
	HEIGHT : "330px",
	VIEW   : '../../Plataforma/UI/LIM/CuentaPropia/ModalLimiteCuentaPropiaRFIFClasificacionRiesgo.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Plataforma/UI/LIM/CuentaPropia/ModalLimiteCuentaPropiaRFIFClasificacionRiesgo.js' ] } ],

};

BCS_VENTANA.ModalLimiteCuentaPropiaRFIFMargenTasas = {
	TITLE : "UI_AGREGAR_MODIFICAR_LIMITE_MARGEN_TASAS",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "450px",
	HEIGHT : "330px",
	VIEW   : '../../Plataforma/UI/LIM/CuentaPropia/ModalLimiteCuentaPropiaRFIFMargenTasas.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Plataforma/UI/LIM/CuentaPropia/ModalLimiteCuentaPropiaRFIFMargenTasas.js' ] } ],
	
};

BCS_VENTANA.ModalLimiteCuentaPropiaSpot = {
    TITLE : "UI_AGREGAR_MODIFICAR_LIMITE_SPOT",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "450px",
    HEIGHT : "330px",
    VIEW   : '../../Plataforma/UI/LIM/CuentaPropia/ModalLimiteCuentaPropiaSpot.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Plataforma/UI/LIM/CuentaPropia/ModalLimiteCuentaPropiaSpot.js' ] } ],

};

BCS_VENTANA.ModalLimiteCuentaPropiaForward = {
    TITLE : "UI_AGREGAR_MODIFICAR_LIMITE_FORWARD",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "450px",
    HEIGHT : "330px",
    VIEW   : '../../Plataforma/UI/LIM/CuentaPropia/ModalLimiteCuentaPropiaForward.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Plataforma/UI/LIM/CuentaPropia/ModalLimiteCuentaPropiaForward.js' ] } ],

};


BCS_VENTANA.MantenedorGrupoEmisores = {
    TITLE : "UI_GRUPO_EMISORES",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "700px",
    HEIGHT : "500px",
    VIEW   : '../../Plataforma/UI/LIM/CuentaPropia/MantenedorGrupoEmisores.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Plataforma/UI/LIM/CuentaPropia/MantenedorGrupoEmisores.js' ] }], 

};

BCS_VENTANA.ModalGrupoEmisores = {
    TITLE : "UI_AGREGAR_MODIFICAR_GRUPO_EMISORES",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "400px",
    HEIGHT : "220px",
    VIEW   : '../../Plataforma/UI/LIM/CuentaPropia/ModalGrupoEmisores.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Plataforma/UI/LIM/CuentaPropia/ModalGrupoEmisores.js' ] } ],

};

BCS_VENTANA.ModalGrupoEmisorDetalle = {
    TITLE : "UI_AGREGAR_MODIFICAR_EMISOR",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "400px",
    HEIGHT : "180px",
    VIEW   : '../../Plataforma/UI/LIM/CuentaPropia/ModalGrupoEmisorDetalle.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Plataforma/UI/LIM/CuentaPropia/ModalGrupoEmisorDetalle.js' ] } ],

};


BCS_VENTANA.ConsultarLimites = {
    TITLE : "UI_CONSULTAR_LIMITES",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1100px",
    HEIGHT : "800px",
    VIEW   : 'LIM/ConsultarLimites/ConsultarLimites.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ 'LIM/ConsultarLimites/ConsultarLimites.js',
                                                         'LIM/ConsultarLimites/PanelLimitesCliente.js',
                                                         'LIM/ConsultarLimites/PanelLimitesCuentaPropia.js',
                                                         'LIM/ConsultarLimites/PanelEvaluacionLimites.js',
                                                         'LIM/ConsultarLimites/PanelLimitesAgente.js'
                                                         ] } ],

};



//-------------- FIN PLATAFORMA / LIMITES ---------------------

BCS_VENTANA.MantenedorBoletinesCli= {
	TITLE : "UI_BOLETINES_OPTIMUS",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "750px",
	HEIGHT : "435px",
	VIEW   : 'PLA/MantenedorBoletines/ConsultarBoletinesCli.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: ['PLA/MantenedorBoletines/ConsultarBoletinesCli.js'] } ],

};

BCS_VENTANA.MantenedorBoletines = {
	TITLE  : "UI_MANTENEDOR_BOLETINES",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "750px",
	HEIGHT : "435px",
	VIEW   : 'PLA/MantenedorBoletines/ConsultarBoletines.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: ['PLA/MantenedorBoletines/ConsultarBoletines.js']}],
};

BCS_VENTANA.OperarBoletinModal = {
	TITLE : "UI_SUBIR_BOLETIN",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "420px",
	HEIGHT : "180px",
	VIEW   : 'PLA/MantenedorBoletines/OperarBoletinModal.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: ['PLA/MantenedorBoletines/OperarBoletinModal.js']}],

};


BCS_VENTANA.MantenedorExigibilidad = {
    TITLE : "UI_MANTENEDOR_EXIGIBILIDAD",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "960px",
    HEIGHT : "650px",
    VIEW   : 'GTA/MantenedorExigibilidad/MantenedorExigibilidad.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['GTA/MantenedorExigibilidad/MantenedorExigibilidad.js','GTA/ConfiguracionGTA.js'] } ],

};

BCS_VENTANA.ModalExigibilidad = {
    TITLE : "UI_RELACION_INSTRUMENTO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "480px",
    HEIGHT : "270px",
    VIEW   : 'GTA/MantenedorExigibilidad/ModalExigibilidad.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['GTA/MantenedorExigibilidad/ModalExigibilidad.js'] } ],

};


BCS_VENTANA.AperturaCierreModulos = {
    TITLE : "UI_APERTURA_CIERRE_MODULOS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "auto",
    VIEW   : 'PAU/AperturaCierreModulos/AperturaCierreModulos.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['PAU/AperturaCierreModulos/AperturaCierreModulos.js',
                                                        'PAU/PautaConfiguracionGrids.js'] } ],

};

BCS_VENTANA.EntregarSistema = {
	    TITLE : "UI_ENTREGAR_SISTEMA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "auto",
	    VIEW   : 'PAU/AperturaCierreModulos/EntregarSistema.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['PAU/AperturaCierreModulos/EntregarSistema.js'] } ],

	};
//------------------- PLATAFORMA / PAUTA  ---------------------
BCS_VENTANA.PlataformaSeparadorPauta= {
		SEPARATE: true,
	    TITLE : "UI_PAUTA",
	    ENABLE : true
};



BCS_VENTANA.MantenedorPauta = {
    TITLE : "UI_CONSULTAR_PAUTA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "960px",
    HEIGHT : "auto",
    VIEW   : 'PAU/MantenedorPauta/MantenedorPauta.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, 
		files: [ 'PAU/MantenedorPauta/MantenedorPauta.js','PAU/PautaConfiguracionGrids.js']
		} ],

};

BCS_VENTANA.InterfazLiquidezIntradia = {
	TITLE  : "UI_INTERFAZ_LIQUIDEZ_INTRADIA",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "500px",
	HEIGHT : "auto",    
	VIEW   : 'PAU/InterfazLiquidezIntradia/InterfazLiquidezIntradia.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, 
		files: [ 'PAU/InterfazLiquidezIntradia/InterfazLiquidezIntradia.js']
	}],
};

BCS_VENTANA.MantenedorPautaDetalle = {
    TITLE : "UI_CONSULTAR_PAUTA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "800px",
    HEIGHT : "500px",
    VIEW   : 'PAU/MantenedorPauta/MantenedorPautaDetalle.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, 
		files: [ 'PAU/MantenedorPauta/MantenedorPautaDetalle.js']
		} ],

};

//------------------- PLATAFORMA / AUDITORIA ---------------------
BCS_VENTANA.PlataformaSeparadorAuditoria = {
		SEPARATE: true,
	    TITLE : "UI_CONTROL",
	    ENABLE : true
};


BCS_VENTANA.ConsultaLog = {
	    TITLE : "UI_CONSULTA_LOGS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1200px",
	    HEIGHT : "auto",
	    VIEW   : 'PLA/ControlProcesos/ControlProcesos.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['PLA/ControlProcesos/ControlProcesos.js'] }], 

};


BCS_VENTANA.LogSistema = {
	    TITLE : "UI_MENSAJES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "270px",
	    VIEW   : 'PLA/RegistroMensajes/RegistroMensajes.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ 'PLA/RegistroMensajes/RegistroMensajes.js'] } ],

};

BCS_VENTANA.MantenedorAgenda = {
	    TITLE : "UI_CONSULTAR_AGENDA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "960px",
	    HEIGHT : "auto",
	    VIEW   : 'BAT/MantenedorAgenda/MantenedorAgenda.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['BAT/MantenedorAgenda/MantenedorAgenda.js'] } ],

};


BCS_VENTANA.MantenedorPrograma = {
	    TITLE : "UI_CONSULTAR_PROGRAMAS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "960px",
	    HEIGHT : "auto",
	    VIEW   : 'BAT/MantenedorPrograma/MantenedorPrograma.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['BAT/MantenedorPrograma/MantenedorPrograma.js','BAT/MantenedorProgramaGrid.js'] } ],

};


BCS_VENTANA.ConsultaGeneralRegistrosSistema = {
	    TITLE : "UI_CONSULTA_REGISTRO_SISTEMA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "450px",
	    HEIGHT : "auto",
	    VIEW   : 'PLA/RegistroGeneral/ConsultaGeneralRegistroSistema.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['PLA/RegistroGeneral/ConsultaGeneralRegistroSistema.js'] } ],

};


//------------------- PLATAFORMA / NOTIFICACIONES  ---------------------
BCS_VENTANA.PlataformaSeparadorNotificaciones = {
		SEPARATE: true,
	    TITLE : "UI_ADMINISTRAR_NOTIFICACIONES",
	    ENABLE : true
};


BCS_VENTANA.MantenedorEventos = {
	    TITLE : "Maestro Notificaciones",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "960px",
	    HEIGHT : "auto",
	    VIEW   : 'EVT/MantenedorEvento/MantenedorEvento.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['EVT/MantenedorEvento/MantenedorEvento.js','EVT/MantenedorEventoGrid.js'] } ],

};

BCS_VENTANA.MantenedorMensajes = {
	    TITLE : "Mantenedor Mensajes",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "820px",
	    HEIGHT : "600px",
	    VIEW   : 'EVT/MantenedorMensaje/MantenedorMensaje.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['EVT/MantenedorMensaje/MantenedorMensaje.js'] } ],

};

BCS_VENTANA.ConfigurarMensajes = {
	    TITLE : "UI_AGREGAR_MENSAJE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "600px",
	    HEIGHT : "400px",
	    VIEW   : 'EVT/MantenedorMensaje/ConfigurarMensajes.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['EVT/MantenedorMensaje/ConfigurarMensajes.js'] } ],

};

BCS_VENTANA.AsociarTiposDeEventos = {
	    TITLE : "Asociar Tipo de Eventos",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "560px",
	    HEIGHT : "360px",
	    VIEW   : 'EVT/MantenedorEvento/AsociarTipoEvento.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['EVT/MantenedorEvento/AsociarTipoEvento.js','EVT/MantenedorEventoGrid.js'] } ],

};


 
BCS_VENTANA.MantenedorPlantilla = {
	    TITLE : "Adminsitrador de Plantillas",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "960px",
	    HEIGHT : "auto",
	    VIEW   : 'EVT/MantenedorPlantilla/MantenedorPlantilla.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['EVT/MantenedorPlantilla/MantenedorPlantilla.js','EVT/MantenedorEventoGrid.js'
                                                        ] } ],

};
 
BCS_VENTANA.BlotterModificarPlantilla = {
	    TITLE : "Modificar Plantilla Notificación",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1200px",
	    HEIGHT : "auto",
	    VIEW   : 'EVT/MantenedorPlantilla/BlotterModificarPlantilla.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: false, files: ['EVT/MantenedorPlantilla/BlotterModificarPlantilla.js'
                                                         ] } ],

};

BCS_VENTANA.BlotterAgregarPlantilla = {
	    TITLE : "Nueva Plantilla Notificación",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1200px",
	    HEIGHT : "auto",
	    VIEW   : 'EVT/MantenedorPlantilla/BlotterAgregarPlantilla.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: false, files: ['EVT/MantenedorPlantilla/BlotterAgregarPlantilla.js'
                                                         ] } ],

}; 

BCS_VENTANA.ReEnviarNotificacion = {
	    TITLE : "ReEnviar Notificacion",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "auto",
	    VIEW   : 'EVT/ConsultarNotificaciones/ReEnviarNotificacion.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: false, files: ['EVT/ConsultarNotificaciones/ReEnviarNotificacion.js'
                                                         ] } ],

};

BCS_VENTANA.ConfirmarNotificacion = {
	    TITLE : "Confirmar Notificacion",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "auto",
	    VIEW   : 'EVT/ConsultarNotificaciones/ConfirmarNotificacion.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: false, files: ['EVT/ConsultarNotificaciones/ConfirmarNotificacion.js'
                                                         ] } ],

}; 

BCS_VENTANA.RechazarNotificacion = {
	    TITLE : "Rechazar Notificacion",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "auto",
	    VIEW   : 'EVT/ConsultarNotificaciones/RechazarNotificacion.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: false, files: ['EVT/ConsultarNotificaciones/ConfirmarNotificacion.js'
                                                         ] } ],

}; 

BCS_VENTANA.MantenedorPlantillaDetalle = {
	    TITLE : "Mantenedor Plantilla Ejemplo",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "960px",
	    HEIGHT : "600px",
	    VIEW   : 'EVT/MantenedorPlantilla/MantenedorPlantillaEjemplo.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['EVT/MantenedorPlantilla/MantenedorPlantillaEjemplo.js','EVT/MantenedorEventoGrid.js'] } ],

};

BCS_VENTANA.MantenedorComentenos = {
	    TITLE : "Comentenos (Dummy)",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "300px",
	    VIEW   : 'EVT/Comentenos/Comentenos.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['EVT/Comentenos/Comentenos.js','EVT/MantenedorEventoGrid.js'] } ],

};



BCS_VENTANA.MisNotificaciones = {   
	    TITLE : "Mis Notificaciones",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "650px",
	    HEIGHT : "400px",
	    VIEW   : 'EVT/ConsultarNotificaciones/ConsultarNotificacionesPopUp.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['EVT/ConsultarNotificaciones/ConsultarNotificacionesPopUp.js','EVT/MantenedorEventoGrid.js'] } ],

};


BCS_VENTANA.MantenedorNotificaciones = {
	    TITLE : "Consultar Notificaciones",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "960px",
	    HEIGHT : "auto",
	    VIEW   : 'EVT/ConsultarNotificaciones/ConsultarNotificaciones.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['EVT/ConsultarNotificaciones/ConsultarNotificaciones.js','EVT/MantenedorEventoGrid.js'] } ],

};


BCS_VENTANA.ConfigurarEventos = {
	    TITLE : "Agregar Notificacion",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "640px",
	    HEIGHT : "auto",
	    VIEW   : 'EVT/MantenedorEvento/ConfigurarEventos.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['EVT/MantenedorEvento/ConfigurarEventos.js','EVT/MantenedorEventoGrid.js'] } ],

};
 
BCS_VENTANA.ActualizarConfEventos = {
	    TITLE : "Actualizar Notificacion",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "640px",
	    HEIGHT : "auto",
	    VIEW   : 'EVT/MantenedorEvento/ActualizarEventos.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['EVT/MantenedorEvento/ActualizarEventos.js','EVT/MantenedorEventoGrid.js'] } ],

};

BCS_VENTANA.Confirmacion = {
	    TITLE : "Confirmacion",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "200px",
	    HEIGHT : "150px",
	    VIEW   : 'EVT/ConsultarNotificaciones/Confirmacion.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['EVT/ConsultarNotificaciones/Confirmacion.js','EVT/MantenedorEventoGrid.js'] } ],

};

BCS_VENTANA.ConfirmacionRechazo = {
	    TITLE : "Rechazo",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "200px",
	    HEIGHT : "150px",
	    VIEW   : 'EVT/ConsultarNotificaciones/ConfirmacionRechazo.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['EVT/ConsultarNotificaciones/Confirmacion.js','EVT/MantenedorEventoGrid.js'] } ],

};



//------------------- PLATAFORMA / TASAS ---------------------
BCS_VENTANA.PlataformaSeparadorTasas= {
		SEPARATE: true,
	    TITLE : "UI_TASAS",
	    ENABLE : true
};



BCS_VENTANA.SolicitudTasas = {
	    TITLE : "UI_SOLICITUD_DE_TASAS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "250px",
	    VIEW   : '../../Plataforma/UI/TAS/SolicitudTasas.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Plataforma/UI/TAS/SolicitudTasas.js'] }],

};



BCS_VENTANA.CargaMantenedorTasas = {
	    TITLE : "UI_CARGA_Y_MANTENEDOR_DE_TASAS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "700px",
	    VIEW   : '../../Plataforma/UI/TAS/CargaMantenedorTasas.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Plataforma/UI/TAS/CargaMantenedorTasas.js'] }],

};



BCS_VENTANA.ModalCargaTasas = {
    TITLE : "UI_AGREGAR_MODIFICAR_TASA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "450px",
    HEIGHT : "auto",
    VIEW   : '../../Plataforma/UI/TAS/ModalCargaTasas.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Plataforma/UI/TAS/ModalCargaTasas.js' ] } ],

};


BCS_VENTANA.ModalCargarArchivoTasas = {
    TITLE : "UI_CARGAR_ARCHIVO_TASAS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "400px",
    HEIGHT : "auto",
    VIEW   : '../../Plataforma/UI/TAS/ModalCargarArchivoTasas.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Plataforma/UI/TAS/ModalCargarArchivoTasas.js' ] } ],

};


BCS_VENTANA.CriterioSeleccionTasas = {
	    TITLE : "UI_CRITERIO_DE_SELECCION_DE_TASAS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1200px",
	    HEIGHT : "700px",
	    VIEW   : '../../Plataforma/UI/TAS/CriterioSeleccionTasas.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Plataforma/UI/TAS/CriterioSeleccionTasas.js'] }],

};



BCS_VENTANA.ModalCriterioSeleccionTasas = {
    TITLE : "UI_AGREGAR_MODIFICAR_CRITERIO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "450px",
    HEIGHT : "350px",
    VIEW   : '../../Plataforma/UI/TAS/ModalCriterioSeleccionTasas.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Plataforma/UI/TAS/ModalCriterioSeleccionTasas.js' ] } ],

};


BCS_VENTANA.MargenesTasas = {
	    TITLE : "UI_MANTENEDOR_MARGENES_TASAS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "700px",
	    VIEW   : '../../Plataforma/UI/TAS/MargenesTasas/AdministrarMargenesTasas.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Plataforma/UI/TAS/MargenesTasas/AdministrarMargenesTasas.js',
	                                                        '../../Plataforma/UI/TAS/MargenesTasas/PanelMargenesTasasAgente.js',
	                                                        '../../Plataforma/UI/TAS/MargenesTasas/PanelMargenesTasasCliente.js',
	                                                        '../../Plataforma/UI/TAS/MargenesTasas/PanelMargenesTasasCustodia.js'] }],

};



BCS_VENTANA.ModalMargenesTasas = {
    TITLE : "UI_AGREGAR_MODIFICAR_MARGEN_TASA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "450px",
    HEIGHT : "350px",
    VIEW   : '../../Plataforma/UI/TAS/MargenesTasas/ModalMargenesTasas.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Plataforma/UI/TAS/MargenesTasas/ModalMargenesTasas.js' ] } ],

};



// ------------------- PLATAFORMA / OTROS ---------------------
BCS_VENTANA.PlataformaSeparadorOtros = {
		SEPARATE: true,
	    TITLE : "UI_OTROS",
	    ENABLE : true
};



BCS_VENTANA.AjusteHorario = {
    TITLE : "UI_AJUSTE_HORARIO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "700px", 
    HEIGHT : "150px",
    VIEW   : 'PLA/AjusteHorario/AjusteHorario.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['PLA/AjusteHorario/AjusteHorario.js'] } ],

};

/*	
BCS_VENTANA.ConsultarParidades = {
    TITLE : " UI_PARIDADES_Y_TIPO_CAMBIO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "800px", 
    HEIGHT : "500px",
    VIEW   : 'MER/ConsultarParidades/ConsultarParidades.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['MER/ConsultarParidades/ConsultarParidades.js'] } ],

};
*/

BCS_VENTANA.CrearParidadTipoCambio = {
    TITLE : "UI_CREAR_REGISTRO_PARIDAD_Y_TIPO_DE_CAMBIO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "500px", 
    HEIGHT : "200px",
    VIEW   : 'MER/CrearParidadTipoCambio/CrearParidadTipoCambio.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['MER/CrearParidadTipoCambio/CrearParidadTipoCambio.js'] } ],

};

BCS_VENTANA.MantenedorGeneral = {
	    TITLE : "Mantenedor General",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "400px",
	    VIEW   : 'PLA/MantenedorGeneral/MantenedorGeneral.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['PLA/MantenedorGeneral/MantenedorGeneral.js'] } ],

};

BCS_VENTANA.ModificarAtributoMantenedorGeneral = {
	    TITLE : "UI_MANTENEDOR_GENERAL",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "500px",
	    HEIGHT : "500px",
	    VIEW   : 'PLA/MantenedorGeneral/ModificarAtributoMantenedorGeneral.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['PLA/MantenedorGeneral/ModificarAtributoMantenedorGeneral.js'] } ],

};
BCS_VENTANA.AgregarAtributoMantenedorGeneral = {
	    TITLE : "UI_MANTENEDOR_GENERAL",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "520px",
	    HEIGHT : "500px",
	    VIEW   : 'PLA/MantenedorGeneral/AgregarAtributoMantenedorGeneral.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['PLA/MantenedorGeneral/AgregarAtributoMantenedorGeneral.js'] } ],

};




BCS_VENTANA.AdministrarBoveda = {
    TITLE : "UI_BOVEDAS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "400px",
    VIEW   : 'PLA/Boveda/AdministrarBoveda.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['PLA/Boveda/AdministrarBoveda.js'] } ],

};


BCS_VENTANA.ModalAdministrarBoveda = {
    TITLE : "UI_BOVEDA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "480px",
    HEIGHT : "240px",
    VIEW   : 'PLA/Boveda/ModalAdministrarBoveda.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['PLA/Boveda/ModalAdministrarBoveda.js'] } ],

};
	
BCS_VENTANA.MantenedorBrokerExtranjero = {
    TITLE : "UI_BROKERS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "400px",
    VIEW   : 'PLA/MantenedorBrokerExtranjero/MantenedorBrokerExtranjero.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['PLA/MantenedorBrokerExtranjero/MantenedorBrokerExtranjero.js'] } ],

};


BCS_VENTANA.ModalBrokerExtranjero = {
    TITLE : "UI_BROKER",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "480px",
    HEIGHT : "240px",
    VIEW   : 'PLA/MantenedorBrokerExtranjero/ModalBrokerExtranjero.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['PLA/MantenedorBrokerExtranjero/ModalBrokerExtranjero.js'] } ],

};
	
BCS_VENTANA.MantenedorBancoCustodio = {
    TITLE : "UI_BANCOS_CUSTODIO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "400px",
    VIEW   : 'PLA/MantenedorBancoCustodio/MantenedorBancoCustodio.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['PLA/MantenedorBancoCustodio/MantenedorBancoCustodio.js'] } ],

};


BCS_VENTANA.ModalBancoCustodio = {
    TITLE : "UI_BANCO_CUSTODIO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "480px",
    HEIGHT : "240px",
    VIEW   : 'PLA/MantenedorBancoCustodio/ModalBancoCustodio.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['PLA/MantenedorBancoCustodio/ModalBancoCustodio.js'] } ],

};

BCS_VENTANA.AdministrarTipoOrigenFondo = {
    TITLE : "UI_TIPO_ORIGEN_FONDO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "920px",
    HEIGHT : "520px",
    VIEW   : '../../Plataforma/UI/COM/TipoOrigenFondo/AdministrarTipoOrigenFondo.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['COM/TipoOrigenFondo/AdministrarTipoOrigenFondo.js' ] } ],

};


BCS_VENTANA.ModalTipoOrigenFondo = {
	    TITLE : "UI_ADMINISTRAR_TIPO_ORIGEN_FONDO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "220px",
	    VIEW   : '../../Plataforma/UI/COM/TipoOrigenFondo/ModalTipoOrigenFondo.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['COM/TipoOrigenFondo/ModalTipoOrigenFondo.js' ] } ],

};

	
BCS_VENTANA.RelacionInstrumento = {
    TITLE : "UI_RELACION_INSTRUMENTO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "400px",
    VIEW   : 'PLA/RelacionInstrumento/RelacionInstrumento.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['PLA/RelacionInstrumento/RelacionInstrumento.js'] } ],

};


BCS_VENTANA.ModalRelacionInstrumento = {
    TITLE : "UI_RELACION_INSTRUMENTO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "480px",
    HEIGHT : "270px",
    VIEW   : 'PLA/RelacionInstrumento/ModalRelacionInstrumento.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['PLA/RelacionInstrumento/ModalRelacionInstrumento.js'] } ],

};

BCS_VENTANA.MantenedorConceptosManuales = {
	    TITLE : "UI_MANTENEDOR_CONCEPTOS_MANUALES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "500px",
	    VIEW   : 'PLA/MantenedorConceptosManuales/MantenedorConceptosManuales.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['PLA/MantenedorConceptosManuales/MantenedorConceptosManuales.js',
	                                                        'PLA/MantenedorConceptosManuales/AgregarModificarConceptosManuales.js'
	                                                       ] } ],
};
BCS_VENTANA.AgregarModificarConceptosManuales = {
	    TITLE : "UI_AGREGAR_MODIFICAR_CONCEPTOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "500px",
	    VIEW   : 'PLA/MantenedorConceptosManuales/AgregarModificarConceptosManuales.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['PLA/MantenedorConceptosManuales/AgregarModificarConceptosManuales.js'] } ],
};


BCS_VENTANA.MantenedorArchivosContratos = {
	    TITLE : "UI_MANTENEDOR_ARCHIVOS_CONTRATOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "472px",
	    VIEW   : 'CTT/MantenedorArchivosContratos.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['CTT/MantenedorArchivosContratos.js'] } ],

};


BCS_VENTANA.MantenedorDatosContratos = {
	    TITLE : "UI_MANTENEDOR_DATOS_CONTRATOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "310px",
	    VIEW   : 'CTT/MantenedorDatosContratos.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['CTT/MantenedorDatosContratos.js'] } ],

};


BCS_VENTANA.ModalRecursosContratos = {
	    TITLE : "UI_VISUALIZAR_RECURSO_CONTRATO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "400px",
	    VIEW   : 'CTT/ModalRecursosContratos.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['CTT/ModalRecursosContratos.js'] } ],

};
BCS_VENTANA.SubirArchivosContratos = {
	    TITLE : "UI_SUBIR_ARCHIVOS_CONTRATO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "450px",
	    HEIGHT : "235px",
	    VIEW   : 'CTT/subirContratoArchivo.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['CTT/subirContratoArchivo.js'] } ],

};

BCS_VENTANA.ModificarArchivosContratos = {
	    TITLE : "UI_MODIFICAR_ARCHIVOS_CONTRATO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "450px",
	    HEIGHT : "247px",
	    VIEW   : 'CTT/ModificarArchivosContratos.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['CTT/ModificarArchivosContratos.js'] } ],

};

BCS_VENTANA.MantenedorImagenes = {
	    TITLE : "UI_MANTENEDOR_IMAGENES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "575px",
	    HEIGHT : "425px",
	    VIEW   : 'PLA/MantenedorImagenes/ConsultarImagenes.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['PLA/MantenedorImagenes/ConsultarImagenes.js'] } ],

};


BCS_VENTANA.OperarImagenModal = {
	    TITLE : "UI_XXX_IMAGEN_MODAL",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "310px",
	    HEIGHT : "180px",
	    VIEW   : 'PLA/MantenedorImagenes/OperarImagenModal.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['PLA/MantenedorImagenes/OperarImagenModal.js'] } ],

};
BCS_VENTANA.OperarFirmaModal = {
	    TITLE : "UI_CARGARFIRMA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "500px",
	    HEIGHT : "300px",
	    VIEW   : 'PLA/MantenedorImagenes/OperarFirmasModal.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['PLA/MantenedorImagenes/OperarFirmasModal.js'] } ],

};


BCS_VENTANA.MantenedorTemplates = {
	    TITLE : "UI_MANTENEDOR_TEMPLATES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "575px",
	    HEIGHT : "425px",
	    VIEW   : 'PLA/MantenedorTemplates/ConsultarTemplates.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['PLA/MantenedorTemplates/ConsultarTemplates.js'] } ],

};


BCS_VENTANA.OperarTemplateModal = {
	    TITLE : "UI_XXX_TEMPLATE_MODAL",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "300px",
	    HEIGHT : "180px",
	    VIEW   : 'PLA/MantenedorTemplates/OperarTemplateModal.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['PLA/MantenedorTemplates/OperarTemplateModal.js'] } ],

};

BCS_VENTANA.AdmDiccionario = {
    TITLE : "Diccionario",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "600px",
    VIEW   : 'PLA/AdmDiccionario/AdmDiccionario.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ 'PLA/AdmDiccionario/AdmDiccionario.js'] } ],

};

BCS_VENTANA.EditarDiccionario = {
	    TITLE : "UI_EDITAR_DICCIONARIO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "180px",
	    VIEW   : 'PLA/EditarDiccionario/EditarDiccionario.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ 'PLA/EditarDiccionario/EditarDiccionario.js'] } ],

};

BCS_VENTANA.MantenedorParametros = {
	    TITLE : "Plataforma: Mantenedor de Parametros",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "541px",
	    VIEW   : 'PLA/MantenedorParametros/MantenedorParametros.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ 'PLA/MantenedorParametros/MantenedorParametros.js'] } ],

};

BCS_VENTANA.MantenedorParametrosEditar= {
	    TITLE : "Editar Parametro",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "500px",
	    HEIGHT : "100px",
	    VIEW   : 'PLA/MantenedorParametros/MantenedorParametrosEditar.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ 'PLA/MantenedorParametros/MantenedorParametrosEditar.js'] } ],

};

BCS_VENTANA.AjusteFechaSistema = {
	    TITLE : "UI_AJUSTE_FECHA_SISTEMA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "300px",
	    HEIGHT : "150px",
	    VIEW   : 'PLA/AjusteFechaSistema/AjusteFechaSistema.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ 'PLA/AjusteFechaSistema/AjusteFechaSistema.js'] } ],
	    
};

BCS_VENTANA.CargaArchivosGeneral = {
	    TITLE : "UI_CARGAR_ARCHIVO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1010px",
	    HEIGHT : "340px",
	    VIEW   : 'PLA/CargaArchivosGeneral/cargarArchivo.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ 'PLA/CargaArchivosGeneral/cargarArchivo.js'] } ],
	    AFECTOCIERRE : true,
		MODULO : "CUS",

};


BCS_VENTANA.ConsultarAsuntosPendientes = {
	    TITLE : "UI_CONSULTAR_ASUNTOS_PENDIENTES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "480px",
	    VIEW   : 'PND/ConsultarAsuntosPendientes.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['PND/ConsultarAsuntosPendientes.js'] } ],

};


BCS_VENTANA.MantenedorDiasFeriados = {
    TITLE : "UI_MANTENEDOR_DIAS_FERIADOS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "700px",
    VIEW   : 'PLA/MantenedorDiasFeriados/MantenedorDiasFeriados.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ 'PLA/MantenedorDiasFeriados/MantenedorDiasFeriados.js'
                                                         ] } ],
};


BCS_VENTANA.ModalDiasFeriados = {
    TITLE : "UI_MODAL_DIAS_FERIADOS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "450px",
    HEIGHT : "280px",
    VIEW   : 'PLA/MantenedorDiasFeriados/ModalDiasFeriados.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ 'PLA/MantenedorDiasFeriados/ModalDiasFeriados.js' ] } ],

};



BCS_VENTANA.PreferenciaCalceLibroInventario = {
    TITLE : "UI_PREFERENCIA_CALCE_LIBRO_INVENTARIO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "600px",
    HEIGHT : "500px",
    VIEW   : 'PLA/TipoCriterioNeteo/PreferenciaCalceLibroInventario.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ 'PLA/TipoCriterioNeteo/PreferenciaCalceLibroInventario.js'
                                                         ] } ],
};

BCS_VENTANA.TipoCriterioNeteo = {
    TITLE : "UI_TIPOS_CRITERIO_NETEO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "600px",
    HEIGHT : "500px",
    VIEW   : 'PLA/TipoCriterioNeteo/TipoCriterioNeteo.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ 'PLA/TipoCriterioNeteo/TipoCriterioNeteo.js'
                                                         ] } ],
};


BCS_VENTANA.ModalTipoCriterioNeteo = {
    TITLE : "UI_TIPO_CRITERIO_NETEO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "300px",
    HEIGHT : "150px",
    VIEW   : 'PLA/TipoCriterioNeteo/ModalTipoCriterioNeteo.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ 'PLA/TipoCriterioNeteo/ModalTipoCriterioNeteo.js'
                                                         ] } ],
};





//------------------- PLATAFORMA / MERCADO ---------------------
//SE LLEVA A MODULO SPOT
/*
BCS_VENTANA.PlataformaSeparadorMercado = {
		SEPARATE: true,
	    TITLE : "UI_MERCADO",
	    ENABLE : true
};


BCS_VENTANA.ParidadYTipoCambio = {
	    TITLE : "UI_PARIDAD_Y_TIPOCAMBIO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "980px",
	    HEIGHT : "600px",
	    VIEW   : 'MER/ParidadesYTipoCambio/ParidadesYTipoCambio.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['MER/ParidadesYTipoCambio/ParidadesYTipoCambio.js'] } ],

};

*/

BCS_VENTANA.DashboardPowerBI = {
	    TITLE : "UI_DASHBOARD_POWER_BI",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1338px",
	    HEIGHT : "840px",
	    VIEW   : 'PLA/DashboardPowerBI/Dashboard.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ 'PLA/DashboardPowerBI/Dashboard.js'] } ],
	};

	BCS_VENTANA.MantenedorDeterioro = {
		TITLE : "UI_MANTENEDOR_DETERIORO_EMISOR",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "800px",
		HEIGHT : "450px",
		VIEW   : '../../Plataforma/UI/MER/MantenedorDeterioro/MantenedorDeterioro.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Plataforma/UI/MER/MantenedorDeterioro/MantenedorDeterioro.js' ] } ],
		
};
BCS_VENTANA.AgregarModificarDeterioro = {
		TITLE : "UI_EMISOR_FLAGS",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "450px",
		HEIGHT : "250px",
		VIEW   : '../../Plataforma/UI/MER/MantenedorDeterioro/AgregarModificarDeterioro.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Plataforma/UI/MER/MantenedorDeterioro/AgregarModificarDeterioro.js'] } ],

};

BCS_VENTANA.CargarDeterioro = {
		TITLE : "UI_CARGAR_DETERIORO_EMISOR",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "1000px",
		HEIGHT : "700px",
		VIEW   : '../../Plataforma/UI/MER/MantenedorDeterioro/CargarDeterioro.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Plataforma/UI/MER/MantenedorDeterioro/CargarDeterioro.js'] } ],

};


//---------------------------------------------
//CONTABILIDAD
//---------------------------------------------
BCS_VENTANA.InformeControlFacturacionOperaciones= {
	TITLE : "UI_INFORME_CONTROL_FACTURACION_OPERACIONES",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "400px",
	HEIGHT : "auto",
	VIEW   : '../../Contabilidad/UI/Informes/InformeControlFacturacionOperaciones.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Contabilidad/UI/Informes/InformeControlFacturacionOperaciones.js'] } ],
};
BCS_VENTANA.MODULO_CONTABILIDAD= {
	    TITLE : "UI_INTERFAZ_CONTABLE",
	    IMAGE  : "sapUiIcon course-book",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "400px",
	    VIEW   : '',
	    PARAMS : {},
	    DEPENDENCIES : [],

};

BCS_VENTANA.InterfazContable = {
	    TITLE : "UI_GESTION_INTERFAZ_CONTABLE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1200px",
	    HEIGHT : "730px",
	    VIEW   : '../../Contabilidad/UI/InterfazContable/InterfazContable.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Contabilidad/UI/InterfazContable/InterfazContable.js',
	                                                         '../../Contabilidad/UI/MantenedorEventoGrid.js'
	                                                         ] } ],
};

BCS_VENTANA.ConsultarCentralizacionModal = {
	    TITLE : "UI_DETALLE_CENTRALIZACION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "874px",
	    HEIGHT : "575px",
	    VIEW   : '../../Contabilidad/UI/InterfazContable/ConsultarCentralizacionModal.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Contabilidad/UI/InterfazContable/ConsultarCentralizacionModal.js'] } ],
	};
	BCS_VENTANA.ProcesarFechaModal = {
	    TITLE : "UI_FECHA_CONSULTA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "350px",
	    HEIGHT : "150px",
	    VIEW   : '../../Contabilidad/UI/InterfazContable/ProcesarFechaModal.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Contabilidad/UI/InterfazContable/ProcesarFechaModal.js'] } ],
	};

BCS_VENTANA.ConsultarPreComprobanteModal = {
	    TITLE : "UI_DETALLE_PRECOMPROBANTE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "874px",
	    HEIGHT : "582px",
	    VIEW   : '../../Contabilidad/UI/InterfazContable/ConsultarPreComprobanteModal.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Contabilidad/UI/InterfazContable/ConsultarPreComprobanteModal.js'] } ],
	};

BCS_VENTANA.ConsultarPreComprobanteEnvioContabilidad = {
	    TITLE : "UI_PREVISUALIZAR_PRECOMPROBANTE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "874px",
	    HEIGHT : "595px",
	    VIEW   : '../../Contabilidad/UI/InterfazContable/ConsultarPreComprobanteEnvioContabilidad.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Contabilidad/UI/InterfazContable/ConsultarPreComprobanteEnvioContabilidad.js'] } ],
	};


BCS_VENTANA.ConfiguracionComprobantes = {
	    TITLE : "UI_CONFIGURACION_COMPROBANTES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1260px",
	    HEIGHT : "750px",
	    VIEW   : '../../Contabilidad/UI/ConfiguracionComprobantes/ConfiguracionComprobantes.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Contabilidad/UI/ConfiguracionComprobantes/ConfiguracionComprobantes.js',
	                                                         '../../Contabilidad/UI/MantenedorEventoGrid.js'
	                                                         ] } ],
};

BCS_VENTANA.ConfiguracionTipoContabilidad = {
	    TITLE : "UI_CONFIGURA_TIPO_CONTABILIDAD",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "210px",
	    VIEW   : '../../Contabilidad/UI/ConfiguracionComprobantes/ConfiguracionTipoContabilidad.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Contabilidad/UI/ConfiguracionComprobantes/ConfiguracionTipoContabilidad.js']}],
};

BCS_VENTANA.AdministrarClaseEmisor = {
	    TITLE : "UI_CLASE_EMISOR",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "400px",
	    VIEW   : '../../Contabilidad/UI/ClaseEmisor/AdministrarClaseEmisor.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Contabilidad/UI/ClaseEmisor/AdministrarClaseEmisor.js']}],
};

BCS_VENTANA.ModalAdministrarClaseEmisor = {
	    TITLE : "UI_CLASE_EMISOR",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "450px",
	    HEIGHT : "369px",
	    VIEW   : '../../Contabilidad/UI/ClaseEmisor/ModalAdministrarClaseEmisor.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Contabilidad/UI/ClaseEmisor/ModalAdministrarClaseEmisor.js'] } ],

	};

BCS_VENTANA.AdministrarCentroCosto = {
	    TITLE : "UI_CENTRO_COSTO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "400px",
	    VIEW   : '../../Contabilidad/UI/CentroCosto/AdministrarCentroCosto.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Contabilidad/UI/CentroCosto/AdministrarCentroCosto.js']}],
};

BCS_VENTANA.ModalAdministrarCentroCosto = {
	    TITLE : "UI_CENTRO_COSTO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "480px",
	    HEIGHT : "285px",
	    VIEW   : '../../Contabilidad/UI/CentroCosto/ModalAdministrarCentroCosto.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Contabilidad/UI/CentroCosto/ModalAdministrarCentroCosto.js'] } ],

	};
		

BCS_VENTANA.CentralizacionesRequeridas = {
	    TITLE : "UI_CENTRALIZACIONES_REQUERIDAS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "951px",
	    HEIGHT : "500px",
	    VIEW   : '../../Contabilidad/UI/ConfiguracionComprobantes/CentralizacionesRequeridas.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Contabilidad/UI/ConfiguracionComprobantes/CentralizacionesRequeridas.js']}],
};

BCS_VENTANA.ConfiguracionGlosa = {
	    TITLE : "UI_CONFIGURACION_GLOSA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "963px",
	    HEIGHT : "489px",
	    VIEW   : '../../Contabilidad/UI/ConfiguracionComprobantes/ConfiguracionGlosa.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Contabilidad/UI/ConfiguracionComprobantes/ConfiguracionGlosa.js']}],
};

BCS_VENTANA.EventosAsiento = {
	    TITLE : "UI_EVENTOS_ASIENTO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "500px",
	    VIEW   : '../../Contabilidad/UI/ConfiguracionComprobantes/EventosAsiento.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Contabilidad/UI/ConfiguracionComprobantes/EventosAsiento.js']}],
};

BCS_VENTANA.MantenedorAsiento = {
	    TITLE : "UI_MANTENEDOR_ASIENTO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "225px",
	    VIEW   : '../../Contabilidad/UI/ConfiguracionComprobantes/MantenedorAsiento.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Contabilidad/UI/ConfiguracionComprobantes/MantenedorAsiento.js']}],
};

BCS_VENTANA.CuadraturaContabilidad = {
		TITLE : "UI_CUADRATURA_CONTABILIDAD",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "147px",
	    VIEW   : '../../Contabilidad/UI/CuadraturaContabilidad/CuadraturaContabilidad.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: [ '../../Contabilidad/UI/CuadraturaContabilidad/CuadraturaContabilidad.js']}],
};

BCS_VENTANA.InformeOperacionesPactosConDetalle = {
	TITLE : "UI_INFORME_PACTO_CON_DETALLE",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "450px",
	HEIGHT : "auto",
	VIEW   : '../../Contabilidad/UI/Informes/InformeOperacionesPactosConDetalle.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Contabilidad/UI/Informes/InformeOperacionesPactosConDetalle.js'] } ],
};

 



//---------------------------------------------
//DESARROLLO
//---------------------------------------------
BCS_VENTANA.MODULO_DESARROLLO= {
    TITLE : "Desarrollo",
    IMAGE  : "sapUiIcon wrench",
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "400px",
    VIEW   : '',
    PARAMS : {},
    DEPENDENCIES : [],

};


BCS_VENTANA.MonitorTools = {
	    TITLE : "MONITOR",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "100px",
	    VIEW   : 'views/elementsDemo/Monitor.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ 'views/elementsDemo/Monitor.js'] } ],

};

	
	
BCS_VENTANA.DebugTools = {
    TITLE : "DEBUG",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "600px",
    HEIGHT : "600px",
    VIEW   : 'PLA/DebugTools/DebugTools.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ 'PLA/DebugTools/DebugTools.js'] } ],

};


BCS_VENTANA.UIPlayground = {
	    TITLE : "Playground",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "600px",
	    HEIGHT : "600px",
	    VIEW   : 'PLA/UIPlayground/UIPlayground.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ 'PLA/UIPlayground/UIPlayground.js'] } ],

};


BCS_VENTANA.ConsultarServicios = {
    TITLE : "UI_CONSULTAR_SERVICIOS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "600px",
    HEIGHT : "330px",
    VIEW   : 'PLA/ConsultarServicios/ConsultarServicios.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ 'PLA/ConsultarServicios/ConsultarServicios.js'] } ],

};


/*Se movió a desarrollo porque no tiene título!*/
BCS_VENTANA.ResumenOperacionesMasivas = {
    TITLE : "NO_TITLE",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "600px",
    HEIGHT : "330px",
    VIEW   : 'PLA/ResumenOperacionesMasivas/ResumenOperacionesMasivas.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [ 'PLA/ResumenOperacionesMasivas/ResumenOperacionesMasivas.js'] } ],

};


BCS_VENTANA.gFormulario = {
    TITLE : "gFormulario",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "560px",
    HEIGHT : "440px",
    VIEW   : 'views/elementsDemo/gFormulario.html',
    PARAMS : {  parametro1: 1, parametro2: 2},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['views/elementsDemo/gFormulario.js'] }],

};


BCS_VENTANA.gFormularioDetalle = {
    TITLE : "gFormularioDetalle",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "550px",
    HEIGHT : "125px",
    VIEW   : 'views/elementsDemo/gFormularioDetalle.html',
    PARAMS : { IdDetalle : 0 },
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['views/elementsDemo/gFormularioDetalle.js'] }],

};


BCS_VENTANA.gBlotter = {
    TITLE : "gBlotter", 
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "600px",
    VIEW   : 'views/elementsDemo/gBlotter.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['views/elementsDemo/gBlotter.js','views/elementsDemo/gBlotter1.js','views/elementsDemo/gBlotter2.js','views/elementsDemo/gBlotter3.js'
                                                        ,'views/elementsDemo/gBlotter4.js'] }], 

};


BCS_VENTANA.PocBlotterOrdenes = {
	    TITLE : "POC Blotter",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
	    HEIGHT : "700px",
	    VIEW   : '/POC/ARQ/BI/UI/BlotterOrdenes/BlotterOrdenes.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '/POC/ARQ/BI/UI/BlotterOrdenes/BlotterOrdenes.js'
	                                                         ,'/POC/ARQ/BI/UI/ConsultarOrdenes/ConsultarOrdenes.js'
	                                                         ,'/POC/ARQ/BI/UI/ConsultarTransacciones/ConsultarTransacciones.js'
	                                                         ,'/POC/ARQ/BI/UI/barraFiltro/barraFiltros.js'
	                                                         ,'/POC/ARQ/BI/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

};


BCS_VENTANA.GeneraRut = {
	    TITLE : "Genera Rut",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "400px",
	    VIEW   : '../../Plataforma/UI/views/GeneraRut/GeneraRut.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Plataforma/UI/views/GeneraRut/GeneraRut.js'] } ],

};




BCS_VENTANA.PocG = {
	    TITLE : "POC G",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
	    HEIGHT : "700px",
	    VIEW   : '../../Plataforma/UI/views/TEST/Test.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Plataforma/UI/views/TEST/Test.js'] } ],

};




BCS_VENTANA.MODULO_HOMOLOGACION= {
    TITLE : "Homologacion",
    IMAGE  : "sapUiIcon compare-2",
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "400px",
    VIEW   : '',
    PARAMS : {},
    DEPENDENCIES : [],

};


BCS_VENTANA.MantenedorCargasMasivas = {
    TITLE : "UI_MANTENEDOR_CARGAS_MASIVAS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "960px",
    HEIGHT : "690px",
    VIEW   : '../../Homologacion/UI/MantenedorCargasMasivas/MantenedorCargasMasivas.html',
    PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Homologacion/UI/MantenedorCargasMasivas/MantenedorCargasMasivas.js'] } ],

};

//---------------------------------------------
//APV
//---------------------------------------------

BCS_VENTANA.MODULO_APV = {
	    TITLE : "UI_APV",
	    IMAGE  : "sapUiIcon course-book",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "400px",
	    VIEW   : '',
	    PARAMS : {},
	    DEPENDENCIES : [],

};

BCS_VENTANA.AfiliacionMasiva = {
	    TITLE : "UI_AFILIACION_MASIVA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "785px",
	    VIEW   : '../../APV/UI/Afiliacion/AfiliacionMasiva/AfiliacionMasiva.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../APV/UI/Afiliacion/AfiliacionMasiva/AfiliacionMasiva.js'] } ],

};

BCS_VENTANA.GestionSAAPV = {
	    TITLE : "UI_GESTION_SAAPV",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1200px",
	    HEIGHT : "625px",
	    VIEW   : '../../APV/UI/Afiliacion/GestionSAAPV/GestionSAAPV.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../APV/UI/Afiliacion/GestionSAAPV/GestionSAAPV.js'] } ],

};

BCS_VENTANA.CargaDeInterfaces = {
	    TITLE : "UI_DETALLE_CARGA_INTERFACES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "720px",
	    VIEW   : '../../APV/UI/Recaudacion/CargaDeInterfaces/CargaDeInterfaces.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../APV/UI/Recaudacion/CargaDeInterfaces/CargaDeInterfaces.js'] } ],

};

BCS_VENTANA.CargaDetalleRecaudacion = {
	    TITLE : "UI_DETALLE_CARGA_RECAUDACION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "400px",
	    VIEW   : '../../APV/UI/Recaudacion/CargaRecaudacion/DetalleCargaRecaudacion.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../APV/UI/Recaudacion/CargaRecaudacion/DetalleCargaRecaudacion.js'] } ],

};

BCS_VENTANA.GestionDeInterfaces = {
	    TITLE : "UI_GESTION_INTERFACES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "400px",
	    VIEW   : '../../APV/UI/Recaudacion/GestionDeInterfaces/GestionDeInterfaces.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../APV/UI/Recaudacion/GestionDeInterfaces/GestionDeInterfaces.js'] } ],

};

BCS_VENTANA.GestionDeRecaudacion = {
	    TITLE : "UI_GESTION_DE_RECAUDACION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1200px",
	    HEIGHT : "762px",
	    VIEW   : '../../APV/UI/Recaudacion/GestionDeRecaudacion/GestionDeRecaudacion.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../APV/UI/Recaudacion/GestionDeRecaudacion/GestionDeRecaudacion.js'
															,'../../APV/UI/Recaudacion/GestionDeRecaudacion/PanelDistribucion.js'
															,'../../APV/UI/Recaudacion/GestionDeRecaudacion/PanelRecaudacion.js'] } ],

};

BCS_VENTANA.DevolucionPagoRezagos = {
	    TITLE : "UI_DEVOLUCION_PAGO_REZAGOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "700px",
	    HEIGHT : "350px",
	    VIEW   : '../../APV/UI/Recaudacion/GestionDeRecaudacion/DevolucionPagoRezagos/DevolucionPagoRezagos.html',
	    PARAMS : {IdCliente:0,IdPortafolio:0},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../APV/UI/Recaudacion/GestionDeRecaudacion/DevolucionPagoRezagos/DevolucionPagoRezagos.js' ] } ],
};

BCS_VENTANA.CargarArchivoCartola = {
	    TITLE : "UI_CARGAR_ARCHIVO_PREVIRED",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "367px",
	    VIEW   : '../../APV/UI/Recaudacion/CargarArchivoCartola/CargarArchivoCartola.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../APV/UI/Recaudacion/CargarArchivoCartola/CargarArchivoCartola.js'] } ],

};

BCS_VENTANA.GestionIngresoEgresoAPV = {
	    TITLE : "UI_GESTION_INGRESO_EGRESO_APV",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "737px",
	    VIEW   : '../../APV/UI/Traspasos/GestionIngresoEgreso/GestionIngresoEgresoAPV.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../APV/UI/Traspasos/GestionIngresoEgreso/GestionIngresoEgresoAPV.js'
															,'../../APV/UI/Traspasos/GestionIngresoEgreso/PanelIngresos.js'
															,'../../APV/UI/Traspasos/GestionIngresoEgreso/PanelEgresos.js'] } ],

};

BCS_VENTANA.ReporteNominas = {
	    TITLE : "UI_NOMINAS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "440px",
	    HEIGHT : "190px",
	    VIEW   : '../../APV/UI/Traspasos/ModalReporteNominas/ModalReporteNominas.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../APV/UI/Traspasos/ModalReporteNominas/ModalReporteNominas.js'] } ],

};

BCS_VENTANA.IngresoTraspasoAPV = {
	    TITLE : "UI_INGRESO_DETALLE_TRASPASO_APV",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "677px",
	    VIEW   : '../../APV/UI/Traspasos/IngresoTraspaso/IngresoTraspaso.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../APV/UI/Traspasos/IngresoTraspaso/IngresoTraspaso.js'] } ],

};

BCS_VENTANA.FormaDePagoAPV = {
	    TITLE : "UI_FORMA_PAGO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "250px",
	    VIEW   : '../../APV/UI/Traspasos/ModalFormaDePago/ModalFormaDePago.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../APV/UI/Traspasos/ModalFormaDePago/ModalFormaDePago.js'] } ],

};

BCS_VENTANA.ContratoSAAPV = {
	    TITLE : "UI_CONTRATO_SAAPV",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
	    HEIGHT : "700px",
	    VIEW   : '../../APV/UI/Afiliacion/ContratoSAAPV/ContratoSAAPV.html',
	    PARAMS : { modoVentana : 'I' , CodigoSolicitud : 'AUT' },
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../APV/UI/Afiliacion/ContratoSAAPV/ContratoSAAPV.js',	    
	    													 '../../APV/UI/Afiliacion/ContratoSAAPV/EMP/PanelEmpleadoContrato.js',
	    													 '../../APV/UI/Afiliacion/ContratoSAAPV/POR/PanelPortafolioContrato.js',
	    													 '../../APV/UI/Afiliacion/ContratoSAAPV/TRA/PanelPortafolioContratoTraspaso.js'
	    													] } ],
};

BCS_VENTANA.RegistroTraspasoAPV = {
		TITLE : "UI_REGISTRO_TRASPASO_APV",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
	    HEIGHT : "720px",
	    VIEW   : '../../APV/UI/Afiliacion/ContratoSAAPV/ContratoSAAPV.html',
	    PARAMS : { modoVentana : 'I', CodigoSolicitud : 'TRA' },
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../APV/UI/Afiliacion/ContratoSAAPV/ContratoSAAPV.js',	    
	    													 '../../APV/UI/Afiliacion/ContratoSAAPV/EMP/PanelEmpleadoContrato.js',
	    													 '../../APV/UI/Afiliacion/ContratoSAAPV/POR/PanelPortafolioContrato.js',
	    													 '../../APV/UI/Afiliacion/ContratoSAAPV/TRA/PanelPortafolioContratoTraspaso.js'
	    													] } ],
};

BCS_VENTANA.IngresoRecaudacionDeposito = {
	    TITLE : "UI_INGRESO_DEPOSITO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "600px",//900
	    HEIGHT : "344px",
	    VIEW   : '../../APV/UI/Recaudacion/IngresoDeposito/IngresoDeposito.html',
	    PARAMS : { modoVentana : 'I' },
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../APV/UI/Recaudacion/IngresoDeposito/IngresoDeposito.js'] } ],
};

BCS_VENTANA.IngresoRecaudacionDetalle = {
	    TITLE : "UI_INGRESO_DETALLE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",//900
	    HEIGHT : "732px", //862
	    VIEW   : '../../APV/UI/Recaudacion/IngresoDetalle/IngresoDetalle.html',
	    PARAMS : { modoVentana : 'I' },
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../APV/UI/Recaudacion/IngresoDetalle/IngresoDetalle.js'] } ],
};

BCS_VENTANA.AsignarDeposito = {
	    TITLE : "UI_ASIGNAR_DEPOSITO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",//900
	    HEIGHT : "554px",
	    VIEW   : '../../APV/UI/Recaudacion/AsignarDeposito/AsignarDeposito.html',
	    PARAMS : { modoVentana : 'I' },
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../APV/UI/Recaudacion/AsignarDeposito/AsignarDeposito.js'] } ],
};

BCS_VENTANA.DistribuirInversionSAAPV = {
	    TITLE : "UI_DISTRIBUCION_INVERSION_SAAPV",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "600px",//900
	    HEIGHT : "400px",
	    VIEW   : '../../APV/UI/Afiliacion/DistribucionInversion/DistribucionInversionSAAPV.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../APV/UI/Afiliacion/DistribucionInversion/DistribucionInversionSAAPV.js'] } ],
};

BCS_VENTANA.CargaMasivaContratoSAAPV = {
	    TITLE : "UI_CARGA_MASIVA_CONTRATO_SAAPV",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "550px",
	    VIEW   : '../../APV/UI/Afiliacion/CargaMasivaContratosSAAPV/CargaMasivaContratosSAAPV.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../APV/UI/Afiliacion/CargaMasivaContratosSAAPV/CargaMasivaContratosSAAPV.js'] } ],
};


BCS_VENTANA.PanelDistribucionEstados = {
	    TITLE : "UI_DISTRIBUCION_ESTADOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "600px",//900
	    HEIGHT : "300px",
	    VIEW   : '../../APV/UI/Recaudacion/GestionDeRecaudacion/PanelDistribucionEstado.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../APV/UI/Recaudacion/GestionDeRecaudacion/PanelDistribucionEstado.js'] } ],
};

BCS_VENTANA.ConfiguracionRecaudacion = {
	    TITLE : "UI_CONFIGURACION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",//900
	    HEIGHT : "230px",
	    VIEW   : '../../APV/UI/Recaudacion/ConfiguracionRecaudacion/ConfiguracionRecaudacion.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../APV/UI/Recaudacion/ConfiguracionRecaudacion/ConfiguracionRecaudacion.js'] } ],
};

BCS_VENTANA.CargaPrevired= {
	    TITLE : "UI_CARGA_PREVIRED",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "700px",
	    HEIGHT : "371px",
	    VIEW   : '../../APV/UI/Recaudacion/CargaPrevired/CargaPrevired.html',
	    PARAMS : {codigoParametro: 'CARGAPREVIRED', nombreParametro: 'UI_CARGA_PREVIRED'},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../APV/UI/Recaudacion/CargaPrevired/CargaPrevired.js' ]} ],

};

BCS_VENTANA.CargaRecaudacion= {
	    TITLE : "RECAUDACION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "700px",
	    HEIGHT : "371px",
	    VIEW   : '../../APV/UI/Recaudacion/CargaRecaudacion/CargaRecaudacion.html',
	    PARAMS : {codigoParametro: 'CARGARECAUDACION', nombreParametro: 'UI_CARGA_RECAUDACION'},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../APV/UI/Recaudacion/CargaRecaudacion/CargaRecaudacion.js' ]} ],

};

BCS_VENTANA.CargaPAC= {
	    TITLE : "UI_CARGA_PAC",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "700px",
	    HEIGHT : "371px",
	    VIEW   : '../../APV/UI/Recaudacion/CargaPAC/CargaPAC.html',
	    PARAMS : {codigoParametro: 'CARGAPAC', nombreParametro: 'UI_CARGA_PAC'},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../APV/UI/Recaudacion/CargaPAC/CargaPAC.js' ]} ],

};

BCS_VENTANA.CargaSAAPVPAC= {
	    TITLE : "UI_CARGA_SAAPVPAC",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "586px",
	    VIEW   : '../../APV/UI/Recaudacion/CargaSAAPVPAC/CargaSAAPVPAC.html',
	    PARAMS : {codigoParametro: 'CARGASAAPVPAC', nombreParametro: 'UI_CARGA_SAAPVPAC'},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../APV/UI/Recaudacion/CargaSAAPVPAC/CargaSAAPVPAC.js' ]} ],

};

BCS_VENTANA.InversionAPV= {
	    TITLE : "UI_INVERSION_APV",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "720px",
	    VIEW   : '../../APV/UI/InversionDesinversion/Inversion/Inversion.html',
	    PARAMS : {codigoParametro: 'INVERSIONAPV', nombreParametro: 'UI_INVERSION_APV'},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../APV/UI/InversionDesinversion/Inversion/Inversion.js' ]} ],
};

BCS_VENTANA.DesinversionAPV= {
	    TITLE : "UI_DESINVERSION_APV",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "720px",
	    VIEW   : '../../APV/UI/InversionDesinversion/Desinversion/Desinversion.html',
	    PARAMS : {codigoParametro: 'DESINVERSIONAPV', nombreParametro: 'UI_DESINVERSION_APV'},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../APV/UI/InversionDesinversion/Desinversion/Desinversion.js' 
															,'../../Orden/UI/bcsCobros/bcsCobrosOrden.js' ]} ],

};

BCS_VENTANA.GestionBonificacionAPV= {
	    TITLE : "UI_GESTION_BONIFICACION_APV",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "650px",
	    VIEW   : '../../APV/UI/InversionDesinversion/GestionBonificacion/GestionBonificacion.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../APV/UI/InversionDesinversion/GestionBonificacion/GestionBonificacion.js' ]} ],

};

BCS_VENTANA.CargaBonificacionTGR= {
	    TITLE : "UI_BONIFICACION_TGR",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "700px",
	    HEIGHT : "371px",
	    VIEW   : '../../APV/UI/InversionDesinversion/CargaBonificacionTGR/CargaBonificacionTGR.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../APV/UI/InversionDesinversion/CargaBonificacionTGR/CargaBonificacionTGR.js' ]} ],

};

BCS_VENTANA.CargaRegistroHistoricoTraspaso= {
	    TITLE : "UI_CARGA_REGISTRO_HISTORICO_TRASPASO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "700px",
	    HEIGHT : "371px",
	    VIEW   : '../../APV/UI/Traspasos/CargaRegistroHistoricoTraspaso/CargaRegistroHistoricoTraspaso.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../APV/UI/Traspasos/CargaRegistroHistoricoTraspaso/CargaRegistroHistoricoTraspaso.js' ]} ],

};

BCS_VENTANA.GestionDeRezagos = {
	    TITLE : "UI_GESTION_REZAGOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1280px",
	    HEIGHT : "720px",
	    VIEW   : '../../APV/UI/Recaudacion/GestionDeRezagos/GestionDeRezagos.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../APV/UI/Recaudacion/GestionDeRezagos/GestionDeRezagos.js'] } ],

};

BCS_VENTANA.VerDoctosDistribucion = {
	    TITLE : "UI_VER_DOCUMENTOS_DISTRIBUCION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
	    HEIGHT : "327px",
	    VIEW   : '../../APV/UI/Recaudacion/GestionDeRecaudacion/VerDoctosDistribucion.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../APV/UI/Recaudacion/GestionDeRecaudacion/VerDoctosDistribucion.js'] } ],
};

BCS_VENTANA.AsignacionPortafolioAPV = {
	    TITLE : "UI_ASIGNACION_CONTRATO_SAAPV",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "178px",
	    VIEW   : '../../APV/UI/Recaudacion/GestionDeRecaudacion/AsignacionPortafolioAPV/AsignacionPortafolioAPV.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../APV/UI/Recaudacion/GestionDeRecaudacion/AsignacionPortafolioAPV/AsignacionPortafolioAPV.js'] } ],
};

BCS_VENTANA.ConsultaContratoSAAPV = {
	    TITLE : "UI_CONSULTA_CONTRATO_SAAPV",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
	    HEIGHT : "705px",
	    VIEW   : '../../APV/UI/Afiliacion/ConsultaContratoSAAPV/ConsultaContratoSAAPV.html',
	    PARAMS : { modoVentana : 'I' },
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../APV/UI/Afiliacion/ConsultaContratoSAAPV/ConsultaContratoSAAPV.js'] } ],
};

BCS_VENTANA.ConsultaDistribucionInversionSAAPV = {
	    TITLE : "UI_DISTRIBUCION_INVERSION_SAAPV",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "600px",
	    HEIGHT : "280px",
	    VIEW   : '../../APV/UI/Afiliacion/ConsultaDistribucionInversion/ConsultaDistribucionInversionSAAPV.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../APV/UI/Afiliacion/ConsultaDistribucionInversion/ConsultaDistribucionInversionSAAPV.js'] } ],
};

BCS_VENTANA.SubirDocumentoRecaudacion = {
	    TITLE : "UI_SUBIR_DOCUMENTO_RECAUDACION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "306px",
	    VIEW   : '../../APV/UI/Recaudacion/GestionDeRecaudacion/ModalDocumentoRecaudacionSubir.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../APV/UI/Recaudacion/GestionDeRecaudacion/ModalDocumentoRecaudacionSubir.js'] } ],
};

BCS_VENTANA.ModalGestionSAAPV = {
	    TITLE : "UI_MODAL_GESTION_SAAPV",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "213px",
	    VIEW   : '../../APV/UI/Afiliacion/GestionSAAPV/ModalGestionSAAPV.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../APV/UI/Afiliacion/GestionSAAPV/ModalGestionSAAPV.js']}],
};

BCS_VENTANA.ClienteContratoSAAPV = {
	    TITLE : "UI_CAMBIAR_A_PLAN_ACTIVO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1200px",
	    HEIGHT : "735px",
	    VIEW   : '../../APV/UI/Afiliacion/ClienteContratoSAAPV/ClienteContratoSAAPV.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../APV/UI/Afiliacion/ClienteContratoSAAPV/ClienteContratoSAAPV.js']}],
};

BCS_VENTANA.ModalClienteContratoSAAPV= {
	    TITLE : "UI_CAMBIAR_A_PLAN_ACTIVO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "600px",
	    VIEW   : '../../APV/UI/Afiliacion/ClienteContratoSAAPV/ModalClienteContratoSAAPV.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../APV/UI/Afiliacion/ClienteContratoSAAPV/ModalClienteContratoSAAPV.js' ]} ],
};
BCS_VENTANA.AsociaPerfilAPV = {
	    TITLE : "UI_ASOCIAR_PERFILES_APV",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "500px",
	    VIEW   : '../../APV/UI/Afiliacion/AsociaPerfilAPV/AsociaPerfilAPV.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../APV/UI/Afiliacion/AsociaPerfilAPV/AsociaPerfilAPV.js']}],
};
//FIN APV




BCS_VENTANA.bcsGridConf = {
	    TITLE : "bcsGridConf",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "610px",
	    HEIGHT : "335px",
	    VIEW   : 'directives/bcs-grid/bcs-grid-conf.html',
	    PARAMS : { confOptions : null },
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['directives/bcs-grid/bcs-grid-conf.js'] }],

};

BCS_VENTANA.bcsGridNewConf = {
	    TITLE : "UI_PREFERENCIAS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "610px",
	    HEIGHT : "335px",
	    VIEW   : 'directives/bcs-grid/bcs-new-grid-conf.html',
	    PARAMS : { confOptions : null },
	    DEPENDENCIES : [{ type: "js",  cache: false, files: ['directives/bcs-grid/bcs-new-grid-conf.js'] }],

};

BCS_VENTANA.ConsultaConfigValorizador = {
	    TITLE : "UI_CONSULTA_CONFIG_VALORIZADOR",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "600px",
	    HEIGHT : "360px",
	    VIEW   : '../../Plataforma/UI/INS/CrearModificarInstrumento/ConsultaConfigValorizador.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Plataforma/UI/INS/CrearModificarInstrumento/ConsultaConfigValorizador.js'] }],

};

BCS_VENTANA.MantenedorConfigValorizador = {
	    TITLE : "UI_MANTENEDOR_VALORIZADOR_CONFIG",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "150px",
	    VIEW   : '../../Plataforma/UI/INS/CrearModificarInstrumento/MantenedorConfigValorizador.html',
	    PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Plataforma/UI/INS/CrearModificarInstrumento/MantenedorConfigValorizador.js'] } ],

};

BCS_VENTANA.MantenedorInstrumentoValorizador = {
	    TITLE : "UI_MANTENEDOR_INSTRUMENTO_VALORIZADOR",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "280px",
	    VIEW   : '../../Plataforma/UI/INS/CrearModificarInstrumento/MantenedorInstrumentoValorizador.html',
	    PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Plataforma/UI/INS/CrearModificarInstrumento/MantenedorInstrumentoValorizador.js'] } ],

};

BCS_VENTANA.MantenedorCupones = {
	    TITLE : "UI_MANTENEDOR_CUPONES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "500px",
	    HEIGHT : "380px",
	    VIEW   : '../../Plataforma/UI/INS/CrearModificarInstrumento/MantenedorCupones.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Plataforma/UI/INS/CrearModificarInstrumento/MantenedorCupones.js'] }],

};

BCS_VENTANA.ValorizadorBono = {
	    TITLE : "UI_VALORIZADOR_BONO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "350px",
	    VIEW   : '../../Plataforma/UI/INS/CrearModificarInstrumento/ValorizadorBono.html',
	    PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Plataforma/UI/INS/CrearModificarInstrumento/ValorizadorBono.js'] } ],

};

BCS_VENTANA.ValorizadorPagare = {
	    TITLE : "UI_VALORIZADOR_PAGARE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "300px",
	    VIEW   : '../../Plataforma/UI/INS/CrearModificarInstrumento/ValorizadorPagare.html',
	    PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Plataforma/UI/INS/CrearModificarInstrumento/ValorizadorPagare.js'] } ],

};

BCS_VENTANA.ValorizadorCartera = {
	    TITLE : "UI_VALORIZADOR_CARTERA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "700px",
	    HEIGHT : "400px",
	    VIEW   : '../../Plataforma/UI/INS/CrearModificarInstrumento/ValorizadorCartera.html',
	    PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Plataforma/UI/INS/CrearModificarInstrumento/ValorizadorCartera.js'] } ],

};

BCS_VENTANA.ConsultaCupones = {
	    TITLE : "UI_CONSULTA_CUPONES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "500px",
	    HEIGHT : "400px",
	    VIEW   : '../../Plataforma/UI/INS/CrearModificarInstrumento/ConsultaCupones.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Plataforma/UI/INS/CrearModificarInstrumento/ConsultaCupones.js'] }],

};
//---------------------------------------------
//REPORTE
//---------------------------------------------
BCS_VENTANA.MODULO_REPORTE= {
	    TITLE : "UI_REPORTE",
	    IMAGE  : "sapUiIcon course-book",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "360px",
	    VIEW   : '',
	    PARAMS : {},
	    DEPENDENCIES : [],

};

BCS_VENTANA.IngresoOperacionesReporte = {
	    TITLE : "UI_REPORTES_INGRESO_ORDEN_REPORTADO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
	    HEIGHT : "700px",
	    VIEW   : '../../Orden/UI/REP/IngresoOperacionesReporte/OperacionReporte.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: [,'../../Orden/UI/REP/IngresoOperacionesReporte/OperacionReporte.js'
	                                                       ,'../../Orden/UI/ModuloOrdenesDirectivas.js'
	                                                       ,'../../Orden/UI/bcsCobros/bcsCobrosOrden.js'
	                                                       ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] }],
	    

};
BCS_VENTANA.IngresoReportantes = {
	    TITLE : "UI_REPORTES_INGRESO_ORDEN_REPORTANTES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "500px",
	    HEIGHT : "519px",
	    VIEW   : '../../Orden/UI/REP/IngresoOperacionesReporte/IngresoReportantes.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Orden/UI/REP/IngresoOperacionesReporte/IngresoReportantes.js'
	                                                       ,'../../Orden/UI/ModuloOrdenesDirectivas.js'
	                                                       ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] }],
	    

};

BCS_VENTANA.BlotterOperacionesReportes = {
	    TITLE : "UI_REPORTES_GESTION_ORDENES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
	    HEIGHT : "620px",
	    VIEW   : '../../Orden/UI/REP/BlotterOperacionesReportes/BlotterOrdenesReportes.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Orden/UI/REP/BlotterOperacionesReportes/BlotterOrdenesReportes.js'
	                                                       ,'../../Orden/UI/ModuloOrdenesDirectivas.js'
	                                                       ,'../../Orden/UI/REP/ConsultarTransaccionesReporte/ConsultarTransaccionesReporte.js'
	                                                       ,'../../Orden/UI/REP/ConsultarOrdenesReporte/ConsultarOrdenesReporte.js'
	                                                       ,'../../Orden/UI/REP/ConsultarAsignacionesReporte/ConsultarAsignacionesReporte.js'
	                                                       ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] }],
	    

};

BCS_VENTANA.BuscarReportantes = {
	    TITLE : "UI_BUSCADOR_REPORTANTES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "700px",
	    VIEW   : '../../Orden/UI/REP/IngresoOperacionesReporte/BuscarReportantes.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Orden/UI/REP/IngresoOperacionesReporte/BuscarReportantes.js'
	                                                       ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] }],
	    
};

BCS_VENTANA.VerReportantes = {
	    TITLE : "UI_VER_REPORTANTES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "400px",
	    VIEW   : '../../Orden/UI/REP/BlotterOperacionesReportes/VerReportantes.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Orden/UI/REP/BlotterOperacionesReportes/VerReportantes.js'
	                                                       ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] }],
	    
};

BCS_VENTANA.BlotterOrdenesReporteNegociacion = {
	    TITLE : "UI_REPORTES_NEGOCIACION_ORDENES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "530px",
	    VIEW   : '../../Orden/UI/REP/BlotterOrdenesReportesNegociacion/BlotterOrdenesNegociacion.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Orden/UI/REP/BlotterOrdenesReportesNegociacion/BlotterOrdenesNegociacion.js'
	                                                       ,'../../Orden/UI/REP/BlotterOrdenesReportesNegociacion/OrdenesPorNegociar.js'
	                                                       ,'../../Orden/UI/REP/BlotterOrdenesReportesNegociacion/OrdenesEnNegociacion.js'
	                                                       ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] }],
	    
};

BCS_VENTANA.AsignacionOrdenReporte = {
		TITLE : "UI_ASIGNACION_ORDEN_REPORTES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "350px",
	    VIEW   : '../../Orden/UI/REP/AsignacionOrdenReporte/AsignacionOrdenReporte.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Orden/UI/REP/AsignacionOrdenReporte/AsignacionOrdenReporte.js'
	                                                       ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] }],
}

BCS_VENTANA.AsociarReportantes = {
		TITLE : "UI_ASOCIAR_REPORTANTE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "500px",
	    HEIGHT : "250px",
	    VIEW   : '../../Orden/UI/REP/IngresoOperacionesReporte/AsociarReportante.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Orden/UI/REP/IngresoOperacionesReporte/AsociarReportante.js'] }],
}

BCS_VENTANA.BlotterCustodiaReportes = {
	    TITLE : "UI_REPORTES_CUSTODIA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
	    HEIGHT : "700px",
	    VIEW   : '../../Orden/UI/REP/BlotterCustodiaOperacionesReportes/BlotterCustodiaOperacionesReportes.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Orden/UI/REP/BlotterCustodiaOperacionesReportes/BlotterCustodiaOperacionesReportes.js'
	                                                       ,'../../Orden/UI/ModuloOrdenesDirectivas.js'
	                                                       ,'../../Orden/UI/REP/Custodia/ConsultaAnticiposReportes/ConsultaAnticiposReportes.js'
	                                                       ,'../../Orden/UI/REP/Custodia/ReportesHistoricos/ReportesHistoricos.js'
	                                                       ,'../../Orden/UI/REP/Custodia/ReportesVigentes/ReportesVigentes.js'
	                                                       ,'../../Orden/UI/REP/Custodia/AnticipoReportes/AnticipoReportes.js'
	                                                       ,'../../Orden/UI/REP/Custodia/VencimientoReportes/VencimientoReportes.js'
	                                                       ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] }],
	    
};

BCS_VENTANA.DetalleReportante = {
	    TITLE : "UI_INFORMACION_ORDEN",
	    IMAGE  : "",
	    PRINTABLE : true,
	    ENABLE : true,
	    WIDTH  : "750px",
	    HEIGHT : "500px",
	    VIEW   : '../../Orden/UI/REP/DetalleOrdenReporte/DetalleReportante.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/REP/DetalleOrdenReporte/DetalleReportante.js'
	                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'
	                                                         ,'../../Orden/UI/ModuloOrdenesDirectivas.js'] } ],

	};

BCS_VENTANA.DetalleOrdenReporte = {
	    TITLE : "UI_INFORMACION_ORDEN",
	    IMAGE  : "",
	    PRINTABLE : true,
	    ENABLE : true,
	    WIDTH  : "750px",
	    HEIGHT : "500px",
	    VIEW   : '../../Orden/UI/REP/DetalleOrdenReporte/DetalleOrdenReporte.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/REP/DetalleOrdenReporte/DetalleOrdenReporte.js'
	                                                         ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'
	                                                         ,'../../Orden/UI/ModuloOrdenesDirectivas.js'] } ],

	};
BCS_VENTANA.AnticipoReportes = {
	    TITLE : "UI_ANTICIPO_REPORTE",
	    IMAGE  : "",
	    ENABLE : true,
	    PRINTABLE : true,
	    WIDTH  : "500px",
		HEIGHT : "430px",
	    VIEW   : '../../Orden/UI/REP/Custodia/AnticipoReportes/AnticipoReportes.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/REP/Custodia/AnticipoReportes/AnticipoReportes.js'] } ],

	};

//---------------------------------------------
//INTERFACES PERU
//---------------------------------------------
BCS_VENTANA.MODULO_INTERFACES= {
	    TITLE : "UI_INTERFACES",
	    IMAGE  : "sapUiIcon course-book",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "360px",
	    VIEW   : '',
	    PARAMS : {},
	    DEPENDENCIES : [],

};

BCS_VENTANA.PanelSolicitudInterfaces = {
		TITLE : "UI_SOLICITAR_INTERFACES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "654px",
	    HEIGHT : "305px",
	    VIEW   : '../../Normativa/UI/Interfaces/SolicitudInterfaces/PanelSolicitudInterfaces.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Interfaces/SolicitudInterfaces/PanelSolicitudInterfaces.js'] }],
};


BCS_VENTANA.MantenedorFilialesP40 = {
		TITLE : "UI_MANTENEDOR_DE_FILIALES_P40",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "730px",
	    HEIGHT : "510px",
	    VIEW   : '../../Normativa/UI/Interfaces/MantenedorFilialesP40/MantenedorFilialesP40.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Interfaces/MantenedorFilialesP40/MantenedorFilialesP40.js'] }],
};

BCS_VENTANA.AgregarModificarFilialesP40 = {
		TITLE : "UI_FILIAL_ARCHIVO_P40",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "350px",
	    HEIGHT : "240px",
	    VIEW   : '../../Normativa/UI/Interfaces/MantenedorFilialesP40/FilialArchivoP40.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Interfaces/MantenedorFilialesP40/FilialArchivoP40.js'] }],
};

BCS_VENTANA.GeneracionInterfaces = {
		TITLE : "UI_GENERACION_INTERFACES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "550px",
	    HEIGHT : "160px",
	    VIEW   : '../../Normativa/UI/Interfaces/GeneracionInterfaces/GenerarInterfaces.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Interfaces/GeneracionInterfaces/GenerarInterfaces.js'] }],
};


BCS_VENTANA.MantenedorSolicitudesInterfaces = {
	    TITLE : "UI_CONSULTAR_SOLICITUDES_INTERFACES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "960px",
	    HEIGHT : "auto",
	    VIEW   : 'BAT/MantenedorPrograma/MantenedorPrograma.html',
	    PARAMS : {IdTipoSalida: 'R'},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['BAT/MantenedorPrograma/MantenedorPrograma.js','BAT/MantenedorProgramaGrid.js'] } ],

};

BCS_VENTANA.CargaDatosInterfaces = {
		TITLE : "UI_CARGAR_ARCHIVOS_INTERFACES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "550px",
	    HEIGHT : "150px",
	    VIEW   : '../../Normativa/UI/Interfaces/CargaDatosInterfaces/CargaDatosInterfaces.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Interfaces/CargaDatosInterfaces/CargaDatosInterfaces.js'] }],
};

BCS_VENTANA.EnvioArchivoInterfaces = {
		TITLE : "UI_ENVIO_ARCHIVO_INTERFACES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
		HEIGHT : "220px",
	    VIEW   : '../../Normativa/UI/Interfaces/SolicitudInterfaces/EnvioInterfaces.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Interfaces/SolicitudInterfaces/EnvioInterfaces.js'] }],
};

BCS_VENTANA.EnvioArchivoCM = {
		TITLE : "UI_ENVIO_ARCHIVO_CM",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
		HEIGHT : "700px",
	    VIEW   : '../../Normativa/UI/Interfaces/SolicitudInterfaces/EnvioArchivoCM.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/Interfaces/SolicitudInterfaces/EnvioArchivoCM.js'] }],
};

BCS_VENTANA.CargaArchivoCuadraturaCustodia = {
		TITLE : "UI_CARGA_ARCHIVO_CUADRATURA_CUSTODIA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "550px",
		HEIGHT : "150px",
	    VIEW   : '../../Custodia/UI/ConsultarCuadraturaCustodia/CargaArchivoCuadraturaCustodia.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Custodia/UI/ConsultarCuadraturaCustodia/CargaArchivoCuadraturaCustodia.js'] }],
};

BCS_VENTANA.MantenedorMetadataContabilidad = {
		TITLE : "UI_CONFIGURACION_METADATA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "650px",
		HEIGHT : "auto",
	    VIEW   : '../../Contabilidad/UI/MantenedorMetadata/BlotterMantenedorMetadata.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Contabilidad/UI/MantenedorMetadata/BlotterMantenedorMetadata.js'
	    													,'../../Contabilidad/UI/MantenedorEventoGrid.js'] }],
};

BCS_VENTANA.AgregarMetadata = {
		TITLE : "UI_AGREGAR_METADATA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
		HEIGHT : "auto",
	    VIEW   : '../../Contabilidad/UI/MantenedorMetadata/AgregarMetadata.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Contabilidad/UI/MantenedorMetadata/AgregarMetadata.js'
	    													,'../../Contabilidad/UI/MantenedorEventoGrid.js'] }],
};

BCS_VENTANA.AgregarAsientoMetadata = {
		TITLE : "UI_AGREGAR_POR_METADATA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
		HEIGHT : "605px",
	    VIEW   : '../../Contabilidad/UI/ConfiguracionComprobantes/ConfiguracionMetadata.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Contabilidad/UI/ConfiguracionComprobantes/ConfiguracionMetadata.js'
	    													,'../../Contabilidad/UI/MantenedorEventoGrid.js'] }],
};

BCS_VENTANA.CuadraturaPagoDividendosAGF = {
		TITLE : "UI_CUADRATURA_PAGO_DIVIDENDOS_AGF",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
		HEIGHT : "700px",
	    VIEW   : '../../FondoMutuo/UI/CuadraturaPagoDividendosAGF/CuadraturaPagoDividendosAGF.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../FondoMutuo/UI/CuadraturaPagoDividendosAGF/CuadraturaPagoDividendosAGF.js'
	    													,'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] }],
};

BCS_VENTANA.CuadraturaAGF = {
		TITLE : "UI_CUADRATURA_AGF",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
		HEIGHT : "700px",
	    VIEW   : '../../FondoMutuo/UI/CuadraturaAGF/CuadraturaAGF.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../FondoMutuo/UI/CuadraturaAGF/CuadraturaAGF.js'
	    													,'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] }],
};

BCS_VENTANA.CuadraturaCierreDiarioFFMM = {
    TITLE : "UI_CUADRATURA_DIARIA_OPERACIONES_FFMM",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1028px",
    HEIGHT : "715px", 
    VIEW   : '../../FondoMutuo/UI/CuadraturaCierreDiario/CuadraturaCierreDiario.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/CuadraturaCierreDiario/CuadraturaCierreDiario.js']}],

};

BCS_VENTANA.CuadraturaCierreDiarioCUS = {
	    TITLE : "UI_CUADRATURA_DIARIA_OPERACIONES_CUS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1028px",
	    HEIGHT : "696px", 
	    VIEW   : '../../Custodia/UI/CuadraturaCierreDiario/CuadraturaCierreDiario.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Custodia/UI/CuadraturaCierreDiario/CuadraturaCierreDiario.js']}],

	};

BCS_VENTANA.EditarMetadata = {
		TITLE : "UI_EDITAR_METADATA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
		HEIGHT : "auto",
	    VIEW   : '../../Contabilidad/UI/MantenedorMetadata/EditarMetadata.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Contabilidad/UI/MantenedorMetadata/EditarMetadata.js'
	    													,'../../Contabilidad/UI/MantenedorEventoGrid.js'] }],

	};

BCS_VENTANA.CuadraturaCierreDiarioTES = {
	    TITLE : "UI_CUADRATURA_DIARIA_OPERACIONES_TES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1028px",
	    HEIGHT : "710px", 
	    VIEW   : '../../Tesoreria/UI/CuadraturaCierreDiario/CuadraturaCierreDiario.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Tesoreria/UI/CuadraturaCierreDiario/CuadraturaCierreDiario.js']}],

	};

BCS_VENTANA.ConsultarGarantiaFFMM = {
	    TITLE : "UI_CONSULTAR_GARANTIA_FFMM",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "874px",
	    HEIGHT : "614px",
	    VIEW   : '../../FondoMutuo/UI/IngresarAporteRescate/BusquedaGarantias.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/IngresarAporteRescate/BusquedaGarantias.js']}],
	};

BCS_VENTANA.CargaConfiguracionContable = {
	    TITLE : "UI_CARGA_CONFIGURACION_CONTABLE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "200px",
	    VIEW   : '../../Contabilidad/UI/ConfiguracionComprobantes/CargaConfiguracion.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Contabilidad/UI/ConfiguracionComprobantes/CargaConfiguracion.js']}],
	};

BCS_VENTANA.CuadraturaCierreDiarioCON = {
	    TITLE : "UI_CUADRATURA_DIARIA_OPERACIONES_CON",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1028px",
	    HEIGHT : "710px", 
	    VIEW   : '../../Contabilidad/UI/CuadraturaCierreDiario/CuadraturaCierreDiario.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Contabilidad/UI/CuadraturaCierreDiario/CuadraturaCierreDiario.js']}],

	};

BCS_VENTANA.CuentascontablesBasilea = {
	    TITLE : "UI_CUENTAS_CONTABLES_BASILEA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1028px",
	    HEIGHT : "710px", 
	    VIEW   : '../../Contabilidad/UI/CuentasContablesBasilea/CuentasContablesBasilea.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Contabilidad/UI/CuentasContablesBasilea/CuentasContablesBasilea.js']}],

	};

BCS_VENTANA.EditarCuentascontablesBasilea = {
	    TITLE : "UI_EDITAR_CUENTA_CONTABLE_BASILEA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "500px",
	    HEIGHT : "275px", 
	    VIEW   : '../../Contabilidad/UI/CuentasContablesBasilea/ModalEditarCuentaBasilea.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Contabilidad/UI/CuentasContablesBasilea/ModalEditarCuentaBasilea.js']}],

	};

BCS_VENTANA.CuadraturaCierreDiarioFAC = {
	    TITLE : "UI_CUADRATURA_DIARIA_OPERACIONES_FAC",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1028px",
	    HEIGHT : "702px", 
	    VIEW   : '../../Facturacion/UI/CuadraturaCierreDiario/CuadraturaCierreDiario.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Facturacion/UI/CuadraturaCierreDiario/CuadraturaCierreDiario.js']}],

	};

BCS_VENTANA.MatenedorParametroFinanciero = {
	    TITLE : "UI_PARAMETRO_FINANCIERO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px", 
	    HEIGHT : "500px",
	    VIEW   : '../../Plataforma/UI/MER/MantenedorParametroFinanciero/MantenedorParametroFinanciero.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Plataforma/UI/MER/MantenedorParametroFinanciero/MantenedorParametroFinanciero.js']}],

	};

BCS_VENTANA.CrearParametroFinanciero = {
	    TITLE : "UI_AGREGAR_PARAMETRO_FINANCIERO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "500px", 
	    HEIGHT : "200px",
	    VIEW   : '../../Plataforma/UI/MER/MantenedorParametroFinanciero/CrearParametroFinanciero.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Plataforma/UI/MER/MantenedorParametroFinanciero/CrearParametroFinanciero.js']}],

	};

BCS_VENTANA.ResumenOperacionesDiarias = {
    TITLE : "UI_RESUMEN_OPERACIONES_DIARIAS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1028", 
    HEIGHT : "597px",
    VIEW   : '../../Orden/UI/ResumenOperacionesDiarias/ResumenOperacionesDiarias.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Orden/UI/ResumenOperacionesDiarias/ResumenOperacionesDiarias.js']}],
};

BCS_VENTANA.CuadraturaCierreDiarioCTA = {
	    TITLE : "UI_CUADRATURA_DIARIA_OPERACIONES_CTA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1028px",
	    HEIGHT : "710px", 
	    VIEW   : '../../CuentaInversion/UI/CuadraturaCierreDiario/CuadraturaCierreDiario.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../CuentaInversion/UI/CuadraturaCierreDiario/CuadraturaCierreDiario.js']}],

	};

BCS_VENTANA.CuadraturaCierreDiarioFWD = {
	    TITLE : "UI_CUADRATURA_DIARIA_OPERACIONES_FWD",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1028px",
	    HEIGHT : "710px", 
	    VIEW   : '../../Mesa/UI/FWD/CuadraturaCierreDiario/CuadraturaCierreDiario.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Mesa/UI/FWD/CuadraturaCierreDiario/CuadraturaCierreDiario.js']}],

	};

BCS_VENTANA.CuadraturaCierreDiarioMRF = {
	    TITLE : "UI_CUADRATURA_DIARIA_OPERACIONES_MRF",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1028px",
	    HEIGHT : "710px", 
	    VIEW   : '../../Mesa/UI/MRF/CuadraturaCierreDiario/CuadraturaCierreDiario.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Mesa/UI/MRF/CuadraturaCierreDiario/CuadraturaCierreDiario.js']}],

	};

BCS_VENTANA.CuadraturaCierreDiarioSPT = {
	    TITLE : "UI_CUADRATURA_DIARIA_OPERACIONES_SPT",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1028px",
	    HEIGHT : "auto", 
	    VIEW   : '../../Mesa/UI/SPT/CuadraturaCierreDiario/CuadraturaCierreDiario.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Mesa/UI/SPT/CuadraturaCierreDiario/CuadraturaCierreDiario.js']}],

	};
	
	BCS_VENTANA.CalculadoraSpot = {
	    TITLE : "UI_CALCULADORA_SPOT",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1028px",
	    HEIGHT : "auto", 
	    VIEW   : '../../Mesa/UI/SPT/CalculadoraSpot/CalculadoraSpot.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Mesa/UI/SPT/CalculadoraSpot/CalculadoraSpot.js' ]}],

	};

BCS_VENTANA.PreCertificacionOmnibus = {
		TITLE : "UI_PRECERTIFICACION_OMNIBUS",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "1100px",
		HEIGHT : "700px",
		VIEW   : '../../FondoMutuo/UI/Omnibus/ConsultarOmnibus.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/Omnibus/ConsultarOmnibus.js'
		                                                   ,'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] }],

	};

BCS_VENTANA.MaestroParametrosFinanciero = {
	    TITLE : "UI_TIPOS_CAMBIO_E_INDICES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "auto",
	    VIEW   : '../../Plataforma/UI/MER/MantenedorParametroFinanciero/MaestroParametroFinanciero.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Plataforma/UI/MER/MantenedorParametroFinanciero/MaestroParametroFinanciero.js'
	                                                         ,'../../Plataforma/UI/MER/MantenedorParametroFinanciero/TipoCambio.js'
	                                                         ,'../../Plataforma/UI/MER/MantenedorParametroFinanciero/Indices.js'] } ],

	};

BCS_VENTANA.ConsultaUsoParametroFinanciero = {
	    TITLE : "UI_CONSULTA_USO_PARAMETRO_FINANCIERO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "500px",
	    VIEW   : '../../Plataforma/UI/MER/MantenedorParametroFinanciero/ConsultaUsoParametroFinanciero.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Plataforma/UI/MER/MantenedorParametroFinanciero/ConsultaUsoParametroFinanciero.js'] } ],

	};


BCS_VENTANA.BlotterCSC = {
	    TITLE : "UI_BLOTTER_CSC",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
		HEIGHT : "630px",
	    VIEW   : '../../FondoMutuo/UI/BlotterCSC/BlotterCSC.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js',
	    													 '../../FondoMutuo/UI/BlotterCSC/BlotterCSC.js'] } ],
	    

	};

BCS_VENTANA.IngresarCSC = {
		  TITLE : "UI_INGRESAR_CSC",
		  IMAGE  : "",
		  ENABLE : true,
		  WIDTH  : "1100px",
		  HEIGHT : "700px",
		  VIEW   : '../../FondoMutuo/UI/IngresarCSC/IngresarCSC.html',
		  PARAMS : {},
		  DEPENDENCIES : [{ type: "js", cache: true, files: [	'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js',
			  													'../../FondoMutuo/UI/IngresarCSC/IngresarCSC.js'] }],
};

BCS_VENTANA.RevocarCSC = {
		  TITLE : "UI_REVOCAR_CSC",
		  IMAGE  : "",
		  ENABLE : true,
		  WIDTH  : "1100px",
		  HEIGHT : "700px",
		  VIEW   : '../../FondoMutuo/UI/RevocarCSC/RevocarCSC.html',
		  PARAMS : {},
		  DEPENDENCIES : [{ type: "js", cache: true, files: [	'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js',
			  													'../../FondoMutuo/UI/RevocarCSC/Revocar.js'] }],
};

BCS_VENTANA.BlotterPAC = {
	    TITLE : "UI_BLOTTER_PAC",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
		HEIGHT : "700px",
	    VIEW   : '../../Tesoreria/UI/BlotterPAC/BlotterPAC.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Tesoreria/UI/BlotterPAC/BlotterPAC.js'] } ],
	    

	};
BCS_VENTANA.IngresarMandatoPAC = { 
		  TITLE : "UI_INGRESAR_MANDATO_PAC",
		  IMAGE  : "",
		  ENABLE : true,
		  WIDTH  : "1100px",
		  HEIGHT : "300px",
		  VIEW   : '../../Tesoreria/UI/IngresoMandatoPAC/IngresarMandatoPAC.html',
		  PARAMS : {},
		  DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Tesoreria/UI/IngresoMandatoPAC/IngresarMandatoPAC.js'] }],
		  
    };
BCS_VENTANA.IngresoNuevoMercado = {
	    TITLE : "UI_NUEVO_INGRESO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "600px",
	    HEIGHT : "200px",
	    VIEW   : 'INS/Clasificaciondeactivos/NuevoMercado.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../FondoMutuo/UI/BlotterTraspasos/Traspaso/Traspaso.js',
	    												   '../../FondoMutuo/UI/BlotterTraspasos/Traspaso/Paneles/PanelEgreso.js',
	    												   '../../FondoMutuo/UI/BlotterTraspasos/Traspaso/Paneles/PanelIngreso.js'] }],
};
BCS_VENTANA.ModificarMercado = {
	    TITLE : "UI_MODIFICAR",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "600px",
	    HEIGHT : "300px",
	    VIEW   : 'INS/Clasificaciondeactivos/Modificar/Modificar.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../FondoMutuo/UI/BlotterTraspasos/Traspaso/Traspaso.js',
	    												   '../../FondoMutuo/UI/BlotterTraspasos/Traspaso/Paneles/PanelEgreso.js',
	    												   '../../FondoMutuo/UI/BlotterTraspasos/Traspaso/Paneles/PanelIngreso.js'] }],

};

BCS_VENTANA.EliminarMercado = {
	    TITLE : "UI_ELIMINAR",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "600px",
	    HEIGHT : "250px",
	    VIEW   : 'INS/Clasificaciondeactivos/Eliminar/Eliminar.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../FondoMutuo/UI/BlotterTraspasos/Traspaso/Traspaso.js',
	    												   '../../FondoMutuo/UI/BlotterTraspasos/Traspaso/Paneles/PanelEgreso.js',
	    												   '../../FondoMutuo/UI/BlotterTraspasos/Traspaso/Paneles/PanelIngreso.js'] }],

};

BCS_VENTANA.ClasificarActivo = {
	    TITLE : "UI_CLASIFICAR",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "600px",
	    HEIGHT : "250px",
	    VIEW   : 'INS/Clasificaciondeactivos/Clasificar/ClasificarActivo.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../FondoMutuo/UI/BlotterTraspasos/Traspaso/Traspaso.js',
	    												   '../../FondoMutuo/UI/BlotterTraspasos/Traspaso/Paneles/PanelEgreso.js',
	    												   '../../FondoMutuo/UI/BlotterTraspasos/Traspaso/Paneles/PanelIngreso.js'] }],

};

BCS_VENTANA.Clasificacion_Indices= {
	    TITLE : "UI_INDICES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "600px",
	    HEIGHT : "250px",
	    VIEW   : '../../Plataforma/UI/INS/Clasificaciondeactivos/Indices.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Plataforma/UI/INS/Clasificaciondeactivos/Indices.js',
	    	'../../Plataforma/UI/INS/Clasificaciondeactivos/Crear/PanelIndice/AsignarIndice.js',
	    	'../../Plataforma/UI/INS/Clasificaciondeactivos/Eliminar/PanelEliminarIndice/EliminarIndice.js'] }],

};


BCS_VENTANA.VDetalleAutorizacion = { 
	    TITLE : "UI_DETALLE_AUTORIZACION", 
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "650px",
	    HEIGHT : "400px",
	    VIEW   : '../../Plataforma/UI/AUT/DetalleAutorizacion/DetalleAutorizacion.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Plataforma/UI/AUT/DetalleAutorizacion/DetalleAutorizacion.js'] } ],

};
	

BCS_VENTANA.SolicitarCartolaOnline = {
	    TITLE : "UI_SOLICITAR_CARTOLA_ONLINE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1500px",
	    HEIGHT : "800px",
	    VIEW   : '../../Normativa/UI/Cartolas/CartolaOnline/CartolaOnline.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Normativa/UI/Cartolas/CartolaOnline/CartolaOnline.js']}],

};

BCS_VENTANA.AtributtionInforme = {
	    TITLE : "UI_ATRIBUTION_ANALISYS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1500px",
	    HEIGHT : "800px",
	    VIEW   : '../../CuentaInversion/UI/AtributtionAnalysis/Informe/AtributtionInforme.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../CuentaInversion/UI/AtributtionAnalysis/Informe/AtributtionInforme.js']}],

};
BCS_VENTANA.InformacionCliente = {
	    TITLE : "UI_REPORTE_INFORMACION_CLIENTE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
	    HEIGHT : "610px",
	    VIEW   : '../../SocioNegocio/UI/EJE/InformacionCliente/ConsultarInformacionCliente.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../SocioNegocio/UI/EJE/InformacionCliente/ConsultarInformacionCliente.js',
	    	                                                 '../../SocioNegocio/UI/EJE/InformacionCliente/ConsultarInformacionClienteGrid.js'] } ],
};
BCS_VENTANA.ForwardVencer = {
	    TITLE : "UI_REPORTE_FWD_VENCER",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
	    HEIGHT : "610px",
	    VIEW   : '../../SocioNegocio/UI/EJE/Fwd/ConsultarForwardPorVencer.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../SocioNegocio/UI/EJE/Fwd/ConsultarForwardPorVencer.js', 
	    	                                                 '../../SocioNegocio/UI/EJE/Fwd/ConsultarForwardPorVencerGrid.js'] } ],

};

BCS_VENTANA.ReporteGestionClienteInstrumento = {
	    TITLE : "UI_REPORTE_GESTION_CLIENTE_INSTRUMENTO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
	    HEIGHT : "610px",
	    VIEW   : '../../SocioNegocio/UI/EJE/ReporteGestionCliente/ReporteGestionClienteInstrumento.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../SocioNegocio/UI/EJE/ReporteGestionCliente/ReporteGestionClienteInstrumento.js'] } ],
};

BCS_VENTANA.ReportePatrimonio = {
	    TITLE : "UI_REPORTE_PATRIMONIO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "auto",
	    VIEW   : '../../SocioNegocio/UI/EJE/ReportePatrimonio/ReportePatrimonio.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../SocioNegocio/UI/EJE/ReportePatrimonio/ReportePatrimonioController.js'] } ],
};

BCS_VENTANA.ReporteGestionClienteFlujoMensualAportesRescates = {
	    TITLE : "UI_REPORTE_GESTION_CLIENTE_FLUJO_MENSUAL_APORTES_RESCATES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "auto",
	    VIEW   : '../../SocioNegocio/UI/EJE/ReporteGestionCliente/ReporteGestionClienteFlujoMensualAportesRescates.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../SocioNegocio/UI/EJE/ReporteGestionCliente/ReporteGestionClienteFlujoMensualAportesRescates.js'] } ],
};

BCS_VENTANA.ReporteGestionConsultarRentabilidad = {
	    TITLE  : "UI_CONSULTAR_RENTABILIDAD",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "auto",
	    VIEW   : '../../SocioNegocio/UI/EJE/ReporteGestionCliente/ReporteGestionConsultarRentabilidad.html',
	    PARAMS : { p1: 0, p2: 0},
	     DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../SocioNegocio/UI/EJE/ReporteGestionCliente/ReporteGestionConsultarRentabilidad.js', 
	    													  '../../SocioNegocio/UI/EJE/ReporteGestionCliente/ReporteGestionConsultarRentabilidadGrid.js'] } ],

};

BCS_VENTANA.InformeOrdenesConfirmadas = {
	    TITLE : "UI_INFORME_ORDENES_CONFIRMADAS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
	    HEIGHT : "610px",
	    VIEW   : '../../Orden/UI/InformeOrdenesConfirmadas/InformeOrdenesConfirmadas.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/InformeOrdenesConfirmadas/InformeOrdenesConfirmadas.js', 
	    	                                                 '../../Orden/UI/InformeOrdenesConfirmadas/InformeOrdenesConfirmadasGrid.js'] } ],
};

BCS_VENTANA.InformeOperacionesPendientesDatatec = {
	    TITLE : "UI_OPERACIONES_PENDIENTES_DATATEC",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "980px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/SPT/InformeOperacionesPendientesDatatec/InformeOperacionesPendientesDatatec.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Mesa/UI/SPT/InformeOperacionesPendientesDatatec/InformeOperacionesPendientesDatatec.js'] } ],

};
BCS_VENTANA.BlotterAdministrarMatrizNOAPV = {
		TITLE : "UI_MANTENEDOR_MATRIZ_NO_APV",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "600px",
		HEIGHT : "auto",
	    VIEW   : '../../Portafolio/UI/AdministrarMatrizAgrupadorNOAPV/BlotterAdministrarMatrizNOAPV.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Portafolio/UI/AdministrarMatrizAgrupadorNOAPV/BlotterAdministrarMatrizNOAPV.js'] }],
};

BCS_VENTANA.CrearMatrizAgrupadorNOAPV = {
		TITLE : "UI_AGREGAR_MATRIZ_NO_APV",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
		HEIGHT : "auto",
	    VIEW   : '../../Portafolio/UI/AdministrarMatrizAgrupadorNOAPV/CrearMatrizNOAPV.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Portafolio/UI/AdministrarMatrizAgrupadorNOAPV/CrearMatrizNOAPV.js'] }],
};

BCS_VENTANA.ModificarMatrizAgrupadorNOAPV = {
		TITLE : "UI_MODIFICAR_MATRIZ_NO_APV",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
		HEIGHT : "400px",
	    VIEW   : '../../Portafolio/UI/AdministrarMatrizAgrupadorNOAPV/ModificarMatrizNOAPV.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Portafolio/UI/AdministrarMatrizAgrupadorNOAPV/ModificarMatrizNOAPV.js'] }],
};

BCS_VENTANA.ConfiguracionPatrimonioConsolidado = {
	    TITLE : "UI_CONFIGURACION_PATRIMONIO_CONSOLIDADO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "500px",
	    VIEW   : '../../Plataforma/UI/PLA/ConfiguracionPatrimonioConsolidado/ConfiguracionPatrimonioConsolidado.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Plataforma/UI/PLA/ConfiguracionPatrimonioConsolidado/ConfiguracionPatrimonioConsolidado.js'
	                                                         ,'../../Plataforma/UI/PLA/ConfiguracionPatrimonioConsolidado/ConfiguracionPatrimonioCaja.js'
	                                                         ,'../../Plataforma/UI/PLA/ConfiguracionPatrimonioConsolidado/ConfiguracionPatrimonioCustodia.js'] } ],

	};
	
	
BCS_VENTANA.ConfiguracionFlujoCustodia = {
	    TITLE  : "UI_CONFIGURACION_FLUJO_CUSTODIA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
	    HEIGHT : "700px",
	    VIEW   : '../../Plataforma/UI/PLA/ConfiguracionFlujoCustodia/ConfiguracionFlujoCustodia.html',
	    PARAMS : { p1: 0, p2: 0},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Plataforma/UI/PLA/ConfiguracionFlujoCustodia/ConfiguracionFlujoCustodia.js'] }],

	};
	

BCS_VENTANA.GeneraArchivoCargoPAC = {
	    TITLE : "UI_GENERA_ARCHIVO_CARGA_PAC",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "500px",
	    VIEW   : '../../Tesoreria/UI/BlotterPAC/GeneraArchivoCargo.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Tesoreria/UI/BlotterPAC/GeneraArchivoCargo.js'
	                                                       ] } ],

	};

BCS_VENTANA.ConsultarPatrimonio = {
	    TITLE : "UI_CONSULTAR_PATRIMONIO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1125px",
	    HEIGHT : "725px",
	    VIEW   : '../../Custodia/UI/ConsultarPatrimonio/ConsultarPatrimonio.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/ConsultarPatrimonio/ConsultarPatrimonio.js', 
	                                                         '../../Custodia/UI/ConsultarPatrimonio/ConsultarPatrimonioCustodia.js', 
	                                                         '../../Custodia/UI/ConsultarPatrimonio/ConsultarPatrimonioConsolidado.js',                                                         
	                                                         '../../Custodia/UI/ConsultarPatrimonio/ConsultarPatrimonioCaja.js'] } ],

	};
BCS_VENTANA.ConsultarHistorialTransacciones = {
	    TITLE : "UI_CONSULTA_HISTORIAL_TRANSACCIONES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
	    HEIGHT : "618px",
	    VIEW   : '../../Orden/UI/ConsultarTransacciones/ConsultarHistorialTransacciones.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Orden/UI/ConsultarTransacciones/ConsultarHistorialTransacciones.js'
	    													,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'
	    													,'../../Orden/UI/ModuloOrdenesDirectivas.js'] } ],
};

BCS_VENTANA.ConsultarDetallePrecioPromedio = {
	    TITLE : "UI_CONSULTA_DETALLE_PRECIO_PROMEDIO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "980px",
	    HEIGHT : "450px",
	    VIEW   : '../../Custodia/UI/ConsultarDetallePrecioPromedio/ConsultarDetallePrecioPromedio.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Custodia/UI/ConsultarDetallePrecioPromedio/ConsultarDetallePrecioPromedio.js'] } ]
};

BCS_VENTANA.IngresarOrdenFU = {
	TITLE : "UI_INGRESAR_ORDEN_FU",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "900px",
	HEIGHT : "650px",
	VIEW   : '../../Orden/UI/IngresarOrden/IngresarOrdenFU.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: false, files: [,'../../Orden/UI/IngresarOrden/IngresarOrdenFU.js'
														,'../../Orden/UI/bcsCobros/bcsCobrosBeta.js'
														,'../../Orden/UI/bcsCobros/bcsCobrosOrden.js'
														 ,'../../Orden/UI/ModuloOrdenesDirectivas.js'
														 ,'../../Orden/UI/ModuloOrdenesConfiguracionGrids.js'] } ],

};

BCS_VENTANA.SigaCrmHistorico = {
	    TITLE : "UI_SIGACRM_HISTORICO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "350px",
	    HEIGHT : "100px",
	    VIEW   : '../../Custodia/UI/SigaCrmHistorico/SigaCrmHistorico.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Custodia/UI/SigaCrmHistorico/SigaCrmHistorico.js']}],

};

BCS_VENTANA.ConfiguracionAFP = {
    TITLE : "UI_CONFIGURAR_AFP",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "1100px",
    HEIGHT : "600px",
    VIEW   : '../../Facturacion/UI/Informes/ConfiguracionAFP.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Facturacion/UI/Informes/ConfiguracionAFP.js'
    													 ,'../../Facturacion/UI/Informes/ConfiguracionAFPGrid.js']}],
    
};

BCS_VENTANA.ConsultaValorCuotaVigente = {
		TITLE : "UI_CONSULTA_VALOR_CUOTA",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "1100px",
		HEIGHT : "650px",
		VIEW   : '../../FondoMutuo/UI/ConsultaValorCuotaVigente/ConsultaValorCuotaVigente.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../FondoMutuo/UI/ConsultaValorCuotaVigente/ConsultaValorCuotaVigente.js'
		                                                   ,'../../FondoMutuo/UI/ModuloFFMConfiguracionGrids.js'] }],
};

BCS_VENTANA.GenerarArchivoAFP = {
    TITLE : "UI_GENERAR_ARCHIVO_AFP",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "900px",
    HEIGHT : "120px",
    VIEW   : '../../Facturacion/UI/Informes/GenerarArchivoAFP.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Facturacion/UI/Informes/GenerarArchivoAFP.js']}],
    
};

BCS_VENTANA.ConsultaConciliacion = {
	    TITLE : "UI_CONSULTA_CONCILIACION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "370px",
	    HEIGHT : "100px",
	    VIEW   : '../../Plataforma/UI/PLA/Conciliacion/ConsultaConciliacion.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [  '../../Plataforma/UI/PLA/Conciliacion/ConsultaConciliacion.js']}],
	    
	};
	
BCS_VENTANA.MODULO_INFORMES= {
	    TITLE : "UI_INFORMES",
	    IMAGE  : "sapUiIcon course-book",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "360px",
	    VIEW   : '',
	    PARAMS : {},
	    DEPENDENCIES : [],

};

BCS_VENTANA.GenerarInformeEstadisticas = {
		TITLE : "UI_GENERAR_INFORME_ESTADISTICAS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "380px",
	    HEIGHT : "145px",
	    VIEW   : '../../Orden/UI/GenerarInformeEstadisticas/GenerarInformeEstadisticas.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Orden/UI/GenerarInformeEstadisticas/GenerarInformeEstadisticas.js'] }],
};

BCS_VENTANA.MantenedorBancoPrevired = {
		TITLE : "UI_MANTENEDOR_BANCO_PREVIRED",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "850px",
	    HEIGHT : "530px",
	    VIEW   : '../../APV/UI/Recaudacion/MantenedorBancoPrevired/MantenedorBancoPrevired.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../APV/UI/Recaudacion/MantenedorBancoPrevired/MantenedorBancoPrevired.js'] }],
};

BCS_VENTANA.IngresarBancoPrevired = {
		TITLE : "UI_INGRESAR_BANCO_PREVIRED",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "405px",
	    HEIGHT : "155px",
	    VIEW   : '../../APV/UI/Recaudacion/MantenedorBancoPrevired/IngresarBancoPrevired.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../APV/UI/Recaudacion/MantenedorBancoPrevired/IngresarBancoPrevired.js'] }],
};

BCS_VENTANA.EditarBancoPrevired = {
		TITLE : "UI_EDITAR_BANCO_PREVIRED",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "405px",
	    HEIGHT : "155px",
	    VIEW   : '../../APV/UI/Recaudacion/MantenedorBancoPrevired/EditarBancoPrevired.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../APV/UI/Recaudacion/MantenedorBancoPrevired/EditarBancoPrevired.js'] }],
};

BCS_VENTANA.GenerarInformeCostoFondo = {
		TITLE : "UI_GENERAR_INFORME_COSTO_FONDO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "400px",
	    HEIGHT : "145px",
	    VIEW   : '../../Mesa/UI/GenerarInformeCostoFondo/GenerarInformeCostoFondo.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Mesa/UI/GenerarInformeCostoFondo/GenerarInformeCostoFondo.js'] }],
};

BCS_VENTANA.GenerarInformeRiesgoFinanciero = {
	    TITLE : "UI_INFORMES_RIESGO_FINANCIERO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "425px",
	    HEIGHT : "142px",
	    VIEW   : '../../Corredor/BECB/UI/GenerarInformeRiesgoFinanciero/GenerarInformeRiesgoFinanciero.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Corredor/BECB/UI/GenerarInformeRiesgoFinanciero/GenerarInformeRiesgoFinanciero.js'] }],
	};

BCS_VENTANA.GenerarInformeVencimientosGenerales = {
		TITLE : "UI_GENERAR_INFORME_VENCIMIENTOS_GENERALES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "330px",
	    HEIGHT : "142px",
	    VIEW   :'../../Corredor/BECB/UI/GenerarInformeVencimientosGenerales/GenerarInformeVencimientosGenerales.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Corredor/BECB/UI/GenerarInformeVencimientosGenerales/GenerarInformeVencimientosGenerales.js'] }],
};

BCS_VENTANA.GenerarInformeCobroComisionesMensuales = {
		TITLE : "UI_GENERAR_INFORME_COBRO_COMISIONES_MENSUALES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "580px",
	    HEIGHT : "200px",
	    VIEW   :'../../Facturacion/UI/GenerarInformeComprobanteComisionesMensuales/GenerarInformeComprobanteComisionesMensuales.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Facturacion/UI/GenerarInformeComprobanteComisionesMensuales/GenerarInformeComprobanteComisionesMensuales.js'] }],
};


BCS_VENTANA.GenerarInformeCobroComisionesDiarias = {
	TITLE : "UI_GENERAR_INFORME_COMISIONES_DIARIAS",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "580px",
	HEIGHT : "200px",
	VIEW   :'../../Facturacion/UI/GenerarInformeComprobanteComisionesDiarias/GenerarInformeComprobanteComisionesDiarias.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Facturacion/UI/GenerarInformeComprobanteComisionesDiarias/GenerarInformeComprobanteComisionesDiarias.js'] }],
};

BCS_VENTANA.ConsultarAprobarRechazarNotificaciones = {
		TITLE : "UI_CONTROL_NOTIFICACIONES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "850px",
	    HEIGHT : "auto",
	    VIEW   : '../../Plataforma/UI/EVT/ConfirmarRechazarNotificaciones/ConsultarConfirmarRechazarNotificaciones.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Plataforma/UI/EVT/ConfirmarRechazarNotificaciones/ConsultarConfirmarRechazarNotificaciones.js',
	    												   'EVT/MantenedorEventoGrid.js',
	    												   '../../Plataforma/UI/EVT/ConfirmarRechazarNotificaciones/ConsultarDetalleEvento.js'] }],
};

BCS_VENTANA.ConsultarDetalleEventos = {
		TITLE : "UI_CONSULTAR_DETALLE_EVENTO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "600px",
	    HEIGHT : "auto",
	    VIEW   : '../../Plataforma/UI/EVT/ConfirmarRechazarNotificaciones/ConsultarDetalleEvento.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Plataforma/UI/EVT/ConfirmarRechazarNotificaciones/ConsultarDetalleEvento.js',
	    	 											   'EVT/MantenedorEventoGrid.js'] }],
};

BCS_VENTANA.MantenedorCamposFormularioObligatorios = {
		TITLE : "UI_MANTENEDOR_CAMPOS_FORMULARIO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
	    HEIGHT : "700px",
	    VIEW   : '../../Plataforma/UI/FRM/MantenedorCamposFormularioObligatorios/MantenedorCamposFormularioObligatorios.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Plataforma/UI/FRM/MantenedorCamposFormularioObligatorios/MantenedorCamposFormularioObligatorios.js'] }],
};

BCS_VENTANA.ModalDetallesDj= {
	    TITLE : "UI_COMPLEMENTOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "800px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/Declaraciones/ModalDetalles.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Normativa/UI/Declaraciones/ModalDetalles.js',
 															 '../../Normativa/UI/Declaraciones/PanelesModalDetalles/PanelCalendarioPagos.js',
															 '../../Normativa/UI/Declaraciones/PanelesModalDetalles/PanelPagosPeriodo.js',
															 '../../Normativa/UI/Declaraciones/PanelesModalDetalles/PanelGarantias.js',
															 '../../Normativa/UI/Declaraciones/PanelesModalDetalles/PanelTipoActivoGarantia.js',
															 '../../Normativa/UI/Declaraciones/PanelesModalDetalles/PanelesModalDetallesGridConfig.js']} ],
};

BCS_VENTANA.agregarModificarCalendarioPagos = {
    TITLE : "UI_CALENDARIO_PAGOS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "500px",
    HEIGHT : "auto",
    VIEW   : '../../Normativa/UI/Declaraciones/PanelesModalDetalles/ModalCalendarioPagos.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Normativa/UI/Declaraciones/PanelesModalDetalles/ModalCalendarioPagos.js']}],

};

BCS_VENTANA.agregarModificarPagosPeriodo = {
    TITLE : "UI_PAGOS_PERIODO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "500px",
    HEIGHT : "auto",
    VIEW   : '../../Normativa/UI/Declaraciones/PanelesModalDetalles/ModalPagosPeriodo.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Normativa/UI/Declaraciones/PanelesModalDetalles/ModalPagosPeriodo.js']}],

};

BCS_VENTANA.agregarModificarGarantias = {
    TITLE : "UI_GARANTIAS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "500px",
    HEIGHT : "auto",
    VIEW   : '../../Normativa/UI/Declaraciones/PanelesModalDetalles/ModalGarantias.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Normativa/UI/Declaraciones/PanelesModalDetalles/ModalGarantias.js']}],

};

BCS_VENTANA.agregarModificarTipoActivoGarantia = {
    TITLE : "UI_TIPO_ACTIVO_GARANTIA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "500px",
    HEIGHT : "auto",
    VIEW   : '../../Normativa/UI/Declaraciones/PanelesModalDetalles/ModalTipoActivoGarantias.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Normativa/UI/Declaraciones/PanelesModalDetalles/ModalTipoActivoGarantias.js']}],

};

BCS_VENTANA.AgregarCampoFormularioObligatorio = {
		TITLE : "UI_AGREGAR_CAMPO_FORMULARIO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "580px",
	    HEIGHT : "210px",
	    VIEW   : '../../Plataforma/UI/FRM/MantenedorCamposFormularioObligatorios/AgregarCampoFormularioObligatorio.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Plataforma/UI/FRM/MantenedorCamposFormularioObligatorios/AgregarCampoFormularioObligatorio.js'] }],
};

BCS_VENTANA.EditarCampoFormularioObligatorio = {
		TITLE : "UI_EDITAR_CAMPO_FORMULARIO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "580px",
	    HEIGHT : "227px",
	    VIEW   : '../../Plataforma/UI/FRM/MantenedorCamposFormularioObligatorios/EditarCampoFormularioObligatorio.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Plataforma/UI/FRM/MantenedorCamposFormularioObligatorios/EditarCampoFormularioObligatorio.js'] }],
};

BCS_VENTANA.agregarModificarEnrolamientoTarjetaBancaria = {
    TITLE : "UI_TARJETA_BANCARIA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "650px",
    HEIGHT : "250px",
    VIEW   : '../../SocioNegocio/UI/ENR/PER/ModalTarjetaBancaria.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/ENR/PER/ModalTarjetaBancaria.js']}],

};

BCS_VENTANA.agregarModificarEnrolamientoRelacion = {
    TITLE : "UI_RELACION",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "500px",
    HEIGHT : "316px",
    VIEW   : '../../SocioNegocio/UI/ENR/PER/ModalRelacion.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/ENR/PER/ModalRelacion.js']}],

};

BCS_VENTANA.agregarModificarEnrolamientoFatcaPaisResidencia = {
    TITLE : "UI_PAIS_RESIDENCIA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "500px",
    HEIGHT : "200px",
    VIEW   : '../../SocioNegocio/UI/ENR/PER/ModalFatcaPaisResidencia.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/ENR/PER/ModalFatcaPaisResidencia.js']}],
};

BCS_VENTANA.ventanaGrillaRequisitosEnrolamientoFull = {
	TITLE : "UI_REQUISITOS_ENROLAMIENTO_FULL",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "600px",
	HEIGHT : "400px",
	VIEW   : '../../SocioNegocio/UI/ventanaGrillaRequisitosEnrolamientoFull.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/ventanaGrillaRequisitosEnrolamientoFull.js']}],
};

BCS_VENTANA.MantenedorPortafolio = {
	TITLE : "UI_MANTENEDOR_PORTAFOLIO",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "600px",
	HEIGHT : "auto",
	VIEW   : '../../Portafolio/UI/MantenedorPortafolio/MantenedorPortafolio.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Portafolio/UI/MantenedorPortafolio/MantenedorPortafolio.js'] }],
};

BCS_VENTANA.CrearPortafolioMantenedor = {
	TITLE : "UI_AGREGAR_PORTAFOLIO_MANTENEDOR",
	IMAGE : "",
	ENABLE : true,
	WIDTH : "450px",
	HEIGHT : "auto",
	VIEW : '../../Portafolio/UI/MantenedorPortafolio/CrearPortafolioMantenedor.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Portafolio/UI/MantenedorPortafolio/CrearPortafolioMantenedor.js'] }],
	
};

BCS_VENTANA.MantenedorMotivos = {
	    TITLE : "UI_MANTENEDOR_MOTIVOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "750px",
	    HEIGHT : "410px",
	    VIEW   : '../../Plataforma/UI/PLA/MantenedorMotivos/MantenedorMotivos.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['PLA/MantenedorMotivos/MantenedorMotivos.js' ] } ],
		
};

BCS_VENTANA.ModalMantenedorMotivos = {
	    TITLE : "UI_MODAL_MANTENEDOR_MOTIVOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "480px",
	    HEIGHT : "210px",
	    VIEW   : '../../Plataforma/UI/PLA/MantenedorMotivos/ModalMantenedorMotivos.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: ['PLA/MantenedorMotivos/ModalMantenedorMotivos.js' ] } ],
		
};

BCS_VENTANA.BlotterCuadraturaCircular1518 = {
	    TITLE : "UI_CUADRATURA_CIRCULAR1518",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1000px",
    	HEIGHT : "637px",
	    VIEW   : '../../Custodia/UI/ConsultarCuadraturaCircular1518/ConsultarCuadraturaCircular1518.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/ConsultarCuadraturaCircular1518/ConsultarCuadraturaCircular1518.js']}],
};

BCS_VENTANA.CargarArchivoCircular1518 = {
		TITLE : "UI_CARGAR_ARCHIVO_CIRCULAR1518",
		IMAGE  : "",
		ENABLE : true,
		WIDTH  : "590px",
		HEIGHT : "290px",											
		VIEW   : '../../Custodia/UI/ConsultarCuadraturaCircular1518/CargaArchivosCircular1518/CargarArchivos.html',
		PARAMS : {},
		DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Custodia/UI/ConsultarCuadraturaCircular1518/CargaArchivosCircular1518/CargarArchivos.js']}],
};

BCS_VENTANA.GenerarContrato = {
    TITLE : "UI_SELECCIONAR_APODERADO",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "600px",
    HEIGHT : "170px",
    VIEW   : '../../Mesa/UI/ConsultaDeOperaciones/ConsultarMovimientos/ModalGenerarContrato.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../Mesa/UI/ConsultaDeOperaciones/ConsultarMovimientos/ModalGenerarContrato.js']}],
};

BCS_VENTANA.AbrirModuloMesa= {
	TITLE : "UI_ABRIR_MODULO_MESA",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "500",
	HEIGHT : "300px",
	VIEW   : '../../Plataforma/UI/PAU/AperturaCierreModulos/AbrirModuloMesa.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Plataforma/UI/PAU/AperturaCierreModulos/AbrirModuloMesa.js'
													   ] }],
};
BCS_VENTANA.NotificacionCircular1985= {
	TITLE : "UI_NOTIFICACION_CIRCULAR_1985",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "1000px",
	HEIGHT : "780px",
	VIEW   : '../../Normativa/UI/NotificacionCircular1985/NotificacionCircular1985.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/NotificacionCircular1985/NotificacionCircular1985.js',
													   '../../Normativa/UI/NotificacionCircular1985/PanelNomina.js'] }],
};

BCS_VENTANA.ReporteMensualOperaciones= {
	    TITLE : "UI_REPORTE_MENSUAL_OPERACIONES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "410px",
	    HEIGHT : "auto",
	    VIEW   : '../../Mesa/UI/SPT/ReporteMensualOperaciones/ReporteMensualOperaciones.html',
	    PARAMS : {idTipoProducto: 'MEME'},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/SPT/ReporteMensualOperaciones/ReporteMensualOperaciones.js' ]} ],

};

BCS_VENTANA.InformeSaldosCuentaCambio= {
	TITLE : "UI_INFORME_SALDOS_CUENTA_CAMBIO",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "410px",
	HEIGHT : "auto",
	VIEW   : '../../Mesa/UI/SPT/InformeSaldosCuentaCambio/InformeSaldosCuentaCambio.html',
	PARAMS : {idTipoProducto: 'MEME'},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Mesa/UI/SPT/InformeSaldosCuentaCambio/InformeSaldosCuentaCambio.js' ]} ],

};

BCS_VENTANA.MantenedorMatrizPerfilComercialAdicional= {
	TITLE : "UI_MATRIZ_CONFIGURACION_ENROLAMIENTO",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "1100px",
	HEIGHT : "500px",
	VIEW   : '../../Plataforma/UI/COM/MantenedorMatrizPerfilComercialAdicional/MantenedorMatrizPerfilComercialAdicional.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Plataforma/UI/COM/MantenedorMatrizPerfilComercialAdicional/MantenedorMatrizPerfilComercialAdicional.js' ]} ],

};

BCS_VENTANA.AgregarModificarConfiguracionPerfilComercialAdicional= {
	TITLE : "UI_AGREGAR_CONFIGURACION_ENROLAMIENTO",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "600px",
	HEIGHT : "260px",
	VIEW   : '../../Plataforma/UI/COM/MantenedorMatrizPerfilComercialAdicional/AgregarModificarConfiguracionPerfilComercialAdicional.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Plataforma/UI/COM/MantenedorMatrizPerfilComercialAdicional/AgregarModificarConfiguracionPerfilComercialAdicional.js' ]} ],

};

/*BCS_VENTANA.CargaMasivaInstrucciones = {
	    TITLE  : "UI_CARGA_MASIVA_INSTRUCCIONES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "500px",    
	    VIEW   : '../../Orden/UI/BlotterCargaMasiva/BlotterCargaMasiva.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, 
	    	files: [ '../../Orden/UI/BlotterCargaMasiva/BlotterCargaMasiva.js',
	    	         '../../Orden/UI/BlotterCargaMasiva/BlotterCargaMasiva.js'
	    	         ] 
		}],

};*/

BCS_VENTANA.CargaMasivaSocioNegocio = {
	    TITLE : "UI_CARGA_MASIVA_SOCIO_NEGOCIO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "770px",
    	HEIGHT : "auto",
	    VIEW   : '../../SocioNegocio/UI/CargaMasiva/CargaMasivaSocioNegocio.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../SocioNegocio/UI/CargaMasiva/CargaMasivaSocioNegocio.js',
	                                                         '../../SocioNegocio/UI/CargaMasiva/CargaMasivaSocioNegocioGrid.js']}],
};

BCS_VENTANA.VerFormatoArchivo = {
	    TITLE : "UI_FORMATO_ARCHIVO_CARGA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "700px",
	    VIEW   : 'PLA/CargaArchivosGeneral/formatoArchivo.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ 'PLA/CargaArchivosGeneral/formatoArchivo.js'] } ],
		MODULO : "PLA",
};

BCS_VENTANA.DetalleCargaMasivaSocioNegocio = {
	    TITLE  : "UI_DETALLE_CARGA_MASIVA",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "500px",    
	    VIEW   : '../../SocioNegocio/UI/CargaMasiva/DetalleCargaMasiva/DetalleCargaMasivaSocioNegocio.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, 
	    	files: [ '../../SocioNegocio/UI/CargaMasiva/DetalleCargaMasiva/DetalleCargaMasivaSocioNegocio.js',
	    	         '../../SocioNegocio/UI/CargaMasiva/CargaMasivaSocioNegocioGrid.js']
		}],
};

//MANTENEDOR REPORTE MENSUAL OPERACIONES
BCS_VENTANA.VisorReporteMensualOperaciones = {
	    TITLE : "Reporte Mensual de Operaciones y Saldos",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1200px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/MantenedorReporteMensualOperaciones/GridViewRMO.html',
	    PARAMS : {IdEntidadReguladora:"RMO"},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/MantenedorReporteMensualOperaciones/GridControllerRMO.js'] }],

};

BCS_VENTANA.MantenedorOperacionesRMO = {
	    TITLE : "Mantenedor Operaciones",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "440px",
	    VIEW   : '../../Normativa/UI/MantenedorReporteMensualOperaciones/DetailViewRMO.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/MantenedorReporteMensualOperaciones/DetailControllerRMO.js'] }],

};

BCS_VENTANA.CargadorDatosRMO = {
	    TITLE : "Cargador Datos",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "340px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/MantenedorReporteMensualOperaciones/LoadFormRMO.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/MantenedorReporteMensualOperaciones/LoadControllerRMO.js'] }],

};

BCS_VENTANA.ExportadorDatosRMO = {
	    TITLE : "Exportador Datos",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "340px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/MantenedorReporteMensualOperaciones/ExportFormRMO.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/MantenedorReporteMensualOperaciones/ExportControllerRMO.js'] }],

};

BCS_VENTANA.GenerarDeclaracionRMO = {
	    TITLE : "Generar Reporte",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "280px",
	    HEIGHT : "170px",
	    VIEW   : '../../Normativa/UI/MantenedorReporteMensualOperaciones/GenFormRMO.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/MantenedorReporteMensualOperaciones/GenControllerRMO.js'] }],
};

BCS_VENTANA.CuadraturaDatosRMO = {
	    TITLE : "Validador de Datos",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "300px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/MantenedorReporteMensualOperaciones/QuadFormRMO.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/MantenedorReporteMensualOperaciones/QuadControllerRMO.js'] }],

};

BCS_VENTANA.ReporteMensualOperacionesComplementos = {
	    TITLE : "UI_COMPLEMENTOS",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/MantenedorReporteMensualOperaciones/Maestros/Complementos/ComplementosMainRMO.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/MantenedorReporteMensualOperaciones/Maestros/Complementos/ComplementosMainRMO.js',
	                                                       '../../Normativa/UI/MantenedorReporteMensualOperaciones/Maestros/Complementos/ReporteSaldoCuentasCambio.js',
	                                                       '../../Normativa/UI/MantenedorReporteMensualOperaciones/Maestros/MaestroRMOConfiguracionGrid.js'] }],
};

BCS_VENTANA.ConciliacionMasivaPrevired = {
	    TITLE  : "UI_FOLIOS_RECAUDACION_PREVIRED",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "500px",
	    HEIGHT : "auto",    
	    VIEW   : '../../APV/UI/Recaudacion/GestionDeRecaudacion/ModalConciliacionMasivaPrevired.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, 
	    	files: [ '../../APV/UI/Recaudacion/GestionDeRecaudacion/ModalConciliacionMasivaPrevired.js']
		}],
};

BCS_VENTANA.BlotterCobroComision = {
	    TITLE  : "UI_COBRO_COMISIONES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
	    HEIGHT : "auto",    
	    VIEW   : '../../CuentaInversion/UI/BlotterCobroComisiones/BlotterCobroComisiones.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, 
	    	files: [ '../../CuentaInversion/UI/BlotterCobroComisiones/BlotterCobroComisiones.js' ] 
		}],
};

BCS_VENTANA.ParametrosCobroComision = {
	    TITLE  : "UI_PARAMETROS_COBRO_COMISIONES",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "900px",
	    HEIGHT : "auto",    
	    VIEW   : '../../CuentaInversion/UI/BlotterCobroComisiones/ParametrosCobroComisiones/ParametrosCobroComisiones.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, 
	    	files: [ 
					'../../CuentaInversion/UI/BlotterCobroComisiones/ParametrosCobroComisiones/ParametrosCobroComisiones.js',
	    			'../../CuentaInversion/UI/BlotterCobroComisiones/ParametrosCobroComisiones/PanelCobroComisionConceptos.js',
	    			'../../CuentaInversion/UI/BlotterCobroComisiones/ParametrosCobroComisiones/PanelCobroComisionOpcionesCobros.js'
	    	 ] 
		}],
};

BCS_VENTANA.AgregarCobroComisionExclusionConcepto = {
	    TITLE  : "UI_AGREGAR_EXCLUSION_CONCEPTO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "850px",
	    HEIGHT : "auto",    
	    VIEW   : '../../CuentaInversion/UI/BlotterCobroComisiones/ParametrosCobroComisiones/ModalCobroComisionConceptos.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, 
	    	files: [ 
					'../../CuentaInversion/UI/BlotterCobroComisiones/ParametrosCobroComisiones/ModalCobroComisionConceptos.js'
	    	 ] 
		}],
};

BCS_VENTANA.AgregarOpcionesCobroComision= {
	    TITLE  : "UI_AGREGAR_OPCION_COBRO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "450px",
	    HEIGHT : "auto",    
	    VIEW   : '../../CuentaInversion/UI/BlotterCobroComisiones/ParametrosCobroComisiones/ModalCobroComisionOpcionesCobros.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, 
	    	files: [ 
					'../../CuentaInversion/UI/BlotterCobroComisiones/ParametrosCobroComisiones/ModalCobroComisionOpcionesCobros.js'
	    	 ] 
		}],
};

BCS_VENTANA.EditarRegistrosCobroComision= {
	    TITLE  : "UI_EDITAR_COBRO_COMISION",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "450px",
	    HEIGHT : "auto",    
	    VIEW   : '../../CuentaInversion/UI/BlotterCobroComisiones/ModalCobroComisionEditarRegistro.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, 
	    	files: [ 
					'../../CuentaInversion/UI/BlotterCobroComisiones/ModalCobroComisionEditarRegistro.js'
	    	 ] 
		}],
};

BCS_VENTANA.ModalPaisResidenciaFiscal = {
    TITLE : "UI_PAIS_RESIDENCIA_FISCAL_CRS",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "650px",
    HEIGHT : "auto",
    VIEW   : '../../SocioNegocio/UI/PER/ModalCrsPaisResidenciaFiscal.html',
    PARAMS : {},
    DEPENDENCIES : [{ type: "js",  cache: true, files: ['../../SocioNegocio/UI/PER/ModalCrsPaisResidenciaFiscal.js']}],

};


BCS_VENTANA.InformeSimultaneaCuentaPropia = {
    TITLE : "UI_INFORME_SIMULTANEA_CUENTA_PROPIA",
    IMAGE  : "",
    ENABLE : true,
    WIDTH  : "450px",
    HEIGHT : "auto",
    VIEW   : '../../Custodia/UI/InformeSimultaneaCuentaPropia/InformeSimultaneaCuentaPropia.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Custodia/UI/InformeSimultaneaCuentaPropia/InformeSimultaneaCuentaPropia.js'] }],

};

BCS_VENTANA.MantenedorConceptosClientesRelacionados = {
	TITLE : "UI_MANTENEDOR_CONCEPTOS_CLIENTES_RELACIONADOS",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "400px",
	HEIGHT : "auto",
	VIEW   : '../../Contabilidad/UI/MantenedorConceptosClientesRelacionados/MantenedorConceptosClientesRelacionados.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Contabilidad/UI/MantenedorConceptosClientesRelacionados/MantenedorConceptosClientesRelacionados.js'] } ],
};

BCS_VENTANA.ConfiguracionConceptosClientesRelacionados = {
	TITLE : "UI_MANTENEDOR_CONCEPTOS_CLIENTES_RELACIONADOS",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "810px",
	HEIGHT : "auto",
	VIEW   : '../../Contabilidad/UI/MantenedorConceptosClientesRelacionados/ConfiguracionConceptosClientesRelacionados.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Contabilidad/UI/MantenedorConceptosClientesRelacionados/ConfiguracionConceptosClientesRelacionados.js'] } ],
};


BCS_VENTANA.InformeClientesRelacionados= {
	TITLE : "UI_INFORME_CLIENTES_RELACIONADOS",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "450px",
	HEIGHT : "auto",
	VIEW   : '../../Contabilidad/UI/Informes/InformeClientesRelacionados.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Contabilidad/UI/Informes/InformeClientesRelacionados.js'] } ],
};

BCS_VENTANA.ExclusionLimpiezaLineaOperacional = {
	    TITLE : "UI_EXCLUSION_LIMPIEZA_LINEA_OPERACIONAL",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "770px",
    	HEIGHT : "auto",
	    VIEW   : '../../SocioNegocio/UI/ExclusionLimpiezaLineaOperacional/ExclusionLimpiezaLineaOperacional.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../SocioNegocio/UI/ExclusionLimpiezaLineaOperacional/ExclusionLimpiezaLineaOperacional.js',
	                                                         '../../SocioNegocio/UI/ExclusionLimpiezaLineaOperacional/ExclusionLimpiezaLineaOperacionalGrid.js']}],
};

BCS_VENTANA.AsignacionMasivaPrecioPromedio = {
	    TITLE : "UI_ASIGNACION_MASIVA_PRECIO_PROMEDIO",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "770px",
    	HEIGHT : "auto",
	    VIEW   : '../../Orden/UI/AsignacionMasivaPrecioPromedio/AsignacionMasivaPrecioPromedio.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/AsignacionMasivaPrecioPromedio/AsignacionMasivaPrecioPromedio.js']}]
};

BCS_VENTANA.AsignacionMasivaPrecioPromedioDetalle = {
	    TITLE : "UI_ASIGNACION_MASIVA_PRECIO_PROMEDIO_VER_DETALLE",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1100px",
    	HEIGHT : "auto",
	    VIEW   : '../../Orden/UI/AsignacionMasivaPrecioPromedio/AsignacionMasivaPrecioPromedioDetalle.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Orden/UI/AsignacionMasivaPrecioPromedio/AsignacionMasivaPrecioPromedioDetalle.js']}]
};

BCS_VENTANA.ConsultaUsoGlosaTipoInstrumento = {
	TITLE : "UI_CONSULTA_USO_GLOSA_TIPO_INSTRUMENTO",
	IMAGE  : "",
	ENABLE : true,
	WIDTH  : "800px",
	HEIGHT : "500px",
	VIEW   : '../../Plataforma/UI/PLA/MantenedorGlosaTipoInstrumento/ConsultaUsoGlosaTipoInstrumento.html',
	PARAMS : {},
	DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Plataforma/UI/PLA/MantenedorGlosaTipoInstrumento/ConsultaUsoGlosaTipoInstrumento.js'] } ],
};

BCS_VENTANA.MantenedorReporteAPV = {
	    TITLE : "UI_MANTENEDOR_REPORTE_APV",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "1200px",
	    HEIGHT : "700px",
	    VIEW   : '../../Normativa/UI/MantenedorReporteAPV/MantenedorReporteAPV.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js",  cache: true, files: [ '../../Normativa/UI/MantenedorReporteAPV/MantenedorReporteAPV.js']}]
};

BCS_VENTANA.ModificarReporteAPV = {
            TITLE : "UI_MODIFICAR_REPORTE_APV",
	    IMAGE  : "",
	    ENABLE : true,
	    WIDTH  : "565px",
	    HEIGHT : "auto",
	    VIEW   : '../../Normativa/UI/MantenedorReporteAPV/ModificarReporteAPV.html',
	    PARAMS : {},
	    DEPENDENCIES : [{ type: "js", cache: true, files: ['../../Normativa/UI/MantenedorReporteAPV/ModificarReporteAPV.js'] }],
};