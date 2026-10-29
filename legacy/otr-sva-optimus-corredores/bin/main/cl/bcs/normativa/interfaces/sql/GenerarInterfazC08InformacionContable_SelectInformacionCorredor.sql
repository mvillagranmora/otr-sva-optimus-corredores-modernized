SELECT 	EMP."Nombre", 
		EMP."NombreCorto", 
		EMP."DocumentoIdentidad" AS "Rut" 
   FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP 
  WHERE EMP."IdEmpresa" = :IdEmpresa