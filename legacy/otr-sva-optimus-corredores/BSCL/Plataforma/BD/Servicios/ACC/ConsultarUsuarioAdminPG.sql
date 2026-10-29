CREATE OR REPLACE PROCEDURE "BSCL.Plataforma.BD.Servicios::ACC.ConsultarUsuarioAdmin" (
  IN  "p_IdEmpresa"        BIGINT,       -- ID Empresa destino
  IN  "p_IdUsuario"        BIGINT,       -- ID del usuario que solicita el servicio
  OUT "p_out_EsAdmin"      VARCHAR(1)    -- 'S' o 'N'  (NVARCHAR(1) -> VARCHAR(1))
)
LANGUAGE plpgsql
SECURITY INVOKER
AS $$
DECLARE
  "v_cantidad_roles_admin" BIGINT;
  "IdRolAdmin"             VARCHAR(520); -- NVARCHAR(520) -> VARCHAR(520)
BEGIN
  -- Emula el bloque interno con handler: si algo falla dentro,
  -- relanza como ERROR_CLIENTE_INVALIDO (10012).
  BEGIN
    -- Obtener valor de configuración ACC.IdRolAdmin (OUT => "IdRolAdmin")
    CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion"(
      "p_IdEmpresa",
      'ACC.IdRolAdmin',
      "IdRolAdmin"
    );

    -- Contar roles autorizados del usuario que coincidan con IdRolAdmin
    SELECT COUNT("IdRol")
    INTO   "v_cantidad_roles_admin"
    FROM   "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesUsuario"
    WHERE  "EsAutorizado" = 'S'
       AND "IdEmpresa"    = "p_IdEmpresa"
       AND "IdUsuario"    = "p_IdUsuario"
       AND "IdRol"        = CAST("IdRolAdmin" AS BIGINT);

    IF "v_cantidad_roles_admin" > 0 THEN
      "p_out_EsAdmin" := 'S';
    ELSE
      "p_out_EsAdmin" := 'N';
    END IF;

  
  END;
END;
$$;

-- Ejemplo de uso en psql (el OUT se muestra automáticamente):
-- CALL "BSCL.Plataforma.BD.Servicios::ACC.ConsultarUsuarioAdmin"(123, 456);