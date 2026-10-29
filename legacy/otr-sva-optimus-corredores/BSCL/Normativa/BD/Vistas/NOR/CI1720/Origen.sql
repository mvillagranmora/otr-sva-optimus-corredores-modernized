SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::NOR.CI1720.Origen";

CREATE VIEW "BSCL.Normativa.BD.Vistas::NOR.CI1720.Origen"  AS
SELECT
	CUA."IdEmpresa",
	CUA."Nemotecnico",
	COALESCE(INS."RazonSocial", TO_VARCHAR('')) AS "NombreEmisor",
	CUA."IdTipoInstrumento" 		AS "TipoInstrumento",
	CUA."CantidadCustodia" 			AS "CantidadUnidadesNominal",
	TO_VARCHAR('') 					AS "TipoUnidades",
	TO_BIGINT (0)					AS "Valorizacion",
	CUA."CantidadCuentaPropia" 		AS "CarteraPropia",
	CUA."CantidadTerceros" 			AS "CustodiaTerceros",
	CUA."CantidadDCVCuentaPropia" 	AS "CustodiaPropiaDCV",
	CUA."CantidadDCVTerceros" 		AS "CustodiaTercerosDCV",
	CUA."CantidadEmisores" 			AS "CustodiaSociedadEmisora",
	CUA."CantidadIntermediario" 	AS "CustodiaIntermediario",
	(CUA."CantidadBCS"+ CUA."CantidadBEC"+ CUA."CantidadBOVALPO")	AS "CustodiaBolsas",
	CUA."CantidadCustodia" - (CUA."CantidadCuentaPropia"+ CUA."CantidadTerceros" + CUA."CantidadDCVCuentaPropia" + CUA."CantidadDCVTerceros" + CUA."CantidadEmisores" + CUA."CantidadIntermediario"  ) AS "CustodiaOtros",
	CUA."FechaCuadraturaCustodia" 	AS "FechaRegistro",
	TO_VARCHAR('')					AS "Usuario"
 FROM  "BSCL.Normativa.BD::PRO.CuadraturaCustodia" CUA
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS ON CUA."IdInstrumento" = INS."IdInstrumento";

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::NOR.CI1720.Origen" TO bcs_desarrollador WITH GRANT OPTION;
