SELECT top 1 "IdReporte" as "Id",* FROM "BSCL.Facturacion.BD.Vistas::ConsultarTemplateFacturacion"
	WHERE "NombreReporte"=:NombreReporte 
	and "IdEmpresa"=:IdEmpresa
	ORDER BY "ReportVersion" desc
