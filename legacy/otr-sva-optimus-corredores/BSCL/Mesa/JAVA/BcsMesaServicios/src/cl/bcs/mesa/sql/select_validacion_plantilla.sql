SELECT 		
CASE WHEN det1."IdTipoOperacion" IS NOT NULL THEN 'S'
	 WHEN det2."IdTipoOperacion" IS NOT NULL THEN 'S' 
ELSE 'N' END AS "resultado"
FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" emp
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tipoope on tipoope."IdTipoOperacion" = :p_IdTipoOperacion
LEFT JOIN "BSCL.Plataforma.BD::COM.PlantillaEmpleado" pnt on emp."IdPlantillaEmpleado" = pnt."IdPlantillaEmpleado" AND pnt."IdEmpresa" =:p_IdEmpresa
LEFT JOIN "BSCL.Plataforma.BD::COM.DetallePlantillaEmpleado" det1 on det1."IdPlantillaEmpleado" = pnt."IdPlantillaEmpleado" and det1."IdEstado" = 'ACT' and det1."IdTipoProducto" = tipoope."IdTipoProducto" and det1."IdTipoOperacion" = :p_IdTipoOperacion
LEFT JOIN "BSCL.Plataforma.BD::COM.DetallePlantillaEmpleado" det2 on det2."IdPlantillaEmpleado" = pnt."IdPlantillaEmpleado" and det2."IdEstado" = 'ACT' and det2."IdTipoProducto" = tipoope."IdTipoProducto" and det2."IdTipoOperacion" = '0'
WHERE emp."IdEmpleado" = :p_IdEmpleado AND emp."IdEmpresa" = :p_IdEmpresa;