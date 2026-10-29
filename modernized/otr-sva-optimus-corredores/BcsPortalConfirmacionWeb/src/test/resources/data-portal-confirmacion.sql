-- Test data for BcsPortalConfirmacionWeb integration tests.

-- Confirmation macro template body
INSERT INTO EVT_TAG_GENERALES (LLAVE, VALOR_DEFAULT)
VALUES ('Confirmar', '<div class="macro"><!-- confirmacion --></div>');

-- Platform URL for empresa 1
INSERT INTO PLA_CONFIGURACION (ID_EMPRESA, CODIGO_PARAMETRO, VALOR)
VALUES (1, 'PLA.UrlHana', 'http://bcs-platform-test.internal');

-- Test notification pending acceptance
INSERT INTO EVT_NOTIFICACION (TOKEN_CONFIRMACION, OBSERVACION, CONFIRMADO, ACCION_CONFIRMADA, FECHA_CONFIRMADO)
VALUES ('TOKEN-001', NULL, 'N', NULL, NULL);
