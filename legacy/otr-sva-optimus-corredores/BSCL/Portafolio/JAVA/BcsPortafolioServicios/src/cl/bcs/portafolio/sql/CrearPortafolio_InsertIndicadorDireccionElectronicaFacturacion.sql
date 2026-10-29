INSERT INTO "BSCL.Plataforma.BD::IND.Indicador"(
 "IdIndicador",
 "IdEntidad",
 "IdTipoEntidad",
 "IdTipoIndicador",
 "ModeloCliente",
 "IdEmpresa",
 "ValorIndicador",
 "FechaIngreso",
 "FechaCreacion",
 "FechaModificacion",
 "IdEstadoAutorizacion",
 "FechaVencimiento"
 )
SELECT
 "BSCL.Plataforma.BD.Secuencias::Indicador".NEXTVAL AS "IdIndicador",
 dirElecTable."IdEntidad",
 dirElecTable."IdTipoEntidad",
 dirElecTable."IdTipoIndicador",
 dirElecTable."ModeloCliente",
 dirElecTable."IdEmpresa",
 dirElecTable."ValorIndicador",
 dirElecTable."FechaIngreso",
 dirElecTable."FechaCreacion",
 dirElecTable."FechaModificacion",
 dirElecTable."IdEstadoAutorizacion",
 dirElecTable."FechaVencimiento"
 FROM (SELECT TOP 1  
 :IdPortafolio AS "IdEntidad",
 ATR."IdTipoEntidad",
 ATR."IdTipoIndicador",
 ATR."ModeloCliente",
 ATR."IdEmpresa",
	 CASE ATR."CodigoTipoIndicador"
		 WHEN 'ORDMAILNTF' THEN TO_NVARCHAR(DIR."IdDireccionElectronica")
		 ELSE ATR."ValorIndicador"
	 END AS "ValorIndicador",
 CURRENT_UTCTIMESTAMP AS "FechaIngreso",
 CURRENT_UTCTIMESTAMP AS "FechaCreacion",
 CURRENT_UTCTIMESTAMP AS "FechaModificacion",
 'ING' AS "IdEstadoAutorizacion",
 '0001-01-01 00:00:01' AS "FechaVencimiento"
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" Cliente
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" DIR ON Cliente."IdPersona" = DIR."IdPersona" --AND DIR."Predeterminado" = 'UI_SI'
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" P ON Cliente."IdCliente" = P."IdCliente" AND P."IdPortafolio"  = :IdPortafolio 
INNER JOIN (
	SELECT TI."CodigoTipoIndicador", I.* 
	FROM BSCL."BSCL.Plataforma.BD::IND.TipoIndicador" TI
 	INNER JOIN BSCL."BSCL.Plataforma.BD::IND.Indicador" I ON TI."IdTipoIndicador" = I."IdTipoIndicador"
	 WHERE TI."IdTipoEntidad" = 'PORTAFOLIO' AND TI."IdUso" = 'PRE' AND  TI."CodigoTipoIndicador" IN ('ORDMAILNTF')
) ATR ON ATR."IdTipoEntidad" = 'PORTAFOLIO'
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" I ON I."IdEntidad" = P."IdPortafolio" AND I."IdTipoEntidad" = 'PORTAFOLIO' AND ATR."IdIndicador" = I."IdIndicador"
WHERE Cliente."IdCliente" = :IdCliente) dirElecTable;