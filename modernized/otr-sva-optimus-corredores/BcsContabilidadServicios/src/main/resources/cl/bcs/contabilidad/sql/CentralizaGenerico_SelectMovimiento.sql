SELECT 
M."IdEmpresa"
,TO_NVARCHAR(M."Fecha") AS "Fecha" 
,M."IdTipoMovimiento"
,M."Numero"
,M."DocumentoIdentidad"
,M."Cuenta"
,C."IdTipoCentralizacion"
FROM 
"BSCL.Contabilidad.BD::ITZ.Movimiento" M
INNER JOIN "BSCL.Contabilidad.BD::ITZ.Centralizacion" C ON (M."IdCentralizacion" = C."IdCentralizacion")
INNER JOIN "BSCL.Contabilidad.BD::GBL.CentralizaGenerico_ProcesoMovimientos_Paso" MP ON (M."IdEmpresa" = MP."IdEmpresa" 
																						AND M."Fecha" = MP."Fecha" 
																						AND M."IdTipoMovimiento" = MP."IdTipoMovimiento" 
																						AND M."Numero" = MP."Numero"
																						AND M."DocumentoIdentidad" = MP."DocumentoIdentidad"
																						AND M."Cuenta" = MP."IdPortafolio")
WHERE 
C."IdTipoCentralizacion" IN (:IdTipoCentralizacion)
AND	M."Estado" = 'V'