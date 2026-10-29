-- BSCL."BSCL.Contabilidad.BD.Vistas::ConsultaComboboxTipoControl" source
SET SCHEMA BSCL;
GO
DROP VIEW "BSCL.Contabilidad.BD.Vistas::ConsultaComboboxTipoControl";
GO
CREATE VIEW "BSCL.Contabilidad.BD.Vistas::ConsultaComboboxTipoControl" AS
((SELECT 'S' AS "IdValorTipoControl" ,'Por Bolsa' AS "ValorTipoControl" FROM DUMMY) UNION ALL (SELECT 'N' AS "IdValorTipoControl" ,'Por Cliente' AS "ValorTipoControl" FROM DUMMY))
WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Contabilidad.BD.Vistas::ConsultaComboboxTipoControl" TO  BCS_DESARROLLADOR WITH GRANT OPTION;		  
GO
