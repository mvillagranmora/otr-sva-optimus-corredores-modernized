

SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/

-- DELETE FROM  "BSCL.Plataforma.BD::PLA.Servicio"; --JG. NO DESCOMENTAR AL MENOS QUE SEA REQUERIDO.


/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(1,'BSCL.Plataforma.BD.Servicios::ConsultarFechaLiquidacion',15,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(2,'BSCL.Plataforma.BD.Servicios::ConsultarFechaHoraSistema',65,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(3,'BSCL.Custodia.BD.Servicios::IngresoManualTitulos',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(4,'BSCL.Normativa.BD.Servicios::TRI.DJ1891.GrabarMasivo',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(5,'BSCL.Normativa.BD.Servicios::TRI.DJ1885.GrabarMasivo',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(7,'BSCL.CuentaInversion.BD.Servicios::CrearCargoAbono',2,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(9,'BSCL.CuentaInversion.BD.Servicios::ConsultarCartolaDeMovimiento',0,'P','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(11,'BSCL.Custodia.BD.Servicios::CertificarMovimientoCarteraEnTransitoMasivo',0,'P','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")  
VALUES(13,'BSCL.Facturacion.BD.Servicios::CrearComprobante',3,'P',' ', 'P_MOVIMIENTOFACTURAR=BSCL.Facturacion.BD::GBL.CrearComprobante_IN_MovimientoFacturar', 'P_OUT_COMPROBANTEGENERADO=BSCL.Facturacion.BD::GBL.CrearComprobante_OUT_ComprobanteGenerado;P_OUT_CARGOABONOGENERADO=BSCL.Facturacion.BD::GBL.CrearComprobante_OUT_CargoAbonoGenerado;P_OUT_INGRESOEGRESOGENERADO=BSCL.Facturacion.BD::GBL.CrearComprobante_OUT_IngresoEgresoGenerado;P_OUT_IngresosRealizados=BSCL.Facturacion.BD::GBL.CrearComprobante_OUT_IngresosRealizados;P_OUT_RETIROSREALIZADOS=BSCL.Facturacion.BD::GBL.CrearComprobante_OUT_RetirosRealizados;P_OUT_INFORMACIONDETALLE=BSCL.Facturacion.BD::GBL.CrearComprobante_OUT_InformacionDetalle;P_OUT_RESULTADOPROCESO=BSCL.Facturacion.BD::GBL.CrearComprobante_OUT_ResultadoProceso;P_OUT_ADVERTENCIASDETALLE=BSCL.Facturacion.BD::GBL.CrearComprobante_OUT_AdvertenciasDetalle;P_OUT_ERRORESDETALLE=BSCL.Facturacion.BD::GBL.CrearComprobante_OUT_ErroresDetalle');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(14,'BSCL.Plataforma.BD.Utilitarios::ObtenerFechas',271,'P',' ');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(15,'BSCL.Custodia.BD.Servicios::AnularMovimientoCarteraEnTransitoMasivo',0,'P',' ');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(16,'BSCL.Facturacion.BD.Servicios::ModificarEstadoMovimientosAFacturar',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(17,'BSCL.Plataforma.BD.Servicios::BolsaInstrumentoPreciosActual',16,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(18,'BSCL/Orden/XSODATA/Ordenes.xsodata/Ordenes',8,'O','servicio de consulta de ordenes en odata');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(19,'BSCL.Custodia.BD.Servicios::ConsultarFlujoInstrumento',1,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(21,'BSCL.CuentaInversion.BD.Servicios::ConsultarSaldoCuentaInversion',3,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(22,'BSCL.Custodia.BD.Servicios::ConsultarCustodiaPortafolio',16,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(23,'BSCL.Plataforma.BD.Servicios::ConsultarDiaSiguiente',0,'P',' ');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(24,'BSCL.Custodia.BD.Servicios::ConsultarCustodiaSubyacente',0,'P',' ');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(25,'BSCL.Plataforma.BD.Servicios::CrearExigibilidad',0,'P',' ');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(26,'BSCL.Plataforma.BD.Servicios::ConsultarFlujoCaja',0,'P',' ');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(27,'BSCL.Plataforma.BD.Servicios::LIM.ConsultarTipoLimite',0,'P',' ');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(28,'BSCL.Custodia.BD.Servicios::GTA.ConsultarCustodiaParaGarantia',0,'P',' ');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(29,'BSCL.Plataforma.BD.Servicios::CrearCoberturaGarantia',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(30,'BSCL.Plataforma.BD.Servicios::CalcularExigibilidad',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(31,'BSCL.Plataforma.BD.Servicios::CalcularCobertura',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(32,'BSCL.Plataforma.BD.Servicios::ProcesarAtributoMantenedorDatoAsociado',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(33,'BSCL.Plataforma.BD.Servicios::LIM.ConsultarLineaRiesgo',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(34,'BSCL.Plataforma.BD.Servicios::ModificarCoberturaGarantia',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(35,'BSCL.Custodia.BD.Servicios::GTA.IngresarGarantiasUI',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(36,'BSCL.Normativa.BD.Servicios::GuardarDocumento',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(37,'BSCL.Plataforma.BD.Servicios::ModificarAtributoMantenedorDato',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(38,'BSCL.Plataforma.BD.Servicios::COM.ConsultarCobrosProducto',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(39,'BSCL.Plataforma.BD.Servicios::LIM.CrearLimite',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(40,'BSCL.Custodia.BD.Servicios::GTA.RetirarGarantiasUI',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(41,'BSCL.Plataforma.BD.Servicios::ModificarEstadoEventoAutorizacion',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(43,'BSCL.Plataforma.BD.Servicios::EliminarAtributoMantenedorDato',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(44,'BSCL.Plataforma.BD.Servicios::COM.CrearPerfilCobros',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(45,'BSCL.Plataforma.BD.Servicios::COM.CrearPerfilProductos',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(46,'BSCL.Plataforma.BD.Servicios::COM.CrearPerfilComercial',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(47,'BSCL.Plataforma.BD.Servicios::ConsultarFechaVencimiento',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(48,'BSCL.Custodia.BD.Servicios::SIM.CalculoInteresesDeSimultanea',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(49,'BSCL.Plataforma.BD.Servicios::CrearEtapaAutorizacion',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(50,'BSCL.Plataforma.BD.Servicios::CrearAutorizadoEtapa',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(51,'BSCL.Custodia.BD.Servicios::SIM.AnticipoSimultanea',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(52,'BSCL.SocioNegocio.BD.Servicios::CLI.CrearBorradorCliente',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(53,'BSCL.SocioNegocio.BD.Servicios::CLI.ConsultarEntidadEnrolamiento',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(54,'BSCL.SocioNegocio.BD.Servicios::CLI.CrearCliente',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(55,'BSCL.Tesoreria.BD.Servicios::CrearComprobante',0,'P',' ');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(56,'BSCL.Custodia.BD.Servicios::SIM.VencimientoSimultanea',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(57,'BSCL.Portafolio.BD.Servicios::ModificarPortafolio',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(58,'BSCL.Portafolio.BD.Servicios::ConfirmarModificarPortafolio',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(59,'BSCL.Portafolio.BD.Servicios::CrearPortafolio',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(60,'BSCL.Plataforma.BD.Servicios::ConsultarPlazo',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(61,'BSCL.Tesoreria.BD.Servicios::CrearIngresoEgreso',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(62,'BSCL.Custodia.BD.Servicios::PRT.CrearPrestamo',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(63,'BSCL.Plataforma.BD.Servicios::IND.ConsultarIndicador',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(64,'BSCL.Custodia.BD.Servicios::PRT.CrearEmprestito',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(65,'BSCL.Plataforma.BD.Servicios::IND.ModificarIndicador',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(67,'BSCL.Plataforma.BD.Servicios::PLA.InsertarDiccionarioMasivo',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(68,'BSCL.SocioNegocio.BD.Servicios::PER.ModificarPersonaDatosGenerales',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(69,'BSCL.Normativa.BD.Servicios::TRI.DJ1891.GrabarDetalle',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(70,'BSCL.Normativa.BD.Servicios::TRI.DJ1891.EliminarDetalles',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(71,'BSCL.Plataforma.BD.Servicios::CalcularMontoPorTipoInstrumento',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(72,'BSCL.SocioNegocio.BD.Servicios::PER.CrearPersonaDocumentoIdentidad',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(73,'BSCL.Plataforma.BD.Servicios::DOC.FirmarAnexo',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(74,'BSCL.SocioNegocio.BD.Servicios::PER.ModificarPersonaDocumentosIdentidad',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(75,'BSCL.SocioNegocio.BD.Servicios::PER.CrearPersonaCuentaBancaria',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(76,'BSCL.SocioNegocio.BD.Servicios::PER.ModificarPersonaCuentaBancaria',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(77,'BSCL.Custodia.BD.Servicios::PRT.AnticiparPrestamo',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(78,'BSCL.Custodia.BD.Servicios::PRT.AnticiparEmprestito',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(79,'BSCL.SocioNegocio.BD.Servicios::PER.CrearPersonaTelefono',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(80,'BSCL.SocioNegocio.BD.Servicios::PER.ModificarPersonaTelefono',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(81,'BSCL.SocioNegocio.BD.Servicios::PER.CrearPersonaDireccionFisica',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(82,'BSCL.SocioNegocio.BD.Servicios::PER.ModificarPersonaDireccionFisica',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(83,'BSCL.SocioNegocio.BD.Servicios::PER.CrearPersonaDireccionElectronica',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(84,'BSCL.SocioNegocio.BD.Servicios::PER.ModificarPersonaDireccionElectronica',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(85,'BSCL.Custodia.BD.Servicios::PRT.AnularPrestamo',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(86,'BSCL.Custodia.BD.Servicios::PRT.AnularEmprestito',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(87,'BSCL.Plataforma.BD.Servicios::COM.ModificarPerfilCobros',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(88,'BSCL.Custodia.BD.Servicios::PRT.VencerPrestamo',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(89,'BSCL.Tesoreria.BD.Servicios::LiquidarComprobantes',0,'P',' ');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(90,'BSCL.SocioNegocio.BD.Servicios::PER.CrearPersona',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(91,'BSCL.Tesoreria.BD.Servicios::CrearComprobanteFormaDePago',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(92,'BSCL.SocioNegocio.BD.Servicios::PER.CrearPersonaTarjetaBancaria',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(93,'BSCL.SocioNegocio.BD.Servicios::PER.ModificarPersonaTarjetaBancaria',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(94,'BSCL.Normativa.BD.Servicios::TRI.DJ1891.ObtenerTotales',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(95,'BSCL.SocioNegocio.BD.Servicios::CLI.ModificarClienteDatosGenerales',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(96,'BSCL.SocioNegocio.BD.Servicios::CLI.ModificarEstadoCliente',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(97,'BSCL.Plataforma.BD.Servicios::COM.CrearGrupoActivos',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(98,'BSCL.SocioNegocio.BD.Servicios::EMP.ModificarEmpleadoDatosGenerales',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(99,'BSCL.Orden.BD.Servicios::OfertarOrdenes',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(100,'BSCL.SocioNegocio.BD.Servicios::PER.ModificarEstadoPersonaDocumentosIdentidad',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(101,'BSCL.SocioNegocio.BD.Servicios::EMP.CrearEmpleado',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(102,'BSCL.Tesoreria.BD.Servicios::LiquidarFormasDePago',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(103,'BSCL.SocioNegocio.BD.Servicios::EMP.ModificarEstadoEmpleado',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(104,'BSCL.SocioNegocio.BD.Servicios::CNT.CrearContraparte',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(105,'BSCL.Orden.BD.Servicios::ORD.ActualizarEstadoLibroAsignacionAutomatica',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(106,'BSCL.Orden.BD.Servicios::ORD.DesactivarAsignacionAutomaticaLibro',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(107,'BSCL.Orden.BD.Servicios::RetirarOfertasOrdenes',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(108,'BSCL.SocioNegocio.BD.Servicios::PER.ModificarEstadoPersona',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(109,'BSCL.Orden.BD.Servicios::AnularAsignacionMasiva',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(110,'BSCL.SocioNegocio.BD.Servicios::CNT.ModificarContraparteDatosGenerales',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(111,'BSCL.SocioNegocio.BD.Servicios::CNT.ModificarEstadoContraparte',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(112,'BSCL.Plataforma.BD.Servicios::ACC.CrearUsuario',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(113,'BSCL.Orden.BD.Servicios::AnularTransaccionMasiva',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(114,'BSCL.Orden.BD.Servicios::AnularOrdenMasiva',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(115,'BSCL.SocioNegocio.BD.Servicios::PER.ModificarEstadoPersonaTarjetaBancaria',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(116,'BSCL.SocioNegocio.BD.Servicios::PER.ModificarEstadoPersonaCuentaBancaria',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(117,'BSCL.SocioNegocio.BD.Servicios::PRO.CrearProveedor',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(118,'BSCL.SocioNegocio.BD.Servicios::PRO.ModificarProveedorDatosGenerales',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(119,'BSCL.SocioNegocio.BD.Servicios::PRO.ModificarEstadoProveedor',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(120,'BSCL.Plataforma.BD.Servicios::ACC.AgregarRolUsuario',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(121,'BSCL.Plataforma.BD.Servicios::ACC.EliminarRolUsuario',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(122,'BSCL.Plataforma.BD.Servicios::IND.AgregarTipoIndicador',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(123,'BSCL.Plataforma.BD.Servicios::IND.ModificarTipoIndicador',0,'P','');
-- 124,125 Disponible!
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(126,'BSCL.Plataforma.BD.Servicios::ACC.CrearRol',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(127,'BSCL.Tesoreria.BD.Servicios::CrearCaja',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(128,'BSCL.Plataforma.BD.Servicios::ACC.AsignarPerfilRol',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(129,'BSCL.Plataforma.BD.Servicios::ACC.EliminarPerfilRol',0,'P','');;
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(130,'BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacion',0,'P','');;
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(131,'BSCL.Tesoreria.BD.Servicios::CrearCajaMoneda',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(132,'BSCL.Plataforma.BD.Servicios::ACC.ActualizarRol',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(133,'BSCL.Tesoreria.BD.Servicios::BloquearDesbloquearRegistros',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(134,'BSCL.Plataforma.BD.Servicios::IND.AgregarDetalleIndicador',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(135,'BSCL.Tesoreria.BD.Servicios::CrearCajaUsuario',0,'P','');
--136 disponible
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(137,'BSCL.Plataforma.BD.Servicios::COM.CalcularNetoYCobrosOperacion',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(138,'BSCL.Plataforma.BD.Servicios::ACC.CrearPerfil',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
values (140,'BSCL.Orden.BD.Servicios::ProgramarAsignacionCronologicaAutomatica',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
values (141,'BSCL.Tesoreria.BD.Servicios::CertificarMovimientosTesoreria',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
values (142,'BSCL.Tesoreria.BD.Servicios::AnularMovimientosTesoreria',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(143,'BSCL.Plataforma.BD.Servicios::COM.CrearPlantillaEmpleado',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(144,'BSCL.Tesoreria.BD.Servicios::ModificarMantenedorCaja',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(145,'BSCL.Facturacion.BD.Servicios::AnularMovimientoComprobante',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(146,'BSCL.Tesoreria.BD.Servicios::CrearCuentaBancaria',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(147,'BSCL.Tesoreria.BD.Servicios::ModificarCuentaBancaria',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(148,'BSCL.Plataforma.BD.Servicios::ACC.ActualizarPerfilAcceso',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(149,'BSCL.Facturacion.BD.Servicios::AnularComprobante',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(152,'BSCL.SocioNegocio.BD.Servicios::REL.CrearRelacionPersona',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(153,'BSCL.SocioNegocio.BD.Servicios::REL.ModificarRelacionPersona',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(154,'BSCL.CuentaInversion.BD.Servicios::CrearTraspaso',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(155,'BSCL.Tesoreria.BD.Servicios::EliminarCaja',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(156,'BSCL.Portafolio.BD.Servicios::CrearAutorizadoOrden',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(158,'BSCL.Orden.BD.Servicios::ModificarTransaccion',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(159,'BSCL.Plataforma.BD.Servicios::ACC.ActualizarUsuario',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(160,'BSCL.Plataforma.BD.Servicios::ACC.DesbloqueoTemporalUsuario',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(161,'BSCL.Tesoreria.BD.Servicios::CrearChequera',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(162,'BSCL.Portafolio.BD.Servicios::ModificarEstadoAutorizadoOrden',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(163,'BSCL.Tesoreria.BD.Servicios::EliminarCuentaBancaria',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(164,'BSCL.SocioNegocio.BD.Servicios::PER.CrearPersonaNombreFantasia',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(165,'BSCL.SocioNegocio.BD.Servicios::PER.ModificarEstadoPersonaNombreFantasia',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(166,'BSCL.Tesoreria.BD.Servicios::ModificarChequera',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(167,'BSCL.Plataforma.BD.Servicios::TRA.CrearHomologacionOperador',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(168,'BSCL.Plataforma.BD.Servicios::TRA.CrearConfiguracionOperador',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(169,'BSCL.CuentaInversion.BD.Servicios::CertificarCargoAbono',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(170,'BSCL.CuentaInversion.BD.Servicios::AnularCargoAbono',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(171,'BSCL.Plataforma.BD.Servicios::TRA.ModificarHomologacionOperador',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(172,'BSCL.Plataforma.BD.Servicios::TRA.ModificarConfiguracionOperador',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(176,'BSCL.SocioNegocio.BD.Servicios::REL.ConsultarRelacionPersona',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(177,'BSCL.Tesoreria.BD.Servicios::BloquearDesbloquearChequera',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(178,'BSCL.Portafolio.BD.Servicios::ModificarEstadoPortafolio',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(179,'BSCL.Orden.BD.Servicios::ReservarLiberarTransacciones',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(180,'BSCL.SocioNegocio.BD.Servicios::REL.CrearTipoRelacion',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(181,'BSCL.SocioNegocio.BD.Servicios::REL.ModificarTipoRelacion',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(182,'BSCL.Plataforma.BD.Servicios::ACC.CerrarSesionUsuario',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(183,'BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma',0,'P',''); 


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(184,'BSCL.Plataforma.BD.Servicios::ACC.CambiarClaveUsuario',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(185,'BSCL.Tesoreria.BD.Servicios::CrearConfiguracionChequera',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(189,'BSCL.Tesoreria.BD.Servicios::ModificarConfiguracionChequera',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(192,'BSCL.Tesoreria.BD.Servicios::EliminarConfiguracionChequera',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(193,'BSCL.Tesoreria.BD.Servicios::ConsultarClearing',0,'P','');

--194 disponible
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(195,'BSCL.Orden.BD.Servicios::CrearGrupo',0,'P','');
--196 disponible
--197 disponible
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(198,'BSCL.Custodia.BD.Servicios::EVC.ProcesarEventoCapital' , 1,  'P', '');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(199,'BSCL.Custodia.BD.Servicios::EVC.CrearEventoCapital' , 1,  'P', '');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(200,'BSCL.Orden.BD.Servicios::ActualizarGrupoPortafolio',0,'P','');   
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(201,'BSCL.Mercado.BD.Servicios::ObtenerParidadTipoCambio',0,'P','');   
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(202,'BSCL.Custodia.BD.Servicios::VAL.ValorizacionCustodia',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(203,'BSCL.Plataforma.BD.Servicios::EVT.AgregarPlantilla',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(205,'BSCL.Plataforma.BD.Servicios::EVT.AgregarConfiguracionEvento',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(206,'BSCL.Plataforma.BD.Servicios::DOC.AgregarTipoDocumento',0,'P',''); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (208, 'BSCL.Custodia.BD.Servicios::EVC.CrearEventoCapitalCliente' , 1,  'P', 'Crea un Evento Capital Cliente');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (209, 'BSCL.Plataforma.BD.Servicios::EVT.AsocicarTipoEventoConConfiguracion' , 1,  'P', 'Crea un Evento Capital Cliente');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (210, 'BSCL.Plataforma.BD.Servicios::DOC.AgregarDocumento' , 1,  'P', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(211,'BSCL.Orden.BD.Servicios::CrearLibroOrdenes',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(212,'BSCL.Orden.BD.Servicios::ModificarLibroOrdenes',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(213,'BSCL.Orden.BD.Servicios::CambiarEstadoLibroOrdenes',0,'P','');


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(214,'BSCL.Orden.BD.Servicios::CrearSimulacionAsignacionPreProm',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (215, 'BSCL.Custodia.BD.Servicios::EVC.ModificarEventoCapital' , 1,  'P', 'Modifica un Evento Capital Cliente');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(216,'BSCL.Tesoreria.BD.Servicios::ProcesarClearing',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(217,'BSCL.Orden.BD.Servicios::CrearLibroOrdenesOperadores',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(218,'BSCL.Portafolio.BD.Servicios::CrearAnotacionPortafolio',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(219,'BSCL.Portafolio.BD.Servicios::ModificarAnotacionPortafolio',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(220,'BSCL.Orden.BD.Servicios::CambiarEstadoLibroOrdenesDetalle',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(221,'BSCL.Orden.BD.Servicios::CrearLibroOrdenesSegmento',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(222,'BSCL.Orden.BD.Servicios::CrearLibroOrdenesMarcasNegociacion',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(223,'BSCL.Orden.BD.Servicios::CrearLibrosPorDefecto',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(224,'BSCL.Orden.BD.Servicios::CrearSimulacionAsignacion',0,'P','');

--DELETE FROM "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" where "IdServicio" = 224;
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT" ) 
VALUES(224,'BSCL.Orden.BD.Servicios::CrearSimulacionAsignacion',0,'P','','P_TRANSACCIONES=BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Entrada_Transacciones;P_PORTAFOLIOS=BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Entrada_Portafolios;P_ORDENES=BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Entrada_Ordenes;P_CARTERAS=BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Entrada_Carteras;',
'P_OUT_SIMASIGNACION=BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Salida_Simulacion;P_OUT_COBROSASIGNACION=BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Salida_Cobros;P_OUT_ADVERTENCIASDETALLE=BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Salida_AdvertenciasDetalle;P_OUT_ERRORESDETALLE=BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Salida_ErroresDetalle');
--commit;
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(225,'BSCL.Tesoreria.BD.Servicios::RecepcionarNominasTransferencias',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(226,'BSCL.Tesoreria.BD.Servicios::GirarComprobantesEgreso',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (227, 'BSCL.Custodia.BD.Servicios::EVC.EliminarEventoCapital' , 1,  'P', 'Elimina un Evento Capital Cliente');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(228,'BSCL.Plataforma.BD.Servicios::CRE.CrearRequisitoAperturaCierreModulo',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (229, 'BSCL.Custodia.BD.Servicios::EVC.ModificarEventoCapitalCliente' , 1,  'P', 'Crea un Evento Capital Cliente');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(230,'BSCL.Tesoreria.BD.Servicios::AnularNominasTransferencias',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(231,'BSCL.Tesoreria.BD.Servicios::ReprocesarNominasTransferencias',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(232,'BSCL.SocioNegocio.BD.Servicios::CLI.CrearClienteNomina',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion", "TablasTempIN", "TablasTempOUT" ) 
VALUES(233,'BSCL.Facturacion.BD.Servicios::ModificarMovimientoComprobante',0,'P','', 'P_MOVIMIENTOFACTURARGRAL=BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_IN_MovimientoComprobante;P_MOVIMIENTOFACTURARCOBROS=BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_IN_MovimientoComprobanteCobro;P_MOVIMIENTOFACTURARFDPAGO=BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_IN_MovimientoComprobanteFormaDePago', 'P_OUT_INFORMACIONDETALLE=BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_OUT_InformacionDetalle;P_OUT_RESULTADOPROCESO=BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_OUT_ResultadoProceso;P_OUT_ADVERTENCIASDETALLE=BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_OUT_AdvertenciasDetalle;P_OUT_ERRORESDETALLE=BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_OUT_ErroresDetalle');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(234,'BSCL.Plataforma.BD.Servicios::CRE.SolicitudAperturaModulo',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(235,'BSCL.Orden.BD.Servicios::BloquearDesbloquearOrdenes',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(236,'BSCL.Orden.BD.Servicios::BloquearDesbloquearTransacciones',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(237,'BSCL.Custodia.BD.Servicios::EVC.EliminarEventoCapitalCliente',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (238, 'BSCL.Custodia.BD.Servicios::EVC.ConsultaEventoCapital' , 1,  'P', 'Consulta un Evento Capital Cliente');


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(239,'BSCL.SocioNegocio.BD.Servicios::PER.CrearPersonaGrupoDireccionElectronica',0,'P','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(240,'BSCL.Plataforma.BD.Servicios::CRE.SolicitudCierreModulo',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(241,'BSCL.SocioNegocio.BD.Servicios::PER.CrearPersonaGrupoDireccionElectronicaDetalle',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(242,'BSCL.SocioNegocio.BD.Servicios::PER.ModificarPersonaGrupoDireccionElectronica',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(243,'BSCL.SocioNegocio.BD.Servicios::PER.ModificarPersonaGrupoDireccionElectronicaDetalle',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(244,'BSCL.Orden.BD.Servicios::CuadraturaOrdenes',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(245,'BSCL.Orden.BD.Servicios::CrearAsignacionMM',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(246,'BSCL.Orden.BD.Servicios::ObtenerDatosDesdeArchivoAsignacion',0,'P','');

--247,248 disponible.

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(249,'BSCL.Normativa.BD.Servicios::TRI.DJ1885.GrabarDetalle',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(250,'BSCL.Normativa.BD.Servicios::TRI.DJ1885.EliminarDetalles',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(251,'BSCL.Normativa.BD.Servicios::TRI.DJ1885.ObtenerTotales',0,'P','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(252,'BSCL.Orden.BD.Servicios::CalcularNetoYCobrosMM',0,'P','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(253,'BSCL.Plataforma.BD.Servicios::COM.ModificarPerfilComercial',0,'P','');


INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(254,'BSCL.Orden.BD.Servicios::CrearOrdenMM',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(255,'BSCL.Tesoreria.BD.Servicios::CrearDeposito',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(256,'BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosGenerarPartidaIngresos',0,'P','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion", "TablasTempIN", "TablasTempOUT" )
VALUES(257,'BSCL.Facturacion.BD.Servicios::CrearComprobanteNotaDeCredito',0,'P','', 'P_COMPROBANTE=BSCL.Facturacion.BD::GBL.CrearComprobanteNotaDeCredito_IN_Comprobante', 'P_MOVIMIENTOCOMPROBANTE=BSCL.Facturacion.BD::GBL.CrearComprobanteNotaDeCredito_OUT_MovimientoComprobante;P_OUT_COMPROBANTE=BSCL.Facturacion.BD::GBL.CrearComprobanteNotaDeCredito_OUT_Comprobante;P_OUT_INFORMACIONDETALLE=BSCL.Facturacion.BD::GBL.CrearComprobanteNotaDeCredito_OUT_InformacionDetalle;P_OUT_RESULTADOPROCESO=BSCL.Facturacion.BD::GBL.CrearComprobanteNotaDeCredito_OUT_ResultadoProceso;P_OUT_ADVERTENCIASDETALLE=BSCL.Facturacion.BD::GBL.CrearComprobanteNotaDeCredito_OUT_AdvertenciasDetalle;P_OUT_ERRORESDETALLE=BSCL.Facturacion.BD::GBL.CrearComprobanteNotaDeCredito_OUT_ErroresDetalle');
 
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(258,'BSCL.Orden.BD.Servicios::CrearOrdenPostreraMM',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(259,'BSCL.Tesoreria.BD.Servicios::CertificarDepositos',0,'P','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(261,'BSCL.Portafolio.BD.Servicios::CrearPreferenciaPago',0,'P','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(262,'BSCL.Orden.BD.Servicios::CrearTransacciones',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(263,'BSCL.Orden.BD.Servicios::CuadraturaCierreDiario',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(265,'BSCL.Custodia.BD.Servicios::GTA.ConsultarCustodiaParaGarantia2',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(266,'BSCL.Plataforma.BD.Servicios::VAL.ValorizarRFMasivo',0,'P','');
--DELETE FROM "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=266;

            
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(267,'BSCL.Custodia.BD.Servicios::GTA.IngresarGarantiasUI2',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(269,'BSCL.Custodia.BD.Servicios::GTA.RetirarGarantiasUI2',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(270,'BSCL.Plataforma.BD.Servicios::REQ.CrearRequisitoOperativo',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(271,'BSCL.Plataforma.BD.Servicios::REQ.ModificarRequisitoOperativo',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(272,'BSCL.Plataforma.BD.Servicios::COM.ModificarPerfilCobrosDetalle',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(273,'BSCL.Plataforma.BD.Servicios::COM.CrearPerfilCobrosDetalle',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(274,'BSCL.Plataforma.BD.Servicios::IND.ConsultarIndicador',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(275,'BSCL.Plataforma.BD.Servicios::IND.ConsultarPreferencia',0,'P','');


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(276,'BSCL.Orden.BD.Servicios::CuadraturaOficializarOrdenes',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(277,'BSCL.Plataforma.BD.Servicios::IND.ConsultarRequisito',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(280,'BSCL.Plataforma.BD.Servicios::IND.ModificarPreferencia',0,'P','');



INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(281,'BSCL.Plataforma.BD.Servicios::COM.CrearPerfilProducto',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(282,'BSCL.Plataforma.BD.Servicios::COM.ModificarPerfilProducto',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(283,'BSCL.Plataforma.BD.Servicios::COM.CrearPerfilProductoDetalle',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(284,'BSCL.Plataforma.BD.Servicios::COM.ModificarPerfilProductoDetalle',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(285,'BSCL.Plataforma.BD.Servicios::CRE.EliminarRequisitoAperturaCierreModulo',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(286,'BSCL.Plataforma.BD.Servicios::COM.CrearValorCobro',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(287,'BSCL.Plataforma.BD.Servicios::COM.ModificarValorCobro',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(288,'BSCL.Plataforma.BD.Servicios::PAG.CrearPreferenciaPago',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(289,'BSCL.Plataforma.BD.Servicios::PAG.ModificarPreferenciaPago',0,'P','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(290,'BSCL.Portafolio.BD.Servicios::ModificarPreferenciaPago',0,'P','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(291,'BSCL.Custodia.BD.Servicios::ConsultarCartolaMovimientos',0,'P','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(292,'BSCL.Plataforma.BD.Servicios::LIM.CrearPlantillaLimite',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(293,'BSCL.Plataforma.BD.Servicios::LIM.CrearPlantillaLimiteDetalle',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(294,'BSCL.Plataforma.BD.Servicios::LIM.ModificarPlantillaLimite',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES (295,'BSCL.Plataforma.BD.Servicios::LIM.ModificarPlantillaLimiteDetalle',0,'P','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES (296,'BSCL.Orden.BD.Servicios::ObtenerDetalleAsignacion',0,'P','Servicio obtencion detalle de Asignacion/Refacturacion');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES (297,'BSCL.Orden.BD.Servicios::RefacturarMovimiento',0,'P','Servicio Refacturacion de movimientos');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES (298,'BSCL.Orden.BD.Servicios::DashboardOrdenes',0,'P','Servicio Dashboard Ordenes');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT")
VALUES(299, 'BSCL.Custodia.BD.Servicios::EVC.CerrarEventoCapitalDV', 1, 'P', 'Cierra un Evento Capital Cliente (DV)', '', 'P_OUT_INFORMACIONDETALLE=BSCL.Custodia.BD::GBL.CerrarEventoCapitalDV_Salida_InformacionDetalle;P_OUT_ADVERTENCIASDETALLE=BSCL.Custodia.BD::GBL.CerrarEventoCapitalDV_Salida_AdvertenciasDetalle;P_OUT_ERRORESDETALLE=BSCL.Custodia.BD::GBL.CerrarEventoCapitalDV_Salida_ErroresDetalle');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (300, 'BSCL.Custodia.BD.Servicios::EVC.CerrarEventoCapitalDA' , 1,  'P', 'Cierra un Evento Capital Cliente (DA)');      
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (301, 'BSCL.Plataforma.BD.Servicios::COM.CrearPlantillaEmpleadoDetalle' , 1,  'P', '');      
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (302, 'BSCL.Plataforma.BD.Servicios::COM.ModificarPlantillaEmpleado' , 1,  'P', '');  
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (303, 'BSCL.Plataforma.BD.Servicios::COM.ModificarPlantillaEmpleadoDetalle' , 1,  'P', ''); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (304, 'BSCL.SocioNegocio.BD.Servicios::PER.ModificarPersonaFatca' , 1,  'P', ''); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (305, 'BSCL.Plataforma.BD.Servicios::COM.ConsultarMatrizOperacional' , 1,  'P', '');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(306,'BSCL.Plataforma.BD.Servicios::LIM.ModificarLimite',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (307, 'BSCL.Plataforma.BD.Servicios::DOC.ModificarTipoDocumento' , 1,  'P', ''); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (308, 'BSCL.SocioNegocio.BD.Servicios::DOC.ConsultarDocumentos' , 1,  'P', ''); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (309, 'BSCL.Normativa.BD.Servicios.NOR::CrearSolicitudCircularAntiguo' , 1,  'P', 'Servicio que crea solicitud circular'); 

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (310, 'BSCL.Plataforma.BD.Servicios::PLA.CrearBoveda' , 1,  'P', ''); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (311, 'BSCL.Plataforma.BD.Servicios::PLA.ModificarBoveda' , 1,  'P', ''); 

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (312, 'BSCL.Plataforma.BD.Servicios::EVT.EnviarReEnviarConfirmarRechazarDescartarNotificacion' , 1,  'P', ''); 

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(313,'BSCL.Tesoreria.BD.Servicios::AnularRegistros',0,'P','Anulación de movimientos y comprobantes de tesorería');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(314,'BSCL.Tesoreria.BD.Servicios::ConsultarSaldoCuentaBanco',0,'P','Consulta de saldo banco');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(315,'BSCL.Plataforma.BD.Servicios::ConsultarInstrumentoUnico',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(316,'BSCL.Plataforma.BD.Servicios::LIM.CrearLimiteCtaPropia',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(317,'BSCL.Orden.BD.Servicios::CrearMovFacturarAsignacionMM',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(318,'BSCL.Plataforma.BD.Servicios::LIM.ModificarLimiteCtaPropia',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(319,'BSCL.Plataforma.BD.Servicios::LIM.AgregarLimiteEmisor',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(320,'BSCL.Plataforma.BD.Servicios::LIM.ModificarLimiteEmisor',0,'P','');

--JPZ

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (321, 'BSCL.Plataforma.BD.Servicios::INS.CrearRelacionInstrumento' , 1,  'P', ''); 

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (322, 'BSCL.Plataforma.BD.Servicios::INS.ModificarRelacionInstrumento' , 1,  'P', ''); 

-- 323 ocupado, agregar al archivo!

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (324, 'BSCL.SocioNegocio.BD.Servicios::CLI.ModificarDeclaracionOrigenFondos' , 1,  'P', ''); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (325, 'BSCL.SocioNegocio.BD.Servicios::CLI.CrearDeclaracionOrigenFondos' , 1,  'P', ''); 

-- 326,327 ocupados -- FAVOR MANTENER ACTUALIZADO ESTE ARCHIVO ¬.¬

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (328, 'BSCL.Plataforma.BD.Servicios::COM.CrearTipoOrigenFondo' , 0,  'P', ''); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (329, 'BSCL.Plataforma.BD.Servicios::COM.ModificarTipoOrigenFondo' , 0,  'P', ''); 

-- 330,331,332 ocupados --

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (333, 'BSCL.Plataforma.BD.Servicios::PLA.CambiarZonaHoraria' , 1,  'P', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(335,'BSCL.Plataforma.BD.Servicios::LIM.AgregarLimiteRangosMontoPlazo',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(336,'BSCL.Plataforma.BD.Servicios::LIM.ModificarLimiteRangosMontoPlazo',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(337,'BSCL.Orden.BD.Servicios::ObtenerDatosDesdeArchivoOPA',0,'P','');

/*==============================================================*/
/* SERVICIOS : Agregar Servicios    							*/
/*==============================================================*/

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) VALUES(330,'BSCL.Normativa.BD.Servicios::TRI.DJ1922.EliminarDetalle',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) VALUES(331,'BSCL.Normativa.BD.Servicios::TRI.DJ1922.GrabarDetalle',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) VALUES(332,'BSCL.Normativa.BD.Servicios::TRI.DJ1922.ObtenerTotales',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (339, 'BSCL.Plataforma.BD.Servicios::PLA.CambiarZonaHoraria' , 1,  'P', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (334, 'BSCL.Normativa.BD.Servicios.NCG380::ObtenerRutaArchivo' , 1,  'P', 'Servicio que obtiene ruta de archivos NCG380'); 


/*CGO*/
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (338, 'BSCL.Custodia.BD.Servicios::EVC.ConsultaCarterasSuscripcionCliente' , 1,  'P', '')
/*CGO*/

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (339, 'BSCL.Plataforma.BD.Servicios::EVT.ActualizarConfiguracionEvento' , 1,  'P', '');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (340, 'BSCL.Plataforma.BD.Servicios::EVT.ActivarDesactivarConfiguracionEventos' , 1,  'P', '');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (341, 'BSCL.Plataforma.BD.Servicios::EVT.ActivarDesactivarDetalleEventos' , 1,  'P', '');


INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(342, 'BSCL.Custodia.BD.Reglas::ServicioClienteTieneCustodiaInstrumento', 0, 'P', 'Servicio para consultar si un cliente tiene custodia');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (343, 'BSCL.Custodia.BD.Servicios::EVC.CertificaSuscripcionOSA' , 1,  'P', 'Certifica Suscripción de Acciones OSA');      

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (344, 'BSCL.Custodia.BD.Servicios::EVC.SuscripcionEmisionPago' , 1,  'P', '')

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (345, 'BSCL.Custodia.BD.Servicios::EVC.NotificaSuscripcionOSA' , 1,  'P', '')

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (346, 'BSCL.Custodia.BD.Servicios::EVC.AnulaSuscripcionOSA' , 1,  'P', '')

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (347, 'BSCL.Custodia.BD.Servicios::EVC.RetirarCustodiaOSA' , 1,  'P', '')

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (348, 'BSCL.Custodia.BD.Servicios::EVC.EnviarMovimientoFacturarSuscripcionOSA' , 1,  'P', '')

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (349,'BSCL.Normativa.BD.Servicios.NCG380::CrearDocumentoNCG380' , 1,  'P', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (350,'BSCL.Normativa.BD.Servicios.NCG380::ActualizarEstadoSolicitudCircular' , 1,  'P', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (351,'BSCL.Plataforma.BD.Servicios::LIM.CopiarLimiteCtaPropia' , 1,  'P', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (352,'BSCL.MesaRF.BD.Servicios::CerrarMesaRF' , 1,  'P', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (353, 'BSCL.Plataforma.BD.Servicios::EVT.ActualizarNotificacion' , 1,  'P', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (354, 'BSCL.Custodia.BD.Servicios::EVC.RenovarEventoCapitalEmisionPago' , 1,  'P', '');

-- 355 ocupado
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (356, 'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondo' , 1,  'Q', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (357, 'BSCL.FondoMutuo.BD.Servicios::CrearSolicitudAporteRescateNormal' , 1,  'P', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (358,'BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteCtaPropia',1,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (359,'BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteEmisor',1,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (360,'BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteRangosMontoPlazo',1,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (361,'BSCL.Plataforma.BD.Vistas::LIM.ConsultarEstadoLimites',1,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (362,'BSCL.Plataforma.BD.Vistas::LIM.ConsultarPlantillaLimites',1,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (363,'BSCL.Plataforma.BD.Vistas::LIM.ConsultarDetallePlantillaLimites',1,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (364,'BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapital',1,'Q','Custodia: Vista Consulta Eventos de Capital');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (365,'BSCL.Normativa.BD.Servicios::NOR.CI1720.ObtenerTotales',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (366,'BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalCliente',1,'Q','Custodia: Vista Consulta Eventos de Capital Cliente');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (367,'BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalClienteFormaDePago',1,'Q','Custodia: Vista Consulta Formas de Pago Eventos de Capital Cliente');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (368, 'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTipoFondo' , 1,  'Q', '');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (369, 'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarConcepto' , 1,  'Q', '');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (370, 'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarProposito' , 1,  'Q', '');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (371, 'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarRegimen' , 1,  'Q', '');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (372, 'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarComisionEntrada' , 1,  'Q', '');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (373, 'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarComisionSalida' , 1,  'Q', '');

--Vistar ODATA Usuarios.
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (374, 'BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuario' , 1,  'Q', '');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (376, 'BSCL.Plataforma.BD.Vistas::ACC.ConsultarRol' , 1,  'Q', '');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (377, 'BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesPerfiles' , 1,  'Q', '');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (378, 'BSCL.Plataforma.BD.Vistas::ACC.ConsultarAccesosPerfilRol' , 1,  'Q', '');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (379, 'BSCL.Plataforma.BD.Vistas::ACC.ConsultarPerfilesNoTieneRol' , 1,  'Q', '');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (380, 'BSCL.Plataforma.BD.Vistas::ACC.ConsultarEstadosRol' , 1,  'Q', '');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (381, 'BSCL.Plataforma.BD.Vistas::ACC.ConsultarEstadosPerfil' , 1,  'Q', '');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (382, 'BSCL.Plataforma.BD.Vistas::ACC.ConsultarPerfiles' , 1,  'Q', '');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (383, 'BSCL.Plataforma.BD.Vistas::ACC.ConsultarAccesosPerfil' , 1,  'Q', '');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (384, 'BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto' , 1,  'Q', '');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (385, 'BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesPerfilesUsuario' , 1,  'Q', '');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (386, 'BSCL.Plataforma.BD.Vistas::ACC.ConsultarEstadosUsuario' , 1,  'Q', '');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (387, 'BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesUsuario' , 1,  'Q', '');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (388, 'BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarTipoSocioNegocio' , 1,  'Q', '');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (389, 'BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesNoTieneUsuario' , 1,  'Q', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (390, 'BSCL.Plataforma.BD.Servicios::COM.ConsultarMatrizOperacionalPortafolios' , 1,  'P', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT" ) 
VALUES(391,'BSCL.Orden.BD.Servicios::CalcularMontoYCobrosOrden',0,'P','',
'p_Ordenes=BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Entrada_Ordenes;p_CobrosModificados=BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Entrada_CobrosModificados',
'P_OUT_ORDENES=BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Salida_Ordenes;P_OUT_COBROS=BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Salida_Cobros;P_OUT_ADVERTENCIASDETALLE=BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Salida_AdvertenciasDetalle;P_OUT_ERRORESDETALLE=BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Salida_ErroresDetalle');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (392, 'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarEstadoSolicitudAporteRescate' , 1,  'Q', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (393, 'BSCL.Mesa.BD.Vistas::ConsultarParametro' , 0,  'Q', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (395, 'BSCL.Custodia.BD.Servicios::IngresoReserva' , 1,  'P', 'Reserva/Libera Cartera RF/IF');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (396, 'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateUI' , 1,  'Q', '');


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (399, 'BSCL.Mesa.BD.Vistas::ConsultarPlanillaBancoCentral' , 0,  'Q', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (500, 'BSCL.Orden.BD.Servicios::CrearOrdenes' , 1,  'P', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (400, 'BSCL.Normativa.BD.Servicios::NOR.CI1720.EliminarDetalles' , 0,  'P', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (401, 'BSCL.Normativa.BD.Servicios::NOR.CI1720.GrabarDetalle' , 0,  'P', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (402, 'BSCL.FondoMutuo.BD.Servicios::AnulacionSolicitudAporteRescate' , 0,  'P', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (407, 'BSCL.Mesa.BD.Servicios::ObtenerValores' , 0,  'P', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (408,'BSCL.Plataforma.BD.Vistas::LIM.ConsultarConfiguracionLimite',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (411,'BSCL.Mesa.BD.Servicios::SPT.CrearOperacionSpot',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (412,'BSCL.Plataforma.BD.Vistas::LIM.ConsultarGrupoEmisor',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (413,'BSCL.Plataforma.BD.Servicios::EVT.EnviarComentenos',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (414,'BSCL.Plataforma.BD.Vistas::LIM.ConsultarGrupoEmisorDetalle',1,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (416,'BSCL.Plataforma.BD.Servicios::LIM.CrearGrupoEmisor',1,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (417,'BSCL.Plataforma.BD.Servicios::LIM.ModificarGrupoEmisor',1,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (418,'BSCL.Plataforma.BD.Servicios::LIM.AgregarGrupoEmisorDetalle',1,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (419,'BSCL.Plataforma.BD.Servicios::LIM.ModificarGrupoEmisorDetalle',1,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (420, 'BSCL.Custodia.BD.Servicios::EVC.ProcesarEventoCapitalCorteCupon' , 1,  'P', 'Generación de Corte Cupón');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (421,'BSCL.Plataforma.BD.Vistas::LIM.UIConsultarLimiteCtaPropia',1,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (422,'BSCL.Plataforma.BD.Vistas::LIM.UIConsultarLimitePortafolio',1,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (423,'BSCL.Plataforma.BD.Vistas::LIM.ConsultarTipoLimite',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (425, 'BSCL.Custodia.BD.Servicios::EVC.CerrarEventoCapitalCorteCupon' , 1,  'P', 'Cierra un Evento Capital Cliente (CC)');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (427,'BSCL.Portafolio.BD.Vistas::ConsultarPortafolioDatosPlantilla',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (431, 'BSCL.Custodia.BD.Servicios::EVC.ProcesarEventoCapitalCanje' , 1,  'P', 'Procesa un Evento Capital Cliente Canje (CA)');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (433,'BSCL.Plataforma.BD.Vistas::LIM.UIConsultarEvaluacionLimite',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (436,'BSCL.Mesa.BD.Servicios::MRF.ObtenerDatosDesdeArchivoDAP',1,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (437,'BSCL.Mesa.BD.Vistas::MRF.ConsultarEstadoDeposito',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (440,'BSCL.Mesa.BD.Servicios::ConsultarConfiguracionCliente',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (444,'BSCL.Mesa.BD.Servicios::MRF.GrabarArchivoCargaDAP',1,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (445,'BSCL.Mesa.BD.Servicios::MRF.ProcesarArchivoCargaDAP',1,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (446,'BSCL.Mesa.BD.Servicios::MRF.ModificarEstadoArchivoCargaDAP',1,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (447,'BSCL.Mesa.BD.Vistas::MRF.ConsultarCargaMasivaDepositos',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (450,'BSCL.Mesa.BD.Vistas::ConsultarTemplateMesa',1,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (451,'BSCL.Mesa.BD.Vistas::ConsultarTipoProductoMesaModulo',1,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (452,'BSCL.Mesa.BD.Vistas::ConsultarCierreMesa',1,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (453,'BSCL.Mesa.BD.Servicios::CierreAperturaMesa',1,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (454,'BSCL.Mesa.BD.Vistas::ConsultarMantenedorMesas',1,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (455,'BSCL.Mesa.BD.Vistas::ConsultarEstadoMesas',1,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (456,'BSCL.Mesa.BD.Servicios::CrearMesa',1,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (457,'BSCL.Mesa.BD.Servicios::ModificarMesa',1,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (458,'BSCL.Mesa.BD.Vistas::ConsultarAgenteOperadorMesa',1,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (459,'BSCL.Mesa.BD.Servicios::CrearAgenteOperadorMesa',1,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (460,'BSCL.Mesa.BD.Servicios::ModificarAgenteOperadorMesa',1,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (461,'BSCL.Mesa.CrystalReports.Servicios::ConsultarOperacionesMRF',1,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (462,'BSCL.Mesa.CrystalReports.Servicios::ConsultarOperacionesDiariasPactos',1,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (463,'BSCL.Mesa.BD.Vistas::ConsultarOperacionesDiariasPactos',1,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (464,'BSCL.Mesa.BD.Servicios::ConsultarPlazosFechas',1,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (465,'BSCL.Mesa.BD.Vistas::ConsultarItemsManualesPosicionMRF',1,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (466,'BSCL.Mesa.BD.Servicios::ConsultarItemsManualesPosicionMRF',1,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (467,'BSCL.Mesa.BD.Servicios::GrabarItemsManualesPosicionMRF',1,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (468,'BSCL.Mesa.BD.Vistas::UIConsultarMetodosPosicionMRF',1,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (469,'BSCL.Mesa.BD.Vistas::ConsultarTipoComprobante',1,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (470,'BSCL.Mesa.BD.Vistas::ConsultarTipoEstado',1,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (471,'BSCL.Mesa.BD.Vistas::ConsultarTipoOperacionPosicion',1,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (472,'BSCL.Mesa.BD.Vistas::ConsultarEventoPosicion',1,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (473,'BSCL.Mesa.BD.Vistas::ConsultarMetodoEventoPosicion',1,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (475,'BSCL.Mesa.BD.Servicios::CrearMetodoPosicionMRF',1,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (476,'BSCL.Mesa.BD.Servicios::ModificarMetodoPosicionMRF',1,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (477,'BSCL.Mesa.BD.Servicios::CrearTipoOperacionPosicion',1,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (478,'BSCL.Mesa.BD.Servicios::ModificarTipoOperacionPosicion',1,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (479,'BSCL.Mesa.BD.Servicios::CrearEventoPosicion',1,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (480,'BSCL.Mesa.BD.Servicios::ModificarEventoPosicion',1,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (481,'BSCL.Mesa.BD.Servicios::CrearMetodoEventoPosicion',1,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (482,'BSCL.Mesa.BD.Servicios::ModificarMetodoEventoPosicion',1,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(544,'BSCL.Mesa.BD.Servicios::SPT.ConsultarParidadYTipoCambio',0,'P','Paridad y Tipo Cambio');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(546,'BSCL.Mesa.BD.Servicios::SPT.ModificarParidadYTipoCambio',0,'P','Modificar Paridad y Tipo Cambio');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (600,'BSCL.Plataforma.BD.Vistas::TAS.ConsultarFuente',1,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (601,'BSCL.Plataforma.BD.Servicios::TAS.GenerarArchivoTasas',1,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (603,'BSCL.Plataforma.BD.Vistas::TAS.ConsultarTipoTasa',1,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (604,'BSCL.Plataforma.BD.Vistas::TAS.ConsultarPrecioTasa',1,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (605,'BSCL.Plataforma.BD.Vistas::TAS.ConsultarEstadoTasa',1,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (606,'BSCL.Plataforma.BD.Vistas::TAS.ConsultarInstrumentoCustodiaParaTasas',1,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (607,'BSCL.Plataforma.BD.Servicios::TAS.CrearTasa',1,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (608,'BSCL.Plataforma.BD.Servicios::TAS.ModificarTasa',1,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (609,'BSCL.Plataforma.BD.Servicios::TAS.ModificarEstadoTasas',1,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (610,'BSCL.Plataforma.BD.Servicios::TAS.CargarArchivoTasas',1,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (611,'BSCL.Plataforma.BD.Vistas::TAS.ConsultarConfiguracionTasas',1,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (612,'BSCL.Plataforma.BD.Vistas::TAS.ConsultarTipoCriterioTasa',1,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (613,'BSCL.Plataforma.BD.Vistas::TAS.ConsultarCriterioSeleccionTasas',1,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (614,'BSCL.Plataforma.BD.Servicios::TAS.CrearCriterioSeleccionTasas',1,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (615,'BSCL.Plataforma.BD.Servicios::TAS.ModificarCriterioSeleccionTasas',1,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (616, 'BSCL.Custodia.BD.Servicios::EVC.ActualizaContraparte' , 1,  'P', 'Actualiza Contrapartes sin Socio de Negocio');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (618,'BSCL.Plataforma.BD.Vistas::TAS.ConsultarMargenTasa',1,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (619,'BSCL.Plataforma.BD.Servicios::TAS.CrearMargenTasa',1,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (620,'BSCL.Plataforma.BD.Servicios::TAS.ModificarMargenTasa',1,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (621,'BSCL.Plataforma.BD.Vistas::TAS.ConsultarUso',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (623,'BSCL.Plataforma.BD.Vistas::PLA.ConsultarDiaFeriado',1,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (624,'BSCL.Plataforma.BD.Servicios::PLA.CrearDiaFeriado',1,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (625,'BSCL.Plataforma.BD.Servicios::PLA.ModificarDiaFeriado',1,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (626,'BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoDiaFeriado',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (627,'BSCL.Custodia.BD.Servicios::MarcarOperacion',1,'P','');



INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (1017, 'BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (1018, 'BSCL.Tesoreria.BD.Vistas::ConsultarChequeras', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (1019, 'BSCL.Tesoreria.BD.Vistas::ConsultarConfiguracionChequeras', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (1020, 'BSCL.Tesoreria.BD.Vistas::ConsultarComprobante', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (1021, 'BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (1022, 'BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteDetalle', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (1023, 'BSCL.Tesoreria.BD.Vistas::ConsultarPreferenciaPagoCorredor', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (1024, 'BSCL.Tesoreria.BD.Vistas::ConsultarCartolaMovimientosTesoreria', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (1025, 'BSCL.Tesoreria.BD.Vistas::ConsultarDeposito', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (1026, 'BSCL.Tesoreria.BD.Vistas::ConsultarDepositoDetalle', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (1027, 'BSCL.Tesoreria.BD.Vistas::ConsultarCajas', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (1028, 'BSCL.Tesoreria.BD.Vistas::ConsultarCajaMoneda', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (1029, 'BSCL.Tesoreria.BD.Vistas::ConsultarCajaUsuario', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (1030, 'BSCL.Tesoreria.BD.Vistas::ConsultarCajaProductoEmpresa', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (1031, 'BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgreso', 0, 'Q', 'Tesoreria: vista');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (1032, 'BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgresoFormaDePago', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (1033, 'BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferencia', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (1034, 'BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferenciaDetalle', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (1035, 'BSCL.Tesoreria.BD.Vistas::ConsultarConceptosTesoreria', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (1036, 'BSCL.Tesoreria.BD.Vistas::ConsultarEstadosTesoreria', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (1037, 'BSCL.Tesoreria.BD.Vistas::ConsultarConfiguracionTesoreria', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (1038, 'BSCL.Tesoreria.BD.Vistas::ConsultarMonedaTesoreria', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (1040, 'BSCL.Tesoreria.BD.Vistas::ConsultarMedioDePagoTesoreria', 0, 'Q', 'Tesoreria: vista');   

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (1041, 'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTipoMovimientoFondoMutuo' , 1,  'Q', '');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1042,'BSCL.Plataforma.BD.Vistas::INS.ConsultarRelacionInstrumento',1,'Q','Plataforma: Vista Consulta RELACION INSTRUMENTOS');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1043,'BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoRelacionInstrumento',1,'Q','Plataforma: Vista CONSULTA TIPO RELACION INSTRUMENTOS');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1045,'BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraReservada',1,'Q','Custodia: Vista Blotter Cartera Reservada');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1044,'BSCL.Custodia.BD.Servicios::MRF.ConsultarCarteraConsolidada',1,'P','Custodia: Vista Cartera Consolidada');


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1050,'BSCL.SocioNegocio.BD.Vistas::SOC.UIConsultarSocioDeNegocio',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1051,'BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1052,'BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarTipoSocioNegocio',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1053,'BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarTipoBloqueoSocioDeNegocio',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1054,'BSCL.SocioNegocio.BD.Vistas::SOC.UIConsultarTipoSocioDeNegocioCustodia',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1055,'BSCL.Normativa.BD.Vistas.Art171::ConsultarCartaUno',1,'Q','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")  
VALUES (	1200	, 'BSCL.Orden.BD.Vistas::ConsultarAsignacionUI' , 1,  'Q', '');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")  
VALUES (	1201	, 'BSCL.Orden.BD.Vistas::ConsultarTransaccionUI' , 1,  'Q', '');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")  
VALUES (	1202	, 'BSCL.Orden.BD.Vistas::ConsultarOrdenPorAsignar' , 1,  'Q', '');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")  
VALUES (	1203	, 'BSCL.Orden.BD.Vistas::ConsultarOrdenPorNegociar' , 1,  'Q', '');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")  
VALUES (	1204	, 'BSCL.Orden.BD.Vistas::ConsultarOrdenEnNegociacion' , 1,  'Q', '');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")  
VALUES (	1205	, 'BSCL.Orden.BD.Vistas::ConsultarClientesLibroOrdenes' , 1,  'Q', '');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")  
VALUES (	1206	, 'BSCL.Orden.BD.Vistas::ConsultarTransaccionBloqueada' , 1,  'Q', '');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")  
VALUES (	1207	, 'BSCL.Orden.BD.Vistas::ConsultarOrdenBloqueada' , 1,  'Q', '');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")  
VALUES (	1208	, 'BSCL.Orden.BD.Vistas::ConsultarCobrosAsignacion' , 1,  'Q', '');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")  
VALUES (	1209	, 'BSCL.Orden.BD.Vistas::ConsultarCobrosOrden' , 1,  'Q', '');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")  
VALUES (	1210	, 'BSCL.Orden.BD.Vistas::ConsultarGrupo' , 1,  'Q', '');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")  
VALUES (	1211	, 'BSCL.Orden.BD.Vistas::ConsultarPortafoliosGrupo' , 1,  'Q', '');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")  
VALUES (	1212	, 'BSCL.Orden.BD.Vistas::ConsultarEstadoGrupoPortafolio' , 1,  'Q', '');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")  
VALUES (	1213	, 'BSCL.Orden.BD.Vistas::ConsultarLibroOrdenes' , 1,  'Q', '');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")  
VALUES (	1214	, 'BSCL.Orden.BD.Vistas::ConsultarLibroOrdenesOperadores' , 1,  'Q', '');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")  
VALUES (	1215	, 'BSCL.Orden.BD.Vistas::ConsultarLibroOrdenesSegmento' , 1,  'Q', '');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")  
VALUES (	1216	, 'BSCL.Orden.BD.Vistas::ConsultarLibroOrdenesMarcasNegociacion' , 1,  'Q', '');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")  
VALUES (	1217	, 'BSCL.Orden.BD.Vistas::ConsultarLibroPorDefecto' , 1,  'Q', '');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")  
VALUES (	1218	, 'BSCL.Orden.BD.Vistas::ConsultarTipoPrecioOrden' , 1,  'Q', '');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")  
VALUES (	1219	, 'BSCL.Orden.BD.Vistas::ConsultarFormaCustodiaOrden' , 1,  'Q', '');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")  
VALUES (	1220	, 'BSCL.Orden.BD.Vistas::ConsultarTipoCustodiaOrden' , 1,  'Q', '');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")  
VALUES (	1221	, 'BSCL.Orden.BD.Vistas::ConsultarPorcentajeVisibilidadOrden' , 1,  'Q', '');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")  
VALUES (	1222	, 'BSCL.Orden.BD.Vistas::ConsultarTipoFechaVigenciaOrden' , 1,  'Q', '');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")  
VALUES (	1223	, 'BSCL.Orden.BD.Vistas::ConsultarEstadoVigenciaOrden' , 1,  'Q', '');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")  
VALUES (	1224	, 'BSCL.Orden.BD.Vistas::ConsultarLibroOrdenesUI' , 1,  'Q', '');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")  
VALUES (	1225	, 'BSCL.Orden.BD.Vistas::ConsultarEstadoLibroOrdenes' , 1,  'Q', '');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")  
VALUES (	1226	, 'BSCL.Orden.BD.Vistas::ConsultarEstadoAsignacion' , 1,  'Q', '');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")  
VALUES (	1227	, 'BSCL.Orden.BD.Vistas::ConsultarTipoAsignacion' , 1,  'Q', '');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")  
VALUES (	1228	, 'BSCL.Orden.BD.Vistas::ConsultarTipoLibroOrden' , 1,  'Q', '');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")  
VALUES (	1229	, 'BSCL.Plataforma.BD.Vistas::PLA.ConsultarServicio' , 1,  'Q', '');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1230,'BSCL.Mesa.BD.Vistas::MRF.ConsultarCarteraInstrumento',1,'Q','Mesa: Vista Consulta Cartera Mesa');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1231,'BSCL.Mesa.BD.Vistas::MRF.ConsultarCarteraDisponibleInstrumento',1,'Q','Mesa: Vista Consulta Cartera disponible Mesa');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1232,'BSCL.Mesa.BD.Vistas::ConsultarMesas',1,'Q','Mesa: Vista Consulta Mesas');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (1233, 'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTipoRescate' , 1,  'Q', '');
 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES(1234,'BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa',0,'Q','Consulta datos de la empresa');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES(1235,'BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante',0,'Q','Consulta movimiento comprobante'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES(1236,'BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobros',0,'Q','Consulta movimiento comprobante cobros');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES(1237,'BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteFormaDePago',0,'Q','Consulta movimiento comprobante forma de pago');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES(1238,'BSCL.Facturacion.BD.Vistas::ConsultarTipoTransaccion',0,'Q','Consulta tipo transaccion de facturacion');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES(1239,'BSCL.Facturacion.BD.Vistas::ConsultarEstadoFacturacion',0,'Q','Consulta estado facturacion');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES(1240,'BSCL.Facturacion.BD.Vistas::ConsultarComprobante',0,'Q','Consulta comprobante');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES(1241,'BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalle',0,'Q','Consulta comprobante detalle');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES(1242,'BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteDetalle',0,'Q','Consulta movimiento comprobante detalle');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES(1243,'BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobros',0,'Q','Consulta comprobante cobros');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES(1244,'BSCL.Facturacion.BD.Vistas::ConsultarComprobanteFormaDePago',0,'Q','Consulta comprobante forma de pago');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES(1245,'BSCL.Facturacion.BD.Vistas::ConsultarTipoComprobante',0,'Q','Consulta tipo comprobante facturacion');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES(1246,'BSCL.Facturacion.BD.Vistas::ConsultarEstadoComprobante',0,'Q','Consulta estado comprobante facturacion');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES(1247,'BSCL.Facturacion.BD.Vistas::ConsultarModoFacturacion',0,'Q','Consulta modo facturacion');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES(1248,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondoUI',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES(1249,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarEstadoInstrumentoFondoMutuo',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1250,'BSCL.Plataforma.BD.Vistas::PAG.ConsultarPreferenciaPago',0,'Q','');

-- INSERT SERVICIOS: VISTAS PORTAFOLIO
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1260,'BSCL.Portafolio.BD.Vistas::ConsultarAnotaciones',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1261,'BSCL.Portafolio.BD.Vistas::ConsultarAtributoAnotacion',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1262,'BSCL.Portafolio.BD.Vistas::ConsultarAutorizadoOrden',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1263,'BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolio',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1264,'BSCL.Portafolio.BD.Vistas::ConsultarEstadoPortafolio',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1265,'BSCL.Portafolio.BD.Vistas::ConsultarPortafolio',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1266,'BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1267,'BSCL.Portafolio.BD.Vistas::ConsultarPreferenciasFacturacion',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1268,'BSCL.Portafolio.BD.Vistas::ConsultarPropositoPortafolio',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1269,'BSCL.Portafolio.BD.Vistas::ConsultarTipoAutorizado',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1270,'BSCL.Portafolio.BD.Vistas::ConsultarTipoAutorizado',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1271,'BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio',1,'Q','');

-- INSERT SERVICIOS: VISTAS SOCIONEGOCIO
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1272,'BSCL.SocioNegocio.BD.Vistas::CLI.ClasificacionDocumento',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1273,'BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarAnexo',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1274,'BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarAnexosVencidos',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1275,'BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1276,'BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarDeclaracionOrigenFondos',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1277,'BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarDetalleDeclaracionOrigenFondos',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1278,'BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarLogCategoriaInversionista',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1279,'BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoCliente',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1280,'BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoOperacionOrigenFondos',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1281,'BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoOrigenFondo',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1282,'BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoSegmento',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1283,'BSCL.SocioNegocio.BD.Vistas::CNT.ConsultarCodigoCorredora',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1284,'BSCL.SocioNegocio.BD.Vistas::CNT.ConsultarContraparte',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1285,'BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1286,'BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEstadoEmpleado',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1287,'BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarTipoCargoEmpleado',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1288,'BSCL.SocioNegocio.BD.Vistas::PRO.ConsultarAreaProveedor',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1289,'BSCL.SocioNegocio.BD.Vistas::PRO.ConsultarEstadoProveedor',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1290,'BSCL.SocioNegocio.BD.Vistas::PRO.ConsultarProveedor',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1291,'BSCL.SocioNegocio.BD.Vistas::REL.ConsultarEntidadDestinoRelacion',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1292,'BSCL.SocioNegocio.BD.Vistas::REL.ConsultarPersonaPorTipoRelacion',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1293,'BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacion',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1294,'BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionNormativa',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1295,'BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacion',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1296,'BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacionDestino',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1297,'BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacionDisponiblePorSocioNegocio',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1298,'BSCL.SocioNegocio.BD.Vistas::REL.UIConsultarTipoRelacion',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1299,'BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarEnrolamiento',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1300,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarClasificacionEmpresaFatca',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1301,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarClasificacionFatca',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1302,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarEstadoCuentaBancaria',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1303,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarEstadoDireccionElectronica',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1304,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarEstadoDireccionFisica',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1305,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarEstadoDocumentoIdentidad',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1306,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarEstadoNombreFantasia',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1307,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarEstadoTarjetaBancaria',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1308,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarEstadoTelefono',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1309,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1310,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCuentasBancarias',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1311,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1312,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronicaDetalle',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1313,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1314,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDocumentoIdentidad',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1315,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaFatca',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1316,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaGrupoDireccionElectronica',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1317,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombresFantasia',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1318,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTarjetasBancarias',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1319,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1320,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPropositoMedioContacto',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1321,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoGenero',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1322,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoMedioContacto',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1323,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoPersona',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1324,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoTelefono',1,'Q','');


-- INSERT SERVICIOS: VISTAS PLATAFORMA RELACIONADAS CON SOCIO DE NEGOCIO.
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1325,'BSCL.Plataforma.BD.Vistas::COM.ConsultarCategoriaInversionista',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1326,'BSCL.Plataforma.BD.Vistas::COM.ConsultarCobro',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1327,'BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilCobros',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1328,'BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1329,'BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePlantillaEmpleado',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1330,'BSCL.Plataforma.BD.Vistas::COM.ConsultarEstadoPerfilComercial',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1331,'BSCL.Plataforma.BD.Vistas::COM.ConsultarGrupoActivos',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1332,'BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilCobros',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1333,'BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilComercialBase',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1334,'BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilProductos',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1335,'BSCL.Plataforma.BD.Vistas::COM.ConsultarPlantillaEmpleado',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1336,'BSCL.Plataforma.BD.Vistas::COM.ConsultarTipoGrupoActivos',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1337,'BSCL.Plataforma.BD.Vistas::COM.ConsultarValorCobro',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1338,'BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicador',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1339,'BSCL.Plataforma.BD.Vistas::IND.ConsultarEstadoTipoIndicador',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1340,'BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1341,'BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresModelo',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1342,'BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1343,'BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1344,'BSCL.Plataforma.BD.Vistas::IND.ConsultarTipoIndicador',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1345,'BSCL.Plataforma.BD.Vistas::REQ.ConsultarCondicionOperativa',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1346,'BSCL.Plataforma.BD.Vistas::REQ.ConsultarEstadoCondicionOperativa',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1347,'BSCL.Plataforma.BD.Vistas::REQ.ConsultarRequisitoOperativo',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1348,'BSCL.Plataforma.BD.Vistas::REQ.ConsultarTipoComportamiento',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1349,'BSCL.Plataforma.BD.Vistas::REQ.ConsultarTipoEtapa',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1350,'BSCL.Plataforma.BD.Vistas::REQ.ConsultarTipoOperacionUsuario',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1351,'BSCL.Plataforma.BD.Vistas::TRA.ConsultarConfiguracionOperador',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1352,'BSCL.Plataforma.BD.Vistas::TRA.ConsultarDetalleConfiguracionOperador',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1353,'BSCL.Plataforma.BD.Vistas::TRA.ConsultarHomologacionOperador',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1354,'BSCL.Plataforma.BD.Vistas::TRA.ConsultarTipoInstrumentoTransaccion',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1355,'BSCL.SocioNegocio.BD.Vistas::SOC.UIBlotterConsultarSocioDeNegocio',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (1356, 'BSCL.FondoMutuo.BD.Servicios::CrearInstrumentoFondoMutuo' , 1,  'P', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (1357, 'BSCL.FondoMutuo.BD.Servicios::ModificarInstrumentoFondoMutuo' , 1,  'P', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1358,'BSCL.Plataforma.BD.Vistas::PLA.ConsultarArchivosFormato',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1359,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFamiliaFondo',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1360,'BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1361,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorCuota',1,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1368,'BSCL.Plataforma.BD.Vistas::ConsultarCoberturaGarantia',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1369,'BSCL.Plataforma.BD.Vistas::ConsultarExigenciaGarantia',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1370,'BSCL.Custodia.BD.Servicios::EVC.CargarSorteoLetrasDesdeArchivoL043',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1371,'BSCL.Facturacion.BD.Servicios::EnviarComprobante',0,'P','');


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1372,'BSCL.Plataforma.BD.Servicios::LIM.EvaluacionLimite',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1373,'BSCL.Orden.BD.Servicios::CalcularLineaRiesgoOrdenes',0,'P','');


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1251,'BSCL.Portafolio.BD.Servicios::CrearCuentaCustodio',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1252,'BSCL.Portafolio.BD.Servicios::ModificarCuentaCustodio',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1374,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarInstrumentoAperturaCierreUI',0,'Q','');



INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1395,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarPerfilFondoMutuo',0,'Q','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(1396,'BSCL.Plataforma.BD.Vistas::AUT.ConsultarEvento',0,'Q','Plataforma: Vista Autorizacion');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(1397,'BSCL.Plataforma.BD.Vistas::AUT.ConsultarEtapaEvento',0,'Q','Plataforma: Vista Autorizacion');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(1398,'BSCL.Plataforma.BD.Vistas::AUT.ConsultarAutorizador',0,'Q','Plataforma: Vista Autorizacion');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(1399,'BSCL.Plataforma.BD.Vistas::AUT.ConsultarTipoEvento',0,'Q','Plataforma: Vista Autorizacion');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(1400,'BSCL.Plataforma.BD.Vistas::AUT.ConsultarEtapa',0,'Q','Plataforma: Vista Autorizacion');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(1401,'BSCL.Plataforma.BD.Vistas::AUT.ConsultarAutorizadorEtapa',0,'Q','Plataforma: Vista Autorizacion');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(1402,'BSCL.Plataforma.BD.Vistas::AUT.ConsultarEstadoAutorizacion',0,'Q','Plataforma: Vista Autorizacion');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1403,'BSCL.Orden.BD.Servicios::ConsultarCarterasAsignacion',0,'P','');


INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(1375, 'BSCL.Custodia.BD.Vistas::SIM.ConsultarAnticipos', 0, 'Q', 'Custodia');


INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(1394, 'BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultaneaNoVigente', 0, 'Q', 'Custodia');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(1408, 'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarHorarioCierreDefault', 0, 'Q', '');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(1409, 'BSCL.FondoMutuo.BD.Servicios::ModificarHorarioCierreDefault', 0, 'P', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(1411, 'BSCL.FondoMutuo.BD.Servicios::ModificarHorarioCierreInstrumento', 0, 'P', '');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(1412, 'BSCL.Orden.BD.Servicios::CrearSimulacionAsignacionCartera', 0, 'P', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(1413, 'BSCL.Normativa.BD.Vistas.NCG380::ConsultarParametrosSolicitudCircular', 0, 'Q', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1414,'BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1416,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorCuotaHistorico',0,'Q','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1418,'BSCL.Mesa.BD.Servicios::MRF.CrearOperacionMesaRF',0,'P','Mesa: Ingreso Operacion Mesa RF');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1419,'BSCL.Custodia.BD.Servicios::EVC.ProrrateoSorteoLetras',0,'P','');


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1420,'BSCL.Plataforma.BD.Servicios::CTT.ModificarContratoDatosGenerales',0,'P','');


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1421,'"BSCL.Plataforma.BD.Vistas::CTT.ConsultarArchivosContrato"',0,'Q','');

-- LOS NUMEROS INTERMEDIOS ESTAN OCUPADOS PERO NO ESTAN INSCRITOS EN ESTE SCRIPT.!!!

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1425,'BSCL.Custodia.BD.Servicios::EVC.CerrarEventoCapitalSorteoLetras',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1426,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarComision',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1427,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTramoComision',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1428,'BSCL.FondoMutuo.BD.Servicios::AdministrarComision',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1429,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarEstadoComisionFondoMutuo',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1431,'BSCL.FondoMutuo.BD.Servicios::CrearSolicitudAporteOvernight',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1432,'BSCL.Custodia.BD.Vistas::CUS.ConsultarConceptosCustodia',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1433,'BSCL.Custodia.BD.Vistas::GTA.ConsultarConceptosGarantia',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1434,'BSCL.FondoMutuo.BD.Servicios::CrearSolicitudAporteRescateProgramado',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1435,'BSCL.Plataforma.BD.Vistas::CTT.ConsultarParametrosContrato',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1436,'BSCL.FondoMutuo.BD.Servicios::CrearSolicitudRescateReinversion',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1437,'BSCL.FondoMutuo.BD.Servicios::CrearSolicitudRescateTraspaso',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1438,'BSCL.FondoMutuo.BD.Servicios::CrearSolicitudRescateTransferencia',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1442,'BSCL.FondoMutuo.BD.Servicios::CrearSolicitudAporteReinversion',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1443,'BSCL.Plataforma.BD.Vistas::INS.ConsultarClasificacionRiesgo',0,'P','');

--* MESA RF ----------------------------
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1450,'BSCL.Custodia.BD.Vistas::CUS.ConsultarOperaciones',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES(1455, 'BSCL.Custodia.BD.Servicios::IngresoOperacionPendiente' , 1,  'P', 'Ingreso/Modificacion de Operacion Mesa RF ');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1456,'BSCL.FondoMutuo.BD.Servicios::CrearSolicitudCambioSerie',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1457,'BSCL.Plataforma.BD.Vistas::ACC.ConsultarEstadosArchivo',0,'Q','');


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1459,'BSCL.Orden.BD.Servicios::CalcularExigibilidadOrdenes',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1461,'BSCL.Orden.BD.Servicios::CalcularCoberturaOrdenes',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1758 ,'BSCL.Mesa.CrystalReports.Servicios::MRF.CarteraPropiaDisponibleMesaRF',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1759 ,'BSCL.Mesa.CrystalReports.Servicios::MRF.CarteraPropiaGarantiaMesaRF',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1760,'BSCL.Mesa.CrystalReports.Servicios::MRF.CarteraPropiaIntermediadaMesaRF',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1761,'BSCL.Mesa.CrystalReports.Servicios::MRF.CarteraPropiaMesaRF',0,'P','');

/*
eliminado
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1462,'BSCL.Mesa.BD.Servicios::MRF.CrearOperacionPendienteMesaRF',0,'P','Mesa: Ingreso Operacion Pendiente Mesa RF');*/


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1465,'BSCL.Plataforma.BD.Vistas::PND.ConsultarAsuntosPendientes',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1466,'BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarSupervisor',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1467,'BSCL.SocioNegocio.BD.Servicios::EJE.CrearSupervisor',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1469,'BSCL.Tesoreria.BD.Servicios.NOM::SolicitudGenerarArchivo',0,'P','Solicitud de generación de archivo de transferencias bancarias');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1470,'BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1471,'BSCL.SocioNegocio.BD.Servicios::EJE.CrearEjecutivo',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1472,'BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1473,'BSCL.Normativa.BD.Vistas::DOC.ConsultarCampos',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1474,'BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarEstadoCliente',0,'Q','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1475,'BSCL.Plataforma.BD.Vistas::PLA.ConsultarBoveda',0,'Q','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1476,'BSCL.Plataforma.BD.Vistas::PLA.ConsultarBoveda',0,'Q','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1477,'BSCL.Mesa.BD.Vistas::MRF.ConsultarDetalleOperacionesMesaRF',0,'Q','Mesa: Mesa: Consulta Operaciones Mesa RF');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1478,'BSCL.SocioNegocio.BD.Servicios::EJE.CrearEjecutivoReemplazo',0,'P','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1479,'BSCL.SocioNegocio.BD.Servicios::CLI.ConsultaAvanzadaClientes',0,'P','');



INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1482,'BSCL.Normativa.BD.Vistas::NOR.SolicitudCircular',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1483,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondoAporteRescate',0,'Q','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1465,'BSCL.Plataforma.BD.Vistas::PND.ConsultarAsuntosPendientes',0,'Q','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1484,'BSCL.Plataforma.BD.Vistas::PND.ConsultarEstadosAsuntosPendientes',0,'Q','');


INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1488,'BSCL.SocioNegocio.BD.Servicios::EJE.ModificarEjecutivoPortafolio',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1490,'BSCL.FondoMutuo.BD.Servicios::CertificarSolicitudAporteRescate',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1492,'BSCL.Tesoreria.BD.Vistas::ConsultarTemplateTesoreria',0,'Q','Consulta template de tesoreria(Rpt)');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1493,'BSCL.Normativa.BD.Servicios.NOR::ART171Car1',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1494,'BSCL.SocioNegocio.BD.Servicios::EJE.TransferirClientes',0,'P','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(1499, 'BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoHistorico', 0, 'Q', '');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(1500, 'BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenEmprestitoHistorico', 0, 'Q', '');


INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(1502, 'BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento', 0, 'Q', '');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(1503, 'BSCL.Mercado.BD.Vistas::MER.ConsultarEmisor', 0, 'Q', '');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(1504, 'BSCL.Mercado.BD.Vistas::MER.ConsultarEmisorInstrumento', 0, 'Q', '');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(1505, 'BSCL.Plataforma.BD.Vistas::INS.UI.ConsultarMaestroInstrumentos', 0, 'Q', '');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(1506, 'BSCL.Facturacion.BD.Servicios::CrearMovimientoComprobante', 0, 'P', '');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(1507, 'BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoReajuste', 0, 'Q', '');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(1508, 'BSCL.Plataforma.BD.Vistas::INS.ConsultarEstadoInstrumento', 0, 'Q', '');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(1509, 'BSCL.Plataforma.BD.Servicios::INS.CrearInstrumentos', 0, 'P', '');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(1510, 'BSCL.Plataforma.BD.Servicios::INS.ModificarInstrumentos', 0, 'P', '');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(1511, 'BSCL.Normativa.BD.Vistas::NOR.ObtenerRelacionNormativa', 0, 'Q', '');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(1512, 'BSCL.Plataforma.BD.Vistas::MER.ConsultarParidadYTipoCambio', 0, 'Q', '');


INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(1514, 'BSCL.Plataforma.BD.Servicios::MER.CrearParidadYTipoCambio', 0, 'P', '');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(1515, 'BSCL.Plataforma.BD.Servicios::MER.ModificarParidadYTipoCambio', 0, 'P', '');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(1516, 'BSCL.Plataforma.BD.Vistas::MER.ConsultarTipoRegistroParidad', 0, 'Q', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(1517, 'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarLogCertificacionAporteRescate', 0, 'Q', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(1658, 'BSCL.Orden.BD.Vistas::ConsultarOrdenUI', 0, 'Q', '');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(1674, 'BSCL.Normativa.BD.Vistas::NOR.ConsultarArchivoNormativa', 0, 'Q', 'Consulta archivos de normativa');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(1676, 'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarEstadoCertificacionAporteRescate', 0, 'Q', '');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(1677, 'BSCL.SocioNegocio.BD.Servicios::EJE.ModificarEstadoSupervisor', 0, 'P', '');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(1678, 'BSCL.SocioNegocio.BD.Servicios::EJE.ModificarEstadoEjecutivo', 0, 'P', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(1679, 'BSCL.Normativa.BD.Servicios.NOR::ServicioObtenerVersionEsquema', 0, 'P', '');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(1680, 'BSCL.Normativa.BD.Servicios.NOR::ART171Car2', 0, 'P', '');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(1681, 'BSCL.Normativa.BD.Servicios.NOR::CI63Nomina', 0, 'P', '');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(1682, 'BSCL.Normativa.BD.Servicios.NOR::CI63Transa', 0, 'P', '');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1513,'BSCL.SocioNegocio.BD.Servicios::BLO.BloqueoMasivoClientesPortafolios',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1519,'BSCL.Tesoreria.BD.Servicios.BIL::ConsultarLiquidacionBilateral',0,'P','Servicio que consultar las transacciones bilaterales para liquidación');

--FIX ODATA
select * from "BSCL"."BSCL"."BSCL.Plataforma.BD::PLA.Servicio" order by "IdServicio" desc
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1520,'BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1521,'BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesPerfilesUsuario',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1522,'BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesNoTieneUsuario',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1523,'BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesUsuario',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1524,'BSCL.Plataforma.BD.Vistas::ACC.ConsultarAccesosPerfilRol',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1525,'BSCL.Plataforma.BD.Vistas::ACC.ConsultarPerfiles',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1526,'BSCL.Plataforma.BD.Vistas::ACC.ConsultarAccesosPerfil',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1527,'BSCL.Plataforma.BD.Vistas::ACC.ConsultarRol',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1528,'BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesPerfiles',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1529,'BSCL.Plataforma.BD.Vistas::ACC.ConsultarPerfilesNoTieneRol',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1530,'BSCL.Plataforma.BD.Vistas::ACC.ConsultarEstadosUsuario',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1531,'BSCL.Plataforma.BD.Vistas::ACC.ConsultarEstadosRol',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1532,'BSCL.Plataforma.BD.Vistas::ACC.ConsultarEstadosPerfil',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1533,'BSCL.Plataforma.BD.Vistas::AUT.ConsultarTipoEvento',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1534,'BSCL"."BSCL.Plataforma.BD.Vistas::AUT.ConsultarEtapa',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1535,'BSCL.Plataforma.BD.Vistas::AUT.ConsultarAutorizadorEtapa',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1536,'BSCL.Plataforma.BD.Vistas::BAT.ConsultarPrograma',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1537,'BSCL.Plataforma.BD.Vistas::BAT.ConsultarLogPrograma',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1538,'BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1539,'BSCL.Plataforma.BD.Vistas::COM.ConsultarGrupoActivos',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1540,'BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1541,'BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoProducto',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1542,'BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1544,'BSCL.Plataforma.BD.Vistas::DOC.ConsultaTipoDocumentos',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1545,'BSCL.Plataforma.BD.Vistas::DOC.ClasificacionDocumento',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1546,'BSCL.Plataforma.BD.Vistas::DOC.ConsultarAnexo',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1547,'BSCL.Plataforma.BD.Vistas::EVT.ConsultarMailPorRol',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1548,'BSCL.Plataforma.BD.Vistas::IND.ConsultarTipoIndicador',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1549,'BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicador',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1550,'BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoSubyacente',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1551,'BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteRangosMontoPlazo',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1552,'BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimites',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1553,'BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteEmisor',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1554,'BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteClasificacionRiesgo',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1555,'BSCL.Plataforma.BD::PLA.Diccionario',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1556,'BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracion',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1557,'BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1558,'BSCL.Plataforma.BD.Vistas::CRE.ConsultarCierreModulos',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1559,'BSCL.Plataforma.BD.Vistas::CRE.ConsultarLogCierreModulos',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1560,'BSCL.Plataforma.BD.Vistas::CRE.ConsultarCierreProcesosFunciones',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1561,'BSCL.Plataforma.BD.Vistas::CRE.ConsultarRequisitoAperturaCierre',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1562,'BSCL.Plataforma.BD.Vistas::PLA.ConsultarLog',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1563,'BSCL.Plataforma.BD.Vistas::PLA.ConsultarLogDetalle',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1564,'BSCL.Plataforma.BD.Vistas::REQ.ConsultarRequisitoOperativo',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1565,'BSCL.Plataforma.BD.Vistas::TRA.ConsultarDetalleConfiguracionOperador',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1566,'BSCL.Plataforma.BD.Vistas::TRA.ConsultarTipoInstrumentoTransaccion',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1567,'BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePlantillaEmpleado',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1568,'BSCL.Plataforma.BD.Vistas::PLA.ConsultarCondicionDeLiquidacion',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1569,'BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoBolsa',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1570,'BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoInstrumento',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1571,'BSCL.Plataforma.BD.Vistas::INS.ConsultarClasificacionTipoInstrumento',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1572,'BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoInstrumentoSubyacente',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1573,'BSCL.Normativa.BD.Vistas::NOR.ConsultarNormativa',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1574,'BSCL.Normativa.BD.Vistas::NOR.ConsultarTipoDocumento',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1575,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarClasificacionFatca',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1576,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarClasificacionEmpresaFatca',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1577,'BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoOperacionOrigenFondos',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1578,'BSCL.Plataforma.BD.Vistas::INS.ConsultarBolsa',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1579,'BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1580,'BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoInstrumentoMonedaLiquidacion',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1581,'BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoPla',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1582,'BSCL.Plataforma.BD.Vistas::INS.ConsultarOperacion',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1583,'BSCL.SocioNegocio.BD.Vistas::CNT.ConsultarContraparte',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1584,'BSCL.Plataforma.BD.Vistas::TRA.ConsultarConfiguracionOperador',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1585,'BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoTipoReajuste',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1586,'BSCL.Custodia.BD.Vistas::GTA.ConsultarEntidadDestinoGarantia',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1587,'BSCL.Custodia.BD.Vistas::GTA.ConsultarEspeciesEnGarantia',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1588,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoGenero',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1589,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoPersona',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1590,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoTelefono',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1591,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPropositoMedioContacto',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1592,'BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1593,'BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarTipoCargoEmpleado',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1594,'BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEstadoEmpleado',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1595,'BSCL.SocioNegocio.BD.Vistas::PRO.ConsultarEstadoProveedor',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1596,'BSCL.SocioNegocio.BD.Vistas::PRO.ConsultarAreaProveedor',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1597,'BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarTipoBloqueoSocioDeNegocio',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1598,'BSCL.Custodia.BD.Vistas::CUS.ConsultarTipoOperacionCustodia',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1599,'BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSaldos',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1600,'BSCL.Plataforma.BD.Vistas::PLA.ConsultarConcepto',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1601,'BSCL.Custodia.BD.Vistas::CUS.ConsultarEstadosCustodia',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1602,'BSCL.Plataforma.BD.Vistas::PLA.ConsultarSucursal',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1603,'BSCL.Plataforma.BD.Vistas::PAG.ConsultarMedioDePago',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1604,'BSCL.Plataforma.BD.Vistas::INS.ConsultarClasificacionRiesgo',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1605,'BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarTipoSocioNegocio',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1606,'BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoSegmento',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1607,'BSCL.Plataforma.BD.Vistas::REG.ConsultarTipoDocumentoIdentidad',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1608,'BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacion',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1609,'BSCL.SocioNegocio.BD.Vistas::REL.ConsultarEntidadDestinoRelacion',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1610,'BSCL.SocioNegocio.BD.Vistas::REL.ConsultarPersonaPorTipoRelacion',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1611,'BSCL.Plataforma.BD.Vistas::BAN.ConsultarBanco',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1612,'BSCL.Plataforma.BD.Vistas::BAN.ConsultarTipoTarjetaBancaria',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1613,'BSCL.Plataforma.BD.Vistas::BAN.ConsultarTipoCuentaBancaria',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1614,'BSCL.Custodia.BD.Vistas::GTA.ConsultarTipoInstrumentoParaGarantia',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1615,'BSCL.Plataforma.BD.Vistas::REG.ConsultarPais',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1616,'BSCL.Plataforma.BD.Vistas::REG.ConsultarGiro',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1617,'BSCL.Plataforma.BD.Vistas::REG.ConsultarTipoEstadoCivil',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1618,'BSCL.Plataforma.BD.Vistas::REG.ConsultarTipoRegimenConyugal',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1619,'BSCL.Plataforma.BD.Vistas::REG.ConsultarProfesion',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1620,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarEstadoDireccionFisica',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1621,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarEstadoDireccionElectronica',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1622,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarEstadoCuentaBancaria',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1623,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarEstadoTarjetaBancaria',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1624,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarEstadoNombreFantasia',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1625,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarEstadoTelefono',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1626,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarEstadoDocumentoIdentidad',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1627,'BSCL.Plataforma.BD.Vistas::REG.ConsultarCiudad',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1628,'BSCL.Plataforma.BD.Vistas::REG.ConsultarDivisionPais',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1629,'BSCL.Custodia.BD.Vistas::GTA.ConsultarOrigenGarantia',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1630,'BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilComercialBase',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1631,'BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilProductos',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1632,'BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilCobros',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1633,'BSCL.Plataforma.BD.Vistas::COM.ConsultarTipoGrupoActivos',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1634,'BSCL.Plataforma.BD.Vistas::COM.ConsultarPlantillaEmpleado',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1635,'BSCL.Plataforma.BD.Vistas::COM.ConsultarEstadoPerfilComercial',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1636,'BSCL.Plataforma.BD.Vistas::COM.ConsultarCategoriaInversionista',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1637,'BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoCanalIngreso',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1638,'BSCL.Custodia.BD.Vistas::CUS.ConsultarTipoEventoCapital',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1639,'BSCL.Custodia.BD.Vistas::CUS.ConsultarTipoCartera',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1640,'BSCL.Custodia.BD.Vistas::PRT.ConsultarTipoPrestamo',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1641,'BSCL.SocioNegocio.BD.Vistas::SOC.UIConsultarTipoSocioDeNegocioCustodia',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1642,'BSCL.Portafolio.BD.Vistas::ConsultarPropositoPortafolio',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1643,'BSCL.Portafolio.BD.Vistas::ConsultarPortafolio',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1644,'BSCL.Plataforma.BD.Vistas::BAT.ConsultarTiposProgramas',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1645,'BSCL.Plataforma.BD.Vistas::PAG.ConsultarModalidadDePago',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1646,'BSCL.Homologacion.BD.Vistas::HOM.TipoCarga',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1647,'BSCL.Homologacion.BD.Vistas::HOM.ModoCarga',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1648,'BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoLog',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1649,'BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoRegistro',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1650,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1651,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1652,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1653,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTarjetasBancarias',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1654,'BSCL.Plataforma.BD.Vistas::INS.ConsultarProducto',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1655,'BSCL.Plataforma.BD::IND.TipoDatoTipoIndicador',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1656,'BSCL.Plataforma.BD.Vistas::ATR.ConsultarEstadoTipoAtributo',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1657,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombresFantasia',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1659,'BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoDestinatarioCorreo',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1660,'BSCL.Plataforma.BD.Vistas::PAG.ConsultarPreferenciaPago',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1661,'BSCL.Portafolio.BD.Vistas::ConsultarEstadoPortafolio',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1662,'BSCL.Portafolio.BD.Vistas::ConsultarAtributoAnotacion',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1663,'BSCL.Portafolio.BD.Vistas::ConsultarTipoAutorizado',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1664,'BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoCliente',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1665,'BSCL.Plataforma.BD.Vistas::REQ.ConsultarCondicionOperativa',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1666,'BSCL.Plataforma.BD.Vistas::REQ.ConsultarEstadoCondicionOperativa',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1667,'BSCL.Plataforma.BD.Vistas::REQ.ConsultarTipoComportamiento',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1668,'BSCL.Plataforma.BD.Vistas::REQ.ConsultarTipoEtapa',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1669,'BSCL.Plataforma.BD.Vistas::REQ.ConsultarTipoOperacionUsuario',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1670,'BSCL.Plataforma.BD.Vistas::COM.ConsultarValorCobro',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1671,'BSCL.Plataforma.BD.Vistas::COM.ConsultarCobro',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1672,'BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1675,'BSCL.Tesoreria.BD.Servicios.BIL::ProcesarLiquidacionBilateral',0,'P','Servicio para procesar las transacciones bilaterales para liquidación');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1683,'BSCL.Orden.BD.Servicios::ConsultarCustodiaSubyacenteSimultanea',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ,"TablasTempIN" ,"TablasTempOUT") 
VALUES(1684,'BSCL.Plataforma.BD.Servicios::VAL.Valorizar',0,'P','',
'p_InstrumentosRV=BSCL.Plataforma.BD::VAL.Valorizar_Entrada_InstrumentosPQ;p_InstrumentosRF=BSCL.Plataforma.BD::VAL.Valorizar_Entrada_InstrumentosWS', 
'P_OUT_INSTRUMENTOSVALORIZADOSRV=BSCL.Plataforma.BD::VAL.Valorizar_Salida_InstrumentosValorizadosPQ;P_OUT_INSTRUMENTOSVALORIZADOSRF=BSCL.Plataforma.BD::VAL.Valorizar_Salida_InstrumentosValorizadosWS');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1685,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarEstadoLiquidacionAporteRescate',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1686,'BSCL.Orden.BD.Servicios::ConsultarCustodiaIngresoOrden',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1688,'BSCL.Orden.BD.Servicios::CrearOrdenesUI',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1689,'BSCL.FondoMutuo.BD.Servicios::LiquidacionSolicitudAporteRescate',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1690,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTipoOperacionFondoMutuo',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1693,'BSCL.Facturacion.BD.Vistas::ConsultarConceptosFacturacion',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1694,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarArchivoFondoMutuo',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(1695, 'BSCL.Normativa.BD.Servicios.NOR::LibroOperaciones', 0, 'P', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1696,'BSCL.Plataforma.BD.Vistas::ACC.ConsultarAccesos',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1697,'BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioAcceso',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1698,'BSCL.Plataforma.BD.Vistas::AUT.ConsultarTipoAutorizacion',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1700,'BSCL.Orden.BD.Servicios::CrearAsignaciones',0,'P','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1701,'BSCL.Plataforma.BD.Servicios::PLA.GuardarConfiguracionUsuario',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1702,'BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionUsuario',0,'Q','');


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1703,'BSCL.Normativa.BD.Servicios.NOR::UAF48PEP',0,'P','Circular de Personas Expuestas Políticamente');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1704,'BSCL.Plataforma.BD.Servicios::AUT.ModificarEtapaAutorizacion',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1705,'BSCL.Plataforma.BD.Servicios::AUT.ModificarAutorizadoEtapa',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1706,'BSCL.Plataforma.BD.Servicios::COM.ConsultarMatrizOperacionalExcel',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1707,'BSCL.Orden.BD.Vistas::ConsultarEventoBitacoraUI',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1709,'BSCL.Plataforma.BD.Servicios::COM.ConsultarMatrizOperacionalPortafolioExcel',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1710,'BSCL.FondoMutuo.BD.Servicios::ModificarBloqueoInstrumento',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1711,'BSCL.FondoMutuo.BD.Servicios::ModificarAperturaCierreInstrumento',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES(1712,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarEstadoFamiliaFondoMutuo',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1713,'BSCL.FondoMutuo.BD.Servicios::CrearFamiliaFondoMutuo',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1714,'BSCL.FondoMutuo.BD.Servicios::AdministrarPerfilFondoMutuo',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES(1715,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTipoPerfilFondoMutuo',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1716,'BSCL.FondoMutuo.BD.Servicios::ModificarFamiliaFondoMutuo',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1717,'BSCL.Orden.BD.Servicios::CantarOrdenes',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1718,'BSCL.Orden.BD.Servicios::RutearOrdenes',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1719,'BSCL.Plataforma.BD.Vistas::BAT.ConsultarEstadoPrograma',0,'Q','');


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1720,'BSCL.Orden.BD.Vistas::ConsultarTransaccionPorAsignar',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1721,'BSCL.Orden.BD.Servicios::ConsultarInformacionEjecutarAsignacionPorHora',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1722,'BSCL.Orden.BD.Servicios::EjecutarAsignacionPorHora',0,'P','');

insert into "BSCL.Plataforma.BD::PLA.Servicio"
values(1723, 'BSCL.Facturacion.BD.Vistas::CuadraturaTransaccionFacturacion',0,'Q',''); 
insert into "BSCL.Plataforma.BD::PLA.Servicio"
values(1724, 'BSCL.Custodia.BD.Servicios::ConsultarCustodiaMovimientos',0,'P',''); 
insert into "BSCL.Plataforma.BD::PLA.Servicio"
values(1725, 'BSCL.Facturacion.BD.Vistas::CuadraturaDetalleTransaccionFacturacion',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1726,'BSCL.Plataforma.BD.Servicios::CRE.EntregaSistema',0,'P','');
insert into "BSCL.Plataforma.BD::PLA.Servicio"
values(1729, 'BSCL.Plataforma.BD.Servicios::CalcularMontoPorTipoInstrumento',0,'P',''); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1730,'BSCL.Plataforma.BD.Servicios::CRE.AperturaSistema',0,'P','');
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES(1731,'BSCL.Tesoreria.CrystalReports.Servicios::DetalleComprobanteTesoreria',0,'P','Detalle Comprobante Tesoreria');

insert into "BSCL.Plataforma.BD::PLA.Servicio"
values(1735, 'BSCL.Mesa.BD.Servicios::MRF.AnularOperacionMesaRF',0,'P',''); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1736,'BSCL.SocioNegocio.BD.Servicios::PER.EliminarPersonaDocumentoIdentidad',0,'P','');
insert into "BSCL.Plataforma.BD::PLA.Servicio"
values(1742, 'BSCL.Mesa.BD.Servicios::MRF.AnticiparOperacionMesaRF',0,'P',''); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(1744,'BSCL.SocioNegocio.BD.Servicios::PER.EliminarPersonaTelefono',0,'P','');
insert into "BSCL.Plataforma.BD::PLA.Servicio"
values(1745, 'BSCL.Mesa.BD.Servicios::MRF.ReversarAnticipoOperacionMesaRF',0,'P',''); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1769,'BSCL.Mesa.BD.Vistas::ConsultarFormasDePagoPortafolioMesa',0,'Q','Mesa: Consulta Formas de Pago Recibe / Entrega');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (1784,'BSCL.Mesa.BD.Vistas::MRF.ConsultarPreferenciaPortafolioMesaRF',0,'Q','Mesa: Consulta Preferencias de Negocio Portafolio');


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3003,'BSCL.Normativa.BD.Vistas::CER.ConsultarCertificadoCliente',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3004,'BSCL.Normativa.BD.Servicios::CER.RecuperarCertificadoCliente',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3005,'BSCL.Plataforma.BD.Servicios::ACC.ValidaSesionUsuario',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3006,'BSCL.Plataforma.BD.Servicios::ACC.CrearSesionUsuario',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3008,'BSCL.Orden.BD.Vistas::ConsultarConfiguracionOrdenes',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3009,'BSCL.Orden.BD.Vistas::ConsultarCorredorCorresponsal',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3010,'BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen',0,'Q','');


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3011,'BSCL.Orden.BD.Servicios::CrearCorredorCorresponsal',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3012,'BSCL.Orden.BD.Servicios::ModificarCorredorCorresponsal',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3013,'BSCL.FondoMutuo.BD.Servicios::ConsultarDetalleSolicitud',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3014,'BSCL.Orden.BD.Vistas::ConsultarEstadoCorredorCorresponsal',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3016,'BSCL.FondoMutuo.BD.Utilitarios::CalcularComisiones',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3015,'BSCL.Plataforma.BD.Vistas::PLA.ConsultarTemplate',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3017,'BSCL.Orden.BD.Servicios::ConsultarCobrosPortafolios',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3018,'BSCL.SocioNegocio.BD.Servicios::EJE.ObtenerResumenEjecutivo',0,'P','');
 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3019,'BSCL.Normativa.BD.Servicios::CIR.ModificarParametro',0,'P','Normativa Circularizacion - Parametros');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3020,'BSCL.Normativa.BD.Vistas::CIR.ConsultarParametro',0,'Q','Normativa Circularizacion - Parametros');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3021,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarCustodiaFondosMutuos',0,'Q','Consultar Custodia Fondos Mutuos');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3022,'BSCL.FondoMutuo.BD.Servicios::ConsultarFechaSolicitudAporteRescate',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3023,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondosEnCustodia',0,'Q','Consutar Instrumentos Fondos Mutuos en Custodia');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3024,'BSCL.Normativa.BD.Servicios.CIR::SolicitudCircularizacion',0,'P','Normativa Circularizacion - Solicitud');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3025,'BSCL.Normativa.BD.Vistas::CIR.ConsultarNomina',0,'Q','Normativa Circularizacion - Nomina');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3026,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarCategoriaPerfilFondoMutuo',0,'Q','Consutar Tipos de Perfiles de Fondos Mutuos')

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3027,'BSCL.Facturacion.BD.Vistas::ConsultarTipoContratoCobroPeriodico',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3033,'BSCL.Normativa.BD.Servicios::NOR.GenerarCertificadosMasivosCliente',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3035,'BSCL.FondoMutuo.BD.Servicios::CargarValorCuota',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3036,'BSCL.FondoMutuo.BD.Servicios::CargarTipoCambio',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3037,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorTipoCambioHistorico',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3038,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorTipoCambio',0,'Q','');


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3039,'BSCL.Normativa.BD.Servicios.UAF::ConsultarPEP',0,'P','Normativa PEP - Reporte');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3040,'BSCL.Facturacion.BD.Vistas::ConsultarPortafolioTipoContratoCobroPeriodico',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3041,'BSCL.FondoMutuo.BD.Servicios::ConsultarCustodiaPortafolioFondoMutuo',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3042,'BSCL.Facturacion.BD.Vistas::ConsultarContratoCobroPeriodico',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3043,'BSCL.Normativa.BD.Servicios::CIR.ConsultarResumen',0,'P','Circularizacion Resumen - Reporte');


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3043,'BSCL.Normativa.BD.Servicios::CIR.ConsultarResumen',0,'P','Circularizacion Resumen - Reporte');



INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3049,'BSCL.Normativa.BD.Servicios.ART171::ConsultarCartaUno',0,'P','Normativa - Art 171 carta 1 - Reporte');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3050,'BSCL.Normativa.BD.Servicios.ART171::ConsultarCartaDos',0,'P','Normativa - Art 171 carta 2 - Reporte');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3054,'BSCL.Normativa.BD.Servicios.CIR63::ConsultarTransacciones',0,'P','Normativa -  Transacciones de Relacionados - Reporte');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3055,'BSCL.Normativa.BD.Servicios.CIR63::ConsultarNominaRelacionados',0,'P','Normativa -  Nomina de Relacionados - Reporte');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3056,'BSCL.Normativa.BD.Servicios.UAF::ConsultarPEP',0,'P','Normativa -  Personas Expuestas Políticamente - Reporte');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3063,'BSCL.Normativa.BD.Servicios::CIR.CertificadoCustodia',0,'P','Normativa -  Carta de Circularizacion - Reporte');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3064,'BSCL.Normativa.BD.Servicios::CIR.NotificarCliente',0,'P','Normativa -  Notificacion de Circularizacion - Cliente');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3065,'BSCL.Normativa.BD.Servicios::CIR.ModificarNomina',0,'P','Normativa -  Modificacion de Circularizacion - Nomina');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3066,'BSCL.Normativa.BD.Servicios::CIR.NotificarNomina',0,'P','Normativa -  Notificacion de Circularizacion - Nomina');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3067,'BSCL.Normativa.BD.Servicios::CIR.ActualizarDatosNomina',0,'P','Normativa -  Actualizar circularizacion desde cliente - Nomina');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3068,'BSCL.Normativa.BD.Vistas::NOR.ConsultarEstadoNormativa',0,'Q','Normativa -  Consultar Estados');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3069,'BSCL.Plataforma.BD.Vistas::IND.UIConsultarIndicadoresCliente',0,'Q','Plataforma -  Consultar Indicador cliente');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3070,'BSCL.Normativa.BD.Servicios.CIR::SolicitudCartaCertificadoMasivo',0,'P','Normativa -  Solicitud de Carta Circularizacion');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3071,'BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion',0,'Q','Normativa -  Consultar Configuracion');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3072,'BSCL.Normativa.BD.Servicios.NOR::UAFROE',0,'P','Normativa -  Consultar Operaciones en Efectivo - UAF');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3074,'BSCL.Normativa.BD.Servicios.NOR::UAFDOF',0,'P','Normativa -  Consultar Transacciones con declaración origen de fondos - UAF');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3075,'BSCL.Normativa.BD.Vistas::UAF.UIConsultarDOF',0,'Q','Normativa -  Consultar Transacciones con Declaración Origenes de Fondos');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3076,'BSCL.Normativa.BD.Servicios.UAF::ConsultarDOF',0,'P','Normativa -  Consultar Transacciones con declaración origen de fondos - UAF');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3077,'BSCL.Normativa.BD.Servicios.UAF::ConsultarComprobanteDOF',0,'P','Normativa -  Reporte Compronate con declaración origen de fondos - UAF');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3078,'BSCL.Normativa.BD.Servicios.UAF::EvaluacionDOF',0,'P','Normativa -  Evaluacion de operaciones para DOF - UAF');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3079,'BSCL.Normativa.BD.Vistas::NOR.UIConsultarSolicitudParametros',0,'Q','Normativa -  Consultar parametros de la solicitud');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3080,'BSCL.Normativa.BD.Vistas::NOR.ConsultarSolicitudParametros',0,'Q','Normativa -  Consultar parametros de la solicitud');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3081,'BSCL.Normativa.BD.Vistas::UAF.UIConsultarEvaluacionOperacionSOI',0,'Q','Normativa -  Consultar Evaluacion SOI');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3082,'BSCL.Normativa.BD.Servicios::UAF.NotificarEvaluacionSOI',0,'P','Normativa -  Notificacion de Evaluacion SOI - UAF');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3083,'BSCL.Normativa.BD.Vistas::PRO.UIConsultarEstadisticaOperacionCliente',0,'Q','Normativa -  Consultar Estadistica Operacion Cliente (SOI)');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3084,'BSCL.Normativa.BD.Servicios::NOR.CrearSolicitudCircular',0,'P','Normativa -  Creacion de una solicitud de Circular ');


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(544,'BSCL.Plataforma.BD.Servicios::PLA.ConsultarParidadYTipoCambio',0,'P','Paridad y Tipo Cambio');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3044,'BSCL.FondoMutuo.BD.Servicios::CargarSolicitudesAporteRescate',0,'P','Carga Solicitudes de Aporte/Rescate Fondos Mutuos');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3048,'BSCL.Facturacion.BD.Vistas::ConsultarTipoInstrumentoContratoCobroPeriodico',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3059,'BSCL.Custodia.BD.Servicios::EVC.ModificarFormaDePagoEventoCapitalCliente',0,'P','');


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3208,'BSCL.Normativa.BD.Vistas::DOC.ConsultarDiccionario',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3209,'BSCL.Normativa.BD.Vistas::DOC.ConsultarDocumento',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3210,'BSCL.Normativa.BD.Vistas::DOC.ConsultarConfiguracionMantenedor',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3211,'BSCL.Normativa.BD.Vistas::DOC.ConsultarResumenCarga',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3212,'BSCL.Normativa.BD.Vistas::DOC.ConsultarResumenCuadratura',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3213,'BSCL.Normativa.BD.Vistas::DOC.ConsultarResumenGeneracion',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3214,'BSCL.Normativa.BD.Vistas::DOC.ConsultarResumenValidacion',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3215,'BSCL.Normativa.BD.Vistas::TRI.DJ1891.Datos',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3216,'BSCL.Normativa.BD.Vistas::TRI.DJ1894.Datos',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3217,'BSCL.Normativa.BD.Vistas::TRI.DJ1885.Datos',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3218,'BSCL.Normativa.BD.Vistas::NOR.CI1720.Datos',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3219,'BSCL.Normativa.BD.Vistas::TRI.DJ1885.Invalidos',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3220,'BSCL.Normativa.BD.Vistas::TRI.DJ1891.Invalidos',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3221,'BSCL.Normativa.BD.Vistas::NOR.CI1720.Invalidos',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3222,'BSCL.Normativa.BD.Vistas::DOC.ConsultarCuadratura',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3223,'BSCL.Normativa.BD.Vistas::DOC.ConsultarClasificador',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3233,'BSCL.Normativa.BD.Vistas::EJORD.ConsultarNomina',0,'Q','Normativa Ejecucion Ordenes - Nomina');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3234,'BSCL.Normativa.BD.Vistas::DOC.ConsultarTipoDocumentoUI',0,'Q','Normativa Consultar Tipo Documento UI - Documento');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3235,'BSCL.Normativa.BD.Vistas::EJORD.UIConsultarEjecucionOrdenes',0,'Q','Normativa Consultar Ejecucion Ordenes - Detalle');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3236,'BSCL.Normativa.CrystalReports.Servicios::ConsultarEjecucionOrdenes',0,'P','Normativa Consultar Ejecucion Ordenes - Crystal');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3241,'BSCL.Normativa.BD.Servicios::EJORD.NotificarCliente',0,'P','Normativa -  Notificacion de Ejecucion Ordenes - Cliente');


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3280,'BSCL.Normativa.BD.Vistas::TRI.DJ1820.Datos',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3281,'BSCL.Normativa.BD.Vistas::TRI.DJ1820.Invalidos',0,'Q','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(3282,'BSCL.Normativa.BD.Servicios::TRI.DJ1820.ObtenerTotales',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(3283,'BSCL.Normativa.BD.Servicios::TRI.DJ1820.GrabarDetalle',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(3284,'BSCL.Normativa.BD.Servicios::TRI.DJ1820.EliminarDetalles',0,'P','');


--INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
--VALUES(100002,'BSCL.Normativo.BD::NOR.INFDERECH',0,'P','generador informe derecho llamado por job');
--INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
--VALUES(100003,'BSCL.Normativo.BD::NOR.CrearInformeDerecho',0,'P','generador informe derecho llamado crystal');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(100004,'BSCL.Normativa.BD.Servicios.Anexos::ConsultarDatosAnexoCuatro',0,'P','AnexoCuatro crystal');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(100005,'BSCL.Normativa.BD.Servicios.Anexos::ConsultarDatosAnexoUno',0,'P','AnexoUno crystal');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(100006,'BSCL.Normativa.BD.Servicios.Anexos::ConsultarDatosAnexoTrece',0,'P','AnexoTrece crystal');




/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
