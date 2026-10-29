--Solo tiene traspaso APV, dado que los MLT son solo pesos y no deberian haber dolares.
WITH
    TRASPASO_APV AS (
      SELECT
      "IdSolicitudAporteRescate"
      ,"IdPersona"
      ,"IdCuentaBancaria"
      ,"IdBanco"
      ,"IdTipoCuentaBancaria"
      ,"IdMoneda"
      ,"NumeroCuentaBancaria"
      ,"NombreTipoCuentaBancaria"
      ,"IdCuentaBancariaCorredora"
      FROM(
       SELECT 
       row_number() OVER (partition by OT."IdSolicitudAporteRescate" order BY OT."IdSolicitudAporteRescate" DESC) AS "Row"
       ,OT."IdSolicitudAporteRescate"
       ,PER."IdPersona"
       ,COALESCE(CUENTA."IdCuentaBancaria",TO_BIGINT(0)) AS "IdCuentaBancaria"
       ,COALESCE(CUENTA."IdBanco",TO_BIGINT(0)) AS "IdBanco"
       ,COALESCE(CUENTA."IdTipoCuentaBancaria",'') AS "IdTipoCuentaBancaria"
       ,COALESCE(CUENTA."IdMoneda",'') AS "IdMoneda"
       ,COALESCE(CUENTA."NumeroCuentaBancaria",'') AS "NumeroCuentaBancaria"
       ,COALESCE(TIPO_CUENTA."NombreTipoCuentaBancaria",'') AS "NombreTipoCuentaBancaria"
       ,COALESCE(CUENTA_CORREDOR."IdCuentaBanco", TO_BIGINT(0)) AS "IdCuentaBancariaCorredora"
       FROM "BSCL.APV.BD::TRA.OrdenTraspaso" AS OT
       INNER JOIN "BSCL.APV.BD::TRA.Traspaso" AS TRA ON OT."IdTraspaso" = TRA."IdTraspaso" AND TRA."RazonRetiro" != 'RES'
       INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON PER."DocumentoIdentidad" = TRA."DocumentoEntidad"
       LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" CUENTA ON CUENTA."IdPersona" = PER."IdPersona"
       LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" TIPO_CUENTA ON TIPO_CUENTA."IdTipoCuentaBancaria" = CUENTA."IdTipoCuentaBancaria"
       LEFT JOIN "BSCL.Tesoreria.BD::TES.PreferenciaPagoCorredor" CUENTA_CORREDOR ON CUENTA_CORREDOR."IdMoneda" = CUENTA."IdMoneda" AND CUENTA_CORREDOR."NombreEntidad" = 'FFM.RescatePorTraspaso'
      
      )
      WHERE "Row" = 1         
                 )          
    SELECT 
    SPOT."IdOperacionSpot",
    SPOT."IdSolicitudAporteRescate",
    FFMM."IdSolicitudAporteRescateRelacionado",
    SPOT."IdTipoCanalIngreso",
    FFMM."TipoSolicitud",
    FFMM."IdTipoMovimientoFondoMutuo",
    FFMM."IdRegimenTributario",
	TRA."IdPersona",
	TRA."IdCuentaBancaria",
	TRA."IdBanco",
	TRA."IdTipoCuentaBancaria",
	TRA."IdMoneda",
	TRA."NumeroCuentaBancaria",
	TRA."NombreTipoCuentaBancaria",
	Emisor."IdPersona" AS "IdPersonaEmisor",
	TRA."IdCuentaBancariaCorredora"
    FROM "BSCL.Mesa.BD::SPT.OperacionSpot" SPOT
    INNER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" FFMM ON SPOT."IdSolicitudAporteRescate" = FFMM."IdSolicitudAporteRescate"
    LEFT JOIN TRASPASO_APV AS TRA ON FFMM."IdSolicitudAporteRescate" = TRA."IdSolicitudAporteRescate" OR (FFMM."IdSolicitudAporteRescateRelacionado" != 0 AND FFMM."IdSolicitudAporteRescateRelacionado" = TRA."IdSolicitudAporteRescate")
    LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SERIE ON SERIE."IdSerieFondo" = FFMM."IdSerieFondo"
    LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" FONDO ON FONDO."IdFondo" = SERIE."IdFondo"
    LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" Emisor ON Emisor."IdEmisor" = FONDO."IdEmisor"
    WHERE SPOT."IdOperacionSpot" IN (:IdOperacionSpot)
    