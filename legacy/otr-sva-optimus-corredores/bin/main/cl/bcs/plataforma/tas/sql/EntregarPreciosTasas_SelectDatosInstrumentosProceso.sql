SELECT
	PRO."IdRegistro",
	PRO."TipoCartera", 
	PRO."IdUso", 
	PRO."IdTipoCriterioTasa", 
	PRO."IdClasificacionTipoInstrumento", 
	PRO."IdTipoInstrumento", 
	PRO."IdInstrumento", 
	PRO."Nemotecnico", 
	PRO."IdMoneda",
	PRO."FechaConsulta", 
	PRO."PlazoResidual",
	
	CONTAS."Valor"  AS "TipoCartera_Real",
	USO."IdUso"     AS "IdUso_Real",
	TIPCRI."Codigo" AS "IdTipoCriterioTasa_Real",
	TIPINS."IdTipoInstrumento" AS "IdTipoInstrumento_Real"
	
FROM "BSCL.Plataforma.BD::GBL.EntregarPreciosTasas_DatosInstrumentosProceso" AS PRO

LEFT OUTER JOIN "BSCL.Plataforma.BD::TAS.ConfiguracionTasas" AS CONTAS ON (CONTAS."Valor" = PRO."TipoCartera"  AND CONTAS."NombreEntidad" = 'TIPOCARTERATASAS' AND CONTAS."IdEmpresa" = :IdEmpresa)
LEFT OUTER JOIN "BSCL.Plataforma.BD::TAS.Uso" AS USO ON (USO."IdUso" = PRO."IdUso" AND USO."IdEmpresa" = :IdEmpresa AND USO."IdEstado" = 'ACT')
LEFT OUTER JOIN "BSCL.Plataforma.BD::TAS.TipoCriterioTasa" AS TIPCRI ON (TIPCRI."Codigo" = PRO."IdTipoCriterioTasa" AND TIPCRI."IdEmpresa" = :IdEmpresa AND TIPCRI."IdEstado" = 'ACT')
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIPINS ON (TIPINS."IdTipoInstrumento" = PRO."IdTipoInstrumento" AND TIPINS."IdEstado" = 'VIG');
