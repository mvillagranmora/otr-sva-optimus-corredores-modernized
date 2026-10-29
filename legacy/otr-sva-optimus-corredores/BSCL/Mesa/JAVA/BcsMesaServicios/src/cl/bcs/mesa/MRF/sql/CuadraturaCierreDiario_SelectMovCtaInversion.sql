SELECT 
 OP."IdOperacion"
,OP."FolioOperacion"
,OP."RazonSocial"
,OP."MontoOperacion"
,OP."MontoOperacionMoneda"
,OP."IdMoneda"
,OP."IdMonedaLiquidacion"
,OP."IdMesaOrigen"
,OP."IdEstado"
---------------
,CT."IdEstado" AS "IdEstadoInv"
,CASE OP."IdEstado" || CT."IdEstado" WHEN 'VIGCRT' THEN 'OK' WHEN 'ANUANU' THEN 'OK' ELSE 'NO_OK' END AS "Validacion" 
FROM "BSCL.Mesa.BD.Vistas::ConsultarOperaciones" OP
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" CT
	ON CT."IdTipoComprobante"='MES' AND CT."IdComprobante" = OP."IdOperacion"
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS inv
	ON inv."IdCuentaInversion" = CT."IdCuentaInversion"
INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SDN
	ON SDN."IdEmpresa" = OP."IdEmpresa" AND SDN."IdCliente" <>inv."IdCliente"
INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMP
	ON EMP."IdSocioDeNegocio" = SDN."IdSocioDeNegocio"
WHERE OP."IdTipoProducto" = 'MERF' AND TO_VARCHAR(OP."FechaOperacion",'YYYY-MM-DD') = :Fecha AND OP."IdEmpresa" = :IdEmpresa
AND OP."IdEstado"<>'PEN';