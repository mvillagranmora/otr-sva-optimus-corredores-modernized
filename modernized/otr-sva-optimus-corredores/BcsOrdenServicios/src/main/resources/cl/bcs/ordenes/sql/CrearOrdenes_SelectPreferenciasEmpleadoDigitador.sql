SELECT
 emp."IdEmpleado" 
,emp."CodigoEmpleado"
,emp."IdSucursal" AS "IdSucursalEmpleadoDigitador"
,DPE."IdPlantillaEmpleado" 
,DPE."IdTipoProducto"
,DPE."IdTipoOperacion"
,DPE."IdEstado"
,CASE WHEN COALESCE(DPE."IdDetallePlantillaEmpleado",-1) > 0 THEN 'S' ELSE 'N' END as "PlantillaEmpleadoProductoHabilitado"
,DPE."PermiteRuteo"
,DPE."PorcentajeDescuentoComision"
,DPE."MontoMaximoOperacion" 
FROM "BSCL.Plataforma.BD::COM.DetallePlantillaEmpleado" DPE
LEFT JOIN "BSCL.Plataforma.BD::COM.PlantillaEmpleado" PEM ON (PEM."IdPlantillaEmpleado" = DPE."IdPlantillaEmpleado")
LEFT JOIN  "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" emp ON (emp."IdPlantillaEmpleado" = PEM."IdPlantillaEmpleado") 
LEFT JOIN "BSCL.SocioNegocio.BD::EJE.Ejecutivo" EJE ON EJE."IdEmpleadoEjecutivo" = emp."IdEmpleado"
WHERE emp."CodigoEmpleado" IN (:CodigosDig) AND DPE."IdEstado" = 'ACT' AND EMP."IdEstado" = 'ACT' AND EJE."IdEstado" = 'ACT'