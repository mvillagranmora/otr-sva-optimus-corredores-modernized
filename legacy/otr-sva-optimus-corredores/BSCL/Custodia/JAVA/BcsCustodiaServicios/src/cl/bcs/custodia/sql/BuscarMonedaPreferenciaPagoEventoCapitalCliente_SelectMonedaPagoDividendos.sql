SELECT 
	DET."DetalleIndicador" AS "IdMoneda"
FROM "BSCL.Plataforma.BD::IND.TipoIndicador" TIN 
INNER JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador" DET ON DET."IdTipoIndicador" = TIN."IdTipoIndicador" 
WHERE "CodigoTipoIndicador" = 'DIV_DOM'