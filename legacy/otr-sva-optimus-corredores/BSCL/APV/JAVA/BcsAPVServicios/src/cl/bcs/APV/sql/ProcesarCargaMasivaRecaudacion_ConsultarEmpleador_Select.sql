SELECT 
	 "IdPersona"
	,"PersonaBusqueda"
	,"DocumentoIdentidad" 
FROM "BSCL.APV.BD.Vistas::REC.ConsultarPersonaJuridica"
WHERE "DocumentoIdentidad" IN (:DocumentoIdentidad) AND "IdEstado" = 'ACT'
GROUP BY  "IdPersona","PersonaBusqueda"	,"DocumentoIdentidad" 