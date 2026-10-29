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
INNER JOIN  "BSCL.Mesa.BD::MES.Parametro" AS PRM_COMP ON (PRM_COMP."IdParametro" = FWD_COMP."Modalidad"
                                                            AND PRM_COMP."NombreTipoParametro" = 'ModalidadLiquidacion' 
                                                            AND PRM_COMP."ValorParametro" = 'COMPENSADA')
WHERE TO_VARCHAR(FWD_COMP."FechaVencimiento",'YYYY-MM-DD') = :Fecha
OR (FWD_COMP."IdEstado" = 'ANT' AND TO_VARCHAR(FWD_COMP."FechaAnticipo",'YYYY-MM-DD') = :Fecha)