SET SCHEMA "BSCL";

---------------------------------------------
-- NORMALIZA A MAYUSCULAS SIN TILDES
---------------------------------------------
UPDATE "BSCL.Plataforma.BD::PLA.Concepto" SET "Nombre" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Nombre"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Custodia.BD::CUS.TipoEventoCapital" SET "Nombre" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Nombre"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Custodia.BD::CUS.TipoCartera" SET "TipoCartera" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("TipoCartera"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Plataforma.BD::INS.TipoProducto" SET "Nombre" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Nombre"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Plataforma.BD::INS.Bolsa" SET "Nombre" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Nombre"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" SET "Nombre" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Nombre"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" SET "Descripcion" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Descripcion"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Plataforma.BD::INS.Mercado" SET "Descripcion" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Descripcion"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Plataforma.BD::INS.TipoInstrumento" SET "Nombre" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Nombre"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Plataforma.BD::INS.TipoInstrumento" SET "Descripcion" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Descripcion"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Plataforma.BD::INS.TipoOperacion" SET "Nombre" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Nombre"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Plataforma.BD::ACC.Acceso" SET "NombreAcceso" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("NombreAcceso"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Custodia.BD::CUS.EstadoCustodia" SET "Descripcion" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Descripcion"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Plataforma.BD::PLA.Estado" SET "Nombre" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Nombre"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Custodia.BD::GTA.EspecieEnGarantia" SET "Descripcion" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Descripcion"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Orden.BD::ORD.TipoLibroOrden" SET "NombreTipoLibroOrden" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("NombreTipoLibroOrden"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Plataforma.BD::AUT.TipoEvento" SET "Nombre" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Nombre"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Plataforma.BD::BAN.Banco" SET "NombreBanco" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("NombreBanco"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Plataforma.BD::COM.Cobro" SET "NombreCobro" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("NombreCobro"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Plataforma.BD::DOC.TipoDocumento" SET "NombreDocumento" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("NombreDocumento"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Plataforma.BD::DOC.TipoDocumento" SET "Clasificacion" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Clasificacion"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Plataforma.BD::IND.DetalleIndicador" SET "DetalleIndicador" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("DetalleIndicador"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Plataforma.BD::IND.TipoDatoTipoIndicador" SET "NombreTipoDato" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("NombreTipoDato"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Plataforma.BD::LIM.GrupoLimite" SET "Descripcion" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Descripcion"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Plataforma.BD::LIM.TipoLimite" SET "Nombre" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Nombre"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Plataforma.BD::PAG.MedioDePago" SET "Nombre" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Nombre"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Plataforma.BD::PAG.MedioDePago" SET "NombreTipoMedioDePago" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("NombreTipoMedioDePago"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Plataforma.BD::PAG.ModalidadDePago" SET "Nombre" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Nombre"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Plataforma.BD::PLA.Modulo" SET "Nombre" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Nombre"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Plataforma.BD::PLA.Moneda" SET "Nombre" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Nombre"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Plataforma.BD::PLA.TipoRegistro" SET "Descripcion" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Descripcion"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Plataforma.BD::REG.Pais" SET "NombrePais" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("NombrePais"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Plataforma.BD::REQ.CondicionOperativa" SET "Nombre" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Nombre"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Plataforma.BD::REQ.TipoOperacionUsuario" SET "Nombre" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Nombre"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Portafolio.BD::POR.AtributoAnotacion" SET "NombreAtributoAnotacion" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("NombreAtributoAnotacion"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Portafolio.BD::POR.TipoAutorizado" SET "NombreTipoAutorizado" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("NombreTipoAutorizado"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.SocioNegocio.BD::PER.ClasificacionEmpresaFatca" SET "Nombre" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Nombre"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.SocioNegocio.BD::PER.ClasificacionFatca" SET "Nombre" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Nombre"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto" SET "Descripcion" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Descripcion"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.SocioNegocio.BD::PER.TipoMedioContacto" SET "Descripcion" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Descripcion"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.SocioNegocio.BD::PER.TipoTelefono" SET "Nombre" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Nombre"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.SocioNegocio.BD::PRO.AreaProveedor" SET "NombreArea" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("NombreArea"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.SocioNegocio.BD::REL.TipoClasificacionRelacion" SET "NombreClasificacion" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("NombreClasificacion"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.SocioNegocio.BD::REL.TipoRelacion" SET "NombreTipoRelacion" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("NombreTipoRelacion"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio" SET "NombreTipoBloqueo" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("NombreTipoBloqueo"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria" SET "Descripcion" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Descripcion"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Plataforma.BD::PLA.UsoMonedaFormato" SET "Nombre" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Nombre"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Plataforma.BD::INS.TipoRelacionInstrumento" SET "TipoRelacionInstrumento" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("TipoRelacionInstrumento"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Plataforma.BD::PLA.UsoFlujoCaja" SET "Descripcion" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Descripcion"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja" SET "Descripcion" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Descripcion"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Tesoreria.BD::TES.FormatoArchivo" SET "Descripcion" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Descripcion"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Plataforma.BD::REG.DiaFeriado" SET "DescripcionDia" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("DescripcionDia"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U'),'�','.');
UPDATE "BSCL.Plataforma.BD::REG.DiaFeriado" SET "DescripcionDia" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("DescripcionDia"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U'),'A.O','AÑO');
UPDATE "BSCL.Plataforma.BD::REG.DiaFeriado" SET "DescripcionDia" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("DescripcionDia"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U'),'D.A','DIA');
UPDATE "BSCL.Plataforma.BD::REG.DiaFeriado" SET "DescripcionDia" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("DescripcionDia"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U'),'S.BADO','SABADO');

UPDATE "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" SET "Descripcion" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Descripcion"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.FondoMutuo.BD::FMM.Concepto" SET "NombreConcepto" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("NombreConcepto"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U');
UPDATE "BSCL.Plataforma.BD::INS.TipoInstrumentoSubyacente" SET "Nemotecnico" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Nemotecnico"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U'),'SIMULT�NEA','SIMULTANEA');
UPDATE "BSCL.Orden.BD::ORD.TipoCanalDistribucion" SET "Nombre" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("Nombre"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U'),'S.BADO','SABADO');
UPDATE "BSCL.Orden.BD::ORD.TipoEventoBitacora" SET "DescripcionTipoEventoBitacora" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("DescripcionTipoEventoBitacora"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U'),'S.BADO','SABADO');
UPDATE "BSCL.Plataforma.BD::BAN.Plaza" SET "NombrePlaza" = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(UPPER("NombrePlaza"),'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U'),'S.BADO','SABADO');

commit;