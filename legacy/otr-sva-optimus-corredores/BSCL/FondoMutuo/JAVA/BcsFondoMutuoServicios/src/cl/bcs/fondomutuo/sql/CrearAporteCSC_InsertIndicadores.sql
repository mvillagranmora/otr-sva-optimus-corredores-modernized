INSERT INTO "BSCL.Plataforma.BD::IND.Indicador" ("IdIndicador","IdEntidad", "IdTipoEntidad", 
"IdTipoIndicador", "ModeloCliente", "IdEmpresa", "ValorIndicador", "FechaIngreso", "FechaCreacion", 
"FechaModificacion", "IdEstadoAutorizacion", "FechaVencimiento", "IdCarga", "IndIdOrigen", "IdOrigen", "IdCuentaOrigen")
SELECT "BSCL.Plataforma.BD.Secuencias::Indicador".NEXTVAL
    ,:p_IdPortafolio
    ,'PORTAFOLIO'
    ,22
    ,Cliente."IdTipoCliente"
    ,:p_IdEmpresa
    ,'S'
    ,CURRENT_UTCTIMESTAMP
    ,CURRENT_UTCTIMESTAMP
    ,CURRENT_UTCTIMESTAMP
    ,'ING'
    ,'0001-01-01 00:00:01.000'
    ,0
    ,NULL
    ,NULL
    ,NULL
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" Cliente
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" P ON Cliente."IdCliente" = P."IdCliente" AND P."IdPortafolio" = :p_IdPortafolio;

