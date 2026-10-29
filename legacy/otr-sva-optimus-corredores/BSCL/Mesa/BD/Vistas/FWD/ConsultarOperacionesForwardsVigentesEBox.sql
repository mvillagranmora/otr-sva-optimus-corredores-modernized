SET SCHEMA "BSCL";

DROP VIEW "BSCL.Mesa.BD.Vistas::FWD.ConsultarOperacionesForwardsVigentesEBox";

CREATE VIEW "BSCL.Mesa.BD.Vistas::FWD.ConsultarOperacionesForwardsVigentesEBox" AS
WITH ReservaUnica AS (
    SELECT
        RES."IdOperacion",
        RES."IdTipoProducto",
        RES."FolioCarteraOrigen",
        ROW_NUMBER() OVER (
            PARTITION BY RES."FolioCarteraOrigen", RES."IdTipoProducto"
            ORDER BY RES."IdReserva"
        ) AS RN
    FROM "BSCL.Custodia.BD::CUS.Reserva" RES
    WHERE RES."IdEstado" = 'VIG'
),
CarteraCalzada AS (
    SELECT
        C."IdOperacion"
       ,C."IdTipoProducto"
       ,C."IdCartera"
       ,C."FolioCartera"
    FROM (
        SELECT
            CAR."IdOperacion"
           ,CAR."IdTipoProducto"
           ,CAR."IdCartera"
           ,CAR."FolioCartera"
           ,ROW_NUMBER() OVER (
                PARTITION BY CAR."IdOperacion",CAR."IdTipoProducto"
                ORDER BY CAR."IdCartera"
            ) AS RN
        FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
          WHERE CAR."IdTipoOperacion" IN ('COMFWD','VENFWD')
		  AND CAR."IdEstado" = 'VIG'
          AND EXISTS (
                SELECT 1
                FROM "BSCL.Custodia.BD::CUS.Reserva" R
                WHERE R."IdOperacion" = CAR."IdOperacion"
                  AND R."IdTipoProducto" = CAR."IdTipoProducto"
                  AND R."IdEstado" = 'VIG'
          )
    ) C
    WHERE C.RN = 1
)
SELECT
	CAR."IdCartera"
   ,CAR."IdEmpresa"
   ,OPE."IdOperacion"
   ,OPE."IdCliente"
   ,OPE."IdTipoOperacion"
   ,OPE."PlazoEnDias"
   ,OPE."IdMonedaPrincipal"
   ,OPE."IdMonedaSecundaria"
   ,CAR."FechaOperacion"
   ,CAR."FolioCartera" AS "NumeroContrato"
   ,COALESCE(CALCE."FolioCartera",0) AS "NumeroContratoCalce"
   ,CLI."DocumentoIdentidad" AS "RutCliente"
   ,CLI."NombreCompleto" AS "NombreCliente"
   ,CASE 
        WHEN IND."ValorIndicador" IN ('1','2','3','4','5') THEN 'SI'
        ELSE 'NO'
    END AS "ClienteRelacionado"
-- Se voltea sentido de tipo operacion ya que el informe es visto desde la perspectiva del cliente
   ,CASE 
        WHEN OPE."IdTipoOperacion"='COMFWD' THEN 'Venta'
        ELSE 'Compra'
    END AS "TipoOperacion"
   ,CAR."FechaVencimientoOperacion" AS "FechaVencimiento"
   ,COALESCE(HOMMOD."ValorDestino", TO_NVARCHAR(OPE."IdModalidadLiquidacion")) AS "ModalidadLiquidacion"
   ,COALESCE(HMONPRI."ValorDestino", OPE."IdMonedaPrincipal") || '/' || COALESCE(HMONSEC."ValorDestino", OPE."IdMonedaSecundaria") AS "MonedaTasa"
   ,OPE."MontoOperacion" AS "ValorNominal"
   ,CASE WHEN CAR."ValorParidadCierre" > 0 THEN CAR."ValorParidadCierre" ELSE CAR."ValorTipoCambioCierre" END AS "TipoCambioPactado"
   ,CASE 
		WHEN CAR."IdMonedaSecundaria" = 'CLP' THEN CAR."INGMontoMonedaSecundaria"
        WHEN CAR."IdMonedaSecundaria" = 'UF' THEN CAR."INGMontoATasaCompraActual"
 		ELSE ROUND(CAR."INGMontoMonedaPrincipal" * PAR."TipoCambioMercado",0)
    END AS "MontoPactado"
   ,OPE."MontoMargen" AS "ResultadoOperacion"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraLite" CAR
INNER JOIN "BSCL.Mesa.BD::MES.Operacion" OPE
	ON OPE."IdOperacion" = CAR."IdOperacion"
	AND OPE."IdTipoProducto" = 'MEFW'
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
	ON CLI."IdCliente" = CAR."IdCliente"
LEFT JOIN ReservaUnica RES
    ON RES."FolioCarteraOrigen" = CAR."IdOperacion"
   AND RES."IdTipoProducto" = CAR."IdTipoProducto"
   AND RES.RN = 1
LEFT JOIN CarteraCalzada CALCE
	ON CALCE."IdOperacion" = RES."IdOperacion"
	AND CALCE."IdTipoProducto" = RES."IdTipoProducto"
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PAR
	ON PAR."IdMoneda" = OPE."IdMonedaPrincipal"
	AND PAR."Fecha" = CAR."FechaOperacion"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" IND
	ON IND."IdPersona" = CLI."IdPersona"
	AND IND."GrupoIndicador" = 'NORMATIVO'
	AND IND."CodigoTipoIndicador" = 'NCG380PER'
LEFT JOIN "BSCL.Normativa.BD::DOC.Homologacion" HOMMOD
	ON HOMMOD."ValorOrigen" = TO_NVARCHAR(OPE."IdModalidadLiquidacion")
	AND HOMMOD."IdEstado" = 'ACT'
	AND HOMMOD."Esquema" = 'FWDVIGEBOX'
	AND HOMMOD."Tipo" = 'MODALIDADCUMPLIMIENTO'
LEFT JOIN "BSCL.Normativa.BD::DOC.Homologacion" HMONPRI
	ON HMONPRI."ValorOrigen" = OPE."IdMonedaPrincipal"
	AND HMONPRI."IdEstado" = 'ACT'
	AND HMONPRI."Esquema" = 'FWDVIGEBOX'
	AND HMONPRI."Tipo" = 'MONEDA'
LEFT JOIN "BSCL.Normativa.BD::DOC.Homologacion" HMONSEC
	ON HMONSEC."ValorOrigen" = OPE."IdMonedaSecundaria"
	AND HMONSEC."IdEstado" = 'ACT'
	AND HMONSEC."Esquema" = 'FWDVIGEBOX'
	AND HMONSEC."Tipo" = 'MONEDA'
WHERE CAR."IdTipoOperacion" IN ('COMFWD','VENFWD') 
AND CAR."IdEstado" = 'VIG';

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::FWD.ConsultarOperacionesForwardsVigentesEBox" TO BCS_DESARROLLADOR WITH GRANT OPTION;