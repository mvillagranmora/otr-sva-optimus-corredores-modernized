SELECT "IdMotivo",
		"IdEstado",
		"Motivo",
		"Tipo",
		"IdTipoCanalIngreso",
		"Activo",
		"CodigoEstado" 
FROM "BSCL.Orden.BD::ORD.InstruccionMotivo"
WHERE COALESCE("CodigoEstado",'') != '';