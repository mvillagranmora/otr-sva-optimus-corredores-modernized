UPDATE "BSCL.Portafolio.BD::POR.Portafolio"
SET "CodigoPortafolio"		= :p_CodigoPortafolio,
	"IdMatrizAgrupadorAPV"	= :p_IdMatrizAgrupadorAPV
WHERE "IdPortafolio" = :p_IdPortafolio;