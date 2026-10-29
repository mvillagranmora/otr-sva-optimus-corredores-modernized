SELECT 
	TEMP."IdRecaudacionCliente" 
	, TEMP."RutCliente"
	, TEMP."MontoRecaudacion" 
	, TEMP."TipoRegimen" 
	, TEMP."Regimen" 
	, TEMP."RutEmpleador"
	, COALESCE(CSAAPV."IdContratoSAAPV", 0) AS "IdContratoSAAPV"
	, COALESCE(CSAAPV."FolioContratoSAAPV", '') AS "FolioContratoSAAPV"
	, MEDIORECAUDACION."CodigoMedioRecaudacion"
FROM "#ValidarYAsignarContratosARecaudaciones_Recaudaciones" AS TEMP
INNER JOIN "BSCL.APV.BD.Vistas::APV.ConsultarIdContratoSAAPV" AS CSAAPV
	ON CSAAPV."RutCliente" = TEMP."RutCliente"
	AND CSAAPV."Regimen" = TEMP."Regimen"
	AND CSAAPV."IdMedioRecaudacionFormaPago" <> 3
INNER JOIN "BSCL.APV.BD.Vistas::AFI.ConsultarGestionContratosSAAPV" AS GEST
	ON GEST."IdContratoSAAPV" = CSAAPV."IdContratoSAAPV"
	AND GEST."MontoPesos" = TEMP."MontoRecaudacion"
	AND GEST."RutEmpleador" = TEMP."RutEmpleador"
	AND GEST."OrigenAhorro" = TEMP."TipoRegimen"
LEFT JOIN "BSCL.APV.BD::SCS.MedioRecaudacion" AS MEDIORECAUDACION
	ON MEDIORECAUDACION."IdMedioRecaudacion" = CSAAPV."IdMedioRecaudacionFormaPago"
	;