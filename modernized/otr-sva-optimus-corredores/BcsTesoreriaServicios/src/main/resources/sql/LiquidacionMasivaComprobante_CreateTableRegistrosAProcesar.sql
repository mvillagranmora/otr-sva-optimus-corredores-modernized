-- Session table with the records handed to the EdicionMultiple procedure
CREATE LOCAL TEMPORARY TABLE #p_RegistrosAProcesar (
    "Id" BIGINT,
    "Entidad" NVARCHAR(300),
    "Accion" NVARCHAR(3)
)
