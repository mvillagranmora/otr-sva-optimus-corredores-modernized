SELECT DISTINCT 
	emp."IdPersona" AS "IdPersona"
	,'OPCIONES' AS "Producto"
	,concat(CONCAT(emp."NombreCompleto",' ('), CONCAT(emp."CodigoEmpleado", ')')) AS "Agente"
	,COALESCE(emp."CodigoEmpleadoSupervisor",'') AS "Supervisor"
	,cor."RazonSocial" AS "NombreCorredora"
	,emp."IdDireccionElectronicaPrincipal" AS "IdDireccionElectronica"
FROM "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" eje
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" emp ON eje."IdEmpleadoEjecutivo" = emp."IdEmpleado" AND emp."UtilizaPlantillaEmpleado" = 'S' AND emp."IdEstado" = 'ACT'
	INNER JOIN "BSCL.Plataforma.BD::COM.DetallePlantillaEmpleado" pla ON emp."IdPlantillaEmpleado" = pla."IdPlantillaEmpleado" AND pla."IdTipoProducto" LIKE 'IO%' AND pla."IdEstado" = 'ACT'
	INNER JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" cor ON cor."IdEmpresa" = :IdEmpresa
WHERE eje."IdEmpresa" = :IdEmpresa AND eje."IdEstado" = 'ACT';
