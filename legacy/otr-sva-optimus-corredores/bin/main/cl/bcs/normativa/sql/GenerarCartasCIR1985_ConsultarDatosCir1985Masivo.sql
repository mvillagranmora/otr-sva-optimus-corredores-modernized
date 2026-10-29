WITH ESQUEMA_DOC AS (
    SELECT "Esquema",
           RANK() OVER (ORDER BY "Esquema") AS RN
    FROM "BSCL.Normativa.BD::DOC.TipoDocumento"
    WHERE "IdTipoDocumento" = 'EJECORD'
),
RELNOR AS (
    SELECT 
         COALESCE(RELHOM."ValorDestino", CAST(REL."CodigoValorIndicador" AS NVARCHAR(100))) AS "RELACION"
        ,REL."IdCliente"
    FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" REL
    LEFT JOIN "BSCL.Normativa.BD::DOC.Homologacion" RELHOM
        ON CAST(REL."CodigoValorIndicador" AS NVARCHAR(100)) = RELHOM."ValorOrigen"
        AND RELHOM."Tipo" = 'RELACION'
        AND RELHOM."Esquema" = (SELECT "Esquema" FROM ESQUEMA_DOC WHERE RN = 1)
        AND RELHOM."Version" = :p_Version
    WHERE REL."IdUso" = 'IND'
      AND REL."CodigoTipoIndicador" = '1064PROP'
      AND REL."CodigoValorIndicador" <> ''
      AND REL."IdCliente" IN (:p_IdClientes)
)
SELECT
     "DATACIR"."IdEjecucion"
    ,"DATACIR"."IdDocumento"
    ,"DATACIR"."NombreCompleto"
    ,"DATACIR"."DireccionFisica"
    ,"DATACIR"."RutCliente"
    ,"DATACIR"."Telefono"
    ,"DATACIR"."DireccionElectronica"
    ,"DATACIR"."FolioGeneracion"
    ,"DATACIR"."IdOrden"
    ,"DATACIR"."Folio"
    ,"DATACIR"."IdCliente"
    ,"DATACIR"."IdPortafolio"
    ,"DATACIR"."CodigoPortafolio"
    ,"DATACIR"."IdClasificacionTipoInstrumento"
    ,"DATACIR"."IdTipoInstrumento"
    ,"DATACIR"."IdTipoOperacion"
    ,"DATACIR"."IdInstrumento"
    ,"DATACIR"."Cantidad"
    ,"DATACIR"."Monto"
    ,"DATACIR"."MontoValorizado"
    ,"DATACIR"."Precio"
    ,"DATACIR"."IdMoneda"
    ,"DATACIR"."IdCondicionDeLiquidacion"
    ,"DATACIR"."FechaRecepcion"
    ,"DATACIR"."FechaAceptacion"
    ,"DATACIR"."TipoEjecucion"
    ,TO_VARCHAR("DATACIR"."FechaEjecucion",'DD-MM-YYYY') AS "FechaEjecucion"
    ,TO_VARCHAR(LEFT(UTCTOLOCAL(TO_TIMESTAMP(EO."FechaEjecucion",'YYYY-MM-DD HH24:MI:SS'),'America/Santiago','platform'),19),'DD-MM-YYYY HH24:MI:SS') AS "FechaHoraEjecucion"
    ,TO_VARCHAR(LEFT(UTCTOLOCAL(TO_TIMESTAMP(EO."FechaRecepcion",'YYYY-MM-DD HH24:MI:SS'),'America/Santiago','platform'),19),'DD/MM/YYYY HH24:MI:SS') AS "FechaRecepcion"
    ,TO_VARCHAR(LEFT(UTCTOLOCAL(TO_TIMESTAMP(EO."FechaAceptacion",'YYYY-MM-DD HH24:MI:SS'),'America/Santiago','platform'),19),'DD/MM/YYYY HH24:MI:SS') AS "FechaAceptacion"
    ,"DATACIR"."IdBolsa"
    ,"DATACIR"."EsOD"
    ,"DATACIR"."Modalidad"
    ,"DATACIR"."PrecioEjecucion"
    ,"DATACIR"."MontoTransado"
    ,"DATACIR"."CantidadTransada"
    ,"DATACIR"."ComisionTransada"
    ,EO."MejorPrecioOtraBolsa" AS "MejorPrecioOtraBolsaNum"
    ,"DATACIR"."CantidadMejorPrecioOtraBolsa"
    ,"DATACIR"."PrecioMinHoraBolsaMcdo"
    ,"DATACIR"."PrecioMaxHoraBolsaMcdo"
    ,"DATACIR"."MejorPrecioMercado"
    ,"DATACIR"."PrecioMinimoMercado"
    ,"DATACIR"."HoraPrecioMinimoMercado"
    ,"DATACIR"."IdBolsaPrecioMinimoMercado"
    ,"DATACIR"."PrecioMaximoMercado"
    ,"DATACIR"."HoraPrecioMaximoMercado"
    ,"DATACIR"."IdBolsaPrecioMaximoMercado"
    ,"DATACIR"."PrecioMinHoraBolsaCorr"
    ,"DATACIR"."PrecioMaxHoraBolsaCorr"
    ,"DATACIR"."MayorPrecioOperacionCorredor"
    ,"DATACIR"."HoraPrecioMayorCorredor"
    ,"DATACIR"."MenorPrecioOperacionCorredor"
    ,"DATACIR"."HoraPrecioMenorCorredor"
    ,"DATACIR"."IdEmpleadoDigitador"
    ,"DATACIR"."FolioComprobanteFacturacion"
    ,"DATACIR"."FechaGeneracion"
    ,"DATACIR"."FolioExterno"
    ,"DATACIR"."IdEstadoNotificacion"
    ,"DATACIR"."IdEstadoCarta"
    ,"DATACIR"."FechaIngreso"
    ,"DATACIR"."FechaCreacion"
    ,"DATACIR"."IdUsuarioCreador"
    ,"DATACIR"."NombreAgente"
    ,"DATACIR"."AccionOperacion"
    ,"DATACIR"."TelefonoAgente"
    ,"DATACIR"."Nemotecnico"
    ,"DATACIR"."PrecioMinHoraBolsaBEC"
    ,"DATACIR"."PrecioMaxHoraBolsaBEC"
    ,"DATACIR"."FolioOrden"
    ,COALESCE(ORDUI."InstrumentoNegociacion",'') AS "InstrumentoNegociacion"
    ,RELNOR."RELACION" AS "RelacionCorredor"
	,TO_VARCHAR(TO_DATE(COALESCE(EO."FechaReferencia", EO."FechaEjecucion")),'YYYY-MM-DD') AS "FechaReferenciaAgrupador"
FROM "BSCL.Normativa.BD.Vistas::CIR.ConsultarDatosCIR1985" AS "DATACIR"
INNER JOIN "BSCL.Normativa.BD::EJORD.EjecucionOrdenes" EO
    ON EO."IdEjecucion" = "DATACIR"."IdEjecucion"
LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarOrdenesUI" AS ORDUI
    ON ORDUI."IdOrden" = "DATACIR"."IdOrden"
LEFT JOIN RELNOR
    ON RELNOR."IdCliente" = "DATACIR"."IdCliente"
WHERE TO_DATE(COALESCE(EO."FechaReferencia", EO."FechaEjecucion")) BETWEEN TO_DATE(:p_FechaDesde) AND TO_DATE(:p_FechaHasta)
  AND "DATACIR"."IdCliente" IN (:p_IdClientes)
ORDER BY "DATACIR"."IdPortafolio", EO."FechaReferencia", "DATACIR"."FechaEjecucion"