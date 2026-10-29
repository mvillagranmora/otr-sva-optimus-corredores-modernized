SELECT 
E."IdIndicador"
,E."IdEnrolamiento"
,E."IdTipoEntidad"
,E."IdTipoIndicador"
,E."ModeloCliente"
,E."IdEmpresa"
,E."ValorIndicador"
,E."IdIndicadorPersona"
,I."CodigoTipoIndicador"
,I."IdTipoPersona"
,I."IdEstado"
,I."ClaseDato"
FROM "BSCL.SocioNegocio.BD::ENR.Indicador" E
LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" I ON E."IdTipoIndicador"=I."IdTipoIndicador"
WHERE E."IdEnrolamiento" IN (:IdEnrolamiento);
