SELECT 
CONCAT('ANTICIPO ',ANTI."NombreOperacion") AS "TipoOperacion"
,ANTI."AccionOperacion" 
,ANTI."EGRFechaOperacion" AS "FechaLiquidacion" 
,COMP_DETALLE."FolioTransaccion" AS "FolioTransaccion"
,ANTI."suby_Nemotecnico" AS "Nemotecnico"
,ANTI."EGRCantidad" AS "Cantidad"
,MVTO_CNTA."MontoOperacion" AS "Monto"
,CAST(:p_CodigoGrupoCamara AS BIGINT) AS "CodigoGrupoCamara"
,:p_NombreGrupoCamara AS "NombreGrupoCamara"
,COR."DocumentoIdentidad" AS "RutContraparte"
,COR."NombreCorto" AS "RazonSocialContraparte"
,TRA."IdCorredorContraparte"
,TRA."IdBolsa"
,TRA."IdClasificacionTipoInstrumento"
,TRA."IdComprobanteTesoreria" 
,TRA."IdMoneda"
FROM "BSCL.Custodia.BD.Vistas::SIM.ConsultarAnticiposHis" AS ANTI
INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS COMP_ANT ON COMP_ANT."IdComprobante" = ANTI."FolioComprobante" 
INNER JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" AS COMP_DETALLE ON COMP_DETALLE."IdComprobante" = COMP_ANT."IdComprobante"
INNER JOIN "BSCL.Orden.BD::ORD.Transaccion" AS TRA ON TRA."IdTransaccion" = COMP_DETALLE."IdTransaccion" 
INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" AS COR ON COR."IdCorredora" = TRA."IdCorredorContraparte" 
INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" AS EMP ON EMP."IdEmpresa" = :p_IdEmpresa
INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC ON SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"
INNER JOIN "BSCL.Plataforma.BD::INS.Corredora" AS COR_SIS  ON COR_SIS."IdPersona" = SOC."IdPersona" 
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS MVTO_CNTA ON MVTO_CNTA."IdComprobante" = ANTI."EGRFolioComprobante"  
WHERE ANTI."EGRFechaOperacion" = :p_FechaLiquidacion 
AND "FechaRespaldo" = :p_FechaLiquidacion 
AND ANTI."IdEmpresa" = :p_IdEmpresa
AND TRA."IdCorredorContraparte" <> COR_SIS."IdCorredora";
