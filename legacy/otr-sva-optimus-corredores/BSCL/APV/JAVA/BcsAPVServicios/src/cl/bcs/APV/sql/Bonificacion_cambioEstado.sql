UPDATE "BSCL.APV.BD::INV.Bonificacion"
SET 
		"IdEstado"					= :IdEstado
WHERE	"IdBonificacion" 			= :IdBonificacion
AND 	"IdEmpresa"					= :IdEmpresa;



