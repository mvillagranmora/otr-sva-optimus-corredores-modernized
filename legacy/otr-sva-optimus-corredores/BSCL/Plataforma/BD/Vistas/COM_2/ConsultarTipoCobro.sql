SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarTipoCobro"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarTipoCobro"  
AS

SELECT 
	"IdTipoCobro","NombreTipoCobro","PermiteMontoFijo","PermitePorcentajeFijo","PermitePorcentajeRangos"
	FROM "BSCL.Plataforma.BD::COM_2.TipoCobro";


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarTipoCobro" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
