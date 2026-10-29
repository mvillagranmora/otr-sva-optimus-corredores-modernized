WITH
	CteOperador AS (
		SELECT
			Usuario."IdUsuario"
			, Empleado."CodigoEmpleado"
		FROM
			"BSCL.Plataforma.BD::ACC.Usuario" Usuario
			INNER JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" Empleado ON (Empleado."IdEmpleado" = Usuario."IdEntidad")
		WHERE
			Usuario."TipoEntidad" = 'EMP'
	)
	, CteOrdenOperacionIntradayAnterior AS (
		SELECT
			RANK() OVER(ORDER BY Operacion."FechaOperacion" ASC, Operacion."IdOperacionSpot" ASC) AS "OrdenOperacion"
			, Operacion."IdOperacionSpot"
			, Operacion."FechaOperacion"
		FROM
			"BSCL.Mesa.BD::SPT.OperacionSpot" Operacion
		WHERE
				TO_DATE(Operacion."FechaOperacion") = TO_DATE(:FechaOperacionDesde)
			AND Operacion."IdTipoSpot" = 'INTRADAY'
   )
	, CteOperacionOvernightAnterior AS (
		SELECT
			Operacion."IdOperacionSpot"         AS "IdOperacionSpot"
			, Operacion."IdCliente"             AS "IdCliente"
			, Operacion."IdPortafolio"          AS "IdPortafolio"
			, Operacion."IdMesa"                AS "IdMesa"
			, Operacion."IdAgente"              AS "IdAgenteOperacion"
			, Operacion."IdTipoSpot"            AS "IdTipoSpot"
			, Operacion."Estado"                AS "Estado"
			, Operacion."EstadoConfirmacion"    AS "EstadoConfirmacion"
			, Operacion."IdInstrumento"         AS "IdInstrumento"
			, Instrumento."IdTipoInstrumento"   AS "IdTipoInstrumento"
			, Instrumento."Nemotecnico"         AS "NemotecnicoInstrumento"
			, Operador."CodigoEmpleado"         AS "CodigoOperador"
			, Operacion."IdTipoCanalIngreso"    AS "IdTipoCanalIngreso"
			, Operacion."IdTipoOperacion"       AS "IdTipoOperacion"
			, Operacion."IdMonedaPrincipal"     AS "IdMonedaPrincipal"
			, Operacion."MontoMonedaPrincipal"  AS "MontoPrincipal"
		FROM
			"BSCL.Mesa.BD::SPT.OperacionSpot" Operacion
			INNER JOIN CteOperador Operador ON (Operador."IdUsuario" = Operacion."IdUsuario")
			INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" Instrumento ON (Instrumento."IdInstrumento" = Operacion."IdInstrumento")
			INNER JOIN CteOrdenOperacionIntradayAnterior PrimeraIntraday ON (PrimeraIntraday."OrdenOperacion" = 1) -- Marca apertura de mesa, asi ignoramos operaciones Overnight que suman para el dia anterior a este.
		WHERE
				(Operacion."FechaOperacion" >= PrimeraIntraday."FechaOperacion" AND TO_DATE(Operacion."FechaOperacion") < TO_DATE(:FechaOperacionHasta))
			AND Operacion."IdTipoSpot" = 'OVERNIGHT'
   )
	, CteOperacionHoy AS (
		SELECT
			Operacion."IdOperacionSpot"         AS "IdOperacionSpot"
			, Operacion."IdCliente"             AS "IdCliente"
			, Operacion."IdPortafolio"          AS "IdPortafolio"
			, Operacion."IdMesa"                AS "IdMesa"
			, Operacion."IdAgente"              AS "IdAgenteOperacion"
			, Operacion."IdTipoSpot"            AS "IdTipoSpot"
			, Operacion."Estado"                AS "Estado"
			, Operacion."EstadoConfirmacion"    AS "EstadoConfirmacion"
			, Operacion."IdInstrumento"         AS "IdInstrumento"
			, Instrumento."IdTipoInstrumento"   AS "IdTipoInstrumento"
			, Instrumento."Nemotecnico"         AS "NemotecnicoInstrumento"
			, Operador."CodigoEmpleado"         AS "CodigoOperador"
			, Operacion."IdTipoCanalIngreso"    AS "IdTipoCanalIngreso"
			, Operacion."IdTipoOperacion"       AS "IdTipoOperacion"
			, Operacion."IdMonedaPrincipal"     AS "IdMonedaPrincipal"
			, Operacion."MontoMonedaPrincipal"  AS "MontoPrincipal"
		FROM
			"BSCL.Mesa.BD::SPT.OperacionSpot" Operacion
			INNER JOIN CteOperador Operador ON (Operador."IdUsuario" = Operacion."IdUsuario")
			INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" Instrumento ON (Instrumento."IdInstrumento" = Operacion."IdInstrumento")
		WHERE
			Operacion."FechaOperacion" BETWEEN (TO_NVARCHAR(:FechaOperacionHasta, 'YYYY-MM-DD') || ' 00:00:00') AND :FechaOperacionHasta
	 )
SELECT
	Operacion."IdOperacionSpot"          AS "IdOperacionSpot"
	, Operacion."IdCliente"              AS "IdCliente"
	, Operacion."IdPortafolio"           AS "IdPortafolio"
	, Operacion."IdMesa"                 AS "IdMesa"
	, Operacion."IdAgenteOperacion"      AS "IdAgenteOperacion"
	, Operacion."IdTipoSpot"             AS "IdTipoSpot"
	, Operacion."Estado"                 AS "Estado"
	, Operacion."EstadoConfirmacion"     AS "EstadoConfirmacion"
	, Operacion."IdInstrumento"          AS "IdInstrumento"
	, Operacion."IdTipoInstrumento"      AS "IdTipoInstrumento"
	, Operacion."NemotecnicoInstrumento" AS "NemotecnicoInstrumento"
	, Operacion."CodigoOperador"         AS "CodigoOperador"
	, Operacion."IdTipoCanalIngreso"     AS "IdTipoCanalIngreso"
	, Operacion."IdTipoOperacion"        AS "IdTipoOperacion"
	, Operacion."IdMonedaPrincipal"      AS "IdMonedaPrincipal"
	, Operacion."MontoPrincipal"         AS "MontoPrincipal"
	, 'BD'                               AS "Origen"
FROM
(
	SELECT
	 	"IdOperacionSpot", "IdCliente", "IdPortafolio", "IdMesa", "IdAgenteOperacion"
	 	, "IdTipoSpot", "Estado", "EstadoConfirmacion", "IdInstrumento", "IdTipoInstrumento"
	 	, "NemotecnicoInstrumento", "CodigoOperador", "IdTipoCanalIngreso", "IdTipoOperacion"
	 	, "IdMonedaPrincipal", "MontoPrincipal" 
	FROM CteOperacionOvernightAnterior
	UNION ALL
	SELECT
		"IdOperacionSpot", "IdCliente", "IdPortafolio", "IdMesa", "IdAgenteOperacion"
	 	, "IdTipoSpot", "Estado", "EstadoConfirmacion", "IdInstrumento", "IdTipoInstrumento"
	 	, "NemotecnicoInstrumento", "CodigoOperador", "IdTipoCanalIngreso", "IdTipoOperacion"
	 	, "IdMonedaPrincipal", "MontoPrincipal"
	FROM CteOperacionHoy
) Operacion
WHERE
		Operacion."Estado" = :Estado
	AND Operacion."IdCliente" = :IdCliente;
