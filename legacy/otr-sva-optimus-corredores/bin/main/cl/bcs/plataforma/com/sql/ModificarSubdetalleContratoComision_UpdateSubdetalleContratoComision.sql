UPDATE "BSCL.Plataforma.BD::COM.SubdetalleContratoComision" 
SET
	"Monto"					= :Monto,
	"Porcentaje"			= :Porcentaje,
	"MontoMaximo"			= :MontoMaximo
WHERE "IdSubdetalleContratoComision" = :IdSubdetalleContratoComision;

