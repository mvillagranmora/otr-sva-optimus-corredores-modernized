SELECT 
FWD_COMP."IdCartera" AS "IdOperacion"
,FWD_COMP."IdOperacion" AS "FolioCartera"
,FWD_COMP."NombreCliente" AS "RazonSocial"
,FWD_COMP."MontoMonedaPrincipal"
,FWD_COMP."MontoMonedaSecundaria"
,FWD_COMP."IdMonedaPrincipal"
,FWD_COMP."IdMonedaSecundaria"
,FWD_COMP."Mesa" AS "IdMesa"
,FWD_COMP."IdEstado"
FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosVencidosFWD" AS FWD_COMP
LEFT JOIN "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" AS SPOT ON (SPOT."IdTipoCanalIngreso" = 'FWD' 
                                                                          AND FWD_COMP."IdOperacion" = SPOT."FolioOrigenFW" 
                                                                          AND TO_VARCHAR(SPOT."FechaOperacion",'YYYY-MM-DD') = :Fecha)
INNER JOIN  "BSCL.Mesa.BD::MES.Parametro" AS PRM_FIS ON (PRM_FIS."IdParametro" = FWD_COMP."Modalidad"
                                                            AND PRM_FIS."NombreTipoParametro" = 'ModalidadLiquidacion' 
                                                            AND PRM_FIS."ValorParametro" = 'ENTREGA_FISICA')
WHERE (TO_VARCHAR(FWD_COMP."FechaVencimiento",'YYYY-MM-DD') = :Fecha
OR (FWD_COMP."IdEstado" = 'ANT' AND TO_VARCHAR(FWD_COMP."FechaAnticipo",'YYYY-MM-DD') = :Fecha))
AND FWD_COMP."IdEstado" = 'LIQ'
AND SPOT."FolioOrigenFW" IS NULL;