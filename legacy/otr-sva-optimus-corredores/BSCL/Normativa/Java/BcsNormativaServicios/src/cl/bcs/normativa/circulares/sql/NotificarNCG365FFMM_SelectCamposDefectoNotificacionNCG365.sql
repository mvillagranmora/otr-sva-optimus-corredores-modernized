SELECT
	COALESCE(CDN."CodigoTipoEvento", :CodigoTipoEvento) AS "CodigoTipoEvento"
	, CDN."NombreCampo"
	, CDN."Valor"
	, CDN."IdEmpresa"
FROM "BSCL.Plataforma.BD::EVT.CampoDefectoNotificacion" AS CDN
LEFT JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" AS CCDG ON CCDG."IdEmpresa" = CDN."IdEmpresa"
WHERE "CodigoTipoEvento" = :CodigoTipoEvento AND CDN."IdEmpresa" = :IdEmpresa
UNION ALL
SELECT 
	:CodigoTipoEvento AS "CodigoTipoEvento"
	, 'RazonCorredoraDefecto' AS "NombreCampo"
	, CCDG."RazonSocial" AS "Valor"
	, CCDG."IdEmpresa"
FROM "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" AS CCDG
UNION ALL
SELECT 
	:CodigoTipoEvento AS "CodigoTipoEvento"
	, 'NumeroCallCenterDefecto' AS "NombreCampo"
	, '000 000 0000' AS "Valor"
	, :IdEmpresa
FROM DUMMY;