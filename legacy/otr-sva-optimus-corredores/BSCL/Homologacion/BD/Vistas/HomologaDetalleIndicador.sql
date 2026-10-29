SET SCHEMA "BSCL"; 
--GO 
DROP VIEW "BSCL.Homologacion.BD.Vistas::HomologaDetalleIndicador";
--GO
create view "BSCL.Homologacion.BD.Vistas::HomologaDetalleIndicador" as
SELECT
	OPT.ind_id, --int 
	IND."ValorIndicador" as din_id,  --char
	DET."IdDetalleIndicador" AS din_valor
FROM "BSCL.Plataforma.BD::IND.Indicador"  AS IND 
JOIN "BSCL.Homologacion.BD.Vistas::HomologaIndicador" AS OPT ON OPT.OPTIMUS_ID = IND."IdTipoIndicador"
JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador" AS DET ON DET."IdTipoIndicador" = IND."IdTipoIndicador"
;
--GO
GRANT SELECT ON "BSCL.Homologacion.BD.Vistas::HomologaDetalleIndicador" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
--GO