SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarEnrolamientoFatcaPaisResidencia"; 
GO
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarEnrolamientoFatcaPaisResidencia"
AS
SELECT	FPR."IdEnrolamiento",
		FPR."IdPaisResidencia",
		PAIS."NombrePais" AS "PaisResidencia",
		FPR."TIN",
		FPR."IdCausaNoTin",
		CASE WHEN FPR."IdCausaNoTin" = 'OTRA' THEN FPR."Observacion" ELSE CNT."Descripcion" END AS "CausaNoTin",
		FPR."PaisResidenciaPrincipal"
FROM "BSCL.SocioNegocio.BD::ENR.FatcaPaisResidencia" FPR
INNER JOIN "BSCL.Plataforma.BD::REG.Pais" PAIS ON PAIS."IdPais" = FPR."IdPaisResidencia"
LEFT JOIN "BSCL.Plataforma.BD::PLA.CausaNoTin" CNT ON CNT."IdCausaNoTin" = FPR."IdCausaNoTin";
GO
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarEnrolamientoFatcaPaisResidencia" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
