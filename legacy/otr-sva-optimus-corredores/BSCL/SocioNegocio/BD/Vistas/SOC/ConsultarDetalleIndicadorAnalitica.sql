SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarDetalleIndicadorAnalitica"; 
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarDetalleIndicadorAnalitica" AS
SELECT   "IdDetalleIndicador" 
        ,"IdTipoIndicador"
        ,CAST("DetalleIndicador" AS NVARCHAR(255)) AS "ValorIndicador"
        ,CAST("Codigo" AS NVARCHAR(40)) AS  "Codigo"
FROM "BSCL.Plataforma.BD::IND.DetalleIndicador";

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarDetalleIndicadorAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
