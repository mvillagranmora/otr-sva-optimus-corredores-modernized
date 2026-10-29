INSERT INTO "BSCL.SocioNegocio.BD::EJE.Ejecutivo"
(
    "IdEmpleadoEjecutivo",
    "IdEmpleadoSupervisor",
    "FechaCreacion",
    "IdUsuarioCreacion",
    "IdEstado"
) VALUES (
    :IdEmpleadoEjecutivo,
    :IdEmpleadoSupervisor,
    TO_SECONDDATE(:Fecha, 'YYYY-MM-DD HH24:MI:SS'),
    :IdUsuario,
    :IdEstado
);
