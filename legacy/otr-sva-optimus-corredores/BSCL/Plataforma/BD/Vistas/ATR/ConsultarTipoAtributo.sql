SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::ATR.ConsultarTipoAtributo";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::ATR.ConsultarTipoAtributo" as
SELECT 
   "IdTipoAtributo",
   "IdTipoEntidad",
   "IdEmpresa",
   "ClaseDato",
   "NombreTipoAtributo",
   "GrupoAtributo",
   "IdEstado",
   "RequiereAutorizacion",
   CASE "EsPreferencia" WHEN 'S' THEN 'Preferencia' ELSE 'Atributo' END as "EsPreferencia"
FROM "BSCL.Plataforma.BD::PLA.TipoAtributo";


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::ATR.ConsultarTipoAtributo" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
 