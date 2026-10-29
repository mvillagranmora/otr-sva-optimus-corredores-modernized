WITH carterasCliente AS (
	SELECT
		CAR."TasaCompra",
		CAR."Nemotecnico",
		CAR."NemoSVS",
		CAR."IdEmisor",
		CAR."IdClasificacionTipoInstrumento",
		CAR."INGPrecioCompra"
	FROM 
		"BSCL.Custodia.BD::CUS.Cartera" AS CAR
	WHERE
		CAR."IdEstado" = 'VIG'
		AND "IdClasificacionTipoInstrumento" IN ('RF', 'IF')
		AND "IdTipoInstrumento" NOT IN ('SIM', 'PAC', 'PRT', 'EMP', 'FFM', 'PRF')
		AND "FolioCartera" != 0
		AND CAR."IdCliente" = :IdCliente
		AND (CAR."IdPortafolio" = :IdPortafolio OR 0 = :IdPortafolio)
		AND CAR."INGFechaOperacion" <= :FechaConsulta
	UNION ALL
	SELECT
		CAR."TasaOperacion" AS "TIRCompra",
		CAR."Nemotecnico" ,
		CAR."NemoSVS" ,
		CAR."IdEmisor" , 
		TINS."IdClasificacionTipoInstrumento",
		CAR."PrecioOperacion" AS "PrecioCompra"
	FROM
		"BSCL.Custodia.BD::CUS.CarteraNegativa" AS car
	LEFT JOIN 
		"BSCL.Plataforma.BD::INS.Instrumento" ins ON ins."IdInstrumento" = car."IdInstrumento"
	LEFT JOIN 
		"BSCL.Plataforma.BD::INS.TipoInstrumento" tins ON tins."IdTipoInstrumento" = ins."IdTipoInstrumento"
	WHERE
		CAR."IdEstado" = 'VIG'
		AND "IdClasificacionTipoInstrumento" IN ('RF', 'IF')
		AND CAR."IdCliente" = :IdCliente
		AND (CAR."IdPortafolio" = :IdPortafolio OR 0 = :IdPortafolio)
		AND CAR."FechaOperacion" <= :FechaConsulta
	UNION ALL
	SELECT
		S."TasaSubyacente",
		S."Nemotecnico" ,
		S."NemoSVS" ,
		S."IdEmisor" ,
		S."IdClasificacionTipoInstrumento",
		S."PrecioSubyacente"
	FROM
		"BSCL.Custodia.BD::CUS.Subyacente" AS S 
	INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS C ON S."FolioCarteraRelacionada" = C."FolioCartera"
	WHERE
		S."IdEstado" = 'VIG'
		AND c."IdEstado" = 'VIG'
		AND S."IdClasificacionTipoInstrumento" IN ('RF', 'IF')
		AND C."IdTipoInstrumento" IN ('PAC')
		AND C."IdTipoOperacion" != 'VENPAC'
		AND C."IdCliente" = :IdCliente
		AND (C."IdPortafolio" = :IdPortafolio OR 0 = :IdPortafolio)
		AND C."FechaOperacion" <= :FechaConsulta
	UNION ALL
	SELECT
		S."TasaSubyacente",
		S."Nemotecnico" ,
		S."NemoSVS" ,
		S."IdEmisor" ,
		S."IdClasificacionTipoInstrumento",
		S."PrecioSubyacente"
	FROM
		"BSCL.Custodia.BD::CUS.Subyacente" AS S
	INNER JOIN 
		"BSCL.Custodia.BD::CUS.Cartera" AS C ON S."FolioCarteraRelacionada" = C."FolioCartera"
	WHERE
		S."IdEstado" = 'VIG'
	AND c."IdEstado" = 'VIG'
	AND S."IdClasificacionTipoInstrumento" IN ('RF', 'IF')
	AND C."IdTipoOperacion" IN ('VENPAC')
	AND C."IdCliente" = :IdCliente
	AND (C."IdPortafolio" = :IdPortafolio OR 0 = :IdPortafolio)
	AND C."FechaOperacion" <= :FechaConsulta
	UNION ALL	
	SELECT
		G."TasaCompraOperacionOrigen" AS "TIRCompra",
		G."Nemotecnico" ,
		G."NemoSVS" ,
		G."IdEmisor" ,
		G."IdClasificacionTipoInstrumento" ,
		G."PrecioCompraOperacionOrigen" AS "PrecioCompra"
	FROM
		"BSCL.Custodia.BD::GTA.Garantia" AS G
	WHERE
		G."IdEstado" = 'VIG'
	AND G."IdClasificacionTipoInstrumento" IN ('RF', 'IF')
	AND G."IdCliente" = :IdCliente
	AND (G."IdPortafolio" = :IdPortafolio OR 0 = :IdPortafolio)
	AND G."FechaOperacion" <= :FechaConsulta
	UNION ALL
	SELECT
		"TasaCompra" AS "TIRCompra",
		t."Nemotecnico" ,
		t."NemoSVS" ,
		t."IdEmisor" ,
		t."IdClasificacionTipoInstrumento" ,
		t."PrecioCompra"
	FROM
		"BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS t
	INNER JOIN 
		"BSCL.Plataforma.BD::INS.TipoOperacion" AS o ON (t."IdTipoOperacion" = o."IdTipoOperacion" AND "AccionCustodia" = 'RETIRO')
	WHERE
		"IdEstado" = 'PEN'
		AND "IdClasificacionTipoInstrumento" IN ('RF', 'IF')
		AND t."IdCliente" = :IdCliente
		AND (t."IdPortafolio" = :IdPortafolio OR 0 = :IdPortafolio)
		AND t."FechaOperacion" <= :FechaConsulta
)SELECT
	CASE 
		WHEN "IdClasificacionTipoInstrumento" = 'RF' THEN AVG(DISTINCT "TasaCompra") 
		ELSE 0
	END AS "TIRCompra", 
	CASE 
        WHEN "IdClasificacionTipoInstrumento" <> 'RF' THEN AVG("TasaCompra")
        ELSE 0
    END AS "TasaCompraCRM", 
	"Nemotecnico",
	"NemoSVS",
	"IdEmisor",
	"IdClasificacionTipoInstrumento"
FROM
	carterasCliente
GROUP BY 
	"Nemotecnico",
	"NemoSVS",
	"IdEmisor",
	"IdClasificacionTipoInstrumento";