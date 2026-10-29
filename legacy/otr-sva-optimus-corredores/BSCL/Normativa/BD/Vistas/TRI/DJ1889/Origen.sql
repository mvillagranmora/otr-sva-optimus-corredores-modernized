SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1889.Origen";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1889.Origen" (
    "IdEmpresa",
    "Numero",
    "RutCliente",
    "IdCliente",
    "FechaOperacion",
    "TipoAhorro",
    ---
    "MontoNominal",
    --
    "RentabilidadPositiva",
    "RentabilidadNegativa",
    --Actualizada ($)
    "Factor",
    "RentabilidadActualizadaPositiva",
    "RentabilidadActualizadaNegativa",
    --
    "NumeroCertificado",
    "EjecutadoComplementoIPC"
) AS

---------------------------------------------------------------------------
------------------ Select se debe considerar para AT2022	---------------
---------------------------------------------------------------------------
SELECT
a."IdEmpresa",
a."Numero",
TRIM(a."RutCliente") AS "RutCliente",
a."IdCliente",
a."FechaOperacion",
a."TipoAhorro",
---
a."MontoNominal",
--
--COALESCE(a."RentabilidadPositiva",0) AS "RentabilidadPositiva",
--COALESCE(a."RentabilidadNegativa",0) AS "RentabilidadNegativa",


-- COALESCE(a."MontoAPODIV",0)
COALESCE(a."RentabilidadPositiva",0) + CASE WHEN a."RentabilidadPositiva" > 0 THEN COALESCE(a."MontoAPODIV",0) ELSE 0 end AS "RentabilidadPositiva",
COALESCE(a."RentabilidadNegativa",0) + CASE WHEN a."RentabilidadNegativa" > 0 THEN COALESCE(a."MontoAPODIV",0) ELSE 0 end AS "RentabilidadNegativa",



--Actualizada ($)
COALESCE(a."Factor",0) AS "Factor",
COALESCE(a."RentabilidadActualizadaPositiva",0) + CASE WHEN a."RentabilidadPositiva" > 0 THEN COALESCE(a."MontoAPODIV",0) ELSE 0 end AS "RentabilidadActualizadaPositiva",
COALESCE(a."RentabilidadActualizadaNegativa",0) + CASE WHEN a."RentabilidadNegativa" > 0 THEN COALESCE(a."MontoAPODIV",0) ELSE 0 end AS "RentabilidadActualizadaNegativa",
--
a."NumeroCertificado",
a."EjecutadoComplementoIPC"



FROM (

	SELECT sol."IdSerieFondo",
	TO_BIGINT(sol."IdEmpresa") AS "IdEmpresa",
	TO_BIGINT(sol."IdSolicitudAporteRescate") AS "Numero",
	TRIM(cli."DocumentoIdentidad") AS "RutCliente",
	TO_BIGINT(cli."IdCliente") AS "IdCliente",
	sol."FechaValorCuota" AS "FechaOperacion",
	2 AS "TipoAhorro",
	sol."MontoMonedaSistema" AS "MontoNominal",
	CASE WHEN round((sol."RentabilidadReal" * PAR."Valor"),0) > 0 THEN round((sol."RentabilidadReal" * PAR."Valor"),0) ELSE 0 END AS "RentabilidadPositiva",
	CASE WHEN round((sol."RentabilidadReal" * PAR."Valor"),0) < 0 THEN abs(round((sol."RentabilidadReal" * PAR."Valor"),0)) ELSE 0 END AS "RentabilidadNegativa",
	round((IPCACT."Valor"/IPCINV."Valor"),3) AS "Factor",
	CASE WHEN round((sol."RentabilidadReal" * PAR."Valor") * round((IPCACT."Valor"/IPCINV."Valor"),3),0) > 0
	THEN round((sol."RentabilidadReal" * PAR."Valor") * round((IPCACT."Valor"/IPCINV."Valor"),3),0) ELSE 0 END AS "RentabilidadActualizadaPositiva",
	CASE WHEN round((sol."RentabilidadReal" * PAR."Valor") * round((IPCACT."Valor"/IPCINV."Valor"),3),0) < 0
	THEN abs(round((sol."RentabilidadReal" * PAR."Valor") * round((IPCACT."Valor"/IPCINV."Valor"),3),0)) ELSE 0 END AS "RentabilidadActualizadaNegativa",
	0 AS "NumeroCertificado",
	'' AS "EjecutadoComplementoIPC",
	b."MontoAPODIV"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" sol
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli
	ON cli."IdCliente" = sol."IdCliente"
	LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" PAR ON (substring(sol."FechaValorCuota",0,7) = PAR."AgnoMes") AND PAR."IdParametroFinanciero" = 'UTM'
	LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCINV ON (substring(ADD_MONTHS (TO_DATE (sol."FechaValorCuota", 'YYYY-MM-DD'), -1),0,7) = IPCINV."AgnoMes") AND IPCINV."IdParametroFinanciero" = 'IPC'
	LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCACT ON (SUBSTRING(sol."FechaValorCuota",0,4) || '-11' = IPCACT."AgnoMes") AND IPCACT."IdParametroFinanciero" = 'IPC'



	LEFT JOIN ( SELECT sum((cust."EGRCantidad"/cust."Cantidad")* cust."MontoCompra") AS "MontoAPODIV", cust."EGRIdOperacion" AS "EGRIdOperacion"
FROM "BSCL.Custodia.BD::CUS.Cartera" cust
	LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" APODIV ON APODIV."IdSolicitudAporteRescate" = cust."IdOperacion"
	LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo" RegSerFon ON APODIV."IdSerieFondo"= RegSerFon."IdSerieFondo"
	WHERE APODIV."IdTipoMovimientoFondoMutuo" = 'APODIV'
	AND APODIV."Estado" = 'CRT'
	--AND RegSerFon."IdRegimenFondoMutuo" = '18T'
	AND RegSerFon."IdRegimenFondoMutuo" = 'APV-A'
	GROUP BY cust."EGRIdOperacion"
	) b ON sol."IdSolicitudAporteRescate" = b."EGRIdOperacion"
	WHERE sol."IdTipoMovimientoFondoMutuo" = 'RESNORM'
	AND sol."IdRegimenTributario" = 'APV-A'
	AND sol."IdMedioDePago" <> 'CIN'
	AND sol."Estado" = 'CRT'
	) a
	
WITH read only;		

grant select on "BSCL.Normativa.BD.Vistas::TRI.DJ1889.Origen" to bcs_desarrollador with grant option;