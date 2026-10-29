SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConceptoAbonoClpContrapartida"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConceptoAbonoClpContrapartida" as
SELECT 
	CNP."IdConcepto",
	CNP."Nombre",
	CNP."IdEstado",
	FAC."Contrapartida",
	FAC."AbonoClpContrapartida"
FROM "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" FAC
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" CNP ON CNP."IdConcepto" = FAC."IdConcepto" 
WHERE FAC."AbonoClpContrapartida"='S'
WITH READ ONLY;
GO
GRANT SELECT on "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConceptoAbonoClpContrapartida" to BCS_DESARROLLADOR with grant option;
GO
