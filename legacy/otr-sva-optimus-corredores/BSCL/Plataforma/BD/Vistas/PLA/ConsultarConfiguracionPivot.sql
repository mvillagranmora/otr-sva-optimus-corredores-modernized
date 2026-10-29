SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionPivot";
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionPivot"  
AS

SELECT  "IdEmpresa",
		MAX(CASE WHEN "CodigoParametro" = 'PLA.IdiomaSistema' THEN "Valor" END) AS "PLA.IdiomaSistema",
		MAX(CASE WHEN "CodigoParametro" = 'PLA.LocalizacionSistema' THEN "Valor" END) AS "PLA.LocalizacionSistema",
		MAX(CASE WHEN "CodigoParametro" = 'PLA.ZonaHoraria' THEN "Valor" END) AS "PLA.ZonaHoraria",
		MAX(CASE WHEN "CodigoParametro" = 'PLA.PorcentajeFluctuacionPrecio' THEN "Valor" END) AS "PLA.PorcentajeFluctuacionPrecio",
		MAX(CASE WHEN "CodigoParametro" = 'PLA.ClaveUsuarioIntentos' THEN "Valor" END) AS "PLA.ClaveUsuarioIntentos",
		MAX(CASE WHEN "CodigoParametro" = 'PLA.ClaveUsuarioTiempoBloqueo' THEN "Valor" END) AS "PLA.ClaveUsuarioTiempoBloqueo",
		MAX(CASE WHEN "CodigoParametro" = 'PLA.MonedaSistema' THEN "Valor" END) AS "PLA.MonedaSistema",
		MAX(CASE WHEN "CodigoParametro" = 'PLA.MonedaAdicional' THEN "Valor" END) AS "PLA.MonedaAdicional",
		MAX(CASE WHEN "CodigoParametro" = 'PLA.CoberturaEfectivoEspecies' THEN "Valor" END) AS "PLA.CoberturaEfectivoEspecies",
		MAX(CASE WHEN "CodigoParametro" = 'PLA.CoberturaSinDefinir' THEN "Valor" END) AS "PLA.CoberturaSinDefinir",
		MAX(CASE WHEN "CodigoParametro" = 'PLA.BolsaDefectoSistema' THEN "Valor" END) AS "PLA.BolsaDefectoSistema",
		MAX(CASE WHEN "CodigoParametro" = 'PLA.ConflictoAccesoDefecto' THEN "Valor" END) AS "PLA.ConflictoAccesoDefecto",
		MAX(CASE WHEN "CodigoParametro" = 'PLA.ConcurrenciaValorizador' THEN "Valor" END) AS "PLA.ConcurrenciaValorizador",
		MAX(CASE WHEN "CodigoParametro" = 'PLA.DiferenciaMontoAsignacion' THEN "Valor" END) AS "PLA.DiferenciaMontoAsignacion",
		MAX(CASE WHEN "CodigoParametro" = 'PLA.Ambiente' THEN "Valor" END) AS "PLA.Ambiente",
		MAX(CASE WHEN "CodigoParametro" = 'PLA.ProgramaValorizador' THEN "Valor" END) AS "PLA.ProgramaValorizador",
		MAX(CASE WHEN "CodigoParametro" = 'PLA.Version' THEN "Valor" END) AS "PLA.Version",
		MAX(CASE WHEN "CodigoParametro" = 'PLA.SegundosZonaHoraria' THEN "Valor" END) AS "PLA.SegundosZonaHoraria",
		MAX(CASE WHEN "CodigoParametro" = 'PLA.FechaSistema' THEN CAST("Valor" AS DATE) END) AS "PLA.FechaSistema",
		MAX(CASE WHEN "CodigoParametro" = 'PLA.RutaArchivosNCG380' THEN "Valor" END) AS "PLA.RutaArchivosNCG380"
  FROM (
		 SELECT "IdEmpresa","CodigoParametro","Valor"
           FROM "BSCL.Plataforma.BD::PLA.Configuracion"
       )
 GROUP BY "IdEmpresa";
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionPivot" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
