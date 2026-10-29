/********************************************************** 
Proposito   : Script para obtener las carteras para vencer
autor       : SM
fecha       : 01/02/2022
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
    CAR."EGRMonto",
    CAR."EGRMontoMonedaInstrumento",
    CAR."INGMontoATasaCompraActual",
    CAR."ValorRescate" AS "INGMontoCompraActualMonedaInstrumento",
    CAR."IdPortafolio",
    CAR."IdMonedaInstrumento",
    CAR."IdConcepto",
    CAR."INGIdCondicionLiquidacion" AS "IdCondicionLiquidacion",
    cast(CAR."EGRFechaLiquidacion" as nvarchar) as "EGRFechaLiquidacion",
    CAR."EGRIdAgente", 
    CAR."EGRIdSucursal", 
    CAR."IdTipoProducto", 
    CAR."IdTipoOperacion",
    CAR."EGRIdTipoComprobante", 
    CAR."EGRFolioComprobante",
    cast(CAR."EGRFechaComprobante" as nvarchar) as "EGRFechaComprobante",
    coalesce(ip."ValorIndicador",'N') as "OperaContraCuentaInversion", 
    SOC."IdPersona"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
INNER JOIN  "BSCL.Mesa.BD::MES.Operacion" AS OPE ON OPE."IdOperacion" = CAR."IdOperacion"
INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC on SOC."IdCliente" = CAR."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS IP on IP."IdPortafolio" = CAR."IdPortafolio" and IP."CodigoTipoIndicador" = 'GEN_OCCINV'
WHERE CAR."IdEstado" = 'VIG' 
AND   OPE."IdEstado" = 'VIG'
AND   CAR."IdTipoInstrumento" = 'PAC' 
AND   CAR."FechaVencimientoOperacion"=:Fecha 
AND   CAR."IdClasificacionTipoInstrumento" = 'IF' 
AND   CAR."IdEmpresa" = :IdEmpresa
