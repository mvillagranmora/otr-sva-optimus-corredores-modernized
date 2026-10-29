SELECT
    Operacion."IdOperacion"         	AS "IdOperacionFwd"
    , Operacion."IdCliente"             AS "IdCliente"
    , Operacion."IdPortafolio"          AS "IdPortafolio"
    , Operacion."IdAgente"              AS "IdAgenteOperacion"
    , Operacion."IdTipoProducto"        AS "IdTipoProducto"
    , Operacion."IdEstado"              AS "IdEstado"
    , Operacion."EstadoConfirmacion"    AS "EstadoConfirmacion"
    , Operacion."IdInstrumento"         AS "IdInstrumento"
    , Instrumento."IdTipoInstrumento"   AS "IdTipoInstrumento"
    , Instrumento."Nemotecnico"         AS "NemotecnicoInstrumento"
    , OperadorEmpleado."CodigoEmpleado" AS "CodigoOperador"
    , Operacion."IdTipoCanalIngreso"    AS "IdTipoCanalIngreso"
    , Operacion."IdTipoOperacion"       AS "IdTipoOperacion"
    , Operacion."IdMonedaPrincipal"     AS "IdMonedaPrincipal"
    --, Operacion."MontoOperacion" 		AS "MontoOperacion"
    --, TABLAFWDDISPONIBLE."MontoDisponible" AS "MontoDisponible"
    , COALESCE (TABLAFWDDISPONIBLE."MontoDisponible", Operacion."MontoOperacion") AS "MontoOperacion"
    , Operacion."FechaOperacion" 		AS "FechaOperacion"
    , Operacion."FechaLiquidacion" 		AS "FechaLiquidacion"
    , Operacion."PlazoEnDias" 			AS "PlazoDias"
FROM "BSCL.Mesa.BD::MES.Operacion" Operacion
	INNER JOIN (
		SELECT
		      Cliente."IdCliente"
		FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" Empresa
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" Cliente ON (Cliente."IdPersona" = Empresa."IdPersona")
		WHERE Empresa."IdEstado" = 'ACT' AND Cliente."IdEstado" = 'ACT'
	) AS CTECUENTAPROPIA
	ON CTECUENTAPROPIA."IdCliente" = Operacion."IdCliente"
    INNER JOIN "BSCL.Plataforma.BD::ACC.Usuario" OperadorUsuario ON (OperadorUsuario."IdUsuario" = Operacion."IdUsuarioCreacion")
    INNER JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" OperadorEmpleado ON (OperadorEmpleado."IdEmpleado" = OperadorUsuario."IdEntidad")
    INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" Instrumento ON (Instrumento."IdInstrumento" = Operacion."IdInstrumento")
    INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraLite" CAR ON (Operacion."IdOperacion" = CAR."IdOperacion" AND Operacion."IdTipoProducto" = CAR."IdTipoProducto" AND CAR."IdEstado" = 'VIG')     
    LEFT JOIN (
		SELECT
			CUS."FolioCartera", SUM(RES."IngCantidad") AS "MontoCalce", CUS."Cantidad" AS "MontoOperacion",
			ABS(SUM(RES."IngCantidad") - CUS."Cantidad") AS "MontoDisponible"
		FROM "BSCL.Custodia.BD::CUS.Reserva" AS RES
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CUS
			ON (CUS."IdOperacion" = RES."IdOperacion" AND RES."IdTipoProducto" = CUS."IdTipoProducto" AND CUS."IdEstado" = 'VIG')
		WHERE RES."IdTipoProducto" = 'MEFW'
		GROUP BY CUS."FolioCartera", CUS."Cantidad"
		) AS TABLAFWDDISPONIBLE
	ON TABLAFWDDISPONIBLE."FolioCartera" = CAR."FolioCartera"	
WHERE Operacion."IdTipoCanalIngreso" IN (:IdTipoCanalIngreso)
	AND Operacion."IdTipoProducto" = 'MEFW'
    AND OperadorUsuario."TipoEntidad" = 'EMP'
   -- AND (Operacion."FechaOperacion" BETWEEN :FechaOperacionDesde AND :FechaOperacionHasta)
    AND Operacion."FechaOperacion" >= :FechaOperacion
    AND Operacion."IdEstado" = :IdEstado;