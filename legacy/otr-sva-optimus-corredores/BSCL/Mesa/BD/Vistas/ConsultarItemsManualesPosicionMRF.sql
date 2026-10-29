SET SCHEMA "BSCL";

DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarItemsManualesPosicionMRF";

CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarItemsManualesPosicionMRF" AS
SELECT
	 MET."IdMetodo"
	,MET."NombreMetodo"
	,MET."Glosa"  		
	,MET."IdTipoReajuste"
	,MET."IdMesa"
	--,MET."IdEstado" AS "IdEstadoMET"
	,MEP."IdMatriz"
	--,MEP."IdMetodo" AS "IdMetodoMEP"
	--,MEP."IdEvento" AS "IdEventoMEP"
	,MEP."IdAgrupacion"
	--,MEP."IdEstado" AS "IdEstadoMEP"
	,EVE."IdEvento"
	--,EVE."IdOperacion" AS "IdOperacionEVE"
	,EVE."IdTipoEvento"
	,EVE."IndAccion"
	--,EVE."IdEstado" AS "IdEstadoEVE"
	,OPE."IdOperacion"
	,OPE."IdTipoProducto"
	,OPE."IdTipoOperacion"
	,OPE."IdTipoComprobante"
	,OPE."NombreEvento"
	,OPE."IdTipoEstado"
	--,OPE."IdEstado" AS "IdEstadoTOP"
	,PRM."IdEmpresa"
	,PRM."IdParametro"
	,PRM."NombreTipoParametro"
	,PRM."ValorParametro"
	,PRM."NombreParametro"
	--,PRM."IdEstado" AS "IdEstadoPRM"
	,POS."IdPosicion"
	,POS."Fecha"
	,POS."IdMatriz" AS "IdMatrizPOS"
	,POS."MontoEvento"
	,COALESCE(USR."CodigoUsuario",'') AS "CodigoUsuario"
	,COALESCE(POS."FechaUltimaModificacion",'0001-01-01') AS "FechaUltimaModificacion"
FROM "BSCL.Mesa.BD.Vistas::MRF.ConsultarMetodosPosicionMRF" MET
LEFT JOIN "BSCL.Mesa.BD::MES.MetodoEventoPosicion" MEP
	ON (MET."IdMetodo" = MEP."IdMetodo")
LEFT JOIN "BSCL.Mesa.BD::MES.EventoPosicion" EVE
	ON (MEP."IdEvento" = EVE."IdEvento")
LEFT JOIN "BSCL.Mesa.BD::MES.TipoOperacionPosicion" OPE
	ON (EVE."IdOperacion" = OPE."IdOperacion")
LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarParametro" PRM
	ON (MEP."IdAgrupacion" = PRM."ValorParametro" AND UPPER(PRM."NombreTipoParametro") = 'GRUPO_POSMRF')
LEFT JOIN "BSCL.Mesa.BD::MES.PosicionMRF" POS
	ON (MEP."IdMatriz" = POS."IdMatriz")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto" USR	ON (POS."IdUsuario" = USR."IdUsuario")
WHERE MET."IdEstado" = 'VIG'
  AND MEP."IdEstado" = 'VIG'
  AND EVE."IdEstado" = 'VIG'
  AND EVE."IdTipoEvento" = CASE WHEN MET."IdTipoPosicion" = 'INICIAL' THEN EVE."IdTipoEvento" ELSE 'M' END
  AND OPE."IdEstado" = 'VIG'
  AND PRM."IdEstado" = 'VIG'
;

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::ConsultarItemsManualesPosicionMRF" TO BCS_DESARROLLADOR WITH GRANT OPTION;
