SET SCHEMA "BSCL"; 
--GO 
DROP VIEW "BSCL.Homologacion.BD.Vistas::HomologaIndicadoresCliente";
--GO
create view "BSCL.Homologacion.BD.Vistas::HomologaIndicadoresCliente" as
SELECT
	PER."IdPersona" AS per_id,--int] NOT NULL,********
	OPT.ind_id,--int
	IND."ValorIndicador" as din_id  --char
FROM "BSCL.SocioNegocio.BD::PER.Persona"  AS PER
JOIN "BSCL.Plataforma.BD::IND.Indicador"  AS IND ON IND."IdTipoEntidad" = 'CLIENTE' AND IND."IdEntidad" = PER."IdPersona" 
JOIN "BSCL.Homologacion.BD.Vistas::HomologaIndicador" AS OPT ON OPT.OPTIMUS_ID = IND."IdTipoIndicador"
;
--GO
GRANT SELECT ON "BSCL.Homologacion.BD.Vistas::HomologaIndicadoresCliente" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
--GO