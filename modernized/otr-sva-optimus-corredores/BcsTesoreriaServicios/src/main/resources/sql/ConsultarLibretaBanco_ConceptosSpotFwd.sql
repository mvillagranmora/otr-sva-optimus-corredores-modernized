SELECT 
"IdConcepto",
"Origen" 
FROM "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" 
WHERE "Origen"IN ('MFW','MSP');