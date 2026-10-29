SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarEstadosOperacionesMesa";
GO
CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarEstadosOperacionesMesa" AS
SELECT "IdTipoProducto"
	 , "IdEstado"
     , "NombreEntidad"
     , CASE WHEN "IdEstado" = 'ANU' THEN 'ANULADO'
     	ELSE CASE WHEN "IdEstado" = 'VIG' THEN 'VIGENTE'
     		ELSE CASE WHEN "IdEstado" = 'LIQ' THEN 'LIQUIDADO'
     			ELSE CASE WHEN "IdEstado" = 'ANT' THEN 'ANTICIPADO'
     				ELSE CASE WHEN "IdEstado" = 'EMI' THEN 'EMITIDO'
     					ELSE CASE WHEN "IdEstado" = 'RGN' THEN 'REASIGNADO' END 
     					END
     				END
     			END
     		END
     	END AS "Descripcion"
  FROM "BSCL.Mesa.BD::MES.EstadoOperacion" WHERE "IdEstado" IN ('VIG','LIQ','ANT','ANU','EMI','RGN') AND "IdTipoProducto" = 'MERF' AND "IdEstadoSiguiente" = ''
  ;
GO 
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::ConsultarEstadosOperacionesMesa" TO BCS_DESARROLLADOR WITH GRANT OPTION;
