DELETE FROM "BSCL.Plataforma.BD::TAS.PrecioTasa" TAS
WHERE TAS."IdPrecioTasa" IN (
	SELECT TAS."IdPrecioTasa" 
	FROM "BSCL.Plataforma.BD::TAS.PrecioTasa" TAS
	INNER JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarFuente" FU
		ON (TAS."IdFuente" = FU."IdFuente" AND FU."Codigo" = :p_CodigosFuentes)
	WHERE TO_VARCHAR(TAS."FechaSolicitud",'YYYY-MM-DD') = :p_Fecha
	  AND TAS."IdEmpresa" = :p_IdEmpresa
	  AND TAS."Borrado" = 'N'
	  AND TAS."IdEstado" NOT IN ('OFI','PUB')
	  AND FU."IdEmpresa" = :p_IdEmpresa
	  AND FU."IdEstado" = 'ACT');