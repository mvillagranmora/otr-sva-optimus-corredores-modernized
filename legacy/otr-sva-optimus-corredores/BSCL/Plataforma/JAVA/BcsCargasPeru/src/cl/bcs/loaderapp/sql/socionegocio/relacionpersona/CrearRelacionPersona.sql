insert into "BSCL.SocioNegocio.BD::REL.RelacionPersona"
(                   
    "IdRelacionPersona",
    "IdTipoRelacion",
    "IdPersonaOrigen",
    "IdPersonaDestino",
    "IdEstado",
    "FechaInicioRelacion",
    "FechaFinRelacion",
    "FechaIngreso",
    "FechaCreacion",
    "FechaModificacion",
    "IdUsuarioCreador",
    "IdEstadoAutorizacion",
    "PorcentajeParticipacion",
    "RetiraDocumentos",
    "FinalidadRelacion",
    "CondicionesRelacion",
    "IdGradoVinculacionParental") 
VALUES (
    "BSCL.SocioNegocio.BD.Secuencias::RelacionPersona".NEXTVAL,
    p_IdTipoRelacion,
    p_IdPersonaOrigen,
    p_IdPersonaDestino,
    p_IdEstado,
    p_FechaInicioRelacion,
    p_FechaFinRelacion,
    v_FechaIngreso,--TO_TIMESTAMP(p_FechaIngreso,'YYYY-MM-DD HH24:MI:SS.FF7')                   
    CURRENT_UTCTIMESTAMP, --p_Creacion, -- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
    CURRENT_UTCTIMESTAMP,
    p_IdUsuario, --IdUsuarioCreador
    v_IdEstadoAutorizacion,
    p_PorcentajeParticipacion,
    p_RetiraDocumentos,
    p_FinalidadRelacion,
    p_CondicionesRelacion,
    p_IdGradoVinculacionParental);