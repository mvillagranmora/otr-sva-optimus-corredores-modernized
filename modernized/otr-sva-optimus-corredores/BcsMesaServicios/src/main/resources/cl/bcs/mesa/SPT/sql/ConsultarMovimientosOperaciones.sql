 SELECT  
	TO_VARCHAR(ADD_SECONDS(t1."FechaOperacion",TO_INTEGER(conf."Valor")),'yyyy-MM-dd') AS "FechaOperacion",T1."IdTipoOperacion" AS "IdTipoOperacion",T1."TipoOperacion" AS "TipoOperacion",T1."Nemotecnico" AS "Nemotecnico",T1."IdOperacion" AS "FolioOperacion" ,T1."MontoMonedaPrincipal" AS "MontoMonedaPrincipal" ,T1."IdMonedaPrincipal" AS "IdMonedaPrincipal",T1."TipoCambioCierre" AS "TipoCambioCierre",T1."MontoMonedaSecundaria" AS "MontoMonedaSecundaria", T1."IdMonedaSecundaria" AS "IdMonedaSecundaria",T1."Estado" AS "IdEstado",ESTADOFM."Nombre" AS "Estado",T1."RutCliente" AS "DocumentoIdentidad", CP."CodigoPortafolio" AS "CodigoPortafolio",T1."NombreCliente" AS "NombreCliente",t1."TipoComprobanteFacturacion" AS "IdTipoComprobante",t2."Nombre" AS "NombreComprobante",EMP."CodigoEmpleado" AS "CodigoAgente"
	FROM(
SELECT
	"IdTipoOperacion"
	,"TipoOperacion"
	,"IdEmpresa"
	,"FechaOperacion"
	,"Accion"
	,"IdOperacion"
	,"MontoMonedaPrincipal"
	,"IdMonedaPrincipal"
	,"TipoCambioCierre" 
	,"MontoMonedaSecundaria" 
	,"IdMonedaSecundaria"
	,"Estado"
	,"RutCliente"
	,"CodigoPropositoPortafolio"
	,"NombreCliente"
	,"NombreAgente"
	,"TipoComprobanteFacturacion"
	,"Mesa"
	, '0001-01-01' AS "FechaRespaldo"
	,"IdPersona"
	,"Nemotecnico"
	,"IdPortafolio"
FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot") AS t1 
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS conf
	ON (conf."IdEmpresa" = t1."IdEmpresa" AND conf."CodigoParametro" = 'PLA.SegundosZonaHoraria')
LEFT JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" t2
ON t2."IdTipoComprobante" = t1."TipoComprobanteFacturacion"
LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" EMP 
ON EMP."IdPersona" = T1."IdPersona"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" CP
ON CP."IdPortafolio" = T1."IdPortafolio"
LEFT OUTER JOIN  "BSCL.Plataforma.BD::PLA.Estado" AS ESTADOFM
ON ESTADOFM."IdEstado" = T1."Estado"
WHERE 
((t1."FechaOperacion" BETWEEN :fechaDesde and  :fechaHasta) AND T1."Mesa"='MEME' AND  (T1."FechaRespaldo" = '0001-01-01' or T1."Mesa" = 'MEME')) AND 
(:p_DocIdentidad IS NULL or T1."RutCliente" LIKE (:p_DocIdentidad)) AND
(:p_CodigoPortafolio IS NULL or T1."CodigoPropositoPortafolio" LIKE (:p_CodigoPortafolio)) AND
(:Accion IS NULL OR (T1."Accion" LIKE :Accion AND T1."Nemotecnico" LIKE :nemotecnico )) AND
(:estado IS NULL OR T1."Estado"  LIKE :estado) AND
(:CodigoAgente IS NULL OR EMP."CodigoEmpleado" like :CodigoAgente);