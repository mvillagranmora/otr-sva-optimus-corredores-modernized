/********************************************************** 
Proposito   : Script para obtener las carteras para vencer
autor       : JG
fecha       : 01/02/2017
***********************************************************/

SELECT 
    CAR."IdCartera",
    CAR."IdOperacion",
    CAR."FolioCartera",
    CAR."Nemotecnico",
    CAR."IdMonedaEmision" AS "EsPagaderoEn",
    CAR."IdClasificacionTipoInstrumento",
    CAR."IdTipoInstrumento",
    CAR."IdInstrumento",
    CAR."EGRMonto", --as "Monto",
    CAR."EGRMontoMonedaInstrumento",-- as "EGRMontoMonedaInstrumento"
    CAR."INGMontoATasaCompraActual",
    --* CAR."INGMontoCompraActualMonedaInstrumento",
    CAR."ValorRescate" AS "INGMontoCompraActualMonedaInstrumento",
    CAR."IdPortafolio",
    CAR."IdMonedaInstrumento",
    CAR."IdConcepto",
    CAR."INGIdCondicionLiquidacion" AS "IdCondicionLiquidacion",
    cast(CAR."EGRFechaLiquidacion" as nvarchar) as "EGRFechaLiquidacion",
    CAR."EGRIdAgente", 
    CAR."EGRIdSucursal", 
    CAR."IdTipoProducto", 
    CAR."IdTipoOperacion", -- "VCDTIT"
    CAR."EGRIdTipoComprobante", 
    CAR."EGRFolioComprobante", -- toString()
    cast(CAR."EGRFechaComprobante" as nvarchar) as "EGRFechaComprobante",
    coalesce(ip."ValorIndicador",'N') as "OperaContraCuentaInversion", 
    SOC."IdPersona"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC on SOC."IdCliente" = CAR."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS IP on IP."IdPortafolio" = CAR."IdPortafolio" and IP."CodigoTipoIndicador" = 'GEN_OCCINV'
WHERE CAR."IdEstado" = 'VIG' 
AND   CAR."IdTipoInstrumento" = 'PAC' 
--AND   CAR."FechaVencimientoOperacion" > '0001-01-01' 
--AND   CAR."FechaVencimientoOperacion"<=:Fecha
AND   CAR."FechaVencimientoOperacion"=:Fecha 
AND   CAR."IdClasificacionTipoInstrumento" = 'IF' 
AND   CAR."IdEmpresa" = :IdEmpresa

/*
 * FROM "BSCL.Custodia.BD::CUS.Cartera" AS  c
    INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" s on s."IdCliente" = CAR."IdCliente"
    LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"ip on ip."IdPortafolio" = CAR."IdPortafolio" and ip."CodigoTipoIndicador" = 'GEN_OCCINV'
WHERE CAR."IdEstado" = 'VIG'  AND CAR."IdClasificacionTipoInstrumento" = 'IF' and CAR."IdTipoInstrumento" = 'PAC' AND CAR."FechaVencimientoInstrumento" > '0001-01-01' AND CAR."FechaVencimientoInstrumento"<=:Fecha and CAR."IdEmpresa" = :IdEmpresa
 * 
 * */


