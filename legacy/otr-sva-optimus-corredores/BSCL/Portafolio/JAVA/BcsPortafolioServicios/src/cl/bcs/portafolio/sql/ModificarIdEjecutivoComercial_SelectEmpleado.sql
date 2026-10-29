SELECT EMP."IdEmpleado" , EMP."IdEstado" , EMP."CodigoTipoCargoEmpleado"  
FROM BSCL."BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP 
WHERE EMP."IdEmpleado" = :idEmpleado;