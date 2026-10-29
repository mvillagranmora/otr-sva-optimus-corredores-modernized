SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarIndicadoresArchivoAGF";
GO
CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarIndicadoresArchivoAGF"  
AS
SELECT
"IdTipoIndicador"
,"IdTipoEntidad" 
,"IdEmpresa"
,"ValorIndicadorDefecto"
,"ClaseDato"
,"NombreTipoIndicador"
,"CodigoTipoIndicador"
,"GrupoIndicador"
,"IdUso"
,"IdEstado"
,"RequiereAutorizacion"
,"CantidadDecimales"
,"LongitudMinima"
,"LongitudMaxima"
,"IdTipoPersona"
,"RequiereFechaVencimiento"
FROM "BSCL.Plataforma.BD::IND.TipoIndicador"
WHERE "CodigoTipoIndicador" IN ('ADMCART','FINVPRIV','FFMM');
GO
GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarIndicadoresArchivoAGF" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO