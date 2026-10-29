SELECT  EJE."IdPortafolio" , 
        "IdEmpleadoEjecutivoPortafolio", 
        "CodigoEmpleado", 
        "NombreCompletoEjecutivo", 
        "CodigoSucursal"
FROM "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio"    AS EJE
WHERE EJE."IdPortafolio" IN (:IdPortafolio)  AND  EJE."EsEjecutivoVigente" = 'S'