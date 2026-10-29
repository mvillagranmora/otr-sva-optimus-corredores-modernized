SELECT
	FEC."Fecha" AS "FechaVencimiento",
	IFNULL(FER."Tipo", 'HABIL') AS "TipoDia",
	FEC."IdTrabajo" AS "IdTrabajo"
FROM
	"BSCL.Mesa.BD::GBL.FechasConsulta" FEC
LEFT JOIN "BSCL.Plataforma.BD::REG.DiaFeriado" FER 
ON
	((UPPER(FER."Tipo") = 'FERIADO'
		AND FER."Fecha" = FEC."Fecha")
	OR
	(UPPER(FER."Tipo") = 'INHABIL'
		AND FER."Dia" = WEEKDAY(FEC."Fecha")))
	AND FER."AgnoCarga" = YEAR(FEC."Fecha")
	AND FER."IdEstado" = 'ACT'
	AND FER."AgnoCarga" = YEAR(FEC."Fecha")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMP ON
	EMP."IdPais" = FER."IdPais"
WHERE
	EMP."IdEmpresa" = :IdEmpresa
	AND FEC."IdTrabajo" = :IdTrabajo;