(
SELECT CTA."IdCargaMasivaDetalle", 
       CTA."IdCargaMasiva", 
       CTA."DocumentoIdentidad",
       CTA."IdMoneda", 
       CTA."IdTipoMovimiento", 
       CTA."IdConcepto", 
       CTA."Monto", 
       CTA."Descripcion",
       PORT."IdPortafolio"
FROM "BSCL.CuentaInversion.BD::CAR.CargaMasivaDetalle" AS CTA
INNER JOIN "BSCL.APV.BD::REC.RegimenTributario" AS RE ON(RE."Codigo" = CTA."IdPortafolio")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON CLI."DocumentoIdentidad" = CTA."DocumentoIdentidad"
LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS PA ON(PA."Codigo" = CTA."ProductoAgrupador")
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MA ON(MA."IdProductoAgrupador" = PA."IdProductoAgrupador" AND MA."IdRegimenTributario" = RE."IdRegimenTributario")
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORT ON(PORT."IdMatrizAgrupadorAPV" = MA."IdMatrizAgrupadorAPV" AND CLI."IdCliente" = PORT."IdCliente")
WHERE CTA."IdCargaMasiva"=:p_IdCargaMasivaDetalle
)

UNION ALL

(
SELECT CTA."IdCargaMasivaDetalle", 
       CTA."IdCargaMasiva", 
       CTA."DocumentoIdentidad",
       CTA."IdMoneda", 
       CTA."IdTipoMovimiento", 
       CTA."IdConcepto", 
       CTA."Monto", 
       CTA."Descripcion",
       PORT."IdPortafolio"
FROM "BSCL.CuentaInversion.BD::CAR.CargaMasivaDetalle" AS CTA
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON CLI."DocumentoIdentidad" = CTA."DocumentoIdentidad"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" PORT ON PORT."CodigoPortafolio" = CTA."IdPortafolio" AND CLI."IdCliente" = PORT."IdCliente"
WHERE CTA."IdCargaMasiva"=:p_IdCargaMasivaDetalle AND CTA."ProductoAgrupador" = ''
);