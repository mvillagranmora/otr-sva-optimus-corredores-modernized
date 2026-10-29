(SELECT 
crpv."IdCliente",
crpv."IdPortafolio",
crpv."NemoSubyacente" AS "Nemo",
crpv."INGCantidad" AS "Cantidad",
crpv."PlazoEnDiasOperacion" AS "Plazo",
crpv."INGTasaPrestamo" AS "Tasa",
CASE WHEN  crpv."IdTipoPrestamo"  = 'PRTVTC' THEN 'VENTA CORTA' WHEN crpv."IdTipoPrestamo"  = 'PRTFIN' THEN 'FINES GENERALES' WHEN crpv."IdTipoPrestamo"  = 'PRTADM' THEN 'ADMINISTRATIVO'
ELSE crpv."NombreTipoPrestamo" END AS "Tipo_Prestamo",
crpv."NombreEstado" AS "Operacion",
crpv."FechaVencimientoPrestamo" AS "F_Vencimiento",
crpv."FechaLiquidacion" AS "F_Liquidacion",
crpv."FechaPrestamo" AS "F_Registro",
crpv."INGPrecioInstrumento" AS "Precio",
sub."MontoSubyacente"  AS "monto",
crpv."INGMontoPrima" AS "Prima_Premio",
CASE WHEN COALESCE(rpbi."Precio",-1) > 0 THEN
    rpbi."Precio" 
    ELSE
        CASE 
            WHEN COALESCE (ins."Precio", -1) > 0 THEN ins."Precio"
        ELSE COALESCE (crpv."INGPrecioMercadoInstrumento", 0) END 
END AS "Precio_Cierre", 
CASE WHEN COALESCE (rpbi."Precio",-1) > 0 THEN 
    sub."INGPrecioMercadoActual" * crpv."Cantidad"
    ELSE
        CASE  
            WHEN COALESCE (ins."Precio", -1) > 0 THEN ins."Precio" * crpv."Cantidad"
        ELSE COALESCE (crpv."INGPrecioMercadoInstrumento", 0) * crpv."Cantidad" END
END AS "Monto_Cierre",
CASE WHEN crpv."IdTipoPrestamo"  = 'PRTVTC' THEN 'VC'
ELSE 'PT' END  AS "Tipo_Operacion"
FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente" AS crpv
LEFT JOIN "BSCL.Plataforma.BD::INS.RegistroPrecioBolsaInstrumento" AS rpbi ON crpv."IdInstrumentoSubyacente" = rpbi."IdInstrumento" AND rpbi."FechaSistema" = :FechaHasta
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS ins ON ins."IdInstrumento" = crpv."IdInstrumentoSubyacente" --AND rpbi."IdInstrumento" = ins."IdInstrumento"
LEFT JOIN "BSCL.Custodia.BD::CUS.Subyacente" sub ON crpv."IdSubyacente" = sub."IdSubyacente"
WHERE crpv."IdEmpresa" = :IdEmpresa 
AND crpv."FechaPrestamo" <= :FechaHasta 
AND crpv."FechaVencimientoPrestamo" >= :FechaHasta 
AND ((crpv."EGRCantidadPrestamo" = 0
AND crpv."IdEstado" = 'VIG'
AND crpv."INGFechaOperacion" <= :FechaHasta )
OR (crpv."INGFechaOperacion" >= :FechaHasta and crpv."EGRCantidadPrestamo" <> 0 and crpv."EGRFechaOperacion" >  :FechaHasta )OR crpv."IdEstado" = 'ANT')
AND crpv."IdCliente" IN (SELECT "IdCliente" from #TempIdClientes)
AND crpv."IdPortafolio" IN (SELECT "IdPortafolio"  FROM #TempIdPortafolios)
AND crpv."IdClasificacionTipoInstrumentoSubyacente" <>  '' )
UNION ALL
(SELECT 
"IdCliente",
"IdPortafolio",
"NemoSubyacente" AS "Nemo",
CREV."INGCantidad" AS "Cantidad", 
"PlazoEnDiasOperacion" AS "Plazo",
"INGTasaEmprestito" AS "Tasa",
'EMPRESTITO' AS "Tipo_Prestamo",
"NombreEstado" AS "Operacion",
"FechaVencimientoEmprestito" AS "F_Vencimiento",
"FechaLiquidacion" AS "F_Liquidacion",
"FechaEmprestito" AS "F_Registro",
"INGPrecioInstrumento" AS "Precio",
sub."MontoSubyacente"  AS "monto",
"INGMontoPremio" AS "Prima_Premio",
CASE WHEN COALESCE(rpbi."Precio",-1) > 0 THEN 
    CREV."Cantidad" 
    ELSE
        CASE 
            WHEN COALESCE (ins."Precio", -1) > 0 THEN ins."Precio"
        ELSE COALESCE (crev."INGPrecioMercadoInstrumento", 0) END 
END AS "Precio_Cierre", 
CASE WHEN COALESCE(rpbi."Precio",-1) > 0 THEN 
     sub."INGPrecioMercadoActual" * CREV."Cantidad"
    ELSE
        CASE  
            WHEN COALESCE (ins."Precio", -1) > 0 THEN ins."Precio" * crev."Cantidad"
        ELSE COALESCE (crev."INGPrecioMercadoInstrumento", 0) * crev."Cantidad" END
END AS "Monto_Cierre",
'EM' AS "Tipo_Operacion"
FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenEmprestitoVigente" crev
LEFT JOIN "BSCL.Plataforma.BD::INS.RegistroPrecioBolsaInstrumento" AS rpbi ON crev."IdInstrumentoSubyacente" = rpbi."IdInstrumento" AND rpbi."FechaSistema" = :FechaHasta
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS ins ON ins."IdInstrumento" = crev."IdInstrumentoSubyacente" AND rpbi."IdInstrumento" = ins."IdInstrumento"
LEFT JOIN "BSCL.Custodia.BD::CUS.Subyacente" sub ON crev."IdSubyacente" = sub."IdSubyacente"
WHERE crev."IdEmpresa" = :IdEmpresa 
AND "FechaEmprestito" <= :FechaHasta
AND "FechaVencimientoEmprestito" >= :FechaHasta
AND crev."IdEstado" = 'VIG'
AND "IdCliente" IN (SELECT "IdCliente" from #TempIdClientes)
AND "IdPortafolio" IN (SELECT "IdPortafolio"  FROM #TempIdPortafolios)
AND "IdClasificacionTipoInstrumentoSubyacente" <>  '')