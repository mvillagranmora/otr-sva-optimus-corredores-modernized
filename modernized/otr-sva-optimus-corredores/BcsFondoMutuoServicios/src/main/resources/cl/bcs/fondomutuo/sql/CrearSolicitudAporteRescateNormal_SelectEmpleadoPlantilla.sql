SELECT "IdPortafolio", "PlantillaEmpleadoProductoHabilitado"
FROM (
SELECT 
	port."IdPortafolio",
	CASE WHEN COALESCE(DPEMP."IdDetallePlantillaEmpleado",DPEMP_TODOS."IdDetallePlantillaEmpleado",-1) > 0 THEN 'S' ELSE 'N' END as "PlantillaEmpleadoProductoHabilitado"		
FROM "BSCL.FondoMutuo.BD::GBL.ServicioCumpleRequisitosCrearSolicitudAporteRescate_Portafolios" PORT_PASO
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" port on port."IdPortafolio" = PORT_PASO."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" emp on port."IdEmpleadoEjecutivo" = emp."IdEmpleado"
LEFT JOIN "BSCL.Plataforma.BD::COM.PlantillaEmpleado" as PEMP ON emp."IdPlantillaEmpleado" = PEMP."IdPlantillaEmpleado" AND emp."UtilizaPlantillaEmpleado" = 'S' AND PEMP."IdEstado" = 'ACT'
LEFT JOIN "BSCL.Plataforma.BD::COM.DetallePlantillaEmpleado" as DPEMP ON PEMP."IdPlantillaEmpleado" = DPEMP."IdPlantillaEmpleado" AND DPEMP."IdTipoProducto" = :TipoProducto AND DPEMP."IdTipoOperacion" in (:TipoOperacion) AND DPEMP."IdEstado" = 'ACT'
LEFT JOIN "BSCL.Plataforma.BD::COM.DetallePlantillaEmpleado" as DPEMP_TODOS ON PEMP."IdPlantillaEmpleado" = DPEMP_TODOS."IdPlantillaEmpleado" AND DPEMP_TODOS."IdTipoProducto" = :TipoProducto AND DPEMP_TODOS."IdTipoOperacion" = '0' AND DPEMP_TODOS."IdEstado" = 'ACT'
) group by "IdPortafolio", "PlantillaEmpleadoProductoHabilitado";