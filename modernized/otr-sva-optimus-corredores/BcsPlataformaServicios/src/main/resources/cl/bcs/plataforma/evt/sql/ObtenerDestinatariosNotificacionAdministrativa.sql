--DESTINATARIOS PRINCIPALES
SELECT
'NOTIFICACION_ADMINISTRADOR' AS "CodigoTipoEvento",
"BSCL.Plataforma.BD.Secuencias::EventoNotificacion".nextval AS "IdSecuencia",
CURRENT_UTCTIMESTAMP AS "FechaCreacionSistema",
TO_BIGINT(0) AS "IdPersonaRemitente",
CE."IdPersona" AS "IdPersonaDestinatario",
TO_BIGINT(0) AS "IdPortafolio",
CASE WHEN IFNULL(CE."IdDireccionElectronica",0)=0 THEN CE."IdDireccionElectronicaPrincipal" ELSE CE."IdDireccionElectronica" END AS "IdDireccion",
'TO' AS "TipoDestinatario"
FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE
WHERE CE."DocumentoIdentidad" = :DocumentoIdentidad