UPDATE "BSCL.Custodia.BD::CUS.SorteoContratos"
SET "NumeroReferencia" = :NumeroReferencia, "IdEstado" = 'PRO'
WHERE 
	"PortafolioDerivados"	= :IdPortafolio   AND 
	"Fecha" 				= :Fecha
