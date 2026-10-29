SET SCHEMA "BSCL";
GO

DROP VIEW "BSCL.APV.BD.Vistas::ConsultarDistribucionFondosSAAPV";
GO

CREATE VIEW "BSCL.APV.BD.Vistas::ConsultarDistribucionFondosSAAPV" AS
SELECT
	CON."IdCliente"
   ,CON."IdPersona"
   ,CON."DocumentoIdentidad"
   ,CON."TipoCuenta"
   ,CON."IdContratoSAAPV"
   ,CON."IdPortafolio"
   ,CON."FolioContratoSAAPV"
   ,CON."IdInstrumento"
   ,CON."Nemotecnico"
   ,CON."CodigoFondo"
   ,CON."Serie"
   ,CON."MontoParcial"
   ,CON."PorcentajeParcial"
   ,CON."NombreFondo"
   ,CON."IdEstado"
FROM
(
	SELECT
		ContratoSAAPV."IdCliente"																AS "IdCliente"
	   ,ContratoSAAPV."IdPersona"																AS "IdPersona"
	   ,DocumentoIdentidad."DocumentoIdentidad"													AS "DocumentoIdentidad"
	   ,RegimenTributario."Codigo"																AS "TipoCuenta"
	   ,ContratoSAAPV."IdContratoSAAPV"															AS "IdContratoSAAPV"
	   ,Portafolio."IdPortafolio"																AS "IdPortafolio"
	   ,ContratoSAAPV."FolioContratoSAAPV"														AS "FolioContratoSAAPV"
	   ,Instrumento."IdInstrumento"																AS "IdInstrumento"
	   ,Instrumento."Nemotecnico"																AS "Nemotecnico"
	   ,Instrumento."IdInstrumento"																AS "CodigoFondo"
	   ,SerieDeFondo."CodigoSerie"																AS "Serie"
	   ,DistribucionInversion."MontoPesos"														AS "MontoParcial"
	   ,DistribucionInversion."Porcentaje"														AS "PorcentajeParcial"
	   ,Instrumento."RazonSocial"																AS "NombreFondo"
	   ,DistribucionInversion."IdEstado"														AS "IdEstado"
	FROM "BSCL.APV.BD::SCS.ContratoSAAPV" AS ContratoSAAPV
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Persona ON Persona."IdPersona" = ContratoSAAPV."IdPersona"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidad ON DocumentoIdentidad."IdDocumentoIdentidad" = Persona."IdDocumentoIdentidad"
	LEFT JOIN "BSCL.APV.BD::SCS.MedioRecaudacion" AS MedioRecaudacion ON MedioRecaudacion."IdMedioRecaudacion" = ContratoSAAPV."IdMedioRecaudacionTrabajador"
	LEFT JOIN "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" AS PortafolioContratoSAAPV ON PortafolioContratoSAAPV."IdContratoSAAPV" = ContratoSAAPV."IdContratoSAAPV"
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = PortafolioContratoSAAPV."IdPortafolio"
	LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
	LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" AS RegimenTributario ON RegimenTributario."IdRegimenTributario" = MatrizAgrupador."IdRegimenTributario"
	LEFT JOIN "BSCL.APV.BD::SCS.DistribucionInversion" AS DistribucionInversion ON DistribucionInversion."IdContratoSAAPV" = ContratoSAAPV."IdContratoSAAPV"
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento ON Instrumento."IdInstrumento" = DistribucionInversion."IdInstrumento"
	LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SerieDeFondo ON SerieDeFondo."IdInstrumento" = Instrumento."IdInstrumento"
	WHERE Portafolio."IdEstado" <> 'INA'
)
AS CON
ORDER BY
	CON."IdContratoSAAPV"
   ,CON."IdPortafolio";
GO

GRANT SELECT ON "BSCL.APV.BD.Vistas::ConsultarDistribucionFondosSAAPV" TO BCS_DESARROLLADOR WITH GRANT OPTION;
GO