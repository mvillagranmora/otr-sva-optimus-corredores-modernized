SET SCHEMA "BSCL";

 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ObtenerRelacionPersona";
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ObtenerRelacionPersona"
AS

---LECTURA DE IDA A LA RELACION

SELECT
  ORI."IdPersona"
, ORI."IdPersonaDestino"
, ORI."IdTipoRelacion"
, ORI."Administracion"
, ORI."FechaInicioRelacion"
, ORI."FechaFinRelacion"
, ORI."IdRelacionPersona"
, ORI."IdEstado"
, ORI."PorcentajeParticipacion"
, ORI."FinalidadRelacion"
, ORI."CondicionesRelacion"
, ORI."IdGradoVinculacionParental"
, ORI."FechaModificacion"
FROM "BSCL.SocioNegocio.BD.Vistas::REL.ObtenerRelacionOrigen" ORI

UNION ALL

---LECTURA DE VUELTA A LA RELACION	
SELECT
  DES."IdPersona"
, DES."IdPersonaDestino"
, DES."IdTipoRelacion"
, DES."Administracion"
, DES."FechaInicioRelacion"
, DES."FechaFinRelacion"
, DES."IdRelacionPersona"
, DES."IdEstado"  
, DES."PorcentajeParticipacion"
, DES."FinalidadRelacion"
, DES."CondicionesRelacion"
, DES."IdGradoVinculacionParental"
, DES."FechaModificacion"
FROM "BSCL.SocioNegocio.BD.Vistas::REL.ObtenerRelacionDestino" DES;			
			
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::REL.ObtenerRelacionPersona" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
