INSERT INTO "BSCL.Plataforma.BD::EVT.EventoDirecciones" 
(
  "IdEventoEventoDireccion", 
  "IdEvento", 
  "IdDireccionElectronica", 
  "TipoDestinatario" 			 
)
SELECT
"BSCL.Plataforma.BD.Secuencias::EVT.EventoDirecciones".nextval,
EVE."IdEvento",
CASE WHEN CPGDE."IdGrupoDireccion" IS NOT NULL THEN CPGDEG."IdDireccionElectronica" ELSE EVEDIR."IdDireccion" END AS "IdDireccionElectronica",
EVEDIR."TipoDestinatario"
FROM  "BSCL.Plataforma.BD::GBL.AgregarEvento_EventoDireccion" AS  EVEDIR
LEFT JOIN "BSCL.Plataforma.BD::GBL.AgregarEvento_Evento" AS  EVE on EVEDIR."IdSecuencia" = EVE."IdSecuencia"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.GrupoDireccion" CPGDE ON CPGDE."IdPersona" = EVE."IdPersonaDestinatario" AND CPGDE."IdGrupoDireccion" = EVEDIR."IdDireccion"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.GrupoDireccionDetalle" CPGDEG ON CPGDEG."IdGrupoDireccion" = CPGDE."IdGrupoDireccion"
INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle" AS CONF ON (CONF."CodigoTipoEvento" = EVE."CodigoTipoEvento" AND CONF."IdEstado" = 'ACT');
