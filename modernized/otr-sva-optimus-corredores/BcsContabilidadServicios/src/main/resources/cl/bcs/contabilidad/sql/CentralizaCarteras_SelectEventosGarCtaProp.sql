SELECT 
	TO_BIGINT(:IdTipoCentralizacion) 						AS "IdTipoCentralizacion", 
	ConsultarGarantias."IdGarantia" 						AS "IdMovimiento",
	'MONTO_GARANTIA' 										AS "IdEvento", 
	'Monto Garantía' 										AS "Nombre", 
	ABS(ConsultarGarantias."MontoGarantia") 	AS "MontoEvento", 
	"IdMonedaSistema" 										AS "CodigoMoneda"

FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias" ConsultarGarantias
WHERE "IdEmpresa" = :IdEmpresa
	AND	((ConsultarGarantias."INGFechaOperacion" = :FechaContable  AND ConsultarGarantias."EGRCantidad" = 0) OR (ConsultarGarantias."EGRFechaOperacion" = :FechaContable  AND ConsultarGarantias."EGRCantidad" <> 0))
--	AND ConsultarGarantias."IdMonedaSistema" = ConsultarGarantias."IdMonedaInstrumento"
	AND ConsultarGarantias."DocumentoIdentidad" =:DocumentoIdentidad

UNION 

SELECT 
	TO_BIGINT(:IdTipoCentralizacion) 							AS "IdTipoCentralizacion", 
	ConsultarGarantias."IdGarantia" 							AS "IdMovimiento",
	'VALOR_GARANTIA' 											AS "IdEvento", 
	'Valor Garantía' 											AS "Nombre", 
	ABS(ConsultarGarantias."MontoGarantiaMonedaInstrumento") 	AS "MontoEvento", 
	"IdMonedaInstrumento" 										AS "CodigoMoneda"

FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias" ConsultarGarantias
WHERE "IdEmpresa" = :IdEmpresa
	AND	((ConsultarGarantias."INGFechaOperacion" = :FechaContable  AND ConsultarGarantias."EGRCantidad" = 0) OR (ConsultarGarantias."EGRFechaOperacion" = :FechaContable  AND ConsultarGarantias."EGRCantidad" <> 0))
--	AND ConsultarGarantias."IdMonedaSistema" <> ConsultarGarantias."IdMonedaInstrumento"
	AND ConsultarGarantias."DocumentoIdentidad" =:DocumentoIdentidad

