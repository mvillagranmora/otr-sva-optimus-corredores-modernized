SELECT
	CEN."IdTipoCentralizacion"
	,MOV."IdMovimiento"
	,MOV."IdTipoMovimiento"
	,MOV."IdEmpresa"
	,MOV."Numero"
	,MOV."Linea"
	,MOV."Fecha"
	,MOV."DocumentoIdentidad"
	,TO_VARCHAR(MOV."Ejercicio")
	,MOV."Periodo"
	,MOV."Monto"
	,MOV."IdMoneda"
	,MOV."Estado"
FROM "BSCL.Contabilidad.BD::ITZ.Movimiento" MOV
INNER JOIN "BSCL.Contabilidad.BD::ITZ.Centralizacion" CEN ON (CEN."IdCentralizacion" = MOV."IdCentralizacion")
WHERE CEN."Estado" = 'V'
AND CEN."FechaContable" = :FechaCentralizacion AND CEN."IdTipoCentralizacion" IN (:IdCentralizaciones)
AND CEN."IdEmpresa" = :IdEmpresa;