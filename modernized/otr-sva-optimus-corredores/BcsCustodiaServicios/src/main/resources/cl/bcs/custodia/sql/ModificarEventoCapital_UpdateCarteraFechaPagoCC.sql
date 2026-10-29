UPDATE "BSCL.Custodia.BD::CUS.Cartera"
SET
    "INGFechaPagoCorteCupon"        = :FechaPago
WHERE
    	"IdInstrumento" = :IdInstrumento 
    AND "INGFechaPagoCorteCupon" = :FechaPagoOld
	AND "IdEstado" IN ('VIG', 'LIQ', 'RPA')