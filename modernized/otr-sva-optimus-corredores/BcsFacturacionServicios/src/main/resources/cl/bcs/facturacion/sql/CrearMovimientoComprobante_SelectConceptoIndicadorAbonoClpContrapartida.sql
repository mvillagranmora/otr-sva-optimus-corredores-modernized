SELECT 
	CNP."IdConcepto",
	CNP."Nombre",
	CNP."IdEstado",
	FAC."Contrapartida",
	FAC."AbonoClpContrapartida"
FROM "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" FAC
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" CNP ON CNP."IdConcepto" = FAC."IdConcepto" 
WHERE FAC."AbonoClpContrapartida"='S'