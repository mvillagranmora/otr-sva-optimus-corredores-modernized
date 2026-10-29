INSERT INTO "BSCL.Plataforma.BD::IND.Indicador"
(
"IdIndicador"
,"IdEntidad"
,"IdTipoEntidad"
,"IdTipoIndicador"
,"ModeloCliente"
,"IdEmpresa"
,"ValorIndicador"
,"FechaIngreso"
,"FechaCreacion"
,"FechaModificacion"
,"IdEstadoAutorizacion"
)
VALUES (
:IdIndicador
,:IdEntidad
,:IdTipoEntidad
,:IdTipoIndicador
,:ModeloCliente
,:IdEmpresa
,:ValorIndicador
,:FechaIngreso
,CURRENT_UTCTIMESTAMP
,CURRENT_UTCTIMESTAMP
,:IdEstadoAutorizacion
);